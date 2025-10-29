package com.juvarya.product.service;

import com.hlt.auth.exception.handling.ErrorCode;
import com.hlt.auth.exception.handling.HltCustomerException;
import com.juvarya.product.model.ProductModel;
import com.juvarya.product.repository.ProductRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ShopifySyncService {

    private final ShopifyService shopifyService;
    private final ProductRepository productRepository;

    public ShopifySyncService(ShopifyService shopifyService, ProductRepository productRepository) {
        this.shopifyService = shopifyService;
        this.productRepository = productRepository;
    }

    @Async
    public void syncToShopify(ProductModel product) {
        try {
            Map<String, String> shopifyIds = shopifyService.createProductInShopify(product);
            String shopifyProductId = shopifyIds.get("productId");
            String shopifyVariantId = shopifyIds.get("variantId");

            product.setShopifyProductId(shopifyProductId);
            product.setShopifyVariantId(shopifyVariantId);

            productRepository.save(product);
        } catch (Exception ex) {
            new HltCustomerException(ErrorCode.SHOPIFY_SYNC_FAILED);
        }
    }
}