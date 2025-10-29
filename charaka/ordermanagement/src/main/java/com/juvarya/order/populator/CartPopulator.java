package com.juvarya.order.populator;

import com.hlt.utils.Populator;
import com.juvarya.order.client.ProductClient;
import com.juvarya.order.client.UserMgmtClient;
import com.juvarya.order.dto.CartDTO;
import com.juvarya.order.dto.CartItemDTO;
import com.juvarya.order.dto.ProductDTO;
import com.juvarya.order.entity.CartModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class CartPopulator implements Populator<CartModel, CartDTO> {

    @Autowired
    private UserMgmtClient userMgmtClient;

    @Autowired
    private ProductClient productClient;

    @Autowired
    private CartItemPopulator cartItemPopulator;

    @Override
    public void populate(CartModel source, CartDTO target) {
        if (source == null || target == null) return;

        target.setId(source.getId());
        target.setUserId(source.getUserId());
        target.setStatus(source.getStatus());
        target.setNotes(source.getNotes());
        target.setShopifyCartId(source.getShopifyCartId());
        target.setCreatedAt(source.getCreatedDate());
        target.setShippingAddressId(source.getShippingAddressId());
        target.setUpdatedAt(source.getUpdatedDate());
        target.setBusinessId(source.getBusinessId());
        target.setBusinessName(source.getBusinessName());

        target.setTotalCount(0);

        if (source.getCartItems() != null && !source.getCartItems().isEmpty()) {
            int totalCount = 0;

            target.setCartItems(
                    source.getCartItems().stream().map(item -> {
                        CartItemDTO dto = new CartItemDTO();
                        cartItemPopulator.populate(item, dto);
                        return dto;
                    }).collect(Collectors.toList())
            );


            totalCount = source.getCartItems().stream()
                    .mapToInt(item -> item.getQuantity())
                    .sum();

            target.setTotalCount(totalCount);


            if (source.getBusinessId() == null) {
                try {
                    if (!source.getCartItems().isEmpty()) {
                        Long productId = source.getCartItems().get(0).getProductId();
                        ProductDTO product = productClient.getProductById(productId);
                        if (product != null) {
                            target.setBusinessId(product.getBusinessId());
                            target.setBusinessName(product.getBusinessName());


                            source.setBusinessId(product.getBusinessId());
                            source.setBusinessName(product.getBusinessName());
                        }
                    }
                } catch (Exception e) {
                    // Log the error but continue with the rest of the population
                    System.err.println("Error retrieving business information: " + e.getMessage());
                }
            }
        }
    }
}
