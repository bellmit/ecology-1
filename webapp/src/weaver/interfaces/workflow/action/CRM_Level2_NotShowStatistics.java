package weaver.interfaces.workflow.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.schedule.BaseCronJob;
import weaver.soa.workflow.request.MainTableInfo;
import weaver.soa.workflow.request.Property;
import weaver.soa.workflow.request.RequestInfo;
import weaver.soa.workflow.request.RequestService;

/**
 * Level-2行情非展示数据转发用户统计报告
 * @author lsq
 * @date 2020/3/3
 * @version 1.0
 */
public class CRM_Level2_NotShowStatistics extends BaseCronJob{
	BaseBean lg = new BaseBean();
	SelectSYFJ sc = new SelectSYFJ();
	Calendar todaycal = Calendar.getInstance();
	String syndate = Util.add0(todaycal.get(Calendar.YEAR), 4) + "-" + Util.add0(todaycal.get(Calendar.MONTH) + 1, 2) + "-" + Util.add0(todaycal.get(Calendar.DAY_OF_MONTH), 2);
	// 通过继承BaseCronJob类可以实现定时同步
	public void execute(){ 
		try {
			create();
		} catch (Exception e) {
			
		}
	}
	
	private void create() {
		lg.writeLog("Level-2行情非展示数据转发用户统计报告流程触发开始：");
		RecordSetDataSource rds = new RecordSetDataSource("exchangeDB");
		String sql = "select * from SYNC_WEBSITE_LEVEL2_FORWARD where STATUS=0";
		rds.executeSql(sql);
		lg.writeLog(sql);		
		while(rds.next()){
			//上游id
			String syid = Util.null2String(rds.getString("ID"));
			//转发用途
			String transmitWay = Util.null2String(rds.getString("TRANSMIT"));
			if(transmitWay !=null){
				if(transmitWay.contains("1")){
					transmitWay="0";
				}else {
					transmitWay="1";
				}
			}
			//供应商名称
			String companyName = Util.null2String(rds.getString("COMPANY_NAME"));
			//统计月份
			String totalYearMonth = Util.null2String(rds.getString("REPORT_TIME"));
			//当月转发用户数
			String monthTransmitNum = Util.null2String(rds.getString("FORWARD_NUM"));
			//当月转发用户使用费总计
			String monthTransmitTotal = Util.null2String(rds.getString("FORWARD_TOTAL_COST"));
			//备注
			String remark = Util.null2String(rds.getString("REMAKE"));
			//组织机构代码证号
			String organizationCode= Util.null2String(rds.getString("ORGANIZATION_CODE"));
			
			//验证客户是否存在
			RecordSet rs = new RecordSet();
			RecordSet rs1 = new RecordSet();			
			String customerid="";//客户id
			String cusbusinessid="";//客户业务id
			String businessid="6";//Level-2行情非展示许可业务
			String sqlkh="select * from uf_crm_customerinfo where name='"+companyName+"'";
			rs.executeSql(sqlkh);
		    if (rs.next()) {
		    	customerid=Util.null2String(rs.getString("id"));
		    	//验证客户业务是否存在
		    	String sqlyw="select * from uf_crm_custbusiness where customer='"+customerid+"' and business='"+businessid+"'";
				rs1.executeSql(sqlyw);
			    if (rs1.next()) {
			    	cusbusinessid=Util.null2String(rs1.getString("id"));     		    	
			    }
		    }
		    
		    //客户业务不存在创建流程
		    if(cusbusinessid.equals("")){
		    	customerid="";//业务不存在时，客户变为空
		    	try {
					int requestids = createCustomerBusiness(companyName,businessid);
					if(requestids > 0){						
						lg.writeLog("创建客户业务卡片流程触发工作流成功，流程requestid:"+requestids);
					}
				} catch (Exception e) {
					lg.writeLog("创建客户业务卡片流程触发出错："+e);
				}		    	
		    }
			//创建工作流开始
			try {
				int requestid = createWF(syid,transmitWay,companyName,totalYearMonth,monthTransmitNum,monthTransmitTotal,
						remark,organizationCode);
				if(requestid > 0){
					SimpleDateFormat dateFormat_now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	               String createDate = dateFormat_now.format(new Date());
	              String sqlupdate = "update SYNC_WEBSITE_LEVEL2_FORWARD set STATUS=1,STEP2_TIME=to_date('"+createDate+"','yyyy-mm-dd hh24:mi:ss')  where ID='"+syid+"'";
					rds.executeSql(sqlupdate);
					lg.writeLog("level2申请流程触发工作流成功，流程requestid:"+requestid);
				}
			} catch (Exception e) {
				lg.writeLog("level2申请流程触发出错："+e);
			}	
		}
		lg.writeLog("Level-2行情非展示数据转发用户统计报告流程触发结束");
	}
	
	/**
	 * 创建工作流方法
	 * @param syid   上游id
	 * @param transmitWay  转发用途
	 * @param companyName  供应商名称
	 * @param totalYearMonth  统计月份
	 * @param monthTransmitNum  当月转发用户数
	 * @param monthTransmitTotal  当月转发用户使用费总计
	 * @param remark  备注
	 * @param ORGANIZATION_CODE  组织机构代码证号
	 * @return int 是否成功创建工作流  0否
	 */
	public int createWF(String syid, String transmitWay, String companyName, String totalYearMonth, String monthTransmitNum,
			String monthTransmitTotal, String remark, String organizationCode){
		String newrequestid = "";
		try {
			String workflowid = "530";	
			String lcbt = "Level-2行情非展示数据转发用户统计报告";// 流程标题   
			RequestService requestService = new RequestService();
			RequestInfo requestInfo = new RequestInfo();
			
			requestInfo.setWorkflowid(workflowid);//流程类型id
			requestInfo.setCreatorid("112");//创建人		
			requestInfo.setDescription(lcbt);//设置流程标题 
			requestInfo.setRequestlevel("0");//0 正常，1重要，2紧急
			requestInfo.setIsNextFlow("1");//流转到下一节点
			
			MainTableInfo mainTableInfo = new MainTableInfo();
			List<Property> fields = new ArrayList<Property>();
			Property field = null;
			
			if(syid !=""){
			field = new Property();
			field.setName("syid");
			field.setValue(syid);
			fields.add(field);
			}
			
			if(transmitWay !=""){
			field = new Property();
			field.setName("transmitWay");
			field.setValue(transmitWay);
			fields.add(field);
			}
			
			if(companyName !=""){
			field = new Property();
			field.setName("companyName");
			field.setValue(companyName);
			fields.add(field);
			}
			
			if(totalYearMonth !=""){
			field = new Property();
			field.setName("totalYearMonth");
			field.setValue(totalYearMonth);
			fields.add(field);
			}
			
			if(monthTransmitNum !=""){
			field = new Property();
			field.setName("monthTransmitNum");
			field.setValue(monthTransmitNum);
			fields.add(field);
			}
			
			if(monthTransmitTotal !=""){
			field = new Property();
			field.setName("monthTransmitTotal");
			field.setValue(monthTransmitTotal);
			fields.add(field);
			}
			
			if(remark !=""){
			field = new Property();
			field.setName("remark");
			field.setValue(remark);
			fields.add(field);
			}
			
			if(organizationCode !=""){
			field = new Property();
			field.setName("organizationCode");
			field.setValue(organizationCode);
			fields.add(field);
			}
			
			//上游附件,根据gsid来查询
			RecordSetDataSource fjss = new RecordSetDataSource("exchangeDB");
			String sqlfj="select * from SYNC_ATTACH where info_id ='"+syid+"'";
			fjss.executeSql(sqlfj);
			String docids = "";
			while(fjss.next()){
				String id= Util.null2String(fjss.getString("id"));//附件id
			    String NRBT= Util.null2String(fjss.getString("ATTACH_NAME"));//附件名称
			    
			    int aa = sc.getContent(id,"112",NRBT);
			    if(aa != 0){
			    	docids = docids+","+aa;
			    }
				
				
			}
			if(docids == null ||docids.length()<=0){
					
			}else{
				docids = docids.substring(1);
			}
			
			
			field = new Property();
			field.setName("fj");
			field.setValue(docids);
			fields.add(field);
			
			Property[] fieldarray = (Property[]) fields.toArray(new Property[fields.size()]);
			mainTableInfo.setProperty(fieldarray);
			requestInfo.setMainTableInfo(mainTableInfo);
			newrequestid = requestService.createRequest(requestInfo);
			
			
		} catch (Exception e) {
			lg.writeLog("Level-2行情非展示数据转发用户统计报告流程触发出错："+e);
		}
        return Util.getIntValue(newrequestid,0);
	}
	
	
	/**
	 * 创建客户卡片流程
	 */
	public int createCustomerBusiness(String name,String business ){
		String newrequestid = "";
		try{
			String workflowid = "430";	//
			String lcbt = "创建客户业务卡片:"+name;// 流程标题   
			RequestService requestService = new RequestService();
			RequestInfo requestInfo = new RequestInfo();
            lg.writeLog("创建客户业务卡片程触发start：");
			requestInfo.setWorkflowid(workflowid);//流程类型id
			requestInfo.setCreatorid("112");//创建人		
			requestInfo.setDescription(lcbt);//设置流程标题 
			requestInfo.setRequestlevel("0");//0 正常，1重要，2紧急
			requestInfo.setIsNextFlow("0");//保存在发起节点
			
			SimpleDateFormat dateFormat_now = new SimpleDateFormat("yyyy-MM-dd");
            String createDate = dateFormat_now.format(new Date());
			
			MainTableInfo mainTableInfo = new MainTableInfo();
			List<Property> fields = new ArrayList<Property>();
			Property field = null;			
					
			field = new Property();
			field.setName("name");
			field.setValue(name);
			fields.add(field);			
			
			field = new Property();
			field.setName("business");
			field.setValue(business);
			fields.add(field);
			
			field = new Property();
			field.setName("createdate");
			field.setValue(createDate);
			fields.add(field);			
			
			field = new Property();
			field.setName("creator");
			field.setValue("112");
			fields.add(field);
			
			field = new Property();
			field.setName("dept");
			field.setValue("23");
			fields.add(field);          
			
			fields.add(field);
			Property[] fieldarray = (Property[]) fields.toArray(new Property[fields.size()]);
			mainTableInfo.setProperty(fieldarray);
			requestInfo.setMainTableInfo(mainTableInfo);
			newrequestid = requestService.createRequest(requestInfo);
            lg.writeLog("创建客户业务卡片流程触发end：");
		}catch(Exception e){
			lg.writeLog("创建客户业务卡片流程触发出错："+e);
		}
		return Util.getIntValue(newrequestid,0);
	}
}
