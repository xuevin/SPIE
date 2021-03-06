package backgroundTasks;

import gffParser.MRNA;

import java.util.ArrayList;
import java.util.List;

import drawableObjects.Rectangle_Weighted;

import processing.core.PApplet;
import spie.ProcessingApplet;
import spie.Read;
import spie.Statistics;

/**
 * This is a thead used to update the animations when a new sample is loaded or 
 * when the user switches between samples
 * 
 * @author Vinny
 *
 */
public class UpdateAnimation implements Runnable{
	private List<Rectangle_Weighted> weightedIsoforms;
	private ArrayList<Read> compatibleShortReads;
	private ProcessingApplet parent;
	private MRNA isoform;
	
	/**
	 * Instantiates a new update animation thread.
	 *
	 * @param processingApplet the parent ProcessingApplet that called it 
	 * 							(Allows the thread to call the "loadIsoforms" function"
	 * @param iIsoform the isoform that the user is currently viewing.
	 * @param iCompatibleShortReads the ArrayList of Compatible reads for the new sample
	 * @param weightedIsoforms2 the ArrayList of weightedIsoforms that the parent ProcessingApplet draws
	 */
	public UpdateAnimation(ProcessingApplet processingApplet,MRNA iIsoform,ArrayList<Read> iCompatibleShortReads,
			List<Rectangle_Weighted> weightedIsoforms2) {
		compatibleShortReads=iCompatibleShortReads;
		weightedIsoforms=weightedIsoforms2;
		parent = processingApplet;
		isoform = iIsoform;		
	}
	public void run() {
		int itr = 30;
		for(int i =0;i<itr;i++){
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				ArrayList<MRNA> listOfMRNA = new ArrayList<MRNA>();
				listOfMRNA.add(isoform);
				parent.loadArrayOfIsoforms(listOfMRNA);
			}
			synchronized (weightedIsoforms) {
				
				for(int j =0;j<weightedIsoforms.size();j++){
					Rectangle_Weighted rect=weightedIsoforms.get(j);
					Boolean endExon=false;
					if(j==0 | j== weightedIsoforms.size()-1){
						endExon=true;
					}
					double newWeight =Statistics.getWeightOfExon(isoform.getExons().get(rect.getAbsoluteStart()),
							compatibleShortReads,endExon);
					double intermediate = PApplet.map(i,0,itr,(float)rect.getWeight(),(float)newWeight);
					rect.setWeight(intermediate);
				}	
			}
				
		}
		ArrayList<MRNA> listOfMRNA = new ArrayList<MRNA>();
		listOfMRNA.add(isoform);
		parent.loadArrayOfIsoforms(listOfMRNA);
	}
}
