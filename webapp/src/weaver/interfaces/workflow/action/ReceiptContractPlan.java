package weaver.interfaces.workflow.action;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import weaver.conn.RecordSet;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.schedule.BaseCronJob;
import weaver.soa.workflow.request.Cell;
import weaver.soa.workflow.request.DetailTable;
import weaver.soa.workflow.request.DetailTableInfo;
import weaver.soa.workflow.request.MainTableInfo;
import weaver.soa.workflow.request.Property;
import weaver.soa.workflow.request.RequestInfo;
import weaver.soa.workflow.request.RequestService;
import weaver.soa.workflow.request.Row;

/**
 * 收款合同计划
 * 
 * @author lsq
 * @date 2019/4/24
 * @version 1.0
 */
public class ReceiptContractPlan extends BaseCronJob {
	BaseBean lg = new BaseBean();
	SelectSYFJ sc = new SelectSYFJ();
	RecordSet rs = new RecordSet();
	Calendar todaycal = Calendar.getInstance();
	String syndate = Util.add0(todaycal.get(Calendar.YEAR), 4) + "-"
			+ Util.add0(todaycal.get(Calendar.MONTH) + 1, 2) + "-"
			+ Util.add0(todaycal.get(Calendar.DAY_OF_MONTH), 2);

	// 每天凌晨1点执行一次0 0 1 * * ?
	// 通过继承BaseCronJob类可以实现定时同步
	public void execute() {
		try {
			create();
		} catch (Exception e) {
			lg.writeLog("创建流程异常e:" + e);
		}
	}

	private void create() {
		lg.writeLog("syndate----" + syndate);
		String sql = "";
		String planNum = "";
		sql = "SELECT COUNT(*) AS 'plan' FROM uf_crm_contract_dt1  WHERE limitdate='"
				+ syndate + "'";
		rs.executeSql(sql);
		if (rs.next()) {
			planNum = Util.null2String(rs.getString("plan"));
		}
		if (planNum.equals("0")) {
			lg.writeLog("今日未有收款计划！！！");
		} else {
			lg.writeLog("今日有收款计划！！！");
			// 费用名称
			List<String> planNameList = new ArrayList<String>();
			// 金额
			List<String> MoneyList = new ArrayList<String>();
			// 当年分摊金额
			List<String> currentMoneyList = new ArrayList<String>();
			// 合同名称
			List<String> contractNameList = new ArrayList<String>();
			// 申请人id
			String personId = "";
			// 部门
			String departMentid = "";

			// 业务id
			List<String> businessIdList = new ArrayList<String>();
			// 客户id
			List<String> customerIdList = new ArrayList<String>();
			// 单位名称
			List<String> unitnameList = new ArrayList<String>();
			// 统一社会信用代码
			List<String> creditcodeList = new ArrayList<String>();
			// 单位地址
			List<String> addressList = new ArrayList<String>();
			// 单位电话
			List<String> telList = new ArrayList<String>();
			// 开户银行名称
			List<String> accountnameList = new ArrayList<String>();
			// 开户银行账号
			List<String> accountnumberList = new ArrayList<String>();
			// 查询收款计划表中的业务经理
			sql = "SELECT id,departmentid  FROM HrmResource where ID IN (SELECT businessmanager FROM uf_crm_custbusiness WHERE customer in(SELECT distinct customer FROM uf_crm_contract a INNER JOIN uf_crm_contract_dt1 u ON a.id=u.mainid WHERE u.limitdate='"
					+ syndate
					+ "' ) and business in(SELECT distinct business FROM uf_crm_contract a INNER JOIN uf_crm_contract_dt1 u ON a.id=u.mainid WHERE u.limitdate='"
					+ syndate + "'))";
			rs.executeSql(sql);
			while (rs.next()) {
				personId = Util.null2String(rs.getString("id"));
				departMentid = Util.null2String(rs.getString("departmentid"));

				lg.writeLog("personId----" + personId);
				lg.writeLog("departMentid----" + departMentid);

				String customerId = "";
				String businessId = "";
				RecordSet rs2 = new RecordSet();
				sql = "SELECT * FROM uf_crm_contract a INNER JOIN uf_crm_contract_dt1 u ON a.id=u.mainid WHERE u.limitdate='"
						+ syndate
						+ "' AND customer in(select customer  from uf_crm_custbusiness where businessmanager='"
						+ personId
						+ "') AND business in(select business  from uf_crm_custbusiness where businessmanager='"
						+ personId + "' )";
				rs2.executeSql(sql);
				while (rs2.next()) {
					String planName = Util.null2String(rs2
							.getString("planname"));

					String Money = Util.null2String(rs2.getString("money"));

					String currentMoney = Util.null2String(rs2
							.getString("currentmoney"));

					businessId = Util.null2String(rs2.getString("business"));

					customerId = Util.null2String(rs2.getString("customer"));

					String contractName = Util.null2String(rs2
							.getString("contract"));

					planNameList.add(planName);

					MoneyList.add(Money);

					currentMoneyList.add(currentMoney);

					contractNameList.add(contractName);

					businessIdList.add(businessId);

					customerIdList.add(customerId);

					RecordSet rs1 = new RecordSet();
					sql = "SELECT * FROM uf_crm_customerinfo u INNER JOIN uf_crm_customerinfo_dt1 c ON u.id=c.mainid WHERE u.id='"
							+ customerId + "'";
					rs1.execute(sql);
					if (rs1.next()) {
						String unitname = Util.null2String(rs1
								.getString("name"));
						String creditcode = Util.null2String(rs1
								.getString("creditcode"));
						String address = Util.null2String(rs1
								.getString("address"));
						String tel = Util.null2String(rs1.getString("tel"));
						String accountname = Util.null2String(rs1
								.getString("accountname"));
						String accountnumber = Util.null2String(rs1
								.getString("accountnumber"));

						unitnameList.add(unitname);
						creditcodeList.add(creditcode);
						addressList.add(address);
						telList.add(tel);
						accountnameList.add(accountname);
						accountnumberList.add(accountnumber);
					}
				}

				int requestid = createPlan(planNameList, MoneyList,
						currentMoneyList, contractNameList, personId,
						departMentid, businessIdList, customerIdList,
						unitnameList, creditcodeList, addressList, telList,
						accountnameList, accountnumberList);

				// 信息提醒
				if (requestid > 0) {

					RecordSet rs3 = new RecordSet();
					String title = "今日，您有收款计划需处理";
					String status = "0";
					int modeId = 131; // 建模id
					UUID uuid = UUID.randomUUID();
					String wyid = uuid.toString();// 生成唯一的标识码

					// 插入变更记录
					sql = "insert into uf_crm_notice(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
							+ "title,business,customer,person,noticedate,status,uuid) values ("
							+ modeId
							+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"
							+ "'"
							+ title
							+ "','"
							+ businessId
							+ "','"
							+ customerId
							+ "','"
							+ personId
							+ "','"
							+ syndate
							+ "','" + status + "','" + wyid + "')";
					rs3.executeSql(sql);
					sql = "select id from uf_crm_notice where uuid='" + wyid
							+ "'";
					RecordSet rs4 = new RecordSet();
					rs4.executeSql(sql);
					if (rs4.next()) {
						String id = Util.null2String(rs4.getString("id"));// 查询出资产变更记录
						int logid = Integer.parseInt(id);
						ModeRightInfo ModeRightInfo = new ModeRightInfo();
						ModeRightInfo.editModeDataShare(5, modeId, logid);// 新建的时候添加共享-所有人
					}

					lg.writeLog("新增消息提醒sql:" + sql);
				}
			}
			lg.writeLog("开票流程触发结束");
		}
	}

	public int createPlan(List<String> planNameList, List<String> MoneyList,
			List<String> currentMoneyList, List<String> contractNameList,
			String personId, String departMentid, List<String> businessIdList,
			List<String> customerIdList, List<String> unitnameList,
			List<String> creditcodeList, List<String> addressList,
			List<String> telList, List<String> accountnameList,
			List<String> accountnumberList) {
		String newrequestid = "";
		try {
			RequestService requestService = new RequestService();
			RequestInfo info = new RequestInfo();
			info.setWorkflowid("446"); // 流程类型id
			info.setCreatorid(personId);// 创建人 id
			info.setDescription("开票申请");// 设置流程标题
			info.setRequestlevel("0");// 0 正常，1重要，2紧急
			info.setIsNextFlow("0");// 流转到下一节点
			MainTableInfo mainTableInfo = new MainTableInfo();
			List<Property> fields = new ArrayList<Property>();
			Property field = null;
			// 主表字段
			if (personId != "") {
				field = new Property();
				field.setName("creator");
				field.setValue(personId);
				fields.add(field);
			}
			if (departMentid != "") {
				field = new Property();
				field.setName("dept");
				field.setValue(departMentid);
				fields.add(field);
			}

			Property[] fieldarray = (Property[]) fields
					.toArray(new Property[fields.size()]);
			mainTableInfo.setProperty(fieldarray);
			info.setMainTableInfo(mainTableInfo);

			// 向明细表添加数据
			DetailTableInfo detailTableInfo = new DetailTableInfo();
			// 一张明细表
			DetailTable[] detailTables = new DetailTable[1];
			detailTables[0] = new DetailTable();
			detailTables[0].setId("1");
			// 明细表行
			Row[] row = new Row[planNameList.size()];
			for (int i = 0; i < 1; i++) {

				row[i] = new Row();
				row[i].setId(i + 1 + ""); // 必须设置
				// 一行中的单元格
				Cell[] cell = new Cell[13];

				int num = 0;
				// 对每个单元格进行设置
				if (customerIdList.get(i) != "") {
					cell[num] = new Cell();
					cell[num].setName("customer");
					cell[num].setValue(customerIdList.get(i));
				}

				if (businessIdList.get(i) != "") {
					cell[num + 1] = new Cell();
					cell[num + 1].setName("business");
					cell[num + 1].setValue(businessIdList.get(i));
				}

				if (contractNameList.get(i) != "") {
					cell[num + 2] = new Cell();
					cell[num + 2].setName("contract");
					cell[num + 2].setValue(contractNameList.get(i));
				}

				if (planNameList.get(i) != "") {
					cell[num + 3] = new Cell();
					cell[num + 3].setName("planname");
					cell[num + 3].setValue(planNameList.get(i));
				}

				if (MoneyList.get(i) != "") {
					cell[num + 4] = new Cell();
					cell[num + 4].setName("invoicemoney");
					cell[num + 4].setValue(MoneyList.get(i));
				}

				if (currentMoneyList.get(i) != "") {
					cell[num + 5] = new Cell();
					cell[num + 5].setName("currentmoney");
					cell[num + 5].setValue(currentMoneyList.get(i));
				}

				if (unitnameList.get(i) != "") {
					cell[num + 6] = new Cell();
					cell[num + 6].setName("unitname");
					cell[num + 6].setValue(unitnameList.get(i));
				}

				if (creditcodeList.get(i) != "") {
					cell[num + 7] = new Cell();
					cell[num + 7].setName("creditcode");
					cell[num + 7].setValue(creditcodeList.get(i));
				}

				if (addressList.get(i) != "") {
					cell[num + 8] = new Cell();
					cell[num + 8].setName("address");
					cell[num + 8].setValue(addressList.get(i));
				}

				if (telList.get(i) != "") {
					cell[num + 9] = new Cell();
					cell[num + 9].setName("tel");
					cell[num + 9].setValue(telList.get(i));
				}

				if (accountnameList.get(i) != "") {
					cell[num + 10] = new Cell();
					cell[num + 10].setName("accountname");
					cell[num + 10].setValue(accountnameList.get(i));
				}

				if (accountnumberList.get(i) != "") {
					cell[num + 11] = new Cell();
					cell[num + 11].setName("accountnumber");
					cell[num + 11].setValue(accountnumberList.get(i));
				}

				cell[num + 12] = new Cell();
				cell[num + 12].setName("invoicetype");
				cell[num + 12].setValue("0");

				row[i].setCell(cell);
			}
			detailTables[0].setRow(row);
			detailTableInfo.setDetailTable(detailTables);
			info.setDetailTableInfo(detailTableInfo);

			newrequestid = requestService.createRequest(info);
			lg.writeLog("创建流程成功!!!");
		} catch (Exception e) {
			lg.writeLog("创建流程异常:" + e);
		}

		return Util.getIntValue(newrequestid, 0);
	}
}
