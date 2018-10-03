package Server;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Server extends Thread {
	private static int port;
	public static ArrayList<String> names = new ArrayList<String>();
	public static ArrayList<MyThread> gameConnection = new ArrayList<MyThread>();
	public static Map<Integer,Game> games = new HashMap<Integer,Game>();
	public static ArrayList<Boolean> status=new ArrayList<Boolean>();
	public Server() {
	}
	public static void main(String[] args) 
	{
		System.out.println("=====new======");
		port = 4000;
		ServerSocketFactory factory = ServerSocketFactory.getDefault();
		try (ServerSocket server = factory.createServerSocket(port)) {
			System.out.println("Waiting for client connection..");
			while (true) {
				Socket socket = server.accept();
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
				String userName = in.readLine();
				names.add(userName);
				System.out.println("Client " + userName + ": Applying for connection!");
				MyThread t = new MyThread(socket, userName, names, gameConnection,games,status);
				t.start();
				gameConnection.add(t);
				status.add(true);
				for (int i = 0; i < names.size(); i++) {
					gameConnection.get(i).update(names,status);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void broadcast(ArrayList<String> names, ArrayList<MyThread> gameConnection,ArrayList<Boolean> status) {
		for (int i = 0; i < names.size(); i++) {
			if(status.get(i))
				gameConnection.get(i).update(names,status);
		}
	}
	public void invite(ArrayList<String>player,ArrayList<String> names,ArrayList<MyThread> gameConnection,String name,int gameID)
	{
		for(int i=0;i<player.size();i++)
		{
			for(int j=0;j<names.size();j++)
			{
				if(player.get(i).equals(names.get(j)))
					gameConnection.get(j).invited(name,gameID);
			}
		}
	}
	public void playGame(ArrayList<String> playerNames,ArrayList<String> names,ArrayList<MyThread> gameConnection)
	{
		for(int i=0;i<playerNames.size();i++)
		{
			for(int j=0;j<names.size();j++)
			{
				if(playerNames.get(i).equals(names.get(j)))
					gameConnection.get(j).openGame(playerNames);
			}
		}
	}
}
