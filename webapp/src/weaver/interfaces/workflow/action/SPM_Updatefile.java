package weaver.interfaces.workflow.action;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;

public class SPM_Updatefile extends BaseBean
  implements Action
{
  public String execute(RequestInfo request)
  {
    try
    {
      writeLog("供应商基本信息变更营业执照赋值--开始");
      RecordSet rs = new RecordSet();
      String billid = request.getRequestid();           
      String mainsql="update uf_spm_supplierbg set licensefile=(select licensefile from uf_spm_supplier  where id=uf_spm_supplierbg.supplier) where requestid="+billid;
      rs.executeSql(mainsql);
      writeLog("供应商基本信息变更营业执照赋值--开始");
    }
    catch (Exception e) {
      writeLog("供应商基本信息变更营业执照赋值--出错" + e);
      return "0";
    }
    return "1";
  }
}