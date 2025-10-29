package com.juvarya.order.controllers;

import com.hlt.commonservice.dto.StandardResponse;
import com.hlt.commonservice.user.UserDetailsImpl;
import com.juvarya.order.aspect.UserLock;
import com.juvarya.order.client.ProductClient;
import com.juvarya.order.dto.*;
import com.juvarya.order.dto.enums.LockType;
import com.juvarya.order.dto.request.CartRequest;
import com.juvarya.order.dto.response.ApiResponse;
import com.juvarya.order.service.CartService;
import com.hlt.utils.JTBaseEndpoint;
import com.hlt.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/carts")
public class CartController extends JTBaseEndpoint {

    @Autowired
    private ProductClient productClient;

    @Autowired
    private CartService cartService;

    private Long getLoggedInUserId() {
        UserDetailsImpl user = SecurityUtils.getCurrentUserDetails();
        if (user == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return user.getId();
    }

    @UserLock(LockType.CART)
    @PostMapping("/create")
    public ResponseEntity<CartDTO> createCart() {
        Long userId = getLoggedInUserId();
        log.info("Creating cart for userId={}", userId);

        CartDTO cart = cartService.createCart(userId);
        log.info("Cart created for userId={}, cartId={}", userId, cart.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(cart);
    }

    @GetMapping("/get")
    public ResponseEntity<CartDTO> getCart() {
        Long userId = getLoggedInUserId();
        log.info("Fetching cart for userId={}", userId);

        CartDTO cart = cartService.getCartByUserId(userId);
        log.info("Fetched cart for userId={}, cartId={}", userId, cart.getId());
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/items")
    public ResponseEntity<List<CartItemDTO>> addItemsToCart(@Valid @RequestBody CartRequest cartRequest) {
        Long userId = getLoggedInUserId();
        log.info("Adding items to cart for userId={}, items={}", userId, cartRequest.getItems());

        List<CartItemDTO> savedItems = cartService.addItemsToCart(userId, cartRequest);
        log.info("Added {} item(s) to cart for userId={}", savedItems.size(), userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedItems);
    }


    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartItemDTO> updateCartItem(@PathVariable Long cartItemId,
                                                      @Valid @RequestBody CartItemDTO updatedCartItemDTO) {
        Long userId = getLoggedInUserId();
        log.info("Updating cart item (id={}) for userId={} with data={}", cartItemId, userId, updatedCartItemDTO);

        CartItemDTO updatedItem = cartService.updateCartItem(userId, cartItemId, updatedCartItemDTO);
        log.info("Updated cart item id={} for userId={}", cartItemId, userId);
        return ResponseEntity.ok(updatedItem);
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<String>> removeCartItem(@PathVariable Long cartItemId) {
        Long userId = getLoggedInUserId();
        log.info("Removing cart item (id={}) for userId={}", cartItemId, userId);

        cartService.removeCartItem(userId, cartItemId);
        log.info("Removed cart item id={} for userId={}", cartItemId, userId);
        return ResponseEntity.ok(ApiResponse.message("Cart item removed successfully"));
    }

    @PostMapping("/address")
    public ResponseEntity<CartDTO> addShippingAddress(@RequestParam("addressId") Long addressId) {
        Long userId = getLoggedInUserId();
        log.info("Adding shipping address id={} to cart for userId={}", addressId, userId);

        CartDTO cartDTO = cartService.addShippingAddress(userId, addressId);
        log.info("Added shipping address to cartId={} for userId={}", cartDTO.getId(), userId);
        return ResponseEntity.ok(cartDTO);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse> clearCart() {
        Long userId = getLoggedInUserId();
        log.info("Clearing cart for userId={}", userId);

        cartService.clearCart(userId);
        log.info("Cleared cart for userId={}", userId);

        return ResponseEntity.ok(new ApiResponse("Cart cleared successfully", "SUCCESS", 1));
    }

    @GetMapping("/priceSummary")
    public ResponseEntity<StandardResponse<List<CartPriceSummaryDTO>>> getCartPriceWithOffer(@RequestParam Long offerId) {
        Long userId = getLoggedInUserId();
        log.info("Calculating cart price summary for userId={}, offerId={}", userId, offerId);

        CartPriceSummaryDTO summary = cartService.calculateCartPriceWithOffer(userId, offerId);

        log.info("Cart price summary calculated for userId={}, offerId={}", userId, offerId);
        return ResponseEntity.ok(StandardResponse.list("Cart price summary with offer applied", List.of(summary)));
    }

}
