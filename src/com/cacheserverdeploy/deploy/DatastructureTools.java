package com.cacheserverdeploy.deploy;

public class DatastructureTools {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public static int getMin(Integer[] data){
		//获取数组中最小值
		int min = data[0];
		for(Integer i:data){
			if(i<min)
				min = i;
		}
		return min;
	}

}
