package weaver.mobile.webservices.workflow;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import weaver.car.CarInfoComInfo;
import weaver.common.StringUtil;
import weaver.conn.RecordSet;
import weaver.cpt.capital.CapitalComInfo;
import weaver.crm.Maint.CustomerInfoComInfo;
import weaver.docs.category.SecCategoryComInfo;
import weaver.docs.docs.DocComInfo;
import weaver.docs.docs.DocImageManager;
import weaver.docs.senddoc.DocReceiveUnitComInfo;
import weaver.fna.maintenance.BudgetfeeTypeComInfo;
import weaver.fna.maintenance.CurrencyComInfo;
import weaver.formmode.tree.CustomTreeUtil;
import weaver.general.DateUtil;
import weaver.general.StaticObj;
import weaver.general.Util;
import weaver.hrm.User;
import weaver.hrm.company.DepartmentComInfo;
import weaver.hrm.company.SubCompanyComInfo;
import weaver.hrm.resource.ResourceComInfo;
import weaver.interfaces.workflow.browser.Browser;
import weaver.interfaces.workflow.browser.BrowserBean;
import weaver.meeting.MeetingBrowser;
import weaver.meeting.Maint.MeetingRoomComInfo;
import weaver.meeting.Maint.MeetingTypeComInfo;
import weaver.mobile.HtmlToPlainText;
import weaver.proj.Maint.ProjectInfoComInfo;
import weaver.systeminfo.SystemEnv;
import weaver.workflow.field.BrowserComInfo;
import weaver.workflow.field.FieldComInfo;
import weaver.workflow.request.RequestPreAddinoperateManager;
import weaver.workflow.request.ResourceConditionManager;
import weaver.workflow.request.WFCoadjutantManager;
import weaver.workflow.request.WFForwardManager;
import weaver.workflow.request.WFFreeFlowManager;
import weaver.workflow.request.WFLinkInfo;
import weaver.workflow.request.WorkflowJspBean;
import weaver.workflow.request.WorkflowSpeechAppend;
import weaver.workflow.workflow.WFManager;
import weaver.workflow.workflow.WFNodeDtlFieldManager;
import weaver.workflow.workflow.WFNodeFieldMainManager;
import weaver.workflow.workflow.WfLinkageInfo;
import weaver.workflow.workflow.WorkflowBillComInfo;
import weaver.workflow.workflow.WorkflowComInfo;
import weaver.workflow.workflow.WorkflowDynamicDataComInfo;
import weaver.workflow.workflow.WorkflowRequestComInfo;
import weaver.workflow.workflow.WorkflowVersion;
import weaver.hrm.schedule.domain.HrmScheduleShiftsSet;
import weaver.hrm.schedule.manager.HrmScheduleShiftsSetManager;

public class WorkflowServiceUtil {
  private static final Log log = LogFactory.getLog(WorkflowServiceUtil.class);
  
	public static final String FieldnonSupport = "<span style='color:#ACA899'>[该字段暂不支持]</span>";
	public static final String FieldOnlyShow = "<span style='color:#ACA899'>[该字段仅支持显示]</span>";
	public static boolean mobileVersion45 = false;   //临时存储当前服务器版本是否为4.5版本
	public static boolean clientMobileVersion45 = false;   //临时存储当前客户端版本是否为4.5版本
	public static boolean androidclienttype = false;   //临时存储当前客户端是否为安卓系统
	public static boolean androidclientosver = false;   //临时存储当前客户安卓系统是否大于等于4.1.1

	
	public static WorkflowMainTableInfo getWorkflowMainTableInfo4default(WorkflowRequestInfo wri, User user) throws Exception {
		return getWorkflowMainTableInfo4default(wri, user, null);
	}
	
	/**
	 * 取得流程主表信息
	 * 
	 * @param RequestInfo
	 * @return WorkflowMainTableInfo
	 * @throws Exception
	 */
	public static WorkflowMainTableInfo getWorkflowMainTableInfo4default(WorkflowRequestInfo wri, User user, Hashtable getPreAddRule_hs) throws Exception {
		log.info("Start to run 'getWorkflowMainTableInfo4default' method.");
		
		WorkflowBillComInfo workflowBillComInfo = new WorkflowBillComInfo();
		WorkflowJspBean workflowJspBean = new WorkflowJspBean();
		WFLinkInfo wfLinkInfo = new WFLinkInfo();
		FieldComInfo fieldComInfo = new FieldComInfo();
		weaver.workflow.field.SpecialFieldInfo specialFieldInfo = new weaver.workflow.field.SpecialFieldInfo();

		RecordSet rs = new RecordSet();
		String wid = wri.getWorkflowBaseInfo().getWorkflowId();
		String rid = wri.getRequestId();
		String module = wri.getModule();

		int workflowid = Util.getIntValue(wid, 0);
		int requestid = Util.getIntValue(rid, 0);
		String currentnodetype = "";
		int currentnodeid = 0;
		int billid = 0;
		int formid = 0;
		String isbill = "";
		int isremark = -1;
		int takisremark = -1;

		int nodeid = Util.getIntValue(wri.getNodeId());
		String nodetype = "";
		boolean mgrflg = false;
		final int userID = user.getUID();
		if(requestid>0) {
		    if (nodeid <= 0) {
		    	nodeid = wfLinkInfo.getCurrentNodeid(requestid, userID, Util.getIntValue(user.getLogintype(), 1)); // 节点id
		    }
			nodetype = wfLinkInfo.getNodeType(nodeid); // 节点类型 0:创建 1:审批 2:实现 3:归档
			
			rs.executeProc("workflow_Requestbase_SByID", requestid + "");
			if (rs.next()) {
				currentnodeid = Util.getIntValue(rs.getString("currentnodeid"), 0);
				if (nodeid < 1)
					nodeid = currentnodeid;
				currentnodetype = Util.null2String(rs.getString("currentnodetype"));
				if (nodetype.equals(""))
					nodetype = currentnodetype;
			}
		} else {
			rs.executeProc("workflow_CreateNode_Select",workflowid+"");
			if(rs.next()){
				nodeid = Util.getIntValue(Util.null2String(rs.getString(1)),0) ;
				nodetype = wfLinkInfo.getNodeType(nodeid);
			}
		}
		rs.executeProc("workflow_Workflowbase_SByID", workflowid + "");
		if (rs.next()) {
			formid = Util.getIntValue(rs.getString("formid"), 0);
			isbill = "" + Util.getIntValue(rs.getString("isbill"), 0);
		}
		if(requestid>0){
			if (isbill.equals("1")) {
				rs.executeProc("workflow_form_SByRequestid", requestid + "");
				if (rs.next()) {
					formid = Util.getIntValue(rs.getString("billformid"), 0);
					billid = Util.getIntValue(rs.getString("billid"));
				}
			}
		}
		
		int groupdetailid = -1;
		if(requestid>0){
			rs.executeSql("select isremark,isreminded,preisremark,id,groupdetailid,nodeid,takisremark from workflow_currentoperator where requestid=" + requestid + " and userid=" + userID + " and usertype=0" + " order by isremark,id");
			while (rs.next()) {
				isremark = Util.getIntValue(rs.getString("isremark"), -1);
				takisremark = Util.getIntValue(rs.getString("takisremark"), -1);
				int tmpnodeid = Util.getIntValue(rs.getString("nodeid"));
				groupdetailid = Util.getIntValue(rs.getString("groupdetailid"),0);
				if (isremark == 1 || isremark == 5 || isremark == 7 || isremark == 9 || (isremark == 0 && !nodetype.equals("3"))) {
					nodeid = tmpnodeid;
					nodetype = wfLinkInfo.getNodeType(nodeid);
					mgrflg = true;
					break;
				}
			}
		}
		WorkflowMainTableInfo wmti = new WorkflowMainTableInfo();
		if (isbill.equals("1"))
			wmti.setTableDBName(workflowBillComInfo.getTablename(formid + ""));
		
		workflowJspBean.setBillid(billid);
		workflowJspBean.setFormid(formid);
		workflowJspBean.setIsbill(isbill);
		workflowJspBean.setNodeid(nodeid);
		workflowJspBean.setRequestid(requestid);
		workflowJspBean.setUser(user);
		workflowJspBean.setWorkflowid(workflowid);
		workflowJspBean.getWorkflowFieldInfo();
		
		List fieldids = workflowJspBean.getFieldids(); // 字段队列
		List fieldorders = workflowJspBean.getFieldorders(); // 字段显示顺序队列 (单据文件不需要)
		List languageids = workflowJspBean.getLanguageids(); // 字段显示的语言(单据文件不需要)
		List fieldlabels = workflowJspBean.getFieldlabels(); // 单据的字段的label队列
		List fieldhtmltypes = workflowJspBean.getFieldhtmltypes(); // 单据的字段的html type队列
		List fieldtypes = workflowJspBean.getFieldtypes(); // 单据的字段的type队列
		List fieldnames = workflowJspBean.getFieldnames(); // 单据的字段的表字段名队列
		List fieldvalues = workflowJspBean.getFieldvalues(); // 字段的值

		List fieldviewtypes = workflowJspBean.getFieldviewtypes(); // 单据是否是detail表的字段1:是 0:否(如果是,将不显示)
		
		List isAutoGetLocations = workflowJspBean.getIsAutoLocate();
		
		int fieldlen = 0; // 字段类型长度
		List fieldrealtype = workflowJspBean.getFieldrealtype();
		String fielddbtype = ""; // 字段数据类型
		String textheight = "4";// xwj for @td2977 20051111
		
		// 确定字段是否显示，是否可以编辑，是否必须输入
		List isfieldids = new ArrayList();//workflowJspBean.getIsfieldids(); // 字段队列
		List isviews = new ArrayList();//workflowJspBean.getIsviews(); // 字段是否显示队列
		List isedits = new ArrayList();//workflowJspBean.getIsedits(); // 字段是否可以编辑队列
		List ismands = new ArrayList();//workflowJspBean.getIsmands(); // 字段是否必须输入队列

		int mode = 0;
		String ismode = "";
		rs.executeSql("select ismode from workflow_flownode where workflowid="+workflowid+" and nodeid="+nodeid);
		if(rs.next()) ismode=rs.getString("ismode");
		if(ismode!=null&&ismode.equals("1")){
			String nodemodeid = "";
			rs.executeSql("select id from workflow_nodemode where isprint='0' and workflowid="+workflowid+" and nodeid=" + nodeid);
			if(rs.next()&&Util.getIntValue(rs.getString("id"),0)>0){
				mode = 1;
			} else {
				rs.executeSql("select id from workflow_formmode where isprint='0' and formid="+formid+" and isbill='"+isbill+"'");
				if(rs.next()&&Util.getIntValue(rs.getString("id"),0)>0){
					mode = 2;
				}
			}
		}
		
		boolean havehtmltemplete = weaver.workflow.exceldesign.HtmlLayoutOperate.judgeHaveHtmlLayout(workflowid, nodeid, 2);
		
		if(mode>0&&!havehtmltemplete){
			//设置为图形化模板模式
            int nodeid_mode = 0;
            if(mode == 1) nodeid_mode = nodeid;
			try {
				if(isbill.equals("1")) {
					rs.executeSql("select distinct a.*, b.dsporder from workflow_modeview a, workflow_billfield b where a.fieldid = b.id and a.formid = "+formid+" and a.nodeid = "+nodeid_mode+" and a.isbill=1 order by b.dsporder");
				} else {
					rs.executeSql("select distinct a.*, b.fieldorder from workflow_modeview a, workflow_formfield b where a.fieldid = b.fieldid and a.formid="+formid+" and a.nodeid="+nodeid_mode+" and a.isbill=0 order by b.fieldorder");
				}
			} catch(Exception e){
				e.printStackTrace();
			}
		} else {
			//设置为普通模式/HTML模板模式
			try {
				if(isbill.equals("1")) {
					rs.executeSql("SELECT distinct a.*, b.dsporder from workflow_nodeform a, workflow_billfield b where a.fieldid = b.id and nodeid = "+nodeid+" order by b.dsporder");
				} else {
					rs.executeSql("SELECT * from workflow_nodeform where nodeid = "+nodeid+" order by fieldid");
				}
			} catch(Exception e){
				e.printStackTrace();
			}
		}
		while(rs.next()){
		    isfieldids.add(rs.getString("fieldid"));
		    isviews.add(rs.getString("isview"));
			isedits.add(rs.getString("isedit"));
			ismands.add(rs.getString("ismandatory"));
		}

		HashMap specialfield = specialFieldInfo.getFormSpecialField();

		WorkflowRequestTableRecord wrtr = new WorkflowRequestTableRecord();
		
		//读取流程正文
		List docfieldids = new ArrayList();
		rs.executeSql("select * from workflow_createdoc where status='1' and workflowId="+workflowid);
		while(rs.next()){
			String flowDocField = Util.null2String(rs.getString("flowDocField"));
			docfieldids.add(flowDocField);
		}
		
		List inoperatefields = null;
		List inoperatevalues = null;
		if(getPreAddRule_hs != null){
			inoperatefields = (ArrayList)getPreAddRule_hs.get("inoperatefields");
			inoperatevalues = (ArrayList)getPreAddRule_hs.get("inoperatevalues");
		}
		
		log.info("The value of the 'requestid' is:\t" + requestid);
		log.info("The value of the 'workflowid' is:\t" + workflowid);
		log.info("Following is the info of the 'user' is:\n" + ReflectionToStringBuilder.toString(user));

		int wfcurrrid = 0;
		rs.execute("select id from workflow_currentoperator where requestid='"+requestid+"' and userid ='"+userID+"' and isremark ='"+isremark+"' and nodeid='"+nodeid+"'");
		if(rs.next()){
			wfcurrrid = rs.getInt("id");
		}	
		WFForwardManager wfForwardManager = new WFForwardManager();
		wfForwardManager.init();
		wfForwardManager.setWorkflowid(workflowid);
		wfForwardManager.setNodeid(nodeid);
		wfForwardManager.setIsremark(isremark+"");
		wfForwardManager.setRequestid(requestid);
		wfForwardManager.setBeForwardid(wfcurrrid);
		wfForwardManager.getWFNodeInfo();
		boolean IsCanSubmit = wfForwardManager.getCanSubmit();
		boolean isCanModify = wfForwardManager.getCanModify();
		
		//获取协办人权限

		WFCoadjutantManager wfcoadjutantmanager = new WFCoadjutantManager();
		wfcoadjutantmanager.getCoadjutantRights(groupdetailid);
		String coadismodify = wfcoadjutantmanager.getIsmodify();
		
		if(!isCanModify && coadismodify.equals("1")) isCanModify=true;
		if(nodeid!=currentnodeid && coadismodify.equals("1") && isremark == 7) isCanModify = false;
		
		boolean editflag = true;//流程的处理人可以编辑流程的优先级和是否短信提醒

		if(isremark==2||isremark==8||isremark==9||((isremark == 1 || isremark == 7) && !isCanModify )) editflag = false;//被转发人或被抄送人不能编辑
		if (requestid > 0 && isremark == -1) {
		    editflag = false;
		}
		
		List workflowRequestTableFields = new ArrayList();
		//取得流程标题,紧急程度,是否短信提醒等表头字段信息

		List workflowHeadFields = getWorkflowHeadFields(wri,user,editflag);
		if(workflowHeadFields!=null) workflowRequestTableFields.addAll(workflowHeadFields);
		
		//--------------------------------------------------------
		// 属性联动功能(处于性能考虑，同一请求只查询一次) START
		//--------------------------------------------------------
		List selfieldsadd = null;
		List changefieldsadd = null;
		if(nodeid > 0){
			WfLinkageInfo wfLinkageInfo = new WfLinkageInfo();
			wfLinkageInfo.setFormid(formid);
			wfLinkageInfo.setIsbill(Util.getIntValue(isbill,0));
			wfLinkageInfo.setWorkflowid(workflowid);
			wfLinkageInfo.setLangurageid(user.getLanguage());
			selfieldsadd = wfLinkageInfo.getSelectField(workflowid, nodeid, 0);
			changefieldsadd = wfLinkageInfo.getChangeField(workflowid, nodeid, 0);
		}
		
		//--------------------------------------------------------
		// 属性联动功能 END
		//--------------------------------------------------------
		boolean freeNodeIsEdit =  WFFreeFlowManager.allowFormEdit(requestid, nodeid);
		
		// 得到每个字段的信息并在页面显示

		for (int i = 0; i < fieldids.size(); i++) { // 循环开始

			int tmpindex = i;
			if (isbill.equals("0"))
				tmpindex = fieldorders.indexOf("" + i); // 如果是表单, 得到表单顺序对应的 i
			//QC158853
			//屏蔽下标溢出错误
			if(tmpindex < 0){
			    continue;
			}
			
			String fieldid = (String) fieldids.get(tmpindex); // 字段id

			if (isbill.equals("1")) {
				String viewtype = (String) fieldviewtypes.get(tmpindex); // 如果是单据的从表字段,不显示

				if (viewtype.equals("1"))
					continue;
			}

			String isview = "0"; // 字段是否显示
			String isedit = "0"; // 字段是否可以编辑
			String ismand = "0"; // 字段是否必须输入

			int isfieldidindex = isfieldids.indexOf(fieldid);
			if (isfieldidindex != -1) {
				isview = (String) isviews.get(isfieldidindex); // 字段是否显示
				isedit = (String) isedits.get(isfieldidindex); // 字段是否可以编辑
				ismand = (String) ismands.get(isfieldidindex); // 字段是否必须输入
			}
			
			if (isremark == 5 || isremark == 9) {
				isedit = "0";// 抄送(需提交)不可编辑
				ismand = "0";
			}
			String fieldname = ""; // 字段数据库表中的字段名

			String fieldhtmltype = ""; // 字段的页面类型

			String fieldtype = ""; // 字段的类型

			String fieldlable = ""; // 字段显示名

			int languageid = 0;
			//是否自动获取位置
			String isAutoLocate = ""; 

			if (isbill.equals("0")) {
				languageid = Util.getIntValue((String) languageids.get(tmpindex), 0); // 需要更新

				fieldhtmltype = fieldComInfo.getFieldhtmltype(fieldid);
				fieldtype = fieldComInfo.getFieldType(fieldid);
				fieldlable = (String) fieldlabels.get(tmpindex);
				fieldname = fieldComInfo.getFieldname(fieldid);
				fielddbtype = fieldComInfo.getFielddbtype(fieldid);
			} else {
				languageid = user.getLanguage();
				fieldname = (String) fieldnames.get(tmpindex);
				fieldhtmltype = (String) fieldhtmltypes.get(tmpindex);
				fieldtype = (String) fieldtypes.get(tmpindex);
				fielddbtype = (String) fieldrealtype.get(tmpindex);
				fieldlable = SystemEnv.getHtmlLabelName(Util.getIntValue((String) fieldlabels.get(tmpindex), 0), languageid);
			}
			isAutoLocate = (String) isAutoGetLocations.get(tmpindex); 
			
			//如果字段不显示，而且字段不为 manager字段，才不作任何处理。

			if (!"1".equals(isview) && !"manager".equalsIgnoreCase(fieldname) && !(getPreAddRule_hs != null && requestid==0)) {
					 continue;
			}

			String fieldvalue = "";
			if(fieldvalues!=null&&fieldvalues.size()>0) fieldvalue = (String) fieldvalues.get(tmpindex);
			
			boolean flagToValue = false;
			//判断是否需要处理节点前附加操作
			if(getPreAddRule_hs != null){
				int fieldIndex = inoperatefields.indexOf(fieldid);
				if(fieldIndex > -1){
					flagToValue = true;
					fieldvalue = (String)inoperatevalues.get(fieldIndex);
				}
			}

			String fieldformname = "field" + fieldid;
			
			//标识是否给字段赋默认值，仅当是新建请求且没有节点前附加操作时。

			boolean flagDefault = requestid==0 && ! flagToValue;
			if(isremark==0 && !IsCanSubmit) isedit = "0";
			//当前操作人对表单无操作权限时，不可编辑表单

			if (!editflag) isedit = "0";
			//当自由流程设置当前节点的表单不可以编辑时，则全部表单字段都禁止编辑

			if( !freeNodeIsEdit ){
			    isedit = "0";
			}
			if(module != null && (module.equals("-1004") || module.equals("-1005"))){
			    isedit = "0";
			}

			
			WorkflowRequestTableField wrtf = WorkflowServiceUtil.getWorkflowRequestField(wid, requestid, nodeid, fieldid, fieldname, fieldvalue, fieldhtmltype, fieldtype, fielddbtype, fieldlable, fieldformname, tmpindex, languageid, isview, isedit, ismand, user, specialfield, docfieldids, flagDefault,0, selfieldsadd, changefieldsadd, 0, isAutoLocate,isremark,takisremark,module,freeNodeIsEdit);
			//判断字段类型是manager类型字段
			if("manager".equals(fieldname)){
				String showvalue = null;
				ResourceComInfo resourceComInfo = new ResourceComInfo();
				//非创建流程的情况
				if(requestid > 0){
					int beagenter = userID;
					String beagenterSql = "select agentorbyagentid from workflow_currentoperator where usertype=0 and isremark='0' and requestid="
							+ requestid + " and userid=" + userID + " and nodeid=" + nodeid + " order by id desc";
					//获得被代理人
					rs.executeSql(beagenterSql);
					if (rs.next()) {
						int beagenter_tem = rs.getInt(1);
						if (beagenter_tem > 0)
							beagenter = beagenter_tem;
					}
					
					if(mgrflg){
						fieldvalue = resourceComInfo.getManagerID(String.valueOf(beagenter));
						showvalue = resourceComInfo.getLastname(fieldvalue);
					}else {
						showvalue = resourceComInfo.getLastname(fieldvalue);
					}
				//创建流程时候拿当前操作者的直接上级
				} else {
					fieldvalue = resourceComInfo.getManagerID(String.valueOf(userID));
					showvalue = resourceComInfo.getLastname(fieldvalue);
				}
				
				String htmlshow = "";
		    	if("1".equals(isview)) {
		    		htmlshow = "<input type=\"hidden\" name=\"" + fieldformname + "\" id=\"" + fieldname + "\" value=\"" + fieldvalue + "\" />" 
		    					+ "<span id=\"" + fieldname + "_span\">" + showvalue + "</span>";
		    	}
				wrtf.setFieldValue(fieldvalue);
				wrtf.setFieldShowValue(showvalue);
				wrtf.setFiledHtmlShow(htmlshow);
			}
			
			workflowRequestTableFields.add(wrtf);
		}
		
		WorkflowRequestTableField[] wrtfs = new WorkflowRequestTableField[workflowRequestTableFields.size()];
		for (int p = 0; p < workflowRequestTableFields.size(); p++)
			wrtfs[p] = (WorkflowRequestTableField) workflowRequestTableFields.get(p);

		wrtr.setWorkflowRequestTableFields(wrtfs);
		wmti.setRequestRecords(new WorkflowRequestTableRecord[] { wrtr });

		log.info("End run 'getWorkflowMainTableInfo4default' method, and following is the return value:\n" + ReflectionToStringBuilder.toString(wmti));
		return wmti;
	}
	
	/**
	 * 取得流程明细表信息

	 * 
	 * @param RequestInfo
	 * @return List
	 * @throws Exception
	 */
	public static WorkflowDetailTableInfo[] getWorkflowDetailTableInfos4default(WorkflowRequestInfo wri, User user) throws Exception {
		List workflowDetailTableInfos = new ArrayList();

		WFLinkInfo wfLinkInfo = new WFLinkInfo();

		RecordSet rs = new RecordSet();
		RecordSet rs1 = new RecordSet();
		RecordSet rs3 = new RecordSet();
		RecordSet rs4 = new RecordSet();

		String wid = wri.getWorkflowBaseInfo().getWorkflowId();
		String rid = wri.getRequestId();
		
		int workflowid = Util.getIntValue(wid, 0);
		int requestid = Util.getIntValue(rid, 0);
		String currentnodetype = "";
		int currentnodeid = 0;
		int billid = 0;
		int formid = 0;
		String isbill = "";
		int isremark = -1;

		int nodeid = Util.getIntValue(wri.getNodeId());//-1;
		String nodetype = "";

		if(requestid>0) {
		    if (nodeid <= 0) {
		    	nodeid = wfLinkInfo.getCurrentNodeid(requestid, user.getUID(), Util.getIntValue(user.getLogintype(), 1)); // 节点id
		    }
			nodetype = wfLinkInfo.getNodeType(nodeid); // 节点类型 0:创建 1:审批 2:实现 3:归档
			
			rs.executeProc("workflow_Requestbase_SByID", requestid + "");
			if (rs.next()) {
				currentnodeid = Util.getIntValue(rs.getString("currentnodeid"), 0);
				if (nodeid < 1)
					nodeid = currentnodeid;
				currentnodetype = Util.null2String(rs.getString("currentnodetype"));
				if (nodetype.equals(""))
					nodetype = currentnodetype;
			}
		} else {
			rs.executeProc("workflow_CreateNode_Select",workflowid+"");
			if(rs.next()){
				nodeid = Util.getIntValue(Util.null2String(rs.getString(1)),0) ;
				nodetype = wfLinkInfo.getNodeType(nodeid);
			}
		}
		rs.executeProc("workflow_Workflowbase_SByID", workflowid + "");
		if (rs.next()) {
			formid = Util.getIntValue(rs.getString("formid"), 0);
			isbill = "" + Util.getIntValue(rs.getString("isbill"), 0);
		}
		
		if(requestid>0){
			if (isbill.equals("1")) {
				rs.executeProc("workflow_form_SByRequestid", requestid + "");
				if (rs.next()) {
					formid = Util.getIntValue(rs.getString("billformid"), 0);
					billid = Util.getIntValue(rs.getString("billid"));
				}
			}
		}
		int groupdetailid = -1;
		if(requestid>0){
			rs.executeSql("select isremark,isreminded,preisremark,id,groupdetailid,nodeid from workflow_currentoperator where requestid=" + requestid + " and userid=" + user.getUID() + " and usertype=0 order by isremark,id");
			while (rs.next()) {
				isremark = Util.getIntValue(rs.getString("isremark"), -1);
				int tmpnodeid = Util.getIntValue(rs.getString("nodeid"));
				groupdetailid = Util.getIntValue(rs.getString("groupdetailid"),0);
				if (isremark == 1 || isremark == 5 || isremark == 7 || isremark == 9 || (isremark == 0 && !nodetype.equals("3"))) {
					nodeid = tmpnodeid;
					nodetype = wfLinkInfo.getNodeType(nodeid);
					break;
				}
			}
		}
        int wfcurrrid = 0;
		rs.execute("select id from workflow_currentoperator where requestid='"+requestid+"' and userid ='"+user.getUID()+"' and isremark ='"+isremark+"' and nodeid='"+nodeid+"'");
		if(rs.next()){
			wfcurrrid = rs.getInt("id");
		}
		WFForwardManager wfForwardManager = new WFForwardManager();
		wfForwardManager.init();
		wfForwardManager.setWorkflowid(workflowid);
		wfForwardManager.setNodeid(nodeid);
		wfForwardManager.setIsremark(isremark+"");
		wfForwardManager.setRequestid(requestid);
		wfForwardManager.setBeForwardid(wfcurrrid);
		wfForwardManager.getWFNodeInfo();
		boolean IsCanSubmit = wfForwardManager.getCanSubmit();
		boolean isCanModify = wfForwardManager.getCanModify();
		
		//获取协办人权限

		WFCoadjutantManager wfcoadjutantmanager = new WFCoadjutantManager();
		wfcoadjutantmanager.getCoadjutantRights(groupdetailid);
		String coadismodify = wfcoadjutantmanager.getIsmodify();
		
		if(!isCanModify && coadismodify.equals("1")) isCanModify=true;
		if(nodeid!=currentnodeid && coadismodify.equals("1") && isremark == 7) isCanModify = false;
		
		boolean editflag = true;//流程的处理人可以编辑流程的优先级和是否短信提醒

		if(isremark==2||isremark==8||isremark==9||((isremark == 1 || isremark == 7) && !isCanModify )) editflag = false;//被转发人或被抄送人不能编辑
		if((isremark==0 && !IsCanSubmit)|| !editflag){wri.setCanDetailEdit(false);}else{wri.setCanDetailEdit(true);}


		// 有明细时才加载

		boolean hasdetailb = false;
		int count = 0;
		String sql = "";
		if (isbill.equals("0")) {
			sql = "select count(*) as count from workflow_formfield  where isdetail='1' and formid=" + formid;
		} else {
			sql = "select count(*) as count from workflow_billfield  where viewtype=1 and billid=" + formid;
		}
		rs.executeSql(sql);
		if (rs.next())
			count = rs.getInt("count");
		if (count > 0)
			hasdetailb = true;

		if (!hasdetailb)
			return null;

		List defieldids = new ArrayList();// 字段队列
		List defieldorders = new ArrayList();// 字段显示顺序队列 (单据文件不需要)
		List delanguageids = new ArrayList();// 字段显示的语言(单据文件不需要)
		List defieldlabels = new ArrayList();// 单据的字段的label队列
		List defieldhtmltypes = new ArrayList();// 单据的字段的html type队列
		List defieldtypes = new ArrayList();// 单据的字段的type队列
		List defieldnames = new ArrayList();// 单据的字段的表字段名队列
		List defieldviewtypes = new ArrayList();// 单据是否是detail表的字段1:是 0:否(如果是,将不显示)

		List isdefieldids = new ArrayList();// 字段队列
		List isdeviews = new ArrayList();// 字段是否显示队列
		List isdeedits = new ArrayList(); // 字段是否可以编辑队列
		List isdemands = new ArrayList(); // 字段是否必须输入队列
		List colCalAry = new ArrayList();
		boolean defshowsum = false;
		List childfieldids = new ArrayList(); // 子字段id队列
		List fieldrealtype = new ArrayList();

		int fieldlen = 0; // 字段类型长度
		String fielddbtype = ""; // 字段数据类型
		
		String defieldid = "";
		String defieldname = "";// 字段数据库表中的字段名

		String defieldhtmltype = "";// 字段的页面类型

		String defieldtype = "";// 字段的类型

		String defieldlable = "";// 字段显示名

		String defieldvalue = "";

		int delanguageid = 0;
		int colcount1 = 0;
		int colwidth1 = 0;
		String rowCalItemStr1, colCalItemStr1, mainCalStr1;
		rowCalItemStr1 = new String("");
		colCalItemStr1 = new String("");
		mainCalStr1 = new String("");
		int detailno = 0;
		int detailsum = 0;
		int derecorderindex = 0;
		
		//显示属性明细联动

		List selfieldsadd = null;
		List changefieldsadd = null;
		if(nodeid > 0){
			WfLinkageInfo wfLinkageInfo = new WfLinkageInfo();
			wfLinkageInfo.setFormid(formid);
			wfLinkageInfo.setIsbill(Util.getIntValue(isbill,0));
			wfLinkageInfo.setWorkflowid(workflowid);
			wfLinkageInfo.setLangurageid(user.getLanguage());
			selfieldsadd = wfLinkageInfo.getSelectField(workflowid, nodeid, 1);
			changefieldsadd = wfLinkageInfo.getChangeField(workflowid, nodeid, 1);
		}
		
		// 对不同的模块来说,可以定义自己相关的内容，作为请求默认值，比如将 docid 赋值，作为该请求的默认文档
		// 默认的值可以赋多个，中间用逗号格开
        int detailGroupId = 0;
		if (isbill.equals("1")) {// 单据(billid>0,无明细或单明细)/新表单(billid<0,支持多明细)
			WFNodeDtlFieldManager wfNodeDtlFieldManager = new WFNodeDtlFieldManager();
			wfNodeDtlFieldManager.resetParameter();

			//行列计算规则
			rs.executeSql("select * from workflow_formdetailinfo where formid=" + formid);
			while (rs.next()) {
				rowCalItemStr1 = Util.null2String(rs.getString("rowCalStr"));
				colCalItemStr1 = Util.null2String(rs.getString("colCalStr"));
				mainCalStr1 = Util.null2String(rs.getString("mainCalStr"));
			}
			StringTokenizer stk = new StringTokenizer(colCalItemStr1, ";");
			while (stk.hasMoreTokens()) {
				colCalAry.add(stk.nextToken());
			}

			//资产申购单 (ID 14) && 资产调拨 (ID 18)特殊处理
			//资产领用 (ID 19) && 资产报废单据 (ID 201)
			if(formid == 14 || formid == 18 || formid == 201 || formid ==19) {
				rs4.executeSql("SELECT detailtablename as tablename, null as title FROM workflow_bill WHERE id = " + formid);
			} else {
				rs4.executeSql("select tablename,title from Workflow_billdetailtable where billid=" + formid + " order by orderid" + "");
			}
			while (rs4.next()) {
				String tablename = rs4.getString("tablename");
				String tabletitle = rs4.getString("title");
				
				boolean deshowaddbutton = false;
				if (tabletitle!=null && Util.getIntValue(tabletitle)>0)
					tabletitle = SystemEnv.getHtmlLabelName(Util.getIntValue(tabletitle), user.getLanguage());
				defieldids.clear();
				defieldlabels.clear();
				defieldhtmltypes.clear();
				defieldtypes.clear();
				defieldnames.clear();
				defieldviewtypes.clear();
				fieldrealtype.clear();
				childfieldids.clear();
				defshowsum = false;
				colcount1 = 0;

				//资产申购单 (ID 14) && 资产调拨 (ID 18)特殊处理
				//资产领用 (ID 19) && 资产报废单据 (ID 201)
				if(formid == 14 || formid == 18 || formid == 201 || formid ==19) {
					rs1.executeSql("select * from workflow_billfield where viewtype='1' and billid=" + formid + " ORDER BY dsporder");
				} else {
					rs1.executeSql("select * from workflow_billfield where viewtype='1' and billid=" + formid + " and detailtable='" + tablename + "' ORDER BY dsporder");
				}
				while (rs1.next()) {
					defieldids.add(Util.null2String(rs1.getString("id")));
					defieldlabels.add(SystemEnv.getHtmlLabelName(Util.getIntValue(rs1.getString("fieldlabel")), user.getLanguage()));
					defieldhtmltypes.add(Util.null2String(rs1.getString("fieldhtmltype")));
					defieldtypes.add(Util.null2String(rs1.getString("type")));
					defieldnames.add(Util.null2String(rs1.getString("fieldname")));
					fieldrealtype.add(Util.null2String(rs1.getString("fielddbtype")));
					childfieldids.add("" + Util.getIntValue(rs1.getString("childfieldid"), 0));
				}

				// 确定字段是否显示，是否可以编辑，是否必须输入
				isdefieldids.clear(); // 字段队列
				isdeviews.clear(); // 字段是否显示队列
				isdeedits.clear(); // 字段是否可以编辑队列
				isdemands.clear(); // 字段是否必须输入队列

				int mode = 0;
        		String ismode = "";
        		rs.executeSql("select ismode from workflow_flownode where workflowid="+workflowid+" and nodeid="+nodeid);
        		if(rs.next()) ismode=rs.getString("ismode");
        		if(ismode!=null&&ismode.equals("1")){//模板模式
        			String nodemodeid = "";
        			rs.executeSql("select id from workflow_nodemode where isprint='0' and workflowid="+workflowid + " and nodeid=" + nodeid);
        			if(rs.next()&&Util.getIntValue(rs.getString("id"),0)>0){
        				mode = 1;
        			} else {
        				rs.executeSql("select id from workflow_formmode where isprint='0' and formid="+formid+" and isbill='"+isbill+"'");
        				if(rs.next()&&Util.getIntValue(rs.getString("id"),0)>0){
        					mode = 2;
        				}
        			}
        		}
        		boolean havehtmltemplete = weaver.workflow.exceldesign.HtmlLayoutOperate.judgeHaveHtmlLayout(workflowid, nodeid, 2);
        		
        		if(mode>0&&!havehtmltemplete){
        			//设置为图形化模板模式
                    int nodeid_mode = 0;
                    if(mode == 1) nodeid_mode = nodeid;
                    rs.executeSql("select distinct a.*, b.dsporder from workflow_modeview a, workflow_billfield b where a.fieldid = b.id and a.formid = "+formid+" and a.nodeid = "+nodeid_mode+" and a.isbill=1 order by b.dsporder");
        		} else {
        			//设置为普通模式/HTML模板模式
        			rs.executeSql("SELECT distinct a.*, b.dsporder from workflow_nodeform a, workflow_billfield b where a.fieldid = b.id and nodeid = "+nodeid+" order by b.dsporder");
        		}
        		
				// RecordSet.executeProc("workflow_FieldForm_Select",nodeid+"");
				//rs.execute("SELECT DISTINCT a.*, b.dsporder FROM workflow_nodeform a ,workflow_billfield b where a.fieldid = b.id and b.viewtype='1' and b.billid =" + formid + " and a.nodeid=" + nodeid + " and b.detailtable='" + tablename + "' ORDER BY b.dsporder");
				// System.out.println("SELECT DISTINCT a.*, b.dsporder FROM workflow_nodeform a ,workflow_billfield b where a.fieldid = b.id and b.viewtype='1' and b.billid ="+formid+" and a.nodeid="+nodeid+" and b.detailtable='"+tablename+"' ORDER BY b.dsporder");

				while (rs.next()) {
					String thedefieldid = Util.null2String(rs.getString("fieldid"));
					// System.out.println("thedefieldid:"+thedefieldid);
					int thefieldidindex = defieldids.indexOf(thedefieldid);
					if (thefieldidindex == -1) {
						continue;
					}
					String theisdeview = Util.null2String(rs.getString("isview"));
					if (theisdeview.equals("1")) {
						colcount1++;
						if (!defshowsum) {
							if (colCalAry.indexOf("detailfield_" + thedefieldid) > -1) {
								defshowsum = true;
							}
						}
					}
					String theisedit = Util.null2String(rs.getString("isedit"));
					if (theisedit.equals("1")) {
						if (!deshowaddbutton)
							deshowaddbutton = true;
					}
					isdefieldids.add(thedefieldid);
					isdeviews.add(theisdeview);
					isdeedits.add(theisedit);
					isdemands.add(Util.null2String(rs.getString("ismandatory")));
				}

				// 获取明细表设置

				wfNodeDtlFieldManager.setNodeid(nodeid);
				wfNodeDtlFieldManager.setGroupid(detailno);
				wfNodeDtlFieldManager.selectWfNodeDtlField();
				String dtladd = wfNodeDtlFieldManager.getIsadd();
				String dtledit = wfNodeDtlFieldManager.getIsedit();
				String dtldelete = wfNodeDtlFieldManager.getIsdelete();

				if (colcount1 > 0 || wri.getVersion() != -1) { //Html模板所有明细信息都需放入wri

					// tabletitle
					
					WorkflowDetailTableInfo wdti = new WorkflowDetailTableInfo();
					wdti.setTableDBName(tablename);
					wdti.setTableTitle(tabletitle);
					List tableFieldNames = new ArrayList();
					
					List viewfieldnames = new ArrayList();

					// 得到每个字段的信息并在页面显示

					int isfieldidindex = -1;
					for (int i = 0; i < defieldids.size(); i++) { // header循环开始

						String isdeview = "0";// 字段是否显示
						String isdeedit = "0"; // 字段是否可以编辑
						String isdemand = "0"; // 字段是否必须输入

						defieldid = (String) defieldids.get(i); // 字段id
						isfieldidindex = isdefieldids.indexOf(defieldid);
						if (isfieldidindex != -1) {
							isdeview = (String) isdeviews.get(isfieldidindex); // 字段是否显示
							isdeedit = (String) isdeedits.get(isfieldidindex); // 字段是否可以编辑
							isdemand = (String) isdemands.get(isfieldidindex); // 字段是否必须输入
						}
						defieldlable = (String) defieldlabels.get(i);
						defieldname = (String) defieldnames.get(i);
						defieldhtmltype = (String) defieldhtmltypes.get(i);

						if (!isdeview.equals("1"))
							continue; // 不显示即进行下一步循环


						viewfieldnames.add(defieldname);
						if (colcount1 != 0) {
							// defieldlable
							
							tableFieldNames.add(defieldlable);
							
							
						}

					}
					
					String[] tfns = new String[tableFieldNames.size()];
					for(int p=0;p<tableFieldNames.size();p++) tfns[p] = (String) tableFieldNames.get(p);
					wdti.setTableFieldName(tfns);
					
					 String sqlstr = "select isadd,isedit,isdelete,isdefault,defaultrows,isneed from workflow_NodeFormGroup where nodeid ="+nodeid+"  and groupid ="+detailGroupId;
					 rs.executeSql(sqlstr);
					 if(rs.next()){
						 wdti.setIsAdd(rs.getString("isadd"));
						 wdti.setIsEdit(rs.getString("isedit"));
						 wdti.setIsDelete(rs.getString("isdelete"));
						 wdti.setDefaultRow(rs.getString("isdefault"));
						 wdti.setDefaultRowCount(rs.getString("defaultrows"));
						 wdti.setNeedAddRow(rs.getString("isneed"));
					 }
				
					List workflowRequestTableRecords = new ArrayList();
					
					

					String maintable = "";
					String detailkeyfield = "";
					derecorderindex = 0;
					String submitdtlid = "";
					rs.executeSql(" select tablename,detailkeyfield from Workflow_bill where id=" + formid);
					if (rs.next()) {
						
						if(requestid>0) {
						
							maintable = rs.getString("tablename");
							detailkeyfield = rs.getString("detailkeyfield");
							if(detailkeyfield==null||"".equals(detailkeyfield.trim())) detailkeyfield = "mainid";
							
							// System.out.println("select b.* from "+maintable+" a,"+tablename+" b where a.id=b.mainid and a.requestid ="+requestid);
							if (maintable.indexOf("formtable_main_") == 0 && tablename.indexOf("formtable_main_") == 0) {// 新表单

								rs3.executeSql("select b.* from " + maintable + " a," + tablename + " b where a.id=b."+detailkeyfield+" and a.requestid =" + requestid + " order by b.id");
							} else if (formid < 0) { // 数据中心模块创建的明细报表

							    //表单建模的表单会走到这里， 会报错， 因为无inputid
								//rs3.executeSql("select b.* from " + maintable + " a," + tablename + " b where a.id=b."+detailkeyfield+" and a.requestid =" + requestid + " order by b.inputid");
							    rs3.executeSql("select b.* from " + maintable + " a," + tablename + " b where a.id=b."+detailkeyfield+" and a.requestid =" + requestid + " order by b.id");
							} else {
								String dspodr = " order by b.id";
								if (formid==156||formid==157||formid==158||formid==159) {
									dspodr = " order by b.dsporder";
								}
								rs3.executeSql("select b.* from " + maintable + " a," + tablename + " b where a.id=b."+detailkeyfield+" and a.requestid =" + requestid + dspodr);
							}
							while (rs3.next()) {
								String maincolvalue = rs3.getString(1);
								String[] colnames = rs3.getColumnName();
								if (colnames != null) {
									for (int m = 0; m < colnames.length; m++) {
										if (colnames[m].toUpperCase().equals("ID")) {
											maincolvalue = rs3.getString("id");
											break;
										} else if (colnames[m].toUpperCase().equals("INPUTID")) {
											maincolvalue = rs3.getString("inputid");
											break;
										}
									}
								}
								if (colcount1 != 0) {
									// checkbox
								}
								
								
								WorkflowRequestTableRecord wdtr = new WorkflowRequestTableRecord();
								wdtr.setRecordId(Integer.parseInt(maincolvalue));
								List workflowRequestTableFields = new ArrayList();
								
	
								for (int i = 0; i < defieldids.size(); i++) { // 明细记录循环开始

									String isdeview = "0";// 字段是否显示
									String isdeedit = "0"; // 字段是否可以编辑
									String isdemand = "0"; // 字段是否必须输入
									
									defieldid = (String) defieldids.get(i); // 字段id
									defieldname = (String) defieldnames.get(i); // 字段名

									// System.out.println("defieldname:"+defieldname);
									defieldtype = (String) defieldtypes.get(i);
	
									isfieldidindex = isdefieldids.indexOf(defieldid);
									if (isfieldidindex != -1) {
										isdeview = (String) isdeviews.get(isfieldidindex); // 字段是否显示
										isdeedit = (String) isdeedits.get(isfieldidindex); // 字段是否可以编辑
										isdemand = (String) isdemands.get(isfieldidindex); // 字段是否必须输入
									}
									defieldhtmltype = (String) defieldhtmltypes.get(i);
									defieldlable = (String) defieldlabels.get(i);
									fielddbtype = (String) fieldrealtype.get(i);
									fieldlen = 0;
									if ((fielddbtype.toLowerCase()).indexOf("varchar") > -1) {
										fieldlen = Util.getIntValue(fielddbtype.substring(fielddbtype.indexOf("(") + 1, fielddbtype.length() - 1));
	
									}
									//if (!"1".equals(dtledit)) {
										//isdeedit = "0";
										//isdemand = "0";
									//}
									//if (isremark == 5 || isremark == 9) {
									////	isdeedit = "0";// 抄送(需提交)不可编辑
									//	isdemand = "0";
									//}
									// if( ! isdeview.equals("1") ) continue; //不显示即进行下一步循环

									defieldvalue = Util.null2String(rs3.getString(defieldname));
									// System.out.println("defieldvalue:"+defieldvalue);
									if (isdemand.equals("1"))
										; // 如果必须输入,加入必须输入的检查中
									// needcheck += ",field" + defieldid + "_"+derecorderindex;
	
									String defieldformname = "field"+defieldid+"_"+derecorderindex;
									//当自由流程设置当前节点的表单不可以编辑时，则全部表单字段都禁止编辑

									if( !WFFreeFlowManager.allowFormEdit(requestid, nodeid) ){
										isdeedit = "0";
									}
									//add by liaodong 

									String module = wri.getModule();
						            if(module != null && (module.equals("-1004") || module.equals("-1005"))) isdeedit = "0";
						            
									WorkflowRequestTableField wrtf = getWorkflowRequestField(wid, requestid, nodeid,  defieldid, defieldname, defieldvalue, defieldhtmltype, defieldtype, fielddbtype, defieldlable, defieldformname, derecorderindex, delanguageid, isdeview, isdeedit, isdemand, user, new HashMap(),new ArrayList(),requestid==0,detailno, selfieldsadd, changefieldsadd, Util.getIntValue(maincolvalue, 0));
									//WorkflowRequestTableField wrtf = getWorkflowRequestField(wid, requestid, nodeid,  defieldid, defieldname, defieldvalue, defieldhtmltype, defieldtype, fielddbtype, defieldlable, defieldformname, derecorderindex, delanguageid, isdeview, isdeedit, isdemand, user, new HashMap(),new ArrayList(),requestid==0,detailno, selfieldsadd, changefieldsadd, Util.getIntValue(maincolvalue, 0));
									
									workflowRequestTableFields.add(wrtf);
								}
	
								wdtr.setRecordOrder(derecorderindex);
								
								WorkflowRequestTableField[] wrtfs = new WorkflowRequestTableField[workflowRequestTableFields.size()];
								for(int p=0;p<workflowRequestTableFields.size();p++) wrtfs[p]=(WorkflowRequestTableField)workflowRequestTableFields.get(p);
								wdtr.setWorkflowRequestTableFields(wrtfs);
								
								workflowRequestTableRecords.add(wdtr);
								
								derecorderindex++;
							}
						
						} else {
							
							WorkflowRequestTableRecord wdtr = new WorkflowRequestTableRecord();
							List workflowRequestTableFields = new ArrayList();
							
							for (int i = 0; i < defieldids.size(); i++) { // 明细记录循环开始

								String isdeview = "0";// 字段是否显示
								String isdeedit = "0"; // 字段是否可以编辑
								String isdemand = "0"; // 字段是否必须输入
								
								defieldid = (String) defieldids.get(i); // 字段id
								defieldname = (String) defieldnames.get(i); // 字段名

								// System.out.println("defieldname:"+defieldname);
								defieldtype = (String) defieldtypes.get(i);

								isfieldidindex = isdefieldids.indexOf(defieldid);
								if (isfieldidindex != -1) {
									isdeview = (String) isdeviews.get(isfieldidindex); // 字段是否显示
									isdeedit = (String) isdeedits.get(isfieldidindex); // 字段是否可以编辑
									isdemand = (String) isdemands.get(isfieldidindex); // 字段是否必须输入
								}
								defieldhtmltype = (String) defieldhtmltypes.get(i);
								defieldlable = (String) defieldlabels.get(i);
								fielddbtype = (String) fieldrealtype.get(i);
								fieldlen = 0;
								if ((fielddbtype.toLowerCase()).indexOf("varchar") > -1) {
									fieldlen = Util.getIntValue(fielddbtype.substring(fielddbtype.indexOf("(") + 1, fielddbtype.length() - 1));

								}
								//if (!"1".equals(dtledit)) {
								//isdeedit = "0";
									//isdemand = "0";
								//}
								//if (isremark == 5 || isremark == 9) {
								//	isdeedit = "0";// 抄送(需提交)不可编辑
								//	isdemand = "0";
								//}
								// if( ! isdeview.equals("1") ) continue; //不显示即进行下一步循环

								defieldvalue = "";
								// System.out.println("defieldvalue:"+defieldvalue);
								if (isdemand.equals("1"))
									; // 如果必须输入,加入必须输入的检查中
								// needcheck += ",field" + defieldid + "_"+derecorderindex;

								String defieldformname = "field"+defieldid+"_"+derecorderindex;
								
								WorkflowRequestTableField wrtf = getWorkflowRequestField(wid, requestid , nodeid, defieldid, defieldname, defieldvalue, defieldhtmltype, defieldtype, fielddbtype, defieldlable, defieldformname, derecorderindex, delanguageid, isdeview,isdeedit, isdemand, user, new HashMap(),new ArrayList(),requestid==0,detailno,selfieldsadd, changefieldsadd);
								workflowRequestTableFields.add(wrtf);
							}

							wdtr.setRecordOrder(derecorderindex);
							WorkflowRequestTableField[] wrtfs = new WorkflowRequestTableField[workflowRequestTableFields.size()];
							for(int p=0;p<workflowRequestTableFields.size();p++) wrtfs[p]=(WorkflowRequestTableField)workflowRequestTableFields.get(p);
							wdtr.setWorkflowRequestTableFields(wrtfs);
							
							workflowRequestTableRecords.add(wdtr);
							
							derecorderindex++;
							
						}
						
						WorkflowRequestTableRecord[] wrtrs = new WorkflowRequestTableRecord[workflowRequestTableRecords.size()];
						for(int p=0;p<workflowRequestTableRecords.size();p++) wrtrs[p] = (WorkflowRequestTableRecord)workflowRequestTableRecords.get(p);
						
						wdti.setWorkflowRequestTableRecords(wrtrs);

						detailno++;
					}

					workflowDetailTableInfos.add(wdti);
					
				}
				detailGroupId ++;
			}
			
		} else {// 老表单

	    	   weaver.workflow.workflow.WFNodeDtlFieldManager wfNodeDtlFieldManager = new weaver.workflow.workflow.WFNodeDtlFieldManager();
				wfNodeDtlFieldManager.resetParameter();

				// 得到计算公式的字符串
				rs.executeProc("Workflow_formdetailinfo_Sel", formid + "");
				while (rs.next()) {
					rowCalItemStr1 = Util.null2String(rs.getString("rowCalStr"));
					colCalItemStr1 = Util.null2String(rs.getString("colCalStr"));
					mainCalStr1 = Util.null2String(rs.getString("mainCalStr"));
					// System.out.println("rowCalItemStr1 = " + rowCalItemStr1);
				}
				StringTokenizer stk = new StringTokenizer(colCalItemStr1, ";");
				while (stk.hasMoreTokens()) {
					colCalAry.add(stk.nextToken());
				}
				Integer language_id = new Integer(user.getLanguage());
				int groupId = 0;
				RecordSet formrs = new RecordSet();
				formrs.execute("select distinct groupId from Workflow_formfield where formid=" + formid + " and isdetail='1' order by groupid");
				while (formrs.next()) {
					defieldids.clear();
					defieldlabels.clear();
					defieldhtmltypes.clear();
					defieldtypes.clear();
					defieldnames.clear();
					defieldviewtypes.clear();
					fieldrealtype.clear();
					childfieldids.clear();
					// 确定字段是否显示，是否可以编辑，是否必须输入
					isdefieldids.clear(); // 字段队列
					isdeviews.clear(); // 字段是否显示队列
					isdeedits.clear(); // 字段是否可以编辑队列
					isdemands.clear(); // 字段是否必须输入队列
					defshowsum = false;
					boolean deshowaddbutton = false;
					colcount1 = 0;
					groupId = formrs.getInt(1);
					
					int mode = 0;
	        		String ismode = "";
	        		rs.executeSql("select ismode from workflow_flownode where workflowid="+workflowid+" and nodeid="+nodeid);
	        		if(rs.next()) ismode = rs.getString("ismode");
	        		if(ismode!=null&&ismode.equals("1")){
	        			String nodemodeid = "";
	        			rs.executeSql("select id from workflow_nodemode where isprint='0' and workflowid="+workflowid + " and nodeid=" + nodeid); 
	        			if(rs.next()&&Util.getIntValue(rs.getString("id"),0)>0){
	        				mode = 1;
	        			} else {
	        				String formmodeid = "";
	        				rs.executeSql("select id from workflow_formmode where isprint='0' and formid="+formid+" and isbill='"+isbill+"'");
	        				if(rs.next()&&Util.getIntValue(rs.getString("id"),0)>0){
	        					mode = 2;
	        				}
	        			}
	        		}
	        		
	        		boolean havehtmltemplete = weaver.workflow.exceldesign.HtmlLayoutOperate.judgeHaveHtmlLayout(workflowid, nodeid, 2);
	        		
	        		if(mode>0&&!havehtmltemplete){
	        			//设置为图形化模板模式
	                    int nodeid_mode = 0;
	                    if(mode == 1) nodeid_mode = nodeid;
	                    rs.executeSql("SELECT formfield.fieldid,formfield.fieldorder,fieldlable.fieldlable,fieldlable.langurageid,modeview.isview,modeview.isedit,modeview.ismandatory,dictdetail.fieldname,dictdetail.fielddbtype,dictdetail.fieldhtmltype,dictdetail.type,dictdetail.childfieldid FROM Workflow_formfield formfield, workflow_modeview modeview, Workflow_fieldlable fieldlable, Workflow_formdictdetail dictdetail " +
	                    		"where formfield.formid = "+formid+" and formfield.fieldid = modeview.fieldid and formfield.fieldid = fieldlable.fieldid and formfield.formid = fieldlable.formid and formfield.fieldid = dictdetail.id and formfield.isdetail = '1' and formfield.groupId = "+groupId+" and modeview.formid = formfield.formid and modeview.nodeid = "+nodeid+" and modeview.isbill='0' Order by formfield.fieldorder");
	        		} else {
	        			//设置为普通模式/HTML模板模式
	        			rs.executeProc("Workflow_formdetailfield_Sel", "" + formid + Util.getSeparator() + nodeid + Util.getSeparator() + groupId);
	        		}
					
					while (rs.next()) {
						if (language_id.toString().equals(Util.null2String(rs.getString("langurageid")))) {
							String theisdeview = Util.null2String(rs.getString("isview"));
							String theisedit = Util.null2String(rs.getString("isedit"));
							String fieldid = Util.null2String(rs.getString("fieldid"));
							
							if (theisdeview.equals("1")) {
								colcount1++;
								if (defshowsum == false) {
									if (colCalAry.indexOf("detailfield_" + fieldid) > -1) {
										defshowsum = true;
									}
								}
							}
							
							defieldids.add(fieldid);
							defieldlabels.add(Util.null2String(rs.getString("fieldlable")));
							defieldhtmltypes.add(Util.null2String(rs.getString("fieldhtmltype")));
							defieldtypes.add(Util.null2String(rs.getString("type")));
							isdeviews.add(theisdeview);
							isdeedits.add(theisedit);
							isdemands.add(Util.null2String(rs.getString("ismandatory")));
							defieldnames.add(Util.null2String(rs.getString("fieldname")));
							fieldrealtype.add(Util.null2String(rs.getString("fielddbtype")));
							childfieldids.add("" + Util.getIntValue(rs.getString("childfieldid"), 0));
						}
					}

					// 获取明细表设置

					wfNodeDtlFieldManager.setNodeid(nodeid);
					wfNodeDtlFieldManager.setGroupid(groupId);
					wfNodeDtlFieldManager.selectWfNodeDtlField();

					if (colcount1 != 0 || wri.getVersion() != -1) { // 有明细才显示,Html模板所有明细信息都需放入wri
						WorkflowDetailTableInfo wdti = new WorkflowDetailTableInfo();
						wdti.setTableDBName(""+groupId);
						wdti.setTableTitle("");
						String sqlstr = "select isadd,isedit,isdelete,isdefault,defaultrows,isneed from workflow_NodeFormGroup where nodeid ="+nodeid+"  and groupid ="+detailGroupId;
						 rs.executeSql(sqlstr);
						 if(rs.next()){
							 wdti.setIsAdd(rs.getString("isadd"));
							 wdti.setIsEdit(rs.getString("isedit"));
							 wdti.setIsDelete(rs.getString("isdelete"));
							 wdti.setDefaultRow(rs.getString("isdefault"));
							 wdti.setDefaultRowCount(rs.getString("defaultrows"));
							 wdti.setNeedAddRow(rs.getString("isneed"));
						 }

						List tableFieldNames = new ArrayList();
						List viewfieldnames = new ArrayList();
						
						// 得到每个字段的信息并在页面显示

						for (int i = 0; i < defieldids.size(); i++) { // 循环开始

							String isdeview = "0";// 字段是否显示
							defieldid = (String) defieldids.get(i); // 字段id
							isdeview = (String) isdeviews.get(i); // 字段是否显示
							defieldlable = (String) defieldlabels.get(i);
							defieldname = (String) defieldnames.get(i);
							defieldhtmltype = (String) defieldhtmltypes.get(i);
							if (!isdeview.equals("1"))
								continue; // 不显示即进行下一步循环


							viewfieldnames.add(defieldname);
							if (colcount1 != 0) {
								tableFieldNames.add(defieldlable);
							}
						}
						
						String[] tfns = new String[tableFieldNames.size()];
						for(int p=0;p<tableFieldNames.size();p++) tfns[p] = (String) tableFieldNames.get(p);
						wdti.setTableFieldName(tfns);
						List workflowRequestTableRecords = new ArrayList();
						derecorderindex = 0;
						if(requestid>0){
							rs.executeSql(" select * from Workflow_formdetail where requestid =" + requestid + "  and groupId=" + (groupId) + " order by id");
							while (rs.next()) {
								WorkflowRequestTableRecord wdtr = new WorkflowRequestTableRecord();
								List workflowRequestTableFields = new ArrayList();
								int maincolvalue = Util.getIntValue(rs.getString("id")) ;
								for (int i = 0; i < defieldids.size(); i++) { // 明细记录循环开始

									String isdeview = "0";// 字段是否显示
									String isdeedit = "0"; //字段是否编辑
								    String isdemand = "0"; //字段是否必填 
									defieldid = (String) defieldids.get(i); // 字段id
									defieldname = (String) defieldnames.get(i); // 字段名

									defieldtype = (String) defieldtypes.get(i);
									isdeview = (String) isdeviews.get(i); // 字段是否显示
									isdeedit = (String) isdeedits.get(i); // 字段是否可以编辑
								    isdemand = (String) isdemands.get(i);//字段是否必填
									defieldlable = (String) defieldlabels.get(i);
									fieldlen = 0;
									fielddbtype = (String) fieldrealtype.get(i);
									if ((fielddbtype.toLowerCase()).indexOf("varchar") > -1) {
										fieldlen = Util.getIntValue(fielddbtype.substring(fielddbtype.indexOf("(") + 1, fielddbtype.length() - 1));
									}
									defieldhtmltype = (String) defieldhtmltypes.get(i);
									defieldvalue = Util.null2String(rs.getString(defieldname));
									String defieldformname = "field"+defieldid+"_"+derecorderindex;
									WorkflowRequestTableField wrtf = getWorkflowRequestField(wid, requestid , nodeid, defieldid, defieldname, defieldvalue, defieldhtmltype, defieldtype, fielddbtype, defieldlable, defieldformname, derecorderindex, delanguageid, isdeview, isdeedit, isdemand, user, new HashMap(),new ArrayList(),requestid==0,detailno,selfieldsadd, changefieldsadd);
									//WorkflowRequestTableField wrtf = getWorkflowRequestField(wid, requestid, nodeid,  defieldid, defieldname, defieldvalue, defieldhtmltype, defieldtype, fielddbtype, defieldlable, defieldformname, derecorderindex, delanguageid, isdeview, isdeedit, isdemand, user, new HashMap(),new ArrayList(),requestid==0,detailno, selfieldsadd, changefieldsadd, maincolvalue);
									workflowRequestTableFields.add(wrtf);
								}
								
								wdtr.setRecordOrder(derecorderindex);
								wdtr.setRecordId(maincolvalue);
								WorkflowRequestTableField[] wrtfs = new WorkflowRequestTableField[workflowRequestTableFields.size()];
								for(int p=0;p<workflowRequestTableFields.size();p++) wrtfs[p]=(WorkflowRequestTableField)workflowRequestTableFields.get(p);
								wdtr.setWorkflowRequestTableFields(wrtfs);
								workflowRequestTableRecords.add(wdtr);
								derecorderindex++;
							}
						}else{
							WorkflowRequestTableRecord wdtr = new WorkflowRequestTableRecord();
							List workflowRequestTableFields = new ArrayList();
							
							for (int i = 0; i < defieldids.size(); i++) { // 明细记录循环开始

								String isdeview = "0";// 字段是否显示
								String isdeedit = "0"; //字段是否编辑
							   String isdemand = "0"; //字段是否必填 
								defieldid = (String) defieldids.get(i); // 字段id
								defieldname = (String) defieldnames.get(i); // 字段名

								defieldtype = (String) defieldtypes.get(i);
								isdeview = (String) isdeviews.get(i); // 字段是否显示
								isdeedit = (String) isdeedits.get(i); // 字段是否可以编辑
							    isdemand = (String) isdemands.get(i);//字段是否必填
								defieldlable = (String) defieldlabels.get(i);
								fieldlen = 0;
								fielddbtype = (String) fieldrealtype.get(i);
								if ((fielddbtype.toLowerCase()).indexOf("varchar") > -1) {
									fieldlen = Util.getIntValue(fielddbtype.substring(fielddbtype.indexOf("(") + 1, fielddbtype.length() - 1));
								}
								defieldhtmltype = (String) defieldhtmltypes.get(i);
								defieldvalue = Util.null2String(rs.getString(defieldname));
								String defieldformname = "field"+defieldid+"_"+derecorderindex;
								WorkflowRequestTableField wrtf = getWorkflowRequestField(wid, requestid ,nodeid ,defieldid, defieldname, defieldvalue, defieldhtmltype, defieldtype, fielddbtype, defieldlable, defieldformname, derecorderindex, delanguageid, isdeview, isdeedit, isdemand, user, new HashMap(),new ArrayList(),requestid==0,detailno,selfieldsadd, changefieldsadd);
								//WorkflowRequestTableField wrtf = getWorkflowRequestField(wid, requestid, nodeid,  defieldid, defieldname, defieldvalue, defieldhtmltype, defieldtype, fielddbtype, defieldlable, defieldformname, derecorderindex, delanguageid, isdeview, isdeedit, isdemand, user, new HashMap(),new ArrayList(),requestid==0,detailno, selfieldsadd, changefieldsadd, 0);
								workflowRequestTableFields.add(wrtf);
							}
							
							wdtr.setRecordOrder(derecorderindex);
							wdtr.setRecordId(0);
							WorkflowRequestTableField[] wrtfs = new WorkflowRequestTableField[workflowRequestTableFields.size()];
							for(int p=0;p<workflowRequestTableFields.size();p++) wrtfs[p]=(WorkflowRequestTableField)workflowRequestTableFields.get(p);
							wdtr.setWorkflowRequestTableFields(wrtfs);
							workflowRequestTableRecords.add(wdtr);
							derecorderindex++;
						}
						WorkflowRequestTableRecord[] wrtrs = new WorkflowRequestTableRecord[workflowRequestTableRecords.size()];
						for(int p=0;p<workflowRequestTableRecords.size();p++) wrtrs[p] = (WorkflowRequestTableRecord)workflowRequestTableRecords.get(p);
						
						wdti.setWorkflowRequestTableRecords(wrtrs);
						workflowDetailTableInfos.add(wdti);
						detailno++;
					}
					detailGroupId ++;
				} 
			
		}

		WorkflowDetailTableInfo[] result = new WorkflowDetailTableInfo[workflowDetailTableInfos.size()];
		for (int i = 0; i < workflowDetailTableInfos.size(); i++)
			result[i] = (WorkflowDetailTableInfo) workflowDetailTableInfos.get(i);

		return result;
	}

     /**
	  * 创建节点获取明细合计
	  */
	public static String getSumString(String workflowid,String nodeid,User user,int tableOrderId,String columnStr){
		   boolean isHeJi =false;
		  StringBuffer sumString = new StringBuffer();
		  RecordSet rs = new RecordSet();
		  RecordSet rs1 = new RecordSet();
		  RecordSet rs2 = new RecordSet();
		  rs.executeSql("select isbill,formid from workflow_base where id ='"+workflowid+"'");
		  if(rs.next()){
			  String isbill = rs.getString("isbill");
			  String formid = rs.getString("formid");
			  if("1".equals(isbill)){ //新表单

				  rs.execute("select tablename,title from Workflow_billdetailtable where billid="+formid+" and  orderid ="+(tableOrderId)+"  order by orderid");
				  if(rs.next()){
					  String tablename=rs.getString("tablename");
			          String tabletitle=rs.getString("title"); 
			          rs1.execute("select * from workflow_billfield where viewtype='1' and billid="+formid+" and detailtable='"+tablename+"' ORDER BY dsporder");
			          int tdidx = 0;
			          while(rs1.next()){
			        	  String id=Util.null2String(rs1.getString("id"));
			        	   String sql="SELECT DISTINCT a.*, b.dsporder FROM workflow_nodeform a ,workflow_billfield b "
								    +" where a.fieldid = b.id and b.billid ="+formid+" and a.nodeid="+nodeid+"  and b.detailtable='"+tablename+"' "
								    +" and a.fieldid = "+id+" ORDER BY b.dsporder ";
							rs2.executeSql(sql);
							String isview = "";
                            if(rs2.next()){
                                  isview =  Util.null2String(rs2.getString("isview")) ;
							}
							if("1".equals(isview)){
								 String styleCls = "";
								 if(++tdidx > 4){
									 styleCls = "style=\"display:none\"";
								 }
								 sumString.append("<td  class=\"detailValueTD\" id=\"sum"+id+"\" "+styleCls+">");
						         sumString.append("<input type=\"hidden\" name=\"sumvalue"+id+"\" >");
						         sumString.append("</td>");   
							}
							if(columnStr.indexOf(id)>=0){
									 isHeJi = true;
							}
			          }
				  }
			  }else{
				    rs.execute("select distinct groupId from Workflow_formfield where formid="+formid+" and isdetail='1' and groupId="+(tableOrderId)+" order by groupid");
				    int groupId=0;
					Integer language_id = new Integer(user.getLanguage());
	                while (rs.next()){
	                	 groupId=rs.getInt(1);
	                	 rs1.executeProc("Workflow_formdetailfield_Sel",""+formid+Util.getSeparator()+nodeid+Util.getSeparator()+groupId);
	                     while (rs1.next()) {
	                    	 if(language_id.toString().equals(Util.null2String(rs1.getString("langurageid")))){
	                    		   String fieldid = rs1.getString("fieldid");
	                    		   String isview = rs1.getString("isview");
								   if("1".equals(isview)){
									    sumString.append("<td  class=\"detailValueTD\" id=\"sum"+fieldid+"\">");
	    						        sumString.append("<input type=\"hidden\" name=\"sumvalue"+fieldid+"\" >");
	    						        sumString.append("</td>");
								  }
	                    		  if(columnStr.indexOf(fieldid)>=0){
	 								 isHeJi = true;
	 						      }
	 						 }
	                     }
	                }
			  }
		  }
		  if(isHeJi){
			  return sumString.toString();
		  }else{
			  return "";
		  }
		  
	}


	 /**
	  * 审批节点数据为零的时候获取明细合计

	  */
	public static String getApproveSumString(String workflowid,String nodeid,User user,int tableOrderId,String columnStr,String[] tableFieldName){
		  StringBuffer tableFieldNameBuffer = new StringBuffer();
		  for(int i=0;i<tableFieldName.length;i++){
			     tableFieldNameBuffer.append(tableFieldName[i]);
		  }
		   boolean isHeJi =false;
		  StringBuffer sumString = new StringBuffer();
		  RecordSet rs = new RecordSet();
		  RecordSet rs1 = new RecordSet();
		  RecordSet rs2 = new RecordSet();
		  rs.executeSql("select isbill,formid from workflow_base where id ='"+workflowid+"'");
		  if(rs.next()){
			  String isbill = rs.getString("isbill");
			  String formid = rs.getString("formid");
			  if("1".equals(isbill)){ //新表单

				  rs.execute("select tablename,title from Workflow_billdetailtable where billid="+formid+" and  orderid ="+(tableOrderId)+"  order by orderid");
				  if(rs.next()){
					  String tablename=rs.getString("tablename");
			          String tabletitle=rs.getString("title"); 
			          rs1.execute("select * from workflow_billfield where viewtype='1' and billid="+formid+" and detailtable='"+tablename+"' ORDER BY dsporder");
			          while(rs1.next()){
			        	  String id=Util.null2String(rs1.getString("id"));
						    String sql1="SELECT DISTINCT a.*, b.dsporder FROM workflow_nodeform a ,workflow_billfield b "
							    +" where a.fieldid = b.id and b.billid ="+formid+" and a.nodeid="+nodeid+"  and b.detailtable='"+tablename+"' "
							    +" and a.fieldid = "+id+" ORDER BY b.dsporder ";
							rs2.executeSql(sql1);
							String isview = "";
		                     if(rs2.next()){
		                            isview =  Util.null2String(rs2.getString("isview")) ;
							}
						  String fieldlabel = Util.null2String(rs1.getString("fieldlabel"));
			        	   String sql="select labelname from HtmlLabelInfo where languageid ="+user.getLanguage()+" and indexid ="+fieldlabel;
							rs2.executeSql(sql);
                            String  labelname ="";
                            if(rs2.next()){
                                  labelname =  Util.null2String(rs2.getString("labelname")) ;
							}
							if(tableFieldNameBuffer.toString().indexOf(labelname)>=0&&"1".equals(isview)){
								 sumString.append("<td  class=\"detailValueTD\" id=\"sum"+id+"\">");
						         sumString.append("<input type=\"hidden\" name=\"sumvalue"+id+"\" >");
						         sumString.append("</td>");   
							}
							if(columnStr.indexOf(id)>=0){
									 isHeJi = true;
							}
			          }
				  }
			  }else{
				    rs.execute("select distinct groupId from Workflow_formfield where formid="+formid+" and isdetail='1' and groupId="+(tableOrderId)+" order by groupid");
				    int groupId=0;
					Integer language_id = new Integer(user.getLanguage());
	                while (rs.next()){
	                	 groupId=rs.getInt(1);
	                	 rs1.executeProc("Workflow_formdetailfield_Sel",""+formid+Util.getSeparator()+nodeid+Util.getSeparator()+groupId);
	                     while (rs1.next()) {
	                    	  if(language_id.toString().equals(Util.null2String(rs1.getString("langurageid")))){
		                    		   String fieldid = rs1.getString("fieldid");
		                    		   String fieldlabel = rs1.getString("fieldlabel");
									   if(tableFieldNameBuffer.toString().indexOf(fieldlabel)>=0&&"1".equals(rs1.getString("isview"))){
										    sumString.append("<td  class=\"detailValueTD\" id=\"sum"+fieldid+"\">");
		    						        sumString.append("<input type=\"hidden\" name=\"sumvalue"+fieldid+"\" >");
		    						        sumString.append("</td>");
									  }
		                    		  if(columnStr.indexOf(fieldid)>=0){
		 								 isHeJi = true;
		 						      }
		                	   }
	                     }
	                }
			  }
		  }
		  if(isHeJi){
			  return sumString.toString();
		  }else{
			  return "";
		  }
		  
	}


		/**
	 * 添加的时候

	 * @param workflowid
	 * @param nodeid
	 * @param user
	 * @param derecorderindex
	 * @return
	 */
	public static String  getAddJsString(String workflowid,String nodeid,User user,int derecorderindex,int groupid,int rowIndex,boolean isEdits,boolean isdisplay,int tableOrderIdInt,String newRowNum,Hashtable getPreAddRule_hs){
		StringBuffer   addString = new StringBuffer();
		RecordSet rs = new RecordSet();
		RecordSet rs1 = new RecordSet();
		RecordSet rs2 = new RecordSet();
	    String rowCalItemStr1,colCalItemStr1,mainCalStr1;
		rowCalItemStr1 = new String("");
		colCalItemStr1 = new String("");
	    mainCalStr1 = new String("");
		List inoperatefields = null;
		List inoperatevalues = null;
		if(getPreAddRule_hs != null){
			inoperatefields = (ArrayList)getPreAddRule_hs.get("inoperatefields");
			inoperatevalues = (ArrayList)getPreAddRule_hs.get("inoperatevalues");
		}
		
		
		rs.executeSql("select isbill,formid from workflow_base where id ='"+workflowid+"'");
		if(rs.next()){
			String isbill = rs.getString("isbill");
			String formid = rs.getString("formid");
			
			//显示属性明细联动

			List selfieldsadd = null;
			List changefieldsadd = null;
			if(Util.getIntValue(nodeid) > 0){
				WfLinkageInfo wfLinkageInfo = new WfLinkageInfo();
				wfLinkageInfo.setFormid(Util.getIntValue(formid));
				wfLinkageInfo.setIsbill(Util.getIntValue(isbill,0));
				wfLinkageInfo.setWorkflowid(Util.getIntValue(workflowid));
				wfLinkageInfo.setLangurageid(user.getLanguage());
				selfieldsadd = wfLinkageInfo.getSelectField(Util.getIntValue(workflowid), Util.getIntValue(nodeid), 1);
				changefieldsadd = wfLinkageInfo.getChangeField(Util.getIntValue(workflowid), Util.getIntValue(nodeid), 1);
			}
			
			int fieldCount = 0;//add by liaodong
			if("1".equals(isbill)){ //新表单

				 rs.execute("select * from workflow_formdetailinfo where formid="+formid);
				 while(rs.next()){
					rowCalItemStr1 = Util.null2String(rs.getString("rowCalStr"));
					colCalItemStr1 = Util.null2String(rs.getString("colCalStr"));
					mainCalStr1 = Util.null2String(rs.getString("mainCalStr"));
	                    //System.out.println("colCalItemStr1 = " + colCalItemStr1);
				 }
				 rs.execute("select tablename,title from Workflow_billdetailtable where billid="+formid+" and  orderid ="+(tableOrderIdInt)+"  order by orderid");
				 if(rs.next()){
					 addString.append("<tr class=\"isneed_"+groupid+"\" style=\"background: #efeff4;\" onclick=\"trMouseOver("+groupid+","+rowIndex+");\" >");
					 addString.append("<td class=\"detailCountTDValue\"><input onclick=\"event.stopPropagation();\"  type=\"checkbox\" name=\"check_node" + groupid + "\" /></td>");
					 addString.append("<td class=\"detailCountTDValue\" id=\"detailRowNum"+groupid+"_"+rowIndex+"\">"+(newRowNum)+"</td>");
					 String tablename=rs.getString("tablename");
		             String tabletitle=rs.getString("title"); 
		             rs1.execute("select * from workflow_billfield where viewtype='1' and billid="+formid+" and detailtable='"+tablename+"' ORDER BY dsporder");
		             while(rs1.next()){
		            	    String id=Util.null2String(rs1.getString("id"));
		                    String fieldlabel = SystemEnv.getHtmlLabelName(Util.getIntValue(rs1.getString("fieldlabel")),user.getLanguage());
		                    String fieldhtmltype =Util.null2String(rs1.getString("fieldhtmltype"));
		                    String type = Util.null2String(rs1.getString("type"));
		                    String fieldname = Util.null2String(rs1.getString("fieldname"));
							String fielddbtype = Util.null2String(rs1.getString("fielddbtype"));
							String childfieldid = ""+Util.getIntValue(rs1.getString("childfieldid"), 0);
							String sql="SELECT DISTINCT a.*, b.dsporder FROM workflow_nodeform a ,workflow_billfield b "
								    +" where a.fieldid = b.id and b.billid ="+formid+" and a.nodeid="+nodeid+"  and b.detailtable='"+tablename+"' "
								    +" and a.fieldid = "+id+" ORDER BY b.dsporder ";
							rs2.executeSql(sql);
							String defieldid = "";
							String isview = "";
							String isedit = "";
							String ismand = "";
							int fieldorder = 0;
							if(rs2.next()){
								  defieldid = Util.null2String(rs2.getString("fieldid")) ;
								  isview =  Util.null2String(rs2.getString("isview")) ;
								  isedit =    Util.null2String(rs2.getString("isedit"));
								  ismand = Util.null2String(rs2.getString("ismandatory"));
								  fieldorder =  rs2.getInt("dsporder");
							}
							if("1".equals(isview)){
								String fieldformname =  "field"+defieldid+"_"+derecorderindex;
				         		  try{
									 	String defieldvalue="";
				         			boolean flagToValue = false;
										//判断是否需要处理节点前附加操作
										if(getPreAddRule_hs != null){
											int fieldIndex = inoperatefields.indexOf(defieldid);
											if(fieldIndex > -1){
												flagToValue = true;
												defieldvalue = (String)inoperatevalues.get(fieldIndex);
											}
									}
									//标识是否给字段赋默认值，仅当是新建请求且没有节点前附加操作时。

									boolean flagDefault =  !flagToValue; 
									WorkflowRequestTableField  wrtf = getWorkflowRequestField(workflowid,-1,Util.getIntValue(nodeid),defieldid,fieldname,defieldvalue, fieldhtmltype, type, fielddbtype,fieldlabel,fieldformname,fieldorder,user.getLanguage(),isview,isedit,ismand,user, new HashMap(), new ArrayList(), flagDefault,groupid,selfieldsadd,changefieldsadd);
									//add by liaodong 
									String  htmlStr="";
									if(isEdits){
										htmlStr=wrtf.getFiledHtmlShow();
									}else{
										htmlStr ="<input type=\"hidden\" id="+defieldid+""+rowIndex+"  value=\"isshow"+groupid+"_"+rowIndex+"_"+fieldCount+"\"/>";
										htmlStr +="<div id=\"isshow"+groupid+"_"+rowIndex+"_"+fieldCount+"\">";
										htmlStr +=wrtf.getFieldShowValue();
										htmlStr +="</div>";
										htmlStr +="<div id=\"isedit\" style=\"display:none\">";
										htmlStr += wrtf.getFiledHtmlShow();
										htmlStr += "</div>";
										
									}
									if("".equals(htmlStr) || null == htmlStr){
										htmlStr = "<span id=\""+fieldformname+"\"></span>";
									}
									//end
									String styleCls = "";
									if(fieldCount >= 4){
										styleCls = "style=\"display:none;\"";
									}
									addString.append("<td class=\"detailValueTD\" "+styleCls+">");
									addString.append(htmlStr);
									addString.append("</td>");
									fieldCount ++;//add by liaodong 
				         		  }catch(Exception e){
				         			  
				         		  }
							}
		             }
		             addString.append("<td width=\"0%\" class=\"detailValueTD2\" style=\"text-align:center;width:0px;display:none;\"><a id=\"a"+groupid+"_"+rowIndex+"\" href=\"#\" onclick=\"dyeditPage("+groupid+","+rowIndex+","+fieldCount+","+isEdits+","+isdisplay+");return false;\"></a></td>");
		             addString.append("</tr>");
		             
					 
				  
		             addString.append("<tr width=\"100%\"  id=\"trspace"+groupid+"_"+rowIndex+"\" style=\"display:none\">");
		             addString.append("<td width=\"100%\"  colspan=\""+(fieldCount+3)+"\" id=\"tdspace"+groupid+"_"+rowIndex+"\" align=\"right\"></td>");
		             addString.append("</tr>");
					 
				 }

			}else{//老表单

				rs.executeProc("Workflow_formdetailinfo_Sel",formid+"");
				while(rs.next()){
					rowCalItemStr1 = Util.null2String(rs.getString("rowCalStr"));
					colCalItemStr1 = Util.null2String(rs.getString("colCalStr"));
					mainCalStr1 = Util.null2String(rs.getString("mainCalStr"));
				}
				addString.append("<tr class=\"isneed_"+groupid+"\" style=\"background-color:#F7F7F7;\" onclick=\"trMouseOver("+groupid+","+rowIndex+");\" >");
			    addString.append("<td class=\"detailCountTDValue\"><input onclick=\"event.stopPropagation();\"  type=\"checkbox\" name=\"check_node" + groupid + "\" /></td>");
				addString.append("<td class=\"detailCountTDValue\" id=\"detailRowNum"+groupid+"_"+rowIndex+"\">"+(newRowNum)+"</td>");
				rs.execute("select distinct groupId from Workflow_formfield where formid="+formid+" and isdetail='1' and groupId="+tableOrderIdInt+" order by groupid");
				int groupId=0;
				Integer language_id = new Integer(user.getLanguage());
                while (rs.next()){
                	 groupId=rs.getInt(1);
                	 rs2.executeProc("Workflow_formdetailfield_Sel",""+formid+Util.getSeparator()+nodeid+Util.getSeparator()+groupId);
                     while (rs2.next()) {
                    	 if(language_id.toString().equals(Util.null2String(rs2.getString("langurageid")))){
                    		  String fieldid = rs2.getString("fieldid");
                    		  String fieldlable = rs2.getString("fieldlable");
                    		  String fieldhtmltype = rs2.getString("fieldhtmltype");
                    		  String type = rs2.getString("type");
                    		  String isview = rs2.getString("isview");
                    		  String isedit = rs2.getString("isedit");
                    		  String ismand = rs2.getString("ismandatory");
                    		  String fieldname = rs2.getString("fieldname");
                    		  String fielddbtype = rs2.getString("fielddbtype");
                    		  String childfieldid = rs2.getString("childfieldid");
                    		  int  fieldorder = rs2.getInt("fieldorder");
                    		if("1".equals(isview)){  
                    		  String fieldformname =  "field"+fieldid+"_"+derecorderindex;
                    		  try{
								 String defieldvalue="";
				         			boolean flagToValue = false;
										//判断是否需要处理节点前附加操作
										if(getPreAddRule_hs != null){
											int fieldIndex = inoperatefields.indexOf(fieldid);
											if(fieldIndex > -1){
												flagToValue = true;
												defieldvalue = (String)inoperatevalues.get(fieldIndex);
											}
									}
									//标识是否给字段赋默认值，仅当是新建请求且没有节点前附加操作时。

									boolean flagDefault =  !flagToValue;
                    			 WorkflowRequestTableField  wrtf=getWorkflowRequestField(workflowid,-1,Util.getIntValue(nodeid),fieldid,fieldname,defieldvalue, fieldhtmltype, type, fielddbtype,fieldlable,fieldformname,fieldorder,user.getLanguage(),
  										isview,isedit,ismand,user, new HashMap(), new ArrayList(), flagDefault,groupId,selfieldsadd,changefieldsadd);
                    			String  htmlStr="";
							    if(isEdits){
									htmlStr=wrtf.getFiledHtmlShow();
								}else{
									htmlStr ="<input type=\"hidden\" id="+fieldid+""+rowIndex+"  value=\"isshow"+groupid+"_"+rowIndex+"_"+fieldCount+"\"/>";
									htmlStr +="<div id=\"isshow"+groupid+"_"+rowIndex+"_"+fieldCount+"\">";
									htmlStr +=wrtf.getFieldShowValue();
									htmlStr +="</div>";
									htmlStr +="<div id=\"isedit\" style=\"display:none\">";
									htmlStr += wrtf.getFiledHtmlShow();
									htmlStr += "</div>";
							    }
  								addString.append("<td class=\"detailValueTD\">");
  								addString.append(htmlStr);
  								addString.append("</td>");
								fieldCount ++;
                    		  }catch(Exception e){
                    			  
                    		  }
                    		}
    								
 						 }
                    	 
                     }
                	
                }
				addString.append("<td width=\"0%\" class=\"detailValueTD2\" style=\"text-align:center;width:0px;display:none;\"><a id=\"a"+groupid+"_"+rowIndex+"\" href=\"#\" onclick=\"dyeditPage("+groupid+","+rowIndex+","+fieldCount+","+isEdits+","+isdisplay+");return false;\"></a></td>");
		        addString.append("</tr>");

		         addString.append("<tr width=\"100%\"  id=\"trspace"+groupid+"_"+rowIndex+"\" style=\"display:none\">");
		         addString.append("<td width=\"100%\"  colspan=\""+(fieldCount+3)+"\" id=\"tdspace"+groupid+"_"+rowIndex+"\" align=\"right\"></td>");
		         addString.append("</tr>");
			}
		}
		return addString.toString();
	}
	
	/**
	 * 判断html模式下面是否为moblie的模式如果是mobile的模式只显示
	 * @param workflowid
	 *          流程编号
	 * @param nodeid
	 *         节点ID
	 * @return
	 */
	public static  boolean  isMobileMode(String workflowid,String nodeid){
	    boolean isMobile = false;
		RecordSet rs = new RecordSet();
		String sql="select ismode from workflow_flownode where workflowid="+workflowid+" and nodeid="+nodeid;
		rs.executeSql(sql);
		if(rs.next() && "2".equals(Util.null2String(rs.getString("ismode")))){
			sql = "select version from workflow_nodehtmllayout where isactive=1 and type=2 and workflowid="+workflowid+" and nodeid="+nodeid+" order by id desc";
		    rs.executeSql(sql);
		    if(rs.next() && Util.getIntValue(rs.getString("version"), 0) != 2){
		    	isMobile = true;
		    }
		}
	    return isMobile;
	}
	
	/**
	 * 获取模板version信息，主要用于区分Html新表单设计器及其他

	 * -1:非Html模式；0/1Html编辑器模板；2新表单设计器模板
	 */
	public static int getHmtlVersion(String workflowid, String nodeid){
		int version = -1;
		RecordSet rs = new RecordSet();
		String sql="select ismode from workflow_flownode where workflowid="+workflowid+" and nodeid="+nodeid;
		rs.executeSql(sql);
		if(rs.next() && "2".equals(Util.null2String(rs.getString("ismode")))){
			sql = "select version from workflow_nodehtmllayout where isactive=1 and type=2 and workflowid="+workflowid+" and nodeid="+nodeid+" order by id desc";
		    rs.executeSql(sql);
		    if(rs.next()){
		    	version = Util.getIntValue(rs.getString("version"), 0);
		    }
		}
		return version;
	}


	/**
	 *当待办中无明细的时候添加的数据
	 *
	 **/
	 public static WorkflowRequestTableRecord[]  getWorkflowReqeustTableRecordWhenNull(String workflowid,String nodeid,String detailTableName,User user){
		 WorkflowRequestTableRecord[] requestTableRecord =new WorkflowRequestTableRecord[1];
		  RecordSet rs = new RecordSet();
		  RecordSet rs1 = new RecordSet();
		  RecordSet rs2 = new RecordSet();  
          rs.executeSql("select isbill,formid from workflow_base where id ='"+workflowid+"'");
         try{
          if(rs.next()){
			     String isbill = rs.getString("isbill");
			     String formid = rs.getString("formid");
			     
		     	//显示属性明细联动

				List selfieldsadd = null;
				List changefieldsadd = null;
				if(Util.getIntValue(nodeid) > 0){
					WfLinkageInfo wfLinkageInfo = new WfLinkageInfo();
					wfLinkageInfo.setFormid(Util.getIntValue(formid));
					wfLinkageInfo.setIsbill(Util.getIntValue(isbill,0));
					wfLinkageInfo.setWorkflowid(Util.getIntValue(workflowid));
					wfLinkageInfo.setLangurageid(user.getLanguage());
					selfieldsadd = wfLinkageInfo.getSelectField(Util.getIntValue(workflowid), Util.getIntValue(nodeid), 1);
					changefieldsadd = wfLinkageInfo.getChangeField(Util.getIntValue(workflowid), Util.getIntValue(nodeid), 1);
				}
			     
				 WorkflowRequestTableRecord   wrtrMode=new WorkflowRequestTableRecord();
                 wrtrMode.setRecordOrder(0);
				if("1".equals(isbill)){ //新表单

					  rs1.execute("select * from workflow_billfield where viewtype='1' and billid="+formid+" and detailtable='"+detailTableName+"' ORDER BY dsporder"); 
                      ArrayList tableFields = new ArrayList();
                      while(rs1.next()){
					         String id=Util.null2String(rs1.getString("id"));
		                    String fieldlabel = SystemEnv.getHtmlLabelName(Util.getIntValue(rs1.getString("fieldlabel")),user.getLanguage());
		                    String fieldhtmltype =Util.null2String(rs1.getString("fieldhtmltype"));
		                    String type = Util.null2String(rs1.getString("type"));
		                    String fieldname = Util.null2String(rs1.getString("fieldname"));
							String fielddbtype = Util.null2String(rs1.getString("fielddbtype"));
							String childfieldid = ""+Util.getIntValue(rs1.getString("childfieldid"), 0);
							String sql="SELECT DISTINCT a.*, b.dsporder FROM workflow_nodeform a ,workflow_billfield b "
								    +" where a.fieldid = b.id and b.billid ="+formid+" and a.nodeid="+nodeid+"  and b.detailtable='"+detailTableName+"' "
								    +" and a.fieldid = "+id+" ORDER BY b.dsporder ";
							rs2.executeSql(sql);
							String defieldid = "";
							String isview = "";
							String isedit = "";
							String ismand = "";
							int fieldorder = 0;
							if(rs2.next()){
								  defieldid = Util.null2String(rs2.getString("fieldid")) ;
								  isview =  Util.null2String(rs2.getString("isview")) ;
								  isedit =    Util.null2String(rs2.getString("isedit"));
								  ismand = Util.null2String(rs2.getString("ismandatory"));
								  fieldorder =  rs2.getInt("dsporder");
							}
							if("1".equals(isview)){
							      String fieldformname =  "field"+defieldid+"_0";
                                  WorkflowRequestTableField  wrtf=	getWorkflowRequestField(workflowid,-1,Util.getIntValue(nodeid),defieldid,fieldname,"", fieldhtmltype, type, fielddbtype,fieldlabel,fieldformname,fieldorder,user.getLanguage(),
											isview,isedit,ismand,user, new HashMap(), new ArrayList(), true,0,selfieldsadd,changefieldsadd);
                                  tableFields.add(wrtf);
							}
					  }
                      WorkflowRequestTableField[] wrtfModels= new WorkflowRequestTableField[tableFields.size()];
                      for(int i=0;i<tableFields.size();i++){
                    	  wrtfModels[i] = (WorkflowRequestTableField) tableFields.get(i);
                      }
                      wrtrMode.setWorkflowRequestTableFields(wrtfModels);
					  requestTableRecord[0] = wrtrMode;
				}else{ //老表单暂未处理

					 ArrayList tableFields = new ArrayList();
						rs.execute("select distinct groupId from Workflow_formfield where formid="+formid+" and isdetail='1' and groupId="+detailTableName+" order by groupid");
	                    int groupId=0;
					    Integer language_id = new Integer(user.getLanguage());
						 while (rs.next()){
	                	     groupId=rs.getInt(1);
	                	     rs2.executeProc("Workflow_formdetailfield_Sel",""+formid+Util.getSeparator()+nodeid+Util.getSeparator()+groupId);
						    while (rs2.next()) {
	                    	   if(language_id.toString().equals(Util.null2String(rs2.getString("langurageid")))){
	                    		  String fieldid = rs2.getString("fieldid");
	                    		  String fieldlable = rs2.getString("fieldlable");
	                    		  String fieldhtmltype = rs2.getString("fieldhtmltype");
	                    		  String type = rs2.getString("type");
	                    		  String isview = rs2.getString("isview");
	                    		  String isedit = rs2.getString("isedit");
	                    		  String ismand = rs2.getString("ismandatory");
	                    		  String fieldname = rs2.getString("fieldname");
	                    		  String fielddbtype = rs2.getString("fielddbtype");
	                    		  String childfieldid = rs2.getString("childfieldid");
	                    		  int  fieldorder = rs2.getInt("fieldorder");
	                    		 if("1".equals(isview)){
	                    		  String fieldformname =  "field"+fieldid+"_0";
	                    		  WorkflowRequestTableField  wrtf=getWorkflowRequestField(workflowid,-1,Util.getIntValue(nodeid),fieldid,fieldname,"", fieldhtmltype, type, fielddbtype,fieldlable,fieldformname,Integer.parseInt(detailTableName),user.getLanguage(),
	  										isview,isedit,ismand,user, new HashMap(), new ArrayList(), true,0,selfieldsadd,changefieldsadd);
									tableFields.add(wrtf);

								 }
	    								
	 						   }
	                       }

						 }
					    WorkflowRequestTableField[] wrtfModels= new WorkflowRequestTableField[tableFields.size()];
	                      for(int i=0;i<tableFields.size();i++){
	                    	  wrtfModels[i] = (WorkflowRequestTableField) tableFields.get(i);
	                      }
	                      wrtrMode.setWorkflowRequestTableFields(wrtfModels);
						  requestTableRecord[0] = wrtrMode;
				}
		  }
         }catch(Exception e){
        	  e.printStackTrace();
         }
		 return requestTableRecord;   
	 }
	


	/**
	 * 取得某字段信息（废弃，请调用最新的方法）

	 * 
	 * @param property
	 * @param isbill
	 * @param formid
	 * @return WorkflowRequestField
	 * @throws Exception
	 */
	 @Deprecated 
	 public static WorkflowRequestTableField getWorkflowRequestField(String workflowid, String fieldid, String fieldname, String fieldvalue, String fieldhtmltype, String fieldtype, String fielddbtype, String fieldlable, String fieldformname, int fieldorder, int languageid,
			String isview, String isedit, String ismand, User user, HashMap specialfield, List docfieldids, boolean flagDefault) throws Exception {
		return getWorkflowRequestField(workflowid, -1, fieldid, fieldname, fieldvalue, fieldhtmltype, fieldtype, fielddbtype, fieldlable, fieldformname, fieldorder, languageid, isview, isedit, ismand, user, specialfield, docfieldids, flagDefault, -1, null, null);
	 }
	
	/**
	 * 取得某字段信息（废弃，请调用最新的方法）

	 * 
	 * @param property
	 * @param isbill
	 * @param formid
	 * @return WorkflowRequestField
	 * @throws Exception
	 */
	@Deprecated 
	public static WorkflowRequestTableField getWorkflowRequestField(String workflowid, String fieldid, String fieldname, String fieldvalue, String fieldhtmltype, String fieldtype, String fielddbtype, String fieldlable, String fieldformname, int fieldorder, int languageid,
			String isview, String isedit, String ismand, User user, HashMap specialfield, List docfieldids, boolean flagDefault,int groupid) throws Exception {
		return getWorkflowRequestField(workflowid, -1, fieldid, fieldname, fieldvalue, fieldhtmltype, fieldtype, fielddbtype, fieldlable, fieldformname, fieldorder, languageid, isview, isedit, ismand, user, specialfield, docfieldids, flagDefault, groupid, null, null);
	}
	
	/**
	 * 取得某字段信息（废弃，请调用最新的方法）

	 * 
	 * @param property
	 * @param isbill
	 * @param formid
	 * @return WorkflowRequestField
	 * @throws Exception
	 */
	@Deprecated 
	public static WorkflowRequestTableField getWorkflowRequestField(String workflowid, int nodeid, String fieldid, String fieldname, String fieldvalue, String fieldhtmltype, String fieldtype, String fielddbtype, String fieldlable, String fieldformname, int fieldorder, int languageid,
			String isview, String isedit, String ismand, User user, HashMap specialfield, List docfieldids, boolean flagDefault,int groupid, List selfieldsadd, List changefieldsadd) throws Exception {
		return getWorkflowRequestField(workflowid, -1, nodeid, fieldid, fieldname, fieldvalue, fieldhtmltype, fieldtype, fielddbtype, fieldlable, fieldformname, fieldorder, languageid, isview, isedit, ismand, user, specialfield, docfieldids, flagDefault, groupid, selfieldsadd, changefieldsadd);
	}
	
	
	public static WorkflowRequestTableField getWorkflowRequestField(String workflowid, int requestid, int nodeid, String fieldid, String fieldname, String fieldvalue, String fieldhtmltype, String fieldtype, String fielddbtype, String fieldlable, String fieldformname, int fieldorder, int languageid,
			String isview, String isedit, String ismand, User user, HashMap specialfield, List docfieldids, boolean flagDefault,int groupid, List selfieldsadd, List changefieldsadd) throws Exception {
		return getWorkflowRequestField(workflowid, requestid, nodeid, fieldid, fieldname, fieldvalue, fieldhtmltype, fieldtype, fielddbtype, fieldlable, fieldformname, fieldorder, languageid, isview, isedit, ismand, user, specialfield, docfieldids, flagDefault, groupid, selfieldsadd, changefieldsadd, 0);
	}
	
	/**
     * 取得某字段信息（新接口，包含明细字段、字段属性联动的处理）

     * @param workflowid 流程ID
     * @param requestid 请求id
     * @param nodeid 节点ID
     * @param fieldid 字段ID
     * @param fieldname 字段名称
     * @param fieldvalue 字段值

     * @param fieldhtmltype 字段HTML类型
     * @param fieldtype 字段类型
     * @param fielddbtype 字段数据库类型

     * @param fieldlable 字段Label
     * @param fieldformname 字段页面显示名称（例：field10024）

     * @param fieldorder 页面显示顺序
     * @param languageid 当前用户语言
     * @param isview 是否可显示

     * @param isedit 是否可编辑

     * @param ismand 是否必填
     * @param user 当前用户
     * @param specialfield 特殊字段信息
     * @param docfieldids 流程正文
     * @param flagDefault 是否需要默认值（创建节点）

     * @param groupid 明细表Index，主表为0
     * @param selfieldsadd 流程显示属性联动select 字段id集合
     * @param changefieldsadd 流程显示属性联动 被联动字段id集合
     * @param detailRecordId 明细表Id
     * @return WorkflowRequestTableField
     * @throws Exception
     */
    public static WorkflowRequestTableField getWorkflowRequestField(String workflowid, int requestid, int nodeid, String fieldid, String fieldname, String fieldvalue, String fieldhtmltype, String fieldtype, String fielddbtype, String fieldlable, String fieldformname, int fieldorder, int languageid,
            String isview, String isedit, String ismand, User user, HashMap specialfield, List docfieldids, boolean flagDefault,int groupid, List selfieldsadd, List changefieldsadd, int detailRecordId) throws Exception {
        return getWorkflowRequestField(workflowid, requestid, nodeid, fieldid, fieldname, fieldvalue, fieldhtmltype, fieldtype, fielddbtype, fieldlable, fieldformname, fieldorder, languageid, isview, isedit, ismand, user, specialfield, docfieldids, flagDefault, groupid, selfieldsadd, changefieldsadd, detailRecordId, "",-1,-1);
    }
    
    /**
     * 取得某字段信息（新接口，包含明细字段、字段属性联动的处理）

     * @param workflowid 流程ID
     * @param requestid 请求id
     * @param nodeid 节点ID
     * @param fieldid 字段ID
     * @param fieldname 字段名称
     * @param fieldvalue 字段值

     * @param fieldhtmltype 字段HTML类型
     * @param fieldtype 字段类型
     * @param fielddbtype 字段数据库类型

     * @param fieldlable 字段Label
     * @param fieldformname 字段页面显示名称（例：field10024）

     * @param fieldorder 页面显示顺序
     * @param languageid 当前用户语言
     * @param isview 是否可显示

     * @param isedit 是否可编辑

     * @param ismand 是否必填
     * @param user 当前用户
     * @param specialfield 特殊字段信息
     * @param docfieldids 流程正文
     * @param flagDefault 是否需要默认值（创建节点）

     * @param groupid 明细表Index，主表为0
     * @param selfieldsadd 流程显示属性联动select 字段id集合
     * @param changefieldsadd 流程显示属性联动 被联动字段id集合
     * @param detailRecordId 明细表Id
     * @return WorkflowRequestTableField
     * @throws Exception
     */
    public static WorkflowRequestTableField getWorkflowRequestField(String workflowid, int requestid, int nodeid, String fieldid, String fieldname, String fieldvalue, String fieldhtmltype, String fieldtype, String fielddbtype, String fieldlable, String fieldformname, int fieldorder, int languageid,
            String isview, String isedit, String ismand, User user, HashMap specialfield, List docfieldids, boolean flagDefault,int groupid, List selfieldsadd, List changefieldsadd, int detailRecordId, String isAutoLocate,int isremark, int takisremark) throws Exception {
        return getWorkflowRequestField(workflowid, requestid, nodeid, fieldid, fieldname, fieldvalue, fieldhtmltype, fieldtype, fielddbtype, fieldlable, fieldformname, fieldorder, languageid, isview, isedit, ismand, user, specialfield, docfieldids, flagDefault, groupid, selfieldsadd, changefieldsadd, detailRecordId, "",-1,-1,"",true);
    }
    
	/**
	 * 取得某字段信息（新接口，包含明细字段、字段属性联动的处理）

	 * @param workflowid 流程ID
	 * @param requestid 请求id
	 * @param nodeid 节点ID
	 * @param fieldid 字段ID
	 * @param fieldname 字段名称
	 * @param fieldvalue 字段值

	 * @param fieldhtmltype 字段HTML类型
	 * @param fieldtype 字段类型
	 * @param fielddbtype 字段数据库类型

	 * @param fieldlable 字段Label
	 * @param fieldformname 字段页面显示名称（例：field10024）

	 * @param fieldorder 页面显示顺序
	 * @param languageid 当前用户语言
	 * @param isview 是否可显示

	 * @param isedit 是否可编辑

	 * @param ismand 是否必填
	 * @param user 当前用户
	 * @param specialfield 特殊字段信息
	 * @param docfieldids 流程正文
	 * @param flagDefault 是否需要默认值（创建节点）

	 * @param groupid 明细表Index，主表为0
	 * @param selfieldsadd 流程显示属性联动select 字段id集合
	 * @param changefieldsadd 流程显示属性联动 被联动字段id集合
	 * @param detailRecordId 明细表Id
	 * @param isAutoLocate 是否为自动定位字段

	 * @param module 用于督办和监控的判断 -1004为督办，-1005为监控

	 * @return WorkflowRequestTableField
	 * @throws Exception
	 */
	public static WorkflowRequestTableField getWorkflowRequestField(String workflowid, int requestid, int nodeid, String fieldid, String fieldname, String fieldvalue, String fieldhtmltype, String fieldtype, String fielddbtype, String fieldlable, String fieldformname, int fieldorder, int languageid,
			String isview, String isedit, String ismand, User user, HashMap specialfield, List docfieldids, boolean flagDefault,int groupid, List selfieldsadd, List changefieldsadd, int detailRecordId, String isAutoLocate,int isremark, int takisremark,String module,boolean freeNodeIsEdit) throws Exception {

		log.info("Start to run getWorkflowRequestField method.");
		log.info("The value of the 'fieldid' is:\t" + fieldid);
		log.info("The value of the 'fieldname' is:\t" + fieldname);
		log.info("The value of the 'fieldValue' is:\t" + fieldvalue);
		
		if(languageid==0)languageid=7;
		
		ProjectInfoComInfo projectInfoComInfo = new ProjectInfoComInfo();
		ResourceComInfo resourceComInfo = new ResourceComInfo();
		DocReceiveUnitComInfo docReceiveUnitComInfo_vb = new DocReceiveUnitComInfo();
		CustomerInfoComInfo customerInfoComInfo = new CustomerInfoComInfo();
		CarInfoComInfo carInfoComInfo = new CarInfoComInfo();
		MeetingRoomComInfo meetingRoomComInfo = new MeetingRoomComInfo();
		MeetingTypeComInfo meetingTypeComInfo = new MeetingTypeComInfo();
		DepartmentComInfo departmentComInfo = new DepartmentComInfo();
		DocComInfo docComInfo = new DocComInfo();
		CapitalComInfo capitalComInfo = new CapitalComInfo();
		WorkflowRequestComInfo workflowRequestComInfo = new WorkflowRequestComInfo();
		BrowserComInfo browserComInfo = new BrowserComInfo();
		WorkflowComInfo workflowComInfo = new WorkflowComInfo();
		DocImageManager docImageManager = new DocImageManager();
		SecCategoryComInfo secCategoryComInfo_vb = new SecCategoryComInfo();
		SubCompanyComInfo subCompanyComInfo = new SubCompanyComInfo();
		ResourceConditionManager resourceConditionManager = new ResourceConditionManager();
		BudgetfeeTypeComInfo budgetfeeTypeComInfo = new BudgetfeeTypeComInfo();
		weaver.hrm.attendance.manager.HrmLeaveTypeColorManager leaveTypeManager = new weaver.hrm.attendance.manager.HrmLeaveTypeColorManager();
	    HrmScheduleShiftsSetManager scheduleManager = new HrmScheduleShiftsSetManager();
		RecordSet rsFna = new RecordSet();

		char flag = Util.getSeparator();
		RecordSet rs = new RecordSet();
		String isbill = workflowComInfo.getIsBill(WorkflowVersion.getActiveVersionWFID(workflowid));
		int formID = Util.getIntValue(workflowComInfo.getFormId(WorkflowVersion.getActiveVersionWFID(workflowid)), 0);
		
		if("8888".equals(fieldid)){
			//System.out.print(">>>>>>>>>>>>>>>>>");
		}
		
		//----------------------------------
		// 属性联动功能 START
		//----------------------------------
		String jsStr = "";
		//----------------------------------
		// 属性联动功能 END
		//----------------------------------
		
		//字段联动功能实现
		String dataLinkageStr = null;
		String dataChangeString = null;
		
		
		//===================================
		// 从缓存中获取
		//===================================
		
		String initDynmjs = "";
		String cValFields = fieldformname;
		String cValFieldsDetail = "";
		boolean isdetail2 = false;
		if(fieldformname.indexOf("_")>=0){
			cValFields = fieldformname.split("_")[0];
			cValFieldsDetail = "_"+fieldformname.split("_")[1];
			isdetail2 =true;
		}
		//List lstInputFields = DynamicDataInput.getTriggerInputFieldName(workflowid, fieldformname);
		WorkflowDynamicDataComInfo wfDynmDataCominfo = new WorkflowDynamicDataComInfo();
		List lstInputFields = wfDynmDataCominfo.getCValFields(workflowid, cValFields);
		
		if (lstInputFields != null) {
			int len = lstInputFields.size();
			if(len > 0){
				//字段联动初始化js
				initDynmjs = "<script type=\"text/javascript\">$(document).ready(function () {";
				
				dataLinkageStr = "dataInput('"+ fieldformname +"','";
				dataChangeString = " onchange=\"";
				for (int i = 0; i < len; i++) {
					if(i==0){
						if(isdetail2){
						  String detailfiled = ""+lstInputFields.get(i);
						  String detailfieldnew = "";
						  if(detailfiled.indexOf(",")!=-1){
							  String[] detailfiledStr = detailfiled.split(",");
							    for(int j=0;j<detailfiledStr.length;j++){
							    	if(detailfiledStr[j].indexOf(cValFields)<0){
							    	detailfieldnew += detailfiledStr[j]+cValFieldsDetail+"_d";
									}else{
									detailfieldnew += detailfiledStr[j]+cValFieldsDetail;
									}
							    	if(j!=(detailfiledStr.length-1)){
							    		detailfieldnew += ","; 
							    	}
							    }
							  
						  }else{
							  detailfieldnew = detailfiled;
						  }
						  dataLinkageStr +=  detailfieldnew;
						}else{
						  dataLinkageStr += ""+ lstInputFields.get(i) ;	
						}
					}else{
						if(isdetail2){
							  String detailfiled = ""+lstInputFields.get(i);
							  String detailfieldnew = "";
							  if(detailfiled.indexOf(",")!=-1){
								  String[] detailfiledStr = detailfiled.split(",");
								    for(int j=0;j<detailfiledStr.length;j++){
								    	detailfieldnew += detailfiledStr[j]+cValFieldsDetail;
								    	if(j!=(detailfiledStr.length-1)){
								    		detailfieldnew += ","; 
								    	}
								    }
							  }else{
								  detailfieldnew = detailfiled;
							  }
							dataLinkageStr += ","+detailfieldnew;
						}else{
						  dataLinkageStr += ","+ lstInputFields.get(i) ;	
						}
					}
				}
				dataLinkageStr += "');";
				dataChangeString += dataLinkageStr;
				dataChangeString += "\" ";
				
				initDynmjs += "\r\n";
				initDynmjs += dataLinkageStr;
				dataChangeString +="try{maindetailfieldchange(this);}catch(e){}";
				initDynmjs += "\r\n";
				initDynmjs += "});</script>";
			}
		}
		log.info("Following is the 'dataChangeString':\n" + dataChangeString);
		String showname = Util.toScreen(fieldlable, languageid);
		String showvalue = "";
		List selectnamelist = new ArrayList();
		List selectvaluelist = new ArrayList();
		String browserurl = "";
		String htmlshow = "";
        boolean isshowtree = false;
		
		//字段属性联动 元素非必填也需要生成必填标示 START
		String disIsmand = " style = \"display:none\" ";
		String ismandfieldval = "";
		
    	if ("1".equals(ismand)) {
    		disIsmand = " class=\"ismand\" ";
    		ismandfieldval = fieldformname;
    	}
		//字段属性联动 元素非必填也需要生成必填标示 END
    	
	    if(fieldname.equals("manager")) {
	    	//Manager字段特殊处理
	    	fieldvalue = resourceComInfo.getManagerID(user.getUID()+"");
	    	showvalue = resourceComInfo.getLastname(fieldvalue);
	    	
	    	htmlshow = "";
	    	//原本根据字段isEdit(是否可编辑)来设置manager字段值是否显示, 
	    	//现根据该字段isView(是否可显示)决定该字段值是否显示

	    	if("1".equals(isview)) htmlshow = 
		    	"<input type=\"hidden\" name=\""+fieldformname+"\" id=\""+fieldname+"\" value=\""+fieldvalue+"\" />"+
		    	"<span id=\""+fieldname+"_span\">"+showvalue+"</span>";
	    	
	    	isedit = "1";
			ismand = "0";
	    
	    } else if(docfieldids.indexOf(fieldid)>-1&&fieldvalue!=null&&!"".equals(fieldvalue)) {
	    	//流程正文特殊处理
	    	rs.executeSql("select doceditionid from DocDetail where doceditionid>-1 and id=" + fieldvalue);
	    	if (rs.next()) {
	    		//获取正文最新版本

	    		int doceditionid = Util.getIntValue(Util.null2String(rs.getString("doceditionid")));
	    		if (doceditionid > -1) {
		    		rs.executeSql("select MAX(id) id from DocDetail where doceditionid="+doceditionid);
			    	if (rs.next()) {
			    		fieldvalue = Util.null2String(rs.getString("id"));
			    	}
		    	}
	    	}
	    	
	    	if (!"".equals(fieldvalue)) {
				//流程正文特殊处理
				rs.executeSql("select doceditionid from DocDetail where doceditionid>-1 and id=" + fieldvalue);
				if (rs.next()) {
					//获取正文最新版本

					int doceditionid = Util.getIntValue(Util.null2String(rs.getString("doceditionid")));
					if (doceditionid > -1) {
						rs.executeSql("select MAX(id) id from DocDetail where doceditionid="+doceditionid);
						if (rs.next()) {
							fieldvalue = Util.null2String(rs.getString("id"));
						}
					}
				}
	    		rs.executeSql("select i.imagefileid,di.imagefilename,i.imagefiletype,i.fileSize,di.id,di.isextfile from docimagefile di,imagefile i where di.imagefileid = i.imagefileid and di.docid = "+fieldvalue+" order by (case when di.isextfile is null or di.isextfile='' then '0' else di.isextfile end) asc,di.id asc,di.versionId desc");
				int tempid = -1;
				int rscnt = 0;
				boolean isprefix = true;
				while(rs.next()){
					int id = Util.getIntValue(Util.null2String(rs.getString("id")));
					if(id == tempid) {
						continue;
					} else {
						tempid = id;
					}
					String docImagefileid = Util.null2String(rs.getString("imagefileid"));
					String docImagefilename = Util.null2String(rs.getString("imagefilename"));
					int docImagefileSize = Util.getIntValue(Util.null2String(rs.getString("fileSize")),0);
					rscnt++;
					if (rscnt != 1) {
						showvalue += "<br/><br/>";
					}
					String isextfile = rs.getString("isextfile");
					if ("1".equals(isextfile)) {
						if (isprefix) {
							showvalue += "<span style='color:#666666;'>附件:</span>";
							isprefix = false;
						} else {
							showvalue += "<span style='color:#666666;padding-left:25px'>&nbsp;</span>";
						}
					}
                   String downloadDocImageFileName = docImagefilename.replaceAll("&","%26");
					showvalue += "<span style='text-decoration:underline;color:blue' onclick=\"toDownload('" + docImagefileid + "','" + downloadDocImageFileName + "',false);\" style=\"cursor:hand;\">" + docImagefilename + "(" + (docImagefileSize / 1000) + "K)" + "</span>";
				}
	    	}
	    	
			htmlshow = showvalue;
			
			if("1".equals(isedit)) {
	    		if (showvalue != null && !"".equals(showvalue.trim())) {
	    			htmlshow += FieldOnlyShow;
	    		} else {
	    			htmlshow += FieldnonSupport;
	    		}
	    	}
	    } else {
			if (fieldhtmltype.equals("1") || fieldhtmltype.equals("2")) {// 单行,多行文本框

				htmlshow = "";
				//金额千分位显示处理 START
		    	if (fieldhtmltype.equals("1") && fieldtype.equals("5")) {
					String targetValue = fieldvalue;
					double tmpDbl = 0d;
					if (targetValue != null && !targetValue.equals("") && targetValue.indexOf(",") == -1) {
						try {
							tmpDbl = Double.parseDouble(targetValue);
							DecimalFormat decimalFormat = new DecimalFormat("###,###.######");
							fieldvalue = decimalFormat.format(tmpDbl);
							showvalue = fieldvalue;
						} catch (NumberFormatException e) {}
					}
				}
		    	//金额千分位显示处理 END
				
		    	if(fieldhtmltype.equals("1")  && !"1".equals(isedit)){
					showvalue = "<span id=\"" +  fieldformname + "_span\" name=\"" + fieldformname + "_span\" >" + fieldvalue + "</span>";
					int decimaldigits_t =2;
			    	int digitsIndex = fielddbtype.indexOf(",");
					if(digitsIndex > -1){
						decimaldigits_t = Util.getIntValue(fielddbtype.substring(digitsIndex+1, fielddbtype.length()-1), 2);
					}else{
						decimaldigits_t = 2;
					}
					if(fieldtype.equals("4")){ //金额转换
						 showvalue += "<input type='hidden' datatype=\"float\" datetype=\"float\" datavaluetype=\"4\" datalength=\""+decimaldigits_t+"\"   name='" + fieldformname + "' id='"+ fieldformname +"' _fieldhtmlType='"+ fieldhtmltype + "' value='"+ fieldvalue +"' nameBak='"+ fieldname + "' >";    
					     if (fieldhtmltype.equals("1") && fieldtype.equals("4") && fieldvalue!=null && !"".equals(fieldvalue)) {
								showvalue += "<script language=\"javascript\">";
								showvalue += " jQuery(function(){";
								showvalue += " var showjezhvalue=(jQuery(\"#"+fieldformname+"\").val().length>0?jQuery(\"#"+fieldformname+"\").val():'"+fieldvalue+"');showjezhvalue=toPrecision(showjezhvalue,"+decimaldigits_t+");var showjezhhtml=(showjezhvalue==''?'':'&nbsp;('+showjezhvalue+')');jQuery(\"span[id='"+fieldformname+"_span']\").html(numberChangeToChinese(showjezhvalue)+''+showjezhhtml)}); ";
								showvalue += "</script>";
						 }
					}else if(fieldtype.equals("5")){//金额千分位

						 showvalue += "<input type='hidden' datatype=\"float\" datetype=\"float\" datavaluetype=\"5\" datalength=\""+decimaldigits_t+"\"  name='" + fieldformname + "' id='"+ fieldformname +"' _fieldhtmlType='"+ fieldhtmltype + "' value='"+ fieldvalue +"' nameBak='"+ fieldname + "' >";
					}else if(fieldtype.equals("2")){//整数 
					     showvalue += "<input type='hidden' datatype=\"int\" datetype=\"int\" datavaluetype=\"2\" datalength=\"0\"  name='" + fieldformname + "' id='"+ fieldformname +"' _fieldhtmlType='"+ fieldhtmltype + "' value='"+ fieldvalue +"' nameBak='"+ fieldname + "' >";
					}else if(fieldtype.equals("3")){//浮点数

					      showvalue += "<input type='hidden' _dataBaseValue=\""+fieldvalue+"\" datatype=\"float\" datetype=\"float\" datavaluetype=\"3\" datalength=\""+decimaldigits_t+"\"  name='" + fieldformname + "' id='"+ fieldformname +"' _fieldhtmlType='"+ fieldhtmltype + "' value='"+ fieldvalue +"' nameBak='"+ fieldname + "' >";
					}else{
						showvalue += "<input type='hidden'  name='" + fieldformname + "' id='"+ fieldformname +"' _fieldhtmlType='"+ fieldhtmltype + "' value='"+ fieldvalue +"' nameBak='"+ fieldname + "' >";
					}
				}else{
					if(fieldhtmltype.equals("2")){
						fieldvalue= fieldvalue.replace("\\r","<br>");
					}
					showvalue = fieldvalue;
				}
		    	
		    	if("1".equals(isedit)) {
		    		if(fieldhtmltype.equals("2") && fieldtype.equals("2")) {//HTML字段
		    			showvalue = stripNonValidXMLCharacters(showvalue);
						htmlshow = "<table style=\"width:100%;\"><tr><td style=\"width:99%;white-space:normal;\" align=\"left\">";
						if (showvalue != null && !"".equals(showvalue.trim())) {
							if(mobileVersion45 && clientMobileVersion45){
								if(androidclienttype && !androidclientosver){ //如果客户手机系统是安卓并且操作系统小于4.1.1 则不启用富文本

									htmlshow +=showvalue;
									htmlshow += FieldOnlyShow;
								}else{
									showvalue=showvalue.replaceAll("<","&lt;").replaceAll(">","&gt;");
					    			htmlshow += "<textarea cols=\"40\" rows=\"8\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" onchange=\"try{maindetailfieldchange(this);}catch(e){}\">"+showvalue+"</textarea>";
								}
								
							}else{
								htmlshow +=showvalue;
								htmlshow += FieldOnlyShow;
							}
			    					    			
			    		} else {
			    			if(mobileVersion45 && clientMobileVersion45){
			    				if(androidclienttype && !androidclientosver){
			    					htmlshow += FieldOnlyShow;
			    				}else{
			    					htmlshow += "<textarea cols=\"40\" rows=\"8\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" onchange=\"try{maindetailfieldchange(this);}catch(e){}\"></textarea>";
			    				}			    				
			    			}else{
			    				htmlshow += FieldOnlyShow;
			    			}
				    		
			    		}
						if(mobileVersion45 && clientMobileVersion45)
							htmlshow +="<script language=\"javascript\">jQuery(function(){if(isShowEditor()){highEditor('"+fieldformname+"',100);}}); </script>";

			    	  	htmlshow += "<span id=\"" + fieldformname + "_ismandspan\" " + disIsmand + ">" + getRequiredMark() + "</span>";
			    	  	htmlshow += "<input type=\"hidden\" id=\"" + fieldformname + "_ismandfield\" name=\"ismandfield\" value=\"" + ismandfieldval + "\"/></td>";
						htmlshow += "</tr></table>";
		    			//重新初始化静态变量

		    			//mobileVersion45 = false;
		    			//clientMobileVersion45 = false;
		    			//androidclienttype = false;
		    			//androidclientosver = false;
		    		} else {
		    			//单行文本长度获取
			    		int fieldlen=0;
			    		if ((fielddbtype.toLowerCase()).indexOf("varchar")>-1) {
			    		   fieldlen = Util.getIntValue(fielddbtype.substring(fielddbtype.indexOf("(") + 1, fielddbtype.length() - 1));
			    		}
			    		
			    		htmlshow = "<table style=\"width:100%;\"><tr><td style=\"width:99%;white-space:normal;\" align=\"left\">";
			    		//单行文本框

				    	if (fieldhtmltype.equals("1")) {
				    	  //文本
				    		if (fieldtype.equals("1")) { 
								fieldvalue =fieldvalue != null && !"".equals(fieldvalue) ? fieldvalue.replace("\"", "&#34;"):"";
				    			htmlshow += "<input placeholder=\"请输入\" type=\"text\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\" onchange=\"checkLength('" + fieldformname + "'," + fieldlen + ", '" + Util.toScreen(fieldlable, languageid) + "', '" + SystemEnv.getHtmlLabelName(20246, user.getLanguage()) + "', '" + SystemEnv.getHtmlLabelName(20247, user.getLanguage()) + "');";
				    			if(dataLinkageStr != null){
				    			  htmlshow += dataLinkageStr;
				    			}
				    			htmlshow += "try{maindetailfieldchange(this);}catch(e){}\"/>";
				    		//整数
				    		} else if (fieldtype.equals("2")) {
								//add by liaodong datatype=\"int\" datetype=\"int\" datavaluetype=\"2\" datalength=\"0\"
				    			htmlshow += "<input placeholder=\"请输入\" type=\"text\" datatype=\"int\" datetype=\"int\" datavaluetype=\"2\" datalength=\"0\"  onkeypress=\"ItemNum_KeyPress('" + fieldformname + "')\" onblur=\"checknumber1(this);calSum("+groupid+");\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\"";
				    			if(dataChangeString != null){
				    				htmlshow += dataChangeString;
					    		}else {
									htmlshow += "onchange=\"try{maindetailfieldchange(this);}catch(e){}\""; 
								}
				    			htmlshow += " />";
			                //浮点数

				    		} else if (fieldtype.equals("3")) {
								//add by liaodong datatype=\"float\" datetype=\"float\" datavaluetype=\"3\" 
				    			int decimaldigits_t =2;
				    			int digitsIndex = fielddbtype.indexOf(",");
								if(digitsIndex > -1){
									decimaldigits_t = Util.getIntValue(fielddbtype.substring(digitsIndex+1, fielddbtype.length()-1), 2);
								}else{
									decimaldigits_t = 2;
								}
				    			//end
				    			htmlshow += "<input placeholder=\"请输入\" type=\"text\" _dataBaseValue=\""+fieldvalue+"\" datatype=\"float\" datetype=\"float\" datavaluetype=\"3\" datalength=\""+decimaldigits_t+"\" onkeypress=\"ItemNum_KeyPress('" + fieldformname + "')\" onblur=\"checknumber1(this);calSum("+groupid+");\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\"";
				    			if(dataChangeString != null){
				    				htmlshow += dataChangeString;
					    		}else {
									htmlshow += "onchange=\"try{maindetailfieldchange(this);}catch(e){}\""; 
								}
				    			htmlshow += " />";
				    		//金额转换
				    		} else if (fieldtype.equals("4")) {
								//add by liaodong datatype=\"float\" datetype=\"float\" datavaluetype=\"4\"
				    			
				    			if (fieldhtmltype.equals("1") && fieldtype.equals("4") && fieldvalue!=null && !"".equals(fieldvalue)) {
						    		showvalue = "<span id=\""+fieldformname.replace("field", "field_chinglish")+"_span\"></span>&nbsp;(" + fieldvalue + ")";
									showvalue += "<script language=\"javascript\">";
									showvalue += " jQuery(function(){jQuery(\"span[id='"+fieldformname.replace("field", "field_chinglish")+"_span']\").html(numberChangeToChinese("+fieldvalue+"))});";
									showvalue += "</script>";
								}
				    			
				    			int decimaldigits_t =2;
				    			int digitsIndex = fielddbtype.indexOf(",");
								if(digitsIndex > -1){
									decimaldigits_t = Util.getIntValue(fielddbtype.substring(digitsIndex+1, fielddbtype.length()-1), 2);
								}else{
									decimaldigits_t = 2;
								}
				    			//end
				    			htmlshow += "<input placeholder=\"请输入\" type=\"text\" datatype=\"float\" datetype=\"float\" datavaluetype=\"4\" datalength=\""+decimaldigits_t+"\"  onkeypress=\"ItemNum_KeyPress('" + fieldformname + "')\" onkeypress=\"ItemNum_KeyPress('" + fieldformname.replace("field", "field_lable") + "')\" onblur=\"checknumber1(this);numberToFormat('" + fieldformname.replace("field", "") + "');calSum("+groupid+"); ";
				    			if(dataLinkageStr != null){
					    			  htmlshow += dataLinkageStr;
					    		}
				    			htmlshow += "try{maindetailfieldchange(this);}catch(e){}\" onfocus=\"FormatToNumber('" + fieldformname.replace("field", "") + "')\" name=\""+fieldformname.replace("field", "field_lable")+"\" id=\""+fieldformname.replace("field", "field_lable")+"\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\" />";
				    			htmlshow += "<br/><input placeholder=\"请输入\" type=\"text\" name=\""+fieldformname.replace("field", "field_chinglish")+"\" id=\""+fieldformname.replace("field", "field_chinglish")+"\" value=\""+fieldvalue+"\" readOnly=\"\"/>";
								htmlshow += "<input type=\"hidden\" id=\"" + fieldformname + "\" datatype=\"float\" datetype=\"float\" datavaluetype=\"4\" datalength=\""+decimaldigits_t+"\" name=\"" + fieldformname + "\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\"/>";
								htmlshow += "<script language=\"javascript\">";
								htmlshow += "jQuery(document).ready(function(){numberToFormat('" + fieldformname.replace("field", "") + "');});";
								htmlshow += "</script>";
				    		//金额千分位

				    		} else if (fieldtype.equals("5")) {
								//add by liaodong datatype=\"float\" datetype=\"float\" datavaluetype=\"5\"
				    			int decimaldigits_t =2;
				    			int digitsIndex = fielddbtype.indexOf(",");
								if(digitsIndex > -1){
									decimaldigits_t = Util.getIntValue(fielddbtype.substring(digitsIndex+1, fielddbtype.length()-1), 2);
								}else{
									decimaldigits_t = 2;
								}
				    			//end
				    			htmlshow += "<input placeholder=\"请输入\" type=\"text\" datatype=\"float\" datetype=\"float\" datavaluetype=\"5\" datalength=\""+decimaldigits_t+"\" onkeypress=\"ItemNum_KeyPress('" + fieldformname + "')\" onblur=\"checknumber1(this);changeToThousands('" + fieldformname + "');calSum("+groupid+");";
				    			if(dataLinkageStr != null){
					    			  htmlshow += dataLinkageStr;
					    		}
				    			htmlshow += "try{maindetailfieldchange(this);}catch(e){}\" onfocus=\"changeToNormalFormat('" + fieldformname + "')\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\" />";
				    		//其它
				    		} else {
				    			htmlshow += "<input placeholder=\"请输入\" type=\"text\" fieldtype=\"" + fieldtype + "\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\" ";
				    			if(dataChangeString != null){
				    				htmlshow += dataChangeString;
					    		}else{
								    htmlshow += "onchange=\"try{maindetailfieldchange(this);}catch(e){}\""; 
								}
				    			htmlshow += "/>";
				    		}
				    	} else {
				    		//非html格式的多行文本框，其值直接包含在textArea标签中间，故对其值不能作任何转换；否则将会改变其原始值。

				    		fieldvalue = fieldvalue.replace("\r","").replace("\n","").replace("<br>","\\r").replace("&nbsp;"," ").replace("\"", "&#34;");
				    		htmlshow += "<textarea placeholder=\"请输入\"   cols=\"40\" rows=\"8\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" onchange=\"try{maindetailfieldchange(this);}catch(e){}\"></textarea>";
				    		htmlshow += "<script>jQuery(function(){jQuery('#"+fieldformname+"').html(\""+ fieldvalue +"\");});</script>";
				    	}
				    	
				    	htmlshow += "</td>";
			    	  	htmlshow += "<td><span id=\"" + fieldformname + "_ismandspan\" " + disIsmand + ">" + getRequiredMark() + "</span>";
			    	  	htmlshow += "<input type=\"hidden\" id=\"" + fieldformname + "_ismandfield\" name=\"ismandfield\" value=\"" + ismandfieldval + "\"/></td>";
				    	htmlshow += "</tr></table>";
		    		}
		    	} else {
		    		htmlshow = showvalue;
					showvalue = fieldvalue;
					
					if ("2".equals(fieldhtmltype)) {
						htmlshow = "<span name=\""+fieldformname+"_span\" id=\"" + fieldformname + "_span\">"+ showvalue + "</span>";
		    			htmlshow += "<textarea cols=\"40\" rows=\"8\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" _fieldhtmlType=\"" + fieldhtmltype + "\" nameBak=\""+fieldname+"\" style=\"display: none;\" onchange=\"try{maindetailfieldchange(this);}catch(e){}\">"
			    			+ showvalue.replaceAll("<","&lt;").replaceAll(">","&gt;")
			    			+ "</textarea>";
					}
		    	}
		
			} else if (fieldhtmltype.equals("3")) {// 浏览按钮 (涉及workflow_broswerurl表)
				if(flagDefault && (fieldvalue==null||"".equals(fieldvalue))) {
					String prjid = "";
					String docid = "";
					String hrmid = user.getUID()+"";
					String crmid = "";
					String reqid = "";
					
		            if((fieldtype.equals("8") || fieldtype.equals("135")) && !prjid.equals("")){       //浏览按钮为项目,从前面的参数中获得项目默认值

		            	fieldvalue = "" + Util.getIntValue(prjid,0);
		            }else if((fieldtype.equals("9") || fieldtype.equals("37")) && !docid.equals("")){ //浏览按钮为文档,从前面的参数中获得文档默认值

		            	fieldvalue = "" + Util.getIntValue(docid,0);
		            }else if((fieldtype.equals("1") ||fieldtype.equals("17")||fieldtype.equals("165")||fieldtype.equals("166")) && !hrmid.equals("")){ //浏览按钮为人,从前面的参数中获得人默认值

		            	fieldvalue = "" + Util.getIntValue(hrmid,0);
		            }else if((fieldtype.equals("7") || fieldtype.equals("18")) && !crmid.equals("")){ //浏览按钮为CRM,从前面的参数中获得CRM默认值

		            	fieldvalue = "" + Util.getIntValue(crmid,0);
		            }else if((fieldtype.equals("16") || fieldtype.equals("152") || fieldtype.equals("171")) && !reqid.equals("")){ //浏览按钮为REQ,从前面的参数中获得REQ默认值

		            	fieldvalue = "" + Util.getIntValue(reqid,0);
					}else if((fieldtype.equals("4") || fieldtype.equals("57") || fieldtype.equals("167") || fieldtype.equals("168")) && !hrmid.equals("")){ //浏览按钮为部门,从前面的参数中获得人默认值(由人力资源的部门得到部门默认值)
						fieldvalue = "" + Util.getIntValue(resourceComInfo.getDepartmentID(hrmid),0);
		            }else if((fieldtype.equals("24") || fieldtype.equals("278")) && !hrmid.equals("")){ //浏览按钮为职务,从前面的参数中获得人默认值(由人力资源的职务得到职务默认值)
		            	fieldvalue = "" + Util.getIntValue(resourceComInfo.getJobTitle(hrmid),0);
		            //QC 152573 新增多分部字段的初始值设定

		            }else if((fieldtype.equals("164") || fieldtype.equals("169") || fieldtype.equals("170") || fieldtype.equals("194")) && !hrmid.equals("")){ //浏览按钮为分部,从前面的参数中获得人默认值(由人力资源的分部得到分部默认值)
		            	fieldvalue = "" + Util.getIntValue(resourceComInfo.getSubCompanyID(hrmid),0);
		            } else if(fieldtype.equals("2") || fieldtype.equals("19") || fieldtype.equals("178")){ //浏览按钮为年份(178)、日期(2)、时间(19)
		            	Date date = new Date();
		            	//日期
		            	if(fieldtype.equals("2")){
		            		fieldvalue = new SimpleDateFormat("yyyy-MM-dd").format(date);
		            	}
		            	//时间
		            	if(fieldtype.equals("19")){
		            		fieldvalue = new SimpleDateFormat("HH:mm").format(date);
		            	}
		            	//年份
		            	if(fieldtype.equals("178")){
		            		fieldvalue = new SimpleDateFormat("yyyy").format(date);
		            	}
		            }
				}
				
				if (fieldtype.equals("2") || fieldtype.equals("19") || fieldtype.equals("178")) {
					// 年份(178)、日期(2)、时间(19)
					showvalue = fieldvalue;
					
			    	htmlshow = "";
			    	if("1".equals(isedit)) {
			    	  //日期
			    	  if("2".equals(fieldtype)){
			    	    htmlshow = "<table style=\"width:100%;\"><tr><td style=\"width:99%;white-space:normal;\" class=\"datetimeWrap\" attrfieldid=\""+fieldformname+"\" align=\"left\">";
			    	    htmlshow += "<input type=\"text\" placeholder=\"请输入\"  class=\"scroller_date\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\" readonly=\"true\" ";
		    			if(dataChangeString != null){
		    				htmlshow += dataChangeString;
			    		}else{
						    htmlshow += "onchange=\"try{maindetailfieldchange(this);}catch(e){}\""; 
						}
			    	    htmlshow += " /><img src=\"/images/dateselect.png\" class=\"calendaricon_class\"></td>";
			    	  }
			    	  //时间
			    	  if("19".equals(fieldtype)){
			    	    htmlshow = "<table style=\"width:100%;\"><tr><td style=\"width:99%;white-space:normal;\" align=\"left\">";
			    	    htmlshow += "<input type=\"text\" placeholder=\"请输入\"  class=\"scroller_time\"  name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\" readonly=\"true\" ";
			    	    if(dataChangeString != null){
			    	    	htmlshow += dataChangeString;
			    	    }else{
						    htmlshow += "onchange=\"try{maindetailfieldchange(this);}catch(e){}\""; 
						}
			    	    htmlshow += " />"+"</td>";
			    	  }
			    	  //年份
			    	  if("178".equals(fieldtype)){
			    	    htmlshow = "<table style=\"width:100%;\"><tr><td style=\"width:99%;white-space:normal;\" align=\"left\">";
			    	    htmlshow += "<input type=\"text\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\" onblur=\"validateYear(this);\" ";
			    	    if(dataChangeString != null){
			    	    	htmlshow += dataChangeString;
			    	    }else{
						    htmlshow += "onchange=\"try{maindetailfieldchange(this);}catch(e){}\""; 
						}
			    	    htmlshow += " />"+"</td>";
			    	  }

			    	  htmlshow += "<td><span id=\"" + fieldformname + "_ismandspan\" " + disIsmand + ">" + getRequiredMark() + "</span>";
			    	  htmlshow += "<input type=\"hidden\" id=\"" + fieldformname + "_ismandfield\" name=\"ismandfield\" value=\"" + ismandfieldval + "\"/></td>";

				    	
				      htmlshow += "</tr></table>";
			    	} else {
			    		//htmlshow = showvalue;
			    		htmlshow = "<span id=\"" +  fieldformname + "_span\" name=\"" + fieldformname + "_span\" >" + showvalue + "</span>";
			    		//判据当前不是请假申请单  和  会议审批单, 以及不为空

			    		if(formID != 0 && formID != 180 && formID != 85){
			    			if(flagDefault && (fieldvalue!=null&&!"".equals(fieldvalue))&&(!"1".equals(isedit))) {
			    				htmlshow += "<input type=\"hidden\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" _fieldhtmlType=\"" + fieldhtmltype + "\" _fieldType=\"" + fieldtype + "\" value=\""+fieldvalue+"\"/>";
			    				isedit = "1";
			    			}
			    		}
			    	}
				} else {
					List tempshowidlist = Util.TokenizerString(fieldvalue, ",");
					if (fieldtype.equals("8") || fieldtype.equals("135")) {
						// 项目(8)，多项目(135)
						for (int k = 0; tempshowidlist!=null&&k < tempshowidlist.size(); k++) {
							if(k != 0) showvalue += "<div style=\"height:10px;overflow:hidden;width:1px;\"></div>";
							showvalue += "<span  keyid=\""+ tempshowidlist.get(k) +"\">"+projectInfoComInfo.getProjectInfoname((String) tempshowidlist.get(k))+"</span>";
						}
						
				    	htmlshow = "";
				    	if("1".equals(isedit)) {
				    		htmlshow = "<table style=\"width:100%;\"><tr><td style=\"width:10%;\" onclick=\"javascript:showDialog('/browser/dialog.do','&returnIdField="+fieldformname+"&returnShowField="+fieldformname+"_span&method=listProject&isMuti="+(("int".equalsIgnoreCase(fielddbtype) || "integer".equalsIgnoreCase(fielddbtype))?0:1)+"')\" '>"+
								"<a href=\"#\" data-rel=\"dialog\" data-transition=\"pop\">"+
								"<div style=\"background-image:url('/images/search_icon_wev8.png');height:30px;width:30px;\"></div></a>"+
								"<input fieldtype=\"browse\" type=\"hidden\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\" ";
			    			    //add by liaodong fieldtype=\"browse\"
			    			if(dataChangeString != null){
			    				htmlshow += dataChangeString;
				    		}else{
						    htmlshow += "onchange=\"try{maindetailfieldchange(this);}catch(e){}\""; 
						}
			    			htmlshow +=	"/></td><td id=\""+fieldformname+"_span\" style=\"width:90%;white-space:normal;\" align=\"left\">"+showvalue+"</td>";
				    		
					    	htmlshow += "<td><span id=\"" + fieldformname + "_ismandspan\" " + disIsmand + ">" + getRequiredMark() + "</span>";
					    	htmlshow += "<input type=\"hidden\" id=\"" + fieldformname + "_ismandfield\" name=\"ismandfield\" value=\"" + ismandfieldval + "\"/></td>";

					    	
					    	htmlshow += "</tr></table>";
						
				    	} else {
				    		htmlshow = showvalue;
				    	}
					} else if (fieldtype.equals("17")) {
					    
                        weaver.workflow.request.WorkflowJspBean workflowJspBean = new weaver.workflow.request.WorkflowJspBean();
                        workflowJspBean.setRequestid(requestid);
                        showvalue = workflowJspBean.getMultiResourceShowName(fieldvalue, "", fieldid, user.getLanguage());
                        boolean hasGroup = workflowJspBean.isHasGroup();
                        if (hasGroup) {
                            String[]fieldvalarray = fieldvalue.split(",");
                            List<String> fieldvalList = new ArrayList<String>();
                            for (int z=0; z<fieldvalarray.length; z++) {
                                if (!fieldvalList.contains(fieldvalarray[z])) {
                                    fieldvalList.add(fieldvalarray[z]);
                                }
                            }
                            if (fieldvalList.size() > 0) {
                                showvalue += "&nbsp;<span style='color:#bfbfc0;'>（共" + fieldvalList.size() + "人）</span>";    
                            }
                        }
                        
                        htmlshow = "<table style=\"width:100%;\"><tr>" +
                        "<td id=\""+fieldformname+"_span\" style=\"width:90%;white-space:normal;\" align=\"left\"><span name=\""+fieldformname+"_span\"  keyid=\"mingxi\">"+showvalue+"</span></td>";
                        
                        htmlshow += "<td style=\"width:10%;\" onclick=\"javascript:showDialog('/browser/dialog.do','&returnIdField="+fieldformname+"&returnShowField="+fieldformname+"_span&method=listUser&isMuti="+(("int".equalsIgnoreCase(fielddbtype) || "integer".equalsIgnoreCase(fielddbtype))?0:1)+"')\">"+
                        "<a href=\"#\" data-rel=\"dialog\" placeholder=\"请输入\" data-transition=\"pop\">"+
                        "<div style=\"background-image:url('/images/search_icon_wev8.png');height:23px;width:30px;background-size: 30px 30px;\"></div></a>"+
                        "<input  fieldtype=\"browse\" type=\"hidden\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\" ";
                        if(dataChangeString != null){
                            htmlshow += dataChangeString;
                        }else{
						    htmlshow += "onchange=\"try{maindetailfieldchange(this);}catch(e){}\""; 
						}
                        htmlshow += "/></td>";
                        htmlshow += "<td><span id=\"" + fieldformname + "_ismandspan\" " + disIsmand + ">" + getRequiredMark() + "</span>";
                        htmlshow += "<input type=\"hidden\" id=\"" + fieldformname + "_ismandfield\" name=\"ismandfield\" value=\"" + ismandfieldval + "\"/></td>";
                        
                        htmlshow += "</tr></table>";
                        
					} else if (fieldtype.equals("1") || fieldtype.equals("160") || fieldtype.equals("165") || fieldtype.equals("166")) {
					  // 人力资源(1)，多人力资源(17)，角色人员(160)，分权单人力资源(165)，分权多人力资源(166)
					  if(tempshowidlist != null){
					    // 多控制多人力资源选择数过多显示会占用大幅屏幕的问题。

					    int size = tempshowidlist.size();
					    if(size <= 5){
					      //如果选择人员数小于等于5个，则直接显示。

					      for (int k = 0; k < size; k++) {
					        if(k != 0) showvalue += "<div style=\"height:10px;overflow:hidden;width:1px;\"></div>";
					        showvalue += "<span  keyid=\""+ tempshowidlist.get(k) +"\">"+resourceComInfo.getResourcename((String) tempshowidlist.get(k))+"</span>";
					      }
					    } else {
					      //如果选择人员数大于5个，则对其显示作调整，一行显示多个。

					      showvalue += "<div style=\"height:10px;overflow:hidden;width:1px;\"></div>";
					      for (int k = 0; k < size; k++) {
					        showvalue += "<span  keyid=\""+ tempshowidlist.get(k) +"\">"+resourceComInfo.getResourcename((String) tempshowidlist.get(k))+"&nbsp;</span>";
					      }
					    }
					  }
						
				    	htmlshow = "";
				    	if("1".equals(isedit)) {
				    		if (fieldtype.equals("160")) {
				    			RecordSet recordSet = new RecordSet();
				    			recordSet.execute("select a.level_n, a.level2_n from workflow_groupdetail a ,workflow_nodegroup b where a.groupid=b.id and a.type=50 and a.objid=" 
				    					+ fieldid 
				    					+ " and b.nodeid in (select nodeid from workflow_flownode where workflowid=" 
				    					+ workflowid + " ) ");
								String roleid = "";
								int rolelevel_tmp = 0;
								if (recordSet.next()) {
									roleid = recordSet.getString(1);
									rolelevel_tmp = Util.getIntValue(recordSet.getString(2), 0);
									roleid += "a"+rolelevel_tmp;
								}
				    			
								htmlshow = "<table style=\"width:100%;\"><tr><td style=\"width:10%;\" onclick=\"javascript:showDialog('/browser/dialog.do','&returnIdField="+fieldformname+"&returnShowField="+fieldformname+"_span&method=listBrowserData&browserTypeId=" + fieldtype + "&customBrowType=" + roleid + "&isMuti=1')\">"+
									"<a href=\"#\" data-rel=\"dialog\" data-transition=\"pop\">"+
									"<div style=\"background-image:url('/images/search_icon_wev8.png');height:30px;width:30px;\"></div></a>"+
									"<input fieldtype=\"browse\" type=\"hidden\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\" ";
								    //add by liaodong fieldtype=\"browse\"
				    			if(dataChangeString != null){
				    				htmlshow += dataChangeString;
					    		}else{
						    htmlshow += "onchange=\"try{maindetailfieldchange(this);}catch(e){}\""; 
						}
				    			htmlshow +=	"/></td><td id=\""+fieldformname+"_span\" style=\"width:90%;white-space:normal;\" align=\"left\">"+showvalue+"</td>";
								
					    		htmlshow += "<td><span id=\"" + fieldformname + "_ismandspan\" " + disIsmand + ">" + getRequiredMark() + "</span>";
					    		htmlshow += "<input type=\"hidden\" id=\"" + fieldformname + "_ismandfield\" name=\"ismandfield\" value=\"" + ismandfieldval + "\"/></td>";
						    	
						    	htmlshow += "</tr></table>";
				    		} else if (fieldtype.equals("165") || fieldtype.equals("166")) {
				    			String otherParam = fieldid + ",-1," + isbill;
				    			htmlshow = "<table style=\"width:100%;\"><tr><td style=\"width:10%;\" onclick=\"javascript:showDialog('/browser/dialog.do','&returnIdField="+fieldformname+"&returnShowField="+fieldformname+"_span&method=listBrowserData&browserTypeId=" + fieldtype + "&customBrowType=" + otherParam + "&isMuti="+(("165".equalsIgnoreCase(fieldtype))?0:1)+"')\">"+
									"<a href=\"#\" data-rel=\"dialog\" data-transition=\"pop\">"+
									"<div style=\"background-image:url('/images/search_icon_wev8.png');height:30px;width:30px;\"></div></a>"+
									"<input  fieldtype=\"browse\" type=\"hidden\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\" ";
				    			    //add by liaodong fieldtype=\"browse\"
				    			if(dataChangeString != null){
				    				htmlshow += dataChangeString;
					    		}else{
						    htmlshow += "onchange=\"try{maindetailfieldchange(this);}catch(e){}\""; 
						}
				    			htmlshow +=	"/></td><td id=\""+fieldformname+"_span\" style=\"width:90%;white-space:normal;\" align=\"left\">"+showvalue+"</td>";
							
					    		htmlshow += "<td><span id=\"" + fieldformname + "_ismandspan\" " + disIsmand + ">" + getRequiredMark() + "</span>";
					    		htmlshow += "<input type=\"hidden\" id=\"" + fieldformname + "_ismandfield\" name=\"ismandfield\" value=\"" + ismandfieldval + "\"/></td>";
						    	
						    	htmlshow += "</tr></table>";
				    		}else {
				    			htmlshow = "<table style=\"width:100%;\"><tr><td id=\""+fieldformname+"_span\" style=\"width:90%;white-space:normal;\" align=\"left\">"+showvalue+"</td>";
				    					
				    			htmlshow += "<td style=\"width:10%;\" onclick=\"javascript:showDialog('/browser/dialog.do','&returnIdField="+fieldformname+"&returnShowField="+fieldformname+"_span&method=listUser&isMuti="+(("int".equalsIgnoreCase(fielddbtype) || "integer".equalsIgnoreCase(fielddbtype))?0:1)+"')\">"+
									"<a href=\"#\" data-rel=\"dialog\" style='float:right'  data-transition=\"pop\">"+
									"<div style=\"background-image:url('/images/search_icon_wev8.png');height:23px;width:30px;background-size: 30px 30px;\"></div></a>"+
								    "<input fieldtype=\"browse\" type=\"hidden\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\" ";
				    			if(dataChangeString != null){
				    				htmlshow += dataChangeString;
					    		}else{
								    htmlshow += "onchange=\"try{maindetailfieldchange(this);}catch(e){}\""; 
								}
								htmlshow +=	"/></td>";
							
								htmlshow += "<td><span id=\"" + fieldformname + "_ismandspan\" " + disIsmand + ">" + getRequiredMark() + "</span>";
								htmlshow += "<input type=\"hidden\" id=\"" + fieldformname + "_ismandfield\" name=\"ismandfield\" value=\"" + ismandfieldval + "\"/></td>";
						    	
						    	htmlshow += "</tr></table>";
				    		}
				    	} else {
				    		htmlshow = showvalue;
				    	}
					} else if (fieldtype.equals("142")) {
						// 收发文单位

						for (int k = 0; tempshowidlist!=null&&k < tempshowidlist.size(); k++) {
							showvalue += docReceiveUnitComInfo_vb.getReceiveUnitName((String) tempshowidlist.get(k)) + " ";
						}
						
				    	htmlshow = "";
				    	if("1".equals(isedit)) {
				    		htmlshow = showvalue;
				    		if (showvalue != null && !"".equals(showvalue.trim())) {
				    			htmlshow += FieldOnlyShow;
				    		} else {
				    			htmlshow += FieldnonSupport;
				    		}
				    	} else {
				    		htmlshow = showvalue;
				    	}
					} else if (fieldtype.equals("7") || fieldtype.equals("18")) {
						// 客户(7)，多客户(18)
						for (int k = 0; tempshowidlist!=null&&k < tempshowidlist.size(); k++) {
							if(k != 0) showvalue += "<div style=\"height:10px;overflow:hidden;width:1px;\"></div>";
							showvalue += "<span  keyid=\""+ tempshowidlist.get(k) +"\">"+customerInfoComInfo.getCustomerInfoname((String) tempshowidlist.get(k))+"</span>";
						}

						htmlshow = "";
				    	if("1".equals(isedit)) { 
				    		htmlshow = "<table style=\"width:100%;\"><tr><td style=\"width:10%;\" onclick=\"javascript:showDialog('/browser/dialog.do','&returnIdField="+fieldformname+"&returnShowField="+fieldformname+"_span&method=listCustomer&isMuti="+(("int".equalsIgnoreCase(fielddbtype) || "integer".equalsIgnoreCase(fielddbtype)) ?0:1)+"')\">"+
								"<a href=\"#\" data-rel=\"dialog\" data-transition=\"pop\">"+
								"<div style=\"background-image:url('/images/search_icon_wev8.png');height:30px;width:30px;\"></div></a>"+
								"<input fieldtype=\"browse\" type=\"hidden\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\" ";
				    		    //add by liaodong fieldtype=\"browse\"
			    			if(dataChangeString != null){
			    				htmlshow += dataChangeString;
				    		}else{
						    htmlshow += "onchange=\"try{maindetailfieldchange(this);}catch(e){}\""; 
						}
			    			htmlshow +=	"/></td><td id=\""+fieldformname+"_span\" style=\"width:90%;white-space:normal;\" align=\"left\">"+showvalue+"</td>";
				    		
			    			htmlshow += "<td><span id=\"" + fieldformname + "_ismandspan\" " + disIsmand + ">" + getRequiredMark() + "</span>";
			    			htmlshow += "<input type=\"hidden\" id=\"" + fieldformname + "_ismandfield\" name=\"ismandfield\" value=\"" + ismandfieldval + "\"/></td>";
					    	
					    	htmlshow += "</tr></table>";
				    	} else {
				    		htmlshow = showvalue;
				    	}
					} else if (fieldtype.equals("137") ) {
						// 车辆
						for (int k = 0; tempshowidlist!=null&&k < tempshowidlist.size(); k++) {
							if(k != 0) showvalue += "<div style=\"height:10px;overflow:hidden;width:1px;\"></div>";
							showvalue += "<span   keyid=\""+ tempshowidlist.get(k) +"\">"+carInfoComInfo.getCarNo((String) tempshowidlist.get(k))+"</span>";
						}

						htmlshow = "";
				    	if("1".equals(isedit)) { 
				    		htmlshow = "<table style=\"width:100%;\"><tr><td style=\"width:10%;\" onclick=\"javascript:showDialog('/browser/dialog.do','&returnIdField="+fieldformname+"&returnShowField="+fieldformname+"_span&method=listCar&isMuti=0')\">"+
								"<a href=\"#\" data-rel=\"dialog\" data-transition=\"pop\">"+
								"<div style=\"background-image:url('/images/search_icon_wev8.png');height:30px;width:30px;\"></div></a>"+
								"<input fieldtype=\"browse\" type=\"hidden\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\" ";
				    		    //add by liaodong fieldtype=\"browse\"
			    			if(dataChangeString != null){
			    				htmlshow += dataChangeString;
				    		}else{
						    htmlshow += "onchange=\"try{maindetailfieldchange(this);}catch(e){}\""; 
						}
			    			htmlshow +=	"/></td><td id=\""+fieldformname+"_span\" style=\"width:90%;white-space:normal;\" align=\"left\">"+showvalue+"</td>";
				    		
			    			htmlshow += "<td><span id=\"" + fieldformname + "_ismandspan\" " + disIsmand + ">" + getRequiredMark() + "</span>";
			    			htmlshow += "<input type=\"hidden\" id=\"" + fieldformname + "_ismandfield\" name=\"ismandfield\" value=\"" + ismandfieldval + "\"/></td>";
					    	
					    	htmlshow += "</tr></table>";
				    	} else {
				    		htmlshow = showvalue;
				    	}
					}else if(fieldtype.equals("22")){ //预算科目
						for (int k = 0; tempshowidlist!=null&&k < tempshowidlist.size(); k++) {
							if(k != 0) showvalue += "<div style=\"height:10px;overflow:hidden;width:1px;\"></div>";
							showvalue += "<span   keyid=\""+ tempshowidlist.get(k) +"\">"+budgetfeeTypeComInfo.getBudgetfeeTypename((String) tempshowidlist.get(k))+"</span>";
						}

						htmlshow = "";
				    	if("1".equals(isedit)) { 
				    		htmlshow = "<table style=\"width:100%;\"><tr>" ;
				    				
				    				
				    		htmlshow += "<td style=\"width:10%;\" onclick=\"javascript:showDialog('/browser/dialog.do','&returnIdField="+fieldformname+"&returnShowField="+fieldformname+"_span&method=listFnaBudgetFeeType&isMuti=0')\">"+
								"<a href=\"#\" data-rel=\"dialog\" data-transition=\"pop\">"+
								"<div class=\"cusbrowserimg\" style=\"background-image:url('/images/search_icon_wev8.png');height:30px;width:30px;\"></div></a>"+
								"<input fieldtype=\"browse\" type=\"hidden\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\" ";
			    			if(dataChangeString != null){
			    				htmlshow += dataChangeString;
				    		}else{
							    htmlshow += "onchange=\"try{maindetailfieldchange(this);}catch(e){}\""; 
							}
			    			htmlshow +=	"/></td>";
				    		htmlshow += "<td id=\""+fieldformname+"_span\" style=\"width:90%;white-space:normal;\" align=\"left\">"+showvalue+"</td>";
			    			htmlshow += "<td><span id=\"" + fieldformname + "_ismandspan\" " + disIsmand + ">" + getRequiredMark() + "</span>";
			    			htmlshow += "<input type=\"hidden\" id=\"" + fieldformname + "_ismandfield\" name=\"ismandfield\" value=\"" + ismandfieldval + "\"/></td>";
					    	
					    	htmlshow += "</tr></table>";
				    	} else {
				    		htmlshow = showvalue;
				    	}
					}else if(fieldtype.equals("251")){ //成本中心
						for (int k = 0; tempshowidlist!=null&&k < tempshowidlist.size(); k++) {
							
							String fccName = "";
							rsFna.executeSql("select name from FnaCostCenter where id = "+Util.getIntValue((String) tempshowidlist.get(k)));
							if(rsFna.next()){
								fccName = Util.null2String(rsFna.getString("name")).trim();
							}
							
							if(k != 0) showvalue += "<div style=\"height:10px;overflow:hidden;width:1px;\"></div>";
							showvalue += "<span   keyid=\""+ tempshowidlist.get(k) +"\">"+fccName+"</span>";
						}

						htmlshow = "";
				    	if("1".equals(isedit)) { 
				    		htmlshow = "<table style=\"width:100%;\"><tr><td style=\"width:10%;\" onclick=\"javascript:showDialog('/browser/dialog.do','&returnIdField="+fieldformname+"&returnShowField="+fieldformname+"_span&method=listFnaCostCenter&isMuti=0')\">"+
								"<a href=\"#\" data-rel=\"dialog\" data-transition=\"pop\">"+
								"<div style=\"background-image:url('/images/search_icon_wev8.png');height:30px;width:30px;\"></div></a>"+
								"<input fieldtype=\"browse\" type=\"hidden\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\" ";
				    		    //add by liaodong fieldtype=\"browse\"
			    			if(dataChangeString != null){
			    				htmlshow += dataChangeString;
				    		}else{
						    htmlshow += "onchange=\"try{maindetailfieldchange(this);}catch(e){}\""; 
						}
			    			htmlshow +=	"/></td><td id=\""+fieldformname+"_span\" style=\"width:90%;white-space:normal;\" align=\"left\">"+showvalue+"</td>";
				    		
			    			htmlshow += "<td><span id=\"" + fieldformname + "_ismandspan\" " + disIsmand + ">" + getRequiredMark() + "</span>";
			    			htmlshow += "<input type=\"hidden\" id=\"" + fieldformname + "_ismandfield\" name=\"ismandfield\" value=\"" + ismandfieldval + "\"/></td>";
					    	
					    	htmlshow += "</tr></table>";
				    	} else {
				    		htmlshow = showvalue;
				    	}
					}else if(fieldtype.equals("87")){

						// 会议室(87)
						for (int k = 0; tempshowidlist!=null&&k < tempshowidlist.size(); k++) {
							if(k != 0) showvalue += "<div style=\"height:10px;overflow:hidden;width:1px;\"></div>";
							showvalue += "<span keyid=\""+ tempshowidlist.get(k) +"\">"+meetingRoomComInfo.getMeetingRoomInfoname((String) tempshowidlist.get(k))+"</span>";
						}

						htmlshow = "";
				    	if("1".equals(isedit)) { 
				    		htmlshow = "<table style=\"width:100%;\"><tr><td style=\"width:10%;\" onclick=\"javascript:showDialog('/browser/dialog.do','&returnIdField="+fieldformname+"&returnShowField="+fieldformname+"_span&method=listMeetingRoom&isMuti=0')\">"+
								"<a href=\"#\" data-rel=\"dialog\" data-transition=\"pop\">"+
								"<div style=\"background-image:url('/images/search_icon_wev8.png');height:30px;width:30px;\"></div></a>"+
								"<input type=\"hidden\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\" ";
			    			if(dataChangeString != null){
			    				htmlshow += dataChangeString;
				    		}else{
						    htmlshow += "onchange=\"try{maindetailfieldchange(this);}catch(e){}\""; 
						}
			    			htmlshow +=	"/></td><td id=\""+fieldformname+"_span\" style=\"width:90%;white-space:normal;\" align=\"left\">"+showvalue+"</td>";
				    		
				    		if("1".equals(ismand)) {
					    	  	htmlshow += "<td><span id=\""+fieldformname+"_ismandspan\" class=\"ismand\">" + getRequiredMark() + "</span>";
					    	  	htmlshow += "<input type=\"hidden\" id=\"ismandfield\" name=\"ismandfield\" value=\""+fieldformname+"\"/></td>";
						    }
					    	
					    	htmlshow += "</tr></table>";
				    	} else {
				    		htmlshow = showvalue;
				    	}
					
					
					}else if(fieldtype.equals("269")){//多提醒


						// 多提醒(2)
						for (int k = 0; tempshowidlist!=null&&k < tempshowidlist.size(); k++) {
							if(k != 0) showvalue += "<div style=\"height:10px;overflow:hidden;width:1px;\"></div>";
							showvalue += "<span keyid=\""+ tempshowidlist.get(k) +"\">"+MeetingBrowser.getRemindNames((String) tempshowidlist.get(k),user.getLanguage())+"</span>";
						}

						htmlshow = "";
				    	if("1".equals(isedit)) { 
				    		htmlshow = "<table style=\"width:100%;\"><tr><td style=\"width:10%;\" onclick=\"javascript:showDialog('/browser/dialog.do','&returnIdField="+fieldformname+"&returnShowField="+fieldformname+"_span&method=listRemindType&browserTypeId=" + fieldtype + "&isMuti=1')\">"+
								"<a href=\"#\" data-rel=\"dialog\" data-transition=\"pop\">"+
								"<div style=\"background-image:url('/images/search_icon_wev8.png');height:30px;width:30px;\"></div></a>"+
								"<input type=\"hidden\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\" ";
			    			if(dataChangeString != null){
			    				htmlshow += dataChangeString;
				    		}else{
						    htmlshow += "onchange=\"try{maindetailfieldchange(this);}catch(e){}\""; 
						}
			    			htmlshow +=	"/></td><td id=\""+fieldformname+"_span\" style=\"width:90%;white-space:normal;\" align=\"left\">"+showvalue+"</td>";
				    		
				    		if("1".equals(ismand)) {
					    	  	htmlshow += "<td><span id=\""+fieldformname+"_ismandspan\" class=\"ismand\">" + getRequiredMark() + "</span>";
					    	  	htmlshow += "<input type=\"hidden\" id=\"ismandfield\" name=\"ismandfield\" value=\""+fieldformname+"\"/></td>";
						    }
					    	
					    	htmlshow += "</tr></table>";
				    	} else {
				    		htmlshow = showvalue;
				    	}
					
					
					}else if(fieldtype.equals("89")){

						// 会议室类型(89)
						for (int k = 0; tempshowidlist!=null&&k < tempshowidlist.size(); k++) {
							if(k != 0) showvalue += "<div style=\"height:10px;overflow:hidden;width:1px;\"></div>";
							showvalue += "<span keyid=\""+ tempshowidlist.get(k) +"\">"+meetingTypeComInfo.getMeetingTypeInfoname((String) tempshowidlist.get(k))+"</span>";
						}

						htmlshow = "";
				    	if("1".equals(isedit)) { 
				    		htmlshow = "<table style=\"width:100%;\"><tr><td style=\"width:10%;\" onclick=\"javascript:showDialog('/browser/dialog.do','&returnIdField="+fieldformname+"&returnShowField="+fieldformname+"_span&method=listMeetingType&customBrowType="+workflowid+"&isMuti=0')\">"+
								"<a href=\"#\" data-rel=\"dialog\" data-transition=\"pop\">"+
								"<div style=\"background-image:url('/images/search_icon_wev8.png');height:30px;width:30px;\"></div></a>"+
								"<input type=\"hidden\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\" ";
			    			if(dataChangeString != null){
			    				htmlshow += dataChangeString;
				    		}else{
						    htmlshow += "onchange=\"try{maindetailfieldchange(this);}catch(e){}\""; 
						}
			    			htmlshow +=	"/></td><td id=\""+fieldformname+"_span\" style=\"width:90%;white-space:normal;\" align=\"left\">"+showvalue+"</td>";
				    		
				    		if("1".equals(ismand)) {
					    	  	htmlshow += "<td><span id=\""+fieldformname+"_ismandspan\" class=\"ismand\">" + getRequiredMark() + "</span>";
					    	  	htmlshow += "<input type=\"hidden\" id=\"ismandfield\" name=\"ismandfield\" value=\""+fieldformname+"\"/></td>";
						    }
					    	
					    	htmlshow += "</tr></table>";
				    	} else {
				    		htmlshow = showvalue;
				    	}
					
					
					}else if (fieldtype.equals("4") || fieldtype.equals("57") || fieldtype.equals("167") || fieldtype.equals("168")) {
						// 部门(4)，多部门(57)，分权单部门(167)，分权多部门(168)
						for (int k = 0; tempshowidlist!=null&&k < tempshowidlist.size(); k++) {
							if(k != 0) showvalue += "<div style=\"height:10px;overflow:hidden;width:1px;\"></div>";
							showvalue += "<span   keyid=\""+ tempshowidlist.get(k) +"\">"+departmentComInfo.getDepartmentname((String) tempshowidlist.get(k))+"</span>";
						}

						htmlshow = "";
				    	if("1".equals(isedit)) {
				    		if(fieldtype.equals("167") || fieldtype.equals("168")) {
				    			String otherParam = fieldid + ",-1," + isbill;
				    			htmlshow = "<table style=\"width:100%;\"><tr><td style=\"width:10%;\" onclick=\"javascript:showDialog('/browser/dialog.do','&returnIdField="+fieldformname+"&returnShowField="+fieldformname+"_span&method=listBrowserData&browserTypeId=" + fieldtype + "&customBrowType=" + otherParam + "&isMuti="+(("167".equalsIgnoreCase(fieldtype))?0:1)+"')\">"+
									"<a href=\"#\" data-rel=\"dialog\" data-transition=\"pop\">"+
									"<div style=\"background-image:url('/images/search_icon_wev8.png');height:30px;width:30px;\"></div></a>"+
									"<input fieldtype=\"browse\" type=\"hidden\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\" ";
				    			//add by liaodong fieldtype=\"browse\"
				    			if(dataChangeString != null){
				    				htmlshow += dataChangeString;
					    		}else{
						    htmlshow += "onchange=\"try{maindetailfieldchange(this);}catch(e){}\""; 
						}
				    			htmlshow +=	"/></td><td id=\""+fieldformname+"_span\" style=\"width:90%;white-space:normal;\" align=\"left\">"+showvalue+"</td>";
				    			
				    			htmlshow += "<td><span id=\"" + fieldformname + "_ismandspan\" " + disIsmand + ">" + getRequiredMark() + "</span>";
				    			htmlshow += "<input type=\"hidden\" id=\"" + fieldformname + "_ismandfield\" name=\"ismandfield\" value=\"" + ismandfieldval + "\"/></td>";
							    	
							    htmlshow += "</tr></table>";
				    		} else {
				    			htmlshow = "<table style=\"width:100%;\"><tr>" +
				    		"<td id=\""+fieldformname+"_span\" style=\"width:90%;white-space:normal;\" align=\"left\">"+showvalue+"</td>"+
				    					"<td style=\"width:10%;\" onclick=\"javascript:showDialog('/browser/dialog.do','&returnIdField="+fieldformname+"&returnShowField="+fieldformname+"_span&method=listDepartment&isMuti="+(("int".equalsIgnoreCase(fielddbtype) || "integer".equalsIgnoreCase(fielddbtype))?0:1)+"')\">"+
									"<a href=\"#\" data-rel=\"dialog\" data-transition=\"pop\" style=\"float:right\">"+
									"<div class=\"cusbrowserimg\" style=\"background-image:url('/images/search_icon_wev8.png');height:30px;width:30px;\"></div></a>"+
									"<input type=\"hidden\" fieldtype=\"browse\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\" ";
				    			   //add by liaodong fieldtype=\"browse\"
				    			if(dataChangeString != null){
				    				htmlshow += dataChangeString;
					    		}else{
								    htmlshow += "onchange=\"try{maindetailfieldchange(this);}catch(e){}\""; 
								}
				    			htmlshow +=	"/></td>";
				    			
				    			htmlshow += "<td><span id=\"" + fieldformname + "_ismandspan\" " + disIsmand + ">" + getRequiredMark() + "</span>";
				    			htmlshow += "<input type=\"hidden\" id=\"" + fieldformname + "_ismandfield\" name=\"ismandfield\" value=\"" + ismandfieldval + "\"/></td>";
				    			
							    htmlshow += "</tr></table>";
				    		}
				    	} else {
				    		htmlshow = showvalue;
				    	}
					} else if (fieldtype.equals("9") || fieldtype.equals("37")) {
						// 文档(9)，多文档(37)
						for (int k = 0; tempshowidlist!=null&&k < tempshowidlist.size(); k++) {
							if (k!=0) {
								showvalue += "<br/><br/>";
							}
							String docid = (String) tempshowidlist.get(k);
							showvalue += "<span   style='cursor:hand;color:blue' onclick='javascript:toDocument(" + docid + ");' keyid='" + docid + "'>" + docComInfo.getDocname(docid) + "</span>";
						}
						
						htmlshow = "";
				    	if("1".equals(isedit)) {
				    		htmlshow = "<table style=\"width:100%;\"><tr><td style=\"width:10%;\" onclick=\"javascript:showDialog('/browser/dialog.do','&returnIdField="+fieldformname+"&returnShowField="+fieldformname+"_span&method=listDocument&isMuti="+(("int".equalsIgnoreCase(fielddbtype) || "integer".equalsIgnoreCase(fielddbtype))?0:1)+"')\">"+
								"<a href=\"#\" data-rel=\"dialog\" data-transition=\"pop\">"+
								"<div style=\"background-image:url('/images/search_icon_wev8.png');height:30px;width:30px;\"></div></a>"+
								"<input type=\"hidden\" fieldtype=\"browse\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\" ";
				    		    //add by liaodong fieldtype=\"browse\"
			    			if(dataChangeString != null){
			    				htmlshow += dataChangeString;
				    		}else{
						    htmlshow += "onchange=\"try{maindetailfieldchange(this);}catch(e){}\""; 
						}
			    			htmlshow +=	"/></td><td id=\""+fieldformname+"_span\" style=\"width:90%;white-space:normal;\" align=\"left\">"+showvalue+"</td>";
				    		
			    			htmlshow += "<td><span id=\"" + fieldformname + "_ismandspan\" " + disIsmand + ">" + getRequiredMark() + "</span>";
			    			htmlshow += "<input type=\"hidden\" id=\"" + fieldformname + "_ismandfield\" name=\"ismandfield\" value=\"" + ismandfieldval + "\"/></td>";
			    			
				    		htmlshow += "</tr></table>";
				    	} else {
				    		htmlshow = showvalue;
				    	}
				    	
					} else if (fieldtype.equals("23")) {
						// 资产
						for (int k = 0; tempshowidlist!=null&&k < tempshowidlist.size(); k++) {
							if(k != 0) showvalue += "<div style=\"height:10px;overflow:hidden;width:1px;\"></div>";
							showvalue += "<span  keyid=\""+ tempshowidlist.get(k) +"\">"+capitalComInfo.getCapitalname((String) tempshowidlist.get(k))+"</span>";
						}

						htmlshow = "";
				    	if("1".equals(isedit)) {
				    		htmlshow = "<table style=\"width:100%;\"><tr><td style=\"width:10%;\" onclick=\"javascript:showDialog('/browser/dialog.do','&returnIdField="+fieldformname+"&returnShowField="+fieldformname+"_span&method=listCpt&isMuti="+(("int".equalsIgnoreCase(fielddbtype) || "integer".equalsIgnoreCase(fielddbtype))?0:1)+"')\">"+
								"<a href=\"#\" data-rel=\"dialog\" data-transition=\"pop\">"+
								"<div style=\"background-image:url('/images/search_icon_wev8.png');height:30px;width:30px;\"></div></a>"+
							    "<input fieldtype=\"browse\" type=\"hidden\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\" ";
				    		   //add by liaodong fieldtype=\"browse\"
			    			if(dataChangeString != null){
			    				htmlshow += dataChangeString;
				    		}else{
						    htmlshow += "onchange=\"try{maindetailfieldchange(this);}catch(e){}\""; 
						}
			    			htmlshow +=	"/></td><td id=\""+fieldformname+"_span\" style=\"width:90%;white-space:normal;\" align=\"left\">"+showvalue+"</td>";
					    	
			    			htmlshow += "<td><span id=\"" + fieldformname + "_ismandspan\" " + disIsmand + ">" + getRequiredMark() + "</span>";
			    			htmlshow += "<input type=\"hidden\" id=\"" + fieldformname + "_ismandfield\" name=\"ismandfield\" value=\"" + ismandfieldval + "\"/></td>";
			    			
					    	htmlshow += "</tr></table>";
				    	} else {
				    		htmlshow = showvalue;
				    	}
				    	
					} else if (fieldtype.equals("16") || fieldtype.equals("152") || fieldtype.equals("171")) {
						// 请求(16) 多请求(162) 归档请求(171)
						for (int k = 0; tempshowidlist!=null&&k < tempshowidlist.size(); k++) {
							if(k != 0) showvalue += "<div style=\"height:10px;overflow:hidden;width:1px;\"></div>";
							//showvalue += "<span   style=\"cursor:hand;color:blue;\" onclick=\"javascript:toRequest("+tempshowidlist.get(k)+");\" keyid=\""+tempshowidlist.get(k)+"\">"+workflowRequestComInfo.getRequestName((String) tempshowidlist.get(k))+"</span>";
							showvalue += "<span   style=\"cursor:hand;color:blue;\" keyid=\""+tempshowidlist.get(k)+"\">"+workflowRequestComInfo.getRequestName((String) tempshowidlist.get(k))+"</span>";
						}

						htmlshow = "";
				    	if("1".equals(isedit)) {
				    		if(fieldtype.equals("171")) {
				    			htmlshow = showvalue;
				    			
				    			if (showvalue != null && !"".equals(showvalue.trim())) {
				    				htmlshow += FieldOnlyShow;
					    		} else {
					    			htmlshow += FieldnonSupport;
					    		}
				    		} else {
				    			htmlshow = "<table style=\"width:100%;\"><tr><td id=\""+fieldformname+"_span\" style=\"width:90%;white-space:normal;\" align=\"left\">"+showvalue+"</td>";
				    			
				    			htmlshow += "<td style=\"width:10%;\" onclick=\"javascript:showDialog('/browser/dialog.do','&returnIdField="+fieldformname+"&returnShowField="+fieldformname+"_span&method=listWorkflowRequest&isMuti="+(("int".equalsIgnoreCase(fielddbtype) || "integer".equalsIgnoreCase(fielddbtype))?0:1)+"')\">"+
									"<a href=\"#\" data-rel=\"dialog\" data-transition=\"pop\">"+
									"<div style=\"background-image:url('/images/search_icon_wev8.png');height:23px;width:30px;background-size: 30px 30px;\"></div></a>"+
									"<input fieldtype=\"browse\" type=\"hidden\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\" ";
				    			    //add by liaodong fieldtype=\"browse\"
				    			if(dataChangeString != null){
				    				htmlshow += dataChangeString;
					    		}else{
					    			htmlshow += "onchange=\"try{maindetailfieldchange(this);}catch(e){}\""; 
					    		}
				    			htmlshow +=	"/></td>";
							    
				    			htmlshow += "<td><span id=\"" + fieldformname + "_ismandspan\" " + disIsmand + ">" + getRequiredMark() + "</span>";
				    			htmlshow += "<input type=\"hidden\" id=\"" + fieldformname + "_ismandfield\" name=\"ismandfield\" value=\"" + ismandfieldval + "\"/></td>";
				    			
							    htmlshow += "</tr></table>";
				    		}
				    	} else {
				    		htmlshow = showvalue;
				    	}
				    	
					} else if (fieldtype.equals("164") || fieldtype.equals("194") || fieldtype.equals("169") || fieldtype.equals("170")) {
						// 分部(164) 分权单分部(169) 分权多分部(170)
						for (int k = 0; tempshowidlist!=null&&k < tempshowidlist.size(); k++) {
							if(k != 0) showvalue += "<div style=\"height:10px;overflow:hidden;width:1px;\"></div>";
							showvalue += "<span  keyid=\""+ tempshowidlist.get(k) +"\">"+subCompanyComInfo.getSubCompanyname((String) tempshowidlist.get(k))+"</span>";
						}

						htmlshow = "";
				    	if("1".equals(isedit)) {
				    		if(fieldtype.equals("169") || fieldtype.equals("170")) {
				    			String otherParam = fieldid + ",-1," + isbill;
				    			htmlshow = "<table style=\"width:100%;\"><tr><td style=\"width:10%;\" onclick=\"javascript:showDialog('/browser/dialog.do','&returnIdField="+fieldformname+"&returnShowField="+fieldformname+"_span&method=listBrowserData&browserTypeId=" + fieldtype + "&customBrowType=" + otherParam + "&isMuti="+(("169".equalsIgnoreCase(fieldtype))?0:1)+"')\">"+
									"<a href=\"#\" data-rel=\"dialog\" data-transition=\"pop\">"+
									"<div style=\"background-image:url('/images/search_icon_wev8.png');height:30px;width:30px;\"></div></a>"+
									"<input fieldtype=\"browse\" type=\"hidden\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\" ";
				    			    //add by liaodong fieldtype=\"browse\"
				    			if(dataChangeString != null){
				    				htmlshow += dataChangeString;
					    		}else{
						    htmlshow += "onchange=\"try{maindetailfieldchange(this);}catch(e){}\""; 
						}
				    			htmlshow +=	"/></td><td id=\""+fieldformname+"_span\" style=\"width:90%;white-space:normal;\" align=\"left\">"+showvalue+"</td>";
							    
				    			htmlshow += "<td><span id=\"" + fieldformname + "_ismandspan\" " + disIsmand + ">" + getRequiredMark() + "</span>";
				    			htmlshow += "<input type=\"hidden\" id=\"" + fieldformname + "_ismandfield\" name=\"ismandfield\" value=\"" + ismandfieldval + "\"/></td>";
				    			
							    htmlshow += "</tr></table>";
				    		} else {
				    			htmlshow = "<table style=\"width:100%;\"><tr><td style=\"width:10%;\" onclick=\"javascript:showDialog('/browser/dialog.do','&returnIdField="+fieldformname+"&returnShowField="+fieldformname+"_span&method=listSubCompany&isMuti="+(("int".equalsIgnoreCase(fielddbtype) || "integer".equalsIgnoreCase(fielddbtype))?0:1)+"')\">"+
									"<a href=\"#\" data-rel=\"dialog\" data-transition=\"pop\">"+
									"<div style=\"background-image:url('/images/search_icon_wev8.png');height:30px;width:30px;\"></div></a>"+
									"<input fieldtype=\"browse\" type=\"hidden\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\" ";
				    			    //add by liaodong fieldtype=\"browse\"
				    			if(dataChangeString != null){
				    				htmlshow += dataChangeString;
					    		}else{
						    htmlshow += "onchange=\"try{maindetailfieldchange(this);}catch(e){}\""; 
						}
				    			htmlshow +=	"/></td><td id=\""+fieldformname+"_span\" style=\"width:90%;white-space:normal;\" align=\"left\">"+showvalue+"</td>";
							    
				    			htmlshow += "<td><span id=\"" + fieldformname + "_ismandspan\" " + disIsmand + ">" + getRequiredMark() + "</span>";
				    			htmlshow += "<input type=\"hidden\" id=\"" + fieldformname + "_ismandfield\" name=\"ismandfield\" value=\"" + ismandfieldval + "\"/></td>";
				    			
							    htmlshow += "</tr></table>";
				    		}
				    	} else {
				    		htmlshow = showvalue;
				    	}
					} else if (fieldtype.equals("161") || fieldtype.equals("162")) {
						String sql="";
						String linkhref = "";
						String showtree = "";
						try {
							// 自定义单选浏览框(161)，自定义多选浏览框(162)
							Browser browser = (Browser) StaticObj.getServiceByFullname(fielddbtype, Browser.class);
							showtree = Util.null2String(browser.getShowtree());
							isshowtree = "1".equals(showtree);
							//linkhref = "";//browser.getHref();
							linkhref =Util.null2String(browser.getHref());
							if(!"".equals(linkhref) && linkhref.indexOf("?") != -1){
								linkhref = "/mobilemode/formbasebrowserview.jsp"+linkhref.substring(linkhref.indexOf("?"));
							}
							sql = browser.getSearch().toLowerCase(); //sql语句
							for (int k = 0; tempshowidlist!=null&&k < tempshowidlist.size(); k++) {
								try {
									BrowserBean bb = browser.searchById(requestid+"^~^"+(String) tempshowidlist.get(k)+(groupid!=0?"^~^"+detailRecordId:""));
									String desc = Util.null2String(bb.getDescription());
									String name = Util.null2String(bb.getName());
									String href = Util.null2String(bb.getHref());
									if(linkhref.equals("")){
										  //showvalue += "<a title='" + desc + "'>" + name + "</a>&nbsp;";
										  showvalue += "<span  keyid=\""+ fieldvalue +"\" style=\"margin-right:1px;\">"+name+"</span>";
									}else if(!"".equals(linkhref) && linkhref.indexOf("?") == -1){
									      //showvalue += "<a title='" + desc + "' href=\"javascript:void(0)\" onclick=\"javascript:openbrowserurl('"+linkhref+"');\" >" + name + "</a>&nbsp;";
										  showvalue += "<span  keyid=\""+ fieldvalue +"\" style=\"margin-right:1px;\">"+name+"</span>";
									}else {
										  //showvalue += "<a title='" + desc + "' href=\"javascript:void(0)\" onclick=\"javascript:openbrowserurl('"+linkhref+((String) tempshowidlist.get(k))+"');\" >" + name + "</a>&nbsp;";
										  showvalue += "<span  keyid=\""+ fieldvalue +"\" style=\"margin-right:1px;\">"+name+"</span>";
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						} catch(Exception e) {
							log.error("Catch a exception.", e);
						}
						
				    	htmlshow = "";
				    	if ("1".equals(isedit)) {
				    		
				    		if(!"1".equals(showtree)){
					    		String joinParams =getJoinParams(sql,isbill,formID,fieldformname);
								if(linkhref.indexOf("&")!=-1){
									linkhref = linkhref.replace("&","%26");
								}
								//调整浏览框位置根据TD
								htmlshow = "<table style=\"width:100%;\" fieldhtmltype=\"" + fieldhtmltype + "\" fieldtype=\"" + fieldtype + "\"><tr>" +
				    			"<td id=\""+fieldformname+"_span\" style=\"width:90%;white-space:normal;\" align=\"left\">"+showvalue+"</td>"+
				    			"<td  style=\"width:10%;\" onclick=\"javascript:showDialog('/browser/dialog.do','&returnIdField="+fieldformname+"&returnShowField="+fieldformname+"_span&method=listBrowserData&browserTypeId=" + fieldtype + "&customBrowType=" + fielddbtype + "&isMuti="+(("161".equalsIgnoreCase(fieldtype))? 0 : 1)+(!"".equals(joinParams)?"&joinFieldParams="+joinParams+"":"")+"&linkhref="+linkhref+"')\">"+
								"<a href=\"#\" data-rel=\"dialog\" data-transition=\"pop\" style=\"float:right;\">"+
								"<div class=\"cusbrowserimg\" style=\"background-image:url('/images/search_icon_wev8.png');height:30px;width:30px;\"></div></a>"+
								"<input fieldtype=\"browse\" type=\"hidden\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\" ";
				    		     //add by liaodong fieldtype=\"browse\"
					    		if(dataChangeString != null){
				    				htmlshow += dataChangeString;
					    		}else{
							    htmlshow += "onchange=\"try{maindetailfieldchange(this);}catch(e){}\""; 
					    		}
				    			htmlshow +=	"/></td>";
							
				    			htmlshow += "<td><span id=\"" + fieldformname + "_ismandspan\" " + disIsmand + ">" + getRequiredMark() + "</span>";
				    			htmlshow += "<input type=\"hidden\" id=\"" + fieldformname + "_ismandfield\" name=\"ismandfield\" value=\"" + ismandfieldval + "\"/></td>";
							    	
							    htmlshow += "</tr></table>";
				    		}else{
				    			String joinParams =getJoinParams(sql,isbill,formID,fieldformname);
								if(linkhref.indexOf("&")!=-1){
									linkhref = linkhref.replace("&","%26");
								}
					    		htmlshow = "<table style=\"width:100%;\" fieldhtmltype=\"" + fieldhtmltype + "\" fieldtype=\"" + fieldtype + "\" showtree=\"" + showtree + "\"><tr><td  style=\"width:10%;\" onclick=\"javascript:void(0);\">"+
									"<a href=\"#\" data-rel=\"dialog\" data-transition=\"pop\">"+
									"<div style=\"height:1px;width:1px;\"></div></a>"+
									"<input fieldtype=\"browse\" type=\"hidden\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\" ";
					    		     //add by liaodong fieldtype=\"browse\"
					    		if(dataChangeString != null){
				    				htmlshow += dataChangeString;
					    		}else{
							    htmlshow += "onchange=\"try{maindetailfieldchange(this);}catch(e){}\""; 
					    		}
				    			htmlshow +=	"/></td><td id=\""+fieldformname+"_span\" style=\"width:90%;white-space:normal;\" align=\"left\">"+showvalue+"</td>";
							    htmlshow += "</tr></table>";
				    		}
				    	} else {
				    		htmlshow = showvalue;
				    	}
				    	
					} else if(fieldtype.equals("256") || fieldtype.equals("257") ){
						if("NULL".equals(fieldvalue)){
							fieldvalue="";
						}
						CustomTreeUtil customTreeUtil = new CustomTreeUtil();
                        //String name = customTreeUtil.getTreeFieldShowName(fieldvalue, fielddbtype, "onlyname");
                        String name = customTreeUtil.getTreeFieldMobileShowName(fieldvalue, fielddbtype);
                        //fieldname += Util.StringReplace(name,"，",",") + ",";
                        showvalue += name;
                        if ("1".equals(isedit)) {
                        	htmlshow = "<table style=\"width:100%;\" fieldhtmltype=\"" + fieldhtmltype + "\" fieldtype=\"" + fieldtype + "\"><tr><td style=\"width:10%;\">";
                        	htmlshow += name;
                        	htmlshow += "</td></tr></table>";
                        }else{
                        	htmlshow = showvalue;
                        }
					} else if(fieldtype.equals("226") || fieldtype.equals("227") ){
						//zzl---sap集成浏览按钮
						if("NULL".equals(fieldvalue)){
							fieldvalue="";
						}
						showvalue += "<a title='" + fieldvalue + "'>" + fieldvalue + "</a>";
						htmlshow = "";
				    	if ("1".equals(isedit)) {
			    			htmlshow = "<table style=\"width:100%;\"><tr><td style=\"width:10%;\">";
			    			if("".equals(fieldvalue)){
			    				htmlshow+=FieldnonSupport;
			    			}else{
			    				htmlshow+=fieldvalue;
			    				htmlshow+="<br>";
			    				htmlshow+=FieldOnlyShow;
			    			}
					    	htmlshow += "</td></tr></table>";
				    	} else {
				    		htmlshow = showvalue;
				    	}
					}  else if(fieldtype.equals("141")){
						//人力资源条件
						if (fieldvalue !=null && !"".equals(fieldvalue)) {
							String html = resourceConditionManager.getFormShowName(fieldvalue,languageid);
							html = Jsoup.parse(html).text();
							showvalue = "<span   keyid=\""+ fieldvalue +"\">"+html+"</span>";
						}
						
						htmlshow = showvalue;
				    	if("1".equals(isedit)) {
				    		if (showvalue != null && !"".equals(showvalue.trim())) {
				    			htmlshow += FieldOnlyShow;
				    		} else {
				    			htmlshow += FieldnonSupport;
				    		}
				    	}
					} else  if(fieldtype.equals("224")){//sap显示
					    showvalue = fieldvalue;
					    htmlshow = "<div style=\"width:100%;\">"+showvalue +"</div>" ;
						
						if ("1".equals(isedit)) {
				    		if (showvalue != null && !"".equals(showvalue.trim())) {
				    			htmlshow += FieldOnlyShow;
				    		} else {
				    			htmlshow += FieldnonSupport;
				    		}
				    	}
					} else if(fieldtype.equals("34")){ //请假类型
						if (StringUtil.isNotNull(fieldvalue)) {
							Map<String, Comparable> map = new HashMap<String, Comparable>();
							map.put("field004", fieldvalue);
							weaver.hrm.attendance.domain.HrmLeaveTypeColor typeBean = leaveTypeManager.get(map);
							showvalue = typeBean == null ? "" : typeBean.getField001();
						}

						htmlshow = "";
				    	if("1".equals(isedit)) { 
				    		/*htmlshow = "<table style=\"width:100%;\"><tr><td style=\"width:10%;\" onclick=\"javascript:showDialog('/browser/dialog.do','&returnIdField="+fieldformname+"&returnShowField="+fieldformname+"_span&method=listLeaveType&isMuti=0')\">"+
								"<a href=\"#\" data-rel=\"dialog\" data-transition=\"pop\">"+
								"<div style=\"background-image:url('/images/search_icon_wev8.png');height:30px;width:30px;\"></div></a>"+
								"<input fieldtype=\"browse\" type=\"text\" style=\"display:none\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\" ";
			    			if(dataChangeString != null){
			    				htmlshow += dataChangeString;
				    		}else{
							    htmlshow += "onchange=\"try{maindetailfieldchange(this);showVacationInfo();}catch(e){}\""; 
							}
			    			htmlshow +=	"/></td><td id=\""+fieldformname+"_span\" style=\"width:90%;white-space:normal;\" align=\"left\">"+showvalue+"</td>";
			    			htmlshow += "<td><span id=\"" + fieldformname + "_ismandspan\" " + disIsmand + ">" + getRequiredMark() + "</span>";
			    			htmlshow += "<input type=\"hidden\" id=\"" + fieldformname + "_ismandfield\" name=\"ismandfield\" value=\"" + ismandfieldval + "\"/></td>";
					    	htmlshow += "</tr></table>";*/
				    		
				    		htmlshow = "<table style=\"width:100%;\"><tr><td style=\"width:99%;white-space:normal;\" align=\"left\">";
				    		htmlshow += "<input class=\"cus_select\" attrname=\""+fieldformname+"\" type=\"text\" readonly=\"readonly\" placeholder=\"请选择\">";
				    		htmlshow += "<input type=\"text\" style=\"display:none\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\" ";
				    		htmlshow += "</td>";
				    		//"<td id=\""+fieldformname+"_span\" style=\"width:90%;white-space:normal;\" align=\"left\">"+showvalue+"</td>";
			    			htmlshow += "<td><span id=\"" + fieldformname + "_ismandspan\" " + disIsmand + ">" + getRequiredMark() + "</span>";
			    			htmlshow += "<input type=\"hidden\" id=\"" + fieldformname + "_ismandfield\" name=\"ismandfield\" value=\"" + ismandfieldval + "\"/></td>";
					    	htmlshow += "</tr></table>";
				    		
				    	} else {
				    		htmlshow = showvalue;
				    	}
					}  else if(fieldtype.equals("28")){
						//会议
						// 浏览框对应的表,比如人力资源表

						String tablename = browserComInfo.getBrowsertablename(fieldtype);
						// 浏览框对应的表名称字段

						String columname = browserComInfo.getBrowsercolumname(fieldtype);
						// 浏览框对应的表值字段

						String keycolumname = browserComInfo.getBrowserkeycolumname(fieldtype);
						
						String sql = "";
						showvalue = "";
						//对字段值、列名、表名等作出验证，如果都为空字符串则不作任何操作。

						if (!"".equals(fieldvalue) && !"".equals(keycolumname) && !"".equals(columname) && !"".equals(tablename)) {
							if (fieldvalue.indexOf(",") != -1) {
								sql = "select " + keycolumname + "," + columname + " from " + tablename + " where " + keycolumname + " in( " + fieldvalue + ")";
							} else {
								sql = "select " + keycolumname + "," + columname + " from " + tablename + " where " + keycolumname + "=" + fieldvalue;
							}
							rs.executeSql(sql);
							int k = 0;
							while (rs.next()) {
								if (k!=0) {
									showvalue += "<br/><br/>";
								}
								showvalue += "<span style='cursor:hand;color:blue' onclick='javascript:toMeeting(" + fieldvalue + ");' keyid='" + fieldvalue + "'>" + Util.toScreen(rs.getString(2), user.getLanguage()) + "</span>";
							}
						}
						htmlshow = "<div style=\"width:100%;\">" + showvalue + "</div>";
				    	
						if("1".equals(isedit)) {
							if (showvalue != null && !"".equals(showvalue.trim())) {
								htmlshow += FieldOnlyShow;
							} else {
								htmlshow += FieldnonSupport;
							}
						}
					}  else if(fieldtype.equals("12")){
						//币种
						if (fieldvalue !=null && !"".equals(fieldvalue)) {
							//因币种属于单选按钮，故不需迭代替换值。

							CurrencyComInfo currencyComInfo = new CurrencyComInfo();
							String currencyName = currencyComInfo.getCurrencyname(fieldvalue);
							showvalue += "<a title='" + currencyName + "'>" + currencyName + "</a>&nbsp";
						}
						
				    	htmlshow = "";
				    	if ("1".equals(isedit)) {
				    		htmlshow = "<table style=\"width:100%;\"><tr><td style=\"width:10%;\" onclick=\"javascript:showDialog('/browser/dialog.do','&returnIdField="+fieldformname+"&returnShowField="+fieldformname+"_span&method=listBrowserData&browserTypeId=" + fieldtype + "&customBrowType=" + fielddbtype + "&isMuti=0')\">"+
								"<a href=\"#\" data-rel=\"dialog\" data-transition=\"pop\">"+
								"<div style=\"background-image:url('/images/search_icon_wev8.png');height:30px;width:30px;\"></div></a>"+
								"<input fieldtype=\"browse\" type=\"hidden\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\" ";
							    //add by liaodong fieldtype=\"browse\"
				    		if(dataChangeString != null){
			    				htmlshow += dataChangeString;
				    		}else{
						    htmlshow += "onchange=\"try{maindetailfieldchange(this);}catch(e){}\""; 
						}
			    			htmlshow +=	"/></td><td id=\""+fieldformname+"_span\" style=\"width:90%;white-space:normal;\" align=\"left\">"+showvalue+"</td>";
						
			    			htmlshow += "<td><span id=\"" + fieldformname + "_ismandspan\" " + disIsmand + ">" + getRequiredMark() + "</span>";
			    			htmlshow += "<input type=\"hidden\" id=\"" + fieldformname + "_ismandfield\" name=\"ismandfield\" value=\"" + ismandfieldval + "\"/></td>";
						    	
						    htmlshow += "</tr></table>";
				    	} else {
				    		htmlshow = showvalue;
				    	}
	                } else if(fieldlable.equals(SystemEnv.getHtmlLabelName(HrmScheduleShiftsSet.SCHEDULE_SHIFTS_LABEL, languageid))) {// 鐝娴忚鎸夐挳

                        if (StringUtil.isNotNull(fieldvalue)) {
                            Map<String, Comparable> map = new HashMap<String, Comparable>();
                            map.put("realId", fieldvalue);
                            HrmScheduleShiftsSet bean = scheduleManager.get(map);
                            showvalue = bean == null ? "" : bean.getField001();
                        }
                        htmlshow = scheduleManager.getMobileBrowserShowContent("listScheduleShifts", isedit, showvalue, fieldformname, fieldname, fieldvalue, dataChangeString, disIsmand, ismandfieldval);
                    
	                }else {
	                	// 浏览框对应的表,比如人力资源表

						String tablename = browserComInfo.getBrowsertablename(fieldtype);
						// 浏览框对应的表名称字段

						String columname = browserComInfo.getBrowsercolumname(fieldtype);
						// 浏览框对应的表值字段

						String keycolumname = browserComInfo.getBrowserkeycolumname(fieldtype);
						
						String sql = "";
						showvalue = "";
						//对字段值、列名、表名等作出验证，如果都为空字符串则不作任何操作。

						if (!"".equals(fieldvalue) && !"".equals(keycolumname) && !"".equals(columname) && !"".equals(tablename)) {
							if (fieldvalue.indexOf(",") != -1) {
								sql = "select " + keycolumname + "," + columname + " from " + tablename + " where " + keycolumname + " in( " + fieldvalue + ")";
							} else {
								sql = "select " + keycolumname + "," + columname + " from " + tablename + " where " + keycolumname + "=" + fieldvalue;
							}
							rs.executeSql(sql);
							while (rs.next()) {
								showvalue += Util.toScreen(rs.getString(2), user.getLanguage()) + " ";
							}
						}
				    	htmlshow = "<div style=\"width:100%;\">" + showvalue + "</div>";
						
				    	if ("1".equals(isedit)) {
				    		if (showvalue != null && !"".equals(showvalue.trim())) {
				    			htmlshow += FieldOnlyShow;
				    		} else {
				    			htmlshow += FieldnonSupport;
				    		}
				    	}
					}
					
					if(!"1".equals(isedit)) {
						showvalue = "<span id=\"" +  fieldformname + "_span\" name=\"" + fieldformname + "_span\" >" + showvalue + "</span>";
						//showvalue += "<input type='hidden' name='" + fieldformname + "' id='"+ fieldformname +"' _fieldhtmlType='"+ fieldhtmltype + "' value='"+ fieldvalue +"' nameBak='"+ fieldname + "' >";
						htmlshow = showvalue;
					}
				}
			} else if (fieldhtmltype.equals("4")) {// check框				
	    		showvalue = "<table style=\"width:100%;\"><tr><td style=\"width:99%;white-space:normal;\" align=\"left\">";
	    		showvalue += "<input type=\"checkbox\" id=\""+fieldformname+"\" " + (fieldvalue!=null&&"1".equals(fieldvalue)?"checked":"") +" disabled/>";
	    		showvalue += "<label for=\"" + fieldname + "\">&nbsp;</label>";
	    		showvalue += "<input  type=\"hidden\" name=\"" + fieldformname + "\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\"/></td>";
	    		//由于checkBox框无需控制必填，故无需添加必填的提示图片。

	    		showvalue += "</tr></table>";
	    		
		    	if("1".equals(isedit)){
		    		htmlshow = "";
		    		htmlshow = "<table style=\"width:100%;\"><tr><td style=\"width:99%;white-space:normal;\" align=\"left\">";
		    		htmlshow += "<input type=\"checkbox\" id=\""+fieldformname+"\" name=\"" + fieldname + "\" " + (fieldvalue!=null&&"1".equals(fieldvalue)?"checked":"");
		    		htmlshow += " onchange=\"jQuery('input[name="+fieldformname+"]').val(this.checked?1:0); ";
	    			if(dataLinkageStr != null){
		    			  htmlshow += dataLinkageStr;
		    		}	    		
		    		htmlshow += "try{maindetailfieldchange(this);}catch(e){}\"/>";
		    		htmlshow += "<label for=\"" + fieldname + "\">&nbsp;</label>";
		    		htmlshow += "<input type=\"hidden\" name=\"" + fieldformname + "\" nameBak=\""+fieldname+"\" value=\""+(fieldvalue==null||"".equals(fieldvalue)?"0":fieldvalue)+"\"/></td>";
		    		//由于checkBox框无需控制必填，故无需添加必填的提示图片。

		    		htmlshow += "</tr></table>";
		    	} else {
		    		htmlshow = showvalue;
		    	}
			} else if (fieldhtmltype.equals("5")) {// 选择框 select
				boolean isdetail = fieldformname.indexOf("_")!=-1?true:false;
				String detailVar = "";
				if(isdetail){
					detailVar = fieldformname.split("_")[1];
				}
				int childfieldid_tmp = 0;
				if("0".equals(isbill)){
					if(isdetail){
						 rs.execute("select childfieldid from workflow_formdictdetail where id="+fieldid);
					}else{
						 rs.execute("select childfieldid from workflow_formdict where id="+fieldid);
					}
	        	}else{
	        		rs.execute("select childfieldid from workflow_billfield where id="+fieldid);
	        	}
				if(rs.next()){
		       		childfieldid_tmp = Util.getIntValue(rs.getString("childfieldid"), 0);
	        	}
				if (selfieldsadd != null && selfieldsadd.indexOf(String.valueOf(fieldid))  != -1) {
					if (dataChangeString != null && !"".equals(dataChangeString)) {
						dataChangeString = dataChangeString.substring(0, dataChangeString.length() - 1);
						if(isdetail){
							dataChangeString += ";changeshowattr('"+fieldid+"_1',this.value,"+detailVar+","+workflowid+","+nodeid+"); ";
						}else{
							dataChangeString += ";changeshowattr('"+fieldid+"_0',this.value,-1,"+workflowid+","+nodeid+"); ";
						}
						if(childfieldid_tmp != 0){//如果先出现子字段，则要把子字段下拉选项清空
							if(isdetail){
								dataChangeString += "changeChildFieldDetail(this, "+fieldid+", "+childfieldid_tmp+","+detailVar+");";
							}else{
								dataChangeString += "changeChildField(this, "+fieldid+", "+childfieldid_tmp+");";
							}
						}
						dataChangeString += "\"";
					} else {
						if(isdetail){
							dataChangeString = " onChange=\"changeshowattr('"+fieldid+"_1',this.value,"+detailVar+","+workflowid+","+nodeid+");";
						}else{
							dataChangeString = " onChange=\"changeshowattr('"+fieldid+"_0',this.value,-1,"+workflowid+","+nodeid+");";
						}
						if(childfieldid_tmp != 0){//如果先出现子字段，则要把子字段下拉选项清空
							if(isdetail){
								dataChangeString += "changeChildFieldDetail(this, "+fieldid+", "+childfieldid_tmp+","+detailVar+");";
							}else{
								dataChangeString += "changeChildField(this, "+fieldid+", "+childfieldid_tmp+");";
							}
						}
						dataChangeString += "try{maindetailfieldchange(this);}catch(e){}\"";
					}
					if(isdetail){
						dataChangeString += " onblur=\"changeshowattr('"+fieldid+"_1',this.value,"+detailVar+","+workflowid+","+nodeid+");\" ";
					}else{
						dataChangeString += " onblur=\"changeshowattr('"+fieldid+"_0',this.value,-1,"+workflowid+","+nodeid+");\" ";
					}
				}

					
				rs.executeProc("workflow_SelectItemSelectByid", "" + fieldid + flag + isbill);
				List lstIsDefault = new ArrayList();
				while (rs.next()) {
					String tmpselectvalue = Util.null2String(rs.getString("selectvalue"));
					String tmpselectname = Util.toScreen(rs.getString("selectname"), user.getLanguage());
					String isCancel = Util.null2String(rs.getString("cancel"));
					
					//如果(非新建请求)此select框在数据库中可能有值

					if (fieldvalue.equals(tmpselectvalue)) {
					  showvalue = tmpselectname;
					}
          
					//如果是封存,则不作任何处理

					if("1".equals(isCancel)){
					  continue;
					}
					selectnamelist.add(tmpselectname);
					selectvaluelist.add(tmpselectvalue);
					
					//如果此select框数据库中不存在值, 则获取默认选项信息
					if(fieldvalue == null || "".equals(fieldvalue)){
					  String isDefault = Util.null2String(rs.getString("isdefault"));
					  lstIsDefault.add(isDefault);
					}
				}
				
				String sltval = fieldvalue;
				
		    	htmlshow = "";
		    	String changeChildField = "";
		    	if("1".equals(isedit)){
		    		htmlshow = 
			    	"<table style=\"width:100%;\"><tr>" +
			    	"<td style=\"width:99%;white-space:normal;\" align=\"left\">"+
					"<select style=\"display:none;\" _detailRecordId=\""+detailRecordId+"\" class='scroller_select' name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\"";
	    			if(dataChangeString != null){
	    				htmlshow += dataChangeString;
	    				changeChildField += dataChangeString;
		    		}else{
					    htmlshow += " onchange=\""; 
					    changeChildField += " onchange=\""; 
					    if(childfieldid_tmp != 0){//如果先出现子字段，则要把子字段下拉选项清空
							if(isdetail){
								htmlshow += "changeChildFieldDetail(this, "+fieldid+", "+childfieldid_tmp+","+detailVar+"); ";
								changeChildField += "changeChildFieldDetail(this, "+fieldid+", "+childfieldid_tmp+","+detailVar+"); ";
							}else{
								htmlshow += "changeChildField(this, "+fieldid+", "+childfieldid_tmp+");";
								changeChildField += "changeChildFieldDetail(this, "+fieldid+", "+childfieldid_tmp+","+detailVar+"); ";
							}
						}
					    htmlshow += "try{maindetailfieldchange(this);}catch(e){}\"";
					    changeChildField += "try{maindetailfieldchange(this);}catch(e){}\"";
					}
		    		htmlshow +=">"+"<option value=\"\" "+(fieldvalue==null||"".equals(fieldvalue)?"selected":"")+"></option>";
		    		boolean isdefaultselect = false;
		    		for(int i=0;i<selectnamelist.size();i++){
		    			String selectnamestr = (String)selectnamelist.get(i);
		    			String selectvaluestr = (String)selectvaluelist.get(i);
		    			
		    			if(fieldvalue == null || "".equals(fieldvalue)){
		    			  String isDefault = (String)lstIsDefault.get(i);
		    			  htmlshow += "<option value=\""+selectvaluestr+"\" "+("y".equals(isDefault)?"selected":"")+">"+selectnamestr+"</option>";
						  if("y".equals(isDefault)){
                             showvalue = selectnamestr;
                             sltval = selectvaluestr;
                             isdefaultselect = true;
						  }
		    			} else {
		    			  htmlshow += "<option value=\""+selectvaluestr+"\" "+(fieldvalue!=null&&selectvaluestr.equals(fieldvalue)?"selected":"")+">"+selectnamestr+"</option>";
		    			}
		    		}
		    		if(childfieldid_tmp != 0){//如果先出现子字段，则要把子字段下拉选项清空
		    		   if(isdetail){
		    			String childfieldformname = fieldformname.replace(""+fieldid,""+childfieldid_tmp);
		    		    htmlshow += "</select><input type=\"hidden\" id=\""+fieldformname+"child\" name=\""+fieldformname+"child\" value=\""+childfieldformname+"\" />";
		    		  }else{
		    			htmlshow += "</select>";
		    		  }
		    		}else{
		    		  htmlshow += "</select>";
		    		}
		    		htmlshow += "<input class=\"cus_select_pub\" attrname=\""+fieldformname+"\" type=\"text\" "+changeChildField+" readonly=\"readonly\" placeholder=\"请选择\">";
		    		htmlshow += "</td>";
		    		htmlshow += "<td><span id=\"" + fieldformname + "_ismandspan\" " + disIsmand + ">" + getRequiredMark() + "</span>";
		    		htmlshow += "<input type=\"hidden\" id=\"" + fieldformname + "_ismandfield\" name=\"ismandfield\" value=\"" + ismandfieldval + "\"/></td>";
		    		
		    		htmlshow += "</tr></table>";
		    	} else {
		    		//htmlshow = showvalue;
		    		htmlshow = "<span id=\""+fieldformname+"_span\">" + showvalue + "</span>";
		    		//当字段为不可编辑时，字段联动需要获取对应key的value值，此处将key value对保存到页面上

		    		String hidOptHtmlstr = "<div style=\"display:none;\">";
		    		for(int i=0;i<selectnamelist.size();i++){
		    			String selectnamestr = (String)selectnamelist.get(i);
		    			String selectvaluestr = (String)selectvaluelist.get(i);
		    			hidOptHtmlstr += "<span id=\"" + fieldformname + "_" + selectvaluestr + "\">" + selectnamestr + "</span>";
		    		}
		    		hidOptHtmlstr += "</div>";
		    		htmlshow += hidOptHtmlstr;
		    	}
		    	if (selfieldsadd != null && selfieldsadd.indexOf(String.valueOf(fieldid))  != -1) {
					jsStr = "<script type=\"text/javascript\">";
					jsStr += "$(document).ready(function () {";
					jsStr += "\t    changeshowattr('"+fieldid+"_0','"+sltval+"',-1,"+workflowid+","+nodeid+");"+"\n";
					jsStr += "})";
					jsStr += "</script>";
				}
		    	if(childfieldid_tmp != 0){
		    		jsStr +=  "<script>$(document).ready(function () {try{doInitChildSelect('"+fieldid+"','"+childfieldid_tmp+"','"+isdetail+"','"+groupid+"','"+detailVar+"','"+sltval+"');}catch(e){alert(e);}});</script>";
		    	}
		    	
		    	htmlshow += jsStr;
		    // 附件上传
			} else if (fieldhtmltype.equals("6")) {
				boolean createCanDel = createCanDel(workflowid,nodeid);
				String showdelbtn = " showdelbtn=\"0\" ";
				if(fieldformname.indexOf("_")!=-1){
					showdelbtn = " showdelbtn=\"1\" ";
				}
				
				if (!fieldvalue.equals("")) {
				  if ("-2".equals(fieldvalue)) {
					  showvalue = SystemEnv.getHtmlLabelName(21710, user.getLanguage());
				  } else {
					fieldvalue = WorkflowSpeechAppend.converBrowserBtnVal(fieldvalue);
					rs.executeSql("select id,doceditionid from DocDetail where id in ("+fieldvalue+")");
					String fieldvalueStr = "";
					RecordSet rsign = new RecordSet();
					while (rs.next()) {
						int doceditionid = Util.getIntValue(Util.null2String(rs.getString("doceditionid")));
						if (doceditionid > -1) {				
							rsign.executeSql("select MAX(id) id from DocDetail where doceditionid="+doceditionid);
							if (rsign.next()) {
								fieldvalueStr += Util.null2String(rsign.getString("id"))+",";
							}
						}else{
							fieldvalueStr += Util.null2String(rs.getString("id"))+",";
						}
					}
				    //如果附件对应目录没有开启版本管理

					if (!"".equals(fieldvalueStr)) {
						fieldvalue = WorkflowSpeechAppend.converBrowserBtnVal(fieldvalueStr);
					}
					
				      String sql = "select id,docsubject,accessorycount,SecCategory from docdetail where id in(" + fieldvalue + ") order by id asc";
					  rs.executeSql(sql);
					  while (rs.next()) {
					    String showid = Util.null2String(rs.getString(1));
					    String SecCategory = Util.null2String(rs.getString(4));
					    docImageManager.resetParameter();
					    docImageManager.setDocid(Integer.parseInt(showid));
					    docImageManager.selectDocImageInfo();
					
					    String docImagefileid = "";
					    long docImagefileSize = 0;
					    String docImagefilename = "";
					
					    if (docImageManager.next()) {
					      docImagefileid = docImageManager.getImagefileid();
					      docImagefileSize = docImageManager.getImageFileSize(Util.getIntValue(docImagefileid));
					      docImagefilename = docImageManager.getImagefilename();
					    }
					    @SuppressWarnings("unused")
						boolean nodownload = secCategoryComInfo_vb.getNoDownload(SecCategory).equals("1");
					    if (fieldtype.equals("2")) {
							String downloadDocImageFileName = docImagefilename.replaceAll("&","%26");
							showvalue += "<div id='appDix_" + showid + "'><span style='text-decoration:underline;cursor:hand;color:blue' onclick=\"toDownload('" + docImagefileid + "','" + downloadDocImageFileName + "',false);\" >" + docImagefilename + "(" + (docImagefileSize / 1000) + "K)" + "</span>" ;
							
							if("1".equals(isedit) && createCanDel){
							  showvalue += "<a name=\"appendixDelField\" "+showdelbtn+" href=\"javascript:delAppendix('#" + fieldformname + "','#appDix_" + showid + "')\" ><img src='/images/delete_wev8.gif'></a>";
							}
					        showvalue += "</div>";
					    } else {
							String downloadDocImageFileName = docImagefilename.replaceAll("&","%26");
							showvalue += "<div id='appDix_" + showid + "'><span style='text-decoration:underline;cursor:hand;color:blue' onclick=\"toDownload('" + docImagefileid + "','" + downloadDocImageFileName + "',false);\" >" + docImagefilename + "(" + (docImagefileSize / 1000) + "K)" + "</span>";
							if("1".equals(isedit) && createCanDel){
								showvalue += "<a name=\"appendixDelField\" "+showdelbtn+" href=\"javascript:delAppendix('#" + fieldformname + "','#appDix_" + showid + "')\" ><img src='/images/delete_wev8.gif'></a>";
							}
							showvalue += "</div>";
					    }
					  }
				  }
				}
				
				
				htmlshow =  "<table style=\"width:100%;\" fieldhtmltype=\"6\" _id=\"" + fieldformname + "\">" +
						    " <tr>"+
					        "  <td id=\""+fieldformname+"_span\" name=\"appendixDatasField\" style=\"width:90%;white-space:normal;\" align=\"left\">"+showvalue.replace(showdelbtn, "")+"</td>";
				htmlshow += "  <td ><span id=\"" + fieldformname + "_ismandspan\" " + disIsmand + ">" + getRequiredMark() + "</span>"+
							"   <input type=\"hidden\" name=\"cnt"+fieldformname+"\" id=\"cnt"+fieldformname+"\" value=\"0\"/>" + 
							"   <input type=\"hidden\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\"/>";
				htmlshow += "   <input type=\"hidden\" id=\"" + fieldformname + "_ismandfield\" name=\"ismandfield\" value=\"" + ismandfieldval + "\"/></td>";
				htmlshow += "</tr></table>";
				
				if ("1".equals(isedit)) {
					//获取当前流程“基本设置”中的“附件上传目录”

					String docCatagory = WorkflowComInfo.getDocCategory(WorkflowVersion.getActiveVersionWFID(workflowid));
					//未设置附件上传目录的情况
					if("".equals(docCatagory) || docCatagory == null){
						String tempStr = "<span style='color:#ACA899' remind=\"1\">" + SystemEnv.getHtmlLabelName(22210, languageid) + SystemEnv.getHtmlLabelName(15808, languageid) + "!" + "</span>";
						//htmlshow += tempStr;
						
						//System.out.println("languageid:"+languageid);
						
						htmlshow =  "<table style=\"width:100%;\" fieldhtmltype=\"6\" _id=\"" + fieldformname + "\">" +
								    " <tr>"+
							        "  <td id=\""+fieldformname+"_span\" name=\"appendixDatasField\" style=\"width:90%;white-space:normal;\" align=\"left\">"+showvalue.replace(showdelbtn, "")+tempStr+"</td>";
						htmlshow += "  <td >" +
							        "<div class=\"upload-box\">"+
								"<span id=\"" + fieldformname + "_ismandspan\" " + disIsmand + ">" + "</span>"+
									"   <input type=\"hidden\" name=\"cnt"+fieldformname+"\" id=\"cnt"+fieldformname+"\" value=\"0\"/>" + 
									"   <input type=\"file\" class=\"original-upload\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\"/>";
						htmlshow += "   <input type=\"hidden\" id=\"" + fieldformname + "_ismandfield\" name=\"ismandfield\" value=\"" + ismandfieldval + "\"/>" +
								"" + "</div>"+
								"</td>";
						
						htmlshow += "</tr></table>";
						
					} else {
						//给页面上定义js变量，用于存储页面上所新增的附件数目。

						htmlshow = "<table style=\"width:100%;\" fieldhtmltype=\"6\" _id=\"" + fieldformname + "\">" +
								   " <tr><td style=\"width:90%;\" name=\"appendixEditField\" onclick=\"javascript:addAppendix('"+ fieldformname +"', '"+String.valueOf(createCanDel)+"')\">"+
//							       "      <a href=\"javascript:void(0);\" data-rel=\"dialog\" data-transition=\"pop\">"+
//							       "      <div style=\"background-image:url('/images/search_icon_wev8.png');height:30px;width:30px;\"></div></a>"+
							       "     </td>"+
							       "     <td id=\""+fieldformname+"_span\" name=\"appendixDatasField\" style=\"width:90%;white-space:normal;display:none;\" align=\"left\">"+showvalue.replace(showdelbtn, "")+"</td>";

						htmlshow += "    <td >" +
								 "<div class=\"upload-box\">"+
								"<span id=\"" + fieldformname + "_ismandspan\" " + disIsmand + ">" + getRequiredMark() + "</span>"+
									"     <input type=\"hidden\" name=\"cnt"+fieldformname+"\" id=\"cnt"+fieldformname+"\" value=\"0\"/>" + 
									"     <input type=\"file\"  multiple=\"multiple\" class=\"original-upload\" onchange=\"updateFilename(this,'"+fieldformname+"')\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\"/>";
						htmlshow += "     <span onclick=\"showfilebrowser(this)\" style=\"width:120px;display:block;top:-25px;position:relative;text-align:left;\" name=\"filedis_" + fieldformname + "\" id=\"filedis_" + fieldformname + "\" \">";
						if(fieldvalue.length()>0){
							htmlshow+="已选择<font class='file_init' color='red'>"+fieldvalue.split(",").length+"</font>个文件";
						}
						htmlshow +="</span>";
						htmlshow += "     <input type=\"hidden\" id=\"filesaveid_" + fieldformname + "\" name=\"filesaveid_" + fieldformname + "\" value='"+fieldvalue+"' \"></input>";
						htmlshow += "     <input type=\"hidden\" id=\"filenames_" + fieldformname + "\" name=\"filenames_" + fieldformname + "\" \"></input>";
						htmlshow += "     <span style=\"display:none\" id=\"fileids_" + fieldformname + "\" name=\"fileids_" + fieldformname + "\" \"></span>";
						htmlshow += "     <input type=\"hidden\" id=\"filedata_" + fieldformname + "\" name=\"filedata_" + fieldformname + "\" \"></input>";
						htmlshow += "     <input type=\"hidden\" id=\"" + fieldformname + "_ismandfield\" name=\"ismandfield\" value=\"" + ismandfieldval + "\"/>" +
								"" +
								"</td>";
						htmlshow += "</tr></table>";
					}
				}else{
					//给页面上定义js变量，用于存储页面上所新增的附件数目。

					htmlshow =  "<table style=\"width:100%;\" fieldhtmltype=\"6\" _id=\"" + fieldformname + "\">" +
							    " <tr>"+
						        "  <td id=\""+fieldformname+"_span\"  name=\"appendixDatasField\" style=\"width:90%;white-space:normal;\" align=\"left\">"+showvalue.replace(showdelbtn, "")+"</td>";
					htmlshow += "  <td ><input type=\"hidden\" name=\"cnt"+fieldformname+"\" id=\"cnt"+fieldformname+"\" value=\"0\"/>" + 
						        "	<input type=\"hidden\" name=\"filesaveid_"+fieldformname+"\" id=\"filesaveid_"+fieldformname+"\" value=\""+fieldvalue+"\"/>"+
								"   <input type=\"hidden\" name=\""+fieldformname+"\" id=\""+fieldformname+"\" nameBak=\""+fieldname+"\" value=\""+fieldvalue+"\"/></td>";
					//htmlshow += "   <input type=\"hidden\" id=\"" + fieldformname + "_ismandfield\" name=\"ismandfield\" value=\"" + ismandfieldval + "\"/></td>";
					htmlshow += "</tr></table>";
				}
				
			} else if (fieldhtmltype.equals("7")) {// 特殊字段
				if (isbill.equals("0"))
					showvalue = (String) specialfield.get(fieldid + "_0");
				else
					showvalue = (String) specialfield.get(fieldid + "_1");
	    		htmlshow = showvalue;
			}else if(fieldhtmltype.equals("9")) {// 位置字段
                //htmlshow = GPSLocationUtil.getGPSMobileHtml(fieldformname, fieldname, fieldvalue, isview, isedit, disIsmand, ismandfieldval);
                htmlshow = GPSLocationUtil.showLocationField(fieldid,fieldvalue,requestid,isAutoLocate,isedit,isview,ismand,languageid,isremark,takisremark,module,freeNodeIsEdit);
                //System.out.println("========================================fieldformname="+fieldformname);
                //System.out.println("====================workflowid="+workflowid +", requestid="+ requestid +", fieldid="+ fieldid);
			    showvalue= htmlshow;
			}
			
			//if(changefieldsadd != null && changefieldsadd.indexOf(fieldid) != -1){
		    int oldval = Util.getIntValue(isview, 0) + Util.getIntValue(isedit, 0) + Util.getIntValue(ismand, 0);
		    String temphtmlshow = "";
		    if(isdetail2){
		    	temphtmlshow = "<input type=\"hidden\" id=\"oldfieldview" + fieldid+""+ cValFieldsDetail + "\" name=\"oldfieldview" + fieldid +""+cValFieldsDetail+ "\" value=\"" + oldval + "\" />";
		    }else{
		    	temphtmlshow = "<input type=\"hidden\" id=\"oldfieldview" + fieldid + "\" name=\"oldfieldview" + fieldid + "\" value=\"" + oldval + "\" />";
		    }
		    //rs.writeLog("isdetail2 = "+isdetail2+"   "+temphtmlshow);
		    htmlshow += temphtmlshow;
			//}
			
			//用于字段联动，单行文本不处理（已经单独处理过了）
			if (!"1".equals(isedit) && !"1".equals(fieldhtmltype) && !"2".equals(fieldhtmltype)) {
				htmlshow += "<input _detailRecordId=\""+detailRecordId+"\" type=\"hidden\" name=\"" + fieldformname + "\" id=\"" + fieldformname + "\" _fieldhtmlType=\"" + fieldhtmltype + "\" _fieldType=\"" + fieldtype + "\" value=\"" + fieldvalue + "\" nameBak=\"" + fieldname + "\" >";
			}
			
			if (requestid <= 0) {
				htmlshow += initDynmjs;
			}
	    }
	    
		WorkflowRequestTableField wrf = new WorkflowRequestTableField();
		wrf.setFieldId(fieldid);
		wrf.setFieldName(fieldname);
		wrf.setFieldValue(fieldvalue);
		wrf.setFieldHtmlType(fieldhtmltype);
		wrf.setFieldType(fieldtype);
		wrf.setFieldDBType(fielddbtype);
		wrf.setFieldFormName(fieldformname);
		wrf.setFieldOrder(fieldorder);
		wrf.setView("1".equals(isview));
		wrf.setEdit("1".equals(isedit));
		wrf.setMand("1".equals(ismand));
		wrf.setFieldOrder(fieldorder);
		wrf.setFieldShowName(showname);
		if(fieldhtmltype.equals("1")&&fieldtype.equals("4")&&showvalue.indexOf("field_chinglish")==-1){
			showvalue += "<script language=\"javascript\">";
			showvalue += "jQuery(function(){";
			showvalue += "jQuery(\"#\"+jQuery(\"#"+fieldformname.replace("field","").replace("_", "")+"\").val()).html(\"<span id='"+fieldformname.replace("field", "field_chinglish")+"_span' ></span>&nbsp;"+("".equals(fieldvalue)?"":"("+fieldvalue+")")+"\");";
			showvalue += "jQuery(\"span[id='"+fieldformname.replace("field", "field_chinglish")+"_span']\").html(numberChangeToChinese("+fieldvalue+"));";
			showvalue += "jQuery(\"span[id='"+fieldformname+"_span']\").html(numberChangeToChinese("+fieldvalue+")+'"+(Util.null2String(fieldvalue).equals("")?"":"&nbsp;("+fieldvalue+")")+"');});";
			showvalue += "</script>";
			wrf.setFieldShowValue(showvalue);
		}else{
			wrf.setFieldShowValue(showvalue);
		}
		String[] selectnames = (String[])selectnamelist.toArray(new String[selectnamelist.size()]);
		String[] selectvalues = (String[])selectvaluelist.toArray(new String[selectvaluelist.size()]);
		wrf.setSelectnames(selectnames);
		wrf.setSelectvalues(selectvalues);
		wrf.setIsshowtree(isshowtree);
		
		wrf.setBrowserurl(browserurl);
		
		wrf.setFiledHtmlShow(htmlshow);
		log.info("End run getWorkflowRequestField method, and following is the returnValue:\n" + ReflectionToStringBuilder.toString(wrf));
		return wrf;
	
	}
	
	
	
	
	
	/**
	 * 取得流程标题,紧急程度,是否短信提醒等表头字段信息

	 * 
	 * @return List
	 * @throws Exception
	 */
	public static List getWorkflowHeadFields(WorkflowRequestInfo wri,User user,boolean editflag) throws Exception {
		List result = new ArrayList();
		
		RecordSet rs = new RecordSet();
		
		WFNodeFieldMainManager wfNodeFieldMainManager = new WFNodeFieldMainManager();
		weaver.workflow.request.WFLinkInfo wfLinkInfo = new weaver.workflow.request.WFLinkInfo();
		String nodetype=wfLinkInfo.getNodeType(Util.getIntValue(wri.getCurrentNodeId()));

		//流程标题
		WorkflowRequestTableField requestnamefield = new WorkflowRequestTableField();
		requestnamefield.setFieldId("-1");
		requestnamefield.setFieldName("requestname");
		requestnamefield.setFieldShowName(SystemEnv.getHtmlLabelName(21192,user.getLanguage()));
		requestnamefield.setFieldFormName("requestname");
		requestnamefield.setFieldOrder(-1);
		//當流程id为小于等于0的时候，此处的流程标题标识为创建流程 将流程标题设置为规则定义之中的内容，否则按照原有的方式处理

		if(Util.getIntValue(wri.getRequestId(),0)<=0){
         weaver.crm.Maint.CustomerInfoComInfo customerInfoComInfo = new weaver.crm.Maint.CustomerInfoComInfo();
         String usernamenew = "";
        	if(user.getLogintype().equals("1"))
        		usernamenew = user.getLastname();
        	if(user.getLogintype().equals("2"))
        		usernamenew = customerInfoComInfo.getCustomerInfoname(""+user.getUID());
        	
          weaver.general.DateUtil   DateUtil=new weaver.general.DateUtil();
          String 	txtuseruse=DateUtil.getWFTitleNew(""+wri.getWorkflowBaseInfo().getWorkflowId(),""+user.getUID(),""+usernamenew,user.getLogintype());

         
         txtuseruse =txtuseruse != null ? txtuseruse.replace("\"", "&#34;"):"";
		 requestnamefield.setFieldValue(txtuseruse);
	     requestnamefield.setFieldShowValue(txtuseruse);
		}else{
			String titleName =wri.getRequestName() != null ? wri.getRequestName().replace("\"", "&#34;"):"";
			requestnamefield.setFieldValue(titleName);
			requestnamefield.setFieldShowValue(wri.getRequestName());
		}
		requestnamefield.setFieldType("");
		requestnamefield.setFiledHtmlShow("");
		requestnamefield.setFieldDBType("");
		requestnamefield.setFieldHtmlType("1");//文本框

		
		requestnamefield.setView(true);
		requestnamefield.setEdit(false);
		requestnamefield.setMand(true);
		
		wfNodeFieldMainManager.resetParameter();
		wfNodeFieldMainManager.setNodeid(Util.getIntValue(wri.getCurrentNodeId()));
		wfNodeFieldMainManager.setFieldid(-1);//"流程标题"字段在workflow_nodeform中的fieldid 定为 "-1"
		wfNodeFieldMainManager.selectWfNodeField();
		if(wfNodeFieldMainManager.getIsedit().equals("1")||"0".equals(nodetype))
			requestnamefield.setEdit(true);
		
		
		//紧急程度

		WorkflowRequestTableField requestlevelfield = new WorkflowRequestTableField();
		
		requestlevelfield.setFieldId("-2");
		requestlevelfield.setFieldName("requestlevel");
		requestlevelfield.setFieldShowName("");
		requestlevelfield.setFieldFormName("requestlevel");
		requestlevelfield.setFieldOrder(-2);
		requestlevelfield.setFieldValue(wri.getRequestLevel());
		if("0".equals(wri.getRequestLevel()))
		requestlevelfield.setFieldShowValue(SystemEnv.getHtmlLabelName(225,user.getLanguage()));
		else if("1".equals(wri.getRequestLevel()))
		requestlevelfield.setFieldShowValue(SystemEnv.getHtmlLabelName(15533,user.getLanguage()));
		else if("2".equals(wri.getRequestLevel()))
		requestlevelfield.setFieldShowValue(SystemEnv.getHtmlLabelName(2087,user.getLanguage()));
			
		requestlevelfield.setFieldType("");
		requestlevelfield.setFiledHtmlShow("");
		requestlevelfield.setFieldDBType("");
		requestlevelfield.setFieldHtmlType("5");//选择框

		
		requestlevelfield.setSelectnames(new String[]{SystemEnv.getHtmlLabelName(225,user.getLanguage()),SystemEnv.getHtmlLabelName(15533,user.getLanguage()),SystemEnv.getHtmlLabelName(2087,user.getLanguage())});
		requestlevelfield.setSelectvalues(new String[]{"0","1","2"});
		
		requestlevelfield.setView(true);
		requestlevelfield.setEdit(false);
		requestlevelfield.setMand(false);
		
		wfNodeFieldMainManager.resetParameter();
		wfNodeFieldMainManager.setNodeid(Util.getIntValue(wri.getCurrentNodeId()));
		wfNodeFieldMainManager.setFieldid(-2);//"紧急程度"字段在workflow_nodeform中的fieldid 定为 "-2"
		wfNodeFieldMainManager.selectWfNodeField();
		if(wfNodeFieldMainManager.getIsedit().equals("1")||"0".equals(nodetype))
			requestlevelfield.setEdit(true);
		//是否短信提醒
		WorkflowRequestTableField messagetypefield = new WorkflowRequestTableField();
		WFManager wf = new WFManager();
		wf.setWfid(Util.getIntValue(wri.getWorkflowBaseInfo().getWorkflowId()));
		wf.getWfInfo();		
		String smsAlertsType =wf.getSmsAlertsType();
		messagetypefield.setFieldId("-3");
		messagetypefield.setFieldName("messageType");
		messagetypefield.setFieldShowName(SystemEnv.getHtmlLabelName(17586,user.getLanguage()));
		messagetypefield.setFieldFormName("messageType");
		messagetypefield.setFieldOrder(-3);
		if(Util.getIntValue(wri.getRequestId())<=0){
			messagetypefield.setFieldValue(smsAlertsType);
			if("0".equals(smsAlertsType))
			messagetypefield.setFieldShowValue(SystemEnv.getHtmlLabelName(17583,user.getLanguage()));
			else if("1".equals(smsAlertsType))
			messagetypefield.setFieldShowValue(SystemEnv.getHtmlLabelName(17584,user.getLanguage()));
			else if("2".equals(smsAlertsType))
			messagetypefield.setFieldShowValue(SystemEnv.getHtmlLabelName(17585,user.getLanguage()));			
		}else{
			messagetypefield.setFieldValue(wri.getMessageType());
			if("0".equals(wri.getMessageType()))
			messagetypefield.setFieldShowValue(SystemEnv.getHtmlLabelName(17583,user.getLanguage()));
			else if("1".equals(wri.getMessageType()))
			messagetypefield.setFieldShowValue(SystemEnv.getHtmlLabelName(17584,user.getLanguage()));
			else if("2".equals(wri.getMessageType()))
			messagetypefield.setFieldShowValue(SystemEnv.getHtmlLabelName(17585,user.getLanguage()));		
		}
		
		messagetypefield.setFieldType("");
		messagetypefield.setFiledHtmlShow("");
		messagetypefield.setFieldDBType("");
		messagetypefield.setFieldHtmlType("5");//选择框

		
		messagetypefield.setSelectnames(new String[]{SystemEnv.getHtmlLabelName(17583,user.getLanguage()),SystemEnv.getHtmlLabelName(17584,user.getLanguage()),SystemEnv.getHtmlLabelName(17585,user.getLanguage())});
		messagetypefield.setSelectvalues(new String[]{"0","1","2"});
		
		messagetypefield.setView(false);
		messagetypefield.setEdit(false);
		messagetypefield.setMand(false);
		
		wfNodeFieldMainManager.resetParameter();
		wfNodeFieldMainManager.setNodeid(Util.getIntValue(wri.getCurrentNodeId()));
		wfNodeFieldMainManager.setFieldid(-3);//"是否短信提醒"字段在workflow_nodeform中的fieldid 定为 "-3"
		wfNodeFieldMainManager.selectWfNodeField();
		if(wfNodeFieldMainManager.getIsedit().equals("1")||"0".equals(nodetype))
			messagetypefield.setEdit(true);
		if(wfNodeFieldMainManager.getIsmandatory().equals("1"))
			messagetypefield.setMand(true);
		
		if((!editflag&&!"0".equals(nodetype))||!wri.isCanEdit()){
			requestnamefield.setEdit(false);
			requestlevelfield.setEdit(false);
			messagetypefield.setEdit(false);
		}

	    String sqlWfMessage = "select messageType from workflow_base where id="+wri.getWorkflowBaseInfo().getWorkflowId();
	    int wfMessageType=0;
	    rs.executeSql(sqlWfMessage);
	    if (rs.next()) {
	    	wfMessageType=rs.getInt("messageType");
	    }
	    if(wfMessageType == 1 && wri.isCanView()){
			messagetypefield.setView(true);
		} else {
			messagetypefield.setView(false);
		}

	    
	    //是否微信提醒chatsType
	    WorkflowRequestTableField chatstypefield = new WorkflowRequestTableField();
		WFManager chatswf = new WFManager();
		chatswf.setWfid(Util.getIntValue(wri.getWorkflowBaseInfo().getWorkflowId()));
		chatswf.getWfInfo();		
		String chatsType =chatswf.getChatsType();
		if("1".equals(chatsType)){chatsType = chatswf.getChatsAlertType();}
		chatstypefield.setFieldId("-5");
		chatstypefield.setFieldName("chatsType");
		chatstypefield.setFieldShowName(SystemEnv.getHtmlLabelName(32812,user.getLanguage()));
		chatstypefield.setFieldFormName("chatsType");
		chatstypefield.setFieldOrder(-5);
		if(Util.getIntValue(wri.getRequestId())<=0){
			chatstypefield.setFieldValue(chatsType);
			if("0".equals(chatsType))
				chatstypefield.setFieldShowValue(SystemEnv.getHtmlLabelName(19782,user.getLanguage()));
			else if("1".equals(chatsType))
				chatstypefield.setFieldShowValue(SystemEnv.getHtmlLabelName(26928,user.getLanguage()));
		}else{
			chatstypefield.setFieldValue(wri.getChatsType());
			if("0".equals(wri.getChatsType()))
				chatstypefield.setFieldShowValue(SystemEnv.getHtmlLabelName(19782,user.getLanguage()));
			else if("1".equals(wri.getChatsType()))
				chatstypefield.setFieldShowValue(SystemEnv.getHtmlLabelName(26928,user.getLanguage()));
		}
		
		chatstypefield.setFieldType("");
		chatstypefield.setFiledHtmlShow("");
		chatstypefield.setFieldDBType("");
		chatstypefield.setFieldHtmlType("5");//选择框

		
		chatstypefield.setSelectnames(new String[]{SystemEnv.getHtmlLabelName(19782,user.getLanguage()),SystemEnv.getHtmlLabelName(26928,user.getLanguage())});
		chatstypefield.setSelectvalues(new String[]{"0","1"});
		
		chatstypefield.setView(false);
		chatstypefield.setEdit(false);
		chatstypefield.setMand(false);
		
		wfNodeFieldMainManager.resetParameter();
		wfNodeFieldMainManager.setNodeid(Util.getIntValue(wri.getCurrentNodeId()));
		wfNodeFieldMainManager.setFieldid(-3);//"是否短信提醒"字段在workflow_nodeform中的fieldid 定为 "-3"
		wfNodeFieldMainManager.selectWfNodeField();
		if(wfNodeFieldMainManager.getIsedit().equals("1")||"0".equals(nodetype))
			chatstypefield.setEdit(true);
		if(wfNodeFieldMainManager.getIsmandatory().equals("1"))
			chatstypefield.setMand(true);
		
		if((!editflag&&!"0".equals(nodetype))||!wri.isCanEdit()){
			requestnamefield.setEdit(false);
			requestlevelfield.setEdit(false);
			chatstypefield.setEdit(false);
		}

	    String sqlChatsWfMessage = "select chatsType from workflow_base where id="+wri.getWorkflowBaseInfo().getWorkflowId();
	    int wfChatsType=0;
	    rs.executeSql(sqlChatsWfMessage);
	    if (rs.next()) {
	    	wfChatsType=rs.getInt("chatsType");
	    }
	    if(wfChatsType == 1 && wri.isCanView()){
	    	chatstypefield.setView(true);
		} else {
			chatstypefield.setView(false);
		}
	    //end
	    
	    String requestnamehtmlshow = "";
    	if(requestnamefield.isView()) {
	    	if(requestnamefield.isEdit()) {
	    		requestnamehtmlshow = "<table style=\"width:100%;\"><tr><td style=\"width:99%;white-space:normal;\" align=\"left\">" +
		    	"<input type=\"text\" name=\""+requestnamefield.getFieldFormName()+"\" id=\""+requestnamefield.getFieldName()+"\" value=\""+requestnamefield.getFieldValue()+"\" />"+
		    	"</td>";
		    	if(requestnamefield.isMand()) requestnamehtmlshow += 
		    		"<td><span id=\""+requestnamefield.getFieldName()+"_ismandspan\" class=\"ismand\">" +
		    		getRequiredMark()+
		    		"</span>" +
		    		"<input type=\"hidden\" id=\"ismandfield\" name=\"ismandfield\" value=\""+requestnamefield.getFieldName()+"\"/></td>";
		    	
		    	requestnamehtmlshow += "</tr></table>";
				
	    	} else {
	    		requestnamehtmlshow = requestnamefield.getFieldShowValue();
	    	}
    	}
    	requestnamefield.setFiledHtmlShow(requestnamehtmlshow);
	    
    	String requestlevelhtmlshow = "";
    	if(requestlevelfield.isView()) {
    		if(requestlevelfield.isEdit()) {
    			requestlevelhtmlshow = "<table style=\"width:100%;\"><tr><td style=\"width:99%;white-space:normal;\" align=\"left\">" +
    	    	//"<fieldset data-role=\"controlgroup\">" +
		        "<input type=\"radio\" name=\""+requestlevelfield.getFieldFormName()+"\" id=\""+requestlevelfield.getFieldName()+"-0\" value=\"0\" "+("0".equals(requestlevelfield.getFieldValue())?"checked":"") + " />" +
		        "<label for=\""+requestlevelfield.getFieldFormName()+"-0\">"+SystemEnv.getHtmlLabelName(225,user.getLanguage())+"</label>" +
		        "<input type=\"radio\" name=\""+requestlevelfield.getFieldFormName()+"\" id=\""+requestlevelfield.getFieldName()+"-1\" value=\"1\" "+("1".equals(requestlevelfield.getFieldValue())?"checked":"") + " />" +
			    "<label for=\""+requestlevelfield.getFieldFormName()+"-1\">"+SystemEnv.getHtmlLabelName(15533,user.getLanguage())+"</label>" +
			    "<input type=\"radio\" name=\""+requestlevelfield.getFieldFormName()+"\" id=\""+requestlevelfield.getFieldName()+"-2\" value=\"2\" "+("2".equals(requestlevelfield.getFieldValue())?"checked":"") + " />" +
			    "<label for=\""+requestlevelfield.getFieldFormName()+"-2\">"+SystemEnv.getHtmlLabelName(2087,user.getLanguage())+"</label>" +
			   // "</fieldset>" +
    	    	"</td>";
    	    	if(requestlevelfield.isMand()) requestlevelhtmlshow += 
    	    		"<td><span id=\""+requestlevelfield.getFieldName()+"_ismandspan\" class=\"ismand\">" +
    	    		getRequiredMark()+
    	    		"</span>" +
    	    		"<input type=\"hidden\" id=\"ismandfield\" name=\"ismandfield\" value=\""+requestlevelfield.getFieldName()+"\"/></td>";
    	    	
    	    	requestlevelhtmlshow += "</tr></table>";
    			
    		} else {
    			requestlevelhtmlshow = requestlevelfield.getFieldShowValue();
    		}
    	}
    	requestlevelfield.setFiledHtmlShow(requestlevelhtmlshow);
    	
        String messagetypehtmlshow = "";
    	if(messagetypefield.isView()) {
    		if(messagetypefield.isEdit()) {
    			messagetypehtmlshow = "<table style=\"width:100%;\"><tr><td style=\"width:99%;white-space:normal;\" align=\"left\">" +
    	    	"<fieldset data-role=\"controlgroup\">" +
		        "<input type=\"radio\" name=\""+messagetypefield.getFieldFormName()+"\" id=\""+messagetypefield.getFieldName()+"-0\" value=\"0\" "+("0".equals(messagetypefield.getFieldValue())?"checked":"") + " />" +
		        "<label for=\""+messagetypefield.getFieldFormName()+"-0\">"+SystemEnv.getHtmlLabelName(17583,user.getLanguage())+"</label>" +
		        "<input type=\"radio\" name=\""+messagetypefield.getFieldFormName()+"\" id=\""+messagetypefield.getFieldName()+"-1\" value=\"1\" "+("1".equals(messagetypefield.getFieldValue())?"checked":"") + " />" +
			    "<label for=\""+messagetypefield.getFieldFormName()+"-1\">"+SystemEnv.getHtmlLabelName(17584,user.getLanguage())+"</label>" +
			    "<input type=\"radio\" name=\""+messagetypefield.getFieldFormName()+"\" id=\""+messagetypefield.getFieldName()+"-2\" value=\"2\" "+("2".equals(messagetypefield.getFieldValue())?"checked":"") + " />" +
			    "<label for=\""+messagetypefield.getFieldFormName()+"-2\">"+SystemEnv.getHtmlLabelName(17585,user.getLanguage())+"</label>" +
			    "</fieldset>" +
    	    	"</td>";
    	    	if(messagetypefield.isMand()) messagetypehtmlshow += 
    	    		"<td><span id=\""+messagetypefield.getFieldName()+"_ismandspan\" class=\"ismand\">" +
    	    		getRequiredMark()+
    	    		"</span>" +
    	    		"<input type=\"hidden\" id=\"ismandfield\" name=\"ismandfield\" value=\""+messagetypefield.getFieldName()+"\"/></td>";
    	    	
    	    	messagetypehtmlshow += "</tr></table>";
    			
    		} else {
    			messagetypehtmlshow = messagetypefield.getFieldShowValue();
    			messagetypehtmlshow += "<input type=\"hidden\" name=\""+messagetypefield.getFieldFormName()+"\" value=\""+messagetypefield.getFieldValue()+"\"/>";
    		}
    	}
    	messagetypefield.setFiledHtmlShow(messagetypehtmlshow);
    	
    	String chatstypehtmlshow = "";
     	if(chatstypefield.isView()) {
     		if(chatstypefield.isEdit()) {
     			chatstypehtmlshow = "<table style=\"width:100%;\"><tr><td style=\"width:99%;white-space:normal;\" align=\"left\">" +
     	    	"<fieldset data-role=\"controlgroup\">" +
 		        "<input type=\"radio\" name=\""+chatstypefield.getFieldFormName()+"\" id=\""+chatstypefield.getFieldName()+"-0\" value=\"0\" "+("0".equals(chatstypefield.getFieldValue())?"checked":"") + " />" +
 		        "<label for=\""+chatstypefield.getFieldFormName()+"-0\">"+SystemEnv.getHtmlLabelName(19782,user.getLanguage())+"</label>" +
 		        "<input type=\"radio\" name=\""+chatstypefield.getFieldFormName()+"\" id=\""+chatstypefield.getFieldName()+"-1\" value=\"1\" "+("1".equals(chatstypefield.getFieldValue())?"checked":"") + " />" +
 			    "<label for=\""+chatstypefield.getFieldFormName()+"-1\">"+SystemEnv.getHtmlLabelName(26928,user.getLanguage())+"</label>" +
 			    "</fieldset>" +
     	    	"</td>";
     	    	if(chatstypefield.isMand()) chatstypehtmlshow += 
     	    		"<td><span id=\""+chatstypefield.getFieldName()+"_ismandspan\" class=\"ismand\">" +
     	    		getRequiredMark()+
     	    		"</span>" +
     	    		"<input type=\"hidden\" id=\"ismandfield\" name=\"ismandfield\" value=\""+chatstypefield.getFieldName()+"\"/></td>";
     	    	
     	    	chatstypehtmlshow += "</tr></table>";
     			
     		} else {
     			chatstypehtmlshow = chatstypefield.getFieldShowValue();
     			chatstypehtmlshow += "<input type=\"hidden\" name=\""+chatstypefield.getFieldFormName()+"\" value=\""+chatstypefield.getFieldValue()+"\"/>";
     		}
     	}
     	chatstypefield.setFiledHtmlShow(chatstypehtmlshow);
    	
    	result.add(requestnamefield);
		result.add(requestlevelfield);
		result.add(messagetypefield);
		result.add(chatstypefield);
		
		return result;
	}
	
	/**
	* This method ensures that the output String has only valid XML unicode
	* characters as specified by the XML 1.0 standard. For reference, please
	* see <a href="http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char">the
	* standard</a>. This method will return an empty String if the input is
	* null or empty.
	* 
	* @param in
	*            The String whose non-valid characters we want to remove.
	* @return The in String, stripped of non-valid characters.
	*/
	public static String stripNonValidXMLCharacters(String in) {
	    StringBuffer out = new StringBuffer(); // Used to hold the output.
	    char current; // Used to reference the current character.
        try{
	      if (in == null || ("".equals(in)))
	          return ""; // vacancy test.
			 for (int i = 0; i < in.length(); i++) {
				current = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught
										// here; it should not happen.
				if ((current == 0x9) || (current == 0xA) || (current == 0xD)
						|| ((current >= 0x20) && (current <= 0xD7FF))
						|| ((current >= 0xE000) && (current <= 0xFFFD))
						|| ((current >= 0x10000) && (current <= 0x10FFFF)))
					out.append(current);
			}
		}catch(Exception e){
		     out.append(in);
		}
	    return out.toString();
	}

	public boolean iscanEdit(int userid,int requestid){
		boolean canEdit = false;
		String nodetype = "";
		RecordSet rs = new RecordSet();
		String sql = "select wb.*, wn.nodename from workflow_requestbase wb inner JOIN workflow_nodebase wn on wn.id=wb.currentnodeid where wb.requestid=" + requestid;
        rs.executeSql(sql);
        if (rs.next()) {
		   nodetype = Util.null2String(rs.getString("currentnodetype"));       	
        }
		rs.executeSql("select id, requestid,isremark,nodeid from workflow_currentoperator where userid="+userid+" and requestid="+requestid+" order by isremark,id");
		while(rs.next()) {
			WFCoadjutantManager wfcm = new WFCoadjutantManager();
		    String isremark = Util.null2String(rs.getString("isremark")) ;
		    int tmpnodeid=Util.getIntValue(rs.getString("nodeid"));
		    if(isremark.equals("7")) wfcm.getCoadjutantRights(Util.getIntValue(rs.getString("groupdetailid")));
		    if( isremark.equals("1")||isremark.equals("5") || (isremark.equals("7") && wfcm.getIsmodify().equals("1"))|| isremark.equals("9") ||(isremark.equals("0")  && !nodetype.equals("3")) ) {
		    	canEdit=true;
		    	break;
		    }
		}
		return canEdit;
	}
	
	//定义UTF-8下边的空格字符

	private static final byte[] UTF8_BLANK = new byte[]{(byte) 0xc2,(byte)0xa0};
	private static final String GENEAL_BLANK = new String(new byte[]{(byte)0x20});
	
	public static String splitAndFilterString(String input, int length) {
        if (input == null || input.trim().equals("")) {   
            return "";   
        }   
        if(input.indexOf("__noShow") >0){ //如果是签字意见添加位置，地址不需要显示            
            input = input.substring(0, input.indexOf("<br/><a __noShow"));
        }
        // 去掉所有html元素
        HtmlToPlainText formatter = new HtmlToPlainText();
        Document doc = Jsoup.parse(input);
        String str = formatter.getPlainText(doc);
        
        try {
        	String utf8Blank = new String(UTF8_BLANK, "UTF-8");
        	//因使用Jsoup解析字符串中会包含UTF-8格式的空格式字符，故需对其进行替换。

        	str = str.replaceAll(utf8Blank, GENEAL_BLANK);
		} catch (Exception e) {
			log.error("Catch a exception.", e);
		}
        
        
        //过滤XML非法字符
        str = stripNonValidXMLCharacters(str);
        
        int len = str.length();
        if (len <= length) {   
            return str;   
        } else {   
            str = str.substring(0, length);   
            str += "......";   
        }   
        return str;   
    }
	
	
	/**
	 * 勾选了创建节点才可删除附件
	 * @param workflowid
	 * @param nodeid
	 * @return
	 */
	public static boolean createCanDel(String workflowid ,int nodeid){
		  boolean result = true;
		  RecordSet rs = new RecordSet();
		  String workflowbaseSql = "select  candelacc from workflow_base  where id= "+workflowid;
		  rs.executeSql(workflowbaseSql);
		  if(rs.next()){
			  String candelacc= rs.getString("candelacc");
			 if("1".equals(candelacc)){
				 String nodeSql = "select nodetype from workflow_flownode where workflowid = "+workflowid+" and nodeid ="+nodeid;
				  rs.executeSql(nodeSql);
				  if(rs.next()){
					  String nodetype = rs.getString("nodetype");
					  if(!"0".equals(nodetype)){
						  result = false;
					  }
				  }
			 }
		  }
		  return result;
	}
	
	/**
	 * 获取sql语句中拼接的参数
	 * @param sql
	 * @return
	 */
	public static String getJoinParams(String sql,String isbill,int formId,String fieldformname){
		  String params = "";
		  if(sql.indexOf("$")!=-1){
	   		 String[] sqlStrs = sql.split("\\$");
	   		 for(int i=1;i<=sqlStrs.length;i++){
	   			 if(i%2==0){
	   				 if(!"userid".equals(sqlStrs[i-1])){
	   					 String reParams = sqlStrs[i-1];
	   					 String fieldParams = getFieldIdByName(reParams,isbill,formId,fieldformname);
	   					 params += reParams+"-"+fieldParams+"#";
	   				 }
	   			 }
	   		 }
	   		 if(params.lastIndexOf("#")!=-1){
	   			params = params.substring(0,params.lastIndexOf("#"));
	   		 }
	   	  }
		  return params;
	}
	/**
	 *根据字段名称查出字段编号
	 */
	public static String getFieldIdByName(String fieldName,String isbill,int formId,String fieldformname){
		String result = "";
		RecordSet rs =new RecordSet();
		if("0".equals(isbill)){ //老表单

			if(fieldName.indexOf("detail")==-1){ //主表
			      String sql = "select id from workflow_formdict where fieldname = '"+fieldName+"'";
			      rs.executeSql(sql);
			      if(rs.next()){
			    	  String id = rs.getString("id");
			    	  result = "field"+id;
			      }
			}else{//明细表

				String oldfieldName = fieldName.replace("detail_", "");
				String sql = "select a.id from workflow_formdictdetail a,workflow_formfield b where a.id = b.fieldid  and  fieldname = '"+oldfieldName+"' and  isdetail = 1 and  formid = "+formId;	 
				rs.executeSql(sql);
			    if(rs.next()){
			    	 String id = rs.getString("id");
			    	 String mgroupid =fieldformname.substring(fieldformname.lastIndexOf("_")+1);
			    	 result = "field"+id+"_"+mgroupid;
			    }
			}
	    }else{ //新表单

			if(fieldName.indexOf("formtable_main")==-1){ //主表
				String sql = "select a.id from workflow_billfield a,workflow_bill b where  a.billid = b.id and (detailtable = '' or detailtable is null) and b.id ="+formId+"  and a.fieldname ='"+fieldName+"'";	 
				rs.executeSql(sql);
			    if(rs.next()){
			    	 String id = rs.getString("id");
			    	 String groupId = rs.getString("groupId");
			    	 result = "field"+id;
			    }	 
			}else{//明细表

				String mtablename = "";
				String mfieldName ="";
				String mgroupid =fieldformname.substring(fieldformname.lastIndexOf("_")+1);
				if(fieldName.indexOf("_")>=0){
					mtablename= fieldName.substring(0,fieldName.lastIndexOf("_"));
					mfieldName = fieldName.substring(fieldName.lastIndexOf("_")+1);
				}
				String sql = "select a.id from workflow_billfield a,workflow_bill b where  a.billid = b.id and detailtable = '"+mtablename+"'  and b.id ="+formId+"  and a.fieldname ='"+mfieldName+"'";	 
				rs.executeSql(sql);
			    if(rs.next()){
			    	 String id = rs.getString("id");
			    	 result = "field"+id+"_"+mgroupid;
			    }	 
			} 
		}
		return result;
	}
	
	/**
	 * 行列规则对只读的字段需要保存

	 */
	public static boolean isRowColumnRule(String formId,String fieldname){

		boolean returnval =false;
		RecordSet rs =new RecordSet();
		String rowcalstr = "";
		String maincalstr = "";
		String sql=" select rowcalstr,maincalstr from workflow_formdetailinfo where formid='"+formId+"'";
		rs.executeSql(sql);
		if(rs.next()){
			rowcalstr = rs.getString("rowcalstr"); //行规则

			maincalstr = rs.getString("maincalstr");//列规则

		}
		boolean rowval=false;
		if(!"".equals(rowcalstr)){
             if(rowcalstr.indexOf(";")!=-1){
            	 String[] rowargs = rowcalstr.split(";");
            	 for(int i=0;i<rowargs.length;i++){
            		  String rowcal=rowargs[i];
            		  if(rowcal.indexOf("=")!=-1){
            			   String comparefield= rowcal.split("=")[0].replace("detail", "").replace("_","");
            			   if(fieldname.indexOf(comparefield)!=-1){
            				   rowval = true;
            				   break;
            			   }
            		  } 
            	 }
             }else{
            	 if(rowcalstr.indexOf("=")!=-1){
      			   String comparefield= rowcalstr.split("=")[0].replace("detail", "").replace("_","");
      			   if(fieldname.indexOf(comparefield)!=-1){
      				   rowval = true;
      			   }
      		    } 
            }
		}
		boolean colval=false;
		if(!"".equals(maincalstr)){
			if(maincalstr.indexOf(";")!=-1){
				String[] colargs = maincalstr.split(";");
	           	 for(int i=0;i<colargs.length;i++){
	           		  String colcal=colargs[i];
	           		  if(colcal.indexOf("=")!=-1){
	           			   String comparefield= colcal.split("=")[0].replace("main", "").replace("_","");
	           			   if(fieldname.indexOf(comparefield)!=-1){
	           				   colval = true;
	           				   break;
	           			   }
	           		  } 
	           	 }
            }else{
            	if(maincalstr.indexOf("=")!=-1){
       			   String comparefield= maincalstr.split("=")[0].replace("main", "").replace("_","");
       			   if(fieldname.indexOf(comparefield)!=-1){
       				   colval = true;
       			   }
       		    } 
            }
		}
		if(colval||rowval){
			 returnval = true;
		}
		return returnval;
	}
	

	/**
	 * liuzy  获得明细表所有字段信息，用于Html模板添加行

	 * @param workflowid	流程ID
	 * @param nodeid	节点ID
	 * @param groupid	明细组ID
	 * @param user
	 */
	public static HashMap<String,WorkflowRequestTableField> getDetailFieldInfo(String workflowid,String nodeid,int groupid,User user){
		RequestPreAddinoperateManager requestPreAddM = new RequestPreAddinoperateManager();
		requestPreAddM.setCreater(user.getUID());
		requestPreAddM.setOptor(user.getUID());
		requestPreAddM.setWorkflowid(Util.getIntValue(workflowid));
		requestPreAddM.setNodeid(Util.getIntValue(nodeid));
		Hashtable getPreAddRule_hs = requestPreAddM.getPreAddRule();
		List inoperatefields = null;
		List inoperatevalues = null;
		if(getPreAddRule_hs != null){
			inoperatefields = (ArrayList)getPreAddRule_hs.get("inoperatefields");
			inoperatevalues = (ArrayList)getPreAddRule_hs.get("inoperatevalues");
		}
		
		HashMap<String,WorkflowRequestTableField> detailFieldMap=new HashMap<String,WorkflowRequestTableField>();
		RecordSet rs = new RecordSet();
		RecordSet rs1 = new RecordSet();
		RecordSet rs2 = new RecordSet();
	    String rowCalItemStr1,colCalItemStr1,mainCalStr1;
		rowCalItemStr1 = new String("");
		colCalItemStr1 = new String("");
	    mainCalStr1 = new String("");
		rs.executeSql("select isbill,formid from workflow_base where id ='"+workflowid+"'");
		if(rs.next()){
			String isbill = rs.getString("isbill");
			String formid = rs.getString("formid");
			
			//--------------------------------------------------------
			// 属性联动功能(处于性能考虑，同一请求只查询一次) START
			//--------------------------------------------------------
			List selfieldsadd = null;
			List changefieldsadd = null;
			if(Util.getIntValue(nodeid) > 0){
				WfLinkageInfo wfLinkageInfo = new WfLinkageInfo();
				wfLinkageInfo.setFormid(Util.getIntValue(formid));
				wfLinkageInfo.setIsbill(Util.getIntValue(isbill,0));
				wfLinkageInfo.setWorkflowid(Util.getIntValue(workflowid));
				wfLinkageInfo.setLangurageid(user.getLanguage());
				selfieldsadd = wfLinkageInfo.getSelectField(Util.getIntValue(workflowid), Util.getIntValue(nodeid), 1);
				changefieldsadd = wfLinkageInfo.getChangeField(Util.getIntValue(workflowid), Util.getIntValue(nodeid), 1);
			}
			
			if("1".equals(isbill)){ 	//新表单

				 rs.execute("select * from workflow_formdetailinfo where formid="+formid);
				 while(rs.next()){
					rowCalItemStr1 = Util.null2String(rs.getString("rowCalStr"));
					colCalItemStr1 = Util.null2String(rs.getString("colCalStr"));
					mainCalStr1 = Util.null2String(rs.getString("mainCalStr"));
				 }
				 rs.execute("select tablename,title from Workflow_billdetailtable where billid="+formid+" order by orderid");
				 int _groupid=-1;
				 while(rs.next()){
					 _groupid++;
					 if(_groupid!=groupid)	continue;
					 String tablename=rs.getString("tablename");
		             String tabletitle=rs.getString("title"); 
		             rs1.execute("select * from workflow_billfield where viewtype='1' and billid="+formid+" and detailtable='"+tablename+"' ORDER BY dsporder");
		             while(rs1.next()){
		            	    String id=Util.null2String(rs1.getString("id"));
		                    String fieldlabel = SystemEnv.getHtmlLabelName(Util.getIntValue(rs1.getString("fieldlabel")),user.getLanguage());
		                    String fieldhtmltype =Util.null2String(rs1.getString("fieldhtmltype"));
		                    String type = Util.null2String(rs1.getString("type"));
		                    String fieldname = Util.null2String(rs1.getString("fieldname"));
							String fielddbtype = Util.null2String(rs1.getString("fielddbtype"));
							String childfieldid = ""+Util.getIntValue(rs1.getString("childfieldid"), 0);
							String sql="SELECT DISTINCT a.*, b.dsporder FROM workflow_nodeform a ,workflow_billfield b "
								    +" where a.fieldid = b.id and b.billid ="+formid+" and a.nodeid="+nodeid+"  and b.detailtable='"+tablename+"' "
								    +" and a.fieldid = "+id+" ORDER BY b.dsporder ";
							rs2.executeSql(sql);
							String defieldid = "";
							String isview = "";
							String isedit = "";
							String ismand = "";
							int fieldorder = 0;
							if(rs2.next()){
								  defieldid = Util.null2String(rs2.getString("fieldid")) ;
								  isview =  Util.null2String(rs2.getString("isview")) ;
								  isedit =    Util.null2String(rs2.getString("isedit"));
								  ismand = Util.null2String(rs2.getString("ismandatory"));
								  fieldorder =  rs2.getInt("dsporder");
							}
							if("1".equals(isview)){
								  String fieldformname =  "field"+defieldid+"_$rowIndex$";
				         		  try{
				         			 String defieldvalue="";
					         			boolean flagToValue = false;
											//判断是否需要处理节点前附加操作
											if(getPreAddRule_hs != null){
												int fieldIndex = inoperatefields.indexOf(defieldid);
												if(fieldIndex > -1){
													flagToValue = true;
													defieldvalue = (String)inoperatevalues.get(fieldIndex);
												}
										}
										//标识是否给字段赋默认值，仅当是新建请求且没有节点前附加操作时。

										boolean flagDefault =  !flagToValue; 
									WorkflowRequestTableField wrtf= getWorkflowRequestField(workflowid,-1,Util.getIntValue(nodeid),defieldid,fieldname,defieldvalue, fieldhtmltype, type, fielddbtype,fieldlabel,fieldformname,fieldorder,user.getLanguage(),
											isview,isedit,ismand,user, new HashMap(), new ArrayList(), flagDefault,groupid,selfieldsadd ,changefieldsadd);
									detailFieldMap.put(defieldid, wrtf);
				         		  }catch(Exception e){}
							}
		             }
				 }

			}else{//老表单

				rs.executeProc("Workflow_formdetailinfo_Sel",formid+"");
				while(rs.next()){
					rowCalItemStr1 = Util.null2String(rs.getString("rowCalStr"));
					colCalItemStr1 = Util.null2String(rs.getString("colCalStr"));
					mainCalStr1 = Util.null2String(rs.getString("mainCalStr"));
				}
				rs.execute("select distinct groupId from Workflow_formfield where formid="+formid+" and isdetail='1' order by groupid");
				Integer language_id = new Integer(user.getLanguage());
                while (rs.next()){
                	int _groupid=rs.getInt(1);
                	if(_groupid!=groupid)	continue;
                	 rs2.executeProc("Workflow_formdetailfield_Sel",""+formid+Util.getSeparator()+nodeid+Util.getSeparator()+_groupid);
                     while (rs2.next()) {
                    	 if(language_id.toString().equals(Util.null2String(rs2.getString("langurageid")))){
                    		  String fieldid = rs2.getString("fieldid");
                    		  String fieldlable = rs2.getString("fieldlable");
                    		  String fieldhtmltype = rs2.getString("fieldhtmltype");
                    		  String type = rs2.getString("type");
                    		  String isview = rs2.getString("isview");
                    		  String isedit = rs2.getString("isedit");
                    		  String ismand = rs2.getString("ismandatory");
                    		  String fieldname = rs2.getString("fieldname");
                    		  String fielddbtype = rs2.getString("fielddbtype");
                    		  String childfieldid = rs2.getString("childfieldid");
                    		  int fieldorder = rs2.getInt("fieldorder");
                    		  if("1".equals(isview)){
	                    		  String fieldformname =  "field"+fieldid+"_$rowIndex$";
	                    		  try{
	                    			  String defieldvalue="";
					         			boolean flagToValue = false;
											//判断是否需要处理节点前附加操作
											if(getPreAddRule_hs != null){
												int fieldIndex = inoperatefields.indexOf(fieldid);
												if(fieldIndex > -1){
													flagToValue = true;
													defieldvalue = (String)inoperatevalues.get(fieldIndex);
												}
										}
										//标识是否给字段赋默认值，仅当是新建请求且没有节点前附加操作时。

										boolean flagDefault =  !flagToValue; 
	                    			 WorkflowRequestTableField  wrtf= getWorkflowRequestField(workflowid,-1,Util.getIntValue(nodeid) , fieldid,fieldname,defieldvalue, fieldhtmltype, type, fielddbtype,fieldlable,fieldformname,fieldorder,user.getLanguage(),
	  										isview,isedit,ismand,user, new HashMap(), new ArrayList(),flagDefault,groupid,selfieldsadd ,changefieldsadd);
	                    			 detailFieldMap.put(fieldid, wrtf);
	                    		  }catch(Exception e){}
                    		  }
 						 }
                     }
                }
			}
			
		}
		return detailFieldMap;
	}
	
	/**
	 * liuzy 根据字段ID取得字段小数位精度

	 */
	public int getDecimaldigitsById(String isBill, String fieldid) {
		RecordSet RecordSet = new RecordSet();
		if ("0".equals(isBill)) {
			RecordSet.executeSql("select fielddbtype from workflow_formdict where id="+ fieldid);
		} else {
			RecordSet.executeSql("select fielddbtype from workflow_billfield where id="+ fieldid);
		}
		int decimaldigits_t = 2;
		if("oracle".equals(RecordSet.getDBType())){
			if (RecordSet.next()) {
				String fielddbtypeStr = RecordSet.getString("fielddbtype");
				if (fielddbtypeStr.indexOf("number") >= 0) {
					int digitsIndex = fielddbtypeStr.indexOf(",");
					if(digitsIndex > -1){
						decimaldigits_t = Util.getIntValue(fielddbtypeStr.substring(digitsIndex + 1, fielddbtypeStr.length() - 1), 2);
					}
				} else {
					if(fielddbtypeStr.equals("integer")){
						decimaldigits_t = 0;
					}
				}
			}
		}else{
			if (RecordSet.next()) {
				String fielddbtypeStr = RecordSet.getString("fielddbtype");
				if (fielddbtypeStr.indexOf("decimal") >= 0) {
					int digitsIndex = fielddbtypeStr.indexOf(",");
					if(digitsIndex > -1){
						decimaldigits_t = Util.getIntValue(fielddbtypeStr.substring(digitsIndex + 1, fielddbtypeStr.length() - 1), 2);
					}
				} else {
					if("int".equals(fielddbtypeStr)){
						decimaldigits_t = 0;
					}
				}
			}
		}
		return decimaldigits_t;
	}

	/**
	 * 手机必填标示换成图片
	 */
	private static String getRequiredMark(){
		return "<img src=\"/mobile/plugin/1/images/BacoErrorM_wev8.png\" class=\"requireicon\" align=\"absmiddle\" />";
	}
}