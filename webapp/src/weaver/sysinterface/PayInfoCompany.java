package weaver.sysinterface;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;
import weaver.conn.RecordSet;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.hrm.User;

import com.weaver.formmodel.mobile.manager.MobileUserInit;

/**
 * 供应商账单信息
 * 
 * @author lsq
 * @date 2019/6/23
 * @version 1.0
 */
public class PayInfoCompany extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private HttpServletRequest request;

	private HttpServletResponse response;

	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
		try {
			String action = Util.null2String(request.getParameter("action"));
			User user = MobileUserInit.getUser(request, response);
			if (user == null) {
				JSONObject checkUser = new JSONObject();
				checkUser.put("msgstatus", "0");
				checkUser.put("msg", "服务器端重置了登录信息，请重新登录");
				response.setContentType("application/json; charset=utf-8");
				response.getWriter().print(checkUser.toString());
				return;
			}
			if ("addPayInfoCompany".equals(action)) { // 新建接口
				addPayInfoCompany(user);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addPayInfoCompany(User user) {
		BaseBean log = new BaseBean();
		log.writeLog("进入供应商新增接口");
		RecordSet rs1 = new RecordSet();
		RecordSet rs8 = new RecordSet();
		RecordSet rs5 = new RecordSet();
		RecordSet rs6 = new RecordSet();
		RecordSet rs7 = new RecordSet();
		RecordSet rs9 = new RecordSet();
		RecordSet rs10 = new RecordSet();
		RecordSet rs11 = new RecordSet();
		String companyName = Util.null2String(request
				.getParameter("companyName")); // 公司名称
		String accountName = Util.null2String(request
				.getParameter("accountName")); // 开户名称
		String accountNumber = Util.null2String(request
				.getParameter("accountNumber"));// 银行账号
		String accountBank = Util.null2String(request
				.getParameter("accountBank")); // 开户银行
		String accountBankBranch = Util.null2String(request
				.getParameter("accountBankBranch")); // 分支行选择
		String belongProCity = Util.null2String(request
				.getParameter("belongProCity")); // 所在省/直辖市
		String typeid = Util.null2String(request.getParameter("typeid")); // 操作类型
																			// 0新增
																			// 1编辑
		String city="";
		String sqlcity="select city  from uf_cityLists where id='"+belongProCity+"'";
		rs10.executeSql(sqlcity);
		if(rs10.next()){
			city=Util.null2String(rs10.getString("city"));
		}
		log.writeLog("sqlcity:" + sqlcity);
		String sql = "select count(*) as 'count' from uf_payInfoCompany where companyName='"
				+ companyName
				+ "' and accountName='"
				+ accountName
				+ "' and accountNumber='"
				+ accountNumber
				+ "' and accountBank='"
				+ accountBank
				+ "' and accountBankBranch='"
				+ accountBankBranch
				+ "' and belongProCity='" + belongProCity + "'";
		String count = "";
		rs1.execute(sql);
		if (rs1.next()) {
			count = Util.null2String(rs1.getString("count"));
		}
		log.writeLog("count:"+count);
		log.writeLog("typeid:"+typeid);
		if (typeid.equals("0")||typeid.equals("")) {
			if (count.equals("0")) {
				// 往日志表中写数据
				String userid = Util.null2String(user.getUID());
				String deparmentid = "";
				if (!userid.equals("1")) {
					String sqld = "select departmentid from HrmResource where id="
							+ userid + "";
					rs7.execute(sqld);
					if (rs7.next()) {
						deparmentid = Util.null2String(rs7
								.getString("departmentid"));
					}
				}
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				String date = format.format(new Date()); // 日期
				SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm");
				String time = formatTime.format(new Date()); // 时间
				
				
				UUID uuidLog = UUID.randomUUID();
				String wyidLog = uuidLog.toString();// 生成唯一的标识码
				int modeidLog = 148;
				String sqlinLog = "insert into uf_CompanyInfoLog(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
						+ "alterPerson,alterPersonDept,alterDates,alterTimes,alterCompanyName,alterAccountName,alterAccountNumber,alterAccountBank,uuid,alterStatus,accountBankBranch,belongProCity) values ("
						+ modeidLog
						+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),'"
						+ userid
						+ "','"
						+ deparmentid
						+ "','"
						+ date
						+ "','"
						+ time
						+ "','"
						+ companyName
						+ "','"
						+ accountName
						+ "','"
						+ accountNumber
						+ "','"
						+ accountBank
						+ "','"
						+ wyidLog
						+ "','0','"
						+ accountBankBranch
						+ "','"
						+ city + "')";
				rs5.executeSql(sqlinLog);
				String sqlLog = "select id from uf_CompanyInfoLog where uuid='"
						+ wyidLog + "'";
				rs6.executeSql(sqlLog);
				if (rs6.next()) {
					String ids = Util.null2String(rs6.getString("id"));// 查询出变更记录id
					int logid = Integer.parseInt(ids);
					ModeRightInfo ModeRightInfo = new ModeRightInfo();
					ModeRightInfo.editModeDataShare(5, modeidLog, logid);// 新建的时候添加共享-所有人
				}
			} else {
				try {
					response.setContentType("application/json; charset=utf-8");
					response.getWriter().print("0");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			String id = Util.null2String(request.getParameter("id"));
			/*
			 * String
			 * sqljuri="select createperson from uf_payInfoCompany where id='"
			 * +id+"'"; String creator=""; rs9.executeSql(sqljuri);
			 * if(rs9.next()){
			 * creator=Util.null2String(rs9.getString("createperson")); }
			 * if(creator.equals(Util.null2String(user.getUID()))){ //编辑人若等于创建人
			 * 则有权编辑保存
			 */if (count.equals("0")) {
				log.writeLog("进入了编辑...");
				String accountNameNEW = Util.null2String(request
						.getParameter("accountNameNEW")); // 开户名称
				String accountNumberNEW = Util.null2String(request
						.getParameter("accountNumberNEW"));// 银行账号
				String accountBankNEW = Util.null2String(request
						.getParameter("accountBankNEW")); // 开户银行
				String accountBankBranchNEW = Util.null2String(request
						.getParameter("accountBankBranchNEW")); // 分支行选择
				String belongProCityNEW = Util.null2String(request
						.getParameter("belongProCityNEW")); // 所在省/直辖市
				String accountNameLog = "";
				String accountNumberLog = "";
				String accountBankLog = "";
				String accountBankBranchLog = "";
				String belongProCityLog = "";
				String flag = "0"; // 标记
				if (!accountName.equals(accountNameNEW)) {
					accountNameLog = accountNameNEW + " 改为了:" + accountName;
					flag = "1";
				}
				if (!accountNumber.equals(accountNumberNEW)) {
					accountNumberLog = accountNumberNEW + " 改为了:"
							+ accountNumber;
					flag = "1";
				}
				if (!accountBank.equals(accountBankNEW)) {
					accountBankLog = accountBankNEW + " 改为了:" + accountBank;
					flag = "1";
				}
				if (!accountBankBranch.equals(accountBankBranchNEW)) {
					accountBankBranchLog = accountBankBranchNEW + " 改为了:"
							+ accountBankBranch;
					flag = "1";
				}
				String citynew="";
				String sqlcitynew="select city  from uf_cityLists where id='"+belongProCityNEW+"'";
				rs11.executeSql(sqlcitynew);
				if(rs11.next()){
					citynew=Util.null2String(rs11.getString("city"));
				}				
				if (!belongProCity.equals(belongProCityNEW)) {
					belongProCityLog = citynew + " 改为了:"
							+ city;
					flag = "1";
				}
				
				log.writeLog("sqlcitynew:" + sqlcitynew);
				if (flag.equals("1")) {
					// 往日志表中写数据
					String userid = Util.null2String(user.getUID());
					String deparmentid = "";
					if (!userid.equals("1")) {
						String sqld = "select departmentid from HrmResource where id="
								+ userid + "";
						rs7.execute(sqld);
						if (rs7.next()) {
							deparmentid = Util.null2String(rs7
									.getString("departmentid"));
						}
					}
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					String date = format.format(new Date()); // 日期
					SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm");
					String time = formatTime.format(new Date()); // 时间
					UUID uuidLog = UUID.randomUUID();
					String wyidLog = uuidLog.toString();// 生成唯一的标识码
					int modeidLog = 148;
					String sqlinLog = "insert into uf_CompanyInfoLog(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
							+ "alterPerson,alterPersonDept,alterDates,alterTimes,alterAccountName,alterAccountNumber,alterAccountBank,uuid,alterStatus,accountBankBranch,belongProCity,alterCompanyName) values ("
							+ modeidLog
							+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),'"
							+ userid
							+ "','"
							+ deparmentid
							+ "','"
							+ date
							+ "','"
							+ time
							+ "','"
							+ accountNameLog
							+ "','"
							+ accountNumberLog
							+ "','"
							+ accountBankLog
							+ "','"
							+ wyidLog
							+ "','1','"
							+ accountBankBranchLog
							+ "','" + belongProCityLog + "','"+companyName+"')";
					rs5.executeSql(sqlinLog);
					log.writeLog("sqlinLog:"+sqlinLog);
					String sqlLog = "select id from uf_CompanyInfoLog where uuid='"
							+ wyidLog + "'";
					rs6.executeSql(sqlLog);
					if (rs6.next()) {
						String ids = Util.null2String(rs6.getString("id"));// 查询出变更记录id
						int logid = Integer.parseInt(ids);
						ModeRightInfo ModeRightInfo = new ModeRightInfo();
						ModeRightInfo.editModeDataShare(5, modeidLog, logid);// 新建的时候添加共享-所有人
					}
					String sqlup = "update uf_payInfoCompany set accountName='"
							+ accountName + "',accountNumber='" + accountNumber
							+ "',accountBank='" + accountBank
							+ "',accountBankBranch='" + accountBankBranch
							+ "',belongProCity='" + belongProCity
							+ "',accountNameNEW='" + accountName
							+ "',accountNumberNEW='" + accountNumber
							+ "',accountBankNEW='" + accountBank
							+ "',accountBankBranchNEW='" + accountBankBranch
							+ "',belongProCityNEW='" + belongProCity
							+ "' where id='" + id + "'";
					rs8.executeSql(sqlup);
					log.writeLog("sqlup:"+sqlup);
				} else if (flag.equals("0")) {
					try {
						response.setContentType("application/json; charset=utf-8");
						response.getWriter().print("1");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else {
				try {
					response.setContentType("application/json; charset=utf-8");
					response.getWriter().print("0");
				} catch (Exception e) {
				}

			}
			/*
			 * }else{ try {
			 * response.setContentType("application/json; charset=utf-8");
			 * response.getWriter().print("2"); } catch (IOException e) {
			 * e.printStackTrace(); } }
			 */

		}

	}
}
