package weaver.interfaces.ehr;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.io.PrintWriter;
import java.util.regex.Matcher; 
import java.util.regex.Pattern; 
import java.text.DateFormat;
import java.text.SimpleDateFormat; 

import javax.jws.WebService;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.record.formula.functions.Left;

import weaver.conn.RecordSet;
import weaver.hrm.User;
import weaver.hrm.company.DepartmentComInfo;
import weaver.hrm.job.JobTitlesComInfo;
import weaver.hrm.resource.ResourceComInfo;


import com.alibaba.fastjson.JSONObject;
import com.informix.util.stringUtil;
import com.sun.xml.internal.ws.message.StringHeader;
import com.weaver.formmodel.base.BaseBean;
import com.weaver.formmodel.mobile.manager.MobileUserInit;
import com.weaver.formmodel.util.StringHelper;
import com.weaver.general.Util;

@WebService(endpointInterface="weaver.shangzheng.interfaces.InterfaceService")
public class InterfaceServiceImpl implements InterfaceService{
	  
	//部门接口
	public String bmInterface(String json){
		
		JSONObject jsonn =JSONObject.parseObject(json);
		String depCode = jsonn.getString("depCode");
		String depName = jsonn.getString("depName");
		String types = jsonn.getString("type");
		RecordSet rs = new RecordSet();
		JSONObject jsonbm = new JSONObject();
		String sql="";
		boolean b = true;
		if(StringHelper.isEmpty(depCode)||StringHelper.isEmpty(depName)||StringHelper.isEmpty(StringHelper.null2String(jsonn.getString("type")))){
			jsonbm.put("statusCode", "101");
			jsonbm.put("message", "部门接口字段含空值");
		} else {
			try {
				if(!types.equals("0")&&!types.equals("1")){
					jsonbm.put("statusCode", "101");
					jsonbm.put("message", "接口操作类型无效");
					b=false;
				}else{
				   int type = jsonn.getIntValue("type");
				
				sql = "select * from hrmdepartment  where id = (select deptid from hrmdepartmentdefined  where hrcode = '"+depCode+"' )";
				//通过部门编码查询deptid，再查询部门信息
				rs.executeSql(sql);
				if(type == 0){//新增
					if(rs.getCounts()>0){
						jsonbm.put("statusCode", "102");
						jsonbm.put("message", "部门信息不唯一");
						b=false;
					}else {
						sql = " insert into hrmdepartment(departmentmark,departmentname,subcompanyid1,showorder) values ('"+depName+"','"+depName+"',5,999)";
						//新增部门
						System.out.println(sql);
					}
				} else if (type == 1){//修改
					if(rs.getCounts()>1){
						jsonbm.put("statusCode", "199");
						jsonbm.put("message", "系统部门接口异常");
						b=false;
					}else if(rs.getCounts()==0){
						jsonbm.put("statusCode", "103");
						jsonbm.put("message", "部门编码:"+depCode+" 部门信息不存在");
						b=false;
					}else {
						sql = " UPDATE hrmdepartment  SET departmentname = '"+depName+"' ,departmentmark = '"+depName+"'  WHERE id = (select deptid from hrmdepartmentdefined hrd where hrcode = '"+depCode+"' )";
						//根据部门编码，查询部门，并修改部门信息
					}
				} else {
					jsonbm.put("statusCode", "101");
					jsonbm.put("message", "部门接口字段含空值");
					b=false;
				 }
				}
				if(b){
					
					RecordSet recordSet = new RecordSet();
					boolean bnn = rs.executeSql(sql);
					if(bnn){
						String sql_hrmdepartmentdefined = "select deptid from hrmdepartmentdefined hrd where hrd.hrcode = '"+depCode+"' ";
						sql = "DELETE from  hrmdepartment  where departmentname = '"+depName+"'";//删除部门表语句
						recordSet.executeSql(sql_hrmdepartmentdefined);
						boolean bn=true;
						if(recordSet.getCounts()>1){
							jsonbm.put("statusCode", "199");
							jsonbm.put("message", "系统部门编码异常");
							rs.executeSql(sql);
							bn=false;
						}else if(recordSet.getCounts() == 1){
							sql_hrmdepartmentdefined = "UPDATE hrmdepartmentdefined SET deptid = (select id from hrmdepartment where departmentname = '"+depName+"') where hrcode = '"+depCode+"'";
						}else {
							sql_hrmdepartmentdefined = "insert into hrmdepartmentdefined(deptid,hrcode) VALUES ((select id from hrmdepartment where departmentname = '"+depName+"'),'"+depCode+"')";
						}
						if(bn){
							boolean bnnn = recordSet.executeSql(sql_hrmdepartmentdefined);
							if(bnnn){
								jsonbm.put("statusCode", "0");
								jsonbm.put("message", "成功");
							}else {
								jsonbm.put("statusCode", "199");
								jsonbm.put("message", "系统部门编码异常");
								rs.executeSql(sql);
								//删除
							}
						}
					}else {
						jsonbm.put("statusCode", "199");
						jsonbm.put("message", "系统部门接口异常");
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				jsonbm.put("statusCode", "199");
				jsonbm.put("message", "系统部门接口异常");
			}
		}
		try {
			DepartmentComInfo clear = new DepartmentComInfo();
			clear.removeCompanyCache();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return StringHelper.null2String(jsonbm);
	}
	
	//岗位接口
	public String gwInterface(String json){
		
		JSONObject jsonn =JSONObject.parseObject(json);
		String jobTitleCode = jsonn.getString("jobTitleCode");
		String jobTitleName = jsonn.getString("jobTitleName");
		String depCode = jsonn.getString("depCode");
		String types = jsonn.getString("type");
		RecordSet rs = new RecordSet();
		RecordSet rs1 = new RecordSet();
		JSONObject jsongw = new JSONObject();
		String sql="";
		boolean b =true;
		if(StringHelper.isEmpty(jobTitleCode)||StringHelper.isEmpty(jobTitleName)||StringHelper.isEmpty(depCode)||StringHelper.isEmpty(StringHelper.null2String(jsonn.getString("type")))){
			jsongw.put("statusCode", "201");
			jsongw.put("message", "岗位接口字段含空值");
		}else {
			try {
				if(!types.equals("0")&&!types.equals("1")){
					jsongw.put("statusCode", "201");
					jsongw.put("message", "接口操作类型无效");
					b=false;
				}else{
				int type = jsonn.getIntValue("type");
				sql = " select * from hrmjobtitles where jobtitlecode = '"+jobTitleCode+"'";//查看岗位中是否存在该岗位编码
				String sql1="select deptid from hrmdepartmentdefined where hrcode = '"+depCode+"'";
				rs.executeSql(sql);
				rs1.executeSql(sql1);
				if(type == 0){
					if(rs.getCounts()>0){
						jsongw.put("statusCode", "202");
						jsongw.put("message", "岗位信息不唯一");
						b=false;
					}else {
						if(rs1.getCounts()==0){
							jsongw.put("statusCode", "299");
							jsongw.put("message", "部门不存在");
							b=false;
						}else{						
						 sql = " insert into hrmjobtitles(jobtitlecode,jobtitlemark,jobtitlename,jobdepartmentid) values ('"+jobTitleCode+"','"+jobTitleName+"','"+jobTitleName+"',0)";
						}
					}
				}else if(type == 1){
					if(rs.getCounts()>1){
						jsongw.put("statusCode", "299");
						jsongw.put("message", "系统岗位接口异常");
						b=false;
					}else if(rs.getCounts()==0){
						jsongw.put("statusCode", "203");
						jsongw.put("message", "岗位编码:"+jobTitleCode+" 岗位信息不存在");
						b=false;
					}else {
						if(rs1.getCounts()==0){
							jsongw.put("statusCode", "299");
							jsongw.put("message", "部门不存在");	
							b=false;
						}else{
						sql = " UPDATE hrmjobtitles SET jobtitlename = '"+jobTitleName+"',jobtitlemark = '"+jobTitleName+"',jobdepartmentid = (select deptid from hrmdepartmentdefined where hrcode = '"+depCode+"') where jobtitlecode = '"+jobTitleCode+"'";
					  
						}
					}
				}else {
					jsongw.put("statusCode", "201");
					jsongw.put("message", "岗位接口字段含空值");
					b=false;
				 }
				}
				if(b){
				boolean bn = rs.executeSql(sql);
					if(bn){
						jsongw.put("statusCode", "0");
						jsongw.put("message", "成功");
					}else {
						jsongw.put("statusCode", "299");
						jsongw.put("message", "系统岗位接口异常");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				// TODO: handle exception
				jsongw.put("statusCode", "299");
				jsongw.put("message", "系统岗位接口异常");
			}
		}
		try {
			JobTitlesComInfo clear = new JobTitlesComInfo();
			clear.removeJobTitlesCache();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return StringHelper.null2String(jsongw);
	}
	
	//人员接口
	public String hrumeInterface(String json){
		
		JSONObject jsonn =JSONObject.parseObject(json);
		String workCode = jsonn.getString("workCode");
		String staffName = jsonn.getString("staffName");
		String gender = jsonn.getString("gender");
		String birthday = jsonn.getString("birthday");        
		String joindate = jsonn.getString("joindate");
		String mobile = jsonn.getString("mobile");
		String mobile2 = jsonn.getString("mobile2");
		String telephone = jsonn.getString("telephone");
		String email = jsonn.getString("email");
		String locationId = jsonn.getString("locationId");
		String depCode = jsonn.getString("depCode");
		String jobTitleCode = jsonn.getString("jobTitleCode");
		String superMgrCode = jsonn.getString("superMgrCode");
		String types = jsonn.getString("type");
		String workStatuss = jsonn.getString("workStatus");
		RecordSet rs = new RecordSet();
		RecordSet rs1 = new RecordSet();
		RecordSet rs2 = new RecordSet();
		JSONObject jsonhrume = new JSONObject();
		String sql="";
		boolean b = true;
		if(StringHelper.isEmpty(workCode)||StringHelper.isEmpty(staffName)||StringHelper.isEmpty(gender)||StringHelper.isEmpty(birthday)||
				StringHelper.isEmpty(joindate)||StringHelper.isEmpty(mobile)||
				StringHelper.isEmpty(email)||StringHelper.isEmpty(locationId)||StringHelper.isEmpty(depCode)||StringHelper.isEmpty(jobTitleCode)||
				StringHelper.isEmpty(superMgrCode)||StringHelper.isEmpty(StringHelper.null2String(jsonn.getString("type")))||StringHelper.isEmpty(StringHelper.null2String(jsonn.getString("workStatus")))){
			jsonhrume.put("statusCode", "301");
			jsonhrume.put("message", "员工接口字段含空值");
		}else {			
			if(!isValidDate(birthday)){
				jsonhrume.put("statusCode", "301");
				jsonhrume.put("message", "出生日期格式不正确");
			}else if(!isValidDate(joindate)){
				jsonhrume.put("statusCode", "301");
				jsonhrume.put("message", "入职日期格式不正确");
			}else if(!gender.equals("0")&&!gender.equals("1")){
				jsonhrume.put("statusCode", "301");
				jsonhrume.put("message", "性别无效");
			}else if(!workStatuss.equals("1")&&!workStatuss.equals("5")){
				jsonhrume.put("statusCode", "301");
				jsonhrume.put("message", "在职状态无效");
			}else if(!types.equals("0")&&!types.equals("1")){
				jsonhrume.put("statusCode", "301");
				jsonhrume.put("message", "接口操作类型无效");
			}else if(email.indexOf("@")== -1){
				jsonhrume.put("statusCode", "304");
				jsonhrume.put("message", "邮箱格式错误");
			}else{			
			
			try {				
				int type = jsonn.getIntValue("type");
				int workStatus = jsonn.getIntValue("workStatus");				
					sql = " select * from hrmresource where workcode = '"+workCode+"'";//查询是否存在该工号
					String sql1="select deptid from hrmdepartmentdefined where hrcode = '"+depCode+"'";
					String sql2="select id from hrmjobtitles where jobtitlecode = '"+jobTitleCode+"'";		
					
					rs.executeSql(sql);	
					rs1.executeSql(sql1);
					rs2.executeSql(sql2);
					if(rs1.getCounts()==0){
						jsonhrume.put("statusCode", "399");
						jsonhrume.put("message", "部门不存在");
					}else if(rs2.getCounts()==0){
						jsonhrume.put("statusCode", "399");
						jsonhrume.put("message", "岗位不存在");
					}else{
						String loginid = email.substring(0, email.indexOf("@"));//用户名/账号
						if(!StringHelper.isEmpty(loginid)){
							String password = "123456";//密码
							password= md5Password(password);
							password = password.toUpperCase();
							if(type == 0){
								if(rs.getCounts()>0){
									jsonhrume.put("statusCode", "302");
									jsonhrume.put("message", "员工工号不唯一，已存在");
									b=false;
								}else {
									sql = " insert into hrmresource(id,workcode,lastname,sex,birthday,mobile,mobilecall,telephone,email,locationid,departmentid,jobtitle,managerid,status,loginid,password,dsporder,seclevel,subcompanyid1,textfield5,systemlanguage) values " +
											"(((select max(id) from hrmresource)+1),'"+workCode+"','"+staffName+"',(select id from hrmsex where sexname = '"+gender+"' ),'"+birthday+"','"+mobile+"','"+mobile2+"','"+telephone+"','"+email+"'," +
											"(select id from hrmlocations where locationname = '"+locationId+"'),(select deptid from hrmdepartmentdefined where hrcode = '"+depCode+"')," +
											"(select id from hrmjobtitles where jobtitlecode = '"+jobTitleCode+"'),(select id from hrmresource where workcode ='"+superMgrCode+"' ),"+workStatus+",'"+loginid+"','"+password+"',999,20,5,'szs上证所_提前审批测试',7)";
								}
							}else if(type == 1){
								if(rs.getCounts()>1){
									jsonhrume.put("statusCode", "399");
									jsonhrume.put("message", "系统人员接口异常");
									b=false;
								}else if(rs.getCounts() == 0){
									jsonhrume.put("statusCode", "303");
									jsonhrume.put("message", "员工工号:"+workCode+"员工工号不存在");
									b=false;
								}else {
									sql = "UPDATE hrmresource set lastname ='"+staffName+"',sex = (select id from hrmsex where sexname = '"+gender+"' ),birthday='"+birthday+"',mobile = '"+mobile+"'," +
											"mobilecall='"+mobile2+"',telephone='"+telephone+"',email ='"+email+"',locationid=(select id from hrmlocations where locationname = '"+locationId+"')," +
											"departmentid=(select deptid from hrmdepartmentdefined where hrcode = '"+depCode+"'),jobtitle=(select id from hrmjobtitles where jobtitlecode = '"+jobTitleCode+"')," +
											"managerid=(select id from hrmresource where workcode ='"+superMgrCode+"' ),status="+workStatus+",loginid='"+loginid+"'  where workcode ='"+workCode+"'";
								}
							}else {
								jsonhrume.put("statusCode", "399");
								jsonhrume.put("message", "系统人员接口异常");
								b=false;
							}
							if(b){
								System.out.println(sql);
								boolean bnn=rs.executeSql(sql);
								RecordSet recordSet = new RecordSet();
								if(bnn){
									String sql_cus_fielddata = "select * from cus_fielddata where id = (select id from hrmresource where workcode = '"+workCode+"') and scopeid = 1";
									recordSet.executeSql(sql_cus_fielddata);
									boolean bn=true;
									if(recordSet.getCounts()>1){
										jsonhrume.put("statusCode", "305");
										jsonhrume.put("message", "人员入职日期异常");
										bn=false;
									}else if(recordSet.getCounts() == 1){
										sql_cus_fielddata = "UPDATE cus_fielddata set  field2 = '"+joindate+"' where id = (select id from hrmresource where workcode = '"+workCode+"') and scopeid = 1 ";
									}else {
										sql_cus_fielddata = "insert into cus_fielddata(scope,scopeid,id,field2) values ('HrmCustomFieldByInfoType',1,(select id from hrmresource where workcode = '"+workCode+"'),'"+joindate+"')";
									}
									if(bn){
										System.out.println(sql_cus_fielddata);
										boolean bnnn = recordSet.executeSql(sql_cus_fielddata);
										if(bnnn){
											jsonhrume.put("statusCode", "0");
											jsonhrume.put("message", "成功");
										}else {
											jsonhrume.put("statusCode", "399");
											jsonhrume.put("message", "系统人员接口异常");
										}
									}
								}else {
									jsonhrume.put("statusCode", "399");
									jsonhrume.put("message", "系统人员接口异常");
								}
							}
						}else {
							jsonhrume.put("statusCode", "304");
							jsonhrume.put("message", "邮箱格式错误");
						}
					}
							  
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				jsonhrume.put("statusCode", "399");
				jsonhrume.put("message", "系统人员接口异常");
			}
		  }
		}
		try {
			ResourceComInfo clear = new ResourceComInfo();
			clear.removeResourceCache();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return StringHelper.null2String(jsonhrume);
	}
	
	public static String md5Password(String password)
	  {
	    try
	    {
	      MessageDigest digest = MessageDigest.getInstance("md5");
	      byte[] result = digest.digest(password.getBytes());
	      StringBuffer buffer = new StringBuffer();

	      for (byte b : result)
	      {
	        int number = b & 0xFF;
	        String str = Integer.toHexString(number);
	        if (str.length() == 1) {
	          buffer.append("0");
	        }
	        buffer.append(str);
	      }

	      return buffer.toString();
	    } catch (Exception e) {
	      e.printStackTrace();
	    }return "";
	  }
	
    /** 
     * 判断日期格式:yyyy-mm-dd 
     *  
     * @param sDate 
     * @return 
     */  
    public static boolean isValidDate(String sDate) {  
        String datePattern1 = "\\d{4}-\\d{2}-\\d{2}";  
        String datePattern2 = "^((\\d{2}(([02468][048])|([13579][26]))"  
                + "[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|"  
                + "(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?"  
                + "((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?("  
                + "(((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?"  
                + "((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))";  
        if ((sDate != null)) {  
            Pattern pattern = Pattern.compile(datePattern1);  
            Matcher match = pattern.matcher(sDate);  
            if (match.matches()) {  
                pattern = Pattern.compile(datePattern2);  
                match = pattern.matcher(sDate);  
                return match.matches();  
            } else {  
                return false;  
            }  
        }  
        return false;  
    } 
	
}
