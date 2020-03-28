package weaver.interfaces.workflow.action;

import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.general.*;
import weaver.soa.workflow.request.RequestInfo;
import java.util.UUID;
//Level1--update表sync_website_lv1_permit_list
public class Level1PublicShow extends BaseBean implements Action {

public String execute(RequestInfo request){
	  try{
			RecordSet ds = new RecordSet();
			String billid  = request.getRequestid();

			RecordSetDataSource exchangeDB = new RecordSetDataSource("exchangeDB");  
            String requestid = Util.null2String(request.getRequestid());

            
            ds.executeSql("select * from uf_levelone where id = "+billid);
            
            if(ds.next()){
                    	String syid=Util.null2String(ds.getString("syid")); //	上游ID
                     	String zzjgdm = Util.null2String(ds.getString("zzjgdm")); // 组织机构代码证号
         				String gsmc= Util.null2String(ds.getString("gsmc")); //公司名称
         				String tcsj = Util.null2String(ds.getString("tcsj"));	//推出时间
         				String gsbj=Util.null2String(ds.getString("gsbj")); 	//公司背景
         				String sqxknxbegin = Util.null2String(ds.getString("sqxknxbegin"));	//许可期限开始
        				String sqxknxend=Util.null2String(ds.getString("sqxknxend")); //许可期限结束
        				String xksj = sqxknxbegin + "~" +sqxknxend ; //许可开始+许可结束

						
						String isShow = Util.null2String(ds.getString("sfxs"));//是否显示

						//中间表状态,表示展示。
						String status = "0";
						if(isShow.equals("1")){
							status = "-1" ;
						}

						String uuid4Change = Util.null2String(ds.getString("uuid4Change"));
						
						String uuid4 = Util.null2String(ds.getString("uuid4Change"));
						if(uuid4.equals("")){
							uuid4 = UUID.randomUUID().toString();
						}
						
						String SHOWORDER=Util.null2String(ds.getString("pxnew"));
						
        				String gsid=Util.null2String(ds.getString("gsid"));  //公司ID
        				String order_type = Util.null2String(ds.getString("sqxknr"));  //申请许可内容
        				if(order_type.equals("0")){
        					order_type="实时行情";
        				}
        				if(order_type.equals("1")){
        					order_type="延时行情（30分钟以上）";
        				}
        				

						if("".equals(uuid4Change)){
							String sql = " insert into sync_website_lv1_permit_list "
									+ " (ID,ORGANIZATION_CODE,COMPANY_NAME,UPLOAD_TIME,CONTENT,DEADLINE,STATUS,COMPANY_ID,ORDER_TYPE,SHOWORDER) " 
									+ " VALUES "
									+ " ('"+uuid4+"','"+zzjgdm+"','"+gsmc+"',"+"to_date('"+tcsj+"','yyyy-mm-dd'),"+"'"+gsbj+"','"+xksj+"','"+status+"','"+gsid+"','"+order_type+"','"+SHOWORDER+"') ";
						try {
							exchangeDB.executeSql(sql);
							writeLog("insert语句："+ sql);
						} catch (Exception e) {
							writeLog("插入sync_website_lv1_permit_list出错："+e);
							return Action.FAILURE_AND_CONTINUE;
						}
						
						RecordSet rs2 = new RecordSet();
						//将中间表id插入oa表保持唯一对应关系
						rs2.executeSql(" update uf_levelone set uuid4Change = '" + uuid4 +"' where id = "+billid );
						
					}else if(status.equals("0")){
						String sql ="update sync_website_lv1_permit_list set "
								+ " ORGANIZATION_CODE = '"+zzjgdm+"'"
								+ " ,COMPANY_NAME = '"+gsmc+"'"
								+ " ,CONTENT = '"+gsbj+"'"
								+ " ,DEADLINE = '"+xksj+"'"
								+ " ,STATUS = '"+status+"'"
								+ " ,COMPANY_ID = '"+gsid+"'"
								+ " ,ORDER_TYPE = '"+order_type+"'"
								+ " ,SHOWORDER = '"+SHOWORDER+"'"
								+ "  where id = '"+uuid4+"'";
						
						exchangeDB.executeSql(sql);
						writeLog("update语句："+ sql);
						writeLog("更新成功");
					}else{
						String sql ="update sync_website_lv1_permit_list set "
								+ " ORGANIZATION_CODE = '"+zzjgdm+"'"	
								+ " ,COMPANY_NAME = '"+gsmc+"'"
								+ " ,CONTENT = '"+gsbj+"'"
								+ " ,DEADLINE = '"+xksj+"'"
								+ " ,STATUS = '"+status+"'"
								+ " ,COMPANY_ID = '"+gsid+"'"
								+ " ,ORDER_TYPE = '"+order_type+"'"
								+ " ,SHOWORDER = '"+SHOWORDER+"'"
								+ "  where id = '"+uuid4+"'";
						
						exchangeDB.executeSql(sql);
						writeLog("update语句："+ sql);
						writeLog("更新成功");

					}		
            }
        }catch(Exception e){
            writeLog("Level1申请往中间表sync_website_lv1_permit_list更新数据出错"+e);
            return "0";
        }
        return Action.SUCCESS;
    }
}