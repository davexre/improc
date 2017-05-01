package com.demo.stage2.test;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import com.demo.stage2.UQLParser;

public class UQLParserTest {

    public static void main(String args[])
    {
        try
        {
                String query = "(title =\"asd qwe\" and title=qwe)";
                UQLParser parser = new UQLParser(new StringReader(query));
                parser.parse();
                System.out.println("\nSQL Query: " + parser.sqlSB.toString());

                // Note: This code assumes a default connection 
                // (current userid and password).
                System.out.println("\nResults of Query");

                Class.forName("COM.ibm.db2.jdbc.app.DB2Driver").newInstance();
                Connection con = DriverManager.getConnection("jdbc:db2:moviedb");
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(parser.sqlSB.toString());
                while(rs.next())
                {
                        System.out.println("Movie Title = " + rs.getString("title") + " Director = " + rs.getString("director"));
                }
                rs.close();
                stmt.close();
                con.close();
        }
        catch(Exception e)
        {
                e.printStackTrace();
        }
    }
}
