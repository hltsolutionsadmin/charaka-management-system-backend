package com.juvarya.order.populator;

import com.juvarya.order.client.ProductClient;
import com.juvarya.order.dto.ProductDTO;
import com.juvarya.order.dto.OrderItemDTO;
import com.juvarya.order.entity.OrderItemModel;
import com.hlt.utils.Populator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class OrderItemPopulator implements Populator<OrderItemModel, OrderItemDTO> {

    private final ProductClient productClient;

    public OrderItemPopulator(ProductClient productClient) {
        this.productClient = productClient;
    }

    @Override
    public void populate(OrderItemModel source, OrderItemDTO target) {
        if (Objects.isNull(source) || Objects.isNull(target)) {
            log.warn("OrderItemPopulator: Source or Target is null. Skipping population.");
            return;
        }

        populateBasicDetails(source, target);
        populateProductDetails(source.getProductId(), target);
    }

    private void populateBasicDetails(OrderItemModel source, OrderItemDTO target) {
        target.setId(source.getId());
        target.setProductId(source.getProductId());
        target.setQuantity(source.getQuantity());
        target.setPrice(source.getPrice());
        target.setEntryNumber(source.getEntryNumber());
        target.setTaxAmount(source.getTaxAmount());
        target.setTaxPercentage(source.getTaxPercentage());
        target.setTotalAmount(source.getTotalAmount());
        target.setTaxIgnored(source.getTaxIgnored());
    }

    private void populateProductDetails(Long productId, OrderItemDTO target) {
        if (productId == null) {
            log.warn("OrderItemPopulator: productId is null. Skipping product details population.");
            setDefaultProductInfo(target);
            return;
        }

        try {
            ProductDTO product = productClient.getProductById(productId);

            if (product == null) {
                log.warn("OrderItemPopulator: Product not found for productId={}", productId);
                setDefaultProductInfo(target);
                return;
            }

            target.setProductName(StringUtils.defaultIfBlank(product.getName(), "No Name"));

            if (product.getMedia() != null && !product.getMedia().isEmpty()) {
                target.setMedia(product.getMedia());
            } else {
                log.info("OrderItemPopulator: No media found for productId={}", productId);
                target.setMedia(null);
            }

        } catch (Exception ex) {
            log.error("OrderItemPopulator: Error while fetching product for productId={}: {}", productId, ex.getMessage(), ex);
            setDefaultProductInfo(target);
        }
    }

    private void setDefaultProductInfo(OrderItemDTO target) {
        target.setProductName("Unknown");
        target.setMedia(null);
    }
}
