package org.ezon.msa.controller;

import java.util.List;
import java.util.Map;
import org.ezon.msa.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    private ProductService productService;

    // 통합검색 (상품명)
    @GetMapping("/products")
    public List<Map<String, Object>> searchProducts(@RequestParam String keyword) {
        return productService.searchProducts(keyword);
    }

    // 내 상품 검색
    @GetMapping("/products/me")
    public List<Map<String, Object>> searchMyProducts(
            @RequestParam Long userId,
            @RequestParam String keyword) {
        return productService.findBySellerAndKeyword(userId, keyword);
    }

    // 최근 검색어 (예시, 구현은 별도 필요)
    @GetMapping("/recent")
    public List<String> getRecentKeywords() {
    	return List.of();
    }

    // 자동완성
    @GetMapping("/products/autocomplete")
    public List<String> autocompleteProductNames(@RequestParam String prefix) {
        return productService.autocompleteProductNames(prefix);
    }

    // 상품 필터 조회
    @GetMapping("/products/result")
    public List<Map<String, Object>> filterProducts(
            @RequestParam Long categoryId,
            @RequestParam int minPrice,
            @RequestParam int maxPrice,
            @RequestParam String status) {
        return productService.filterProducts(categoryId, minPrice, maxPrice, status);
    }
}
