package org.uma.jmetal.algorithm.multiobjective.tspmoea;
import TSPMOEA.Tool;

public class Bounds {
	//分别是vm 和 task执行序号
	public int lowBound1[] = new int[Tool.TaskNum];
	public int upBound1[] = new int[Tool.TaskNum];
	public int lowBound2[] = new int[Tool.TaskNum];
	public int upBound2[] = new int[Tool.TaskNum];
	
	//java的区间默认是[a,b)所以得留意这个地方，免得搜索空间变小，留意初始化、和交叉变异
	public Bounds() {
		for (int i=0;i<Tool.TaskNum;i++) {
			lowBound1[i] = 0;
			upBound1[i] = Tool.VmNum-1;//留意这里
			lowBound2[i] = 0;
			upBound2[i] = Tool.TaskNum-1;//留意这里
		}
	}
	
	
	public int getLowerBound( int index) {
		return lowBound1[index];
	}
	
	public int getUpperBound(int index) {
		return upBound1[index];
	}
	public int getLowerBound2( int index) {
		return lowBound2[index];
	}
	
	public int getUpperBound2(int index) {
		return upBound2[index];
	}
}
