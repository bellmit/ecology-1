package weaver.interfaces.workflow.action;

import java.util.UUID;

import weaver.conn.RecordSet;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;

public class SPM_Glcg2 extends BaseBean
  implements Action
{
  public String execute(RequestInfo request)
  {
    try
    {
      writeLog("管理采购流程生成供应商合同数据--开始");
      RecordSet rs = new RecordSet();
      RecordSet rs1 = new RecordSet();
      RecordSet rs2 = new RecordSet();
      RecordSet rs3 = new RecordSet();
      int modeId=73; //建模id
      String billid = request.getRequestid();
      String sqr ="";
      String sqrbm ="";
      String htmc ="";
      String je ="";
      String htqdrq ="";
      String htqsrq ="";
      String htjzrq ="";
      String htsmj ="";
      String supplier ="";
      String agentsupplier ="";
      String sql="select a.sqr,a.sqrbm,b.htmc,b.je,b.htqdrq,b.htqsrq,b.htjzrq,b.htsmj,b.supplier,b.agentsupplier from formtable_main_136 a,formtable_main_136_dt1 b where a.id=b.mainid and a.requestid="+billid;
      rs.executeSql(sql);
      while (rs.next()) {
    	  sqr = Util.null2String(rs.getString("sqr"));
    	  sqrbm = Util.null2String(rs.getString("sqrbm"));
    	  htmc = Util.null2String(rs.getString("htmc"));
    	  je = Util.null2String(rs.getString("je"));
    	  htqdrq = Util.null2String(rs.getString("htqdrq"));
    	  htqsrq = Util.null2String(rs.getString("htqsrq"));
    	  htjzrq = Util.null2String(rs.getString("htjzrq"));
    	  htsmj = Util.null2String(rs.getString("htsmj"));
    	  supplier = Util.null2String(rs.getString("supplier"));
    	  agentsupplier = Util.null2String(rs.getString("agentsupplier"));
    	  
    	  if(!agentsupplier.equals("")){
    		  supplier=agentsupplier; 
    	  }
    	  
        	UUID uuid = UUID.randomUUID();
          	String  wyid =uuid.toString();//生成唯一的标识码         	
          	
          	sql="insert into uf_spm_contract(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
          	+"supplier,createdate,creator,dept,contractflow,contractname,money,signdate,begindate,enddate,contractfile,uuid) values ("+modeId+",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"
          	+"'"+supplier+"',convert(varchar(10),getdate(),120),'"+sqr+"','"+sqrbm+"','"+billid+"','"+htmc+"','"+je+"','"+htqdrq+"','"+htqsrq+"','"+htjzrq+"','"+htsmj+"','"+wyid+"')";
          	rs1.executeSql(sql);      	
          	sql="select id from uf_spm_contract where uuid='"+wyid+"'";
          	rs2.executeSql(sql);
            if (rs2.next()){
      			String id = Util.null2String(rs2.getString("id"));//查询合同id
      			int htid=Integer.parseInt(id);  			
      			ModeRightInfo ModeRightInfo = new ModeRightInfo();
      			ModeRightInfo.editModeDataShare(1,modeId,htid);//新建的时候添加共享-人员
      			ModeRightInfo.editModeDataShare(4,modeId,htid);//新建的时候添加共享-角色 	        
      		 }         
          writeLog("管理采购流程生成供应商合同数据--成功");
       }      
    }
    catch (Exception e) {
      writeLog("管理采购流程生成供应商合同数据--出错" + e);
      return "0";
    }
    return "1";
  }
}