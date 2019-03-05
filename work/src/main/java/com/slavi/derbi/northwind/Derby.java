package com.slavi.derbi.northwind;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
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
import com.slavi.jdbcspy.SpyDataSource;

@Component
@ComponentScan
@EnableTransactionManagement
@Configuration
public class Derby {

	public final static Charset utf8 = Charset.forName("UTF8");

	private final static String hexChars = "0123456789ABCDEF";

	public static byte[] hexToBytes(String hex) {
		if (hex.length() % 2 != 0)
			throw new NumberFormatException("Number of chars in hex string must be multiple of 2 but was " + hex.length());
		byte[] r = new byte[hex.length() / 2];
		for (int i = 0, ii = 0; i < r.length; i++) {
			int a1 = hexChars.indexOf(hex.charAt(ii));
			if (a1 < 0) throw new NumberFormatException("Invalid char " + hex.charAt(ii) + " at position " + ii);
			int a2 = hexChars.indexOf(hex.charAt(++ii));
			if (a2 < 0) throw new NumberFormatException("Invalid char " + hex.charAt(ii) + " at position " + ii);
			r[i] = (byte) (((a1 << 4) + a2) & 0xff);
		}
		return r;
	}

	public static ByteArrayInputStream parseHex(String str) throws ParseException {
		if (str == null || "".equals(str) || "NULL".equalsIgnoreCase(str))
			return null;
		return new ByteArrayInputStream(hexToBytes(str));
	}

	public static Date parseDate(String str, SimpleDateFormat df) throws ParseException {
		if (str == null || "".equals(str) || "NULL".equalsIgnoreCase(str))
			return null;
		return new Date(df.parse(str).getTime());
	}

	public static Integer parseInt(String str) {
		if (str == null || "".equals(str) || "NULL".equalsIgnoreCase(str))
			return null;
		return Integer.parseInt(str);
	}

	public static DataSource generateDb() throws Exception {
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		EmbeddedDataSource ds = new EmbeddedDataSource();
		ds.setDatabaseName("memory:northwind;create=true");
		try (Connection conn = ds.getConnection()) {
			MyDbScriptRunner sr = new MyDbScriptRunner(conn);
			sr.process(Derby.class.getResourceAsStream("Derby_create_schema.sql.txt"));

			PreparedStatement ps = conn.prepareStatement(IOUtils.toString(Derby.class.getResourceAsStream("data/Employees.sql"), utf8));

			/*
			CsvTokenizer t = new CsvTokenizer(",", "'");
			LineNumberReader r = new LineNumberReader(new InputStreamReader(Derby.class.getResourceAsStream("data/Employees.csv")));
			String line = r.readLine();
			t.setLine(line);
			while ((line = r.readLine()) != null) {
				t.setLine(line);
			*/

			CSVParser p = CSVParser.parse(Derby.class.getResourceAsStream("data/Employees.csv"), utf8, CSVFormat.EXCEL.withQuote('\''));
			p.iterator().next();
			while (p.iterator().hasNext()) {
				CSVRecord r = p.iterator().next();
				int col = 0;

				// "EmployeeID","LastName","FirstName"
				ps.setObject(1, parseInt(r.get(col++)));
				ps.setString(2, r.get(col++));
				ps.setString(3, r.get(col++));

				// "Title","TitleOfCourtesy","BirthDate",
				ps.setString(4, r.get(col++));
				ps.setString(5, r.get(col++));
				ps.setDate(6, parseDate(r.get(col++), df));

				// "HireDate","Address","City",
				ps.setDate(7, parseDate(r.get(col++), df));
				ps.setString(8, r.get(col++));
				ps.setString(9, r.get(col++));

				// "Region","PostalCode","Country",
				ps.setString(10, r.get(col++));
				ps.setString(11, r.get(col++));
				ps.setString(12, r.get(col++));

				// "HomePhone","Extension","Photo",
				ps.setString(13, r.get(col++));
				ps.setString(14, r.get(col++));
				ps.setBlob(15, parseHex(r.get(col++)));

				// "Notes","ReportsTo","PhotoPath"
				ps.setString(16, r.get(col++));
				ps.setObject(17, parseInt(r.get(col++)));
				ps.setString(18, r.get(col++));

				ps.executeUpdate();
			}
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
		try (PreparedStatement st = conn.prepareStatement("select * from \"Employees\" fetch first 3 rows only")) {
			System.out.println(ResultSetToString.resultSetToString(st.executeQuery()));
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
			ctx.getBean(Derby.class).loadAllEntities();
		};
		System.out.println("Done.");
	}
}
