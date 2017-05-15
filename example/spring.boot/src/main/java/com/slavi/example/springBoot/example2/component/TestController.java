package com.slavi.example.springBoot.example2.component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.slavi.example.springBoot.example2.model.Role;
import com.slavi.example.springBoot.example2.model.User;
import com.slavi.parser.Filter;
import com.slavi.parser.PagedResult;
import com.slavi.parser.WebFilterParser;

@RestController
@RequestMapping("/test")
public class TestController {
	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	@PersistenceContext
	protected EntityManager em;

	@Autowired
	Dao dao;

	@RequestMapping(value="{id}", method=RequestMethod.GET)
	public ResponseEntity loadItem(@PathVariable("id") int id) throws Exception {
		log.debug("Get user {}", id);
		User o = em.find(User.class, id);
		if (o == null)
			return ResponseEntity.status(404).build();
		return ResponseEntity.ok().body(o);
	}

	@RequestMapping(value="/users", method=RequestMethod.GET)
	public @ResponseBody PagedResult<User> filterItems(Filter filter) throws Exception {
		log.debug("Test {}", filter);
		WebFilterParser<User> parser = new WebFilterParser<>(em, User.class);
		if (!Utils.userHasRole(Role.ROLE_ADMIN))
			parser.addQuery("role=ROLE_USER", null);
		return parser.execute(filter);
	}
}
