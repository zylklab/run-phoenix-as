package net.zylklab.run_phoenix_as;

import java.io.IOException;
import java.security.PrivilegedExceptionAction;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.hadoop.security.UserGroupInformation;


public class App {
	
	public static void main(String[] args) throws IOException, InterruptedException, SQLException {
		final String ZK_PORT = "2181";
		final String ZK_HOST = "odin003.bigdata.zylk.net";
		final String ZK_PATH = "/hbase-unsecure";
		//jdbc:phoenix:m1.hdp.local,m2.hdp.local,d1.hdp.local:2181:/hbase-unsecure
		final String url = "jdbc:phoenix:" + ZK_HOST +":"+ ZK_PORT+":"+ZK_PATH;
		UserGroupInformation user2Ugi = UserGroupInformation.createRemoteUser("hbase", null);
		final List<String> result = new ArrayList<>();
		long t0 = System.currentTimeMillis();
		user2Ugi.doAs(new PrivilegedExceptionAction<Void>() {
			@Override
			public Void run() throws Exception {
				try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement()) {
					conn.setAutoCommit(true);
					try (ResultSet rs = stmt.executeQuery("select STATE,CITY,POPULATION from US_POPULATION")) {
						while (rs.next()) {
							result.add(rs.getString("STATE"));
						}
					} 
				} 
				return null;
			}
		});
		
		for (String string : result) {
			System.out.println("resultado mapeado "+string);
		}
		System.out.println("tiempo "+(System.currentTimeMillis() - t0));
	}
	/*
	 * Prueba con queryserer que no termina de funcionar ... pero que creo que también debería funcionar con el doAs
	 */
	public static void main2(String[] args) throws IOException, InterruptedException {
		final String PQS_PORT = "8765";
		final String PQS_HOST = "odin001.bigdata.zylk.net";
		final String doAsUrlTemplate = "jdbc:phoenix:url=http://" + PQS_HOST + ":" + PQS_PORT + "?doAs"+"=%s;authentication=SPNEGO;serialization=PROTOBUF";
		final String doAsUrl = String.format(doAsUrlTemplate, "alice");

		try (Connection conn = DriverManager.getConnection(doAsUrl); Statement stmt = conn.createStatement()) {
			conn.setAutoCommit(true);
			try (ResultSet rs = stmt.executeQuery("select STATE,CITY,POPULATION from US_POPULATION")) {
				while (rs.next()) {
					System.out.println("STATE: " + rs.getString("STATE"));
				}
			}
			// select * from US_POPULATION

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
