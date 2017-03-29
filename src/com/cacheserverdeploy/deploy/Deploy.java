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
    	String[] filename = {"D:\\chengxu\\java_program\\case_example\\level0\\case0.txt"};
    	String[] contents = FileUtil.read(filename[0], null);
    	
    	String[] result = deployServer(contents);
    	for(String string:result){
    		System.out.println(string);
    	}
    	long end = System.currentTimeMillis();
    	System.out.println("time:"+(end-start)+"ms");
    	
    }

}
