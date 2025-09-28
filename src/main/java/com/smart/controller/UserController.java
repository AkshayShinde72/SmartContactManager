package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;

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
			,Principal principal
			,RedirectAttributes redirectAttributes){

		try {

			String name = principal.getName();
			User user = this.userRepository.findByEmail(name);
			contact.setUser(user);

			//processing and uploading file
			if(file.isEmpty()) {
				System.out.println("file is empty");
				contact.setImage("contact.png");
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
			//Message success
			 redirectAttributes.addFlashAttribute("message", new Message("Your contact is added || Add more.", "success"));
		    
		}catch (Exception e) {
			System.out.println("ERROR : "+e.getMessage());
			e.printStackTrace();
			//Message error
			  redirectAttributes.addFlashAttribute("message", new Message("Have error try again", "error"));
		}
		return "redirect:/user/add-contact";


	}
	
	//show contacts handler
	//per page 5 contct(n) current page 0(page)
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page")int page,Model m, Principal principal) {
		m.addAttribute("title", "Show User Contacts");
		
		/*we creating without repo of contact
		String userName = principal.getName();
		User user = this.userRepository.findByEmail(userName);
		List<Contact> contactList = user.getContacts();*/
		
		String username = principal.getName();
		User user = this.userRepository.findByEmail(username);
		
		Pageable pageable = PageRequest.of(page, 5) ;
		Page<Contact> contacts = this.contactRepository.getContactsByUserId(user.getId(), pageable);
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());
		
		return "normal/show_contacts";
	}
	
	//showing particular contact details
	@GetMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId, Model m, Principal principal) {
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		String username = principal.getName();
		User user = this.userRepository.findByEmail(username);
		
		if(user.getId()==contact.getUser().getId()) {
			m.addAttribute("contact", contact);
			m.addAttribute("title", contact.getName());
		}
		
		return "normal/contact_detail";
	}
	
	//delete contact handler
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId")int cId, Principal principal, HttpSession session) {
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		String username = principal.getName();
		User user = this.userRepository.findByEmail(username);

		if(user.getId()==contact.getUser().getId()) {
			contact.setUser(null);
			this.contactRepository.delete(contact);
			session.setAttribute("message", new Message("contact deleted successfuly..", "success"));
		}
		
		
		return "redirect:/user/show-contacts/0";
		
	}
	
}
