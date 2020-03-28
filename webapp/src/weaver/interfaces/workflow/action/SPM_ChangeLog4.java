package weaver.interfaces.workflow.action;
import java.util.UUID;

import weaver.conn.RecordSet;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;

public class SPM_ChangeLog4 extends BaseBean
implements Action
{
public String execute(RequestInfo request)
{
  try
  {
    writeLog("供应商变更记录4--开始");
    RecordSet rs = new RecordSet();
    UUID uuid = UUID.randomUUID();
    int modeId=77; //建模id
    String billid = request.getRequestid();
    String sql="";
    sql="select a.creator,b.supplier from uf_spm_supplierdel a,uf_spm_supplierdel_dt1 b where a.id=b.mainid and a.requestid="+billid;
    rs.executeSql(sql);
    if (rs.next()) {
      
    	String  title="";//变更事项
      	String  supplier = Util.null2String(rs.getString("supplier"));//供应商      	
      	String  creator = Util.null2String(rs.getString("creator"));//流程申请人
      	String  wyid =uuid.toString();//生成唯一的标识码
      	title="出库";

      	//插入变更记录
      	sql="insert into uf_spm_supplierlog(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
      	+"supplier,title,flowid,creator,createdate,uuid) values ("+modeId+",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"
      	+"'"+supplier+"','"+title+"','"+billid+"','"+creator+"',convert(varchar(10),getdate(),120),'"+wyid+"')";
      	rs.executeSql(sql);      	
      	sql="select id from uf_spm_supplierlog where uuid='"+wyid+"'";
        rs.executeSql(sql);
        if (rs.next()) {
  			String id = Util.null2String(rs.getString("id"));//查询出供应商变更记录
  			int logid=Integer.parseInt(id);  			
  			ModeRightInfo ModeRightInfo = new ModeRightInfo();
  			ModeRightInfo.editModeDataShare(5,modeId,logid);//新建的时候添加共享-所有人 	        
  		 }              	
      }
      writeLog("供应商变更记录4--结束");
  }
  catch (Exception e) {
    writeLog("供应商变更记录4--出错" + e);
    return "0";
  }
  return "1";
 }
}