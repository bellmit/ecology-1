package weaver.interfaces.workflow.action;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import net.sf.json.JSONObject;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

public class AM_GdzcCmdbSyn extends BaseBean implements Action{
	
	public String execute(RequestInfo request){
		String result = "1";
		try {
			RecordSet rs = new RecordSet();
			String requestid = Util.null2String(request.getRequestid());
			String sql="select *,(select name from uf_am_assettype where id=assettype3) as assettypename3," +
					"(select lastname from hrmresource where id=userperson) as userpersonname " +
					"from uf_am_fixedassets  where  assettype3 in(37,56,63,64) and  flowid='" + requestid + "'";
			rs.executeSql(sql);
			String assetno="";//固定资产编号
			String serial_number="";//序列号
			String supplier="";//供应商
			String asset_name="";//类型名称
			String operator="";//使用人名称
			String insurance_time="";//资产入库日期
			String bk_supplier_account="0";//开发商账号，填 "0"
			//String prefix_url ="http://10.10.20.102:33031/api/v3";//接口前缀:测试地址
			String prefix_url ="http://10.95.11.94:33031/api/v3";//接口前缀
			String assettype3="";//资产类型id
			
			String syntype="0";//接口类型
			String flowid=requestid;//验收流程
			String bdresult="";//同步结果
			String datainfo="";//返回信息
			RecordSet rs2 = new RecordSet();
			
			while(rs.next()){
				String url=prefix_url+"/inst/"+bk_supplier_account+"/";
				JSONObject jsonobject=new JSONObject();
				assetno=Util.null2String(rs.getString("assetno"));//固定资产编号
				serial_number=Util.null2String(rs.getString("snno"));//序列号
				supplier=Util.null2String(rs.getString("supplier"));//供应商
				assettype3=Util.null2String(rs.getString("assettype3"));//资产类型id
				asset_name=Util.null2String(rs.getString("assettypename3"));//类型名称
				operator=Util.null2String(rs.getString("userpersonname"));//使用人名称
				insurance_time=Util.null2String(rs.getString("storagedate"));//资产入库日期
				jsonobject.put("bk_supplier_account", bk_supplier_account);
				jsonobject.put("bk_asset_id", assetno);
				jsonobject.put("serial_number", serial_number);
				jsonobject.put("bk_inst_name", serial_number);
				jsonobject.put("bk_supplier", supplier);
				jsonobject.put("assets_name", asset_name);
				jsonobject.put("bk_operator", operator);
				jsonobject.put("insurance_time", insurance_time);
				if("37".equals(assettype3)){
					url+="server";//服务器
				}else if("56".equals(assettype3)){
					url+="bk_router";//路由器
				}else if("63".equals(assettype3)){
					url+="bk_firewall";//防火墙
				}else{
					url+="bk_swith";//交换机
				}
				String value=this.sendPost(url, jsonobject.toString());
				JSONObject jsonObject2 = JSONObject.fromObject(value);
				bdresult=jsonObject2.getString("result");
				datainfo=jsonObject2.getString("bk_error_msg");
				String syndate=com.weaver.formmodel.util.DateHelper.getCurrentDate();//同步日期
				String syntime=com.weaver.formmodel.util.DateHelper.getCurrentTime();//同步时间
				String sql2="insert into uf_am_synlog(syntype,title,flowid,result,datainfo,syndate,syntime) values('"+syntype+"','"+assetno+"','"+flowid+"','"+bdresult+"','"+datainfo+"','"+syndate+"','"+syntime+"')";
				rs2.executeSql(sql2);		
			}	
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return "0";
		}
		return result;
	}
	
	public String sendPost(String url, String param) {
		OutputStreamWriter out = null;
        BufferedReader in = null;
	    String result = "";
	    try {
	        URL realUrl = new URL(url);
	        HttpURLConnection conn = null;
	        conn = (HttpURLConnection) realUrl.openConnection();// 打开和URL之间的连接
	        // 发送POST请求必须设置如下两行
	        conn.setRequestMethod("POST"); // POST方法
	        conn.setDoOutput(true);
	        conn.setDoInput(true);
	        // 设置通用的请求属性
	        conn.setRequestProperty("BK_USER", "admin");
	        conn.setRequestProperty("HTTP_BLUEKING_SUPPLIER_ID", "0");
	        conn.setRequestProperty("Content-Type", "application/json");
	        conn.connect();
	        out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");// 获取URLConnection对象对应的输出流
	        out.write(param);// 发送请求参数
	        out.flush();// flush输出流的缓冲
	        in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));//定义BufferedReader输入流来读取URL的响应
	        String line;
	        while ((line = in.readLine()) != null) {
	            result += line;
	        }
	    } catch (Exception e) {
	        System.out.println("发送 POST 请求出现异常！"+e);
	        e.printStackTrace();
	    }
	    //使用finally块来关闭输出流、输入流
	    finally{
	        try{
	           if (out!=null) {
	               out.close();
	           }
	           if (in!=null) {
	               in.close();
	           }
	        }
	        catch(IOException ex){
	            ex.printStackTrace();
	        }
	    }
	    return result;
	}
}

