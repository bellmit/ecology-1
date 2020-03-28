package weaver.sysinterface;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import weaver.general.BaseBean;
public class BaseService extends BaseBean {
	
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