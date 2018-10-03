/**
 * 
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * @author ZIYANG XIE StudentID 870523 23 Sep. 2018-10:12:51 am
 */
public class MainInterface {

	JFrame frame;
	private JPanel boardPanel;
	private JPanel userPanel;
	private JButton submitButton;
	private JButton voteButton;
	private JButton passButton;
	private JPanel resultPanel;
	JComboBox<String> jcombo;
	private Map<String, Double> position;
	private int[] scoreOfPlayer;
	private int disableCounter = 0;
	private String[] inputWords;

	/**
	 * Create the application.
	 */
	public MainInterface() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setVisible(true);
		frame.setBounds(100, 100, 900, 600);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));

		boardPanel = new JPanel();
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
		String[] playerArray = { "player1", "player2", "player3", "player4" };
		scoreOfPlayer = new int[4];
		scoreOfPlayer[0] = 0;
		scoreOfPlayer[1] = 0;
		scoreOfPlayer[2] = 0;
		scoreOfPlayer[3] = 0;
		addResultPanel(playerArray);

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exit();
			}

		});
		position = new HashMap<String, Double>();
		voteButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				String inputValue = JOptionPane.showInputDialog("Input your words(Insert a \",\" among words):");
				if (inputValue != null) {
					inputWords = inputValue.split(",");
					System.out.println(inputWords);
					Point p = new Point();
					p.setLocation(Double.parseDouble(position.get("x").toString()),
							Double.parseDouble(position.get("y").toString()));
					JButton button = (JButton) boardPanel.getComponentAt(p);
					button.setEnabled(false);
					position.clear();
					//change player score
					changeScore();
					disableCounter++;
					if (disableCounter == 400) {
						System.out.println("Game Over!!!!!");
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
				p.setLocation(Double.parseDouble(position.get("x").toString()),
						Double.parseDouble(position.get("y").toString()));
				JButton button = (JButton) boardPanel.getComponentAt(p);
				button.setText("");
				position.clear();
			}

		});
		submitButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				// submit the character without vote
				Point p = new Point();
				p.setLocation(Double.parseDouble(position.get("x").toString()),
						Double.parseDouble(position.get("y").toString()));
				JButton button = (JButton) boardPanel.getComponentAt(p);
				button.setEnabled(false);
				position.clear();
				disableCounter++;
				if (disableCounter == 400) {
					System.out.println("Game Over!!!!!");
				}
			}

		});
	}

	/**
	 * @param playerArray
	 */
	private void addResultPanel(String[] playerArray) {
		// TODO Auto-generated method stub
		JLabel result = new JLabel("The score of each users");
		resultPanel.add(result);
		for (int i = 0; i < playerArray.length; i++) {
			JPanel scorePanel = new JPanel();
			JLabel player = new JLabel(playerArray[i] + ": ");
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
		System.out.println("exit!!");
		System.exit(0);
	}

	
	private void changeScore() {
		for (int i = 0; i < inputWords.length; i++) {
			scoreOfPlayer[0] = scoreOfPlayer[0] + inputWords[i].length();
		}
		scoreOfPlayer[0] = scoreOfPlayer[0] - inputWords.length + 1;
		JPanel tempPanel = (JPanel) resultPanel.getComponent(1);
		JLabel tempLable = (JLabel) tempPanel.getComponent(1);
		tempLable.setText(String.valueOf(scoreOfPlayer[0]));
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
}
