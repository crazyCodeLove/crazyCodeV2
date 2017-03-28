package com.cacheserverdeploy.deploy;

import java.util.ArrayList;

public class SearchTreeSL {
	/*
	 * 一条链路可以满足带宽需求,SL(single line)
	 */
	private static Graph linkNetGraph = Deploy.parseInput.getLinkNetGraph();
	
	private Node root;
	
	// 保存该树的所有节点
	private ArrayList<Node> treeIndexs;
	private ConsumeNode consumeNode;
	
	
	public SearchTreeSL(ConsumeNode comsumeNode){
		this.consumeNode = consumeNode;
		
		root = new Node(linkNetGraph.getVertex(consumeNode.linknetIndex));
		treeIndexs = new ArrayList<Node>();
		treeIndexs.add(root);
	}
	
	
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	

}
