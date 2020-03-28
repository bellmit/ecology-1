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
//信息商上游触发上证level1申请流程
public class LEVEL1_SQ_DAO extends BaseCronJob{
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
	 * level1申请流程触发
	 * 
	 */
	public void create(){
		RecordSetDataSource rds = new RecordSetDataSource("exchangeDB");
		String sql = "select * from SYNC_WEBSITE_LEVEL1 where STATUS=0";
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
			String gsbj = "";
			String mgs = "";
			String wxzz = "";
			String xzgly = "";
			
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
				wxzz = Util.null2String(rds1.getString("SATELLITE_ADDRESS"));
				 xzgly = Util.null2String(rds1.getString("SATELLITE_MANAGER"));
				gsbj =Util.null2String(rds1.getString("MAIN_BUSINESS"));
				mgs =Util.null2String(rds1.getString("SHAREHOLDING_RATIO"));				
			}
			//申请许可要素
			//申请许可内容
			String sqxknr = Util.null2String(rds.getString("APPLY_CONTENT"));
			if(sqxknr !=""){
				Boolean a=sqxknr.contains("实时行情");
				Boolean b=sqxknr.contains("延时行情");
				if(a){
					sqxknr="0";
				}
				if(b){
					sqxknr="1";
				}
			
		}
			//申请许可用途
			String sqxkyt = Util.null2String(rds.getString("APPLY_PURPOSE"));
			String dnrjzs="";
			String hlwwzzs="";
			String sjzs="";
			String gbdszs="";
			String qt1="";
			if(sqxkyt !=""){
					Boolean a=sqxkyt.contains("电脑软件");
					Boolean b=sqxkyt.contains("互联网网站");
					Boolean c=sqxkyt.contains("手机");
					Boolean d=sqxkyt.contains("广播电视");
					Boolean e=sqxkyt.contains("其他");
					if(a){
						dnrjzs="1";
					}
					if(b){
						hlwwzzs="1";
					}
					if(c){
						sjzs="1";
					}
					if(d){
						gbdszs="1";
					}
					if(e){
						qt1="1";
					}
				
			}
			//申请许可范围
			String sqxkfw = Util.null2String(rds.getString("APPLY_RANGE"));
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
			//申请许可年限
			String sqxknxbegin = Util.null2String(rds.getString("APPLY_START_TIME"));
			String sqxknxend = Util.null2String(rds.getString("APPLY_END_TIME"));
			
			
			String sqsj = Util.null2String(rds.getString("APPLY_TIME"));
			//行情接受方式
			String hqjsfs = Util.null2String(rds.getString("RECEIVE_MODE"));
			String shztkd="";
			String hlw1="";
			String zx1="";
			if(hqjsfs !=null){
				Boolean a=hqjsfs.contains("上海证通宽带");
				Boolean b=hqjsfs.contains("互联网");
				Boolean c=hqjsfs.contains("专线");
				if(a){
					shztkd="1";
				}
				if(b){
					hlw1="1";
				}
				if(c){
					zx1="1";
				}
			}
			//产品或服务描述
			String cphfwmc = Util.null2String(rds.getString("PRODUCT"));
			String mbkh = Util.null2String(rds.getString("CUSTOMER"));
			
			String dj = Util.null2String(rds.getString("PRICE"));
			String tcsj = Util.null2String(rds.getString("LAUNCH_TIME"));
			//最终用户接收类别
			String zzyhjslb = Util.null2String(rds.getString("RECEIVE_DEVICE"));
			String pc="";
			String wife="";
			String dsjjdh="";
			String other="";
			if(zzyhjslb !=""){
				Boolean a=zzyhjslb.contains("电脑");
				Boolean b=zzyhjslb.contains("无线");
				Boolean c=zzyhjslb.contains("电视机/机顶盒");
				Boolean d=zzyhjslb.contains("其他");
				if(a){
					pc="1";
				}
				if(b){
					wife="1";
				}
				if(c){
					dsjjdh="1";
				}
				if(d){
					other="1";
				}
			}
			//传输方式
			String csfs = Util.null2String(rds.getString("TRANSMISSION_MODE"));
			String zx="";
			String wx="";
			String jyw="";
			String hlw="";
			String qt="";
			if(csfs !=""){
				Boolean a=csfs.contains("专线");
				Boolean b=csfs.contains("无线网络");
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
			}
			String qtgnhxx = Util.null2String(rds.getString("MEMO"));
			String othrebz = Util.null2String(rds.getString("RECEIVE_DEVICE_OTHER"));
			String qtsrk = Util.null2String(rds.getString("TRANSMISSION_MODE_OTHER"));
						
			//创建工作流开始
			try {
				int requestid = createWF(syid,gsid,gsmc,zcdz,zzjgdm,frdb,
						gswz,glfzr,fzrdh,ywlxr,lxrdh,gscz,
						Email,bgdz,jsfzr,fzrdh1,sjkfwq,gsbj,
						mgs,dnrjzs,hlwwzzs,sjzs,gbdszs,qt1,xjsdjs,shcs,zgnd, jhqs,qq,sqxknxbegin,sqxknxend,
						sqsj,shztkd, hlw1,zx1,cphfwmc,mbkh,dj,
						tcsj,pc,wife,dsjjdh,other,zx,wx,jyw,hlw,qt,qtgnhxx,sqxknr,wxzz, xzgly,othrebz,qtsrk);
				if(requestid > 0){
					SimpleDateFormat dateFormat_now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	               String createDate = dateFormat_now.format(new Date());
					String sqlupdate = "update SYNC_WEBSITE_LEVEL1 set STATUS=1,STEP2_TIME=to_date('"+createDate+"','yyyy-mm-dd hh24:mi:ss')  where ID='"+syid+"'";
					rds.executeSql(sqlupdate);
					lg.writeLog("level1申请流程触发工作流成功，流程requestid:"+requestid);
				}
			} catch (Exception e) {
				lg.writeLog("level1申请流程触发出错："+e);
			}
		}
		lg.writeLog("level1申请流程触发结束");
	}
	
	/**
	 * 创建level1申请流程
	 */
	
	public int createWF(String syid,String gsid,String gsmc,String zcdz,String zzjgdm,String frdb,
			String gswz,String glfzr,String fzrdh,String ywlxr,String lxrdh,String gscz,
			String Email,String bgdz,String jsfzr,String fzrdh1,String sjkfwq,String gsbj,
			String mgs,
			String dnrjzs,String hlwwzzs,String sjzs,String gbdszs,String qt1,
			String xjsdjs,String shcs,String zgnd,String jhqs,String qq,
			String sqxknxbegin,String sqxknxend,
			String sqsj,
			String shztkd,String hlw1,String zx1,
			String cphfwmc,
			String mbkh,String dj,
			String tcsj,
			String pc,String wife,String dsjjdh,String other,String zx,String wx,String jyw,String hlw,String qt,String qtgnhxx,
			String sqxknr,
			String wxzz,String xzgly,String othrebz,String qtsrk
			){
		String newrequestid = "";
		try{
			String workflowid = "274";	
			String lcbt = "LEVEL-1 申请";// 流程标题   
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
			if(sqxknr !=""){
			field = new Property();
			field.setName("sqxknr");
			field.setValue(sqxknr);
			fields.add(field);
			}
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
			
            //7,gswz,glfzr,fzrdh,ywlxr,lxrdh,gscz,
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
			
			//13,Email,bgdz,jsfzr,fzrdh1,sjkfwq,gsbj
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
			
			//19,mgs,dnzd,ydzd,sqxkfw,sqxknx,sqksksrq,sqsxjsrq
			if(mgs !=""){
			field = new Property();
			field.setName("mgs");
			field.setValue(mgs);
			fields.add(field);
			}
			if(dnrjzs !=""){
			field = new Property();
			field.setName("dnrjzs");
			field.setValue(dnrjzs);
			fields.add(field);
			}
			if(hlwwzzs !=""){
			field = new Property();
			field.setName("hlwwzzs");
			field.setValue(hlwwzzs);
			fields.add(field);
			}
			if(sjzs !=""){
			field = new Property();
			field.setName("sjzs");
			field.setValue(sjzs);
			fields.add(field);
			}
			if(gbdszs !=""){
			field = new Property();
			field.setName("gbdszs");
			field.setValue(gbdszs);
			fields.add(field);
			}
			if(qt1 !=""){
			field = new Property();
			field.setName("qt1");
			field.setValue(qt1);
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
			if(sqxknxbegin !=""){
			field = new Property();
			field.setName("sqxknxbegin");
			field.setValue(sqxknxbegin);
			fields.add(field);
			}
			if(sqxknxend !=""){
			field = new Property();
			field.setName("sqxknxend");
			field.setValue(sqxknxend);
			fields.add(field);
			}
			
			//25,sqsj,hqjsfs,cphfwmc,cpljdz,mbkh,dj,         
			if(sqsj !=""){
			field = new Property();
			field.setName("sqsj");
			field.setValue(sqsj);
			fields.add(field);
			}
			if(shztkd !=""){
			field = new Property();
			field.setName("shztkd");
			field.setValue(shztkd);
			fields.add(field);
			}
			if(hlw1 !=""){
			field = new Property();
			field.setName("hlw1");
			field.setValue(hlw1);
			fields.add(field);
			}
			if(zx1 !=""){
			field = new Property();
			field.setName("zx1");
			field.setValue(zx1);
			fields.add(field);
			}
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
			//31  tcsj,pc,mobile,qt2,zx,,wx,jyw,hlw,qt,qtgnhxx,ywbdynr,ywbdrnr
			if(tcsj !=""){
			field = new Property();
			field.setName("tcsj");
			field.setValue(tcsj);
			fields.add(field);
			}
			if(pc !=""){
			field = new Property();
			field.setName("pc");
			field.setValue(pc);
			fields.add(field);
			}

			if(wife !=""){
			field = new Property();
			field.setName("wifi");
			field.setValue(wife);
			fields.add(field);
			}
			if(dsjjdh !=""){
			field = new Property();
			field.setName("dsjjdh");
			field.setValue(dsjjdh);
			fields.add(field);
			}
			if(other !=""){
			field = new Property();
			field.setName("other");
			field.setValue(other);
			fields.add(field);
			}
			if(zx !=""){
			field = new Property();
			field.setName("zx");
			field.setValue(zx);
			fields.add(field);
			}
			if(wx !=""){
			field = new Property();
			field.setName("wx");
			field.setValue(wx);
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
			if(qt !=""){
			field = new Property();
			field.setName("qt");
			field.setValue(qt);
			fields.add(field);
			}
			if(qtgnhxx !=""){
			field = new Property();
			field.setName("qtgnhxx");
			field.setValue(qtgnhxx);
			fields.add(field);	
			}
			//wxzz, xzgly
			if(wxzz !=""){
			field = new Property();
			field.setName("wxzz");
			field.setValue(wxzz);
			fields.add(field);
			}
			if(xzgly !=""){
			field = new Property();
			field.setName("xzgly");
			field.setValue(xzgly);
			fields.add(field);
			}
			
			if(othrebz !=""){
				field = new Property();
				field.setName("othrebz");
				field.setValue(othrebz);
				fields.add(field);
				}
			if(qtsrk !=""){
				field = new Property();
				field.setName("qtsrk");
				field.setValue(qtsrk);
				fields.add(field);
				}
			//上游附件,根据gsid来查询
			RecordSetDataSource fjss = new RecordSetDataSource("exchangeDB");
			String sqlfj="select * from SYNC_ATTACH where info_id ='"+gsid+"'";
			fjss.executeSql(sqlfj);
			String docids = "";
			while(fjss.next()){
			    String NRBT= Util.null2String(fjss.getString("ATTACH_NAME"));//附件名称
			    
			    int aa = sc.getContent(gsid,"124",NRBT);
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
			lg.writeLog("level1申请流程触发出错："+e);
		}
		return Util.getIntValue(newrequestid,0);
	}
}
