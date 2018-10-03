import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import java.awt.GridLayout;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.JLabel;

public class FirstWindows extends JFrame {

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FirstWindows frame = new FirstWindows();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public FirstWindows() {
		setBounds(100, 100, 604, 447);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(null);
		
		JPanel panel=new JPanel();
		String[] name= {"A","B","C","D","E"};
		ArrayList<JCheckBox> checkBox=setCheckBox(panel,name);
		getContentPane().add(panel);
		JButton btnInvite = new JButton("Invite");
		btnInvite.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				for(int i=0;i<checkBox.size();i++)
				{
					if(checkBox.get(i).isSelected()==true)
					{
						System.out.println(checkBox.get(i).getActionCommand());
					}
				}
				JFrame f=new JFrame();
				f.setSize(100, 100);
				JLabel label=new JLabel("Waiting");
				label.setBounds(50, 50, 100, 100);
				f.add(label);
				f.setVisible(true);
			}
		});
		btnInvite.setBounds(49, 326, 137, 45);
		getContentPane().add(btnInvite);
		
		JButton btnExit = new JButton("Exit");
		btnExit.setBounds(386, 326, 137, 45);
		getContentPane().add(btnExit);

	}
	public ArrayList<JCheckBox> setCheckBox(JPanel panel,String[] name)
	{
		panel.setLayout(new GridLayout(name.length/4+1,4));
		panel.setBounds(49, 13, 474, 300);
		ArrayList<JCheckBox> checkBox=new ArrayList<JCheckBox>();
		for(int i=0;i<name.length;i++)
		{
			checkBox.add(new JCheckBox(name[i]));
			panel.add(checkBox.get(i));
		}
		return checkBox;
	}

}
