package com.uwl.config;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.uwl.model.UserDtls;
import com.uwl.repository.UserRepository;

@Component
public class CustomLoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private UserDetailsServiceImpl userServiceimpl;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		String username = request.getParameter("username");
		UserDtls userdtls = userRepo.findByEmail(username);


		if (userdtls != null) {
			if (userdtls.isEnabled() && userdtls.isAccountNonLocked()) {

				if (userdtls.getFailedAttempt() < UserDetailsServiceImpl.MAX_FAILED_ATTEMPTS - 1) {
					userServiceimpl.increaseFailedAttempts(userdtls);
				} else {
					userServiceimpl.lock(userdtls);
					exception = new LockedException("Your account has been locked due to 3 failed attempts."
							+ " It will be unlocked after 24 hours.");
				}
			} else if (!userdtls.isAccountNonLocked()) {
				if (userServiceimpl.unlockWhenTimeExpired(userdtls)) {
					exception = new LockedException("Your account has been unlocked. Please try to login again.");
				}
			}

		}

		super.setDefaultFailureUrl("/signin?error");
		super.onAuthenticationFailure(request, response, exception);
		
	}

}