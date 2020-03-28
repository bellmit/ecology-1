package weaver.sysinterface;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.star.lib.util.StringHelper;
import com.sun.star.util.Date;
import com.weaver.formmodel.mobile.manager.MobileUserInit;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import weaver.conn.RecordSet;
import weaver.general.Util;
import weaver.hrm.User;
import weaver.hrm.attendance.manager.HrmAttVacationManager;
import weaver.hrm.schedule.HrmAnnualManagement;

public class BusyAction extends HttpServlet {

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
			if("convertGetOrgNotice".equals(action)){//所内公告接口
				convertGetOrgNotice();
			}else if("convertGetUserNative".equals(action)){//人员动态接口-全部
				convertGetUserNative();
			}else if("convertGetUserImg".equals(action)){//获取人员头像接口
				convertGetUserImg(user);
			}else if("convertGetMeetInfo".equals(action)){//获取会议纪要接口
				convertGetMeetInfo(user);
			}else if("convertGetMyTraining".equals(action)){//我的培训
				convertGetMyTraining(user);
			}else if("convertGetAssessment".equals(action)){//绩效考核
				convertGetAssessment(user);
			}else if("convertGetBirthdayMan".equals(action)){//本月寿星
				convertGetBirthdayMan();
			}else if("convertGetHolidays".equals(action)){//年假信息
				convertGetHolidays(user);
			}else if("convertCompPhotos".equals(action)){//获取公司相册目录接口
				convertCompPhotos();
			}else if("convertCoWork".equals(action)){//获取协作接口
				convertCoWork(user);
			}else if("convertGetUserNative_xj".equals(action)){//人员动态接口-休假
				convertGetUserNative_xj();
			}else if("convertGetUserNative_cc".equals(action)){//人员动态接口-出差
				convertGetUserNative_cc();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**所内公告接口
	 * params action
	 * pageno	当前页
	 * pagesize	每页显示数
	 * @throws Exception 
	 */
	
	public void convertGetOrgNotice() throws Exception{
		RecordSet rs = new RecordSet();
		int recordCount=0;
		int pageno = Util.getIntValue(request.getParameter("pageno"),1);
		int pagesize = Util.getIntValue(request.getParameter("pagesize"),5);
		String selSql = "select count(1) from info_data where row_state=1";
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
		
		//取按倒序排序的数据的前iNextNum条倒序排序
		selSql = "select top " + iNextNum +" info_id,CONVERT(VARCHAR(10),publish_time,120) publish_time,publish_time as publish_times,info_title from info_data "+
				" where row_state=1 order by info_data.publish_time desc";
		//将上面得到的数据取按顺序排序的ipageset条数据顺序排序
		selSql = "select top " + ipageset +" t1.* from (" + selSql + ") t1 order by publish_times asc";
		//将得到的数据倒序排序显示
		selSql = "select t2.* from (" + selSql + ") t2 order by publish_times desc";
		
		JSONArray jsonArray = new JSONArray();
		if((recordCount - iNextNum + pagesize)>0){
			rs.executeSql(selSql);
			while(rs.next()){
				JSONObject json = new JSONObject();
				json.put("id", Util.null2String(rs.getString("info_id")));
				json.put("publishtime", Util.null2String(rs.getString("publish_time")));
				json.put("title", Util.null2String(rs.getString("info_title")));
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
	
	/**人员动态接口
	 * @params 
	 * pageno 	当前页数
	 * pagesize	每页显示数
	 */
	
	public void convertGetUserNative(){
		RecordSet rs = new RecordSet();
		int recordCount=0;
		int pageno = Util.getIntValue(request.getParameter("pageno"),1);
		int pagesize = Util.getIntValue(request.getParameter("pagesize"),10);
		String selSql = "select count(1) from v_dtkb";
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

			
			//取按顺序排序的数据的前iNextNum条顺序排序
			selSql = "select top " + iNextNum +" hrmid,lastname,date,dttype,ccd,showorder,dsporder,seclevel from v_dtkb order by seclevel desc,showorder asc,dsporder asc,hrmid,dttype";
			//将上面得到的数据按倒序排序取ipageset条数据倒序排序
			selSql = "select top " + ipageset +" t1.* from (" + selSql + ") t1 order by seclevel asc,showorder desc,dsporder desc,hrmid,dttype";
			//将上面得到的数据顺序排序
			selSql = "select t2.* from (" + selSql + ") t2 order by seclevel desc,showorder asc,dsporder asc,hrmid,dttype";
		

		JSONArray jsonArray = new JSONArray();
		if((recordCount - iNextNum + pagesize)>0){
			rs.executeSql(selSql);
			while(rs.next()){
				JSONObject json = new JSONObject();
				json.put("hrmid", Util.null2String(rs.getString("hrmid")));
				json.put("lastname", Util.null2String(rs.getString("lastname")));
				json.put("date", Util.null2String(rs.getString("date")));
				json.put("dttype", Util.null2String(rs.getString("dttype")));
				json.put("ccd", Util.null2String(rs.getString("ccd")));
				json.put("seclevel", Util.null2String(rs.getString("seclevel")));
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

	public void convertGetUserNative_xj(){
		RecordSet rs = new RecordSet();
		int recordCount=0;

		int pageno = Util.getIntValue(request.getParameter("pageno"),1);
		int pagesize = Util.getIntValue(request.getParameter("pagesize"),10);
		String selSql = "select count(1) from v_dtkb where dttype = '休假'";
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

			//取按顺序排序的数据的前iNextNum条顺序排序
			selSql = "select top " + iNextNum +" hrmid,lastname,date,dttype,ccd,showorder,dsporder,seclevel from v_dtkb where dttype = '休假' order by seclevel desc,showorder asc,dsporder asc,hrmid,dttype";
			//将上面得到的数据按倒序排序取ipageset条数据倒序排序
			selSql = "select top " + ipageset +" t1.* from (" + selSql + ") t1 order by seclevel asc,showorder desc,dsporder desc,hrmid,dttype";
			//将上面得到的数据顺序排序
			selSql = "select t2.* from (" + selSql + ") t2 order by seclevel desc,showorder asc,dsporder asc,hrmid,dttype";
					
		JSONArray jsonArray = new JSONArray();
		if((recordCount - iNextNum + pagesize)>0){
			rs.executeSql(selSql);
			while(rs.next()){
				JSONObject json = new JSONObject();
				json.put("hrmid", Util.null2String(rs.getString("hrmid")));
				json.put("lastname", Util.null2String(rs.getString("lastname")));
				json.put("date", Util.null2String(rs.getString("date")));
				json.put("dttype", Util.null2String(rs.getString("dttype")));
				json.put("ccd", Util.null2String(rs.getString("ccd")));
				json.put("seclevel", Util.null2String(rs.getString("seclevel")));
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
	
	public void convertGetUserNative_cc(){
		RecordSet rs = new RecordSet();
		int recordCount=0;
		int pageno = Util.getIntValue(request.getParameter("pageno"),1);
		int pagesize = Util.getIntValue(request.getParameter("pagesize"),10);
		String selSql = "select count(1) from v_dtkb where dttype = '出差'";
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

			//取按顺序排序的数据的前iNextNum条顺序排序
			selSql = "select top " + iNextNum +" hrmid,lastname,date,dttype,ccd,showorder,dsporder,seclevel from v_dtkb where dttype='出差' order by seclevel desc,showorder asc,dsporder asc,hrmid,dttype";
			//将上面得到的数据按倒序排序取ipageset条数据倒序排序
			selSql = "select top " + ipageset +" t1.* from (" + selSql + ") t1 order by seclevel asc,showorder desc,dsporder desc,hrmid,dttype";
			//将上面得到的数据顺序排序
			selSql = "select t2.* from (" + selSql + ") t2 order by seclevel desc,showorder asc,dsporder asc,hrmid,dttype";


		JSONArray jsonArray = new JSONArray();
		if((recordCount - iNextNum + pagesize)>0){
			rs.executeSql(selSql);
			while(rs.next()){
				JSONObject json = new JSONObject();
				json.put("hrmid", Util.null2String(rs.getString("hrmid")));
				json.put("lastname", Util.null2String(rs.getString("lastname")));
				json.put("date", Util.null2String(rs.getString("date")));
				json.put("dttype", Util.null2String(rs.getString("dttype")));
				json.put("ccd", Util.null2String(rs.getString("ccd")));
				json.put("seclevel", Util.null2String(rs.getString("seclevel")));
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
	 * 获取人员头像接口
	 * @param user
	 */
	private void convertGetUserImg(User user) {

		RecordSet rs = new RecordSet();
		String userid = String.valueOf(user.getUID());
		String selSql = "select id,lastname,resourceimageid from hrmresource where id='"+userid+"'";
		rs.executeSql(selSql);
		JSONObject result = new JSONObject();
		if(rs.next()){
			result.put("id", Util.null2String(rs.getString("id")));
			result.put("lastname", Util.null2String(rs.getString("lastname")));
			result.put("resourceimageid", Util.null2String(rs.getString("resourceimageid")));
		}
		
		try {
			response.setContentType("application/json; charset=utf-8");  
			response.getWriter().print(result.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取会议纪要接口
	 * @param
	 * jylx 纪要类型（非必输）
	 * pageno 当前页数
	 * pagesize 每页显示数
	 */
	private void convertGetMeetInfo(User user) {

		RecordSet rs = new RecordSet();
		int recordCount=0;
		String jylx = Util.null2String(request.getParameter("jylx"));
		int pageno = Util.getIntValue(request.getParameter("pageno"),1);
		int pagesize = Util.getIntValue(request.getParameter("pagesize"),10);
		String sqlWhere = "";
		if(!(jylx == null || jylx.equalsIgnoreCase("null") || jylx.length() == 0)){
			sqlWhere = " and jylx = '" + jylx + "'";
		}
		//获取流程权限进行数据过滤
		String userid = String.valueOf(user.getUID());
		String limitWorkflow = "SELECT t1.requestid FROM workflow_requestbase t1,workflow_currentoperator t2 WHERE (t1.deleted <> 1 OR t1.deleted is null OR t1.deleted='') AND t1.requestid = t2.requestid AND t2.userid IN ('" + userid + "') AND t2.usertype=0 AND t2.islasttimes=1 AND (isnull(t1.currentstatus,-1) = -1 OR (isnull(t1.currentstatus,-1)=0 AND t1.creater IN ('" + userid + "'))) AND t1.workflowid IN (SELECT id FROM workflow_base WHERE (isvalid='1' OR isvalid='3')) AND t1.currentnodetype=3";
		
		String selSql = "select count(1) from formtable_main_26 where 1=1 and exists (select 1 from ("+limitWorkflow+") m where m.requestid=formtable_main_26.requestid) " + sqlWhere;
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
		//取按顺序排序的数据的前iNextNum条顺序排序
		selSql = "select top " + iNextNum + " m1.requestid,m1.bt,m1.ffrq,m1.jylx,m2.lastoperatedate,m2.lastoperatetime from formtable_main_26 m1 ,workflow_requestbase m2  where 1=1 and m1.requestid=m2.requestid and exists (select 1 from (" + limitWorkflow + ") m where m.requestid=m1.requestid) " + sqlWhere + " order by m2.lastoperatedate desc,m2.lastoperatetime desc";
		//将上面得到的数据按倒序排序取ipageset条数据倒序排序
		selSql = "select top " + ipageset + " t1.* from (" + selSql + ") t1 order by lastoperatedate asc,lastoperatetime asc";
		//将上面得到的数据顺序排序
		selSql = "select t2.* from (" + selSql + ") t2 order by lastoperatedate desc,lastoperatetime desc";
		
		JSONArray jsonArray = new JSONArray();
		if((recordCount - iNextNum + pagesize)>0){
			rs.executeSql(selSql);
			while(rs.next()){
				JSONObject json = new JSONObject();
				json.put("requestid", Util.null2String(rs.getString("requestid")));
				json.put("bt", Util.null2String(rs.getString("bt")));
				json.put("ffrq", Util.null2String(rs.getString("ffrq")));
				json.put("jylx", Util.null2String(rs.getString("jylx")));
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
	 * 我的培训
	 * @param user
	 * pageno 当前页数
	 * pagesize 每页显示数
	 */
	private void convertGetMyTraining(User user) {
		
		RecordSet rs = new RecordSet();
		int recordCount=0;
		int pageno = Util.getIntValue(request.getParameter("pageno"),1);
		int pagesize = Util.getIntValue(request.getParameter("pagesize"),10);
		String userid = String.valueOf(user.getUID());
		String selSql = "select count(1) from uf_Train where train_staff='" + userid + "' ";
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
		//取按顺序排序的数据的前iNextNum条顺序排序
		selSql = "select top " + iNextNum +" id,train_staff,train_name,train_type,train_integral,train_date from uf_Train where train_staff='" + userid + "' order by train_date desc";
		//将上面得到的数据按倒序排序取ipageset条数据倒序排序
		selSql = "select top " + ipageset +" t1.* from (" + selSql + ") t1 order by train_date asc";
		//将上面得到的数据顺序排序
		selSql = "select t2.* from (" + selSql + ") t2 order by train_date desc";
		
		JSONArray jsonArray = new JSONArray();
		if((recordCount - iNextNum + pagesize)>0){
			rs.executeSql(selSql);
			while(rs.next()){
				JSONObject json = new JSONObject();
				json.put("id", Util.null2String(rs.getString("id")));
				json.put("train_staff", Util.null2String(rs.getString("train_staff")));
				json.put("train_name", Util.null2String(rs.getString("train_name")));
				json.put("train_type", Util.null2String(rs.getString("train_type")));
				json.put("train_integral", Util.null2String(rs.getString("train_integral")));
				json.put("train_date", Util.null2String(rs.getString("train_date")));
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
	 * 绩效考核
	 * @param user
	 * pageno 当前页数
	 * pagesize 每页显示数
	 */
	private void convertGetAssessment(User user) {

		RecordSet rs = new RecordSet();
		int recordCount=0;
		int pageno = Util.getIntValue(request.getParameter("pageno"),1);
		int pagesize = Util.getIntValue(request.getParameter("pagesize"),10);
		String userid = String.valueOf(user.getUID());
		String selSql = "select count(1) from uf_Assess where assess_staff='" + userid + "'";
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
		//取按顺序排序的数据的前iNextNum条顺序排序
		selSql = "select top " + iNextNum +" id,assess_staff,assess_date,assess_grade from uf_Assess where assess_staff='" + userid + "' order by assess_date desc";
		//将上面得到的数据按倒序排序取ipageset条数据倒序排序
		selSql = "select top " + ipageset +" t1.* from (" + selSql + ") t1 order by assess_date asc";
		//将上面得到的数据顺序排序
		selSql = "select t2.* from (" + selSql + ") t2 order by assess_date desc";
		
		JSONArray jsonArray = new JSONArray();
		if((recordCount - iNextNum + pagesize)>0){
			rs.executeSql(selSql);
			while(rs.next()){
				JSONObject json = new JSONObject();
				json.put("id", Util.null2String(rs.getString("id")));
				json.put("assess_staff", Util.null2String(rs.getString("assess_staff")));
				json.put("assess_date", Util.null2String(rs.getString("assess_date")));
				json.put("assess_grade", Util.null2String(rs.getString("assess_grade")));
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
	 * 本月寿星
	 * @param
	 */
	private void convertGetBirthdayMan() {

		RecordSet rs = new RecordSet();
		int recordCount=0;
		String selSql = "select count(1) from hrmresource where (status = 1 or status = 0) and SUBSTRING(birthday,6,2) = right('0'+ltrim(MONTH(GETDATE())),2)";
		rs.executeSql(selSql);
		if(rs.next()){
			recordCount = rs.getInt(1);
		}
		
		//取按顺序排序的数据的前iNextNum条顺序排序
		selSql = "select id,lastname,resourceimageid,birthday from hrmresource where (status = 1 or status = 0) and SUBSTRING(birthday,6,2) = right('0'+ltrim(MONTH(GETDATE())),2)";
		rs.executeSql(selSql);
		JSONArray jsonArray = new JSONArray();
		while(rs.next()){
			JSONObject json = new JSONObject();
			json.put("id", Util.null2String(rs.getString("id")));
			json.put("lastname", Util.null2String(rs.getString("lastname")));
			json.put("resourceimageid", Util.null2String(rs.getString("resourceimageid")));
			json.put("birthday", Util.null2String(rs.getString("birthday")));
			jsonArray.add(json);
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
	 * 年假信息
	 * @param user
	 */
	private void convertGetHolidays(User user) {

		RecordSet rs = new RecordSet();
		String userid = String.valueOf(user.getUID());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance(); //当前日期
		String thisYear = Util.add0((cal.get(Calendar.YEAR)), 4); //当前年
		String thisMonth = Util.add0((cal.get(Calendar.MONTH)) + 1, 2); //当前月
		String thisDayOfMonth = Util.add0((cal.get(Calendar.DAY_OF_MONTH)), 2); //当前日
		String date=thisYear + "-" + thisMonth + "-" + thisDayOfMonth;//日期
		String userannualinfo;
		try {
			userannualinfo = HrmAnnualManagement.getUserAannualInfo(userid,date);
			String todayYearDays = "";
			String lastYearDays = "";

			String todayYear = "";
			String lastYear = "";
			
			String thisyearannual = Util.TokenizerString2(userannualinfo,"#")[0];
			String lastyearannual = Util.TokenizerString2(userannualinfo,"#")[1];
			String allannual = Util.TokenizerString2(userannualinfo,"#")[2];
			HrmAttVacationManager hvm = new HrmAttVacationManager();
			float[] freezeDays = hvm.getFreezeDays(userid);
			if(freezeDays[0] > 0) allannual += " - "+freezeDays[0];
			String selSql = "select hrmresource.id,hrmresource.lastname,cus_fielddata.field2 as joindate,cus_fielddata.field1 as gl,(select b.annualdays_new from hrmresource a,uf_YearMaintain b where b.annualyear=(select DATENAME(YEAR,GETDATE())) and a.id=b.resourceid and b.resourceid='" + userid + "')as todayYearDays,(select b.annualyear from hrmresource a,uf_YearMaintain b where b.annualyear=(select DATENAME(YEAR,GETDATE())) and a.id=b.resourceid and b.resourceid='" + userid + "')as todayYear,(select b.annualdays_new from hrmresource a,uf_YearMaintain b where b.annualyear=(select DATENAME(YEAR,GETDATE())-1) and a.id=b.resourceid and b.resourceid='" + userid + "')as lastYearDays,(select b.annualyear from hrmresource a,uf_YearMaintain b where b.annualyear=(select DATENAME(YEAR,GETDATE())-1) and a.id=b.resourceid and b.resourceid='" + userid + "')as lastYear from hrmresource left join cus_fielddata on hrmresource.id=cus_fielddata.id and cus_fielddata.scopeid=1 where hrmresource.id='" + userid + "'";
			rs.executeSql(selSql);
			JSONObject result = new JSONObject();
			if(rs.next()){
				result.put("joindate", Util.null2String(rs.getString("joindate")));
				result.put("gl", Util.null2String(rs.getString("gl")));
				result.put("lastyearannual", lastyearannual);
				result.put("thisyearannual", thisyearannual);
				result.put("allannual", allannual);
				result.put("todayYearDays", Util.null2String(rs.getString("todayYearDays")));
				result.put("lastYearDays", Util.null2String(rs.getString("lastYearDays")));
				result.put("todayYear", Util.null2String(rs.getString("todayYear")));
				result.put("lastYear", Util.null2String(rs.getString("lastYear")));
			}
			response.setContentType("application/json; charset=utf-8");  
			response.getWriter().print(result.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**获取公司相册目录接口
	 * @param
	 */
	private void convertCompPhotos() {

		RecordSet rs = new RecordSet();
		String selSql = "select id,photoName,photoCount,(select top 1 id from AlbumPhotos where isFolder='0' and parentId=AlbumPhotos1.id order by  ordernum desc,id desc ) as picture from  AlbumPhotos as AlbumPhotos1 where  isFolder='1' order by id asc";
		rs.executeSql(selSql);
		JSONArray jsonArray = new JSONArray();
		while(rs.next()){
			JSONObject json = new JSONObject();
			json.put("id", Util.null2String(rs.getString("id")));
			json.put("photoName", Util.null2String(rs.getString("photoName")));
			json.put("photoCount", Util.null2String(rs.getString("photoCount")));
			json.put("picture", Util.null2String(rs.getString("picture")));
			jsonArray.add(json);
		}
		JSONObject result = new JSONObject();
		result.put("datas", jsonArray.toString());
		try {
			response.setContentType("application/json; charset=utf-8");  
			response.getWriter().print(result.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取协作接口
	 * @param user
	 * typeid	类型(必输)
	 * name	(非必输)
	 * pageno 当前页
	 * pagesize	每页显示数
	 */
	private void convertCoWork(User user) {
		
		String userid = String.valueOf(user.getUID());
		int departmentid=user.getUserDepartment();//用户所属部门
		String jobtitle=user.getJobtitle();//用户岗位
		String seclevel=user.getSeclevel();//用于安全等级
		String typeid = Util.null2String(request.getParameter("typeid"));//类别
		String name = Util.null2String(request.getParameter("name"));//主题名称
		String sWhere="";
		if(!(name==null || "".equals(name) || name.length()<=0)){
			sWhere = " and t.name like '%"+name+"%'";
		}
		RecordSet rs = new RecordSet();
		int recordCount=0;
		int pageno = Util.getIntValue(request.getParameter("pageno"),1);
		int pagesize = Util.getIntValue(request.getParameter("pagesize"),10);
		String sql="select t1.id,t1.name,t1.status,t1.typeid,t1.creater,t1.principal,t1.begindate,t1.enddate,t1.replyNum,t1.readNum,t1.lastdiscussant,t1.lastupdatedate,t1.lastupdatetime,t1.isApproval,"
				+"t1.approvalAtatus,t1.isTop,t2.cotypeid, case when  t3.sourceid is not null then 1 when t2.cotypeid is not null then 0 end as jointype, case when  t4.coworkid is not null then 0 else 1 end as isnew,"
				+" case when  t5.coworkid is not null then 1 else 0 end as important, case when  t6.coworkid is not null then 1 else 0 end as ishidden from cowork_items  t1 left join  ( select distinct cotypeid  from ( select cotypeid from  cotype_sharemanager where sharetype=1 and sharevalue='"+userid
				+"' UNION all  select cotypeid from  cotype_sharemanager where sharetype=2 and sharevalue='"+departmentid+"' and "+seclevel+">=seclevel and "+seclevel+" <= seclevelMax UNION all  select cotypeid from  cotype_sharemanager where sharetype=3 and sharevalue='"+departmentid+"'  and "
				+seclevel+">=seclevel and "+seclevel+" <= seclevelMax UNION all select cotypeid from  cotype_sharemanager where sharetype=4 and exists (select id from hrmrolemembers  where resourceid="+userid+"  and  sharevalue=Cast(roleid as varchar(100))) and "+seclevel+">=seclevel and "
				+seclevel+" <= seclevelMax UNION all  select cotypeid from  cotype_sharemanager where sharetype=5 and "+seclevel+">=seclevel and "+seclevel+" <= seclevelMax) t  )  t2 on t1.typeid=t2.cotypeid left join  (select distinct sourceid from ( select sourceid from coworkshare where type=1 and  (content='"+userid
				+"' or content like '%,"+userid+",%') UNION all  select sourceid from coworkshare where type=2 and content like '%,"+departmentid+",%' and "+seclevel+">=seclevel and "+seclevel+" <= seclevelMax UNION all  select sourceid from coworkshare where type=2 and content like '%,"+departmentid+",%' and "
				+seclevel+">=seclevel and "+seclevel+" <= seclevelMax UNION all  select sourceid from coworkshare where type=3 and content like '%,"+departmentid+",%' and "+seclevel+">=seclevel and "+seclevel+" <= seclevelMax UNION all  select sourceid from coworkshare where type=3 and content like '%,"+departmentid+",%' and "
				+seclevel+">=seclevel and "+seclevel+" <= seclevelMax UNION all  select sourceid from coworkshare where type=6 and content like  '%,"+jobtitle+",%' and  joblevel = 0 UNION all   select sourceid from coworkshare where type=6 and content like  '%,"+jobtitle+",%' and  joblevel = 1 and scopeid like '%,"+departmentid
				+",%' UNION all    select sourceid from coworkshare where type=6 and content like  '%,"+jobtitle+",%' and  joblevel = 2 and scopeid like '%,"+departmentid+",%' UNION all   select sourceid from coworkshare where type=4 and exists (select id from hrmrolemembers  where resourceid="+userid
				+"  and content=Cast(roleid as varchar(100))) and "+seclevel+">=seclevel and "+seclevel+" <= seclevelMax UNION all  select sourceid from coworkshare where type=5 and "+seclevel+">=seclevel and "+seclevel+" <= seclevelMax) t )  t3 on t3.sourceid=t1.id left join (select distinct coworkid,userid from cowork_read where userid="+userid
				+")  t4 on t1.id=t4.coworkid left join (select distinct coworkid,userid from cowork_important where userid="+userid+" )  t5 on t1.id=t5.coworkid left join (select distinct coworkid,userid from cowork_hidden where userid="+userid+" )  t6 on t1.id=t6.coworkid";
		
		String selSql = "select count(*) as total from ("+sql+") t  where t.status=1 and t.jointype is not null and t.ishidden<>1 and (t.approvalAtatus=0 or (t.approvalAtatus=1 and (t.creater='"+userid
				+"' or t.principal='"+userid+"' or t.cotypeid is not null))) and t.typeid='"+typeid+"'"+sWhere;
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
		//取按顺序排序的数据的前iNextNum条倒序排序
		selSql = "select top " + iNextNum +" t.id,t.name,t.lastupdatetime,(select lastname from hrmresource where id=t.principal) as principal,t.replyNum,t.readNum from ("+sql
				+") t  where t.status=1 and t.jointype is not null and t.ishidden<>1 and (t.approvalAtatus=0 or (t.approvalAtatus=1 and (t.creater='"+userid
				+"' or t.principal='"+userid+"' or t.cotypeid is not null))) and t.typeid='"+typeid+"'"+sWhere+" order by t.lastupdatetime desc";
		//将上面得到的数据按倒序排序取ipageset条数据顺序排序
		selSql = "select top " + ipageset +" t7.* from (" + selSql + ") t7 order by lastupdatetime asc";
		//将上面得到的数据倒序排序
		selSql = "select t8.* from (" + selSql + ") t8 order by lastupdatetime desc";
		
		JSONArray jsonArray = new JSONArray();
		if((recordCount - iNextNum + pagesize)>0){
			rs.executeSql(selSql);
			while(rs.next()){
				JSONObject json = new JSONObject();
				json.put("id", Util.null2String(rs.getString("id")));
				json.put("name", Util.null2String(rs.getString("name")));//主题名称
				json.put("lastupdatetime", Util.null2String(rs.getString("lastupdatetime")));//最后回复时间
				json.put("principal", Util.null2String(rs.getString("principal")));//负责人
				json.put("replyNum", Util.null2String(rs.getString("replyNum")));//回复数
				json.put("readNum", Util.null2String(rs.getString("readNum")));//查看数
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

}
