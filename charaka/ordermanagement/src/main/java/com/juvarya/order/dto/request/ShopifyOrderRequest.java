package com.juvarya.order.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopifyOrderRequest {
    private Order order;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Order {
        private List<LineItem> line_items;
        private Customer customer;
        private ShippingAddress shipping_address;
        private Boolean send_receipt = true;
        private Boolean send_fulfillment_receipt = false;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class LineItem {
            private Long variant_id;
            private int quantity;
            private BigDecimal price;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Customer {
            private String first_name;
            private String last_name;
            private String email;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ShippingAddress {
            private String first_name;
            private String last_name;
            private String address1;
            private String phone;
            private String city;
            private String province;
            private String country;
            private String zip;
        }
    }
}
