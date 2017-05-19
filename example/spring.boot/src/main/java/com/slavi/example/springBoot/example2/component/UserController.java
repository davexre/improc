package com.slavi.example.springBoot.example2.component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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
@RequestMapping("/users")
//@Secured({ "ROLE_MANAGER", "ROLE_ADMIN"})
public class UserController {
	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	@PersistenceContext
	protected EntityManager em;

	@Transactional
	@RequestMapping(value="{id}", method=RequestMethod.DELETE)
	public ResponseEntity deleteItem(@PathVariable("id") String id) throws Exception {
		log.debug("Delete user {}", id);
		User o = em.find(User.class, id);
		if (o == null)
			return ResponseEntity.notFound().build();

		if (!Utils.userHasRole(Role.ROLE_ADMIN) && (!Role.ROLE_USER.equals(o.getRole())))
			return ResponseEntity.status(403).build();
		em.remove(o);
		return ResponseEntity.ok().build();
	}

	@Transactional
	@RequestMapping(value="", method=RequestMethod.POST)
	public ResponseEntity saveItem(
			@RequestBody @Valid User item, BindingResult result) throws Exception {
		log.debug("Save user {}", item);
		if (result.hasErrors() || (item == null)) {
			return ResponseEntity.status(413).build();
		}

		User o = em.find(User.class, item.getUsername());
		if (!Utils.userHasRole(Role.ROLE_ADMIN) &&
			(o == null || !Role.ROLE_USER.equals(o.getRole()) || !Role.ROLE_USER.equals(item.getRole())))
			return ResponseEntity.status(403).build();

		if (o == null) {
			em.persist(item);
		} else {
			item = em.merge(item);
		}
		return ResponseEntity.ok(item);
	}

	@RequestMapping(value="{id}", method=RequestMethod.GET)
	public ResponseEntity loadItem(@PathVariable("id") String id) throws Exception {
		log.debug("Get user {}", id);
		User o = em.find(User.class, id);
		if (o == null)
			return ResponseEntity.status(404).build();
		return ResponseEntity.ok().body(o);
	}

	@RequestMapping(value="", method=RequestMethod.GET)
	public @ResponseBody PagedResult<User> filterItems(Filter filter) throws Exception {
		log.debug("Filter users {}", filter);
		WebFilterParser<User> parser = new WebFilterParser<>(em, User.class);
		if (!Utils.userHasRole(Role.ROLE_ADMIN))
			parser.addQuery("role=ROLE_USER", null);
		return parser.execute(filter);
	}
}
