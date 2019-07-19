/**
 *
 */
$(document).ready(function () {
    $("form").submit(function () {
        return false;
    });

    var VAL = {
        EVT_USR: $("#userId").text(),
        FBPRETLOT : "FBPRETLOT",
        DISABLED_ATTR: {
            "disabled": true
        },
        ENABLED_ATTR: {
            "disabled": false
        }
    };

    var domObj = {
        W: $(window),
        $query_btn    : $("#query_btn"),
        $downlaod_btn : $("#downlaod_btn"),
        $toolIdSel    : $("#toolIdSel"),
        $dataNameSel  : $("#dataNameSel"),
        jgird :{
            $dataInfoDiv  : $("#dataInfoListDiv"),
            $dataInfoGrid : $("#dataInfoListGrd"),
            $dataInfoPag  : "#dataInfoListPg"
        },
    };

    var initFnc = {
        initTool :function () {
            var inObj ={
                data_cate: "TOOL"

            }
            var outObj = comUplaod("getTool.do", inObj);
            if(outObj.rtn_code == _NORMAL){
                SelectDom.addSelectArr(domObj.$toolIdSel, outObj.oary, "tool_id", "tool_dsc", "", true, false);
            }
        },
        initDataName :function () {
            var tool_id = domObj.$toolIdSel.find("option:selected").val();
            if(!tool_id){
                return false;
            }
            var inObj ={
                tool_id: tool_id

            }
            var outObj = comUplaod("getDataName.do", inObj);
            if(outObj.rtn_code == _NORMAL){
                SelectDom.addSelectArr(domObj.$dataNameSel, outObj.oary, "variableName", "variableName", "", true, false);
                domObj.jgird.$dataInfoGrid.jqGrid("clearGridData");
            }
        },

        initLotInfoGrd: function () {
            var itemInfoCM =
                [
                    {name: 'id',           index: 'id',            label: "ID",                  width: 180},
                    {name: 'item_ID',      index: 'item_ID',       label: DATA_ADDRESS_TAG,      width: 180},
                    {name: 'variableName', index: 'variableName',  label: DATA_GROUP_TAG,        width: 160},
                    {name: 'data_Type',    index: 'data_Type',     label: DATA_TYPE_TAG,         width: 160},
                    {name: 'value',        index: 'value',         label: DATA_VAL_TAG,          width: 160},
                    {name: 'quality',      index: 'quality',       label: DATA_QUALITY_TAG,      width: 160},
                    {name: 'update_Count', index: 'update_Count',  label: DATA_UPDATA_CNT_TAG,   width: 160},
                    {name: 'eqt',          index: 'eqt',           label: DATA_MAIN_TOOL_TAG,    width: 160},
                    {name: 'subEqt',       index: 'subEqt',        label: DATA_UNIT_TOOL_TAG,    width: 160},
                    {name: 'timeStamp',    index: 'timeStamp',     label: UPDATE_TIMESTAMP_TAG,  width: 160}
                ];
            //调用封装的ddGrid方法
            var options = {
                scroll: true,   //支持滚动条
                fixed: true,
                shrinkToFit: true,
                viewrecords: true,
                colModel: itemInfoCM,
                pager: domObj.jgird.$dataInfoPag,
            }
            domObj.jgird.$dataInfoGrid.ddGrid(options);
        },
    }

    var buttonFnc = {
        queryFnc : function () {
            var tool_id = domObj.$toolIdSel.find("option:selected").val();
            if(!tool_id){
              showErrorDialog("","请选择设备代码");
              return false;
            }

            var inObj ={
                tool_id: tool_id

            }
            var iary = new Array();
            var chartListValue = domObj.$dataNameSel.val();
            if(chartListValue != null && chartListValue.length > 0){

                for(var i=0;i<chartListValue.length;i++){
                    var item = {
                        data_name : chartListValue[i]
                    }
                    iary.push(item);
                }
            }
            var outObj = batchQuery("getData.do", inObj, iary);
            if(outObj.rtn_code == _NORMAL){
                var oary = outObj.oary;
                setGridInfo(oary, domObj.jgird.$dataInfoGrid);
            }
        },
        export_func :function () {
            var tool_id = domObj.$toolIdSel.find("option:selected").val();
            if(!tool_id){
                showErrorDialog("","请选择设备代码");
                return false;
            }

            var inObj ={
                tool_id: tool_id,

            }
            var iary = new Array();
            var chartListValue = domObj.$dataNameSel.val();
            if(chartListValue != null && chartListValue.length > 0){
                for(var i=0;i<chartListValue.length;i++){
                    var item = {
                        data_name : chartListValue[i]
                    }
                    iary.push(item);
                }
                inObj.iary = iary
            }
            postData(JSON.stringify(inObj),"exportData.do");
        }
    };

    var iniButtonAction = function () {
        domObj.$query_btn.click(function () {
            buttonFnc.queryFnc();
        });
        domObj.$toolIdSel.change(function () {
            initFnc.initDataName();
        })
        domObj.$downlaod_btn.click(function () {
            buttonFnc.export_func();
        })

    };


    function init(){
        initFnc.initLotInfoGrd();
        initFnc.initTool();
        iniButtonAction();
    }

    init();
    resizeFnc();
    window.setInterval(function () {
        var tool_id = domObj.$toolIdSel.find("option:selected").val();
        if(tool_id){
            var inObj ={
                tool_id: tool_id

            }
            var iary = new Array();
            var chartListValue = domObj.$dataNameSel.val();
            if(chartListValue != null && chartListValue.length > 0){
                for(var i=0;i<chartListValue.length;i++){
                    var item = {
                        data_name : chartListValue[i]
                    }
                    iary.push(item);
                }
            }

            var outObj = batchQuery("getData.do", inObj, iary);
            if(outObj.rtn_code == _NORMAL){
                var oary = outObj.oary;
                var ids = domObj.jgird.$dataInfoGrid.jqGrid("getDataIDs");
                if(ids.length == 0){
                    setGridInfo(oary, domObj.jgird.$dataInfoGrid);
                }else{
                    var dataMap = jsonArrayGroup(oary, "id");
                    for(var i=0;i<ids.length;i++){
                        var newData = dataMap[ids[i]];
                        if(newData){
                            domObj.jgird.$dataInfoGrid.jqGrid("setRowData",ids[i], newData[0]);
                        }
                    }

                }
            }
        }
    }, 5000);

    //表格自适应
    function resizeFnc(){
        domObj.jgird.$dataInfoGrid.changeTableLocation({
            widthOffset: 50,     //调整表格宽度
            heightOffset: 153,   //调整表格高度
        });

        var tabs = ['.cardBoxForm']
        tabs.forEach(function(v) {
            $(v).changeTabHeight({
                heightOffset: 60   //合并表格边框线
            });
        });
    };

    var batchQuery = function (url, inTrx, iary) {
        var data = new FormData();
        $.each(inTrx,function(name,value) {
            data.append(name, value);
        });
        if(iary.length >0){
            for(var i=0;i<iary.length;i++){
                data.append("data_groups", iary[i].data_name);
            }
        }

        var outTrx = null;
        $.ajax({
            type : "POST",
            url : url,
            async : false,// 同步
            data : data,
            cache: false,
            contentType: false,    //不可缺
            processData: false,    //不可缺
            success:function (data) {
                outTrx = JSON.parse(data);
            },
            error : function(xhr, stat, e){
                console.error(xhr);
            }
        })
        return outTrx;
    }

    $(window).resize(function () {
        resizeFnc();
    });

    var nodeNames = ['.ui-jqgrid-bdiv'];
    nodeNames.forEach(function(v) {
        $(v).setNiceScrollType({});   //设置滚动条样式
    });

    function jsonArrayGroup(arr, key){
        var map = {};
        for(var i = 0; i < arr.length; i++){
            var ai = arr[i];
            if(!map[ai[key]]){
                map[ai[key]] = [ai];
            }else{
                map[ai[key]].push(ai);
            }
        }
        return map;
    }

    function postData(data, url) {

        var formId = "form-" + new Date().getTime();
        var dataId = formId + "-data";
        var html = '<form id="' + formId
            + '" style="position:absolute;z-index:-1122;" method="post" action='+ url + '>';
        html += '<input id="' + dataId + '" name="data" type="hidden">';
        html += '</form>';

        if ($("#" + formId).length > 0) {
            $("#" + formId).remove();
        }


        $(html).appendTo("body");
        $("#" + dataId).val(data);
        $("#" + formId).submit();
        $("#" + formId).remove();
    }
});