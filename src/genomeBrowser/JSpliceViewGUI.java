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
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

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
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import backgroundTasks.CountReadsInBAM;


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
	private JPanel controlBox;
	private JComboBox geneChooser,shortReadChooser,normalizeComboBox;
	private JSlider scaleSlider;
	private JList multiIsoformChooser;
	private DefaultListModel isoformList;
	private JScrollPane listHolder; 
	private JButton reloadButton,loadWeightedIsoformButton,shortReadsPlotChooser;
	private JProgressBar bamCounterProgressBar;
	private ArrayList<SAMFileReader> listOfSamRecords;
	private JLabel readsCounter;
	private JCheckBox compatibleShortReadsCheckBox;
	private JSpinner overhangSpinner;
	private JTabbedPane tabbedPane;
	private JPanel graphBox;
	private JLabel currentShortReadLabel;
	
	//Constructor
	public JSpliceViewGUI(){
		setLayout(new BorderLayout());
		
		fileChooser= new JFileChooser();
		geneRecords=null;
		listOfSamRecords=new ArrayList<SAMFileReader>();
		
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
		
		//readsCounter
		readsCounter= new JLabel("Reads:0");
		readsCounter.setAlignmentX(LEFT_ALIGNMENT);
		
		//Processing Applet
		applet = new ProcessingApplet(1050, 680);
		applet.addMouseWheelListener(this);
		applet.init();
		
		
		
		//GeneChooser
		geneChooser = new JComboBox();
		geneChooser.setAlignmentX(LEFT_ALIGNMENT);
		geneChooser.setEnabled(false);
		geneChooser.setPreferredSize((new Dimension(150,20)));
		geneChooser.setMaximumSize(new Dimension(150,25));
		geneChooser.addActionListener(this);
		
		
		//SpliceLines Check Box
		spliceLinesCheckBox = new JCheckBox("Show SpliceLine");
		spliceLinesCheckBox.setAlignmentX(LEFT_ALIGNMENT);
		spliceLinesCheckBox.setSelected(true);
		spliceLinesCheckBox.setEnabled(false);
		spliceLinesCheckBox.addActionListener(this);
		
		//Directionality Check Box
		isCodingCheckBox = new JCheckBox("Show Coding");
		isCodingCheckBox.setAlignmentX(LEFT_ALIGNMENT);
		isCodingCheckBox.setSelected(true);
		isCodingCheckBox.setEnabled(false);
		isCodingCheckBox.addActionListener(this);
		
		//ShortReadChooser
		shortReadChooser = new JComboBox();
		shortReadChooser.setEnabled(false);
		shortReadChooser.setPreferredSize((new Dimension(150,20)));
		shortReadChooser.setMaximumSize(new Dimension(150,25));
		shortReadChooser.setAlignmentX(LEFT_ALIGNMENT);
		shortReadChooser.addActionListener(this);
		
		//Short Reads CheckBox
		shortReadCheckBox = new JCheckBox("Show Short Reads");
		shortReadCheckBox.setAlignmentX(LEFT_ALIGNMENT);
		shortReadCheckBox.setEnabled(false);
		shortReadCheckBox.addActionListener(this);

		//loadWeightedIsoformButton
		loadWeightedIsoformButton = new JButton("Show Weighted Isoforms");
		loadWeightedIsoformButton.setAlignmentX(LEFT_ALIGNMENT);
		loadWeightedIsoformButton.setEnabled(false);
		loadWeightedIsoformButton.addActionListener(this);
		
		
		//scalingSlider
		scaleSlider = new JSlider(JSlider.HORIZONTAL,1,100,5);
		scaleSlider.setAlignmentX(LEFT_ALIGNMENT);
		scaleSlider.setPaintLabels(true);
		scaleSlider.setEnabled(false);
		scaleSlider.addChangeListener(this);
		
		//isoformList
		isoformList = new DefaultListModel();
		isoformList.addElement("View All");
		
		//multiIsoformChooser
		multiIsoformChooser = new JList(isoformList);
		multiIsoformChooser.setAlignmentX(LEFT_ALIGNMENT);
		multiIsoformChooser.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		multiIsoformChooser.setLayoutOrientation(JList.VERTICAL);
		multiIsoformChooser.setEnabled(false);
		multiIsoformChooser.addListSelectionListener(this);
		
		
		//listHolder
		listHolder = new JScrollPane(multiIsoformChooser);
		listHolder.setAlignmentX(LEFT_ALIGNMENT);
		listHolder.setBorder(BorderFactory.createLineBorder(Color.black));
		//listHolder.setPreferredSize(new Dimension(250,200));
		listHolder.setMaximumSize(new Dimension(350,200));
		
		//Reload All Button
		reloadButton = new JButton("Reload");
		reloadButton.setAlignmentX(LEFT_ALIGNMENT);
		reloadButton.setEnabled(false);
		reloadButton.addActionListener(this);
		
		//normalizeButton
		normalizeComboBox = new JComboBox();
		normalizeComboBox.setPreferredSize((new Dimension(150,20)));
		normalizeComboBox.setMaximumSize(new Dimension(150,50));
		normalizeComboBox.addItem("Sum of Reads Per Base/ Length of Exon");
		normalizeComboBox.addItem("Sum of Short Reads / Length of Exon - (Length of Short Read-1)");
		normalizeComboBox.addItem("Method 3");
		normalizeComboBox.setAlignmentX(LEFT_ALIGNMENT);
		normalizeComboBox.addActionListener(this);
		
		//ProgressBar
		bamCounterProgressBar = new JProgressBar();
		bamCounterProgressBar.setAlignmentX(LEFT_ALIGNMENT);
		
		//setCurrentAsConstituitive Button
		shortReadsPlotChooser = new JButton("Choose Short Reads");
		shortReadsPlotChooser.setAlignmentX(LEFT_ALIGNMENT);
		shortReadsPlotChooser.addActionListener(this);
		
		//compatibleShortReadsCheckBox
		compatibleShortReadsCheckBox = new JCheckBox("Overlay Compatible Reads");
		compatibleShortReadsCheckBox.setAlignmentX(LEFT_ALIGNMENT);
		compatibleShortReadsCheckBox.setEnabled(false);
		compatibleShortReadsCheckBox.addActionListener(this);
		//TODO fix checkbox
		
		//Overhang Spinner
		overhangSpinner = new JSpinner();
		overhangSpinner.setAlignmentX(LEFT_ALIGNMENT);
		overhangSpinner.addChangeListener(this);
		overhangSpinner.setMaximumSize(new Dimension(60,30));
		overhangSpinner.setEnabled(false);
		//TODO fix changelistener
		
		
		
		//ControlBox
		controlBox = new JPanel();
		controlBox.setPreferredSize(new Dimension(200,getHeight()));
		controlBox.setMinimumSize(new Dimension(200,getHeight()));
		controlBox.setLayout(new BoxLayout(controlBox,BoxLayout.PAGE_AXIS));
		controlBox.setAlignmentX(LEFT_ALIGNMENT);
		controlBox.add(reloadButton);
		controlBox.add(spliceLinesCheckBox);
		controlBox.add(isCodingCheckBox);
		controlBox.add(shortReadCheckBox);
		controlBox.add(new JLabel("Choose a Gene"));
		controlBox.add(geneChooser);
		controlBox.add(new JLabel("Choose Short Reads Sample"));
		controlBox.add(shortReadChooser);
		controlBox.add(new JLabel("Choose an Isoform"));
		controlBox.add(listHolder);
		controlBox.add(loadWeightedIsoformButton);
		controlBox.add(shortReadsPlotChooser);
		
		//currentShortReadLabel
		currentShortReadLabel = new JLabel("Sample:");
		//graphBox
		graphBox = new JPanel();
		graphBox.setLayout(new BoxLayout(graphBox,BoxLayout.Y_AXIS));
		graphBox.setPreferredSize(new Dimension(200,getHeight()));
		graphBox.setMinimumSize(new Dimension(200,getHeight()));
		graphBox.add(currentShortReadLabel);
		graphBox.add(bamCounterProgressBar);
		graphBox.add(readsCounter);
		graphBox.add(normalizeComboBox);
		graphBox.add(new JLabel("Scale"));
		graphBox.add(Box.createRigidArea(new Dimension(0,5)));
		graphBox.add(scaleSlider);
		graphBox.add(new JLabel("Exon Overhang"));
		graphBox.add(overhangSpinner);
		graphBox.add(compatibleShortReadsCheckBox);
		
		
		
		//tabbedPane
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab("General", controlBox);
		tabbedPane.addTab("Statistics", graphBox);
		
		add(menuBar,BorderLayout.NORTH);
		add(applet,BorderLayout.CENTER);
		add(tabbedPane,BorderLayout.EAST);
		
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
		}else if(e.getSource()==normalizeComboBox){
			applet.normalize(Integer.parseInt(readsCounter.getText().substring(6)),normalizeComboBox.getSelectedIndex());
			Gene gene = getCurrentlySelectedGene();
			SAMFileReader samRecords = getCurrentlySelectedReader(); 
			
			if(multiIsoformChooser.getSelectedIndex()>0){
				applet.animatedLoadShortReads(getShortReadMatch(gene, samRecords),
						gene.getMRNA().get(isoformList.get(multiIsoformChooser.getSelectedIndex())));	
			}
			
//			Gene gene =  geneRecords.get(geneChooser.getSelectedItem().toString());
//			MRNA mrna =gene.getMRNA().get(isoformList.get(multiIsoformChooser.getSelectedIndex()).toString());
//			applet.loadCompatibleReads(mrna, getShortReadMatch(gene));
			
			
			
//			appletWidth+=100;
//			applet.setNewSize(appletWidth,800);
//			Gene gene =  geneRecords.get(geneChooser.getSelectedItem().toString());
//			SAMFileReader samRecords = listOfSamRecords.get(shortReadChooser.getSelectedIndex());
//			applet.loadShortReads(getShortReadMatch(gene,samRecords));
//			applet.loadWeightsOfCurrentlyShowingIsoforms();
		}else if(e.getSource()==shortReadsPlotChooser){
								
			
		}else if(e.getSource()==shortReadChooser){
			shortReadChooserAction();
		}
	}
	private void shortReadChooserAction() {
		if(filesLoaded() &&  shortReadChooser.isEnabled()){
			SAMFileReader samReader = getCurrentlySelectedReader();
			Gene gene = getCurrentlySelectedGene();
			currentShortReadLabel.setText("Sample: "+shortReadChooser.getSelectedItem().toString());
			if(multiIsoformChooser.getSelectedIndex()!=0 && multiIsoformChooser.getSelectedIndices().length==1){
				applet.animatedLoadShortReads(getShortReadMatch(gene, samReader),
						gene.getMRNA().get(isoformList.get(multiIsoformChooser.getSelectedIndex())));	
			}else{
				applet.loadShortReads(getShortReadMatch(gene, samReader));
			}
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
				//applet.loadArrayOfUnweightedIsoforms(gene.getMRNA().values());
				applet.loadUnweightedOFCurrentlyShowingIsoforms();
				applet.setUnweightedIsoformsVisible(true);
				applet.setWeightedIsoformsVisible(false);
			}
		}else{
			if(filesLoaded()){
				applet.loadArrayOfWeightedIsoforms(getCurrentlySelectedMRNAs());
				applet.setUnweightedIsoformsVisible(false);
				applet.setWeightedIsoformsVisible(true);
				
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
		applet.clearConstitutive(); 
		isCodingCheckBox.setSelected(true);
	}
	private void loadBAMMenuItemAction() {
		int returnVal = fileChooser.showOpenDialog(JSpliceViewGUI.this);
		if (returnVal == JFileChooser.APPROVE_OPTION){
			File inputBamFile = fileChooser.getSelectedFile();
				//Attempt to automatically find bai, if not found,ask user to select one
				File inputBamIndex = new File(inputBamFile.getAbsolutePath()+".bai");

				if(!inputBamIndex.isFile()){
					int loadIndex = JOptionPane.showConfirmDialog(this, "Please Select a BAM Index","Load BamIndex",JOptionPane.YES_OPTION);
					if (loadIndex == JOptionPane.OK_OPTION){
						returnVal = fileChooser.showOpenDialog(JSpliceViewGUI.this);
						if(returnVal==JFileChooser.APPROVE_OPTION){
							inputBamIndex=fileChooser.getSelectedFile();
						}else{
							return;
						}
					}else{
						JOptionPane.showMessageDialog(this,"A BAM index is needed to continue.","Error",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				try{
					
					SAMFileReader samRecords = new SAMFileReader(inputBamFile,inputBamIndex);
					samRecords.setValidationStringency(SAMFileReader.ValidationStringency.SILENT);					
					
//					int totalNumberOfReadsInBam=0;
//					CloseableIterator<SAMRecord> samIteror=samRecords.iterator();
//					long time=System.currentTimeMillis();
//					while(samIteror.hasNext()){
//						totalNumberOfReadsInBam++;
//						samIteror.next();
//					}
//					samIteror.close();
//					System.out.println(totalNumberOfReadsInBam + " "+ (System.currentTimeMillis()-time));
					
					listOfSamRecords.add(samRecords);
					shortReadChooser.setEnabled(false);
					shortReadChooser.addItem(JOptionPane.showInputDialog(this,"Please Name The Short Reads File", "name"));
					shortReadChooser.setSelectedIndex(listOfSamRecords.size()-1);
					shortReadChooser.setEnabled(true);
					
					currentShortReadLabel.setText("Sample: "+shortReadChooser.getSelectedItem().toString());
					
					actionWhenBothFilesAreLoaded();
					
					//TODO May lead to memory leak....
					SAMFileReader samRecordsCount = new SAMFileReader(inputBamFile,inputBamIndex);
					samRecordsCount.setValidationStringency(SAMFileReader.ValidationStringency.SILENT);
					new Thread(new CountReadsInBAM(samRecordsCount,normalizeComboBox,readsCounter)).start();
					
				}catch(Exception e){
					JOptionPane.showMessageDialog(this,"An error was detected while parsing files","Error",
						JOptionPane.ERROR_MESSAGE);
					System.err.println("Error Parsing BAM Files");
					e.printStackTrace();
				}
								
		}
		
	}
	private void loadGFFMenuItemAction(){
		int returnVal = fileChooser.showOpenDialog(JSpliceViewGUI.this);
		if (returnVal == JFileChooser.APPROVE_OPTION){
			File file = fileChooser.getSelectedFile();
			GFF3Parser gff3Parser = new GFF3Parser();

			JOptionPane.showMessageDialog(this,gff3Parser.parse(file),"Error",
					JOptionPane.ERROR_MESSAGE);
			HashMap<String, Gene> tempRecords = geneRecords;
			 
			//GFF3Loader gff3Loader= new GFF3Loader(in);
//				try {
//					geneRecords=gff3Loader.doInBackground();
//				} catch (Exception e) {
//					System.err.println("Did Not Finish Loading");
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			geneRecords =gff3Parser.getGenes();
			
			// If the gene is null from the parser, that means that there was some problem with parsing
			if(geneRecords==null){
				geneRecords=tempRecords;
				System.err.println("Parsing Error");
			}else{
				//Fix the Options in the GUI
				reloadButton.setEnabled(true);
				
				//Fill in choices for gene the user can view
				geneChooser.setEnabled(false);
				geneChooser.removeAllItems();
				for(Gene gene:geneRecords.values()){
					geneChooser.addItem(gene.getAttributes().get("Name"));
				}
				geneChooser.setEnabled(true);
				
				Gene gene = getCurrentlySelectedGene();
				applet.loadNewArrayOfUnweightedIsoforms(gene.getMRNA().values(),gene.getStart(),gene.getLength(),gene.getStrand());
				
				//Fill in the choices for the Isoforms
				isoformList.removeAllElements();
				isoformList.addElement("View All");
				for(MRNA mrna:gene.getMRNA().values()){
					isoformList.addElement(mrna.getId());
				}
				isCodingCheckBox.setEnabled(true);
				spliceLinesCheckBox.setEnabled(true);
				actionWhenBothFilesAreLoaded();			
			}
			applet.setUnweightedIsoformsVisible(true);
			applet.setWeightedIsoformsVisible(false);
			applet.setSpliceLinesVisible(true);
		}	
	}
	private void geneChooserAction(){
		if(geneRecords!=null && geneChooser.isEnabled()){
			Gene gene = getCurrentlySelectedGene();	
			isoformList.removeAllElements();
			isoformList.addElement("View All");
			for(MRNA mrna:gene.getMRNA().values()){
				isoformList.addElement(mrna.getId());
			}
			applet.loadNewArrayOfUnweightedIsoforms(gene.getMRNA().values(), gene.getStart(), gene.getLength(), gene.getStrand());
			if(filesLoaded()){	
				SAMFileReader samRecords = getCurrentlySelectedReader();
				applet.setUnweightedIsoformsVisible(true);
				applet.setWeightedIsoformsVisible(false);
				applet.loadShortReads(getShortReadMatch(gene,samRecords));
				applet.clearConstitutive();
				applet.setShortReadsVisible(true);
			}
		}
	}
	private ArrayList<SAMRecord> getShortReadMatch(Gene gene,SAMFileReader samReader){
		if(filesLoaded()){
			//CloseableIterator<SAMRecord> match = samRecords.query(gene.getSeqID(),gene.getStart(), (gene.getStart()+gene.getLength()-1), true);
			CloseableIterator<SAMRecord> match = samReader.queryContained(gene.getSeqID(),gene.getStart(), (gene.getStart()+gene.getLength()-1));
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
		if(geneRecords!=null && listOfSamRecords!=null && listOfSamRecords.size()!=0){
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
			
			Gene gene = getCurrentlySelectedGene();
//			ArrayList<SAMRecord> shortReadRecords = getShortReadMatch(gene,samRecords);
//			applet.loadShortReads(shortReadRecords,totalNumberOfReadsInBam);
			SAMFileReader samRecords = getCurrentlySelectedReader();
			if(multiIsoformChooser.getSelectedIndex()!=0 && multiIsoformChooser.getSelectedIndices().length==1){
				applet.animatedLoadShortReads(getShortReadMatch(gene, samRecords),
						gene.getMRNA().get(isoformList.get(multiIsoformChooser.getSelectedIndex())));	
			}else{
				applet.loadShortReads(getShortReadMatch(gene, samRecords));
			}
			applet.setShortReadsVisible(true);
			shortReadCheckBox.setSelected(true);
			scaleSlider.setEnabled(true);
				
		}
	}
	public void valueChanged(ListSelectionEvent e) {
		multipleIsoformChooserAction();	
	}
	public void mouseWheelMoved(MouseWheelEvent e) {
		 int notches = e.getWheelRotation();
		 //System.out.println(notches);
		
	}
	private SAMFileReader getCurrentlySelectedReader(){
		if(listOfSamRecords.get(shortReadChooser.getSelectedIndex())!=null){
			return listOfSamRecords.get(shortReadChooser.getSelectedIndex());	
		}else{
			System.err.println("There was an error when trying to get the currently selected short read");
			return null;
		}
	}
	private Gene getCurrentlySelectedGene(){
		if(geneRecords.get(geneChooser.getSelectedItem().toString())!=null){
			return geneRecords.get(geneChooser.getSelectedItem().toString());
		}else{
			System.err.println("There was an error when trying to get the currently selected Gene");
			return null;
		}
	}
	private ArrayList<MRNA> getCurrentlySelectedMRNAs(){
		Gene gene = getCurrentlySelectedGene();
		ArrayList<MRNA> listOfMRNA = new ArrayList<MRNA>();
		for(Integer index:multiIsoformChooser.getSelectedIndices()){
			if(index==0){
				//Skip
			}else{
				listOfMRNA.add(gene.getMRNA().get(isoformList.get(index).toString()));
			}
		}
		return listOfMRNA;
	}
	public JMenuBar getJMenuBar(){
		return menuBar;
	}
}
