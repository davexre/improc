package com.slavi.derbi.northwind;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Properties;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.sql.DataSource;
import javax.transaction.Transactional;

import org.apache.commons.beanutils.BeanUtilsBean2;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
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
import com.slavi.derbi.dbload.DbDataParser;
import com.slavi.derbi.dbload.DbDataParserTemplate;
import com.slavi.derbi.dbload.ValueParser;
import com.slavi.jdbcspy.SpyDataSource;

@Component
@ComponentScan
@EnableTransactionManagement
@Configuration
public class Derby {

	public final static Charset utf8 = Charset.forName("UTF8");

	public static ByteArrayInputStream parseCrappyBmpImage(String str) throws ParseException {
		byte[] r = DbDataParserTemplate.hexToBytes(str);
		if (r == null || r.length <= 78)
			return null;
		return new ByteArrayInputStream(r, 78, r.length - 78);
	}

	static void processCSV(DbDataParser dp, String fin) throws Exception {
		CSVParser p = CSVParser.parse(Derby.class.getResourceAsStream("data/Employees.csv"), utf8, CSVFormat.EXCEL.withQuote('\''));
		p.iterator().next();
		while (p.iterator().hasNext()) {
			CSVRecord r = p.iterator().next();
			dp.reset();
			for (int i = 0; i < dp.size(); i++) {
				String v = i < r.size() ? r.get(i) : null;
				dp.set(v);
			}
			dp.ps.executeUpdate();
		}
		p.close();
		dp.ps.close();
	}

	public static DataSource generateDb() throws Exception {
		EmbeddedDataSource ds = new EmbeddedDataSource();
		ds.setDatabaseName("memory:northwind;create=true");

		ValueParser crappyImageParser = (v) -> parseCrappyBmpImage(v);
		Properties sql = new Properties();
		sql.load(Derby.class.getResourceAsStream("data/sql.properties"));
		try (Connection conn = ds.getConnection()) {
			DbDataParserTemplate dbDataParserTemplate = new DbDataParserTemplate("MM/dd/yyyy");
			MyDbScriptRunner sr = new MyDbScriptRunner(conn);
			sr.process(Derby.class.getResourceAsStream("Derby_create_schema.sql.txt"));

			PreparedStatement ps = conn.prepareStatement(sql.getProperty("employees.sql"));
			DbDataParser dp = new DbDataParser(ps, dbDataParserTemplate);
			dp.parsers.set(14, crappyImageParser);
			processCSV(dp, "data/Employees.csv");

			ps = conn.prepareStatement(sql.getProperty("categories.sql"));
			dp = new DbDataParser(ps, dbDataParserTemplate);
			dp.parsers.set(3, crappyImageParser);
			processCSV(dp, "data/Categories.csv");
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
		try (PreparedStatement st = conn.prepareStatement("select * from \"Employees\" order by \"EmployeeID\" fetch first 3 rows only")) {
			System.out.println(ResultSetToString.resultSetToString(st.executeQuery()));
		}
		try (PreparedStatement st = conn.prepareStatement("select * from \"Employees\" order by \"EmployeeID\" fetch first 3 rows only")) {
			ResultSet rs = st.executeQuery();
			rs.next();
			IOUtils.copy(rs.getBlob("Photo").getBinaryStream(), new FileOutputStream("target/aa.bmp"));
		}
/*
		TypedQuery<OrderDetail> tq = em.createQuery("select e from \"Employees\" e", OrderDetail.class);
		tq.setMaxResults(1);
		List<OrderDetail> r = tq.getResultList();
		System.out.println(r.get(0).getId().getOrder().getOrderNumber());
		System.out.println("--------");
		System.out.println(r.get(0).getId().getProduct().getProductName());
		System.out.println("--------");
		System.out.println(r.get(0).getPriceEach());*/
	}

	public static void main(String[] args) throws Exception {
		new FileOutputStream("target/createDDL_ddlGeneration.sql").close();
		new FileOutputStream("target/dropDDL_ddlGeneration.sql").close();
		try (AnnotationConfigApplicationContext ctx =  new AnnotationConfigApplicationContext(Derby.class)) {
			ctx.getBean(Derby.class).doIt();
		};
		System.out.println("Done.");
	}
}
