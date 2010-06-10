package genomeBrowser;

import gffParser.GFF3Parser;
import gffParser.Gene;
import gffParser.MRNA;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.BorderFactory;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


import net.sf.samtools.*;
import net.sf.samtools.util.CloseableIterator;


public class JSpliceViewGUI extends JPanel implements ActionListener,ChangeListener{
	private JMenuBar menuBar;
	private JMenu file,help;
	private JMenuItem quit,loadGFFMenuItem,loadBAMMenuItem, saveMenuItem, about;
	private JFileChooser fileChooser;
	private ProcessingApplet applet;
	private HashMap<String,Gene> geneRecords;
	private JCheckBox spliceLinesCheckBox,isCodingCheckBox,shortReadCheckBox,weightedIsoformCheckBox;
	private JPanel geneBox,controlBox;
	private JComboBox geneChooser,isoformChooser;
	private JSlider slider,scaleSlider;
	private SAMFileReader samRecords;
	
	//Constructor
	public JSpliceViewGUI(){
		setLayout(new BorderLayout());
		
		fileChooser= new JFileChooser();
		geneRecords=null;
		
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
		saveMenuItem = new JMenuItem("Save",'s');
		saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.META_MASK));
		saveMenuItem.addActionListener(this);
		
		//MenuItem for Quiting
		quit = new JMenuItem("Quit");
		quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.META_MASK));
		quit.addActionListener(this);
		
		//MenuItem for About
		about = new JMenuItem("About");
		
		//Help Menu
		help = new JMenu("Help");
		help.add(about);
		
		//File Menu
		file = new JMenu("File");
		file.add(loadGFFMenuItem);
		file.add(loadBAMMenuItem);
		file.add(saveMenuItem);
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
		applet.setSize(850, 800);
		applet.init();
		
		//GeneChooser
		geneChooser = new JComboBox();
		geneChooser.setEnabled(false);
		geneChooser.setPreferredSize((new Dimension(150,15)));
		geneChooser.setMaximumSize(new Dimension(150,25));
		geneChooser.setAlignmentX(LEFT_ALIGNMENT);
		geneChooser.addActionListener(this);
		
		//GeneBox
		geneBox = new JPanel();
		geneBox.setLayout(new BoxLayout(geneBox,BoxLayout.Y_AXIS));
		geneBox.add(new JLabel("Choose a Gene"));
		geneBox.add(geneChooser);
		
		//isoformChooser
		isoformChooser = new JComboBox();
		isoformChooser.addItem("View All");
		isoformChooser.setEnabled(false);
		isoformChooser.setPreferredSize((new Dimension(150,15)));
		isoformChooser.setMaximumSize(new Dimension(150,25));
		isoformChooser.setAlignmentX(LEFT_ALIGNMENT);
		isoformChooser.addActionListener(this);
		
		//SpliceLines Check Box
		spliceLinesCheckBox = new JCheckBox("Show SpliceLine");
		spliceLinesCheckBox.setSelected(true);
		spliceLinesCheckBox.setEnabled(false);
		spliceLinesCheckBox.addActionListener(this);
		
		//Directionality Check Box
		isCodingCheckBox = new JCheckBox("Show Coding");
		isCodingCheckBox.setAlignmentX(LEFT_ALIGNMENT);
		isCodingCheckBox.setSelected(true);
		isCodingCheckBox.setEnabled(false);
		isCodingCheckBox.addActionListener(this);
		
		
		//Short Reads CheckBox
		shortReadCheckBox = new JCheckBox("Show Short Reads");
		shortReadCheckBox.setEnabled(false);
		shortReadCheckBox.addActionListener(this);

		//WeightedIsoform CheckBox
		weightedIsoformCheckBox = new JCheckBox("Show Weighted Isoforms");
		weightedIsoformCheckBox.setEnabled(false);
		weightedIsoformCheckBox.addActionListener(this);
		
		
		//ScalingSlider
		scaleSlider = new JSlider(JSlider.HORIZONTAL,2,20,10);
		scaleSlider.setPaintLabels(true);
		scaleSlider.setMajorTickSpacing(1);
		scaleSlider.setEnabled(false);
		scaleSlider.addChangeListener(this);
		
		
		//ControlBox
		controlBox = new JPanel();
		controlBox.setPreferredSize(new Dimension(200,getHeight()));
		controlBox.setLayout(new BoxLayout(controlBox,BoxLayout.Y_AXIS));
		controlBox.add(spliceLinesCheckBox);
		controlBox.add(isCodingCheckBox);
		controlBox.add(geneBox);
		controlBox.add(shortReadCheckBox);
		controlBox.add(isoformChooser);
		controlBox.add(weightedIsoformCheckBox);
		controlBox.add(new JLabel("Scale"));
		controlBox.add(Box.createRigidArea(new Dimension(0,5)));
		controlBox.add(scaleSlider);
		controlBox.add(new JLabel("Scale"));
		
		add(menuBar,BorderLayout.NORTH);
		add(slider,BorderLayout.SOUTH);
		add(applet,BorderLayout.CENTER);
		add(controlBox,BorderLayout.EAST);
		
	}
	public void actionPerformed(ActionEvent e){
		
		if(e.getSource()==loadGFFMenuItem){
			loadGFFMenuItemAction();
		}else if(e.getSource()==loadBAMMenuItem){
			loadBAMMenuItemAction();
		}else if(e.getSource()==saveMenuItem){
			saveMenuItemAction();
		}else if(e.getSource()==spliceLinesCheckBox){
			applet.setSpliceLinesVisible(spliceLinesCheckBox.isSelected());
		}else if(e.getSource()==isCodingCheckBox){
			//The Graph Always begins with coding strand.
			applet.flip();
		}else if(e.getSource()==shortReadCheckBox){
			applet.setShortReadsVisible(shortReadCheckBox.isSelected());
		}else if(e.getSource()==quit){
			System.exit(0);	
		}else if(e.getSource()==isoformChooser){
			isoformChooserAction();
		}else if(e.getSource()==geneChooser){
			geneChooserAction();
		}else if(e.getSource()==weightedIsoformCheckBox){
			applet.setWeightedIsoformsVisible(weightedIsoformCheckBox.isSelected());
			applet.setGeneVisible(!weightedIsoformCheckBox.isSelected());
			applet.setErrorBarsVisible(false);
		}else{
			
		}
		
	}
	public void stateChanged(ChangeEvent e){
		if(e.getSource()==scaleSlider){
			applet.setYScale(scaleSlider.getValue());
		}
	}
	private void isoformChooserAction() {
		if(filesLoaded()){
			if(isoformChooser.getItemCount()==0||isoformChooser.getSelectedItem()=="View All"){
				//Do Nothing
			}else{
				Gene gene =  geneRecords.get(geneChooser.getSelectedItem().toString());
				MRNA mrna = gene.getMRNA().get(isoformChooser.getSelectedItem().toString());
				int geneStart = gene.getStart();
				int geneLength = gene.getLength();
				weightedIsoformCheckBox.setSelected(true);
				applet.loadWeightedIsoform(mrna, geneStart, geneLength);
				applet.setGeneVisible(false);
				applet.setWeightedIsoformsVisible(true);
				applet.setErrorBarsVisible(true);
			}	
		}
		
		
		
		
	}
	
	/**
	 * Action to be performed when save menu is clicked 
	 * This action asks the user to save the current image as a PNG in a directory
	 */
	private void saveMenuItemAction() {
		int returnVal = fileChooser.showSaveDialog(JSpliceViewGUI.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            applet.save(file.toString()+".png");
        } 
		
	}
	private void loadBAMMenuItemAction() {
		int returnVal = fileChooser.showOpenDialog(JSpliceViewGUI.this);
		if (returnVal == JFileChooser.APPROVE_OPTION){
			File inputBamFile = fileChooser.getSelectedFile();
			
			//Asks user to load an index file along with the BAM file
			int loadIndex = JOptionPane.showConfirmDialog(
				    this, "Would you like to load a index file?\n" +
					"Please note that performance will be hindered without one",
				    "Load Index", JOptionPane.YES_NO_OPTION);
			
			if (loadIndex == JOptionPane.OK_OPTION){
				returnVal = fileChooser.showOpenDialog(JSpliceViewGUI.this);
				if(returnVal==JFileChooser.APPROVE_OPTION){
					File inputBamIndex = fileChooser.getSelectedFile();
					
					samRecords = new SAMFileReader(inputBamFile,inputBamIndex);
					samRecords.setValidationStringency(SAMFileReader.ValidationStringency.LENIENT);
					actionWhenBothFilesAreLoaded();
				}					
			}else{
				JOptionPane.showMessageDialog(this,"Sorry, This part of the program has not been implemented yet.","Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		
	}
	private void loadGFFMenuItemAction(){
		int returnVal = fileChooser.showOpenDialog(JSpliceViewGUI.this);
		if (returnVal == JFileChooser.APPROVE_OPTION){
			File file = fileChooser.getSelectedFile();
			GFF3Parser gff3Parser = new GFF3Parser();
			try {
				//Load the file
				BufferedReader in = new BufferedReader(new FileReader(file));
				//Attempt to parse file and tell user if it was successful or not
				
				JOptionPane.showMessageDialog(this,gff3Parser.parse(in),"Error",
						JOptionPane.ERROR_MESSAGE);
				geneRecords=gff3Parser.getGenes();
				// If the gene is null from the parser, that means that there was some problem with parsing
				if(geneRecords==null){
					System.err.println("Parsing Error");
				}else{
					//Fix the Options in the GUI
					//Fill in choices for gene the user can view
					geneChooser.setEnabled(true);
					geneChooser.removeAllItems();
					for(Gene gene:geneRecords.values()){
						geneChooser.addItem(gene.getAttributes().get("Name"));
					}
					applet.loadGene(geneRecords.get(geneChooser.getSelectedItem().toString()));
					
					//Fill in the choices for the Isoforms
					isoformChooser.removeAllItems();
					isoformChooser.addItem("View All");
					for(MRNA mrna:geneRecords.get(geneChooser.getSelectedItem().toString()).getMRNA().values()){
						isoformChooser.addItem(mrna.getId());
					}
					isoformChooser.setEnabled(true);
					
//					//Make sure that the first loaded gene is showing the coding read
//					if(genes.get(geneChooser.getSelectedItem().toString()).getStrand()){
//						applet.setCoding(true);
//					}else{
//						applet.setCoding(false);
//					}
					isCodingCheckBox.setEnabled(true);
					//Allow the user to decided whether or not to view the splice lines
					spliceLinesCheckBox.setEnabled(true);
					actionWhenBothFilesAreLoaded();			
				}
				applet.setGeneVisible(true);
				applet.setSpliceLinesVisible(true);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}	
	}
	private void geneChooserAction(){
		if(filesLoaded()){
			Gene gene = geneRecords.get(geneChooser.getSelectedItem().toString());
			isoformChooser.removeAllItems();
			isoformChooser.addItem("View All");
			for(MRNA mrna:geneRecords.get(geneChooser.getSelectedItem().toString()).getMRNA().values()){
				isoformChooser.addItem(mrna.getId());
			}
			
			weightedIsoformCheckBox.setSelected(false);
			applet.loadGene(gene);
			applet.setGeneVisible(true);
			applet.setWeightedIsoformsVisible(false);
			applet.setErrorBarsVisible(false);
			applet.loadShortReads(getShortReadMatch(gene),gene.getStart(),(gene.getStart()+gene.getLength()-1));	
		}
	}
	private ArrayList<SAMRecord> getShortReadMatch(Gene gene){
		if(filesLoaded()){
			//CloseableIterator<SAMRecord> match = samRecords.query(gene.getSeqID(),gene.getStart(), (gene.getStart()+gene.getLength()-1), true);
			CloseableIterator<SAMRecord> match = samRecords.queryContained(gene.getSeqID(),gene.getStart(), (gene.getStart()+gene.getLength()-1));
			ArrayList<SAMRecord> shortReadRecords = new ArrayList<SAMRecord>();
			while(match.hasNext()){
				shortReadRecords.add(match.next());	
			}
			match.close();
			return shortReadRecords;
		}else{
			System.err.println("SAM Records could not be located");
			return null;
		}
	}
	
	private boolean filesLoaded(){
		if(geneRecords!=null && samRecords!=null){
			return true;
		}else{
			return false;
		}
	}
	private void actionWhenBothFilesAreLoaded(){
		if(filesLoaded()){
			shortReadCheckBox.setEnabled(true);
			weightedIsoformCheckBox.setEnabled(true);
			
			Gene gene = geneRecords.get(geneChooser.getSelectedItem().toString());
			ArrayList<SAMRecord> shortReadRecords = getShortReadMatch(gene);
			applet.loadShortReads(shortReadRecords,gene.getStart(),(gene.getStart()+gene.getLength()-1));
			//Allow the user to decide whether or not to view the shortReads
			//if BAM file and GFF3 are loaded
			if(geneRecords!=null && shortReadRecords!=null){
				shortReadCheckBox.setEnabled(true);
				shortReadCheckBox.setSelected(true);
				weightedIsoformCheckBox.setEnabled(true);
				scaleSlider.setEnabled(true);
			}
		}
	}
	
}
