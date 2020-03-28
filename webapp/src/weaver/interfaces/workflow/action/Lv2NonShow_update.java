package weaver.interfaces.workflow.action;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.alibaba.fastjson.JSON;

import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;
/**
 * 固定收益许可公示保存中间库接口
 * @author user-dev3
 *
 */
public class Lv2NonShow_update extends BaseBean implements Action {

	@Override
	public String execute(RequestInfo request) {
		//uf_fxincome表要插入的数据
		try {
			RecordSet rs = new RecordSet();
			String requestid = Util.null2String(request.getRequestid());
			rs.executeSql("select * from uf_level2_nonshow where id = "+requestid);
			RecordSetDataSource exchangeDB = new RecordSetDataSource("exchangeDB");  
			while(rs.next()){
				String uuid4Change = Util.null2String(rs.getString("uuid4Change"));
				String mainId = Util.null2String(rs.getString("id"));
				//许可用途
				//许可用途
				String permittedUse = Util.null2String(rs.getString("permittedUse"));
				//许可范围
				String permitScope = Util.null2String(rs.getString("permitScope"));
				if("0".equals(permitScope)){
					permitScope = "中国内地（不含港、澳、台地区）" ;
				}
				//申请类型
				String applyType = Util.null2String(rs.getString("applyType"));
				//许可开始日期startDate
				String startDate = Util.null2String(rs.getString("startDate"));
				//许可结束日期endDate
				String endDate = Util.null2String(rs.getString("endDate"));
				//中间表许可期限
				String deadLine = startDate + "~" + endDate;
				//是否展示isShow
				String isShow = Util.null2String(rs.getString("isShow"));
				//中间表状态
				String status = "0";
				
				if(isShow.equals("1")){
					status = "-1" ;
				}
				//排序showOrder
				String showOrder = Util.null2String(rs.getString("showOrder"));
				
				//机房描述
				String engineRoom ="";
				RecordSet rss = new RecordSet();
				rss.executeSql("select * from uf_level2_nonshow_dt1 where mainid = "+mainId);
				List<EngineRoom> engineRooms = new ArrayList<EngineRoom>();
				while(rss.next()){
					//机房地址
					String type = "" ;
					String engineName = Util.null2String(rss.getString("engineRoom"));
					if(!"".equals(engineName)){
						if(engineName.contains("自有机房")){
							type = "zy " ;
						}else{
							type = "yy";
						}
					}
					//系统部署地址
					String systemLocation = Util.null2String(rss.getString("systemLocation"));
					EngineRoom room =new EngineRoom();
					room.setEngine_name(engineName);
					room.setSystem_position(systemLocation);
					room.setType(type);
					engineRooms.add(room);
				}
				if(engineRooms.size()>0){
					engineRoom = JSON.toJSONString(engineRooms);  
				}
				
				//uuid4Change为空则是insert
				if("".equals(uuid4Change)){
					//中间表Id
					String id = UUID.randomUUID().toString();
					//公司id
					String companyId = Util.null2String(rs.getString("companyId"));
					RecordSet rscompany = new RecordSet();
					rscompany.executeSql("select * from CRM_CustomerInfo where id="+companyId);
					String organizationCode ="" ;
					String companyName ="";
					while(rscompany.next()){
						//公司名称
						companyName = Util.null2String(rscompany.getString("name"));
						//组织机构代码
						organizationCode = Util.null2String(rscompany.getString("crmcode"));
					}
				    
					String sql = "insert into SYNC_LV2DATAFEED_PERMIT_LIST "
							+ " (id,ORGANIZATION_CODE,ENGINE_ROOM,COMPANY_NAME, permission_usage,permission_range ,permission_deadline,apply_type,STATUS,SHOWORDER) "
							+ " VALUES "
							+ " ('"+id+"' , '"+organizationCode+"' , '"+engineRoom+"' , '"+companyName+"' , '"+permittedUse+"' , '"+permitScope+"' , '"+deadLine+"' , '"+applyType+"' , '"+status+"' , '"+showOrder+"' ) ";
					//将数据插入中间表
					try {
						exchangeDB.executeSql(sql);
						writeLog("insert语句："+ sql);
					} catch (Exception e) {
					//	e.printStackTrace();
						writeLog("插入SYNC_LV2DATAFEED_PERMIT_LIST出错："+e);
						return Action.FAILURE_AND_CONTINUE;
					}
					
					RecordSet rs2 = new RecordSet();
					//将中间表id插入oa表保持唯一对应关系
					rs2.executeSql(" update uf_level2_nonshow set uuid4Change = '" + id +"' where id = "+requestid );
				}else{//有值则为update操作
					String sql = " update SYNC_LV2DATAFEED_PERMIT_LIST set"
							+ " permission_usage = '"+permittedUse+"'"
							+ " ,permission_range = '"+permitScope+"'"
							+ " ,permission_deadline = '"+deadLine+"'"
							+ " ,apply_type = '"+applyType+"'"
							+ " ,STATUS = '"+status+"'"
							+ " ,SHOWORDER = '"+showOrder+"'"
							+ " ,ENGINE_ROOM = '"+engineRoom+"'"
							+ "  where id = '"+uuid4Change+"'";
					//更新中间表
					exchangeDB.executeSql(sql);
					writeLog("update语句："+ sql);
					writeLog("更新成功");
				}

			}
		} catch (Exception e) {
			writeLog("更新或插入SYNC_LV2DATAFEED_PERMIT_LIST出错："+e);
			return Action.FAILURE_AND_CONTINUE;
		}
		return Action.SUCCESS;
	}
}
