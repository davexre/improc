package org.velobg;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.slavi.math.adjust.Statistics;

public class BikeToWork {
	public static void main(String[] args) {
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		String url = "jdbc:mysql://localhost:3306/s";
		String user = "s";
		String password = "";

		try {
			con = DriverManager.getConnection(url, user, password);
			st = con.createStatement();
			// rs = st.executeQuery("SELECT VERSION()");
			rs = st.executeQuery("SELECT travelDate, distance from wp_td_distance order by 2");

			Statistics stat = new Statistics();
			stat.start();
			while (rs.next()) {
				stat.addValue(rs.getDouble(2));
				System.out.println(rs.getDate(1) + "\t" + rs.getDouble(2));
			}
			System.out.println();
			stat.stop();
			System.out.println(stat.toString(Statistics.CStatAll));
			
		} catch (SQLException ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (st != null) {
					st.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
		System.out.println("Done.");
	}
}
