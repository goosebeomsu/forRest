package com.mvc.forrest.productcondition;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.mvc.forrest.common.productcondition.ProdCondition;
import com.mvc.forrest.service.domain.Product;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProductConditionTest {
	
	@Test
	void getValueTest() {
		String productCondition = ProdCondition.APPLYING_RENTAL_APPROVAL.getValue();
		Assertions.assertThat("물품대여승인신청중").isEqualTo(productCondition);
		log.info("productCondition = {}", productCondition);
	}
	
	@Test
	void registerStorageProductConditionTest() {
		Product product = new Product();
		product.setProdCondition(ProdCondition.APPLYING_STORAGE_APPROVAL.getValue());
		
	}

}
