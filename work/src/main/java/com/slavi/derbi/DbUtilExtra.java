package com.slavi.derbi;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.sql.DataSource;

import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.io.DatabaseDataIO;
import org.apache.ddlutils.io.DatabaseIO;
import org.apache.ddlutils.model.Database;

public class DbUtilExtra {
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
}
