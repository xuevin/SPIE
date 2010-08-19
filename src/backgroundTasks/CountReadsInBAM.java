package backgroundTasks;


import java.util.HashMap;
import javax.swing.JLabel;
import javax.swing.JPanel;

import spie.JSpliceViewGUI;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.util.CloseableIterator;

/**
 * CountReadsInBam is a class that counts the number of reads in a BAM file.
 * It takes in two SAMFileReaders because iterating through a BAM 
 * can take a while and cannot be interupted.
 * 
 * Instead, the program counts a clone which increases responsivness while users use other features.
 * When it completes counting, it updates a JPanel and puts the count in a HashMap,
 * specified by the programer.
 * 
 * @author Vincent Xue
 *
 */
public class CountReadsInBAM implements Runnable{
	private CloseableIterator<SAMRecord> itr;
	private JLabel labelToUpdate;
	private HashMap<SAMFileReader, Integer> hashMapToFill;
	private SAMFileReader samFile;
	private String name;
	private JPanel panelToUpdate;
	private JLabel rpkm;
	private JSpliceViewGUI parent;
	private SAMFileReader clone;
	

	/**
	 * Instantiates a thread to count the BAM file
	 *
	 * @param input the original SAMFileReader 
	 * @param iClone the cloneFileReader
	 * @param iName the name of the Sample
	 * @param iParent the parent JSpliceViewGui that called it. (This is to get the RPKM function) 
	 * @param bamFileCountHash the hash that contains the count of the file. 
	 * @param iPanelToUpdate the Jpanel to update the count
	 */
	public CountReadsInBAM(SAMFileReader input,SAMFileReader iClone,String iName,
			JSpliceViewGUI iParent,HashMap<SAMFileReader, Integer> bamFileCountHash,JPanel iPanelToUpdate){ 
		clone = iClone;
		itr = clone.iterator();
		panelToUpdate=iPanelToUpdate;
		hashMapToFill=bamFileCountHash;
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
		clone.close();
	}
}
