package org.uma.jmetal.algorithm.multiobjective.tspmoea;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.netlib.util.doubleW;
import java_cup.internal_error;


// (a,b,c), 取值范围,[a,b),  如果[0,10),10就会每次结果一样
public class MyUtils {
	
    public static void main(String[] args) {
    	Random random = new Random();
    	int a = 2;
    	double b = a;
    	System.out.print(b);
//    	for(int i=0;i<10;i++) {
//    		System.out.println(random.nextInt(10));
//	}
//    	
		
//    	int oriSortIndex[] = MyUtils.generateUqiInt(10);
//    	for(int i=0;i<10;i++) {
//            System.out.println(oriSortIndex[i]);
//    	}
    	
    	
    }

    
    //Generate unique integers  
    public static int[] generateUqiInt(int len) {
    	Random ra = new Random();
    	double temp[] = new double[len];
    	for(int i=0;i<len;i++) {
    		temp[i] = ra.nextDouble();
    	}
    	int oriSortIndex[] = MyUtils.sortIndex(temp); 
    	return oriSortIndex;
    }
    
    
    /**
     * 生成一组不重复随机数 (a,b,c)  范围是 [a,b)
     *
     * @param start 开始位置：可以为负数
     * @param end   结束位置：end > start
     * @param count 数量 >= 0
     * @return
     */
	
	public static double randValue(double y,double low,double up) {

		Random ra = new Random();
		double result = y;
		if (y<low) {
			result = low + ra.nextDouble()*(up - low);
		}
		if (y>up) {
			result = low + ra.nextDouble()*(up - low);
		}
		return result;
	}
	
	
	
    public static Set<Integer> getRandoms(int start, int end, int count) {
        // 参数有效性检查
        if (start > end || count < 1) {
            count = 0;
        }
        // 结束值 与 开始值 的差小于 总数量
        if ((end - start) < count) {
            count = (end - start) > 0 ? (end - start) : 0;
        }

        // 定义存放集合
        Set<Integer> set = new HashSet<>(count);
        if (count > 0) {
            Random r = new Random();
            // 一直生成足够数量后再停止
            while (set.size() < count) {
                set.add(start + r.nextInt(end - start));
            }
        }
        return set;
    }
    
    
    //排序并返回序号，这是从小到大的排序
    public static int[] sortIndex(double a[]) {
		int count = 0;//用于加入到数组中
		int oriSortIndex[] = new int[a.length];
		Number sorted[] = new Number[a.length];
        for (int i = 0; i < a.length; ++i) {
            sorted[i] = new Number(a[i], i);
        }
        Arrays.sort(sorted);
      //print sorted array
//        for (Number n : sorted){
//            System.out.print("" + n.data +",");
//        }
//        System.out.println();

        // print original index
    
        for (Number n: sorted){
//            System.out.print("" + n.index + ",");
            oriSortIndex[count++] = n.index;
        }
        return oriSortIndex;
        
	}
    
    
  //方差s^2=[(x1-x)^2 +...(xn-x)^2]/n 或者s^2=[(x1-x)^2 +...(xn-x)^2]/(n-1)
    public static double Variance(double[] x) {
        int m=x.length;
        double sum=0;
        for(int i=0;i<m;i++){//求和
            sum+=x[i];
        }
        double dAve=sum/m;//求平均值
        double dVar=0;
        for(int i=0;i<m;i++){//求方差
            dVar+=(x[i]-dAve)*(x[i]-dAve);
        }
        return dVar/m;
    }

    
    //标准差σ=sqrt(s^2)
    public static double StandardDiviation(double[] x) {
        int m=x.length;
        double sum=0;
        for(int i=0;i<m;i++){//求和
            sum+=x[i];
        }
        double dAve=sum/m;//求平均值
        double dVar=0;
        for(int i=0;i<m;i++){//求方差
            dVar+=(x[i]-dAve)*(x[i]-dAve);
        }
                //reture Math.sqrt(dVar/(m-1));
        return Math.sqrt(dVar/m);
    }



    
    
}


//辅助，排序并返回原来序号的方法
class Number implements Comparable<Number>{
  Double data;
  int index;

  Number(double d, int i){
      this.data = d;
      this.index = i;
  }
  
  @Override
  public int compareTo(Number o) {
      return this.data.compareTo(o.data);
  }
}
