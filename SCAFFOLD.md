# SCAFFOLD —— 「倾心家教」脚手架落地手册

> **本文档与 `SPEC.md` 的关系**
>
> - `SPEC.md` 是**唯一权威源（Single Source of Truth）**：任务分解、验收标准、数据库契约、接口清单、目录结构、Git 规范，全部以它为准。
> - 本文档是 `SPEC.md` 的**可复制粘贴落地版**：把 Part 0 / Part 1 / Part 2 / Part 3 中「要写的配置文件与起步类」整理成能直接落盘的全文内容，并补全 SPEC 未覆盖的**DevEco 签名配置**。
> - **冲突时以 `SPEC.md` 为准**。本文档中任何与 SPEC 不一致的措辞、路径、表名，都是本文档写错，应改本文档而**不是**改代码去迁就它。
> - 本文档中的事实若不确定（版本号兼容性、DevEco 菜单路径、SDK 细节等），一律显式标注 **⚠️**，请动手前自行验证，**不要照抄未验证项**。
>
> 依据的 SPEC 章节：§1（项目概述）、§2（本地开发环境）、§3（仓库与目录结构）、§6（数据库设计）、§7（接口清单）、§8 Part 0–3、§9（Git 工作流）。
> 技术口径（SPEC 已定死）：**JDK 8 + Spring Boot 2.7.18 + MyBatis-Plus + MySQL 8 + JWT + Knife4j，包名 `com.jsu.qxjiajiao`，端口 8080**；客户端 **HarmonyOS ArkTS/ArkUI，`compatibleSdkVersion = 5.0.0(12)`，bundleName `com.jsu.qxjiajiao`**。

---

## 目录

- [0. 落地总览（先读这一节）](#0-落地总览先读这一节)
- [1. 服务端 `pom.xml` 全文](#1-服务端-pomxml-全文)
- [2. `application.yml` 全文](#2-applicationyml-全文)
- [3. Java 侧起步类](#3-java-侧起步类)
- [4. 数据库初始化](#4-数据库初始化)
- [5. 鸿蒙客户端工程创建](#5-鸿蒙客户端工程创建)
- [6. ⚠️ DevEco 签名配置（重点，SPEC 未覆盖）](#6-️-deveco-签名配置重点spec-未覆盖)
- [7. `module.json5` 全文](#7-modulejson5-全文)
- [8. `build-profile.json5` / `oh-package.json5` / `main_pages.json`](#8-build-profilejson5--oh-packagejson5--main_pagesjson)
- [9. 常量起点：`request.ets` 里的 `BASE_URL`](#9-常量起点requestets-里的-base_url)
- [10. 启动与自检清单](#10-启动与自检清单)

---

## 0. 落地总览（先读这一节）

### 0.1 这套脚手架要达成的目标（对应 SPEC Part 0）

建立**可编译、可运行、可推送**的空壳双端工程：

- 服务端：`mvn spring-boot:run` 起得来，`/` 有响应，`/api/ping` 返回统一响应体；
- 客户端：DevEco 能 `assembleHap` 成功，模拟器能显示 Hello World，并能访问宿主机 `http://<局域网IP>:8080/`；
- 数据库：`scripts/init-db.bat` 一键建库建表 + 导入演示数据。

### 0.2 落盘顺序（严格按此顺序，不要跳）

```
① 读 SPEC §2.3  →  处理中文路径风险（迁到 D:\dev\qingxin-jiajiao\ 或开启 UTF-8 Beta）
② 服务端      →  server/pom.xml → application.yml → 三个起步类
③ 数据库      →  schema.sql / data.sql → scripts/init-db.bat → 执行
④ 服务端自检  →  mvn spring-boot:run → 浏览器访问 /api/ping
⑤ 客户端工程  →  DevEco 新建工程（§5）→ 手工建包（§5.3）→ 签名（§6）
⑥ 客户端自检  →  模拟器 Run → 显示 Hello World
⑦ 连通性四步  →  见 §10.3（SPEC §2.2）
⑧ git init + 首次 commit + push（SPEC §9）
```

### 0.3 目录落点速查（对照 SPEC §3）

| 内容 | 落点 |
|---|---|
| Maven 工程根 | `server/` |
| Spring Boot 配置 | `server/src/main/resources/application.yml` |
| 建表脚本 | `server/src/main/resources/db/schema.sql` |
| 演示数据 | `server/src/main/resources/db/data.sql` |
| 一键初始化 | `scripts/init-db.bat` |
| DevEco 工程根 | `client/` |
| 权限声明 | `client/entry/src/main/module.json5` |
| 网络层单点 | `client/entry/src/main/ets/api/request.ets` |

---

## 1. 服务端 `pom.xml` 全文

> **这个文件解决什么问题**：声明服务端用的是 Spring Boot 2.7.18 父工程、JDK 8，以及全部第三方依赖与版本，让 `mvn` 能一次性拉齐依赖并编译打包。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- ============================================================
         Spring Boot 父工程：2.7.18（方案 A：JDK 8 + Spring Boot 2.7.18）
         2.7.x 是最后一条支持 JDK 8 的 Spring Boot 主线；2.7.18 为其最后一个补丁版。
         【变更规则】一旦选定方案 A，Part 0 之后不允许中途换 3.x（3.x 要求 JDK 17 且 javax.* → jakarta.*）。
         ============================================================ -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.18</version>
        <relativePath/>
    </parent>

    <groupId>com.jsu</groupId>
    <artifactId>qxjiajiao-server</artifactId>
    <version>1.0.0</version>
    <name>qxjiajiao-server</name>
    <description>倾心家教 · 服务端（Spring Boot + MyBatis-Plus + MySQL）</description>
    <packaging>jar</packaging>

    <properties>
        <!-- JDK 8：由 spring-boot-starter-parent 映射到 maven.compiler.source/target -->
        <java.version>1.8</java.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>

        <!-- 第三方依赖版本集中管理（下面 dependency 里不再散写版本号） -->
        <mybatis-plus.version>3.5.3.1</mybatis-plus.version>
        <mysql.version>8.0.33</mysql.version>
        <lombok.version>1.18.30</lombok.version>
        <jjwt.version>0.11.5</jjwt.version>
        <knife4j.version>3.0.3</knife4j.version>
    </properties>

    <dependencies>
        <!-- ==================== Web / 校验 / WebSocket（由 Boot BOM 管理版本=2.7.18） ==================== -->
        <!-- 【版本说明】以下 4 个 spring-boot-starter-* 的版本由父工程 BOM 统一提供 = 2.7.18，
             与 JDK 8 完全兼容。此处显式写出 2.7.18 仅为“口径可见”，删掉亦可正常构建。 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <version>2.7.18</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
            <version>2.7.18</version>
        </dependency>
        <!-- Part 11 实时聊天用：@ServerEndpoint / WebSocketConfig -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-websocket</artifactId>
            <version>2.7.18</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <version>2.7.18</version>
            <scope>test</scope>
        </dependency>

        <!-- ==================== MyBatis-Plus ==================== -->
        <!-- 【兼容性】MyBatis-Plus 3.5.3.1 是 Boot 2.x + JDK 8 广泛验证过的稳定版本。
             ⚠️ 3.5.9+ 起官方拆分了 starter（针对 Boot 3 的 -spring-boot3-starter），若升级务必确认
             是否仍使用 mybatis-plus-boot-starter 这个坐标，且不要在 Boot 2.7 上误用 Boot3 版 starter。
             若 3.5.3.1 拉取异常，可退回 3.5.5（同样支持 Boot 2.7 + JDK 8）。 -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>

        <!-- ==================== MySQL 驱动 ==================== -->
        <!-- 【兼容性】Spring Boot 2.7.18 管理的是 mysql:mysql-connector-java:8.0.33。
             注意：Boot 3.x 才改为新的坐标 com.mysql:mysql-connector-j，本项目（Boot 2.7）用旧坐标。
             ⚠️ 若构建时提示该坐标版本找不到，用 `mvn dependency:tree` 确认 BOM 实际管理版本后对齐。 -->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>${mysql.version}</version>
            <scope>runtime</scope>
        </dependency>

        <!-- ==================== Lombok ==================== -->
        <!-- 【兼容性】1.18.30 支持 JDK 8；也是 Boot 2.7.18 BOM 管理的版本。 -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>${lombok.version}</version>
            <optional>true</optional>
        </dependency>

        <!-- ==================== JWT（jjwt 0.11.5 三件套） ==================== -->
        <!-- 【兼容性】0.11.5 支持 JDK 8，API 与网上教程一致（Jwts.builder()/parserBuilder()）。
             ⚠️ 若误用 0.12.x，API 大改（parserBuilder 废弃、签名密钥改用 SecretKey），会编译失败——不要升级。
             注意：HS256 要求密钥长度 >= 256 bit（32 字节），见 application.yml 中 jwt.secret 说明。 -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>

        <!-- ==================== Knife4j 接口文档 ==================== -->
        <!-- 【兼容性】knife4j-spring-boot-starter 3.0.3 基于 Springfox 3.0，适配 Spring Boot 2.x。
             ⚠️ 重要坑：Spring Boot 2.6+ 默认改用 PathPatternParser，会与 Springfox 冲突导致启动报
             "Failed to start bean 'documentationPluginsBootstrapper'"。因此必须在 application.yml 中加
             spring.mvc.pathmatch.matching-strategy: ant_path_matcher（已在 §2 写好）。
             ⚠️ 若 3.0.3 在你的环境仍起不来，可改用 knife4j-openapi3-spring-boot-starter 4.x（基于 springdoc，
             对 Boot 2.7 更友好），但包名/依赖坐标与注解需相应调整——请先本地验证后再定稿。 -->
        <dependency>
            <groupId>com.github.xiaoymin</groupId>
            <artifactId>knife4j-spring-boot-starter</artifactId>
            <version>${knife4j.version}</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <!-- 打包时排除 lombok（编译期注解处理器，无需进 runtime） -->
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### 1.1 依赖版本与兼容性一览（写报告「技术选型」章可直接引用）

| 依赖 | 版本 | 与 Boot 2.7.18 / JDK 8 的兼容性 | 备注 |
|---|---|---|---|
| spring-boot-starter-web / validation / websocket / test | 2.7.18 | ✅ 由 BOM 管理，原生支持 JDK 8 | 显式写版本仅为口径可见 |
| mybatis-plus-boot-starter | 3.5.3.1 | ✅ 稳定组合；⚠️ 升级 3.5.9+ 需确认坐标 | 备选 3.5.5 |
| mysql-connector-java | 8.0.33 | ✅ 与 MySQL 8、Boot 2.7 BOM 一致 | Boot 3 才换新坐标 |
| lombok | 1.18.30 | ✅ 支持 JDK 8 | BOM 同版本 |
| jjwt-api/impl/jackson | 0.11.5 | ✅ 支持 JDK 8；⚠️ 勿升 0.12.x | 密钥须 ≥ 32 字节 |
| knife4j-spring-boot-starter | 3.0.3 | ✅ 适配 Boot 2.x；⚠️ 需 `ant_path_matcher` 兜底 | 备选 knife4j-openapi3 4.x |

---

## 2. `application.yml` 全文

> **这个文件解决什么问题**：一处集中声明端口、数据源、MyBatis-Plus 行为、上传大小限制、JWT 密钥、上传目录、支付 mock 开关与日志级别，替代散落在代码里的硬编码。

```yaml
# ============================================================
#  倾心家教 · 服务端配置
#  方案 A：JDK 8 + Spring Boot 2.7.18
# ============================================================
server:
  port: 8080
  # SPEC §2.2：必须监听 0.0.0.0，模拟器才能通过宿主局域网 IP 访问
  address: 0.0.0.0

spring:
  application:
    name: qxjiajiao-server

  # ---------- 数据源（MySQL 8，库名 qxjiajiao，utf8mb4，Asia/Shanghai） ----------
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    # ⚠️ characterEncoding=utf8 在 Connector/J 8 下会被映射为服务端 utf8mb4，配合库表 utf8mb4 即可存中文/emoji
    url: "jdbc:mysql://localhost:3306/qxjiajiao?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&rewriteBatchedStatements=true"
    username: root
    # ⚠️ 请改成你自己的 MySQL 密码；不要把真实密码 push 到远端（可改用 application-local.yml，已被 .gitignore 排除）
    password: "请替换为你的MySQL密码"

  # ---------- 上传大小限制（SPEC Part 2：限制后缀 jpg/png/webp/mp3/m4a，单文件 10MB） ----------
  servlet:
    multipart:
      enabled: true
      max-file-size: 10MB        # 单文件上限
      max-request-size: 20MB     # 单次请求上限（多文件时）
      file-size-threshold: 2MB

  # ---------- ⚠️ Knife4j(Springfox) 必需：解决 Boot 2.6+ PathPatternParser 冲突 ----------
  mvc:
    pathmatch:
      matching-strategy: ant_path_matcher

  # ---------- Jackson（统一时间格式，避免前端拿到时间戳） ----------
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: Asia/Shanghai
    default-property-inclusion: non_null

# ============================================================
#  MyBatis-Plus
# ============================================================
mybatis-plus:
  # Mapper XML 位置（SPEC §3.3：resources/mapper/*.xml）
  mapper-locations: classpath*:/mapper/**/*.xml
  # 实体包，开启后 XML 里可直接写类名不用全限定名
  type-aliases-package: com.jsu.qxjiajiao.entity
  configuration:
    # 下划线字段名 -> 驼峰属性名（如 create_time -> createTime）
    map-underscore-to-camel-case: true
    # 开发期打印 SQL；上线前可改为 NoLoggingImpl
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    banner: false
    db-config:
      # 主键策略：数据库自增（对应 schema.sql 的 BIGINT AUTO_INCREMENT）
      id-type: auto
      # 逻辑删除：实体字段 deleted，0=未删除，1=已删除（对应 §6.3 sys_user/order 的 deleted 字段）
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
  # 分页插件不是在这里配的，而在 MybatisPlusConfig.java 里注册（见 §3.2）

# ============================================================
#  本项目自定义配置项
# ============================================================
qxjiajiao:
  jwt:
    # ⚠️⚠️ 必须自行替换！这不是可用密钥，只是一个占位符。⚠️⚠️
    # 生成建议（任选一种，得到 >= 32 字符的随机串）：
    #   PowerShell : [Convert]::ToBase64String((1..48 | %{[byte](Get-Random -Max 256)}))
    #   Git Bash   : openssl rand -base64 48
    # 长度要求：jjwt 0.11.5 使用 HS256，密钥必须 >= 256 bit（32 字节），否则抛 WeakKeyException。
    secret: "CHANGE_ME_REPLACE_WITH_YOUR_OWN_RANDOM_SECRET_AT_LEAST_32_CHARS"
    # JWT 有效期（天）：SPEC Part 2 规定 7 天
    expire-days: 7
  upload:
    # 文件落盘根目录：必须是【绝对路径】且用【正斜杠 /】（Windows 反斜杠会被当转义）。
    # 该目录已被 .gitignore 排除（server/src/main/resources/upload/），运行时自动创建。
    # ⚠️ 若工程迁到 ASCII 路径（SPEC §2.3），这里也要同步改成新路径。
    dir: "D:/dev/qingxin-jiajiao/server/src/main/resources/upload"
  pay:
    # 支付 mock 总开关：true=走模拟支付（本项目默认），不接真实微信/支付宝
    mock: true

# ============================================================
#  日志
# ============================================================
logging:
  level:
    root: info
    com.jsu.qxjiajiao: debug
    com.jsu.qxjiajiao.mapper: debug   # 打印 Mapper 层 SQL（调试期）
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
```

**配置项落点核对（务必与 SPEC 一致）**

| 配置 | 值 | SPEC 依据 |
|---|---|---|
| 端口 | 8080 | §2.2 |
| 监听地址 | 0.0.0.0 | §2.2 |
| 库名 / 字符集 / 时区 | `qxjiajiao` / utf8mb4 / Asia/Shanghai | §6 |
| 逻辑删除字段 | `deleted`（1/0） | §6.3 |
| JWT 有效期 | 7 天 | Part 2 实现方式 3 |
| 上传大小 | 10MB | Part 2 实现方式 8 |
| 支付 mock | true | §1.2 / Part 9 |
| 接口前缀 | `/api` | §7 |

---

## 3. Java 侧起步类

> 本节三个类对应 SPEC §8 Part 2 的「公共层」地基。**注意：Spring Boot 2.7 用 `javax.*`，不是 `jakarta.*`**（`jakarta.*` 是 Boot 3 的写法，本项目用不到，写错会编译失败）。

### 3.1 `QxJiaJiaoApplication.java`

> **这个文件解决什么问题**：Spring Boot 启动入口，同时声明 Mapper 扫描路径与定时任务开关（Part 12 提醒任务需要）。

路径：`server/src/main/java/com/jsu/qxjiajiao/QxJiaJiaoApplication.java`

```java
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
```

### 3.2 `MybatisPlusConfig.java`（分页插件 + 逻辑删除）

> **这个文件解决什么问题**：注册 MyBatis-Plus 分页内插件，让 `Page<T>` 查询真的会拼 `LIMIT`；逻辑删除则由 §2 的 yml 全局配置生效（此处不再重复声明，避免双份配置）。

路径：`server/src/main/java/com/jsu/qxjiajiao/common/config/MybatisPlusConfig.java`

```java
package com.jsu.qxjiajiao.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置。
 * 说明：逻辑删除（deleted 字段，1=删 0=未删）在 application.yml 的
 *      mybatis-plus.global-config.db-config 中全局声明，此处只注册分页插件。
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 分页插件。SPEC §7 约定分页响应为 { "total": n, "records": [...] }，
     * 与 MyBatis-Plus Page 对象的字段命名一致，Controller 直接透传即可。
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        PaginationInnerInterceptor pagination = new PaginationInnerInterceptor(DbType.MYSQL);
        // 单页上限，防止前端传超大 size 拖垮数据库
        pagination.setMaxLimit(100L);
        // 请求页码超过总页数时返回空集（false=不回到首页）
        pagination.setOverflow(false);

        interceptor.addInnerInterceptor(pagination);
        return interceptor;
    }
}
```

> **⚠️ 顺序提醒**：如果将来加了多租户、动态表名等插件，**分页插件必须最后 add**（MyBatis-Plus 官方要求），否则分页失效。

### 3.3 `WebMvcConfig.java`（CORS + 静态资源映射 + 拦截器注册骨架）

> **这个文件解决什么问题**：① 开发期放开跨域；② 把 `/upload/**` 映射到本地磁盘，让上传的图片/语音能被浏览器与客户端直接访问（SPEC Part 2 实现方式 5、风险表第 8 条）；③ 预留 JWT 拦截器的注册位置。

路径：`server/src/main/java/com/jsu/qxjiajiao/common/config/WebMvcConfig.java`

```java
package com.jsu.qxjiajiao.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Web MVC 配置：跨域 + 静态资源映射 + 拦截器注册。
 * 注意：本类当前【可独立编译】——拦截器部分以注释给出骨架，
 *      待 Part 2 写完 JwtInterceptor 后再解除注释（见类尾）。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /** 上传根目录，来自 application.yml 的 qxjiajiao.upload.dir（绝对路径） */
    @Value("${qxjiajiao.upload.dir}")
    private String uploadDir;

    /**
     * 全局 CORS：开发期允许所有来源。
     * 用 allowedOriginPatterns 而不是 allowedOrigins("*")，因为要同时 allowCredentials(true)。
     * （Spring 5.3 起允许 allowedOriginPatterns("*") + 凭证，是老写法 allowedOrigins("*") 的替代）
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 静态资源映射：/upload/** -> 本地磁盘 upload 目录。
     * 用 Paths.get(...).toUri() 生成 file: URI，自动兼容 Windows 反斜杠，避免手拼 file: 前缀踩坑。
     * 例：上传后返回的 URL 形如 http://<host>:8080/upload/2026/10/14/xxx.jpg
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Paths.get(uploadDir).toUri().toString();
        registry.addResourceHandler("/upload/**")
                .addResourceLocations(location);

        // Knife4j / Springfox 的静态资源（文档页 /doc.html）
        registry.addResourceHandler("/doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    // =========================================================================
    //  拦截器注册【骨架】—— Part 2 完成 JwtInterceptor 后，删除注释并补 import 即可
    //
    //  import jakarta.* >>> 不需要！本项目 Boot 2.7 一律 javax.*
    //  import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
    //  import javax.annotation.Resource;
    //  import com.jsu.qxjiajiao.common.interceptor.JwtInterceptor;
    //
    //  @Resource
    //  private JwtInterceptor jwtInterceptor;
    //
    //  @Override
    //  public void addInterceptors(InterceptorRegistry registry) {
    //      registry.addInterceptor(jwtInterceptor)
    //              .addPathPatterns("/api/**")                 // 拦截所有业务接口
    //              .excludePathPatterns(
    //                      "/api/auth/**",                     // 登录/注册/验证码放行
    //                      "/api/ping",                        // 探针放行
    //                      "/api/file/**",                     // 上传/静态读放行
    //                      "/doc.html",                        // 接口文档放行
    //                      "/webjars/**",
    //                      "/swagger-resources/**",
    //                      "/v2/api-docs/**"
    //              );
    //  }
    // =========================================================================
}
```

**三个类落地后的自检**：`mvn -q compile` 应零报错；`javax.*` 全篇不出现 `jakarta.*`。

---

## 4. 数据库初始化

### 4.1 创建库与用户（MySQL 命令说明）

> **这个文件解决什么问题**：手工确认 MySQL 已就绪、库与账号已建立。`schema.sql` 本身也会建库，但先把账号建好可避免权限问题。

方式一（最省事，直接用 root）：无需额外建用户，直接执行 `scripts/init-db.bat`。

方式二（推荐，建专用账号，避免把 root 密码写进配置）：

```sql
-- 以 root 登录后执行
-- 1) 建库（字符集 utf8mb4 / 排序 utf8mb4_general_ci，与 SPEC §6 一致）
CREATE DATABASE IF NOT EXISTS `qxjiajiao`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

-- 2) 建专用用户（仅本机可连，符合 SPEC §2.2「MySQL 仅本机使用」）
CREATE USER IF NOT EXISTS 'qxjiajiao'@'localhost' IDENTIFIED BY 'qxjiajiao123';

-- 3) 授权
GRANT ALL PRIVILEGES ON `qxjiajiao`.* TO 'qxjiajiao'@'localhost';
FLUSH PRIVILEGES;
```

> 若用专用账号，请同步把 `application.yml` 的 `username` / `password` 改成 `qxjiajiao` / `qxjiajiao123`。
> ⚠️ 验证服务端能否连上：`mysql -uqxjiajiao -pqxjiajiao123 -e "SELECT 1;"`。

### 4.2 `scripts/init-db.bat` 全文

> **这个文件解决什么问题**：Windows 下双击即可按顺序执行 `schema.sql`（建库建表）与 `data.sql`（导入演示数据），免去手敲两条长命令。

路径：`scripts/init-db.bat`

```bat
@echo off
REM ============================================================
REM  倾心家教 —— 数据库一键初始化脚本 (Windows)
REM  作用：依次执行 db\schema.sql（建库 + 建 19 张表）与 db\data.sql（导入演示数据）
REM  依赖：MySQL 8 已安装，且 mysql.exe 已加入系统 PATH
REM  用法：双击运行，或在项目根执行  scripts\init-db.bat
REM ============================================================
chcp 65001 >nul
setlocal

REM ---------- 可按需修改 ----------
set "MYSQL_HOST=127.0.0.1"
set "MYSQL_PORT=3306"
set "MYSQL_USER=root"
REM ⚠️ 把下面改成你的 MySQL 密码；若留空，则脚本会以交互方式提示输入密码
set "MYSQL_PWD=你的密码"

REM ---------- 定位 SQL 文件（相对本脚本所在目录） ----------
set "SCRIPT_DIR=%~dp0"
set "DB_DIR=%SCRIPT_DIR%..\server\src\main\resources\db"
REM 无密码时用 -p 让 mysql 交互输入；有密码时脚本自动拼 -p<密码>
set "PW_ARG=-p%MYSQL_PWD%"
if "%MYSQL_PWD%"=="" set "PW_ARG=-p"

echo ============================================================
echo  倾心家教 数据库初始化
echo  SQL 目录：%DB_DIR%
echo ============================================================

REM ---------- [1/3] 检查 mysql 客户端 ----------
echo [1/3] 检查 mysql 客户端是否可用...
where mysql >nul 2>nul
if errorlevel 1 (
    echo [错误] 未找到 mysql 命令。请将 MySQL 的 bin 目录加入系统 PATH 后重试。
    pause
    exit /b 1
)

REM ---------- [2/3] 执行 schema.sql（建库 + 建表） ----------
echo [2/3] 执行 schema.sql（建库 + 建表）...
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% %PW_ARG% --default-character-set=utf8mb4 < "%DB_DIR%\schema.sql"
if errorlevel 1 (
    echo [错误] schema.sql 执行失败，请检查上方报错信息。
    pause
    exit /b 1
)

REM ---------- [3/3] 执行 data.sql（导入演示数据） ----------
echo [3/3] 执行 data.sql（导入演示数据）...
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% %PW_ARG% --default-character-set=utf8mb4 qxjiajiao < "%DB_DIR%\data.sql"
if errorlevel 1 (
    echo [错误] data.sql 执行失败，请检查上方报错信息。
    pause
    exit /b 1
)

echo.
echo [完成] 数据库 qxjiajiao 初始化成功。
echo        管理员账号：13800000000 / admin123
pause
endlocal
```

**使用说明与注意事项**

1. `schema.sql` 首部应包含（与 SPEC §6 一致）：
   ```sql
   CREATE DATABASE IF NOT EXISTS `qxjiajiao`
     DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
   USE `qxjiajiao`;
   ```
   这样即使还没建库也能一次跑通。
2. `data.sql` 需显式指定库：脚本里已加 `qxjiajiao` 参数（或在文件头写 `USE qxjiajiao;`）。
3. ⚠️ 若密码含特殊字符（`&`、`^`、`%` 等），批处理的 `-p<密码>` 拼接可能出错——此时把 `MYSQL_PWD` 留空，走交互式 `-p` 提示输入。
4. ⚠️ Windows 中文路径（`D:\dev\...` 之外的 `桌面\物联网移动应用开发\`）下若 `mysql` 读取中文名文件异常，先把工程迁到 ASCII 路径（SPEC §2.3）。
5. 执行完成后自检：
   ```sql
   USE qxjiajiao;
   SHOW TABLES;          -- 应有 19 张表
   SELECT COUNT(*) FROM sys_user;   -- 至少 7 条（1 管理员 + 2 家长 + 4 教师）
   ```

---

## 5. 鸿蒙客户端工程创建

> 对应 SPEC §8 Part 0 第 3 步与 §3.2 目录结构。

### 5.1 DevEco Studio 新建工程 step-by-step

> ⚠️ **DevEco 菜单项名称与位置随版本变化**，以下按 5.0（API 12）常见版本描述。若你的界面与描述不符，以你本地实际菜单为准，**不要硬套**。

1. 启动 DevEco Studio → 顶部菜单 **`File > New > Create Project`**。
2. 左侧选 **`Application`** 分类 → 选模板 **`Empty Ability`** → **`Next`**。
3. 填写工程信息（**关键，逐项对照 SPEC**）：

   | 字段 | 填什么 | 依据 |
   |---|---|---|
   | Project name | `client`（或 `qingxin-jiajiao-client`） | §3.1 `client/` |
   | Bundle name | `com.jsu.qxjiajiao` | §3.2（与服务端包名一致） |
   | Save location | 指向仓库的 **`...\qingxin-jiajiao\client`** | §3.1 |
   | Compile SDK | **`5.0.0(12)`** | §2.1 |
   | Compatible SDK | **`5.0.0(12)`** | §3.2 `compatibleSdkVersion: 5.0.0(12)` |
   | Model | `Stage`（Stage 模型） | API 12 默认 |
   | Device type | 勾选 **`Phone`** | Part 0 第 3 步 |
   | Language | **`ArkTS`** | §1 客户端技术栈 |

4. **`Finish`** → 等待右下角 `Sync` 完成（首次会下载依赖，耗时较长）。
5. 确认生成后目录里存在：`AppScope/`、`entry/`、`hvigor/`、`build-profile.json5`、`oh-package.json5`、`hvigorfile.ts`（与 SPEC §3.2 生成部分一致）。

> ⚠️ **优先在 `D:\dev\qingxin-jiajiao\` 等 ASCII 路径下新建工程**（SPEC §2.3）。若必须在中文路径下，先按 §2.3 方式二开启 Windows UTF-8 Beta 并重启。

### 5.2 生成后需手工核对/修改的文件

| 文件 | 动作 |
|---|---|
| `AppScope/app.json5` | 确认 `bundleName = com.jsu.qxjiajiao` |
| `build-profile.json5`（根） | 确认 `compatibleSdkVersion: "5.0.0(12)"`（见 §8.1） |
| `entry/src/main/module.json5` | 补全 5 个权限（见 §7） |
| `entry/src/main/resources/base/profile/main_pages.json` | 见 §8.3 |
| `entry/src/main/ets/entryability/EntryAbility.ets` | 由模板生成，Part 0 不改 |

### 5.3 需手工新建的包与空目录清单（对照 SPEC §3.2）

ArkTS 没有 Java 那样严格的「包」概念，**这些就是 `entry/src/main/ets/` 下的目录**：

```
entry/src/main/ets/
├── entryability/          （模板已生成，含 EntryAbility.ets）
├── pages/                 ← 新建：路由可达页（@Entry），Part 3 放 Index.ets
├── views/                 ← 新建：Tab 内容视图（非路由）
├── components/            ← 新建：可复用 UI 组件（Part 3 起建 common/ 子目录）
├── api/                   ← 新建：网络层（Part 3 放 request.ets）
│   └── modules/           ← 新建：按域拆分接口调用（auth/user/file/... 空壳待填）
├── models/                ← 新建：类型定义（interface）
├── constants/             ← 新建：常量 / 枚举 / 字典 / 主题
├── utils/                 ← 新建：工具函数
├── storages/              ← 新建：全局状态（AppStorage / Preferences）
└── router/                ← 新建：Navigation 路由表（RouterMap.ets）
```

> ⚠️ **空目录不会被 Git 跟踪**。在 Part 3 真正写入文件前，可在每个空目录里放一个占位文件（例如 `.gitkeep`），否则 `git add` 时目录会「消失」。
> ⚠️ **不要把测试文件混进来**：模板生成的 `entry/src/ohosTest/`、`entry/src/test/` 保留即可，不属于 §3.2 手工建包范围。

### 5.4 客户端包名 / SDK 口径（不要改）

| 项 | 值 | 位置 |
|---|---|---|
| bundleName | `com.jsu.qxjiajiao` | `AppScope/app.json5` |
| compatibleSdkVersion | `5.0.0(12)` | `build-profile.json5` |
| deviceTypes | `phone`（可按需加 `tablet`） | `module.json5` |

---

## 6. ⚠️ DevEco 签名配置（重点，SPEC 未覆盖）

> **为什么单独抽一章**：这是鸿蒙新手**最容易卡住**的一环。SPEC §8 Part 0 的「DevEco 能 `assembleHap` 成功」隐含了「签名已配好」，但没有写怎么配。**不配签名，Run 按钮会报错，HAP 无法安装到模拟器或真机。**
>
> ⚠️ **本章菜单路径描述基于常见 DevEco 5.0 版本，不同版本可能略有出入；凡我不能 100% 确认的地方都已标注 ⚠️，请以你本地 DevEco 实际界面为准。**

### 6.1 为什么必须配签名

HAP 包在安装到设备（模拟器或真机）前，系统会校验其**数字签名**：

- 未签名的 HAP **无法安装**，DevEco 报错类似 `Failed to install the Hap because the signature is missing` 或 `signingConfig not found`；
- `assembleHap` 可以跑通（它只编译打包），但 **`Run` / 安装会失败**；
- 因此 Part 0 验收里的「DevEco 能 assembleHap 成功，模拟器启动后显示 Hello World」，实际上要求**两件事都对**：能编译 + 能签名安装。

### 6.2 需要的账号

1. 注册**华为开发者账号**（个人开发者即可，**免费**）：
   - ⚠️ 注册入口请走华为开发者联盟官网（`developer.huawei.com`）→ 注册 → 完成**实名认证**。
   - ⚠️ **实名认证是自动签名的前置条件**，未实名会导致自动签名失败（见 §6.4）。
2. 在 DevEco Studio 内登录该账号：
   - 右上角头像 / 或 `File > Settings > ... > DevEco Account`（⚠️ 具体位置随版本变化）→ 扫码或账号密码登录。
   - ⚠️ 若登录态过期，自动签名会静默失败，先重新登录再试。

### 6.3 自动签名 step-by-step

> 目标：让 DevEco **自动生成**调试证书与 Profile（个人开发者调试最常见、最省事的方式）。

1. 打开工程后，菜单 **`File > Project Structure`**（⚠️ 也可能是 `Project Structure...` 带省略号）。
2. 左侧选 **`Project`** → 顶部切到 **`Signing Configs`** 标签页。
3. 勾选 **`Automatically generate signature`**（自动生成签名）。
   - ⚠️ 勾选后 DevEco 会要求先登录华为账号（若未登录会弹登录框）。
4. 点击 **`Sign In`** 登录（若已登录可跳过）→ 等待自动生成。
5. 生成成功后，`Signing Configs` 下会出现一条名为 `default` 的配置，包含：
   - `Store File`（`.p12`）、`Store Password`、`Key Alias`、`Key Password`、`Profile File`（`.p7b`）、`Certpath File`（`.cer`）。
   - ⚠️ 这些文件默认落在用户目录（如 `C:\Users\<你>\.ohos\config\` 或工程内 `signature/`），**不要提交到 Git**（已在 `.gitignore` 排除 `client/signature/`）。
6. 回到 `build-profile.json5`（根），应能看到 `signingConfigs` 数组被自动填入，且 `products[].signingConfig` 指向 `"default"`（见 §8.1）。
7. **验证**：点 **`Run`**（或 `Build > Build Hap(s)`）→ 无签名相关报错即成功。

### 6.4 自动签名失败的常见原因与排查

| # | 现象 | 可能原因 | 排查/解决 |
|---|---|---|---|
| 1 | 勾选后无法生成，或提示需登录 | **未登录 / 登录态过期** | 重新登录华为开发者账号（§6.2） |
| 2 | 生成流程卡住或报网络错 | **网络不通**（需访问华为服务器） | 检查能否访问 `developer.huawei.com`；关闭会拦截的代理/VPN 重试 |
| 3 | 提示账号未认证 | **未完成实名认证** | 去开发者联盟官网完成实名认证后重试 |
| 4 | 提示 SDK / 工具缺失 | **HarmonyOS SDK 未下载完整** | 打开 `SDK Manager`（⚠️ 入口可能在 `File > Settings > SDK` 或独立工具）确认 `5.0.0(12)` 平台及工具链已安装 |
| 5 | 提示设备未授权 / 首次连接 | **真机未开启调试或未信任电脑** | 见 §6.5 真机步骤 |
| 6 | 提示 `bundleName` 与已有证书冲突 | 同一 bundleName 已被别的证书占用 | 换用未被占用的调试 bundleName，或清理旧证书后重签 |
| 7 | 签名成功但安装失败 | 设备 API 版本低于 `compatibleSdkVersion` | 确认模拟器/真机为 API 12（HarmonyOS 5.0） |

> ⚠️ 以上报错文案为常见情形的**近似描述**，DevEco 不同版本提示语不同；请以**报错关键字段**（如 `signature`、`signingConfig`、`certificate`、`profile`）在日志里定位，而不是纠结原文。

### 6.5 真机调试额外步骤

1. 真机需为**鸿蒙/HarmonyOS 5.0（API 12）** 设备，并开启**开发者模式**：
   - `设置 > 关于手机 > 连点「版本号」7 次` → 返回 `设置 > 系统和更新 > 开发人员选项` → 打开 **`USB 调试`**。
   - ⚠️ 具体路径随 HarmonyOS 版本变化。
2. USB 连接电脑，设备弹出「**是否允许 USB 调试**」→ 勾选「始终允许」→ 确定。
3. DevEco 顶部设备下拉里应出现该真机；点 `Run` 安装。
4. ⚠️ 真机调试同样需要签名，且**真机的签名要求比模拟器更严**：自动签名会为你的调试证书申请一个包含该真机 UDID 的调试 Profile——因此真机首次接入时**务必重新执行一次自动签名**（§6.3），否则 Profile 不含此设备会安装失败。
5. ⚠️ 免费个人开发者账号的调试设备数量有名额限制；若提示超出数量，需先在开发者联盟后台移除不再使用的设备。

### 6.6 模拟器是否也需要签名？以及模拟器设备怎么来

- **模拟器同样需要签名**。HarmonyOS 的模拟器本质上也是一台「设备」，安装未签名 HAP 一样失败。**§6.3 的自动签名对模拟器与真机都适用**。
- 模拟器设备**必须先创建**：
  1. DevEco 菜单 **`Tools > Device Manager`**（⚠️ 也可能叫 `Device Manager` 浮动面板）。
  2. 切到 **`Local Emulator`** 标签 → **`New Emulator`**（新建）。
  3. 选择 **`Phone`** 设备类型与 API 12 系统镜像 → 下载镜像 → 创建（首次需下载系统镜像，体积较大）。
  4. 创建完成后**启动**该模拟器，DevEco 设备下拉里选中它，再 `Run`。
- ⚠️ 若 `Device Manager` 里没有 API 12 镜像可选，需先在 `SDK Manager` 下载对应镜像。

### 6.7 本章不确定项汇总（务必自行验证）

> 以下内容我**不能 100% 确认**，请以你本地 DevEco 5.0 实际界面为准，**不要照抄**：
>
> - ⚠️ `File > Project Structure > Project > Signing Configs` 的**确切层级与名称**，不同 DevEco 版本可能不同（有的版本在 `Project Structure` 下叫 `Signing Configs`，有的把它放在 `Project` 节点内）。
> - ⚠️ 账号登录入口的具体位置（顶部头像 / `Settings` / 首次签名时弹窗）。
> - ⚠️ 自动生成的证书文件默认目录（`.ohos/config/` 还是工程内 `signature/`）。
> - ⚠️ 真机开启开发者模式的**具体菜单层级**。
> - ⚠️ `Device Manager` 与 `SDK Manager` 的确切入口名称与位置。

---

## 7. `module.json5` 全文

> **这个文件解决什么问题**：声明本模块（entry）的基本信息、启动 Ability，以及**全部所需权限**——SPEC §3.2 要求的 5 个权限必须在此登记，否则对应能力（网络 / 相册 / 相机 / 麦克风 / 通知）在运行时会被拒绝。
>
> ⚠️ **只把 `requestPermissions` 当作「要新增的内容」**；`name`、`icon`、`label`、`startWindowIcon` 等字段请**保留 DevEco 模板生成的原值**（不同模板的图标资源名可能是 `layered_image` 或 `startIcon`，不要强行替换成本文示例值）。

路径：`client/entry/src/main/module.json5`

```json5
{
  "module": {
    // ===== 基本字段（模板生成，除 requestPermissions 外一般不用改）=====
    "name": "entry",
    "type": "entry",
    "description": "$string:module_desc",
    "mainElement": "EntryAbility",
    "deviceTypes": [
      "phone"
      // 需要平板时可加 "tablet"
    ],
    "deliveryWithInstall": true,
    "installationFree": false,
    "pages": "$profile:main_pages",

    // ===== 启动 Ability（字段名对照模板，图标资源名以模板为准）=====
    "abilities": [
      {
        "name": "EntryAbility",
        "srcEntry": "./ets/entryability/EntryAbility.ets",
        "description": "$string:EntryAbility_desc",
        "icon": "$media:startIcon",
        "label": "$string:EntryAbility_label",
        "startWindowIcon": "$media:startIcon",
        "startWindowBackground": "$color:start_window_background",
        "exported": true,
        "skills": [
          {
            "entities": [
              "entity.system.home"
            ],
            "actions": [
              "action.system.home"
            ]
          }
        ]
      }
    ],

    // ===== 权限声明（SPEC §3.2 的 5 个权限，全部登记）=====
    // INTERNET：normal 级，无需 reason/usedScene，声明即可生效
    // 其余 4 个：user_grant 级，必须带 reason（引用 $string 资源）与 usedScene
    "requestPermissions": [
      {
        "name": "ohos.permission.INTERNET"
      },
      {
        "name": "ohos.permission.READ_IMAGEVIDEO",
        "reason": "$string:reason_read_imagevideo",
        "usedScene": {
          "abilities": [
            "EntryAbility"
          ],
          "when": "inuse"
        }
      },
      {
        "name": "ohos.permission.CAMERA",
        "reason": "$string:reason_camera",
        "usedScene": {
          "abilities": [
            "EntryAbility"
          ],
          "when": "inuse"
        }
      },
      {
        "name": "ohos.permission.MICROPHONE",
        "reason": "$string:reason_microphone",
        "usedScene": {
          "abilities": [
            "EntryAbility"
          ],
          "when": "inuse"
        }
      },
      {
        "name": "ohos.permission.NOTIFICATION_CONTROLLER",
        "reason": "$string:reason_notification",
        "usedScene": {
          "abilities": [
            "EntryAbility"
          ],
          "when": "inuse"
        }
      }
    ]
  }
}
```

### 7.1 权限与 SPEC §3.2 对照表

| 权限 | 用途（SPEC 原文） | 触发 Part | 级别 | 备注 |
|---|---|---|---|---|
| `ohos.permission.INTERNET` | 全部网络请求 | Part 3 | normal | 直接声明即可 |
| `ohos.permission.READ_IMAGEVIDEO` | 选头像 / 上传资质证书 | Part 4 | user_grant | 需 `reason` + `usedScene`，运行时还需动态申请 |
| `ohos.permission.CAMERA` | 拍摄资质证书 | Part 4 | user_grant | 同上 |
| `ohos.permission.MICROPHONE` | 语音消息录制 | Part 11 | user_grant | 同上 |
| `ohos.permission.NOTIFICATION_CONTROLLER` | 课程 / 作业提醒 | Part 12 | ⚠️ 存疑，见下 | ⚠️ **见 7.3** |

### 7.2 需要同步补的字符串资源

> **这个文件解决什么问题**：`reason` 必须是 `$string:xxx`，所以要往字符串资源里加对应条目，否则编译会报「资源未定义」。

路径：`client/entry/src/main/resources/base/element/string.json`

在现有 `string` 数组里**追加**（不要覆盖模板原有条目）：

```json
{
  "name": "reason_read_imagevideo",
  "value": "用于选择头像与上传资质证书照片"
},
{
  "name": "reason_camera",
  "value": "用于拍摄教师资质证书照片"
},
{
  "name": "reason_microphone",
  "value": "用于录制发送语音消息"
},
{
  "name": "reason_notification",
  "value": "用于接收上课与作业提醒通知"
}
```

### 7.3 ⚠️ 关于 `NOTIFICATION_CONTROLLER` 的重要不确定提示

⚠️ **我不确定 `ohos.permission.NOTIFICATION_CONTROLLER` 是否对普通三方应用开放**。据我所知：

- 该权限历史上多为**系统应用（system_basic 级别）**使用，用于**管理**通知（增删改系统通知）；
- 普通三方应用**发布**通知，在较新版本（含 API 12 / HarmonyOS NEXT 方向）通常是通过 `@ohos.notificationManager` 的 **`requestEnableNotification()`** 申请「通知使能」，而**不一定**需要声明该权限；
- **后果**：若该权限是受限系统权限，写进 `module.json5` 可能导致**签名 / 安装失败**，或声明后被系统忽略、始终未授权。

**建议做法**：
1. **先按 SPEC §3.2 把 5 个权限都写上**（SPEC 是权威源），先验证能否正常签名安装；
2. 若出现签名/安装失败且报错指向该权限，**改为只声明前 4 个**，Part 12 的通知走 `notificationManager.requestEnableNotification()` 的运行时申请路线；
3. 无论如何，**动手前先在 DevEco 里对该权限名做一次索引/校验**（DevEco 对未识别或未申请到的权限通常会给出提示），并把结论回填到 SPEC 或本节。
> 这是一处**我无法在当前环境确认**的事实，请以你本地 DevEco 的权限校验结果和官方文档为准。

---

## 8. `build-profile.json5` / `oh-package.json5` / `main_pages.json`

> ⚠️ 本节内容**以 DevEco 5.0(API 12) 模板生成物为基准**。若你的模板生成的字段略有差异，**以模板为准，只改必要的口径字段**（`compatibleSdkVersion`、`bundleName`）。

### 8.1 根 `build-profile.json5`

> **这个文件解决什么问题**：工程级构建配置——声明兼容 SDK 版本、构建产物（product）、签名配置（Part 6 自动签名会回填）、以及模块（entry）挂载关系。

路径：`client/build-profile.json5`

```json5
{
  "app": {
    // 签名配置：初始为空数组；执行 §6.3 自动签名后 DevEco 会自动回填一条名为 "default" 的配置
    "signingConfigs": [],
    "products": [
      {
        "name": "default",
        // ⚠️ 完成自动签名后此处会被填入 "default"；未签名时该字段可能不存在
        "signingConfig": "default",
        // 【口径】与 SPEC §3.2 一致，不要改
        "compatibleSdkVersion": "5.0.0(12)",
        // HarmonyOS 应用；若面向 OpenHarmony 则为 "OpenHarmony"（本项目为 HarmonyOS）
        "runtimeOS": "HarmonyOS",
        "buildOption": {
          "strictMode": {
            "caseSensitiveCheck": true,
            "useNormalizedOHMUrl": true
          }
        }
      }
    ],
    "buildModeSet": [
      { "name": "debug" },
      { "name": "release" }
    ]
  },
  "modules": [
    {
      "name": "entry",
      "srcPath": "./entry",
      "targets": [
        {
          "name": "default",
          "applyToProducts": ["default"]
        }
      ]
    }
  ]
}
```

> ⚠️ **未签名前**：若 `signingConfig: "default"` 报「找不到签名配置」，把这一行**先删掉**（或整段 `signingConfigs` 保持空数组），等 §6.3 自动签名回填后再保留。

> ⚠️ **不要写 `compileSdkVersion`（Part 0 实测补充）**：HarmonyOS 工程里 hvigor 强制以**本机已安装的** SDK 作为编译 SDK。
> 若显式写 `"compileSdkVersion": "5.0.0(12)"`，会撞上 `ValidateUtil.integrationVersionCheck`
> 的 `UNSUPPORTED_COMPILESDKVERSION` 而直接终止构建——**`compileSdkVersion` 必须等于 `VersionConst.SUPPORT_COMPILE_VERSION`**，
> 即本机 SDK 的 `${平台版本}(${API})`。因此本工程**只声明 `compatibleSdkVersion`（= `5.0.0(12)`，SPEC 口径不变）**，
> `compileSdkVersion` 留空、由 hvigor 用本机 SDK 兜底。完整实测偏差见 `SPEC.md` §2.1.1。

### 8.2 `oh-package.json5`

**工程根** `client/oh-package.json5`：

> **这个文件解决什么问题**：声明工程级的 ohpm 依赖与模型版本；`modelVersion` 需与 API 12 对应。

```json5
{
  "modelVersion": "5.0.0",
  "description": "倾心家教 · 鸿蒙客户端",
  "dependencies": {},
  "devDependencies": {
    // ⚠️ 版本号以 DevEco 模板生成值为准；下方仅为常见默认值，若模板不同请保留模板值
    "@ohos/hypium": "1.0.19"
  }
}
```

**模块级** `client/entry/oh-package.json5`：

```json5
{
  "name": "entry",
  "version": "1.0.0",
  "description": "倾心家教 · entry 模块",
  "main": "",
  "author": "",
  "license": "",
  "dependencies": {}
}
```

> ⚠️ `@ohos/hypium` 版本、`modelVersion` 值请以 DevEco 模板实际生成为准，不要照抄。本项目**不引入任何三方图表库**（SPEC 风险表第 5 条：图表全部 Canvas 自绘），因此 `dependencies` 保持为空是正确的。

### 8.3 `main_pages.json`

> **这个文件解决什么问题**：登记「可以被路由直接到达的页面」清单。只有登记在此的页面才能作为路由目标被加载。SPEC Part 3 规定 `pages/Index.ets` 为唯一启动页。

路径：`client/entry/src/main/resources/base/profile/main_pages.json`

```json
{
  "src": [
    "pages/Index"
  ]
}
```

> 后续每新增一个**路由可达页**（如 `pages/auth/Login`），就在这里追加一行 `"pages/auth/Login"`。
> 注意：`views/` 下的 Tab 内容视图**不是路由页**，**不要**登记到 `main_pages.json`（SPEC §3.2 已区分 `pages/` 与 `views/`）。

---

## 9. 常量起点：`request.ets` 里的 `BASE_URL`

> 对应 SPEC §8 Part 3 实现方式 3（单点 `BASE_URL` 常量注入）与 §2.2（严禁 `127.0.0.1`）。Part 0 阶段可先在 `Index.ets` 里用这个常量做连通性验证。

### 9.1 为什么不能用 `127.0.0.1` / `localhost`

> 这是 SPEC §2.2 明令禁止的红线，原理必须搞清楚：

- `127.0.0.1` / `localhost` 是**回环地址**，永远指向**发出请求的那台设备自身**；
- 请求是在**鸿蒙模拟器**里发出的，所以 `127.0.0.1:8080` 指的是**模拟器自己的 8080 端口**——而服务端跑在**宿主机**上，模拟器的 8080 上什么都没有；
- 结果必然是 `Connection refused` / 超时；
- 因此客户端**必须**写**宿主机的局域网 IPv4**，让请求穿透到宿主机。
- （⚠️ 其他平台模拟器有时提供 `10.0.2.2` 这类宿主机别名，但 **SPEC 明确要求用局域网 IP**，本项目**不依赖**任何别名，请按 SPEC 执行。）

### 9.2 `BASE_URL` 写法

路径：`client/entry/src/main/ets/constants/` 或直接落在 `api/request.ets` 顶部（SPEC Part 3 要求抽成单点常量）

```ts
/**
 * 服务端基地址 —— 全工程唯一单点，换网络环境只改这一处。
 *
 * ⚠️ 必须是【宿主机局域网 IPv4】，例如 192.168.1.100 / 10.0.0.5。
 * ⚠️ 严禁写 127.0.0.1 / localhost（在模拟器里指向模拟器自身，必然连接失败）。
 * ⚠️ 换 WiFi / 换网段后此 IP 会变，需重新执行 ipconfig 查询并更新本常量。
 * ⚠️ 端口固定 8080（与 application.yml 的 server.port 一致）。
 *
 * 正确的形态：http://192.168.1.100:8080
 */
export const BASE_URL: string = 'http://192.168.1.100:8080';

/** 统一接口前缀（SPEC §7：所有业务接口以 /api 开头） */
export const API_PREFIX: string = '/api';
```

### 9.3 如何查宿主机局域网 IP

Windows 下打开 **PowerShell 或 CMD**，执行：

```bash
ipconfig
```

在输出里找到**当前正在联网的那块网卡**（`以太网` / `WLAN`）下的：

```
IPv4 地址 . . . . . . . . . . . . : 192.168.1.100
```

那一行就是宿主机局域网 IPv4。把它填进 `BASE_URL`。

**注意**：
- 有多个适配器（VMware / VirtualBox / WSL）时，**别选** `192.168.56.x`（VirtualBox 虚拟网段）、`172.x`（WSL/Docker）等虚拟网卡地址，要选**真实上网网卡**的地址；
- 换网络后 IP 会变，**重新执行 `ipconfig` 并改 `BASE_URL`**（这正是 §9.2 要把它做成单点常量的原因）；
- 详见 §10.3 的四步连通性验证。

---

## 10. 启动与自检清单

### 10.1 服务端启动

> ⚠️ 前置：数据库已按 §4 初始化；`application.yml` 的账号密码已改对。

```bash
# 方式一：开发期直接跑
cd server
mvn spring-boot:run

# 方式二：打包后跑
mvn clean package -DskipTests
java -jar target/qxjiajiao-server-1.0.0.jar
```

**启动成功标志**（日志无异常，出现类似）：

```
Tomcat started on port(s): 8080 (http)
Started QxJiaJiaoApplication in x.xxx seconds
```

### 10.2 客户端 Run

```
1. DevEco Studio 打开 client/ 工程
2. 已按 §6 配好自动签名
3. Tools > Device Manager 创建并启动 Phone 模拟器（API 12）
4. 顶部设备下拉选中该模拟器
5. 点 Run（或 Shift+F10）
6. 首次运行应看到 "Hello World"
```

### 10.3 连通性四步验证（SPEC §2.2，**Part 0 必过项**）

> 顺序不能跳，第 ④ 步之前不要写 ArkTS 业务代码。

```
① 宿主机浏览器访问  http://localhost:8080/api/ping
    → 期望：{"code":200,"msg":"success","data":"pong"}

② 宿主机浏览器改用局域网 IP 再访问一次  http://<局域网IP>:8080/api/ping
    → 期望：同上
    → 若①通②不通：多半是 Windows 防火墙拦了 8080，见下方「防火墙放行」

③ 打开模拟器里的浏览器，访问  http://<局域网IP>:8080/api/ping
    → 期望：同上
    → 若②通③不通：确认模拟器网络正常、baseUrl 确为局域网 IP（不是 127.0.0.1）

④ 以上三步全过，才回到 DevEco 写 ArkTS 代码（BASE_URL 用 §9.2 的值）
```

**防火墙放行 8080 入站**（管理员 PowerShell）：

```powershell
New-NetFirewallRule -DisplayName "qxjiajiao-server-8080" `
  -Direction Inbound -Action Allow -Protocol TCP -LocalPort 8080
```

> ⚠️ 若①就不通：检查服务端是否真的启动、是否监听 `0.0.0.0:8080`（`server.address=0.0.0.0`）、端口是否被占用（`netstat -ano | findstr :8080`）。

### 10.4 Part 0 验收对照（SPEC §8 Part 0「验收成果」逐条核对）

| # | 验收项 | 自检方式 | 状态 |
|---|---|---|---|
| 1 | `git status` 干净；`.gitignore` 已排除 `build/`、`oh_modules/`、`node_modules/`、`target/`、`.hvigor/`、`.idea/`、`server/src/main/resources/upload/` | 对照仓库现有 `.gitignore`（已包含上述项） | [ ] |
| 2 | DevEco 能 `assembleHap` 成功；模拟器启动后显示 "Hello World" | §10.2 | [ ] |
| 3 | `mvn spring-boot:run` 启动成功，日志无异常 | §10.1 | [ ] |
| 4 | 宿主机浏览器访问 `http://localhost:8080/` 有响应 | §10.3 ① | [ ] |
| 5 | **模拟器浏览器能访问 `http://<局域网IP>:8080/`**（四步全过） | §10.3 ③ | [ ] |
| 6 | 已 `push` 到远端，远端可见首次提交 | SPEC §9.3 收尾动作 | [ ] |

**Part 0 提交信息**（SPEC 原文）：`chore: 初始化双端工程与仓库结构`

### 10.5 常见坑速查

| 现象 | 最可能原因 | 处理 |
|---|---|---|
| DevEco 构建失败 | **中文路径** | 迁到 `D:\dev\qingxin-jiajiao\`（SPEC §2.3、风险表第 1 条） |
| Run 报签名错误 | 未配签名 | 见 §6 |
| 模拟器请求连接失败 | 用了 `127.0.0.1` | 改用局域网 IP（§9.1） |
| 宿主机通、模拟器不通 | 防火墙 / IP 选错网卡 | §10.3 防火墙放行 + `ipconfig` 重选 |
| 服务端起不来，报 Springfox / documentationPlugins | 缺 `ant_path_matcher` | 已在 §2 yml 配好 |
| 中文字段乱码 | 库/连接非 utf8mb4 | 核对 §4.1 建库语句与 §2 数据源 URL |
| 上传的图片 404 | 静态资源未映射 / 目录非绝对路径 | 核对 §3.3 的 `/upload/**` 映射与 §2 的 `upload.dir` |

---

**文档版本**：v1.0　**依据 SPEC**：v1.0（2026-10-14）
**变更规则**：本文件是对 SPEC 的落地细化；若与 SPEC 冲突，**以 SPEC 为准**，并回改本文件。
