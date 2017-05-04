package com.slavi.example.springBoot.example2;

import java.io.StringReader;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.EntityType;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.slavi.example.springBoot.example2.model.Department;
import com.slavi.example.springBoot.example2.model.DepartmentType;
import com.slavi.example.springBoot.example2.model.Role;
import com.slavi.example.springBoot.example2.model.User;
import com.slavi.example.springBoot.example2.repository.UserRepository;
import com.slavi.parser.MyParser2;
import com.slavi.parser.ParserHelper;

@Component
public class MyDao {
	@Autowired
	DataSource dataSource;

	@PersistenceContext
	EntityManager em;

	@Autowired
	UserRepository userRepository;

	@Transactional(readOnly=false)
	public void initialize() throws Exception {
		DepartmentType types[] = DepartmentType.values();
		for (int i = 0; i < 5; i++)
			em.persist(new Department("Department " + i, types[i % types.length]));
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
		EntityType rootType = em.getMetamodel().entity(User.class);
		MyParser2 parser = new MyParser2(new StringReader(queryStr));
		parser.helper = new ParserHelper(em, rootType);
		parser.parse();

/*		try (Connection conn = dataSource.getConnection()) {
			PreparedStatement ps = conn.prepareStatement("select * from users where enabled = ?");
			ps.setObject(1, "true");
			ResultSet rs = ps.executeQuery();
			ResultSetToString rss = new ResultSetToString();
			System.out.println(rss.resultSetToString(rs));
		}
*/

		String q = parser.helper.query.toString();
		q = parser.helper.sql.toString() + (StringUtils.isEmpty(q) ? "" : " where " + q);
		System.out.println(q);

		TypedQuery<User> query = em.createQuery(q, User.class);
		for (int i = 0; i < parser.helper.paramVals.size(); i++)
			query.setParameter(i + 1, parser.helper.paramVals.get(i));

		//TypedQuery<User> query = em.createQuery("select u from User u where u.department.name = 1", User.class);
		return query.getResultList();
	}
}
