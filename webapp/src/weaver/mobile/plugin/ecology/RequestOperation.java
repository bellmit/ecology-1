package weaver.mobile.plugin.ecology;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.weaver.formmodel.mobile.MobileFileUpload;
import com.weaver.formmodel.mobile.utils.MobileCommonUtil;

import weaver.file.FileUpload;
import weaver.general.Util;
import weaver.hrm.User;
import weaver.mobile.webservices.workflow.WorkflowDetailTableInfo;
import weaver.mobile.webservices.workflow.WorkflowMainTableInfo;
import weaver.mobile.webservices.workflow.WorkflowRequestInfo;
import weaver.mobile.webservices.workflow.WorkflowRequestTableField;
import weaver.mobile.webservices.workflow.WorkflowRequestTableRecord;
import weaver.workflow.datainput.DynamicDataInput;
import weaver.workflow.mode.FieldInfo;
import weaver.workflow.request.WorkflowSpeechAppend;
import weaver.mobile.webservices.workflow.WorkflowServiceUtil;


/**
 * 本类中用于定义mobile\plugin\1\RequestOperation.jsp下所调用的方法。
 * 
 * @author Liulyx
 * 
 */
public class RequestOperation {
  private static final Log log = LogFactory.getLog(RequestOperation.class);

  /**
   * 已转换的单据ID号。
   * 对应于Mobile3.0程序中cn.com.weaver.Constants.AVAILABLE_WORKFLOW常量值。
   */
  final public static String AVAILABLE_WORKFLOW = "7,13,46,49,74,79,158,181,182,200,10,11,156,28,180,14,159,38,85,18,19,201,224,17,21,163,157,45";

  public static WorkflowRequestInfo getWorkflowRequestInfoFromRequest(FileUpload fileUpload, WorkflowRequestInfo workflowRequestInfo, User userObj, int[] docCategory) {
    log.info("Start to invoke the 'getWorkflowRequestInfoFromRequest' method.");
    WorkflowRequestInfo result = workflowRequestInfo;
    
    String requestID = result.getRequestId();
    String workflowID = result.getWorkflowBaseInfo().getWorkflowId();
	String formID = result.getWorkflowBaseInfo().getFormId();
	log.info("The value of the 'requestID' is:\t" + requestID);
	log.info("The value of the 'workflowID' is:\t" + workflowID);
	log.info("The value of the 'formID' is:\t" + formID);
	log.info("Following is the info of the 'userObj' is:\n" + ReflectionToStringBuilder.toString(userObj));

    try {
      if (result != null) {
        String fieldformname = "requestname";
        String fieldformvalue = fileUpload.getParameter3(fieldformname);
        result.setRequestName(fieldformvalue==null?"":fieldformvalue.replace("\'","&#39;"));

        fieldformname = "requestlevel";
        fieldformvalue = fileUpload.getParameter3(fieldformname);
        result.setRequestLevel(fieldformvalue);

        fieldformname = "messageType";
        fieldformvalue = fileUpload.getParameter3(fieldformname);
        result.setMessageType(fieldformvalue);
        
        //微信提醒
        fieldformname = "chatsType";
        fieldformvalue = fileUpload.getParameter3(fieldformname);
        result.setChatsType(fieldformvalue);

        WorkflowMainTableInfo wmti = result.getWorkflowMainTableInfo();
        // 判断 MainTable(主表)非空性
        if (wmti != null) {
          // 获取主表字段信息列表，并检查非空性
          WorkflowRequestTableRecord[] wrtrs = wmti.getRequestRecords();

          if (wrtrs != null && wrtrs[0] != null) {
            String remoteAddr = fileUpload.getRemoteAddr();
            
            //获取所有被字段联动设置值的字段列表
            List assignmentFieldNames = DynamicDataInput.getAssignmentFieldsByWorkflowID(workflowRequestInfo.getWorkflowBaseInfo().getWorkflowId(), 0);
            
			FieldInfo fieldinfo = new FieldInfo();
			fieldinfo.getHtmlAttrField_mobile(Util.getIntValue(result.getNodeId(), 0), (ArrayList)assignmentFieldNames);
            
            // 对主表各字段信息进行循环
            for (int i = 0; i < wrtrs[0].getWorkflowRequestTableFields().length; i++) {
              WorkflowRequestTableField wrtf = wrtrs[0].getWorkflowRequestTableFields()[i];
              getFieldFormInfo(fileUpload, userObj, docCategory, remoteAddr,
					assignmentFieldNames, wrtf,formID,result.isCanEdit());
            }
          }
        }
        WorkflowDetailTableInfo[]  wdtis = result.getWorkflowDetailTableInfos();
        if(null!=wdtis){
        	 for(int i=0;i<wdtis.length;i++){
        		 WorkflowDetailTableInfo wdt=wdtis[i];
        		 WorkflowRequestTableRecord[] wrtrs= wdt.getWorkflowRequestTableRecords();
        		 //复制第一条数据放入其他的文件中
        		 String nodeNum=fileUpload.getParameter3("nodenum"+i);
        		 int rowInt = Util.getIntValue(nodeNum,0);
        		 String deleteIds = fileUpload.getParameter3("deleteId"+i);
        		 wdt.setDeleteIds(deleteIds);
        		 wdt.setRowcount(""+rowInt);
				 String deleteRowIndexs = fileUpload.getParameter3("deleteRowIndex"+i);
				 if(rowInt<=0){continue;}
        		 WorkflowRequestTableRecord[] totalWrtrs=new  WorkflowRequestTableRecord[rowInt];
    			 //WorkflowRequestTableRecord[] wrtrs2=new WorkflowRequestTableRecord[rowInt-1];
    			 if(wrtrs.length >0){
    				 for(int k=0;k<wrtrs.length;k++){
    					 totalWrtrs[k] = wrtrs[k];
    				 }
    				 //将第一条数据作为模块
    				 WorkflowRequestTableRecord   wrtrMode=wrtrs[0];
    				 if(rowInt - wrtrs.length > 0){
    					 for(int m=0;m<rowInt-wrtrs.length;m++){
    						 WorkflowRequestTableRecord   wrtr =new WorkflowRequestTableRecord();
    						 wrtr.setRecordOrder(wrtrMode.getRecordOrder());
    						 WorkflowRequestTableField[] wrtfModels= wrtrMode.getWorkflowRequestTableFields();
    						 int length =wrtfModels.length;
    						 WorkflowRequestTableField[] wrtrValues= new WorkflowRequestTableField[length];
    						 for(int g=0;g<length;g++){
    							WorkflowRequestTableField wrtfModel= wrtfModels[g];
    							WorkflowRequestTableField wrtrValue= new WorkflowRequestTableField();
    							wrtrValue.setBrowserurl(wrtfModel.getBrowserurl());
    							wrtrValue.setFieldDBType(wrtfModel.getFieldDBType());
    							String fieldFormName = "field"+wrtfModel.getFieldId()+"_"+(m+wrtrs.length);
    							wrtrValue.setFieldFormName(fieldFormName);
    							wrtrValue.setFieldHtmlType(wrtfModel.getFieldHtmlType());
    							wrtrValue.setFieldId(wrtfModel.getFieldId());
    							wrtrValue.setFieldName(wrtfModel.getFieldName());
    							wrtrValue.setFieldOrder(wrtfModel.getFieldOrder());
    							wrtrValue.setFieldShowName(wrtfModel.getFieldShowName());
    							wrtrValue.setFieldShowValue(wrtfModel.getFieldShowValue());
    							wrtrValue.setFieldType(wrtfModel.getFieldType());
    							wrtrValue.setFieldValue(wrtfModel.getFieldValue());
    							wrtrValue.setFiledHtmlShow(wrtfModel.getFiledHtmlShow());
    							wrtrValue.setMand(wrtfModel.isMand());
    							wrtrValue.setEdit(wrtfModel.isEdit());
    							wrtrValue.setSelectnames(wrtfModel.getSelectnames());
    							wrtrValue.setSelectvalues(wrtfModel.getSelectvalues());
    							wrtrValue.setView(wrtfModel.isView());
    							wrtrValues[g] =wrtrValue;
    						 }
    						 wrtr.setWorkflowRequestTableFields(wrtrValues);
    						// wrtrs2[m] = wrtr;
    						 totalWrtrs[m+wrtrs.length] = wrtr;
        				 }
    				 }
					
    			 }else{
						  String nodeid =result.getNodeId();
						  String detailTableName =wdt.getTableDBName();
						  WorkflowRequestTableRecord[]  wrtrsRecodes= WorkflowServiceUtil.getWorkflowReqeustTableRecordWhenNull(workflowID,nodeid,detailTableName,userObj);
						   if(null != wrtrsRecodes[0]){
                                 WorkflowRequestTableRecord   wrtrMode=wrtrsRecodes[0];
							     for(int m=0;m<rowInt;m++){
										 WorkflowRequestTableRecord   wrtr =new WorkflowRequestTableRecord();
										 wrtr.setRecordOrder(wrtrMode.getRecordOrder());
										 WorkflowRequestTableField[] wrtfModels= wrtrMode.getWorkflowRequestTableFields();
										 int length =wrtfModels.length;
										 WorkflowRequestTableField[] wrtrValues= new WorkflowRequestTableField[length];
										 for(int g=0;g<length;g++){
											WorkflowRequestTableField wrtfModel= wrtfModels[g];
											WorkflowRequestTableField wrtrValue= new WorkflowRequestTableField();
											wrtrValue.setBrowserurl(wrtfModel.getBrowserurl());
											wrtrValue.setFieldDBType(wrtfModel.getFieldDBType());
											String fieldFormName = "field"+wrtfModel.getFieldId()+"_"+m;
											wrtrValue.setFieldFormName(fieldFormName);
											wrtrValue.setFieldHtmlType(wrtfModel.getFieldHtmlType());
											wrtrValue.setFieldId(wrtfModel.getFieldId());
											wrtrValue.setFieldName(wrtfModel.getFieldName());
											wrtrValue.setFieldOrder(wrtfModel.getFieldOrder());
											wrtrValue.setFieldShowName(wrtfModel.getFieldShowName());
											wrtrValue.setFieldShowValue(wrtfModel.getFieldShowValue());
											wrtrValue.setFieldType(wrtfModel.getFieldType());
											wrtrValue.setFieldValue(wrtfModel.getFieldValue());
											wrtrValue.setFiledHtmlShow(wrtfModel.getFiledHtmlShow());
											wrtrValue.setMand(wrtfModel.isMand());
											wrtrValue.setEdit(wrtfModel.isEdit());
											wrtrValue.setSelectnames(wrtfModel.getSelectnames());
											wrtrValue.setSelectvalues(wrtfModel.getSelectvalues());
											wrtrValue.setView(wrtfModel.isView());
											wrtrValues[g] =wrtrValue;
										 }
										 wrtr.setWorkflowRequestTableFields(wrtrValues);
										 totalWrtrs[m] = wrtr;
        				         }   
						}
					 }
    			 wdt.setWorkflowRequestTableRecords(totalWrtrs);
    			  //end
				 for(int j=0;j<totalWrtrs.length;j++){
					 WorkflowRequestTableRecord wrtr= totalWrtrs[j];
					 if(!"".equals(deleteRowIndexs)){
						 wrtr.setIsDelete(isDeleteRow(j,deleteRowIndexs));
					 }
					 WorkflowRequestTableField[] wrtfs= wrtr.getWorkflowRequestTableFields();
					 String remoteAddr = fileUpload.getRemoteAddr();
						//获取所有被字段联动设置值的字段列表
					List assignmentFieldNames = DynamicDataInput.getAssignmentFieldsByWorkflowID(workflowRequestInfo.getWorkflowBaseInfo().getWorkflowId(), 1);
					FieldInfo fieldinfo = new FieldInfo();
					fieldinfo.getHtmlAttrField(Util.getIntValue(result.getNodeId(), 0), (ArrayList)assignmentFieldNames);
					 for(int k=0;k<wrtfs.length;k++){
						 WorkflowRequestTableField wrtf=wrtfs[k];
//						 getFieldFormInfo(fileUpload, userObj, docCategory, remoteAddr,
//								assignmentFieldNames, wrtf,formID);
                         getFieldFormInfo(fileUpload, userObj, docCategory, remoteAddr,
	                              assignmentFieldNames, wrtf,formID,true,"1".equals(wdt.getIsAdd()));
						
					 }
				 }
        	 }
        }
      }

      log.info("End run the 'getWorkflowRequestInfoFromRequest' method, and return the value.");
      return result;
    } catch (Exception e) {
    	e.printStackTrace();
      log.error("Catch a exception .", e);
      return result;
    }
  }
  
  private static boolean isDeleteRow(int row,String rows){
		boolean result = false;
		String[] rowsArray = rows.split(",");
		int rowsCount = rowsArray.length;
		for(int i=0;i<rowsCount;i++){
			String rowValue = rowsArray[i];
			 if(!"".equals(rowValue)){
				  if(Util.getIntValue(rowValue) == row){
					  result = true;
					  break;
				  }
			 }
		}
		return result;
  }

  private static void getFieldFormInfo(FileUpload fileUpload, User userObj,
          int[] docCategory, String remoteAddr, List assignmentFieldNames,
          WorkflowRequestTableField wrtf,String formId) {
      getFieldFormInfo(fileUpload,userObj,docCategory,remoteAddr,assignmentFieldNames,
              wrtf,formId, true);
  }

  private static void getFieldFormInfo(FileUpload fileUpload, User userObj,
		int[] docCategory, String remoteAddr, List assignmentFieldNames,
		WorkflowRequestTableField wrtf,String formId,boolean isCanEdit) {
	String fieldformname;
	String fieldformvalue;
	if (wrtf != null) {
	    // 字段名称
	    String fieldName = wrtf.getFieldName();
	    // 字段HTML类型
	    String fieldHtmlType = wrtf.getFieldHtmlType();
	    fieldformname = wrtf.getFieldFormName();
		String cValFields = fieldformname;
	    if(fieldformname.indexOf("_")>=0&&fieldformname.indexOf("span")==-1){
			cValFields = fieldformname.split("_")[0];
		}
	    
	    String meteth = fileUpload.getParameter("method2");
	   if (fieldName != null && !"".equals(fieldName) && wrtf.isView() && ("create".equals(meteth) || wrtf.isEdit() || assignmentFieldNames.indexOf(cValFields) != -1
			||WorkflowServiceUtil.isRowColumnRule(formId,fieldformname) || (wrtf.getFieldHtmlType().equals("9") && isCanEdit))) {
	      // 如果是“附件上传”类型，则需作特殊处理。
	      if ("6".equals(fieldHtmlType) && docCategory != null) {
	    	MobileCommonUtil MobileCommonUtil = new MobileCommonUtil();
	        String fieldValue = Util.null2String(fileUpload.getParameter3("filesaveid_"+fieldformname));
//	        MobileFileUpload fileUpload2 = new MobileFileUpload(fileUpload.getRequest(), "UTF-8", false);
	           //String docIdContents = MobileCommonUtil.uploadFile4(fileUpload, userObj,fieldformname);
	        // 新增附件上传 的 文档ID号
//	        String newDocIDs = "";
//	        String cntFieldName = "cnt" + fieldformname;
//	        // 获取该附件上传字段 新增附件数。
//	        int cntAppendfix = 1;// Util.getIntValue(fileUpload.getParameter3(cntFieldName), -1);
//	        if (cntAppendfix > 0) {
//	          for (int j = 0; j < cntAppendfix; j++) {
//	            String strData = fileUpload.getParameter3(fieldformname + "_" + j);
//	            // 新增附件可能被删除，需作判断。
//	            if (strData != null) {
//	              String strFileName = fileUpload.getParameter3(fieldformname + "name_" + j);
//	              int docID = WorkflowSpeechAppend.uploadAppdix(strData, strFileName, userObj, docCategory, remoteAddr);
//	              if (docID > -1) {
//	                newDocIDs += docID + ",";
//	              }
//	            }
//	          }
//	        }
	        String docIdContents = "";
	        String fielddata = Util.null2String(fileUpload.getParameter3("filedata_"+fieldformname));
	        String filenames = Util.null2String(fileUpload.getParameter3("filenames_"+fieldformname));
	        String[] fielddataArray = fielddata.split("_file_data_split_");
	        String[] filenamesArray = filenames.split(",");
	        for (int i = 0; i < fielddataArray.length; i++) {
	        	String temp = Util.null2String(fielddataArray[i]);
	        	String tempname = Util.null2String(filenamesArray[i]);
	        	if(tempname.length()==0){
	        		continue;
	        	}
	        	String extstr = tempname.substring(tempname.lastIndexOf(".")).toLowerCase();
	        	if(".png,.jpg,.tiff,.doc,.docx,.xls,.xlsx,.pptx,.ppt,.pdf,.txt,.jpeg,".indexOf(extstr+",")==-1){
	        		continue;
	        	}
	        	if(temp.length()==0){
	        		continue;
	        	}
	        	//判断附件类型过滤掉
	        	//if(tempname.endsWith(suffix))
	        	if(temp.indexOf("base64,")>-1){
	        		temp = temp.substring(temp.indexOf("base64,")+7);
	        	}
	        	int docID = WorkflowSpeechAppend.uploadAppdix(temp, tempname, userObj, docCategory, remoteAddr);
	        	docIdContents+=","+docID;
			}
	        if(docIdContents.length()>0){
	        	docIdContents = docIdContents.substring(1);
	        }
	        
	        fieldformvalue = docIdContents;
	        String tmpfieldvalue = "";
	        if(fieldValue.length()>0){
	        	tmpfieldvalue += ","+fieldValue;
	        }
	        if(fieldformvalue.length()>0){
	        	tmpfieldvalue += ","+fieldformvalue;
	        }
	        if(tmpfieldvalue.length()>0){
	        	tmpfieldvalue = tmpfieldvalue.substring(1);
	        }
            wrtf.setFieldValue(tmpfieldvalue);
	      } else {
	        fieldformvalue = fileUpload.getParameter3(fieldformname);
			if("2".equals(fieldHtmlType)){ 
                    	if(fieldformvalue !=null && fieldformvalue.indexOf("ke-content-div-mobil")>0){ //富文本专用标记，如果是有该div则提交时去掉该div
                    		fieldformvalue = fieldformvalue.substring(fieldformvalue.indexOf(">")+1, fieldformvalue.length());
                    		fieldformvalue = fieldformvalue.substring(0,fieldformvalue.lastIndexOf("</div>"));
                    	}
              }
	        if (fieldformvalue != null) {
	          if ("3".equals(wrtf.getFieldHtmlType())) {
	            String[] values = StringUtils.split(fieldformvalue, ",");
	            fieldformvalue = "";
	            for (int p = 0; values != null && values.length > 0 && p < values.length; p++) {
	              String value = values[p];
	              if (StringUtils.isNotEmpty(value)) {
	                fieldformvalue += "," + value;
	              }
	            }
	            fieldformvalue = fieldformvalue.startsWith(",") ? fieldformvalue.substring(1) : fieldformvalue;
	          }
	          wrtf.setFieldValue(fieldformvalue);
	        }
	      }
	    }
	  }
  }

  /**
   * 重写方法，增加是否新增明细的字段
   * @param fileUpload
   * @param userObj
   * @param docCategory
   * @param remoteAddr
   * @param assignmentFieldNames
   * @param wrtf
   * @param formId
   * @param isCanEdit
   * @param isadd
   */
  private static void getFieldFormInfo(FileUpload fileUpload, User userObj,
        int[] docCategory, String remoteAddr, List assignmentFieldNames,
        WorkflowRequestTableField wrtf,String formId,boolean isCanEdit,boolean isadd) {
    String fieldformname;
    String fieldformvalue;
    if (wrtf != null) {
        // 字段名称
        String fieldName = wrtf.getFieldName();
        // 字段HTML类型
        String fieldHtmlType = wrtf.getFieldHtmlType();
        fieldformname = wrtf.getFieldFormName();
        String cValFields = fieldformname;
        if(fieldformname.indexOf("_")>=0&&fieldformname.indexOf("span")==-1){
            cValFields = fieldformname.split("_")[0];
        }
        
        String meteth = fileUpload.getParameter("method2");
       if (fieldName != null && !"".equals(fieldName) && wrtf.isView() && ("create".equals(meteth) || (wrtf.isEdit() || isadd) || assignmentFieldNames.indexOf(cValFields) != -1
            ||WorkflowServiceUtil.isRowColumnRule(formId,fieldformname) || (wrtf.getFieldHtmlType().equals("9") && isCanEdit))) {
          // 如果是“附件上传”类型，则需作特殊处理。
          if ("6".equals(fieldHtmlType) && docCategory != null) {
            String fieldValue = Util.null2String(fileUpload.getParameter3("filesaveid_"+fieldformname));

//            // 新增附件上传 的 文档ID号
//            String newDocIDs = "";
//            String cntFieldName = "cnt" + fieldformname;
//            // 获取该附件上传字段 新增附件数。
//            int cntAppendfix = Util.getIntValue(fileUpload.getParameter3(cntFieldName), -1);
//            if (cntAppendfix > 0) {
//              for (int j = 0; j < cntAppendfix; j++) {
//                String strData = fileUpload.getParameter3(fieldformname + "_" + j);
//                // 新增附件可能被删除，需作判断。
//                if (strData != null) {
//                  String strFileName = fileUpload.getParameter3(fieldformname + "name_" + j);
//                  int docID = WorkflowSpeechAppend.uploadAppdix(strData, strFileName, userObj, docCategory, remoteAddr);
//                  if (docID > -1) {
//                    newDocIDs += docID + ",";
//                  }
//                }
//              }
//            }
//
//            fieldformvalue = newDocIDs + fieldValue;
//
//            // 去掉多余的逗号
//            String[] arrFieldValues = fieldformvalue.split(",");
//            fieldformvalue = "";
//            for (int p = 0; arrFieldValues != null && arrFieldValues.length > 0 && p < arrFieldValues.length; p++) {
//              String value = StringUtils.trim(arrFieldValues[p]);
//              if (StringUtils.isNotEmpty(value)) {
//                fieldformvalue += "," + value;
//              }
//            }
//            fieldformvalue = fieldformvalue.startsWith(",") ? fieldformvalue.substring(1) : fieldformvalue;
            
            String docIdContents = "";
	        String fielddata = Util.null2String(fileUpload.getParameter3("filedata_"+fieldformname));
	        String filenames = Util.null2String(fileUpload.getParameter3("filenames_"+fieldformname));
	        String[] fielddataArray = fielddata.split("_file_data_split_");
	        String[] filenamesArray = filenames.split(",");
	        for (int i = 0; i < fielddataArray.length; i++) {
	        	String temp = Util.null2String(fielddataArray[i]);
	        	String tempname = Util.null2String(filenamesArray[i]);
	        	if(temp.length()==0){
	        		continue;
	        	}
	        	//判断附件类型过滤掉
	        	//if(tempname.endsWith(suffix))
	        	if(temp.indexOf("base64,")>-1){
	        		temp = temp.substring(temp.indexOf("base64,")+7);
	        	}
	        	int docID = WorkflowSpeechAppend.uploadAppdix(temp, tempname, userObj, docCategory, remoteAddr);
	        	docIdContents+=","+docID;
			}
	        if(docIdContents.length()>0){
	        	docIdContents = docIdContents.substring(1);
	        }
	        fieldformvalue = docIdContents;
	        String tmpfieldvalue = "";
	        if(fieldValue.length()>0){
	        	tmpfieldvalue += ","+fieldValue;
	        }
	        if(fieldformvalue.length()>0){
	        	tmpfieldvalue += ","+fieldformvalue;
	        }
	        if(tmpfieldvalue.length()>0){
	        	tmpfieldvalue = tmpfieldvalue.substring(1);
	        }
            wrtf.setFieldValue(tmpfieldvalue);
          } else {
            fieldformvalue = fileUpload.getParameter3(fieldformname);
            if("2".equals(fieldHtmlType)){ 
                        if(fieldformvalue !=null && fieldformvalue.indexOf("ke-content-div-mobil")>0){ //富文本专用标记，如果是有该div则提交时去掉该div
                            fieldformvalue = fieldformvalue.substring(fieldformvalue.indexOf(">")+1, fieldformvalue.length());
                            fieldformvalue = fieldformvalue.substring(0,fieldformvalue.lastIndexOf("</div>"));
                        }
              }
            if (fieldformvalue != null) {
              if ("3".equals(wrtf.getFieldHtmlType())) {
                String[] values = StringUtils.split(fieldformvalue, ",");
                fieldformvalue = "";
                for (int p = 0; values != null && values.length > 0 && p < values.length; p++) {
                  String value = values[p];
                  if (StringUtils.isNotEmpty(value)) {
                    fieldformvalue += "," + value;
                  }
                }
                fieldformvalue = fieldformvalue.startsWith(",") ? fieldformvalue.substring(1) : fieldformvalue;
              }
              wrtf.setFieldValue(fieldformvalue);
            }
          }
        }
      }
  }

  final public static double VERSION_45 = 4.5;

  /**
   * 使用版本号的字符串来与指定版本作比较，当前系统版本比指定版本高或者相等都返回true
   * 
   * @param clientVersion
   *          当前系统的版本号 如4.5 4.5.0 4.51等
   * @param version
   *          指定比较的版本号
   * @return
   */
  public static boolean compareVersion(String clientVersion, double version) {
    // 如果传入版本字符串为null 或 空字符串 则直接返回false。
    if (clientVersion == null || "".equals(clientVersion)) {
      return false;
    }

    int index = clientVersion.indexOf(".");
    if (index > 0) {
      int len = clientVersion.length();
      index = (index + 2) > len ? len : index + 2;
      clientVersion = clientVersion.substring(0, index);
    }

    // 对版本号转换成浮点型，再进行比较。
    double currenVer = Util.getDoubleValue(clientVersion, -1);
    if (currenVer == -1) {
      return false;
    } else {
      return (currenVer >= version);
    }
  }
  
  public static String uploadSignatureAppends(FileUpload fileUpload, User userObj, int[] docCategory){
  	String fieldValue = Util.null2String(fileUpload.getParameter3("fieldSignAppendfix"));
  	  
  	if(docCategory == null){
  	  log.info("The 'docCategory' is null, return null directly.");
  	  return fieldValue;
  	}
  	
  	// 流程签字意见附件上传 的 原始值
  	String newDocIDs = "";
  	
  	String remoteAddr = fileUpload.getRemoteAddr();
  	String cntFieldName = "cntFieldSignAppends";
  	int cntAppendfix = Util.getIntValue(fileUpload.getParameter3(cntFieldName), -1);
  	if (cntAppendfix > 0) {
  	  for (int j = 0; j < cntAppendfix; j++) {
  	    String strData = fileUpload.getParameter3("fieldSignAppend_" + j);
  	    // 新增附件可能被删除，需作判断。
  	    if (strData != null) {
  	      String strFileName = fileUpload.getParameter3("fieldSignAppendName_" + j);
  	      int docID = WorkflowSpeechAppend.uploadAppdix(strData, strFileName, userObj, docCategory, remoteAddr);
  	      if (docID > -1) {
  	        newDocIDs += (docID + ",");
  	      }
  	    }
  	  }
  	}
  	
  	fieldValue = newDocIDs + fieldValue;
  	
  	// 去掉多余的逗号
  	String[] arrFieldValues = fieldValue.split(",");
  	fieldValue = "";
  	for (int p = 0; arrFieldValues != null && arrFieldValues.length > 0 && p < arrFieldValues.length; p++) {
  	  String value = StringUtils.trim(arrFieldValues[p]);
  	  if (StringUtils.isNotEmpty(value)) {
  		  fieldValue += "," + value;
  	  }
  	}
  	fieldValue = fieldValue.startsWith(",") ? fieldValue.substring(1) : fieldValue;
  	return fieldValue;
  }
}