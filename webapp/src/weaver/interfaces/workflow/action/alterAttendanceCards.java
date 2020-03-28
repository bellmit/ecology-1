package weaver.interfaces.workflow.action;
import weaver.conn.RecordSet;
import weaver.general.*;
import weaver.soa.workflow.request.RequestInfo;


public class alterAttendanceCards  extends BaseBean implements Action {

public String execute(RequestInfo request){
	  try{

			RecordSet rs = new RecordSet();
            String requestid = Util.null2String(request.getRequestid());
writeLog("requestid:____"+requestid);
            rs.executeSql("select a.id as ZBid,b.* from formtable_main_171 a,formtable_main_171_dt1 b where a.id=b.mainid and a.requestid = "+requestid );
            while(rs.next()){
			
				//主表id
				String id = Util.null2String(rs.getString("ZBid"));
writeLog("id:____"+id);
				//明细表id
				String mxid = Util.null2String(rs.getString("id"));
writeLog("mxid:____"+mxid);
				//模块数据ID
				String  selectCards= Util.null2String(rs.getString("selectCards"));
writeLog("selectCards:____"+selectCards);
				//手机-带出
				String  Moblie= Util.null2String(rs.getString("Moblie"));
writeLog("Moblie:____"+Moblie);
				//公司名称-带出
				String  companyName= Util.null2String(rs.getString("companyName"));
writeLog("companyName:____"+companyName);
				//使用人-带出
				String  userName= Util.null2String(rs.getString("userName"));
writeLog("userName:____"+userName);
				//门禁卡状态-带出
				String  cardStatus2= Util.null2String(rs.getString("cardStatus2"));
writeLog("cardStatus2:____"+cardStatus2);
				//门禁卡状态-注销
				String  cardStatus= Util.null2String(rs.getString("cardStatus"));
writeLog("cardStatus:____"+cardStatus);




					String sql ="update uf_attendanceCards set "
							+ " cardStatus = '"+cardStatus+"'"
							+ "  where id = '"+selectCards+"'";
					
					rs.executeSql(sql);
					writeLog("update语句："+ sql);
                    writeLog("更新成功");
            }
        }catch(Exception e){
            writeLog("更新考勤门禁卡表uf_attendanceCards失败"+e);
            return "0";
        }
        return Action.SUCCESS;
    }
}
