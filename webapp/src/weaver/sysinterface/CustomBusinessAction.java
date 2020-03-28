package weaver.sysinterface;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.weaver.formmodel.mobile.manager.MobileUserInit;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import weaver.conn.RecordSet;
import weaver.general.Util;
import weaver.hrm.User;
public class CustomBusinessAction extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private HttpServletRequest request;
	
	private HttpServletResponse response;
	
	public void doGet(HttpServletRequest request, HttpServletResponse response){    
		this.request = request;
		this.response = response;
		try {
			String action = Util.null2String(request.getParameter("action"));			
			User user = MobileUserInit.getUser(request, response);
			if(user == null){
				JSONObject checkUser = new JSONObject();
				checkUser.put("msgstatus", "0");
				checkUser.put("msg", "服务器端重置了登录信息，请重新登录");
				response.setContentType("application/json; charset=utf-8");  
				response.getWriter().print(checkUser.toString());
				return;
			}
			if("CustomGetBusiness".equals(action)){//获取客户业务信息
				CustomGetBusiness(user);
			}else if("CustomGetContract".equals(action)){//获取客户业务合同信息
				CustomGetContract(user);
			}else if("BusinessGetData".equals(action)){//获取业务相关统计数据
				BusinessGetData(user);
			}else if("CustomGetName".equals(action)){//获取客户信息
				CustomGetName(user);
			}else if("ContractGetInfo".equals(action)){//获取客户、业务、合同信息
				ContractGetInfo(user);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**获取客户业务信息
	 * params action
	 * billid	客户id	 
	 * @throws Exception 
	 */
	
	public void CustomGetBusiness(User user) throws Exception{
		String billid = Util.null2String(request.getParameter("billid"));
		String userid = String.valueOf(user.getUID());
		RecordSet rs = new RecordSet();
		int modeId=127;
		int formId=-263;
		String busid="";
		String busname="";
		String ifqx="";
		String htmlStr="";
		String sql = " select b.id,c.name,d.business from uf_crm_customerinfo a" +
				" join uf_crm_custbusiness b on a.id=b.customer" +
				" join uf_crm_businessinfo c on b.business=c.id" +
				" left join Matrixtable_7 d on b.business=d.business and" +
				" ("+userid+" in(select resourceid from hrmrolemembers where  roleid=161 or roleid=2)" +
				" or "+userid+" in (select F1 from vfn_Splitstr(d.businessleader,','))" +
				" or "+userid+" in (select F1 from vfn_Splitstr(d.businessmanager,','))" +
				" or ("+userid+" in (select F1 from vfn_Splitstr(d.regionleader,',')) and b.regionalcenter is not null)" +
				" or "+userid+" =b.accountmanager)" +
				" where b.customer='"+billid+"' order by d.business desc,c.dsporder ";
		rs.executeSql(sql);		
		while(rs.next()){
			busid = Util.null2String(rs.getString("id"));	
	        busname = Util.null2String(rs.getString("name"));
	        ifqx = Util.null2String(rs.getString("business"));
	        if(!ifqx.equals("")){
	          htmlStr=htmlStr+"<a style=\"height:40px;line-height:40px;\" href=\"/formmode/view/AddFormMode.jsp?type=0&modeId="+modeId+"&formId="+formId+"&billid="+busid+"\" target=\"_blank\">"+busname+"</a><br>";	
	        }else{
	          htmlStr=htmlStr+"<a style=\"height:40px;line-height:40px;color:#545454;\">"+busname+"</a><br>";	
	        }
		}
		
		JSONArray jsonArray = new JSONArray();
		JSONObject json = new JSONObject();
		json.put("htmlStr",htmlStr);
		jsonArray.add(json);
		
		JSONObject result = new JSONObject();		
		result.put("datas", jsonArray.toString());
		try {
			response.setContentType("application/json; charset=utf-8");  
			response.getWriter().print(result.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**获取客户业务合同信息
	 * params action
	 * billid	客户业务id	 
	 * @throws Exception 
	 */
	
	public void CustomGetContract(User user) throws Exception{
		String billid = Util.null2String(request.getParameter("billid"));
		String userid = String.valueOf(user.getUID());
		RecordSet rs = new RecordSet();
		int modeId=112;
		int formId=-268;
		String contractid="";
		String contractname="";
		String contractno="";
		String htmlStr="";
		String sql = " select b.id,b.contractname,b.contractno,b.signdate from uf_crm_custbusiness a,uf_crm_contract b where a.customer=b.customer and a.business=b.business and b.status=0  and a.id='"+billid+"' order by b.signdate desc";
		rs.executeSql(sql);		
		while(rs.next()){
			contractid = Util.null2String(rs.getString("id"));	
			contractname = Util.null2String(rs.getString("contractname"));
			contractno = Util.null2String(rs.getString("contractno"));
	        htmlStr=htmlStr+"<a style=\"height:40px;line-height:40px;\" href=\"/formmode/view/AddFormMode.jsp?type=0&modeId="+modeId+"&formId="+formId+"&billid="+contractid+"\" target=\"_blank\">"+contractno+"</a><br>";			
		}
		
		JSONArray jsonArray = new JSONArray();
		JSONObject json = new JSONObject();
		json.put("htmlStr",htmlStr);
		jsonArray.add(json);
		
		JSONObject result = new JSONObject();		
		result.put("datas", jsonArray.toString());
		try {
			response.setContentType("application/json; charset=utf-8");  
			response.getWriter().print(result.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**获取客户信息
	 * params action
	 * billid	客户id	 
	 * @throws Exception 
	 */
	
	public void CustomGetName(User user) throws Exception{
		String billid = Util.null2String(request.getParameter("billid"));		
		RecordSet rs = new RecordSet();		
		String customername="";
		String htmlStr="";
		String sql = " select name from uf_crm_customerinfo where id='"+billid+"' ";
		rs.executeSql(sql);		
		if(rs.next()){
			customername = Util.null2String(rs.getString("name"));
			htmlStr="<a href=\"/formmode/view/AddFormMode.jsp?type=0&modeId=102&formId=-256&billid="+billid+"\" target=\"_blank\">"+customername+"</a>";
		}
		
		JSONArray jsonArray = new JSONArray();
		JSONObject json = new JSONObject();
		json.put("htmlStr",htmlStr);
		jsonArray.add(json);
		
		JSONObject result = new JSONObject();		
		result.put("datas", jsonArray.toString());
		try {
			response.setContentType("application/json; charset=utf-8");  
			response.getWriter().print(result.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**获取客户、业务、合同信息
	 * params action	  
	 * @throws Exception 
	 */
	
	public void ContractGetInfo(User user) throws Exception{
		String customerid = Util.null2String(request.getParameter("customerid"));
		String businessid = Util.null2String(request.getParameter("businessid"));
		String contractid = Util.null2String(request.getParameter("contractid"));
		RecordSet rs = new RecordSet();	
		RecordSet rs1 = new RecordSet();	
		RecordSet rs2 = new RecordSet();	
		String customername="";
		String businessname="";
		String contractname="";
		
		String customerHtml="";
		String businessHtml="";
		String contractHtml="";
		String sql = " select name from uf_crm_customerinfo where id='"+customerid+"' ";
		rs.executeSql(sql);		
		if(rs.next()){
			customername = Util.null2String(rs.getString("name"));
			customerHtml="<a href=\"/formmode/view/AddFormMode.jsp?type=0&modeId=102&formId=-256&billid="+customerid+"\" target=\"_blank\">"+customername+"</a>";
		}
		
		sql = " select name from uf_crm_businessinfo where id='"+businessid+"' ";
		rs1.executeSql(sql);		
		if(rs1.next()){
			businessname = Util.null2String(rs1.getString("name"));
			businessHtml="<a href=\"/formmode/view/AddFormMode.jsp?type=0&modeId=104&formId=-257&billid="+businessid+"\" target=\"_blank\">"+businessname+"</a>";
		}
		
		if(!contractid.equals("")){
		sql = " select contractno from uf_crm_contract where id='"+contractid+"' ";
		rs2.executeSql(sql);		
		if(rs2.next()){
			contractname = Util.null2String(rs2.getString("contractno"));
			contractHtml="<a href=\"/formmode/view/AddFormMode.jsp?type=0&modeId=112&formId=-268&billid="+contractid+"\" target=\"_blank\">"+contractname+"</a>";
		 }
		}
		JSONArray jsonArray = new JSONArray();
		JSONObject json = new JSONObject();
		json.put("customerHtml",customerHtml);
		json.put("businessHtml",businessHtml);
		json.put("contractHtml",contractHtml);
		jsonArray.add(json);
		
		JSONObject result = new JSONObject();		
		result.put("datas", jsonArray.toString());
		try {
			response.setContentType("application/json; charset=utf-8");  
			response.getWriter().print(result.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**获取客户业务合同信息
	 * params action
	 * billid	客户业务id	 
	 * @throws Exception 
	 */
	
	public void BusinessGetData(User user) throws Exception{
		String billid = Util.null2String(request.getParameter("billid"));
		String userid = String.valueOf(user.getUID());
		RecordSet rs = new RecordSet();		
		String customertotal="";//总客户数
		String zycustomertotal="";//在约客户数
		String contracttotal="";//合同数
		String monthysmoney="";//应收金额（当月）
		String yearysmoney="";//应收金额（当年）
		String monthinvoicemoney="";//开票金额（当月）
		String yearinvoicemoney="";//开票金额（当年）
		String monthproceedsmoney="";//收款金额（当月）
		String yearproceedsmoney="";//收款金额（当年）
		String monthcall="";//走访客户数（当月）
		String yearcall="";//走访客户数（当年）
		String sql ="";	
		
		//总客户数
		sql = " select count(1) as customertotal from uf_crm_custbusiness where business='"+billid+"'";
		rs.executeSql(sql);		
		if(rs.next()){
			customertotal = Util.null2String(rs.getString("customertotal"));	        
		}
		//在约客户数
		sql = "select count(1) as zycustomertotal from uf_crm_customerinfo where " +
				"id in(select customer from (select customer,convert(datetime,cast(year(startdate) as varchar)+'-'+cast(month(startdate) as varchar)+'-1',101) as t1,convert(datetime,cast(year(enddate)as varchar)+'-'+cast(month(enddate)as varchar)+'-1',101) as t2,convert(datetime,cast(year(GETDATE()) as varchar)+'-'+cast(month(GETDATE()) as varchar)+'-1',101) as t3  from uf_crm_contract where business='"+billid+"' and status=0 ) t  where t3  between t1 and t2)";
		rs.executeSql(sql);		
		if(rs.next()){
			zycustomertotal = Util.null2String(rs.getString("zycustomertotal"));	        
		}
		//合同数
		sql = "select count(1) as contracttotal from uf_crm_contract where business='"+billid+"' and status=0";
		rs.executeSql(sql);		
		if(rs.next()){
			contracttotal = Util.null2String(rs.getString("contracttotal"));	        
		}
		//应收金额（当月）
		sql = "select isnull(sum(currentmoney),0) as ysmoney from  uf_crm_orderinfo  where business='"+billid+"' and  year(GETDATE())=year(orderdate) and month(GETDATE())=month(orderdate)";		
		rs.executeSql(sql);		
		if(rs.next()){
			monthysmoney = Util.null2String(rs.getString("ysmoney"));	        
		}
		//应收金额（当年）
		sql = "select isnull(sum(currentmoney),0) as ysmoney from uf_crm_orderinfo where business='"+billid+"' and  year(GETDATE())=year(orderdate) and month(GETDATE())>=month(orderdate)";
		rs.executeSql(sql);		
		if(rs.next()){
			yearysmoney = Util.null2String(rs.getString("ysmoney"));	        
		}
		//开票金额（当月）
		sql = "select isnull(sum(a.invoicemoney),0) as invoicemoney from uf_crm_invoiceinfo a,uf_crm_orderinfo b where a.ordernumber=b.id and b.business='"+billid+"' and  year(GETDATE())=year(a.invoicedate) and month(GETDATE())=month(a.invoicedate)";
		rs.executeSql(sql);		
		if(rs.next()){
			monthinvoicemoney = Util.null2String(rs.getString("invoicemoney"));	        
		}
		//开票金额（当年）
		sql = "select isnull(sum(a.invoicemoney),0) as invoicemoney from uf_crm_invoiceinfo a,uf_crm_orderinfo b where a.ordernumber=b.id and b.business='"+billid+"' and  year(GETDATE())=year(a.invoicedate) and month(GETDATE())>=month(a.invoicedate)";
		rs.executeSql(sql);		
		if(rs.next()){
			yearinvoicemoney = Util.null2String(rs.getString("invoicemoney"));	        
		}		
		//收款金额（当月）
		sql = "select isnull(sum(a.collectmoney),0) as proceedsmoney from uf_crm_collectinfo a,uf_crm_orderinfo b where a.ordernumber=b.id and b.business='"+billid+"' and  year(GETDATE())=year(a.collectdate) and month(GETDATE())=month(a.collectdate)";
		rs.executeSql(sql);		
		if(rs.next()){
			monthproceedsmoney = Util.null2String(rs.getString("proceedsmoney"));	        
		}
		//收款金额（当年）
		sql = "select isnull(sum(a.collectmoney),0) as proceedsmoney from uf_crm_collectinfo a,uf_crm_orderinfo b where a.ordernumber=b.id and b.business='"+billid+"' and  year(GETDATE())=year(a.collectdate) and month(GETDATE())>=month(a.collectdate)";
		rs.executeSql(sql);		
		if(rs.next()){
			yearproceedsmoney = Util.null2String(rs.getString("proceedsmoney"));	        
		}
		//走访客户数（当月）
		sql = "select count(1) as monthcall from uf_crm_customerinfo where id in(select customer from uf_crm_calllog where business='"+billid+"' and  year(GETDATE())=year(linkdate) and month(GETDATE())=month(linkdate))";
		rs.executeSql(sql);		
		if(rs.next()){
			monthcall = Util.null2String(rs.getString("monthcall"));	        
		}
		//走访客户数（当年）
		sql = "select count(1) as yearcall from uf_crm_customerinfo where id in(select customer from uf_crm_calllog where business='"+billid+"' and  year(GETDATE())=year(linkdate) and month(GETDATE())>=month(linkdate))";
		rs.executeSql(sql);		
		if(rs.next()){
			yearcall = Util.null2String(rs.getString("yearcall"));	        
		}		
		
		JSONArray jsonArray = new JSONArray();
		JSONObject json = new JSONObject();
		json.put("customertotal",customertotal);
		json.put("zycustomertotal",zycustomertotal);
		json.put("contracttotal",contracttotal);
		json.put("monthysmoney",monthysmoney);
		json.put("yearysmoney",yearysmoney);
		json.put("monthinvoicemoney",monthinvoicemoney);
		json.put("yearinvoicemoney",yearinvoicemoney);
		json.put("monthproceedsmoney",monthproceedsmoney);
		json.put("yearproceedsmoney",yearproceedsmoney);
		json.put("monthcall",monthcall);
		json.put("yearcall",yearcall);
		jsonArray.add(json);
		
		JSONObject result = new JSONObject();		
		result.put("datas", jsonArray.toString());
		try {
			response.setContentType("application/json; charset=utf-8");  
			response.getWriter().print(result.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
