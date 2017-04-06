package com.slavi.derbi.jpa;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.io.IOUtils;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.io.DatabaseIO;
import org.apache.ddlutils.model.Database;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slavi.derbi.jpa.entity.DateStyle;
import com.slavi.derbi.jpa.entity.EntityWithDate;
import com.slavi.derbi.jpa.entity.MyEntity;
import com.slavi.derbi.jpa.entity.MyEntityPartial;
import com.slavi.util.StringPrintStream;

public class JpaCreate {

	public static String dbToXml(DataSource dataSource) throws SQLException, IOException {
		StringPrintStream out = new StringPrintStream();
		dbToXml(dataSource, out);
		return out.toString();
	}
	
	public static void dbToXml(DataSource dataSource, OutputStream out) throws SQLException, IOException {
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

	public void createORMs(ApplicationContext appContext) throws Exception {
		EntityManagerFactory emf = appContext.getBean("entityManagerFactory", EntityManagerFactory.class);
		EntityManager em = emf.createEntityManager();
		// ... more
		em.getTransaction().begin();
		
		DateStyle ds = new DateStyle();
		ds.setFormat("dd.mm.yyyy");
		ds = em.merge(ds);
		em.getTransaction().commit();
		em.getTransaction().begin();
		
		EntityWithDate ed = new EntityWithDate();
		ed.setData("My data");
		ed.setDateIdRef(ds.getDateId());
		ed = em.merge(ed);

		em.getTransaction().commit();
		int edId = ed.getEntityWithDateId();

		ed = em.find(EntityWithDate.class, edId);
		ObjectMapper mapper = new ObjectMapper();
		
		StringPrintStream out = new StringPrintStream();
		mapper.writeValue(out, ed);
		System.out.println(out.toString());
		
		System.out.println("---------------------");
		em.getTransaction().begin();
		for (int i = 0; i < 10; i++) {
			MyEntity ent = new MyEntity();
			ent.setData ("Data  for entity No " + i);
			ent.setData1("Data1 for entity No " + i);
			ent.setData2("Data2 for entity No " + i);
			em.merge(ent);
		}
		Query q = em.createQuery("select e from MyEntityPartial e where e.id = 1", MyEntityPartial.class);
		List<MyEntityPartial> r = q.getResultList();
		for (MyEntityPartial i : r) {
			System.out.println(i);
		}
		em.getTransaction().commit();
		System.out.println("---------------------");
		
		em.close();
	}
	
	void doIt() throws Exception {
		ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext("JpaCreate-sping.xml", getClass());
		DataSource dataSource = appContext.getBean("dataSource", DataSource.class);
		createORMs(appContext);
		
		dbToXml(dataSource);
		appContext.close();
	}

	public static void main(String[] args) throws Exception {
		System.setProperty("derby.stream.error.method", "com.slavi.dbutil.DerbyLogOverSlf4j.getLogger");
		new JpaCreate().doIt();
		System.out.println("Done.");
	}
}
