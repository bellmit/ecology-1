package weaver.sysinterface.am;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;
import weaver.conn.RecordSet;
import weaver.hrm.User;

import com.alibaba.fastjson.JSONArray;
import com.weaver.formmodel.mobile.manager.MobileUserInit;
import com.weaver.formmodel.util.StringHelper;

public class amCaiDanLan extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private HttpServletRequest request;
	private HttpServletResponse response;
	
	public void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException{
		this.request = request;
		this.response = response;
		try {
			User user = MobileUserInit.getUser(request, response);
			if(user == null){
				JSONObject checkUser = new JSONObject();
				checkUser.put("msgstatus", "0");
				checkUser.put("msg", "服务器端重置了登录信息，请重新登录");
				response.setContentType("application/json; charset=utf-8");  
				response.getWriter().print(checkUser.toString());
				return;
			}else{
				RecordSet rs = new RecordSet();
				String userid = String.valueOf(user.getUID());				
					String sqlUser = "  select hrs.id from hrmrolemembers hrme left join hrmroles hrs on hrme.roleid = hrs.id " +
							" where (hrs.id = 148 or hrs.id = 149 or hrs.id = 150 or hrs.id = 155 or hrs.id = 2) and hrme.resourceid = '"+userid+"'" +
							" union all "+
							" select 156 from Matrixtable_3 where bmfzr='"+userid+"'";//矩阵中的部门负责人
					rs.executeSql(sqlUser);
					if(rs.next()){
						String action = StringHelper.null2String(rs.getString("id"));
						if("148".equals(action)||"2".equals(action)){		//公司资产管理员--系统管理员
							getGS();
						}else if("150".equals(action)||"155".equals(action)){	//公司领导--公司资产负责人
							getGSleader();
						}else if("149".equals(action)||"156".equals(action)){		//部门资产管理员--部门负责人
							getBM();
						}
					}				
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	/**
	 * 资产管理-公司领导菜单接口
	 * @param 
	 * user	用户
	 * pageno	当前页数
	 * pagesize	每页显示数
	 */
	public void getGSleader(){
		String gSCaiDan = " <dl class=\"daohang-item\" data-name=\"导航\" style=\"height: 30px;margin-top:10px;\">" +
				"<dt class=\"am-accordion-title\">" +
				"<img src=\"img/dh.png\" style=\"height: 28px;margin-bottom: 5px;\" alt=\"\" >" +
				"<a style=\"font-size:24px;color:#d44632;line-height: 30px;\">导航<a/>" +
				"</dt>" +
				"</dl>" +
				"<dl class=\"am-accordion-item\" data-name=\"固定资产管理\">" +
				"<dt class=\"am-accordion-title actived\">" +
				"<img class=\"img1\" src=\"img/col01.png\" alt=\"\" height=\"25\" style=\"display: none;\"><img class=\"img2\" src=\"img/col11.png\" alt=\"\" height=\"25\" style=\"\">固定资产" +
				"</dt>" +
				"<dd class=\"am-accordion-bd am-collapse\" style=\"height: 0px;\">" +
				"<div class=\"am-accordion-content\">" +
				"<ul>" +
				"<li ><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=99\" target=\"_blank\" data-name=\"\">我的资产</a></li>" +
				"<li ><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=109\" target=\"_blank\" data-name=\"\">资产查询</a></li>" +
				"<li style=\"border-bottom:0px\"><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=102\" target=\"_blank\" data-name=\"\">已处置资产查询</a></li>" +				
				"</ul>" +
				"</div>" +
				"</dd>" +
				"</dl>" +
				"<dl class=\"am-accordion-item\" data-name=\"无形资产管理\">" +
				"<dt class=\"am-accordion-title actived\">" +
				"<img class=\"img1\" src=\"img/col02.png\" alt=\"\" height=\"25\" style=\"display: none;\"><img class=\"img2\" src=\"img/col12.png\" alt=\"\" height=\"25\" style=\"\">无形资产" +
				"</dt>" +
				"<dd class=\"am-accordion-bd am-collapse\" style=\"height: 0px;\">" +				
				"<div class=\"am-accordion-content\">" +
				"<ul>" +
				"<li ><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=98\" target=\"_blank\" data-name=\"\">我的无形资产</a></li>" +
				"<li ><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=97\" target=\"_blank\" data-name=\"\">无形资产查询</a></li>" +
				"<li style=\"border-bottom:0px\"><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=103\" target=\"_blank\" data-name=\"\">已下线资产查询</a></li>" +
				"</ul>" +
				"</div>" +
				"</dd>" +
				"</dl>" +
				"<dl class=\"am-accordion-item\" data-name=\"消耗品管理\">" +
				"<dt class=\"am-accordion-title actived\">" +
				"<img class=\"img1\" src=\"img/col03.png\" alt=\"\" height=\"25\" style=\"display: none;\"><img class=\"img2\" src=\"img/col13.png\" alt=\"\" height=\"25\" style=\"\">消耗品" +
				"</dt>" +
				"<dd class=\"am-accordion-bd am-collapse\" style=\"height: 0px;\">" +				
				"<div class=\"am-accordion-content\">" +
				"<ul>" +
				"<li ><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=107\" target=\"_blank\" data-name=\"\">消耗品名称</a></li>" +
				"<li ><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=105\" target=\"_blank\" data-name=\"\">消耗品查询</a></li>" +
				"<li style=\"border-bottom:0px\"><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=106\" target=\"_blank\" data-name=\"\">消耗品库存预警</a></li>" +
				"</ul>" +
				"</div>" +
				"</dd>" +
				"</dl>" +
				"<dl class=\"am-accordion-item\" data-name=\"低值易耗品管理\">" +
				"<dt class=\"am-accordion-title actived\">" +
				"<img class=\"img1\" src=\"img/col04.png\" alt=\"\" height=\"25\" style=\"display: none;\"><img class=\"img2\" src=\"img/col14.png\" alt=\"\" height=\"25\" style=\"\">低值易耗品" +
				"</dt>" +
				"<dd class=\"am-accordion-bd am-collapse\" style=\"height: 0px;\">" +				
				"<div class=\"am-accordion-content\">" +
				"<ul>" +
				"<li ><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=132\" target=\"_blank\" data-name=\"\">我的低值易耗品</a></li>" +
				"<li style=\"border-bottom:0px\"><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=130\" target=\"_blank\" data-name=\"\">低值易耗品查询</a></li>" +
				"</ul>" +
				"</div>" +
				"</dd>" +
				"</dl>" +				
				"<dl class=\"am-accordion-item\" data-name=\"统计报表\">" +
				"<dt class=\"am-accordion-title actived\">" +
				"<img class=\"img1\" src=\"img/col06.png\" alt=\"\" height=\"25\" style=\"display: none;\"><img class=\"img2\" src=\"img/col16.png\" alt=\"\" height=\"25\" style=\"\">统计报表" +
				"</dt>" +
				"<dd class=\"am-accordion-bd am-collapse\" style=\"height: 0px;\">" +				
				"<div class=\"am-accordion-content\">" +
				"<ul>" +
				"<li ><a class=\"a1\" data-id=\"1\" href=\"#\" target=\"_blank\" data-name=\"\">报表1</a></li>" +
				"<li ><a class=\"a1\" data-id=\"1\" href=\"#\" target=\"_blank\" data-name=\"\">报表2</a></li>" +
				"<li style=\"border-bottom:0px\"><a class=\"a1\" data-id=\"1\" href=\"#\" target=\"_blank\" data-name=\"\">报表3</a></li>" +
				"</ul>" +
				"</div>" +
				"</dd>" +
				"</dl>";
		String gSDaoHhang = "<div class=\"yewu-item\">" +
				"<div class=\"yewu-header\">固 定 资 产&nbsp;&nbsp;<i class=\"am-icon am-icon-chevron-right\"></i></div>" +
				"<ul>" +
				"<li><a>|</a><a href=\"/formmode/search/CustomSearchBySimple.jsp?customid=109\" target=\"_blank\">资产库</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=399\" target=\"_blank\">验收入库</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=400\" target=\"_blank\">资产领用</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=401\" target=\"_blank\">资产归还</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=402\" target=\"_blank\">部门内变更</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=403\" target=\"_blank\">部门间调配</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=404\" target=\"_blank\">资产处置</a></li>" +
				"</ul>" +
				"</div>" +
				"<div class=\"yewu-item\">" +
				"<div class=\"yewu-header\">无 形 资 产&nbsp;&nbsp;<i class=\"am-icon am-icon-chevron-right\"></i></div>" +
				"<ul>" +
				"<li><a>|</a><a href=\"/formmode/search/CustomSearchBySimple.jsp?customid=97\" target=\"_blank\">无形资产库</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=406\" target=\"_blank\">无形资产入库</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=407\" target=\"_blank\">无形资产处置</a></li>" +
				"</ul>" +
				"</div>" +
				"<div class=\"yewu-item\">" +
				"<div class=\"yewu-header\">消 耗 品&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<i class=\"am-icon am-icon-chevron-right\"></i></div>" +
				"<ul>" +
				"<li class=\"firisItem\"><a>|</a><a href=\"/formmode/search/CustomSearchBySimple.jsp?customid=110\" target=\"_blank\">消耗品库</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=408\" target=\"_blank\">消耗品入库</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=409\" target=\"_blank\">消耗品领用</a></li>" +
				"</ul>" +
				"</div>" +
				"<div class=\"yewu-item\">" +
				"<div class=\"yewu-header\">低值易耗品&nbsp;<i class=\"am-icon am-icon-chevron-right\"></i></div>" +
				"<ul>" +
				"<li class=\"firisItem\"><a>|</a><a href=\"/formmode/search/CustomSearchBySimple.jsp?customid=130\" target=\"_blank\">低值易耗品库</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=414\" target=\"_blank\">低值易耗品入库</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=416\" target=\"_blank\">低值易耗品分配</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=415\" target=\"_blank\">低值易耗品归还</a></li>" +
				"</ul>" +
				"</div>";
		try {
			response.setContentType("application/json; charset=utf-8");  
			JSONObject jsonobject=new JSONObject();
			jsonobject.put("CaiDan", gSCaiDan);
			jsonobject.put("DaoHhang", gSDaoHhang);
			response.getWriter().print(jsonobject);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	/**
	 * 资产管理-部门菜单接口
	 * @param 
	 * user	用户
	 * pageno	当前页数
	 * pagesize	每页显示数
	 */
	public void getBM(){
		String gSCaiDan = " <dl class=\"daohang-item\" data-name=\"导航\" style=\"height: 30px;margin-top:10px;\">" +
				"<dt class=\"am-accordion-title\">" +
				"<img src=\"img/dh.png\" style=\"height: 28px;margin-bottom: 5px;\" alt=\"\" >" +
				"<a style=\"font-size:24px;color:#d44632;line-height: 30px;\">导航<a/>" +
				"</dt>" +
				"</dl>" +
				"<dl class=\"am-accordion-item\" data-name=\"固定资产管理\">" +
				"<dt class=\"am-accordion-title actived\">" +
				"<img class=\"img1\" src=\"img/col01.png\" alt=\"\" height=\"25\" style=\"display: none;\"><img class=\"img2\" src=\"img/col11.png\" alt=\"\" height=\"25\" style=\"\">固定资产" +
				"</dt>" +
				"<dd class=\"am-accordion-bd am-collapse\" style=\"height: 0px;\">" +
				"<div class=\"am-accordion-content\">" +
				"<ul>" +
				"<li ><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=99\" target=\"_blank\" data-name=\"\">我的资产</a></li>" +
				"<li style=\"border-bottom:0px\"><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=100\" target=\"_blank\" data-name=\"\">资产查询</a></li>" +								
				"</ul>" +
				"</div>" +
				"</dd>" +
				"</dl>" +
				"<dl class=\"am-accordion-item\" data-name=\"无形资产管理\">" +
				"<dt class=\"am-accordion-title actived\">" +
				"<img class=\"img1\" src=\"img/col02.png\" alt=\"\" height=\"25\" style=\"display: none;\"><img class=\"img2\" src=\"img/col12.png\" alt=\"\" height=\"25\" style=\"\">无形资产" +
				"</dt>" +
				"<dd class=\"am-accordion-bd am-collapse\" style=\"height: 0px;\">" +				
				"<div class=\"am-accordion-content\">" +
				"<ul>" +
				"<li ><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=98\" target=\"_blank\" data-name=\"\">我的无形资产</a></li>" +
				"<li style=\"border-bottom:0px\"><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=101\" target=\"_blank\" data-name=\"\">无形资产查询</a></li>" +				
				"</ul>" +
				"</div>" +
				"</dd>" +
				"</dl>" +
				"<dl class=\"am-accordion-item\" data-name=\"消耗品管理\">" +
				"<dt class=\"am-accordion-title actived\">" +
				"<img class=\"img1\" src=\"img/col03.png\" alt=\"\" height=\"25\" style=\"display: none;\"><img class=\"img2\" src=\"img/col13.png\" alt=\"\" height=\"25\" style=\"\">消耗品" +
				"</dt>" +
				"<dd class=\"am-accordion-bd am-collapse\" style=\"height: 0px;\">" +				
				"<div class=\"am-accordion-content\">" +
				"<ul>" +				
				"<li style=\"border-bottom:0px\"><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=105\" target=\"_blank\" data-name=\"\">消耗品查询</a></li>" +				
				"</ul>" +
				"</div>" +
				"</dd>" +
				"</dl>" +
				"<dl class=\"am-accordion-item\" data-name=\"低值易耗品管理\">" +
				"<dt class=\"am-accordion-title actived\">" +
				"<img class=\"img1\" src=\"img/col04.png\" alt=\"\" height=\"25\" style=\"display: none;\"><img class=\"img2\" src=\"img/col14.png\" alt=\"\" height=\"25\" style=\"\">低值易耗品" +
				"</dt>" +
				"<dd class=\"am-accordion-bd am-collapse\" style=\"height: 0px;\">" +				
				"<div class=\"am-accordion-content\">" +
				"<ul>" +
				"<li ><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=132\" target=\"_blank\" data-name=\"\">我的低值易耗品</a></li>" +
				"<li style=\"border-bottom:0px\"><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=135\" target=\"_blank\" data-name=\"\">低值易耗品查询</a></li>" +
				"</ul>" +
				"</div>" +
				"</dd>" +
				"</dl>" ;			

		String gSDaoHhang = "<div class=\"yewu-item\">" +
				"<div class=\"yewu-header\">固 定 资 产&nbsp;&nbsp;<i class=\"am-icon am-icon-chevron-right\"></i></div>" +
				"<ul>" +
				"<li><a>|</a><a href=\"/formmode/search/CustomSearchBySimple.jsp?customid=100\" target=\"_blank\">资产库</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=399\" target=\"_blank\">验收入库</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=400\" target=\"_blank\">资产领用</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=401\" target=\"_blank\">资产归还</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=402\" target=\"_blank\">部门内变更</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=403\" target=\"_blank\">部门间调配</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=404\" target=\"_blank\">资产处置</a></li>" +
				"</ul>" +
				"</div>" +
				"<div class=\"yewu-item\">" +
				"<div class=\"yewu-header\">无 形 资 产&nbsp;&nbsp;<i class=\"am-icon am-icon-chevron-right\"></i></div>" +
				"<ul>" +
				"<li><a>|</a><a href=\"/formmode/search/CustomSearchBySimple.jsp?customid=101\" target=\"_blank\">无形资产库</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=406\" target=\"_blank\">无形资产入库</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=407\" target=\"_blank\">无形资产处置</a></li>" +
				"</ul>" +
				"</div>" +
				"<div class=\"yewu-item\">" +
				"<div class=\"yewu-header\">消 耗 品&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<i class=\"am-icon am-icon-chevron-right\"></i></div>" +
				"<ul>" +
				"<li class=\"firisItem\"><a>|</a><a href=\"/formmode/search/CustomSearchBySimple.jsp?customid=110\" target=\"_blank\">消耗品库</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=408\" target=\"_blank\">消耗品入库</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=409\" target=\"_blank\">消耗品领用</a></li>" +
				"</ul>" +
				"</div>" +
				"<div class=\"yewu-item\">" +
				"<div class=\"yewu-header\">低值易耗品&nbsp;<i class=\"am-icon am-icon-chevron-right\"></i></div>" +
				"<ul>" +
				"<li class=\"firisItem\"><a>|</a><a href=\"/formmode/search/CustomSearchBySimple.jsp?customid=135\" target=\"_blank\">低值易耗品库</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=414\" target=\"_blank\">低值易耗品入库</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=416\" target=\"_blank\">低值易耗品分配</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=415\" target=\"_blank\">低值易耗品归还</a></li>" +
				"</ul>" +
				"</div>";
		try {
			response.setContentType("application/json; charset=utf-8");  
			JSONObject jsonobject=new JSONObject();
			jsonobject.put("CaiDan", gSCaiDan);
			jsonobject.put("DaoHhang", gSDaoHhang);
			response.getWriter().print(jsonobject);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	/**
	 * 资产管理-公司菜单接口
	 * @param 
	 * user	用户
	 * pageno	当前页数
	 * pagesize	每页显示数
	 */
	public void getGS(){
		String gSCaiDan = " <dl class=\"daohang-item\" data-name=\"导航\" style=\"height: 30px;margin-top:10px;\">" +
				"<dt class=\"am-accordion-title\">" +
				"<img src=\"img/dh.png\" style=\"height: 28px;margin-bottom: 5px;\" alt=\"\" >" +
				"<a style=\"font-size:24px;color:#d44632;line-height: 30px;\">导航<a/>" +
				"</dt>" +
				"</dl>" +
				"<dl class=\"am-accordion-item\" data-name=\"固定资产管理\">" +
				"<dt class=\"am-accordion-title actived\">" +
				"<img class=\"img1\" src=\"img/col01.png\" alt=\"\" height=\"25\" style=\"display: none;\"><img class=\"img2\" src=\"img/col11.png\" alt=\"\" height=\"25\" style=\"\">固定资产" +
				"</dt>" +
				"<dd class=\"am-accordion-bd am-collapse\" style=\"height: 0px;\">" +
				"<div class=\"am-accordion-content\">" +
				"<ul>" +
				"<li ><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=99\" target=\"_blank\" data-name=\"\">我的资产</a></li>" +
				"<li ><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=109\" target=\"_blank\" data-name=\"\">资产查询</a></li>" +
				"<li style=\"border-bottom:0px\"><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=102\" target=\"_blank\" data-name=\"\">已处置资产查询</a></li>" +				
				"</ul>" +
				"</div>" +
				"</dd>" +
				"</dl>" +
				"<dl class=\"am-accordion-item\" data-name=\"无形资产管理\">" +
				"<dt class=\"am-accordion-title actived\">" +
				"<img class=\"img1\" src=\"img/col02.png\" alt=\"\" height=\"25\" style=\"display: none;\"><img class=\"img2\" src=\"img/col12.png\" alt=\"\" height=\"25\" style=\"\">无形资产" +
				"</dt>" +
				"<dd class=\"am-accordion-bd am-collapse\" style=\"height: 0px;\">" +				
				"<div class=\"am-accordion-content\">" +
				"<ul>" +
				"<li ><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=98\" target=\"_blank\" data-name=\"\">我的无形资产</a></li>" +
				"<li ><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=97\" target=\"_blank\" data-name=\"\">无形资产查询</a></li>" +
				"<li style=\"border-bottom:0px\"><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=103\" target=\"_blank\" data-name=\"\">已下线资产查询</a></li>" +
				"</ul>" +
				"</div>" +
				"</dd>" +
				"</dl>" +
				"<dl class=\"am-accordion-item\" data-name=\"消耗品管理\">" +
				"<dt class=\"am-accordion-title actived\">" +
				"<img class=\"img1\" src=\"img/col03.png\" alt=\"\" height=\"25\" style=\"display: none;\"><img class=\"img2\" src=\"img/col13.png\" alt=\"\" height=\"25\" style=\"\">消耗品" +
				"</dt>" +
				"<dd class=\"am-accordion-bd am-collapse\" style=\"height: 0px;\">" +				
				"<div class=\"am-accordion-content\">" +
				"<ul>" +
				"<li ><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=107\" target=\"_blank\" data-name=\"\">消耗品名称</a></li>" +
				"<li ><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=105\" target=\"_blank\" data-name=\"\">消耗品查询</a></li>" +
				"<li style=\"border-bottom:0px\"><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=106\" target=\"_blank\" data-name=\"\">消耗品库存预警</a></li>" +
				"</ul>" +
				"</div>" +
				"</dd>" +
				"</dl>" +
				"<dl class=\"am-accordion-item\" data-name=\"低值易耗品管理\">" +
				"<dt class=\"am-accordion-title actived\">" +
				"<img class=\"img1\" src=\"img/col04.png\" alt=\"\" height=\"25\" style=\"display: none;\"><img class=\"img2\" src=\"img/col14.png\" alt=\"\" height=\"25\" style=\"\">低值易耗品" +
				"</dt>" +
				"<dd class=\"am-accordion-bd am-collapse\" style=\"height: 0px;\">" +				
				"<div class=\"am-accordion-content\">" +
				"<ul>" +
				"<li ><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=132\" target=\"_blank\" data-name=\"\">我的低值易耗品</a></li>" +
				"<li style=\"border-bottom:0px\"><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=130\" target=\"_blank\" data-name=\"\">低值易耗品查询</a></li>" +
				"</ul>" +
				"</div>" +
				"</dd>" +
				"</dl>" +
				"<dl class=\"am-accordion-item\" data-name=\"资产盘点管理\">" +
				"<dt class=\"am-accordion-title actived\">" +
				"<img class=\"img1\" src=\"img/col05.png\" alt=\"\" height=\"25\" style=\"display: none;\"><img class=\"img2\" src=\"img/col15.png\" alt=\"\" height=\"25\" style=\"\">资产盘点" +
				"</dt>" +
				"<dd class=\"am-accordion-bd am-collapse\" style=\"height: 0px;\">" +				
				"<div class=\"am-accordion-content\">" +
				"<ul>" +
				"<li ><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=110\" target=\"_blank\" data-name=\"\">固定资产标签</a></li>" +
				"<li ><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=111\" target=\"_blank\" data-name=\"\">无形资产标签</a></li>" +
				"<li ><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=133\" target=\"_blank\" data-name=\"\">低值易耗品标签</a></li>" +
				"<li ><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=112\" target=\"_blank\" data-name=\"\">固定资产盘点</a></li>" +				
				"<li ><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=116\" target=\"_blank\" data-name=\"\">无形资产盘点</a></li>" +
				"<li ><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=131\" target=\"_blank\" data-name=\"\">盘点任务</a></li>" +
				"<li style=\"border-bottom:0px\"><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=104\" target=\"_blank\" data-name=\"\">盘点记录</a></li>" +
				"</ul>" +
				"</div>" +
				"</dd>" +
				"</dl>" +
				"<dl class=\"am-accordion-item\" data-name=\"统计报表\">" +
				"<dt class=\"am-accordion-title actived\">" +
				"<img class=\"img1\" src=\"img/col06.png\" alt=\"\" height=\"25\" style=\"display: none;\"><img class=\"img2\" src=\"img/col16.png\" alt=\"\" height=\"25\" style=\"\">统计报表" +
				"</dt>" +
				"<dd class=\"am-accordion-bd am-collapse\" style=\"height: 0px;\">" +				
				"<div class=\"am-accordion-content\">" +
				"<ul>" +
				"<li ><a class=\"a1\" data-id=\"1\" href=\"#\" target=\"_blank\" data-name=\"\">报表1</a></li>" +
				"<li ><a class=\"a1\" data-id=\"1\" href=\"#\" target=\"_blank\" data-name=\"\">报表2</a></li>" +
				"<li style=\"border-bottom:0px\"><a class=\"a1\" data-id=\"1\" href=\"#\" target=\"_blank\" data-name=\"\">报表3</a></li>" +
				"</ul>" +
				"</div>" +
				"</dd>" +
				"</dl>" +
				"<dl class=\"am-accordion-item\" data-name=\"设置\">" +
				"<dt class=\"am-accordion-title actived\">" +
				"<img class=\"img1\" src=\"img/col07.png\" alt=\"\" height=\"25\" style=\"display: none;\"><img class=\"img2\" src=\"img/col17.png\" alt=\"\" height=\"25\" style=\"\">设置" +
				"</dt>" +
				"<dd class=\"am-accordion-bd am-collapse\" style=\"height: 0px;\">" +				
				"<div class=\"am-accordion-content\">" +
				"<ul>" +				
				"<li ><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=89\" target=\"_blank\" data-name=\"\">固定资产类型</a></li>" +
				"<li ><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=96\" target=\"_blank\" data-name=\"\">消耗品类型</a></li>" +
				"<li ><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=129\" target=\"_blank\" data-name=\"\">低值易耗品类型</a></li>" +
				"<li ><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=90\" target=\"_blank\" data-name=\"\">资产所在位置</a></li>" +
				"<li ><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=94\" target=\"_blank\" data-name=\"\">固定资产单位</a></li>" +
				"<li ><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=95\" target=\"_blank\" data-name=\"\">消耗品单位</a></li>" +
				"<li style=\"border-bottom:0px\"><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=134\" target=\"_blank\" data-name=\"\">资产同步日志</a></li>" +
				"</ul>" +
				"</div>" +
				"</dd>" +
				"</dl>";
		String gSDaoHhang = "<div class=\"yewu-item\">" +
				"<div class=\"yewu-header\">固 定 资 产&nbsp;&nbsp;<i class=\"am-icon am-icon-chevron-right\"></i></div>" +
				"<ul>" +
				"<li><a>|</a><a href=\"/formmode/search/CustomSearchBySimple.jsp?customid=109\" target=\"_blank\">资产库</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=399\" target=\"_blank\">验收入库</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=400\" target=\"_blank\">资产领用</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=401\" target=\"_blank\">资产归还</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=402\" target=\"_blank\">部门内变更</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=403\" target=\"_blank\">部门间调配</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=404\" target=\"_blank\">资产处置</a></li>" +
				"</ul>" +
				"</div>" +
				"<div class=\"yewu-item\">" +
				"<div class=\"yewu-header\">无 形 资 产&nbsp;&nbsp;<i class=\"am-icon am-icon-chevron-right\"></i></div>" +
				"<ul>" +
				"<li><a>|</a><a href=\"/formmode/search/CustomSearchBySimple.jsp?customid=97\" target=\"_blank\">无形资产库</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=406\" target=\"_blank\">无形资产入库</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=407\" target=\"_blank\">无形资产处置</a></li>" +
				"</ul>" +
				"</div>" +
				"<div class=\"yewu-item\">" +
				"<div class=\"yewu-header\">消 耗 品&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<i class=\"am-icon am-icon-chevron-right\"></i></div>" +
				"<ul>" +
				"<li class=\"firisItem\"><a>|</a><a href=\"/formmode/search/CustomSearchBySimple.jsp?customid=110\" target=\"_blank\">消耗品库</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=408\" target=\"_blank\">消耗品入库</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=409\" target=\"_blank\">消耗品领用</a></li>" +
				"</ul>" +
				"</div>" +
				"<div class=\"yewu-item\">" +
				"<div class=\"yewu-header\">低值易耗品&nbsp;<i class=\"am-icon am-icon-chevron-right\"></i></div>" +
				"<ul>" +
				"<li class=\"firisItem\"><a>|</a><a href=\"/formmode/search/CustomSearchBySimple.jsp?customid=130\" target=\"_blank\">低值易耗品库</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=414\" target=\"_blank\">低值易耗品入库</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=416\" target=\"_blank\">低值易耗品分配</a></li>" +
				"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=415\" target=\"_blank\">低值易耗品归还</a></li>" +
				"</ul>" +
				"</div>" +
				"<div class=\"yewu-item\">" +
				"<div class=\"yewu-header\">资 产 盘 点&nbsp;&nbsp;<i class=\"am-icon am-icon-chevron-right\"></i></div>" +
				"<ul>" +
				"<li class=\"firisItem\"><a>|</a><a href=\"/formmode/search/CustomSearchBySimple.jsp?customid=110\" target=\"_blank\">固定资产标签</a></li>" +
				"<li><a>|</a><a href=\"/formmode/search/CustomSearchBySimple.jsp?customid=111\" target=\"_blank\">无形资产标签</a></li>" +
				"<li><a>|</a><a href=\"/formmode/search/CustomSearchBySimple.jsp?customid=133\" target=\"_blank\">低值易耗品标签</a></li>" +				
				"<li><a>|</a><a href=\"/formmode/search/CustomSearchBySimple.jsp?customid=112\" target=\"_blank\">固定资产盘点</a></li>" +
				"<li><a>|</a><a href=\"/formmode/search/CustomSearchBySimple.jsp?customid=116\" target=\"_blank\">无形资产盘点</a></li>" +
				"</ul>" +
				"</div>";
		try {
			response.setContentType("application/json; charset=utf-8");  
			JSONObject jsonobject=new JSONObject();
			jsonobject.put("CaiDan", gSCaiDan);
			jsonobject.put("DaoHhang", gSDaoHhang);
			response.getWriter().print(jsonobject);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}