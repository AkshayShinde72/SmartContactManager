package com.smart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.validation.Valid;



@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;

	//home handler
	@RequestMapping("/")
	public String home(Model model) {

		model.addAttribute("title", "Home - Smart Contact Manager");
		return "home";
	}

	//about page
	@RequestMapping("/about")
	public String about(Model model) {

		model.addAttribute("title", "About - Smart Contact Manager");
		return "about";
	}

	//signup page
	@RequestMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title", "Register - Smart Contact Manager");
		model.addAttribute("user", new User());
		return "signup";
	}

	//register user
	@PostMapping("/do_register")
	public String register(@Valid @ModelAttribute("user") User user, BindingResult result1,
	        @RequestParam(value = "agreement", defaultValue = "false") boolean agreement,
	        Model model) {

	    try {
	        if (!agreement) {
	            model.addAttribute("user", user);
	            model.addAttribute("message", new Message("You must agree to the terms and conditions", "alert-danger"));
	            return "signup";
	        }
	        
	        if(result1.hasErrors()) {
	        	System.out.println("Error "+result1.toString());
	        	model.addAttribute("user",user);
	        	return "signup";
	        }

	        User existingUser = userRepository.findByEmail(user.getEmail());
	        if (existingUser != null) {
	            model.addAttribute("user", user);
	            model.addAttribute("message", new Message("Email already registered", "alert-danger"));
	            return "signup";
	        }

	        user.setRole("ROLE_USER");
	        user.setEnabled(true);
	        user.setImageUrl("default.png");
	        user.setPassword(passwordEncoder.encode(user.getPassword()));

	        User result = this.userRepository.save(user);

	        model.addAttribute("user", new User());
	        model.addAttribute("message", new Message("Successfully registered", "alert-success"));
	        return "signup";

	    } catch (Exception e) {
	        e.printStackTrace();
	        model.addAttribute("user", user);
	        model.addAttribute("message", new Message("Something went wrong: " + e.getMessage(), "alert-danger"));
	        return "signup";
	    }
	}

}
