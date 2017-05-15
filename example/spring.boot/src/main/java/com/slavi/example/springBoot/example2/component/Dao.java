package com.slavi.example.springBoot.example2.component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.slavi.dbutil.ResultSetToString;
import com.slavi.example.springBoot.example2.model.Department;
import com.slavi.example.springBoot.example2.model.DepartmentType;
import com.slavi.example.springBoot.example2.model.Role;
import com.slavi.example.springBoot.example2.model.User;
import com.slavi.example.springBoot.example2.repository.UserRepository;
import com.slavi.parser.Filter;
import com.slavi.parser.PagedResult;
import com.slavi.parser.ParseException;
import com.slavi.parser.WebFilterParser;

@Component
public class Dao {
	@Autowired
	DataSource dataSource;

	@PersistenceContext
	EntityManager em;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	UserRepository userRepository;

	@Transactional
	public void populateInitialData() {
		TypedQuery<Long> q = em.createQuery("select count(*) from User u where u.role=?1", Long.class);
		q.setParameter(1, Role.ROLE_ADMIN);
		if (q.getSingleResult() == 0) {
			User admin = new User();
			admin.setUsername("admin");
			admin.setPassword(passwordEncoder.encode("admin"));
			admin.setDisplayName("administrator");
			admin.setRole(Role.ROLE_ADMIN);
			admin.setEnabled(true);
			em.persist(admin);
		}
	}

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
				user.setRole(Role.ROLE_MANAGER);
			} else
				user.setManager(manager);
			em.persist(user);
			if (i % 5 == 0) {
				manager = em.find(User.class, user.getUsername());
			}
		}
	}

	public void test() throws Exception {
		ResultSetToString rss = new ResultSetToString();
		try (Connection conn = dataSource.getConnection()) {
			String sql = "select username, password, 1 from users where username=?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, "admin");
			System.out.println(rss.resultSetToString(ps.executeQuery()));
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

	public PagedResult<User> filterUsers(Filter filter) throws ParseException {
		WebFilterParser<User> parser = new WebFilterParser<>(em, User.class);
		return parser.execute(filter);
	}

	public PagedResult<User> queryUser(String queryStr, Filter paging) throws Exception {
		WebFilterParser<User> parser = new WebFilterParser(em, User.class);
		parser.addQuery(queryStr, null);
		return parser.execute(paging);

/*		try (Connection conn = dataSource.getConnection()) {
			PreparedStatement ps = conn.prepareStatement("select * from users where enabled = ?");
			ps.setObject(1, "true");
			ResultSet rs = ps.executeQuery();
			ResultSetToString rss = new ResultSetToString();
			System.out.println(rss.resultSetToString(rs));
		}
*/
		//TypedQuery<User> query = em.createQuery("select u from User u where u.department.name = 1", User.class);
		//return query.getResultList();
	}
}
