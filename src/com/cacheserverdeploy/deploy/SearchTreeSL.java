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
	private Node serverNode;
	
	
	// 与该树相连的消费节点
	private ConsumeNode consumeNode;
	
	
	public SearchTreeSL(ConsumeNode consumeNode){
		this.consumeNode = consumeNode;
		
		Vertex t = linkNetGraph.getVertex((consumeNode.linknetIndex));
		root = new Node(t);
		t.setVisited(true);
		
	}
	
	public void TreeDFS(TreeStructure treeObj){
		/**深度优先遍历树**/
		Node pointer = root;
		ArrayDeque<Node> nodeStack = new ArrayDeque<>();
		
		while(pointer!=null || !(nodeStack.isEmpty())){
			if(pointer!=null){
				treeObj.visitNode(pointer);
				nodeStack.push(pointer);
				pointer = pointer.getFirstChild();
			} else {
				pointer = nodeStack.pop().getNextSlibing();
			}
		}
		
	}
	
	public Node getNode(final int index){
		Node pointer = root;
		ArrayDeque<Node> nodeStack = new ArrayDeque<>();
		
		while(pointer!=null || !(nodeStack.isEmpty())){
			if(pointer!=null){
				/**深度优先遍历树,访问代码放在此处**/
				if(pointer.getVertexIndex() == index)
					return pointer;
				nodeStack.push(pointer);
				pointer = pointer.getFirstChild();
			} else {
				pointer = nodeStack.pop().getNextSlibing();
			}
		}
		return null;
	}	

	public Node getServerNode() {
		return serverNode;
	}

	public void setServerNode(Node serverNode) {
		this.serverNode = serverNode;
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
		//添加该节点到treeNodes
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

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return root.getVertexIndex();
	}
	
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return root.getVertexIndex() == ((SearchTreeSL)obj).getRoot().getVertexIndex();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public static ArrayList<String> deploySLTree(){
		ArrayList<SearchTreeSL> allSLTree = new ArrayList<>();
		SearchTreeSL tTree ;
		Iterator<ConsumeNode> iterator = consumeNet.iterator();
		ConsumeNode consumeNode ;
		while(iterator.hasNext()){
			consumeNode = iterator.next();
			tTree = new SearchTreeSL(consumeNode);
			tTree.searchTreeHelper(tTree.getRoot(), new HashMap<Integer,Node>());
			tTree.TreeDFS(new TreeClearVisited());
			
//			tTree.TreeDFS(new TreeShow());
//			System.out.println();
			allSLTree.add(tTree);
			
		}
		allSLTree.trimToSize();
		
		return allTrees22Part(allSLTree);
	}
	
	public static ArrayList<String> allTrees22Part(ArrayList<SearchTreeSL> allTrees) {
		//将所有树分成两部分，一部分可以优化，一部分不可以优化
		ArrayList<String> result = new ArrayList<String>();
		
		// canoptimizeTree 统计每颗树可以在哪些节点上部署,{节点index:树...,};记录可以优化的树
		HashMap<Integer, LinkedList<SearchTreeSL>> canOptimizeTree = new HashMap<Integer, LinkedList<SearchTreeSL>>();
		
		//保存最优的服务器index和对应的树列表
		HashMap<Integer, LinkedList<SearchTreeSL>> resultTreeIndexMap = new HashMap<Integer, LinkedList<SearchTreeSL>>();
		calculateTreeIndex(allTrees, canOptimizeTree);
		
		//noOptimizeTree 保存没有交点的树
		HashSet<SearchTreeSL> noOptimizeTree = new HashSet<SearchTreeSL>();
		int minIndex=0;
		LinkedList<SearchTreeSL> optimizeTreeList;
		
		Integer[] sortedindexAndTreeInfo = getSortedIndexAndTreeInfo(noOptimizeTree, canOptimizeTree);		
		do  {
			// Integer(32): 该节点树的个数(16 bit) 该节点的索引index(16 bit)
			minIndex=getMinCostIndex(sortedindexAndTreeInfo, canOptimizeTree);
			optimizeTreeList = canOptimizeTree.get(minIndex);
			resultTreeIndexMap.put(minIndex, optimizeTreeList);
			canOptimizeTree.remove(minIndex);
			removeMinCostTrees(optimizeTreeList, canOptimizeTree, noOptimizeTree);
			sortedindexAndTreeInfo = getSortedIndexAndTreeInfo(noOptimizeTree, canOptimizeTree);
		} while (!canOptimizeTree.isEmpty());
		
		
		fillResult(result, noOptimizeTree, resultTreeIndexMap);
		
		
		return result;
		
	}
	
	public static void calculateTreeIndex(
			ArrayList<SearchTreeSL> allTrees, HashMap<Integer, LinkedList<SearchTreeSL>> canoptimizeTree) {
		// canoptimizeTree 统计每颗树可以在哪些节点上部署,{节点Node:树...,};记录可以优化的树
		//遍历树
		Iterator<SearchTreeSL> iterator = allTrees.iterator();
		SearchTreeSL tSLTree;
		
		while (iterator.hasNext()) {
			tSLTree = iterator.next();
			//遍历树上的节点
			
			Node pointer = tSLTree.getRoot();
			ArrayDeque<Node> nodeStack = new ArrayDeque<>();
			
			while(pointer!=null || !(nodeStack.isEmpty())){
				if(pointer!=null){
					/**深度优先遍历树，访问代码在此处**/
					if(canoptimizeTree.keySet().contains(pointer.getVertexIndex())){
						canoptimizeTree.get(pointer.getVertexIndex()).add(tSLTree);
						
					}else{
						LinkedList<SearchTreeSL> treelist = new LinkedList<SearchTreeSL>();
						treelist.add(tSLTree);
						canoptimizeTree.put(pointer.getVertexIndex(), treelist);
					}
					
					nodeStack.push(pointer);
					pointer = pointer.getFirstChild();
				} else {
					pointer = nodeStack.pop().getNextSlibing();				
				}
			}
			
		}
		//遍历树结束
		
	}
	
	public static Integer[] getSortedIndexAndTreeInfo(
			HashSet<SearchTreeSL> noOptimizeTree, HashMap<Integer, LinkedList<SearchTreeSL>> canoptimizeTree) {
		// 将canoptimizeTree集合中, 节点上tree的数量 >= 2的加进 canoptimizeTree, 否则加进 noOptimizeTree,处理的时候优先处理canoptimizeTree
		// 所有可以优化的树处理完以后，从集合noOptimizeTree 去除 canoptimizeTree
		// Integer(32): 该节点树的个数(16 bit) 该节点的索引index(16 bit)
		// maxConsumeCount 服务器部署在该节点可以提供最多的消费节点数
		// sortedindexAndTreeInfo 只记录可以提供最多消费节点的 链路网络节点的index
		LinkedList<Integer> indexAndTreeInfo = new LinkedList<Integer>();
		Iterator<Map.Entry<Integer, LinkedList<SearchTreeSL>>> iterator = canoptimizeTree.entrySet().iterator();
		Map.Entry<Integer, LinkedList<SearchTreeSL>> tEntry;
		
		int size ;
		int maxConsumeCount = -1;
		while (iterator.hasNext()) {
			tEntry = iterator.next();
			size = tEntry.getValue().size();
			if (size<2) {
				noOptimizeTree.addAll(tEntry.getValue());
				iterator.remove();
			} else {
				if(size>maxConsumeCount){
					maxConsumeCount = size;
					indexAndTreeInfo.clear();
					indexAndTreeInfo.add( tEntry.getKey());
				} else if(size == maxConsumeCount){
					indexAndTreeInfo.add( tEntry.getKey());
				}
			}
			
		}
		Integer[] sortedindexAndTreeInfo = indexAndTreeInfo.toArray(new Integer[0]);
		
		return sortedindexAndTreeInfo;
	}
	
	public static int getMinCostIndex(
			Integer[] sortedindexAndTreeInfo, HashMap<Integer, LinkedList<SearchTreeSL>> canoptimizeTree) {
		//获取相交节点最多，连接成本最小的，节点索引index
		int index=0,minindex=0,cost,mincost = Integer.MAX_VALUE;
		
		// sortedindexAndTreeInfo 只记录可以提供最多消费节点的 链路网络节点的index
		for(int i=0; i<sortedindexAndTreeInfo.length; i++){
			index = sortedindexAndTreeInfo[i];
			cost = getLinkCostByTrees(index, canoptimizeTree.get(index));
			if (cost < mincost) {
				mincost = cost;
				minindex = index;
			}
		}
		
		Iterator<SearchTreeSL> iterator = canoptimizeTree.get(minindex).iterator();
		SearchTreeSL optimizeTree;
		while(iterator.hasNext()){
			optimizeTree = iterator.next();
			optimizeTree.setServerNode(optimizeTree.getNode(minindex));			
		}
		
		return minindex;
	}
	
	public static int getLinkCostByTrees(int nodeIndex, LinkedList<SearchTreeSL> trees) {
		int sum=0;
		Iterator<SearchTreeSL> iterator = trees.iterator();
		SearchTreeSL tTree;
		while (iterator.hasNext()) {
			tTree = iterator.next();
			sum += tTree.consumeNode.bandwidthNeed * tTree.getNode(nodeIndex).getCost();
		}
		return sum;
	}

	public static void removeMinCostTrees(
			LinkedList<SearchTreeSL> optimizeTrees, 
			HashMap<Integer, LinkedList<SearchTreeSL>> canOptimizeTree,
			HashSet<SearchTreeSL> noOptimizeTree) {
		// 将optimizeTrees从canOptimizeTree中全部移除
		LinkedList<SearchTreeSL> tSearchTreeSLs;
		for(int i: canOptimizeTree.keySet()){
			tSearchTreeSLs = canOptimizeTree.get(i);
			tSearchTreeSLs.removeAll(optimizeTrees);
			canOptimizeTree.put(i, tSearchTreeSLs);//将删除过后的{Integer:LinkedList<SearchTreeSL>}覆盖掉以前的键值对
		}
		noOptimizeTree.removeAll(optimizeTrees);
		
	}
	
	public static void fillResult(
			ArrayList<String> result, HashSet<SearchTreeSL> noOptimizeTree, 
			HashMap<Integer, LinkedList<SearchTreeSL>> resultTreeIndexMap) {
		
		Iterator<SearchTreeSL> iterator = noOptimizeTree.iterator();
		SearchTreeSL treev2;
		while (iterator.hasNext()) {
			treev2 = iterator.next();
			result.add(getPathFromIndex(treev2.getRoot().getVertexIndex(), treev2));
		}
		
		for(int i: resultTreeIndexMap.keySet()){
			iterator = resultTreeIndexMap.get(i).iterator();
			while (iterator.hasNext()) {
				treev2 = iterator.next();
				result.add(getPathFromIndex(i, treev2));
			}
		}
	
	}
	
	public static String getPathFromIndex(int index, SearchTreeSL tree) {
		StringBuilder sb = new StringBuilder();
		Node t = tree.getNode(index);
		while (t!= null) {
			sb.append(t.getVertexIndex() + " ");
			t = t.getParent();
		}
		sb.append(tree.consumeNode.consumeIndex + " " + tree.consumeNode.bandwidthNeed);		
		return sb.toString();
		
	}
	
	
}
