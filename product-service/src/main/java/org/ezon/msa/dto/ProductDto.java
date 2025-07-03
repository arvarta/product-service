package org.ezon.msa.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDto {
    private Long productId;
    private String name;
    private String userName;
    private String status;
    private int price;
    private int totalPrice;
    private int stockQuantity;
    private Long categoryId;
    private int discountPrice;
    private String image;
    private int shippingFee;
    private String sellerAddressId;
    private String courier_name;
    private LocalDateTime addedAt;
}
