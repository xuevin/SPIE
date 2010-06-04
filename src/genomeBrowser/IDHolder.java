package genomeBrowser;

import gffParser.GFF3;

public interface IDHolder {
	public String getId();
	public void addChild(GFF3 child);

}
