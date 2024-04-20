package com.practice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.practice.dao.UserRepository;
import com.practice.entity.User;
import com.practice.service.EmailService;

import jakarta.servlet.http.HttpSession;

import java.util.Random;

@Controller
public class ForgotController {

	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	

	Random random = new Random();

	// Handler to Open email Form when click on forgot password

	@RequestMapping("/forgot")
	public String openEmailForm() {
		return "forgot_email_form";
	}

	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email, HttpSession session) {
		System.out.println("EMAIL " + email);

		// generating otp of 4 digit

		int otp = random.nextInt(9999);

		System.out.println("OTP " + otp);

		String subject = "OTP From SCM";
		String message = "" + "<div style='border:1px solid #e2e2e2; padding:20px'>" + "<h1>" + "OTP is " + "<b>" + otp
				+ "</n>" + "</h1> " + "</div>";
		String to = email;

		boolean flag = this.emailService.sendEmail(subject, message, to);
		if (flag) {

			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			return "verify_otp";

		} else {

			session.setAttribute("message", "Check your email id !!");

			return "forgot_email_form";
		}

	}

	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp, HttpSession session) {
		 // getting otp send from backend as we stored it in session
		int myOtp = (int) session.getAttribute("myotp");

		System.out.println("User OTP " + otp);
		System.out.println("Our OTP " + myOtp);

		String email = (String) session.getAttribute("email");
		if (myOtp == otp) {
			// password change form
		User user = this.userRepository.getUserByUserName(email);

			if (user == null) {
				// send error message
				session.setAttribute("message", "User does not exits with this email !!");

				return "forgot_email_form";
			} else {
				//
				return "password_change_form";

			}

		
		} else {
			session.setAttribute("message", "You have entered wrong otp !!");
			return "verify_otp";
		}
	}
	
	// Handler to save new password
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newPassword") String newPassword,HttpSession session){
		String email = (String) session.getAttribute("email");
		User user = this.userRepository.getUserByUserName(email);
		user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
		this.userRepository.save(user);
		System.out.println("working in password change");
		return "redirect:/signin?change=password changed successfully..";
		
	}

}
