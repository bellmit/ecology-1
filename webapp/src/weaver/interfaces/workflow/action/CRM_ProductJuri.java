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
 * 上证云生产权限开通
 * 
 * @author lsq
 * @date 2019/5/24
 * @version 1.0
 */
public class CRM_ProductJuri extends BaseBean implements Action {

	@Override
	public String execute(RequestInfo request) {
		try {
			String requestid = Util.null2String(request.getRequestid());
			String sql = "select customer,business,qsrq,jzrq,zyfsl,sl50,sl100,sl200,sl500,sl1000,wtyj,Mobilesl50,Mobilesl100,"
					+ "Mobilesl200,Mobilesl500,Mobilesl1000,Mobileslywsj,Mobileslkfzc,dyzysl,zjgzysl,ysdjslA,ysdjslB,"
					+ "ysdjslV from formtable_main_85 where requestid='"
					+ requestid + "'";
			RecordSet rs = new RecordSet();
			rs.executeSql(sql);
			if (rs.next()) {
				// 业务
				String business = Util.null2String(rs.getString("business"));
				// 客户
				String customer = Util.null2String(rs.getString("customer"));
				// 业务开始日期
				String startdate = Util.null2String(rs.getString("qsrq"));
				// 业务结束日期
				String enddate = Util.null2String(rs.getString("jzrq"));

				// 权限关闭日期
				SimpleDateFormat simpldate = new SimpleDateFormat("yyyy-MM-dd");
				String opendate = simpldate.format(new Date());

				// 单位资源租用费数量
				String zyfsl = Util.null2String(rs.getString("zyfsl"));
				double zyfsls = 0;
				if (zyfsl != null && zyfsl.length() > 0) {
					zyfsls = Double.parseDouble(zyfsl);
				}
				// PC行情50万数量
				String sl50 = Util.null2String(rs.getString("sl50"));
				double pcsl50 = 0;
				if (sl50 != null && sl50.length() > 0) {
					pcsl50 = Double.parseDouble(sl50);
				}

				// PC行情100万数量
				String sl100 = Util.null2String(rs.getString("sl100"));
				double pcsl100 = 0;
				if (sl100 != null && sl100.length() > 0) {
					pcsl100 = Double.parseDouble(sl100);
				}

				// PC行情200万数量
				String sl200 = Util.null2String(rs.getString("sl200"));
				double pcsl200 = 0;
				if (sl200 != null && sl200.length() > 0) {
					pcsl200 = Double.parseDouble(sl200);
				}

				// PC行情500万数量
				String sl500 = Util.null2String(rs.getString("sl500"));
				double pcsl500 = 0;
				if (sl500 != null && sl500.length() > 0) {
					pcsl500 = Double.parseDouble(sl500);
				}

				// PC行情1000万数量
				String sl1000 = Util.null2String(rs.getString("sl1000"));
				double pcsl1000 = 0;
				if (sl1000 != null && sl1000.length() > 0) {
					pcsl1000 = Double.parseDouble(sl1000);
				}

				// 委托依据
				String openfile = Util.null2String(rs.getString("wtyj"));
				// 移动行情50万价格
				String Mobilesl50 = Util
						.null2String(rs.getString("Mobilesl50"));
				double bilesl50 = 0;
				if (Mobilesl50 != null && Mobilesl50.length() > 0) {
					bilesl50 = Double.parseDouble(Mobilesl50);
				}

				// 移动行情100万价格
				String Mobilesl100 = Util.null2String(rs
						.getString("Mobilesl100"));
				double bilesl100 = 0;
				if (Mobilesl100 != null && Mobilesl100.length() > 0) {
					bilesl100 = Double.parseDouble(Mobilesl100);
				}

				// 移动行情200万价格
				String Mobilesl200 = Util.null2String(rs
						.getString("Mobilesl200"));
				double bilesl200 = 0;
				if (Mobilesl200 != null && Mobilesl200.length() > 0) {
					bilesl200 = Double.parseDouble(Mobilesl200);
				}

				// 移动行情500万价格
				String Mobilesl500 = Util.null2String(rs
						.getString("Mobilesl500"));
				double bilesl500 = 0;
				if (Mobilesl500 != null && Mobilesl500.length() > 0) {
					bilesl500 = Double.parseDouble(Mobilesl500);
				}

				// 移动行情1000万价格
				String Mobilesl1000 = Util.null2String(rs
						.getString("Mobilesl1000"));
				double bilesl1000 = 0;
				if (Mobilesl1000 != null && Mobilesl1000.length() > 0) {
					bilesl1000 = Double.parseDouble(Mobilesl1000);
				}

				// 移动行情业务升级数量
				String Mobileslywsj = Util.null2String(rs
						.getString("Mobileslywsj"));
				double bileslywsj = 0;
				if (Mobileslywsj != null && Mobileslywsj.length() > 0) {
					bileslywsj = Double.parseDouble(Mobileslywsj);
				}

				// 移动行情开发支持数量
				String Mobileslkfzc = Util.null2String(rs
						.getString("Mobileslkfzc"));
				double bileslkfzc = 0;
				if (Mobileslkfzc != null && Mobileslkfzc.length() > 0) {
					bileslkfzc = Double.parseDouble(Mobileslkfzc);
				}

				// 单元租用数量
				String dyzysl = Util.null2String(rs.getString("dyzysl"));
				double dyzyslc = 0;
				if (dyzysl != null && dyzysl.length() > 0) {
					dyzyslc = Double.parseDouble(dyzysl);
				}

				// 整机柜租用数量
				String zjgzysl = Util.null2String(rs.getString("zjgzysl"));
				double zjgzyslc = 0;
				if (zjgzysl != null && zjgzysl.length() > 0) {
					zjgzyslc = Double.parseDouble(zjgzysl);
				}

				// 运算单元数量A
				String ysdjslA = Util.null2String(rs.getString("ysdjslA"));
				double ysdjsla = 0;
				if (ysdjslA != null && ysdjslA.length() > 0) {
					ysdjsla = Double.parseDouble(ysdjslA);
				}

				// 运算单元数量B
				String ysdjslC = Util.null2String(rs.getString("ysdjslV"));
				double ysdjslc = 0;
				if (ysdjslC != null && ysdjslC.length() > 0) {
					ysdjslc = Double.parseDouble(ysdjslC);
				}

				// 运算单元数量C
				String ysdjslB = Util.null2String(rs.getString("ysdjslB"));
				double ysdjslb = 0;
				if (ysdjslB != null && ysdjslB.length() > 0) {
					ysdjslb = Double.parseDouble(ysdjslB);
				}

				if (pcsl50 > 0 || pcsl100 > 0 || pcsl200 > 0 || pcsl500 > 0
						|| pcsl1000 > 0) {
					String contentid = "70";
					createWF(business, customer, startdate, "", "", opendate,
							openfile, contentid, requestid);
				}
				if (bilesl50 > 0 || bilesl100 > 0 || bilesl200 > 0
						|| bilesl500 > 0 || bilesl1000 > 0 || bileslywsj > 0
						|| bileslkfzc > 0) {
					String contentid = "71";
					createWF(business, customer, startdate, "", "", opendate,
							openfile, contentid, requestid);
				}
				if (ysdjsla > 0 || ysdjslb > 0 || ysdjslc > 0 || dyzyslc > 0
						|| zjgzyslc > 0 || zyfsls > 0) {
					String contentid = "72";
					createWF(business, customer, startdate, "", "", opendate,
							openfile, contentid, requestid);
				}
				// 更新客户业务卡片权限信息
				sql = "update uf_crm_custbusiness set qxstatus='开通'  where customer='"
						+ customer + "' and business='" + business + "'";
				RecordSet rs1 = new RecordSet();
				rs1.executeSql(sql);
			}
			writeLog("权限开通--结束");
		} catch (Exception e) {
			writeLog("上证云生产权限开通异常:" + e);
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
