package com.hlt.productmanagement.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name = "ORDERMGMT")
public interface OrderClient {

    @GetMapping("/api/order/api/orders/productOrder")
    Boolean hasUserOrderedProductFromBusiness(@RequestParam("userId") Long userId,
                                              @RequestParam("productId") Long productId,
                                              @RequestParam("businessId") Long businessId);

    @GetMapping("/api/order/api/orders/businessOrder")
    Boolean hasUserOrderedFromBusiness(@RequestParam("userId") Long userId,
                                       @RequestParam("businessId") Long businessId);
}
