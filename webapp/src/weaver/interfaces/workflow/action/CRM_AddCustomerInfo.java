package weaver.interfaces.workflow.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import weaver.conn.RecordSet;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.schedule.BaseCronJob;
import weaver.soa.workflow.request.MainTableInfo;
import weaver.soa.workflow.request.Property;
import weaver.soa.workflow.request.RequestInfo;
import weaver.soa.workflow.request.RequestService;

/**
 * 定时检索客户信息表,新增客户业务信息
 * 
 * @author lsq
 * @date 2019/7/11
 * @version 1.0
 */
public class CRM_AddCustomerInfo extends BaseCronJob {
	BaseBean lg = new BaseBean();
	SelectSYFJ sc = new SelectSYFJ();
	Calendar todaycal = Calendar.getInstance();
	String syndate = Util.add0(todaycal.get(Calendar.YEAR), 4) + "-"
			+ Util.add0(todaycal.get(Calendar.MONTH) + 1, 2) + "-"
			+ Util.add0(todaycal.get(Calendar.DAY_OF_MONTH), 2);

	public void execute() {
		try {

			create();
		} catch (Exception e) {
			lg.writeLog("定时任务检索客户信息表,新增客户业务信息异常:" + e);
		}
	}

	public void create() {
		lg.writeLog("开始定时任务检索客户信息表!!!");
		RecordSet rs = new RecordSet();
		RecordSet rs1 = new RecordSet();
		RecordSet rs2 = new RecordSet();
		RecordSet rs3 = new RecordSet();
		RecordSet rs4 = new RecordSet();
		RecordSet rs5 = new RecordSet();
		RecordSet rs6 = new RecordSet();
		RecordSet rs7 = new RecordSet();
		RecordSet rs8 = new RecordSet();
		RecordSet rs9 = new RecordSet();
		RecordSet rs10 = new RecordSet();
		String sql = "select id,iflist,listaddress,ifbond,ifmarket"
				+ ",(select count(1) from uf_crm_custbusiness where customer=uf_crm_customerinfo.id and business=16) as list1"
				+ ",(select count(1) from uf_crm_custbusiness where customer=uf_crm_customerinfo.id and business=17) as list2"
				+ ",(select count(1) from uf_crm_custbusiness where customer=uf_crm_customerinfo.id and business=18) as list3"
				+ ",(select count(1) from uf_crm_custbusiness where customer=uf_crm_customerinfo.id and business=13) as bond1"
				+ ",(select count(1) from uf_crm_custbusiness where customer=uf_crm_customerinfo.id and business=14) as bond2"
				+ ",(select count(1) from uf_crm_custbusiness where customer=uf_crm_customerinfo.id and business=15) as bond3"
				+ ",(select count(1) from uf_crm_custbusiness where customer=uf_crm_customerinfo.id and business=22) as marketbus"
				+ " from uf_crm_customerinfo where (iflist='0' and listaddress='0') or ifbond='0' or ifmarket='0'";
		rs.executeSql(sql);
		while (rs.next()) {
			String id = Util.null2String(rs.getString("id")); // 客户id
			String iflist = Util.null2String(rs.getString("iflist")); // 是否上市
			String listaddress = Util.null2String(rs.getString("listaddress")); // 上市地点是否为上海证券交易所
			String ifbond = Util.null2String(rs.getString("ifbond")); // 是否为证券公司
			String ifmarket = Util.null2String(rs.getString("ifmarket")); // 是否为科创板

			String list1 = Util.null2String(rs.getString("list1")); // 是否为
																	// 网投统计服务
			String list2 = Util.null2String(rs.getString("list2")); // 是否为
																	// e服务专业版
			String list3 = Util.null2String(rs.getString("list3")); // 是否为
																	// 配股缴款统计服务

			String bond1 = Util.null2String(rs.getString("bond1")); // 是否为 云平台
			String bond2 = Util.null2String(rs.getString("bond2")); // 是否为
																	// 会员信息服务平台
			String bond3 = Util.null2String(rs.getString("bond3")); // 是否为
																	// 融资融券投票代征集
 
			String marketbus = Util.null2String(rs.getString("marketbus")); // 是否为
																			// e服务科创专板
			SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
			String createdate=dateFormat.format(new Date());
			
			int modeId = 127;
			if (iflist.equals("0") && listaddress.equals("0")
					&& (ifmarket.equals("1") || ifmarket.length()==0)) { // 是上市公司
				if (list1.equals("0")) {
					lg.writeLog("网投统计服务---开始");
					String sql1 = "SELECT businessmanager FROM  Matrixtable_7 where business='16'";
					rs1.executeSql(sql1);
					String businessmanager = "";
					if (rs1.next()) {
						businessmanager = Util.null2String(rs1
								.getString("businessmanager"));
					}
					String[] busarr = businessmanager.split(",");
					UUID uuid = UUID.randomUUID();
					String wyid = uuid.toString();// 生成唯一的标识码
					String sql2 = "insert into uf_crm_custbusiness(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
							+ "customer,business,businessmanager,uuid) values ("
							+ modeId
							+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),'"
							+ id
							+ "','"
							+ "16"
							+ "','"
							+ busarr[0]
							+ "','"
							+ wyid + "')";
					rs2.executeSql(sql2);
					String sql5 = "select id from uf_crm_custbusiness where uuid='"
							+ wyid + "'";
					rs5.executeSql(sql5);
					if (rs5.next()) {
						String ids = Util.null2String(rs5.getString("id"));
						int logid = Integer.parseInt(ids);
						ModeRightInfo ModeRightInfo = new ModeRightInfo();
						ModeRightInfo.editModeDataShare(5, modeId, logid);
					}
					
					String departmentid="";
					String sql10=" select departmentid from hrmresource a where id='"+busarr[0]+"'";
					rs10.executeSql(sql10);
					if(rs10.next()){
						departmentid=Util.null2String(rs10.getString("departmentid"));
					}
					//触发客户业务信息补充流程
					createWF(id,"16",busarr[0],departmentid,createdate); 
					lg.writeLog("网投统计服务---结束");
				}
				if (list2.equals("0")) {
					lg.writeLog("e服务专业版---开始");
					String sql1 = "SELECT businessmanager FROM  Matrixtable_7 where business='17'";
					rs1.executeSql(sql1);
					String businessmanager = "";
					if (rs1.next()) {
						businessmanager = Util.null2String(rs1
								.getString("businessmanager"));
					}
					String[] busarr = businessmanager.split(",");
					UUID uuid = UUID.randomUUID();
					String wyid = uuid.toString();// 生成唯一的标识码
					String sql2 = "insert into uf_crm_custbusiness(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
							+ "customer,business,businessmanager,uuid) values ("
							+ modeId
							+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),'"
							+ id
							+ "','"
							+ "17"
							+ "','"
							+ busarr[0]
							+ "','"
							+ wyid + "')";
					rs2.executeSql(sql2);
					String sql5 = "select id from uf_crm_custbusiness where uuid='"
							+ wyid + "'";
					rs5.executeSql(sql5);
					if (rs5.next()) {
						String ids = Util.null2String(rs5.getString("id"));
						int logid = Integer.parseInt(ids);
						ModeRightInfo ModeRightInfo = new ModeRightInfo();
						ModeRightInfo.editModeDataShare(5, modeId, logid);
					}
					
					String departmentid="";
					String sql10=" select departmentid from hrmresource a where id='"+busarr[0]+"'";
					rs10.executeSql(sql10);
					if(rs10.next()){
						departmentid=Util.null2String(rs10.getString("departmentid"));
					}
					//触发客户业务信息补充流程
					createWF(id,"17",busarr[0],departmentid,createdate); 	
					lg.writeLog("e服务专业版---结束");
				}
				if (list3.equals("0")) {
					String sql1 = "SELECT businessmanager FROM  Matrixtable_7 where business='18'";
					rs1.executeSql(sql1);
					String businessmanager = "";
					if (rs1.next()) {
						businessmanager = Util.null2String(rs1
								.getString("businessmanager"));
					}
					String[] busarr = businessmanager.split(",");
					UUID uuid = UUID.randomUUID();
					String wyid = uuid.toString();// 生成唯一的标识码
					String sql2 = "insert into uf_crm_custbusiness(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
							+ "customer,business,businessmanager,uuid) values ("
							+ modeId
							+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),'"
							+ id
							+ "','"
							+ "18"
							+ "','"
							+ busarr[0]
							+ "','"
							+ wyid + "')";
					rs2.executeSql(sql2);
					String sql5 = "select id from uf_crm_custbusiness where uuid='"
							+ wyid + "'";
					rs5.executeSql(sql5);
					if (rs5.next()) {
						String ids = Util.null2String(rs5.getString("id"));
						int logid = Integer.parseInt(ids);
						ModeRightInfo ModeRightInfo = new ModeRightInfo();
						ModeRightInfo.editModeDataShare(5, modeId, logid);
					}
				}
			}
			if (ifbond.equals("0")) {// 是证券公司
				if (bond1.equals("0")) {
					String sql3 = "SELECT businessmanager FROM  Matrixtable_7 where business='13'";
					rs3.executeSql(sql3);
					String businessmanager = "";
					if (rs3.next()) {
						businessmanager = Util.null2String(rs3
								.getString("businessmanager"));
					}
					String[] busarr = businessmanager.split(",");
					UUID uuid = UUID.randomUUID();
					String wyid = uuid.toString();// 生成唯一的标识码
					String sql4 = "insert into uf_crm_custbusiness(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
							+ "customer,business,businessmanager,uuid) values ("
							+ modeId
							+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),'"
							+ id
							+ "','"
							+ "13"
							+ "','"
							+ busarr[0]
							+ "','"
							+ wyid + "')";
					rs4.executeSql(sql4);
					String sql6 = "select id from uf_crm_custbusiness where uuid='"
							+ wyid + "'";
					rs6.executeSql(sql6);
					if (rs6.next()) {
						String ids = Util.null2String(rs6.getString("id"));
						int logid = Integer.parseInt(ids);
						ModeRightInfo ModeRightInfo = new ModeRightInfo();
						ModeRightInfo.editModeDataShare(5, modeId, logid);
					}
				}
				if (bond2.equals("0")) {
					String sql3 = "SELECT  businessmanager FROM  Matrixtable_7 where business='14' ";
					rs3.executeSql(sql3);
					String businessmanager = "";
					if (rs3.next()) {
						businessmanager = Util.null2String(rs3
								.getString("businessmanager"));
					}
					String[] busarr = businessmanager.split(",");
					UUID uuid = UUID.randomUUID();
					String wyid = uuid.toString();// 生成唯一的标识码
					String sql4 = "insert into uf_crm_custbusiness(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
							+ "customer,business,businessmanager,uuid) values ("
							+ modeId
							+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),'"
							+ id
							+ "','"
							+ "14"
							+ "','"
							+ busarr[0]
							+ "','"
							+ wyid + "')";
					rs4.executeSql(sql4);
					String sql6 = "select id from uf_crm_custbusiness where uuid='"
							+ wyid + "'";
					rs6.executeSql(sql6);
					if (rs6.next()) {
						String ids = Util.null2String(rs6.getString("id"));
						int logid = Integer.parseInt(ids);
						ModeRightInfo ModeRightInfo = new ModeRightInfo();
						ModeRightInfo.editModeDataShare(5, modeId, logid);
					}
				}
				if (bond3.equals("0")) {
					String sql3 = "SELECT businessmanager FROM  Matrixtable_7 where business ='15'";
					rs3.executeSql(sql3);
					String businessmanager = "";
					if (rs3.next()) {
						businessmanager = Util.null2String(rs3
								.getString("businessmanager"));
					}
					String[] busarr = businessmanager.split(",");
					UUID uuid = UUID.randomUUID();
					String wyid = uuid.toString();// 生成唯一的标识码
					String sql4 = "insert into uf_crm_custbusiness(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
							+ "customer,business,businessmanager,uuid) values ("
							+ modeId
							+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),'"
							+ id
							+ "','"
							+ "15"
							+ "','"
							+ busarr[0]
							+ "','"
							+ wyid + "')";
					rs4.executeSql(sql4);
					String sql6 = "select id from uf_crm_custbusiness where uuid='"
							+ wyid + "'";
					rs6.executeSql(sql6);
					if (rs6.next()) {
						String ids = Util.null2String(rs6.getString("id"));
						int logid = Integer.parseInt(ids);
						ModeRightInfo ModeRightInfo = new ModeRightInfo();
						ModeRightInfo.editModeDataShare(5, modeId, logid);
					}
				}
			}
			if (ifmarket.equals("0") && marketbus.equals("0")) {// 是科创板公司
				lg.writeLog("e服务科创专版---开始");
				String sql7 = "SELECT  businessmanager FROM  Matrixtable_7 where business='22'";
				rs7.executeSql(sql7);
				if (rs7.next()) {
					UUID uuid = UUID.randomUUID();
					String wyid = uuid.toString();// 生成唯一的标识码
					String businessmanager = Util.null2String(rs7
							.getString("businessmanager"));// 业务经理
					String[] busarr = businessmanager.split(",");
					String sql8 = "insert into uf_crm_custbusiness(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
							+ "customer,business,businessmanager,uuid) values ("
							+ modeId
							+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),'"
							+ id
							+ "','"
							+ "22"
							+ "','"
							+ busarr[0]
							+ "','"
							+ wyid + "')";
					rs8.executeSql(sql8);
					String sql9 = "select id from uf_crm_custbusiness where uuid='"
							+ wyid + "'";
					rs9.executeSql(sql9);
					if (rs9.next()) {
						String ids = Util.null2String(rs9.getString("id"));
						int logid = Integer.parseInt(ids);
						ModeRightInfo ModeRightInfo = new ModeRightInfo();
						ModeRightInfo.editModeDataShare(5, modeId, logid);
					}
					
					String departmentid="";
					String sql10=" select departmentid from hrmresource a where id='"+busarr[0]+"'";
					rs10.executeSql(sql10);
					if(rs10.next()){
						departmentid=Util.null2String(rs10.getString("departmentid"));
					}
					//触发客户业务信息补充流程
					createWF(id,"22",busarr[0],departmentid,createdate); 
				}
				lg.writeLog("e服务科创专版---结束");
				
				
			}
		}

	}

	
	/**
	 * 创建客户业务信息补充流程
	 */
	public int createWF(String customerid,String business,String creator,String dept,String createdate){
		String newrequestid = "";
		lg.writeLog("creator="+creator+";dept="+dept+";createdate="+createdate);
		try{
			lg.writeLog("创建客户业务信息补充流程子--开始");
			String workflowid = "431";//流程id
			String lcbt = "客户业务信息补充";// 流程标题   
			RequestService requestService = new RequestService();
			RequestInfo requestInfo = new RequestInfo();            
			requestInfo.setWorkflowid(workflowid);//流程类型id
			requestInfo.setCreatorid(creator);//创建人	
			requestInfo.setDescription(lcbt);//设置流程标题 
			requestInfo.setRequestlevel("0");//0 正常，1重要，2紧急
			requestInfo.setIsNextFlow("0");//流转到下一节点
			
			MainTableInfo mainTableInfo = new MainTableInfo();
			List<Property> fields = new ArrayList<Property>();
			Property field = null;	
			
			field = new Property();
			field.setName("createdate");
			field.setValue(createdate);
			fields.add(field);
			
			field = new Property();
			field.setName("creator");
			field.setValue(creator);
			fields.add(field);
			
			field = new Property();
			field.setName("dept");
			field.setValue(dept);
			fields.add(field);
					
			field = new Property();
			field.setName("customer");
			field.setValue(customerid);
			fields.add(field);
			
			field = new Property();
			field.setName("business");
			field.setValue(business);
			fields.add(field);			
			
			Property[] fieldarray = (Property[]) fields.toArray(new Property[fields.size()]);
			mainTableInfo.setProperty(fieldarray);
			requestInfo.setMainTableInfo(mainTableInfo);			
			newrequestid = requestService.createRequest(requestInfo);			
			
			lg.writeLog("创建客户业务信息补充流程子--结束--"+newrequestid);
		}catch(Exception e){
			lg.writeLog("创建客户业务信息补充流程子--出错："+e);
		}
		return Util.getIntValue(newrequestid,0);
	}
}
