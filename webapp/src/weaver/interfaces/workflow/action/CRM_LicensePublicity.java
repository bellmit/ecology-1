package weaver.interfaces.workflow.action;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;

/**
 * 许可公示推中间库
 * 
 * @author lsq
 * @date 2019/5/17
 * @version 1.0
 */
public class CRM_LicensePublicity extends BaseBean implements Action {

	@Override
	public String execute(RequestInfo reqInfo) {
		try {
			RecordSet rs = new RecordSet();
			String requestid = Util.null2String(reqInfo.getRequestid());
			rs.executeSql("select id,customer,business,startdate,enddate,status,dsporder,uuid,syid,gsid,type,applyType from uf_crm_permitpub where id = '"
					+ requestid + "' ");
			RecordSetDataSource exchangeDB = new RecordSetDataSource("exchangeDB");
			if (rs.next()) {
				String ids = Util.null2String(rs.getString("id"));
				String customer = Util.null2String(rs.getString("customer"));
				String businessid = Util.null2String(rs.getString("business"));
				String startdate = Util.null2String(rs.getString("startdate"));
				String enddate = Util.null2String(rs.getString("enddate"));
				String status = Util.null2String(rs.getString("status"));
				String dsporder = Util.null2String(rs.getString("dsporder"));
				String uuid = Util.null2String(rs.getString("uuid"));
				String syid = Util.null2String(rs.getString("syid"));
				String gsid = Util.null2String(rs.getString("gsid"));
				String type = Util.null2String(rs.getString("type"));
				String applyType = Util.null2String(rs.getString("applyType"));
				// 中间表许可期限
				String deadLine = startdate + "~" + enddate;
				// 中间表Id
				String id = UUID.randomUUID().toString();
				RecordSet rscompany = new RecordSet();
				rscompany.executeSql("select name,creditcode from uf_CRM_CustomerInfo where id='"+ customer + "'");
				String organizationCode = "";
				String companyName = "";
				if (rscompany.next()) {
					// 公司名称
					companyName = Util.null2String(rscompany.getString("name"));
					// 组织机构代码
					organizationCode = Util.null2String(rscompany.getString("creditcode"));
				}
				// 调用获取首字母方法
				String initialUp = getInitialUp(customer);
				//更新 许可库中的首字母
			    RecordSet rs9=new RecordSet();
			    String sql9="update uf_crm_permitpub set  initialUp='"+initialUp+"' where id='"+ids+"'";
			    rs9.executeSql(sql9);
				SimpleDateFormat dateFormat_now = new SimpleDateFormat("yyyy/MM/dd");
				String tcsj = dateFormat_now.format(new Date());
				String sql = "";
				// uuid为空则为insert
				if ("".equals(uuid)) {
					if (businessid.equals("1")) { // Level-1行情许可
						sql = "insert into sync_website_lv1_permit_list "
								+ "(id,organization_code,company_name,upload_time,deadline,status,company_id,showorder,initialUp) "
								+ " values"
								+ " ('"+ id + "','"+ organizationCode+ "','"+ companyName+ "',to_date('" + tcsj + "','yyyy/MM/dd'),'"+ deadLine + "','" + status+ "','"+ gsid+ "','"+ dsporder+ "','" + initialUp + "')";
					} else if (businessid.equals("2")) {// 期权行情展示许可
						sql = " insert into sync_website_opt_permit_list "
								+ " (ID,ORGANIZATION_CODE,COMPANY_NAME,UPLOAD_TIME,DEADLINE,STATUS,COMPANY_ID,SHOWORDER,initialUp,type) "
								+ " VALUES " 
								+ " ('"+ id + "','"+ organizationCode+ "','"+ companyName+ "',to_date('" + tcsj + "','yyyy/MM/dd'),'"+ deadLine + "','" + status+ "','"+ gsid+ "','"+ dsporder+ "','" + initialUp + "','"+type+"')";
					} else if (businessid.equals("3")) {// 固定收益行情许可
						sql = "insert into SYNC_FIXEDINCOME_PERMIT_LIST  "
								+ "(id,ORGANIZATION_CODE,COMPANY_ID,COMPANY_NAME,DEADLINE,STATUS,SHOWORDER,initialUp)  "
								+ "VALUES  "
								+ " ('"+ id + "','"+ organizationCode+ "' , '"+ gsid+ "' , '"+ companyName+ "' , '"+ deadLine+ "' , '"+ status+ "' , '"+ dsporder + "' ,'" + initialUp + "') ";
					} else if (businessid.equals("4")) {// 指数授权
						sql = "insert into sync_website_sse_permit_list"
								+ "(ID,ORGANIZATION_CODE,COMPANY_NAME,DEADLINE,UPLOAD_TIME,STATUS,company_id,SHOWORDER,initialUp) "
								+ "values"
								+ " ('"+ id+ "','"+ organizationCode+ "','"+ companyName+ "','"+ deadLine+ "',to_date('"+ tcsj+ "','yyyy/MM/dd'),'"+ status+ "','"+ gsid+ "','"+ dsporder+ "','" + initialUp + "')";
					} else if (businessid.equals("5")) {// Level-2行情展示许可

						sql = "insert into sync_website_lv2_permit_list"
								+ "(id,organization_code,company_name,upload_time,permission_deadline,status,company_id,showorder,initialUp)  "
								+ "values"
								+ " ('"+ id+ "','"+ organizationCode+ "','"+ companyName+ "',to_date('"+ tcsj+ "','yyyy/MM/dd'),'"+ deadLine+ "','"+ status+ "','"+ gsid+ "','"+ dsporder+ "','" + initialUp + "')";
					} else if (businessid.equals("6")) {// 或Level-2行情非展示许可
						sql = "insert into SYNC_LV2DATAFEED_PERMIT_LIST "
								+ " (id,ORGANIZATION_CODE,COMPANY_NAME ,permission_deadline,STATUS,SHOWORDER,initialUp,applyType) "
								+ " VALUES" 
								+ " ('" + id + "','"+ organizationCode + "' , '" + companyName+ "' ,'" + deadLine + "' ,'" + status + "' , '"+ dsporder + "' ,'" + initialUp + "','"+applyType+"') ";
					}else if (businessid.equals("7")) {//期权非展示
						sql = " insert into sync_website_opt_permit_list "
								+ " (ID,ORGANIZATION_CODE,COMPANY_NAME,UPLOAD_TIME,DEADLINE,STATUS,COMPANY_ID,SHOWORDER,initialUp,type) "
								+ " VALUES " 
								+ " ('"+ id + "','"+ organizationCode+ "','"+ companyName+ "',to_date('" + tcsj + "','yyyy/MM/dd'),'"+ deadLine + "','" + status+ "','"+ gsid+ "','"+ dsporder+ "','" + initialUp + "','"+type+"')";						
					}
					// 将数据插入中间表
					try {
						exchangeDB.executeSql(sql);
						writeLog("insert语句：" + sql);
					} catch (Exception e) {
						writeLog("插入中间表出错：" + e);
						return Action.FAILURE_AND_CONTINUE;
					}
					RecordSet rs2 = new RecordSet();
					rs2.executeSql("update uf_crm_permitpub set uuid='" + id+ "' where id='" + requestid + "'");

				} else {

					if (businessid.equals("1")) {
						sql = "update sync_website_lv1_permit_list "
								+ "set "
								+ "company_name='"+ companyName+ "',status='"+ status+ "',showorder='"+ dsporder+ "',initialUp='"+ initialUp 
								+ "' where id='" + uuid + "'";
					} else if (businessid.equals("2")) {
						sql = "update sync_website_opt_permit_list "
								+ "set "
								+ "company_name='"+ companyName+ "',status='"+ status+ "',showorder='"+ dsporder+ "',initialUp='"+ initialUp 
								+ "' where id='" + uuid + "'";
					} else if (businessid.equals("3")) {
						sql = " update SYNC_FIXEDINCOME_PERMIT_LIST "
								+ "set "
								+ "company_name='"+ companyName+ "',STATUS='"+ status+ "',SHOWORDER='"+ dsporder+ "',initialUp='"+ initialUp
								+ "' where id='" + uuid + "'";
					} else if (businessid.equals("4")) {
						sql = "update sync_website_sse_permit_list "
								+ "set "
								+ "company_name='"+ companyName+ "',status='"+ status+ "',showorder='"+ dsporder+ "',initialUp='"+ initialUp 
								+ "' where id='" + uuid + "'";
					} else if (businessid.equals("5")) {
						sql = "update sync_website_lv2_permit_list "
								+ "set "
								+ "company_name='"+ companyName+ "',status='"+ status+ "',showorder='"+ dsporder+ "',initialUp='"+ initialUp 
								+ "' where id='" + uuid + "'";
					} else if (businessid.equals("6")) {
						sql = "update SYNC_LV2DATAFEED_PERMIT_LIST "
								+ "set "
								+ "company_name='"+ companyName+ "',status='"+ status+ "',showorder='"+ dsporder+ "',initialUp='"+ initialUp 
								+ "' where id='" + uuid + "'";
					} else if (businessid.equals("7")) {
						sql = "update sync_website_opt_permit_list "
								+ "set "
								+ "company_name='"+ companyName+ "',status='"+ status+ "',showorder='"+ dsporder+ "',initialUp='"+ initialUp 
								+ "' where id='" + uuid + "'";
					} 

					// 更新中间表
					try {
						exchangeDB.executeSql(sql);
						writeLog("更新成功----" + sql);
					} catch (Exception e) {
						writeLog("中间库更新异常e" + e);
					}
				}

			}
		} catch (Exception e) {
			writeLog("许可公示推中间库异常:"+e);
		}

		return null;
	}

	/**
	 * 获取客户名称首字母大写
	 *
	 * @param customerid
	 * @return String 首字母
	 */
	public String getInitialUp(String customerid) {
		writeLog("获取客户名称首字母开始...");
		String initail = ""; // 首字母
		try {
			RecordSet rs = new RecordSet();
			String sql = " select dbo.fnpbGetPYFirstLetter((select name from uf_crm_customerinfo where id='"
					+ customerid + "')) as chinaname";
			rs.executeSql(sql);
			writeLog("获取客户名称首字母sql:" + sql);
			if (rs.next()) {
				initail = Util.null2String(rs.getString("chinaname"));
			}
		} catch (Exception e) {
			writeLog("获取客户名称首字母异常:" + e);
		}
		writeLog("获取客户名称首字母结束...");
		return initail;
	}
}
