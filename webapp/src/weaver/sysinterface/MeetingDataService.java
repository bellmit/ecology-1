package weaver.sysinterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import weaver.conn.RecordSet;
import weaver.file.FileUpload;
import weaver.general.BaseBean;
import weaver.general.TimeUtil;
import weaver.general.Util;
import weaver.hrm.HrmUserVarify;
import weaver.hrm.User;
import weaver.hrm.moduledetach.ManageDetachComInfo;
import weaver.hrm.resource.ResourceComInfo;
import weaver.meeting.MeetingBrowser;
import weaver.meeting.MeetingLog;
import weaver.meeting.MeetingShareUtil;
import weaver.meeting.MeetingUtil;
import weaver.meeting.MeetingViewer;
import weaver.meeting.Maint.MeetingComInfo;
import weaver.meeting.Maint.MeetingInterval;
import weaver.meeting.Maint.MeetingRoomComInfo;
import weaver.meeting.Maint.MeetingSetInfo;
import weaver.meeting.Maint.MeetingTransMethod;
import weaver.meeting.defined.MeetingCreateWFUtil;
import weaver.meeting.defined.MeetingFieldManager;
import weaver.meeting.defined.MeetingWFUtil;
import weaver.meeting.remind.MeetingRemindUtil;
import weaver.system.SysRemindWorkflow;
import weaver.systeminfo.SystemEnv;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import com.weaver.formmodel.mobile.MobileFileUpload;
import com.weaver.formmodel.mobile.utils.MobileCommonUtil;
import com.weaver.formmodel.util.DateHelper;

public class MeetingDataService extends BaseService {
	private HttpServletRequest request;
	private  HttpServletResponse response;
	private final static String regxpForHtml = "<([^>]*)>"; // 过滤所有以<开头以>结尾的标签  
	
	public MeetingDataService(HttpServletRequest request, HttpServletResponse response){
		this.request = request;
		this.response = response;
	}
	
	public void createMeeting(User user) throws Exception{	
		
		String method = Util.null2String(request.getParameter("method"));
		String meetingtype=Util.null2String(request.getParameter("meetingtype"));//会议类型
		String meetingid=Util.null2String(request.getParameter("meetingid"));
		String CurrentUser = user.getUID()+"";
		//基本信息
		String name=Util.null2String(request.getParameter("name"));//会议名称
		name = filterHtml(name);
		String caller=Util.null2String(request.getParameter("caller"));//召集人,必填
		String contacter=Util.null2String(request.getParameter("contacter"));//联系人,空值使用当前操作人
		if("".equals(contacter)) contacter=CurrentUser;
		
		int roomType = 1;
		String address=Util.null2String(request.getParameter("address"));//会议地点
		String customizeAddress = Util.null2String(request.getParameter("customizeaddress"));
		if(!"".equals(address)){//优先选择会议室
			customizeAddress="";
		}else{//自定义会议室
			roomType=2;
		}
		String desc=Util.htmlFilter4UTF8(Util.spacetoHtml(Util.null2String(request.getParameter("desc_n"))));//描述,可为空
		 
		//时间
		int repeatType = Util.getIntValue(request.getParameter("repeattype"),0);//是否是重复会议,0 正常会议.
		String begindate=Util.null2String(request.getParameter("begindate"));
		String enddate=Util.null2String(request.getParameter("enddate"));
		if(repeatType>0){
			begindate=Util.null2String(request.getParameter("repeatbegindate"));
			enddate=Util.null2String(request.getParameter("repeatenddate"));
		}
		String begintime=Util.null2String(request.getParameter("begintime"));
		String endtime=Util.null2String(request.getParameter("endtime"));
		//提醒方式和时间
		String remindTypeNew=Util.null2String(request.getParameter("remindtypenew"));//新的提示方式
		int remindImmediately = Util.getIntValue(request.getParameter("remindimmediately"),0);  //是否立即提醒 
		int remindBeforeStart = Util.getIntValue(request.getParameter("remindbeforestart"),0);  //是否开始前提醒
		int remindBeforeEnd = Util.getIntValue(request.getParameter("remindbeforeend"),0);  //是否结束前提醒
		int remindHoursBeforeStart = Util.getIntValue(request.getParameter("remindhoursbeforestart"),0);//开始前提醒小时
		int remindTimesBeforeStart = Util.getIntValue(Util.null2String(request.getParameter("remindtimesbeforestart")),0);  //开始前提醒时间
	    int remindHoursBeforeEnd = Util.getIntValue(request.getParameter("remindhoursbeforeend"),0);//结束前提醒小时
	    int remindTimesBeforeEnd = Util.getIntValue(Util.null2String(request.getParameter("remindtimesbeforeend")),0);  //结束前提醒时间
		//参会人员
	    String hrmmembers=Util.null2String(request.getParameter("hrmmembers"));//参会人员
	    int totalmember=Util.getIntValue(request.getParameter("totalmember"),0);//参会人数
		String othermembers=Util.fromScreen(request.getParameter("othermembers"),user.getLanguage());//其他参会人员
		String crmmembers=Util.null2String(request.getParameter("crmmembers"));//参会客户
		int crmtotalmember=Util.getIntValue(request.getParameter("crmtotalmember"),0);//参会人数
		//其他信息
		String projectid=Util.null2String(request.getParameter("projectid"));	//加入了项目id
//		String accessorys=Util.null2String(request.getParameter("field35"));	//系统附件
		
		
//		accessorys = meetingfile2doc(request,user);
		//自定义字段
		int remindType = 1;  //老的提醒方式,默认1不提醒
	    
		//重复策略字段
		int repeatdays = Util.getIntValue(request.getParameter("repeatdays"),0);
		int repeatweeks = Util.getIntValue(request.getParameter("repeatweeks"),0);
		String rptWeekDays=Util.null2String(request.getParameter("rptWeekdays"));
		int repeatmonths = Util.getIntValue(request.getParameter("repeatmonths"),0);
		int repeatmonthdays = Util.getIntValue(request.getParameter("repeatmonthdays"),0);
		int repeatStrategy = Util.getIntValue(request.getParameter("repeatstrategy"),0);
		JSONObject jsonObject = chkRoom(user);
		String code = Util.null2String(jsonObject.get("code"));
		if(code.equals("1")){
			this.returnValue(jsonObject, response);
			return ;
		}
		
		MeetingRoomComInfo MeetingRoomComInfo= new MeetingRoomComInfo();
		char flag = 2;
		String ProcPara = "";
		
		String CurrentDate = DateHelper.getCurrentDate();
		String CurrentTime = DateHelper.getCurDateTime();
		
	    String description = "您有会议: "+name+"   会议时间:"+begindate+" "+begintime+" 会议地点:"+MeetingRoomComInfo.getMeetingRoomInfoname(""+address)+customizeAddress;
	    ProcPara =  meetingtype;
		ProcPara += flag + name;
		ProcPara += flag + caller;
		ProcPara += flag + contacter;
		ProcPara += flag + projectid; //加入项目id
		ProcPara += flag + address;
		ProcPara += flag + begindate;
		ProcPara += flag + begintime;
		ProcPara += flag + enddate;
		ProcPara += flag + endtime;
		ProcPara += flag + desc;
		ProcPara += flag + CurrentUser;
		ProcPara += flag + CurrentDate;
		ProcPara += flag + CurrentTime;
	    ProcPara += flag + ""+totalmember;
	    ProcPara += flag + othermembers;
	    ProcPara += flag + "";
	    ProcPara += flag + description;
	    ProcPara += flag + ""+remindType;
	    ProcPara += flag + ""+remindBeforeStart;
	    ProcPara += flag + ""+remindBeforeEnd;
	    ProcPara += flag + ""+remindTimesBeforeStart;
	    ProcPara += flag + ""+remindTimesBeforeEnd;
	    ProcPara += flag + customizeAddress;
	    
	    RecordSet RecordSet = new RecordSet();
	    if (RecordSet.getDBType().equals("oracle"))
		{
			RecordSet.executeProc("Meeting_Insert",ProcPara);
	    
			RecordSet.executeSql("SELECT max(id) FROM Meeting where creater = "+CurrentUser);
		}
		else
		{
			RecordSet.executeProc("Meeting_Insert",ProcPara);
		}
		RecordSet.next();
		String MaxID = RecordSet.getString(1);

		String updateSql = "update Meeting set repeatType = " + repeatType 
						+" , repeatdays = "+ repeatdays 
						+" , repeatweeks = "+ repeatweeks 
						+" , rptWeekDays = '"+ rptWeekDays +"' "
						+" , repeatbegindate = '"+ begindate +"' "
						+" , repeatenddate = '"+ enddate +"' "
						+" , repeatmonths = "+ repeatmonths 
						+" , repeatmonthdays = "+ repeatmonthdays
						+" , repeatStrategy = "+ repeatStrategy
						+" , roomType = "+ roomType
						+" , remindTypeNew = '"+ remindTypeNew+"' "
						+" , remindImmediately = "+ remindImmediately
						+" , remindHoursBeforeStart = "+ remindHoursBeforeStart
						+" , remindHoursBeforeEnd = "+ remindHoursBeforeEnd
						+" , hrmmembers = '"+ hrmmembers+"' "
						+" , crmmembers = '"+ crmmembers+"' "
						+" , crmtotalmember = "+ crmtotalmember
//						+" , accessorys = '"+ accessorys+"' "
						+" where id = " + MaxID;
		RecordSet.executeSql(updateSql);
		//保存自定义字段
		MeetingFieldManager mfm=new MeetingFieldManager(1);
		mfm.editCustomData(request,Util.getIntValue(MaxID));
		ArrayList arrayhrmids02 = Util.TokenizerString(hrmmembers,",");
		for(int i=0;i<arrayhrmids02.size();i++){
			ProcPara =  MaxID;
			ProcPara += flag + "1";
			ProcPara += flag + "" + arrayhrmids02.get(i);
			ProcPara += flag + "" + arrayhrmids02.get(i);
			RecordSet.executeProc("Meeting_Member2_Insert",ProcPara);
			//标识会议是否查看过
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("INSERT INTO Meeting_View_Status(meetingId, userId, userType, status) VALUES(");
			stringBuffer.append(MaxID);
			stringBuffer.append(", ");
			stringBuffer.append(arrayhrmids02.get(i));
			stringBuffer.append(", '");
			stringBuffer.append("1");
			stringBuffer.append("', '");
			if(CurrentUser.equals(arrayhrmids02.get(i)))
			//当前操作用户表示已看
			{
			    stringBuffer.append("1");
			}
			else
			{
			    stringBuffer.append("0");
			}
			stringBuffer.append("')");
			RecordSet.executeSql(stringBuffer.toString());
		}

		ArrayList arraycrmids02 = Util.TokenizerString(crmmembers,",");
		for(int i=0;i<arraycrmids02.size();i++){
			String membermanager="";
			RecordSet.executeProc("CRM_CustomerInfo_SelectByID",""+arraycrmids02.get(i));
			if(RecordSet.next()) membermanager=RecordSet.getString("manager");
			ProcPara =  MaxID;
			ProcPara += flag + "2";
			ProcPara += flag + "" + arraycrmids02.get(i);
			ProcPara += flag + membermanager;
			RecordSet.executeProc("Meeting_Member2_Insert",ProcPara);
		}
		//会议议程
		int topicrows=Util.getIntValue(Util.null2String(request.getParameter("topicrows")),0);
		if(topicrows>0){
			MeetingFieldManager mfm2=new MeetingFieldManager(2);
			for(int i=1;i<=topicrows;i++){
				mfm2.editCustomDataDetail(request,0,i,Util.getIntValue(MaxID));
			}
		}
		//会议服务
		int servicerows=Util.getIntValue(Util.null2String(request.getParameter("servicerows")),0);
		if(servicerows>0){
			MeetingFieldManager mfm3=new MeetingFieldManager(3);
			for(int i=1;i<=servicerows;i++){
				mfm3.editCustomDataDetail(request,0,i,Util.getIntValue(MaxID));
			}
		}
		MeetingViewer MeetingViewer = new MeetingViewer();
		MeetingViewer.setMeetingShareById(""+MaxID);
		MeetingComInfo MeetingComInfo = new MeetingComInfo();
		MeetingComInfo.removeMeetingInfoCache();
		MeetingUtil MeetingUtil = new MeetingUtil();
		MeetingLog meetingLog = new MeetingLog();
		MeetingCreateWFUtil MeetingCreateWFUtil = new MeetingCreateWFUtil();
		String ClientIP = request.getRemoteAddr();
		MeetingSetInfo meetingSetInfo = new MeetingSetInfo();
		int days = meetingSetInfo.getDays();
		//文档和附件的共享明细
		MeetingUtil.meetingDocShare(MaxID);
		
//		if(method.equals("add")){
//			meetingLog.resetParameter();
//	    	meetingLog.insSysLogInfo(user,Util.getIntValue(MaxID),name,"新建草稿会议"+(repeatType>0?"模板":""),"303","1",1,Util.getIpAddr(request));
//		}
		String approvewfid = "";
		String formid = "";
		//2004年4月17日，根据会议类型所对应的工作流判断是否需要触发审批工作流
//	    if(method.equals("addSubmit")){
		if(true){
	    	//新建会议日志
	        if(!meetingtype.equals("")){
	        	if(repeatType>0){//周期会议,查看周期会议审批流程
	        		RecordSet.executeSql("Select approver1,formid From Meeting_Type t1 join workflow_base t2 on t1.approver1=t2.id  where t1.approver1>0 and t1.ID ="+meetingtype);
	        	}else{
	        		RecordSet.executeSql("Select approver,formid From Meeting_Type t1 join workflow_base t2 on t1.approver=t2.id  where t1.approver>0 and t1.ID ="+meetingtype);
	        	}
	            RecordSet.next();
	            approvewfid = RecordSet.getString(1);
	            formid=RecordSet.getString(2);
	        }
	        if(!approvewfid.equals("0")&&!approvewfid.equals("")){
	        	meetingLog.resetParameter();
	        	meetingLog.insSysLogInfo(user,Util.getIntValue(MaxID),name,"新建审批会议","303","1",1,Util.getIpAddr(request));
	        	if("85".equals(formid)){//原系统表单
	        	}else{//新表单,通过Action统一处理
	        		MeetingCreateWFUtil.createWF(MaxID,user,approvewfid,ClientIP);
	        		JSONObject json = new JSONObject();
	        		json.put("meetingid", MaxID);
		            this.returnValue(json, response);
	        		return;
	        	}
	        }else{
	        	
		        RecordSet.executeSql("Update Meeting Set meetingstatus = 2 WHERE id="+MaxID);//更新会议状态为正常
		        if(repeatType == 0){
		        	meetingLog.resetParameter();
		        	meetingLog.insSysLogInfo(user,Util.getIntValue(MaxID),name,"新建正常会议","303","1",1,Util.getIpAddr(request));
		        	//生成会议日程和会议提醒
		            MeetingInterval.createWPAndRemind(MaxID,null,request.getRemoteAddr());
		            JSONObject json = new JSONObject();
		            json.put("meetingid", MaxID);
		            this.returnValue(json, response);
		            return;
	            } else {
	            	meetingLog.resetParameter();
	            	meetingLog.insSysLogInfo(user,Util.getIntValue(MaxID),name,"新建会议模板","303","1",1,Util.getIpAddr(request));
					int intervaltime = 0;
					String otherinfo = "";
					if(repeatType == 1){
						intervaltime = repeatdays;
					} else if(repeatType == 2){
						intervaltime = repeatweeks;
						otherinfo = rptWeekDays;
					}else if(repeatType == 3){
						intervaltime = repeatmonths;
						otherinfo = "" + repeatmonthdays;
					}
	            	MeetingInterval.updateMeetingRepeat(days,MaxID,begindate,enddate,""+repeatType,intervaltime,otherinfo,repeatStrategy);
	            }
	        }
	    }
	    JSONObject json = new JSONObject();
        json.put("meetingid", MaxID);
        json.put("code", "0");
        json.put("message", "成功");
        this.returnValue(json, response);
	    return;
}
	public void submintMeeting(User user) throws Exception{//ViewMeeting.jsp 页面直接提交
		String meetingid = Util.null2String(request.getParameter("meetingid"));
	    if(!meetingid.equals("")) {
	    	MeetingSetInfo meetingSetInfo = new MeetingSetInfo();
	    	int days = meetingSetInfo.getDays();
	        RecordSet rs = new RecordSet();
	        rs.executeSql("Update Meeting Set meetingstatus = 2 WHERE id="+meetingid);//更新会议状态为正常
	        rs.executeProc("Meeting_SelectByID",meetingid);
		    rs.next();
		    String name=rs.getString("name");
		    String begindate=rs.getString("begindate");
		    String enddate=rs.getString("enddate");
		    
		    int repeatType = Util.getIntValue(rs.getString("repeattype"),0);
			int repeatdays = Util.getIntValue(rs.getString("repeatdays"),0);
			int repeatweeks = Util.getIntValue(rs.getString("repeatweeks"),0);
			int repeatmonths = Util.getIntValue(rs.getString("repeatmonths"),0);
			int repeatmonthdays = Util.getIntValue(rs.getString("repeatmonthdays"),0);
			int repeatStrategy = Util.getIntValue(rs.getString("repeatstrategy"),0);
			String rptWeekDays = rs.getString("rptWeekDays");
			MeetingLog meetingLog = new MeetingLog();
			if(repeatType == 0){
				meetingLog.resetParameter();
				meetingLog.insSysLogInfo(user,Util.getIntValue(meetingid),name,"提交会议","303","2",1,Util.getIpAddr(request));
				//生成会议日程和会议提醒
				MeetingInterval.createWPAndRemind(meetingid,null,request.getRemoteAddr());
			 } else {
				int intervaltime = 0;
				String otherinfo = "";
				if(repeatType == 1){
					intervaltime = repeatdays;
				} else if(repeatType == 2){
					intervaltime = repeatweeks;
					otherinfo = rptWeekDays;
				}else if(repeatType == 3){
					intervaltime = repeatmonths;
					otherinfo = "" + repeatmonthdays;
				}
				meetingLog.resetParameter();
				meetingLog.insSysLogInfo(user,Util.getIntValue(meetingid),name,"提交会议模板","303","2",1,Util.getIpAddr(request));
				MeetingInterval.updateMeetingRepeat(days,meetingid,begindate,enddate,""+repeatType,intervaltime,otherinfo,repeatStrategy);
			}
			MeetingViewer MeetingViewer = new MeetingViewer();
			MeetingComInfo MeetingComInfo = new MeetingComInfo();
	        MeetingViewer.setMeetingShareById(meetingid);
			MeetingComInfo.removeMeetingInfoCache();
	    }
	    JSONObject json = new JSONObject();
        json.put("meetingid", meetingid);
        this.returnValue(json, response);
	    return;
}
	
	public void deleteMeeting(User user) throws Exception{
		String meetingid = Util.null2String(request.getParameter("meetingid"));
		RecordSet RecordSet = new RecordSet();
	    RecordSet.executeSql("select requestid,name,meetingtype,repeattype,caller,creater,contacter From meeting where meetingstatus=0 and id="+meetingid);
	    int requestid=0;
	    int meetingtype1=0;
	    if(RecordSet.next()){
	       requestid=Integer.valueOf(Util.null2String(RecordSet.getString("requestid"))).intValue();
	       meetingtype1=RecordSet.getInt("meetingtype");
	       String caller=Util.null2String(RecordSet.getString("caller"));
	       String creater=Util.null2String(RecordSet.getString("creater"));
	       String contacter=Util.null2String(RecordSet.getString("contacter"));
	       MeetingShareUtil MeetingShareUtil = new MeetingShareUtil();
	       String allUser=MeetingShareUtil.getAllUser(user);
	       //有编辑权限的人员才可删除会议
	       if((MeetingShareUtil.containUser(allUser,caller)|| MeetingShareUtil.containUser(allUser,contacter)||MeetingShareUtil.containUser(allUser,creater))){
	    	   MeetingLog meetingLog = new MeetingLog();
	    	   meetingLog.resetParameter();
	       	   meetingLog.insSysLogInfo(user,Util.getIntValue(meetingid),RecordSet.getString("name"),"删除会议","303","3",1,Util.getIpAddr(request));
	       		if(RecordSet.getInt("repeattype")>0){//周期会议,查看周期会议审批流程
	        		RecordSet.executeSql("Select formid From Meeting_Type t1 join workflow_base t2 on t1.approver1=t2.id  where t1.approver1>0 and t1.ID ="+meetingtype1);
	        	}else{
	        		RecordSet.executeSql("Select formid From Meeting_Type t1 join workflow_base t2 on t1.approver=t2.id  where t1.approver>0 and t1.ID ="+meetingtype1);
	        	}
	       		if(RecordSet.next()){
	       			int fromid=RecordSet.getInt("formid");
	       		    if(requestid>0){
	    	   			MeetingWFUtil.deleteWF(requestid,meetingid,fromid);
	       		        RecordSet.executeSql("delete From workflow_currentoperator where requestid="+requestid);
	       		    }
	       		}
	       		MeetingWFUtil.deleteMeeting(meetingid);
	       		MeetingComInfo MeetingComInfo = new MeetingComInfo();
	       	    MeetingComInfo.removeMeetingInfoCache();
	       }
	    }
	    
	    JSONObject json = new JSONObject();
        json.put("meetingid", meetingid);
        json.put("code", "0");
        json.put("message", "成功");
        this.returnValue(json, response);
		return;

	}
	
	public void editMeeting(User user) throws Exception{	
		String CurrentUser = user.getUID()+"";
		char flag = 2;
		String ProcPara = "";
		String meetingid=Util.null2String(request.getParameter("meetingid"));
		//基本信息
		String name=Util.null2String(request.getParameter("name"));//会议名称
		name = filterHtml(name);
		String caller=Util.null2String(request.getParameter("caller"));//召集人,必填
		String contacter=Util.null2String(request.getParameter("contacter"));//联系人,空值使用当前操作人
		if("".equals(contacter)) contacter=CurrentUser;
		
		int roomType = 1;
		String address=Util.null2String(request.getParameter("address"));//会议地点
		String customizeAddress = Util.null2String(request.getParameter("customizeaddress"));
		if(!"".equals(address)){//优先选择会议室
			customizeAddress="";
		}else{//自定义会议室
			roomType=2;
		}
		String desc=Util.htmlFilter4UTF8(Util.spacetoHtml(Util.null2String(request.getParameter("desc_n"))));//描述,可为空
		//时间
		int repeatType = Util.getIntValue(request.getParameter("repeattype"),0);//是否是重复会议,0 正常会议.
		String begindate=Util.null2String(request.getParameter("begindate"));
		String enddate=Util.null2String(request.getParameter("enddate"));
		if(repeatType>0){
			begindate=Util.null2String(request.getParameter("repeatbegindate"));
			enddate=Util.null2String(request.getParameter("repeatenddate"));
		}
		String begintime=Util.null2String(request.getParameter("begintime"));
		String endtime=Util.null2String(request.getParameter("endtime"));
		//提醒方式和时间
		String remindTypeNew=Util.null2String(request.getParameter("remindtypenew"));//新的提示方式
		int remindImmediately = Util.getIntValue(request.getParameter("remindimmediately"),0);  //是否立即提醒 
		int remindBeforeStart = Util.getIntValue(request.getParameter("remindbeforestart"),0);  //是否开始前提醒
		int remindBeforeEnd = Util.getIntValue(request.getParameter("remindbeforeend"),0);  //是否结束前提醒
		int remindHoursBeforeStart = Util.getIntValue(request.getParameter("remindHoursbeforestart"),0);//开始前提醒小时
		int remindTimesBeforeStart = Util.getIntValue(Util.null2String(request.getParameter("remindtimesbeforestart")),0);  //开始前提醒时间
	    int remindHoursBeforeEnd = Util.getIntValue(request.getParameter("remindhoursbeforeend"),0);//结束前提醒小时
	    int remindTimesBeforeEnd = Util.getIntValue(Util.null2String(request.getParameter("remindtimesbeforeend")),0);  //结束前提醒时间
		//参会人员
	    String hrmmembers=Util.null2String(request.getParameter("hrmmembers"));//参会人员
	    int totalmember=Util.getIntValue(request.getParameter("totalmember"),0);//参会人数
		String othermembers=Util.fromScreen(request.getParameter("othermembers"),user.getLanguage());//其他参会人员
		String crmmembers=Util.null2String(request.getParameter("crmmembers"));//参会客户
		int crmtotalmember=Util.getIntValue(request.getParameter("crmtotalmember"),0);//参会人数
		//其他信息
		String projectid=Util.null2String(request.getParameter("projectid"));	//加入了项目id
//		String accessorys=Util.null2String(request.getParameter("field35"));	//系统附件
		
//		accessorys = meetingfile2doc(request,user);
		//自定义字段
		int remindType = 1;  //老的提醒方式,默认1不提醒
	    
		//重复策略字段
		int repeatdays = Util.getIntValue(request.getParameter("repeatdays"),0);
		int repeatweeks = Util.getIntValue(request.getParameter("repeatweeks"),0);
		String rptWeekDays=Util.null2String(request.getParameter("rptweekdays"));
		int repeatmonths = Util.getIntValue(request.getParameter("repeatmonths"),0);
		int repeatmonthdays = Util.getIntValue(request.getParameter("repeatmonthdays"),0);
		int repeatStrategy = Util.getIntValue(request.getParameter("repeatstrategy"),0);
		MeetingRoomComInfo MeetingRoomComInfo = new MeetingRoomComInfo();
		String description= "您有会议: "+name+"   会议时间:"+begindate+" "+begintime+" 会议地点:"+MeetingRoomComInfo.getMeetingRoomInfoname(""+address)+customizeAddress;
		ProcPara +=  meetingid;
		ProcPara += flag + name;
		ProcPara += flag + caller;
		ProcPara += flag + contacter;
		ProcPara += flag + projectid;	//加入修改字段
		ProcPara += flag + address;
		ProcPara += flag + begindate;
		ProcPara += flag + begintime;
		ProcPara += flag + enddate;
		ProcPara += flag + endtime;
		ProcPara += flag + desc;
	    ProcPara += flag + ""+totalmember;
	    ProcPara += flag + othermembers;
	    ProcPara += flag + "";
	    ProcPara += flag + description;
	    ProcPara += flag + ""+remindType;
	    ProcPara += flag + ""+remindBeforeStart;
	    ProcPara += flag + ""+remindBeforeEnd;
	    ProcPara += flag + ""+remindTimesBeforeStart;
	    ProcPara += flag + ""+remindTimesBeforeEnd;
	    ProcPara += flag + customizeAddress;
	    RecordSet RecordSet = new RecordSet();
		RecordSet.executeProc("Meeting_Update",ProcPara);
		String meetingtype=Util.null2String(request.getParameter("meetingtype"));//会议类型
		String updateSql = "update Meeting set meetingtype='"+meetingtype+"',repeatType = " + repeatType 
						+" , repeatdays = "+ repeatdays 
						+" , repeatweeks = "+ repeatweeks 
						+" , rptWeekDays = '"+ rptWeekDays +"' "
						+" , repeatbegindate = '"+ begindate +"' "
						+" , repeatenddate = '"+ enddate +"' "
						+" , repeatmonths = "+ repeatmonths 
						+" , repeatmonthdays = "+ repeatmonthdays
						+" , repeatStrategy = "+ repeatStrategy
						+" , roomType = "+ roomType
						+" , remindTypeNew = '"+ remindTypeNew+"' "
						+" , remindImmediately = "+ remindImmediately
						+" , remindHoursBeforeStart = "+ remindHoursBeforeStart
						+" , remindHoursBeforeEnd = "+ remindHoursBeforeEnd
						+" , hrmmembers = '"+ hrmmembers+"' "
						+" , crmmembers = '"+ crmmembers+"' "
						+" , crmtotalmember = "+ crmtotalmember
//						+" , accessorys = '"+ accessorys+"' "
						+" where id = " + meetingid;
		RecordSet.executeSql(updateSql);
		//保存自定义字段
		MeetingFieldManager mfm=new MeetingFieldManager(1);
		mfm.editCustomData(request,Util.getIntValue(meetingid));
		
		//删除会议人员
		RecordSet.executeProc("Meeting_Member2_Delete",meetingid);
			
		//删除会议中相关的标识是否查看的信息
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("DELETE FROM Meeting_View_Status WHERE meetingId = ");
		stringBuffer.append(meetingid);
		RecordSet.executeSql(stringBuffer.toString());
		

		ArrayList arrayhrmids02 = Util.TokenizerString(hrmmembers,",");
		for(int i=0;i<arrayhrmids02.size();i++){
			ProcPara =  meetingid;
			ProcPara += flag + "1";
			ProcPara += flag + "" + arrayhrmids02.get(i);
			ProcPara += flag + "" + arrayhrmids02.get(i);
			RecordSet.executeProc("Meeting_Member2_Insert",ProcPara);
			
			//标识会议是否查看过
			stringBuffer = new StringBuffer();
			stringBuffer.append("INSERT INTO Meeting_View_Status(meetingId, userId, userType, status) VALUES(");
			stringBuffer.append(meetingid);
			stringBuffer.append(", ");
			stringBuffer.append(arrayhrmids02.get(i));
			stringBuffer.append(", '");
			stringBuffer.append("1");
			stringBuffer.append("', '");
			if(CurrentUser.equals(arrayhrmids02.get(i)))
			//当前操作用户表示已看
			{
			    stringBuffer.append("1");
			}
			else
			{
			    stringBuffer.append("0");
			}
			stringBuffer.append("')");
			RecordSet.executeSql(stringBuffer.toString());
		}
		String Sql = "";
		ArrayList arraycrmids02 = Util.TokenizerString(crmmembers,",");
		for(int i=0;i<arraycrmids02.size();i++){
			String membermanager="";
			RecordSet.executeProc("CRM_CustomerInfo_SelectByID",""+arraycrmids02.get(i));
			if(RecordSet.next()) membermanager=RecordSet.getString("manager");
			ProcPara =  meetingid;
			ProcPara += flag + "2";
			ProcPara += flag + "" + arraycrmids02.get(i);
			ProcPara += flag + membermanager;
			RecordSet.executeProc("Meeting_Member2_Insert",ProcPara);
		}
		//会议议程
		int topicrows=Util.getIntValue(Util.null2String(request.getParameter("topicrows")),0);
		if(topicrows>0){
			String recordsetids="";
			for(int i=1;i<=topicrows;i++){
				String recordsetid=Util.null2String(request.getParameter("topic_data_"+i));
				if(!recordsetid.equals("")) recordsetids+=","+recordsetid;
			}
			if(!recordsetids.equals("")){
				recordsetids=recordsetids.substring(1);
				Sql = "delete from Meeting_Topic WHERE ( meetingid = "+meetingid+" and id not in ("+recordsetids+"))";
				RecordSet.executeSql(Sql);
			}else{
				Sql = "delete from Meeting_Topic WHERE ( meetingid = "+meetingid+")";
				RecordSet.executeSql(Sql);
			}
			MeetingFieldManager mfm2=new MeetingFieldManager(2);
			for(int i=1;i<=topicrows;i++){
				String recordsetid=Util.null2String(request.getParameter("topic_data_"+i));
				mfm2.editCustomDataDetail(request,Util.getIntValue(recordsetid),i,Util.getIntValue(meetingid));
			}
			
		}
		
		//会议服务
		int servicerows=Util.getIntValue(Util.null2String(request.getParameter("servicerows")),0);
		if(servicerows>0){
			String recordsetids="";
			for(int i=1;i<=servicerows;i++){
				String recordsetid=Util.null2String(request.getParameter("serivce_data_"+i));
				if(!recordsetid.equals("")) recordsetids+=","+recordsetid;
			}
			if(!recordsetids.equals("")){
				recordsetids=recordsetids.substring(1);
				Sql = "delete from Meeting_Service_New WHERE ( meetingid = "+meetingid+" and id not in ("+recordsetids+"))";
				RecordSet.executeSql(Sql);
			}else{
				Sql = "delete from Meeting_Service_New WHERE ( meetingid = "+meetingid+")";
				RecordSet.executeSql(Sql);
			}
			MeetingFieldManager mfm3=new MeetingFieldManager(3);
			for(int i=1;i<=servicerows;i++){
				String recordsetid=Util.null2String(request.getParameter("serivce_data_"+i));
				mfm3.editCustomDataDetail(request,Util.getIntValue(recordsetid),i,Util.getIntValue(meetingid));
			}
		}
		MeetingViewer MeetingViewer = new MeetingViewer();
		MeetingComInfo MeetingComInfo = new MeetingComInfo();
	    MeetingViewer.setMeetingShareById(meetingid);
		MeetingComInfo.removeMeetingInfoCache();
		MeetingUtil MeetingUtil = new MeetingUtil();
		//文档和附件的共享明细
		MeetingUtil.meetingDocShare(meetingid);
		MeetingLog meetingLog = new MeetingLog();
		meetingLog.resetParameter();
		meetingLog.insSysLogInfo(user,Util.getIntValue(meetingid),name,"修改会议"+(repeatType>0?"模板":""),"303","2",1,Util.getIpAddr(request));
		JSONObject json = new JSONObject();
        json.put("meetingid", meetingid);
        json.put("code", "0");
        json.put("message", "成功");
        this.returnValue(json, response);
		return;
	}
	
	/**
	 * 取消会议
	 * @param user
	 * @throws Exception
	 */
	public void cancelMeeting(User user) throws Exception{
		RecordSet RecordSet = new RecordSet();
		RecordSet RecordSetDB = new RecordSet();
		MeetingRemindUtil MeetingRemindUtil = new MeetingRemindUtil();
		String meetingId = request.getParameter("meetingid");
		String userId = "" + user.getUID();
		int meetingStatus = -1;
		String allUser=MeetingShareUtil.getAllUser(user);
		//会议取消，触发系统提醒工作流
		String MeetingName="";
		String MeetingDate="";
		String MeetingContacter="";
		String callerN = "";
		String createrN = "";
		String remindTypeNew="";
		RecordSet.executeSql("select * from meeting where id = '"+meetingId+"'");
		while(RecordSet.next()){
		   MeetingName=RecordSet.getString("name");
		   MeetingDate=RecordSet.getString("begindate");
		   MeetingContacter=RecordSet.getString("contacter");
		   meetingStatus = RecordSet.getInt("meetingStatus");
		   callerN = RecordSet.getString("caller");
		   createrN = RecordSet.getString("creater");
		   remindTypeNew=RecordSet.getString("remindTypeNew");
		}
		MeetingLog meetingLog = new MeetingLog();
		meetingLog.resetParameter();
		meetingLog.insSysLogInfo(user,Util.getIntValue(meetingId),MeetingName,"取消会议","303","2",1,Util.getIpAddr(request));

		String wfname="";
		String wfaccepter="";
		String wfremark="";
		String CurrentDate = DateHelper.getCurrentDate();
		char flag = 2;
		ResourceComInfo ResourceComInfo = new ResourceComInfo();
		wfname=Util.toMultiLangScreen("23269")+":"+MeetingName+"-"+ResourceComInfo.getLastname(user.getUID()+"")+"-"+CurrentDate;
		
		RecordSet.executeProc("Meeting_Member2_SelectByType",meetingId+flag+"1");
		while(RecordSet.next()){
		   wfaccepter+=","+RecordSet.getString("memberid");
		}
		if(!"".equals(wfaccepter)){
			wfaccepter+=",";
		}

		RecordSet.execute("select hrmids from meeting_service_new where meetingid="+meetingId);
		while(RecordSet.next()){
			String hrmids=RecordSet.getString("hrmids");
			String[] hrmidarrs=hrmids.split(",");
			for(int i=0;i<hrmidarrs.length;i++){
				if(!hrmidarrs[i].equals("")&&wfaccepter.indexOf(","+hrmidarrs[i]+",")==-1){
					wfaccepter+=hrmidarrs[i]+",";
				}
			}
		}
		if(!"".equals(wfaccepter)){
			wfaccepter=wfaccepter.substring(1,wfaccepter.length()-1);
		}
		MeetingSetInfo meetingSetInfo = new MeetingSetInfo();
		SysRemindWorkflow SysRemindWorkflow = new SysRemindWorkflow();
		if(1!=meetingStatus&&meetingSetInfo.getCancelMeetingRemindChk()==1){
		    SysRemindWorkflow.setMeetingSysRemind(wfname,Util.getIntValue(meetingId),Util.getIntValue(MeetingContacter),wfaccepter,wfremark);
		}
		int userPrm=1;

		if(MeetingShareUtil.containUser(allUser,callerN)){//是召集人 赋权限为3
			userPrm = meetingSetInfo.getCallerPrm();
			if(userPrm != 3) userPrm = 3;
		}
		if(MeetingShareUtil.containUser(allUser,MeetingContacter)&&userPrm<3){//是联系人 且权限小于3
			if(userPrm < meetingSetInfo.getContacterPrm()){ //当前权限小于联系人权限
				userPrm = meetingSetInfo.getContacterPrm(); //赋联系人权限
			}
		}
		if(MeetingShareUtil.containUser(allUser,createrN)&&userPrm<3){//是创建人 且权限小于3
		   if(userPrm < meetingSetInfo.getCreaterPrm()){//当前权限小于创建人权限
				userPrm = meetingSetInfo.getCreaterPrm();//赋创建人权限
			}
		} 
		
		//更新状态
		RecordSet.executeSql("SELECT * FROM Meeting WHERE id = " + meetingId + " AND (meetingStatus = 1 OR meetingStatus = 2)");	
		boolean cancelRight = HrmUserVarify.checkUserRight("Canceledpermissions:Edit",user);
		if(RecordSet.next()  && ( userPrm == 3 || cancelRight))
		{
			meetingStatus = RecordSet.getInt("meetingStatus");
			String nowdate = DateHelper.getCurrentDate();
	        String nowtime = DateHelper.getCurrentTime();
	        RecordSetDB.executeSql("update meeting set cancel='1',meetingStatus=4,canceldate='"+nowdate+"',canceltime='"+nowtime+"' where id="+meetingId);
			//标识会议已经被取消
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("UPDATE Meeting_View_Status SET status = '2'");		
			stringBuffer.append(" WHERE meetingId = ");
			stringBuffer.append(meetingId);
			stringBuffer.append(" AND userId <> ");
			stringBuffer.append(user.getUID());
			
			RecordSetDB.executeSql(stringBuffer.toString());
			
			RecordSet.execute("select id from workplan where meetingId='"+meetingId+"'");
			weaver.WorkPlan.WorkPlanHandler wph = new weaver.WorkPlan.WorkPlanHandler();
			while(RecordSet.next()){
				wph.delete(RecordSet.getString("id"));
			}
			//待审批则删除相关流程
			if(1 == meetingStatus)
			{	
				int requestId = 0;	
		   	    int meetingtype1=0;
		 		RecordSet.executeSql("SELECT  requestid,name,meetingtype,repeattype FROM Meeting WHERE id = " + meetingId);
		    	if(RecordSet.next())
		    	{
		       		requestId = Integer.valueOf(Util.null2String(RecordSet.getString("requestid"))).intValue();
		       		meetingtype1=RecordSet.getInt("meetingtype");
		       		if(RecordSet.getInt("repeattype")>0){//周期会议,查看周期会议审批流程
			    		RecordSet.executeSql("Select formid From Meeting_Type t1 join workflow_base t2 on t1.approver1=t2.id  where t1.approver1>0 and t1.ID ="+meetingtype1);
			    	}else{
			    		RecordSet.executeSql("Select formid From Meeting_Type t1 join workflow_base t2 on t1.approver=t2.id  where t1.approver>0 and t1.ID ="+meetingtype1);
			    	}
			   		if(RecordSet.next()){
			   			int fromid=RecordSet.getInt("formid");
			   		    if(requestId>0){
				   			MeetingWFUtil.deleteWF(requestId,meetingId,fromid);
			   		        RecordSet.executeSql("delete From workflow_currentoperator where requestid="+requestId);
			   		    }
			   		}
		       	}
			}
			
		    MeetingInterval.deleteMeetingRepeat(meetingId);
		    //之前是正常会议,被取消后进行取消会议提醒
		    if(meetingStatus==2&&!"".equals(remindTypeNew)){
			    MeetingRemindUtil.cancelMeeting(meetingId);
		    }
		}
		JSONObject json = new JSONObject();
        json.put("meetingid", meetingId);
        json.put("code", "0");
        json.put("message", "成功");
        this.returnValue(json, response);
		return;
	}
	
	/**
	 * 获取未来一小时没被占用的
	 * @throws Exception 
	 */
	public void getUnUseMeeting(User user) throws Exception{
		String begindate=Util.null2String(request.getParameter("begindate"));
		String begintime=Util.null2String(request.getParameter("begintime"));
		String enddate=Util.null2String(request.getParameter("enddate"));
		String endtime=Util.null2String(request.getParameter("endtime"));
		int hours=Util.getIntValue(request.getParameter("hours"),0);
		
		if(hours>0){
			Calendar now=Calendar.getInstance();
			long datetime = now.getTimeInMillis() ;
			Timestamp timestamp = new Timestamp(datetime) ;
			begindate = (timestamp.toString()).substring(0,4) + "-" + (timestamp.toString()).substring(5,7) + "-" +(timestamp.toString()).substring(8,10);
			begintime = (timestamp.toString()).substring(11,13) + ":" + (timestamp.toString()).substring(14,16) + ":" +(timestamp.toString()).substring(17,19);
			
			now.add(Calendar.HOUR,hours);
			long end_datetime = now.getTimeInMillis() ;
			timestamp = new Timestamp(end_datetime) ;
			enddate = (timestamp.toString()).substring(0,4) + "-" + (timestamp.toString()).substring(5,7) + "-" +(timestamp.toString()).substring(8,10);
			endtime = (timestamp.toString()).substring(11,13) + ":" + (timestamp.toString()).substring(14,16) + ":" +(timestamp.toString()).substring(17,19);
		}else if(begindate.length()==0){
			Calendar now=Calendar.getInstance();
			long datetime = now.getTimeInMillis() ;
			Timestamp timestamp = new Timestamp(datetime) ;
			begindate = (timestamp.toString()).substring(0,4) + "-" + (timestamp.toString()).substring(5,7) + "-" +(timestamp.toString()).substring(8,10);
			begintime = (timestamp.toString()).substring(11,13) + ":" + (timestamp.toString()).substring(14,16) + ":" +(timestamp.toString()).substring(17,19);
			
			now.add(Calendar.HOUR,1);
			long end_datetime = now.getTimeInMillis() ;
			timestamp = new Timestamp(end_datetime) ;
			enddate = (timestamp.toString()).substring(0,4) + "-" + (timestamp.toString()).substring(5,7) + "-" +(timestamp.toString()).substring(8,10);
			endtime = (timestamp.toString()).substring(11,13) + ":" + (timestamp.toString()).substring(14,16) + ":" +(timestamp.toString()).substring(17,19);
		}
		RecordSet RecordSet = new RecordSet();
		String str1 = begindate+" "+begintime;
		String str3 = enddate+" "+endtime;
		MeetingRoomComInfo meetingRoomComInfo = new MeetingRoomComInfo();
		String sql="select r.id as roomid,r.name as roomname, "+
				" m.address,m.begindate,m.enddate,m.begintime,m.endtime,m.id,m.name "+
				" from MeetingRoom r left join (select * from meeting t where  t.meetingstatus in (1,2) and t.repeatType = 0 and t.isdecision<2 "+
				" and (t.cancel is null or t.cancel<>'1') and (t.begindate <=  '"+begindate+"' and enddate >='"+begindate+"' ) )  m on m.address=r.id  "+
				" where r.status!=2 order by r.dsporder,r.name";
		new BaseBean().writeLog("会议sql>>> "+sql);
		
//		RecordSet.executeSql("select address,begindate,enddate,begintime,endtime,id,name from meeting where meetingstatus in (1,2) and repeatType = 0 and isdecision<2 and (cancel is null or cancel<>'1') and (begindate <= '"+begindate+"' and enddate >='"+begindate+"')");
		RecordSet.executeSql(sql);
		String usemeetingids ="";
		String continueRoomId = "";
		while(RecordSet.next()) {
			String roomid = Util.null2String(RecordSet.getString("roomid"));
			if(continueRoomId.indexOf(roomid+",")>-1){//如果会议室已经判断为被占用 就不做后面的判断了。
				continue;
			}
			String roomname = Util.null2String(RecordSet.getString("roomname"));
			String begindatetmp = Util.null2String(RecordSet.getString("begindate"));
			String begintimetmp = Util.null2String(RecordSet.getString("begintime"));
			String enddatetmp = Util.null2String(RecordSet.getString("enddate"));
			String endtimetmp = Util.null2String(RecordSet.getString("endtime"));
			String addresstmp = Util.null2String(RecordSet.getString("address"));
			String mid = Util.null2String(RecordSet.getString("id"));
			String name = Util.null2String(RecordSet.getString("name"));
			if(begindatetmp.length()>0&&begintimetmp.length()>0){
				String str2 = enddatetmp+" "+endtimetmp;
				String str4 = begindatetmp+" "+begintimetmp;
				if((str1.compareTo(str2) < 0 && str3.compareTo(str4) > 0)) {
					continueRoomId+=roomid+",";
					   continue;
				}else{
					if(usemeetingids.indexOf(","+roomid)>-1){
						continue;
					}else{
						usemeetingids+=","+roomid;
					}
					
				}
			}else{
				if(usemeetingids.indexOf(","+roomid)>-1){
					continue;
				}else{
					usemeetingids+=","+roomid;
				}
			}
		}
		if(usemeetingids.length()>0){
			usemeetingids = usemeetingids.substring(1);
		}
		
		usemeetingids = replaceRoom(usemeetingids, continueRoomId);
		
		JSONArray jsonArray  = new JSONArray();
		sql="select a.id,a.name,a.subcompanyid,a.roomdesc from MeetingRoom a where a.status!=2 ";
		sql += MeetingShareUtil.getRoomShareSql(user);
		sql += " and (a.status=1 or a.status is null ) and id in("+usemeetingids+")";
		sql+="order by a.dsporder ,a.name  ";
		RecordSet.executeSql(sql);
		while(RecordSet.next()){ 
			String roomid = Util.null2String(RecordSet.getString("id"));
			String roomname = Util.null2String(RecordSet.getString("name"));
			JSONObject meetingMap = new JSONObject();
			meetingMap.put("id", roomid);
			meetingMap.put("name", roomname);
			jsonArray.add(meetingMap);
		}
		JSONObject json = new JSONObject();
	    json.put("UnUseMeetings", jsonArray);
	    json.put("code", "0");
	    json.put("message", "成功");
	    this.returnValue(json, response);
		return;
	}
	
	public void getMeetingView(User user) throws Exception{
		RecordSet RecordSet = new RecordSet();
		int bywhat = Util.getIntValue(request.getParameter("bywhat"),4);
		String userid=user.getUID()+"" ;
		String datenow = Util.null2String(request.getParameter("datenow"));
		int subids = Util.getIntValue(request.getParameter("subids"), -1);
		String content = Util.null2String(request.getParameter("content"));
		int roomid = Util.getIntValue(request.getParameter("roomid"), 0);
		ManageDetachComInfo ManageDetachComInfo = new ManageDetachComInfo();
		boolean isUseMtiManageDetach=ManageDetachComInfo.isUseMtiManageDetach();
		int detachable=0;
		if(isUseMtiManageDetach){
			detachable=1;
		}else{
			detachable=0;
		}
		
		String sqlwhere = "";

		if(subids > 0){
			 sqlwhere = "and a.subCompanyId = "+ subids ;
		}
		if(roomid > 0){
			sqlwhere = "and a.id = " + roomid ;
		} else {
			if(!"".equals(content.trim())){
				sqlwhere = "and a.name like '%" + content + "%' ";
			}
			sqlwhere += " and (a.status=1 or a.status is null ) ";
		}
		MeetingSetInfo meetingSetInfo = new MeetingSetInfo();
		
		String returnStr = "" ;   
        switch (bywhat) {
            case 2 :                  
                returnStr=" and  (t1.meetingstatus=2 or t1.meetingstatus=1) and ('"+datenow+"'" +
                        " between SUBSTRING(t1.begindate,1,7) and SUBSTRING(t1.enddate,1,7)) and (t1.isdecision<2)" ;
                break ;
            case 3 :
                returnStr=" and  (t1.meetingstatus=2 or t1.meetingstatus=1) and ( "   ;   
                for (int h = -1;h<6;h++){                 
                    String newTempDate = TimeUtil.dateAdd(datenow,h) ;
                    returnStr +="('"+newTempDate+"' between t1.begindate and t1.enddate) or" ;         
                }
                returnStr = returnStr.substring(0,returnStr.length()-2);
                returnStr += ") and (t1.isdecision<2) " ;
                break ;                
            case 4 : 
                returnStr = " and  (t1.meetingstatus=2 or t1.meetingstatus=1) and ('"+datenow+"' " +
                        " between t1.begindate and t1.enddate)  and (t1.isdecision<2) " 
						+ " and ( (t1.endtime between '"+Util.add0(meetingSetInfo.getTimeRangeStart(),2)+"' and '"+Util.add0(meetingSetInfo.getTimeRangeEnd(),2)+":59') or (t1.begintime between '"+Util.add0(meetingSetInfo.getTimeRangeStart(),2)+"' and '"+Util.add0(meetingSetInfo.getTimeRangeEnd(),2)+":59'))";
                break ;      
        }
        
        if ((RecordSet.getDBType()).equals("oracle")) {
            returnStr = Util.StringReplace(returnStr,"SUBSTRING","substr");   
        }
        
        sqlwhere = MeetingShareUtil.getRoomShareSql(user) + sqlwhere;
        if(sqlwhere.length() > 4) {
        	returnStr += " and exists (select 1 from MeetingRoom a  where t1.address = a.id "+ sqlwhere + ") ";
        }
		String backfields = "t1.id,t1.name,t1.address,t1.customizeAddress,t1.caller,t1.contacter,t1.begindate,t1.cancel,t1.begintime,t1.enddate,t1.endtime,t1.meetingstatus,t1.isdecision,t1.description,t1.repeattype, t3.status as status,t.id as tid, t.name as typename,t1.creater ";
		String fromSql = "  Meeting t1 left join Meeting_View_Status t3 on t3.meetingId = t1.id and t3.userId = " + userid + ", Meeting_Type  t ";
		
		String whereSql = " where t1.meetingtype = t.id  and t1.repeatType = 0 " + returnStr;
		MeetingTransMethod MeetingTransMethod = new MeetingTransMethod();
		MeetingRoomComInfo meetingRoomComInfo = new MeetingRoomComInfo();
		String sql="select "+ backfields + " from "+ fromSql + whereSql;
		RecordSet.executeQuery(sql);
		JSONArray jsonArray = new JSONArray();
		while(RecordSet.next()){
			String id = Util.null2String(RecordSet.getString("id"));
			String name = Util.null2String(RecordSet.getString("name"));
			String caller = new ResourceComInfo().getResourcename( Util.null2String(RecordSet.getString("caller")));
			String typename = Util.null2String(RecordSet.getString("typename"));
			String r_roomid = Util.null2String(RecordSet.getString("address"));
			String address = meetingRoomComInfo.getMeetingRoomInfoname(Util.null2String(RecordSet.getString("address")));
			String begindatetime = Util.null2String(RecordSet.getString("begindate"))+" "+Util.null2String(RecordSet.getString("begintime"));
			String enddatetime = Util.null2String(RecordSet.getString("enddate"))+" "+Util.null2String(RecordSet.getString("endtime"));
			String enddatetime2 = Util.null2String(RecordSet.getString("enddate"))+"+"+Util.null2String(RecordSet.getString("endtime"));
			String meetingstatus = Util.null2String(RecordSet.getString("meetingstatus"));
			String isdecision = Util.null2String(RecordSet.getString("isdecision"));
			meetingstatus = MeetingTransMethod.getMeetingStatus(meetingstatus,user.getLanguage()+"+"+enddatetime2+"+"+isdecision);
			JSONObject json = new JSONObject();
			json.put("id", id);
			json.put("name", name);
			json.put("caller", caller);
			json.put("typename", typename);
			json.put("address", address);
			json.put("roomid", r_roomid);
			json.put("begindatetime", begindatetime);
			json.put("enddatetime", enddatetime);
			json.put("meetingstatus", meetingstatus);
			
			jsonArray.add(json);
		}
		JSONObject json = new JSONObject();
	    json.put("MeetingViewData", jsonArray);
	    json.put("code", "0");
	    json.put("message", "成功");
	    this.returnValue(json, response);
		return;
	}
	
	public void getMeetingTypeBrowser(User user){
		String sql="select a.id,a.name,a.subcompanyid,a.desc_n,a.approver,a.approver1 from Meeting_Type a where 1=1 ";
		String name = Util.null2String(request.getParameter("name"));
		if(name.length()>0){
			sql+=" and a.name='"+name+"' ";
		}
		sql += MeetingShareUtil.getTypeShareSql(user);
		sql+=" order by a.dsporder,a.name";
		RecordSet rs = new RecordSet();
		rs.executeSql(sql);
		JSONArray array = new JSONArray();
		while(rs.next()){
			JSONObject json = new JSONObject();
			json.put("id", rs.getString("id"));
			json.put("name", rs.getString("name"));
			array.add(json);
		}
		this.returnValue(array, response);
	}
	
	public JSONObject chkRoom(User user) {
		RecordSet RecordSet = new RecordSet();
		MeetingSetInfo meetingSetInfo = new MeetingSetInfo();
		//会议室冲突校验
		String meetingaddress = Util.null2String(request.getParameter("address"));
		String begindate=Util.null2String(request.getParameter("begindate"));
		String begintime=Util.null2String(request.getParameter("begintime"));
		String enddate=Util.null2String(request.getParameter("enddate"));
		String endtime=Util.null2String(request.getParameter("endtime"));
		String meetingids ="";
		String returnstr = "0";
		String message = "会议室验证:无冲突，可以预定";
		if(meetingSetInfo.getRoomConflictChk() == 1 ){
			RecordSet.executeSql("select address,begindate,enddate,begintime,endtime,id from meeting where meetingstatus in (1,2) and repeatType = 0 and isdecision<2 and (cancel is null or cancel<>'1') and (begindate <= '"+enddate+"' and enddate >='"+begindate+"')");
			while(RecordSet.next()) {
				String begindatetmp = Util.null2String(RecordSet.getString("begindate"));
				String begintimetmp = Util.null2String(RecordSet.getString("begintime"));
				String enddatetmp = Util.null2String(RecordSet.getString("enddate"));
				String endtimetmp = Util.null2String(RecordSet.getString("endtime"));
				String addresstmp = Util.null2String(RecordSet.getString("address"));
				String mid = Util.null2String(RecordSet.getString("id"));

				String str1 = begindate+" "+begintime;
				String str2 = enddatetmp+" "+endtimetmp;
				String str3 = enddate+" "+endtime;
				String str4 = begindatetmp+" "+begintimetmp;

				if(!"".equals(meetingaddress) && meetingaddress.equals(addresstmp) && !mid.equals(meetingids)) {
					if((str1.compareTo(str2) < 0 && str3.compareTo(str4) > 0)) {
					   returnstr = "1";
					   message = "会议室验证:有冲突，不可预定，请选择其他会议室";
					   break;
					}
				}
			}
		}
	    JSONObject json = new JSONObject();
	    json.put("code", returnstr);
	    json.put("message", message);
//	    this.returnValue(json, response);
	    return json;
	}
	
	public void getMeetingRoomBrowser(User user){
		String sql="select a.id,a.name,a.subcompanyid,a.roomdesc from MeetingRoom a where a.status!=2 ";
		String name = Util.null2String(request.getParameter("name"));
		if(name.length()>0){
			sql+=" and a.name='"+name+"'";
		}
		sql += MeetingShareUtil.getRoomShareSql(user);
		sql += " and (a.status=1 or a.status is null )";
		sql+="order by a.dsporder ,a.name  ";
		RecordSet rs = new RecordSet();
		rs.executeSql(sql);
		JSONArray array = new JSONArray();
		while(rs.next()){
			JSONObject json = new JSONObject();
			json.put("id", rs.getString("id"));
			json.put("name", rs.getString("name"));
			json.put("roomdesc", rs.getString("roomdesc"));
			array.add(json);
		}
		this.returnValue(array, response);
	}
	
	public void getMeetingRemindBrowser(User user){
		Map<String,String> map=MeetingBrowser.getRemindMap();
		JSONArray array = new JSONArray();
		if(map.size()>0){
			 Iterator<String> it =map.keySet().iterator();
			 while(it.hasNext()){
				 String i=it.next();
				 JSONObject json = new JSONObject();
				 String name = MeetingBrowser.getRemindName(Util.getIntValue(i),user.getLanguage());
				 json.put("id", i);
				 json.put("name", name);
				 array.add(json);
			 }
		}
		this.returnValue(array, response);
	}
	
	/**
     * base64字符串转文件
     * @param base64
     * @return
	 * @throws IOException 
     */
    public String base64ToFile(String base64Code,String targetPath) throws IOException {
    	if (base64Code == null && targetPath == null) {
            return "生成文件失败，请给出相应的数据。";
		}
    	byte[] buffer = new BASE64Decoder().decodeBuffer(base64Code);
    	  FileOutputStream out = new FileOutputStream(targetPath);
    	  out.write(buffer);
    	  out.close();
		return "指定路径下生成文件成功！";

    }
    
//    public String meetingfile2doc(HttpServletRequest request,User user){
//    	FileUpload fileUpload = new MobileFileUpload(request,"UTF-8",false);
//    	//附件上传处理
//		String docIdContent = "";
//		docIdContent = MobileCommonUtil.uploadFile4(fileUpload, user);
//		String keyValue = Util.null2String(fileUpload.getParameter("field35"));
//		if(keyValue.indexOf("#") == -1 && !keyValue.equals("")){
//			if(!docIdContent.equals("")){
//				docIdContent += "," + keyValue;
//			}else{
//				docIdContent = keyValue;
//			}
//		}
//		return docIdContent;
//    }
    
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
    
    public String replaceRoom(String usemeetingids,String continueRoomId){
    	String str1 = usemeetingids;
    	String str2 = continueRoomId;
    	String[] arr1 = str1.split(",");
    	String[] arr2 = str2.split(",");
    	for (int i = 0; i < arr2.length; i++) {
    		for (int j = 0; j < arr1.length; j++) {
    			if (arr1[j].equals(arr2[i])) {
    				arr1[j] = "";
    			}
    		}
    	}
    	StringBuffer sb = new StringBuffer();
    	for (int j = 0; j < arr1.length; j++) {
    		if (!"".equals(arr1[j])) {
    			sb.append(arr1[j] + ",");
    		}
    	}
    	return sb.toString().substring(0, sb.toString().length() - 1);
    }
	
}
