package weaver.interfaces.workflow.action;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import weaver.conn.RecordSet;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.MainTableInfo;
import weaver.soa.workflow.request.Property;
import weaver.soa.workflow.request.RequestInfo;
import weaver.soa.workflow.request.RequestService;

public class CRM_PermissOpen extends BaseBean
implements Action
{
	public String execute(RequestInfo request)
	{
		try
		{
			writeLog("权限开通--开始");
			RecordSet rs = new RecordSet();	
			RecordSet rs1 = new RecordSet();	
			
			String billid = request.getRequestid();
			String sql="";			
			sql="select business,customer,startdate,enddate,reminddate,opendate,openfile,content1,content2,content3,content4,content5,content6 from uf_crm_openpermiss  where requestid="+billid;
			rs.executeSql(sql);
			if (rs.next()) {      	
				String  business = Util.null2String(rs.getString("business"));//所属业务
				String  customer = Util.null2String(rs.getString("customer"));//客户名称
				String  startdate = Util.null2String(rs.getString("startdate"));//权限起始时间
				String  enddate = Util.null2String(rs.getString("enddate"));//权限终止时间
				String  reminddate = Util.null2String(rs.getString("reminddate"));//到期提醒时间
				String  opendate = Util.null2String(rs.getString("opendate"));//开通时间
				String  openfile = Util.null2String(rs.getString("openfile"));//开通相关文件
				String  content1 = Util.null2String(rs.getString("content1"));//权限1
				String  content2 = Util.null2String(rs.getString("content2"));//权限2
				String  content3 = Util.null2String(rs.getString("content3"));//权限3
				String  content4 = Util.null2String(rs.getString("content4"));//权限4
				String  content5 = Util.null2String(rs.getString("content5"));//权限5
				String  content6 = Util.null2String(rs.getString("content6"));//权限6
				//开始生成权限				
				if(business.equals("11")){//海外数据代理业务
					int requestidzi1 = createWF( business, customer, startdate, enddate, reminddate, opendate, openfile, content1, billid);
					int requestidzi2 = createWF( business, customer, startdate, enddate, reminddate, opendate, openfile, content2, billid);
					int requestidzi3 = createWF( business, customer, startdate, enddate, reminddate, opendate, openfile, content3, billid);
					int requestidzi4 = createWF( business, customer, startdate, enddate, reminddate, opendate, openfile, content4, billid);
					int requestidzi5 = createWF( business, customer, startdate, enddate, reminddate, opendate, openfile, content5, billid);
					int requestidzi6 = createWF( business, customer, startdate, enddate, reminddate, opendate, openfile, content6, billid);
				}else{
					int requestidzi1 = createWF( business, customer, startdate, enddate, reminddate, opendate, openfile, content1, billid);
					int requestidzi2 = createWF( business, customer, startdate, enddate, reminddate, opendate, openfile, content2, billid);
					int requestidzi3 = createWF( business, customer, startdate, enddate, reminddate, opendate, openfile, content3, billid);		
					
				}
				
				//更新客户业务卡片权限信息
				sql="update uf_crm_custbusiness set qxstatus='开通'  where customer='"+customer+"' and business='"+business+"'";
				rs1.executeSql(sql);
				
			}
			
			writeLog("权限开通--结束");
		}
		catch (Exception e) {
			writeLog("权限开通--出错" + e);
			return "0";
		}
		return "1";
	}
	/**
	 * 创建权限
	 */
	public int createWF(String business,String customer,String startdate,String enddate,String reminddate,String opendate,String openfile,String contentid,String billid){
		String newid = "";		
		try{
			writeLog("创建权限子--开始");
			int modeId=116; //建模id			
			RecordSet rs1 = new RecordSet();
			RecordSet rs2 = new RecordSet(); 
			RecordSet rs3 = new RecordSet(); 
			RecordSet rs4 = new RecordSet();
			RecordSet rs5 = new RecordSet();
			String sql="";
			
			//权限拆分
			String[] contents = contentid.split(",");	
			
			for (int i = 0; i < contents.length; i++) {	    		  
				if(!"".equals(contents[i])){  			
					String qxid="";	
					//查询权限是否存在
					sql="select id from uf_crm_permission where customer='"+customer+"' and business='"+business+"' and contentid='"+contents[i]+"' and status='0'";
					rs1.executeSql(sql);
					if (rs1.next()) {
						qxid=Util.null2String(rs1.getString("id"));
						//权限存在，则新增原权限状态为关闭
						if(!qxid.equals("")){
							sql="update uf_crm_permission set status='1' , closedate='"+opendate+"' where id='"+qxid+"'";	
							rs2.executeSql(sql);
						}
					}
					
					String contentname="";							
					sql="select content from uf_crm_permissinfo where id='"+contents[i]+"'";
					rs3.executeSql(sql);
					if (rs3.next()) {
						contentname=Util.null2String(rs3.getString("content"));
					}
					
					//写入新权限
					UUID uuid = UUID.randomUUID();
		  	      	String  wyid =uuid.toString();//生成唯一的标识码     		  	      	
		  	      	sql="insert into uf_crm_permission(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
		  	      	+"customer,business,startdate,enddate,reminddate,opendate,openfile," +
		  	      	" status,contentid,title,flowid,uuid) " +
		  	      	"select "+modeId+",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"+
		  	      	"'"+customer+"','"+business+"','"+startdate+"','"+enddate+"','"+reminddate+"','"+opendate+"','"+openfile+"', " +
		  	      	"'0','"+contents[i]+"','"+contentname+"','"+billid+"','"+wyid+"'";
		  	        writeLog("权限sql="+sql);
		  	      	rs4.executeSql(sql);      	
		  	      	sql="select id from uf_crm_permission where uuid='"+wyid+"'";
		  	      	rs5.executeSql(sql);
		  	        if (rs5.next()){
		  	  			String id = Util.null2String(rs5.getString("id"));//查询权限id
		  	  			int logid=Integer.parseInt(id);  			
		  	  			ModeRightInfo ModeRightInfo = new ModeRightInfo();
		  	  			ModeRightInfo.editModeDataShare(5,modeId,logid);//新建的时候添加共享-所有人
		  	        }					
				}
			}			
			writeLog("创建权限子--结束--");
		}catch(Exception e){
			writeLog("创建权限子--出错："+e);
		}
		return Util.getIntValue(newid,0);
	}
}