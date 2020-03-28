package weaver.sysinterface.PanDian;

import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import weaver.conn.RecordSet;
import weaver.file.FileUpload;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.SplitPageUtil;
import weaver.general.Util;
import weaver.hrm.User;
import weaver.hrm.company.DepartmentComInfo;
import weaver.hrm.company.SubCompanyComInfo;
import weaver.hrm.job.JobTitlesComInfo;

import com.alibaba.fastjson.JSON;
import com.weaver.formmodel.mobile.MobileFileUpload;
import com.weaver.formmodel.mobile.manager.MobileUserInit;
import com.weaver.formmodel.mobile.utils.MobileCommonUtil;
import com.weaver.formmodel.util.DateHelper;
import com.weaver.formmodel.util.StringHelper;

public class PcPanDianAction extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private HttpServletRequest request;
	private HttpServletResponse response;
	
	public void doPost(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException{
		doGet(request, response);
	}
	
	public void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException{
		this.request = request;
		this.response = response;
		try {
			String action = StringHelper.null2String(request.getParameter("action"));
			User user = MobileUserInit.getUser(request, response);
			if(user == null){
				JSONObject checkUser = new JSONObject();
				checkUser.put("msgstatus", "0");
				checkUser.put("msg", "服务器端重置了登录信息，请重新登录");
				response.setContentType("application/json; charset=utf-8");  
				response.getWriter().print(checkUser.toString());
				return;
			}else if("pcPanDian".equals(action)){
				pcPanDian(user);
			}else if("pcPanDianPL".equals(action)){
				pcPanDianPL(user);
			}else if("userid".equals(action)){
				getUser(user);
			}else if("zcbmSS".equals(action)){
				zcbmSS(user);
			}else if("zcbmXS".equals(action)){
				zcbmXS(user);
			}else if("BMGL".equals(action)){
				getManage();
			}else if("AppPanDian".equals(action)){
				AppPanDian(user);
			}else if("WXZCpcPanDian".equals(action)){
				WXZCpcPanDian(user);
			}else if("WXZCpcPanDianPL".equals(action)){
				WXZCpcPanDianPL(user);
			}else if("IsUserid".equals(action)){
				IsUserid(user);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	/**
	 * 资产管理-PC盘点接口
	 * @param 
	 * user	用户
	 * pageno	当前页数
	 * pagesize	每页显示数
	 */
	private void pcPanDian(User user){
		try {
			RecordSet rs = new RecordSet();
			RecordSet rs1 = new RecordSet();
			RecordSet rs2 = new RecordSet();
			RecordSet rs3 = new RecordSet();
			RecordSet rs4 = new RecordSet();
			RecordSet rs5 = new RecordSet();
			String userid = String.valueOf(user.getUID());	//用户id
			int modeId1=91; //盘点模块id
			int modeId2=94; //资产变更模块id
			ModeRightInfo ModeRightInfo = new ModeRightInfo();
			String myArray = StringHelper.null2String(request.getParameter("myArray"));		//获取选中的资产id
			String [] id = null;
			String selectSql = "";
			String insertSql = "";
			String insertSql2 = "";
			id = myArray.split(",");
			String  wyid = "";
			String  wyid2 = "";
			for(int i =0;i<id.length;i++){
				selectSql = " select * from uf_am_fixedassets where id = " + id[i]+" and (usestatus =1 or usestatus = 2)";
				rs.executeSql(selectSql);
				UUID uuid = UUID.randomUUID();
				UUID uuid2 = UUID.randomUUID();
				wyid =uuid.toString();//生成唯一的标识码
				wyid2 =uuid2.toString();//生成唯一的标识码
				int Year = (Integer.parseInt(DateHelper.getCurrentYear()))-1;
				String title = Year + "年资产盘点";
				if(rs.next()){
					insertSql = "insert into  uf_am_checklog(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime," +
							"assetno,assetname,zcname,useperson,dept,storecity,newuseperson,newdept,newstorecity,remark,checkdate,checkperson,uuid) " +
							"VALUES("+modeId1+",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108)," +
							"'"+rs.getString("assetno")+"','"+rs.getString("id")+"','"+rs.getString("assetname")+"','"+rs.getString("userperson")+"','"+rs.getString("managedept")+"'," +
							"'"+rs.getString("storecity")+"','"+rs.getString("userperson")+"','"+rs.getString("managedept")+"','"+rs.getString("storecity")+"','批量盘点','"+DateHelper.getCurrentDate()+"','"+userid+"','"+wyid+"')";
				}
				boolean b = rs1.executeSql(insertSql);
				if(b){
					String selectsql2 = " select id from uf_am_checklog where uuid = '" + wyid+"'";
					rs2.executeSql(selectsql2);
					if(rs2.next()){
						insertSql2 = "insert into uf_am_assetslog(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
								+"assetname,title,flowid,creator,createdate,checklog,uuid) " +
								"values ("+modeId2+",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"
								+"'"+rs.getString("id")+"','"+title+"','','"+userid+"',convert(varchar(10),getdate(),120),'"+rs2.getString("id")+"','"+wyid2+"')";
						boolean b1 = rs3.executeSql(insertSql2);
						if(b1){
							selectsql2 = "select id from uf_am_assetslog where uuid='"+wyid2+"'";
							rs4.executeSql(selectsql2);
							if(rs4.next()){
								ModeRightInfo.editModeDataShare(5,modeId1,Integer.parseInt(rs2.getString("id")));//新建的时候添加共享-所有人
								ModeRightInfo.editModeDataShare(5,modeId2,Integer.parseInt(rs4.getString("id")));//新建的时候添加共享-所有人								
							}							
						}						
					}
				}
			}
			response.setContentType("application/text; charset=utf-8");  
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 资产管理-APP盘点接口
	 * @param 
	 * user	用户
	 * pageno	当前页数
	 * pagesize	每页显示数
	 */
	private void AppPanDian(User user){
		try {
			RecordSet rs = new RecordSet();
			RecordSet rs1 = new RecordSet();
			RecordSet rs2 = new RecordSet();
			RecordSet rs3 = new RecordSet();
			RecordSet rs4 = new RecordSet();
			ModeRightInfo ModeRightInfo = new ModeRightInfo();
			String userid = String.valueOf(user.getUID());	//用户id
			int modeId1=91; //盘点模块id
			int modeId2=94; //资产变更模块id
			FileUpload fileUpload = new MobileFileUpload(request,"UTF-8",false);
			String assetname = Util.null2String(fileUpload.getParameter("assetname")); // 资产名称
			String assetno = Util.null2String(fileUpload.getParameter("assetno")); // 资产编号
			String modeid = Util.null2String(fileUpload.getParameter("modeid")); // 资产模块
			String assettype = Util.null2String(fileUpload.getParameter("assettype")); // 资产类型
			String storagedate = Util.null2String(fileUpload.getParameter("storagedate")); // 入库日期
			String assetoriginalcost = Util.null2String(fileUpload.getParameter("assetoriginalcost")); // 资产原值
			String manage = Util.null2String(fileUpload.getParameter("manage")); // 管理部门id
			String old_managedept = Util.null2String(fileUpload.getParameter("old_managedept")); // old_管理部门id
			String check = Util.null2String(fileUpload.getParameter("check")); // 使用人门id
			String old_userperson = Util.null2String(fileUpload.getParameter("old_userperson")); // old_使用人门id
			String storecity = Util.null2String(fileUpload.getParameter("storecity")); // 存放地点
			String old_storecity = Util.null2String(fileUpload.getParameter("old_storecity")); // old_存放地点
			String remark = Util.null2String(fileUpload.getParameter("remark")); // 备注
			String filds = Util.null2String(fileUpload.getParameter("filds"));
			String id = Util.null2String(fileUpload.getParameter("id"));  //资产id
			
			JSONObject jsonObject=JSONObject.fromObject(filds);
			Iterator iterator = jsonObject.keys();
			String docIdContent = "";	//附件id集
			while(iterator.hasNext()){
				String key = (String) iterator.next();
				String value = jsonObject.getString(key);
				int docId = MobileCommonUtil.uploadFile3(value, fileUpload, user);
				if(docId != -1){
					if(StringHelper.isEmpty(docIdContent)){
						docIdContent = ""+docId;
					}else{
						docIdContent += "," + docId;
					}
				}
			}
			
			if(!"".equals(docIdContent)){
				String sql = "select t1.imagefileid,t2.imagefilename,t1.filesize from ImageFile t1,DocImageFile t2 "
						+"where t1.imagefileid = t2.imagefileid and t2.docid in ("+docIdContent+")";
				rs.executeSql(sql);
				while(rs.next()){
					String imagefileid = Util.null2String(rs.getString("imagefileid"));
					String imagefilename = Util.null2String(rs.getString("imagefilename"));
					String filesize = Util.null2String(rs.getString("filesize"));
					
					sql = "insert into docpicupload(picname,pictype,imagefilename,imagefileid,imagefilewidth,imagefileheight,imagefilesize,imagefilescale) "
							+"values('"+imagefilename+"',0,'"+imagefilename+"','"+imagefileid+"',0,0,'"+filesize+"',1)";
					rs1.executeSql(sql);
				}
			}
			
			UUID uuid = UUID.randomUUID();
			UUID uuid2 = UUID.randomUUID();
			String wyid =uuid.toString();//生成唯一的标识码
			String wyid2 =uuid2.toString();//生成唯一的标识码
			String insertSql="";
			String insertSql2="";
			int Year = (Integer.parseInt(DateHelper.getCurrentYear()))-1;
			String title = Year + "年资产盘点";
			if("89".equals(modeid)){
				insertSql = "insert into  uf_am_checklog(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime," +
						"assetno,assetname,zcname,useperson,dept,storecity,newuseperson,newdept,newstorecity,remark,checkdate,checkperson,uuid,checkfile) " +
						"VALUES("+modeId1+",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108)," +
						"'"+assetno+"','"+id+"','"+assetname+"','"+old_userperson+"','"+old_managedept+"'," +
						"'"+old_storecity+"','"+check+"','"+manage+"','"+storecity+"','"+remark+"','"+DateHelper.getCurrentDate()+"','"+userid+"','"+wyid+"','"+docIdContent+"')";
				
			}else if("90".equals(modeid)){
				insertSql = "insert into  uf_am_checklog(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime," +
						"assetno,wxassetname,zcname,useperson,dept,storecity,newuseperson,newdept,newstorecity,remark,checkdate,checkperson,uuid,checkfile) " +
						"VALUES("+modeId1+",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108)," +
						"'"+assetno+"','"+id+"','"+assetname+"','"+old_userperson+"','"+old_managedept+"'," +
						"'"+old_storecity+"','"+check+"','"+manage+"','"+storecity+"','"+remark+"','"+DateHelper.getCurrentDate()+"','"+userid+"','"+wyid+"','"+docIdContent+"')";
				
			}
			boolean b = rs.executeSql(insertSql);
			if(b){
				String selectsql2 = " select id from uf_am_checklog where uuid = '" + wyid+"'";
				rs1.executeSql(selectsql2);
				if(rs1.next()){
					if("89".equals(modeid)){
						insertSql2 = "insert into uf_am_assetslog(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
								+"assetname,title,flowid,creator,createdate,checklog,uuid) " +
								"values ("+modeId2+",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"
								+"'"+id+"','"+title+"','','"+userid+"',convert(varchar(10),getdate(),120),'"+rs1.getString("id")+"','"+wyid2+"')";
					}else if("90".equals(modeid)){
						insertSql2 = "insert into uf_am_assetslog(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
								+"wxassetname,title,flowid,creator,createdate,checklog,uuid) " +
								"values ("+modeId2+",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"
								+"'"+id+"','"+title+"','','"+userid+"',convert(varchar(10),getdate(),120),'"+rs1.getString("id")+"','"+wyid2+"')";
					}
					boolean b1 = rs2.executeSql(insertSql2);
					if(b1){
						selectsql2 = "select id from uf_am_assetslog where uuid='"+wyid2+"'";
						rs3.executeSql(selectsql2);
						if(rs3.next()){
							ModeRightInfo.editModeDataShare(5,modeId1,Integer.parseInt(rs1.getString("id")));//新建的时候添加共享-所有人
							ModeRightInfo.editModeDataShare(5,modeId2,Integer.parseInt(rs3.getString("id")));//新建的时候添加共享-所有人							
						}
						
					}
					String sql="";
					if("89".equals(modeid)){
						sql = "UPDATE uf_am_fixedassets SET storecity = '"+storecity+"',managedept = '"+manage+"',userperson = '"+check+"' WHERE id = '"+id+"'";
					}else if("90".equals(modeid)){
						sql = "UPDATE uf_am_immatassets SET managedept = '"+manage+"',manager = '"+check+"' WHERE id = '"+id+"'";
					}
					rs4.executeSql(sql);	
				}
			}
			
			response.setContentType("application/text; charset=utf-8");  
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	
	
	
	/**
	 * 资产管理-PC盘点列表查询接口
	 * @param 
	 * user	用户
	 * pageno	当前页数
	 * pagesize	每页显示数
	 */
	private void pcPanDianPL(User user){
		try {
			RecordSet rs = new RecordSet();
			RecordSet rs1 = new RecordSet();
			RecordSet rs2 = new RecordSet();
			RecordSet rs3 = new RecordSet();
			RecordSet rs4 = new RecordSet();
			RecordSet rs5 = new RecordSet();
			ModeRightInfo ModeRightInfo = new ModeRightInfo();
			String userid = String.valueOf(user.getUID());	//用户id
			int modeId1=91; //盘点模块id
			int modeId2=94; //资产变更模块id
			String zcbh = StringHelper.null2String(request.getParameter("zcbh"));		//获取选中的资产id
			String zcmc = StringHelper.null2String(request.getParameter("zcmc"));		//获取选中的资产id
			String zclx1 = StringHelper.null2String(request.getParameter("zclx1"));		//获取选中的资产id
			String zclx2 = StringHelper.null2String(request.getParameter("zclx2"));		//获取选中的资产id
			String zclx3 = StringHelper.null2String(request.getParameter("zclx3"));		//获取选中的资产id
			String sbszwz = StringHelper.null2String(request.getParameter("sbszwz"));		//获取选中的资产id
			String cfdd = StringHelper.null2String(request.getParameter("cfdd"));		//获取选中的资产id
			String glbm = StringHelper.null2String(request.getParameter("glbm"));		//获取选中的资产id
			String syr = StringHelper.null2String(request.getParameter("syr"));		//获取选中的资产id
			
			 String newselectSql = " select uaf.* from  uf_am_fixedassets uaf where (usestatus =1 or usestatus = 2) ";
			  if(StringHelper.isNotEmpty(zcbh)){
				  newselectSql += " and uaf.assetno like '%"+zcbh+"%'";
			  }
			  if(StringHelper.isNotEmpty(zcmc)){
				  newselectSql += " and uaf.assetname like '%"+zcmc+"%'";
			  }
			  if(StringHelper.isNotEmpty(zclx1)){
				  newselectSql += " and uaf.assettype1 = '"+zclx1+"'";
			  }
			  if(StringHelper.isNotEmpty(zclx2)){
				  newselectSql += " and uaf.assettype2 = '"+zclx2+"'";
			  }
			  if(StringHelper.isNotEmpty(zclx3)){
				  newselectSql += " and uaf.assettype3 = '"+zclx3+"'";
			  }
			  if(StringHelper.isNotEmpty(sbszwz)){
				  newselectSql += " and uaf.address = '"+sbszwz+"'";
			  }
			  if(StringHelper.isNotEmpty(cfdd)){
				  newselectSql += " and uaf.storecity like '%"+cfdd+"%'";
			  }
			  if(StringHelper.isNotEmpty(glbm)){
				  newselectSql += " and uaf.managedept = '"+glbm+"'";
			  }
			  if(StringHelper.isNotEmpty(syr)){
				  newselectSql += " and uaf.userperson = '"+syr+"'";
			  }
			
			String insertSql = "";
			String insertSql2 = "";
			int Year = (Integer.parseInt(DateHelper.getCurrentYear()))-1;
			String title = Year + "年资产盘点";
			rs.executeSql(newselectSql);
			while(rs.next()){
				UUID uuid = UUID.randomUUID();
				UUID uuid2 = UUID.randomUUID();
				String  wyid =uuid.toString();//生成唯一的标识码
				String  wyid2 =uuid2.toString();//生成唯一的标识码
				insertSql = "insert into  uf_am_checklog(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime," +
						"assetno,assetname,zcname,useperson,dept,storecity,remark,checkdate,checkperson,uuid) " +
						"VALUES("+modeId1+",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108)," +
						"'"+rs.getString("assetno")+"','"+rs.getString("id")+"','"+rs.getString("assetname")+"','"+rs.getString("userperson")+"','"+rs.getString("managedept")+"'," +
						"'"+rs.getString("storecity")+"','批量盘点','"+DateHelper.getCurrentDate()+"','"+userid+"','"+wyid+"')";
				boolean b = rs1.executeSql(insertSql);
				if(b){
					String selectsql2 = " select id from uf_am_checklog where uuid = '" + wyid+"'";
					rs2.executeSql(selectsql2);
					if(rs2.next()){
						insertSql2 = "insert into uf_am_assetslog(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
								+"assetname,title,flowid,creator,createdate,checklog,uuid) " +
								"values ("+modeId2+",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"
								+"'"+rs.getString("id")+"','"+title+"','','"+userid+"',convert(varchar(10),getdate(),120),'"+rs2.getString("id")+"','"+wyid2+"')";
						boolean b1 = rs3.executeSql(insertSql2);
						if(b1){
							selectsql2 = "select id from uf_am_assetslog where uuid='"+wyid2+"'";
							rs4.executeSql(selectsql2);
							if(rs4.next()){
								ModeRightInfo.editModeDataShare(5,modeId1,Integer.parseInt(rs2.getString("id")));//新建的时候添加共享-所有人
								ModeRightInfo.editModeDataShare(5,modeId2,Integer.parseInt(rs4.getString("id")));//新建的时候添加共享-所有人							
								
							}
							
						}
					}
				}
			}
			response.setContentType("application/text; charset=utf-8");  
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	/**
	 * 资产管理-角色判断接口
	 * @param 
	 * user	用户
	 * pageno	当前页数
	 * pagesize	每页显示数
	 */
	public void getUser(User user){
		String userid = String.valueOf(user.getUID());	//用户id
		RecordSet rs = new RecordSet();
		String sqlUser = "  select hrs.id from hrmrolemembers hrme left join hrmroles hrs on hrme.roleid = hrs.id " +
				" where (hrs.id = 148 or hrs.id = 149 or hrs.id = 150 or hrs.id = 155 or hrs.id = 2) and hrme.resourceid = '"+userid+"'";
		rs.executeSql(sqlUser);
		try {
			response.setContentType("application/text; charset=utf-8");  
			if(rs.next()){
				response.getWriter().print(StringHelper.null2String(rs.getString("id")));
			}else {
				response.getWriter().print("null");
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	/**
	 * 资产管理-资产查询接口
	 * @param 
	 * user	用户
	 * pageno	当前页数
	 * pagesize	每页显示数
	 */
	public void zcbmSS(User user){
		String souSuo = StringHelper.null2String(request.getParameter("souSuo"));		//获取指定的资产编码或资产名称
		RecordSet rs = new RecordSet();
		RecordSet rs2 = new RecordSet();
		String sqlUser = " select * from uf_am_fixedassets where(assetno like '%"+souSuo+"%' or assetname like '%"+souSuo+"%') and (usestatus =1 or usestatus = 2)";
		String sqlUser2 = " select * from uf_am_immatassets where(assetno like '%"+souSuo+"%' or assetname like '%"+souSuo+"%') and usestatus!=3";
		rs.executeSql(sqlUser);
		rs2.executeSql(sqlUser2);
		JSONArray jsonArray = new JSONArray();
		while(rs.next()){
			JSONObject json = new JSONObject();
			json.put("id", StringHelper.null2String(rs.getString("id")));
			json.put("modeid", "89");
			json.put("assetname", StringHelper.null2String(rs.getString("assetname")));
			json.put("assetno", StringHelper.null2String(rs.getString("assetno")));
			jsonArray.add(json);
		}
		while(rs2.next()){
			JSONObject json = new JSONObject();
			json.put("id", StringHelper.null2String(rs2.getString("id")));
			json.put("modeid", "90");
			json.put("assetname", StringHelper.null2String(rs2.getString("assetname")));
			json.put("assetno", StringHelper.null2String(rs2.getString("assetno")));
			jsonArray.add(json);
		}
		JSONObject result = new JSONObject();
		result.put("totalSize", jsonArray.size());
		result.put("datas", jsonArray.toString());
		try {
			response.setContentType("application/json; charset=utf-8");  
			response.getWriter().print(result.toString());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	/**
	 * 资产管理-资产显示接口
	 * @param 
	 * user	用户
	 * pageno	当前页数
	 * pagesize	每页显示数
	 */
	public void zcbmXS(User user){
		String id = StringHelper.null2String(request.getParameter("id"));		
		String modeid=StringHelper.null2String(request.getParameter("modeid"));
		if("89".equals(modeid)){
			RecordSet rs = new RecordSet();
			String selectSql = "SELECT uaf.id,uaf.assetname,uaf.assetno,"+
					"( SELECT name FROM uf_am_assettype uaa WHERE uaa.id = uaf.assettype1 ) AS assettype1,"+
					"( SELECT name FROM uf_am_assettype uaa WHERE uaa.id = uaf.assettype2 ) AS assettype2,"+
					"( SELECT name FROM uf_am_assettype uaa WHERE uaa.id = uaf.assettype3 ) AS assettype3,"+
					"( SELECT lastname FROM HrmResource n WHERE n.id= uaf.userperson ) AS creatorname,"+
					"( select departmentname from hrmdepartment hr where hr.id = uaf.managedept) AS departmentname,"+
					" uaf.storagedate,uaf.assetoriginalcost ,uaf.remark,uaf.storecity,uaf.userperson,uaf.managedept " +
					" FROM uf_am_fixedassets uaf where id = " + id;
			rs.executeSql(selectSql);
			JSONArray jsonArray = new JSONArray();
			if(rs.next()){
				String assettype = "";
				String assettype1 = Util.null2String(rs.getString("assettype1"));
				String assettype2 = Util.null2String(rs.getString("assettype2"));
				String assettype3 = Util.null2String(rs.getString("assettype3"));
				
				if(StringHelper.isNotEmpty(assettype1)){
					assettype += assettype1;
					if(StringHelper.isNotEmpty(assettype2)){
						assettype += "-"+assettype2;
					}
					if(StringHelper.isNotEmpty(assettype3)){
						assettype += "-"+assettype3;
					}
				}else {
					if(StringHelper.isNotEmpty(assettype2)){
						assettype += assettype2;
						if(StringHelper.isNotEmpty(assettype3)){
							assettype += "-"+assettype3;
						}
					}else{
						if(StringHelper.isNotEmpty(assettype3)){
							assettype += assettype3;
						}
					}
				}
				JSONObject json = new JSONObject();
				json.put("id", StringHelper.null2String(rs.getString("id")));
				json.put("assetname", StringHelper.null2String(rs.getString("assetname")));
				json.put("assetno", StringHelper.null2String(rs.getString("assetno")));
				json.put("modeid", modeid);
				json.put("assettype", assettype);
				json.put("storagedate", StringHelper.null2String(rs.getString("storagedate")));
				json.put("assetoriginalcost", StringHelper.null2String(rs.getString("assetoriginalcost")));
				json.put("creatorname", StringHelper.null2String(rs.getString("creatorname")));
				json.put("userperson", StringHelper.null2String(rs.getString("userperson")));
				json.put("departmentname", StringHelper.null2String(rs.getString("departmentname")));
				json.put("managedept", StringHelper.null2String(rs.getString("managedept")));
				json.put("storecity", StringHelper.null2String(rs.getString("storecity")));
				json.put("remark", StringHelper.null2String(rs.getString("remark")));
				jsonArray.add(json);
			}
			JSONObject result = new JSONObject();
			result.put("totalSize", jsonArray.size());
			result.put("datas", jsonArray.toString());
			try {
				response.setContentType("application/json; charset=utf-8");  
				response.getWriter().print(result.toString());
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}else if("90".equals(modeid)){
			RecordSet rs = new RecordSet();
			String selectSql = "SELECT uaf.id,uaf.assetname,uaf.assetno,"+
					"( SELECT lastname FROM HrmResource n WHERE n.id= uaf.manager ) AS creatorname,"+
					"( select departmentname from hrmdepartment hr where hr.id = uaf.managedept) AS departmentname,"+
					" uaf.storagedate,uaf.amount ,uaf.remark,uaf.manager,uaf.managedept " +
					" FROM uf_am_immatassets uaf where id = " + id;
			rs.executeSql(selectSql);
			JSONArray jsonArray = new JSONArray();
			if(rs.next()){
				JSONObject json = new JSONObject();
				json.put("id", StringHelper.null2String(rs.getString("id")));
				json.put("assetname", StringHelper.null2String(rs.getString("assetname")));
				json.put("assetno", StringHelper.null2String(rs.getString("assetno")));
				json.put("modeid", modeid);
				json.put("assettype", "无形资产");
				json.put("storagedate", StringHelper.null2String(rs.getString("storagedate")));
				json.put("assetoriginalcost", StringHelper.null2String(rs.getString("amount")));
				json.put("creatorname", StringHelper.null2String(rs.getString("creatorname")));
				json.put("userperson", StringHelper.null2String(rs.getString("manager")));
				json.put("departmentname", StringHelper.null2String(rs.getString("departmentname")));
				json.put("managedept", StringHelper.null2String(rs.getString("managedept")));
				json.put("storecity", "");
				json.put("remark", StringHelper.null2String(rs.getString("remark")));
				jsonArray.add(json);
			}
			JSONObject result = new JSONObject();
			result.put("totalSize", jsonArray.size());
			result.put("datas", jsonArray.toString());
			try {
				response.setContentType("application/json; charset=utf-8");  
				response.getWriter().print(result.toString());
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		
	}
	
	/*获取部门信息列表接口
	 * params 
     * pageno 当前页数(必需)
	 * pagesize 每页条数(必需)
	 * sqlwhere 过滤条件(非必须，如sqlwhere = " and a.lastname like '%张三%' ");
	 * */
	public void getManage() throws Exception{
		
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
		String sqlwhereParam = StringHelper.null2String(request.getParameter("sqlwhere"));
		String sqlwhere = "";
		if(!"".equals(sqlwhereParam)){
			if(sqlwhereParam.startsWith("and")){
				sqlwhere += sqlwhereParam;
			}else{
				sqlwhere += " and "+sqlwhereParam;
			}
		}
		
		String selSql = "select count(1) from  hrmdepartment  where 1=1  "+sqlwhere;
		rs.executeSql(selSql);
		if(rs.next()){
			recordCount = rs.getInt(1);
		}
		int iNextNum = pageno * pagesize;
		int ipageset = pagesize;
		if(recordCount - iNextNum + pagesize < pagesize) ipageset = recordCount - iNextNum + pagesize;
		//if(recordCount < pagesize) ipageset = recordCount;
		
		selSql = "select top " + iNextNum +"  hr.*  from  hrmdepartment hr  where 1=1 "+sqlwhere+" order by showorder ";
		selSql = "select top " + ipageset +" t1.* from (" + selSql + ") t1 order by t1.showorder desc";
		selSql = "select top " + ipageset +" t2.* from (" + selSql + ") t2 order by t2.showorder asc";
		System.out.println(selSql);
		rs.executeSql(selSql);
		JSONArray jsonArray = new JSONArray();
		while(rs.next()){
			JSONObject json = new JSONObject();
			String id = rs.getString("id");
			json.put("id", id);
			json.put("departmentmark", Util.null2String(rs.getString("departmentmark")));
			json.put("departmentname", Util.null2String(rs.getString("departmentname")));
			json.put("subcompanyid1", Util.null2String(rs.getString("subcompanyid1")));
			json.put("supdepid", Util.null2String(rs.getString("supdepid")));
			json.put("allsupdepid", Util.null2String(rs.getString("allsupdepid")));
			String showorder = Util.null2String(rs.getString("showorder"));
			json.put("showorder", showorder);
			String canceled = Util.null2String(rs.getString("canceled"));
			json.put("canceled", canceled);
			String departmentcode =  Util.null2String(rs.getString("departmentcode"));
			json.put("departmentcode", departmentcode);
			String coadjutant = Util.null2String(rs.getString("coadjutant"));
			json.put("coadjutant", coadjutant);
			json.put("zzjgbmfzr", Util.null2String(rs.getString("zzjgbmfzr")));
			json.put("zzjgbmfgld", Util.null2String(rs.getString("zzjgbmfgld")));
			json.put("jzglbmfzr", Util.null2String(rs.getString("jzglbmfzr")));
			json.put("jzglbmfgld", Util.null2String(rs.getString("jzglbmfgld")));
			json.put("bmfzr", Util.null2String(rs.getString("bmfzr")));
			json.put("bmfgld", Util.null2String(rs.getString("bmfgld")));
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
	 * 资产管理-无形资产-PC盘点接口
	 * @param 
	 * user	用户
	 * pageno	当前页数
	 * pagesize	每页显示数
	 */
	private void WXZCpcPanDian(User user){
		try {
			RecordSet rs = new RecordSet();
			RecordSet rs1 = new RecordSet();
			RecordSet rs2 = new RecordSet();
			RecordSet rs3 = new RecordSet();
			RecordSet rs4 = new RecordSet();
			String userid = String.valueOf(user.getUID());	//用户id
			int modeId1=91; //盘点模块id
			int modeId2=94; //资产变更模块id
			ModeRightInfo ModeRightInfo = new ModeRightInfo();
			String myArray = StringHelper.null2String(request.getParameter("myArray"));		//获取选中的资产id
			String [] id = null;
			String selectSql = "";
			String insertSql = "";
			String insertSql2 = "";
			id = myArray.split(",");
			String  wyid = "";
			String  wyid2 = "";
			int Year = (Integer.parseInt(DateHelper.getCurrentYear()))-1;
			String title = Year + "年资产盘点";
			for(int i =0;i<id.length;i++){
				selectSql = " select * from uf_am_immatassets where id = " + id[i]+" and usestatus!=3";
				rs.executeSql(selectSql);
				UUID uuid = UUID.randomUUID();
				wyid =uuid.toString();//生成唯一的标识码
				uuid = UUID.randomUUID();
				wyid2 =uuid.toString();//生成唯一的标识码
				if(rs.next()){//要根据实际改
					insertSql = "insert into  uf_am_checklog(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime," +
							"assetno,wxassetname,zcname,useperson,dept,newuseperson,newdept,remark,checkdate,checkperson,uuid) " +
							"VALUES("+modeId1+",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108)," +
							"'"+rs.getString("assetno")+"','"+rs.getString("id")+"','"+rs.getString("assetname")+"','"+rs.getString("manager")+"','"+rs.getString("managedept")+"'," +
							"'"+rs.getString("manager")+"','"+rs.getString("managedept")+"','批量盘点','"+DateHelper.getCurrentDate()+"','"+userid+"','"+wyid+"')";
					
				}
				boolean b = rs.executeSql(insertSql);
				if(b){
					String selectsql2 = " select id from uf_am_checklog where uuid = '" + wyid+"'";
					rs1.executeSql(selectsql2);
					if(rs1.next()){
						insertSql2 = "insert into uf_am_assetslog(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
								+"wxassetname,title,flowid,creator,createdate,checklog,uuid) " +
								"values ("+modeId2+",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"
								+"'"+rs.getString("id")+"','"+title+"','','"+userid+"',convert(varchar(10),getdate(),120),'"+rs1.getString("id")+"','"+wyid2+"')";
						boolean b1 = rs2.executeSql(insertSql2);
						if(b1){
							selectsql2 = "select id from uf_am_assetslog where uuid='"+wyid2+"'";
							rs3.executeSql(selectsql2);
							if(rs3.next()){
								ModeRightInfo.editModeDataShare(5,modeId1,Integer.parseInt(rs1.getString("id")));//新建的时候添加共享-所有人
								ModeRightInfo.editModeDataShare(5,modeId2,Integer.parseInt(rs3.getString("id")));//新建的时候添加共享-所有人
								
							}
							
						}
					}
				}
			}
			response.setContentType("application/text; charset=utf-8");  
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 资产管理-无形资产-PC盘点列表查询接口
	 * @param 
	 * user	用户
	 * pageno	当前页数
	 * pagesize	每页显示数
	 */
	private void WXZCpcPanDianPL(User user){
		try {
			RecordSet rs = new RecordSet();
			RecordSet rs1 = new RecordSet();
			RecordSet rs2 = new RecordSet();
			RecordSet rs3 = new RecordSet();
			RecordSet rs4 = new RecordSet();
			RecordSet rs5 = new RecordSet();
			ModeRightInfo ModeRightInfo = new ModeRightInfo();
			String userid = String.valueOf(user.getUID());	//用户id
			int modeId1=91; //盘点模块id
			int modeId2=94; //资产变更模块id
			String zcbh = StringHelper.null2String(request.getParameter("zcbh"));		
			String zcmc = StringHelper.null2String(request.getParameter("zcmc"));		
			String zclx1 = StringHelper.null2String(request.getParameter("zclx1"));		
			String zclx2 = StringHelper.null2String(request.getParameter("zclx2"));		
			String zclx3 = StringHelper.null2String(request.getParameter("zclx3"));		
			String sbszwz = StringHelper.null2String(request.getParameter("sbszwz"));		
			String cfdd = StringHelper.null2String(request.getParameter("cfdd"));		
			String glbm = StringHelper.null2String(request.getParameter("glbm"));		
			String syr = StringHelper.null2String(request.getParameter("syr"));		
			
			 String newselectSql = " select uaf.* from  uf_am_immatassets uaf where usestatus!=3 ";
			  if(StringHelper.isNotEmpty(zcbh)){
				  newselectSql += " and uaf.assetno like '%"+zcbh+"%'";
			  }
			  if(StringHelper.isNotEmpty(zcmc)){
				  newselectSql += " and uaf.assetname like '%"+zcmc+"%'";
			  }
			 
			
			String insertSql = "";
			String insertSql2 = "";
			int Year = (Integer.parseInt(DateHelper.getCurrentYear()))-1;
			String title = Year + "年资产盘点";
			rs.executeSql(newselectSql);
			
			while(rs.next()){
				UUID uuid = UUID.randomUUID();
				String  wyid =uuid.toString();//生成唯一的标识码
				uuid = UUID.randomUUID();
				String  wyid2 =uuid.toString();//生成唯一的标识码
				//要根据实际改
				insertSql = "insert into  uf_am_checklog(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime," +
						"assetno,wxassetname,zcname,useperson,dept,remark,checkdate,checkperson,uuid) " +
						"VALUES("+modeId1+",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108)," +
						"'"+rs.getString("assetno")+"','"+rs.getString("id")+"','"+rs.getString("assetname")+"','"+rs.getString("manager")+"','"+rs.getString("managedept")+"'," +
						"'批量盘点','"+DateHelper.getCurrentDate()+"','"+userid+"','"+wyid+"')";
				boolean b = rs1.executeSql(insertSql);
				if(b){
					String selectsql2 = " select id from uf_am_checklog where uuid = '" + wyid+"'";
					rs2.executeSql(selectsql2);
					if(rs2.next()){
						insertSql2 = "insert into uf_am_assetslog(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
								+"wxassetname,title,flowid,creator,createdate,checklog,uuid) " +
								"values ("+modeId2+",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"
								+"'"+rs.getString("id")+"','"+title+"','','"+userid+"',convert(varchar(10),getdate(),120),'"+rs2.getString("id")+"','"+wyid2+"')";
						boolean b1 = rs3.executeSql(insertSql2);
						if(b1){
							selectsql2 = "select id from uf_am_assetslog where uuid='"+wyid2+"'";
							rs4.executeSql(selectsql2);
							if(rs4.next()){
								ModeRightInfo.editModeDataShare(5,modeId1,Integer.parseInt(rs2.getString("id")));//新建的时候添加共享-所有人
								ModeRightInfo.editModeDataShare(5,modeId2,Integer.parseInt(rs4.getString("id")));//新建的时候添加共享-所有人
								
							}
							
						}
					}
				}
			}
			response.setContentType("application/text; charset=utf-8");  
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 判断用户是否进入盘点界面
	 * */
	public void IsUserid(User user){
		String userid = String.valueOf(user.getUID());	//用户id
		String id = StringHelper.null2String(request.getParameter("id"));		
		String modeid=StringHelper.null2String(request.getParameter("modeid"));
		
		RecordSet rs = new RecordSet();
		String sqlUser = "  select hrs.id from hrmrolemembers hrme left join hrmroles hrs on hrme.roleid = hrs.id " +
				" where hrs.id in (148,149) and hrme.resourceid = '"+userid+"'";
		rs.executeSql(sqlUser);
		try {
			response.setContentType("application/json; charset=utf-8");  
			boolean b = rs.next();
			if(b){
				int Year = (Integer.parseInt(DateHelper.getCurrentYear()))-1;
				String date = DateHelper.getCurrentDate();
				String selectSql = "select storagedate from ";
				if("89".equals(modeid)){
					selectSql += "uf_am_fixedassets ";
				}else if("90".equals(modeid)){
					selectSql += "uf_am_immatassets ";
				}
				selectSql += "where id = " +id;
				rs.executeSql(selectSql);
				boolean b2 = rs.next();
				String storagedate = "";
				if(b2){
					 storagedate = StringHelper.null2String(rs.getString("storagedate"));
				}
				selectSql = "select 1 from uf_am_checktask uac LEFT JOIN workflow_SelectItem ws on uac.year = ws.selectvalue  and ws.fieldid='11601' WHERE ws.selectname =" +
						"'"+Year+"' and '"+date+"' between uac.checkbegindate and uac.checkenddate and '"+storagedate+"' between uac.begindate and uac.enddate";
				rs.executeSql(selectSql);
				if(rs.next()){
					response.getWriter().print("1");
				}else{
					response.getWriter().print("2");
				}
			}else {

				response.getWriter().print("2");
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	
}
