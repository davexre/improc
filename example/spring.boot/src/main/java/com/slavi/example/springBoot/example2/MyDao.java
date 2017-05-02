package com.slavi.example.springBoot.example2;

import java.io.StringReader;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.slavi.example.springBoot.example2.model.User;
import com.slavi.example.springBoot.example2.repository.UserRepository;
import com.slavi.parser.MyParser;
import com.slavi.parser.ParseException;

@Component
public class MyDao {
	@Autowired
	DataSource dataSource;

	@PersistenceContext
	EntityManager em;

	@Autowired
	UserRepository userRepository;

	@Transactional(readOnly=false)
	public User makeAndGet(String name) {
		em.persist(new User(name));
		return em.find(User.class, name);
	}

	public List<User> queryUser(String queryStr) throws ParseException {
		MyParser parser = new MyParser(new StringReader(queryStr));
		parser.fieldPrefix = "u.";
		parser.parse();

		String q = parser.sb.toString();
		q = "select u from User u" + (StringUtils.isEmpty(q) ? "" : " where (" + parser.sb.toString() + ")");
		System.out.println(q);
		TypedQuery<User> query = em.createQuery(q, User.class);
		//EntityType etype = em.getMetamodel().entity(User.class);

		for (int i = 0; i < parser.paramVals.size(); i++) {
			//String paramName = (String) parser.paramNames.get(i);
			//Attribute a = etype.getAttribute(paramName);
			query.setParameter(i + 1, parser.paramVals.get(i));
			System.out.println(i + " -> " + parser.paramVals.get(i));
		}

//		TypedQuery<User> query = em.createQuery("select u from User u where someInt = 12", User.class);
		return query.getResultList();
	}
}
