package com.cacheserverdeploy.deploy;

import java.util.*;

public class SearchTreeSL {
	/*
	 * 一条链路可以满足带宽需求,SL(single line)
	 */
	private static Graph linkNetGraph = Deploy.parseInput.getLinkNetGraph();
	private static final int serverCost = Deploy.parseInput.getServerCost();
	private static ArrayList<ConsumeNode> consumeNet = Deploy.parseInput.getConsumeNet();
	
	private Node root;
	
	// 与该树相连的消费节点
	private ConsumeNode consumeNode;
	
	
	public SearchTreeSL(ConsumeNode consumeNode){
		this.consumeNode = consumeNode;
		
		Vertex t = linkNetGraph.getVertex((consumeNode.linknetIndex));
		root = new Node(t);
		t.setVisited(true);
		
	}
	
	public void showTreeDFS(){
		/**深度优先显示树**/
		Node pointer = root;
		ArrayDeque<Node> nodeStack = new ArrayDeque<>();
		
		while(pointer!=null || !(nodeStack.isEmpty())){
			if(pointer!=null){
				System.out.print(pointer.getVertexIndex() + " ");
				nodeStack.push(pointer);
				pointer = pointer.getFirstChild();
			} else {
				pointer = nodeStack.pop().getNextSlibing();				
			}
		}
		
	}
	
	public void searchTreeHelper(Node root,HashMap<Integer, Node> candidateMap){
		// 构造搜索树
		Node parent = root, newNode;
		//cost(12 bit)/parentIndex(10 bit)/candidateIndex(10 bit)
		getAndUpdateCandidateMap(parent, candidateMap);
		do {
			newNode = selectCandidateIndexAndUpdateTree(candidateMap);
			if (newNode != null) {
				updateJobAfterTreeAddNode(newNode, candidateMap);
				parent = newNode;
				getAndUpdateCandidateMap(parent, candidateMap);
			}
			
		} while (!candidateMap.isEmpty());
		
	}
	
	public void clearVisited(){
		/**深度优先清除树的访问节点**/
		Node pointer = root;
		ArrayDeque<Node> nodeStack = new ArrayDeque<>();
		
		while(pointer!=null || !(nodeStack.isEmpty())){
			if(pointer!=null){
				pointer.getVertex().clearVisited();
				nodeStack.push(pointer);
				pointer = pointer.getFirstChild();
			} else {
				pointer = nodeStack.pop().getNextSlibing();				
			}
		}
	}
	
	public void getAndUpdateCandidateMap(Node root, HashMap<Integer, Node> candidateMap) {
		//获取与该节点相连的所有可选节点，并添加进candidateMap
		//获取所有与root相连的节点
		LinkedList<Edge> edges = linkNetGraph.getAdjEdges(root.getVertex());

		Iterator<Edge> iterator = edges.iterator();
		Edge tedge;
		int cost=0,candidateindex=0,nodeRentCost=0;
		while (iterator.hasNext()) {
			tedge = iterator.next();
			nodeRentCost = root.getCost() + tedge.getRentcost();
			cost = nodeRentCost * consumeNode.bandwidthNeed;
			
			//判断候选点是否满足要求：带宽满足需求/不在treeIndexs/费用不超过服务器成本
			if(tedge.getBandwidth() >= consumeNode.bandwidthNeed 
					&& !(tedge.getEndVertex().isVisited()) 
					&& cost<serverCost){
				
				//candidateindex(32 bit): cost(12 bit)/parentIndex(10 bit)/candidateIndex(10 bit)
				candidateindex = (((nodeRentCost<<10) + root.getVertexIndex())<<10) + tedge.getEndVertex().index;
				candidateMap.put(candidateindex, root);
			}
		}
	}
	
	public Node selectCandidateIndexAndUpdateTree(HashMap<Integer, Node> candidateMap) {
		//获取cost最小的候选节点，并添加进tree中, 返回最新节点
		if (!candidateMap.isEmpty()){
			Integer[] myIntegers = candidateMap.keySet().toArray(new Integer[0]);
			
			int index = DatastructureTools.getMin(myIntegers);
			Node parent = candidateMap.get(index);
			
			//更新tree的结构
			//candidateindex cost(12 bit)/parentIndex(10 bit)/candidateIndex(10 bit)
			Vertex newVertex = linkNetGraph.getVertex((index<<22)>>>22);
			newVertex.setVisited(true);
			
			Node childNode = new Node(newVertex,(index>>>20),parent);
			if (parent.getFirstChild() == null) {
				parent.setFirstChild(childNode);
			}else {
				Node tNode = parent.getFirstChild();
				while (tNode.getNextSlibing() != null) {
					tNode = tNode.getNextSlibing();	
				}
				tNode.setNextSlibing(childNode);
			}
			return childNode;
		}
		
		return null;
	}
	
	public void updateJobAfterTreeAddNode(Node newNode, HashMap<Integer, Node> candidateMap) {
		//去除所有可选节点为上一次添加的新节点
		//新节点index添加进treeindexs
		//更新linknet带宽
		//cost(12 bit)/parentIndex(10 bit)/candidateIndex(10 bit)
		Iterator<Map.Entry<Integer, Node>> iterator = candidateMap.entrySet().iterator();
		int newindex = newNode.getVertexIndex();
		int parentindex = newNode.getParent().getVertexIndex();
		int candidateindex=0;
		while (iterator.hasNext()) {
			Map.Entry<Integer, Node> entry = iterator.next();
			candidateindex = (entry.getKey()<<22)>>>22;
			
			if(candidateindex == newindex)
				iterator.remove();
		}
		linkNetGraph.cutdownBandwidth(parentindex, newindex, consumeNode.bandwidthNeed);
	}
	
	public Node getRoot() {
		return root;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public static void deploySLTree(){
		ArrayList<SearchTreeSL> allSLTree = new ArrayList<>();
		SearchTreeSL tTree ;
		Iterator<ConsumeNode> iterator = consumeNet.iterator();
		ConsumeNode consumeNode ;
		while(iterator.hasNext()){
			consumeNode = iterator.next();
			tTree = new SearchTreeSL(consumeNode);
			tTree.searchTreeHelper(tTree.getRoot(), new HashMap<Integer,Node>());
			
			tTree.clearVisited();
			
			tTree.showTreeDFS();
			System.out.println();
		}
	}

}
