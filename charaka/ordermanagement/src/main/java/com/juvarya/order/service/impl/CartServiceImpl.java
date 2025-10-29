package com.juvarya.order.service.impl;

import com.hlt.auth.exception.handling.ErrorCode;
import com.hlt.auth.exception.handling.HltCustomerException;
import com.hlt.commonservice.dto.StandardResponse;
import com.juvarya.order.client.ProductClient;
import com.juvarya.order.client.UserMgmtClient;
import com.juvarya.order.dao.CartItemRepository;
import com.juvarya.order.dao.CartRepository;
import com.juvarya.order.dto.*;
import com.juvarya.order.dto.enums.OfferTargetType;
import com.juvarya.order.dto.enums.OfferType;
import com.juvarya.order.dto.request.CartRequest;
import com.juvarya.order.entity.CartItemModel;
import com.juvarya.order.entity.CartModel;
import com.juvarya.order.populator.CartItemPopulator;
import com.juvarya.order.populator.CartPopulator;
import com.juvarya.order.service.CartService;
import com.juvarya.order.service.ShopifyCartService;
import feign.FeignException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartPopulator cartPopulator;
    private final CartItemPopulator cartItemPopulator;
    private final ProductClient productClient;
    private final UserMgmtClient userMgmtClient;
    private final ShopifyCartService shopifyCartService;

    @Override
    public CartDTO createCart(Long userId) {
        Optional<CartModel> existingCartOpt = cartRepository.findByUserId(userId);

        CartModel cartModel = existingCartOpt.orElseGet(() -> {
            CartModel newCart = new CartModel();
            newCart.setUserId(userId);
            newCart.setStatus("CREATED");
            return cartRepository.save(newCart);
        });

        return getCartDTO(cartModel);
    }

    @NotNull
    private CartDTO getCartDTO(CartModel cartModel) {
        CartDTO cartDTO = new CartDTO();
        cartPopulator.populate(cartModel, cartDTO);
        return cartDTO;
    }


    @Override
    public CartDTO
    getCartByUserId(Long userId) {
        CartModel cartModel = getCartModel(userId);
        return getCartDTO(cartModel);
    }

    @Transactional
    @Override
    public List<CartItemDTO> addItemsToCart(Long userId, CartRequest cartRequest) {
        List<CartItemDTO> cartItemDTOs = cartRequest.getItems();
        String cartNotes = cartRequest.getNotes();

        if (cartItemDTOs == null || cartItemDTOs.isEmpty()) {
            throw new HltCustomerException(ErrorCode.INVALID_CART_ITEM, "Cart items cannot be empty.");
        }

        // Fetch or create cart
        CartModel cartModel = getCartModel(userId);
        cartModel.setNotes(cartNotes);
        cartRepository.save(cartModel);

        // Clear all existing cart items before saving new ones
        cartItemRepository.deleteByCartId(cartModel.getId());

        List<CartItemDTO> savedCartItemDTOs = new ArrayList<>();

        for (CartItemDTO cartItemDTO : cartItemDTOs) {
            // Skip items with 0 quantity
            if (cartItemDTO.getQuantity() == 0) {
                continue;
            }

            ProductDTO product = validateAndGetProduct(cartItemDTO);
            BigDecimal finalPrice = calculatePrice(product);
            updateCartBusinessInfoIfNeeded(cartModel, product);

            CartItemModel cartItemModel = new CartItemModel();
            cartItemModel.setCart(cartModel);
            cartItemModel.setProductId(product.getId());
            cartItemModel.setQuantity(cartItemDTO.getQuantity());
            cartItemModel.setPrice(finalPrice);
            cartItemModel.setCategoryId(product.getCategoryId());

            CartItemModel savedItem = cartItemRepository.save(cartItemModel);

            CartItemDTO savedDTO = new CartItemDTO();
            cartItemPopulator.populate(savedItem, savedDTO);
            savedCartItemDTOs.add(savedDTO);
        }

        return savedCartItemDTOs;
    }



    private CartModel getCartModel(Long userId) {
      return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.CART_NOT_FOUND));

    }

    @Transactional
    @Override
    public CartItemDTO updateCartItem(Long userId, Long cartItemId, CartItemDTO updatedCartItemDTO) {
        CartModel cartModel = getCartModel(userId);

        CartItemModel cartItemModel = getCartItemModel(cartItemId);

        if (updatedCartItemDTO.getProductId() != null &&
                !updatedCartItemDTO.getProductId().equals(cartItemModel.getProductId())) {

            ProductDTO product = validateAndGetProduct(updatedCartItemDTO);
            cartItemModel.setProductId(product.getId());

            if (cartModel.getBusinessId() == null || cartModel.getCartItems().size() == 1) {
                cartModel.setBusinessId(product.getBusinessId());
                cartModel.setBusinessName(product.getBusinessName());
                cartRepository.save(cartModel);
            }
        }

        cartItemModel.setQuantity(updatedCartItemDTO.getQuantity());
        cartItemModel.setPrice(updatedCartItemDTO.getPrice());

        CartItemModel savedCartItem = cartItemRepository.save(cartItemModel);

        CartItemDTO savedCartItemDTO = new CartItemDTO();
        cartItemPopulator.populate(savedCartItem, savedCartItemDTO);
        return savedCartItemDTO;
    }

    @Transactional
    @Override
    public void removeCartItem(Long userId, Long cartItemId) {
        CartModel cartModel = getCartModel(userId);

        CartItemModel cartItemModel = getCartItemModel(cartItemId);

        cartItemRepository.delete(cartItemModel);

        if (cartModel.getCartItems().size() <= 1) {
            cartModel.setBusinessId(null);
            cartModel.setBusinessName(null);
        } else {
            try {
                cartModel.getCartItems().removeIf(item -> item.getId().equals(cartItemId));
                CartItemModel firstItem = cartModel.getCartItems().stream().findFirst().orElse(null);
                if (firstItem != null) {
                    ProductDTO product = productClient.getProductById(firstItem.getProductId());
                    updateCartBusinessInfoIfNeeded(cartModel, product);
                }
            } catch (Exception e) {
                log.error("Error updating business info after item removal: {}", e.getMessage(), e);
            }
        }

        cartRepository.save(cartModel);
    }

    private CartItemModel getCartItemModel(Long cartItemId) {
        return  cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.CART_ITEM_NOT_FOUND));

    }

    private ProductDTO validateAndGetProduct(CartItemDTO cartItemDTO) {
        try {
            ProductDTO product = productClient.getProductById(cartItemDTO.getProductId());
            if (product == null || product.getName() == null) {
                throw new HltCustomerException(ErrorCode.INVALID_PRODUCT_DATA);
            }
            return product;
        } catch (Exception ex) {
            throw new HltCustomerException(ErrorCode.INVALID_PRODUCT_ID);
        }
    }

    private BigDecimal calculatePrice(ProductDTO product) {
        String onlinePriceStr = product.getAttributes() != null ? product.getAttributes().stream()
                .filter(attr -> "OnlinePrice".equalsIgnoreCase(attr.getAttributeName()))
                .map(ProductAttributeDTO::getAttributeValue)
                .findFirst()
                .orElse(null) : null;

        if (onlinePriceStr != null) {
            try {
                return new BigDecimal(onlinePriceStr);
            } catch (NumberFormatException e) {
                throw new HltCustomerException(ErrorCode.INVALID_PRICE,
                        "Invalid OnlinePrice format for productId: " + product.getId());
            }
        } else if (product.getPrice() != null) {
            return product.getPrice();
        } else {
            throw new HltCustomerException(ErrorCode.INVALID_PRICE,
                    "No valid price found for productId: " + product.getId());
        }
    }

    private void updateCartBusinessInfoIfNeeded(CartModel cartModel, ProductDTO product) {
        if (cartModel.getBusinessId() == null && product.getBusinessId() != null) {
            cartModel.setBusinessId(product.getBusinessId());
            cartModel.setBusinessName(product.getBusinessName());
            cartRepository.save(cartModel);
        } else if (cartModel.getBusinessId() != null &&
                !cartModel.getBusinessId().equals(product.getBusinessId())) {
            throw new HltCustomerException(ErrorCode.INVALID_BUSINESS,
                    "Cannot add items from multiple businesses to the same cart.");
        }
    }

    @Transactional
    @Override
    public CartDTO addShippingAddress(Long userId, Long shippingAddressId) {
        if (shippingAddressId == null) {
            throw new HltCustomerException(ErrorCode.INVALID_ADDRESS);
        }

        try {
            AddressDTO existingAddress = userMgmtClient.getAddressById(shippingAddressId);
            if (existingAddress == null || !existingAddress.getUserId().equals(userId)) {
                throw new HltCustomerException(ErrorCode.INVALID_ADDRESS);
            }

            CartModel cart = getCartModel(userId);
            cart.setShippingAddressId(shippingAddressId);
            CartModel savedCart = cartRepository.save(cart);

            return getCartDTO(savedCart);
        } catch (FeignException.NotFound e) {
            throw new HltCustomerException(ErrorCode.ADDRESS_NOT_FOUND);
        }
    }

    @Transactional
    @Override
    public void clearCart(Long userId) {
        CartModel cart = getCartModel(userId);

        cart.getCartItems().clear();
        cart.setBusinessId(null);
        cart.setBusinessName(null);

        cartRepository.save(cart);

    }
    @Override
    public CartPriceSummaryDTO calculateCartPriceWithOffer(Long userId, Long offerId) {
        CartDTO cartDTO = getCartByUserId(userId);
        OfferDTO offer = fetchOfferById(offerId);

        List<CartItemDTO> eligibleItems = getEligibleItemsByTargetType(cartDTO, offer);
        BigDecimal eligibleSubtotal = eligibleItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (!isOfferApplicable(offer, cartDTO.getBusinessId(), eligibleSubtotal)) {
            log.warn("Offer {} is not applicable for userId={} or cart", offerId, userId);
            throw new HltCustomerException(ErrorCode.OFFER_NOT_APPLICABLE);
        }

        BigDecimal discount = calculateDiscount(offer, eligibleSubtotal);
        BigDecimal total = calculateCartTotal(cartDTO);
        BigDecimal finalAmount = total.subtract(discount);

        return new CartPriceSummaryDTO(total, discount, finalAmount, offer);
    }

    private BigDecimal calculateCartTotal(CartDTO cartDTO) {
        return cartDTO.getCartItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private OfferDTO fetchOfferById(Long offerId) {
        try {
            ResponseEntity<StandardResponse<OfferDTO>> response = productClient.getOfferById(offerId);
            OfferDTO offer = response.getBody().getData();
            if (offer == null) {
                throw new HltCustomerException(ErrorCode.INVALID_OFFER_ID);
            }
            return offer;
        } catch (Exception ex) {
            log.error("Failed to fetch offer with ID: {}", offerId, ex);
            throw new HltCustomerException(ErrorCode.INVALID_OFFER_ID);
        }
    }

    private boolean isOfferApplicable(OfferDTO offer, Long businessId, BigDecimal eligibleSubtotal) {
        if (offer == null) {
            log.warn("Offer is null");
            return false;
        }

        if (!Boolean.TRUE.equals(offer.getActive())) {
            log.warn("Offer ID {} is not active", offer.getId());
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        if (offer.getStartDate().isAfter(now)) {
            log.warn("Offer ID {} is not yet started. StartDate={}, Now={}", offer.getId(), offer.getStartDate(), now);
            return false;
        }

        if (offer.getEndDate().isBefore(now)) {
            log.warn("Offer ID {} has expired. EndDate={}, Now={}", offer.getEndDate(), now);
            return false;
        }

        if (offer.getTargetType() != OfferTargetType.GLOBAL && !businessId.equals(offer.getBusinessId())) {
            log.warn("Offer businessId mismatch. Offer businessId={}, Cart businessId={}", offer.getBusinessId(), businessId);
            return false;
        }

        if (eligibleSubtotal.compareTo(offer.getMinOrderValue()) < 0) {
            log.warn("Subtotal too low for offer. Subtotal={}, MinRequired={}", eligibleSubtotal, offer.getMinOrderValue());
            return false;
        }

        log.info("Offer ID {} is applicable", offer.getId());
        return true;
    }


    private BigDecimal calculateDiscount(OfferDTO offer, BigDecimal eligibleSubtotal) {
        if (offer.getOfferType() == OfferType.FLAT) {
            return offer.getValue().min(eligibleSubtotal);
        } else if (offer.getOfferType() == OfferType.PERCENTAGE) {
            return eligibleSubtotal.multiply(offer.getValue().divide(BigDecimal.valueOf(100)))
                    .min(eligibleSubtotal);
        }
        return BigDecimal.ZERO;
    }

    private List<CartItemDTO> getEligibleItemsByTargetType(CartDTO cart, OfferDTO offer) {
        List<CartItemDTO> cartItems = cart.getCartItems();

        switch (offer.getTargetType()) {
            case PRODUCT:
                List<Long> offerProductIds = offer.getProductIds();
                return cartItems.stream()
                        .filter(item -> offerProductIds != null && offerProductIds.contains(item.getProductId()))
                        .toList();

            case CATEGORY:
                List<Long> offerCategoryIds = offer.getCategoryIds();
                return cartItems.stream()
                        .filter(item -> offerCategoryIds != null && offerCategoryIds.contains(item.getCategoryId()))
                        .toList();

            case GLOBAL:
            default:
                return cartItems;
        }
    }




}
