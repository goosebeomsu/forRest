package com.mvc.forrest.service.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.mvc.forrest.config.auth.LoginUser;
import com.mvc.forrest.dao.user.UserDAO;
import com.mvc.forrest.service.domain.Search;
import com.mvc.forrest.service.domain.User;

@Service
public class UserService {
	
	@Autowired
	private UserDAO userDao;
	
	public void addUser(User user) throws Exception {
		userDao.addUser(user);
	}
	
	public User getUser(String userId) throws Exception {
		return userDao.getUser(userId);
	}
	
	public User getUserByNickname(String nickname) throws Exception {
		return userDao.getUserByNickname(nickname);
	}

	
	public User getUserByName(String userName) throws Exception {
		return userDao.getUserByName(userName);
	}
	
	public User getUserByPhone(String Phone) throws Exception {
		return userDao.getUserByPhone(Phone);
	}

	public User getMyPage(String userId) throws Exception {
		return userDao.getUser(userId);
	}
	
	public Map<String, Object> getUserList(Search search) throws Exception {
		List<User> list = userDao.getUserList(search);
		int totalCount = userDao.getTotalCount();
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("list", list);
		map.put("totalCount", totalCount);
		
		return map ;
	}
	
	public void updateUser(User user) throws Exception {
		userDao.updateUser(user);
	}
	
	public void updateUserImg(User user) throws Exception {
		userDao.updateUserImg(user);
	}

	
	public void updatePassword(User user) throws Exception {
		userDao.updatePassword(user);
	}
	
	public void updateRecentDate(User user) throws Exception{
		userDao.updateRecentDate(user);
	}
	
	public void leaveUser(User user) throws Exception {
		userDao.leaveUser(user);
	}
	
	public void applyLeave(User user) throws Exception{
		userDao.applyLeave(user);
	}
	
	public boolean checkDuplication(String userId) throws Exception {
		boolean result=true;
		User user=userDao.getUser(userId);
		if(user != null) {
			result=false;
		}
		return result;
	}
	
	public String getEncodedUserId() {
		
		LoginUser loginUser= (LoginUser)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return loginUser.getUser().getUserId();
	}
	


}