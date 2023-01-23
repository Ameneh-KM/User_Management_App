package com.uwl.service;

import com.uwl.model.UserDtls;

public interface UserService {
	public UserDtls CreateUser(UserDtls user, String url);

	public boolean checkEmail(String email);
	public boolean verifyAccount(String Code);
	
}
