package main_server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Formatter;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JDBC {
	private static Connection conn = null;

	boolean vaild = false;
	
	public JDBC() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			String url = "jdbc:mysql://gcckim2020.kro.kr:3306/game_db";
			conn = DriverManager.getConnection(url, "game", "nwteamb");

			if (conn != null)
				System.out.println("[JDBC] Connected with: " + url);
		} catch (ClassNotFoundException e) {
			System.out.println("Cannot found the driver");
		} catch (SQLException e) {
			System.out.println("Error: " + e);
		}
		
		vaild = true;
	}
	
	public void finalize() {
		try {
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static String date() {
		LocalDate now = LocalDate.now();
		LocalTime time = LocalTime.now();
		
		Formatter fm = new Formatter();
		fm.format("%02d-%02d-%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
		DateTimeFormatter datefm = DateTimeFormatter.ofPattern("HH:mm:ss");

		String date = fm.toString() + " " + time.format(datefm);
		fm.close();
		
		return date;
	}
	
	public static String getString(int id, String key) {
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			stmt = conn.createStatement();
			String sql = "SELECT " + key + " FROM PLAYER WHERE\n" 
					+ "ID = " + id;
	
			rs = stmt.executeQuery(sql);
			if (rs.next()) {
				return rs.getString(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static int getInt(int id, String key) {
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			stmt = conn.createStatement();
			String sql = "SELECT " + key + " FROM PLAYER WHERE\n" 
					+ "ID = " + id;
	
			rs = stmt.executeQuery(sql);
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return 0;
	}
	
	public static double getDouble(int id, String key) {
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			stmt = conn.createStatement();
			String sql = "SELECT " + key + " FROM PLAYER WHERE\n" 
					+ "ID = " + id;
	
			rs = stmt.executeQuery(sql);
			if (rs.next()) {
				return rs.getDouble(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return 0.0;
	}
	
	public static boolean getBool(int id, String key) {
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			stmt = conn.createStatement();
			String sql = "SELECT " + key + " FROM PLAYER WHERE\n" 
					+ "ID = " + id;
	
			rs = stmt.executeQuery(sql);
			if (rs.next()) {
				return rs.getBoolean(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public static String Login(String data) {
		JSONParser parser = new JSONParser();
		JSONObject obj = null;
		try {
			obj = (JSONObject)parser.parse(data);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		if (obj == null)
			return null;
		
		String id = (String)obj.get("ID");
		String pw = (String)obj.get("passwd");
		obj.clear();
		
		Statement stmt = null;
		ResultSet rs = null;
		
		String date = date();

		try {
			String sql = "UPDATE player\n"
					+ "SET connection = true,\n"
					+ "con_date = ?"
					+ "WHERE connection = false AND\n"
					+ "user_id = ? AND\n"
					+ "password = hex(aes_encrypt(?, 'team2'))";
			
			var pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, date);
			pstmt.setString(2, id);
			pstmt.setString(3, pw);
			int count = pstmt.executeUpdate();
			
			if (count != 0) {
				stmt = conn.createStatement();
				sql = "SELECT * FROM PLAYER WHERE\n" 
						+ "USER_ID = '" + id + "'";

				rs = stmt.executeQuery(sql);
				if (rs.next()) {
					obj.clear();
					obj.put("ID", rs.getInt(1));
					return obj.toJSONString();
				}
			}
			else {
				stmt = conn.createStatement();
				sql = "SELECT * FROM PLAYER WHERE\n" 
						+ "USER_ID = '" + id + "'\n"
						+ "AND PASSWORD = hex(aes_encrypt('" + pw + "', 'team2'))";

				rs = stmt.executeQuery(sql);
				if (rs.next()) {
					obj.clear();
					obj.put("ID", -1);
					obj.put("ERROR", "ALREADY_LOGINED");
					return obj.toJSONString();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public static boolean logout(int id) {
		if (id < 0)
			return false;
		
		try {
			String sql = "UPDATE player\n"
					+ "SET connection = false,\n"
					+ "con_date = ?\n"
					+ "WHERE id = ?";
			
			var pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, date());
			pstmt.setInt(2, id);

			int count = pstmt.executeUpdate();
			if (count != 0)
				return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;
	}
	
	public static int register(String data) {
		JSONParser parser = new JSONParser();
		JSONObject obj = null;
		try {
			obj = (JSONObject)parser.parse(data);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		int id = 0;
		
		try {
			var stmt = conn.createStatement();
			String sql = "SELECT MAX(ID) FROM PLAYER";
	
			var rs = stmt.executeQuery(sql);
			
			if (rs.next())
				id = rs.getInt(1) + 1;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		String uid = (String)obj.get("ID");
		String pw = (String)obj.get("passwd");
		
		String name = (String)obj.get("Name");
		String nname = (String)obj.get("NickName");
		String email = (String)obj.get("EMail");
		String psite = (String)obj.get("PSite");
		
		PreparedStatement pstmt = null;

		try {
			String sql = "INSERT INTO PLAYER VALUES(?, ?, hex(aes_encrypt(?, 'team2')), 1,"
					+ "?, ?, ?, ?,"
					+ "true, null, 0, 0, 0)";
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, id);
			pstmt.setString(2, uid);
			pstmt.setString(3, pw);
			
			pstmt.setString(4, name);
			pstmt.setString(5, nname);
			pstmt.setString(6, String.valueOf(email));
			pstmt.setString(7, String.valueOf(psite));

			int count = pstmt.executeUpdate();
			if (count != 0)
				return id;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return -1;
	}
	
	public static boolean receivechat(String data) {
		JSONParser parser = new JSONParser();
		JSONObject obj = null;
		try {
			obj = (JSONObject)parser.parse(data);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		if (obj == null)
			return false;

		String date = date();
		String context = (String)obj.get("Data");
		
		PreparedStatement pstmt = null;

		try {
			String sql = "INSERT INTO CHAT VALUES('Unknown', 0, 0, ?, ?, asdf)";
			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, date);
			pstmt.setString(2, context);

			System.out.println(date);
			
			 int count = pstmt.executeUpdate();
			 if (count != 0)
				 return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
}
