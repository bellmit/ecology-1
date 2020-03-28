package weaver.sysinterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import weaver.conn.RecordSet;
import weaver.file.Prop;
import weaver.general.BaseBean;
import weaver.general.TimeUtil;
import weaver.general.Util;
import weaver.hrm.resource.ResourceComInfo;


public class SendMessageService extends Thread {
	
	
	private HttpServletRequest request;
	private  HttpServletResponse response;
	private BaseBean baseBean = new BaseBean();
	
	private String requestid;
	private String userIds;
	
	
	public String getRequestid() {
		return requestid;
	}

	public void setRequestid(String requestid) {
		this.requestid = requestid;
	}

	public String getUserIds() {
		return userIds;
	}

	public void setUserIds(String userIds) {
		this.userIds = userIds;
	}

	public SendMessageService(HttpServletRequest request, HttpServletResponse response){
		this.request = request;
		this.response = response;
	}
	
	public SendMessageService(){
		
	}
	
	public SendMessageService(String requestid,String userIds){
		this.requestid = requestid;
		this.userIds = userIds;
	}
	
	public void run() {
		
		try {
			long currenttime=System.currentTimeMillis();
			baseBean.writeLog("starttime:"+ currenttime);
			long sendsleepmillis=Util.getIntValue(Util.null2String(Prop.getPropValue("logindoor","sendsleepmillis")).trim(),5000);
			baseBean.writeLog("sendsleepmillis:"+sendsleepmillis);
			Thread.sleep(sendsleepmillis);
			long currenttime2=System.currentTimeMillis();
			baseBean.writeLog("endtime:"+ currenttime2+"间隔时间："+(currenttime2-currenttime)+" mm");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendMessageBatch(this.requestid, this.userIds);
	}
	
	public void sendMessageBatch(String requestid,String userIds){
		RecordSet rs = new RecordSet();
		rs.executeSql("select workflowid,requestname,requestnamenew from workflow_requestbase where requestid='"+requestid+"'");
		if(rs.next()){
			String workflowid = Util.null2String(rs.getString("workflowid"));
			String requestname = Util.null2String(rs.getString("requestnamenew"));
			String sendmessageworkflowid=Util.null2String(Prop.getPropValue("logindoor","sendmessageworkflowid")).trim();
			if((","+sendmessageworkflowid+",").indexOf((","+workflowid+","))==-1){
				return ;
			}else{
				try {
					this.sendMessage(requestname, requestid, userIds);
				} catch (Exception e) {
					baseBean.writeLog("发送手机助手信息失败："+e.getMessage());
				}
			}
		}
	}
	
	public void sendMessage(int type,String requestname,int requestid,int userid){
		baseBean.writeLog("20180917>sendMessage===type:"+type+" requestname:"+requestname+" requestid:"+requestid+" userid:"+userid);
		if (type == 0 || type == 1 || type == 10 || type == 14) {//流程提醒
			try{
				sendMessage(requestname, requestid+"", userid+"");
			} catch (Exception e) {
				baseBean.writeLog("发送手机助手信息失败："+e.getMessage());
			}
		}
	}
	
	public static void main(String[] args) throws InterruptedException{
		long currenttime=System.currentTimeMillis();
		System.out.println("starttime:"+ currenttime);
		long sendsleepmillis=Util.getIntValue(Util.null2String(Prop.getPropValue("logindoor","sendsleepmillis")).trim(),15000);
		System.out.println("sendsleepmillis:"+sendsleepmillis);
		Thread.sleep(sendsleepmillis);
		long currenttime2=System.currentTimeMillis();
		System.out.println("endtime:"+ currenttime2+"间隔时间："+(currenttime2-currenttime)+" mm");
	}
	
	/**
	 * @param requestname 流程标题
	 * @param requestid   流程id
	 * @param userIds     需要提醒的用户id 多个以逗号隔开
	 * @return
	 * @throws Exception
	 */
	public void sendMessage(String requestname,String requestid,
			String userIds) throws Exception{
		ResourceComInfo rci = new ResourceComInfo();
		String msgTime = TimeUtil.getCurrentTimeString();
		RecordSet rs = new RecordSet();
		rs.executeSql("select t1.workflowid,t1.requestname,t1.requestnamenew from workflow_requestbase t1 where requestid='"+requestid+"'");
		String requestnamenew = "";
		String workflowid = "";
		if(rs.next()){
			workflowid= Util.null2String(rs.getString("workflowid"));
			requestnamenew= Util.null2String(rs.getString("requestnamenew"));
		}
		this.baseBean.writeLog("select workflowid,requestname,requestnamenew from workflow_requestbase where requestid='"+requestid+"'");
		this.baseBean.writeLog("requestnamenew:"+requestnamenew);
		String url = "/mobile/plugin/1/view.jsp?detailid="+requestid;
		String detailUrl=Util.null2String(Prop.getPropValue("logindoor","appsend_detailUrl"));
		url=detailUrl+requestid;
		url = url.replaceAll("%7Bworkflowid%7D", workflowid);
		url = url.replaceAll("%7Brequestid%7D", requestid);
		
		String content = "您有一个流程【"+requestnamenew+"】需要处理，请点击本信息进行处理";
		String loginids = "";
		String[] userIdArray = userIds.split(",");
		for (int i = 0; i < userIdArray.length; i++) {
			String userid = Util.null2String(userIdArray[i]);
			if(userid.length()>0){
				String loginid = rci.getLoginID(userid);
				loginids+=","+loginid;
			}
		}
		if(loginids.length()>0){
			loginids = loginids.substring(1);
		}
		JSONObject json = sendMessage(requestnamenew, content, requestid, loginids, msgTime, url);
		baseBean.writeLog("20180917>sendMessage===return json:"+json);
		this.returnValue(json, response);
	}

	/**
	 * 	短信接口	http://10.10.20.36:7772/web/bqzs/sendMessage	
		List<PushMessageVo> notices	
	 	title	标题	管理类项目采购_常规采购
		content	推送内容	内容
		targetId	业务数据ID	OA的流程requestid
		msgType	消息类型	流程通知（双方约定，待后续提供正确的值）
		userId	消息接收人，传登录账号（只能单人，多个人，请list里传多条记录）	pengwang
		userName	消息接收人姓名	王鹏
		msgTime	发送时间	2018-01-26 17:35:26
		url	跳转路径	/mobile/plugin/1/view.jsp?detailid=288890
	 */
	public JSONObject sendMessage(String title,String content,String targetId,
		String userIds,String msgTime,String url){
		String targetUrl=Util.null2String(Prop.getPropValue("logindoor","appsend_targetUrl"));
		
		JSONObject json = new JSONObject();
		json.put("appId", Util.null2String(Prop.getPropValue("logindoor","appsend_appId")));
		JSONObject message = new JSONObject();
		message.put("title", title);
		message.put("content", content);
		message.put("showBadge", true);
		
		JSONObject playloads = new JSONObject();
		playloads.put("msgType", "2");
		playloads.put("url", url);
		message.put("payloads", playloads);
		
		JSONObject filter = new JSONObject();
		JSONArray userId_json = new JSONArray();
		
		String[] userIdArray = userIds.split(",");
		for (int i = 0; i < userIdArray.length; i++) {
			userId_json.add(userIdArray[i]);
		}
		
		filter.put("userId", userId_json);
		json.put("message", message);
//		json.put("playloads", playloads);
		json.put("filter", filter);
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("userIds", userIds);
		String returnString="";
		try {
			baseBean.writeLog("20180917>sendPost>>targetUrl:"+targetUrl+" json:"+json.toString());
			returnString = sendPost(targetUrl, json);
		} catch (Exception e) {
			e.printStackTrace();
			jsonObject.put("errorMessage", e.getMessage());
		}
		jsonObject.put("message", "发消息推送反馈内容："+returnString);
		return json;
	}
	
	 /**
     * 向指定URL发送GET方法的请求
     * 
     * @param url
     *            发送请求的URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
	 * @throws Exception 
     */
    public String sendGet(String url, String param) throws Exception {
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url + "?" + param;
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            for (String key : map.keySet()) {
                System.out.println(key + "--->" + map.get(key));
            }
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
            throw new Exception("发送GET请求出现异常！" + e.getMessage());
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }
    
    /**
     * 向指定 URL 发送POST方法的请求
     * 
     * @param url
     *            发送请求的 URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     * @throws Exception 
     */
    public String sendPost(String url1, JSONObject obj) throws Exception {
    	try {
            //创建连接 
//            URL url = new URL(url1);
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            connection.setDoOutput(true);
//            connection.setDoInput(true);
//            connection.setRequestMethod("POST");
//            connection.setUseCaches(false);
//            connection.setInstanceFollowRedirects(true);
//            connection.setRequestProperty("Content-Type", "application/json");
//            connection.connect();
//            String json = java.net.URLEncoder.encode(obj.toString(), "utf-8");
//            PrintWriter pw = null ;
//            BufferedReader rd = null ;
//            StringBuilder sb = new StringBuilder ();
//            String line = null ;
//            String response = null;
//            /*
//            // POST请求 
//            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
//            
//            
//            out.writeBytes(json);
//            out.flush();
//            out.close();
//            // 读取响应 
//            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//            String lines;
//            StringBuffer sb = new StringBuffer("");
//            while ((lines = reader.readLine()) != null) {
//                lines = URLDecoder.decode(lines, "utf-8");
//                sb.append(lines);
//            }
//            reader.close();
//            // 断开连接 
//            connection.disconnect(); 
//            return sb.toString();
//            */
//            pw = new PrintWriter(connection.getOutputStream());
//            pw.print(json);
//            pw.flush();
//            rd  = new BufferedReader( new InputStreamReader(connection.getInputStream(), "UTF-8"));
//            while ((line = rd.readLine()) != null ) {
//                sb.append(line);
//            }
//            response = sb.toString();
            
            String response= httpJsonPost(url1, obj.toString(),5000, 5000, "application/json;charset=UTF-8");
            System.out.println("发送POST请求返回结果："+response);
            baseBean.writeLog("发送POST请求返回结果："+response);
    		return response;
        } catch (Exception e){
            e.printStackTrace(); 
            throw new Exception("发送POST请求出现异常！" + e.getMessage());
        }
    }    
    
    public String httpJsonPost(String httpUrl, String data, int connectTimeout, int readTimeout, String contentType) throws IOException {
        OutputStream outPut = null;
        HttpURLConnection urlConnection = null;
        InputStream in = null;

        try {
            URL url = new URL(httpUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestProperty("Content-Type", contentType);
            urlConnection.setConnectTimeout(connectTimeout);
            urlConnection.setReadTimeout(readTimeout);
            urlConnection.connect();

            // POST data
            outPut = urlConnection.getOutputStream();
            outPut.write(data.getBytes("UTF-8"));
            outPut.flush();

            // read response
            if (urlConnection.getResponseCode() < 400) {
                in = urlConnection.getInputStream();
            } else {
                in = urlConnection.getErrorStream();
            }

            List<String> lines = IOUtils.readLines(in, urlConnection.getContentEncoding());
            StringBuffer strBuf = new StringBuffer();
            for (String line : lines) {
                strBuf.append(line);
            }
            return strBuf.toString();
        } finally {
            IOUtils.closeQuietly(outPut);
            IOUtils.closeQuietly(in);
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
    
    public void returnValue(JSONObject json ,HttpServletResponse response){
		if(response==null){
			return;
		}
    	response.setContentType("application/json; charset=utf-8");  
		try {
			response.getWriter().print(json.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	
	public void returnValue(JSONArray jsonArray ,HttpServletResponse response){
		if(response==null){
			return;
		}
    	response.setContentType("application/json; charset=utf-8");  
		try {
			response.getWriter().print(jsonArray.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

}
