package api.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import format.RespWrapper;
import util.DBUtil;
import util.RequestParser;


@WebServlet(name = "TemplateServlet",urlPatterns = {"/api/datacrawling/task/template/*"})
public class TemplateServlet extends HttpServlet {
	/*
	for api: /api/datacrawling/task/template/all
	for api: /api/datacrawling/task/template/:id
	 */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String[] pathParam= RequestParser.parsePath(request.getRequestURI(),2);
		Map<String,Object> data=new HashMap<>();

		String [] templateParams={"id", "patternName"};
		String [] param={"webId", "webName"};
		List<Map<String,Object>> dataList=new ArrayList<Map<String,Object>>();
		if("template".equals(pathParam[0]) && "all".equals(pathParam[1])){//for task/template/all
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			try{
				String[][] res=util.DBUtil.select("website", param);
				for(int i=0;i<res.length;i++){
					//use webId to search in pattern
					String[][] template=util.DBUtil.select("pattern", templateParams, Integer.parseInt(res[i][0]));
					if(template.length>0){
						for(int j=0;j<template.length;j++){
							Map<String,Object> content=new HashMap<>();
							content.put("taskID", res[i][0]);
							content.put("taskName", res[i][1]);
							content.put("templateID",template[j][0]);
							content.put("templateName",template[j][1]);
							dataList.add(content);
						}
					}
				}
				response.getWriter().println(RespWrapper.build(dataList,dataList.size()));
			}catch(Exception e){
				data.put("msg","模板参数获取失败");
				response.getWriter().println(RespWrapper.build(RespWrapper.AnsMode.SYSERROR,data));
			}
		} else if("template".equals(pathParam[0])){//for task/template/:id
			int templateID = 0;
			try{
				templateID=Integer.parseInt(pathParam[1]);
			}catch (NumberFormatException e){
				data.put("msg", "id参数解析错误");
				response.getWriter().println(RespWrapper.build(RespWrapper.AnsMode.SYSERROR, data));
				return;
			}
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			try{
				String[][] template=util.DBUtil.select("pattern", new String[]{"webId", "patternName", "xpath"}, new String[]{"id"}, new String[]{templateID + ""});
				Map<String, Object> content = new HashMap<>();
				if(template != null && template.length>0) {
					String[] res = util.DBUtil.select("website", param, Integer.parseInt(template[0][0]))[0];
					content.put("taskID", res[0]);
					content.put("taskName", res[1]);
					content.put("templateName", template[0][1]);
					content.put("templateXpath",template[0][2]);
					content.put("templateID", templateID);
				}
				response.getWriter().println(RespWrapper.build(content));
			}catch(Exception e){
				data.put("msg","模板参数获取失败");
				response.getWriter().println(RespWrapper.build(RespWrapper.AnsMode.SYSERROR,data));
			}
		} else{
			response.getWriter().println(RespWrapper.build(RespWrapper.AnsMode.SYSERROR,data));
		}
    }


    /*
    for api: /api/datacrawling/task/template/:id
	for api: /api/datacrawling/task/template/new
     */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		String[] pathParam=RequestParser.parsePath(request.getRequestURI(),2);
		int templateID = 0;
		int webId = 0;
		Map<String,Object> data=new HashMap<>();
		if("template".equals(pathParam[0])){
		 	if(!"new".equals(pathParam[1])){//for task/template/:id
				try{
					templateID = Integer.parseInt(pathParam[1]);
				}catch (NumberFormatException e){
					response.getWriter().println(RespWrapper.build(RespWrapper.AnsMode.SYSERROR,null));
					return;
				}
			}
			webId = Integer.parseInt(request.getParameter("taskID"));

			//前端页面保证这些值均不为空，这里无需验证
			String templateName=request.getParameter("templateName");
			String templateXpath=request.getParameter("templateXpath");
			String[] p = {"patternName", "webId"};
			String[] pv = {templateName, webId + ""};

			String[] par = {"id"};
			String[] parValue = {templateID + ""};

			String[] params = {"webId","patternName","xpath"};
			String[] paramsValue = {webId+"", templateName, templateXpath};

			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			data.put("taskID",webId);
			data.put("templateName",templateName);
			data.put("templateXpath",templateXpath);

			if("new".equals(pathParam[1])) {//for task/template/new
				 if(DBUtil.select("pattern", par,p, pv).length != 0) {
					 data.put("msg", "该模板名称已使用，请重新输入");
					 response.getWriter().println(RespWrapper.build(RespWrapper.AnsMode.SYSERROR,data));
				 } else {
				 	DBUtil.insert("pattern", params, paramsValue);
					response.getWriter().println(RespWrapper.build(data));
				}
			} else if(DBUtil.update("pattern", params, paramsValue,par,parValue)) {//for task/template/:id
				 response.getWriter().println(RespWrapper.build(data));
			} else {
				data.clear();
				data.put("msg","操作失败");
				response.getWriter().println(RespWrapper.build(RespWrapper.AnsMode.SYSERROR, data));
			}
		}else {
			response.getWriter().println(RespWrapper.build(RespWrapper.AnsMode.SYSERROR, data));
		}

	}
}
