package weaver.interfaces.workflow.action;
import java.util.UUID;

import weaver.conn.RecordSet;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;

public class CRM_CustomerAdd extends BaseBean
implements Action
{
public String execute(RequestInfo request)
{
  try
  {
    writeLog("客户创建--开始");
    RecordSet rs = new RecordSet();
    RecordSet rs1 = new RecordSet();
    RecordSet rs2 = new RecordSet();
    RecordSet rs3 = new RecordSet();
    RecordSet rs4 = new RecordSet();
    RecordSet rs5 = new RecordSet();
    RecordSet rs6 = new RecordSet();
    int modeId=98; //建模id    
    String billid = request.getRequestid();
    String sql="";
    sql="select newcustomer,name,creditcode,createdate,newcustomer,sourcetype,oldname from uf_crm_custinfoadd where requestid="+billid;
    rs.executeSql(sql);
    if (rs.next()) {     
    	String  newcustomer = Util.null2String(rs.getString("newcustomer"));//是否新增客户	
      	String  name = Util.null2String(rs.getString("name"));//客户名称
      	String  credit = Util.null2String(rs.getString("creditcode"));//统一社会信用代码
      	String creditcode=credit.replace(" ", "");
      	String  createdate = Util.null2String(rs.getString("createdate"));//客户创建日期
      	String  sourcetype = Util.null2String(rs.getString("sourcetype"));//境内境外
      	String  oldname = Util.null2String(rs.getString("oldname"));//原客户名称
      	UUID uuid = UUID.randomUUID();
      	String  wyid =uuid.toString();//生成唯一的标识码     	
      	if(newcustomer.equals("0")){//新增客户
          	//插入客户
          	sql="insert into uf_crm_customerinfo(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
          	+"name,creditcode,sourcetype,createdate,oldname,uuid) values ("+modeId+",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"
          	+"'"+name+"','"+creditcode+"','"+sourcetype+"','"+createdate+"','"+oldname+"','"+wyid+"')";
          	rs1.executeSql(sql);      	
          	sql="select id from uf_crm_customerinfo where uuid='"+wyid+"'";
          	rs2.executeSql(sql);
            if (rs2.next()){
      			String id = Util.null2String(rs2.getString("id"));//查询出客户id      			
      			//将客户id反写进流程中
      			sql="update uf_crm_custinfoadd set customer='"+id+"' where requestid="+billid;
      			rs4.executeSql(sql);      			
      			
      			int logid=Integer.parseInt(id);  			
      			ModeRightInfo ModeRightInfo = new ModeRightInfo();  			
      			sql="select resourceid from hrmrolemembers where roleid=2 union all select id from hrmresource where departmentid=3";
      			rs3.executeSql(sql);
      			while (rs3.next()) {
      		       int resourceid=Integer.parseInt(Util.null2String(rs3.getString("resourceid")));//赋权人  	
      		       ModeRightInfo.editModeDataShare(resourceid,modeId,logid);//新建的时候添加共享
      			}  	        
      		 }
      	}else{
      	    //更新客户名称
  			sql="update uf_crm_customerinfo set name='"+name+"',oldname='"+oldname+"' where creditcode='"+creditcode+"'";
  			rs5.executeSql(sql);
  		    //将客户id反写进流程中
  			sql="update uf_crm_custinfoadd set customer=(select id from uf_crm_customerinfo where creditcode='"+creditcode+"') where requestid="+billid;
  			rs6.executeSql(sql);
      	}      
      writeLog("客户创建--结束");
    }    
  }
  catch (Exception e) {
    writeLog("客户创建--出错" + e);
    return "0";
  }
  return "1";
 }
}