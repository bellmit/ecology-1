package weaver.sysinterface.am;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import weaver.conn.RecordSet;
import weaver.formmode.view.ModeShareManager;
import weaver.general.Util;
import weaver.hrm.User;

import com.weaver.formmodel.mobile.manager.MobileUserInit;
import com.weaver.formmodel.util.StringHelper;

public class amAction extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private HttpServletRequest request;
	private HttpServletResponse response;
	
	public void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException{
		this.request = request;
		this.response = response;
		try {
			String action = StringHelper.null2String(request.getParameter("action"));
			User user = MobileUserInit.getUser(request, response);
			if(user == null){
				JSONObject checkUser = new JSONObject();
				checkUser.put("msgstatus", "0");
				checkUser.put("msg", "服务器端重置了登录信息，请重新登录");
				response.setContentType("application/json; charset=utf-8");  
				response.getWriter().print(checkUser.toString());
				return;
			}else if("userid".equals(action)){		//判断是否是管理员				
				getUser(user);
			}else if("getmyGdzc".equals(action)){		//我的固定资产列表
				getmyGdzc(user);
			}else if("getmyWxzc".equals(action)){		//我的无形资产列表
				getmyWxzc(user);
			}else if("getGdzc".equals(action)){		//固定资产列表
				getGdzc(user);
			}else if("getWxzc".equals(action)){    //无形资产列表
				getWxzc(user);
			}else if("getYhp".equals(action)){    //消耗品列表
				getYhp(user);
			}else if("getSYYhp".equals(action)){  //消耗品库存预警列表
				getSYYhp(user);
			}else if("getDzyhp".equals(action)){  //低值易耗品列表
				getDzyhp(user);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	/**
	 * 资产管理-管理员判断接口
	 * @param 
	 * user	用户
	 * pageno	当前页数
	 * pagesize	每页显示数
	 */
	public void getUser(User user){
		String userid = String.valueOf(user.getUID());	//用户id
		RecordSet rs = new RecordSet();
		String sqlUser = "  select hrs.id from hrmrolemembers hrme left join hrmroles hrs on hrme.roleid = hrs.id " +
				" where (hrs.id = 148 or hrs.id = 149 or hrs.id = 150 or hrs.id = 155 or hrs.id = 2 ) and hrme.resourceid = '"+userid+"'"+
				" union all "+
				" select 156 from Matrixtable_3 where bmfzr='"+userid+"'";//矩阵中的部门负责人;
		rs.executeSql(sqlUser);
		try {
			response.setContentType("application/text; charset=utf-8");  
			if(rs.next()){
				response.getWriter().print(StringHelper.null2String(rs.getString("id")));
			}else {
				response.getWriter().print("null");
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	/**
	 * 资产管理-我的固定资产接口
	 * @param 
	 * user	用户
	 * pageno	当前页数
	 * pagesize	每页显示数
	 */
	private void getmyGdzc(User user) {
		
		String userid = String.valueOf(user.getUID());	//用户id		
		RecordSet rs = new RecordSet();
		int recordCount=0;//总记录数
		int pageno = Util.getIntValue(request.getParameter("pageno"),1);//当前页
		int pagesize = Util.getIntValue(request.getParameter("pagesize"),10);//每页显示数
		//权限过滤
		String rightSql =getShareSql(89,Integer.parseInt(userid));
		String selSql = "select count(1) from uf_am_fixedassets where 1=1 and usestatus=1 and userperson='"+userid+"' and exists (select 1 from "+rightSql+" m where m.sourceid=uf_am_fixedassets.id)";
		rs.executeSql(selSql);
		
		if(rs.next()){
			recordCount = rs.getInt(1);
		}
		int iNextNum = pageno * pagesize;//第几页的总数
		int ipageset = pagesize;//第几页的显示条数
		
		if((recordCount - iNextNum + pagesize)>0){
			if((recordCount - iNextNum + pagesize < pagesize)){
				ipageset = recordCount - iNextNum + pagesize;
			}else{
				ipageset = pagesize;
			}
		}else{
			ipageset = 0;
		}
		//取按时间倒序排序的数据的前iNextNum条倒序排序
		selSql = "select top " + iNextNum +" uaf.id,uaf.assetname,uaf.assetno, " +
				"( SELECT name FROM uf_am_assettype uaa WHERE  uaa.id = uaf.assettype1 ) AS assettype1, " +
				"( SELECT name FROM uf_am_assettype uaa WHERE  uaa.id = uaf.assettype2 ) AS assettype2, " +
				"( SELECT name FROM uf_am_assettype uaa WHERE  uaa.id = uaf.assettype3 ) AS assettype3, " +
				"( SELECT lastname FROM HrmResource n WHERE n.id= uaf.userperson ) AS creatorname, " +
				" modedatacreatedate,modedatacreatetime FROM uf_am_fixedassets uaf  where uaf.usestatus=1 and uaf.userperson='"+userid+"' ";		
		
		selSql += "and exists (select 1 from "+rightSql+" m where m.sourceid=uaf.id) order by modedatacreatedate desc,modedatacreatetime desc";
		//将上面得到的数据取按顺序排序的ipageset条数据顺序排序
		selSql = "select top " + ipageset +" t1.* from (" + selSql + ") t1 order by modedatacreatedate asc,modedatacreatetime asc";
		//将得到的数据倒序排序显示
		selSql = "select t2.* from (" + selSql + ") t2 order by modedatacreatedate desc,modedatacreatetime desc";
		
		JSONArray jsonArray = new JSONArray();
		if((recordCount - iNextNum + pagesize)>0){
			rs.executeSql(selSql);
			while(rs.next()){
				JSONObject json = new JSONObject();
				json.put("id", Util.null2String(rs.getString("id")));
				json.put("assetname", Util.null2String(rs.getString("assetname")));
				json.put("assetno", Util.null2String(rs.getString("assetno")));
				
				String assettype = "";
				String assettype1 = Util.null2String(rs.getString("assettype1"));
				String assettype2 = Util.null2String(rs.getString("assettype2"));
				String assettype3 = Util.null2String(rs.getString("assettype3"));
				
				if(StringHelper.isNotEmpty(assettype1)){
					assettype += assettype1;
					if(StringHelper.isNotEmpty(assettype2)){
						assettype += "-"+assettype2;
					}
					if(StringHelper.isNotEmpty(assettype3)){
						assettype += "-"+assettype3;
					}
				}else {
					if(StringHelper.isNotEmpty(assettype2)){
						assettype += assettype2;
						if(StringHelper.isNotEmpty(assettype3)){
							assettype += "-"+assettype3;
						}
					}else{
						if(StringHelper.isNotEmpty(assettype3)){
							assettype += assettype3;
						}
					}
				}
				
				json.put("assettype", assettype);
				json.put("creatorname", Util.null2String(rs.getString("creatorname")));
				jsonArray.add(json);
			}
		}
		JSONObject result = new JSONObject();
		result.put("totalSize", recordCount);
		result.put("datas", jsonArray.toString());
		try {
			response.setContentType("application/json; charset=utf-8");  
			response.getWriter().print(result.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 资产管理-我的无形资产接口
	 * @param 
	 * user	用户
	 * pageno	当前页数
	 * pagesize	每页显示数
	 */
	private void getmyWxzc(User user) {
		
		String userid = String.valueOf(user.getUID());	//用户id		
		RecordSet rs = new RecordSet();
		int recordCount=0;//总记录数
		int pageno = Util.getIntValue(request.getParameter("pageno"),1);//当前页
		int pagesize = Util.getIntValue(request.getParameter("pagesize"),10);//每页显示数
		//权限过滤
		String rightSql =getShareSql(90,Integer.parseInt(userid));
		String selSql = "select count(1) from uf_am_immatassets where 1=1 and usestatus!=3  and manager='"+userid+"' and exists (select 1 from "+rightSql+" m where m.sourceid=uf_am_immatassets.id)";
		rs.executeSql(selSql);
		if(rs.next()){
			recordCount = rs.getInt(1);
		}
		int iNextNum = pageno * pagesize;//第几页的总数
		int ipageset = pagesize;//第几页的显示条数
		
		if((recordCount - iNextNum + pagesize)>0){
			if((recordCount - iNextNum + pagesize < pagesize)){
				ipageset = recordCount - iNextNum + pagesize;
			}else{
				ipageset = pagesize;
			}
		}else{
			ipageset = 0;
		}
		//取按时间倒序排序的数据的前iNextNum条倒序排序
		selSql = "select top " + iNextNum +" uai.id,uai.assetname,uai.assetno, " +
				"( SELECT lastname FROM HrmResource n WHERE n.id= uai.manager ) AS creatorname," +
				"( SELECT name FROM uf_am_immatstatus WHERE id= uai.usestatus ) AS usestatus, "+
				" modedatacreatedate,modedatacreatetime FROM uf_am_immatassets uai  where uai.usestatus!=3 and uai.manager='"+userid+"'";		
		selSql += " and exists (select 1 from "+rightSql+" m where m.sourceid=uai.id) order by modedatacreatedate desc,modedatacreatetime desc";
		//将上面得到的数据取按顺序排序的ipageset条数据顺序排序
		selSql = "select top " + ipageset +" t1.* from (" + selSql + ") t1 order by modedatacreatedate asc,modedatacreatetime asc";
		//将得到的数据倒序排序显示
		selSql = "select t2.* from (" + selSql + ") t2 order by modedatacreatedate desc,modedatacreatetime desc";
		
		JSONArray jsonArray = new JSONArray();
		if((recordCount - iNextNum + pagesize)>0){
			rs.executeSql(selSql);
			while(rs.next()){
				JSONObject json = new JSONObject();
				json.put("id", Util.null2String(rs.getString("id")));
				json.put("assetname", Util.null2String(rs.getString("assetname")));
				json.put("assetno", Util.null2String(rs.getString("assetno")));				
				json.put("creatorname", Util.null2String(rs.getString("creatorname")));
				json.put("usestatus", Util.null2String(rs.getString("usestatus")));
				jsonArray.add(json);
			}
		}
		JSONObject result = new JSONObject();
		result.put("totalSize", recordCount);
		result.put("datas", jsonArray.toString());
		try {
			response.setContentType("application/json; charset=utf-8");  
			response.getWriter().print(result.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 资产管理-固定资产接口
	 * @param 
	 * user	用户
	 * pageno	当前页数
	 * pagesize	每页显示数
	 */
	private void getGdzc(User user) {
		
		String userid = String.valueOf(user.getUID());	//用户id
		String glyqx = StringHelper.null2String(request.getParameter("glyqx"));
		String searchValue = StringHelper.null2String(request.getParameter("searchValue"));
		RecordSet rs = new RecordSet();
		int recordCount=0;//总记录数
		int pageno = Util.getIntValue(request.getParameter("pageno"),1);//当前页
		int pagesize = Util.getIntValue(request.getParameter("pagesize"),10);//每页显示数
		//权限过滤
		String rightSql =getShareSql(89,Integer.parseInt(userid));
		String selSql = "select count(1) from uf_am_fixedassets where 1=1 and (usestatus=1 or usestatus=2 or usestatus=6) and exists (select 1 from "+rightSql+" m where m.sourceid=uf_am_fixedassets.id)";
		rs.executeSql(selSql);
		if(rs.next()){
			recordCount = rs.getInt(1);
		}
		int iNextNum = pageno * pagesize;//第几页的总数
		int ipageset = pagesize;//第几页的显示条数
		
		if((recordCount - iNextNum + pagesize)>0){
			if((recordCount - iNextNum + pagesize < pagesize)){
				ipageset = recordCount - iNextNum + pagesize;
			}else{
				ipageset = pagesize;
			}
		}else{
			ipageset = 0;
		}
		//取按时间倒序排序的数据的前iNextNum条倒序排序
		selSql = "select top " + iNextNum +" uaf.id,uaf.assetname,uaf.assetno," +
				"( select departmentname from HrmDepartment where id=uaf.managedept) as managedept, " +
				"( select name from uf_am_storecity where id=uaf.address) as address, " +
				"( SELECT name FROM uf_am_assettype uaa WHERE  uaa.id = uaf.assettype1 ) AS assettype1, " +
				//"( SELECT name FROM uf_am_assettype uaa WHERE  uaa.id = uaf.assettype2 ) AS assettype2, " +
				//"( SELECT name FROM uf_am_assettype uaa WHERE  uaa.id = uaf.assettype3 ) AS assettype3, " +
				"( SELECT lastname FROM HrmResource n WHERE n.id= uaf.userperson ) AS creatorname, " +
				" modedatacreatedate,modedatacreatetime FROM uf_am_fixedassets uaf  where  (uaf.usestatus=1 or usestatus=2 or uaf.usestatus=6) and";
		if("149".equals(glyqx)||"156".equals(glyqx)){	//部门管理员-部门负责人
			selSql += "  uaf.managedept = (select departmentid from hrmresource n where n.id='"+userid+"' ) and "; 
		}
		if(StringHelper.isNotEmpty(searchValue)){
			selSql += " (uaf.assetname like '%"+searchValue+"%' or uaf.assetno like '%"+searchValue+"%') and ";
		}
		selSql += "  1=1 and exists (select 1 from "+rightSql+" m where m.sourceid=uaf.id) order by modedatacreatedate desc,modedatacreatetime desc";
		//将上面得到的数据取按顺序排序的ipageset条数据顺序排序
		selSql = "select top " + ipageset +" t1.* from (" + selSql + ") t1 order by modedatacreatedate asc,modedatacreatetime asc";
		//将得到的数据倒序排序显示
		selSql = "select t2.* from (" + selSql + ") t2 order by modedatacreatedate desc,modedatacreatetime desc";
		
		JSONArray jsonArray = new JSONArray();
		if((recordCount - iNextNum + pagesize)>0){
			rs.executeSql(selSql);
			while(rs.next()){
				JSONObject json = new JSONObject();
				json.put("id", Util.null2String(rs.getString("id")));
				json.put("assetname", Util.null2String(rs.getString("assetname")));
				json.put("assetno", Util.null2String(rs.getString("assetno")));
				
				String assettype = "";
				String assettype1 = Util.null2String(rs.getString("assettype1"));
				/*
				String assettype2 = Util.null2String(rs.getString("assettype2"));
				String assettype3 = Util.null2String(rs.getString("assettype3"));
				
				if(StringHelper.isNotEmpty(assettype1)){
					assettype += assettype1;
					if(StringHelper.isNotEmpty(assettype2)){
						assettype += "-"+assettype2;
					}
					if(StringHelper.isNotEmpty(assettype3)){
						assettype += "-"+assettype3;
					}
				}else {
					if(StringHelper.isNotEmpty(assettype2)){
						assettype += assettype2;
						if(StringHelper.isNotEmpty(assettype3)){
							assettype += "-"+assettype3;
						}
					}else{
						if(StringHelper.isNotEmpty(assettype3)){
							assettype += assettype3;
						}
					}
				}
				*/
				json.put("assettype", assettype1);
				json.put("managedept", Util.null2String(rs.getString("managedept")));
				json.put("creatorname", Util.null2String(rs.getString("creatorname")));
				json.put("address", Util.null2String(rs.getString("address")));
				jsonArray.add(json);
			}
		}
		JSONObject result = new JSONObject();
		result.put("totalSize", recordCount);
		result.put("datas", jsonArray.toString());
		try {
			response.setContentType("application/json; charset=utf-8");  
			response.getWriter().print(result.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 资产管理-无形资产接口
	 * @param 
	 * user	用户
	 * pageno	当前页数
	 * pagesize	每页显示数
	 */
	private void getWxzc(User user) {
		
		String userid = String.valueOf(user.getUID());	//用户id
		String glyqx = StringHelper.null2String(request.getParameter("glyqx"));
		String searchValue = StringHelper.null2String(request.getParameter("searchValue"));
		RecordSet rs = new RecordSet();
		int recordCount=0;//总记录数
		int pageno = Util.getIntValue(request.getParameter("pageno"),1);//当前页
		int pagesize = Util.getIntValue(request.getParameter("pagesize"),10);//每页显示数
		//权限过滤
		String rightSql =getShareSql(90,Integer.parseInt(userid));
		String selSql = "select count(1) from uf_am_immatassets where 1=1 and usestatus!=3 and exists (select 1 from "+rightSql+" m where m.sourceid=uf_am_immatassets.id)";
		rs.executeSql(selSql);
		if(rs.next()){
			recordCount = rs.getInt(1);
		}
		int iNextNum = pageno * pagesize;//第几页的总数
		int ipageset = pagesize;//第几页的显示条数
		
		if((recordCount - iNextNum + pagesize)>0){
			if((recordCount - iNextNum + pagesize < pagesize)){
				ipageset = recordCount - iNextNum + pagesize;
			}else{
				ipageset = pagesize;
			}
		}else{
			ipageset = 0;
		}
		//取按时间倒序排序的数据的前iNextNum条倒序排序
		selSql = "select top " + iNextNum +" uai.id,uai.assetname,uai.assetno, " +
				"( SELECT lastname FROM HrmResource n WHERE n.id= uai.manager ) AS creatorname," +
				"( SELECT name FROM uf_am_immatstatus WHERE id= uai.usestatus ) AS usestatus, "+
				" modedatacreatedate,modedatacreatetime FROM uf_am_immatassets uai  where  uai.usestatus!=3 and ";
		if("149".equals(glyqx)||"156".equals(glyqx)){	//部门管理员-部门负责人
			selSql += "  uai.managedept = (select departmentid from hrmresource n where n.id='"+userid+"' ) and "; 
		}
		if(StringHelper.isNotEmpty(searchValue)){
			selSql += " (uai.assetname like '%"+searchValue+"%' or uai.assetno like '%"+searchValue+"%') and ";
		}
		selSql += "  1=1 and exists (select 1 from "+rightSql+" m where m.sourceid=uai.id) order by modedatacreatedate desc,modedatacreatetime desc";
		//将上面得到的数据取按顺序排序的ipageset条数据顺序排序
		selSql = "select top " + ipageset +" t1.* from (" + selSql + ") t1 order by modedatacreatedate asc,modedatacreatetime asc";
		//将得到的数据倒序排序显示
		selSql = "select t2.* from (" + selSql + ") t2 order by modedatacreatedate desc,modedatacreatetime desc";
		
		JSONArray jsonArray = new JSONArray();
		if((recordCount - iNextNum + pagesize)>0){
			rs.executeSql(selSql);
			while(rs.next()){
				JSONObject json = new JSONObject();
				json.put("id", Util.null2String(rs.getString("id")));
				json.put("assetname", Util.null2String(rs.getString("assetname")));
				json.put("assetno", Util.null2String(rs.getString("assetno")));				
				json.put("creatorname", Util.null2String(rs.getString("creatorname")));
				json.put("usestatus", Util.null2String(rs.getString("usestatus")));
				jsonArray.add(json);
			}
		}
		JSONObject result = new JSONObject();
		result.put("totalSize", recordCount);
		result.put("datas", jsonArray.toString());
		try {
			response.setContentType("application/json; charset=utf-8");  
			response.getWriter().print(result.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 资产管理-易耗品接口
	 * @param 
	 * user	用户
	 * pageno	当前页数
	 * pagesize	每页显示数
	 */
	private void getYhp(User user) {
		
		String userid = String.valueOf(user.getUID());	//用户id
		RecordSet rs = new RecordSet();
		int recordCount=0;//总记录数
		int pageno = Util.getIntValue(request.getParameter("pageno"),1);//当前页
		int pagesize = Util.getIntValue(request.getParameter("pagesize"),10);//每页显示数
		String searchValue = StringHelper.null2String(request.getParameter("searchValue"));
		//权限过滤
		String rightSql =getShareSql(92,Integer.parseInt(userid));
		String selSql = "select count(1) from uf_am_lowitem where 1=1 and exists (select 1 from "+rightSql+" m where m.sourceid=uf_am_lowitem.id)";
		rs.executeSql(selSql);
		if(rs.next()){
			recordCount = rs.getInt(1);
		}
		int iNextNum = pageno * pagesize;//第几页的总数
		int ipageset = pagesize;//第几页的显示条数
		
		if((recordCount - iNextNum + pagesize)>0){
			if((recordCount - iNextNum + pagesize < pagesize)){
				ipageset = recordCount - iNextNum + pagesize;
			}else{
				ipageset = pagesize;
			}
		}else{
			ipageset = 0;
		}
		//取按时间倒序排序的数据的前iNextNum条倒序排序
		selSql = "select top " + iNextNum +" ual.id,uai.itemname,(select name from uf_am_itemtype where id=uai.itemtype) as itemtype,uak.name as measureunit,ual.amount,ual.remaindamount, " +
				" ual.modedatacreatedate,ual.modedatacreatetime FROM uf_am_lowitem ual left JOIN uf_am_itemname uai on ual.itemname = uai.id " +
				" left JOIN uf_am_itemuni uak on uai.measureunit = uak.id where ";
		if(StringHelper.isNotEmpty(searchValue)){
			selSql += " (uai.itemname like '%"+searchValue+"%' ) and ";
		}
		selSql += "  1=1 and exists (select 1 from "+rightSql+" m where m.sourceid=ual.id) order by modedatacreatedate desc,modedatacreatetime desc";
		//将上面得到的数据取按顺序排序的ipageset条数据顺序排序
		selSql = "select top " + ipageset +" t1.* from (" + selSql + ") t1 order by modedatacreatedate asc,modedatacreatetime asc";
		//将得到的数据倒序排序显示
		selSql = "select t2.* from (" + selSql + ") t2 order by modedatacreatedate desc,modedatacreatetime desc";
		
		JSONArray jsonArray = new JSONArray();
		if((recordCount - iNextNum + pagesize)>0){
			rs.executeSql(selSql);
			while(rs.next()){
				JSONObject json = new JSONObject();
				json.put("id", Util.null2String(rs.getString("id")));
				json.put("itemname", Util.null2String(rs.getString("itemname")));
				json.put("itemtype", Util.null2String(rs.getString("itemtype")));
				json.put("measureunit", Util.null2String(rs.getString("measureunit")));
				json.put("amount", Util.null2String(rs.getString("amount")));
				json.put("remaindamount", Util.null2String(rs.getString("remaindamount")));
				jsonArray.add(json);
			}
		}
		JSONObject result = new JSONObject();
		result.put("totalSize", recordCount);
		result.put("datas", jsonArray.toString());
		try {
			response.setContentType("application/json; charset=utf-8");  
			response.getWriter().print(result.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 资产管理-剩余易耗品接口
	 * @param 
	 * user	用户
	 * pageno	当前页数
	 * pagesize	每页显示数
	 */
	private void getSYYhp(User user) {
		
		String userid = String.valueOf(user.getUID());	//用户id
		RecordSet rs = new RecordSet();
		int recordCount=0;//总记录数
		int pageno = Util.getIntValue(request.getParameter("pageno"),1);//当前页
		int pagesize = Util.getIntValue(request.getParameter("pagesize"),10);//每页显示数
		//权限过滤
		String rightSql =getShareSql(92,Integer.parseInt(userid));
		String selSql = "select count(1) from uf_am_lowitem ual left JOIN uf_am_itemname uai on ual.itemname = uai.id where 1=1 and ual.remaindamount<=isnull(uai.amount,0) and uai.amount>0 and exists (select 1 from "+rightSql+" m where m.sourceid=ual.id)";
		
		rs.executeSql(selSql);
		if(rs.next()){
			recordCount = rs.getInt(1);
		}
		int iNextNum = pageno * pagesize;//第几页的总数
		int ipageset = pagesize;//第几页的显示条数
		
		if((recordCount - iNextNum + pagesize)>0){
			if((recordCount - iNextNum + pagesize < pagesize)){
				ipageset = recordCount - iNextNum + pagesize;
			}else{
				ipageset = pagesize;
			}
		}else{
			ipageset = 0;
		}
		//取按时间倒序排序的数据的前iNextNum条倒序排序
		selSql = "select top " + iNextNum +" ual.id,uai.itemname,(select name from uf_am_itemuni where id=uai.measureunit) as measureunit,ual.remaindamount, " +
				" ual.modedatacreatedate,ual.modedatacreatetime FROM uf_am_lowitem ual left JOIN uf_am_itemname uai on ual.itemname = uai.id  where ";
		selSql += "  1=1 and ual.remaindamount <=isnull(uai.amount,0) and uai.amount>0 and exists (select 1 from "+rightSql+" m where m.sourceid=ual.id) order by modedatacreatedate desc,modedatacreatetime desc";
		//将上面得到的数据取按顺序排序的ipageset条数据顺序排序
		selSql = "select top " + ipageset +" t1.* from (" + selSql + ") t1 order by modedatacreatedate asc,modedatacreatetime asc";
		//将得到的数据倒序排序显示
		selSql = "select t2.* from (" + selSql + ") t2 order by modedatacreatedate desc,modedatacreatetime desc";
		
		JSONArray jsonArray = new JSONArray();
		if((recordCount - iNextNum + pagesize)>0){
			rs.executeSql(selSql);
			while(rs.next()){
				JSONObject json = new JSONObject();
				json.put("id", Util.null2String(rs.getString("id")));
				json.put("itemname", Util.null2String(rs.getString("itemname")));
				json.put("measureunit", Util.null2String(rs.getString("measureunit")));
				json.put("remaindamount", Util.null2String(rs.getString("remaindamount")));
				jsonArray.add(json);
			}
		}
		JSONObject result = new JSONObject();
		result.put("totalSize", recordCount);
		result.put("datas", jsonArray.toString());
		try {
			response.setContentType("application/json; charset=utf-8");  
			response.getWriter().print(result.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 资产管理-低值易耗品接口
	 * @param 
	 * user	用户
	 * pageno	当前页数
	 * pagesize	每页显示数
	 */
	private void getDzyhp(User user) {
		
		String userid = String.valueOf(user.getUID());	//用户id
		String glyqx = StringHelper.null2String(request.getParameter("glyqx"));
		String searchValue = StringHelper.null2String(request.getParameter("searchValue"));
		RecordSet rs = new RecordSet();
		int recordCount=0;//总记录数
		int pageno = Util.getIntValue(request.getParameter("pageno"),1);//当前页
		int pagesize = Util.getIntValue(request.getParameter("pagesize"),10);//每页显示数
		//权限过滤
		String rightSql =getShareSql(99,Integer.parseInt(userid));
		String selSql = "select count(1) from uf_am_articles where 1=1 and (usestatus=1 or usestatus=2 or usestatus=6) and exists (select 1 from "+rightSql+" m where m.sourceid=uf_am_articles.id)";
		rs.executeSql(selSql);
		if(rs.next()){
			recordCount = rs.getInt(1);
		}
		int iNextNum = pageno * pagesize;//第几页的总数
		int ipageset = pagesize;//第几页的显示条数
		
		if((recordCount - iNextNum + pagesize)>0){
			if((recordCount - iNextNum + pagesize < pagesize)){
				ipageset = recordCount - iNextNum + pagesize;
			}else{
				ipageset = pagesize;
			}
		}else{
			ipageset = 0;
		}
		//取按时间倒序排序的数据的前iNextNum条倒序排序
		selSql = "select top " + iNextNum +" uaf.id,uaf.assetname,uaf.assetno," +
				"( select departmentname from HrmDepartment where id=uaf.managedept) as managedept, " +
				"( select name from uf_am_storecity where id=uaf.address) as address, " +
				"( SELECT name FROM uf_am_articlestype uaa WHERE  uaa.id = uaf.assettype1 ) AS assettype1, " +
				//"( SELECT name FROM uf_am_assettype uaa WHERE  uaa.id = uaf.assettype2 ) AS assettype2, " +
				//"( SELECT name FROM uf_am_assettype uaa WHERE  uaa.id = uaf.assettype3 ) AS assettype3, " +
				"( SELECT lastname FROM HrmResource n WHERE n.id= uaf.userperson ) AS creatorname, " +
				" modedatacreatedate,modedatacreatetime FROM uf_am_articles uaf  where  (uaf.usestatus=1 or usestatus=2 or uaf.usestatus=6) and";
		if("149".equals(glyqx)||"156".equals(glyqx)){	//部门管理员-部门负责人
			selSql += "  uaf.managedept = (select departmentid from hrmresource n where n.id='"+userid+"' ) and "; 
		}
		if(StringHelper.isNotEmpty(searchValue)){
			selSql += " (uaf.assetname like '%"+searchValue+"%' or uaf.assetno like '%"+searchValue+"%') and ";
		}
		selSql += "  1=1 and exists (select 1 from "+rightSql+" m where m.sourceid=uaf.id) order by modedatacreatedate desc,modedatacreatetime desc";
		//将上面得到的数据取按顺序排序的ipageset条数据顺序排序
		selSql = "select top " + ipageset +" t1.* from (" + selSql + ") t1 order by modedatacreatedate asc,modedatacreatetime asc";
		//将得到的数据倒序排序显示
		selSql = "select t2.* from (" + selSql + ") t2 order by modedatacreatedate desc,modedatacreatetime desc";
		
		JSONArray jsonArray = new JSONArray();
		if((recordCount - iNextNum + pagesize)>0){
			rs.executeSql(selSql);
			while(rs.next()){
				JSONObject json = new JSONObject();
				json.put("id", Util.null2String(rs.getString("id")));
				json.put("assetname", Util.null2String(rs.getString("assetname")));
				json.put("assetno", Util.null2String(rs.getString("assetno")));
				
				String assettype = "";
				String assettype1 = Util.null2String(rs.getString("assettype1"));
				/*
				String assettype2 = Util.null2String(rs.getString("assettype2"));
				String assettype3 = Util.null2String(rs.getString("assettype3"));
				
				if(StringHelper.isNotEmpty(assettype1)){
					assettype += assettype1;
					if(StringHelper.isNotEmpty(assettype2)){
						assettype += "-"+assettype2;
					}
					if(StringHelper.isNotEmpty(assettype3)){
						assettype += "-"+assettype3;
					}
				}else {
					if(StringHelper.isNotEmpty(assettype2)){
						assettype += assettype2;
						if(StringHelper.isNotEmpty(assettype3)){
							assettype += "-"+assettype3;
						}
					}else{
						if(StringHelper.isNotEmpty(assettype3)){
							assettype += assettype3;
						}
					}
				}
				*/
				json.put("assettype", assettype1);
				json.put("managedept", Util.null2String(rs.getString("managedept")));
				json.put("creatorname", Util.null2String(rs.getString("creatorname")));
				json.put("address", Util.null2String(rs.getString("address")));
				jsonArray.add(json);
			}
		}
		JSONObject result = new JSONObject();
		result.put("totalSize", recordCount);
		result.put("datas", jsonArray.toString());
		try {
			response.setContentType("application/json; charset=utf-8");  
			response.getWriter().print(result.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取数据共享权限 sql
	 * @param formmodeid	建模id
	 * @param userid	用户id
	 * @return
	 */
	public String getShareSql(int formmodeid,int userid){
		RecordSet rs = new RecordSet();
		User user = new User();
		String sql = "";
		if(userid==1){
			sql = "select * from HrmResourceManager where id="+userid;
		}else{
			sql="select * from HrmResource where id="+userid;
		}
		rs.executeSql(sql);
        rs.next();
		user.setUid(rs.getInt("id"));
        user.setLoginid(rs.getString("loginid"));
        user.setSeclevel(rs.getString("seclevel"));
        user.setUserDepartment(Util.getIntValue(rs.getString("departmentid"), 0));
        user.setUserSubCompany1(Util.getIntValue(rs.getString("subcompanyid1"), 0));
        user.setUserSubCompany2(Util.getIntValue(rs.getString("subcompanyid2"), 0));
        user.setUserSubCompany3(Util.getIntValue(rs.getString("subcompanyid3"), 0));
        user.setUserSubCompany4(Util.getIntValue(rs.getString("subcompanyid4"), 0));
        user.setManagerid(rs.getString("managerid"));
        user.setLogintype("1");
		
        ModeShareManager modeshare = new ModeShareManager();
        modeshare.setModeId(formmodeid);
        String rightsql = modeshare.getShareDetailTableByUser("formmode",user);
        return rightsql;
	}
}
