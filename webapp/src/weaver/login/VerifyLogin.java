package weaver.login;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ln.LN;

import org.w3c.dom.Document;

import weaver.common.StringUtil;
import weaver.conn.RecordSet;
import weaver.file.Prop;
import weaver.general.BaseBean;
import weaver.general.GCONST;
import weaver.general.IpUtils;
import weaver.general.StaticObj;
import weaver.general.TimeUtil;
import weaver.general.Util;
import weaver.hrm.User;
import weaver.hrm.settings.ChgPasswdReminder;
import weaver.hrm.settings.HrmSettingsComInfo;
import weaver.hrm.settings.RemindSettings;
import weaver.ldap.LdapUtil;
import weaver.sms.SMSManager;
import weaver.systeminfo.SysMaintenanceLog;
import weaver.usb.UsbKeyProxy;
import HT.HTSrvAPI;

/**
 * Title:        wahaha project
 * Description:  for wahaha product
 * Copyright:    Copyright (c) 1995
 * Company:      weaver
 *
 * @author liuyu
 * @version 1.0
 */

public class VerifyLogin extends BaseBean {

    public String getUserCheck(HttpServletRequest request, HttpServletResponse response, String login_id, String user_password, String serial, String username, String rnd, String login_type, String login_file,String validateCode,String messages,String languid,boolean ismutilangua) throws Exception {
    	StaticObj staticobj = null; 
        staticobj = StaticObj.getInstance(); 
        String frommail=Util.null2String(request.getParameter("frommail"));//针对华虹要求邮件直接登陆，不考虑安全性（内部使用）

        RecordSet rs = new RecordSet();
        RecordSet rs1 = new RecordSet();
        RecordSet rs2 = new RecordSet();
        char separator = Util.getSeparator();
        String message = "";
        boolean MOREACCOUNTLANDING = GCONST.getMOREACCOUNTLANDING();
        Calendar today = Calendar.getInstance();
        String currentdate = Util.add0(today.get(Calendar.YEAR), 4) + "-" + Util.add0(today.get(Calendar.MONTH) + 1, 2) + "-" + Util.add0(today.get(Calendar.DAY_OF_MONTH), 2);
        String currenttime = Util.add0(today.get(Calendar.HOUR_OF_DAY), 2) + ":" + Util.add0(today.get(Calendar.MINUTE), 2) + ":" + Util.add0(today.get(Calendar.SECOND), 2);

        String isADAccount = "";
        String isADAccountSql = "select isADAccount from HrmResource where loginid = '"+ login_id +"'";
        rs.executeSql(isADAccountSql);
        if(rs.next()) {
        	isADAccount = rs.getString("isADAccount");
        }
        
        String isUseLdap = Prop.getPropValue(GCONST.getConfigFile(), "authentic");
        
        try {

            String isLicense = (String) staticobj.getObject("isLicense");
            //国生 2004-9-16 , TD1096 , License的日期限制不起作用

            LN ckLicense = new LN();
            try{
            	message="1";
            }catch (Exception e) {
            	return "19";
			}
            if (!message.equals("1")) {
                return "19";
            } else {
                staticobj.putObject("isLicense", "true");
            }

			//检测用户并发数start
			String concurrentFlag = Util.null2String(ckLicense.getConcurrentFlag());
			int hrmnumber = Util.getIntValue(ckLicense.getHrmnum());
			if("1".equals(concurrentFlag)){
				LicenseCheckLogin  lchl = new LicenseCheckLogin();
				if(lchl.getLicUserCheck(login_id,hrmnumber)){
					recordFefuseLogin(login_id); //拒绝登陆记录
					return "26";
				}
			}
            //检测用户并发数end
            
            message = "";
            String software = (String) staticobj.getObject("software");
            String portal = "n";
            String multilanguage = "n";
            if (software == null) {
                rs.executeSql("select * from license");
                if (rs.next()) {
                    software = rs.getString("software");
                    if (software.equals("")) software = "ALL";
                    staticobj.putObject("software", software);
                    portal = rs.getString("portal");
                    if (portal.equals("")) portal = "n";
                    staticobj.putObject("portal", portal);
                    multilanguage = rs.getString("multilanguage");
                    if (multilanguage.equals("")) multilanguage = "n";
                    staticobj.putObject("multilanguage", multilanguage);
                }
            }
            
            if(!checkIpSegByForbidLogin(request, login_id)){
            	if(checkIsNeedIp(login_id)){
            		return "88";
            	}
            }
            boolean isAdmin = false;//Added by wcd 2014-11-21

            if (login_type.equals("1")) {
                ChgPasswdReminder reminder=new ChgPasswdReminder();
                RemindSettings settings=reminder.getRemindSettings();

                //String mode = Prop.getPropValue(GCONST.getConfigFile(), "authentic");
                String usbserver = Prop.getPropValue(GCONST.getConfigFile(), "usbserver.ip");

                int needvalidate=settings.getNeedvalidate();//0: 否,1: 是

                String validateRand="";
                validateRand=Util.null2String((String)request.getSession(true).getAttribute("validateRand"));
                if(needvalidate==1){
                    if(!validateRand.toLowerCase().equals(validateCode.trim().toLowerCase()))
                        return "52";
                }
                
                /***解决AD同步过后分权管理员不能登录***/
                boolean isloginlpad = true;
                if (ifEqlTarget(isADAccount, "1")){
                	rs.executeSql("select * from HrmResourceManager where loginid = '"+login_id+"'");
                	if(rs.next()) isloginlpad = false;
                }
                if (ifEqlTarget(isUseLdap, "ldap") && ifEqlTarget(isADAccount, "1") && isloginlpad) {
                    LdapUtil util = LdapUtil.getInstance();
                    boolean flag = true;
                    if(!"101".equals(messages)) {//未输入正确密码之前需要检验密码。密码正确之后验证验证动态密码

                    	String authenticResult = util.authenticuser(login_id);//先判断用户是否存在AD中存在，或被禁用
                    	if(authenticResult == null || "uac".equals(authenticResult)) {
                    		return "17";
                    	}
                    	flag = util.authentic(login_id, user_password);
                    }
                    rs.executeSql("select * from HrmResource where loginid='" + login_id + "'");
                    
                    if (flag && !"101".equals(messages)) {
                        if (!rs.next())
                            return "17";
                        else {
							int tmpid = rs.getInt("id");
                            int needdynapass_usr = rs.getInt("needdynapass");
                            int needdynapass_sys = settings.getNeeddynapass();
                            int dynapasslen = settings.getDynapasslen();
                            int passwordstateip = 1;
                            if(needdynapass_sys==1) {
                            	String sql = "select password,usbstate as passwordstate from HrmResource where loginid='"+login_id+"'";
                            	rs2.executeSql(sql);
                            	if(rs2.next()) passwordstateip = rs2.getInt("passwordstate");
                            }
                            boolean ipaddress = checkIpSeg(request, login_id, passwordstateip);//检查网段策略

                            if (needdynapass_usr == 1 && needdynapass_sys == 1 && dynapasslen > 0 && ipaddress) {
                                String mobileno = rs.getString("mobile");
                                if(sendOk("l-160", settings.getDypadcon(), dynapasslen, mobileno, currentdate+" "+currenttime, String.valueOf(tmpid), settings.getValiditySec()))
                                	return "101";
                            }
                        }
                    } else if("101".equals(messages)  && flag == true){
                        if (!rs.next())
                            return "17";
                        else {
							int tmpid = rs.getInt("id");
                            int needdynapass_usr = rs.getInt("needdynapass");
                            int needdynapass_sys = settings.getNeeddynapass();
                            int dynapasslen = settings.getDynapasslen();
                            if (needdynapass_usr == 1 && needdynapass_sys == 1 && dynapasslen > 0) {
                            	rs1.executeSql("select password from hrmpassword where id=" + tmpid);
                                String pswd = "";
                                if(rs1.next()){
                                	pswd = StringUtil.vString(rs1.getString(1));
                                	if(pswd.length() == 0){
                                		if (rs.getString("password").equals(Util.getEncrypt(user_password))) {
											//重新发送

											if (sendOk("l-180", settings.getDypadcon(), dynapasslen, rs.getString("mobile"), currentdate + " " + currenttime, String.valueOf(tmpid), settings.getValiditySec()))
												return "101";
										} else {
                                			return "730";
                                    	}
                                	} else if(pswd.equals(Util.getEncrypt(user_password))){
                                		rs1.executeSql("update hrmpassword set password='' where id=" + tmpid);
                                	} else {
                                		return "57";
                                	}
                                } else {
                                	return "57";
                                }
                            }else
                                return "16";
                        }
                    } else if(!"101".equals(messages) && flag==false) {
                    	 return "16";
                    }
                } else {
                    String[] loginCheck = checkUserPass(request,login_id,user_password,messages);
                    if (frommail.equals("1")) loginCheck=checkUserPassM(login_id,user_password);
                    if(loginCheck[0].equals("-2"))
                        return "55";
                    else if(loginCheck[0].equals("-1"))
                        return "17";
                    else if(loginCheck[1].equals("0"))
                            return "16";
                    else if(loginCheck[1].equals("101"))
                            return "101";
                    else if(loginCheck[1].equals("730"))
                        return "730";
                    else if(loginCheck[1].equals("57"))
                        return "57";
                        else if(loginCheck[0].equals("0")){
                                rs.executeSql("select * from HrmResource where loginid='"+login_id+"'");
                                rs.next();
                            }
                            else{
                            	isAdmin = true;
                                rs.executeSql("select * from HrmResourceManager where loginid='"+login_id+"'");
                                rs.next();
                            }
                    /*
                    if (rs.next()) {

                        if (rs.getString(1).equals("0")) {
                            return "16";
                        }
                    }else{
                         return "17";
                    }*/
                }
                
                //用于检查重复登录

                ArrayList onlineuserids = null;
                onlineuserids = (ArrayList) staticobj.getObject("onlineuserids");
                if (onlineuserids != null) {
                    if (onlineuserids.indexOf("" + rs.getInt("id")) != -1) {
                        String sqltmp = "";
                        if (rs1.getDBType().equals("oracle")) {
                            sqltmp = "select * from (select * from HrmSysMaintenanceLog where relatedid = " +rs.getInt("id") + " order by id desc ) where rownum=1 ";
                         }else if(rs.getDBType().equals("db2")){
                            sqltmp = "select * from HrmSysMaintenanceLog where relatedid = "+rs.getInt("id") +" order by id desc fetch first 1 rows only ";
                        } else {
                            sqltmp = "select top 1 * from HrmSysMaintenanceLog where relatedid = " + rs.getInt("id") + " order by id desc";
                        }
                       
                        rs1.executeSql(sqltmp);
                        if (rs1.next())
                            message = rs1.getString("clientaddress") + " " + rs1.getString("operatedate") + " " + rs1.getString("operatetime");
                    }
                }

                String startdate = rs.getString("startdate");
                String enddate = rs.getString("enddate");
/*

				if((currentdate.compareTo(startdate) < 0 && !startdate.equals("")) ||
				   (currentdate.compareTo(enddate) >0 && !enddate.equals("")))  return "17" ;
*/
                int status = Util.getIntValue(rs.getString("status"));
                if (status != 0 && status != 1 && status != 2 && status != 3) {
                    return "17";
                }

                User user = new User();

                user.setUid(rs.getInt("id"));
                user.setLoginid(login_id);
                user.setFirstname(rs.getString("firstname"));
                user.setLastname(rs.getString("lastname"));
                user.setAliasname(rs.getString("aliasname"));
                user.setTitle(rs.getString("title"));
                user.setTitlelocation(rs.getString("titlelocation"));
                user.setSex(rs.getString("sex"));
                user.setPwd(user_password);
                String languageidweaver = rs.getString("systemlanguage");
                if(!languid.equalsIgnoreCase(languageidweaver) && ismutilangua){
                	rs2.execute("update hrmresource set  systemlanguage = "+languid+" where id ="+rs.getInt("id"));
                	languageidweaver = languid;
                }
                user.setLanguage(Util.getIntValue(languageidweaver, 0));

                user.setTelephone(rs.getString("telephone"));
                user.setMobile(rs.getString("mobile"));
                user.setMobilecall(rs.getString("mobilecall"));
                user.setEmail(rs.getString("email"));
                user.setCountryid(rs.getString("countryid"));
                user.setLocationid(rs.getString("locationid"));
                user.setResourcetype(rs.getString("resourcetype"));
                user.setStartdate(startdate);
                user.setEnddate(enddate);
                user.setContractdate(rs.getString("contractdate"));
                user.setJobtitle(rs.getString("jobtitle"));
                user.setJobgroup(rs.getString("jobgroup"));
                user.setJobactivity(rs.getString("jobactivity"));
                user.setJoblevel(rs.getString("joblevel"));
                user.setSeclevel(rs.getString("seclevel"));
                user.setUserDepartment(Util.getIntValue(rs.getString("departmentid"), 0));
                user.setUserSubCompany1(Util.getIntValue(rs.getString("subcompanyid1"), 0));
                user.setUserSubCompany2(Util.getIntValue(rs.getString("subcompanyid2"), 0));
                user.setUserSubCompany3(Util.getIntValue(rs.getString("subcompanyid3"), 0));
                user.setUserSubCompany4(Util.getIntValue(rs.getString("subcompanyid4"), 0));
                user.setManagerid(rs.getString("managerid"));
                user.setAssistantid(rs.getString("assistantid"));
                user.setPurchaselimit(rs.getString("purchaselimit"));
                user.setCurrencyid(rs.getString("currencyid"));
                user.setLastlogindate(currentdate);
                user.setLogintype("1");
                user.setAccount(rs.getString("account"));
                user.setIsAdmin(isAdmin);
                //xiaofeng
                HrmSettingsComInfo sci=new HrmSettingsComInfo();

                /**检测是否启用usb网段策略开始**/
				CheckIpNetWork checkipnetwork = new CheckIpNetWork();
				String clientIP = request.getRemoteAddr();
				boolean checktmp = checkipnetwork.checkIpSeg(clientIP);//true表示在网段之外,false表示在网段之内

				/**检测是否启用usb网段策略结束**/
				
                int needusb = rs.getInt("needusb");
                int usbstate = rs.getInt("usbstate");
                /*****add by lvyi begin******/
                if(usbstate!=2){
                	//个人设置不是网段策略的 不需要判断 按checkipnetwork.checkIpSeg 默认值

                	checktmp = true;
                }
                /*****add by lvyi end*****/
                String usbType = sci.getUsbType();
                String needusbHt = sci.getNeedusbHt();
                String needusbDt= sci.getNeedusbDt();
                String userUsbType=Util.null2String(rs.getString("userUsbType")); //指定usbkey验证类型
                if(!userUsbType.equals("")){
                	usbType=userUsbType;
                }
                needusb = (userUsbType.equals("2") || userUsbType.equals("3")) ? 1 : 0;
                if (needusb==1) {                   
                    if(checktmp && usbstate != 1) {
	                	if("1".equals(usbType)){
	                		String serialNo = Util.null2String(rs.getString("serial"));
		                    byte[] bts = Base64.decode(serial);
		                    String serial1 = new String(bts, "ISO8859_1");
		
		                    //TD4290
		                    //modified by hubo,2006-05-09
		                    long firmcode = Util.getIntValue(sci.getFirmcode());
		                    long usercode = Util.getIntValue(sci.getUsercode());
		                    /*rs1.executeSql("SELECT firmcode,usercode FROM HrmOtherSettings");
		                    if(rs1.next()){
		                        firmcode = Long.parseLong(rs1.getString("firmcode"));
		                        usercode = Long.parseLong(rs1.getString("usercode"));
		                    }*/
		                    String serialNo1=null;
		                    if(usbserver!=null&&!usbserver.equals("")) {
		                     UsbKeyProxy proxy=new UsbKeyProxy(usbserver);
		                     serialNo1=proxy.decrypt(firmcode, usercode, Long.parseLong(rnd), serial1);
		                    }else
		                     serialNo1= AuthenticUtil.decrypt(firmcode, usercode, Long.parseLong(rnd), serial1);
		
		                    if (serial.equals("0"))
		                        return "45";
		                    else if (serial.equals("1") || serial.equals(serialNo))
		                        return "46";
		                    else if (serialNo.equals(serialNo1)) {
		                        user.setNeedusb(needusb);
		                        user.setSerial(serialNo);
		                    } else if (serialNo1.equals("0")) {
		                        return "48";
		                    } else
		                        return "47";
	                	}else if(needusbDt.equals("1")&&"3".equals(usbType)){ //坚石key验证 增加大开关控制 add by lvyi
	                		//qc172088 对于绑定了动态令牌的人员的逻辑是，在网段外需要使用动态令牌登录，在网段内不需要直接使用普通用户名、密码登录即可。
//	                		 * 当网段策略没有开启的时候，正常验证海泰key和动态令牌
//	                		 * 当网段策略开启的时候，网段内海泰key和动态令牌不做验证
//	                		 * 当网段策略开启的时候，网段外的海泰key和动态令牌验证
	                		boolean isNeedIp = true;
	                		int forbidLogin = Util.getIntValue(sci.getForbidLogin(), 0);
	                		if(forbidLogin == 0){
	                			isNeedIp = false;
	                			if(usbstate == 2 && !checktmp) isNeedIp = true; 
	                		}else{
	                			isNeedIp = checkIpSegByForbidLogin(request, login_id);
	                		}
	                		if(!isNeedIp){
	                			String tokenAuthKey=Util.null2String(request.getParameter("tokenAuthKey"));
	                			String tokenKey=Util.null2String(rs.getString("tokenKey"));
	                			if(tokenKey.equals(""))
	                				return "120"; //未绑定令牌
	                			
	                			else{
	                				TokenJSCX token=new TokenJSCX();
	                				boolean isTokenAuthKeyPass=false;
	                				
	                				RecordSet recordSet=new RecordSet();  
	                				String sql="select * from tokenJscx WHERE tokenKey='"+tokenKey+"'";
	                				recordSet.execute(sql);
	                				if(recordSet.next()){
	                					if(tokenKey.startsWith("1"))
	                						isTokenAuthKeyPass=token.checkDLKey(tokenKey,tokenAuthKey);
	                					else if(tokenKey.startsWith("2"))
	                						isTokenAuthKeyPass=token.checkDLKey(tokenKey,tokenAuthKey);
	                					else if(tokenKey.startsWith("3"))
	                						isTokenAuthKeyPass=token.checkKey(tokenKey,tokenAuthKey); 
	                					
	                					if(!isTokenAuthKeyPass)
	                						return "122"; //验证不通过 
	                				}else
	                					return "120";     //令牌未进行初始化操作
	                			}
	                		}
	                	}else if((needusbHt.equals("1")&&userUsbType.equals("2"))) { //增加大开关控制 add by lvyi
	                		//td 128484 loginid、account字段整合
	                		//String username1 = Util.null2String(rs.getString("account"));
	                		String username1 = Util.null2String(rs.getString("loginid"));
	                		String serialNo = rs.getString("serial");
	                		//user.setNeedusb(needusb);
	                		//user.setSerial(serialNo);
	                		HTSrvAPI htsrv = new HTSrvAPI();
	                		String sharv = "";
	                		sharv = htsrv.HTSrvSHA1(rnd, rnd.length());
	
	                		sharv = sharv + "04040404";
	                		String ServerEncData = htsrv.HTSrvCrypt(0, serialNo, 0, sharv);
	                		if(serial.equals("0")){
	                			return "45";
	                		}else if(!username1.equals(username)){
	                			return "17";
	                		}else if(!ServerEncData.equals(serial)){
	                			return "16";
	                		}
							user.setNeedusb(needusb);
		                    user.setSerial(serialNo);
	                	}
                    } else {
                    	user.setNeedusb(0);
                    }
                } else {   //没有启用key
                	int needusbnetwork = Util.getIntValue(sci.getNeedusbnetwork());
                	
                	boolean isSysadmin = false;
									rs1.executeSql("select count(loginid) from HrmResourceManager where loginid = '"+login_id+"'");
									if(rs1.next() && rs1.getInt(1)>0){
										isSysadmin = true;
									}
					
									if(needusbnetwork == 1 && !isSysadmin) {   //启用网段策略
	                	if(checktmp) {  //如果在网段策略之外，必须用key登陆
	                		return "45";
	                	} else {  //网段策略之内，就不需要key正常登陆
	                		user.setNeedusb(0);
	                	}
                	} else {
                		user.setNeedusb(0);
                	}
                }

                user.setLoginip(request.getRemoteAddr());
                // request.getSession(true).setMaxInactiveInterval(60 * 60 * 24);
                request.getSession(true).setAttribute("weaver_user@bean", user);
                request.getSession(true).setAttribute("rtxlogin", "0");
                //request.getSession(true).setAttribute("moniter", new OnLineMonitor("" + user.getUID(),user.getLoginip()));

                Util.setCookie(response, "loginfileweaver", login_file, 172800);
                Util.setCookie(response, "loginidweaver", ""+user.getUID(), 172800);
                Util.setCookie(response, "languageidweaver", languageidweaver, 172800);
                if(MOREACCOUNTLANDING){
                //多帐号登陆

                if (user.getUID() != 1) {  //is not sysadmin
                    List accounts = this.getAccountsById(user.getUID());
                    request.getSession(true).setAttribute("accounts", accounts);
                	}
                	Util.setCookie(response, "loginfileweaver", login_file, 172800);
                	Util.setCookie(response, "loginidweaver", login_id, 172800);
                }
                char separater = Util.getSeparator();
                RecordSet rsLoginUpdate = new RecordSet();
                rsLoginUpdate.execute("HrmResource_UpdateLoginDate", ""+user.getUID() + separater + currentdate);
                //解决开启USB情况下时人员登录出现重复记录的问题 by sjh 54096
				//if("".equals(usbType) || usbType==null){
					SysMaintenanceLog log = new SysMaintenanceLog();
					log.resetParameter();
					log.setRelatedId(rs.getInt("id"));
					log.setRelatedName((rs.getString("firstname") + " " + rs.getString("lastname")).trim());
					log.setOperateType("6");
					log.setOperateDesc("");
					log.setOperateItem("60");
					log.setOperateUserid(rs.getInt("id"));
					log.setClientAddress(request.getRemoteAddr());
					log.setSysLogInfo();
                //}
                //检查是否有客户联系计划
                /* 刘煜修改 2004－05－08 登录检查客户联系计划以前是选取所有的记录，并不需要，现在更改为选择总数 */
                char flag = 2;
                String sql = "";
                rs1.executeProc("SysRemindInfo_InserCrmcontact", "" + rs.getInt("id") + flag + "0" + flag + "0");
                sql = " select count(*) from CRM_ContactLog where isfinished = 0 and contactdate ='" + currentdate + "' and resourceid =" + rs.getInt("id");
                rs1.executeSql(sql);
                if (rs1.next()) {
                    if (Util.getIntValue(rs1.getString(1), 0) > 0)
                        rs1.executeProc("SysRemindInfo_InserCrmcontact", "" + rs.getInt("id") + flag + "0" + flag + "1");

                } else {
                    return "15";
                }
            }

            if (login_type.equals("2")) {
                //add by sean.yang 2006-02-09 for TD3609
            	//edit by chujun 2009-03-05 TD10057
                ChgPasswdReminder reminder=new ChgPasswdReminder();
                RemindSettings settings=reminder.getRemindSettings();
                int needvalidate=settings.getNeedvalidate();//0: 否,1: 是

                String validateRand="";
                validateRand=Util.null2String((String)request.getSession(true).getAttribute("validateRand"));
                if(needvalidate==1){
                    if(!validateRand.toLowerCase().equals(validateCode.trim().toLowerCase()))
                        return "52";
                }
                rs.execute("CRM_CustomerInfo_SByLoginID", login_id);

                if (rs.next()) {
                	if (rs.getString("deleted").equals("1")) {
                        return "17";
                    }
                    if (!rs.getString("PortalPassword").equals(user_password)) {
                        return "16";
                    }

                    if (!rs.getString("PortalStatus").equals("2")) return "17";

                    User user = new User();

                    user.setUid(rs.getInt("id"));
                    user.setLoginid(login_id);
					user.setPwd(user_password);
                    user.setFirstname(rs.getString("name"));
                    //user.setLanguage(Util.getIntValue("7", 0));
					//Editted by zhaolg
                    String languageidweaver =Util.null2String(rs.getString("language"),"7");
                    if(!languid.equalsIgnoreCase(languageidweaver) && ismutilangua){
                    	rs2.execute("update CRM_CustomerInfo set  language = "+languid+" where id ="+rs.getInt("id"));
                    	languageidweaver = languid;
                    }
                    user.setLanguage(Util.getIntValue(languageidweaver, 0));
                    user.setUserDepartment(Util.getIntValue(rs.getString("department"), 0));
                    user.setUserSubCompany1(Util.getIntValue(rs.getString("subcompanyid1"), 0));
                    user.setManagerid(rs.getString("manager"));
                    user.setCountryid(rs.getString("country"));
                    user.setEmail(rs.getString("email"));
                    user.setAgent(Util.getIntValue(rs.getString("agent"), 0));
                    user.setType(Util.getIntValue(rs.getString("type"), 0));
                    user.setParentid(Util.getIntValue(rs.getString("parentid"), 0));
                    user.setProvince(Util.getIntValue(rs.getString("province"), 0));
                    user.setCity(Util.getIntValue(rs.getString("city"), 0));
                    user.setLogintype("2");
                    //user.setSeclevel(rs.getString("seclevel"));
                    user.setSeclevel(""+Util.getIntValue(rs.getString("setSeclevel"), 0));
                    user.setLoginip(request.getRemoteAddr());
                    user.setIsAdmin(isAdmin);
                    request.getSession(true).setAttribute("weaver_user@bean", user);
                    request.getSession(true).setAttribute("rtxlogin", "0");
                    //request.getSession(true).setAttribute("moniter", new OnLineMonitor());

                    Util.setCookie(response, "loginfileweaver", login_file, 172800);
                    Util.setCookie(response, "loginidweaver", ""+user.getUID(), 172800);
                    Util.setCookie(response, "languageidweaver", languageidweaver, 172800);

                    String para = "" + rs.getInt("id") + separator + currentdate + separator + currenttime + separator + request.getRemoteAddr();
                    rs.executeProc("CRM_LoginLog_Insert", para);

                    //检查是否有客户联系计划
                    /* 刘煜修改 2004－05－08 登录检查客户联系计划以前是选取所有的记录，并不需要，现在更改为选择总数 */
                    char flag = 2;
                    String sql = "";
                    rs1.executeProc("SysRemindInfo_InserCrmcontact", "" + rs.getInt("id") + flag + "1" + flag + "0");
                    sql = " select count(*) from CRM_ContactLog where isfinished = 0 and contactdate ='" + currentdate + "' and agentid =" + rs.getInt("id");
                    rs1.executeSql(sql);
                    if (rs1.next()) {
                        if (Util.getIntValue(rs1.getString(1), 0) > 0)
                            rs1.executeProc("SysRemindInfo_InserCrmcontact", "" + rs.getInt("id") + flag + "1" + flag + "1");
                    }
                } else {
                    return "15";
                }
            }
        } catch (Exception e) {
            writeLog(e);
            throw e;
        }
        return message;
    }


    /**
     * 检测当前用户是否需要受网段策略控制
     * 
     * @param loginId
     * @return
     */
    private boolean checkIsNeedIp(String loginId) {
        RecordSet rs = new RecordSet();
        rs.executeSql("select userusbtype,usbstate from hrmresource where loginid='"+loginId+"'");
        rs.next();
        String userusbtype = rs.getString("userusbtype");
        String usbstate = rs.getString("usbstate");
        //动态令牌 || 海泰key
        if((userusbtype.equals("3") && !usbstate.equals("1")) || (userusbtype.equals("2") && !usbstate.equals("1"))){
        	return false;
        }
		return true;
	}

	public String getUserCheck(HttpServletRequest request, HttpServletResponse response, String login_id, String user_password, String login_type, String login_file,String validateCode,String messages,String languid,boolean ismutilangua) throws Exception {
    	StaticObj staticobj = null;
        staticobj = StaticObj.getInstance();
        String frommail=Util.null2String(request.getParameter("frommail"));//针对华虹要求邮件直接登陆，不考虑安全性（内部使用）

        RecordSet rs = new RecordSet();
        RecordSet rs1 = new RecordSet();
        RecordSet rs2 = new RecordSet();
        char separator = Util.getSeparator();
        String message = "";

        Calendar today = Calendar.getInstance();
        String currentdate = Util.add0(today.get(Calendar.YEAR), 4) + "-" + Util.add0(today.get(Calendar.MONTH) + 1, 2) + "-" + Util.add0(today.get(Calendar.DAY_OF_MONTH), 2);
        String currenttime = Util.add0(today.get(Calendar.HOUR_OF_DAY), 2) + ":" + Util.add0(today.get(Calendar.MINUTE), 2) + ":" + Util.add0(today.get(Calendar.SECOND), 2);

        //judge the type of the account 
        String isADAccount = "";
        String isADAccountSql = "select isADAccount from hrmresource where loginid = '"+ login_id +"'";
        rs.executeSql(isADAccountSql);
        if(rs.next()) {
        	isADAccount = rs.getString("isADAccount");
        }
        String isUseLdap = Prop.getPropValue(GCONST.getConfigFile(), "authentic");
       
        
        boolean isAdmin = false;//Added by wcd 2014-11-21
        Document doc;
        try {

            String isLicense = (String) staticobj.getObject("isLicense");
            //国生 2004-9-16 , TD1096 , License的日期限制不起作用

            
            LN ckLicense = new LN();
            try{
            	message="1";
            }catch(Exception e){
            	return "19";
            }
            if (!message.equals("1")) {
                return "19";
            } else {
                staticobj.putObject("isLicense", "true");
            }
            
            //检测用户并发数start
			String concurrentFlag = Util.null2String(ckLicense.getConcurrentFlag());
			int hrmnumber = Util.getIntValue(ckLicense.getHrmnum());
			if("1".equals(concurrentFlag)){
				LicenseCheckLogin  lchl = new LicenseCheckLogin();
				if(lchl.getLicUserCheck(login_id,hrmnumber)){
					recordFefuseLogin(login_id); //拒绝登陆记录
					return "26";
				}
			}
            //检测用户并发数end

            message = "";

            String software = (String) staticobj.getObject("software");
            String portal = "n";
            String multilanguage = "n";
            if (software == null) {
                rs.executeSql("select * from license");
                if (rs.next()) {
                    software = rs.getString("software");
                    if (software.equals("")) software = "ALL";
                    staticobj.putObject("software", software);
                    portal = rs.getString("portal");
                    if (portal.equals("")) portal = "n";
                    staticobj.putObject("portal", portal);
                    multilanguage = rs.getString("multilanguage");
                    if (multilanguage.equals("")) multilanguage = "n";
                    staticobj.putObject("multilanguage", multilanguage);
                }
            }
            
            if(!checkIpSegByForbidLogin(request, login_id)){
            	return "88";
            }
            //ADD BY liudl 20160527 LDAP集成-AD认证时账号大小写不敏感（Oracle数据库）
            if (login_type.equals("1")) {
                ChgPasswdReminder reminder=new ChgPasswdReminder();
                RemindSettings settings=reminder.getRemindSettings();
                String usbserver = Prop.getPropValue(GCONST.getConfigFile(), "usbserver.ip");
                int needvalidate=settings.getNeedvalidate();//0: 否,1: 是
                isADAccountSql = "select isADAccount from hrmresource where upper(loginid) = '"+ login_id.toUpperCase() +"'";
                rs.executeSql(isADAccountSql);
                if(rs.next()) {
                	isADAccount = rs.getString("isADAccount");
                }
                String validateRand="";
                validateRand=Util.null2String((String)request.getSession(true).getAttribute("validateRand"));
                if(needvalidate==1){
                    if(!validateRand.toLowerCase().equals(validateCode.trim().toLowerCase()))
                        return "52";
                }
                /***解决AD同步过后分权管理员不能登录***/
                boolean isloginlpad = true;
                //查询语句改成大写
                if (ifEqlTarget(isADAccount, "1")){
                	rs.executeSql("select * from HrmResourceManager where upper(loginid) = '"+login_id.toUpperCase()+"'");
                	if(rs.next()) isloginlpad = false;
                }
                if (ifEqlTarget(isUseLdap, "ldap") && ifEqlTarget(isADAccount, "1") && isloginlpad) {
                    LdapUtil util = LdapUtil.getInstance();
                    boolean flag = true;
                    if(!"101".equals(messages)) {
                    	String authenticResult = util.authenticuser(login_id);//先判断用户是否存在AD中存在，或被禁用
                    	if(authenticResult == null || "uac".equals(authenticResult)) {
                    		return "17";
                    	}
                    	flag = util.authentic(login_id, user_password);
                    }
                    rs.executeSql("select * from HrmResource where upper(loginid)='" + login_id.toUpperCase() + "'");
                    if (flag && !"101".equals(messages)) {
                        if (!rs.next())
                            return "17";
                        else {
							int tmpid = rs.getInt("id");
                            int needdynapass_usr = rs.getInt("needdynapass");
                            int needdynapass_sys = settings.getNeeddynapass();
                            int dynapasslen = settings.getDynapasslen();
                            int passwordstateip = 1;
                            //查询语句改成大写
                            if(needdynapass_sys==1) {
                            	String sql = "select password,usbstate as passwordstate from HrmResource where (upper)loginid='"+login_id.toUpperCase()+"'";
                            	rs2.executeSql(sql);
                            	if(rs2.next()) passwordstateip = rs2.getInt("passwordstate");
                            }
                            boolean ipaddress = checkIpSeg(request, login_id, passwordstateip);//检查网段策略

                            if (needdynapass_usr == 1 && needdynapass_sys == 1 && dynapasslen > 0 && ipaddress) {
                                String mobileno = rs.getString("mobile");
                                if(sendOk("l-700", settings.getDypadcon(), dynapasslen, mobileno, currentdate+" "+currenttime, String.valueOf(tmpid), settings.getValiditySec()))
                                	return "101";
                            }
                        }
                    } else if("101".equals(messages)  && flag == true) {
                        if (!rs.next())
                            return "17";
                        else {
							int tmpid = rs.getInt("id");
                            int needdynapass_usr = rs.getInt("needdynapass");
                            int needdynapass_sys = settings.getNeeddynapass();
                            int dynapasslen = settings.getDynapasslen();
                            if (needdynapass_usr == 1 && needdynapass_sys == 1 && dynapasslen > 0) {
                            	rs1.executeSql("select password from hrmpassword where id=" + tmpid);
                                String pswd = "";
                                if(rs1.next()){
                                	pswd = StringUtil.vString(rs1.getString(1));
                                	if(pswd.length() == 0){
                                		if (rs.getString("password").equals(Util.getEncrypt(user_password))) {
											//重新发送

											if (sendOk("l-180", settings.getDypadcon(), dynapasslen, rs.getString("mobile"), currentdate + " " + currenttime, String.valueOf(tmpid), settings.getValiditySec()))
												return "101";
										} else {
                                			return "730";
                                    	}
                                	} else if(pswd.equals(Util.getEncrypt(user_password))){
                                		rs1.executeSql("update hrmpassword set password='' where id=" + tmpid);
                                	} else {
                                		return "57";
                                	}
                                } else {
                                	return "57";
                                }
                            }else
                                return "16";

                        }

                    } else if(!"101".equals(messages) && flag==false){
                    	 return "16";
                    }
                } else {
                    String[] loginCheck = checkUserPass(request,login_id,user_password,messages);
                    if (frommail.equals("1")) loginCheck = checkUserPassM(login_id,user_password);
                    if(loginCheck[0].equals("-2"))
                        return "55";
                    else if(loginCheck[0].equals("-1"))
                        return "17";
                    else if(loginCheck[1].equals("0"))
                            return "16";
                    else if(loginCheck[1].equals("101"))
                            return "101";
                    else if(loginCheck[1].equals("730"))
                        return "730";
                    else if(loginCheck[1].equals("57"))
                        return "57";
                        else if(loginCheck[0].equals("0")){
                                rs.executeSql("select * from HrmResource where loginid='"+login_id+"'");
                                rs.next();
                            }
                            else{
                            	isAdmin = true;
                                rs.executeSql("select * from HrmResourceManager where loginid='"+login_id+"'");
                                rs.next();
                            }

                    /*
                    if (rs.next()) {

                        if (rs.getString(1).equals("0")) {
                            return "16";
                        }
                    }else
                         return "17";
                    */
                }
                
                //用于检查重复登录

                ArrayList onlineuserids = null;
                onlineuserids = (ArrayList) staticobj.getObject("onlineuserids");
                if (onlineuserids != null) {
                    if (onlineuserids.indexOf("" + rs.getInt("id")) != -1) {
                        String sqltmp = "";
                        if (rs1.getDBType().equals("oracle")) {
                            sqltmp = "select * from (select * from HrmSysMaintenanceLog where relatedid = " + rs.getInt("id") + " order by id desc ) where rownum=1 ";
                        }else if(rs.getDBType().equals("db2")){
                            sqltmp = "select * from HrmSysMaintenanceLog where relatedid = "+rs.getInt("id") +" order by id desc fetch first 1 rows only ";
                        } else {
                            sqltmp = "select top 1 * from HrmSysMaintenanceLog where relatedid = " + rs.getInt("id") + " order by id desc";
                        }

                        rs1.executeSql(sqltmp);
                        if (rs1.next())
                            message = rs1.getString("clientaddress") + " " + rs1.getString("operatedate") + " " + rs1.getString("operatetime");
                    }
                }
                
                String startdate = rs.getString("startdate");
                String enddate = rs.getString("enddate");
/*

				if((currentdate.compareTo(startdate) < 0 && !startdate.equals("")) ||
				   (currentdate.compareTo(enddate) >0 && !enddate.equals("")))  return "17" ;
*/
                int status = rs.getInt("status");
                if (status != 0 && status != 1 && status != 2 && status != 3) {
                    return "17";
                }
                User user = new User();
                
                user.setUid(rs.getInt("id"));
                user.setLoginid(login_id);
                user.setFirstname(rs.getString("firstname"));
                user.setLastname(rs.getString("lastname"));
                user.setAliasname(rs.getString("aliasname"));
                user.setTitle(rs.getString("title"));
                user.setTitlelocation(rs.getString("titlelocation"));
                user.setSex(rs.getString("sex"));
				user.setPwd(user_password);
                String languageidweaver = rs.getString("systemlanguage");
                if(!languid.equalsIgnoreCase(languageidweaver) && ismutilangua){
                	rs2.execute("update hrmresource set  systemlanguage = "+languid+" where id ="+rs.getInt("id"));
                	languageidweaver = languid;
                }
                user.setLanguage(Util.getIntValue(languageidweaver, 0));

                user.setTelephone(rs.getString("telephone"));
                user.setMobile(rs.getString("mobile"));
                user.setMobilecall(rs.getString("mobilecall"));
                user.setEmail(rs.getString("email"));
                user.setCountryid(rs.getString("countryid"));
                user.setLocationid(rs.getString("locationid"));
                user.setResourcetype(rs.getString("resourcetype"));
                user.setStartdate(startdate);
                user.setEnddate(enddate);
                user.setContractdate(rs.getString("contractdate"));
                user.setJobtitle(rs.getString("jobtitle"));
                user.setJobgroup(rs.getString("jobgroup"));
                user.setJobactivity(rs.getString("jobactivity"));
                user.setJoblevel(rs.getString("joblevel"));
                user.setSeclevel(rs.getString("seclevel"));
                user.setUserDepartment(Util.getIntValue(rs.getString("departmentid"), 0));
                user.setUserSubCompany1(Util.getIntValue(rs.getString("subcompanyid1"), 0));
                user.setUserSubCompany2(Util.getIntValue(rs.getString("subcompanyid2"), 0));
                user.setUserSubCompany3(Util.getIntValue(rs.getString("subcompanyid3"), 0));
                user.setUserSubCompany4(Util.getIntValue(rs.getString("subcompanyid4"), 0));
                user.setManagerid(rs.getString("managerid"));
                user.setAssistantid(rs.getString("assistantid"));
                user.setPurchaselimit(rs.getString("purchaselimit"));
                user.setCurrencyid(rs.getString("currencyid"));
                user.setLastlogindate(currentdate);
                user.setLogintype("1");
                user.setAccount(rs.getString("account"));
                user.setIsAdmin(isAdmin);

                user.setLoginip(request.getRemoteAddr());
                // request.getSession(true).setMaxInactiveInterval(60 * 60 * 24);
                request.getSession(true).setAttribute("weaver_user@bean", user);
                request.getSession(true).setAttribute("rtxlogin", "0");
                //request.getSession(true).setAttribute("moniter", new OnLineMonitor("" + user.getUID(),user.getLoginip()));
//              多帐号登陆

                if (user.getUID() != 1) {  //is not sysadmin
                    List accounts = this.getAccountsById(user.getUID());
                    request.getSession(true).setAttribute("accounts", accounts);
                }
                Util.setCookie(response, "loginfileweaver", login_file, 172800);
                Util.setCookie(response, "loginidweaver", login_id, 172800);
                
                Util.setCookie(response, "loginfileweaver", login_file, 172800);
                Util.setCookie(response, "loginidweaver", ""+user.getUID(), 172800);
                Util.setCookie(response, "languageidweaver", languageidweaver, 172800);

                char separater = Util.getSeparator();
                RecordSet rsLoginUpdate = new RecordSet();
                rsLoginUpdate.execute("HrmResource_UpdateLoginDate", ""+user.getUID() + separater + currentdate);
                
                //int userNeedUSB = rs.getInt("userUsbType");
                //if(userNeedUSB!=2 && userNeedUSB!=3) {
                	SysMaintenanceLog log = new SysMaintenanceLog();
                	log.resetParameter();
                	log.setRelatedId(rs.getInt("id"));
                	log.setRelatedName((rs.getString("firstname") + " " + rs.getString("lastname")).trim());
                	log.setOperateType("6");
                	log.setOperateDesc("");
                	log.setOperateItem("60");
                	log.setOperateUserid(rs.getInt("id"));
                	log.setClientAddress(request.getRemoteAddr());
                	log.setSysLogInfo();
                //}

                //检查是否有客户联系计划
/* 刘煜修改 2004－05－08 登录检查客户联系计划以前是选取所有的记录，并不需要，现在更改为选择总数 */
                char flag = 2;
                String sql = "";
                rs1.executeProc("SysRemindInfo_InserCrmcontact", "" + rs.getInt("id") + flag + "0" + flag + "0");
                sql = " select count(*) from CRM_ContactLog where isfinished = 0 and contactdate ='" + currentdate + "' and resourceid =" + rs.getInt("id");
                rs1.executeSql(sql);
                if (rs1.next()) {
                    if (Util.getIntValue(rs1.getString(1), 0) > 0)
                        rs1.executeProc("SysRemindInfo_InserCrmcontact", "" + rs.getInt("id") + flag + "0" + flag + "1");
                } else {
                  return "15";
                }
            }

            if (login_type.equals("2")) {
                //add by sean.yang 2006-02-09 for TD3609
            	//edit by chujun 2009-03-05 TD10057
                ChgPasswdReminder reminder=new ChgPasswdReminder();
                RemindSettings settings=reminder.getRemindSettings();
                int needvalidate=settings.getNeedvalidate();//0: 否,1: 是

                String validateRand="";
                validateRand=Util.null2String((String)request.getSession(true).getAttribute("validateRand"));
                if(needvalidate==1){
                    if(!validateRand.toLowerCase().equals(validateCode.trim().toLowerCase()))
                        return "52";
                }

                rs.execute("CRM_CustomerInfo_SByLoginID", login_id);

                if (rs.next()) {
                	if (rs.getString("deleted").equals("1")) {
                        return "17";
                    }
                    if (!rs.getString("PortalPassword").equals(user_password)) {
                        return "16";
                    }

                    if (!rs.getString("PortalStatus").equals("2")) return "17";
                    User user = new User();
                    
                    user.setUid(rs.getInt("id"));
                    user.setLoginid(login_id);
					user.setPwd(user_password);
                    user.setFirstname(rs.getString("name"));
                    // user.setLanguage(Util.getIntValue("7", 0));
				    //Editted by zhaolg
                    String languageidweaver =Util.null2String(rs.getString("language"),"7");
                    if(!languid.equalsIgnoreCase(languageidweaver) && ismutilangua){
                    	rs2.execute("update CRM_CustomerInfo set  language = "+languid+" where id ="+rs.getInt("id"));
                    	languageidweaver = languid;
                    }
                    user.setLanguage(Util.getIntValue(languageidweaver, 0));
                    user.setUserDepartment(Util.getIntValue(rs.getString("department"), 0));
                    user.setUserSubCompany1(Util.getIntValue(rs.getString("subcompanyid1"), 0));
                    user.setManagerid(rs.getString("manager"));
                    user.setCountryid(rs.getString("country"));
                    user.setEmail(rs.getString("email"));
                    user.setAgent(Util.getIntValue(rs.getString("agent"), 0));
                    user.setType(Util.getIntValue(rs.getString("type"), 0));
                    user.setParentid(Util.getIntValue(rs.getString("parentid"), 0));
                    user.setProvince(Util.getIntValue(rs.getString("province"), 0));
                    user.setCity(Util.getIntValue(rs.getString("city"), 0));
                    user.setLogintype("2");
                    //user.setSeclevel(rs.getString("seclevel"));
                    user.setSeclevel(""+Util.getIntValue(rs.getString("setSeclevel"), 0));
                    user.setLoginip(request.getRemoteAddr());
                    request.getSession(true).setAttribute("weaver_user@bean", user);
                    request.getSession(true).setAttribute("rtxlogin", "0");
                    user.setIsAdmin(isAdmin);
                    //request.getSession(true).setAttribute("moniter", new OnLineMonitor());

                    Util.setCookie(response, "loginfileweaver", login_file, 172800);
                    Util.setCookie(response, "loginidweaver", ""+user.getUID(), 172800);
                    Util.setCookie(response, "languageidweaver", languageidweaver, 172800);

                    String para = "" + rs.getInt("id") + separator + currentdate + separator + currenttime + separator + request.getRemoteAddr();
                    rs.executeProc("CRM_LoginLog_Insert", para);

                    //检查是否有客户联系计划
/* 刘煜修改 2004－05－08 登录检查客户联系计划以前是选取所有的记录，并不需要，现在更改为选择总数 */
                    char flag = 2;
                    String sql = "";
                    rs1.executeProc("SysRemindInfo_InserCrmcontact", "" + rs.getInt("id") + flag + "1" + flag + "0");
                    sql = " select count(*) from CRM_ContactLog where isfinished = 0 and contactdate ='" + currentdate + "' and agentid =" + rs.getInt("id");
                    rs1.executeSql(sql);
                    if (rs1.next()) {
                        if (Util.getIntValue(rs1.getString(1), 0) > 0)
                            rs1.executeProc("SysRemindInfo_InserCrmcontact", "" + rs.getInt("id") + flag + "1" + flag + "1");
                    }
                } else {
                    return "15";
                }
            }
        } catch (Exception e) {
            writeLog(e);
            throw e;
        }
        return message;
    }
    public String getElementUserCheck(HttpServletRequest request, HttpServletResponse response, String login_id, String user_password, String login_type, String login_file,String validateCode,String messages,String languid,boolean ismutilangua) throws Exception {
    	StaticObj staticobj = null;
        staticobj = StaticObj.getInstance();
        String frommail=Util.null2String(request.getParameter("frommail"));//针对华虹要求邮件直接登陆，不考虑安全性（内部使用）

        RecordSet rs = new RecordSet();
        RecordSet rs1 = new RecordSet();
        RecordSet rs2 = new RecordSet();
        char separator = Util.getSeparator();
        String message = "";

        Calendar today = Calendar.getInstance();
        String currentdate = Util.add0(today.get(Calendar.YEAR), 4) + "-" + Util.add0(today.get(Calendar.MONTH) + 1, 2) + "-" + Util.add0(today.get(Calendar.DAY_OF_MONTH), 2);
        String currenttime = Util.add0(today.get(Calendar.HOUR_OF_DAY), 2) + ":" + Util.add0(today.get(Calendar.MINUTE), 2) + ":" + Util.add0(today.get(Calendar.SECOND), 2);

        //judge the type of the account 
        String isADAccount = "";
        String isADAccountSql = "select isADAccount from hrmresource where loginid = '"+ login_id +"'";
        rs.executeSql(isADAccountSql);
        if(rs.next()) {
        	isADAccount = rs.getString("isADAccount");
        }
        String isUseLdap = Prop.getPropValue(GCONST.getConfigFile(), "authentic");
        
        boolean isAdmin = false;
        Document doc;
        try 
        {
            String isLicense = (String) staticobj.getObject("isLicense");
            //国生 2004-9-16 , TD1096 , License的日期限制不起作用

            
            LN ckLicense = new LN();
            try{
            	message="1";
            }catch (Exception e) {
            	return "19";
			}
            if (!message.equals("1")) {
                return "19";
            } else {
                staticobj.putObject("isLicense", "true");
            }
            
            //检测用户并发数start
			String concurrentFlag = Util.null2String(ckLicense.getConcurrentFlag());
			int hrmnumber = Util.getIntValue(ckLicense.getHrmnum());
			if("1".equals(concurrentFlag)){
				LicenseCheckLogin  lchl = new LicenseCheckLogin();
				if(lchl.getLicUserCheck(login_id,hrmnumber)){
					recordFefuseLogin(login_id); //拒绝登陆记录
					return "26";
				}
			}
            //检测用户并发数end

            message = "";

            String software = (String) staticobj.getObject("software");
            String portal = "n";
            String multilanguage = "n";
            if (software == null) {
                rs.executeSql("select * from license");
                if (rs.next()) {
                    software = rs.getString("software");
                    if (software.equals("")) software = "ALL";
                    staticobj.putObject("software", software);
                    portal = rs.getString("portal");
                    if (portal.equals("")) portal = "n";
                    staticobj.putObject("portal", portal);
                    multilanguage = rs.getString("multilanguage");
                    if (multilanguage.equals("")) multilanguage = "n";
                    staticobj.putObject("multilanguage", multilanguage);
                }
            }
            
            if(!checkIpSegByForbidLogin(request, login_id)){
            	return "88";
            }

            if (login_type.equals("1")) {
                ChgPasswdReminder reminder=new ChgPasswdReminder();
                RemindSettings settings=reminder.getRemindSettings();

                //String mode = Prop.getPropValue(GCONST.getConfigFile(), "authentic");
                String usbserver = Prop.getPropValue(GCONST.getConfigFile(), "usbserver.ip");
                int needvalidate=settings.getNeedvalidate();//0: 否,1: 是

                String validateRand="";
                validateRand=Util.null2String((String)request.getSession(true).getAttribute("validateRand"));
                if(needvalidate==1){
                    if(!validateRand.toLowerCase().equals(validateCode.trim().toLowerCase()))
                        return "52";
                }
                /***解决AD同步过后分权管理员不能登录***/
                boolean isloginlpad = true;
                if (ifEqlTarget(isADAccount, "1")){
                	rs.executeSql("select * from HrmResourceManager where loginid = '"+login_id+"'");
                	if(rs.next()) isloginlpad = false;
                }

                if (ifEqlTarget(isUseLdap, "ldap") && ifEqlTarget(isADAccount, "1") && isloginlpad) {
                    LdapUtil util = LdapUtil.getInstance();
                    String authenticResult = util.authenticuser(login_id);//先判断用户是否存在AD中存在，或被禁用
                	if(authenticResult == null || "uac".equals(authenticResult)) {
                		return "17";
                	}
                    boolean flag = util.authentic(login_id, user_password);
                    rs.executeSql("select * from HrmResource where loginid='" + login_id + "'");
                    if (flag) {
                        if (!rs.next())
                            return "17";
                        else {
							int tmpid = rs.getInt("id");
                            int needdynapass_usr = rs.getInt("needdynapass");
                            int needdynapass_sys = settings.getNeeddynapass();
                            int dynapasslen = settings.getDynapasslen();
                            int passwordstateip = 1;
                            if(needdynapass_sys==1) {
                            	String sql = "select password,usbstate as passwordstate from HrmResource where loginid='"+login_id+"'";
                            	rs2.executeSql(sql);
                            	if(rs2.next()) passwordstateip = rs2.getInt("passwordstate");
                            }
                            boolean ipaddress = checkIpSeg(request, login_id, passwordstateip);//检查网段策略

                            if (needdynapass_usr == 1 && needdynapass_sys == 1 && dynapasslen > 0 && ipaddress) {
                                String mobileno = rs.getString("mobile");
                                if(sendOk("l-1110", settings.getDypadcon(), dynapasslen, mobileno, currentdate+" "+currenttime, String.valueOf(tmpid), settings.getValiditySec()))
                                	return "101";
                            }
                        }
                    } else if("101".equals(messages)  && flag == true) {
                        if (!rs.next())
                            return "17";
                        else {
							int tmpid = rs.getInt("id");
                            int needdynapass_usr = rs.getInt("needdynapass");
                            int needdynapass_sys = settings.getNeeddynapass();
                            int dynapasslen = settings.getDynapasslen();
                            if (needdynapass_usr == 1 && needdynapass_sys == 1 && dynapasslen > 0) {
                            	rs1.executeSql("select password from hrmpassword where id=" + tmpid);
                                String pswd = "";
                                if(rs1.next()){
                                	pswd = StringUtil.vString(rs1.getString(1));
                                	if(pswd.length() == 0){
                                		if (rs.getString("password").equals(Util.getEncrypt(user_password))) {
											//重新发送

											if (sendOk("l-180", settings.getDypadcon(), dynapasslen, rs.getString("mobile"), currentdate + " " + currenttime, String.valueOf(tmpid), settings.getValiditySec()))
												return "101";
										} else {
                                			return "730";
                                    	}
                                	} else if(pswd.equals(Util.getEncrypt(user_password))){
                                		rs1.executeSql("update hrmpassword set password='' where id=" + tmpid);
                                	} else {
                                		return "57";
                                	}
                                } else {
                                	return "57";
                                }
                            }else
                                return "16";

                        }

                    } else if(!"101".equals(messages) && flag==false){
                    	 return "16";
                    }
                } else {
                    String[] loginCheck = checkUserPass(request,login_id,user_password,messages);
                    if (frommail.equals("1")) loginCheck = checkUserPassM(login_id,user_password);
                    if(loginCheck[0].equals("-2"))
                        return "55";
                    else if(loginCheck[0].equals("-1"))
                        return "17";
                    else if(loginCheck[1].equals("0"))
                            return "16";
                    else if(loginCheck[1].equals("101"))
                            return "101";
                    else if(loginCheck[1].equals("730"))
                        return "730";
                    else if(loginCheck[1].equals("57"))
                        return "57";
                    else if(loginCheck[0].equals("0")){
                            rs.executeSql("select * from HrmResource where loginid='"+login_id+"'");
                            rs.next();
                    }
                    else{
                    	isAdmin = true;
                        rs.executeSql("select * from HrmResourceManager where loginid='"+login_id+"'");
                        rs.next();
                    }

                    /*
                    if (rs.next()) {

                        if (rs.getString(1).equals("0")) {
                            return "16";
                        }
                    }else
                         return "17";
                    */
                }
                
                
                
                String startdate = rs.getString("startdate");
                String enddate = rs.getString("enddate");
/*

				if((currentdate.compareTo(startdate) < 0 && !startdate.equals("")) ||
				   (currentdate.compareTo(enddate) >0 && !enddate.equals("")))  return "17" ;
*/
                int status = rs.getInt("status");
                if (status != 0 && status != 1 && status != 2 && status != 3) {
                    return "17";
                }
                User user = new User();
                
                user.setUid(rs.getInt("id"));
                user.setLoginid(login_id);
                user.setIsAdmin(isAdmin);
                user.setLoginip(request.getRemoteAddr());
                // request.getSession(true).setMaxInactiveInterval(60 * 60 * 24);
                request.getSession(true).setAttribute("weaver_user@bean", user);
                request.getSession(true).setAttribute("rtxlogin", "0");
//              多帐号登陆

                if (user.getUID() != 1) {  //is not sysadmin
                    List accounts = this.getAccountsById(user.getUID());
                    request.getSession(true).setAttribute("accounts", accounts);
                }
                //检查是否有客户联系计划
/* 刘煜修改 2004－05－08 登录检查客户联系计划以前是选取所有的记录，并不需要，现在更改为选择总数 */
                char flag = 2;
                String sql = "";
                sql = " select count(*) from CRM_ContactLog where isfinished = 0 and contactdate ='" + currentdate + "' and resourceid =" + rs.getInt("id");
                rs1.executeSql(sql);
                if (rs1.next()) 
                {
                    if (Util.getIntValue(rs1.getString(1), 0) > 0)
                        rs1.executeProc("SysRemindInfo_InserCrmcontact", "" + rs.getInt("id") + flag + "0" + flag + "1");
                } 
                else 
                {
                  return "15";
                }
            }

            if (login_type.equals("2")) {
                //add by sean.yang 2006-02-09 for TD3609
            	//edit by chujun 2009-03-05 TD10057
                ChgPasswdReminder reminder=new ChgPasswdReminder();
                RemindSettings settings=reminder.getRemindSettings();
                int needvalidate=settings.getNeedvalidate();//0: 否,1: 是

                String validateRand="";
                validateRand=Util.null2String((String)request.getSession(true).getAttribute("validateRand"));
                if(needvalidate==1){
                    if(!validateRand.toLowerCase().equals(validateCode.trim().toLowerCase()))
                        return "52";
                }

                rs.execute("CRM_CustomerInfo_SByLoginID", login_id);

                if (rs.next()) {
                	if (rs.getString("deleted").equals("1")) {
                        return "17";
                    }
                    if (!rs.getString("PortalPassword").equals(user_password)) {
                        return "16";
                    }

                    if (!rs.getString("PortalStatus").equals("2")) return "17";
                    User user = new User();
                    
                    user.setUid(rs.getInt("id"));
                    user.setLoginid(login_id);
					
                    request.getSession(true).setAttribute("weaver_user@bean", user);
                    request.getSession(true).setAttribute("rtxlogin", "0");
                } 
                else 
                {
                    return "15";
                }
            }
        } catch (Exception e) {
            writeLog(e);
            throw e;
        }
        return message;
    }
    public List getAccountsById(int id) {
        RecordSet rs = new RecordSet();
        List l = new ArrayList();
        String sql = "select id,loginid,subcompanyid1,departmentid,jobtitle,accounttype,belongto from hrmresource where id=" + id +" and status<4 order by dsporder asc,id asc";


        rs.executeSql(sql);
        if (rs.next()) {
            String accounttype = rs.getString("accounttype");
            if (accounttype == null || accounttype.equals("") || accounttype.equals("0")) { //the logging account is a major account
                Account major_account = new Account();
                major_account.setId(rs.getInt("id"));
                major_account.setSubcompanyid(rs.getInt("subcompanyid1"));
                major_account.setDepartmentid(rs.getInt("departmentid"));
                major_account.setJobtitleid(rs.getInt("jobtitle"));
                major_account.setType(0);
                major_account.setAccount(rs.getString("loginid"));
                l.add(major_account);
                sql = "select id,loginid,subcompanyid1,departmentid,jobtitle,accounttype,belongto from hrmresource where accounttype=1 and belongto=" + rs.getInt("id")+" and status<4 order by dsporder asc,id asc";
                rs.executeSql(sql);
                while (rs.next()) {
                	Account accessory_account = new Account();
                    accessory_account.setId(rs.getInt("id"));
                    accessory_account.setSubcompanyid(rs.getInt("subcompanyid1"));
                    accessory_account.setDepartmentid(rs.getInt("departmentid"));
                    accessory_account.setJobtitleid(rs.getInt("jobtitle"));
                    accessory_account.setType(1);
                    accessory_account.setAccount(rs.getString("loginid"));
                    l.add(accessory_account);
                }
                return l;
            } else {   //the logging account is a accessory account
                String major_id = rs.getString("belongto");
                sql = "select id,loginid,subcompanyid1,departmentid,jobtitle,accounttype,belongto from hrmresource where id=" + major_id+" and status<4 order by dsporder asc,id asc";
                rs.executeSql(sql);
                if (rs.next()) {
                    Account major_account = new Account();
                    major_account.setId(rs.getInt("id"));
                    major_account.setSubcompanyid(rs.getInt("subcompanyid1"));
                    major_account.setDepartmentid(rs.getInt("departmentid"));
                    major_account.setJobtitleid(rs.getInt("jobtitle"));
                    major_account.setType(0);
                    major_account.setAccount(rs.getString("loginid"));
                    l.add(major_account);
                    sql = "select id,loginid,subcompanyid1,departmentid,jobtitle,accounttype,belongto from hrmresource where belongto=" + major_id+" and status<4 order by dsporder asc,id asc";
                    rs.executeSql(sql);
                    while (rs.next()) {
                    	Account accessory_account = new Account();
                        accessory_account.setId(rs.getInt("id"));
                        accessory_account.setSubcompanyid(rs.getInt("subcompanyid1"));
                        accessory_account.setDepartmentid(rs.getInt("departmentid"));
                        accessory_account.setJobtitleid(rs.getInt("jobtitle"));
                        accessory_account.setType(1);
                        accessory_account.setAccount(rs.getString("loginid"));
                        l.add(accessory_account);
                    }
                    return l;
                } else
                    return null;
            }

        } else
            return null;
    }	
    
    public String shiftIdentity(HttpServletRequest request, HttpServletResponse response, int id) throws Exception {
        StaticObj staticobj = null;
        staticobj = StaticObj.getInstance();

        if(!checkIpSegByForbidLogin(request, String.valueOf(id))){
        	return "88";
        }
        
        RecordSet rs = new RecordSet();
        RecordSet rs1 = new RecordSet();
        char separator = Util.getSeparator();
        String message = "";
        boolean MOREACCOUNTLANDING = GCONST.getMOREACCOUNTLANDING();
        Calendar today = Calendar.getInstance();
        String currentdate = Util.add0(today.get(Calendar.YEAR), 4) + "-" + Util.add0(today.get(Calendar.MONTH) + 1, 2) + "-" + Util.add0(today.get(Calendar.DAY_OF_MONTH), 2);
        String currenttime = Util.add0(today.get(Calendar.HOUR_OF_DAY), 2) + ":" + Util.add0(today.get(Calendar.MINUTE), 2) + ":" + Util.add0(today.get(Calendar.SECOND), 2);
        rs.executeProc("HrmResource_SelectByID",""+id);
        rs.next();
        try {


            ArrayList onlineuserids = null;
            onlineuserids = (ArrayList) staticobj.getObject("onlineuserids");
            if (onlineuserids != null) {
                if (onlineuserids.indexOf("" + id) != -1) {
                    String sqltmp = "";
                    if (rs1.getDBType().equals("oracle")) {
                        sqltmp = "select * from (select * from HrmSysMaintenanceLog where relatedid = " + rs.getInt("id") + " order by id desc ) where rownum=1 ";
                    } else {
                        sqltmp = "select top 1 * from HrmSysMaintenanceLog where relatedid = " + rs.getInt("id") + " order by id desc";
                    }

                    rs1.executeSql(sqltmp);
                    if (rs1.next())
                        message = rs1.getString("clientaddress") + " " + rs1.getString("operatedate") + " " + rs1.getString("operatetime");
                }
            }

            String startdate = rs.getString("startdate");
            String enddate = rs.getString("enddate");
/*

				if((currentdate.compareTo(startdate) < 0 && !startdate.equals("")) ||
				   (currentdate.compareTo(enddate) >0 && !enddate.equals("")))  return "17" ;
*/
            int status = Util.getIntValue(rs.getString("status"));
            if (status != 0 && status != 1 && status != 2 && status != 3) {
                return "17";
            }
            
            User user = new User();

            user.setUid(rs.getInt("id"));
            user.setLoginid(rs.getString("loginid"));
            user.setFirstname(rs.getString("firstname"));
            user.setLastname(rs.getString("lastname"));
            user.setAliasname(rs.getString("aliasname"));
            user.setTitle(rs.getString("title"));
            user.setTitlelocation(rs.getString("titlelocation"));
            user.setSex(rs.getString("sex"));
            user.setLanguage(Util.getIntValue(rs.getString("systemlanguage"), 0));
            user.setTelephone(rs.getString("telephone"));
            user.setMobile(rs.getString("mobile"));
            user.setMobilecall(rs.getString("mobilecall"));
            user.setEmail(rs.getString("email"));
            user.setCountryid(rs.getString("countryid"));
            user.setLocationid(rs.getString("locationid"));
            user.setResourcetype(rs.getString("resourcetype"));
            user.setStartdate(startdate);
            user.setEnddate(enddate);
            user.setContractdate(rs.getString("contractdate"));
            user.setJobtitle(rs.getString("jobtitle"));
            user.setJobgroup(rs.getString("jobgroup"));
            user.setJobactivity(rs.getString("jobactivity"));
            user.setJoblevel(rs.getString("joblevel"));
            user.setSeclevel(rs.getString("seclevel"));
            user.setUserDepartment(Util.getIntValue(rs.getString("departmentid"), 0));
            user.setUserSubCompany1(Util.getIntValue(rs.getString("subcompanyid1"), 0));
            user.setUserSubCompany2(Util.getIntValue(rs.getString("subcompanyid2"), 0));
            user.setUserSubCompany3(Util.getIntValue(rs.getString("subcompanyid3"), 0));
            user.setUserSubCompany4(Util.getIntValue(rs.getString("subcompanyid4"), 0));
            user.setManagerid(rs.getString("managerid"));
            user.setAssistantid(rs.getString("assistantid"));
            user.setPurchaselimit(rs.getString("purchaselimit"));
            user.setCurrencyid(rs.getString("currencyid"));
            user.setLastlogindate(currentdate);
            user.setLogintype("1");

            user.setLoginip(request.getRemoteAddr());
            // request.getSession(true).setMaxInactiveInterval(60 * 60 * 24);
            request.getSession(true).setAttribute("weaver_user@bean", user);
            request.getSession(true).setAttribute("rtxlogin", "0");
            //request.getSession(true).setAttribute("moniter", new OnLineMonitor("" + user.getUID(),user.getLoginip()));
            if(MOREACCOUNTLANDING){
            //多帐号登陆

            if (user.getUID() != 1) {
                List accounts = this.getAccountsById(user.getUID());
                request.getSession(true).setAttribute("accounts", accounts);
            }
            	Util.setCookie(response, "loginfileweaver", "/login/Login.jsp?logintype=1", 172800);
            	Util.setCookie(response, "loginidweaver", user.getLoginid(), 172800);
            }
            char separater = Util.getSeparator();
            RecordSet rsLoginUpdate = new RecordSet();
            rsLoginUpdate.execute("HrmResource_UpdateLoginDate", ""+user.getUID() + separater + currentdate);

            SysMaintenanceLog log = new SysMaintenanceLog();
            log.resetParameter();
            log.setRelatedId(rs.getInt("id"));
            log.setRelatedName((rs.getString("firstname") + " " + rs.getString("lastname")).trim());
            log.setOperateType("6");
            log.setOperateDesc("");
            log.setOperateItem("60");
            log.setOperateUserid(rs.getInt("id"));
            log.setClientAddress(request.getRemoteAddr());
            log.setSysLogInfo();

            //检查是否有客户联系计划
            /* 刘煜修改 2004－05－08 登录检查客户联系计划以前是选取所有的记录，并不需要，现在更改为选择总数 */
            char flag = 2;
            String sql = "";
            rs1.executeProc("SysRemindInfo_InserCrmcontact", "" + rs.getInt("id") + flag + "0" + flag + "0");
            sql = " select count(*) from CRM_ContactLog where isfinished = 0 and contactdate ='" + currentdate + "' and resourceid =" + rs.getInt("id");
            rs1.executeSql(sql);
            if (rs1.next()) {
                if (Util.getIntValue(rs1.getString(1), 0) > 0)
                    rs1.executeProc("SysRemindInfo_InserCrmcontact", "" + rs.getInt("id") + flag + "0" + flag + "1");
            }
        }


        catch (Exception e) {
            writeLog(e);
            throw e;
        }
        return message;
    }
    
    /*
     * 基于安全考虑把原来用存储过程HrmResource_SByLoginIDPass改为此方法来运算
     * returnValue[0]
     * 0：普通用户；1：系统管理员
     * returnValue[1]
     * 0: 用户存在但秘密不对；1：用户密码都正确
    */
    public String[] checkUserPass(HttpServletRequest request, String loginid, String pass,String messages){
    	String ClientIP = request.getRemoteAddr();
        String[] returnValue = new String[2];
        returnValue[0]="-1"; //0：普通用户；1：系统管理员
        returnValue[1]="-1"; //0: 用户存在但秘密不对；1：用户密码都正确
        
        if (loginid.indexOf(";")>-1||loginid.indexOf("--")>-1||loginid.indexOf(" ")>-1||loginid.indexOf("'")>-1) {
            writeLog("illegal sql statement input loginid:" + loginid);
            returnValue[0]="-2";
            return returnValue;
        }
        //Modified by wcd 2015-01-05 管理员也拥有辅助校验方式
        SMSManager sm = new SMSManager();
        final HrmSettingsComInfo sci=new HrmSettingsComInfo();
        int needdynapass_sys = Util.getIntValue(sci.getNeeddynapass());
        int dynapasslen = Util.getIntValue(sci.getDynapasslen());
        //动态密码内容

        String dypadcon = Util.null2String(sci.getDypadcon());
        boolean ipaddress= false; 
        int passwordstateip = 1;
        int needdynapass = 0;
        String mobile = "";
        
        RecordSet rs = new RecordSet();
        RecordSet rs1 = new RecordSet();
        RecordSet rs2 = new RecordSet();
        String sql = "";
        String idTemp ="0";
        String passwordTemp ="";
        sql = "select id,needdynapass,mobile,usbstate as passwordstate from HrmResource where loginid='"+loginid+"' and (accounttype is null  or accounttype=0)";
        rs.executeSql(sql);
        if(rs.next()&&(Util.getIntValue(rs.getString(1),0)>0)) {
            idTemp = rs.getString(1);
            returnValue[0] = "0";
            returnValue[1] = "0";
            needdynapass = rs.getInt(2);
            if(needdynapass!=0&&needdynapass!=1){
            	needdynapass= 1;
            }
            mobile = rs.getString(3);
            /*
             * 系统人员导入有可能产生needdynapass,usbstate as passwordstate等字段为NUll，在hrmpassword表中也没有

             * 添加ID和Loginid，这些字段为Null或在表中没有记录，会出现登陆验证错误或动态密码错误。

             * */
            if(needdynapass==1){
            	rs1.executeSql("select id from hrmpassword where id='"+idTemp+"'") ;
            	if(rs1.next()) ;
            	else{                 
                    rs1.executeSql("insert into hrmpassword(id,loginid) values("+idTemp+",'"+loginid+"')") ;
                   }
            }
            sql = "select password,usbstate as passwordstate from HrmResource where id= " + idTemp;
            rs.executeSql(sql);
            if (rs.next()) {
                passwordTemp = Util.null2String(rs.getString(1));
                if (needdynapass != 1) {
                    if (passwordTemp.equals(Util.getEncrypt(pass)))
                        returnValue[1] = "1";
                } else {
                    if (passwordTemp.equals(Util.getEncrypt(pass))&&!"101".equals(messages)&&!"57".equals(messages)) {
                            if(needdynapass_sys==1) {
                            	sql = "select password,usbstate as passwordstate from HrmResource where loginid='"+loginid+"'";
                            	rs2.executeSql(sql);
                            	if(rs2.next()) passwordstateip = rs2.getInt("passwordstate");
                            }
                            ipaddress = checkIpSeg(request, loginid, passwordstateip);//检查网段策略

                            if ((passwordstateip==0 || passwordstateip ==2) && dynapasslen > 0&&ipaddress) {
                                if(sendOk("l-1540", sci.getDypadcon(), dynapasslen, mobile, TimeUtil.getCurrentTimeString(), idTemp, sci.getValiditySec(), ",IP："+ClientIP))
                                	returnValue[1] = "101";
                            	else 
                            		returnValue[1] = "1";
                            } else
                                returnValue[1] = "1";
                    } else if("101".equals(messages) || "57".equals(messages)){
                        rs.executeSql("select password from hrmpassword where id=" + idTemp);
                        String pswd = "";
                        if(rs.next()){
                        	pswd = StringUtil.vString(rs.getString(1));
                        	if(pswd.length() == 0){
                        		if(passwordTemp.equals(Util.getEncrypt(pass))){
	                        		//重新发送

	                                if(sendOk("l-1550", sci.getDypadcon(), dynapasslen, mobile, TimeUtil.getCurrentTimeString(), idTemp, sci.getValiditySec(), ",IP："+ClientIP))
	                                	returnValue[1] = "101";
	                            	else 
	                            		returnValue[1] = "1";
                        		} else {
                            		returnValue[1] = "730";
                            	}
                        	} else if(pswd.equals(Util.getEncrypt(pass))){
                        		rs.executeSql("update hrmpassword set password='' where id=" + idTemp);
                                returnValue[1] = "1";
                        	} else {
                        		returnValue[1] = "57";
                        	}
                        } else {
                        	returnValue[1] = "57";
                        }
                    } else {
						if(!passwordTemp.equals(Util.getEncrypt(pass))){
							returnValue[1] = "0";
						}
					}
                }
            }
        } else{
        	rs.executeProc("SystemSet_Select","");//fix TD4867 不分权时除systemadmin其他帐号不能登陆
        	rs.next();
        	String detachable= Util.null2String(rs.getString("detachable"));
            sql = "select count(id),id from HrmResourceManager where loginid='"+loginid+"' group by id";
            rs.executeSql(sql);
            if(rs.next()&&(Util.getIntValue(rs.getString(1),0)>0)){
            	if (!detachable.equals("1") && !loginid.equalsIgnoreCase("sysadmin")){
            		returnValue[0]="-1";
            		returnValue[1] = "0";
            		return returnValue;
            	}
            	
                idTemp = rs.getString(2);
                returnValue[0]="1";
                returnValue[1] = "0";
                
                sql = "select password,userUsbType,usbstate,mobile from HrmResourceManager where id= " + idTemp;
                rs.executeSql(sql);
                if(rs.next()){
                    passwordTemp = Util.null2String(rs.getString(1));
                    needdynapass = rs.getInt(2);
                    mobile = rs.getString(4);
                    if (needdynapass != 4) {
	                    if(passwordTemp.equals(Util.getEncrypt(pass))) returnValue[1] = "1";
                    } else {
                    	rs1.executeSql("select id from hrmpassword where id='"+idTemp+"'") ;
                    	if(rs1.next()) ;
                    	else rs1.executeSql("insert into hrmpassword(id,loginid) values("+idTemp+",'"+loginid+"')");
                    	if (passwordTemp.equals(Util.getEncrypt(pass)) && !"101".equals(messages) && !"57".equals(messages)) {
							if (needdynapass_sys == 1) {
								passwordstateip = rs.getInt(3);
							}
							ipaddress = checkIpSeg(request, loginid, passwordstateip);// 检查网段策略

							if ((passwordstateip == 0 || passwordstateip == 2) && dynapasslen > 0 && ipaddress) {
								if(sendOk("l-1610", sci.getDypadcon(), dynapasslen, mobile, TimeUtil.getCurrentTimeString(), idTemp, sci.getValiditySec(), ",IP："+ClientIP))
                                	returnValue[1] = "101";
                            	else 
                            		returnValue[1] = "1";
							} else
								returnValue[1] = "1";
						}  else if("101".equals(messages) || "57".equals(messages)){
							rs.executeSql("select password from hrmpassword where id=" + idTemp);
	                        String pswd = "";
	                        if(rs.next()){
	                        	pswd = StringUtil.vString(rs.getString(1));
	                        	if(pswd.length() == 0){
	                        		if(passwordTemp.equals(Util.getEncrypt(pass))){
		                        		//重新发送

		                                if(sendOk("l-1550", sci.getDypadcon(), dynapasslen, mobile, TimeUtil.getCurrentTimeString(), idTemp, sci.getValiditySec(), ",IP："+ClientIP))
		                                	returnValue[1] = "101";
		                            	else 
		                            		returnValue[1] = "1";
	                        		} else {
	                            		returnValue[1] = "730";
	                            	}
	                        	} else if(pswd.equals(Util.getEncrypt(pass))){
	                        		rs.executeSql("update hrmpassword set password='' where id=" + idTemp);
	                                returnValue[1] = "1";
	                        	} else {
	                        		returnValue[1] = "57";
	                        	}
	                        } else {
	                        	returnValue[1] = "57";
	                        }
						} else {
							if(!passwordTemp.equals(Util.getEncrypt(pass))){
								returnValue[1] = "0";
							}
						}
                    }
                }
            }
        }
        return returnValue;
    }

    private void upPswdJob(final String arg0, final String arg1){
        final long sleeps = StringUtil.parseToLong(arg1, 120) * 1000;
        new Thread(new Runnable() {
            public void run() {
            	try {
					Thread.sleep(sleeps);
                	new RecordSet().executeSql("update hrmpassword set password='' where id=" + arg0);
				} catch (InterruptedException e) {}
            }
		}).start();
    }

	/**
	 * added by wcd 2014-12-23 禁止在网段外登录
	 * 
	 * @param request
	 * @return
	 */
	public boolean checkIpSegByForbidLogin(HttpServletRequest request, String loginId) {
		RecordSet rs = new RecordSet();
		rs.executeSql("select * from HrmResourceManager where loginid = '"+loginId+"'");
		if(rs.next()) return true;
		
		String ClientIP = request.getRemoteAddr();
		if(ClientIP.equals("0:0:0:0:0:0:0:1")) return true;
		HrmSettingsComInfo sci = new HrmSettingsComInfo();
		int forbidLogin = Util.getIntValue(sci.getForbidLogin(), 0);
		if(forbidLogin == 0) return true;

		boolean ipaddress = false;
		String inceptipaddress ="";
        String endipaddress = "";
        String sql = "select * from HrmnetworkSegStr";
        rs.executeSql(sql);
        if(rs.getCounts() == 0) return false;
        while(rs.next()){
            inceptipaddress =rs.getString("inceptipaddress");
            endipaddress = rs.getString("endipaddress");
            long ip1 = IpUtils.ip2number(inceptipaddress);
    		long ip2 = IpUtils.ip2number(endipaddress);
    		long ip3 = IpUtils.ip2number(ClientIP);
    		if(ip3>=ip1 && ip3<=ip2){
				ipaddress = true;
				break;
			}
        }
		return ipaddress;
	}
	
	/**
	 * 检查网段策略

	 * @param request
	 * @param loginid
	 * @return
	 */
	public boolean checkIpSeg(HttpServletRequest request, String loginid, int passwordstateip) {
		String ClientIP = request.getRemoteAddr();
		//added by wcd 2014-11-11
		if(ClientIP.equals("0:0:0:0:0:0:0:1"))ClientIP = "127.0.0.1";
		boolean ipaddress = true;
        
        HrmSettingsComInfo sci=new HrmSettingsComInfo();
        int needdynapass_sys = Util.getIntValue(sci.getNeeddynapass());
        if(needdynapass_sys==1) {
        	RecordSet rs = new RecordSet();
			/**
	         * 0,启用.
	         * 1，停止（默认）.
	         * 2，网段策略.
	         * */
	
	        String inceptipaddress ="";
	        String endipaddress = "";
	        String sql = "select * from HrmnetworkSegStr";
	        rs.executeSql(sql);
	        /*
	         * 把获得的Ip地址转化为16进制数，与设置的网段策略区间进行比较判断是否使用动态密码

	         * */
	        while(rs.next()){
                inceptipaddress =rs.getString("inceptipaddress");
                endipaddress = rs.getString("endipaddress");
                long ip1=IpUtils.ip2number(inceptipaddress);
        		long iIp2=IpUtils.ip2number(endipaddress);
        		
        		String ip3=ClientIP;
        		long iIp3=IpUtils.ip2number(ip3);
        		if(passwordstateip==2){
        			if(iIp3>=ip1&&iIp3<=iIp2){
        				ipaddress = false;
        				break;
        			}
        			else if(iIp3<=ip1||iIp3>=iIp2){
        				ipaddress = true;
        			}
        		}else if(passwordstateip==0){
        			ipaddress = true;
        		}
	        }
        }
        return ipaddress;
	}
    /*
     * 基于安全考虑把原来用存储过程HrmResource_SByLoginIDPass改为此方法来运算
     * returnValue[0]
     * 0：普通用户；1：系统管理员
     * returnValue[1]
     * 0: 用户存在但秘密不对；1：用户密码都正确
    */
    public String[] checkUserPassM(String loginid, String pass){
        String[] returnValue = new String[2];
        returnValue[0]="-1"; //0：普通用户；1：系统管理员
        returnValue[1]="-1"; //0: 用户存在但秘密不对；1：用户密码都正确
        if (loginid.indexOf(";")>-1||loginid.indexOf("--")>-1||loginid.indexOf(" ")>-1||loginid.indexOf("'")>-1) {
            writeLog("illegal sql statement input loginid:" + loginid);
            returnValue[0]="-2";
            return returnValue;
        }
        RecordSet rs = new RecordSet();
        String sql = "";
        String idTemp ="0";
        String passwordTemp ="";
        sql = "select count(id),id from HrmResource where loginid='"+loginid+"' group by id";
        rs.executeSql(sql);
        if(rs.next()&&(Util.getIntValue(rs.getString(1),0)>0)){
            idTemp = rs.getString(2);
            returnValue[0]="0";
            returnValue[1] = "0";
            sql = "select password from HrmResource where id= " + idTemp;
            rs.executeSql(sql);
            if(rs.next()){
                passwordTemp = Util.null2String(rs.getString(1));
                if(passwordTemp.equals(pass))
                        returnValue[1] = "1";
            }
        } 
        return returnValue;
    }
    
    public String getUserCheckByDactylogram(HttpServletRequest request, HttpServletResponse response, String login_id, String user_password, String login_type, String login_file,String validateCode,String messages,String languid,boolean ismutilangua) throws Exception {
    	StaticObj staticobj = null;
        staticobj = StaticObj.getInstance();
        String frommail=Util.null2String(request.getParameter("frommail"));//针对华虹要求邮件直接登陆，不考虑安全性（内部使用）

        RecordSet rs = new RecordSet();
        RecordSet rs1 = new RecordSet();
        RecordSet rs2 = new RecordSet();
        char separator = Util.getSeparator();
        String message = "";

        Calendar today = Calendar.getInstance();
        String currentdate = Util.add0(today.get(Calendar.YEAR), 4) + "-" + Util.add0(today.get(Calendar.MONTH) + 1, 2) + "-" + Util.add0(today.get(Calendar.DAY_OF_MONTH), 2);
        String currenttime = Util.add0(today.get(Calendar.HOUR_OF_DAY), 2) + ":" + Util.add0(today.get(Calendar.MINUTE), 2) + ":" + Util.add0(today.get(Calendar.SECOND), 2);

        Document doc;
        try {

            String isLicense = (String) staticobj.getObject("isLicense");
            //国生 2004-9-16 , TD1096 , License的日期限制不起作用

            
            LN ckLicense = new LN();
            try{
            	message="1";
            }catch (Exception e) {
            	return "19";
			}
            if (!message.equals("1")) {
                return "19";
            } else {
                staticobj.putObject("isLicense", "true");
            }
            
            //检测用户并发数start
			String concurrentFlag = Util.null2String(ckLicense.getConcurrentFlag());
			int hrmnumber = Util.getIntValue(ckLicense.getHrmnum());
			if("1".equals(concurrentFlag)){
				LicenseCheckLogin  lchl = new LicenseCheckLogin();
				if(lchl.getLicUserCheck(login_id,hrmnumber)){
					recordFefuseLogin(login_id); //拒绝登陆记录
					return "26";
				}
			}
            //检测用户并发数end

            message = "";

            String software = (String) staticobj.getObject("software");
            String portal = "n";
            String multilanguage = "n";
            if (software == null) {
                rs.executeSql("select * from license");
                if (rs.next()) {
                    software = rs.getString("software");
                    if (software.equals("")) software = "ALL";
                    staticobj.putObject("software", software);
                    portal = rs.getString("portal");
                    if (portal.equals("")) portal = "n";
                    staticobj.putObject("portal", portal);
                    multilanguage = rs.getString("multilanguage");
                    if (multilanguage.equals("")) multilanguage = "n";
                    staticobj.putObject("multilanguage", multilanguage);
                }
            }
            
            if(!checkIpSegByForbidLogin(request, login_id)){
            	return "88";
            }

            if (login_type.equals("1")) {
            	//edit by chujun 2009-03-05 TD10057
                ChgPasswdReminder reminder=new ChgPasswdReminder();
                RemindSettings settings=reminder.getRemindSettings();
                int needvalidate=settings.getNeedvalidate();//0: 否,1: 是

                String validateRand="";
                validateRand=Util.null2String((String)request.getSession(true).getAttribute("validateRand"));
                if(needvalidate==1){
                    if(!validateRand.toLowerCase().equals(validateCode.trim().toLowerCase()))
                        return "52";
                }

            rs.executeSql("select * from HrmResource where loginid='" + login_id + "'");
            if(rs.next()){
                
                String startdate = rs.getString("startdate");
                String enddate = rs.getString("enddate");
                
                int status = rs.getInt("status");
                if (status != 0 && status != 1 && status != 2 && status != 3) {
                    return "17";
                }
                User user = new User();

                user.setUid(rs.getInt("id"));
                user.setLoginid(login_id);
                user.setFirstname(rs.getString("firstname"));
                user.setLastname(rs.getString("lastname"));
                user.setAliasname(rs.getString("aliasname"));
                user.setTitle(rs.getString("title"));
                user.setTitlelocation(rs.getString("titlelocation"));
                user.setSex(rs.getString("sex"));
				//user.setPwd(user_password);
                String languageidweaver = rs.getString("systemlanguage");
                if(!languid.equalsIgnoreCase(languageidweaver) && ismutilangua){
                	rs2.execute("update hrmresource set  systemlanguage = "+languid+" where id ="+rs.getInt("id"));
                	languageidweaver = languid;
                }
                user.setLanguage(Util.getIntValue(languageidweaver, 0));

                user.setTelephone(rs.getString("telephone"));
                user.setMobile(rs.getString("mobile"));
                user.setMobilecall(rs.getString("mobilecall"));
                user.setEmail(rs.getString("email"));
                user.setCountryid(rs.getString("countryid"));
                user.setLocationid(rs.getString("locationid"));
                user.setResourcetype(rs.getString("resourcetype"));
                user.setStartdate(startdate);
                user.setEnddate(enddate);
                user.setContractdate(rs.getString("contractdate"));
                user.setJobtitle(rs.getString("jobtitle"));
                user.setJobgroup(rs.getString("jobgroup"));
                user.setJobactivity(rs.getString("jobactivity"));
                user.setJoblevel(rs.getString("joblevel"));
                user.setSeclevel(rs.getString("seclevel"));
                user.setUserDepartment(Util.getIntValue(rs.getString("departmentid"), 0));
                user.setUserSubCompany1(Util.getIntValue(rs.getString("subcompanyid1"), 0));
                user.setUserSubCompany2(Util.getIntValue(rs.getString("subcompanyid2"), 0));
                user.setUserSubCompany3(Util.getIntValue(rs.getString("subcompanyid3"), 0));
                user.setUserSubCompany4(Util.getIntValue(rs.getString("subcompanyid4"), 0));
                user.setManagerid(rs.getString("managerid"));
                user.setAssistantid(rs.getString("assistantid"));
                user.setPurchaselimit(rs.getString("purchaselimit"));
                user.setCurrencyid(rs.getString("currencyid"));
                user.setLastlogindate(currentdate);
                user.setLogintype("1");
                user.setAccount(rs.getString("account"));
                user.setIsAdmin(false);
                user.setLoginip(request.getRemoteAddr());
                // request.getSession(true).setMaxInactiveInterval(60 * 60 * 24);
                request.getSession(true).setAttribute("weaver_user@bean", user);
                request.getSession(true).setAttribute("rtxlogin", "0");
                //request.getSession(true).setAttribute("moniter", new OnLineMonitor("" + user.getUID(),user.getLoginip()));

                Util.setCookie(response, "loginfileweaver", login_file, 172800);
                Util.setCookie(response, "loginidweaver", login_id, 172800);
                
                Util.setCookie(response, "loginfileweaver", login_file, 172800);
                Util.setCookie(response, "loginidweaver", ""+user.getUID(), 172800);
                Util.setCookie(response, "languageidweaver", languageidweaver, 172800);
                
                //Util.setCookie(response, "dactylogram", rs.getString("dactylogram"), 172800);
                //Util.setCookie(response, "assistantdactylogram", rs.getString("assistantdactylogram"), 172800);

                char separater = Util.getSeparator();
                RecordSet rsLoginUpdate = new RecordSet();
                rsLoginUpdate.execute("HrmResource_UpdateLoginDate", ""+user.getUID() + separater + currentdate);

                SysMaintenanceLog log = new SysMaintenanceLog();
                log.resetParameter();
                log.setRelatedId(rs.getInt("id"));
                log.setRelatedName((rs.getString("firstname") + " " + rs.getString("lastname")).trim());
                log.setOperateType("6");
                log.setOperateDesc("");
                log.setOperateItem("60");
                log.setOperateUserid(rs.getInt("id"));
                log.setClientAddress(request.getRemoteAddr());
                log.setSysLogInfo();

                //检查是否有客户联系计划
/* 刘煜修改 2004－05－08 登录检查客户联系计划以前是选取所有的记录，并不需要，现在更改为选择总数 */
                char flag = 2;
                String sql = "";
                rs1.executeProc("SysRemindInfo_InserCrmcontact", "" + rs.getInt("id") + flag + "0" + flag + "0");
                sql = " select count(*) from CRM_ContactLog where isfinished = 0 and contactdate ='" + currentdate + "' and resourceid =" + rs.getInt("id");
                rs1.executeSql(sql);
                if (rs1.next()) {
                    if (Util.getIntValue(rs1.getString(1), 0) > 0)
                        rs1.executeProc("SysRemindInfo_InserCrmcontact", "" + rs.getInt("id") + flag + "0" + flag + "1");
                } else {
                  return "15";
                }
              }else{
              	rs.executeSql("select * from HrmResourceManager where loginid='" + login_id + "'");
              	rs.next();

                String startdate = rs.getString("startdate");
                String enddate = rs.getString("enddate");

                int status = rs.getInt("status");
                if (status != 0 && status != 1 && status != 2 && status != 3) {
                    return "17";
                }
                User user = new User();

                user.setUid(rs.getInt("id"));
                user.setLoginid(login_id);
                user.setFirstname(rs.getString("firstname"));
                user.setLastname(rs.getString("lastname"));
                user.setAliasname(rs.getString("aliasname"));
                user.setTitle(rs.getString("title"));
                user.setTitlelocation(rs.getString("titlelocation"));
                user.setSex(rs.getString("sex"));
				//user.setPwd(user_password);
                String languageidweaver = rs.getString("systemlanguage");
                if(!languid.equalsIgnoreCase(languageidweaver) && ismutilangua){
                	rs2.execute("update hrmresource set  systemlanguage = "+languid+" where id ="+rs.getInt("id"));
                	languageidweaver = languid;
                }
                user.setLanguage(Util.getIntValue(languageidweaver, 0));
                user.setIsAdmin(true);
                user.setTelephone(rs.getString("telephone"));
                user.setMobile(rs.getString("mobile"));
                user.setMobilecall(rs.getString("mobilecall"));
                user.setEmail(rs.getString("email"));
                user.setCountryid(rs.getString("countryid"));
                user.setLocationid(rs.getString("locationid"));
                user.setResourcetype(rs.getString("resourcetype"));
                user.setStartdate(startdate);
                user.setEnddate(enddate);
                user.setContractdate(rs.getString("contractdate"));
                user.setJobtitle(rs.getString("jobtitle"));
                user.setJobgroup(rs.getString("jobgroup"));
                user.setJobactivity(rs.getString("jobactivity"));
                user.setJoblevel(rs.getString("joblevel"));
                user.setSeclevel(rs.getString("seclevel"));
                user.setUserDepartment(Util.getIntValue(rs.getString("departmentid"), 0));
                user.setUserSubCompany1(Util.getIntValue(rs.getString("subcompanyid1"), 0));
                user.setUserSubCompany2(Util.getIntValue(rs.getString("subcompanyid2"), 0));
                user.setUserSubCompany3(Util.getIntValue(rs.getString("subcompanyid3"), 0));
                user.setUserSubCompany4(Util.getIntValue(rs.getString("subcompanyid4"), 0));
                user.setManagerid(rs.getString("managerid"));
                user.setAssistantid(rs.getString("assistantid"));
                user.setPurchaselimit(rs.getString("purchaselimit"));
                user.setCurrencyid(rs.getString("currencyid"));
                user.setLastlogindate(currentdate);
                user.setLogintype("1");
                user.setAccount(rs.getString("account"));

                user.setLoginip(request.getRemoteAddr());
                // request.getSession(true).setMaxInactiveInterval(60 * 60 * 24);
                request.getSession(true).setAttribute("weaver_user@bean", user);
                request.getSession(true).setAttribute("rtxlogin", "0");
                //request.getSession(true).setAttribute("moniter", new OnLineMonitor("" + user.getUID(),user.getLoginip()));

                Util.setCookie(response, "loginfileweaver", login_file, 172800);
                Util.setCookie(response, "loginidweaver", login_id, 172800);
                
                Util.setCookie(response, "loginfileweaver", login_file, 172800);
                Util.setCookie(response, "loginidweaver", ""+user.getUID(), 172800);
                Util.setCookie(response, "languageidweaver", languageidweaver, 172800);
                
                //Util.setCookie(response, "dactylogram", rs.getString("dactylogram"), 172800);
                //Util.setCookie(response, "assistantdactylogram", rs.getString("assistantdactylogram"), 172800);

                char separater = Util.getSeparator();
                RecordSet rsLoginUpdate = new RecordSet();
                rsLoginUpdate.execute("HrmResource_UpdateLoginDate", ""+user.getUID() + separater + currentdate);

                SysMaintenanceLog log = new SysMaintenanceLog();
                log.resetParameter();
                log.setRelatedId(rs.getInt("id"));
                log.setRelatedName((rs.getString("firstname") + " " + rs.getString("lastname")).trim());
                log.setOperateType("6");
                log.setOperateDesc("");
                log.setOperateItem("60");
                log.setOperateUserid(rs.getInt("id"));
                log.setClientAddress(request.getRemoteAddr());
                log.setSysLogInfo();

                //检查是否有客户联系计划
/* 刘煜修改 2004－05－08 登录检查客户联系计划以前是选取所有的记录，并不需要，现在更改为选择总数 */
                char flag = 2;
                String sql = "";
                rs1.executeProc("SysRemindInfo_InserCrmcontact", "" + rs.getInt("id") + flag + "0" + flag + "0");
                sql = " select count(*) from CRM_ContactLog where isfinished = 0 and contactdate ='" + currentdate + "' and resourceid =" + rs.getInt("id");
                rs1.executeSql(sql);
                if (rs1.next()) {
                    if (Util.getIntValue(rs1.getString(1), 0) > 0)
                        rs1.executeProc("SysRemindInfo_InserCrmcontact", "" + rs.getInt("id") + flag + "0" + flag + "1");
                } else {
                  return "15";
                }
              }
            }

        } catch (Exception e) {
            writeLog(e);
            throw e;
        }
        return message;
    }
    public static void checkLicenseInfo() {
		RecordSet rs = new RecordSet();
		StaticObj staticobj = StaticObj.getInstance();
		String software = (String) staticobj.getObject("software");
		String portal = "n";
		String multilanguage = "n";
		if (software == null) {
		    rs.executeSql("select * from license");
		    if (rs.next()) {
		        software = rs.getString("software");
		        if (software.equals("")) software = "ALL";
		        staticobj.putObject("software", software);
		        portal = rs.getString("portal");
		        if (portal.equals("")) portal = "n";
		        staticobj.putObject("portal", portal);
		        multilanguage = rs.getString("multilanguage");
		        if (multilanguage.equals("")) multilanguage = "n";
		        staticobj.putObject("multilanguage", multilanguage);
		    }
		}
	}

	/**
     * 拒绝登陆记录
     * @param loginid
     */
    public void recordFefuseLogin(String loginid){
    	
    	SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
    	Calendar calendar=Calendar.getInstance();
    	String currentdate=dateFormat.format(calendar.getTime());
    	int currentYear=calendar.get(Calendar.YEAR);
    	int currentMonth=calendar.get(Calendar.MONTH)+1;
    	int currentHour=calendar.get(Calendar.HOUR_OF_DAY);
    	
    	String sql="select id from HrmRefuseCount where refuse_date='"+currentdate+"' and refuse_hour="+currentHour+" and refuse_loginid='"+loginid+"'";
    	RecordSet rs=new RecordSet();
    	rs.execute(sql);
    	if(!rs.next()){
    		sql="insert into HrmRefuseCount(refuse_date,refuse_year,refuse_month,refuse_hour,refuse_loginid)" +
    			"values('"+currentdate+"',"+currentYear+","+currentMonth+","+currentHour+",'"+loginid+"')";
    		rs.execute(sql);
    	}	
    }
    
    public boolean ifEqlTarget(String val, String target) {
    	if(val == null || val.equals("")) {
    		return false;
    	}
    	if(!val.equals(target)) {
    		return false;
    	}
    	return true;
    }
    
    private boolean sendOk(String ln, String sDypadcon, int dynapasslen, String mobile, String time, String tmpid, String sValiditySec){
    	return sendOk(ln, sDypadcon, dynapasslen, mobile, time, tmpid, sValiditySec, "");
    }
    
    private boolean sendOk(String ln, String sDypadcon, int dynapasslen, String mobile, String time, String tmpid, String sValiditySec, String ip){
    	String dypadcon = Util.null2String(sDypadcon);
		String dynapass = "";
        if(dypadcon.equals("0")){
        	dynapass = Util.passwordBuilderNo(dynapasslen);
        }else if (dypadcon.equals("1")){
        	dynapass = Util.passwordBuilderEn(dynapasslen);
        }else if (dypadcon.equals("2")){
        	 dynapass = Util.passwordBuilder(dynapasslen);
        }
        SMSManager sm = new SMSManager();
        boolean sendflag = sm.sendSMS(mobile, "您在"+time+"登录系统的动态密码为：" + dynapass+ip);
        if (sendflag) {
            new RecordSet().executeSql("update hrmpassword set password='" + Util.getEncrypt(dynapass) + "' where id=" + tmpid);
            upPswdJob(tmpid, sValiditySec);
        }
        return sendflag;
    }

}
