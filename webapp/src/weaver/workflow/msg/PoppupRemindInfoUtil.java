package weaver.workflow.msg;

/*
 * Created on 2006-05-18
 * Copyright (c) 2001-2006 泛微软件
 * 泛微协同商务系统，版权所有。
 * 
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import weaver.conn.RecordSet;
import weaver.file.Prop;
import weaver.general.AES;
import weaver.general.BaseBean;
import weaver.general.GCONST;
import weaver.general.Util;
import weaver.interfaces.hrm.SendMessageWorkRunnable;
import weaver.rtx.ElinkWorkRunnable;
import weaver.rtx.RTXConfig;
import weaver.rtx.RTXWorkRunnable;
import weaver.sysinterface.SendMessageService;
import weaver.systeminfo.SystemEnv;
import weaver.workflow.request.WFPathUtil;
import weaver.workflow.workflow.WorkflowComInfo;
/**
 * 提醒信息后台处理公共接口
 * 
 * @author xwj 2005-12-27
 */

public class PoppupRemindInfoUtil extends BaseBean
{
    public RecordSet rs;

    public RecordSet rs1;

    public RecordSet rsrtx;
    
    public String sqlStr = "";

    public String sqlrtx = "";
    
    private String requeststr;

    private WorkflowComInfo wfcif;
    
    //是否来自流程引擎
    private boolean isfromwfengine = false;
    
    /**
     * 构造提醒类，设置提醒发起者为流程引擎（RequestManager） 
     * @param isfromwfengine
     */
    public PoppupRemindInfoUtil(boolean isfromwfengine) {
        //调用默认构造方法
        this();
        this.isfromwfengine = isfromwfengine;
    }
    
    public PoppupRemindInfoUtil()
    {
        rs = new RecordSet();
        rs1 = new RecordSet();
        rsrtx = new RecordSet();
        requeststr = "";
        try {
        	wfcif = new  WorkflowComInfo();
        } catch(Exception ex) {
        }
    }

    /**
     * 流程请求id过滤
     * 
     * @param userid
     *            和某类别提醒相关联的用户id (如流程操作者,文档审批人......
     * @param type
     *            提醒类别代码
     * @param logintype
     *            用户类型 (0: 内部, 1:外部)
     * @param requestid
     *            流程请求id
     * @param flag
     *            insert or update
     * @return 过滤后的流程请求id
     */
    public String requstidsFilter(int userid, int type, String logintype, String requestid, String flag)
    {
        String requestids = requestid;
        String[] requestidstrs = new String[2];
        ArrayList arr = new ArrayList();
        sqlStr = "select requestids from SysPoppupRemindInfo where userid =" + userid + " and usertype='" + logintype + "' and type = " + type;

        rs.executeSql(sqlStr);
        if (rs.next())
        {
            requestidstrs = Util.TokenizerString2(rs.getString("requestids"), ",");
            for (int i = 0; i < requestidstrs.length; i++)
            {
                arr.add(requestidstrs[i]);
            }
            if ("i".equals(flag))
            {
                arr.add(requestid);
            }
            else
            {
                if (arr.size() != 0)
                {
                    arr.remove(requestid);
                }
            }
            if (arr.size() != 0)
            {
                requestids = "";
                for (int a = 0; a < arr.size(); a++)
                {
                    requestids += (String) arr.get(a) + ",";
                }
            }
            else
            {
                requestids = requestids + ",";
            }

        }
        else
        {
            requestids = requestids + ",";
        }
        return requestids.trim();

    }

    /**
     * 增加流程请求id
     * 
     * @param userid
     *            和某类别提醒相关联的用户id (如流程操作者,文档审批人......
     * @param type
     *            提醒类别代码
     * @param logintype
     *            用户类型 (0: 内部, 1:外部)
     * @param requestid
     *            流程请求id

     * @return boolean requestid是否已经存在,不存在为true
     */
    public boolean addRequstids(int userid, int type, String logintype, String requestid)
    {
        String requestids = requestid;
        String[] requestidstrs = new String[2];
        ArrayList arr = new ArrayList();
        requeststr = "";
        boolean returnvalue = false;
		if (!requestid.equals("-1"))
		{
        sqlStr = "select requestid from SysPoppupRemindInfoNew where userid =" + userid + " and usertype='" + logintype + "' and type = " + type+" and requestid="+requestid;
        }
		else
		{
		 sqlStr = "select requestid from SysPoppupRemindInfoNew where userid =" + userid + " and usertype='" + logintype + "' and type = " + type+"  and requestid is null ";

		}
		rs1.executeSql(sqlStr);
        if (rs1.next())
        {returnvalue=true;}
      
        return returnvalue;
    }

    /**
     * 生成新的提醒信息
     * 
     * @param userid
     *            和某类别提醒相关联的用户id (如流程操作者,文档审批人......
     * @param type
     *            提醒类别代码
     * @param logintype
     *            用户类型 (0: 内部, 1:外部)
     * @param requestid
     *            该提醒信息若生成requestid(比如流程提醒)则为具体的请求id, 不生成requestid的在调用该方法时设置为 -1
     * @return 是否生成新的提醒信息
     */
    public boolean insertPoppupRemindInfo(int userid, int type, String logintype, int requestid)
    {
        return (this.addPoppupRemindInfo(userid, type, logintype, requestid));
       
    }

    /**
     * 生成新的提醒信息
     * 
     * @param userid
     *            和某类别提醒相关联的用户id (如流程操作者,文档审批人......
     * @param type
     *            提醒类别代码
     * @param logintype
     *            用户类型 (0: 内部, 1:外部)
     * @param requestid
     *            该提醒信息若生成requestid(比如流程提醒)则为具体的请求id, 不生成requestid的在调用该方法时设置为 -1
     * @param requestname 
     * 			  流程名           
     * @return 是否生成新的提醒信息
     */
    public boolean insertPoppupRemindInfo(int userid, int type, String logintype, int requestid,String requestname)
    {
        return (this.addPoppupRemindInfo(userid, type, logintype, requestid,requestname));
       
    }
    
    /**
     * 生成新的提醒信息(王宇静添加，此方法为RequestManager使用，由于该类中的事务导致无法执行查询操作，workflowid采用传入方式)
     * 
     * @param userid
     *            和某类别提醒相关联的用户id (如流程操作者,文档审批人......
     * @param type
     *            提醒类别代码
     * @param logintype
     *            用户类型 (0: 内部, 1:外部)
     * @param requestid
     *            该提醒信息若生成requestid(比如流程提醒)则为具体的请求id, 不生成requestid的在调用该方法时设置为 -1
     * @param requestname 
     * 			  流程名 
     * @param workflowid 
     * 			  流程ID           
     * @return 是否生成新的提醒信息
     */
    public boolean insertPoppupRemindInfo(int userid, int type, String logintype, int requestid,String requestname,int workflowid)
    {
        return (this.addPoppupRemindInfo(userid, type, logintype, requestid,requestname,workflowid));
    }

    /**
     * 生成新的提醒信息
     * 
     * @param userid
     *            和某类别提醒相关联的用户id (如流程操作者,文档审批人......
     * @param type
     *            提醒类别代码
     * @param logintype
     *            用户类型 (0: 内部, 1:外部)
     * @param requestid
     *            该提醒信息若生成requestid(比如流程提醒)则为具体的请求id, 不生成requestid的在调用该方法时设置为 -1
     * @return 是否生成新的提醒信息
     */
    public boolean addPoppupRemindInfo(int userid, int type, String logintype, int requestid){
    	return addPoppupRemindInfo(userid,type,logintype,requestid,"");
    }
    
    public boolean addPoppupRemindInfo(int userid, int type, String logintype, int requestid,String requestname){
    	return addPoppupRemindInfo(userid,type,logintype,requestid,requestname,-1);
    }
    
    
    private Map<String, Map<String, String>> getResouceIdapInfos(String remindresstrs, String mode) {
        Map<String, Map<String, String>> residapinfo = new HashMap<String, Map<String,String>>();
        
        if (remindresstrs == null || "".equals(remindresstrs)) {
            return residapinfo;
        }
        //获取人员相关信息
        
            sqlrtx = "select id, loginid, password, isADAccount  from hrmresource where " + Util.getSubINClause(remindresstrs, "id", "IN");
            RecordSet resrs = new RecordSet();
            resrs.executeSql(sqlrtx);
            while (resrs.next()) {
                String loginid = resrs.getString("loginid");
                String password = resrs.getString("password");
                String isADAccount = resrs.getString("isADAccount");
                // 解决rtx消息提醒是ldap登录验证(ldap是没有loginid和password)td13185
                if (mode != null && mode.equals("ldap")) {
	                if ("1".equals(isADAccount)) {
	                    // loginid、account字段整合 qc:128484
	                    //loginid = resrs.getString("loginid");
	                    password = resrs.getString("loginid");
	                }
                }
                Map<String, String> resmap = new HashMap<String, String>();
                resmap.put("loginid", loginid);
                resmap.put("isADAccount", isADAccount);
                resmap.put("password", password);
                residapinfo.put(resrs.getString("id"), resmap);
            
        }
        
        return residapinfo;
    }

    private String reslist2resstr(List list) {
        String result = "";
        for (int i = 0; i < list.size(); i++) {
            Map map = (Map) list.get(i);
            int userid = Util.getIntValue((String)map.get("userid"), 0);
            int logintype = Util.getIntValue((String)map.get("logintype"));
            
            if (logintype == 0) {
                result += "," + userid;
            }
        }
        
        if (result.length() > 1) {
            result = result.substring(1);
        }
        return result;
    }
    
    private Map<String, Map<String, String>> getSysPoppupInfos() {
        
        Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();
        RecordSet prs = new RecordSet();
        String sqlStr = "select statistic,typedescription,link, type from SysPoppupInfo";
        prs.executeSql(sqlStr);
        while (prs.next()) {
            
            Map<String, String> syspoppupmap = new HashMap<String, String>();
            syspoppupmap.put("statistic", prs.getString("statistic"));
            syspoppupmap.put("typedescription", prs.getString("typedescription"));
            syspoppupmap.put("link", prs.getString("link"));
            
            result.put(Util.null2String(prs.getString("type")), syspoppupmap);
        }
        return result;
    }
    
    /**
     * 返回人员提醒设置信息
     * @param remindresstrs 数据结构：{人员ID : {
     *                                             model:0,
     *                                             list:[
     *                                                 1243,
     *                                                 1212,
     *                                                 9843
     *                                             ]
     *                                         }
     *                               }
     * @return
     */
    private Map<String, Map<String, Object>> getRemindInfosByRes(String remindresstrs) {
        
        Map<String, Map<String, Object>> result = new HashMap<String, Map<String, Object>>();
        if (remindresstrs == null || "".equals(remindresstrs)) {
            return result;
        }
        boolean insertorupdate = false;
        RecordSet rs = new RecordSet();
        int module = 0;
        String sqlStr = "select idsmodule, resourceid, ids from SysPoppupRemindInfoConfig  where id_type = 'flowids' and (" + Util.getSubINClause(remindresstrs, "resourceid", "IN") + ")";
        rs.execute(sqlStr);
        Map<String, Object> remindmap = null;
        List<String> remindlist = null;
        while (rs.next()) {
            /*
            Map<String, String> resremindMap = new HashMap<String, String>();
            resremindMap.put("idsmodule", rs.getString("idsmodule"));
            resremindMap.put("resourceid", rs.getString("resourceid"));
            resremindMap.put("ids", rs.getString("ids"));
            
            remindlist = result.get(rs.getString("resourceid"));
            if (remindlist == null) {
                remindlist = new ArrayList<Map<String,String>>();
                result.put(rs.getString("resourceid"), remindlist);   
            }
            remindlist.add(resremindMap);
            */
            remindmap = result.get(rs.getString("resourceid"));
            if (remindmap == null) {
                remindmap = new HashMap<String,Object>();
                remindmap.put("list", new ArrayList<String>());
                remindmap.put("model",  rs.getString("idsmodule"));
                result.put(rs.getString("resourceid"), remindmap);   
            }
            remindlist = (List<String>)remindmap.get("list");
            remindlist.add(rs.getString("ids"));
        }
        
        return result;
        /*
        if (rs.next()) {
            module = rs.getInt("idsmodule");
        } else {
            insertorupdate = true;
        }
        sqlStr = "select count(*) as cou from SysPoppupRemindInfoConfig  where id_type = 'flowids' and resourceid in (" + remindresstrs + ") and ids = '" + workflowid + "'";
        rs.execute(sqlStr);
        if (rs.next()) {
            int cou = rs.getInt("cou");
            if (cou > 0 && module == 0) {
                insertorupdate = true;
            }
            if (cou == 0 && module != 0) {
                insertorupdate = true;
            }
        }
        */
    }
    
    /**
     * 获取需要提醒的人员在提醒信息表中的记录数<font color="red">注意：不保证流程引擎以外的业务类调用正确性</font>
     * 只查询内部用户和requestid不会为空的情况
     * @param userids
     * @param requestid
     * @return
     */
    public Map<String, String> addRequstids(String userids, String requestid) {
        
        Map<String, String> resourcePoppupRemindRecords = new HashMap<String, String>();
        
        RecordSet rs = new RecordSet();
        String sqlstr = "select userid, type, count(1) count FROM SysPoppupRemindInfoNew where requestid='" + requestid + "' and (" + Util.getSubINClause(userids, "userid", "IN") + ") and usertype=0 group by userid, usertype, type, requestid";
        rs.executeSql(sqlstr);
        while (rs.next()) {
            resourcePoppupRemindRecords.put(rs.getString("userid") + "_" + rs.getString("type"), rs.getString("count"));
        }
        return resourcePoppupRemindRecords;
    }
    
    /**
     * 流程短信提醒接口改造
     * @param list
     * @return
     */
    public boolean insertPoppupRemindInfo(List list)
    {
        return (this.addPoppupRemindInfo(list));
    }
    
    /**
     * 生成新的提醒信息
     * 
     * @param userid
     *            和某类别提醒相关联的用户id (如流程操作者,文档审批人......
     * @param type
     *            提醒类别代码
     * @param logintype
     *            用户类型 (0: 内部, 1:外部)
     * @param requestid
     *            该提醒信息若生成requestid(比如流程提醒)则为具体的请求id, 不生成requestid的在调用该方法时设置为 -1
     * @param requestname
     * 			  流程名           
     * @return 是否生成新的提醒信息
     */
    public boolean addPoppupRemindInfo(List list)
    {
        List pushmsginfolist = new ArrayList();
        List ulist = new ArrayList();
        boolean flag = true;
        boolean statistic = false;
        String tempurl = "";
        String loginid = "";
        String password = "";
        String para = "";
        String oaaddress = "";
        String creater = "";
        //获取相关设置
        sqlStr = "select * from SystemSet";
        rsrtx.executeSql(sqlStr);
        rsrtx.next();
        oaaddress = rsrtx.getString("oaaddress");
        
        String mode = Prop.getPropValue(GCONST.getConfigFile(), "authentic");
        //所有提醒人, 将list中内部用户转换为字符串（1,23,4）
        String remindresstrs = reslist2resstr(list);
        //获取人员相关信息residapinfo
        Map<String, Map<String, String>> residapinfos = getResouceIdapInfos(remindresstrs, mode);
        //获取提醒链接等设置
        Map<String, Map<String, String>> sysPoppupInfos = getSysPoppupInfos();
        //人员提醒设置信息
        Map<String, Map<String, Object>> remindinfos = getRemindInfosByRes(remindresstrs);
        
        //获取指定人员在提醒信息表中的记录信息
        Map<String, String> resourcePoppupRemindRecords = null;
        int remindReqid = -1;
        if (this.isfromwfengine && list.size() > 0) {
            remindReqid = Util.getIntValue((String)((Map)list.get(0)).get("requestid"), -1);
            if (remindReqid > 0) {
                resourcePoppupRemindRecords = addRequstids(remindresstrs, remindReqid + "");
                SendMessageService sendMessageService= new SendMessageService(remindReqid+"",remindresstrs);
                sendMessageService.start();
            }
        }
        Map<String, StringBuffer> insertremindMap = new HashMap<String, StringBuffer>();
        Map<String, StringBuffer> updateremindMap = new HashMap<String, StringBuffer>();
        
        for (int i = 0; i < list.size(); i++) {
            Map map = (HashMap) list.get(i);
            int userid = Util.getIntValue("" + map.get("userid"), 0);
            int type = Util.getIntValue("" + map.get("type"), 0);
            String logintype = Util.null2String("" + map.get("logintype"));
            int requestid = Util.getIntValue("" + map.get("requestid"), 0);
            String requestname = Util.null2String("" + map.get("requestname"));
            int workflowid = Util.getIntValue("" + map.get("workflowid"), 0);
            creater = Util.null2String("" + map.get("creater"));
            boolean insertorupdate = false;
            if (type == 11||type == 25) {// 资产验收提醒 公共组提醒建议
                insertorupdate = true;
            }
            if (workflowid == -1) {// workflowid未传入，即不是在RequestManager事务使用，采用查询方式
                RecordSet rs1 = new RecordSet();
                String flowidsql = "select workflowid,requestname,creater from workflow_requestbase where requestid=" + requestid;
                rs1.execute(flowidsql);
                if (rs1.next()) {
                    workflowid = rs1.getInt("workflowid");
                    requestname = Util.null2String(rs1.getString("requestname"));
                    creater = Util.null2String(rs1.getString("creater"));
                    /* 推送消息 */
                    map.put("workflowid", "" + workflowid);
                    map.put("requestname", requestname);
                    map.put("creater", creater);
                }
            }

			//添加流程类型id,流程类型名称,接收时间
		    String workflowtypeSql = "select a.id,a.typename from workflow_type a join workflow_base  b on a.id = b.workflowtype where b.id ="+workflowid;
		    rs1.execute(workflowtypeSql);
		    String wftypeid="";
		    String wftypename ="";
		    String receivetime="";
		     if(rs1.next()){
		        wftypeid = Util.null2String(rs1.getString("id"));
		        wftypename = Util.null2String(rs1.getString("typename"));
		      }
		      String receivetimeSql = " select t2.receivedate,t2.receivetime from workflow_requestbase t1,workflow_currentoperator t2  where t1.requestid=t2.requestid  and t2.usertype = 0 and t2.userid = " + userid+" and t1.requestid = "+requestid+"  and ((t2.isremark=0 and (t2.takisremark is null or t2.takisremark=0 )) or t2.isremark in('1','5','7')) and t2.islasttimes=1  and t1.workflowID in(select id from workflow_base where isvalid='1') ";
		      rs1.execute(receivetimeSql);
		      if(rs1.next()){
		          receivetime = Util.null2String(rs1.getString("receivedate"))+" "+Util.null2String(rs1.getString("receivetime"));
		       }
		       map.put("wftypeid", wftypeid);
		       map.put("wftypename", wftypename);
		       map.put("receivetime", receivetime);

            // 工作流为测试状态不产生提醒信息
            String isvalid = "1";
            if (workflowid > 0 && wfcif != null) {
                isvalid = wfcif.getIsValid("" + workflowid);
            }
			String baseSql = "select isvalid from workflow_base where id="+workflowid;
		    rs1.execute(baseSql);
		    if(rs1.next()){
		          isvalid = Util.null2String(rs1.getString("isvalid"));
		    }
            if (!"1".equals(isvalid)) {
                return flag;
            }
            
            //-----------------------------------------------------------------------------------------------------
            //获取是否人员是否提醒（从缓存中获取人员提醒设置信息，不再每个人去查询一次数据库，减少与数据库交互时间） start
            //-----------------------------------------------------------------------------------------------------
            int module = 0;
            Map<String, Object> remindmap = remindinfos.get(userid + "");
            
            List<String> remindlist = null;
            if (remindmap != null && !remindmap.isEmpty()) {
                module = Util.getIntValue((String)remindmap.get("model"));
                remindlist = (List<String>)remindmap.get("list");
            } else {
                insertorupdate = true;
            }
            
            int remindcount = 0;
            if (remindlist != null && remindlist.contains(workflowid + "")) {
                if (module == 0) {
                    insertorupdate = true;
                    //break;
                }
                remindcount++;
            } else if (module != 0){
                insertorupdate = true;
            }
            
            if (module != 0 && remindcount == 0) {
                insertorupdate = true;
            }
            //-----------------------------------------------------------------------------------------------------
            //获取是否人员是否提醒（从缓存中获取人员提醒设置信息，不再每个人去查询一次数据库，减少与数据库交互时间） end
            //-----------------------------------------------------------------------------------------------------
            /*
            
            int module = 0;
            sqlStr = "select idsmodule from SysPoppupRemindInfoConfig  where id_type = 'flowids' and resourceid=" + userid;
            rs.execute(sqlStr);
            if (rs.next()) {
                module = rs.getInt("idsmodule");
            } else {
                insertorupdate = true;
            }
            sqlStr = "select count(*) as cou from SysPoppupRemindInfoConfig  where id_type = 'flowids' and resourceid=" + userid + "and ids = '" + workflowid + "'";
            rs.execute(sqlStr);
            if (rs.next()) {
                int cou = rs.getInt("cou");
                if (cou > 0 && module == 0) {
                    insertorupdate = true;
                }
                if (cou == 0 && module != 0) {
                    insertorupdate = true;
                }
            }
            
            sqlStr = "select * from SystemSet";
            rsrtx.executeSql(sqlStr);
            rsrtx.next();
            oaaddress = rsrtx.getString("oaaddress");
            */
            //sqlStr = "select statistic,typedescription,link from SysPoppupInfo where  type = " + type;

            // 解决rtx消息提醒是ldap登录验证(ldap是没有loginid和password)td13185
            //String mode = Prop.getPropValue(GCONST.getConfigFile(), "authentic");
            /*
            sqlrtx = "select * from hrmresource where id = " + userid;
            rsrtx.executeSql(sqlrtx);
            String isADAccount = "";
            while (rsrtx.next()) {
                loginid = rsrtx.getString("loginid");
                password = rsrtx.getString("password");
                isADAccount = rsrtx.getString("isADAccount");

                // 解决rtx消息提醒是ldap登录验证(ldap是没有loginid和password)td13185
                if (mode != null && mode.equals("ldap") && "1".equals(isADAccount)) {
                    // loginid、account字段整合 qc:128484
                    loginid = rsrtx.getString("loginid");
                    password = rsrtx.getString("loginid");
                    // loginid = rsrtx.getString("account");
                    // password = rsrtx.getString("account");
                }
            }
            */
            //从缓存中获取idap信息，不再查表
            Map<String, String> residapinfo = residapinfos.get(userid + "");
            String isADAccount = "";
            if (residapinfo != null) {
                loginid = residapinfo.get("loginid");
                password =residapinfo.get("password");
                isADAccount = residapinfo.get("isADAccount");
            }
            
            String loginPage = "login/VerifyRtxLogin.jsp";
            String gotoPage = "workflow/request/ViewRequest.jsp";
            if (GCONST.getRTXReminderSet()) {
                loginPage = GCONST.getVerifyRTXLoginPage();
                gotoPage = GCONST.getVerifyRTXGotoPage();
            }
            if (type == 0 || type == 1 || type == 10 || type == 14) {
                para = "/" + gotoPage + "?requestid=" + requestid + "#" + loginid + "#" + password;
            } else {
                //rsrtx.executeSql(sqlStr);
                Map<String, String> sysPoppupInfo = sysPoppupInfos.get(type + "");
                //while (rsrtx.next()) {
                if (sysPoppupInfo != null) {
                    //String link = rsrtx.getString("link");
                    String link = sysPoppupInfo.get("link");
                    para = link + "#" + loginid + "#" + password;
                }
            }
            try {
                para = encrypt(para);

                // 解决rtx消息提醒是ldap登录验证(ldap是没有loginid和password)td13185
                if (mode != null && mode.equals("ldap") && "1".equals(isADAccount)) {
                    rs.executeSql("insert into RtxLdapLoginLog values ('" + loginid + "','" + para + "','0')");
                }
            } catch (Exception e) {
                writeLog(e);
            }
            tempurl = oaaddress + "/" + loginPage + "?para=" + para;
            try {
                Map<String, String> sysPoppupInfo = sysPoppupInfos.get(type + "");
                //while (rsrtx.next()) {
                if (sysPoppupInfo != null) {
                //rs.executeSql(sqlStr);
                //if (rs.next()) {
                    // OA与第三方系统集成流程消息提醒开始 (此处的短信提醒将在 addPoppupRemindInfo(List
                    // list)方法中实现，主要避免触发同时启动N个线程 影响系统性能 )
                    if (type == 0 || type == 1 || type == 10 || type == 14) {
                        BaseBean bean = new BaseBean();
                        String smsinterfaceon = bean.getPropValue("SMSinterface", "smsinterfaceon");
                        if (smsinterfaceon.toUpperCase().equals("Y")) {
                            Map mapsms = new HashMap();
                            mapsms.put("userid", +userid);
                            mapsms.put("requestname", "" + requestname);
                            mapsms.put("creater", "" + creater);
                            mapsms.put("requestid", "" + requestid);
                            mapsms.put("tempurl", "" + tempurl);
                            ulist.add(mapsms);
                        }
                        // new Thread(new
                        // SendMessageWorkRunnable(userid,requestid,tempurl,requestname,creater)).start();
                    }
                    // OA与第三方系统集成流程消息提醒结束
                    boolean ispushmsgflag = false;
                    // 如果有RTX提醒
                    boolean rtxSend = false;
                    RTXConfig config = new RTXConfig();
                    String rtxSip = Util.null2String(config.getPorp(RTXConfig.RTX_SERVER_IP));
                    String RtxOrElinkType = (Util.null2String(config.getPorp(RTXConfig.RtxOrElinkType))).toUpperCase();
                    if (!"".equals(rtxSip)) {
                        NotifyManager nm = new NotifyManager();
                        try {
                            // 是否rtx提醒改为从库中读取的方式(启用rtx提醒并且允许提醒)
                            // RecordSet rsIn=new RecordSet();
                            // rsIn.executeSql("select rtxAlert from
                            // SystemSet");
                            // if(rsIn.next()){
                            String rtxAlert = Util.null2String(config.getPorp("rtxAlert"));
                            if ("1".equals(rtxAlert) && "1".equals(config.getPorp("isusedtx"))) {
                                if (requestname.indexOf("[") > -1) {
                                    requestname = requestname.replaceAll("\\[(.*?)\\]", "($1)");
                                }
                                if (type == 0 || type == 1 || type == 10 || type == 14) {
                                    if (requestname.equals(""))
                                        //requestname = SystemEnv.getHtmlLabelName(rs.getInt("typedescription"), 7);
                                        requestname = SystemEnv.getHtmlLabelName(Util.getIntValue(sysPoppupInfo.get("typedescription")), 7);
                                    else
                                        //requestname = SystemEnv.getHtmlLabelName(rs.getInt("typedescription"), 7) + "：" + requestname;
                                    requestname = SystemEnv.getHtmlLabelName(Util.getIntValue(sysPoppupInfo.get("typedescription")), 7) + "：" + requestname;
                                    tempurl = "[" + requestname + "|" + tempurl + "]";
                                    if (!addRequstids(userid, type, logintype, "" + requestid)) {
                                        if ("ELINK".equals(RtxOrElinkType)) {
                                            //new Thread(new ElinkWorkRunnable(userid, SystemEnv.getHtmlLabelName(rs.getInt("typedescription"), 7), requestid, oaaddress)).start();
                                            new Thread(new ElinkWorkRunnable(userid, SystemEnv.getHtmlLabelName(Util.getIntValue(sysPoppupInfo.get("typedescription")), 7), requestid, oaaddress)).start();
                                            //rtxSend = true;
                                        } else if ("RTX".equals(RtxOrElinkType)){
                                            new Thread(new RTXWorkRunnable(userid, tempurl)).start();
                                            //rtxSend = true;
                                        }
                                    }
                                } else {
                                    //tempurl = "[" + SystemEnv.getHtmlLabelName(rs.getInt("typedescription"), 7) + "|" + tempurl + "]";
                                    tempurl = "[" + SystemEnv.getHtmlLabelName(Util.getIntValue(sysPoppupInfo.get("typedescription")), 7) + "|" + tempurl + "]";
                                    if (!addRequstids(userid, type, logintype, "" + requestid)) {
                                        if ("ELINK".equals(RtxOrElinkType)) {
                                            //new Thread(new ElinkWorkRunnable(userid, SystemEnv.getHtmlLabelName(rs.getInt("typedescription"), 7), requestid, oaaddress)).start();
                                            new Thread(new ElinkWorkRunnable(userid, SystemEnv.getHtmlLabelName(Util.getIntValue(sysPoppupInfo.get("typedescription")), 7), requestid, oaaddress)).start();
                                            //rtxSend = true;
                                        } else if ("RTX".equals(RtxOrElinkType)){
                                            new Thread(new RTXWorkRunnable(userid, tempurl)).start();
                                            //rtxSend = true;
                                        }
                                    }
                                }
                            }
                            // }
                        } catch (Exception e) {
                            writeLog(e);
                            rtxSend = false;
                        }
                    }
                    if (rtxSend)
                        return true;
                    //if ("y".equals(rs.getString("statistic"))) {
                    if ("y".equals(sysPoppupInfo.get("statistic"))) {
                        statistic = true;
                    }
                    // sqlStr = "select * from SysPoppupRemindInfoNew where
                    // userid =" + userid + " and usertype='" + logintype + "'
                    // and type = " + type;
                    // rs.executeSql(sqlStr);
                    //------------------------------------------------------------------------------------------------------
                    // 优化提醒信息插入，如果提醒信息发起者为流程引擎，
                    // 且被提醒用户为内部用户，则直接从缓存中获取其提醒状态（插入 or  更新）
                    // 满足以上条件的提醒信息，不再单条插入或更新， 此处只记录插入和更新的用户id
                    // 待提醒信息全部计算完毕后，一次性插入或更新
                    //
                    // 注意：如果提醒信息发起者不是流程引擎，或者被提醒人为外部用户，还是会单条获取状态、插入或更新
                    //------------------------------------------------------------------------------------------------------
                    if (this.isfromwfengine && "0".equals(logintype)) {
                        if (insertorupdate) {
                            int count = Util.getIntValue(Util.null2String(resourcePoppupRemindRecords.get(userid + "_" + type)), -1);
                            if (count > 0) {
                                StringBuffer updateRemindUserid = updateremindMap.get(String.valueOf(type));
                                if (updateRemindUserid == null) {
                                    updateRemindUserid = new StringBuffer();
                                    updateremindMap.put(String.valueOf(type), updateRemindUserid);
                                }
                                updateRemindUserid.append(",").append(userid);
                                ispushmsgflag = true;
                            } else {
                                StringBuffer insertRemindUserid = insertremindMap.get(String.valueOf(type));
                                if (insertRemindUserid == null) {
                                    insertRemindUserid = new StringBuffer();
                                    insertremindMap.put(String.valueOf(type), insertRemindUserid);
                                }
                                insertRemindUserid.append(",").append(userid);
                                ispushmsgflag = true;
                            }
                        }
                    } else {
                        if (addRequstids(userid, type, logintype, "" + requestid)) {
    
                            if (requestid != -1) {
                                if (insertorupdate) {
                                    sqlStr = "update SysPoppupRemindInfoNew set ifPup=1 ,counts=1 where userid = " + userid + " and usertype = '" + logintype + "' and type = " + type + " and requestid=" + requestid;
                                    ispushmsgflag = true;
                                } else {
                                    sqlStr = "select 1";
                                }
    
                            } else {
                                sqlStr = "update SysPoppupRemindInfoNew set ifPup=ifPup+1 ,counts=counts+1 where userid = " + userid + " and usertype = '" + logintype + "' and type = " + type + " and requestid is null ";
                                // ispushmsgflag = true;
                            }
                        } else {
                            if (requestid != -1) {
                                if (insertorupdate) {
                                    sqlStr = "insert into SysPoppupRemindInfoNew (userid,type,usertype,ifPup,counts,requestid) values (" + userid + "," + type + ",'" + logintype + "',1,1," + requestid + ")";
                                    ispushmsgflag = true;
                                } else {
                                    sqlStr = "select 1";
                                }
                            } else {
                                sqlStr = "insert into SysPoppupRemindInfoNew (userid,type,usertype,ifPup,counts) values (" + userid + "," + type + ",'" + logintype + "',1,1)";
                                // ispushmsgflag = true;
                            }
    
                        }
                        rs.execute(sqlStr);
                    }
                    // 推送消息
                    if (ispushmsgflag) {
                        if (type == 0 || type == 10 || type == 14) {
                            pushmsginfolist.add(map);
                        }
                    }
                } else {
                    writeLog("=====消息提醒未查到需要提醒的RTX和外部短信提醒数据==SQL:" + sqlStr);
                    flag = false;
                }

            } catch (Exception e) {
                writeLog("=====RTX短信提醒有误==");
                flag = false;
                writeLog(e);
            }
        }
        //------------------------------------------------------------------------------------------------------
        // 优化提醒信息插入，如果提醒信息发起者为流程引擎，
        // 且被提醒用户为内部用户，则直接从缓存中获取其提醒状态（插入 or  更新）
        // 满足以上条件的提醒信息，不再单条插入或更新， 此处只记录插入和更新的用户id
        // 待提醒信息全部计算完毕后，一次性插入或更新
        //
        // 注意：如果提醒信息发起者不是流程引擎，或者被提醒人为外部用户，还是会单条获取状态、插入或更新
        //------------------------------------------------------------------------------------------------------
        //----------------------------------------------------
        // 一次性插入提醒信息
        // 一次性更新提醒信息
        // start
        //----------------------------------------------------
        try {
            if (isfromwfengine && remindReqid > 0) {
                
                Set<String> _keyset = insertremindMap.keySet();
                Iterator<String> _iterator = _keyset.iterator();
                while (_iterator.hasNext()) {
                    String _key = _iterator.next();
                    StringBuffer _value = insertremindMap.get(_key);
                    
                    if (_value != null && _value.length() > 1) {
                        String insertRemindSql = "insert into SysPoppupRemindInfoNew(userid,type,usertype,ifPup,counts,requestid) SELECT id, " + _key + ", 0, 1, 1, " + remindReqid + " FROM HrmResource WHERE " + Util.getSubINClause(_value.toString().substring(1), "id",  "IN");
                        rs.executeSql(insertRemindSql);
                    }
                }
                
                
                Set<String> _keyset2 = updateremindMap.keySet();
                Iterator<String> _iterator2 = _keyset2.iterator();
                while (_iterator2.hasNext()) {
                    String _key = _iterator2.next();
                    StringBuffer _value = updateremindMap.get(_key);
                    
                    if (_value != null && _value.length() > 1) {
                        String updateRemindSql = "update SysPoppupRemindInfoNew set ifPup=1 ,counts=1 where (" + Util.getSubINClause(_value.toString().substring(1), "userid",  "IN") + ") and usertype='0' and type=" + _key + " and requestid=" + remindReqid;
                        rs.executeSql(updateRemindSql);
                    }
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        //----------------------------------------------------
        // end
        //----------------------------------------------------
        try {
            //strat--通过微信集成平台发送消息提醒,具体是否发送如何发送由微信集成平台本身来定--康渊炎
            weaver.weixin.sdkforthird.WechatApiForEc.sendPopupRemind(list);
            //end 通过微信集成平台发送消息提醒,具体是否发送如何发送由微信集成平台本身来定	
            //只有选择IM集成类型是其他的时候才进行判断
            boolean validOfRTX = false;
            RTXConfig config = new RTXConfig();
            String RtxOrElinkType = (Util.null2String(config.getPorp(RTXConfig.RtxOrElinkType))).toUpperCase();
//            String temStr = Util.null2String(config.getPorp(RTXConfig.RTX_SERVER_IP));
            if ("OTHER".equals(RtxOrElinkType)) {
                if ("1".equals(config.getPorp("rtxAlert")) && "1".equals(config.getPorp("isusedtx"))) {
                    validOfRTX = true;
                }
            }
            if (validOfRTX) {
                new Thread(new SendMessageWorkRunnable(list)).start();
            }
        } catch (Exception e) {
            flag = false;
            writeLog(e);
        }

        if (!pushmsginfolist.isEmpty()) {
            try {
                WFNotificationService wfnotservice = new WFNotificationService();
                wfnotservice.pushMsgs(pushmsginfolist);
                
                new WFPathUtil().getFixedThreadPool().execute(wfnotservice);
                //new Thread(wfnotservice).start();
            } catch (Exception e) {
                e.printStackTrace();
                this.writeLog("消息接口调用失败");
            }
        }

        return flag;
    }
    
    /**
     * 生成新的提醒信息
     * 
     * @param userid
     *            和某类别提醒相关联的用户id (如流程操作者,文档审批人......
     * @param type
     *            提醒类别代码
     * @param logintype
     *            用户类型 (0: 内部, 1:外部)
     * @param requestid
     *            该提醒信息若生成requestid(比如流程提醒)则为具体的请求id, 不生成requestid的在调用该方法时设置为 -1
     * @param requestname
     * 			  流程名           
     * @return 是否生成新的提醒信息
     */
    public boolean addPoppupRemindInfo(int userid, int type, String logintype, int requestid,String requestname,int workflowid)
    {
    	boolean flag = true;
    	try
	    {
	    	List poppuplist=new ArrayList();
	        Map map=new HashMap();
		    map.put("userid",""+userid);
		    map.put("type",""+type);
		    map.put("logintype",""+logintype);
		    map.put("requestid",""+requestid);
		    map.put("requestname",""+requestname);
		    map.put("workflowid",""+workflowid);
		    map.put("creater","");
		    poppuplist.add(map);
		    flag=this.insertPoppupRemindInfo(poppuplist);
	    }catch (Exception e){
          flag = false;
          writeLog(e);
	    }
	    return flag;
    }

    /**
     * 各类提醒操作后更新相关数据
     * 
     * @param userid
     *            和某类别提醒相关联的用户id (如流程操作者,文档审批人......
     * @param type
     *            提醒类别代码
     * @param logintype
     *            用户类型 (0: 内部, 1:外部)
     * @param requestid
     *            该提醒信息若生成requestid(比如流程提醒)则为具体的请求id, 不生成requestid的在调用该方法时设置为 -1
     * @return 是否更新相关数据成功
     */
    public boolean updatePoppupRemindInfo(int userid, int type, String logintype, int requestid)
    {
        boolean flag = true;
        boolean statistic = false;
        sqlStr = "select statistic from SysPoppupInfo where  type = " + type;
        try
        {
            rs.executeSql(sqlStr);
            if (rs.next())
            {
                if ("y".equals(rs.getString("statistic")))
                {
                    statistic = true;
                }
             
                    if (statistic)
                    { // 需要统计


                        if (requestid != -1)
						{
						 sqlStr = "delete from  SysPoppupRemindInfoNew  where userid = " + userid+ " and usertype = '" + logintype + "' and type = " + type+" and requestid="+requestid;
						}
                        else
						{
						rs1.executeSql("select * from SysPoppupRemindInfoNew where userid = " + userid + " and usertype = '" + logintype + "' and type = " + type);
						if (rs.getInt("counts")<2)
						sqlStr = "delete SysPoppupRemindInfoNew where userid = " + userid + " and usertype = '" + logintype + "' and type = " + type;
						else
						sqlStr = "update SysPoppupRemindInfoNew set counts=counts - 1  where userid = " + userid + " and usertype = '" + logintype + "' and type = " + type;
						
						}
                        
					    
                       rs1.executeSql(sqlStr);
                       
                       

                    }
                    else
                    { // 不需要统计时删除该条记录
                        sqlStr = "delete SysPoppupRemindInfoNew where userid = " + userid + " and usertype = '" + logintype + "' and type = " + type;
                        rs1.executeSql(sqlStr);
                    }

              
            }
            else
            {
                flag = false;
            }

        }
        catch (Exception e)
        {
            flag = false;
            writeLog(e);
        }
        return flag;
    }

	/*
	删除流程时删除提醒
	
	*/
	public  void deletePoppupRemindInfo (int requestid,int type)
	{
	 rs.executeSql("delete from SysPoppupRemindInfoNew where type="+type+" and  requestid="+requestid);
	}
	
	/**
	 * 加密
	 * @param str
	 * @return
	 */
	public static String encrypt(String str){
		/*
		String temp = "";
		char ch[] = str.toCharArray();
		for(int i=0;i<ch.length;i++){
			temp = temp + AsciiAddOne(ch[i]);
		}
		return temp;
		*/
		String password=new BaseBean().getPropValue("AESpassword", "pwd");
		if(password.equals("")){
			password="1";
		}
		return AES.encrypt(str,password);	
	}
	/**
	 * 解密
	 * @param str
	 * @return
	 */
	public static String decrypt(String str){
		/*
		String temp = "";
		char ch[] = str.toCharArray();
		for(int i=0;i<ch.length;i++){
			temp = temp + AsciiSubOne(ch[i]);
		}
		return temp;
		*/
		String password=new BaseBean().getPropValue("AESpassword", "pwd");
		if(password.equals("")){
			password="1";
		}
		return AES.decrypt(str,password);
	}
	
	/**
	 * ascii + 1
	 * @param c
	 * @return
	 */
	public static String AsciiAddOne(char c){
		c = (char)((int)c + 1);		
		
		return c + "";
	}
	/**
	 * ascii - 1 
	 * @param c
	 * @return
	 */
	public static String AsciiSubOne(char c){
		c = (char)((int)c - 1);		
		
		return c + "";		
	}

    public boolean isIsfromwfengine() {
        return isfromwfengine;
    }

    public void setIsfromwfengine(boolean isfromwfengine) {
        this.isfromwfengine = isfromwfengine;
    }
}