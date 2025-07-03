package org.ezon.msa.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ezon.msa.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ReviewController {

	@Autowired
	private ReviewService reviewService;

	public ReviewController(ReviewService reviewService) {
		super();
		this.reviewService = reviewService;
	}

	// 제품별 리뷰 목록 조회
		@GetMapping("/product/{productId}/reviews")
		public ResponseEntity<Map<String, Object>> getReviewsByProductId(@PathVariable Long productId) {
			List<Map<String, Object>> reviews = reviewService.findReviewsByProductIdAll(productId);

			Map<String, Object> response = new HashMap<>();
			response.put("reviews", reviews);

			return ResponseEntity.ok(response);
		}

	// 사용자별 리뷰 목록 조회
	@GetMapping("/{userId}/reviews")
	public ResponseEntity<Map<String, Object>> getReviewsByUserId(@PathVariable Long userId) {
		List<Map<String, Object>> reviews = reviewService.getUserReviewListWithProductName(userId);

		Map<String, Object> response = new HashMap<>();
		response.put("reviews", reviews);

		return ResponseEntity.ok(response);
	}

	// [user] 리뷰 작성, 리뷰등록
	@PostMapping("/reviews/save")
	public void addReview(@RequestBody Map<String, Object> map) {
		reviewService.createReview(map);
	}

	// 리뷰 수정
	@PostMapping("/reviews/update/save")
	public void updateReview(@RequestBody Map<String, Object> map) {
		reviewService.updateReview(map);
	}

	// 리뷰 삭제
	@DeleteMapping("/{reviewId}/delete")
	public void deleteReview(@PathVariable Long reviewId) {
		reviewService.deleteReview(reviewId);
	}
	
	// 리뷰 등록된 상세주문id 
	@GetMapping("/reviews/orderItemIds/{userId}")
	public ResponseEntity<List<Long>> getReviewedOrderItemIds(@PathVariable Long userId) {
		return ResponseEntity.ok(reviewService.getReviewedOrderItemIds(userId));
	}
}
