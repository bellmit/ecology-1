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
 * 银企直连-合同管理
 * 
 * @author lsq
 * @date 2019/6/24
 * @version 1.0
 */
public class EASProjectContractManage extends BaseBean implements Action {

	@Override
	public String execute(RequestInfo request) {
		try {
			RecordSet rs1 = new RecordSet();
			RecordSet rs2 = new RecordSet();
			RecordSet rs3 = new RecordSet();
			RecordSet rs4 = new RecordSet();
			RecordSet rs5 = new RecordSet();
			RecordSet rs6 = new RecordSet();
			RecordSet rs7 = new RecordSet();
			RecordSet rs8 = new RecordSet();
			String requestid = Util.null2String(request.getRequestid());
			String sql = "select sqr,bm,khyh,khmc,yhzh,accountBankBrow,accountBankBranch,htmc,belongProCity from formtable_main_138 a inner join formtable_main_138_dt1 b on a.id=b.mainid where a.requestId='"
					+ requestid + "'";
			String sqr = ""; // 创建人
			String bm = ""; // 部门
			String htdf = ""; // 合同对方
			String htmc = ""; // 合同名称
			String khyh = ""; // 开户银行
			String khmc = ""; // 开户名称
			String yhzh = ""; // 银行账户
			String accountBankBrow = ""; // 开户银行选择
			String accountBankBranch = ""; // 账户银行支行
			String belongProCity = ""; // 所在省/直辖市
			
			rs1.execute(sql);
			while (rs1.next()) {
				sqr = Util.null2String(rs1.getString("sqr"));
				bm = Util.null2String(rs1.getString("bm"));
				htmc = Util.null2String(rs1.getString("htmc"));
				khyh = Util.null2String(rs1.getString("khyh"));
				khmc = Util.null2String(rs1.getString("khmc"));
				yhzh = Util.null2String(rs1.getString("yhzh"));
				accountBankBrow = Util.null2String(rs1
						.getString("accountBankBrow"));
				accountBankBranch = Util.null2String(rs1
						.getString("accountBankBranch"));
				belongProCity = Util
						.null2String(rs1.getString("belongProCity"));
				String sql7 = "select gys from formtable_main_142 where id='"
						+ htmc + "'";
				rs7.executeSql(sql7);
				if (rs7.next()) {
					htdf = Util.null2String(rs7.getString("gys"));
				}
				writeLog("合同对方:" + htdf);
				sql = "select count(*) as 'count' from uf_payInfoCompany where companyName='"
						+ htdf
						+ "' and accountName='"
						+ khmc
						+ "' and accountNumber='"
						+ yhzh
						+ "' and accountBank='"
						+ khyh
						+ "' and accountBankBranch='"
						+ accountBankBranch
						+ "' and belongProCity='" + belongProCity + "'";
				String count = "";
				rs2.execute(sql);
				if (rs2.next()) {
					count = Util.null2String(rs2.getString("count"));
				}

				if (count.equals("0")) { // coun=0 则说明表中 没有该供应商 则新增
					UUID uuid = UUID.randomUUID();
					String wyid = uuid.toString();// 生成唯一的标识码
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					String date = format.format(new Date()); // 日期
					SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm");
					String time = formatTime.format(new Date()); // 时间
					int modeid = 149;
					String sqlin = "insert into uf_payInfoCompany(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
							+ "companyName,accountName,accountNumber,accountBank,createPerson,createDept,createDates,createTimes," +
							"companyNameNEW,accountNameNEW,accountNumberNEW,accountBankNEW,accountBankBrow,uuid,accountBankBranch,accountBankBranchNEW,belongProCity,belongProCityNEW) values ("
							+ modeid
							+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),'"
							+ htdf
							+ "','"
							+ khmc
							+ "','"
							+ yhzh
							+ "','"
							+ khyh
							+ "','"
							+ sqr
							+ "','"
							+ bm
							+ "','"
							+ date
							+ "','"
							+ time
							+ "','"
							+ htdf
							+ "','"
							+ khmc
							+ "','"
							+ yhzh
							+ "','"
							+ khyh
							+ "','"
							+ accountBankBrow
							+ "','"
							+ wyid
							+ "','"
							+ accountBankBranch
							+ "','"
							+ accountBankBranch
							+ "','"
							+ belongProCity
							+ "','"
							+ belongProCity + "')";
					rs3.executeSql(sqlin);
					String sqlre = "select id from uf_payInfoCompany where uuid='"
							+ wyid + "'";
					rs4.executeSql(sqlre);
					if (rs4.next()) {
						String ids = Util.null2String(rs4.getString("id"));// 查询出变更记录id
						int logid = Integer.parseInt(ids);
						ModeRightInfo ModeRightInfo = new ModeRightInfo();
						ModeRightInfo.editModeDataShare(5, modeid, logid);// 新建的时候添加共享-所有人
					}
					
					String city="";
					String sqlcity="select city  from uf_cityLists where id='"+belongProCity+"'";
					rs8.executeSql(sqlcity);
					if(rs7.next()){
						city=Util.null2String(rs8.getString("city"));
					}
                    writeLog("管理sqlcity:"+sqlcity);					
					UUID uuidLog = UUID.randomUUID();
					String wyidLog = uuidLog.toString();// 生成唯一的标识码
					int modeidLog = 148;
					String sqlinLog = "insert into uf_CompanyInfoLog(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
							+ "alterPerson,alterPersonDept,alterDates,alterTimes,alterCompanyName,alterAccountName,alterAccountNumber,alterAccountBank,uuid,alterStatus,accountBankBranch,belongProCity) values ("
							+ modeidLog
							+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),'"
							+ sqr
							+ "','"
							+ bm
							+ "','"
							+ date
							+ "','"
							+ time
							+ "','"
							+ htdf
							+ "','"
							+ khmc
							+ "','"
							+ yhzh
							+ "','"
							+ khyh
							+ "','"
							+ wyidLog
							+ "','2','"
							+ accountBankBranch + "','" + city + "')";
					rs5.executeSql(sqlinLog);
					String sqlLog = "select id from uf_CompanyInfoLog where uuid='"
							+ wyidLog + "'";
					rs6.executeSql(sqlLog);
					writeLog("管理sqlLog:"+sqlLog);
					if (rs6.next()) {
						String ids = Util.null2String(rs6.getString("id"));// 查询出变更记录id
						int logid = Integer.parseInt(ids);
						ModeRightInfo ModeRightInfo = new ModeRightInfo();
						ModeRightInfo.editModeDataShare(5, modeidLog, logid);// 新建的时候添加共享-所有人
					}
				} else {
					writeLog("uf_payInfoCompany中已存在该供应商信息");
				}

			}
		} catch (Exception e) {
			writeLog("银企直连项目合同账户异常" + e);
			return "0";
		}

		return null;
	}

}
