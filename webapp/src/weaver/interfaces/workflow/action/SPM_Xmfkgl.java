package weaver.interfaces.workflow.action;

import java.util.UUID;

import weaver.conn.RecordSet;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;

public class SPM_Xmfkgl extends BaseBean
  implements Action
{
  public String execute(RequestInfo request)
  {
    try
    {
      writeLog("管理项目付款流程生成供应商付款数据--开始");
      RecordSet rs = new RecordSet();
      RecordSet rs1 = new RecordSet();
      RecordSet rs2 = new RecordSet();      
      int modeId=74; //建模id
      String billid = request.getRequestid();
      String supplier ="";
      String sqr ="";
      String sqrbm ="";
      String fkje ="";      
      String htmc ="";
      String htids ="";
      String paytitle ="";
      String sql="select a.sqr,a.bm,b.id,a.htdf,c.htmc,a.htmc as htid,d.fkje,(select selectname from workflow_SelectItem where selectvalue=d.fkjh and fieldid='9859') as paytitle from formtable_main_139 a join uf_spm_supplier b on a.htdf=b.suppliername join formtable_main_142 c on a.htmc=c.id left join formtable_main_139_dt1 d on a.id=d.mainid where c.sqlx='0' and a.requestid="+billid;
      rs.executeSql(sql);
      while (rs.next()) {
    	  supplier = Util.null2String(rs.getString("id"));
    	  sqr = Util.null2String(rs.getString("sqr"));
    	  sqrbm = Util.null2String(rs.getString("bm"));
    	  fkje = Util.null2String(rs.getString("fkje"));
    	  htmc = Util.null2String(rs.getString("htmc"));
    	  htids = Util.null2String(rs.getString("htid"));
    	  paytitle = Util.null2String(rs.getString("paytitle"));
    	  
          UUID uuid = UUID.randomUUID();
          String  wyid =uuid.toString();//生成唯一的标识码         	
          	
          	sql="insert into uf_spm_payinfo(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
          	+"supplier,createdate,creator,dept,payflow,paycontent,money,paydate,paytitle,glxmhtid,uuid) values ("+modeId+",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"
          	+"'"+supplier+"',convert(varchar(10),getdate(),120),'"+sqr+"','"+sqrbm+"','"+billid+"','"+htmc+"','"+fkje+"',convert(varchar(10),getdate(),120),'"+paytitle+"','"+htids+"','"+wyid+"')";
          	rs1.executeSql(sql);      	
          	sql="select id from uf_spm_payinfo where uuid='"+wyid+"'";
          	rs2.executeSql(sql);
            if (rs2.next()){
      			String id = Util.null2String(rs2.getString("id"));//查询合同id
      			int htid=Integer.parseInt(id);  			
      			ModeRightInfo ModeRightInfo = new ModeRightInfo();
      			ModeRightInfo.editModeDataShare(1,modeId,htid);//新建的时候添加共享-人员
      			ModeRightInfo.editModeDataShare(4,modeId,htid);//新建的时候添加共享-角色  	        
      		 }         
          writeLog("管理项目付款流程生成供应商付款数据--成功");
       }      
    }
    catch (Exception e) {
      writeLog("管理项目付款流程生成供应商付款数据--出错" + e);
      return "0";
    }
    return "1";
  }
}