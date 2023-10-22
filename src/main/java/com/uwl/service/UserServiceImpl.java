package com.uwl.service;

import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.*;

import com.uwl.model.UserDtls;
import com.uwl.repository.UserRepository;

import net.bytebuddy.utility.RandomString;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private BCryptPasswordEncoder passwordEncode;

	@Autowired
	private JavaMailSender mailsender;
	

	@Override
	public boolean checkEmail(String email) {

		return userRepo.existsByEmail(email);
	}

	@Override
	public UserDtls CreateUser(UserDtls user, String url) {
		user.setPassword(passwordEncode.encode(user.getPassword()));
		user.setRole("ROLE_USER");
		user.setEnabled(false);
		user.setAccountNonLocked(true);
		RandomString rn = new RandomString();
		user.setVerificationCode(rn.make(64));
		sendVerificationMail(user, url);

		return userRepo.save(user);
	}

	public void sendVerificationMail(UserDtls user, String url) {
		String from = "Ameneh.keshavarz@gmail.com";
		String to = user.getEmail();
		String subject = "Account Verification";
		String content = "Dear [[name]], <br>" + "Please click the link below to verify your registration: <br>"
				+ "<h3><a href=\"[[URL]]\" target=\"_self\"> VERIFY </a></h3>" + "Thank you, <br>"
				+ "University Of West London";

		try {
			MimeMessage message = mailsender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message);
			helper.setFrom(from, "UWL");
			helper.setTo(to);
			helper.setSubject(subject);
			content = content.replace("[[name]]", user.getFullName());

			String siteurl = url + "/verify?code=" + user.getVerificationCode();
			content = content.replace("[[URL]]", siteurl);

			helper.setText(content, true);
			mailsender.send(message);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	@Override
	public boolean verifyAccount(String Code) {
		UserDtls userdtls = userRepo.findByVerificationCode(Code);
		if (userdtls != null) {
			userdtls.setEnabled(true);
			userdtls.setVerificationCode(null);
			userRepo.save(userdtls);
			return true;
		}

		return false;
	}

}
