/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TSPMOEA;


import org.uma.jmetal.algorithm.multiobjective.tspmoea.MyUtils;
import org.workflowsim.CondorVM;
import org.workflowsim.FileItem;
import org.workflowsim.Task;
import org.workflowsim.utils.Parameters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


//时间的计算
class TaskTime {

    public double startTime;
    public double stopTime;
    public double totalExcuteTime;
    
}

//辅助作用
class VM {

    public double VMBusyTime;
    public double transmission;
    public double startTime;
    public double endTime;
    public List<TaskTime> taskArray ;
}

public class MyFitnessFunction  {
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------

    //单独一个向量保存task的序列0-N
    List<?> availableVMs;
    List<Task> avaliableTasks;
    List<HostInfo> avaliableHosts;

    List<TaskTime> taskTimeList = new ArrayList<>();
    List<VM> vmTimeTranList = new ArrayList<>();
    
    //三个基础数
    double deadline;
    double budget;
    double energy; //rand algorithm energy

    public List<?> getVmList() {
        return availableVMs;
    }

    public List<Task> getTaskList() {
        return avaliableTasks;
    }

    public MyFitnessFunction() {
        availableVMs = Tool.vmList;//有序
        avaliableTasks = Tool.tasktList;//无序
        avaliableHosts = Tool.HostInfoList;//有序
        deadline = Tool.jobDeadline;
        budget = Tool.jobBudget;
        energy = Tool.jobEnergy;
    }

    //检查list是否还包含其他元素
    public boolean checkDuplicate(ArrayList<Integer> list, int value) {

        return list.contains(value);
    }

    
    
    //task分层处理  把根据task1-N的序号换成根据分层来重新安排序号
    public List<Task> taskLayerProcess(List<Task> taskList1,int taskOrder[]) {
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
    	
    	//2.遍历分层，然后排序
    	for(int i=0;i<layerTaskList.size();i++) {
    		ArrayList<Task> taskList2 = layerTaskList.get(i);
    		ArrayList<Task> taskList3 = new ArrayList<>();
    		int taskIndex[] = new int[taskList2.size()];
    		double taskOrder1[] = new double[taskList2.size()];
    		for(int j=0;j<taskList2.size();j++) {
    			taskIndex[j] = taskList2.get(j).getCloudletId();
    			taskOrder1[j] = (double)taskOrder[taskIndex[j]-1];
    		}
    		//对taskorder1排序
    		int sortIndex[] = MyUtils.sortIndex(taskOrder1);
    		//新的task序号 并添加到新的list中
    		for(int k=0;k<taskList2.size();k++) {
    			taskList3.add(taskList2.get(sortIndex[k]));
    		}
    		layerTaskList.set(i, taskList3);//重新放回去
    	}
    	
    	
    	
    	//3.根据层重新组合成list
    	for(int i=0;i<layerTaskList.size();i++) {
    		ArrayList<Task> taskList2  = layerTaskList.get(i);
    		for(int j=0;j<taskList2.size();j++) {
    			ta = taskList2.get(j);
    			taskList.add(ta);
    		}
    		
    	}
    	
    	
    	return taskList;
    }
    
    
    //通过DAG图仿真
    public void scheduleSimulation(int assign1[],int taskOrder[]) {

    	//根据task的顺序，把assign重新变换顺序
    	int assign[] =  assign1.clone();
    	//1.根据我们的编码方式进行解码。
    	//默认的是同一层，task序号小的在前
    	avaliableTasks =  taskLayerProcess(avaliableTasks,taskOrder);

        //为每个VM弄一个专门的时间管理 vmTimeTranList transmission  初始化。
        for (int i=0;i<getVmList().size();i++) {
            VM v = new VM();
            v.VMBusyTime = 0.0;
            v.transmission = 0.0;
            v.startTime = 0.0;
            v.endTime = 0.0;
            v.taskArray = new ArrayList<TaskTime>();
            vmTimeTranList.add(v);
           
        }
        //对taskTimeList初始化。根据task的顺序存储
        for (int i=0;i<getTaskList().size();i++) {
        	TaskTime t = new TaskTime();
        	t.startTime = 0;
        	t.stopTime = 0;
        	t.totalExcuteTime = 0;
        	taskTimeList.add(t); 
        }

        
        //DAG taskList遍历
        for (int i=0;i<getTaskList().size();i++) {

        	
        	Task task = getTaskList().get(i);
            TaskTime t = new TaskTime();
            int taskId = task.getCloudletId()-1;
            int vmId = assign[taskId];
            

           
            //CondorVM  包含更多的属性，用于调度的计算
            CondorVM vm = (CondorVM) getVmList().get(vmId);
            
            if (task.getParentList().isEmpty()) // no parrent，没有父代节点
            {

            	
            	t.startTime = 0.0;
                t.totalExcuteTime = task.getCloudletLength() / vm.getMips();
                t.stopTime = t.startTime + t.totalExcuteTime;
                

                //更新taskTime,和 vmTimeTranList
                taskTimeList.set(taskId, t);
//                taskTimeList.add(t);
                VM tmVM = new VM();
                tmVM.VMBusyTime = t.stopTime;
                vmTimeTranList.set(vmId, tmVM);//vm根据序号依次存放

            } else {

                //find max parrent time
            	
                int parentVmId=-1;
                int parentTaskId = -1;
                double maxTime = -1;
                for (Task parrentTask : task.getParentList()) {
                	parentTaskId = parrentTask.getCloudletId() - 1;
                    if (taskTimeList.get(parentTaskId).stopTime > maxTime) {
                        maxTime = taskTimeList.get(parentTaskId).stopTime;
                    }

                }

 
               // max(availTime,max(FTti))
                double avalTime = vmTimeTranList.get(vmId).VMBusyTime;
                if (avalTime > maxTime) {
                    maxTime = avalTime;
                }

                //更新startTime
                TaskTime t2 = new TaskTime();
                t2.startTime = maxTime;
                t2.totalExcuteTime = task.getCloudletLength() / vm.getMips();
                t2.stopTime = t2.startTime + t2.totalExcuteTime;

                
                double sumTransferTime = 0;
                double tempTime=0.0;
                VM vmTimeTran  = vmTimeTranList.get(vmId);
                VM vmParTimeTran = new VM(); 
                CondorVM parentVm ;
                for (Task parrentTask : task.getParentList()) {
                	parentTaskId = parrentTask.getCloudletId() - 1;//序号减了1
                	parentVmId = assign[parentTaskId];
                	
                	vmParTimeTran = vmTimeTranList.get(parentVmId);
                    if (vmId != parentVmId) {
                    	parentVm = (CondorVM) getVmList().get(parentVmId);
                    	double tempFileSize = findOutputInput(parrentTask, task);
//                    	double tempFileSize = getOutputSize(parrentTask);
                        tempTime = tempFileSize / (Math.min(vm.getBw(), parentVm.getBw())*1000.0*1000.0*1000.0);
                        sumTransferTime += tempTime;

                        
                        //计算传输量 
                        vmParTimeTran.transmission = vmParTimeTran.transmission + tempFileSize;
                        vmTimeTranList.set(parentVmId, vmParTimeTran);//保存数据
                        vmTimeTran.transmission = vmTimeTran.transmission + tempFileSize;
                        //vmTimeTranList.set(vmId, vmTimeTran);//保存数据//留到后面保存
                    }
                    
                }
 

                t2.stopTime += sumTransferTime;//stop = start + execu + trans
                taskTimeList.set(taskId, t2);
//                taskTimeList.add(t2);

                
                vmTimeTran.VMBusyTime = t2.stopTime;
                vmTimeTranList.set(vmId, vmTimeTran);
                
            }

            
        }
        
        
        //构建临时存储 保存vm存task的队列
        List<ArrayList<TaskTime>> vmListTemp = new ArrayList<>();		
		for(int i=0;i<getVmList().size();i++) {
			vmListTemp.add(new ArrayList<TaskTime>());
		}

		ArrayList <TaskTime>taskTimeTempList = new ArrayList<TaskTime>();
        //把相应的task信息保存到对应的vm上，并保存到vmTimeTranList
//        VM vm = new VM();
        Task ta1 ;
        int taskId1 ;
        int vmId1;
        TaskTime taskTime1 ;
        for(int i=0;i<getTaskList().size();i++) {
        	ta1 = (Task)getTaskList().get(i);
        	taskId1 = ta1.getCloudletId()-1;
        	taskTime1 = taskTimeList.get(taskId1);
        	vmId1 = assign[taskId1];
        	taskTimeTempList = vmListTemp.get(vmId1);//得到该vm下的taskarray
        	taskTimeTempList.add(taskTime1);
        	vmListTemp.set(vmId1, taskTimeTempList);
        }
        
        //把taskArray关联到对应的vm中
        for(int i=0;i<getVmList().size();i++) {
        	
        	vmTimeTranList.get(i).taskArray = vmListTemp.get(i);
        	
        }

    }

      //找到父节点的输出刚好是子节点的输入，那么就对了
    public double findOutputInput(Task parent, Task child) {
        List<FileItem> parentFiles = parent.getFileList();
        List<FileItem> childFiles = child.getFileList();

        double acc = 0.0;

        for (FileItem parentFile : parentFiles) {
            if (parentFile.getType() != Parameters.FileType.OUTPUT) {
                continue;
            }

            for (FileItem childFile : childFiles) {
                if (childFile.getType() == Parameters.FileType.INPUT
                        && childFile.getName().equals(parentFile.getName())) {
                    acc += childFile.getSize();
                    break;
                }
            }
        }

//        //file Size is in Bytes, acc in MB
//        acc = acc / Consts.MILLION; //MILLION:1000000
//        // acc in MB, averageBandwidth in Mb/s
        return acc;
    }
    
    

    //得到该task的outputsize
    public double getOutputSize(Task t) {
        double outputSize = 0;
        for (Iterator it = t.getFileList().iterator(); it.hasNext();) {
            FileItem f = (FileItem) it.next();

            if (f.getType() == Parameters.FileType.OUTPUT) {
                outputSize += f.getSize();
            }
        }
        return outputSize ;
    }

    public double getInputSize(Task t) {
        double inputSize = 0;
        for (Iterator it = t.getFileList().iterator(); it.hasNext();) {
            FileItem f = (FileItem) it.next();
            if (f.getType() == Parameters.FileType.INPUT) {
                inputSize += f.getSize();
            }
        }
        return inputSize ;
    }
    
    
    
    //计算makespan   两种方式的计算都是一样的
//    public  double calMakespan() {
//    	double makespan=0.0;
//    	VM vm;
//    	TaskTime taskTime;
//    	for(int i=0;i<vmTimeTranList.size();i++) {
//    		vm = vmTimeTranList.get(i);
//    		for(int j=0;j<vm.taskArray.size();j++) {
//    			taskTime = vm.taskArray.get(j);
//    			if(taskTime.stopTime > makespan) {
//    				makespan = taskTime.stopTime;
//    			}
//    		}
//    		
//    	}
//    	return makespan;
//    }
    
    public  double calMakespan() {
    	double makespan=0.0;
    	
    	TaskTime taskTime;
    	
    	for(int i=0;i<taskTimeList.size();i++) {
    		taskTime = taskTimeList.get(i);
    		//System.out.println(i+1 + " " + (Tool.allot[i]+1) + " " + taskTime.startTime + "  "+ "tasktime");
    		if (taskTime.stopTime > makespan) {
    			makespan = taskTime.stopTime;
    		}
    		
    	}
    	
    	return makespan;
    }
    
    
    //计算cost
    public double calCost() {
    	double cpuCost = 0.0;
        double transmissionCost = 0;
        double allCost = 0;
        VM vm;
        TaskTime taskTime;
        CondorVM condorVM;
        
        for(int i=0;i<vmTimeTranList.size();i++) {
        	vm = vmTimeTranList.get(i);
        	condorVM = (CondorVM) getVmList().get(i);
        	if (vm.taskArray.size()!= 0) {//判断是否启动了该机器
        		for(int j=0;j<vm.taskArray.size();j++) {
        			taskTime = vm.taskArray.get(j);
        			
        			cpuCost = cpuCost + condorVM.getCost()*Math.ceil(taskTime.totalExcuteTime/3600);
        		}
        		transmissionCost = transmissionCost + (vm.transmission/(1000.0*1000*1000))*condorVM.getCostPerBW();
        				
        			
        	}
        	
        }
        allCost = cpuCost + transmissionCost;
        return allCost;
    }
    
    
    //计算资源利用率
    public double[] calHostResourceUtilization(double hostActiveTime[]) {
    	
    	//统计每个Host的开启时间
    	//统计每个Host的关闭时间
    	//统计每个VM的总执行时间
    	List<VMInfo> VMInfoList = Tool.VMInfoList;
    	VMInfo vmInfo;
    	double EPS = 1.0e-14;
    	double maxValue = 1000000000;
    	double minValue = -1;
    	double hostStartTime[] = new double[avaliableHosts.size()];
    	double hostEndTime[] = new double[avaliableHosts.size()];
//    	double hostActiveTime[] = new double[avaliableHosts.size()];
    	double vmToalActiveOnHost[] = new double[avaliableHosts.size()];
    	double hostHasVMs[] = new double[avaliableHosts.size()];
    			
    	double vmExecuTime[] = new double[availableVMs.size()]; 
    	double vmStartTime[] = new double[availableVMs.size()]; 
    	double vmEndTime[] = new double[availableVMs.size()]; 
    	
    	double hostResourceUtilization[] = new double[avaliableHosts.size()];
    	
    	VM vm;
    	TaskTime taskTime;
    	//初始化hostStartTime endTime
    	for(int i=0;i<avaliableHosts.size();i++) {
    		hostStartTime[i] = maxValue;
    		hostEndTime[i] = minValue;
    		hostActiveTime[i] = 0.0;
    		hostResourceUtilization[i] = 0.0;
    		vmToalActiveOnHost[i] = 0.0;
    		hostHasVMs[i] = 0.0;
    	}
    	for(int i=0;i<getVmList().size();i++) {
    		vmExecuTime[i] = 0.0;
    		vmStartTime[i] = maxValue;
    		vmEndTime[i] = minValue;
    	}
    	
    	
    	//更新VM数据
    	for(int i=0;i<getVmList().size();i++) {
    		vm = vmTimeTranList.get(i);

    		for(int j=0;j<vm.taskArray.size();j++) {
    			taskTime = vm.taskArray.get(j);
    			
    			vmStartTime[i] = Math.min(vmStartTime[i], taskTime.startTime);
    			vmEndTime[i] = Math.max(vmEndTime[i] , taskTime.stopTime);			
    			vmExecuTime[i] = vmExecuTime[i]+taskTime.totalExcuteTime;

    		}
    		//表示vm未开启
    		if (vmStartTime[i] == maxValue) { //或者根据vm的busytime判断
        		vmStartTime[i] = 0;
        		vmEndTime[i] = 0;
        		vmExecuTime[i] = 0;
    		}
    		
    		
    		
    	}
    	
    	
    	//更新Host数据 ,最原始Info里面的序号全是从1开始的 ,double 里面的第一个数存开始时间，结束时间，和执行时间
    	
    	for(int i=0;i<getVmList().size();i++) {
    		vmInfo = VMInfoList.get(i);
    		//更新每个host的start time
    		int hostId = vmInfo.hostId-1;
    		//机子开启了才需要比较
    		if (vmExecuTime[i]!=0) {
        		hostStartTime[hostId] = Math.min(hostStartTime[hostId],vmStartTime[i]);
        		hostEndTime[hostId] = Math.max(hostEndTime[hostId],vmEndTime[i]);
    		}

    		hostHasVMs[hostId] = hostHasVMs[hostId] + 1;
    		vmToalActiveOnHost[hostId] = vmToalActiveOnHost[hostId] + vmExecuTime[i];
    		
    	}
    	
    	//更新主机的活跃时间
    	for(int i=0;i<hostActiveTime.length;i++) {
    		//判断主机是否开启
    		if (vmToalActiveOnHost[i] == 0) {
    			hostEndTime[i]= 0; 
    			hostStartTime[i] = 0;
    		}
    		hostActiveTime[i] = hostEndTime[i] - hostStartTime[i];	
    	}
    	
    	
    	for(int i=0;i<avaliableHosts.size();i++) {
    		hostResourceUtilization[i] = (vmToalActiveOnHost[i])/(hostActiveTime[i]*hostHasVMs[i]+EPS);

    	}
    	
    	return hostResourceUtilization;
    	
    }
    
    
    
    //计算w1和w2.
    /**1.资源利用率需要乘以100，2.w1和w2根据主机的power变化，根据我们参考的那篇文章 **/
    //Paper:Power-aware and performance-guaranteed virtual machine placement in the cloud
    public double[] calW1andW2(double power, double paras[]) {
    	paras[0] = (power * (13.0 / (13.0 + 29.0))) / 100.0;
    	paras[1] = (power * (29.0 / (13.0 + 29.0))) / 10000.0;
    	return paras;
    }
    
    //计算能耗
    public double calEnergyConsumption() {
    	double hostActiveTime[] = new double[avaliableHosts.size()];
    	double hostPowerStatic[] = new double[avaliableHosts.size()];
    	double hostEnergyConsumption[] = new double[avaliableHosts.size()];
    	double paras[] = new double[2];//分别放w1和w2
    	double w1 = -1;//两个拟合系数 w1 = 1.30447; w2 = 0.02867;
        double w2 = -1;
        double powerDynamic = 0.0;
        double powerDynamicAndStatic = 0.0;
        double computationEnergy = 0.0;
        double transmissionEnergy = 0.0;
        double a = 3* 8.6320e-07; //byte/s 多乘以了个3
        VM vm;
        double hostResourceUtilization[] = calHostResourceUtilization(hostActiveTime);
        for(int i=0;i<avaliableHosts.size();i++) {
	        hostPowerStatic[i] = avaliableHosts.get(i).powerMax*0.5;
	        //获取w1 和 w2 
	        calW1andW2(avaliableHosts.get(i).powerMax * 0.5, paras);
	        w1 = paras[0];
	        w2 = paras[1];
	        powerDynamic = w1*hostResourceUtilization[i]*100 + w2*(hostResourceUtilization[i]*hostResourceUtilization[i])*10000;//加了100,10000，去了hostPowerStatic[i]
	        powerDynamicAndStatic = powerDynamic+hostPowerStatic[i];//临时变量，且变化
	        hostEnergyConsumption[i] = powerDynamicAndStatic*hostActiveTime[i];
        }
        
        for(int i=0;i<hostEnergyConsumption.length;i++) {
        	computationEnergy = computationEnergy + hostEnergyConsumption[i];
        	
        }
        for(int i=0;i<getVmList().size();i++) {
        	vm = vmTimeTranList.get(i);
        	transmissionEnergy = transmissionEnergy + vm.transmission*a;
        }
        return computationEnergy + transmissionEnergy;
        
        
    }
    
    
    //求满意度  USD
    public double userSatisfactionDegree() {

    	double makespan = calMakespan();
    	double userSati = 0.0;
        userSati = (-1/deadline)*makespan + 1;
        
//	    if (userSati < 0) {
//	    	userSati = 0;
//	    }
	    return userSati;
    }

    // 求利润率
    public double providerProfitMargin() {
    	double actualCost = calCost();
    	double payment = budget;
    	double profitability = (payment-actualCost)/actualCost;
    	return profitability;
    }
    
    //求能耗改进率
    public double energyConsumptionImprovement( ) {
    	double randAlgorithmEnergy = energy;
    	double currentEnergy = calEnergyConsumption();
    	double energyImprove = (randAlgorithmEnergy-currentEnergy)/randAlgorithmEnergy;
    	return energyImprove;
    }
}
