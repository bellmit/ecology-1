package weaver.interfaces.workflow.action;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;

public class SPM_Update extends BaseBean
  implements Action
{
  public String execute(RequestInfo request)
  {
    try
    {
      writeLog("供应商基本信息变更--开始");
      RecordSet rs = new RecordSet();
      String billid = request.getRequestid();           
      String mainsql="select * from uf_spm_supplierbg where requestid="+billid;
      rs.executeSql(mainsql);
      if (rs.next()) {
        	String  supplier = Util.null2String(rs.getString("supplier"));//供应商
        	String  suppliertypebg = Util.null2String(rs.getString("suppliertypebg"));//供应商类型
        	String  suppliertypesbg = Util.null2String(rs.getString("suppliertypesbg"));//供应商二级小类
        	String  corporatdelegatebbg = Util.null2String(rs.getString("corporatdelegatebbg"));//法定代表人
        	String  addressbg = Util.null2String(rs.getString("addressbg"));//单位地址
        	String  bgzipcode = Util.null2String(rs.getString("bgzipcode"));//邮编
        	String  bgbanknamebg = Util.null2String(rs.getString("bgbanknamebg"));//开户银行名称
        	String  accountsbg = Util.null2String(rs.getString("accountsbg"));//开户银行账号
        	String  telbg = Util.null2String(rs.getString("telbg"));//单位电话
        	String  faxbg = Util.null2String(rs.getString("faxbg"));//单位传真
        	String  linknamebg = Util.null2String(rs.getString("linknamebg"));//联系人        	
        	String  linktelbg = Util.null2String(rs.getString("linktelbg"));//联系电话
        	String  licensefilebg = Util.null2String(rs.getString("licensefilebg"));//营业执照扫描件
        	String  unitbackgroundbg = Util.null2String(rs.getString("unitbackgroundbg"));//单位背景及主要业绩
        	String  businessscopebg = Util.null2String(rs.getString("businessscopebg"));//经营范围或专业特长
        	
        	String updatesql="update uf_spm_supplier set ";        	
        	if(!suppliertypebg.equals("")){
        		updatesql +=" suppliertype='"+suppliertypebg+"',";        		
        	}
        	if(!suppliertypesbg.equals("")){
        		updatesql +=" suppliertypes='"+suppliertypesbg+"',";        		
        	}
        	if(!corporatdelegatebbg.equals("")){
        		updatesql +=" corporatdelegate='"+corporatdelegatebbg+"',";        		
        	}
        	if(!addressbg.equals("")){
        		updatesql +=" address='"+addressbg+"',";        		
        	}
        	if(!bgzipcode.equals("")){
        		updatesql +=" zipcode='"+bgzipcode+"',";        		
        	}
        	if(!bgbanknamebg.equals("")){
        		updatesql +=" bankname='"+bgbanknamebg+"',";        		
        	}
        	if(!accountsbg.equals("")){
        		updatesql +=" accounts='"+accountsbg+"',";        		
        	}
        	if(!telbg.equals("")){
        		updatesql +=" tel='"+telbg+"',";        		
        	}
        	if(!faxbg.equals("")){
        		updatesql +=" fax='"+faxbg+"',";        		
        	}
        	if(!linknamebg.equals("")){
        		updatesql +=" linkame='"+linknamebg+"',";        		
        	}
        	if(!linktelbg.equals("")){
        		updatesql +=" linktel='"+linktelbg+"',";        		
        	}
        	if(!licensefilebg.equals("")){
        		updatesql +=" licensefile='"+licensefilebg+"',";        		
        	}
        	if(!unitbackgroundbg.equals("")){
        		updatesql +=" unitbackground='"+unitbackgroundbg+"',";        		
        	}
        	if(!businessscopebg.equals("")){
        		updatesql +=" businessscope='"+businessscopebg+"',";        		
        	}
				
        	    updatesql=updatesql.substring(0,updatesql.length()-1);//去掉结尾处最后一个逗号
        	    updatesql+=" where id='"+supplier+"'";        	
                rs.executeSql(updatesql); 
                writeLog("供应商基本信息变更--成功");
        
       }      
    }
    catch (Exception e) {
      writeLog("供应商基本信息变更--出错" + e);
      return "0";
    }
    return "1";
  }
}