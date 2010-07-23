package backgroundTasks;

import genomeBrowser.JSpliceViewGUI;
import genomeBrowser.ProcessingApplet;
import genomeBrowser.Statistics;

import java.util.HashMap;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.util.CloseableIterator;

public class CountReadsInBAM implements Runnable{
	private CloseableIterator<SAMRecord> itr;
	private JLabel labelToUpdate;
	private HashMap<SAMFileReader, Integer> hashMapToFill;
	private SAMFileReader samFile;
	private String name;
	private JPanel panelToUpdate;
	private JLabel rpkm;
	private JSpliceViewGUI parent;
	
	
	
	public CountReadsInBAM(SAMFileReader input,SAMFileReader clone,String iName,
			JSpliceViewGUI iParent,HashMap<SAMFileReader, Integer> bamFileCount,JPanel iPanelToUpdate){ 
		itr = clone.iterator();
		panelToUpdate=iPanelToUpdate;
		hashMapToFill=bamFileCount;
		samFile = input;
		name = iName;
		parent = iParent;
	}
	public void run() {
		int count =0;
		labelToUpdate = new JLabel();
		rpkm = new JLabel("\tRPKM: ");
		panelToUpdate.add(labelToUpdate);
		panelToUpdate.add(rpkm);
		while(!Thread.interrupted() &&itr.hasNext()){
			count++;
			if(count%100000==0){
				labelToUpdate.setText(name + ": "+count);	
			}
			itr.next();
		}
		itr.close();
		labelToUpdate.setText(name+": "+count);
		rpkm.setText("\tRPKM: "+ parent.getRPKM(samFile, count));
		hashMapToFill.put(samFile, count);
	}
}
