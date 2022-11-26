package com.mvc.forrest.common.productcondition;

public enum ProdCondition {
	
	APPLYING_STORAGE_APPROVAL("물품보관승인신청중"),
	RECEIVING_WAREHOUSE("입고중"),
	STORAGE("보관중"),
	APPLYING_RETURN_APPROVAL("출고승인신청중"),
	RETURN_COMPLETE("출고완료"),
	APPLYING_RENTAL_APPROVAL("물품대여승인신청중"),
	DELIVERY("배송중"),
	RENTAL("대여중"),
	CANCEL_COMPLETE("취소완료");
	
	private final String value;

	private ProdCondition(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
	
}
