package com.cacheserverdeploy.deploy;

import java.util.*;
/*
 * 链接表
 * 存储起始点、和该起始点相连的所有边的信息
 * 
 */

public class LinkTable {
	private Vertex startVertex;
	private LinkedList<Edge> adjEdges;
	
	public LinkTable(){
		startVertex = null;
		adjEdges = new LinkedList<>();
	}
	
	public LinkTable(Vertex vertex){
		startVertex = vertex;
		adjEdges = new LinkedList<>();
	}
	
	public Edge getEdge(Vertex vertex){
		Iterator<Edge> iterator = adjEdges.iterator();
		Edge edge;
		while(iterator.hasNext()){
			edge = iterator.next();
			if(edge.getEndVertex().equals(vertex)){
				return edge;
			}
		}
		return null;
	}
	
	public Edge getEdge(int index){
		Iterator<Edge> iterator = adjEdges.iterator();
		Edge edge;
		while(iterator.hasNext()){
			edge = iterator.next();
			if(edge.getEndVertex().index == index)
				return edge;
		}
		return null;
	}

	public Vertex getStartVertex() {
		return startVertex;
	}

	public void setStartVertex(Vertex startVertex) {
		this.startVertex = startVertex;
	}
	
	public LinkedList<Edge> getAdjEdges() {
		return adjEdges;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
