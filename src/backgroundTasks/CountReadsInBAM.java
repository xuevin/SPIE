package backgroundTasks;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.util.CloseableIterator;

public class CountReadsInBAM implements Runnable{
	private CloseableIterator<SAMRecord> itr;
	private JLabel labelToUpdate;
	private JComboBox comboBoxToSetActivated;
	
	public CountReadsInBAM(SAMFileReader input,JComboBox iComboBoxToSetActivated,JLabel iLabelToUpdate){ 
		itr = input.iterator();
		labelToUpdate=iLabelToUpdate;
		comboBoxToSetActivated=iComboBoxToSetActivated;
	}
	public void run() {
		int count =0;
		
		while(!Thread.interrupted() &&itr.hasNext()){
			count++;
			if(count%100000==0){
				labelToUpdate.setText("Reads:"+count);	
			}
			itr.next();
		}
		itr.close();
		labelToUpdate.setText("Reads:"+count);
		comboBoxToSetActivated.setEnabled(true);
		
	}
}
