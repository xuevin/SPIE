package spie;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;


public class Main {
	public static void main(String[] args){
		createAndShow();
	}
	public static void createAndShow(){
		//Create a JFrame
		
		JFrame frame= new JFrame("SPlicing Interactive Environment");
		BufferedImage img = null;
		try {
			img = ImageIO.read(frame.getClass().getResource("/spie.gif"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		frame.setIconImage(img);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JSpliceViewGUI panel = new JSpliceViewGUI();
		frame.add(panel);
		frame.setJMenuBar(panel.getJMenuBar());
		//Start the JFrame
		frame.pack();
		frame.setVisible(true);
	}

}
