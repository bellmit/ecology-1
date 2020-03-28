package weaver.sysinterface;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import weaver.conn.RecordSet;
import weaver.general.Util;
import weaver.hrm.User;

import com.weaver.formmodel.mobile.manager.MobileUserInit;

public class GetLabel extends HttpServlet{
	private static final long serialVersionUID = 1L;

	private HttpServletRequest request;

	private HttpServletResponse response;

	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
		try {
			String action = Util.null2String(request.getParameter("action"));
			User user = MobileUserInit.getUser(request, response);
			if (user == null) {
				JSONObject checkUser = new JSONObject();
				checkUser.put("msgstatus", "0");
				checkUser.put("msg", "服务器端重置了登录信息，请重新登录");
				response.setContentType("application/json; charset=utf-8");
				response.getWriter().print(checkUser.toString());
				return;
			}
			if ("getLabel".equals(action)) { // 处理接口
				getLabel();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void getLabel() {
		RecordSet rs=new RecordSet();
		RecordSet rs1=new RecordSet();
		String pageCountStr = Util.null2String(request.getParameter("count"));
		String pageStr = Util.null2String(request.getParameter("page"));
		int pageCount=0;
		if(pageCountStr!=null||pageCountStr!=""){
			pageCount=Integer.parseInt(pageCountStr);
		}
		int page=0;
		if(pageStr!=null||pageStr!=""){
			page=Integer.parseInt(pageStr);
		}
		String sql="select count(*) as count  from  uf_LabelBank";
		rs.executeSql(sql);
		int totalcount = 0;
		if(rs.next()){
			totalcount=rs.getInt("count");
		}
		int totalpage=0;
		if(totalcount%pageCount==0){
			totalpage=totalcount/pageCount;
		}else{
			totalpage=totalcount/pageCount+1;
		}
		JSONObject result = new JSONObject();
		if(page<=totalpage){
			sql="select top "+pageCount+"labelname from uf_LabelBank where id not in(select top ("+pageCount+"*("+page+"-1))id from uf_LabelBank)";
			rs1.executeSql(sql);
			JSONArray jsonArrays = new JSONArray();
			while(rs1.next()){
				JSONObject json = new JSONObject();
				String labelname=Util.null2String(rs1.getString("labelname"));
				json.put("labelname", labelname);
				jsonArrays.add(json);
			}
			result.put("name", jsonArrays.toString());
			
		}else{
			page=1;
			sql="select top "+pageCount+"labelname from uf_LabelBank where id not in(select top ("+pageCount+"*("+page+"-1))id from uf_LabelBank)";
			rs1.executeSql(sql);
			JSONArray jsonArrays = new JSONArray();
			while(rs1.next()){
				JSONObject json = new JSONObject();
				String labelname=Util.null2String(rs1.getString("labelname"));
				json.put("labelname", labelname);
				jsonArrays.add(json);
			}
			result.put("result", jsonArrays.toString());
			result.put("name", "1");
		}

		try {
			response.setContentType("application/json; charset=utf-8");
			response.getWriter().print(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
