package Client;

import java.awt.Color;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.json.JSONException;
import org.json.JSONObject;

public class Client {
	private String inputStr = null;
	private static String name;
	private static Socket socket = null;
	static Scanner keyBoard = new Scanner(System.in);
	static Client client;
	static int gameID;
	static ArrayList<String> playerOrder;
	static Boolean isPlaying = false;
	private static String host;
	private static int port;

	public Client() {
	};

	public static void main(String[] args) {
		try {
			if (args.length < 2) {
				System.out.println("You should enter both server_address and port");
				System.exit(0);
			}else {
				name = keyBoard.nextLine();
				FirstWindow win = new FirstWindow();
				SecondWindow win2=new SecondWindow();
				socket = connect(args[0], Integer.parseInt(args[1]), name);
				host = args[0];
				port = Integer.parseInt(args[1]);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
				System.out.println("Connection established");
				String message = null;
				while ((message = in.readLine()) != null) {	
					// check if there is duplicate username
					if(message.equals("duplicat user name")) {
						System.out.println(message);
						System.out.println("Please enter your name again");
						name = keyBoard.nextLine();
						socket = connect(args[0], Integer.parseInt(args[1]), name);
						in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
					}

					String[] str = message.split(",");
					ArrayList<String> mes = new ArrayList<String>();
					for (int i = 0; i < str.length; i++) {
						mes.add(str[i]);
					}
					if (mes.get(0).equals("update")) {
						if (!isPlaying) {
							if(win2.frame!=null&&win2.frame.isVisible())
								win2.frame.setVisible(false);
							if (win.frame != null)
								win.frame.setVisible(false);
							if(win.fw.isVisible())
								win.fw.setVisible(false);
							mes.remove(0);
							win.initialize(mes, name);
							win.frame.setBounds(100, 100, 604, 447);
//							win.frame.setResizable(false);
						}
					}
					if (mes.size() > 1) {
						if (mes.get(0).equals("invited")) {
							mes.remove(0);
							gameID=Integer.parseInt(mes.get(1));
							win.invited(mes.get(0), mes.get(1));
						}
						if(mes.get(0).equals("play"))
						{
							ArrayList<String> playerNames = new ArrayList<String>();
							for (int i = 1; i < mes.size(); i++) {
								playerNames.add(mes.get(i));
							}
							win.frame.setVisible(false);
							win.fw.setVisible(false);
							win2.initialize(name, playerNames);
							win2.frame.setBounds(100, 100, 1200, 750);
							win2.frame.setResizable(false);
							isPlaying = true;
							playerOrder = playerNames;
							win2.changePlayer(mes.get(1));
							if (!name.equals(mes.get(1))) {
								controlBtn(false, win2);
							}
							
						}
					}

					if(isJSONValid(message)){
						JSONObject jsonobj = new JSONObject(message);
						// receive submit from server
						if (jsonobj.get("command").equals("submit")) { 
							win2.changePlayer(jsonobj.get("whoShouldPlay").toString());
							disableLetterBtn(win2, jsonobj);
							if(jsonobj.get("whoShouldPlay").equals(name)) {
								controlBtn(true, win2);		
							}
						}else if (jsonobj.get("command").equals("vote")) {
							// receive vote from server
							disableLetterBtn(win2, jsonobj);
							String wordToVote = jsonobj.get("words").toString();
							
							// ask player if they agree words
							String[] words = wordToVote.split(",");
							String question = "Do you agree '" + words[0] +"' is a word?";
							int reply = JOptionPane.showConfirmDialog(null, question, "Vote", JOptionPane.YES_NO_OPTION);
							if (reply == JOptionPane.YES_OPTION) {	        
								jsonobj.put("votefor1", "yes");
							}else{
								jsonobj.put("votefor1", "no");
							}
							jsonobj.put("command", "voted");
							if (words.length == 2) {
								String question2 = "Do you agree '" + words[1] +"' is a word?"; 	
								int reply2 = JOptionPane.showConfirmDialog(null, question2, "Vote", JOptionPane.YES_NO_OPTION);
								if (reply2 == JOptionPane.YES_OPTION) {	        
									jsonobj.put("votefor2", "yes");
								}else{
									jsonobj.put("votefor2", "no");
								}
							}
							
							// answer to server
							voted(jsonobj);
							String resultList="";
							for(int i=0;i<playerOrder.size();i++)
							{
								resultList +=playerOrder.get(i);
								resultList +=":";
								resultList +=SecondWindow.scoreOfPlayer[i];
								resultList += "\n";
							}
						}else if (jsonobj.get("command").equals("voted")) {
							win2.changePlayer(jsonobj.get("whoShouldPlay").toString());
							if(jsonobj.get("whoShouldPlay").equals(name)) {
								controlBtn(true, win2);		
							}
							// receive the voted result from server
							if(jsonobj.has("updateScore")) {
								int score = (Integer) jsonobj.get("updateScore");
								int index = playerOrder.indexOf(jsonobj.get("playerName"));
								win2.changeScore(score, index);
							}
						}else if (jsonobj.get("command").equals("pass")) { 
							win2.changePlayer(jsonobj.get("whoShouldPlay").toString());
							if(jsonobj.get("whoShouldPlay").equals(name)) {
								controlBtn(true, win2);		
							}
							// receive pass from server and check if everyone pass in row
							if (jsonobj.get("isGameOver").equals("Y")) {
								String resultList="";
								for(int i=0;i<playerOrder.size();i++)
								{
									resultList +=playerOrder.get(i);
									resultList +=":";
									resultList +=win2.scoreOfPlayer[i];
									resultList += "\n";
								}
								JOptionPane.showMessageDialog(null, "Score:\n"+resultList,"Game Over",JOptionPane.PLAIN_MESSAGE);
								back("N");
							}
						}else if (jsonobj.get("command").equals("gameOver")) { 
							// receive gameOver from server, someone leaves, return to the player pool
							isPlaying=false;
							String resultList="";
							for(int i=0;i<playerOrder.size();i++)
							{
								resultList +=playerOrder.get(i);
								resultList +=":";
								resultList +=SecondWindow.scoreOfPlayer[i];
								resultList += "\n";
							}
							JOptionPane.showMessageDialog(null, "Someone leaves!\nScore:\n"+resultList,"Game Over",JOptionPane.PLAIN_MESSAGE);
							if(win2.frame!=null&&win2.frame.isVisible())
								win2.frame.setVisible(false);
							if (win.frame != null)
								win.frame.setVisible(false);
							if(win.fw.isVisible())
								win.fw.setVisible(false);
							String availablePlayer = jsonobj.get("update").toString();
							String[] players = availablePlayer.split(",");
							ArrayList<String> playerList = new ArrayList<String>();
							for (int i = 1; i < players.length; i++) {
								playerList.add(players[i]);
							}
						}
					}
				}	
			}
			
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Server is down", "Information", JOptionPane.PLAIN_MESSAGE);
			System.exit(0);
		}

	}

	public static Socket connect(String address, int port, String name) {
		try {
			// Create a stream socket bounded to any port and connect it to the
			socket = new Socket("localhost", 4000);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
			out.write(name + "\n");
			out.flush();
			return socket;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void diconnect() {
		// TODO Auto-generated method stub
		System.out.print("disconnect");
		if (!socket.isClosed()) {
			System.out.print("disconnect22222");
			BufferedReader buf = null;
			PrintStream out = null;
			try {
				buf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintStream(socket.getOutputStream());
				System.out.print("disconnect==========");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				JOptionPane.showMessageDialog(null, "Something wrong when disconnecting to the server!", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
			try {
				System.out.print("disconnec======0000");
				// generate request message
				JSONObject jsonobj = new JSONObject();
				jsonobj.put("connect", "0");
				jsonobj.put("message", "Goodbye,Sever!");
				String str = jsonobj.toString();
				System.out.print("disconnec======11111");
				// set message to server
				out.println(str);

				System.out.print("disconnect33333");
				System.exit(0);
		
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Something wrong when connecting to the server!", "Error",
						JOptionPane.ERROR_MESSAGE);

			}
		}
	}
	public static void back(String isPlayerLeave)
	{
		// TODO Auto-generated method stub
				if (!socket.isClosed()) {
					PrintStream out = null;
					try {
						out = new PrintStream(socket.getOutputStream());
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						JOptionPane.showMessageDialog(null, "Something wrong when disconnecting to the server!", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
					isPlaying = false;
					// generate request message
					JSONObject jsonobj = new JSONObject();
					jsonobj.put("connect", "4");
					jsonobj.put("gameID",String.valueOf(gameID));
					jsonobj.put("playerName", name);
					jsonobj.put("isPlayerLeave", isPlayerLeave);
					String str = jsonobj.toString();
					// set message to server
					out.println(str);
				}
	}
		
	public void invitePlayer(ArrayList<String> players) {
		if (!socket.isClosed()) {
			BufferedReader buf = null;
			BufferedWriter out = null;
			try {
				buf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				JOptionPane.showMessageDialog(null, "Something wrong when inviting players!", "Error",
						JOptionPane.ERROR_MESSAGE);
			}

			try {
				String names = players.get(0);
				for (int i = 1; i < players.size(); i++) {
					names = names + "," + players.get(i);
				}
				// generate request message
				JSONObject jsonobj = new JSONObject();
				jsonobj.put("connect", "2");
				jsonobj.put("players", names);
				String str = jsonobj.toString();
				// set message to server
				out.write(str + "\n");
				out.flush();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Something wrong when connecting to the server!", "Error",
						JOptionPane.ERROR_MESSAGE);
			}

		}
	}

	public void acceptInvite(String accept, String gameID) {
		if (!socket.isClosed()) {
			BufferedWriter out = null;
			try {
				out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				JOptionPane.showMessageDialog(null, "Something wrong when inviting players!", "Error",
						JOptionPane.ERROR_MESSAGE);
			}

			try {
				JSONObject jsonobj = new JSONObject();
				jsonobj.put("connect", "3");
				jsonobj.put("accept", accept);
				jsonobj.put("gameID", gameID);
				String str = jsonobj.toString();
				// set message to server
				out.write(str + "\n");
				out.flush();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Something wrong when connecting to the server!", "Error",
						JOptionPane.ERROR_MESSAGE);
			}

		}
	}
	
	public void submit(JSONObject jsonobj){
		if (!socket.isClosed()) {
			BufferedWriter out = null;
			try {
				out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8")); 
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				JOptionPane.showMessageDialog(null, "Something wrong when submit the letter!", "Error",
						JOptionPane.ERROR_MESSAGE);
			}

			try {
				String str = jsonobj.toString();
				// set message to server
				out.write(str + "\n");
				out.flush();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Something wrong when connecting to the server!", "Error",
						JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
		}
	}
	
	public void vote(JSONObject jsonobj) {
		if (!socket.isClosed()) {
			BufferedWriter out = null;
			try {
				out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				JOptionPane.showMessageDialog(null, "Something wrong when vote the words!", "Error",
						JOptionPane.ERROR_MESSAGE);
			}

			try {
				String str = jsonobj.toString();
				// set message to server
				out.write(str + "\n");
				out.flush();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Something wrong when connecting to the server!", "Error",
						JOptionPane.ERROR_MESSAGE);
			}

		}
	}

	public static void voted(JSONObject jsonobj) {
		if (!socket.isClosed()) {
			BufferedWriter out = null;
			try {
				out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				JOptionPane.showMessageDialog(null, "Something wrong when vote the words!", "Error",
						JOptionPane.ERROR_MESSAGE);
			}

			try {
				String str = jsonobj.toString();
				// set message to server
				out.write(str + "\n");
				out.flush();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Something wrong when connecting to the server!", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	public void pass(JSONObject jsonobj) {
		if (!socket.isClosed()) {
			BufferedWriter out = null;
			try {
				out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				JOptionPane.showMessageDialog(null, "Something wrong when pass!", "Error",
						JOptionPane.ERROR_MESSAGE);
			}

			try {
				String str = jsonobj.toString();
				// set message to server
				out.write(str + "\n");
				out.flush();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Something wrong when connecting to the server!", "Error",
						JOptionPane.ERROR_MESSAGE);
			}

		}
	}
	
	public static void disableLetterBtn(SecondWindow win2, JSONObject jsonobj) {
		Double pointx = Double.parseDouble(jsonobj.get("pointx").toString());
		Double pointy = Double.parseDouble(jsonobj.get("pointy").toString());
		String letter = jsonobj.get("letter").toString();
		Point p = new Point();
		p.setLocation(pointx, pointy);
		JButton button = (JButton) win2.boardPanel.getComponentAt(p);		
		button.setText(letter);
		button.setEnabled(false);
	}

	public static boolean isJSONValid(String test) {
	    try {
	        new JSONObject(test);
	    } catch (JSONException ex) {
	            return false;
	    }
	    return true;
	}
	
	public static void controlBtn(Boolean isOpen, SecondWindow win2) {
		// change the state of playing button
		if (isOpen) {
			win2.submitButton.setEnabled(true);
			win2.voteButton.setEnabled(true);
			win2.passButton.setEnabled(true);	
		}else {
			win2.submitButton.setEnabled(false);
			win2.voteButton.setEnabled(false);
			win2.passButton.setEnabled(false);
		}
		
	}
	
	public static boolean isSocketAliveUitlitybyCrunchify(String hostName, int port) {
		boolean isAlive = false;
 
		// Creates a socket address from a hostname and a port number
		SocketAddress socketAddress = new InetSocketAddress(hostName, port);
		Socket socket = new Socket();
 
		// Timeout required - it's in milliseconds
		int timeout = 2000;
 
		try {
			socket.connect(socketAddress, timeout);
			socket.close();
			isAlive = true;
 
		} catch (SocketTimeoutException exception) {
			System.out.println("SocketTimeoutException " + hostName + ":" + port + ". " + exception.getMessage());
			return isAlive;
		} catch (IOException exception) {
			System.out.println(
					"IOException - Unable to connect to " + hostName + ":" + port + ". " + exception.getMessage());
			return isAlive;
		}
		return isAlive;
	}
}
