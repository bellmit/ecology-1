package weaver.sysinterface;

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

public class SearchByName extends HttpServlet{
	
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
			if("supplierName".equals(action)){//供应商搜索接口
				getSupplierNameSearch(user);
			}else if("getContractName".equals(action)){//合同搜索接口
				getContractNameSearch(user);
			}else if("getPaymentName".equals(action)){//付款搜索接口
				getPaymentNameSearch(user);
			}else if("getComparisonName".equals(action)){//合作搜索接口
				getComparisonNameSearch(user);
			}
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
	/**
	 * 供应商搜索接口
	 * @param 
	 * user	用户
	 * pageno	当前页数
	 * pagesize	每页显示数
	 */
	private void getSupplierNameSearch(User user) {
		long startTime = System.currentTimeMillis();
		String searchStr = Util.null2String(request.getParameter("searchStr"));//搜索的字符串
		//System.out.println("供应商进入的值："+searchStr);
		
		String userid = String.valueOf(user.getUID());
			RecordSet rs = new RecordSet();
			RecordSet rs2 = new RecordSet();
			int recordCount=0;//总记录数
			int pageno = Util.getIntValue(request.getParameter("pageno"),1);//当前页
			int pagesize = Util.getIntValue(request.getParameter("pagesize"),10);//每页显示数
			//权限过滤
			String rightSql = getShareSql(70,Integer.parseInt(userid));
			//System.out.println(rightSql);
			String selSql = "select count(1) from uf_spm_supplier  where status in('1','4') and suppliername like '%"+searchStr+"%' and 1=1 and exists (select 1 from "+rightSql+" m where m.sourceid=uf_spm_supplier.id)";
			rs.executeSql(selSql);
			//String rightSql="select * from uf_spm_supplier where suppliername like '%"+searchStr+"%'";
			//String selSql="select count(1) from uf_spm_supplier where suppliername like '%"+searchStr+"%'";
			//rs.executeSql(selSql);
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
			selSql = "select top " + iNextNum +" j.id,j.suppliername,j.supplierno,stuff((select ','+b.name from [dbo].[vfn_Splitstr]((select suppliertype from  uf_spm_supplier where id=j.id),',') as a,uf_spm_suppliertype b where a.F1=b.id for xml path('')),1,1,'') as typename,stuff((select ','+b.name from [dbo].[vfn_Splitstr]((select suppliertypes from  uf_spm_supplier where id=j.id),',') as a,uf_spm_suppliercate b where a.F1=b.id for xml path('')),1,1,'') as typenames,j.createdate from uf_spm_supplier j where j.status in('1','4') and j.suppliername like '%"+searchStr+"%' and exists (select 1 from "+rightSql+" m where m.sourceid=j.id) order by j.createdate desc";
			//将上面得到的数据取按顺序排序的ipageset条数据顺序排序
			selSql = "select top " + ipageset +" t1.* from (" + selSql + ") t1 order by t1.createdate asc";
			//将得到的数据倒序排序显示
			selSql = "select t2.* from (" + selSql + ") t2 order by t2.createdate desc";
			
			
			JSONArray jsonArray = new JSONArray();
			String img="";
			if((recordCount - iNextNum + pagesize)>0){
				rs.executeSql(selSql);
				while(rs.next()){
					String id=Util.null2String(rs.getString("id"));
					rs2.executeSql("select * from uf_spm_enshrine where supperId="+id+" and enshrinetor="+userid+" ");
					if(rs2.next()){
						img="img/shoucang2.png";
					}else{
						img="img/shoucang.png";
					}
					JSONObject json = new JSONObject();
					json.put("id", id);
					json.put("suppliername", Util.null2String(rs.getString("suppliername")));
					json.put("collection", img);
					json.put("supplierno", Util.null2String(rs.getString("supplierno")));
					json.put("suppliertype", Util.null2String(rs.getString("typename")));
					json.put("suppliertypes", Util.null2String(rs.getString("typenames")));
					jsonArray.add(json);
				}
			}
			long endTime = System.currentTimeMillis();
			JSONObject result = new JSONObject();
			result.put("totalSize", recordCount);
			result.put("datas", jsonArray.toString());
			result.put("costTime", endTime-startTime);
			try {
				response.setContentType("application/json; charset=utf-8");  
				response.getWriter().print(result.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		
	}
	
	/**
	 * 合同搜索接口
	 * @param 
	 * user	用户
	 * pageno	当前页数
	 * pagesize	每页显示数
	 */
	private void getContractNameSearch(User user) {
		long startTime = System.currentTimeMillis();
		String searchStr = Util.null2String(request.getParameter("searchStr"));//搜索的字符串
		//System.out.println("合同进入的值："+searchStr);
		
		String userid = String.valueOf(user.getUID());
			RecordSet rs = new RecordSet();
			int recordCount=0;//总记录数
			int pageno = Util.getIntValue(request.getParameter("pageno"),1);//当前页
			int pagesize = Util.getIntValue(request.getParameter("pagesize"),10);//每页显示数
			//权限过滤
			String rightSql = getShareSql(73,Integer.parseInt(userid));
			//System.out.println(rightSql);
			String selSql = "select count(1) from uf_spm_contract  where (select suppliername from uf_spm_supplier k where k.status in('1','4') and k.id=uf_spm_contract.supplier) like '%"+searchStr+"%' and 1=1 and exists (select 1 from "+rightSql+" m where m.sourceid=uf_spm_contract.id)";
			rs.executeSql(selSql);
			//String rightSql="select * from uf_spm_contract where supplier like '%"+searchStr+"%'";
			//String selSql="select count(1) from uf_spm_contract where supplier like '%"+searchStr+"%'";
			//rs.executeSql(selSql);
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
			selSql = "select top " + iNextNum +" id,(select suppliername from uf_spm_supplier k where k.id=uf_spm_contract.supplier) as supname,contractname,money,signdate,begindate,enddate from uf_spm_contract where (select suppliername from uf_spm_supplier k where k.status in('1','4') and k.id=uf_spm_contract.supplier) like '%"+searchStr+"%' and 1=1 and exists (select 1 from "+rightSql+" m where m.sourceid=uf_spm_contract.id) order by signdate desc";
			//将上面得到的数据取按顺序排序的ipageset条数据顺序排序
			selSql = "select top " + ipageset +" t1.* from (" + selSql + ") t1 order by t1.signdate asc";
			//将得到的数据倒序排序显示
			selSql = "select t2.* from (" + selSql + ") t2 order by t2.signdate desc";
			
			JSONArray jsonArray = new JSONArray();
			if((recordCount - iNextNum + pagesize)>0){
				rs.executeSql(selSql);
				while(rs.next()){
					JSONObject json = new JSONObject();
					json.put("id", Util.null2String(rs.getString("id")));
					json.put("supplier", Util.null2String(rs.getString("supname")));
					json.put("contractname", Util.null2String(rs.getString("contractname")));
					json.put("money", Util.null2String(rs.getString("money")));
					json.put("signdate", Util.null2String(rs.getString("signdate")));
					json.put("begindate", Util.null2String(rs.getString("begindate")));
					json.put("enddate", Util.null2String(rs.getString("enddate")));
					jsonArray.add(json);
				}
			}
			long endTime = System.currentTimeMillis();
			JSONObject result = new JSONObject();
			result.put("totalSize", recordCount);
			result.put("datas", jsonArray.toString());
			result.put("costTime", endTime-startTime);
			try {
				response.setContentType("application/json; charset=utf-8");  
				response.getWriter().print(result.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		
	}
	
	/**
	 * 付款搜索接口
	 * @param 
	 * user	用户
	 * pageno	当前页数
	 * pagesize	每页显示数
	 */
	private void getPaymentNameSearch(User user) {
		long startTime = System.currentTimeMillis();
		String searchStr = Util.null2String(request.getParameter("searchStr"));//搜索的字符串
		//System.out.println("付款进入的值："+searchStr);
		
		String userid = String.valueOf(user.getUID());
			RecordSet rs = new RecordSet();
			int recordCount=0;//总记录数
			int pageno = Util.getIntValue(request.getParameter("pageno"),1);//当前页
			int pagesize = Util.getIntValue(request.getParameter("pagesize"),10);//每页显示数
			//权限过滤
			String rightSql = getShareSql(74,Integer.parseInt(userid));
			//System.out.println(rightSql);
			String selSql = "select count(1) from uf_spm_payinfo  where (select suppliername from uf_spm_supplier k where k.status in('1','4') and k.id=uf_spm_payinfo.supplier) like '%"+searchStr+"%' and 1=1 and exists (select 1 from "+rightSql+" m where m.sourceid=uf_spm_payinfo.id)";
			rs.executeSql(selSql);
			//String rightSql="select * from uf_spm_payinfo where supplier like '%"+searchStr+"%'";
			//String selSql="select count(1) from uf_spm_payinfo where supplier like '%"+searchStr+"%'";
			//rs.executeSql(selSql);
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
			selSql = "select top " + iNextNum +" id,(select suppliername from uf_spm_supplier k where k.id=uf_spm_payinfo.supplier) as supname,paycontent,money,paydate,paytitle from uf_spm_payinfo where (select suppliername from uf_spm_supplier k where k.status in('1','4') and k.id=uf_spm_payinfo.supplier) like '%"+searchStr+"%' and 1=1 and exists (select 1 from "+rightSql+" m where m.sourceid=uf_spm_payinfo.id) order by paydate desc";
			//将上面得到的数据取按顺序排序的ipageset条数据顺序排序
			selSql = "select top " + ipageset +" t1.* from (" + selSql + ") t1 order by t1.paydate asc";
			//将得到的数据倒序排序显示
			selSql = "select t2.* from (" + selSql + ") t2 order by t2.paydate desc";
			
			JSONArray jsonArray = new JSONArray();
			if((recordCount - iNextNum + pagesize)>0){
				rs.executeSql(selSql);
				while(rs.next()){
					JSONObject json = new JSONObject();
					json.put("id", Util.null2String(rs.getString("id")));
					json.put("supplier", Util.null2String(rs.getString("supname")));
					json.put("paycontent", Util.null2String(rs.getString("paycontent")));
					json.put("money", Util.null2String(rs.getString("money")));
					json.put("paydate", Util.null2String(rs.getString("paydate")));
					json.put("paytitle", Util.null2String(rs.getString("paytitle")));
					jsonArray.add(json);
				}
			}
			long endTime = System.currentTimeMillis();
			JSONObject result = new JSONObject();
			result.put("totalSize", recordCount);
			result.put("datas", jsonArray.toString());
			result.put("costTime", endTime-startTime);
			try {
				response.setContentType("application/json; charset=utf-8");  
				response.getWriter().print(result.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		
	}
	
	/**
	 * 合作搜索接口
	 * @param 
	 * user	用户
	 * pageno	当前页数
	 * pagesize	每页显示数
	 */
	private void getComparisonNameSearch(User user) {
		long startTime = System.currentTimeMillis();
		String searchStr = Util.null2String(request.getParameter("searchStr"));//搜索的字符串
		//System.out.println("比选进入的值："+searchStr);
		
		String userid = String.valueOf(user.getUID());
			RecordSet rs = new RecordSet();
			int recordCount=0;//总记录数
			int pageno = Util.getIntValue(request.getParameter("pageno"),1);//当前页
			int pagesize = Util.getIntValue(request.getParameter("pagesize"),10);//每页显示数
			//权限过滤
			String rightSql = getShareSql(72,Integer.parseInt(userid));
			//System.out.println(rightSql);
			String selSql = "select count(1) from uf_spm_comparison  where (select suppliername from uf_spm_supplier k where k.status in('1','4') and k.id=uf_spm_comparison.supplier) like '%"+searchStr+"%' and 1=1 and exists (select 1 from "+rightSql+" m where m.sourceid=uf_spm_comparison.id)";
			rs.executeSql(selSql);
			//String rightSql="select * from uf_spm_comparison where supplier like '%"+searchStr+"%'";
			//String selSql="select count(1) from uf_spm_comparison where supplier like '%"+searchStr+"%'";
			//rs.executeSql(selSql);
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
			selSql = "select top " + iNextNum +" id,(select suppliername from uf_spm_supplier k where k.id=uf_spm_comparison.supplier)as supname,projectname,(select selectname from workflow_SelectItem l where fieldid='10980' and l.selectvalue=uf_spm_comparison.sfsupplier) as bxresult,applyflow,createdate from uf_spm_comparison where (select suppliername from uf_spm_supplier k where k.status in('1','4') and k.id=uf_spm_comparison.supplier) like '%"+searchStr+"%' and 1=1 and exists (select 1 from "+rightSql+" m where m.sourceid=uf_spm_comparison.id) order by createdate desc";
			//将上面得到的数据取按顺序排序的ipageset条数据顺序排序
			selSql = "select top " + ipageset +" t1.* from (" + selSql + ") t1 order by t1.createdate asc";
			//将得到的数据倒序排序显示
			selSql = "select t2.* from (" + selSql + ") t2 order by t2.createdate desc";
			
			JSONArray jsonArray = new JSONArray();
			if((recordCount - iNextNum + pagesize)>0){
				rs.executeSql(selSql);
				while(rs.next()){
					JSONObject json = new JSONObject();
					json.put("id", Util.null2String(rs.getString("id")));
					json.put("supplier", Util.null2String(rs.getString("supname")));
					json.put("projectname", Util.null2String(rs.getString("projectname")));
					json.put("sfsupplier", Util.null2String(rs.getString("bxresult")));
					json.put("createdate", Util.null2String(rs.getString("createdate")));
					json.put("applyflow", Util.null2String(rs.getString("applyflow")));
					jsonArray.add(json);
				}
			}
			long endTime = System.currentTimeMillis();
			JSONObject result = new JSONObject();
			result.put("totalSize", recordCount);
			result.put("datas", jsonArray.toString());
			result.put("costTime", endTime-startTime);
			try {
				response.setContentType("application/json; charset=utf-8");  
				response.getWriter().print(result.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		
	}

}
