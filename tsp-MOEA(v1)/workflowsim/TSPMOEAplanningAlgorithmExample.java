package TSPMOEA;


import java.io.File;


import java.io.FileWriter;
import java.io.IOException;
import java.security.PublicKey;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.uma.jmetal.algorithm.multiobjective.tspmoea.Individual;
import org.uma.jmetal.algorithm.multiobjective.tspmoea.TSPMOEA;

import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.workflowsim.CondorVM;
import org.workflowsim.Job;
import org.workflowsim.Task;
import org.workflowsim.WorkflowDatacenter;
import org.workflowsim.WorkflowEngine;
import org.workflowsim.WorkflowParser;
import org.workflowsim.WorkflowPlanner;
import org.workflowsim.examples.WorkflowSimBasicExample1;
import org.workflowsim.utils.ClusteringParameters;
import org.workflowsim.utils.OverheadParameters;
import org.workflowsim.utils.Parameters;
import org.workflowsim.utils.ReplicaCatalog;







public class TSPMOEAplanningAlgorithmExample extends WorkflowSimBasicExample1{


    public static void main(String[] args) {
    	int experimentNum = 10;
    	int workflowNum = 20;
    	for(int workflowIndex = 0;workflowIndex<workflowNum;workflowIndex++) {
    		Tool.workflowIndex = workflowIndex;
    		for(int experIndex = 0;experIndex<experimentNum;experIndex++) {

	    	long startTime = System.currentTimeMillis();
	    	try {
	    		
	    		//写入数据
	    		FileWriter fw = new FileWriter("G:\\eclipseWorksapce\\tsp-MOEA(v1)\\WorkflowSim-1.0-master\\examples\\TSPMOEA\\results\\" +  Tool.workflowIndex+"_" + experIndex + ".txt");
	    		//配置和初始化相应参数
	    		Tool.configureParameters(Tool.workflowIndex);
	    		InitCloudParameters();
		        TSPMOEA tspmoea = new TSPMOEA();
		        tspmoea.initPop();
		        tspmoea.evaluatePop(tspmoea.population);
		        tspmoea.generatePoints();   
		        tspmoea.generalArchive(tspmoea.population);
				for(int i=0;i<tspmoea.maxIter;i++) {	
					System.out.println("iteration: "+i);
					
					//reprocudtion2中包含了ELS
					tspmoea.offspringPopulation = tspmoea.reproduction2(tspmoea.population,tspmoea.crossoverProbability,tspmoea.mutationProbability);
					tspmoea.iter = i;
					//ASTS包含了 generalArchive
					tspmoea.population = tspmoea.adaptiveStageTransitionStrategy(tspmoea.population, tspmoea.offspringPopulation);
				}
				
				//写入文件
				for(int i=0;i<tspmoea.population.size();i++) {
					for(int j=0;j<tspmoea.nobj;j++) {
						fw.write(tspmoea.population.get(i).getObjectives(j)+" ");
					}	
					fw.write("\n");
				}
	
	            fw.close();
	
	            //最后一次仿真系统自带
	            //lastSimulation();
	            System.out.println("finish~");
	    	}catch (IOException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
	    	long endTime = System.currentTimeMillis();
	    	System.out.println("程序运行时间：" + (endTime - startTime) + "ms");    //输出程序运行时间
    		}
    	}
        
    }


    //初始化
    public static void InitCloudParameters() {
    	

    	try {
	    	// First step: Initialize the WorkflowSim package. 
	
	        /**
	         * However, the exact number of vms may not necessarily be vmNum If
	         * the data center or the host doesn't have sufficient resources the
	         * exact vmNum would be smaller than that. Take care.
	         */
	        int vmNum = Tool.VmNum;//number of vms;
	        /**
	         * Should change this based on real physical path
	         */
	        String daxPath = "G:\\eclipseWorksapce\\tsp-MOEA(v1)\\WorkflowSim-1.0-master\\config\\dax\\" + String.valueOf(Tool.workflowName)+"_"+Integer.toString(Tool.fileTaskNum)+".xml";
	        
	        
	        File daxFile = new File(daxPath);
	        if (!daxFile.exists()) {
	            Log.printLine("Warning: Please replace daxPath with the physical path in your working environment!");
	            return;
	        }
	
	        /**
	         * Since we are using HEFT planning algorithm, the scheduling
	         * algorithm should be static such that the scheduler would not
	         * override the result of the planner
	         */
	        
	    	
	        Parameters.SchedulingAlgorithm sch_method = Parameters.SchedulingAlgorithm.STATIC;
	        Parameters.PlanningAlgorithm pln_method = Parameters.PlanningAlgorithm.TSPMOEA;
	        ReplicaCatalog.FileSystem file_system = ReplicaCatalog.FileSystem.LOCAL;
	
	        /**
	         * No overheads
	         */
	        OverheadParameters op = new OverheadParameters(0, null, null, null, null, 0);
	
	        /**
	         * No Clustering
	         */
	        ClusteringParameters.ClusteringMethod method = ClusteringParameters.ClusteringMethod.NONE;
	        ClusteringParameters cp = new ClusteringParameters(0, 0, method, null);
	
	        /**
	         * Initialize static parameters
	         */
	        Parameters.init(vmNum, daxPath, null,
	                null, op, cp, sch_method, pln_method,
	                null, 0);
	        ReplicaCatalog.init(file_system);
	
	        // before creating any entities.
	        int num_user = 1;   // number of grid users
	        Calendar calendar = Calendar.getInstance();
	        boolean trace_flag = false;  // mean trace events
	
	        // Initialize the CloudSim library
	        CloudSim.init(num_user, calendar, trace_flag);
	        
	        
	        //得到taskList
	        WorkflowParser wfp = new WorkflowParser(0);
	        wfp.parse();
	        //更新实际的task数目
	        Tool.TaskNum = wfp.getTaskList().size();//少的task都是末尾连续的
	        //对task进行了分层处理
	        Tool.setTaskList(taskLayerProcess(wfp.getTaskList()));
	        
	        WorkflowDatacenter datacenter0 = createDatacenter("Datacenter_0");

            /**
             * Create a WorkflowPlanner with one schedulers.
             */
            WorkflowPlanner wfPlanner = new WorkflowPlanner("planner_0", 1);
            /**
             * Create a WorkflowEngine.
             */
            WorkflowEngine wfEngine = wfPlanner.getWorkflowEngine();
            /**
             * Create a list of VMs.The userId of a vm is basically the id of
             * the scheduler that controls this vm.
             */
            List<CondorVM> vmlist0 = createVM(wfEngine.getSchedulerId(0), Parameters.getVmNum());
            
            Tool.setVmList(vmlist0);
            /**
             * Submits this list of vms to this WorkflowEngine.
             */
            wfEngine.submitVmList(vmlist0, 0);

            /**
             * Binds the data centers with the scheduler.
             */
            wfEngine.bindSchedulerDatacenter(datacenter0.getId(), 0);
            
      

    	}catch (Exception e) {
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
    }
    
    //task分层处理  把根据task1-N的序号换成根据分层来重新安排序号
    public static List<Task> taskLayerProcess(List<Task> taskList1) {
    	List<Task> taskList = new ArrayList<Task>();

    	List<ArrayList<Task>> layerTaskList = new ArrayList<>();
		Task ta;
		//找到最大层
		int maxLayer = -1;
		for(int i=0;i<taskList1.size();i++) {
			ta = taskList1.get(i);
			if(ta.getDepth()>maxLayer) {
				maxLayer = ta.getDepth();
			}
		}
		
		
		//为list分层分配好空间
		for(int i=0;i<maxLayer;i++) {
			layerTaskList.add(new ArrayList<Task>());
		}
    	//1.分层
    	for(int i=0;i<taskList1.size();i++) {
    		ta = taskList1.get(i);
    		ArrayList<Task> taskList2  = layerTaskList.get(ta.getDepth()-1);
    		taskList2.add(ta);
    		layerTaskList.set(ta.getDepth()-1,  taskList2);
    	}
    	
    	//2.根据层重新组合成list
    	for(int i=0;i<layerTaskList.size();i++) {
    		ArrayList<Task> taskList2  = layerTaskList.get(i);
    		for(int j=0;j<taskList2.size();j++) {
    			ta = taskList2.get(j);
    			taskList.add(ta);
    		}
    		
    	}
    	
    	
    	return taskList;
    }
    
    //接着仿真最后一个仿真
    public static void lastSimulation() {
    	try {
	    	// First step: Initialize the WorkflowSim package. 
	
	        /**
	         * However, the exact number of vms may not necessarily be vmNum If
	         * the data center or the host doesn't have sufficient resources the
	         * exact vmNum would be smaller than that. Take care.
	         */
	        int vmNum = Tool.VmNum;//number of vms;
	        /**
	         * Should change this based on real physical path
	         */
	        String daxPath = "G:\\eclipseWorksapce\\tsp-MOEA(v1)\\WorkflowSim-1.0-master\\config\\dax\\" + String.valueOf(Tool.workflowName)+"_"+Integer.toString(Tool.fileTaskNum)+".xml";
	        
	        
	        File daxFile = new File(daxPath);
	        if (!daxFile.exists()) {
	            Log.printLine("Warning: Please replace daxPath with the physical path in your working environment!");
	            return;
	        }
	
	        /**
	         * Since we are using HEFT planning algorithm, the scheduling
	         * algorithm should be static such that the scheduler would not
	         * override the result of the planner
	         */
	        Parameters.SchedulingAlgorithm sch_method = Parameters.SchedulingAlgorithm.STATIC;
	        Parameters.PlanningAlgorithm pln_method = Parameters.PlanningAlgorithm.TSPMOEA;
	        ReplicaCatalog.FileSystem file_system = ReplicaCatalog.FileSystem.LOCAL;
	
	        /**
	         * No overheads
	         */
	        OverheadParameters op = new OverheadParameters(0, null, null, null, null, 0);
	
	        /**
	         * No Clustering
	         */
	        ClusteringParameters.ClusteringMethod method = ClusteringParameters.ClusteringMethod.NONE;
	        ClusteringParameters cp = new ClusteringParameters(0, 0, method, null);
	
	        /**
	         * Initialize static parameters
	         */
	        Parameters.init(vmNum, daxPath, null,
	                null, op, cp, sch_method, pln_method,
	                null, 0);
	        ReplicaCatalog.init(file_system);
	
	        // before creating any entities.
	        int num_user = 1;   // number of grid users
	        Calendar calendar = Calendar.getInstance();
	        boolean trace_flag = false;  // mean trace events
	
	        // Initialize the CloudSim library
	        CloudSim.init(num_user, calendar, trace_flag);
	
	        WorkflowDatacenter datacenter0 = createDatacenter("Datacenter_0");
	
	        /**
	         * Create a WorkflowPlanner with one schedulers.
	         */
	        WorkflowPlanner wfPlanner = new WorkflowPlanner("planner_0", 1);
	        /**
	         * Create a WorkflowEngine.
	         */
	        WorkflowEngine wfEngine = wfPlanner.getWorkflowEngine();
	        /**
	         * Create a list of VMs.The userId of a vm is basically the id of
	         * the scheduler that controls this vm.
	         */
	        List<CondorVM> vmlist0 = createVM(wfEngine.getSchedulerId(0), vmNum);
	    	
	        /**
	         * Submits this list of vms to this WorkflowEngine.
	         */
	        wfEngine.submitVmList(vmlist0, 0);
	
	
	        /**
	         * Binds the data centers with the scheduler.
	         */
	        wfEngine.bindSchedulerDatacenter(datacenter0.getId(), 0);
	
	        CloudSim.startSimulation();
	        List<Job> outputList0 = wfEngine.getJobsReceivedList();
	        
            CloudSim.stopSimulation();
            printJobList(outputList0);
    	}catch (Exception e) {
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
    }
    
    

    ////////////////////////// STATIC METHODS ///////////////////////
    protected static List<CondorVM> createVM(int userId, int vms) {

    	
    	
    	
    	
        //Creates a container to store VMs. This list is passed to the broker later
    	//根据task数目产生vm数目


        LinkedList<CondorVM> list = new LinkedList<CondorVM>();

        //VM Parameters
        long size = 10000; //image size (MB)
        int ram = 512; //vm memory (MB)
        double mips = 0;
        long bw = 0;
        int pesNumber = 1; //number of cpus
        String vmm = "Xen"; //VMM name
        double cost=0.0;//CPU cost
        double costPerMem=0.0;
        double costPerStorage = 0.0;
        double costPerBW = 0.0;
        
        
        //create VMs
        CondorVM[] vm = new CondorVM[vms];

        //Random bwRandom = new Random(System.currentTimeMillis());
        
        for (int i = 0; i < vms; i++) {
        	//加载数据
        	//i 是 vmid
        	mips = Tool.VMInfoList.get(i).mips;
        	ram = Tool.VMInfoList.get(i).memory;
        	bw = (long)Tool.VMInfoList.get(i).bandWidth;
        	size = (long)Tool.VMInfoList.get(i).storage;
        	vmm = String.valueOf(Tool.VMInfoList.get(i).vmName);
        	cost = Tool.VMInfoList.get(i).vmCost;
        	costPerStorage = Tool.VMInfoList.get(i).storageCost;
        	costPerBW = Tool.VMInfoList.get(i).bandwidthCost;
        	
            //double ratio = bwRandom.nextDouble();
            //double mipsValue = Tool.getVmlist().get(i);
            
            vm[i] = new CondorVM(i, userId,mips, pesNumber, ram, bw, size, vmm, cost,costPerMem,costPerStorage,costPerBW,new CloudletSchedulerSpaceShared());
            list.add(vm[i]);
        }
        
        Tool.setVmList(list);
        
        //Collections.copy(descList, srcList)
        return list;
    }

    /**
     * Creates main() to run this example This example has only one datacenter
     * and one storage
     * @throws InterruptedException 
     */
    
//  
    
    public static double calMakespan() {


        try {
            // First step: Initialize the WorkflowSim package.

            /**
             * However, the exact number of vms may not necessarily be vmNum If
             * the data center or the host doesn't have sufficient resources the
             * exact vmNum would be smaller than that. Take care.
             */
            int vmNum = Tool.VmNum;//number of vms;
            /**
             * Should change this based on real physical path
             */
            String daxPath = "G:\\eclipseWorksapce\\tsp-MOEA(v1)\\WorkflowSim-1.0-master\\config\\dax\\" + String.valueOf(Tool.workflowName)+"_"+Integer.toString(Tool.fileTaskNum)+".xml";
            
            File daxFile = new File(daxPath);
            if(!daxFile.exists()){
                Log.printLine("Warning: Please replace daxPath with the physical path in your working environment!");
                return -1;
            }

            /**
             * Since we are using HEFT planning algorithm, the scheduling algorithm should be static
             * such that the scheduler would not override the result of the planner
             */
            Parameters.SchedulingAlgorithm sch_method = Parameters.SchedulingAlgorithm.STATIC;
            Parameters.PlanningAlgorithm pln_method = Parameters.PlanningAlgorithm.TSPMOEA;
            ReplicaCatalog.FileSystem file_system = ReplicaCatalog.FileSystem.LOCAL;

            /**
             * No overheads
             */
            OverheadParameters op = new OverheadParameters(0, null, null, null, null, 0);;

            /**
             * No Clustering
             */
            ClusteringParameters.ClusteringMethod method = ClusteringParameters.ClusteringMethod.NONE;
            ClusteringParameters cp = new ClusteringParameters(0, 0, method, null);

            /**
             * Initialize static parameters
             */
            Parameters.init(vmNum, daxPath, null,
                    null, op, cp, sch_method, pln_method,
                    null, 0);
            ReplicaCatalog.init(file_system);

            // before creating any entities.
            int num_user = 1;   // number of grid users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;  // mean trace events

            // Initialize the CloudSim library
            CloudSim.init(num_user, calendar, trace_flag);

            WorkflowDatacenter datacenter0 = createDatacenter("Datacenter_0");

            /**
             * Create a WorkflowPlanner with one schedulers.
             */
            WorkflowPlanner wfPlanner = new WorkflowPlanner("planner_0", 1);
            /**
             * Create a WorkflowEngine.
             */
            WorkflowEngine wfEngine = wfPlanner.getWorkflowEngine();
            /**
             * Create a list of VMs.The userId of a vm is basically the id of
             * the scheduler that controls this vm.
             */
            List<CondorVM> vmlist0 = createVM(wfEngine.getSchedulerId(0), Parameters.getVmNum());

            /**
             * Submits this list of vms to this WorkflowEngine.
             */
            wfEngine.submitVmList(vmlist0, 0);

            /**
             * Binds the data centers with the scheduler.
             */
            wfEngine.bindSchedulerDatacenter(datacenter0.getId(), 0);



            CloudSim.startSimulation();


            List<Job> outputList0 = wfEngine.getJobsReceivedList();
             
            

            CloudSim.stopSimulation();
//            printJobList(outputList0);

            
            double[] fTime = new double[Tool.TaskNum+1];

            DecimalFormat dft = new DecimalFormat("###.##");
            String tab = "\t";
            Log.printLine("========== OUTPUT ==========");
            Log.printLine("TaskID" + tab + "vmID" + tab + "RunTime" + tab + "StartTime" + tab + "FinishTime" + tab + "Depth"+tab+"STATUS");

            for (int i = 0; i < outputList0.size(); i++) {
                Job oneJob = outputList0.get(i);
                Log.printLine(oneJob.getCloudletId() + tab
                        + oneJob.getVmId() + tab
                        + dft.format(oneJob.getActualCPUTime()) + tab
                        + dft.format(oneJob.getExecStartTime()) + tab+tab
                        + dft.format(oneJob.getFinishTime()) + tab +tab
                        + oneJob.getDepth()+ tab +oneJob.getCloudletStatusString() );

                fTime[oneJob.getCloudletId()] = oneJob.getFinishTime();
            }

            double makespan = outputList0.get((outputList0.size()-1)).getFinishTime()-outputList0.get(0).getFinishTime();
//            Thread.sleep(100000000);
//            System.out.print(makespan);
            return makespan;


        } catch (Exception e) {
            Log.printLine("The simulation has been terminated due to an unexpected error");
            return -1;
        }
    }
    
    
    
    public static void initWorkflow()
    {

        /**
         * However, the exact number of vms may not necessarily be vmNum If
         * the data center or the host doesn't have sufficient resources the
         * exact vmNum would be smaller than that. Take care.
         */
        int vmNum = Tool.VmNum;//number of vms;
        /**
         * Should change this based on real physical path
         */
        String daxPath = "G:\\eclipseWorksapce\\tsp-MOEA(v1)\\WorkflowSim-1.0-master\\config\\dax\\" + String.valueOf(Tool.workflowName)+"_"+Integer.toString(Tool.fileTaskNum)+".xml";
        
        File daxFile = new File(daxPath);
        if(!daxFile.exists()){
            Log.printLine("Warning: Please replace daxPath with the physical path in your working environment!");
            return;
        }

        /**
         * Since we are using HEFT planning algorithm, the scheduling algorithm should be static
         * such that the scheduler would not override the result of the planner
         */
        Parameters.SchedulingAlgorithm sch_method = Parameters.SchedulingAlgorithm.STATIC;
        Parameters.PlanningAlgorithm pln_method = Parameters.PlanningAlgorithm.TSPMOEA;
        ReplicaCatalog.FileSystem file_system = ReplicaCatalog.FileSystem.LOCAL;

        /**
         * No overheads
         */
        OverheadParameters op = new OverheadParameters(0, null, null, null, null, 0);;

        /**
         * No Clustering
         */
        ClusteringParameters.ClusteringMethod method = ClusteringParameters.ClusteringMethod.NONE;
        ClusteringParameters cp = new ClusteringParameters(0, 0, method, null);

        /**
         * Initialize static parameters
         */
        Parameters.init(vmNum, daxPath, null,
                null, op, cp, sch_method, pln_method,
                null, 0);
        ReplicaCatalog.init(file_system);

        // before creating any entities.
        int num_user = 1;   // number of grid users
        Calendar calendar = Calendar.getInstance();
        boolean trace_flag = false;  // mean trace events

        // Initialize the CloudSim library
        CloudSim.init(num_user, calendar, trace_flag);

        WorkflowParser wfp = new WorkflowParser(0);
        wfp.parse();

        Tool.setTaskList(wfp.getTaskList());

        
    }



}
