package com.slavi.example.springBoot.example2.component;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.slavi.example.springBoot.example2.model.Calories;
import com.slavi.example.springBoot.example2.model.Role;
import com.slavi.parser.Filter;
import com.slavi.parser.WebFilterParser;

@RestController
@RequestMapping("/calories")
@Secured({"ROLE_USER", "ROLE_MANAGER", "ROLE_ADMIN"})
public class CaloriesController {
	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	@PersistenceContext
	protected EntityManager em;

	@Transactional
	@RequestMapping(value="{id}", method=RequestMethod.DELETE)
	public @ResponseBody ResponseEntity deleteItem(@PathVariable("id") int id) throws Exception {
		log.debug("Delete calories {}", id);
		Calories o = em.find(Calories.class, id);
		if (o == null)
			return ResponseEntity.notFound().build();

		if (Utils.userHasRole(Role.ROLE_ADMIN)) {
			// admin
		} else if (Utils.userHasRole(Role.ROLE_MANAGER)) {
			if (!Role.ROLE_USER.equals(o.getUser().getRole()) &&
				!Utils.getUsername().equals(o.getUser().getUsername())) {
				return ResponseEntity.status(403).build();
			}
		} else {
			if (!Utils.getUsername().equals(o.getUser().getUsername())) {
				return ResponseEntity.status(403).build();
			}
		}

		em.remove(o);
		return ResponseEntity.ok().build();
	}

	@Transactional
	@RequestMapping(value="", method=RequestMethod.POST)
	public ResponseEntity saveItem(
			@RequestBody @Valid Calories item, BindingResult result) throws Exception {
		log.debug("Save calories {}", item);
		if (result.hasErrors() || (item == null)) {
			return ResponseEntity.status(413).build();
		}

		if (em.getReference(Calories.class, item.getId()) == null) {
			em.persist(item);
		} else {
			item = em.merge(item);
		}
		return ResponseEntity.ok(item);
	}

	@RequestMapping(value="{id}", method=RequestMethod.GET)
	public ResponseEntity loadItem(@PathVariable("id") int id) throws Exception {
		log.debug("Get calories {}", id);
		Calories o = em.find(Calories.class, id);
		if (o == null)
			return ResponseEntity.status(404).build();
		BodyBuilder r = ResponseEntity.ok();
		return r.body(o);
	}

	@RequestMapping(value="", method=RequestMethod.GET)
	public @ResponseBody List<Calories> filterItems(Filter filter) throws Exception {
		log.debug("Filter calories {}", filter);
		WebFilterParser<Calories> parser = new WebFilterParser<>(em, Calories.class);
		if (Utils.userHasRole(Role.ROLE_ADMIN)) {
			// admin
		} else if (Utils.userHasRole(Role.ROLE_MANAGER)) {
			ArrayList params = new ArrayList();
			params.add(Utils.getUsername());
			params.add(Role.ROLE_MANAGER);
			parser.addQuery("user.username=? or user.role=?", params);
		} else {
			ArrayList params = new ArrayList();
			params.add(Utils.getUsername());
			parser.addQuery("user.username=?", params);
		}
		return parser.execute(filter);
	}
}
