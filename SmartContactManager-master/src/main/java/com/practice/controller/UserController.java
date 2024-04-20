package com.practice.controller;

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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.practice.dao.ContactRepository;
import com.practice.dao.UserRepository;
import com.practice.entity.Contact;
import com.practice.entity.User;
import com.practice.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;

	// Making common handler which add data in model object
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {

		// Principal will give us primary key of user of login person through that we
		// can get there detail and we will send it to dashboard
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		model.addAttribute("user", user);
	}

	// Handler for userDashBoard

	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		/*
		 * System.out.println("Fireing the user url with index"); // Principal will give
		 * us primary key of user of login person through that we can get there detail
		 * and we will send it to dashboard String userName = principal.getName(); User
		 * user = this.userRepository.getUserByUserName(userName);
		 * model.addAttribute("user", user);
		 */
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}

	// Handler for Add Contact Form

	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		// Sending titile and also new object of contact and user is set in model by
		// comman data handler
		System.out.println("add contact form Handler");
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());

		return "normal/add_contact_form";
	}

	// process add-contact form saving data to db
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {

		try {
			String name = principal.getName();

			// we want user also in which we want to store this contact
			User user = userRepository.getUserByUserName(name);

			// processing and uploading file..

			if (file.isEmpty()) {
				// if the file is empty then try our message
				System.out.println("File is empty");
				contact.setImage("contact.png"); // Setting the default image

			} else {
				// file the file to folder and update the name to contact
				contact.setImage(file.getOriginalFilename());

				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				System.out.println("Image is uploaded");

			}

			// Mapping in contact also this user. So it tells contact is of this login user
			contact.setUser(user);

			// now in user adding contact given and than saving back the user
			user.getContact().add(contact);

			this.userRepository.save(user);

			// message success.......
			session.setAttribute("message", new Message("Your contact is added !! Add more..", "success"));

			// System.out.println("contact details" + contact);
		} catch (Exception e) {
			System.out.println("ERROR" + e.getMessage());
			e.printStackTrace();

			// message error
			session.setAttribute("message", new Message("Some went wrong !! Try again..", "danger"));
		}
		return "normal/add_contact_form";
	}

	// Handler for View Contacts

	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model m, Principal principal) {

		// Geting user and than geting contact base on id of user
		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);

		// geting contact by contactRepository
		// Creating object of pageable
		Pageable pageable = PageRequest.of(page, 3);
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);
		m.addAttribute("contacts", contacts);
		// Sending current page and total page to html page
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());

		return "normal/show_contacts";
	}
	

	// showing particular contact details.

		@RequestMapping("/{cId}/contact")
		public String showContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal) {
			System.out.println("CID " + cId);

			Optional<Contact> contactOptional = this.contactRepository.findById(cId);
			Contact contact = contactOptional.get();

			//
			String userName = principal.getName();
			User user = this.userRepository.getUserByUserName(userName);

			if (user.getId() == contact.getUser().getId()) {
				model.addAttribute("contact", contact);
				model.addAttribute("title", contact.getName());
			}

			return "normal/contact_detail";
		}

		@GetMapping("/delete/{cid}")
		@Transactional
		public String deleteContact(@PathVariable("cid") Integer cId, Model model, HttpSession session,
				Principal principal) {
			System.out.println("CID " + cId);

			Contact contact = this.contactRepository.findById(cId).get();
		// check...Assignment..image delete

			// delete old photo

			User user = this.userRepository.getUserByUserName(principal.getName());
            contact.setUser(null); // used this because if we delete than also it will not delelte as cascade rule of mapping. So we set reference null and than deleted
			this.contactRepository.delete(contact);
			

		//	this.userRepository.save(user);

			System.out.println("DELETED");
			session.setAttribute("message", new Message("Contact deleted succesfully...", "success"));

			return "redirect:/user/show-contacts/0";
		}
		
		// Update form handler
		@PostMapping("/update-contact/{cId}")
		public String updateForm(@PathVariable("cId")Integer cId,Model model) {
			
			Contact contact = this.contactRepository.findById(cId).get();
			model.addAttribute("contact", contact);
			
		 return "normal/update_form";
		}
		
		// update contact handler
		@PostMapping("/process-update")
		public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
				Model m, HttpSession session, Principal principal) {

			try {

				// Getting old  contact details
				Contact oldcontactDetail = this.contactRepository.findById(contact.getcId()).get();

				// image..
				if (!file.isEmpty()) {
					// file work..
					// rewrite

//					delete old photo

					File deleteFile = new ClassPathResource("static/img").getFile();
					File file1 = new File(deleteFile, oldcontactDetail.getImage());
					file1.delete();

//					update new photo

					File saveFile = new ClassPathResource("static/img").getFile();

					Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

					Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

					contact.setImage(file.getOriginalFilename());

				} else {
					contact.setImage(oldcontactDetail.getImage());
				}

				User user = this.userRepository.getUserByUserName(principal.getName());

				contact.setUser(user);

				this.contactRepository.save(contact);

				session.setAttribute("message", new Message("Your contact is updated...", "success"));

			} catch (Exception e) {
				e.printStackTrace();
			}

			System.out.println("CONTACT NAME " + contact.getName());
			System.out.println("CONTACT ID " + contact.getcId());
			return "redirect:/user/" + contact.getcId() + "/contact";
		}
		
		
		// your profile handler
		@GetMapping("/profile")
		public String yourProfile(Model model) {
			model.addAttribute("title", "Profile Page");
			return "normal/profile";
		}


		
		//Open Setting Handler
		@GetMapping("/settings")
		public String openSetting() {
			
			return "normal/settings";
		}
		
		// Handler to change password
		@PostMapping("/change-password")
		public String changePassword(@RequestParam("oldPassword")String oldPassword,@RequestParam("newPassword")String newPassword,Principal principal,HttpSession session) {
			
			// System.out.println(oldPassword   );
			String name = principal.getName();
			User userByUserName = this.userRepository.getUserByUserName(name);
			
			if(this.bCryptPasswordEncoder.matches(oldPassword, userByUserName.getPassword())) {
				//set new passoword in encoded form and gives sucess msg with redirect to home
				userByUserName.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
				this.userRepository.save(userByUserName);
				session.setAttribute("message", new Message("Your password has been changed...", "success"));
			
			}
			else {
				// not match than give message and return same page
				session.setAttribute("message", new Message("Enter correct password...", "danger"));
				return"redirect:/user/settings";
			}
			
			return"redirect:/user/index";
			
		}
}
