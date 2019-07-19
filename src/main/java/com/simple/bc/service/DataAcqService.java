package com.simple.bc.service;

import com.simple.bc.core.redis.IRedisManager;
import com.simple.bc.trx.DataStructI;
import com.simple.bc.trx.DataStructIA;
import com.simple.bc.util.*;
import com.totainfo.result.OperateResult;
import com.totainfo.result.OperateResultExOne;
import com.totainfo.siemens.SiemensS7Net;
import com.totainfo.siemens.def.SiemensPLCS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import static com.simple.bc.commdef.GenericDef.REDIS_KEY_FORMAT;
import static com.simple.bc.commdef.GenericDef._SPACE;

@Service
public class DataAcqService {

    private LogUtils logUtils;

    @Autowired
    private AmqpTemplate rabbitTemplate;

    @Autowired
    private IRedisManager redisManager;


    @Async("taskExecutor")
    public void getPlcData(String evt_no, String plc_id, int plc_port, String main_tool, String fab_id, String line_id, String ope_id, String mq_name, List<String[]> confData){

        AppContext.clear();
        AppContext.setCurrEventNumber(evt_no);
        AppContext.setCurrServiceName(DataAcqService.class.getSimpleName());
        logUtils = new LogUtils(DataAcqService.class);


        logUtils.info("设备:[" + main_tool +"]，开始监控PLC:[" + plc_id +"][" + plc_port +"]的数据");


        SiemensS7Net siemensS7Net = new SiemensS7Net(SiemensPLCS.S1200, plc_id, plc_port);
        OperateResult operateResult = siemensS7Net.connectServer();
        while (true) {
            try {
                if (!operateResult.isSuccess) {
                    logUtils.info("设备:[" + main_tool + "]，开始监控PLC:[" + plc_id + "][" + plc_port + "]的数据, PLC连接失败，开始重连....");
                    operateResult = siemensS7Net.connectServer();
                } else {
                    OperateResultExOne dataCont = null;
                    boolean endMQflg = false;
                    String dataVal;
                    DataStructI dataStructI = new DataStructI();
                    dataStructI.setFab(fab_id);
                    dataStructI.setLine(line_id);
                    dataStructI.setOperation(ope_id);
                    List<DataStructIA> dataList = new ArrayList<>();
                    for (int i = 0; i < confData.size(); i++) {
                        String[] strings = confData.get(i);
                        DataStructIA dataStructIA = (DataStructIA) redisManager.getMapFromRedis(REDIS_KEY_FORMAT, main_tool, strings[2]);
                        if (dataStructIA == null) {
                            dataStructIA = new DataStructIA();
                            dataStructIA.setId(Integer.valueOf(strings[0]));
                            dataStructIA.setItem_ID(strings[1]);
                            dataStructIA.setVariableName(strings[2]);
                            dataStructIA.setData_Type(strings[3]);
                            dataStructIA.setUpdate_Count(0);
                            dataStructIA.setValue("");
                            dataStructIA.setEqt(strings[4]);
                            dataStructIA.setSubEqt(strings[5]);
                            dataStructIA.setTimeStamp(DateUtil.getCurrentTimestamp());
                        }
                        dataStructIA.setQuality("Bad");

                        dataVal = _SPACE;
                        if ("Short".equals(strings[3])) {
                            dataCont = siemensS7Net.readInt16(strings[1]);
                        }else if("Float".equals(strings[3])){
                            dataCont = siemensS7Net.readFloat(strings[1]);
                        }else if("Double".equals(strings[3])){
                            dataCont = siemensS7Net.readInt32(strings[1]);
                        }else if("Boolean".equals(strings[3])){
                            dataCont = siemensS7Net.ReadBool(strings[1]);
                        }
                        if(dataCont == null || !dataCont.isSuccess){
                            logUtils.info("设备:[" + main_tool + "]，开始监控PLC:[" + plc_id + "][" + plc_port + "]的数据,参数：["+ strings[2] +"]获取PLC数据失败");
                        }else{
                            dataStructIA.setQuality("Good");
                            if("Boolean".equals(strings[3])){
                                dataVal = (Boolean) dataCont.content ? "1":"0";
                            }else {
                                dataVal = String.valueOf(dataCont.content);
                            }
                            if (!dataStructIA.getValue().equals(dataVal)) {
                                dataStructIA.setUpdate_Count(dataStructIA.getUpdate_Count() < Integer.MAX_VALUE ? dataStructIA.getUpdate_Count() + 1 : 1);
                                dataStructIA.setValue(dataVal);
                                dataStructIA.setTimeStamp(DateUtil.getCurrentTimestamp());
                                if (!endMQflg) {
                                    endMQflg = true;
                                }
                            }
                            dataList.add(dataStructIA);
                            redisManager.setToMapRedis(REDIS_KEY_FORMAT, main_tool, strings[2], dataStructIA);
                        }
                    }
                    if (endMQflg) {
                        logUtils.info("设备:[" + main_tool + "]，开始监控PLC:[" + plc_id + "][" + plc_port + "]的数据,数据有改变，开始抛送MQ:[" + mq_name + "]");
                        dataStructI.setRpt_time(DateUtil.getCurrentTimestamp());
                        dataStructI.setDataList(dataList);
                        rabbitTemplate.convertAndSend(mq_name, MessageBuilder.withBody(JacksonUtil.toJSONStr(dataStructI).getBytes("UTF-8")).build());
                    }
                }
            }catch(Exception e){
                logUtils.info("设备:[" + main_tool + "]，开始监控PLC:[" + plc_id + "][" + plc_port + "]的数据， 发生异常，原因[" + StringUtil.stackTraceToString(e) + "]");
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
