package com.slavi.example.springBoot.example2;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.slavi.dbutil.ResultSetToString;
import com.slavi.example.springBoot.example2.model.User;
import com.slavi.example.springBoot.example2.repository.UserRepository;
import com.slavi.parser.MyParser;

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
		em.persist(new User(name, null));
		return em.find(User.class, name);
	}

	public List<User> dummy() throws Exception {
		TypedQuery<User> query = em.createQuery("select u from User u left join u.department department where department.name = 'Department 1'", User.class);
		return query.getResultList();
	}

	public List<User> queryUser(String queryStr) throws Exception {
		MyParser parser = new MyParser(new StringReader(queryStr));
		parser.fieldPrefix = "u.";
		parser.parse();

		try (Connection conn = dataSource.getConnection()) {
			PreparedStatement ps = conn.prepareStatement("select * from users where enabled = ?");
			ps.setObject(1, "true");
			ResultSet rs = ps.executeQuery();
			ResultSetToString rss = new ResultSetToString();
			System.out.println(rss.resultSetToString(rs));
		}




		String q = parser.sb.toString();
		q = "select u from User u" + (StringUtils.isEmpty(q) ? "" : " where (" + parser.sb.toString() + ")");
		System.out.println(q);
		TypedQuery<User> query = em.createQuery(q, User.class);
		EntityType etype = em.getMetamodel().entity(User.class);

		for (int i = 0; i < parser.paramVals.size(); i++) {
			String paramName = (String) parser.paramNames.get(i);
			Attribute a = etype.getAttribute(paramName);
			Object o = parser.paramVals.get(i);
			Class clazz = a.getJavaType();
			if (Long.class.isAssignableFrom(clazz)) {
				o = Long.parseLong((String) o);
			} else if (Double.class.isAssignableFrom(clazz)) {
				o = Double.parseDouble((String) o);
			} else if (Date.class.isAssignableFrom(clazz)) {
				o = DateUtils.parseDate((String) o, new String[] {
						"yyyy",
						"yyyy-MM",
						"yyyy-MM-dd",
						"yyyy-MM-dd'T'HH",
						"yyyy-MM-dd'T'HH:mm",
						"yyyy-MM-dd'T'HH:mm:ss",
						"yyyy-MM-dd'T'HH:mm:ss.SSS",
						"yyyy-MM-dd'T'HH:mm:ss.SSSZ",
				});
			} else if (Boolean.class.isAssignableFrom(clazz)) {
				o = Boolean.parseBoolean((String) o);
			}

			query.setParameter(i + 1, o);
			System.out.println(i + " -> " + o);
		}

		//TypedQuery<User> query = em.createQuery("select u from User u where u.department.name = 1", User.class);
		return query.getResultList();
	}
}
