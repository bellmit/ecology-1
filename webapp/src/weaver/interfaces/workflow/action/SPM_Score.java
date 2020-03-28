package weaver.interfaces.workflow.action;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestService;
import weaver.soa.workflow.request.MainTableInfo;
import weaver.soa.workflow.request.Property;
import weaver.soa.workflow.request.RequestInfo;
import java.util.ArrayList;
import java.util.List;

public class SPM_Score extends BaseBean
  implements Action
{
  public String execute(RequestInfo request)
  {
    try
    {
      writeLog("供应商考评流程触发打分流程--开始");
      RecordSet rs = new RecordSet();
      String billid = request.getRequestid();
      String requestid = Util.null2String(request.getRequestid());
      String years="";//考评年份
      String yearname="";//考评年份名称
      String grador="";//打分人员
      String gradorname="";//打分姓名
      String tsdate="";//当前日期
      String mainsql="select years,(select selectname from workflow_SelectItem   where  fieldid='11100' and selectvalue=uf_spm_appraisalapp.years) as yearname from uf_spm_appraisalapp where requestid="+billid;
      rs.executeSql(mainsql);
      if (rs.next()) {
          years = Util.null2String(rs.getString("years"));
          yearname = Util.null2String(rs.getString("yearname"));
       }
      
      rs.executeSql(" select id,lastname,convert(varchar(10),getdate(),120) as tsdate from hrmresource where id in(select b.Col2 from (select COl2=convert(xml,' <root> <v>'+replace(m.persons,',',' </v> <v>')+' </v> </root>') from (select stuff((select ','+person from (select cast(b.businessgrador as varchar)+','+cast(b.technologygrador as varchar) as person from uf_spm_appraisalapp a,uf_spm_appraisalapp_dt1 b where a.id=b.mainid and a.requestid='"+requestid+"') as T for xml path('')), 1, 1, '') AS persons) as m) a outer apply (select Col2=C.v.value('.','nvarchar(100)') from a.COl2.nodes('/root/v')C(v)) b ) ");

      while (rs.next()) {
        grador = Util.null2String(rs.getString("id"));//打分人
        gradorname = Util.null2String(rs.getString("lastname"));//打分人
        tsdate = Util.null2String(rs.getString("tsdate"));//当前日期
        
        int requestidzi = createWF(requestid,years,yearname,grador,gradorname,tsdate);      

        
        
      }
      writeLog("供应商考评流程触发打分流程--成功");
    }
    catch (Exception e) {
      writeLog("供应商考评流程触发打分流程--出错" + e);
      return "0";
    }
    return "1";
  }
  
	/**
	 * 创建供应商考评打分流程
	 */
	public int createWF(String requestid,String years,String yearname,String grador,String gradorname,String tsdate){
		String newrequestid = "";
		try{
			writeLog(grador+"--触发打分流程--开始");
			String workflowid = "396";//流程id
			String lcbt = yearname +"年供应商考评打分："+gradorname;// 流程标题			  
			RequestService requestService = new RequestService();
			RequestInfo requestInfo = new RequestInfo();            
			requestInfo.setWorkflowid(workflowid);//流程类型id
			requestInfo.setCreatorid("36");//创建人--龚婷		
			requestInfo.setDescription("供应商考评打分");//设置流程标题 
			requestInfo.setRequestlevel("0");//0 正常，1重要，2紧急
			requestInfo.setIsNextFlow("1");//流转到下一节点
			
			MainTableInfo mainTableInfo = new MainTableInfo();
			List<Property> fields = new ArrayList<Property>();
			Property field = null;			
			
			field = new Property();
			field.setName("title");
			field.setValue(lcbt);
			fields.add(field);
			
			field = new Property();
			field.setName("createdate");
			field.setValue(tsdate);
			fields.add(field);
			
			field = new Property();
			field.setName("appraisalflow");
			field.setValue(requestid);
			fields.add(field);
			
			field = new Property();
			field.setName("years");
			field.setValue(years);
			fields.add(field);
			
			field = new Property();
			field.setName("grador");
			field.setValue(grador);
			fields.add(field);
			
			
			fields.add(field);
			Property[] fieldarray = (Property[]) fields.toArray(new Property[fields.size()]);
			mainTableInfo.setProperty(fieldarray);
			requestInfo.setMainTableInfo(mainTableInfo);			
			newrequestid = requestService.createRequest(requestInfo);
			RecordSet rs = new RecordSet();
			RecordSet rs1 = new RecordSet();
			RecordSet rs2 = new RecordSet();
			RecordSet rs3 = new RecordSet();
			//获取打分流程id
			String sqlid=" select id from uf_spm_score where requestid='"+newrequestid+"'";
			rs.executeSql(sqlid);
			String mainid="";
			if (rs.next()) {
				mainid = Util.null2String(rs.getString("id"));
			}		
			
			//插入打分明细表
			String supplier="";//供应商
			String businessgrador="";//商务打分人
			String technologygrador="";//技术打分人
			int control=0;//是否仅商务打分
			String sqlzi=" select supplier,businessgrador,technologygrador from uf_spm_appraisalapp_dt1 where mainid=(select id from uf_spm_appraisalapp where requestid='"+requestid+"') ";
			writeLog("触发打分流程sqlzi--"+sqlzi);
			rs1.executeSql(sqlzi);
			while (rs1.next()) {
		    	  supplier = Util.null2String(rs1.getString("supplier"));
		    	  businessgrador = Util.null2String(rs1.getString("businessgrador")); 
		    	  technologygrador = Util.null2String(rs1.getString("technologygrador"));
		    	  
		    	  String[] businessgradors = businessgrador.split(",");
		    	  String[] technologygradors = technologygrador.split(",");
		    	  
		    	  writeLog("触发打分流程businessgradors--"+businessgradors);
		    	  int flag=0;
		    	  int flag1=0;//判断是否是商务打分人
		    	  int flag2=0;//判断是否是技术打分人
                  //商务评分表
		    	  for (int i = 0; i < businessgradors.length; i++) {	    		  
		    		  if(grador.equals(businessgradors[i])){                      
		    			  flag1++;
		    		  }
		    	  }
		    	  for (int i = 0; i < technologygradors.length; i++) {
		    		  if(grador.equals(technologygradors[i])){                      
		    			  flag2++;
		    		  }
		    	  }
		    	  
		    	  if(flag1==0&&flag2==0){
		    		  flag=0;//不插入明细表		    		  
		    	  }else if(flag1>0&&flag2==0){
		    		  flag=1;//插入明细表,只打商务分		    		  
		    	  }else if(flag1==0&&flag2>0){
		    		  flag=2;//插入明细表,只打技术分
		    		  control=1;
		    	  }else{
		    		  flag=3;//插入明细表,打商务分和技术分
		    		  control=1;
		    	  }		    	  
		    	  
		    	  if(flag>0){
		    		   String sqlinsert1="insert into uf_spm_score_dt1(mainid,supplier,control) values ('"+mainid+"','"+supplier+"','"+flag+"')";
		    		   writeLog("触发打分流程sqlinsert1--"+sqlinsert1);
		    		   rs2.executeSql(sqlinsert1);  
		    	  }                 
		       }
			//更新是否仅商务打分
			String sql="update uf_spm_score set control="+control+" where requestid='"+newrequestid+"'";
			rs3.executeSql(sql); 
			writeLog(grador+"--触发打分流程--结束--"+newrequestid);
		}catch(Exception e){
			writeLog(grador+"--触发打分流程--出错："+e);
		}
		return Util.getIntValue(newrequestid,0);
	}
  
}