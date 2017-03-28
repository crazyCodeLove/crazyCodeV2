package com.cacheserverdeploy.deploy;

public class ConsumeNode implements Comparable<ConsumeNode> {
	public final int consumeIndex;
	public final int linknetIndex;
	public final int bandwidthNeed;
	
	public ConsumeNode(int consumeIndex, int linknetIndex, int bandwidthNeed) {
		// TODO Auto-generated constructor stub
		this.consumeIndex = consumeIndex;
		this.bandwidthNeed = bandwidthNeed;
		this.linknetIndex = linknetIndex;
	}
	
	@Override
	public int compareTo(ConsumeNode o) {
		// TODO Auto-generated method stub
		return o.bandwidthNeed - bandwidthNeed;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "(" + consumeIndex + " " + linknetIndex + " " + bandwidthNeed+ ")";
	}
}
