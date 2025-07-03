package org.ezon.msa.common;

public class ApiEndpointConstants {
    public static final String GATEWAY_BASE_URL = "http://localhost:8080";

    // 사용자 서비스 (User/Auth)
    public static final String USER_SERVICE_URL           = GATEWAY_BASE_URL + "/api/profile/users";
    public static final String SELLER_SERVICE_URL         = GATEWAY_BASE_URL + "/api/profile/sellers";
    public static final String AUTH_SERVICE_URL           = GATEWAY_BASE_URL + "/api/auth";
    public static final String PROFILE_SERVICE_URL        = GATEWAY_BASE_URL + "/api/profile";

    // 상품 서비스
    public static final String PRODUCT_SERVICE_URL        = GATEWAY_BASE_URL + "/api/products";
    public static final String PRODUCT_CATEGORY_URL       = GATEWAY_BASE_URL + "/api/products/categories";
    public static final String PRODUCT_SEARCH_URL         = GATEWAY_BASE_URL + "/api/search/products";
    public static final String PRODUCT_AUTOCOMPLETE_URL   = GATEWAY_BASE_URL + "/api/search/products/autocomplete";
    public static final String PRODUCT_FILTER_URL         = GATEWAY_BASE_URL + "/api/search/products/result";

    // 리뷰 서비스
    public static final String REVIEW_SERVICE_URL         = GATEWAY_BASE_URL + "/api/reviews";

    // 문의 서비스 (1:1 문의)
    public static final String QNA_SERVICE_URL            = GATEWAY_BASE_URL + "/api/oneToOnes";

    // 결제 서비스
    public static final String PAYMENT_SERVICE_URL        = GATEWAY_BASE_URL + "/api/payment";

    // 주문 서비스
    public static final String ORDER_SERVICE_URL          = GATEWAY_BASE_URL + "/api/orders";
    public static final String ORDER_ITEM_SERVICE_URL     = GATEWAY_BASE_URL + "/api/orders/order-items";

    // 배송 서비스
    public static final String DELIVERY_SERVICE_URL       = GATEWAY_BASE_URL + "/api/delivery";

    // 정산 서비스
    public static final String SETTLEMENT_SERVICE_URL     = GATEWAY_BASE_URL + "/api/settlement";

    // 장바구니 서비스
    public static final String CART_SERVICE_URL           = GATEWAY_BASE_URL + "/api/cart";

    // 찜 서비스
    public static final String WISH_PRODUCT_SERVICE_URL   = GATEWAY_BASE_URL + "/api/wish/products";
    public static final String WISH_SELLER_SERVICE_URL    = GATEWAY_BASE_URL + "/api/wish/sellers";

    // 고객센터 서비스
    public static final String CUSTOMER_NOTICE_URL        = GATEWAY_BASE_URL + "/api/customer/notice";
    public static final String CUSTOMER_FAQ_URL           = GATEWAY_BASE_URL + "/api/customer/faq";
}
