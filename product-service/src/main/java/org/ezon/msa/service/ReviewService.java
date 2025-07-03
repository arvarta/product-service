package org.ezon.msa.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.ezon.msa.entity.Product;
import org.ezon.msa.entity.Review;
import org.ezon.msa.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class ReviewService {
	
    private final ReviewRepository reviewRepository;
    private final ProductService productService;
    
    public ReviewService(ReviewRepository reviewRepository, ProductService productService) {
    	this.reviewRepository = reviewRepository;
    	this.productService = productService;
    }

	// 상품별 리뷰 목록 조회
    public List<Map<String, Object>> findReviewsByProductIdAll(Long productId) {
    	List<Review> reviews = reviewRepository.findByProductId(productId)
                .orElse(Collections.emptyList());
    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");

        return reviews.stream().map(review -> {
            Map<String, Object> map = new HashMap<>();
            map.put("reviewId", review.getReviewId());
            map.put("productId", review.getProductId());
            map.put("orderItemId", review.getOrderItemId());
            map.put("rating", review.getRating());
            map.put("content", review.getContent());
            map.put("image", review.getImage());
            map.put("userId", review.getUserId());

            // 날짜 포맷 적용
            String formattedDate = review.getCreatedAt() != null
                    ? review.getCreatedAt().toLocalDate().format(formatter)
                    : "";
            map.put("createdAt", formattedDate);

            return map;
        }).collect(Collectors.toList());
    }
    
	// 사용자별 리뷰 목록 조회
    public List<Map<String, Object>> getUserReviewListWithProductName(Long userId) {
        List<Review> reviews = reviewRepository.findByUserId(userId)
                .orElse(Collections.emptyList());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");

        return reviews.stream().map(review -> {
            Map<String, Object> map = new HashMap<>();
            map.put("reviewId", review.getReviewId());
            map.put("productId", review.getProductId());
            map.put("orderItemId", review.getOrderItemId());
            map.put("rating", review.getRating());
            map.put("content", review.getContent());
            map.put("image", review.getImage());

            // 날짜 포맷 적용
            String formattedDate = review.getCreatedAt() != null
                    ? review.getCreatedAt().toLocalDate().format(formatter)
                    : "";
            map.put("createdAt", formattedDate);

            // 상품명 추가
            Map<String, Object> product = productService.getProductById(review.getProductId());
            map.put("productName", product.get("productName"));
            map.put("productImage", product.get("image"));
            map.put("productPrice", product.get("price"));

            return map;
        }).collect(Collectors.toList());
    }

    
    
    
    // 리뷰 작성
    public void createReview(Map<String, Object> map) {
        System.err.println("-----------------");
        System.err.println(map.toString());

        Object imageObj = map.get("image");
        if (imageObj == null || imageObj.toString().trim().isEmpty()) {
            map.put("image", "/reviewImg/noImg.jpg");
        }

        validateRequiredKeys(map, "user_id", "product_id", "order_item_id", "content", "image", "rating");

        Long orderItemId = toLong(map.get("order_item_id"));

        // 중복 리뷰 방지
        if (reviewRepository.existsByOrderItemId(orderItemId)) {
            throw new IllegalStateException("이미 해당 주문에 대한 리뷰가 존재합니다.");
        }

        Review review = Review.builder()
            .userId(toLong(map.get("user_id")))
            .productId(toLong(map.get("product_id")))
            .orderItemId(orderItemId)
            .content(map.get("content").toString())
            .image(map.get("image").toString())
            .createdAt(LocalDateTime.now())
            .rating(toInt(map.get("rating")))
            .build();

        reviewRepository.save(review);
        System.err.println(review);
    }

    // 리뷰 수정
    @Transactional
    public Optional<Review> updateReview(Map<String, Object> map) {
    	System.err.println("-----------------");
    	System.err.println(map.toString());
    	Object imageObj = map.get("image");
    	if (imageObj == null || imageObj.toString().trim().isEmpty()) {
    		map.put("image", "/reviewImg/noImg.jpg");
    	}
        validateRequiredKeys(map, "review_id", "content", "image", "rating");

        Long reviewId = toLong(map.get("review_id"));
        Optional<Review> result = reviewRepository.findById(reviewId);

        result.ifPresent(saved -> {
            saved.setContent(map.get("content").toString());
            saved.setImage(map.get("image").toString());
            saved.setRating(toInt(map.get("rating")));
        });

        return result;
    }

    // 리뷰 삭제
    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
        		.orElseThrow(() -> new NoSuchElementException("리뷰가 존재하지 않습니다."));
        reviewRepository.delete(review);
    }
    
    private void validateRequiredKeys(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            if (!map.containsKey(key) || map.get(key) == null) {
                throw new IllegalArgumentException("필수값 누락: " + key);
            }
        }
    }

    private void validateSellerPermission(Long requesterId, Product product) {
        if (!product.getUserId().equals(requesterId)) {
            throw new SecurityException("본인의 상품만 접근할 수 있습니다.");
        }
    }

    private int toInt(Object value) { 
    	return Integer.parseInt(value.toString()); 
    }
    
    private int toIntOrDefault(Object value, int def) {
    	return value == null ? def : toInt(value); 
    }
    
    private Long toLong(Object value) {
    	return Long.parseLong(value.toString()); 
    }
    
    private void validateKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("검색어를 입력해주세요.");
        }
    }
    
    private Map<String, Object> toMap(Review r) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("reviewId", r.getReviewId());
        map.put("userId", r.getUserId());
        map.put("productId", r.getProductId());
        map.put("content", r.getContent());
        map.put("image", r.getImage());
        map.put("createdAt", r.getCreatedAt());
        map.put("rating", r.getRating());

        return map;
    }

    public List<Long> getReviewedOrderItemIds(Long userId) {
        return reviewRepository.findReviewedOrderItemIdsByUserId(userId);
    }
//============================= 이정우    
    public Map<String, Object> getReviewSummaryByProductId(Long productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId)
                .orElse(Collections.emptyList());
        int reviewCount = reviews.size();
        double averageRating = 0.0;
        if (reviewCount > 0) {
            averageRating = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("averageRating", averageRating);
        result.put("reviewCount", reviewCount);
        return result;
    }
    
    public Map<Long, Map<String, Object>> getReviewSummaryForProductIdList(List<Long> productIds) {
        Map<Long, Map<String, Object>> result = new HashMap<>();
        for (Long productId : productIds) {
            List<Review> reviews = reviewRepository.findByProductId(productId)
                    .orElse(Collections.emptyList());
            int reviewCount = reviews.size();
            double averageRating = 0.0;
            if (reviewCount > 0) {
                averageRating = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
            }
            Map<String, Object> summary = new HashMap<>();
            summary.put("averageRating", averageRating);
            summary.put("reviewCount", reviewCount);
            result.put(productId, summary);
        }
        return result;
    }
}
