package Client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.json.JSONObject;
import java.util.Arrays;

/**
 * @author ZIYANG XIE StudentID 870523 23 Sep. 2018-10:12:51 am
 */
public class SecondWindow {

	JFrame frame;
	JPanel boardPanel;
	private JPanel userPanel;
	JButton submitButton;
	JButton voteButton;
	JButton passButton;
	private JPanel resultPanel;
	JComboBox<String> jcombo;
	private Map<String, Double> position;
	private int[] scoreOfPlayer;
	private int disableCounter = 0;
	private String[] inputWords;

	/**
	 * Create the application.
	 */


	/**
	 * Initialize the contents of the frame.
	 */
	public void initialize(String name, ArrayList<String> playerNames) {
		frame = new JFrame(name);
		frame.setVisible(true);
		frame.setBounds(100, 100, 1199, 749);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));

		boardPanel = new JPanel();
		boardPanel.setVisible(true);
		boardPanel.setLayout(new GridLayout(20, 20));
		frame.getContentPane().add(boardPanel, BorderLayout.CENTER);
		addGameBoard();

		userPanel = new JPanel();
		frame.getContentPane().add(userPanel, BorderLayout.SOUTH);
		userPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		addPlayerPanel();

		resultPanel = new JPanel();
		frame.getContentPane().add(resultPanel, BorderLayout.EAST);
		resultPanel.setLayout(new GridLayout(10, 1));
		scoreOfPlayer = new int[4];
		scoreOfPlayer[0] = 0;
		scoreOfPlayer[1] = 0;
		scoreOfPlayer[2] = 0;
		scoreOfPlayer[3] = 0;
		addResultPanel(playerNames);

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				frame.setVisible(false);
				exit();
			}

		});
		position = new HashMap<String, Double>();
		voteButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
				if (position.get("x") == null ) {
					JOptionPane.showMessageDialog(null, "You Should Put a Letter on Tile to Vote", "Waring", 2);
				}else {
					Point p = new Point();
					p.setLocation(Double.parseDouble(position.get("x").toString()),
							Double.parseDouble(position.get("y").toString()));
					JButton button = (JButton) boardPanel.getComponentAt(p);
					if (button.getText()!= null && button.getText().equals("")) {
						JOptionPane.showMessageDialog(null, "You Should Put a Letter on Tile to Vote", "Waring", 2);
					}else {
						String inputValue = JOptionPane.showInputDialog("Input your words(Insert a \",\" among words):");
						while(true) {
							inputValue = JOptionPane.showInputDialog("Input your words(Insert a \",\" among words):");
							if(inputValue == null) {
								JOptionPane.showMessageDialog(null, "You Should Enter Word(s) to Vote", "Waring", 2);
								
							}else {
								break;
							}
							
						}
						
						if (inputValue != null) {
							button.setEnabled(false);
							// msg to server 
							JSONObject jsonobj = generateJSON("vote", position, inputValue, name);
							jsonobj.put("connect", 5);
							jsonobj.put("letter", button.getText());
							new Client().vote(jsonobj);
							submitButton.setEnabled(false);
							voteButton.setEnabled(false);
							passButton.setEnabled(false);
							position.clear();
						}
					}
				}
			}
		});

		passButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				// pass to next player
				Point p = new Point();
				if (position.get("x") != null) {
					p.setLocation(Double.parseDouble(position.get("x").toString()),
							Double.parseDouble(position.get("y").toString()));
					JButton button = (JButton) boardPanel.getComponentAt(p);
					button.setText("");
				}

				// msg to server 
				JSONObject jsonobj = generateJSON("pass", null, null, name);
				jsonobj.put("connect", 5);
				new Client().pass(jsonobj);
				submitButton.setEnabled(false);
				voteButton.setEnabled(false);
				passButton.setEnabled(false);
				position.clear();
			}

		});
		submitButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				// submit the character without vote
				Point p = new Point();
				
				if (position.get("x") != null ) {
					p.setLocation(Double.parseDouble(position.get("x").toString()),
							Double.parseDouble(position.get("y").toString()));
					JButton button = (JButton) boardPanel.getComponentAt(p);
					if (button.getText()!= null && button.getText().equals("")) {
						JOptionPane.showMessageDialog(null, "You Should Put a Letter on Tile to Submit", "Waring", 2);
					}else {
						// msg to server 
						JSONObject jsonobj = generateJSON("submit", position, null, name);
						jsonobj.put("connect", 5);
						jsonobj.put("letter", button.getText());
						button.setEnabled(false);
						new Client().submit(jsonobj);
						submitButton.setEnabled(false);
						voteButton.setEnabled(false);
						passButton.setEnabled(false);			
						position.clear();
					}

				}else {
					JOptionPane.showMessageDialog(null, "You Should Put a Letter on Tile to Submit", "Waring", 2);
				}
				
			}

		});
	}

	/**
	 * @param playerArray
	 */
	private void addResultPanel(ArrayList<String> playerNames) {
		// TODO Auto-generated method stub
		JLabel result = new JLabel("The score of each users");
		resultPanel.add(result);
		for (int i = 0; i < playerNames.size(); i++) {
			JPanel scorePanel = new JPanel();
			JLabel player = new JLabel(playerNames.get(i) + ": ");
			JLabel score = new JLabel(String.valueOf(scoreOfPlayer[i]));
			scorePanel.add(player);
			scorePanel.add(score);

			resultPanel.add(scorePanel, i + 1);
		}

	}

	// add 20*20 buttons on the game board
	private void addGameBoard() {
		for (int i = 0; i < 20; i++) {
			for (int j = 0; j < 20; j++) {
				JButton jb = new JButton();
				jb.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						if (position.isEmpty()) {
							jb.setText(jcombo.getSelectedItem().toString());
							jb.setForeground(Color.red);
							jb.setBackground(Color.black);
							position.put("x", jb.getLocation().getX());
							position.put("y", jb.getLocation().getY());
						} else {
							jb.setText(jcombo.getSelectedItem().toString());
							jb.setForeground(Color.red);
							jb.setBackground(Color.black);
							Point p = new Point();
							p.setLocation(Double.parseDouble(position.get("x").toString()),
									Double.parseDouble(position.get("y").toString()));
							JButton button = (JButton) boardPanel.getComponentAt(p);
							button.setText("");
							button.setBackground(null);
							position.clear();
							position.put("x", jb.getLocation().getX());
							position.put("y", jb.getLocation().getY());
						}
					}
				});
				boardPanel.add(jb);
			}
		}
	}

	// add the submit,vote and pass button on the user panel
	// add the user selection panel
	private void addPlayerPanel() {
		JPanel lPanel = new JPanel();

		submitButton = new JButton("submit");
		voteButton = new JButton("vote");
		passButton = new JButton("pass");
		lPanel.add(submitButton);
		lPanel.add(voteButton);
		lPanel.add(passButton);

		JPanel rPanel = new JPanel();
		rPanel.setLayout(new GridLayout(1, 7));
		String[] characters = { "a", "b", "c", "d", "e", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o",
				"p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z" };
		jcombo = new JComboBox<String>(characters);
		JLabel label = new JLabel("Select your letter:");
		rPanel.add(label);
		rPanel.add(jcombo);

		userPanel.add(lPanel);
		userPanel.add(rPanel);
	}

	private void exit() {
		// TODO Auto-generated method stub
		new Client().back("Y");
		frame.setVisible(false);
	}

	
	public void changeScore(int score, int index) {	
		int indexOflabel = index+1;
		JPanel tempPanel = (JPanel) resultPanel.getComponent(indexOflabel);
		JLabel tempLable = (JLabel) tempPanel.getComponent(1);
		score += Integer.valueOf(tempLable.getText());
		tempLable.setText(String.valueOf(score));
	}
	
	/**
	 * @return the frame
	 */
	public JFrame getFrame() {
		return frame;
	}

	/**
	 * @param frame
	 *            the frame to set
	 */
	public void setFrame(JFrame frame) {
		this.frame = frame;
	}
	
	private JSONObject generateJSON(String command, Map<String, Double> position, String inputwords, String name) {
		  // generate request message
		  JSONObject jsonobj = new JSONObject();

			  jsonobj.put("command", command);
			  jsonobj.put("playerName", name);
			  jsonobj.put("isGameOver", "N");
			  switch(command){
			  case "submit":
				  jsonobj.put("pointx", position.get("x").toString());
				  jsonobj.put("pointy", position.get("y").toString());
				  break;
			  case "vote":
				  jsonobj.put("pointx", position.get("x").toString());
				  jsonobj.put("pointy", position.get("y").toString());
				  jsonobj.put("words", inputwords);
				  break;
			  case "pass":		    
				  break;
			  default: 
				  try {
					  throw new Exception();
				  } catch (Exception e) {  
					  e.printStackTrace();		   
				  }
			  }
		  return jsonobj;	 
	}
}

