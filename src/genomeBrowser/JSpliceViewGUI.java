package genomeBrowser;

import gffParser.GFF3Parser;
import gffParser.Gene;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.RenderingHints.Key;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.KeyStroke;


import net.sf.samtools.*;


public class JSpliceViewGUI extends JPanel implements ActionListener{
	private JMenuBar menuBar;
	private JMenu file,help;
	private JMenuItem quit,loadGFFMenuItem,loadBAMMenuItem, save, about;
	private JFileChooser fileChooser;
	private ProcessingApplet applet;
	private HashMap<String,Gene> genes;
	private JCheckBox showIntronsCheckBox,isCodingCheckBox;
	private JPanel geneBox,controlBox;
	private JComboBox geneChooser;
	private JSlider slider;
	
	//Constructor
	public JSpliceViewGUI(){
		setLayout(new BorderLayout());
		
		fileChooser= new JFileChooser();
		genes=null;
		
		//MenuItem for loading GFF
		loadGFFMenuItem = new JMenuItem("Open GFF",'O');
		loadGFFMenuItem.setMnemonic(KeyEvent.VK_O);
		loadGFFMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.META_MASK));
		loadGFFMenuItem.addActionListener(this);
		
		//MenuItem for loading BAM
		loadBAMMenuItem = new JMenuItem("Load Bam",'L');
		loadBAMMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.META_MASK));
		loadBAMMenuItem.addActionListener(this);
		
		//MenuItem for saving PNG
		save = new JMenuItem("Save",'s');
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.META_MASK));
		save.addActionListener(this);
		
		//MenuItem for Quiting
		quit = new JMenuItem("Quit");
		quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.META_MASK));
		
		//MenuItem for About
		about = new JMenuItem("About");
		
		//Help Menu
		help = new JMenu("Help");
		help.add(about);
		
		//File Menu
		file = new JMenu("File");
		file.add(loadGFFMenuItem);
		file.add(loadBAMMenuItem);
		file.add(save);
		file.add(new JSeparator());
		file.add(quit);
		
		//MenuBar
		menuBar = new JMenuBar();
		menuBar.add(file);
		menuBar.add(help);
		
		//JScrollBar
		slider= new JSlider();
		
		//Processing Applet
		applet = new ProcessingApplet(850, 800);
		applet.init();
		
		
		
		//GeneChooser
		geneChooser = new JComboBox();
		geneChooser.addActionListener(this);
		
		//GeneBox
		geneBox = new JPanel();
		geneBox.setLayout(new BoxLayout(geneBox,BoxLayout.Y_AXIS));
		geneBox.add(new JLabel("Choose a Gene"));
		geneBox.add(geneChooser);
		
		//Introns Check Box
		showIntronsCheckBox = new JCheckBox("Show Introns");
		showIntronsCheckBox.setSelected(true);
		showIntronsCheckBox.setEnabled(false);
		showIntronsCheckBox.addActionListener(this);
		//Directionality Check Box
		isCodingCheckBox = new JCheckBox("Show Coding");
		isCodingCheckBox.setSelected(true);
		isCodingCheckBox.setEnabled(false);
		isCodingCheckBox.addActionListener(this);
		//ControlBox
		controlBox = new JPanel();
		controlBox.setAlignmentY(TOP_ALIGNMENT);
		controlBox.setLayout(new BoxLayout(controlBox,BoxLayout.Y_AXIS));
		controlBox.add(geneBox);
		controlBox.add(showIntronsCheckBox);
		controlBox.add(isCodingCheckBox);
		
		add(menuBar,BorderLayout.NORTH);
		add(slider,BorderLayout.SOUTH);
		add(applet,BorderLayout.CENTER);
		add(controlBox,BorderLayout.EAST);
		
	}
	public void actionPerformed(ActionEvent e){
		
		if(e.getSource()==loadGFFMenuItem){
			int returnVal = fileChooser.showOpenDialog(JSpliceViewGUI.this);
			if (returnVal == JFileChooser.APPROVE_OPTION){
				File file = fileChooser.getSelectedFile();
				GFF3Parser gff3Parser = new GFF3Parser();
				try {
					//Load the file
					BufferedReader in = new BufferedReader(new FileReader(file));
					//Attempt to parse file
					gff3Parser.parse(in);
					genes=gff3Parser.getGenes();
					// If the gene is null from the parser, that means that there was some problem with parsing
					if(genes==null){
						System.err.println("Parsing Error");
					}else{
						geneChooser.removeAllItems();
						for(String geneID:gff3Parser.getGenes().keySet()){
							geneChooser.addItem(geneID);
						}
						
						//Fix the Options in the GUI
						//If Gene Is Coding Check the box/else uncheck it
						if(genes.get(geneChooser.getSelectedItem().toString()).getStrand()){
							applet.setCoding(true);
						}else{
							applet.setCoding(false);
						}
						isCodingCheckBox.setEnabled(true);
						showIntronsCheckBox.setEnabled(true);
						
						applet.loadIsoform(genes.get(geneChooser.getSelectedItem().toString()));
					}					
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}	
		}else if(e.getSource()==loadBAMMenuItem){
			int returnVal = fileChooser.showOpenDialog(JSpliceViewGUI.this);
			if (returnVal == JFileChooser.APPROVE_OPTION){
				File inputBamFile = fileChooser.getSelectedFile();
				
				//Asks user to load an index file along with the BAM file
				int loadIndex = JOptionPane.showConfirmDialog(
					    this, "Would you like to load a index file?\n" +
						"Please note that performance will be hindered without one",
					    "Load Index", JOptionPane.YES_NO_OPTION);
				
				if (loadIndex == JFileChooser.APPROVE_OPTION){
					File inputBamIndex = fileChooser.getSelectedFile();
					
					final SAMFileReader inputSam = new SAMFileReader(inputBamFile,inputBamIndex);
					inputSam.setValidationStringency(SAMFileReader.ValidationStringency.LENIENT);
					
					for(SAMSequenceRecord sequence:inputSam.getFileHeader().getSequenceDictionary().getSequences()){
						System.out.println(sequence.getSequenceName());
					}
					Iterator<SAMRecord> match = inputSam.query("18", 21712662, 21713020, true);
					while(match.hasNext()){	
						System.out.println(match.next().getReadName());
					}
				}else{
					//TODO Make a slow version where it iterates through... I don't suggest this
					//I should ask
				}
			}
		}else if(e.getSource()==save){
			int returnVal = fileChooser.showSaveDialog(JSpliceViewGUI.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                applet.save(file.toString()+".png");
            } 
		}else if(e.getSource()==showIntronsCheckBox){
			System.out.println("WTF");
			applet.disableIntrons(!showIntronsCheckBox.isSelected());
		}else if(e.getSource()==isCodingCheckBox){
			
			System.out.println("WTF");
			//The Graph Always begins with coding strand.
			applet.flip();
		}
		
	}

	
}
