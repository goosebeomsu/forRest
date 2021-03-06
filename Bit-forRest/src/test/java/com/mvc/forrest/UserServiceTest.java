package com.mvc.forrest;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.mvc.forrest.service.coupon.CouponService;
import com.mvc.forrest.service.domain.Coupon;
import com.mvc.forrest.service.domain.OldReview;
import com.mvc.forrest.service.domain.OwnCoupon;
import com.mvc.forrest.service.domain.Search;
import com.mvc.forrest.service.domain.User;
import com.mvc.forrest.service.oldreview.OldReviewService;
import com.mvc.forrest.service.user.UserService;


//@RunWith(SpringJUnit4ClassRunner.class)


@SpringBootTest 
public class UserServiceTest {

	@Autowired
	private UserService userService;
	@Autowired
	private OldReviewService oldReviewService;
	@Autowired
	private CouponService couponService;
	
//	@Test
	public void testAddUser() throws Exception {
		
		User user = new User();
		user.setUserId("testUserId");
		user.setUserName("testUserName");
		user.setPassword("testPasswd");
		user.setNickname("testNickname");
		user.setPhone("testPhone");
		user.setUserAddr("testAddr");
		user.setJoinPath("testPath");
		user.setUserRate(4);
		
//		userService.addUser(user);
//		user = userService.getUser("testUserId");

		userService.addUser(user);
		userService.getUser("admin");
		
		
		user = userService.getUser("admin");

		//==> console 확인
		System.out.println(user);
		
		//==> API 확인
		assertEquals("testUserId", user.getUserId());
		assertEquals("testUserName", user.getUserName());
		assertEquals("testPasswd", user.getPassword());
		assertEquals("testPhone", user.getPhone());
		assertEquals("testNickname", user.getNickname());
		assertEquals("testAddr", user.getUserAddr());
		assertEquals("testPath", user.getJoinPath());
		assertEquals(4, 4, user.getUserRate());
		

		
	}
	
	
	//@Test
	public void testUpdateUser() throws Exception{
		
		//테스트 아이디가 있는지 확인
		User user = userService.getUser("testUserId");
		
		//기존정보가 맞는지 확인
		 assertEquals("testUserId", user.getUserId());
		 assertEquals("testUserName", user.getUserName());
		 assertEquals("testPasswd", user.getPassword());
		 assertEquals("testPhone", user.getPhone());
		 assertEquals("testNickname", user.getNickname());
		 assertEquals("testAddr", user.getUserAddr());
		 assertEquals("testPath", user.getJoinPath());
		 assertEquals(4, 4, user.getUserRate());
		
		//새로운 데이터 입력
		user.setNickname("newNickname");
		user.setUserAddr("newUserAddr");
		user.setPhone("newPhone");
		user.setUserImg("newImg");
		
		//업데이트
		userService.updateUser(user);
		
		//업데이트 내용 확인
		user = userService.getUser("testUserId");
		 assertEquals("newNickname", user.getNickname());
		 assertEquals("newUserAddr", user.getUserAddr());
		 assertEquals("newPhone", user.getPhone());
		 assertEquals("newImg", user.getUserImg());

		
		assertEquals("admin", user.getUserId());

	}
	
//	@Test
	public void testGetUserList() throws Exception{
		
		User user = new User();
		user.setUserId("user03@naver.com");
		String userId = user.getUserId();
		
		System.out.println(userId);
		
		Search search = new Search();
		Map<String, Object> map = userService.getUserList(search);
		System.out.println(map.get("list"));
		
		List<OldReview> list = oldReviewService.getOldReviewList(userId);
		System.out.println(list);
		
	}
	
//	@Test
	public void testGetUserByName() throws Exception{
		String userName = "user01";
		User user = new User();
		user.setUserName(userName);
		user = userService.getUserByName(userName);
		System.out.println(user);
		
	}

//	@Test
	public void loginCouponTest() throws Exception{
		
		OwnCoupon oc = new OwnCoupon();
		User user = userService.getUser("admin");
		Coupon coupon = couponService.getCoupon("2");	//2번 쿠폰 = 신규회원 쿠폰
		Calendar cal= Calendar.getInstance();
		cal.add(Calendar.DATE,30);
		Timestamp ts1 = new Timestamp(System.currentTimeMillis());
		Timestamp ts2 = new Timestamp(cal.getTimeInMillis());
		
		oc.setOwnUser(user);
		oc.setOwnCoupon(coupon);
		oc.setOwnCouponCreDate(ts1);
		oc.setOwnCouponDelDate(ts2);
		System.out.println(oc);
		couponService.addOwnCoupon(oc);
		
		
		
		
		
		
//		oc.setOwnUser(dbUser);
//		oc.setOwnCoupon(coupon);
//		oc.setOwnCouponCreDate(ts1);
//		oc.setOwnCouponDelDate(ts2);
//		System.out.println(oc);
//		couponService.addOwnCoupon(oc);
		
		
	}
	
//	@Test
	public void getNickname() throws Exception{
		
		String nickname = "user01";
		
		User user = userService.getUserByNickname(nickname);
		
		System.out.println("결과 : "+user);
		
	}
	
	@Test
	public void leaveUserTest() throws Exception{
		System.out.println();
		
		Search search = new Search();
		User user = new User();
		Map<String , Object> map=userService.getUserList(search);
		System.out.println("map : "+map);
		System.out.println("mapList : "+map.get("list"));

		List<User> list = (List<User>) map.get("list");
		System.out.println("list : "+list);
		System.out.println("leaveDate : "+list.get(0).getLeaveDate().toString().substring(0,10)); 

		SimpleDateFormat dtf = new SimpleDateFormat("yyyy/MM/dd");
        Calendar calendar = Calendar.getInstance();

        LocalDate todaysDate = LocalDate.now();
        System.out.println("today : "+todaysDate.toString());
		System.out.println();
	}

}