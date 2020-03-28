package weaver.interfaces.workflow.action;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import weaver.conn.*;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.schedule.BaseCronJob;
import weaver.soa.workflow.request.Cell;
import weaver.soa.workflow.request.DetailTable;
import weaver.soa.workflow.request.DetailTableInfo;
import weaver.soa.workflow.request.MainTableInfo;
import weaver.soa.workflow.request.Property;
import weaver.soa.workflow.request.RequestInfo;
import weaver.soa.workflow.request.RequestService;
import weaver.soa.workflow.request.Row;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import com.alibaba.fastjson.JSON;
//上证level2非展示数据申请流程
public class CRM_Level2_dataFeed_applicate extends BaseCronJob{
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
	 * level2非展示数据申请流程触发
	 * 
	 */
	public void create(){
		lg.writeLog("level2非展示数据申请流程触发开始：");
		RecordSetDataSource rds = new RecordSetDataSource("exchangeDB");
		String sql = "select * from SYNC_WEBSITE_LEVEL2_DATAFEED where STATUS=0";
		rds.executeSql(sql);
		lg.writeLog(sql);
		while(rds.next()){
			//上游id
			String syid = Util.null2String(rds.getString("ID"));	
			//公司id
			String companyId =Util.null2String(rds.getString("COMPANY_ID"));
			//公司名称 
			String companyName ="";
			//联系人
			String businessLinkMan ="";
			//电话
			String businessTel ="";
			//电子邮箱
			String email ="";
			//传真
			String fax ="";
			//联系地址
			//String address ="";
			//邮编
			//String zipCode ="";
			
			RecordSetDataSource rds1 = new RecordSetDataSource("exchangeDB");
			String sqlcom = "select * from SYNC_COMPANY where ID='"+companyId+"'";
			rds1.executeSql(sqlcom);
			lg.writeLog(sqlcom);
			while(rds1.next()){
				companyName =Util.null2String(rds1.getString("COMPANY_NAME"));
				businessLinkMan =Util.null2String(rds1.getString("BUSINESS_LINKMAN"));
				businessTel =Util.null2String(rds1.getString("BUSINESS_TEL"));
				fax =Util.null2String(rds1.getString("FAX"));
				email =Util.null2String(rds1.getString("EMAIL"));

				/*String address_zipCode =Util.null2String(rds1.getString("ADDRESS"));
				int num = address_zipCode.indexOf(",");
				if(num>-1){
					address = address_zipCode.substring(0, num);
					zipCode = address_zipCode.substring(num+1);
				}else{
					address = address_zipCode ;
				}*/
			}
			//数据用途
			String dataUse = Util.null2String(rds.getString("DATA_USE"));
			//期权做市
			String dataUse_qxzs = "" ;
			//会员监控系统优化
			String dataUse_xtyh ="";
			//系统开发
			String dataUse_xtkf ="";
			//数据用途其他
			String dataUser_other ="";
			//数据展示其他填写
			String dataUse_Owrite = "";
			if(""!=dataUse){
				if(dataUse.contains("期权做市")){
					dataUse_qxzs = "1";
				}
				if(dataUse.contains("会员监控系统优化")){
					dataUse_xtyh = "1";
				}
				if(dataUse.contains("系统开发")){
					dataUse_xtkf = "1";
				}
				if(dataUse.contains("其他")){
					dataUser_other = "1";
					String[] other = dataUse.split(",");
					for(int i = 0;i<other.length;i++){
						if(other[i].contains("其他")){
							dataUse_Owrite = other[i].substring(2);
						}
					}
				}

			}

			//起始时间
			//String startTime = Util.null2String(rds.getString("STARTING_TIME"));
			//指定机房
			String engineRoom = Util.null2String(rds.getString("ENGINE_ROOM"));
			String XXRoom600="";
			String XXRoom77="";
			String XXRoom801="";
			String JSRoom1="";
			String JSRoom801="";
			String JSRoom77="";
			String ZJRoom600="";
			String ZJRoom500="";
			String otherRoom="";
			String otherRoomWriter="";
			//将json字符串转化为list对象
			List<EngineRoom> engineRoomList =new ArrayList<EngineRoom>();
			//json格式转为list格式
			if(""!=engineRoom){
				engineRoomList = JSON.parseArray(engineRoom, EngineRoom.class);  
				for(int i=0;i<engineRoomList.size();i++){
					String engine_name =engineRoomList.get(i).getEngine_name();
					if(engineRoomList.get(i).getType().equals("zy")){
						otherRoom="1";
						otherRoomWriter=engine_name.substring(engine_name.indexOf("("), engine_name.length()-1);
					}else{
						if(engine_name.contains("上证信息")){
							if(engine_name.contains("600")){
								XXRoom600="1";
							}else if(engine_name.contains("77")){
								XXRoom77="1";
							}else if(engine_name.contains("801")){
								XXRoom801="1";
							}
						}else if(engine_name.contains("技术")){
							if(engine_name.contains("华京路1号")){
								JSRoom1="1";
							}else if(engine_name.contains("宁桥路801号")){
								JSRoom801="1";
							}else if(engine_name.contains("泰谷路77号")){
								JSRoom77="1";
							}
						}else if(engine_name.contains("中金所")){
							if(engine_name.contains("600")){
								ZJRoom600="1";
							}else if(engine_name.contains("500")){
								ZJRoom500="1";
							}
						}
					}
				}
			}
                        
			//系统名称
			/*String systemName = Util.null2String(rds.getString("SYSTEM_NAME"));
			//开发厂商
			String manufacturer = Util.null2String(rds.getString("MANUFACTURER"));

			//接口联系人
			String 	interfaceLinkMan = Util.null2String(rds.getString("INTERFACE_LINKMAN"));
			//电话（接口）
			String 	interfaceTel = Util.null2String(rds.getString("INTERFACE_TEL"));
			//邮箱（接口）
			String 	interfaceEmail = Util.null2String(rds.getString("INTERFACE_EMAIL"));

			//运维联系人
			String 	operationLinkMan = Util.null2String(rds.getString("OPERATION_LINKMAN"));
			//电话（运维）
			String operationTel	= Util.null2String(rds.getString("OPERATION_TEL"));
			//邮箱（运维）
			String operationEmail = Util.null2String(rds.getString("OPERATION_EMAIL"));

			//应急联系人
			String emergencyLinkMan	 = Util.null2String(rds.getString("EMERGENCY_LINKMAN"));
			//电话（应急）
			String emergencyTel	 = Util.null2String(rds.getString("EMERGENCY_TEL"));
			//邮箱（应急）
			String emergencyEmail = Util.null2String(rds.getString("EMERGENCY_EMAIL"));*/

			//转发数据规模
			String scale = Util.null2String(rds.getString("SCALE"));

			//申请类型
			String applyType = Util.null2String(rds.getString("APPLY_TYPE"));

			//转发用途
			String transmit = Util.null2String(rds.getString("TRANSMIT"));
            if(transmit!=""){
            	if(transmit.equals("1")){
            		transmit="0";    //机房内转发
            	}else{
            		transmit="1";    //接口转发
            	}
            }
			//客户类型
			String customerType = Util.null2String(rds.getString("CUSTOMER_TYPE"));
			String InAboutCustomer="";    //在约客户
			String notInAboutCustomer=""; //非在约客户
			String otherCustomer="";      //其他
			if(customerType!=""){
				if(customerType.contains("在约客户")){
					InAboutCustomer = "1";
				}
				if(customerType.contains("非在约客户")){
					notInAboutCustomer = "1";
				}
				if(customerType.contains("其他")){
					otherCustomer = "1";
				}
				
			}
			
			//机房类型
			String roomType = Util.null2String(rds.getString("ROOM_TYPE"));
			String permitRoom="";
			String newlyRoom="";
			String changeRoom="";
			String elseRoom="";
			if(roomType!=""){
				if(roomType.contains("已获许可机房")){
					permitRoom="1";
				}
				if(roomType.contains("新增机房")){
					newlyRoom="1";
				}
				if(roomType.contains("变更机房")){
					changeRoom="1";
				}
				if(roomType.contains("其他")){
					elseRoom="1";
				}
			}
			//系统类型
			String systemType = Util.null2String(rds.getString("SYSTEM_TYPE"));
			String permitSystem="";
			String newlySystem="";
			String changeSystem="";
			String otherSystem="";
			if(systemType!=""){
				if(systemType.contains("已获许可系统")){
					permitSystem="1";
				}
				if(systemType.contains("新增系统")){
					newlySystem="1";
				}
				if(systemType.contains("变更系统")){
					changeSystem="1";
				}
				if(systemType.contains("其他")){
					otherSystem="1";
				}
			}
			//VDE类型
			String vdeType = Util.null2String(rds.getString("VDE_TYPE"));
			String newlyVDE="";
			String multiplexVDE="";
			String changeVDE="";
			String otherVDE	="";
			if(vdeType!=""){
				if(vdeType.contains("新增VDE")){
					newlyVDE="1";
				}
				if(vdeType.contains("复用原有VDE")){
					multiplexVDE="1";
				}
				if(vdeType.contains("变更VDE地址")){
					changeVDE="1";
				}
				if(vdeType.contains("其他")){
					otherVDE="1";
				}
			}
			//备注
			String remarks = Util.null2String(rds.getString("REMARKS"));
			
			//验证客户是否存在
			RecordSet rs = new RecordSet();
			RecordSet rs1 = new RecordSet();			
			String customerid="";//客户id
			String cusbusinessid="";//客户业务id
			String businessid="6";//level2行情非展示许可业务
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
			try {//没进入方法
				int requestid = createWF(syid,companyId,companyName, businessLinkMan,businessTel,email,fax,
						dataUse_xtyh,dataUse_xtkf,dataUser_other,dataUse_Owrite,dataUse_qxzs,scale,applyType,
						transmit,customerid,remarks,XXRoom600,XXRoom77,XXRoom801,
						JSRoom1,JSRoom801,JSRoom77,ZJRoom600,ZJRoom500,otherRoom,otherRoomWriter,
						InAboutCustomer, notInAboutCustomer, otherCustomer,
						permitRoom, newlyRoom, changeRoom, elseRoom,
						permitSystem, newlySystem, changeSystem, otherSystem,
						newlyVDE, multiplexVDE, changeVDE, otherVDE);
				if(requestid > 0){
					
					SimpleDateFormat dateFormat_now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String createDate = dateFormat_now.format(new Date());
					String sqlupdate = "update SYNC_WEBSITE_LEVEL2_DATAFEED set STATUS=1,STEP2_TIME=to_date('"+createDate+"','yyyy-mm-dd hh24:mi:ss')  where ID='"+syid+"'";
					rds.executeSql(sqlupdate);
					lg.writeLog("=================更新中间库sql===="+sqlupdate);
					lg.writeLog("level2非展示数据申请流程触发工作流成功，流程requestid:"+requestid);
					/*RecordSet rs5 = new RecordSet();
					if(""!=engineRoom){
						rs5.executeSql("select * from  formtable_main_99 where requestId = "+requestid);
						while(rs5.next()){
							String mainId = Util.null2String(rs5.getString("id"));
							for(int i=0;i<engineRoomList.size();i++){
								String engine_name =engineRoomList.get(i).getEngine_name();
								String type ="1";
								if(engineRoomList.get(i).getType().equals("zy")){
									//engine_name = "(自有机房)" + engine_name;
									type ="0";
								}*/
								/*RecordSet rss = new RecordSet();
								//查询机房模块是否已存在
								rss.executeSql("select * from uf_lv2Feed_engine where engine_name = '"+engine_name+"'");
								//如果模块表没则插入模块表
								if(rss.getCounts()<1){
									int formmodeid=34;//模块id
									int modedatacreater=112;
									int modedatacreatertype=0;
									String modedatacreatedate =syndate;
									String modedatacreatetime =Util.add0(todaycal.get(Calendar.HOUR_OF_DAY), 2) + ":" + Util.add0(todaycal.get(Calendar.MINUTE) , 2)+ ":" + Util.add0(todaycal.get(Calendar.SECOND) , 2) ;
									String sqll ="insert into uf_lv2Feed_engine (engine_name,engineType ,formmodeid,modedatacreater,  modedatacreatertype ,  modedatacreatedate , modedatacreatetime) "
											+ " VALUES "
											+ " ('"+engine_name+"' , '"+type+"'"
													+ " , "+formmodeid+" , "+modedatacreater+" , "+modedatacreatertype+" "
															+ ", '"+modedatacreatedate+"', '"+modedatacreatetime+"') ";
									RecordSet rsss = new RecordSet();
									rsss.executeSql(sqll);
									lg.writeLog("-----------插入模块表-----"+sqll);
								}*/

								/*RecordSet rdd= new RecordSet();
								rdd.executeSql("select * from uf_lv2Feed_engine where engine_name = '"+engine_name+"'");
								//while(rdd.next()){
									//String engineId = Util.null2String(rdd.getString("id"));	
									RecordSet rs2 = new RecordSet();
									rs2.executeSql("insert into formtable_main_99_dt1  (mainid,engineName,xtbswz,money ) values ('"+mainId+"','"+engine_name+"','"+engineRoomList.get(i).getSystem_position()+"',"+200000+")");
									lg.writeLog("-----------插入明细表-----"+"insert into formtable_main_99_dt1  (mainid,engine_name,xtbswz,type ) values ('"+mainId+"','"+engine_name+"','"+engineRoomList.get(i).getSystem_position()+"')");
								}
							}
						}*/
				}
			} catch (Exception e) {
				e.printStackTrace();
				lg.writeLog("level2非展示数据申请流程触发出错："+e);
			}	
		}
		lg.writeLog("level2非展示数据申请流程触发结束");
	}

	/**
	 * 创建level2非展示数据申请流程
	 */
	public int createWF(String syid,String companyId,String companyName,String businessLinkMan,
			String businessTel,String email,String fax,String dataUse_xtyh,String dataUse_xtkf,
			String dataUser_other,String dataUse_Owrite,String dataUse_qxzs,
			String scale,String applyType,String transmit,String customerid,String remarks,
			String XXRoom600,String XXRoom77,String XXRoom801,String JSRoom1,String JSRoom801,
			String JSRoom77,String ZJRoom600,String ZJRoom500,String otherRoom,String otherRoomWriter,
			String InAboutCustomer,String notInAboutCustomer,String otherCustomer,
			String permitRoom,String newlyRoom,String changeRoom,String elseRoom,
			String permitSystem,String newlySystem,String changeSystem,String otherSystem,
			String newlyVDE,String multiplexVDE,String changeVDE,String otherVDE
			){
		String newrequestid = "";
		try{
			String workflowid = "476";
			String lcbt = "";
			if(applyType.equals("1")){//自用
				workflowid = "476";
				lcbt = "LEVEL-2非展示数据自用申请";// 流程标题  
			}else{//转发
				workflowid = "477";
				lcbt = "LEVEL-2非展示数据转发申请";// 流程标题  
			}

			String transmit_flow = "";
			if (transmit.equals("1")){
				transmit_flow = "0";
			}else if(transmit.equals("2")){
				transmit_flow = "1";
			}else{
				
			}
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

			// 主表字段开始,syid,companyId,companyName, businessLinkMan,
			if(syid !=""){
				field = new Property();
				field.setName("syid");
				field.setValue(syid);
				fields.add(field);
			}
			if(applyType !=""){
				field = new Property();
				field.setName("applyType");
				field.setValue(applyType);
				fields.add(field);
			}
			
			/*if(companyId !=""){
				field = new Property();
				field.setName("companyId");
				field.setValue(companyId);
				fields.add(field);
			}*/
			if(companyName !=""){
				field = new Property();
				field.setName("companyName");
				field.setValue(companyName);
				fields.add(field);
			}
			
			if(businessLinkMan !=""){
				field = new Property();
				field.setName("businessLinkMan");
				field.setValue(businessLinkMan);
				fields.add(field);
			}
			//businessTel,email,
			//fax,address, zipCode,
			if(businessTel !=""){
				field = new Property();
				field.setName("businessTel");
				field.setValue(businessTel);
				fields.add(field);
			}
			if(email !=""){
				field = new Property();
				field.setName("email");
				field.setValue(email);
				fields.add(field);
			}
			//7,gswz,glfzr,fzrdh,ywlxr,lxrdh,gscz,
			if(fax !=""){
				field = new Property();
				field.setName("fax");
				field.setValue(fax);
				fields.add(field);
			}
			//dataUse_lhpt,dataUser_other,dataUse_Owrite,dataUse_qxzs,
			if(dataUse_xtkf !=""){
				field = new Property();
				field.setName("SystemCreate");
				field.setValue(dataUse_xtkf);
				fields.add(field);
			}
			if(dataUse_xtyh !=""){
				field = new Property();
				field.setName("VIPSystem");
				field.setValue(dataUse_xtyh);
				fields.add(field);
			}
			if(dataUser_other !=""){
				field = new Property();
				field.setName("dataUser_other");
				field.setValue(dataUser_other);
				fields.add(field);
			}
			if(dataUse_Owrite !=""){
				field = new Property();
				field.setName("dataUse_Owrite");
				field.setValue(dataUse_Owrite);
				fields.add(field);
			}
			//13,Email,bgdz,jsfzr,fzrdh1,sjkfwq,gsbj
			if(dataUse_qxzs !=""){
				field = new Property();
				field.setName("dataUse_qxzs");
				field.setValue(dataUse_qxzs);
				fields.add(field);
			}

			if(scale !=""){
				field = new Property();
				field.setName("scale");
				field.setValue(scale);
				fields.add(field);
			}
			if(transmit !=""){
				field = new Property();
				field.setName("transmit");
				field.setValue(transmit_flow);
				fields.add(field);
			}
			if(customerid !=""){
				field = new Property();
				field.setName("customer");
				field.setValue(customerid);
				fields.add(field);
			}
			
			
			if(remarks !=""){
				field = new Property();
				field.setName("remarks");
				field.setValue(remarks);
				fields.add(field);
			}
			
			if(XXRoom600 !=""){
				field = new Property();
				field.setName("XXRoom600");
				field.setValue(XXRoom600);
				fields.add(field);
			}
			
			if(XXRoom77 !=""){
				field = new Property();
				field.setName("XXRoom77");
				field.setValue(XXRoom77);
				fields.add(field);
			}
			
			if(XXRoom801 !=""){
				field = new Property();
				field.setName("XXRoom801");
				field.setValue(XXRoom801);
				fields.add(field);
			}
			
			if(JSRoom1 !=""){
				field = new Property();
				field.setName("JSRoom1");
				field.setValue(JSRoom1);
				fields.add(field);
			}
			
			if(JSRoom801 !=""){
				field = new Property();
				field.setName("JSRoom801");
				field.setValue(JSRoom801);
				fields.add(field);
			}
			
			if(JSRoom77 !=""){
				field = new Property();
				field.setName("JSRoom77");
				field.setValue(JSRoom77);
				fields.add(field);
			}
			
			if(ZJRoom600 !=""){
				field = new Property();
				field.setName("ZJRoom600");
				field.setValue(ZJRoom600);
				fields.add(field);
			}
			
			if(ZJRoom500 !=""){
				field = new Property();
				field.setName("ZJRoom500");
				field.setValue(ZJRoom500);
				fields.add(field);
			}
			
			if(otherRoom !=""){
				field = new Property();
				field.setName("otherRoom");
				field.setValue(otherRoom);
				fields.add(field);
			}
			
			if(otherRoomWriter !=""){
				field = new Property();
				field.setName("otherRoomWriter");
				field.setValue(otherRoomWriter);
				fields.add(field);
			}		
			
			if(InAboutCustomer !=""){
				field = new Property();
				field.setName("InAboutCustomer");
				field.setValue(InAboutCustomer);
				fields.add(field);
			}
			
			if(notInAboutCustomer !=""){
				field = new Property();
				field.setName("notInAboutCustomer");
				field.setValue(notInAboutCustomer);
				fields.add(field);
			}
			
			if(otherCustomer !=""){
				field = new Property();
				field.setName("otherCustomer");
				field.setValue(otherCustomer);
				fields.add(field);
			}
			
			if(permitRoom !=""){
				field = new Property();
				field.setName("permitRoom");
				field.setValue(permitRoom);
				fields.add(field);
			}
			
			if(newlyRoom !=""){
				field = new Property();
				field.setName("newlyRoom");
				field.setValue(newlyRoom);
				fields.add(field);
			}
			
			if(changeRoom !=""){
				field = new Property();
				field.setName("changeRoom");
				field.setValue(changeRoom);
				fields.add(field);
			}
			
			if(elseRoom !=""){
				field = new Property();
				field.setName("elseRoom");
				field.setValue(elseRoom);
				fields.add(field);
			}
			
			if(permitSystem !=""){
				field = new Property();
				field.setName("permitSystem");
				field.setValue(permitSystem);
				fields.add(field);
			}
			
			if(newlySystem !=""){
				field = new Property();
				field.setName("newlySystem");
				field.setValue(newlySystem);
				fields.add(field);
			}
			
			if(changeSystem !=""){
				field = new Property();
				field.setName("changeSystem");
				field.setValue(changeSystem);
				fields.add(field);
			}
			
			if(otherSystem !=""){
				field = new Property();
				field.setName("otherSystem");
				field.setValue(otherSystem);
				fields.add(field);
			}
			
			if(newlyVDE !=""){
				field = new Property();
				field.setName("newlyVDE");
				field.setValue(newlyVDE);
				fields.add(field);
			}
			
			if(multiplexVDE !=""){
				field = new Property();
				field.setName("multiplexVDE");
				field.setValue(multiplexVDE);
				fields.add(field);
			}
			
			if(changeVDE !=""){
				field = new Property();
				field.setName("changeVDE");
				field.setValue(changeVDE);
				fields.add(field);
			}
			
			if(otherVDE !=""){
				field = new Property();
				field.setName("otherVDE");
				field.setValue(otherVDE);
				fields.add(field);
			}
			//上游附件,根据companyId来查询
			RecordSetDataSource fjss = new RecordSetDataSource("exchangeDB");
			String sqlfj="select * from SYNC_ATTACH where info_id ='"+syid+"'";
			fjss.executeSql(sqlfj);
			String docids = "";
			while(fjss.next()){
				String id= Util.null2String(fjss.getString("id"));//附件id
				String NRBT= Util.null2String(fjss.getString("ATTACH_NAME"));//附件名称
				lg.writeLog("------------NRBT-------------"+NRBT);
				int aa = sc.getContent(id,"112",NRBT);
				if(aa != 0){
					docids = docids+","+aa;
				}
			}
			if(docids == null ||docids.length()<=0){

			}else{
				docids = docids.substring(1);
			}
			lg.writeLog("------------docids-------------"+docids);
			field = new Property();
			field.setName("files");
			field.setValue(docids);
			fields.add(field);
			/*
			//明细表 formtable_main_99_dt1
			DetailTableInfo  detailTableInfo = new  DetailTableInfo();
			//List<DetailTable> detailFields = new ArrayList<DetailTable>();
			DetailTable detail= new DetailTable();
			//DetailTable[] detailtable = new DetailTable[engineRoomList.size()]; // 获取所有明细表

			Row[] rows = new Row[engineRoomList.size()];

			for(int i = 0;i<rows.length;i++){
				Row row =rows[i];
				Cell cell = null;
				RecordSet rs = new RecordSet();
				//查询机房模块是否已存在
				rs.executeSql("select * from uf_lv2Feed_engine where engine_name = '"+engineRoomList.get(i).getEngine_name()+"'");
				//如果存在则查询到id联通插入明细表
				lg.writeLog("第一次select * from uf_lv2Feed_engine where engine_name = '"+engineRoomList.get(i).getEngine_name()+"'");
				String engine_name =engineRoomList.get(i).getEngine_name();
				if(engineRoomList.get(i).getType().equals("zy")){
					engine_name = "(自有机房)" + engine_name;
				}
				if(rs.getCounts()>0){
					lg.writeLog("---------进入rs.getCounts()>0");
					while(rs.next()){
						//机房id 
						String engineId = Util.null2String(rs.getString("ID"));	

						cell =  new Cell();
						cell.setName("id");
						cell.setValue(engineId);
						row.addCell(cell);

						cell =  new Cell();
						cell.setName("engine_name");
						cell.setValue(engine_name);
						row.addCell(cell);

						cell =  new Cell();
						cell.setName("xtbswz");
						cell.setValue(engineRoomList.get(i).getSystem_position());
						rows[i]=row;
					}
				}else{//如果没有则先插入到模块表里再插入到明细表
					lg.writeLog("---------进入rs.getCounts() < 0----------");
					RecordSet rs2 = new RecordSet();

					String sql ="insert into uf_lv2Feed_engine (engine_name,engineType ) "
							+ " VALUES "
							+ " ('"+engine_name+"' , '"+engineRoomList.get(i).getType()+"' ) ";
					lg.writeLog(sql);
					rs2.executeSql(sql);
					RecordSet rs3 = new RecordSet();
					rs3.executeSql("select * from uf_lv2Feed_engine where engine_name = '"+engineRoomList.get(i).getEngine_name()+"'");
					lg.writeLog("第二次select * from uf_lv2Feed_engine where engine_name = '"+engineRoomList.get(i).getEngine_name()+"'");

					while(rs3.next()){
						//机房id 
						String engineId = Util.null2String(rs3.getString("ID"));	
						cell =  new Cell();
						cell.setName("id");
						cell.setValue(engineId);
						row.addCell(cell);

						cell =  new Cell();
						cell.setName("engine_name");
						cell.setValue(engine_name);
						row.addCell(cell);

						cell =  new Cell();
						cell.setName("xtbswz");
						cell.setValue(engineRoomList.get(i).getSystem_position());
						rows[i]=row;
					}
				}


			}*/

			Property[] fieldarray = (Property[]) fields.toArray(new Property[fields.size()]);
			mainTableInfo.setProperty(fieldarray);
			requestInfo.setMainTableInfo(mainTableInfo);

			/*detail.setRow(rows);
			detailTableInfo.addDetailTable(detail);
			requestInfo.setDetailTableInfo(detailTableInfo);;*/
			newrequestid = requestService.createRequest(requestInfo);
			lg.writeLog("....方法结束.........");
		}catch(Exception e){
			e.printStackTrace();
			lg.writeLog("level2非展示数据申请流程触发出错："+e);
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
			String lcbt = "创建客户业务卡片";// 流程标题   
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