package weaver.formmode.servelt;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import weaver.conn.RecordSet;
import weaver.formmode.EncoderHandler;
import weaver.formmode.search.FormModeTransMethod;
import weaver.formmode.service.ExpandInfoService;
import weaver.formmode.setup.ExpandBaseRightInfo;
import weaver.formmode.virtualform.VirtualFormHandler;
import weaver.general.Util;
import weaver.hrm.HrmUserVarify;
import weaver.hrm.User;
import weaver.workflow.form.FormManager;

public class QRcodeBuildAction extends HttpServlet
{
  private static final long serialVersionUID = 1L;

  public void service(HttpServletRequest paramHttpServletRequest, HttpServletResponse paramHttpServletResponse)
    throws ServletException, IOException
  {
    String str1 = Util.null2String(paramHttpServletRequest.getParameter("modeid"));
    String str2 = Util.null2String(paramHttpServletRequest.getParameter("formid"));
    String str3 = Util.null2String(paramHttpServletRequest.getParameter("billid"));
    String str4 = Util.null2String(paramHttpServletRequest.getParameter("customid"));
    User localUser = HrmUserVarify.getUser(paramHttpServletRequest, paramHttpServletResponse);
    ExpandInfoService localExpandInfoService = new ExpandInfoService();

    ExpandBaseRightInfo localExpandBaseRightInfo = new ExpandBaseRightInfo();
    localExpandBaseRightInfo.setUser(localUser);

    String str5 = null;
    String str6 = "id";
    boolean bool = VirtualFormHandler.isVirtualForm(str2);
    Object localObject1 = new HashMap();
    if (bool) {
      localObject1 = VirtualFormHandler.getVFormInfo(str2);
      str5 = Util.null2String(((Map)localObject1).get("vdatasource"));
      str6 = Util.null2String(((Map)localObject1).get("vprimarykey"));
    }

    RecordSet localRecordSet1 = new RecordSet();
    RecordSet localRecordSet2 = new RecordSet();

    int i = 0;
    String str7 = "";
    String str8;
    String str9;
    String str10;
    String str11;
    String str12;
    if (Util.getIntValue(str1) > 0) {
    FormManager  localObject2 = new FormManager();
      str7 = "select id,expendname,createpage,showtype,opentype,hreftype,hrefid,hreftarget,showcondition,showcondition2,showorder,issystem from mode_pageexpand where modeid = " + str1 + " and isshow = 1 and showtype = 1 and isbatch in(0,2) order by showorder asc";
      localRecordSet1.executeSql(str7);
      while (localRecordSet1.next()) {
        str8 = Util.null2String(localRecordSet1.getString("id"));
        if (localExpandBaseRightInfo.checkExpandRight(str8, str1, str3))
        {
          str9 = Util.null2String(localRecordSet1.getString("showcondition"));
          str10 = localExpandInfoService.replaceParam(localUser, Util.null2String(localRecordSet1.getString("showcondition2")));
          str11 = Util.null2String(localRecordSet1.getString("createpage"));
          int j = 1;
          str12 = bool ? VirtualFormHandler.getRealFromName(((FormManager)localObject2).getTablename(str2)) : ((FormManager)localObject2).getTablename(str2);
          str7 = "select 1 from " + str12 + " where " + str6 + " = '" + str3 + "'";
          if ((!str9.equals("")) && (!"1".equals(str11))) {
            j = 0;
            str7 = str7 + " and (" + str9 + ")";
          }
          if ((!str10.equals("")) && (!"1".equals(str11))) {
            j = 0;
            str7 = str7 + " and (" + str10 + ")";
          }
          try {
            localRecordSet2.executeSql(str7, str5);
            if (localRecordSet2.next())
              j = 1;
          } catch (Exception localException) {
          }
          if (j != 0)
          {
            i = 1;
          }
        }
      }
    }
    Object localObject2 = new FormModeTransMethod();
    localRecordSet1.executeSql("select * from ModeQRCode where modeid=" + str1);
    if (localRecordSet1.next()) {
      str8 = localRecordSet1.getString("targetType");
      str9 = localRecordSet1.getString("targetUrl");
      if ("1".equals(str8)) {
    	    	  if (i != 0)
    	              str9 = "/formmode/view/ViewMode.jsp?type=0&modeId=" + str1 + "&formId=" + str2 + "&billid=" + str3 + "&opentype=0&customid=" + str4 + "&viewfrom=fromsearchlist&mainid=0";
    	            else
    	              str9 = "/formmode/view/AddFormMode.jsp?type=0&modeId=" + str1 + "&formId=" + str2 + "&billid=" + str3 + "&opentype=0&customid=" + str4 + "&viewfrom=fromsearchlist&mainid=0";
      }
      else if ("2".equals(str8)) {
    		  if (i != 0)
    	          str9 = "/formmode/view/ViewMode.jsp?isfromTab=0&modeId=" + str1 + "&formId=" + str2 + "&type=2&billid=" + str3 + "&viewfrom=fromsearchlist&opentype=0&customid=" + str4 + "&isRefreshTree=0&mainid=0";
    	        else
    	          str9 = "/formmode/view/AddFormMode.jsp?isfromTab=0&modeId=" + str1 + "&formId=" + str2 + "&type=2&billid=" + str3 + "&viewfrom=fromsearchlist&opentype=0&customid=" + str4;
      }
      else
      {
        str9 = ((FormModeTransMethod)localObject2).getDefaultSql(localUser, str9);

        str10 = "";
        str11 = "select tablename from workflow_bill where id=" + str2;
        localRecordSet1.executeSql(str11);
        if (localRecordSet1.next()) {
          str10 = Util.null2String(localRecordSet1.getString("tablename"));
        }
        if (bool) {
          str10 = VirtualFormHandler.getRealFromName(str10);
        }

        if (!"".equals(str3)) {
        	String localObject3 = "0";
          str12 = "select * from " + str10 + " where id='" + str3 + "'";

          if (bool)
            localRecordSet1.executeSql(str12, str5);
          else {
            localRecordSet1.executeSql(str12);
          }
          if (localRecordSet1.next()) {
            localObject3 = localRecordSet1.getString("requestId");
            str9 = Util.replaceString2(str9, "\\$requestId\\$", (String)localObject3);
            RecordSet localRecordSet3 = new RecordSet();
            String str13 = "select * from workflow_billfield where billid=" + str2 + " and (detailtable ='' or detailtable is null)";
            localRecordSet3.executeSql(str13);
            while (localRecordSet3.next()) {
              String str14 = localRecordSet3.getString("fieldhtmltype");
              String str15 = localRecordSet3.getString("type");
              String str16 = localRecordSet3.getString("fieldname");
              if ((!"2".equals(str14)) || (!"2".equals(str15)))
              {
                str9 = Util.replaceString2(str9, "\\$" + str16 + "\\$", localRecordSet1.getString(str16));
              }
            }
          }
          str9 = Util.replaceString2(str9, "\\$billid\\$", str3);
          str9 = Util.replaceString2(str9, "\\$modeid\\$", str1);
          str9 = Util.replaceString2(str9, "\\$formid\\$", str2);
        }
      }
      if("89".equals(str1)||"90".equals(str1)||"99".equals(str1)){
    	  str9="" + str1 + "||" + str3 + "";
      }      
      str10 = str9;
      str11 = getServletContext().getRealPath("/") + "/formmode/images/qrcodeError_wev8.png";
      Object localObject3 = new EncoderHandler();
      ((EncoderHandler)localObject3).encoderQRCoder(str10, paramHttpServletResponse, str11);
    }
  }

  public void doPost(HttpServletRequest paramHttpServletRequest, HttpServletResponse paramHttpServletResponse)
    throws ServletException, IOException
  {
  }
}