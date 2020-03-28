package weaver.workflow.request;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.util.Base64;
import org.apache.ws.commons.util.Base64.DecodingException;

import weaver.common.util.string.StringUtil;
import weaver.conn.RecordSet;
import weaver.docs.category.SecCategoryComInfo;
import weaver.docs.docs.DocCoder;
import weaver.docs.docs.DocComInfo;
import weaver.docs.docs.DocImageManager;
import weaver.docs.docs.DocManager;
import weaver.docs.docs.DocViewer;
import weaver.docs.docs.ImageFileIdUpdate;
import weaver.docs.webservices.DocAttachment;
import weaver.file.AESCoder;
import weaver.file.FileManage;
import weaver.file.FileUpload;
import weaver.file.multipart.DefaultFileRenamePolicy;
import weaver.general.TimeUtil;
import weaver.general.Util;
import weaver.hrm.User;
import weaver.system.SystemComInfo;
import weaver.workflow.workflow.WorkflowComInfo;
import weaver.file.ImageFileManager;

/**
 * 
 * @author Li Zhaoqi   本类用于手机版流程中的 语音附件。 
 * 因语音附件与手写签章逻辑基本相一致，故本类中也提供方法供手写签章来调用。
 */
public class WorkflowSpeechAppend {
  private static final org.apache.commons.logging.Log log = LogFactory.getLog(WorkflowSpeechAppend.class);

  public static final String FMT_SPEECHATTACHMENT = "application/mp3";
  public static final String FMT_HANDWRITTEN_SIGN = "image/png";
  
	private static final String[] suffixs = {
		"<br/><br/><span style='font-size:11px;color:#666;'>来自iPhone客户端</span>",
		"<br/><br/><span style='font-size:11px;color:#666;'>来自iPad客户端</span>",
		"<br/><br/><span style='font-size:11px;color:#666;'>来自Android客户端</span>",
		"<br/><br/><span style='font-size:11px;color:#666;'>来自android客户端</span>",
		"<br/><br/><span style='font-size:11px;color:#666;'>来自AndPad客户端</span>",
		"<br/><br/><span style='font-size:11px;color:#666;'>来自andPad客户端</span>",
		"<br/><br/><span style='font-size:11px;color:#666;'>来自Web手机版</span>",
		"<br/><br/><span style='font-size:11px;color:#666;'>来自Web手机版客户端</span>" };

	public static String getMobileSuffix(String input) {
		if (input == null || input.trim().equals("")) {
			return null;
		}
	
		for (int i = 0; i < suffixs.length; i++) {
			if (input.indexOf(suffixs[i]) > -1) {
				return suffixs[i];
			}
		}
		return null;
	}
	
	/**
	 * 对签字意见进行截取，获取其中的电子签章部分。
	 * @param remark
	 * @return
	 */
	public static String getElectrSignatrue(String remark){
		int index = remark.lastIndexOf("<br/><img alt='electricSignature'");
		if(index > 0){
			return remark.substring(index);
		}else{
			return null;
		}
	}
	
	/**
	 * 对浏览按钮的类型值进行转换，去掉多余的逗号。
	 * 1,2,   -->   1,2
	 * ,1,2,  -->   1,2
	 * @param originalVal
	 * @return
	 */
	public static String converBrowserBtnVal(String originalVal){
		log.info("Start to run 'converBrowserBtnVal' value, the value of param 'originalVal': " + originalVal);
		StringBuffer tmpString = new StringBuffer();
		String[] arrVals = originalVal.split(",");
		for (String val : arrVals) {
			tmpString.append(",").append(val);
		}
		String newValue = tmpString.toString().substring(1);
		log.info("End to run 'converBrowserBtnVal' value, return value: " + newValue);
		return newValue;
	}
	

  /**
   * 将相应数据保存至服务器上，并写入数据库附件表中。
   * 
   * @param appendData
   *          语音附件数据
   * @return 返回附件ID号。
   */
  public static int uploadAppend(String appendData, String fileType) {
    log.info("Start to upload append data.");
    byte[] arrAppendData = null;
    try {
      arrAppendData = Base64.decode(appendData);
    } catch (DecodingException e) {
      log.error("Catch a exception during decode data, return -1.", e);
      return -1;
    }

    DocAttachment docAttObj = writeData(arrAppendData);
    if (docAttObj == null) {
      log.error("It is fialure to write the data, return -1.");
      return -1;
    }
    // 设置文件格式
    docAttObj.setFiletype(fileType);

    int imageFileID = saveAttachment(docAttObj);
    if (imageFileID == -1) {
      log.error("It is fialure to save doc attenchent into database, return -1.");
      return -1;
    }

    log.info("End upload append data, return " + imageFileID);
    return imageFileID;
  }

  private static DocAttachment writeData(byte[] arrAppendData) {
    return writeData(arrAppendData, null);
  }
  
  /**
   * 将语音附件内容写入文件系统，并返回一个文档附件对象。
   * @param arrAppendData  文件数据字节
   * @param fileName 文件名称，如果为空则随机生成。
   * @return 文档附件对象
   */
  @SuppressWarnings("resource")
  private static DocAttachment writeData(byte[] arrAppendData, String fileName) {
    DocAttachment docAttObj = new DocAttachment();
   
    String saveFileName = null;
    if(StringUtil.isNullOrEmpty(fileName)){
      docAttObj.setFilename("");
      // 如果文件名称为空，则随机生成文件名称。
      saveFileName = weaver.general.Util.getRandom();
    }else{
      docAttObj.setFilename(fileName);
      saveFileName = weaver.general.Util.getRandom();
    }
    
    //读取配置信息: a、是否启用压缩；b、是否启用AES加密
    SystemComInfo syscominfo = new SystemComInfo();
    String isNeedZip = syscominfo.getNeedzip();
    String isAesEncrypt = syscominfo.getIsaesencrypt();

    OutputStream fileOut = null;
    InputStream fileInput = null;
    ZipOutputStream filezipOut = null;
    try {
      // 获取配置信息，是否需要压缩
      boolean needzip = false;
      if (isNeedZip.equals("1")) {
        needzip = true;
      }
      docAttObj.setIszip(needzip ? 1 : 0);
      fileInput = new BufferedInputStream(new ByteArrayInputStream(arrAppendData));
      
      // 获取文件保存路径
      String saveDirectory = FileUpload.getCreateDir(syscominfo.getFilesystem());
      if (saveDirectory != null) {
        FileManage.createDir(saveDirectory);
      }

      String preZipFileName = null;
      if (saveFileName != null) {
        if (needzip) {
          String body = null;
          preZipFileName = saveFileName;

          int dot = saveFileName.lastIndexOf(".");
          if (dot != -1) {
            body = saveFileName.substring(0, dot);
          } else {
            body = saveFileName;
          }
          saveFileName = body + ".zip";
        }

        // 将文件的真实保存路及名称设置到附近属性中
        File file = new File(new String(saveDirectory.getBytes("ISO8859_1"), "UTF-8"), new String(saveFileName.getBytes("ISO8859_1"), "UTF-8"));
        docAttObj.setFilerealpath(file.getPath());

        DefaultFileRenamePolicy defpolicy = new DefaultFileRenamePolicy();
        if (defpolicy != null) {
          file = defpolicy.rename(file);
          saveFileName = new String((file.getName()).getBytes("UTF-8"), "ISO8859_1");
        }

        if (needzip) {
          filezipOut = new ZipOutputStream(new FileOutputStream(file));
          filezipOut.setMethod(ZipOutputStream.DEFLATED);
          filezipOut.putNextEntry(new ZipEntry(new String(preZipFileName.getBytes("UTF-8"), "ISO8859_1")));
          fileOut = filezipOut;
        } else {
          fileOut = new BufferedOutputStream(new FileOutputStream(file));
        }
        
        // AES加密操作
        if("1".equals(isAesEncrypt)){ 
			//获取当前的 ASE加密key.
			String aesCode = Util.getRandomString(13);
			docAttObj.setAesCode(aesCode);
			docAttObj.setIsAesEncrype(Util.getIntValue(isAesEncrypt, 0));
			fileOut = AESCoder.encrypt(fileOut, aesCode);
        }

        int read;
        int size = 0;
        byte[] buf = new byte[8 * 1024];
        while ((read = fileInput.read(buf)) != -1) {
          fileOut.write(buf, 0, read);
          size += read;
        }
        fileOut.flush();
        docAttObj.setImagefilesize(size);
      }
    } catch (Exception e) {
      log.error("Catch a Exception during write data. ", e);
      return null;
    } finally {
      if (fileOut != null) {
        try {
          fileOut.close();
        } catch (Exception e) {
          log.error("Catch a exception during closing output stream.", e);
        }
      }
      if (filezipOut != null) {
        try {
          filezipOut.close();
        } catch (Exception e) {
          log.error("Catch a exception during closing output stream.", e);
        }
      }
      if (fileInput != null) {
        try {
          fileInput.close();
        } catch (Exception e) {
          log.error("Catch a exception during closing input stream.", e);
        }
      }
    }

    return docAttObj;
  }
  
  /**
   * 将附件对象信息保存到数据库中
   * 
   * @param da
   *          文件附件对象
   * @return 文件附件ID号
   */
  private static int saveAttachment(DocAttachment da) {
    int imageid = -1;
    try {
      int filesize = da.getImagefilesize();
      String filerealpath = da.getFilerealpath();
      String imagefileused = "1";
      String iszip = String.valueOf(da.getIszip());
      String isencrypt = "0";
      String originalfilename = da.getFilename();
      String contenttype = da.getFiletype();
      int isAesEncrypt = da.getIsAesEncrype();
      String aesCode = da.getAesCode();
      RecordSet rs = new RecordSet();
      char separator = Util.getSeparator();
      imageid = new ImageFileIdUpdate().getImageFileNewId();
      String para = "" + imageid + separator + originalfilename + separator + contenttype + separator + imagefileused 
          + separator + filerealpath + separator + iszip + separator + isencrypt + separator + filesize 
          + separator + isAesEncrypt + separator + aesCode;
      rs.executeProc("ImageFile_Insert_New", para);
    } catch (Exception e) {
      log.error("Catch a exception during save data into database, return -1.", e);
      return imageid;
    }
    return imageid;
  }

  /**
   * 根据附件ID号来查询数据库，再读取相应文件，并返回64编码。
   * 
   * @param imageFileID
   * @return
   */
  public static String getAppend(int imageFileID) {
    log.info("Start to get append data.");

    if (imageFileID == -1) {
      log.info("The value of the imageFileID is -1, return null.");
      return null;
    }

    // 查询数据库获取语音的相关信息(包括文件路径、文件名称)。
    DocAttachment docAttObj = getAttachment(imageFileID);
    if (docAttObj == null) {
      log.info("The 'DocAttachment' object is null, return null.");
      return null;
    }

    byte[] speedAttachment = readAttachment(docAttObj);
    if (speedAttachment == null) {
      log.info("The 'speedAttachment' object is null, return null.");
      return null;
    }

    String result = Base64.encode(speedAttachment);
    log.info("End upload append data, return correct result.");
    return result;
  }

  /**
   * 根据附件ID号来查询数据库，获取该附件的相关信息。
   * 
   * @param imageFileID
   *          附件ID号
   * @return 返回该附件对象的相关信息。
   */
  public static DocAttachment getAttachment(int imageFileID) {
    DocAttachment docAttObj = null;
    RecordSet recordSet = new RecordSet();
    String sql = "Select * from ImageFile where imagefileid = " + imageFileID;
    recordSet.execute(sql);
    // 根据主键查询仅一条记录。
    if (recordSet.next()) {
      docAttObj = new DocAttachment();
      docAttObj.setImagefileid(imageFileID);
      docAttObj.setFilename(recordSet.getString("imageFileName"));
      docAttObj.setFiletype(recordSet.getString("imageFiletype"));
      docAttObj.setFileused(recordSet.getInt("imagefileused"));
      docAttObj.setFilerealpath(recordSet.getString("filerealpath"));
      docAttObj.setIszip(recordSet.getInt("iszip"));
      docAttObj.setIsencrype(recordSet.getInt("isencrypt"));
      docAttObj.setImagefilesize(recordSet.getInt("fileSize"));
      docAttObj.setIsAesEncrype(recordSet.getInt("isaesencrypt"));
      docAttObj.setAesCode(recordSet.getString("aescode"));
    }
    return docAttObj;
  }

  /**
   * 从文件系统中读取文件数据。
   * 
   * @param docAttObj
   *          附件信息对象
   * @return 返回附件的相应字节码
   */
  private static byte[] readAttachment(DocAttachment docAttObj) {
    // 文件格式是否压缩
    boolean isZip = docAttObj.getIszip() == 1;
    String fileRealPath = docAttObj.getFilerealpath();

    byte[] result = null;
    InputStream inStream = null;
   // ZipInputStream zipStream = null;
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    try {
      /*if (isZip) {
        zipStream = new ZipInputStream(new FileInputStream(fileRealPath));
        if (zipStream.getNextEntry() != null) {
          inStream = new BufferedInputStream(zipStream);
        }
      } else {
        inStream = new BufferedInputStream(new FileInputStream(fileRealPath));
      }*/
      
      //是否AES加密
      /*int isAesEncrypt = docAttObj.getIsAesEncrype();
      String aesCode = docAttObj.getAesCode();
      if(isAesEncrypt == 1){
    	  inStream = AESCoder.decrypt(inStream, aesCode);
      }*/
      ImageFileManager imageFileManager=new ImageFileManager();
	  imageFileManager.getImageFileInfoById(docAttObj.getImagefileid());
	  inStream=imageFileManager.getInputStream();

      int i;
      byte[] buffer = new byte[8 * 1024];
      while ((i = inStream.read(buffer)) != -1) {
        outStream.write(buffer, 0, i);
      }
      result = outStream.toByteArray();
    } catch (Exception e) {
      log.error("Catch a exception during reading data from file system.", e);
      return null;
    } finally {
      if (inStream != null) {
        try {
          inStream.close();
        } catch (Exception e) {
          log.error("Catch a exception during closing input stream.", e);
        }
      }
      try {
        outStream.close();
      } catch (Exception e) {
        log.error("Catch a exception during closing output stream.", e);
      }
    }
    return result;
  }

  /**
   * 根据页面上传入参数，来获取当前流程附件上传的目录。
   * @param workflowID
   * @param fu
   * @return
   */
  public static int[] getDocCategory(String workflowID, FileUpload fu) {
    // 非空检查
    if (workflowID == null) {
      return null;
    }

    // 调用公共方法获取当前流程的附件上传目录类型
    String docCategory = WorkflowComInfo.getDocCategory(workflowID);
    log.info("The original 'docCategory' value :\t" + docCategory);
    if (docCategory == null || "".equals(docCategory)) {
      return null;
    }

    int[] result = null;
    // 附件上传目录设置成固定目录
    if (docCategory.indexOf("field") != 0) {
      String[] arrCategory = docCategory.split(",");
      result = new int[arrCategory.length];
      for (int i = 0; i < arrCategory.length; i++) {
        result[i] = Util.getIntValue(arrCategory[i]);
      }
      return result;
      // 附件上传目录设置成选择框
    } else {
      if (fu == null) {
        return null;
      }

      String fieldValue = fu.getParameter3(docCategory);
      log.info("The 'fieldValue' value :\t" + fieldValue);
      fieldValue = (fieldValue == null || "".equals(fieldValue)) ? "0" : fieldValue;

      String fieldID = docCategory.substring(5);
      String sql = String.format("select docCategory from workflow_selectitem where selectvalue = %1$s and fieldid = %2$s", fieldValue, fieldID);
      log.info("Following is the run SQL : \n" + sql);

      RecordSet rs = new RecordSet();
      rs.executeSql(sql);
      if (rs.next()) {
        docCategory = Util.null2String(rs.getString("docCategory"));
      }

      log.info("The real 'docCategory' value :\t" + docCategory);
      if (docCategory == null || "".equals(docCategory)) {
        return null;
      }

      String[] arrCategory = docCategory.split(",");
      result = new int[arrCategory.length];
      for (int i = 0; i < arrCategory.length; i++) {
        result[i] = Util.getIntValue(arrCategory[i]);
      }
      return result;
    }
  }

  /**
   * 上传文档。
   * @param strData 需上传附件的数据
   * @param fileName 文件名称
   * @param user 当前用户
   * @param docCategory 上传文档对象
   * @param remoteAddr 客户IP地址
   * @return
   */
  public static int uploadAppdix(String strData, String fileName, User user, int[] docCategory, String remoteAddr) {
    log.info("Start to upload append data.");
    //如果文档保存目录为空，则直接返回。
    if(docCategory == null || docCategory.length < 3){
      log.info("The parameter 'docCategory' is null, return -1.");
      return -1;
    }
    int mainCategory = docCategory[0];
    int subCategory = docCategory[1];
    int secCategory = docCategory[2];
    
    //对数据进行解码
    byte[] arrAppendData = null;
    try {
      arrAppendData = Base64.decode(strData);
    } catch (DecodingException e) {
      log.error("Catch a exception during decode data, return -1.", e);
      return -1;
    }
    
    DocAttachment docAttObj = writeData(arrAppendData, fileName);
    if (docAttObj == null) {
      log.error("It is fialure to write the data, return -1.");
      return -1;
    }
    // 设置文件格式
    docAttObj.setFiletype("application/octet-stream");
    
    int imageFileID = saveAttachment(docAttObj);
    if (imageFileID == -1) {
      log.error("It is fialure to save doc attenchent into database, return -1.");
      return -1;
    }
    
    try {
      DocViewer dv = new DocViewer();
      DocComInfo dc = new DocComInfo();
      SecCategoryComInfo scc = new SecCategoryComInfo();
      //文档状态
      String docStatus = "1";
      //获取文档ID
      DocManager dm = new DocManager();
      int docId = dm.getNextDocId(new RecordSet());
      
      DocImageManager imgManger = new DocImageManager();
      imgManger.resetParameter();
      imgManger.setImagefilename(fileName);
      //获取文档主名称
      String docsubject = getFileMainName(fileName);
      //获取并根据文档扩展名来设置文档类型。
      String ext = getFileExt(fileName);
      if (ext.equalsIgnoreCase("doc")) {
        imgManger.setDocfiletype("3");
      } else if (ext.equalsIgnoreCase("xls")) {
        imgManger.setDocfiletype("4");
      } else if (ext.equalsIgnoreCase("ppt")) {
        imgManger.setDocfiletype("5");
      } else if (ext.equalsIgnoreCase("wps")) {
        imgManger.setDocfiletype("6");
      } else if (ext.equalsIgnoreCase("docx")) {
        imgManger.setDocfiletype("7");
      } else if (ext.equalsIgnoreCase("xlsx")) {
        imgManger.setDocfiletype("8");
      } else if (ext.equalsIgnoreCase("pptx")) {
        imgManger.setDocfiletype("9");
      } else if (ext.equalsIgnoreCase("et")) {
        imgManger.setDocfiletype("10");
      } else {
        imgManger.setDocfiletype("2");
      }
      imgManger.setDocid(docId);
      imgManger.setImagefileid(imageFileID);
      imgManger.setIsextfile("1");
      imgManger.AddDocImageInfo();

      String date = TimeUtil.getCurrentDateString();
      String time = TimeUtil.getOnlyCurrentTimeString();
      dm.setId(docId);
      dm.setMaincategory(mainCategory);
      dm.setSubcategory(subCategory);
      dm.setSeccategory(secCategory);
      dm.setLanguageid(user.getLanguage());
      dm.setDoccontent("");
      dm.setDocstatus(docStatus);
      dm.setDocsubject(docsubject);
      dm.setDoccreaterid(user.getUID());
      dm.setDocCreaterType(user.getLogintype());
      dm.setUsertype(user.getLogintype());
      dm.setOwnerid(user.getUID());
      dm.setOwnerType(user.getLogintype());
      dm.setDoclastmoduserid(user.getUID());
      dm.setDocLastModUserType(user.getLogintype());
      dm.setDoccreatedate(date);
      dm.setDoclastmoddate(date);
      dm.setDoccreatetime(time);
      dm.setDoclastmodtime(time);
      dm.setDoclangurage(user.getLanguage());
      dm.setKeyword(docsubject);
      dm.setIsapprover("0");
      dm.setIsreply("");
      dm.setDocdepartmentid(user.getUserDepartment());
      dm.setDocreplyable("1");
      dm.setAccessorycount(1);
      dm.setParentids("" + docId);
      dm.setOrderable("" + scc.getSecOrderable(secCategory));
      dm.setClientAddress(remoteAddr);
      dm.setUserid(user.getUID());
      DocCoder docCoder = new DocCoder();
      dm.setDocCode(docCoder.getDocCoder("" + secCategory));
      int docEdition = -1;
      int docEditionId = -1;
      //如果版本管理开启
      if (scc.isEditionOpen(secCategory)) {
        //如果存在历史版本, 通过版本ID找到同文档的版本。新建文档不存在以前有版本。
        if (docEditionId == -1) {
          docEditionId = dm.getNextEditionId(new RecordSet());
        }
        docEdition = dc.getEdition(docEditionId) + 1;
      }
      dm.setDocEditionId(docEditionId);
      dm.setDocEdition(docEdition);
      dm.AddDocInfo();
      //设置共享
      dm.AddShareInfo();
      dc.addDocInfoCache("" + docId);
      dv.setDocShareByDoc("" + docId);
      return docId;
    } catch (Exception ex) {
      log.error("Catch a exception.", ex);
      return -1;
    }
  }

  /**
   * 获取文件的扩展名
   * @param fileName
   * @return 文件主名称 
   * 如果传入 abc_wev8.jpg 返回 abc
   */
  private static String getFileMainName(String fileName) {
    if (fileName == null){
      return "";
    }
    int pos = fileName.lastIndexOf(".");
    if (pos > -1) {
      fileName = fileName.substring(0, pos);
    }
    return fileName;
  }

  /**
   * 获取文件的扩展名
   * @param file 文件全名
   * @return  文件的扩展名
   * 如果传入 abc_wev8.jpg 返回 jpg
   */
  public static String getFileExt(String file) {
    if (file == null || file.trim().equals("")) {
      return "";
    } else {
      int idx = file.lastIndexOf(".");
      if (idx == -1) {
        return "";
      } else {
        if (idx + 1 >= file.length()) {
          return "";
        } else {
          return file.substring(idx + 1);
        }
      }
    }
  }
  
  /**
   * 根据clientType来判断当前流程处理是否来源于手机版
   * 如果为null或empty，都为false；如果为0，亦为false；
   * 如果为大于0的任意数字，则为true 。
   * @param clientType
   * @return
   */
  public static boolean isFromMobile(String clientType){
	if(clientType == null){
		return false;
	}
	
	clientType = StringUtils.trim(clientType);
	if("".equals(clientType) || "0".equals(clientType)){
		return false;
	}
	
	int intVal = Util.getIntValue(clientType, 0);
	return intVal > 0;
  }
  
  /**
   * 根据请求ID号，获取当前流程所有已操作人
   * @param requestID
   * @return
   */
  public static String getAllOperateredResource(String requestID){
	log.info("Start to invoke 'getAllOperateredResource' method.");
	if(StringUtils.isEmpty(requestID)){
		log.warn("The requestid is emtpy, program return emtpy directly.");
		return "";
	}
	  
	String sql = "Select distinct operator from workflow_requestLog where requestid = %1$s";
	sql = String.format(sql, requestID);
	log.info("Following is the run sql:\n" + sql);
	
	RecordSet rs = new RecordSet();
	rs.executeSql(sql);
	StringBuffer sb = new StringBuffer();
	while(rs.next()){
		sb.append(",").append(rs.getString(1));
	}
	
	String result = sb.toString();
	result = ("".equals(result)) ? "" : result.substring(1);
	return result;
  }
  
  /**
   * 上传图片。
   * @param strData 需上传附件的数据
   * @param fileName 文件名称
   * @param user 当前用户
   * @return
   */
  public static int uploadImage(String strData, String fileName, User user) {
    log.info("Start to upload append data.");
    
    //对数据进行解码
    byte[] arrAppendData = null;
    try {
      arrAppendData = Base64.decode(strData);
    } catch (DecodingException e) {
      log.error("Catch a exception during decode data, return -1.", e);
      return -1;
    }
    
    DocAttachment docAttObj = writeData(arrAppendData, fileName);
    if (docAttObj == null) {
      log.error("It is fialure to write the data, return -1.");
      return -1;
    }
    // 设置文件格式
    docAttObj.setFiletype("application/octet-stream");
    
    int imageFileID = saveAttachment(docAttObj);
    if (imageFileID == -1) {
      log.error("It is fialure to save doc attenchent into database, return -1.");
      return -1;
    }
    
    return imageFileID;
  }
}