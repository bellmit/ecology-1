package weaver.interfaces.workflow.action;
import java.util.UUID;

import weaver.conn.RecordSet;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;

public class AM_ChangeLog1 extends BaseBean
implements Action
{
public String execute(RequestInfo request)
{
  try
  {
    writeLog("固定资产变更记录1--开始");
    RecordSet rs = new RecordSet();
    RecordSet rs1 = new RecordSet();
    RecordSet rs2 = new RecordSet();
    RecordSet rs3 = new RecordSet();
    int modeId=94; //建模id    
    String billid = request.getRequestid();
    String sql="";
    sql="select b.id,a.creator from uf_am_storage a,uf_am_fixedassets b where a.requestid=b.flowid and a.requestid="+billid;
    rs.executeSql(sql);
    while (rs.next()) {     
    	String  title="资产入库";//变更事项      	
      	String  zcid = Util.null2String(rs.getString("id"));//资产id
      	String  creator = Util.null2String(rs.getString("creator"));//流程申请人
      	UUID uuid = UUID.randomUUID();
      	String  wyid =uuid.toString();//生成唯一的标识码     	
      	
      	//插入变更记录
      	sql="insert into uf_am_assetslog(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
      	+"assetname,title,flowid,creator,createdate,uuid) values ("+modeId+",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"
      	+"'"+zcid+"','"+title+"','"+billid+"','"+creator+"',convert(varchar(10),getdate(),120),'"+wyid+"')";
      	rs1.executeSql(sql);      	
      	sql="select id from uf_am_assetslog where uuid='"+wyid+"'";
      	rs2.executeSql(sql);
        if (rs2.next()){
  			String id = Util.null2String(rs2.getString("id"));//查询出资产变更记录
  			int logid=Integer.parseInt(id);  			
  			ModeRightInfo ModeRightInfo = new ModeRightInfo();
  			ModeRightInfo.editModeDataShare(5,modeId,logid);//新建的时候添加共享-所有人  	        
  		 }              	
     
      writeLog("固定资产变更记录1--结束");
    }    
  }
  catch (Exception e) {
    writeLog("固定资产变更记录1--出错" + e);
    return "0";
  }
  return "1";
 }
}