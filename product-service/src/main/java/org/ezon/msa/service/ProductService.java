package org.ezon.msa.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.ezon.msa.common.ApiEndpointConstants;
import org.ezon.msa.dto.ProductDto;
import org.ezon.msa.dto.UserDto;
import org.ezon.msa.entity.Category;
import org.ezon.msa.entity.Product;
import org.ezon.msa.entity.Review;
import org.ezon.msa.enums.ProductStatus;
import org.ezon.msa.repository.CategoryRepository;
import org.ezon.msa.repository.ProductRepository;
import org.ezon.msa.repository.ReviewRepository;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ProductService {

	private final RestTemplate restTemplate;

	private final ProductRepository productRepository;
	private final CategoryRepository categoryRepository;
	private final ReviewRepository reviewRepository;
	
	public ProductService(RestTemplate restTemplate, ProductRepository productRepository,
			CategoryRepository categoryRepository, ReviewRepository reviewRepository) {
		super();
		this.restTemplate = restTemplate;
		this.productRepository = productRepository;
		this.categoryRepository = categoryRepository;
		this.reviewRepository = reviewRepository;
	}

	// 통합 검색(브랜드/카테고리/상품명)
	public List<Map<String, Object>> searchProductsAll(String keyword, Long categoryId, Integer minPrice,
			Integer maxPrice, Double minRating, String sort) {
		List<Product> list = productRepository.searchProductsAll(keyword, categoryId, minPrice, maxPrice, minRating,
				ProductStatus.APPROVED);
		// 정렬 (Java Stream 예시)
		if ("price_asc".equals(sort)) {
			list = list.stream().sorted(Comparator.comparingInt(Product::getPrice)).collect(Collectors.toList());
		} else if ("price_desc".equals(sort)) {
			list = list.stream().sorted(Comparator.comparingInt(Product::getPrice).reversed())
					.collect(Collectors.toList());
		} else if ("rating_desc".equals(sort)) {
			list = list.stream().sorted(Comparator.comparingDouble(Product::getAverageRating).reversed())
					.collect(Collectors.toList());
		} else if ("rating_asc".equals(sort)) {
			list = list.stream().sorted(Comparator.comparingDouble(Product::getAverageRating))
					.collect(Collectors.toList());
		}
		return list.stream().map(this::toMap).toList();
	}

	// [user] 상품 목록 (검색, 필터링 포함)
	public List<Map<String, Object>> getProductList(Long userId, String role, Long categoryId, String keyword) {
		List<Product> products;
		if ("ADMIN".equals(role)) {
			products = productRepository.findAll();
		} else if ("SELLER".equals(role)) {
			if (categoryId != null && keyword != null && !keyword.isBlank()) {
				products = productRepository.findByUserIdAndCategoryIdAndNameContainingIgnoreCase(userId, categoryId,
						keyword);
			} else if (categoryId != null) {
				products = productRepository.findByUserIdAndCategoryId(userId, categoryId);
			} else if (keyword != null && !keyword.isBlank()) {
				products = productRepository.findByUserIdAndNameContainingIgnoreCase(userId, keyword);
			} else {
				products = productRepository.findByUserId(userId);
			}
		} else {
			// 일반 구매자: 승인 상품만
			if (categoryId != null && keyword != null && !keyword.isBlank()) {
				products = productRepository.findByCategoryIdAndNameContainingIgnoreCaseAndStatus(categoryId, keyword,
						ProductStatus.APPROVED);
			} else if (categoryId != null) {
				products = productRepository.findByCategoryIdAndStatus(categoryId, ProductStatus.APPROVED);
			} else if (keyword != null && !keyword.isBlank()) {
				products = productRepository.findByNameContainingIgnoreCaseAndStatus(keyword, ProductStatus.APPROVED);
			} else {
				products = productRepository.findByStatus(ProductStatus.APPROVED);
			}
		}
		
		return products.stream().map(this::toMap).toList();
	}
	private List<ProductDto> getDto(List<Product> products){
		List<ProductDto> result = new ArrayList<>(); 
		for(Product p : products) {
			ProductDto temp = new ProductDto();
			temp.setName(p.getName());
			temp.setProductId(p.getProductId());
			UserDto user = getUser(p.getUserId());
			if(user != null) {
				temp.setUserName(user.getName());
			}
			temp.setStatus(p.getStatus().name());
			temp.setAddedAt(p.getAddedAt());
			result.add(temp);
		}
		return result;
	}
	private UserDto getUser(Long userId){
		String url = "http://localhost:10000/api/users/"+userId;
		ResponseEntity<UserDto> temp = restTemplate.exchange(
			url, 
			HttpMethod.GET,
			null,
			new ParameterizedTypeReference<UserDto>() {}
		);
		UserDto result = temp.getBody();
		return result == null? null:result;
	}
	// PENDING 상태 상품만 (관리자)
	public List<Map<String, Object>> getPendingProductsForAdmin() {
		return productRepository.findByStatus(ProductStatus.PENDING).stream().map(this::toMap)
				.collect(Collectors.toList());
	}

	// [user] 상품 상세 조회
	public Map<String, Object> getProductByIdWithAuth(Long productId) {
		Product p = productRepository.findById(productId)
				.orElseThrow(() -> new NoSuchElementException("상품이 존재하지 않습니다."));
		if (p.getStatus() != ProductStatus.APPROVED)
			throw new SecurityException("승인된 상품만 조회 가능합니다.");
		return toMap(p);
	}

	// [seller] 카테고리별 상품 목록
	public List<Map<String, Object>> findByCategory(Long categoryId) {
		return productRepository.findByCategoryIdAndStatus(categoryId, ProductStatus.APPROVED).stream().map(this::toMap)
				.toList();
	}

	// [seller] 상품 등록
	public void createProduct(Map<String, Object> params) {
		validateRequiredKeys(params, "image", "userId", "sellerAddressId", "name", 
				"categoryId", "price", "discountPrice", "stockQuantity", "description",
				"courierName", "shippingFee", "status", "addedAt");
		System.out.println("[디버깅] params = " + params);
		Product product = Product.builder()
			    .image(params.get("image").toString())
			    .userId(Long.parseLong(params.get("userId").toString()))
		        .sellerAddressId(Long.parseLong(params.get("sellerAddressId").toString()))
		        .categoryId(Long.parseLong(params.get("categoryId").toString()))
			    .name(params.get("name").toString())
			    .price(Integer.parseInt(params.get("price").toString()))
			    .discountPrice(params.get("discountPrice") != null ? Integer.parseInt(params.get("discountPrice").toString()) : 0)
			    .stockQuantity(Integer.parseInt(params.get("stockQuantity").toString()))
			    .description(params.get("description").toString())
			    .courierName(params.get("courierName").toString())
			    .shippingFee(Integer.parseInt(params.get("shippingFee").toString()))
			    .status(ProductStatus.PENDING)
			    .addedAt(LocalDateTime.now())
			    .reviewCount(0)
			    .salesCount(0)
			    .isApproved(false)
			    .averageRating(0.0)
			    .addedAt(LocalDateTime.now())
			    .build();

	        productRepository.save(product);
	}

	// 판매자 본인만 상세조회 허용
	public Map<String, Object> getProductByIdForSeller(Long productId, Long userId, String role) {
		Product p = productRepository.findById(productId)
				.orElseThrow(() -> new NoSuchElementException("상품이 존재하지 않습니다."));
		// 판매자 검증
		if (!"SELLER".equals(role) || !p.getUserId().equals(userId)) {
			throw new SecurityException("본인 상품만 조회 가능합니다.");
		}
		return toMap(p);
	}

	// [seller] 상품 수정
	public void updateProduct(Long productId, Map<String, Object> map) {
	    Product product = productRepository.findById(productId)
	        .orElseThrow(() -> new NoSuchElementException("상품이 존재하지 않습니다."));
	    Long requesterId = toLong(map.get("userId"));
	    validateSellerPermission(requesterId, product);

	    // name
	    if (map.containsKey("name") && map.get("name") != null && !map.get("name").toString().isBlank())
	        product.setName(map.get("name").toString());

	    // categoryId
	    if (map.containsKey("categoryId") && map.get("categoryId") != null && !map.get("categoryId").toString().isBlank())
	        product.setCategoryId(Long.parseLong(map.get("categoryId").toString()));

	    // description
	    if (map.containsKey("description") && map.get("description") != null)
	        product.setDescription(map.get("description").toString());

	    // price
	    if (map.containsKey("price") && map.get("price") != null && !map.get("price").toString().isBlank())
	        product.setPrice(Integer.parseInt(map.get("price").toString()));

	    // discountPrice
	    if (map.containsKey("discountPrice") && map.get("discountPrice") != null && !map.get("discountPrice").toString().isBlank())
	        product.setDiscountPrice(Integer.parseInt(map.get("discountPrice").toString()));

	    // stockQuantity
	    if (map.containsKey("stockQuantity") && map.get("stockQuantity") != null && !map.get("stockQuantity").toString().isBlank())
	        product.setStockQuantity(Integer.parseInt(map.get("stockQuantity").toString()));

	    // image
	    if (map.containsKey("image") && map.get("image") != null && !map.get("image").toString().isBlank())
	        product.setImage(map.get("image").toString());

	    // courierName
	    if (map.containsKey("courierName") && map.get("courierName") != null && !map.get("courierName").toString().isBlank())
	        product.setCourierName(map.get("courierName").toString());

	    // shippingFee
	    if (map.containsKey("shippingFee") && map.get("shippingFee") != null && !map.get("shippingFee").toString().isBlank())
	        product.setShippingFee(Integer.parseInt(map.get("shippingFee").toString()));

	    // sellerAddressId
	    if (map.containsKey("sellerAddressId") && map.get("sellerAddressId") != null && !map.get("sellerAddressId").toString().isBlank())
	        product.setSellerAddressId(Long.parseLong(map.get("sellerAddressId").toString()));

	    // status
	    if (map.containsKey("status") && map.get("status") != null && !map.get("status").toString().isBlank()) {
	        Object statusObj = map.get("status");
	        if (statusObj instanceof ProductStatus) {
	            product.setStatus((ProductStatus) statusObj);
	        } else {
	            product.setStatus(ProductStatus.valueOf(statusObj.toString()));
	        }
	    }

	    productRepository.save(product);
	}

	// [seller] 상품 삭제 (컨트롤러에서 (Long, Long, String)으로 받음)
	public void deleteProduct(Long productId, Long userId, String role) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new NoSuchElementException("상품이 존재하지 않습니다."));
		validateSellerPermission(userId, product);
		productRepository.delete(product);
	}

	// [seller] 재고 조회 (컨트롤러에서 (Long, Long, String)으로 받음)
	public int getInventory(Long productId, Long userId, String role) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new NoSuchElementException("상품이 존재하지 않습니다."));
		validateSellerPermission(userId, product);
		return product.getStockQuantity();
	}

	// [seller] 재고 변경 (컨트롤러에서 (Long, int, Long, String)으로 받음)
	public void updateInventory(Long productId, int stock, Long userId, String role) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new NoSuchElementException("상품이 존재하지 않습니다."));
		validateSellerPermission(userId, product);

		product.setStockQuantity(stock);

		// 상태 자동 변경
		if (stock <= 0) {
			product.setStatus(ProductStatus.SOLD_OUT);
		} else if (product.getStatus() == ProductStatus.SOLD_OUT || product.getStatus() == ProductStatus.APPROVED) {
			product.setStatus(ProductStatus.APPROVED);
		}
		productRepository.save(product);
	}

	// [admin] 상품 승인
	public void approveProduct(Long productId, Long adminId, String role) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new NoSuchElementException("상품이 존재하지 않습니다."));
		product.setIsApproved(true);
		product.setStatus(ProductStatus.APPROVED);
		productRepository.save(product);
	}

	// [admin] 상품 거절
	public void rejectProduct(Long productId, Long adminId, String role) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new NoSuchElementException("상품이 존재하지 않습니다."));
		product.setIsApproved(false);
		product.setStatus(ProductStatus.REJECTED);
		productRepository.save(product);
	}

	// [admin] 승인 → 대기(PENDING) 상태로 변경
	public void setPending(Long productId, Long adminId, String role) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new NoSuchElementException("상품이 존재하지 않습니다."));
		product.setIsApproved(false);
		product.setStatus(ProductStatus.PENDING);
		productRepository.save(product);
	}

	// 전체 상품 조회
	public List<Map<String, Object>> searchProducts(String keyword) {
		validateKeyword(keyword);
		return productRepository.findByNameContainingIgnoreCaseAndStatus(keyword, ProductStatus.APPROVED).stream()
				.map(this::toMap).toList();
	}

	// 판매자로 검색
	public List<Map<String, Object>> findBySellerAndKeyword(Long userId, String keyword) {
		validateKeyword(keyword);
		return productRepository.findByUserIdAndNameContainingIgnoreCase(userId, keyword).stream().map(this::toMap)
				.toList();
	}

	// 자동 완성
	public List<String> autocompleteProductNames(String prefix) {
		validateKeyword(prefix);
		return productRepository.findTop10ByNameStartingWithIgnoreCase(prefix).stream().map(Product::getName).toList();
	}

	// 필터 검색(금액/평점)
	public List<Map<String, Object>> filterProducts(Long categoryId, int minPrice, int maxPrice, String status) {
		validateKeyword(status);
		return productRepository.findByCategoryIdAndPriceBetweenAndStatus(categoryId, minPrice, maxPrice, status)
				.stream().map(this::toMap).toList();
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

	private Long toLong(Object value) {
		return Long.parseLong(value.toString());
	}

	private void validateKeyword(String keyword) {
		if (keyword == null || keyword.trim().isEmpty()) {
			throw new IllegalArgumentException("검색어를 입력해주세요.");
		}
	}

	private Map<String, Object> toMap(Product p) {
		Map<String, Object> map = new LinkedHashMap<>();
		List<String> categoryPath = getCategoryPath(p.getCategoryId());
		String companyName = getUserCompanyName(p.getUserId());
		List<Review> reviews = reviewRepository.findByProductId(p.getProductId())
	            .orElse(Collections.emptyList());
	    int reviewCount = reviews.size();
	    double averageRating = 0.0;
	    if (reviewCount > 0) {
	        averageRating = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
	    }
	    String url = "http://localhost:10000/api/users/" + p.getUserId();
	    String userName = restTemplate.getForEntity(url, UserDto.class).getBody().getName();
		map.put("productId", p.getProductId());
		map.put("categoryId", p.getCategoryId());
		map.put("userId", p.getUserId());
		map.put("sellerAddressId", p.getSellerAddressId());
		map.put("name", p.getName());
		map.put("userName", userName);
		map.put("description", p.getDescription());
		map.put("price", p.getPrice());
		map.put("discountPrice", p.getDiscountPrice());
		map.put("stockQuantity", p.getStockQuantity());
		map.put("image", p.getImage());
		map.put("status", p.getStatus());
		map.put("courierName", p.getCourierName());
		map.put("shippingFee", p.getShippingFee());
		map.put("reviewCount", p.getReviewCount());
		map.put("salesCount", p.getSalesCount());
		map.put("averageRating", p.getAverageRating());
		map.put("addedAt", p.getAddedAt().toLocalDate());
		map.put("categoryPath", categoryPath);
		map.put("brand", companyName);

		map.put("categoryName", !categoryPath.isEmpty() ? categoryPath.get(categoryPath.size() - 1) : "");
		map.put("brandName", companyName);
		
		map.put("averageRating", averageRating);
	    map.put("reviewCount", reviewCount);
		return map;
	}
	
	// 단일 상품
	public Map<String, Object> getProductById(Long productId) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new NoSuchElementException("상품이 없습니다."));
		Map<String, Object> map = new HashMap<>();
		map.put("productId", product.getProductId());
		map.put("productName", product.getName()); // 이름 통일화
		map.put("name", product.getName());
		map.put("price", product.getPrice());
		map.put("discountPrice", product.getDiscountPrice());
		map.put("image", product.getImage());
		map.put("shippingFee", product.getShippingFee());
		return map;
	}

	public List<Map<String, Object>> getProductsByIds(List<Long> productIds) {
		// 예시: JPA, MyBatis, 직접 쿼리 등
		// select * from product where productId in (?,?,?)
		return productRepository.findByProductIdIn(productIds).stream().map(product -> {
			Map<String, Object> map = new HashMap<>();
			map.put("productId", product.getProductId());
			map.put("name", product.getName());
			map.put("price", product.getPrice());
			map.put("discountPrice", product.getDiscountPrice());
			map.put("image", product.getImage());
			map.put("shippingFee", product.getShippingFee());
			// 필요한 값 추가
			return map;
		}).collect(Collectors.toList());
	}

	// 카테고리 경로화
	private List<String> getCategoryPath(Long categoryId) {
		List<String> path = new java.util.ArrayList<>();
		Category cur = categoryRepository.findById(categoryId).orElse(null);
		while (cur != null) {
			path.add(0, cur.getName());
			if (cur.getParentId() == null)
				break;
			cur = categoryRepository.findById(cur.getParentId()).orElse(null);
		}
		return path;
	}

	// 카테고리 팝업
	public List<Map<String, Object>> getCategoryHierarchy() {
		List<Category> all = categoryRepository.findAll();
		// 대분류
		List<Category> topList = all.stream().filter(c -> c.getParentId() == null).collect(Collectors.toList());

		// 중분류 맵핑
		List<Map<String, Object>> result = new ArrayList<>();
		for (Category top : topList) {
			Map<String, Object> map = new LinkedHashMap<>();
			map.put("id", top.getCategoryId());
			map.put("name", top.getName());
			List<Map<String, Object>> children = all.stream()
					.filter(sub -> top.getCategoryId().equals(sub.getParentId())).map(sub -> {
						Map<String, Object> child = new LinkedHashMap<>();
						child.put("id", sub.getCategoryId());
						child.put("name", sub.getName());
						return child;
					}).collect(Collectors.toList());
			map.put("children", children);
			result.add(map);
		}
		return result;
	}

	private String getUserCompanyName(Long userId) {
		try {
			String url = ApiEndpointConstants.USER_SERVICE_URL + "/" + userId;
			Map<String, Object> userInfo = restTemplate.getForObject(url, Map.class);
			if (userInfo != null && userInfo.get("companyName") != null)
				return userInfo.get("companyName").toString();
		} catch (Exception e) {
		}
		return "";
	}

	public void writeQna(Map<String, Object> map) {
		restTemplate.postForObject(ApiEndpointConstants.QNA_SERVICE_URL, map, Void.class);
	}

	// ProductService 내에서 배송지 목록 가져오기 (userId로)
	public List<Map<String, Object>> getSellerAddresses(Long userId) {
	    String url = ApiEndpointConstants.DELIVERY_SERVICE_URL + "/seller/all?userId=" + userId;
	    ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);
	    return response.getBody();
	}

	public List<Long> findBySellerId(Long userId) {
		List<Long> result = new ArrayList();
		List<Product> pList = productRepository.findAll();
		for(Product p : pList) {
			result.add(p.getProductId());
		}
		return result;
	}
}
