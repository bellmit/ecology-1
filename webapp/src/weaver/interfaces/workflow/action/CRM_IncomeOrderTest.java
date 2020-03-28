package weaver.interfaces.workflow.action;

import java.text.SimpleDateFormat;
import java.util.Date;

import weaver.conn.RecordSet;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;

public class CRM_IncomeOrderTest extends BaseBean implements Action{
	
	public String execute(RequestInfo request) {
		try {
			writeLog("开票申请更新订单数据--开始");
			RecordSet rs3 = new RecordSet();
			RecordSet rs4 = new RecordSet();
			RecordSet rs5 = new RecordSet();
			RecordSet rs6 = new RecordSet();
			RecordSet rsuuid = new RecordSet();
			int modeId = 153; // 收入分摊建模id
			String billid = request.getRequestid();
			String sql = "";
			
			String orderdate = "2019-09-05";
            String whetherShare="0";
			// 更新客户业务卡片中的应收金额
			sql = "select a.businessTwo,a.customerTwo,a.invoicemoney,a.currentmoney,a.startdate,a.enddate,"
					+ "year(a.startdate) as startyear,month(a.startdate) as startmonth,day(a.startdate) as startday,"
					+ "year(a.enddate) as endyear,month(a.enddate) as endmonth,"
					+ "(case when month(a.startdate)=month(a.enddate) then DATEDIFF(mm,a.startdate, a.enddate) else DATEDIFF(mm,a.startdate, a.enddate)+1 end) as totalmonth"
					+ " from uf_ShareMoney a where id<275";
			rs3.executeSql(sql);
			while (rs3.next()) {
				String business = Util.null2String(rs3.getString("businessTwo"));// 所属业务
				String customer = Util.null2String(rs3.getString("customerTwo"));// 客户
				String invoicemoney = Util.null2String(rs3
						.getString("invoicemoney"));// 本次开票金额
				String currentmoney = Util.null2String(rs3
						.getString("currentmoney"));// 当年分摊金额
				String startdate = Util.null2String(rs3.getString("startdate"));// 合同开始日期
				String enddate = Util.null2String(rs3.getString("enddate"));// 合同结束日期
				
				//String uuid = Util.null2String(rs3.getString("uuid"));// 关联字段
				
				String uuid="";
				String sqluuid="select SUBSTRING(uuid,1,8)+SUBSTRING(uuid,10,4)+SUBSTRING(uuid,15,4)+SUBSTRING(uuid,20,4)+SUBSTRING(uuid,25,12) " +
						"as 'uuid' from (select cast(NEWID() as varchar(36)) as uuid ) s ";
				rsuuid.executeSql(sqluuid);
				
				if(rsuuid.next()){
					uuid=Util.null2String(rsuuid.getString("uuid"));
				}
				int startyear = Integer.parseInt(Util.null2String(rs3
						.getString("startyear")));// 开始年份
				int startmonth = Integer.parseInt(Util.null2String(rs3
						.getString("startmonth")));// 开始月份
				int startday = Integer.parseInt(Util.null2String(rs3
						.getString("startday")));// 开始天
				int endyear = Integer.parseInt(Util.null2String(rs3
						.getString("endyear")));// 结束年份
				int endmonth = Integer.parseInt(Util.null2String(rs3
						.getString("endmonth")));// 结束月份
				int totalmonth = Integer.parseInt(Util.null2String(rs3
						.getString("totalmonth")));// 总月份

				sql = "update uf_crm_custbusiness set invoicemoney= isnull(invoicemoney,0)+"
						+ currentmoney
						+ " where customer='"
						+ customer
						+ "' and business='" + business + "'";
				rs4.executeSql(sql);

				// 收入分摊--LEVEL1、LEVEL2展示、LEVEL2非展示、科创专版

				if (business.equals("1") || business.equals("5")
						|| business.equals("6") || business.equals("22")) {
					String sharesql = "";
					writeLog("写入分摊customer--" + customer);
					// 判断是否分摊
					if (whetherShare.equals("0")) {
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
										+ "' and fieldid='13910' and cancel=0),'"
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
								rs5.executeSql(sharesql);

							} else {
								// 15号及以后的从下月开始分摊
								if (endmonth < 12) {
									// 未超过当年不分摊，全部计入本年分摊 2019-07-15 2019-11-31
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
											+ "' and fieldid='13910' and cancel=0),'"
											+ invoicemoney
											+ "','"
											+ orderdate
											+ "','"
											+ billid
											+ "','"
											+ uuid
											+ "','" + whetherShare + "')";
									writeLog("写入分摊2--" + sharesql);
									rs5.executeSql(sharesql);

								} else {
									// 分摊，其中一个月分摊到下年，剩余计入本年分摊 2019-07-15
									// 2019-12-31

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
											+ "' and fieldid='13910' and cancel=0),"
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
											+ uuid
											+ "','"
											+ whetherShare + "')";
									writeLog("写入分摊3--" + sharesql);
									rs5.executeSql(sharesql);

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
											+ "+1) and fieldid='13910' and cancel=0),"
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
											+ whetherShare + "')";
									writeLog("写入分摊4--" + sharesql);
									rs5.executeSql(sharesql);

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
												+ "' and fieldid='13910' and cancel=0),"
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
										rs5.executeSql(sharesql);

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
													+ "' and fieldid='13910' and cancel=0),"
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
													+ whetherShare
													+ "')";
											writeLog("写入分摊6--" + sharesql);
											rs5.executeSql(sharesql);

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
												+ " and fieldid='13910' and cancel=0),"
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
										rs5.executeSql(sharesql);

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
												+ " and fieldid='13910' and cancel=0),"
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
										rs5.executeSql(sharesql);

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
											+ " and fieldid='13910' and cancel=0),"
											+ invoicemoney
											+ "*12/"
											+ totalmonth
											+ ",'"
											+ orderdate
											+ "','"
											+ billid
											+ "','"
											+ uuid
											+ "','" + whetherShare + "')";
									writeLog("写入分摊9--" + sharesql);
									rs5.executeSql(sharesql);
								}

							}
						}
					} else {
						// 当前年
						SimpleDateFormat format = new SimpleDateFormat("yyyy");
						String currentdate = format.format(new Date());
						sharesql = "insert into uf_crm_incomeshare(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
								+ "customer,business,shareyear,sharemoney,orderdate,flowid,uuid,whetherShare) values ("
								+ modeId
								+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"
								+ "'"
								+ customer
								+ "','"
								+ business
								+ "',(select selectvalue from workflow_SelectItem where selectname='"
								+ currentdate
								+ "' and fieldid='13910' and cancel=0),'"
								+ invoicemoney
								+ "','"
								+ orderdate
								+ "','"
								+ billid
								+ "','"
								+ uuid
								+ "','"
								+ whetherShare
								+ "')";
						writeLog("是否分摊--否" + sharesql);
						rs5.executeSql(sharesql);
					}
					// 授权
					sql = "select id from uf_crm_incomeshare where uuid='"
							+ uuid + "'";
					rs6.executeSql(sql);
					while (rs6.next()) {
						String id = Util.null2String(rs6.getString("id"));// 查询收入分摊记录id
						int logid = Integer.parseInt(id);
						ModeRightInfo ModeRightInfo = new ModeRightInfo();
						ModeRightInfo.editModeDataShare(5, modeId, logid);// 新建的时候添加共享-所有人
					}
				}
			}
		} catch (Exception e) {
			writeLog("开票申请更新订单数据--出错" + e);
			return "0";
		}
		return "1";
	}
}
