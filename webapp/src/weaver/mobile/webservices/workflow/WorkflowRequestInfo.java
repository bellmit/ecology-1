package weaver.mobile.webservices.workflow;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import weaver.general.Util;

/**
 * 工作流请求信息
 */
public class WorkflowRequestInfo implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4236890406953604169L;

	/**
	 * 请求ID
	 */
	private String requestId;
	
	/**
	 * 请求标题
	 */
	private String requestName;
	
	/**
	 * 请求标题
	 */
	private String requestNameNew;
	
	public String getRequestNameNew() {
		return requestNameNew;
	}

	public void setRequestNameNew(String requestNameNew) {
		this.requestNameNew = requestNameNew;
	}

	/**
	 * 请求重要级别
	 */
	private String requestLevel;
	
	/**
	 * 短信提醒
	 */
	private String messageType;
	
	/**
	 * 微信提醒
	 */
	private String chatsType;
	
	/**
	 * 流程类型
	 */
//	private WorkflowBaseInfo workflowBaseInfo;
    private WorkflowExtInfo workflowBaseInfo;
	
	/**
	 * 当前节点名称
	 */
	private String currentNodeName;
	
	/**
	 * 当前节点Id
	 */
	private String currentNodeId;
	
	/**
	 * 显示自由节点Id
	 */
	private String freeNodeId;
	
	/**
	 * 显示自由节点Id
	 */
	private String freeNodeName;
	
	/**
	 * 当前节点类型
	 */
	private String currentNodeType;

	/**
	 * 自由流程节点操作者
	 */
	private String operators;
	
	/**
	 * 自由流程节点会签关系
	 */
	private String signtype;
	
	private int isremark;
	
	/**
	 * 
	 */
	private String nodeId;
	
	/**
	 * 流程状态
	 */
	private String status;
	
	/**
	 * 创建者
	 */
	private String creatorId;
	private String creatorName;
	
	/**
	 * 创建时间
	 */
	private String createTime;
	
	/**
	 * 最后操作者名称
	 */
	private String lastOperatorName;
	
	/**
	 * 最后操作时间
	 */
	private String lastOperateTime;
	
	/**
	 * 接收时间
	 */
	private String receiveTime;
	
	/**
	 * 是否可查看
	 */
	private boolean canView;
	
	/**
	 * 是否可编辑
	 */
	private boolean canEdit;
	
	/**
	 * 签字意见是否必填
	 */
	private boolean mustInputRemark;

	/**是否可编辑签字意见
	 */
	private boolean canEditRemark;
	
	/**
	 * 是否需要提交确认
	 */
	private boolean needAffirmance;
	
	/**
	 * 流程退回节点（工作流允许退回选择节点时）
	 */
	private int rejectToNodeid;
	/**
	 * 退回类型
	 */
	private int rejcetToType ;
	/**
	 * 流程提交节点
	 */
	private int submitToNodeid;
	/**
	 * 套红节点
	 * 0、非套红节点
	 * 1、套红节点未套红
	 * 2、套红节点已套红
	 */
	private String templetStatus;
	
	/**
	 * 签章节点
	 * 0、非签章节点
	 * 1、签章节点未签章
	 * 2、签章节点已签章
	 */
	private String signatureStatus;
	
	/**
	 * 
	 */
	private int languageid;
	
	/**
	 * 按钮名称
	 */
	private String submitButtonName;
	private String subnobackButtonName;
	private String subbackButtonName;
	private String submitDirectName; // 提交至退回节点
	private String rejectButtonName;
	private String forwardButtonName;
	private String takingOpsButtonName;
	private String HandleForwardButtonName;  //转办
	private String forhandbackButtonName;
	private String forhandnobackButtonName;
	private	String givingopinionsName ="";  //回复
	private	String givingOpinionsnobackName = ""; // 回复不反馈
	private	String givingOpinionsbackName = ""; // 回复需反馈
	
	private String formsignaturemd5 = "";   //表单签名信息 
	
	/**
	 * 主表信息
	 */
	private WorkflowMainTableInfo workflowMainTableInfo;
	
	/**
	 * 明细表信息
	 */
	private WorkflowDetailTableInfo[] workflowDetailTableInfos;
	
	/**
	 * 流转日志信息
	 */
	private WorkflowRequestLog[] workflowRequestLogs;
	
	/**
	 * HTML显示模板
	 * 0 iPad
	 * 1 iPhone
	 */
	private String[] WorkflowHtmlTemplete;
	
	/**
	 * 解析后的HTML显示内容
	 * 0 iPad
	 * 1 iPhone
	 */
	private String[] WorkflowHtmlShow;
	
	/**
	 * 流程短语
	 */
	private String[][] workflowPhrases;
	
	/**
	 * 手写签章
	 */
	private int handWrittenSign;
	
	/**
	 * 语音附件
	 */
	private int speechAttachment;
	
	/**
	 * 表单签章
	 */
	private String isFormSignature;
	
	/**
	 * 是否允许签字意见上传附件
	 */
	private String isAnnexUpload;
	
	/**
	 * 签字意见上传附件
	 */
	private String signatureAppendfix;

	private boolean canDetailEdit;
	
	/**
	 * 签字意见
	 */
	private String remark;
	
	private int version=0;
	
	/**
	 * 签字意见添加位置信息
	 */
	private String remarkLocation;
	
    /**
     * 提醒信息id
     */
    private String messageid;
    
    /**
     * 提醒信息内容
     */
    private String messagecontent;
    
	/**
	 * 处理类型
	 */
	private String module;
	
	//用户选择人员，异常处理信息
	private Map<String,Object> eh_operatorMap = new HashMap<String,Object>();

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getRequestName() {
		return requestName;
	}

	public void setRequestName(String requestName) {
		this.requestName = requestName;
	}

	public String getRequestLevel() {
		return requestLevel;
	}

	public void setRequestLevel(String requestLevel) {
		this.requestLevel = requestLevel;
	}

	public WorkflowExtInfo getWorkflowBaseInfo() {
		return workflowBaseInfo;
	}

	public void setWorkflowBaseInfo(WorkflowExtInfo workflowBaseInfo) {
		this.workflowBaseInfo = workflowBaseInfo;
	}

	public String getCurrentNodeName() {
		return currentNodeName;
	}

	public void setCurrentNodeName(String currentNodeName) {
		this.currentNodeName = currentNodeName;
	}

	public String getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(String creatorId) {
		this.creatorId = creatorId;
	}

	public String getCreatorName() {
		return creatorName;
	}

	public void setCreatorName(String creatorName) {
		this.creatorName = creatorName;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getLastOperatorName() {
		return lastOperatorName;
	}

	public void setLastOperatorName(String lastOperatorName) {
		this.lastOperatorName = lastOperatorName;
	}

	public String getLastOperateTime() {
		return lastOperateTime;
	}

	public void setLastOperateTime(String lastOperateTime) {
		this.lastOperateTime = lastOperateTime;
	}

	public WorkflowMainTableInfo getWorkflowMainTableInfo() {
		return workflowMainTableInfo;
	}

	public void setWorkflowMainTableInfo(WorkflowMainTableInfo workflowMainTableInfo) {
		this.workflowMainTableInfo = workflowMainTableInfo;
	}

	public WorkflowDetailTableInfo[] getWorkflowDetailTableInfos() {
		return workflowDetailTableInfos;
	}

	public void setWorkflowDetailTableInfos(WorkflowDetailTableInfo[] workflowDetailTableInfos) {
		this.workflowDetailTableInfos = workflowDetailTableInfos;
	}

	public WorkflowRequestLog[] getWorkflowRequestLogs() {
		return workflowRequestLogs;
	}

	public void setWorkflowRequestLogs(WorkflowRequestLog[] workflowRequestLogs) {
		this.workflowRequestLogs = workflowRequestLogs;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public boolean isCanView() {
		return canView;
	}

	public void setCanView(boolean canView) {
		this.canView = canView;
	}

	public boolean isCanEdit() {
		return canEdit;
	}

	public void setCanEdit(boolean canEdit) {
		this.canEdit = canEdit;
	}

	public String getSubmitButtonName() {
		return submitButtonName;
	}

	public void setSubmitButtonName(String submitButtonName) {
		this.submitButtonName = submitButtonName;
	}

	public String getRejectButtonName() {
		return rejectButtonName;
	}

	public void setRejectButtonName(String rejectButtonName) {
		this.rejectButtonName = rejectButtonName;
	}

	public String getForwardButtonName() {
		return forwardButtonName;
	}

	public void setForwardButtonName(String forwardButtonName) {
		this.forwardButtonName = forwardButtonName;
	}

	public String getCurrentNodeId() {
		return currentNodeId;
	}

	public void setCurrentNodeId(String currentNodeId) {
		this.currentNodeId = currentNodeId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public boolean isMustInputRemark() {
		return mustInputRemark;
	}

	public void setMustInputRemark(boolean mustInputRemark) {
		this.mustInputRemark = mustInputRemark;
	}

	public boolean isCanEditRemark() {
		return canEditRemark;
	}

	public void setCanEditRemark(boolean canEditRemark) {
		this.canEditRemark = canEditRemark;
	}

	public String[] getWorkflowHtmlTemplete() {
		return WorkflowHtmlTemplete;
	}

	public void setWorkflowHtmlTemplete(String[] workflowHtmlTemplete) {
		WorkflowHtmlTemplete = workflowHtmlTemplete;
	}

	public String[] getWorkflowHtmlShow() {
		return WorkflowHtmlShow;
	}

	public void setWorkflowHtmlShow(String[] workflowHtmlShow) {
		WorkflowHtmlShow = workflowHtmlShow;
	}

	public String[][] getWorkflowPhrases() {
		return workflowPhrases;
	}

	public void setWorkflowPhrases(String[][] workflowPhrases) {
		this.workflowPhrases = workflowPhrases;
	}
	
	public String getReceiveTime() {
		return receiveTime;
	}

	public void setReceiveTime(String receiveTime) {
		this.receiveTime = receiveTime;
	}

	public String getSubnobackButtonName() {
		return subnobackButtonName;
	}

	public void setSubnobackButtonName(String subnobackButtonName) {
		this.subnobackButtonName = subnobackButtonName;
	}

	public String getSubbackButtonName() {
		return subbackButtonName;
	}

	public void setSubbackButtonName(String subbackButtonName) {
		this.subbackButtonName = subbackButtonName;
	}

	public boolean isNeedAffirmance() {
		return needAffirmance;
	}

	public void setNeedAffirmance(boolean needAffirmance) {
		this.needAffirmance = needAffirmance;
	}
	
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getTempletStatus() {
		return templetStatus;
	}

	public void setTempletStatus(String templetStatus) {
		this.templetStatus = templetStatus;
	}

	public String getSignatureStatus() {
		return signatureStatus;
	}

	public void setSignatureStatus(String signatureStatus) {
		this.signatureStatus = signatureStatus;
	}

	public int getRejectToNodeid() {
		return rejectToNodeid;
	}

	public void setRejectToNodeid(int rejectToNodeid) {
		this.rejectToNodeid = rejectToNodeid;
	}

  public int getHandWrittenSign() {
    return handWrittenSign;
  }

  public void setHandWrittenSign(int handWrittenSign) {
    this.handWrittenSign = handWrittenSign;
  }

  public int getSpeechAttachment() {
    return speechAttachment;
  }

  public void setSpeechAttachment(int speechAttachment) {
    this.speechAttachment = speechAttachment;
  }

	public String getIsFormSignature() {
		return isFormSignature;
	}
	
	public void setIsFormSignature(String isFormSignature) {
		this.isFormSignature = isFormSignature;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public int getIsremark() {
		return isremark;
	}

	public void setIsremark(int isremark) {
		this.isremark = isremark;
	}

	public String getSignatureAppendfix() {
		return signatureAppendfix;
	}

	public void setSignatureAppendfix(String signatureAppendfix) {
		this.signatureAppendfix = signatureAppendfix;
	}

	public String getIsAnnexUpload() {
		return isAnnexUpload;
	}

	public void setIsAnnexUpload(String isAnnexUpload) {
		this.isAnnexUpload = isAnnexUpload;
	}

	public String getChatsType() {
		return chatsType;
	}

	public void setChatsType(String chatsType) {
		this.chatsType = chatsType;
	}
	
	public boolean isCanDetailEdit() {
		return canDetailEdit;
	}

	public void setCanDetailEdit(boolean canDetailEdit) {
		this.canDetailEdit = canDetailEdit;
	}
	
	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public Map<String, Object> getEh_operatorMap() {
		return eh_operatorMap;
	}

	public void setEh_operatorMap(Map<String, Object> eh_operatorMap) {
		this.eh_operatorMap = eh_operatorMap;
	}

	public String getTakingOpsButtonName() {
		return takingOpsButtonName;
	}

	public void setTakingOpsButtonName(String takingOpsButtonName) {
		this.takingOpsButtonName = takingOpsButtonName;
	}

	public String getHandleForwardButtonName() {
		return HandleForwardButtonName;
	}

	public void setHandleForwardButtonName(String handleForwardButtonName) {
		HandleForwardButtonName = handleForwardButtonName;
	}

	public String getForhandbackButtonName() {
		return forhandbackButtonName;
	}

	public void setForhandbackButtonName(String forhandbackButtonName) {
		this.forhandbackButtonName = forhandbackButtonName;
	}

	public String getForhandnobackButtonName() {
		return forhandnobackButtonName;
	}

	public void setForhandnobackButtonName(String forhandnobackButtonName) {
		this.forhandnobackButtonName = forhandnobackButtonName;
	}

	public String getGivingopinionsName() {
		return givingopinionsName;
	}

	public void setGivingopinionsName(String givingopinionsName) {
		this.givingopinionsName = givingopinionsName;
	}

	public String getGivingOpinionsnobackName() {
		return givingOpinionsnobackName;
	}

	public void setGivingOpinionsnobackName(String givingOpinionsnobackName) {
		this.givingOpinionsnobackName = givingOpinionsnobackName;
	}

	public String getGivingOpinionsbackName() {
		return givingOpinionsbackName;
	}

	public void setGivingOpinionsbackName(String givingOpinionsbackName) {
		this.givingOpinionsbackName = givingOpinionsbackName;
	}

    public String getFormsignaturemd5() {
        return formsignaturemd5;
    }

    public void setFormsignaturemd5(String formsignaturemd5) {
        this.formsignaturemd5 = formsignaturemd5;
    }

	public String getCurrentNodeType() {
		return currentNodeType;
	}

	public void setCurrentNodeType(String currentNodeType) {
		this.currentNodeType = currentNodeType;
	}

	public String getOperators() {
		return operators;
	}

	public void setOperators(String operators) {
		this.operators = operators;
	}

	public String getSigntype() {
		return signtype;
	}

	public void setSigntype(String signtype) {
		this.signtype = signtype;
	}

	public String getFreeNodeId() {
		return freeNodeId;
	}

	public void setFreeNodeId(String freeNodeId) {
		this.freeNodeId = freeNodeId;
	}

    public String getFreeNodeName() {
		return freeNodeName;
	}

	public void setFreeNodeName(String freeNodeName) {
		this.freeNodeName = freeNodeName;
	}

	public String getRemarkLocation() {
        return remarkLocation;
    }

    public void setRemarkLocation(String remarkLocation) {
        this.remarkLocation = remarkLocation;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }
    public String getMessageid() {
        return messageid;
    }

    public void setMessageid(String messageid) {
        this.messageid = messageid;
    }

    public String getMessagecontent() {
        return messagecontent;
    }

    public void setMessagecontent(String messagecontent) {
        this.messagecontent = messagecontent;
    }

    public int getLanguageid() {
        return languageid;
    }

    public void setLanguageid(int languageid) {
        this.languageid = languageid;
    }

	public String getSubmitDirectName() {
		return submitDirectName;
	}

	public void setSubmitDirectName(String submitDirectName) {
		this.submitDirectName = submitDirectName;
	}
	
	public int getSubmitToNodeid() {
		return submitToNodeid;
	}

	public void setSubmitToNodeid(int submitToNodeid) {
		this.submitToNodeid = submitToNodeid;
	}

	public int getRejcetToType() {
		return rejcetToType;
	}

	public void setRejcetToType(int rejcetToType) {
		this.rejcetToType = rejcetToType;
	}
	
}