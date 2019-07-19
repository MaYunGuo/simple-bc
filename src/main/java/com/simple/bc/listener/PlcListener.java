package com.simple.bc.listener;

import com.alibaba.fastjson.JSONObject;
import com.simple.bc.service.DataAcqService;
import com.simple.bc.util.*;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import static com.simple.bc.commdef.GenericDef.*;

@Component
@Order(value = 1)
public class PlcListener implements ApplicationRunner {

    private  LogUtils logUtils;

    @Autowired
    private DataAcqService dataAcqService;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        AppContext.clear();
        AppContext.setCurrServiceName(DataAcqService.class.getSimpleName());
        logUtils = new LogUtils(PlcListener.class);

        JSONObject jsonObject = ProperUtil.getToolInfo();
        for(String tool_id : jsonObject.keySet()){
            String evt_no = GUIDGenerator.javaGUID();
            AppContext.setCurrEventNumber(evt_no);
            logUtils.info("设备:[" + tool_id +"]，开始解析点表.............");
            InputStream inputStream  = null;
            Workbook workbook = null;
            try {
                inputStream  = new FileInputStream("/opt/simple-bc/excel/"+ tool_id +".xlsx");
                workbook = new XSSFWorkbook(inputStream);
                List<String[]> excelData = ExcelUtil.getExcelData(workbook);
                if(excelData != null && !excelData.isEmpty()) {
                    String plc_ip = ProperUtil.getToolConf(tool_id, TOOL_CONF_KEY_PLC_IP);
                    String plc_pt = ProperUtil.getToolConf(tool_id, TOOL_CONF_KEY_PLC_PT);
                    String fab_id = ProperUtil.getToolConf(tool_id, TOOL_CONF_KEY_FAB_ID);
                    String bay_id = ProperUtil.getToolConf(tool_id, TOOL_CONF_KEY_BAY_ID);
                    String ope_id = ProperUtil.getToolConf(tool_id, TOOL_CONF_KEY_OPE_ID);
                    String mq_name = ProperUtil.getToolConf(tool_id, TOOL_CONF_KEY_MP_NAME);
                    dataAcqService.getPlcData(evt_no, plc_ip, Integer.valueOf(plc_pt), tool_id, fab_id, bay_id, ope_id, mq_name, excelData);
                    Thread.sleep(2000);
                }
            } catch (Exception e) {
                logUtils.info("设备:[" + tool_id +"]，解析点表发生异常，原因:["+ StringUtil.stackTraceToString(e)+" ]");
            }finally {
                workbook.close();
                inputStream.close();
            }
            logUtils.info("设备:[" + tool_id +"]，结束解析点表.............");
        }
    }
}
