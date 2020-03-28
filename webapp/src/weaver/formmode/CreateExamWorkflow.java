package weaver.formmode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import weaver.common.DateUtil;
import weaver.conn.RecordSet;
import weaver.general.Util;
import weaver.workflow.webservices.WorkflowBaseInfo;
import weaver.workflow.webservices.WorkflowDetailTableInfo;
import weaver.workflow.webservices.WorkflowMainTableInfo;
import weaver.workflow.webservices.WorkflowRequestInfo;
import weaver.workflow.webservices.WorkflowRequestTableField;
import weaver.workflow.webservices.WorkflowRequestTableRecord;
import weaver.workflow.webservices.WorkflowServiceImpl;

public class CreateExamWorkflow {

	public void createworkflow(String ksid){
		
		//查询考生信息
		RecordSet rs = new RecordSet();
		rs.execute("select workcode,departmentid,jobtitle,lastname, from HrmResource where id="+ksid);
		String workcode = "";
		
		String departmentid = "";
		String jobtitle = "";
		String lastname = "";
		String ksdate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());// 日期格式化
		if(rs.next()){
			workcode = Util.null2String(rs.getString("workcode"));
			departmentid = Util.null2String(rs.getString("departmentid"));
			jobtitle = Util.null2String(rs.getString("jobtitle"));
			lastname = Util.null2String(rs.getString("lastname"));
		}
		//主字段
		WorkflowRequestTableField[] wrti = new WorkflowRequestTableField[5]; //字段信息
		//特此重点说明：WorkflowRequestTableField[3] 3代表此类流程可编辑或必填的字段有3个
		//①、必填的字段必须都要按照如下方式拼接进去 ；②、可编辑字段不需要都编辑进去 可选择性进行拼接 可按照实际情况进行选择；
		wrti[0]=new WorkflowRequestTableField();
		wrti[0].setFieldName("ksxm");// 考生姓名
		wrti[0].setFieldValue(ksid);//字段值 
		wrti[0].setEdit(true);//是否能编辑
		wrti[0].setView(true);//是否可以查看
		wrti[0].setMand(true);//是否必填
									
		wrti[1]=new WorkflowRequestTableField();
		wrti[1].setFieldName("ksgh");//考生工号
		wrti[1].setFieldValue(workcode);//字段值 
		wrti[1].setEdit(true);//是否能编辑
		wrti[1].setView(true);//是否可以查看
		wrti[1].setMand(false);//是否必填
		
		wrti[2]=new WorkflowRequestTableField();
		wrti[2].setFieldName("ksssbm");//考生所属部门
		wrti[2].setFieldValue(departmentid);//字段值 
		wrti[2].setEdit(true);//是否能编辑
		wrti[2].setView(true);//是否可以查看
		wrti[2].setMand(false);//是否必填
		
		wrti[3]=new WorkflowRequestTableField();
		wrti[3].setFieldName("kszw");//考生职务
		wrti[3].setFieldValue(jobtitle);//字段值 
		wrti[3].setEdit(true);//是否能编辑
		wrti[3].setView(true);//是否可以查看
		wrti[3].setMand(false);//是否必填
		
		wrti[4]=new WorkflowRequestTableField();
		wrti[4].setFieldName("bcksfs");//考生分数
		wrti[4].setFieldValue("");//字段值 
		wrti[4].setEdit(false);//是否能编辑
		wrti[4].setView(false);//是否可以查看
		wrti[4].setMand(false);//是否必填

		wrti[5] = new WorkflowRequestTableField(); 
		wrti[5].setFieldName("ksdate");//考试日期
		wrti[5].setFieldValue(ksdate);//字段的值
		wrti[5].setEdit(true);//字段是否可编辑
		wrti[5].setView(true);//字段是否可见
		
		WorkflowRequestTableRecord[] wrtri = new WorkflowRequestTableRecord[1];//主字段只有一行数据
		wrtri[0] = new WorkflowRequestTableRecord();
		wrtri[0].setWorkflowRequestTableFields(wrti);	
		
		WorkflowMainTableInfo wmi = new WorkflowMainTableInfo();
		wmi.setRequestRecords(wrtri);
				
		//随机10道题目 formtable_main_59
		List templist = new ArrayList();
		rs.execute("select * from formtable_main_59");
		while(rs.next()){
			Map map = new HashMap();
			map.put("id", Util.null2String(rs.getString("id")));
			map.put("tm", Util.null2String(rs.getString("tm")));
			map.put("daxx", Util.null2String(rs.getString("daxx")));
			map.put("ksdate", Util.null2String(rs.getString("ksdate")));
			map.put("zqda", Util.null2String(rs.getString("zqda")));
			templist.add(map);
		}
    	List examlist  = getRandomNum(templist,10);
    	
    	//明细字段
    	WorkflowDetailTableInfo wdti[] = new WorkflowDetailTableInfo[1];//两个明细表0明细表1,1明细表2
    	wrtri = new WorkflowRequestTableRecord[examlist.size()];//数据 行数
    	
		for(int i=0;i<examlist.size();i++){
			Map map = (Map)examlist.get(i);
			wrti = new WorkflowRequestTableField[4]; //每行字段个数
			wrti[0] = new WorkflowRequestTableField(); 
			wrti[0].setFieldName("kstm");//考试题目
			wrti[0].setFieldValue(Util.null2String(map.get("tm")));//字段的值
			wrti[0].setEdit(true);//字段是否可编辑
			wrti[0].setView(true);//字段是否可见
			
			wrti[1] = new WorkflowRequestTableField(); 
			wrti[1].setFieldName("tmxx");//题目选项
			wrti[1].setFieldValue(Util.null2String(map.get("daxx")));//字段的值
			wrti[1].setEdit(true);//字段是否可编辑
			wrti[1].setView(true);//字段是否可见
			
			wrti[2] = new WorkflowRequestTableField(); 
			wrti[2].setFieldName("xzda");//选择答案
			wrti[2].setFieldValue("");//字段的值
			wrti[2].setEdit(true);//字段是否可编辑
			wrti[2].setView(true);//字段是否可见
			
			wrti[3] = new WorkflowRequestTableField(); 
			wrti[3].setFieldName("fz");//本题分值
			wrti[3].setFieldValue(Util.null2String(map.get("id")));//字段的值
			wrti[3].setEdit(true);//字段是否可编辑
			wrti[3].setView(true);//字段是否可见

			wrti[4] = new WorkflowRequestTableField(); 
			wrti[4].setFieldName("zqda");//正确答案
			wrti[4].setFieldValue(Util.null2String(map.get("zqda")));//字段的值
			wrti[4].setEdit(true);//字段是否可编辑
			wrti[4].setView(true);//字段是否可见
			
			wrtri[i] = new WorkflowRequestTableRecord();
			wrtri[i].setWorkflowRequestTableFields(wrti);
		}
		
		wdti[0] = new WorkflowDetailTableInfo();
		wdti[0].setWorkflowRequestTableRecords(wrtri);//加入明细表1的数据
		
		WorkflowBaseInfo wbi = new WorkflowBaseInfo();
		wbi.setWorkflowId("81");//流程ID
		
		WorkflowRequestInfo wri = new WorkflowRequestInfo();//流程基本信息
		
		wri.setRequestName("考试-"+lastname+"-"+DateUtil.getCurrentDate());
		wri.setCreatorId(String.valueOf(1));//创建人id
		wri.setRequestLevel("0");//0 正常，1重要，2紧急
		wri.setWorkflowMainTableInfo(wmi);//添加主字段数据
		wri.setWorkflowDetailTableInfos(wdti);//明细字段
		wri.setWorkflowBaseInfo(wbi);
		
		WorkflowServiceImpl wsi = new WorkflowServiceImpl();
		int requestid = Integer.parseInt(wsi.doCreateWorkflowRequest(wri,1));
		
	}
	
	/**
     * 返回随机数
     * @param list 备选号码
     * @param selected 备选数量
     * @return
     */
    public List getRandomNum(List list, int selected) {
        List reList = new ArrayList();
        Random random = new Random();
        // 先抽取，备选数量的个数
        if (list.size() >= selected) {
            for (int i = 0; i < selected; i++) {
                // 随机数的范围为0-list.size()-1;
                int target = random.nextInt(list.size());
                reList.add(list.get(target));
                list.remove(target);
            }
        } else {
            selected = list.size();
            for (int i = 0; i < selected; i++) {
                // 随机数的范围为0-list.size()-1;
                int target = random.nextInt(list.size());
                reList.add(list.get(target));
                list.remove(target);
            }
        }
        return reList;
    }
}
