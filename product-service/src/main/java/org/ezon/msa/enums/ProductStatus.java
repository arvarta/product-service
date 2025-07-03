package org.ezon.msa.enums;

public enum ProductStatus {
	PENDING,        // 판매자 등록 -> 승인 대기중 (관리자 승인 필요)
    APPROVED,       // 판매 승인 완료, 정상 판매중
    REJECTED,       // 판매(승인) 거절, 미노출
    SOLD_OUT,       // 품절 (재고 0, 임시 비노출)
    HIDDEN			// 판매자가 숨김
}
