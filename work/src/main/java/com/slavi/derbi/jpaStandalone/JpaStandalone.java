package com.slavi.derbi.jpaStandalone;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.sql.DataSource;

import org.apache.derby.jdbc.EmbeddedDataSource40;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

/**
 * Demonstrates a BUG in eclipselink sql generation
 */

@Component
@Configuration
@EnableTransactionManagement
public class JpaStandalone {

	@Bean
	public static DataSource dataSource() {
		EmbeddedDataSource40 ds = new EmbeddedDataSource40();
		ds.setDatabaseName("memory:test");
		ds.setCreateDatabase("create");
		return ds;
	}

	@Bean
	public static LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource ds) {
		LocalContainerEntityManagerFactoryBean r = new LocalContainerEntityManagerFactoryBean();
		r.setDataSource(ds);
		r.setPackagesToScan(JpaStandalone.class.getPackage().getName());
		Properties p = new Properties();

		if (false) {
			r.setJpaVendorAdapter(new EclipseLinkJpaVendorAdapter());
			p.put("eclipselink.weaving", "false");
			p.put("eclipselink.logging.level.sql", "fine");
			p.put("eclipselink.logging.parameters", "true");
			p.put("eclipselink.ddl-generation", "create-tables");
		} else {
			r.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
			p.put("hibernate.show_sql", "true");
			p.put("hibernate.format_sql", "true");
			p.put("hibernate.use_sql_comments", "true");
			p.put("hibernate.hbm2ddl.auto", "create-only");
			p.put("hibernate.dialect", "org.hibernate.dialect.DerbyTenSevenDialect");
		}

		r.setJpaProperties(p);
		return r;
	}

	@Bean
	public static PlatformTransactionManager transactionManager(DataSource ds, EntityManagerFactory emf) {
		JpaTransactionManager r = new JpaTransactionManager();
		r.setDataSource(ds);
		r.setEntityManagerFactory(emf);
		return r;
	}

	@Entity(name = "BaseEntity")
	@Inheritance(strategy = InheritanceType.JOINED)
	public static class BaseEntity {
		@Id @GeneratedValue
		public int id;
	}

	@Entity(name = "MyKey")
	public static class MyKey extends BaseEntity {
		public String myKeyData;
	}

	@Entity(name = "MyData")
	public static class MyData {
		@Id @GeneratedValue
		public int id;

		@ElementCollection
		public Map<MyKey, String> aMap = new HashMap<>();
	}

	@PersistenceContext
	EntityManager em;

	@Transactional
	public void badSqlGeneratedInEclipseLink() {
		TypedQuery<MyData> q;
		if (true) {
			MyKey k = new MyKey();
			em.persist(k);
			q = em.createQuery("select d from MyData d join d.aMap m where key(m) = :aKey", MyData.class);
			q.setParameter("aKey", k);
		} else {
			q = em.createQuery("select d from MyData d join d.aMap m where m like 'asd%'", MyData.class);
		}
		System.out.println(q.getResultList().size());
	}

	public static void main(String[] args) throws Exception {
		try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(JpaStandalone.class)) {
			ctx.getBean(JpaStandalone.class).badSqlGeneratedInEclipseLink();
		}
	}
}
