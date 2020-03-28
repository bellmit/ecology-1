package weaver.interfaces.workflow.action;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;

public class CRM_FormContractAdd extends BaseBean
implements Action
{
public String execute(RequestInfo request)
{
  try
  {
    writeLog("创建格式合同--开始");
    RecordSet rs = new RecordSet();
    RecordSet rs1 = new RecordSet();
    RecordSet rs2 = new RecordSet(); 
    RecordSet rs3 = new RecordSet();     
    String billid = request.getRequestid();
    String sql="";
    sql="select type,originalcontract from uf_crm_formctradd where requestid="+billid;
    rs.executeSql(sql);
    if (rs.next()) {     
    	String  type = Util.null2String(rs.getString("type"));//类型	
      	String  originalcontract = Util.null2String(rs.getString("originalcontract"));//原格式合同
      	
     if(type.equals("1")){//更新
      	    //更新客户名称
  			sql="update uf_crm_formcontract set status='1' where id='"+originalcontract+"'";
  			rs1.executeSql(sql); 
  			//更新版本号
  			sql="update uf_crm_formcontract set version=(select Convert(decimal(18,1),cast(version as float)+1) from uf_crm_formcontract where id='"+originalcontract+"') where flowid="+billid;
  			rs2.executeSql(sql);		
  			
      	}else{//新增
      	    //更新版本号为1.0
  			sql="update uf_crm_formcontract set version='1.0' where flowid="+billid;
  			rs3.executeSql(sql);
      	}      
      writeLog("创建格式合同--结束");
    }    
  }
  catch (Exception e) {
    writeLog("创建格式合同--出错" + e);
    return "0";
  }
  return "1";
 }
}