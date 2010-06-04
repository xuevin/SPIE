package genomeBrowser;

import gffParser.GFF3Parser;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;

import net.sf.samtools.*;


public class LevelA extends JPanel implements ActionListener{
	private JMenuBar menuBar;
	private JMenu menuA;
	private JMenuItem item1,loadGFFMenuItem,loadSAMMenuItem;
	private JFileChooser fileChooser;
	private JPanel controlPanel;
	private JComboBox geneChooser;
	private ProcessingApplet applet;
	
	//Constructor
	public LevelA(){
		setLayout(new BorderLayout());
		fileChooser= new JFileChooser();
		
		//Menu
		menuBar = new JMenuBar();
		menuA = new JMenu("File");
			item1 = new JMenuItem("Item 1");
			menuA.add(item1);
			loadGFFMenuItem = new JMenuItem("Open GFF");
			loadGFFMenuItem.addActionListener(this);
			menuA.add(loadGFFMenuItem);
			loadSAMMenuItem = new JMenuItem("Load Sam/Bam");
			loadSAMMenuItem.addActionListener(this);
			menuA.add(loadSAMMenuItem);
			
		menuBar.add(menuA);
		add(menuBar,BorderLayout.NORTH);
		
		//JScrollBar
		JSlider slider= new JSlider();
		add(slider,BorderLayout.SOUTH);
		
		//Processing Applet
		applet = new ProcessingApplet(800, 800);
		applet.init();
		add(applet,BorderLayout.CENTER);
		
		//Control Panel
		controlPanel = new JPanel();
		controlPanel.setPreferredSize(new Dimension(200,800));
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
		controlPanel.add(new JLabel("Choose a Gene"));
		geneChooser = new JComboBox();
		controlPanel.add(geneChooser);
		controlPanel.add(new JLabel("Choose an Isoform"));
		
		add(controlPanel,BorderLayout.EAST);
	}
	public void actionPerformed(ActionEvent e){
		if(e.getSource()==loadGFFMenuItem){
			int returnVal = fileChooser.showOpenDialog(LevelA.this);
			if (returnVal == JFileChooser.APPROVE_OPTION){
				File file = fileChooser.getSelectedFile();
				GFF3Parser gff3Parser = new GFF3Parser();
				try {
					//Load the file
					BufferedReader in = new BufferedReader(new FileReader(file));
					//Attempt to parse file
					gff3Parser.parse(in);
					for(String geneID:gff3Parser.getGenes().keySet()){
						geneChooser.addItem(geneID);
					}
					//The command below retrieves the current gene's mRNA.
					applet.showNewIsoform(gff3Parser.getGenes().get(geneChooser.getSelectedItem().toString()).getMRNA().values(),
							gff3Parser.getGenes().get(geneChooser.getSelectedItem().toString()).getLength(),
							gff3Parser.getGenes().get(geneChooser.getSelectedItem().toString()).getStart());
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}	
		}else if(e.getSource()==loadSAMMenuItem){
			int returnVal = fileChooser.showOpenDialog(LevelA.this);
			if (returnVal == JFileChooser.APPROVE_OPTION){
				File inputSamOrBamFile = fileChooser.getSelectedFile();
				final SAMFileReader inputSam = new SAMFileReader(inputSamOrBamFile);
//				for (final BAMRecord bamRecord : inputSam) {
//					System.out.print(samRecord.getAlignmentStart());
//					// Convert read name to upper case.
//					//samRecord.ge
//				}
				
			}	
		}
	}
	
}
