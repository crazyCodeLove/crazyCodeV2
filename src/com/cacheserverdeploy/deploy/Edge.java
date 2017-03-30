package com.cacheserverdeploy.deploy;


/*
 * 图中边的信息，包含租赁费用、带宽、边的终点、是否访问过
 */
public class Edge {
	private int bandwidth;
	private int rentcost;
	private Vertex endVertex;
	private boolean visited;
	
	public Edge(int bandwidth, int rentcost, Vertex vertex){
		this.bandwidth = bandwidth;
		this.rentcost = rentcost;
		this.endVertex = vertex;
		visited = false;
	}	

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public void cutdownBandwidth(final int bandwidth){
		if (bandwidth <= this.bandwidth) {
			this.bandwidth -= bandwidth;
		} else {
			throw new RuntimeException("bandwidth not enough");
		}
	}
	
	public void addBandwidth(int bandwidth) {
		if (bandwidth<0) {
			throw new RuntimeException("add bandwidth is negative.");
		}
		this.bandwidth += bandwidth;
	}

	public Vertex getEndVertex() {
		return endVertex;
	}

	public int getRentcost() {
		return rentcost;
	}

	public void setEndVertex(Vertex endVertex) {
		this.endVertex = endVertex;
	}
	

	public int getBandwidth() {
		return bandwidth;
	}
	
	public void setBandwidth(int bandwidth) {
		this.bandwidth = bandwidth;
	}

	public boolean isVisited() {
		return visited;
	}
	
	public void clearVisited(){
		visited = false;
	}
	
	public void setVisited(boolean visited) {
		this.visited = visited;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return endVertex + "bandwidth:" + bandwidth + " rentcost:" + rentcost + " ";
	}

}
