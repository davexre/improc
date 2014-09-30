package com.slavi.derbi;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.io.DatabaseDataIO;
import org.apache.ddlutils.io.DatabaseIO;
import org.apache.ddlutils.model.Database;
import org.apache.derby.jdbc.EmbeddedDataSource40;

import com.slavi.util.StringPrintStream;

public class DbExport {

	public static void makeArchive(DataSource ds, OutputStream out, String modelName) throws Exception {
		Platform  platform = PlatformFactory.createNewPlatformInstance(ds);
		Connection connection = ds.getConnection();
		ZipOutputStream zos = new ZipOutputStream(out);
		try {
			Database database = platform.readModelFromDatabase(connection, modelName);

			DatabaseIO databaseIO = new DatabaseIO();
			ZipEntry zipEntry = new ZipEntry(modelName + "_ddl.xml");
			zos.putNextEntry(zipEntry);
			Writer writer = new OutputStreamWriter(zos, "UTF-8");
			databaseIO.write(database, writer);
			writer.flush();
			zos.closeEntry();
			
			DatabaseDataIO databaseDataIO = new DatabaseDataIO();
			zipEntry = new ZipEntry(modelName + "_data.xml");
			zos.putNextEntry(zipEntry);
			writer = new OutputStreamWriter(zos, "UTF-8") {
				public void close() throws IOException {
					// Ignore
				}
			};
			databaseDataIO.writeDataToXML(platform, database, writer, "UTF-8");
			writer.flush();
			zos.closeEntry();
		} finally {
			connection.close();
			zos.close();
		}
	}
	
	public static void importArchive(DataSource ds, ZipFile zipFile, String modelName) throws Exception {
		String dataFileName = modelName + "_ddl.xml";
		ZipEntry zipEntry = zipFile.getEntry(dataFileName);
		if (zipEntry == null)
			throw new FileNotFoundException(dataFileName);
		
		Platform platform = PlatformFactory.createNewPlatformInstance(ds);
		InputStreamReader reader = new InputStreamReader(zipFile.getInputStream(zipEntry));
		Database database = new DatabaseIO().read(reader);
		reader.close();
		platform.alterTables(database, false);
		
		dataFileName = modelName + "_data.xml";
		zipEntry = zipFile.getEntry(dataFileName);
		if (zipEntry == null)
			throw new FileNotFoundException(dataFileName);
		DatabaseDataIO dataIO = new DatabaseDataIO();
		dataIO.writeDataToDatabase(platform, database, new InputStream[] { zipFile.getInputStream(zipEntry) } );
	}
	
	void ddlUtilsImport() throws Exception {
		// Import
		EmbeddedDataSource40 ds = new EmbeddedDataSource40();
		ds.setDatabaseName("memory:myTestDB;create=true");
		
		Platform platform = PlatformFactory.createNewPlatformInstance(ds);
		Database db = new DatabaseIO().read(new InputStreamReader(new ByteArrayInputStream(exportDDL.getBytes())));
//		platform.createTables(db, false, false);
		platform.alterTables(db, false);
		
		Connection conn = ds.getConnection();
		QueryRunner qr = new QueryRunner(ds);
		qr.update("insert into Channel(name, pollinterval, type) values (?,?,?)", "Some channel name", "2d", 1);

		List r = qr.query("select * from Channel", new MapListHandler());
		System.out.println(r);

		DatabaseDataIO dataIO = new DatabaseDataIO();
		dataIO.writeDataToDatabase(platform, db, new InputStream[] { new ByteArrayInputStream(exportDML.getBytes()) } );
		
		r = qr.query("select * from Channel", new MapListHandler());
		System.out.println(r);
		
		conn.close();
	}
	
	String exportDDL;
	String exportDML;
	
	void ddlUtilsExport() throws Exception {
		Platform  platform = PlatformFactory.createNewPlatformInstance(ds);
		Connection conn = ds.getConnection();
		Database db = platform.readModelFromDatabase(conn, "MyDbTest");
		DatabaseIO dbio = new DatabaseIO();
		
		StringPrintStream out;
		
		out = new StringPrintStream();
		Writer wr = new OutputStreamWriter(out, "UTF-8");
		dbio.write(db, wr);
		wr.flush();
		exportDDL = out.toString();

		out = new StringPrintStream();
		DatabaseDataIO dataIO = new DatabaseDataIO();
		dataIO.writeDataToXML(platform, db, out, "utf-8");
		exportDML = out.toString();
		
		conn.close();
	}
	
	EmbeddedDataSource40 ds;
	void doIt() throws Exception {
		ds = new EmbeddedDataSource40();
		ds.setDatabaseName("../Database/webscrap;create=true");

		Context ctx = new InitialContext();
		ctx.bind("webscrap", ds);
		try {
			ddlUtilsExport();
			System.out.println(exportDDL);
			// makeArchive(ds, new FileOutputStream("/home/slavian/temp/aaa.zip"), "webscrap");
			//ddlUtilsImport();
		} finally {
		}
	}
	
	public static void main(String[] args) throws Exception {
		new DbExport().doIt();
		System.out.println("Done.");
	}
}
