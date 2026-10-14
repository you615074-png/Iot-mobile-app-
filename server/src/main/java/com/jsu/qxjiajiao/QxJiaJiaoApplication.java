package com.jsu.qxjiajiao;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 「倾心家教」服务端启动类。
 * - @MapperScan：扫描 mapper 包，免去每个 Mapper 接口单独加 @Mapper
 * - @EnableScheduling：Part 12 的 @Scheduled 定时提醒任务依赖此注解
 */
@SpringBootApplication
@MapperScan("com.jsu.qxjiajiao.mapper")
@EnableScheduling
public class QxJiaJiaoApplication {

    public static void main(String[] args) {
        SpringApplication.run(QxJiaJiaoApplication.class, args);
    }
}
