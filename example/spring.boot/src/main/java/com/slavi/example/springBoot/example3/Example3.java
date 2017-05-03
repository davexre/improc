package com.slavi.example.springBoot.example3;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan
@EnableTransactionManagement
@EnableJpaRepositories
@AutoConfigureDataJpa
@ImportAutoConfiguration
@Import({ DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
public class Example3 {

	static final Class<Example3> clazz = Example3.class;
	static Logger log = LoggerFactory.getLogger(clazz);
	static Properties appProperties = new Properties();
	
	static {
		try (InputStream is = clazz.getResourceAsStream(clazz.getName() + ".properties")) {
			if (is != null)
				appProperties.load(new InputStreamReader(is));
		} catch (Exception e) {
			throw new Error(e);
		}
	}
	
	@Autowired
	DataSource dataSource;
	
	@PersistenceContext
	EntityManager em;
/*	
	@Bean
	static LocalContainerEntityManagerFactoryBean getEntityManagerFactory(@Autowired DataSource dataSource) {
		LocalContainerEntityManagerFactoryBean r = new LocalContainerEntityManagerFactoryBean();
		r.setDataSource(dataSource);
		r.setPackagesToScan(clazz.getPackage().getName());
		//EclipseLinkJpaVendorAdapter a = new EclipseLinkJpaVendorAdapter();
		HibernateJpaVendorAdapter a = new HibernateJpaVendorAdapter();
		a.setShowSql(true);
		r.setJpaVendorAdapter(a);
		r.setJpaProperties(appProperties);
		return r;
	}
/*	
	@Bean
	static PlatformTransactionManager getTransactionManager(@Autowired DataSource dataSource, @Autowired EntityManagerFactory emf) {
		JpaTransactionManager r = new JpaTransactionManager();
		r.setDataSource(dataSource);
		r.setEntityManagerFactory(emf);
		return r;
	}
*/
	void springMain() throws Exception {
		log.error("HI");
	}
	
	void doIt() throws Exception {
		SpringApplication app = new SpringApplication(getClass());
		app.setWebEnvironment(false);
		app.setDefaultProperties(appProperties);
		ConfigurableApplicationContext ctx = app.run();
		ctx.getBean(getClass()).springMain();
		ctx.close();
	}

	public static void main(String[] args) throws Exception {
		new Example3().doIt();
		System.out.println("Done.");
	}
}
