package gffParser;


public interface IDHolder {
	
	/**
	 * Gets the id(From Attributes) of the GFF3.
	 *
	 * @return the id
	 */
	public String getId();
	
	/**
	 * Adds a child. 
	 * (Genes have mRNA as children)
	 * (mRNA have cds,exons, start and stop codons as children) 
	 *
	 * @param child the child
	 */
	public void addChild(GFF3 child);

}
