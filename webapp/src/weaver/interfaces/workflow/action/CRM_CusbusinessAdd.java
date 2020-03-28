package weaver.interfaces.workflow.action;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestService;
import weaver.soa.workflow.request.MainTableInfo;
import weaver.soa.workflow.request.Property;
import weaver.soa.workflow.request.RequestInfo;
import java.util.ArrayList;
import java.util.List;

public class CRM_CusbusinessAdd extends BaseBean
  implements Action
{
  public String execute(RequestInfo request)
  {
    try
    {
      writeLog("创建客户业务信息补充流程--开始");
      RecordSet rs = new RecordSet();
      RecordSet rs1 = new RecordSet();
      String billid = request.getRequestid();      
      String name="";//客户名称
      String credit="";//统一社会信用代码
      String business="";//所属业务
      String creator="";//流程创建人
      String dept="";//流程创建部门
      String createdate="";//流程创建日期
      String customerid="";//客户id
      String sql="";
      sql="select creator,dept,convert(varchar(10),getdate(),120) as createdate,name,creditcode,business from uf_crm_custinfoadd where requestid="+billid;
      rs.executeSql(sql);
      if (rs.next()) {
    	  name = Util.null2String(rs.getString("name")); 
    	  credit = Util.null2String(rs.getString("creditcode")); 
    	  business = Util.null2String(rs.getString("business")); 
    	  creator = Util.null2String(rs.getString("creator"));
    	  dept = Util.null2String(rs.getString("dept"));
    	  createdate = Util.null2String(rs.getString("createdate"));
       }  
      String creditcode=credit.replace(" ", "");
      sql="select id from uf_crm_customerinfo where name='"+name+"' and creditcode='"+creditcode+"'";
      rs1.executeSql(sql);
      if (rs1.next()) {
    	  customerid = Util.null2String(rs1.getString("id")); 
    	  
       }
        int requestidzi = createWF(customerid,business,creator,dept,createdate); 
        
     
      writeLog("创建客户业务信息补充流程--成功");
    }
    catch (Exception e) {
      writeLog("创建客户业务信息补充流程--出错" + e);
      return "0";
    }
    return "1";
  }
  
	/**
	 * 创建客户业务信息补充流程
	 */
	public int createWF(String customerid,String business,String creator,String dept,String createdate){
		String newrequestid = "";
		writeLog("creator="+creator+";dept="+dept+";createdate="+createdate);
		try{
			writeLog("创建客户业务信息补充流程子--开始");
			String workflowid = "383";//流程id
			String lcbt = "客户业务信息补充";// 流程标题   
			RequestService requestService = new RequestService();
			RequestInfo requestInfo = new RequestInfo();            
			requestInfo.setWorkflowid(workflowid);//流程类型id
			requestInfo.setCreatorid(creator);//创建人--龚婷		
			requestInfo.setDescription(lcbt);//设置流程标题 
			requestInfo.setRequestlevel("0");//0 正常，1重要，2紧急
			requestInfo.setIsNextFlow("0");//流转到下一节点
			
			MainTableInfo mainTableInfo = new MainTableInfo();
			List<Property> fields = new ArrayList<Property>();
			Property field = null;	
			
			field = new Property();
			field.setName("createdate");
			field.setValue(createdate);
			fields.add(field);
			
			field = new Property();
			field.setName("creator");
			field.setValue(creator);
			fields.add(field);
			
			field = new Property();
			field.setName("dept");
			field.setValue(dept);
			fields.add(field);
					
			field = new Property();
			field.setName("customer");
			field.setValue(customerid);
			fields.add(field);
			
			field = new Property();
			field.setName("business");
			field.setValue(business);
			fields.add(field);			
			
			Property[] fieldarray = (Property[]) fields.toArray(new Property[fields.size()]);
			mainTableInfo.setProperty(fieldarray);
			requestInfo.setMainTableInfo(mainTableInfo);			
			newrequestid = requestService.createRequest(requestInfo);			
			
			writeLog("创建客户业务信息补充流程子--结束--"+newrequestid);
		}catch(Exception e){
			writeLog("创建客户业务信息补充流程子--出错："+e);
		}
		return Util.getIntValue(newrequestid,0);
	}
  
}