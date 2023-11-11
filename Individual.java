package org.uma.jmetal.algorithm.multiobjective.tspmoea;
import TSPMOEA.Tool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.uma.jmetal.solution.permutationsolution.impl.IntegerPermutationSolution;

//用于整数的编码
public class Individual {

	  public Individual () {
		    
    }
	  
//	//构造方法
	public Individual(Individual individual) {
		//variable的更新
		for (int i = 0; i < varLen; i++) {
				setVariables(i,individual.variables[i]);	
				setVariables2(i, individual.variables2[i]);
		    }
			
		//objectives的更新
		for(int i=0;i<nobj;i++) {
			setObjectives(i,individual.objectives[i]);
		}
		//
		attributes = new HashMap<Object, Object>(individual.attributes);

	  }

	  public Individual copy() {
		    return new Individual(this);
      }
	  
	  
	  //第index上的位置值为val
	  public void setVariables(int index,int val) {
		  this.variables[index] = val;
	  }
	  //
	  public void setVariables2(int index,int val) {
		  this.variables2[index] = val;
	  }
	  //set
	  public void setObjectives(int index,double val) {
		  this.objectives[index] = val;
	  }
	  
	  //返回两种类型变量的值
	  public int getVariables(int index) {
		  return variables[index];
	  }
	  public int getVariables2(int index) {
		  return variables2[index];
	  }
	  
	  //get
	  public double getObjectives(int index) {
		  return objectives[index];
	  }
	  
	  public int [] getAllVariables() {
		  return variables;
	  }
	  
	  public int [] getAllVariables2() {
		  return variables2;
	  }
	  
	  public double[] getAllObjectives() {
		  return objectives;
	  }

	  
	  
	  
	  int[] variables = new int[Tool.TaskNum] ;//存变量  存vm的序号
	  int[] variables2 = new int[Tool.TaskNum];//存第二个  存order
	  int varLen = Tool.TaskNum;//这里改了
	  int nobj = Tool.nobj;
	  double[] objectives = new double[Tool.nobj];
	  Map<Object,Object> attributes  = new HashMap<Object,Object>(); 
	  
	  
	  
	  public void setAttribute(Individual solution, List<Double> value) {
		    solution.attributes.put(getAttributeIdentifier(), value);
	  }

	  
	  @SuppressWarnings("unchecked")
	public List<Double> getAttribute(Individual solution) {
	    return (List<Double>) solution.attributes.get(getAttributeIdentifier());
	  }
	  
	  public Object getAttributeIdentifier() {
	    return this.getClass();
	  }
	  
	}
