package weaver.interfaces.workflow.action;

import weaver.conn.RecordSet;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;

public class CRM_IncomeTpkp extends BaseBean implements Action {
	public String execute(RequestInfo request) {
		try {
			writeLog("订单变更开票--开始");
			RecordSet rs = new RecordSet();
			RecordSet rs1 = new RecordSet();
			RecordSet rs2 = new RecordSet();
			RecordSet rs3 = new RecordSet();
			RecordSet rs4 = new RecordSet();
			RecordSet rs5 = new RecordSet();
			RecordSet rs6 = new RecordSet();
			RecordSet rs7 = new RecordSet();
			RecordSet rs8 = new RecordSet();
			String billid = request.getRequestid();
			String sql = "";
			sql = "update uf_crm_orderinfo set invoicedate=b.invoicedate ,invoiceno=b.invoiceno,kpstatus=1,invoicemoney=a.ordermoney  "
					+ "from uf_crm_orderinfo a,uf_crm_invoiceret b where a.uuid=b.uuid  and b.requestid="
					+ billid;
			rs.executeSql(sql);
			sql = "update uf_crm_invoiceinfo set ordernumber=a.id,paydate=a.paydate,unitname=a.unitname,creditcode=a.creditcode,content=a.product,"
					+ "address=a.address,accountname=a.accountname,invoicemoney=a.invoicemoney,invoicetype=a.invoicetype  "
					+ "from  uf_crm_orderinfo a,uf_crm_invoiceret b,uf_crm_invoiceinfo c "
					+ "where a.uuid=b.uuid and c.uuid=b.uuid and b.requestid="
					+ billid;
			rs.executeSql(sql);

			String currentmoney1 = "";// 分摊金额
			String skstatus = "";// 收款状态 0收款中 1已收款
			String business1 = "";// 业务
			String customer1 = "";// 客户
			String sql3 = "select business,customer,skstatus,isnull(currentmoney,0) as currentmoney  from uf_crm_orderinfo "
					+ "where id=(select orderlist from uf_crm_invoiceret where requestid='"
					+ billid + "')";
			rs3.executeSql(sql3);
			if (rs3.next()) {
				currentmoney1 = Util.null2String(rs3.getString("currentmoney"));
				skstatus = Util.null2String(rs3.getString("skstatus"));
				business1 = Util.null2String(rs3.getString("business"));
				customer1 = Util.null2String(rs3.getString("customer"));
			}

			sql = "select business,customer,isnull(invoicemoney,0) as currentmoney from uf_crm_orderinfo where flowid="
					+ billid;
			rs1.executeSql(sql);
			if (rs1.next()) {
				String business = Util.null2String(rs1.getString("business"));// 业务
				String customer = Util.null2String(rs1.getString("customer"));// 客户
				String currentmoney = Util.null2String(rs1
						.getString("currentmoney"));// 开票金额
				sql = "update uf_crm_custbusiness set kpmoney= isnull(kpmoney,0)+"
						+ currentmoney
						+ " where customer='"
						+ customer
						+ "' and business='" + business + "'";

				if (skstatus.equals("1")) { // 旧订单已收款
					String sql4 = "update uf_crm_custbusiness set reallymoney=isnull(reallymoney,0)-"
							+ currentmoney1
							+ " where customer='"
							+ customer1
							+ "' and business='" + business1 + "'";
					rs4.executeSql(sql4);
					writeLog("订单变更开票-已收款sql:" + sql4);
				}

				writeLog("订单变更开票-开票sql:" + sql);
				rs2.executeSql(sql);
			}

			String sql5 = "select customer,business,uuid,orderdate,-currentmoney as currentmoney,-ordermoney as ordermoney,contractStartDate as startdate,contractEndDate as enddate,"
					+ "year(contractStartDate) as startyear,month(contractStartDate) as startmonth,day(contractStartDate) as startday,"
					+ "year(contractEndDate) as endyear,month(contractEndDate) as endmonth,"
					+ "(case when month(contractStartDate)=month(contractEndDate) then DATEDIFF(mm,contractStartDate,contractEndDate) "
					+ "else DATEDIFF(mm,contractStartDate, contractEndDate)+1 end) as totalmonth "
					+ "from uf_crm_orderinfo where id=(select orderlist from uf_crm_invoiceret where requestid='"
					+ billid + "')";
			rs5.executeSql(sql5);
			if (rs5.next()) {
				String business = Util.null2String(rs5.getString("business"));// 所属业务
				String customer = Util.null2String(rs5.getString("customer"));// 客户
				String invoicemoney = Util.null2String(rs5
						.getString("ordermoney"));// 开票金额
				String currentmoney = Util.null2String(rs5
						.getString("currentmoney"));// 当年分摊金额
				String startdate = Util.null2String(rs5.getString("startdate"));// 合同开始日期
				String enddate = Util.null2String(rs5.getString("enddate"));// 合同结束日期
				String orderdate = Util.null2String(rs5.getString("orderdate"));// 订购日期
				String uuid = Util.null2String(rs5.getString("uuid"));// 关联字段

				int startyear = Integer.parseInt(Util.null2String(rs5
						.getString("startyear")));// 开始年份
				int startmonth = Integer.parseInt(Util.null2String(rs5
						.getString("startmonth")));// 开始月份
				int startday = Integer.parseInt(Util.null2String(rs5
						.getString("startday")));// 开始天
				int endyear = Integer.parseInt(Util.null2String(rs5
						.getString("endyear")));// 结束年份
				int endmonth = Integer.parseInt(Util.null2String(rs5
						.getString("endmonth")));// 结束月份
				int totalmonth = Integer.parseInt(Util.null2String(rs5
						.getString("totalmonth")));// 总月份

				String sql6 = "update uf_crm_custbusiness set invoicemoney= isnull(invoicemoney,0)+"
						+ currentmoney
						+ " where customer='"
						+ customer
						+ "' and business='" + business + "'";
				rs6.executeSql(sql6);

				// 收入分摊--LEVEL1、LEVEL2展示、LEVEL2非展示、科创专版
				int modeId = 153; // 收入分摊建模id
				if (business.equals("1") || business.equals("5")
						|| business.equals("6") || business.equals("22")) {
					String sharesql = "";
					writeLog("写入分摊customer--" + customer);
					String whetherShare = "0";  //默认  
					// 开始年份和结束年份同一年
					if (startyear == endyear) {
						if (startday < 15) {
							// 15号前的从当前月开始分摊 2019-07-01 2019-12-31
							// 不分摊，全部计入本年分摊
							// 插入收入分摊记录
							sharesql = "insert into uf_crm_incomeshare(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
									+ "customer,business,shareyear,sharemoney,orderdate,flowid,uuid,whetherShare) values ("
									+ modeId
									+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"
									+ "'"
									+ customer
									+ "','"
									+ business
									+ "',(select selectvalue from workflow_SelectItem where selectname='"
									+ startyear
									+ "' and fieldid='13953' and cancel=0),'"
									+ invoicemoney
									+ "','"
									+ orderdate
									+ "','"
									+ billid
									+ "','"
									+ uuid
									+ "','"
									+ whetherShare + "')";
							writeLog("写入分摊1--" + sharesql);
							rs7.executeSql(sharesql);
						} else {
							// 15号及以后的从下月开始分摊
							if (endmonth < 12) {
								// 未超过当年不分摊，全部计入本年分摊 2019-07-15 2019-11-30
								// 插入收入分摊记录
								sharesql = "insert into uf_crm_incomeshare(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
										+ "customer,business,shareyear,sharemoney,orderdate,flowid,uuid,whetherShare) values ("
										+ modeId
										+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"
										+ "'"
										+ customer
										+ "','"
										+ business
										+ "',(select selectvalue from workflow_SelectItem where selectname='"
										+ startyear
										+ "' and fieldid='13953' and cancel=0),'"
										+ invoicemoney
										+ "','"
										+ orderdate
										+ "','"
										+ billid
										+ "','"
										+ uuid
										+ "','"
										+ whetherShare + "')";
								writeLog("写入分摊2--" + sharesql);
								rs7.executeSql(sharesql);
							} else {
								// 分摊，其中一个月分摊到下年，剩余计入本年分摊 2019-07-15 2019-12-31

								// 插入当年收入分摊记录
								sharesql = "insert into uf_crm_incomeshare(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
										+ "customer,business,shareyear,sharemoney,orderdate,flowid,uuid,whetherShare) values ("
										+ modeId
										+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"
										+ "'"
										+ customer
										+ "','"
										+ business
										+ "',(select selectvalue from workflow_SelectItem where selectname='"
										+ startyear
										+ "' and fieldid='13953' and cancel=0),"
										+ invoicemoney
										+ "*("
										+ totalmonth
										+ "-1)/"
										+ totalmonth
										+ ",'"
										+ orderdate
										+ "','"
										+ billid
										+ "','"
										+ uuid + "','" + whetherShare + "')";
								writeLog("写入分摊3--" + sharesql);
								rs7.executeSql(sharesql);

								// 插入下年收入分摊记录
								sharesql = "insert into uf_crm_incomeshare(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
										+ "customer,business,shareyear,sharemoney,orderdate,flowid,uuid,whetherShare) values ("
										+ modeId
										+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"
										+ "'"
										+ customer
										+ "','"
										+ business
										+ "',(select selectvalue from workflow_SelectItem where selectname=("
										+ startyear
										+ "+1) and fieldid='13953' and cancel=0),"
										+ invoicemoney
										+ "/"
										+ totalmonth
										+ ",'"
										+ orderdate
										+ "','"
										+ billid
										+ "','"
										+ uuid
										+ "','"
										+ whetherShare
										+ "')";
								writeLog("写入分摊4--" + sharesql);
								rs7.executeSql(sharesql);
							}
						}
					} else {
						int nextsharemonth = 0; // 下年增加分摊月份
						// 开始年份和结束年份不在同一年
						for (int i = startyear; i <= endyear; i++) {
							writeLog("开票申请更新订单数据--开始" + i);
							int sharemonth = 0; // 年度分摊月份
							if (i == startyear) {
								// 开始年
								if (startday < 15) {
									// 15号前的从当前月开始分摊 2019-07-01 2020-06-30
									sharemonth = 12 - startmonth + 1;
									// 插入当年收入分摊记录
									sharesql = "insert into uf_crm_incomeshare(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
											+ "customer,business,shareyear,sharemoney,orderdate,flowid,uuid,whetherShare) values ("
											+ modeId
											+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"
											+ "'"
											+ customer
											+ "','"
											+ business
											+ "',(select selectvalue from workflow_SelectItem where selectname='"
											+ startyear
											+ "' and fieldid='13953' and cancel=0),"
											+ invoicemoney
											+ "*"
											+ sharemonth
											+ "/"
											+ totalmonth
											+ ",'"
											+ orderdate
											+ "','"
											+ billid
											+ "','"
											+ uuid
											+ "','"
											+ whetherShare + "')";
									writeLog("写入分摊5--" + sharesql);
									rs7.executeSql(sharesql);
								} else {
									// 15号后的从下月开始分摊
									if (startmonth < 12) {
										// 开始年分摊少一个月 2019-07-15 2020-07-14
										sharemonth = 12 - startmonth;
										// 插入当年收入分摊记录
										sharesql = "insert into uf_crm_incomeshare(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
												+ "customer,business,shareyear,sharemoney,orderdate,flowid,uuid,whetherShare) values ("
												+ modeId
												+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"
												+ "'"
												+ customer
												+ "','"
												+ business
												+ "',(select selectvalue from workflow_SelectItem where selectname='"
												+ startyear
												+ "' and fieldid='13953' and cancel=0),"
												+ invoicemoney
												+ "*"
												+ sharemonth
												+ "/"
												+ totalmonth
												+ ",'"
												+ orderdate
												+ "','"
												+ billid
												+ "','"
												+ uuid
												+ "','"
												+ whetherShare + "')";
										writeLog("写入分摊6--" + sharesql);
										rs7.executeSql(sharesql);
									} else {
										// 开始年不分摊
									}
									nextsharemonth = 1;
								}

							} else if (i == endyear) {
								// 结束年
								// 开始年分摊少一个月
								if (nextsharemonth == 0) // 不存在下年多分摊的情况
								{
									if (startmonth == endmonth) // 2019-07-14
																// 2020-07-13
									{
										sharemonth = endmonth - 1;
									} else // 2019-07-01 2020-06-31
									{
										sharemonth = endmonth;
									}
									sharesql = "insert into uf_crm_incomeshare(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
											+ "customer,business,shareyear,sharemoney,orderdate,flowid,uuid,whetherShare) values ("
											+ modeId
											+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"
											+ "'"
											+ customer
											+ "','"
											+ business
											+ "',(select selectvalue from workflow_SelectItem where selectname="
											+ endyear
											+ " and fieldid='13953' and cancel=0),"
											+ invoicemoney
											+ "*"
											+ sharemonth
											+ "/"
											+ totalmonth
											+ ",'"
											+ orderdate
											+ "','"
											+ billid
											+ "','"
											+ uuid
											+ "','"
											+ whetherShare + "')";
									writeLog("写入分摊7--" + sharesql);
									rs7.executeSql(sharesql);
								} else // 存在下年多分摊的情况 2019-07-15 2020-07-14
								{
									sharemonth = endmonth;
									sharesql = "insert into uf_crm_incomeshare(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
											+ "customer,business,shareyear,sharemoney,orderdate,flowid,uuid,whetherShare) values ("
											+ modeId
											+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"
											+ "'"
											+ customer
											+ "','"
											+ business
											+ "',(select selectvalue from workflow_SelectItem where selectname="
											+ endyear
											+ " and fieldid='13953' and cancel=0),"
											+ invoicemoney
											+ "*"
											+ sharemonth
											+ "/"
											+ totalmonth
											+ ",'"
											+ orderdate
											+ "','"
											+ billid
											+ "','"
											+ uuid
											+ "','"
											+ whetherShare + "')";
									writeLog("写入分摊8--" + sharesql);
									rs7.executeSql(sharesql);
								}
							} else {
								// 中间年
								// 满年算
								sharesql = "insert into uf_crm_incomeshare(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
										+ "customer,business,shareyear,sharemoney,orderdate,flowid,uuid,whetherShare) values ("
										+ modeId
										+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"
										+ "'"
										+ customer
										+ "','"
										+ business
										+ "',(select selectvalue from workflow_SelectItem where selectname="
										+ i
										+ " and fieldid='13953' and cancel=0),"
										+ invoicemoney
										+ "*12/"
										+ totalmonth
										+ ",'"
										+ orderdate
										+ "','"
										+ billid
										+ "','"
										+ uuid
										+ "','"
										+ whetherShare
										+ "')";
								writeLog("写入分摊9--" + sharesql);
								rs7.executeSql(sharesql);
							}
						}
					}
					// 授权
					String sql8 = "select id from uf_crm_incomeshare where uuid='"
							+ uuid + "'";
					rs8.executeSql(sql8);
					while (rs8.next()) {
						String id = Util.null2String(rs8.getString("id"));// 查询收入分摊记录id
						int logid = Integer.parseInt(id);
						ModeRightInfo ModeRightInfo = new ModeRightInfo();
						ModeRightInfo.editModeDataShare(5, modeId, logid);// 新建的时候添加共享-所有人
					}
				}
			}
			writeLog("订单变更开票--结束");
		} catch (Exception e) {
			writeLog("订单变更开票--出错" + e);
			return "0";
		}
		return "1";
	}
}