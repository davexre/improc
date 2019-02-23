package com.slavi.db.speed;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.slavi.math.MathUtil;
import com.slavi.util.Marker;
import com.xuggle.ferry.AtomicInteger;

public abstract class TestDbSpeedBase {

	ExecutorService exec;

	abstract Connection getConn() throws SQLException;

	abstract void createTables(Connection conn) throws SQLException;

	abstract void doTest() throws Exception;

	String getSqlForCreateDataRows() {
		return "insert into t(data) values(?)";
	}

	String getSqlForMinMaxId() {
		return "select min(id), max(id) from t";
	}

	String makeRandomAsciiString(int numberOfChars) {
		StringBuilder sb = new StringBuilder(numberOfChars);
		Random r = new Random(); // Tried using SecureRandom as Sonar suggests, but it is way too slow.
		for (int i = 0; i < numberOfChars; i++) {
			sb.append((char) (r.nextInt(127 - 33) + 33));
		}
		return sb.toString();
	}

	void createDataRows(Connection conn, AtomicInteger countDown, boolean useAutoCommit) throws SQLException {
		conn.setAutoCommit(useAutoCommit);
		PreparedStatement ps = conn.prepareStatement(getSqlForCreateDataRows());
		int id = countDown.getAndDecrement();
		int count = 0;
		while (id >= 0) {
			count++;
			ps.setString(1, "Data index " + id + " " + makeRandomAsciiString(1900));
			ps.execute();
			if (!useAutoCommit && count % 10000 == 0) {
				conn.commit();
			}
			if (id % 10000 == 0) {
				System.out.println("Remaining " + id);
			}
			id = countDown.getAndDecrement();
		}
		if (!useAutoCommit)
			conn.commit();
	}

	void createDataRowsTask(AtomicInteger countDown) throws SQLException {
		try (Connection conn = getConn()) {
			createDataRows(conn, countDown, false);
		}
	}

	void createData(int maxMessages) throws Exception {
		try (Connection conn = getConn()) {
			createTables(conn);
		}
		Marker.mark("create data");
		List<CompletableFuture> tasks = new ArrayList<>();
		AtomicInteger countDown = new AtomicInteger(maxMessages);
		int numberOfParallelTasks = 1;
		for (int i = 0; i < numberOfParallelTasks; i++) {
			tasks.add(CompletableFuture.runAsync(new Runnable() {
				public void run() {
					try {
						createDataRowsTask(countDown);
					} catch (Exception e) {
						throw new CompletionException(e);
					}
				}
			}, exec));
		}
		CompletableFuture.allOf(tasks.toArray(new CompletableFuture[tasks.size()])).get();

		Marker state = Marker.release();
		double tps = 1000.0 * maxMessages / (state.end - state.start);
		System.out.println("TPS is " + MathUtil.d2(tps));
	}

	void readDataRows(Connection conn, int start, int finish, AtomicInteger countDown) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("select * from t where id = ?");
		for (int index = start; index < finish; index++) {
			ps.setInt(1, index);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				rs.getInt(1);
				rs.getString(2);
			};
			rs.close();
			int todo = countDown.decrementAndGet();
			if (todo % 10000 == 0)
				System.out.println("Remaining " + todo);
		}
	}

	void readDataRowsTask(int start, int finish, AtomicInteger countDown) throws SQLException {
		try (Connection conn = getConn()) {
			readDataRows(conn, start, finish, countDown);
		}
	}

	void readAllData(int numberOfParallelTasks) throws Exception {
		int min, max;

		try (Connection conn = getConn()) {
			Marker.mark("scan min/max data");
			PreparedStatement ps = conn.prepareStatement(getSqlForMinMaxId());
			ResultSet rs = ps.executeQuery();
			rs.next();
			min = (int) rs.getLong(1);
			max = (int) rs.getLong(2);
			rs.close();
			ps.close();

			System.out.println(min);
			System.out.println(max);
			Marker.releaseAndMark("random read");
		}

		List<CompletableFuture> tasks = new ArrayList<>();
		AtomicInteger countDown = new AtomicInteger(max - min);
		final int span = (max - min) / numberOfParallelTasks ;

		for (int i = 0; i < numberOfParallelTasks; i++) {
			final int ii = i;
			tasks.add(CompletableFuture.runAsync(new Runnable() {
				public void run() {
					int start = ii * span;
					int finish = Math.min((ii + 1) * span, max + 1);
					try {
						readDataRowsTask(start, finish, countDown);
					} catch (Exception e) {
						throw new CompletionException(e);
					}
				}
			}, exec));
		}
		CompletableFuture.allOf(tasks.toArray(new CompletableFuture[tasks.size()])).get();

		Marker state = Marker.release();
		double tps = 1000.0 * (max - min) / (state.end - state.start);
		System.out.println("TPS is " + MathUtil.d2(tps));
	}

	void doIt() throws Exception {
		exec = Executors.newCachedThreadPool();
		try {
			doTest();
		} finally {
			exec.shutdown();
		}
	}
}
