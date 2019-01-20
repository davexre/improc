package com.slavi.example.springBoot.example3.component;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.slavi.example.springBoot.example3.model.Department;
import com.slavi.example.springBoot.example3.model.Role;
import com.slavi.example.springBoot.example3.model.User;

@Component
public class Dao {
	@PersistenceContext
	EntityManager em;

	@Autowired
	DataSource dataSource;

	@Transactional(readOnly=false)
	public void populateInitialData() throws Exception {
		for (int i = 0; i < 5; i++)
			em.persist(new Department("Department " + i));
		List<Department> deparments = em.createQuery("select d from Department d", Department.class).getResultList();
		User manager = null;
		for (int i = 0; i < 20; i++) {
			User user = new User("User " + i, deparments.get(i % deparments.size()));
			if (i % 5 == 0) {
				user.setRole(Role.MANAGER);
			} else
				user.setManager(manager);
			em.persist(user);
			if (i % 5 == 0) {
				manager = em.find(User.class, user.getUsername());
			}
		}
	}

	@javax.transaction.Transactional
	public void dummy() {
		User u = em.find(User.class, "User 1");
		em.detach(u);
		System.out.println(u.getManager());
	}
}
