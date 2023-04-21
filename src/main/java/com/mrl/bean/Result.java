package com.mrl.bean;

/**
 * @Auther: MrL
 * @Date: 2023-04-14-15:57
 * @Description: 返回结果类
 * @Version: 1.0
 */
public class Result {
    //返回码
    String resultCode;
    //返回信息
    String resultMsg;
    //返回数据
    Object data;

    public Result(){}

    public Result(String resultCode, String resultMsg, Object data) {
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
        this.data = data;
    }

    public void successResult() {
        this.resultCode = ResultCode.SUCCESS;
        this.resultMsg = ResultMsg.SUCCESS_MSG;
    }

    public void successResult(Object data) {
        this.resultCode = ResultCode.SUCCESS;
        this.resultMsg = ResultMsg.SUCCESS_MSG;
        this.data = data;
    }

    public void failResult() {
        this.resultCode = ResultCode.FAIL;
        this.resultMsg = ResultMsg.FAIL_MSG;
    }

    public void failResult(Object data) {
        this.resultCode = ResultCode.FAIL;
        this.resultMsg = ResultMsg.FAIL_MSG;
        this.data = data;
    }

    public void exceptionResult() {
        this.resultCode = ResultCode.EXCEPTION;
        this.resultMsg = ResultMsg.EXCEPTION_MSG;
    }

    public void exceptionResult(Object data) {
        this.resultCode = ResultCode.EXCEPTION;
        this.resultMsg = ResultMsg.EXCEPTION_MSG;
        this.data = data;
    }

    public void loginErrorResult() {
        this.resultCode = ResultCode.LOGIN_ERROR;
        this.resultMsg = ResultMsg.LOGIN_ERROR_MSG;
    }

    public void loginErrorResult(Object data) {
        this.resultCode = ResultCode.LOGIN_ERROR;
        this.resultMsg = ResultMsg.LOGIN_ERROR_MSG;
        this.data = data;
    }
    @Override
    public String toString() {
        return "Result{" +
                "resultCode='" + resultCode + '\'' +
                ", resultMsg='" + resultMsg + '\'' +
                ", data=" + data +
                '}';
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
