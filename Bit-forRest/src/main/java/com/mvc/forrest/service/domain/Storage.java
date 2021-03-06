package com.mvc.forrest.service.domain;

import java.sql.Date;
import java.sql.Timestamp;

import lombok.Data;

@Data
public class Storage {
	
	private String tranNo;
	private String userId;
	private String prodNo;
	private String divyRequest;
	private String divyAddress;
	private String pickupAddress;
	private Date startDate;
	private Date endDate;
	private int period;
	private int tranCode;
	private String paymentNo;
	private Timestamp  paymentDate;
	private String paymentWay;
	private String receiverPhone;
	private String receiverName;
	private String prodName;
	private String prodImg;
	private int originPrice;
	private int discountPrice;
	private int resultPrice;
	private Product storageProd;
	private Date rentalStartDate;
	private Date rentalEndDate;
	
	
}