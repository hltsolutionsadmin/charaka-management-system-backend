package com.juvarya.order.service;

import com.juvarya.order.dto.*;
import com.juvarya.order.dto.request.CartRequest;

import java.math.BigDecimal;

import java.util.List;



public interface CartService {

    CartDTO createCart(Long userId);

    CartDTO getCartByUserId(Long userId);

    List<CartItemDTO> addItemsToCart(Long userId, CartRequest request);

    CartItemDTO updateCartItem(Long userId, Long cartItemId, CartItemDTO updatedCartItemDTO);

    void removeCartItem(Long userId, Long cartItemId);

    CartDTO addShippingAddress(Long userId, Long shippingAddressId);

    void clearCart(Long userId);

    CartPriceSummaryDTO calculateCartPriceWithOffer(Long userId, Long offerId);

}
