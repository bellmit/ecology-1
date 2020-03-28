package weaver.interfaces.workflow.action;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;

public class AM_GdzcAllocate extends BaseBean
  implements Action
{
  public String execute(RequestInfo request)
  {
    try
    {
      writeLog("固定资产调配--开始");
      RecordSet rs = new RecordSet();
      RecordSet rs1 = new RecordSet();        
      String billid = request.getRequestid(); 
      String assetname="";//资产  
      String managedept="";//调入部门  
      String userperson="";//使用人   
      String status="";//资产状态
      String address="";//设备所在位置
      String storecity="";//存放地点
      String applydate="";//领用日期
      String sql="select b.assetname,a.managedept,b.userperson,b.address,b.storecity,convert(varchar(10),getdate(),120) as applydate from uf_am_assetallocate a,uf_am_assetallocate_dt1 b where a.id=b.mainid and a.requestid="+billid;
      rs.executeSql(sql);
      while (rs.next()) {
    	  assetname=Util.null2String(rs.getString("assetname"));
    	  managedept=Util.null2String(rs.getString("managedept"));
    	  userperson=Util.null2String(rs.getString("userperson"));
    	  address=Util.null2String(rs.getString("address"));
    	  storecity=Util.null2String(rs.getString("storecity"));
    	  applydate=Util.null2String(rs.getString("applydate"));
    	  if(!userperson.equals("")){
    		  status="1";
    		  sql="update uf_am_fixedassets set usestatus='"+status+"',managedept='"+managedept+"',userperson='"+userperson+"',address='"+address+"',storecity='"+storecity+"',applydate='"+applydate+"' where id='"+assetname+"'";
        	  rs1.executeSql(sql);
    	  }else{
    		  status="2"; 
    		  applydate="";
    		  sql="update uf_am_fixedassets set usestatus='"+status+"',managedept='"+managedept+"',userperson='"+userperson+"',applydate='"+applydate+"' where id='"+assetname+"'";
        	  rs1.executeSql(sql);
    	  }
    	  
       }     
      writeLog("固定资产调配--成功");
    }
    catch (Exception e) {
      writeLog("固定资产调配--出错" + e);
      return "0";
    }
    return "1";
  }  
}