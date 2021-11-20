package main_server;


import java.util.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Main_server {

	ServerSocket server_socket = null; 
	List<Thread> socket_list = null;
	Socket socket;
	Client_manager manager = null;
	Room_manager R_manager = null;
	JDBC jdbc = null;
	String str;
	JSONParser parser = new JSONParser();
	JSONObject tmp = null;
	
	
	
	public Main_server() throws IOException{
		
		server_socket = new ServerSocket(9647);
		System.out.println("Main_server_open in 9647");
		manager = new Client_manager(this);
		jdbc = new JDBC();
		socket_list = new ArrayList<Thread>();
		R_manager = new Room_manager(this);
				
	}
	
	public void start() {
		
		try {
			while(true) {
			
				socket = server_socket.accept();
				
				Client_socket thread = new Client_socket(this,socket,manager.echo);
				add_client(thread);
				thread.start();
				
			}		
			
		}catch(IOException e) {
			e.printStackTrace();
		}

	}
	
	public synchronized void add_client(Client_socket thread) {
		
		socket_list.add(thread);
		System.out.println("client add!.");
		
	}
	
	public synchronized Boolean remove_client(Client_socket thread) {
		
		if(socket_list.remove(thread)) {
			System.out.println("client removed.");
			JDBC.logout(thread.ID);
			return true;
		}else {
			System.out.println("client removed fail");
			return false;
		}
		
		
	}
	
	public synchronized int Login_request(String data,Client_socket socket) {

		data = JDBC.Login(data);
		if (data != null) {
			try {
				tmp = (JSONObject) parser.parse(data);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				System.out.println("parser_fail");
			}
			
			if (tmp.get("ERROR") != null) {
				socket.Fail_login((String)tmp.get("ERROR"));
				return -1;
			}

			int id = ((Long)tmp.get("ID")).intValue();
			manager.client_access(id, socket);
			socket.clear_login(id);
			return id;
		}else {
			socket.Fail_login("Invalid ID or Password.");
			return -1;
		}
		
	}
	
	public synchronized void regiester_request(String data,Client_socket socket) {
		
		int result = JDBC.register(data);
		if (result != -1) {
			socket.clear_login(result);
		} else {
			socket.Fail_login("Unable to register for these data.");
		}
		
	}
	
	public synchronized void make_Room(String data,Client_socket socket, int ID) {
		socket.manager.client_leave(ID, socket);
		if(R_manager.Make_Room(socket, data, ID)) {
			System.out.println("the room has been made.");
		}
	}
	
	public synchronized void Join_Room(String data,Client_socket socket,int ID) {
		socket.manager.client_leave(ID, socket);
		if(R_manager.Join_Room(socket, data,ID)) {
			System.out.println("the room has been made.");
		}
	}
	
	
	
}
