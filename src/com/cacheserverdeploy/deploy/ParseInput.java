package com.cacheserverdeploy.deploy;

import java.util.*;

public class ParseInput {
	private String[] content;
	private int vertexCount;
	private int linkCount;
	private int consumeCount;
	private int serverCost;
	private Graph linkNetGraph;
	private ArrayList<ConsumeNode> consumeNet;
	
	public ParseInput(String[] content){
		this.content = content;
		vertexCount=0;
		linkCount=0;
		consumeCount = 0;
		serverCost = 0;
		linkNetGraph = null;
		consumeNet = null;
		
		initData();
	}
	
	private void initData(){
		parseHead();
		parseServerCost();
		parseLinkNetGraph();
		parseConsumeNet();
		
	}
	
	private void parseHead(){
		int[] head = parseLine2IntArr(content[0]);
		vertexCount = head[0];
		linkCount = head[1];
		consumeCount = head[2];
	}
	
	private void parseServerCost(){
		serverCost = Integer.parseInt(content[2]);
	}
	
	private void parseLinkNetGraph(){
		/*
		 * 将content内容解析到linkNetGraph对象中
		 */
		linkNetGraph = new Graph(vertexCount);
		int[] data;
		for(int i=0;i<linkCount;i++){
			data = parseLine2IntArr(content[i+4]);
			linkNetGraph.addEdge(data[0], data[1], data[2], data[3]);
			linkNetGraph.addEdge(data[1], data[0], data[2], data[3]);
		}		
		
	}
	
	private void parseConsumeNet(){
		/*
		 * result: 消费节点ID 相连网络节点ID，视频带宽需求
		 */
		consumeNet = new ArrayList<>();
		int start=5+linkCount;
		int[] data;
		for(int i=0;i<consumeCount;i++){
			data = parseLine2IntArr(content[i+start]);
			consumeNet.add(new ConsumeNode(data[0], data[1], data[2]));
		}
		
		Collections.sort(consumeNet);
	}
	
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public static int[] parseLine2IntArr(String content){
		//将一行String内容解析成int数组
		String[] contents = content.split(" ");
		int[] result = new int[contents.length];
		for(int i=0;i<contents.length;i++){
			result[i] = Integer.parseInt(contents[i]);
		}
		return result;
	}

	public int getVertexCount() {
		return vertexCount;
	}

	public int getLinkCount() {
		return linkCount;
	}

	public int getConsumeCount() {
		return consumeCount;
	}

	public int getServerCost() {
		return serverCost;
	}

	public Graph getLinkNetGraph() {
		return linkNetGraph;
	}

	public ArrayList<ConsumeNode> getConsumeNet() {
		return consumeNet;
	}

}
