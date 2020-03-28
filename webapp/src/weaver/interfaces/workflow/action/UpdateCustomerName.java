package weaver.interfaces.workflow.action;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;

//马坤鹏  2019.3.25
public class UpdateCustomerName extends BaseBean
implements Action
{
public String execute(RequestInfo request){
	try
	{
		writeLog("客户名称变更");
		RecordSet rs = new RecordSet();//连接数据库
		String name = Util.null2String(rs.getString("name"));//更改后的客户名字
		writeLog("name==="+name);
		String oldname = Util.null2String(rs.getString("oldname"));//更改前的客户名字
		writeLog("oldname==="+oldname);
		String oldnamelist = oldname+name;//就名字+新名字
		String id = Util.null2String(rs.getString("id"));//客户id
		writeLog("id==="+id);
		String sql="";
		sql="update uf_crm_customerinfo set name='"+name+"',oldname='"+oldnamelist+"' where id="+id+"";
	     rs.executeSql(sql);
	     
	}
	catch(Exception e){
		
		writeLog("客户姓名变更--出错");
		return "0";
	}
	return Action.SUCCESS;
	
}

}