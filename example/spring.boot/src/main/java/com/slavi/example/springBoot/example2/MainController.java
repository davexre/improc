package com.slavi.example.springBoot.example2;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.slavi.example.springBoot.example2.model.Department;
import com.slavi.example.springBoot.example2.model.User;
import com.slavi.example.springBoot.example2.repository.DepartmentRepository;
import com.slavi.example.springBoot.example2.repository.UserRepository;

@RestController
public class MainController {

	@Autowired
	MyDao myDao;

	@Autowired
	DepartmentRepository departmentRepository;

	@Autowired
	UserRepository userRepository;

	@PostConstruct
	void initialize() throws Exception {
		for (int i = 0; i < 5; i++)
			departmentRepository.save(new Department("Department " + i));
		List<Department> deparments = departmentRepository.findAll();
		for (int i = 0; i < 20; i++)
			userRepository.save(new User("User " + i, deparments.get(i % deparments.size())));
		List<User> users = myDao.dummy();
		for (User u : users)
			System.out.println(u);
	}


	@RequestMapping(path="/", produces={ MediaType.TEXT_PLAIN_VALUE })
	@Transactional(readOnly=false)
	@ResponseBody
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
			out.println(u);
		}

		return bo.toString();
	}
}
