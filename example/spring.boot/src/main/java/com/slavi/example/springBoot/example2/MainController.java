package com.slavi.example.springBoot.example2;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.slavi.example.springBoot.example2.model.Department;
import com.slavi.example.springBoot.example2.model.DepartmentType;
import com.slavi.example.springBoot.example2.model.Role;
import com.slavi.example.springBoot.example2.model.User;
import com.slavi.example.springBoot.example2.repository.DepartmentRepository;
import com.slavi.example.springBoot.example2.repository.UserRepository;
import com.slavi.parser.Paging;

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
		myDao.initialize();
	}

	@RequestMapping(path="/", produces={ MediaType.TEXT_PLAIN_VALUE })
	@Transactional(readOnly=false)
	@ResponseBody
	String home(@RequestParam(defaultValue="") String q) throws Exception {
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(bo);
		out.println("Your query:");
		out.println(q);
		for (User u : myDao.queryUser(q)) {
			out.println(u);
			out.println();
		}
		return bo.toString();
	}

	@RequestMapping(path="/dummy", produces={ MediaType.TEXT_PLAIN_VALUE })
	@Transactional(readOnly=false)
	@ResponseBody
	String dummy(Paging page) throws Exception {
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(bo);
		out.println("DUMMY");
		
		out.println("Got page = " + page);
		
		return bo.toString();
		
	}
}
