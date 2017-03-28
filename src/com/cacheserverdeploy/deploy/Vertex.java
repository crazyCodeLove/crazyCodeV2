package com.cacheserverdeploy.deploy;

/*
 * 图中顶点信息
 * 包含顶点标号、是否访问过
 * 
 */

public class Vertex {
	public final int index;
	private boolean visited;
	
	public Vertex(int index){
		this.index = index;
		visited = false;
	}
	
	
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String s = "nice to meet you";
		System.out.println(s.length());
		
		

	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return index + " ";
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return index;
	}
	
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return index == ((Vertex)obj).index;
	}
	
	public boolean isVisited() {
		return visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}

}
