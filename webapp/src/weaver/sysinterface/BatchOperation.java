package weaver.sysinterface;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;
import weaver.conn.RecordSet;
import weaver.file.Prop;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.hrm.User;

import com.weaver.formmodel.mobile.manager.MobileUserInit;
import com.weaver.formmodel.util.DateHelper;
import com.weaver.formmodel.util.StringHelper;

public class BatchOperation extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private HttpServletRequest request;
	private HttpServletResponse response;

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		this.request = request;
		this.response = response;
		try {
			String action = StringHelper.null2String(request
					.getParameter("action"));
			User user = MobileUserInit.getUser(request, response);
			if (user == null) {
				JSONObject checkUser = new JSONObject();
				checkUser.put("msgstatus", "0");
				checkUser.put("msg", "服务器端重置了登录信息，请重新登录");
				response.setContentType("application/json; charset=utf-8");
				response.getWriter().print(checkUser.toString());
				return;
			} else if ("pltsjd".equals(action)) {
				pltsjd(user);
			} else if ("plchushen".equals(action)) {
				plchushen(user);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	/**
	 * 批量操作-批量推送金蝶接口
	 * 
	 * @param user
	 *            用户
	 * @throws IOException
	 */
	private void pltsjd(User user) throws IOException {
		BaseBean log=new BaseBean();
		String userid = String.valueOf(user.getUID());
		String quanxianid = Prop.getPropValue("EASPersonJuri", "recheck");
		RecordSet rsd = new RecordSet();
		RecordSet rsd2 = new RecordSet();
		RecordSet rsd3 = new RecordSet();
		if (userid.equals(quanxianid)) {
			String myArray = StringHelper.null2String(request
					.getParameter("myArray"));// 获取选中的id
			String[] id = myArray.split(",");
			for (int i = 0; i < id.length; i++) {
				rsd.executeSql("select * from uf_payList where id='" + id[i]
						+ "'");
				String status = "";
				if (rsd.next()) {
					status = Util.null2String(rsd.getString("dealstatus"));// 状态
				}
				if (!"2".equals(status)) {
					response.setContentType("application/text; charset=utf-8");
					response.getWriter().print("2");
					return;
				}
			}

			String skfgjNum = Prop.getPropValue("addfkd", "skfgjNum");// 收款方国家
			String expenseType = Prop.getPropValue("addfkd", "expenseType");// 费用类型编码
			String sourceBillType = Prop.getPropValue("addfkd",
					"sourceBillType");// 付款类型编码
			String OppAccountNum = Prop.getPropValue("addfkd", "OppAccountNum");// 对方科目编码
			String CompanyName = Prop.getPropValue("addfkd", "CompanyName");// 付款公司编码
			String payerAccountBank = Prop.getPropValue("addfkd",
					"payerAccountBank");// 付款账户编码,付款账户的账户账号
			String payeeType = Prop.getPropValue("addfkd", "payeeType");// 收款人类型
			String flag = "0"; // 判断是否有 推送失败的数据 0没有 1有
			String check="0";  //判断必填字段是否 填写
			for (int i = 0; i < id.length; i++) {
				String bizDate = "";// 业务日期,抛单日期
				String payeeName = "";// 收款人名称
				String payeeAcccountBankName = "";// 收款银行名称,银行名称+支行名称
				String payeeAccountBank = "";// 收款账户,收款人银行账号
				String belongProCity = "";// 省份id
				String skfsNum = "";// 收款方省
				String skfsxNum = "";// 收款方市
				String skfxianNum = "";// 收款方县
				String useWay = "";// 付款类型id
				String payRemark = "";// 款项用途
				String SourceSys = Prop.getPropValue("addfkd", "SourceSys");// 异构系统代码
				String MyBillGUID = "";// 异构系统单据ID,ERP表单ID
				String actPayAmt = "";// 付款金额
				String flowNum = "";// 文号
				String tsjdresult = "1";// 推送金蝶接口结果,0成功,1失败
				String flow = "";// 对应流程
				rsd.executeSql("select * from uf_payList where id='" + id[i]
						+ "'");
				String status = "";
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				String disposeTime = dateFormat.format(new Date());
				if (rsd.next()) {
					status = Util.null2String(rsd.getString("dealstatus"));// 状态
					bizDate = disposeTime;// 当前日期
					payeeName = Util.null2String(rsd
							.getString("payAccountName"));// 收款人名称
					payeeAcccountBankName = Util.null2String(rsd
							.getString("depositBank"));// 收款银行名称
					payeeAccountBank = Util.null2String(rsd
							.getString("payAccountNum"));// 收款账户
					belongProCity = Util.null2String(rsd
							.getString("belongProCity"));// 省份id
					flow = Util.null2String(rsd.getString("flow"));// 对应流程
					rsd2.executeSql("select * from uf_cityLists where id='"
							+ belongProCity + "'");
					if (rsd2.next()) {
						skfsNum = Util.null2String(rsd2.getString("province"));// 收款方省
						skfsxNum = Util.null2String(rsd2.getString("city"));// 收款方市
						if ("732".equals(belongProCity)
								|| "734".equals(belongProCity)
								|| "733".equals(belongProCity)
								|| "731".equals(belongProCity)) {
							skfsxNum = "市辖区";
						}
					}
					useWay = Util.null2String(rsd.getString("useWay"));// 付款类型id
					rsd3.executeSql("select * from uf_payUse where id='"
							+ useWay + "'");
					if (rsd3.next()) {
						payRemark = Util.null2String(rsd3.getString("useWay"));// 款项用途
					}
					flowNum = Util.null2String(rsd.getString("flowNum"));// 文号
					MyBillGUID = Util.null2String(rsd.getString("uuid"));// 随机码ID
					SourceSys += flowNum + ":" + MyBillGUID;
					actPayAmt = Util.null2String(rsd.getString("payMoney"));// 付款金额
				}
				if (CompanyName.equals("") || bizDate.equals("")
						|| payerAccountBank.equals("") || payeeType.equals("")
						|| payeeName.equals("")
						|| payeeAcccountBankName.equals("")
						|| payeeAccountBank.equals("") || skfgjNum.equals("")
						|| skfsNum.equals("") || skfsxNum.equals("")
						|| payRemark.equals("") || SourceSys.equals("")
						|| MyBillGUID.equals("") || actPayAmt.equals("")
						|| expenseType.equals("")) {   //必填字段未填写
					check="1";	
				}else{
					if (status.equals("2")) {
						String xml = "<KingdeeWsDataSet><KingdeeWsData>"
								+ "<CompanyName>"
								+ CompanyName
								+ "</CompanyName>"
								+ "<bizDate>"
								+ bizDate
								+ "</bizDate>"
								+ "<payerAccountBank>"
								+ payerAccountBank
								+ "</payerAccountBank>"
								+ "<payeeType>"
								+ payeeType
								+ "</payeeType>"
								+ "<payeeName>"
								+ payeeName
								+ "</payeeName>"
								+ "<payeeAccountBankName>"
								+ payeeAcccountBankName
								+ "</payeeAccountBankName>"
								+ "<payeeAccountBank>"
								+ payeeAccountBank
								+ "</payeeAccountBank>"
								+ "<skfgjNum>"
								+ skfgjNum
								+ "</skfgjNum>"
								+ "<skfsNum>"
								+ skfsNum
								+ "</skfsNum>"
								+ "<skfsxNum>"
								+ skfsxNum
								+ "</skfsxNum>"
								+ "<skfxianNum>"
								+ skfxianNum
								+ "</skfxianNum>"
								+ "<payRemark>"
								+ payRemark
								+ "</payRemark>"
								+ "<sourceBillType>"
								+ sourceBillType
								+ "</sourceBillType>"
								+ "<SourceSys>"
								+ SourceSys
								+ "</SourceSys>"
								+ "<MyBillGUID>"
								+ MyBillGUID
								+ "</MyBillGUID>"
								+ "<actPayAmt>"
								+ actPayAmt
								+ "</actPayAmt>"
								+ "<Entry>"
								+ "<expenseType>"
								+ expenseType
								+ "</expenseType>"
								+ "<amount>"
								+ actPayAmt
								+ "</amount>"
								+ // 付款金额（应与表头金额一致，否则报错）
								"<OppAccountNum>"
								+ OppAccountNum
								+ "</OppAccountNum>" + // 对方科目编码（业务人员提供一个不带辅助核算项目的科目即可）
								"<payRemark></payRemark>" + // 备注
								"</Entry></KingdeeWsData></KingdeeWsDataSet>";
						EASWSDispose dispose = new EASWSDispose();
						String result = dispose.AddFKD(xml);
						log.writeLog("result:" + result);
						if (!"".equals(result)) {
							if ("1".equals(result.substring(0, 1))) {
								RecordSet rs = new RecordSet();
								String sql = "update uf_payList set dealstatus='3',secondDate='"
										+ disposeTime
										+ "'  where id='"
										+ id[i]
										+ "'";
								rs.executeSql(sql);
								status = "3";// 成功，状态改变
								tsjdresult = "0";// 成功，状态改变
								String curddatetime = DateHelper.getCurDateTime();// 当前日期时间
								RecordSet rsd4 = new RecordSet();
								rsd4.executeSql("select max(num)as xh from uf_payListlog");
								int num = 1;// 序号
								if (rsd4.next()) {
									if (rsd4.getString("xh") != null
											&& !"".equals(rsd4.getString("xh"))) {
										num = rsd4.getInt("xh") + 1;
									}
								}
								RecordSet rs1 = new RecordSet();
								int modeId = 140; // 建模id
								// 插入变更记录
								String sql2 = "insert into uf_payListLog(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
										+ "num,flowNum,dealMoney,dealType,useDateTime,useResult,userPerson,flow) values ("
										+ modeId
										+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"
										+ ""
										+ num
										+ ",'"
										+ flowNum
										+ "','"
										+ actPayAmt
										+ "','"
										+ status
										+ "','"
										+ curddatetime
										+ "','"
										+ tsjdresult
										+ "','"
										+ userid + "','" + flow + "')";
								rs1.executeSql(sql2);
								RecordSet rs2 = new RecordSet();
								sql2 = "select id from uf_payListLog where num="
										+ num + "";
								rs2.executeSql(sql2);
								if (rs2.next()) {
									String ids = Util.null2String(rs2
											.getString("id"));// 查询出变更记录id
									int logid = Integer.parseInt(ids);
									ModeRightInfo ModeRightInfo = new ModeRightInfo();
									ModeRightInfo.editModeDataShare(5, modeId,
											logid);// 新建的时候添加共享-所有人
								}

							} else {
								String curddatetime = DateHelper.getCurDateTime();// 当前日期时间
								RecordSet rsd4 = new RecordSet();
								rsd4.executeSql("select max(num)as xh from uf_payListlog");
								int num = 1;// 序号
								if (rsd4.next()) {
									if (rsd4.getString("xh") != null
											&& !"".equals(rsd4.getString("xh"))) {
										num = rsd4.getInt("xh") + 1;
									}
								}
								RecordSet rs1 = new RecordSet();
								int modeId = 140; // 建模id
								// 插入变更记录
								String sql = "insert into uf_payListLog(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
										+ "num,flowNum,dealMoney,dealType,useDateTime,useResult,userPerson,flow) values ("
										+ modeId
										+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"
										+ ""
										+ num
										+ ",'"
										+ flowNum
										+ "','"
										+ actPayAmt
										+ "','"
										+ status
										+ "','"
										+ curddatetime
										+ "','"
										+ tsjdresult
										+ "','"
										+ userid + "','" + flow + "')";
								rs1.executeSql(sql);
								RecordSet rs2 = new RecordSet();
								sql = "select id from uf_payListLog where num='"
										+ num + "'";
								rs2.executeSql(sql);
								if (rs2.next()) {
									String ids = Util.null2String(rs2
											.getString("id"));// 查询出变更记录id
									int logid = Integer.parseInt(ids);
									ModeRightInfo ModeRightInfo = new ModeRightInfo();
									ModeRightInfo.editModeDataShare(5, modeId,
											logid);// 新建的时候添加共享-所有人
								}
								flag = "1";
							}
						} else {
							String curddatetime = DateHelper.getCurDateTime();// 当前日期时间
							RecordSet rsd4 = new RecordSet();
							rsd4.executeSql("select max(num)as xh from uf_payListlog");
							int num = 1;// 序号
							if (rsd4.next()) {
								if (rsd4.getString("xh") != null
										&& !"".equals(rsd4.getString("xh"))) {
									num = rsd4.getInt("xh") + 1;
								}
							}
							RecordSet rs1 = new RecordSet();
							int modeId = 140; // 建模id
							// 插入变更记录
							String sql = "insert into uf_payListLog(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
									+ "num,flowNum,dealMoney,dealType,useDateTime,useResult,userPerson,flow) values ("
									+ modeId
									+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"
									+ ""
									+ num
									+ ",'"
									+ flowNum
									+ "','"
									+ actPayAmt
									+ "','"
									+ status
									+ "','"
									+ curddatetime
									+ "','"
									+ tsjdresult
									+ "','"
									+ userid
									+ "','"
									+ flow
									+ "')";
							rs1.executeSql(sql);
							RecordSet rs2 = new RecordSet();
							sql = "select id from uf_payListLog where num='" + num
									+ "'";
							rs2.executeSql(sql);
							if (rs2.next()) {
								String ids = Util.null2String(rs2.getString("id"));// 查询出变更记录id
								int logid = Integer.parseInt(ids);
								ModeRightInfo ModeRightInfo = new ModeRightInfo();
								ModeRightInfo.editModeDataShare(5, modeId, logid);// 新建的时候添加共享-所有人
							}
							flag = "1";
						}
					}
				}
			}
            if(check.equals("1")){
				response.setContentType("application/text; charset=utf-8");
				response.getWriter().print("-1");
            }
			if (flag.equals("0")) {
				response.setContentType("application/text; charset=utf-8");
				response.getWriter().print("0");
			} else {
				response.setContentType("application/text; charset=utf-8");
				response.getWriter().print("3");
			}

		} else {
			response.setContentType("application/text; charset=utf-8");
			response.getWriter().print("1");
		}
	}

	/**
	 * 批量操作-初审批量通过接口
	 * 
	 * @param user
	 *            用户
	 * @throws IOException
	 */
	private void plchushen(User user) throws IOException {
		String userid = String.valueOf(user.getUID());
		String quanxianid = Prop.getPropValue("EASPersonJuri", "firstTrial");
		if (userid.equals(quanxianid)) {
			RecordSet rs = new RecordSet();
			RecordSet rs2 = new RecordSet();
			String myArray = StringHelper.null2String(request
					.getParameter("myArray"));// 获取选中的id
			String[] id = myArray.split(",");
			for (int i = 0; i < id.length; i++) {
				rs.executeSql("select * from uf_payList where id='" + id[i]
						+ "'");
				String status = "";
				if (rs.next()) {
					status = Util.null2String(rs.getString("dealstatus"));// 状态
				}
				if (!"0".equals(status)) {
					response.setContentType("application/text; charset=utf-8");
					response.getWriter().print("2");
					return;
				}
			}
			for (int i = 0; i < id.length; i++) {
				rs.executeSql("select * from uf_payList where id='" + id[i]
						+ "'");
				String status = "";
				if (rs.next()) {
					status = Util.null2String(rs.getString("dealstatus"));// 状态
				}
				if (status.equals("0")) {
					rs2.executeSql("update uf_payList set dealstatus='2',firstDate='"
							+ DateHelper.getCurrentDate()
							+ "'  where id='"
							+ id[i] + "'");
				}
			}
			response.setContentType("application/text; charset=utf-8");
			response.getWriter().print("0");
		} else {
			response.setContentType("application/text; charset=utf-8");
			response.getWriter().print("1");
		}
	}
}
