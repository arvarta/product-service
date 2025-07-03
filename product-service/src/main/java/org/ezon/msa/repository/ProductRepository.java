package org.ezon.msa.repository;

import java.util.List;

import org.ezon.msa.entity.Product;
import org.ezon.msa.enums.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

	List<Product> findByUserId(Long userId);

	List<Product> findTop10ByNameStartingWithIgnoreCase(String prefix);

	List<Product> findByUserIdAndNameContainingIgnoreCase(Long userId, String keyword);

	List<Product> findByCategoryIdAndPriceBetweenAndStatus(Long categoryId, int minPrice, int maxPrice, String status);

	List<Product> findByNameContainingIgnoreCaseAndStatus(String keyword, ProductStatus status);

	List<Product> findByCategoryIdAndStatus(Long categoryId, ProductStatus status);

	List<Product> findByStatus(ProductStatus status);

	List<Product> findByProductIdIn(List<Long> productIds);
	
	@Query("""
		    SELECT p FROM Product p JOIN Category c ON p.categoryId = c.categoryId
		    WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
		       OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
		    AND p.status = :status
		""")
		List<Product> searchByKeywordProductOrCategory(String keyword, ProductStatus status);
	
	@Query("""
		    SELECT p FROM Product p
		    WHERE (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
		      AND (:categoryId IS NULL OR p.categoryId = :categoryId)
		      AND (:minPrice IS NULL OR p.price >= :minPrice)
		      AND (:maxPrice IS NULL OR p.price <= :maxPrice)
		      AND (:minRating IS NULL OR p.averageRating >= :minRating)
		      AND p.status = :status
		""")
		List<Product> searchProductsAll(
		    @Param("keyword") String keyword,
		    @Param("categoryId") Long categoryId,
		    @Param("minPrice") Integer minPrice,
		    @Param("maxPrice") Integer maxPrice,
		    @Param("minRating") Double minRating,
		    @Param("status") ProductStatus status
		);

	List<Product> findByCategoryIdAndNameContainingIgnoreCaseAndStatus(Long categoryId, String keyword,
			ProductStatus approved);

	List<Product> findByUserIdAndCategoryIdAndNameContainingIgnoreCase(Long userId, Long categoryId, String keyword);

	List<Product> findByUserIdAndCategoryId(Long userId, Long categoryId);
}
