package weaver.interfaces.workflow.action;

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
//期权行情许可申请流程
public class CRM_QQHQXKSQ_DAO extends BaseCronJob{
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
	 * 期权行情许可申请流程触发
	 * 
	 */
	public void create(){
		RecordSetDataSource rds = new RecordSetDataSource("exchangeDB");
		String sql = "select * from SYNC_WEBSITE_OPTION where STATUS=0";
		rds.executeSql(sql);
		while(rds.next()){
			//31个字段
			
			String syid = Util.null2String(rds.getString("ID"));	
			String gsid = Util.null2String(rds.getString("COMPANY_ID"));
			//公司基本信息：根据gsid查询出公司的全部信息
			String gsmc = "";
			String zcdz = "";
			String zzjgdm = "";
			String frdb = "";
			String gswz = "";
			String glfzr = "";
			String fzrdh = "";
			String ywlxr = "";
			String lxrdh = "";
			String gscz = "";
			String Email = "";
			String bgdz = "";
			String jsfzr = "";
			String fzrdh1 = "";
			String sjkfwq = "";
			String wxxzazd = "";
			String xzgly = "";
			String gsbj = "";
			String mgs = "";
			
			RecordSetDataSource rds1 = new RecordSetDataSource("exchangeDB");
			String sqlcom = "select * from SYNC_COMPANY where ID='"+gsid+"'";
			rds1.executeSql(sqlcom);
			while(rds1.next()){
				gsmc =Util.null2String(rds1.getString("COMPANY_NAME"));
				zcdz =Util.null2String(rds1.getString("REGISTERED_ADDRESS"));
				zzjgdm =Util.null2String(rds1.getString("ORGANIZATION_CODE"));
				frdb =Util.null2String(rds1.getString("LEGAL_PERSON"));
				gswz =Util.null2String(rds1.getString("WEBSITE"));
				glfzr =Util.null2String(rds1.getString("MANAGER_PRINCIPAL"));
				fzrdh =Util.null2String(rds1.getString("MANAGER_TEL"));
				ywlxr =Util.null2String(rds1.getString("BUSINESS_LINKMAN"));
				lxrdh =Util.null2String(rds1.getString("BUSINESS_TEL"));
				gscz =Util.null2String(rds1.getString("FAX"));
				Email =Util.null2String(rds1.getString("EMAIL"));
				bgdz =Util.null2String(rds1.getString("ADDRESS"));
				jsfzr =Util.null2String(rds1.getString("TECH_PRINCIPAL"));
				fzrdh1 =Util.null2String(rds1.getString("TECH_TEL"));
				sjkfwq =Util.null2String(rds1.getString("SYSTEM_ADDRESS"));
			    wxxzazd = Util.null2String(rds1.getString("SATELLITE_ADDRESS"));
				 xzgly = Util.null2String(rds1.getString("SATELLITE_MANAGER"));
				gsbj =Util.null2String(rds1.getString("MAIN_BUSINESS"));
				mgs =Util.null2String(rds1.getString("SHAREHOLDING_RATIO"));				
			}
			
			//申请许可要素：
			//申请许可内容
			String sqxknr = Util.null2String(rds.getString("APPLY_CONTENT"));
			//申请许可用途
			String sqxkyt = Util.null2String(rds.getString("APPLY_PURPOSE"));
			String dnrjzs="";
			String wzzs="";
			String sjyyzs="";
			String qt2="";
			if(sqxkyt !=""){
					Boolean a=sqxkyt.contains("电脑软件");
					Boolean b=sqxkyt.contains("网站");
					Boolean c=sqxkyt.contains("手机");
					Boolean d=sqxkyt.contains("其他");
					if(a){
						dnrjzs="1";
					}
					if(b){
						wzzs="1";
					}
					if(c){
						sjyyzs="1";
					}
					if(d){
						qt2="1";
					}
				
			}
	
			String sqxkfw = Util.null2String(rds.getString("APPLY_RANGE"));//申请许可范围
			String xjsdjs="";
			String shcs="";
			String zgnd="";
			String jhqs="";
			String qq="";
			
			if(sqxkfw !=null){
				Boolean a=sqxkfw.contains("县级市/地级市");
				Boolean b=sqxkfw.contains("省会城市");
				Boolean c=sqxkfw.contains("中国内地");
				Boolean d=sqxkfw.contains("京沪/全省");
				Boolean e=sqxkfw.contains("全球");
				if(a){
					xjsdjs="1";
				}
				if(b){
					shcs="1";
				}
				if(c){
					zgnd="1";
				}
				if(d){
					jhqs="1";
				}
				if(e){
					qq="1";
				}
			}
			String sqxknxks = Util.null2String(rds.getString("APPLY_START_TIME"));//申请许可年限
			String sqxknxjs = Util.null2String(rds.getString("APPLY_END_TIME"));
			
			
			String sqsj = Util.null2String(rds.getString("APPLY_TIME"));
			
			String hqjsfs = Util.null2String(rds.getString("RECEIVE_MODE"));//行情接受方式
			
			//产品或服务描述
			String cphfwmc = Util.null2String(rds.getString("PRODUCT"));
			String mbkh = Util.null2String(rds.getString("CUSTOMER"));
			
			String dj = Util.null2String(rds.getString("PRICE"));
			String tcsj = Util.null2String(rds.getString("LAUNCH_TIME"));
			
			String zzyhjslb = Util.null2String(rds.getString("RECEIVE_DEVICE"));//最终用户接收类别
			String dnswb="";
			String sj="";
			String qt="";
			if(zzyhjslb !=""){
				Boolean a=zzyhjslb.contains("电脑/上网本");
				Boolean b=zzyhjslb.contains("手机");
				Boolean c=zzyhjslb.contains("其他");
				if(a){
					dnswb="1";
				}
				if(b){
					sj="1";
				}
				if(c){
					qt="1";
				}
			}
			
			String csfs = Util.null2String(rds.getString("TRANSMISSION_MODE"));//传输方式------------
			String zx="";
			String wxwl="";
			String jyw="";
			String hlw="";
			String qt3="";
			if(csfs !=""){
				Boolean a=csfs.contains("专线");				
				Boolean b=csfs.contains("局域网");
				Boolean c=csfs.contains("互联网");
				Boolean d=csfs.contains("无线网络");
				Boolean e=csfs.contains("其他");
				if(a){
					zx="1";
				}
				if(b){
					jyw="1";
				}
				if(c){
					hlw="1";
				}
				if(d){
					wxwl="1";
				}
				if(e){
					qt3="1";
				}
			}
			String qtgnhxx = Util.null2String(rds.getString("MEMO"));
			String qtsrk = Util.null2String(rds.getString("RECEIVE_DEVICE_OTHER"));
			String qtsrk3 = Util.null2String(rds.getString("TRANSMISSION_MODE_OTHER"));
						
			//验证客户是否存在
			RecordSet rs = new RecordSet();
			RecordSet rs1 = new RecordSet();			
			String customerid="";//客户id
			String cusbusinessid="";//客户业务id
			String businessid="2";//期权行情许可业务
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
				int requestid = createWF(syid,gsid,gsmc,zcdz,zzjgdm,frdb,
						gswz,glfzr,fzrdh,ywlxr,lxrdh,gscz,wxxzazd,xzgly,sqxknr,
						Email,bgdz,jsfzr,fzrdh1,sjkfwq,gsbj,mgs,
						dnrjzs,wzzs,sjyyzs,qt2,xjsdjs,shcs,zgnd, jhqs,qq,sqxknxks,sqxknxjs,sqsj,hqjsfs,
						cphfwmc,mbkh,dj,tcsj,dnswb,sj,qt,zx,wxwl,jyw,hlw,qt3,qtgnhxx,qtsrk,qtsrk3,customerid);
				if(requestid > 0){
					SimpleDateFormat dateFormat_now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	               String createDate = dateFormat_now.format(new Date());
					String sqlupdate = "update SYNC_WEBSITE_OPTION set STATUS=1,STEP2_TIME=to_date('"+createDate+"','yyyy-mm-dd hh24:mi:ss')  where ID='"+syid+"'";
					rds.executeSql(sqlupdate);
					lg.writeLog("期权行情许可申请流程触发工作流成功，流程requestid:"+requestid);
				}
			} catch (Exception e) {
				lg.writeLog("期权行情许可申请流程触发出错："+e);
			}
		}
	}
	
	/**
	 * 创建期权行情许可申请流程
	 */
	public int createWF(String syid,String gsid,String gsmc,String zcdz,String zzjgdm,String frdb,
			String gswz,String glfzr,String fzrdh,String ywlxr,String lxrdh,String gscz,String wxxzazd,String xzgly,String sqxknr,
			String Email,String bgdz,String jsfzr,String fzrdh1,String sjkfwq,String gsbj,String mgs,
			String dnrjzs,String wzzs,String sjyyzs,String qt2,
			String xjsdjs,String shcs,String zgnd,String jhqs,String qq,String sqxknxks,String sqxknxjs,String sqsj,String hqjsfs,
			String cphfwmc,String mbkh,String dj,
			String tcsj,String dnswb,String sj,String qt,String zx,String wxwl,String jyw,String hlw,String qt3,String qtgnhxx,
			String qtsrk,String qtsrk3,String customerid
			){
		String newrequestid = "";
		try{
			String workflowid = "473";	
			String lcbt = "期权行情许可申请";// 流程标题   
			RequestService requestService = new RequestService();
			RequestInfo requestInfo = new RequestInfo();
			
			requestInfo.setWorkflowid(workflowid);//流程类型id
			requestInfo.setCreatorid("124");//创建人		
			requestInfo.setDescription(lcbt);//设置流程标题 
			requestInfo.setRequestlevel("0");//0 正常，1重要，2紧急
			requestInfo.setIsNextFlow("1");//流转到下一节点
			
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
			
            //7,gswz,glfzr,fzrdh,ywlxr,lxrdh,gscz,wxxzazd,xzgly,sqxknr
			if(gswz !=""){
			field = new Property();
			field.setName("gswz");
			field.setValue(gswz);
			fields.add(field);
			}
			if(glfzr !=""){
			field = new Property();
			field.setName("glfzr");
			field.setValue(glfzr);
			fields.add(field);
			}
			if(fzrdh !=""){
			field = new Property();
			field.setName("fzrdh");
			field.setValue(fzrdh);
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
			if(wxxzazd !=""){
			field = new Property();
			field.setName("wxxzazd");
			field.setValue(wxxzazd);
			fields.add(field);
			}
			if(xzgly !=""){
			field = new Property();
			field.setName("xzgly");
			field.setValue(xzgly);
			fields.add(field);
			}
			if(sqxknr !=""){
			field = new Property();
			field.setName("sqxknr");
			field.setValue(sqxknr);
			fields.add(field);
			}
			
			//13,Email,bgdz,jsfzr,fzrdh1,sjkfwq,gsbj,mgs
			if(Email !=""){
			field = new Property();
			field.setName("Email");
			field.setValue(Email);
			fields.add(field);
			}
			if(bgdz !=""){
			field = new Property();
			field.setName("bgdz");
			field.setValue(bgdz);
			fields.add(field);
			}
			if(jsfzr !=""){
			field = new Property();
			field.setName("jsfzr");
			field.setValue(jsfzr);
			fields.add(field);
			}
			if(fzrdh1 !=""){
			field = new Property();
			field.setName("fzrdh1");
			field.setValue(fzrdh1);
			fields.add(field);
			}
			if(sjkfwq !=""){
			field = new Property();
			field.setName("sjkfwq");
			field.setValue(sjkfwq);
			fields.add(field);
			}
			if(gsbj !=""){
			field = new Property();
			field.setName("gsbj");
			field.setValue(gsbj);
			fields.add(field);
			}
			if(mgs !=""){
			field = new Property();
			field.setName("mgs");
			field.setValue(mgs);
			fields.add(field);
			}
			//19,dnrjzs,wzzs,sjyyzs,qt2,sqxkfw,sqxknxks,sqxknxjs,sqsj,hqjsfs,
			if(dnrjzs !=""){
			field = new Property();
			field.setName("dnrjzs");
			field.setValue(dnrjzs);
			fields.add(field);
			}
			if(wzzs !=""){
			field = new Property();
			field.setName("wzzs");
			field.setValue(wzzs);
			fields.add(field);
			}
			if(sjyyzs !=""){
			field = new Property();
			field.setName("sjyyzs");
			field.setValue(sjyyzs);
			fields.add(field);
			}
			if(qt2 !=""){
			field = new Property();
			field.setName("qt2");
			field.setValue(qt2);
			fields.add(field);
			}
			if(xjsdjs !=""){
				field = new Property();
				field.setName("xjsdjs");
				field.setValue(xjsdjs);
				fields.add(field);
				}
				if(shcs !=""){
				field = new Property();
				field.setName("shcs");
				field.setValue(shcs);
				fields.add(field);
				}
				if(zgnd !=""){
				field = new Property();
				field.setName("zgnd");
				field.setValue(zgnd);
				fields.add(field);
				}
				if(jhqs !=""){
				field = new Property();
				field.setName("jhqs");
				field.setValue(jhqs);
				fields.add(field);
				}
				if(qq !=""){
				field = new Property();
				field.setName("qq");
				field.setValue(qq);
				fields.add(field);
				}
			if(sqxknxks !=""){
			field = new Property();
			field.setName("sqxknxks");
			field.setValue(sqxknxks);
			fields.add(field);
			}
			if(sqxknxjs !=""){
				field = new Property();
				field.setName("sqxknxjs");
				field.setValue(sqxknxjs);
				fields.add(field);
				}
			if(sqsj !=""){
			field = new Property();
			field.setName("sqsj");
			field.setValue(sqsj);
			fields.add(field);
			}
			if(hqjsfs !=""){
			field = new Property();
			field.setName("hqjsfs");
			field.setValue(hqjsfs);
			fields.add(field);
			}
			//25,cphfwmc,mbkh,dj,tcsj,dnswb,sj,qt,zx,wxwl,jyw,hlw,qt3,qtgnhxx
			if(cphfwmc !=""){
			field = new Property();
			field.setName("cphfwmc");
			field.setValue(cphfwmc);
			fields.add(field);
			}
			if(mbkh !=""){		
			field = new Property();
			field.setName("mbkh");
			field.setValue(mbkh);
			fields.add(field);
			}
			if(dj !=""){
			field = new Property();
			field.setName("dj");
			field.setValue(dj);
			fields.add(field);
			}
			if(tcsj !=""){
			field = new Property();
			field.setName("tcsj");
			field.setValue(tcsj);
			fields.add(field);
			}
			if(dnswb !=""){	
			field = new Property();
			field.setName("dnswb");
			field.setValue(dnswb);
			fields.add(field);
			}
			if(sj !=""){
			field = new Property();
			field.setName("sj");
			field.setValue(sj);
			fields.add(field);
			}
			if(qt !=""){
			field = new Property();
			field.setName("qt");
			field.setValue(qt);
			fields.add(field);
			}
			if(zx !=""){
			field = new Property();
			field.setName("zx");
			field.setValue(zx);
			fields.add(field);
			}
			if(wxwl !=""){
			field = new Property();
			field.setName("wxwl");
			field.setValue(wxwl);
			fields.add(field);
			}
			if(jyw !=""){
			field = new Property();
			field.setName("jyw");
			field.setValue(jyw);
			fields.add(field);
			}
			if(hlw !=""){
			field = new Property();
			field.setName("hlw");
			field.setValue(hlw);
			fields.add(field);
			}
			if(qt3 !=""){
			field = new Property();
			field.setName("qt3");
			field.setValue(qt3);
			fields.add(field);
			}
			if(qtgnhxx !=""){
			field = new Property();
			field.setName("qtgnhxx");
			field.setValue(qtgnhxx);
			fields.add(field);		
			}
			
			if(qtsrk !=""){
				field = new Property();
				field.setName("qtsrk");
				field.setValue(qtsrk);
				fields.add(field);		
				}
			if(qtsrk3 !=""){
				field = new Property();
				field.setName("qtsrk3");
				field.setValue(qtsrk3);
				fields.add(field);		
				}
			if(customerid !=""){
				field = new Property();
				field.setName("customer");
				field.setValue(customerid);
				fields.add(field);
			}
			//上游附件,根据gsid来查询
			RecordSetDataSource fjss = new RecordSetDataSource("exchangeDB");
			String sqlfj="select * from SYNC_ATTACH where info_id ='"+syid+"'";
			fjss.executeSql(sqlfj);
			String docids = "";
			while(fjss.next()){
				String id= Util.null2String(fjss.getString("ATTACH_NAME"));//附件id
			    String NRBT= Util.null2String(fjss.getString("ATTACH_NAME"));//附件名称
			    
			    int aa = sc.getContent(id,"124",NRBT);
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
			fields.add(field);
			Property[] fieldarray = (Property[]) fields.toArray(new Property[fields.size()]);
			mainTableInfo.setProperty(fieldarray);
			requestInfo.setMainTableInfo(mainTableInfo);
			newrequestid = requestService.createRequest(requestInfo);
		}catch(Exception e){
			lg.writeLog("期权行情许可申请流程触发出错："+e);
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
