package Client;

import javax.swing.JFrame;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import java.awt.GridLayout;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.JLabel;
import java.util.Scanner;

public class FirstWindow  {
	public JFrame frame;
	public JPanel panel;
	public ArrayList<String> names;
	public String name;
	public ArrayList<String> mes;
	public JFrame fw= new JFrame();

	/**
	 * Launch the application.
	 */
	public void initialize(ArrayList<String> names, String name) {
		this.name=name;
		frame = new JFrame(name);
		frame.setVisible(true);
		frame.setEnabled(true );
		frame.setBounds(100, 100, 604, 447);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		panel = new JPanel();
		this.names = names;
		for(int i=0;i<names.size();i++)
		{
			if(name.equals(names.get(i)))
				names.remove(i);
		}
		ArrayList<JCheckBox> checkBox = setCheckBox(panel, names);
		frame.getContentPane().add(panel);
		JButton btnInvite = new JButton("Invite");
		btnInvite.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ArrayList<String> players = new ArrayList<String>();
				for (int i = 0; i < checkBox.size(); i++) {
					if (checkBox.get(i).isSelected() == true) {
						// System.out.println(checkBox.get(i).getActionCommand());
						players.add(checkBox.get(i).getActionCommand());
					}
				}
				fw = new JFrame(name);
				frame.setEnabled(false);
				fw.setBounds(100, 100, 400, 200);
				JLabel label = new JLabel("Waiting", JLabel.CENTER);
				label.setBounds(100, 100, 400, 200);
				fw.add(label);
				fw.setVisible(true);
				new Client().invitePlayer(players);
			}
		});
		btnInvite.setBounds(49, 326, 137, 45);
		frame.getContentPane().add(btnInvite);

		JButton btnExit = new JButton("Exit");
		btnExit.setBounds(386, 326, 137, 45);
		frame.getContentPane().add(btnExit);

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				frame.setVisible(false);
				exit();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				System.exit(0);
			}

		});
		btnExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				frame.setVisible(false);
				exit();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				System.exit(0);
			}
		});
	}

	public ArrayList<JCheckBox> setCheckBox(JPanel panel, ArrayList<String> names) {
		panel.setLayout(new GridLayout(names.size() / 4 + 1, 4));
		panel.setBounds(49, 13, 474, 300);
		ArrayList<JCheckBox> checkBox = new ArrayList<JCheckBox>();
		for (int i = 0; i < names.size(); i++) {
			if (!name.equals(names.get(i))) {
				checkBox.add(new JCheckBox(names.get(i)));
				panel.add(checkBox.get(i));
			}
		}
		return checkBox;
	}

	private void exit() {
		new Client().diconnect();
		frame.setVisible(false);

	}

	public void invited(String name,String gameID) {
		this.frame.setEnabled(false);
		JFrame f = new JFrame(this.name);
		f.setBounds(100, 100, 450, 300);
		f.getContentPane().setLayout(null);
		JLabel label = new JLabel(name+" invite you!", JLabel.CENTER);
		label.setBounds(135, 79, 169, 42);
		f.add(label);
		f.setVisible(true);
		JButton btnNo = new JButton("No");
		btnNo.setBounds(297, 197,117,29);
		f.getContentPane().add(btnNo);
		btnNo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				new Client().acceptInvite("no", gameID);
				f.setVisible(false);
			}
		});
		JButton btnYes = new JButton("Yes");
		btnYes.setBounds(36, 197,117,29);
		f.getContentPane().add(btnYes);
		btnYes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				new Client().acceptInvite("yes", gameID);
				f.setVisible(false);
			}
		});
	}
}
