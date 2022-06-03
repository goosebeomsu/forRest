package com.mvc.forrest.controller.user;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.mvc.forrest.service.coupon.CouponService;
import com.mvc.forrest.service.domain.OwnCoupon;
import com.mvc.forrest.service.domain.Page;
import com.mvc.forrest.service.domain.Search;
import com.mvc.forrest.service.domain.User;
import com.mvc.forrest.service.old.OldService;
import com.mvc.forrest.service.rental.RentalService;
import com.mvc.forrest.service.user.UserService;


@Controller
@RequestMapping("/user/*")
public class UserController {

	@Autowired
	private UserService userService;
	@Autowired
	private CouponService couponService;
	@Autowired
	private OldService oldService;
	@Autowired
	private RentalService rentalService;
	@Autowired
//	private OldReviewService oldReivewService;
	
	@Value("5")
	int pageUnit;
	@Value("10")
	int pageSize;
	
	public UserController(){
	}
	
	@GetMapping("login")
	public String login() throws Exception{
		
		System.out.println("/user/logon : GET");

		return "user/login";
	}
	
	@PostMapping("login")
	public String login(@ModelAttribute("user") User user , HttpSession session, Model model ) throws Exception{
		
		System.out.println("/user/login : POST");
		
		User dbUser=userService.getUser(user.getUserId());
		
		//db에 아이디가 없을 경우
		if(dbUser==null) {
			model.addAttribute("message", "가입되지않은 아이디입니다.");
			return "user/login";
		}
		
		//db에 아이디가 있지만 회원탈퇴
		if(dbUser.getRole()=="leave") {
			model.addAttribute("message", "탈퇴처리된 회원입니다..");
			return "user/login";	
		}
		
		//db에 아이디가 있지만 로그인제한된 유저
		if(dbUser.getRole()=="restrict") {
			model.addAttribute("message", "이용제한된 회원입니다..");
			return "user/login";	
		}
		
		//해당 id와 pwd가 일치할 경우
		if( user.getPassword().equals(dbUser.getPassword())){
			session.setAttribute("user", dbUser);		//세션에 user 저장
			
			if(user.getJoinDate()==user.getRecentDate()) {
				OwnCoupon oc = new OwnCoupon();
				oc.setOwnuser(dbUser);
				couponService.getCoupon(2);
			}
			
			userService.updateRecentDate(dbUser);		//최근접속일자 update
				
			return "index";								//나중에 정확한 경로로 수정
			
		//해당 id와 pwd가 불일치할 경우	
		}else{
			model.addAttribute("message", "비밀번호가 일치하지 않습니다.");
			return "user/login";
		}
		
	}
	
	@GetMapping("logout")
	public String logout(HttpSession session ) throws Exception{
		
		System.out.println("/user/logout : GET");
		
		session.invalidate();
		
		return "redirect:/";
	}
	
//	@GetMapping("addUser")
	public String addUser() throws Exception{
		
		System.out.println("/user/addUser : GET");
		
		return "user/Test";
	}
	
	@RequestMapping("addUser")
	public String addUser( @ModelAttribute("user") User user ) throws Exception {

		System.out.println("/user/addUser : POST");
		
		user = new User();
		user.setUserId("test");
		user.setJoinPath("own");
		user.setNickname("testnick");
		user.setPassword("1234");
		user.setPhone("phone");
		user.setUserAddr("testAddr");
		user.setUserName("testName");
		user.setUserRate(4);
		
		userService.addUser(user);
		System.out.println("Test add");
		
		return "user/Test";
	}
	
	@GetMapping("findId")
	public String findId () throws Exception{

		System.out.println("/user/findId : GET");
		
		return null;
	}
	
	@PostMapping("findId")
	public String findId (String userId) throws Exception{

		System.out.println("/user/findId : POST");
		
		// #############	need logic	################
		
		return null;
	}
	
	@GetMapping("findPwd")
	public String findPwd() throws Exception{
		
		System.out.println("/user/findPwd : GET");
		
		return null;
	}
	
	@PostMapping("findPwd")
	public String findPwd(String password) throws Exception{
		
		System.out.println("/user/findPwd : POST");
		
		// #############	need logic	################
		
		return null;
	}	
	
	@GetMapping("pwdReset")
	public String pwdReset() throws Exception{
		
		System.out.println("/user/pwdReset : GET");
		
		return null;
	}
	
	@PostMapping("pwdReset")
	public String pwdReset(String password) throws Exception{
		
		System.out.println("/user/pwdReset : POST");
		
		// #############	need logic	################
		
		return null;
	}
	
	@RequestMapping("listUser")
	public String listUser( @ModelAttribute("search") Search search , Model model , HttpServletRequest request) throws Exception{
		
		System.out.println("/user/listUser : GET / POST");
		
		if(search.getCurrentPage() ==0 ){
			search.setCurrentPage(1);
		}
		search.setPageSize(pageSize);
		
		Map<String , Object> map=userService.getUserList(search);
		
		Page resultPage = new Page( search.getCurrentPage(), ((Integer)map.get("totalCount")).intValue(), pageUnit, pageSize);
		System.out.println(resultPage);
		
		model.addAttribute("list", map.get("list"));
		model.addAttribute("resultPage", resultPage);
		model.addAttribute("search", search);
		
		return null;
	}
	
	@GetMapping("getUser")
	public String getUser( @RequestParam("userId") String userId , Model model ) throws Exception {
		
		System.out.println("/user/getUser : GET");
		
		User user = userService.getUser(userId);

		model.addAttribute("user", user);
		
		return null;
	}
	
	@GetMapping("getMyPage")
	public String getMyPage( @RequestParam("userId") String userId , Model model ) throws Exception {
		
		System.out.println("/user/getMyPage : GET");
		
		User user = userService.getUser(userId);

		model.addAttribute("user", user);
		
		return null;
	}
	
	@GetMapping("deleteUser")
	public String deleteUser()throws Exception {
		
		System.out.println("/user/deleteUser : GET");
		
		return null;
	}
	
	@PostMapping("deleteUser")
	public String deleteUser(String userId)throws Exception {
		
		System.out.println("/user/deleteUser : POST");

		// #############	need logic // 인자수정		################

		return null;
	}
}