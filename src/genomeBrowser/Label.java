package genomeBrowser;

public class Label {
	
	private float xScaled;
	private float yScaled;
	private String text;
	public Label(String iText, float iXScaled, float iYScaled){
		text =iText;
		xScaled=iXScaled;
		yScaled=iYScaled;
	}
	public String getText(){
		return text;
	}
	public float getXScaled(){
		return xScaled;
	}
	public float getYScaled(){
		return yScaled;
	}
	

}
