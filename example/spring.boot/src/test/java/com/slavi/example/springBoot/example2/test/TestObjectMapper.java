package com.slavi.example.springBoot.example2.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slavi.example.springBoot.example2.Config;
import com.slavi.example.springBoot.example2.model.Department;
import com.slavi.example.springBoot.example2.model.DepartmentType;
import com.slavi.example.springBoot.example2.model.User;
import com.slavi.parser.PagedResult;

public class TestObjectMapper {

	void doIt() throws Exception {
		PagedResult<Department> r = new PagedResult<>();
		r.setPage(1);
		r.setSize(12);
		r.setHasNext(false);
		List<Department> items = new ArrayList<>();
		items.add(new Department("Department 1", DepartmentType.OFFICE));
		r.setItems(items);

//		ObjectMapper mapper = Config.prettyJsonObjectMapper();
		ObjectMapper mapper = Config.xmlObjectMapper();
		System.out.println(mapper.writeValueAsString(r));
	}

	void doIt2() throws Exception {
		Department d = new Department("Department 1", DepartmentType.OFFICE);
		User u = new User("u1", d);
		HashSet<User> subordinate = new HashSet<>();
		subordinate.add(new User("a1", d));
		u.setSubordinate(subordinate);

		ObjectMapper mapper = Config.prettyJsonObjectMapper();
		System.out.println(mapper.writeValueAsString(u));
	}

	public static void main(String[] args) throws Exception {
		new TestObjectMapper().doIt();
		System.out.println("Done.");
	}
}
