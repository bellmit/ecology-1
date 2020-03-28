package weaver.interfaces.workflow.action;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;

public class AM_WxzcNumber extends BaseBean
  implements Action
{
  public String execute(RequestInfo request)
  {
    try
    {
      writeLog("无形资产入库，生成合同流水号--开始");
      RecordSet rs = new RecordSet();
      RecordSet rs1 = new RecordSet();            
      String billid = request.getRequestid();      
      String htcode="";//入库流水号
      int htcount=0;//已入库流程数 
      String htcounts="";//已入库流程数
      String sql="select count(1)+711+1 as htcount from uf_am_immatstorage where applycode is not null and requestid!="+billid;
      rs.executeSql(sql);
      if (rs.next()) {
    	  htcounts=Util.null2String(rs.getString("htcount"));
    	  htcount =Integer.parseInt(Util.null2String(rs.getString("htcount")));
       }      
      if(htcount<10){
    	  htcode="00"+htcounts;
      }else if(htcount<100&&htcount>=10){
    	  htcode="0"+htcounts;
      }else{
    	  htcode=htcounts;
      }      
      sql="update uf_am_immatstorage set applycode='"+htcode+"',uuid=newid() where requestid="+billid;
      rs1.executeSql(sql);      
      writeLog("无形资产入库，生成合同流水号--成功");
    }
    catch (Exception e) {
      writeLog("无形资产入库，生成合同流水号--出错" + e);
      return "0";
    }
    return "1";
  }  
}