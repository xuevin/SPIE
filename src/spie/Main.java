package spie;

import javax.swing.JFrame;


/**
 * The Class Main.
 */
public class Main {
	public static void main(String[] args){
		createAndShow();
	}
	
	/**
	 * Creates the and shows the User Interface
	 */
	public static void createAndShow(){
		//Create a JFrame
		
		JFrame frame= new JFrame("SPlicing Interactive Environment");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JSpliceViewGUI panel = new JSpliceViewGUI();
		frame.add(panel);
		frame.setJMenuBar(panel.getJMenuBar());
		//Start the JFrame
		frame.pack();
		frame.setVisible(true);
	}

}
