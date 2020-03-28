/**
 * 
 */
package weaver.mobile.plugin.ecology.service;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipInputStream;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import weaver.blog.webservices.BlogService;
import weaver.blog.webservices.BlogServiceImpl;
import weaver.conn.BatchRecordSet;
import weaver.conn.RecordSet;
import weaver.docs.category.DocTreeDocFieldComInfo;
import weaver.docs.category.SecCategoryComInfo;
import weaver.docs.docs.DocComInfo;
import weaver.docs.news.DocNewsComInfo;
import weaver.file.FileUpload;
import weaver.file.Prop;
import weaver.general.GCONST;
import weaver.general.TimeUtil;
import weaver.general.Util;
import weaver.general.WorkFlowTransMethod;
import weaver.hrm.HrmUserVarify;
import weaver.hrm.User;
import weaver.hrm.company.DepartmentComInfo;
import weaver.hrm.company.SubCompanyComInfo;
import weaver.hrm.report.schedulediff.HrmScheduleDiffUtil;
import weaver.hrm.resource.ResourceComInfo;
import weaver.hrm.roles.RolesComInfo;
import weaver.hrm.schedule.HrmScheduleSignManager;
import weaver.mobile.webservices.workflow.WorkflowExtInfo;
import weaver.mobile.webservices.workflow.WorkflowRequestInfo;
import weaver.mobile.webservices.workflow.WorkflowService;
import weaver.mobile.webservices.workflow.WorkflowServiceImpl;
import weaver.systeminfo.SystemEnv;
import weaver.workflow.request.WFUrgerManager;

/**
 * @author donnie
 */
public class PluginServiceImpl {
	private static final double EARTH_RADIUS = 6378137;//地球半径
	
	private static Log logger = LogFactory.getLog(PluginServiceImpl.class);
	
	static {
		try {
			Protocol.registerProtocol("https", new Protocol("https", new EasySSLProtocolSocketFactory(), 443));
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	private static Map<Integer, Map<String, Object>> WorkCenterClassMap;

	private static final long serialVersionUID = 1912105668880694883L;
	
	DocumentService ds = new DocumentService();
	
	HrmResourceService hrs = new HrmResourceService();
	
	WorkflowServiceImpl ws = new WorkflowServiceImpl();
	
	ScheduleService ss = new ScheduleService();
	
	MeetingService ms = new MeetingService();

	WorkBlogService wbs = new WorkBlogService();
	
	BlogService bs = new BlogServiceImpl();
	
	AuthService as = new AuthService();
	
	UserService us = new UserService();
	
	public Map login(String loginId,String password,String secrect,String loginTokenFromThird,String dynapass,String tokenpass,String language,String ipaddress,int policy,List auths) throws Exception {
		return as.login(loginId, password,secrect, loginTokenFromThird,dynapass, tokenpass, language, ipaddress, policy, auths);
	}
	
	public Map login(String loginId,String password,String dynapass,String tokenpass,String language,String ipaddress,int policy,List auths) throws Exception {
		return as.login(loginId, password,dynapass, tokenpass, language, ipaddress, policy, auths);
	}

	public Map login(String id,String language,String ipaddress) throws Exception {
		return as.login(id, language, ipaddress);
	}
	
	public Map adminLogin(String loginid,String password,String ipaddress) throws Exception {
		return as.adminLogin(loginid, password, ipaddress);
	}

	public boolean verify(String sessionKey) throws Exception {
		return as.verify(sessionKey);
	}
	
	public boolean verify(String loginID,String password) throws Exception {
		return as.verify(loginID,password);
	}
	
	public Map getCurrUser(String sessionKey) throws Exception {
		return us.getCurrUser(sessionKey);
	}
	
	public Map getUser(String userId, String sessionKey) throws Exception {
		return us.getUser(userId, sessionKey);
	}
	
	public Map getUserList(List conditions, int pageIndex, int pageSize, int hrmorder, String sessionKey) throws Exception {
		return us.getUserList(conditions, pageIndex, pageSize, hrmorder, sessionKey);
	}
	
	public Map getUserCount(List conditions, String sessionKey) throws Exception {
		return us.getUserCount(conditions, sessionKey);
	}
	
	public Map getDocumentList(List conditions, int pageIndex, int pageSize, String sessionKey) throws Exception {
		User user = as.getCurrUser(sessionKey);
		if(user==null) {
			Map result = new HashMap();
			//未登录或登录超时
			result.put("error", "200001");
			return result;
		}
		return ds.getDocumentList(conditions, pageIndex, pageSize, user); 
	}

	public Map getDocumentCount(List conditions,String sessionKey) throws Exception {
		User user = as.getCurrUser(sessionKey);
		if(user==null) {
			Map result = new HashMap();
			//未登录或登录超时
			result.put("error", "200001");
			return result;
		}
		return ds.getDocumentCount(conditions, user);
	}

	public Map<String, String> getModuleScopeSetting(int module,int scope) throws Exception {
		Map<String, String> result =  new HashMap<String, String>();
		RecordSet rs = new RecordSet();
		rs.executeSql("select fields, value from MobileSetting where module=" + module + " and scope=" + scope + " and fields<>'workflowid'");
		while(rs.next()){
			result.put(rs.getString("fields"),rs.getString("value"));
		}
		return result;
	}
	
	public Map getWorkflowList(int module,int scope, List conditions, int pageIndex, int pageSize, String sessionKey) throws Exception {
	    return getWorkflowList(module, scope, conditions, pageIndex, pageSize, sessionKey, null);
	}
	
	public Map getWorkflowList(int module,int scope, List conditions, int pageIndex, int pageSize, String sessionKey, Map<String, String> otherParams) throws Exception {
		User user = as.getCurrUser(sessionKey);
		if(user==null) {
			Map result = new HashMap();
			//未登录或登录超时
			result.put("error", "200001");
			return result;
		}
		
		ResourceComInfo rci = new ResourceComInfo();

		Map result = new HashMap();
		List list = new ArrayList();
		int count = 0;
		int pageCount = 0;
		int isHavePre = 0;
		int isHaveNext = 0;

		String listwfids = "";
		//待办是否包含抄送
		boolean isshowcopy = false;
		//已办是否包含办结
		boolean isshowprocessed = false;
		//是否需要获取所有的流程类型id
		boolean isneedgetwfids = false;
		//是否需要流程倒序排序
		int order = 0;
		//是否是浏览框
		boolean isbrow = false;
		
		List<String> listtypeidconditionlist = new ArrayList<String>();
		
		if (otherParams != null) {
		    //isshowcopy = Util.getIntValue(Util.null2String(otherParams.get("isshowcopy")), 0) == 1;
		    //isshowprocessed = Util.getIntValue(Util.null2String(otherParams.get("isshowprocessed")), 0) == 1;
		    isneedgetwfids = Util.getIntValue(Util.null2String(otherParams.get("isneedgetwfids")), 0) == 1;
		    //order = Util.getIntValue(Util.null2String(otherParams.get("order")), 0);
		    listtypeidconditionlist.add(Util.null2String(otherParams.get("listtypeidcondition")));
		    isbrow = "1".equals((String)otherParams.get("isbrow"));
		}
		
		Map<String, String> setting = getModuleScopeSetting(module, scope);
		order = Util.getIntValue(Util.null2String(setting.get("orasc")), 0);
		isshowcopy = Util.getIntValue(setting.get("include"), 0) == 1;
		isshowprocessed = Util.getIntValue(setting.get("include"), 0) == 1;
		
		
		String[] listtypeconditions = new String[listtypeidconditionlist.size()];
		listtypeidconditionlist.toArray(listtypeconditions);
		
		String[] strconditions = new String[conditions.size()];
		conditions.toArray(strconditions);

	    //20151201 多账号对应 Start
		String belongtoshow = user.getBelongtoshowByUserId(user.getUID());
		boolean belongtoshowFlag = false;
		if("1".equals(belongtoshow)){
		    belongtoshowFlag = true;
		}
        //20151201 多账号对应 End

		WorkflowRequestInfo[] wris = null;
		if(module==1) {
			count = ws.getToDoWorkflowRequestCount(user.getUID(), isshowcopy, strconditions,belongtoshowFlag);
			wris = ws.getToDoWorkflowRequestList(isshowcopy, pageIndex, pageSize, count, user.getUID(), strconditions, order,belongtoshowFlag);
			if (isneedgetwfids) {
			    listwfids = ws.getToDoWorkflowRequestTypeIds(user.getUID(), isshowcopy, listtypeconditions,belongtoshowFlag);
			}
		} else if(module==7) {
			count = ws.getProcessedWorkflowRequestCount(user.getUID(), strconditions,belongtoshowFlag);
			wris = ws.getProcessedWorkflowRequestList(pageIndex, pageSize, count, user.getUID(), strconditions, order,belongtoshowFlag);
			if (isneedgetwfids) {
                listwfids = ws.getProcessedWorkflowRequestTypeIds(user.getUID(), listtypeconditions,belongtoshowFlag);
            }
		} else if(module==8) {
			count = ws.getHendledWorkflowRequestCount(user.getUID(), isshowprocessed, strconditions,belongtoshowFlag);
			wris = ws.getHendledWorkflowRequestList(isshowprocessed, pageIndex, pageSize, count, user.getUID(), strconditions, order, belongtoshowFlag);
			if (isneedgetwfids) {
                listwfids = ws.getHendledWorkflowRequestTypeIds(user.getUID(), isshowprocessed, listtypeconditions,belongtoshowFlag);
            }
		} else if(module==9) {
			count = ws.getMyWorkflowRequestCount(user.getUID(), strconditions,belongtoshowFlag);
			wris = ws.getMyWorkflowRequestList(pageIndex, pageSize, count, user.getUID(), strconditions, order,belongtoshowFlag);
			if (isneedgetwfids) {
                listwfids = ws.getMyWorkflowRequestTypeIds(user.getUID(), listtypeconditions,belongtoshowFlag);
            }
		} else if(module==10) {
			count = ws.getCCWorkflowRequestCount(user.getUID(), strconditions,belongtoshowFlag);
			wris = ws.getCCWorkflowRequestList(pageIndex, pageSize, count, user.getUID(), strconditions, order,belongtoshowFlag);
			if (isneedgetwfids) {
                listwfids = ws.getCCWorkflowRequestTypeIds(user.getUID(), listtypeconditions,belongtoshowFlag);
            }
		} else if(module == -9) {

		    count = ws.getWorkflowRequestCount(user.getUID(), strconditions,belongtoshowFlag);
            wris = ws.getWorkflowRequestList(pageIndex, pageSize, count, user.getUID(), strconditions,belongtoshowFlag);
            if (isneedgetwfids) {
                listwfids = ws.getWorkflowRequestTypeIds(user.getUID(), listtypeconditions,belongtoshowFlag);
            }
        } else if(module==-1005) { //监控
            count = ws.getMonitorWorkflowRequestCount(user.getUID(), strconditions);
            wris = ws.getMonitorWorkflowRequestList( pageIndex, pageSize, count, user.getUID(), strconditions, order);
            if (isneedgetwfids) {
                listwfids = ws.getMonitorWorkflowRequestTypeIds(user.getUID(), listtypeconditions);
            }
        } else if(module==-1004) { //督办

            //提高效率，只调用一次
            //ArrayList _wftypes_Temp;
            //String _tmpTableName_Temp;
            //WFUrgerManager WFUrgerManager = new WFUrgerManager();

            //WFUrgerManager.setLogintype(Util.getIntValue(user.getLogintype()));
            //WFUrgerManager.setUserid(user.getUID());
            //_wftypes_Temp = WFUrgerManager.getWorkflowTreeCount();
            //_tmpTableName_Temp = WFUrgerManager.getTmpTableName();
            //int _wftotalCount = WFUrgerManager.getTotalcounts();
            //ws.set_wftypes_Temp(_wftypes_Temp);
            //ws.set_tmpTableName_Temp(_tmpTableName_Temp);
            //ws.set_wftotalCount(_wftotalCount);
            //count = ws.getSuperviseWorkflowRequestCount(user.getUID(), strconditions);

            count = pageIndex * pageSize + 1;
            wris = ws.getSuperviseWorkflowRequestList( pageIndex, pageSize, count, user.getUID(), strconditions, order);
            if(wris.length < pageSize){
                count = (pageIndex - 1) * pageSize + wris.length;
            }
            if (isneedgetwfids) {
                listwfids = ws.getSuperviseWorkflowRequestTypeIds(user.getUID(), listtypeconditions);
            }
        }
		
		if (count <= 0) pageCount = 0;
		pageCount = count / pageSize + ((count % pageSize > 0)?1:0);
		
		isHaveNext = (pageIndex + 1 <= pageCount)?1:0;

		isHavePre = (pageIndex - 1 >= 1)?1:0;
		

		//{"createtime":"2012-02-23 17:11:32","implev":"0","status":"","recivetime":"2012-02-23 17:11:32","wfid":"17928",
		//"creatorpic":"/messager/usericon/loginid20100126102914_wev8.jpg","wftype":"周工作小结与计划","wftitle":"周工作小结与计划-杨文元-2012-02-23","isnew":"0","creator":"杨文元"}

		List<WorkflowRequestInfo> datas = Arrays.asList(wris);
		String[] requestids = new String[datas.size()];
		//主次账号统一显示时，需要当前节点操作者的用户id
		String[] userids = new String[datas.size()];
		int i=0;
		for(WorkflowRequestInfo wri:datas) {
			requestids[i] = wri.getRequestId();
			//当前节点操作者
			userids[i] = Util.null2String(wri.getWorkflowBaseInfo().getF_weaver_belongto_userid(),user.getUID() + "");
			i++;
			
			Map<String, String> wf = new HashMap<String, String>();
			wf.put("wfid", Util.null2String(wri.getRequestId()));
			wf.put("requestNameNew", Util.null2String(wri.getRequestNameNew()));
			wf.put("wftitle", Util.null2String(wri.getRequestName()));
			wf.put("wftype", Util.null2String(wri.getWorkflowBaseInfo().getWorkflowName()));
			wf.put("creator", Util.null2String(wri.getCreatorName()));
			wf.put("createtime", Util.null2String(wri.getCreateTime()));
			wf.put("recivetime", Util.null2String(wri.getReceiveTime()));
			wf.put("implev", Util.null2String(wri.getRequestLevel()));
			wf.put("status", Util.null2String(wri.getStatus()));
			wf.put("creatorpic", Util.null2String(rci.getMessagerUrls(wri.getCreatorId())));
			//增加创建人ID
			wf.put("creatorid", Util.null2String(wri.getCreatorId()));
			//当前节点
			wf.put("currentnodeid", Util.null2String(wri.getCurrentNodeId()));
			wf.put("currentnodename", Util.null2String(wri.getCurrentNodeName()));

            //当主次账号统一显示的时候,获取用户名和用户类型
            if(wri.getWorkflowBaseInfo() != null){
                WorkflowExtInfo workflowBaseInfo = wri.getWorkflowBaseInfo();
                wf.put("f_weaver_belongto_userid", Util.null2String(workflowBaseInfo.getF_weaver_belongto_userid()));
                wf.put("f_weaver_belongto_usertype", Util.null2String(workflowBaseInfo.getF_weaver_belongto_usertype()));
            }
            
			
			if (!isbrow && module!=-1004 && module!=-1005) {//监控模块不做考虑
    			//是否可以批量提交
    			WorkFlowTransMethod wtm = new WorkFlowTransMethod();
    			String canMultiSubmit = wtm.getWFSearchResultCheckBox(wri.getWorkflowBaseInfo().getWorkflowId() + "+" + wri.getIsremark() + "+" + wri.getRequestId() + "+" + wri.getNodeId() + "+" + user.getUID());
    			wf.put("canMultiSubmit", canMultiSubmit);
    			
    			String formsignaturemd5 = wri.getFormsignaturemd5();
    			if (!"".equals(formsignaturemd5) && (module == 1 || module == 10)) {
    			    formsignaturemd5 = Util.getEncrypt(formsignaturemd5 + ",currentnodeid:" + wri.getNodeId());
    			}
    			//添加标签签名信息
    			wf.put("formsignaturemd5", Util.null2String(formsignaturemd5));
			}
			list.add(wf);
		}
		
		if (!isbrow) {
    		if (requestids.length > 0) {
    		    //按照requestid列表和用户id列表取得是否新到流程
    			//String[] newflagarray = ws.getWorkflowNewFlag(requestids, user.getUID()+"");
    		    String[] newflagarray = ws.getWorkflowNewFlagByList(requestids, userids);
    		    
    			if(newflagarray!=null&&newflagarray.length==requestids.length){
    				for(i=0;i<requestids.length;i++){
    					((Map) list.get(i)).put("isnew", newflagarray[i]);
    				}
    			}
    		}
    		RecordSet rs = new RecordSet();
    		int multisubmitnotinputsign = 0;
    		rs.executeSql("select multisubmitnotinputsign from workflow_RequestUserDefault where userId = "+ user.getUID());
            if(rs.next()){
                multisubmitnotinputsign = Util.getIntValue(Util.null2String(rs.getString("multisubmitnotinputsign")), 0);
            }
    		//批量提交是否需要提交签字意见
            result.put("isneedremark", multisubmitnotinputsign == 1 ? 0 : 1);
		}
		
		result.put("result", "list");
		result.put("pagesize",pageSize+"");
		result.put("pageindex",pageIndex+"");
		result.put("count",count+"");
		result.put("pagecount",pageCount+"");
		result.put("ishavepre",isHavePre+"");
		result.put("ishavenext",isHaveNext+"");
		result.put("list",list);
		
		result.put("listtypes", listwfids);
		
		return result;
	}
	
	public Map getWorkflowCount(int module,int scope,List conditions,String sessionKey) throws Exception {
		User user = as.getCurrUser(sessionKey);
		if(user==null) {
			Map result = new HashMap();
			//未登录或登录超时
			result.put("error", "200001");
			return result;
		}

		Map result = new HashMap();
		
		result.put("result", "count");

		int count1 = 0;
		int count2 = 0;

		Map<String, String> setting = getModuleScopeSetting(module, scope);
        //待办是否包含抄送/已办是否包含办结
        boolean isinclude = Util.getIntValue(setting.get("include"), 0) == 1;;
		
		String[] strconditions = new String[conditions.size()];
		conditions.toArray(strconditions);
		
		if(module==1) {
			count1 = ws.getToDoWorkflowRequestCount(user.getUID(), isinclude, strconditions);
		} else if(module==7) {
			count1 = ws.getProcessedWorkflowRequestCount(user.getUID(), strconditions);
		} else if(module==8) {
			count1 = ws.getHendledWorkflowRequestCount(user.getUID(), isinclude, strconditions);
		} else if(module==9) {
			count1 = ws.getMyWorkflowRequestCount(user.getUID(), strconditions);
		} else if(module==10) {
			count1 = ws.getCCWorkflowRequestCount(user.getUID(), strconditions);
		}
		
		result.put("count",count1+"");

		conditions.add(" (t2.viewtype = 0) ");
		
		strconditions = new String[conditions.size()];
		conditions.toArray(strconditions);
		
		if(module==1) {
			count2 = ws.getToDoWorkflowRequestCount(user.getUID(), isinclude, strconditions);
		} else if(module==7) {
			count2 = ws.getProcessedWorkflowRequestCount(user.getUID(), strconditions);
		} else if(module==8) {
			count2 = ws.getHendledWorkflowRequestCount(user.getUID(), isinclude, strconditions);
		} else if(module==9) {
			count2 = ws.getMyWorkflowRequestCount(user.getUID(), strconditions);
		} else if(module==10) {
			count2 = ws.getCCWorkflowRequestCount(user.getUID(), strconditions);
		}
		
		result.put("unread",count2+"");
		
		return result;
	}
	

	public Map getAttachment(String url,int type,String sessionKey) throws Exception {
		String filepath = "";
		String iszip = "";
		String filename = "";
		String contenttype = "";
		byte[] content = null;
		
		if(type==1) {
			if(Util.getIntValue(url)>0) {
			
				RecordSet rs = new RecordSet();
				
				String sql = "select imagefilename,imagefiletype,filerealpath,iszip from imagefile where imagefileid = " + url;
				
				rs.executeSql(sql);
				
				if(rs.next()) {
					
					filepath = rs.getString("filerealpath");
					iszip = rs.getString("iszip");
					filename = rs.getString("imagefilename");
					contenttype = rs.getString("imagefiletype");
				
				}
			}
		} else if(type==2) {
		
			filepath = url;
			iszip = "0";
			filename = filepath.substring(filepath.lastIndexOf("/")+1);
			contenttype = "application/octet-stream";
			
		}
		
		File file = new File(filepath);
		
		if(file.exists()) {
			InputStream is = null;
			if(Util.getIntValue(iszip)>0) {
				ZipInputStream zin = new ZipInputStream(new FileInputStream(file));
				if (zin.getNextEntry() != null)
					is = new BufferedInputStream(zin);
			} else {
				is = new BufferedInputStream(new FileInputStream(file));
			}

			byte[] rbs = new byte[2048];
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int len = 0;
			while (((len = is.read(rbs)) > 0)) {
				out.write(rbs, 0, len);
			}
			content = out.toByteArray();
			is.close();
			out.close();
			
			Map result = new HashMap();
			result.put("filename", filename);
			result.put("data", content);
			result.put("contenttype", contenttype);
			
			return result;
		}
		
		return null;
	}

	

	
	public Map getDocumentPage(String mobileSession, String page, String module, String scope, String detailid, String fromid, String url,String sessionKey) throws Exception {

		User user = as.getCurrUser(sessionKey);
		if(user==null) {
			Map result = new HashMap();
			//未登录或登录超时
			result.put("error", "200001");
			return result;
		}
		
		return this.getPage(mobileSession, page, "", module, scope, detailid, fromid, url, new HashMap(), sessionKey);
		
	}
	
	public Map postDocumentPage(String mobileSession, String page, String module, String scope, String detailid, String fromid, String url,Map form,String sessionKey) throws Exception {
		
		User user = as.getCurrUser(sessionKey);
		if(user==null) {
			Map result = new HashMap();
			//未登录或登录超时
			result.put("error", "200001");
			return result;
		}
		
		return this.postPage(mobileSession, page, module, scope, detailid, fromid, url, form, sessionKey);
		
	}
	
	
	public Map getUserPage(String mobileSession, String page, String module, String scope, String detailid, String fromid, String url,Map params,String sessionKey) throws Exception {

		User user = as.getCurrUser(sessionKey);
		if(user==null) {
			Map result = new HashMap();
			//未登录或登录超时
			result.put("error", "200001");
			return result;
		}
		
		return this.getPage(mobileSession, page, "", module, scope, detailid, fromid, url, params, sessionKey);
		
	}


	/**
	 * 取得服务器具体页面
	 * 
	 * 首先会先调用PluginViewServlet的get方法进行ecology系统页面的用户授权
	 * 再由PluginViewServlet转发至所指定的相应页面，由page或path决定页面地址
	 * 
	 * @param mobileSession Mobile服务器上的SessionKey，用于Ajax回传参数时Mobile用户授权
	 * @param page 指定具体某个jsp页面，page所指定的jsp只能在ecology的mobile/plugin/相应的模块code/xxx.jsp?....
	 * @param path 指定具体路径，如果设定了path，则直接转向path所指定的路径
	 * @param scope 模块id
	 * @param module 模块code
	 * @param detailid 由列表传回的具体数据id
	 * @param fromid 由页面跳转过来的相关数据id,如相关流程、相关文档等
	 * @param url PluginViewServlet的地址
	 * @param param 其他参数，将会拼接在url后
	 * @param sessionKey ecology系统上的授权SessionKey
	 * @return
	 */
	private Map getPage(String mobileSession, String page, String path, String module, String scope,  String detailid, String fromid, String url,Map param,String sessionKey) {
		Map result = new HashMap();
		try {
			
			String requestURL = url+"?"+"sessionkey"+"="+sessionKey+"&"+"mobileSession"+"="+mobileSession+"&"+"page"+"="+page+"&"+"scope"+"="+scope+"&"+"module"+"="+module+"&"+"detailid"+"="+detailid+"&"+"fromid"+"="+fromid;
			if(param != null) {
				for(Iterator it=param.keySet().iterator();it.hasNext();){
					String key = (String) it.next();
					String value = (String) param.get(key);
					requestURL += "&" + key + "=" + value;
				}
			}
		
	        HttpClient httpClient = new HttpClient();  
	          
	        GetMethod method = new GetMethod(requestURL);  
	          
	        httpClient.executeMethod(method);
	          
	        String response = method.getResponseBodyAsString();  
	        
	        method.releaseConnection();
			
	        String html="";
	        String dataType="";
	        if(param!=null) dataType = Util.null2String((String)param.get("dataType")); //返回数据类型 dataType=json 为返回json格式字符串
	        if(dataType.equals("json")){
	        	//html=getBlogJson(param).toString();
	        }else{
                Document doc = Jsoup.parse(response);
			    Element head = doc.head();
			    Elements metas = head.select("meta");
			    for (Element meta : metas) {
			        String content = meta.attr("content");
			        if ("content-type".equalsIgnoreCase(meta.attr("http-equiv")) && content.startsWith("text/html")) {
			        	meta.attr("content", "text/html; charset=UTF-8");
			        }
			    }
			    html = doc.html();
	        }
	        result.put("result", "html");
	        result.put("html", html);
	    
		} catch(Exception e) {
			e.printStackTrace();
		}

		return result;
	}
	
	/**
	 * 提交页面
	 * 
	 * 首先会先调用PluginViewServlet的post方法进行ecology系统页面的用户授权
	 * 再由PluginViewServlet转发至所指定的相应页面，由page决定提交的页面地址，可上传附件
	 * 
	 * @param mobileSession Mobile服务器上的SessionKey，用于Ajax回传参数时Mobile用户授权
	 * @param page 指定具体某个jsp页面，page所指定的jsp只能在ecology的mobile/plugin/相应的模块code/xxx.jsp?....
	 * @param scope 模块id
	 * @param module 模块code
	 * @param detailid 由列表传回的具体数据id
	 * @param fromid 由页面跳转过来的相关数据id,如相关流程、相关文档等
	 * @param url PluginViewServlet的地址
	 * @param form 表单所提交的所有内容，分为附件和传参
	 * @param sessionKey ecology系统上的授权SessionKey
	 * @return
	 */
	private Map postPage(String mobileSession, String page, String module, String scope, String detailid, String fromid, String url,Map form,String sessionKey) {
		
		Map result = new HashMap();
		
		try {
			
	        HttpClient httpClient = new HttpClient();
			
			PostMethod method = new PostMethod(url+"?"+"sessionkey"+"="+sessionKey+"&"+"mobilesession"+"="+mobileSession+"&"+"page"+"="+page+"&"+"scope"+"="+scope+"&"+"module"+"="+module+"&"+"detailid"+"="+detailid+"&"+"fromid"+"="+fromid){
	            public String getRequestCharSet() {
                    return "UTF-8";
                }     
			};
			
			Map attachment = (Map) form.get("attachment");
			Map parameters = (Map) form.get("parameters");

			List parts = new ArrayList();
			
			for(Iterator it=attachment.keySet().iterator();it.hasNext();) {
				String key = (String) it.next();
				Map map = (Map) attachment.get(key);
				for(Iterator it2=map.keySet().iterator();it2.hasNext();) {
					String name = (String) it2.next();
					byte[] content = (byte[]) map.get(name);
					Part filepart = new CustomFilePart(key,new ByteArrayPartSource(name,content));
					parts.add(filepart);
				}
			}
			
			for(Iterator it=parameters.keySet().iterator();it.hasNext();) {
				String key = (String) it.next();
				
				if(parameters.get(key) instanceof Object[]) {
					Object[] vos = (Object[]) parameters.get(key);
					for(Object vo:vos) {
						Part stringpart = new StringPart(key,vo.toString(),"UTF-8");
						parts.add(stringpart);
					}
					
				} else if(parameters.get(key) instanceof Object) {
					Object vo = (Object) parameters.get(key);
					Part stringpart = new StringPart(key,vo.toString(),"UTF-8");
					parts.add(stringpart);
				}
			}

			Part[] partarray = (Part []) parts.toArray(new Part[parts.size()]);  
            method.setRequestEntity(new MultipartRequestEntity(partarray, method.getParams()));
	        
	        httpClient.executeMethod(method);
	          
	        String response = method.getResponseBodyAsString();  
	        //String response = new String(method.getResponseBodyAsString().getBytes("ISO-8859-1"));
	        
	        method.releaseConnection();  
	        String html="";
	        String dataType=Util.null2String((String)form.get("dataType")); //返回数据类型 dataType=json 为返回json格式字符串
	        if(dataType.equals("json")){
	        	html=response;
	        }else {
	        	Document doc = Jsoup.parse(response);
			    
			    Element head = doc.head();  
			    Elements metas = head.select("meta");  
			    for (Element meta : metas) {  
			        String content = meta.attr("content");  
			        if ("content-type".equalsIgnoreCase(meta.attr("http-equiv")) && content.startsWith("text/html")) {  
			        	meta.attr("content", "text/html; charset=UTF-8");
			        }  
			    }  
			    
			    //String html = new String(doc.html().getBytes("UTF-8"), "UTF-8");
			    html = doc.html();
			}
	        
	        result.put("result", "html");
	        result.put("html", html);

		} catch(Exception e) {
			e.printStackTrace();
		}

		return result;
		
	}
	
	class CustomFilePart extends FilePart {     
	    public CustomFilePart(String filename, File file) throws FileNotFoundException {
	        super(filename, file);
	    }
	    
	    public CustomFilePart(String filename, PartSource ps) throws FileNotFoundException {
	        super(filename, ps);
	    }     
	    
	    protected void sendDispositionHeader(OutputStream out) throws IOException {     
	        super.sendDispositionHeader(out);     
	        String filename = getSource().getFileName();     
	        if (filename != null) {     
	            out.write(EncodingUtil.getAsciiBytes(FILE_NAME));     
	            out.write(QUOTE_BYTES);     
	            out.write(EncodingUtil.getBytes(filename, "utf-8"));     
	            out.write(QUOTE_BYTES);     
	        }     
	    }     
	}

	public Map getWorkflowPage(String mobileSession, String page,
			String module, String scope, String detailid, String fromid,
			String url, Map param, String sessionKey) throws Exception {
		
		User user = as.getCurrUser(sessionKey);
		if(user==null) {
			Map result = new HashMap();
			//未登录或登录超时
			result.put("error", "200001");
			return result;
		}
		
		return this.getPage(mobileSession, page, null, module, scope, detailid, fromid, url, param, sessionKey);
	}

	public Map postWorkflowPage(String mobileSession, String page,
			String module, String scope, String detailid, String fromid,
			String url, Map form, String sessionKey) throws Exception {
		User user = as.getCurrUser(sessionKey);
		if(user==null) {
			Map result = new HashMap();
			//未登录或登录超时
			result.put("error", "200001");
			return result;
		}
		
		return this.postPage(mobileSession, page, module, scope, detailid, fromid, url, form, sessionKey);
	}

	public Map getScheduleCount(List conditions, String sessionKey) throws Exception {
		User user = as.getCurrUser(sessionKey);
		if(user==null) {
			Map result = new HashMap();
			//未登录或登录超时
			result.put("error", "200001");
			return result;
		}
		return ss.getScheduleCount(conditions, user);
	}

	public Map getScheduleList(List conditions, int pageIndex, int pageSize, String sessionKey) throws Exception {
		User user = as.getCurrUser(sessionKey);
		if(user==null) {
			Map result = new HashMap();
			//未登录或登录超时
			result.put("error", "200001");
			return result;
		}
		return ss.getScheduleList(conditions, pageIndex, pageSize, user);
	}

	public Map getSchedulePage(String mobileSession,String page,String module,String scope,String url,String sessionKey,Map param) throws Exception {
		User user = as.getCurrUser(sessionKey);
		if(user==null) {
			Map result = new HashMap();
			//未登录或登录超时
			result.put("error", "200001");
			return result;
		}
		
		return this.getPage(mobileSession, page, null, module, scope, "", "", url, param, sessionKey);
	}

	public Map postSchedulePage(String mobileSession, String page, String module, String component, String detailid, String fromid, String url, Map form, String sessionKey) throws Exception {
		User user = as.getCurrUser(sessionKey);
		if(user==null) {
			Map result = new HashMap();
			//未登录或登录超时
			result.put("error", "200001");
			return result;
		}
		
		
		return this.postPage(mobileSession, page, module, component, detailid, fromid, url, form, sessionKey);
	}

	public Map getMeetingCount(List conditions, String sessionKey) throws Exception {
		User user = as.getCurrUser(sessionKey);
		if(user==null) {
			Map result = new HashMap();
			//未登录或登录超时
			result.put("error", "200001");
			return result;
		}
		return ms.getMeetingCount(conditions, user);
	}

	public Map getMeetingList(List conditions, int pageIndex, int pageSize, String sessionKey) throws Exception {
		User user = as.getCurrUser(sessionKey);
		if(user==null) {
			Map result = new HashMap();
			//未登录或登录超时
			result.put("error", "200001");
			return result;
		}
		return ms.getMeetingList(conditions, pageIndex, pageSize, user);
	}

	public Map getMeetingPage(String mobileSession,String page,String module,String scope,String url,String sessionKey,Map param) throws Exception {
		User user = as.getCurrUser(sessionKey);
		if(user==null) {
			Map result = new HashMap();
			//未登录或登录超时
			result.put("error", "200001");
			return result;
		}
		
		return this.getPage(mobileSession, page, null, module, scope, "", "", url, param, sessionKey);
	}

	public Map postMeetingPage(String mobileSession, String page, String module, String component, String detailid, String fromid, String url, Map form, String sessionKey) throws Exception {
		User user = as.getCurrUser(sessionKey);
		if(user==null) {
			Map result = new HashMap();
			//未登录或登录超时
			result.put("error", "200001");
			return result;
		}
		
		return this.postPage(mobileSession, page, module, component, detailid, fromid, url, form, sessionKey);
	}    
	
	/**
	 * 获取同步用户数据
	 */
	public Map getSyncUser(String userIdentifiers){
		Map result=new HashMap();
		if(userIdentifiers==null) return null;
		if(userIdentifiers.equals("")) return result;
		
		String sql="select id,loginid,password,lastname,email,mobile,workcode from hrmresource where status in(0,1,2,3) and id in("+userIdentifiers+")";
		
		String userListStr="";
		RecordSet recordSet=new RecordSet(); 
		recordSet.execute(sql);
		while(recordSet.next()){
			String userid=recordSet.getString("id");
			String loginid=recordSet.getString("loginid"); 
			String password=recordSet.getString("password");
			String lastname=recordSet.getString("lastname");
			String email=recordSet.getString("email");
			String mobile=recordSet.getString("mobile");
			String workcode=recordSet.getString("workcode");
			
			userListStr=userListStr+",{userid:\""+userid+"\",loginid:\""+loginid+"\",password:\""+password+"\",name:\""+lastname+"\",email:\""+email+"\",mobile:\""+mobile+"\",workcode:\""+workcode+"\"}";
		}
		if(userListStr.length()>0)
			userListStr=userListStr.substring(1);
		userListStr="["+userListStr+"]";
		result.put("list", userListStr);
		return result;
	}
	
	/**
	    * 同步用户条件
	    * @param shareConditions
	    * @return
	    */
		public List syncUserAuth(List conditionList){
			try {
				ResourceComInfo rci = new ResourceComInfo();
				SubCompanyComInfo subCompanyComInfo=new SubCompanyComInfo();
				DepartmentComInfo departmentComInfo=new DepartmentComInfo();
				RolesComInfo rolesComInfo=new RolesComInfo();
				
				for(int i=0;i<conditionList.size();i++){
					Map map=(Map)conditionList.get(i);
					int authType = Util.getIntValue((String)map.get("type"),0);
					String authValue = Util.null2String((String)map.get("value"));
					String authValueName=Util.null2String((String)map.get("valuename"));
					String authStatus="1";
					
					if(authType==0){        //人员
						String status=rci.getStatus(authValue);
						if(!status.equals("0")&&!status.equals("1")&&!status.equals("2")&&!status.equals("3"))
							authStatus="0";
					} else if(authType==1){ //分部
						authValueName=subCompanyComInfo.getSubCompanyname(authValue);
						if("1".equals(subCompanyComInfo.getCompanyiscanceled(authValue))) authStatus="0";
					} else if(authType==2){ //部门
						authValueName=departmentComInfo.getDepartmentname(authValue);
						if("1".equals(departmentComInfo.getDeparmentcanceled(authValue))) authStatus="0";
					} else if(authType==3){ //角色
						authValue=authValue.length()>0?authValue.substring(0,authValue.length()-1):"";
						authValueName=rolesComInfo.getRolesname(authValue);
						if("".equals(authValueName)) authStatus="0";
					} else if(authType==4){ //所有人
						
					}
					map.put("valuename", authValueName);
					map.put("status", authStatus);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return conditionList;
		}
	
	public List syncUserInfo(String userIdentifiers){
		
		List userList=new ArrayList();
		String sql="select id,loginid,password,lastname,email,mobile,workcode,status from hrmresource where id in("+userIdentifiers+")";
		RecordSet recordSet=new RecordSet(); 
		recordSet.execute(sql);
		while(recordSet.next()){
			
			String id=recordSet.getString("id");
			String loginid=recordSet.getString("loginid"); 
			String loginpass=recordSet.getString("password");
			String name=recordSet.getString("lastname");
			String email=recordSet.getString("email");
			String mobile=recordSet.getString("mobile");
			String workcode=recordSet.getString("workcode");
			String status=recordSet.getString("status");
			if(status.equals("0")||status.equals("1")||status.equals("2")||status.equals("3"))
				status="1";
			else
				status="0";
			
			Map map=new HashMap();
			map.put("id", id);
			map.put("name", name);
			map.put("loginid", loginid);
			map.put("loginpass", loginpass);
			map.put("mobile", mobile);
			map.put("email", email);
			map.put("workcode", workcode);
			map.put("status", status);
			
			map.put("isadmin", "0");
			User user = hrs.getUserById(Util.getIntValue(id, 0));
			if(HrmUserVarify.checkUserRight("Mobile:Setting", user)) {
				map.put("isadmin", "1");
			}
			
			userList.add(map);
		}
			
		return userList;
	}
		
	/**
	 * 检查插件服务器访问状态
	 */
	public Map checkServerStatus(){
		Map result=new HashMap();
		result.put("status", true);
		result.put("syncSetting", ""+this.syncSettingCheck());
		return result;
	}
	
	private String syncSettingCheck() {
		RecordSet rs = new RecordSet();
		rs.executeSql("select value from MobileSetting where scope=0 and module=0");
		if (rs.next()) {
			return rs.getString("value");
		}
		return "-1";
	}
	
	public Map<String, Object> syncMobileSetting(String settingString, String timestamp) {
		Map result = new HashMap();
		RecordSet rs = new RecordSet();
		
		try {
			JSONArray jsonArray = JSONArray.fromObject(settingString);
			List settingList = JSONArray.toList(jsonArray, new HashMap(), new JsonConfig());
			
			rs.executeSql("delete from MobileSetting");
			rs.executeSql("insert into MobileSetting(scope,module,fields,value) values(0,0,'timestamp','"+timestamp+"')");
			
			if(settingList != null && !settingList.isEmpty()) {
				List<String> paraList = new ArrayList<String>();
				
				Iterator settingIter = settingList.iterator();
				while(settingIter.hasNext()) {
					Map settingMap = (Map)settingIter.next();
					int scope = NumberUtils.toInt((String)settingMap.get("scope"));
					int module = NumberUtils.toInt((String)settingMap.get("module"));
					String setting = (String)settingMap.get("setting");
					String modulename = (String)settingMap.get("modulename");
					String include = (String)settingMap.get("include");
					String orasc = (String)settingMap.get("orasc");
					if(module==1 || module==7 || module==8 || module==9 || module==10) {
						if(StringUtils.isNotBlank(setting)) {
							String[] workflowids = StringUtils.splitByWholeSeparator(setting, ",");
							if(workflowids != null && workflowids.length > 0) {
								for(int i=0; i<workflowids.length; i++) {
									//rs.executeSql("insert into MobileSetting(scope,module,value) values("+scope+","+module+",'"+workflowids[i]+"')");
									paraList.add(""+scope + Util.getSeparator() + module + Util.getSeparator() + "workflowid" + Util.getSeparator() + workflowids[i]);
								}
							}
						}else {
							//rs.executeSql("insert into MobileSetting(scope,module,value) values("+scope+","+module+",'0')");
							paraList.add(""+scope + Util.getSeparator() + module + Util.getSeparator() + "workflowid" + Util.getSeparator() + "0");
						}
						if(StringUtils.isNotBlank(include))
								paraList.add(""+scope + Util.getSeparator() + module + Util.getSeparator() + "include" + Util.getSeparator() + include);
						if(StringUtils.isNotBlank(orasc)) 
								paraList.add(""+scope + Util.getSeparator() + module + Util.getSeparator() + "orasc" + Util.getSeparator() + orasc);
					} else if(module==2 || module==3) {
						saveMobileDocSetting(scope, setting, modulename);
					} else {
						//rs.executeSql("insert into MobileSetting(scope,module,value) values("+scope+","+module+",'"+setting+"')");
						if(StringUtils.isBlank(setting)) setting = "0";
						paraList.add(""+scope + Util.getSeparator() + module + Util.getSeparator() + "setting" + Util.getSeparator() + setting);
					}
				}
				
				BatchRecordSet brs = new BatchRecordSet();
				brs.executeSqlBatch("insert into MobileSetting(scope,module,fields,value) values(?,?,?,?)", paraList);
			}
			
			result.put("success", "1");
		} catch (Exception e) {
			logger.error("", e);
			result.put("error", "system error");
		}
		
		return result;
	}
	
	private void saveMobileDocSetting(int scope, String settings, String modulename) throws Exception {
		if(scope <= 0 || StringUtils.isBlank(modulename)) return;
		
		RecordSet rs = new RecordSet();
		BatchRecordSet brs = new BatchRecordSet();
		
		if(StringUtils.isBlank(settings)) {
			rs.executeSql("delete from MobileDocColSetting where columnid in (select columnid from MobileDocSetting where scope="+scope+")");
			rs.executeSql("delete from MobileDocSetting where scope="+scope+"");
			rs.executeSql("insert into MobileDocSetting(scope,name,source,showOrder,isreplay) values("+scope+",'"+modulename+"',0,1,0)");
		} else {
			String columnids = "";
			settings = StringUtils.trimToEmpty(settings);
			boolean newdoc = settings.startsWith("@");
			if(newdoc) settings = settings.substring(1);
			
			String[] settingList = StringUtils.split(settings, '#');
			
			if(settingList == null || settingList.length == 0) return;
			for(int i=0; i<settingList.length; i++) {
				String setting = settingList[i];
				String[] colsetting = setting.split("\\|");
				
				if(colsetting == null || colsetting.length == 0) continue;
				
				String name = "";
				int showOrder = 0;
				int source = 0;
				String[] docids = null;
				int isreplay = 0;
				
				if(newdoc) {
					name = URLDecoder.decode(colsetting[0], "UTF-8");
					showOrder = NumberUtils.toInt(colsetting[1]);
					source = colsetting.length > 2 ? NumberUtils.toInt(colsetting[2]) : 1;
					docids = colsetting.length > 3 ? StringUtils.split(colsetting[3], ',') : null;
					isreplay = colsetting.length > 4 ? ("1".equals(colsetting[4]) ? 1 : 0) : 0;
				} else {
					name = modulename;
					showOrder = 1;
					source = colsetting.length>0 ? NumberUtils.toInt(colsetting[0]) : 1;
					docids = colsetting.length>1 ? StringUtils.split(colsetting[1], ',') : null;
					isreplay = colsetting.length>2 ? ("1".equals(colsetting[2]) ? 1 : 0) : 0;
				}
				
				int columnid = 0;
				
				String sql = "select columnid from  MobileDocSetting where name = '"+name+"' and scope ="+scope;
				rs.executeSql(sql);
				if(rs.next()) {
					columnid = rs.getInt("columnid");
					rs.executeSql("update  MobileDocSetting set scope = "+scope+",name = '"+name+"',source="+ source +",showOrder = "+  showOrder  + ",isreplay = " + isreplay +"where columnid="+columnid) ;
					rs.executeSql("delete from MobileDocColSetting where columnid="+columnid) ;
				}else{
					rs.executeSql("insert into MobileDocSetting(scope,name,source,showOrder,isreplay) values("+scope+",'"+name+"',"+source+","+showOrder+","+isreplay+")");
					rs.executeSql("select max(columnid) as maxid from MobileDocSetting");
					if(rs.next()) {
						columnid = rs.getInt("maxid");
					}
				}
				if(docids == null || docids.length == 0) continue;
				if(columnid <= 0) continue;
					columnids = columnids+columnid+",";
					List<String> paraList = new ArrayList<String>();
					for(int j=0; j<docids.length; j++) {
						paraList.add(""+columnid+Util.getSeparator()+docids[j]);
					}
					if(paraList != null && !paraList.isEmpty()) {
						brs.executeSqlBatch("insert into MobileDocColSetting(columnid,docid) values(?,?)", paraList);
					}
			}
			if(columnids.endsWith(",")){
				columnids = columnids.substring(0,columnids.length()-1);
			}
			if(!columnids.equals("")){
				rs.executeSql("delete from MobileDocColSetting where columnid in (select columnid from MobileDocSetting where scope="+scope+") and columnid not in(" +columnids+")");
				rs.executeSql("delete from MobileDocSetting where scope="+scope+" and columnid not in("+columnids+")");
			}
			
		}
	}
	
	public List<Map<String,Object>> parseDocSetting(String settings, String defaultName, boolean isShowDocName) throws Exception {
		List<Map<String,Object>> docSetting = new ArrayList<Map<String,Object>>();
		
		if(StringUtils.isBlank(settings)) {
			Map<String,Object> columnItem = new HashMap<String,Object>();
			
			columnItem.put("name", defaultName);
			columnItem.put("showOrder", 1);
			columnItem.put("source", 0);
			columnItem.put("docids", null);
			if(isShowDocName) {
				columnItem.put("docnames", null);
			}
			columnItem.put("isreplay", 0);
			
			docSetting.add(columnItem);
		} else {
			settings = StringUtils.trimToEmpty(settings);
			if(settings.startsWith("@")) {
				settings = settings.substring(1);
				String[] settingList = StringUtils.split(settings, '#');
				
				if(settingList == null || settingList.length == 0) return null;
				
				for(int i=0; i<settingList.length; i++) {
					Map<String,Object> columnItem = new HashMap<String,Object>();
					String setting = settingList[i];
					String[] colsetting = setting.split("\\|");
					
					columnItem.put("name", URLDecoder.decode(colsetting[0], "UTF-8"));
					columnItem.put("showOrder", NumberUtils.toInt(colsetting[1]));
					int source = colsetting.length > 2 ? NumberUtils.toInt(colsetting[2]) : 1;
					columnItem.put("source", source);
					List docids = colsetting.length > 3 ? Arrays.asList(StringUtils.split(colsetting[3], ',')) : null;
					columnItem.put("docids", docids);
					if(isShowDocName) {
						columnItem.put("docnames", this.getDocNames(source, docids));
					}
					columnItem.put("isreplay", colsetting.length > 4 ? ("1".equals(colsetting[4]) ? 1 : 0) : 0);
					
					docSetting.add(columnItem);
				}
			} else {
				String[] colsetting = StringUtils.split(settings, '|');
				Map<String,Object> columnItem = new HashMap<String,Object>();
				
				columnItem.put("name", defaultName);
				columnItem.put("showOrder", 1);
				int source = colsetting.length>0 ? NumberUtils.toInt(colsetting[0]) : 0;
				columnItem.put("source", source);
				List<String> docids = colsetting.length > 1 ? Arrays.asList(StringUtils.split(colsetting[1], ',')) : null;
				columnItem.put("docids", docids);
				if(isShowDocName) {
					columnItem.put("docnames", this.getDocNames(source, docids));
				}
				columnItem.put("isreplay", colsetting.length>2 ? ("1".equals(colsetting[2]) ? 1 : 0) : 0);
				
				docSetting.add(columnItem);
			}
		}
		
		return docSetting;
	}
	
	private List<String> getDocNames(int source, List<String> docids) throws Exception {
		if(docids == null || docids.size() == 0) return null;
		
		List<String> docnames = new ArrayList<String>();
		DocNewsComInfo docNewsComInfo = new DocNewsComInfo();
		SecCategoryComInfo secCategoryComInfo = new SecCategoryComInfo();
		DocTreeDocFieldComInfo docTreeDocFieldComInfo = new DocTreeDocFieldComInfo();
		DocComInfo docComInfo = new DocComInfo();
		
		if(source == 1){
			for(String docid : docids) {
				docnames.add(docNewsComInfo.getDocNewsname(docid));
			}
		} else if(source == 2){
			for(String docid : docids) {
				docnames.add(secCategoryComInfo.getSecCategoryname(docid));
			}
		} else if(source == 3){
			for(String docid : docids) {
				docnames.add(docTreeDocFieldComInfo.getTreeDocFieldName(docid));
			}
		} else if(source == 4){
			for(String docid : docids) {
				docnames.add(docComInfo.getDocname(docid));
			}
		}
		
		return docnames;
	}
	
	//微博接口实现方法=========================以下的=======================================================
	public Map getBlogPage(String mobileSession, String page,
			String module, String component,
			String url,String sessionKey,Map param) throws Exception {
		User user = as.getCurrUser(sessionKey);
		if(user==null) {
			Map result = new HashMap();
			//未登录或登录超时
			result.put("error", "200001");
			return result;
		}
		return this.getPage(mobileSession, page, null, module, component, null, null, url, param, sessionKey);
	}
	public Map postBlogPage(String mobileSession, String page, String module, String component, String detailid, String fromid, String url, Map form, String sessionKey) throws Exception {
		User user = as.getCurrUser(sessionKey);
		if(user==null) {
			Map result = new HashMap();
			//未登录或登录超时
			result.put("error", "200001");
			return result;
		}
		return postPage(mobileSession, page, module, component, detailid, fromid, url, form, sessionKey);
	}

	public JSONObject getBlogJson(FileUpload fu) throws Exception {
		String operation=Util.null2String((String)fu.getParameter("operation"));  
		String userid=Util.null2String((String)fu.getParameter("userid"));
		String blogid=Util.null2String((String)fu.getParameter("blogid"));
		String discussid=Util.null2String((String)fu.getParameter("discussid"));
		String enddatestr=Util.null2String((String)fu.getParameter("enddatestr"));
		int pageindex = Util.getIntValue(Util.null2String((String)fu.getParameter("pageindex")),1);
		int pagesize = Util.getIntValue(Util.null2String((String)fu.getParameter("pagesize")),10);
		if(pagesize <= 0) {
			pagesize = 10;
		}
		String workdate = Util.null2String((String)fu.getParameter("workdate"));
		String content = URLDecoder.decode(Util.null2String((String)fu.getParameter("content")),"utf-8");
		String isReplenish = Util.null2String((String)fu.getParameter("isReplenish"));
		String comefrom = Util.null2String((String)fu.getParameter("comefrom"));
		comefrom = comefrom.trim().toLowerCase();
		String location = Util.null2String((String)fu.getParameter("location"));
		String groupid = Util.null2String((String)fu.getParameter("groupid"));
		String score = Util.null2String((String)fu.getParameter("score"));    //分值mood
		String moodid = Util.null2String((String)fu.getParameter("moodid"));  //心情id
		String groupName = Util.null2String((String)fu.getParameter("groupName"));  //分组名称
		String uploadname = Util.null2String((String)fu.getParameter("uploadname"));  //附件名称
		String uploaddata = Util.null2String((String)fu.getParameter("uploaddata"));  //附件数据
		String imageids = Util.null2String((String)fu.getParameter("imageids"));      //图片id
		String userName = Util.null2String(URLDecoder.decode(fu.getParameter("userName")+"","utf-8"));  //用户名
		String replyid = Util.null2String((String)fu.getParameter("replyid"));      //回复记录id
		
		if("iphone".equals(comefrom))  
			comefrom = "1";
	    else if("ipad".equals(comefrom))  
	    	comefrom = "2";
	    else if("android".equals(comefrom)||"androidpad".equals(comefrom)) 
	    	comefrom="3";          
	    else if("webclient".equals(comefrom)) 
	    	comefrom="4";
	    else
	    	comefrom = "0";
		String commentType = Util.null2String((String)fu.getParameter("commentType"));
		String relatedid = Util.null2String((String)fu.getParameter("relatedid"));
		JSONObject json = new JSONObject();
		if ("getBlogListBydate".equals(operation)) { //查看微博获取更多
			Map map = wbs.getBlogDiscussListByDate(userid,blogid,enddatestr,""+pagesize);
			json = JSONObject.fromObject(map);
		} else if ("viewBlog".equals(operation)) {   //加载个人（我的）第一页
			Map map = wbs.viewBlog(userid,blogid,pagesize);
			if(map!=null){
				json = JSONObject.fromObject(map);
			}
			//保存微博
		} else if ("saveBlog".equals(operation)) {
			Map map = wbs.saveBlog(userid,workdate,content,isReplenish,comefrom,location,moodid,imageids);
			if(map!=null){
				json = JSONObject.fromObject(map);
			}
			//保存微博评论
		} else if ("saveBlogReply".equals(operation)) {
			Map map = wbs.saveReply(userid,blogid,discussid,content,comefrom,workdate,commentType,relatedid);
			if(map!=null){
				json = JSONObject.fromObject(map);
			}
			//更新微博
		} else if ("updateBlog".equals(operation)) {
			Map map = wbs.updateBlogDiscuss(discussid,content,userid,workdate,moodid,imageids); 
			if(map!=null){
				json = JSONObject.fromObject(map);
			}
			//发送提醒
		} else if ("sendRemind".equals(operation)) {
			Map map = wbs.sendSubmitRemind(userid,blogid,workdate);
			if(map!=null){
				json = JSONObject.fromObject(map);
			}
			//加载微博主页非第一页
		} else if ("getBlogDynamic".equals(operation)) {
			Map map = wbs.getBlogDiscussListByTime(userid,pageindex,pagesize,groupid);
			if(map!=null){
				json = JSONObject.fromObject(map);
			}
			//加载微博主页第一页
		} else if ("viewBlogDynamic".equals(operation)) {
			Map map = wbs.viewBlogDynamic(userid,1,10,groupid);
			if(map!=null){
				json = JSONObject.fromObject(map);
			}
			//标记微博为已读
		} else if ("markBlogRead".equals(operation)) {
			Map map = wbs.markBlogRead(userid,blogid,discussid);
			if(map!=null){
				json = JSONObject.fromObject(map);
			}
			//加载评论列表
		} else if ("getCommentList".equals(operation)) {
			Map map = wbs.getCommentList(userid,pageindex,pagesize);
			if(map!=null){
				json = JSONObject.fromObject(map);
			}
			//获得关注人列表非第一页
		} else if ("getAttentionList".equals(operation)) {
			Map conditions=null;
			if(!userName.equals("")){
				conditions=new HashMap();
				conditions.put("userName", userName);
			}
			Map map = wbs.getAttentionList(userid,pageindex,pagesize,groupid,conditions);
			if(map!=null){
				json = JSONObject.fromObject(map);
			}
			//获得关注人类表第一页
		} else if ("viewAttention".equals(operation)) {
			Map map = wbs.viewAttention(userid,pageindex,pagesize,groupid);
			if(map!=null){
				json = JSONObject.fromObject(map);
			}
		} else if ("addScore".equals(operation)) { //添加评分
			Map map = wbs.addScore(discussid, score);
			if(map!=null){
				json = JSONObject.fromObject(map);
			}
		}else if ("getUnreadCount".equals(operation)) { //获取主页、评论未读数 blogDao.getAttentionMe(userid)
			Map map = wbs.getUnreadCount(userid);
			if(map!=null){
				json = JSONObject.fromObject(map);
			}
		}else if ("getAttentionMeList".equals(operation)) { //获得我关注列表
			Map map = wbs.getAttentionMeList(blogid, pageindex,pagesize);
			if(map!=null){
				json = JSONObject.fromObject(map);
			}
		}else if ("addAttention".equals(operation)) { //添加关注
			Map map = wbs.addAttention(userid,blogid);
			if(map!=null){
				json = JSONObject.fromObject(map);
			}
		}else if ("cancelAttention".equals(operation)) { //取消关注
			Map map = wbs.cancelAttention(userid,blogid);
			if(map!=null){
				json = JSONObject.fromObject(map);
			}
		}else if ("requestAttention".equals(operation)) { //申请关注
			Map map = wbs.requestAttention(userid,blogid);
			if(map!=null){
				json = JSONObject.fromObject(map);
			}
		}else if ("getGroupList".equals(operation)) { //分组列表
			Map map = wbs.getGroupList(userid);
			if(map!=null){
				json = JSONObject.fromObject(map);
			}
		}else if ("addGroup".equals(operation)) { //新建分组
			Map map = wbs.addGroup(userid, groupName);
			if(map!=null){
				json = JSONObject.fromObject(map);
			}
		}else if ("editGroup".equals(operation)) { //编辑分组
			Map map = wbs.editGroup(userid, groupid, groupName);
			if(map!=null){
				json = JSONObject.fromObject(map);
			}
		}else if ("delGroup".equals(operation)) { //删除分组
			Map map = wbs.delGroup(userid, groupid);
			if(map!=null){
				json = JSONObject.fromObject(map);
			}
		}else if ("getBlogCount".equals(operation)) { //获得微博统计数据
			Map map = wbs.getBlogCount(userid);
			if(map!=null){
				json = JSONObject.fromObject(map);
			}
		}else if ("uploadFile".equals(operation)) {   //上传图片
			Map map = wbs.uploadFile(uploadname, uploaddata);
			if(map!=null){
				json = JSONObject.fromObject(map);
			}
		}else if ("getVisit".equals(operation)) {    //获取最近来访
			Map map = wbs.getVisitList(userid, pageindex, pagesize);
			if(map!=null){
				json = JSONObject.fromObject(map);
			}
		}else if ("delComment".equals(operation)) {    //删除评论
			Map map = wbs.delComment(replyid, discussid, userid);
			if(map!=null){
				json = JSONObject.fromObject(map);
			}
		}
		return json;
	}
	//微博接口实现方法==========================以上的======================================================
    public Map mobileUpdate(){
    	
		
		ResourceComInfo resourceComInfo=null;
		DepartmentComInfo departmentComInfo=null;
		SubCompanyComInfo subCompanyComInfo=null;
		RolesComInfo rolesComInfo=null;
		try {
			resourceComInfo=new ResourceComInfo();
			departmentComInfo=new DepartmentComInfo();
			subCompanyComInfo=new SubCompanyComInfo();
			rolesComInfo=new RolesComInfo();
		} catch (Exception e) {
		}
		
		Map result=new HashMap();
		RecordSet recordSet=new RecordSet();
		RecordSet rs=new RecordSet();
		String sql="";
		//获得mobile用户
		List shareList=new ArrayList();
		sql="SELECT * FROM PluginLicenseUser WHERE plugintype='mobile'";
		rs.execute(sql);
		while(rs.next()){
			String shareType = Util.null2String(rs.getString("sharetype"));
			String shareTypeName="";
			String shareValue = Util.null2String(rs.getString("sharevalue"));
			if(shareValue!=null){
				shareValue=shareValue.startsWith(",")?shareValue.substring(1):shareValue;
				shareValue=shareValue.endsWith(",")?shareValue.substring(1):shareValue; 
			}
			List shareValueList=Util.TokenizerString(shareValue, ",");
			String shareValueName="";
			
			if(shareType.equals("0")){
			   shareTypeName="人员";
			   for(int i=0;i<shareValueList.size();i++){
				   shareValueName=shareValueName+","+resourceComInfo.getLastname((String)shareValueList.get(i));
			   }
			}else if(shareType.equals("1")){
				shareTypeName="分部";
				for(int i=0;i<shareValueList.size();i++){
				   shareValueName=shareValueName+","+subCompanyComInfo.getSubCompanyname((String)shareValueList.get(i));
				}
			}else if(shareType.equals("2")){
				shareTypeName="部门";
				for(int i=0;i<shareValueList.size();i++){
				   shareValueName=shareValueName+","+departmentComInfo.getDepartmentname((String)shareValueList.get(i));
				}
			}else if(shareType.equals("3")){
				shareTypeName="角色";
				for(int i=0;i<shareValueList.size();i++){
					String sharelev=(String)shareValueList.get(i);
					sharelev =sharelev.length()>0?sharelev.substring(0,sharelev.length()-1):"";
					shareValueName=shareValueName+","+rolesComInfo.getRolesname(sharelev);
				}
			}else if(shareType.equals("4"))
				shareTypeName="所有人";
			
			if(shareValueName.length()>0)
				shareValueName=shareValueName.substring(1);
			
			String shareSeclevel = rs.getString("seclevel");
			
			recordSet.execute(sql);
			Map map=new HashMap();
			map.put("authType", shareType);
			map.put("authTypeName", shareTypeName);
			map.put("authValue", shareValue);
			map.put("authValueName", shareValueName);
			map.put("authSeclevel", shareSeclevel);
			
			shareList.add(map);
		}
		
		
		//获得模块配置
		sql="SELECT * FROM MobileConfig WHERE mc_type= 5 ORDER BY mc_module,mc_scope";
		recordSet.execute(sql);
		String tempModule="";
		String tempScope="";
		Map map=null;
		List moduleList=new ArrayList();
		while(recordSet.next()){
			
			String mc_module=recordSet.getString("mc_module");
			String mc_scope=recordSet.getString("mc_scope");
			String mc_name=recordSet.getString("mc_name");
			String mc_value=recordSet.getString("mc_value");
			
			if(!tempScope.equals(mc_scope)){
				tempScope=mc_scope;
				map=new HashMap();
				map.put("componentCode", mc_module); //功能编号
				moduleList.add(map);
			}
			if(mc_name.equals("label"))
				map.put("label", mc_value);          //模块名称
			
			if(mc_name.equals("visible"))
				map.put("visible", mc_value);        //模块是否可见
			
			if(mc_name.equals("index"))
				map.put("showOrder", mc_value);      //模块显示顺序
			
			if(mc_name.equals("flowids")||mc_name.equals("target"))
				map.put("setting", mc_value);        //模块设置内容
			
		}
		
		//获取分页设置
		String pagesize="10";
		sql="SELECT * FROM MobileConfig WHERE mc_type= 14";
		recordSet.execute(sql);
		while(recordSet.next()){
			pagesize=recordSet.getString("mc_value");
		}
		
		String hrmorder = "0";
		sql = "SELECT * FROM MobileConfig WHERE mc_type = 15";
		recordSet.execute(sql);
		while(recordSet.next()){
			hrmorder = recordSet.getString("mc_value");
		}
		
		String isShowTerminal = "1";
		sql = "SELECT * FROM MobileConfig WHERE mc_type = 16";
		recordSet.execute(sql);
		while(recordSet.next()){
			isShowTerminal = recordSet.getString("mc_value");
		}
		
		String loginpageinfo = "";
		sql = "SELECT * FROM MobileConfig WHERE mc_type = 18";
		recordSet.execute(sql);
		while(recordSet.next()){
			loginpageinfo = recordSet.getString("mc_value");
		}
		
		String welcome = "";
		sql = "SELECT * FROM MobileConfig WHERE mc_type = 17";
		recordSet.execute(sql);
		while(recordSet.next()){
			welcome = recordSet.getString("mc_value");
		}
		
		result.put("shareList", shareList);
		result.put("moduleList", moduleList);
		result.put("mobile.client.pagesize", pagesize);
		result.put("loginpageinfo", loginpageinfo);
		result.put("hrmorder", hrmorder);
		result.put("isShowTerminal", isShowTerminal);
		result.put("welcome", welcome);
		
		return result;
	}
    
    /**
     * 获得用户总数
     * @param conditionList  共享条件
     * @param reload  是否重新加载
     * @return
     */
    public int getMobileUserCount(List conditionList,boolean reload){
    	if(reload) as.clearPluginUserCache();
    	int count=as.getPluginAllUserId(conditionList).size();
    	return count;
    }
    
    /**
     * 更新消息推送密钥
     * @param key
     */
    public void updatePushkey(String key) {
    	File propFile = new File(GCONST.getPropertyPath(), "EMobile4.properties");
    	if(propFile.exists()) {
    		try {
    			PropertiesConfiguration properties = new PropertiesConfiguration();
    			properties.setDelimiterParsingDisabled(true);
    			properties.setEncoding(weaver.general.GCONST.PROP_UTF8);
    			properties.setFile(propFile);
    			properties.load();
    			
    			properties.setProperty("pushKey", key);
    			properties.save();
    		} catch (ConfigurationException e) {
    			e.printStackTrace();
    		}
    	}
    }
    
    public boolean saveMobileProp(String propName, String propValue) {
    	try {
    		RecordSet rs = new RecordSet();
    		rs.executeSql("select propValue from mobileProperty where name='" + propName + "'");
    		if(rs.next()){
    			rs.executeSql("update mobileProperty set propValue='" + propValue + "' where name='" + propName + "'");
    		} else {
    			rs.executeSql("insert into mobileProperty(name, propValue) values('" + propName + "','" + propValue + "')");
    		}
    		
    		return true;
    	} catch (Exception e) {
    		logger.error("", e);
    		return false;
    	}
    }
    
    public boolean saveMobileProp(String propName, List<String> propValue) {
    	try {
    		RecordSet rs = new RecordSet();
    		rs.executeSql("delete from mobileProperty where name='" + propName + "'");
    		
    		if(propValue != null && propValue.size() > 0) {
    			List<String> paraList = new ArrayList<String>();
        		
    			for(String value : propValue) {
    				paraList.add(propName + Util.getSeparator() + value);
    			}
        		
        		BatchRecordSet brs = new BatchRecordSet();
    			brs.executeSqlBatch("insert into mobileProperty(name, propValue) values(?,?)", paraList);
    		}
    		
    		return true;
    	} catch (Exception e) {
    		logger.error("", e);
    		return false;
    	}
    }
    
    public Map<String, String> checkPluginFile(String fileHash) {
    	Map<String, String> result = new HashMap<String, String>();
    	FileInputStream pluginInput = null;
    	
    	try {
    		result.put("isUpdate", "0");
    		pluginInput = new FileInputStream(GCONST.getRootPath() + "mobile/plugin/plugin.xml");
    		String curHash = DigestUtils.md5Hex(IOUtils.toByteArray(pluginInput));
    		
    		if(StringUtils.isNotBlank(fileHash) && !fileHash.equals(curHash)) {
    			result.put("isUpdate", "1");
    		}
    	} catch (Exception e) {
    		logger.error("", e);
    	} finally {
    		IOUtils.closeQuietly(pluginInput);
    	}
    	
    	return result;
    }
    
    public Map<String, Object> getWorkCenterList(User user, String modules, String keyword, int pageIndex, int pageSize) {
    	Map<String, Object> data = new HashMap<String, Object>();
    	
    	try {
    		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        	int count = 0;
    		int pageCount = 0;
    		int isHavePre = 0;
    		int isHaveNext = 0;
        	
    		getWorkCenterSqlClass();
    		
    		StringBuilder sqlsb = new StringBuilder();
        	
        	JSONObject jsonObj = JSONObject.fromObject(modules);
        	Map<String, List<String>> moduleMap = (Map<String, List<String>>)JSONObject.toBean(jsonObj, HashMap.class);
        	
        	Iterator<Entry<String, List<String>>> moduleItor = moduleMap.entrySet().iterator();
        	while(moduleItor.hasNext()) {
        		Entry<String, List<String>> entry = moduleItor.next();
        		int moduleid = NumberUtils.toInt(entry.getKey());
        		List<String> scopeList = entry.getValue();
        		
        		Map<String, Object> workCenterMap = WorkCenterClassMap.get(moduleid);
        		if(workCenterMap == null) continue;
        		Integer wctcategory = (Integer)workCenterMap.get("wctcategory");
        		IWorkCenter workCenterSql = (IWorkCenter)workCenterMap.get("wctClass");
    			if(wctcategory == null || wctcategory == 0 || workCenterSql == null) continue;
    			String sql = workCenterSql.getWorkCenterSql(wctcategory, moduleid, scopeList, user);
    			if(StringUtils.isBlank(sql)) continue;
    			if(sqlsb.length() > 0) {
    				sqlsb.append(" UNION ALL ");
    			}
    			sqlsb.append("("+sql+")");
        	}
        	
        	String baseSql = " FROM (" + sqlsb.toString() + ") t ";
        	
        	String countSql = "select count(1) as c " + baseSql;
        	
        	RecordSet rs = new RecordSet();
    		
    		rs.executeSql(countSql);
    		if(rs.next()) count = rs.getInt("c");
    		if (count <= 0) pageCount = 0;
    		pageCount = count / pageSize + ((count % pageSize > 0)?1:0);
    		isHaveNext = (pageIndex + 1 <= pageCount)?1:0;
    		isHavePre = (pageIndex - 1 >= 1)?1:0;
    		
    		String listSql = " * " + baseSql;
    		listSql += " order by recivetime desc,id desc";
    		
    		if(pageIndex>0&&pageSize>0&&pageCount>1) {
    			if (rs.getDBType().equals("oracle")) {
    				listSql = "select " + listSql;
    				listSql = "select * from ( select row_.*, rownum rownum_ from ( " + listSql + " ) row_ where rownum <= " + (pageIndex * pageSize) + ") where rownum_ > " + ((pageIndex-1) * pageSize); 					
    			} else {
    				if(pageIndex>1) {
    					int topSize = pageSize;
    					if(pageSize * pageIndex > count) {
    						topSize = count - (pageSize * (pageIndex - 1));
    					}
    					listSql = " select top " + topSize + " * from ( select top  " + topSize + " * from ( select top " + (pageIndex * pageSize) + listSql + " ) tbltemp1 order by recivetime asc,id asc ) tbltemp2 order by recivetime desc,id desc ";
    				} else {
    					listSql = " select top " + pageSize + listSql;
    				}
    			}
    		} else {
    			listSql = " select " + listSql;
    		}
    		
    		rs.executeSql(listSql);
    		ResourceComInfo rci = new ResourceComInfo();
    		while(rs.next()) {
    			Map<String, String> dataMap = new HashMap<String, String>();
    			dataMap.put("id", rs.getString("id"));//请求ID
    			dataMap.put("name", rs.getString("name"));//请求标题
    			dataMap.put("type", rs.getString("type"));//请求类型
    			dataMap.put("creater", rci.getLastname(rs.getString("creater")));//请求创建人
    			dataMap.put("recivetime", rs.getString("recivetime"));//接收时间
    			dataMap.put("status", rs.getString("status"));//请求状态
    			
    			dataMap.put("module", rs.getString("module"));//模块小类
    			dataMap.put("scope", rs.getString("scope"));//模块id
    			
    			list.add(dataMap);
    		}
    		
    		data.put("result", "list");
    		
    		data.put("pagesize",pageSize+"");
    		data.put("pageindex",pageIndex+"");
    		data.put("count",count+"");
    		data.put("pagecount",pageCount+"");
    		data.put("ishavepre",isHavePre+"");
    		data.put("ishavenext",isHaveNext+"");

    		data.put("list",list);
    	}catch (Exception e) {
			logger.error("", e);
		}
    	
    	return data;
    }
    
    private Map<Integer, Map<String, Object>> getWorkCenterSqlClass() throws ClassNotFoundException, InstantiationException, IllegalAccessException, JDOMException, IOException {
    	if(WorkCenterClassMap == null) {
    		WorkCenterClassMap = new HashMap<Integer, Map<String, Object>>();
    		
    		SAXBuilder builder = new SAXBuilder();
    		org.jdom.Document doc = builder.build(new File(GCONST.getRootPath(), "/mobile/plugin/plugin.xml"));
    		List<org.jdom.Element> components = XPath.selectNodes(doc, "/plugin/components/component[wctcategory]");
    		for(org.jdom.Element component : components) {
    			int moduleid = NumberUtils.toInt(component.getChildText("id"));
    			int wctcategory = NumberUtils.toInt(component.getChildText("wctcategory"));
				String clazzName = component.getChildText("wctClass");
				if(moduleid == 0 || wctcategory == 0 || StringUtils.isBlank(clazzName)) continue;
    			Class clazz = Class.forName(clazzName);
    	    	IWorkCenter instance = (IWorkCenter)clazz.newInstance();
    	    	Map<String, Object> wctMap = new HashMap<String, Object>();
    	    	wctMap.put("wctcategory", wctcategory);
    	    	wctMap.put("wctClass", instance);
    			WorkCenterClassMap.put(moduleid, wctMap);
    		}
    	}
    	
    	return WorkCenterClassMap;
    }
    
    /**
     * 用户签到接口
     * 
     * @param user 当前用户
     * @param type 签到类型:checkin/checkout
     * @param ipaddr 客户端ip地址
     * @param latlng 客户端地理位置经纬度
     * @param inCom 是否在公司
     * @return 签到信息
     */
    public Map<String, String> checkIn(User user, String type, String ipaddr, String latlng, String addr, boolean inCom) {
    	Map<String, String> result = new HashMap<String, String>();
    	try {
    		RecordSet rs = new RecordSet();
    		
    		HrmScheduleDiffUtil hrmScheduleDiffUtil = new HrmScheduleDiffUtil();
    		hrmScheduleDiffUtil.setUser(user);
    		
    		String[] signInfo = HrmScheduleDiffUtil.getSignInfo(user);
    		boolean isNeedSign = Boolean.parseBoolean(signInfo[0]);
    		String sign_flag = signInfo[1];
        	
        	int checkType = "checkin".equals(type) ? 1 : ("checkout".equals(type) ? 2 : 0);
        	if(checkType == 0) {
        		result.put("isEnableCheckin", isNeedSign ? "1" : "0");
        		result.put("isCheckin", "1".equals(sign_flag) ? "0" : "1");
        		return result;
        	}
    		
    		String isInCom = inCom ? "1" : "0";
    		
    		String currentDate = TimeUtil.getCurrentDateString();
    		String currentTime = TimeUtil.getOnlyCurrentTimeString();
    		String tsql = "";
    		if("oracle".equals(rs.getDBType())) {
    			tsql = "select to_char(sysdate,'yyyy-mm-dd') as currentDate,to_char(sysdate,'hh24:mi:ss') as currentTime from dual";
    		} else {
    			tsql = "select convert(char(10),getdate(),20) currentDate,convert(char(8),getdate(),108) currentTime";	
    		}
    		rs.executeSql(tsql);
    		while(rs.next()) {
    			currentDate = StringUtils.defaultIfEmpty(rs.getString("currentDate"), currentDate);
    			currentTime = StringUtils.defaultIfEmpty(rs.getString("currentTime"), currentTime);
    		}
    		
    		String[] latlngs = StringUtils.split(latlng, ',');
    		String latitude = (latlngs != null && latlngs.length==2) ? latlngs[0] : "";
    		String longitude = (latlngs != null && latlngs.length==2) ? latlngs[1] : "";
    		
    		rs.executeSql("insert into HrmScheduleSign(userId,userType,signType,signDate,signTime,clientAddress,isInCom,signFrom,longitude,latitude,addr) " +
    				"values("+user.getUID()+",'"+user.getLogintype()+"','"+checkType+"','"+currentDate+"','"+currentTime+"','"+ipaddr+"','"+isInCom+"','mobile','"+longitude+"','"+latitude+"','"+addr+"')");
    		
    		if("1".equals(isInCom)){
    			String returnString = "";
    			
    			Map onDutyAndOffDutyTimeMap = hrmScheduleDiffUtil.getOnDutyAndOffDutyTimeMap(currentDate, user.getUserSubCompany1());
    			String onDutyTimeAM = Util.null2String((String)onDutyAndOffDutyTimeMap.get("onDutyTimeAM"));
    			String offDutyTimePM = Util.null2String((String)onDutyAndOffDutyTimeMap.get("offDutyTimePM"));
    			
    			if(checkType == 1 && currentTime.compareTo(onDutyTimeAM) <= 0){
    				//您已成功签到，签到时间：
    				returnString += SystemEnv.getHtmlLabelName(20034,user.getLanguage())+"，"+SystemEnv.getHtmlLabelName(20035,user.getLanguage())+"：";
    			} else if(checkType == 2 && currentTime.compareTo(offDutyTimePM) >= 0){
    				//您已成功签退，签退时间：
    				returnString += SystemEnv.getHtmlLabelName(20038,user.getLanguage())+"，"+SystemEnv.getHtmlLabelName(20039,user.getLanguage())+"：";
    			} else {
    				//如因工作原因迟到或早退请提交相应流程，签到（签退）时间：
    				returnString += SystemEnv.getHtmlLabelName(20036,user.getLanguage())+"，"+SystemEnv.getHtmlLabelName(20037,user.getLanguage())+"：";
    			}
    			
    			returnString += currentDate+" "+currentTime;
    			
    			result.put("result", "success");
    			result.put("msg", returnString);
    		} else {
    			result.put("result", "error");
    			result.put("error", "地理位置不在考勤范围内，此次签到（签退）不计入考勤！");
    		}
        }catch (Exception e) {
			logger.error("", e);
			result.put("result", "error");
			result.put("error", e.getMessage());
		}
    	return result;
    }
    
    /**
     * 计算两个经度纬度(WGS-84)之间的距离
     * @param lng1 坐标1经度
     * @param lat1 坐标1纬度
     * @param lng2 坐标2经度
     * @param lat2 坐标2纬度
     * @return 2个坐标之间距离,单位:米
     */
    private double geo_distance(double lng1, double lat1, double lng2, double lat2) {
    	double radLat1 = Math.toRadians(lat1);
		double radLat2 = Math.toRadians(lat2);
		double a = Math.abs(radLat1 - radLat2);
		double b = Math.abs(Math.toRadians(lng1) - Math.toRadians(lng2));
		double distance = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
		distance = distance * EARTH_RADIUS;
		distance = Math.round(distance * 10000) / 10000;
		return distance;
	}
}
