package Server;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.*;
import javax.net.ServerSocketFactory;

import org.json.JSONObject;

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
	public static int numGames=0;
	public Server() {
	}
	public static void main(String[] args) 
	{
		port = 4000;
		ServerSocketFactory factory = ServerSocketFactory.getDefault();
		try (ServerSocket server = factory.createServerSocket(port)) {
			System.out.println("Server ip adress: " + InetAddress.getLocalHost());
			System.out.println("Waiting for client connection..");
			while (true) {
				Socket socket =null;
				try {
					socket = server.accept();
					BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
					String userName = in.readLine();
					if(names.contains(userName)) {
						try {
							BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
							out.write("duplicat user name");
							out.flush();
						} catch (SocketException ex) {
							ex.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						socket.close();
					}else {
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
				}catch(Exception e) {
					socket.close();
					e.printStackTrace();
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
	
	public void submit(ArrayList<String> playerNames,String name, JSONObject jsonobj,Game game)
	{
		game.passInRow = 0;
		for(int i=0;i<playerNames.size();i++)
		{
			for(int j=0;j<gameConnection.size();j++)
			{
				if (gameConnection.get(j).getClientName().equals(playerNames.get(i)) && !gameConnection.get(j).getClientName().equals(name)) {
					gameConnection.get(j).msgToClient(jsonobj);
				}
			}
		}
	}

	public void vote(ArrayList<String> playerNames,String name, JSONObject jsonobj, Game game)
	{
		game.passInRow = 0;
		for(int i=0;i<playerNames.size();i++)
		{
			for(int j=0;j<gameConnection.size();j++)
			{
				if (gameConnection.get(j).getClientName().equals(playerNames.get(i)) && !gameConnection.get(j).getClientName().equals(name)) {
					gameConnection.get(j).msgToClient(jsonobj);
				}
			}
		}
	}
	
	public void voted(ArrayList<String> playerNames,String name, JSONObject jsonobj,Game game)
	{
		if (jsonobj.get("votefor1").equals("yes")){
			game.response_yes += 1;
			game.vote_response += 1;
		}else {
			game.vote_response += 1;
		}
		String words = jsonobj.get("words").toString();
		String[] wordsArr = words.split(",");
		
		if (wordsArr.length == 2) {
			if (jsonobj.get("votefor2").equals("yes")) {
				game.response_yes_2 += 1;
				game.vote_response_2 += 1;
			}else {
				game.vote_response_2 += 1;
			}
		}else {
			game.vote_response_2 += 1;
		}

		if (game.vote_response == playerNames.size() - 1 && game.vote_response_2 == playerNames.size() - 1) {
			// compute score
			int score = 0;
			
			if (game.response_yes == playerNames.size() - 1 && game.response_yes_2 == playerNames.size() - 1) {
				score += wordsArr[0].length() + wordsArr[1].length() - 1;
				jsonobj.put("updateScore", score);
			}else if (game.response_yes == playerNames.size() - 1){
				score += wordsArr[0].length();
				jsonobj.put("updateScore", score);
			}else if (game.response_yes_2 == playerNames.size() - 1){
				score += wordsArr[1].length();
				jsonobj.put("updateScore", score);
			}
			for(int i=0;i<playerNames.size();i++)
			{

				for(int j=0;j<gameConnection.size();j++)
				{

					if (jsonobj.has("updateScore")) {
						if (gameConnection.get(j).getClientName().equals(playerNames.get(i))){
							gameConnection.get(j).msgToClient(jsonobj);
						}
					}else {
						if (gameConnection.get(j).getClientName().equals(playerNames.get(i)) && !gameConnection.get(j).getClientName().equals(name)) {
							gameConnection.get(j).msgToClient(jsonobj);
						}
					}
					

				}
			}			
			game.response_yes = 0;
			game.vote_response = 0;
			game.response_yes_2 = 0;
			game.vote_response_2 = 0;
			
		}
		
	}
	
	public void pass(ArrayList<String> playerNames,String name, JSONObject jsonobj, Game game)
	{
		game.passInRow += 1;
		Boolean isGameOver = false;
		if (game.passInRow == playerNames.size()) {
			isGameOver = true;
		}
		for(int i=0;i<playerNames.size();i++)
		{
			for(int j=0;j<gameConnection.size();j++)
			{
				if(isGameOver) {
					if (gameConnection.get(j).getClientName().equals(playerNames.get(i))) {
						jsonobj.put("isGameOver", "Y");
						gameConnection.get(j).msgToClient(jsonobj);
					}
				}else {
					if (gameConnection.get(j).getClientName().equals(playerNames.get(i)) && !gameConnection.get(j).getClientName().equals(name)) {
						gameConnection.get(j).msgToClient(jsonobj);
					}
				}

			}
		}
	}
	
	public void gameOver(ArrayList<String> names, ArrayList<MyThread> gameConnection, ArrayList<String> gamePlayers, String playerName, ArrayList<Boolean> status) {
		String availablePlayer = "update";
		for (int i = 0; i < names.size(); i++) {
			if(status.get(i))
				availablePlayer = availablePlayer + "," + names.get(i);
		}
		
		for(int i=0;i<gamePlayers.size();i++)
		{
			for(int j=0;j<gamePlayers.size();j++)
				if(gamePlayers.get(i).equals(gameConnection.get(j).getClientName()) && !gamePlayers.get(i).equals(playerName)) {
					JSONObject jsonobj = new JSONObject();
					jsonobj.put("command", "gameOver");
					jsonobj.put("update", availablePlayer);
					gameConnection.get(j).msgToClient(jsonobj);
				}
		}
	}
	
}
