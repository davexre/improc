package com.slavi.example.springBoot.example3;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.slavi.example.springBoot.example3.component.Dao;

@Configuration
@ComponentScan
@EnableTransactionManagement
@EnableJpaRepositories
@EnableAutoConfiguration
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
 */
/*
	@Bean
	static PlatformTransactionManager getTransactionManager(@Autowired DataSource dataSource, @Autowired EntityManagerFactory emf) {
		JpaTransactionManager r = new JpaTransactionManager();
		r.setDataSource(dataSource);
		r.setEntityManagerFactory(emf);
		return r;
	}*/

	@Autowired
	Dao dao;

	void springMain() throws Exception {
		log.error("HI");
		dao.populateInitialData();
		dao.dummy();
	}

	void doIt() throws Exception {
		SpringApplication app = new SpringApplication(getClass());
		app.setWebApplicationType(WebApplicationType.NONE);
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
