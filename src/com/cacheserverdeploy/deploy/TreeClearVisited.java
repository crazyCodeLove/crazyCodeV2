package com.cacheserverdeploy.deploy;

public class TreeClearVisited implements TreeStructure{

	@Override
	public void visitNode(Node root) {
		// TODO Auto-generated method stub
		root.getVertex().clearVisited();
		
	}

	
}
