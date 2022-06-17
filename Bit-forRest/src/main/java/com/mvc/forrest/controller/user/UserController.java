package com.mvc.forrest.controller.user;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.mvc.forrest.common.utils.FileNameUtils;
import com.mvc.forrest.config.auth.LoginUser;
import com.mvc.forrest.service.coupon.CouponService;
import com.mvc.forrest.service.domain.Coupon;
import com.mvc.forrest.service.domain.OldReview;
import com.mvc.forrest.service.domain.OwnCoupon;
import com.mvc.forrest.service.domain.Page;
import com.mvc.forrest.service.domain.Search;
import com.mvc.forrest.service.domain.User;
import com.mvc.forrest.service.old.OldService;
import com.mvc.forrest.service.oldreview.OldReviewService;
import com.mvc.forrest.service.rental.RentalService;
import com.mvc.forrest.service.user.UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
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
	private OldReviewService oldReviewService;
	@Autowired
	private RentalService rentalService;
//	@Autowired
//	private OldReviewService oldReivewService;
	@Autowired
	private PasswordEncoder passwordEncoder;
	
//	private final Authentication authentication;
    private final AuthenticationManager authenticationManager;
	
	@Value("5")
	int pageUnit;
	@Value("10")
	int pageSize;
	
	
	@GetMapping("login")			//유저, 관리자
	public String login() throws Exception{
		
		System.out.println("/user/login : GET");

		return "user/login";
	}
	
	@PostMapping("login")			//유저, 관리자
	public String login(@ModelAttribute("user") User user , HttpSession session, Model model ) throws Exception{
		
		System.out.println("/user/login : POST");
		
		User dbUser=userService.getUser(user.getUserId());
		
		System.out.println("입력받은 ID/PW : "+user);
		System.out.println("DB와 일치하는 ID/PW : "+dbUser);
		
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
			
			//세션에 user 저장
			session.setAttribute("user", dbUser);
//			model.addAttribute("user", dbUser);	
			
			//신규회원 쿠폰발급
			if(dbUser.getJoinDate().equals(dbUser.getRecentDate())) {
				OwnCoupon oc = new OwnCoupon();
				Coupon coupon = couponService.getCoupon("2");	//2번 쿠폰 = 신규회원 쿠폰
				Calendar cal= Calendar.getInstance();
				cal.add(Calendar.DATE,30);
				Timestamp ts1 = new Timestamp(System.currentTimeMillis());
				Timestamp ts2 = new Timestamp(cal.getTimeInMillis());
				
				oc.setOwnUser(dbUser);
				oc.setOwnCoupon(coupon);
				oc.setOwnCouponCreDate(ts1);
				oc.setOwnCouponDelDate(ts2);
				couponService.addOwnCoupon(oc);
				System.out.println("### 신규회원 쿠폰발급 ###");
			}
			
			userService.updateRecentDate(dbUser);		//최근접속일자 update
				
			return "redirect:/";
			
		//해당 id와 pwd가 불일치할 경우	
		}else{
			model.addAttribute("message", "비밀번호가 일치하지 않습니다.");
			return "user/login";
		}
		
	}
	

//		### spring security 사용으로 인한 미사용 method	###
	@GetMapping("logout")				//유저, 관리자
	public String logout(HttpServletRequest request, HttpServletResponse response) throws Exception{
			
		
	        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
	        return "redirect:/";
	}
	
	@GetMapping("addUser")				//유저, 관리자
	public String addUser() throws Exception{
		
		System.out.println("/user/addUser : GET");
		
		return "user/addUserView";
	}
	
	@RequestMapping("addUser")			//유저, 관리자
	public String addUser( @ModelAttribute("user") User user,
							@RequestParam("userImgFile")MultipartFile file ) throws Exception {

		String temDir = "C:\\Users\\bitcamp\\git\\forRest\\Bit-forRest\\src\\main\\resources\\static\\images\\uploadFiles";
		
		System.out.println("/user/addUser : POST");
		
		if (!file.getOriginalFilename().isEmpty()) {
            String filename = file.getOriginalFilename();
            filename =  FileNameUtils.fileNameConvert(filename);
            file.transferTo(new File(temDir,filename));
            user.setUserImg(filename);
        }
		
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		
		userService.addUser(user);
				
		return "user/login";
	}
	
	@GetMapping("findId")				//유저, 관리자
	public String findId () throws Exception{

		System.out.println("/user/findId : GET");
		
		return "user/findIdView";
	}
	
	@PostMapping("findId")				//유저, 관리자
	public String findId (@ModelAttribute("user") User user, String sms,
							Model model) throws Exception{
		System.out.println("/user/findId : POST");
		
		// sms 인증필요 보낸 sms와 유저sms가 일치해야함
		User userByPhone = userService.getUserByPhone(user.getPhone());
		User userByName = userService.getUserByName(user.getUserName());
		if(userByName.getUserId().equals(userByPhone.getUserId())){
			user = userByName;
			userByName.getJoinDate().toString().substring(pageUnit, pageSize);
			model.addAttribute("userId", user.getUserId());
			model.addAttribute("userJoinDate", user.getJoinDate().toString().substring(0, 10));
			
			return "user/findId";
		}
		 
		return "user/findIdView";
	}
	
	@GetMapping("findPwd")				//유저, 관리자
	public String findPwd() throws Exception{
		
		System.out.println("/user/findPwd : GET");
		
		return "user/findPwd";
	}
	
	@PostMapping("findPwd")				//유저, 관리자
	public String findPwd(@ModelAttribute("user") User user, String sms, 
							HttpSession session, Model model) throws Exception{
		
		System.out.println("/user/findPwd : POST");
		
		// sms인증 필요
		
		User userById = userService.getUser(user.getUserId());
		User userByPhone = userService.getUserByPhone(user.getPhone());
		
		if(userById.getUserId().equals(userByPhone.getUserId())){
			session.setAttribute("user", userById);
			model.addAttribute("user", userById);
			return "user/pwdReset";
		}
		
		return "user/findPwd";
	}	
	
	@GetMapping("pwdReset")				//유저, 관리자				
	public String pwdReset() throws Exception{
		
		System.out.println("/user/pwdReset : GET");
		
		return "user/pwdReset";
	}
	
	@PostMapping("pwdReset")			//유저, 관리자
	public String pwdReset(@RequestParam("password") String password, HttpSession session) throws Exception{
		
		System.out.println("/user/pwdReset : POST");
		
		User sessionUser = (User)session.getAttribute("user");
		sessionUser.setPassword(password);
		userService.updatePassword(sessionUser);
		
		return "redirect:/";
	}
	
	@RequestMapping("getUserList")		//관리자
	public String getUserList( @ModelAttribute("search") Search search , Model model ) throws Exception{
		
		System.out.println("/user/getUserList : GET / POST");
		
		if(search.getCurrentPage() ==0 ){
			search.setCurrentPage(1);
		}
		search.setPageSize(pageSize);
		
		Map<String , Object> map=userService.getUserList(search);
		
		System.out.println("# map : "+map);
		
		Page resultPage = new Page( search.getCurrentPage(), ((Integer)map.get("totalCount")).intValue(), pageUnit, pageSize);
		
		// Model 과 View 연결
		model.addAttribute("list", map.get("list"));
		model.addAttribute("resultPage", resultPage);
		model.addAttribute("search", search);
		
		return "user/getUserList";
	}
	
	@RequestMapping("getUser")		//유저, 관리자
	public String getUser( @RequestParam("userId") String userId , Model model,
							HttpSession session, Search search) throws Exception {
		
		System.out.println("/user/getUser : POST / GET");
		
		User dbUser = userService.getUser(userId);
		LoginUser sessionUser= 
				(LoginUser)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		Map<String , Object> map=couponService.getOwnCouponList(userId);
		model.addAttribute("map", map.get("list"));
		model.addAttribute("user", sessionUser);
		

		model.addAttribute("user", dbUser);

		return "user/getUser";
	}
	
	@GetMapping("updateUser")
	public String updateUser( @RequestParam("userId") String userId , Model model ) throws Exception {
		
		System.out.println("/user/updateUser : GET");
		
		User user = userService.getUser(userId);
		
		//////////////////////////////////////////////////////////////////////////////////
//		System.out.println("   리로드 시작");
//		Collection<SimpleGrantedAuthority> oldAuthorities = (Collection<SimpleGrantedAuthority>)SecurityContextHolder.getContext().getAuthentication().getAuthorities();
//		SimpleGrantedAuthority authority = new SimpleGrantedAuthority("admin");
//		List<SimpleGrantedAuthority> updatedAuthorities = new ArrayList<SimpleGrantedAuthority>();
//		
//		updatedAuthorities.add(authority);
//		updatedAuthorities.addAll(oldAuthorities);
//		
//		SecurityContextHolder.getContext().setAuthentication(
//		new UsernamePasswordAuthenticationToken(
//		SecurityContextHolder.getContext().getAuthentication().getPrincipal(), 
//		SecurityContextHolder.getContext().getAuthentication().getCredentials(), 
//		updatedAuthorities));
//		
//		SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		
		
		
		model.addAttribute("user", user);
		
		return "user/updateUser";
	}
	
	@PostMapping("updateUser")			//유저, 관리자
	public String updateUser( @ModelAttribute("user") User user,
							@RequestParam("userImgFile")MultipartFile file ) throws Exception {
		System.out.println("/user/updateUser : POST");

		String temDir = "C:\\Users\\bitcamp\\git\\forRest\\Bit-forRest\\src\\main\\resources\\static\\images\\uploadFiles";
		
		if (!file.getOriginalFilename().isEmpty()) {
            String filename = file.getOriginalFilename();
            filename =  FileNameUtils.fileNameConvert(filename);
            file.transferTo(new File(temDir,filename));
            user.setUserImg(filename);

        }
		userService.updateUser(user);
		/////////////////////////////////////////////////////////////////////////////////
//	
//		user = userService.getUser(user.getUserId());
//		System.out.println("          #");
//	    Authentication authentication = new 
//	    LoginUser loginUser = (LoginUser) authentication.getPrincipal();
//	    
//	    SecurityContextHolder.getContext().setAuthentication(new authenti
//		
//		System.out.println("          ##");
//		System.out.println("          ###");
//		
//		
//		
//		System.out.println("   리로드 끝");
		//////////////////////////////////////////////////////////////////////////////////
		
		
		return "redirect:/user/updateUser?userId="+user.getUserId();
	}
	
	@GetMapping("deleteUser")		//유저, 관리자
	public String deleteUser()throws Exception {
		
		System.out.println("/user/deleteUser : GET");
		
		return "user/deleteUserView";
	}
	
	@PostMapping("deleteUser")		//유저, 관리자
	public String deleteUser(@RequestParam("password") String password, HttpSession session)throws Exception {
		
		System.out.println("/user/deleteUser : POST");

		User user = (User)session.getAttribute("user");
		
		System.out.println(user);
		if(user.getPassword().equals(password)) {
//			userService.leaverUser(user);
			System.out.println("if문안으로 진입");
		}
		

		return "redirect:/";
	}
}