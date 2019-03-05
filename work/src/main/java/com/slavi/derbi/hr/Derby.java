package com.slavi.derbi.hr;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Properties;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.sql.DataSource;
import javax.transaction.Transactional;

import org.apache.commons.beanutils.BeanUtilsBean2;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.support.ClasspathScanningPersistenceUnitPostProcessor;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.slavi.dbutil.MyDbScriptRunner;
import com.slavi.dbutil.ResultSetToString;
import com.slavi.derbi.hr.model.EmployeeDetails;
import com.slavi.jdbcspy.SpyDataSource;

@Component
@ComponentScan
@EnableTransactionManagement
@Configuration
public class Derby {

	public static DataSource generateDb() throws Exception {
		EmbeddedDataSource ds = new EmbeddedDataSource();
		ds.setDatabaseName("memory:hr;create=true");
		try (Connection conn = ds.getConnection()) {
			MyDbScriptRunner sr = new MyDbScriptRunner(conn);
			sr.process(Derby.class.getResourceAsStream("Derby_create_schema.sql.txt"));
		}
		return ds;
	}

	@Bean
	public static DataSource dataSource() throws Exception {
//		return generateDb();
		return new SpyDataSource(generateDb());
	}
	@Bean(name = "entityManagerFactory")
	public static LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
		LocalContainerEntityManagerFactoryBean r = new LocalContainerEntityManagerFactoryBean();
		r.setDataSource(dataSource);
		r.setPackagesToScan(Derby.class.getPackageName());
		r.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
		ClasspathScanningPersistenceUnitPostProcessor pp = new ClasspathScanningPersistenceUnitPostProcessor(Derby.class.getPackageName());
		r.setPersistenceUnitPostProcessors(pp);

		Properties prop = new Properties();
		prop.setProperty("javax.persistence.schema-generation.database.action", "none");
		prop.setProperty("javax.persistence.schema-generation.scripts.action", "drop-and-create");
		prop.setProperty("javax.persistence.schema-generation.scripts.create-target", "target/createDDL_ddlGeneration.sql");
		prop.setProperty("javax.persistence.schema-generation.scripts.drop-target", "target/dropDDL_ddlGeneration.sql");
		r.setJpaProperties(prop);
		return r;
	}

	@Bean(name = "transactionManager")
	public static JpaTransactionManager transactionManager(EntityManagerFactory emf) {
		JpaTransactionManager r = new JpaTransactionManager();
		r.setEntityManagerFactory(emf);
		return r;
	}

	@Autowired
	DataSource dataSource;

	@PersistenceContext
	EntityManager em;

	@Transactional
	public void loadAllEntities() throws Exception {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		Reflections rr = new Reflections(getClass().getPackageName());
		BeanUtilsBean2 bu = new BeanUtilsBean2();
		for (Class clazz : rr.getTypesAnnotatedWith(Entity.class)) {
			System.out.println("Loading entity from class: " + clazz.getCanonicalName());
			CriteriaQuery cq = cb.createQuery();
			Root root = cq.from(clazz);
			cq.select(root);
			Query q = em.createQuery(cq);
			q.setMaxResults(3);
			List res = q.getResultList();
			if (res.size() > 0) {
				BeanInfo bi = Introspector.getBeanInfo(clazz);
				Object o = res.get(0);
				for (PropertyDescriptor pd : bi.getPropertyDescriptors()) {
					bu.getProperty(o, pd.getName());
				}
			}
		}
	}


	@Transactional
	public void doIt() throws Exception {
		SharedSessionContractImplementor session = em.unwrap(SharedSessionContractImplementor.class);
		Connection conn = session.connection();
		try (PreparedStatement st = conn.prepareStatement("select * from emp_details_view fetch first 3 rows only")) {
			System.out.println(ResultSetToString.resultSetToString(st.executeQuery()));
		}

		TypedQuery<EmployeeDetails> tq = em.createQuery("select e from EmployeeDetails e", EmployeeDetails.class);
		tq.setMaxResults(1);
		List<EmployeeDetails> r = tq.getResultList();
		System.out.println(r.get(0).getLastName());
		System.out.println("--------");
		System.out.println(r.get(0).getEmployee().getId());
		System.out.println("--------");
		System.out.println(r.get(0).getEmployee().getFirstName());
		System.out.println("--------");
		System.out.println(r.get(0).getEmployee().getJob().getTitle());
	}

	public static void main(String[] args) throws Exception {
		new FileOutputStream("target/createDDL_ddlGeneration.sql").close();
		new FileOutputStream("target/dropDDL_ddlGeneration.sql").close();
		try (AnnotationConfigApplicationContext ctx =  new AnnotationConfigApplicationContext(Derby.class)) {
			ctx.getBean(Derby.class).loadAllEntities();
		};
		System.out.println("Done.");
	}
}
