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
 * 上证e服务权限开通
 * 
 * @author lsq
 * @date 2019/5/24
 * @version 1.0
 */
public class CRM_EServerJuri extends BaseBean implements Action {

	@Override
	public String execute(RequestInfo requestInfo) {
		try {
			String requestid = Util.null2String(requestInfo.getRequestid());
			String sql = "select business,fileAttach,customer,brokerBizStart"
					+ ",brokerBizEnd,leaseReportStart,leaseReportEnd,zltj_month_04,zltj_month_05,SHConnectStart,SHConnectEnd,marginTradeStart,marginTradeEnd"
					+ ",repurchaseStart,repurchaseEnd,contactRepoStart,contactRepoEnd,pledgeStockStart,pledgeStockEnd,activitySerStart,activitySerEnd,allDiscountsStart,allDiscountsEnd"
					+ ",jjyw_money,zltj_week_money,zltj_month_money,marginTrade_money,repurchase_money,contactRepo_money,pledgeStock_money,activityService_money,allDiscounts_money from  formtable_main_102 where requestid='"
					+ requestid + "'";
			RecordSet rs = new RecordSet();
			rs.executeSql(sql);
			if (rs.next()) {
				String business = Util.null2String(rs.getString("business"));
				// 附件
				String fileAttach = Util
						.null2String(rs.getString("fileAttach"));
				// 客户名称
				String customer = Util.null2String(rs.getString("customer"));

				// 权限关闭日期
				SimpleDateFormat simpldate = new SimpleDateFormat("yyyy-MM-dd");
				String opendate = simpldate.format(new Date());

				// 经纪业务
				String jjyw_money = Util
						.null2String(rs.getString("jjyw_money"));
				if (!"".equals(jjyw_money)) {
					// 经纪业务开始日
					String brokerBizStart = Util.null2String(rs
							.getString("brokerBizStart"));
					// 经纪业务结束日
					String brokerBizEnd = Util.null2String(rs
							.getString("brokerBizEnd"));
					String contentid = "57";

					createWF(business, customer, brokerBizStart, brokerBizEnd,
							"", opendate, fileAttach, contentid, requestid);
				}
				// 租赁统计-周
				String zltj_week_money = Util.null2String(rs
						.getString("zltj_week_money"));
				if (!"".equals(zltj_week_money)) {
					// 周-租赁统计开始日
					String leaseReportStart = Util.null2String(rs
							.getString("leaseReportStart"));
					// 周-租赁统计结束日
					String leaseReportEnd = Util.null2String(rs
							.getString("leaseReportEnd"));
					String contentid = "58";
					createWF(business, customer, leaseReportStart,
							leaseReportEnd, "", opendate, fileAttach,
							contentid, requestid);
				}
				// 租赁统计-月
				String zltj_month_money = Util.null2String(rs
						.getString("zltj_month_money"));
				if (!"".equals(zltj_month_money)) {
					// 月-租赁统计开始日
					String zltj_month_04 = Util.null2String(rs
							.getString("zltj_month_04"));
					// 月-租赁统计结束日
					String zltj_month_05 = Util.null2String(rs
							.getString("zltj_month_05"));
					String contentid = "58";
					createWF(business, customer, zltj_month_04, zltj_month_05,
							"", opendate, fileAttach, contentid, requestid);
				}
				// 融资融券
				String marginTrade_money = Util.null2String(rs
						.getString("marginTrade_money"));
				if (!"".equals(marginTrade_money)) {
					// 融资融券开始日
					String marginTradeStart = Util.null2String(rs
							.getString("marginTradeStart"));
					// 融资融券结束日
					String marginTradeEnd = Util.null2String(rs
							.getString("marginTradeEnd"));
					String contentid = "60";
					createWF(business, customer, marginTradeStart,
							marginTradeEnd, "", opendate, fileAttach,
							contentid, requestid);
				}
				// 报价回购
				String repurchase_money = Util.null2String(rs
						.getString("repurchase_money"));
				if (!"".equals(repurchase_money)) {
					// 报价回购开始日
					String repurchaseStart = Util.null2String(rs
							.getString("repurchaseStart"));
					// 报价回购结束日
					String repurchaseEnd = Util.null2String(rs
							.getString("repurchaseEnd"));
					String contentid = "61";
					createWF(business, customer, repurchaseStart,
							repurchaseEnd, "", opendate, fileAttach, contentid,
							requestid);
				}
				// 约定购回
				String contactRepo_money = Util.null2String(rs
						.getString("contactRepo_money"));
				if (!"".equals(contactRepo_money)) {
					// 约定购回开始日
					String contactRepoStart = Util.null2String(rs
							.getString("contactRepoStart"));
					// 约定购回结束日
					String contactRepoEnd = Util.null2String(rs
							.getString("contactRepoEnd"));
					String contentid = "62";
					createWF(business, customer, contactRepoStart,
							contactRepoEnd, "", opendate, fileAttach,
							contentid, requestid);
				}
				// 股票质押
				String pledgeStock_money = Util.null2String(rs
						.getString("pledgeStock_money"));
				if (!"".equals(pledgeStock_money)) {
					// 股票质押开始日
					String pledgeStockStart = Util.null2String(rs
							.getString("pledgeStockStart"));
					// 股票质押结束日
					String pledgeStockEnd = Util.null2String(rs
							.getString("pledgeStockEnd"));
					String contentid = "63";
					createWF(business, customer, pledgeStockStart,
							pledgeStockEnd, "", opendate, fileAttach,
							contentid, requestid);
				}
				// 基础信息/融资融券/转融通
				String activityService_money = Util.null2String(rs
						.getString("activityService_money"));
				if (!"".equals(activityService_money)) {
					// 运营服务开始日
					String activitySerStart = Util.null2String(rs
							.getString("activitySerStart"));
					// 运营服务结束日
					String activitySerEnd = Util.null2String(rs
							.getString("activitySerEnd"));
					String contentidjc = "64";
					String contentidrz = "65";
					String contentidzr = "66";
					createWF(business, customer, activitySerStart,
							activitySerEnd, "", opendate, fileAttach,
							contentidjc, requestid);
					createWF(business, customer, activitySerStart,
							activitySerEnd, "", opendate, fileAttach,
							contentidrz, requestid);
					createWF(business, customer, activitySerStart,
							activitySerEnd, "", opendate, fileAttach,
							contentidzr, requestid);
				}
				// 合并优惠
				String allDiscounts_money = Util.null2String(rs
						.getString("allDiscounts_money"));
				if (!"".equals(allDiscounts_money)) {
					// 合并优惠开始日
					String allDiscountsStart = Util.null2String(rs
							.getString("allDiscountsStart"));
					// 合并优惠结束日
					String allDiscountsEnd = Util.null2String(rs
							.getString("allDiscountsEnd"));
					String contentid1 = "60";
					String contentid2 = "61";
					String contentid3 = "62";
					String contentid4 = "63";
					String contentid5 = "64";
					String contentid6 = "65";
					String contentid7 = "66";
					createWF(business, customer, allDiscountsStart,
							allDiscountsEnd, "", opendate, fileAttach,
							contentid1, requestid);
					createWF(business, customer, allDiscountsStart,
							allDiscountsEnd, "", opendate, fileAttach,
							contentid2, requestid);
					createWF(business, customer, allDiscountsStart,
							allDiscountsEnd, "", opendate, fileAttach,
							contentid3, requestid);
					createWF(business, customer, allDiscountsStart,
							allDiscountsEnd, "", opendate, fileAttach,
							contentid4, requestid);
					createWF(business, customer, allDiscountsStart,
							allDiscountsEnd, "", opendate, fileAttach,
							contentid5, requestid);
					createWF(business, customer, allDiscountsStart,
							allDiscountsEnd, "", opendate, fileAttach,
							contentid6, requestid);
					createWF(business, customer, allDiscountsStart,
							allDiscountsEnd, "", opendate, fileAttach,
							contentid7, requestid);
				}

				// 沪港通开始日
				String SHConnectStart = Util.null2String(rs
						.getString("SHConnectStart"));
				// 沪港通结束日
				String SHConnectEnd = Util.null2String(rs
						.getString("SHConnectEnd"));
				String contentid = "59";
				createWF(business, customer, SHConnectStart, SHConnectEnd, "",
						opendate, fileAttach, contentid, requestid);
                //信息参考
				String contentidhy = "68";
				String contentidjy = "67";
				createWF(business, customer, "", "", "", opendate, fileAttach,
						contentidhy, requestid);
				createWF(business, customer, "", "", "", opendate, fileAttach,
						contentidjy, requestid);
                //股票期权
				String contentid8 = "69";
				createWF(business, customer, "", "", "", opendate, fileAttach,
						contentid8, requestid);

				// 更新客户业务卡片权限信息
				sql = "update uf_crm_custbusiness set qxstatus='开通'  where customer='"
						+ customer + "' and business='" + business + "'";
				RecordSet rs1 = new RecordSet();
				rs1.executeSql(sql);
			}

			writeLog("权限开通--结束");
		} catch (Exception e) {
			writeLog("上证e服务权限开通异常:" + e);
			return "0";
		}
		return "1";
	}

	/**
	 * 创建权限
	 */
	public int createWF(String business, String customer, String startdate,
			String enddate, String reminddate, String opendate,
			String openfile, String contentid, String billid) {
		String newid = "";
		try {
			writeLog("创建权限子--开始");
			int modeId = 116; // 建模id
			RecordSet rs1 = new RecordSet();
			RecordSet rs2 = new RecordSet();
			RecordSet rs3 = new RecordSet();
			RecordSet rs4 = new RecordSet();
			RecordSet rs5 = new RecordSet();
			String sql = "";

			// 权限拆分
			// / String[] contents = contentid.split(",");

			// for (int i = 0; i < contents.length; i++) {
			if (!"".equals(contentid)) {
				String qxid = "";
				// 查询权限是否存在
				sql = "select id from uf_crm_permission where customer='"
						+ customer + "' and business='" + business
						+ "' and contentid='" + contentid + "' and status='0'";
				rs1.executeSql(sql);
				if (rs1.next()) {
					qxid = Util.null2String(rs1.getString("id"));
					// 权限存在，则原权限状态为关闭
					if (!qxid.equals("")) {
						sql = "update uf_crm_permission set status='1' , closedate='"
								+ opendate + "' where id='" + qxid + "'";
						rs2.executeSql(sql);
					}
				}

				String contentname = "";
				sql = "select content from uf_crm_permissinfo where id='"
						+ contentid + "'";
				rs3.executeSql(sql);
				if (rs3.next()) {
					contentname = Util.null2String(rs3.getString("content"));
				}

				// 写入新权限
				UUID uuid = UUID.randomUUID();
				String wyid = uuid.toString();// 生成唯一的标识码
				sql = "insert into uf_crm_permission(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
						+ "customer,business,startdate,enddate,reminddate,opendate,openfile,"
						+ " status,contentid,title,flowid,uuid) " + "select "
						+ modeId
						+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"
						+ "'"
						+ customer
						+ "','"
						+ business
						+ "','"
						+ startdate
						+ "','"
						+ enddate
						+ "','"
						+ reminddate
						+ "','"
						+ opendate
						+ "','"
						+ openfile
						+ "', "
						+ "'0','"
						+ contentid
						+ "','"
						+ contentname
						+ "','"
						+ billid
						+ "','" + wyid + "'";
				writeLog("权限sql=" + sql);
				rs4.executeSql(sql);
				sql = "select id from uf_crm_permission where uuid='" + wyid
						+ "'";
				rs5.executeSql(sql);
				if (rs5.next()) {
					String id = Util.null2String(rs5.getString("id"));// 查询权限id
					int logid = Integer.parseInt(id);
					ModeRightInfo ModeRightInfo = new ModeRightInfo();
					ModeRightInfo.editModeDataShare(5, modeId, logid);// 新建的时候添加共享-所有人
				}
			}
			// }
			writeLog("创建权限子--结束--");
		} catch (Exception e) {
			writeLog("创建权限子--出错：" + e);
		}
		return Util.getIntValue(newid, 0);
	}
}
