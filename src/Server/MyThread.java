package Server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MyThread extends Server {
	private Socket clientSocket;
	private String name;
	public ArrayList<String> names = new ArrayList<String>();
	public ArrayList<MyThread> gameConnection = new ArrayList<MyThread>();
	public Map<Integer,Game> games = new HashMap<Integer,Game>();
	public ArrayList<Boolean> status=new ArrayList<Boolean>();
	public int numGames=0;
	public int gameID;

	public MyThread(Socket client, String user, ArrayList<String> names, ArrayList<MyThread> gameConnection,Map<Integer,Game> games,ArrayList<Boolean> status) {
		this.clientSocket = client;
		this.name = user;
		this.names = names;
		this.gameConnection = gameConnection;
		this.games=games;
		this.status=status;
	}

	public void run() {
		try {
			BufferedReader buf = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			boolean flag = true;
			while (flag) {
				try {
					String str = buf.readLine();
					
					
					JSONObject jsonobj = new JSONObject(str);
					if (jsonobj.get("connect").equals("0")) { //disconnect
						disconnect();
						flag = false;
					}else if(jsonobj.get("connect").equals("2")){  //invite
						String userName = jsonobj.getString("players");
						String[] userNames=userName.split(",");
						ArrayList<String> players=new ArrayList<String>();
						for(int i=0;i<userNames.length;i++)
						{
							if(!userNames[i].equals(this.name))
								players.add(userNames[i]);
						}
						gameID=super.numGames;
						super.invite(players, names, gameConnection,name,gameID);
						Game game=new Game(players,name);
						games.put(gameID, game);
						super.numGames++;
					}else if(jsonobj.get("connect").equals("3")) {//respond invite
						gameID= Integer.parseInt((String) jsonobj.get("gameID"));
						
						if(jsonobj.get("accept").equals("yes")) {
							 games.get(gameID).status.add(1);
						}
						else
						{
							games.get(gameID).status.add(2);
						}
						if(games.get(gameID).status.size()==(games.get(gameID).names.size()-1))
						{
							int g = 0 ;
							for(int i=0;i<games.get(gameID).status.size();i++)
							{
								if(games.get(gameID).status.get(i)==2)
									g = 1;
							}
							if(g == 0) {
								super.playGame(games.get(gameID).names,names,gameConnection);
								for(int i=0;i<games.get(gameID).names.size();i++)
								{
									for(int j=0;j<names.size();j++)
									{
										if(games.get(gameID).names.get(i).equals(names.get(j)))
												status.set(j, false);
									}
								}
								super.broadcast(names, gameConnection,status);
							}
							else
							{
								super.broadcast(names, gameConnection,status);
							}
						}
					}else if(jsonobj.get("connect").equals("4"))
					{
						gameID= Integer.parseInt((String) jsonobj.get("gameID"));
						for(int i=0;i<games.get(gameID).names.size();i++)
						{
							for(int j=0;j<names.size();j++)
								if(games.get(gameID).names.get(i).equals(names.get(j)))
									status.set(j, true);
						}
						if(jsonobj.get("isPlayerLeave").equals("Y")) {
							super.gameOver(names, gameConnection,games.get(gameID).names, jsonobj.get("playerName").toString(), status);
						}
						super.broadcast(names, gameConnection, status);
					}else if (jsonobj.get("command").equals("submit")) 
					{
						games.get(gameID).tileCount += 1;
						jsonobj.put("whoShouldPlay", games.get(gameID).names.get(whoShouldPlay(jsonobj)));
						super.submit(games.get(gameID).names, jsonobj.get("playerName").toString(), jsonobj, games.get(gameID));
						if (games.get(gameID).tileCount == 400) {
							gameID= Integer.parseInt((String) jsonobj.get("gameID"));
							for(int i=0;i<games.get(gameID).names.size();i++)
							{
								for(int j=0;j<names.size();j++)
									if(games.get(gameID).names.get(i).equals(names.get(j)))
										status.set(j, true);
							}
							super.gameOver(names, gameConnection,games.get(gameID).names, jsonobj.get("playerName").toString(), status);
						}
					}else if (jsonobj.get("command").equals("vote")) 
					{						
						games.get(gameID).tileCount += 1;
						super.vote(games.get(gameID).names, jsonobj.get("playerName").toString(), jsonobj, games.get(gameID));
					}else if (jsonobj.get("command").equals("voted")) 
					{						
						jsonobj.put("whoShouldPlay", games.get(gameID).names.get(whoShouldPlay(jsonobj)));
						super.voted(games.get(gameID).names, jsonobj.get("playerName").toString(), jsonobj, games.get(gameID));
						if (games.get(gameID).tileCount == 400) {
							gameID= Integer.parseInt((String) jsonobj.get("gameID"));
							for(int i=0;i<games.get(gameID).names.size();i++)
							{
								for(int j=0;j<names.size();j++)
									if(games.get(gameID).names.get(i).equals(names.get(j)))
										status.set(j, true);
							}
							super.gameOver(names, gameConnection,games.get(gameID).names, jsonobj.get("playerName").toString(), status);
						}
					}else if (jsonobj.get("command").equals("pass")) {
						jsonobj.put("whoShouldPlay", games.get(gameID).names.get(whoShouldPlay(jsonobj)));
						super.pass(games.get(gameID).names, jsonobj.get("playerName").toString(), jsonobj, games.get(gameID));
					}
				} catch (Exception e) {
					System.out.println("Client " + name + " is closed...");
					for (int i = 0; i < names.size(); i++) {
						if (name.equals(names.get(i))) {
							names.remove(i);
							gameConnection.remove(i);
							status.remove(i);
						}
					}
					super.broadcast(names, gameConnection,status);
					flag = false;
				}
			}
		} catch (Exception ex) {
		}
	}

	public void update(ArrayList<String> user,ArrayList<Boolean> status) {
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
			String str = "update";
			for (int i = 0; i < user.size(); i++) {
				if(status.get(i))
					str = str + "," + user.get(i);
			}
			out.write(str + "\n");
			out.flush();
		} catch (SocketException ex) {
			ex.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void disconnect() {
		System.out.println("Client " + name + " is closed...");
		for (int i = 0; i < names.size(); i++) {
			if (name.equals(names.get(i))) {
				names.remove(i);
				gameConnection.remove(i);
				status.remove(i);
			}
		}
		super.broadcast(names, gameConnection,status);
	}
	public void invited(String name,int gameID)
	{
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
			out.write("invited,"+name+","+String.valueOf(gameID)+"\n");
			out.flush();
		}catch (SocketException ex) {
			ex.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void openGame(ArrayList<String> playerNames)
	{
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
			String str = "play";
			for (int i = 0; i < playerNames.size(); i++) {
				str = str + "," + playerNames.get(i);
			}
			out.write(str + "\n");
			out.flush();
		}catch (SocketException ex) {
			ex.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void msgToClient(JSONObject jsonobj) {
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
			String str = jsonobj.toString();
			// sent message to client		
			out.write(str + "\n");
			out.flush();
		}catch (SocketException ex) {
			ex.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int whoShouldPlay(JSONObject jsonobj) {
		String playerName = jsonobj.get("playerName").toString();
		int whoShouldPlay = games.get(gameID).names.indexOf(playerName) + 1;
		if (whoShouldPlay >= games.get(gameID).names.size()) {
			whoShouldPlay = 0;
		}
		
		return whoShouldPlay;
	}
	
	public String getClientName() {
		return this.name;
	}
	
}
