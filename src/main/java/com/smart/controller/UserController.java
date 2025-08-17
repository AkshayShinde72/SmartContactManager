package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	UserRepository userRepository;

	//method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {

		String userName = principal.getName();
		User user = userRepository.findByEmail(userName);
		model.addAttribute("user", user);
	}


	//dashboard home
	@RequestMapping("index")
	public String dashboard(Model model, Principal principal) {

		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}


	//open add form handler
	@GetMapping("/add-contact") 
	public String openContactForm(Model model) {

		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact",new Contact());
		return "normal/add_contact_form";
	}

	//process add contact form
	@PostMapping("/process-contact")
	public String processContact(
			@ModelAttribute Contact contact
			,@RequestParam("profileImage") MultipartFile file
			,Principal principal){

		try {

			String name = principal.getName();
			User user = this.userRepository.findByEmail(name);
			contact.setUser(user);

			//processing and uploading file
			if(file.isEmpty()) {
				System.out.println("file is empty");
			}else {
				contact.setImage(file.getOriginalFilename());
				File Savedfiles = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(Savedfiles.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image is uploaded");
			}

			user.getContacts().add(contact);
			System.out.println("Data : "+contact);

			this.userRepository.save(user);
			System.out.println("Added contact to database");
		}catch (Exception e) {
			System.out.println("ERROR : "+e.getMessage());
			e.printStackTrace();
		}
		return "normal/add_contact_form";

	}
}
