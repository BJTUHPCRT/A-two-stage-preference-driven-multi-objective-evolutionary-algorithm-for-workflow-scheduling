package org.uma.jmetal.algorithm.multiobjective.tspmoea;
import TSPMOEA.Tool;
import java_cup.internal_error;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import TSPMOEA.MyFitnessFunction;



//新的导入
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.io.output.BrokenWriter;
import org.netlib.util.doubleW;



public class TSPMOEA {

	public int getMaxIter() {
		return maxIter;
	}

	public void setMaxIter(int maxIter) {
		this.maxIter = maxIter;
	}

	public int getPopSize() {
		return popSize;
	}
	
	public int getArchivepSize() {
		return archiveSize;
	}

	public void setPopSize(int popSize) {
		this.popSize = popSize;
	}

	public int getDimension() {
		return dimension;
	}

	public void setDimension(int dimension) {
		this.dimension = dimension;
	}

	public int getNobj() {
		return nobj;
	}

	public void setNobj(int nobj) {
		this.nobj = nobj;
	}

	//构造方法 初始化一下 
	public TSPMOEA() {
		//初始化一些参数
		for(int i=0;i<k_1;i++) {
			informationStaitc[i] = ((Double.MAX_VALUE));
		}	
		for(int i=0;i<nobj;i++) {
			meanPoint[i]  = 0;
		}
	}
	
	
	
	//设置相应的参数
	public int maxIter = Tool.IterationNum;
	public int popSize = Tool.populationSize;
	public int dimension = Tool.TaskNum;
	public  static int nobj = Tool.nobj;

	public static double p[] = Tool.p;
	public double distributionIndex = 20;
	public double crossoverProbability = 0.9;
	public double mutationProbability = 0.1;
	public int numberOfDivisions = Tool.divisions;
	
	
	//新添加的参数
	public static int archiveSize = Tool.populationSize;
	
	public static int k_1 = Tool.k_1;
	public static double threshold = Tool.threshold;
	
	
	public static int switchFlag = 0;//为1则是转换
	public double informationStaitc[]  = new double[k_1];
	
	
	public List<ReferencePoint2<Individual>> referencePoints = new Vector<>() ;
	public static double meanPoint[] = new double[Tool.nobj];
	public static double sigma = Double.MAX_VALUE;
	public static double iter = 0;
	
	//Test Ok    (2,99)=100 point, (3,13)=105 point   position对应点的值
	public void generatePoints() {
		(new ReferencePoint2<Individual>()).generateReferencePoints(referencePoints,nobj, numberOfDivisions);	
	}
	
	//初始化种群
	
	public List<Individual> population = new ArrayList<>();
	public List<Individual> offspringPopulation = new ArrayList<>();
	public List<Individual> archive = new ArrayList<>();
	
	
	//初始化种群
	public void initPop() {
		Random random = new Random();
		Bounds bo = new Bounds();
		
		//初始化种群
        for (int i = 0; i < popSize; i++) {
        	//对每一个个体初始化
        	Individual individual  = new Individual();
        	int val = 0;
        	int taskOrder[] = MyUtils.generateUqiInt(dimension);
        	for(int j=0;j<dimension;j++) {
        		//留意下这里的区间
        		val = bo.getLowerBound(j) + random.nextInt(bo.getUpperBound(j)+1 -bo.getLowerBound(j));
        		individual.setVariables(j, val);
        		individual.setVariables2(j, taskOrder[j]);

        	}
        	population.add(individual);
        }
	}
	
//用于测试是否与matlab版本一致
//	public void initPop() throws NumberFormatException, IOException {
//
//		List<Solution> solutionList = Solution.readSolution();
//		Solution solution;
//		int val;
//		//初始化种群
//        for (int i = 0; i < popSize; i++) {
//        	//对每一个个体初始化
//        	Individual individual  = new Individual();
//        	solution = solutionList.get(i);
//        	for(int j=0;j<dimension;j++) {
//        		
//        		val = solution.arr[j];
//        		
//        		individual.setVariables(j, val-1);
//
//        	}
//        	population.add(individual);
//        }
//	}

	
    
    //计算种群的平均适应值
    public double[] calMeanFitness() {
    	double meanArr[] = new double[nobj];
    	//初始化
    	for(int i=0;i<nobj;i++) {
    		meanArr[i] = 0;
    	}
    	
    	
    	//累加
    	for (int i = 0; i <population.size(); i++) {
    		for(int j=0;j<nobj;j++) {
    			meanArr[j] = meanArr[j] + population.get(i).getObjectives(j);
    		}
    		
    	}
    	
    	//求平均
    	for(int i=0;i<nobj;i++) {
    		meanArr[i] = meanArr[i]/population.size();
    	}
    	return meanArr;
    }
    
    
    
	//计算fitness
	public void evaluatePop(List<Individual> population) {
		
	

		double makespan=0.0;
		double cost=0.0;
		double energy=0.0; 
		double USD = 0;//user satisfac degree
		double PPM = 0;
		double ECI = 0;
		int assignment[];
		int taskOrder[];
		//简单自己设计的方法
		for(int i=0;i<population.size();i++) {
			
			assignment = population.get(i).getAllVariables().clone();
			taskOrder = population.get(i).getAllVariables2().clone();
			Tool.allot = assignment;
			Tool.taskOrder = taskOrder;
			MyFitnessFunction myFitness = new MyFitnessFunction(); 
			myFitness.scheduleSimulation(assignment,taskOrder);
			USD = myFitness.userSatisfactionDegree();
			PPM = myFitness.providerProfitMargin();
			ECI = myFitness.energyConsumptionImprovement();
			
//			makespan = myFitness.calMakespan();
//			cost = myFitness.calCost();
//			energy = myFitness.calEnergyConsumption();

			
			//验证下get这种方法是否改变了值。
			population.get(i).setObjectives(0, USD);

			population.get(i).setObjectives(1, PPM);
			
			population.get(i).setObjectives(2, ECI);
		    
		}
		
	}
	
	 //交叉操作  
    public List<Individual>  Intercrossover2( double probability, Individual parent1, Individual parent2) {
    	int crossLen = (int) Math.ceil( probability* dimension );
    	int tempValue = 0;
    	int temp1 = 0;
    	int temp2 = 0;
    	List<Individual> offspring = new ArrayList<Individual>(2);

	    
	    //把两个父代复制到offspring中
	    offspring.add(parent1.copy());
	    offspring.add(parent2.copy()) ;
    	//模拟多点交叉  
        
        	Set<Integer> muIndex1 = MyUtils.getRandoms(0, dimension, crossLen);
        	Set<Integer> muIndex2 = MyUtils.getRandoms(0, dimension, crossLen);
        	
        	//交叉
    		Iterator<Integer> x = muIndex1.iterator();//先迭代出来
    		Iterator<Integer> y = muIndex2.iterator();//先迭代出来	
    		while(x.hasNext() && y.hasNext() ){//遍历

    			temp1 = x.next().intValue();
    			temp2 = y.next().intValue();
    			tempValue = offspring.get(0).getVariables(temp1);
    			offspring.get(0).setVariables(temp1, offspring.get(1).getVariables(temp2));
    			offspring.get(1).setVariables(temp2, tempValue);
    			
    	
    		}
    		return offspring;
    	
        }

	
  //模拟二进制交叉
  	public List<Individual> Intercrossover( double probability, Individual parent1, Individual parent2) {
  		/** EPS defines the minimum difference allowed between real values */
  		
  		double EPS = 1.0e-14;
  	    
  	    
  	    List<Individual> offspring = new ArrayList<Individual>(2);

  	    
  	    //把两个父代复制到offspring中
  	    offspring.add(parent1.copy());
  	    offspring.add(parent2.copy()) ;
  	    
  	    //这里要测试一下  copy的效果确实有效，是深拷贝    
  	    int i;
  	    double rand;
  	    double y1, y2, yL, yu;
  	    double c1, c2;
  	    double alpha, beta, betaq;
  	    int valueX1, valueX2;
  	    Random random = new Random();
  	    if (random.nextDouble() <= probability) {
  	      for (i = 0; i < dimension ; i++) {
  	    	valueX1 = parent1.getVariables(i);
  	    	valueX2 = parent2.getVariables(i);

  	        if (random.nextDouble() <= 0.5) {
  	          if (Math.abs(valueX1 - valueX2) > EPS) {

  	            if (valueX1 < valueX2) {
  	              y1 = valueX1;
  	              y2 = valueX2;
  	            } else {
  	              y1 = valueX2;
  	              y2 = valueX1;
  	            }
  	            
  	            Bounds bound = new Bounds();
  	           
  	            yL = bound.getLowerBound(i);
  	            yu = bound.getUpperBound(i);
  	            rand = random.nextDouble();
  	            beta = 1.0 + (2.0 * (y1 - yL) / (y2 - y1));
  	            alpha = 2.0 - Math.pow(beta, -(distributionIndex + 1.0));

  	            if (rand <= (1.0 / alpha)) {
  	              betaq = Math.pow((rand * alpha), (1.0 / (distributionIndex + 1.0)));
  	            } else {
  	              betaq = Math.pow(1.0 / (2.0 - rand * alpha), 1.0 / (distributionIndex + 1.0));
  	            }

  	            c1 = 0.5 * ((y1 + y2) - betaq * (y2 - y1));
  	            beta = 1.0 + (2.0 * (yu - y2) / (y2 - y1));
  	            alpha = 2.0 - Math.pow(beta, -(distributionIndex + 1.0));

  	            if (rand <= (1.0 / alpha)) {
  	              betaq = Math.pow((rand * alpha), (1.0 / (distributionIndex + 1.0)));
  	            } else {
  	              betaq = Math.pow(1.0 / (2.0 - rand * alpha), 1.0 / (distributionIndex + 1.0));
  	            }

  	            c2 = 0.5 * (y1 + y2 + betaq * (y2 - y1));
  	            
  	            //这里没采用区间，而是直接采用上下界
  	            if (c1 < yL) {
  	              c1 = yL;
  	            
  	            }

  	            if (c2 < yL) {
  	              c2 = yL;
  	            }

  	            if (c1 > yu) {
  	              c1 = yu;
  	            
  	            }

  	            if (c2 > yu) {
  	              c2 = yu;
  	              
  	            }

  	            if (random.nextDouble() <= 0.5) {
  	            	offspring.get(0).setVariables(i, (int)c2);
  	            	offspring.get(1).setVariables(i, (int)c1);
  	      
  	            } else {
  	            	offspring.get(0).setVariables(i, (int)c1);
  	            	offspring.get(1).setVariables(i, (int)c2);
  	         
  	            }
  	          } else {
  	            	offspring.get(0).setVariables(i, valueX1);
  	            	offspring.get(1).setVariables(i, valueX2);

  	          }
  	        } else {
              	offspring.get(0).setVariables(i, valueX2);
              	offspring.get(1).setVariables(i, valueX1);

  	        }
  	      }
  	    }

  		
  		return offspring;
  	}
	
	//针对顺序的交叉
  //模拟二进制交叉
  	public List<Individual> IntercrossoverOrder( double probability, Individual parent1, Individual parent2) {
  		/** EPS defines the minimum difference allowed between real values */
  		
  		double EPS = 1.0e-14;
  	    
  	    
  	    List<Individual> offspring = new ArrayList<Individual>(2);

  	    
  	    //把两个父代复制到offspring中
  	    offspring.add(parent1.copy());
  	    offspring.add(parent2.copy()) ;
  	    
  	    //这里要测试一下  copy的效果确实有效，是深拷贝    
  	    int i;
  	    double rand;
  	    double y1, y2, yL, yu;
  	    double c1, c2;
  	    double alpha, beta, betaq;
  	    int valueX1, valueX2;
  	    Random random = new Random();
  	    if (random.nextDouble() <= probability) {
  	      for (i = 0; i < dimension ; i++) {
  	    	valueX1 = parent1.getVariables2(i);
  	    	valueX2 = parent2.getVariables2(i);

  	        if (random.nextDouble() <= 0.5) {
  	          if (Math.abs(valueX1 - valueX2) > EPS) {

  	            if (valueX1 < valueX2) {
  	              y1 = valueX1;
  	              y2 = valueX2;
  	            } else {
  	              y1 = valueX2;
  	              y2 = valueX1;
  	            }
  	            
  	            Bounds bound = new Bounds();
  	           
  	            yL = bound.getLowerBound2(i);
  	            yu = bound.getUpperBound2(i);
  	            rand = random.nextDouble();
  	            beta = 1.0 + (2.0 * (y1 - yL) / (y2 - y1));
  	            alpha = 2.0 - Math.pow(beta, -(distributionIndex + 1.0));

  	            if (rand <= (1.0 / alpha)) {
  	              betaq = Math.pow((rand * alpha), (1.0 / (distributionIndex + 1.0)));
  	            } else {
  	              betaq = Math.pow(1.0 / (2.0 - rand * alpha), 1.0 / (distributionIndex + 1.0));
  	            }

  	            c1 = 0.5 * ((y1 + y2) - betaq * (y2 - y1));
  	            beta = 1.0 + (2.0 * (yu - y2) / (y2 - y1));
  	            alpha = 2.0 - Math.pow(beta, -(distributionIndex + 1.0));

  	            if (rand <= (1.0 / alpha)) {
  	              betaq = Math.pow((rand * alpha), (1.0 / (distributionIndex + 1.0)));
  	            } else {
  	              betaq = Math.pow(1.0 / (2.0 - rand * alpha), 1.0 / (distributionIndex + 1.0));
  	            }

  	            c2 = 0.5 * (y1 + y2 + betaq * (y2 - y1));
  	            
  	            //这里没采用区间，而是直接采用上下界
  	            if (c1 < yL) {
  	              c1 = yL;
  	            
  	            }

  	            if (c2 < yL) {
  	              c2 = yL;
  	            }

  	            if (c1 > yu) {
  	              c1 = yu;
  	            
  	            }

  	            if (c2 > yu) {
  	              c2 = yu;
  	              
  	            }

  	            if (random.nextDouble() <= 0.5) {
  	            	offspring.get(0).setVariables2(i, (int)c2);
  	            	offspring.get(1).setVariables2(i, (int)c1);
  	      
  	            } else {
  	            	offspring.get(0).setVariables2(i, (int)c1);
  	            	offspring.get(1).setVariables2(i, (int)c2);
  	         
  	            }
  	          } else {
  	            	offspring.get(0).setVariables2(i, valueX1);
  	            	offspring.get(1).setVariables2(i, valueX2);

  	          }
  	        } else {
              	offspring.get(0).setVariables2(i, valueX2);
              	offspring.get(1).setVariables2(i, valueX1);

  	        }
  	      }
  	    }

  		
  		return offspring;
  	}
  	
  	//变异操作
  	public void Intermutation2(double probability, Individual individual) {
    	int mutationLen = (int) Math.ceil(probability * dimension );
    	Random rand = new Random();
       
        	Set<Integer> muIndex = MyUtils.getRandoms(0, dimension, mutationLen);
        	
        	//变异
    		for (Integer s:muIndex) {  
    			individual.setVariables(s.intValue(), rand.nextInt(Tool.VmNum));//[0,vmnum)
    			 
    			}
        	
        
    }
  	
  	
	//多项式变异
	public void Intermutation(double probability, Individual individual) {
		double rnd, delta1, delta2, mutPow, deltaq;
	    double y, yl, yu, val, xy;
	    
	    Random random = new Random();
	    Bounds bound = new Bounds();
	    for (int i = 0; i < individual.varLen; i++) {
	      if (random.nextDouble() <= probability) {
	    	
	        y = (double)individual.getVariables(i);
	        
	        yl = (double)bound.getLowerBound(i);
	        yu = (double)bound.getUpperBound(i);
	        if (yl == yu) {
	          y = yl ;
	        } else {
	          delta1 = (y - yl) / (yu - yl);
	          delta2 = (yu - y) / (yu - yl);
	          rnd = random.nextDouble();
	          mutPow = 1.0 / (distributionIndex + 1.0);
	          if (rnd <= 0.5) {
	            xy = 1.0 - delta1;
	            val = 2.0 * rnd + (1.0 - 2.0 * rnd) * (Math.pow(xy, distributionIndex + 1.0));
	            deltaq = Math.pow(val, mutPow) - 1.0;
	          } else {
	            xy = 1.0 - delta2;
	            val = 2.0 * (1.0 - rnd) + 2.0 * (rnd - 0.5) * (Math.pow(xy, distributionIndex + 1.0));
	            deltaq = 1.0 - Math.pow(val, mutPow);
	          }
	          y = y + deltaq * (yu - yl);
	          y = MyUtils.randValue(y, yl, yu);
	        }
	        individual.setVariables(i, (int) y);
	      }
	    }
	    

  }
 
	//针对order的变异
	public void IntermutationOrder(double probability, Individual individual) {
		double rnd, delta1, delta2, mutPow, deltaq;
	    double y, yl, yu, val, xy;
	    
	    Random random = new Random();
	    Bounds bound = new Bounds();
	    for (int i = 0; i < individual.varLen; i++) {
	      if (random.nextDouble() <= probability) {
	    	
	        y = (double)individual.getVariables2(i);
	        
	        yl = (double)bound.getLowerBound2(i);
	        yu = (double)bound.getUpperBound2(i);
	        if (yl == yu) {
	          y = yl ;
	        } else {
	          delta1 = (y - yl) / (yu - yl);
	          delta2 = (yu - y) / (yu - yl);
	          rnd = random.nextDouble();
	          mutPow = 1.0 / (distributionIndex + 1.0);
	          if (rnd <= 0.5) {
	            xy = 1.0 - delta1;
	            val = 2.0 * rnd + (1.0 - 2.0 * rnd) * (Math.pow(xy, distributionIndex + 1.0));
	            deltaq = Math.pow(val, mutPow) - 1.0;
	          } else {
	            xy = 1.0 - delta2;
	            val = 2.0 * (1.0 - rnd) + 2.0 * (rnd - 0.5) * (Math.pow(xy, distributionIndex + 1.0));
	            deltaq = 1.0 - Math.pow(val, mutPow);
	          }
	          y = y + deltaq * (yu - yl);
	          y = MyUtils.randValue(y, yl, yu);
	        }
	        individual.setVariables2(i, (int) y);
	      }
	    }
	    

  }
	

	
	//进行二进制锦标赛选择   Test OK
	public List<Individual> TournamentSelection(List<Individual> population) {
		
		List<Individual> cpopulation = new ArrayList<>(getPopSize());
		Random rand = new Random();
		int induIndex1 = 0;
		int induIndex2 = 0;
		int dominanceFlag = 0;
		int selectIndex = 0;
		FastNonDominatedSortRanking2 fndsr = new FastNonDominatedSortRanking2 ();
    	for (int i=0;i<getPopSize();i++) {
    		//选俩
    		induIndex1 = rand.nextInt(getPopSize());
    		induIndex2 = rand.nextInt(getPopSize());
    		
    		dominanceFlag = fndsr.compare(population.get(induIndex1), population.get(induIndex2));
    		if (dominanceFlag == -1) {
    			selectIndex = induIndex1;
    		} else {
    			selectIndex = induIndex2;
    		}

    		//再放置
    		cpopulation.add(population.get(selectIndex));
        		
    	}
    	
    	population = cpopulation;
    	return population;
    }
	
	
	


	    //产生子代 Test OK
		public List<Individual> reproduction2(List<Individual> population,double crossoverProbability,double mutationProbability) {
		  List<Individual> offspringPopulation = new ArrayList<>(getPopSize());
		  
		  
		  //选择交配
		  population = TournamentSelection(population);


		  for (int i = 0; i < getPopSize(); i+=2) {//注意这里是+2
		    List<Individual> parents = new ArrayList<>(2);
		    
		    parents.add(population.get(i));
		    parents.add(population.get(Math.min(i + 1, getPopSize()-1)));

		    
		    //交叉变异
		    List<Individual> offspring = Intercrossover(crossoverProbability, parents.get(0), parents.get(1));
		    
		    //
		    offspring = IntercrossoverOrder(crossoverProbability, offspring.get(0), offspring.get(1));

		    Intermutation(mutationProbability, offspring.get(0));
		    //
		    IntermutationOrder(mutationProbability, offspring.get(0));
		    
		    Intermutation(mutationProbability, offspring.get(1));
		    //
		    IntermutationOrder(mutationProbability, offspring.get(1));
		    

		    

		    
		    

		    //再计算下fitness
//		    evaluatePop(offspring); //暂不评估，留到ELS之后
		    
		    offspringPopulation.add(offspring.get(0));
		    offspringPopulation.add(offspring.get(1));
		  }
		  
		    //ELS
		  offspringPopulation =  eliteLearningStrategy(offspringPopulation, archive);
		  //全部留在这里评估
		  evaluatePop(offspringPopulation);
		  return offspringPopulation ;
		}
	
	
		  
     //参考点的复制
	  private List<ReferencePoint2<Individual>> getReferencePointsCopy() {
		  List<ReferencePoint2<Individual>> copy = new ArrayList<>();
		  for (ReferencePoint2<Individual> r : this.referencePoints) {
			  copy.add(new ReferencePoint2<>(r));
		  }
		  return copy;
	  }
		  
		  
 
	
	//
		//preferenceRegionSort   PRRS
		public  List<List<Integer>>  preferenceRegionRankingStrategy(List<Individual> jointPopulation) {
			//preferenceRegionSort
			int preFlagArray[]  = getPreferredFlag(jointPopulation);
			List<Integer> A1 = new ArrayList<>();
			List<Integer> A2 = new ArrayList<>();
			List<Integer> A3 = new ArrayList<>();
			//保存三个区的个体序号
			for(int i=0;i<preFlagArray.length;i++) {
				if(preFlagArray[i] == 1) {
					A1.add(Integer.valueOf(i));
				}else if (preFlagArray[i] == 2) {
					A2.add(Integer.valueOf(i));
				}else {
					A3.add(Integer.valueOf(i));
				}
				
			}
			
			//
			List<List<Integer>> FrontLayer = new ArrayList<>();
			FrontLayer.add(A1);
			FrontLayer.add(A2);
			FrontLayer.add(A3);
			
			return FrontLayer;
		}
		
		
		//从混合种群中选择出单个种群
		
		public List<Individual> replacement2(List<Individual> population, List<Individual> offspringPopulation) {
		
			List<Individual> jointPopulation = new ArrayList<>();
			List<Individual> choosen = new ArrayList<>();
			
			//将父代和子代种群加入到混合种群中
			for(int i=0;i<population.size();i++) {
				jointPopulation.add(population.get(i)) ;
			}
			for(int i=0;i<offspringPopulation.size();i++) {
				jointPopulation.add(offspringPopulation.get(i)) ;
			}

			//顺带生成archive
			generalArchive(jointPopulation);
			
			//preferenceRegionSort
			List<List<Integer>> FrontLayer =   preferenceRegionRankingStrategy(jointPopulation);
	
			//
			List<Integer> canInPopIndivi = new ArrayList<>();
			List<Integer> lastRegionIndivi = new ArrayList<>();
			for(int i=0;i<FrontLayer.size();i++) {
				if (canInPopIndivi.size() + FrontLayer.get(i).size() <= getPopSize()) {
					canInPopIndivi.addAll(FrontLayer.get(i));
					
					
				} else {
					lastRegionIndivi.addAll(FrontLayer.get(i));
					break;
				}
			}
			
			//对最优一个区域进行非支配排序
			List<Individual> lastRegionIndiviPopulation = new ArrayList<>();
			for(int i=0;i<lastRegionIndivi.size();i++) {
				lastRegionIndiviPopulation.add(jointPopulation.get(lastRegionIndivi.get(i).intValue()));
			}
				
			
			
			FastNonDominatedSortRanking2  fndsr = new FastNonDominatedSortRanking2();
			List<ArrayList<Individual>> ranking = fndsr.computeRanking(lastRegionIndiviPopulation);
			

			
			List<Individual> last = new ArrayList<>();
			List<Individual> pop = new ArrayList<>();
			List<List<Individual>> fronts = new ArrayList<>();
			int rankingIndex = 0;
			int candidateSolutions = canInPopIndivi.size();
			
			
			//先把之前的保存到pop
			for(int i=0;i<canInPopIndivi.size();i++) {
				pop.add(jointPopulation.get(canInPopIndivi.get(i).intValue()));
			}
			
			
			//再添加从 lastRegion中选择的 一直添加，直到刚好满足或者超过了popSize的大小
			while (candidateSolutions < getPopSize()) {				
			  last = fndsr.getSubFront(rankingIndex);			  
			  fronts.add(last);
			  candidateSolutions += last.size();
			  if ((pop.size() + last.size()) <= getPopSize()) {
				  for(int j=0;j<last.size();j++) {
					  pop.add(last.get(j));
				  }
				 
			  }			    
			  rankingIndex++;
			}
			if (pop.size() == this.getPopSize()) {
				return pop;
			}
			
//			
			EnvironmentalSelection2 selection =
			        new EnvironmentalSelection2(fronts,getPopSize() - pop.size(),getReferencePointsCopy(),
			               nobj);	
			
			
			choosen = selection.execute(last);
			for(int j=0;j<choosen.size();j++) {
				pop.add(choosen.get(j));
			}
			
			 
			return pop;
		}


		//算archive  算population
		public void generalArchive(List<Individual> population) {
			
			if (switchFlag == 1) {
				//第一层放入archive中
				FastNonDominatedSortRanking2  fndsr = new FastNonDominatedSortRanking2();
				List<ArrayList<Individual>> ranking = fndsr.computeRanking(population);
				List<Individual> firstLayerIndividuals = ranking.get(0);//
				archive.addAll(firstLayerIndividuals);
				
//				System.out.println(archive.size());
				//然后再求archive的第一层	
				fndsr = new FastNonDominatedSortRanking2();
				ranking = fndsr.computeRanking(archive);
				firstLayerIndividuals = ranking.get(0);//
				archive.clear();//清掉元素
				archive.addAll(firstLayerIndividuals);
			} else {
				
				//第一层放入archive中
				
				archive.addAll(population);
				
//				System.out.println(archive.size());
				//然后再求archive的第一层	
				FastNonDominatedSortRanking2 fndsr = new FastNonDominatedSortRanking2();
				List<ArrayList<Individual>> ranking = fndsr.computeRanking(archive);
				List<Individual> firstLayerIndividuals = ranking.get(0);//
				archive.clear();//清掉元素
				archive.addAll(firstLayerIndividuals);
			}
			
			
			
			
			
			
			//如果第一层超过了archivesize,则环境选择
			if (archive.size() > getArchivepSize()) {
				
				FastNonDominatedSortRanking2 fndsr2 = new FastNonDominatedSortRanking2();
				List<ArrayList<Individual>> ranking2 = fndsr2.computeRanking(archive);
				

				
				List<Individual> last = new ArrayList<>();
				List<Individual> pop = new ArrayList<>();
				List<List<Individual>> fronts = new ArrayList<>();
				int rankingIndex = 0;
				int candidateSolutions = 0;
				
			
				
				//再添加从 lastRegion中选择的 一直添加，直到刚好满足或者超过了popSize的大小
				while (candidateSolutions < getArchivepSize()) {				
				  last = fndsr2.getSubFront(rankingIndex);			  
				  fronts.add(last);
				  candidateSolutions += last.size();
				  if ((pop.size() + last.size()) <= getArchivepSize()) {
					  for(int j=0;j<last.size();j++) {
						  pop.add(last.get(j));
					  }
					 
				  }			    
				  rankingIndex++;
				}
				
				if (pop.size() == this.getArchivepSize()) {
					archive = pop;
				}
				
//				
				EnvironmentalSelection2 selection =
				        new EnvironmentalSelection2(fronts,getArchivepSize() - pop.size(),getReferencePointsCopy(),
				               nobj);	
				
				
				List<Individual> choosen = selection.execute(last);
				for(int j=0;j<choosen.size();j++) {
					pop.add(choosen.get(j));
				}
				
				archive = pop;
			}
		}
		
		
		
		
		//从混合种群中选择出单个种群 使用拥挤距离
			public List<Individual> replacement3(List<Individual> population, List<Individual> offspringPopulation) {
			
				List<Individual> jointPopulation = new ArrayList<>();
				
				
				//将父代和子代种群加入到混合种群中
				for(int i=0;i<population.size();i++) {
					jointPopulation.add(population.get(i)) ;
				}
				for(int i=0;i<offspringPopulation.size();i++) {
					jointPopulation.add(offspringPopulation.get(i)) ;
				}


				//测试输出objectives
				

				FastNonDominatedSortRanking2  fndsr = new FastNonDominatedSortRanking2();

				List<ArrayList<Individual>> ranking = fndsr.computeRanking(jointPopulation);
				

				
				
				//NSGA2中的步骤
				List<Individual> last = new ArrayList<>();
				List<Individual> pop = new ArrayList<>();
				List<List<Individual>> fronts = new ArrayList<>();
				

				int rankingIndex = 0;
				int candidateSolutions = 0;
				
				
				//一直添加，直到刚好满足或者超过了popSize的大小
				while (candidateSolutions < getPopSize()) {				
				  last = fndsr.getSubFront(rankingIndex);		

				  fronts.add(last);
				  candidateSolutions += last.size();
				  if ((pop.size() + last.size()) <= getPopSize()) {
					  for(int j=0;j<last.size();j++) {
						  pop.add(last.get(j));
					  }
					 
				  }			    
				  rankingIndex++;
				}
				if (pop.size() == this.getPopSize()) {
					return pop;
				}
				   
//					System.out.println(getPopSize());
				pop = crowdDistance(getPopSize() - pop.size(), pop,last);
				
				
				 
				return pop;
			}

			
			
			//拥挤距离选择
		
			public List<Individual>  crowdDistance(int need,List<Individual> pop,List<Individual> last) {
				
				double fitnessArray[][] = new double[last.size()][nobj];
				double fitnessArrayNew[][] = new double[last.size()][nobj];
				double eps = 0.0000000000000001;
				double maxAndMinMatrix[][] = new double[2][nobj];//%第一行表示最大值，第二行最小值，第一列是第
				maxAndMinMatrix[0][0] = -10000000000000.0;
				maxAndMinMatrix[0][1] = -10000000000000.0;
				maxAndMinMatrix[1][0] = 10000000000000.0;
				maxAndMinMatrix[1][1] = 10000000000000.0;
				//先存入数组
				for(int i=0;i<last.size();i++) {
					for(int j=0;j<nobj;j++) {
						fitnessArray[i][j] = last.get(i).getObjectives(j);
					}
					
				}
				
				//对某一列排序
//					System.out.println(last.size());
//					System.out.println("----------");
				double oneObjec[] = new double[last.size()];
				for(int i=0;i<last.size();i++) {
					oneObjec[i] = fitnessArray[i][0];
				
				}
				
//					System.out.println(oneObjec[0]);
				int oriIndex[] = MyUtils.sortIndex(oneObjec);  //原有的顺序
//					for(int i=0;i<oriIndex.length;i++) {
//						System.out.print(oriIndex[i]);
//					}
//					
//					System.out.println("----------");
				//跟着变化
				for(int i=0;i<last.size();i++) {
					for (int j=0;j<nobj;j++) {
						fitnessArrayNew[i][j] = fitnessArray[oriIndex[i]][j];
					}
//						System.out.println(fitnessArrayNew[i][0] + " yy" + fitnessArrayNew[i][1]);
				}
				
				//计算拥挤距离
				double crowdD[] = new double[last.size()];
				if(last.size()==2) {
					crowdD[0] = 100000000.0;
					crowdD[1] = 100000000.0;
				} else {
					//计算最小值和最大值

					for(int i=0;i<last.size();i++) {
						for(int j=0;j<nobj;j++) {
							
							if (fitnessArray[i][j]>maxAndMinMatrix[0][j]) {
								maxAndMinMatrix[0][j] = fitnessArray[i][j];
							}
							
							if (fitnessArray[i][j]<maxAndMinMatrix[1][j]) {
								maxAndMinMatrix[1][j] = fitnessArray[i][j];
							} 


						}
					}
				}
				
				//拥挤距离计算
				double crow = 1.0;
				for(int i=0;i<last.size();i++) {
					if(i==0 || i+1 ==last.size()) {
						crowdD[i] = 10000000000.0;
					} else {
						for(int k=0;k<nobj;k++) {
							crowdD[i] = crow * Math.abs(fitnessArrayNew[i-1][k] - fitnessArrayNew[i+1][k]+eps)/(maxAndMinMatrix[0][k]-maxAndMinMatrix[1][k]+eps);
						}
						
					}
				}
				

				
				int oriIndex2[] = MyUtils.sortIndex(crowdD); 
				
				//加入到我所需要的队列中,从拥挤距离大那里开始挑
				int needArray[] = new int[need];
				int needIndex = 0;
				for (int i=last.size()-1;i>0;i--) {
					needArray[needIndex] = oriIndex[oriIndex2[i]];
					needIndex = needIndex + 1;
					if(needIndex == need) {
						break;
					}
				}
				
				//添加到pop中
				for(int i=0;i<need;i++) {
					pop.add(last.get(needArray[i]));
				}
				
				
				
				return pop;
			}
			
			
			//得到分区
			public static int[] getFlagArray(List<Individual> jointPopulation) {
				
				int popLen = jointPopulation.size();
				int flagArray[] = new int[popLen];
				int temp1 = 0;
				int temp2 = 0;
				int temp3 = 0;
				//初始化
				for(int i=0;i<popLen;i++) {
					flagArray[i] = 0;
				}
				
				//统计判断属于哪个区域
				for (int i=0;i<popLen;i++) {
					temp1 = 0;
					temp2 = 0;
					temp3 = 0;
					for(int j=0;j<nobj;j++) {
						if(p[j]<=jointPopulation.get(i).getObjectives(j)) {
							temp1 = temp1 + 1;
						}else if (p[j]>=jointPopulation.get(i).getObjectives(j)) {
							temp2 = temp2 + 1;
						} else {
//							temp3 = temp3 + 1;
						}
						
					}
					//对统计的的值分配值
					if (temp1 == nobj) {
						flagArray[i] = 1;
					}
					if (temp2 == nobj) {
						flagArray[i] = 1;
					}
//					if (temp3 == nobj) {
//						
//						flagArray[i] = 0;
//					}
				}
				
				return flagArray;
			}
			
			
			
			//得到偏好flag
			public static int[] getPreferredFlag(List<Individual> jointPopulation) { 
				int popLen = jointPopulation.size();
				int preferredFlagArray[] = new int[popLen];
				int temp1 = 0;
				int temp2 = 0;
				int temp3 = 0;
				//初始化
				for(int i=0;i<popLen;i++) {
					preferredFlagArray[i] = 3;//默认属于第三个区域
				}
				
				//统计判断属于哪个区域
				for (int i=0;i<popLen;i++) {
					temp1 = 0;
					temp2 = 0;
					temp3 = 0;
					for(int j=0;j<nobj;j++) {
						if(p[j]<=jointPopulation.get(i).getObjectives(j)) {
							temp1 = temp1 + 1;
						}else if (p[j]>=jointPopulation.get(i).getObjectives(j)) {
							temp2 = temp2 + 1;
						} else {
//							temp3 = temp3 + 1;
						}
						
					}
					//对统计的的值分配值
					if (temp1 == nobj) {
						preferredFlagArray[i] = 1;
					}
					if (temp2 == nobj) {
						preferredFlagArray[i] = 2;
					}
//					if (temp3 == nobj) {
//						
//						flagArray[i] = 0;
//					}
				}
				
				return preferredFlagArray;
			
			}
			
			
			//计算sigma判断是否转换
			public List<Individual> adaptiveStageTransitionStrategy(List<Individual> population, List<Individual> offspringPopulation) {
				meanPoint = calMeanFitness();
				List<Individual> pop = new ArrayList<>();
				if (switchFlag == 0) {
					for(int i=0;i<informationStaitc.length-1;i++) {
						informationStaitc[i] = informationStaitc[i+1];
					}
					informationStaitc[k_1-1] = euclideanDistance(p,meanPoint); 
					
					sigma = MyUtils.Variance(informationStaitc);
//					System.out.println(sigma);
					
					if (sigma < threshold) {
						switchFlag = 1;
					}
				}
				

				//转换
				if (switchFlag == 1 || iter >= (0.5*maxIter)) {
					pop = replacement2(population, offspringPopulation);
				} else {
					pop = preferenceDistanceStrategy(population, offspringPopulation);
				}
				return pop;
			}
			
			
			//计算欧式距离
			public double euclideanDistance(double g[],double point[]) {
				double temp = 0.0;
				double distance = 0.0;

				for(int i=0;i<nobj;i++) {
					temp = temp + Math.pow((g[i]- point[i]),2.0);
				}
				distance = Math.pow(temp, 0.5);
				return distance;
			}
			
			
			//PDS
			public List<Individual>  preferenceDistanceStrategy(List<Individual> population, List<Individual> offspringPopulation) {
				List<Individual> jointPopulation = new ArrayList<>();
				jointPopulation.addAll(population);
				jointPopulation.addAll(offspringPopulation);
				double preDistanceArray[] = new double[jointPopulation.size()];
				for(int i=0;i<jointPopulation.size();i++) {
					preDistanceArray[i] = euclideanDistance(p,jointPopulation.get(i).getAllObjectives());
				}
				//对距离排序
				int sortIndex[] =  MyUtils.sortIndex(preDistanceArray);
				List<Individual> pop = new ArrayList<>();
				for(int i=0;i<getPopSize();i++) {
					pop.add(jointPopulation.get(sortIndex[i]));
				}
				generalArchive(pop);//形成archive
				return pop;
				
			}
			
			
			
			
			//精英学习策略
			public  List<Individual> eliteLearningStrategy(List<Individual> population, List<Individual> archvie) {
				List<Individual> leader = new ArrayList<>();
				Random random = new Random(); 
				int temp;
				//形成leader
				for(int i=0;i<popSize;i++) {
					temp = 0 + random.nextInt(archive.size()- 0);//在指定范围内生成
					leader.add(archvie.get(temp));
				}
				
				//leader和population交叉
				Individual t1;
				Individual t2;
				double crossProb = 0.05;
				int crossLen = (int) Math.ceil(dimension*crossProb);
				Set<Integer> crossIndexArrayIntegers;
				int crossIndex;
				
				for(int i=0;i<popSize;i++) {
					crossIndexArrayIntegers = MyUtils.getRandoms(0, dimension, crossLen);
					t1 = population.get(i);
					t2 = leader.get(i);
					//产生部分交换的gene
					Iterator<Integer> it = crossIndexArrayIntegers.iterator();
					while (it.hasNext()) {
						crossIndex = it.next().intValue();
						t1.setVariables(crossIndex, t2.getVariables(crossIndex));
						t1.setVariables2(crossIndex, t2.getVariables2(crossIndex));
					}
					population.set(i, t1);		
				}
				
				return population;
			}

		
}
	
	




