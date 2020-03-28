package weaver.interfaces.workflow.action;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.formmode.data.RequestInfoForAction;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;

/**
 * 指数许可
 * 
 * @author lsq
 * @date 2019/5/13
 * @version 1.0
 */
public class Exponent_Permit extends RequestInfoForAction implements Action {

	@Override
	public String execute(RequestInfo request) {
		try {
			RecordSet rs = new RecordSet();
			RecordSetDataSource exchangeDB = new RecordSetDataSource(
					"exchangeDB");
			String requestId = Util.null2String(request.getRequestid());
			writeLog("requestid-----" + requestId);
			String sql = "";
			rs.executeSql("select * from uf_exponent_permit where id ='"+requestId+"' ");
			if (rs.next()) {
				writeLog("遍历结果集开始");
				// uuid
				String uu_id = Util.null2String(rs.getString("uu_id"));
				// 公司名称id
				String companyNameId = Util.null2String(rs
						.getString("companyName"));
				// 状态
				String status = Util.null2String(rs.getString("status"));
				// 顺序
				String showOrder = Util.null2String(rs.getString("showOrder"));

				String companyName = "";
				
				RecordSet rscompany = new RecordSet();
				sql = "SELECT * FROM CRM_CustomerInfo WHERE id='"
						+ companyNameId + "'";
				rscompany.executeSql(sql);
				if (rscompany.next()) {
					companyName = Util.null2String(rscompany
							.getString("name"));
				}
				// uuid为空，则insert
				if ("".equals(uu_id)) {
					SimpleDateFormat dateFormat_now = new SimpleDateFormat("yyyy-MM-dd");
					String createDate = dateFormat_now.format(new Date());
					SimpleDateFormat timeFormat_now = new SimpleDateFormat("HH:mm");
					String createtime = timeFormat_now.format(new Date());
					sql="update uf_exponent_permit set createDate='"+createDate+"',createTime='"+createtime+"' where  id='"+requestId+"'";
					RecordSet rsds=new RecordSet();
					rsds.executeSql(sql);
					// 中间表Id
					String id = UUID.randomUUID().toString();
					writeLog("uuid----" + id);

					String sql1 = "insert into sync_website_sse_permit_list (ID,COMPANY_NAME,STATUS,SHOWORDER) values('"
							+ id
							+ "','"
							+ companyName
							+ "','"
							+ status
							+ "','"
							+ showOrder + "')";
					// 将数据插入中间表
					try {
						exchangeDB.executeSql(sql1);
						writeLog("插入中间表成功insert语句：" + sql1);
					} catch (Exception e) {
						// e.printStackTrace();
						writeLog("插入sync_website_sse_permit_list出错：" + e);
						return Action.FAILURE_AND_CONTINUE;
					}
					RecordSet rs2 = new RecordSet();
					// 将中间表id插入oa表保持唯一对应关系
					rs2.executeSql(" update uf_exponent_permit set uu_id = '"
							+ id + "' where id = " + requestId);
					// 有值则为update操作
				} else if (status.equals("0")) {
					writeLog("uu_id不为空，并且状态为显示");
					
					SimpleDateFormat dateFormat_update = new SimpleDateFormat("yyyy-MM-dd");
					String updateDate = dateFormat_update.format(new Date());
					SimpleDateFormat timeFormat_update = new SimpleDateFormat("HH:mm");
					String updatetime = timeFormat_update.format(new Date());
					sql="update uf_exponent_permit set updatDate='"+updateDate+"',updatTime='"+updatetime+"' where id='"+requestId+"'";
					RecordSet rsp = new RecordSet();
					rsp.executeSql(sql);
					String sql2 = " UPDATE sync_website_sse_permit_list SET COMPANY_NAME = '"
							+ companyName
							+ "'"
							+ ",STATUS = '"
							+ status
							+ "'"
							+ ",SHOWORDER = '"
							+ showOrder
							+ "' WHERE id = '"
							+ uu_id + "'";
					// 更新中间表
					exchangeDB.executeSql(sql2);
					writeLog("更新成功1");
				} else {
					writeLog("uu_id不为空，并且状态为不显示");
					SimpleDateFormat dateFormat_update = new SimpleDateFormat("yyyy-MM-dd");
					String updateDate = dateFormat_update.format(new Date());
					SimpleDateFormat timeFormat_update = new SimpleDateFormat("HH:mm");
					String updatetime = timeFormat_update.format(new Date());
					sql="update uf_exponent_permit set updatDate='"+updateDate+"',updatTime='"+updatetime+"' where id='"+requestId+"'";
					RecordSet rsp = new RecordSet();
					rsp.executeSql(sql);
					String sql3 = " UPDATE sync_website_sse_permit_list SET  COMPANY_NAME = '"
							+ companyName
							+ "'"
							+ ",STATUS = '"
							+ status
							+ "'"
							+ ",SHOWORDER = '999' WHERE id = '" + uu_id + "'";
					// 更新中间表
					exchangeDB.executeSql(sql3);
					writeLog("更新成功2");
				}

			}

		} catch (Exception e) {

		}

		return null;
	}

}
