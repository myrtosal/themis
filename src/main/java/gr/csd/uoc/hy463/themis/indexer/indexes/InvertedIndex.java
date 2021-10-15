package gr.csd.uoc.hy463.themis.indexer.indexes;

import java.util.List;

public class InvertedIndex {
	private float df; 
	private float tf; 
	private List<Long> postions;
	public InvertedIndex(float df, float tf, List<Long> postions) {
		this.df = df;
		this.tf = tf;
		this.postions = postions;
	}
	public float getDf() {
		return df;
	}
	public void setDf(float df) {
		this.df = df;
	}
	public float getTf() {
		return tf;
	}
	public void setTf(float tf) {
		this.tf = tf;
	}
	public List<Long> getPostions() {
		return postions;
	}
	public void setPostions(List<Long> postions) {
		this.postions = postions;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(df);
		result = prime * result + ((postions == null) ? 0 : postions.hashCode());
		result = prime * result + Float.floatToIntBits(tf);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InvertedIndex other = (InvertedIndex) obj;
		if (Float.floatToIntBits(df) != Float.floatToIntBits(other.df))
			return false;
		if (postions == null) {
			if (other.postions != null)
				return false;
		} else if (!postions.equals(other.postions))
			return false;
		if (Float.floatToIntBits(tf) != Float.floatToIntBits(other.tf))
			return false;
		return true;
	}
		
}
