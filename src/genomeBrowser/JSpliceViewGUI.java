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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
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
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
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
	private JMenuItem quit,loadGFFMenuItem,loadBAMMenuItem, saveMenuItem, about;
	private JFileChooser fileChooser;
	private ProcessingApplet applet;
	private HashMap<String,Gene> geneRecords;
	private JCheckBox spliceLinesCheckBox,isCodingCheckBox,shortReadCheckBox;
	private JPanel controlBox;
	private JComboBox geneChooser,shortReadChooser,methodComboBox;
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
	private HashMap<SAMFileReader,Integer> bamFileCount;
	private JButton rpkmButton;
	private JRadioButtonMenuItem collapsed_Weighted;
	private JRadioButtonMenuItem collapsed_Unweighted;
	private JRadioButtonMenuItem uncollapsed_Weighted;
	private JRadioButtonMenuItem uncollapsed_Unweighted;
	private JMenu view;
	private JPanel rpkmBox;
	private HashMap<SAMFileReader, String> bamFileName;
	
	//Constructor
	public JSpliceViewGUI(){
		setLayout(new BorderLayout());
		
		fileChooser= new JFileChooser();
		geneRecords=null;
		listOfSamRecords=new ArrayList<SAMFileReader>();
		bamFileCount = new HashMap<SAMFileReader, Integer>();
		bamFileName = new HashMap<SAMFileReader,String>();
		
		//MenuItem for loading GFF
		loadGFFMenuItem = new JMenuItem("Open GFF",'O');
		loadGFFMenuItem.setMnemonic(KeyEvent.VK_O);
		loadGFFMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.META_MASK));
		loadGFFMenuItem.addActionListener(this);
		
		//MenuItem for loading BAM
		loadBAMMenuItem = new JMenuItem("Load Bam",'L');
		loadBAMMenuItem.setMnemonic(KeyEvent.VK_L);
		loadBAMMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.META_MASK));
		loadBAMMenuItem.addActionListener(this);
		
		//MenuItem for saving PNG
		saveMenuItem = new JMenuItem("Save",'s');
		saveMenuItem.setMnemonic(KeyEvent.VK_S);
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
		
		
		collapsed_Weighted = new JRadioButtonMenuItem("Collapsed Weighted");
		collapsed_Weighted.addActionListener(this);
		collapsed_Weighted.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, ActionEvent.META_MASK));
		collapsed_Weighted.setEnabled(false);
		
		collapsed_Unweighted = new JRadioButtonMenuItem("Collapsed Unweighted");
		collapsed_Unweighted.addActionListener(this);
		collapsed_Unweighted.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, ActionEvent.META_MASK));
		collapsed_Unweighted.setEnabled(false);
		
		uncollapsed_Weighted = new JRadioButtonMenuItem("Uncollapsed Weighted");
		uncollapsed_Weighted.addActionListener(this);
		uncollapsed_Weighted.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.META_MASK));
		uncollapsed_Weighted.setEnabled(false);
		
		
		uncollapsed_Unweighted = new JRadioButtonMenuItem("Uncollapsed Unweighted");
		uncollapsed_Unweighted.addActionListener(this);
		uncollapsed_Unweighted.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.META_MASK));
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
		loadWeightedIsoformButton = new JButton("This is a useless button....");
		loadWeightedIsoformButton.setAlignmentX(LEFT_ALIGNMENT);
		loadWeightedIsoformButton.setEnabled(false);
		loadWeightedIsoformButton.addActionListener(this);
		
		
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
		methodComboBox = new JComboBox();
		methodComboBox.setPreferredSize((new Dimension(200,50)));
		methodComboBox.setMaximumSize(new Dimension(200,50));
		methodComboBox.addItem("Coverage Per Exon");
		methodComboBox.addItem("SR / Exon");
		methodComboBox.addItem("RPKM");
		methodComboBox.setAlignmentX(LEFT_ALIGNMENT);
		methodComboBox.addActionListener(this);
		
		//ProgressBar
		bamCounterProgressBar = new JProgressBar();
		bamCounterProgressBar.setAlignmentX(LEFT_ALIGNMENT);
		
		//setCurrentAsConstituitive Button
		shortReadsPlotChooser = new JButton("Load 2 Samples");
		shortReadsPlotChooser.setAlignmentX(LEFT_ALIGNMENT);
		shortReadsPlotChooser.addActionListener(this);
		
		//compatibleShortReadsCheckBox
		compatibleShortReadsCheckBox = new JCheckBox("Overlay Compatible Reads");
		compatibleShortReadsCheckBox.setAlignmentX(LEFT_ALIGNMENT);
		compatibleShortReadsCheckBox.setEnabled(false);
		compatibleShortReadsCheckBox.addActionListener(this);
		//TODO fix checkbox
		
		//Overhang Spinner
		overhangSpinner = new JSpinner(new SpinnerListModel(
				new String[]{"0","1","2","3","4","5","6","7","8","9","10"}));
		overhangSpinner.getModel().setValue("4");
		overhangSpinner.setAlignmentX(LEFT_ALIGNMENT);
		overhangSpinner.addChangeListener(this);
		overhangSpinner.setMaximumSize(new Dimension(60,30));
		overhangSpinner.setEnabled(false);
		
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
		graphBox.add(compatibleShortReadsCheckBox);
		graphBox.add(new JSeparator());
		graphBox.add(rpkmButton);
		graphBox.add(rpkmBox);
		
		
		//tabbedPane
		tabbedPane = new JTabbedPane();
		tabbedPane.setPreferredSize(new Dimension(200,50));
		tabbedPane.addTab("General", controlBox);
		tabbedPane.addTab("Statistics", graphBox);
		
		
		add(menuBar,BorderLayout.NORTH);
		add(applet,BorderLayout.CENTER);
		add(tabbedPane,BorderLayout.EAST);
		//applet
		
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
			applet.setUncollapsed_ShortReadsVisible(shortReadCheckBox.isSelected());
		}else if(e.getSource()==quit){
			System.exit(0);	
		}else if(e.getSource()==geneChooser){
			geneChooserAction();
		}else if(e.getSource()==loadWeightedIsoformButton){
			loadWeightedIsoformAction();
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
		}else if(e.getSource()==shortReadsPlotChooser){
			Gene gene = getCurrentlySelectedGene();
			ArrayList<SAMRecord> sample1 = getShortReadMatch(gene, listOfSamRecords.get(0));
			ArrayList<SAMRecord> sample2 = getShortReadMatch(gene, listOfSamRecords.get(1));
			int sample1Size = bamFileCount.get(listOfSamRecords.get(0));
			int sample2Size = bamFileCount.get(listOfSamRecords.get(1));
			applet.loadUncollapsed_TwoShortReadSamples(sample1, sample1Size, sample2, sample2Size);
		}else if(e.getSource()==rpkmButton){
			for(SAMFileReader samFileReader:bamFileName.keySet()){ 
				if(bamFileCount.get(samFileReader)==null){
					JOptionPane.showMessageDialog(this,"The files have not completed counting","Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}	
			
			//Should catch an error before comming here
			rpkmBox.removeAll();
			for(SAMFileReader samFileReader:bamFileName.keySet()){
				rpkmBox.add(new JLabel(bamFileName.get(samFileReader) + ":" + 
						bamFileCount.get(samFileReader)));
				rpkmBox.add(new JLabel("\tRPKM: " + getRPKM(samFileReader, bamFileCount.get(samFileReader))));
			}
			repaint();
			
			//At most choose 2 short read samples						
			//FIXME Temporarily a test button
//			Gene gene = getCurrentlySelectedGene();
//			applet.setUncollapsed_WeightedIsoformsVisible(false);
//			applet.setUncollapsed_UnweightedIsoformsVisible(false);
//			applet.setCollapsed_UnweightedIsoformsVisible(true);
//			applet.setCollapsed_SpliceLinesVisible(true);
//			applet.loadCollapsed_NewArrayOfUnweightedIsoforms(	gene.getMRNA().values(),
//													gene.getStart(),
//													gene.getLength(),
//													gene.getStrand());
			
//				System.out.println(applet.getRPKM(getShortReadMatch(getCurrentlySelectedGene(), getCurrentlySelectedReader()),
//						bamFileCount.get(getCurrentlySelectedReader()).intValue()));	
		}else if(e.getSource()==shortReadChooser){
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
		}
	}
	private void changeMethod() {
		int overhang = Integer.parseInt(overhangSpinner.getModel().getValue().toString());
		try{
			int readCount = bamFileCount.get(getCurrentlySelectedReader());
			applet.changeMethod(readCount,methodComboBox.getSelectedIndex(),overhang);
			Gene gene = getCurrentlySelectedGene();
			SAMFileReader samRecords = getCurrentlySelectedReader(); 
			
			if(samRecords!=null&&multiIsoformChooser.getSelectedIndex()>1){
				applet.animatedLoadShortReads(getShortReadMatch(gene, samRecords),
						gene.getMRNA().get(isoformList.get(multiIsoformChooser.getSelectedIndex())));	
			}
		}catch(NullPointerException e){
			JOptionPane.showMessageDialog(this,"Normalization Failed\n Please wait until the Short Read count has finished.","Error",
					JOptionPane.ERROR_MESSAGE);
		}	
	}
	private void shortReadChooserAction() {
		if(filesLoaded() &&  shortReadChooser.isEnabled()){
			SAMFileReader samReader = getCurrentlySelectedReader();
			Gene gene = getCurrentlySelectedGene();
			currentShortReadLabel.setText("Sample: "+shortReadChooser.getSelectedItem().toString());
			try{
				readsCounter.setText("Count: " + bamFileCount.get(getCurrentlySelectedReader())+"");	
			}catch(NullPointerException e){
				readsCounter.setText("Count: Still Counting...");
			}
			
			if(multiIsoformChooser.getSelectedIndex()>1 && multiIsoformChooser.getSelectedIndices().length==1){
				applet.animatedLoadShortReads(getShortReadMatch(gene, samReader),
						gene.getMRNA().get(isoformList.get(multiIsoformChooser.getSelectedIndex())));	
			}else{
				applet.loadShortReads(getShortReadMatch(gene, samReader));
			}
		}
		
		
	}
	private void loadWeightedIsoformAction() {
		//FIXME
//		applet.setUncollapsed_WeightedIsoformsVisible(true);
//		applet.setUncollapsed_UnweightedIsoformsVisible(false);
//		applet.loadUncollapsed_WeightedOfCurrentlyShowingIsoforms();
	}
	public void stateChanged(ChangeEvent e){
		if(e.getSource()==scaleSlider){
			applet.setYScale(scaleSlider.getValue());
		}else if (e.getSource()==overhangSpinner){
			if(getCurrentlySelectedMethod()==2){
				int overhang = Integer.parseInt(overhangSpinner.getModel().getValue().toString());
				applet.changeMethod(Integer.parseInt(readsCounter.getText().substring(6)),methodComboBox.getSelectedIndex(),overhang);
				Gene gene = getCurrentlySelectedGene();
				SAMFileReader samRecords = getCurrentlySelectedReader(); 
				
				if(samRecords!=null&&multiIsoformChooser.getSelectedIndex()>1 && multiIsoformChooser.getSelectedIndices().length==1){
					applet.animatedLoadShortReads(getShortReadMatch(gene, samRecords),
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
	private void saveMenuItemAction() {
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
			applet.loadShortReads(getShortReadMatch(gene, getCurrentlySelectedReader()));
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
					
					listOfSamRecords.add(samRecords);
					shortReadChooser.setEnabled(false);
					String name = JOptionPane.showInputDialog(this,"Please Name The Short Reads File", "name");
					shortReadChooser.addItem(name);
					shortReadChooser.setSelectedIndex(listOfSamRecords.size()-1);
					shortReadChooser.setEnabled(true);
					
					currentShortReadLabel.setText("Sample: "+shortReadChooser.getSelectedItem().toString());
					
					bamFileName.put(samRecords,name);
					
					actionWhenBothFilesAreLoaded();
					SAMFileReader clone = new SAMFileReader(inputBamFile,inputBamIndex);
					clone.setValidationStringency(SAMFileReader.ValidationStringency.SILENT);
					new Thread(new CountReadsInBAM(samRecords,clone,name,this, bamFileCount,rpkmBox)).start();
							
					
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
			HashMap<String, Gene> tempRecords = geneRecords;
			
			geneRecords =gff3Parser.getGenes();
			
			// If the gene is null from the parser, that means that there was some problem with parsing
			if(geneRecords==null){
				geneRecords=tempRecords;
				System.err.println("Parsing Error");
			}else{
				//Fix the Options in the GUI
				reloadButton.setEnabled(true);
				uncollapsed_Unweighted.setEnabled(true);
				collapsed_Unweighted.setEnabled(true);
				uncollapsed_Unweighted.setSelected(true);
				
				//Fill in choices for gene the user can view
				geneChooser.setEnabled(false);
				geneChooser.removeAllItems();
				for(Gene gene:geneRecords.values()){
					//Assuming that the name attribute exits
					geneChooser.addItem(gene.getAttributes().get("Name")); 
				}
				geneChooser.setEnabled(true);
				
				Gene gene = getCurrentlySelectedGene();
				applet.loadNewArrayOfIsoforms(gene.getMRNA().values(),gene.getStart(),gene.getLength(),gene.getStrand());
				
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
				actionWhenBothFilesAreLoaded();			
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
				applet.loadShortReads(getShortReadMatch(gene,samRecords));
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
			collapsed_Weighted.setEnabled(true);
			uncollapsed_Weighted.setEnabled(true);
			
			Gene gene = getCurrentlySelectedGene();
			SAMFileReader samRecords = getCurrentlySelectedReader();
			
			//Comparing samples among each other
			if(multiIsoformChooser.getSelectedIndex()>1 && multiIsoformChooser.getSelectedIndices().length==1){
				applet.animatedLoadShortReads(getShortReadMatch(gene, samRecords),
						gene.getMRNA().get(isoformList.get(multiIsoformChooser.getSelectedIndex())));
			}else{
				applet.loadShortReads(getShortReadMatch(gene, samRecords));
			}
			applet.setShortReadsVisible(true);
			shortReadCheckBox.setSelected(true);
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
		if(shortReadChooser.getSelectedIndex()<0){
			System.err.println("There was an error when trying to get the currently selected short read");
			return null;
		}
		if(listOfSamRecords.get(shortReadChooser.getSelectedIndex())!=null){
			return listOfSamRecords.get(shortReadChooser.getSelectedIndex());	
		}else{
			System.err.println("There was an error when trying to get the currently selected short read");
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
	public float getRPKM(SAMFileReader samFileReader,int totalNumberOfReads){
		return applet.getRPKM(getShortReadMatch(getCurrentlySelectedGene(), samFileReader), totalNumberOfReads);
	}
	public void mouseClicked(MouseEvent e) {
		if(e.isMetaDown() && multiIsoformChooser.isEnabled() &&
				uncollapsed_Unweighted.isSelected() ||
				collapsed_Unweighted.isSelected()){
			multiIsoformChooser.setSelectedIndex(1);
		}
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
	
}
