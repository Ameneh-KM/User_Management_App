package com.uwl.controller;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.uwl.model.UserDtls;
import com.uwl.repository.UserRepository;
import com.uwl.service.UserService;

@Controller
public class HomeController {
	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private BCryptPasswordEncoder bpasswordencoder;

	@ModelAttribute
	private void userDetails(Model m, Principal p) {
		if (p != null) {
			String email = p.getName();
			UserDtls user = userRepo.findByEmail(email);
			m.addAttribute("user", user);
		}
	}

	@GetMapping("/")
	public String index() {
		return "Index";
	}

	@GetMapping("/register")
	public String register() {
		return "Register";
	}

	@GetMapping("/signin")
	public String login() {
		return "Login";
	}

	@PostMapping("/createUser")
	public String createuser(@ModelAttribute UserDtls user, HttpSession session, HttpServletRequest request) {

		// http://localhost:8065

		String url = request.getRequestURL().toString();

		url = url.replace(request.getServletPath(), "");

		boolean userexist = userService.checkEmail(user.getEmail());
		if (userexist) {
			session.setAttribute("msg", "Email Id already Exists");
		} else {
			UserDtls userdtls = userService.CreateUser(user, url);
			if (userdtls != null) {
				session.setAttribute("msg", "Registered Successfully");
			} else
				session.setAttribute("msg", "Sorry,Something went wrong");
		}
		return "redirect:/register";
	}

	@GetMapping("/loadForgotPassword")
	public String loadForgotPassword() {
		return "forgot_password";

	}

	@GetMapping("/loadResetPassword/{id}")
	public String resetForgotPassword(@PathVariable int id, Model model) {
		model.addAttribute("id", id);
		return "reset_password";

	}

	@PostMapping("/forgotPassword")
	public String forgotPassword(@RequestParam String email, @RequestParam String mobileNumber, HttpSession session) {
		UserDtls userdtls = userRepo.findByEmailAndMobileNumber(email, mobileNumber);
		if (userdtls != null) {
			return "redirect:/loadResetPassword/" + userdtls.getId();

		} else {
			session.setAttribute("msg", "Invalid Email or Mobile Number");
			return "forgot_password";
		}
	}

	@PostMapping("/changePassword")
	public String resetPassword(@RequestParam String psw, @RequestParam String cpsw, Integer id, HttpSession session) {
		UserDtls userdtls = userRepo.findById(id).get();
		String pswencoder = bpasswordencoder.encode(psw);
		userdtls.setPassword(pswencoder);
		UserDtls updateduser = userRepo.save(userdtls);
		if (updateduser != null)
			session.setAttribute("msg", "Password Successfully Changed");
		return "redirect:/loadForgotPassword";

	}

	@GetMapping("/verify")
	public String verifyAccount(@Param("code") String code) {
		if (userService.verifyAccount(code)) {
			return "verify_success";
		} else {
			return "verify_failed";

		}

	}
}
