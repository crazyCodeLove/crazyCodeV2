package com.cacheserverdeploy.deploy;

public class TreeShow implements TreeStructure{

	@Override
	public void visitNode(Node root) {
		// TODO Auto-generated method stub
		System.out.print(root.getVertexIndex()+" ");
		
	}
	
}
