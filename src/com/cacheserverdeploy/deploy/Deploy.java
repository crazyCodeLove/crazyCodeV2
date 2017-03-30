package com.cacheserverdeploy.deploy;

import java.util.*;
import com.filetool.util.FileUtil;



public class Deploy
{

	public static ParseInput parseInput;

	/**
     * 你需要完成的入口
     * <功能详细描述>
     * @param graphContent 用例信息文件
     * @return [参数说明] 输出结果信息
     * @see [类、类#方法、类#成员]
     */
    public static String[] deployServer(String[] graphContent)
    {
        /**do your work here**/
    	
    	parseInput = new ParseInput(graphContent);
    	
    	List<String> result = SearchTreeSL.deploySLTree();
    	
    	result.add(0, String.valueOf(result.size()));
    	result.add(1, "");
    	
    	return result.toArray(new String[result.size()]);
    	
    }
    
    
    
    
    public static void main(String[] args){
    	
    	long start = System.currentTimeMillis();
    	String[] filename = {"D:\\chengxu\\java_program\\case_example\\level2\\case0.txt"};
    	String[] contents = FileUtil.read(filename[0], null);
    	
    	String[] result = deployServer(contents);
    	for(String string:result){
    		System.out.println(string);
    	}
    	long end = System.currentTimeMillis();
    	System.out.println("time:"+(end-start)+"ms");
    	
    	printCost(result);
    	
    }
    
    public static void printCost(String[] result){
    	Graph linknetGraph = parseInput.getLinkNetGraph();
    	int linknum = Integer.parseInt(result[0]);
    	HashSet<Integer> serverNodes = new HashSet<>();
    	
    	int[] data;
    	int sum=0;
    	
    	for(int i=0;i<linknum;i++){
    		data = ParseInput.parseLine2IntArr(result[i+2]);
    		serverNodes.add(data[0]);
    		
    		sum += getSingleLineCost(data, linknetGraph);
    	}
    	sum += serverNodes.size() * parseInput.getServerCost();
    	System.out.println("cost:" + sum);
    }
    
    public static int getSingleLineCost(int[] data, Graph linknetGraph){
    	int sum=0;
    	int bandwidth = data[data.length-1];
    	int startVertexIndex = data[0];
    	int endVertexIndex;
    	for(int i=1;i<data.length-2;i++){
    		endVertexIndex = data[i];
    		sum += bandwidth*linknetGraph.getEdgeRentCost(startVertexIndex, endVertexIndex);
    		startVertexIndex = endVertexIndex;
    	}
    	
    	return sum;
    }

}
