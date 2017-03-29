package com.cacheserverdeploy.deploy;

public class Node{
	private Vertex vertex;
	/**cost:从根节点到该节点的单位租赁费用**/
	private int cost;
	/**maxbandwidth:从根节点到该节点的最大带宽**/
	private int maxbandwidth;
	
	private Node parent;
	private Node firstChild;
	private Node nextSlibing;
	
	public Node(Vertex vertex) {
		// TODO Auto-generated constructor stub
		this.vertex = vertex;
		cost = 0;
		maxbandwidth = 0;
		parent = null;
		firstChild = null;
		nextSlibing = null;
	}
	
	public Node(Vertex vertex, int cost, Node parent) {
		// TODO Auto-generated constructor stub
		this.vertex = vertex;
		this.cost = cost;
		maxbandwidth = 0;
		this.parent = parent;
		firstChild = null;
		nextSlibing = null;
	}	
	
	
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return vertex.index == ((Node)obj).getVertex().index;
	}
	
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return vertex.index;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return String.valueOf(vertex.index);
	}

	public Node getParent() {
		return parent;
	}

	public Node getFirstChild() {
		return firstChild;
	}

	public int getCost() {
		return cost;
	}
	
	public void setFirstChild(Node firstChild) {
		this.firstChild = firstChild;
	}

	public Node getNextSlibing() {
		return nextSlibing;
	}

	public void setNextSlibing(Node nextSlibing) {
		this.nextSlibing = nextSlibing;
	}

	public Vertex getVertex() {
		return vertex;
	}
	
	public int getVertexIndex(){
		return vertex.index;
	}
	
	
}
