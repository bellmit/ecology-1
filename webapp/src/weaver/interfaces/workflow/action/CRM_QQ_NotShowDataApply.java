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
 * 期权非展示数据申请
 * @author lsq
 * @date 2020/3/4
 * @version 1.0
 */
public class CRM_QQ_NotShowDataApply extends BaseCronJob{
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
		lg.writeLog("期权非展示数据申请流程触发开始：");
		RecordSetDataSource rds = new RecordSetDataSource("exchangeDB");
		String sql = "select * from SYNC_WEBSITE_OPTION_NONDISPLAY where STATUS=0";
		rds.executeSql(sql);
		lg.writeLog(sql);		
		while(rds.next()){
			//上游id
			String syid = Util.null2String(rds.getString("ID"));
			//公司id
			String companyid = Util.null2String(rds.getString("COMPANY_ID"));
			//数据用途
			String dataUse = Util.null2String(rds.getString("DATA_USE"));
			String qqzs="";
			String fxkz="";
			String sjyj="";
			String other="";
			String otherWrite="";
			if(dataUse !=""){
					Boolean a=dataUse.contains("期权做市");
					Boolean b=dataUse.contains("风险控制");
					Boolean c=dataUse.contains("数据研究");
					Boolean d=dataUse.contains("其他");
					if(a){
						qqzs="1";
					}
					if(b){
						fxkz="1";
					}
					if(c){
						sjyj="1";
					}
					if(d){
						other="1";
						String[] qt = dataUse.split(",");
						for(int i = 0;i<qt.length;i++){
							if(qt[i].contains("其他")){
								otherWrite = qt[i].substring(2);
							}
						}
					}
			}
			//公司名称 
			String companyName ="";
			//联系人
			String businessLinkMan ="";
			//电话
			String businessTel ="";
			//手机
			String phone="";
			//电子邮箱
			String email ="";
			
			RecordSetDataSource rds1 = new RecordSetDataSource("exchangeDB");
			String sqlcom = "select * from SYNC_COMPANY where ID='"+companyid+"'";
			rds1.executeSql(sqlcom);
			lg.writeLog(sqlcom);
			while(rds1.next()){
				companyName =Util.null2String(rds1.getString("COMPANY_NAME"));
				businessLinkMan =Util.null2String(rds1.getString("BUSINESS_LINKMAN"));
				businessTel =Util.null2String(rds1.getString("BUSINESS_TEL"));
				phone =Util.null2String(rds1.getString("FAX"));
				email =Util.null2String(rds1.getString("EMAIL"));
			}
			
			//验证客户是否存在
			RecordSet rs = new RecordSet();
			RecordSet rs1 = new RecordSet();			
			String customerid="";//客户id
			String cusbusinessid="";//客户业务id
			String businessid="7";//期权行情非展示许可业务
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
		    lg.writeLog("syid:"+syid+"--companyName:"+companyName+"--businessLinkMan:"+businessLinkMan+
		    		"--businessTel:"+businessTel+"--phone:"+phone+"--email:"+email+"--qqzs:"+qqzs+
		    		"--fxkz:"+fxkz+"--sjyj:"+sjyj+"--other:"+other+"--otherWrite:"+otherWrite+
		    		"--customerid:"+customerid);
			//创建工作流开始
			try {
				int requestid = createWF(syid,companyName,businessLinkMan,businessTel,phone,email,qqzs,
						fxkz,sjyj,other,otherWrite,customerid);
				if(requestid > 0){
					SimpleDateFormat dateFormat_now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	               String createDate = dateFormat_now.format(new Date());
	              String sqlupdate = "update SYNC_WEBSITE_OPTION_NONDISPLAY set STATUS=1,STEP2_TIME=to_date('"+createDate+"','yyyy-mm-dd hh24:mi:ss')  where ID='"+syid+"'";
					rds.executeSql(sqlupdate);
					lg.writeLog("期权非展示数据申请流程触发工作流成功，流程requestid:"+requestid);
				}
			} catch (Exception e) {
				lg.writeLog("期权非展示数据申请流程触发出错："+e);
			}
		}
	}
	
	/**
	 * 创建期权非展示数据申请流程
	 * @param syid   上游id
	 * @param companyName  公司名称
	 * @param businessLinkMan 联系人
	 * @param businessTel  联系人电话
	 * @param email 电子邮件
	 * @param qqzs  期权做市
	 * @param fxkz  风险控制
	 * @param sjyj  数据研究
	 * @param qt    其他
	 * @return
	 */
	public int createWF(String syid,String companyName,String businessLinkMan,String businessTel,
			String phone,String email,String qqzs,String fxkz,String sjyj,String other,String otherWrite,
			String customerid){
		String newrequestid = "";
		try {
			String workflowid = "530";	
			String lcbt = "期权非展示数据申请";// 流程标题   
			RequestService requestService = new RequestService();
			RequestInfo requestInfo = new RequestInfo();
			
			requestInfo.setWorkflowid(workflowid);//流程类型id
			requestInfo.setCreatorid("112");//创建人		
			requestInfo.setDescription(lcbt);//设置流程标题 
			requestInfo.setRequestlevel("0");//0 正常，1重要，2紧急
			requestInfo.setIsNextFlow("0");//流转到下一节点
			
			MainTableInfo mainTableInfo = new MainTableInfo();
			List<Property> fields = new ArrayList<Property>();
			Property field = null;
			
			if(customerid !=""){
			field = new Property();
			field.setName("companyName");
			field.setValue(customerid);
			fields.add(field);
			}
			
			if(businessLinkMan !=""){
			field = new Property();
			field.setName("linkMan");
			field.setValue(businessLinkMan);
			fields.add(field);
			}
			
			if(businessTel !=""){
			field = new Property();
			field.setName("linkTel");
			field.setValue(businessTel);
			fields.add(field);
			}
			
			if(phone !=""){
			field = new Property();
			field.setName("phone");
			field.setValue(phone);
			fields.add(field);
			}
			
			if(email !=""){
			field = new Property();
			field.setName("linkMail");
			field.setValue(email);
			fields.add(field);
			}
			
			if(other !=""){
			field = new Property();
			field.setName("other");
			field.setValue(other);
			fields.add(field);
			}
			
			if(otherWrite !=""){
			field = new Property();
			field.setName("otherWrite");
			field.setValue(otherWrite);
			fields.add(field);
			}
			
			if(qqzs !=""){
			field = new Property();
			field.setName("qqzs");
			field.setValue(qqzs);
			fields.add(field);
			}
			
			if(fxkz !=""){
			field = new Property();
			field.setName("fxkz");
			field.setValue(fxkz);
			fields.add(field);
			}
			
			if(sjyj !=""){
			field = new Property();
			field.setName("sjyj");
			field.setValue(sjyj);
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
			field.setName("files");
			field.setValue(docids);
			fields.add(field);
			
			Property[] fieldarray = (Property[]) fields.toArray(new Property[fields.size()]);
			mainTableInfo.setProperty(fieldarray);
			requestInfo.setMainTableInfo(mainTableInfo);
			newrequestid = requestService.createRequest(requestInfo);
			
			
		} catch (Exception e) {
			lg.writeLog("期权非展示数据申请流程触发出错："+e);
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
			requestInfo.setCreatorid("124");//创建人		
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
			field.setValue("124");
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
