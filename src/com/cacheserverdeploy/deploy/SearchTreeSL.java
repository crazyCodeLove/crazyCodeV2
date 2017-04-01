package com.cacheserverdeploy.deploy;

import java.util.*;

public class SearchTreeSL {
	/*
	 * 一条链路可以满足带宽需求,SL(single line)
	 */	 
	private static ArrayList<ConsumeNode>  noOptimizeConsumeNodeList = new ArrayList<>();
	private static ArrayList<SearchTreeSL>         optimizeTrees     = new ArrayList<>();
	
	
	private static Graph linkNetGraph   = Deploy.parseInput.getLinkNetGraph();
	private static final int serverCost = Deploy.parseInput.getServerCost();
	private static ArrayList<ConsumeNode> consumeNet = Deploy.parseInput.getConsumeNet();
	
	private Node root;
	private Node serverNode;
	private Vertex serverVertex;
	private LinkedList<String> path;
	
	// 与该树相连的消费节点
	private ConsumeNode consumeNode;
	
	
	public SearchTreeSL(ConsumeNode consumeNode){
		path = new LinkedList<>();
		this.consumeNode = consumeNode;
		
		Vertex t = linkNetGraph.getVertex((consumeNode.linknetIndex));
		root = new Node(t);
		t.setVisited(true);
		
	}
	
	public void clearTreeRedundancy(){
		root = null;
		serverNode = null;
		
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
	
	public void restoreTreeBandwidth() {
		// 将树上的节点带宽恢复到原始状态,
		//将linkNetGraph 中从服务器节点到消费节点的连路加上之前剪掉的 bandwidthNeed 
		TreeDFS(new TreeStructure() {
			
			@Override
			public void visitNode(Node pointer) {
				// TODO Auto-generated method stub
				Node parent = pointer.getParent();
				if(parent!=null){
					linkNetGraph.addBandwidth(pointer.getVertexIndex(), parent.getVertexIndex(), consumeNode.bandwidthNeed);
				}
				
			}
		});
		
	
	}
	
	
	public void cutdownPathBandwidth(){
		/** 将linkNetGraph 中从服务器节点到消费节点的连路上 bandwidthNeed 减掉  **/
		Node pointer = serverNode;
		Node parent = pointer.getParent();
		while(parent!=null){
			linkNetGraph.cutdownBandwidth(pointer.getVertexIndex(), parent.getVertexIndex(), consumeNode.bandwidthNeed);
			pointer = parent;
			parent = parent.getParent();
		}
	}
	
	public Node getNode(final int index){
		/*
		 * 遍历该树，将树上节点index与请求的index相同的节点返回，否则返回null。
		 */
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
	
	public Vertex getServerVertex() {
		return serverVertex;
	}

	public void setServerVertex(Vertex serverVertex) {
		this.serverVertex = serverVertex;
	}

	public void setServerNode(Node serverNode) {
		this.serverNode = serverNode;
	}

	public void searchTreeHelper(Node root, HashMap<Integer, Node> candidateMap){
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
		//获取与 root 节点相连的所有可选节点，并添加进candidateMap
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
			if(linkNetGraph.getBandwidth(tedge.getEndVertex().index, root.getVertexIndex()) >= consumeNode.bandwidthNeed 
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
		linkNetGraph.cutdownBandwidth(newindex, parentindex, consumeNode.bandwidthNeed);
	}
	
	public Node getRoot() {
		return root;
	}

	public void appendPath(String newPath){
		path.add(newPath);
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return root.getVertexIndex()+"";
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
	
	public static ArrayList<ConsumeNode> getNoOptimizeConsumeNodeList(){
		return noOptimizeConsumeNodeList;
	}
	
	public static ArrayList<SearchTreeSL>  getOptimizeTrees(){
		return optimizeTrees;
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
		
		Integer[] indexTreeInfo = getSortedIndexAndTreeInfo(canOptimizeTree);		
		do  {
			// Integer(32): 该节点树的个数(16 bit) 该节点的索引index(16 bit)
			minIndex=getMinCostIndex(indexTreeInfo, canOptimizeTree);
			optimizeTreeList = canOptimizeTree.get(minIndex);
			resultTreeIndexMap.put(minIndex, optimizeTreeList);
			canOptimizeTree.remove(minIndex);
			removeMinCostTrees(optimizeTreeList, canOptimizeTree);
			indexTreeInfo = getSortedIndexAndTreeInfo(canOptimizeTree);
		} while (!canOptimizeTree.isEmpty());
		
		//获取 noOptimizeTree, 将 resultTreeIndexMap 的所有可以优化的树从 allTrees 中全部移除就是 noOptimizeTree
		for(LinkedList<SearchTreeSL> optimizetress:resultTreeIndexMap.values()){
			allTrees.removeAll(optimizetress);
		}
		noOptimizeTree.addAll(allTrees);
		
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
							HashMap<Integer, LinkedList<SearchTreeSL>> canoptimizeTree) {
		// 将canoptimizeTree集合中, 节点上tree的数量 >= 2的加进 canoptimizeTree, 处理的时候优先处理canoptimizeTree
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
		Integer[] arrindexTreeInfo = indexAndTreeInfo.toArray(new Integer[0]);
		
		return arrindexTreeInfo;
	}
	
	public static int getMinCostIndex(
			Integer[] sortedindexAndTreeInfo, HashMap<Integer, LinkedList<SearchTreeSL>> canoptimizeTree) {
		//获取相交节点最多(此处的 sortedindexAndTreeInfo 已经是相交点最多的index集合了)，连接成本最小的，节点索引index
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
		/**求得所有 trees 在 nodeindex 处部署服务器的链路成本之和**/
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
			HashMap<Integer, LinkedList<SearchTreeSL>> canOptimizeTree) {
		// 将optimizeTrees从canOptimizeTree中全部移除
		LinkedList<SearchTreeSL> tSearchTreeSLs;
		for(int i: canOptimizeTree.keySet()){
			tSearchTreeSLs = canOptimizeTree.get(i);
			tSearchTreeSLs.removeAll(optimizeTrees);
			canOptimizeTree.put(i, tSearchTreeSLs);//将删除过后的{Integer:LinkedList<SearchTreeSL>}覆盖掉以前的键值对
		}
		
	}
	
	public static void fillResult(
			ArrayList<String> result, HashSet<SearchTreeSL> noOptimizeTree, 
			HashMap<Integer, LinkedList<SearchTreeSL>> resultTreeIndexMap) {
		
		processNoOptimizeTree(result, noOptimizeTree);
		
		processOptimizeTree(result, resultTreeIndexMap);
	
	}
	
	public static void processNoOptimizeTree(
			ArrayList<String> result, HashSet<SearchTreeSL> noOptimizeTree){
		/*
		 * 处理不可以优化的树
		 * 将与该树相连的消费节点保存到 noOptimizeConsumeNode 
		 */
		Iterator<SearchTreeSL> iterator = noOptimizeTree.iterator();
		SearchTreeSL stree;
		StringBuilder sb;
		while (iterator.hasNext()) {
			stree = iterator.next();
			
			sb = new StringBuilder();
			sb.append(stree.consumeNode.linknetIndex + " " + stree.consumeNode.consumeIndex + " " + stree.consumeNode.bandwidthNeed);
			result.add(sb.toString());
			
			stree.restoreTreeBandwidth();
			noOptimizeConsumeNodeList.add(stree.consumeNode);
			stree.clearTreeRedundancy();
		}
	}
	
	public static void processOptimizeTree(
			ArrayList<String> result,
			HashMap<Integer, LinkedList<SearchTreeSL>> resultTreeIndexMap){
		/*
		 * 处理可以优化的树， 
		 * 优化的消费节点需要保存在该树中：
		 * 1）服务器位置Vertex
		 * 2）路径 path （String）
		 * 3)将该树保存到 optimizeTrees 列表中，以备后面使用
		 * 4）清空树中额外的信息，释放内存
		 * */
		Iterator<SearchTreeSL> iterator;
		SearchTreeSL stree;
		String path;
		
		for(int i: resultTreeIndexMap.keySet()){
			iterator = resultTreeIndexMap.get(i).iterator();
			while (iterator.hasNext()) {
				stree = iterator.next();
				
				path = getPathFromServerIndex(stree);
				result.add(path);
				stree.appendPath(path);
				
				stree.setServerVertex(stree.getServerNode().getVertex());
				
				stree.restoreTreeBandwidth();
				stree.cutdownPathBandwidth();
				optimizeTrees.add(stree);
				stree.clearTreeRedundancy();
			}
		}
		
	}
	
	public static String getPathFromServerIndex(SearchTreeSL tree) {
		/*
		 * 返回该树的服务器位置到消费节点的路径字符串
		 */
		StringBuilder sb = new StringBuilder();
		Node t = tree.getServerNode();
		while (t!= null) {
			sb.append(t.getVertexIndex() + " ");
			t = t.getParent();
		}
		sb.append(tree.consumeNode.consumeIndex + " " + tree.consumeNode.bandwidthNeed);
		return sb.toString();
		
	}
	
	
}
