package com.juvarya.order.populator;

import com.juvarya.order.client.ProductClient;
import com.juvarya.order.dto.CartItemDTO;
import com.juvarya.order.dto.ProductDTO;
import com.juvarya.order.entity.CartItemModel;
import com.hlt.utils.Populator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class CartItemPopulator implements Populator<CartItemModel, CartItemDTO> {

    private final ProductClient productClient;

    public CartItemPopulator(ProductClient productClient) {
        this.productClient = productClient;
    }

    @Override
    public void populate(CartItemModel source, CartItemDTO target) {
        if (Objects.isNull(source) || Objects.isNull(target)) {
            log.warn("CartItemPopulator: Source or Target is null. Skipping population.");
            return;
        }

        populateBasicDetails(source, target);
        populateProductDetails(source.getProductId(), target);
    }

    private void populateBasicDetails(CartItemModel source, CartItemDTO target) {
        target.setId(source.getId());
        target.setNotes(source.getNotes());
        target.setProductId(source.getProductId());
        target.setQuantity(source.getQuantity());
        target.setPrice(source.getPrice());
        target.setCreatedAt(source.getCreatedDate());
        target.setUpdatedAt(source.getUpdatedDate());
        target.setCategoryId(source.getCategoryId());

        if (source.getCart() != null) {
            target.setCartId(source.getCart().getId());
        }
    }

    private void populateProductDetails(Long productId, CartItemDTO target) {
        if (productId == null) {
            log.warn("CartItemPopulator: productId is null. Skipping product details population.");
            setDefaultProductInfo(target);
            return;
        }

        try {
            ProductDTO product = productClient.getProductById(productId);

            if (product == null) {
                log.warn("CartItemPopulator: Product not found for productId={}", productId);
                setDefaultProductInfo(target);
                return;
            }

            target.setProductName(StringUtils.defaultIfBlank(product.getName(), "No Name"));

            if (product.getMedia() != null && !product.getMedia().isEmpty()) {
                target.setMedia(product.getMedia());
            } else {
                log.info("CartItemPopulator: No media found for productId={}", productId);
                target.setMedia(null);
            }

        } catch (Exception ex) {
            log.error("CartItemPopulator: Error while fetching product for productId={}: {}", productId, ex.getMessage(), ex);
            setDefaultProductInfo(target);
        }
    }

    private void setDefaultProductInfo(CartItemDTO target) {
        target.setProductName("Unavailable");
        target.setMedia(null);
    }
}
