package genomeBrowser;

import gffParser.GFF3Parser;
import gffParser.Gene;
import gffParser.MRNA;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


import net.sf.samtools.*;
import net.sf.samtools.util.CloseableIterator;


public class JSpliceViewGUI extends JPanel implements ActionListener,ChangeListener,
											MouseWheelListener,ListSelectionListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JMenuBar menuBar;
	private JMenu file,help;
	private JMenuItem quit,loadGFFMenuItem,loadBAMMenuItem, saveMenuItem, about;
	private JFileChooser fileChooser;
	private ProcessingApplet applet;
	private HashMap<String,Gene> geneRecords;
	private JCheckBox spliceLinesCheckBox,isCodingCheckBox,shortReadCheckBox;
	private JPanel geneBox,controlBox;
	private JComboBox geneChooser;
	private JSlider slider,scaleSlider;
	private JList multiIsoformChooser;
	private DefaultListModel isoformList;
	private JScrollPane listHolder,appletSlider; 
	private SAMFileReader samRecords;
	private JButton reloadButton,loadWeightedIsoformButton,testButton;
	private int appletWidth =1000;
	
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
		applet = new ProcessingApplet(950, 680);
		applet.addMouseWheelListener(this);
		applet.init();
		
		//appletSlide
		appletSlider=new JScrollPane(applet);
		appletSlider.setPreferredSize(new Dimension(1000,800));
		
		
		//GeneChooser
		geneChooser = new JComboBox();
		geneChooser.setEnabled(false);
		geneChooser.setPreferredSize((new Dimension(150,20)));
		geneChooser.setMaximumSize(new Dimension(150,25));
		geneChooser.setAlignmentX(LEFT_ALIGNMENT);
		geneChooser.addActionListener(this);
		
		//GeneBox
		geneBox = new JPanel();
		geneBox.setLayout(new BoxLayout(geneBox,BoxLayout.Y_AXIS));
		geneBox.add(new JLabel("Choose a Gene"));
		geneBox.add(geneChooser);
		
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

		//loadWeightedIsoformButton
		loadWeightedIsoformButton = new JButton("Show Weighted Isoforms");
		loadWeightedIsoformButton.setEnabled(false);
		loadWeightedIsoformButton.addActionListener(this);
		
		
		//scalingSlider
		scaleSlider = new JSlider(JSlider.HORIZONTAL,1,20,5);
		scaleSlider.setPaintLabels(true);
		scaleSlider.setEnabled(false);
		scaleSlider.addChangeListener(this);
		
		//isoformList
		isoformList = new DefaultListModel();
		isoformList.addElement("View All");
		
		//multiIsoformChooser
		multiIsoformChooser = new JList(isoformList);
		multiIsoformChooser.setAlignmentX(CENTER_ALIGNMENT);
		multiIsoformChooser.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		multiIsoformChooser.setLayoutOrientation(JList.VERTICAL);
		multiIsoformChooser.setEnabled(false);
		multiIsoformChooser.addListSelectionListener(this);
		
		
		//listHolder
		listHolder = new JScrollPane(multiIsoformChooser);
		listHolder.setAlignmentX(CENTER_ALIGNMENT);
		listHolder.setBorder(BorderFactory.createLineBorder(Color.black));
		//listHolder.setPreferredSize(new Dimension(250,200));
		listHolder.setMaximumSize(new Dimension(350,200));
		
		//Reload All Button
		reloadButton = new JButton("Reload");
		reloadButton.setEnabled(false);
		reloadButton.addActionListener(this);
		
		//testButton
		testButton = new JButton("Test");
		testButton.addActionListener(this);
		
		
		//ControlBox
		controlBox = new JPanel();
		controlBox.setPreferredSize(new Dimension(200,getHeight()));
		controlBox.setMinimumSize(new Dimension(200,getHeight()));
		controlBox.setLayout(new BoxLayout(controlBox,BoxLayout.Y_AXIS));
		controlBox.add(spliceLinesCheckBox);
		controlBox.add(isCodingCheckBox);
		controlBox.add(geneBox);
		controlBox.add(reloadButton);
		controlBox.add(shortReadCheckBox);
		controlBox.add(loadWeightedIsoformButton);
		controlBox.add(new JLabel("Scale"));
		controlBox.add(Box.createRigidArea(new Dimension(0,5)));
		controlBox.add(scaleSlider);
		controlBox.add(listHolder);
		controlBox.add(testButton);
		
		
	
		
		add(menuBar,BorderLayout.NORTH);
		add(slider,BorderLayout.SOUTH);
		add(appletSlider,BorderLayout.CENTER);
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
		}else if(e.getSource()==geneChooser){
			geneChooserAction();
		}else if(e.getSource()==loadWeightedIsoformButton){
			loadWeightedIsoformAction();
		}else if(e.getSource()==reloadButton){
			reloadButonAction();
		}else if(e.getSource()==testButton){
//			Gene gene =  geneRecords.get(geneChooser.getSelectedItem().toString());
//			MRNA mrna =gene.getMRNA().get(isoformList.get(multiIsoformChooser.getSelectedIndex()).toString());
//			applet.loadCompatibleReads(mrna, getShortReadMatch(gene));
			appletWidth+=100;
			applet.setNewSize(appletWidth,800);
			Gene gene =  geneRecords.get(geneChooser.getSelectedItem().toString());
			applet.loadShortReads(getShortReadMatch(gene));
			applet.loadWeightsOfCurrentlyShowingIsoforms();
			
		}
	}
	private void loadWeightedIsoformAction() {
		applet.setWeightedIsoformsVisible(true);
		applet.setUnweightedIsoformsVisible(false);
		applet.loadWeightsOfCurrentlyShowingIsoforms();
	}
	public void stateChanged(ChangeEvent e){
		if(e.getSource()==scaleSlider){
			applet.setYScale(scaleSlider.getValue());
		}
	}
	private void multipleIsoformChooserAction() {
		Gene gene =  geneRecords.get(geneChooser.getSelectedItem().toString());
		if(isoformList.getSize()==0||multiIsoformChooser.getSelectedIndex()==0){
			if(multiIsoformChooser.getSelectedIndex()==0){
				applet.loadArrayOfUnweightedIsoforms(gene.getMRNA().values());
				applet.setUnweightedIsoformsVisible(true);
				applet.setWeightedIsoformsVisible(false);
			}
		}else{
			if(filesLoaded()){
				List<MRNA> listOfMRNA = new ArrayList<MRNA>();
				for(Integer index:multiIsoformChooser.getSelectedIndices()){
					if(index==0){
						//Skip
					}else{					
						listOfMRNA.add(gene.getMRNA().get(isoformList.get(index).toString()));
					}
				}
				applet.loadArrayOfWeightedIsoforms(listOfMRNA);
				applet.setUnweightedIsoformsVisible(false);
				applet.setWeightedIsoformsVisible(true);
				
				if(listOfMRNA.size()==1){
					MRNA mrna =listOfMRNA.get(0);
					applet.loadCompatibleReads(mrna, getShortReadMatch(gene));
				}
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
	private void reloadButonAction(){
		Gene gene = geneRecords.get(geneChooser.getSelectedItem().toString());
		multiIsoformChooser.setSelectedIndex(0);
		applet.loadNewArrayOfUnweightedIsoforms(gene.getMRNA().values(),gene.getStart(),gene.getLength(),gene.getStrand());
		applet.setWeightedIsoformsVisible(false);
		applet.setUnweightedIsoformsVisible(true);
		isCodingCheckBox.setSelected(true);
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

					File inputBamIndex = new File(inputBamFile.getAbsolutePath()+".bai");

					if(!inputBamIndex.isFile()){
						returnVal = fileChooser.showOpenDialog(JSpliceViewGUI.this);
						if(returnVal==JFileChooser.APPROVE_OPTION){
							samRecords = new SAMFileReader(inputBamFile,inputBamIndex);
							samRecords.setValidationStringency(SAMFileReader.ValidationStringency.LENIENT);
							actionWhenBothFilesAreLoaded();
						}						
					}else{
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
				HashMap<String, Gene> tempRecords = geneRecords;
				geneRecords=gff3Parser.getGenes();
				// If the gene is null from the parser, that means that there was some problem with parsing
				if(geneRecords==null){
					geneRecords=tempRecords;
					System.err.println("Parsing Error");
				}else{
					//Fix the Options in the GUI
					reloadButton.setEnabled(true);
					
					//Fill in choices for gene the user can view
					geneChooser.setEnabled(true);
					geneChooser.removeAllItems();
					for(Gene gene:geneRecords.values()){
						geneChooser.addItem(gene.getAttributes().get("Name"));
					}
					
					Gene gene = geneRecords.get(geneChooser.getSelectedItem().toString());
					applet.loadNewArrayOfUnweightedIsoforms(gene.getMRNA().values(),gene.getStart(),gene.getLength(),gene.getStrand());
					
					//Fill in the choices for the Isoforms
					isoformList.removeAllElements();
					isoformList.addElement("View All");
					for(MRNA mrna:geneRecords.get(geneChooser.getSelectedItem().toString()).getMRNA().values()){
						isoformList.addElement(mrna.getId());
					}
					isCodingCheckBox.setEnabled(true);
					spliceLinesCheckBox.setEnabled(true);
					actionWhenBothFilesAreLoaded();			
				}
				applet.setUnweightedIsoformsVisible(true);
				applet.setSpliceLinesVisible(true);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}	
	}
	private void geneChooserAction(){
		if(geneRecords!=null){
			Gene gene = geneRecords.get(geneChooser.getSelectedItem().toString());	
			isoformList.removeAllElements();
			isoformList.addElement("View All");
			for(MRNA mrna:geneRecords.get(geneChooser.getSelectedItem().toString()).getMRNA().values()){
				isoformList.addElement(mrna.getId());
			}
			applet.loadNewArrayOfUnweightedIsoforms(gene.getMRNA().values(),gene.getStart(),gene.getLength(),gene.getStrand());
			if(filesLoaded()){	
				applet.setUnweightedIsoformsVisible(true);
				applet.setWeightedIsoformsVisible(false);
				applet.loadShortReads(getShortReadMatch(gene));
				applet.setShortReadsVisible(true);
			}
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
			loadWeightedIsoformButton.setEnabled(true);
			multiIsoformChooser.setEnabled(true);
			
			Gene gene = geneRecords.get(geneChooser.getSelectedItem().toString());
			ArrayList<SAMRecord> shortReadRecords = getShortReadMatch(gene);
			applet.loadShortReads(shortReadRecords);
			applet.setShortReadsVisible(true);
			
			if(geneRecords!=null && shortReadRecords!=null){
				shortReadCheckBox.setEnabled(true);
				shortReadCheckBox.setSelected(true);
				scaleSlider.setEnabled(true);
			}
		}
	}
	public void valueChanged(ListSelectionEvent e) {
		multipleIsoformChooserAction();	
	}
	public void mouseWheelMoved(MouseWheelEvent e) {
		 int notches = e.getWheelRotation();
		 //System.out.println(notches);
		
	}
}
