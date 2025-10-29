package com.juvarya.product.service.impl;

import com.hlt.auth.exception.handling.ErrorCode;
import com.hlt.auth.exception.handling.HltCustomerException;
import com.juvarya.product.azure.service.AwsBlobService;
import com.juvarya.product.client.UserMgmtClient;
import com.juvarya.product.dto.*;
import com.juvarya.product.dto.response.ProductAttributeResponse;
import com.juvarya.product.model.*;
import com.juvarya.product.populator.ProductPopulator;
import com.juvarya.product.repository.CategoryRepository;
import com.juvarya.product.repository.ProductRepository;
import com.juvarya.product.service.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    private final AwsBlobService awsBlobService;
    private final MediaService mediaService;
    private final UserMgmtClient userMgmtClient;
    private final ShopifySyncService shopifySyncService;
    private final ProductPopulator productPopulator;
    private final ProductAvailabilityScheduler productAvailabilityScheduler;

    public static final String START_TIME = "startTime";
    public static final String END_TIME = "endTime";
    public static final String LOGIN_TIME = "loginTime";

//    @CachePut(value = "products", key = "#result.id")
    @Override
    @Transactional
    public ProductDTO createOrUpdateProduct(ProductDTO dto) throws IOException {
        var business = validateBusinessApproval(dto.getBusinessId());
        var category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new HltCustomerException(ErrorCode.CATEGORY_NOT_FOUND));

        var product = Optional.ofNullable(dto.getId())
                .flatMap(productRepository::findById)
                .orElseGet(() -> {
                    if (productRepository.existsByBusinessIdAndShortCode(business.getId(), dto.getShortCode())) {
                        throw new HltCustomerException(ErrorCode.PRODUCT_ALREADY_EXISTS);
                    }
                    return new ProductModel();
                });

        updateProductDetails(product, dto, business.getId(), category);
        updateProductAttributes(product, dto.getAttributes());
        updateProductMedia(product, dto);

        var savedProduct = productRepository.save(product);
        if (savedProduct.getShopifyProductId() == null) {
            shopifySyncService.syncToShopify(savedProduct);
        }

        var response = new ProductDTO();
        productPopulator.populate(savedProduct, response);
        return response;
    }

    private void updateProductDetails(ProductModel product, ProductDTO dto, Long businessId, CategoryModel category) {
        product.setCategory(category);
        product.setBusinessId(businessId);
        product.setDescription(dto.getDescription());
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        product.setShortCode(dto.getShortCode());
        product.setDiscount(dto.isDiscount());
        product.setIgnoreTax(dto.isIgnoreTax());
        product.setAvailable(dto.isAvailable());
    }

    private void updateProductAttributes(ProductModel product, List<ProductAttributeDTO> attributeDTOs) {
        if (attributeDTOs == null) return;

        var attributes = product.getAttributes();
        if (attributes == null) {
            attributes = new HashSet<>();
            product.setAttributes(attributes);
        } else {
            attributes.clear();
        }

        for (var attr : attributeDTOs) {
            var attribute = new ProductAttributeModel();
            attribute.setProduct(product);
            attribute.setAttributeName(attr.getAttributeName());
            attribute.setAttributeValue(attr.getAttributeValue());
            attributes.add(attribute);
        }
    }

    private void updateProductMedia(ProductModel product, ProductDTO dto) throws IOException {
        var mediaList = new ArrayList<MediaModel>();

        if (!CollectionUtils.isEmpty(dto.getMediaFiles())) {
            var uploaded = awsBlobService.uploadFiles(dto.getMediaFiles());
            uploaded.forEach(media -> {
                media.setMediaType("PRODUCT");
                mediaService.saveMedia(media);
            });
            mediaList.addAll(uploaded);
        } else if (dto.getId() != null) {
            var existing = productRepository.findById(dto.getId())
                    .orElseThrow(() -> new HltCustomerException(ErrorCode.PRODUCT_NOT_FOUND));
            if (!CollectionUtils.isEmpty(existing.getMedia())) {
                mediaList.addAll(existing.getMedia());
            }
        }

        product.setMedia(mediaList);
    }

    @Override
    @Transactional
    public ProductDTO addProductTimingAttributes(Long productId, List<ProductAttributeDTO> attributes, String durationKeyword) {
        var product = productRepository.findById(productId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.PRODUCT_NOT_FOUND));

        var useLoginTimeAsEndTime = durationKeyword != null && !durationKeyword.isBlank();
        String endTimeStr;

        if (useLoginTimeAsEndTime) {
            endTimeStr = fetchLoginTime(product.getBusinessId());
            validateOnlyStartTime(attributes);
        } else {
            validateStartAndEndTime(attributes);
            endTimeStr = getEndTime(attributes);
        }

        var startTimeStr = getStartTime(attributes);
        var attributesSet = prepareAttributeSet(product);
        attributesSet.removeIf(attr -> isTimingAttribute(attr.getAttributeName()));
        attributesSet.add(buildAttribute(product, START_TIME, startTimeStr));
        if (endTimeStr != null) {
            attributesSet.add(buildAttribute(product, END_TIME, endTimeStr));
        }

        product.setAvailable(false);
        var saved = productRepository.save(product);

        if (endTimeStr != null) {
            productAvailabilityScheduler.scheduleReactivation(product.getId(), endTimeStr);
        }

        var dto = new ProductDTO();
        productPopulator.populate(saved, dto);
        return dto;
    }

    @Override
    public Page<ProductDTO> getProductsByBusinessIdAndAttributeValues(Long businessId, String attributeName, List<String> attributeValues, String search, Pageable pageable) {

        if (attributeValues == null || attributeValues.isEmpty()) {
            throw new HltCustomerException(ErrorCode.INVALID_INPUT, "Attribute values cannot be empty");
        }

        var productPage = productRepository.findByBusinessIdAndAttributeValuesWithSearch(
                businessId, attributeName, attributeValues, search, pageable
        );

        if (productPage.isEmpty()) throw new HltCustomerException(ErrorCode.PRODUCT_NOT_FOUND);

        return productPage.map(product -> {
            var dto = new ProductDTO();
            productPopulator.populate(product, dto);
            return dto;
        });
    }


    private Set<ProductAttributeModel> prepareAttributeSet(ProductModel product) {
        return Optional.ofNullable(product.getAttributes())
                .orElseGet(() -> {
                    var set = new HashSet<ProductAttributeModel>();
                    product.setAttributes(set);
                    return set;
                });
    }

    private ProductAttributeModel buildAttribute(ProductModel product, String name, String value) {
        var attr = new ProductAttributeModel();
        attr.setProduct(product);
        attr.setAttributeName(name);
        attr.setAttributeValue(value);
        return attr;
    }

    private boolean isTimingAttribute(String name) {
        return START_TIME.equalsIgnoreCase(name) || END_TIME.equalsIgnoreCase(name);
    }

    private void validateOnlyStartTime(List<ProductAttributeDTO> attrs) {
        for (var attr : attrs) {
            if (!START_TIME.equalsIgnoreCase(attr.getAttributeName())) {
                throw new HltCustomerException(ErrorCode.INVALID_ATTRIBUTE, "Only startTime allowed when durationKeyword is used.");
            }
        }
    }

    private void validateStartAndEndTime(List<ProductAttributeDTO> attrs) {
        for (var attr : attrs) {
            var name = attr.getAttributeName();
            if (!START_TIME.equalsIgnoreCase(name) && !END_TIME.equalsIgnoreCase(name)) {
                throw new HltCustomerException(ErrorCode.INVALID_ATTRIBUTE, "Only startTime and endTime allowed.");
            }
        }
    }

    private String getStartTime(List<ProductAttributeDTO> attrs) {
        return attrs.stream()
                .filter(attr -> START_TIME.equalsIgnoreCase(attr.getAttributeName()))
                .map(ProductAttributeDTO::getAttributeValue)
                .findFirst()
                .orElseThrow(() -> new HltCustomerException(ErrorCode.INVALID_ATTRIBUTE, "startTime is required"));
    }

    private String getEndTime(List<ProductAttributeDTO> attrs) {
        return attrs.stream()
                .filter(attr -> END_TIME.equalsIgnoreCase(attr.getAttributeName()))
                .map(ProductAttributeDTO::getAttributeValue)
                .findFirst()
                .orElse(null);
    }

    private String fetchLoginTime(Long businessId) {
        var business = userMgmtClient.getBusinessById(businessId);
        return business.getAttributes().stream()
                .filter(attr -> LOGIN_TIME.equalsIgnoreCase(attr.getAttributeName()))
                .map(ProductAttributeResponse::getAttributeValue)
                .findFirst()
                .orElseThrow(() -> new HltCustomerException(ErrorCode.BUSINESS_TIMING_NOT_FOUND, "loginTime missing in business settings"));
    }

    private B2BUnitDTO validateBusinessApproval(Long businessId) {
        var business = userMgmtClient.getBusinessById(businessId);
        if (business == null || !business.isApproved()) {
            throw new HltCustomerException(ErrorCode.BUSSINESS_NOT_APPROVED);
        }
        return business;
    }

    private ProductDTO mapProductToDTO(ProductModel product) {
        var dto = modelMapper.map(product, ProductDTO.class);
        if (product.getCategory() != null) {
            dto.setCategoryName(product.getCategory().getName());
        }
        if (product.getBusinessId() != null) {
            try {
                var business = userMgmtClient.getBusinessById(product.getBusinessId());
                if (business != null) {
                    dto.setBusinessName(business.getBusinessName());
                }
            } catch (Exception e) {
                log.warn("Failed to fetch business for product {}: {}", product.getId(), e.getMessage());
            }
        }
        return dto;
    }


    @Override
//    @Cacheable(
//            value = "productsByBusinessId",
//            key = "#businessId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize + '-' + (#keyword != null ? #keyword.trim() : '')"
//    )
    public Page<ProductDTO> getProductsByBusinessId(Long businessId, Pageable pageable, String keyword) {
        var page = (keyword != null && !keyword.isBlank())
                ? productRepository.searchByKeywordAndBusinessId(businessId, keyword.trim(), pageable)
                : productRepository.findByBusinessId(businessId, pageable);

        if (page.isEmpty()) throw new HltCustomerException(ErrorCode.PRODUCT_NOT_FOUND);
        return page.map(this::mapProductToDTO);
    }


//    @Cacheable(key = "#id", value = "productCache")
    @Override
    public ProductDTO getProductById(Long id) {
        log.info("Fetching product from DB for id {}", id);
        var product = productRepository.findById(id)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.PRODUCT_NOT_FOUND));

        ProductModel filteredProduct = filterTimingAttributes(product);

        return mapProductToDTO(filteredProduct);
    }

    private ProductModel filterTimingAttributes(ProductModel product) {
        if (CollectionUtils.isEmpty(product.getAttributes())) {
            return product;
        }

        LocalTime currentTime = LocalTime.now(java.time.ZoneId.of("Asia/Kolkata"));
        LocalDate currentDate = LocalDate.now(java.time.ZoneId.of("Asia/Kolkata"));

        String endTimeStr = product.getAttributes().stream()
                .filter(attr -> END_TIME.equalsIgnoreCase(attr.getAttributeName()))
                .map(ProductAttributeModel::getAttributeValue)
                .findFirst()
                .orElse(null);

        if (endTimeStr != null) {
            try {
                LocalTime endTime = LocalTime.parse(endTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
                if (currentTime.isAfter(endTime)) {
                    Set<ProductAttributeModel> filteredAttributes = product.getAttributes().stream()
                            .filter(attr -> !isTimingAttribute(attr.getAttributeName()))
                            .collect(Collectors.toSet());
                    product.setAttributes(filteredAttributes);
                }
            } catch (DateTimeParseException e) {
                log.warn("Invalid endTime format for product {}: {}", product.getId(), endTimeStr);
            }
        }

        return product;
    }

    @Override
    @Transactional
    public ProductDTO toggleProductAvailability(Long id) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.PRODUCT_NOT_FOUND));
        product.setAvailable(!product.isAvailable());
        return mapProductToDTO(productRepository.save(product));
    }

    @Override
    public Page<ProductDTO> searchProducts(String query, Pageable pageable) {
        Page<ProductModel> page = productRepository.searchProducts(
                query, query, query, pageable
        );
        if (page.isEmpty()) throw new HltCustomerException(ErrorCode.PRODUCT_NOT_FOUND);
        return page.map(this::mapProductToDTO);
    }

    @Override
//    @Cacheable(
//            value = "productsByBusinessIdWithAttr",
//            key = "#businessId + '-' + T(java.lang.String).join(',', #attrValues) + '-' + (#keyword != null ? #keyword.trim() : '') + '-' + #pageable.pageNumber + '-' + #pageable.pageSize"
//    )
    public Page<ProductDTO> getProductsByBusinessIdWithAttributeValue(
            Long businessId, List<String> attrValues, String keyword, Pageable pageable) {

        var page = productRepository.findByBusinessIdAndAttributeValueIn(businessId, attrValues, keyword, pageable);

        if (page.isEmpty()) throw new HltCustomerException(ErrorCode.PRODUCT_NOT_FOUND);

        return page.map(product -> {
            var dto = new ProductDTO();
            productPopulator.populate(product, dto);
            return dto;
        });
    }



    @Override
    @Transactional
    public String deleteProductById(Long id) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.PRODUCT_NOT_FOUND));
        productRepository.delete(product);
        return "Product with ID " + id + " deleted successfully.";
    }


    @Override
    public Page<ProductDTO> getProductsByCategoryNameAndBusiness(String categoryName, Long businessId, Pageable pageable) {
        var page = productRepository.findByCategoryNameAndBusinessId(categoryName, businessId, pageable);
        if (page.isEmpty()) throw new HltCustomerException(ErrorCode.PRODUCT_NOT_FOUND);
        return page.map(this::mapProductToDTO);
    }

    @Override
    public Page<BusinessWithProductsDTO> searchNearbyProducts(String productName, Double latitude, Double longitude, double radius, String categoryName, Pageable pageable) {
        if (latitude == null || longitude == null) {
            log.warn("Latitude and Longitude are required for nearby search.");
            return Page.empty(pageable);
        }

        List<B2BUnitDTO> nearbyUnits = fetchNearbyUnits(latitude, longitude, radius, categoryName);
        if (nearbyUnits.isEmpty()) return Page.empty(pageable);

        Map<Long, B2BUnitDTO> unitMap = mapUnitsById(nearbyUnits);
        List<Long> businessIds = new ArrayList<>(unitMap.keySet());

        List<ProductModel> productModels = fetchProducts(productName, businessIds);
        if (productModels.isEmpty()) return Page.empty(pageable);

        List<ProductDTO> filteredProducts = mapAndFilterProducts(productModels, unitMap);

        Map<Long, List<ProductDTO>> groupedByBusiness = filteredProducts.stream()
                .collect(Collectors.groupingBy(ProductDTO::getBusinessId));

        List<BusinessWithProductsDTO> businessDTOs = buildBusinessProductDTOs(unitMap, groupedByBusiness);

        return paginate(businessDTOs, pageable);
    }

    private List<B2BUnitDTO> fetchNearbyUnits(Double lat, Double lon, double radius, String categoryName) {
        return Optional.ofNullable(
                userMgmtClient.findNearbyUnits(lat, lon, radius, null, null, categoryName, 0, 100).getContent()
        ).orElse(Collections.emptyList());
    }

    private Map<Long, B2BUnitDTO> mapUnitsById(List<B2BUnitDTO> units) {
        return units.stream().collect(Collectors.toMap(B2BUnitDTO::getId, Function.identity()));
    }

    private List<ProductModel> fetchProducts(String productName, List<Long> businessIds) {
        Page<ProductModel> page = (productName != null && !productName.isBlank())
                ? productRepository.findByBusinessIdInAndNameContainingIgnoreCase(businessIds, productName, Pageable.unpaged())
                : productRepository.findByBusinessIdIn(businessIds, Pageable.unpaged());
        return page.getContent();
    }

    private List<ProductDTO> mapAndFilterProducts(List<ProductModel> models, Map<Long, B2BUnitDTO> unitMap) {
        return models.stream()
                .map(product -> {
                    ProductDTO dto = modelMapper.map(product, ProductDTO.class);
                    B2BUnitDTO unit = unitMap.get(product.getBusinessId());
                    if (unit != null) {
                        dto.setBusinessId(unit.getId());
                        dto.setBusinessName(unit.getBusinessName());
                        dto.setCategoryName(unit.getCategoryName());
                    }
                    return dto;
                })
                .filter(dto ->
                        dto.getAttributes() == null || dto.getAttributes().stream()
                                .noneMatch(attr -> "ProductType".equalsIgnoreCase(attr.getAttributeName())
                                        && "DineIn".equalsIgnoreCase(attr.getAttributeValue()))
                )
                .toList();
    }

    private List<BusinessWithProductsDTO> buildBusinessProductDTOs(Map<Long, B2BUnitDTO> unitMap, Map<Long, List<ProductDTO>> groupedProducts) {
        return unitMap.values().stream()
                .filter(unit -> groupedProducts.containsKey(unit.getId()))
                .map(unit -> {
                    BusinessWithProductsDTO dto = new BusinessWithProductsDTO();
                    dto.setId(unit.getId());
                    dto.setName(unit.getBusinessName());
                    dto.setCategory(unit.getCategoryName());
                    dto.setApproved(unit.isApproved());
                    dto.setCreatedAt(unit.getCreationDate());
                    dto.setAttributes(unit.getAttributes());
                    dto.setProducts(groupedProducts.get(unit.getId()));
                    return dto;
                })
                .toList();
    }

    private Page<BusinessWithProductsDTO> paginate(List<BusinessWithProductsDTO> dtos, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), dtos.size());
        if (start >= end) return Page.empty(pageable);
        return new PageImpl<>(dtos.subList(start, end), pageable, dtos.size());
    }

}