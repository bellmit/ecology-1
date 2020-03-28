package weaver.interfaces.workflow.action;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import weaver.conn.*;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.schedule.BaseCronJob;
import weaver.soa.workflow.request.MainTableInfo;
import weaver.soa.workflow.request.Property;
import weaver.soa.workflow.request.RequestInfo;
import weaver.soa.workflow.request.RequestService; 

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import oracle.sql.BLOB;
//上证level2申请流程
public class CRM_LEVEL2_SQ_DAO extends BaseCronJob{
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
	
	/**
	 * level2申请流程触发
	 * 
	 */
	public void create(){
		lg.writeLog("level2申请流程触发开始:");
		RecordSetDataSource rds = new RecordSetDataSource("exchangeDB");
		String sql = "select * from SYNC_WEBSITE_LEVEL2 where STATUS=0";
		rds.executeSql(sql);
		while(rds.next()){
			
			String syid = Util.null2String(rds.getString("ID"));	
			String gsid = Util.null2String(rds.getString("COMPANY_ID"));
			//公司基本信息：根据gsid查询出公司的全部信息
			String gsmc = "";
			String zcdz = "";
			String zzjgdm = "";
			String frdb = "";
			String gswz = "";
			String ywlxr = "";//联系人
			String lxrdh = "";//座机
			String gscz = ""; //手机
			String Email = "";//电子邮件
			/*String glfzr = "";
			String fzrdh = "";
            */
			String bgdz = "";
			/*String jsfzr = "";
			String fzrdh1 = "";
			String sjkfwq = "";
			String gsbj = "";
			String mgs = "";*/
			
			RecordSetDataSource rds1 = new RecordSetDataSource("exchangeDB");
			String sqlcom = "select * from SYNC_COMPANY where ID='"+gsid+"'";
			rds1.executeSql(sqlcom);
			while(rds1.next()){
				gsmc =Util.null2String(rds1.getString("COMPANY_NAME"));
				zcdz =Util.null2String(rds1.getString("REGISTERED_ADDRESS"));
				zzjgdm =Util.null2String(rds1.getString("ORGANIZATION_CODE"));
				frdb =Util.null2String(rds1.getString("LEGAL_PERSON"));
				gswz =Util.null2String(rds1.getString("WEBSITE"));
				ywlxr =Util.null2String(rds1.getString("BUSINESS_LINKMAN"));
				lxrdh =Util.null2String(rds1.getString("BUSINESS_TEL"));
				gscz =Util.null2String(rds1.getString("FAX"));
				Email =Util.null2String(rds1.getString("EMAIL"));
				/*glfzr =Util.null2String(rds1.getString("MANAGER_PRINCIPAL"));
				fzrdh =Util.null2String(rds1.getString("MANAGER_TEL"));
*/
				bgdz =Util.null2String(rds1.getString("ADDRESS"));
				/*jsfzr =Util.null2String(rds1.getString("TECH_PRINCIPAL"));
				fzrdh1 =Util.null2String(rds1.getString("TECH_TEL"));
				sjkfwq =Util.null2String(rds1.getString("SYSTEM_ADDRESS"));
				gsbj =Util.null2String(rds1.getString("MAIN_BUSINESS"));
				mgs =Util.null2String(rds1.getString("SHAREHOLDING_RATIO"));*/		
			}
			//申请许可要素
			String sqxkyt = Util.null2String(rds.getString("APPLY_PURPOSE"));
			String dnzd="";
			String ydzd="";
			if(sqxkyt !=""){
					Boolean a=sqxkyt.contains("以电脑");
					Boolean b=sqxkyt.contains("以移动");
					if(a){
						dnzd="1";
					}
					if(b){
						ydzd="1";
					}
				
			}
			//申请许可范围
			/*String sqxkfw = Util.null2String(rds.getString("APPLY_RANGE"));
			if(sqxkfw !=null){
				String aa=sqxkfw.substring(0,2);
				if(aa.equals("中国")){
					sqxkfw="0";
				}else if(aa.equals("全球")){
					sqxkfw="1";
				}
			}*/
			//申请许可年限
			/*String sqxknx = Util.null2String(rds.getString("DEADLINE"));
			if(sqxknx !=null){
				if(sqxknx.equals("1")){
					sqxknx="0";
				}else if(sqxknx.equals("2")){
					sqxknx="1";
				}else if(sqxknx.equals("3")){
					sqxknx="2";
				}
			}*/
			
			//String sqsj = Util.null2String(rds.getString("APPLY_TIME"));
			//行情接受方式
			String mstp="";
			String ypt="";
			String hqjsfs = Util.null2String(rds.getString("RECEIVE_MODE"));
			if(hqjsfs !=null){
				if(hqjsfs.contains("MSTP专线传送")){
					mstp="1";
				}
				if(hqjsfs.contains("云平台")){
					ypt="1";
				}
			}
			//产品或服务描述
			String cphfwmc = Util.null2String(rds.getString("PRODUCT"));
			/*String mbkh = Util.null2String(rds.getString("CUSTOMER"));
			
			String dj = Util.null2String(rds.getString("PRICE"));
			String tcsj = Util.null2String(rds.getString("LAUNCH_TIME"));*/
			
			/*String zzyhjslb = Util.null2String(rds.getString("RECEIVE_DEVICE"));//最终用户接收类别--------------
			String pc="";
			String mobile="";
			String qt2="";
			if(zzyhjslb !=""){
				Boolean a=zzyhjslb.contains("终端/电脑");
				Boolean b=zzyhjslb.contains("手机/PDA");
				Boolean c=zzyhjslb.contains("其他");
				if(a){
					pc="1";
				}
				if(b){
					mobile="1";
				}
				if(c){
					qt2="1";
				}
			}
			
			String csfs = Util.null2String(rds.getString("TRANSMISSION_MODE"));//传输方式------------
			String zx="";
			String wx="";
			String jyw="";
			String hlw="";
			String qt="";
			if(csfs !=""){
				Boolean a=csfs.contains("专线");
				Boolean b=csfs.contains("卫星");
				Boolean c=csfs.contains("局域网");
				Boolean d=csfs.contains("互联网");
				Boolean e=csfs.contains("其他");
				if(a){
					zx="1";
				}
				if(b){
					wx="1";
				}
				if(c){
					jyw="1";
				}
				if(d){
					hlw="1";
				}
				if(e){
					qt="1";
				}
			}*/
			String qtgnhxx = Util.null2String(rds.getString("MEMO"));
			String qtsrk2 = Util.null2String(rds.getString("RECEIVE_DEVICE_OTHER"));
			String qtsrk = Util.null2String(rds.getString("TRANSMISSION_MODE_OTHER"));
			
			//验证客户是否存在
			RecordSet rs = new RecordSet();
			RecordSet rs1 = new RecordSet();			
			String customerid="";//客户id
			String cusbusinessid="";//客户业务id
			String businessid="5";//level2行情许可申请业务
			String sqlkh="select * from uf_crm_customerinfo where name='"+gsmc+"'";
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
					int requestids = createCustomerBusiness(gsmc,businessid);
					if(requestids > 0){						
						lg.writeLog("创建客户业务卡片流程触发工作流成功，流程requestid:"+requestids);
					}
				} catch (Exception e) {
					lg.writeLog("创建客户业务卡片流程触发出错："+e);
				}		    	
		    }
			//创建工作流开始
			try {
				int requestid = createWF(syid,gsid,gsmc,zcdz,zzjgdm,frdb,ywlxr,lxrdh,gscz,Email,
						gswz,bgdz,dnzd,ydzd,cphfwmc,qtgnhxx,qtsrk2,qtsrk,customerid,mstp,ypt);
				if(requestid > 0){
					SimpleDateFormat dateFormat_now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	               String createDate = dateFormat_now.format(new Date());
	              String sqlupdate = "update SYNC_WEBSITE_LEVEL2 set STATUS=1,STEP2_TIME=to_date('"+createDate+"','yyyy-mm-dd hh24:mi:ss')  where ID='"+syid+"'";
					rds.executeSql(sqlupdate);
					lg.writeLog("level2申请流程触发工作流成功，流程requestid:"+requestid);
				}
			} catch (Exception e) {
				lg.writeLog("level2申请流程触发出错："+e);
			}		    
		}
		lg.writeLog("level2申请流程触发结束");
	}
	
	/**
	 * 创建level2申请流程
	 */
	public int createWF(String syid,String gsid,String gsmc,String zcdz,String zzjgdm,String frdb,
			String ywlxr,String lxrdh,String gscz,String Email,String gswz,String bgdz,String dnzd,
			String ydzd,String cphfwmc,String qtgnhxx,String qtsrk2,String qtsrk,
			String customerid,String mstp,String ypt
			){
		String newrequestid = "";
		try{
			String workflowid = "474";	
			String lcbt = "LEVEL-2行情展示许可申请";// 流程标题   
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
			

			// 主表字段开始,1,syid,gsid,gsmc,zcdz,zzjgdm,frdb,
			if(syid !=""){
			field = new Property();
			field.setName("syid");
			field.setValue(syid);
			fields.add(field);
			}
			if(gsid !=""){
			field = new Property();
			field.setName("gsid");
			field.setValue(gsid);
			fields.add(field);
			}
			if(gsmc !=""){
			field = new Property();
			field.setName("gsmc");
			field.setValue(gsmc);
			fields.add(field);
			}
			if(zcdz !=""){
			field = new Property();
			field.setName("zcdz");
			field.setValue(zcdz);
			fields.add(field);
			}
			if(zzjgdm !=""){
			field = new Property();
			field.setName("zzjgdm");
			field.setValue(zzjgdm);
			fields.add(field);
			}
			if(frdb !=""){
			field = new Property();
			field.setName("frdb");
			field.setValue(frdb);
			fields.add(field);
			}
			
			if(ywlxr !=""){
			field = new Property();
			field.setName("ywlxr");
			field.setValue(ywlxr);
			fields.add(field);
			}
			
			if(lxrdh !=""){
			field = new Property();
			field.setName("lxrdh");
			field.setValue(lxrdh);
			fields.add(field);
			}
			
			if(gscz !=""){
			field = new Property();
			field.setName("gscz");
			field.setValue(gscz);
			fields.add(field);
			}
			
			if(Email !=""){
			field = new Property();
			field.setName("Email");
			field.setValue(Email);
			fields.add(field);
			}
            //7,gswz,glfzr,fzrdh,ywlxr,lxrdh,gscz,
			if(gswz !=""){
			field = new Property();
			field.setName("gswz");
			field.setValue(gswz);
			fields.add(field);
			}
			if(bgdz !=""){
			field = new Property();
			field.setName("bgdz");
			field.setValue(bgdz);
			fields.add(field);
			}
			if(dnzd !=""){
			field = new Property();
			field.setName("dnzd");
			field.setValue(dnzd);
			fields.add(field);
			}
			if(ydzd !=""){
			field = new Property();
			field.setName("ydzd");
			field.setValue(ydzd);
			fields.add(field);
			}	
			//25,sqsj,hqjsfs,cphfwmc,cpljdz,mbkh,dj,         
			if(cphfwmc !=""){
			field = new Property();
			field.setName("cphfwmc");
			field.setValue(cphfwmc);
			fields.add(field);
			}
			//31  tcsj,pc,mobile,qt2,zx,,wx,jyw,hlw,qt,qtgnhxx,ywbdynr,ywbdrnr
			if(qtgnhxx !=""){
			field = new Property();
			field.setName("qtgnhxx");
			field.setValue(qtgnhxx);
			fields.add(field);			
			}
			
			if(qtsrk2 !=""){
				field = new Property();
				field.setName("qtsrk2");
				field.setValue(qtsrk2);
				fields.add(field);			
				}
			if(qtsrk !=""){
				field = new Property();
				field.setName("qtsrk");
				field.setValue(qtsrk);
				fields.add(field);			
				}
			if(customerid !=""){
				field = new Property();
				field.setName("customer");
				field.setValue(customerid);
				fields.add(field);
			}
			
			if(mstp !=""){
			field = new Property();
			field.setName("mstp");
			field.setValue(mstp);
			fields.add(field);
			}
			
			if(ypt !=""){
			field = new Property();
			field.setName("ypt");
			field.setValue(ypt);
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
		}catch(Exception e){
			lg.writeLog("level2申请流程触发出错："+e);
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
