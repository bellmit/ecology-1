package weaver.workflow.field;

import java.util.ArrayList;
import java.util.Hashtable;

import weaver.conn.RecordSet;
import weaver.docs.category.SecCategoryComInfo;
import weaver.docs.docs.DocImageManager;
import weaver.general.AttachFileUtil;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.hrm.User;
import weaver.systeminfo.SystemEnv;
import weaver.workflow.request.RequestManager;

public class FileElement extends BaseBean implements HtmlElement {

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  /**
   * fieldid：字段id fieldname：字段的数据库名 type：字段小类型，文本、整数、浮点....、人力资源、文档....浏览框
   * fieldlabel：字段显示名称，在这里可以直接用，不是数据库标签id
   * textlength：字段长度。在单行文本框中，代表该文本框可是输入最大的字符数（不是中文字数，所以要除以2）；在多行文本框中，代表文本框的高度。
   * isdetail：是否明细字段，1、是；0、不是；该参数的识别优先级高于groupid
   * groupid：明细字段组id，如果是-1，则代表是用在addrow里
   * fieldvalue：值。传过来前认为没做过任何处理。即使是创建流程，也可能因为节点前附加操作而有值
   * isviewonly：是否仅查看，1、是；0、不是
   * 。如果是1，则认为是viewrequest，不要hidden的input。该参数的识别优先级高于isedit和ismand
   * isview：字段是否显示，1、显示；0、不显示。该参数的识别优先级高于isviewonly、isedit和ismand
   * isedit：是否可编辑，1、可编辑；0、不可编辑。该参书的识别优先及高于ismand ismand：是否必填，1、是；0、否
   * otherPara_hs：其他参数，为实现个别字段的特殊功能而设，每种字段所需要的各不相同，用Hashtable存放。
   */
  public Hashtable getHtmlElementString(int fieldid, String fieldname, int type, String fieldlabel, int textlength, int isdetail, int groupid, String fieldvalue, int isviewonly, int isview, int isedit, int ismand, User user, Hashtable otherPara_hs) {
    // TODO Auto-generated method stub
    Hashtable ret_hs = new Hashtable();
    String inputStr = "";
    String jsStr = "";
    String addRowjsStr = "";
    ArrayList uploadfieldids = null;// TD16860
    uploadfieldids = (ArrayList) otherPara_hs.get("uploadfieldids");
    ArrayList changefieldsadd = new ArrayList();
    
    if(isdetail==1){
    	changefieldsadd = (ArrayList)otherPara_hs.get("changedefieldsadd");
    }else{
    	changefieldsadd = (ArrayList)otherPara_hs.get("changefieldsadd");
    }
    
    if(changefieldsadd==null){
    	changefieldsadd = new ArrayList();
    }
    
    
    String f_weaver_belongto_userid = user.getUID()+"";
    String f_weaver_belongto_usertype = Util.getIntValue(""+(Integer.parseInt(user.getLogintype()) - 1),0)+"";
    String f_weaver_belongto_param = "&f_weaver_belongto_userid="+f_weaver_belongto_userid+"&f_weaver_belongto_usertype="+f_weaver_belongto_usertype;
    
    if (uploadfieldids == null)
      uploadfieldids = new ArrayList();
    try {
      int languageid = user.getLanguage();
      String sql = "";
      RecordSet rs = new RecordSet();
      DocImageManager docImageManager = new DocImageManager();
      SecCategoryComInfo secCategoryComInfo = new SecCategoryComInfo();
      AttachFileUtil attachFileUtil = new AttachFileUtil();
      RequestManager requestManager = new RequestManager();
      int workflowid = Util.getIntValue((String) otherPara_hs.get("workflowid"));
      String forbidAttDownload = "";
      String sqldownload = "select forbidAttDownload from workflow_base where id=" + workflowid;
      rs.execute(sqldownload);
      if (rs.next()) {
        forbidAttDownload = rs.getString("forbidAttDownload");
      }
      int requestid = Util.getIntValue((String) otherPara_hs.get("requestid"));
	  int desrequestid = Util.getIntValue((String) otherPara_hs.get("desrequestid"));
      String nodetype = Util.null2String((String) otherPara_hs.get("nodetype"));
      String canDelAcc = Util.null2String((String) otherPara_hs.get("canDelAcc"));
      int fieldimgwidth = Util.getIntValue((String) otherPara_hs.get("fieldimgwidth" + fieldid), 0); // 图片字段宽度
      int fieldimgheight = Util.getIntValue((String) otherPara_hs.get("fieldimgheight" + fieldid), 0); // 图片字段高度
      int fieldimgnum = Util.getIntValue((String) otherPara_hs.get("fieldimgnum" + fieldid), 1); // 每行显示图片个数
      int isprint = Util.getIntValue((String) otherPara_hs.get("isprint"), 0);// 是否打印模板
      
      boolean isurger = Util.null2String((String)otherPara_hs.get("isurger")).equalsIgnoreCase("true");
      boolean wfmonitor = Util.null2String((String)otherPara_hs.get("wfmonitor")).equalsIgnoreCase("true");
      
      boolean additionflag = false;
//      if (!isurger && !wfmonitor) {
    	  additionflag = true;
//	  }
      
      // 因为一般来说，附件字段一个流程也就一个，所以在这里获取，不在前面统一获取
      String docCategory = Util.null2String((String) otherPara_hs.get("docCategory"));
      int maxUploadImageSize_Para = Util.getIntValue((String) otherPara_hs.get("maxUploadImageSize"), -1);
      int maxUploadImageSize = 5;
      if (maxUploadImageSize_Para > 0) {
        maxUploadImageSize = maxUploadImageSize_Para;
      } else {
        if (docCategory.equals("")) {
          sql = "select docCategory from workflow_base where id=" + workflowid;
          rs.execute(sql);
          if (rs.next()) {
            docCategory = rs.getString("docCategory");
          }
        }
        int secid = Util.getIntValue(docCategory.substring(docCategory.lastIndexOf(",") + 1), -1);
        maxUploadImageSize = Util.getIntValue(secCategoryComInfo.getMaxUploadFileSize("" + secid), 5);
      }
      if (maxUploadImageSize <= 0) {
        maxUploadImageSize = 5;
      }
      int uploadType = 0;
      String selectedfieldid = "";
      String result = requestManager.getUpLoadTypeForSelect(workflowid);
      if (!result.equals("")) {
        selectedfieldid = result.substring(0, result.indexOf(","));
        uploadType = Integer.valueOf(result.substring(result.indexOf(",") + 1)).intValue();
      }
      boolean isCanuse = requestManager.hasUsedType(workflowid);
      if (selectedfieldid.equals("") || selectedfieldid.equals("0")) {
        isCanuse = false;
      }
      String ismandStr = "";
      if (isviewonly == 0 && isview == 1 && isedit == 1 && ismand == 1 && "".equals(fieldvalue)) {
        ismandStr = "<img src='/images/BacoError_wev8.gif' align='absmiddle'>";
      }
      if (isdetail == 0) {// 主字段
        if (isview == 1) {
          // if(isviewonly == 0){//相差比较少，分插在一起实现isviewonly==0的功能
          boolean nodownloadnew = true;
     	  int AttachmentCountsnew = 0;
     	  int linknumnew= -1;  
          if (isedit == 1 && isviewonly == 0) {
            inputStr += "<table _target=\"mainFileUploadField\" class=\"annexblocktblclass\" cols=\"3\" id=\"field" + fieldid + "_tab\" style=\"border-collapse:collapse;border:0px;width:400px\">" + "\n";
            inputStr += "<tbody>" + "\n";
            inputStr += "<col width=\"70%\">" + "\n";
            inputStr += "<col width=\"17%\">" + "\n";
            inputStr += "<col width=\"13%\">" + "\n";
            if ("-2".equals(fieldvalue)) {
              inputStr += "<tr>" + "\n";
              inputStr += "<td colSpan=\"3\"><font color=\"red\">" + "\n";
              inputStr += "" + SystemEnv.getHtmlLabelName(21710, languageid) + "</font>" + "\n";
              inputStr += "</td>" + "\n";
              inputStr += "</tr>" + "\n";
            } else {
              if (!fieldvalue.equals("")) {
                sql = "select id,docsubject,accessorycount,SecCategory from docdetail where id in(" + fieldvalue + ") order by id asc";
                rs.executeSql(sql);
                int AttachmentCounts = rs.getCounts();
                AttachmentCountsnew = AttachmentCounts;
                int linknum = -1;
                int imgnum = fieldimgnum;
                boolean isfrist = false;
                while (rs.next()) {
                  isfrist = false;
                  linknum++;
                  String showid = Util.null2String(rs.getString(1));
                  String tempshowname = Util.toScreen(rs.getString(2), languageid);
                  int accessoryCount = rs.getInt(3);
                  String SecCategory = Util.null2String(rs.getString(4));
                  docImageManager.resetParameter();
                  docImageManager.setDocid(Integer.parseInt(showid));
                  docImageManager.selectDocImageInfo();

                  String docImagefileid = "";
                  long docImagefileSize = 0;
                  String docImagefilename = "";
                  String fileExtendName = "";
                  int versionId = 0;

                  if (docImageManager.next()) {
                    // docImageManager会得到doc第一个附件的最新版本
                    docImagefileid = docImageManager.getImagefileid();
                    docImagefileSize = docImageManager.getImageFileSize(Util.getIntValue(docImagefileid));
                    docImagefilename = docImageManager.getImagefilename();
                    fileExtendName = docImagefilename.substring(docImagefilename.lastIndexOf(".") + 1).toLowerCase();
                    versionId = docImageManager.getVersionId();
                  }
                  if (accessoryCount > 1) {
                    fileExtendName = "htm";
                  }
                  boolean nodownload = secCategoryComInfo.getNoDownload(SecCategory).equals("1") ? true : false;
                  nodownloadnew = nodownload;
                  if (type == 2) {
                    if (linknum == 0) {
                      isfrist = true;
                      if (!"1".equals(forbidAttDownload) && !nodownload && AttachmentCounts > 1 && linknum == 0 && additionflag && isprint != 1) {
                        inputStr += "<button style=\"color:#123885;border:0px;line-height:20px;font-size:12px;padding:3px;background: #fff\" class=wffbtn type=button accessKey=\"1\" onclick=\"addDocReadTag('" + showid + "');downloadsBatch('" + fieldvalue + "','" + requestid + "')\">" + "\n";
                        inputStr += "[" + SystemEnv.getHtmlLabelName(74, user.getLanguage()) + SystemEnv.getHtmlLabelName(332, user.getLanguage()) + SystemEnv.getHtmlLabelName(258, languageid) + "]\n";
                        inputStr += "</button>" + "\n";
                      }

                      inputStr += "<tr>\n";
                      inputStr += "<td colSpan=3>\n";
                      inputStr += "<table cellspacing=\"0\" cellpadding=\"0\">\n";
                      inputStr += "<tr>\n";
                    }
                    if (imgnum > 0 && linknum >= imgnum) {
                      imgnum += fieldimgnum;
                      isfrist = true;
                      inputStr += "</tr>\n";
                      inputStr += "<tr>\n";
                    }
                    inputStr += "<input type=\"hidden\" id=\"field" + fieldid + "_del_" + linknum + "\" name=\"field" + fieldid + "_del_" + linknum + "\" value=\"0\" >" + "\n";
                    inputStr += "<input type=\"hidden\" id=\"field" + fieldid + "_id_" + linknum + "\" name=\"field" + fieldid + "_id_" + linknum + "\" value=\"" + showid + "\" >" + "\n";
                    inputStr += "<td ";
                    if (!isfrist) {
                      inputStr += "style=\"padding-left:15\"";
                    }
                    inputStr += ">\n";
                    inputStr += "<table>\n";
                    inputStr += "<tr>\n";
                    inputStr += "<td colspan=2 align=\"center\"><img src=\"/weaver/weaver.file.FileDownload?fileid=" + docImagefileid +f_weaver_belongto_param+ "&requestid=" + requestid + "&desrequestid="+desrequestid+"\" style=\"cursor:pointer;\" alt=\"" + docImagefilename + "\"";
                    if (fieldimgwidth > 0) {
                      inputStr += " width=" + fieldimgwidth;
                    }
                    if (fieldimgheight > 0) {
                      inputStr += " height=" + fieldimgheight;
                    }
                    inputStr += " onclick=\"addDocReadTag('" + showid + "');openAccessory('" + docImagefileid + "')\">\n";
                    inputStr += "</td>\n";
                    inputStr += "</tr>\n";
                    inputStr += "<tr>\n";
                    if ((!canDelAcc.equals("1") || (canDelAcc.equals("1") && nodetype.equals("0"))) && isprint != 1) {
                      inputStr += "<td align=\"center\"><nobr>\n";
                      inputStr += "<a href=\"#\" style=\"text-decoration:underline\" onmouseover=\"this.style.color='blue'\" onclick='onChangeSharetype(\"span" + fieldid + "_id_" + linknum + "\",\"field" + fieldid + "_del_" + linknum + "\",\"" + ismand + "\",oUpload" + fieldid + ");return false;'>[<span style=\"cursor:pointer;color:black;\">" + SystemEnv.getHtmlLabelName(91, languageid) + "</span>]</a>\n";
                      inputStr += "<span id=\"span" + fieldid + "_id_" + linknum + "\" name=\"span" + fieldid + "_id_" + linknum + "\" style=\"visibility:hidden\"><b><font COLOR=\"#FF0033\">√</font></b><span></td>\n";
                    }
                    if (!nodownload && isprint != 1) {
                      inputStr += "<td align=\"center\"><nobr>\n";
                      inputStr += "<a href=\"#\" style=\"text-decoration:underline\" onmouseover=\"this.style.color='blue'\" onclick=\"addDocReadTag('" + showid + "');downloads('" + docImagefileid + "');return false;\">[<span style=\"cursor:pointer;color:black;\">" + SystemEnv.getHtmlLabelName(258, languageid) + "</span>]</a>\n";
                      inputStr += "</td>\n";
                    }
                    inputStr += "</tr>\n";
                    inputStr += "</table>\n";
                    inputStr += "</td>\n";
                  } else {
                    String imgSrc = AttachFileUtil.getImgStrbyExtendName(fileExtendName, 20);

                    inputStr += "<tr onmouseover=\"changecancleon(this)\" onmouseout=\"changecancleout(this)\" style=\"border-bottom:1px solid #e6e6e6;height: 40px;\">" + "\n";
                    inputStr += "<input type=\"hidden\" id=\"field" + fieldid + "_del_" + linknum + "\" name=\"field" + fieldid + "_del_" + linknum + "\" value=\"0\" >" + "\n";
                    inputStr += "<td class=\"fieldvalueClass\" valign=\"middle\" colSpan=3 style=\"word-break:normal;word-wrap:normal;\">" + "\n";
                    inputStr += "<div style=\"float:left;height:40px; line-height:40px;width:270px;\" class=\"fieldClassChange\">" + "\n";
                    inputStr += "<div style=\"float:left;width:20px;height:40px; line-height:40px;\">" + "\n";
                    inputStr += "<span style=\"display:inline-block;vertical-align: middle;padding-top:6px;\">" + "\n";
                    inputStr += imgSrc + "\n";
                    inputStr += "</span>" + "\n";
                    inputStr += "</div>" + "\n";
                    inputStr += "<div style=\"float:left;\">" + "\n";
                    inputStr += "<span style=\"display:inline-block;width:245px;height:30px;padding-bottom:10px;overflow:hidden;white-space:nowrap;text-overflow:ellipsis;vertical-align: middle;\">" + "\n";
                    
                    if (accessoryCount == 1 && (Util.isExt(fileExtendName)||fileExtendName.equalsIgnoreCase("pdf"))) {
                      inputStr += "<a style=\"cursor:pointer;color:#8b8b8b!important;\" onmouseover=\"changefileaon(this)\" onmouseout=\"changefileaout(this)\" onclick=\"addDocReadTag('" + showid + "');openDocExt('" + showid + "','" + versionId + "','" + docImagefileid + "',1)\" title=\""+docImagefilename+"\">" + docImagefilename + "</a>&nbsp;" + "\n";
                    } else {
                      inputStr += "<a style=\"cursor:pointer;color:#8b8b8b!important;\" onmouseover=\"changefileaon(this)\" onmouseout=\"changefileaout(this)\" onclick=\"addDocReadTag('" + showid + "');openAccessory('" + docImagefileid + "')\" title=\""+docImagefilename+"\">" + docImagefilename + "</a>&nbsp;" + "\n";
                    }
                    inputStr += "</span>" + "\n";
                    inputStr += "</div>" + "\n";
                    inputStr += "</div>" + "\n";
                    
                    inputStr += "<input type=\"hidden\" id=\"field" + fieldid + "_id_" + linknum + "\" name=\"field" + fieldid + "_id_" + linknum + "\" value=\"" + showid + "\">" + "\n";
                    
                    if (accessoryCount == 1 && ((!fileExtendName.equalsIgnoreCase("xls") && !fileExtendName.equalsIgnoreCase("doc") && !fileExtendName.equalsIgnoreCase("ppt") && !fileExtendName.equalsIgnoreCase("xlsx") && !fileExtendName.equalsIgnoreCase("docx") && !fileExtendName.equalsIgnoreCase("pptx") && !fileExtendName.equalsIgnoreCase("pdf") && !fileExtendName.equalsIgnoreCase("pdfx")) || !nodownload) && additionflag ) {
                    	inputStr += "<div style=\"float:left;height:40px; line-height:40px;width:70px;padding-left:10px;\" class=\"fieldClassChange\">" + "\n";
                        inputStr += "<span id=\"selectDownload\">" + "\n";
                        
                        inputStr += "<span style=\"width:45px;display:inline-block;color:#898989;margin-top:1px;\">" +docImagefileSize / 1000+ "K</span>" + "\n";
                        inputStr += "<a style=\"display:inline-block;cursor:pointer;vertical-align:middle;width:20px;height:20px;margin-bottom:5px;background-image:url('/images/ecology8/workflow/fileupload/upload_wev8.png');\" onclick=\"addDocReadTag('" + showid + "');downloads('" + docImagefileid + "')\" title=\""+SystemEnv.getHtmlLabelName(31156,user.getLanguage())+"\"></a>" + "\n";
                        
                        /*if (((!fileExtendName.equalsIgnoreCase("xls") && !fileExtendName.equalsIgnoreCase("doc") && !fileExtendName.equalsIgnoreCase("ppt") && !fileExtendName.equalsIgnoreCase("xlsx") && !fileExtendName.equalsIgnoreCase("docx") && !fileExtendName.equalsIgnoreCase("pptx") && !fileExtendName.equalsIgnoreCase("pdf") && !fileExtendName.equalsIgnoreCase("pdfx")) || !nodownload) && additionflag ) {
                          inputStr += "<button style=\"color:#123885;border:0px;line-height:20px;font-size:12px;padding:3px;background: #fff\" class=wffbtn type=button accessKey=\"1\" onclick=\"addDocReadTag('" + showid + "');downloads('" + docImagefileid + "')\">" + "\n";
                          inputStr += "[<u>" + linknum + "</u>-" + SystemEnv.getHtmlLabelName(258, languageid) + "		(" + (docImagefileSize / 1000) + "K)" + "]\n";
                          inputStr += "</button>" + "\n";
                          if (!"1".equals(forbidAttDownload) && AttachmentCounts > 1 && !nodownload && linknum == 0 && additionflag) {
                            inputStr += "<button style=\"color:#123885;border:0px;line-height:20px;font-size:12px;padding:3px;background: #fff\" class=wffbtn type=button accessKey=\"1\" onclick=\"addDocReadTag('" + showid + "');downloadsBatch('" + fieldvalue + "','" + requestid + "')\">" + "\n";
                            inputStr += "[" + SystemEnv.getHtmlLabelName(332, user.getLanguage()) + SystemEnv.getHtmlLabelName(258, languageid) + "]\n";
                            inputStr += "</button>" + "\n";
                          }
                        }*/
                        inputStr += "</span>" + "\n";
                        inputStr += "</div>" + "\n";
                      }
                    
                    
                    if (!canDelAcc.equals("1") || (canDelAcc.equals("1") && nodetype.equals("0"))) {
                      
                      inputStr += "<div class=\"fieldClassChange\" id=\"fieldCancleChange\" style=\"float:left;width:50px;height:40px; line-height: 40px;text-align:center;\">" + "\n";
                      inputStr += "<span id=\"span"+fieldid+"_id_"+linknum+"\" name=\"span"+fieldid+"_id_"+linknum+"\" style=\"display:none;\">" + "\n";
                      inputStr += "<a style=\"display:inline-block;cursor:pointer;vertical-align:middle;width:20px;height:20px;background-image:url('/images/ecology8/workflow/fileupload/cancle_wev8.png');background-repeat :no-repeat;\" onclick=\"onChangeSharetypeNew(this,'span"+fieldid+"_id_"+linknum+"','field"+fieldid+"_del_"+linknum+"','"+showid+"','"+docImagefilename+"',"+ismand+",oUpload"+fieldid+")\" title=\""+SystemEnv.getHtmlLabelName(91,user.getLanguage())+"\"></a>" + "\n";
                      inputStr += "</span>" + "\n";
                      inputStr += "</div>" + "\n";
                    
                    /*
                      inputStr += "<button style=\"color:#123885;border:0px;line-height:20px;font-size:12px;padding:3px;background: #fff\" class=wffbtn type=button accessKey=\"1\" onclick=\"onChangeSharetype('span" + fieldid + "_id_" + linknum + "','field" + fieldid + "_del_" + linknum + "','" + ismand + "',oUpload" + fieldid + ")\">[<u>" + linknum + "</u>-" + SystemEnv.getHtmlLabelName(91, languageid) + "]</button>" + "\n";
                      inputStr += "<span id=\"span" + fieldid + "_id_" + linknum + "\" name=\"span" + fieldid + "_id_" + linknum + "\" style=\"visibility:hidden\">" + "\n";
                      inputStr += "<b><font color=\"#FF0033\">√</font></b>";
                      inputStr += "</span>" + "\n";*/
                      
                    }
                    
                    inputStr += "</tr>" + "\n";
                  }
                }// while(rs.next()){ end
                linknumnew = linknum;
                if (type == 2 && linknum > -1) {
                  inputStr += "</tr>\n</table>\n</td>\n</tr>\n";
                }
                inputStr += "<input type=\"hidden\" id=\"field" + fieldid + "_idnum\" name=\"field" + fieldid + "_idnum\" value=\"" + (linknum + 1) + "\">" + "\n";
                inputStr += "<input type=\"hidden\" id=\"field" + fieldid + "_idnum_1\" name=\"field" + fieldid + "_idnum_1\" value=\"" + (linknum + 1) + "\">" + "\n";
              }
            }
            inputStr += "<tr>" + "\n";
            inputStr += "<td colspan=3>" + "\n";
            String mainId = "";
            String subId = "";
            String secId = "";
            if (docCategory != null && !docCategory.equals("")) {
              int pindex = docCategory.indexOf(',');
              if(pindex > -1){
            	  mainId = docCategory.substring(0, docCategory.indexOf(','));
            	  subId = docCategory.substring(docCategory.indexOf(',') + 1, docCategory.lastIndexOf(','));
            	  secId = docCategory.substring(docCategory.lastIndexOf(',') + 1, docCategory.length());
              }else{
            	  mainId = "-1";
            	  subId = "-1";
            	  secId = docCategory;
              }
            }
            String picfiletypes = "*.*";
            String filetypedesc = "All Files";
            if (type == 2) {
              picfiletypes = new BaseBean().getPropValue("PicFileTypes", "PicFileTypes");
              filetypedesc = "Images Files";
            }
            boolean canupload = true;
            if (uploadType == 0) {
              if ("".equals(mainId) && "".equals(subId) && "".equals(secId)) {
                canupload = false;
                inputStr += "<font color=\"red\">" + SystemEnv.getHtmlLabelName(17616, languageid) + SystemEnv.getHtmlLabelName(92, languageid) + SystemEnv.getHtmlLabelName(15808, languageid) + "!</font>\n";
              }
            } else if (!isCanuse) {
              canupload = false;
              inputStr += "<font color=\"red\">" + SystemEnv.getHtmlLabelName(17616, languageid) + SystemEnv.getHtmlLabelName(92, languageid) + SystemEnv.getHtmlLabelName(15808, languageid) + "!</font>\n";
            }
            if (canupload) {
              uploadfieldids.add("" + fieldid);
              jsStr += "var oUpload" + fieldid + ";\n";
              jsStr += "function fileupload" + fieldid + "() {\n";
              jsStr += " var settings = {\n";
              jsStr += "flash_url:\"/js/swfupload/swfupload.swf\",\n";
              jsStr += "upload_url:\"/docs/docupload/MultiDocUploadByWorkflow.jsp\",\n";
              jsStr += "post_params:{\n";
              jsStr += "\t\"mainId\":\"" + mainId + "\",\n";
              jsStr += "\t\"subId\":\"" + subId + "\",\n";
              jsStr += "\t\"secId\":\"" + secId + "\",\n";
              jsStr += "\t\"userid\":\"" + user.getUID() + "\",\n";
              jsStr += "\t\"logintype\":\"" + user.getLogintype() + "\",\n";
              jsStr += "\t\"workflowid\":\"" + workflowid + "\"\n";
              jsStr += "},\n";
              jsStr += "file_size_limit :\"" + maxUploadImageSize + " MB\",\n";
              jsStr += "file_types : \"" + picfiletypes + "\",\n";
              jsStr += "file_types_description : \"" + filetypedesc + "\",\n";
              jsStr += "file_upload_limit : 100,\n";
              jsStr += "file_queue_limit : 0,\n";
              jsStr += "custom_settings : { \n";
              jsStr += "\tprogressTarget : \"fsUploadProgress" + fieldid + "\",\n";
              jsStr += "\tcancelButtonId : \"btnCancel" + fieldid + "\",\n";
              jsStr += "\tuploadspan : \"field_" + fieldid + "span\",\n";
              jsStr += "\tuploadfiedid : \"field" + fieldid + "\"\n";
              jsStr += "},\n";
              jsStr += "debug: false,\n";
              if(languageid == 8){
            	  jsStr += "button_image_url : \"/images/ecology8/workflow/fileupload/begin1_wev8-2.png\",\n";
			  }else{
				  jsStr += "button_image_url : \"/images/ecology8/workflow/fileupload/begin1_wev8.png\",\n";
			  }
              jsStr += "button_placeholder_id : \"spanButtonPlaceHolder" + fieldid + "\",\n";
              if(languageid == 8){
                  jsStr += "button_width: 144,\n";
              }else{
                  jsStr += "button_width: 104,\n";
              }
              jsStr += "button_height: 26,\n";
              //jsStr += "button_text : '<span class=\"button\">" + SystemEnv.getHtmlLabelName(21406, languageid) + "</span>',\n";
              //jsStr += "button_text_style : '.button { font-family: Helvetica, Arial, sans-serif; font-size: 12pt; } .buttonSmall { font-size: 10pt; }',\n";
              jsStr += "button_text_top_padding: 0,\n";
              jsStr += "button_text_left_padding: 18,\n";
              jsStr += "button_window_mode: SWFUpload.WINDOW_MODE.TRANSPARENT,\n";
              jsStr += "button_cursor: SWFUpload.CURSOR.HAND,\n";
              jsStr += "file_queued_handler : fileQueued,\n";
              jsStr += "file_queue_error_handler : fileQueueError,\n";
              jsStr += "file_dialog_complete_handler : fileDialogComplete_1,\n";
              jsStr += "upload_start_handler : uploadStart,\n";
              jsStr += "upload_progress_handler : uploadProgress,\n";
              jsStr += "upload_error_handler : uploadError,\n";
              jsStr += "upload_success_handler : uploadSuccess_1,\n";
              jsStr += "upload_complete_handler : uploadComplete_1,\n";
              jsStr += "queue_complete_handler : queueComplete\n";
              jsStr += "};\n";
              jsStr += "try {\n";
              jsStr += "\toUpload" + fieldid + "=new SWFUpload(settings);\n";
              jsStr += "} catch(e) {\n";
              jsStr += "\talert(e)\n";
              jsStr += "}\n";
              jsStr += "}\n";
              // jsStr += "window.attachEvent(\"onload\", fileupload" + fieldid
              // + ");\n";

              jsStr += "\t" + "if (window.addEventListener){" + "\n";
              jsStr += "\t" + "    window.addEventListener(\"load\", fileupload" + fieldid + ", false);" + "\n";
              jsStr += "\t" + "}else if (window.attachEvent){" + "\n";
              jsStr += "\t" + "    window.attachEvent(\"onload\", fileupload" + fieldid + ");" + "\n";
              jsStr += "\t" + "}else{" + "\n";
              jsStr += "\t" + "    window.onload=fileupload" + fieldid + ";" + "\n";
              jsStr += "\t" + "}" + "\n";

              inputStr += "<TABLE class=\"ViewForm\">\n";
              inputStr += "<tr>\n";
              inputStr += "<td colspan=2>\n";
              
              inputStr += "<div style=\"height: 32px;vertical-align:middle;\">" + "\n";
              inputStr += "<span id=\"uploadspan\" style=\"display:inline-block;line-height: 32px;\">"+SystemEnv.getHtmlLabelName(83523,user.getLanguage()) ;
              inputStr += maxUploadImageSize+SystemEnv.getHtmlLabelName(83528,user.getLanguage())+"</span>" + "\n";
              
			  if(ismand ==1 && fieldvalue.equals("")){
				  inputStr += "<span id=\"field_" + fieldid + "span\" style='display:inline-block;line-height: 32px;color:red !important;font-weight:normal;'>"+SystemEnv.getHtmlLabelName(81909,user.getLanguage())+"</span>" + "\n";
			  }else{
				  inputStr += "<span id=\"field_" + fieldid + "span\" style='display:inline-block;line-height: 32px;color:red !important;font-weight:normal;'></span>" + "\n";
			  }
              inputStr += "</div>" + "\n";
              inputStr += "<div style=\"height: 30px;\">" + "\n";
              inputStr += "<div style=\"float:left;\">" + "\n";
              inputStr += "<span>" + "\n";
              inputStr += "<span id=\"spanButtonPlaceHolder" + fieldid + "\"></span>" + "\n";
              inputStr += "</span>" + "\n";
              inputStr += "</div>" + "\n";
              inputStr += "<div style=\"width:10px!important;height:3px;float:left;\"></div>" + "\n";
              inputStr += "<div style=\"height: 30px;float:left;\">" + "\n";
              //inputStr += "<span style=\"display:inline-block;height:25px;cursor:pointer;text-align:center;vertical-align:middle;line-height:25px;background-color: #aaaaaa;color:#ffffff;padding:0 20px 0 14px;\" onclick=\"clearAllQueue(oUpload"+fieldid+");showmustinput(oUpload"+fieldid+");\"><img src='/images/ecology8/workflow/fileupload/clearall_wev8.png' style=\"width:20px;height:20px;padding-bottom:2px;\" align=absMiddle>"+SystemEnv.getHtmlLabelName(21407,user.getLanguage())+"</span>" + "\n";
              inputStr += "<button type=\"button\" id=\"btnCancel" + fieldid + "\" disabled=\"disabled\" style=\"height:25px;text-align:center;vertical-align:middle;line-height:23px;background-color: #dfdfdf;color:#999999;padding:0 10px 0 4px;\" onclick=\"clearAllQueue(oUpload"+fieldid+");showmustinput(oUpload"+fieldid+");\" onmouseover=\"changebuttonon(this)\" onmouseout=\"changebuttonout(this)\"><img src='/images/ecology8/workflow/fileupload/clearallenable_wev8.png' style=\"width:20px;height:20px;\" align=absMiddle>"+SystemEnv.getHtmlLabelName(21407,user.getLanguage())+"</button>" + "\n";
              
              /*inputStr += "&nbsp;&nbsp;<span style=\"color:#262626;cursor:pointer;TEXT-DECORATION:none\" disabled onclick=\"oUpload" + fieldid + ".cancelQueue();showmustinput(oUpload" + fieldid + ");\" id=\"btnCancel" + fieldid + "\">\n";
              inputStr += "<span><img src=\"/js/swfupload/delete_wev8.gif\" border=0></span>\n";
              inputStr += "<span style=\"height:19px\"><font style=\"margin:0 0 0 -1\">" + SystemEnv.getHtmlLabelName(21407, languageid) + "</font></span>\n";
              inputStr += "</span><span id=\"uploadspan\">(" + SystemEnv.getHtmlLabelName(18976, languageid) + maxUploadImageSize + SystemEnv.getHtmlLabelName(18977, languageid) + ")</span>\n";
              */
              inputStr += "<span id=\"field" + fieldid + "spantest\" style=\"display:none;\" >\n";
              if (ismand == 1 && fieldvalue.equals("")) {
                inputStr += "<img src='/images/BacoError_wev8.gif' align=absMiddle>\n";
              }
              inputStr += "</span>\n";
              
              inputStr += "</div>" + "\n";
              inputStr += "<div style=\"width:10px!important;height:3px;float:left;\"></div>" + "\n";
              inputStr += "<div style=\"height: 30px;float:left;\">" + "\n";
              if(!"1".equals(forbidAttDownload) && !nodownloadnew && AttachmentCountsnew>1 && linknumnew>=0){
            	  //inputStr += "<span onclick=\"downloadsBatch('"+fieldvalue+"','"+requestid+"')\" style=\"display:inline-block;height:25px;cursor:pointer;text-align:center;vertical-align:middle;line-height:25px;background-color: #6bcc44;color:#ffffff;padding:0 20px 0 14px;\"><img src='/images/ecology8/workflow/fileupload/uploadall_wev8.png' style=\"width:20px;height:20px;padding-bottom:2px;\" align=absMiddle>"+SystemEnv.getHtmlLabelName(332,user.getLanguage())+SystemEnv.getHtmlLabelName(258,user.getLanguage())+"</span>" + "\n";
            	  inputStr += "<button type=\"button\" id=\"field_upload_"+fieldid+"\" onclick=\"downloadsBatch('"+fieldvalue+"','"+requestid+"')\" style=\"height:25px;cursor:pointer;text-align:center;vertical-align:middle;line-height:25px;background-color: #6bcc44;color:#ffffff;padding:0 10px 0 4px;\" onmouseover=\"uploadbuttonon(this)\" onmouseout=\"uploadbuttonout(this)\"><img src='/images/ecology8/workflow/fileupload/uploadall_wev8.png' style=\"width:20px;height:20px;padding-bottom:2px;\" align=absMiddle>"+SystemEnv.getHtmlLabelName(332,user.getLanguage())+SystemEnv.getHtmlLabelName(258,user.getLanguage())+"</button>" + "\n";
              }
              inputStr += "</div>\n";
              inputStr += "<div style=\"clear:both;\"></div>\n";
              inputStr += "</div>\n";
              inputStr += "<input  class=InputStyle  type=hidden size=60 name=\"field" + fieldid + "\" id=\"field" + fieldid + "\" temptitle=\"" + Util.toScreen(fieldlabel, languageid) + "\"  viewtype=" + ismand + " value=\"" + fieldvalue + "\">\n";
              inputStr += "</td>\n";
              inputStr += "</tr>\n";
              inputStr += "<tr>\n";
              inputStr += "<td colspan=2>\n";
              inputStr += "<div class=\"_uploadForClass\">\n";
              inputStr += "<div class=\"fieldset flash\" id=\"fsUploadProgress" + fieldid + "\">\n";
              inputStr += "</div>\n";
              inputStr += "</div>\n";
              inputStr += "<div id=\"divStatus" + fieldid + "\"></div>\n";
              inputStr += "</td>\n";
              inputStr += "</tr>\n";
              inputStr += "</TABLE>\n";
            }
            inputStr += "<input type=\"hidden\" id=\"mainId\" name=\"mainId\" value=\"" + mainId + "\">" + "\n";
            inputStr += "<input type=\"hidden\" id=\"subId\" name=\"subId\" value=\"" + subId + "\">" + "\n";
            inputStr += "<input type=\"hidden\" id=\"secId\" name=\"secId\" value=\"" + secId + "\">" + "\n";
            inputStr += "</td>\n";
            inputStr += "</tr>\n";
            inputStr += "</TABLE>\n";
          } else {
            inputStr += "<table _target=\"mainFileUploadField\" cols=\"3\" style=\"border-collapse:collapse;border:0px;width:"+(isprint==1?"95%":"400px")+"\" id=\"field" + fieldid + "_tab\">" + "\n";
            inputStr += "<tbody>" + "\n";
            inputStr += "<col width=\"70%\">" + "\n";
            inputStr += "<col width=\"17%\">" + "\n";
            inputStr += "<col width=\"13%\">" + "\n";
            if ("-2".equals(fieldvalue)) {
              inputStr += "<tr>" + "\n";
              inputStr += "<td colSpan=\"3\"><font color=\"red\">" + "\n";
              inputStr += "" + SystemEnv.getHtmlLabelName(21710, languageid) + "</font>" + "\n";
              inputStr += "</td>" + "\n";
              inputStr += "</tr>" + "\n";
            } else {
              if (!fieldvalue.equals("")) {
                sql = "select id,docsubject,accessorycount,SecCategory from docdetail where id in(" + fieldvalue + ") order by id asc";
                int linknum = -1;
                int imgnum = fieldimgnum;
                boolean isfrist = false;
                rs.executeSql(sql);
                int AttachmentCounts = rs.getCounts();
                AttachmentCountsnew = AttachmentCounts;
                while (rs.next()) {
                  isfrist = false;
                  linknum++;
                  String showid = Util.null2String(rs.getString(1));
                  String tempshowname = Util.toScreen(rs.getString(2), languageid);
                  int accessoryCount = rs.getInt(3);
                  String SecCategory = Util.null2String(rs.getString(4));
                  docImageManager.resetParameter();
                  docImageManager.setDocid(Integer.parseInt(showid));
                  docImageManager.selectDocImageInfo();

                  String docImagefileid = "";
                  long docImagefileSize = 0;
                  String docImagefilename = "";
                  String fileExtendName = "";
                  int versionId = 0;

                  if (docImageManager.next()) {
                    docImagefileid = docImageManager.getImagefileid();
                    docImagefileSize = docImageManager.getImageFileSize(Util.getIntValue(docImagefileid));
                    docImagefilename = docImageManager.getImagefilename();
                    fileExtendName = docImagefilename.substring(docImagefilename.lastIndexOf(".") + 1).toLowerCase();
                    versionId = docImageManager.getVersionId();
                  }
                  if (accessoryCount > 1) {
                    fileExtendName = "htm";
                  }
                  String imgSrc = attachFileUtil.getImgStrbyExtendName(fileExtendName, 20);
                  boolean nodownload = secCategoryComInfo.getNoDownload(SecCategory).equals("1") ? true : false;
                  nodownloadnew = nodownload;
                  //流程督办、流程兼容均屏蔽下载按钮
                  if (isprint == 1) {// 如果是打印模板，就没有下载按钮
                    nodownload = false;
                  }
                  if (type == 2) {
                    if (linknum == 0) {
                      isfrist = true;

                      if (!"1".equals(forbidAttDownload) && !nodownload && AttachmentCounts > 1 && linknum == 0 && additionflag && isprint != 1) {
                        // inputStr +=
                        // "<button type=button  class=\"btnFlowd\" accessKey=\"1\" onclick=\"addDocReadTag('"+showid+"');downloadsBatch('"+fieldvalue+"','"+requestid+"')\">"+"\n";
                        inputStr += "<button style=\"color:#123885;border:0px;line-height:20px;font-size:12px;padding:3px;background: #fff\" class=wffbtn type=button accessKey=\"1\" onclick=\"addDocReadTag('" + showid + "');top.location='/weaver/weaver.file.FileDownload?fieldvalue=" + fieldvalue + "&download=1&downloadBatch=1&requestid=" + requestid + "&desrequestid="+desrequestid+"'\">" + "\n";
                        inputStr += "[" + SystemEnv.getHtmlLabelName(74, user.getLanguage()) + SystemEnv.getHtmlLabelName(332, user.getLanguage()) + SystemEnv.getHtmlLabelName(258, languageid) + "]\n";
                        inputStr += "</button>" + "\n";
                      }

                      inputStr += "<tr>\n";
                      inputStr += "<td colSpan=3>\n";
                      inputStr += "<table cellspacing=\"0\" cellpadding=\"0\">\n";
                      inputStr += "<tr>\n";
                    }
                    if (imgnum > 0 && linknum >= imgnum) {
                      imgnum += fieldimgnum;
                      isfrist = true;
                      inputStr += "</tr>\n";
                      inputStr += "<tr>\n";
                    }
                    inputStr += "<input type=\"hidden\" id=\"field" + fieldid + "_del_" + linknum + "\" name=\"field" + fieldid + "_del_" + linknum + "\" value=\"0\" >" + "\n";
                    inputStr += "<input type=\"hidden\" id=\"field" + fieldid + "_id_" + linknum + "\" name=\"field" + fieldid + "_id_" + linknum + "\" value=\"" + showid + "\" >" + "\n";
                    inputStr += "<td ";
                    if (!isfrist) {
                      inputStr += "style=\"padding-left:15\"";
                    }
                    inputStr += ">\n";
                    inputStr += "<table>\n";
                    inputStr += "<tr>\n";
                    inputStr += "<td colspan=2 align=\"center\"><img src=\"/weaver/weaver.file.FileDownload?fileid=" + docImagefileid +f_weaver_belongto_param+ "&requestid=" + requestid + "&desrequestid="+desrequestid+"\" style=\"cursor:pointer\" alt=\"" + docImagefilename + "\"";
                    if (fieldimgwidth > 0)
                      inputStr += " width=" + fieldimgwidth;
                    if (fieldimgheight > 0)
                      inputStr += " height=" + fieldimgheight;
                    inputStr += " onclick=\"addDocReadTag('" + showid + "');openAccessory('" + docImagefileid + "')\">\n";
                    inputStr += "</td>\n";
                    inputStr += "</tr>\n";
                     if (!nodownload && additionflag && isprint != 1) {
                      inputStr += "<tr>\n";
                    //if (!nodownload && isviewonly == 0) { //update by liao dong for QC36142
                      inputStr += "<td align=\"center\"><nobr>\n";
                      inputStr += "<a href=\"#\" style=\"text-decoration:underline\" onmouseover=\"this.style.color='blue'\" onclick=\"addDocReadTag('" + showid + "');top.location='/weaver/weaver.file.FileDownload?fileid=" + docImagefileid + "&download=1&requestid=" + requestid + "&desrequestid="+desrequestid+"';return false;\">[<span style=\"cursor:pointer;color:black;\">" + SystemEnv.getHtmlLabelName(258, languageid) + "</span>]</a>\n";
                      inputStr += "</td>\n";
                      inputStr += "</tr>\n";
                    }
                    inputStr += "</table>\n";
                    inputStr += "</td>\n";
                  } else {
                    inputStr += "<tr style=\"border-bottom:1px solid #e6e6e6;"+(isprint==1?"":"height:42px;")+"\">" + "\n";
                    inputStr += "<td class=\"fieldvalueClass\" valign=\"middle\" colspan=\"3\" style=\"word-break:normal;word-wrap:normal;\">" + "\n";
                    inputStr += "<div style=\"float:left;"+(isprint==1?"width:100%;":"height:40px;line-height:38px;width:270px;")+"\" class=\"fieldClassChange\">" + "\n";
                    inputStr += "<div style=\"float:left;width:20px;"+(isprint==1?"":"height:40px;line-height:38px;")+"\">" + "\n";
                    inputStr += "<span style=\"display:inline-block;vertical-align: middle;\">" + "\n";
                    inputStr += imgSrc;
                    inputStr += "</span>" + "\n";
                    inputStr += "</div>" + "\n";
                    inputStr += "<div style=\"float:left;"+(isprint==1?"width:90%;":"")+"\">" + "\n";
                    if(isprint == 1){
	                    inputStr += "<span style=\"display:inline-block;vertical-align: middle;\">" + "\n";
	                    inputStr += docImagefilename;
                    }else{ 
                    	inputStr += "<span style=\"display:inline-block;width:245px;height:30px;padding-bottom:10px;overflow:hidden;white-space:nowrap;text-overflow:ellipsis;vertical-align: middle;\">" + "\n";
	                    if (accessoryCount == 1 && (Util.isExt(fileExtendName)||fileExtendName.equalsIgnoreCase("pdf"))) {
	                      inputStr += "<a style=\"cursor:pointer;color:#8b8b8b!important;margin-top:1px;\" onmouseover=\"changefileaon(this)\" onmouseout=\"changefileaout(this)\" onclick=\"addDocReadTag('" + showid + "');openDocExt('" + showid + "','" + versionId + "','" + docImagefileid + "',0)\" title=\""+docImagefilename+"\">" + docImagefilename + "</a>&nbsp;" + "\n";
	                    } else {
	                      inputStr += "<a style=\"cursor:pointer;color:#8b8b8b!important;margin-top:1px;\" onmouseover=\"changefileaon(this)\" onmouseout=\"changefileaout(this)\" onclick=\"addDocReadTag('" + showid + "');openAccessory('" + docImagefileid + "')\" title=\""+docImagefilename+"\">" + docImagefilename + "</a>&nbsp;" + "\n";
	                    }
                    }
                    inputStr += "</span>" + "\n";
                    inputStr += "</div>" + "\n";
                    inputStr += "</div>" + "\n";
                    if (isviewonly == 0) {
                      inputStr += "<input type=\"hidden\" id=\"field" + fieldid + "_id_" + linknum + "\" name=\"field" + fieldid + "_id_" + linknum + "\" value=\"" + showid + "\">" + "\n";
                    }
                    
                    if (((!fileExtendName.equalsIgnoreCase("xls") && !fileExtendName.equalsIgnoreCase("doc") && !fileExtendName.equalsIgnoreCase("ppt") && !fileExtendName.equalsIgnoreCase("xlsx") && !fileExtendName.equalsIgnoreCase("docx") && !fileExtendName.equalsIgnoreCase("pptx") && !fileExtendName.equalsIgnoreCase("pdf") && !fileExtendName.equalsIgnoreCase("pdfx")) || !nodownload) && additionflag && isprint != 1) {
                    	inputStr += "<div style=\"float:left;height:40px; line-height:38px;width:70px;padding-left:10px;\" class=\"fieldClassChange\">" + "\n";
                    	inputStr += "<span id = \"selectDownload\">" + "\n";
                    	inputStr += "<nobr>" + "\n";
                    	inputStr += "<span style=\"width:45px;display:inline-block;color:#898989;margin-top:1px;\">"+docImagefileSize / 1000+ "K</span>" + "\n";
                    	inputStr += "<a style=\"display:inline-block;cursor:pointer;vertical-align:middle;width:20px;height:20px;background-image:url('/images/ecology8/workflow/fileupload/upload_wev8.png');\" onclick=\"addDocReadTag('" +showid+ "');downloads('" +docImagefileid+ "')\" title=\""+SystemEnv.getHtmlLabelName(31156,user.getLanguage())+"\"></a>" + "\n";
                    	inputStr += "</nobr>" + "\n";
                    	inputStr += "</span>" + "\n";
                    	inputStr += "</div>" + "\n";
                      /*inputStr += "<span id=\"selectDownload\">" + "\n";
                      inputStr += "<button style=\"color:#123885;border:0px;line-height:20px;font-size:12px;padding:3px;background: #fff\" class=wffbtn type=button accessKey=\"1\" onclick=\"addDocReadTag('" + showid + "');top.location='/weaver/weaver.file.FileDownload?fileid=" + docImagefileid + "&download=1&requestid=" + requestid + "'\">" + "\n";
                      inputStr += "[<u>" + linknum + "</u>-" + SystemEnv.getHtmlLabelName(258, languageid) + "	(" + docImagefileSize / 1000 + "K)" + "]\n";
                      inputStr += "</button>" + "\n";
                      inputStr += "</span>" + "\n";
                      if (!"1".equals(forbidAttDownload) && AttachmentCounts > 1 && !nodownload && linknum == 0 && additionflag) {
                        inputStr += "<button style=\"color:#123885;border:0px;line-height:20px;font-size:12px;padding:3px;background: #fff\" class=wffbtn type=button accessKey=\"1\" onclick=\"addDocReadTag('" + showid + "');top.location='/weaver/weaver.file.FileDownload?fieldvalue=" + fieldvalue + "&download=1&downloadBatch=1&requestid=" + requestid + "'\">" + "\n";
                        inputStr += "[" + SystemEnv.getHtmlLabelName(332, user.getLanguage()) + SystemEnv.getHtmlLabelName(258, languageid) + "]\n";
                        inputStr += "</button>" + "\n";
                      }*/
                    }
                    
                    inputStr += "</td>" + "\n";
                    inputStr += "</tr>" + "\n";
                  }
                }
                linknumnew = linknum;
                if (type == 2 && linknum > -1) {
                  inputStr += "</tr></table></td></tr>\n";
                }
                // if(isviewonly == 0){
                if(isprint != 1){
	                inputStr += "<input type=\"hidden\" id=\"field" + fieldid + "_idnum\" name=\"field" + fieldid + "_idnum\" value=\"" + (linknum + 1) + "\">" + "\n";
	                inputStr += "<input type=hidden name=\"field" + fieldid + "\" value=\"" + fieldvalue + "\">" + "\n";
                }
              }
            }
            if(isprint != 1){
	            inputStr += "<tr>" + "\n";
	            inputStr += "<td class=\"fieldvalueClass\" valign=\"middle\" colSpan=3>" + "\n";
	            if(!"1".equals(forbidAttDownload) && !nodownloadnew && AttachmentCountsnew>1 && linknumnew>=0){
	                inputStr += "<span onclick=\"top.location='/weaver/weaver.file.FileDownload?fieldvalue=" + fieldvalue + "&download=1&downloadBatch=1&requestid=" + requestid + "&desrequestid="+desrequestid+"'\" style=\"display:inline-block;height:25px;cursor:pointer;text-align:center;vertical-align:middle;line-height:25px;background-color: #6bcc44;color:#ffffff;padding:0 20px 0 14px;\" onmouseover=\"uploadbuttonon(this)\" onmouseout=\"uploadbuttonout(this)\"><img src='/images/ecology8/workflow/fileupload/uploadall_wev8.png' style=\"width:20px;height:20px;padding-bottom:2px;\" align=absMiddle>"+SystemEnv.getHtmlLabelName(332,user.getLanguage())+SystemEnv.getHtmlLabelName(258,user.getLanguage())+"</span>" + "\n";
	            }
	            inputStr += "</td>" + "\n";
	            inputStr += "</tr>" + "\n";
            }
            inputStr += "</tbody>" + "\n";
            inputStr += "</table>" + "\n";
          }
        } else {
          if (isviewonly == 0) {
            if (!fieldvalue.equals("") && !fieldvalue.equals("-2")) {
              String[] fieldvalueas = Util.TokenizerString2(fieldvalue, ",");
              int linknum = -1;
              for (int j = 0; j < fieldvalueas.length; j++) {
                linknum++;
                String showid = Util.null2String("" + fieldvalueas[j]);
                inputStr = "<input type=\"hidden\" id=\"field" + fieldid + "_id_" + linknum + "\" name=\"field" + fieldid + "_id_" + linknum + "\" value=\"" + showid + "\">" + "\n";
              }
              inputStr = "<input type=\"hidden\" id=\"field" + fieldid + "_idnum\" name=\"field" + fieldid + "_idnum\" value=\"" + linknum + 1 + "\">" + "\n";
            }
          }
        }
        if(changefieldsadd.indexOf(""+fieldid)>=0){
        	inputStr += "<input type=\"hidden\" id=\"oldfieldview"+fieldid+"\" name=\"oldfieldview"+fieldid+"\" _readonly=\"0\" value=\""+(isview+isedit+ismand)+"\" >";
        }
      } else {// 明细字段
    	  String derecorderindex = Util.null2String((String)otherPara_hs.get("derecorderindex"));
    	  //System.out.println("isview:"+isview+"   isedit:"+isedit+"  isviewonly:"+isviewonly);
          if (isview == 1) {
        	  boolean nodownloadnew = true;
        	  int AttachmentCountsnew = 0;
        	  int linknumnew= -1;  
        	  if (isedit == 1 && isviewonly == 0) {
        		  
        		  int tabwidth = 170;
        		  if (fieldimgwidth > 120) {
        			  tabwidth = fieldimgwidth+51;
        		  }
        		  
        		  inputStr += "<table class='annexblocktblclass' cols='3' id='field" + fieldid+"_"+derecorderindex + "_tab' style='border-collapse:collapse;border:0px;"+(isprint==1?"width:95%":"min-width:"+tabwidth+"px;width:100%;")+";table-layout:fixed;' align='top'>" + "\n";
        		  //inputStr += "<tbody>" + "\n";
        		  inputStr += "<col width='70%'>" + "\n";
        		  inputStr += "<col width='14%'>" + "\n";
        		  inputStr += "<col width='12%'>" + "\n";
	              if ("-2".equals(fieldvalue)) {
	            	  inputStr += "<tr>" + "\n";
	            	  inputStr += "<td colSpan='3'><font color='red'>" + "\n";
	            	  inputStr += "" + SystemEnv.getHtmlLabelName(21710, languageid) + "</font>" + "\n";
	            	  inputStr += "</td>" + "\n";
	            	  inputStr += "</tr>" + "\n";
	              } else {
	            	  if (!fieldvalue.equals("")) {
	                  sql = "select id,docsubject,accessorycount,SecCategory from docdetail where id in(" + fieldvalue + ") order by id asc";
	                  rs.executeSql(sql);
	                  int AttachmentCounts = rs.getCounts();
	                  AttachmentCountsnew = AttachmentCounts;
	                  int linknum = -1;
	                  int imgnum = fieldimgnum;
	                  boolean isfrist = false;
	                  while (rs.next()) {
	                	  isfrist = false;
	                	  linknum++;
	                	  String showid = Util.null2String(rs.getString(1));
	                	  String tempshowname = Util.toScreen(rs.getString(2), languageid);
	                	  int accessoryCount = rs.getInt(3);
	                	  String SecCategory = Util.null2String(rs.getString(4));
	                	  docImageManager.resetParameter();
	                	  docImageManager.setDocid(Integer.parseInt(showid));
	                	  docImageManager.selectDocImageInfo();
	
	                	  String docImagefileid = "";
	                	  long docImagefileSize = 0;
	                	  String docImagefilename = "";
	                	  String fileExtendName = "";
	                	  int versionId = 0;
	
	                	  if (docImageManager.next()) {
	                		  // docImageManager会得到doc第一个附件的最新版本
	                		  docImagefileid = docImageManager.getImagefileid();
	                		  docImagefileSize = docImageManager.getImageFileSize(Util.getIntValue(docImagefileid));
	                		  docImagefilename = docImageManager.getImagefilename();
	                		  fileExtendName = docImagefilename.substring(docImagefilename.lastIndexOf(".") + 1).toLowerCase();
	                		  versionId = docImageManager.getVersionId();
	                      }
	                	  if (accessoryCount > 1) {
	                		  fileExtendName = "htm";
	                	  }
	                	  boolean nodownload = secCategoryComInfo.getNoDownload(SecCategory).equals("1") ? true : false;
	                	  nodownloadnew = nodownload;
	                	  if (type == 2) {
	                		  /*
	                		  if (linknum == 0) {
	                			  isfrist = true;
	
	                			  inputStr += "<tr>\n";
	                			  inputStr += "<td colSpan=3 >\n";
	                			  inputStr += "<table cellspacing='0' cellpadding='0' width='98%'>\n";
	                			  inputStr += "<tr>\n";
	                		  }
	                		  if (imgnum > 0 && linknum >= imgnum) {
	                			  imgnum += fieldimgnum;
	                			  isfrist = true;
	                			  inputStr += "</tr>\n";
	                			  inputStr += "<tr>\n";
	                		  }
	                		  inputStr += "<input type='hidden' id='field" + fieldid+"_"+derecorderindex + "_del_" + linknum + "' name='field" + fieldid+"_"+derecorderindex + "_del_" + linknum + "' value='0' >" + "\n";
	                		  inputStr += "<input type='hidden' id='field" + fieldid+"_"+derecorderindex + "_id_" + linknum + "' name='field" + fieldid+"_"+derecorderindex + "_id_" + linknum + "' value='" + showid + "' >" + "\n";
	                		  inputStr += "<td ";
	                		  if (!isfrist) {
	                			  inputStr += "style='padding-left:15'";
	                		  }
	                		  inputStr += ">\n";
	                		  inputStr += "<table width='98%'>\n";
	                		  inputStr += "<tr>\n";
	                		  inputStr += "<td colspan=2 align='center'><img src='/weaver/weaver.file.FileDownload?fileid=" + docImagefileid + "&requestid=" + requestid + "' style='cursor:pointer;' alt='" + docImagefilename + "'";
	                		  if (fieldimgwidth > 0) {
	                			  inputStr += " width=" + fieldimgwidth;
	                		  }
	                		  if (fieldimgheight > 0) {
	                			  inputStr += " height=" + fieldimgheight;
	                		  }
	                		  inputStr += " onclick='addDocReadTag(\"" + showid + "\");openAccessory(\"" + docImagefileid + "\")'>\n";
	                		  inputStr += "</td>\n";
	                		  inputStr += "</tr>\n";
	                		  inputStr += "<tr>\n";
	                		  if (!canDelAcc.equals("1") || (canDelAcc.equals("1") && nodetype.equals("0"))) {
	                			  inputStr += "<td align='center'><nobr>\n";
	                			  inputStr += "<a href='#' style='text-decoration:underline' onmouseover='this.style.color='blue'' onclick='onChangeSharetype2(\"span" + fieldid+"_"+derecorderindex + "_id_" + linknum + "\",\"field" + fieldid+"_"+derecorderindex + "_del_" + linknum + "\",\"" + ismand + "\",oUpload" + fieldid+"_"+derecorderindex + ");return false;'>[<span style='cursor:pointer;color:black;'>" + SystemEnv.getHtmlLabelName(91, languageid) + "</span>]</a>\n";
	                			  inputStr += "<span id='span" + fieldid+"_"+derecorderindex + "_id_" + linknum + "' name='span" + fieldid+"_"+derecorderindex + "_id_" + linknum + "' style='visibility:hidden'><b><font COLOR='#FF0033'>√</font></b><span></td>\n";
	                		  }
	                		  if (!nodownload) {
	                			  inputStr += "<td align='center'><nobr>\n";
	                			  inputStr += "<a href='#' style='text-decoration:underline' onmouseover='this.style.color='blue'' onclick='addDocReadTag(\"" + showid + "\");downloads(\"" + docImagefileid + "\");return false;'>[<span style='cursor:pointer;color:black;'>" + SystemEnv.getHtmlLabelName(258, languageid) + "</span>]</a>\n";
	                			  inputStr += "</td>\n";
	                		  }
	                		  inputStr += "</tr>\n";
	                		  inputStr += "</table>\n";
	                		  inputStr += "</td>\n";
	                		  */
	                		  int divwidth = 120;
	                		  if (fieldimgwidth > 120) {
	                			  divwidth = fieldimgwidth;
	                		  }
	                			
	                		  inputStr += "<tr onmouseover='changecancleonnew(this)' onmouseout='changecancleoutnew(this)' style='height: 40px;'>" + "\n";
	                		  inputStr += "<input type='hidden' id='field" + fieldid+"_"+derecorderindex + "_del_" + linknum + "' name='field" + fieldid+"_"+derecorderindex + "_del_" + linknum + "' value='0' >" + "\n";
	                		  inputStr += "<input type='hidden' id='field" + fieldid+"_"+derecorderindex + "_id_" + linknum + "' name='field" + fieldid+"_"+derecorderindex + "_id_" + linknum + "' value='" + showid + "'>" + "\n";
	                		  inputStr += "<td class='fieldvalueClass' valign='middle' colSpan=3 style='padding: 0px !important;border-bottom-width:0px !important;'>" + "\n";
	                		  inputStr += "<div style='position: relative;height:"+fieldimgheight+"px;'>";
	                		  inputStr += "<div style='float:left;min-width:"+divwidth+"px;width:100%;margin:1px 0;' class='fieldClassChange'>" + "\n";
	                		  inputStr += "<img src='/weaver/weaver.file.FileDownload?fileid=" + docImagefileid +f_weaver_belongto_param+ "&requestid=" + requestid + "&desrequestid="+desrequestid+"' style='cursor:pointer;' alt='" + docImagefilename + "'";
	                		  if (fieldimgwidth > 0) {
	                			  inputStr += " width=" + fieldimgwidth;
	                		  }
	                		  if (fieldimgheight > 0) {
	                			  inputStr += " height=" + fieldimgheight;
	                		  }
	                		  inputStr += " onclick='addDocReadTag(\"" + showid + "\");openAccessory(\"" + docImagefileid + "\")'>\n";
	                		  
	                		  inputStr += "</div>" + "\n";
	                		  
	                      
	                		  if (!nodownload && isprint != 1) {
	                			  
	                			  inputStr += "<div style='display:none;float: right;position: absolute;z-index:1;right:25px;height:"+fieldimgheight+"px; line-height:"+fieldimgheight+"px;width:25px;' class='fieldClassChange' id='fielddownloadChange'>" + "\n";
	                			  inputStr += "<span id='selectDownload'>" + "\n";
	                			  inputStr += "<a style='display:inline-block;cursor:pointer;vertical-align:middle;width:20px;height:20px;background-image:url(\"/images/ecology8/workflow/fileupload/upload_wev8.png\");background-repeat :no-repeat' onclick='addDocReadTag(\"" + showid + "\");downloads(\"" + docImagefileid + "\");return false;' title='"+SystemEnv.getHtmlLabelName(31156,user.getLanguage())+"'></a>" + "\n";
	                			  inputStr += "</span>" + "\n";
	                			  inputStr += "</div>" + "\n";
	                			  
	                		  }
	                      
	                		  if ((!canDelAcc.equals("1") || (canDelAcc.equals("1") && nodetype.equals("0"))) && isprint != 1) {
	                			  inputStr += "<div class='fieldClassChange' id='fieldCancleChange' style='display:none;float: right;position: absolute;z-index:1;right:0px;height:"+fieldimgheight+"px; line-height: "+fieldimgheight+"px;width:25px;'>" + "\n";
	                			  inputStr += "<span id='span"+fieldid+"_"+derecorderindex+"_id_"+linknum+"' name='span"+fieldid+"_"+derecorderindex+"_id_"+linknum+"'>" + "\n";
	                			  inputStr += "<a style='display:inline-block;cursor:pointer;vertical-align:middle;width:20px;height:14px;background-image:url(\"/images/ecology8/workflow/fileupload/cancle_wev8.png\");background-repeat :no-repeat' onclick='onChangeSharetypeNew2(this,\"span"+fieldid+"_"+derecorderindex+"_id_"+linknum+"\",\"field"+fieldid+"_"+derecorderindex+"_del_"+linknum+"\",\""+showid+"\",\""+docImagefilename+"\","+ismand+",oUpload"+fieldid+"_"+derecorderindex+")' title='"+SystemEnv.getHtmlLabelName(91,user.getLanguage())+"'></a>" + "\n";
	                			  inputStr += "</span>" + "\n";
	                			  inputStr += "</div>" + "\n";
	                		  }
	                      
	                		  inputStr += "</div>";
	                		  inputStr += "</td></tr>" + "\n";
	                		  
	                	  } else {
	                		  String imgSrc = AttachFileUtil.getImgStrbyExtendName(fileExtendName, 20);
	                		  if(imgSrc.indexOf("style=")==-1){
	                			  imgSrc = imgSrc.replace("<img", "<img style='display:inline-block;vertical-align: top;' ");
	    	        		  }
	
	                		  inputStr += "<tr onmouseover='changecancleonnew(this)' onmouseout='changecancleoutnew(this)' style='height: 40px;'>" + "\n";
	                		  inputStr += "<input type='hidden' id='field" + fieldid+"_"+derecorderindex + "_del_" + linknum + "' name='field" + fieldid+"_"+derecorderindex + "_del_" + linknum + "' value='0' >" + "\n";
	                		  inputStr += "<td class='fieldvalueClass' valign='middle' colSpan=3 style='padding: 0px !important;border-bottom-width:0px !important;word-break: normal; word-wrap: normal;'>" + "\n";
	                		  
	                		  inputStr += "<div style='height:20px;line-height:20px;padding:10px 0; position: relative;width:100%;margin:0 0 0 0;' class='fieldClassChange'>" + "\n";
	                		  inputStr += "<div style='display:inline-block;min-width:90px;width:90%;height:20px;line-height:20px;z-index:0;vertical-align: top;overflow:hidden;white-space:nowrap;text-overflow:ellipsis;'>" + "\n";
	                		  inputStr += imgSrc + "\n";
	                		  if (accessoryCount == 1 && (Util.isExt(fileExtendName)||fileExtendName.equalsIgnoreCase("pdf"))) {
	                			  inputStr += "<a style='cursor:pointer;color:#8b8b8b !important;' onmouseover='changefileaon(this)' onmouseout='changefileaout(this)' onclick='addDocReadTag(\"" + showid + "\");openDocExt(\"" + showid + "\",\"" + versionId + "\",\"" + docImagefileid + "\",1)' title='"+docImagefilename+"'>" + docImagefilename + "</a>&nbsp;" + "\n";
	                		  } else {
	                			  inputStr += "<a style='cursor:pointer;color:#8b8b8b !important;' onmouseover='changefileaon(this)' onmouseout='changefileaout(this)' onclick='addDocReadTag(\"" + showid + "\");openAccessory(\"" + docImagefileid + "\")' title='"+docImagefilename+"'>" + docImagefilename + "</a>&nbsp;" + "\n";
	                		  }
	                		  inputStr += "</div>" + "\n";
	                		  
	                		  
	                		  
	                      
	                		  inputStr += "<input type='hidden' id='field" + fieldid+"_"+derecorderindex + "_id_" + linknum + "' name='field" + fieldid+"_"+derecorderindex + "_id_" + linknum + "' value='" + showid + "'>" + "\n";
	                      
	                		  if (accessoryCount == 1 && ((!fileExtendName.equalsIgnoreCase("xls") && !fileExtendName.equalsIgnoreCase("doc") && !fileExtendName.equalsIgnoreCase("ppt") && !fileExtendName.equalsIgnoreCase("xlsx") && !fileExtendName.equalsIgnoreCase("docx") && !fileExtendName.equalsIgnoreCase("pptx") && !fileExtendName.equalsIgnoreCase("pdf") && !fileExtendName.equalsIgnoreCase("pdfx")) || !nodownload) && additionflag ) {
	                			  inputStr += "<div style='display:none;float: right;position: absolute;z-index:1;right:25px;width:25px;height:20px; line-height: 20px;margin:0 0 0 0;' class='fieldClassChange' id='fielddownloadChange'>" + "\n";
	                			  inputStr += "<span id='selectDownload'>" + "\n";
	                			  //inputStr += "<span style='width:45px;display:inline-block;color:#898989;margin-top:1px;'>" +docImagefileSize / 1000+ "K</span>" + "\n";
	                			  inputStr += "<a style='display:inline-block;cursor:pointer;vertical-align:middle;width:20px;height:20px;background-image:url(\"/images/ecology8/workflow/fileupload/upload_wev8.png\");background-repeat :no-repeat' onclick='addDocReadTag(\"" + showid + "\");downloads(\"" + docImagefileid + "\")' title='"+SystemEnv.getHtmlLabelName(31156,user.getLanguage())+"'></a>" + "\n";
	                			  inputStr += "</span>" + "\n";
	                			  inputStr += "</div>" + "\n";
	                		  }
	                      
	                      
	                		  if (!canDelAcc.equals("1") || (canDelAcc.equals("1") && nodetype.equals("0"))) {
	                			  inputStr += "<div class='fieldClassChange' id='fieldCancleChange' style='display:none;float: right;position: absolute;z-index:1;right:0px;width:25px;height:20px; line-height: 20px;margin:0 0 0 0;'>" + "\n";
	                			  inputStr += "<span id='span"+fieldid+"_"+derecorderindex+"_id_"+linknum+"' name='span"+fieldid+"_"+derecorderindex+"_id_"+linknum+"'>" + "\n";
	                			  inputStr += "<a style='display:inline-block;cursor:pointer;vertical-align:middle;width:20px;height:14px;background-image:url(\"/images/ecology8/workflow/fileupload/cancle_wev8.png\");background-repeat :no-repeat' onclick='onChangeSharetypeNew2(this,\"span"+fieldid+"_"+derecorderindex+"_id_"+linknum+"\",\"field"+fieldid+"_"+derecorderindex+"_del_"+linknum+"\",\""+showid+"\",\""+docImagefilename+"\","+ismand+",oUpload"+fieldid+"_"+derecorderindex+")' title='"+SystemEnv.getHtmlLabelName(91,user.getLanguage())+"'></a>" + "\n";
	                			  inputStr += "</span>" + "\n";
	                			  inputStr += "</div>" + "\n";
	                      
	                		  }
	                		  inputStr += "</div>";
	                		  inputStr += "</td></tr>" + "\n";
	                	  }
	                  }// while(rs.next()){ end
	                  linknumnew = linknum;
	                  //if (type == 2 && linknum > -1) {
	                    //inputStr += "</tr>\n</table>\n</td>\n</tr>\n";
	                  //}
	              }
              }
              inputStr += "<tr>" + "\n";
              inputStr += "<td colspan=3 style='padding-right: 0px !important; padding-left: 0px !important;border-bottom-width:0px !important;'>" + "\n";
              String mainId = "";
              String subId = "";
              String secId = "";
			  
			  if(",".equals(docCategory.trim()) || ",,".equals(docCategory.trim())) {
				docCategory = "";
			  }
			  
              if (docCategory != null && !docCategory.equals("")) {
            	  int pindex = docCategory.indexOf(',');
            	  if(pindex > 0){
            		  mainId = docCategory.substring(0, docCategory.indexOf(','));
            		  subId = docCategory.substring(docCategory.indexOf(',') + 1, docCategory.lastIndexOf(','));
            		  secId = docCategory.substring(docCategory.lastIndexOf(',') + 1, docCategory.length());
            	  }else{
            		  mainId = "-1";
            		  subId = "-1";
            		  secId = docCategory;
            	  }
              }
              String picfiletypes = "*.*";
              String filetypedesc = "All Files";
              if(type == 2) {
            	  picfiletypes = new BaseBean().getPropValue("PicFileTypes", "PicFileTypes");
            	  filetypedesc = "Images Files";
              }
              boolean canupload = true;
              if(uploadType == 0) {
            	  if ("".equals(mainId) && "".equals(subId) && "".equals(secId)) {
            		  canupload = false;
            		  inputStr += "<font color='red'>" + SystemEnv.getHtmlLabelName(17616, languageid) + SystemEnv.getHtmlLabelName(92, languageid) + SystemEnv.getHtmlLabelName(15808, languageid) + "!</font>\n";
            	  }
              } else if (!isCanuse) {
            	  canupload = false;
            	  inputStr += "<font color='red'>" + SystemEnv.getHtmlLabelName(17616, languageid) + SystemEnv.getHtmlLabelName(92, languageid) + SystemEnv.getHtmlLabelName(15808, languageid) + "!</font>\n";
              }
              
              //System.out.println("canupload:"+canupload+"   uploadType:"+uploadType+"  isCanuse:"+isCanuse+"  docCategory:"+docCategory);
              
              
              if (canupload) {
            	  if("\"+rowindex+\"".equals(derecorderindex)){
            		  //addRowjsStr += "try{";
            		  //addRowjsStr += "oUpload" + fieldid+"_\"+rowindex;\n";
            		  //addRowjsStr += "}catch(e){}";
            		  //uploadfieldids.add("" + fieldid+"_"+derecorderindex);
            		  
            		  if (!selectedfieldid.equals("") && !selectedfieldid.equals("0") && uploadType!=0) {
            			  addRowjsStr += " var needChange = 1; \n";
            		  }else{
            			  addRowjsStr += " var needChange = 0; \n";
            		  }
            		  if (!selectedfieldid.equals("") && !selectedfieldid.equals("0") && uploadType!=0) {
            			  addRowjsStr += "try{  \n";
            			  addRowjsStr += "changeMaxUpload2('field"+selectedfieldid+"',rowindex); \n";
            			  addRowjsStr += "}catch(e){} \n";
            		  }
            		  
            		  addRowjsStr += "try{ \n";
            		  addRowjsStr += "fileuploadDetailtab('" + mainId + "','" + subId + "','" + secId + "'," + user.getUID() + "," + user.getLogintype() + "," + workflowid + ",'" + maxUploadImageSize + "','" + picfiletypes + "','" + filetypedesc + "',"+fieldid+",rowindex,needChange); \n";
            		  addRowjsStr += "}catch(e){} \n";
            		  
            		  
            	  }else{
            		  uploadfieldids.add("" + fieldid+"_"+derecorderindex);
                      jsStr += "var oUpload" + fieldid+"_"+derecorderindex+";\n";
            		  jsStr += "jQuery(document).ready(function(){";
            		  jsStr += "try{";
            		  String needChange = "0";
            		  if (!selectedfieldid.equals("") && !selectedfieldid.equals("0") && uploadType!=0) {
            			  needChange = "1";
            		  }
            		  jsStr += "fileuploadDetailtab('" + mainId + "','" + subId + "','" + secId + "'," + user.getUID() + "," + user.getLogintype() + "," + workflowid + ",'" + maxUploadImageSize + "','" + picfiletypes + "','" + filetypedesc + "',"+fieldid+","+derecorderindex+","+needChange+");";
            		  jsStr += "}catch(e){}";
            		  jsStr += "});\n";
            	  }

            	  inputStr += "<TABLE class='ViewForm' id='field" + fieldid+"_"+derecorderindex + "_tab_2' width='98%'>\n";
            	  inputStr += "<tr>\n";
            	  inputStr += "<td colspan=2 style='padding-right: 0px !important; padding-left: 0px !important;border-bottom-width:0px !important;'>\n";
                
            	  
            	  inputStr += "<div>" + "\n";
            	  inputStr += "<div style='float:left;'>" + "\n";
            	  inputStr += "<span id='field" + fieldid+"_"+derecorderindex + "_remind' title='"+SystemEnv.getHtmlLabelName(21406, languageid) +"'>" + "\n";
            	  inputStr += "<span id='spanButtonPlaceHolder" + fieldid+"_"+derecorderindex + "'></span>" + "\n";
            	  inputStr += "</span>" + "\n";
            	  inputStr += "</div>" + "\n";
            	  //inputStr += "<div style='width:10px!important;height:3px;float:left;'></div>" + "\n";
            	  inputStr += "<div style='float:left;'>" + "\n";
            	  
            	  
            	  //inputStr += "<button type='button' id='btnCancel" + fieldid+"_"+derecorderindex + "' disabled='disabled' style='height:25px;text-align:center;vertical-align:middle;line-height:23px;background-color: #dfdfdf;color:#999999;padding:0 10px 0 4px;' onclick='clearAllQueue(oUpload"+fieldid+"_"+derecorderindex+");showmustinput(oUpload"+fieldid+"_"+derecorderindex+");' onmouseover='changebuttonon(this)' onmouseout='changebuttonout(this)'><img src='/images/ecology8/workflow/fileupload/clearallenable_wev8.png' style='width:20px;height:20px;' align=absMiddle>"+SystemEnv.getHtmlLabelName(21407,user.getLanguage())+"</button>" + "\n";
                
            	  //inputStr += "<div style='height: 22px;vertical-align:middle;'>" + "\n";
            	  inputStr += "<span id='uploadspan_"+derecorderindex + "' viewtype='1' style='display:inline-block;line-height: 22px;'>"+SystemEnv.getHtmlLabelName(83523,user.getLanguage()) +maxUploadImageSize+SystemEnv.getHtmlLabelName(83528,user.getLanguage())+"</span> \n";
            	  if(ismand ==1 && fieldvalue.equals("")){
            		  inputStr += "<span id='field_" + fieldid+"_"+derecorderindex + "span' style='display:inline-block;line-height: 22px;color:red !important;font-weight:normal;'>"+SystemEnv.getHtmlLabelName(81909,user.getLanguage())+"</span>" + "\n";
            	  }else{
            		  inputStr += "<span id='field_" + fieldid+"_"+derecorderindex + "span' style='display:inline-block;line-height: 22px;color:red !important;font-weight:normal;'></span>" + "\n";
            	  }
            	  //inputStr += "</div>" + "\n";
            	  
            	  inputStr += "<span id='field" + fieldid+"_"+derecorderindex + "spantest' style='display:none;' >\n";
            	  if (ismand == 1 && fieldvalue.equals("")) {
            		  inputStr += "<img src='/images/BacoError_wev8.gif' align=absMiddle>\n";
            	  }
            	  inputStr += "</span>\n";
                
            	  inputStr += "</div>" + "\n";
            	  inputStr += "<div style='width:10px!important;height:3px;float:left;'></div>" + "\n";
            	  //inputStr += "<div style='height: 30px;float:left;'>" + "\n";
            	  //if(!"1".equals(forbidAttDownload) && !nodownloadnew && AttachmentCountsnew>1 && linknumnew>=0){
            		  //inputStr += "<button type='button' id='field_upload_"+fieldid+"_"+derecorderindex+"' onclick='downloadsBatch(\""+fieldvalue+"\",\""+requestid+"\")' style='height:25px;cursor:pointer;text-align:center;vertical-align:middle;line-height:25px;background-color: #6bcc44;color:#ffffff;padding:0 10px 0 4px;' onmouseover='uploadbuttonon(this)' onmouseout='uploadbuttonout(this)'><img src='/images/ecology8/workflow/fileupload/uploadall_wev8.png' style='width:20px;height:20px;padding-bottom:2px;' align=absMiddle>"+SystemEnv.getHtmlLabelName(332,user.getLanguage())+SystemEnv.getHtmlLabelName(258,user.getLanguage())+"</button>" + "\n";
            	  //}
            	  //inputStr += "</div>\n";
            	  inputStr += "<div style='clear:both;'></div>\n";
            	  inputStr += "</div>\n";
            	  inputStr += "<input  class=InputStyle  type=hidden size=60 name='field" + fieldid+"_"+derecorderindex + "' id='field" + fieldid+"_"+derecorderindex + "' temptitle='" + Util.toScreen(fieldlabel, languageid) + "'  viewtype=" + ismand + " value='" + fieldvalue + "'>\n";
            	  inputStr += "</td>\n";
            	  inputStr += "</tr>\n";
            	  inputStr += "<tr style='height:1px;'>\n";
            	  inputStr += "<td colspan=2 style='height: 1px;padding-right: 0px !important; padding-left: 0px !important;border-bottom-width:0px !important;'>\n";
            	  inputStr += "<div class='_uploadForClass'>\n";
            	  inputStr += "<div class='fieldset flash' id='fsUploadProgress" + fieldid+"_"+derecorderindex + "'>\n";
            	  inputStr += "</div>\n";
            	  inputStr += "</div>\n";
            	  inputStr += "<div id='divStatus" + fieldid+"_"+derecorderindex + "'></div>\n";
            	  inputStr += "</td>\n";
            	  inputStr += "</tr>\n";
            	  inputStr += "</TABLE>\n";
              }
              inputStr += "<input type='hidden' id='mainId_" + fieldid+"_"+derecorderindex+"' name='mainId' value='" + mainId + "'>" + "\n";
              inputStr += "<input type='hidden' id='subId_" + fieldid+"_"+derecorderindex+"' name='subId' value='" + subId + "'>" + "\n";
              inputStr += "<input type='hidden' id='secId_" + fieldid+"_"+derecorderindex+"' name='secId' value='" + secId + "'>" + "\n";
              
              inputStr += "<input type='hidden' id='type_" + fieldid+"_"+derecorderindex+"' name='type' value='" + type + "'>" + "\n";
              inputStr += "<input type='hidden' id='field" + fieldid+"_"+derecorderindex + "_idnum' name='field" + fieldid+"_"+derecorderindex + "_idnum' value='" + (linknumnew + 1) + "'>" + "\n";
              inputStr += "<input type='hidden' id='field" + fieldid+"_"+derecorderindex + "_idnum_1' name='field" + fieldid+"_"+derecorderindex + "_idnum_1' value='" + (linknumnew + 1) + "'>" + "\n";
              
              inputStr += "</td>\n";
              inputStr += "</tr>\n";
              inputStr += "</TABLE>\n";
            } else {
              
              if (type == 2) {
            	  inputStr += "<table cols='3' style='border-collapse:collapse;border:0px;width:95%;margin:0px;padding:2px 0;table-layout:fixed;' id='field" + fieldid+"_"+derecorderindex + "_tab'>" + "\n";  
              }else{
            	  inputStr += "<table cols='3' style='border-collapse:collapse;border:0px;"+(isprint==1?"width:95%":"min-width:170px;width:100%;")+";margin:0px;padding:0px;table-layout:fixed;' id='field" + fieldid+"_"+derecorderindex + "_tab'>" + "\n";
              }
              
              //inputStr += "<tbody>" + "\n";
              inputStr += "<col width='70%'>" + "\n";
              inputStr += "<col width='14%'>" + "\n";
              inputStr += "<col width='11%'>" + "\n";
              if ("-2".equals(fieldvalue)) {
                inputStr += "<tr>" + "\n";
                inputStr += "<td colSpan='3'><font color='red'>" + "\n";
                inputStr += "" + SystemEnv.getHtmlLabelName(21710, languageid) + "</font>" + "\n";
                inputStr += "</td>" + "\n";
                inputStr += "</tr>" + "\n";
              } else {
                if (!fieldvalue.equals("")) {
                  sql = "select id,docsubject,accessorycount,SecCategory from docdetail where id in(" + fieldvalue + ") order by id asc";
                  int linknum = -1;
                  int imgnum = fieldimgnum;
                  boolean isfrist = false;
                  rs.executeSql(sql);
                  int AttachmentCounts = rs.getCounts();
                  AttachmentCountsnew = AttachmentCounts;
                  while (rs.next()) {
                    isfrist = false;
                    linknum++;
                    String showid = Util.null2String(rs.getString(1));
                    String tempshowname = Util.toScreen(rs.getString(2), languageid);
                    int accessoryCount = rs.getInt(3);
                    String SecCategory = Util.null2String(rs.getString(4));
                    docImageManager.resetParameter();
                    docImageManager.setDocid(Integer.parseInt(showid));
                    docImageManager.selectDocImageInfo();

                    String docImagefileid = "";
                    long docImagefileSize = 0;
                    String docImagefilename = "";
                    String fileExtendName = "";
                    int versionId = 0;

                    if (docImageManager.next()) {
                      docImagefileid = docImageManager.getImagefileid();
                      docImagefileSize = docImageManager.getImageFileSize(Util.getIntValue(docImagefileid));
                      docImagefilename = docImageManager.getImagefilename();
                      fileExtendName = docImagefilename.substring(docImagefilename.lastIndexOf(".") + 1).toLowerCase();
                      versionId = docImageManager.getVersionId();
                    }
                    if (accessoryCount > 1) {
                      fileExtendName = "htm";
                    }
                    String imgSrc = attachFileUtil.getImgStrbyExtendName(fileExtendName, 20);
                    boolean nodownload = secCategoryComInfo.getNoDownload(SecCategory).equals("1") ? true : false;
                    nodownloadnew = nodownload;
                    //流程督办、流程兼容均屏蔽下载按钮
                    if (isprint == 1) {// 如果是打印模板，就没有下载按钮
                      nodownload = false;
                    }
                    if (type == 2) {
                      
                      inputStr += "<tr onmouseover='changecancleonnew(this)' onmouseout='changecancleoutnew(this)' style='height: 40px;'>" + "\n";
            		  inputStr += "<input type='hidden' id='field" + fieldid+"_"+derecorderindex + "_del_" + linknum + "' name='field" + fieldid+"_"+derecorderindex + "_del_" + linknum + "' value='0' >" + "\n";
            		  inputStr += "<input type='hidden' id='field" + fieldid+"_"+derecorderindex + "_id_" + linknum + "' name='field" + fieldid+"_"+derecorderindex + "_id_" + linknum + "' value='" + showid + "'>" + "\n";
            		  inputStr += "<td class='fieldvalueClass' valign='middle' colSpan=3 style='padding: 0px !important;border-bottom-width:0px !important;'>" + "\n";
            		  
            		  inputStr += "<div style='position: relative;height:"+fieldimgheight+"px;'>";
            		  inputStr += "<div style='float:left;min-width:"+(fieldimgwidth+38)+"px;width:100%;margin:1px 0;' class='fieldClassChange'>" + "\n";
            		  inputStr += "<img src='/weaver/weaver.file.FileDownload?fileid=" + docImagefileid +f_weaver_belongto_param+ "&requestid=" + requestid + "&desrequestid="+desrequestid+"' style='cursor:pointer;' alt='" + docImagefilename + "'";
            		  if (fieldimgwidth > 0) {
            			  inputStr += " width=" + fieldimgwidth;
            		  }
            		  if (fieldimgheight > 0) {
            			  inputStr += " height=" + fieldimgheight;
            		  }
            		  inputStr += " onclick='addDocReadTag(\"" + showid + "\");openAccessory(\"" + docImagefileid + "\")'>\n";
            		  
            		  inputStr += "</div>" + "\n";
            		  
                  
            		  if (!nodownload && additionflag && isprint != 1) {
            			  inputStr += "<div style='display:none;float: right;position: absolute;z-index:1;right:5px;height:"+fieldimgheight+"px; line-height:"+fieldimgheight+"px;width:25px;' class='fieldClassChange' id='fielddownloadChange'>" + "\n";
            			  inputStr += "<span id='selectDownload'>" + "\n";
            			  inputStr += "<a style='display:inline-block;cursor:pointer;vertical-align:middle;width:20px;height:20px;background-image:url(\"/images/ecology8/workflow/fileupload/upload_wev8.png\");background-repeat :no-repeat' onclick='addDocReadTag(\"" + showid + "\");top.location=\"/weaver/weaver.file.FileDownload?fileid=" + docImagefileid + "&download=1&requestid=" + requestid + "&desrequestid="+desrequestid+"\";return false;' title='"+SystemEnv.getHtmlLabelName(31156,user.getLanguage())+"'></a>" + "\n";
            			  inputStr += "</span>" + "\n";
            			  inputStr += "</div>" + "\n";
            		  }
            		  inputStr += "</div>";
            		  inputStr += "</td></tr>" + "\n";
                    } else {
                      inputStr += "<tr style='"+(isprint==1?"":"height:40px;")+"' "+(isprint!=1?"onmouseover='changecancleonnew(this)' onmouseout='changecancleoutnew(this)' ":"")+">" + "\n";
                      inputStr += "<td class='fieldvalueClass' valign='middle' colspan='3' style='padding: 0px !important;;border-bottom-width:0px !important;word-break: normal; word-wrap: normal;'>" + "\n";
                      
                      inputStr += "<div style='height:20px;line-height:20px;padding:10px 0; position: relative;width:100%;margin:0 0 0 0;' class='fieldClassChange'>" + "\n";
                      if(isprint==1){
                          inputStr += "<div style='display:inline-block;min-width:90px;width:90%;height:20px;line-height:20px;z-index:0;vertical-align: top;overflow:hidden;white-space:nowrap;text-overflow:ellipsis;' title='"+docImagefilename+"'>" + "\n";
                          if(imgSrc.indexOf("style=")==-1){
                        	  imgSrc = imgSrc.replace("<img", "<img style='display:inline-block;vertical-align: top;' ");
    	        		  }
                          inputStr += imgSrc;
    	                  inputStr += docImagefilename;
                          inputStr += "</div>" + "\n";
                      }else{
                          
                          inputStr += "<div style='display:inline-block;min-width:90px;width:90%;height:20px;line-height:20px;z-index:0;vertical-align: top;overflow:hidden;white-space:nowrap;text-overflow:ellipsis;'>" + "\n";
                    	  if(imgSrc.indexOf("style=")==-1){
                        	  imgSrc = imgSrc.replace("<img", "<img style='display:inline-block;vertical-align: top;' ");
    	        		  }
                          inputStr += imgSrc;
                      	  if (accessoryCount == 1 && (Util.isExt(fileExtendName)||fileExtendName.equalsIgnoreCase("pdf"))) {
                      		  inputStr += "<a style='cursor:pointer;color:#8b8b8b!important;margin-top:1px;' onmouseover='changefileaon(this)' onmouseout='changefileaout(this)' onclick='addDocReadTag(\"" + showid + "\");openDocExt(" + showid + ",\"" + versionId + "\",\"" + docImagefileid + "\",0)' title='"+docImagefilename+"'>" + docImagefilename + "</a>&nbsp;" + "\n";
                      	  } else {
                      		  inputStr += "<a style='cursor:pointer;color:#8b8b8b!important;margin-top:1px;' onmouseover='changefileaon(this)' onmouseout='changefileaout(this)' onclick='addDocReadTag(\"" + showid + "\");openAccessory(\"" + docImagefileid + "\")' title='"+docImagefilename+"'>" + docImagefilename + "</a>&nbsp;" + "\n";
                      	  }
                      	  inputStr += "</div>" + "\n";
                      }
                      
                      if (isviewonly == 0) {
                        inputStr += "<input type='hidden' id='field" + fieldid+"_"+derecorderindex + "_id_" + linknum + "' name='field" + fieldid+"_"+derecorderindex + "_id_" + linknum + "' value='" + showid + "'>" + "\n";
                      }
                      
                      if (((!fileExtendName.equalsIgnoreCase("xls") && !fileExtendName.equalsIgnoreCase("doc") && !fileExtendName.equalsIgnoreCase("ppt") && !fileExtendName.equalsIgnoreCase("xlsx") && !fileExtendName.equalsIgnoreCase("docx") && !fileExtendName.equalsIgnoreCase("pptx") && !fileExtendName.equalsIgnoreCase("pdf") && !fileExtendName.equalsIgnoreCase("pdfx")) || !nodownload) && additionflag && isprint != 1) {
                      	
                      	inputStr += "<div style='display:none;float: right;position: absolute;z-index:1;right:5px;width:25px;height:20px; line-height: 20px;margin:0 0 0 0;' class='fieldClassChange' id='fielddownloadChange'>" + "\n";
                  		inputStr += "<span id='selectDownload'>" + "\n";
                  		inputStr += "<a style='display:inline-block;cursor:pointer;vertical-align:middle;width:20px;height:20px;background-image:url(\"/images/ecology8/workflow/fileupload/upload_wev8.png\");background-repeat :no-repeat' onclick='addDocReadTag(\"" + showid + "\");top.location=\"/weaver/weaver.file.FileDownload?fileid=" + docImagefileid + "&download=1&requestid=" + requestid + "&desrequestid="+desrequestid+"\";return false;' title='"+SystemEnv.getHtmlLabelName(31156,user.getLanguage())+"'></a>" + "\n";
                  		inputStr += "</span>" + "\n";
                  		inputStr += "</div>" + "\n";
                      }
                      inputStr += "</div>" + "\n";
                      inputStr += "</td>" + "\n";
                      inputStr += "</tr>" + "\n";
                    }
                  }
                  linknumnew = linknum;
                  if(isprint != 1){
  	                inputStr += "<input type='hidden' id='field" + fieldid+"_"+derecorderindex + "_idnum' name='field" + fieldid+"_"+derecorderindex + "_idnum' value='" + (linknum + 1) + "'>" + "\n";
  	                inputStr += "<input type=hidden name='field" + fieldid+"_"+derecorderindex + "' value='" + fieldvalue + "'>" + "\n";
                  }
                }
              }
			  /*
              if(isprint != 1){
  	            inputStr += "<tr>" + "\n";
  	            inputStr += "<td class='fieldvalueClass' valign='middle' style='padding-right: 0px; padding-left: 0px;' colSpan=3>" + "\n";
  	            if(!"1".equals(forbidAttDownload) && !nodownloadnew && AttachmentCountsnew>1 && linknumnew>=0){
  	                inputStr += "<span onclick='top.location=\"/weaver/weaver.file.FileDownload?fieldvalue=" + fieldvalue + "&download=1&downloadBatch=1&requestid=" + requestid + "\"' style='display:inline-block;height:25px;cursor:pointer;text-align:center;vertical-align:middle;line-height:25px;background-color: #6bcc44;color:#ffffff;padding:0 20px 0 14px;' onmouseover='uploadbuttonon(this)' onmouseout='uploadbuttonout(this)'><img src='/images/ecology8/workflow/fileupload/uploadall_wev8.png' style='width:20px;height:20px;padding-bottom:2px;' align=absMiddle>"+SystemEnv.getHtmlLabelName(332,user.getLanguage())+SystemEnv.getHtmlLabelName(258,user.getLanguage())+"</span>" + "\n";
  	            }
  	            inputStr += "</td>" + "\n";
  	            inputStr += "</tr>" + "\n";
              }
			  */
              inputStr += "</table>" + "\n";
            }
          } else {
            if (isviewonly == 0) {
              if (!fieldvalue.equals("") && !fieldvalue.equals("-2")) {
                String[] fieldvalueas = Util.TokenizerString2(fieldvalue, ",");
                int linknum = -1;
                for (int j = 0; j < fieldvalueas.length; j++) {
                  linknum++;
                  String showid = Util.null2String("" + fieldvalueas[j]);
                  inputStr = "<input type='hidden' id='field" + fieldid+"_"+derecorderindex + "_id_" + linknum + "' name='field" + fieldid+"_"+derecorderindex + "_id_" + linknum + "' value='" + showid + "'>" + "\n";
                }
                inputStr = "<input type='hidden' id='field" + fieldid+"_"+derecorderindex + "_idnum' name='field" + fieldid+"_"+derecorderindex + "_idnum' value='" + linknum + 1 + "'>" + "\n";
              }
            }
          }
          
          if(changefieldsadd.indexOf(""+fieldid)>=0){
          	inputStr += "<input type='hidden' id='oldfieldview"+fieldid+"_"+derecorderindex+"' name='oldfieldview"+fieldid+"_"+derecorderindex+"' _readonly='0' value='"+(isview+isedit+ismand)+"' >";
          }
          inputStr = inputStr.replace("\n", " ");
          
      }
    } catch (Exception e) {
      inputStr = "";
      writeLog(e);
      e.printStackTrace();
    }
    
    ret_hs.put("addRowjsStr", addRowjsStr);
    ret_hs.put("jsStr", jsStr);
    ret_hs.put("inputStr", inputStr);
    otherPara_hs.put("uploadfieldids", uploadfieldids);
    return ret_hs;
  }
  
  
  public Hashtable getHtmlElementNewString(int workflowid,int isbill,int requestid,int fieldid,int derecorderindex,int ismand, int type,String fieldvalue,User user,int linknum) {
	  Hashtable ret_hs = new Hashtable();
	  String inputStr = "";
	  String sql = "";
	  try {
		  RecordSet rs = new RecordSet();
		  RecordSet rs2 = new RecordSet();
	      DocImageManager docImageManager = new DocImageManager();
	      SecCategoryComInfo secCategoryComInfo = new SecCategoryComInfo();
		  if (!fieldvalue.equals("")) {
	          sql = "select id,docsubject,accessorycount,SecCategory from docdetail where id in(" + fieldvalue + ") order by id asc";
	          rs.executeSql(sql);
	          //int linknum = -1;
	          while (rs.next()) {
	        	  linknum++;
	        	  String showid = Util.null2String(rs.getString(1));
	        	  String tempshowname = Util.toScreen(rs.getString(2), user.getLanguage());
	        	  int accessoryCount = rs.getInt(3);
	        	  String SecCategory = Util.null2String(rs.getString(4));
	        	  docImageManager.resetParameter();
	        	  docImageManager.setDocid(Integer.parseInt(showid));
	        	  docImageManager.selectDocImageInfo();

	        	  String docImagefileid = "";
	        	  String docImagefilename = "";
	        	  String fileExtendName = "";
	        	  int versionId = 0;

	        	  if (docImageManager.next()) {
	        		  // docImageManager会得到doc第一个附件的最新版本
	        		  docImagefileid = docImageManager.getImagefileid();
	        		  docImagefilename = docImageManager.getImagefilename();
	        		  fileExtendName = docImagefilename.substring(docImagefilename.lastIndexOf(".") + 1).toLowerCase();
	        		  versionId = docImageManager.getVersionId();
	              }
	        	  if (accessoryCount > 1) {
	        		  fileExtendName = "htm";
	        	  }
	        	  if (type == 2) {
	        		  int fieldimgwidth = 50;
	        	      int fieldimgheight = 50;
	        		  if (isbill == 0) {
	        			  DetailFieldComInfo detailFieldComInfo = new DetailFieldComInfo();
	        			  fieldimgwidth = Util.getIntValue(detailFieldComInfo.getImgWidth("" + fieldid)+"", 50);
	        			  fieldimgheight = Util.getIntValue(detailFieldComInfo.getImgHeight("" + fieldid)+"", 50);
	                   } else {
	                	   sql = "select bf.imgheight,bf.imgwidth from workflow_billfield bf where id="+fieldid;
	                	   rs2.executeSql(sql);
	                	   if(rs2.next()){
	                		   fieldimgwidth = Util.getIntValue(rs2.getString("imgwidth"), 50);
	 	        			   fieldimgheight = Util.getIntValue(rs2.getString("imgheight"), 50);
	                	   }
	                  }
	        		  
	        		  int divwidth = 120;
            		  if (fieldimgwidth > 120) {
            			  divwidth = fieldimgwidth;
            		  }
					  
					  //System.out.println(sql);
					  //System.out.println(fieldimgwidth);
	        		  
	        		  inputStr += "<tr onmouseover='changecancleonnew(this)' onmouseout='changecancleoutnew(this)' style='height: 40px;'>" + "\n";
	        		  inputStr += "<input type='hidden' id='field" + fieldid+"_"+derecorderindex + "_del_" + linknum + "' name='field" + fieldid+"_"+derecorderindex + "_del_" + linknum + "' value='0' >" + "\n";
	        		  inputStr += "<input type='hidden' id='field" + fieldid+"_"+derecorderindex + "_id_" + linknum + "' name='field" + fieldid+"_"+derecorderindex + "_id_" + linknum + "' value='" + showid + "'>" + "\n";
	        		  inputStr += "<td class='fieldvalueClass' valign='middle' colSpan=3 style='padding: 0px !important;border-bottom-width:0px !important;'>" + "\n";
	        		  
	        		  inputStr += "<div style='position: relative;height:"+fieldimgheight+"px;'>";
	        		  inputStr += "<div style='float:left;min-width:"+divwidth+"px;width:100%;margin:1px 0;' class='fieldClassChange'>" + "\n";
            		  inputStr += "<img src='/weaver/weaver.file.FileDownload?fileid=" + docImagefileid+ "&requestid=" + requestid + "' style='cursor:pointer;' alt='" + docImagefilename + "'";
            		  if (fieldimgwidth > 0) {
            			  inputStr += " width=" + fieldimgwidth;
            		  }
            		  if (fieldimgheight > 0) {
            			  inputStr += " height=" + fieldimgheight;
            		  }
            		  inputStr += " onclick='addDocReadTag(\"" + showid + "\");openAccessory(\"" + docImagefileid + "\")'>\n";
            		  
            		  inputStr += "</div>" + "\n";
	    			  
	    			  inputStr += "<div style='display:none;float: right;position: absolute;z-index:1;right:25px;height:"+fieldimgheight+"px; line-height:"+fieldimgheight+"px;width:25px;' class='fieldClassChange' id='fielddownloadChange'>" + "\n";
        			  inputStr += "<span id='selectDownload'>" + "\n";
        			  inputStr += "<a style='display:inline-block;cursor:pointer;vertical-align:middle;width:20px;height:20px;background-image:url(\"/images/ecology8/workflow/fileupload/upload_wev8.png\");background-repeat :no-repeat' onclick='addDocReadTag(\"" + showid + "\");downloads(\"" + docImagefileid + "\");return false;' title='"+SystemEnv.getHtmlLabelName(31156,user.getLanguage())+"'></a>" + "\n";
        			  inputStr += "</span>" + "\n";
        			  inputStr += "</div>" + "\n";
	    			  
	    			  inputStr += "<div class='fieldClassChange' id='fieldCancleChange' style='display:none;float: right;position: absolute;z-index:1;right:0px;height:"+fieldimgheight+"px; line-height: "+fieldimgheight+"px;width:25px;'>" + "\n";
        			  inputStr += "<span id='span"+fieldid+"_"+derecorderindex+"_id_"+linknum+"' name='span"+fieldid+"_"+derecorderindex+"_id_"+linknum+"'>" + "\n";
        			  inputStr += "<a style='display:inline-block;cursor:pointer;vertical-align:middle;width:20px;height:14px;background-image:url(\"/images/ecology8/workflow/fileupload/cancle_wev8.png\");background-repeat :no-repeat' onclick='onChangeSharetypeNew2(this,\"span"+fieldid+"_"+derecorderindex+"_id_"+linknum+"\",\"field"+fieldid+"_"+derecorderindex+"_del_"+linknum+"\",\""+showid+"\",\""+docImagefilename+"\","+ismand+",oUpload"+fieldid+"_"+derecorderindex+")' title='"+SystemEnv.getHtmlLabelName(91,user.getLanguage())+"'></a>" + "\n";
        			  inputStr += "</span>" + "\n";
        			  inputStr += "</div>" + "\n";

        			  inputStr += "</div>" + "\n";    			  
	        		  inputStr += "</td></tr>" + "\n";
	        		  
	        	  } else {
	        		  String imgSrc = AttachFileUtil.getImgStrbyExtendName(fileExtendName, 20);
	        		  if(imgSrc.indexOf("style=")==-1){
	        			  imgSrc = imgSrc.replace("<img", "<img style='display:inline-block;vertical-align: top;' ");
	        		  }
	        		  inputStr += "<tr onmouseover='changecancleonnew(this)' onmouseout='changecancleoutnew(this)' style='height: 40px;'>" + "\n";
	        		  inputStr += "<input type='hidden' id='field" + fieldid+"_"+derecorderindex + "_del_" + linknum + "' name='field" + fieldid+"_"+derecorderindex + "_del_" + linknum + "' value='0' >" + "\n";
	        		  inputStr += "<td class='fieldvalueClass' valign='middle' colSpan=3 style='padding: 0px !important;border-bottom-width:0px !important;word-break: normal; word-wrap: normal;'>" + "\n";
	        		  
	        		  inputStr += "<div style='height:20px;line-height:20px;padding:10px 0; position: relative;width:100%;margin:0 0 0 0;' class='fieldClassChange'>" + "\n";
            		  inputStr += "<div style='display:inline-block;min-width:90px;width:90%;height:20px;line-height:20px;z-index:0;vertical-align: top;overflow:hidden;white-space:nowrap;text-overflow:ellipsis;'>" + "\n";
            		  inputStr += imgSrc + "\n";
            		  if (accessoryCount == 1 && (Util.isExt(fileExtendName)||fileExtendName.equalsIgnoreCase("pdf"))) {
            			  inputStr += "<a style='cursor:pointer;color:#8b8b8b !important;' onmouseover='changefileaon(this)' onmouseout='changefileaout(this)' onclick='addDocReadTag(\"" + showid + "\");openDocExt(\"" + showid + "\",\"" + versionId + "\",\"" + docImagefileid + "\",1)' title='"+docImagefilename+"'>" + docImagefilename + "</a>&nbsp;" + "\n";
            		  } else {
            			  inputStr += "<a style='cursor:pointer;color:#8b8b8b !important;' onmouseover='changefileaon(this)' onmouseout='changefileaout(this)' onclick='addDocReadTag(\"" + showid + "\");openAccessory(\"" + docImagefileid + "\")' title='"+docImagefilename+"'>" + docImagefilename + "</a>&nbsp;" + "\n";
            		  }
            		  inputStr += "</div>" + "\n";
	        		  
	              
	        		  inputStr += "<input type='hidden' id='field" + fieldid+"_"+derecorderindex + "_id_" + linknum + "' name='field" + fieldid+"_"+derecorderindex + "_id_" + linknum + "' value='" + showid + "'>" + "\n";
	              
	    			  inputStr += "<div style='display:none;float: right;position: absolute;z-index:1;right:25px;width:25px;height:20px; line-height: 20px;margin:0 0;' class='fieldClassChange' id='fielddownloadChange'>" + "\n";
        			  inputStr += "<span id='selectDownload'>" + "\n";
        			  inputStr += "<a style='display:inline-block;cursor:pointer;vertical-align:middle;width:20px;height:20px;background-image:url(\"/images/ecology8/workflow/fileupload/upload_wev8.png\");background-repeat :no-repeat' onclick='addDocReadTag(\"" + showid + "\");downloads(\"" + docImagefileid + "\")' title='"+SystemEnv.getHtmlLabelName(31156,user.getLanguage())+"'></a>" + "\n";
        			  inputStr += "</span>" + "\n";
        			  inputStr += "</div>" + "\n";
        			  
	    			  inputStr += "<div class='fieldClassChange' id='fieldCancleChange' style='display:none;float: right;position: absolute;z-index:1;right:0px;width:25px;height:20px; line-height: 20px;margin:0 0 0 0;'>" + "\n";
        			  inputStr += "<span id='span"+fieldid+"_"+derecorderindex+"_id_"+linknum+"' name='span"+fieldid+"_"+derecorderindex+"_id_"+linknum+"'>" + "\n";
        			  inputStr += "<a style='display:inline-block;cursor:pointer;vertical-align:middle;width:20px;height:14px;background-image:url(\"/images/ecology8/workflow/fileupload/cancle_wev8.png\");background-repeat :no-repeat' onclick='onChangeSharetypeNew2(this,\"span"+fieldid+"_"+derecorderindex+"_id_"+linknum+"\",\"field"+fieldid+"_"+derecorderindex+"_del_"+linknum+"\",\""+showid+"\",\""+docImagefilename+"\","+ismand+",oUpload"+fieldid+"_"+derecorderindex+")' title='"+SystemEnv.getHtmlLabelName(91,user.getLanguage())+"'></a>" + "\n";
        			  inputStr += "</span>" + "\n";
        			  inputStr += "</div>" + "\n";
	              
        			  inputStr += "</div>";
	        		  inputStr += "</td></tr>" + "\n";
	        	  }
	          }
	          
	      }
	  } catch (Exception e) {
		e.printStackTrace();
	  }
  
	  ret_hs.put("inputStr", inputStr);
	  return ret_hs;
  }

}
