package com.cacheserverdeploy.deploy;

public class ConsumeNode implements Comparable<ConsumeNode> {
	public final int consumeIndex;  //消费节点索引
	public final int linknetIndex;  //与消费节点相连的网络节点
	public final int bandwidthNeed; //带宽需求
	
	public ConsumeNode(int consumeIndex, int linknetIndex, int bandwidthNeed) {
		// TODO Auto-generated constructor stub
		this.consumeIndex = consumeIndex;
		this.bandwidthNeed = bandwidthNeed;
		this.linknetIndex = linknetIndex;
	}
	
	@Override
	public int compareTo(ConsumeNode o) {
		// 按照带宽降序对消费节点进行排序
		return o.bandwidthNeed - bandwidthNeed;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "(" + consumeIndex + " " + linknetIndex + " " + bandwidthNeed+ ")";
	}
}
