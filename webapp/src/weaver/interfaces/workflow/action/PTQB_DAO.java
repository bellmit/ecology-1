package weaver.interfaces.workflow.action;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import weaver.interfaces.workflow.action.FileManager;
import weaver.conn.*;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.schedule.BaseCronJob;
import weaver.soa.workflow.request.MainTableInfo;
import weaver.soa.workflow.request.Property;
import weaver.soa.workflow.request.RequestInfo;
import weaver.soa.workflow.request.RequestService;


public class PTQB_DAO extends BaseCronJob{
	FileManager fm=new FileManager();
	SelectContent sc=new SelectContent();
	BaseBean lg = new BaseBean();
	Calendar todaycal = Calendar.getInstance();
	String syndate = Util.add0(todaycal.get(Calendar.YEAR), 4) + "-" + Util.add0(todaycal.get(Calendar.MONTH) + 1, 2) + "-" + Util.add0(todaycal.get(Calendar.DAY_OF_MONTH), 2);
	// 通过继承BaseCronJob类可以实现定时同步
	public void execute(){ 
		try {
			lg.writeLog("普通签报流程触发开始----"+syndate);
			create();
		} catch (Exception e) {
			lg.writeLog("普通签报流程触发出错："+e);
		}
	}
	/**
	 * 普通签报流程触发
	 * 
	 */
	public void create(){
		RecordSetDataSource rds = new RecordSetDataSource("oldoa");
		//获取老OA流程主要信息
		String OBJCLASS = "PTQB";//流程类型
		
		String sql = "SELECT USER_ID,BT,ID,WH,NGDATE FROM G_INFOS WHERE OBJCLASS = '"+OBJCLASS+"' AND STATUS = 2 ORDER BY NGDATE ASC";
		//String sql = "SELECT USER_ID,BT,ID,WH,NGDATE FROM G_INFOS WHERE OBJCLASS = '"+OBJCLASS+"' AND STATUS = 2 and ID=10094 ORDER BY NGDATE ASC";

		//测试，直接赋值ID为and ID=10094
		rds.executeSql(sql);
		while(rds.next()){
			String USER_ID = Util.null2String(rds.getString("USER_ID"));//老OA流程创建人
			String BT = Util.null2String(rds.getString("BT"));//流程标题
			String WH = Util.null2String(rds.getString("WH"));//文号
			String ID = Util.null2String(rds.getString("ID"));//老OA流程主ID
			String NGDATE = Util.null2String(rds.getString("NGDATE"));//老OA流程创建日期
			RecordSet rs = new RecordSet();
			
			
			
			String CREATORID = "";//新OA创建人
			String cjrname = "";//老OA创建人姓名
			String DEPARTMENTID = "";//新OA创建人部门
			String deptName = "";//老OA创建人部门名称
			rs.executeSql("SELECT H1.ID,H1.LASTNAME,H1.DEPARTMENTID,AA.departmentname FROM HRMRESOURCE H1 "
					+ "JOIN CUS_FIELDDATA C1 ON H1.ID = C1.ID JOIN HrmDepartment AA ON AA.ID=h1.departmentid WHERE C1.FIELD1 = "+USER_ID);
			if(rs.next()){
				CREATORID = Util.null2String(rs.getString("ID"));
				DEPARTMENTID = Util.null2String(rs.getString("DEPARTMENTID"));

				RecordSetDataSource rslqy = new RecordSetDataSource("oldoa");
				String sqlaa = "SELECT NGDW,NGR FROM FW WHERE INFO_ID = "+ID;
				rslqy.executeSql(sqlaa);
				if(rslqy.next()){
					cjrname = Util.null2String(rslqy.getString("NGR"));
					deptName= Util.null2String(rslqy.getString("NGDW"));
				}
				//如果双杨的数据库中申请人姓名和申请部门为空，则赋OA的姓名和部门名称
				if("".equals(cjrname)){
					cjrname=Util.null2String(rs.getString("LASTNAME"));
				}
				if("".equals(deptName)){
					deptName=Util.null2String(rs.getString("departmentname"));
				}
			}
			//如果创建人找不到默认为“彭茹”
			if("".equals(CREATORID)){
				CREATORID = "41";		
				DEPARTMENTID = "6";
				
				RecordSetDataSource rslqy = new RecordSetDataSource("oldoa");
				String sqlbb = "SELECT NGDW,NGR FROM FW WHERE INFO_ID = "+ID;
				rslqy.executeSql(sqlbb);
				if(rslqy.next()){
					cjrname = Util.null2String(rslqy.getString("NGR"));
					deptName= Util.null2String(rslqy.getString("NGDW"));
				}
				//如果双杨的数据库中申请人姓名和申请部门为空，则赋彭茹和综合管理部
				if("".equals(cjrname)){
					cjrname="彭茹";
				}
				if("".equals(deptName)){
					deptName="综合管理部";
				}
			}
			//获取老OA流程签字意见
			RecordSetDataSource rds1 = new RecordSetDataSource("oldoa");
			rds1.executeSql("select b.UNAME,b.ID,a.CONTENT,to_char(b.PDATE,'YYYY-MM-DD hh24:mi') PDATE from G_PNODES b left join G_OPINION a on a.PNID=b.ID and a.PID=b.PID where b.PID="+ID+" and b.PDATE is not null order by b.ID ASC");
					String CONTENTS = "";//老OA流程签字意见
					while(rds1.next()){
						String CONTENT = Util.null2String(rds1.getString("CONTENT"));//老OA流程意见
						String UNAME = Util.null2String(rds1.getString("UNAME"));//老OA流程操作者
						String PDATE = Util.null2String(rds1.getString("PDATE"));//老OA流程签字意见时间						
						CONTENTS +=CONTENT+UNAME+ "<br>"+PDATE+ "<br>";						
					}			
			//创建工作流开始
			try {
				int requestid = createWF(ID,CREATORID,cjrname,BT,WH,NGDATE,CONTENTS,DEPARTMENTID,deptName);
				if(requestid > 0){
					lg.writeLog("普通签报流程触发工作流成功，流程requestid:"+requestid);
				}
			} catch (Exception e) {
				lg.writeLog("普通签报流程触发出错："+e);
			}
		}
		lg.writeLog("普通签报流程触发结束");
	}
	
	/**
	* 创建普通签报流程
	*/
	//deptName,cjrname
	public int createWF(String ID,String CREATORID,String cjrname,String BT,String WH,String NGDATE,String CONTENTS,String DEPARTMENTID,String deptName){
		String newrequestid = "";
		try{
			String workflowid = "61";	
			String lcbt = "普通签报-"+cjrname+"-"+NGDATE;//流程标题   
			RequestService requestService = new RequestService();
			RequestInfo requestInfo = new RequestInfo();
			requestInfo.setWorkflowid(workflowid);//流程类型id
			requestInfo.setCreatorid(CREATORID);//创建人		
			requestInfo.setDescription(lcbt);//设置流程标题 
			requestInfo.setRequestlevel("0");//0 正常，1重要，2紧急
			requestInfo.setIsNextFlow("1");//流转到下一节点
	
			MainTableInfo mainTableInfo = new MainTableInfo();
			List<Property> fields = new ArrayList<Property>();
			Property field = null;
			
			//主表字段开始-从G_INFOS获取
			field = new Property();
			field.setName("qbbt");
			field.setValue(BT);
			fields.add(field);
			
			field = new Property();
			field.setName("bh");
			field.setValue(WH);
			fields.add(field);
			
			field = new Property();
			field.setName("spyj");
			field.setValue(CONTENTS);
			fields.add(field);
			
			field = new Property();
			field.setName("cjrname");
			field.setValue(cjrname);
			fields.add(field);
			
			field = new Property();
			field.setName("deptName");
			field.setValue(deptName);
			fields.add(field);
			
			//主表字段开始-从FW获取
			RecordSetDataSource rd = new RecordSetDataSource("oldoa");
			RecordSet rs = new RecordSet();
			//获取老OA流程主要信息
			String sql = "SELECT NGR,HQBM FROM FW WHERE INFO_ID = "+ID;
			rd.executeSql(sql);
			if(rd.next()){
				String NGR = Util.null2String(rd.getString("NGR"));
				String HQBM = Util.null2String(rd.getString("HQBM"));
//				String USERID = "";
//				String DEPARTMENTID = "";
//				lg.writeLog("NGR为："+NGR);
//				rs.executeSql("SELECT H1.ID,H1.DEPARTMENTID FROM HRMRESOURCE H1 JOIN CUS_FIELDDATA C1 ON H1.ID = C1.ID WHERE H1.LASTNAME LIKE '%"+NGR+"%'");
//				while(rs.next()){
//					USERID = Util.null2String(rs.getString("ID"));
//					DEPARTMENTID = Util.null2String(rs.getString("DEPARTMENTID"));
//					lg.writeLog("泛微OA可以查到该人员，该人员ID为："+USERID+",部门ID为："+DEPARTMENTID);
//				}
//				//如果创建人找不到默认为“彭茹”
//				if("".equals(USERID)){
//					USERID = "41";
//					DEPARTMENTID = "6";
//					
//					
//				}
//				lg.writeLog("创建人ID为："+USERID);
				//相关附件				
				RecordSetDataSource fjaa = new RecordSetDataSource("oldoa");
				
				String sqlaa="select * from G_NR where INFO_ID="+ID;
				fjaa.executeSql(sqlaa);
				String docids = "";
				while(fjaa.next()){
				    String NRBT= Util.null2String(fjaa.getString("NRBT"));//附件名称
				    String MNR= Util.null2String(fjaa.getString("MNR"));//附件表主键ID
				    
				    int aa = sc.getContent(MNR,CREATORID,NRBT);
				    if(aa != 0){
				    	docids = docids+","+aa;
				    }
					
					
				}
				 if(docids == null ||docids.length()<=0){
						
					}else{
						docids = docids.substring(1);
					}
				
				
				field = new Property();
				field.setName("xgfj");
				field.setValue(docids);
				fields.add(field);
				
				
				field = new Property();
				field.setName("sqr");
				field.setValue(CREATORID);
				fields.add(field);
				
				field = new Property();
				field.setName("sqrbm");
				field.setValue(DEPARTMENTID);
				fields.add(field);
				
				
				field = new Property();
				field.setName("sqrq");
				field.setValue(NGDATE);
				fields.add(field);

				field = new Property();
				field.setName("nhqbmy");
				field.setValue(HQBM);
				fields.add(field);
			}
			fields.add(field);
			Property[] fieldarray = (Property[]) fields.toArray(new Property[fields.size()]);
			mainTableInfo.setProperty(fieldarray);
			requestInfo.setMainTableInfo(mainTableInfo);
			newrequestid = requestService.createRequest(requestInfo);
		}catch(Exception e){
			lg.writeLog("普通签报流程触发出错："+e);
		}
		return Util.getIntValue(newrequestid,0);
	}
}
