package com.jsu.qxjiajiao.common;

import java.io.Serializable;

/**
 * 统一响应体。
 *
 * <p>契约来源：{@code API.md} §1.3「统一响应体」——除 {@code /api/order/export}（返回 CSV 文件流）外，
 * 所有接口均返回 {@code { code:int, msg:string, data:any }} 三段结构。</p>
 *
 * <p><b>职责边界</b>：本类在 Part 0 只落地「成功路径」，因为 Part 0 的验收要求完成
 * {@code SPEC.md} §2.2 的四步连通性验证，而该验证全部围绕 {@code GET /api/ping} 展开。
 * 错误码枚举 {@code ResultCode}、分页 {@code PageResult}、全局异常映射均属 Part 2 范围，
 * 届时在本类基础上扩展，不在此处提前实现。</p>
 *
 * @param <T> 业务数据类型
 */
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 业务状态码：200 表示成功；非 200 表示失败（完整错误码表见 API.md §1.6） */
    private Integer code;

    /** 提示语：成功固定为 "success"，失败为可直接 Toast 的中文提示 */
    private String msg;

    /** 业务数据：失败时为 null */
    private T data;

    public Result() {
    }

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /** 成功（带数据）。 */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    /** 成功（无数据）。 */
    public static Result<Void> success() {
        return new Result<>(200, "success", null);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
