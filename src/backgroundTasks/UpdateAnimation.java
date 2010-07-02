package backgroundTasks;

import genomeBrowser.ProcessingApplet;
import genomeBrowser.ShortRead;
import genomeBrowser.Statistics;
import gffParser.MRNA;

import java.util.ArrayList;
import java.util.List;

import drawableObjects.Rectangle_Weighted;

import processing.core.PApplet;

public class UpdateAnimation implements Runnable{
	private List<Rectangle_Weighted> weightedIsoforms;
	private ArrayList<ShortRead> compatibleShortReads;
	private ProcessingApplet parent;
	private MRNA isoform;
	
	public UpdateAnimation(ProcessingApplet iParent,MRNA iIsoform,ArrayList<ShortRead> iCompatibleShortReads,
			List<Rectangle_Weighted> weightedIsoforms2) {
		compatibleShortReads=iCompatibleShortReads;
		weightedIsoforms=weightedIsoforms2;
		parent = iParent;
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
				parent.loadArrayOfWeightedIsoforms(listOfMRNA);
			}
			synchronized (weightedIsoforms) {
				
				for(int j =0;j<weightedIsoforms.size();j++){
					Rectangle_Weighted rect=weightedIsoforms.get(j);
					Boolean endExon=false;
					if(j==0 | j== weightedIsoforms.size()-1){
						endExon=true;
					}
					float newWeight =Statistics.getWeight(isoform.getExons().get(rect.getAbsoluteStart()),
							compatibleShortReads,endExon);
					float intermediate = PApplet.map(i,0,itr,rect.getWeight(),newWeight);
					rect.setWeight(intermediate);
				}	
			}
				
		}
		ArrayList<MRNA> listOfMRNA = new ArrayList<MRNA>();
		listOfMRNA.add(isoform);
		parent.loadArrayOfWeightedIsoforms(listOfMRNA);
	}
}
