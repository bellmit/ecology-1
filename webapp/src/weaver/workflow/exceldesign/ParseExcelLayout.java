package weaver.workflow.exceldesign;

import java.util.*;
import java.math.BigDecimal;

import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.systeminfo.SystemEnv;
import weaver.workflow.workflow.WFNodeDtlFieldManager;
import weaver.workflow.workflow.WorkflowVersion;
import java.util.regex.*;

public class ParseExcelLayout extends BaseBean{
	
	//高级明细解析头尾标记，需为小写(否则jsoup解析会自动转换为小写)，且为闭合标签
	public final static String BEGMARK = "<seniordetailmark>";
	public final static String ENDMARK = "</seniordetailmark>";
	
	private int wfid;
	private int nodeid;
	private int formid;
	private int isbill;
	private int modeid;
	private int type = -1;		//0显示模板、1打印模板、2Mobile模板
	private int requestid = -1;
	private int languageid;
	private int isFromApp = 0;
	
	private JSONObject etable = new JSONObject();
	private HashMap<String,String> formulaMap = new HashMap<String,String>();
	private StringBuilder tempHtml = new StringBuilder();
	private StringBuilder tempCss = new StringBuilder();
	private StringBuilder tempScript = new StringBuilder();
	
	private int tabAreaLength = 0;
	private RecordSet rs = new RecordSet();
	private ExcelLayoutManager excelLayoutManager = new ExcelLayoutManager();
	
	public ParseExcelLayout(HashMap<String,String> other_pars){
		this.wfid = Util.getIntValue(other_pars.get("wfid"),0);
		this.nodeid = Util.getIntValue(other_pars.get("nodeid"),0);
		this.formid = Util.getIntValue(other_pars.get("formid"),0);
		this.isbill = Util.getIntValue(other_pars.get("isbill"),-1);
		this.modeid = Util.getIntValue(other_pars.get("modeid"),-1);
		this.type = Util.getIntValue(other_pars.get("type"),0);
		this.requestid = Util.getIntValue(other_pars.get("requestid"));
		this.languageid = Util.getIntValue(other_pars.get("languageid"),7);
		this.isFromApp = Util.getIntValue(other_pars.get("isFromApp"),0);
	}
	
	public void analyzeLayout(String datajson,String scripts){
		try {
			JSONObject dataJson = new JSONObject(datajson);
			JSONObject eformdesign = dataJson.getJSONObject("eformdesign");
			this.etable = eformdesign.getJSONObject("etables");
			//JS文件引用(必须包在p标签里，否则经jsoup解析会丢失)
			tempHtml.append("<p id=\"edesign_script_css\" style=\"display:none !important\">\n");
			if(type==0 || type==1){
				tempHtml.append("<script type=\"text/javascript\" src=\"/workflow/exceldesign/js/format_wev8.js\"></script>\n");
				tempHtml.append("<script type=\"text/javascript\" src=\"/workflow/exceldesign/js/formula_wev8.js\"></script>\n");
			}
			tempHtml.append("<script type=\"text/javascript\" src=\"/workflow/exceldesign/js/tabpage_wev8.js\"></script>\n");
			tempHtml.append("<script type=\"text/javascript\" src=\"/workflow/exceldesign/js/wfExcelHtml_wev8.js\"></script>\n");
			tempHtml.append("</p>\n");
			//公式解析
			if(eformdesign.has("formula") && type==0){
				JSONObject formulaJson = eformdesign.getJSONObject("formula");
				if(!"".equals(formulaJson)){
					this.formulaMap = this.transFormulaJsonToMap(formulaJson);
					tempScript.append("try{\n");
					tempScript.append("\t var globalFormula = '"+formulaJson.toString()+"';\n");
					tempScript.append("\t globalFormula = JSON.parse(globalFormula);\n");
					tempScript.append("}catch(e){}\n");
				}
			}
			//模板解析，从主表--到明细表
			this.analyzeMainTable(scripts);
			//处理模板-联动隐藏设置
			if(this.requestid > 0){
				try{
					ParseLinkHideAttr parseLinkHideAttr = new ParseLinkHideAttr(wfid, nodeid, formid, isbill, requestid);
					tempHtml = parseLinkHideAttr.adjustTemplateHide(this.tempHtml);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			//隐藏域标示为表单设计器模板
			tempHtml.append("<input type=\"hidden\" id=\"edesign_layout\" />\n");
			//ready执行相关JS
			tempScript.append("jQuery(document).ready(function(){").append("\n");
			tempScript.append("\t readyOperate.execute("+type+"); \n");
			tempScript.append("});\n");
		} catch (Exception e) {
			writeLog(e);
		}
	}
	
	private void analyzeMainTable(String scripts) throws Exception{
		JSONObject mainTable = etable.getJSONObject("emaintable");
		JSONObject rowheads = mainTable.getJSONObject("rowheads");	//行头
		JSONObject colheads = mainTable.getJSONObject("colheads");	//列头
		JSONObject rowattrs = new JSONObject();
		if(mainTable.has("rowattrs"))
			rowattrs = mainTable.getJSONObject("rowattrs");
		boolean hasPercent = colheads.toString().indexOf("%")>-1;
		int rownum=rowheads.length();		//行数
		int colnum=colheads.length();		//列数
		if(type==0 || type==2){
			tempCss.append(".excelOuterTable a{color:#123885 !important;}");
			tempCss.append(".excelOuterTable a:hover{color:red !important;}");
		}
		tempHtml.append("<div class=\"excelTempDiv\">").append("\n");
		tempHtml.append("<table class=\"excelOuterTable tablefixed\">").append("\n");
		tempHtml.append("<tbody>").append("\n")
			.append("<tr>").append("\n")
			.append("<td align=\"center\">").append("\n");
		tempHtml.append("<table class=\"excelMainTable tablefixed\" style=\" ");
		if(type==0 || type==1){
			if(hasPercent){
				tempHtml.append("width:95%; ");
			}else{
				int sumWidth=0;
				for(int i=0;i<colheads.length();i++){
					sumWidth += Util.getIntValue(colheads.get("col_"+i).toString(),0);
				}
				tempHtml.append("width:").append(sumWidth).append("px; ");
			}
		}else if(type==2){
			tempHtml.append("width:98%; ");
		}
		tempHtml.append("\" ");
		tempHtml.append("_hasPercent=\"").append(hasPercent).append("\" ");
		tempHtml.append(">").append("\n");
		boolean needTransPercent = false;
		if((type==1 && hasPercent) || type==2)
			needTransPercent = true;
		tempHtml.append("<tbody>").append("\n");
		//解析table的列宽，使用异常TR的TD控制
		tempHtml.append(this.parseMainTableWidth(colheads, needTransPercent));
		//解析背景图、悬浮图
		tempHtml.append(parseTableImage("main", mainTable, colnum));
		//单元格解析
		JSONArray cells = mainTable.getJSONArray("ec");
		String[][] cellArr = buildMainCellArray("main", cells, rowheads, colheads, rowattrs);
		//循环二维数组生成tBody
		String cellValue="";
		for(int row=0;row<rownum;row++){
			String rowattr_text="",rowattr_class="",rowattr_style="";
			if(rowattrs.has("row_"+row)){
				Map<String,String> rowattr_map = parseAttrs(rowattrs.getJSONObject("row_"+row));
				rowattr_text = rowattr_map.get("_text");
				rowattr_class = rowattr_map.get("_class");
				rowattr_style = rowattr_map.get("_style");
			}
			tempHtml.append("<tr style=\"height:").append(rowheads.get("row_"+row)).append("px;").append(rowattr_style).append("\" ");
			tempHtml.append("".equals(rowattr_class)?"":"class=\""+rowattr_class+"\" ").append(rowattr_text).append(">\n");
			for(int col=0;col<colnum;col++){
				cellValue=Util.null2String(cellArr[row][col]);
				if("combine_cell".equals(cellValue)){
					continue;
				}else if("".equals(cellValue)){
					tempHtml.append("<td></td>");
				}else{
					tempHtml.append(cellValue);
				}
			}
			tempHtml.append("</tr>").append("\n");
		}
		tempHtml.append("</tbody>\n").append("</table>\n");
		tempHtml.append("</td>\n").append("</tr>\n").append("</tbody>\n").append("</table>\n");
		//添加代码块
		if(!"".equals(scripts)){
			tempHtml.append(this.decodeStr(scripts)).append("\n");
		}
		tempHtml.append("</div>\n");
	}
	
	/**
	 * 解析标签页区域
	 */
	private String analyzeTabArea(JSONObject tab) throws Exception{
		int styleid = -1;
		int defshow = 0;
		String areaheight = "";
		int tabsize = 0;
		Iterator<?> it = tab.keys();
		while(it.hasNext()){
			String key = it.next().toString();
			String value = Util.null2String(tab.get(key));
			if("style".equals(key))
				styleid = Util.getIntValue(value, -1);
			else if("defshow".equals(key))
				defshow = Util.getIntValue(value, 0);
			else if("areaheight".equals(key))
				areaheight = value;
			else if(key.startsWith("order_"))
				tabsize++;
		}
		//找不到的自定义样式使用系统样式
		if(styleid>0){
			rs.executeSql("select * from workflow_tabstyle where styleid="+styleid);
			if(!rs.next())
				styleid = -1;
		}
		StringBuilder tabTopHtml = new StringBuilder();
		StringBuilder tabBottomHtml = new StringBuilder();
		if(type != 2)
			tabTopHtml.append("<div class=\"tab_movebtn tab_turnleft\"></div>").append("\n");
		tabTopHtml.append("<div class=\"tab_head\"");
		if(type == 2)
			tabTopHtml.append(" style=\"overflow-x:auto\"");
		tabTopHtml.append(">").append("\n");
		tabTopHtml.append("<div class=\"t_area xrepeat\"> ");
		for(int i=1; i<=tabsize; i++){
			String tabinfo = Util.null2String(tab.get("order_"+i));
			String tabid = tabinfo.substring(0, tabinfo.indexOf(","));
			String tabname = tabinfo.substring(tabinfo.indexOf(",")+1);
			//头部区域
			tabTopHtml.append("<div id=\""+tabid+"\" ");
			String selStr = (i-1 == defshow) ? "t_sel" : "t_unsel";
			tabTopHtml.append("class=\""+selStr+"\">");
			tabTopHtml.append("<div class=\""+selStr+"_left norepeat\"></div>");
			tabTopHtml.append("<div class=\""+selStr+"_middle xrepeat lineheight30\">");
			tabTopHtml.append("<span>").append(tabname).append("</span>");
			tabTopHtml.append("</div>");
			tabTopHtml.append("<div class=\""+selStr+"_right norepeat\"></div>");
			tabTopHtml.append("</div>");
			if(i != tabsize)
				tabTopHtml.append("<div class=\"t_sep norepeat\"></div>");
			//内容区域
			if(etable.has(tabid)){
				tabBottomHtml.append("<div class=\"tab_content\" id=\""+tabid+"_content\" ");
				if(i-1 != defshow)
					tabBottomHtml.append(" style=\"display:none\" ");
				tabBottomHtml.append(">");
				tabBottomHtml.append(this.analyzeTab(tabid));
				tabBottomHtml.append("</div>");
			}
		}
		tabTopHtml.append("</div>");
		tabTopHtml.append("</div>");
		if(type != 2)
			tabTopHtml.append("<div class=\"tab_movebtn tab_turnright\"></div>").append("\n");
		
		String tabAreaClass = "tabarea_"+tabAreaLength;
		tabAreaLength++;
		StringBuilder tabAreaHtml = new StringBuilder();
		tabAreaHtml.append("<div class=\"").append(tabAreaClass).append("\">");
		tabAreaHtml.append("<div class=\"tab_top\">").append(tabTopHtml).append("</div>");
		tabAreaHtml.append("<div class=\"tab_bottom\" style=\"");
		if(areaheight.startsWith("2,")){	//固定高度，出滚动条
			String areaheightpx = areaheight.substring(areaheight.indexOf(",")+1);
			tabAreaHtml.append("height:").append(areaheightpx).append("px; overflow-y:auto;");
		}
		tabAreaHtml.append("\" >").append(tabBottomHtml).append("</div>");
		tabAreaHtml.append("</div>");
		
		//绑定Tab页切换的JS
		tempScript.append("jQuery(document).ready(function(){").append("\n");
		if(styleid < 0){	//系统样式
			tempScript.append("\t tabPage.front_initEvent_sysFace('"+tabAreaClass+"', '"+styleid+"'); ").append("\n");
		}else{
			JSONObject stylejson = getCustomStyle(styleid);
			tempScript.append("\t tabPage.front_initEvent_cusFace('"+tabAreaClass+"', '"+stylejson.toString()+"'); ").append("\n");
		}
		tempScript.append("});\n");
		
		return tabAreaHtml.toString();
	}
	
	/**
	 * 解析单个标签页
	 */
	private String analyzeTab(String tabid) throws Exception{
		JSONObject tabTable = etable.getJSONObject(tabid);
		JSONObject rowheads = tabTable.getJSONObject("rowheads");	//行头
		JSONObject colheads = tabTable.getJSONObject("colheads");	//列头
		JSONObject rowattrs = new JSONObject();
		if(tabTable.has("rowattrs"))
			rowattrs = tabTable.getJSONObject("rowattrs");
		int rownum=rowheads.length();		//行数
		int colnum=colheads.length();		//列数
		
		StringBuilder tabHtml = new StringBuilder();
		tabHtml.append("<table class=\"excelTabTable tablefixed\">").append("\n");
		tabHtml.append("<tbody>").append("\n");
		//解析table的列宽，使用异常TR的TD控制
		tabHtml.append(this.parseMainTableWidth(colheads, true));
		//解析背景图、悬浮图
		tabHtml.append(parseTableImage(tabid, tabTable, colnum));
		//单元格解析
		JSONArray cells = tabTable.getJSONArray("ec");
		String[][] cellArr = buildMainCellArray(tabid, cells, rowheads, colheads, rowattrs);
		//循环二维数组生成tBody
		String cellValue="";
		for(int row=0;row<rownum;row++){
			String rowattr_text="",rowattr_class="",rowattr_style="";
			if(rowattrs.has("row_"+row)){
				Map<String,String> rowattr_map = parseAttrs(rowattrs.getJSONObject("row_"+row));
				rowattr_text = rowattr_map.get("_text");
				rowattr_class = rowattr_map.get("_class");
				rowattr_style = rowattr_map.get("_style");
			}
			tabHtml.append("<tr style=\"height:").append(rowheads.get("row_"+row)).append("px;").append(rowattr_style).append("\" ");
			tabHtml.append("".equals(rowattr_class)?"":"class=\""+rowattr_class+"\" ").append(rowattr_text).append(">\n");
			for(int col=0;col<colnum;col++){
				cellValue=Util.null2String(cellArr[row][col]);
				if("combine_cell".equals(cellValue)){
					continue;
				}else if("".equals(cellValue)){
					tabHtml.append("<td></td>");
				}else{
					tabHtml.append(cellValue);
				}
			}
			tabHtml.append("</tr>").append("\n");
		}
		tabHtml.append("</tbody>\n").append("</table>\n");
		return tabHtml.toString();
	}
	
	/**
	 * 主表解析单元格生成二维数组（主表、标签页共有，易维护）
	 */
	private String[][] buildMainCellArray(String symbol, JSONArray cells, 
			JSONObject rowheads, JSONObject colheads, JSONObject rowattrs)throws Exception{
		int rownum=rowheads.length();		//行数
		int colnum=colheads.length();		//列数
		//方案：先初始化空二位数组，根据JSON中单元格属性生成TD的html放入数组中，同时根据colspan/rowspan的值修改相应的数组值
		String[][] cellArr = new String[rownum][colnum];
		StringBuilder cellContent = new StringBuilder();
		StringBuilder cellStyle = new StringBuilder();
		String cellid,efield,efinancial;
		int etype,rowid,colid,rowspan,colspan,borderstyle;
		for(int i=0;i<cells.length();i++){
			cellContent.setLength(0);
			cellStyle.setLength(0);
			JSONObject cell = cells.getJSONObject(i);
			cellid = cell.getString("id");
			efield = "";
			efinancial = "";
			if(cell.has("field"))		efield = cell.getString("field");
			if(cell.has("financial"))	efinancial = cell.getString("financial");
			etype = Util.getIntValue(cell.getString("etype"),0);
			rowid = Util.getIntValue(cellid.substring(0, cellid.indexOf(",")));
			colid = Util.getIntValue(cellid.substring(cellid.indexOf(",")+1));
			
			cellContent.append("<td ");
			//如果cell为合并内部的单元格,则不处理
			if("combine_cell".equals(cellArr[rowid][colid]))
				continue;
			//行合并、列合并处理
			rowspan = 1;
			colspan = 1;
			if(cell.has("rowspan"))
				rowspan = Util.getIntValue(cell.getString("rowspan"),1);
			if(cell.has("colspan"))
				colspan = Util.getIntValue(cell.getString("colspan"),1);
			if(rowspan>1 || colspan>1){
				if(rowspan>1)
					cellContent.append(" rowspan=\"").append(rowspan).append("\"");
				if(colspan>1)
					cellContent.append(" colspan=\"").append(colspan).append("\"");
				if(rowspan>rownum)	rowspan = rownum;
				if(colspan>colnum)	colspan = colnum;
				for(int num1=0; num1<rowspan; num1++){
					for(int num2=0; num2<colspan; num2++){
						if(num1==0 && num2==0)	continue;				//第一个单元格为数据格
						cellArr[rowid+num1][colid+num2] = "combine_cell";	//标示为合并单元格
					}
				}
			}
			//解析TD的style
			String className = symbol+"Td_"+rowid+"_"+colid;
			cellStyle.append(this.parseCellStyle(cell, className));
			if(cell.has("eborder")){
				cellStyle.append("\n").append("\t");
				JSONArray borders = cell.getJSONArray("eborder");
				for(int j=0; j<borders.length(); j++){
					JSONObject border = borders.getJSONObject(j);
					borderstyle = Util.getIntValue(border.getString("style"));
					if(etype==7 && ("top".equals(border.getString("kind"))||"bottom".equals(border.getString("kind"))))
						continue;		//明细表上下边框忽略
					cellStyle.append(this.parseBorder(borderstyle, border.getString("kind"), border.getString("color")));
				}
			}
			cellContent.append(" class=\"td_edesign td_etype_").append(etype).append(" ").append(className).append("\"");
			//TD绑定相关属性
			if(etype == 2){
				cellContent.append(" _fieldlabel=\"").append(efield).append("\"");
			}else if(etype == 3){
                cellContent.append(" id=\"").append("field"+efield+"_tdwrap").append("\"");
                cellContent.append(" _fieldid=\"").append(efield).append("\"");
                String cellattr = symbol.toUpperCase()+"."+this.getCellAttr(rowid, colid);
                cell.put("cellattr", cellattr);
				cellContent.append(" _cellattr=\"").append(cellattr).append("\"");
				if(type==0 && formulaMap.containsKey(cellattr)){
					cellContent.append(" _formula=\"").append(formulaMap.get(cellattr)).append("\"");
				}
			}
			if(efinancial.startsWith("1")){		//财务表头
				cellContent.append(this.parseFinancial(efinancial));
			}
			cellContent.append(">").append("\n");
			//单元格自定义属性
			String cellattr_text="",cellattr_class="",cellattr_style="";
			if(cell.has("attrs")){
				Map<String,String> cellattr_map = parseAttrs(cell.getJSONObject("attrs"));
				cellattr_text = cellattr_map.get("_text");
				cellattr_class = cellattr_map.get("_class");
				cellattr_style = cellattr_map.get("_style");
			}
			cellContent.append("<div ").append(cellattr_text)
				.append("".equals(cellattr_class)?"":"class=\""+cellattr_class+"\" ")
				.append("".equals(cellattr_style)?"":"style=\""+cellattr_style+"\" ")
				.append(">").append("\n");
			
			/**********TD标签结束，解析TD内容**********/
			Map<String,String> cellmap = this.parseCellText(symbol, cell);
			cellContent.append(cellmap.get("cellText"));
			cellStyle.append(cellmap.get("cellStyle"));
			
			cellContent.append("</div>").append("\n");
			cellContent.append("</td>").append("\n");
			
			cellArr[rowid][colid] = cellContent.toString();
			//生成样式并放入allStyle对象
			cellStyle.append("height:").append(rowheads.get("row_"+rowid)).append("px; ");
			//解决IE下textarea字数多只读查看，所在单元格为合并单元格时，TD宽度不受控制的Bug
			if(type==0||type==1){
				float tdwidth = this.countTdWidth(colheads, colid, colspan);
				if(tdwidth!=-1)
					cellStyle.append("width:").append(tdwidth).append("px; ");
			}
			tempCss.append(".").append(className).append("{").append(cellStyle).append("\n").append("}").append("\n");
		}
		return cellArr;
	}
	
	/**
	 * 获取Tab页头部个性化样式
	 */
	private JSONObject getCustomStyle(int styleid) throws JSONException{
		JSONObject stylejson = new JSONObject();
		rs.executeSql("select * from workflow_tabstyle where styleid="+styleid);
		if(rs.next()){
			stylejson.put("image_bg", Util.null2String(rs.getString("image_bg")));
			stylejson.put("image_sep", Util.null2String(rs.getString("image_sep")));
			stylejson.put("image_sepwidth", Util.null2String(rs.getString("image_sepwidth")));
			
			stylejson.put("sel_bgleft", Util.null2String(rs.getString("sel_bgleft")));
			stylejson.put("sel_bgleftwidth", Util.null2String(rs.getString("sel_bgleftwidth")));
			stylejson.put("sel_bgmiddle", Util.null2String(rs.getString("sel_bgmiddle")));
			stylejson.put("sel_bgright", Util.null2String(rs.getString("sel_bgright")));
			stylejson.put("sel_bgrightwidth", Util.null2String(rs.getString("sel_bgrightwidth")));
			stylejson.put("sel_color", Util.null2String(rs.getString("sel_color")));
			stylejson.put("sel_fontsize", Util.null2String(rs.getString("sel_fontsize")));
			stylejson.put("sel_family", Util.null2String(rs.getString("sel_family")));
			stylejson.put("sel_bold", Util.null2String(rs.getString("sel_bold")));
			stylejson.put("sel_italic", Util.null2String(rs.getString("sel_italic")));

			stylejson.put("unsel_bgleft", Util.null2String(rs.getString("unsel_bgleft")));
			stylejson.put("unsel_bgleftwidth", Util.null2String(rs.getString("unsel_bgleftwidth")));
			stylejson.put("unsel_bgmiddle", Util.null2String(rs.getString("unsel_bgmiddle")));
			stylejson.put("unsel_bgright", Util.null2String(rs.getString("unsel_bgright")));
			stylejson.put("unsel_bgrightwidth", Util.null2String(rs.getString("unsel_bgrightwidth")));
			stylejson.put("unsel_color", Util.null2String(rs.getString("unsel_color")));
			stylejson.put("unsel_fontsize", Util.null2String(rs.getString("unsel_fontsize")));
			stylejson.put("unsel_family", Util.null2String(rs.getString("unsel_family")));
			stylejson.put("unsel_bold", Util.null2String(rs.getString("unsel_bold")));
			stylejson.put("unsel_italic", Util.null2String(rs.getString("unsel_italic")));
		}
		return stylejson;
	}
	
	/**
	 * 根据明细JSON解析成明细Html
	 */
	private String analyzeDetail(String detailid){
		String detailhtml = "";
		int dindex=Util.getIntValue(detailid.replace("detail_",""))-1;
		try{
			JSONObject detailObj = etable.getJSONObject(detailid);
			if(detailObj.has("edtitleinrow") && detailObj.has("edtailinrow")){
				if(detailObj.has("seniorset") && "1".equals(detailObj.getString("seniorset")))
					detailhtml = analyzeDetail_Senior(detailid, detailObj);
				else
					detailhtml = analyzeDetail_Simple(detailid, detailObj);
			}else
				detailhtml = "<span class=\"warnInfoSpan\">"+SystemEnv.getHtmlLabelName(19325,languageid)+""+(dindex+1)+""+SystemEnv.getHtmlLabelName(84102,languageid)+"</span>";
		}catch(Exception e) {
			detailhtml = "<span class=\"warnInfoSpan\">"+SystemEnv.getHtmlLabelName(32395,languageid)+""+SystemEnv.getHtmlLabelName(19325,languageid)+""+(dindex+1)+""+SystemEnv.getHtmlLabelName(32140,languageid)+"</span>";
			writeLog(e);
		}
		return detailhtml;
	}
	
	/**
	 * 高级定制明细模板解析（新模板全为高级定制模板、重点维护）
	 */
	private String analyzeDetail_Senior(String detailid, JSONObject detailObj) throws Exception{
		int dindex=Util.getIntValue(detailid.replace("detail_",""))-1;
		int edtitleinrow = Util.getIntValue(detailObj.getString("edtitleinrow"));	//表头所在行
		int edtailinrow = Util.getIntValue(detailObj.getString("edtailinrow"));		//表尾所在行
		JSONObject rowheads = detailObj.getJSONObject("rowheads");	//行头
		JSONObject colheads = detailObj.getJSONObject("colheads");	//列头
		JSONObject rowattrs = new JSONObject();
		JSONObject colattrs = new JSONObject();
		if(detailObj.has("rowattrs"))
			rowattrs = detailObj.getJSONObject("rowattrs");
		if(detailObj.has("colattrs"))
			colattrs = detailObj.getJSONObject("colattrs");
		int rownum = rowheads.length();		//行数
		int colnum = colheads.length();		//列数
		//明细配置项
		WFNodeDtlFieldManager wFNodeDtlFieldManager = new WFNodeDtlFieldManager();
		wFNodeDtlFieldManager.resetParameter();
		wFNodeDtlFieldManager.setNodeid(nodeid);
		wFNodeDtlFieldManager.setGroupid(dindex);
		wFNodeDtlFieldManager.selectWfNodeDtlField();
		boolean allowscroll = false;	//勾选允许滚动条、每列都为px宽、显示模板，则明细table以绝对宽度解析
		if(type==0 && "1".equals(wFNodeDtlFieldManager.getAllowscroll()) && colheads.toString().indexOf("%") == -1)
			allowscroll = true;
		
		String detailDiv = "detailDiv_"+dindex;
		String detailTable = "oTable"+dindex;
		
		StringBuilder detailHtml = new StringBuilder();
		detailHtml.append("<div id=\"").append(detailDiv).append("\" class=\"");
		if(allowscroll){
		    tempCss.append(".tablefixed{table-layout:fixed;}").append("\n");
			detailHtml.append("excelDetailOuterDiv");		//excelDetailOuterDiv样式写在WFLayoutToHtml.java
		}
		detailHtml.append("\">").append("\n");
		if(type == 0 || type ==1)
			detailHtml.append(BEGMARK).append("\n");		//标记为高级定制明细，此类标记经过联动隐藏jsoup解析不会丢失,用于PC端解析截取
		detailHtml.append("<table class=\"excelDetailTable\" _seniorset=\"y\" ")
			.append("id=\"").append(detailTable).append("\" ")
			.append("name=\"").append(detailTable).append("\" ")
			.append("style=\"");
		if(!allowscroll)
			detailHtml.append("width:100%;");
		detailHtml.append("\">").append("\n");
		detailHtml.append("<tbody>").append("\n");
		
		int fullPercent = -1;
		if(allowscroll){	//允许滚动条，绝对宽度解析，显示模板肯定有序号列
			double totalWidth = 0;
			for(int i=0; i<colnum; i++){
				totalWidth += Double.parseDouble(colheads.getString("col_"+i));
			}
			//ready控制宽度的JS
			tempScript.append("jQuery(document).ready(function(){").append("\n");
			tempScript.append("\t jQuery('table#"+detailTable+"').width('"+totalWidth+"px');").append("\n");
			tempScript.append("});\n");
		}else{				//计算成百分比解析
			fullPercent = 100;
		}
		//解析table的列宽，使用异常TR的TD控制
		detailHtml.append(this.parseDetailTableWidth(colheads, colattrs, "", fullPercent));
		//解析背景图、悬浮图
		detailHtml.append(parseTableImage(detailid, detailObj, colnum));
		//单元格解析
		String[][] cellArr = buildDetailCellArray(detailid, detailObj, rowheads, colheads, rowattrs, colattrs, true);
		//循环二维数组生成tBody内容
		String cellValue="";
		for(int row=0;row<rownum;row++){
			if(row == edtitleinrow || row == edtailinrow)
				continue;
			String rowattr_text="",rowattr_class="",rowattr_style="";
			if(rowattrs.has("row_"+row)){
				Map<String,String> rowattr_map = parseAttrs(rowattrs.getJSONObject("row_"+row));
				rowattr_text = rowattr_map.get("_text");
				rowattr_class = rowattr_map.get("_class");
				rowattr_style = rowattr_map.get("_style");
			}
			detailHtml.append("<tr ");
			if(row < edtitleinrow){			//表头前行给class标示，便于addRow时计算
				detailHtml.append("_target=\"headrow\" ");
				detailHtml.append("class=\"exceldetailtitle ").append(" ").append(rowattr_class).append("\" ");
				detailHtml.append("style=\"height:").append(rowheads.get("row_"+row)).append("px;").append(rowattr_style).append("\" ");
				detailHtml.append(rowattr_text);
			}else if(row > edtitleinrow && row < edtailinrow){		//明细数据行给标示
				detailHtml.append("_target=\"datarow\" ");
			}else if(row > edtailinrow){		//明细合计行给标示
				detailHtml.append("_target=\"tailrow\" ");
			}
			detailHtml.append(">").append("\n");
			for(int col=0;col<colnum;col++){
				cellValue=Util.null2String(cellArr[row][col]);
				if("combine_cell".equals(cellValue)){
					continue;
				}else if("".equals(cellValue)){
					String classname_col = getClassByColAttrs(colattrs, col);
					detailHtml.append("<td class=\""+classname_col+"\"></td>");
				}else{
					detailHtml.append(cellValue);
				}
			}
			detailHtml.append("</tr>").append("\n");
		}
		detailHtml.append("</tbody>").append("\n").append("</table>").append("\n");
		if(type == 0 || type ==1)
			detailHtml.append(ENDMARK).append("\n");
		detailHtml.append("</div>\n");
		return detailHtml.toString();
	}
	
	
	/**
	 * 简单明细模板解析（老版本、单行模板）
	 */
	private String analyzeDetail_Simple(String detailid, JSONObject detailObj) throws Exception{
		int dindex=Util.getIntValue(detailid.replace("detail_",""))-1;
		StringBuilder detailHtml = new StringBuilder();
		int edtitleinrow = Util.getIntValue(detailObj.getString("edtitleinrow"));		//表头所在行
		int edtailinrow = Util.getIntValue(detailObj.getString("edtailinrow"));		//表尾所在行
		JSONObject rowheads = detailObj.getJSONObject("rowheads");	//行头
		JSONObject colheads = detailObj.getJSONObject("colheads");	//列头
		JSONObject rowattrs = new JSONObject();
		JSONObject colattrs = new JSONObject();
		if(detailObj.has("rowattrs"))
			rowattrs = detailObj.getJSONObject("rowattrs");
		if(detailObj.has("colattrs"))
			colattrs = detailObj.getJSONObject("colattrs");
		int rownum = rowheads.length();		//行数
		int colnum = colheads.length();		//列数
		//明细配置项
		WFNodeDtlFieldManager wFNodeDtlFieldManager = new WFNodeDtlFieldManager();
		wFNodeDtlFieldManager.resetParameter();
		wFNodeDtlFieldManager.setNodeid(nodeid);
		wFNodeDtlFieldManager.setGroupid(dindex);
		wFNodeDtlFieldManager.selectWfNodeDtlField();
		boolean serialColumn = true;	//打印生成序号列
		if(type==1 && !"1".equals(wFNodeDtlFieldManager.getIsprintserial()))
			serialColumn = false;
		boolean allowscroll = false;	//勾选允许滚动条、每列都为px宽、显示模板，则明细table以绝对宽度解析
		if(type==0 && "1".equals(wFNodeDtlFieldManager.getAllowscroll()) && colheads.toString().indexOf("%") == -1)
			allowscroll = true;
		
		String detailDiv = "detailDiv_"+dindex;
		String detailTable = "oTable"+dindex;
		detailHtml.append("<div id=\"").append(detailDiv).append("\" class=\"");
		if(allowscroll){
		    tempCss.append(".tablefixed{table-layout:fixed;}").append("\n");
			detailHtml.append("excelDetailOuterDiv");		//excelDetailOuterDiv样式写在WFLayoutToHtml.java
		}
		detailHtml.append("\">").append("\n");
		detailHtml.append("<table class=\"excelDetailTable\" ")
			.append("id=\"").append(detailTable).append("\" ")
			.append("name=\"").append(detailTable).append("\" ")
			.append("style=\"");
		if(!allowscroll)
			detailHtml.append("width:100%;");
		detailHtml.append("\">").append("\n");
		detailHtml.append("<tbody>").append("\n");
		
		String serColWidth = "";
		int fullPercent = -1;
		if(allowscroll){	//允许滚动条，绝对宽度解析，显示模板肯定有序号列
			serColWidth = "60px";
			double totalWidth = 60;
			for(int i=0; i<colnum; i++){
				totalWidth += Double.parseDouble(colheads.getString("col_"+i));
			}
			//ready控制宽度的JS
			tempScript.append("jQuery(document).ready(function(){").append("\n");
			tempScript.append("\t jQuery('table#"+detailTable+"').width('"+totalWidth+"px');").append("\n");
			tempScript.append("});\n");
		}else{				//计算成百分比解析
			fullPercent = 100;
			if(serialColumn){
				serColWidth = "6%";		//序号列宽度定为5%
				fullPercent = 94;
				for(int i=0; i<colheads.length(); i++){		//处理宽度,加一序号列6%，宽度统一*0.94
					String colhead = colheads.getString("col_"+i);
					if(colhead.indexOf("%")==-1){
						colheads.put("col_"+i, Double.parseDouble(colhead)*0.94+"");
					}else{
						colheads.put("col_"+i, (Double.parseDouble(colhead.replace("%", ""))*0.94)+"%");
					}
				}
			}
		}
		//解析table的列宽，使用异常TR的TD控制
		detailHtml.append(this.parseDetailTableWidth(colheads, colattrs, serColWidth, fullPercent));
		//解析背景图、悬浮图
		detailHtml.append(parseTableImage(detailid, detailObj, colnum));
		//单元格解析
		String[][] cellArr = buildDetailCellArray(detailid, detailObj, rowheads, colheads, rowattrs, colattrs, false);
		//循环二维数组生成tBody内容
		String cellValue="";
		for(int row=0;row<rownum;row++){
			if(row == edtitleinrow || row >= edtailinrow)		//表头表尾不用解析
				continue;
			String rowattr_text="",rowattr_class="",rowattr_style="";
			if(rowattrs.has("row_"+row)){
				Map<String,String> rowattr_map = parseAttrs(rowattrs.getJSONObject("row_"+row));
				rowattr_text = rowattr_map.get("_text");
				rowattr_class = rowattr_map.get("_class");
				rowattr_style = rowattr_map.get("_style");
			}
			detailHtml.append("<tr ");
			if(row < edtitleinrow){			//表头前行给class标示，便于addRow时计算
				detailHtml.append("_target=\"headrow\" ");
				detailHtml.append("class=\"exceldetailtitle ").append(" ").append(rowattr_class).append("\" ");
				detailHtml.append("style=\"height:").append(rowheads.get("row_"+row)).append("px;").append(rowattr_style).append("\" ");
				detailHtml.append(rowattr_text);
			}else if(row > edtitleinrow && row < edtailinrow){
				detailHtml.append("_target=\"datarow\" ");
			}
			detailHtml.append(">").append("\n");
			if(serialColumn){
				//每行加一列存序号，样式取第一个单元格
				detailHtml.append("<td class=\"detail"+dindex+"_"+row+"_0\" style=\"background-image:none !important;\">");
				if(row==edtitleinrow-1){
					detailHtml.append("<input type=\"checkbox\" notbeauty=\"true\" name=\"check_all_record\" onclick=\"detailOperate.checkAllFun("+dindex+");\" title=\""+SystemEnv.getHtmlLabelName(556, languageid)+"\" />").append("\n");
					detailHtml.append("<span>").append(SystemEnv.getHtmlLabelName(15486,languageid)).append("</span>");
				}
				detailHtml.append("</td>").append("\n");
			}
			for(int col=0;col<colnum;col++){
				cellValue=Util.null2String(cellArr[row][col]);
				if("combine_cell".equals(cellValue)){
					continue;
				}else if("".equals(cellValue)){
					String classname_col = getClassByColAttrs(colattrs, col);
					detailHtml.append("<td class=\""+classname_col+"\"></td>");
				}else{
					detailHtml.append(cellValue);
				}
			}
			detailHtml.append("</tr>").append("\n");
		}
		detailHtml.append("</tbody>").append("\n").append("</table>").append("\n");
		detailHtml.append("</div>\n");
		return detailHtml.toString();
	}
	
	/**
	 * 明细解析单元格生成二维数组（新、老模板共有，易维护）
	 */
	private String[][] buildDetailCellArray(String detailid, JSONObject detailObj, JSONObject rowheads, JSONObject colheads, 
			JSONObject rowattrs, JSONObject colattrs, boolean seniorset) throws Exception{
		int dindex=Util.getIntValue(detailid.replace("detail_",""))-1;
		int edtitleinrow = Util.getIntValue(detailObj.getString("edtitleinrow"));	//表头所在行
		int edtailinrow = Util.getIntValue(detailObj.getString("edtailinrow"));		//表尾所在行
		int rownum = rowheads.length();		//行数
		int colnum = colheads.length();		//列数
		//方案：先初始化空二位数组，根据JSON中单元格属性生成TD的html放入数组中，同时根据colspan/rowspan的值修改相应的数组值
		JSONArray cells = detailObj.getJSONArray("ec");
		String[][] cellArr = new String[rownum][colnum];
		StringBuilder cellContent = new StringBuilder();
		StringBuilder cellStyle = new StringBuilder();
		String cellid,efield,efinancial;
		int etype,rowid,colid,rowspan,colspan,borderstyle;
		for(int i=0; i<cells.length(); i++){
			cellContent.setLength(0);
			cellStyle.setLength(0);
			JSONObject cell = cells.getJSONObject(i);
			cellid = cell.getString("id");
			efield = "";
			efinancial = "";
			if(cell.has("field"))		efield = cell.getString("field");
			if(cell.has("financial"))	efinancial = cell.getString("financial");
			etype = Util.getIntValue(cell.getString("etype"),0);
			rowid = Util.getIntValue(cellid.substring(0, cellid.indexOf(",")));
			colid = Util.getIntValue(cellid.substring(cellid.indexOf(",")+1));
			if(rowid == edtitleinrow || rowid == edtailinrow)		//表头表尾不解析
				continue;
			if(!seniorset && rowid>edtailinrow)		//简单模式表尾后行不解析
				continue;
			//单元格自定义属性
			String cellattr_text="",cellattr_dftext="",cellattr_class="",cellattr_style="";
			if(cell.has("attrs")){
				Map<String,String> cellattr_map = parseAttrs(cell.getJSONObject("attrs"), true);
				cellattr_text = cellattr_map.get("_text");
				cellattr_dftext = cellattr_map.get("_dftext");
				cellattr_class = cellattr_map.get("_class");
				cellattr_style = cellattr_map.get("_style");
			}
			if(!"".equals(cellattr_class))
				cellattr_class = " "+cellattr_class;
			cellattr_class += getClassByColAttrs(colattrs, colid);
			cellContent.append("<td ").append(cellattr_text);
			//如果cell为合并内部的单元格,则不处理
			if("combine_cell".equals(cellArr[rowid][colid]))
				continue;
			//行合并、列合并处理
			rowspan = 1;
			colspan = 1;
			if(cell.has("rowspan"))
				rowspan = Util.getIntValue(cell.getString("rowspan"),1);
			if(cell.has("colspan"))
				colspan = Util.getIntValue(cell.getString("colspan"),1);
			if(rowspan>1 || colspan>1){
				if(rowspan>1)
					cellContent.append(" rowspan=\"").append(rowspan).append("\"");
				if(colspan>1)
					cellContent.append(" colspan=\"").append(colspan).append("\"");
				if(rowspan>rownum)	rowspan = rownum;
				if(colspan>colnum)	colspan = colnum;
				for(int num1=0; num1<rowspan; num1++){
					for(int num2=0; num2<colspan; num2++){
						if(num1==0 && num2==0)	continue;				//第一个单元格为数据格
						cellArr[rowid+num1][colid+num2] = "combine_cell";	//标示为合并单元格
					}
				}
			}
			//解析TD的style
			String className = "detail"+dindex+"_"+rowid+"_"+colid;
			cellStyle.append(this.parseCellStyle(cell, className));
			
			if(cell.has("eborder")){
				cellStyle.append("\n").append("\t");
				JSONArray borders=cell.getJSONArray("eborder");
				for(int j=0;j<borders.length();j++){
					JSONObject border=borders.getJSONObject(j);
					borderstyle=Util.getIntValue(border.getString("style"));
					if(colid==0&&"left".equals(border.getString("kind")))
						continue;		//第一列的左边框忽略
					if(colid+colspan-1==colnum-1&&"right".equals(border.getString("kind")))
						continue;		//最后一列的右边框忽略
					cellStyle.append(this.parseBorder(borderstyle, border.getString("kind"), border.getString("color")));
				}
			}
			cellContent.append(" class=\"").append(className).append(cellattr_class).append("\"");	//TD生成Class
			String cellattr = detailid.toUpperCase()+"."+this.getCellAttr(rowid, colid);
			if(etype == 3){
				if(type == 0 || type == 1){	//如果是字段则将class名放在TD上供显示模板解析调用
					cellContent.append(" ").append(cellattr_dftext);
					cellContent.append(" _fieldclass=\"$[").append(className).append(cellattr_class).append("]$\"");
				}
				if(type == 0){	//增加公式相关属性
					cellContent.append(" _cellattr=\"$[").append(cellattr).append("]$\"");
					cellContent.append(" _fieldid=\"$[").append(efield).append("]$\"");
					if(formulaMap.containsKey(cellattr))
						cellContent.append(" _formula=\"$[").append(formulaMap.get(cellattr)).append("]$\"");
					if(seniorset)
						cellContent.append(" _fieldtype=\"").append(excelLayoutManager.getFieldType(formid, isbill, Util.getIntValue(efield), '_')).append("\"");
				}
			}
			if(etype == 2 && type == 2 && !"".equals(efield)){		//标示手机版字段名称，用于编辑明细
				cellContent.append(" _fieldlabel=\"").append(efield).append("\"");
			}
			if(efinancial.startsWith("1")){		//财务表头
				cellContent.append(this.parseFinancial(efinancial));
			}
			if(isFromApp == 1){
				if(colid > 0 && colid<=3){
					if(colid == 2 || colid == 3){
						cellContent.append("style=\"width:45%;\"");
					}else if(colid == 1){
						cellContent.append("style=\"width:10%;\"");
					}
				}
				if(rowid == 1 && colid > 3){
					cellContent.append("style=\"display:none;\"");
				}else if(rowid > 1){
					if(colid > 3){
						cellContent.append("style=\"display:none;\"");
					}
				}
			}
			cellContent.append(">").append("\n");
			
			/**********TD标签结束，解析TD内容**********/
			cell.put("cellattr", cellattr);
			Map<String,String> cellmap = this.parseCellText(detailid, cell, seniorset);
			cellContent.append(cellmap.get("cellText"));
			cellStyle.append(cellmap.get("cellStyle"));
			
			cellContent.append("</td>").append("\n");
			//System.err.println(cellContent.toString());
			cellArr[rowid][colid]=cellContent.toString();
			//生成样式并放入对象
			cellStyle.append("height:").append(rowheads.get("row_"+rowid)).append("px;");
			tempCss.append(".").append(className).append("{").append(cellStyle).append("\n").append(cellattr_style).append("\n").append("}").append("\n");
		}
		return cellArr;
	}
	
	/**
	 * 解析多内容区域
	 */
	private String analyzeMoreContent(String mcpoint) throws JSONException{
		StringBuilder mcHtml = new StringBuilder();
		try{
			JSONObject mcObj = etable.getJSONObject(mcpoint);
			int rowcount = Util.getIntValue(mcObj.getString("rowcount"));
			int colcount = Util.getIntValue(mcObj.getString("colcount"));
			String[][] cellArr = new String[rowcount][colcount];
			JSONArray cells = mcObj.getJSONArray("ec");
			
			StringBuilder cellContent = new StringBuilder();
			StringBuilder cellStyle = new StringBuilder();
			Map<String,String> cellattr_map = new HashMap<String,String>();
			String cellattr_text,cellattr_class,cellattr_style;
			for(int i=0; i<cells.length(); i++){
				cellContent.setLength(0);
				cellStyle.setLength(0);
				JSONObject cell = cells.getJSONObject(i);
				String cellid = cell.getString("id");
				int rowid = Util.getIntValue(cellid.substring(0, cellid.indexOf(",")));
				int colid = Util.getIntValue(cellid.substring(cellid.indexOf(",")+1));
				int etype = Util.getIntValue(cell.getString("etype"), 0);
				String efield = "";
				if(cell.has("field"))		efield = cell.getString("field");
				if(etype == 14){		//换行单元格
					if("Y".equals(Util.null2String(cell.getString("brsign"))))
						cellContent.append("</br>");
				}else{
					//单元格自定义属性
					if(cell.has("attrs")){
						cellattr_map = parseAttrs(cell.getJSONObject("attrs"));
						cellattr_text = cellattr_map.get("_text");
						cellattr_class = cellattr_map.get("_class");
						cellattr_style = cellattr_map.get("_style");
					}else{
						cellattr_text = "";
						cellattr_class = "";
						cellattr_style = "";
					}
					String className = mcpoint+"_"+rowid;
					cellStyle.append(this.parseCellStyle(cell, className));	//解析样式
					
					cellContent.append("<span ").append(cellattr_text)
						.append(" class=\"span_mc ").append(className).append(" ").append(cellattr_class).append("\" ")
						.append(" style=\"display:inline-block;").append(cellattr_style).append("\" ");
					if(etype == 2){
						cellContent.append(" _fieldlabel=\""+efield+"\" ");
					}else if(etype == 3){
						cellContent.append(" _fieldid=\""+efield+"\" ");
					}
					cellContent.append(">").append("\n");
					Map<String,String> cellmap = this.parseCellText(mcpoint, cell);
					cellContent.append(cellmap.get("cellText"));
					cellStyle.append(cellmap.get("cellStyle"));
					cellContent.append("</span>").append("\n");
					tempCss.append(".").append(className).append("{").append(cellStyle).append("\n").append("}").append("\n");
				}
				cellArr[rowid][colid] = cellContent.toString();
			}
			//组装二维数组内容生成多内容Html
			for(int i=0; i<rowcount; i++){
				for(int j=0; j<colcount; j++){
					String arrVal=Util.null2String(cellArr[i][j]);
					if(!"".equals(arrVal))
						mcHtml.append(arrVal);
				}
			}
		}catch(Exception e){
			writeLog(e);
		}
		return mcHtml.toString();
	}
	
	/**
	 * 解析table宽度，不使用colgroup，改用隐藏TR内放TD控制
	 */
	private String parseMainTableWidth(JSONObject colheads, boolean needTransPercent) throws JSONException{
		StringBuilder width_tr = new StringBuilder();
		width_tr.append("<tr name=\"controlwidth\">\n");
		JSONObject colheads_new = colheads;
		if(needTransPercent)
			colheads_new = countPercentWidth(colheads, 100);
		for(int i=0; i<colheads_new.length(); i++){
			String colwidth = colheads_new.getString("col_"+i);
			if(colwidth.indexOf("%") == -1 && colwidth.indexOf("px") == -1)
				colwidth += "px";
			width_tr.append("<td width=\"").append(colwidth).append("\"").append("></td>\n");
		}
		width_tr.append("</tr>\n");
		return width_tr.toString();
	}
	private String parseDetailTableWidth(JSONObject colheads, JSONObject colattrs, String serColWidth, int fullPercent) throws JSONException{
		StringBuilder width_tr = new StringBuilder();
		width_tr.append("<tr name=\"controlwidth\" class=\"exceldetailtitle\">\n");
		if(!"".equals(serColWidth))
			width_tr.append("<td width=\"").append(serColWidth).append("\"></td>\n");
		JSONObject colheads_new = colheads;
		if(fullPercent > 0)
			colheads_new = countPercentWidth(colheads, fullPercent);
		int tdCount = 0;
		for(int i=0; i<colheads_new.length(); i++){
			String classname = getClassByColAttrs(colattrs, i);
			String colwidth = colheads_new.getString("col_"+i);
			if(colwidth.indexOf("%") == -1 && colwidth.indexOf("px") == -1){
				colwidth += "px";
			}
			String tdStyle = "";
			if(isFromApp == 1){
				tdCount++;
				if(tdCount > 3){
					tdStyle = "style='display:none'";
				}
			}
			width_tr.append("<td "+tdStyle+" width=\"").append(colwidth).append("\" ").append("".equals(classname)?"":"class=\""+classname+"\" ").append("></td>\n");
		}
		width_tr.append("</tr>\n");
		return width_tr.toString();
	}
	
	/**
	 * 解析单元格内容
	 */
	private Map<String,String> parseCellText(String symbol, JSONObject cell) throws Exception{
		return parseCellText(symbol, cell, false);
	}
	private Map<String,String> parseCellText(String symbol, JSONObject cell, boolean seniorset) throws Exception{
		StringBuilder cellText = new StringBuilder();
		StringBuilder cellStyle = new StringBuilder();
		
		int etype=Util.getIntValue(cell.getString("etype"), 0);
		String evalue="", efinancial="", efield="", cellattr="";
		if(cell.has("evalue"))		evalue = cell.getString("evalue");
		if(cell.has("field"))		efield = cell.getString("field");
		if(cell.has("financial"))	efinancial = cell.getString("financial");
		if(cell.has("cellattr"))		cellattr = cell.getString("cellattr");
		if(etype==1){			//文本
			cellText.append("<span>");
			cellText.append(evalue.replaceAll("\r\n", "</br>").replaceAll("\r", "</br>").replaceAll("\n", "</br>").replaceAll(" ","&nbsp;"));
			cellText.append("</span>");
		}else if(etype==2){		//字段名称
			cellText.append("<span>");
			cellText.append(excelLayoutManager.getFieldName(Util.getIntValue(efield), formid, isbill, languageid));
			cellText.append("</span>");
		}else if(etype==3){		//表单内容
			cellText.append("<input type=\"hidden\" ");
			if(efinancial.startsWith("2")||efinancial.startsWith("3")||efinancial.startsWith("4")){		//财务格式先于格式化解析
				cellText.append(this.parseFinancial(efinancial));
			}else{
				if(cell.has("format")){
					JSONObject format=cell.getJSONObject("format");
					if(!"".equals(format.toString().replace("{", "").replace("}", ""))){
						cellText.append("_format=\"$").append(format.toString().replace("\"", "")).append("$\" ");
					}
				}
			}
			if(formulaMap.containsKey(cellattr)){		//字段包含公式
				cellText.append(" _formulaField_ ");
			}
			cellText.append("class=\"InputStyle\" ")
				.append("id=\"$field").append(efield).append("$\" ")
				.append("name=\"field").append(efield).append("\" ")
				.append("value=\"").append(evalue).append("\" ");
			cellText.append(" />");
		}else if(etype==4){		//节点名称
			if(Util.getIntValue(efield)==999999999){
				cellText.append(SystemEnv.getHtmlLabelNames("21779,17614", languageid));
			}else{
				efield = manageNodeid(efield);
				rs.executeSql(" select nodename from workflow_nodebase where id="+(Util.getIntValue(efield)));
				if(rs.next()){
					cellText.append(rs.getString("nodename"));
				}
			}
		}else if(etype==5){		//流转意见
			efield = manageNodeid(efield);
			cellText.append("<input class=\"InputStyle\" id=\"$node").append(efield)
				.append("$\" name=\"node").append(efield).append("\" />");
		}else if(etype==6){		//图片(添加TD的背景图片)
			if(cell.has("field")){
				String imageurl = Util.null2String(cell.get("field"));
				if(!"".equals(imageurl)){
					cellStyle.append("background-image:url("+imageurl+") !important; ");
					cellStyle.append("background-repeat:no-repeat !important; ");
				}
			}
			cellText.append("<span>");
			cellText.append(evalue.replaceAll("\r\n", "</br>").replaceAll("\r", "</br>").replaceAll("\n", "</br>").replaceAll(" ","&nbsp;"));
			cellText.append("</span>");
		}else if(etype==7){		//明细表
			if(cell.has("detail")){
				String detailid = cell.getString("detail");
				if(etable.has(detailid))
					cellText.append(this.analyzeDetail(detailid));
			}
			cellStyle.append("padding:0px !important; ");
		}else if(etype==10){		//按钮
			int dindex=Util.getIntValue(symbol.replace("detail_",""))-1;
			if(isFromApp == 1){
				cellText.append("<div id=\"div").append(dindex).append("button\" class=\"detailButtonDiv\" style=\"width:100px;\">").append("\n");
			}else{
				cellText.append("<div id=\"div").append(dindex).append("button\" class=\"detailButtonDiv\" style=\"width:"+(type==2?160:100)+"px;\">").append("\n");
			}
			//cellText.append("<div id=\"div").append(dindex).append("button\" class=\"detailButtonDiv\" style=\"\">").append("\n");
			
			WFNodeDtlFieldManager wFNodeDtlFieldManager = new WFNodeDtlFieldManager();
			wFNodeDtlFieldManager.resetParameter();
			wFNodeDtlFieldManager.setNodeid(nodeid);
			wFNodeDtlFieldManager.setGroupid(dindex);
			wFNodeDtlFieldManager.selectWfNodeDtlField();
			if("1".equals(wFNodeDtlFieldManager.getIsopensapmul())){
				cellText.append("<button class=\"sapbtn"+(type==2?"_m":"_p")+"\" type=\"button\" id=\"$sapmulbutton").append(dindex).append("$\" name=\"sapmulbutton").append(dindex)
					.append("\" onclick=\"addSapRow").append(dindex+"("+dindex+")").append(";return false;\" title=\"SAP\"></button>").append("\n");
			}
			cellText.append("<button class=\"addbtn"+(type==2?"_m":"_p")+"\" type=\"button\" id=\"$addbutton").append(dindex).append("$\" name=\"addbutton").append(dindex)
				.append("\" onclick=\"addRow").append(dindex+"("+dindex+")").append(";return false;\" title=\"").append(SystemEnv.getHtmlLabelName(611, languageid)).append("\"></button>").append("\n");
			cellText.append("<button class=\"delbtn"+(type==2?"_m":"_p")+"\" type=\"button\" id=\"$delbutton").append(dindex).append("$\" name=\"delbutton").append(dindex)
				.append("\" onclick=\"deleteRow").append(dindex+"("+dindex+")").append(";return false;\" title=\"").append(SystemEnv.getHtmlLabelName(23777, languageid)).append("\"></button>").append("\n");
			cellText.append("</div>");
		}else if(etype==11){	//链接
			if(cell.has("fieldtype") && cell.has("field"))
				cellText.append(this.parseHref(Util.getIntValue(cell.getString("fieldtype"),1), Util.null2String(cell.get("field")), evalue));
		}else if(etype==12 && "main".equals(symbol)){	//标签页
			JSONObject tab = cell.getJSONObject("tab");
			cellText.append(this.analyzeTabArea(tab));
			cellStyle.append("padding:6px; ");
		}else if(etype == 13){		//多内容
			if(cell.has("mcpoint")){
				String mcpoint = cell.getString("mcpoint");
				if(etable.has(mcpoint))
					cellText.append(analyzeMoreContent(mcpoint));
			}
		}else if(etype == 15){		//门户元素
			if(type == 2){
				cellText.append("手机端不支持门户元素！");
			}else{
				if(cell.has("jsonparam"))
					cellText.append(parseHtml_portal(cell.getJSONObject("jsonparam")));
			}
		}else if(etype == 16){		//iframe区域
			if(cell.has("jsonparam"))
				cellText.append(parseHtml_iframe(cell.getJSONObject("jsonparam")));
		}else if(etype == 17){		//二维/条形码
			if(cell.has("jsonparam"))
				cellText.append(parseHtml_scancode(cell.getJSONObject("jsonparam")));
		}else if(etype == 18){
			cellText.append("<span>");
			cellText.append(excelLayoutManager.getFieldName(Util.getIntValue(efield), formid, isbill, languageid)).append("(").append(SystemEnv.getHtmlLabelName(358, languageid)).append(")");
			cellText.append("</span>");
		}else if(etype == 19){
			cellText.append("<input type=\"hidden\" id=\"$sumfield"+efield+"$\" name=\"sumfield"+efield+"\" value=\""+evalue+"\" />");
		}else if(etype == 20 || etype == 21 || etype == 22){
			cellText.append("<input type=\"hidden\" name=\"detailSpecialMark\" value=\""+etype+"\" />");
		}
		Map<String,String> retmap = new HashMap<String,String>();
		retmap.put("cellText", cellText.toString()+"\n");
		retmap.put("cellStyle", cellStyle.toString());
		return retmap;
	}
	

	/**
	 * 解析自定义属性
	 */
	private Map<String,String> parseAttrs(JSONObject attrjson) throws JSONException{
		return parseAttrs(attrjson, false);
	}
	private Map<String,String> parseAttrs(JSONObject attrjson,boolean isdetailcell) throws JSONException{
		StringBuilder _text = new StringBuilder();
		StringBuilder _dftext = new StringBuilder();
		String _class = "";
		String _style = "";
		Iterator<?> it = attrjson.keys();
		while(it.hasNext()){
			String itkey = it.next().toString();
			String itval = Util.null2String(attrjson.get(itkey));
			if("hide".equals(itkey)){
				if("y".equals(itval)){
					if(!"".equals(_style) && !_style.endsWith(";"))
						_style += ";";
					_style += "display:none;";
				}
			}else if("style".equals(itkey)){
				_style += itval;
			}else if("class".equals(itkey)){
				_class = itval;
			}else{
				_text.append(itkey).append("=").append("\"").append(itval).append("\" ");
				if(isdetailcell)
					_dftext.append("_attr").append(itkey).append("=").append("\"$[").append(itval).append("]$\" ");
			}
		}
		Map<String,String> retmap = new HashMap<String,String>();
		retmap.put("_text", _text.toString());
		retmap.put("_dftext", _dftext.toString());
		retmap.put("_class", _class);
		retmap.put("_style", _style);
		return retmap;
	}
	
	/**
	 * 根据明细列自定义属性得到明细TD的class
	 */
	private String getClassByColAttrs(JSONObject colattrs, int colid) throws JSONException{
		String classname = "";
		if(colattrs.has("col_"+colid)){
			JSONObject colattr = colattrs.getJSONObject("col_"+colid);
			if(colattr.has("hide") && "y".equals(colattr.getString("hide")))
				classname += " detail_hide_col";		//隐藏列通过加class实现
			if(colattr.has("class") && !"".equals(colattr.getString("class")))
				classname += " "+colattr.getString("class");
		}
		return classname;
	}
	
	/**
	 * 解析单元格样式
	 */
	private String parseCellStyle(JSONObject cell, String className) throws JSONException{
		StringBuilder sbStyle = new StringBuilder();
		//字体、字号、颜色、斜体、粗体、下划线、删除线等样式提取出来，作为.className *{}，应用给TD所有子对象
		StringBuilder fontStyle = new StringBuilder();
		String itkey;
		boolean isMc = className.startsWith("mc_");
		int etype = Util.getIntValue(cell.getString("etype"),0);
		if(cell.has("backgroundColor") && etype!=7){
			sbStyle.append("background:").append(cell.get("backgroundColor")).append("!important; ");
		}
		JSONObject font = new JSONObject();
		if(cell.has("font")){
			font = cell.getJSONObject("font");
		}
		Iterator<?> it = font.keys();
		while(it.hasNext()){
			itkey = it.next().toString();
			if("text-align".equals(itkey)){	//水平对齐
				if(!"".equals(font.get(itkey)))
					sbStyle.append("text-align:").append(font.get(itkey)).append("; ");
			}else if("valign".equals(itkey) && !isMc){		//垂直对齐
				if(!"".equals(font.get(itkey)))
					sbStyle.append("vertical-align:").append(font.get(itkey)).append("; ");
			}else if("autoWrap".equals(itkey)){	//自动换行
				//if("true".equals(Util.null2String(font.get(itkey))))
				//	sbStyle.append("word-break:break-all; word-wrap:break-word; ");
			}else if("italic".equals(itkey)){		//斜体
				if("true".equals(Util.null2String(font.get(itkey))))
					fontStyle.append("font-style:italic; ");
			}else if("bold".equals(itkey)){			//粗体
				if("true".equals(Util.null2String(font.get(itkey))))
					fontStyle.append("font-weight:bold; ");
			}else if("underline".equals(itkey)){	//下划线
				if("true".equals(Util.null2String(font.get(itkey))))
					fontStyle.append("text-decoration:underline; ");
			}else if("deleteline".equals(itkey)){	//删除线
				if("true".equals(Util.null2String(font.get(itkey))))
					fontStyle.append("text-decoration:line-through; ");
			}else if("font-size".equals(itkey)){
				if(!"".equals(font.get(itkey))){
					fontStyle.append("font-size:").append(font.get(itkey)).append("!important; ");
					int fontsize = Util.getIntValue(font.getString(itkey).trim().replace("pt", ""));
					if(etype == 3 && fontsize > 9){
						int suitheight = fontsize_compare_height(fontsize);
						tempCss.append(".").append(className).append(" input[type=\"text\"]{\n")
							.append("height:"+suitheight+"px; line-height:"+suitheight+"px;").append("\n}\n");
					}
				}
			}else if("font-family".equals(itkey)){
				if(!"".equals(font.get(itkey)))
					fontStyle.append("font-family:").append(font.get(itkey)).append("!important; ");
			}else if("color".equals(itkey)){
				if(!"".equals(font.get(itkey)))
					fontStyle.append("color:").append(font.get(itkey)).append("!important; ");
			}
		}
		sbStyle.append("word-break:break-all; word-wrap:break-word; ");
		//默认居左、居上、9pt、微软雅黑,默认属性都不加!important
		if(sbStyle.indexOf("text-align") == -1)
			sbStyle.append("text-align:left; ");
		if(sbStyle.indexOf("vertical-align") == -1)
			sbStyle.append("vertical-align:").append(isMc?"middle":"top").append("; ");
		if(fontStyle.indexOf("font-size") == -1)
			fontStyle.append("font-size:9pt; ");
		if(fontStyle.indexOf("font-family") == -1)
			fontStyle.append("font-family:Microsoft YaHei; ");
		//单元格缩进
		if(cell.has("etxtindent") && !"0".equals(cell.getString("etxtindent")) && Pattern.matches("\\d+(\\.\\d+)?", cell.getString("etxtindent"))){
			if(sbStyle.indexOf("text-align:left")>-1){
				sbStyle.append("padding-left:").append(Double.parseDouble(cell.getString("etxtindent"))*8).append("px; ");
			}else if(sbStyle.indexOf("text-align:right")>-1){
				sbStyle.append("padding-right:").append(Double.parseDouble(cell.getString("etxtindent"))*8).append("px; ");
			}
		}
		if(etype<=6 || etype==11 || etype==18 || etype==19 || etype==20 || etype==21 || etype==22){
			tempCss.append(".").append(className).append(" *{\n").append(fontStyle).append("\n}\n");
			if(isMc)		//多字段放节点意见，未包层div/span等导致字体样式无效
				sbStyle.append(fontStyle);
		}
		return sbStyle.toString();
	}
	
	/**
	 * 解析背景图、悬浮图
	 */
	private String parseTableImage(String symbol, JSONObject tableObj, int colnum) throws JSONException{
		if(tableObj.has("backgroundImage")||tableObj.has("floatingObjectArray")){
			StringBuilder returnSb = new StringBuilder();
			if(symbol.startsWith("detail")){
				returnSb.append("<tr class=\"exceldetailtitle\">");
			}else{
				returnSb.append("<tr>");
			}
			returnSb.append("<td colspan=\""+colnum+"\" style=\"position:relative;padding:0px !important;margin:0px !important;\">").append("\n");
			if(tableObj.has("backgroundImage")){			//背景图
				String backgroundSrc=Util.null2String(tableObj.get("backgroundImage"));
				//if(symbol.startsWith("detail"))		backgroundSrc=backgroundSrc.replaceAll("\\\\/", "/");
				returnSb.append("<img src=\"").append(backgroundSrc).append("\" style=\"position:absolute;z-index:-100;top:0px;left:0px;\" />").append("\n");
			}
			if(tableObj.has("floatingObjectArray")){		//悬浮图
				JSONObject floatingImgArrObj=tableObj.getJSONObject("floatingObjectArray");
				if(floatingImgArrObj.has("floatingObjects")){
					JSONArray floatingImgArr=floatingImgArrObj.getJSONArray("floatingObjects");
					for(int i=0;i<floatingImgArr.length();i++){
						JSONObject floatingImg=floatingImgArr.getJSONObject(i);
						if(floatingImg.has("x")&&floatingImg.has("y")&&floatingImg.has("width")&&floatingImg.has("height")&&floatingImg.has("src")){
							String floatSrc=Util.null2String(floatingImg.get("src"));
							//if(symbol.startsWith("detail"))	floatSrc=floatSrc.replaceAll("\\\\/", "/");
							returnSb.append("<div style=\"position:absolute; z-index:99999; padding:0px; margin:0px; ")
								.append("width:").append(floatingImg.getString("width")+"px").append("; ")
								.append("height:").append(floatingImg.getString("height")+"px").append("; ")
								.append("top:").append(floatingImg.getString("y")+"px").append("; ")
								.append("left:").append(floatingImg.getString("x")+"px").append("; ")
								//.append("background-image:url(").append(floatSrc).append("); ")
								//.append("background-size:100% 100%; background-position:0% 0%; background-repeat:no-repeat; ")
								//.append("\"></div>").append("\n");
								.append("\"><img src=\"").append(floatSrc).append("\" style=\"width:100%;height:100%\" /></div>").append("\n");
						}
					}
				}
			}
			returnSb.append("</td>").append("</tr>").append("\n");
			return returnSb.toString();
		}else{
			return "";
		}
	}
	
	/**
	 * 解析单元格每种边框
	 */
	private StringBuffer parseBorder(int style,String kind,String color){
		StringBuffer borderStr=new StringBuffer();
		switch(style){
			case 0:
				break;
			case 1:
				borderStr.append("border-").append(kind).append("-width:1px; ")
					.append("border-").append(kind).append("-style:solid; ");
				break;
			case 2:
				borderStr.append("border-").append(kind).append("-width:2px; ")
					.append("border-").append(kind).append("-style:solid; ");
				break;
			case 3:
				borderStr.append("border-").append(kind).append("-width:1px; ")
					.append("border-").append(kind).append("-style:dashed; ");
				break;
			case 5:
				borderStr.append("border-").append(kind).append("-width:3px; ")
					.append("border-").append(kind).append("-style:solid; ");
				break;
			case 6:
				borderStr.append("border-").append(kind).append("-width:3px; ")
					.append("border-").append(kind).append("-style:double; ");
				break;
			case 7:
				borderStr.append("border-").append(kind).append("-width:1px; ")
					.append("border-").append(kind).append("-style:dotted; ");
				break;
			case 8:
				borderStr.append("border-").append(kind).append("-width:2px; ")
					.append("border-").append(kind).append("-style:dashed; ");
				break;
			default:
				borderStr.append("border-").append(kind).append("-width:1px; ")
					.append("border-").append(kind).append("-style:solid; ");
				break;
		}
		if(!"".equals(color))
			borderStr.append("border-").append(kind).append("-color:").append(color).append("; ");
		return borderStr;
	}
	
	/**
	 * 解析财务格式串
	 */
	private String parseFinancial(String efinancial){
		String str = "";
		int findex =3 ;
		if(efinancial.startsWith("1")){
			if(efinancial.indexOf("-")>-1)
				findex = Util.getIntValue(efinancial.substring(efinancial.indexOf("-")+1),3);
			str = " _financialHead=\"$["+findex+"]$\" ";
		}else if(efinancial.startsWith("2")){
			if(efinancial.indexOf("-")>-1)
				findex = Util.getIntValue(efinancial.substring(efinancial.indexOf("-")+1),3);
			str = " _financialField=\"$["+findex+"]$\" ";
		}else if(efinancial.startsWith("3")){	//千分位、金额大写模拟使用格式化逻辑
			str = " _format=\"${decimals:-1,formatPattern:-1,thousands:-1,numberType:99}$\" ";
		}else if(efinancial.startsWith("4")){
			str = " _format=\"${decimals:2,formatPattern:2,thousands:1,numberType:2}$\" ";
		}
		return str;
	}
	
	/**
	 * 解析链接
	 */
	private String parseHref(int fieldtype,String field,String text){
		String _html="<a target=\"_blank\" href=\"";
		switch(fieldtype){
			case 1:
				_html += "http://";
				break;
			case 2:
				_html += "https://";
				break;
			case 3:
				_html += "ftp://";
				break;
			case 4:
				_html += "news://";
				break;
		}
		_html += field+"\">"+text+"</a>";
		return _html;
	}
	
	/**
	 * 解析门户元素
	 */
	private String parseHtml_portal(JSONObject params) throws JSONException{
		String hpid = params.has("hpid") ? params.getString("hpid") : "";
		String trifields = params.has("trifields") ? params.getString("trifields") : "";
		if("".equals(hpid))
			return "";
		StringBuilder _html = new StringBuilder();
		String iframeSrc = "/homepage/maint/HomepageForWorkflow.jsp?isSetting=false&hpid="+hpid+"&paramfieldid="+trifields;
		iframeSrc += "&requestid="+requestid+"&wfid="+wfid+"&nodeid="+nodeid+"&formid="+formid+"&isbill="+isbill+"&moduleid="+modeid+"&layouttype="+type;
		//表单字段联动门户元素，ready绑定事件及JS发起iframe请求
		if(!"".equals(trifields)){
			_html.append("<input type=\"hidden\" id=\"portalInfo_").append(hpid)
				.append("\" _trifields=\"").append(trifields).append("\" value=\"").append(iframeSrc).append("\" />");
			iframeSrc = "";
			tempScript.append("jQuery(document).ready(function(){").append("\n");
			tempScript.append("\t portalOperate.initEvent('"+hpid+"');").append("\n");
			tempScript.append("});\n");
		}
		_html.append("<div class=\"portalLoading\" id=\"portalLoading_"+hpid+"\">")
			.append("<span><img src=\"/images/loading2_wev8.gif\" align=\"absmiddle\" /></span>")
			.append("<span>  ").append(SystemEnv.getHtmlLabelName(125516, languageid)).append("</span>")
			.append("</div>")
			.append("<iframe id=\"portalIframe_").append(hpid)
			.append("\" name=\"portalIframe_").append(hpid)
			.append("\" src=\"").append(iframeSrc)
			.append("\" frameborder=\"0\" scrolling=\"auto\" ")
			.append("style=\"width:100%;height:100%;display:none;\" >").append("</iframe>");
		return _html.toString();
	}
	
	/**
	 * 解析iframe区域
	 */
	private String parseHtml_iframe(JSONObject params) throws JSONException{
		String id = params.has("set_id") ? params.getString("set_id") : "";
		String name = params.has("set_name") ? params.getString("set_name") : "";
		String src = params.has("set_src") ? params.getString("set_src") : "";
		String height = params.has("set_height") ? params.getString("set_height") : "";
		String style = params.has("set_style") ? params.getString("set_style") : "";
		if("".equals(src) || "".equals(height))
			return "";
		
		if(src.indexOf("$requestid$") > -1)
			src = src.replaceAll("\\$requestid\\$", requestid+"");
		if(src.indexOf("$workflowid$") > -1)
			src = src.replaceAll("\\$workflowid\\$", wfid+"");
		if(src.indexOf("$nodeid$") > -1)
			src = src.replaceAll("\\$nodeid\\$", nodeid+"");
		if(src.indexOf("$formid$") > -1)
			src = src.replaceAll("\\$formid\\$", formid+"");
		if(type == 2){		//手机端src需做跳转
			String srcPage = src;
			String srcParams = "";
			if(src.indexOf("?") > 0){
				srcPage = src.substring(0, src.indexOf("?"));
				srcParams = "&"+src.substring(src.indexOf("?")+1);
			}
			if("".equals(srcPage) || srcPage.startsWith("http://") || srcPage.startsWith("https://")
					|| srcPage.endsWith(".com") || srcPage.endsWith(".cn")){
			}else{
				src = "/mobile/plugin/wfIframeForward.jsp?forwardUrl="+srcPage+srcParams;
			}
		}
		
		String iframeAutoStr = "";
		if("auto".equals(height)){	//自适应高度
			height = "100";
			iframeAutoStr = " adjustheight='y' eachcount=0 ";
		}
		StringBuilder _html = new StringBuilder();
		_html.append("<div class=\"iframeLoading\" style=\"height:"+height+"px;line-height:"+height+"px;\">")
			.append("<span><img src=\"/images/loading2_wev8.gif\" align=\"absmiddle\" /></span>")
			.append("<span>  ").append(SystemEnv.getHtmlLabelName(125516, languageid)).append("</span>")
			.append("</div>")
			.append("<iframe src=\""+src+"\"");
		if(!"".equals(id))		_html.append(" id=\""+id+"\"");
		if(!"".equals(name))	_html.append(" name=\""+name+"\"");
		_html.append(" onload=\"iframeOperate.loadingOver(this);\" frameborder=\"0\" scrolling=\"auto\"")
			.append(" style=\"width:100%;display:none;")
			.append("auto".equals(height) ? "" : ("height:"+height+"px"))
			.append(style).append("\"")
			.append(iframeAutoStr).append("></iframe>");
		return _html.toString();
	}
	
	/**
	 * 解析二维/条形码
	 */
	private String parseHtml_scancode(JSONObject params) throws JSONException{
		String codetype = params.has("codetype") ? params.getString("codetype") : "";
		String relatefield = params.has("relatefield") ? params.getString("relatefield") : "";
		String msg = calculateRelateValue(relatefield);
		String _html = "";
		if("1".equals(codetype)){
			if("".equals(msg))
				msg = SystemEnv.getHtmlLabelName(15863, languageid);
			String url = "/createQRCode?firstParam=1";
			if(this.type == 2)
				url = "/download.do?forwardurl=/createQRCode&from=onlyForward";
			url += "&msg="+msg;
			
			_html = "<img class=\"scancode qrcodeimg\" src=\""+url+"\" />";
		}else if("2".equals(codetype) && !"".equals(msg)){
			char[] msgchar = msg.toCharArray();
			boolean illegalASCII = false;
			for(char c : msgchar){
				if(isChinese(c)){
					illegalASCII = true;
					break;
				}
			}
			if(illegalASCII){
				_html = "<img class=\"scancode\" src=\"/workflow/exceldesign/image/illegalBarCode_wev8.png\" />";
			}else{
				String url = "/createWfBarCode?firstParam=1";
				if(this.type == 2)
					url = "/download.do?forwardurl=/createWfBarCode&from=onlyForward";
				url += "&type=code128&qz=6&msg="+msg;
				if(params.has("hidetext") && "true".equals(params.getString("hidetext")))
					url += "&hrp=none";
				
				_html = "<img class=\"scancode barcodeimg\" src=\""+url+"\" />";
			}
		}
		return _html;
	}
	
	/**
	 * 计算二维/条形码关联字段值,
	 */
	private String calculateRelateValue(String relatefield){
		if("".equals(relatefield) || this.requestid <= 0)
			return "";
		String relateFieldValue = "";
		String[] fieldArr = relatefield.split(",");
		String tablename = getTableName();
		for(String fieldid : fieldArr){
			String _value = "";
			if("requestid".equals(fieldid)){
				_value = this.requestid+"";
			}else if("requestname".equals(fieldid)){
				rs.executeSql("select requestname from workflow_requestbase where requestid="+this.requestid);
				if(rs.next())
					_value = Util.null2String(rs.getString(1));
			}else{	//表单字段
				String columnname = getColumnName(fieldid);
				if(!"".equals(tablename) && !"".equals(columnname)){
					try{
						rs.executeSql("select "+columnname+" from "+tablename+" where requestid="+this.requestid);
						if(rs.next())
							_value = Util.null2String(rs.getString(1));
					}catch(Exception e){}
				}
			}
			relateFieldValue += _value;
		}
		return relateFieldValue;
	}
	
	/**
	 * 取数据库表名
	 */
	private String getTableName(){
		String tablename = "";
		if(this.isbill == 0){
			tablename = "workflow_form";
		}else if(this.isbill == 1){
			rs.executeSql("select tablename from workflow_bill where id="+this.formid);
			if(rs.next())
				tablename = Util.null2String(rs.getString("tablename"));
		}
		return tablename;
	}
	
	/**
	 * 取数据库字段列名
	 */
	private String getColumnName(String fieldid){
		if(Util.getIntValue(fieldid) <= 0)
			return "";
		String colname = "";
		if(this.isbill == 0){
			rs.executeSql("select fieldname from workflow_formdict where id="+fieldid);
			if(rs.next())
				colname = Util.null2String(rs.getString(1));
		}else if(this.isbill == 1){
			rs.executeSql("select fieldname from workflow_billfield where id="+fieldid+" and billid="+this.formid);
			if(rs.next())
				colname = Util.null2String(rs.getString(1));
		}
		return colname;
	}
	
	/**
	 * 判断是否是中文字符
	 */
	private static boolean isChinese(char c){
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }
	
	/**
	 * 解密字符串
	 */
	private String decodeStr(String content) {
		byte[] keybytes = "WEAVER E-DESIGN.".getBytes();
		byte[] iv = "weaver e-design.".getBytes();
		try {
			BufferedBlockCipher engine = new PaddedBufferedBlockCipher(
					new CBCBlockCipher(new AESFastEngine()));
			engine.init(true, new ParametersWithIV(new KeyParameter(keybytes),iv));
			byte[] deByte = Hex.decode(content);
			engine.init(false, new ParametersWithIV(new KeyParameter(keybytes),iv));
			byte[] dec = new byte[engine.getOutputSize(deByte.length)];
			int size1 = engine.processBytes(deByte, 0, deByte.length, dec, 0);
			int size2 = engine.doFinal(dec, size1);
			byte[] decryptedContent = new byte[size1 + size2];
			System.arraycopy(dec, 0, decryptedContent, 0,
					decryptedContent.length);
			return new String(decryptedContent);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return "";
	}
	
	/**
	 * 将公式JSON转换成HashMap，字段cellattr做为key，字段所涉及的所有公式做为value
	 */
	private HashMap<String,String> transFormulaJsonToMap(JSONObject formulaJson){
		HashMap<String,String> formulaMap = new HashMap<String,String>();
		Iterator<?> it = formulaJson.keys();
		while(it.hasNext()){
			try{
				String key = it.next().toString();
				JSONObject val = formulaJson.getJSONObject(key);
				String destcell = Util.null2String(val.get("destcell"));
				String formulatxt = Util.null2String(val.get("formulatxt"));
				if("".equals(destcell)||"".equals(formulatxt))
					continue;
				if(formulatxt.indexOf("=")==-1)
					continue;
				JSONArray cellrange = val.getJSONArray("cellrange");
				for(int i=0;i<cellrange.length();i++){
					String cellattr = cellrange.getString(i);
					if(formulaMap.containsKey(cellattr)){
						String formulaVal = formulaMap.get(cellattr);
						formulaMap.put(cellattr, formulaVal+","+key);
					}else{
						formulaMap.put(cellattr, key);
					}
				}
			}catch(Exception e){
				super.writeLog(e);
			}
		}
		return formulaMap;
	}
	
	/**
	 * 根据行、列得出对应的单元格
	 */
	private String getCellAttr(int rowid,int colid){
		String cellattr="";
		if(colid<26){
			char colname = (char)(65+colid);
			cellattr += String.valueOf(colname);
		}else if(colid>=26 && colid<676){
			char firstChar = (char)(65+(colid/26-1));
			cellattr += String.valueOf(firstChar);
			char nextChar = (char)(65+colid%26);
			cellattr += String.valueOf(nextChar);
		}
		cellattr += (rowid+1);
		return cellattr;
	}
	
	/**
	 * 计算百分比宽度
	 */
	private JSONObject countPercentWidth(JSONObject widths,int fullPercent) throws JSONException{
		double totalPercent=0.0;	//总占的百分比
		double totalPx=0.0;			//总占的PX宽
		for(int i=0;i<widths.length();i++){
			String curwidth=widths.getString("col_"+i);
			if(curwidth.indexOf("%")>-1){
				totalPercent += Double.parseDouble(curwidth.replace("%", ""));
			}else{
				totalPx += Double.parseDouble(curwidth);
			}
		}
		if(totalPercent>=fullPercent||totalPx==0)	return widths;
		
		JSONObject perwidths=new JSONObject();
		for(int i=0;i<widths.length();i++){
			String curwidth=widths.getString("col_"+i);
			if(curwidth.indexOf("%")>-1){		//已经是百分比
				perwidths.put("col_"+i, curwidth);
			}else{		//原来是px,则算成百分比
				double perwidth = (Float.parseFloat(curwidth)/totalPx)*(fullPercent-totalPercent);
				perwidth = this.round(perwidth, 2, BigDecimal.ROUND_DOWN);
				perwidths.put("col_"+i, perwidth+"%");
			}
		}
		return perwidths;
	}
	
	/**
	 * 计算单个TD宽度
	 */
	private float countTdWidth(JSONObject widths,int colid,int colspan) throws JSONException{
		float tdwidth = 0;
		while(colspan>0){
			colspan--;
			String curwidth=widths.getString("col_"+(colid+colspan));
			if(curwidth.indexOf("%")>-1){
				return -1;
			}else{
				tdwidth += Float.parseFloat(curwidth);
			}
		}
		return tdwidth;
	}
	
	/**
	 * 字号pt对应需要的单行文本px高度
	 */
	private int fontsize_compare_height(int fontsize){
		if(fontsize <= 10){
			return 24;
		}else if(fontsize <= 12){
			return 26;
		}else if(fontsize <= 24){
			return fontsize*2;
		}else if(fontsize <= 26){
			return 50;
		}else if(fontsize <= 28){
			return 52;
		}else if(fontsize <= 32){
			return 58;
		}else if(fontsize <= 48){
			return 82;
		}else{
			return 108;
		}
	}
	
	/**
	 * 计算double精度
	 * @param value		原值
	 * @param scale		小数位数
	 * @param roundingMode	BigDecimal.ROUND_UP(最后位+1)、BigDecimal.ROUND_DOWN(舍弃后面)、BigDecimal.ROUND_HALF_UP(四舍五入)
	 * @return
	 */
	private double round(double value,int scale,int roundingMode){
		BigDecimal bd=new BigDecimal(value);
		bd=bd.setScale(scale,roundingMode);
		double d=bd.doubleValue();
		bd=null;
		return d;
	}
	
	/**
	 * 针对存为新版后，模板内JSON节点ID为老版本节点ID，历史数据处理
	 */
	private String manageNodeid(String curnodeid){
		try{
			String sql = " select nodeid from workflow_flownode where workflowid="+wfid+" and nodeid="+curnodeid;
			rs.executeSql(sql);
			if(!rs.next()){
				List<String> allNodes = WorkflowVersion.getChildrenNodeListByNodeID(curnodeid);
				String allNodesStr = "";
				for(String node: allNodes){
					allNodesStr += node+",";
				}
				if(allNodesStr.endsWith(","))
					allNodesStr = allNodesStr.substring(0, allNodesStr.length()-1);
				if(!"".equals(allNodesStr)){
					sql = " select nodeid from workflow_flownode where workflowid="+wfid+" and nodeid in ("+allNodesStr+") ";
					rs.executeSql(sql);
					if(rs.next())
						return Util.null2String(rs.getString("nodeid"));
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return curnodeid;
	}
	
	
	public StringBuilder getTempHtml() {
		return tempHtml;
	}

	public StringBuilder getTempCss() {
		return tempCss;
	}

	public StringBuilder getTempScript() {
		return tempScript;
	}
	
}
