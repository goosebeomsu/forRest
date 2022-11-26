package com.mvc.forrest.controller.product;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mvc.forrest.common.productcondition.ProdCondition;
import com.mvc.forrest.common.utils.FileNameUtils;
import com.mvc.forrest.common.utils.FileUtils;
import com.mvc.forrest.common.validation.ProductUpdateForm;
import com.mvc.forrest.common.validation.ProductValidator;
import com.mvc.forrest.config.auth.LoginUser;
import com.mvc.forrest.service.coupon.CouponService;
import com.mvc.forrest.service.domain.Img;
import com.mvc.forrest.service.domain.Page;
import com.mvc.forrest.service.domain.Product;
import com.mvc.forrest.service.domain.Rental;
import com.mvc.forrest.service.domain.Search;
import com.mvc.forrest.service.domain.Storage;
import com.mvc.forrest.service.domain.User;
import com.mvc.forrest.service.firebase.FCMService;
import com.mvc.forrest.service.product.ProductService;
import com.mvc.forrest.service.rental.RentalService;
import com.mvc.forrest.service.rentalreview.RentalReviewService;
import com.mvc.forrest.service.storage.StorageService;
import com.mvc.forrest.service.user.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;




@Slf4j
@Controller
@RequestMapping("/product/*")
@RequiredArgsConstructor
public class ProductController {
	
	private final ProductService productService;
	
	private final StorageService storageService;
	
	private final RentalService rentalService;
	
	private final UserService userService;
	
	private final RentalReviewService rentalReviewService;
	
	private final FileUtils fileUtils;
		
	private final ProductValidator productValidator;
	
	@Value("5")
	int pageUnit;
	
	@Value("8")
	int pageSize;
	
	@GetMapping("updateRecentImg")
	public String updateRecentImgGet(@RequestParam("prodNo") String prodNo, Model model) throws Exception {
			
		model.addAttribute("prodNo", prodNo);
		
		return "product/updateRecentImg";
	}

	

	@PostMapping("updateRecentImg")
	public String updateRecentImgPost(@RequestParam("fileName") MultipartFile fileName, @ModelAttribute("product") Product product) throws Exception {
	
		String temDir = "C:\\\\Users\\\\bitcamp\\\\git\\\\forRest\\\\Bit-forRest\\\\src\\\\main\\\\resources\\\\static\\\\images\\\\uploadFiles";
		String convertFileName = FileNameUtils.fileNameConvert(fileName.getOriginalFilename());
		
		
		product.setProdNo(product.getProdNo());
		product.setRecentImg(convertFileName);
		
		if(!fileName.getOriginalFilename().isEmpty()) {
			
			fileName.transferTo(new File(temDir, convertFileName));
			System.out.println("파일명 :: "+convertFileName);
			
			product.setRecentImg(convertFileName);			
		}else {
			System.out.println("파일업로드 실패...?");
		}
		
		productService.updateRecentImg(product);
		
		return null;
	}
	
	//회원, 어드민 가능
	@GetMapping("{prodNo}/update")
	public String updateProductGet(@PathVariable String prodNo, Model model) throws Exception {
		log.info("errors={}", model.getAttribute("errors"));
		Product product = productService.getProduct(prodNo);
		model.addAttribute("product", product);	
	
		return "product/updateProduct";
	}
	

	@PostMapping("{prodNo}/update")
	public String updateProductPost(@PathVariable String prodNo, @Validated @ModelAttribute("product") ProductUpdateForm form, 
													BindingResult bindingResult,@RequestParam("uploadFile") List<MultipartFile> uploadFile) throws Exception {
		
		if(uploadFile.size()!=1) {
			fileUtils.deleteImg(prodNo);
			String mainImg=fileUtils.uploadFiles(uploadFile, prodNo, "product");
			form.setProdImg(mainImg);
		}
		
		productValidator.validate(form, bindingResult);
		
		if(bindingResult.hasErrors()) {
			log.info("errors={}", bindingResult);
			return "product/updateProduct";
		}
		
		Product product = Product.builder()
								  .prodNo(form.getProdNo())
								  .prodDetail(form.getProdDetail())
								  .isRental(form.getIsRental())
								  .rentalPrice(form.getRentalPrice())
								  .account(form.getAccount())
								  .category(form.getCategory())
								  .divyAddress(form.getDivyAddress())
								  .prodImg(form.getProdImg())
								  .build();
								  
		
		
		productService.updateProduct(product);
		
		return "redirect:/storage/listStorage";
	}
	
	//어드민 가능
	// 관리자가 물품의 상태변경 ( 보관 )
	@RequestMapping("updateProductCondition")
	public String updateProductCondition(@RequestParam("prodNo") String prodNo) throws Exception {
		
		Product product = productService.getProduct(prodNo);		
				
		String changedProdCondition = productService.getChangedStorageCondition(product.getProdCondition());
		
		product.setProdCondition(changedProdCondition);
		
		productService.updateProductCondition(product);
	
		return "redirect:/storage/listStorageForAdmin";
	}
	

		// 관리자가 물품의 상태변경 ( 대여 )
		@RequestMapping("updateRentalProductCondition")
		public String updateRentalProductCondition(@RequestParam("prodNo") String prodNo, @RequestParam("tranNo") String tranNo) throws Exception {
			
			Product product = productService.getProduct(prodNo);		
            Rental rental = rentalService.getRental(tranNo);
                                    
            String changedProdCondition = productService.getChangedRentalCondition(product.getProdCondition());
            
            product.setProdCondition(changedProdCondition);
            
            if(product.getProdCondition().equals(ProdCondition.STORAGE.getValue())) {
            	rental.setComplete(1);
				rentalService.updateComplete(rental);
            }
			
			productService.updateProductCondition(product);
			
		
			return "redirect:/rental/listRentalForAdmin";
      
		}
	

	//관리자가 물품상태를 일괄변경
	@RequestMapping("updateProductAllCondition")
	public String updateProductAllCondition(@RequestParam("prodNo") String[] prodNo) throws Exception {
		
		for(int i=0; i<prodNo.length; i++) {
			
			Product product = productService.getProduct(prodNo[i]);
			
			String changedProdCondition = productService.getChangedStorageCondition(product.getProdCondition());
			
			product.setProdCondition(changedProdCondition);
	
			productService.updateProductCondition(product);
		}
		
		return "redirect:/storage/listStorageForAdmin";
	}
	
	
		//관리자가 물품상태를 일괄변경 ( 대여 )
	@RequestMapping("updateRentalProductAllCondition")
	public String updateRentalProductAllCondition(@RequestParam("tranNo") String[] tranNo) throws Exception {
		
		int length = tranNo.length;
						
		String[] prodNo = new String[length];
		
		for(int i=0; i<length; i++) {
			prodNo[i] = rentalService.getRental(tranNo[i]).getProdNo();
		}
					
		for(int i=0; i<prodNo.length; i++) {
				
			Product product = productService.getProduct(prodNo[i]);
			Rental rental = rentalService.getRental(tranNo[i]);
			
			String changedProdCondition = productService.getChangedRentalCondition(product.getProdCondition());
			
			product.setProdCondition(changedProdCondition);
			
			if(product.getProdCondition().equals(ProdCondition.STORAGE.getValue())) {
            	rental.setComplete(1);
				rentalService.updateComplete(rental);
            }
			
				productService.updateProductCondition(product);
		}
			
			return "redirect:/rental/listRentalForAdmin";
	}
	
	//유저가 물품보관승인신청을 취소하거나 보관중인 물품을 되돌려받을때 사용
	@RequestMapping("cancelProduct")
	public String cancelProduct (@RequestParam("prodNo") String prodNo) throws Exception {
		
		Product product = productService.getProduct(prodNo);
		
		String changedProdCondition = productService.getCanceledProdCondition(product.getProdCondition());
		
		product.setProdCondition(changedProdCondition);
		
		productService.updateProductCondition(product);
		
		return "redirect:/storage/listStorage";
	}
	
	//유저가 물품대여 승인신청을 취소
		@RequestMapping("cancelRentalProduct")
		public String cancelRentalProduct (@RequestParam("prodNo") String prodNo,@RequestParam("tranNo") String tranNo) throws Exception {
			
			Product product = productService.getProduct(prodNo);
			
			if(product.getProdCondition().equals("물품대여승인신청중")) {
				product.setProdCondition("보관중");
			}	
			
			Rental rental = new Rental();
			rental.setTranNo(tranNo);
			rental.setCancelComplete(1);
			
			rentalService.updateCancelDone(rental);
			productService.updateProductCondition(product);
			
			return "redirect:/rental/listRental";
		}
	
	
	//회원, 어드민 가능
	@RequestMapping("getProduct")
	public String getProduct(@RequestParam("prodNo") String prodNo, Model model) throws Exception {

		Product product = productService.getProduct(prodNo);
		
		//암호화된 유저아이디를 받아옴
		LoginUser loginUser= (LoginUser)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String userId= loginUser.getUser().getUserId();
		
		//getProduct에서 물품주인과 구매자에 따라 다른화면출력을 위한 유저정보
		User sessionUser = userService.getUser(userId);
		
		//물품상세보기에서 리뷰리스트를 받아옴
		Map<String, Object> map = rentalReviewService.getRentalReviewList(prodNo);
		
		List<Img> imglist = fileUtils.getProductImgList(prodNo);
		System.out.println(imglist);
		model.addAttribute("product", product);
		model.addAttribute("sessionUser", sessionUser);
		model.addAttribute("list", map.get("list"));
		model.addAttribute("imglist", imglist);
		
		return "product/getProduct";
	}
	
	//비회원가능
	@RequestMapping("listProduct")
	public String listProduct(@ModelAttribute("search") Search search, Model model) throws Exception {
	
	//뒤로가기시 에러 방지
		if(pageSize != 8) {
			return "main/index";
		}
		
		//카테고리중 전체를 클릭했을때 서치카테고리의 value를 null로 만듬
		if(search.getSearchCategory()=="") {
			search.setSearchCategory(null);
		}
		
		if(search.getSearchKeyword()=="") {
			search.setSearchKeyword(null);
		}
		
		if(search.getOrderCondition()=="") {
			search.setSearchKeyword(null);
		}
		
		if(search.getCurrentPage()==0) {
			search.setCurrentPage(1);
		}
		search.setPageSize(pageSize);
		
		Map<String, Object> map = productService.getProductList(search);
		List<Product> listName = productService.getProductNames();
		
		Page resultPage = new Page( search.getCurrentPage(), ((Integer)map.get("totalCount")).intValue(), pageUnit, pageSize);
		
		model.addAttribute("list", map.get("list"));
		model.addAttribute("resultPage", resultPage);
		model.addAttribute("search", search);
		model.addAttribute("prodNames", listName);
		
		return "product/listProduct";
	}
	
	@RequestMapping("listProductAfterLogin")
	public String listProductAfterLogin(@ModelAttribute("search") Search search, Model model)
			throws Exception {
		
		
		if(search.getSearchCategory()=="") {
			search.setSearchCategory(null);
		}
		
		if(search.getSearchKeyword()=="") {
			search.setSearchKeyword(null);
		}
		
		if(search.getOrderCondition()=="") {
			search.setSearchKeyword(null);
		}
		
		if(search.getCurrentPage()==0) {
			search.setCurrentPage(1);
		}
		
		search.setPageSize(pageSize);

		LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String userId = loginUser.getUser().getUserId();
		
		
		List<Product> list = productService.getProductListHasUser(search, userId);
		List<Product> listName = productService.getProductNames();
		
		Page resultPage = new Page(search.getCurrentPage(), productService.getTotalCount(search), pageUnit, pageSize);
		
		model.addAttribute("resultPage", resultPage);
		model.addAttribute("loginUserId", userId);
		model.addAttribute("list", list);
		model.addAttribute("search", search);
		model.addAttribute("prodNames", listName);

		return "product/listProduct";
	}
	
	//지정된 시간에 보관기간이 만료된 물품의 상태를 자동으로 변경(09 30)
	@Scheduled(cron = "0 30 09 * * ?")
	public void updateProductConditionAuto() throws Exception {
		
		System.out.println("자동실행 테스트");
		
		List<Storage> list = storageService.getExpiredStorageList();
		
		
		for(Storage storage : list) {
			
			Product product = storage.getStorageProd();
			product.setProdCondition("출고완료");
			
			productService.updateProductCondition(product);
			
		}
		
		System.out.println("list:"+ list);
	}

	

}