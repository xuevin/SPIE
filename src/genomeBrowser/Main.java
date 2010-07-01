package genomeBrowser;

import javax.swing.JFrame;

public class Main {
	public static void main(String[] args){
		createAndShow();
	}
	public static void createAndShow(){
		//Create a JFrame
		JFrame frame= new JFrame("Genome Broswer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JSpliceViewGUI panel = new JSpliceViewGUI();
		frame.add(panel);
		frame.setJMenuBar(panel.getJMenuBar());
		
		//Start the JFrame
		frame.pack();
		frame.setVisible(true);
	}

}
