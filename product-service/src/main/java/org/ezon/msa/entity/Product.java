package org.ezon.msa.entity;

import java.time.LocalDateTime;

import org.ezon.msa.enums.ProductStatus;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @NotNull
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotNull
    @Column(name = "seller_address_id", nullable = false)
    private Long sellerAddressId;

    @NotNull
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @NotNull
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description; // 상세 이미지

    @NotNull
    @Column(name = "price", nullable = false)
    private int price;

    @NotNull
    @Column(name = "discount_price", nullable = false)
    private int discountPrice;

    @NotNull
    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @Column(name = "image", columnDefinition = "TEXT", nullable = false)
    private String image; //썸네일

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private ProductStatus status;

    @NotNull
    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    @NotNull
    @Column(name = "review_count", nullable = false)
    private int reviewCount;

    @NotNull
    @Column(name = "sales_count", nullable = false)
    private int salesCount;

    @NotNull
    @Column(name = "courier_name", nullable = false, length = 100)
    private String courierName;

    @NotNull
    @Column(name = "shipping_fee", nullable = false)
    private int shippingFee;

    @NotNull
    @Column(name = "is_approved", nullable = false)
    private Boolean isApproved;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @DecimalMax(value = "5.0", inclusive = true)
    @Column(name = "average_rating", nullable = false)
    private Double averageRating;
}
