package weaver.sysinterface;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import weaver.conn.RecordSet;
import weaver.general.Util;
import weaver.hrm.User;

import com.weaver.formmodel.mobile.manager.MobileUserInit;

public class JurisdictionJudgeNew extends HttpServlet{
	private static final long serialVersionUID = 1L;

	private HttpServletRequest request;
	
	private HttpServletResponse response;
	
	public void doGet(HttpServletRequest request, HttpServletResponse response){    
		this.request = request;
		this.response = response;
		try {
			User user = MobileUserInit.getUser(request, response);
			if(user == null){
				JSONObject checkUser = new JSONObject();
				checkUser.put("msgstatus", "0");
				checkUser.put("msg", "服务器端重置了登录信息，请重新登录");
				response.setContentType("application/json; charset=utf-8");  
				response.getWriter().print(checkUser.toString());
				return;
			}
			String userid = String.valueOf(user.getUID());
			
			RecordSet rs = new RecordSet();
			String roleSql="select top 1 * from hrmrolemembers where resourceid="+userid+" and (roleid=146 or roleid=147 or roleid=2) order by roleid asc";
			System.out.println(roleSql);
			rs.executeSql(roleSql);
			if(rs.next()){
				if(rs.getInt("roleid")==146 || rs.getInt("roleid")==2){//spm_供应商管理员/系统管理员
						try {
							String tableStr="<dl class=\"daohang-item\" data-name=\"导航\" style=\"height: 30px;margin-top:10px;\">"+
							"<dt class=\"am-accordion-title\">"+
							"<img src=\"img/dh.png\" style=\"height: 28px;margin-bottom: 5px;\" alt=\"\" >"+
					    		"<a style=\"font-size:24px;color:#d44632;line-height: 30px;\">导航</a>"+
					        "</dt>"+		
						"</dl>"+
						"<dl class=\"am-accordion-item\" data-name=\"供应商管理\">"+
						"<dt class=\"am-accordion-title actived\">"+
				    		"<img class=\"img1\" src=\"img/col01.png\" alt=\"\" height=\"19\" style=\"display: none;\"><img class=\"img2\" src=\"img/col11.png\" alt=\"\" height=\"19\" style=\"\">供应商管理"+
				        "</dt>"+
						"<dd class=\"am-accordion-bd am-collapse\" style=\"height: 0px;\">"+
							"<div class=\"am-accordion-content\">"+
								"<ul>"+
									"<li class=\"\"><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=77\" target=\"_blank\" data-name=\"\">我的供应商</a></li>"+
									"<li ><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=76\" target=\"_blank\" data-name=\"\">供应商库</a></li>"+
									"<li ><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=81\" target=\"_blank\" data-name=\"\">合同信息查询</a></li>	"+							
									"<li ><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=82\" target=\"_blank\" data-name=\"\">付款信息查询</a></li>	"+							
									"<li><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=80\" target=\"_blank\" data-name=\"\">合作信息查询</a></li>"+
									"<li ><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=121\" target=\"_blank\" data-name=\"\">黑名单</a></li>"+
									"<li style=\"border-bottom:0px\"><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=122\" target=\"_blank\" data-name=\"\">出库名单</a></li>"+															
								"</ul>"+
							"</div>"+
						"</dd>"+
					"</dl>"+
					"<dl class=\"am-accordion-item\" data-name=\"考评管理\">"+
						"<dt class=\"am-accordion-title actived\">"+
				    		"<img class=\"img1\" src=\"img/col02.png\" alt=\"\" height=\"19\" style=\"display: none;\"><img class=\"img2\" src=\"img/col12.png\" alt=\"\" height=\"19\" style=\"\">考评管理"+
				        "</dt>"+
						"<dd class=\"am-accordion-bd am-collapse\" style=\"height: 0px;\">"+
							"<div class=\"am-accordion-content\">"+
								"<ul>"+
									"<li class=\"\" style=\"border-bottom:0px\"><a class=\"a1\" data-id=\"1\" href=\"http://172.18.180.140:8075/WebReport/ReportServer?reportlet=SzxReport/Spm/供应商年度考评评分汇总表.cpt\" target=\"_blank\" data-name=\"\">考评查询</a></li>"+
									//"<li ><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=116\" target=\"_blank\" data-name=\"\">整改查询</a></li>"+
									//"<li style=\"border-bottom:0px\"><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=117\" target=\"_blank\" data-name=\"\">反馈查询</a></li>"+								
								"</ul>"+
							"</div>"+
						"</dd>"+
					"</dl>"+		
						"<dl class=\"am-accordion-item\" data-name=\"账号管理\">"+
							"<dt class=\"am-accordion-title actived\">"+
					    		"<img class=\"img1\" src=\"img/col03.png\" alt=\"\" height=\"19\" style=\"display: none;\"><img class=\"img2\" src=\"img/col13.png\" alt=\"\" height=\"19\" style=\"\">账号管理"+
					        "</dt>"+
							"<dd class=\"am-accordion-bd am-collapse\" style=\"height: 0px;\">"+
								"<div class=\"am-accordion-content\">"+
									"<ul>"+
										"<li class=\"\" style=\"border-bottom:0px\"><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=120\" target=\"_blank\" data-name=\"\">账号设置</a></li>"+								
									"</ul>"+
								"</div>"+
							"</dd>"+
						"</dl>"+
						"<dl class=\"am-accordion-item\" data-name=\"统计报表\" id=\"bpgl\">"+
							"<dt class=\"am-accordion-title actived\">"+
					    		"<img class=\"img1\" src=\"img/col05.png\" alt=\"\" height=\"19\" style=\"display: none;\"><img class=\"img2\" src=\"img/col15.png\" alt=\"\" height=\"19\" style=\"\">统计报表"+
					        "</dt>"+
							"<dd class=\"am-accordion-bd am-collapse\" style=\"height: 0px;\">"+
								
								"<div class=\"am-accordion-content\">"+
									"<ul>"+
										"<li class=\"\"><a class=\"a1\" data-id=\"1\" href=\"http://172.18.180.140:8075/WebReport/ReportServer?reportlet=SzxReport/Spm/供应商采购方式分布图.cpt\" target=\"_blank\" data-name=\"\">采购方式分布</a></li>"+
										"<li><a class=\"a1\" data-id=\"1\" href=\"http://172.18.180.140:8075/WebReport/ReportServer?reportlet=SzxReport/Spm/各部门年度采购情况分布图.cpt\" target=\"_blank\" data-name=\"\">部门年度采购分布</a></li>"+
										"<li><a class=\"a1\" data-id=\"1\" href=\"http://172.18.180.140:8075/WebReport/ReportServer?reportlet=SzxReport/Spm/供应商年度合同金额图.cpt\" target=\"_blank\" data-name=\"\">年度合同金额</a></li>"+
										"<li><a class=\"a1\" data-id=\"1\" href=\"http://172.18.180.140:8075/WebReport/ReportServer?reportlet=SzxReport/Spm/历年采购金额对比图.cpt\" target=\"_blank\" data-name=\"\">历年采购金额对比</a></li>"+
										"<li style=\"border-bottom:0px\"><a class=\"a1\" data-id=\"1\" href=\"http://172.18.180.140:8075/WebReport/ReportServer?reportlet=SzxReport/Spm/项目属性分布图.cpt\" target=\"_blank\" data-name=\"\">项目属性分布</a></li>"+                         								
									"</ul>"+
								"</div>"+
							"</dd>"+
						"</dl>"+	
									"<dl class=\"am-accordion-item\" data-name=\"设置\">"+
									"<dt class=\"am-accordion-title actived\">"+
							    		"<img class=\"img1\" src=\"img/col05.png\" alt=\"\" height=\"19\" style=\"display: none;\"><img class=\"img2\" src=\"img/col15.png\" alt=\"\" height=\"19\" style=\"\">设置"+
							        "</dt>"+
									"<dd class=\"am-accordion-bd am-collapse\" style=\"height: 0px;\">"+
										"<div class=\"am-accordion-content\">"+
											"<ul>"+
											"<li class=\"\"><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=72\" target=\"_blank\" data-name=\"\">供应商一级分类</a></li>"+
											"<li class=\"\"><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=73\" target=\"_blank\" data-name=\"\">供应商二级分类</a></li>"+
											"<li class=\"\"><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=75\" target=\"_blank\" data-name=\"\">黑名单情形</a></li>"+
											"<li style=\"border-bottom:0px\" ><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=74\" target=\"_blank\" data-name=\"\">供应商状态</a></li>"+					                              								
											"</ul>"+
										"</div>"+
									"</dd>"+
								"</dl>";
						
							String tableStr2="<div class=\"yewu-item\">"+
						"<div class=\"yewu-header\">供应商&nbsp; <i class=\"am-icon am-icon-chevron-right\"></i></div>"+
							"<ul>"+
								"<li><a>|</a><a href=\"/formmode/search/CustomSearchBySimple.jsp?customid=76\" target=\"_blank\">供应商库</a></li>"+
								"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=391\" target=\"_blank\">供应商入库</a></li>"+
								"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=392\" target=\"_blank\">供应商基本信息变更</a></li>"+
								"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=393\" target=\"_blank\">供应商黑名单</a></li>"+
								"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=394\" target=\"_blank\">供应商出库</a></li>"+
							"</ul>"+
						"</div>"+
						"<div class=\"yewu-item\">"+
							"<div class=\"yewu-header\">考评&nbsp;&nbsp;&nbsp;&nbsp; <i class=\"am-icon am-icon-chevron-right\"></i></div>"+
							"<ul>"+
								"<li><a>|</a><a href=\"http://172.18.180.140:8075/WebReport/ReportServer?reportlet=SzxReport/Spm/供应商年度考评评分汇总表.cpt\" target=\"_blank\">供应商考评查询</a></li>"+
								"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=395\" target=\"_blank\">发起供应商考评</a></li>"+								
								
							"</ul>"+
						"</div>"+
						"<div class=\"yewu-item\">"+
							"<div class=\"yewu-header\">统计&nbsp;&nbsp;&nbsp;&nbsp; <i class=\"am-icon am-icon-chevron-right\"></i></div>"+
							"<ul>"+								
								"<li class=\"firisItem\"><a>|</a><a href=\"http://172.18.180.140:8075/WebReport/ReportServer?reportlet=SzxReport/Spm/供应商采购方式分布图.cpt\" target=\"_blank\">采购方式分布</a></li>"+
								"<li><a>|</a><a href=\"http://172.18.180.140:8075/WebReport/ReportServer?reportlet=SzxReport/Spm/各部门年度采购情况分布图.cpt\" target=\"_blank\">部门年度采购分布</a></li>"+
								"<li><a>|</a><a href=\"http://172.18.180.140:8075/WebReport/ReportServer?reportlet=SzxReport/Spm/供应商年度合同金额图.cpt\" target=\"_blank\">年度合同金额</a></li>"+	
								"<li><a>|</a><a href=\"http://172.18.180.140:8075/WebReport/ReportServer?reportlet=SzxReport/Spm/历年采购金额对比图.cpt\" target=\"_blank\">历年采购金额对比</a></li>"+	
								"<li><a>|</a><a href=\"http://172.18.180.140:8075/WebReport/ReportServer?reportlet=SzxReport/Spm/项目属性分布图.cpt\" target=\"_blank\">项目属性分布</a></li>"+									
							"</ul>"+
						"</div>";
							JSONArray jsonArray = new JSONArray();
							JSONObject jsonobject=new JSONObject();
							jsonobject.put("tableStr", tableStr);
							JSONObject jsonobject2=new JSONObject();
							jsonobject2.put("tableStr2", tableStr2);
							jsonArray.add(jsonobject);
							jsonArray.add(jsonobject2);
								response.setContentType("application/json; charset=utf-8");  
								response.getWriter().print(jsonArray);
						} catch (Exception e) {
							e.printStackTrace();
						}
						
					}else if(rs.getInt("roleid")==147){//spm_采购分管领导						
						try {
							String tableStr="<dl class=\"daohang-item\" data-name=\"导航\" style=\"height: 30px;margin-top:10px;\">"+
									"<dt class=\"am-accordion-title\">"+
									"<img src=\"img/dh.png\" style=\"height: 28px;margin-bottom: 5px;\" alt=\"\" >"+
							    		"<a style=\"font-size:24px;color:#d44632;line-height: 30px;\">导航</a>"+
							        "</dt>"+		
								"</dl>"+
								"<dl class=\"am-accordion-item\" data-name=\"供应商管理\">"+
								"<dt class=\"am-accordion-title actived\">"+
						    		"<img class=\"img1\" src=\"img/col01.png\" alt=\"\" height=\"19\" style=\"display: none;\"><img class=\"img2\" src=\"img/col11.png\" alt=\"\" height=\"19\" style=\"\">供应商管理"+
						        "</dt>"+
								"<dd class=\"am-accordion-bd am-collapse\" style=\"height: 0px;\">"+
								"<!-- 规避 Collapase 处理有 padding 的折叠内容计算计算有误问题， 加一个容器 -->"+
									"<div class=\"am-accordion-content\">"+
										"<ul>"+											
											"<li class=\"\"><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=76\" target=\"_blank\" data-name=\"\">供应商库</a></li>"+
											"<li ><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=121\" target=\"_blank\" data-name=\"\">黑名单</a></li>"+
											"<li style=\"border-bottom:0px\"><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=122\" target=\"_blank\" data-name=\"\">出库名单</a></li>"+															
										"</ul>"+
									"</div>"+
								"</dd>"+
							"</dl>"+
							"<dl class=\"am-accordion-item\" data-name=\"统计报表\" id=\"bpgl\">"+
							"<dt class=\"am-accordion-title actived\">"+
					    		"<img class=\"img1\" src=\"img/col05.png\" alt=\"\" height=\"19\" style=\"display: none;\"><img class=\"img2\" src=\"img/col15.png\" alt=\"\" height=\"19\" style=\"\">统计报表"+
					        "</dt>"+
							"<dd class=\"am-accordion-bd am-collapse\" style=\"height: 0px;\">"+
								
								"<div class=\"am-accordion-content\">"+
									"<ul>"+
										"<li class=\"\"><a class=\"a1\" data-id=\"1\" href=\"http://172.18.180.140:8075/WebReport/ReportServer?reportlet=SzxReport/Spm/供应商采购方式分布图.cpt\" target=\"_blank\" data-name=\"\">采购方式分布</a></li>"+
										"<li><a class=\"a1\" data-id=\"1\" href=\"http://172.18.180.140:8075/WebReport/ReportServer?reportlet=SzxReport/Spm/各部门年度采购情况分布图.cpt\" target=\"_blank\" data-name=\"\">部门年度采购分布</a></li>"+
										"<li><a class=\"a1\" data-id=\"1\" href=\"http://172.18.180.140:8075/WebReport/ReportServer?reportlet=SzxReport/Spm/供应商年度合同金额图.cpt\" target=\"_blank\" data-name=\"\">年度合同金额</a></li>"+
										"<li><a class=\"a1\" data-id=\"1\" href=\"http://172.18.180.140:8075/WebReport/ReportServer?reportlet=SzxReport/Spm/历年采购金额对比图.cpt\" target=\"_blank\" data-name=\"\">历年采购金额对比</a></li>"+
										"<li style=\"border-bottom:0px\"><a class=\"a1\" data-id=\"1\" href=\"http://172.18.180.140:8075/WebReport/ReportServer?reportlet=SzxReport/Spm/项目属性分布图.cpt\" target=\"_blank\" data-name=\"\">项目属性分布</a></li>"+                          								
									"</ul>"+
								"</div>"+
							"</dd>"+
						"</dl>";
							
							String tableStr2="<div class=\"yewu-item\">"+
									"<div class=\"yewu-header\">供应商&nbsp; <i class=\"am-icon am-icon-chevron-right\"></i></div>"+
										"<ul>"+
											"<li><a>|</a><a href=\"/formmode/search/CustomSearchBySimple.jsp?customid=76\" target=\"_blank\">供应商库</a></li>"+
											"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=391\" target=\"_blank\">供应商入库</a></li>"+
											"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=392\" target=\"_blank\">供应商基本信息变更</a></li>"+
											"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=393\" target=\"_blank\">供应商黑名单</a></li>"+
											"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=394\" target=\"_blank\">供应商出库</a></li>"+
										"</ul>"+
									"</div>"+									
									"<div class=\"yewu-item\">"+
										"<div class=\"yewu-header\">统计&nbsp;&nbsp;&nbsp;&nbsp; <i class=\"am-icon am-icon-chevron-right\"></i></div>"+
										"<ul>"+
											"<li class=\"firisItem\"><a>|</a><a href=\"http://172.18.180.140:8075/WebReport/ReportServer?reportlet=SzxReport/Spm/供应商采购方式分布图.cpt\" target=\"_blank\">采购方式分布</a></li>"+								
										    "<li><a>|</a><a href=\"http://172.18.180.140:8075/WebReport/ReportServer?reportlet=SzxReport/Spm/各部门年度采购情况分布图.cpt\" target=\"_blank\">部门年度采购分布</a></li>"+
											"<li><a>|</a><a href=\"http://172.18.180.140:8075/WebReport/ReportServer?reportlet=SzxReport/Spm/供应商年度合同金额图.cpt\" target=\"_blank\">年度合同金额</a></li>"+
										    "<li><a>|</a><a href=\"http://172.18.180.140:8075/WebReport/ReportServer?reportlet=SzxReport/Spm/历年采购金额对比图.cpt\" target=\"_blank\">历年采购金额对比</a></li>"+
											"<li><a>|</a><a href=\"http://172.18.180.140:8075/WebReport/ReportServer?reportlet=SzxReport/Spm/项目属性分布图.cpt\" target=\"_blank\">项目属性分布</a></li>"+			
										"</ul>"+
									"</div>";
							JSONArray jsonArray = new JSONArray();
							JSONObject jsonobject=new JSONObject();
							jsonobject.put("tableStr", tableStr);
							JSONObject jsonobject2=new JSONObject();
							jsonobject2.put("tableStr2", tableStr2);
							jsonArray.add(jsonobject);
							jsonArray.add(jsonobject2);
								response.setContentType("application/json; charset=utf-8");  
								response.getWriter().print(jsonArray);
						} catch (Exception e) {
							e.printStackTrace();
						}
						
					}
				}else{ //普通员工
					try {
						String tableStr="<dl class=\"daohang-item\" data-name=\"导航\" style=\"height: 30px;margin-top:10px;\">"+
								"<dt class=\"am-accordion-title\">"+
								"<img src=\"img/dh.png\" style=\"height: 28px;margin-bottom: 5px;\" alt=\"\" >"+
						    		"<a style=\"font-size:24px;color:#d44632;line-height: 30px;\">导航</a>"+
						        "</dt>"+		
							"</dl>"+
							"<dl class=\"am-accordion-item\" data-name=\"供应商管理\">"+
							"<dt class=\"am-accordion-title actived\">"+
					    		"<img class=\"img1\" src=\"img/col01.png\" alt=\"\" height=\"19\" style=\"display: none;\"><img class=\"img2\" src=\"img/col11.png\" alt=\"\" height=\"19\" style=\"\">供应商管理"+
					        "</dt>"+
							"<dd class=\"am-accordion-bd am-collapse\" style=\"height: 0px;\">"+

								"<div class=\"am-accordion-content\">"+
									"<ul>"+
										"<li class=\"\"><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=77\" target=\"_blank\" data-name=\"\">我的供应商</a></li>"+
										"<li ><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=76\" target=\"_blank\" data-name=\"\">供应商库</a></li>"+
										"<li ><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=121\" target=\"_blank\" data-name=\"\">黑名单</a></li>"+
										"<li style=\"border-bottom:0px\"><a class=\"a1\" data-id=\"1\" href=\"/formmode/search/CustomSearchBySimple.jsp?customid=122\" target=\"_blank\" data-name=\"\">出库名单</a></li>"+															
									"</ul>"+
								"</div>"+
							"</dd>"+
						"</dl>";
					
						String tableStr2="<div class=\"yewu-item\">"+
								"<div class=\"yewu-header\">供应商&nbsp; <i class=\"am-icon am-icon-chevron-right\"></i></div>"+
									"<ul>"+
										"<li><a>|</a><a href=\"/formmode/search/CustomSearchBySimple.jsp?customid=76\" target=\"_blank\">供应商库</a></li>"+
										"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=391\" target=\"_blank\">供应商入库</a></li>"+
										"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=392\" target=\"_blank\">供应商基本信息变更</a></li>"+
										"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=393\" target=\"_blank\">供应商黑名单</a></li>"+
										"<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=394\" target=\"_blank\">供应商出库</a></li>"+
									"</ul>"+
								"</div>";
									JSONArray jsonArray = new JSONArray();
									JSONObject jsonobject=new JSONObject();
									jsonobject.put("tableStr", tableStr);
									JSONObject jsonobject2=new JSONObject();
									jsonobject2.put("tableStr2", tableStr2);
									jsonArray.add(jsonobject);
									jsonArray.add(jsonobject2);
										response.setContentType("application/json; charset=utf-8");  
										response.getWriter().print(jsonArray);
					} catch (Exception e) {
						e.printStackTrace();
					}
			}		
		  
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}