package weaver.page.maint;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.hrm.User;
import weaver.hrm.company.DepartmentComInfo;
import weaver.hrm.company.SubCompanyComInfo;
import weaver.hrm.companyvirtual.ResourceVirtualComInfo;
import weaver.hrm.resource.ResourceComInfo;
import weaver.hrm.roles.RolesComInfo;
import weaver.page.menu.MenuShareCominfo;
import weaver.systeminfo.setting.HrmUserSettingComInfo;
import weaver.systeminfo.systemright.CheckSubCompanyRight;

/**
 * 菜单基础方法工具类
 * @author hqf
 *
 */
public class MenuUtil extends BaseBean {
	
	/**
	 * 获取菜单的josn信息对象
	 * @param menutype
	 * @param parentid
	 * @param userId
	 * @param subCompanyId
	 * @param hasRight
	 * @return
	 */
	public JSONArray getMenuJsonStr(String menutype,int parentid,String userId,String subCompanyId,boolean hasRight){
		//boolean hasRight=false;
		RecordSet rs=new RecordSet();
		JSONArray children=new JSONArray();		
		
		rs.executeSql("select * from menucustom where menutype='"+menutype+"' and menuparentid="+parentid+" order by cast( menuindex as int)");
		while(rs.next()){
			JSONObject jsonObj=new JSONObject() ;			
			try {
				jsonObj.put("id","sys_"+rs.getInt("id"));
				jsonObj.put("text","<font id='fontmenuname_sys_"+rs.getInt("id")+"'>"+rs.getString("menuname")+"</font>");
				jsonObj.put("target",rs.getString("menutarget"));
				jsonObj.put("href",Util.StringReplace(rs.getString("menuhref"), "\\", "/"));
				jsonObj.put("hrefTarget","_blank");
				jsonObj.put("icon",rs.getString("menuIcon"));
				jsonObj.put("righttype",rs.getString("righttype"));
				jsonObj.put("rightvalue",rs.getString("rightvalue"));
				jsonObj.put("righttext",getMenuRightText(rs.getString("rightvalue"),rs.getString("righttype")));
				jsonObj.put("sharetype",rs.getString("sharetype"));
				jsonObj.put("sharevalue",rs.getString("sharevalue"));
				jsonObj.put("sharetext",getMenuShareRightText(rs.getString("sharevalue"), rs.getString("sharetype")));
				jsonObj.put("parentid",rs.getString("menuparentid"));
				jsonObj.put("leaf",!isHaveChild(menutype,rs.getInt("id")));
				//TODO
				// 用户是否有权限设置此节点菜单
				boolean tmp = false;
				if(!hasRight){
					tmp = hasRight(userId,subCompanyId,rs.getString("rightvalue"),rs.getString("righttype"));
				}else{
					tmp = true;
				}
				jsonObj.put("draggable",tmp);
				jsonObj.put("allowDrop",tmp);
				jsonObj.put("allowDrag",tmp);
				jsonObj.put("allowChildren",tmp);
				jsonObj.put("hasright",tmp);
				// 循环组织菜单结构
				jsonObj.put("children", getMenuJsonStr(menutype,rs.getInt("id"),userId,subCompanyId,tmp));
				if(parentid==0){
					jsonObj.put("expanded",true);
				}
			} catch (JSONException e) {
				writeLog(e);
				e.printStackTrace();
			}			
			children.put(jsonObj);
		}
		return children;
	} 
	
	/**
	 * 获取横向菜单信息
	 * @param menutype 菜单标识
	 * @param parentid 父菜单标识
	 * @return
	 */
	public String getMenuTableStr_H(String menutype,int parentid){
		return getMenuTableStr_H(menutype, parentid,0);
	}
	
	/**
	 * 获取横向菜单信息
	 * @param menutype 菜单标识
	 * @param parentid 父菜单标识
	 * @param user 用户信息
	 * @return
	 */
	public String getMenuTableStr_H(String menutype,int parentid,User user){
		return getMenuTableStr_H(menutype, parentid,0,user);
	}
	
	/**
	 * 获取横向菜单信息
	 * @param menutype 菜单表示
	 * @param parentid 父菜单表示
	 * @param opentype 菜单链接打开方式
	 * @return
	 */
	public String getMenuTableStr_H(String menutype, int parentid, int opentype){
		String returnStr="";
		RecordSet rs=new RecordSet();
		rs.executeSql("select * from menucustom where menutype='"+menutype+"' and menuparentid="+parentid+" order by cast( menuindex as int)");
		if(rs.getCounts()>0) returnStr+="<ul>\n";
		while(rs.next()){
			String className="main";
			if(parentid>0) className="sub"; 
			if(opentype==2){
				if(Util.urlAddPara(rs.getString("menuhref"),"isfromportal=1&menutype="+menutype+"&menuparentid="+parentid+"&menuindex="+rs.getString("menuindex")).equals("#")){
					returnStr+="<li><a href='#'  class='"+className+"'>"+rs.getString("menuname").replaceAll("#", "&nbsp;")+"</a>\n";
				}else{
					returnStr+="<li><a href='"+Util.urlAddPara(rs.getString("menuhref"),"isfromportal=1&menutype="+menutype+"&menuparentid="+parentid+"&menuindex="+rs.getString("menuindex"))+"'  target='"+rs.getString("menuTarget")+"' class='"+className+"'>"+rs.getString("menuname").replaceAll("#", "&nbsp;")+"</a>\n";
				}
						
			}else{
				if(Util.urlAddPara(rs.getString("menuhref"),"isfromportal=1&menutype="+menutype+"&menuparentid="+parentid+"&menuindex="+rs.getString("menuindex")).equals("#")){
					returnStr+="<li><a href='#'  class='"+className+"'>"+rs.getString("menuname").replaceAll("#", "&nbsp;")+"</a>\n";
				}else{
					returnStr+="<li><a href='"+Util.urlAddPara(rs.getString("menuhref"),"isfromportal=1&menutype="+menutype+"&menuparentid="+parentid+"&menuindex="+rs.getString("menuindex"))+"' target='"+rs.getString("menuTarget")+"' class='"+className+"'>"+rs.getString("menuname").replaceAll("#", "&nbsp;")+"</a>\n";
				}
				
			}
			returnStr+=getMenuTableStr_H(menutype,rs.getInt("id"),opentype);
			returnStr+="</li>\n";
			
			
		}		
		if(rs.getCounts()>0) returnStr+="</ul>\n";
		return returnStr;
	}
	
	/**
	 * 获取横向菜单信息
	 * @param menutype
	 * @param parentid
	 * @param opentype
	 * @param userid
	 * @return
	 */
	public String getMenuTableStr_H(String menutype, int parentid, int opentype, User user){
		String returnStr="";
		RecordSet rs=new RecordSet();
		rs.executeSql("select * from menucustom where menutype='"+menutype+"' and menuparentid="+parentid+" order by cast( menuindex as int)");
		if(rs.getCounts()>0) returnStr+="<ul>\n";
		while(rs.next()){
			if(!hasShareRight(user, rs.getString("sharetype"), rs.getString("sharevalue"),rs.getInt("id"),menutype)){
				continue;
			}
			String className="main";
			if(parentid>0) className="sub"; 
			if(opentype==2){
				if(Util.urlAddPara(rs.getString("menuhref"),"isfromportal=1&menutype="+menutype+"&menuparentid="+parentid+"&menuindex="+rs.getString("menuindex")).equals("#")){
					returnStr+="<li><a href='#' class='"+className+"'>"+rs.getString("menuname").replaceAll("#", "&nbsp;")+"</a>\n";
				}else{
					returnStr+="<li><a href='"+Util.urlAddPara(rs.getString("menuhref"),"isfromportal=1&menutype="+menutype+"&menuparentid="+parentid+"&menuindex="+rs.getString("menuindex"))+"'  target='"+rs.getString("menuTarget")+"' class='"+className+"'>"+rs.getString("menuname").replaceAll("#", "&nbsp;")+"</a>\n";					
				}
						
			}else{
				if(Util.urlAddPara(rs.getString("menuhref"),"isfromportal=1&menutype="+menutype+"&menuparentid="+parentid+"&menuindex="+rs.getString("menuindex")).equals("#")){
					returnStr+="<li><a href='#' class='"+className+"'>"+rs.getString("menuname").replaceAll("#", "&nbsp;")+"</a>\n";
				}else{
					returnStr+="<li><a href='"+Util.urlAddPara(rs.getString("menuhref"),"isfromportal=1&menutype="+menutype+"&menuparentid="+parentid+"&menuindex="+rs.getString("menuindex"))+"' target='"+rs.getString("menuTarget")+"' class='"+className+"'>"+rs.getString("menuname").replaceAll("#", "&nbsp;")+"</a>\n";					
				}
				
			}
			returnStr+=getMenuTableStr_H(menutype,rs.getInt("id"),opentype,user);
			returnStr+="</li>\n";
			
			
		}		
		if(rs.getCounts()>0) returnStr+="</ul>\n";
		return returnStr;
	}
	
	/**
	 * 获取纵向菜单内容信息
	 * @param menutype 菜单标识
	 * @return
	 */
	public String getMenuTableStr_V(String menutype){
		return getMenuTableStr_V(menutype, 0);
	}
	
	/**
	 * 获取纵向菜单内容信息
	 * @param menutype 菜单标识
	 * @param opentype 菜单链接打开方式
	 * @return
	 */
	public String getMenuTableStr_V(String menutype, int opentype){
		String returnStr="";
		RecordSet rs=new RecordSet();
		rs.executeSql("select * from menucustom where menutype='"+menutype+"' and menuparentid=0 order by cast( menuindex as int)");
		boolean isFirst=true;
		String menutarget="";
		while(rs.next()){
			menutarget = rs.getString("menutarget");
			if(isFirst){
				isFirst=false;
				returnStr+="<div class='mainBg_top'>\n";
			} else {
				returnStr+="<div class='mainBg'>\n";
			}
			if(Util.urlAddPara(rs.getString("menuhref"), "menutype="+menutype+"&menuindex="+rs.getString("menuindex")).equals("#")){
				returnStr+="<a class='mainFont' style='cursor:hand' href='#' >"+rs.getString("menuname").replaceAll("#", "&nbsp;")+"</a>\n";
			}else{
				returnStr+="<a class='mainFont' style='cursor:hand' href='"+Util.urlAddPara(rs.getString("menuhref"), "menutype="+menutype+"&menuindex="+rs.getString("menuindex"))+"' target='"+rs.getString("menuTarget")+"'>"+rs.getString("menuname").replaceAll("#", "&nbsp;")+"</a>\n";				
			}
			
			returnStr+=getMenuTableStr_V_Sub(menutype,rs.getInt("id"),opentype);
			returnStr+="</div>\n";
		}
		return returnStr;
	}
	
	/**
	 * 获取纵向菜单内容信息
	 * @param menutype 菜单标识
	 * @param opentype 菜单链接打开方式
	 * @param user 用户对象
	 * @return
	 */
	public String getMenuTableStr_V(String menutype, int opentype, User user){
		String returnStr="";
		RecordSet rs=new RecordSet();
		rs.executeSql("select * from menucustom where menutype='"+menutype+"' and menuparentid=0 order by cast( menuindex as int)");
		boolean isFirst=true;
		String menutarget="";
		while(rs.next()){
			if(!hasShareRight(user, rs.getString("sharetype"), rs.getString("sharevalue"),rs.getInt("id"),menutype)){
				continue;
			}
			menutarget = rs.getString("menutarget");
			if(isFirst){
				isFirst=false;
				returnStr+="<div class='mainBg_top'>\n";
			} else {
				returnStr+="<div class='mainBg'>\n";
			}
			if(Util.urlAddPara(rs.getString("menuhref"), "menutype="+menutype+"&menuindex="+rs.getString("menuindex")).equals("#")){
				returnStr+="<a class='mainFont' style='cursor:hand' href='#' >"+rs.getString("menuname").replaceAll("#", "&nbsp;")+"</a>\n";
			}else{
				returnStr+="<a class='mainFont' style='cursor:hand' href='"+Util.urlAddPara(rs.getString("menuhref"), "menutype="+menutype+"&menuindex="+rs.getString("menuindex"))+"' target='"+rs.getString("menuTarget")+"'>"+rs.getString("menuname").replaceAll("#", "&nbsp;")+"</a>\n";
			}
			
			returnStr+=getMenuTableStr_V_Sub(menutype,rs.getInt("id"),opentype,user);
			returnStr+="</div>\n";
		}
		return returnStr;
	}
	
	/**
	 * 获取纵向菜单子菜单内容
	 * @param menutype 菜单标识
	 * @param parentid 父菜单标识
	 * @param opentype 菜单链接打开方式
	 * @return
	 */
	public String getMenuTableStr_V_Sub(String menutype,int parentid, int opentype){
		String returnStr="";
		RecordSet rs=new RecordSet();
		rs.executeSql("select * from menucustom where menutype='"+menutype+"' and menuparentid="+parentid+" order by cast( menuindex as int)");
		boolean isFirst=true;
		while(rs.next()){	
			if(opentype==2){
				if(Util.urlAddPara(rs.getString("menuhref"), "menutype="+menutype+"&menuparentid="+parentid+"&menuindex="+rs.getString("menuindex")).equals("#")){
					returnStr+="<a href='#' class='sub' >"+rs.getString("menuname")+"</a>\n";
				}else{
					returnStr+="<a href='"+Util.urlAddPara(rs.getString("menuhref"), "menutype="+menutype+"&menuparentid="+parentid+"&menuindex="+rs.getString("menuindex"))+"' target='"+rs.getString("menuTarget")+"' class='sub' >"+rs.getString("menuname")+"</a>\n";
				}
				
			}else{
				if(Util.urlAddPara(rs.getString("menuhref"), "menutype="+menutype+"&menuparentid="+parentid+"&menuindex="+rs.getString("menuindex")).equals("#")){
					returnStr+="<a href='#' class='sub' >"+rs.getString("menuname")+"</a>\n";
				}else{
					returnStr+="<a href='"+Util.urlAddPara(rs.getString("menuhref"), "menutype="+menutype+"&menuparentid="+parentid+"&menuindex="+rs.getString("menuindex"))+"' target='"+rs.getString("menuTarget")+"' class='sub'>"+rs.getString("menuname")+"</a>\n";
				}
				
			}
			
		}
		return returnStr;
	}
	
	/**
	 * 获取纵向菜单子菜单内容
	 * @param menutype 菜单标识
	 * @param parentid 父菜单标识
	 * @param opentype 菜单链接打开方式
	 * @param user 用户对象
	 * @return
	 */
	public String getMenuTableStr_V_Sub(String menutype,int parentid, int opentype, User user){
		String returnStr="";
		RecordSet rs=new RecordSet();
		rs.executeSql("select * from menucustom where menutype='"+menutype+"' and menuparentid="+parentid+" order by cast( menuindex as int)");
		boolean isFirst=true;
		while(rs.next()){
			if(!hasShareRight(user, rs.getString("sharetype"), rs.getString("sharevalue"),rs.getInt("id"),menutype)){
				continue;
			}
			if(opentype==2){
				if(Util.urlAddPara(rs.getString("menuhref"), "menutype="+menutype+"&menuparentid="+parentid+"&menuindex="+rs.getString("menuindex")).equals("#")){
					returnStr+="<a href='#' class='sub' >"+rs.getString("menuname")+"</a>\n";
				}else{
					returnStr+="<a href='"+Util.urlAddPara(rs.getString("menuhref"), "menutype="+menutype+"&menuparentid="+parentid+"&menuindex="+rs.getString("menuindex"))+"' target='"+rs.getString("menuTarget")+"' class='sub' >"+rs.getString("menuname")+"</a>\n";
				}
			}else{
				
				if(Util.urlAddPara(rs.getString("menuhref"), "menutype="+menutype+"&menuparentid="+parentid+"&menuindex="+rs.getString("menuindex")).equals("#")){
					returnStr+="<a href='#' class='sub' >"+rs.getString("menuname")+"</a>\n";
				}else{
					returnStr+="<a href='"+Util.urlAddPara(rs.getString("menuhref"), "menutype="+menutype+"&menuparentid="+parentid+"&menuindex="+rs.getString("menuindex"))+"' target='"+rs.getString("menuTarget")+"' class='sub'>"+rs.getString("menuname")+"</a>\n";
				}
				
			}
			
		}
		return returnStr;
	}
	
	/**
	 * 是否有子菜单
	 * @param menutype 菜单标识
	 * @param parentid 父菜单标识
	 * @return
	 */
	private boolean isHaveChild(String menutype,int parentid){
		RecordSet rs=new RecordSet();
		rs.executeSql("select count(*) from menucustom where menutype='"+menutype+"' and menuparentid="+parentid);		
		if(rs.next()){
			if(rs.getInt(1)>0) return true;
		}
		return false;
	}
	
	/**
	 * 获取菜单的权限设置信息
	 * @param rightValue
	 * @param rightType
	 * @return
	 */
	private String getMenuRightText(String rightValue, String rightType){
		String rightText="";
		if("0".equals(rightType)){
			ResourceComInfo rc = null;
			try {
				rc = new ResourceComInfo();
			} catch (Exception ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
			ArrayList idList = Util.TokenizerString(rightValue, ",");
			for(int i=0;i<idList.size();i++){
				rightText+="&nbsp;<a onclick='pointerXY(event);' href='javascript:openhrm("+idList.get(i)+");'>"+rc.getResourcename((String)idList.get(i))+"</a>";
			}
		}
		return rightText;
	}
	
	/**
	 * 获取菜单的共享设置信息
	 * @param shareValue
	 * @param Sharetype
	 * @return
	 */
	public String getMenuShareRightText(String shareValue, String shareType){
		String returnStr="";
    	ResourceComInfo rc = null;
    	DepartmentComInfo dci=null;
    	SubCompanyComInfo scc=null;
    	RolesComInfo roc = null;
 
    	try {
			rc = new ResourceComInfo();
			dci = new DepartmentComInfo();
			scc = new SubCompanyComInfo();
			roc = new RolesComInfo();
		} catch (Exception ex) {
			writeLog(ex);
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
    	if(shareValue.equals("")){
    		shareValue = "1";
    		shareType ="5";
    	}
    	ArrayList shareList = Util.TokenizerString(shareValue, "$");
    	ArrayList typeList = Util.TokenizerString(shareType, "$");
    	for(int i=0; i<shareList.size();i++){
    		shareValue = (String)shareList.get(i);
			int type = Util.getIntValue((String)typeList.get(i));
			
			String[] contentList = Util.TokenizerString2(Util.TokenizerString2(shareValue, "_")[0],",");
			switch(type){
			case 1://人力资源
				for(int j=0;j<contentList.length;j++){
	    			returnStr +="<a href='/hrm/resource/HrmResource.jsp?id="+contentList[j]+"' target='_blank'>"+rc.getResourcename(""+contentList[j])+"</a>&nbsp;";
	    		}
				break;
			case 2://分部
				for(int j=0;j<contentList.length;j++){
	    			returnStr +="<a href=\"/hrm/company/HrmDepartment.jsp?companyid=1&subcompanyid="+contentList[j]+"\" target='_blank'>"+scc.getSubCompanyname(""+contentList[j])+"</a>&nbsp;";
	    		}
				break;
			case 3://部门
				for(int j=0;j<contentList.length;j++){
	    			returnStr +="<a href=\"/hrm/company/HrmDepartmentDsp.jsp?id="+contentList[j]+"\">"+dci.getDepartmentname(""+contentList[j])+"</a>&nbsp;";
	    		}
				break;
			case 5://所有人
				break;
			case 6: //角色
				String[] list = Util.TokenizerString2(shareValue,"_");
				returnStr +=roc.getRolesRemark(list[0]);
				break;
			case 7://安全级别
				break;
			}
			returnStr =returnStr+"$";
    	}
    	if(!returnStr.equals("")){
    		returnStr = returnStr.substring(0,returnStr.length()-1);
    	}
    	return returnStr;
	}
	
	/**
	 * 判断用户是否有该菜单节点的查看权限（此方法作废）
	 * @param userid
	 * @param sharetype
	 * @param sharevalue
	 * 
	 * @return
	 */
	public boolean hasShareRight(User user, String sharetype, String sharevalue){
		if(user==null||user.getUID()==1){
			return true;
		}
		boolean hasRight = false;
		int securitylevel;
		int operate=0; // 0:>=  1:<=
    	ResourceComInfo rc = null;
    	try {
			rc = new ResourceComInfo();

		} catch (Exception ex) {
			writeLog(ex);
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
		ArrayList typeList = Util.TokenizerString(sharetype, "$");
		ArrayList valueList = Util.TokenizerString(sharevalue, "$");
		for(int i=0;i<valueList.size();i++){
			sharetype = (String)typeList.get(i);
			sharevalue = (String)valueList.get(i);
			ArrayList contentList = Util.TokenizerString(Util.TokenizerString2(sharevalue, "_")[0],",");
			switch(Util.getIntValue(sharetype)){
			case 1://人力资源
				if(contentList.indexOf(user.getUID()+"")!=-1){
					hasRight = true;
				}
				break;
			case 2://分部
				operate = Util.getIntValue(Util.TokenizerString2(sharevalue, "_")[1]);
				securitylevel = Util.getIntValue(Util.TokenizerString2(sharevalue, "_")[2]);
				if(operate==0){
					if(contentList.indexOf(rc.getSubCompanyID(user.getUID()+""))!=-1&&Util.getIntValue(user.getSeclevel())>=securitylevel){
						hasRight = true;
					}
				}else if(operate==1){
					if(contentList.indexOf(rc.getSubCompanyID(user.getUID()+""))!=-1&&Util.getIntValue(user.getSeclevel())<=securitylevel){
						hasRight = true;
					}
				}
				
				break;
			case 3://部门
				operate = Util.getIntValue(Util.TokenizerString2(sharevalue, "_")[1]);
				securitylevel = Util.getIntValue(Util.TokenizerString2(sharevalue, "_")[2]);
				if(operate==0){
					if(contentList.indexOf(rc.getDepartmentID(user.getUID()+""))!=-1&&Util.getIntValue(user.getSeclevel())>=securitylevel){
						hasRight = true;
					}
				}else if(operate==1){
					if(contentList.indexOf(rc.getDepartmentID(user.getUID()+""))!=-1&&Util.getIntValue(user.getSeclevel())<=securitylevel){
						hasRight = true;
					}
				}
				break;
			case 5://所有人
				hasRight=true;
				break;
			case 6://角色
				String[] temp =Util.TokenizerString2(sharevalue, "_");
				String rolevalue = temp[0];
				String roletype = temp[1];
				operate = Util.getIntValue(temp[2]);
				securitylevel = Util.getIntValue(temp[3]);
				RecordSet rs = new RecordSet();
				rs.execute("select count(id) as countid from hrmrolemembers where roleid="+rolevalue+" and resourceid="+user.getUID()+" and rolelevel >= "+roletype);
				if(rs.next()){
					if(operate==0){
						if(rs.getInt("countid")>0&&Util.getIntValue(user.getSeclevel())>=securitylevel){
							hasRight = true;
						}
					}else if(operate==1){
						if(rs.getInt("countid")>0&&Util.getIntValue(user.getSeclevel())<=securitylevel){
							hasRight = true;
						}
					}
				}
				break;
			case 7://安全级别
				operate = Util.getIntValue(Util.TokenizerString2(sharevalue, "_")[0]);
				
				securitylevel =  Util.getIntValue(Util.TokenizerString2(sharevalue, "_")[1]);
				if(operate==0){
					if(Util.getIntValue(user.getSeclevel())>=securitylevel){
						hasRight = true;
					}
				}else if(operate==1){
					if(Util.getIntValue(user.getSeclevel())<=securitylevel){
						hasRight = true;
					}
				}
				
				break;
			}
			
			//如果用户满足一个权限，则停止判断
			if(hasRight){
				return hasRight;
			}
		}
		return hasRight;
	}
	
	
	public boolean hasShareRight(User user, String sharetype, String sharevalue,int menuid,String costomid){
		if(user==null||user.getUID()==1){
			return true;
		}
		
		return hasShareRight(menuid,1,3,user,costomid);
	}
	
	public boolean hasShareRight(int infoid,int resourceid,int resourcetype,User user,String costomid){
		boolean flag = false;
		
		//RecordSet rs = new RecordSet();
		ResourceComInfo rc = null;
		ResourceVirtualComInfo rvc = null;
    	try {
			rc = new ResourceComInfo();
			rvc = new ResourceVirtualComInfo();

		} catch (Exception ex) {
			//writeLog(ex);
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
		
		MenuShareCominfo msc = new MenuShareCominfo();
		
		
		 //rs.executeSql("select sharetype,sharevalue,seclevel,rolelevel from menushareinfo where infoid ='"+infoid+"' and resourceid='"+resourceid+"' and resourcetype='"+resourcetype+"' and menutype='"+menuType+"'");
		 if(msc.getAssetNum()>0){
			 msc.setTofirstRow();
			 int count =0;
			 while(msc.next()){
				 
				 int m_infoid = Util.getIntValue(msc.getInfoId());
				 int m_resourcetype = Util.getIntValue(msc.getResourcetype());
				 int m_resourceid = Util.getIntValue(msc.getResourceid());
				 String m_menutype = msc.getMenutype();
				 String m_costomid = msc.getCustomid();
				 
				 if(m_infoid!=infoid||!m_costomid.equals(costomid)||!m_menutype.equals("custom")){
					
					 continue;
				 }
				 
				 count++;
				
				 int sharetype = Util.getIntValue(msc.getSharetype());
				 String sharevalue = msc.getSharevalue();
				 String seclevel =  msc.getSeclevel();
				 String rolelevel =  msc.getRolelevel();
				
				 int minlevel=0;
				 int maxlevel = 100;
				 switch(sharetype){
				 case 5: // 人力资源
						if(user.getUID()==Util.getIntValue(sharevalue)){
							flag = true;
						}
						break;
					case 6: // 分部
						 minlevel = Util.getIntValue(Util.TokenizerString2(seclevel, "-")[0]);
						 maxlevel = Util.getIntValue(Util.TokenizerString2(seclevel, "-")[1]);
						 String subids = rvc.getSubcompanyids(user.getUID()+"");
						 ArrayList subidList  = Util.TokenizerString(subids,",");
						 for(int i=0;i<subidList.size();i++){
							String subid = (String)subidList.get(i);
							if(subid.equals(sharevalue)&&Util.getIntValue(user.getSeclevel())>=minlevel&&Util.getIntValue(user.getSeclevel())<=maxlevel){
									flag = true;
									break;
							}
						 }
						
						break;
					case 1: // 部门
						minlevel = Util.getIntValue(Util.TokenizerString2(seclevel, "-")[0]);
						maxlevel = Util.getIntValue(Util.TokenizerString2(seclevel, "-")[1]);
						
						 String departids = rvc.getDepartmentids(user.getUID()+"");
						 ArrayList departList  = Util.TokenizerString(departids,",");
						 for(int i=0;i<departList.size();i++){
							String departid = (String)departList.get(i);
							if(departid.equals(sharevalue)&&Util.getIntValue(user.getSeclevel())>=minlevel&&Util.getIntValue(user.getSeclevel())<=maxlevel){
									flag = true;
									break;
							}
						 }
						
						break;
					case 2: // 角色
						minlevel = Util.getIntValue(Util.TokenizerString2(seclevel, "-")[0]);
						maxlevel = Util.getIntValue(Util.TokenizerString2(seclevel, "-")[1]);
					
						RecordSet rsRole = new RecordSet();
						rsRole.execute("select count(id) as countid from hrmrolemembers where roleid="+sharevalue+" and resourceid="+user.getUID()+" and rolelevel >= "+rolelevel);
						rsRole.next();
						if(rsRole.getInt("countid")>0){
							if(Util.getIntValue(user.getSeclevel())>=minlevel&&Util.getIntValue(user.getSeclevel())<=maxlevel){
								flag = true;
							}
						}
						break;
						
					case 3: // 所有人（安全级别）
						minlevel = Util.getIntValue(Util.TokenizerString2(seclevel, "-")[0]);
						maxlevel = Util.getIntValue(Util.TokenizerString2(seclevel, "-")[1]);
					
						if(Util.getIntValue(user.getSeclevel())>=minlevel&&Util.getIntValue(user.getSeclevel())<=maxlevel){
							flag = true;
						}
						break;
				 }
			 }
			 
			 if(count==0){
				 flag = true;
			 }
		 }else{
			 flag = true;
		 }
	
		
		return flag;
	}
	
	public boolean hasShareRight(int infoid,int resourceid,int resourcetype, User user){
		try{
			HrmUserSettingComInfo userSetting = new HrmUserSettingComInfo();
			
			String belongtoshow = userSetting.getBelongtoshowByUserId(user.getUID()+"");
	   		
	   		if(belongtoshow.equals("1")&&"0".equals(user.getAccount_type())){
	   			String belongtoids = user.getBelongtoids();
	   			List<User> userList = user.getBelongtoUsersByUserId(user.getUID());
	   			for(int i=0;i<userList.size();i++){
	   				if(hasShareRight(infoid,resourceid,resourcetype,userList.get(i))){
	   					return true;
	   				}
	   			}
	   			return hasShareRight(infoid,resourceid,resourcetype,user);
	   		}else{
	   			return hasShareRight(infoid,resourceid,resourcetype,user);
	   		}
		}catch (Exception e) {	
			writeLog(e);
			return hasShareRight(infoid,resourceid,resourcetype,user);
			
		}
		
	}
	
	/**
	 * 获取用户默认的菜单链接地址
	 * @param menuid
	 * @param user
	 * @return
	 */
	public String getDefaultMenuLink(String menuid, User user){
		String returnStr="";
		RecordSet rs = new RecordSet();
		rs.executeSql("select * from menucustom where menutype='"+menuid+"' and menuparentid=0 order by cast( menuindex as int)");
		while(rs.next()){
			if(hasShareRight(user, rs.getString("sharetype"), rs.getString("sharevalue"),rs.getInt("id"),rs.getString("menutype"))){
				returnStr = rs.getString("menuhref");
				break;
			}
		}
		return returnStr;
	}
	
	/**
	 * 是否有权限设置菜单
	 * @param userId
	 * @param subCompanyId
	 * @param rightvalue
	 * @param righttype
	 * @return
	 */
	private boolean hasRight(String userId,String subCompanyId,String rightvalue,String righttype){
		boolean hasRight= false;
		CheckSubCompanyRight cscr=new CheckSubCompanyRight();
		int opreateLevel=cscr.ChkComRightByUserRightCompanyId(Util.getIntValue(userId),"homepage:Maint",Util.getIntValue(subCompanyId));
		if(!"1".equals(righttype)){
			if("1".equals(userId)||opreateLevel>0){
				hasRight = true;
			}else{
				ArrayList idList = Util.TokenizerString(rightvalue, ",");
				if(idList.indexOf(userId)!=-1){
					hasRight = true;
				}
			}
		}
		return hasRight;
	}
	
}
