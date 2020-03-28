package weaver.sysinterface;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


import weaver.conn.RecordSet;
import weaver.general.Util;
import com.weaver.formmodel.util.DateHelper;

public class ZcsysAction extends HttpServlet{
	private static final long serialVersionUID = 1L;

	private HttpServletRequest request;
	
	private HttpServletResponse response;
	
	public void doGet(HttpServletRequest request, HttpServletResponse response){    
		this.request = request;
		this.response = response;
		String action = Util.null2String(request.getParameter("action"));
		if("jdzcsys".equals(action)){//金蝶资产同步接口
				jdzcsys();
		}else if("getzcdata".equals(action)){//提取资产数据接口
				getzcdata();
		}
		
	}
	
	/**
	 * 获取数据共享权限 sql
	 * @param formmodeid	建模id
	 * @param userid	用户id
	 * @return
	 */
	/*public String getShareSql(int formmodeid,int userid){
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
	*/
	
	
	
	/**
	 * 金蝶资产同步接口
	 *
	 */
	private void jdzcsys(){
		String year = Util.null2String(request.getParameter("year"));
		String moon = Util.null2String(request.getParameter("moon"));
		String cxqj="";
		if(moon.indexOf("0")==0){
			String period=moon.substring(1);
			cxqj=year+"-"+period;
		}else{
			cxqj=year+"-"+moon;
		}
		RecordSet rs = new RecordSet();
		RecordSet rs2 = new RecordSet();
		try {
			rs.executeSql("declare @return_value int   EXEC @return_value=[dbo].[falist_sseinfo] @year="+year+",@period="+moon+",@cardnums=N'all'   select 'Return Value'=@return_value ");
			rs2.executeSql("select * from uf_am_gdzcdatainfo where periodname='"+cxqj+"' ");
			if(rs2.next()){
				rs2.executeSql("delete from uf_am_gdzcdatainfo where periodname='"+cxqj+"' ");
			}
			while(rs.next()){
				//String periodname=Util.null2String(rs.getString("查询期间"));
				String assetno=Util.null2String(rs.getString("卡片编码"));
				String assetname=Util.null2String(rs.getString("卡片名称"));
				String accountdate=Util.null2String(rs.getString("入账日期"));
				String assetoriginalcost=Util.null2String(rs.getString("购进原值"));
				String depreendbalancefor=Util.null2String(rs.getString("累计折旧"));
				String endnetvalue=Util.null2String(rs.getString("资产净值"));
				String syndatetime=DateHelper.getCurDateTime();
				if(!"".equals(assetno)){
					rs2.executeSql("insert into uf_am_gdzcdatainfo(periodname,assetno,assetname,accountdate,assetoriginalcost," +
							"depreendbalancefor,endnetvalue,syndatetime) values('"+cxqj+"','"+assetno+"','"+assetname+"'," +
									"'"+accountdate+"','"+assetoriginalcost+"','"+depreendbalancefor+"','"+endnetvalue+"','"+syndatetime+"')");
				}
				
			}
			
			
			
				response.setContentType("application/json; charset=utf-8");  
				response.getWriter().print(1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		
	}
	
	
	/**
	 * 提取资产数据接口
	 *
	 */
	private void getzcdata(){
		String year = Util.null2String(request.getParameter("year"));
		String moon = Util.null2String(request.getParameter("moon"));
		String assetnos=Util.null2String(request.getParameter("assetnos"));
		RecordSet rs = new RecordSet();
		JSONObject jsonobjectall=new JSONObject();
		try {
			rs.executeSql("declare @return_value int   EXEC @return_value=[dbo].[falist_sseinfo] @year="+year+",@period="+moon+",@cardnums=N'"+assetnos+"'   select 'Return Value'=@return_value ");
			while(rs.next()){
				JSONArray jsonarray=new JSONArray();
				JSONObject jsonobject=new JSONObject();
				//String periodname=Util.null2String(rs.getString("查询期间"));
				String assetno=Util.null2String(rs.getString("卡片编码"));
				//String assetname=Util.null2String(rs.getString("卡片名称"));
				//String accountdate=Util.null2String(rs.getString("入账日期"));
				String assetoriginalcost=Util.null2String(rs.getString("购进原值"));
				String depreendbalancefor=Util.null2String(rs.getString("累计折旧"));
				String endnetvalue=Util.null2String(rs.getString("资产净值"));
				//String syndatetime=DateHelper.getCurDateTime();
				if(!"".equals(assetno)){
					jsonobject.put("assetoriginalcost", assetoriginalcost);
					jsonobject.put("depreendbalancefor", depreendbalancefor);
					jsonobject.put("endnetvalue", endnetvalue);
					jsonarray.add(jsonobject);
					jsonobjectall.put(assetno, jsonarray);
				}
				
			}
			
			
			
				response.setContentType("application/json; charset=utf-8");  
				response.getWriter().print(jsonobjectall.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		
	}
}
