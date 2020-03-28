/**
 * 
 */
package weaver.interfaces.workflow.action;
import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.formmode.data.RequestInfoForAction;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;
import java.util.UUID;

/**
 * @author pengwang 
 * 实现level2许可信息公示
 * 2018-05-22
 *
 */
public class Level2PublicShow extends RequestInfoForAction implements Action {

	/* (non-Javadoc)
	 * @see weaver.interfaces.workflow.action.Action#execute(weaver.soa.workflow.request.RequestInfo)
	 */
	@Override
	public String execute(RequestInfo request) {
		
		try{
			//数据结果集
			RecordSet rs = new RecordSet();
			String requestId=  Util.null2String(request.getRequestid());
			rs.executeSql("select * from uf_leveltwo where id = "+requestId);
			RecordSetDataSource exchangeDB = new RecordSetDataSource("exchangeDB");
			while(rs.next()){
				String uuid4Change = Util.null2String(rs.getString("uuid4Change")); 
				//公司id
				String companyId = Util.null2String(rs.getString("gsid"));
				//许可用途
				String permittedUse = Util.null2String(rs.getString("xkyt"));
				//许可范围
				String permitScope = Util.null2String(rs.getString("sqxkfw"));
				if("0".equals(permitScope)){
					permitScope = "中国内地（不含港、澳、台地区）" ;
				}
				//许可开始日期startDate
				String startDate = Util.null2String(rs.getString("fwkssj"));
				//许可结束日期endDate
				String endDate = Util.null2String(rs.getString("fwjssj"));
				//中间表许可期限
				String deadLine = startDate + "~" + endDate;
				//是否展示
				String isShow = Util.null2String(rs.getString("sfxs"));
				//中间表状态,表示展示。
				String status = "0";
				if(isShow.equals("1")){
					status = "-1" ;
				}
				//排序
				String showOrder = Util.null2String(rs.getString("pxnew"));
				//审核产品名称
				String auditPproduct = Util.null2String(rs.getString("cphfwmc"));
				//uuid4Change为空则是insert
				if("".equals(uuid4Change)){
					//中间表Id
					String id = UUID.randomUUID().toString();
					RecordSet rscompany = new RecordSet();
					rscompany.executeSql("select * from CRM_CustomerInfo where id='"+companyId+"'");
					String organizationCode ="" ;
					String companyName ="";
					while(rscompany.next()){
						//公司名称
						companyName = Util.null2String(rscompany.getString("name"));
						//组织机构代码
						organizationCode = Util.null2String(rscompany.getString("crmcode"));
					}
					String sql = "insert into sync_website_lv2_permit_list "
							+ " (ID,ORGANIZATION_CODE,COMPANY_NAME,PERMISSION_USAGE,PERMISSION_RANGE,PERMISSION_DEADLINE,AUDITED_PRODUCT,STATUS,COMPANY_ID,SHOWORDER) "
							+ " VALUES "
							+ " ('"+id+"' , '"+organizationCode+"' , '"+companyName+"' , '"+permittedUse+"' , '"+permitScope+"' , '"+deadLine+"' , '"+auditPproduct+"' , '"+status+"' , '"+companyId+"' , '"+showOrder+"' ) ";
					//将数据插入中间表
					try {
						exchangeDB.executeSql(sql);
						writeLog("insert语句："+ sql);
					} catch (Exception e) {
					//	e.printStackTrace();
						writeLog("插入sync_website_lv2_permit_list出错："+e);
						return Action.FAILURE_AND_CONTINUE;
					}
					
					RecordSet rs2 = new RecordSet();
					//将中间表id插入oa表保持唯一对应关系
					rs2.executeSql(" update uf_leveltwo set uuid4Change = '" + id +"' where id = "+requestId );
				//有值则为update操作
				}else if(status.equals("0")){
					String sql = " update sync_website_lv2_permit_list set"
							+ " permission_usage = '"+permittedUse+"'"
							+ " ,permission_range = '"+permitScope+"'"
							+ " ,permission_deadline = '"+deadLine+"'"
							+ " ,STATUS = '"+status+"'"
							+ " ,SHOWORDER = '"+showOrder+"'"
							+ " ,AUDITED_PRODUCT = '"+auditPproduct+"'"
							+ "  where id = '"+uuid4Change+"'";
					//更新中间表
					exchangeDB.executeSql(sql);
					writeLog("update语句："+ sql);
					writeLog("更新成功");
				}else{
					String sql = " update sync_website_lv2_permit_list set"
							+ " permission_usage = '"+permittedUse+"'"
							+ " ,permission_range = '"+permitScope+"'"
							+ " ,permission_deadline = '"+deadLine+"'"
							+ " ,STATUS = '"+status+"'"
							+ " ,SHOWORDER ='999'"
							+ " ,AUDITED_PRODUCT = '"+auditPproduct+"'"
							+ "  where id = '"+uuid4Change+"'";
					//更新中间表
					exchangeDB.executeSql(sql);
					writeLog("update语句："+ sql);
					writeLog("更新成功");
				}
			}
			
		}catch(Exception e){
			
		}
	
		return null;
	}

}
