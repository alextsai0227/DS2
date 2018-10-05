package Client;

import java.awt.Color;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

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

	public Client() {
	};

	public static void main(String[] args) {
		try {
			name = keyBoard.nextLine();
			FirstWindow win = new FirstWindow();
			SecondWindow win2=new SecondWindow();
			socket = connect("localhost", 4000, name);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
			System.out.println("Connection established");
			String message = null;
			while ((message = in.readLine()) != null) {
				
				// debug print
				System.out.println("What is message");
				System.out.println(message);
				System.out.println(socket.toString());
				
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
						if (!name.equals(mes.get(1))) {
							controlBtn(false, win2);
						}
						
					}
				}

				if(isJSONValid(message)){
					JSONObject jsonobj = new JSONObject(message);
					System.out.println("=============jsonobj============");
					System.out.println(jsonobj.toString());
					if (jsonobj.get("command").equals("submit")) { 
						disableLetterBtn(win2, jsonobj);
						if(jsonobj.get("whoShouldPlay").equals(name)) {
							controlBtn(true, win2);		
						}
					}else if (jsonobj.get("command").equals("vote")) {
						disableLetterBtn(win2, jsonobj);
						String wordToVote = jsonobj.get("words").toString();
						String question = "Do you agree " + wordToVote +" is a word?"; 	
						int reply = JOptionPane.showConfirmDialog(null, question, "Vote", JOptionPane.YES_NO_OPTION);
						if (reply == JOptionPane.YES_OPTION) {	        
							jsonobj.put("votefor", "yes");
						}else{
							jsonobj.put("votefor", "no");
						}
						jsonobj.put("command", "voted");
						voted(jsonobj);
						System.out.println("voted end 4");
						System.out.println("voted end 5");
					}else if (jsonobj.get("command").equals("voted")) {
						System.out.println("=============OH!!!!!!!!!!!============");
						if(jsonobj.get("whoShouldPlay").equals(name)) {
							controlBtn(true, win2);		
						}
						if(jsonobj.has("updateScore")) {
							System.out.println("=============yeah!!!!!!!!!!!============");
							int score = (Integer) jsonobj.get("updateScore");
							System.out.println(score);
							int index = playerOrder.indexOf(jsonobj.get("playerName"));
							System.out.println("=============index!!!!!!!!!!!============");
							System.out.println(index);
							win2.changeScore(score, index);
						}
					}else if (jsonobj.get("command").equals("pass")) { 
						if(jsonobj.get("whoShouldPlay").equals(name)) {
							controlBtn(true, win2);		
						}
						if (jsonobj.get("isGameOver").equals("Y")) {
							JOptionPane.showMessageDialog(null, "Game over!", "Information", JOptionPane.PLAIN_MESSAGE);
							back();
						}
					}else if (jsonobj.get("command").equals("gameOver")) { 
						JOptionPane.showMessageDialog(null, "Someone leaves, Game over!", "Information", JOptionPane.PLAIN_MESSAGE);
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
						win.initialize(playerList, name);
					}
				}
				
				System.out.println("=============END!!!!!!!!!!!============");
			}
		} catch (Exception e) {
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
		if (!socket.isClosed()) {
			BufferedReader buf = null;
			PrintStream out = null;
			try {
				buf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintStream(socket.getOutputStream());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				JOptionPane.showMessageDialog(null, "Something wrong when disconnecting to the server!", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
			try {
				// generate request message
				JSONObject jsonobj = new JSONObject();
				jsonobj.put("connect", "0");
				jsonobj.put("message", "Goodbye,Sever!");
				String str = jsonobj.toString();
				// set message to server
				out.println(str);
				// waiting for server's respond
				String respond = buf.readLine();
				JSONObject obj = new JSONObject(respond);
				JOptionPane.showMessageDialog(null, obj.get("message"), "Information", JOptionPane.PLAIN_MESSAGE);
				// System.out.println(obj.get("message"));
				if (obj.get("connect").equals("0")) {
					socket.close();

				}

			} catch (SocketTimeoutException e) {
				JOptionPane.showMessageDialog(null, "Time out, the server does not response!", "Error",
						JOptionPane.ERROR_MESSAGE);
				// System.out.println("Time out, the server does not response");

			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Something wrong when connecting to the server!", "Error",
						JOptionPane.ERROR_MESSAGE);

			}
		}
	}
	public static void back()
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
			} catch (Exception e) {
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
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Something wrong when connecting to the server!", "Error",
						JOptionPane.ERROR_MESSAGE);
			}

		}
	}
	
	public void submit(JSONObject jsonobj) {
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
				System.out.println("Client submit");
				System.out.println(str);
				out.write(str + "\n");
				out.flush();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Something wrong when connecting to the server!", "Error",
						JOptionPane.ERROR_MESSAGE);
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
				System.out.println("Client vote");
				System.out.println(str);
				out.write(str + "\n");
				out.flush();
			} catch (Exception e) {
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
				System.out.println("Client voted");
				System.out.println(str);
				out.write(str + "\n");
				out.flush();
				System.out.println("voted end");
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Something wrong when connecting to the server!", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
			System.out.println("voted end 2");
		}
		System.out.println("voted end 3");
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
				System.out.println("Client pass");
				System.out.println(str);
				out.write(str + "\n");
				out.flush();
			} catch (Exception e) {
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
	
}
