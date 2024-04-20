package com.practice.controller;

import com.practice.helper.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.practice.dao.UserRepository;
import com.practice.entity.User;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	public UserRepository  userRepository;
	
	@GetMapping("/")
	public String home(Model m ) {
		
        m.addAttribute("title","Home - SmartContact Manger");
		return"home";
	}
	
	
	@GetMapping("/about")
	public String about(Model m ) {
		
        m.addAttribute("title","About - SmartContact Manger");
		return"about";
	}
	
	@GetMapping("/signup")
	public String signup(Model m ) {
		
        m.addAttribute("title","Register - SmartContact Manger");
        m.addAttribute("user",new User());
		return"signup";
	}
	
	// getting data from login page by /do_register url
	
	@RequestMapping(value="/do_register",method = RequestMethod.POST)
	public  String registerUser(@Valid@ModelAttribute("user")User user,BindingResult result,@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model,HttpSession session) {
	
		
		try {
			if(!agreement) {
				System.out.println("You have not agreed the terms and coditions");
				// throw exception so go to catch and again same process
				throw new Exception("You have not agreed the terms and conditions");
			}
			
			if(result.hasErrors()) {
				
				System.out.println("Error"  + result.toString());
				model.addAttribute("user", user);
				return "signup";
			}
			
			user.setEnable(true);
			user.setImageUrl("default.png");
			user.setRole("ROLE_USER");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			System.out.println(user);
			System.out.println(agreement);
			
			 this.userRepository.save(user);
		
			model.addAttribute("user",new User());
			session.setAttribute("message", new Message("Successfully Registered !!", "alert-success"));
			return"signup";
		}
		
			
		 catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something Went wrong !! " + e.getMessage(), "alert-danger"));
			return"signup";
			}
		}
	
     // Handler for Custome login page
	@GetMapping("/signin")
	public String customeLogin(Model model) {
		System.out.println("In SignIN url");
		//model.addAttribute("title", "Login- SmartContact Manager");
		return "login";
	}


}