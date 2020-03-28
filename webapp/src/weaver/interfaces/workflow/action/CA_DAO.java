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
//CA证书流程
//cron表达式：0 0/5 * * * ?  每5分钟触发一次
//测试，status查询3，修改为4，正式为0，修改为1
public class CA_DAO extends BaseCronJob{
	BaseBean lg = new BaseBean();
	Calendar todaycal = Calendar.getInstance();
	String syndate = Util.add0(todaycal.get(Calendar.YEAR), 4) + "-" + Util.add0(todaycal.get(Calendar.MONTH) + 1, 2) + "-" + Util.add0(todaycal.get(Calendar.DAY_OF_MONTH), 2);
	// 通过继承BaseCronJob类可以实现定时同步
	public void execute(){ 
		try {
			lg.writeLog("CA证书流程触发开始----"+syndate);
			create();
		} catch (Exception e) {
			lg.writeLog("CA证书流程触发出错："+e);
		}
	}

	/**
	 * CA证书流程触发
	 * 
	 */
	public void create(){
		RecordSetDataSource rds = new RecordSetDataSource("exchangeDB");
		String sql = "select * from SYNC_CA_KEY_INFO where STATUS=0";
		rds.executeSql(sql);
		lg.writeLog("CA证书中间表查询");
		while(rds.next()){
			lg.writeLog("CA证书while循环");
			
			String syid = Util.null2String(rds.getString("ID"));
			String zzjgdm = Util.null2String(rds.getString("ORGANIZATION_CODE"));
			String gsmc = Util.null2String(rds.getString("COMPANY_NAME"));
			
			String zslx = Util.null2String(rds.getString("KEY_NAME"));
			String yhlx = Util.null2String(rds.getString("KEY_USER_TYPE"));
			if(yhlx !=""){
				Boolean a=yhlx.contains("会员单位");
				Boolean b=yhlx.contains("上市公司");
				Boolean c=yhlx.contains("level2信息商");
				Boolean d=yhlx.contains("媒体机构");
				Boolean e=yhlx.contains("所内用户");
				Boolean f=yhlx.contains("证券公司");
				Boolean g=yhlx.contains("基金公司");
				Boolean h=yhlx.contains("非会员");
				Boolean i=yhlx.contains("其他类型");
				if(a){
					yhlx="0";
				}
				if(b){
					yhlx="1";
				}
				if(c){
					yhlx="2";
				}
				if(d){
					yhlx="3";
				}
				if(e){
					yhlx="4";
				}
				if(f){
					yhlx="5";
				}
				if(g){
					yhlx="6";
				}
				if(h){
					yhlx="7";
				}
				if(i){
					yhlx="8";
				}
			}
			
			String zsylx = Util.null2String(rds.getString("KEY_APP_TYPE"));
			if(zsylx !=""){
				Boolean a1=zsylx.contains("会籍业务");
				Boolean a2=zsylx.contains("上市公司");
				Boolean a3=zsylx.contains("媒体专区");
				Boolean a4=zsylx.contains("权证业务");
				Boolean a5=zsylx.contains("债基专区");
				Boolean a6=zsylx.contains("融资融券");
				if(a1){
					zsylx="0";
				}
				if(a2){
					zsylx="1";
				}
				if(a3){
					zsylx="2";
				}
				if(a4){
					zsylx="3";
				}
				if(a5){
					zsylx="4";
				}
				if(a6){
					zsylx="5";
				}
				Boolean a7=zsylx.contains("数据服务");
				Boolean a8=zsylx.contains("上市公司个性化数据服务");
				Boolean a9=zsylx.contains("ETF下载");
				Boolean a10=zsylx.contains("大宗交易");
				Boolean a11=zsylx.contains("网下申购电子化平台");
				Boolean a12=zsylx.contains("固定收益证券综合电子平台");
				if(a7){
					zsylx="6";
				}
				if(a8){
					zsylx="7";
				}
				if(a9){
					zsylx="8";
				}
				if(a10){
					zsylx="9";
				}
				if(a11){
					zsylx="10";
				}
				if(a12){
					zsylx="11";
				}
				Boolean a13=zsylx.contains("桌面安全系统");
				Boolean a14=zsylx.contains("设备证书");
				Boolean a15=zsylx.contains("Level-2");
				Boolean a16=zsylx.contains("SSL");
				Boolean a17=zsylx.contains("消息总线");
				Boolean a18=zsylx.contains("托管行XBRL签名");
				if(a13){
					zsylx="12";
				}
				if(a14){
					zsylx="13";
				}
				if(a15){
					zsylx="14";
				}
				if(a16){
					zsylx="15";
				}
				if(a17){
					zsylx="16";
				}
				if(a18){
					zsylx="17";
				}
				Boolean a19=zsylx.contains("其他应用");
				Boolean a20=zsylx.contains("基金XBRL报送");
				Boolean a21=zsylx.contains("监察信息发布");
				Boolean a22=zsylx.contains("报价回购监管信息下载");
				Boolean a23=zsylx.contains("投保基金相关应用");
				Boolean a24=zsylx.contains("证监会IPO项目");
				Boolean a25=zsylx.contains("约定购回式证券交易业务");
				Boolean a26=zsylx.contains("债券质押式报价回购业务");
				if(a19){
					zsylx="18";
				}
				if(a20){
					zsylx="19";
				}
				if(a21){
					zsylx="20";
				}
				if(a22){
					zsylx="21";
				}
				if(a23){
					zsylx="22";
				}
				if(a24){
					zsylx="23";
				}
				if(a25){
					zsylx="24";
				}
				if(a26){
					zsylx="25";
				}
			}
			
			String zsjzlx = Util.null2String(rds.getString("KEY_MEDIA_TYPE"));
			if(zsjzlx !=""){
				//Boolean a=zsjzlx.contains("Ekey");
				//Boolean b=zsjzlx.contains("文件");
				Boolean a=zsjzlx.contains("1");
				Boolean b=zsjzlx.contains("2");
				if(a){
					zsjzlx="0";
				}
				if(b){
					zsjzlx="1";
				}
			}
			String zssflx = Util.null2String(rds.getString("KEY_FEE_TYPE"));
			if(zsjzlx !=""){
				Boolean a=zssflx.contains("用户收费");
				Boolean b=zssflx.contains("所司结算");
				Boolean c=zssflx.contains("公司结算");
				Boolean d=zssflx.contains("内部证书");
				Boolean e=zssflx.contains("免费");
				if(a){
					zssflx="0";
				}
				if(b){
					zssflx="1";
				}
				if(c){
					zssflx="2";
				}
				if(d){
					zssflx="3";
				}
				if(e){
					zssflx="4";
				}
			}
			String ddlx = Util.null2String(rds.getString("ORDER_TYPE"));
//			if(ddlx !=""){
//				Boolean a=ddlx.contains("制证");
//				Boolean b=ddlx.contains("续费");
//				if(a){
//					ddlx="0";
//				}
//				if(b){
//					ddlx="1";
//				}
//			}
			
			String jzfjekh = Util.null2String(rds.getString("MEDIA_FEE_CUSTOMER"));
			String jzfjejys = Util.null2String(rds.getString("MEDIA_FEE_SSE"));
			
			String fwfjekh = Util.null2String(rds.getString("SERVICE_FEE_CUSTOMER"));
			String fwfjys = Util.null2String(rds.getString("SERVICE_FEE_SSE"));
			
			String caywpt = Util.null2String(rds.getString("USER_NAME"));
			String caid = Util.null2String(rds.getString("CA_REQUESTID"));
			
			String yhdz = Util.null2String(rds.getString("USER_ADDRES"));
			String yhyb = Util.null2String(rds.getString("USER_ZIPCODE"));
			
			String yhlxr = Util.null2String(rds.getString("USER_LINKMAN"));
			String lxdh = Util.null2String(rds.getString("USER_TEL"));
			String jbr = Util.null2String(rds.getString("OPERATOR"));
			String jbrdh = Util.null2String(rds.getString("OPERATOR_TEL"));
			
			String bhly = Util.null2String(rds.getString("REJECT_REASON"));
			String bz = Util.null2String(rds.getString("MEMO"));
			//zzjgdm,gsmc,zslx,yhlx,zsylx,zsjzlx,
			//zssflx,ddlx,jzfjekh,jzfjejys,fwfjekh,fwfjys,
			//caywpt,caid,yhdz,yhyb,yhlxr,lxdh
			//jbr,jbrdh,bhly,bz
			//创建工作流开始
			try {
				lg.writeLog("CA证书创建流程");
				int requestid = createWF(zzjgdm,gsmc,zslx,yhlx,zsylx,zsjzlx,zssflx,ddlx,jzfjekh,jzfjejys,fwfjekh,fwfjys,caywpt,caid,yhdz,yhyb,yhlxr,lxdh,jbr,jbrdh,bhly,bz,syid);
				if(requestid > 0){
					lg.writeLog("CA证书回写中间表");
					SimpleDateFormat dateFormat_now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	               String createDate = dateFormat_now.format(new Date());
	               //java中日期转换为oracle中日期总结：
	               //1.实体类中日期属性定义为String类型。 
	               //2.sql语句中与日期对应得列写成to_date(?,'yyyy-mm-dd hh24:mi:ss') 
					String sqlupdate = "update SYNC_CA_KEY_INFO set STATUS=1,STEP2_TIME=to_date('"+createDate+"','yyyy-mm-dd hh24:mi:ss')  where ID='"+syid+"'";
					rds.executeSql(sqlupdate);
					lg.writeLog("CA证书流程触发工作流成功，流程requestid:"+requestid);
				}
			} catch (Exception e) {
				lg.writeLog("CA证书流程触发出错："+e);
			}
		}
		lg.writeLog("CA证书流程触发结束");
	}
	
	/**
	 * 创建CA证书流程
	 */
	//zzjgdm,gsmc,zslx,yhlx,zsylx,zsjzlx,
	//zssflx,ddlx,jzfjekh,jzfjejys,fwfjekh,fwfjys,
	//caywpt,caid,yhdz,yhyb,yhlxr,lxdh
	//jbr,jbrdh,bhly,bz,syid
	public int createWF(String zzjgdm,String gsmc,String zslx,String yhlx,String zsylx,String zsjzlx,
			                        String zssflx,String ddlx,String jzfjekh,String jzfjejys,String fwfjekh,String fwfjys,
			                        String caywpt,String caid,String yhdz,String yhyb,String yhlxr,String lxdh,
			                        String jbr,String jbrdh,String bhly,String bz,String syid
			){
		String newrequestid = "";
		try{
			String workflowid = "110";	
			String lcbt = "CA证书-"+syndate;// 流程标题   
			RequestService requestService = new RequestService();
			RequestInfo requestInfo = new RequestInfo();
			
			requestInfo.setWorkflowid(workflowid);//流程类型id
			requestInfo.setCreatorid("1");//创建人		
			requestInfo.setDescription(lcbt);//设置流程标题 
			requestInfo.setRequestlevel("0");//0 正常，1重要，2紧急
			requestInfo.setIsNextFlow("1");//流转到下一节点
			
			MainTableInfo mainTableInfo = new MainTableInfo();
			List<Property> fields = new ArrayList<Property>();
			Property field = null;
			
			// 主表字段开始,1,//zzjgdm,gsmc,zslx,yhlx,zsylx,zsjzlx,
			if(zzjgdm !=""){
			field = new Property();
			field.setName("zzjgdm");
			field.setValue(zzjgdm);
			fields.add(field);
			}
			if(gsmc !=""){
			field = new Property();
			field.setName("gsmc");
			field.setValue(gsmc);
			fields.add(field);
			}
			if(zslx !=""){
			field = new Property();
			field.setName("zslx");
			field.setValue(zslx);
			fields.add(field);
			}
			if(yhlx !=""){
			field = new Property();
			field.setName("yhlx");
			field.setValue(yhlx);
			fields.add(field);
			}
			if(zsylx !=""){
			field = new Property();
			field.setName("zsylx");
			field.setValue(zsylx);
			fields.add(field);
			}
			if(zsjzlx !=""){
			field = new Property();
			field.setName("zsjzlx");
			field.setValue(zsjzlx);
			fields.add(field);
			}
//7,zssflx,ddlx,jzfjekh,jzfjejys,fwfjekh,fwfjys,
			if(zssflx !=""){
			field = new Property();
			field.setName("zssflx");
			field.setValue(zssflx);
			fields.add(field);
			}
			if(ddlx !=""){
			field = new Property();
			field.setName("ddlx");
			field.setValue(ddlx);
			fields.add(field);
			}
			if(jzfjekh !=""){
			field = new Property();
			field.setName("jzfjekh");
			field.setValue(jzfjekh);
			fields.add(field);
			}
			if(jzfjejys !=""){
			field = new Property();
			field.setName("jzfjejys");
			field.setValue(jzfjejys);
			fields.add(field);
			}
			if(fwfjekh !=""){
			field = new Property();
			field.setName("fwfjekh");
			field.setValue(fwfjekh);
			fields.add(field);
			}
			if(fwfjys !=""){
			field = new Property();
			field.setName("fwfjys");
			field.setValue(fwfjys);
			fields.add(field);
			}
			//13,	//caywpt,caid,yhdz,yhyb,yhlxr,lxdh
			
			if(caywpt !=""){
			field = new Property();
			field.setName("caywpt");
			field.setValue(caywpt);
			fields.add(field);
			}
			if(caid !=""){
			field = new Property();
			field.setName("caid");
			field.setValue(caid);
			fields.add(field);
			}
			if(yhdz !=""){
			field = new Property();
			field.setName("yhdz");
			field.setValue(yhdz);
			fields.add(field);
			}
			if(yhyb !=""){
			field = new Property();
			field.setName("yhyb");
			field.setValue(yhyb);
			fields.add(field);
			}
			if(yhlxr !=""){
			field = new Property();
			field.setName("yhlxr");
			field.setValue(yhlxr);
			fields.add(field);
			}
			if(lxdh !=""){
			field = new Property();
			field.setName("lxdh");
			field.setValue(lxdh);
			fields.add(field);
			}
			//19,jbr,jbrdh,bhly,bz
			if(jbr !=""){
			field = new Property();
			field.setName("jbr");
			field.setValue(jbr);
			fields.add(field);
			}
			if(jbrdh !=""){
			field = new Property();
			field.setName("jbrdh");
			field.setValue(jbrdh);
			fields.add(field);
			}
			if(bhly !=""){
			field = new Property();
			field.setName("bhly");
			field.setValue(bhly);
			fields.add(field);
			}
			if(bz !=""){
			field = new Property();
			field.setName("bz");
			field.setValue(bz);
			fields.add(field);
			}
			if(syid !=""){
			field = new Property();
			field.setName("syid");
			field.setValue(syid);
			fields.add(field);
			}
	
			fields.add(field);
			Property[] fieldarray = (Property[]) fields.toArray(new Property[fields.size()]);
			mainTableInfo.setProperty(fieldarray);
			requestInfo.setMainTableInfo(mainTableInfo);
			newrequestid = requestService.createRequest(requestInfo);
		}catch(Exception e){
			lg.writeLog("CA证书流程触发出错："+e);
		}
		return Util.getIntValue(newrequestid,0);
	}
}
