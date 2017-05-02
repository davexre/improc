package com.slavi.example.springBoot.example2;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.slavi.example.springBoot.example2.model.User;
import com.slavi.example.springBoot.example2.repository.UserRepository;

@RestController
public class MainController {
	@Autowired
	UserRepository userRepository;

	@Autowired
	MyDao myDao;

	@PostConstruct
	void initialize() {
		for (int i = 0; i < 10; i++)
			myDao.makeAndGet("user" + i);
	}


	@RequestMapping("/")
	@Transactional(readOnly=false)
	String home(@RequestParam(defaultValue="") String q) throws Exception {
		//User user;
		//userRepository.save(new Users("asd"));
		//user = userRepository.findOne("asd");
		//System.out.println(user.getUsername());
		//user = myDao.makeAndGet("asd");
		//System.out.println(user.getUsername());

		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(bo);
		out.println("Your query:");
		out.println(q);
		//String queryStr = "(username=\"user1\")";
		for (User u : myDao.queryUser(q)) {
			out.println(u.getUsername());
		}

		return bo.toString();
	}
}
