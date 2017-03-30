package com.cacheserverdeploy.deploy;

import java.util.*;

import com.filetool.util.FileUtil;

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
	
	public Vertex getVertex(final int vertexIndex){
		if(vertexIndex>=0 && vertexIndex<linkTables.length)
			return linkTables[vertexIndex].getStartVertex();
		else
			return null;
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
	
	public int getEdgeSize(Vertex vertex){
		return linkTables[vertex.index].getAdjEdges().size();
	}
	
	public Edge getEdge(final int startVertexIndex, final int endVertexIndex){
		if(startVertexIndex<0 || startVertexIndex>=linkTables.length)
			return null;
		return linkTables[startVertexIndex].getEdge(endVertexIndex);
				
	}
	
	public int getEdgeRentCost(final int startVertexIndex, final int endVertexIndex){
		return getEdge(startVertexIndex, endVertexIndex).getRentcost();
	}
	
	public LinkTable getLinkTable(int vertexIndex){
		if(vertexIndex<0 || vertexIndex>=linkTables.length)
			return null;
		return linkTables[vertexIndex];
	}
	
	public LinkedList<Edge> getAdjEdges(final int vertexIndex) {
		if(vertexIndex<0 || vertexIndex>=linkTables.length)
			return null;
		return linkTables[vertexIndex].getAdjEdges();
	}
	
	public LinkedList<Edge> getAdjEdges(Vertex vertex) {
		return linkTables[vertex.index].getAdjEdges();
	}
	
	public int getBandwidth(final int startVertexIndex, final int endVertexIndex){
		return getEdge(startVertexIndex, endVertexIndex).getBandwidth();
	}
	
	public void cutdownBandwidth(
			final int startVertexIndex, final int endVertexIndex, final int bandwidth){
		Edge edge = getEdge(startVertexIndex, endVertexIndex);
		edge.cutdownBandwidth(bandwidth);
	}
	
	public void addBandwidth(
			final int startVertexIndex, final int endVertexIndex, final int bandwidth){
		Edge edge = getEdge(startVertexIndex, endVertexIndex);
		edge.addBandwidth(bandwidth);
	}

	public void showGraphBFS(){
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
		
		clearVertexVisited();
	}
	
	public void clearVertexVisited(){
		for(int i=0;i<linkTables.length;i++){
			linkTables[i].getStartVertex().clearVisited();
		}
	}
	
	public void showGraphEdgeAllDirec(){
		Iterator<Edge> iterator;
		Edge edge;
		Vertex vertex;
		for(int i=0;i<linkTables.length;i++){
			vertex = linkTables[i].getStartVertex();
			iterator = linkTables[i].getAdjEdges().iterator();
			while(iterator.hasNext()){
				edge = iterator.next();
				System.out.println(vertex.index + " " + edge.getEndVertex().index + " " + edge.getBandwidth());
				
			}
		}
	}
	
	public void showGraphEdge(){
		/**打印图中所有链接的信息**/
		Vertex vertex;
		Iterator<Edge> iterator;
		Edge edge;
		for(int i=0;i<linkTables.length;i++){
			vertex = linkTables[i].getStartVertex();
			iterator = linkTables[i].getAdjEdges().iterator();
			while(iterator.hasNext()){
				edge = iterator.next();
				if(!edge.isVisited()){
					System.out.println(vertex.index + " " + edge.getEndVertex().index + " " + edge.getBandwidth());
					edge.setVisited(true);
					getEdge(edge.getEndVertex().index, vertex.index).setVisited(true);
				}
			
			}
		}
		
		clearEdgeVisited();
	}
	
	public void clearEdgeVisited(){
		Iterator<Edge> iterator;
		Edge edge;
		for(int i=0;i<linkTables.length;i++){
			iterator = linkTables[i].getAdjEdges().iterator();
			while(iterator.hasNext()){
				edge = iterator.next();
				edge.clearVisited();			
				
			}
		}
	}
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String[] filename = {"D:\\chengxu\\java_program\\case_example\\case0.txt"};
    	String[] graphContent = FileUtil.read(filename[0], null);
    	ParseInput parseInput = new ParseInput(graphContent);
    	Vertex vertex = parseInput.getLinkNetGraph().getVertex(38);
    	System.out.println(vertex.index);
    	

	}

}
