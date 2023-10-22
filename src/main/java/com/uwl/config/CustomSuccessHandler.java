package com.uwl.config;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.uwl.model.UserDtls;
import com.uwl.repository.UserRepository;
import com.uwl.service.UserServiceImpl;

@Configuration
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

	@Autowired
	private UserDetailsServiceImpl userServiceimpl;

	@Autowired
	private UserRepository userRep;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

		CustomUserDetails customuserdtls = (CustomUserDetails) authentication.getPrincipal();
		UserDtls userdtls = userRep.findByEmail(customuserdtls.getUsername());

		if (userdtls.getFailedAttempt() > 0) {
			userServiceimpl.resetFailedAttempts(userdtls.getEmail());
		}

		if (roles.contains("ROLE_ADMIN")) {
			response.sendRedirect("/admin/");
		} else if (roles.contains("ROLE_TEACHER")) {
			response.sendRedirect("/teacher/");
		} else {
			response.sendRedirect("/user/");
		}
	}
}