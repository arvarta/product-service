package org.ezon.msa.repository;

import java.util.List;
import java.util.Optional;

import org.ezon.msa.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface ReviewRepository extends JpaRepository<Review, Long>{
    //상품별 리뷰 조회(상품상세페이지 > 리뷰)
	Optional<List<Review>> findByProductId(Long productId);
	//사용자별 리뷰조회 (마이페이지 > 내가 쓴 리뷰)
    Optional<List<Review>> findByUserId(Long userId);
    
    //OrderItemId로 리뷰 있는지 검사
    boolean existsByOrderItemId(Long orderItemId);
    
    @Query("SELECT r.orderItemId FROM Review r WHERE r.userId = :userId")
    List<Long> findReviewedOrderItemIdsByUserId(@Param("userId") Long userId);
}
