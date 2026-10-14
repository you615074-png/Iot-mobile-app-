package com.jsu.qxjiajiao.controller;

import com.jsu.qxjiajiao.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 连通性探针。
 *
 * <p>契约来源：{@code API.md} {@code API-SYS-01} —— 🔓 {@code GET /api/ping}，
 * 无需 Token，返回 {@code {"code":200,"msg":"success","data":"pong"}}。</p>
 *
 * <p><b>为什么在 Part 0 落地</b>：{@code SPEC.md} §8 Part 0「实现方式 6」要求完成 §2.2 的
 * 四步连通性验证，而 §2.2 的四步全部以本接口为探测目标。接口本身在 §7 接口清单中归属 Part 2，
 * 此处仅提前落地探针所需的最小实现；Part 2 的其余内容（JWT、全局异常、文件上传、接口文档、
 * 分页）不在本 Part 范围。</p>
 */
@RestController
public class PingController {

    @GetMapping("/api/ping")
    public Result<String> ping() {
        return Result.success("pong");
    }
}
