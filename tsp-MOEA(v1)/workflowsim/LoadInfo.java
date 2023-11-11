package TSPMOEA;


import java.io.*;
import java.util.ArrayList;
import java.util.List;



public class LoadInfo {


	/*注意下面这些文件的地址是否有问题*/
	//可能需要更换这里的参数
	static String vmFileName = new String("G:\\eclipseWorksapce\\tsp-MOEA(v1)\\WorkflowSim-1.0-master\\examples\\TSPMOEA\\vmInformation.txt");
	static String hostFileName = new String("G:\\eclipseWorksapce\\tsp-MOEA(v1)\\WorkflowSim-1.0-master\\examples\\TSPMOEA\\hostInformation.txt");
	static String jobFileName = new String("G:\\eclipseWorksapce\\tsp-MOEA(v1)\\WorkflowSim-1.0-master\\examples\\TSPMOEA\\allJobName.txt");
	static String jobBudgetFileName = new String("G:\\\\eclipseWorksapce\\tsp-MOEA(v1)\\WorkflowSim-1.0-master\\examples\\TSPMOEA\\jobBudget.txt");
	static String jobDeadlineFileName = new String("G:\\\\eclipseWorksapce\\tsp-MOEA(v1)\\WorkflowSim-1.0-master\\examples\\TSPMOEA\\jobDeadline.txt");
	static String jobEnergyFileName = new String("G:\\\\eclipseWorksapce\\tsp-MOEA(v1)\\WorkflowSim-1.0-master\\examples\\TSPMOEA\\jobDataCenterEnergyRandAlgorithm.txt");
	static String jobMakespanFileName = new String("G:\\\\eclipseWorksapce\\tsp-MOEA(v1)\\WorkflowSim-1.0-master\\examples\\TSPMOEA\\jobMakespan.txt");//matlab版本是makespan.txt
	static String jobCostFileName = new String("G:\\\\eclipseWorksapce\\tsp-MOEA(v1)\\WorkflowSim-1.0-master\\examples\\TSPMOEA\\jobCost.txt");
	
	public static void main(String[] args) throws Exception {
//		VMInfo.readVMInfo(vmFileName);
//		HostInfo.readHostInfo(hostFileName);
//		JobInfo.readJobInfo(jobFileName);
//		JobBudgetInfo.readJobBudgetInfo(jobBudgetFileName);
//		JobDeadlineInfo.readJobDeadlineInfo(jobDeadlineFileName);
//		JobEnergyInfo.readJobEnergyInfo(jobEnergyFileName);
//		JobMakespanInfo.readJobMakespanInfo(jobMakespanFileName);
		JobCostInfo.readJobCostInfo(jobCostFileName);
		System.out.println("finish");
	}

	
		

	
	
	
}


//vminfo类

class VMInfo {
	//vm等信息
	int vmId = 0;
	char vmName[];
	int hostId = 0;
	int vCPU = 0;
	int memory = 0;
	int storage = 0;
	double bandWidth  = 0.0;
	double mips = 0.0;
	double vmCost = 0.0;
	double storageCost = 0.0;
	double bandwidthCost = 0.0;
	

	//获取
	public static List<VMInfo> readVMInfo(String fileName) throws IOException {
		
		List<VMInfo> VMInfoList = new ArrayList<>();
		File file = new File(fileName);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String st;
		int index ;
	
		while ((st = br.readLine()) != null) {
			VMInfo vm = new VMInfo();
			index = 0;
			//分割字符
	        for (String retval: st.split(" ")){
//	            System.out.println(retval); 
	            
	            switch(index)
	            {
	               case 0 :
	            	  vm.vmId = Integer.parseInt(retval);
	                  break;
	               case 1 :
	            	  vm.vmName = (retval.toCharArray()).clone();
	                  break; 
	               case 2 :
	            	  vm.hostId = Integer.parseInt(retval);
	                  break;
	               case 3 :
	            	  vm.vCPU = Integer.parseInt(retval);
	                  break;
	               case 4 :
	            	  vm.memory = Integer.parseInt(retval);
	                  break;
	               case 5 :
	            	  vm.storage = Integer.parseInt(retval);
	                  break;
	               case 6 :
	            	  vm.bandWidth = Double.valueOf(retval.toString());
	                  break;
	               case 7 :
	            	  vm.mips = Double.valueOf(retval.toString());
	                  break;
	               case 8 :
	            	  vm.vmCost = Double.valueOf(retval.toString());
	                  break;
	               case 9 :
	            	  vm.storageCost = Double.valueOf(retval.toString());
	                  break;
	               case 10 :
	            	  vm.bandwidthCost = Double.valueOf(retval.toString());
	                  break;
	               default :
	                  System.out.println("default");
	                  break;
	            }
	            index = index + 1;
//	            
	        }
            VMInfoList.add(vm);
            

		}
		br.close();
		return VMInfoList;
	}
}




class HostInfo {
	//host等信息
	int hostId = 0;
	char hostName[];
	char cpuName[];
	double powerMax = 0.0;

	//获取
	public static List<HostInfo> readHostInfo(String fileName) throws IOException {
		
		List<HostInfo> HostInfoList = new ArrayList<>();
		File file = new File(fileName);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String st;
		int index;
		while ((st = br.readLine()) != null) {
			HostInfo host = new HostInfo();
			index=0;
			//分割字符
	        for (String retval: st.split(" ")){
	            //System.out.println(retval);  
	        	
	            switch(index)
	            {
	               case 0 :
	            	  host.hostId = Integer.parseInt(retval);
	                  break;
	               case 1 :
	            	  host.hostName =  (retval.toCharArray()).clone();
	            	 
	                  break; 
	               case 2 :
            	      host.cpuName =  (retval.toCharArray()).clone();
	                  break; 
	               case 3 :
	            	  host.powerMax = Double.valueOf(retval.toString());
	                  break;
	               default :
	                  System.out.println("default");
	                  break;
	            }
	            index = index + 1;

//	            
	        }
            HostInfoList.add(host);


		}
		br.close();
	
		return HostInfoList;
	}
}


//job类，一个job=workflow
class JobInfo {
	//host等信息
	char wfFileName[];//文件全称
	char workflowName[];
	int taskNum = 0;
	//获取
	public static List<JobInfo> readJobInfo(String fileName) throws IOException {
		
		List<JobInfo> JobInfoList = new ArrayList<>();
		File file = new File(fileName);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String st;
		while ((st = br.readLine()) != null) {
			JobInfo job = new JobInfo();
			job.wfFileName =  (st.toCharArray()).clone();
			String[] ss1 =  st.split("_");

			job.workflowName =  (ss1[0].toCharArray()).clone();
			String[] ss2 =  ss1[1].split("\\.");
			
			job.taskNum =  Integer.parseInt(ss2[0]);
            JobInfoList.add(job);   
		}
		br.close();
	
//		System.out.println(JobInfoList.get(2).taskNum);
//		System.out.println(JobInfoList.get(2).wfFileName);
//		System.out.println(JobInfoList.get(2).workflowName);
		return JobInfoList;
	}
}



//jobbudget
class JobBudgetInfo {
	//host等信息
	char wfFileName[];//文件全称
	char workflowName[];
	int taskNum = 0;
	double budget = 0;
	//获取
	public static List<JobBudgetInfo> readJobBudgetInfo(String fileName) throws IOException {
		
		List<JobBudgetInfo> JobBudgetList = new ArrayList<>();
		File file = new File(fileName);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String str;
		int index;
		while ((str = br.readLine()) != null) {
			JobBudgetInfo jobBudget = new JobBudgetInfo();
			index = 0;
			//分割字符
	        for (String st: str.split(" ")){
//	            System.out.println(retval); 
	            
	            switch(index)
	            {
	               case 0 :
	            	   jobBudget.wfFileName =  (st.toCharArray()).clone();
		       			String[] ss1 =  st.split("_");
	
		       			jobBudget.workflowName =  (ss1[0].toCharArray()).clone();
		       			String[] ss2 =  ss1[1].split("\\.");
		       			
		       			jobBudget.taskNum =  Integer.parseInt(ss2[0]);
		       			// 
	                  break;
	               case 1 :
	            	  jobBudget.budget = Double.valueOf(st.toString());
	                  break; 
	               default :
	                  System.out.println("default");
	                  break;
	            }
	            index = index + 1;
	            
	        }
	        JobBudgetList.add(jobBudget);  
	        
		}
		br.close();
		return JobBudgetList;
	}
}





//jobdeadline
class JobDeadlineInfo {
	//host等信息
	char wfFileName[];//文件全称
	char workflowName[];
	int taskNum = 0;
	double deadline = 0;
	//获取
	public static List<JobDeadlineInfo> readJobDeadlineInfo(String fileName) throws IOException {
		
		List<JobDeadlineInfo> JobDeadlineInfoList = new ArrayList<>();
		File file = new File(fileName);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String str;
		int index;
		while ((str = br.readLine()) != null) {
			JobDeadlineInfo jobDeadline = new JobDeadlineInfo();
			index = 0;
			//分割字符
	        for (String st: str.split(" ")){
//	            System.out.println(retval); 
	            
	            switch(index)
	            {
	               case 0 :
	            	   jobDeadline.wfFileName =  (st.toCharArray()).clone();
		       			String[] ss1 =  st.split("_");
	
		       			jobDeadline.workflowName =  (ss1[0].toCharArray()).clone();
		       			String[] ss2 =  ss1[1].split("\\.");
		       			
		       			jobDeadline.taskNum =  Integer.parseInt(ss2[0]);
		       			// 
	                  break;
	               case 1 :
	            	   jobDeadline.deadline = Double.valueOf(st.toString());
	                  break; 
	               default :
	                  System.out.println("default");
	                  break;
	            }
	            index = index + 1;
	            
	        }
	        JobDeadlineInfoList.add(jobDeadline);  
	        
		}
		br.close();
	
//		System.out.println(JobDeadlineInfoList.get(2).taskNum);
//		System.out.println(JobDeadlineInfoList.get(2).wfFileName);
//		System.out.println(JobDeadlineInfoList.get(2).workflowName);
//		System.out.println(JobDeadlineInfoList.get(2).deadline);
		return JobDeadlineInfoList;
	}
}



//jobdatacenterenergyRandAlgorithm
class JobEnergyInfo {
	//host等信息
	char wfFileName[];//文件全称
	char workflowName[];
	int taskNum = 0;
	double energy = 0;//randAlgorithm energy consumption
	//获取
	public static List<JobEnergyInfo> readJobEnergyInfo(String fileName) throws IOException {
		
		List<JobEnergyInfo> JobEnergyInfoList = new ArrayList<>();
		File file = new File(fileName);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String str;
		int index;
		while ((str = br.readLine()) != null) {
			JobEnergyInfo jobEnergy = new JobEnergyInfo();
			index = 0;
			//分割字符
	        for (String st: str.split(" ")){
//	            System.out.println(retval); 
	            
	            switch(index)
	            {
	               case 0 :
	            	   jobEnergy.wfFileName =  (st.toCharArray()).clone();
		       			String[] ss1 =  st.split("_");
	
		       			jobEnergy.workflowName =  (ss1[0].toCharArray()).clone();
		       			String[] ss2 =  ss1[1].split("\\.");
		       			
		       			jobEnergy.taskNum =  Integer.parseInt(ss2[0]);
		       			// 
	                  break;
	               case 1 :
	            	   jobEnergy.energy = Double.valueOf(st.toString());
	                  break; 
	               default :
	                  System.out.println("default");
	                  break;
	            }
	            index = index + 1;
	            
	        }
	        JobEnergyInfoList.add(jobEnergy);  
	        
		}
		br.close();
	
//		System.out.println(JobEnergyInfoList.get(2).taskNum);
//		System.out.println(JobEnergyInfoList.get(2).wfFileName);
//		System.out.println(JobEnergyInfoList.get(2).workflowName);
//		System.out.println(JobEnergyInfoList.get(2).energy);
		return JobEnergyInfoList;
	}
}




//JobMakespanInfo   matlab版本的是makespan.txt  java是 jobMakespan.txt
class JobMakespanInfo {
	//host等信息
	char wfFileName[];//文件全称
	double makespan = 0.0;
	double cost = 0.0;
	//获取
	public static List<JobMakespanInfo> readJobMakespanInfo(String fileName) throws IOException {
		
		List<JobMakespanInfo> JobMakespanInfoList = new ArrayList<>();
		File file = new File(fileName);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String str;
		int index;
		while ((str = br.readLine()) != null) {
			JobMakespanInfo jobMakespan = new JobMakespanInfo();
			index = 0;
			//分割字符
	        for (String st: str.split(" ")){
//	            System.out.println(retval); 
	            
	            switch(index)
	            {
	               case 0 :
	            	   jobMakespan.wfFileName =  (st.toCharArray()).clone();
		       		   
	                  break;
	               case 1 :
	            	   jobMakespan.makespan = Double.valueOf(st.toString());
	                  break; 
	               case 2 :
	            	   jobMakespan.cost = Double.valueOf(st.toString());
	                  break; 
	               default :
	                  System.out.println("default");
	                  break;
	            }
	            index = index + 1;
	            
	        }
	        JobMakespanInfoList.add(jobMakespan);  
	        
		}
		br.close();
	
		
//		System.out.println(JobMakespanInfoList.get(2).wfFileName);
//		System.out.println(JobMakespanInfoList.get(2).makespan);
//		System.out.println(JobMakespanInfoList.get(2).cost);
		return JobMakespanInfoList;
	}
}




//JobCostInfo  matlab版本点的是cost.txt
class JobCostInfo {
	//host等信息
	char wfFileName[];//文件全称
	double makespan = 0.0;
	double cost = 0.0;
	//获取
	public static List<JobCostInfo> readJobCostInfo(String fileName) throws IOException {
		
		List<JobCostInfo> JobCostInfoList = new ArrayList<>();
		File file = new File(fileName);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String str;
		int index;
		while ((str = br.readLine()) != null) {
			JobCostInfo jobCost = new JobCostInfo();
			index = 0;
			//分割字符
	        for (String st: str.split(" ")){
//	            System.out.println(retval); 
	            
	            switch(index)
	            {
	               case 0 :
	            	   jobCost.wfFileName =  (st.toCharArray()).clone();
		       		   
	                  break;
	               case 1 :
	            	   jobCost.cost = Double.valueOf(st.toString());
	                  break; 
	               case 2 :
	            	   jobCost.makespan = Double.valueOf(st.toString());
	                  break; 
	               default :
	                  System.out.println("default");
	                  break;
	            }
	            index = index + 1;
	            
	        }
	        JobCostInfoList.add(jobCost);  
	        
		}
		br.close();
	
		
//		System.out.println(JobCostInfoList.get(2).wfFileName);
//		System.out.println(JobCostInfoList.get(2).cost);
//		System.out.println(JobCostInfoList.get(2).makespan);
		return JobCostInfoList;
	}
}
