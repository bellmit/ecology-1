package weaver.interfaces.workflow.action;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.soa.workflow.request.RequestInfo;

public class AM_WxzcNumberReturn extends BaseBean
  implements Action
{
  public String execute(RequestInfo request)
  {
    try
    {
      writeLog("无形资产入库编号反写--开始");
      RecordSet rs = new RecordSet();
      String billid = request.getRequestid();    
      
      String sql="update uf_am_immatstorage set assetno=b.assetno from uf_am_immatstorage a,uf_am_immatassets b where a.requestid=b.applyflowid and a.uuid=b.uuid and a.requestid="+billid;
      rs.executeSql(sql);      
      writeLog("无形资产入库编号反写--成功");
    }
    catch (Exception e) {
      writeLog("无形资产入库编号反写--出错" + e);
      return "0";
    }
    return "1";
  }  
}