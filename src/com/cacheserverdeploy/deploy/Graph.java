package com.cacheserverdeploy.deploy;

import java.util.*;

/*
 * 生成图的信息
 * 包含
 */
public class Graph {
	private LinkTable[] linkTables;
	
	public Graph(final int vertexNum){
		linkTables = new LinkTable[vertexNum];
		for(int i=0;i<vertexNum;i++){
			linkTables[i] = new LinkTable(new Vertex(i));
		}
	}
	
	public void addEdge(Vertex startVertex, Edge edge){
		linkTables[startVertex.index].getAdjEdges().add(edge);
		
	}
	
	public void addEdge(final int startVertexIndex, Edge edge){
		linkTables[startVertexIndex].getAdjEdges().add(edge);
		
	}
	
	public void addEdge(
			final int startVertexIndex, final int endVertextIndex, 
			final int bandwidth, final int rentcost){
		Edge newEdge = new Edge(bandwidth, rentcost, linkTables[endVertextIndex].getStartVertex());
		linkTables[startVertexIndex].getAdjEdges().add(newEdge);
		
	}
	
	public int getEdgeNums(Vertex vertex){
		return linkTables[vertex.index].getAdjEdges().size();
	}
	
	public Edge getFirstEdge(Vertex vertex){
		LinkedList<Edge> edges = linkTables[vertex.index].getAdjEdges();
		if(edges.size()>0)
			return edges.get(0);
		else
			return null;
	}
	
	
	public LinkTable getLinkTable(int index){
		if(index<0 || index>linkTables.length)
			return null;
		return linkTables[index];
	}

	public void showGraphGFS(){
		/**打印图的节点标号，按照广度优先策略**/
		ArrayDeque<Vertex> vertexQue = new ArrayDeque<>();
		if(linkTables.length>0){
			Vertex vertex=linkTables[0].getStartVertex();
			vertex.setVisited(true);
			vertexQue.addLast(vertex);
			
			
			Edge edge;
			while(!vertexQue.isEmpty()){
				vertex = vertexQue.removeFirst();
				System.out.print(vertex.index + " ");
				
				Iterator<Edge> iterator = linkTables[vertex.index].getAdjEdges().iterator();				
				while(iterator.hasNext()){
					
					edge = iterator.next();
					if(!(vertex = edge.getEndVertex()).isVisited()){
						vertex.setVisited(true);
						vertexQue.addLast(vertex);
						
					}
				}
				
			}
		}
		
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
