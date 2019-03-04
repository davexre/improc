package com.slavi.derbi.jpa;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.sql.DataSource;
import javax.transaction.Transactional;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.io.DatabaseIO;
import org.apache.ddlutils.model.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Propagation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slavi.derbi.jpa.entity.DateStyle;
import com.slavi.derbi.jpa.entity.Department;
import com.slavi.derbi.jpa.entity.DepartmentType;
import com.slavi.derbi.jpa.entity.EntityWithDate;
import com.slavi.derbi.jpa.entity.MyEntity;
import com.slavi.derbi.jpa.entity.MyEntityPartial;
import com.slavi.derbi.jpa.entity.Role;
import com.slavi.derbi.jpa.entity.User;
import com.slavi.derbi.jpa.repository.MyRepo;
import com.slavi.derbi.jpa.repository.UserRepository;
import com.slavi.util.StringPrintStream;

@Component
@ComponentScan
@EnableTransactionManagement
@EnableJpaRepositories(namedQueriesLocation="classpath:/com/slavi/derbi/jpa/JpaCreate-jpa-named-queries.properties")
@ImportResource("/com/slavi/derbi/jpa/JpaCreate-sping.xml")
public class JpaCreate {

	@Autowired
	DataSource dataSource;

	@Autowired
	MyRepo userRepository;

	@Transactional
	public String dbSomething2() throws SQLException, IOException {
		EntityGraph graph = this.em.getEntityGraph("manager.subordinate");
		System.out.println("-------------");
//		TypedQuery<User> q = this.em.createQuery("select distinct u from User u where u.username = :un", User.class).setParameter("un", "User 0");
		TypedQuery<User> q = this.em.createQuery("select u from User u join fetch u.subordinate s where u.username = :un", User.class).setParameter("un", "User 0");
//		q.setHint("javax.persistence.loadgraph", graph);
//		q.setHint("javax.persistence.fetchgraph", graph);
		List<User> users = q.getResultList();
		System.out.println("-------------");
		for (User u : users)
			System.out.println(u.getUsername() + " " + u.getSubordinate().size());
		return "";
	}

	@Transactional
	public String dbSomething() throws SQLException, IOException {
		{
			User manager = em.find(User.class, "User 0");
			//System.out.println(manager.params2.entrySet().iterator().next().getValue());
/*
			String jpql = "select u from User u join u.params2 p where key(p) = :s";
			TypedQuery<User> q = em.createQuery(jpql, User.class);
			User s = new User("s", null);
			q.setParameter("s", s);
			System.out.println(q.getResultList().size());

			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<User> cq = cb.createQuery(User.class);
			Root<User> root = cq.from(User.class);
			MapJoin j = root.joinMap("params2");
			cq.where(cb.equal(j.key(), s));
			q = em.createQuery(cq);
			System.out.println(q.getResultList().size());*/
		}
		if (true)
			return "";

//		String jpql = "select u from AdvancedUser u where u.someInt = 12";
//		String jpql = "select distinct u from User u join u.subordinate s where s.name = 'aaa'";
//		String jpql = "select u from MyEntity u where u.data like 'Data %'";
		String jpql = "select u from User u where :s member of u.subordinate";
		TypedQuery<User> q = em.createQuery(jpql, User.class);
		User s = new User("s", null);
		q.setParameter("s", s);
		System.out.println(q.getResultList().size());
/*
		jpql = "select u from User u left join u.subordinate s where s.name is null";
		q = em.createQuery(jpql, User.class);
		//q.setParameter("s", s);
		System.out.println(q.getResultList().size());*/


		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<User> cq = cb.createQuery(User.class);
		Root<User> root = cq.from(User.class);
		cq.where(cb.isNotEmpty(root.get("subordinate")));
/*		Join sub = root.join("subordinate");
//		cq.where(cb.equal(sub, s));
		ArrayList lst = new ArrayList<>();
		lst.add(s);
		cq.where(sub.in(lst));
		cq.select(cb.count(root));*/
		System.out.println(em.createQuery(cq).getResultList().size());

/*
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<User> root = cq.from(User.class);
		Join j = root.join("subordinate");
		cq.where(
				cb.equal(j.get("name"), "asd")
//				cb.equal(root.get("subordinate"), new User("asd", null))
				);
		cq.select(cb.count(root));
		System.out.println(em.createQuery(cq).getResultList().get(0));	*/


//		System.out.println(userRepository.findAllOrdered().size());
//		System.out.println("------------- " + em.createNamedQuery("User.countAll").getResultList().size());
//		System.out.println("------------- " + em.createNamedQuery("User.findAllOrdered").getResultList().size());
		StringPrintStream out = new StringPrintStream();

/*
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery qry = builder.createQuery(User.class);
		Root<User> ru = qry.from(User.class);
		qry.where(builder.and(
				builder.equal(ru.get("manager"), new User()),
				builder.equal(ru.get("role"), Role.ADMIN))
		);

		//qry.orderBy(builder.desc(ru.get("username")));
		em.createQuery(qry).getResultList();

		dbToXml(out);*/
		return out.toString();
	}

	public void dbToXml(OutputStream out) throws SQLException, IOException {
		Connection conn = dataSource.getConnection();
		try {
			Platform  platform = PlatformFactory.createNewPlatformInstance(dataSource);
			Database db = platform.readModelFromDatabase(conn, "test");
			DatabaseIO dbio = new DatabaseIO();
			Writer wr = new OutputStreamWriter(out);
			dbio.write(db, wr);
			wr.close();
		} finally {
			DbUtils.close(conn);
			IOUtils.closeQuietly(out);
		}
	}

	public void createDummyTableWithData(Connection connection) throws SQLException {
		Statement st = connection.createStatement();
		st.execute("create table asd(a int, b char(10))");
		st.execute("insert into asd(a,b) values(1,'123')");
		connection.commit();
	}

	@PersistenceContext
	EntityManager em;

	//@Transactional
	@org.springframework.transaction.annotation.Transactional(propagation=Propagation.REQUIRES_NEW)
	public void initialize() throws Exception {
		DepartmentType types[] = DepartmentType.values();
		for (int i = 0; i < 5; i++)
			em.persist(new Department("Department " + i, types[i % types.length]));
		List<Department> deparments = em.createQuery("select d from Department d", Department.class).getResultList();
		User manager = null;
		for (int i = 0; i < 20; i++) {
			User user = new User("User " + i, deparments.get(i % deparments.size()));
			//User user = new AdvancedUser();
			//user.setUsername("User " + i);
			//user.setDepartment(deparments.get(i % deparments.size()));
			if (i % 5 == 0) {
				user.setRole(Role.MANAGER);
				manager = user;
			} else {
				user.setManager(manager);
				manager.getSubordinate().add(user);
			}
			user.params2.put("user", null);

			em.persist(user);
/*			if (i % 5 == 0) {
				manager = em.find(User.class, user.getUsername());
			}
*/
			MyEntity e = new MyEntity(Integer.toString(i), "Data " + i);
			System.out.println(e);
			em.persist(e);
		}
	}

	@Transactional
	public void createORMs() throws Exception {
		User manager = em.find(User.class, "User 0");
		System.out.println(manager);
		User user = em.find(User.class, "User 1");
		System.out.println(user);

		EntityType<User> etype = em.getMetamodel().entity(User.class);

		for (Attribute attribute : etype.getAttributes()) {
			//ManagedType<User> mtype = attribute.getDeclaringType();
			System.out.println(attribute);
			System.out.println(attribute.isAssociation());
		}
		String paramVal = DepartmentType.FACTORY.name();
		//String paramName = "department.type";
		String queryStr = "select distinct u from User u join u.subordinate subordinate join subordinate.department department where department.type=?1";
		Object o = paramVal;
		Attribute attribute = etype.getAttribute("department");
		Class clazz = attribute.getJavaType();
		if (attribute.isAssociation()) {
		} else {
			if (clazz.isEnum()) {
				o = Enum.valueOf(clazz, (String) o);
			} else if (Long.class.isAssignableFrom(clazz)) {
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

		}
		TypedQuery<User> query = em.createQuery(queryStr, User.class);
		query.setParameter(1, DepartmentType.OFFICE);

		System.out.println("\n\n--------------------");
		System.out.println(queryStr);
		List<User> users = query.getResultList();
		for (User u : users) {
			System.out.println(u);
		}

		//EntityType<User> etype = em.getMetamodel().entity(User.class);
/*		IdentifiableType itype = etype.getSupertype();
		for (Object a : itype.getAttributes())
			System.out.println(a);
	*/
		if (true)
			return;

		DateStyle ds = new DateStyle();
		ds.setFormat("dd.mm.yyyy");
		ds = em.merge(ds);

		EntityWithDate ed = new EntityWithDate();
		ed.setData("My data");
		ed.setDateIdRef(ds.getDateId());
		ed = em.merge(ed);

		int edId = ed.getEntityWithDateId();

		ed = em.find(EntityWithDate.class, edId);
		ObjectMapper mapper = new ObjectMapper();

		StringPrintStream out = new StringPrintStream();
		mapper.writeValue(out, ed);
		System.out.println(out.toString());

		System.out.println("---------------------");
		for (int i = 0; i < 10; i++) {
			MyEntity ent = new MyEntity(Integer.toString(i), "Data  for entity No " + i);
			ent.setData1("Data1 for entity No " + i);
			ent.setData2("Data2 for entity No " + i);
			em.merge(ent);
		}
		Query q = em.createQuery("select e from MyEntityPartial e where e.id = 1", MyEntityPartial.class);
		//em.getCriteriaBuilder().

		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery qry = builder.createQuery(User.class);
		Root<User> ru = qry.from(User.class);
		qry.where(builder.and(
				builder.equal(ru.get("manager"), new User()),
				builder.equal(ru.get("uuuasd"), Role.ADMIN))
		);

		qry.orderBy(builder.asc(ru.get("id")));
		em.createQuery(qry).getResultList();

		List<MyEntityPartial> r = q.getResultList();
		for (MyEntityPartial i : r) {
			System.out.println(i);
		}
		System.out.println("---------------------");
	}

	public static void main(String[] args) throws Exception {
		new FileOutputStream("target/createDDL_ddlGeneration.sql").close();
		new FileOutputStream("target/dropDDL_ddlGeneration.sql").close();

		System.setProperty("derby.stream.error.method", "com.slavi.dbutil.DerbyLogOverSlf4j.getLogger");
		//ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext("JpaCreate-sping.xml", JpaCreate.class);
		AnnotationConfigApplicationContext appContext = new AnnotationConfigApplicationContext(JpaCreate.class);

		JpaCreate bean = appContext.getBean(JpaCreate.class);
		bean.initialize();
//		bean.createORMs();
		bean.dbSomething2();
		appContext.close();
		System.out.println("Done.");
	}
}
