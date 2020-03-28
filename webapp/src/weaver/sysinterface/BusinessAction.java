package weaver.sysinterface;

import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.weaver.formmodel.mobile.manager.MobileUserInit;
import com.weaver.formmodel.util.DateHelper;
import com.weaver.formmodel.util.NumberHelper;
import com.weaver.formmodel.util.StringHelper;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import weaver.Constants;
import weaver.WorkPlan.MutilUserUtil;
import weaver.WorkPlan.WorkPlanSetInfo;
import weaver.WorkPlan.WorkPlanShareUtil;
import weaver.conn.RecordSet;
import weaver.cpt.util.CommonShareManager;
import weaver.crm.CrmShareBase;
import weaver.crm.Maint.CustomerInfoComInfo;
import weaver.docs.search.DocSearchComInfo;
import weaver.fna.maintenance.BudgetfeeTypeComInfo;
import weaver.fna.maintenance.FnaCostCenter;
import weaver.formmode.customjavacode.CustomJavaCodeRun;
import weaver.formmode.dao.BaseDao;
import weaver.formmode.search.FormModeTransMethod;
import weaver.formmode.service.CommonConstant;
import weaver.formmode.setup.ModeRightInfo;
import weaver.formmode.view.ModeShareManager;
import weaver.formmode.virtualform.VirtualFormHandler;
import weaver.general.SplitPageParaBean;
import weaver.general.SplitPageUtil;
import weaver.general.StaticObj;
import weaver.general.Util;
import weaver.general.WorkFlowTransMethod;
import weaver.hrm.HrmUserVarify;
import weaver.hrm.User;
import weaver.hrm.UserManager;
import weaver.hrm.company.DepartmentComInfo;
import weaver.hrm.company.SubCompanyComInfo;
import weaver.hrm.job.JobTitlesComInfo;
import weaver.hrm.resource.ResourceComInfo;
import weaver.interfaces.workflow.browser.Browser;
import weaver.mobile.plugin.ecology.service.AuthService;
import weaver.mobile.plugin.ecology.service.HrmResourceService;
import weaver.mobile.plugin.ecology.service.PluginServiceImpl;
import weaver.mobile.plugin.ecology.service.ScheduleService;
import weaver.mobile.webservices.common.BrowserAction;
import weaver.mobile.webservices.workflow.WorkflowExtInfo;
import weaver.mobile.webservices.workflow.WorkflowRequestInfo;
import weaver.mobile.webservices.workflow.WorkflowService;
import weaver.mobile.webservices.workflow.WorkflowServiceImpl;
import weaver.search.SearchClause;
import weaver.servicefiles.BrowserXML;
import weaver.share.ShareManager;
import weaver.systeminfo.SystemEnv;
import weaver.workflow.request.RequestRejectManager;
import weaver.workflow.request.WFLinkInfo;

import com.weaver.formmodel.mobile.manager.MobileUserInit;
import com.weaver.formmodel.util.DateHelper;
import com.weaver.formmodel.util.StringHelper;

public class BusinessAction extends HttpServlet {

	private HttpServletRequest request;
	
	private HttpServletResponse response;
	
    private final static String regxpForHtml = "<([^>]*)>"; // 过滤所有以<开头以>结尾的标签  
	
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
			if("convertGetUserListData".equals(action)){
				convertGetUserListData();
			}else if("convertGetUserDetailData".equals(action)){
				convertGetUserDetailData();
			}else if("convertCreateWorkflowList".equals(action)){
				convertCreateWorkflowList(user);
			}else if("convertWorkflowData".equals(action)){
				convertWorkflowData(user);
			}else if("convertGetSchedule".equals(action)){
				convertGetSchedule(user);
			}else if("convertGetScheduleByMonth".equals(action)){
				convertGetScheduleByMonth(user);
			}else if("convertcreateSchedule".equals(action) || "convertupdateSchedule".equals(action)){
				convertcreateSchedule(user);
			}else if("convertdelSchedule".equals(action)){
				convertdelSchedule(user);
			}else if("convertoverSchedule".equals(action)){
				convertoverSchedule(user);
			}else if("convertGetScheduleInfo".equals(action)){
				convertGetScheduleInfo(user);
			}else if("convertGetFlowWorkCount".equals(action)){
				convertGetFlowWorkCount(user);
			}else if("getScheduleToday".equals(action)){
				getScheduleToday(user);
			}else if("getListData".equals(action)){
				getBrowserData(user);
			}else if("getTodoWorkFlowData".equals(action)){
				getTodoWorkFlowData(user);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String listRejectNode(){
		String method = Util.null2String(request.getParameter("method"));
		int browserTypeId = Util.getIntValue(request.getParameter("browserTypeId"), 0);
		String customBrowType = Util.null2String(request.getParameter("customBrowType"));
		int hrmOrder = Util.getIntValue(request.getParameter("_hrmorder_"), 0);
		String linkhref =  Util.null2String(request.getParameter("linkhref"));
		String joinFieldParams= Util.null2String(request.getParameter("joinFieldParams"));
		int pageNo = Util.getIntValue(request.getParameter("pageno"), 1);
		int pageSize = Util.getIntValue(request.getParameter("pageSize"), 10);
		String keyword = Util.null2String(request.getParameter("keyword"));
		
		String f_weaver_belongto_userid=Util.null2String(request.getParameter("f_weaver_belongto_userid"));//需要增加的代码
		String f_weaver_belongto_usertype=Util.null2String(request.getParameter("f_weaver_belongto_usertype"));//需要增加的代码
		User user  = HrmUserVarify.getUser(request, response, f_weaver_belongto_userid, f_weaver_belongto_usertype) ;//需要增加的代码
		
		BrowserAction braction = new BrowserAction(user, browserTypeId, pageNo, pageSize);
		braction.setKeyword(keyword);
		braction.setMethod(method);
		braction.setHrmOrder(hrmOrder);
		//分权用
		braction.setCustomBrowType(customBrowType);
		braction.setJoinFieldParams(joinFieldParams);
		braction.setLinkhref(linkhref);
		
		String result = braction.getBrowserData();
		
		return result;
	}
	
	public void getBrowserData(User user){
		String action = Util.null2String(request.getParameter("action"));
		String method = Util.null2String(request.getParameter("method"));
			JSONObject resultObj = new JSONObject();
			try {
				weaver.formmode.view.ModeShareManager ModeShareManager = new weaver.formmode.view.ModeShareManager();
				weaver.formmode.setup.ModeRightInfo ModeRightInfo = new weaver.formmode.setup.ModeRightInfo();
				weaver.formmode.service.BrowserInfoService browserInfoService = new weaver.formmode.service.BrowserInfoService();
				weaver.cpt.util.CommonShareManager CommonShareManager = new weaver.cpt.util.CommonShareManager();
				weaver.docs.search.DocSearchComInfo DocSearchComInfo = new weaver.docs.search.DocSearchComInfo();
				weaver.share.ShareManager ShareManager = new weaver.share.ShareManager();
				long curr = System.currentTimeMillis();
				
				String browserId = Util.null2String(request.getParameter("browserId"));
				String browserName = Util.null2String(request.getParameter("browserName"));
				String browsertypeid = Util.null2String(request.getParameter("browserTypeId"));
				String customBrowType = Util.null2String(request.getParameter("customBrowType"));
				if(browserId.length()==0){
					browserId = browsertypeid;
				}
				if(browserName.length()==0){
					browserName = customBrowType;
				}
				
				int pageNo = Util.getIntValue(request.getParameter("pageNo"), 1);
				int pageSize = Util.getIntValue(request.getParameter("pageSize"), 10);
				
				String searchKey = "";
				try {
					searchKey = URLDecoder.decode(Util.null2String(request.getParameter("searchKey")), "utf-8");
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				
				String sqlwhere = "";
				String poolname = "";
				String orderby = "";
				String orderbyField = "";
				String expandfield = "";
				int totalSize = 0;
				boolean single = true;
				JSONArray dataArr = new JSONArray();
				JSONArray selDataArr = new JSONArray();
				if(StringHelper.isNotEmpty(browserId)&&"-9".equals(browserId)){
					String rejectNodeJsonString = listRejectNode();
					JSONObject json = new JSONObject();
					json= JSONObject.fromObject(rejectNodeJsonString);
					JSONArray jsonArray =  json.getJSONArray("result");
					for (int i = 0; i < jsonArray.size(); i++) {
						JSONObject json1 =  (JSONObject)jsonArray.get(i);
						String id = Util.null2String(json1.get("id"));
						String show1 = Util.null2String(json1.get("show1"));
						String type1 = Util.null2String(json1.get("type"));
						JSONObject datasJson =  new JSONObject();
						datasJson.put("id", id);
						datasJson.put("objname", show1);
						datasJson.put("type1", type1);
						dataArr.add(datasJson);
					}
					totalSize = dataArr.size();
					
				}else if(StringHelper.isNotEmpty(browserId)){
					String refsql = "select * from workflow_browserurl where id =" + browserId;
					RecordSet rs = new RecordSet();
					rs.execute(refsql);
					if(rs.next()){
						String reftable = rs.getString("tablename");
						String keyfield = rs.getString("keycolumname");
						String viewfield = rs.getString("columname");
						String fielddbtype = rs.getString("fielddbtype");
						single = (fielddbtype.equalsIgnoreCase("int") || fielddbtype.equalsIgnoreCase("integer")) ? true : false;
						int sortWay = 0;
						if(StringHelper.isEmpty(reftable) && StringHelper.isNotEmpty(browserName)){
							Browser browser = (Browser)StaticObj.getServiceByFullname(browserName, Browser.class);
							poolname = browser.getPoolname();
							String search = StringHelper.null2String(browser.getSearch()).trim();
							
							if(browser.getFrom().equals("2")){
								
								reftable = "(" + search + ")";
								 
								 String newtype = browserName.substring("browser.".length());
								 String sql = "select keyfield from datashowset where showname='"+newtype+"' and showclass=1 ";
								 rs.execute(sql);
								 if(rs.next()){
									 keyfield = StringHelper.null2String(rs.getString("keyfield"));
								 }
								 
								Map showfieldMap = browser.getShowfieldMap();
								Set showfieldSet = showfieldMap.keySet();
								for(Object showfieldObj : showfieldSet){
									viewfield = showfieldObj.toString() ;
									break;
								}
								
							}else{
							 	String[] fieldArr = search.split(",");
								//keyfield = fieldArr[0].substring(6).trim();
								String newtype = browserName.substring("browser.".length());
								String sql = "select customid from datashowset where showname='"+newtype+"' and showclass=1 ";
								rs.execute(sql);
								String customid = "";
								if(rs.next()){
									customid = StringHelper.null2String(rs.getString("customid"));
								}
								sql = "select workflow_billfield.fieldname from Mode_CustomBrowser Mode_CustomBrowser "+
								 	"left join mode_custombrowserdspfield mode_custombrowserdspfield on Mode_CustomBrowser.id=mode_custombrowserdspfield.customid "+
								 	"left join workflow_billfield workflow_billfield on workflow_billfield.id=mode_custombrowserdspfield.fieldid "+
								 	"where Mode_CustomBrowser.id="+customid+" and mode_custombrowserdspfield.ispk='1'";
								rs.execute(sql);
								if(rs.next()){
									keyfield = StringHelper.null2String(rs.getString("fieldname"));
								}
								viewfield = fieldArr[1].trim();
								expandfield = fieldArr[2].split("from")[0].trim();
								expandfield = StringHelper.isNotEmpty(expandfield) ? ("," + expandfield + " exp1") : "";
								String tableStr = search.substring(search.toLowerCase().indexOf(" from "));
								tableStr = tableStr.substring(6).trim();
								if(tableStr.indexOf(" ")==-1){
									reftable = tableStr;
								}else{
									reftable = tableStr.substring(0,tableStr.indexOf(" "));
								}
							}
							
							if("161".equals(browserId)){
								single = true;
							}else if("162".equals(browserId)){
								single = false;
							}
							
							/**增加browser框查询限定条件**/
							String pointId = browserName.replaceAll("browser.", "");
							BrowserXML browserXML = new BrowserXML();
							Hashtable dataHST = browserXML.getDataHST();
							Hashtable datadetailHST = (Hashtable)dataHST.get(pointId);
							if(datadetailHST != null){
								String customid = (String)datadetailHST.get("customid");
								if(StringHelper.isNotEmpty(customid)){
									RecordSet reS = new RecordSet();
									reS.execute("select a.defaultsql,a.searchconditiontype,a.javafilename from mode_custombrowser a where a.id="+customid);
									if(reS.next()){
										String searchconditiontype = Util.null2String(reS.getString("searchconditiontype"));
										searchconditiontype = searchconditiontype.equals("") ? "1" : searchconditiontype;
										String sqlCondition = "";
										if(searchconditiontype.equals("2")){	//java file
											String javafilename = Util.null2String(reS.getString("javafilename"));
											if(!javafilename.equals("")){
												Map<String, String> sourceCodePackageNameMap = CommonConstant.SOURCECODE_PACKAGENAME_MAP;
												String sourceCodePackageName = sourceCodePackageNameMap.get("3");
												String classFullName = sourceCodePackageName + "." + javafilename;
												
												Map<String, Object> param = new HashMap<String, Object>();
												param.put("user", user);
												
												Object javaResult = CustomJavaCodeRun.run(classFullName, param);
												sqlCondition = Util.null2String(javaResult);
											}
										}else{
											String defaultsql = Util.toScreenToEdit(reS.getString("defaultsql"),user.getLanguage()).trim();
											FormModeTransMethod formModeTransMethod = new FormModeTransMethod();
											defaultsql = formModeTransMethod.getDefaultSql(user,defaultsql);
											sqlCondition = defaultsql;
										}
										if(!sqlCondition.equals("")){
											sqlwhere = sqlwhere + " and "+sqlCondition;
										}
									}
								}
							}
						}
						
						try{
							int wIndex;
							String browserSearch = "";
							if(StringHelper.isNotEmpty(browserName) && browserName.indexOf("browser.") > -1){
								Browser browser = (Browser)StaticObj.getServiceByFullname(browserName, Browser.class);
								browserSearch = Util.null2String(browser.getSearch());
								if ((wIndex = browserSearch.toLowerCase().lastIndexOf("where ")) > -1) {
									String whereSearch = browserSearch.substring(wIndex + 6);
						
									String params = Util.null2String(request.getParameter("params"));
									JSONObject paramsJson = JSONObject.fromObject(params);
									//nj = $nj$ and bj = $bj$
									if (whereSearch.indexOf("$") > -1) {
										int beginIndex = whereSearch.indexOf("$");
										while (beginIndex > -1) {
											int endIndex = whereSearch.indexOf("$", beginIndex + 1);
											if (endIndex > -1) {
												String parentName = whereSearch.substring(beginIndex + 1, endIndex).toLowerCase();
												String parentValue = paramsJson.getString("fieldname_" + parentName);
												whereSearch = whereSearch.replace("$" + parentName + "$", parentValue);
												beginIndex = whereSearch.indexOf("$");
											} else {
												break;
											}
										}
									}
						
									sqlwhere = sqlwhere + " and " + whereSearch;
								}
							}
						}catch(Exception e){
							e.printStackTrace();
						}
						
						String customid = "0";
						if(StringHelper.isNotEmpty(browserName)){
							String pointId = browserName.replaceAll("browser.", "");
							BrowserXML browserXML = new BrowserXML();
							Hashtable dataHST = browserXML.getDataHST();
							Hashtable datadetailHST = (Hashtable) dataHST.get(pointId);
							if (datadetailHST != null) {
								customid = (String) datadetailHST.get("customid");
							}
						}
						
						String rightsql = "";
						String sqlfrom = reftable + " t1";
						//sqlwhere = viewfield + " like '%" + searchKey + "%' " + sqlwhere + " ";
						String sqlwhere2 = viewfield + " like '%" + searchKey + "%' ";
						if("HrmDepartment".equalsIgnoreCase(reftable) || "HrmDepartmentallview".equalsIgnoreCase(reftable)){// 部门或者部门视图，要过滤掉封存的数据
							sqlwhere2 += " and (t1.canceled is null or t1.canceled=0) ";
						}else if("hrmsubcompany".equalsIgnoreCase(reftable) || "hrmsubcompanyallview".equalsIgnoreCase(reftable)){// 分部或者分部视图，要过滤掉封存的数据
							sqlwhere2 += " and (t1.canceled is null or t1.canceled=0) ";
						}
						
						//系统自定义浏览框权限过滤
						if("7".equals(browserId)||"18".equals(browserId)){
							CrmShareBase crmShareBase = new CrmShareBase();
							String leftjointable = crmShareBase.getTempTable(""+user.getUID());
							if(user.getLogintype().equals("1")){
								sqlfrom += " left join "+leftjointable+" t2 on t1.id = t2.relateditemid ";
								sqlwhere2 +=" and t1.id = t2.relateditemid ";
							}else{
								sqlwhere2 +="  and t1.deleted<>1 and t1.agent="+user.getUID();
								sqlwhere2 +="  and CRM_CustomerInfo t1 "+sqlwhere+" and t1.deleted<>1 and t1.agent="+user.getUID();
							}
						}else if("8".equals(browserId)||"135".equals(browserId)){
							sqlwhere2 +=" and ("+CommonShareManager.getPrjShareWhereByUser(user)+") ";
						}else if("9".equals(browserId)||"37".equals(browserId)){
							DocSearchComInfo.resetSearchInfo() ;
							String docstatus[] = new String[]{"1","2","5","7"};
							for(int i = 0;i<docstatus.length;i++){
							   	DocSearchComInfo.addDocstatus(docstatus[i]);
							}
							String tempsqlwhere = DocSearchComInfo.FormatSQLSearch(user.getLanguage()) ;
							tempsqlwhere+=" and (ishistory is null or ishistory = 0) ";
						    tempsqlwhere  += " and t1.id=t2.sourceid  ";
							sqlwhere2 +=" and "+tempsqlwhere;
							sqlfrom += ", "+ShareManager.getShareDetailTableByUser("doc", user) + "  t2 ";
							orderby = "doclastmoddate desc,doclastmodtime desc";
							sortWay = 1;
						}else if("16".equals(browserId)||"152".equals(browserId)||"171".equals(browserId)){
							String belongtoshow = "";		
							int userid=user.getUID();	
							String usertype = "0";
							if ("2".equals(user.getLogintype())) {
								usertype = "1";
							}
							RecordSet rswf = new RecordSet();
							rswf.executeSql("select * from HrmUserSetting where resourceId = "+userid);
							if(rswf.next()){
								belongtoshow = rswf.getString("belongtoshow");
							}
							String tempsqlwhere = " where 1 =1";
							if("171".equals(browserId)){
								tempsqlwhere +=" and workflow_requestbase.currentnodetype = 3";
							}
							if("oracle".equals(rswf.getDBType())){
								tempsqlwhere += " and (nvl(workflow_requestbase.currentstatus,-1) = -1 or (nvl(workflow_requestbase.currentstatus,-1)=0 and workflow_requestbase.creater="+user.getUID()+"))";
							} else {
								tempsqlwhere += " and (isnull(workflow_requestbase.currentstatus,-1) = -1 or (isnull(workflow_requestbase.currentstatus,-1)=0 and workflow_requestbase.creater="+user.getUID()+"))";
							}
					
							if("1".equals(belongtoshow)){
								if (rswf.getDBType().equals("oracle") || rswf.getDBType().equals("db2")) {
									sqlfrom=" ("+
									" select distinct workflow_requestbase.requestid ,requestname,creater,creatertype,createdate,createtime,createdate||' '||createtime as createtimes from workflow_requestbase , workflow_currentoperator , workflow_base" +tempsqlwhere+
									" and workflow_currentoperator.requestid = workflow_requestbase.requestid and workflow_currentoperator.userid in (" +userid + ") and workflow_currentoperator.usertype="+usertype+
									" and workflow_requestbase.workflowid = workflow_base.id and (workflow_base.isvalid='1' or workflow_base.isvalid='3') "+
									" ) t1 ";
								}else{
									sqlfrom="("+
										" select distinct workflow_requestbase.requestid ,requestname,creater,creatertype,createdate,createtime,createdate+' '+createtime as createtimes from workflow_requestbase , workflow_currentoperator , workflow_base" +tempsqlwhere+
										" and workflow_currentoperator.requestid = workflow_requestbase.requestid and workflow_currentoperator.userid in (" +userid + ") and workflow_currentoperator.usertype="+usertype+
										" and workflow_requestbase.workflowid = workflow_base.id and (workflow_base.isvalid='1' or workflow_base.isvalid='3') "+
										" ) t1 ";
								}
							}else{
								if (rswf.getDBType().equals("oracle") || rswf.getDBType().equals("db2")) {
									sqlfrom="  ("+
									" select distinct workflow_requestbase.requestid ,requestname,creater,creatertype,createdate,createtime,createdate||' '||createtime as createtimes from workflow_requestbase , workflow_currentoperator , workflow_base" +tempsqlwhere+
									" and workflow_currentoperator.requestid = workflow_requestbase.requestid and workflow_currentoperator.userid=" +userid + " and workflow_currentoperator.usertype="+usertype+
									" and workflow_requestbase.workflowid = workflow_base.id and (workflow_base.isvalid='1' or workflow_base.isvalid='3') "+
									" ) t1 ";
								}else{
									sqlfrom=" ("+
										" select distinct workflow_requestbase.requestid ,requestname,creater,creatertype,createdate,createtime,createdate+' '+createtime as createtimes from workflow_requestbase , workflow_currentoperator , workflow_base" +tempsqlwhere+
										" and workflow_currentoperator.requestid = workflow_requestbase.requestid and workflow_currentoperator.userid=" +userid + " and workflow_currentoperator.usertype="+usertype+
										" and workflow_requestbase.workflowid = workflow_base.id and (workflow_base.isvalid='1' or workflow_base.isvalid='3') "+
										" ) t1 ";
								}
							}
							orderby  = "createdate desc , createtime desc";
						}else if("137".equals(browserId)){
							RecordSet rscar = new RecordSet();
							rscar.executeSql("select carsdetachable from SystemSet");
							int detachable=0;
							if(rscar.next()){
							    detachable=rscar.getInt(1);
							}
							int userid=user.getUID();
							if(detachable==1){
								if(userid!=1){
									String sqltmp = "";
									String blonsubcomid="";
									char flag=Util.getSeparator();
									rscar.executeProc("HrmRoleSR_SeByURId", ""+userid+flag+"Car:Maintenance");
									while(rscar.next()){
										blonsubcomid=rscar.getString("subcompanyid");
										sqltmp += (", "+blonsubcomid);
									}
									if(!"".equals(sqltmp)){//角色设置的权限
										sqltmp = sqltmp.substring(1);
										sqlwhere2 += " and subcompanyid in ("+sqltmp+") ";
									}else{
										sqlwhere2 += " and subcompanyid="+user.getUserSubCompany1() ;
									}
								}
							}
							sortWay = 1;
							single = true;
						}else if("23".equals(browserId)){
							RecordSet rscap = new RecordSet();
							rscap.executeSql("select cptdetachable from SystemSet");
							int detachable=0;
							if(rscap.next()){
							   detachable=rscap.getInt("cptdetachable");
							}
							int userid=user.getUID();
							int belid = user.getUserSubCompany1();
							char flag=Util.getSeparator();
							String rightStr = "";
							if(HrmUserVarify.checkUserRight("Capital:Maintenance",user)){
								rightStr = "Capital:Maintenance";
							}
					
							sqlwhere2 += " and isdata = '2' ";
							if(detachable == 1 && userid!=1){
								String sqltmp = "";
								String blonsubcomid="";
								rscap.executeProc("HrmRoleSR_SeByURId", ""+userid+flag+rightStr);
								while(rscap.next()){
								    blonsubcomid=rscap.getString("subcompanyid");
									sqltmp += (", "+blonsubcomid);
								}
								if(!"".equals(sqltmp)){//角色设置的权限
									sqltmp = sqltmp.substring(1);
									sqlwhere2 += " and blongsubcompany in ("+sqltmp+") ";
								}else{
									sqlwhere2 += " and blongsubcompany in ("+belid+") ";
								}
							}
							CommonShareManager.setAliasTableName("t2");
							sqlwhere2+= " and exists(select 1 from CptCapitalShareInfo t2 where t2.relateditemid=t1.id and ("+CommonShareManager.getShareWhereByUser("cpt", user)+") ) ";
							
							if("oracle".equalsIgnoreCase(rscap.getDBType())){
								sqlwhere2 += " and (nvl(capitalnum,0)-nvl(frozennum,0))>0 ";
							}else{
								sqlwhere2 += " and (isnull(capitalnum,0)-isnull(frozennum,0))>0 ";
							}
						}
						
						sqlwhere = sqlwhere2 + sqlwhere + " ";
						
						if(!"0".equals(customid) && customid != null){
							String formID = "0";
							String formmodeid = "0";
							RecordSet reS = new RecordSet();
							reS.execute("select a.modeid,a.customname,a.customdesc,a.formid from  mode_custombrowser a where  a.id=" + customid);
							if (reS.next()) {
								formID = Util.null2String(reS.getString("formid"));
								formmodeid = Util.null2String(reS.getString("modeid"));
							}
							
							boolean isVirtualForm = VirtualFormHandler.isVirtualForm(formID);
							
							//虚拟表单需求重新获取主键
							if(isVirtualForm){
								Map<String, Object> vFormInfo = new HashMap<String, Object>();
								vFormInfo = VirtualFormHandler.getVFormInfo(formID);
								keyfield = Util.null2String(vFormInfo.get("vprimarykey"));	//虚拟表单主键列名称
							}
							if(!isVirtualForm){// 虚拟表单不考虑权限
								
								List<User> lsUser = ModeRightInfo.getAllUserCountList(user);
			
								if (formmodeid.equals("") || formmodeid.equals("0")) {// 浏览框中没有设置模块
									String sqlStr1 = "select id,modename from modeinfo where formid=" + formID + " order by id";
									RecordSet rs1 = new RecordSet();
									rs1.executeSql(sqlStr1);
									while (rs1.next()) {
										String mid = rs1.getString("id");
										ModeShareManager.setModeId(Util.getIntValue(mid, 0));
										for (int i = 0; i < lsUser.size(); i++) {
											User tempUser = lsUser.get(i);
											String tempRightStr = ModeShareManager.getShareDetailTableByUser("formmode", tempUser);
											if (rightsql.isEmpty()) {
												rightsql += tempRightStr;
											} else {
												rightsql += " union  all " + tempRightStr;
											}
										}
									}
									if (!rightsql.isEmpty()) {
										rightsql = " (SELECT  sourceid,MAX(sharelevel) AS sharelevel from ( " + rightsql + " ) temptable group by temptable.sourceid) ";
									}
								} else {
									ModeShareManager.setModeId(Util.getIntValue(formmodeid, 0));
									for (int i = 0; i < lsUser.size(); i++) {
										User tempUser = (User) lsUser.get(i);
										String tempRightStr = ModeShareManager.getShareDetailTableByUser("formmode", tempUser);
										if (rightsql.isEmpty()) {
											rightsql += tempRightStr;
										} else {
											rightsql += " union  all " + tempRightStr;
										}
									}
									if (!rightsql.isEmpty()) {
										rightsql = " (SELECT  sourceid,MAX(sharelevel) AS sharelevel from ( " + rightsql + " ) temptable group by temptable.sourceid) ";
									}
								}
								
							}
							
							if (StringHelper.isNotEmpty(rightsql)) {
								List<Map<String, Object>> selectDataList1 = new ArrayList<Map<String, Object>>();
								BaseDao baseDao1 = new BaseDao();
								selectDataList1 = baseDao1.getResultByList(rightsql);
								if(!selectDataList1.isEmpty()){
									if(selectDataList1.size() > 1){
										sqlwhere += " and t1.id in (";
										String sourceids = "";
										for(Map<String, Object> map : selectDataList1){
							    	 		String sourceid = Util.null2String(map.get("sourceid"));
											sourceids += ("," + sourceid);
							    	 	}
							    	 	sqlwhere += (sourceids.substring(1) + ") ");
									}else{
										String sourceid = Util.null2String(selectDataList1.get(0).get("sourceid"));
										sqlwhere += (" and t1.id = "+sourceid);
									}
						    	 }
							}
							orderby = browserInfoService.getOrderSQL(customid);
						}
						
						if(StringHelper.isEmpty(keyfield)){
							keyfield = "id";
						}
						if(!"".equals(orderby)){
							String orderbyStr = orderby.toLowerCase().replaceAll("t1.","").replaceAll("asc","").replaceAll("desc","");
							List orderbyList = StringHelper.string2ArrayList(orderbyStr,",");
							for(int idx = 0; idx < orderbyList.size(); idx++){
								String orderbyFColumnName = StringHelper.null2String(orderbyList.get(idx)).trim();
								if(!viewfield.toLowerCase().equals(orderbyFColumnName) && !keyfield.toLowerCase().equals(orderbyFColumnName)){
									orderbyField += ","+orderbyFColumnName;
								}
							}
						}
						
						SplitPageParaBean spp = new SplitPageParaBean();
						spp.setSqlFrom(sqlfrom);
						spp.setPrimaryKey(keyfield);
						if(keyfield.equalsIgnoreCase(viewfield)){
		     				spp.setBackFields(viewfield + expandfield + orderbyField);
					    }else{
					     	spp.setBackFields(keyfield + "," + viewfield + expandfield + orderbyField);
					    }
						spp.setSqlWhere(sqlwhere);
						spp.setPoolname(poolname);
						spp.setSqlOrderBy(orderby);
						spp.setSortWay(sortWay);
						SplitPageUtil spu = new SplitPageUtil();
						spu.setSpp(spp);
						RecordSet rs2 = spu.getCurrentPageRs(pageNo,pageSize);
						totalSize = spu.getRecordCount();
						while(rs2.next()) {
							String objid = rs2.getString(keyfield);
							String objname = rs2.getString(viewfield);
							String objname2 = rs2.getString("exp1");
							
							JSONObject jsonObj = new JSONObject();
							jsonObj.put("id",objid);
							jsonObj.put("objname",objname);
							jsonObj.put("objname2", objname2);
							dataArr.add(jsonObj);
						}
						
						String selectedIds = Util.null2String(request.getParameter("selectedIds"));
						if(!selectedIds.equals("")){
							String sqlIds = StringHelper.formatMutiIDs(selectedIds);
							String sqlSel = "select " + keyfield + "," + viewfield + expandfield + " from " + reftable + " t1 where t1." + keyfield + " in (" + sqlIds + ")";
							RecordSet rsSel = new RecordSet();
							if (!"".equals(poolname)) {
								rsSel.executeSql(sqlSel, poolname);
							} else {
								rsSel.executeSql(sqlSel);
							}
							
							while(rsSel.next()) {
								String objid = rsSel.getString(keyfield);
								String objname = rsSel.getString(viewfield);
								String objname2 = rsSel.getString("exp1");
								
								JSONObject jsonObj = new JSONObject();
								jsonObj.put("id",objid);
								jsonObj.put("objname",objname);
								jsonObj.put("objname2", objname2);
								selDataArr.add(jsonObj);
							}
						}
					}
				}else{
					
					String browserType = Util.null2String(request.getParameter("browserType"));
					 //请假类型
					if("listLeaveType".equals(method)){
						try{
							/*RecordSet rs = new RecordSet();
							String sqlCount = " select count(0) as count from hrmLeaveTypeColor t where t.field002 = 1 ";
							if(StringHelper.isNotEmpty(searchKey)) sqlCount+= " and (t.field001 like '%" + searchKey + "%') ";
							rs.executeSql(sqlCount);
							if (rs.next()) {
								totalSize = Util.getIntValue(rs.getString("count"), 0);
							}
							String selsql = "select t.field004 as id2, t.field001, t.field001 as show2, t.field004 as id from hrmLeaveTypeColor t ";
							sqlwhere = " where t.field002 = 1";
							if(StringHelper.isNotEmpty(searchKey)) {
								sqlwhere += " and (t.field001 like '%" + searchKey + "%') ";
							}
							selsql+=sqlwhere+" order by id asc";
							rs.executeSql(selsql);*/
							
							String sqlform = " from hrmLeaveTypeColor ";
							String backfields = "field004, field006, field004 as id2, field001, field001 as show2, field004 as id";
							sqlwhere = " where field002 = 1";
							if(StringHelper.isNotEmpty(searchKey)) {
								sqlwhere += " and (field001 like '%" + searchKey + "%') ";
							}
							
							SplitPageParaBean spp = new SplitPageParaBean();
							spp.setSqlFrom(sqlform);
							spp.setPrimaryKey("field004");
							spp.setBackFields(backfields);
							spp.setSqlWhere(sqlwhere);
							spp.setPoolname(poolname);
							spp.setSqlOrderBy("field006");
							spp.setSortWay(0);
							SplitPageUtil spu = new SplitPageUtil();
							spu.setSpp(spp);
							RecordSet rs = spu.getCurrentPageRs(pageNo,pageSize);
							totalSize = spu.getRecordCount();
							while(rs.next()) {
								String objid = rs.getString("id");
								String objname = rs.getString("field001");
								String objname2 = rs.getString("");
								
								JSONObject jsonObj = new JSONObject();
								jsonObj.put("id",objid);
								jsonObj.put("objname",objname);
								jsonObj.put("objname2", objname2);
								dataArr.add(jsonObj);
							}
							String selectedIds = Util.null2String(request.getParameter("selectedIds"));
							if(!selectedIds.equals("")){
								String sqlIds = StringHelper.formatMutiIDs(selectedIds);
								String sqlSel = "select field004,field001 from hrmLeaveTypeColor t1 where t1.field004 in (" + sqlIds + ")";
								RecordSet rsSel = new RecordSet();
								rsSel.executeSql(sqlSel);
								while(rsSel.next()) {
									String objid = rsSel.getString("field004");
									String objname = rsSel.getString("field001");
									String objname2 = rsSel.getString("");
									
									JSONObject jsonObj = new JSONObject();
									jsonObj.put("id",objid);
									jsonObj.put("objname",objname);
									jsonObj.put("objname2", objname2);
									selDataArr.add(jsonObj);
								}
							}
						} catch(Exception e) {
							e.printStackTrace();
						}
					}else if("listWorkflowRequest".equals(method)){ //关联流程
						String[] condition = null;
						sqlwhere = Util.null2String(request.getParameter("sqlwhere"));
						WorkflowService wfs = new WorkflowServiceImpl();
						condition = new String[]{" (t1.requestnamenew like '%" + searchKey + "%') "+sqlwhere};
						
						totalSize = wfs.getAllWorkflowRequestCount(user.getUID(), condition);
						WorkflowRequestInfo[] wfreqinfos = wfs.getAllWorkflowRequestList(pageNo, pageSize, totalSize, user.getUID(), condition);
						List resultList = new ArrayList();
						for(WorkflowRequestInfo wRequestInfo : wfreqinfos){
							String objid = wRequestInfo.getRequestId();
							String objname = wRequestInfo.getRequestName();
							String objname2 = "";
							
							JSONObject jsonObj = new JSONObject();
							jsonObj.put("id",objid);
							jsonObj.put("objname",objname);
							jsonObj.put("objname2", objname2);
							dataArr.add(jsonObj);
						}
						
						String selectedIds = Util.null2String(request.getParameter("selectedIds"));
						if(!selectedIds.equals("")){
							String sqlIds = StringHelper.formatMutiIDs(selectedIds);
							String sqlSel =  "select * from (select distinct t1.requestid,t1.requestname "
							+"from workflow_requestbase t1,workflow_currentoperator t2,workflow_base t3 " 
							+"where t1.requestid=t2.requestid  and t3.id=t2.workflowid and t3.isvalid in('1','3') "  
							+"and t2.usertype = 0 and islasttimes=1 and islasttimes=1 and t1.requestid in("+sqlIds+")) tableA ";
							RecordSet rsSel = new RecordSet();
							rsSel.executeSql(sqlSel);
							while(rsSel.next()) {
								String objid = rsSel.getString("requestid");
								String objname = rsSel.getString("requestname");
								String objname2 = rsSel.getString("");
								
								JSONObject jsonObj = new JSONObject();
								jsonObj.put("id",objid);
								jsonObj.put("objname",objname);
								jsonObj.put("objname2", objname2);
								selDataArr.add(jsonObj);
							}
						}
						
					}else if("listFnaBudgetFeeType".equals(method)){

						try{
							int fnaworkflowid = -1;
							String keyword = searchKey;
							
							RecordSet rs = new RecordSet();
							
							//财务 QC141205 科目应用范围过滤
							String orgClause = " ";
							if(false){//是自定义费控流程
//								boolean subjectFilter = false;
//								String _sql4FnaSystemSet = "select * from FnaSystemSet";
//								rs.executeSql(_sql4FnaSystemSet);
//								while(rs.next()){
//									subjectFilter = 1==Util.getIntValue(rs.getString("subjectFilter"), 0);
//								}
//								
//								if(subjectFilter){
//									if(this.getOrgid() > 0 && this.getOrgtype() > -1){
//										orgClause = " ( \n"+
//												" not exists (select 1 from FnabudgetfeetypeRuleSet ftRul \n"+
//												" 		where ftRul.type = "+this.getOrgtype()+" and ftRul.mainid = a3.id ) \n"+
//												" or \n"+
//												" exists (select 1 from FnabudgetfeetypeRuleSet ftRul \n"+
//												" 		where ftRul.type = "+this.getOrgtype()+" and ftRul.orgid = "+this.getOrgid()+" and ftRul.mainid = a3.id ) \n"+
//												" ) \n";
//									}else{
//										orgClause = " 1=2 ";
//									}
//								}else{
//									orgClause = " 1=1 ";
//								}
							}else{
								orgClause = " 1=1 ";
							}
							
							String sql = "select * from workflow_base where id = " + fnaworkflowid;
							rs.executeSql(sql);
							rs.next();
							int isbill = rs.getInt("isbill");
							int formid = Math.abs(rs.getInt("formid"));
							
							if(isbill == 1){
//								sql = "select * from workflow_billfield where (fieldname = 'subject' or fieldname = 'feetypeid') and billid = " + formid;
//								rs.executeSql(sql);
//								if(rs.next()){
//									this.setFnafieldid(rs.getString("id"));
//								}
							}
							String fnafieldid = "";
							if(fnaworkflowid > 0 && !"".equals(Util.null2String(fnafieldid).trim())){
								fnafieldid = fnafieldid.split("_")[0].replaceAll("field", "");
							}
							
							String wfFeetypeClause = "";
							if(fnaworkflowid > 0 && Util.getIntValue(fnafieldid) > 0){
								sql = " SELECT count(*) cnt \n" +
									" from FnaFeetypeWfbrowdef_dt1 t1 \n" +
									" join FnaFeetypeWfbrowdef t2 on t1.mainid = t2.id \n" +
									" where t2.fieldType = "+BudgetfeeTypeComInfo.FNAFEETYPE_FIELDTYPE+"\n" +
									" and t2.fieldId = "+Util.getIntValue(fnafieldid)+"\n" +
									" and t2.workflowid = "+fnaworkflowid+" ";
								rs.executeSql(sql);
								if(rs.next() && rs.getInt("cnt") > 0){
									wfFeetypeClause = " and ( exists ( "+
											" SELECT 1 \n" +
											" from FnaFeetypeWfbrowdef_dt1 t1 \n" +
											" join FnaFeetypeWfbrowdef t2 on t1.mainid = t2.id \n" +
											" where "+FnaCostCenter.getDbUserName()+"checkSubjectById(t1.refid, a3.id) = 1 \n"+
											" and t2.fieldType = "+BudgetfeeTypeComInfo.FNAFEETYPE_FIELDTYPE+"\n" +
											" and t2.fieldId = "+Util.getIntValue(fnafieldid)+"\n" +
											" and t2.workflowid = "+fnaworkflowid+"\n"+
											" ) ) ";
								}
							}
							
							
							String sqlCount = "select count(1) as count  \n" +
									" from FnaBudgetfeeType a1 \n" +
									" join FnaBudgetfeeType a2 on a1.id = a2.supsubject \n" +
									" join FnaBudgetfeeType a3 on a2.id = a3.supsubject \n" +
									" where "+orgClause+" \n"+
									wfFeetypeClause + "\n" +
									" and (a1.Archive = 0 or a1.Archive is null) \n" +
									" and (a2.Archive = 0 or a2.Archive is null) \n" +
									" and (a3.Archive = 0 or a3.Archive is null) \n" +
									" and a3.feelevel = 3 ";
//							log.info("Following is the run SQL:\n" + sqlCount);
//							new BaseBean().writeLog("sqlCount>>>>>"+sqlCount);
							rs.executeSql(sqlCount);
							int count = 0;
							if (rs.next()) {
								count = rs.getInt(1);
							}
							
							totalSize = count;
							String sqlform = " from FnaBudgetfeeType a1 \n" +
									" join FnaBudgetfeeType a2 on a1.id = a2.supsubject \n" +
									" join FnaBudgetfeeType a3 on a2.id = a3.supsubject \n";
							
							String backfields = " a3.ID,a3.name";
							if ("oracle".equalsIgnoreCase(rs.getDBType())) {
								backfields += ", a1.name || '/' || a2.name  as show2";
							} else {
								backfields += ", CONVERT(NVARCHAR(200), a1.name) + '/' + CONVERT(NVARCHAR(200), a2.name)  as show2";
							}
							backfields += ",a3.codename ";
							
							sqlwhere = orgClause+" "+
									wfFeetypeClause + "\n" +
									" and (a1.Archive = 0 or a1.Archive is null) " +
									" and (a2.Archive = 0 or a2.Archive is null) " +
									" and (a3.Archive = 0 or a3.Archive is null) " +
									" and a3.feelevel = 3 ";
							if(StringUtils.isNotEmpty(keyword)){
								sqlwhere += " and (a3.codeName LIKE '%" + keyword + "%' OR a3.name LIKE '%" + keyword + "%')";
							}
							
							String orderBy = "a3.codeName,a3.name";
							String sqlprimarykey  = "a3.id";
							
							List result = this.getLimitPageData(backfields, sqlform, sqlwhere, sqlprimarykey, orderBy, 0, pageNo, pageSize, 3);
							for (int i = 0; i < result.size(); i++) {
								Map map= (Map)result.get(i);
								JSONObject jsonObj = new JSONObject();
								jsonObj.put("id",map.get("id"));
								jsonObj.put("objname",map.get("show1")+" - "+map.get("show2"));
								jsonObj.put("objname2", map.get("show2"));
								dataArr.add(jsonObj);
							}
						} catch(Exception e) {
							//log.error("Catch a exception",e);
						}
					}
					 
					if("0".equals(browserType)){
						single = true;
					}else{
						single = false;
					}
					
				}

				long curr2 = System.currentTimeMillis() - curr;
				
				resultObj.put("time", curr2);
				resultObj.put("single", single);
				resultObj.put("totalSize", totalSize);
				resultObj.put("datas", dataArr);
				resultObj.put("sel_datas", selDataArr);
				resultObj.put("status", "1");
			} catch (Exception ex) {
				ex.printStackTrace();
				resultObj.put("status", "0");
				resultObj.put("errMsg", ex.getMessage());
			}finally{
				try {
					response.setContentType("application/json; charset=utf-8");  
					response.getWriter().print(resultObj.toString());
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
	}
	
	private List getLimitPageData(String backfields, String sqlform, String sqlwhere, String sqlprimarykey, String orderBy, int sortWay, int pageNo, int pageSize, int fieldCount) {
		SplitPageParaBean sppb = new SplitPageParaBean();
		sppb.setBackFields(backfields);
        sppb.setSqlFrom(sqlform);
        sppb.setSqlWhere(sqlwhere);
        sppb.setPrimaryKey(sqlprimarykey);
        sppb.setSqlOrderBy(orderBy);
        sppb.setSortWay(sortWay);
        sppb.setDistinct(true);
        sppb.setIsPrintExecuteSql(true);
        return this.getLimitPageData(sppb, pageNo, pageSize, fieldCount);
	}
	
	private List getLimitPageData(SplitPageParaBean sppb, int pageNo, int pageSize, int fieldCount) {
		List resultList = new ArrayList(); 
		
        SplitPageUtil spu=new SplitPageUtil();
        spu.setSpp(sppb);
        RecordSet rs = spu.getCurrentPageRs(pageNo, pageSize);
        while (rs.next()) {
			Map mapBean = new HashMap(); 
			mapBean.put("id", Util.getIntValue(rs.getString(1), 0));
			for (int i=1; i<fieldCount; i++) {
				mapBean.put("show" + i, Util.null2String(rs.getString(i + 1)));
			}
			resultList.add(mapBean);
        }
        
        return resultList;
	}

	/*获取人员信息列表接口
	 * params 
     * pageno 当前页数(必需)
	 * pagesize 每页条数(必需)
	 * sqlwhere 过滤条件(非必须，如sqlwhere = " and a.lastname like '%张三%' ");
	 * */
	public void convertGetUserListData() throws Exception{
		
		RecordSet rs = new RecordSet();
		SubCompanyComInfo subCompanyComInfo = null;
		DepartmentComInfo departmentComInfo = null;
		JobTitlesComInfo jobTitlesComInfo = null;
		try {
			subCompanyComInfo = new SubCompanyComInfo();
			departmentComInfo = new DepartmentComInfo();
			jobTitlesComInfo = new JobTitlesComInfo();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		int recordCount=0;
		int pageno = Util.getIntValue(request.getParameter("pageno"),1);
		int pagesize = Util.getIntValue(request.getParameter("pagesize"),10);
		String sqlwhereParam = Util.null2String(request.getParameter("sqlwhere"));
		sqlwhereParam = sqlwhereParam.trim();
		
		String sqlwhere = " and (status = 0 or status = 1 or status = 2 or status = 3) ";
		sqlwhere = " and a.status=1 ";
		if(!"".equals(sqlwhereParam)){
			if(sqlwhereParam.startsWith("and")){
				sqlwhere += sqlwhereParam;
			}else{
				sqlwhere += " and "+sqlwhereParam;
			}
		}
		
		String selSql = "select count(1) from hrmresource a join hrmdepartment b on a.departmentid = b.id where 1=1 "+sqlwhere;
		rs.executeSql(selSql);
		if(rs.next()){
			recordCount = rs.getInt(1);
		}
		int iNextNum = pageno * pagesize;
		int ipageset = pagesize;
		if(recordCount - iNextNum + pagesize < pagesize) ipageset = recordCount - iNextNum + pagesize;
		//if(recordCount < pagesize) ipageset = recordCount;
		
		selSql = "select top " + iNextNum +" b.showorder,convert(int,a.dsporder) as dsporders,a.*,(select lastname from hrmresource where id=a.managerid) manageridname "
		+"from hrmresource a join hrmdepartment b on a.departmentid = b.id  where 1=1 "+sqlwhere+" order by b.showorder,convert(int,a.dsporder) ";
		selSql = "select top " + ipageset +" t1.* from (" + selSql + ") t1 order by t1.showorder desc,t1.dsporders desc";
		selSql = "select top " + ipageset +" t2.* from (" + selSql + ") t2 order by t2.showorder asc,t2.dsporders asc";
		System.out.println(selSql);
		rs.executeSql(selSql);
		JSONArray jsonArray = new JSONArray();
		while(rs.next()){
			JSONObject json = new JSONObject();
			String id = rs.getString("id");
			json.put("id", id);
			json.put("lastname", Util.null2String(rs.getString("lastname")));
			String sex = Util.null2String(rs.getString("sex"));
			json.put("sex", sex);
			json.put("sexname", "");
			if("0".equals(sex)){
				json.put("sexname", "男");
			}else if("1".equals(sex)){
				json.put("sexname", "女");
			}
			json.put("birthday", Util.null2String(rs.getString("birthday")));
			json.put("telephone", Util.null2String(rs.getString("telephone")));
			json.put("mobile", Util.null2String(rs.getString("mobile")));
			json.put("email", Util.null2String(rs.getString("email")));
			String jobtitle = Util.null2String(rs.getString("jobtitle"));
			json.put("jobtitle", jobtitle);
			String jobtitlename = jobTitlesComInfo.getJobTitlesname(jobtitle);
			json.put("jobtitlename", jobtitlename);
			String departmentid = Util.null2String(rs.getString("departmentid"));
			json.put("departmentid", departmentid);
			String departmentname = departmentComInfo.getDepartmentname(departmentid);
			json.put("departmentname", departmentname);
			String subcompanyid1 =  Util.null2String(rs.getString("subcompanyid1"));
			json.put("subcompanyid1", subcompanyid1);
			String subcompanyid1name = subCompanyComInfo.getSubCompanyname(subcompanyid1);
			json.put("subcompanyid1name", subcompanyid1name);
			String managerid = Util.null2String(rs.getString("managerid"));
			json.put("managerid", managerid);
			json.put("manageridname", Util.null2String(rs.getString("manageridname")));
			json.put("msgerurl", Util.null2String(rs.getString("msgerurl")));
			json.put("location", Util.null2String(rs.getString("location")));
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
	
	/*获取人员详细信息接口
	 * params 
     * userid 用户(必需)
	 * */
	public void convertGetUserDetailData(){
		RecordSet rs = new RecordSet();
		SubCompanyComInfo subCompanyComInfo = null;
		DepartmentComInfo departmentComInfo = null;
		JobTitlesComInfo jobTitlesComInfo = null;
		try {
			subCompanyComInfo = new SubCompanyComInfo();
			departmentComInfo = new DepartmentComInfo();
			jobTitlesComInfo = new JobTitlesComInfo();
		} catch (Exception e) {
			e.printStackTrace();
		}
		JSONObject jsonObject = new JSONObject();
		int userid = Util.getIntValue(request.getParameter("userid"));
		String lastname = "";String sex = "";String sexname = "";String birthday = "";String telephone = "";
		String mobile = "";String email = "";String jobtitle = "";String jobtitlename = "";String departmentid = "";String departmentname = "";
		String subcompanyid1 = "";String subcompanyid1name = "";String managerid = "";String manageridname = "";String msgerurl = "";
		String location = "";
		
		rs.executeSql("select a.*,(select lastname from hrmresource where id=a.managerid) manageridname from hrmresource a where a.id='"+userid+"'");
		if(rs.next()){
			lastname = Util.null2String(rs.getString("lastname"));
			sex = Util.null2String(rs.getString("sex"));
			if("0".equals(sex)){
				sexname = "男";
			}else if("1".equals(sex)){
				sexname = "女";
			}
			birthday = Util.null2String(rs.getString("birthday"));
			telephone = Util.null2String(rs.getString("telephone"));
			mobile = Util.null2String(rs.getString("mobile"));
			email = Util.null2String(rs.getString("email"));
			jobtitle = Util.null2String(rs.getString("jobtitle"));
			jobtitlename = jobTitlesComInfo.getJobTitlesname(jobtitle);
			departmentid = Util.null2String(rs.getString("departmentid"));
			departmentname = departmentComInfo.getDepartmentname(departmentid);
			subcompanyid1 = Util.null2String(rs.getString("subcompanyid1"));
			subcompanyid1name = subCompanyComInfo.getSubCompanyname(subcompanyid1);
			managerid = Util.null2String(rs.getString("managerid"));
			manageridname = Util.null2String(rs.getString("manageridname"));
			msgerurl = Util.null2String(rs.getString("msgerurl"));
			location = Util.null2String(rs.getString("location"));
		}
		jsonObject.put("id", userid);
		jsonObject.put("lastname", lastname);
		jsonObject.put("sexname", sexname);
		jsonObject.put("birthday", birthday);
		jsonObject.put("telephone", telephone);
		jsonObject.put("mobile", mobile);
		jsonObject.put("email", email);
		jsonObject.put("jobtitle", jobtitle);
		jsonObject.put("jobtitlename", jobtitlename);
		jsonObject.put("departmentid", departmentid);
		jsonObject.put("departmentname", departmentname);
		jsonObject.put("subcompanyid1", subcompanyid1);
		jsonObject.put("subcompanyid1name", subcompanyid1name);
		jsonObject.put("managerid", managerid);
		jsonObject.put("manageridname", manageridname);
		jsonObject.put("msgerurl", msgerurl);
		jsonObject.put("location", location);
		if(rs.getCounts() <= 0){
			rs.executeSql("select * from hrmresourcemanager where id='"+userid+"'");
			if(rs.next()){
				jsonObject.put("lastname", Util.null2String(rs.getString("lastname")));
			}
		}
		try {
			response.setContentType("application/json; charset=utf-8");  
			response.getWriter().print(jsonObject.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	/*创建流程列表接口*/
	public void convertCreateWorkflowList(User user){
		WorkflowServiceImpl wsi = new WorkflowServiceImpl();
		
		String[] conditions = new String[2];
		conditions[0] = "";
		conditions[1] = weaver.mobile.plugin.ecology.RequestOperation.AVAILABLE_WORKFLOW;
		
		String belongtoshow = user.getBelongtoshowByUserId(user.getUID());
		WorkflowExtInfo[] wbis = null;
		if("1".equals(belongtoshow)){
		   wbis = wsi.getCreateWorkflowList(0, 99999999, 0, user.getUID(), 0, conditions,true);
		}else{
		   wbis = wsi.getCreateWorkflowList(0, 99999999, 0, user.getUID(), 0, conditions);
		}
		
		String wtid = "";
		JSONArray jsonArray = new JSONArray();
		for(int i=0;wbis!=null&&i<wbis.length;i++) {
			JSONObject json = new JSONObject();
			if(!wbis[i].getWorkflowTypeId().equals(wtid)) {
				
			}
			json.put("workflowtypeid", wbis[i].getWorkflowTypeId());
			json.put("workflowtypename", wbis[i].getWorkflowTypeName());
			json.put("workflowid", wbis[i].getWorkflowId());
			json.put("belongtouserid", Util.null2String(wbis[i].getF_weaver_belongto_userid()));
			json.put("belongtousertype", Util.null2String(wbis[i].getF_weaver_belongto_usertype()));
			json.put("workflowname", wbis[i].getWorkflowName());
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
	
	/*获取流程接口包括(1待办、7办结、8已办、9我的请求、10抄送)
	 * params 
     * module 流程类型(必需)
     * pageno 当前页数(必需)
	 * pagesize 每页条数(必需)
	 * */
	public void convertWorkflowData(User user){
		RecordSet rs = new RecordSet();
		PluginServiceImpl pServiceImpl = new PluginServiceImpl();
		WorkFlowTransMethod workFlowTransMethod = new WorkFlowTransMethod();
		ResourceComInfo rc = null;
		CustomerInfoComInfo cci = null;
		try {
			rc = new ResourceComInfo();
			cci = new CustomerInfoComInfo();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		AuthService aService = new AuthService();
		String userid = String.valueOf(user.getUID());
		String module = Util.null2String(request.getParameter("module"));//1待办、7办结、8已办、9我的请求、10抄送
		String scope = Util.null2String(request.getParameter("scope"));
		int pageno = Util.getIntValue(request.getParameter("pageno"),1);
		int pagesize = Util.getIntValue(request.getParameter("pagesize"),10);
		String supid=Util.null2String(request.getParameter("supid"));//1流程与供应商相关，3流程与客户信息相关
		String workflowid = Util.null2String(request.getParameter("workflowid"));//指定流程类id，支持多个流程，如123,456
		String requestname = Util.null2String(request.getParameter("requestname"));//按流程标题搜索
		String createname = Util.null2String(request.getParameter("createname"));//按流程创建人
		Map map = aService.login(userid, "7", request.getRemoteAddr());
		String sessionkey = Util.null2String(map.get("sessionkey"));
		
		List conditions = new ArrayList();
		if(StringHelper.isNotEmpty(workflowid)) {
			if(module.equals("1")||module.equals("7")||module.equals("8")||module.equals("9")||module.equals("10")) {
				String condition = "";
				String cfgstr = workflowid;
				cfgstr = cfgstr.startsWith(",")?cfgstr.substring(1):cfgstr;
				if(StringHelper.isNotEmpty(cfgstr)) {
					String strSubClause = Util.getSubINClause(cfgstr, "t1.workflowid", "IN");
					if("".equals(condition)){
						 condition += strSubClause;
					} else {
						 condition += " or " + strSubClause;
					}
					if (condition != null && !"".equals(condition)) {
						condition = " (" + condition + ") ";
					}
					conditions.add(condition);
				}
			}
		}
		
		Map result = new HashMap();
		Map resetResultMap = new HashMap();
		if(module.equals("1")||module.equals("7")||module.equals("8")||module.equals("9")||module.equals("10")) {
			
			String sqlwhere = "";
			if(StringHelper.isNotEmpty(requestname)){
				sqlwhere = "t1.requestnamenew like '%" + requestname + "%'";
			}
			if(StringHelper.isNotEmpty(createname)) {
				if(StringHelper.isNotEmpty(sqlwhere)){
					sqlwhere += " or ";
				}
				sqlwhere +=" t1.creater in (select id from hrmresource where lastname like '%"+createname+"%')";
			}
			
			if(StringHelper.isNotEmpty(sqlwhere)) {
				conditions.add(" ("+sqlwhere+") ");
			}
			
			if(supid.equals("1")){
				conditions.add(" (t1.workflowid in(select id from workflow_base where workflowtype=55))");
			}
			
			if(supid.equals("2")){
				conditions.add(" (t1.workflowid in(select id from workflow_base where workflowtype=56))");
			}
			
			if(supid.equals("3")){
				conditions.add(" (t1.workflowid in(select id from workflow_base where workflowtype=58))");
			}
			
			try {
				result = (Map) pServiceImpl.getWorkflowList(Util.getIntValue(module), Util.getIntValue(scope), conditions, pageno, pagesize, sessionkey);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if(result!=null&&result.get("list")!=null) {
			
			List<Map<String,Object>> list = (List<Map<String,Object>>) result.get("list");

			if(module.equals("1")||module.equals("7")||module.equals("8")||module.equals("9")||module.equals("10")) {
				List<Map<String,Object>> newlist = new ArrayList<Map<String,Object>>();
				for(Map<String,Object> d:list) {
					Map<String,Object> newdata = new HashMap<String,Object>();
					
					String requestid = StringUtils.defaultIfEmpty((String) d.get("wfid"),"");//流程id
					requestname = StringUtils.defaultIfEmpty((String) d.get("wftitle"),"").replace("&quot;", "\"");///流程标题
					int idx = requestname.indexOf("（流程标题:");
					if(idx > -1){
						requestname = requestname.substring(idx+6,requestname.length());
						if(requestname.endsWith("）")){
							requestname = requestname.substring(0, requestname.length()-1);
						}
					}else{
						idx = requestname.indexOf("（标题:");
						if(idx > -1){
							requestname = requestname.substring(idx+4,requestname.length());
							if(requestname.endsWith("）")){
								requestname = requestname.substring(0, requestname.length()-1);
							}
						}
					}
					String importantlevel = StringUtils.defaultIfEmpty((String) d.get("implev"),"");//安全级别，0 正常  1 重要  2紧急
					if(importantlevel.equals("0")){
						importantlevel = "正常";
					}else if(importantlevel.equals("1")){
						importantlevel = "重要";
					}else if(importantlevel.equals("2")){
						importantlevel = "紧急";
					}
					String requestnamenew = StringUtils.defaultIfEmpty((String) d.get("requestNameNew"),"");//requestnamenew
					String workflowname = StringUtils.defaultIfEmpty((String) d.get("wftype"),"");//流程名称
					String createrid = StringUtils.defaultIfEmpty((String) d.get("creatorid"),"");//创建人id
					String creatername = StringUtils.defaultIfEmpty((String) d.get("creator"),"");//创建人姓名
					String createrimage = StringUtils.defaultIfEmpty((String) d.get("creatorpic"),"");//创建人头像
					String createtime = StringUtils.defaultIfEmpty((String) d.get("createtime"),"");//创建时间
					String receivetime = StringUtils.defaultIfEmpty((String) d.get("recivetime"),"");//接收时间
					String status = StringUtils.defaultIfEmpty((String) d.get("status"),"");//出口名称
					String appurl = StringUtils.defaultIfEmpty((String) d.get("appurl"),"");//统一待办访问地址
					String currentnodeid = StringUtils.defaultIfEmpty((String) d.get("currentnodeid"),"");//当前节点id
					String currentnodename = StringUtils.defaultIfEmpty((String) d.get("currentnodename"),"");//当前节点名称
					if("".equals(currentnodename)){
						rs.executeSql("select nodename from workflow_nodebase where id='"+currentnodeid+"'");
						if(rs.next()){
							currentnodename = Util.null2String(rs.getString("nodename"));
						}
					}
					String htime = workFlowTransMethod.getCurrentUseTime(requestid);//耗时
					if(!"".equals(htime) && htime.indexOf("时") > -1){
						htime = htime.substring(0,htime.indexOf("时")+1);
					}else if(!"".equals(htime) && htime.indexOf("分") >-1 ){
						htime = htime.substring(0,htime.indexOf("分")+1);
					}else{
						htime = "1分";
					}
					
					String nooperation = "";//未操作者
					rs.executeSql("select distinct userid,usertype,agenttype,agentorbyagentid,isremark from workflow_currentoperator where (isremark in ('0','1','5','7','8','9') or (isremark='4' and viewtype=0))  and requestid = " + requestid);
			        while(rs.next()){
			        	if(rs.getInt("usertype")==0){        		
			        		if(rs.getInt("agenttype")==2){
			        			nooperation +=  ","+rc.getResourcename(rs.getString("agentorbyagentid"))+"->"+rc.getResourcename(rs.getString("userid"));
			        			//判断是否被代理者,如果是，则不显示该记录
		                    }else if(rs.getInt("agenttype")==1 && rs.getInt("isremark") == 4){
		                        continue;
			        		}else{
			        			nooperation +=  ","+rc.getResourcename(rs.getString("userid"));
			        		}
			    		}else{
			    			//TD11591(人力资源与客户同时存在时、加','处理)
			    			nooperation +=  ","+cci.getCustomerInfoname(rs.getString("userid"));
			    		}
			        }
			        if(nooperation.startsWith(",")){
			        	nooperation = nooperation.substring(1,nooperation.length());
			        }
					
					//常用描述
					String description = "" +
										 "[" + workflowname + "]" +
										 "   接收时间 : " + receivetime +
										 "   流程状态 : " + status +
										 "   创建人 : " + creatername +
										 "   创建时间 : " + createtime;
					
					//当主次账号统一显示的时候,获取用户名和用户类型
					String f_weaver_belongto_userid = StringUtils.defaultIfEmpty((String) d.get("f_weaver_belongto_userid"),"");
					String f_weaver_belongto_usertype = StringUtils.defaultIfEmpty((String) d.get("f_weaver_belongto_usertype"),"");
					
					//监控模块不做考虑
					String canMultiSubmit = StringUtils.defaultIfEmpty((String) d.get("canMultiSubmit"),"");//是否可以批量提交
					String formsignaturemd5 = StringUtils.defaultIfEmpty((String) d.get("formsignaturemd5"),"");//添加标签签名信息
					
					newdata.put("userid", userid);
					newdata.put("requestid", requestid);
					newdata.put("requestname", requestname);
					newdata.put("requestnamenew", requestnamenew);
					newdata.put("importantlevel", importantlevel);
					newdata.put("workflowname", workflowname);
					newdata.put("createrid", createrid);
					newdata.put("creatername", creatername);
					newdata.put("createrimage", createrimage);
					newdata.put("createtime", createtime);
					newdata.put("receivetime", receivetime);
					newdata.put("status", status);
					newdata.put("appurl", appurl);
					newdata.put("currentnodeid", currentnodeid);
					newdata.put("currentnodename", currentnodename);
					newdata.put("description", description);
					newdata.put("f_weaver_belongto_userid", f_weaver_belongto_userid);
					newdata.put("f_weaver_belongto_usertype", f_weaver_belongto_usertype);
					newdata.put("canMultiSubmit", canMultiSubmit);
					newdata.put("formsignaturemd5", formsignaturemd5);
					newdata.put("htime", htime);
					newdata.put("nooperation", nooperation);
					newlist.add(newdata);
				}
				resetResultMap.put("totalSize", Integer.parseInt(Util.null2String(result.get("count"))));
				resetResultMap.put("datas", newlist);
			}
		}	
		JSONObject jsonObject = JSONObject.fromObject(resetResultMap);
		try {
			response.setContentType("application/json; charset=utf-8");  
			response.getWriter().print(jsonObject.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	/*获取日程列表接口
	 * params 
     * userid 用户id(必须)
     * pageno 当前页数(必需)
	 * pagesize 每页条数(必需)
	 * */
	public void convertGetSchedule(User user){ 
		
		Calendar thisCalendar = Calendar.getInstance(); //当前日期
		Calendar selectCalendar = Calendar.getInstance(); //用于显示的日期

		int countDays = 0; //需要显示的天数
		int offsetDays = 0; //相对显示显示第一天的偏移天数
		String thisDate = ""; //当前日期
		String selectDate = ""; //用于显示日期

		String beginDate = "";
		String endDate = "";

		String beginYear = "";
		String beginMonth = "";
		String beginDay = "";

		String endYear = "";
		String endMonth = "";
		String endDay = "";

		//参数传递
		RecordSet recordSet = new RecordSet();
		RecordSet rs = new RecordSet();
		String userid = Util.null2String(request.getParameter("userid")); //当前用户Id
		if(userid==null||userid.equals("")){
			userid = String.valueOf(user.getUID());
		}
		String userType = user.getLogintype(); //当前用户类型
		String sql = WorkPlanShareUtil.getShareSql(user);

		String selectUser = Util.null2String(request.getParameter("selectUser")); //被选择用户Id				
		String hasrightview = Util.null2String(request.getParameter("hasrightview")); //是否有权限查看	
		String viewType = request.getParameter("viewtype"); //1:日计划显示 2:周计划显示 3:月计划显示
		String selectDateString = Util.null2String(request.getParameter("selectdate")); //被选择日期
		String isShare = Util.null2String(request.getParameter("isShare")); //是否是共享    1：共享日程
		String selectUserNames = Util.null2String(request.getParameter("selectUserNames")); //查看其他人姓名
		String workPlanType = Util.null2String(request.getParameter("workPlanType")); //被选择用户Id	
		boolean appendselectUser = false;
		viewType = "day".equals(viewType) ? "1": ("week".equals(viewType) ? "2" : "3");

		if ("".equals(selectUser) || userid.equals(selectUser)) {
			appendselectUser = true;
			selectUser = userid;
		}
		
		boolean belongshow = MutilUserUtil.isShowBelongto(user);
		String belongids="";
		if(belongshow){
			belongids=User.getBelongtoidsByUserId(user.getUID());
		}
		
		selectUser = selectUser.replaceAll(",", "");
		if (!"1".equals(isShare) && !"".equals(selectUser)
				&& !userid.equals(selectUser)) {
			boolean hasright = false;
			String tempsql = "select a.managerstr "
					+ "	  from hrmresource a "
					+ "	  where (a.managerstr = '" + userid
					+ "' or a.managerstr like '" + userid + ",%' or "
					+ "	        a.managerstr like '%," + userid
					+ ",%' or a.managerstr like '%," + userid + "') "
					+ "	        and a.id=" + selectUser;
			recordSet.executeSql(tempsql);
			if (recordSet.next()) {
				String tmanagerstr = recordSet.getString(1);
				if (!"".equals(tmanagerstr)) {
					hasright = true;
				}
			}
			if (!hasright) {
				//out.clearBuffer();
			}
		}
		String thisYear = Util.add0((thisCalendar.get(Calendar.YEAR)), 4); //当前年
		String thisMonth = Util.add0((thisCalendar.get(Calendar.MONTH)) + 1, 2); //当前月
		String thisDayOfMonth = Util.add0((thisCalendar.get(Calendar.DAY_OF_MONTH)), 2); //当前日
		thisDate = thisYear + "-" + thisMonth + "-" + thisDayOfMonth;

		if (!"".equals(selectDateString)){//当选择日期
			int selectYear = Util.getIntValue(selectDateString.substring(0,4)); //被选择年
			int selectMonth = Util.getIntValue(selectDateString.substring(5, 7)) - 1; //被选择月
			int selectDay = Util.getIntValue(selectDateString.substring(8,10)); //被选择日
			selectCalendar.set(selectYear, selectMonth, selectDay);
		}

		String selectYear = Util.add0((selectCalendar.get(Calendar.YEAR)),4); //年 
		String selectMonth = Util.add0((selectCalendar.get(Calendar.MONTH)) + 1, 2); // 月
		String selectDayOfMonth = Util.add0((selectCalendar.get(Calendar.DAY_OF_MONTH)), 2); //日    
		String selectWeekOfYear = String.valueOf(selectCalendar.get(Calendar.WEEK_OF_YEAR)); //第几周
		String selectDayOfWeek = String.valueOf(selectCalendar.get(Calendar.DAY_OF_WEEK)); //一周第几天
		selectDate = selectYear + "-" + selectMonth + "-"+ selectDayOfMonth;

		switch (Integer.parseInt(viewType))
		//设置为显示的第一天
		{
		case 1:
			//日计划显示
			offsetDays = 0;
			break;
		case 2:
			//周计划显示
			offsetDays = Integer.parseInt(selectDayOfWeek) - 1;
			selectCalendar.add(Calendar.DAY_OF_WEEK, -1
					* Integer.parseInt(selectDayOfWeek) + 1);
			break;
		case 3:
			//月计划显示
			selectCalendar.set(Calendar.DATE, 1); //设置为月第一天
			int offsetDayOfWeek = selectCalendar.get(Calendar.DAY_OF_WEEK) - 1;
			offsetDays = Integer.parseInt(selectDayOfMonth) - 1+ offsetDayOfWeek;
			selectCalendar.add(Calendar.DAY_OF_WEEK, -1 * offsetDayOfWeek); //设置为月首日那周的第一天
			break;
		}
		beginYear = Util.add0(selectCalendar.get(Calendar.YEAR), 4); //年 
		beginMonth = Util.add0(selectCalendar.get(Calendar.MONTH) + 1, 2); // 月
		beginDay = Util.add0(selectCalendar.get(Calendar.DAY_OF_MONTH), 2); //日 
		beginDate = beginYear + "-" + beginMonth + "-" + beginDay;
		//System.out.println("viewType:" + viewType);
		switch (Integer.parseInt(viewType))
		//设置为显示的最后一天
		{
		case 1:
			//日计划显示
			countDays = 1;
			break;
		case 2:
			//周计划显示
			selectCalendar.add(Calendar.WEEK_OF_YEAR, 1);
			selectCalendar.add(Calendar.DATE, -1);
			countDays = 7;
			break;
		case 3:
			//月计划显示
			selectCalendar.add(Calendar.DATE, offsetDays);
			//System.out.println("######" + selectCalendar.get(Calendar.DATE));
			selectCalendar.set(Calendar.DATE, 1); //设置为月第一天
			selectCalendar.add(Calendar.MONTH, 1);
			selectCalendar.add(Calendar.DATE, -1);
			countDays = selectCalendar.get(Calendar.DAY_OF_MONTH); //当月天数
			int offsetDayOfWeekEnd = 7 - selectCalendar
					.get(Calendar.DAY_OF_WEEK);
			selectCalendar.add(Calendar.DAY_OF_WEEK, offsetDayOfWeekEnd); //设置为月末日那周的最后一天
			break;
		}
		endYear = Util.add0(selectCalendar.get(Calendar.YEAR), 4); //年 
		endMonth = Util.add0(selectCalendar.get(Calendar.MONTH) + 1, 2); // 月
		endDay = Util.add0(selectCalendar.get(Calendar.DAY_OF_MONTH), 2); //日
		endDate = endYear + "-" + endMonth + "-" + endDay;

		String overColor = "";
		String archiveColor = "";
		String archiveAvailable = "0";
		String overAvailable = "0";
		String oversql = "select * from overworkplan order by workplanname desc";
		recordSet.executeSql(oversql);
		while (recordSet.next()) {
			String id = recordSet.getString("id");
			String workplanname = recordSet.getString("workplanname");
			String workplancolor = recordSet.getString("workplancolor");
			String wavailable = recordSet.getString("wavailable");
			if ("1".equals(id)) {
				overColor = workplancolor;
				if ("1".equals(wavailable))
					overAvailable = "1";
			} else {
				archiveColor = workplancolor;
				if ("1".equals(wavailable))
					archiveAvailable = "2";
			}
		}
		if ("".equals(overColor)) {
			overColor = "#c3c3c2";
		}
		if ("".equals(archiveColor)) {
			archiveColor = "#937a47";
		}
		//String temptable = WorkPlanShareBase.getTempTable(userId);
		StringBuffer sqlStringBuffer = new StringBuffer();

		sqlStringBuffer.append("SELECT C.*,overworkplan.workplancolor FROM (SELECT * FROM ");
		sqlStringBuffer.append("(");
		sqlStringBuffer.append("SELECT workPlan.*, workPlanType.workPlanTypeColor,(select lastname from hrmresource where id=workPlan.createrid) createridname ");
		sqlStringBuffer.append(" FROM WorkPlan workPlan, WorkPlanType workPlanType");
		//显示所有日程，包含已结束日程
		//sqlStringBuffer.append(" WHERE (workPlan.status = 0 or workPlan.status = 1 or workPlan.status = 2)");
		sqlStringBuffer.append(" WHERE (workPlan.status = 0 ");
		if("1".equals(overAvailable)){
			sqlStringBuffer.append(" or workPlan.status = 1 ");
		}
		if("2".equals(archiveAvailable)){
			sqlStringBuffer.append(" or workPlan.status = 2 ");
		}
		sqlStringBuffer.append(" ) ");		
		//sqlStringBuffer.append(Constants.WorkPlan_Status_Unfinished);
		/** Add By Hqf for TD9970 Start **/
		sqlStringBuffer.append(" AND workPlan.deleted <> 1");
		if(!"".equals(workPlanType)){
			sqlStringBuffer.append(" AND workPlan.type_n = '"+workPlanType+"'");		
		}
		/** Add By Hqf for TD9970 End **/
		sqlStringBuffer.append(" AND workPlan.type_n = workPlanType.workPlanTypeId");
		sqlStringBuffer.append(" AND workPlan.createrType = '" + userType+ "'");

		////////////////////////////////////////////
		if (!"1".equals(isShare)) {
			sqlStringBuffer.append(" AND (");
			if(appendselectUser&&!"".equals(belongids)){//自己
				sqlStringBuffer.append("(");
					if (recordSet.getDBType().equals("oracle")) {
						sqlStringBuffer.append(" ','||workPlan.resourceID||',' LIKE '%,"+ selectUser + ",%'");
					} else {
						sqlStringBuffer.append(" ','+workPlan.resourceID+',' LIKE '%,"+ selectUser + ",%'");
					}
			
					StringTokenizer idsst = new StringTokenizer(belongids, ",");
					while (idsst.hasMoreTokens()) {
						String id = idsst.nextToken();
						if (recordSet.getDBType().equals("oracle")) {
							sqlStringBuffer.append(" OR ','||workPlan.resourceID||',' LIKE '%,"+ id + ",%'");
						} else {
							sqlStringBuffer.append(" OR ','+workPlan.resourceID+',' LIKE '%,"+ id + ",%'");
						}
					}
					sqlStringBuffer.append(")");
				
			}else{
				if (recordSet.getDBType().equals("oracle")) {
					sqlStringBuffer.append("  ','||workPlan.resourceID||',' LIKE '%,"+ selectUser + ",%'");
				}else{
					sqlStringBuffer.append("  ','+workPlan.resourceID+',' LIKE '%,"+ selectUser + ",%'");
				}
			}
			sqlStringBuffer.append(" )");
		} else {
			if (!appendselectUser) {
				sqlStringBuffer.append(" AND (");
				StringTokenizer namesst = new StringTokenizer(selectUserNames, ",");
				StringTokenizer idsst = new StringTokenizer(selectUser, ",");
				sqlStringBuffer.append(" workPlan.resourceID = '");
				sqlStringBuffer.append(selectUser);
				sqlStringBuffer.append("'");
				while (idsst.hasMoreTokens()) {
					String id = idsst.nextToken();
					if (recordSet.getDBType().equals("oracle")) {
						sqlStringBuffer.append(" OR ','||workPlan.resourceID||',' LIKE '%,"+ id + ",%'");
					} else {
						sqlStringBuffer.append(" OR ','+workPlan.resourceID+',' LIKE '%,"+ id + ",%'");
					}
				}
				sqlStringBuffer.append(")");
			}

		}
		String monthyear = Util.null2String(request.getParameter("monthyear"));
		monthyear = monthyear.trim();
		if(!"".equals(monthyear)){
			sqlStringBuffer.append(" AND (  workPlan.beginDate like '%"+monthyear+"%' or workPlan.endDate  like '%"+monthyear+"%') ");
		}else{
			sqlStringBuffer.append(" AND (  workPlan.beginDate <= '");
			endDate = Util.null2String(request.getParameter("enddate"));
			sqlStringBuffer.append(endDate);
			sqlStringBuffer.append("' and ");
			sqlStringBuffer.append(" workPlan.endDate >= '");
			beginDate = Util.null2String(request.getParameter("begindate"));
			sqlStringBuffer.append(beginDate);
			sqlStringBuffer.append("' )");
		}
		sqlStringBuffer.append(" ) A");
		sqlStringBuffer.append(" JOIN");
		sqlStringBuffer.append(" (");
		sqlStringBuffer.append(sql);
		sqlStringBuffer.append(" ) B");
		sqlStringBuffer.append(" ON A.id = B.workId) C");
		sqlStringBuffer.append(" LEFT JOIN overworkplan ON overworkplan.id=c.status ");
		sqlStringBuffer.append(" WHERE shareLevel >= 1");
		sqlStringBuffer.append(" ORDER BY beginDate asc, beginTime ASC");
		recordSet.executeSql(sqlStringBuffer.toString());
		//System.out.println(sqlStringBuffer.toString());
		Map result = new HashMap();
		List eventslist = new ArrayList();
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		SimpleDateFormat format3 = new SimpleDateFormat("HH:mm");
		
		WorkPlanSetInfo workPlanSetInfo = new WorkPlanSetInfo();
		int timeRangeStart=workPlanSetInfo.getTimeRangeStart();
		int timeRangeEnd=workPlanSetInfo.getTimeRangeEnd();
		String sTime=(timeRangeStart<10?"0"+timeRangeStart:timeRangeStart)+":00";
		String eTime=(timeRangeEnd<10?"0"+timeRangeEnd:timeRangeEnd)+":59";
		
		JSONArray jsArray = new JSONArray();
		while (recordSet.next()) {
			try {
				boolean isAllDay = false;
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("id",recordSet.getString("id"));
				jsonObject.put("name",recordSet.getString("name"));
				jsonObject.put("begindate",Util.null2String(recordSet.getString("begindate")));
				jsonObject.put("begintime",Util.null2String(recordSet.getString("begintime")));
				jsonObject.put("enddate",Util.null2String(recordSet.getString("enddate")));
				jsonObject.put("endtime",Util.null2String(recordSet.getString("endtime")));
				jsonObject.put("description",Util.null2String(recordSet.getString("description")));
				jsonObject.put("status",Util.null2String(recordSet.getString("status")));
				jsonObject.put("createrid",Util.null2String(recordSet.getString("createrid")));
				jsonObject.put("createridname",Util.null2String(recordSet.getString("createridname")));
				jsonObject.put("createdate",Util.null2String(recordSet.getString("createdate")));
				jsonObject.put("createtime",Util.null2String(recordSet.getString("createtime")));
				String resourceid = Util.null2String(recordSet.getString("resourceid"));
				String resourceidname = "";
				if(!"".equals(resourceid)){
					if(resourceid.startsWith(",")){
						resourceid = resourceid.substring(1);
					}
					rs.executeSql("select lastname from hrmresource where id in("+resourceid+")");
					while(rs.next()){
						resourceidname += Util.null2String(rs.getString("lastname"))+",";
					}
					if(resourceidname.endsWith(",")){
						resourceidname = resourceidname.substring(0,resourceidname.length()-1);
					}
				}
				jsonObject.put("resourceid",resourceid);
				jsonObject.put("resourceidname",resourceidname);
				jsArray.add(jsonObject);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("totalSize", jsArray.size());
		jsonObject.put("datas", jsArray.toString());
		try {
			response.setContentType("application/json; charset=utf-8");  
			response.getWriter().print(jsonObject.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	}
	
	/*获取日程列表接口
	 * params 
     * userid 用户id(必须)
     * pageno 当前页数(必需)
	 * pagesize 每页条数(必需)
	 * */
	public void convertGetScheduleByMonth(User user){ 
		
		Calendar thisCalendar = Calendar.getInstance(); //当前日期
		Calendar selectCalendar = Calendar.getInstance(); //用于显示的日期

		int countDays = 0; //需要显示的天数
		int offsetDays = 0; //相对显示显示第一天的偏移天数
		String thisDate = ""; //当前日期
		String selectDate = ""; //用于显示日期

		String beginDate = "";
		String endDate = "";

		String beginYear = "";
		String beginMonth = "";
		String beginDay = "";

		String endYear = "";
		String endMonth = "";
		String endDay = "";

		//参数传递
		RecordSet recordSet = new RecordSet();
		RecordSet rs = new RecordSet();
		String userid = Util.null2String(request.getParameter("userid")); //当前用户Id
		if(userid==null||userid.equals("")){
			userid = String.valueOf(user.getUID());
		}
		String userType = user.getLogintype(); //当前用户类型
		String sql = WorkPlanShareUtil.getShareSql(user);

		String selectUser = Util.null2String(request.getParameter("selectUser")); //被选择用户Id				
		String hasrightview = Util.null2String(request.getParameter("hasrightview")); //是否有权限查看	
		String viewType = request.getParameter("viewtype"); //1:日计划显示 2:周计划显示 3:月计划显示
		String selectDateString = Util.null2String(request.getParameter("selectdate")); //被选择日期
		String isShare = Util.null2String(request.getParameter("isShare")); //是否是共享    1：共享日程
		String selectUserNames = Util.null2String(request.getParameter("selectUserNames")); //查看其他人姓名
		String workPlanType = Util.null2String(request.getParameter("workPlanType")); //被选择用户Id	
		boolean appendselectUser = false;
		viewType = "day".equals(viewType) ? "1": ("week".equals(viewType) ? "2" : "3");

		if ("".equals(selectUser) || userid.equals(selectUser)) {
			appendselectUser = true;
			selectUser = userid;
		}
		
		boolean belongshow = MutilUserUtil.isShowBelongto(user);
		String belongids="";
		if(belongshow){
			belongids=User.getBelongtoidsByUserId(user.getUID());
		}
		
		selectUser = selectUser.replaceAll(",", "");
		if (!"1".equals(isShare) && !"".equals(selectUser)
				&& !userid.equals(selectUser)) {
			boolean hasright = false;
			String tempsql = "select a.managerstr "
					+ "	  from hrmresource a "
					+ "	  where (a.managerstr = '" + userid
					+ "' or a.managerstr like '" + userid + ",%' or "
					+ "	        a.managerstr like '%," + userid
					+ ",%' or a.managerstr like '%," + userid + "') "
					+ "	        and a.id=" + selectUser;
			recordSet.executeSql(tempsql);
			if (recordSet.next()) {
				String tmanagerstr = recordSet.getString(1);
				if (!"".equals(tmanagerstr)) {
					hasright = true;
				}
			}
			if (!hasright) {
				//out.clearBuffer();
			}
		}
		String thisYear = Util.add0((thisCalendar.get(Calendar.YEAR)), 4); //当前年
		String thisMonth = Util.add0((thisCalendar.get(Calendar.MONTH)) + 1, 2); //当前月
		String thisDayOfMonth = Util.add0((thisCalendar.get(Calendar.DAY_OF_MONTH)), 2); //当前日
		thisDate = thisYear + "-" + thisMonth + "-" + thisDayOfMonth;

		if (!"".equals(selectDateString)){//当选择日期
			int selectYear = Util.getIntValue(selectDateString.substring(0,4)); //被选择年
			int selectMonth = Util.getIntValue(selectDateString.substring(5, 7)) - 1; //被选择月
			int selectDay = Util.getIntValue(selectDateString.substring(8,10)); //被选择日
			selectCalendar.set(selectYear, selectMonth, selectDay);
		}

		String selectYear = Util.add0((selectCalendar.get(Calendar.YEAR)),4); //年 
		String selectMonth = Util.add0((selectCalendar.get(Calendar.MONTH)) + 1, 2); // 月
		String selectDayOfMonth = Util.add0((selectCalendar.get(Calendar.DAY_OF_MONTH)), 2); //日    
		String selectWeekOfYear = String.valueOf(selectCalendar.get(Calendar.WEEK_OF_YEAR)); //第几周
		String selectDayOfWeek = String.valueOf(selectCalendar.get(Calendar.DAY_OF_WEEK)); //一周第几天
		selectDate = selectYear + "-" + selectMonth + "-"+ selectDayOfMonth;

		switch (Integer.parseInt(viewType))
		//设置为显示的第一天
		{
		case 1:
			//日计划显示
			offsetDays = 0;
			break;
		case 2:
			//周计划显示
			offsetDays = Integer.parseInt(selectDayOfWeek) - 1;
			selectCalendar.add(Calendar.DAY_OF_WEEK, -1
					* Integer.parseInt(selectDayOfWeek) + 1);
			break;
		case 3:
			//月计划显示
			selectCalendar.set(Calendar.DATE, 1); //设置为月第一天
			int offsetDayOfWeek = selectCalendar.get(Calendar.DAY_OF_WEEK) - 1;
			offsetDays = Integer.parseInt(selectDayOfMonth) - 1+ offsetDayOfWeek;
			selectCalendar.add(Calendar.DAY_OF_WEEK, -1 * offsetDayOfWeek); //设置为月首日那周的第一天
			break;
		}
		beginYear = Util.add0(selectCalendar.get(Calendar.YEAR), 4); //年 
		beginMonth = Util.add0(selectCalendar.get(Calendar.MONTH) + 1, 2); // 月
		beginDay = Util.add0(selectCalendar.get(Calendar.DAY_OF_MONTH), 2); //日 
		beginDate = beginYear + "-" + beginMonth + "-" + beginDay;
		//System.out.println("viewType:" + viewType);
		switch (Integer.parseInt(viewType))
		//设置为显示的最后一天
		{
		case 1:
			//日计划显示
			countDays = 1;
			break;
		case 2:
			//周计划显示
			selectCalendar.add(Calendar.WEEK_OF_YEAR, 1);
			selectCalendar.add(Calendar.DATE, -1);
			countDays = 7;
			break;
		case 3:
			//月计划显示
			selectCalendar.add(Calendar.DATE, offsetDays);
			//System.out.println("######" + selectCalendar.get(Calendar.DATE));
			selectCalendar.set(Calendar.DATE, 1); //设置为月第一天
			selectCalendar.add(Calendar.MONTH, 1);
			selectCalendar.add(Calendar.DATE, -1);
			countDays = selectCalendar.get(Calendar.DAY_OF_MONTH); //当月天数
			int offsetDayOfWeekEnd = 7 - selectCalendar
					.get(Calendar.DAY_OF_WEEK);
			selectCalendar.add(Calendar.DAY_OF_WEEK, offsetDayOfWeekEnd); //设置为月末日那周的最后一天
			break;
		}
		endYear = Util.add0(selectCalendar.get(Calendar.YEAR), 4); //年 
		endMonth = Util.add0(selectCalendar.get(Calendar.MONTH) + 1, 2); // 月
		endDay = Util.add0(selectCalendar.get(Calendar.DAY_OF_MONTH), 2); //日
		endDate = endYear + "-" + endMonth + "-" + endDay;

		String overColor = "";
		String archiveColor = "";
		String archiveAvailable = "0";
		String overAvailable = "0";
		String oversql = "select * from overworkplan order by workplanname desc";
		recordSet.executeSql(oversql);
		while (recordSet.next()) {
			String id = recordSet.getString("id");
			String workplanname = recordSet.getString("workplanname");
			String workplancolor = recordSet.getString("workplancolor");
			String wavailable = recordSet.getString("wavailable");
			if ("1".equals(id)) {
				overColor = workplancolor;
				if ("1".equals(wavailable))
					overAvailable = "1";
			} else {
				archiveColor = workplancolor;
				if ("1".equals(wavailable))
					archiveAvailable = "2";
			}
		}
		if ("".equals(overColor)) {
			overColor = "#c3c3c2";
		}
		if ("".equals(archiveColor)) {
			archiveColor = "#937a47";
		}
		//String temptable = WorkPlanShareBase.getTempTable(userId);
		StringBuffer sqlStringBuffer = new StringBuffer();

		sqlStringBuffer.append("SELECT C.*,overworkplan.workplancolor FROM (SELECT * FROM ");
		sqlStringBuffer.append("(");
		sqlStringBuffer.append("SELECT workPlan.*, workPlanType.workPlanTypeColor,(select lastname from hrmresource where id=workPlan.createrid) createridname ");
		sqlStringBuffer.append(" FROM WorkPlan workPlan, WorkPlanType workPlanType");
		//显示所有日程，包含已结束日程
		//sqlStringBuffer.append(" WHERE (workPlan.status = 0 or workPlan.status = 1 or workPlan.status = 2)");
		sqlStringBuffer.append(" WHERE (workPlan.status = 0 ");
		if("1".equals(overAvailable)){
			sqlStringBuffer.append(" or workPlan.status = 1 ");
		}
		if("2".equals(archiveAvailable)){
			sqlStringBuffer.append(" or workPlan.status = 2 ");
		}
		sqlStringBuffer.append(" ) ");		
		//sqlStringBuffer.append(Constants.WorkPlan_Status_Unfinished);
		/** Add By Hqf for TD9970 Start **/
		sqlStringBuffer.append(" AND workPlan.deleted <> 1");
		if(!"".equals(workPlanType)){
			sqlStringBuffer.append(" AND workPlan.type_n = '"+workPlanType+"'");		
		}
		/** Add By Hqf for TD9970 End **/
		sqlStringBuffer.append(" AND workPlan.type_n = workPlanType.workPlanTypeId");
		sqlStringBuffer.append(" AND workPlan.createrType = '" + userType+ "'");

		////////////////////////////////////////////
		if (!"1".equals(isShare)) {
			sqlStringBuffer.append(" AND (");
			if(appendselectUser&&!"".equals(belongids)){//自己
				sqlStringBuffer.append("(");
					if (recordSet.getDBType().equals("oracle")) {
						sqlStringBuffer.append(" ','||workPlan.resourceID||',' LIKE '%,"+ selectUser + ",%'");
					} else {
						sqlStringBuffer.append(" ','+workPlan.resourceID+',' LIKE '%,"+ selectUser + ",%'");
					}
			
					StringTokenizer idsst = new StringTokenizer(belongids, ",");
					while (idsst.hasMoreTokens()) {
						String id = idsst.nextToken();
						if (recordSet.getDBType().equals("oracle")) {
							sqlStringBuffer.append(" OR ','||workPlan.resourceID||',' LIKE '%,"+ id + ",%'");
						} else {
							sqlStringBuffer.append(" OR ','+workPlan.resourceID+',' LIKE '%,"+ id + ",%'");
						}
					}
					sqlStringBuffer.append(")");
				
			}else{
				if (recordSet.getDBType().equals("oracle")) {
					sqlStringBuffer.append("  ','||workPlan.resourceID||',' LIKE '%,"+ selectUser + ",%'");
				}else{
					sqlStringBuffer.append("  ','+workPlan.resourceID+',' LIKE '%,"+ selectUser + ",%'");
				}
			}
			sqlStringBuffer.append(" )");
		} else {
			if (!appendselectUser) {
				sqlStringBuffer.append(" AND (");
				StringTokenizer namesst = new StringTokenizer(selectUserNames, ",");
				StringTokenizer idsst = new StringTokenizer(selectUser, ",");
				sqlStringBuffer.append(" workPlan.resourceID = '");
				sqlStringBuffer.append(selectUser);
				sqlStringBuffer.append("'");
				while (idsst.hasMoreTokens()) {
					String id = idsst.nextToken();
					if (recordSet.getDBType().equals("oracle")) {
						sqlStringBuffer.append(" OR ','||workPlan.resourceID||',' LIKE '%,"+ id + ",%'");
					} else {
						sqlStringBuffer.append(" OR ','+workPlan.resourceID+',' LIKE '%,"+ id + ",%'");
					}
				}
				sqlStringBuffer.append(")");
			}

		}
		String monthyear = Util.null2String(request.getParameter("monthyear"));
		monthyear = monthyear.trim();
		if(!"".equals(monthyear)){
			String bgdate = DateHelper.getFirstDayOfMonthWeek(DateHelper.stringtoDate(monthyear+"-01"));
			String eddate = DateHelper.getLastDayOfMonthWeek(DateHelper.stringtoDate(monthyear+"-01"));
			sqlStringBuffer.append(" AND (  workPlan.beginDate <='"+eddate+"' and workPlan.endDate  >= '"+bgdate+"') ");
		}else{
			sqlStringBuffer.append(" and 1!=1 ");
//			sqlStringBuffer.append(" AND (  workPlan.beginDate <= '");
//			endDate = Util.null2String(request.getParameter("enddate"));
//			sqlStringBuffer.append(endDate);
//			sqlStringBuffer.append("' and ");
//			sqlStringBuffer.append(" workPlan.endDate >= '");
//			beginDate = Util.null2String(request.getParameter("begindate"));
//			sqlStringBuffer.append(beginDate);
//			sqlStringBuffer.append("' )");
		}
		sqlStringBuffer.append(" ) A");
		sqlStringBuffer.append(" JOIN");
		sqlStringBuffer.append(" (");
		sqlStringBuffer.append(sql);
		sqlStringBuffer.append(" ) B");
		sqlStringBuffer.append(" ON A.id = B.workId) C");
		sqlStringBuffer.append(" LEFT JOIN overworkplan ON overworkplan.id=c.status ");
		sqlStringBuffer.append(" WHERE shareLevel >= 1");
		sqlStringBuffer.append(" ORDER BY beginDate asc, beginTime ASC");
		recordSet.executeSql(sqlStringBuffer.toString());
		//System.out.println(sqlStringBuffer.toString());
		Map result = new HashMap();
		List eventslist = new ArrayList();
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		SimpleDateFormat format3 = new SimpleDateFormat("HH:mm");
		
		WorkPlanSetInfo workPlanSetInfo = new WorkPlanSetInfo();
		int timeRangeStart=workPlanSetInfo.getTimeRangeStart();
		int timeRangeEnd=workPlanSetInfo.getTimeRangeEnd();
		String sTime=(timeRangeStart<10?"0"+timeRangeStart:timeRangeStart)+":00";
		String eTime=(timeRangeEnd<10?"0"+timeRangeEnd:timeRangeEnd)+":59";
		
		JSONObject totalObject = new JSONObject();
		while (recordSet.next()) {
			try {
				boolean isAllDay = false;
//				JSONObject jsonObject = new JSONObject();
//				jsonObject.put("id",recordSet.getString("id"));
//				jsonObject.put("name",recordSet.getString("name"));
				//jsonObject.put("begindate",Util.null2String(recordSet.getString("begindate")));
				String tempid = Util.null2String(recordSet.getString("id"));
				String tempname = Util.null2String(recordSet.getString("name"));
				String tempbegindate = Util.null2String(recordSet.getString("begindate"));
				String tempbegintime = Util.null2String(recordSet.getString("begintime"));
				String tempenddate = Util.null2String(recordSet.getString("enddate"));
				String tempendtime = Util.null2String(recordSet.getString("endtime"));
				String tempdescription = Util.null2String(recordSet.getString("description"));
				String tempstatus = Util.null2String(recordSet.getString("status"));
				String tempcreaterid = Util.null2String(recordSet.getString("createrid"));
				String tempcreateridname = Util.null2String(recordSet.getString("createridname"));
				String tempcreatedate = Util.null2String(recordSet.getString("createdate"));
				String tempcreatetime = Util.null2String(recordSet.getString("createtime"));
				String tempresourceid = Util.null2String(recordSet.getString("resourceid"));
				String tempresourceidname = "";
				if(!"".equals(tempresourceid)){
					if(tempresourceid.startsWith(",")){
						tempresourceid = tempresourceid.substring(1);
					}
					rs.executeSql("select lastname from hrmresource where id in("+tempresourceid+")");
					while(rs.next()){
						tempresourceidname += Util.null2String(rs.getString("lastname"))+",";
					}
					if(tempresourceidname.endsWith(",")){
						tempresourceidname = tempresourceidname.substring(0,tempresourceidname.length()-1);
					}
				}
//				jsonObject.put("resourceid",resourceid);
//				jsonObject.put("resourceidname",resourceidname);
				int daycount =0;
				//开始时间每天加1  当开始时间等于结束时间的时候结束循环
				String eddate = DateHelper.getLastDayOfMonthWeek(DateHelper.stringtoDate(monthyear+"-01"));
				while(daycount<365){
					String tmpBeginDate = DateHelper.dayMove(tempbegindate,daycount);
					daycount++;
					if(tmpBeginDate.indexOf(monthyear)==-1){
						continue;
					}
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("id",tempid);
					jsonObject.put("name",tempname);
					jsonObject.put("begindate",Util.null2String(tempbegindate));
					jsonObject.put("begintime",Util.null2String(tempbegintime));
					jsonObject.put("enddate",Util.null2String(tempenddate));
					jsonObject.put("endtime",Util.null2String(tempendtime));
					jsonObject.put("description",Util.null2String(tempdescription));
					jsonObject.put("status",Util.null2String(tempstatus));
					jsonObject.put("createrid",Util.null2String(tempcreaterid));
					jsonObject.put("createridname",Util.null2String(tempcreateridname));
					jsonObject.put("createdate",Util.null2String(tempcreatedate));
					jsonObject.put("createtime",Util.null2String(tempcreatetime));
					jsonObject.put("resourceid",tempresourceid);
					jsonObject.put("resourceidname",tempresourceidname);
					JSONArray tempJsonArray =  (JSONArray)totalObject.get(tmpBeginDate);
					if(tempJsonArray==null){
						tempJsonArray = new JSONArray();
						tempJsonArray.add(jsonObject);
						totalObject.put(tmpBeginDate, tempJsonArray);
					}else{
						tempJsonArray.add(jsonObject);
						totalObject.put(tmpBeginDate, tempJsonArray);
					}
					if(tmpBeginDate.equals(tempenddate)||tmpBeginDate.equals(eddate)){
						break;					
					}
				}
				
//				jsonObject.put("begintime",Util.null2String(recordSet.getString("begintime")));
//				jsonObject.put("enddate",Util.null2String(recordSet.getString("enddate")));
//				jsonObject.put("endtime",Util.null2String(recordSet.getString("endtime")));
//				jsonObject.put("description",Util.null2String(recordSet.getString("description")));
//				jsonObject.put("status",Util.null2String(recordSet.getString("status")));
//				jsonObject.put("createrid",Util.null2String(recordSet.getString("createrid")));
//				jsonObject.put("createridname",Util.null2String(recordSet.getString("createridname")));
//				jsonObject.put("createdate",Util.null2String(recordSet.getString("createdate")));
//				jsonObject.put("createtime",Util.null2String(recordSet.getString("createtime")));
//				String resourceid = Util.null2String(recordSet.getString("resourceid"));
//				String resourceidname = "";
//				if(!"".equals(resourceid)){
//					if(resourceid.startsWith(",")){
//						resourceid = resourceid.substring(1);
//					}
//					rs.executeSql("select lastname from hrmresource where id in("+resourceid+")");
//					while(rs.next()){
//						resourceidname += Util.null2String(rs.getString("lastname"))+",";
//					}
//					if(resourceidname.endsWith(",")){
//						resourceidname = resourceidname.substring(0,resourceidname.length()-1);
//					}
//				}
//				jsonObject.put("resourceid",resourceid);
//				jsonObject.put("resourceidname",resourceidname);
//				jsArray.add(jsonObject);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("totalSize", totalObject.size());
		jsonObject.put("datas", totalObject.toString());
		try {
			response.setContentType("application/json; charset=utf-8");  
			response.getWriter().print(jsonObject.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	}
	
	/*创建日程接口
	 * params 
	 * */
	public void convertcreateSchedule(User user){ 
		int detailid = Util.getIntValue((String)request.getParameter("detailid"));
		String title = Util.null2String((String)request.getParameter("name"));//名称
		title = filterHtml(title);
		//title = "测试创建会议";
		String clienttype = Util.null2String((String)request.getParameter("clienttype"));
		String clientlevel = Util.null2String((String)request.getParameter("clientlevel"));
		String month = Util.null2String((String)request.getParameter("month"));
		String year = Util.null2String((String)request.getParameter("year"));
		String date = Util.null2String((String)request.getParameter("fromdate"));
		String begindate = Util.null2String(request.getParameter("begindate"));//开始日期
		String begintime = Util.null2String(request.getParameter("begintime"));//开始时间
		begindate = begindate+" "+begintime;
		//begindate = "2018-08-01 08:00";
		String enddate = Util.null2String(request.getParameter("enddate"));//结束日期
		String endtime = Util.null2String(request.getParameter("endtime"));//结束时间
		enddate = enddate+" "+endtime;
		//enddate = "2018-08-01 09:00";
		String selectUser = Util.null2String(request.getParameter("selectUser"));//选中的用户
		if("".equals(selectUser)){
			selectUser = String.valueOf(user.getUID());
		}
		String isShare = Util.null2String(request.getParameter("isShare"));
		String urgentlevel  = Util.null2String(request.getParameter("urgentlevel"));//紧急程度
		//urgentlevel = "1";
		String resourceid = Util.null2String(request.getParameter("resourceid"));//接收人
		//resourceid = "3,58";
		String scheduletype = Util.null2String(request.getParameter("schduletype"));//日程类型
		//scheduletype = "0";
		
		String remindType = Util.null2String(request.getParameter("remindtype"));//提醒方式(短信...)
		//remindType = "2";
		String remindbeforestart = Util.null2String(request.getParameter("remindbeforestart"));//是否开始前提醒
		//remindbeforestart = "1";
		String startbefore1 = Util.null2String(request.getParameter("startbefore1"));//开始前1小时
		//startbefore1 = "1";
		String startbefore2 = Util.null2String(request.getParameter("startbefore2"));//开始前10分钟
		//startbefore2 = "10";
		int alarmstart = 0;//开始前提醒时间(10分钟...)
		alarmstart = Util.getIntValue(startbefore1,0)*60+Util.getIntValue(startbefore2,0);
		
		String remindbeforeend = Util.null2String(request.getParameter("remindbeforeend"));//是否结束前提醒
		//remindbeforeend = "1";
		String endbefore1 = Util.null2String(request.getParameter("endbefore1"));//结束前1小时
		//endbefore1 = "2";
		String endbefore2 = Util.null2String(request.getParameter("endbefore2"));//结束前10分钟
		//endbefore2 = "10";
		int alarmend = 0;//结束前提醒时间(10分钟...)
		alarmend = Util.getIntValue(endbefore1,0)*60+Util.getIntValue(endbefore2,0);  
		
		String description = Util.toHtml100(request.getParameter("description"));//描述
		//description="test";
		SimpleDateFormat sourceDate = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date startdateObj = null;
		Date enddateObj = null;
		try {
			startdateObj = sourceDate.parse(begindate);
			enddateObj = sourceDate.parse(enddate);
		}catch (Exception e) {}
		
		Map<String, String> scheduleMap = new HashMap<String, String>();
		scheduleMap.put("id", String.valueOf(detailid));//日程id
		scheduleMap.put("workPlanType", scheduletype);//日程类型
		scheduleMap.put("planName", title);//标题
		scheduleMap.put("urgentLevel", urgentlevel);//紧急程度(1.[一般]/2.重要/3.紧急)
		scheduleMap.put("remindType", remindType);//提醒类型(1.[不提醒]/2.短信/3.邮件)
		scheduleMap.put("appWorkPlanId", "");//手机同步中防止重复的手机端日程ID
		scheduleMap.put("remindBeforeStart", remindbeforestart);//是否开始前提醒
		scheduleMap.put("remindBeforeEnd", remindbeforeend);//是否结束前提醒
		if(alarmstart > 0) {
			scheduleMap.put("remindTimesBeforeStart", String.valueOf(alarmstart));//开始前提醒时间 min
		}
		if(alarmend > 0) {
			scheduleMap.put("remindTimesBeforeEnd", String.valueOf(alarmend));//结束前提醒时间 min
		}
		scheduleMap.put("memberIDs", resourceid);//接收人(1,2,3...)

		SimpleDateFormat targetDate = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat targetTime = new SimpleDateFormat("HH:mm");
		if(startdateObj != null){
			scheduleMap.put("beginDate", targetDate.format(startdateObj));//开始日期 yyyy-MM-dd
			scheduleMap.put("beginTime", targetTime.format(startdateObj));//开始时间 HH:mm
		} else {
			scheduleMap.put("beginDate", targetDate.format(new Date()));//开始日期 yyyy-MM-dd
			scheduleMap.put("beginTime", targetTime.format(new Date()));//开始时间 HH:mm
		}
		if(enddateObj != null){
			scheduleMap.put("endDate", targetDate.format(enddateObj));//结束日期 yyyy-MM-dd
			scheduleMap.put("endTime", targetTime.format(enddateObj));//结束时间 HH:mm
		}
		String relateddoc = Util.null2String(request.getParameter("relateddoc"));//相关文档
		String relatedwf = Util.null2String(request.getParameter("relatedwf"));//相关文档
		scheduleMap.put("description", description);//内容
		scheduleMap.put("crmIDs","");//相关客户
		scheduleMap.put("docIDs",relateddoc);//相关文档
		scheduleMap.put("projectIDs", "");//相关项目
		scheduleMap.put("taskIDs", "");//相关项目任务
		scheduleMap.put("requestIDs",relatedwf);//相关流程
		ScheduleService scheduleService = new ScheduleService();
		Map<String, Object> map = new HashMap<String, Object>();
		if(detailid>=0){
			map = scheduleService.editSchedule(scheduleMap, user);
		}else{
			map = scheduleService.createSchedule(scheduleMap, user);
		}
		JSONObject result = new JSONObject();
		result.put("detailid","");
		if(map != null && map.size()>0){
			result.put("detailid",Util.null2String(map.get("detailid")));
		}
		try {
			response.setContentType("application/json; charset=utf-8");  
			response.getWriter().print(result.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	/*删除日程接口
	 * params 
	 * */
	public void convertdelSchedule(User user){ 
		String detailid = Util.null2String(request.getParameter("detailid"));
		ScheduleService scheduleService = new ScheduleService();
		scheduleService.delSchedule(detailid, user);
	}
	
	/*结束日程接口
	 * params 
	 * */
	public void convertoverSchedule(User user){ 
		String detailid = Util.null2String(request.getParameter("detailid"));
		ScheduleService scheduleService = new ScheduleService();
		scheduleService.overSchedule(detailid, user);
	}
	
	/*获取日程明细数据
	 * params 
	 * */
	public void convertGetScheduleInfo(User user){ 
		int userid = user.getUID();
		userid=3;
		RecordSet rs = new RecordSet();
		RecordSet recordSet = new RecordSet();
		HrmResourceService hrmResourceService = new HrmResourceService();
		int scheduleid = Util.getIntValue(Util.null2String(request.getParameter("detailid")));
		Map<String,String> scheduleTypeMap = new HashMap<String,String>();
		rs.executeSql("SELECT * FROM WorkPlanType" + Constants.WorkPlan_Type_Query_By_Menu);
		while(rs.next()){
			scheduleTypeMap.put(rs.getString("workplantypeid"),Util.null2String(rs.getString("workplantypename")));
		}
		String sql = "SELECT a.*, hrmPerformanceCheckDetail.targetName FROM "
			+ " (SELECT workPlan.*, workPlanType.workPlanTypeName "
		    + " FROM WorkPlan workPlan, WorkPlanType workPlanType "
		    + " WHERE workPlan.type_n = workPlanType.workPlanTypeID "
		    + " AND workPlan.ID = " + scheduleid
		    + " ) a "
		    + " LEFT JOIN HrmPerformanceCheckDetail hrmPerformanceCheckDetail "
		    + " ON a.hrmPerformanceCheckDetailID = hrmPerformanceCheckDetail.ID";
		rs.executeSql(sql);
		JSONObject jsonResult = new JSONObject();
		boolean canEdit = false;
		boolean canFinish = false;
		boolean belongshow=MutilUserUtil.isShowBelongto(user);
		if(rs.next()) {
			String workID = Util.null2String(rs.getString("id"));
			String memberIDs = Util.null2String(rs.getString("resourceiD"));
			String status = Util.null2String(rs.getString("status"));
			String urgentLevel = Util.null2String(rs.getString("urgentLevel"));
			String remindtype = Util.null2String(rs.getString("remindtype"));
			String remindTimesBeforeStart = Util.null2String(rs.getString("remindTimesBeforeStart"));
			String remindTimesBeforeEnd = Util.null2String(rs.getString("remindTimesBeforeEnd"));
			jsonResult.put("id", workID);
			jsonResult.put("name", Util.null2String(rs.getString("name")));
			jsonResult.put("urgentLevel", urgentLevel);
			jsonResult.put("createdate", Util.null2String(rs.getString("createdate")));
			jsonResult.put("createtime", Util.null2String(rs.getString("createtime")));
			jsonResult.put("begindate", Util.null2String(rs.getString("begindate")));
			jsonResult.put("begintime", Util.null2String(rs.getString("begintime")));
			jsonResult.put("enddate", Util.null2String(rs.getString("enddate")));
			jsonResult.put("endtime", Util.null2String(rs.getString("endtime")));
			jsonResult.put("isremind", Util.null2String(rs.getString("isremind")));
			jsonResult.put("type_n", Util.null2String(rs.getString("type_n")));
			jsonResult.put("workplantypename", Util.null2String(rs.getString("workplantypename")));
			int shareLevel=WorkPlanShareUtil.getShareLevel(workID,user);
			if(shareLevel>-1){
				if(shareLevel==2){
					canEdit = true;
				}
			}
			boolean belongFinish=false;
			if(belongshow){
				String belongs=user.getBelongtoids();
				if(!"".equals(belongs)){
					String[] belongids=belongs.split(",");
					for(int i=0;i<belongids.length;i++){
						if(!"".equals(belongids[i])&&(","+memberIDs+",").indexOf(","+belongids[i]+",")>-1){//参会人员
							belongFinish=true;
							break;
						}
					}
				}
			}
			if (status.equals("0") && (canEdit || (","+memberIDs+",").indexOf(","+userid+",") != -1||belongFinish))
			{
				canFinish = true;
			}
			jsonResult.put("canEdit",canEdit);
			jsonResult.put("canFinish",canFinish);
			if (urgentLevel.equals("2")) {
				jsonResult.put("urgentLevelName", SystemEnv.getHtmlLabelName(15533,user.getLanguage()));
			}else if(urgentLevel.equals("3")){
				jsonResult.put("urgentLevelName", SystemEnv.getHtmlLabelName(2087,user.getLanguage()));
			}else{
				jsonResult.put("urgentLevelName", SystemEnv.getHtmlLabelName(154,user.getLanguage()));
			}
			jsonResult.put("remindType",Util.null2String(rs.getString("remindType")));
			if("1".equals(remindtype)){
				jsonResult.put("remindtypename", SystemEnv.getHtmlLabelName(19782,user.getLanguage()));
			}else if("2".equals(remindtype)){
				jsonResult.put("remindtypename", SystemEnv.getHtmlLabelName(17586,user.getLanguage()));
			}else{
				jsonResult.put("remindtypename", SystemEnv.getHtmlLabelName(18845,user.getLanguage()));
			}
			int temmhour=Util.getIntValue(remindTimesBeforeStart,0)/60;
			int temptinme=Util.getIntValue(remindTimesBeforeStart,0)%60;
			int temmhourend=Util.getIntValue(remindTimesBeforeEnd,0)/60;
			int temptinmeend=Util.getIntValue(remindTimesBeforeEnd,0)%60;
			jsonResult.put("temmhour", temmhour);
			jsonResult.put("temptinme", temptinme);
			jsonResult.put("temmhourend", temmhourend);
			jsonResult.put("temptinmeend", temptinmeend);
			jsonResult.put("creater", "");
			int createrid = Util.getIntValue(rs.getString("createrid"));
			if(createrid>0){
				User creater = hrmResourceService.getUserById(createrid);
				jsonResult.put("creater", creater!=null?creater.getLastname():"");
			}
			String resourcename = "";
			String resourceid = Util.null2String(rs.getString("resourceid"));
			if(resourceid.startsWith(",")){
				resourceid = resourceid.substring(1,resourceid.length());
			}
			jsonResult.put("resourceid", resourceid);
			if(!"".equals(resourceid)){
				recordSet.executeSql("select id,lastname from HrmResource where id in("+resourceid+")");
				while(recordSet.next()){
					resourcename+= Util.null2String(recordSet.getString("lastname"))+" ";
				}
			}
			jsonResult.put("resourcename", resourcename);
			jsonResult.put("description", Util.null2String(rs.getString("description")).replace("\n", "<br/>"));
		}
		try {
			response.setContentType("application/json; charset=utf-8");  
			response.getWriter().print(jsonResult.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	/*获取获取指定类型流程的数量(1待办、7办结、8已办、9我的请求、10抄送)
	 * params 
     * module 流程类型(必需)
     * workflowid 流程id(必须,多个以逗号隔开，如:1,2)
	 * */
	public void convertGetFlowWorkCount(User user){ 
		PluginServiceImpl pServiceImpl = new PluginServiceImpl();
		AuthService aService = new AuthService();
		String userid = String.valueOf(user.getUID());
		String module = Util.null2String(request.getParameter("module"));//1待办、7办结、8已办、9我的请求、10抄送
		String scope = Util.null2String(request.getParameter("scope"));
		String workflowid = Util.null2String(request.getParameter("workflowid"));//指定流程类id，支持多个流程，如123,456
		Map map = aService.login(userid, "7", request.getRemoteAddr());
		String sessionkey = Util.null2String(map.get("sessionkey"));
		
		JSONArray jsArray = new JSONArray();
		List<String> workflowidList = StringHelper.string2ArrayList(workflowid, ",");
		for(int i=0;i<workflowidList.size();i++){
			List conditions = new ArrayList();
			String itemWorkflowid = Util.null2String(workflowidList.get(i));
			if(StringHelper.isEmpty(itemWorkflowid)){
				continue;
			}
			if(module.equals("1")||module.equals("7")||module.equals("8")||module.equals("9")||module.equals("10")) {
				String condition = "";
				String cfgstr = itemWorkflowid;
				cfgstr = cfgstr.startsWith(",")?cfgstr.substring(1):cfgstr;
				if(StringHelper.isNotEmpty(cfgstr)) {
					String strSubClause = Util.getSubINClause(cfgstr, "t1.workflowid", "IN");
					if("".equals(condition)){
						 condition += strSubClause;
					} else {
						 condition += " or " + strSubClause;
					}
					if (condition != null && !"".equals(condition)) {
						condition = " (" + condition + ") ";
					}
					conditions.add(condition);
				}
			}
			try {
				Map resultmap = pServiceImpl.getWorkflowCount(Util.getIntValue(module),Util.getIntValue(scope),conditions,sessionkey);
				if(resultmap != null && resultmap.size() >0 ){
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("workflowid",itemWorkflowid);
					jsonObject.put("count",Util.null2String(resultmap.get("count")));
					jsonObject.put("unread",Util.null2String(resultmap.get("unread")));
					jsArray.add(jsonObject.toString());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		try {
			JSONObject result = new JSONObject();
			result.put("data", jsArray.toString());
			response.setContentType("application/json; charset=utf-8");  
			response.getWriter().print(result.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	//
	public void getTodoWorkFlowData(User user){
		
		WorkFlowTransMethod workFlowTransMethod = new WorkFlowTransMethod();
		ResourceComInfo rc = null;
		CustomerInfoComInfo cci = null;
		try {
			rc = new ResourceComInfo();
			cci = new CustomerInfoComInfo();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		int isovertime = Util.getIntValue(request.getParameter("isovertime"), 0);
		String overtimetype=Util.null2String(request.getParameter("overtimetype"));
		String flag = Util.null2String(request.getParameter("flag"));
		int pageno = Util.getIntValue(request.getParameter("pageno"),1);
		int pagesize = Util.getIntValue(request.getParameter("pagesize"),10);
		String workflowid = Util.null2String(request.getParameter("workflowid"));//指定流程类id，支持多个流程，如123,456
		String requestname = Util.null2String(request.getParameter("requestname"));//按流程标题搜索
		String createname = Util.null2String(request.getParameter("createname"));//按流程创建人
		String CurrentUser = "";
		RecordSet rs = new RecordSet();
		RecordSet rs1 = new RecordSet();
		String userID = String.valueOf(user.getUID());
		int userid=user.getUID();
		String belongtoshow = "";				
		rs.executeSql("select * from HrmUserSetting where resourceId = "+userID);
		if(rs.next()){
			belongtoshow = rs.getString("belongtoshow");
		}
		
		String userIDAll = String.valueOf(user.getUID());	
		String Belongtoids =user.getBelongtoids();
		int Belongtoid=0;
		String[] arr2 = null;
		ArrayList<String> userlist = new ArrayList();
		userlist.add(userid + "");
		if(!"".equals(Belongtoids)){
			userIDAll = userID+","+Belongtoids;
			arr2 = Belongtoids.split(",");
			for(int i=0;i<arr2.length;i++){
				Belongtoid = Util.getIntValue(arr2[i]);
				userlist.add(Belongtoid + "");
			}
		}
		String logintype = "" + user.getLogintype();
		int usertype = 0;
		boolean superior = false; //是否为被查看者上级或者本身

		if (logintype.equals("2"))
			usertype = 1;
		if (CurrentUser.equals("")) {
			CurrentUser = "" + user.getUID();
		}

		if (userID.equals(CurrentUser)) {
			superior = true;
		} else {
			rs.executeSql("SELECT * FROM HrmResource WHERE ID = " + CurrentUser + " AND managerStr LIKE '%" + userID + "%'");
			if (rs.next()) {
				superior = true;
			}
		}
		
		int viewcondition = 0;
		String whereclause = " (t1.deleted=0 or t1.deleted is null)  ";
		whereclause += " and (t2.isremark in('2','4') or (t2.isremark=0 and t2.takisremark =-2)) and (t1.deleted=0 or t1.deleted is null) and t2.islasttimes=1";
		String sqlwhere = "";
		if("1".equals(belongtoshow)){
			if (isovertime == 1) {
				if("0".equals(overtimetype)){
					sqlwhere = "where t1.requestid = t2.requestid "
							//+ " and t2.isremark='0'  AND t2.isprocessed='3' and t2.islasttimes=1 "
							+ " and t2.userid in (" + userIDAll + ") and t2.usertype='" + (Util.getIntValue(logintype, 1) - 1) + "' and t2.islasttimes = 1 "
							+ " and exists (select 1 from workflow_currentoperator c where c.requestid = t2.requestid and c.isremark = 0 and c.isreminded = '1' and (c.isreminded_csh != '1' or c.isreminded_csh is null)) "
							+ " AND exists(select 1 from SysPoppupRemindInfonew z2  "
							+ " where  t1.requestid=z2.requestid and z2.type=10  "
							+ " and z2.userid in ("
							+ userIDAll
							+ ") and z2.usertype='"
							+ (Util.getIntValue(logintype, 1) - 1)
							+ "' )";
				}else{
					sqlwhere = "where t1.requestid = t2.requestid "
							//+ " and t2.isremark = 5 AND (t2.isprocessed = 1 OR t2.isprocessed = 2)  "
							+ " and t2.userid in (" + userIDAll + ") and t2.usertype='" + (Util.getIntValue(logintype, 1) - 1) + "' and t2.islasttimes = 1 "
							+ " and exists (select 1 from workflow_currentoperator c where c.requestid = t2.requestid and c.isremark = 0 and c.isreminded_csh = '1') "
							+ " AND exists(select 1 from SysPoppupRemindInfonew z2  "
							+ " where  t1.requestid=z2.requestid and z2.type=10  "
							+ " and z2.userid in ( "
							+ userIDAll
							+ " )and z2.usertype='"
							+ (Util.getIntValue(logintype, 1) - 1)
							+ "' )";
				}
			} else {
				if (superior && !flag.equals("")){
					CurrentUser = userID;
				}
				sqlwhere = "where  (t1.deleted <> 1 or t1.deleted is null or t1.deleted='') and t1.requestid = t2.requestid and t2.userid in (" + userIDAll + " ) and t2.usertype=" + usertype;
				sqlwhere += " and " + whereclause;
			}
			if (rs.getDBType().equals("oracle")) {
				sqlwhere += " and (nvl(t1.currentstatus,-1) = -1 or (nvl(t1.currentstatus,-1)=0 and t1.creater in (" + userIDAll + "))) ";
			} else {
				sqlwhere += " and (isnull(t1.currentstatus,-1) = -1 or (isnull(t1.currentstatus,-1)=0 and t1.creater in (" + userIDAll + "))) ";
				//System.out.print("--436--sqlwhere--"+sqlwhere);
			}
		}else{
			if (isovertime == 1) {
				if("0".equals(overtimetype)){
					sqlwhere = "where t1.requestid = t2.requestid "
							//+ " and t2.isremark='0'  AND t2.isprocessed='3' and t2.islasttimes=1 "
							+ " and t2.userid in (" + user.getUID() + ") and t2.usertype='" + (Util.getIntValue(logintype, 1) - 1) + "' and t2.islasttimes = 1 "
							+ " and exists (select 1 from workflow_currentoperator c where c.requestid = t2.requestid and c.isremark = 0 and c.isreminded = '1' and (c.isreminded_csh != '1' or c.isreminded_csh is null)) "
							+ " AND exists(select 1 from SysPoppupRemindInfonew z2  "
							+ " where  t1.requestid=z2.requestid and z2.type=10  "
							+ " and z2.userid in ("
							+ user.getUID()
							+ ") and z2.usertype='"
							+ (Util.getIntValue(logintype, 1) - 1)
							+ "' )";
				}else{
					sqlwhere = "where t1.requestid = t2.requestid "
							//+ " and t2.isremark = 5 AND (t2.isprocessed = 1 OR t2.isprocessed = 2)  "
							+ " and t2.userid in (" + user.getUID() + ") and t2.usertype='" + (Util.getIntValue(logintype, 1) - 1) + "' and t2.islasttimes = 1 "
							+ " and exists (select 1 from workflow_currentoperator c where c.requestid = t2.requestid and c.isremark = 0 and c.isreminded_csh = '1') "
							+ " AND exists(select 1 from SysPoppupRemindInfonew z2  "
							+ " where  t1.requestid=z2.requestid and z2.type=10  "
							+ " and z2.userid in ( "
							+ user.getUID()
							+ " )and z2.usertype='"
							+ (Util.getIntValue(logintype, 1) - 1)
							+ "' )";
				}
			} else {
				if (superior && !flag.equals("")){
					CurrentUser = userID;
				}
				sqlwhere = "where  (t1.deleted <> 1 or t1.deleted is null or t1.deleted='') and t1.requestid = t2.requestid and t2.userid = " + CurrentUser + " and t2.usertype=" + usertype;
				sqlwhere += " and " + whereclause;
			}
			if (rs.getDBType().equals("oracle")) {
				sqlwhere += " and (nvl(t1.currentstatus,-1) = -1 or (nvl(t1.currentstatus,-1)=0 and t1.creater in (" + user.getUID() + "))) ";
			} else {
				sqlwhere += " and (isnull(t1.currentstatus,-1) = -1 or (isnull(t1.currentstatus,-1)=0 and t1.creater in (" + user.getUID() + "))) ";
			}
		}
		
		if(!"".equals(workflowid)){
			sqlwhere += " and t1.workflowid in("+workflowid+")";
		}
		String swhere = "";
		if(StringHelper.isNotEmpty(requestname)){
			swhere = "t1.requestname like '%" + requestname + "%'";
		}
		if(StringHelper.isNotEmpty(createname)) {
			if(StringHelper.isNotEmpty(swhere)){
				swhere += " or ";
			}
			swhere +=" t1.creater in (select id from hrmresource where lastname like '%"+createname+"%')";
		}
		
		if(StringHelper.isNotEmpty(swhere)) {
			sqlwhere +=" and ("+swhere+")";
		}
		
		String orderby = "";
		if (orderby.equals("")) {
			orderby = "t2.receivedate ,t2.receivetime";
		}
		String sql = "select count( distinct t1.requestid)  from workflow_requestbase t1,workflow_currentoperator t2 "+sqlwhere;
		rs.executeSql(sql);
		int totalSize = 0; 
		if(rs.next()){
			totalSize = Util.getIntValue(rs.getString(1), 0);
		}
		
		JSONArray jsonArray = new JSONArray();
		sql = "select outtemp1409477546046.* from (select row_number()over(order by tempcolumn1409477546046) temprownumber1409477546046," +
		"* from ( select  distinct  top  "+(pageno*pagesize)+" tempcolumn1409477546046=0, t1.requestid,t1.requestmark,t1.createdate, " +
		"t1.createtime,t1.creater, t1.creatertype, t1.workflowid, t1.requestname, t1.requestnamenew, " +
		"t1.status,t1.requestlevel,t1.currentnodeid,t2.viewtype,t2.userid,t2.receivedate,t2.receivetime," +
		"t2.isremark,t2.nodeid,t2.agentorbyagentid,t2.agenttype,t2.isprocessed , " +
		"(case  WHEN t2.operatedate IS NULL  THEN t2.receivedate ELSE t2.operatedate END) operatedate , " +
		"(case  WHEN t2.operatetime IS NULL  THEN t2.receivetime ELSE t2.operatetime END) operatetime," +
		"(select lastname from hrmresource where id=t1.creater) creatername,"+
		"(select nodename from workflow_nodebase where id=t1.currentnodeid) currentnodename "+
		"from workflow_requestbase t1,workflow_currentoperator t2  " +sqlwhere+
		"order by operatedate desc,operatetime desc,t1.requestid desc) innertemp1409477546046 ) outtemp1409477546046  " +
		"where temprownumber1409477546046>"+(pageno-1)*pagesize+" order by temprownumber1409477546046";
		rs.executeSql(sql);
		while(rs.next()){
			JSONObject jsonObject = new JSONObject();
			String requestid = rs.getString("requestid");
			requestname = Util.null2String(rs.getString("requestnamenew"));
			int idx = requestname.indexOf("（流程标题:");
			if(idx > -1){
				requestname = requestname.substring(idx+6,requestname.length());
				if(requestname.endsWith("）")){
					requestname = requestname.substring(0, requestname.length()-1);
				}
			}else{
				idx = requestname.indexOf("（标题:");
				if(idx > -1){
					requestname = requestname.substring(idx+4,requestname.length());
					if(requestname.endsWith("）")){
						requestname = requestname.substring(0, requestname.length()-1);
					}
				}
			}
			jsonObject.put("receivetime", Util.null2String(rs.getString("receivedate"))+" "+Util.null2String(rs.getString("receivetime")));
			jsonObject.put("currentnodeid", Util.null2String(rs.getString("currentnodeid")));
			jsonObject.put("createrid", Util.null2String(rs.getString("creater")));
			jsonObject.put("creatername", Util.null2String(rs.getString("creatername")));
			jsonObject.put("requestnamenew", Util.null2String(rs.getString("requestnamenew")));
			jsonObject.put("requestname", requestname);
			String htime = workFlowTransMethod.getCurrentUseTime(requestid);//耗时
			if(!"".equals(htime) && htime.indexOf("时") > -1){
				htime = htime.substring(0,htime.indexOf("时")+1);
			}else if(!"".equals(htime) && htime.indexOf("分") >-1 ){
				htime = htime.substring(0,htime.indexOf("分")+1);
			}else{
				htime = "1分";
			}
			jsonObject.put("htime",htime);
			String importantlevel = Util.null2String(rs.getString("requestlevel"));
			if(importantlevel.equals("0")){
				importantlevel = "正常";
			}else if(importantlevel.equals("1")){
				importantlevel = "重要";
			}else if(importantlevel.equals("2")){
				importantlevel = "紧急";
			}
			jsonObject.put("importantlevel",importantlevel);
			jsonObject.put("workflowname","");
			jsonObject.put("createtime",rs.getString("createdate")+" "+rs.getString("createtime"));
			jsonObject.put("currentnodename",Util.null2String(rs.getString("currentnodename")));
			
			String nooperation = "";//未操作者
			rs1.executeSql("select distinct userid,usertype,agenttype,agentorbyagentid,isremark from workflow_currentoperator where (isremark in ('0','1','5','7','8','9') or (isremark='4' and viewtype=0))  and requestid = " + requestid);
	        while(rs1.next()){
	        	if(rs1.getInt("usertype")==0){        		
	        		if(rs1.getInt("agenttype")==2){
	        			nooperation +=  ","+rc.getResourcename(rs1.getString("agentorbyagentid"))+"->"+rc.getResourcename(rs1.getString("userid"));
	        			//判断是否被代理者,如果是，则不显示该记录
                    }else if(rs1.getInt("agenttype")==1 && rs1.getInt("isremark") == 4){
                        continue;
	        		}else{
	        			nooperation +=  ","+rc.getResourcename(rs1.getString("userid"));
	        		}
	    		}else{
	    			//TD11591(人力资源与客户同时存在时、加','处理)
	    			nooperation +=  ","+cci.getCustomerInfoname(rs1.getString("userid"));
	    		}
	        }
	        if(nooperation.startsWith(",")){
	        	nooperation = nooperation.substring(1,nooperation.length());
	        }
			jsonObject.put("nooperation",nooperation);
			jsonObject.put("requestid",requestid);
			jsonArray.add(jsonObject);
		}
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("totalSize", totalSize);
		jsonObject.put("datas", jsonArray.toString());
		try {
			response.setContentType("application/json; charset=utf-8");  
			response.getWriter().print(jsonObject.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	} 
	
	/*获取今天的日程 当前时间到24点
	 * params 
	 * */
	public void getScheduleToday(User user){ 
		int userid = user.getUID();
		String startdate = DateHelper.getCurrentDate();
		String staramStr = DateHelper.getCurrentTime();
		String enddate = DateHelper.dayMove( DateHelper.getCurrentDate(),1);
		String endamStr = "00:00";
		RecordSet rs = new RecordSet();
		String sql = ""
			+ " SELECT id,begindate,begintime,enddate,endtime "
		    + " FROM WorkPlan workPlan, WorkPlanType workPlanType "
		    + " WHERE workPlan.type_n = workPlanType.workPlanTypeID "
		    + " and begindate<='"+startdate+"' and enddate>='"+startdate +"'"
		    + " and ','+resourceid+',' like '%,"+userid+",%'";
		rs.executeSql(sql);
		int count = 0;
		while(rs.next()){
			String begindatetmp = Util.null2String(rs.getString("begindate"));
			String begintimetmpStr = Util.null2String(rs.getString("begintime"));
			String enddatetmp = Util.null2String(rs.getString("enddate"));
			String endtimetmpStr = Util.null2String(rs.getString("endtime"));
			
			//传入的开始时间
			String str1 = startdate+" "+staramStr;
			//传入的结束时间
			String str3 = enddate+" "+endamStr;
			//数据库中的开始时间
			String str4 = begindatetmp+" "+begintimetmpStr;
			//数据库中的结束时间
			String str2 = enddatetmp+" "+endtimetmpStr;
			
			if((str1.compareTo(str2) <= 0 && str3.compareTo(str4) >= 0)) {
				count++;
			}
		}
		try {
			JSONObject jsonResult = new JSONObject();
			jsonResult.put("count", count);
			response.setContentType("application/json; charset=utf-8");  
			response.getWriter().print(jsonResult.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	public static String filterHtml(String str) {   
        Pattern pattern = Pattern.compile(regxpForHtml);   
        Matcher matcher = pattern.matcher(str);   
        StringBuffer sb = new StringBuffer();   
        boolean result1 = matcher.find();   
        while (result1) {   
            matcher.appendReplacement(sb, "");   
            result1 = matcher.find();   
        }   
        matcher.appendTail(sb);   
        return sb.toString();   
    }  
	
}
