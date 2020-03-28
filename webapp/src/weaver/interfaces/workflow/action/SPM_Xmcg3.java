package weaver.interfaces.workflow.action;

import java.util.UUID;

import weaver.conn.RecordSet;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;

public class SPM_Xmcg3 extends BaseBean
  implements Action
{
  public String execute(RequestInfo request)
  {
    try
    {
      writeLog("采购流程生成供应商合作数据--开始");
      RecordSet rs = new RecordSet();
      RecordSet rs1 = new RecordSet();
      RecordSet rs2 = new RecordSet();      
      int modeId=72; //建模id
      String billid = request.getRequestid();
      String ids ="";
      String suppliers ="";
      String xmmc ="";
      String sqrq ="";
      String sqr ="";
      String sqrbm ="";
      String sql="select id,supplier,(select xmmc from uf_xmkp where id=formtable_main_120.xmmc) as xmmc,sqrq,sqr,sqrbm from formtable_main_120  where requestid="+billid;
      rs.executeSql(sql);
      if (rs.next()) {
    	  ids = Util.null2String(rs.getString("id"));
          suppliers = Util.null2String(rs.getString("supplier"));
          xmmc = Util.null2String(rs.getString("xmmc"));
          sqrq = Util.null2String(rs.getString("sqrq"));
          sqr = Util.null2String(rs.getString("sqr"));
          sqrbm = Util.null2String(rs.getString("sqrbm"));
          String[] supplier = suppliers.split(",");
          
          for (int i = 0; i < supplier.length; i++) {
           int flag=0;
           String sfsupplier="";//是否本次采购供应商
           sql="select count(1) as flag from formtable_main_120_dt1  where mainid='"+ids+"' and (supplier='"+supplier[i]+"' or agentsupplier='"+supplier[i]+"')";
           rs1.executeSql(sql);
           if (rs1.next()) {
         	  flag =Integer.parseInt(Util.null2String(rs1.getString("flag")));          
           }
           if(flag==0){
        	   sfsupplier="1";
           }else{
        	   sfsupplier="0";
           }
           
       	   UUID uuid = UUID.randomUUID();
      	   String  wyid =uuid.toString();//生成唯一的标识码         	
      	
      	   sql="insert into uf_spm_comparison(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
      	+"supplier,createdate,applyflow,projectname,sfsupplier,creator,dept,uuid) values ("+modeId+",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"
      	+"'"+supplier[i]+"','"+sqrq+"','"+billid+"','"+xmmc+"','"+sfsupplier+"','"+sqr+"','"+sqrbm+"','"+wyid+"')";
      	rs1.executeSql(sql);      	
      	sql="select id from uf_spm_comparison where uuid='"+wyid+"'";
      	rs2.executeSql(sql);
        if (rs2.next()){
  			String id = Util.null2String(rs2.getString("id"));//查询合同id
  			int htid=Integer.parseInt(id);  			
  			ModeRightInfo ModeRightInfo = new ModeRightInfo();
  			ModeRightInfo.editModeDataShare(5,modeId,htid);//新建的时候添加共享-所有人 			
 	        
  		   }           
          }
          writeLog("采购流程生成供应商合作数据--成功");
       }      
    }
    catch (Exception e) {
      writeLog("采购流程生成供应商合作数据--出错" + e);
      return "0";
    }
    return "1";
  }
}