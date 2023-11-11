package TSPMOEA;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.workflowsim.Task;



public class Tool {
	
	//workflow序号
	public static int workflowIndex = -1;
    //两个约束
    public static double k1 = 0.25;
    public static double k2 = 0.25;
	
	//host数目
	public static int hostNum = 0;
    //资源数量
    public static  int VmNum = 0;
    //任务数量
    public static  int TaskNum = 0;//文件中实际的task数目
    //
    public static int fileTaskNum = 0;//用于读取文件
    //迭代次数
    public static final int IterationNum = 500;
    //种群数量
    public static final int populationSize = 105;
    
    //偏好点
    public static final double p[] = {0.7,0.7,0.7};
    
    public static final int nobj = 3;
    
    public static final int divisions = 13;// （2,99） （3,13）
    
    public static int k_1 = 5;//
    
    public static double threshold = 1e-6;
    //

    

    
    //解析文件的名字 
    public static char wfFileName[] = {'1'};
    //workflow 名字
    public static char workflowName[] = {'1'};

    //vmInfolist
    public static List<VMInfo> VMInfoList =   null;

    public static List<HostInfo> HostInfoList =  null;
    //得到makespan和cost的两个区间
    public static List<JobMakespanInfo> JobMakespanInfoList =   null;
    public static List<JobCostInfo> JobCostInfoList = null;
    //三个文件中的数据
    public static List<JobBudgetInfo> JobBudgetInfoList =   null;
    public static List<JobDeadlineInfo> JobDeadlineInfoList =   null;
    public static List<JobEnergyInfo> JobEnergyInfoList =   null;


    public static double jobBudget = 0;
    public static double jobDeadline = 0;
    public static double jobEnergy = 0;
    
    

    //tasklist ,vmlist
    public static List<Task> tasktList ;
    public static List<?> vmList;

    
    public static void setVmList(List<?> list) {
        Tool.vmList = list;
    }

    public static void setTaskList(List<Task> list) {
    	Tool.tasktList = list;
    }
    public List<Task> getTaskList() {
        return tasktList;
    }

    public List<?> getVmList() {
        return vmList;
    }

    
    //当前进行任务分配方案
    public static int[] allot= new int[Tool.TaskNum];
    public static int[] taskOrder = new int[Tool.TaskNum];

    
  //根据约束值  产生jobMakespan.txt   jobCost.txt文件
    public static void dataProcessMakespanCost() throws IOException {
    	//得到jobmakespan和jobcost
    	JobMakespanInfoList = JobMakespanInfo.readJobMakespanInfo(LoadInfo.jobMakespanFileName);
    	JobCostInfoList = JobCostInfo.readJobCostInfo(LoadInfo.jobCostFileName);
    	
    	FileWriter fw1 = new FileWriter("G:\\eclipseWorksapce\\tsp-MOEA(v1)\\WorkflowSim-1.0-master\\examples\\TSPMOEA\\jobBudget"+".txt");
    	FileWriter fw2 = new FileWriter("G:\\eclipseWorksapce\\tsp-MOEA(v1)\\WorkflowSim-1.0-master\\examples\\TSPMOEA\\jobDeadline"+".txt");
    	
    	//
    	char wfFileName[];
    	double makespanDiff = 0.0;
    	double costDiff = 0.0;
    	//计算
    	for(int i=0;i<JobMakespanInfoList.size();i++) {
    		JobMakespanInfo jobMakespanInfo = JobMakespanInfoList.get(i);
    		JobCostInfo jobCostInfo = JobCostInfoList.get(i);
    		wfFileName = jobMakespanInfo.wfFileName.clone();
    		makespanDiff = jobCostInfo.makespan - k1*(jobCostInfo.makespan - jobMakespanInfo.makespan);
    		costDiff = jobMakespanInfo.cost  - k2*(jobMakespanInfo.cost - jobCostInfo.cost);
    		
    		//写入文件
    		
    		fw1.write(new String(wfFileName)+" ");	
    		fw1.write(costDiff+"\n");	
    		fw2.write(new String(wfFileName)+" ");	
    		fw2.write(makespanDiff+"\n");	
    		
    	}
    	fw1.close();
    	fw2.close();
    	
    	
    			
    }


    //配制基本的参数  task数目，vm数目，host数目
    public static void configureParameters(int workflowIndex) throws IOException {

    	//产生jobdeadline.txt  jobbudget.txt文件
    	dataProcessMakespanCost();
    	
    	//解决job
    	List<JobInfo> JobInfoList = JobInfo.readJobInfo(LoadInfo.jobFileName);
    	JobInfo job = JobInfoList.get(workflowIndex);
    	
    	if (job.taskNum == 0) {
    		System.out.println("LoadInfo类里面的路径可能错了~");
    		return ;
    	}
    	Tool.fileTaskNum = job.taskNum;
    	Tool.wfFileName = job.wfFileName.clone();
    	Tool.workflowName = job.workflowName.clone();
    	
    	if (Tool.fileTaskNum > 0 && Tool.fileTaskNum<=30 ) {
    		Tool.VmNum = 5;
    		Tool.hostNum = 1;
    	}
    	if (Tool.fileTaskNum > 30 && Tool.fileTaskNum<=60 ) {
    		Tool.VmNum = 10;
    		Tool.hostNum = 2;
    	}
    	if (Tool.fileTaskNum > 60 && Tool.fileTaskNum<=100 ) {
    		Tool.VmNum = 15;
    		Tool.hostNum = 3;	
    	}
    	if (Tool.fileTaskNum > 100 && Tool.fileTaskNum<=1000 ) {
    		Tool.VmNum = 20;
    		Tool.hostNum = 4; 		
    	}
    	
    	
    	//解决vm
    	List<VMInfo> VMInfoList1 = VMInfo.readVMInfo(LoadInfo.vmFileName);
    	List<VMInfo> VMInfoList2 =   new ArrayList<>();
    	for(int i=0;i<Tool.VmNum;i++) {
    		VMInfoList2.add(VMInfoList1.get(i));
    	}
    	VMInfoList = VMInfoList2;
    	
    	//解决Host
    	List<HostInfo> HostInfoList1 = HostInfo.readHostInfo(LoadInfo.hostFileName);
    	List<HostInfo> HostInfoList2 =   new ArrayList<>();
    	for(int i=0;i<Tool.hostNum;i++) {
    		HostInfoList2.add(HostInfoList1.get(i));
    	}
    	HostInfoList = HostInfoList2;
    	
    	//
    	
    	//解决budget deadline randAlgorithmEnergy
    	JobBudgetInfoList = JobBudgetInfo.readJobBudgetInfo(LoadInfo.jobBudgetFileName);
    	JobDeadlineInfoList = JobDeadlineInfo.readJobDeadlineInfo(LoadInfo.jobDeadlineFileName);
    	JobEnergyInfoList = JobEnergyInfo.readJobEnergyInfo(LoadInfo.jobEnergyFileName);
    	
    	jobBudget = JobBudgetInfoList.get(workflowIndex).budget;
    	jobDeadline = JobDeadlineInfoList.get(workflowIndex).deadline;
    	jobEnergy = JobEnergyInfoList.get(workflowIndex).energy;
    	

    }


}
