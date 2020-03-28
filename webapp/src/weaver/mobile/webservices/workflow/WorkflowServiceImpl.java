package weaver.mobile.webservices.workflow;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import weaver.common.DateUtil;
import weaver.common.StringUtil;
import weaver.conn.RecordSet;
import weaver.crm.Maint.CustomerInfoComInfo;
import weaver.docs.docs.DocManager;
import weaver.docs.webservices.DocAttachment;
import weaver.file.Prop;
import weaver.formmode.ThreadLocalUser;
import weaver.general.BaseBean;
import weaver.general.TimeUtil;
import weaver.general.Util;
import weaver.hrm.User;
import weaver.hrm.attendance.manager.HrmPaidLeaveTimeManager;
import weaver.hrm.company.DepartmentComInfo;
import weaver.hrm.report.schedulediff.HrmScheduleDiffUtil;
import weaver.hrm.resource.ResourceComInfo;
import weaver.hrm.schedule.domain.HrmLeaveDay;
import weaver.hrm.schedule.manager.HrmScheduleManager;
import weaver.mobile.webservices.common.BrowserAction;
import weaver.mobile.webservices.common.ChatResourceShareManager;
import weaver.mobile.webservices.common.HtmlUtil;
import weaver.mobile.webservices.workflow.bill.BillManager;
import weaver.mobile.webservices.workflow.soa.Cell;
import weaver.mobile.webservices.workflow.soa.DetailTable;
import weaver.mobile.webservices.workflow.soa.DetailTableInfo;
import weaver.mobile.webservices.workflow.soa.Log;
import weaver.mobile.webservices.workflow.soa.MainTableInfo;
import weaver.mobile.webservices.workflow.soa.Property;
import weaver.mobile.webservices.workflow.soa.RequestInfo;
import weaver.mobile.webservices.workflow.soa.RequestPreProcessing;
import weaver.mobile.webservices.workflow.soa.RequestService;
import weaver.mobile.webservices.workflow.soa.Row;
import weaver.mobile.webservices.workflow.soa.WorkFlowInit;
import weaver.share.ShareManager;
import weaver.systeminfo.SystemEnv;
import weaver.workflow.datainput.DynamicDataInput;
import weaver.workflow.exceldesign.ExcelLayoutManager;
import weaver.workflow.html.FieldAttrManager;
import weaver.workflow.mode.FieldInfo;
import weaver.workflow.request.MailAndMessage;
import weaver.workflow.request.RequestCheckUser;
import weaver.workflow.request.RequestComInfo;
import weaver.workflow.request.RequestDoc;
import weaver.workflow.request.RequestLogOperateName;
import weaver.workflow.request.RequestPreAddinoperateManager;
import weaver.workflow.request.RequestRemarkRight;
import weaver.workflow.request.RequestSignatureManager;
import weaver.workflow.request.RequestUseTempletManager;
import weaver.workflow.request.WFCoadjutantManager;
import weaver.workflow.request.WFForwardManager;
import weaver.workflow.request.WFLinkInfo;
import weaver.workflow.request.WFPathUtil;
import weaver.workflow.request.WorkflowSpeechAppend;
import weaver.workflow.workflow.WFManager;
import weaver.workflow.workflow.WFNodeFieldManager;
import weaver.workflow.workflow.WorkTypeComInfo;
import weaver.workflow.workflow.WorkflowAllComInfo;
import weaver.workflow.workflow.WorkflowAllEComInfo;
import weaver.workflow.workflow.WorkflowComInfo;
import weaver.workflow.workflow.WorkflowVersion;
import weaver.workflow.monitor.Monitor;
import weaver.workflow.request.WFUrgerManager;
import weaver.workflow.request.WFWorkflows;
import weaver.workflow.request.WFWorkflowTypes;

public class WorkflowServiceImpl extends BaseBean implements WorkflowService {
	private static final org.apache.commons.logging.Log log = LogFactory.getLog(WorkflowServiceImpl.class);
	//private static ThreadLocal wsiConn = new ThreadLocal();
	private RequestService requestService = new RequestService();
	
	/**
	 * 该方法为兼容老版本客户端，新版本请调用








	 * public WorkflowRequestInfo getWorkflowRequest(int requestid, int userid,int fromrequestid, int pagesize)
	 * 此方法推后两个版本后删除
 	 */
	public WorkflowRequestInfo getWorkflowRequest(int requestid, int userid,int fromrequestid) {
		try {
			return getFromRequestInfo(String.valueOf(requestid), userid,fromrequestid, 10, false, null);
		} catch (Exception e) {
			log.error("Catch a exception.", e);
		}
		return null;
	}

	/**
	 * 删除流程
	 * @param requestid
	 * @return
	 */
	public boolean deleteRequest(int requestid,int userid) {
		try {
			return requestService.deleteRequest(requestid);
		} catch (Exception e) {
			log.error("Catch a exception.", e);
			return false;
		}
	}
	
	public WorkflowRequestInfo getWorkflowRequest4split(int requestid, int userid,int fromrequestid, int pagesize) {
		return getWorkflowRequest4split(requestid, userid,fromrequestid, pagesize, false);
	}
    public WorkflowRequestInfo getWorkflowRequest4splitmonitor(int requestid, int userid, int fromrequestid, int pagesize) {
        return getWorkflowRequest4splitmonitor(requestid, userid, fromrequestid, pagesize, false);
    }
	/**
	 * 将RequestInfo转换为WorkflowRequestInfo
	 * requestid        请求ID号








	 * userid           用户ID号








	 * fromrequestid    相关请求ID号








	 * pagesize         签字意见加载单面显示条数
	 * isPreLoad        是否为预加载
	 * @param RequestInfo
	 * @return WorkflowRequestInfo
	 */
	public WorkflowRequestInfo getWorkflowRequest4split(int requestid, int userid,int fromrequestid, int pagesize, boolean isPreLoad) {
		try {
			return getWorkflowRequest4split(requestid, userid, fromrequestid, pagesize, isPreLoad, null);
		} catch (Exception e) {
			log.error("Catch a exception.", e);
		}
		return null;
	}
    public WorkflowRequestInfo getWorkflowRequest4splitmonitor(int requestid, int userid, int fromrequestid, int pagesize, boolean isPreLoad) {
        try {
            return getWorkflowRequest4splitmonitor(requestid, userid, fromrequestid, pagesize, isPreLoad, null);
        } catch (Exception e) {
            log.error("Catch a exception.", e);
        }
        return null;
    }

	/**
     * 将RequestInfo转换为WorkflowRequestInfo
     * requestid        请求ID号







     * userid           用户ID号







     * fromrequestid    相关请求ID号







     * pagesize         签字意见加载单面显示条数
     * isPreLoad        是否为预加载
     * @param otherinfo 其他信息
     * @param RequestInfo
     * @return WorkflowRequestInfo
     */
    public WorkflowRequestInfo getWorkflowRequest4split(int requestid, int userid, int fromrequestid, int pagesize, boolean isPreLoad, Map<String, Object> otherinfo) {
        try {
            return getFromRequestInfo(String.valueOf(requestid), userid,fromrequestid, pagesize, isPreLoad, otherinfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    /**
     * 将RequestInfo转换为WorkflowRequestInfo requestid 请求ID号



     * 
     * 
     * 
     * 
     * userid 用户ID号



     * 
     * 
     * 
     * 
     * fromrequestid 相关请求ID号



     * 
     * 
     * 
     * 
     * pagesize 签字意见加载单面显示条数 isPreLoad 是否为预加载
     * 
     * @param otherinfo
     *            其他信息
     * @param RequestInfo
     * @return WorkflowRequestInfo
     */
    public WorkflowRequestInfo getWorkflowRequest4splitmonitor(int requestid, int userid, int fromrequestid, int pagesize, boolean isPreLoad, Map<String, Object> otherinfo) {
        try {
            return getFromRequestInfomonitor(String.valueOf(requestid), userid, fromrequestid, pagesize, isPreLoad, otherinfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String doCreateWorkflowRequest(WorkflowRequestInfo wri, int userid, String remark, String clientType) {

        return doCreateWorkflowRequest(wri, userid, remark, clientType, "");
    }

    public String doCreateWorkflowRequest(WorkflowRequestInfo wri, int userid, String remark, String clientType, String src) {
        return doCreateWorkflowRequest(wri, userid, remark, clientType, src, new HashMap<String, String>());
    }

    public String doCreateWorkflowRequest(WorkflowRequestInfo wri, int userid, String remark, String clientType, String src, Map<String, String> params) {
        try {
            RequestInfo ri = toRequestInfo(wri, userid);
            ri.setLanguageid(wri.getLanguageid());
            WorkflowRequestBean wfRstObj = new WorkflowRequestBean();
            wfRstObj.setRemark(remark);
            wfRstObj.setUserID(userid);
            wfRstObj.setClientType(clientType);
            wfRstObj.setHandWrittenSign(wri.getHandWrittenSign());
            wfRstObj.setSpeechAttachment(wri.getSpeechAttachment());
            wfRstObj.setSignatureAppendfix(wri.getSignatureAppendfix());
            wfRstObj.setRemarkLocation(wri.getRemarkLocation());
            if (src.equals("submit")) {
                wfRstObj.setNeedBack("");
            } else if (src.equals("subnoback")) {
                wfRstObj.setNeedBack("0");
            } else if (src.equals("subback")) {
                wfRstObj.setNeedBack("1");
            }
            String result = requestService.createRequest(ri, wfRstObj, params); 
            wri.setRequestId(ri.getRequestid());
            wri.setMessageid(ri.getMessageid());
            wri.setMessagecontent(ri.getMessagecontent());
            return result;
        } catch (Exception e) {
            log.error("Catch a exception.", e);
        }
        return null;
    }

    /**
     * 供Mobile-3.0、4.0调用。



     * 
     * 
     * 
     * 
     * 
     */
    public String forwardWorkflowRequest(int requestid, String forwardReceiver, String remark, int userid, String clientip, String clientType) {
        return forwardWorkflowRequest(requestid, forwardReceiver, remark, userid, clientip, clientType, 0, 0, null);
    }
    
    public String forwardWorkflowRequest(int requestid, String forwardReceiver, String remark, int userid, String clientip, String clientType, int handWrittenSign, int speechAttachment, String signatureAppendfix) {
        return forwardWorkflowRequest(requestid, forwardReceiver, remark, userid, clientip, clientType, 0, 0, null,"");
    }

    /**
     * 供Mobile-4.5调用。



     * 
     * 
     * 
     * 
     * 
     */

	  public String forwardWorkflowRequest(int requestid, String forwardReceiver, String remark, int userid, String clientip, String clientType, int handWrittenSign, int speechAttachment, String signatureAppendfix,String remarkLocation) {
        return forwardWorkflowRequest(requestid, forwardReceiver, remark, userid, clientip, clientType, 0, 0, null,remarkLocation,null);
    }

    public String forwardWorkflowRequest(int requestid, String forwardReceiver, String remark, int userid, String clientip, String clientType, int handWrittenSign, int speechAttachment, String signatureAppendfix,String remarkLocation,String module) {
        try {
            WorkflowRequestBean wfRstObj = new WorkflowRequestBean();
            wfRstObj.setRemark(remark);
            wfRstObj.setUserID(userid);
            wfRstObj.setClientIP(clientip);
            wfRstObj.setRequestID(requestid);
            wfRstObj.setForwardReceiver(forwardReceiver);
            wfRstObj.setClientType(clientType);
            wfRstObj.setHandWrittenSign(handWrittenSign);
            wfRstObj.setSpeechAttachment(speechAttachment);
            signatureAppendfix = Util.null2String(signatureAppendfix);
            wfRstObj.setSignatureAppendfix(signatureAppendfix);
            wfRstObj.setRemarkLocation(remarkLocation);
            if (requestService.forwardFlow(wfRstObj,module))
                return "success";
            else
                return "failed";
        } catch (Exception e) {
            log.error("Catch a exception.", e);
        }
        return "error";
    }


	/**
	 * 供Mobile-3.0、4.0调用。








	 */
	public String forwardWorkflowRequest(int requestid, String forwardReceiver, String remark, int userid, String clientip, String clientType,int forwardflag) {
		return forwardWorkflowRequest(requestid, forwardReceiver, remark, userid, clientip, clientType,forwardflag, 0, 0, null);
	}
    public String forwardWorkflowRequest(int requestid, String forwardReceiver, String remark, int userid, String clientip, String clientType, int forwardflag, int handWrittenSign, int speechAttachment, String signatureAppendfix) {
        return forwardWorkflowRequest(requestid, forwardReceiver, remark, userid, clientip, clientType, forwardflag, 0, 0, null,"");
    }

		/**
	 * 供Mobile-4.5调用。








	 */
    public String forwardWorkflowRequest(int requestid, String forwardReceiver, String remark, int userid, String clientip, String clientType, int forwardflag, int handWrittenSign, int speechAttachment, String signatureAppendfix,String remarkLocation) {
		try {
	      WorkflowRequestBean wfRstObj = new WorkflowRequestBean();
	      wfRstObj.setRemark(remark);
	      wfRstObj.setUserID(userid);
	      wfRstObj.setClientIP(clientip);
	      wfRstObj.setRequestID(requestid);
	      wfRstObj.setForwardReceiver(forwardReceiver);
	      wfRstObj.setClientType(clientType);
		  wfRstObj.setForwardflag(forwardflag);
	      wfRstObj.setHandWrittenSign(handWrittenSign);
	      wfRstObj.setSpeechAttachment(speechAttachment);
	      signatureAppendfix = Util.null2String(signatureAppendfix);
	      wfRstObj.setSignatureAppendfix(signatureAppendfix);
            wfRstObj.setRemarkLocation(remarkLocation);
			if (requestService.forwardFlow(wfRstObj))
				return "success";
			else
				return "failed";
		} catch (Exception e) {
			log.error("Catch a exception.", e);
		}
		return "error";
	}
	
	public String submitWorkflowRequest(WorkflowRequestInfo wri, int requestid, int userid, String type, String remark, String clientType) {
		return submitWorkflowRequest(wri,requestid,userid,type,remark,clientType,new HashMap<String, String>());
	}

	public String submitWorkflowRequest(WorkflowRequestInfo wri, int requestid, int userid, String type, String remark, String clientType,Map<String, String> params) {
		String result = "error";
		try {
			RequestInfo ri = this.toRequestInfo(wri, userid);
			ri.setLanguageid(wri.getLanguageid());
			WorkflowRequestBean wfResObj = new WorkflowRequestBean();
			wfResObj.setRequestID(requestid);
			wfResObj.setUserID(userid);
			wfResObj.setRemark(remark);
			wfResObj.setRejectToNodeId(wri.getRejectToNodeid());
			wfResObj.setSubmitToNodeId(wri.getSubmitToNodeid());
			wfResObj.setClientType(clientType);
			wfResObj.setHandWrittenSign(wri.getHandWrittenSign());
			wfResObj.setSpeechAttachment(wri.getSpeechAttachment());
			wfResObj.setSignatureAppendfix(wri.getSignatureAppendfix());
            wfResObj.setRemarkLocation(wri.getRemarkLocation());
            wfResObj.setLanguageid(wri.getLanguageid());
			wfResObj.setRejectToType(wri.getRejcetToType());
			
            if (type.equals("submit") || type.equals("subnoback") || type.equals("subback") || type.equals("supervise") || type.equals("intervenor") ) {
                if (type.equals("submit"))
                    wfResObj.setNeedBack("");
                else if (type.equals("subnoback"))
                    wfResObj.setNeedBack("0");
                else if (type.equals("subback"))
                    wfResObj.setNeedBack("1");

                wfResObj.set_eh_operatorMap(wri.getEh_operatorMap());
                
                if (type.equals("supervise")){
                result = requestService.nextNodeBySupervise_retStr(ri, wfResObj, params);  //督办提交
                }else if(type.equals("intervenor")){
                result = requestService.nextNodeByIntervenor_retStr(ri, wfResObj, params);  //干预提交   
                }else{
                result = requestService.nextNodeBySubmit_retStr(ri, wfResObj, params);
                }
                
            } else if (type.equals("reject")) {
                result = requestService.nextNodeByRejectToMobile_retStr(ri, wfResObj);
            }
            
            wri.setMessageid(ri.getMessageid());
            wri.setMessagecontent(ri.getMessagecontent());
            
            if(StringUtil.isNull(wri.getMessageid())){
                wri.setMessageid(wfResObj.getMessageid());
                wri.setMessagecontent(wfResObj.getMessagecontent());
            }

            if (!"success".equals(result)) {
                String mobileSuffix = WorkflowSpeechAppend.getMobileSuffix(remark);
                if (mobileSuffix != null && !"".equals(mobileSuffix)) {
                    remark = remark.substring(0, remark.lastIndexOf(mobileSuffix));
                    int usertype = (getUser(userid).getLogintype()).equals("1") ? 0 : 1;
                    RecordSet rs = new RecordSet();
                    rs.executeSql("update workflow_requestlog set remark='" + remark + "' where requestid='" + requestid + "' and operator='" + userid + "' and operatortype='" + usertype + "' and logtype='1'");
                }
            }
        } catch (Exception e) {
            log.error("Catch a exception.", e);
        }
        return result;
    }

    /**
     * 已办事宜
     */
	public int getHendledWorkflowRequestCount(int userid, String[] conditions) {
	    return getHendledWorkflowRequestCount(userid, false, conditions);
	}
	
	/**
     * 已办事宜
     */
	public WorkflowRequestInfo[] getHendledWorkflowRequestList(int pageNo, int pageSize, int recordCount, int userid, String[] conditions) {
	    return getHendledWorkflowRequestList(false, pageNo, pageSize, recordCount, userid, conditions);
	}
	
	/**
	 * 已办事宜（可选择是否包含办结）







	 * @param userid
	 * @param hasShowProcessed
	 * @param conditions
	 * @return
	 */
	public int getHendledWorkflowRequestCount(int userid, boolean hasShowProcessed, String[] conditions) {
		String select = " select distinct ";
		String fields = " t1.createdate,t1.createtime,t1.creater,t1.currentnodeid,t1.currentnodetype,t1.lastoperator,t1.creatertype,t1.lastoperatortype,t1.lastoperatedate,t1.lastoperatetime,t1.requestid,t1.requestname,t1.requestlevel,t1.workflowid,t2.receivedate,t2.receivetime ";
		String from = " from workflow_requestbase t1,workflow_currentoperator t2 ";
		String where = " where t1.requestid=t2.requestid ";
		where += " and t2.usertype = 0 and t2.userid = " + userid;
		where += " and (t2.isremark in('2','4') or (t2.isremark='0' and t2.takisremark ='-2')) ";
		
		if (!hasShowProcessed) {
		    where += " and t2.iscomplete=0 ";
		}
		where += " and  t2.islasttimes=1 ";
		where += " and t1.workflowID in(select id from workflow_base where isvalid in('1','3')) ";
		log.info("Follow is the 'where' condition SQL:\n" + where);
		if (conditions != null)
			for (int m=0;m<conditions.length;m++) {
				String condition = conditions[m];
				where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
			}

		String sql = getPaginationCountSql(select, fields, from, where);

		return getWorkflowRequestCount(sql);
	}

    /**
     * 已办事宜（可选择是否包含办结）


     * (多账号统一显示)
     * @param userid
     * @param hasShowProcessed
     * @param conditions
     * @return
     */
    public int getHendledWorkflowRequestCount(int userid, boolean hasShowProcessed, String[] conditions,boolean belongtoshowFlag) {
        String select = " select distinct ";
        String fields = " t1.createdate,t1.createtime,t1.creater,t1.currentnodeid,t1.currentnodetype,t1.lastoperator,t1.creatertype,t1.lastoperatortype,t1.lastoperatedate,t1.lastoperatetime,t1.requestid,t1.requestname,t1.requestlevel,t1.workflowid,t2.receivedate,t2.receivetime,t2.userid ";
        String from = " from workflow_requestbase t1,workflow_currentoperator t2 ";
        String where = " where t1.requestid=t2.requestid ";
      //当显示次账号的时候


        if(belongtoshowFlag){
            String belongtoIdsql = getsqlByBelongtoId(userid);
            where += " and t2.usertype = 0 and (t2.userid = " + userid + belongtoIdsql + ") ";
        }else{
            where += " and t2.usertype = 0 and t2.userid = " + userid;
        }
        where += " and (t2.isremark in('2','4') or (t2.isremark='0' and t2.takisremark ='-2')) ";
        
        if (!hasShowProcessed) {
            where += " and t2.iscomplete=0 ";
        }
        where += " and  t2.islasttimes=1 ";
        where += " and t1.workflowID in(select id from workflow_base where isvalid in('1','3')) ";
        log.info("Follow is the 'where' condition SQL:\n" + where);
        if (conditions != null)
            for (int m=0;m<conditions.length;m++) {
                String condition = conditions[m];
                where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
            }

        String sql = getPaginationCountSql(select, fields, from, where);

        return getWorkflowRequestCount(sql);
    }

	/**
     * 已办事宜（可选择是否包含办结）







     */
    public WorkflowRequestInfo[] getHendledWorkflowRequestList(boolean hasShowProcessed, int pageNo, int pageSize, int recordCount, int userid, String[] conditions) {
        return getHendledWorkflowRequestList(hasShowProcessed, pageNo, pageSize, recordCount, userid, conditions, 0);
    }
	
	/**
     * 已办事宜（可选择是否包含办结）







     */
    public WorkflowRequestInfo[] getHendledWorkflowRequestList(boolean hasShowProcessed, int pageNo, int pageSize, int recordCount, int userid, String[] conditions, int orderflag) {
		if (pageNo < 1)
			pageNo = 1;
		String select = " select distinct ";
		String fields = " t1.createdate,t1.createtime,t1.creater,t1.currentnodeid,t1.currentnodetype,t1.lastoperator,t1.creatertype,t1.lastoperatortype,t1.lastoperatedate,t1.lastoperatetime,t1.requestid,t1.requestname,t1.requestlevel,t1.workflowid,t2.receivedate,t2.receivetime , t2.isremark, t2.nodeid, t1.formsignaturemd5 , t1.requestnamenew, t1.status ";
		String from = " from workflow_requestbase t1,workflow_currentoperator t2 ";
		String where = " where t1.requestid=t2.requestid ";
		where += " and t2.usertype = 0 and t2.userid = " + userid;
		where += " and (t2.isremark in('2','4') or (t2.isremark='0' and t2.takisremark ='-2')) ";
		if (!hasShowProcessed) {
            where += " and t2.iscomplete=0 ";
        }
        where += " and  t2.islasttimes=1 ";
		where += " and t1.workflowID in(select id from workflow_base where isvalid in('1','3')) ";
		log.info("Follow is the 'where' condition SQL:\n" + where);
		if (conditions != null)
			for (int m=0;m<conditions.length;m++) {
				String condition = conditions[m];
				where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
			}
		String orderby = " order by t2.receivedate desc,t2.receivetime desc,t1.requestid desc ";
		String orderby1 = " order by receivedate asc,receivetime asc,requestid asc ";
		String orderby2 = " order by receivedate desc,receivetime desc,requestid desc ";
		//1：正序






        if (orderflag == 1) {
            orderby = " order by t2.receivedate asc,t2.receivetime asc,t1.requestid asc ";
            orderby1 = " order by receivedate desc,receivetime desc,requestid desc ";
            orderby2 = " order by receivedate asc,receivetime asc,requestid asc ";
        }
		String sql = getPaginationSql(select, fields, from, where, orderby, orderby1, orderby2, pageNo, pageSize, recordCount);

		return getWorkflowRequestList(sql, userid);
	}
    /**
     * 已办事宜（可选择是否包含办结）


     * (多账号统一显示)
     */
    public WorkflowRequestInfo[] getHendledWorkflowRequestList(boolean hasShowProcessed, int pageNo, int pageSize, int recordCount, int userid, String[] conditions, int orderflag,boolean belongtoshowFlag) {
        if (pageNo < 1)
            pageNo = 1;
        String select = " select distinct ";
        String fields = " t1.createdate,t1.createtime,t1.creater,t1.currentnodeid,t1.currentnodetype,t1.lastoperator,t1.creatertype,t1.lastoperatortype,t1.lastoperatedate,t1.lastoperatetime,t1.requestid,t1.requestname,t1.requestlevel,t1.workflowid,t2.receivedate,t2.receivetime , t2.isremark, t2.nodeid, t1.formsignaturemd5 , t1.requestnamenew, t1.status ";


        //当主次账号统一显示的时候,获取用户名和用户类型
        if(belongtoshowFlag){
            fields +=",t2.userid ,t2.usertype";
        }
        
        String from = " from workflow_requestbase t1,workflow_currentoperator t2 ";
        String where = " where t1.requestid=t2.requestid ";
        //当显示次账号的时候


        if(belongtoshowFlag){
            String belongtoIdsql = getsqlByBelongtoId(userid);
            where += " and t2.usertype = 0 and (t2.userid = " + userid + belongtoIdsql + ") ";
        }else{
            where += " and t2.usertype = 0 and t2.userid = " + userid;
        }
        where += " and (t2.isremark in('2','4') or (t2.isremark='0' and t2.takisremark ='-2')) ";
        if (!hasShowProcessed) {
            where += " and t2.iscomplete=0 ";
        }
        where += " and  t2.islasttimes=1 ";
        where += " and t1.workflowID in(select id from workflow_base where isvalid in('1','3')) ";
        log.info("Follow is the 'where' condition SQL:\n" + where);
        if (conditions != null)
            for (int m=0;m<conditions.length;m++) {
                String condition = conditions[m];
                where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
            }
        String orderby = " order by t2.receivedate desc,t2.receivetime desc,t1.requestid desc ";
        String orderby1 = " order by receivedate asc,receivetime asc,requestid asc ";
        String orderby2 = " order by receivedate desc,receivetime desc,requestid desc ";
        //1：正序






        if (orderflag == 1) {
            orderby = " order by t2.receivedate asc,t2.receivetime asc,t1.requestid asc ";
            orderby1 = " order by receivedate desc,receivetime desc,requestid desc ";
            orderby2 = " order by receivedate asc,receivetime asc,requestid asc ";
        }
        String sql = getPaginationSql(select, fields, from, where, orderby, orderby1, orderby2, pageNo, pageSize, recordCount);

        return getWorkflowRequestList(sql, userid);
    }

	public int getMyWorkflowRequestCount(int userid, String[] conditions) {
		String select = " select distinct ";
		String fields = " t1.createdate,t1.createtime,t1.creater,t1.currentnodeid,t1.currentnodetype,t1.lastoperator,t1.creatertype,t1.lastoperatortype,t1.lastoperatedate,t1.lastoperatetime,t1.requestid,t1.requestname,t1.requestlevel,t1.workflowid,t2.receivedate,t2.receivetime ";
		String from = " from workflow_requestbase t1,workflow_currentoperator t2 ";
		String where = " where t1.requestid=t2.requestid ";
		where += " and (t2.isremark is not null or t2.isremark !='') and t2.usertype = 0 and t2.userid = " + userid;
		where += " and t1.creater = " + userid + " and t1.creatertype = 0 and (t1.deleted=0 or t1.deleted is null) and t2.islasttimes=1 ";
		where += " and t1.workflowID in(select id from workflow_base where isvalid in('1','3')) ";
		log.info("Follow is the 'where' condition SQL:\n" + where);
		if (conditions != null)
			for (int m=0;m<conditions.length;m++) {
				String condition = conditions[m];
				where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
			}

		String sql = getPaginationCountSql(select, fields, from, where);

		return getWorkflowRequestCount(sql);
	}
	/**
	 * (多账号统一显示)
	 */
    public int getMyWorkflowRequestCount(int userid, String[] conditions, boolean belongtoshowFlag) {
        String select = " select distinct ";
        String fields = " t1.createdate,t1.createtime,t1.creater,t1.currentnodeid,t1.currentnodetype,t1.lastoperator,t1.creatertype,t1.lastoperatortype,t1.lastoperatedate,t1.lastoperatetime,t1.requestid,t1.requestname,t1.requestlevel,t1.workflowid,t2.receivedate,t2.receivetime,t2.userid ";
        String from = " from workflow_requestbase t1,workflow_currentoperator t2 ";
        String where = " where t1.requestid=t2.requestid ";
        //当显示次账号的时候


        if(belongtoshowFlag){
            User user = this.getUser(userid);
            String belongtoIds = user.getBelongtoids();
            StringBuffer belongtoIdsql = new StringBuffer();
            StringBuffer belongtoIdcreatesql = new StringBuffer();
            if(StringUtils.isNotEmpty(belongtoIds)){
                String[] belongtoIdArray =  belongtoIds.split(",");
                for(String belongtoId : belongtoIdArray){
                    if(StringUtils.isNotEmpty(belongtoId)){
                        belongtoIdsql.append(" OR t2.userid = " + belongtoId);
                        belongtoIdcreatesql.append(" OR t1.creater = " + belongtoId);
                    }
                }
            }
            where += " and (t2.isremark is not null or t2.isremark !='') and t2.usertype = 0 and (t2.userid = " + userid + belongtoIdsql + ") ";
            where += " and (t1.creater = " + userid + belongtoIdcreatesql + " ) and t1.creatertype = 0 and (t1.deleted=0 or t1.deleted is null) and t2.islasttimes=1 ";
        }else{
            where += " and (t2.isremark is not null or t2.isremark !='') and t2.usertype = 0 and t2.userid = " + userid;
            where += " and t1.creater = " + userid + " and t1.creatertype = 0 and (t1.deleted=0 or t1.deleted is null) and t2.islasttimes=1 ";
        }
        where += " and t1.workflowID in(select id from workflow_base where isvalid in('1','3')) ";
        log.info("Follow is the 'where' condition SQL:\n" + where);
        if (conditions != null)
            for (int m=0;m<conditions.length;m++) {
                String condition = conditions[m];
                where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
            }

        String sql = getPaginationCountSql(select, fields, from, where);

        return getWorkflowRequestCount(sql);
    }

	/**
     * 我的请求
     */
    public WorkflowRequestInfo[] getMyWorkflowRequestList(int pageNo, int pageSize, int recordCount, int userid, String[] conditions) {
        return getMyWorkflowRequestList(pageNo, pageSize, recordCount, userid, conditions, 0);
    }
	
	/**
     * 我的请求
     */
    public WorkflowRequestInfo[] getMyWorkflowRequestList(int pageNo, int pageSize, int recordCount, int userid, String[] conditions, int orderflag) {
		if (pageNo < 1)
			pageNo = 1;
		String select = " select distinct ";
		String fields = " t1.createdate,t1.createtime,t1.creater,t1.currentnodeid,t1.currentnodetype,t1.lastoperator,t1.creatertype,t1.lastoperatortype,t1.lastoperatedate,t1.lastoperatetime,t1.requestid,t1.requestname,t1.requestlevel,t1.workflowid,t2.receivedate,t2.receivetime , t2.isremark, t2.nodeid, t1.formsignaturemd5 , t1.requestnamenew, t1.status ";
		String from = " from workflow_requestbase t1,workflow_currentoperator t2 ";
		String where = " where t1.requestid=t2.requestid ";
		where += " and (t2.isremark is not null or t2.isremark !='') and t2.usertype = 0 and t2.userid = " + userid;
		where += " and t1.creater = " + userid + " and t1.creatertype = 0 and (t1.deleted=0 or t1.deleted is null) and t2.islasttimes=1 ";
		where += " and t1.workflowID in(select id from workflow_base where isvalid in('1','3')) ";
		log.info("Follow is the 'where' condition SQL:\n" + where);
		if (conditions != null)
			for (int m=0;m<conditions.length;m++) {
				String condition = conditions[m];
				where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
			}
		String orderby = " order by t2.receivedate desc,t2.receivetime desc,t1.requestid desc ";
		String orderby1 = " order by receivedate asc,receivetime asc,requestid asc ";
		String orderby2 = " order by receivedate desc,receivetime desc,requestid desc ";

		//1：正序






        if (orderflag == 1) {
            orderby = " order by t2.receivedate asc,t2.receivetime asc,t1.requestid asc ";
            orderby1 = " order by receivedate desc,receivetime desc,requestid desc ";
            orderby2 = " order by receivedate asc,receivetime asc,requestid asc ";
        }
		
		String sql = getPaginationSql(select, fields, from, where, orderby, orderby1, orderby2, pageNo, pageSize, recordCount);

		return getWorkflowRequestList(sql, userid);
	}

    /**
     * 我的请求
     * (多账号统一显示)
     */
    public WorkflowRequestInfo[] getMyWorkflowRequestList(int pageNo, int pageSize, int recordCount, int userid, String[] conditions, int orderflag, boolean belongtoshowFlag) {
        if (pageNo < 1)
            pageNo = 1;
        String select = " select distinct ";
        String fields = " t1.createdate,t1.createtime,t1.creater,t1.currentnodeid,t1.currentnodetype,t1.lastoperator,t1.creatertype,t1.lastoperatortype,t1.lastoperatedate,t1.lastoperatetime,t1.requestid,t1.requestname,t1.requestlevel,t1.workflowid,t2.receivedate,t2.receivetime , t2.isremark, t2.nodeid, t1.formsignaturemd5 , t1.requestnamenew, t1.status ";


        //当主次账号统一显示的时候,获取用户名和用户类型
        if(belongtoshowFlag){
            fields +=",t2.userid ,t2.usertype";
        }
        
        String from = " from workflow_requestbase t1,workflow_currentoperator t2 ";
        String where = " where t1.requestid=t2.requestid ";
      //当显示次账号的时候


        if(belongtoshowFlag){
            User user = this.getUser(userid);
            String belongtoIds = user.getBelongtoids();
            StringBuffer belongtoIdsql = new StringBuffer();
            StringBuffer belongtoIdcreatesql = new StringBuffer();
            if(StringUtils.isNotEmpty(belongtoIds)){
                String[] belongtoIdArray =  belongtoIds.split(",");
                for(String belongtoId : belongtoIdArray){
                    if(StringUtils.isNotEmpty(belongtoId)){
                        belongtoIdsql.append(" OR t2.userid = " + belongtoId);
                        belongtoIdcreatesql.append(" OR t1.creater = " + belongtoId);
                    }
                }
            }
            where += " and (t2.isremark is not null or t2.isremark !='') and t2.usertype = 0 and (t2.userid = " + userid + belongtoIdsql + ") ";
            where += " and (t1.creater = " + userid + belongtoIdcreatesql + " ) and t1.creatertype = 0 and (t1.deleted=0 or t1.deleted is null) and t2.islasttimes=1 ";
        }else{
            where += " and (t2.isremark is not null or t2.isremark !='') and t2.usertype = 0 and t2.userid = " + userid;
            where += " and t1.creater = " + userid + " and t1.creatertype = 0 and (t1.deleted=0 or t1.deleted is null) and t2.islasttimes=1 ";
        }
        where += " and t1.workflowID in(select id from workflow_base where isvalid in('1','3')) ";
        log.info("Follow is the 'where' condition SQL:\n" + where);
        if (conditions != null)
            for (int m=0;m<conditions.length;m++) {
                String condition = conditions[m];
                where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
            }
        String orderby = " order by t2.receivedate desc,t2.receivetime desc,t1.requestid desc ";
        String orderby1 = " order by receivedate asc,receivetime asc,requestid asc ";
        String orderby2 = " order by receivedate desc,receivetime desc,requestid desc ";

        //1：正序






        if (orderflag == 1) {
            orderby = " order by t2.receivedate asc,t2.receivetime asc,t1.requestid asc ";
            orderby1 = " order by receivedate desc,receivetime desc,requestid desc ";
            orderby2 = " order by receivedate asc,receivetime asc,requestid asc ";
        }
        
        String sql = getPaginationSql(select, fields, from, where, orderby, orderby1, orderby2, pageNo, pageSize, recordCount);

        return getWorkflowRequestList(sql, userid);
    }

	public int getProcessedWorkflowRequestCount(int userid, String[] conditions) {
		String select = " select distinct ";
		String fields = " t1.createdate,t1.createtime,t1.creater,t1.currentnodeid,t1.currentnodetype,t1.lastoperator,t1.creatertype,t1.lastoperatortype,t1.lastoperatedate,t1.lastoperatetime,t1.requestid,t1.requestname,t1.requestlevel,t1.workflowid,t2.receivedate,t2.receivetime ";
		String from = " from workflow_requestbase t1,workflow_currentoperator t2 ";
		String where = " where t1.requestid=t2.requestid ";
		where += " and t2.usertype = 0 and t2.userid = " + userid;
		where += " and t2.isremark in('2','4') and t1.currentnodetype = '3' and iscomplete=1 and islasttimes=1 ";
		where += " and t1.workflowID in(select id from workflow_base where isvalid in('1','3')) ";
		log.info("Follow is the 'where' condition SQL:\n" + where);
		if (conditions != null)
			for (int m=0;m<conditions.length;m++) {
				String condition = conditions[m];
				where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
			}

		String sql = getPaginationCountSql(select, fields, from, where);

		return getWorkflowRequestCount(sql);
	}
	/**
	 * (多账号统一显示)
	 */
    public int getProcessedWorkflowRequestCount(int userid, String[] conditions,boolean belongtoshowFlag) {
        String select = " select distinct ";
        String fields = " t1.createdate,t1.createtime,t1.creater,t1.currentnodeid,t1.currentnodetype,t1.lastoperator,t1.creatertype,t1.lastoperatortype,t1.lastoperatedate,t1.lastoperatetime,t1.requestid,t1.requestname,t1.requestlevel,t1.workflowid,t2.receivedate,t2.receivetime,t2.userid ";
        String from = " from workflow_requestbase t1,workflow_currentoperator t2 ";
        String where = " where t1.requestid=t2.requestid ";
        //当显示次账号的时候


        if(belongtoshowFlag){
            String belongtoIdsql = getsqlByBelongtoId(userid);
            where += " and t2.usertype = 0 and (t2.userid = " + userid + belongtoIdsql + ") ";
        }else{
            where += " and t2.usertype = 0 and t2.userid = " + userid;
        }
        where += " and t2.isremark in('2','4') and t1.currentnodetype = '3' and iscomplete=1 and islasttimes=1 ";
        where += " and t1.workflowID in(select id from workflow_base where isvalid in('1','3')) ";
        log.info("Follow is the 'where' condition SQL:\n" + where);
        if (conditions != null)
            for (int m=0;m<conditions.length;m++) {
                String condition = conditions[m];
                where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
            }

        String sql = getPaginationCountSql(select, fields, from, where);

        return getWorkflowRequestCount(sql);
    }

	/**
     * 办结事宜
     */
    public WorkflowRequestInfo[] getProcessedWorkflowRequestList(int pageNo, int pageSize, int recordCount, int userid, String[] conditions) {
        return getProcessedWorkflowRequestList(pageNo, pageSize, recordCount, userid, conditions, 0);
    }
	
        
	/**
	 * 办结事宜
	 */
	public WorkflowRequestInfo[] getProcessedWorkflowRequestList(int pageNo, int pageSize, int recordCount, int userid, String[] conditions, int orderflag) {
		if (pageNo < 1)
			pageNo = 1;
		String select = " select distinct ";
		String fields = " t1.createdate,t1.createtime,t1.creater,t1.currentnodeid,t1.currentnodetype,t1.lastoperator,t1.creatertype,t1.lastoperatortype,t1.lastoperatedate,t1.lastoperatetime,t1.requestid,t1.requestname,t1.requestlevel,t1.workflowid,t2.receivedate,t2.receivetime, t2.isremark, t2.nodeid, t1.formsignaturemd5 , t1.requestnamenew, t1.status ";
		String from = " from workflow_requestbase t1,workflow_currentoperator t2 ";
		String where = " where t1.requestid=t2.requestid ";
		where += " and t2.usertype = 0 and t2.userid = " + userid;
		where += " and t2.isremark in('2','4') and t1.currentnodetype = '3' and iscomplete=1 and islasttimes=1 ";
		where += " and t1.workflowID in(select id from workflow_base where isvalid in('1','3')) ";
		log.info("Follow is the 'where' condition SQL:\n" + where);
		if (conditions != null)
			for (int m=0;m<conditions.length;m++) {
				String condition = conditions[m];
				where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
			}
		String orderby = " order by t2.receivedate desc,t2.receivetime desc,t1.requestid desc ";
		String orderby1 = " order by receivedate asc,receivetime asc,requestid asc ";
		String orderby2 = " order by receivedate desc,receivetime desc,requestid desc ";
		
		//1：正序






        if (orderflag == 1) {
            orderby = " order by t2.receivedate asc,t2.receivetime asc,t1.requestid asc ";
            orderby1 = " order by receivedate desc,receivetime desc,requestid desc ";
            orderby2 = " order by receivedate asc,receivetime asc,requestid asc ";
        }
		
		String sql = getPaginationSql(select, fields, from, where, orderby, orderby1, orderby2, pageNo, pageSize, recordCount);

		return getWorkflowRequestList(sql, userid);
	} 
    /**
     * 办结事宜
     * (多账号统一显示)
     */
    public WorkflowRequestInfo[] getProcessedWorkflowRequestList(int pageNo, int pageSize, int recordCount, int userid, String[] conditions, int orderflag,boolean belongtoshowFlag) {
        if (pageNo < 1)
            pageNo = 1;
        String select = " select distinct ";
        String fields = " t1.createdate,t1.createtime,t1.creater,t1.currentnodeid,t1.currentnodetype,t1.lastoperator,t1.creatertype,t1.lastoperatortype,t1.lastoperatedate,t1.lastoperatetime,t1.requestid,t1.requestname,t1.requestlevel,t1.workflowid,t2.receivedate,t2.receivetime, t2.isremark, t2.nodeid, t1.formsignaturemd5 , t1.requestnamenew, t1.status ";


        //当主次账号统一显示的时候,获取用户名和用户类型
        if(belongtoshowFlag){
            fields +=",t2.userid ,t2.usertype";
        }
        
        String from = " from workflow_requestbase t1,workflow_currentoperator t2 ";
        String where = " where t1.requestid=t2.requestid ";
        //当显示次账号的时候


        if(belongtoshowFlag){
            String belongtoIdsql = getsqlByBelongtoId(userid);
            where += " and t2.usertype = 0 and (t2.userid = " + userid + belongtoIdsql + ") ";
        }else{
            where += " and t2.usertype = 0 and t2.userid = " + userid;
        }
        where += " and t2.isremark in('2','4') and t1.currentnodetype = '3' and iscomplete=1 and islasttimes=1 ";
        where += " and t1.workflowID in(select id from workflow_base where isvalid in('1','3')) ";
        log.info("Follow is the 'where' condition SQL:\n" + where);
        if (conditions != null)
            for (int m=0;m<conditions.length;m++) {
                String condition = conditions[m];
                where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
            }
        String orderby = " order by t2.receivedate desc,t2.receivetime desc,t1.requestid desc ";
        String orderby1 = " order by receivedate asc,receivetime asc,requestid asc ";
        String orderby2 = " order by receivedate desc,receivetime desc,requestid desc ";
        
        //1：正序






        if (orderflag == 1) {
            orderby = " order by t2.receivedate asc,t2.receivetime asc,t1.requestid asc ";
            orderby1 = " order by receivedate desc,receivetime desc,requestid desc ";
            orderby2 = " order by receivedate asc,receivetime asc,requestid asc ";
        }
        
        String sql = getPaginationSql(select, fields, from, where, orderby, orderby1, orderby2, pageNo, pageSize, recordCount);

        return getWorkflowRequestList(sql, userid);
    }

	/**
     * 待办事宜数量(不包括抄送)
     */
    public int getToDoWorkflowRequestCount(int userid, String[] conditions) {
        return getToDoWorkflowRequestCount(userid, false, conditions);
    }
    
    /**
     * 待办事宜信息(不包括抄送)
     */
    public WorkflowRequestInfo[] getToDoWorkflowRequestList(int pageNo, int pageSize, int recordCount, int userid, String[] conditions) {
        return getToDoWorkflowRequestList(false, pageNo, pageSize, recordCount, userid, conditions);
    }
    
	/**
	 * 待办事宜数量(可选择是否包括抄送)
	 */
	public int getToDoWorkflowRequestCount(int userid, boolean hasShowCopy, String[] conditions) {
	    
	    String remark = "'1','5','7'";
	    if (hasShowCopy) {
	        remark += ",'8','9'";
	    }
	    
		String select = " select distinct ";
		String fields = " t1.createdate,t1.createtime,t1.creater,t1.currentnodeid,t1.currentnodetype,t1.lastoperator,t1.creatertype,t1.lastoperatortype,t1.lastoperatedate,t1.lastoperatetime,t1.requestid,t1.requestname,t1.requestlevel,t1.workflowid,t2.receivedate,t2.receivetime ";
		String from = " from workflow_requestbase t1,workflow_currentoperator t2 ";
		String where = " where t1.requestid=t2.requestid ";
		where += " and t2.usertype = 0 and t2.userid = " + userid;
		where += " and ((t2.isremark='0' and (t2.takisremark is null or t2.takisremark='0' )) or t2.isremark in(" + remark +")) and t2.islasttimes=1 ";
		where += " and t1.workflowID in(select id from workflow_base where isvalid in('1','3')) ";
		log.info("Follow is the 'where' condition SQL:\n" + where);
		if (conditions != null)
			for (int m=0;m<conditions.length;m++) {
				String condition = conditions[m];
				where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
			}

		String sql = getPaginationCountSql(select, fields, from, where);

		return getWorkflowRequestCount(sql);
	}
    /**
     * 待办事宜数量(可选择是否包括抄送)
     * (多账号统一显示)
     */
    public int getToDoWorkflowRequestCount(int userid, boolean hasShowCopy, String[] conditions,boolean belongtoshowFlag) {
        
        String remark = "'1','5','7'";
        if (hasShowCopy) {
            remark += ",'8','9'";
        }
        
        String select = " select distinct ";
        String fields = " t1.createdate,t1.createtime,t1.creater,t1.currentnodeid,t1.currentnodetype,t1.lastoperator,t1.creatertype,t1.lastoperatortype,t1.lastoperatedate,t1.lastoperatetime,t1.requestid,t1.requestname,t1.requestlevel,t1.workflowid,t2.receivedate,t2.receivetime,t2.userid ";
        String from = " from workflow_requestbase t1,workflow_currentoperator t2 ";
        String where = " where t1.requestid=t2.requestid ";
//        where += " and t2.usertype = 0 and t2.userid = " + userid;
        //当显示次账号的时候


        if(belongtoshowFlag){
            String belongtoIdsql = getsqlByBelongtoId(userid);
            where += " and t2.usertype = 0 and (t2.userid = " + userid + belongtoIdsql + ") ";
        }else{
            where += " and t2.usertype = 0 and t2.userid = " + userid;
        }
        where += " and ((t2.isremark='0' and (t2.takisremark is null or t2.takisremark='0' )) or t2.isremark in(" + remark +")) and t2.islasttimes=1 ";
        where += " and t1.workflowID in(select id from workflow_base where isvalid in('1','3')) ";
        log.info("Follow is the 'where' condition SQL:\n" + where);
        if (conditions != null)
            for (int m=0;m<conditions.length;m++) {
                String condition = conditions[m];
                where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
            }

        String sql = getPaginationCountSql(select, fields, from, where);

        return getWorkflowRequestCount(sql);
    }

	/**
     * 待办事宜信息(可选择是否包括抄送)
     */
    public WorkflowRequestInfo[] getToDoWorkflowRequestList(boolean hasShowCopy, int pageNo, int pageSize, int recordCount, int userid, String[] conditions) {
        return getToDoWorkflowRequestList(hasShowCopy, pageNo, pageSize, recordCount, userid, conditions, 0);
    }
	
	/**
	 * 待办事宜信息(可选择是否包括抄送)
	 */
	public WorkflowRequestInfo[] getToDoWorkflowRequestList(boolean hasShowCopy, int pageNo, int pageSize, int recordCount, int userid, String[] conditions, int orderflag) {
		if (pageNo < 1)
			pageNo = 1;
		
		String remark = "'1','5','7'";
        if (hasShowCopy) {
            remark += ",'8','9'";
        }
        
		String select = " select distinct ";
		String fields = " t1.createdate,t1.createtime,t1.creater,t1.currentnodeid,t1.currentnodetype,t1.lastoperator,t1.creatertype,t1.lastoperatortype,t1.lastoperatedate,t1.lastoperatetime,t1.requestid,t1.requestname,t1.requestlevel,t1.workflowid,t2.receivedate,t2.receivetime, t2.isremark, t2.nodeid, t1.formsignaturemd5, t1.requestnamenew, t1.status ";
		String from = " from workflow_requestbase t1,workflow_currentoperator t2 ";
		String where = " where t1.requestid=t2.requestid ";
		where += " and t2.usertype = 0 and t2.userid = " + userid;
		where += " and ((t2.isremark='0' and (t2.takisremark is null or t2.takisremark='0' )) or t2.isremark in(" + remark + ")) and t2.islasttimes=1 ";
		where += " and t1.workflowID in(select id from workflow_base where isvalid in('1','3')) ";
		log.info("Follow is the 'where' condition SQL:\n" + where);
		if (conditions != null)
			for (int m=0;m<conditions.length;m++) {
				String condition = conditions[m];
				where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
			}
		
		String orderby = " order by t2.receivedate desc,t2.receivetime desc,t1.requestid desc ";
		String orderby1 = " order by receivedate asc,receivetime asc,requestid asc ";
		String orderby2 = " order by receivedate desc,receivetime desc,requestid desc ";
		//1：正序






		if (orderflag == 1) {
		    orderby = " order by t2.receivedate asc,t2.receivetime asc,t1.requestid asc ";
	        orderby1 = " order by receivedate desc,receivetime desc,requestid desc ";
	        orderby2 = " order by receivedate asc,receivetime asc,requestid asc ";
        }
			
		String sql = getPaginationSql(select, fields, from, where, orderby, orderby1, orderby2, pageNo, pageSize, recordCount);

		return getWorkflowRequestList(sql, userid);
	}

    /**
     * 待办事宜信息(可选择是否包括抄送)
     * (多账号统一显示)
     */
    public WorkflowRequestInfo[] getToDoWorkflowRequestList(boolean hasShowCopy, int pageNo, int pageSize, int recordCount, int userid, String[] conditions, int orderflag,Boolean belongtoshowFlag) {
        if (pageNo < 1)
            pageNo = 1;
        
        String remark = "'1','5','7'";
        if (hasShowCopy) {
            remark += ",'8','9'";
        }
        
        String select = " select distinct ";
        String fields = " t1.createdate,t1.createtime,t1.creater,t1.currentnodeid,t1.currentnodetype,t1.lastoperator,t1.creatertype,t1.lastoperatortype,t1.lastoperatedate,t1.lastoperatetime,t1.requestid,t1.requestname,t1.requestlevel,t1.workflowid,t2.receivedate,t2.receivetime, t2.isremark, t2.nodeid, t1.formsignaturemd5, t1.requestnamenew, t1.status ";

        //当主次账号统一显示的时候,获取用户名和用户类型
        if(belongtoshowFlag){
            fields +=",t2.userid ,t2.usertype";
        }
        String from = " from workflow_requestbase t1,workflow_currentoperator t2 ";
        String where = " where t1.requestid=t2.requestid ";
        //当显示次账号的时候


        if(belongtoshowFlag){
            String belongtoIdsql = getsqlByBelongtoId(userid);
            where += " and t2.usertype = 0 and (t2.userid = " + userid + belongtoIdsql + ") ";
        }else{
            where += " and t2.usertype = 0 and t2.userid = " + userid;
        }
        where += " and ((t2.isremark='0' and (t2.takisremark is null or t2.takisremark='0' )) or t2.isremark in(" + remark + ")) and t2.islasttimes=1 ";
        where += " and t1.workflowID in(select id from workflow_base where isvalid in('1','3')) ";
        log.info("Follow is the 'where' condition SQL:\n" + where);
        if (conditions != null)
            for (int m=0;m<conditions.length;m++) {
                String condition = conditions[m];
                where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
            }
        
        String orderby = " order by t2.receivedate desc,t2.receivetime desc,t1.requestid desc ";
        String orderby1 = " order by receivedate asc,receivetime asc,requestid asc ";
        String orderby2 = " order by receivedate desc,receivetime desc,requestid desc ";
        //1：正序






        if (orderflag == 1) {
            orderby = " order by t2.receivedate asc,t2.receivetime asc,t1.requestid asc ";
            orderby1 = " order by receivedate desc,receivetime desc,requestid desc ";
            orderby2 = " order by receivedate asc,receivetime asc,requestid asc ";
        }
            
        String sql = getPaginationSql(select, fields, from, where, orderby, orderby1, orderby2, pageNo, pageSize, recordCount);

        return getWorkflowRequestList(sql, userid);
    }

	/**
	 * 抄送请求数
	 */
	public int getCCWorkflowRequestCount(int userid, String[] conditions) {
		String select = " select distinct ";
		String fields = " t1.createdate,t1.createtime,t1.creater,t1.currentnodeid,t1.currentnodetype,t1.lastoperator,t1.creatertype,t1.lastoperatortype,t1.lastoperatedate,t1.lastoperatetime,t1.requestid,t1.requestname,t1.requestlevel,t1.workflowid,t2.receivedate,t2.receivetime ";
		String from = " from workflow_requestbase t1,workflow_currentoperator t2 ";
		String where = " where t1.requestid=t2.requestid ";
		where += " and t2.usertype = 0 and t2.userid = " + userid;
		where += " and t2.isremark in( '8','9' ) and t2.islasttimes=1 ";
		where += " and t1.workflowID in(select id from workflow_base where isvalid in('1','3')) ";
		log.info("Follow is the 'where' condition SQL:\n" + where);
		if (conditions != null)
			for (int m=0;m<conditions.length;m++) {
				String condition = conditions[m];
				where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
			}

		String sql = getPaginationCountSql(select, fields, from, where);

		return getWorkflowRequestCount(sql);
	}

    /**
     * 抄送请求数
     * (多账号统一显示)
     */
    public int getCCWorkflowRequestCount(int userid, String[] conditions,boolean belongtoshowFlag) {
        String select = " select distinct ";
        String fields = " t1.createdate,t1.createtime,t1.creater,t1.currentnodeid,t1.currentnodetype,t1.lastoperator,t1.creatertype,t1.lastoperatortype,t1.lastoperatedate,t1.lastoperatetime,t1.requestid,t1.requestname,t1.requestlevel,t1.workflowid,t2.receivedate,t2.receivetime,t2.userid ";
        String from = " from workflow_requestbase t1,workflow_currentoperator t2 ";
        String where = " where t1.requestid=t2.requestid ";
        //当显示次账号的时候


        if(belongtoshowFlag){
            String belongtoIdsql = getsqlByBelongtoId(userid);
            where += " and t2.usertype = 0 and (t2.userid = " + userid + belongtoIdsql + ") ";
        }else{
            where += " and t2.usertype = 0 and t2.userid = " + userid;
        }
        where += " and t2.isremark in( '8','9' ) and t2.islasttimes=1 ";
        where += " and t1.workflowID in(select id from workflow_base where isvalid in('1','3')) ";
        log.info("Follow is the 'where' condition SQL:\n" + where);
        if (conditions != null)
            for (int m=0;m<conditions.length;m++) {
                String condition = conditions[m];
                where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
            }

        String sql = getPaginationCountSql(select, fields, from, where);

        return getWorkflowRequestCount(sql);
    }

	/**
     * 抄送请求信息







     */
    public WorkflowRequestInfo[] getCCWorkflowRequestList(int pageNo, int pageSize, int recordCount, int userid, String[] conditions) {
        return getCCWorkflowRequestList(pageNo, pageSize, recordCount, userid, conditions, 0);
    }
    
	/**
	 * 抄送请求信息







     */
    public WorkflowRequestInfo[] getCCWorkflowRequestList(int pageNo, int pageSize, int recordCount, int userid, String[] conditions, int orderflag) {
        if (pageNo < 1)
            pageNo = 1;
        String select = " select distinct ";
        String fields = " t1.createdate,t1.createtime,t1.creater,t1.currentnodeid,t1.currentnodetype,t1.lastoperator,t1.creatertype,t1.lastoperatortype,t1.lastoperatedate,t1.lastoperatetime,t1.requestid,t1.requestname,t1.requestlevel,t1.workflowid,t2.receivedate,t2.receivetime, t2.isremark, t2.nodeid, t1.formsignaturemd5, t1.requestnamenew, t1.status ";
        String from = " from workflow_requestbase t1,workflow_currentoperator t2 ";
        String where = " where t1.requestid=t2.requestid ";
        where += " and t2.usertype = 0 and t2.userid = " + userid;
        where += " and t2.isremark in( '8','9' ) and t2.islasttimes=1 ";
        where += " and t1.workflowID in(select id from workflow_base where isvalid in('1','3')) ";
        log.info("Follow is the 'where' condition SQL:\n" + where);
        if (conditions != null)
            for (int m=0;m<conditions.length;m++) {
                String condition = conditions[m];
                where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
            }
        String orderby = " order by t2.receivedate desc,t2.receivetime desc,t1.requestid desc ";
        String orderby1 = " order by receivedate asc,receivetime asc,requestid asc ";
        String orderby2 = " order by receivedate desc,receivetime desc,requestid desc ";

        //1：正序






        if (orderflag == 1) {
            orderby = " order by t2.receivedate asc,t2.receivetime asc,t1.requestid asc ";
            orderby1 = " order by receivedate desc,receivetime desc,requestid desc ";
            orderby2 = " order by receivedate asc,receivetime asc,requestid asc ";
        }
        
        String sql = getPaginationSql(select, fields, from, where, orderby, orderby1, orderby2, pageNo, pageSize, recordCount);

        return getWorkflowRequestList(sql, userid);
    }

    /**
     * 抄送请求信息


     * (多账号统一显示)




     */
    public WorkflowRequestInfo[] getCCWorkflowRequestList(int pageNo, int pageSize, int recordCount, int userid, String[] conditions, int orderflag,boolean belongtoshowFlag) {
        if (pageNo < 1)
            pageNo = 1;
        String select = " select distinct ";
        String fields = " t1.createdate,t1.createtime,t1.creater,t1.currentnodeid,t1.currentnodetype,t1.lastoperator,t1.creatertype,t1.lastoperatortype,t1.lastoperatedate,t1.lastoperatetime,t1.requestid,t1.requestname,t1.requestlevel,t1.workflowid,t2.receivedate,t2.receivetime, t2.isremark, t2.nodeid, t1.formsignaturemd5, t1.requestnamenew, t1.status ";


        //当主次账号统一显示的时候,获取用户名和用户类型
        if(belongtoshowFlag){
            fields +=",t2.userid ,t2.usertype";
        }
        
        String from = " from workflow_requestbase t1,workflow_currentoperator t2 ";
        String where = " where t1.requestid=t2.requestid ";
        //当显示次账号的时候


        if(belongtoshowFlag){
            String belongtoIdsql = getsqlByBelongtoId(userid);
            where += " and t2.usertype = 0 and (t2.userid = " + userid + belongtoIdsql + ") ";
        }else{
            where += " and t2.usertype = 0 and t2.userid = " + userid;
        }
        where += " and t2.isremark in( '8','9' ) and t2.islasttimes=1 ";
        where += " and t1.workflowID in(select id from workflow_base where isvalid in('1','3')) ";
        log.info("Follow is the 'where' condition SQL:\n" + where);
        if (conditions != null)
            for (int m=0;m<conditions.length;m++) {
                String condition = conditions[m];
                where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
            }
        String orderby = " order by t2.receivedate desc,t2.receivetime desc,t1.requestid desc ";
        String orderby1 = " order by receivedate asc,receivetime asc,requestid asc ";
        String orderby2 = " order by receivedate desc,receivetime desc,requestid desc ";

        //1：正序






        if (orderflag == 1) {
            orderby = " order by t2.receivedate asc,t2.receivetime asc,t1.requestid asc ";
            orderby1 = " order by receivedate desc,receivetime desc,requestid desc ";
            orderby2 = " order by receivedate asc,receivetime asc,requestid asc ";
        }
        
        String sql = getPaginationSql(select, fields, from, where, orderby, orderby1, orderby2, pageNo, pageSize, recordCount);

        return getWorkflowRequestList(sql, userid);
    }
	
	public int getAllWorkflowRequestCount(int userid, String[] conditions) {
		String select = " select distinct ";
		String fields = " t1.createdate,t1.createtime,t1.creater,t1.currentnodeid,t1.currentnodetype,t1.lastoperator,t1.creatertype,t1.lastoperatortype,t1.lastoperatedate,t1.lastoperatetime,t1.requestid,t1.requestname,t1.requestlevel,t1.workflowid,t2.receivedate,t2.receivetime ";
		String from = " from workflow_requestbase t1,workflow_currentoperator t2,workflow_base t3 ";
		String where = " where t1.requestid=t2.requestid ";
		where += " and t3.id=t2.workflowid and t3.isvalid in('1','3') ";
		where += " and t2.usertype = 0 and t2.userid = " + userid;
		where += " and islasttimes=1 and islasttimes=1 ";
		if (conditions != null)
			for (int m=0;m<conditions.length;m++) {
				String condition = conditions[m];
				where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
			}

		String sql = getPaginationCountSql(select, fields, from, where);

		return getWorkflowRequestCount(sql);
	}

	public WorkflowRequestInfo[] getAllWorkflowRequestList(int pageNo, int pageSize, int recordCount, int userid, String[] conditions) {
		if (pageNo < 1)
			pageNo = 1;
		String select = " select distinct ";
		String fields = " t1.createdate,t1.createtime,t1.creater,t1.currentnodeid,t1.currentnodetype,t1.lastoperator,t1.creatertype,t1.lastoperatortype,t1.lastoperatedate,t1.lastoperatetime,t1.requestid,t1.requestname,t1.requestlevel,t1.workflowid,t2.receivedate,t2.receivetime,t1.requestnamenew ";
		String from = " from workflow_requestbase t1,workflow_currentoperator t2,workflow_base t3 ";
		String where = " where t1.requestid=t2.requestid ";
		where += " and t3.id=t2.workflowid and t3.isvalid in('1','3') ";
		where += " and t2.usertype = 0 and t2.userid = " + userid;
		where += " and islasttimes=1 and islasttimes=1 ";
		if (conditions != null)
			for (int m=0;m<conditions.length;m++) {
				String condition = conditions[m];
				where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
			}
		String orderby = " order by t2.receivedate desc,t2.receivetime desc,t1.requestid desc ";
		String orderby1 = " order by receivedate asc,receivetime asc,requestid asc ";
		String orderby2 = " order by receivedate desc,receivetime desc,requestid desc ";

		String sql = getPaginationSql(select, fields, from, where, orderby, orderby1, orderby2, pageNo, pageSize, recordCount);

		return getWorkflowRequestList(sql, userid);
	}

	/**
	 * 取得分页统计记录数的SQL
	 * @param select sql中select
	 * @param fields sql中fields
	 * @param from sql中from
	 * @param where sql中where
	 * @return String sql分页统计记录数语句








	 */
	private String getPaginationCountSql(String select, String fields, String from, String where) {
		String sql = " select count(*) my_count from ( " + select + " " + fields + " " + from + " " + where + " ) tableA ";
		return sql;
	}

	/**
	 * 取得分页的SQL
	 * @param select sql中select
	 * @param fields sql中fields
	 * @param from sql中from
	 * @param where sql中where
	 * @param orderby sql中orderby
	 * @param orderby1 sql中orderby1
	 * @param orderby2 sql中orderby2
	 * @param pageNo 当前页数
	 * @param pageSize 每页记录数








	 * @param recordCount 记录总数
	 * @return String 分页SQL
	 */
	private String getPaginationSql(String select, String fields, String from, String where, String orderby, String orderby1, String orderby2, int pageNo, int pageSize, int recordCount) {
		String sql = "";
		RecordSet rs = new RecordSet();
		int firstResult = 0;
		int endResult = 0;
		if (rs.getDBType().equals("oracle")) {
			firstResult = pageNo * pageSize + 1;
			endResult = (pageNo - 1) * pageSize;
			sql = " select * from ( select my_table.*,rownum as my_rownum from ( select tableA.*,rownum as r from ( " + select + " " + fields + " " + from + " " + where + " " + orderby + " ) tableA  ) my_table where rownum < " + firstResult + " ) where my_rownum > " + endResult;
		} else {
			firstResult = pageSize * pageNo;
			endResult = pageSize;
			if (firstResult > recordCount) {
				firstResult = recordCount;
				endResult = recordCount - (pageSize * (pageNo - 1));
				//对结果集记录数进行判断，如果小于0则设为0。








				if(endResult < 0){
					endResult = 0;
				}
			}
			if (pageNo == 1)
				sql = select + " top " + endResult + " " + fields + " " + from + " " + where + " " + orderby;
			else
				sql = " select top " + endResult + " * from ( select top " + endResult + " * from ( " + select + " top " + firstResult + " " + fields + " " + from + " " + where + " " + orderby + " " + ") tbltemp1 " + orderby1 + " ) tbltemp2 " + orderby2;
		}
		return sql;
	}

	/**
	 * 取得sql中的记录数








	 * @param sql sql语句
	 * @return int 记录数








	 */
	private int getWorkflowRequestCount(String sql) {
		RecordSet rs = new RecordSet();
		int count = 0;
		try {
			rs.executeSql(sql);
			if (rs.next()) {
				count = rs.getInt("my_count");
			}
		} catch (Exception e) {
			e.printStackTrace();
			writeLog(e);
		}
		return count;
	}

	/**
	 * 取得sql中的流程记录
	 * @param sql sql语句
	 * @return WorkflowRequestInfo[] 流程记录
	 */
	private WorkflowRequestInfo[] getWorkflowRequestList(String sql, int userID) {
		RecordSet rs = new RecordSet();
		RecordSet rs1 = new RecordSet();

		int userLanguageID = 7;
		StringBuffer sbSQL = new StringBuffer(100);
		sbSQL.append("  Select systemlanguage from (                                       \n");
		sbSQL.append("    SELECT id,systemlanguage FROM HrmResourceManager union all       \n");
		sbSQL.append("    SELECT id,systemlanguage FROM HrmResource) a where a.id = %1$s   \n");
		String sqlGetUserLanguage = sbSQL.toString();
		sqlGetUserLanguage = String.format(sqlGetUserLanguage, userID);
		//log.info("Following is the run sql:\n" + sqlGetUserLanguage);
		rs.execute(sqlGetUserLanguage);
		if(rs.next()){
			userLanguageID = rs.getInt(1);
		}
		
		List wris = new ArrayList();
		try {
			WorkflowAllComInfo WorkflowComInfo = new WorkflowAllComInfo();
			WorkTypeComInfo workflowTypeComInfo = new WorkTypeComInfo();
			ResourceComInfo resourceComInfo = new ResourceComInfo();

			rs.executeSql(sql);
			while (rs.next()) {
				WorkflowRequestInfo wri = new WorkflowRequestInfo();

				String requestID = rs.getString("requestid");
				wri.setRequestId(requestID);
				String requestnamenew = rs.getString("requestnamenew");
				String requestName = rs.getString("requestname");
				wri.setRequestLevel(rs.getString("requestlevel"));
				//添加标签签名信息
				wri.setFormsignaturemd5(Util.null2String(rs.getString("formsignaturemd5")));
				
				WorkflowExtInfo wbi = new WorkflowExtInfo();
				String workflowID = rs.getString("workflowid");
				wbi.setWorkflowId(workflowID);
		        //当主次账号统一显示的时候,获取用户名和用户类型
		        if(StringUtils.isNotEmpty(rs.getString("userid"))){

	                String userid = rs.getString("userid");
	                wbi.setF_weaver_belongto_userid(userid);
		        }
                if(StringUtils.isNotEmpty(rs.getString("usertype"))){

                    String usertype = rs.getString("usertype");
                    wbi.setF_weaver_belongto_usertype(usertype);
                }
				wbi.setWorkflowName(WorkflowComInfo.getWorkflowname(workflowID));
				wbi.setWorkflowTypeId(WorkflowComInfo.getWorkflowtype(workflowID));
				wbi.setWorkflowTypeName(workflowTypeComInfo.getWorkTypename(WorkflowComInfo.getWorkflowtype(workflowID)));
				wri.setWorkflowBaseInfo(wbi);
				
		        String isbill = WorkflowComInfo.getIsBill(workflowID);
		        //log.info("The workflowid value :\t" + workflowID);
		        //log.info("The isbill value :\t" + isbill);
		        //监控的时候，不过滤无效流程


		        //if(StringUtils.isEmpty(isbill)){
	            if(StringUtils.isEmpty(isbill) && !monitorFlag){
		            
		        	//如果 isBill为空，可能是新建流程则重新加载流程配置信息。








		        	//log.warn("The 'isbill' is empty, start to relaodWorkflowInfos.");
		        	WorkflowComInfo.reloadWorkflowInfos();
		        	isbill = WorkflowComInfo.getIsBill(WorkflowVersion.getActiveVersionWFID(workflowID));
		        	
		        	//log.warn("The isbill value :\t" + isbill);
		        	//如果重新加载之后仍为空，则直接抛异常。








		        	if(StringUtils.isEmpty(isbill)){
		        		log.error("The 'isbill' still is empty, and throw a RuntimeException. ");
		        		throw new RuntimeException("The workflow doesn't exists");
		        	}
		        }
		        String formId = WorkflowComInfo.getFormId(workflowID);
		        //log.info("The formID value is :\t" + formId);
		        
		        int intRequestID = Util.getIntValue(requestID, -1);
		        int intWorkflowID = Util.getIntValue(workflowID, -1);
		        int intIsBill = Util.getIntValue(isbill, -1);
		        int intFormID = Util.getIntValue(formId, -1);
//		        MailAndMessage mailTitle = new MailAndMessage();
//				String titles = mai|lTitle.getTitle(intRequestID, intWorkflowID, intFormID, userLanguageID, intIsBill);
		        
//				if (titles != null && !titles.equals("") ){
//					//去除其中的Html标签
//					titles = Util.delHtml(titles);
//					//对空格标签进行替换，对Flash脚本进行替换。



//
//
//
//
//
//					titles = titles.replace("%nbsp;", " ").replace("initFlashVideo();", "");
//					requestName = requestName + "（"+titles+"）";
//				}
				
				String request_name_new = Util.null2String(rs.getString("requestnamenew"));
                if (!"".equals(request_name_new) && !requestName.equals(request_name_new)) {
                    requestName = request_name_new;
                }
				requestName=(requestName==null?"":requestName.replace("&quot;", "\""));
				wri.setRequestName(requestName);

				String currentnodeid = "";
				String currentnodename = "";
				currentnodeid = rs.getString("currentnodeid");
				/*
				rs1.executeSql("select * from workflow_nodebase where id = " + currentnodeid);
				if (rs1.next())
					currentnodename = rs1.getString("nodename");
				wri.setCurrentNodeName(currentnodename);
				*/
				wri.setCurrentNodeId(currentnodeid);
				
//				String requestid = "";
//				String status = "";
//				requestid = wri.getRequestId();
//				rs1.executeSql("select * from workflow_requestbase where requestid = " + requestid);
//				if (rs1.next())
//					status = rs1.getString("status");
//				wri.setStatus(status);
				wri.setStatus(rs.getString("status"));
				

				wri.setCreatorId(rs.getString("creater"));
				wri.setCreatorName(resourceComInfo.getLastname(rs.getString("creater")));
				wri.setCreateTime(rs.getString("createdate") + " " + rs.getString("createtime"));
				wri.setLastOperatorName(resourceComInfo.getLastname(rs.getString("lastoperator")));
				wri.setLastOperateTime(rs.getString("lastoperatedate") + " " + rs.getString("lastoperatetime"));
				wri.setReceiveTime(rs.getString("receivedate") + " " + rs.getString("receivetime"));
				
				wri.setNodeId(rs.getString("nodeid"));
				wri.setIsremark(Util.getIntValue(rs.getString("isremark")));
				wri.setRequestNameNew(requestnamenew);
				wris.add(wri);
			}
		} catch (Exception e) {
			e.printStackTrace();
			writeLog(e);
		}
		WorkflowRequestInfo[] wriarrays = new WorkflowRequestInfo[wris.size()];
		for (int i = 0; i < wris.size(); i++)
			wriarrays[i] = (WorkflowRequestInfo) wris.get(i);
		return wriarrays;
	}

	public WorkflowExtInfo[] getCreateWorkflowList(int pageNo, int pageSize, int recordCount, int userid, int workflowType, String[] conditions) {
		List wbis = new ArrayList();
		try {
			WorkTypeComInfo workTypeComInfo = new WorkTypeComInfo();
			WorkflowComInfo WorkflowComInfo = new WorkflowComInfo();
			ResourceComInfo resourceComInfo = new ResourceComInfo();
			
			RecordSet recordSet = new RecordSet();
			if (pageNo < 1) {
				pageNo = 1;
			}
			if (pageSize < 1) {
				pageSize = 1;
			}
			
			String workflowName = conditions.length > 0 ? Util.null2String(conditions[0]) : "";
			String formids = conditions.length>1 && !"".equals(Util.null2String(conditions[1])) ? Util.null2String(conditions[1]) : "0";
			
			List alloworkflow = new ArrayList();
			recordSet.executeSql("select id from workflow_base where isvalid in('1','3') and  ( isbill=0 or (isbill=1 and formid<0) or (isbill=1 and formid in ("+formids+")))");
			while(recordSet.next()){
				alloworkflow.add(recordSet.getString("id"));
			}
			
			User user = this.getUser(userid);
			
			String logintype = user.getLogintype();
			int usertype = 0;
			if(logintype.equals("2")){
				usertype = 1;
			}

			String seclevel = user.getSeclevel();

			String selectedworkflow="";
			String isuserdefault="";

			recordSet.executeProc("workflow_RUserDefault_Select",""+userid);
			if(recordSet.next()){
				selectedworkflow=recordSet.getString("selectedworkflow");
				isuserdefault=recordSet.getString("isuserdefault");
			}
			
			//是否显示自定义工作流,目前默认显示全部
			isuserdefault = "0";
			
			if(!selectedworkflow.equals("")) {
				selectedworkflow+="|";
			}

			ShareManager shareManager = new ShareManager();
			//获取流程新建权限体系sql条件
			String wfcrtSqlWhere = shareManager.getWfShareSqlWhere(user, "t1");
			//mobile5.0新建权限控制
			String wfscope="0"; //0表示全部 1 表示选择 2 表示非选择
            String scopeSql = " select propValue from mobileProperty where name = 'wfscope' ";
            recordSet.executeSql(scopeSql);
			if(recordSet.next()){
				  wfscope = recordSet.getString("propValue");
			}
			
			ArrayList NewWorkflowTypes = new ArrayList();

			String sql = "select distinct workflowtype from ShareInnerWfCreate t1,workflow_base t2 where t1.workflowid=t2.id and t2.isvalid in('1','3') and " + wfcrtSqlWhere;
			if("1".equals(wfscope)){
                 sql = sql +" and  t2.id in (select propValue from mobileProperty where name = 'wfid') ";
			}else if("2".equals(wfscope)){
                 sql = sql +" and  t2.id  not in (select propValue from mobileProperty where name = 'wfid') ";
			}
			recordSet.executeSql(sql);
			while(recordSet.next()){
				NewWorkflowTypes.add(recordSet.getString("workflowtype"));
			}

			//所有可创建流程集合
			ArrayList NewWorkflows = new ArrayList();

			sql = "select t1.* from ShareInnerWfCreate t1 where " +  wfcrtSqlWhere;
			if("1".equals(wfscope)){
                 sql ="select * from ("+ sql +") a where  workflowid in (select propValue from mobileProperty where name = 'wfid') ";
			}else if("2".equals(wfscope)){
                 sql ="select * from ("+ sql +") a where  workflowid  not in (select propValue from mobileProperty where name = 'wfid') ";
			}
			recordSet.executeSql(sql);
			while(recordSet.next()){
				NewWorkflows.add(recordSet.getString("workflowid"));
			}
			
			/*modify by mackjoe at 2005-09-14 增加流程代理创建权限*/
			ArrayList AgentWorkflows = new ArrayList();
			ArrayList Agenterids = new ArrayList();
			/* 暂时不开放流程代理创建权限








			if (usertype == 0) {
				//获得当前的日期和时间
				Calendar today = Calendar.getInstance();
				String currentdate = Util.add0(today.get(Calendar.YEAR), 4) + "-" +
			                     Util.add0(today.get(Calendar.MONTH) + 1, 2) + "-" +
			                     Util.add0(today.get(Calendar.DAY_OF_MONTH), 2) ;

				String currenttime = Util.add0(today.get(Calendar.HOUR_OF_DAY), 2) + ":" +
			                     Util.add0(today.get(Calendar.MINUTE), 2) + ":" +
			                     Util.add0(today.get(Calendar.SECOND), 2) ;
				String begindate="";
				String begintime="";
				String enddate="";
				String endtime="";
				int agentworkflowtype=0;
				int agentworkflow=0;
				int beagenterid=0;
				sql = "select distinct t1.workflowtype,t.workflowid,t.beagenterid,t.begindate,t.begintime,t.enddate,t.endtime from workflow_agent t,workflow_base t1 where t.workflowid=t1.id and t.agenttype>'0' and t.iscreateagenter=1 and t.agenterid="+userid+" order by t1.workflowtype,t.workflowid";
				recordSet.executeSql(sql);
				while(recordSet.next()){
					begindate=Util.null2String(recordSet.getString("begindate"));
					begintime=Util.null2String(recordSet.getString("begintime"));
					enddate=Util.null2String(recordSet.getString("enddate"));
					endtime=Util.null2String(recordSet.getString("endtime"));
					agentworkflowtype=Util.getIntValue(recordSet.getString("workflowtype"),0);
					agentworkflow=Util.getIntValue(recordSet.getString("workflowid"),0);
					beagenterid=Util.getIntValue(recordSet.getString("beagenterid"),0);
					if(!begindate.equals("")){
						if((begindate+" "+begintime).compareTo(currentdate+" "+currenttime)>0)
							continue;
					}
					if(!enddate.equals("")){
						if((enddate+" "+endtime).compareTo(currentdate+" "+currenttime)<0)
							continue;
					}
					String sqltemp = "select * from workflow_createrlist a,hrmresource b where b.id="+beagenterid+" and ((userid = -1 and usertype <= b.seclevel and usertype2 >= b.seclevel) or (userid="+beagenterid+" and usertype=0)) and workflowid="+agentworkflow;
					rs.executeSql(sqltemp);
					if(rs.next()){
						if(NewWorkflowTypes.indexOf(agentworkflowtype+"")==-1){
							NewWorkflowTypes.add(agentworkflowtype+"");
						}
						int indx=AgentWorkflows.indexOf(""+agentworkflow);
						if(indx==-1){
							AgentWorkflows.add(""+agentworkflow);
							Agenterids.add(""+beagenterid);
						}else{
							String tempagenter=(String)Agenterids.get(indx);
							tempagenter+=","+beagenterid;
							Agenterids.set(indx,tempagenter);
						}
					}
				}
				//end
			}
			*/

			int total = 0;
			int startindex = (pageNo-1)*pageSize+1;
			int endindex = pageNo*pageSize;
			while(workTypeComInfo.next()){
				String wftypename=workTypeComInfo.getWorkTypename();
				String wftypeid = workTypeComInfo.getWorkTypeid();
				 String orderid = workTypeComInfo.getWorkDsporder();

				if(NewWorkflowTypes.indexOf(wftypeid)==-1){
		 			continue;            
				}
			 	if(selectedworkflow.indexOf("T"+wftypeid+"|")==-1&& isuserdefault.equals("1")){
					continue;
				}
			 	
			 	if(workflowType > 0 && workflowType != Util.getIntValue(wftypeid, 0)) {
			 		continue;
			 	}
				
				while(WorkflowComInfo.next()){
					String wfname=WorkflowComInfo.getWorkflowname();
					String wfid = WorkflowComInfo.getWorkflowid();
					String curtypeid = WorkflowComInfo.getWorkflowtype();
					String agentname="";
					ArrayList agenterlist=new ArrayList();
					
					if(alloworkflow.indexOf(wfid) == -1) continue;
					
					if(!curtypeid.equals(wftypeid)) continue;
					
					if(!"".equals(workflowName) && wfname.indexOf(workflowName) == -1) continue;

					//check right
					if(selectedworkflow.indexOf("W"+wfid+"|")==-1&& isuserdefault.equals("1")) continue;
					
					if(NewWorkflows.indexOf(wfid)==-1){
						if(AgentWorkflows.indexOf(wfid)==-1){
							continue;
						}else{
							agenterlist=Util.TokenizerString((String)Agenterids.get(AgentWorkflows.indexOf(wfid)),",");
							for(int k=0;k<agenterlist.size();k++){
								total++;
								if (total < startindex || total > endindex) continue;
								agentname="("+resourceComInfo.getResourcename((String)agenterlist.get(k))+"->"+user.getUsername()+")";
								WorkflowExtInfo wbi = new WorkflowExtInfo();
								wbi.setWorkflowId(wfid);
								wbi.setWorkflowName(Util.toScreen(wfname,user.getLanguage()) + agentname);
								wbi.setWorkflowTypeId(wftypeid);
								wbi.setWorkflowTypeName(wftypename);
								wbi.setWorkflowDsOrder(orderid);
								wbis.add(wbi);
							}
						}
					}else{
						total++;
						if (total < startindex || total > endindex) continue;
						WorkflowExtInfo wbi = new WorkflowExtInfo();
						wbi.setWorkflowId(wfid);
						wbi.setWorkflowName(Util.toScreen(wfname,user.getLanguage()));
						wbi.setWorkflowTypeId(wftypeid);
						wbi.setWorkflowTypeName(wftypename);
						wbi.setWorkflowDsOrder(orderid);
						wbis.add(wbi);
					}
				}
				WorkflowComInfo.setTofirstRow();
			}
		} catch (Exception e) {
			e.printStackTrace();
			writeLog(e);
		}
		
		WorkflowExtInfo[] wbiarrays = new WorkflowExtInfo[wbis.size()];
		wbis.toArray(wbiarrays);
		return wbiarrays;
	}

	/**
	 * (多账号统一显示)
	 * @param pageNo
	 * @param pageSize
	 * @param recordCount
	 * @param userid
	 * @param workflowType
	 * @param conditions
	 * @param belongtoshow
	 * @return
	 */
    public WorkflowExtInfo[] getCreateWorkflowList(int pageNo, int pageSize, int recordCount, int userid, int workflowType, String[] conditions,boolean belongtoshow) {
        List wbis = new ArrayList();
        try {
            WorkTypeComInfo workTypeComInfo = new WorkTypeComInfo();
            WorkflowComInfo WorkflowComInfo = new WorkflowComInfo();
            ResourceComInfo resourceComInfo = new ResourceComInfo();
            
            RecordSet recordSet = new RecordSet();
            if (pageNo < 1) {
                pageNo = 1;
            }
            if (pageSize < 1) {
                pageSize = 1;
            }
            
            String workflowName = conditions.length > 0 ? Util.null2String(conditions[0]) : "";
            String formids = conditions.length>1 && !"".equals(Util.null2String(conditions[1])) ? Util.null2String(conditions[1]) : "0";
            
            List alloworkflow = new ArrayList();
            recordSet.executeSql("select id from workflow_base where isvalid in('1','3') and  ( isbill=0 or (isbill=1 and formid<0) or (isbill=1 and formid in ("+formids+")))");
            while(recordSet.next()){
                alloworkflow.add(recordSet.getString("id"));
            }
            
            User user = this.getUser(userid);
            
            String logintype = user.getLogintype();
            int usertype = 0;
            if(logintype.equals("2")){
                usertype = 1;
            }

            String seclevel = user.getSeclevel();

            String selectedworkflow="";
            String isuserdefault="";

            recordSet.executeProc("workflow_RUserDefault_Select",""+userid);
            if(recordSet.next()){
                selectedworkflow=recordSet.getString("selectedworkflow");
                isuserdefault=recordSet.getString("isuserdefault");
            }
            
            //是否显示自定义工作流,目前默认显示全部
            isuserdefault = "0";
            
            if(!selectedworkflow.equals("")) {
                selectedworkflow+="|";
            }

            ShareManager shareManager = new ShareManager();
            //获取流程新建权限体系sql条件
            String wfcrtSqlWhere = shareManager.getWfShareSqlWhere(user, "t1");
            //mobile5.0新建权限控制
            String wfscope="0"; //0表示全部 1 表示选择 2 表示非选择
            String scopeSql = " select propValue from mobileProperty where name = 'wfscope' ";
            recordSet.executeSql(scopeSql);
            if(recordSet.next()){
                  wfscope = recordSet.getString("propValue");
            }
            
            ArrayList NewWorkflowTypes = new ArrayList();

            String sql = "select distinct workflowtype from ShareInnerWfCreate t1,workflow_base t2 where t1.workflowid=t2.id and t2.isvalid in('1','3') and " + wfcrtSqlWhere;
            if("1".equals(wfscope)){
                 sql = sql +" and  t2.id in (select propValue from mobileProperty where name = 'wfid') ";
            }else if("2".equals(wfscope)){
                 sql = sql +" and  t2.id  not in (select propValue from mobileProperty where name = 'wfid') ";
            }
            recordSet.executeSql(sql);
            while(recordSet.next()){
                NewWorkflowTypes.add(recordSet.getString("workflowtype"));
            }


            //所有可创建流程集合
            ArrayList NewWorkflows = new ArrayList();

            sql = "select t1.* from ShareInnerWfCreate t1 where " +  wfcrtSqlWhere;
            if("1".equals(wfscope)){
                 sql ="select * from ("+ sql +") a where  workflowid in (select propValue from mobileProperty where name = 'wfid') ";
            }else if("2".equals(wfscope)){
                 sql ="select * from ("+ sql +") a where  workflowid  not in (select propValue from mobileProperty where name = 'wfid') ";
            }
            recordSet.executeSql(sql);
            while(recordSet.next()){
                NewWorkflows.add(recordSet.getString("workflowid"));
            }

            //用户列表保存
            Map<String,User> userMap = new HashMap<String, User>();
            //主账号


            userMap.put("0_" + user.getUID(), user);

            //当显示次账号的时候


            List<Map<String,String>> belongtoMapList = new ArrayList<Map<String,String>>();
            List<User> belongtoUserList = new ArrayList<User>();
            if(belongtoshow){
                String belongtoIds = user.getBelongtoids();
                if(StringUtils.isNotEmpty(belongtoIds)){
                    String[] belongtoIdArray =  belongtoIds.split(",");
                    RecordSet recordSetBelongto = new RecordSet();
                    for(String belongtoId : belongtoIdArray){
                        User belongtoUser = this.getUser(Util.getIntValue(belongtoId));
                        if(StringUtils.isNotEmpty(belongtoId)){
                            belongtoUserList.add(belongtoUser);
                            recordSetBelongto.executeProc("workflow_RUserDefault_Select",belongtoId);

                            Map<String,String> belongtoMap = new HashMap<String,String>();
                            belongtoMap.put("userid",belongtoId);
                            if(recordSetBelongto.next()){
                                String selectedBelongtoworkflow = recordSetBelongto.getString("selectedworkflow");
                                String isuserdeBelongtofault = recordSetBelongto.getString("isuserdefault");
                                belongtoMap.put("selectedworkflow",selectedBelongtoworkflow);
                                belongtoMap.put("isuserdefault",isuserdeBelongtofault);
                            }

                            //次账号


                            userMap.put("1_" + belongtoUser.getUID(), belongtoUser);
                            
                            String wfcrtSqlWhereBelongto = shareManager.getWfShareSqlWhere(belongtoUser, "t1");

                            StringBuffer NewWfTypesBelongtoStr = new StringBuffer();

                            sql = "select distinct workflowtype from ShareInnerWfCreate t1,workflow_base t2 where t1.workflowid=t2.id and t2.isvalid in('1','3') and " + wfcrtSqlWhereBelongto;
                            if("1".equals(wfscope)){
                                 sql = sql +" and  t2.id in (select propValue from mobileProperty where name = 'wfid') ";
                            }else if("2".equals(wfscope)){
                                 sql = sql +" and  t2.id  not in (select propValue from mobileProperty where name = 'wfid') ";
                            }
                            recordSet.executeSql(sql);
                            while(recordSet.next()){
                                NewWfTypesBelongtoStr.append("|");
                                NewWfTypesBelongtoStr.append(recordSet.getString("workflowtype"));
                            }
                            NewWfTypesBelongtoStr.append("|");
                            belongtoMap.put("NewWfTypesBelongto", NewWfTypesBelongtoStr.toString());


                            //所有可创建流程集合
                            StringBuffer NewWfBelongtoStr = new StringBuffer();
                            
                            sql = "select t1.* from ShareInnerWfCreate t1 where " +  wfcrtSqlWhereBelongto;
                            if("1".equals(wfscope)){
                                 sql ="select * from ("+ sql +") a where  workflowid in (select propValue from mobileProperty where name = 'wfid') ";
                            }else if("2".equals(wfscope)){
                                 sql ="select * from ("+ sql +") a where  workflowid  not in (select propValue from mobileProperty where name = 'wfid') ";
                            }
                            recordSet.executeSql(sql);
                            while(recordSet.next()){
                                NewWfBelongtoStr.append("|");
                                NewWfBelongtoStr.append(recordSet.getString("workflowid"));
                            }
                            NewWfBelongtoStr.append("|");
                            belongtoMap.put("NewWfBelongto", NewWfBelongtoStr.toString());
                            belongtoMapList.add(belongtoMap);
                        }
                    }
                }
            }
            if(belongtoshow && belongtoUserList.size() > 0){
                for(User belongtoUser : belongtoUserList){
                }
            }
            
            /*modify by mackjoe at 2005-09-14 增加流程代理创建权限*/
            ArrayList AgentWorkflows = new ArrayList();
            ArrayList Agenterids = new ArrayList();
            /* 暂时不开放流程代理创建权限







            if (usertype == 0) {
                //获得当前的日期和时间
                Calendar today = Calendar.getInstance();
                String currentdate = Util.add0(today.get(Calendar.YEAR), 4) + "-" +
                                 Util.add0(today.get(Calendar.MONTH) + 1, 2) + "-" +
                                 Util.add0(today.get(Calendar.DAY_OF_MONTH), 2) ;

                String currenttime = Util.add0(today.get(Calendar.HOUR_OF_DAY), 2) + ":" +
                                 Util.add0(today.get(Calendar.MINUTE), 2) + ":" +
                                 Util.add0(today.get(Calendar.SECOND), 2) ;
                String begindate="";
                String begintime="";
                String enddate="";
                String endtime="";
                int agentworkflowtype=0;
                int agentworkflow=0;
                int beagenterid=0;
                sql = "select distinct t1.workflowtype,t.workflowid,t.beagenterid,t.begindate,t.begintime,t.enddate,t.endtime from workflow_agent t,workflow_base t1 where t.workflowid=t1.id and t.agenttype>'0' and t.iscreateagenter=1 and t.agenterid="+userid+" order by t1.workflowtype,t.workflowid";
                recordSet.executeSql(sql);
                while(recordSet.next()){
                    begindate=Util.null2String(recordSet.getString("begindate"));
                    begintime=Util.null2String(recordSet.getString("begintime"));
                    enddate=Util.null2String(recordSet.getString("enddate"));
                    endtime=Util.null2String(recordSet.getString("endtime"));
                    agentworkflowtype=Util.getIntValue(recordSet.getString("workflowtype"),0);
                    agentworkflow=Util.getIntValue(recordSet.getString("workflowid"),0);
                    beagenterid=Util.getIntValue(recordSet.getString("beagenterid"),0);
                    if(!begindate.equals("")){
                        if((begindate+" "+begintime).compareTo(currentdate+" "+currenttime)>0)
                            continue;
                    }
                    if(!enddate.equals("")){
                        if((enddate+" "+endtime).compareTo(currentdate+" "+currenttime)<0)
                            continue;
                    }
                    String sqltemp = "select * from workflow_createrlist a,hrmresource b where b.id="+beagenterid+" and ((userid = -1 and usertype <= b.seclevel and usertype2 >= b.seclevel) or (userid="+beagenterid+" and usertype=0)) and workflowid="+agentworkflow;
                    rs.executeSql(sqltemp);
                    if(rs.next()){
                        if(NewWorkflowTypes.indexOf(agentworkflowtype+"")==-1){
                            NewWorkflowTypes.add(agentworkflowtype+"");
                        }
                        int indx=AgentWorkflows.indexOf(""+agentworkflow);
                        if(indx==-1){
                            AgentWorkflows.add(""+agentworkflow);
                            Agenterids.add(""+beagenterid);
                        }else{
                            String tempagenter=(String)Agenterids.get(indx);
                            tempagenter+=","+beagenterid;
                            Agenterids.set(indx,tempagenter);
                        }
                    }
                }
                //end
            }
            */

            String belongtoId = "";
            int total = 0;
            int startindex = (pageNo-1)*pageSize+1;
            int endindex = pageNo*pageSize;
            while(workTypeComInfo.next()){

                //是否次账号


                boolean belongtoFalg = false;
                String wftypename=workTypeComInfo.getWorkTypename();
                String wftypeid = workTypeComInfo.getWorkTypeid();

                
                if(NewWorkflowTypes.indexOf(wftypeid)==-1){
                    //当主次账号统一显示时


                    boolean belongToExists = false;
                    for(Map<String,String> belongtoMap : belongtoMapList){

                        String NewWfTypesBelongtoStr = belongtoMap.get("NewWfTypesBelongto"); 
                        if(NewWfTypesBelongtoStr.indexOf("|" + wftypeid + "|") >= 0){
                            belongToExists = true;
                            //是次账号
                            belongtoFalg = true;
                            belongtoId = belongtoMap.get("userid");
                            break;
                        }
                        
                    }
                    if(!belongToExists) continue;            
                }

                if(selectedworkflow.indexOf("T"+wftypeid+"|")==-1&& isuserdefault.equals("1")){
                    //当主次账号统一显示时


                    boolean belongToExists = false;
                    for(Map<String,String> belongtoMap : belongtoMapList){
                        String selectedBelongtoworkflow = belongtoMap.get("selectedworkflow");
                        String isuserdeBelongtofault = belongtoMap.get("isuserdefault");
                        if(selectedBelongtoworkflow.indexOf("T"+wftypeid+"|") >= 0 || !isuserdeBelongtofault.equals("1")){
                            //是次账号
                            belongtoFalg = true;
                            belongToExists = true;
                            belongtoId = belongtoMap.get("userid");
                            break;
                        }
                    }
                    if(!belongToExists){
                        continue;
                    }
                }
                
                if(workflowType > 0 && workflowType != Util.getIntValue(wftypeid, 0)) {
                    continue;
                }
                
                while(WorkflowComInfo.next()){
                    String wfname=WorkflowComInfo.getWorkflowname();
                    String wfid = WorkflowComInfo.getWorkflowid();
                    String curtypeid = WorkflowComInfo.getWorkflowtype();
                    String agentname="";
                    ArrayList agenterlist=new ArrayList();
                    
                    if(alloworkflow.indexOf(wfid) == -1) continue;
                    
                    if(!curtypeid.equals(wftypeid)) continue;
                    
                    if(!"".equals(workflowName) && wfname.indexOf(workflowName) == -1) continue;

                    //check right
                    if(selectedworkflow.indexOf("W"+wfid+"|")==-1&& isuserdefault.equals("1")){
                        //当主次账号统一显示时


                        boolean belongToExists = false;
                        for(Map<String,String> belongtoMap : belongtoMapList){
                            String selectedBelongtoworkflow = belongtoMap.get("selectedworkflow");
                            String isuserdeBelongtofault = belongtoMap.get("isuserdefault");
                            if(selectedBelongtoworkflow.indexOf("W"+wfid+"|") >= 0 || !isuserdeBelongtofault.equals("1")){
                                //是次账号
                                belongtoFalg = true;
                                belongToExists = true;
                                belongtoId = belongtoMap.get("userid");
                                break;
                            }
                        }
                        if(!belongToExists){
                            continue;
                        }
                    }


                    if(NewWorkflows.indexOf(wfid)==-1){

                        //当主次账号统一显示时


                        boolean belongToExists = false;
                        for(Map<String,String> belongtoMap : belongtoMapList){

                            String NewWfBelongto = belongtoMap.get("NewWfBelongto"); 
                            if(NewWfBelongto.indexOf("|" + wfid + "|") >= 0){
                                belongToExists = true;
                                belongtoId = belongtoMap.get("userid");
                                break;
                            }
                        }

                        if(belongToExists){
                            total++;
                            if (total < startindex || total > endindex) continue;
                            //获取流程信息
                            WorkflowExtInfo wbi = editWfInfo(true, wfid, Util.toScreen(wfname,user.getLanguage()), wftypeid, wftypename, belongtoMapList, belongtoId, usertype,userMap,userid);
                            
                            wbis.add(wbi);
                            continue;
                        }

                        if(AgentWorkflows.indexOf(wfid)==-1){
                            continue;
                        }else{
                            agenterlist=Util.TokenizerString((String)Agenterids.get(AgentWorkflows.indexOf(wfid)),",");
                            for(int k=0;k<agenterlist.size();k++){
                                total++;
                                if (total < startindex || total > endindex) continue;
                                agentname="("+resourceComInfo.getResourcename((String)agenterlist.get(k))+"->"+user.getUsername()+")";

                                //获取流程信息
                                WorkflowExtInfo wbi = editWfInfo(belongtoFalg, wfid, Util.toScreen(wfname,user.getLanguage()) + agentname, wftypeid, wftypename, belongtoMapList, belongtoId, usertype,userMap,userid);
                                
                                wbis.add(wbi);
                            }
                        }
                    }else{
                        total++;
                        if (total < startindex || total > endindex) continue;

                        //获取流程信息
                        WorkflowExtInfo wbi = editWfInfo(belongtoFalg, wfid, Util.toScreen(wfname,user.getLanguage()) + agentname, wftypeid, wftypename, belongtoMapList, belongtoId, usertype,userMap,userid);
                        
                        wbis.add(wbi);
                    }
                }
                WorkflowComInfo.setTofirstRow();
            }
        } catch (Exception e) {
            e.printStackTrace();
            writeLog(e);
        }
        
        WorkflowExtInfo[] wbiarrays = new WorkflowExtInfo[wbis.size()];
        wbis.toArray(wbiarrays);
        return wbiarrays;
    }
    /**
     * 
     * 编辑工作流信息


     * @param belongtoFalg 是否次账号


     * @param wfid 流程id
     * @param wfname 流程显示名称
     * @param wftypeid 流程类型id
     * @param wftypename 流程类型名称
     * @param belongtoMapList 次账号流程信息


     * @param belongtoId 次账号id
     * @param usertype 次账号用户类型


     * @param userMap 用户列表
     * @param userid 当前用户id
     * @return
     */
    private WorkflowExtInfo editWfInfo(boolean belongtoFalg,
            String wfid,String wfname,String wftypeid,String wftypename,
            List<Map<String,String>> belongtoMapList,
            String belongtoId,int usertype,
            Map<String,User> userMap,
            int userid){
        WorkflowExtInfo wbi = new WorkflowExtInfo();
        wbi.setWorkflowId(wfid);
        wbi.setWorkflowName(wfname);
        wbi.setWorkflowTypeId(wftypeid);
        wbi.setWorkflowTypeName(wftypename);
        List<User> userList = new ArrayList<User>();
        //当次账号显示时


        if(belongtoFalg){
            wbi.setF_weaver_belongto_userid(belongtoId);
            wbi.setF_weaver_belongto_usertype(String.valueOf(usertype));
        }else{
            if(userMap.containsKey("0_" + userid)){
                userList.add(userMap.get("0_" + userid));
            }
        }
        //获取次账号信息


        for(Map<String,String> belongtoMap : belongtoMapList){


            String belongtoUserId = belongtoMap.get("userid");
            String NewWfTypesBelongtoStr = belongtoMap.get("NewWfTypesBelongto");
            //检查流程类型是否允许申请


            if(NewWfTypesBelongtoStr.indexOf("|" + wftypeid + "|") == -1){
                continue;
            }

            String selectedBelongtoworkflow = Util.null2String(belongtoMap.get("selectedworkflow"));
            String isuserdeBelongtofault = Util.null2String(belongtoMap.get("isuserdefault"));
            //检查流程类型是否选中
           // if(selectedBelongtoworkflow.indexOf("T"+wftypeid+"|") == -1 && isuserdeBelongtofault.equals("1")){
           //     continue;
          //  }

            //检查流程是否选中
          //  if(selectedBelongtoworkflow.indexOf("W"+wfid+"|")==-1 && isuserdeBelongtofault.equals("1")){
              //  continue;
          //  }

            //检查流程是否允许申请


            String NewWfBelongto = belongtoMap.get("NewWfBelongto"); 
            if(NewWfBelongto.indexOf("|" + wfid + "|") == -1){
                continue;
            }
            if(userMap.containsKey("1_" + belongtoUserId)){
                userList.add(userMap.get("1_" + belongtoUserId));
            }
        }
        wbi.setUserList(userList);
        return wbi;
    }

	public int getCreateWorkflowCount(int userid, int workflowType, String[] conditions) {
		int total = 0;
		try {
			WorkTypeComInfo workTypeComInfo = new WorkTypeComInfo();
			WorkflowComInfo WorkflowComInfo = new WorkflowComInfo();
			
			RecordSet recordSet = new RecordSet();
			RecordSet rs = new RecordSet();
			
			String workflowName = conditions.length > 0 ? Util.null2String(conditions[0]) : "";
			
			User user = this.getUser(userid);
			
			String logintype = user.getLogintype();
			int usertype = 0;
			if(logintype.equals("2")){
				usertype = 1;
			}

			String seclevel = user.getSeclevel();

			String selectedworkflow="";
			String isuserdefault="";

			recordSet.executeProc("workflow_RUserDefault_Select",""+userid);
			if(recordSet.next()){
				selectedworkflow=recordSet.getString("selectedworkflow");
				isuserdefault=recordSet.getString("isuserdefault");
			}
			
			if(!selectedworkflow.equals("")) {
				selectedworkflow+="|";
			}

			ArrayList NewWorkflowTypes = new ArrayList();
			ShareManager shareManager = new ShareManager();
			//获取流程新建权限sql条件
			String wfcrtSqlWhere = shareManager.getWfShareSqlWhere(user, "t1");
			
			String sql = "select distinct workflowtype from ShareInnerWfCreate t1,workflow_base t2 where t1.workflowid=t2.id and t2.isvalid in('1','3') and " + wfcrtSqlWhere;
			recordSet.executeSql(sql);
			while(recordSet.next()){
				NewWorkflowTypes.add(recordSet.getString("workflowtype"));
			}

			//modify by xhheng @20050110 for 流程代理
			ArrayList NewWorkflows = new ArrayList();
			
			sql = "select * from ShareInnerWfCreate t1 where " +  wfcrtSqlWhere;
			recordSet.executeSql(sql);
			while(recordSet.next()){
				NewWorkflows.add(recordSet.getString("workflowid"));
			}
			
			/*modify by mackjoe at 2005-09-14 增加流程代理创建权限*/
			ArrayList AgentWorkflows = new ArrayList();
			ArrayList Agenterids = new ArrayList();
			//TD13554
			/*
			if (usertype == 0) {
				//获得当前的日期和时间
				Calendar today = Calendar.getInstance();
				String currentdate = Util.add0(today.get(Calendar.YEAR), 4) + "-" +
			                     Util.add0(today.get(Calendar.MONTH) + 1, 2) + "-" +
			                     Util.add0(today.get(Calendar.DAY_OF_MONTH), 2) ;

				String currenttime = Util.add0(today.get(Calendar.HOUR_OF_DAY), 2) + ":" +
			                     Util.add0(today.get(Calendar.MINUTE), 2) + ":" +
			                     Util.add0(today.get(Calendar.SECOND), 2) ;
				String begindate="";
				String begintime="";
				String enddate="";
				String endtime="";
				int agentworkflowtype=0;
				int agentworkflow=0;
				int beagenterid=0;
				sql = "select distinct t1.workflowtype,t.workflowid,t.beagenterid,t.begindate,t.begintime,t.enddate,t.endtime from workflow_agent t,workflow_base t1 where t.workflowid=t1.id and t.agenttype>'0' and t.iscreateagenter=1 and t.agenterid="+userid+" order by t1.workflowtype,t.workflowid";
				recordSet.executeSql(sql);
				while(recordSet.next()){
					begindate=Util.null2String(recordSet.getString("begindate"));
					begintime=Util.null2String(recordSet.getString("begintime"));
					enddate=Util.null2String(recordSet.getString("enddate"));
					endtime=Util.null2String(recordSet.getString("endtime"));
					agentworkflowtype=Util.getIntValue(recordSet.getString("workflowtype"),0);
					agentworkflow=Util.getIntValue(recordSet.getString("workflowid"),0);
					beagenterid=Util.getIntValue(recordSet.getString("beagenterid"),0);
					if(!begindate.equals("")){
						if((begindate+" "+begintime).compareTo(currentdate+" "+currenttime)>0)
							continue;
					}
					if(!enddate.equals("")){
						if((enddate+" "+endtime).compareTo(currentdate+" "+currenttime)<0)
							continue;
					}
					String sqltemp = "select * from workflow_createrlist a,hrmresource b where b.id="+beagenterid+" and ((userid = -1 and usertype <= b.seclevel and usertype2 >= b.seclevel) or (userid="+beagenterid+" and usertype=0)) and workflowid="+agentworkflow;
					rs.executeSql(sqltemp);
					if(rs.next()){
						if(NewWorkflowTypes.indexOf(agentworkflowtype+"")==-1){
							NewWorkflowTypes.add(agentworkflowtype+"");
						}
						int indx=AgentWorkflows.indexOf(""+agentworkflow);
						if(indx==-1){
							AgentWorkflows.add(""+agentworkflow);
							Agenterids.add(""+beagenterid);
						}else{
							String tempagenter=(String)Agenterids.get(indx);
							tempagenter+=","+beagenterid;
							Agenterids.set(indx,tempagenter);
						}
					}
				}
				//end
				 
			}
			 */
			String dataCenterWorkflowTypeId="";
			recordSet.executeSql("select currentId from sequenceindex where indexDesc='dataCenterWorkflowTypeId'");
			if(recordSet.next()){
				dataCenterWorkflowTypeId=Util.null2String(recordSet.getString("currentId"));
			}
			
			while(workTypeComInfo.next()){
				String wftypename=workTypeComInfo.getWorkTypename();
				String wftypeid = workTypeComInfo.getWorkTypeid();

				if(NewWorkflowTypes.indexOf(wftypeid)==-1){
		 			continue;            
				}
			 	if(selectedworkflow.indexOf("T"+wftypeid+"|")==-1&& isuserdefault.equals("1")){
					continue;
				}
			 	if(dataCenterWorkflowTypeId.equals(wftypeid)) {
					continue;
				}
			 	
			 	if(workflowType > 0 && workflowType != Util.getIntValue(wftypeid, 0)) {
			 		continue;
			 	}
				
				while(WorkflowComInfo.next()){
					String wfname=WorkflowComInfo.getWorkflowname();
					String wfid = WorkflowComInfo.getWorkflowid();
					String curtypeid = WorkflowComInfo.getWorkflowtype();
					String agentname="";
					ArrayList agenterlist=new ArrayList();
					
					if(!curtypeid.equals(wftypeid)) continue;
					
					if(!"".equals(workflowName) && wfname.indexOf(workflowName) == -1) continue;

					//check right
					if(selectedworkflow.indexOf("W"+wfid+"|")==-1&& isuserdefault.equals("1")) continue;
					
					if(NewWorkflows.indexOf(wfid)==-1){
						if(AgentWorkflows.indexOf(wfid)==-1){
							continue;
						}else{
							agenterlist=Util.TokenizerString((String)Agenterids.get(AgentWorkflows.indexOf(wfid)),",");
							for(int k=0;k<agenterlist.size();k++){
								total++;
							}
						}
					}else{
						total++;
					}
				}
				WorkflowComInfo.setTofirstRow();
			}
		} catch (Exception e) {
			e.printStackTrace();
			writeLog(e);
		}
		
		return total;
	}

	public WorkflowBaseInfo[] getCreateWorkflowTypeList(int pageNo, int pageSize, int recordCount, int userid, String[] conditions) {
		if (pageNo < 1)
			pageNo = 1;
		List wbis = new ArrayList();
		try {
			ResourceComInfo resourceComInfo = new ResourceComInfo();
			ShareManager shareManager = new ShareManager();
			//获取流程新建权限体系sql条件
			String wfcrtSqlWhere = shareManager.getWfShareSqlWhere(this.getUser(userid), "t1");
			
			String select = " select distinct ";
			String fields = " t2.workflowtype,t3.typename ";
			String from = " from ShareInnerWfCreate t1,workflow_base t2,workflow_type t3 ";
			String where = " where t1.workflowid=t2.id and t2.workflowtype = t3.id and t2.isvalid in('1','3') and ( ";
			where += wfcrtSqlWhere;
			where += " ) ";
			if (conditions != null)
				for (int m=0;m<conditions.length;m++) {
					String condition = conditions[m];
					where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
				}
			String orderby = " order by t3.typename desc,t2.workflowtype desc ";
			String orderby1 = " order by typename asc,workflowtype asc ";
			String orderby2 = " order by typename desc,workflowtype desc ";

			String sql = getPaginationSql(select, fields, from, where, orderby, orderby1, orderby2, pageNo, pageSize, recordCount);

			RecordSet rs = new RecordSet();

			rs.executeSql(sql);
			while (rs.next()) {
				WorkflowBaseInfo wbi = new WorkflowBaseInfo();

				wbi.setWorkflowId("");
				wbi.setWorkflowName("");
				wbi.setWorkflowTypeId(rs.getString("workflowtype"));
				wbi.setWorkflowTypeName(rs.getString("typename"));

				wbis.add(wbi);
			}
		} catch (Exception e) {
			e.printStackTrace();
			writeLog(e);
		}
		WorkflowBaseInfo[] wbiarrays = new WorkflowBaseInfo[wbis.size()];
		for (int i = 0; i < wbis.size(); i++)
			wbiarrays[i] = (WorkflowBaseInfo) wbis.get(i);
		return wbiarrays;
	}

	public int getCreateWorkflowTypeCount(int userid, String[] conditions) {
		int count = 0;
		try {
			ResourceComInfo resourceComInfo = new ResourceComInfo();
			ShareManager shareManager = new ShareManager();
			//获取流程新建权限体系sql条件ShareInnerWfCreate
			String wfcrtSqlWhere = shareManager.getWfShareSqlWhere(this.getUser(userid), "t1");
			/*//2015/11/30 获取次账号信息 Start
			//获取次账号的检索sql文


			//获取所有次账号
			String userBelongTo = User.getBelongtoidsByUserId(userid);
			//所有次账号的拼接sql文列表


			List<String> userbelongSqlList = new ArrayList<String>();
			if(StringUtils.isNotEmpty(userBelongTo)){
			    String[] userbelongArray = userBelongTo.split(",");
			    //循环所有次账号信息，根据次账号id生成拼接sql文


			    for(String userbelongStr : userbelongArray){
		            //如果取得的次账号信息为空，该条此账号不作处理
		            if(StringUtils.isEmpty(userbelongStr)){
		                continue;
		            }
		            int userbelong = Util.getIntValue(userbelongStr);
		            //如果int化失败，该条此账号不作处理


		            if(userbelong <= 0){
                        continue;
		            }
		            String userbelongSq = shareManager.getWfShareSqlWhere(this.getUser(userbelong), "t1");
		            userbelongSqlList.add(userbelongSq);
			    }
			}
			//2015/11/30 获取次账号信息 End
*/			
			String select = " select distinct ";
			String fields = " t2.workflowtype,t3.typename ";
			String from = " from ShareInnerWfCreate t1,workflow_base t2,workflow_type t3 ";
			String where = " where t1.workflowid=t2.id and t2.workflowtype = t3.id and t2.isvalid in('1','3') and ( ";
			where += wfcrtSqlWhere;

            /*//2015/11/30 获取次账号信息 Start
			where += "(" + wfcrtSqlWhere + ")";
			StringBuffer userbelongTotalSql = new StringBuffer();
			for(String userbelongSql : userbelongSqlList){
			    userbelongTotalSql.append( " OR (" + userbelongSql + ")");
			}
            where += userbelongTotalSql;
			//2015/11/30 获取次账号信息 End
*/			
			where += " ) ";
			if (conditions != null)
				for (int m=0;m<conditions.length;m++) {
					String condition = conditions[m];
					where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
				}

			String sql = getPaginationCountSql(select, fields, from, where);

			RecordSet rs = new RecordSet();

			rs.executeSql(sql);
			if (rs.next()) {
				count = rs.getInt("my_count");
			}

		} catch (Exception e) {
			e.printStackTrace();
			writeLog(e);
		}
		return count;
	}

	public WorkflowRequestInfo getCreateWorkflowRequestInfo(int workflowid, int userid) {
		WorkflowRequestInfo wri = new WorkflowRequestInfo();
		try {
			RecordSet rs = new RecordSet();
			char flag = Util.getSeparator() ;
			WorkFlowInit wfi = new WorkFlowInit();
			User user = getUser(userid);
			//User user =ThreadLocalUser.getUser();
			int isfree = 0;

			//检查用户是否有创建权限
			RequestCheckUser requestCheckUser = new RequestCheckUser();

			requestCheckUser.setUserid(userid);
			requestCheckUser.setWorkflowid(workflowid);
			requestCheckUser.setLogintype(user.getLogintype());
			requestCheckUser.checkUser();
			int hasright=requestCheckUser.getHasright();
			if(hasright==0){
				return null;
			}

			WorkflowComInfo WorkflowComInfo = new WorkflowComInfo();
			WorkTypeComInfo workflowTypeComInfo = new WorkTypeComInfo();
			
			ResourceComInfo resourceComInfo = new ResourceComInfo();
			
			WorkflowExtInfo wbi = new WorkflowExtInfo();
			
			//获得当前的日期和时间
			Calendar today = Calendar.getInstance();
			String currentdate = Util.add0(today.get(Calendar.YEAR), 4) + "-" +
			                     Util.add0(today.get(Calendar.MONTH) + 1, 2) + "-" +
			                     Util.add0(today.get(Calendar.DAY_OF_MONTH), 2) ;

			String currenttime = Util.add0(today.get(Calendar.HOUR_OF_DAY), 2) + ":" +
			                     Util.add0(today.get(Calendar.MINUTE), 2) + ":" +
			                     Util.add0(today.get(Calendar.SECOND), 2) ;

			wbi.setWorkflowId(workflowid+"");
			wbi.setWorkflowName(WorkflowComInfo.getWorkflowname(workflowid+""));
			wbi.setWorkflowTypeId(WorkflowComInfo.getWorkflowtype(workflowid+""));
			wbi.setWorkflowTypeName(workflowTypeComInfo.getWorkTypename(WorkflowComInfo.getWorkflowtype(workflowid+"")));
			wbi.setIsBill(WorkflowComInfo.getIsBill(String.valueOf(workflowid)));
			wbi.setFormId(WorkflowComInfo.getFormId(String.valueOf(workflowid)));
			wri.setIsAnnexUpload(WorkflowComInfo.getIsAnnexUpload(String.valueOf(workflowid)));

			
		    String needAffirmance = "";//是否需要确认








		    String defaultName = ""; //是否默认说明
		    rs.executeSql("select needAffirmance,defaultName,isfree from workflow_base where id="+workflowid);
		    if (rs.next()) {
		    	needAffirmance = rs.getString("needAffirmance");
		    	defaultName = rs.getString("defaultName");
		    	isfree = rs.getInt("isfree");
		    }
		    if ("1".equals(needAffirmance)) {
		    	wri.setNeedAffirmance(true);
		    } else {
		    	wri.setNeedAffirmance(false);
		    }

		    String defaultRequestName = "";
		    if ("1".equals(defaultName)) {
				//defaultRequestName = wbi.getWorkflowName()+"-"+user.getLastname()+"-"+currentdate;
				defaultRequestName = wbi.getWorkflowName();
		    }
		    wbi.setIsFreeWorkflow(isfree+"");
		    wri.setWorkflowBaseInfo(wbi);
			wri.setRequestId("0");
			wri.setRequestName(defaultRequestName);
			wri.setRequestLevel("0");
			wri.setMessageType("0");
			wri.setChatsType("0");
			

			String creatorid = userid+"";
			
			weaver.workflow.request.WFLinkInfo wfLinkInfo = new weaver.workflow.request.WFLinkInfo();
			
			int nodeid = -1;
			rs.executeProc("workflow_CreateNode_Select",workflowid+"");
			if(rs.next()) nodeid = Util.getIntValue(Util.null2String(rs.getString(1)));
			String nodetype=wfLinkInfo.getNodeType(nodeid);
			String nodename="";
			
			wri.setCreatorId(creatorid);
			wri.setCreatorName(resourceComInfo.getLastname(user.getUID()+""));
			wri.setCreateTime(currentdate + " " + currenttime);
			wri.setLastOperatorName(resourceComInfo.getLastname(user.getUID()+""));
			wri.setLastOperateTime(currentdate + " " + currenttime);
			
			boolean canView = true; // 是否可以查看
			boolean canEdit = true; // 是否可以编辑
			
			wri.setCanView(canView);
			wri.setCanEdit(canEdit);
			Map map=getRightMenu(wri.getWorkflowBaseInfo().getWorkflowId(), nodeid, wri.getRequestId(), 0, user, nodetype, false,false);
			wri.setSubmitButtonName((String)map.get("submitName"));
			wri.setSubnobackButtonName((String)map.get("subnobackName"));
			wri.setSubbackButtonName((String)map.get("subbackName"));
			wri.setRejectButtonName((String)map.get("rejectName"));
			wri.setForwardButtonName((String)map.get("forwardName"));
			wri.setTakingOpsButtonName((String)map.get("takingopinionsName"));
			wri.setHandleForwardButtonName((String)map.get("HandleForwardName"));
			wri.setForhandbackButtonName((String)map.get("forhandbackName"));
			wri.setForhandnobackButtonName((String)map.get("forhandnobackName"));
			wri.setGivingopinionsName((String)map.get("givingopinionsName"));
			wri.setGivingOpinionsbackName((String)map.get("givingOpinionsbackName"));
			wri.setGivingOpinionsnobackName((String)map.get("givingOpinionsnobackName"));
			wri.setVersion(WorkflowServiceUtil.getHmtlVersion(workflowid+"", nodeid+""));
			
			//设置当前节点是否启用表单签章
			String isFormSignature = "0";
			//读取配置文件，是否开启电子签章。








			boolean flagFormSignature = Prop.getPropValue("weaver_iWebRevision","isUseWebRevision").equalsIgnoreCase("1");
			if(flagFormSignature){
				//获取并设置当前节点是否启用表单签章








				WFForwardManager wffm = new WFForwardManager();
				wffm.init();
				wffm.setWorkflowid(Util.getIntValue(wri.getWorkflowBaseInfo().getWorkflowId(), 0));
				wffm.setNodeid(nodeid);
				wffm.getWFNodeInfo();
				isFormSignature = wffm.getIsFormSignature();
			}
			wri.setIsFormSignature(isFormSignature);
			
			
			//获取并设置当前节点名称








			rs.executeSql("select * from workflow_nodebase where id = " + nodeid);
			if (rs.next())
				nodename = rs.getString("nodename");
			wri.setCurrentNodeName(nodename);
			wri.setCurrentNodeId(nodeid+"");
			wri.setNodeId(String.valueOf(nodeid));
			
			//处理创建流程时的节点前附加操作








			RequestPreAddinoperateManager requestPreAddM = new RequestPreAddinoperateManager();
			requestPreAddM.setCreater(userid);
			requestPreAddM.setOptor(userid);
			requestPreAddM.setWorkflowid(workflowid);
			requestPreAddM.setNodeid(nodeid);
			Hashtable getPreAddRule_hs = requestPreAddM.getPreAddRule();
			
			WorkflowMainTableInfo wmti = getWorkflowMainTableInfo(wri, user, getPreAddRule_hs);
			wri.setWorkflowMainTableInfo(wmti);
			
			//因手机版不支持对明细的编辑，故此处设置成空数组。








			if(WorkflowServiceUtil.isMobileMode(workflowid+"", nodeid+"")){
				wri.setWorkflowDetailTableInfos(new WorkflowDetailTableInfo[]{});
			}else{
				WorkflowDetailTableInfo[] wdt=getWorkflowDetailTableInfos(wri,user);
				wri.setWorkflowDetailTableInfos(wdt);
			}

			wri.setWorkflowRequestLogs(new WorkflowRequestLog[]{});
			
			boolean IsUseMobileHtmlLayout = Prop.getPropValue("Mobile","IsUseMobileHtmlLayout").equalsIgnoreCase("1");
			if(IsUseMobileHtmlLayout) {
				wri = getWorkflowRequestInfoHTMLTemplete(wri, user);
			}
			
			wri.setMustInputRemark(requestService.whetherMustInputRemark(Util.getIntValue(wri.getRequestId(), 0), workflowid, nodeid, userid, 1));
			//流程短语
			wri.setWorkflowPhrases(getWorkflowPhrases(null,userid));
		} catch (Exception e) {
			e.printStackTrace();
			writeLog(e);
		}
		return wri;

	}

	/**
	 * 将WorkflowRequestInfo转换为RequestInfo
	 * 
	 * @param WorkflowRequestInfo
	 * @return RequestInfo
	 */
	private RequestInfo toRequestInfo(WorkflowRequestInfo wri, int userid) throws Exception {
		if(wri==null) return null;
		
		ResourceComInfo resourceComInfo = new ResourceComInfo();
		int formid = 0;
		String isbill = "0";
		RecordSet rs = new RecordSet();
		int workflowid = Util.getIntValue(wri.getWorkflowBaseInfo().getWorkflowId(), 0);
		//单据(系统表单)特殊逻辑
		rs.executeProc("workflow_Workflowbase_SByID", workflowid + "");
		if (rs.next()) {
			formid = Util.getIntValue(rs.getString("formid"), 0);
			isbill = "" + Util.getIntValue(rs.getString("isbill"), 0);
		}
		
		if("1".equals(isbill) && formid == 158) {//报销申请单








			String amount = "0";
			WorkflowDetailTableInfo[] wdtis = wri.getWorkflowDetailTableInfos();
			if(wdtis != null) {
				for(int i=0; i<wdtis.length; i++) {
					WorkflowRequestTableRecord[] wrtrs = wdtis[i].getWorkflowRequestTableRecords();
					if(wrtrs != null) {
						for(int j=0; j<wrtrs.length; j++) {
							if(wrtrs[j]!=null && wrtrs[j].getRecordOrder()==-1) {
								WorkflowRequestTableField[] wrtfs = wrtrs[j].getWorkflowRequestTableFields();
								if(wrtfs != null) {
									for(int k=0; k<wrtfs.length; k++) {
										if(wrtfs[k]!=null && "amount".equals(wrtfs[k].getFieldName())){
											amount = wrtfs[k].getFieldValue();
										}
									}
								}
							}
						}
					}
				}
			}
			
			WorkflowMainTableInfo wmti = wri.getWorkflowMainTableInfo();
			if(wmti!=null) {
				WorkflowRequestTableRecord[] wrtrs = wmti.getRequestRecords();
				if(wrtrs!=null&&wrtrs[0]!=null) {
					for(int i=0;i<wrtrs[0].getWorkflowRequestTableFields().length;i++) {
						WorkflowRequestTableField wrtf = wrtrs[0].getWorkflowRequestTableFields()[i];
						if(wrtf!=null && "total".equals(wrtf.getFieldName())){
							wrtf.setFieldValue(amount);
						}
					}
				}
			}
		}
		
		RequestInfo requestInfo = new RequestInfo();
		
		String requestID = wri.getRequestId();
		int intRequestID = Util.getIntValue(requestID, -1);
		if(intRequestID > 0){
			requestInfo = requestService.getRequest(Util.getIntValue(requestID));
		}
		
		requestInfo.setRequestid(requestID);
		requestInfo.setWorkflowid(wri.getWorkflowBaseInfo().getWorkflowId());
		requestInfo.setCreatorid(wri.getCreatorId());
		requestInfo.setDescription(wri.getRequestName());
		requestInfo.setRequestlevel(wri.getRequestLevel());
		requestInfo.setRemindtype(wri.getMessageType());
		requestInfo.setCharsRemindType(wri.getChatsType());
		requestInfo.setNodeId(wri.getNodeId());
		
		MainTableInfo mainTableInfo = new MainTableInfo();
		List fields = new ArrayList();
		
		WorkflowMainTableInfo wmti = wri.getWorkflowMainTableInfo();
		List lstInoperateFieldIDs = null;
		if(wmti!=null) {
			WorkflowRequestTableRecord[] wrtrs = wmti.getRequestRecords();
			if(wrtrs!=null&&wrtrs[0]!=null) {
				//获取所有被字段联动设置值的字段列表
				List assignmentFieldNames = DynamicDataInput.getAssignmentFieldsByWorkflowID(wri.getWorkflowBaseInfo().getWorkflowId(), 0);
				
				FieldInfo fieldinfo = new FieldInfo();
				fieldinfo.getHtmlAttrField_mobile(Util.getIntValue(wri.getCurrentNodeId(), 0), (ArrayList)assignmentFieldNames);
				
				//如果是创建流程，则获取创建节点的节点前附件加操作字段ID列表。








				
				if(intRequestID <= 0){
					int createNodeID = WFLinkInfo.getCreatNodeId(String.valueOf(workflowid));
					
					//获取当前节点附件操作的字段ID列表
					RequestPreAddinoperateManager requestPreAddM = new RequestPreAddinoperateManager();
					requestPreAddM.setCreater(userid);
					requestPreAddM.setOptor(userid);
					requestPreAddM.setWorkflowid(workflowid);
					requestPreAddM.setNodeid(createNodeID);
					Hashtable getPreAddRule_hs = requestPreAddM.getPreAddRule();
					lstInoperateFieldIDs = (List)getPreAddRule_hs.get("inoperatefields");
				}
				
				for(int i=0;i<wrtrs[0].getWorkflowRequestTableFields().length;i++) {
					WorkflowRequestTableField wrtf = wrtrs[0].getWorkflowRequestTableFields()[i];
					if(wrtf!=null){
						String fieldID = wrtf.getFieldId();
						String fieldName = wrtf.getFieldName();
						//流程创建/提交时manager字段单独做处理


						if("manager".equals(fieldName)) {
							Property field = new Property();
							field.setName("manager");
							
							//获得被代理人
							String beagenter = userid+"";
							rs.executeSql("select agentorbyagentid from workflow_currentoperator where usertype=0 and isremark='0' and requestid="+requestID+" and userid="+userid+" and nodeid="+wri.getCurrentNodeId()+" order by id desc");
							if(rs.next()){
								int tembeagenter=rs.getInt(1);
					    		if(tembeagenter>0) beagenter=""+tembeagenter;
							}
					    	
							String thetempvalue = resourceComInfo.getManagerID(beagenter);
							field.setValue(thetempvalue);
							fields.add(field);
						} else if(!StringUtils.isEmpty(fieldName) 
								&& ((intRequestID <= 0 && !"".equals(wrtf.getFieldValue())) 
										|| (wrtf.isEdit() && wri.isCanEdit())
										|| assignmentFieldNames.indexOf(wrtf.getFieldFormName()) != -1 
										|| (lstInoperateFieldIDs != null && lstInoperateFieldIDs.indexOf(fieldID) != -1) 
							            || WorkflowServiceUtil.isRowColumnRule(""+formid,wrtf.getFieldFormName())
										|| wrtf.getFieldHtmlType().equals("9")
									)){
							Property field = new Property();
							field.setName(fieldName);
							String thetempvalue = wrtf.getFieldValue();
							if("2".equals(wrtf.getFieldHtmlType())&&"1".equals(wrtf.getFieldType())) {
								thetempvalue = Util.StringReplace(Util.fromScreen2(thetempvalue, 7),"\u00A0","&nbsp;");
								thetempvalue = Util.StringReplace(thetempvalue, "<br>", "\r\n<br>");
							}
							field.setValue(thetempvalue);
							fields.add(field);
						}
					}
				}
			}
		}
		Property[] fieldarray = (Property[]) fields.toArray(new Property[fields.size()]);
		mainTableInfo.setProperty(fieldarray);
		requestInfo.setMainTableInfo(mainTableInfo);
		
		DetailTableInfo detailTableInfo = new DetailTableInfo();
		WorkflowDetailTableInfo[] wdtis = wri.getWorkflowDetailTableInfos();
		
		//手机版暂不支持明细字段编辑功能








		//wdtis = null;
		
		List detailTables = new ArrayList();
		//获取所有被字段联动设置值的字段列表
		List assignmentDetailFieldNames = DynamicDataInput.getAssignmentFieldsByWorkflowID(wri.getWorkflowBaseInfo().getWorkflowId(), 1);

		for(int i=0;wdtis!=null&&i<wdtis.length;i++){
			DetailTable detailTable = new DetailTable();
			
			WorkflowDetailTableInfo wdti = wdtis[i];
			detailTable.setTableDBName(wdti.getTableDBName());
				//老表单的时候需要查询








			if(isbill.equals("1")){ //新表单








				detailTable.setId((i+1)+"");
			}else{//老表单








				detailTable.setId(wdti.getTableDBName());
			}
			detailTable.setDeleteIds(wdti.getDeleteIds());
			detailTable.setRowcount(wdti.getRowcount());
			
			WorkflowRequestTableRecord[] wrtrs = wdti.getWorkflowRequestTableRecords();
			
			List rows = new ArrayList();
			
			for(int j=0;wrtrs!=null&&j<wrtrs.length;j++) {
				
				Row row = new Row();
				row.setId(j+"");
				
				WorkflowRequestTableRecord wrtr = wrtrs[j];
				row.setRowId(wrtr.getRecordId());
				row.setIsDelete(wrtr.getIsDelete());
				WorkflowRequestTableField[] wrtfs = wrtr.getWorkflowRequestTableFields();
				
				List cells = new ArrayList();
				
				for(int k=0;wrtfs!=null&&k<wrtfs.length;k++) {
				
					WorkflowRequestTableField wrtf = wrtfs[k];
					
					if(wrtf!=null) {
						String fieldid = wrtf.getFieldId();
						String cValFields ="field"+fieldid;
					    if(fieldid.indexOf("_")>=0&&fieldid.indexOf("span")==-1){
						    cValFields = "field"+fieldid.split("_")[0];
					    }
						if(!StringUtils.isEmpty(wrtf.getFieldName()) && 
							    (intRequestID <= 0 && !"".equals(wrtf.getFieldValue()))
							    //新追加的明细行或者该明细字段可编辑的时候，保存该字段的值
							    ||((wrtf.isEdit() || "1".equals(wdti.getIsAdd())) && wri.isCanEdit())




							    || assignmentDetailFieldNames.indexOf(cValFields) != -1
							    || (lstInoperateFieldIDs != null && lstInoperateFieldIDs.indexOf(cValFields) != -1) 
						        || WorkflowServiceUtil.isRowColumnRule(""+formid,wrtf.getFieldFormName())
							){
							Cell cell = new Cell();
							
							cell.setName(wrtf.getFieldName());
							cell.setValue(wrtf.getFieldValue()==null?"":wrtf.getFieldValue().replace("\'","&#39;").replace("\"", "&#34;"));
						
							cells.add(cell);
						}
					
					}
				}
				
				if(cells!=null&&cells.size()>0) {
					Cell[] cellarray = (Cell[])cells.toArray(new Cell[cells.size()]);
					row.setCell(cellarray);
				}
				rows.add(row);
			}
			
			if(rows!=null&&rows.size()>0) {
				Row[] rowarray = (Row[])rows.toArray(new Row[rows.size()]);
				detailTable.setRow(rowarray);
			}
			detailTables.add(detailTable);
		}
		DetailTable[] detailTablearray = (DetailTable[])detailTables.toArray(new DetailTable[detailTables.size()]);
		detailTableInfo.setDetailTable(detailTablearray);
		requestInfo.setDetailTableInfo(detailTableInfo);

		return requestInfo;
	}

	/**
	 * 将RequestInfo转换为WorkflowRequestInfo
	 * reqid            请求ID号








	 * userid           用户ID号








	 * fromrequestid    相关请求ID号








	 * pagesize         签字意见加载单面显示条数
	 * isPreLoad        是否为预加载
	 * @param RequestInfo
	 * @return WorkflowRequestInfo
	 */
	private WorkflowRequestInfo getFromRequestInfo(String reqid, int userid, int fromrequestid, int pagesize, boolean isPreLoad, Map<String, Object> otherinfo) throws Exception {
		WorkflowRequestInfo wri = new WorkflowRequestInfo();

		WorkflowAllEComInfo WorkflowComInfo = new WorkflowAllEComInfo();
		WorkTypeComInfo workflowTypeComInfo = new WorkTypeComInfo();
		RequestComInfo requestComInfo = new RequestComInfo();
		ResourceComInfo resourceComInfo = new ResourceComInfo();
		User user = getUser(userid);
		//User user =ThreadLocalUser.getUser();
		//===============================================
		// 整合 START
		//===============================================
		
		WFLinkInfo wfLinkInfoObj = new WFLinkInfo();
		
		String workflowid = "";
		int creater = 0;
		String createTime = "";
		
		String requestname = "";
		String requestlevel = "";
		String messagetype = ""; 
		String chatsType = ""; 
		String status = "";
		String currentnodetype = "";
		
		String nodename = "";
		int nodeid = wfLinkInfoObj.getCurrentNodeid(Util.getIntValue(reqid),userid,Util.getIntValue(user.getLogintype(),1));               //节点id
		String nodetype = wfLinkInfoObj.getNodeType(nodeid);         //节点类型  0:创建 1:审批 2:实现 3:归档
		String operators = "";//自由流程的相关节点操作者






		String signtype = "";//自由流程的会签关系






		String freeNodeId = "";//显示自由节点Id
		String freeNodeName = "";//显示自由节点名称
		//当前节点赋默认值。








		String currentNodeId = String.valueOf(nodeid);
		// 查询请求的相关工作流基本信息
		String sql = "select wn.nodename,wn.operators,wn.signtype,wb.* from workflow_requestbase wb inner JOIN workflow_nodebase wn on wn.id=wb.currentnodeid where wb.requestid=" + reqid;
        RecordSet rs = new RecordSet();
        rs.executeSql(sql);
        if (rs.next()) {
        	workflowid = Util.getIntValue(rs.getString("workflowid"),0) + "";
        	creater = Util.getIntValue(rs.getString("creater"),0);
        	createTime = rs.getString("createdate") + " " + rs.getString("createtime");
        	
        	requestname = rs.getString("requestname");
        	requestlevel = rs.getString("requestlevel");
        	messagetype = rs.getString("messagetype");
        	chatsType = rs.getString("chatsType");
        	status = rs.getString("status");
        	nodename = rs.getString("nodename");
        	//currentNodeId = rs.getString("currentnodeid");
        	//currentnodetype = rs.getString("currentnodetype");
        	operators = rs.getString("operators");
        	signtype = rs.getString("signtype");
        	//if(nodeid<1) nodeid = Util.getIntValue(rs.getString("currentnodeid"),0);
		    //if(nodetype.equals("")) nodetype = Util.null2String(rs.getString("currentnodetype"));
        	
        }
        
        rs.executeQuery("select nodetype from workflow_flownode  where workflowid = ? and nodeid = ?", workflowid,nodeid);
        if(rs.next()){
            currentnodetype = Util.null2String(rs.getString(1));
        }
        
        
        //===============================================
		// 整合 end
		//===============================================
        String basesql = "";
        if("3".equals(currentnodetype)){
        	basesql = "select id,nodename,operators,signtype from workflow_nodebase where requestid=" + reqid + " order by floworder desc ";
        }else if("0".equals(currentnodetype)){
        	basesql = "select id,nodename,operators,signtype from workflow_nodebase where requestid=" + reqid + " order by floworder ";
        }else{
        	basesql = "select id,nodename,operators,signtype from workflow_nodebase where requestid=" + reqid + " and id = " + currentNodeId;
        }
        rs.executeSql(basesql);
        if (rs.next()) {
        	freeNodeId = rs.getString("id");
        	freeNodeName = rs.getString("nodename");
        	operators = rs.getString("operators");
        	signtype = rs.getString("signtype");
        }
        
		wri.setRequestId(reqid);
		wri.setRequestName(requestname);
		wri.setRequestLevel(requestlevel);
		wri.setMessageType(messagetype);
		wri.setChatsType(chatsType);

		wri.setCurrentNodeId(currentNodeId);
		wri.setCurrentNodeType(currentnodetype);
		wri.setFreeNodeId(freeNodeId);
		wri.setFreeNodeName(freeNodeName);
		wri.setOperators(operators);
		wri.setSigntype(signtype);
		wri.setCurrentNodeName(nodename);
		log.info("The current nodeid : " + currentNodeId);
		wri.setStatus(status);
		
		WorkflowExtInfo wbi = new WorkflowExtInfo();
		String workflowType = WorkflowComInfo.getWorkflowtype(WorkflowVersion.getActiveVersionWFID(workflowid));
		wbi.setWorkflowId(workflowid);
		wbi.setWorkflowName(WorkflowComInfo.getWorkflowname(WorkflowVersion.getActiveVersionWFID(workflowid)));
		wbi.setWorkflowTypeId(workflowType);
		wbi.setWorkflowTypeName(workflowTypeComInfo.getWorkTypename(workflowType));
		wbi.setIsBill(WorkflowComInfo.getIsBill(WorkflowVersion.getActiveVersionWFID(workflowid)));
		wbi.setFormId(WorkflowComInfo.getFormId(WorkflowVersion.getActiveVersionWFID(workflowid)));
		wbi.setIsForwardReceiveDef(WorkflowComInfo.getIsForwardReceiveDef(WorkflowVersion.getActiveVersionWFID(workflowid)));
		wri.setWorkflowBaseInfo(wbi);
		wri.setIsAnnexUpload(WorkflowComInfo.getIsAnnexUpload(WorkflowVersion.getActiveVersionWFID(workflowid)));

		wri.setCreatorId(String.valueOf(creater));
		wri.setCreatorName(resourceComInfo.getLastname(String.valueOf(creater)));
		wri.setCreateTime(createTime);
		wri.setLastOperateTime(createTime);
		
		RecordSet rs2 = new RecordSet();
		char flag = Util.getSeparator() ;
		
		int wfcurrrid = -1;
		boolean canView = false ;// 是否可以查看
		boolean canEdit = false; // 是否可以编辑
		boolean canSubmit = false;// 是否可以提交
		rs.executeSql("select id, requestid,isremark,nodeid,groupdetailid from workflow_currentoperator where userid="+userid+" and requestid="+reqid+" order by isremark,islasttimes desc");
		while(rs.next()) {
			canView=true;
			WFCoadjutantManager wfcm = new WFCoadjutantManager();
		    String isremark = Util.null2String(rs.getString("isremark")) ;
		    int tmpnodeid=Util.getIntValue(rs.getString("nodeid"));
		    if(isremark.equals("7")) wfcm.getCoadjutantRights(Util.getIntValue(rs.getString("groupdetailid")));
		    if( isremark.equals("1")||isremark.equals("5") || (isremark.equals("7") && wfcm.getIsmodify().equals("1"))|| isremark.equals("9") ||(isremark.equals("0")  && !nodetype.equals("3")) ) {
		    	canEdit=true;
		    	canView=true;
		    	canSubmit=true;
		    	nodeid=tmpnodeid;
		    	nodetype=wfLinkInfoObj.getNodeType(nodeid);  
		    	wfcurrrid = rs.getInt("id");
		    	break;
		    }
		    if(isremark.equals("8")){
		    	canView=true;
		        break;
		    }
		}
		if(fromrequestid>0){      // 从相关工作流过来,有查看权限








		    rs.executeSql("select count(*) from workflow_currentoperator where userid="+userid+" and requestid="+fromrequestid);
		    if(rs.next()){
		        int counts=rs.getInt(1);
		        if(counts>0){
		        	canView=true;
		        }
		    }
		}

		
		int isremark = -1 ;
		rs.executeSql("select isremark from workflow_currentoperator where (isremark<8 or isremark>8) and requestid="+reqid+" and userid="+userid+" order by isremark");
		while(rs.next())	{
		    int tempisremark = Util.getIntValue(rs.getString("isremark"),0) ;
		    if( tempisremark == 0 || tempisremark == 1 || tempisremark == 5 || tempisremark == 9|| tempisremark == 7) {                       // 当前操作者或被转发者








		        isremark = tempisremark ;
		        break ;
		    }
		}

		if( isremark != 0 && isremark != 1&& isremark != 5 && isremark != 9&& isremark != 7) {
			canEdit = false;
			canSubmit = false;
		}
		
		wri.setNodeId(String.valueOf(nodeid));
		wri.setIsremark(isremark);
		
		WFForwardManager wffm = new WFForwardManager();
		wffm.init();
		wffm.setWorkflowid(Util.getIntValue(workflowid, 0));
		wffm.setNodeid(nodeid);
		wffm.setIsremark(isremark + "");
		wffm.setRequestid(Integer.parseInt(wri.getRequestId()));
		wffm.setBeForwardid(wfcurrrid);
		wffm.getWFNodeInfo();
		
		
		//设置当前表单是否可编辑。








		boolean editbodyactionflag = true;
		boolean IsCanModify = wffm.getCanModify();
		boolean IsCanEditRemark = wffm.getBeForwardCanSubmitOpinion();
		if (isremark == 1 && !IsCanModify) {
			editbodyactionflag = false;
		}
		
		if (!canView) {
		    boolean canShareView = false;
		    //获取通过群组分享相关的信息







	        int isfromchatshare = 0;
	        int sharer = 0;
	        //int sharertype = 0;
	        int sharegroupid = 0;
	        if (otherinfo != null) {
	            isfromchatshare = Util.getIntValue(Util.null2String(otherinfo.get("isfromchatshare")));
	            sharer = Util.getIntValue(Util.null2String(otherinfo.get("sharer")));
	            sharegroupid = Util.getIntValue(Util.null2String(otherinfo.get("sharegroupid")));
	            if (sharer > 0) {
	                wri.setNodeId(String.valueOf(wfLinkInfoObj.getCurrentNodeid(Util.getIntValue(reqid), sharer, 1)));
	            }
	        }
		    //验证是否具备分享的权限




            if (isfromchatshare == 1) {
                canShareView = ChatResourceShareManager.authority(user, ChatResourceShareManager.RESOURCETYPE_WORKLFOW, Util.getIntValue(reqid), sharer, sharegroupid);
            }
            
            /*----------监控-------------*/
            WFUrgerManager WFUrgerManager = new WFUrgerManager();
            boolean canMonitorView = false;
            String ismonitor = "0";
           
            if (otherinfo != null) {
                ismonitor = Util.null2String(otherinfo.get("ismonitor")); 
               
            }
            //验证是否具备监控查看权限
            if ("1".equals(ismonitor)) {
                canMonitorView = WFUrgerManager.getMonitorViewRight(Util.getIntValue(reqid,-1),userid);
            }
            
            
            /*----------督办-------------*/
            boolean canUrgerView = false;
            int isurger = 0;
            int logintype = (user.getLogintype()).equals("1") ? 1 : 2;
            if (otherinfo != null) {
                isurger = Util.getIntValue(Util.null2String(otherinfo.get("isurger")));
               
            }
            //验证是否具备监控查看权限
            if (isurger==1) {
                canUrgerView = WFUrgerManager.UrgerHaveWorkflowViewRight(Util.getIntValue(reqid,-1),userid,logintype);
            }
            
            
            
            
            editbodyactionflag = false;
            canEdit = false;
            IsCanEditRemark = false;
            
            if (isfromchatshare == 1 && !canShareView) {
                return null;
            }
            
           /* if ("1".equals(ismonitor) && !canMonitorView) {
                return null;
            }*/
            
            if (isurger==1 && !canUrgerView) {
                return null;
            }
            
            if(isurger==1 && canUrgerView){
                canView = true;
            }
            
            
        }

        wri.setCanView(canView);
        wri.setCanEdit(canEdit && editbodyactionflag);
        wri.setCanEditRemark(IsCanEditRemark);
        // 设置当前节点是否启用表单签章
        String isFormSignature = "0";
        // 读取配置文件，是否开启电子签章。








		boolean flagFormSignature = Prop.getPropValue("weaver_iWebRevision","isUseWebRevision").equalsIgnoreCase("1");
		if(flagFormSignature){
			isFormSignature = wffm.getIsFormSignature();
		}
		wri.setIsFormSignature(isFormSignature);
		
		
		String requestId = wri.getRequestId();
		/*
		rs.executeSql("select id,nodename from workflow_nodebase where id = (select currentnodeid from Workflow_Requestbase where requestid=" + requestId + ")");
		if (rs.next()){
			currentNodeId = rs.getString("id");
			nodename = rs.getString("nodename");
		}
		
		wri.setCurrentNodeId(currentNodeId);
		wri.setCurrentNodeName(nodename);
		log.info("The current nodeid : " + currentNodeId);
		*/
		
		//设置流程页面按钮
		//应64243要求，对 相关请求 的转发按钮不作控制。    "fromrequestid > 0"    -->>    false
		Map map=getRightMenu(wri.getWorkflowBaseInfo().getWorkflowId(), nodeid, requestId, isremark, user, nodetype, false,isPreLoad);
		wri.setSubmitButtonName((String)map.get("submitName"));
		wri.setSubnobackButtonName((String)map.get("subnobackName"));
		wri.setSubbackButtonName((String)map.get("subbackName"));
		wri.setRejectButtonName((String)map.get("rejectName"));
		wri.setForwardButtonName((String)map.get("forwardName"));
		wri.setTakingOpsButtonName((String)map.get("takingopinionsName"));
		wri.setHandleForwardButtonName((String)map.get("HandleForwardName"));
		wri.setForhandbackButtonName((String)map.get("forhandbackName"));
		wri.setForhandnobackButtonName((String)map.get("forhandnobackName"));
		wri.setGivingopinionsName((String)map.get("givingopinionsName"));
		wri.setGivingOpinionsbackName((String)map.get("givingOpinionsbackName"));
		wri.setGivingOpinionsnobackName((String)map.get("givingOpinionsnobackName"));
		wri.setSubmitDirectName((String)map.get("submitDirectName"));
		wri.setVersion(WorkflowServiceUtil.getHmtlVersion(workflowid+"", nodeid+""));
		/*
		String status = "";
		rs.executeSql("select * from workflow_requestbase where requestid = " + requestId);
		if (rs.next())
			status = rs.getString("status");
		wri.setStatus(status);
		*/

        if(otherinfo != null){
            //应155683要求，添加module属性


            wri.setModule((String)otherinfo.get("module"));
            if("-1004".equals(otherinfo.get("module")) || "-1005".equals(otherinfo.get("module"))){
                wri.setCanEdit(false);
                nodeid = Util.getIntValue(currentNodeId);
            }
        }
		
		WorkflowMainTableInfo wmti = getWorkflowMainTableInfo(wri, user);
		wri.setWorkflowMainTableInfo(wmti);

		//===========================================
		// 查询流程是否包含明细，不包含，则不加载ＳＴＡＲＴ
		//============================================
		
		boolean  hasdetailb=false;
		if(wri.getWorkflowBaseInfo().getIsBill().equals("0")) {
		    rs.executeSql("select count(*) from workflow_formfield  where isdetail='1' and formid=" + wri.getWorkflowBaseInfo().getFormId());
		}else{
		    rs.executeSql("select count(*) from workflow_billfield  where viewtype=1 and billid="+wri.getWorkflowBaseInfo().getFormId());
		}
		if(rs.next()){
		    if(rs.getInt(1)>0) hasdetailb = true;
		}
		
		if(hasdetailb){
			WorkflowDetailTableInfo[] wdtis = getWorkflowDetailTableInfos(wri, user);
			wri.setWorkflowDetailTableInfos(wdtis);
		}
		
		//===========================================
		// 查询流程是否包含明细，不包含，则不加载END
		//============================================
		//===================================================================   
		// 表单加载时，不加载签字意见 等页面load完毕后再通过ajax加载 START                              
		//===================================================================   
		//		WorkflowRequestLog[] workflowRequestLogs = null;
		//		if(fromrequestid > 0){
		//			workflowRequestLogs = getWorkflowRequestLogsFor(workflowid, ri.getRequestid(), userid, pagesize, 0, fromrequestid);
		//		} else {
		//			workflowRequestLogs = getWorkflowRequestLogs(workflowid, ri.getRequestid(), userid, pagesize, 0);
		//		}
		//		wri.setWorkflowRequestLogs(workflowRequestLogs);
		//===================================================================   
		// 表单加载时，不加载签字意见 等页面load完毕后再通过ajax加载 END                                
		//===================================================================   
		wri.setMustInputRemark(requestService.whetherMustInputRemark(Util.getIntValue(wri.getRequestId(), 0), Util.getIntValue(workflowid, 0), nodeid, userid, 1));
		
		//是否允许退回时选择节点
		int isselectrejectnode = 0;
		//退回时是否提醒
		int isrejectremind = 0;
		//退回时是否可选择提醒节点
		int ischangrejectnode = 0;
		//是否是自由流程






		int isfree = 0;
		String isRjtSltNodeSql = "select a.isfree, b.* from workflow_base a, workflow_flownode b where a.id = b.workflowid and a.id=" + workflowid + " and b.nodeid=" + nodeid;
		rs.executeSql(isRjtSltNodeSql);
		if(rs.next()){
			isrejectremind = Util.getIntValue(rs.getString("isrejectremind"), 0);
		    ischangrejectnode = Util.getIntValue(rs.getString("ischangrejectnode"), 0);
		    isselectrejectnode = Util.getIntValue(rs.getString("isselectrejectnode"), 0);
		    isfree = Util.getIntValue(rs.getString("isfree"), 0);
		}
		wri.getWorkflowBaseInfo().setIschangrejectnode(ischangrejectnode);
		wri.getWorkflowBaseInfo().setIsrejectremind(isrejectremind);
		wri.getWorkflowBaseInfo().setIsselectrejectnode(isselectrejectnode);
		wri.getWorkflowBaseInfo().setIsFreeWorkflow(isfree+"");
		
		//流程短语
		wri.setWorkflowPhrases(getWorkflowPhrases(null,userid));
		
		
		boolean IsUseMobileHtmlLayout = Prop.getPropValue("Mobile","IsUseMobileHtmlLayout").equalsIgnoreCase("1");
		if(IsUseMobileHtmlLayout) {
			wri = getWorkflowRequestInfoHTMLTemplete(wri, user);
		}
		
		//是否需要确认








	    String needAffirmance = "";
	    rs.executeSql("select needAffirmance from workflow_base where id="+workflowid);
	    if (rs.next()) {
	    	needAffirmance = rs.getString("needAffirmance");
	    }
	    if ("1".equals(needAffirmance)) {
	    	wri.setNeedAffirmance(true);
	    } else {
	    	wri.setNeedAffirmance(false);
	    }
	    
	    //签字意见
	    char flag1 = Util.getSeparator();
	    int usertype = (user.getLogintype()).equals("1") ? 0 : 1;
	    
	    String myremark = "" ;
	    //签字意见相关附件
	    String annexDocIDs = "";
	    rs.executeProc("workflow_RequestLog_SBUser",""+requestId+flag1+""+userid+flag1+""+usertype+flag1+"1");
        if(rs.next()){
           myremark = Util.null2String(rs.getString("remark"));
           //去掉签字意见中所添加的特定标识符。








           int index = myremark.indexOf("<br/><br/><span style='font-size:11px;color:#666;'>来自");
           if(index > -1){
        	   myremark = myremark.substring(0, index);
           }
           myremark = WorkflowServiceUtil.splitAndFilterString(myremark,10000);
           
           annexDocIDs = rs.getString("ANNEXDOCIDS");
        }
        wri.setRemark(myremark);
        wri.setSignatureAppendfix(annexDocIDs);
        
        
        String templetStatus = "0";//套红状态:0.非套红节点,1.套红节点未套红,2.已套红








        String signatureStatus = "0";//签章状态:0.非签章节点或已签章,1.签章节点未签章








        
        RequestDoc flowDoc = new RequestDoc();
        RequestUseTempletManager rutm = new RequestUseTempletManager();
        RequestSignatureManager rsm = new RequestSignatureManager();
        
        String isworkflowdoc = "0";//是否为公文








        boolean docFlag=flowDoc.haveDocFiled(workflowid,""+nodeid);
        if (docFlag) isworkflowdoc = "1";
        
        if("1".equals(isworkflowdoc) && isremark != 1 && isremark != 9) {
        	int docfieldvalue = -1;
        	rs.executeSql("SELECT * FROM workflow_createdoc WHERE workFlowID = " + workflowid);
        	if(rs.next()) {
        		int flowDocField = rs.getInt("flowDocField");
        		if(flowDocField > 0) {
        			WorkflowRequestTableField[] wrtf = wmti.getRequestRecords()[0].getWorkflowRequestTableFields();
                	for(int i=0; i<wrtf.length; i++) {
                		int docfieldid = Util.getIntValue(wrtf[i].getFieldId());
                		if(docfieldid == flowDocField) {
                			docfieldvalue = Util.getIntValue(wrtf[i].getFieldValue());
                		}
                	}
        		}
        	}
        	
        	if(docfieldvalue > 0) {
        		boolean hasUseTempletSucceed = rutm.ifHasUseTempletSucceed(Util.getIntValue(requestId, 0));
        		boolean isUseTempletNode = rutm.ifIsUseTempletNode(Util.getIntValue(requestId, 0),user.getUID(),user.getLogintype());
        		boolean hasSignatureSucceed = rsm.ifHasSignatureSucceed(Util.getIntValue(requestId, 0),nodeid,user.getUID(),Util.getIntValue(user.getLogintype(),1));
                
        		if(isUseTempletNode) {
            		templetStatus = "1";
            		if(hasUseTempletSucceed) {
            			templetStatus = "2";
            		}
            	}
            	if(!hasSignatureSucceed) {
            		signatureStatus = "1";
            	}
        	}
        }
        wri.setTempletStatus(templetStatus);
        wri.setSignatureStatus(signatureStatus);

        return wri;
    }
    
    
    /**
     * 将RequestInfo转换为WorkflowRequestInfo reqid 请求ID号



     * 
     * 
     * 
     * 
     * 
     * userid 用户ID号



     * 
     * 
     * 
     * 
     * 
     * fromrequestid 相关请求ID号



     * 
     * 
     * 
     * 
     * 
     * pagesize 签字意见加载单面显示条数 isPreLoad 是否为预加载
     * 
     * @param RequestInfo
     * @return WorkflowRequestInfo
     */
    private WorkflowRequestInfo getFromRequestInfomonitor(String reqid, int userid, int fromrequestid, int pagesize, boolean isPreLoad, Map<String, Object> otherinfo) throws Exception {
        WorkflowRequestInfo wri = new WorkflowRequestInfo();

        WorkflowAllEComInfo WorkflowComInfo = new WorkflowAllEComInfo();
        WorkTypeComInfo workflowTypeComInfo = new WorkTypeComInfo();
        RequestComInfo requestComInfo = new RequestComInfo();
        ResourceComInfo resourceComInfo = new ResourceComInfo();
        String msql = "select * from workflow_currentoperator where requestid = "+reqid+" and nodeid =(select nownodeid from workflow_nownode where requestid = "+ reqid +")";
        RecordSet mrs = new RecordSet();
        mrs.execute(msql);
        if(mrs.next()){
            userid = Util.getIntValue(mrs.getString("userid"), 0);      
        }
        User user = getUser(userid);
        // User user =ThreadLocalUser.getUser();
        // ===============================================
        // 整合 START
        // ===============================================

        WFLinkInfo wfLinkInfoObj = new WFLinkInfo();

        String workflowid = "";
        int creater = 0;
        String createTime = "";

        String requestname = "";
        String requestlevel = "";
        String messagetype = "";
        String chatsType = "";
        String status = "";
        String currentnodetype = "";

        String nodename = "";
        int nodeid = wfLinkInfoObj.getCurrentNodeid(Util.getIntValue(reqid), userid, Util.getIntValue(user.getLogintype(), 1)); // 节点id
        String nodetype = wfLinkInfoObj.getNodeType(nodeid); // 节点类型 0:创建
                                                                // 1:审批 2:实现
                                                                // 3:归档
        String operators = "";// 自由流程的相关节点操作者




        String signtype = "";// 自由流程的会签关系




        String freeNodeId = "";// 显示自由节点Id
        String freeNodeName = "";// 显示自由节点Id
        // 当前节点赋默认值。




        String currentNodeId = String.valueOf(nodeid);
        // 查询请求的相关工作流基本信息
        String sql = "select wb.*, wn.nodename,wn.operators,wn.signtype from workflow_requestbase wb inner JOIN workflow_nodebase wn on wn.id=wb.currentnodeid where wb.requestid=" + reqid;
        RecordSet rs = new RecordSet();
        rs.executeSql(sql);
        if (rs.next()) {
            workflowid = Util.getIntValue(rs.getString("workflowid"), 0) + "";
            creater = Util.getIntValue(rs.getString("creater"), 0);
            createTime = rs.getString("createdate") + " " + rs.getString("createtime");

            requestname = rs.getString("requestname");
            requestlevel = rs.getString("requestlevel");
            messagetype = rs.getString("messagetype");
            chatsType = rs.getString("chatsType");
            status = rs.getString("status");
            nodename = rs.getString("nodename");
            currentNodeId = rs.getString("currentnodeid");
            currentnodetype = rs.getString("currentnodetype");
            operators = rs.getString("operators");
            signtype = rs.getString("signtype");
            if (nodeid < 1)
                nodeid = Util.getIntValue(rs.getString("currentnodeid"), 0);
            if (nodetype.equals(""))
                nodetype = Util.null2String(rs.getString("currentnodetype"));

        }
        currentNodeId = String.valueOf(nodeid);
        // ===============================================
        // 整合 end
        // ===============================================
        String basesql = "";
        if ("3".equals(currentnodetype)) {
            basesql = "select id,nodename,operators,signtype from workflow_nodebase where requestid=" + reqid + " order by floworder desc ";
        } else if ("0".equals(currentnodetype)) {
            basesql = "select id,nodename,operators,signtype from workflow_nodebase where requestid=" + reqid + " order by floworder ";
        } else {
            basesql = "select id,nodename,operators,signtype from workflow_nodebase where requestid=" + reqid + " and id = " + currentNodeId;
        }
        rs.executeSql(basesql);
        if (rs.next()) {
            freeNodeId = rs.getString("id");
            freeNodeName = rs.getString("nodename");
            operators = rs.getString("operators");
            signtype = rs.getString("signtype");
        }

        wri.setRequestId(reqid);
        wri.setRequestName(requestname);
        wri.setRequestLevel(requestlevel);
        wri.setMessageType(messagetype);
        wri.setChatsType(chatsType);

        wri.setCurrentNodeId(currentNodeId);
        wri.setCurrentNodeType(currentnodetype);
        wri.setFreeNodeId(freeNodeId);
        wri.setFreeNodeName(freeNodeName);
        wri.setOperators(operators);
        wri.setSigntype(signtype);
        wri.setCurrentNodeName(nodename);
        log.info("The current nodeid : " + currentNodeId);
        wri.setStatus(status);

        WorkflowExtInfo wbi = new WorkflowExtInfo();
        String workflowType = WorkflowComInfo.getWorkflowtype(WorkflowVersion.getActiveVersionWFID(workflowid));
        wbi.setWorkflowId(workflowid);
        wbi.setWorkflowName(WorkflowComInfo.getWorkflowname(WorkflowVersion.getActiveVersionWFID(workflowid)));
        wbi.setWorkflowTypeId(workflowType);
        wbi.setWorkflowTypeName(workflowTypeComInfo.getWorkTypename(workflowType));
        wbi.setIsBill(WorkflowComInfo.getIsBill(WorkflowVersion.getActiveVersionWFID(workflowid)));
        wbi.setFormId(WorkflowComInfo.getFormId(WorkflowVersion.getActiveVersionWFID(workflowid)));
        wbi.setIsForwardReceiveDef(WorkflowComInfo.getIsForwardReceiveDef(WorkflowVersion.getActiveVersionWFID(workflowid)));
        wri.setWorkflowBaseInfo(wbi);
        wri.setIsAnnexUpload(WorkflowComInfo.getIsAnnexUpload(WorkflowVersion.getActiveVersionWFID(workflowid)));

        wri.setCreatorId(String.valueOf(creater));
        wri.setCreatorName(resourceComInfo.getLastname(String.valueOf(creater)));
        wri.setCreateTime(createTime);
        wri.setLastOperateTime(createTime);

        RecordSet rs2 = new RecordSet();
        char flag = Util.getSeparator();

        int wfcurrrid = -1;
        boolean canView = false;// 是否可以查看
        boolean canEdit = false; // 是否可以编辑
        boolean canSubmit = false;// 是否可以提交
        rs.executeSql("select id, requestid,isremark,nodeid,groupdetailid from workflow_currentoperator where userid=" + userid + " and requestid=" + reqid + " order by isremark,id");
        while (rs.next()) {
            canView = true;
            WFCoadjutantManager wfcm = new WFCoadjutantManager();
            String isremark = Util.null2String(rs.getString("isremark"));
            int tmpnodeid = Util.getIntValue(rs.getString("nodeid"));
            if (isremark.equals("7"))
                wfcm.getCoadjutantRights(Util.getIntValue(rs.getString("groupdetailid")));
            if (isremark.equals("1") || isremark.equals("5") || (isremark.equals("7") && wfcm.getIsmodify().equals("1")) || isremark.equals("9") || (isremark.equals("0") && !nodetype.equals("3"))) {
                canEdit = true;
                canView = true;
                canSubmit = true;
                nodeid = tmpnodeid;
                nodetype = wfLinkInfoObj.getNodeType(nodeid);
                wfcurrrid = rs.getInt("id");
                break;
            }
            if (isremark.equals("8")) {
                canView = true;
                break;
            }
        }
        if (fromrequestid > 0) { // 从相关工作流过来,有查看权限




            rs.executeSql("select count(*) from workflow_currentoperator where userid=" + userid + " and requestid=" + fromrequestid);
            if (rs.next()) {
                int counts = rs.getInt(1);
                if (counts > 0) {
                    canView = true;
                }
            }
        }

        int isremark = -1;
        rs.executeSql("select isremark from workflow_currentoperator where (isremark<8 or isremark>8) and requestid=" + reqid + " and userid=" + userid + " order by isremark");
        while (rs.next()) {
            int tempisremark = Util.getIntValue(rs.getString("isremark"), 0);
            if (tempisremark == 0 || tempisremark == 1 || tempisremark == 5 || tempisremark == 9 || tempisremark == 7) { // 当前操作者或被转发者




                isremark = tempisremark;
                break;
            }
        }

        if (isremark != 0 && isremark != 1 && isremark != 5 && isremark != 9 && isremark != 7) {
            canEdit = false;
            canSubmit = false;
        }

        wri.setNodeId(String.valueOf(nodeid));
        wri.setIsremark(isremark);

        WFForwardManager wffm = new WFForwardManager();
        wffm.init();
        wffm.setWorkflowid(Util.getIntValue(workflowid, 0));
        wffm.setNodeid(nodeid);
        wffm.setIsremark(isremark + "");
        wffm.setRequestid(Integer.parseInt(wri.getRequestId()));
        wffm.setBeForwardid(wfcurrrid);
        wffm.getWFNodeInfo();

        // 设置当前表单是否可编辑。




        boolean editbodyactionflag = true;
        boolean IsCanModify = wffm.getCanModify();
        boolean IsCanEditRemark = wffm.getBeForwardCanSubmitOpinion();
        if (isremark == 1 && !IsCanModify) {
            editbodyactionflag = false;
        }

        if (!canView) {
            boolean canShareView = false;
            // 获取通过群组分享相关的信息




            int isfromchatshare = 0;
            int sharer = 0;
            // int sharertype = 0;
            int sharegroupid = 0;
            if (otherinfo != null) {
                isfromchatshare = Util.getIntValue(Util.null2String(otherinfo.get("isfromchatshare")));
                sharer = Util.getIntValue(Util.null2String(otherinfo.get("sharer")));
                sharegroupid = Util.getIntValue(Util.null2String(otherinfo.get("sharegroupid")));
                if (sharer > 0) {
                    wri.setNodeId(String.valueOf(wfLinkInfoObj.getCurrentNodeid(Util.getIntValue(reqid), sharer, 1)));
                }
            }
            // 验证是否具备分享的权限




            if (isfromchatshare == 1) {
                canShareView = ChatResourceShareManager.authority(user, ChatResourceShareManager.RESOURCETYPE_WORKLFOW, Util.getIntValue(reqid), sharer, sharegroupid);
            }
            editbodyactionflag = false;
            canEdit = false;
            IsCanEditRemark = false;

            if (!canShareView) {
                return null;
            }
        }

        wri.setCanView(canView);
        wri.setCanEdit(canEdit && editbodyactionflag);
        wri.setCanEditRemark(IsCanEditRemark);
        // 设置当前节点是否启用表单签章
        String isFormSignature = "0";
        // 读取配置文件，是否开启电子签章。




        boolean flagFormSignature = Prop.getPropValue("weaver_iWebRevision", "isUseWebRevision").equalsIgnoreCase("1");
        if (flagFormSignature) {
            isFormSignature = wffm.getIsFormSignature();
        }
        wri.setIsFormSignature(isFormSignature);

        String requestId = wri.getRequestId();
        /*
         * rs.executeSql("select id,nodename from workflow_nodebase where id =
         * (select currentnodeid from Workflow_Requestbase where requestid=" +
         * requestId + ")"); if (rs.next()){ currentNodeId = rs.getString("id");
         * nodename = rs.getString("nodename"); }
         * 
         * wri.setCurrentNodeId(currentNodeId);
         * wri.setCurrentNodeName(nodename); log.info("The current nodeid : " +
         * currentNodeId);
         */

        // 设置流程页面按钮
        // 应64243要求，对 相关请求 的转发按钮不作控制。 "fromrequestid > 0" -->> false
        Map map = getRightMenu(wri.getWorkflowBaseInfo().getWorkflowId(), nodeid, requestId, isremark, user, nodetype, false, isPreLoad);
        wri.setSubmitButtonName((String) map.get("submitName"));
        wri.setSubnobackButtonName((String) map.get("subnobackName"));
        wri.setSubbackButtonName((String) map.get("subbackName"));
        wri.setRejectButtonName((String) map.get("rejectName"));
        wri.setForwardButtonName((String) map.get("forwardName"));
        wri.setTakingOpsButtonName((String) map.get("takingopinionsName"));
        wri.setHandleForwardButtonName((String) map.get("HandleForwardName"));
        wri.setForhandbackButtonName((String) map.get("forhandbackName"));
        wri.setForhandnobackButtonName((String) map.get("forhandnobackName"));
        wri.setGivingopinionsName((String) map.get("givingopinionsName"));
        wri.setGivingOpinionsbackName((String) map.get("givingOpinionsbackName"));
        wri.setGivingOpinionsnobackName((String) map.get("givingOpinionsnobackName"));
        wri.setVersion(WorkflowServiceUtil.getHmtlVersion(workflowid + "", nodeid + ""));
        /*
         * String status = ""; rs.executeSql("select * from workflow_requestbase
         * where requestid = " + requestId); if (rs.next()) status =
         * rs.getString("status"); wri.setStatus(status);
         */

        WorkflowMainTableInfo wmti = getWorkflowMainTableInfo(wri, user);
        wri.setWorkflowMainTableInfo(wmti);

        // ===========================================
        // 查询流程是否包含明细，不包含，则不加载ＳＴＡＲＴ
        // ============================================

        boolean hasdetailb = false;
        if (wri.getWorkflowBaseInfo().getIsBill().equals("0")) {
            rs.executeSql("select count(*) from workflow_formfield  where isdetail='1' and formid=" + wri.getWorkflowBaseInfo().getFormId());
        } else {
            rs.executeSql("select count(*) from workflow_billfield  where viewtype=1 and billid=" + wri.getWorkflowBaseInfo().getFormId());
        }
        if (rs.next()) {
            if (rs.getInt(1) > 0)
                hasdetailb = true;
        }

        if (hasdetailb) {
            WorkflowDetailTableInfo[] wdtis = getWorkflowDetailTableInfos(wri, user);
            wri.setWorkflowDetailTableInfos(wdtis);
        }

        // ===========================================
        // 查询流程是否包含明细，不包含，则不加载END
        // ============================================
        // ===================================================================
        // 表单加载时，不加载签字意见 等页面load完毕后再通过ajax加载 START
        // ===================================================================
        // WorkflowRequestLog[] workflowRequestLogs = null;
        // if(fromrequestid > 0){
        // workflowRequestLogs = getWorkflowRequestLogsFor(workflowid,
        // ri.getRequestid(), userid, pagesize, 0, fromrequestid);
        // } else {
        // workflowRequestLogs = getWorkflowRequestLogs(workflowid,
        // ri.getRequestid(), userid, pagesize, 0);
        // }
        // wri.setWorkflowRequestLogs(workflowRequestLogs);
        // ===================================================================
        // 表单加载时，不加载签字意见 等页面load完毕后再通过ajax加载 END
        // ===================================================================
        wri.setMustInputRemark(requestService.whetherMustInputRemark(Util.getIntValue(wri.getRequestId(), 0), Util.getIntValue(workflowid, 0), nodeid, userid, 1));

        // 是否允许退回时选择节点
        int isselectrejectnode = 0;
        // 退回时是否提醒
        int isrejectremind = 0;
        // 退回时是否可选择提醒节点
        int ischangrejectnode = 0;
        // 是否是自由流程




        int isfree = 0;
        String isRjtSltNodeSql = "select isrejectremind,ischangrejectnode,isselectrejectnode,isfree from workflow_base where id=" + workflowid;
        rs.executeSql(isRjtSltNodeSql);
        if (rs.next()) {
            isselectrejectnode = Util.getIntValue(rs.getString("isrejectremind"), 0);
            ischangrejectnode = Util.getIntValue(rs.getString("ischangrejectnode"), 0);
            isselectrejectnode = Util.getIntValue(rs.getString("isselectrejectnode"), 0);
            isfree = Util.getIntValue(rs.getString("isfree"), 0);
        }
        wri.getWorkflowBaseInfo().setIschangrejectnode(ischangrejectnode);
        wri.getWorkflowBaseInfo().setIsrejectremind(isrejectremind);
        wri.getWorkflowBaseInfo().setIsselectrejectnode(isselectrejectnode);
        wri.getWorkflowBaseInfo().setIsFreeWorkflow(isfree + "");

        // 流程短语
        wri.setWorkflowPhrases(getWorkflowPhrases(null, userid));

        boolean IsUseMobileHtmlLayout = Prop.getPropValue("Mobile", "IsUseMobileHtmlLayout").equalsIgnoreCase("1");
        if (IsUseMobileHtmlLayout) {
            wri = getWorkflowRequestInfoHTMLTemplete(wri, user);
        }

        // 是否需要确认




        String needAffirmance = "";
        rs.executeSql("select needAffirmance from workflow_base where id=" + workflowid);
        if (rs.next()) {
            needAffirmance = rs.getString("needAffirmance");
        }
        if ("1".equals(needAffirmance)) {
            wri.setNeedAffirmance(true);
        } else {
            wri.setNeedAffirmance(false);
        }

        // 签字意见
        char flag1 = Util.getSeparator();
        int usertype = (user.getLogintype()).equals("1") ? 0 : 1;

        String myremark = "";
        // 签字意见相关附件
        String annexDocIDs = "";
        rs.executeProc("workflow_RequestLog_SBUser", "" + requestId + flag1 + "" + userid + flag1 + "" + usertype + flag1 + "1");
        if (rs.next()) {
            myremark = Util.null2String(rs.getString("remark"));
            // 去掉签字意见中所添加的特定标识符。




            int index = myremark.indexOf("<br/><br/><span style='font-size:11px;color:#666;'>来自");
            if (index > -1) {
                myremark = myremark.substring(0, index);
            }
            myremark = WorkflowServiceUtil.splitAndFilterString(myremark, 10000);

            annexDocIDs = rs.getString("ANNEXDOCIDS");
        }
        wri.setRemark(myremark);
        wri.setSignatureAppendfix(annexDocIDs);

        String templetStatus = "0";// 套红状态:0.非套红节点,1.套红节点未套红,2.已套红




        String signatureStatus = "0";// 签章状态:0.非签章节点或已签章,1.签章节点未签章




        RequestDoc flowDoc = new RequestDoc();
        RequestUseTempletManager rutm = new RequestUseTempletManager();
        RequestSignatureManager rsm = new RequestSignatureManager();

        String isworkflowdoc = "0";// 是否为公文




        boolean docFlag = flowDoc.haveDocFiled(workflowid, "" + nodeid);
        if (docFlag)
            isworkflowdoc = "1";

        if ("1".equals(isworkflowdoc) && isremark != 1 && isremark != 9) {
            int docfieldvalue = -1;
            rs.executeSql("SELECT * FROM workflow_createdoc WHERE workFlowID = " + workflowid);
            if (rs.next()) {
                int flowDocField = rs.getInt("flowDocField");
                if (flowDocField > 0) {
                    WorkflowRequestTableField[] wrtf = wmti.getRequestRecords()[0].getWorkflowRequestTableFields();
                    for (int i = 0; i < wrtf.length; i++) {
                        int docfieldid = Util.getIntValue(wrtf[i].getFieldId());
                        if (docfieldid == flowDocField) {
                            docfieldvalue = Util.getIntValue(wrtf[i].getFieldValue());
                        }
                    }
                }
            }

            if (docfieldvalue > 0) {
                boolean hasUseTempletSucceed = rutm.ifHasUseTempletSucceed(Util.getIntValue(requestId, 0));
                boolean isUseTempletNode = rutm.ifIsUseTempletNode(Util.getIntValue(requestId, 0), user.getUID(), user.getLogintype());
                boolean hasSignatureSucceed = rsm.ifHasSignatureSucceed(Util.getIntValue(requestId, 0), nodeid, user.getUID(), Util.getIntValue(user.getLogintype(), 1));

                if (isUseTempletNode) {
                    templetStatus = "1";
                    if (hasUseTempletSucceed) {
                        templetStatus = "2";
                    }
                }
                if (!hasSignatureSucceed) {
                    signatureStatus = "1";
                }
            }
        }
        wri.setTempletStatus(templetStatus);
        wri.setSignatureStatus(signatureStatus);

        return wri;
    }

    private User getUser(int userid) {

        User user1 = ThreadLocalUser.getUser();
        ThreadLocalUser.destory();
        // if(user == null){
        User user = new User();
        // }
        try {
            ResourceComInfo rc = new ResourceComInfo();
            DepartmentComInfo dc = new DepartmentComInfo();

            user.setUid(userid);
            if (user1 != null) {
                user.setLanguage(Util.getIntValue("" + user1.getLanguage(), 7));
                // new
                // weaver.general.BaseBean().writeLog("--2416--user1-"+Util.getIntValue(""+user1.getLanguage(),
                // 7));
            } else {
                user.setLanguage(Util.getIntValue(rc.getSystemLanguage("" + userid), 7));
                // new
                // weaver.general.BaseBean().writeLog("--2416--user-"+Util.getIntValue(rc.getSystemLanguage(""
                // + userid), 7));
            }
            user.setLoginid(rc.getLoginID("" + userid));
            user.setFirstname(rc.getFirstname("" + userid));
            user.setLastname(rc.getLastname("" + userid));
            user.setLogintype("1");
            // user.setAliasname(rc.getAssistantID(""+userid));
            // user.setTitle(rs.getString("title"));
            // user.setTitlelocation(rc.getLocationid(""+userid));
            user.setSex(rc.getSexs("" + userid));
            // user.setLanguage(Util.getIntValue(rc.getSystemLanguage("" +
            // userid), 7));
            // user.setTelephone(rc);
            // user.setMobile(rc.getm);
            // user.setMobilecall(rs.getString("mobilecall"));
            user.setEmail(rc.getEmail("" + userid));
            // user.setCountryid();
            user.setLocationid(rc.getLocationid("" + userid));
            user.setResourcetype(rc.getResourcetype("" + userid));
            // user.setStartdate(rc.gets);
            // user.setEnddate(rc.gete);
            // user.setContractdate(rc.getc);
            user.setJobtitle(rc.getJobTitle("" + userid));
            // user.setJobgroup(rs.getString("jobgroup"));
            // user.setJobactivity(rs.getString("jobactivity"));
            user.setJoblevel(rc.getJoblevel("" + userid));
            user.setSeclevel(rc.getSeclevel("" + userid));
            user.setUserDepartment(Util.getIntValue(rc.getDepartmentID("" + userid), 0));
            user.setUserSubCompany1(Util.getIntValue(dc.getSubcompanyid1(user.getUserDepartment() + ""), 0));
            // user.setUserSubCompany2(Util.getIntValue(rs.getString("subcompanyid2"),0));
            // user.setUserSubCompany3(Util.getIntValue(rs.getString("subcompanyid3"),0));
            // user.setUserSubCompany4(Util.getIntValue(rs.getString("subcompanyid4"),0));
            user.setManagerid(rc.getManagerID("" + userid));
            user.setAssistantid(rc.getAssistantID("" + userid));
            // user.setPurchaselimit(rc.getPropValue(""+userid));
            // user.setCurrencyid(rc.getc);
            // user.setLastlogindate(rc.get);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return user;
    }

    /**
     * 取得流程主表HTML模板信息
     * 
     * @param WorkflowMainTableInfo
     * @return WorkflowMainTableInfo
     * @throws Exception
     */
    private WorkflowRequestInfo getWorkflowRequestInfoHTMLTemplete(WorkflowRequestInfo wri, User user) throws Exception {
        RecordSet rs = new RecordSet();
        int workflowid = Util.getIntValue(wri.getWorkflowBaseInfo().getWorkflowId(), 0);
        int requestid = Util.getIntValue(wri.getRequestId(), 0);
        int userID = user.getUID();
        int nodeid = Util.getIntValue(wri.getNodeId());// -1;

        log.info("The 'requestid' value is:\t" + requestid);
        // 如果requestId大于0，表示非创建流程
        if (requestid > 0) {
            // 注：nodeid值此处不能使用当前请求的流转节点，而应该使用当前操作人在当前请求中的节点ID号。







		    if (nodeid <= 0) {
		        nodeid = new WFLinkInfo().getCurrentNodeid(requestid,userID,Util.getIntValue(user.getLogintype(),1));
		    }
		}else{
			nodeid = Util.getIntValue(wri.getCurrentNodeId());
		}
		log.info("The 'nodeid' value is:\t" + nodeid);
		
		String ismode = "";
		rs.executeSql("select ismode from workflow_flownode where workflowid="+workflowid+" and nodeid="+nodeid);
		if(rs.next()) ismode=rs.getString("ismode");
		if(Util.getIntValue(ismode,0)>0){ //非普通模式








		
	    	String[] workflowHtmlTemplete = new String[]{null,null};
	    	String[] workflowHtmlShow = new String[]{null,null};
	    	
	    	WFNodeFieldManager wFNodeFieldManager = new WFNodeFieldManager();
			String syspath = "";
			rs.executeSql(" select * from workflow_nodehtmllayout where type=2 and workflowid = " + workflowid + " and nodeid = " + nodeid +" and isactive=1");
			if(rs.next()){
				syspath = Util.null2String(rs.getString("syspath"));
				String htmlLayout = "";
				String htmlShow = "";
				int version=Util.getIntValue(rs.getString("version"),0);
				try {
					String edesign_script_css = "";
					if(version==2){
						ExcelLayoutManager ExcelLayoutManager=new ExcelLayoutManager();
						int modeid=Util.getIntValue(rs.getString("id"), 0);
						HashMap<String,String> other_pars=new HashMap<String,String>();
						other_pars.put("wfid", Util.null2String(rs.getString("workflowid")));
						other_pars.put("nodeid", Util.null2String(rs.getString("nodeid")));
						other_pars.put("formid", Util.null2String(rs.getString("formid")));
						other_pars.put("isbill", Util.null2String(rs.getString("isbill")));
						other_pars.put("type", Util.null2String(rs.getString("type")));
						other_pars.put("requestid", Util.null2String(wri.getRequestId()));
						other_pars.put("languageid", Util.null2String(user.getLanguage()));
						other_pars.put("isFromApp", "1");
						HashMap<String,String> tempMap = ExcelLayoutManager.analyzeExcelLayout(modeid, other_pars);
						htmlLayout = tempMap.get("temphtml");
						
						edesign_script_css += "<p id=\"edesign_script_css\" style=\"display:none !important\">\n";
						edesign_script_css += "<script type=\"text/javascript\" src=\"/workflow/exceldesign/js/parseLayout_wev8.js\"></script>\n";
						edesign_script_css += "<link rel=\"stylesheet\" type=\"text/css\" href=\"/workflow/exceldesign/css/excelHtml_wev8.css\" />\n";
						edesign_script_css += "<style>"+tempMap.get("tempcss")+"</style>\n";
						if(!"".equals(Util.null2String(tempMap.get("tempscript"))))
							edesign_script_css += "<script>\n"+ Util.null2String(tempMap.get("tempscript")) +"</script>\n";
						edesign_script_css += "</p>";
					}else{
						log.info("The path of template file is: \t" + syspath);
						htmlLayout = wFNodeFieldManager.readHtmlFile(syspath);//取得模板
					}
					//log.info("Following is the content of the template file: \n" + htmlLayout);
					HashMap<String,String> map=	getWorkflowHtmlShow(htmlLayout,wri,user);//解析模板
					htmlShow = map.get("showhtml");
					if(version==2){
						if(wri != null && wri.getWorkflowDetailTableInfos() != null){		//明细新增JS
							int i=0;
						    for(WorkflowDetailTableInfo detailTable:wri.getWorkflowDetailTableInfos()){
							   detailTable.setAddStr(Util.null2String(map.get("detailAddStr"+i)));
							   i++;
						    } 
						}
						htmlLayout += edesign_script_css;
						htmlShow += edesign_script_css;
					}
				} catch(Exception e) {
					writeLog(e);
					e.printStackTrace();
				}
				workflowHtmlTemplete[0] = htmlLayout;
				
				workflowHtmlShow[0] = htmlShow;
			}
	
		   	wri.setWorkflowHtmlShow(workflowHtmlShow);
		    wri.setWorkflowHtmlTemplete(workflowHtmlTemplete);

		}

	    return wri;
	}

	private HashMap<String,String> getWorkflowHtmlShow(String htmlLayout, WorkflowRequestInfo wri, User user) throws Exception {
		HashMap<String,String>	returnMap=new HashMap<String,String>();
		log.info("Start to invoke the 'getWorkflowHtmlShow' method.");
		//获取主表所有字段列表








		WorkflowRequestTableRecord wrtr = wri.getWorkflowMainTableInfo().getRequestRecords()[0];
		WorkflowRequestTableField[] wrtfs = wrtr.getWorkflowRequestTableFields();
		log.info("The count of the fields(wrtfs) is " + wrtfs.length);
		
		//标识当前模板是否将 流程标题显示
		boolean isViewRequestName = false;
        boolean canedit = wri.isCanEdit();
		
		Document doc = Jsoup.parse(htmlLayout,"UTF-8");
		Elements inputs = doc.getElementsByTag("input");
		log.info("The count of the input elements is " + inputs.size());
		RecordSet rs1 = new RecordSet();
		int creater = 0;
		int iscreate = 1;
		if (Util.getIntValue(wri.getRequestId(), 0) > 0) {
			rs1.executeProc("workflow_Requestbase_SByID", wri.getRequestId());
	    	if(rs1.next()){
	    		creater = Util.getIntValue(Util.null2String(rs1.getString("creater")));
	    		iscreate = 0;
	    	}
		}
		
		//--------------------------------------
		// html字段属性功能实现 START
		//--------------------------------------
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String currentdate = sdf.format(new Date());
		Map fieldattrmap = new FieldAttrManager().getFieldAttrMobile(user, Util.getIntValue(wri.getWorkflowBaseInfo().getWorkflowId()), Util.getIntValue(wri.getWorkflowBaseInfo().getIsBill()), Util.getIntValue(wri.getWorkflowBaseInfo().getFormId()), Util.getIntValue(wri.getRequestId()), Util.getIntValue(wri.getNodeId()), iscreate, creater, currentdate);
		
		StringBuffer htmlHiddenElementsb = (StringBuffer)fieldattrmap.get("htmlHiddenElementsb");
		String jsStr = fieldattrmap.get("jsStr").toString();
		Map otherPara_hs = (Map)fieldattrmap.get("otherPara_hs");
		
		Pattern p = Pattern.compile("field\\d+span|this\\.id\\+\"span");
		Matcher m = p.matcher(jsStr.toString());
		while (m.find()) {
			String fieldspnaid = m.group();
			String mobileFieldspnaid = fieldspnaid.replace("span", "_span");
			jsStr = jsStr.replace(fieldspnaid, mobileFieldspnaid);
		}
		//--------------------------------------
		// html字段属性功能实现 END
		//--------------------------------------
		
		StringBuffer attachmentssb = new StringBuffer("[");
		String fieldHtmlType = "";
		String fieldValue = "";
		String fieldType = "";
	    String fileExtend="";
	    String[] docids;
	    String item="";
	    String[] uploadids;
	    String imageFileid="";
	    String rsdata = "";
	    DocManager  docManager=new DocManager();
	    ResourceComInfo  resource=new ResourceComInfo();
	    RecordSet rs = new RecordSet();
	    boolean isloadsqlfield=loadSqlField(wri.getCurrentNodeId(),user.getUID()+"",wri.getRequestId());

		for (Iterator it = inputs.iterator();it.hasNext();) {
			Element input = (Element) it.next();
			Element parent = input.parent();
			String inputname = input.attr("name");
			if(!inputname.startsWith("field") && !inputname.startsWith("label") && !inputname.startsWith("node"))
				continue;
			String inputvalue = input.attr("value");
			int index = 5;
			if(inputname.startsWith("node")){
				index = 4;
			}
			String fieldid = inputname.substring(index);
			
			//如果模板中包含 流程标题 字段，则标识为true
			if("-1".equals(fieldid)){
				isViewRequestName = true;
			}
			
			//字段显示内容
			String fieldshowhtml = "";
			//字段名称显示
			String fieldshowname = inputvalue;
			
			log.info("The current field name is : " + inputname);
			log.info("The current field id is : " + fieldid);
			WorkflowRequestTableField wrtf = null;
			for(int i=0;i<wrtfs.length;i++){
				if(wrtfs[i].getFieldId().equals(fieldid)){
					wrtf = wrtfs[i];
					break;
				}
			}
			
			if(wrtf!=null){
				fieldType = wrtf.getFieldType();
				fieldHtmlType = wrtf.getFieldHtmlType();
				fieldValue = wrtf.getFieldValue();
			    fileExtend="";
			    
			    //前台资源统一显示用，此处生成json数据，供客户端调用 START
                if(wrtf.isView()) {
				    //附件上传
					if ("6".equals(fieldHtmlType)) {
						if (!"".equals(fieldValue)) {
							docids=fieldValue.split(",");
							for (String docid:docids) {
								docManager.resetParameter();
								docManager.setId(Integer.valueOf(docid));
				                
								rs.executeSql("select  imagefilename,imagefileid  from DocImageFile  where docid="+Integer.valueOf(docid));
								if (rs.next()) {
								   fileExtend=rs.getString("imagefilename");
								   imageFileid=rs.getString("imagefileid");
								   if(fileExtend.lastIndexOf(".")>-1){
									   fileExtend=fileExtend.substring(fileExtend.lastIndexOf("."));
								   }else{
									   fileExtend="";
								   }
								} else {
								   fileExtend="";
								}
								docManager.getDocInfoById();
								item = "{" 
									+ "\"filetitle\":\"" + getTransform(docManager.getDocsubject() + fileExtend) 
									+ "\",\"fileauthor\":\"" + getTransform(resource.getLastname("" + docManager.getOwnerid())) 
									+ "\",\"filecreatetime\":\"" + docManager.getDoccreatedate()
									+ "\",\"fileid\":\"" + imageFileid
									+ "\",\"filetype\":\"1\"}";
								attachmentssb.append(item).append(",");
							}	
						}
					} else if("3".equals(fieldHtmlType) && ("9".equals(fieldType) || "37".equals(fieldType))) { //文档和多文档
						if(!"".equals(fieldValue)) { 
							uploadids=fieldValue.split(",");
							for(String docid:uploadids) {
								docManager.resetParameter();
								docManager.setId(Integer.valueOf(docid));
								docManager.getDocInfoById();
								item = "{" 
									+ "\"filetitle\":\"" + getTransform(docManager.getDocsubject())
									+ "\",\"fileauthor\":\"" + getTransform(resource.getLastname("" + docManager.getOwnerid()))
									+ "\",\"filecreatetime\":\"" + docManager.getDoccreatedate()
									+ "\",\"fileid\":\"" + docid
									+ "\",\"filetype\":\"0\"}";
								attachmentssb.append(item).append(",");
							}
						}
					}
				}
				
				log.info("This current field isn't null, the field info exists.");
				//对主表所有字段进行数据替换








				if(wrtf!=null&&wrtf.getFieldShowName()!=null&&!"".equals(wrtf.getFieldShowName())) {
					fieldshowname = wrtf.getFieldShowName();
				}
				
				if(wrtf!=null&&wrtf.getFiledHtmlShow()!=null&&!"".equals(wrtf.getFiledHtmlShow())){
					fieldshowhtml = wrtf.getFiledHtmlShow();
				}
				
				log.info("The 'fieldshowhtml' value of this field is:" + fieldshowhtml);
				
				//如果是字段名称(明细表头)
				if(inputname.startsWith("label")) { //字段名称
					//input.remove();
					//parent.append("<span style=\"white-space:normal;\">"+fieldshowname+"</span>");
					input.after("<span style=\"white-space:normal;\">"+fieldshowname+"</span>");
					input.remove();
				}
				//字段
				if (inputname.startsWith("field")) {
                    if (inputname.startsWith("field")) {
                        if (canedit) {
                            String fieldjsstring = getFieldattrString(Util.getIntValue(fieldid), Util.getIntValue(fieldHtmlType), Util.getIntValue(fieldType), wrtf.isEdit(), otherPara_hs);
                            if (!"".equals(fieldjsstring)) {
                                fieldshowhtml += "<script type='text/javascript'>\n" + fieldjsstring + "</script>";
                            }
                        }
                    }
                    input.after(fieldshowhtml);
                    input.remove();
                }
				
			} else {
				log.info("This current field is null, the field info doesn't exist.");
				//如果是字段名称(明细表头)
				if(inputname.startsWith("label")) { //字段名称
//					input.remove();
//					parent.append("<span style=\"white-space:normal;\">"+fieldshowname+"</span>");
					input.after("<span style=\"white-space:normal;\">"+fieldshowname+"</span>");
					input.remove();
				}
					//节点
				if(inputname.startsWith("node")){
					input.remove();
					FieldInfo fieldinfo = new FieldInfo();
					fieldinfo.setUser(user);
					fieldinfo.setRequestid(Util.getIntValue(wri.getRequestId(), 0));
					String workflowid = wri.getWorkflowBaseInfo().getWorkflowId();
					String currentNodeId = wri.getCurrentNodeId();
					String nodeRemark = fieldinfo.GetNodeRemark(Util.getIntValue(workflowid, 0), Util.getIntValue(fieldid, 0), Util.getIntValue(currentNodeId, 0),2);
					boolean ishtmlmobile =WorkflowServiceUtil.isMobileMode(workflowid,currentNodeId);
					if(ishtmlmobile){ //目前mobilehtml没有支持个性化签章
						if(nodeRemark.indexOf(Util.getSeparator())  != -1){
							String nodeRemarkHead = nodeRemark.substring(0,nodeRemark.indexOf(Util.getSeparator()));
							String nodeRemarkEnd = nodeRemark.substring(nodeRemark.indexOf(Util.getSeparator())+1,nodeRemark.length());
							nodeRemark = nodeRemarkHead+nodeRemarkEnd;
						}
						if(nodeRemark.indexOf(FieldInfo.getNodeSeparator())  != -1){
							String nodeRemarkHead = nodeRemark.substring(0,nodeRemark.indexOf(FieldInfo.getNodeSeparator()));
							String nodeRemarkEnd = nodeRemark.substring(nodeRemark.indexOf(FieldInfo.getNodeSeparator())+1,nodeRemark.length());
							nodeRemark = nodeRemarkHead+nodeRemarkEnd;
						}
					}
					if(nodeRemark!=null) parent.append(Util.null2String(nodeRemark));
				}
			}
		}
		 
	       //如果里面包含元素
	    if (attachmentssb.length()>2) {
			rsdata = attachmentssb.substring(0, attachmentssb.length()-1) + "]";
		} else {
			rsdata = "[]";
		}
		
        rsdata=rsdata.replace("\"", "\\\"");

        StringBuilder detailJsStr = new StringBuilder();
		dealDetailTableData(wri, doc, returnMap, detailJsStr, user); 
		
		Elements bodys = doc.getElementsByTag("body");
		Element body = bodys.first();
		String result = (body!=null)?body.children().outerHtml():"";

        result += "<script>try {if(typeof(formcontainattachs)!='undefined'&&formcontainattachs.length>0){var temparray = JSON.parse(\""+rsdata+"\"); for(var i=0;i<temparray.length;i++){ formcontainattachs.push(temparray[i]); }}else{formcontainattachs = JSON.parse(\""+rsdata+"\");} } catch(e) {}";
        result += detailJsStr.toString();
        result += "</script>";
//System.out.println(result+"<<<<");
		//如果是使用Html模板来创建流程，而且流程标题字段未放入模板中，则
		//将流程标题字段隐藏表单域追加其后，避免流程标题为空的情况发生。


		int requestID = Util.getIntValue(wri.getRequestId(), -1);
		if(!(requestID > 0) && !isViewRequestName){
			String requestTitle = wri.getRequestName();
			String fieldHtmlShow = "<input type=\"hidden\" name=\"requestname\" id=\"requestname\" value=\""+ requestTitle +"\" />";
			result += fieldHtmlShow;
		}
		result += "<script type='text/javascript'>\n" + jsStr.toString() + "</script>";
		result += htmlHiddenElementsb.toString();
		
		//log.info("Following is return value invoke convertData method: \n" + result);
		returnMap.put("showhtml", result);
		return returnMap;
	}
	
	
	private String getFieldattrString(int fieldid, int fieldHtmlType, int fieldType, boolean isedit, Map otherPara_hs) {
		
		String jsStr = "";
		String sqlfieldids = "";
		List sqlfieldidList = (List)otherPara_hs.get("sqlfieldidList");
		List sqlcontentList = (List)otherPara_hs.get("sqlcontentList");
		
		String changefieldid = "#field" + fieldid;
		
		if (isedit && fieldHtmlType == 1 && fieldType == 4) {
			changefieldid = "#field_lable" + fieldid;
		}
		
		if(sqlfieldidList!=null && sqlfieldidList.size()>0){
			for(int i=0; i<sqlfieldidList.size(); i++){
				String sqlfieldid_tmp = Util.null2String((String)sqlfieldidList.get(i)).trim();
				String sqlcontent_tmp = Util.null2String((String)sqlcontentList.get(i)).trim();
				if(!"".equals(sqlcontent_tmp)){
					if(sqlcontent_tmp.indexOf("$"+fieldid+"$") > -1){
						sqlfieldids += (sqlfieldid_tmp+",");
					}
				}
			}
			if(sqlfieldids.length() > 0){
				jsStr += "$(function(){\n" 
					+ "       var changestr = $('" + changefieldid + "').attr('_listener');"
					+ "       if (!!!changestr) { changestr=\"\"; } else {changestr += ';'}"
					+ "       $('" + changefieldid + "').attr('_listener', changestr + \"fieldAttrOperate.doSqlFieldAjax(this, '" + sqlfieldids.substring(0, sqlfieldids.length()-1) + "');\");"
					+ "		  loadListener();\n"
					+ "   });\n";
			}
		}
		
		//数字数值型字段之间的计算、赋值操作


		ArrayList attrfieldidList = (ArrayList)otherPara_hs.get("attrfieldidList");
        ArrayList attrcontentList = (ArrayList)otherPara_hs.get("attrcontentList");
        if(attrfieldidList!=null && attrfieldidList.size()>0){
            String MathFieldJS = "function temp_doMathFieldAttr" + fieldid + "() {\n";
            String MathFieldJSInfo = "";
            for(int i=0; i<attrfieldidList.size(); i++){
                String attrfieldid_tmp = Util.null2String((String)attrfieldidList.get(i));
                String attrcontent_tmp = Util.null2String((String)attrcontentList.get(i));
                if(attrcontent_tmp.indexOf("$"+fieldid+"$") > -1){//作为主动字段，启动onchange事件
                    MathFieldJSInfo +=  "       $('#field" + fieldid + "').val($('" + changefieldid + "').val());\n"
                        + "       doMathFieldAttr" + attrfieldid_tmp + "();\n"
                        + "       $('#field" + attrfieldid_tmp + "').trigger(\"onchange\");\n";
                }
            }
             MathFieldJS += MathFieldJSInfo+"};\n";
             if(!"".equals(MathFieldJSInfo)){
                 jsStr += MathFieldJS;
                 jsStr += "$(function(){\n" 
                        + "    var changestr = $('" + changefieldid + "').attr('_listener');"
                        + "    if (!!!changestr) { changestr=\"\"; } else {changestr += ';'}"
                        + "    $('" + changefieldid + "').attr('_listener', changestr"
                        + "        + \"temp_doMathFieldAttr" + fieldid + "()"
                        + "    \");"
                        + "		loadListener();\n"
                        + "       temp_doMathFieldAttr" + fieldid + "();\n"
                        + "});\n";
             }
           
        }
		
		//日期计算
		String datefieldids = "";
		ArrayList datefieldidList = (ArrayList)otherPara_hs.get("datefieldidList");
		ArrayList datecontentList = (ArrayList)otherPara_hs.get("datecontentList");
		if(datefieldidList!=null && datefieldidList.size()>0){
			for(int i=0; i<datefieldidList.size(); i++){
				String datefieldid_tmp = Util.null2String((String)datefieldidList.get(i)).trim();
				String datecontent_tmp = Util.null2String((String)datecontentList.get(i)).trim();
				if(!"".equals(datecontent_tmp)){
					if(datecontent_tmp.indexOf("$"+fieldid+"$") > -1){
						jsStr += "$(function(){\n" 
							+ "    var changestr = $('" + changefieldid + "').attr('_listener');"
							+ "    if (!!!changestr) { changestr=\"\"; } else {changestr += ';'}"
							+ "    $('" + changefieldid + "').attr('_listener', changestr "
							+ "        + \"doFieldDate" + datefieldid_tmp + "(-1);"
							+ "    \");"
							+ "		loadListener();\n"
							+ "});\n";
					}
				}
				if(datefieldid_tmp.equals(""+fieldid)){
					jsStr += "function getFieldDateAjax" + fieldid + "(){\n"
							+ "    doFieldDate" + fieldid + "(-1);\n"
							+ "}\n";
					jsStr += "$(function(){\n" 
							+ "    getFieldDateAjax" + fieldid + "();\n"
							+ "});\n";
				}
			}
		}
		
		//SAP取值


		ArrayList sapfieldidList = (ArrayList)otherPara_hs.get("sapfieldidList");
		if(sapfieldidList != null && sapfieldidList.size() > 0){
			for(int i = 0; i<sapfieldidList.size(); i++){
				String attrfieldid_tmp = Util.null2String((String)sapfieldidList.get(i));
				String fieldidtmp = attrfieldid_tmp.substring(0,attrfieldid_tmp.indexOf("-"));
				String attridtmp = attrfieldid_tmp.substring(attrfieldid_tmp.indexOf("-") + 1);
				if(("" + fieldid).equals(fieldidtmp)){
					jsStr += "$(function(){\n" 
						+ "    var changestr = $('" + changefieldid + "').attr('onchange');"
						+ "    if (!!!changestr) { changestr=\"\"; } else {changestr += ';'}"
						+ "    $('" + changefieldid + "').attr('onchange', changestr "
						+ "        + \"doSAPField('"+attridtmp+"',this);"
						+ "    \");"
						+ "});\n";
				}
			}
		}
		
		return jsStr;
	}
	
	/**
	 * 明细解析,(简单模式、高级定制)共用
	 */
	private void dealDetailTableData(WorkflowRequestInfo wri, Document doc, HashMap<String,String> returnMap, StringBuilder detailJsStr, User user) throws Exception {
		if(wri.getWorkflowDetailTableInfos() == null)
			return;
		int i = -1;
		for(WorkflowDetailTableInfo detailTableInfo : wri.getWorkflowDetailTableInfos()){	//wri生成全部明细并按顺序排列
			i++;
			String tableName = "oTable" + i;
			// 获取明细显示数据table标签，如果不存在明细则该数据为空。


			Element eleDetail = doc.getElementById(tableName);
			if(eleDetail == null)	//模板内不存在此明细表
				continue;
			Elements eleTbody = eleDetail.getElementsByTag("tbody");
			if (eleTbody == null || eleTbody.size() == 0)	//TBODY为空
				continue;
			Elements eleTrs = eleTbody.first().children();
			if (eleTrs == null || eleTrs.size() == 0)		//TR为空
				continue;
			boolean seniorset = "y".equals(eleDetail.attr("_seniorset"));	//设计器高级定制模式，支持折行
			
			if(wri.getVersion()==2){	//新表单设计器-明细可编辑，加隐藏域，控制按钮


				eleDetail.after("<input type=\"hidden\" id=\"nodenum"+i+"\" name=\"nodenum"+i+"\" value=\""+("0".equals(wri.getRequestId())?0:detailTableInfo.getWorkflowRequestTableRecords().length)+"\"></input>");
				eleDetail.after("<input type=\"hidden\" id=\"deleteId"+i+"\" name=\"deleteId"+i+"\" ></input>");
				eleDetail.after("<input type=\"hidden\" id=\"deleteRowIndex"+i+"\" name=\"deleteRowIndex"+i+"\" ></input>");
				if(!wri.isCanEdit()||!"1".equals(detailTableInfo.getIsAdd())){
					if(doc.getElementById("$addbutton"+i+"$") != null)
						doc.getElementById("$addbutton"+i+"$").remove();
				}
				boolean canDelDetail = true;
				if(!wri.isCanEdit()||!"1".equals(detailTableInfo.getIsDelete())){
					canDelDetail = false;
					if(doc.getElementById("$delbutton"+i+"$") != null)
						doc.getElementById("$delbutton"+i+"$").remove();
				}
				if(!wri.isCanEdit()){
					if(doc.getElementById("$sapmulbutton"+i+"$") != null)
						doc.getElementById("$sapmulbutton"+i+"$").remove();
				}
				//解析全选按钮

		        if(seniorset && eleDetail.select("input[name=detailSpecialMark][value=20]").size() > 0){
					String checkAllStr = "<input type=\"checkbox\" notbeauty=\"true\" "+(canDelDetail?"":"disabled")+" name=\"check_all_record\" onclick=\"detailOperate.checkAllFun("+i+");\" title=\""+SystemEnv.getHtmlLabelName(556, user.getLanguage())+"\" />";
					eleDetail.select("input[name=detailSpecialMark][value=20]").first().after(checkAllStr).remove();
		        }
				//增加明细是否可编辑标示

				eleDetail.attr("_canedit", wri.isCanEdit()?"y":"n");
			}
			Elements dataTRs = new Elements();
			if(wri.getVersion() == 2){
				dataTRs = eleDetail.select("tr[_target=datarow]");
				if(seniorset){
					//生成隐藏DIV存隐藏对象

					dataTRs.select("td").first().append("<div class=\"detailRowHideArea\"></div>");
					//解析合计
					Elements sumFields = eleDetail.select("input[name^=sumfield]");
					for(Element sumField: sumFields){
						sumField.parent().attr("id", sumField.attr("name").replace("sumfield", "sum"));
						sumField.remove();
					}
				}
			}else{		//Html编辑器模板


				//如果最后一行是数据行，则直接取最后一行进行明细数据解析


				//如果最后一行不是数据行，则将获取倒数第二数据行(含字段)进行明细数据解析
				int size = eleTrs.size();
				for(int j=size-1; j>=size-2 && j>=0; j--) {
					Element eleTr = eleTrs.get(j);
					if(eleTr == null)
						continue;
					if(eleTr.select("input[name^=field]").size() > 0){
						dataTRs.add(eleTr);
						break;
					}
				}
			}
			if(dataTRs.size() > 0 && dataTRs.select("input[name^=field]").size() > 0){		//明细数据行解析


				if(wri.getVersion()==2 && wri.isCanEdit() && "1".equals(detailTableInfo.getIsAdd())){	//新表单设计器，生成添加行的JS
					String addJsStr = this.getDetailAddStr(wri, dataTRs.clone(), i, seniorset, user);
					returnMap.put("detailAddStr"+i, addJsStr);
				}
				String[] dataTR = convertData(wri, dataTRs.clone(), i, seniorset, detailJsStr);
				//如果存在明细数据，则将明细数据追加至tbody标签中。


				if(dataTR != null){
					for (String element : dataTR) {
						dataTRs.first().before(element);		//before追加，顺序正常


					}
				}
				dataTRs.remove();
				if(wri.getVersion()==2 && !seniorset){	//新表单设计器,行列规则生成合计<tfoot>
					String calculateTR = this.getCalculateStr(wri, dataTRs.clone().first(), i, user);
					if(!"".equals(calculateTR)){
						Element tfoot = doc.createElement("tfoot");
						tfoot.append(calculateTR);
						eleTbody.first().after(tfoot.toString());
					}
				}
			}else{
				log.warn("The detail table("+tableName+") is illegal.");
			}
		}
	}
	
	/**
	 * 已有明细数据生成(简单模式、高级定制)共用
	 * @param eleTr		数据行对象


	 * @param groupid	明细groupid
	 * @param seniorset	是否高级定制模式
	 */
	private String[] convertData(WorkflowRequestInfo wri, Elements eleTRs, int groupid, boolean seniorset, StringBuilder detailJsStr) throws Exception {
		if(wri.getVersion()==2 && "0".equals(wri.getRequestId()))	//新表单设计器-新增流程，明细不解析
			return null;
		WorkflowDetailTableInfo[] arrWfDtailTables = wri.getWorkflowDetailTableInfos();
		if(arrWfDtailTables.length <= groupid)
			return null;
		WorkflowDetailTableInfo wfDetailTable = arrWfDtailTables[groupid];
		WorkflowRequestTableRecord[] arrWfrtRecords = wfDetailTable.getWorkflowRequestTableRecords();
		if(arrWfrtRecords == null || arrWfrtRecords.length == 0)		//不存在数据


			return null;
		
		//生成字段顺序List,避免循环记录同时去循环取字段对象
	    ArrayList<String> fieldorder = new ArrayList<String>();
	    for(WorkflowRequestTableField wrtf: arrWfrtRecords[0].getWorkflowRequestTableFields()){
	    	fieldorder.add(wrtf.getFieldId());
	    }
		
		StringBuffer attachmentssb ;
		String fieldID = "";
		String fieldHtmlType = "";
		String fieldValue = "";
		String fieldType = "";
	    String fileExtend="";
	    String[] docids;
	    String item="";
	    String[] uploadids;
	    String imageFileid="";
	    String rsdata = "";
	    DocManager  docManager=new DocManager();
	    ResourceComInfo  resource=new ResourceComInfo();
	    RecordSet rs = new RecordSet();
	    
		WorkflowRequestTableField tempField = null;
		Elements newEleTRs = null;
		Elements newInputs = null;
		boolean hasCheckSingle = eleTRs.select("input[name=detailSpecialMark][value=21]").size() == 1;
		boolean hasSerialNum = eleTRs.select("input[name=detailSpecialMark][value=22]").size() == 1;
		//循环解析明细记录
		int m = 0;
		String[] result = new String[arrWfrtRecords.length];
		int rowIndex = -1;
		for(WorkflowRequestTableRecord wfrtr : arrWfrtRecords){
			rowIndex++;
			newEleTRs = eleTRs.clone();
			newInputs = newEleTRs.select("input[name^=field]");
            attachmentssb = new StringBuffer("[");
            WorkflowRequestTableField[] arrWfRtf = wfrtr.getWorkflowRequestTableFields();
			for(Element inputElm : newInputs){
				fieldID = inputElm.attr("name").substring(5);
				int orderIndex = fieldorder.indexOf(fieldID);
				if(orderIndex == -1 || arrWfRtf.length <= orderIndex)
					continue;
				tempField = arrWfRtf[orderIndex];
				fieldType = tempField.getFieldType();
				fieldHtmlType = tempField.getFieldHtmlType();
				fieldValue = tempField.getFieldValue();
				fileExtend="";
				if (tempField.isView()) {
					if ("6".equals(fieldHtmlType)) {		//附件上传
						if (!"".equals(fieldValue)) {
							docids=fieldValue.split(",");
							for (String docid:docids) {
								docManager.resetParameter();
								docManager.setId(Integer.valueOf(docid));
								rs.executeSql("select  imagefilename,imagefileid  from DocImageFile  where docid="+Integer.valueOf(docid));
								if(rs.next()){
								   fileExtend=rs.getString("imagefilename");
								   imageFileid=rs.getString("imagefileid");
								   
								   if(fileExtend.indexOf(".")!=-1)
									   fileExtend=fileExtend.substring(fileExtend.lastIndexOf("."));
								}else{
								   fileExtend="";
								}
								docManager.getDocInfoById();
								item="{\"filetitle\":\""+getTransform(docManager.getDocsubject()+fileExtend)+"\",\"fileauthor\":\""+getTransform(resource.getLastname(""+docManager.getOwnerid()))+"\",\"" +
						          "filecreatetime\":\""+docManager.getDoccreatedate()+"\",\"fileid\":\""+imageFileid+"\",\"filetype\":\"1\"}";
								attachmentssb.append(item).append(",");
							}	
						}
					} else if("3".equals(fieldHtmlType) && ("9".equals(fieldType) || "37".equals(fieldType))) { //文档和多文档
						if(!"".equals(fieldValue)) { 
							uploadids=fieldValue.split(",");
							for(String docid:uploadids) {
								docManager.resetParameter();
								docManager.setId(Integer.valueOf(docid));
								docManager.getDocInfoById();
								item="{\"filetitle\":\""+getTransform(docManager.getDocsubject())+"\",\"fileauthor\":\""+getTransform(resource.getLastname(""+docManager.getOwnerid()))+"\",\"" +
						        "filecreatetime\":\""+docManager.getDoccreatedate()+"\",\"fileid\":\""+docid+"\",\"filetype\":\"0\"}";
								attachmentssb.append(item).append(",");
							}
						}
					}
                }
				String fieldshowhtml = "";
				//新设计器-明细表支持可编辑
				if(wri.getVersion()==2){
					fieldshowhtml += "<input type=\"hidden\" id=\""+fieldID+""+rowIndex+"\" name=\""+fieldID+""+rowIndex+"\" value=\"isshow"+groupid+"_"+rowIndex+"_"+fieldID+"\">";
					fieldshowhtml += "<div id=\"isshow"+groupid+"_"+rowIndex+"_"+fieldID+"\">";
					if(fieldHtmlType.equals("1")&&fieldType.equals("4")){
						if(tempField.isView()&&!tempField.isEdit()){
							 fieldshowhtml += Util.null2String(tempField.getFiledHtmlShow());
						}else{
							fieldshowhtml += Util.null2String(tempField.getFieldShowValue());
						}
					}else{
						fieldshowhtml += Util.null2String(tempField.getFieldShowValue());
					}
					fieldshowhtml += "</div>";
					if(wri.isCanEdit() && "1".equals(wfDetailTable.getIsEdit())){
						fieldshowhtml += "<div name=\"hiddenEditdiv\" style=\"display:none\">";
						fieldshowhtml += tempField.getFiledHtmlShow();
						fieldshowhtml += "</div>";
					}else{
						fieldshowhtml += "<input type=\"hidden\" id=\"field"+fieldID+"_"+rowIndex+"\" name=\"field"+fieldID+"_"+rowIndex+"\" value=\""+tempField.getFieldValue()+"\" />";
					}
				}else{
					fieldshowhtml += Util.null2String(tempField.getFieldShowValue());
				}
				inputElm.after(fieldshowhtml);
				inputElm.remove();
			}
            if(wri.getVersion()==2){
            	//TR-点击下拉编辑
            	newEleTRs.attr("name", "trView_"+groupid+"_"+rowIndex);
            	if(wri.isCanEdit() && "1".equals(wfDetailTable.getIsEdit())){
            		newEleTRs.attr("onclick", "javascript:detailTrClick("+groupid+", "+rowIndex+")");
            	}
            	String checkStr = "<input type=\"checkbox\" name=\"check_node_"+groupid+"\" _rowindex=\""+rowIndex+"\" value=\""+wfrtr.getRecordId()+"\" onclick=\"event.stopPropagation();\" "+(("1".equals(wfDetailTable.getIsDelete())&&wri.isCanEdit())?"":"disabled")+" />";
            	String serialStr = "<span name=\"detailIndexSpan"+groupid+"\">"+(rowIndex+1)+"</span>";
            	if(seniorset){
            		if(hasCheckSingle){
            			Element checkElm = newEleTRs.select("input[name=detailSpecialMark][value=21]").first();
            			checkElm.after(checkStr);
            			checkElm.remove();
            		}else{
            			newEleTRs.select("div.detailRowHideArea").first().append(checkStr);
            		}
            		if(hasSerialNum){
            			Element serialElm = newEleTRs.select("input[name=detailSpecialMark][value=22]").first();
						serialElm.after(serialStr);
						serialElm.remove();
            		}
            	}else{
	            	newEleTRs.first().children().first().append(checkStr+serialStr);		//TR第一个单元格解析为序号


            	}
            }
            result[m++] = newEleTRs.toString();
			//如果里面包含元素
			if(attachmentssb.length()>2){
				rsdata = attachmentssb.substring(0, attachmentssb.length()-1) + "]";
				rsdata = rsdata.replace("\"", "\\\"");
				detailJsStr.append("trattachs = JSON.parse(\""+rsdata+"\");for(var i=0;i<trattachs.length;i++){if(typeof(formcontainattachs)!='undefined'){formcontainattachs.push(trattachs[i]);}}").append("\n");
			}
		}
		log.info("Following is return value invoke convertData method: \n" + result);
		return result;
	}
	
	/**
	 * 获取主表数据
	 * @param wri
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public WorkflowRequestInfo getWorkflowMainForm(WorkflowRequestInfo wri, User user) throws Exception {
		
		wri.setWorkflowMainTableInfo(getWorkflowMainTableInfo(wri, user, null));
		
        String templetStatus = "0";//套红状态:0.非套红节点,1.套红节点未套红,2.已套红








        String signatureStatus = "0";//签章状态:0.非签章节点或已签章,1.签章节点未签章








        
        RequestDoc flowDoc = new RequestDoc();
        RequestUseTempletManager rutm = new RequestUseTempletManager();
        RequestSignatureManager rsm = new RequestSignatureManager();
        
        String isworkflowdoc = "0";//是否为公文








        boolean docFlag=flowDoc.haveDocFiled(wri.getWorkflowBaseInfo().getWorkflowId(), wri.getNodeId());
        if (docFlag) isworkflowdoc = "1";
        
        RecordSet rs = new RecordSet();
        
        if("1".equals(isworkflowdoc) && wri.getIsremark() != 1 && wri.getIsremark() != 9) {
        	int docfieldvalue = -1;
        	rs.executeSql("SELECT * FROM workflow_createdoc WHERE workFlowID = " + wri.getWorkflowBaseInfo().getWorkflowId());
        	if(rs.next()) {
        		int flowDocField = rs.getInt("flowDocField");
        		if(flowDocField > 0) {
        			WorkflowRequestTableField[] wrtf = wri.getWorkflowMainTableInfo().getRequestRecords()[0].getWorkflowRequestTableFields();
                	for(int i=0; i<wrtf.length; i++) {
                		int docfieldid = Util.getIntValue(wrtf[i].getFieldId());
                		if(docfieldid == flowDocField) {
                			docfieldvalue = Util.getIntValue(wrtf[i].getFieldValue());
                		}
                	}
        		}
        	}
        	
        	if(docfieldvalue > 0) {
        		boolean hasUseTempletSucceed = rutm.ifHasUseTempletSucceed(Util.getIntValue(wri.getRequestId(), 0));
        		boolean isUseTempletNode = rutm.ifIsUseTempletNode(Util.getIntValue(wri.getRequestId(), 0),user.getUID(),user.getLogintype());
        		boolean hasSignatureSucceed = rsm.ifHasSignatureSucceed(Util.getIntValue(wri.getRequestId(), 0),Util.getIntValue(wri.getNodeId()),user.getUID(),Util.getIntValue(user.getLogintype(),1));
                
        		if(isUseTempletNode) {
            		templetStatus = "1";
            		if(hasUseTempletSucceed) {
            			templetStatus = "2";
            		}
            	}
            	if(!hasSignatureSucceed) {
            		signatureStatus = "1";
            	}
        	}
        }
        wri.setTempletStatus(templetStatus);
    	wri.setSignatureStatus(signatureStatus);
		
		return wri;
	}
	
	/**
	 * 获取明细表数据








	 * 
	 * @param wri
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public WorkflowRequestInfo getWorkflowDetailForm(WorkflowRequestInfo wri, User user) throws Exception {
		RecordSet rs = new RecordSet();
		boolean  hasdetailb=false;
		if(wri.getWorkflowBaseInfo().getIsBill().equals("0")) {
		    rs.executeSql("select count(*) from workflow_formfield  where isdetail='1' and formid=" + wri.getWorkflowBaseInfo().getFormId());
		}else{
		    rs.executeSql("select count(*) from workflow_billfield  where viewtype=1 and billid="+wri.getWorkflowBaseInfo().getFormId());
		}
		if(rs.next()){
		    if(rs.getInt(1)>0) hasdetailb = true;
		}
		if(!hasdetailb) return wri;
		
		WorkflowDetailTableInfo[] detailtblinfos = null;
		
		BillManager billManager = getBillManager(wri, user);
		if(billManager != null) {
			//特殊处理
			detailtblinfos = billManager.getWorkflowDetailTableInfos(wri, user);
		}
		
		if (detailtblinfos == null) {
			detailtblinfos = WorkflowServiceUtil.getWorkflowDetailTableInfos4default(wri, user);
		}
		wri.setWorkflowDetailTableInfos(detailtblinfos);
		
		return wri;
	}
	
	/**
	 * 应用于非创建流程，该方法不处理节点前附加操作。








	 * @param wri
	 * @param user
	 * @return
	 * @throws Exception
	 */
	private WorkflowMainTableInfo getWorkflowMainTableInfo(WorkflowRequestInfo wri, User user) throws Exception {
		return getWorkflowMainTableInfo(wri, user, null);
	}
	
	/**
	 * 应用于创建流程时，该方法需处理节点前附加操作。








	 * @param wri
	 * @param user
	 * @param getPreAddRule_hs
	 * @return
	 * @throws Exception
	 */
	private WorkflowMainTableInfo getWorkflowMainTableInfo(WorkflowRequestInfo wri, User user, Hashtable getPreAddRule_hs) throws Exception {
		WorkflowMainTableInfo result = null;
		
		BillManager billManager = getBillManager(wri, user);
		if(billManager != null) {
			//特殊逻辑处理
			result = billManager.getWorkflowMainTableInfo(wri, user, getPreAddRule_hs);
		}
		
		if (result == null) {
			result = WorkflowServiceUtil.getWorkflowMainTableInfo4default(wri, user, getPreAddRule_hs);
		}
		
		return result;
	}

	private WorkflowDetailTableInfo[] getWorkflowDetailTableInfos(WorkflowRequestInfo wri, User user) throws Exception {
		WorkflowDetailTableInfo[] result = null;
		
		BillManager billManager = getBillManager(wri, user);
		if(billManager != null) {
			//特殊处理
			result = billManager.getWorkflowDetailTableInfos(wri, user);
		}
		
		if (result == null) {
			result = WorkflowServiceUtil.getWorkflowDetailTableInfos4default(wri, user);
		}
		
		return result;
	}
	
	private BillManager getBillManager(WorkflowRequestInfo wri, User user) throws Exception {
		RecordSet rs = new RecordSet();
		RecordSet rs1 = new RecordSet();
		BillManager billManager = null;
		String managepage = "";
		int formid = 0;
		String isbill = "0";
		int workflowid = Util.getIntValue(wri.getWorkflowBaseInfo().getWorkflowId(), 0);
		int requestid = Util.getIntValue(wri.getRequestId(), 0);
		
//		rs.executeProc("workflow_Workflowbase_SByID", workflowid + "");
//		if (rs.next()) {
//			formid = Util.getIntValue(rs.getString("formid"), 0);
//			isbill = "" + Util.getIntValue(rs.getString("isbill"), 0);
//		}

		formid = Util.getIntValue(wri.getWorkflowBaseInfo().getFormId(), 0);
		isbill = wri.getWorkflowBaseInfo().getIsBill();
		
		if("1".equals(isbill)&&formid>0) {
			try {
				rs.executeProc("bill_includepages_SelectByID",formid+"");
				if(rs.next()) {
					if (requestid <= 0) {
						managepage = Util.null2String(rs.getString("createpage")).trim();
					} else {
						int groupdetailid = 0;
						String isremark = "";
						int nodeid = -1;
						int wfcurrrid = -1;
						boolean istoManagePage=false;   //add by xhheng @20041217 for TD 1438
						int usertype = Util.getIntValue(user.getLogintype(),1)-1;
						
						String nodetype = "";
						rs1.executeProc("workflow_Requestbase_SByID",requestid+"");
	                	if(rs1.next()){
	                		nodetype = Util.null2String(rs1.getString("currentnodetype"));
	                	}
						
						rs1.executeSql("select isremark,isreminded,preisremark,id,groupdetailid,nodeid from workflow_currentoperator where requestid="+requestid+" and userid="+user.getUID()+" and usertype="+usertype+" order by isremark,id");
						while(rs1.next())	{
							wfcurrrid = rs1.getInt("id");
						    isremark = Util.null2String(rs1.getString("isremark")) ;
						    groupdetailid = Util.getIntValue(rs1.getString("groupdetailid"), 0);
						    nodeid = Util.getIntValue(rs1.getString("nodeid"));
						    //modify by mackjoe at 2005-09-29 td1772 转发特殊处理，转发信息本人未处理一直需要处理即使流程已归档
						    if( isremark.equals("1")||isremark.equals("5") || isremark.equals("7")|| isremark.equals("9") ||(isremark.equals("0")  && !nodetype.equals("3")) ) {
						      //modify by xhheng @20041217 for TD 1438
						      istoManagePage=true;
						      break;
						    }
						    if(isremark.equals("8")){
						        break;
						    }
						}
						
						//参照managerequestNoform.jsp
						WFForwardManager wfm = new WFForwardManager();
		                wfm.init();
		                wfm.setWorkflowid(workflowid);
		                wfm.setNodeid(nodeid);
		                wfm.setIsremark("" + isremark);
		                wfm.setRequestid(requestid);
		                wfm.setBeForwardid(wfcurrrid);
		                wfm.getWFNodeInfo();
		                String IsPendingForward = wfm.getIsPendingForward();
		                String IsBeForward = wfm.getIsBeForward();
		                String IsSubmitForward=wfm.getIsSubmitForward();
		                boolean IsCanSubmit = wfm.getCanSubmit();
		                WFCoadjutantManager wfcm = new WFCoadjutantManager();
		                wfcm.getCoadjutantRights(groupdetailid);
		                String coadsigntype = wfcm.getSigntype();
		                String coadisforward = wfcm.getIsforward();
		                boolean coadCanSubmit = wfcm.getCoadjutantCanSubmit(requestid, wfcurrrid, "" + isremark, coadsigntype);
						
						if((isremark.equals("1")&&!IsCanSubmit)||("7".equals(isremark)&&!coadCanSubmit)){
						    istoManagePage=false;
						}
						
						if(isremark.equals("0")&&!IsCanSubmit) {
							istoManagePage=false;
						}
						
						if(istoManagePage) {
							managepage = Util.null2String(rs.getString("managepage")).trim();
						} else {
							managepage = Util.null2String(rs.getString("viewpage")).trim();
						}
					}
			    }
				
				if(managepage != null && !"".equals(managepage) && managepage.indexOf(".jsp") >= 0) {
					managepage = managepage.substring(0, managepage.indexOf(".jsp"));
				}
				
				if (managepage != null && !"".equals(managepage)) {
					managepage = "weaver.mobile.webservices.workflow.bill."+managepage;
					Class operationClass = Class.forName(managepage);
					billManager = (BillManager)operationClass.newInstance();
				}
			}catch (Exception e) {
				writeLog(e);
				billManager = null;
			}
		}
		
		return billManager;
	}
	
	/**
	 * 取得流程签字意见信息
	 * 
	 * @param RequestInfo
	 * @return WorkflowRequestLog[]
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public WorkflowRequestLog[] getWorkflowRequestLogs(String workflowId, String requestId, int userid, int pagesize, int endId) throws Exception {
		RequestService requestService = new RequestService();
		boolean isOldWf = false;
		RecordSet rs = new RecordSet();
		rs.executeSql("select nodeid from workflow_currentoperator where requestid = " + requestId);
		while(rs.next()){
			if(rs.getString("nodeid") == null || "".equals(rs.getString("nodeid")) || "-1".equals(rs.getString("nodeid"))){
					isOldWf = true;
			}
		}
		
		String creatorNodeId = "-1";
		rs.executeSql("select nodeid from workflow_flownode where workflowid = "+workflowId+" and nodetype = '0'");
		if (rs.next()) {
			creatorNodeId = rs.getString("nodeid");
		}
		
		//获取当前节点的日志查看范围








		String currentNodeId = "";
		String strViewLogIDs = "";
		List canViewIds = new ArrayList();
		rs.executeSql("select nodeid from workflow_currentoperator where requestid="+requestId+" and userid="+userid+" order by receivedate desc ,receivetime desc");
		if(rs.next()){
			currentNodeId = Util.null2String(rs.getString("nodeid"));
			rs.executeSql("select viewnodeids from workflow_flownode where workflowid=" + workflowId + " and nodeid=" + currentNodeId);
			if(rs.next()){
				strViewLogIDs = Util.null2String(rs.getString("viewnodeids"));
			}
			//为空表示流程日志全部不能查看
			if(!"".equals(strViewLogIDs)){
				//查看全部
				if("-1".equals(strViewLogIDs)){
					rs.executeSql("select nodeid from workflow_flownode where workflowid= " + workflowId+" and exists(select 1 from workflow_nodebase where id=workflow_flownode.nodeid and (requestid is null or requestid="+requestId+"))");
					while(rs.next()){
						String tempNodeId = Util.null2String(rs.getString("nodeid"));
						if(!canViewIds.contains(tempNodeId)){
							canViewIds.add(tempNodeId);
						}
					}
				//查看部分
				}else{
					String tempidstrs[] = Util.TokenizerString2(strViewLogIDs, ",");
					for(int i=0;i<tempidstrs.length;i++){
						if(!canViewIds.contains(tempidstrs[i])){
							canViewIds.add(tempidstrs[i]);
						}
					}
				}
			}
		}
		
		String strViewLogIds = "";
		if(canViewIds.size()>0){
			for(int a=0;a<canViewIds.size();a++){
				strViewLogIds += "," + (String)canViewIds.get(a);
			}
			strViewLogIds = strViewLogIds.substring(1);
		} else{
			strViewLogIds = "-1";
		}
		
		User user = getUser(userid);
		//User user =ThreadLocalUser.getUser();
		RequestRemarkRight remarkRight = new RequestRemarkRight();
		String sqlwhere = remarkRight.getRightCondition(Util.getIntValue(requestId,-1),Util.getIntValue(workflowId,-1), userid);
		Log[] logs = requestService.getRequestLogs(requestId, strViewLogIds, pagesize, endId,sqlwhere);
		WorkflowRequestLog[] result = convertRequestLogs(logs, isOldWf, workflowId, requestId, user, creatorNodeId);
		return result;
	}

	public WorkflowRequestLog[] getWorkflowRequestLogsFor(String workflowId, String requestId, int userid, int pagesize, int endId, int formRequestId) throws Exception {
		RequestService requestService = new RequestService();
		boolean isOldWf = false;
		RecordSet rs = new RecordSet();
		rs.executeSql("select nodeid from workflow_currentoperator where requestid = " + requestId);
		while(rs.next()){
			if(rs.getString("nodeid") == null || "".equals(rs.getString("nodeid")) || "-1".equals(rs.getString("nodeid"))){
					isOldWf = true;
			}
		}
		
		String creatorNodeId = "-1";
		rs.executeSql("select nodeid from workflow_flownode where workflowid = "+workflowId+" and nodetype = '0'");
		if (rs.next()) {
			creatorNodeId = rs.getString("nodeid");
		}
		
		//督办监控对应
		if(formRequestId == -99999){
	        rs.executeSql("select workflowid from workflow_requestbase where requestid = "+ requestId);
		}else{
            rs.executeSql("select workflowid from workflow_requestbase where requestid = "+ formRequestId);
		}
        //rs.executeSql("select workflowid from workflow_requestbase where requestid = "+ formRequestId);
		WFManager wfManager = new WFManager();
		if(rs.next()){
			wfManager.setWfid(rs.getInt("workflowid"));
			wfManager.getWfInfo();
		}
		String issignview = wfManager.getIssignview();
		
		String strViewLogIDs = "";
		List canViewIds = new ArrayList();
		//获取源流程的设置中 相关流程意见不显示 参数值。








		if("1".equals(issignview)){
			String viewNodeId = "";
			if(formRequestId == -99999){
			    //对应流程撤销后无法显示签字意见的问题
			    //rs.executeSql("select  a.nodeid from  workflow_currentoperator a  where a.requestid="+requestId+" and  exists (select 1 from workflow_currentoperator b where b.isremark in ('2','4') and b.requestid="+requestId+"  and  a.userid=b.userid) order by receivedate desc ,receivetime desc");
			    rs.executeSql("select  a.nodeid from  workflow_currentoperator a  where a.requestid="+requestId+" and  exists (select 1 from workflow_currentoperator b where b.requestid="+requestId+"  and  a.userid=b.userid) order by receivedate desc ,receivetime desc");
			}else{
			    rs.executeSql("select  a.nodeid from  workflow_currentoperator a  where a.requestid="+requestId+" and  exists (select 1 from workflow_currentoperator b where b.isremark in ('2','4') and b.requestid="+formRequestId+"  and  a.userid=b.userid) and userid="+userid+" order by receivedate desc ,receivetime desc");
			}
			//rs.executeSql("select  a.nodeid from  workflow_currentoperator a  where a.requestid="+requestId+" and  exists (select 1 from workflow_currentoperator b where b.isremark in ('2','4') and b.requestid="+formRequestId+"  and  a.userid=b.userid) and userid="+userid+" order by receivedate desc ,receivetime desc");
			if(rs.next()){
				viewNodeId = rs.getString("nodeid");
				rs.executeSql("select viewnodeids from workflow_flownode where workflowid=" + workflowId + " and nodeid="+viewNodeId);
				if(rs.next()){
					strViewLogIDs = Util.null2String(rs.getString("viewnodeids"));
				}
				
				//为空表示流程日志全部不能查看
				if(!"".equals(strViewLogIDs)){
					//查看全部
					if("-1".equals(strViewLogIDs)){
                        //督办监控对应
			            if(formRequestId == -99999){

	                        rs.executeSql("select nodeid from workflow_flownode where workflowid= " + workflowId+" and exists(select 1 from workflow_nodebase where id=workflow_flownode.nodeid and (requestid is null or requestid="+requestId+"))");
			            }else{

	                        rs.executeSql("select nodeid from workflow_flownode where workflowid= " + workflowId+" and exists(select 1 from workflow_nodebase where id=workflow_flownode.nodeid and (requestid is null or requestid="+formRequestId+"))");
			            }
						//rs.executeSql("select nodeid from workflow_flownode where workflowid= " + workflowId+" and exists(select 1 from workflow_nodebase where id=workflow_flownode.nodeid and (requestid is null or requestid="+formRequestId+"))");
						while(rs.next()){
							String tempNodeId = Util.null2String(rs.getString("nodeid"));
							if(!canViewIds.contains(tempNodeId)){
								canViewIds.add(tempNodeId);
							}
						}
					//查看部分
					}else{
						String tempidstrs[] = Util.TokenizerString2(strViewLogIDs, ",");
						for(int i=0;i<tempidstrs.length;i++){
							if(!canViewIds.contains(tempidstrs[i])){
								canViewIds.add(tempidstrs[i]);
							}
						}
					}
				}
			}
		}else{
			String viewNodeId = "";
			RecordSet rs1 = new RecordSet();
			RecordSet rs2 = new RecordSet();

	        //督办监控对应
            //rs.executeSql("select  distinct a.nodeid from  workflow_currentoperator a  where a.requestid="+requestId+" and  exists (select 1 from workflow_currentoperator b where b.isremark in ('0','2','4') and b.requestid="+formRequestId+"  and  a.userid=b.userid)");
	        if(formRequestId == -99999){
	            rs.executeSql("select  distinct a.nodeid from  workflow_currentoperator a  where a.requestid="+requestId+" and  exists (select 1 from workflow_currentoperator b where b.isremark in ('0','2','4') and b.requestid="+requestId+"  and  a.userid=b.userid)");
	        }else{
	            rs.executeSql("select  distinct a.nodeid from  workflow_currentoperator a  where a.requestid="+requestId+" and  exists (select 1 from workflow_currentoperator b where b.isremark in ('0','2','4') and b.requestid="+formRequestId+"  and  a.userid=b.userid)");
	        }
			while(rs.next()){
				viewNodeId = rs.getString("nodeid");
				rs1.executeSql("select viewnodeids from workflow_flownode where workflowid=" + workflowId + " and nodeid="+ viewNodeId);
				if(rs1.next()){
					strViewLogIDs = Util.null2String(rs1.getString("viewnodeids"));
				}
				
				//为空表示流程日志全部不能查看
				if(!"".equals(strViewLogIDs)){
					//查看全部
					if("-1".equals(strViewLogIDs)){
			            //督办监控对应
                        //rs2.executeSql("select nodeid from workflow_flownode where workflowid= " + workflowId+" and exists(select 1 from workflow_nodebase where id=workflow_flownode.nodeid and (requestid is null or requestid="+formRequestId+"))");
					    if(formRequestId == -99999){
	                        rs2.executeSql("select nodeid from workflow_flownode where workflowid= " + workflowId+" and exists(select 1 from workflow_nodebase where id=workflow_flownode.nodeid and (requestid is null or requestid="+requestId+"))");
					    }else{
                            rs2.executeSql("select nodeid from workflow_flownode where workflowid= " + workflowId+" and exists(select 1 from workflow_nodebase where id=workflow_flownode.nodeid and (requestid is null or requestid="+formRequestId+"))");
					    }
						while(rs2.next()){
							String tempNodeId = Util.null2String(rs2.getString("nodeid"));
							if(!canViewIds.contains(tempNodeId)){
								canViewIds.add(tempNodeId);
							}
						}
					//查看部分
					}else{
						String tempidstrs[] = Util.TokenizerString2(strViewLogIDs, ",");
						for(int i=0;i<tempidstrs.length;i++){
							if(!canViewIds.contains(tempidstrs[i])){
								canViewIds.add(tempidstrs[i]);
							}
						}
					}
				}
			}
		}
		
		String strViewLogIds = "";
		if(canViewIds.size()>0){
			for(int a=0;a<canViewIds.size();a++){
				strViewLogIds += "," + (String)canViewIds.get(a);
			}
			strViewLogIds = strViewLogIds.substring(1);
		} else{
			strViewLogIds = "-1";
		}
		
		User user = getUser(userid);
		//User user =ThreadLocalUser.getUser();
		RequestRemarkRight remarkRight = new RequestRemarkRight();
		String sqlwhere = remarkRight.getRightCondition(Util.getIntValue(requestId,-1),Util.getIntValue(workflowId,-1), userid);
		Log[] logs = requestService.getRequestLogs(requestId, strViewLogIds, pagesize, endId,sqlwhere);
		WorkflowRequestLog[] result = convertRequestLogs(logs, isOldWf, workflowId, requestId, user, creatorNodeId);
		return result;
	}
	
	private static WorkflowRequestLog[] convertRequestLogs(Log[] arrLogs, boolean isOldWf, String workflowId, String requestId, User user, String creatorNodeId)throws Exception{
		DepartmentComInfo dci = new DepartmentComInfo();
		ResourceComInfo rci = new ResourceComInfo();
		CustomerInfoComInfo cici = new CustomerInfoComInfo();
		WFLinkInfo wfli = new WFLinkInfo();
		RecordSet rs = new RecordSet();
		List wrls = new ArrayList();
		
		for (int m=0; arrLogs != null && m < arrLogs.length;m++) {
			Log log = arrLogs[m];
			WorkflowRequestLog wrl = new WorkflowRequestLog();
			String logId = log.getId();
			wrl.setId(logId);
			wrl.setNodeId(log.getNodeid());
			wrl.setNodeName(log.getNode());
			wrl.setOperateDate(log.getOpdate());
			wrl.setOperateTime(log.getOptime());
			wrl.setClientType(log.getClientType());
			
			String returnValue = log.getOptype();
			returnValue = new RequestLogOperateName().getOperateName(workflowId, requestId, log.getNodeid(), log.getOptype(), log.getOperatorid(), user.getLanguage());
			if(returnValue == null || "".equals(returnValue)) {
				continue;
			}
            wrl.setOperateType(returnValue);
			
            wrl.setOperatorId(log.getOperatorid());
            
            //获取代理关系
            String log_operatorDept = log.getOperatordept();
            String log_operator = log.getOperatorid();
            String log_nodeid = log.getNodeid();
            String log_operatortype = log.getOperatortype();
            String log_agentorbyagentid = log.getAgentorbyagentid();
            String log_agenttype = log.getAgenttype();
            if(isOldWf) {
            	if(log_operatortype.equals("0")){
            		if(!"0".equals(Util.null2String(log_operatorDept))&&!"".equals(Util.null2String(log_operatorDept))){
            			wrl.setOperatorDept(dci.getDepartmentname(log_operatorDept));
            		}
            		wrl.setOperatorName(rci.getLastname(log_operator));
            	}else if(log_operatortype.equals("1")){
            		wrl.setOperatorName(cici.getCustomerInfoname(log_operator));
            	}else{
            		wrl.setOperatorName(SystemEnv.getHtmlLabelName(468,user.getLanguage()));
            	}
            } else {
            	if(log_operatortype.equals("0")) {
            		if(!log_agenttype.equals("2")){
            			if(!"0".equals(Util.null2String(log_operatorDept))&&!"".equals(Util.null2String(log_operatorDept))){
            				wrl.setOperatorDept(dci.getDepartmentname(log_operatorDept));
            			}
            			wrl.setOperatorName(rci.getLastname(log_operator));
            		} else if(log_agenttype.equals("2")){
            			if(!(""+log_nodeid).equals(creatorNodeId) || ((""+log_nodeid).equals(creatorNodeId) && !wfli.isCreateOpt(Util.getIntValue(log.getId()),Util.getIntValue(requestId)))){//非创建节点log,必须体现代理关系
            				if(!"0".equals(Util.null2String(rci.getDepartmentID(log_agentorbyagentid)))&&!"".equals(Util.null2String(rci.getDepartmentID(log_agentorbyagentid)))){
            					wrl.setAgentorDept(dci.getDepartmentname(rci.getDepartmentID(log_agentorbyagentid)));
            				}
            				wrl.setAgentor(rci.getResourcename(log_agentorbyagentid) + SystemEnv.getHtmlLabelName(24214,user.getLanguage()));
            				if(!"0".equals(Util.null2String(log_operatorDept))&&!"".equals(Util.null2String(log_operatorDept))){
            					wrl.setOperatorDept(dci.getDepartmentname(log_operatorDept));
            				}
            				wrl.setOperatorName(rci.getLastname(log_operator)+SystemEnv.getHtmlLabelName(24213,user.getLanguage()));
            				//获取代理信息
            				wrl.setOperatorAgentorFrom(rci.getResourcename(log_agentorbyagentid));
                            wrl.setOperatorAgentorTo(rci.getResourcename(log_operator));
            				wrl.setOperatorAgentLog(rci.getLastname(log_operator) + "（" + SystemEnv.getHtmlLabelName(26241,user.getLanguage()) + " " + rci.getResourcename(log_agentorbyagentid) + "）");
            			} else{//创造节点log, 如果设置代理时选中了代理流程创建,同时代理人本身对该流程就具有创建权限,那么该代理人创建节点的log不体现代理关系








            				String agentCheckSql = " select * from workflow_agentConditionSet where workflowId="+ workflowId +" and bagentuid=" + log_agentorbyagentid +
												 " and agenttype = '1' and isCreateAgenter='1' " +
												 " and ( ( (endDate = '" + TimeUtil.getCurrentDateString() + "' and (endTime='' or endTime is null))" +
												 " or (endDate = '" + TimeUtil.getCurrentDateString() + "' and endTime > '" + (TimeUtil.getCurrentTimeString()).substring(11,19) + "' ) ) " +
												 " or endDate > '" + TimeUtil.getCurrentDateString() + "' or endDate = '' or endDate is null)" +
												 " and ( ( (beginDate = '" + TimeUtil.getCurrentDateString() + "' and (beginTime='' or beginTime is null))" +
												 " or (beginDate = '" + TimeUtil.getCurrentDateString() + "' and beginTime < '" + (TimeUtil.getCurrentTimeString()).substring(11,19) + "' ) ) " +
												 " or beginDate < '" + TimeUtil.getCurrentDateString() + "' or beginDate = '' or beginDate is null) order by agentbatch asc  ,id asc ";
            				rs.executeSql(agentCheckSql);
            				if(!rs.next()){
            					if(!"0".equals(Util.null2String(log_operatorDept))&&!"".equals(Util.null2String(log_operatorDept))){
            						wrl.setOperatorDept(dci.getDepartmentname(log_operatorDept));
            					}
            					wrl.setOperatorName(rci.getLastname(log_operator));
            				} else {
            					String isCreator = rs.getString("isCreateAgenter");
            					if(!isCreator.equals("1")){
            						if(!"0".equals(Util.null2String(log_operatorDept))&&!"".equals(Util.null2String(log_operatorDept))){
            							wrl.setOperatorDept(dci.getDepartmentname(log_operatorDept));
            						}
            						wrl.setOperatorName(rci.getLastname(log_operator));
            					} else{
            						//是否有此流程的创建权限








                                    boolean haswfcreate = new ShareManager().hasWfCreatePermission(user, Integer.parseInt(workflowId));
            						
            						if(haswfcreate){
            							if(!"0".equals(Util.null2String(log_operatorDept))&&!"".equals(Util.null2String(log_operatorDept))){
            								wrl.setOperatorDept(dci.getDepartmentname(log_operatorDept));
            							}
            							wrl.setOperatorName(rci.getLastname(log_operator));
            						} else {
            							if(!"0".equals(Util.null2String(rci.getDepartmentID(log_agentorbyagentid)))&&!"".equals(Util.null2String(rci.getDepartmentID(log_agentorbyagentid)))){
            								wrl.setAgentorDept(dci.getDepartmentname(rci.getDepartmentID(log_agentorbyagentid)));
            							}
            							wrl.setAgentor(rci.getResourcename(log_agentorbyagentid) + SystemEnv.getHtmlLabelName(24214,user.getLanguage()));
            							if(!"0".equals(Util.null2String(log_operatorDept))&&!"".equals(Util.null2String(log_operatorDept))){
            								wrl.setOperatorDept(dci.getDepartmentname(log_operatorDept));
            							}
            							wrl.setOperatorName(rci.getLastname(log_operator)+SystemEnv.getHtmlLabelName(24213,user.getLanguage()));
                                        //获取代理信息
                                        wrl.setOperatorAgentorFrom(rci.getResourcename(log_agentorbyagentid));
                                        wrl.setOperatorAgentorTo(rci.getResourcename(log_operator));
                                        wrl.setOperatorAgentLog(rci.getLastname(log_operator) + "（" + SystemEnv.getHtmlLabelName(26241,user.getLanguage()) + " " + rci.getResourcename(log_agentorbyagentid) + "）");
            						}
            					}
            				}
            			}
            		}
            	} else if(log_operatortype.equals("1")){
            		wrl.setOperatorName(cici.getCustomerInfoname(log_operator));
            	}else{
            		wrl.setOperatorName(SystemEnv.getHtmlLabelName(468,user.getLanguage()));
            	}
            }
            
			wrl.setReceivedPersons(log.getReceiver());
			
			String remark = log.getComment();
            //手机端也直接显示用remark
			String remarkClient = remark;
			//处理手机版中所添加的来源的后缀。








			String mobileSuffix = WorkflowSpeechAppend.getMobileSuffix(remark);
			if (mobileSuffix != null) {
				remark = remark.substring(0, remark.lastIndexOf(mobileSuffix));
			}
			
			//获取签字意见中的电子签章部分。







			remark=remark.replaceAll("&#39;", "'");
			String eletriSignature = WorkflowSpeechAppend.getElectrSignatrue(remark);
			
			//用于显示Mobile3.0中E人E本中的手写签批功能。








			String msigntureid = getSigntureFileId(remark);
			remark = WorkflowServiceUtil.splitAndFilterString(remark,5000);
			
			remark = Util.toHtml2(remark);
			
			/*
			//用于显示Mobile3.0中E人E本中的手写签批功能。








			if(msigntureid != null && !"".equals(msigntureid)){
				remark += "<BR/><div ><img src=\"/news/show.do?fileid=" + msigntureid + "\" style=\"width:98%;\"></div>";
			}

			//设置手写签批
			int attenchmentID = log.getHandWrittenSign();
			if(attenchmentID > 0){
            	String strData = WorkflowSpeechAppend.getAppend(attenchmentID);
            	remark += "<BR/><div ><img src=\"data:image/png;base64," + strData + "\" ></div>";
			}
			//设置语音附件
			attenchmentID = log.getSpeechAttachment();
			if(attenchmentID > 0){
				String strData = WorkflowSpeechAppend.getAppend(attenchmentID);
				String speechId = "speech" + logId;
				String tempStr = "<BR/><div name=\"divSpeechDisplay\" class=\"divSpeechDisplay\"><input type=\"hidden\" name=\""+ speechId +"\" id=\""+ speechId +"\" value=\""+ strData +"\">";
				tempStr += "<div class='voicebox'> <div class='outBox'> <div class='innerBox'>";
				tempStr += "	<div class='playbox'> <a href='emobile:play:"+ speechId +"'><div class='playButtonStyle'></div></a> </div>";
				tempStr += "	<div class='outBoxSchedule'> <div class='schedule'></div> <div class='scheduleBottom'></div> </div>";
				tempStr += "</div> </div> </div> </div>";
				remark += tempStr;
			}
			
			if (eletriSignature != null && !"".equals(eletriSignature)){
				remark += eletriSignature;
			}
			if (mobileSuffix != null && !"".equals(mobileSuffix)) {
				remark += mobileSuffix;
			}
			*/
			String _handWrittenSign = "";
			//用于显示Mobile3.0中E人E本中的手写签批功能。








			if(msigntureid != null && !"".equals(msigntureid)){
				wrl.setHandWrittenSign("/news/show.do?fileid=" + msigntureid);
			}

			//设置手写签批
			int attenchmentID = log.getHandWrittenSign();
			if(attenchmentID > 0){
            	String strData = WorkflowSpeechAppend.getAppend(attenchmentID);
            	wrl.setHandWrittenSign("data:image/png;base64," + strData);
            	//获取手写签章详细信息
                DocAttachment docAttObj = WorkflowSpeechAppend.getAttachment(attenchmentID);
                wrl.setHandWrittenSignDoc(docAttObj);
			}
			//设置语音附件
			attenchmentID = log.getSpeechAttachment();
			if(attenchmentID > 0){
			    DocAttachment attenchmentDoc = WorkflowSpeechAppend.getAttachment(attenchmentID);
			    String fileType = attenchmentDoc.getFiletype();
			    if(fileType.indexOf("/") >=0){
			        fileType = fileType.substring(fileType.indexOf("/") + 1);
			    }
				wrl.setSpeechAttachment("data:audio/" + fileType + ";base64," + WorkflowSpeechAppend.getAppend(attenchmentID));


			}
			
			//电子签章
			if (eletriSignature != null && !"".equals(eletriSignature)){
				wrl.setEletriSignature(eletriSignature);
			}
			
			//mobile后缀
			if (mobileSuffix != null && !"".equals(mobileSuffix)) {
				wrl.setMobileSuffix(mobileSuffix);
			}
			
			//抄送时不显示签字意见






            wrl.setRemarkLocation(log.getRemarkLocation());

			if(log.getOptype().equals("t")) remark = "";
			
			wrl.setRemark(remark);
			wrl.setRemarkClient(HtmlUtil.translateMarkup(remarkClient));
			
			
			//签字意见中上传附件的处理
			StringBuffer annexDocHtmls = new StringBuffer();
			String annexdocids = log.getAnnexdocids();
			List<Map<String, String>> annexlist = new ArrayList<Map<String, String>>();
			if (annexdocids!=null&&!"".equals(annexdocids)) {
				//对引用的多个文档ID号列表去除两端的逗号。






				annexdocids = annexdocids.startsWith(",")?annexdocids.substring(1):annexdocids;
				annexdocids = annexdocids.endsWith(",")?annexdocids.substring(0, annexdocids.length()-1):annexdocids;
				/*
		    	if(annexdocids.indexOf(",")==-1){
                    //获取最新版本annexdocid
					rs.executeSql("select i.imagefileid from docimagefile di,imagefile i where di.imagefileid = i.imagefileid and di.docid ="+annexdocids+"");
                    String  imagefileidStr = "";
					if(rs.next()){
						  imagefileidStr =  rs.getString("imagefileid");
					}
					if(!"".equals(imagefileidStr)){
						rs.executeSql("select docid from docimagefile di,imagefile i where di.imagefileid = i.imagefileid and di.imagefileid ="+imagefileidStr+" order by docid desc");
						if(rs.next()){
							  annexdocids =  rs.getString("docid");
						}
					}
					
					//读取流程流转对应文档的附件








					rs.executeSql("select i.imagefileid,di.imagefilename,i.imagefiletype,i.fileSize from docimagefile di,imagefile i where di.imagefileid = i.imagefileid and di.docid in ("+annexdocids+") order by versionId desc ");
					int curIndex = 0;
					if(rs.next()){
						curIndex++;
						String docImagefileid = Util.null2String(rs.getString("imagefileid"));
						String docImagefilename = Util.null2String(rs.getString("imagefilename"));
						int docImagefileSize = Util.getIntValue(Util.null2String(rs.getString("fileSize")),0);
						String downloadDocImageFileName = docImagefilename.replaceAll("&","%26");
						
						String annexurl = "/download.do?fileid=" + docImagefileid + "&filename="+ downloadDocImageFileName;
						annexDocHtmls.append("<span style='text-decoration:underline;color:blue;cursor:hand;");
						if (curIndex > 1) annexDocHtmls.append("padding-left:54px;");
						annexDocHtmls.append("' onclick=\"toURL('");
						annexDocHtmls.append(annexurl);
						annexDocHtmls.append("',false);\" >" + docImagefilename + "(" + (new BigDecimal((docImagefileSize / 1000)+"").setScale(1, BigDecimal.ROUND_HALF_UP)) + "K)</span><br/><br/>");
						
						Map<String, String> annexmap = new HashMap<String, String>();
						annexmap.put("id", docImagefileid);
						annexmap.put("name", downloadDocImageFileName);
						annexmap.put("url", annexurl);
						annexmap.put("size", (new BigDecimal((docImagefileSize / 1000)+"").setScale(1, BigDecimal.ROUND_HALF_UP)) + "K");
						try {
						    String annextype = docImagefilename.substring(docImagefilename.lastIndexOf(".") + 1);
						    annexmap.put("type", annextype);
						} catch (Exception e) {
						    e.printStackTrace();
						}
						annexlist.add(annexmap);
				    }
				}else{
				*/
                    String[] annexdocidsArray = annexdocids.split(",");
					for(int i=0;i<annexdocidsArray.length;i++){
						 String annexdocid = annexdocidsArray[i];
						 //获取最新版本annexdocid
						 rs.executeSql("select i.imagefileid from docimagefile di,imagefile i where di.imagefileid = i.imagefileid and di.docid ="+annexdocid+" order by docid desc");
						 String  imagefileidStr = "";
						 if(rs.next()){
								 imagefileidStr =  rs.getString("imagefileid");
						 }
						 if(!"".equals(imagefileidStr)){
							 rs.executeSql("select docid from docimagefile di,imagefile i where di.imagefileid = i.imagefileid and di.imagefileid ="+imagefileidStr+" order by docid desc");
							 if(rs.next()){
								  annexdocid =  rs.getString("docid");
							 }
						 }
						//读取流程流转对应文档的附件








						rs.executeSql("select i.imagefileid,di.imagefilename,i.imagefiletype,i.fileSize from docimagefile di,imagefile i where di.imagefileid = i.imagefileid and di.docid ='"+annexdocid+"' order by versionId desc ");
						int curIndex = 0;
						if(rs.next()){
							curIndex++;
							String docImagefileid = Util.null2String(rs.getString("imagefileid"));
							String docImagefilename = Util.null2String(rs.getString("imagefilename"));
							String downloadDocImageFileName = docImagefilename.replaceAll("&","%26");
							int docImagefileSize = Util.getIntValue(Util.null2String(rs.getString("fileSize")),0);
							
							String annexurl = "/download.do?fileid=" + docImagefileid + "&filename="+ downloadDocImageFileName;
							
							annexDocHtmls.append("<span style='text-decoration:underline;color:blue;cursor:hand;");
							if (curIndex > 1) annexDocHtmls.append("padding-left:54px;");
							annexDocHtmls.append("' onclick=\"toURL('");
							annexDocHtmls.append(annexurl);
							annexDocHtmls.append("',false);\" >" + docImagefilename + "(" + (new BigDecimal((docImagefileSize / 1000)+"").setScale(1, BigDecimal.ROUND_HALF_UP)) + "K)</span><br/><br/>");
							
							Map<String, String> annexmap = new HashMap<String, String>();
							annexmap.put("id", docImagefileid);
							annexmap.put("name", downloadDocImageFileName);
	                        annexmap.put("url", annexurl);
	                        annexmap.put("size", (new BigDecimal((docImagefileSize / 1000)+"").setScale(1, BigDecimal.ROUND_HALF_UP)) + "K");
	                        try {
	                            String annextype = docImagefilename.substring(docImagefilename.lastIndexOf(".") + 1);
	                            annexmap.put("type", annextype);
	                        } catch (Exception e) {
	                            e.printStackTrace();
	                        }
	                        
	                        annexlist.add(annexmap);
						}
					}
					/*
				}
				*/
			}
			
			wrl.setAnnexDocHtmls(annexDocHtmls.toString());
			wrl.setAnnexDocs(annexlist);
			
			//取得流程签章(表单签章)
			int tempImageFileId=0;
			int tempRequestLogId=Util.getIntValue(log.getRequestLogId());
			if(tempRequestLogId>0){
				rs.executeSql("select imageFileId from Workflow_FormSignRemark where requestLogId="+tempRequestLogId);
				if(rs.next()){
					tempImageFileId=Util.getIntValue(rs.getString("imageFileId"),0);
				}
			}

			if(!log.getOptype().equals("t")){
				if(tempRequestLogId>0&&tempImageFileId>0){
					wrl.setRemarkSign(tempImageFileId+"");
				}
			}
			
			wrl.setOperatorType(log_operatortype);
			//取得个人签章。仅当本次操作为公司内部人员(非客户、供应商)、即logtype为0时才取个性化签章图片。








			if("0".equals(log_operatortype)){
				weaver.workflow.mode.FieldInfo fieldInfo = new weaver.workflow.mode.FieldInfo();
				BaseBean wfsbean=fieldInfo.getWfsbean();
				int showimg = Util.getIntValue(wfsbean.getPropValue("WFSignatureImg","showimg"),0);
				rs.execute("select * from DocSignature  where hrmresid=" + log.getOperatorid() + "order by markid");
				String userimg = "";
				if (showimg == 1 && rs.next()) {
					// 获取签章图片并显示








					userimg = Util.null2String(rs.getString("markPath"));
				}
				if(!userimg.equals("")){
					wrl.setOperatorSign(userimg);
				}
			}
            
            //取得相关流程
            weaver.workflow.workflow.WorkflowRequestComInfo workflowRequestComInfo = new weaver.workflow.workflow.WorkflowRequestComInfo();
            String tempshowvalue = "";
            String log_signworkflowids = log.getSignworkflowids();
            ArrayList tempwflists=Util.TokenizerString(log_signworkflowids,",");
            for(int k=0;k<tempwflists.size();k++){
            	String signwfid = (String)tempwflists.get(k);
            	
				tempshowvalue += "<span style='text-decoration:underline;color:blue;";
				if (k > 0) tempshowvalue += "padding-left:54px;";
				tempshowvalue += "' onclick='toRequest(" + signwfid + ");'>" + workflowRequestComInfo.getRequestName(signwfid) + "</span><br/><br/>";
            }
            wrl.setSignWorkFlowHtmls(tempshowvalue);
            
			// 取得相关文档
			tempshowvalue = "";
			String log_signdocids = log.getSigndocids();
			if (log_signdocids!= null && !log_signdocids.equals("")) {
				rs.executeSql("select id,docsubject,accessorycount,SecCategory from docdetail where id in("	+ log_signdocids + ") order by id asc");
				int curIndex = 0;
				while (rs.next()) {
					curIndex++;
					String tempshowid = Util.null2String(rs.getString(1));
					String tempshowname = Util.toScreen(rs.getString(2),user.getLanguage());
					
					tempshowvalue += "<span style='text-decoration:underline;color:blue;";
					if (curIndex > 1) tempshowvalue += "padding-left:54px;";
					tempshowvalue += "' onclick='toDocument(" + tempshowid + ");'>" + tempshowname + "</span><br/><br/>";
				}
			}
            wrl.setSignDocHtmls(tempshowvalue);

			wrls.add(wrl);
		}

		WorkflowRequestLog[] result = new WorkflowRequestLog[wrls.size()];
		wrls.toArray(result);
		return result;
	}
	
	private static String splitAndFilterString(String input) {
		if (input == null) {
			return "";
		} else {
			return WorkflowServiceUtil.splitAndFilterString(input, input.length());
		}
	}
	
	public String getLeaveDays(String fromDate, String fromTime, String toDate, String toTime, String resourceId) {
		return getLeaveDays(fromDate, fromTime, toDate, toTime, resourceId, true);
	}
	
	public String getLeaveDays(String fromDate, String fromTime, String toDate, String toTime, String resourceId, boolean worktime) {
		HrmLeaveDay bean = new HrmLeaveDay();
		bean.setFromDate(fromDate);
		bean.setFromTime(fromTime);
		bean.setToDate(toDate);
		bean.setToTime(toTime);
		bean.setResourceId(resourceId);
		bean.setWorktime(worktime);
		return new HrmScheduleManager().getLeaveDays(bean);
	}
	
	/**
	 * @author liu zheng yang
	 * @param workflowid
	 * @param nodeid
	 * @param requestid
	 * @param isremark
	 * @param user
	 * @param nodetype
	 * @return
	 */
	private Map getCustomeButtonMenu(String workflowid ,int nodeid,String requestid,int isremark,User user,String nodetype){
		Map map=new HashMap();
		RecordSet recordSet=new RecordSet();
		String submitname = "" ; // 提交按钮的名称 : 创建, 审批, 实现
		String forwardName = "";//转发
		String takingopinionsName = ""; //征求意见
		String HandleForwardName = ""; //转办
		String forhandbackName = ""; //转办需反馈
		String forhandnobackName = ""; //转办不需反馈
		String givingopinionsName ="";  //回复
		String givingOpinionsnobackName = ""; // 回复不反馈








		String givingOpinionsbackName = ""; // 回复需反馈
		int handleforwardid = -1;     //转办状态








		int takisremark = -1;     //意见征询状态









		String saveName = "";//保存
		String rejectName = "";//退回








		String forsubName = "";//转发提交
		String ccsubName = "";//抄送提交








		String newWFName = "";//新建流程按钮
		String newSMSName = "";//新建短信按钮
		String haswfrm = "";//是否使用新建流程按钮
		String hassmsrm = "";//是否使用新建短信按钮
		int t_workflowid = 0;//新建流程的ID
		String subnobackName = "";//提交不需反馈
		String subbackName = "";//提交需反馈
		String hasnoback = "";//使用提交不需反馈按钮
		String hasback = "";//使用提交需反馈按钮
		String forsubnobackName = "";//转发批注不需反馈
		String forsubbackName = "";//转发批注需反馈
		String hasfornoback = "";//使用转发批注不需反馈按钮
		String hasforback = "";//使用转发批注需反馈按钮
		String ccsubnobackName = "";//抄送批注不需反馈
		String ccsubbackName = "";//抄送批注需反馈
		String hasccnoback = "";//使用抄送批注不需反馈按钮
		String hasccback = "";//使用抄送批注需反馈按钮
		String newOverTimeName=""; //超时设置按钮
		String hasovertime="";    //是否使用超时设置按钮
		String hasforhandback = "";  //是否转办反馈
		String hasforhandnoback = ""; //是否转办不需反馈
		String hastakingOpinionsback = ""; //是否回复反馈
		String hastakingOpinionsnoback = "";//是否回复不需反馈
		String sqlselectName = "select * from workflow_nodecustomrcmenu where wfid="+workflowid+" and nodeid="+nodeid;

		if(isremark != 0){
			recordSet.executeSql("select * from workflow_currentoperator c where c.requestid="+requestid+" and c.userid="+user.getUID()+" and c.usertype="+user.getType()+" and c.isremark='"+isremark+"' ");
			String tmpnodeid="";
			if(recordSet.next()){
				tmpnodeid = Util.null2String(recordSet.getString("nodeid"));
				handleforwardid = Util.getIntValue(recordSet.getString("handleforwardid"),-1) ;     //转办状态








				takisremark = Util.getIntValue(recordSet.getString("takisremark"),0) ;     //意见征询状态









			}
			sqlselectName = "select * from workflow_nodecustomrcmenu where wfid="+workflowid+" and nodeid="+tmpnodeid;
		}
		recordSet.executeSql(sqlselectName);

		if(recordSet.next()){
			if(user.getLanguage() == 7){
				submitname = Util.null2String(recordSet.getString("submitname7"));
				forwardName = Util.null2String(recordSet.getString("forwardName7"));
				takingopinionsName = Util.null2String(recordSet.getString("takingOpName7"));
				HandleForwardName = Util.null2String(recordSet.getString("forhandName7"));
				forhandnobackName = Util.null2String(recordSet.getString("forhandnobackName7"));
				forhandbackName = Util.null2String(recordSet.getString("forhandbackName7"));
				givingopinionsName = Util.null2String(recordSet.getString("takingOpinionsName7"));
				givingOpinionsnobackName = Util.null2String(recordSet.getString("takingOpinionsnobackName7"));
				givingOpinionsbackName = Util.null2String(recordSet.getString("takingOpinionsbackName7"));

				saveName = Util.null2String(recordSet.getString("saveName7"));
				rejectName = Util.null2String(recordSet.getString("rejectName7"));
				forsubName = Util.null2String(recordSet.getString("forsubName7"));
				ccsubName = Util.null2String(recordSet.getString("ccsubName7"));
				newWFName = Util.null2String(recordSet.getString("newWFName7"));
				newSMSName = Util.null2String(recordSet.getString("newSMSName7"));
				subnobackName = Util.null2String(recordSet.getString("subnobackName7"));
				subbackName = Util.null2String(recordSet.getString("subbackName7"));
				forsubnobackName = Util.null2String(recordSet.getString("forsubnobackName7"));
				forsubbackName = Util.null2String(recordSet.getString("forsubbackName7"));
				ccsubnobackName = Util.null2String(recordSet.getString("ccsubnobackName7"));
				ccsubbackName = Util.null2String(recordSet.getString("ccsubbackName7"));
		        newOverTimeName = Util.null2String(recordSet.getString("newOverTimeName7"));
			}
			else if(user.getLanguage() == 9){
				submitname = Util.null2String(recordSet.getString("submitname9"));
				forwardName = Util.null2String(recordSet.getString("forwardName9"));
				takingopinionsName = Util.null2String(recordSet.getString("takingOpName9"));
				HandleForwardName = Util.null2String(recordSet.getString("forhandName9"));
				forhandnobackName = Util.null2String(recordSet.getString("forhandnobackName9"));
				forhandbackName = Util.null2String(recordSet.getString("forhandbackName9"));
				givingopinionsName = Util.null2String(recordSet.getString("takingOpinionsName9"));
				givingOpinionsnobackName = Util.null2String(recordSet.getString("takingOpinionsnobackName9"));
				givingOpinionsbackName = Util.null2String(recordSet.getString("takingOpinionsbackName9"));

				saveName = Util.null2String(recordSet.getString("saveName9"));
				rejectName = Util.null2String(recordSet.getString("rejectName9"));
				forsubName = Util.null2String(recordSet.getString("forsubName9"));
				ccsubName = Util.null2String(recordSet.getString("ccsubName9"));
				newWFName = Util.null2String(recordSet.getString("newWFName9"));
				newSMSName = Util.null2String(recordSet.getString("newSMSName9"));
				subnobackName = Util.null2String(recordSet.getString("subnobackName9"));
				subbackName = Util.null2String(recordSet.getString("subbackName9"));
				forsubnobackName = Util.null2String(recordSet.getString("forsubnobackName9"));
				forsubbackName = Util.null2String(recordSet.getString("forsubbackName9"));
				ccsubnobackName = Util.null2String(recordSet.getString("ccsubnobackName9"));
				ccsubbackName = Util.null2String(recordSet.getString("ccsubbackName9"));
		        newOverTimeName = Util.null2String(recordSet.getString("newOverTimeName9"));
			}
			else{
				submitname = Util.null2String(recordSet.getString("submitname8"));
				forwardName = Util.null2String(recordSet.getString("forwardName8"));
				takingopinionsName = Util.null2String(recordSet.getString("takingOpName8"));
				HandleForwardName = Util.null2String(recordSet.getString("forhandName8"));
				forhandnobackName = Util.null2String(recordSet.getString("forhandnobackName8"));
				forhandbackName = Util.null2String(recordSet.getString("forhandbackName8"));
				givingopinionsName = Util.null2String(recordSet.getString("takingOpinionsName8"));
				givingOpinionsnobackName = Util.null2String(recordSet.getString("takingOpinionsnobackName8"));
				givingOpinionsbackName = Util.null2String(recordSet.getString("takingOpinionsbackName8"));

				saveName = Util.null2String(recordSet.getString("saveName8"));
				rejectName = Util.null2String(recordSet.getString("rejectName8"));
				forsubName = Util.null2String(recordSet.getString("forsubName8"));
				ccsubName = Util.null2String(recordSet.getString("ccsubName8"));
				newWFName = Util.null2String(recordSet.getString("newWFName8"));
				newSMSName = Util.null2String(recordSet.getString("newSMSName8"));
				subnobackName = Util.null2String(recordSet.getString("subnobackName8"));
				subbackName = Util.null2String(recordSet.getString("subbackName8"));
				forsubnobackName = Util.null2String(recordSet.getString("forsubnobackName8"));
				forsubbackName = Util.null2String(recordSet.getString("forsubbackName8"));
				ccsubnobackName = Util.null2String(recordSet.getString("ccsubnobackName8"));
				ccsubbackName = Util.null2String(recordSet.getString("ccsubbackName8"));
		        newOverTimeName = Util.null2String(recordSet.getString("newOverTimeName8"));
			}
			haswfrm = Util.null2String(recordSet.getString("haswfrm"));
			hassmsrm = Util.null2String(recordSet.getString("hassmsrm"));
			hasnoback = Util.null2String(recordSet.getString("hasnoback"));
			hasback = Util.null2String(recordSet.getString("hasback"));
			hasfornoback = Util.null2String(recordSet.getString("hasfornoback"));
			hasforback = Util.null2String(recordSet.getString("hasforback"));
			hasccnoback = Util.null2String(recordSet.getString("hasccnoback"));
			hasccback = Util.null2String(recordSet.getString("hasccback"));
			t_workflowid = Util.getIntValue(recordSet.getString("workflowid"), 0);
		    hasovertime = Util.null2String(recordSet.getString("hasovertime"));
			hasforhandback = Util.null2String(recordSet.getString("hasforhandback"));
			hasforhandnoback = Util.null2String(recordSet.getString("hasforhandnoback"));
			hastakingOpinionsback = Util.null2String(recordSet.getString("hastakingOpinionsback"));
			hastakingOpinionsnoback = Util.null2String(recordSet.getString("hastakingOpinionsnoback"));

		}
        if("".equals(HandleForwardName)){
        	HandleForwardName = "转办";
        }
        
        if("".equals(forhandbackName)){
        	forhandbackName = "转办（需反馈）";
        }
        
        
        if("".equals(forhandnobackName)){
        	forhandnobackName = "转办（不需反馈）";
        }
        
        if("".equals(givingopinionsName)){
        	givingopinionsName = "回复";
        }
        
        if("".equals(givingOpinionsnobackName)){
        	givingOpinionsnobackName = "回复（不需反馈）";
        }
        
        
        if("".equals(givingOpinionsbackName)){
        	givingOpinionsbackName = "回复（需反馈）";
        }
        
        
        if(handleforwardid > 0){
        	submitname = submitname; // 转办批注
        	subnobackName = subnobackName;
        	subbackName = subbackName;
        }
        if(isremark == 1 && takisremark ==2){  //征询意见回复
        	submitname = givingopinionsName;
        	subnobackName = givingOpinionsnobackName;
        	subbackName = givingOpinionsbackName;
        }


		if(isremark == 1 && takisremark !=2){
			submitname = forsubName;
			subnobackName = forsubnobackName;
			subbackName = forsubbackName;
		}
		if(isremark == 9||isremark == 7){
			submitname = ccsubName;
			subnobackName = ccsubnobackName;
			subbackName =  ccsubbackName;
		}
		if("".equals(submitname)){
			if(nodetype.equals("0") || isremark == 1 || isremark == 9||isremark == 7){
				submitname = SystemEnv.getHtmlLabelName(615,user.getLanguage());      // 创建节点或者转发, 为提交








			}else if(nodetype.equals("1")){
				submitname = SystemEnv.getHtmlLabelName(142,user.getLanguage());  // 审批
			}else if(nodetype.equals("2")){
				submitname = SystemEnv.getHtmlLabelName(725,user.getLanguage());  // 实现
			}
		}
		if("".equals(subbackName)){
				if(nodetype.equals("0") || isremark == 1 || isremark == 9||isremark == 7)	{
					if((nodetype.equals("0") && ("1".equals(hasnoback)||"1".equals(hasback))) || (isremark==1 && ("1".equals(hasfornoback)||"1".equals(hasforback))) || (isremark==9 && ("1".equals(hasccnoback)||"1".equals(hasccback)))){
						subbackName = SystemEnv.getHtmlLabelName(615,user.getLanguage())+"（"+SystemEnv.getHtmlLabelName(21761,user.getLanguage())+"）";      // 创建节点或者转发, 为提交








					}else{
						subbackName = SystemEnv.getHtmlLabelName(615,user.getLanguage());
					}
				}else if(nodetype.equals("1")){
					if("1".equals(hasnoback)||"1".equals(hasback)){
						subbackName = SystemEnv.getHtmlLabelName(142,user.getLanguage())+"（"+SystemEnv.getHtmlLabelName(21761,user.getLanguage())+"）";  // 审批
					}else{
						subbackName = SystemEnv.getHtmlLabelName(142,user.getLanguage());
					}
				}else if(nodetype.equals("2")){
					if("1".equals(hasnoback)||"1".equals(hasback)){
						subbackName = SystemEnv.getHtmlLabelName(725,user.getLanguage())+"（"+SystemEnv.getHtmlLabelName(21761,user.getLanguage())+"）";  // 实现
					}else{
						subbackName = SystemEnv.getHtmlLabelName(725,user.getLanguage());
					}
				}
		}
		if("".equals(subnobackName)){
			if(nodetype.equals("0") || isremark == 1 || isremark == 9 ||isremark == 7)	{
				subnobackName = SystemEnv.getHtmlLabelName(615,user.getLanguage())+"（"+SystemEnv.getHtmlLabelName(21762,user.getLanguage())+"）";      // 创建节点或者转发, 为提交








			}else if(nodetype.equals("1")){
				subnobackName = SystemEnv.getHtmlLabelName(142,user.getLanguage())+"（"+SystemEnv.getHtmlLabelName(21762,user.getLanguage())+"）";  // 审批
			}else if(nodetype.equals("2")){
				subnobackName = SystemEnv.getHtmlLabelName(725,user.getLanguage())+"（"+SystemEnv.getHtmlLabelName(21762,user.getLanguage())+"）";  // 实现
			}
		}
		if("".equals(forwardName)){
			forwardName = SystemEnv.getHtmlLabelName(6011,user.getLanguage());
		}

		if("".equals(takingopinionsName)){
	takingopinionsName = "意见征询";
}

if("".equals(HandleForwardName)){
	HandleForwardName = "转办";
}

		if("".equals(saveName)){
			saveName = SystemEnv.getHtmlLabelName(86,user.getLanguage());
		}
		if("".equals(rejectName)){
			rejectName = SystemEnv.getHtmlLabelName(236,user.getLanguage());
		}
		if("".equals(submitname)){
			if(!"".equals(subbackName)) submitname = subbackName;
			else if(!"".equals(subnobackName)) submitname = subnobackName;
		}
		map.put("submitName", submitname);
		map.put("rejectName", rejectName);
		map.put("forwardName",forwardName);
		map.put("HandleForwardName",HandleForwardName);
		map.put("forhandbackName",forhandbackName);
		map.put("forhandnobackName",forhandnobackName);
		map.put("takingopinionsName",takingopinionsName);
		map.put("givingopinionsName",givingopinionsName);
		map.put("givingOpinionsbackName",givingOpinionsbackName);
		map.put("givingOpinionsnobackName",givingOpinionsnobackName);
		return map;
	}
	
	public String[] getWorkflowNewFlag(String[] requestids, String resourceid) {
		if(requestids==null || requestids.length <= 0) return new String[]{};
		String[] result = new String[requestids.length];
		try {
			RecordSet rs = new RecordSet();
			
			String requestidstr = "";
			
			for(int i=0;i<requestids.length;i++){
			    if(requestids[i]!= null && !requestids[i].equals(""))
			        requestidstr+=","+requestids[i];
			}
			requestidstr=requestidstr.startsWith(",")?requestidstr.substring(1):requestidstr;
			
			boolean[] isprocessed=new boolean[requestids.length];
			for(int i=0;i<isprocessed.length;i++) isprocessed[i] = false;
			
		    rs.executeSql("select requestid,isprocessed from workflow_currentoperator where ((isremark='0' and (isprocessed='2' or isprocessed='3'))  or isremark='5') and requestid in (" + requestidstr + ") ");
		    while(rs.next()){
		    	String tmprequestid = rs.getString("requestid");
		        for(int i=0;i<requestids.length;i++){
		        	if(requestids[i].equals(tmprequestid))
		        		isprocessed[i] = true;
		        }
		    }
		    
		    String[] viewtype=new String[requestids.length];
		    for(int i=0;i<viewtype.length;i++) viewtype[i] = "";
		    
		    rs.executeSql("select distinct t1.requestid,t2.viewtype from workflow_requestbase t1,workflow_currentoperator t2 where t1.requestid = t2.requestid and t2.userid = " + resourceid + " and t2.viewtype = 0 and t1.requestid in (" + requestidstr + ") ");
		    while(rs.next()){
		    	String tmprequestid = rs.getString("requestid");
		    	String tmpviewtype = rs.getString("viewtype");
		        for(int i=0;i<requestids.length;i++){
		        	if(requestids[i].equals(tmprequestid)){
		        		viewtype[i] = tmpviewtype;
		        		break;
		        	}
		        }
		    }

		    for(int i=0;i<result.length;i++) result[i] = "0";
		    for(int i=0;i<requestids.length;i++){
			    if (viewtype[i].equals("0")) { 
			        if(!isprocessed[i]){
			        	result[i] = "1";
			        }
			    }
		    }
	
		} catch (Exception e) {
			e.printStackTrace();
			writeLog(e);
		}
		return result;
	}

	/**
	 * 按照requestid列表和用户id列表取得是否新到流程
	 * @param requestids
	 * @param resourceids
	 * @return
	 */
    public String[] getWorkflowNewFlagByList(String[] requestids, String[] resourceids) {
        if(requestids==null || requestids.length <= 0) return new String[]{};
        String[] result = new String[requestids.length];
        try {
            RecordSet rs = new RecordSet();
            
            String requestidstr = "";
            
            String resourceidstr = "";
            
            for(int i=0;i<requestids.length;i++){
                if(requestids[i]!= null && !requestids[i].equals(""))
                    requestidstr+=","+requestids[i];
            }
            requestidstr=requestidstr.startsWith(",")?requestidstr.substring(1):requestidstr;
            
            for(int i=0;i<resourceids.length;i++){
                if(resourceids[i]!= null && !resourceids[i].equals(""))
                    resourceidstr+=","+resourceids[i];
            }
            resourceidstr=resourceidstr.startsWith(",")?resourceidstr.substring(1):resourceidstr;
            
            boolean[] isprocessed=new boolean[requestids.length];
            for(int i=0;i<isprocessed.length;i++) isprocessed[i] = false;
            
            rs.executeSql("select requestid,isprocessed from workflow_currentoperator where ((isremark='0' and (isprocessed='2' or isprocessed='3'))  or isremark='5') and requestid in (" + requestidstr + ") ");
            while(rs.next()){
                String tmprequestid = rs.getString("requestid");
                for(int i=0;i<requestids.length;i++){
                    if(requestids[i].equals(tmprequestid))
                        isprocessed[i] = true;
                }
            }
            
            String[] viewtype=new String[requestids.length];
            for(int i=0;i<viewtype.length;i++) viewtype[i] = "";
            
            rs.executeSql("select distinct t1.requestid,t2.viewtype,t2.userid from workflow_requestbase t1,workflow_currentoperator t2 where t1.requestid = t2.requestid and t2.userid in (" + resourceidstr + ") and t2.viewtype = 0 and t1.requestid in (" + requestidstr + ") ");
            while(rs.next()){
                String tmprequestid = rs.getString("requestid");
                String tmpviewtype = rs.getString("viewtype");
                String tmpuserid = rs.getString("userid");
                for(int i=0;i<requestids.length;i++){
                    if(requestids[i].equals(tmprequestid) && resourceids[i].equals(tmpuserid)){
                        viewtype[i] = tmpviewtype;
                        break;
                    }
                }
            }

            for(int i=0;i<result.length;i++) result[i] = "0";
            for(int i=0;i<requestids.length;i++){
                if (viewtype[i].equals("0")) { 
                    if(!isprocessed[i]){
                        result[i] = "1";
                    }
                }
            }
    
        } catch (Exception e) {
            e.printStackTrace();
            writeLog(e);
        }
        return result;
    }
	
	public void writeWorkflowReadFlag(String requestid, String userid) {
	    writeWorkflowReadFlag(requestid, userid, -1);
    }
	
	public void writeWorkflowReadFlag(String requestid, String userid, int isremark) {
        try {
            RecordSet rs = new RecordSet();
            
            String dbtype = rs.getDBType();
            User user = this.getUser(Integer.parseInt(userid));
            
            int usertype = 0;
            if(user.getLogintype().equals("1")) {
                usertype = 0;
            } else if(user.getLogintype().equals("2")) {
                usertype = 1;
            }
            
            String sql = "";
            if ("oracle".equals(dbtype)) {
                sql = "update workflow_currentoperator set viewtype=-2,operatedate=( case isremark when '2' then operatedate else to_char(sysdate,'yyyy-mm-dd') end  ) ,operatetime=( case isremark when '2' then operatetime else to_char(sysdate,'hh24:mi:ss') end  ) where requestid = " + requestid + "  and userid ="+userid+" and usertype = " + usertype + " and viewtype<>-2 ";
            } else if ("db2".equals(dbtype)) {
                sql = "update workflow_currentoperator set viewtype=-2,operatedate=( case isremark when '2' then operatedate else to_char(current date,'yyyy-mm-dd') end ),operatetime=( case isremark when '2' then operatetime else to_char(current time,'hh24:mi:ss') end ) where requestid = " + requestid + "  and userid ="+userid+" and usertype = " + usertype + " and viewtype<>-2";
            } else {
                sql = "update workflow_currentoperator set viewtype=-2,operatedate=( case isremark when '2' then operatedate else convert(char(10),getdate(),20) end ),operatetime=( case isremark when '2' then operatetime else convert(char(8),getdate(),108) end ) where requestid = " + requestid + "  and userid ="+userid+" and usertype = " + usertype + " and viewtype<>-2";
            }
            
            rs.executeSql(sql);
            
            if (isremark == 8) {
                //推送处理start
                WFPathUtil wfutil = new WFPathUtil();
                new Thread(new RequestPreProcessing(0, -1, -1, Util.getIntValue(requestid), "", "", -1, 0, false, "", user, true)).start();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            writeLog(e);
        }
    }
	
	/**
	 * 取得右键菜单
	 * @param workflowid
	 * @param nodeid
	 * @param requestid
	 * @param isremark
	 * @param user
	 * @param nodetype
	 * @param isrequest
	 * @return
	 * @throws Exception
	 */
	public Map getRightMenu(String workflowid ,int nodeid,String requestid,int isremark,User user,String nodetype, boolean isrequest,boolean isPreLoad) throws Exception {
		Map result = new HashMap();
		boolean getsuccess = true;
		try {
			String submitName = "";
			String subnobackName = "";
			String subbackName = "";
			String forwardName = "";
			String takingopinionsName = ""; //征求意见
			String HandleForwardName = ""; //转办
			String forhandbackName = ""; //转办需反馈
			String forhandnobackName = ""; //转办不需反馈
			String givingopinionsName ="";  //回复
			String givingOpinionsnobackName = ""; // 回复不反馈








			String givingOpinionsbackName = ""; // 回复需反馈


			String rejectName = "";
			String submitDirectName = ""; // 提交至退回节点


			//返回格式:0,提交|0,提交不需反馈|0,提交需反馈|0,转发|0,退回|0,保存
			//new weaver.general.BaseBean().writeLog("-4580-user.getLanguage()->>>"+user.getLanguage());
			String menu = requestService.getRightMenu(Util.getIntValue(requestid, 0), user.getUID(), Util.getIntValue(user.getLogintype(),1), user.getLanguage(), Util.getIntValue(workflowid, 0), isrequest,isPreLoad);
			if(menu!=null&&!"".equals(menu)) {
				List tmpmenustrs = Util.TokenizerString(menu, "|");
				for(int i=0;tmpmenustrs!=null&&i<tmpmenustrs.size();i++){
					String tmpmenustr = (String) tmpmenustrs.get(i);
					if(tmpmenustr!=null||!"".equals(tmpmenustr)){
						List tmpmenus = Util.TokenizerString(tmpmenustr, ",");
						if(tmpmenus!=null&&tmpmenus.size()>0){
							String tmpmenu1 = "";
							String tmpmenu2 = "";
							if(tmpmenus.size()>=1) tmpmenu1 =(String) tmpmenus.get(0);
							if(tmpmenus.size()>=2) tmpmenu2 =(String) tmpmenus.get(1);
							if(tmpmenu1!=null&&"0".equals(tmpmenu1)) {
								if(i==0) {//提交
									submitName="";
								} else if(i==1) {//提交不需反馈
									subnobackName="";
								} else if(i==2) {//提交需反馈
									subbackName="";
								} else if(i==3) {//转发
									forwardName="";
								} else if(i==4) {//转办
									HandleForwardName="";
								} else if(i==5) {//转办需反馈
									forhandbackName="";
								} else if(i==6) {//转办不需反馈
									forhandnobackName="";
								} else if(i==7) {//意见征询
									takingopinionsName="";
								}else if(i==8) {//回复
									givingopinionsName="";
								}else if(i==9) {//回复反馈
									givingOpinionsbackName="";
								}else if(i==10) {//回复不反馈








									givingOpinionsnobackName="";
								} else if(i==11) {//退回








									rejectName="";
								} else if(i==12) {//保存
								} else if(i == 13) { // 提交至退回节点


									submitDirectName = "";
								}
							} else if(tmpmenu1!=null&&"1".equals(tmpmenu1)) {
								if(i==0) {//提交
									submitName=tmpmenu2;
								} else if(i==1) {//提交不需反馈
									subnobackName=tmpmenu2;
								} else if(i==2) {//提交需反馈
									subbackName=tmpmenu2;
								} else if(i==3) {//转发
									forwardName=tmpmenu2;
								}else if(i==4) {//转办
									HandleForwardName=tmpmenu2;
								}else if(i==5) {//转办需反馈
									forhandbackName=tmpmenu2;
								} else if(i==6) {//转办不需反馈
									forhandnobackName=tmpmenu2;
								} else if(i==7) {//意见征询
									takingopinionsName=tmpmenu2;
								}else if(i==8) {//回复
									givingopinionsName=tmpmenu2;
								}else if(i==9) {//回复反馈
									givingOpinionsbackName=tmpmenu2;
								}else if(i==10) {//回复不反馈








									givingOpinionsnobackName=tmpmenu2;
								} else if(i==11) {//退回








									rejectName=tmpmenu2;
								} else if(i==12) {//保存
								} else if(i == 13) { // 提交至退回节点


									submitDirectName = tmpmenu2;
								}
							} else {
								getsuccess = false;
								writeLog("error:requestService.getRightMenu("+requestid+","+user.getUID()+","+Util.getIntValue(user.getLogintype(),0)+","+user.getLanguage()+"):"+menu);
							    //System.out.println("error:requestService.getRightMenu("+requestid+","+user.getUID()+","+Util.getIntValue(user.getLogintype(),0)+","+user.getLanguage()+"):"+menu);
							}
						}
					}
				}			
			}

			if(getsuccess) {
				result.put("submitName", submitName);
				result.put("subnobackName", subnobackName);
				result.put("subbackName", subbackName);
				result.put("rejectName", rejectName);
				result.put("forwardName",forwardName);
				result.put("HandleForwardName",HandleForwardName);
				result.put("forhandbackName",forhandbackName);
				result.put("forhandnobackName",forhandnobackName);
				result.put("takingopinionsName",takingopinionsName);
				result.put("givingopinionsName",givingopinionsName);
				result.put("givingOpinionsnobackName",givingOpinionsnobackName);
				result.put("givingOpinionsbackName",givingOpinionsbackName);
				result.put("submitDirectName", submitDirectName);
			}
		} catch(Exception e) {
			writeLog(e);
			e.printStackTrace();
		}
		if(result.size()==0){
			result = getCustomeButtonMenu(workflowid, nodeid, requestid, isremark, user, nodetype);
		}
		return result;
	}

	public String getRightMenu(String reqid, User user) throws Exception {
        WFLinkInfo wfLinkInfoObj = new WFLinkInfo();
        String workflowid = "";

        int nodeid = wfLinkInfoObj.getCurrentNodeid(Util.getIntValue(reqid), user.getUID(), Util.getIntValue(user.getLogintype(), 1)); // 节点id
        String nodetype = wfLinkInfoObj.getNodeType(nodeid); // 节点类型 0:创建
        // 1:审批 2:实现
        // 3:归档
        String currentNodeId = String.valueOf(nodeid);
        // 查询请求的相关工作流基本信息
        String sql = "select wb.*, wn.nodename from workflow_requestbase wb inner JOIN workflow_nodebase wn on wn.id=wb.currentnodeid where wb.requestid=" + reqid;
        RecordSet rs = new RecordSet();
        rs.executeSql(sql);
        if (rs.next()) {
            workflowid = Util.getIntValue(rs.getString("workflowid"), 0) + "";
            currentNodeId = rs.getString("currentnodeid");
            if (nodeid < 1)
                nodeid = Util.getIntValue(rs.getString("currentnodeid"), 0);
            if (nodetype.equals(""))
                nodetype = Util.null2String(rs.getString("currentnodetype"));
        }

        RecordSet rs2 = new RecordSet();
        char flag = Util.getSeparator();

        int wfcurrrid = -1;
        boolean canView = false;// 是否可以查看
        rs.executeSql("select id, requestid,isremark,nodeid,groupdetailid from workflow_currentoperator where userid=" + user.getUID() + " and requestid=" + reqid + " order by isremark,id");
        while (rs.next()) {
            canView = true;
            WFCoadjutantManager wfcm = new WFCoadjutantManager();
            String isremark = Util.null2String(rs.getString("isremark"));
            int tmpnodeid = Util.getIntValue(rs.getString("nodeid"));
            if (isremark.equals("7"))
                wfcm.getCoadjutantRights(Util.getIntValue(rs.getString("groupdetailid")));
            if (isremark.equals("1") || isremark.equals("5") || (isremark.equals("7") && wfcm.getIsmodify().equals("1")) || isremark.equals("9") || (isremark.equals("0") && !nodetype.equals("3"))) {
                canView = true;
                nodeid = tmpnodeid;
                nodetype = wfLinkInfoObj.getNodeType(nodeid);
                wfcurrrid = rs.getInt("id");
                break;
            }
            if (isremark.equals("8")) {
                canView = true;
                break;
            }
        }

        int isremark = -1;
        rs.executeSql("select isremark from workflow_currentoperator where (isremark<8 or isremark>8) and requestid=" + reqid + " and userid=" + user.getUID() + " order by isremark");
        while (rs.next()) {
            int tempisremark = Util.getIntValue(rs.getString("isremark"), 0);
            if (tempisremark == 0 || tempisremark == 1 || tempisremark == 5 || tempisremark == 9 || tempisremark == 7) { // 当前操作者或被转发者








                isremark = tempisremark;
                break;
            }
        }

        if (!canView) {
            return null;
        }

        // 设置流程页面按钮
        // 应64243要求，对 相关请求 的转发按钮不作控制。 "fromrequestid > 0" -->> false
        Map map = getRightMenu(workflowid, nodeid, reqid, isremark, user, nodetype, false, false);
        WorkflowRequestInfo workflowRequestInfo = new WorkflowRequestInfo();
        workflowRequestInfo.setSubmitButtonName((String) map.get("submitName"));
        workflowRequestInfo.setSubnobackButtonName((String) map.get("subnobackName"));
        workflowRequestInfo.setSubbackButtonName((String) map.get("subbackName"));
        workflowRequestInfo.setRejectButtonName((String) map.get("rejectName"));
        workflowRequestInfo.setForwardButtonName((String) map.get("forwardName"));
        workflowRequestInfo.setTakingOpsButtonName((String) map.get("takingopinionsName"));
        workflowRequestInfo.setHandleForwardButtonName((String) map.get("HandleForwardName"));
        workflowRequestInfo.setForhandbackButtonName((String) map.get("forhandbackName"));
        workflowRequestInfo.setForhandnobackButtonName((String) map.get("forhandnobackName"));
        workflowRequestInfo.setGivingopinionsName((String) map.get("givingopinionsName"));
        workflowRequestInfo.setGivingOpinionsbackName((String) map.get("givingOpinionsbackName"));
        workflowRequestInfo.setGivingOpinionsnobackName((String) map.get("givingOpinionsnobackName"));

        // 自由流程相关信息
        weaver.workflow.request.WorkflowIsFreeStartNode stnode = new weaver.workflow.request.WorkflowIsFreeStartNode();
        String nodeidss = stnode.getIsFreeStartNode("" + currentNodeId);
        String freedis = stnode.getNodeid(nodeidss);
        String isornotFree = stnode.isornotFree("" + currentNodeId);
        // 判断是不是创建保存







        String isreject = "";
        boolean iscreate = stnode.IScreateNode("" + reqid);
        if (!isornotFree.equals("") && !freedis.equals("") && !iscreate) {
            isreject = "1";
        }
        String zjclNodeid = stnode.getIsFreeStart01Node("" + currentNodeId);

        int operatorType = -1;

        Map<String, List<Object>> menumap = new HashMap<String, List<Object>>();

        List<Object> menulist = new ArrayList<Object>();
        menumap.put("operations", menulist);

        Map<String, Object> menuobj = new HashMap<String, Object>();
        String mname = "";
        String mtype = "";
        String mcallback = "";
        String mforwardoperationkey = "";

        if (workflowRequestInfo.getSubmitButtonName() != null && !workflowRequestInfo.getSubmitButtonName().equals("")) {
            menuobj = new HashMap<String, Object>();
            menuobj.put("name", workflowRequestInfo.getSubmitButtonName());
            menuobj.put("type", "0");
            menuobj.put("callback", "doSubmit_4Mobile(1)");
            menulist.add(menuobj);
        }
        if (workflowRequestInfo.getSubnobackButtonName() != null && !workflowRequestInfo.getSubnobackButtonName().equals("")) {
            menuobj = new HashMap<String, Object>();
            menuobj.put("name", workflowRequestInfo.getSubnobackButtonName());
            menuobj.put("type", "2");
            menuobj.put("callback", "doSubmit_4Mobile(2)");
            menulist.add(menuobj);
        }
        if (workflowRequestInfo.getSubbackButtonName() != null && !workflowRequestInfo.getSubbackButtonName().equals("")) {
            menuobj = new HashMap<String, Object>();
            menuobj.put("name", workflowRequestInfo.getSubbackButtonName());
            menuobj.put("type", "3");
            menuobj.put("callback", "doSubmit_4Mobile(3)");
            menulist.add(menuobj);
        }

        if (workflowRequestInfo.getRejectButtonName() != null && !workflowRequestInfo.getRejectButtonName().equals("") && "".equals(isornotFree)) {
            menuobj = new HashMap<String, Object>();
            menuobj.put("name", workflowRequestInfo.getRejectButtonName());
            menuobj.put("type", "1");
            menuobj.put("callback", "doreject()");
            menulist.add(menuobj);

        } else {
            if (isreject.equals("1")) {
                menuobj = new HashMap<String, Object>();
                menuobj.put("name", workflowRequestInfo.getRejectButtonName());
                menuobj.put("type", "1");
                menuobj.put("callback", "dorejectIsfree()");
                menulist.add(menuobj);
            }
        }
        if (workflowRequestInfo.getForwardButtonName() != null && !workflowRequestInfo.getForwardButtonName().equals("")) {
            menuobj = new HashMap<String, Object>();
            menuobj.put("name", workflowRequestInfo.getForwardButtonName());
            menuobj.put("type", "4");
            menuobj.put("callback", "doforward()");
            menuobj.put("forwardoperationkey", "forwardoperation");

            menulist.add(menuobj);

        }
        if (workflowRequestInfo.getTakingOpsButtonName() != null && !workflowRequestInfo.getTakingOpsButtonName().equals("")) {
            menuobj = new HashMap<String, Object>();
            menuobj.put("name", workflowRequestInfo.getTakingOpsButtonName());
            menuobj.put("type", "4");
            menuobj.put("callback", "doforward2()");
            menuobj.put("forwardoperationkey", "forwardoperation2");

            menulist.add(menuobj);

        }
        if (workflowRequestInfo.getHandleForwardButtonName() != null && !workflowRequestInfo.getHandleForwardButtonName().equals("")) {
            menuobj = new HashMap<String, Object>();
            menuobj.put("name", workflowRequestInfo.getHandleForwardButtonName());
            menuobj.put("type", "4");
            menuobj.put("callback", "doforward3()");
            menuobj.put("forwardoperationkey", "forwardoperation3");
            menulist.add(menuobj);
        }
        if (workflowRequestInfo.getForhandbackButtonName() != null && !workflowRequestInfo.getForhandbackButtonName().equals("")) {
            menuobj = new HashMap<String, Object>();
            menuobj.put("name", workflowRequestInfo.getForhandbackButtonName());
            menuobj.put("type", "4");
            menuobj.put("callback", "doforward3()");
            menuobj.put("forwardoperationkey", "forwardoperation4");
            menulist.add(menuobj);
        }
        if (workflowRequestInfo.getForhandnobackButtonName() != null && !workflowRequestInfo.getForhandnobackButtonName().equals("")) {
            menuobj = new HashMap<String, Object>();
            menuobj.put("name", workflowRequestInfo.getForhandnobackButtonName());
            menuobj.put("type", "4");
            menuobj.put("callback", "doforward3()");
            menuobj.put("forwardoperationkey", "forwardoperation5");
            menulist.add(menuobj);
        }
        if (workflowRequestInfo.getGivingopinionsName() != null && !workflowRequestInfo.getGivingopinionsName().equals("")) {
            menuobj = new HashMap<String, Object>();
            menuobj.put("name", workflowRequestInfo.getGivingopinionsName());
            menuobj.put("type", "0");
            menuobj.put("callback", "doSubmit_4Mobile(1)");

            menulist.add(menuobj);
        }
        if (workflowRequestInfo.getGivingOpinionsnobackName() != null && !workflowRequestInfo.getGivingOpinionsnobackName().equals("")) {
            menuobj = new HashMap<String, Object>();

            menuobj.put("name", workflowRequestInfo.getGivingOpinionsnobackName());
            menuobj.put("type", "2");
            menuobj.put("callback", "doSubmit_4Mobile(2)");

            menulist.add(menuobj);
        }
        if (workflowRequestInfo.getGivingOpinionsbackName() != null && !workflowRequestInfo.getGivingOpinionsbackName().equals("")) {
            menuobj = new HashMap<String, Object>();
            menuobj.put("name", workflowRequestInfo.getGivingOpinionsbackName());
            menuobj.put("type", "3");
            menuobj.put("callback", "doSubmit_4Mobile(3)");

            menulist.add(menuobj);
        }

        // operationJson.delete(operationJson.length() - 1,
        // operationJson.length());
        // operationJson.append("]");
        if (workflowRequestInfo.getForwardButtonName() != null && !workflowRequestInfo.getForwardButtonName().equals("")) {
            List<Object> omenulist = new ArrayList<Object>();
            menumap.put("forwardoperation", omenulist);

            Map<String, String> omenuobj = new HashMap<String, String>();
            omenuobj.put("isFromFord", "true");
            omenuobj.put("name", "提交");
            omenuobj.put("type", "6");
            omenuobj.put("callbackFunction1", "forwardresources");
            omenuobj.put("callback", "doforward()");
            omenulist.add(omenuobj);
            // operationJson.append(",\"forwardoperation\" :");
            // operationJson.append("[{\"isFromFord\":\"true\",").append("\"name\":
            // \"").append("提交").append("\",
            // \"type\":\"6\"").append(",\"callbackFunction1\":\"forwardresourceids\"").append(",\"callbackFunction2\":\"forwardresources\"").append(",
            // \"callback\":\"doforward()\"}]");
        }
        if (workflowRequestInfo.getTakingOpsButtonName() != null && !workflowRequestInfo.getTakingOpsButtonName().equals("")) {
            // operationJson.append(",\"forwardoperation2\" :");
            // operationJson.append("[{\"isFromFord\":\"true\",").append("\"name\":
            // \"").append("意见征询").append("\",
            // \"type\":\"6\"").append(",\"callbackFunction1\":\"forwardresourceids2\"").append(",\"callbackFunction2\":\"forwardresources2\"").append(",
            // \"callback\":\"doforward2()\"}]");

            List<Object> omenulist = new ArrayList<Object>();
            menumap.put("forwardoperation2", omenulist);

            Map<String, String> omenuobj = new HashMap<String, String>();
            omenuobj.put("isFromFord", "true");
            omenuobj.put("name", "意见征询");
            omenuobj.put("type", "6");
            omenuobj.put("callbackFunction1", "forwardresources2");
            omenuobj.put("callbackFunction2", "forwardresources2");
            omenuobj.put("callback", "doforward2()");
            omenulist.add(omenuobj);
        }
        if (workflowRequestInfo.getHandleForwardButtonName() != null && !workflowRequestInfo.getHandleForwardButtonName().equals("")) {
            // operationJson.append(",\"forwardoperation3\" :");
            // operationJson.append("[{\"isFromFord\":\"true\",").append("\"name\":
            // \"").append("转办提交").append("\",
            // \"type\":\"6\"").append(",\"callbackFunction1\":\"forwardresourceids3\"").append(",\"callbackFunction2\":\"forwardresources3\"").append(",
            // \"callback\":\"doforward3()\"}]");

            List<Object> omenulist = new ArrayList<Object>();
            menumap.put("forwardoperation3", omenulist);

            Map<String, String> omenuobj = new HashMap<String, String>();
            omenuobj.put("isFromFord", "true");
            omenuobj.put("name", "转办提交");
            omenuobj.put("type", "6");
            omenuobj.put("callbackFunction1", "forwardresources3");
            omenuobj.put("callbackFunction2", "forwardresources3");
            omenuobj.put("callback", "doforward3()");
            omenulist.add(omenuobj);
        }
        if (workflowRequestInfo.getForhandbackButtonName() != null && !workflowRequestInfo.getForhandbackButtonName().equals("")) {
            // operationJson.append(",\"forwardoperation4\" :");
            // operationJson.append("[{\"isFromFord\":\"true\",").append("\"name\":
            // \"").append("转办需反馈提交").append("\",
            // \"type\":\"6\"").append(",\"callbackFunction1\":\"forwardresourceids3\"").append(",\"callbackFunction2\":\"forwardresources3\"").append(",
            // \"callback\":\"doforward3()\"}]");

            List<Object> omenulist = new ArrayList<Object>();
            menumap.put("forwardoperation4", omenulist);

            Map<String, String> omenuobj = new HashMap<String, String>();
            omenuobj.put("isFromFord", "true");
            omenuobj.put("name", "转办需反馈提交");
            omenuobj.put("type", "6");
            omenuobj.put("callbackFunction1", "forwardresources3");
            omenuobj.put("callbackFunction2", "forwardresources3");
            omenuobj.put("callback", "doforward3()");
            omenulist.add(omenuobj);
        }
        if (workflowRequestInfo.getForhandnobackButtonName() != null && !workflowRequestInfo.getForhandnobackButtonName().equals("")) {
            // operationJson.append(",\"forwardoperation5\" :");
            // operationJson.append("[{\"isFromFord\":\"true\",").append("\"name\":
            // \"").append("转办不需反馈提交").append("\",
            // \"type\":\"6\"").append(",\"callbackFunction1\":\"forwardresourceids3\"").append(",\"callbackFunction2\":\"forwardresources3\"").append(",
            // \"callback\":\"doforward3()\"}]");
            List<Object> omenulist = new ArrayList<Object>();
            menumap.put("forwardoperation5", omenulist);

            Map<String, String> omenuobj = new HashMap<String, String>();
            omenuobj.put("isFromFord", "true");
            omenuobj.put("name", "转办需反馈提交");
            omenuobj.put("type", "6");
            omenuobj.put("callbackFunction1", "forwardresources3");
            omenuobj.put("callbackFunction2", "forwardresources3");
            omenuobj.put("callback", "doforward3()");
            omenulist.add(omenuobj);
        }

        // operationJson.append("}");
        JSONObject jsonobject = JSONObject.fromObject(menumap);
        return jsonobject.toString();
    }
	
	private String[][] getWorkflowPhrases(User user) {
		 return getWorkflowPhrases(user,0);
	}

	
	private String[][] getWorkflowPhrases(User user,int userid) {
		String[][] phraseList = null;
		
		try {
			RecordSet rs = new RecordSet();
			List phrases = new ArrayList();
			String phraseSql = "";
			if(user == null){
				phraseSql = " select phraseShort,phraseDesc from sysPhrase where hrmid ="+userid+" order by id ";
			}else{
				phraseSql = " select phraseShort,phraseDesc from sysPhrase where hrmid ="+user.getUID()+" order by id ";
			}
			rs.executeSql(phraseSql);
		    while(rs.next()){
		    	String[] phrase = new String[2];
		    	phrase[0] = splitAndFilterString(rs.getString("phraseShort"));
		    	String phraseStr = splitAndFilterString(rs.getString("phraseDesc"));
		    	if(phraseStr.endsWith("\n\n"))		//替换后产生两个换行\n\n，去掉一个







		    		phraseStr = phraseStr.substring(0, phraseStr.length()-2);
		    	phrase[1] = phraseStr;
		    	phrases.add(phrase);
		    }
		    
		    if (phrases.size() > 0) {
		    	phraseList = new String[phrases.size()][2];
				for(int i=0;i<phrases.size();i++) {
					phraseList[i] = (String[]) phrases.get(i);
				}
		    }		    
		} catch (Exception e) {
			writeLog(e);
			e.printStackTrace();
		}
	    
		return phraseList;
	}
	/**
	 * 获取签字意见附件和文档








	 * @param workflowId
	 * @param requestId
	 * @param userid
	 * @param pagesize
	 * @param endId
	 * @return
	 * @throws Exception
	 */
    public   String  getWorkflowResource(String workflowId, String requestId, int userid, int pagesize, int endId)  {
    	
    	
    	try{
		RequestService requestService = new RequestService();
		boolean isOldWf = false;
		RecordSet rs = new RecordSet();
		rs.executeSql("select nodeid from workflow_currentoperator where requestid = " + requestId);
		while(rs.next()){
			if(rs.getString("nodeid") == null || "".equals(rs.getString("nodeid")) || "-1".equals(rs.getString("nodeid"))){
					isOldWf = true;
			}
		}
		
		String creatorNodeId = "-1";
		rs.executeSql("select nodeid from workflow_flownode where workflowid = "+workflowId+" and nodetype = '0'");
		if (rs.next()) {
			creatorNodeId = rs.getString("nodeid");
		}
		
		//获取当前节点的日志查看范围








		String currentNodeId = "";
		String strViewLogIDs = "";
		List canViewIds = new ArrayList();
		rs.executeSql("select nodeid from workflow_currentoperator where requestid="+requestId+" and userid="+userid+" order by receivedate desc ,receivetime desc");
		if(rs.next()){
			currentNodeId = Util.null2String(rs.getString("nodeid"));
			rs.executeSql("select viewnodeids from workflow_flownode where workflowid=" + workflowId + " and nodeid=" + currentNodeId);
			if(rs.next()){
				strViewLogIDs = Util.null2String(rs.getString("viewnodeids"));
			}

			//为空表示流程日志全部不能查看
			if(!"".equals(strViewLogIDs)){
				//查看全部
				if("-1".equals(strViewLogIDs)){
					rs.executeSql("select nodeid from workflow_flownode where workflowid= " + workflowId+" and exists(select 1 from workflow_nodebase where id=workflow_flownode.nodeid and (requestid is null or requestid="+requestId+"))");
					while(rs.next()){
						String tempNodeId = Util.null2String(rs.getString("nodeid"));
						if(!canViewIds.contains(tempNodeId)){
							canViewIds.add(tempNodeId);
						}
					}
				//查看部分
				}else{
					String tempidstrs[] = Util.TokenizerString2(strViewLogIDs, ",");
					for(int i=0;i<tempidstrs.length;i++){
						if(!canViewIds.contains(tempidstrs[i])){
							canViewIds.add(tempidstrs[i]);
						}
					}
				}
			}
		}
		
		String strViewLogIds = "";
		if(canViewIds.size()>0){
			for(int a=0;a<canViewIds.size();a++){
				strViewLogIds += "," + (String)canViewIds.get(a);
			}
			strViewLogIds = strViewLogIds.substring(1);
		} else{
			strViewLogIds = "-1";
		}
		
		User user = getUser(userid);
		//User user =ThreadLocalUser.getUser();
		RequestRemarkRight remarkRight = new RequestRemarkRight();
		String sqlwhere = remarkRight.getRightCondition(Util.getIntValue(requestId,-1),Util.getIntValue(workflowId,-1), userid);
		Log[] logs = requestService.getRequestLogs(requestId, strViewLogIds, pagesize, endId,sqlwhere);
		
		StringBuffer sb=new StringBuffer("[");
		
		ResourceComInfo  resource=new ResourceComInfo();
		
		String docidstr;
		String[] docids;
		String uploadidstr;
		String[] uploadids;
		
		DocManager  docManager=new DocManager();
		
		String item="";
		
		String docsql="";
		
		String fileExtend="";
		
		String imageFileid="";
		
		for(Log log :logs)
		{ 
			//相关文档
			docidstr=log.getSigndocids();
			
			if(!"".equals(docidstr))
			{
				docids=docidstr.split(",");
				for(String docid:docids)
				{
					if(!docid.equals("null") &&  !docid.equals("") && !docid.equals("NULL"))
					{
					docManager.resetParameter();
					docManager.setId(Integer.valueOf(docid));
					docManager.getDocInfoById();
					item="{\"filetitle\":\""+getTransform(docManager.getDocsubject())+"\",\"fileauthor\":\""+getTransform(resource.getLastname(""+docManager.getOwnerid()))+"\",\"" +
							"filecreatetime\":\""+docManager.getDoccreatedate()+"\",\"fileid\":\""+docid+"\",\"filetype\":\"0\"}";
					sb.append(item).append(",");
					}
				}	
			}
			//相关附件
			uploadidstr=log.getAnnexdocids();
			if(!"".equals(uploadidstr))
			{
				uploadids=uploadidstr.split(",");
				for(String docid:uploadids)
				{
					if(!docid.equals("null") &&  !docid.equals("") && !docid.equals("NULL"))
					{
					rs.executeSql("select  imagefilename,imagefileid  from DocImageFile  where docid="+Integer.valueOf(docid));
					if(rs.next())
					{
						fileExtend=rs.getString("imagefilename");
						imageFileid=rs.getString("imagefileid");
						if (fileExtend.lastIndexOf(".") != -1) {
							fileExtend = fileExtend.substring(fileExtend.lastIndexOf("."));
						} else {
							fileExtend = "";
						}
					}else
					{
						fileExtend="";
					}
					docManager.resetParameter();
					docManager.setId(Integer.valueOf(docid));
					docManager.getDocInfoById();
					item="{\"filetitle\":\""+getTransform(docManager.getDocsubject()+fileExtend)+"\",\"fileauthor\":\""+getTransform(resource.getLastname(""+docManager.getOwnerid()))+"\",\"" +
							"filecreatetime\":\""+docManager.getDoccreatedate()+"\",\"fileid\":\""+imageFileid+"\",\"filetype\":\"1\"}";
					sb.append(item).append(",");
					}
				}	
				
			}
		}
		String rsdata="";
		//如果里面包含元素
		if(sb.length()>2)
		{
			rsdata=sb.substring(0, sb.length()-1)+"]";
		}else
		{
			rsdata="[]";
		}
		 return rsdata;
    	}catch(Exception e)
    	{
    		return "[]";
    	}
    	
    }
	public String getBrowserData(int userId, int browserTypeId,
			String customBrowType, String keyword, int pageNo, int pageSize) {
		
		User user = this.getUser(userId);
		
		BrowserAction brwa = new BrowserAction(user, browserTypeId, pageNo, pageSize);
		brwa.setCustomBrowType(customBrowType);
		brwa.setKeyword(keyword);
		return brwa.getBrowserData();
		
	}
	
	/**
	 * 查询保存的是否显示手机后缀信息
	 */
	public  String getIsShowTerminal(){
		String isshowterminalstr = "1";
		RecordSet rs = null;
		String sql = "select propValue from mobileProperty where name='isShowTerminal'";

		try{
		//查询
		rs = new RecordSet();
		rs.executeSql(sql);
		if(rs.next()){
			isshowterminalstr =  Util.null2String(rs.getString("propValue"),"1");
		}
		}catch(Exception e){
			writeLog(e);
			return isshowterminalstr;
		}
		return isshowterminalstr;
	}

	/**
	 * 用于显示Mobile3.0中E人E本中的手写签批功能，对其进行截取功能。








	 * @param remark
	 * @return
	 */
	private static String getSigntureFileId(String remark) {
		String msigntureid = "";
		String regex = "<img alt='signture' _signtureid='";
		int start = remark.indexOf(regex);
		
		if (start != -1) {
			String tempremark = remark.substring(start + regex.length(), remark.length());
			int end = tempremark.indexOf("'");
			if (end != -1) {
				msigntureid = tempremark.substring(0, end);
			}
		}
		return msigntureid;
	}
	
	
	/**
	 * 用于保存Mobile3.0中E人E本中的手写签批数据。








	 */
	public String saveRequestLog4Signture(int userid, String remark) {
		String nremark = requestService.saveRequestSigntureLog(userid, remark);
		return nremark;
	}
	
	public static String getTransform(String s) {
		  
            String datareturn=Util.StringReplace(s,"&quot;","&quotforjson;");
			datareturn=Util.StringReplace(datareturn,"&gt;","&gtforjson;");
			datareturn=Util.StringReplace(datareturn,"&lt;","&ltforjson;");
			datareturn=Util.StringReplace(datareturn,"\\","&transline;");
            return datareturn;

	}
	
	public static String toHtml(String s) 
    {  if (s == null)
        s = Util.null2String(s);
    	String tempString=s;
    	tempString=Util.StringReplace(tempString,"<br>",""+'\r');  
    	tempString=Util.StringReplace(tempString,"\'","\''");
    	tempString=Util.StringReplace(tempString,"<","&lt;");
    	tempString=Util.StringReplace(tempString,">'","&gt;'");
    	tempString=Util.StringReplace(tempString,"\"","&quot;");
    	tempString=Util.StringReplace(tempString,"\r","\r<br>");
    	try
    	{
    	return tempString;
    	}
    	catch (Exception ee)
    	{
    		return tempString;
    	}
    	
    }

	/**
	 * 是否加载sql赋值的操作
	 * 已办，办结不加载
	 * @param currentNodeId
	 * @param userid
	 * @param requestid
	 * @return
	 */
	public static boolean loadSqlField(String currentNodeId,String userid,String requestid){
		 boolean  returnval = true;
		 try{
			RecordSet rs = new RecordSet();
			String sql = " select distinct t1.createdate,t1.currentnodetype,t2.isremark,t2.iscomplete,t2.islasttimes from workflow_requestbase t1,workflow_currentoperator t2  where t1.requestid=t2.requestid  and t2.requestid ='"+requestid+"' and t2.userid  ='"+userid+"' and t2.nodeid ='"+currentNodeId+"'";
			rs.executeSql(sql);
			if(rs.next()){
				boolean compeleteVal = false;
				boolean alreadyVal = false;
				String isremark = rs.getString("isremark");
				String currentnodetype = rs.getString("currentnodetype");
				String iscomplete = rs.getString("iscomplete");
				String islasttimes = rs.getString("islasttimes");
				if(("2".equals(isremark)||"4".equals(isremark))
						 && "3".equals(currentnodetype)
						 && "1".equals(iscomplete)&&"1".equals(islasttimes)){ //办结
					compeleteVal = true;
				}
				
				if("2".equals(isremark)&&"0".equals(iscomplete)
						&&"1".equals(islasttimes)){
					alreadyVal = true;
				}
				
				if(compeleteVal || alreadyVal){
					returnval = false;
				}
			}
		 }catch(Exception e){
			  e.printStackTrace();
		 }
		 return returnval;
	}
	
	/**
	 * 新表单设计器-明细新增的Html
	 */
	private String getDetailAddStr(WorkflowRequestInfo wri, Elements eleTRs, int groupid, boolean seniorset, User user){
		String wfid = wri.getWorkflowBaseInfo().getWorkflowId();
		String nodeid = wri.getCurrentNodeId();
		HashMap<String,WorkflowRequestTableField> detailFieldMap=WorkflowServiceUtil.getDetailFieldInfo(wfid, nodeid, groupid, user);
		WorkflowRequestTableField fieldInfo = null;
		Elements inputs = eleTRs.select("input[name^=field]");
		for(Element input: inputs){
			String fieldid = input.attr("name").substring(5);
			if(detailFieldMap.containsKey(fieldid)){
				fieldInfo = detailFieldMap.get(fieldid);
				String fieldshowhtml="";
				fieldshowhtml += "<input type=\"hidden\" id=\""+fieldid+"$rowIndex$\" name=\""+fieldid+"$rowIndex$\" value=\"isshow"+groupid+"_$rowIndex$_"+fieldid+"\">";
				fieldshowhtml += "<div id=\"isshow"+groupid+"_$rowIndex$_"+fieldid+"\">";
				fieldshowhtml += fieldInfo.getFieldShowValue();
				fieldshowhtml += "</div>";
				fieldshowhtml += "<div name=\"hiddenEditdiv\" style=\"display:none\">";
				fieldshowhtml += fieldInfo.getFiledHtmlShow();
				fieldshowhtml += "</div>";
				input.after(fieldshowhtml);
			}
			input.remove();
		}
		eleTRs.attr("name", "trView_"+groupid+"_$rowIndex$");
		eleTRs.attr("onclick", "javascript:detailTrClick("+groupid+", \"$rowIndex$\")");
		String checkStr = "<input type=\"checkbox\" name=\"check_node_"+groupid+"\" _rowindex=\"$rowIndex$\" onclick=\"event.stopPropagation();\" />";
		String serialStr = "<span name=\"detailIndexSpan"+groupid+"\"></span>";
		if(seniorset){
    		if(eleTRs.select("input[name=detailSpecialMark][value=21]").size() > 0){
    			Element checkElm = eleTRs.select("input[name=detailSpecialMark][value=21]").first();
    			checkElm.after(checkStr);
    			checkElm.remove();
    		}else{
    			eleTRs.select("div.detailRowHideArea").first().append(checkStr);
    		}
    		if(eleTRs.select("input[name=detailSpecialMark][value=22]").size() > 0){
    			Element serialElm = eleTRs.select("input[name=detailSpecialMark][value=22]").first();
				serialElm.after(serialStr);
				serialElm.remove();
    		}
		}else{
	    	eleTRs.first().children().first().append(checkStr+serialStr);		//TR第一个单元格增加序号
		}
		return eleTRs.toString();
	}
	
	/**
	 * 新表单设计器-合计行的HTML
	 */
	private String getCalculateStr(WorkflowRequestInfo wri, Element eleTr, int groupid, User user){
		try {
			RecordSet rs=new RecordSet();
			String formid=wri.getWorkflowBaseInfo().getFormId();
			rs.executeSql("select colCalStr from workflow_formdetailinfo where formid="+formid);
			String colCalItemStr="";
			while(rs.next()){
				colCalItemStr = Util.null2String(rs.getString("colCalStr"));
			}
			if("".equals(colCalItemStr))	return "";
			
			//当前明细表单是否含合计字段


			boolean hasCalculate=false;
			Element newTR=eleTr.clone();
			Elements fieldinputs = newTR.getElementsByTag("input");
			for(Element input:fieldinputs){
				String fieldid=input.attr("name");
				if(fieldid.startsWith("field")){
					fieldid = fieldid.substring(5);
					if((";"+colCalItemStr+";").indexOf(";detailfield_"+fieldid+";")>-1){
						hasCalculate = true;
						break;
					}
				}
			}
			if(!hasCalculate)	return "";
			
			//计算已存在记录的合计值


			/*HashMap summap=new HashMap();
			WorkflowRequestTableRecord[] detailTableRecords=wri.getWorkflowDetailTableInfos()[groupid].getWorkflowRequestTableRecords();
			for(int i=0;i<detailTableRecords.length;i++){
				WorkflowRequestTableField[] detailTableFields=detailTableRecords[i].getWorkflowRequestTableFields();
				for(WorkflowRequestTableField detailField:detailTableFields){
					String curFieldid=detailField.getFieldId();
					if((";"+colCalItemStr+";").indexOf(";detailfield_"+curFieldid+";")>-1){
						String fieldValue=Util.null2String(detailField.getFieldValue());
						if(fieldValue.indexOf(",")>-1){		//去除千分位


							fieldValue=fieldValue.replaceAll(",", "");
						}
						if(null!=summap.get(curFieldid)){
							try{
								String sumValue=(String)summap.get(curFieldid);
								summap.put(curFieldid, (Double.parseDouble(sumValue)+Double.parseDouble(fieldValue)+""));
							}catch(NumberFormatException e){}
						}else{
							summap.put(curFieldid, fieldValue);
						}
					}
				}
			}*/
			//合计<tfoot>生成
			newTR.removeAttr("_target");
			Elements tds=newTR.children();
			for(int i=0;i<tds.size();i++){
				Element curTD=tds.get(i);
				if(i==0){
					curTD.html(SystemEnv.getHtmlLabelName(358, user.getLanguage()));
				}else{
					Element input=curTD.getElementsByTag("input").first();
					String fieldid=input.attr("name").substring(5);
					input.remove();
					curTD.attr("id","sum"+fieldid);
					/*if(summap.containsKey(fieldid)){
						String sumValue=Util.null2String(summap.get(fieldid));
						if(!"".equals(sumValue)){
							try{
								DecimalFormat df=new DecimalFormat("#.00");
								sumValue=df.format(Double.parseDouble(sumValue));
								curTD.html(sumValue);
							}catch(Exception e){}
						}
					}*/
				}
			}
			return newTR.toString();
		} catch (Exception e) {
			log.warn("DetailTable"+groupid+" calculate total error!");
			return "";
		}
	}

	/**
	 * 获取已办事宜的类型id集合，以逗号分隔（可选择是否包含办结事宜）







	 * @param userid
	 * @param hasShowProcessed
	 * @param conditions
	 * @return
	 */
    public String getHendledWorkflowRequestTypeIds(int userid, boolean hasShowProcessed, String[] conditions) {
        String select = " select distinct ";
        String fields = " t1.workflowid ";
        String from = " from workflow_requestbase t1,workflow_currentoperator t2 ";
        String where = " where t1.requestid=t2.requestid ";
        where += " and t2.usertype = 0 and t2.userid = " + userid;
        where += " and (t2.isremark in('2','4') or (t2.isremark=0 and t2.takisremark ='-2')) ";
        
        if (!hasShowProcessed) {
            where += " and t2.iscomplete=0 ";
        }
        where += " and  t2.islasttimes=1 ";
        where += " and t1.workflowID in(select id from workflow_base where isvalid in('1','3')) ";
        log.info("Follow is the 'where' condition SQL:\n" + where);
        if (conditions != null) {
            for (int m=0;m<conditions.length;m++) {
                String condition = conditions[m];
                where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
            }
        }
        
        return getSqlResult4SingleField(select, fields, from, where);
    }
    /**
     * 获取已办事宜的类型id集合，以逗号分隔（可选择是否包含办结事宜）


     * (多账号统一显示)



     * @param userid
     * @param hasShowProcessed
     * @param conditions
     * @return
     */
    public String getHendledWorkflowRequestTypeIds(int userid, boolean hasShowProcessed, String[] conditions, boolean belongtoshowFlag) {
        String select = " select distinct ";
        String fields = " t1.workflowid ";
        String from = " from workflow_requestbase t1,workflow_currentoperator t2 ";
        String where = " where t1.requestid=t2.requestid ";
      //当显示次账号的时候


        if(belongtoshowFlag){
            String belongtoIdsql = getsqlByBelongtoId(userid);
            where += " and t2.usertype = 0 and (t2.userid = " + userid + belongtoIdsql + ") ";
        }else{
            where += " and t2.usertype = 0 and t2.userid = " + userid;
        }
        where += " and (t2.isremark in('2','4') or (t2.isremark=0 and t2.takisremark ='-2')) ";
        
        if (!hasShowProcessed) {
            where += " and t2.iscomplete=0 ";
        }
        where += " and  t2.islasttimes=1 ";
        where += " and t1.workflowID in(select id from workflow_base where isvalid in('1','3')) ";
        log.info("Follow is the 'where' condition SQL:\n" + where);
        if (conditions != null) {
            for (int m=0;m<conditions.length;m++) {
                String condition = conditions[m];
                where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
            }
        }
        
        return getSqlResult4SingleField(select, fields, from, where);
    }

    /**
     * 获取我的请求的类型id集合，以逗号分隔
     * @param userid
     * @param conditions
     * @return
     */
    public String getMyWorkflowRequestTypeIds(int userid, String[] conditions) {
        String select = " select distinct ";
        String fields = " t1.workflowid ";
        String from = " from workflow_requestbase t1,workflow_currentoperator t2 ";
        String where = " where t1.requestid=t2.requestid ";
        where += " and (t2.isremark is not null or t2.isremark !='') and t2.usertype = 0 and t2.userid = " + userid;
        where += " and t1.creater = " + userid + " and t1.creatertype = 0 and (t1.deleted=0 or t1.deleted is null) and t2.islasttimes=1 ";
        where += " and t1.workflowID in(select id from workflow_base where isvalid in('1','3')) ";
        log.info("Follow is the 'where' condition SQL:\n" + where);
        if (conditions != null) {
            for (int m=0;m<conditions.length;m++) {
                String condition = conditions[m];
                where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
            }
        }

        return getSqlResult4SingleField(select, fields, from, where);
    }
    /**
     * 获取我的请求的类型id集合，以逗号分隔
     * (多账号统一显示)
     * @param userid
     * @param conditions
     * @return
     */
    public String getMyWorkflowRequestTypeIds(int userid, String[] conditions, boolean belongtoshowFlag) {
        String select = " select distinct ";
        String fields = " t1.workflowid ";
        String from = " from workflow_requestbase t1,workflow_currentoperator t2 ";
        String where = " where t1.requestid=t2.requestid ";
      //当显示次账号的时候


        if(belongtoshowFlag){
            User user = this.getUser(userid);
            String belongtoIds = user.getBelongtoids();
            StringBuffer belongtoIdsql = new StringBuffer();
            StringBuffer belongtoIdcreatesql = new StringBuffer();
            if(StringUtils.isNotEmpty(belongtoIds)){
                String[] belongtoIdArray =  belongtoIds.split(",");
                for(String belongtoId : belongtoIdArray){
                    if(StringUtils.isNotEmpty(belongtoId)){
                        belongtoIdsql.append(" OR t2.userid = " + belongtoId);
                        belongtoIdcreatesql.append(" OR t1.creater = " + belongtoId);
                    }
                }
            }
            where += " and (t2.isremark is not null or t2.isremark !='') and t2.usertype = 0 and (t2.userid = " + userid + belongtoIdsql + ") ";
            where += " and (t1.creater = " + userid + belongtoIdcreatesql + " ) and t1.creatertype = 0 and (t1.deleted=0 or t1.deleted is null) and t2.islasttimes=1 ";
        }else{
            where += " and (t2.isremark is not null or t2.isremark !='') and t2.usertype = 0 and t2.userid = " + userid;
            where += " and t1.creater = " + userid + " and t1.creatertype = 0 and (t1.deleted=0 or t1.deleted is null) and t2.islasttimes=1 ";
        }
        where += " and t1.workflowID in(select id from workflow_base where isvalid in('1','3')) ";
        log.info("Follow is the 'where' condition SQL:\n" + where);
        if (conditions != null) {
            for (int m=0;m<conditions.length;m++) {
                String condition = conditions[m];
                where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
            }
        }

        return getSqlResult4SingleField(select, fields, from, where);
    }
    
    /**
     * 获取办结事宜的类型id集合，以逗号分隔
     * @param userid
     * @param conditions
     * @return
     */
    public String getProcessedWorkflowRequestTypeIds(int userid, String[] conditions) {
        String select = " select distinct ";
        String fields = " t1.workflowid ";
        String from = " from workflow_requestbase t1,workflow_currentoperator t2 ";
        String where = " where t1.requestid=t2.requestid ";
        where += " and t2.usertype = 0 and t2.userid = " + userid;
        where += " and t2.isremark in('2','4') and t1.currentnodetype = '3' and iscomplete=1 and islasttimes=1 ";
        where += " and t1.workflowID in(select id from workflow_base where isvalid in('1','3')) ";
        log.info("Follow is the 'where' condition SQL:\n" + where);
        if (conditions != null)
            for (int m=0;m<conditions.length;m++) {
                String condition = conditions[m];
                where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
            }


        return getSqlResult4SingleField(select, fields, from, where);
    }

    /**
     * 获取办结事宜的类型id集合，以逗号分隔
     * (多账号统一显示)
     * @param userid
     * @param conditions
     * @param belongtoshowFlag
     * @return
     */
    public String getProcessedWorkflowRequestTypeIds(int userid, String[] conditions,boolean belongtoshowFlag ) {
        String select = " select distinct ";
        String fields = " t1.workflowid ";
        String from = " from workflow_requestbase t1,workflow_currentoperator t2 ";
        String where = " where t1.requestid=t2.requestid ";
        where += " and t2.usertype = 0 and t2.userid = " + userid;
        where += " and t2.isremark in('2','4') and t1.currentnodetype = '3' and iscomplete=1 and islasttimes=1 ";
        where += " and t1.workflowID in(select id from workflow_base where isvalid in('1','3')) ";
        log.info("Follow is the 'where' condition SQL:\n" + where);
        if (conditions != null)
            for (int m=0;m<conditions.length;m++) {
                String condition = conditions[m];
                where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
            }


        return getSqlResult4SingleField(select, fields, from, where);
    }
    
    /**
     * main方法（测试用）







     * @param args
     */
    public static void main(String[] args) {
//        WorkflowServiceImpl wsi = new WorkflowServiceImpl();
//        System.out.println(wsi.getToDoWorkflowRequestTypeIds(122, true, null));
    }
    
    /**
     * 获取待办事宜的类型id集合，以逗号分隔（可选择是否包含抄送事宜）
     * @param userid
     * @param hasShowCopy
     * @param conditions
     * @return
     */
    public String getToDoWorkflowRequestTypeIds(int userid, boolean hasShowCopy, String[] conditions) {
        
        String remark = "'1','5','7'";
        if (hasShowCopy) {
            remark += ",'8','9'";
        }
        
        String select = " select distinct ";
        String fields = " t1.workflowid ";
        String from = " from workflow_requestbase t1,workflow_currentoperator t2 ";
        String where = " where t1.requestid=t2.requestid ";
        where += " and t2.usertype = 0 and t2.userid = " + userid;
        where += " and ((t2.isremark='0' and (t2.takisremark is null or t2.takisremark='0' )) or t2.isremark in(" + remark +")) and t2.islasttimes=1 ";
        where += " and t1.workflowID in(select id from workflow_base where isvalid in('1','3')) ";
        log.info("Follow is the 'where' condition SQL:\n" + where);
        if (conditions != null)
            for (int m=0;m<conditions.length;m++) {
                String condition = conditions[m];
                where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
            }

        return getSqlResult4SingleField(select, fields, from, where);        
    }

    /**
     * 获取待办事宜的类型id集合，以逗号分隔（可选择是否包含抄送事宜）
     * (多账号统一显示)
     * @param userid
     * @param hasShowCopy
     * @param conditions
     * @param belongtoshowFlag
     * @return
     */
    public String getToDoWorkflowRequestTypeIds(int userid, boolean hasShowCopy, String[] conditions,boolean belongtoshowFlag) {
        
        String remark = "'1','5','7'";
        if (hasShowCopy) {
            remark += ",'8','9'";
        }
        
        String select = " select distinct ";
        String fields = " t1.workflowid ";
        String from = " from workflow_requestbase t1,workflow_currentoperator t2 ";
        String where = " where t1.requestid=t2.requestid ";
        //当显示次账号的时候


        if(belongtoshowFlag){
            String belongtoIdsql = getsqlByBelongtoId(userid);
            where += " and t2.usertype = 0 and (t2.userid = " + userid + belongtoIdsql + ") ";
        }else{
            where += " and t2.usertype = 0 and t2.userid = " + userid;
        }
        where += " and ((t2.isremark='0' and (t2.takisremark is null or t2.takisremark='0' )) or t2.isremark in(" + remark +")) and t2.islasttimes=1 ";
        where += " and t1.workflowID in(select id from workflow_base where isvalid in('1','3')) ";
        log.info("Follow is the 'where' condition SQL:\n" + where);
        if (conditions != null)
            for (int m=0;m<conditions.length;m++) {
                String condition = conditions[m];
                where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
            }

        return getSqlResult4SingleField(select, fields, from, where);        
    }
    
    /**
     * 获取抄送事宜的类型id集合
     * @param userid
     * @param conditions
     * @return
     */
    public String getCCWorkflowRequestTypeIds(int userid, String[] conditions) {
        String select = " select distinct ";
        String fields = " t1.workflowid ";
        String from = " from workflow_requestbase t1,workflow_currentoperator t2 ";
        String where = " where t1.requestid=t2.requestid ";
        where += " and t2.usertype = 0 and t2.userid = " + userid;
        where += " and t2.isremark in( '8','9' ) and t2.islasttimes=1 ";
        where += " and t1.workflowID in(select id from workflow_base where isvalid in('1','3')) ";
        log.info("Follow is the 'where' condition SQL:\n" + where);
        if (conditions != null)
            for (int m=0;m<conditions.length;m++) {
                String condition = conditions[m];
                where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
            }

        return getSqlResult4SingleField(select, fields, from, where);
    }
    /**
     * 获取抄送事宜的类型id集合
     * @param userid
     * @param conditions
     * @return
     */
    public String getCCWorkflowRequestTypeIds(int userid, String[] conditions,boolean belongtoshowFlag) {
        String select = " select distinct ";
        String fields = " t1.workflowid ";
        String from = " from workflow_requestbase t1,workflow_currentoperator t2 ";
        String where = " where t1.requestid=t2.requestid ";
        //当显示次账号的时候


        if(belongtoshowFlag){
            String belongtoIdsql = getsqlByBelongtoId(userid);
            where += " and t2.usertype = 0 and (t2.userid = " + userid + belongtoIdsql + ") ";
        }else{
            where += " and t2.usertype = 0 and t2.userid = " + userid;
        }
        where += " and t2.isremark in( '8','9' ) and t2.islasttimes=1 ";
        where += " and t1.workflowID in(select id from workflow_base where isvalid in('1','3')) ";
        log.info("Follow is the 'where' condition SQL:\n" + where);
        if (conditions != null)
            for (int m=0;m<conditions.length;m++) {
                String condition = conditions[m];
                where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
            }

        return getSqlResult4SingleField(select, fields, from, where);
    }
    
    private String getSqlResult4SingleField(String select, String fields, String from, String where) {
        String result = "";
        String sql = select + fields + from + where;
        
        RecordSet rs = new RecordSet();
        rs.executeSql(sql);
        while (rs.next()) {
            result += "," + rs.getString(1);
        }
        
        if (result.length() > 1) {
            result = result.substring(1);
        }
        return result;
    }
    
    
    /**
     * 与我相关流程数量
     */
    public int getWorkflowRequestCount(int userid, String[] conditions) {
        String select = " select distinct ";
        String fields = " t1.createdate,t1.createtime,t1.creater,t1.currentnodeid,t1.currentnodetype,t1.lastoperator,t1.creatertype,t1.lastoperatortype,t1.lastoperatedate,t1.lastoperatetime,t1.requestid,t1.requestname,t1.requestlevel,t1.workflowid,t2.receivedate,t2.receivetime ";
        String from = " from workflow_requestbase t1,workflow_currentoperator t2 ";
        String where = " where t1.requestid=t2.requestid ";
        where += " and t2.usertype = 0 and t2.userid = " + userid;
        where += " and t2.islasttimes=1 ";
        where += " and t1.workflowID in(select id from workflow_base where isvalid in('1','3')) ";
        log.info("Follow is the 'where' condition SQL:\n" + where);
        if (conditions != null)
            for (int m=0;m<conditions.length;m++) {
                String condition = conditions[m];
                where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
            }

        String sql = getPaginationCountSql(select, fields, from, where);

        return getWorkflowRequestCount(sql);
    }

    /**
     * 与我相关流程数量
     * (多账号统一显示)
     */
    public int getWorkflowRequestCount(int userid, String[] conditions, boolean belongtoshowFlag) {
        String select = " select distinct ";
        String fields = " t1.createdate,t1.createtime,t1.creater,t1.currentnodeid,t1.currentnodetype,t1.lastoperator,t1.creatertype,t1.lastoperatortype,t1.lastoperatedate,t1.lastoperatetime,t1.requestid,t1.requestname,t1.requestlevel,t1.workflowid,t2.receivedate,t2.receivetime,t2.userid ";
        String from = " from workflow_requestbase t1,workflow_currentoperator t2 ";
        String where = " where t1.requestid=t2.requestid ";
      //当显示次账号的时候


        if(belongtoshowFlag){
            String belongtoIdsql = getsqlByBelongtoId(userid);
            where += " and t2.usertype = 0 and (t2.userid = " + userid + belongtoIdsql + ") ";
        }else{
            where += " and t2.usertype = 0 and t2.userid = " + userid;
        }
        where += " and t2.islasttimes=1 ";
        where += " and t1.workflowID in(select id from workflow_base where isvalid in('1','3')) ";
        log.info("Follow is the 'where' condition SQL:\n" + where);
        if (conditions != null)
            for (int m=0;m<conditions.length;m++) {
                String condition = conditions[m];
                where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
            }

        String sql = getPaginationCountSql(select, fields, from, where);

        return getWorkflowRequestCount(sql);
    }
    
    /**
     * 所有与我相关信息




     */
    public WorkflowRequestInfo[] getWorkflowRequestList(int pageNo, int pageSize, int recordCount, int userid, String[] conditions) {
        if (pageNo < 1)
            pageNo = 1;
        
        String select = " select distinct ";
        String fields = " t1.createdate,t1.createtime,t1.creater,t1.currentnodeid,t1.currentnodetype,t1.lastoperator,t1.creatertype,t1.lastoperatortype,t1.lastoperatedate,t1.lastoperatetime,t1.requestid,t1.requestname,t1.requestlevel,t1.workflowid,t2.receivedate,t2.receivetime, t2.isremark, t2.nodeid, t1.formsignaturemd5, t1.requestnamenew, t1.status  ";
        String from = " from workflow_requestbase t1,workflow_currentoperator t2 ";
        String where = " where t1.requestid=t2.requestid ";
        where += " and t2.usertype = 0 and t2.userid = " + userid;
        where += " and t2.islasttimes=1 ";
        where += " and t1.workflowID in(select id from workflow_base where isvalid in('1','3')) ";
        log.info("Follow is the 'where' condition SQL:\n" + where);
        if (conditions != null)
            for (int m=0;m<conditions.length;m++) {
                String condition = conditions[m];
                where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
            }
        
        String orderby = " order by t2.receivedate desc,t2.receivetime desc,t1.requestid desc ";
        String orderby1 = " order by receivedate asc,receivetime asc,requestid asc ";
        String orderby2 = " order by receivedate desc,receivetime desc,requestid desc ";
            
        String sql = getPaginationSql(select, fields, from, where, orderby, orderby1, orderby2, pageNo, pageSize, recordCount);

        return getWorkflowRequestList(sql, userid);
    }

    /**
     * 所有与我相关信息


     * (多账号统一显示)

     */
    public WorkflowRequestInfo[] getWorkflowRequestList(int pageNo, int pageSize, int recordCount, int userid, String[] conditions,boolean belongtoshowFlag) {
        if (pageNo < 1)
            pageNo = 1;
        
        String select = " select distinct ";
        String fields = " t1.createdate,t1.createtime,t1.creater,t1.currentnodeid,t1.currentnodetype,t1.lastoperator,t1.creatertype,t1.lastoperatortype,t1.lastoperatedate,t1.lastoperatetime,t1.requestid,t1.requestname,t1.requestlevel,t1.workflowid,t2.receivedate,t2.receivetime, t2.isremark, t2.nodeid, t1.formsignaturemd5, t1.requestnamenew, t1.status  ";


        //当主次账号统一显示的时候,获取用户名和用户类型
        if(belongtoshowFlag){
            fields +=",t2.userid ,t2.usertype";
        }
        
        String from = " from workflow_requestbase t1,workflow_currentoperator t2 ";
        String where = " where t1.requestid=t2.requestid ";
        //当显示次账号的时候


        if(belongtoshowFlag){
            String belongtoIdsql = getsqlByBelongtoId(userid);
            where += " and t2.usertype = 0 and (t2.userid = " + userid + belongtoIdsql + ") ";
        }else{
            where += " and t2.usertype = 0 and t2.userid = " + userid;
        }
        where += " and t2.islasttimes=1 ";
        where += " and t1.workflowID in(select id from workflow_base where isvalid in('1','3')) ";
        log.info("Follow is the 'where' condition SQL:\n" + where);
        if (conditions != null)
            for (int m=0;m<conditions.length;m++) {
                String condition = conditions[m];
                where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
            }
        
        String orderby = " order by t2.receivedate desc,t2.receivetime desc,t1.requestid desc ";
        String orderby1 = " order by receivedate asc,receivetime asc,requestid asc ";
        String orderby2 = " order by receivedate desc,receivetime desc,requestid desc ";
            
        String sql = getPaginationSql(select, fields, from, where, orderby, orderby1, orderby2, pageNo, pageSize, recordCount);

        return getWorkflowRequestList(sql, userid);
    }
    
    
    /**
     * 获取与我相关的类型id集合，以逗号分隔
     * @param userid
     * @param conditions
     * @return
     */
    public String getWorkflowRequestTypeIds(int userid, String[] conditions) {
        String select = " select distinct ";
        String fields = " t1.workflowid ";
        String from = " from workflow_requestbase t1,workflow_currentoperator t2 ";
        String where = " where t1.requestid=t2.requestid ";
        where += " and t2.usertype = 0 and t2.userid = " + userid;
        where += " and t2.islasttimes=1 ";
        where += " and t1.workflowID in(select id from workflow_base where isvalid in('1','3')) ";
        log.info("Follow is the 'where' condition SQL:\n" + where);
        if (conditions != null)
            for (int m=0;m<conditions.length;m++) {
                String condition = conditions[m];
                where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
            }

        return getSqlResult4SingleField(select, fields, from, where);        
    }

    /**
     * 获取与我相关的类型id集合，以逗号分隔
     * (多账号统一显示)
     * @param userid
     * @param conditions
     * @param belongtoshowFlag
     * @return
     */
    public String getWorkflowRequestTypeIds(int userid, String[] conditions,boolean belongtoshowFlag) {
        String select = " select distinct ";
        String fields = " t1.workflowid ";
        String from = " from workflow_requestbase t1,workflow_currentoperator t2 ";
        String where = " where t1.requestid=t2.requestid ";
      //当显示次账号的时候


        if(belongtoshowFlag){
            String belongtoIdsql = getsqlByBelongtoId(userid);
            where += " and t2.usertype = 0 and (t2.userid = " + userid + belongtoIdsql + ") ";
        }else{
            where += " and t2.usertype = 0 and t2.userid = " + userid;
        }
        where += " and t2.islasttimes=1 ";
        where += " and t1.workflowID in(select id from workflow_base where isvalid in('1','3')) ";
        log.info("Follow is the 'where' condition SQL:\n" + where);
        if (conditions != null)
            for (int m=0;m<conditions.length;m++) {
                String condition = conditions[m];
                where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
            }

        return getSqlResult4SingleField(select, fields, from, where);        
    }
    /**
     * 根据userid取得belongtoIds，生成拼接sql文(SQL固定为“t2.userid = ”)
     */
    private String getsqlByBelongtoId(int userid){
        User user = this.getUser(userid);
        String belongtoIds = user.getBelongtoids();
        StringBuffer belongtoIdsql = new StringBuffer();
        if(StringUtils.isNotEmpty(belongtoIds)){
            String[] belongtoIdArray =  belongtoIds.split(",");
            for(String belongtoId : belongtoIdArray){
                if(StringUtils.isNotEmpty(belongtoId)){
                    belongtoIdsql.append(" OR t2.userid = " + belongtoId);
                }
            }
        }
        return belongtoIdsql.toString();
        
    }
	
    /**
     * 监控信息
     */
    public WorkflowRequestInfo[] getMonitorWorkflowRequestList(int pageNo, int pageSize, int recordCount, int userid, String[] conditions) {
        return getMonitorWorkflowRequestList(pageNo, pageSize, recordCount, userid, conditions, 0);

    }

    private boolean monitorFlag = false;
    /**
     * 监控信息
     */
    public WorkflowRequestInfo[] getMonitorWorkflowRequestList(int pageNo, int pageSize, int recordCount, int userid, String[] conditions, int orderflag) {
        if (pageNo < 1)
            pageNo = 1;

        // String remark = "'1','5','7','8','9'";
        Monitor Monitor = new Monitor();
        String select = " select distinct ";
        //String fields = " t1.createdate,t1.createtime,t1.creater,t1.currentnodeid,t1.currentnodetype,t1.lastoperator,t1.creatertype,t1.lastoperatortype,t1.lastoperatedate,t1.lastoperatetime,t1.requestid,t1.requestname,t1.requestlevel,t1.workflowid,t2.receivedate,t2.receivetime, t2.isremark, t2.nodeid, t1.formsignaturemd5, t1.requestnamenew, t1.status ";
        String fields = " t1.createdate,t1.createtime,t1.creater,t1.currentnodeid,t1.currentnodetype,t1.lastoperator,t1.creatertype,t1.lastoperatortype,t1.lastoperatedate,t1.lastoperatetime,t1.requestid,t1.requestname,t1.requestlevel,t1.workflowid, t1.formsignaturemd5, t1.requestnamenew, t1.status ";
        // String from = " from workflow_requestbase t1,workflow_currentoperator
        // t2 ";
        // String fields = " t1.requestid, t1.currentnodeid, t1.createdate,
        // t1.createtime,t1.lastoperatedate, t1.lastoperatetime,t1.creater,
        // t1.creatertype, t1.workflowid, t1.requestname, t1.status,
        // t1.requestlevel,t1.currentstatus,t1.currentnodetype ";
        String from = " from workflow_requestbase t1 ";
        //过滤掉测试版本


        from += " inner join workflow_base wfb on t1.workflowid = wfb.id and wfb.isvalid in (0,1,3) ";
        
        String monitorsql = Monitor.getMobileMonitorSql(userid + "");
        String newsql = "";
        if ("".equals(monitorsql)) {
            newsql += " and 1 = 2 ";
        } else {
            newsql += " and (" + monitorsql + ")";
        }
        if (!"".equals(newsql)) {
            newsql = newsql.replaceFirst("and", "");
        }
        //new weaver.general.BaseBean().writeLog("---6158---监控-newsql--<>>>>>" + newsql);
        String where = "where  " + newsql + "  ";
        //where += " and t1.requestid=t2.requestid ";
        //where += " and t2.usertype = 0 ";
        // where += " and ((t2.isremark='0' and (t2.takisremark is null or
        // t2.takisremark='0' )) or t2.isremark in(" + remark + ")) and
        // t2.islasttimes=1 ";
        // where += " and t1.workflowID in(select id from workflow_base where
        // isvalid in('1','3')) ";
        log.info("Follow is the 'where' condition SQL:\n" + where);
        if (conditions != null)
            for (int m = 0; m < conditions.length; m++) {
                String condition = conditions[m];
                where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
            }

        String orderby = " order by t1.createdate desc,t1.createtime desc,t1.requestid desc ";
        String orderby1 = " order by createdate asc,createtime asc,requestid asc ";
        String orderby2 = " order by createdate desc,createtime desc,requestid desc ";
        // 1：正序



        monitorFlag = true;
        if (orderflag == 1) {
            orderby = " order by t1.createdate asc,t1.createtime asc,t1.requestid asc ";
            orderby1 = " order by createdate desc,createtime desc,requestid desc ";
            orderby2 = " order by createdate asc,createtime asc,requestid asc ";
        }

        String sql = getPaginationSql(select, fields, from, where, orderby, orderby1, orderby2, pageNo, pageSize, recordCount);
        //new weaver.general.BaseBean().writeLog("---6185---监控-sql--<>>>>>" + sql);
        return getWorkflowRequestList(sql, userid);
    }

    /**
     * 监控流程数量
     */
    public int getMonitorWorkflowRequestCount(int userid, String[] conditions) {

        // String remark = "'1','5','7','8','9'";
        Monitor Monitor = new Monitor();
        String select = " select distinct ";
        //String fields = " t1.createdate,t1.createtime,t1.creater,t1.currentnodeid,t1.currentnodetype,t1.lastoperator,t1.creatertype,t1.lastoperatortype,t1.lastoperatedate,t1.lastoperatetime,t1.requestid,t1.requestname,t1.requestlevel,t1.workflowid,t2.receivedate,t2.receivetime, t2.isremark, t2.nodeid, t1.formsignaturemd5, t1.requestnamenew, t1.status ";
        String fields = " t1.createdate,t1.createtime,t1.creater,t1.currentnodeid,t1.currentnodetype,t1.lastoperator,t1.creatertype,t1.lastoperatortype,t1.lastoperatedate,t1.lastoperatetime,t1.requestid,t1.requestname,t1.requestlevel,t1.workflowid, t1.formsignaturemd5, t1.requestnamenew, t1.status ";
        // String from = " from workflow_requestbase t1,workflow_currentoperator
        // t2 ";
        // String fields = " t1.requestid, t1.currentnodeid, t1.createdate,
        // t1.createtime,t1.lastoperatedate, t1.lastoperatetime,t1.creater,
        // t1.creatertype, t1.workflowid, t1.requestname, t1.status,
        // t1.requestlevel,t1.currentstatus,t1.currentnodetype ";
        String from = " from workflow_requestbase t1";
        //过滤掉测试版本


        from += " inner join workflow_base wfb on t1.workflowid = wfb.id and wfb.isvalid in (0,1,3) ";
        
        String monitorsql = Monitor.getMobileMonitorSql(userid + "");
        String newsql = "";
        if ("".equals(monitorsql)) {
            newsql += " and 1 = 2 ";
        } else {
            newsql += " and (" + monitorsql + ")";
        }
        if (!"".equals(newsql)) {
            newsql = newsql.replaceFirst("and", "");
        }
        String where = "where  " + newsql + "  ";
       // where += " and t1.requestid=t2.requestid ";
       // where += " and t2.usertype = 0 ";
        // where += " and ((t2.isremark='0' and (t2.takisremark is null or
        // t2.takisremark='0' )) or t2.isremark in(" + remark + ")) and
        // t2.islasttimes=1 ";
        // where += " and t1.workflowID in(select id from workflow_base where
        // isvalid in('1','3')) ";
        log.info("Follow is the 'where' condition SQL:\n" + where);
        if (conditions != null)
            for (int m = 0; m < conditions.length; m++) {
                String condition = conditions[m];
                where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
            }

        String sql = getPaginationCountSql(select, fields, from, where);
        return getWorkflowRequestCount(sql);
    }

    /**
     * 获取监控事宜的类型id集合，以逗号分隔
     * 
     * @param userid
     * @param hasShowCopy
     * @param conditions
     * @return
     */
    public String getMonitorWorkflowRequestTypeIds(int userid, String[] conditions) {

        // String remark = "'1','5','7','8','9'";
        Monitor Monitor = new Monitor();
        String select = " select distinct ";
        //String fields = " t1.createdate,t1.createtime,t1.creater,t1.currentnodeid,t1.currentnodetype,t1.lastoperator,t1.creatertype,t1.lastoperatortype,t1.lastoperatedate,t1.lastoperatetime,t1.requestid,t1.requestname,t1.requestlevel,t1.workflowid,t2.receivedate,t2.receivetime, t2.isremark, t2.nodeid, t1.formsignaturemd5, t1.requestnamenew, t1.status ";
        String fields = " t1.workflowid ";
        // String from = " from workflow_requestbase t1,workflow_currentoperator
        // t2 ";
        // String fields = " t1.requestid, t1.currentnodeid, t1.createdate,
        // t1.createtime,t1.lastoperatedate, t1.lastoperatetime,t1.creater,
        // t1.creatertype, t1.workflowid, t1.requestname, t1.status,
        // t1.requestlevel,t1.currentstatus,t1.currentnodetype ";
        String from = " from workflow_requestbase t1 ";
        //过滤掉测试版本


        from += " inner join workflow_base wfb on t1.workflowid = wfb.id and wfb.isvalid in (0,1,3) ";
        
        String monitorsql = Monitor.getMobileMonitorSql(userid + "");
        String newsql = "";
        if ("".equals(monitorsql)) {
            newsql += " and 1 = 2 ";
        } else {
            newsql += " and (" + monitorsql + ")";
        }
        if (!"".equals(newsql)) {
            newsql = newsql.replaceFirst("and", "");
        }
        String where = "where  " + newsql + "  ";
      //  where += " and t1.requestid=t2.requestid ";
       // where += " and t2.usertype = 0 and t2.userid = " + userid;
        // where += " and ((t2.isremark='0' and (t2.takisremark is null or
        // t2.takisremark='0' )) or t2.isremark in(" + remark + ")) and
        // t2.islasttimes=1 ";
        // where += " and t1.workflowID in(select id from workflow_base where
        // isvalid in('1','3')) ";
        log.info("Follow is the 'where' condition SQL:\n" + where);
        if (conditions != null)
            for (int m = 0; m < conditions.length; m++) {
                String condition = conditions[m];
                where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
            }

        return getSqlResult4SingleField(select, fields, from, where);
    }

    /**
     * 督办信息
     */
    public WorkflowRequestInfo[] getSuperviseWorkflowRequestList(int pageNo, int pageSize, int recordCount, int userid, String[] conditions) {
        return getSuperviseWorkflowRequestList(pageNo, pageSize, recordCount, userid, conditions, 0);

    }

    /**
     * 督办信息
     */
    private ArrayList _wftypes_Temp;
    private String _tmpTableName_Temp;
    private int _wftotalCount;

    public ArrayList get_wftypes_Temp() {
        return _wftypes_Temp;
    }

    public void set_wftypes_Temp(ArrayList temp) {
        _wftypes_Temp = temp;
    }

    public String get_tmpTableName_Temp() {
        return _tmpTableName_Temp;
    }

    public void set_tmpTableName_Temp(String tableName_Temp) {
        _tmpTableName_Temp = tableName_Temp;
    }
    public int get_wftotalCount() {
        return _wftotalCount;
    }

    public void set_wftotalCount(int count) {
        _wftotalCount = count;
    }
    
    public WorkflowRequestInfo[] getSuperviseWorkflowRequestList(int pageNo, int pageSize, int recordCount, int userid, String[] conditions, int orderflag) {
        if (pageNo < 1)pageNo = 1;
        
        String workflowid = "";
        RecordSet RecordSet = new RecordSet();
        WFUrgerManager WFUrgerManager = new WFUrgerManager();
        WFWorkflowTypes WFWorkflowTypes = new WFWorkflowTypes();
        User user = this.getUser(userid);
        WFUrgerManager.setLogintype(Util.getIntValue(user.getLogintype()));
        WFUrgerManager.setUserid(userid);
        //WFUrgerManager.setSqlwhere(sqlwhere);
        //WFUrgerManager.setWorkflowIDs(workflowid);

        //提高效率，只调用一次


        //ArrayList wftypes=WFUrgerManager.getWrokflowTree();
        //String tmpTableName = WFUrgerManager.getTmpTableName();
//        ArrayList wftypes = _wftypes_Temp;
//        String tmpTableName = _tmpTableName_Temp;
//        if(wftypes == null){
//            //wftypes= WFUrgerManager.getWrokflowTree();
//            //tmpTableName = WFUrgerManager.getTmpTableName();
//        }
        
        String requestidGetSql = WFUrgerManager.getWfShareSqlWhere();
        
        String requestids = "";
        String requestSql = "";
        Map mapRequestIDs = new HashMap();
        //性能优化，不在取得requestid
        //for(int i=0;i<wftypes.size();i++){
        //    WFWorkflowTypes wftype=(WFWorkflowTypes)wftypes.get(i);
        //    ArrayList workflows=wftype.getWorkflows();
        //
        //    for(int j=0;j<workflows.size();j++){
        //        //if(j>0) break;
        //        WFWorkflows wfObj=(WFWorkflows)workflows.get(j);
        //        String tempWorkflow=wfObj.getWorkflowid()+"";
        //        //查询的流程ID列表为空，或者流程ID列表包含
        //        if("".equals(workflowid)) {
        //            ArrayList requests = null;
        //
        //           
        //            requests=wfObj.getReqeustids();
        //           
        //            
        //            for(int k=0;k<requests.size();k++){
        //                if(requestids.equals("")){
        //                    requestids=(String)requests.get(k);
        //                }else{
        //                    requestids+=","+requests.get(k);
        //                }
        //            }
        //        }
        //    }
        //}
        
        
       // String remark = "'1','5','7','8','9'";
        String newsql =" where (t1.currentnodetype is null or t1.currentnodetype<>'3') and t1.requestid=t2.requestid and (t1.deleted<>1 or (t1.deleted is null or t1.deleted='')) ";
        String fromSql = "";
        String sqlWhere = newsql;
        //String backfields = " t1.requestid, t1.createdate, t1.createtime,t1.creater, t1.creatertype, t1.workflowid, t1.requestname,t1.requestnamenew, t1.status,t1.requestlevel,t1.currentnodeid,t2.receivedatetime";
        String backfields = " t1.requestid, t1.createdate, t1.createtime,t1.creater, t1.creatertype, t1.workflowid, t1.requestname,t1.requestnamenew, t1.status,t1.requestlevel,t1.currentnodeid";
        if(RecordSet.getDBType().equals("oracle")){
            backfields += ",t1.createdate || t1.createtime as receivedatetime";
        }else{
            backfields += ",t1.createdate + t1.createtime as receivedatetime";
        }
//        if(RecordSet.getDBType().equals("oracle")){
//            fromSql = " from (select requestid,max(receivedate||' '||receivetime) as receivedatetime from workflow_currentoperator group by requestid) t2,workflow_requestbase t1 ";
//        }else{
//            fromSql = " from (select requestid,max(receivedate+' '+receivetime) as receivedatetime from workflow_currentoperator group by requestid) t2,workflow_requestbase t1 ";
//        }
        fromSql = " from (" + requestidGetSql + ") t2,workflow_requestbase t1 ";
//        if (!requestids.equals("")) {
//           
//           sqlWhere += " AND t1.requestid in("+requestids+") ";
//        }else{
//            sqlWhere+=" and 1>2 ";
//        }
        //过滤停留在督办人待办的流程


        sqlWhere += "        and exists (select 1 from workFlow_CurrentOperator where t1.workflowid=workflow_currentoperator.workflowid and t1.requestid=workflow_currentoperator.requestid) and NOT EXISTS (select 1 from workFlow_CurrentOperator t where t.isremark in('0','1','5','8','9','7') and t.userid=" + userid + " and t.usertype=" + (Util.getIntValue(user.getLogintype()) - 1) + " and t.requestid=t1.requestid)";
        if(RecordSet.getDBType().equals("oracle"))
        {     
            sqlWhere += " and ((t1.currentstatus = -1 or t1.currentstatus is null) or (t1.currentstatus=0 and t1.creater in ("+user.getUID()+"))) ";  
        }
        else
        {  
            sqlWhere += " and ((t1.currentstatus = -1 or t1.currentstatus is null) or (t1.currentstatus=0 and t1.creater="+user.getUID()+")) ";   
        }
        String sqlOrderBy = " order by t1.createdate,t1.createtime ";
        //String table1 = "select " + backfields + fromSql + sqlWhere;
        
        //String select = " select  ";
        String select = " select ";
        //String fields = " t1.requestid, t1.createdate, t1.createtime,t1.creater, t1.creatertype, t1.workflowid, t1.requestname, t1.requestnamenew, t1.status,t1.requestlevel,t1.currentnodeid,t1.receivedatetime";
        String fields = backfields;
        //fromSql =" from ("+table1+") t1 ";
        //String fields = " t1.requestid, t1.createdate, t1.createtime,t1.creater, t1.creatertype, t1.workflowid, t1.requestname, t1.requestnamenew, t1.status,t1.requestlevel,t1.currentnodeid,t1.receivedatetime";
        //String fields = " t1.createdate,t1.createtime,t1.creater,t1.currentnodeid,t1.currentnodetype,t1.lastoperator,t1.creatertype,t1.lastoperatortype,t1.lastoperatedate,t1.lastoperatetime,t1.requestid,t1.requestname,t1.requestlevel,t1.workflowid,t2.receivedate,t2.receivetime, t2.isremark, t2.nodeid, t1.formsignaturemd5, t1.requestnamenew, t1.status ";
        //String from = " from workflow_requestbase t1,workflow_currentoperator t2 ";
        //性能优化，不再使用临时表
//        if(tmpTableName != null) {
//            fromSql += " ,(Select requestId from "+tmpTableName+") t2 ";
//            sqlWhere = " where t1.requestid=t2.requestid ";
//        } else {
//            sqlWhere = "";
//        }
       // String where = " where t1.requestid=t2.requestid ";
        String where = sqlWhere;
        //where += " and t2.usertype = 0 and t2.userid = " + userid;
       // where += " and ((t2.isremark='0' and (t2.takisremark is null or t2.takisremark='0' )) or t2.isremark in(" + remark + ")) and t2.islasttimes=1 ";
       // where += " and t1.workflowID in(select id from workflow_base where isvalid in('1','3')) ";
        log.info("Follow is the 'where' condition SQL:\n" + where);
        if (conditions != null)
            for (int m = 0; m < conditions.length; m++) {
                String condition = conditions[m];
                where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
            }

//        String orderby = " order by t1.receivedatetime desc ";
//        String orderby1 = " order by receivedatetime asc ";
//        String orderby2 = " order by receivedatetime desc ";
        String orderby = " order by t1.createdate desc,t1.createtime desc ";
        String orderby1 = " order by createdate asc,createtime asc ";
        String orderby2 = " order by createdate desc,createtime desc ";
        // 1：正序




        if (orderflag == 1) {
//            orderby = " order by t1.receivedatetime asc ";
//            orderby1 = " order by receivedatetime asc ";
//            orderby2 = " order by receivedatetime desc ";
            orderby = " order by t1.createdate asc,t1.createtime asc ";
            orderby1 = " order by createdate asc,createtime asc ";
            orderby2 = " order by createdate desc,createtime desc ";
        }
        String sql2 = select + fields + fromSql +sqlWhere;
        String sql = getPaginationSql(select, fields, fromSql, where, orderby, orderby1, orderby2, pageNo, pageSize, recordCount);
        return getWorkflowRequestList(sql, userid);
    
    }


    /**
     * 督办流程数量
     */
    public int getSuperviseWorkflowRequestCount(int userid, String[] conditions) {

        String workflowid = "";
        RecordSet RecordSet = new RecordSet();
        WFUrgerManager WFUrgerManager = new WFUrgerManager();
        WFWorkflowTypes WFWorkflowTypes = new WFWorkflowTypes();
        User user = this.getUser(userid);
        WFUrgerManager.setLogintype(Util.getIntValue(user.getLogintype()));
        WFUrgerManager.setUserid(userid);
        //WFUrgerManager.setSqlwhere(sqlwhere);
        //WFUrgerManager.setWorkflowIDs(workflowid);

        //提高效率，只调用一次


        //ArrayList wftypes=WFUrgerManager.getWrokflowTree();
        //String tmpTableName = WFUrgerManager.getTmpTableName();
        ArrayList wftypes = _wftypes_Temp;
        String tmpTableName = _tmpTableName_Temp;
        if(wftypes == null){
            wftypes= WFUrgerManager.getWorkflowTreeCount();
            return WFUrgerManager.getTotalcounts();
            //tmpTableName = WFUrgerManager.getTmpTableName();
        }else{
            return _wftotalCount;
        }
        //性能优化，直接取得count数


        /*
        String requestids = "";
        String requestSql = "";
        Map mapRequestIDs = new HashMap();
        for(int i=0;i<wftypes.size();i++){
            WFWorkflowTypes wftype=(WFWorkflowTypes)wftypes.get(i);
            ArrayList workflows=wftype.getWorkflows();

            for(int j=0;j<workflows.size();j++){
                //if(j>0) break;
                WFWorkflows wfObj=(WFWorkflows)workflows.get(j);
                String tempWorkflow=wfObj.getWorkflowid()+"";
                //查询的流程ID列表为空，或者流程ID列表包含
                if("".equals(workflowid)) {
                    ArrayList requests = null;

                   
                    //requests=wfObj.getNewrequestids();
                    requests=wfObj.getReqeustids();
                   
                    
                    for(int k=0;k<requests.size();k++){
                        if(requestids.equals("")){
                            requestids=(String)requests.get(k);
                        }else{
                            requestids+=","+requests.get(k);
                        }
                    }
                }
            }
        }
        
        
       // String remark = "'1','5','7','8','9'";
        String newsql =" where (t1.currentnodetype is null or t1.currentnodetype<>'3') and t1.requestid=t2.requestid and (t1.deleted<>1 or (t1.deleted is null or t1.deleted='')) ";
        String fromSql = "";
        String sqlWhere = newsql;
        String backfields = " t1.requestid, t1.createdate, t1.createtime,t1.creater, t1.creatertype, t1.workflowid, t1.requestname,t1.requestnamenew, t1.status,t1.requestlevel,t1.currentnodeid,t2.receivedatetime";
        if(RecordSet.getDBType().equals("oracle")){
            fromSql = " from (select requestid,max(receivedate||' '||receivetime) as receivedatetime from workflow_currentoperator group by requestid) t2,workflow_requestbase t1 ";
        }else{
            fromSql = " from (select requestid,max(receivedate+' '+receivetime) as receivedatetime from workflow_currentoperator group by requestid) t2,workflow_requestbase t1 ";
        }
        if (!requestids.equals("")) {
           
           sqlWhere += " AND t1.requestid in("+requestids+") ";
        }else{
            sqlWhere+=" and 1>2 ";
        }  
        if(RecordSet.getDBType().equals("oracle"))
        {     
            sqlWhere += " and (nvl(t1.currentstatus,-1) = -1 or (nvl(t1.currentstatus,-1)=0 and t1.creater in ("+user.getUID()+"))) ";  
        }
        else
        {  
            sqlWhere += " and (isnull(t1.currentstatus,-1) = -1 or (isnull(t1.currentstatus,-1)=0 and t1.creater="+user.getUID()+")) ";   
        }
        String table1 = "select " + backfields + fromSql + sqlWhere;
        
        //String select = " select  ";
        String select = " select distinct ";
        String fields = " t1.requestid, t1.createdate, t1.createtime,t1.creater, t1.creatertype, t1.workflowid, t1.requestname, t1.requestnamenew, t1.status,t1.requestlevel,t1.currentnodeid,t1.receivedatetime";
        fromSql =" from ("+table1+") t1 ";
        //String fields = " t1.requestid, t1.createdate, t1.createtime,t1.creater, t1.creatertype, t1.workflowid, t1.requestname, t1.requestnamenew, t1.status,t1.requestlevel,t1.currentnodeid,t1.receivedatetime";
        //String fields = " t1.createdate,t1.createtime,t1.creater,t1.currentnodeid,t1.currentnodetype,t1.lastoperator,t1.creatertype,t1.lastoperatortype,t1.lastoperatedate,t1.lastoperatetime,t1.requestid,t1.requestname,t1.requestlevel,t1.workflowid,t2.receivedate,t2.receivetime, t2.isremark, t2.nodeid, t1.formsignaturemd5, t1.requestnamenew, t1.status ";
        //String from = " from workflow_requestbase t1,workflow_currentoperator t2 ";
        if(tmpTableName != null) {
            fromSql += " ,(Select requestId from "+tmpTableName+") t2 ";
            sqlWhere = " where t1.requestid=t2.requestid ";
        } else {
            sqlWhere = "";
        }
       // String where = " where t1.requestid=t2.requestid ";
        String where = sqlWhere;
        //where += sqlWhere;
        //where += " and t2.usertype = 0 and t2.userid = " + userid;
       // where += " and ((t2.isremark='0' and (t2.takisremark is null or t2.takisremark='0' )) or t2.isremark in(" + remark + ")) and t2.islasttimes=1 ";
       // where += " and t1.workflowID in(select id from workflow_base where isvalid in('1','3')) ";
        log.info("Follow is the 'where' condition SQL:\n" + where);
        if (conditions != null)
            for (int m = 0; m < conditions.length; m++) {
                String condition = conditions[m];
                where += (condition != null && !"".equals(condition)) ? " and " + condition : "";
            }

        String sql = getPaginationCountSql(select, fields, fromSql, where);

        return getWorkflowRequestCount(sql);
        */
    }

    /**
     * 获取督办事宜的类型id集合，以逗号分隔
     * 
     * @param userid
     * @param hasShowCopy
     * @param conditions
     * @return
     */
    public String getSuperviseWorkflowRequestTypeIds(int userid, String[] conditions) {

        
        //TODO
        //暂时只使用所有的有督办的workflowid
        String sql = "";
        sql +="         select distinct case ";
        sql +="         when t2.activeversionid is null then ";
        sql +="          t2.id ";
        sql +="         else ";
        sql +="          t2.activeversionid ";
        sql +="       end as workflowid ";
        sql +=" from shareinnerwfurger t1 ";
        sql +=" inner join workflow_base t2 ";
        sql +=" on t1.workflowid = t2.id ";
        sql +=" order by workflowid ";
        RecordSet rs = new RecordSet();
        rs.execute(sql);
        String workdlowids = "";
        while (rs.next()){
            String tempWorkflow = rs.getString("workflowid");
            if("".equals(workdlowids)){
                workdlowids += tempWorkflow;
            }else{
                workdlowids += ","+tempWorkflow;
            }
        }
        return workdlowids;
        
        
        
//        String workflowid = "";
//        RecordSet RecordSet = new RecordSet();
//        WFUrgerManager WFUrgerManager = new WFUrgerManager();
//        WFWorkflowTypes WFWorkflowTypes = new WFWorkflowTypes();
//        User user = this.getUser(userid);
//        WFUrgerManager.setLogintype(Util.getIntValue(user.getLogintype()));
//        WFUrgerManager.setUserid(userid);
//        //WFUrgerManager.setSqlwhere(sqlwhere);
//        //WFUrgerManager.setWorkflowIDs(workflowid);
//        
//
//        //提高效率，只调用一次


//        //ArrayList wftypes=WFUrgerManager.getWrokflowTree();
//        //String tmpTableName = WFUrgerManager.getTmpTableName();
//        ArrayList wftypes = _wftypes_Temp;
//        String tmpTableName = _tmpTableName_Temp;
//        if(wftypes == null){
//            //性能优化，直接调用计数方法


//            wftypes= WFUrgerManager.getWorkflowTreeCount();
//            tmpTableName = WFUrgerManager.getTmpTableName();
//        }
//        
//        
//        String requestids = "";
//        String requestSql = "";
//        Map mapRequestIDs = new HashMap();
//        for(int i=0;i<wftypes.size();i++){
//            WFWorkflowTypes wftype=(WFWorkflowTypes)wftypes.get(i);
//            ArrayList workflows=wftype.getWorkflows();
//
//            for(int j=0;j<workflows.size();j++){
//                //if(j>0) break;
//                WFWorkflows wfObj=(WFWorkflows)workflows.get(j);
//                String tempWorkflow=wfObj.getWorkflowid()+"";
//                //查询的流程ID列表为空，或者流程ID列表包含
//                if("".equals(workflowid)) {
//                    /*ArrayList requests = null;
//
//                   
//                    requests=wfObj.getNewrequestids();
//                   
//                    
//                    for(int k=0;k<requests.size();k++){
//                        if(requestids.equals("")){
//                            requestids=(String)requests.get(k);
//                        }else{
//                            requestids+=","+requests.get(k);
//                        }
//                    }*/
//                    if(mapRequestIDs.containsKey(tempWorkflow)){
//                        continue;
//                    }
//                    mapRequestIDs.put(tempWorkflow, "");
//                    if(requestids == ""){
//                        requestids += tempWorkflow;
//                    }else{
//                        requestids += ","+tempWorkflow;
//                    }
//                }
//            }
//        }
//        return requestids;
    }
    
    
    /**
     * 监控流程状态摘要



     */
  //处理直接放进/mobile/plugin/1/monitor/monitordtform.jsp中


    /*public List getMonitoRequesttabList(int requestid) {
       
       RecordSet rs = new RecordSet();
       RecordSet rs2 = new RecordSet();
       Map<String, String> map = new HashMap<String, String>();
       Map<String, String> map2 = new HashMap<String, String>();
       String select = " select distinct ";
       
       //String fields = " t1.requestid,t1.requestname,t3.workflowname,t1.creater,t1.createtime,t1.currentnodeid,t4.nodename,t2.userid ";
       String fields = " t1.status,t1.requestid,t1.requestname,t3.workflowname,t1.creater,t1.createtime,t1.currentnodeid,t4.nodename,t2.userid ";
       String from = " from workflow_requestbase t1,workflow_currentoperator t2,workflow_base t3,workflow_nodebase t4 ";
       String where = "where t1.requestid = t2.requestid and t1.workflowid = t3.id and t4.id = t1.currentnodeid ";
       where += "and  t1.requestid =  " + requestid;
       //String where2 = "and  t2.isremark not in ('2','4') ";
       
       //修改暂停后未操作者依然可以显示的问题
       //String where2 = "and  (t2.isremark not in ('2','4') or (t2.isremark='4' and t2.viewtype=0)) ";
       String where2 = "and  (t2.isremark in ('0','1','5','7','8','9') or (t2.isremark='4' and t2.viewtype=0)) ";
       String sql2 = select + fields +from +where + where2 ;
       String sql = select + fields +from +where ;
       List wris = new ArrayList();
       rs.execute(sql);
       if(rs.next()){
           
       String requestID = rs.getString("requestid");
       String requestname = rs.getString("requestname");
       String workflowname = rs.getString("workflowname");
       String creater = rs.getString("creater");
       String creatername = "";

       String createtime = rs.getString("createtime");
       String currentnodeid = rs.getString("currentnodeid");
       String nodename = rs.getString("nodename");

       String status = rs.getString("status");
       //String userid = rs.getString("userid");
       rs2.execute("select lastname from hrmresource where id =" +creater);
       if(rs2.next()){
           creatername = rs2.getString("lastname");
       }
       map.put("流程ID",requestID);
       map.put("流程标题",requestname);
       map.put("所属路径",workflowname);
       map.put("创建人",creatername);
       map.put("创建时间",createtime);
       map.put("当前节点",nodename);
       //map.put("当前状况",nodename);
       map.put("当前状况",status);
       //map.put("nodename",nodename);
       
       }
       wris.add(map);
       rs.execute(sql2);
       
       
//       while(rs.next()){
//           String userid = rs.getString("userid"); 
//           String username = "";
//           rs2.execute("select lastname from hrmresource where id =" +userid);
//           if(rs2.next()){
//               username = rs2.getString("lastname");
//           }
//           map2 = new HashMap<String, String>();
//           map2.put("当前未操作人",username);
//           wris.add(map2);
//       }
       

       ResourceComInfo rc = null;
       CustomerInfoComInfo cci = null;
        try {
            rc = new ResourceComInfo();
            cci = new CustomerInfoComInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
       if(rs.next()){
           String returnStr = "";
           String userid = rs.getString("userid"); 
           String username = "";
           rs.executeSql("select distinct userid,usertype,agenttype,agentorbyagentid from workflow_currentoperator where (isremark in ('0','1','5','7','8','9') or (isremark='4' and viewtype=0))  and requestid = " + requestid);
           
           while(rs.next()){
               if(rs.getInt("usertype")==0){
                   if(rs.getInt("agenttype")==2)
                       returnStr =  rc.getResourcename(rs.getString("agentorbyagentid"))+"->"+rc.getResourcename(rs.getString("userid"));
                   else
                       returnStr =  rc.getResourcename(rs.getString("userid"));
               }else{
                   returnStr =  cci.getCustomerInfoname(rs.getString("userid"));
               }
               map2 = new HashMap<String, String>();
               map2.put("当前未操作人",returnStr);
               wris.add(map2);
           }
       }
       
      // WorkflowRequestInfo[] wriarrays = new WorkflowRequestInfo[wris.size()];
      
       return wris; 
       
       //return getWorkflowRequestList(sql, userid);
        
        
    }*/

}
