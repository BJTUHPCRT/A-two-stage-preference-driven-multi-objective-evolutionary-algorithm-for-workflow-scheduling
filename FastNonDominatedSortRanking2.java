package org.uma.jmetal.algorithm.multiobjective.tspmoea;
import TSPMOEA.Tool;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.uma.jmetal.util.ranking.Ranking;


public class FastNonDominatedSortRanking2 {
	  private String attributeId = getClass().getName();  //如果需要找到它原有的序号，可以使用原有序号当做Id


	  private List<ArrayList<Individual>> rankedSubPopulations;

	  /** Constructor */
	  public FastNonDominatedSortRanking2() {
	     
	    rankedSubPopulations = new ArrayList<>();
	  }

	  /** Constructor */
	//  public FastNonDominatedSortRanking() {
//	    this(new DominanceComparator<>());
	//  }


	  public List<ArrayList<Individual>>  computeRanking(List<Individual> solutionList) {
		  
		  //popualtion , solutionList 这里似乎是多余的？
	    List<Individual> population = solutionList;
	    


	    // dominateMe[i] contains the number of population dominating i
	    int[] dominateMe = new int[population.size()];

	    // iDominate[k] contains the list of population dominated by k
	    List<List<Integer>> iDominate = new ArrayList<>(population.size());

	    // front[i] contains the list of individuals belonging to the front i
	    ArrayList<List<Integer>> front = new ArrayList<>(population.size() + 1);

	    // Initialize the fronts
	    for (int i = 0; i < population.size() + 1; i++) {
	      front.add(new LinkedList<Integer>());
	    }

	    // Fast non dominated sorting algorithm
	    // Contribution of Guillaume Jacquenot
	    //初始化两个列表
	    for (int p = 0; p < population.size(); p++) {
	      // Initialize the list of individuals that i dominate and the number
	      // of individuals that dominate me
	      iDominate.add(new LinkedList<Integer>());
	      dominateMe[p] = 0;
	    }

	    int flagDominate;
	    for (int p = 0; p < (population.size() - 1); p++) {
	      // For all q individuals , calculate if p dominates q or vice versa
	      for (int q = p + 1; q < population.size(); q++) {
	    	  //去除违背的计算
	        flagDominate = 0;
	            //CONSTRAINT_VIOLATION_COMPARATOR.compare(solutionList.get(p), solutionList.get(q));
	        if (flagDominate == 0) {
	        	//自己修改的非支配比较
	          flagDominate = compare(solutionList.get(p), solutionList.get(q));
	        }
	        if (flagDominate == -1) {
	          iDominate.get(p).add(q);
	          dominateMe[q]++;
	        } else if (flagDominate == 1) {
	          iDominate.get(q).add(p);
	          dominateMe[p]++;
	        }
	      }
	    }

	    //这是第一层支配的东西
	    for (int i = 0; i < population.size(); i++) {
	      if (dominateMe[i] == 0) {
	        front.get(0).add(i);
	        solutionList.get(i).attributes.put(attributeId, 0);
	      }
	    }
	    
	    // Obtain the rest of fronts
	    //front是用于辅助计算，不过也加入了rank值，而solutionList全部设置了rank
	    int i = 0;
	    Iterator<Integer> it1, it2; // Iterators
	    while (front.get(i).size() != 0) {
	      i++;
//	      System.out.println(i);
	      it1 = front.get(i - 1).iterator();
	      while (it1.hasNext()) {
	        it2 = iDominate.get(it1.next()).iterator();
	        while (it2.hasNext()) {
	          int index = it2.next();
	          dominateMe[index]--;
	          if (dominateMe[index] == 0) {
	            front.get(i).add(index);
	            solutionList.get(index).attributes.put(attributeId, i);//设置rank
	            
	          }
	        }
	      }
	    }

	    
	    //rankSubPopulations是根据0,1,2,3这样的顺序又重新排好了。
	    rankedSubPopulations = new ArrayList<>();
	    // 0,1,2,....,i-1 are fronts, then i fronts
	    for (int j = 0; j < i; j++) {
	      //添加每一层，所以计算了每一层的大小，一层使用arrlist组成
	      rankedSubPopulations.add(j, new ArrayList<Individual>(front.get(j).size()));
	      it1 = front.get(j).iterator();
	      while (it1.hasNext()) {
	        rankedSubPopulations.get(j).add(solutionList.get(it1.next()));
	      }
	    }

	    //看下这句咋回事 
	    return rankedSubPopulations;
	  }
	   
	  public List<Individual> getSubFront(int rank) {
//	    Check.that(
//	        rank < rankedSubPopulations.size(),
//	        "Invalid rank: " + rank + ". Max rank = " + (rankedSubPopulations.size() - 1));

	    return rankedSubPopulations.get(rank);
	  }

	  
	  public int getNumberOfSubFronts() {
	    return rankedSubPopulations.size();
	  }

	 
	  public Integer getRank(Individual solution) {
//	    Check.notNull(solution);

	    Integer result = -1;
	    if (solution.attributes.get(attributeId) != null) {
	      result = (Integer) solution.attributes.get(attributeId);
	    }
	    return result;
	  }

	   
	  public Object getAttributedId() {
	    return attributeId;
	  }
	  
	  //支配的比较  如果是最小化的问题 -1支配了对方 0 互相不支配，1被支配
	  public int compare(Individual solution1, Individual solution2) {
		    
		    int result;
		    //去除了约束 
		    //result = constraintViolationComparator.compare(solution1, solution2);
		    result = 0;
		    if (result == 0) {
		      result = dominance(solution1, solution2);
		    }

		    return result;
		  }

		  private int dominance(Individual solution1, Individual solution2) {
		    int bestIsOne = 0;
		    int bestIsTwo = 0;
		    int result;
		    for (int i = 0; i < Tool.nobj; i++) {
		      double value1 = solution1.getObjectives(i);
		      double value2 = solution2.getObjectives(i);
		      if (value1 != value2) {
		        if (value1 > value2) {//只需要这两个地方改成大于和小于就行   
		          bestIsOne = 1;
		        }
		        if (value2 > value1) {
		          bestIsTwo = 1;
		        }
		      }
		    }
		    result = Integer.compare(bestIsTwo, bestIsOne);//留意这里的顺序
		    return result;
		  }
}