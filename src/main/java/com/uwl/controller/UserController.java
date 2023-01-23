package com.uwl.controller;

import java.security.Principal;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.uwl.model.UserDtls;
import com.uwl.repository.UserRepository;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserRepository userRepo;

	@Autowired
	private BCryptPasswordEncoder passwordEncode;

	@ModelAttribute
	private void userDetails(Model m, Principal p) {
		String email = p.getName();
		UserDtls user = userRepo.findByEmail(email);

		m.addAttribute("user", user);

	}

	@GetMapping("/")
	public String home() {
		return "user/home";
	}

	@GetMapping("/changePass")
	public String loadChangePassword() {
		return "user/change_password";
	}

	@PostMapping("/updatePassword")
	public String ChangePassword(Principal p, @RequestParam("oldPass") String oldPass,
			@RequestParam("newPass") String newPass, HttpSession session) {

		String email = p.getName();
		UserDtls loginuser = userRepo.findByEmail(email);

		Boolean f = passwordEncode.matches(oldPass, loginuser.getPassword());
		if (f) {
			loginuser.setPassword(passwordEncode.encode(newPass));
			UserDtls updatePasswordUser = userRepo.save(loginuser);
			if (updatePasswordUser != null) {
				session.setAttribute("msg", "Password Has Been Changed Successfully");
				System.out.println(session.getAttribute("msg"));

			} else {
				session.setAttribute("msg", "Something Went Wrong On Server");
				System.out.println(session.getAttribute("msg"));

			}
		} else {
			session.setAttribute("msg", "Old Password is Incorrect");
			System.out.println(session.getAttribute("msg"));

		}
		return "redirect:/user/changePass";
	}
}
