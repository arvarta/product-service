package org.ezon.msa.controller;

import java.util.List;
import java.util.Map;

import org.ezon.msa.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // 메인화면 아이템 목록
    @GetMapping
    public List<Map<String, Object>> getProductsForUser(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) Integer minPrice,
        @RequestParam(required = false) Integer maxPrice,
        @RequestParam(required = false) Double minRating,
        @RequestParam(required = false) String sort
    ) {
        return productService.searchProductsAll(keyword, categoryId, minPrice, maxPrice, minRating, sort);
    }
    
    //카테고리 depth로 나누기
    @GetMapping("/categories")
    public List<Map<String, Object>> getCategoryHierarchy() {
        return productService.getCategoryHierarchy();
    }

    // [seller] 내 상품 목록
    @GetMapping("/seller")
    public List<Map<String, Object>> getMyProducts(
        @RequestParam Long userId,
        @RequestParam String role,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) String keyword
    ) {
        return productService.getProductList(userId, role, categoryId, keyword);
    }

    // [user] 상품 상세 조회
    @GetMapping("/{productId}")
    public Map<String, Object> getProductDetailForUser(@PathVariable Long productId) {
        // userId, role 등 인증에서 추출하여 넘겨야함
        return productService.getProductByIdWithAuth(productId);
    }

    // [seller] 상품 등록
    @PostMapping("/seller")
    public void createProduct(@RequestBody Map<String, Object> map) {
        productService.createProduct(map);
    }
    
    // [seller] 단일 상품 상세 (판매자 권한)
    @GetMapping("/{productId}/seller")
    public Map<String, Object> getProductByIdForSeller(@PathVariable Long productId, @RequestParam Long userId, @RequestParam String role) {
        // 판매자 본인만 볼 수 있도록 체크
        return productService.getProductByIdForSeller(productId, userId, role);
    }


    // [seller] 상품 수정
    @PutMapping("/{productId}/seller")
    public void updateProduct(@PathVariable Long productId, @RequestBody Map<String, Object> map) {
        productService.updateProduct(productId, map);
    }

    // [seller] 상품 삭제
    @DeleteMapping("/{productId}/seller")
    public void deleteProduct(@PathVariable Long productId,
                             @RequestParam Long userId,
                             @RequestParam String role) {
        productService.deleteProduct(productId, userId, role);
    }

    // [seller] 카테고리별 상품 목록
    @GetMapping("/categories/{categoryId}")
    public List<Map<String, Object>> getProductsByCategory(@PathVariable Long categoryId) {
        // userId, role 인증에서 추출 필요. 여기선 미반영.
        return productService.findByCategory(categoryId);
    }

    // [seller] 재고 조회
    @GetMapping("/{productId}/seller/inventory")
    public int getInventory(@PathVariable Long productId,
                            @RequestParam Long userId,
                            @RequestParam String role) {
        return productService.getInventory(productId, userId, role);
    }

    // [seller] 재고 변경
    @PutMapping("/{productId}/seller/inventory")
    public void updateInventory(@PathVariable Long productId,
                                @RequestParam int stock_quantity,
                                @RequestParam Long userId,
                                @RequestParam String role) {
        productService.updateInventory(productId, stock_quantity, userId, role);
    }
    
    // [ADMIN] 전체 상품 목록
    @GetMapping("/admin/all")
    public List<Map<String, Object>> getAllProductsForAdmin() {
        // 관리자이므로 userId는 null, role만 "ADMIN"으로 전달
        return productService.getProductList(null, "ADMIN", null, null);
    }

    // [ADMIN] 승인대기(PENDING) 상품만
    @GetMapping("/admin/pending")
    public List<Map<String, Object>> getPendingProductsForAdmin() {
        return productService.getPendingProductsForAdmin();
    }
    
    // [admin] 상품 승인
    @PostMapping("/{productId}/approve")
    public void approveProduct(@PathVariable Long productId,
                               @RequestParam Long adminId,
                               @RequestParam String role) {
        productService.approveProduct(productId, adminId, role);
    }

    // [admin] 상품 거절
    @PostMapping("/{productId}/reject")
    public void rejectProduct(@PathVariable Long productId,
                              @RequestParam Long adminId,
                              @RequestParam String role) {
        productService.rejectProduct(productId, adminId, role);
    }
    
    // [admin] 승인 → 대기(PENDING) 상태로 변경
    @PostMapping("/{productId}/pending")
    public void setPending(@PathVariable Long productId,
                           @RequestParam Long adminId,
                           @RequestParam String role) {
        productService.setPending(productId, adminId, role);
    }

    
    // 제품별 문의(QnA) 작성
    @PostMapping("/{productId}/oneToOnes")
    public void writeQna(@PathVariable Long productId, @RequestBody Map<String, Object> map) {
        map.put("product_id", productId);
        productService.writeQna(map);
    }

    // 단일 상품 조회 (프론트·주문·장바구니 등에서 개별 상품정보 필요할 때)
    @GetMapping("/cart/{productId}")
    public Map<String, Object> getProductById(@PathVariable Long productId) {
        // 예시: 실제로는 DTO 또는 Map으로 반환
        return productService.getProductById(productId);
    }
    
    // 여러 상품ID로 한 번에 상품정보 조회 (장바구니, 주문 등에서 사용)
    @GetMapping("/cart")
    public List<Map<String, Object>> getProductsByIds(@RequestParam("productIds") List<Long> productIds) {
        return productService.getProductsByIds(productIds);
    }
    
    @GetMapping("/user/{userId}/pIdList")
    public ResponseEntity<List<Long>> getProductsId(@PathVariable Long userId) {
        return ResponseEntity.ok(productService.findBySellerId(userId));
    }
}
