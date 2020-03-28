package weaver.interfaces.workflow.action;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import weaver.conn.RecordSet;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;

/**
 * 银企直连供应商操作日志
 * 
 * @author lsq
 * @date 2019/6/20
 * @version 1.0
 */
public class EASCompanyLog extends BaseBean implements Action {

	@Override
	public String execute(RequestInfo request) {
		try {
			String requestid = Util.null2String(request.getRequestid());
			String userid = request.getLastoperator();
			RecordSet rs = new RecordSet();
			RecordSet rs2 = new RecordSet();
			RecordSet rs3 = new RecordSet();
			RecordSet rsd = new RecordSet();
			RecordSet rsd2 = new RecordSet();
			String deparmentid = "";
			if (!userid.equals("1")) {
				String sqld = "select departmentid from HrmResource where id="
						+ userid + "";
				rsd.execute(sqld);
				if (rsd.next()) {
					deparmentid = Util.null2String(rsd
							.getString("departmentid"));

				}
			}

			String sql = "select id,companyName,accountName,accountNumber,accountBank,companyNameNEW,accountNameNEW,accountNumberNEW,accountBankNEW from  uf_payInfoCompany where id='"
					+ requestid + "'";
			rs.executeSql(sql);
			if (rs.next()) {
				String id = Util.null2String(rs.getString("id"));
				String companyName = Util.null2String(rs
						.getString("companyName"));
				String accountName = Util.null2String(rs
						.getString("accountName"));
				String accountNumber = Util.null2String(rs
						.getString("accountNumber"));
				String accountBank = Util.null2String(rs
						.getString("accountBank"));
				String companyNameNEW = Util.null2String(rs
						.getString("companyNameNEW"));
				String accountNameNEW = Util.null2String(rs
						.getString("accountNameNEW"));
				String accountNumberNEW = Util.null2String(rs
						.getString("accountNumberNEW"));
				String accountBankNEW = Util.null2String(rs
						.getString("accountBankNEW"));
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				String date = format.format(new Date()); // 日期
				SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm");
				String time = formatTime.format(new Date()); // 时间
				String companyNameLog = "";
				String accountNameLog = "";
				String accountNumberLog = "";
				String accountBankLog = "";
				String flag = "0"; // 标记
				if (!companyName.equals(companyNameNEW)) {
					companyNameLog = companyNameNEW + " 改为了:" + companyName;
					flag = "1";
				}
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
				if (flag.equals("1")) { // 有修改过 供应商信息
					UUID uuid = UUID.randomUUID();
					String wyid = uuid.toString();// 生成唯一的标识码
					int modeid = 145;
					String sqlin = "insert into uf_CompanyInfoLog(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
							+ "alterPerson,alterPersonDept,alterDates,alterTimes,alterCompanyName,alterAccountName,alterAccountNumber,alterAccountBank,uuid,alterStatus) values ("
							+ modeid
							+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),'"
							+ userid
							+ "','"
							+ deparmentid
							+ "','"
							+ date
							+ "','"
							+ time
							+ "','"
							+ companyNameLog
							+ "','"
							+ accountNameLog
							+ "','"
							+ accountNumberLog
							+ "','"
							+ accountBankLog + "','" + wyid + "','1')";
					rsd2.executeSql(sqlin);
					String sqlre = "select id from uf_CompanyInfoLog where uuid='"
							+ wyid + "'";
					rs2.executeSql(sqlre);
					if (rs2.next()) {
						String ids = Util.null2String(rs2.getString("id"));// 查询出变更记录id
						int logid = Integer.parseInt(ids);
						ModeRightInfo ModeRightInfo = new ModeRightInfo();
						ModeRightInfo.editModeDataShare(5, modeid, logid);// 新建的时候添加共享-所有人
					}

					String sqlup = "update uf_payInfoCompany set accountName='"
							+ accountName + "',accountNumber='" + accountNumber
							+ "',accountBank='" + accountBank
							+ "',accountNameNEW='" + accountName
							+ "',accountNumberNEW='" + accountNumber
							+ "',accountBankNEW='" + accountBank
							+ "' where id='" + id + "'";
					rs3.executeSql(sqlup);
				} else {
					writeLog("当前用户未修改供应商信息");
				}

			}
		} catch (Exception e) {
			writeLog("记录修改供应商信息日志异常" + e);
		}

		return null;
	}

}
