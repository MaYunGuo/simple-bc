package com.simple.bc.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.simple.bc.core.redis.IRedisManager;
import com.simple.bc.trx.DataStructIA;
import com.simple.bc.trx.RtnObj;
import com.simple.bc.trx.ToolInfo;
import com.simple.bc.util.ExcelUtil;
import com.simple.bc.util.JacksonUtil;
import com.simple.bc.util.ProperUtil;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static com.simple.bc.commdef.GenericDef.REDIS_KEY_FORMAT;

@RestController
public class SendMsgController {

    @Autowired
    private IRedisManager redisManager;

    @RequestMapping("/getData.do")
    public String getPlcData(String tool_id,  String[] data_groups){
        List<DataStructIA> oary = new ArrayList<>();
        if(data_groups != null && data_groups.length > 0){
             for(String data_group : data_groups){
                 DataStructIA dataStructIA = (DataStructIA) redisManager.getMapFromRedis(REDIS_KEY_FORMAT, tool_id, data_group);
                 oary.add(dataStructIA);
             }
        }else {
            Map<Object, Object> dataStructMap = redisManager.getMapFromRedis(REDIS_KEY_FORMAT, tool_id);
            for (Object key : dataStructMap.keySet()) {
                DataStructIA dataStructIA = (DataStructIA) dataStructMap.get(key);
                oary.add(dataStructIA);
            }
        }

        Collections.sort(oary, new Comparator<DataStructIA>() {
            public int compare(DataStructIA item1, DataStructIA item2) {
                return item1.getId().compareTo(item2.getId());
            }
        });

        RtnObj rtnObj = new RtnObj();
        rtnObj.setRtn_code("0000000");
        rtnObj.setRtn_mesg("SUCCESS");
        rtnObj.setOary(oary);
        return JacksonUtil.toJSONStr(rtnObj);
    }

    @RequestMapping("/getTool.do")
    public String getTools(String data_cate){

        List<ToolInfo> oary = new ArrayList<>();
        JSONObject jsonObject = ProperUtil.getToolInfo();
        for(String tool_id : jsonObject.keySet()){
            ToolInfo toolInfo = new ToolInfo();
            toolInfo.setTool_id(tool_id);
            toolInfo.setTool_dsc(jsonObject.getString(tool_id));
            oary.add(toolInfo);
        }
        RtnObj rtnObj = new RtnObj();
        rtnObj.setRtn_code("0000000");
        rtnObj.setRtn_mesg("SUCCESS");
        rtnObj.setOary(oary);
        return JacksonUtil.toJSONStr(rtnObj);
    }
    @RequestMapping("/getDataName.do")
    public String getDataName(String tool_id){
        RtnObj rtnObj = new RtnObj();

        InputStream inputStream = null;
        try {
            inputStream  = new FileInputStream("/opt/simple-bc/excel/"+ tool_id +".xlsx");
            Workbook workbook = new XSSFWorkbook(inputStream);
            List<String[]>  excelData = ExcelUtil.getExcelData(workbook);
            workbook.close();
            if(excelData == null || excelData.isEmpty()){
                rtnObj.setRtn_code("1234567");
                rtnObj.setRtn_mesg("没有找打设备[" + tool_id +"]的点表信息");
                return JacksonUtil.toJSONStr(rtnObj);
            }

            List<DataStructIA> oary = new ArrayList<>();
            for(String[] strings : excelData){
                DataStructIA dataStructIA = new DataStructIA();
                dataStructIA.setVariableName(strings[2]);
                oary.add(dataStructIA);
            }
            rtnObj.setRtn_code("0000000");
            rtnObj.setRtn_mesg("SUCCESS");
            rtnObj.setOary(oary);
        } catch (Exception e) {
            e.printStackTrace();
            rtnObj.setRtn_code("1234567");
            rtnObj.setRtn_mesg("没有找打设备[" + tool_id +"]的点表信息");
        }
        return JacksonUtil.toJSONStr(rtnObj);
    }
    @RequestMapping("/exportData.do")
    public void exportData(HttpServletRequest request, HttpServletResponse response){
        String tool_id = null;
        String [] data_groups = null;


        String dataStr = request.getParameter("data");
        JSONObject jsonObject = JSONObject.parseObject(dataStr);
        tool_id = jsonObject.getString("tool_id");
        JSONArray jsonArray = jsonObject.getJSONArray("iary");
        if(jsonArray != null && jsonArray.size() > 0 ){
            data_groups = new String[jsonArray.size()];
            for(int i=0;i<jsonArray.size();i++){
                JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
                data_groups[i] = jsonObject1.getString("data_name");

            }
        }
        List<DataStructIA> oary = new ArrayList<>();

        if(data_groups != null && data_groups.length > 0){
            for(String data_group : data_groups){
                DataStructIA dataStructIA = (DataStructIA) redisManager.getMapFromRedis(REDIS_KEY_FORMAT, tool_id, data_group);
                oary.add(dataStructIA);
            }
        }else {
            Map<Object, Object> dataStructMap = redisManager.getMapFromRedis(REDIS_KEY_FORMAT, tool_id);
            for (Object key : dataStructMap.keySet()) {
                DataStructIA dataStructIA = (DataStructIA) dataStructMap.get(key);
                oary.add(dataStructIA);
            }
        }

        Collections.sort(oary, new Comparator<DataStructIA>() {
            public int compare(DataStructIA item1, DataStructIA item2) {
                return item1.getId().compareTo(item2.getId());
            }
        });


        response.setContentType("text/html;charset=utf-8");
        String[] excelHead ={"ID","地址位","名称","数据类型","数据值","Quality","更新次数","主设备","子设备","更新时间"};
        response.setContentType("text/html;charset=utf-8");
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();
        for(int j =0; j < excelHead.length; j++){
            sheet.setColumnWidth(j,5500);
        }

        HSSFFont font = workbook.createFont();
        font.setFontName("宋体");
        font.setBold(true);
        font.setFontHeightInPoints((short) 15);//设置字体大小

        HSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFont(font);
        cellStyle.setAlignment(HorizontalAlignment.CENTER_SELECTION);

        HSSFRow head = sheet.createRow(0);
        for(int i=0;i<excelHead.length;i++){
            HSSFCell  cell = head.createCell(i);
            cell.setCellValue(excelHead[i]);
            cell.setCellStyle(cellStyle);
        }


        for (int i = 0; i < oary.size(); i++) {
            HSSFRow row = sheet.createRow(i+1);
            row.createCell(0).setCellValue(oary.get(i).getId());
            row.createCell(1).setCellValue(oary.get(i).getItem_ID());
            row.createCell(2).setCellValue(oary.get(i).getVariableName());
            row.createCell(3).setCellValue(oary.get(i).getData_Type());
            row.createCell(4).setCellValue(oary.get(i).getValue());
            row.createCell(5).setCellValue(oary.get(i).getQuality());
            row.createCell(6).setCellValue(oary.get(i).getUpdate_Count());
            row.createCell(7).setCellValue(oary.get(i).getEqt());
            row.createCell(8).setCellValue(oary.get(i).getSubEqt());
            row.createCell(9).setCellValue(oary.get(i).getTimeStamp().toString().substring(0,19));
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            workbook.write(baos);
            baos.flush();
            baos.close();
            response.reset();
            response.addHeader("Content-Disposition", "attachment;filename=" + System.currentTimeMillis() + ".xls");
            response.addHeader("Content-Length", String.valueOf(baos.size()));
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.getOutputStream().write(baos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
