package spie;

import gffParser.GFF3Parser;
import gffParser.Gene;
import gffParser.MRNA;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import backgroundTasks.CountReadsInBAM;


import net.sf.samtools.*;
import net.sf.samtools.util.CloseableIterator;

public class JSpliceViewGUI extends JPanel implements ActionListener,ChangeListener,
													ListSelectionListener,MouseListener{
	private static final long serialVersionUID = 1L;
	private JMenuBar menuBar;
	private JMenu file,help;
	private JMenuItem quit,loadGFFMenuItem,loadBAMMenuItem, saveMenuItemPNG, about;
	private JFileChooser fileChooser;
	private ProcessingApplet applet;
	private HashMap<String,Gene> geneRecords;
	private JCheckBox spliceLinesCheckBox,isCodingCheckBox,readCheckBox;
	private JPanel controlBox;
	private JComboBox geneChooser,readChooser,methodComboBox;
	private JSlider scaleSlider;
	private JList multiIsoformChooser;
	private DefaultListModel isoformList;
	private JScrollPane listHolder; 
	private JButton reloadButton,sampleChooser;
	private JProgressBar bamCounterProgressBar;
	private ArrayList<SAMFileReader> listOfSamRecords;
	private JLabel readsCounter;
	private JCheckBox constitutiveRegionsVisibleCheckBox;
	private JSpinner overhangSpinner;
	private JTabbedPane tabbedPane;
	private JPanel graphBox;
	private JLabel currentShortReadLabel;
	private HashMap<SAMFileReader,Integer> bamTo_FileCount;
	private HashMap<SAMFileReader, String> bamTo_FileName;
	private HashMap<String,SAMFileReader> nameTo_BamFile;
	private JButton rpkmButton;
	private JRadioButtonMenuItem collapsed_Weighted;
	private JRadioButtonMenuItem collapsed_Unweighted;
	private JRadioButtonMenuItem uncollapsed_Weighted;
	private JRadioButtonMenuItem uncollapsed_Unweighted;
	private JMenu view;
	private JPanel rpkmBox;
	
	private JMenuItem saveMenuItemPDF;
	private JFrame popupSampleChooser;
	private DefaultListModel listOfBamSamples;
	private JCheckBox multipleReadsVisibleCheckbox;
	private JMenuItem loadDistributionMenuItem;
	
	//Constructor
	public JSpliceViewGUI(){
		setLayout(new BorderLayout());
		
		fileChooser= new JFileChooser();
		geneRecords=null;
		listOfSamRecords=new ArrayList<SAMFileReader>();
		bamTo_FileCount = new HashMap<SAMFileReader, Integer>();
		bamTo_FileName = new HashMap<SAMFileReader,String>();
		nameTo_BamFile = new HashMap<String, SAMFileReader>();
		
		//MenuItem for loading GFF
		loadGFFMenuItem = new JMenuItem("Open GFF",'O');
		loadGFFMenuItem.setMnemonic(KeyEvent.VK_O);
		loadGFFMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		loadGFFMenuItem.addActionListener(this);
		
		//MenuItem for loading BAM
		loadBAMMenuItem = new JMenuItem("Load Bam",'L');
		loadBAMMenuItem.setMnemonic(KeyEvent.VK_L);
		loadBAMMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
		loadBAMMenuItem.addActionListener(this);
		
		//MenuItem for saving PNG
		saveMenuItemPNG = new JMenuItem("Save as PNG",'s');
		saveMenuItemPNG.setMnemonic(KeyEvent.VK_S);
		saveMenuItemPNG.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		saveMenuItemPNG.addActionListener(this);
		
		
		//MenuItem for saving PDF
		saveMenuItemPDF = new JMenuItem("Export as PDF",'e');
		saveMenuItemPDF.setMnemonic(KeyEvent.VK_E);
		saveMenuItemPDF.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
		saveMenuItemPDF.addActionListener(this);
		
		//MenuItem for loading BAM
		loadDistributionMenuItem = new JMenuItem("Load Distribution");
		loadDistributionMenuItem.addActionListener(this);
		
		//MenuItem for Quiting
		quit = new JMenuItem("Quit");
		quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
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
		file.add(saveMenuItemPNG);
		file.add(saveMenuItemPDF);
		file.add(loadDistributionMenuItem);
		file.add(new JSeparator());
		file.add(quit);
		
		
		collapsed_Weighted = new JRadioButtonMenuItem("Collapsed Weighted");
		collapsed_Weighted.addActionListener(this);
		collapsed_Weighted.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, ActionEvent.CTRL_MASK));
		collapsed_Weighted.setEnabled(false);
		
		collapsed_Unweighted = new JRadioButtonMenuItem("Collapsed Unweighted");
		collapsed_Unweighted.addActionListener(this);
		collapsed_Unweighted.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, ActionEvent.CTRL_MASK));
		collapsed_Unweighted.setEnabled(false);
		
		uncollapsed_Weighted = new JRadioButtonMenuItem("Uncollapsed Weighted");
		uncollapsed_Weighted.addActionListener(this);
		uncollapsed_Weighted.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.CTRL_MASK));
		uncollapsed_Weighted.setEnabled(false);
		
		
		uncollapsed_Unweighted = new JRadioButtonMenuItem("Uncollapsed Unweighted");
		uncollapsed_Unweighted.addActionListener(this);
		uncollapsed_Unweighted.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.CTRL_MASK));
		uncollapsed_Unweighted.setSelected(true);
		uncollapsed_Unweighted.setEnabled(false);
		
		ButtonGroup viewGroup = new ButtonGroup();
		viewGroup.add(uncollapsed_Unweighted);
		viewGroup.add(collapsed_Weighted);
		viewGroup.add(collapsed_Unweighted);
		viewGroup.add(uncollapsed_Weighted);
		
		
		view = new JMenu("View");
		view.add(uncollapsed_Unweighted);
		view.add(uncollapsed_Weighted);
		view.add(collapsed_Unweighted);
		view.add(collapsed_Weighted);
		
		
		//MenuBar
		menuBar = new JMenuBar();
		menuBar.add(file);
		menuBar.add(view);
		menuBar.add(help);
		menuBar.requestFocusInWindow();
		
		//readsCounter
		readsCounter= new JLabel("Reads:0");
		readsCounter.setAlignmentX(LEFT_ALIGNMENT);
		
		//Processing Applet
		applet = new ProcessingApplet(1050, 680);
		applet.addMouseListener(this);
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
		
		//readChooser
		readChooser = new JComboBox();
		readChooser.setEnabled(false);
		readChooser.setPreferredSize((new Dimension(150,20)));
		readChooser.setMaximumSize(new Dimension(150,25));
		readChooser.setAlignmentX(LEFT_ALIGNMENT);
		readChooser.addActionListener(this);
		
		//Short Reads CheckBox
		readCheckBox = new JCheckBox("Show Reads");
		readCheckBox.setAlignmentX(LEFT_ALIGNMENT);
		readCheckBox.setSelected(true);
		readCheckBox.setEnabled(false);
		readCheckBox.addActionListener(this);
		
		//scalingSlider
		scaleSlider = new JSlider(JSlider.HORIZONTAL,1,40,5);
		scaleSlider.setAlignmentX(LEFT_ALIGNMENT);
		scaleSlider.setPaintLabels(true);
		scaleSlider.setEnabled(false);
		scaleSlider.addChangeListener(this);
		
		//isoformList
		isoformList = new DefaultListModel();
		isoformList.addElement("View All");
		isoformList.addElement("Custom Selection");
		
		//multiIsoformChooserBox
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
		methodComboBox = new JComboBox();
		methodComboBox.setPreferredSize((new Dimension(200,50)));
		methodComboBox.setMaximumSize(new Dimension(200,50));
		methodComboBox.addItem("Coverage Per Exon");
		methodComboBox.addItem("RPK");
		methodComboBox.addItem("RPKM");
		methodComboBox.setAlignmentX(LEFT_ALIGNMENT);
		methodComboBox.addActionListener(this);
		
		//ProgressBar
		bamCounterProgressBar = new JProgressBar();
		bamCounterProgressBar.setAlignmentX(LEFT_ALIGNMENT);
		
		//sampleList
		listOfBamSamples = new DefaultListModel();
		
		//sampleChooser
		sampleChooser = new JButton("Load Multiple Samples");
		sampleChooser.setAlignmentX(LEFT_ALIGNMENT);
		sampleChooser.addActionListener(this);
		
		//constitutiveRegionsVisibleCheckBox
		constitutiveRegionsVisibleCheckBox = new JCheckBox("Overlay Constitutive Reads");
		constitutiveRegionsVisibleCheckBox.setAlignmentX(LEFT_ALIGNMENT);
		constitutiveRegionsVisibleCheckBox.setEnabled(false);
		constitutiveRegionsVisibleCheckBox.setSelected(true);
		constitutiveRegionsVisibleCheckBox.addActionListener(this);
		
		//Overhang Spinner
		overhangSpinner = new JSpinner(new SpinnerListModel(
				new String[]{"0","1","2","3","4","5","6","7","8","9","10"}));
		overhangSpinner.getModel().setValue("4");
		overhangSpinner.setAlignmentX(LEFT_ALIGNMENT);
		overhangSpinner.addChangeListener(this);
		overhangSpinner.setMaximumSize(new Dimension(60,30));
		overhangSpinner.setEnabled(false);
		
		//multipleReadsVisibleCheckBox
		multipleReadsVisibleCheckbox = new JCheckBox("View Mutliple Short Reads");
		multipleReadsVisibleCheckbox.setEnabled(false);
		multipleReadsVisibleCheckbox.addActionListener(this);
		
		
		//ControlBox
		controlBox = new JPanel();
		controlBox.setPreferredSize(new Dimension(200,getHeight()));
		controlBox.setMinimumSize(new Dimension(200,getHeight()));
		controlBox.setLayout(new BoxLayout(controlBox,BoxLayout.PAGE_AXIS));
		controlBox.setAlignmentX(LEFT_ALIGNMENT);
		controlBox.add(reloadButton);
		controlBox.add(spliceLinesCheckBox);
		controlBox.add(isCodingCheckBox);
		controlBox.add(constitutiveRegionsVisibleCheckBox);
		controlBox.add(readCheckBox);
		controlBox.add(multipleReadsVisibleCheckbox);
		controlBox.add(new JLabel("Choose a Gene"));
		controlBox.add(geneChooser);
		controlBox.add(new JLabel("Choose BAM Sample"));
		controlBox.add(readChooser);
		controlBox.add(new JLabel("Choose an Isoform"));
		controlBox.add(listHolder);
		controlBox.add(sampleChooser);
		
		
		//currentShortReadLabel
		currentShortReadLabel = new JLabel("Sample:");
		
		
		rpkmButton = new JButton("Click to update RPKM");
		rpkmButton.addActionListener(this);
		
		//rpkmBox
		rpkmBox = new JPanel();
		rpkmBox.setLayout(new BoxLayout(rpkmBox,BoxLayout.Y_AXIS));
		
		
		
		//graphBox
		graphBox = new JPanel();
		graphBox.setLayout(new BoxLayout(graphBox,BoxLayout.Y_AXIS));
		graphBox.setPreferredSize(new Dimension(200,getHeight()));
		graphBox.setMinimumSize(new Dimension(200,getHeight()));
		graphBox.add(currentShortReadLabel);
		graphBox.add(bamCounterProgressBar);
		graphBox.add(readsCounter);
		graphBox.add(methodComboBox);
		graphBox.add(new JLabel("Scale"));
		graphBox.add(Box.createRigidArea(new Dimension(0,5)));
		graphBox.add(scaleSlider);
		graphBox.add(new JLabel("Exon Overhang"));
		graphBox.add(overhangSpinner);
		graphBox.add(new JSeparator());
		graphBox.add(rpkmButton);
		graphBox.add(rpkmBox);
		
		
		//tabbedPane
		tabbedPane = new JTabbedPane();
		tabbedPane.setPreferredSize(new Dimension(200,50));
		tabbedPane.addTab("General", controlBox);
		tabbedPane.addTab("Statistics", graphBox);
		
		
		
		//add(menuBar,BorderLayout.NORTH);
		add(applet,BorderLayout.CENTER);
		add(tabbedPane,BorderLayout.EAST);
		//applet
		
	}
	public void actionPerformed(ActionEvent e){
		if(e.getSource()==loadGFFMenuItem){
			loadGFFMenuItemAction();
		}else if(e.getSource()==loadBAMMenuItem){
			loadBAMMenuItemAction();
		}else if(e.getSource()==saveMenuItemPNG){
			saveMenuItemActionPNG();
		}else if(e.getSource()==saveMenuItemPDF){
			saveMenuItemActionPDF();
		}else if(e.getSource()==spliceLinesCheckBox){
			applet.setSpliceLinesVisible(spliceLinesCheckBox.isSelected());
		}else if(e.getSource()==isCodingCheckBox){
			//The Graph Always begins with coding strand.
			applet.flip();
		}else if(e.getSource()==readCheckBox){
			applet.setReadsVisible(readCheckBox.isSelected());
		}else if(e.getSource()==quit){
			System.exit(0);	
		}else if(e.getSource()==geneChooser){
			geneChooserAction();
		}else if(e.getSource()==reloadButton){
			reloadButonAction();
		}else if(e.getSource()==methodComboBox){
			changeMethod();
//			Gene gene =  geneRecords.get(geneChooser.getSelectedItem().toString());
//			MRNA mrna =gene.getMRNA().get(isoformList.get(multiIsoformChooser.getSelectedIndex()).toString());
//			applet.loadCompatibleReads(mrna, getShortReadMatch(gene));
			
				
//			appletWidth+=100;
//			applet.setNewSize(appletWidth,800);
//			Gene gene =  geneRecords.get(geneChooser.getSelectedItem().toString());
//			SAMFileReader samRecords = listOfSamRecords.get(shortReadChooser.getSelectedIndex());
//			applet.loadShortReads(getShortReadMatch(gene,samRecords));
//			applet.loadWeightsOfCurrentlyShowingIsoforms();
		}else if(e.getSource()==sampleChooser){
			sampleChooserAction();			
		}else if(e.getSource()==rpkmButton){
			for(SAMFileReader samFileReader:bamTo_FileName.keySet()){ 
				if(bamTo_FileCount.get(samFileReader)==null){
					JOptionPane.showMessageDialog(this,"The files have not completed counting","Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}	
			
			//Should catch an error before comming here
			rpkmBox.removeAll();
			for(SAMFileReader samFileReader:bamTo_FileName.keySet()){
				rpkmBox.add(new JLabel(bamTo_FileName.get(samFileReader) + ":" + 
						bamTo_FileCount.get(samFileReader)));
				rpkmBox.add(new JLabel("\tRPKM: " + getRPKM(samFileReader, bamTo_FileCount.get(samFileReader))));
			}
			repaint();	
		}else if(e.getSource()==readChooser){
			shortReadChooserAction();
		}else if(e.getSource()==uncollapsed_Unweighted){
			applet.setView(ProcessingApplet.View.UNCOLLAPSED_UNWEIGHTED);
			applet.loadCurrentlyViewingIsoforms();
			applet.loadCurrentlyViewingShortReads();
		}else if(e.getSource()==uncollapsed_Weighted){
			applet.setView(ProcessingApplet.View.UNCOLLAPSED_WEIGHTED);
			applet.loadCurrentlyViewingIsoforms();
			applet.loadCurrentlyViewingShortReads();
		}else if(e.getSource()==collapsed_Weighted){
			applet.setView(ProcessingApplet.View.COLLAPSED_WEIGHTED);
			applet.loadCurrentlyViewingIsoforms();
			applet.loadCurrentlyViewingShortReads();
		}else if(e.getSource() ==collapsed_Unweighted){
			applet.setView(ProcessingApplet.View.COLLAPSED_UNWEIGHTED);
			applet.loadCurrentlyViewingIsoforms();
			applet.loadCurrentlyViewingShortReads();
		}else if(e.getSource()==constitutiveRegionsVisibleCheckBox){
			applet.setConstitutiveRegionsVisible(constitutiveRegionsVisibleCheckBox.isSelected());	
		}else if(e.getSource()==multipleReadsVisibleCheckbox){
			applet.setMultipleReadsVisible(multipleReadsVisibleCheckbox.isSelected());
		}else if(e.getSource()==loadDistributionMenuItem){
			loadDistributionAction();
		}
	}
	private void loadDistributionAction() {
		int returnVal = fileChooser.showOpenDialog(JSpliceViewGUI.this);
		if (returnVal == JFileChooser.APPROVE_OPTION){
			
			File file = fileChooser.getSelectedFile();
			if(file.getName().endsWith(".vito")){
				try{
					ArrayList<Integer> distributionArrayList = new ArrayList<Integer>();
					FileInputStream fis = new FileInputStream(file);
					BufferedInputStream bis = new BufferedInputStream(fis);
					ObjectInputStream ois = new ObjectInputStream(bis);
					distributionArrayList = (ArrayList<Integer>)ois.readObject();
					ois.close();
					applet.loadDistribution(distributionArrayList);
				}catch(Exception e){
					e.printStackTrace();
				}	
			}
		
			
		}
		
	}
	private void sampleChooserAction() {
		if(popupSampleChooser!=null){
			
		}else{
			final JList jListOfSamples = new JList(listOfBamSamples);
			jListOfSamples.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			jListOfSamples.setLayoutOrientation(JList.VERTICAL);
			
			JScrollPane scrollPane = new JScrollPane(jListOfSamples);
			scrollPane.setAlignmentX(CENTER_ALIGNMENT);
			scrollPane.setBorder(BorderFactory.createLineBorder(Color.black));
			scrollPane.setMaximumSize(new Dimension(200,300));
			
			JButton button = new JButton("Load Short Reads");
			button.setAlignmentX(CENTER_ALIGNMENT);
			button.addActionListener(new ActionListener(){ 
				public void actionPerformed(ActionEvent e) {
					Gene gene = getCurrentlySelectedGene();
					if(jListOfSamples.getSelectedIndices().length>1){
						ArrayList<ArrayList<SAMRecord>> listOfSamples = new ArrayList<ArrayList<SAMRecord>>();
						ArrayList<Integer> listOfCounts = new ArrayList<Integer>();
						ArrayList<String> listOfNames = new ArrayList<String>();
						
						//For each selected sample, load the corresponding short reads and pass it
						//into the applet with its count
						for(Object string:jListOfSamples.getSelectedValues()){	
							listOfNames.add((String) string);
							listOfSamples.add(getShortReadMatch(gene, nameTo_BamFile.get(string)));
							listOfCounts.add(bamTo_FileCount.get(nameTo_BamFile.get(string)));	
						}
						applet.loadMultipleSamples(listOfNames,listOfSamples,listOfCounts);
						applet.setMultipleReadsVisible(true);
						multipleReadsVisibleCheckbox.setSelected(true);
						multipleReadsVisibleCheckbox.setEnabled(true);
						
					}else if (jListOfSamples.getSelectedIndices().length==1){
						applet.loadSingleReadSample(
								(String)jListOfSamples.getSelectedValue(),
								getShortReadMatch(gene,nameTo_BamFile.get(jListOfSamples.getSelectedValue()))); 
								
					}else{
						//Do nothing and close
					}
					popupSampleChooser.setVisible(false);
		            popupSampleChooser.dispose();
		            popupSampleChooser=null;
		        }	
			});
			
			JPanel panel = new JPanel();
			
			panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
			panel.add(scrollPane);
			panel.add(button);
			
			popupSampleChooser = new JFrame("Choose The Samples To View");
			popupSampleChooser.setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
			popupSampleChooser.setLocationRelativeTo(null);
			popupSampleChooser.setAlwaysOnTop(true);
			popupSampleChooser.setResizable(false);
			popupSampleChooser.setSize(new Dimension(200,300));
			popupSampleChooser.add(panel);
			popupSampleChooser.setVisible(true);	
		}
		
		
		
		
//		Gene gene = getCurrentlySelectedGene();
//		ArrayList<SAMRecord> sample1 = getShortReadMatch(gene, listOfSamRecords.get(0));
//		ArrayList<SAMRecord> sample2 = getShortReadMatch(gene, listOfSamRecords.get(1));
//		int sample1Size = bamFileCount.get(listOfSamRecords.get(0));
//		int sample2Size = bamFileCount.get(listOfSamRecords.get(1));
//		applet.loadUncollapsed_TwoShortReadSamples(sample1, sample1Size, sample2, sample2Size);
		
	}
	private void saveMenuItemActionPDF() {
		int returnVal = fileChooser.showSaveDialog(JSpliceViewGUI.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            applet.printPDF(file.toString()+".pdf");
        }
	}
	private void changeMethod() {
		int overhang = getCurrentOverhang();
		int readCount = 0;
		if(methodComboBox.getSelectedIndex()!=2){
			//Method 2 is the only one which is normalized
		}else{
			try{
				readCount = bamTo_FileCount.get(getCurrentlySelectedReader());
			}catch(NullPointerException e){
				JOptionPane.showMessageDialog(this,"Normalization Failed\n Please wait until the Short Read count has finished.","Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}	
		}
		Gene gene = getCurrentlySelectedGene();
		SAMFileReader samReader = getCurrentlySelectedReader();
		if(samReader ==null){
			return;
		}
		int readLength = getReadLengthForSample(samReader);
		
		applet.changeMethod(readCount,
							methodComboBox.getSelectedIndex(),
							overhang,
							readLength);
		 
		if(samReader!=null&&multiIsoformChooser.getSelectedIndex()>1){
			applet.animatedLoadReads(
					bamTo_FileName.get(samReader),
					getShortReadMatch(gene, samReader),
					gene.getMRNA().get(isoformList.get(multiIsoformChooser.getSelectedIndex())));	
		}
	}
	private int getCurrentOverhang() {
		return Integer.parseInt(overhangSpinner.getModel().getValue().toString());
	}
	private void shortReadChooserAction() {
		if(filesLoaded() &&  readChooser.isEnabled()){
			SAMFileReader samReader = getCurrentlySelectedReader();
			Gene gene = getCurrentlySelectedGene();
			currentShortReadLabel.setText("Sample: "+readChooser.getSelectedItem().toString());
			try{
				readsCounter.setText("Count: " + bamTo_FileCount.get(getCurrentlySelectedReader())+"");	
			}catch(NullPointerException e){
				readsCounter.setText("Count: Still Counting...");
			}
			
			if(multiIsoformChooser.getSelectedIndex()>1 && multiIsoformChooser.getSelectedIndices().length==1){
				applet.animatedLoadReads(
						bamTo_FileName.get(samReader),
						getShortReadMatch(gene, samReader),
						gene.getMRNA().get(isoformList.get(multiIsoformChooser.getSelectedIndex())));	
			}else{
				applet.loadSingleReadSample(bamTo_FileName.get(samReader),getShortReadMatch(gene, samReader));
			}
		}
		
		
	}
	public void stateChanged(ChangeEvent e){
		if(e.getSource()==scaleSlider){
			applet.setYScale(scaleSlider.getValue());
		}else if (e.getSource()==overhangSpinner){
			if(getCurrentlySelectedMethod()==2){
				int overhang = Integer.parseInt(overhangSpinner.getModel().getValue().toString());
				SAMFileReader samReader = getCurrentlySelectedReader();
				int readLength = getReadLengthForSample(samReader);
				applet.changeMethod(bamTo_FileCount.get(samReader),
									methodComboBox.getSelectedIndex(),
									overhang,readLength);
				Gene gene = getCurrentlySelectedGene();
				 
				if(samReader!=null&&multiIsoformChooser.getSelectedIndex()>1 && multiIsoformChooser.getSelectedIndices().length==1){
					applet.animatedLoadReads(
							bamTo_FileName.get(samReader),
							getShortReadMatch(gene, samReader),
							gene.getMRNA().get(isoformList.get(multiIsoformChooser.getSelectedIndex())));	
				}	
			}
		}
	}
	private void multipleIsoformChooserAction() {
		applet.loadArrayOfIsoforms(getCurrentlySelectedMRNAs());
		applet.loadCurrentlyViewingShortReads();
	}
	
	/**
	 * Action to be performed when save menu is clicked 
	 * This action asks the user to save the current image as a PNG in a directory
	 */
	private void saveMenuItemActionPNG() {
		int returnVal = fileChooser.showSaveDialog(JSpliceViewGUI.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            applet.save(file.toString()+".png");
        }
	}
	private void reloadButonAction(){
		Gene gene = getCurrentlySelectedGene();
		multiIsoformChooser.setSelectedIndex(0);
		applet.clearConstitutive();
		applet.loadNewArrayOfIsoforms(gene.getMRNA().values(),gene.getStart(),gene.getLength(),gene.getStrand());
		if(filesLoaded()){
			applet.loadSingleReadSample(bamTo_FileName.get(getCurrentlySelectedReader()),getShortReadMatch(gene, getCurrentlySelectedReader()));
		}
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
					
					String name = JOptionPane.showInputDialog(this,"Please Name The Short Reads File", "name");
					if ((name != null) && (name.length() > 0)){
						
						while(bamTo_FileName.values().contains(name)){
							name = JOptionPane.showInputDialog(this,"Please Enter a Unique File Name", "name");
						}
						listOfSamRecords.add(samRecords);
						bamTo_FileName.put(samRecords,name);
						nameTo_BamFile.put(name, samRecords);
						
						listOfBamSamples.addElement(name);
						
						
						readChooser.setEnabled(false);
						readChooser.addItem(name);
						readChooser.setSelectedIndex(listOfSamRecords.size()-1);
						readChooser.setEnabled(true);
						
						
						currentShortReadLabel.setText("Sample: "+readChooser.getSelectedItem().toString());
						
						
						
						actionWhenBothFilesAreNewlyLoaded();
						SAMFileReader clone = new SAMFileReader(inputBamFile,inputBamIndex);
						clone.setValidationStringency(SAMFileReader.ValidationStringency.SILENT);
						new Thread(new CountReadsInBAM(samRecords,clone,name,this, bamTo_FileCount,rpkmBox)).start();
								
					}
				}catch(Exception e){
					JOptionPane.showMessageDialog(this,"An error was detected while loading the BAM file","Error",
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

			//Returns a message that describes whether or not the file was parsed correctly
			JOptionPane.showMessageDialog(this,gff3Parser.parse(file),"Error",
					JOptionPane.ERROR_MESSAGE);
			
			HashMap<String, Gene> previousGeneRecords = geneRecords;
			
			//Attempt to get genes from the GFF3 Parser
			geneRecords =gff3Parser.getGenes();
			
			// If the gene is null from the parser, that means that there was some problem with parsing
			if(geneRecords==null){
				geneRecords=previousGeneRecords;
				System.err.println("Parsing Error");
			}else{
				//Fix the Options in the GUI
				reloadButton.setEnabled(true);
				uncollapsed_Unweighted.setEnabled(true);
				collapsed_Unweighted.setEnabled(true);
				constitutiveRegionsVisibleCheckBox.setEnabled(true);
				
				
				
				//Fill in choices for gene the user can view
				geneChooser.setEnabled(false);
				geneChooser.removeAllItems();
				for(String id:geneRecords.keySet()){
					geneChooser.addItem(id);
				}
				
				geneChooser.setEnabled(true);
				
				Gene gene = getCurrentlySelectedGene();
				
				
				//Fill in the choices for the Isoforms
				isoformList.removeAllElements();
				isoformList.addElement("View All");
				isoformList.addElement("Custom Selection");
				for(MRNA mrna:gene.getMRNA().values()){
					isoformList.addElement(mrna.getId());
				}
				
				isCodingCheckBox.setSelected(true);//Always start off loading a coding gene 
				isCodingCheckBox.setEnabled(true);
				spliceLinesCheckBox.setEnabled(true);
				
				applet.loadNewArrayOfIsoforms(gene.getMRNA().values(),gene.getStart(),gene.getLength(),gene.getStrand());
				actionWhenBothFilesAreNewlyLoaded();			
			}
		
			
		}	
	}
	private void geneChooserAction(){
		if(geneRecords!=null && geneChooser.isEnabled()){
			Gene gene = getCurrentlySelectedGene();	
			isoformList.removeAllElements();
			isoformList.addElement("View All");
			isoformList.addElement("Custom Selection");
			for(MRNA mrna:gene.getMRNA().values()){
				isoformList.addElement(mrna.getId());
			}
			applet.clearConstitutive();
			applet.loadNewArrayOfIsoforms(gene.getMRNA().values(), gene.getStart(), gene.getLength(),gene.getStrand());
			if(filesLoaded()){	
				SAMFileReader samRecords = getCurrentlySelectedReader();
				applet.loadSingleReadSample(bamTo_FileName.get(samRecords),getShortReadMatch(gene,samRecords));
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
	private int getReadLengthForSample(SAMFileReader reader){
		CloseableIterator<SAMRecord> itr = reader.iterator();
		
		int readLength = 0;
		while(itr.hasNext()){
			readLength = itr.next().getCigar().getReadLength();
			break;
		}
		itr.close();
		return readLength;
	
	}
	private void actionWhenBothFilesAreNewlyLoaded(){
		if(filesLoaded()){
			
			readCheckBox.setEnabled(true);
			multiIsoformChooser.setEnabled(true);
			collapsed_Weighted.setEnabled(true);
			uncollapsed_Weighted.setEnabled(true);
			
			Gene gene = getCurrentlySelectedGene();
			SAMFileReader samRecords = getCurrentlySelectedReader();
			int readLength = getReadLengthForSample(samRecords);
			
			applet.changeMethod(0,0,getCurrentOverhang(),readLength);
			methodComboBox.setSelectedIndex(0);
			
			//Comparing BAM samples
			if(multiIsoformChooser.getSelectedIndex()>1 && multiIsoformChooser.getSelectedIndices().length==1){
				applet.animatedLoadReads(
						bamTo_FileName.get(samRecords),
						getShortReadMatch(gene, samRecords),
						gene.getMRNA().get(isoformList.get(multiIsoformChooser.getSelectedIndex())));
			}else{
				applet.loadSingleReadSample(bamTo_FileName.get(samRecords),getShortReadMatch(gene, samRecords));
			}
			
			applet.setReadsVisible(true);
			scaleSlider.setEnabled(true);
			overhangSpinner.setEnabled(true);
		}
	}
	public void valueChanged(ListSelectionEvent e) {
		if(multiIsoformChooser.isEnabled()){
			multipleIsoformChooserAction();
		}
			
	}
	/**
	 * Gets the currently selected SAMFileReader.
	 * 
	 * @return the currently selected reader (users refer to this as a sample)
	 */
	private SAMFileReader getCurrentlySelectedReader(){
		if(readChooser.getSelectedIndex()<0){
			System.err.println("There was an error when trying to get the currently selected short read");
			return null;
		}
		if(listOfSamRecords.get(readChooser.getSelectedIndex())!=null){
			return listOfSamRecords.get(readChooser.getSelectedIndex());	
		}else{
			System.err.println("There was an error when trying to get the currently selected SamReader");
			return null;
		}
	}
	
	/**
	 * Gets the currently selected gene.
	 * 
	 * @return the currently selected gene
	 */
	private Gene getCurrentlySelectedGene(){
		if(geneChooser.getSelectedIndex()>=0){
			if(geneRecords.get(geneChooser.getSelectedItem().toString())!=null){
				return geneRecords.get(geneChooser.getSelectedItem().toString());
			}else{
				System.err.println("There was an error when trying to get the currently selected Gene");
				return null;
			}	
		}else{
			System.err.println("There was an error when trying to get the currently selected Gene");
			return null;
		}
		
	}
	/**
	 * Gets the currently selected method for normalization
	 * 
	 * @return the currently selected method
	 */
	private int getCurrentlySelectedMethod(){
		return methodComboBox.getSelectedIndex();
	}
	/**
	 * Gets an ArrayList of the currently selected MRNA.
	 * If the selected index is zero, it returns all of the isoforms
	 * 
	 * @return the currently selected MRNAs in multiIsoformChooser
	 */
	private ArrayList<MRNA> getCurrentlySelectedMRNAs(){
		Gene gene = getCurrentlySelectedGene();
		ArrayList<MRNA> listOfMRNA = new ArrayList<MRNA>();
		for(Integer index:multiIsoformChooser.getSelectedIndices()){
			if(index==0){
				for(MRNA mrna:gene.getMRNA().values()){
					listOfMRNA.add(mrna);
				}
				return listOfMRNA;
			}else if (index==1){
				return applet.getCustomListOfIsoforms();
			}else{
				listOfMRNA.add(gene.getMRNA().get(isoformList.get(index).toString()));
			}
		}
		return listOfMRNA;
	}
	public double getRPKM(SAMFileReader samFileReader,int totalNumberOfReads){
		return applet.getRPKM(getShortReadMatch(getCurrentlySelectedGene(), samFileReader), totalNumberOfReads);
	}
	public void mouseClicked(MouseEvent e) {
		if(e.isMetaDown() && multiIsoformChooser.isEnabled() &&
				(uncollapsed_Unweighted.isSelected() ||
				collapsed_Unweighted.isSelected())){
			multiIsoformChooser.setSelectedIndex(1);
		}
		menuBar.requestFocusInWindow();
	}
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	public JMenuBar getJMenuBar() {
		return menuBar;
	}
	
}
