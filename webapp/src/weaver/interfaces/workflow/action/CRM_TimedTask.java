package weaver.interfaces.workflow.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.axiom.util.blob.WritableBlob;

import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
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
 * CRM定时任务
 * 
 * @author lsq
 * @date 2019/6/25
 * @version 1.0
 */
public class CRM_TimedTask extends BaseCronJob {
	BaseBean lg = new BaseBean();
	SelectSYFJ sc = new SelectSYFJ();
	Calendar todaycal = Calendar.getInstance();
	String syndate = Util.add0(todaycal.get(Calendar.YEAR), 4) + "-"
			+ Util.add0(todaycal.get(Calendar.MONTH) + 1, 2) + "-"
			+ Util.add0(todaycal.get(Calendar.DAY_OF_MONTH), 2);

	// 通过继承BaseCronJob类可以实现定时同步
	// 0 0 1 1/1 * ? 每天凌晨一点执行一次
	public void execute() {
		try {
			deleNotice();
			create1();
			//create2();
			// create3();
			create4();
			
			create5();

			create6();

		} catch (Exception e) {
		}
	}

	/**
	 * 删除提醒记录
	 */
	public void deleNotice() {
		RecordSet rs = new RecordSet();
		String sql = "delete from  uf_crm_notice";
		rs.executeSql(sql);
	}

	/**
	 * 合同到期
	 */
	private String create1() {
		try {
			String sqlcon = "select id from uf_crm_contract where enddate<convert(varchar(10),GETDATE(),120) " +
					"and status='0' and (contracttype='0' or contracttype='1') and extendcycle!='4' " +
					"and (extendcycle is not null and startdate is not null and enddate is not null)";
			RecordSet rss = new RecordSet();
			RecordSet rs = new RecordSet();
			RecordSet rs1 = new RecordSet();
			RecordSet rs2 = new RecordSet();
			RecordSet rs3 = new RecordSet();
			rss.executeSql(sqlcon);
			while (rss.next()) {
				String id = Util.null2String(rss.getString("id"));
				create(id); // 调用自动顺延的方法
			}
			// 对存在合同库 不在许可库 的有效合同 新增 许可卡片
			String sqlcrea = "select distinct customer,business  from uf_crm_contract where " +
					"not exists(select 1 from uf_crm_permitpub where customer=uf_crm_contract.customer and business=uf_crm_contract.business)" +
					" and status='0' and business in(1,2,3,4,5,6)";
			rs1.executeSql(sqlcrea);
			while (rs1.next()) {
				String customercrea = Util.null2String(rs1
						.getString("customer"));
				String businesscrea = Util.null2String(rs1
						.getString("business"));

				int modeId = 112; // 建模id
				UUID uuid = UUID.randomUUID();
				String wyid = uuid.toString();// 生成唯一的标识码
				String sql = "insert into uf_crm_permitpub(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
						+ "business,customer,status,uuid) values ("
						+ modeId
						+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"
						+ "'"
						+ businesscrea
						+ "','"
						+ customercrea
						+ "','0','"
						+ wyid + "')";
				rs3.executeSql(sql);

				sql = "select id from uf_crm_permitpub where uuid='" + wyid
						+ "'";
				rs2.executeSql(sql);
				if (rs2.next()) {
					String id = Util.null2String(rs2.getString("id"));
					int logid = Integer.parseInt(id);
					ModeRightInfo ModeRightInfo = new ModeRightInfo();
					ModeRightInfo.editModeDataShare(5, modeId, logid);
				}

			}

			
			String sql = "select uuid,customer,business,specialset from uf_crm_permitpub where business in(1,2,3,4,5,6)";
			rs.executeSql(sql);
			while (rs.next()) {
				// uuid
				String uuid = Util.null2String(rs.getString("uuid"));
				// 客户
				String customer = Util.null2String(rs.getString("customer"));
				// 业务
				String businessid = Util.null2String(rs.getString("business"));
				// 特殊字符
				String specialset = Util
						.null2String(rs.getString("specialset"));
				RecordSet rstype = new RecordSet();
				if (specialset.equals("0")) {
					sql = "update uf_crm_permitpub set status='0' where uuid='"
							+ uuid + "'";

					rstype.executeSql(sql);
				} else {
					String flag = "";
					sql = "select count(1) as flag from uf_crm_contract where "
							+ "startdate <=convert(varchar(10),GETDATE(),120) "
							+ "and enddate>=convert(varchar(10),GETDATE(),120) and status='0'"
							+ "and customer='" + customer + "' and business='"
							+ businessid + "'";
					RecordSet rscon = new RecordSet();
					rscon.executeSql(sql);
					if (rscon.next()) {
						flag = Util.null2String(rscon.getString("flag"));
					}
					RecordSet rsva = new RecordSet();
					if (flag.equals("0")) {
						sql = "update uf_crm_permitpub set status='1' where customer='"
								+ customer
								+ "'and business='"
								+ businessid
								+ "'";
					} else {
						sql = "update uf_crm_permitpub set status='0' where customer='"
								+ customer
								+ "' and business='"
								+ businessid
								+ "'";
					}
					rsva.executeSql(sql);
				}
			}
		} catch (Exception e) {
			lg.writeLog("检索出现异常e---" + e);
		}
		lg.writeLog("合同到期执行完毕");
		return "1";
	}

	/**
	 * 自动顺延
	 */
	public void create(String id) {
		try {
			RecordSet rs1 = new RecordSet();
			RecordSet rs2 = new RecordSet();
			RecordSet rs3 = new RecordSet();
			RecordSet rs4 = new RecordSet();
			RecordSet rs5 = new RecordSet();
			RecordSet rs6 = new RecordSet();
			RecordSet rs7 = new RecordSet();
			int modeId = 112; // 建模id
			String sql1 = "";
			String sql2 = "";
			String sql3="";
			String sql4="";
			String sql5="";
			String sql6="";
			String sql7="";

			sql2 = "select extendtype,extendcycle from uf_crm_contract where id='"
					+ id + "'";
			rs2.executeSql(sql2);
			if (rs2.next()) {
				String extendtype = Util.null2String(rs2
						.getString("extendtype"));// 延展方式
				String extendcycle = Util.null2String(rs2
						.getString("extendcycle"));// 周期

				if (extendtype.equals("1")) {// 自动顺延
					
					UUID uuid = UUID.randomUUID();
					String wyid = uuid.toString();// 生成唯一的标识码
					// 复制合同记录
					sql3 = "insert into uf_crm_contract(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
							+ "customer,business,contractname,contractno,contracttype,contractrelation,extendtype,extendcycle"
							+ ",formcontract,contract,iffixed,contractmoney,finalizedfile,contractperiod,startdate,enddate,signdate,contractfile,expressfile,remark,status,flowid,uuid) "
							+ "select "
							+ modeId
							+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"
							+ "customer,business,contractname,contractno,contracttype,contractrelation,extendtype,extendcycle "
							+ ",formcontract,'"
							+ id
							+ "',iffixed,contractmoney,finalizedfile,contractperiod,convert(varchar(10),DATEADD(DAY,1,enddate),120),enddate,signdate,contractfile,expressfile,remark,0,'"
							+ id
							+ "','"
							+ wyid
							+ "'"
							+ " from uf_crm_contract where id='" + id + "' ";
					lg.writeLog("------------自动顺延合同复制合同sq-------------" + sql3);
					rs3.executeSql(sql3);
					sql4 = "select id from uf_crm_contract where uuid='" + wyid
							+ "'";
					rs4.executeSql(sql4);
					if (rs4.next()) {
						String ids = Util.null2String(rs4.getString("id"));// 查询合同id
						int logid = Integer.parseInt(ids);
						ModeRightInfo ModeRightInfo = new ModeRightInfo();
						ModeRightInfo.editModeDataShare(5, modeId, logid);// 新建的时候添加共享-所有人

						// 复制收款计划
						sql5 = "insert into uf_crm_contract_dt1(mainid,money,usernumber,limitdate,cycle,currentmoney,remark) "
								+ " select "
								+ logid
								+ ",money,usernumber,limitdate,cycle,currentmoney,remark "
								+ " from uf_crm_contract_dt1 where  mainid="
								+ id;
						lg.writeLog("---------------合同计划明细---------------"+sql5);
						rs5.executeSql(sql5);  
						if (extendcycle.equals("0")) {// 每年
							sql6 = "update uf_crm_contract set  enddate=convert(varchar(10),DATEADD(MONTH,12,enddate),120) where id='"
									+ logid + "'";
							sql7 = "update uf_crm_contract_dt1 set limitdate=convert(varchar(10),DATEADD(MONTH,12,limitdate),120) where mainid='"
									+ logid + "'";
							
							lg.writeLog("----------周期每年--------");

						} else if (extendcycle.equals("1")) {// 每半年
							sql6 = "update uf_crm_contract set enddate=convert(varchar(10),DATEADD(MONTH,6,enddate),120) where id='"
									+ logid + "'";
							sql7 = "update uf_crm_contract_dt1 set limitdate=convert(varchar(10),DATEADD(MONTH,6,limitdate),120) where mainid='"
									+ logid + "'";
							lg.writeLog("----------周期每半年--------");
						} else if (extendcycle.equals("2")) {// 每季度
							sql6 = "update uf_crm_contract set enddate=convert(varchar(10),DATEADD(MONTH,3,enddate),120) where id='"
									+ logid + "'";
							sql7 = "update uf_crm_contract_dt1 set limitdate=convert(varchar(10),DATEADD(MONTH,3,limitdate),120) where mainid='"
									+ logid + "'";
							lg.writeLog("----------周期每季度--------");
						} else if (extendcycle.equals("3")) {// 每月
							sql6 = "update uf_crm_contract set enddate=convert(varchar(10),DATEADD(MONTH,1,enddate),120) where id='"
									+ logid + "'";
							sql7 = "update uf_crm_contract_dt1 set limitdate=convert(varchar(10),DATEADD(MONTH,1,limitdate),120) where mainid='"
									+ logid + "'";
							lg.writeLog("----------周期每月--------");
						} else if (extendcycle.equals("4")) {// 无
							lg.writeLog("----------自动顺延合同--无周期-----------");
						} else if (extendcycle.equals("5")) { // 每三年
							sql6 = "update uf_crm_contract set enddate=convert(varchar(10),DATEADD(MONTH,36,enddate),120) where id='"
									+ logid + "'";
							sql7 = "update uf_crm_contract_dt1 set limitdate=convert(varchar(10),DATEADD(MONTH,36,limitdate),120) where mainid='"
									+ logid + "'";
							lg.writeLog("----------周期每三年--------");
						}
						rs6.executeSql(sql6);
						rs7.executeSql(sql7);
					}

				} else {
					lg.writeLog("自动顺延合同--不是自动顺延合同");
				}
				// 更新原合同为终止
				sql1 = "update uf_crm_contract set status='1' where id='" + id
						+ "'";
				lg.writeLog("更新原合同为终止sql=" + sql1);
				rs1.executeSql(sql1);
			}

			// }
			lg.writeLog("自动顺延合同--结束");
			// }

		} catch (Exception e) {
			lg.writeLog("自动顺延合同--出错" + e);
		}
		lg.writeLog("自动顺延执行完毕");
	}

	/**
	 * 许可公示
	 */
	private void create2() {
		try {
			RecordSetDataSource exchangeDB = new RecordSetDataSource(
					"exchangeDB");
			RecordSetDataSource exchangeDBs = new RecordSetDataSource(
					"exchangeDB");
			RecordSetDataSource eDB = new RecordSetDataSource("exchangeDB");
			RecordSet rs = new RecordSet();
			String sql = "select  customer,business,startdate,enddate,status,dsporder,uuid,syid,gsid from uf_crm_permitpub where business in(1,2,3,4,5,6)";
			rs.executeSql(sql);
			while (rs.next()) {
				String customer = Util.null2String(rs.getString("customer"));
				String businessid = Util.null2String(rs.getString("business"));
				String startdate = Util.null2String(rs.getString("startdate"));
				String enddate = Util.null2String(rs.getString("enddate"));
				String status = Util.null2String(rs.getString("status"));
				String dsporder = Util.null2String(rs.getString("dsporder"));
				String uuid = Util.null2String(rs.getString("uuid"));
				String syid = Util.null2String(rs.getString("syid"));
				String gsid = Util.null2String(rs.getString("gsid"));
				// 中间表许可期限
				String deadLine = startdate + "~" + enddate;

				RecordSet rscompany = new RecordSet();
				rscompany
						.executeSql("select name,creditcode from uf_CRM_CustomerInfo where id='"
								+ customer + "'");
				String organizationCode = "";
				String companyName = "";
				if (rscompany.next()) {
					// 公司名称
					companyName = Util.null2String(rscompany.getString("name"));
					// 组织机构代码
					organizationCode = Util.null2String(rscompany
							.getString("creditcode"));
				}
				SimpleDateFormat dateFormat_now = new SimpleDateFormat(
						"yyyy/MM/dd");
				String tcsj = dateFormat_now.format(new Date());
				String count = "";
				if (businessid.equals("1")) {
					exchangeDB
							.executeSql("select count(*) as count from sync_website_lv1_permit_list where id='"
									+ uuid + "'");
				} else if (businessid.equals("2")) {
					exchangeDB
							.executeSql("select count(*) as count from sync_website_opt_permit_list where id='"
									+ uuid + "'");
				} else if (businessid.equals("3")) {
					exchangeDB
							.executeSql("select count(*) as count from SYNC_FIXEDINCOME_PERMIT_LIST where id='"
									+ uuid + "'");
				} else if (businessid.equals("4")) {
					exchangeDB
							.executeSql("select count(*) as count from sync_website_sse_permit_list where id='"
									+ uuid + "'");
				} else if (businessid.equals("5")) {
					exchangeDB
							.executeSql("select count(*) as count from sync_website_lv2_permit_list where id='"
									+ uuid + "'");
				} else if (businessid.equals("6")) {
					exchangeDB
							.executeSql("select count(*) as count from SYNC_LV2DATAFEED_PERMIT_LIST where id='"
									+ uuid + "'");
				}
				if (exchangeDB.next()) {
					count = Util.null2String(exchangeDB.getString("count"));
				}
				if (count.equals("0") && uuid.length() > 0) {
					if (businessid.equals("1")) { // Level-1行情许可
						sql = "insert into sync_website_lv1_permit_list(id,organization_code,company_name,upload_time,deadline,status,company_id,showorder)  values('"
								+ uuid
								+ "','"
								+ organizationCode
								+ "','"
								+ companyName
								+ "',to_date('"
								+ tcsj
								+ "','yyyy/MM/dd'),'"
								+ deadLine
								+ "','"
								+ status
								+ "','"
								+ gsid
								+ "','"
								+ dsporder
								+ "')";
					} else if (businessid.equals("2")) {// 期权行情展示许可
						sql = " insert into sync_website_opt_permit_list "
								+ " (ID,ORGANIZATION_CODE,COMPANY_NAME,UPLOAD_TIME,DEADLINE,STATUS,COMPANY_ID,SHOWORDER) "
								+ " VALUES " + " ('" + uuid + "','"
								+ organizationCode + "','" + companyName
								+ "',to_date('" + tcsj + "','yyyy/MM/dd'),'"
								+ deadLine + "','" + status + "','" + gsid
								+ "','" + dsporder + "') ";
					} else if (businessid.equals("3")) {// 固定收益行情许可

						sql = "insert into SYNC_FIXEDINCOME_PERMIT_LIST  (id,ORGANIZATION_CODE,COMPANY_ID,COMPANY_NAME,DEADLINE,STATUS,SHOWORDER)  VALUES  ('"
								+ uuid
								+ "' , '"
								+ organizationCode
								+ "' , '"
								+ gsid
								+ "' , '"
								+ companyName
								+ "' ,  '"
								+ deadLine
								+ "' , '"
								+ status
								+ "' , '"
								+ dsporder + "' ) ";

					} else if (businessid.equals("4")) {// 指数授权
						sql = "insert into sync_website_sse_permit_list (ID,ORGANIZATION_CODE,COMPANY_NAME,DEADLINE,UPLOAD_TIME,STATUS,company_id,SHOWORDER) values('"
								+ uuid
								+ "','"
								+ organizationCode
								+ "','"
								+ companyName
								+ "','"
								+ deadLine
								+ "',to_date('"
								+ tcsj
								+ "','yyyy/MM/dd'),'"
								+ status
								+ "','"
								+ gsid
								+ "','"
								+ dsporder
								+ "')";
					} else if (businessid.equals("5")) {// Level-2行情展示许可

						sql = "insert into sync_website_lv2_permit_list(id,organization_code,company_name,upload_time,permission_deadline,status,company_id,showorder)  values('"
								+ uuid
								+ "','"
								+ organizationCode
								+ "','"
								+ companyName
								+ "',to_date('"
								+ tcsj
								+ "','yyyy/MM/dd'),'"
								+ deadLine
								+ "','"
								+ status
								+ "','"
								+ gsid
								+ "','"
								+ dsporder
								+ "')";
					} else if (businessid.equals("6")) {// 或Level-2行情非展示许可
						sql = "insert into SYNC_LV2DATAFEED_PERMIT_LIST "
								+ " (id,ORGANIZATION_CODE,COMPANY_NAME ,permission_deadline,STATUS,SHOWORDER) "
								+ " VALUES " + " ('" + uuid + "' , '"
								+ organizationCode + "' , '" + companyName
								+ "' ,'" + deadLine + "' ,'" + status + "' , '"
								+ dsporder + "' ) ";
					}
					try {
						exchangeDBs.executeSql(sql);
					} catch (Exception e) {
						lg.writeLog("中间库插入出错e----" + e);
					}

				} else { // 中间库存在 ,则更新 中间库的名称
					String sqls = "";
					if (businessid.equals("1")) {
						sqls = "update sync_website_lv1_permit_list set company_name='"
								+ companyName
								+ "',status='"
								+ status
								+ "' where id='" + uuid + "'";
					} else if (businessid.equals("2")) {
						sqls = "update sync_website_opt_permit_list set company_name='"
								+ companyName
								+ "',status='"
								+ status
								+ "' where id='" + uuid + "'";
					} else if (businessid.equals("3")) {
						sqls = " update SYNC_FIXEDINCOME_PERMIT_LIST set company_name='"
								+ companyName
								+ "',status='"
								+ status
								+ "' where id = '" + uuid + "'";
					} else if (businessid.equals("4")) {
						sqls = "update sync_website_sse_permit_list set company_name='"
								+ companyName
								+ "',status='"
								+ status
								+ "' where id='" + uuid + "'";

					} else if (businessid.equals("5")) {
						sqls = "update sync_website_lv2_permit_list set company_name='"
								+ companyName
								+ "',status='"
								+ status
								+ "' where id='" + uuid + "'";
					} else if (businessid.equals("6")) {
						sqls = "update SYNC_LV2DATAFEED_PERMIT_LIST set company_name='"
								+ companyName
								+ "',status='"
								+ status
								+ "' where id='" + uuid + "'";
					}

					// 更新中间表
					try {
						eDB.executeSql(sqls);
						lg.writeLog("更新成功----" + sqls);
					} catch (Exception e) {
						lg.writeLog("中间库更新异常e" + e);
					}

				}

			}

		} catch (Exception e) {
		}

	}

	/**
	 * 收款计划
	 */
	private void create3() {
		String sql = "";
		String planNum = "";
		sql = "SELECT COUNT(*) AS 'plan' FROM uf_crm_contract_dt1  WHERE limitdate='"
				+ syndate + "'";
		RecordSet rs = new RecordSet();
		rs.executeSql(sql);
		if (rs.next()) {
			planNum = Util.null2String(rs.getString("plan"));
		}
		if (planNum.equals("0")) {
			lg.writeLog("今日未有收款计划！！！");
		} else {
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

				/*
				 * int requestid = createPlan(planNameList, MoneyList,
				 * currentMoneyList, contractNameList, personId, departMentid,
				 * businessIdList, customerIdList, unitnameList, creditcodeList,
				 * addressList, telList, accountnameList, accountnumberList);
				 */

				// 信息提醒
				// if (requestid > 0) {

				RecordSet rs3 = new RecordSet();
				String title = "收款计划提醒";
				String status = "0";
				int modeId = 134; // 建模id
				UUID uuid = UUID.randomUUID();
				String wyid = uuid.toString();// 生成唯一的标识码
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
						+ "','"
						+ status
						+ "','" + wyid + "')";
				rs3.executeSql(sql);
				sql = "select id from uf_crm_notice where uuid='" + wyid + "'";
				RecordSet rs4 = new RecordSet();
				rs4.executeSql(sql);
				if (rs4.next()) {
					String id = Util.null2String(rs4.getString("id"));
					int logid = Integer.parseInt(id);
					ModeRightInfo ModeRightInfo = new ModeRightInfo();
					ModeRightInfo.editModeDataShare(5, modeId, logid);
				}

				lg.writeLog("新增消息提醒sql:" + sql);
				// }
			}
			lg.writeLog("开票流程触发结束");
		}
		lg.writeLog("收款计划执行完毕");
	}

	/**
	 * 创建开票流程
	 */
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

	/**
	 * 检测权限
	 */
	public void create4() {
		RecordSet rs = new RecordSet();
		RecordSet rs1 = new RecordSet();
		RecordSet rs2 = new RecordSet();
		RecordSet rs3 = new RecordSet();
		String sql = "select customer,business,reminddate,enddate from uf_crm_permission where status='0' and (case when reminddate is null or reminddate='' then enddate else reminddate end)<=convert(varchar(10),GETDATE(),120) and ((reminddate is not null and reminddate!='') or (enddate is not null and enddate!=''))";
		rs.executeSql(sql);
		while (rs.next()) {

			String customer = Util.null2String(rs.getString("customer")); // 客户
			String business = Util.null2String(rs.getString("business")); // 业务
			String businessmanager = ""; // 业务经理
			sql = "select businessmanager from uf_crm_custbusiness where customer='"
					+ customer + "' and business='" + business + "'";
			rs1.executeSql(sql);
			if (rs1.next()) {
				businessmanager = Util.null2String(rs1
						.getString("businessmanager"));
			}
			String title = "权限到期提醒";
			String status = "0";
			int modeId = 134; // 建模id
			UUID uuid = UUID.randomUUID();
			String wyid = uuid.toString();// 生成唯一的标识码
			sql = "insert into uf_crm_notice(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
					+ "title,business,customer,person,noticedate,status,uuid) values ("
					+ modeId
					+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"
					+ "'"
					+ title
					+ "','"
					+ business
					+ "','"
					+ customer
					+ "','"
					+ businessmanager
					+ "','"
					+ syndate
					+ "','"
					+ status
					+ "','" + wyid + "')";
			rs2.executeSql(sql);

			sql = "select id from uf_crm_notice where uuid='" + wyid + "'";
			rs3.executeSql(sql);
			if (rs3.next()) {
				String id = Util.null2String(rs3.getString("id"));
				int logid = Integer.parseInt(id);
				ModeRightInfo ModeRightInfo = new ModeRightInfo();
				ModeRightInfo.editModeDataShare(5, modeId, logid);
			}

		}
		lg.writeLog("检测权限执行完毕");
	}

	/**
	 * 月走访计划
	 */
	private void create5() {
		RecordSet rs = new RecordSet();
		RecordSet rs1 = new RecordSet();
		RecordSet rs2 = new RecordSet();
		RecordSet rs3 = new RecordSet();
		RecordSet rs4 = new RecordSet();
		RecordSet rs5 = new RecordSet();
		RecordSet rs6 = new RecordSet();
		RecordSet rs7 = new RecordSet();
		RecordSet rs8 = new RecordSet();
		String year = Util.add0(todaycal.get(Calendar.YEAR), 4); // 当前年
		String month = Util.add0(todaycal.get(Calendar.MONTH) + 1, 1); // 当前月
		String sqlyear = "select selectvalue,selectname from workflow_SelectItem where fieldid='11946' and selectvalue>7 order by selectvalue";
		String sqlmonth = "select selectvalue,selectname from workflow_SelectItem where fieldid='11947' order by selectvalue";
		String yselectvalue = "";
		String yselectname = "";
		String mselectvalue = "";
		String mselectname = "";
		rs7.executeSql(sqlyear);
		while (rs7.next()) {
			yselectvalue = Util.null2String(rs7.getString("selectvalue"));
			yselectname = Util.null2String(rs7.getString("selectname"));
			if (yselectname.equals(year)) {
				break;
			}
		}
		rs8.executeSql(sqlmonth);
		while (rs8.next()) {
			mselectvalue = Util.null2String(rs8.getString("selectvalue"));
			mselectname = Util.null2String(rs8.getString("selectname"));
			if (month.equals(mselectname)) {
				break;
			}
		}
		String sqlnotice = "select count(*) AS 'flag' from uf_crm_visitplan where visityear='"
				+ yselectvalue + "' and visitmonth='" + mselectvalue + "'"; // 检测走访表
		rs3.execute(sqlnotice);
		lg.writeLog("sqlnotice:" + sqlnotice);
		String flag = "";
		if (rs3.next()) {
			flag = Util.null2String(rs3.getString("flag"));
		}
		if (flag.equals("0")) { // 走访表不存在下月走访数据 ,则提醒所有的客户经理
			String sql = "SELECT  distinct  custommanager FROM  Matrixtable_7 WHERE custommanager !=''";
			rs.executeSql(sql);
			while (rs.next()) {
				String title = "创建"+month+"月走访计划提醒";
				String status = "0";
				String custommanager = Util.null2String(rs
						.getString("custommanager"));
				String[] strarr = custommanager.split(",");
				for (int i = 0; i < strarr.length; i++) {
					sql = "SELECT count(*) AS 'count' FROM uf_crm_notice WHERE title='"
							+ title
							+ "' AND  person='"
							+ strarr[i]
							+ "' AND noticedate='" + syndate + "'";
					rs4.executeSql(sql);
					String count = "";
					if (rs4.next()) {
						count = Util.null2String(rs4.getString("count"));
					}
					if (count.equals("0")) {

						int modeId = 134; // 建模id
						UUID uuid = UUID.randomUUID();
						String wyid = uuid.toString();// 生成唯一的标识码
						sql = "insert into uf_crm_notice(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
								+ "title,business,customer,person,noticedate,status,uuid) values ("
								+ modeId
								+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"
								+ "'"
								+ title
								+ "',null,null,'"
								+ strarr[i]
								+ "','"
								+ syndate
								+ "','"
								+ status
								+ "','"
								+ wyid + "')";
						rs1.executeSql(sql);
						sql = "select id from uf_crm_notice where uuid='"
								+ wyid + "'";
						rs2.executeSql(sql);
						if (rs2.next()) {
							String id = Util.null2String(rs2.getString("id"));
							int logid = Integer.parseInt(id);
							ModeRightInfo ModeRightInfo = new ModeRightInfo();
							ModeRightInfo.editModeDataShare(5, modeId, logid);
						}

					}
				}
			}
		} else {
			sqlnotice = "select customer,business from uf_crm_visitplan where visityear='"
					+ yselectvalue + "' and visitmonth='" + mselectvalue + "'";
			rs5.executeSql(sqlnotice);
			List<String> accountmanager = new ArrayList<String>();
			while (rs5.next()) {
				String custoemr = Util.null2String(rs5.getString("customer"));
				String business = Util.null2String(rs5.getString("business"));
				String sqlmana = "select accountmanager from uf_crm_custbusiness where customer='"
						+ custoemr + "' and business='" + business + "'";
				rs6.executeSql(sqlmana);
				if (rs6.next()) {
					accountmanager.add(Util.null2String(rs6
							.getString("accountmanager")));
				}

			}
			String sql = "SELECT  distinct  custommanager FROM  Matrixtable_7 WHERE custommanager !=''";
			rs.executeSql(sql);
			while (rs.next()) {
				String title = "创建"+month+"月走访计划";
				String status = "0";
				String custommanager = Util.null2String(rs
						.getString("custommanager"));
				String[] strarr = custommanager.split(",");
				for (int i = 0; i < strarr.length; i++) {
					String num = "0";
					for (int j = 0; j < accountmanager.size(); j++) {
						if (strarr[i].equals(accountmanager.get(j))) {
							num = "1";
						}
					}
					if (num.equals("0")) { // 该客户经理没有提前发布 下月走访计划
						sql = "SELECT count(*) AS 'count' FROM uf_crm_notice WHERE title='"
								+ title
								+ "' AND  person='"
								+ strarr[i]
								+ "' AND noticedate='" + syndate + "'";
						rs4.executeSql(sql);
						String count = "";
						if (rs4.next()) {
							count = Util.null2String(rs4.getString("count"));
						}
						if (count.equals("0")) {
							int modeId = 134; // 建模id
							UUID uuid = UUID.randomUUID();
							String wyid = uuid.toString();// 生成唯一的标识码
							sql = "insert into uf_crm_notice(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
									+ "title,business,customer,person,noticedate,status,uuid) values ("
									+ modeId
									+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"
									+ "'"
									+ title
									+ "',null,null,'"
									+ strarr[i]
									+ "','"
									+ syndate
									+ "','"
									+ status
									+ "','"
									+ wyid + "')";
							rs1.executeSql(sql);
							sql = "select id from uf_crm_notice where uuid='"
									+ wyid + "'";
							rs2.executeSql(sql);
							if (rs2.next()) {
								String id = Util.null2String(rs2
										.getString("id"));
								int logid = Integer.parseInt(id);
								ModeRightInfo ModeRightInfo = new ModeRightInfo();
								ModeRightInfo.editModeDataShare(5, modeId,
										logid);
							}

						}
					}
				}
			}
		}
	}

	/**
	 * 年收入计划
	 */
	private void create6() {
		RecordSet rs = new RecordSet();
		RecordSet rs1 = new RecordSet();
		RecordSet rs2 = new RecordSet();
		RecordSet rs3 = new RecordSet();
		RecordSet rs4 = new RecordSet();
		RecordSet rs5 = new RecordSet();
		RecordSet rs7 = new RecordSet();
		String year = Util.add0(todaycal.get(Calendar.YEAR), 4); // 当前年
		if(!year.equals("2019")){    //当前年不为2019  ,即从2020年开始执行
			String sqlyear = "select selectvalue,selectname from workflow_SelectItem where fieldid='11946' and selectvalue>7 order by selectvalue";
			String yselectvalue = "";
			String yselectname = "";
			rs7.executeSql(sqlyear);
			while (rs7.next()) {
				yselectvalue = Util.null2String(rs7.getString("selectvalue"));
				yselectname = Util.null2String(rs7.getString("selectname"));
				if (yselectname.equals(year)) {
					break;
				}
			}
			String sql3 = "select count(*) AS 'flag' from uf_crm_incomplanadd where annual='"
					+ yselectvalue + "'";
			rs3.executeSql(sql3);
			String flag = "";
			if (rs3.next()) {
				flag = Util.null2String(rs3.getString("flag"));
			}
			if (flag.equals("0")) {
				String sql = "SELECT  distinct  custommanager FROM  Matrixtable_7 WHERE custommanager !=''";
				rs.executeSql(sql);
				while (rs.next()) {
					String title = "创建"+year+"年度收入计划提醒";
					String status = "0";
					String custommanager = Util.null2String(rs
							.getString("custommanager"));
					String[] strarr = custommanager.split(",");
					for (int i = 0; i < strarr.length; i++) {

						sql = "SELECT count(*) AS 'count' FROM uf_crm_notice WHERE title='"
								+ title
								+ "' AND  person='"
								+ strarr[i]
								+ "' AND noticedate='" + syndate + "'";
						rs4.executeSql(sql);
						String count = "";
						if (rs4.next()) {
							count = Util.null2String(rs4.getString("count"));
						}
						if (count.equals("0")) {
							int modeId = 134; // 建模id
							UUID uuid = UUID.randomUUID();
							String wyid = uuid.toString();// 生成唯一的标识码
							sql = "insert into uf_crm_notice(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
									+ "title,business,customer,person,noticedate,status,uuid) values ("
									+ modeId
									+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"
									+ "'"
									+ title
									+ "',null,null,'"
									+ strarr[i]
									+ "','"
									+ syndate
									+ "','"
									+ status
									+ "','"
									+ wyid + "')";
							rs1.executeSql(sql);

							sql = "select id from uf_crm_notice where uuid='"
									+ wyid + "'";
							rs2.executeSql(sql);
							if (rs2.next()) {
								String id = Util.null2String(rs2.getString("id"));
								int logid = Integer.parseInt(id);
								ModeRightInfo ModeRightInfo = new ModeRightInfo();
								ModeRightInfo.editModeDataShare(5, modeId, logid);
							}

						}
					}

				}
			} else {
				List<String> list = new ArrayList<String>();
				String sql5 = "select creator from uf_crm_incomplanadd where annual='"
						+ yselectvalue + "'";
				rs5.executeSql(sql5);
				while (rs5.next()) {
					list.add(Util.null2String(rs5.getString("creator")));
				}
				String sql = "SELECT  distinct  custommanager FROM  Matrixtable_7 WHERE custommanager !=''";
				rs.executeSql(sql);
				while (rs.next()) {
					String title = "创建"+year+"年收入分月计划";
					String status = "0";
					String custommanager = Util.null2String(rs
							.getString("custommanager"));
					String[] strarr = custommanager.split(",");
					for (int i = 0; i < strarr.length; i++) {
						String num = "0";
						for (int j = 0; j < list.size(); j++) {
							if (strarr[i].equals(list.get(j))) {
								num = "1";
							}
						}
						if (num.equals("0")) {
							sql = "SELECT count(*) AS 'count' FROM uf_crm_notice WHERE title='"
									+ title
									+ "' AND  person='"
									+ strarr[i]
									+ "' AND noticedate='" + syndate + "'";
							rs4.executeSql(sql);
							String count = "";
							if (rs4.next()) {
								count = Util.null2String(rs4.getString("count"));
							}
							if (count.equals("0")) {
								int modeId = 134; // 建模id
								UUID uuid = UUID.randomUUID();
								String wyid = uuid.toString();// 生成唯一的标识码
								sql = "insert into uf_crm_notice(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
										+ "title,business,customer,person,noticedate,status,uuid) values ("
										+ modeId
										+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"
										+ "'"
										+ title
										+ "',null,null,'"
										+ strarr[i]
										+ "','"
										+ syndate
										+ "','"
										+ status
										+ "','"
										+ wyid + "')";
								rs1.executeSql(sql);
								sql = "select id from uf_crm_notice where uuid='"
										+ wyid + "'";
								rs2.executeSql(sql);
								if (rs2.next()) {
									String id = Util.null2String(rs2
											.getString("id"));
									int logid = Integer.parseInt(id);
									ModeRightInfo ModeRightInfo = new ModeRightInfo();
									ModeRightInfo.editModeDataShare(5, modeId,
											logid);
								}

							}
						}

					}

				}
			}
		}else{
			lg.writeLog("年度收入计划从2020年开始执行");
		}
	}

}
