package com.mvc.forrest.service.product;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.mvc.forrest.common.productcondition.ProdCondition;
import com.mvc.forrest.dao.product.ProductDAO;
import com.mvc.forrest.service.domain.Product;
import com.mvc.forrest.service.domain.Search;

import lombok.RequiredArgsConstructor;




@Service
@RequiredArgsConstructor

public class ProductService {
	
	private final ProductDAO productDAO;
	
	// 물품등록
	public void addProduct(Product product) throws Exception{
		productDAO.addProduct(product);
	}
	
	//물품상세정보
	public Product getProduct(String prodNo) throws Exception{
		 return productDAO.getProduct(prodNo);
	}
	
	//물품정보 업데이트
	public void updateProduct(Product product) throws Exception{
		productDAO.updateProduct(product);
	}
	
	//물품상태 업데이트
	public void updateProductCondition(Product product) throws Exception{
		productDAO.updateProductCondition(product);	
	}
	
	//관리자가 물품의 최근 보관사진을 업데이트
	public void updateRecentImg(Product product) throws Exception{
		productDAO.updateRecentImg(product);	
	}
	
	//대여가능하면서 현재보관중인 물품들의 리스트
	public Map<String, Object> getProductList(Search search) throws Exception{
		
		List<Product> list= productDAO.getProductList(search);
		
		int totalCount = productDAO.getTotalCount(search);
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("list", list );
		map.put("totalCount", totalCount);
		
		return map;
	}
	
	//검색어 자동완성을 위한 물품이름 리스트
	public List<Product> getProductNames() throws Exception{
		
		return productDAO.getProductNames();
	}
	
	//로그인한 유저가 보는 물품리스트
	public List<Product> getProductListHasUser(Search search, String userId) throws Exception{
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("userId", userId);
		map.put("search", search);
		
		return productDAO.getProductListHasUser(map);
	}
	
	//렌탈마켓 상품중 최신순 4개를 메인화면에 출력
	public List<Product> getProductListForIndex() throws Exception{
		List<Product> list= productDAO.getProductListForIndex();
		
		return list;
	}
	
	//물품의 총 갯수
	public int getTotalCount(Search search) throws Exception {
		
		return productDAO.getTotalCount(search);
	}
	
	//대여시 대여횟수 증가를 위해 DB의 대여횟수 받아오기
	public int getRentalCount(String prodNo) throws Exception{
		
		return productDAO.getRentalCount(prodNo);
	}
	
	//대여횟수 업데이트
	public void updateRentalCounting(Product product) throws Exception{

		productDAO.updateRentalCounting(product);

	}
	
	//변경된 보관관련 물품상태를 리턴
	public String getChangedStorageCondition(String prodCondition) {
			
		if(prodCondition.equals(ProdCondition.APPLYING_STORAGE_APPROVAL.getValue())) {
			return ProdCondition.RECEIVING_WAREHOUSE.getValue();
		}
		if (prodCondition.equals(ProdCondition.RECEIVING_WAREHOUSE.getValue())){
			return ProdCondition.STORAGE.getValue();
		}
		if(prodCondition.equals(ProdCondition.STORAGE.getValue())){
			return ProdCondition.RETURN_COMPLETE.getValue();
		} 
			
		throw new RuntimeException("invalid StorageCondition: " + prodCondition);
			
		}
	
	//변경된 대여관련 물품상태를 리턴
	public String getChangedRentalCondition(String prodCondition) {
			
		if(prodCondition.equals(ProdCondition.APPLYING_RENTAL_APPROVAL.getValue())) {
			return ProdCondition.DELIVERY.getValue();
		}
		if (prodCondition.equals(ProdCondition.DELIVERY.getValue())){
			return ProdCondition.RENTAL.getValue();
		}
		if(prodCondition.equals(ProdCondition.RENTAL.getValue())){
			return ProdCondition.STORAGE.getValue();
		} 
			
		throw new RuntimeException("invalid RentalCondition: " + prodCondition);
			
		}
	
	//변경된 대여관련 물품상태를 리턴
	public String getCanceledProdCondition(String prodCondition) {
			
		if(prodCondition.equals(ProdCondition.APPLYING_STORAGE_APPROVAL.getValue())) {
			return ProdCondition.CANCEL_COMPLETE.getValue();
		}
		if (prodCondition.equals(ProdCondition.STORAGE.getValue())){
			return ProdCondition.APPLYING_RETURN_APPROVAL.getValue();
		}
		if(prodCondition.equals(ProdCondition.APPLYING_RETURN_APPROVAL.getValue())){
			return ProdCondition.STORAGE.getValue();
		} 
			
		throw new RuntimeException("invalid CanceledProdCondition: " + prodCondition);
			
		}
	
	public Product setParam(Product product, String userId, String prodNo, String prodImg) {
		product.setUserId(userId);
		product.setProdNo(prodNo);
		product.setProdImg(prodImg);
		return product;
	}
	
	
		
}