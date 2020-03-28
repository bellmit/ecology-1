package weaver.interfaces.ehr;

import java.util.Date;

import com.alibaba.fastjson.JSONObject;

public interface InterfaceService {
	public String bmInterface(String json);
//	public String bmInterface(String depCode,String depName);

	public String gwInterface(String json);

	public String hrumeInterface(String json);
}
