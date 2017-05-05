package com.slavi.example.springBoot.example2;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.slavi.example.springBoot.example2.model.User;
import com.slavi.example.springBoot.example2.repository.DepartmentRepository;
import com.slavi.example.springBoot.example2.repository.UserRepository;
import com.slavi.parser.Filter;

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
	@ResponseBody
	String home(@RequestParam(defaultValue="") String q, Filter paging) throws Exception {
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(bo);
		out.println("Your query:");
		out.println(q);
		for (User u : myDao.queryUser(q, paging)) {
			out.println(u);
			out.println();
		}
		return bo.toString();
	}

	@RequestMapping(path="/q")
	@ResponseBody
	List q(@RequestParam(defaultValue="") String q, Filter paging) throws Exception {
		List<User> r = myDao.queryUser(q, paging);
//		if (r.size() > 0)
//			System.out.println("\n\n\n---------------- " + r.get(0).getSubordinate().size());
		return r;
	}

	@RequestMapping(path="/dummy")
	@ResponseBody
	Map dummy(Filter filter) throws Exception {
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(bo);
		out.println("DUMMY");

		out.println("Got filter = " + filter);

		Map map = new HashMap();
		map.put("response", bo.toString());
		return map;
	}
}
