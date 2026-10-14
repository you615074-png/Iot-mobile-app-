# SPEC —— 「倾心家教」鸿蒙移动应用开发（物联网移动应用开发 · 大作业一）

> **开发模式**：单人全栈开发
> **环境**：本地模拟器 + 本地编译器 + 本地数据库，全程离线可开发
> **进度管理**：每个 Part 完成后 `git commit` + `git push` 到远端仓库
> **本文件地位**：唯一任务权威源（Single Source of Truth）。任何实现与本文冲突时，改代码或先改本文，不允许代码与 SPEC 长期不一致。

---

## 目录

- [1. 项目概述](#1-项目概述)
- [2. 本地开发环境](#2-本地开发环境)
- [3. 仓库与目录结构](#3-仓库与目录结构)
- [4. 角色与权限矩阵](#4-角色与权限矩阵)
- [5. 功能范围](#5-功能范围)
- [6. 数据库设计](#6-数据库设计)
- [7. 接口清单](#7-接口清单)
- [8. 任务分解 Part 0 ~ Part 15](#8-任务分解)
- [9. Git 工作流](#9-git-工作流)
- [10. 全局验收清单](#10-全局验收清单)
- [11. 风险与降级方案](#11-风险与降级方案)
- [12. 附录：枚举与字典](#12-附录枚举与字典)

---

## 1. 项目概述

| 项 | 内容 |
|---|---|
| 课程 | 物联网移动应用开发（江苏大学 计算机科学与通信工程学院） |
| 作业 | 大作业一 |
| 题目 | **倾心家教** 鸿蒙应用开发 |
| 参考蓝本 | 《大作业一："倾心家教"移动应用开发案例参考.pdf》（Android + PHP + MySQL 教材扫描章节） |
| 客户端 | **HarmonyOS / ArkTS + ArkUI**，SDK `5.0.0(12)`，DevEco Studio |
| 服务端 | **Spring Boot + MySQL**（作业强制要求，不使用参考案例的 PHP） |
| 开发方式 | 允许 AI 辅助开发 |

### 1.1 最终交付物（4 项，缺一不可）

| # | 交付物 | 形式 | 存放位置 |
|---|---|---|---|
| 1 | 开题 PPT | `.pptx` | `docs/开题PPT/` |
| 2 | 系统演示 | 现场演示 + 录屏 `.mp4` | `docs/演示/` |
| 3 | 课程报告 | `.docx`，**≥ 25 页** | `docs/报告/` |
| 4 | 答辩 PPT | `.pptx` | `docs/答辩PPT/` |

### 1.2 与参考案例的差异（必须在报告/答辩中明确说明）

| 维度 | 参考案例（PDF） | 本项目 |
|---|---|---|
| 客户端 | Android (Java) + XML | HarmonyOS (ArkTS) + ArkUI |
| 服务端 | PHP 脚本 + Apache | Spring Boot + MyBatis-Plus |
| 会话标识 | Intent 透传 `phone` | JWT Token |
| 角色 | **仅家长 1 种** | **教师 / 家长 / 学生 / 管理员 4 种** |
| 支付 | 无 | 模拟微信/支付宝 + 单次/套餐 |
| 沟通 | 无 | WebSocket 实时聊天 + 学习报告 + 提醒 |
| 数据分析 | 无 | 三维度数据看板 |

---

## 2. 本地开发环境

### 2.1 工具清单

| 用途 | 工具 | 版本要求 | 验证命令 |
|---|---|---|---|
| 鸿蒙 IDE | DevEco Studio | 5.0 及以上（支持 API 12） | 启动后 `Help > About` |
| 鸿蒙 SDK | HarmonyOS SDK | 目标 API 12；**实际编译 SDK = 本机已装的那个**（见下方「§2.1.1 实测偏差」） | DevEco `SDK Manager` |
| 鸿蒙模拟器 | DevEco 内置 Emulator | Phone 设备（**镜像 API 版本随本机已装 SDK**） | `Device Manager` 能启动 |
| 鸿蒙构建 | hvigorw / ohpm | DevEco 自带 | `hvigorw -v` |
| 服务端 JDK | JDK | **8** 或 17（与 Spring Boot 版本配套） | `java -version` |
| 服务端构建 | Maven | 3.8+ | `mvn -v` |
| 数据库 | MySQL | 8.0 | `mysql --version` |
| 数据库客户端 | MySQL Workbench / Navicat / DBeaver | 任意 | 能连上 3306 |
| 版本控制 | Git | 2.30+ | `git --version` |
| 作图 | Visio（或 draw.io 导出 Visio 兼容格式） | 任意 | —— |

#### 2.1.1 实测偏差（Part 0 落地时发现，**已有代码级证据，不是推测**）

| # | SPEC 原口径 | 本机实测 | 处置 | 证据来源 |
|---|---|---|---|---|
| 1 | 鸿蒙 SDK `5.0.0(12)` | 本机**只装了 API 24**（HarmonyOS 6.1.1，`6.1.1.125`） | `compatibleSdkVersion` 仍写 **`5.0.0(12)`**（SPEC 口径保留，语义＝「可运行于 API 12 设备」）；**不写 `compileSdkVersion`** | `sdk\default\sdk-pkg.json` → `"apiVersion":"24"` |
| 2 | （未提） | **HarmonyOS 工程不允许自定义 `compileSdkVersion`**：hvigor 对 `runtimeOS: "HarmonyOS"` 强制以本机已装 SDK 编译，显式写别的版本会被 `ValidateUtil.integrationVersionCheck` 判 `UNSUPPORTED_COMPILESDKVERSION` 直接终止 | `client/build-profile.json5` **不写**该字段；改在 `build-profile.json5` 内就地注释说明 | `hvigor-ohos-plugin\src\utils\validate\validate-util.js` |
| 3 | 模拟器：Phone 设备 | 本机已部署 6 个镜像，**全部是 API 24**（`Pura` / `Pura 90` / `Mate X7` / …），**没有 API 12 镜像**；补装 API 12 镜像需登录华为账号走 SDK Manager | 四步验证第 ③ 步在 **API 24 模拟器**上做（向下兼容，能装 `compatibleSdkVersion=12` 的应用）。若答辩要求严格对齐 API 12，需另行下载镜像 | `%LOCALAPPDATA%\Huawei\Emulator\deployed\lists.json` → 每项 `"apiVersion":"24"` |
| 4 | `modelVersion` 与 API 12 对应 | hvigor 6.24.4 要求 `modelVersion ∈ [5.0.0, 6.1.1]`，且 `oh-package.json5` 与 `hvigor\hvigor-config.json5` 两处**必须完全一致** | 取 **`"5.0.0"`**（满足区间，且与 API 12 口径一致） | `version-const.js` → `MINIMUM_MODEL_VERSION="5.0.0"`；`validate-util.js` → `modelVersionCheck()` |

> **结论**：SPEC 关于「目标 API 12」的**业务语义完整保留**（`compatibleSdkVersion: "5.0.0(12)"` 被 hvigor 接受并写进了 `build-profile.json5`），
> 只有「用 API 12 SDK 去编译」这一条在本机工具链下不可能成立——这是 DevEco 6.x 单 SDK 模型的硬约束，不是本项目的实现问题。

**版本口径决定**（Part 0 开工前必须二选一定死，不允许中途换）：

- **方案 A（稳妥，推荐）**：`JDK 8` + `Spring Boot 2.7.18`
- **方案 B（新）**：`JDK 17` + `Spring Boot 3.2.x`

> 本 SPEC 之后的所有代码示例与依赖，按**方案 A**书写。选 B 时只需替换 Spring Boot 父版本、`javax.*` → `jakarta.*`。

### 2.2 网络连通（最容易卡住的一环，先打通再写代码）

```
[HarmonyOS 模拟器] ──HTTP──> [宿主机 Spring Boot :8080] ──JDBC──> [本地 MySQL :3306]
```

| 项 | 规则 |
|---|---|
| 服务端监听 | `server.address=0.0.0.0`，端口 `8080` |
| MySQL | 仅本机使用，`3306`，不需要对外开放 |
| 客户端 baseUrl | **宿主机局域网 IPv4**，例如 `http://192.168.1.100:8080` |
| 严禁 | 客户端写 `127.0.0.1` / `localhost`（在模拟器里指向模拟器自身，必然失败） |
| 验证顺序 | ① 宿主机浏览器访问 `http://localhost:8080/api/ping` → ② 用局域网 IP 再访问一次 → ③ 浏览器进模拟器访问 `http://<局域网IP>:8080/api/ping` → ④ 才写 ArkTS 代码 |
| 防火墙 | Windows 防火墙需放行 8080 入站 |

> 局域网 IP 会随网络环境变化。**必须**把 baseUrl 抽成单点常量，换网络只改一处。

### 2.3 中文路径风险（务必处理）

当前工程位于 `C:\Users\28451\Desktop\物联网移动应用开发\qingxin-jiajiao\`，**路径含中文**。hvigor/ohpm 与部分 Maven 插件在 Windows 中文路径下存在构建失败记录。

处理方式（任选其一，Part 0 内完成）：

1. **推荐**：把工程移到纯 ASCII 路径，例如 `D:\dev\qingxin-jiajiao\`，开发全程在 ASCII 路径下进行；
2. 保留现路径，但开启 Windows「区域设置 → 管理 → 更改系统区域设置 → Beta: 使用 Unicode UTF-8 提供全球语言支持」，重启后验证一次 `hvigorw assembleHap` 能否通过。

> 若 Part 0 的构建验证失败，**优先怀疑中文路径**，不要先去改代码。

---

## 3. 仓库与目录结构

### 3.1 仓库根

```
qingxin-jiajiao/                    ← git 仓库根（本文件夹）
├── SPEC.md                         ← 本文件
├── README.md                       ← 项目简介 + 启动步骤
├── .gitignore
├── client/                         ← 鸿蒙客户端（DevEco 工程根）
├── server/                         ← Spring Boot 服务端（Maven 工程根）
├── docs/                           ← 交付物与素材
│   ├── 报告/                       课程报告 .docx + 图片子目录
│   ├── 开题PPT/
│   ├── 答辩PPT/
│   ├── 演示/                       录屏、演示脚本
│   ├── diagrams/                   Visio 源文件（架构图/ER图/流程图/UML）
│   └── screenshots/                界面截图（按 Part 分目录）
└── scripts/                        ← 辅助脚本（启停、造数据、导出）
```

### 3.2 客户端目录（`client/`）

由 DevEco Studio 生成 `AppScope/`、`entry/`、`hvigor/`、`oh-package.json5` 后，**在此之上手工建包**：

```
client/
├── AppScope/app.json5                      bundleName: com.jsu.qxjiajiao
├── build-profile.json5                     compatibleSdkVersion: 5.0.0(12)
├── oh-package.json5
├── hvigorfile.ts
└── entry/
    ├── oh-package.json5
    ├── build-profile.json5
    └── src/main/
        ├── module.json5                    ⭐权限声明
        ├── resources/
        │   ├── base/element/{string,color}.json
        │   ├── base/media/                 图标、占位图
        │   └── base/profile/main_pages.json
        └── ets/
            ├── entryability/EntryAbility.ets
            ├── pages/                      ← 路由可达页（@Entry）
            ├── views/                      ← Tab 内容视图（非路由）
            ├── components/                 ← 可复用 UI 组件
            ├── api/                        ← 网络层
            ├── models/                     ← 类型定义
            ├── constants/                  ← 常量 / 枚举 / 字典 / 主题
            ├── utils/                      ← 工具函数
            ├── storages/                   ← 全局状态（AppStorage）
            └── router/                     ← Navigation 路由表
```

包内详细文件清单见 [第 8 章](#8-任务分解) 各 Part 的「路径」段落。

**⚠️ 权限第 5 项 `ohos.permission.NOTIFICATION_CONTROLLER` 有风险**（见 `SCAFFOLD.md` §7.3）：该权限可能属 `system_basic` 级，普通三方应用写进 `module.json5` 会导致**签名失败**。**先只声明前 4 项**，通知改用运行时 `notificationManager.requestEnableNotification()` 拉起授权弹窗。Part 12 如遇签名失败，优先删掉这一项。

#### 3.2.1 客户端完整页面清单

**逐页布局稿、色值字号、四态规范见 [`UI.md`](./UI.md)**——本文档只给路径索引，避免两处维护。

共 **40 个页面/视图**。下表列出 `SPEC.md` 原始规划**未包含、由 `UI.md` 补充**的 9 个（其余 31 个见各 Part 路径段落）：

| 路径 | 作用 | 归属 Part | 为什么必须有 |
|---|---|---|---|
| `pages/Main.ets` | 家长/学生 Tab 容器 | P3 | 三个 Tab 视图需要宿主页 |
| `pages/TeacherMain.ets` | 教师 Tab 容器 | P4 | 同上 |
| `pages/AdminMain.ets` | 管理员 Tab 容器 | P5 | 同上 |
| `views/mine/MineView.ets` | 「我的」Tab 内容视图 | P4 | SPEC 只列了 `pages/mine/*` 子页，缺 Tab 容器本身 |
| `views/chat/MessageView.ets` | 「消息」Tab 内容视图 | P11 | Part 11 要求「底栏消息 Tab 显示未读数」，缺 Tab 容器本身 |
| `views/teacher/TeacherWorkbenchView.ets` | 教师工作台 Tab | P4 | Part 4 明确要求「教师→工作台」，但未给路径 |
| `pages/mine/ProfileEdit.ets` | 个人资料编辑页 | P4 | §7 接口 11「改个人资料」无对应界面 |
| `pages/tutor/FavoriteList.ets` | 我的收藏列表 | P7 | §7 接口 30「我的收藏」无对应界面 |
| `pages/report/ReportPublish.ets` | 教师发布学习报告 | P12 | §7 接口 55「教师提交报告」无对应界面 |

> **Tab 结构**（`UI.md` §3.2）：家长/学生 = 首页·消息·我的；教师 = 工作台·消息·我的；管理员 = **审核管理·我的（2 个，不硬凑 3 个）**。

**模块权限**（`module.json5` 的 `requestPermissions`）：

| 权限 | 用途 | 触发 Part |
|---|---|---|
| `ohos.permission.INTERNET` | 全部网络请求 | Part 3 |
| `ohos.permission.READ_IMAGEVIDEO` | 选头像 / 上传资质证书 | Part 4 |
| `ohos.permission.CAMERA` | 拍摄资质证书 | Part 4 |
| `ohos.permission.MICROPHONE` | 语音消息录制 | Part 11 |
| `ohos.permission.NOTIFICATION_CONTROLLER` | 课程/作业提醒 | Part 12 |

### 3.3 服务端目录（`server/`）

```
server/
├── pom.xml
├── src/main/java/com/jsu/qxjiajiao/
│   ├── QxJiaJiaoApplication.java
│   ├── common/
│   │   ├── Result.java  ResultCode.java  PageResult.java
│   │   ├── exception/   BizException.java  GlobalExceptionHandler.java
│   │   ├── config/      WebMvcConfig.java  MybatisPlusConfig.java
│   │   │                Knife4jConfig.java  WebSocketConfig.java
│   │   ├── interceptor/ JwtInterceptor.java
│   │   └── util/        JwtUtil.java  FileUtil.java  DateUtil.java
│   ├── controller/
│   ├── service/ + service/impl/
│   ├── mapper/
│   ├── entity/
│   ├── dto/  vo/
│   └── websocket/       ChatWsServer.java
└── src/main/resources/
    ├── application.yml
    ├── mapper/*.xml
    ├── db/schema.sql    db/data.sql
    └── upload/          （运行时生成，不入库）
```

包名统一 `com.jsu.qxjiajiao`；客户端 bundleName 与服务端包名保持一致，便于识别。

---

## 4. 角色与权限矩阵

四种角色。**注册即可选家长 / 学生 / 教师；管理员账号由 `data.sql` 预置，不开放注册。**

| 功能域 | 家长 | 学生 | 教师 | 管理员 |
|---|---|---|---|---|
| 注册 / 登录 | ✅ | ✅ | ✅ 需上传资质 | ❌ 预置账号 |
| 实名认证（绑身份证） | ✅ 必填 | ❌ | ✅ 必填 | —— |
| 资质审核 | —— | —— | 提交后等待 | **✅ 通过 / 驳回** |
| 首页浏览 | ✅ | ✅ | ✅ | ✅ |
| 教师检索 / 收藏 | ✅ | ✅ | 👁 只读 | ✅ |
| 预约 / 下单 / 支付 | ✅ | ❌ | ❌ | ❌ |
| 查看订单 | 自己下的 | 自己被安排的 | 接单记录 | 全部 |
| 退款申请 | ✅ | ❌ | ❌ | 审核 |
| 聊天 | ✅ 与教师 | ✅ 与教师 | ✅ 与家长 | ❌ |
| 提交学习反馈报告 | 👁 查看 | 👁 查看 | **✅ 提交** | 👁 查看 |
| 提醒中心 | ✅ | ✅ | ✅ | ✅ |
| 数据看板 | ❌ | ❌ | 👁 个人表现 | **✅ 全量** |
| 用户管理 | ❌ | ❌ | ❌ | ✅ 启用/禁用 |
| 建议反馈 | ✅ | ✅ | ✅ | ✅ 查看 |

**登录方式**：手机号 + 验证码（模拟）／账号密码；微信、QQ 登录**以 mock 形式实现**（点击后直接以预置 openid 登录，不接真 SDK）。此决策需在报告"需求分析"章说明理由。

---

## 5. 功能范围

对照《大作业一要求.docx》的四大扩展模块，本项目功能划分如下（✅ = 必须实现，🔸 = 加分项）：

### 模块 ① 用户登录模块优化
- ✅ 角色划分：教师（注册需上传资质认证）、家长、学生、管理员（审核资质）
- ✅ 手机号注册登录；微信 / QQ 登录（mock）
- ✅ 关联实名认证：家长必绑身份证；教师额外上传资质
- ✅ 个人资料：编辑昵称、头像、联系方式、地址

### 模块 ② 支付与订单管理（功能模拟）
- ✅ 多样化支付：微信 / 支付宝 / 余额，**单次支付 与 套餐支付（套餐享折扣）**
- ✅ 订单明细：课程名称、课时数、费用、支付状态、上课时间
- ✅ **订单导出**（CSV / Excel）
- ✅ 退款保障：未上课可退，明确流程与到账时间（7 个工作日）

### 模块 ③ 家长沟通与服务
- ✅ 实时聊天：文字（WebSocket）+ **语音（录音上传后播放，模拟）**，视频以模拟界面呈现 🔸
- ✅ 学习反馈：教师定期提交学习进度报告、课堂表现分析
- ✅ 课程提醒：上课提醒、作业提交提醒（本地通知 + 提醒中心列表）

### 模块 ④ AI 智能增值分析
- ✅ 核心数据看板：用户增长数、订单量、交易额、复购率
- ✅ 家教表现分析：好评率、接单率、退课率
- ✅ 家长需求分析：热门学科、年级分布、价格敏感度

### 参考案例保留功能
- ✅ 首页轮播 + 科目九宫格（语文/数学/英语/物理/政治/化学/生物/地理）
- ✅ 动态公告滚动 + 名师推荐
- ✅ 按科目 / 科目+年级组合检索教师
- ✅ 教师详情 + 预约
- ✅ 我的：订单 / 钱包 / 奖学券 / 学习计划

---

## 6. 数据库设计

**库名**：`qxjiajiao`　**字符集**：`utf8mb4` / `utf8mb4_general_ci`　**引擎**：`InnoDB`

### 6.1 相对参考案例的改进（写进报告「项目设计」章）

1. 原案例 8 张表 → 本项目 **18 张表**，补齐角色、审核、支付、聊天、分析。
2. 原案例字段名存在笔误且不统一（`phonenum` / `phoncnc` / `paret_phone` / `acount` / `data`）→ 统一为 `phone` / `parent_phone` / `amount` / `create_time`。
3. 原案例**金额全部用 `varchar` 存储** → 本项目统一 `decimal(10,2)`。
4. 原案例状态字段为自由文本 → 本项目统一为枚举常量（见第 12 章）。

### 6.2 表清单

| # | 表名 | 中文名 | 说明 |
|---|---|---|---|
| 1 | `sys_user` | 用户主表 | 四角色统一表，取代原 `parent` |
| 2 | `teacher_profile` | 教师资料表 | 扩展原 `teacher` |
| 3 | `teacher_cert` | 教师资质认证表 | 新增，支撑「上传资质 + 管理员审核」 |
| 4 | `student` | 学生表 | 新增 |
| 5 | `admin_log` | 管理员操作日志 | 新增 |
| 6 | `course` | 课程/服务表 | 扩展 |
| 7 | `course_package` | 套餐表 | 新增，支撑「套餐享折扣」 |
| 8 | `favorite` | 收藏表 | 新增 |
| 9 | `order` | 订单表 | 重构原 `par_order` |
| 10 | `wallet` | 钱包表 | 扩展原 `par_money` |
| 11 | `wallet_record` | 资金流水表 | 新增 |
| 12 | `coupon` | 奖学券表 | 重构原 `par_reward` |
| 13 | `chat_session` | 会话表 | 新增 |
| 14 | `chat_message` | 消息表 | 新增 |
| 15 | `study_report` | 学习反馈报告表 | 新增 |
| 16 | `notice` | 提醒表 | 扩展原 `plant` |
| 17 | `evaluation` | 评价表 | 扩展原 `evaluate` |
| 18 | `feedback` | 建议反馈表 | 保留原案例 |
| 19 | `daily_stat` | 看板预聚合表 | 新增 |

### 6.3 关键表字段（建表以 `db/schema.sql` 为准，此处为契约）

**1. sys_user**
`id` bigint PK AI · `phone` varchar(11) UNIQUE · `password` varchar(100)（BCrypt） · `role` varchar(20)（见 §12.1） · `nickname` · `avatar` · `real_name` · `id_card` varchar(18) · `gender` tinyint · `address` · `wx_openid` · `qq_openid` · `status` tinyint（0 正常 / 1 禁用 / 2 待审核） · `create_time` · `update_time` · `deleted` tinyint

**2. teacher_profile**
`id` · `user_id` FK · `subjects` varchar(200)（多科目逗号分隔） · `grades` varchar(200) · `teach_years` int · `education` · `school` · `intro` text · `price_per_hour` decimal(10,2) · `accept_rate` decimal(5,2) · `good_rate` decimal(5,2) · `refund_rate` decimal(5,2) · `order_count` int · `rating` decimal(3,2) · `create_time`

**3. teacher_cert**
`id` · `user_id` FK · `cert_type` varchar(30)（教师资格证 / 学历证书 / 身份证） · `cert_img` varchar(255) · `cert_no` · `status` varchar(20)（待审核 / 已通过 / 已驳回） · `reject_reason` · `audit_by` · `audit_time` · `create_time`

**4. student**
`id` · `parent_id` FK→sys_user · `name` · `gender` tinyint · `grade` varchar(20) · `school` · `avatar` · `create_time`

**5. admin_log**
`id` · `admin_id` · `action` · `target_type` · `target_id` · `detail` · `create_time`

**6. course**
`id` · `teacher_id` FK→sys_user · `subject` · `grade` · `title` · `cover` · `price_per_hour` decimal(10,2) · `duration_min` int · `class_type`（线上 / 线下） · `intro` · `status` tinyint · `create_time`

**7. course_package**
`id` · `course_id` FK · `lesson_count` int · `origin_price` decimal(10,2) · `package_price` decimal(10,2) · `discount_rate` decimal(5,2) · `valid_days` int · `create_time`

**8. favorite**
`id` · `user_id` · `teacher_id` · `create_time`　（`user_id + teacher_id` 唯一索引）

**9. order**（核心表）
`id` · `order_no` varchar(32) UNIQUE · `parent_id` · `student_id` · `teacher_id` · `course_id` · `package_id` · `course_name`（冗余快照） · `lesson_count` int · `unit_price` decimal(10,2) · `total_amount` decimal(10,2) · `pay_amount` decimal(10,2) · `discount_amount` decimal(10,2) · `coupon_id` · `pay_type`（WECHAT / ALIPAY / BALANCE） · `pay_status`（未支付 / 已支付 / 已退款） · `order_status`（待支付 / 已支付 / 上课中 / 已完成 / 已取消 / 退款中 / 已退款） · `class_time` datetime · `pay_time` · `refund_reason` · `refund_amount` · `refund_time` · `remark` · `create_time` · `deleted`

**10. wallet**：`id` · `user_id` UNIQUE · `total_amount` · `balance` · `points` int · `update_time`

**11. wallet_record**：`id` · `user_id` · `biz_type`（充值 / 支付 / 退款 / 奖励） · `amount` decimal(10,2) · `balance_after` · `order_no` · `remark` · `create_time`

**12. coupon**：`id` · `user_id` · `name` · `amount` decimal(10,2) · `min_amount` decimal(10,2) · `expire_time` date · `status`（未使用 / 已使用 / 已过期） · `create_time`

**13. chat_session**：`id` · `user_a_id` · `user_b_id` · `last_msg` varchar(255) · `last_time` datetime · `unread_a` int · `unread_b` int · `create_time`　（`user_a_id + user_b_id` 唯一索引，入库前按小 ID 在前归一化）

**14. chat_message**：`id` · `session_id` FK · `from_id` · `to_id` · `msg_type`（TEXT / VOICE / IMAGE / VIDEO） · `content` text · `media_url` · `duration` int（秒） · `is_read` tinyint · `create_time`

**15. study_report**：`id` · `student_id` · `teacher_id` · `parent_id` · `period` varchar(30)（如 2026-10 第 2 周） · `summary` text · `performance` text · `score` decimal(4,1) · `attitude` varchar(20) · `homework_rate` decimal(5,2) · `suggestion` text · `is_read` tinyint · `create_time`

**16. notice**：`id` · `user_id` · `notice_type`（CLASS_REMIND / HOMEWORK_REMIND / SYSTEM） · `title` · `content` · `biz_id`（关联 order_id 等） · `remind_time` datetime · `status`（未读 / 已读） · `create_time`

**17. evaluation**：`id` · `order_id` UNIQUE · `parent_id` · `teacher_id` · `score` tinyint（1~5） · `tags` varchar(200) · `content` · `create_time`

**18. feedback**：`id` · `user_id` · `subject` varchar(50) · `title` · `content` · `reply` · `reply_time` · `create_time`

**19. daily_stat**：`id` · `stat_date` date UNIQUE · `new_user` int · `active_user` int · `order_count` int · `trade_amount` decimal(12,2) · `repurchase_rate` decimal(5,2) · `create_time`

### 6.4 初始化数据（`db/data.sql` 必须包含）

| 数据 | 数量 | 说明 |
|---|---|---|
| 管理员账号 | 1 | `13800000000` / `admin123`，`role=ADMIN` |
| 家长账号 | 2 | 含 1 个已实名 |
| 学生 | 3 | 挂到家长下 |
| 教师账号 | 4 | 2 个已通过资质、1 个待审核、1 个已驳回 |
| 教师资料 | 4 | 覆盖不同科目与年级 |
| 课程 | 8 | 每教师 2 门，含单次与套餐 |
| 套餐 | 4 | 折扣率 0.85 / 0.80 |
| 订单 | 10 | 覆盖各状态（待支付/已支付/已完成/退款中/已退款） |
| 评价 | 6 | 用于算好评率 |
| 聊天记录 | 20 | 2 个会话 |
| 学习报告 | 5 | 不同学生 |
| 提醒 | 8 | 上课/作业/系统各若干 |
| 奖学券 | 4 | 含 1 张已过期 |
| `daily_stat` | 30 天 | 供看板画折线图 |

> 演示数据量必须足以让**每个看板页面都有可见曲线/占比**，否则演示时是空白页 —— 这是最常见的演示翻车点。

---

## 7. 接口清单

**统一前缀** `/api`。**统一响应体**：`{ "code": 200, "msg": "success", "data": {...} }`。
**认证方式**：`Authorization: Bearer <token>`（登录、注册、发送验证码接口除外）。
**分页参数**：`page`（从 1 开始）、`size`（默认 10）；**分页响应**：`{ "total": 0, "records": [] }`。

> **本表是索引，字段级契约（请求体/响应体/错误码）见 [`API.md`](./API.md)。**
> 本表编号与 `API.md` §6 附录一一对应。**新增接口必须先改本表再改 `API.md`。**

| # | 方法 | 路径 | 说明 | Part |
|---|---|---|---|---|
| 1 | GET | `/api/ping` | 连通性探针（不改动、不需登录） | P2 |
| 2 | POST | `/api/auth/sms` | 发送验证码（模拟，固定 `123456`） | P4 |
| 3 | POST | `/api/auth/register` | 注册（含角色、实名、资质图片） | P4 |
| 4 | POST | `/api/auth/login` | 账号密码登录 | P4 |
| 5 | POST | `/api/auth/login/sms` | 验证码登录 | P4 |
| 6 | POST | `/api/auth/login/wx` | 微信登录（mock） | P4 |
| 7 | POST | `/api/auth/login/qq` | QQ 登录（mock） | P4 |
| 8 | GET | `/api/auth/me` | 当前登录用户信息（冷启动校验 Token） | P4 |
| 9 | POST | `/api/auth/logout` | 登出 | P4 |
| 10 | GET | `/api/user/profile` | 查个人资料 | P4 |
| 11 | PUT | `/api/user/profile` | 改个人资料 | P4 |
| 12 | POST | `/api/user/realname` | 提交/修改实名信息 | P4 |
| 13 | GET | `/api/user/students` | 我的孩子列表（下单时选学生） | P4 |
| 14 | POST | `/api/user/students` | 新增学生档案 | P4 |
| 15 | POST | `/api/file/upload` | 通用文件上传（头像/证书/语音） | P2 |
| 16 | POST | `/api/teacher/cert` | 提交教师资质 | P4 |
| 17 | GET | `/api/teacher/cert/my` | 查我的资质状态 | P4 |
| 18 | GET | `/api/teacher/profile` | 查我的教师资料 | P4 |
| 19 | PUT | `/api/teacher/profile` | 改教师资料（科目/年级/时薪/简介） | P4 |
| 20 | GET | `/api/admin/cert/list` | 资质待审列表（分页、按状态筛） | P5 |
| 21 | POST | `/api/admin/cert/audit` | 审核通过 / 驳回（带原因） | P5 |
| 22 | GET | `/api/admin/users` | 用户列表（按角色筛） | P5 |
| 23 | PUT | `/api/admin/user/{id}/status` | 启用 / 禁用用户 | P5 |
| 24 | GET | `/api/admin/dashboard` | 管理员首页汇总数 | P5 |
| 25 | POST | `/api/admin/feedback/reply` | 管理员回复用户建议 | P14 |
| 26 | GET | `/api/home/index` | 首页聚合：轮播图 + 公告 + 科目 + 名师推荐 | P6 |
| 27 | GET | `/api/tutor/list` | 教师检索（`subject`/`grade`/`keyword`/排序/分页） | P6 |
| 28 | GET | `/api/tutor/{id}` | 教师详情（含资料 + 课程 + 评价） | P7 |
| 29 | POST | `/api/tutor/reserve` | 预约教师（生成待支付订单） | P7 |
| 30 | GET | `/api/tutor/favorite` | 我的收藏 | P7 |
| 31 | POST | `/api/tutor/favorite` | 收藏 / 取消收藏（幂等） | P7 |
| 32 | GET | `/api/course/list` | 课程列表（按教师 / 科目筛） | P8 |
| 33 | GET | `/api/course/{id}` | 课程详情（含套餐） | P8 |
| 34 | GET | `/api/course/{id}/packages` | 该课程套餐 | P8 |
| 35 | POST | `/api/order/preview` | 下单预览（算价、可用券、折扣） | P8 |
| 36 | POST | `/api/order/create` | 创建订单（服务端重算金额） | P8 |
| 37 | POST | `/api/pay/prepay` | 发起支付（返回模拟支付参数） | P9 |
| 38 | POST | `/api/pay/callback` | 支付回调（模拟成功，**幂等**） | P9 |
| 39 | GET | `/api/wallet` | 我的钱包（总额/余额/积分） | P9 |
| 40 | GET | `/api/wallet/records` | 资金流水 | P9 |
| 41 | GET | `/api/coupon/list` | 我的奖学券（按状态筛，惰性过期） | P9 |
| 42 | GET | `/api/order/list` | 订单列表（按状态/角色筛、分页） | P10 |
| 43 | GET | `/api/order/{orderNo}` | 订单详情 | P10 |
| 44 | POST | `/api/order/{orderNo}/cancel` | 取消订单 | P10 |
| 45 | POST | `/api/order/{orderNo}/refund` | 申请退款（模拟审核即时通过） | P10 |
| 46 | GET | `/api/order/refund/rule` | 退款规则说明（到账 7 个工作日） | P10 |
| 47 | GET | `/api/order/export` | **导出订单 CSV**（⚠️ 非统一响应体，带 BOM） | P10 |
| 48 | GET | `/api/chat/sessions` | 会话列表（含未读数） | P11 |
| 49 | GET | `/api/chat/messages` | 历史消息（按 **`peerId`** 定位会话，分页） | P11 |
| 50 | POST | `/api/chat/send` | HTTP 兜底发消息 | P11 |
| 51 | POST | `/api/chat/read` | 标记某会话已读（按 **`peerId`**） | P11 |
| 52 | WS | `/ws/chat?token=<jwt>` | WebSocket 实时收发 | P11 |
| 53 | GET | `/api/report/list` | 学习报告列表（按角色） | P12 |
| 54 | GET | `/api/report/{id}` | 学习报告详情（顺手置已读） | P12 |
| 55 | POST | `/api/report` | 教师提交学习报告 | P12 |
| 56 | GET | `/api/notice/list` | 提醒列表 | P12 |
| 57 | GET | `/api/notice/unread-count` | 未读提醒数（角标） | P12 |
| 58 | POST | `/api/notice/read` | 批量标记已读（`ids` 为空＝全部） | P12 |
| 59 | GET | `/api/analysis/overview` | 核心看板：用户增长/订单量/交易额/复购率 | P13 |
| 60 | GET | `/api/analysis/tutor` | 家教表现：好评率/接单率/退课率 | P13 |
| 61 | GET | `/api/analysis/demand` | 家长需求：热门学科/年级分布/价格敏感度 | P13 |
| 62 | POST | `/api/evaluation` | 提交评价（**回算教师评分**） | P14 |
| 63 | GET | `/api/evaluation/list` | 评价列表 | P14 |
| 64 | POST | `/api/feedback` | 提交建议 | P14 |
| 65 | GET | `/api/feedback/list` | 建议列表（管理员可筛回复状态） | P14 |

> **Knife4j** 挂载后自动生成接口文档，路径 `/doc.html`。该文档直接用于演示与报告「接口设计」章截图。

### 7.1 三条贯穿全表的接口设计约定

写代码时若与本节冲突，以本节为准。**这三条是「项目设计」章可以写的亮点，也是防两端规则漂移的机制。**

1. **状态机判断只在服务端。** `OrderVO` 返回 `canCancel` / `canRefund` / `canEvaluate` / `statusDesc`，客户端**只按布尔值显隐按钮**，不得自己写 `if (orderStatus === '已支付')`。
2. **计价算法只有一份。** `/api/order/preview` 与 `/api/order/create` 共用同一段 `BigDecimal` 逻辑；`create` 的请求体**不接收任何金额字段**，全部由服务端重算。
3. **涉及「我的数据」的查询，服务端强制以 Token 中的 `userId` 为条件**，绝不信任请求参数传来的 `userId`。

---

## 8. 任务分解

**Part = 一个可独立验收、可独立 `push` 的工作单元。**
每个 Part 统一包含四段：**目标** · **实现方式** · **路径** · **验收成果**。

Parts 依赖顺序：`P0 → P1 → P2 → P3 → P4 → … → P15`，不允许跳序；同 Part 内后端先行、客户端后接。

---

### Part 0 —— 仓库与双端工程初始化

**目标**：建立可编译、可运行、可推送的空壳双端工程。

**实现方式**
1. 在 `qingxin-jiajiao/` 执行 `git init`，配置 `.gitignore`，创建 `git remote add origin <你的远端地址>`（远端由本人创建，SPEC 不代填）。
2. 检查 §2.3 中文路径风险，必要时将工程迁到 ASCII 路径。
3. 建 `Empty Ability` 工程，工程目录指向 `client/`；`bundleName = com.jsu.qxjiajiao`，`compatibleSdkVersion = 5.0.0(12)`，设备类型 `phone`。
   > 本项目工程由 DevEco 官方 FreeMarker 模板（`plugins\openharmony\lib\templates\`）按 `command.xml.ftl` 的 file→dest 配方**手工渲染**落地，
   > 与 GUI 生成物等价；`compileSdkVersion` 按 §2.1.1 第 2 条**不写**。
4. IDEA / Eclipse 新建 Maven 工程于 `server/`，Spring Boot 版本按 §2.1 定死。
5. 编写 `README.md`：环境要求、启动步骤、目录说明。
6. 完成 §2.2 的**四步连通性验证**。

**路径**

| 端 | 路径 |
|---|---|
| 根 | `.gitignore`　`README.md` |
| 客户端 | `client/AppScope/app.json5`　`client/build-profile.json5`　`client/entry/src/main/module.json5`　`client/entry/src/main/ets/entryability/EntryAbility.ets` |
| 服务端 | `server/pom.xml`　`server/src/main/java/com/jsu/qxjiajiao/QxJiaJiaoApplication.java`　`server/src/main/resources/application.yml` |

**验收成果**
- [x] `git status` 干净，`.gitignore` 已排除 `build/`、`oh_modules/`、`node_modules/`、`target/`、`.hvigor/`、`.idea/`、`server/src/main/resources/upload/`
- [ ] DevEco 能 `assembleHap` 成功，模拟器启动后显示 "Hello World"
      —— **`assembleHap` 已过**（`BUILD SUCCESSFUL`，产物 `entry-default-unsigned.hap` 121,495 B，见 `PROGRESS.md` §3）；
      **「模拟器显示 Hello World」未过**：未签名 hap 装不进模拟器，需先按 §6.3 完成自动签名（见 `PROGRESS.md` §4 阻塞项 2/5）
- [x] `mvn spring-boot:run` 启动成功，日志无异常
- [x] 宿主机浏览器访问 `http://localhost:8080/` 有响应（HTTP 404，Spring 默认错误页；§7 无根路径接口，不新增）
- [ ] **模拟器浏览器能访问 `http://<局域网IP>:8080/`**（四步验证全过）
      —— ①② 已过（`/api/ping` 双通返回契约值）；**③ 未做**（模拟器未启动）
- [x] 已 `push` 到远端，远端可见首次提交

**提交信息**：`chore: 初始化双端工程与仓库结构`

---

### Part 1 —— 数据库建模与初始化数据

**目标**：`schema.sql` + `data.sql` 可一键执行，19 张表与演示数据到位。

**实现方式**
1. 按 §6.3 编写 `schema.sql`：建库、建 19 张表、加索引与外键。
   - 必备索引：`sys_user(phone)`、`order(order_no)`、`order(parent_id, order_status)`、`chat_message(session_id, create_time)`、`teacher_profile(user_id)`。
2. 按 §6.4 编写 `data.sql`：覆盖所有业务状态的演示数据；密码统一 BCrypt 加密后的同一个明文便于演示。
3. 用 Navicat/Workbench 画出 ER 图，**导出为 Visio 可再编辑格式**放入 `docs/diagrams/`。
4. 编写 `scripts/init-db.bat`：一键 `mysql -u root -p < schema.sql && ... data.sql`。

**路径**

| 端 | 路径 |
|---|---|
| 服务端 | `server/src/main/resources/db/schema.sql`　`server/src/main/resources/db/data.sql` |
| 脚本 | `scripts/init-db.bat` |
| 文档 | `docs/diagrams/ER图.vsdx`　`docs/diagrams/ER图.png` |

**验收成果**
- [ ] `schema.sql` 在空库上执行零报错，19 张表全部创建
- [ ] `data.sql` 执行后每张表都有数据，`SELECT COUNT(*)` 与 §6.4 一致
- [ ] 管理员账号 `13800000000 / admin123` 可查到
- [ ] ER 图画出主链路（`sys_user → teacher_profile → course → order → evaluation`）+ `wallet` 分支，导出 PNG 清晰可读（**不必画全 19 张表**，理由见 `UI.md` §8.1）
- [ ] 已 `push`

**提交信息**：`feat(db): 建立 qxjiajiao 数据库 19 张表与演示数据`

---

### Part 2 —— 服务端公共层

**目标**：所有后续接口共用的地基就位 —— 统一响应、全局异常、JWT、拦截器、跨域、接口文档、文件上传。

**实现方式**
1. `Result<T>` / `ResultCode` / `PageResult<T>` 统一响应体（§7 约定）。
2. `BizException` + `@RestControllerAdvice` 全局异常处理：业务异常返 `code=业务码`，参数校验异常返 `400`，未知异常返 `500` 且**不泄露堆栈**。
3. `JwtUtil` 生成/解析 Token（载荷放 `userId` + `role`，有效期 7 天）。
4. `JwtInterceptor`：拦截 `/api/**`，放行 `/api/auth/**`、`/api/ping`、`/doc.html`、`/api/file/**` 公开读；解析失败返 `401`。
5. `WebMvcConfig`：注册拦截器 + 全局 CORS（开发期允许所有来源）+ **静态资源映射 `/upload/**` → 本地 upload 目录**。
6. `MybatisPlusConfig`：分页插件 + 逻辑删除（`deleted` 字段）。
7. `Knife4jConfig`：接口文档分组（认证 / 用户 / 交易 / 沟通 / 分析）。
8. `FileController` + `FileUtil`：`POST /api/file/upload`，按日期分目录落盘，返回可访问 URL；限制后缀（jpg/png/webp/mp3/m4a）与大小（10MB）。
9. `GET /api/ping` 探针接口。

**路径**

| 端 | 路径 |
|---|---|
| 公共 | `common/Result.java`　`common/ResultCode.java`　`common/PageResult.java` |
| 异常 | `common/exception/BizException.java`　`common/exception/GlobalExceptionHandler.java` |
| 配置 | `common/config/{WebMvcConfig,MybatisPlusConfig,Knife4jConfig}.java` |
| 拦截 | `common/interceptor/JwtInterceptor.java` |
| 工具 | `common/util/{JwtUtil,FileUtil,DateUtil}.java` |
| 控制器 | `controller/{PingController,FileController}.java` |
| 配置 | `src/main/resources/application.yml` |

**验收成果**
- [ ] `GET /api/ping` 返回 `{"code":200,"msg":"success","data":"pong"}`
- [ ] 访问任意受保护接口不带 Token → 返回 401 且响应体是统一格式（不是 Spring 默认错误页）
- [ ] 故意抛 `BizException` → 返回业务码与中文提示
- [ ] 故意抛 `NullPointerException` → 返回 500 且**响应体中无堆栈信息**
- [ ] `http://localhost:8080/doc.html` 可打开，能看到 2 个接口
- [ ] `POST /api/file/upload` 上传一张图 → 磁盘有文件，返回 URL 能在浏览器打开
- [ ] 已 `push`

**提交信息**：`feat(server): 搭建统一响应/异常/JWT/跨域/接口文档/上传公共层`

---

### Part 3 —— 客户端基建

**目标**：客户端骨架成型 —— 路由、网络层、常量、模型、主题、全局状态、通用组件；此后每个页面只写业务。

**实现方式**
1. `module.json5` 声明 §3.2 的全部权限（本期先只加 INTERNET）。
2. **路由**：采用 `Navigation` + `NavPathStack`（不用 `router.pushUrl`）。`pages/Index.ets` 为唯一启动页，读 Token 决定跳登录还是主页。
3. **网络层** `api/request.ets`：单例封装 `http.createHttp()`，统一处理
   - 单点 `BASE_URL` 常量注入
   - 自动带 `Authorization: Bearer <token>`
   - 统一解包 `Result` 并判定 `code`
   - `401` → 清 Token 并跳登录
   - 统一失败 Toast、超时设置、可选 Loading
   - **此后任何页面禁止直接 `http.createHttp()`**
4. **常量层**：`constants/api.ets`（路径常量）、`constants/enums.ets`（角色/订单状态/支付方式，与第 12 章对齐）、`constants/dict.ets`（科目 8 项 + 年级 12 项，含图标映射）、`constants/theme.ets`（主色/字号/间距）。
5. **模型层**：`models/` 按域定义 `interface`，字段名与后端 DTO/接口清单**逐字对齐**。
6. **全局状态**：`storages/UserStore.ets` 用 `AppStorage` 存 `token` / `userInfo` / `role`；`utils/storage.ets` 用 `@ohos.data.preferences` 持久化 Token。
7. **工具层**：`utils/{format,validator,toast,permission,upload}.ets`（金额格式化、手机号/身份证校验、Toast 封装、权限申请、文件上传）。
8. **通用组件**：`components/common/` 的 `HmNavBar` / `HmTabBar` / `HmEmpty` / `HmLoading` / `HmAvatar` / `HmTag` / `HmDialog`。
9. **路由表** `router/RouterMap.ets`：集中注册所有页面路由名与参数类型。

**路径**

| 端 | 路径 |
|---|---|
| 客户端 | `entry/src/main/module.json5` |
| | `entry/src/main/ets/api/request.ets`　`api/modules/`（空壳待填） |
| | `entry/src/main/ets/constants/{api,enums,dict,theme}.ets` |
| | `entry/src/main/ets/models/` |
| | `entry/src/main/ets/utils/{storage,format,validator,toast,permission,upload}.ets` |
| | `entry/src/main/ets/storages/UserStore.ets` |
| | `entry/src/main/ets/router/RouterMap.ets` |
| | `entry/src/main/ets/components/common/*.ets` |
| | `entry/src/main/ets/pages/Index.ets` |

**验收成果**
- [ ] 客户端能编译运行，`Index` 页能成功调用 `GET /api/ping` 并把结果显示在页面上
- [ ] 断网 / 服务端未启动时，界面弹出统一错误 Toast，不白屏、不崩溃
- [ ] Token 写入 Preferences 后杀掉进程重启仍能读到
- [ ] 8 个科目、12 个年级的字典与图标可正常渲染出九宫格（临时页面即可）
- [ ] 全局无 `http.createHttp()` 散落（`grep` 全局只有一处，在 `request.ets`）
- [ ] 已 `push`

**提交信息**：`feat(client): 搭建路由/网络/常量/模型/状态/通用组件基建`

---

### Part 4 —— 认证与角色（端到端）

**目标**：注册、登录、实名、教师资质上传全链路可用。**这是模块①的核心。**

**实现方式（服务端）**
1. `AuthController`：`/sms`（验证码写死 `123456` 并记录到内存 Map，5 分钟有效）、`/register`、`/login`、`/login/sms`、`/login/wx`、`/login/qq`、`/me`、`/logout`。
2. 注册逻辑按角色分支：
   - `PARENT` / `STUDENT`：写 `sys_user`；`PARENT` 若带实名信息同时写 `real_name`/`id_card`
   - `TEACHER`：写 `sys_user`（`status=2 待审核`）+ `teacher_cert`（`status=待审核`）+ `teacher_profile`
   - 手机号唯一性校验；密码 BCrypt 加密
3. 微信/QQ 登录：`openid` 由客户端传 mock 值，服务端首次登录自动建号（角色默认 `PARENT`）。
4. `UserController`：`GET/PUT /api/user/profile`、`POST /api/user/realname`（身份证 18 位正则 + 校验位校验）。
5. `TeacherCertController` + `TeacherProfileController`：提交资质、查资质状态、查改教师资料。

**实现方式（客户端）**
6. 页面链路：`Login` →（无账号）`Register`（选角色）→ `PARENT/STUDENT` 走 `RealName`；`TEACHER` 走 `TeacherCert`（拍摄/相册选图 → `upload` → 带 URL 提交）。
7. `Login` 支持三种方式 Tab：账号密码 / 验证码 / 第三方；「记住密码」与「忘记密码」保留（参考案例原有功能）。
8. 登录成功后写 `UserStore` + Preferences，按角色跳不同首页（教师→工作台、管理员→审核台、家长/学生→主页）。
9. 表单校验用 `utils/validator.ets`，错误就近提示而非只在 Toast。

**路径**

| 端 | 路径 |
|---|---|
| 服务端 | `controller/{AuthController,UserController,TeacherCertController,TeacherProfileController}.java` |
| | `service/{AuthService,UserService,TeacherService}.java` + `impl/` |
| | `mapper/{SysUserMapper,TeacherCertMapper,TeacherProfileMapper}.java` |
| | `entity/{SysUser,TeacherCert,TeacherProfile}.java` |
| | `dto/{RegisterDTO,LoginDTO,RealNameDTO,CertSubmitDTO}.java`　`vo/{LoginVO,UserVO}.java` |
| 客户端 | `pages/auth/{Login,Register,RoleSelect,RealName,TeacherCert,SmsLogin,ForgetPwd}.ets` |
| | `api/modules/{auth,user,file}.ets` |
| | `models/user.ets`　`storages/UserStore.ets` |

**验收成果**
- [ ] 注册家长 → 登录 → 「我的」显示手机号与头像
- [ ] 注册教师 → 提交资质图片 → 登录后状态显示「待审核」，且**无法接单**
- [ ] 家长未实名时下单被拦截，实名后放行
- [ ] 身份证格式错误（17 位 / 校验位错）被前端 + 后端双重拦截
- [ ] 手机号重复注册返回明确中文提示
- [ ] 微信 / QQ 登录按钮点击后可进入主页（mock 生效）
- [ ] 同账号重复登录不报错，Token 可刷新
- [ ] 已 `push`

**提交信息**：`feat(auth): 实现四角色注册登录/实名认证/教师资质上传`

---

### Part 5 —— 管理员审核台（端到端）

**目标**：管理员可审核教师资质、管理用户、看汇总数。**这是「管理员（审核资质）」要求的落地点。**

**实现方式**
1. `AdminController`：`/cert/list`（按状态分页）、`/cert/audit`（通过 → `sys_user.status=0` 且 `teacher_cert.status=已通过`；驳回 → 写 `reject_reason`）、`/users`、`/user/{id}/status`、`/dashboard`。
2. 每次审核写 `admin_log`。
3. 客户端 `CertAudit` 页：Tab 切换「待审核 / 已通过 / 已驳回」，列表项可展开看大图；`AuditDetail` 页提供「通过 / 驳回」按钮，驳回必填原因（弹窗输入）。
4. `AdminDashboard` 页展示待审核数、总用户数、今日订单数，点击可跳对应列表。
5. 路由守卫：非 ADMIN 角色不可进入 `pages/admin/*`。

**路径**

| 端 | 路径 |
|---|---|
| 服务端 | `controller/AdminController.java`　`service/AdminService.java`　`mapper/AdminLogMapper.java`　`entity/AdminLog.java` |
| 客户端 | `pages/admin/{CertAudit,AuditDetail,UserManage,AdminDashboard}.ets`　`api/modules/admin.ets` |

**验收成果**
- [ ] 待审核列表能看到 Part 4 提交的教师
- [ ] 点击「通过」→ 该教师状态变「已通过」，可用教师账号登录并接单
- [ ] 点击「驳回」且不填原因 → 被拦截；填写后状态变「已驳回」，教师端能看到驳回原因
- [ ] 禁用某用户 → 该用户下次请求返回 401/403 且给出提示
- [ ] `admin_log` 表有对应记录
- [ ] 家长账号直接访问 `pages/admin/CertAudit` 被拦截
- [ ] 已 `push`

**提交信息**：`feat(admin): 实现教师资质审核/用户管理/管理台首页`

---

### Part 6 —— 首页与教师检索（端到端）

**目标**：首页聚合 + 按科目/年级检索教师。**对应参考案例的首页与查找教师页。**

**实现方式**
1. `/api/home/index` 聚合返回：轮播图（5 张，取自 `resources/base/media` 或服务端静态图）、公告列表、名师推荐（按 `rating` 取前 6）。
2. `/api/tutor/list` 支持 `subject`、`grade`、`keyword`、`sortBy`（评分/价格/接单量）、分页；**只返回资质已通过的教师**。
3. 客户端 `HomeView`：`Swiper` 自动轮播 + 8 科目九宫格（`Grid`）+ 公告 `Swiper` 纵向滚动 + 名师横滑 `List`。
4. 客户端 `TutorSearchView`：顶部返回 + 科目下拉 + 年级下拉（用 `@ohos.arkui.advanced` 或自绘浮层模拟参考案例的 `PopupWindow`）+ 结果列表；**支持「仅按科目」（首页点九宫格进入）与「科目+年级」两种入口**，默认「语文 / 一年级」。
5. 滚到底部加载下一页；无结果展示 `HmEmpty` + 文案「抱歉，没有满足你要查找的教师！」（沿用参考案例文案）。
6. `TutorCard` 组件：头像、姓名、科目标签、年级、教龄、评分、时薪。

**路径**

| 端 | 路径 |
|---|---|
| 服务端 | `controller/{HomeController,TutorController}.java`　`service/TutorService.java`　`mapper/TeacherProfileMapper.xml`（组合条件动态 SQL） |
| 客户端 | `views/home/HomeView.ets`　`views/tutor/TutorSearchView.ets` |
| | `pages/tutor/TutorSearch.ets` |
| | `components/tutor/{TutorCard,SubjectGrid,FilterPopup}.ets` |
| | `api/modules/{home,tutor}.ets`　`models/tutor.ets` |

**验收成果**
- [ ] 首页轮播自动播放且可手势切换；8 个科目图标可点击
- [ ] 点「数学」→ 跳到检索页且结果全是数学教师
- [ ] 检索页选「英语」+「七年级」→ 结果与 §6.4 数据吻合
- [ ] 选一个无教师组合 → 显示空态与指定文案，不白屏
- [ ] 列表下滑能加载第 2 页，不重复、不丢项
- [ ] 待审核 / 已驳回的教师**不出现在**检索结果中
- [ ] 已 `push`

**提交信息**：`feat(tutor): 实现首页聚合与教师科目年级检索`

---

### Part 7 —— 教师详情与预约（端到端）

**目标**：教师详情页 + 预约生成待支付订单。**对应参考案例的教师详情与预约功能。**

**实现方式**
1. `/api/tutor/{id}` 返回教师资料 + 课程列表 + 评价列表 + 是否已收藏。
2. `/api/tutor/reserve` 生成一条 `order_status = 待支付` 的订单，返回 `orderNo`；客户端跳收银台。
3. 收藏/取消收藏幂等。
4. 客户端 `TutorDetail`：头像、性别、姓名、手机号（脱敏）、地址、科目、教学经历、评分、评价列表、「预约教师」按钮。
5. 点击「预约教师」→ 按钮立即置灰（防重复提交）→ 弹 `PaySheet` 选课程/套餐 → 进 `OrderConfirm`。
6. 预约成功 Toast 沿用参考案例文案：「预约成功，在消息中查看相关信息」。

**路径**

| 端 | 路径 |
|---|---|
| 服务端 | `controller/TutorController.java`（详情/预约/收藏）　`service/{TutorService,OrderService}.java`　`mapper/FavoriteMapper.java` |
| 客户端 | `pages/tutor/{TutorDetail,TutorReserve}.ets`　`components/order/PaySheet.ets`　`api/modules/tutor.ets` |

**验收成果**
- [ ] 教师详情页字段与 §6.3 数据一致，教学经历可换行完整显示
- [ ] 点击「预约教师」后按钮置灰，重复点击不产生第二条订单
- [ ] 数据库 `order` 表新增 `待支付` 记录，`order_no` 唯一
- [ ] 收藏后再次进入详情，按钮显示「已收藏」状态
- [ ] 未登录点预约 → 跳登录页，登录后回到原页
- [ ] 已 `push`

**提交信息**：`feat(reserve): 实现教师详情与预约下单`

---

### Part 8 —— 课程、套餐与下单（端到端）

**目标**：单次支付 / 套餐支付两条价格链路都能算对。**这是模块②的核心前置。**

**实现方式**
1. `/api/course/list`、`/api/course/{id}`、`/api/course/{id}/packages`。
2. `/api/order/preview`：入参「课程 + 是否套餐 + 用哪张券」→ 返回
   - `unit_price`、`lesson_count`、`total_amount`
   - `discount_amount`（套餐折扣 + 券折扣，**两者可叠加，需定义叠加顺序**）
   - `pay_amount`（= total − discount，且**不小于 0**）
3. 折扣规则（写死在 `OrderService` 常量，并在报告「功能设计」章说明）：
   - 套餐折扣优先于券；券要求 `total_amount >= min_amount` 才可用
   - 折扣后金额四舍五入保留 2 位
4. `/api/order/create`：用「预览同款算法」再算一次（**服务端不信任客户端传来的金额**），写订单。
5. 客户端 `OrderConfirm`：课程信息、课时数、上课时间选择器、券选择、金额明细（划掉原价显示优惠）、「提交订单」。
6. 客户端 `Cashier` 页在 Part 9 完成。

**路径**

| 端 | 路径 |
|---|---|
| 服务端 | `controller/{CourseController,OrderController}.java`　`service/OrderService.java`（`calcPrice` 私有方法）　`mapper/{CourseMapper,CoursePackageMapper,CouponMapper}.java` |
| 客户端 | `pages/order/OrderConfirm.ets`　`api/modules/{course,order}.ets`　`models/order.ets`　`components/order/{PriceTag,StatusTag}.ets` |

**验收成果**
- [ ] 单次下单：`pay_amount == 单价 × 1`
- [ ] 套餐下单：`pay_amount == 套餐价`，且界面显示原价被划掉
- [ ] 套餐 + 可用券：两个折扣都生效，金额与手算一致
- [ ] 券金额大于订单金额时：`pay_amount = 0`，不出现负数
- [ ] 篡改前端传的 `payAmount` → 服务端仍按自己算的金额落库
- [ ] 折扣计算结果有单元测试或至少手工验算记录（保留在 `docs/`）
- [ ] 已 `push`

**提交信息**：`feat(order): 实现课程套餐与订单金额计算`

---

### Part 9 —— 模拟支付、钱包与奖学券（端到端）

**目标**：收银台可完成支付，钱包与券状态联动更新。**模块②核心。**

**实现方式**
1. `/api/pay/prepay`：返回模拟支付参数（`payType`、`orderNo`、`mockPayUrl`）。
2. `/api/pay/callback`：模拟支付成功 → `pay_status = 已支付`、`order_status = 已支付`、写 `pay_time`、生成 `wallet_record`、扣减或增加钱包、发放首单奖励积分、核销券。**整个过程单一事务。**
3. `/api/wallet`、`/api/wallet/records`、`/api/coupon/list`。
4. 客户端 `Cashier`：三选一支付方式（微信 / 支付宝 / 余额），余额不足时禁用余额选项并提示；点支付 → 弹出模拟支付面板 → 2 秒后成功 → 跳订单详情。
5. 「我的钱包」页：总额 / 余额 / 积分三格（参考案例 `me_money.xml` 的布局思路）+ 流水列表。
6. 「奖学券」页：未使用 / 已使用 / 已过期三态，过期券置灰。

**路径**

| 端 | 路径 |
|---|---|
| 服务端 | `controller/{PayController,WalletController,CouponController}.java`　`service/{PayService,WalletService,CouponService}.java`　`mapper/{WalletMapper,WalletRecordMapper,CouponMapper}.java`　`entity/{Wallet,WalletRecord,Coupon}.java` |
| 客户端 | `pages/order/Cashier.ets`　`pages/mine/{Wallet,Coupon}.ets`　`api/modules/{pay,wallet}.ets` |

**验收成果**
- [ ] 选择微信支付 → 订单变「已支付」，`pay_time` 有值
- [ ] 余额不足时「余额支付」为禁用态，无法点进去
- [ ] 用券支付后，该券状态变「已使用」，不可再用
- [ ] 支付成功后钱包流水新增一条，金额与订单一致
- [ ] 重复调用 `/api/pay/callback` **不会**产生重复流水（幂等校验）
- [ ] 已过期的券在选择列表里不可选
- [ ] 已 `push`

**提交信息**：`feat(pay): 实现模拟支付/钱包/奖学券联动`

---

### Part 10 —— 订单管理与退款（端到端）

**目标**：订单列表/详情/取消/退款/导出全通。**模块②收口。**

**实现方式**
1. `/api/order/list`：按角色自动过滤（家长看自己下的、教师看接到的、管理员看全部），支持 `order_status` 筛选与分页。
2. `/api/order/{orderNo}` 详情：含课程、教师、学生、金额明细、状态时间轴。
3. `/api/order/{orderNo}/cancel`：仅「待支付」可取消。
4. `/api/order/{orderNo}/refund`：**仅「已支付 且 未到上课时间」可申请** → `order_status = 退款中`；模拟审核通过 → `已退款`，回退钱包或原路退回，写 `wallet_record`。
5. `/api/order/refund/rule`：返回退款规则文案（含「7 个工作日到账」）。
6. `/api/order/export`：把当前筛选结果导出 **CSV（UTF-8 带 BOM，Excel 直接打开不乱码）**，列含 `订单号, 课程名称, 课时数, 费用, 支付状态, 上课时间`。
7. 客户端 `OrderList`（分 Tab：全部/待支付/已支付/已完成/退款）、`OrderDetail`（状态时间轴 + 取消/退款/评价按钮，按状态显隐）、退款原因弹窗。

**路径**

| 端 | 路径 |
|---|---|
| 服务端 | `controller/OrderController.java`　`service/OrderService.java`（状态机）　`common/util/CsvUtil.java` |
| 客户端 | `pages/order/{OrderList,OrderDetail,RefundApply}.ets`　`components/order/OrderCard.ets`　`utils/download.ets`（导出/分享） |

**验收成果**
- [ ] 家长端只看到自己的订单，教师端只看到自己的接单记录
- [ ] 「待支付」订单可取消，取消后状态变「已取消」
- [ ] 「已完成」订单点退款 → 被拦截并提示规则
- [ ] 可退款订单申请退款 → 状态「退款中」→ 模拟通过后「已退款」，钱包到账
- [ ] 导出的 CSV 用 Excel 打开不乱码，行数与列表条数一致
- [ ] 订单状态非法流转（如「已退款」再取消）被服务端拒绝
- [ ] 已 `push`

**提交信息**：`feat(order): 实现订单管理/退款流程/CSV导出`

---

### Part 11 —— 实时聊天（端到端）

**目标**：家长与教师可实时收发消息。**模块③核心。**

**实现方式**
1. 服务端 `WebSocketConfig` + `ChatWsServer`（`@ServerEndpoint("/ws/chat")`）：
   - 连接时用 query 里的 `token` 解析 `userId`，绑定到在线表 `Map<Long, Session>`
   - 收到消息 → 落库 `chat_message` → 更新 `chat_session` 的 `last_msg`/`last_time`/对端未读数 → 推给对端（在线则推，离线则仅落库）
   - 心跳保活；断线清理
2. HTTP 兜底：`/api/chat/send`（用于 WebSocket 不可用时）、`/api/chat/sessions`、`/api/chat/messages`、`/api/chat/read`。
3. **消息类型**：
   - `TEXT`：直接发文本
   - `VOICE`：客户端用录音能力录 `m4a` → 走 `/api/file/upload` → 发送带 `media_url + duration` 的语音消息（**接收端用 `AVPlayer` 播放**）
   - `VIDEO`：以「发起视频通话」的模拟界面实现（来电弹窗 + 接通/挂断动画），不接真实 RTC
4. **会话归一化**：入库前 `user_a_id = min(双方ID)`，避免 A→B 与 B→A 生成两条会话。
5. 客户端 `ChatList`：会话列表含头像、最后一条、时间、未读红点（数据源 `ChatStore`）。
6. 客户端 `ChatDetail`：气泡列表、`@ohos.net.webSocket` 长连接、输入框、发送、滚动到底、未读数清零。
7. 未读角标：底栏「消息」Tab 显示总未读数，随 `ChatStore` 变更刷新。

**路径**

| 端 | 路径 |
|---|---|
| 服务端 | `websocket/ChatWsServer.java`　`common/config/WebSocketConfig.java`　`controller/ChatController.java`　`service/ChatService.java`　`mapper/{ChatSessionMapper,ChatMessageMapper}.java`　`entity/{ChatSession,ChatMessage}.java` |
| 客户端 | `pages/chat/{ChatList,ChatDetail}.ets`　`components/chat/{ChatBubble,ChatInput,VoiceBubble}.ets`　`api/modules/chat.ets`　`utils/recorder.ets`　`storages/ChatStore.ets`　`models/chat.ets` |

**验收成果**
- [ ] 家长端发消息 → 教师端**不刷新页面**即可收到，延迟肉眼不可感
- [ ] 断开 WebSocket（杀网络/关服务端）后，客户端能自动重连并提示
- [ ] 教师离线时发的消息，教师上线后能在历史记录中看到
- [ ] 未读数正确：收到消息底部 Tab 出现红点，进入会话后清零
- [ ] 语音消息可录制、发送、在接收端播放
- [ ] 视频通话按钮能弹出模拟接通界面（不崩溃、可挂断）
- [ ] 已 `push`

**提交信息**：`feat(chat): 实现 WebSocket 实时聊天与语音/视频模拟`

---

### Part 12 —— 学习反馈与提醒（端到端）

**目标**：教师提交学习报告，家长可查；上课/作业提醒可送达。**模块③收口。**

**实现方式**
1. `/api/report` 教师提交报告（周期、总结、课堂表现、评分、态度、作业完成率、建议）；家长端仅可读自己孩子的。
2. 提交报告时**自动生成一条 `notice`** 推给家长。
3. `/api/notice/list`、`/unread-count`、`/read`。
4. **提醒生成**（两种触发，任一实现即可，建议都做）：
   - 下单成功 → 创建「上课提醒」记录（`remind_time = class_time - 30min`）
   - 定时任务（`@Scheduled`，每 5 分钟）扫描到点提醒 → 由客户端拉取 `/unread-count` 展示
5. 客户端 `ReportDetail` 页：报告卡片（评分用星级、作业完成率用进度条）。
6. 客户端 `NoticeCenter` 页：Tab「全部/上课/作业/系统」，未读高亮，点击标记已读。
7. 客户端用本地通知（`@ohos.notificationManager`）弹系统级提醒。

**路径**

| 端 | 路径 |
|---|---|
| 服务端 | `controller/{ReportController,NoticeController}.java`　`service/{ReportService,NoticeService}.java`　`mapper/{StudyReportMapper,NoticeMapper}.java`　`entity/{StudyReport,Notice}.java`　`task/RemindTask.java` |
| 客户端 | `pages/chat/ReportDetail.ets`　`pages/notice/NoticeCenter.ets`　`api/modules/{report,notice}.ets`　`utils/notify.ets` |

**验收成果**
- [ ] 教师提交报告 → 家长端通知中心出现未读，进入可看完整报告
- [ ] 报告列表分页正确，家长看不到别人孩子的报告（越权测试）
- [ ] 下单后生成「上课提醒」，到点后未读数 +1
- [ ] 点击提醒后未读数 -1，重启 App 后已读状态保持
- [ ] 系统级通知能弹出（需已授予通知权限）
- [ ] 已 `push`

**提交信息**：`feat(report): 实现学习反馈报告与课程作业提醒`

---

### Part 13 —— AI 数据看板（端到端）

**目标**：三个维度的分析看板全部出图。**模块④核心，答辩重点展示。**

**实现方式**
1. `/api/analysis/overview`：从 `daily_stat` + 实时聚合返回
   - 用户增长数（近 30 天折线 + 环比）
   - 订单量（柱状）
   - 交易额（柱状/折线）
   - 复购率（百分比 + 环形）
2. `/api/analysis/tutor`：
   - 好评率 = `AVG(evaluation.score)/5`
   - 接单率 = 已完成订单数 / 收到的预约总数
   - 退课率 = 已退款订单数 / 总订单数
   - 返回 Top10 教师排行榜
3. `/api/analysis/demand`：
   - 热门学科（饼图，按订单数聚合）
   - 年级分布（柱状）
   - 价格敏感度（价格带 vs 下单量，双轴）
4. **数据来源**：`daily_stat` 预聚合表 + 复杂聚合 SQL 写在 `mapper/*.xml`（报告「数据库设计」章可截图这些 SQL 说明"分析类查询"）。
5. **「AI」的定位**（答辩口径，务必统一）：
   - 主体是**数据驱动的智能分析**：多维度聚合 + 阈值预警（如退课率 > 20% 标红）+ 明细下钻
   - 加分项：将聚合结果拼成 prompt 调一次大模型 API，生成一段中文「经营建议」展示在卡片上。**未接通时为本地模板文案兜底**，不允许出现空白。
6. **图表组件自绘**：`components/chart/` 用 `Canvas` 手写 `LineChart` / `BarChart` / `PieChart`，不引第三方图表库（避免版本兼容风险）。

**路径**

| 端 | 路径 |
|---|---|
| 服务端 | `controller/AnalysisController.java`　`service/AnalysisService.java`　`mapper/AnalysisMapper.java` + `mapper/AnalysisMapper.xml`　`vo/{OverviewVO,TutorAnalysisVO,DemandAnalysisVO}.java` |
| 客户端 | `pages/analysis/{OverviewBoard,TutorPerformance,ParentDemand}.ets`　`pages/admin/AdminDashboard.ets`（复用）　`components/chart/{LineChart,BarChart,PieChart,StatCard}.ets`　`api/modules/analysis.ets` |

**验收成果**
- [ ] 三个看板页均有**真实数据**渲染出的图表，无空图、无占位文字
- [ ] 数据与 §6.4 初始化数据手算结果一致（至少核对复购率与好评率各一次）
- [ ] 环形/饼图占比合计 100%
- [ ] 教师角色进入只看到自己的表现数据；管理员看到全量
- [ ] 图表在 Phone 模拟器竖屏下不溢出、不重叠、文字可读
- [ ] 已 `push`

**提交信息**：`feat(analysis): 实现三维度数据看板与自绘图表组件`

---

### Part 14 —— 评价与建议反馈（端到端）

**目标**：完成参考案例遗留的「我的评价 / 我要提建议」入口。

**实现方式**
1. `/api/evaluation`：订单完成后可评价（评分 + 标签 + 文字），一单一评。
2. 评价写入后**回算教师 `good_rate` 与 `rating`**（保证 Part 13 看板数据随之变化，演示时可现场制造数据变化 —— 很好的答辩点）。
3. `/api/evaluation/list` 支持按教师查。
4. `/api/feedback`（科目 / 标题 / 内容）与 `/api/feedback/list`；管理员可回填 `reply`。
5. 客户端：订单详情页「去评价」入口 → 评价页（星级 + 标签多选 + 文本）；「我的」里加「我要提建议」入口（沿用参考案例的 `mes_order_suggest` 结构）。

**路径**

| 端 | 路径 |
|---|---|
| 服务端 | `controller/{EvaluationController,FeedbackController}.java`　`service/{EvaluationService,FeedbackService}.java`　`mapper/{EvaluationMapper,FeedbackMapper}.java`　`entity/{Evaluation,Feedback}.java` |
| 客户端 | `pages/order/EvaluationEdit.ets`　`pages/mine/FeedbackSubmit.ets`　`components/common/HmRate.ets`　`api/modules/evaluation.ets` |

**验收成果**
- [ ] 已完成订单可评价，未完成订单不显示入口
- [ ] 同一订单重复评价被拦截
- [ ] 评价提交后，Part 13 的好评率随之变化并能在看板看到
- [ ] 建议提交后可在列表中查到
- [ ] 已 `push`

**提交信息**：`feat(evaluation): 实现订单评价与建议反馈`

---

### Part 15 —— 联调、演示与交付物

**目标**：全链路走通、演示素材与三项文档交付物产出。

**实现方式**
1. **越权自查**：用家长 Token 调教师/管理员接口，逐一确认返回 403/401（列检查表）。
2. **异常自查**：断网、服务端宕机、Token 过期、重复提交、超长文本、空列表，逐项确认不崩溃、有提示。
3. **演示脚本**：写 `docs/演示/演示脚本.md`，按「注册教师 → 管理员审核 → 家长检索 → 下单支付 → 聊天 → 看报告 → 看板」顺序，标注每步截图位置，控制在 5–7 分钟。
4. **录屏**：`docs/演示/演示录屏.mp4`。
5. **截图归档**：每页至少 1 张 × 每个关键状态，存 `docs/screenshots/`，**用于报告**。
6. **Visio 图**：系统架构图、功能模块图、ER 图、关键业务流程图（下单/支付/退款/审核）、用例图，存 `docs/diagrams/`。
7. **代码图片**：把关键代码段（`request.ets`、`OrderService.calcPrice`、`ChatWsServer`）导出为**浅灰底 + 注释**的图片，存 `docs/report-code/`，用于报告正文（正文禁止粘贴长段代码）。
8. **课程报告**：按模板写 ≥25 页，五部分 + 参考文献，严格执行 §10.2 的排版项。
9. **开题 PPT / 答辩 PPT**：开题讲架构与技术思路；答辩讲四大模块 + 演示 + 难点 + 收获。

**路径**

| 交付物 | 路径 |
|---|---|
| 报告 | `docs/报告/课程报告.docx` |
| 开题 PPT | `docs/开题PPT/开题PPT.pptx` |
| 答辩 PPT | `docs/答辩PPT/答辩PPT.pptx` |
| 演示 | `docs/演示/{演示脚本.md,演示录屏.mp4}` |
| 图 | `docs/diagrams/*.vsdx` `*.png` |
| 截图 | `docs/screenshots/**` |
| 代码图 | `docs/report-code/*.png` |

**验收成果**
- [ ] §10.1 的「功能验收总表」全部打勾
- [ ] 越权与异常自查表全部通过（表格存 `docs/演示/自查表.md`）
- [ ] 演示脚本按顺序一次跑通，全程无崩溃、无空白页
- [ ] 录屏文件可正常播放，时长 5–7 分钟
- [ ] 报告页数 ≥ 25，五部分完整，参考文献 ≥ 10 条且格式正确
- [ ] 两套 PPT 完成
- [ ] 已 `push`

**提交信息**：`docs: 完成联调、演示素材与三项交付物`

---

## 9. Git 工作流

### 9.1 分支策略

单人开发，采用**主干直推 + Part 标签**：

```
main ──●──●──●──●──●──● ...（每个 Part 一个提交或一组提交）
       P0 P1 P2 P3 P4 ...
```

- 每个 Part **至少一个提交**，Part 完成后立刻 `push`。
- Part 内允许若干次 `commit`，但 `push` 前必须处于**可编译、可运行**状态。
- 每个 Part 完成后打 tag：`git tag p0-init`、`git tag p4-auth` …… 便于回溯与答辩时展示进度。

### 9.2 提交信息规范

```
<type>: <中文描述>

<可选正文：关键决策与原因>
```

| type | 用途 |
|---|---|
| `chore` | 工程初始化、配置、依赖 |
| `feat` | 新功能 |
| `fix` | 修复缺陷 |
| `refactor` | 重构（不改行为） |
| `docs` | 文档、报告、PPT |
| `test` | 测试 |
| `perf` | 性能 |

### 9.3 每个 Part 的固定收尾动作

```
1. 自检 Part 的「验收成果」清单，全部勾选
2. 双端都能编译运行
3. git add -A
4. git commit -m "<type>: <描述>"
5. git push origin main
6. git tag pN-<slug> && git push origin pN-<slug>
7. 在 SPEC.md 对应 Part 的验收清单里打勾（本次一并提交）
```

> **约束**：不允许把「编译不过」的代码 push 上去。远端仓库的意义是"随时可回滚到能跑的版本"，一旦混入坏状态就失去价值。

### 9.4 `.gitignore` 必须包含

```
# 客户端
client/**/build/
client/**/.hvigor/
client/**/oh_modules/
client/**/.preview/
client/**/.idea/
client/local.properties
client/**/*.hap

# 服务端
server/target/
server/src/main/resources/upload/
server/logs/
*.log

# 通用
.idea/
.vscode/
*.iml
.DS_Store
```

---

## 10. 全局验收清单

### 10.1 功能验收总表（对照作业要求）

| # | 作业要求 | 落点 Part | 验收方式 |
|---|---|---|---|
| 1 | 教师注册上传资质认证 | P4 P5 | 教师注册→上传→管理员审核全流程演示 |
| 2 | 角色划分（教师/家长/学生/管理员） | P4 P5 | 四个账号分别登录，首页不同 |
| 3 | 管理员审核资质 | P5 | 审核台通过/驳回 |
| 4 | 手机号 / 微信 / QQ 注册登录 | P4 | 三种登录方式各演示一次 |
| 5 | 关联实名认证（家长身份证 / 教师资质） | P4 | 未实名被拦截 → 实名后放行 |
| 6 | 个人资料可编辑 | P4 | 改昵称/头像/联系方式并生效 |
| 7 | 多样化支付（微信 / 支付宝） | P9 | 收银台三种方式各演示 |
| 8 | 单次支付 / 套餐支付（套餐折扣） | P8 P9 | 同一课程两种下单，对比金额 |
| 9 | 订单明细（课程/课时/费用/状态/上课时间） | P10 | 订单列表与详情 |
| 10 | 订单导出 | P10 | 导出 CSV 并打开 |
| 11 | 退款保障（流程 + 7 个工作日） | P10 | 申请退款 → 规则说明页 |
| 12 | 实时聊天（文字） | P11 | 双端实时收发 |
| 13 | 实时聊天（语音 / 视频） | P11 | 语音录制播放；视频模拟接通 |
| 14 | 学习反馈（进度报告 / 课堂表现） | P12 | 教师提交 → 家长查看 |
| 15 | 课程提醒（上课 / 作业） | P12 | 提醒中心 + 系统通知 |
| 16 | 核心数据看板（增长/订单/交易额/复购率） | P13 | 看板页 |
| 17 | 家教表现分析（好评率/接单率/退课率） | P13 | 看板页 |
| 18 | 家长需求分析（学科/年级/价格敏感度） | P13 | 看板页 |
| 19 | 参考案例基础功能（首页/检索/详情/预约/我的） | P6 P7 | 对照参考案例逐项走 |
| 20 | 服务端为 Spring Boot + MySQL | P2–P13 | 展示 `pom.xml` 与数据库 |

### 10.2 报告排版验收（**10 项，每项不满足扣 10%**）

| # | 项 | 要求 | 自检 |
|---|---|---|---|
| 1 | 页边距 | 左、上 2.5cm；右、下 2cm | [ ] |
| 2 | 字体字号 | 正文小四，中文宋体 / 英数 Times New Roman | [ ] |
| 3 | 段落 | 行距**固定值 22 磅**、首行缩进 2 字符、两端对齐 | [ ] |
| 4 | 标题 | 一级小三黑体居中；二级四号黑体左对齐；三级小四黑体左对齐 | [ ] |
| 5 | 页码 | 封面无码；目录罗马数字；正文阿拉伯数字从 1 开始 | [ ] |
| 6 | 页眉 | 正面「物联网移动应用开发课程案例实践报告」；反面本组作品题目；均居中 | [ ] |
| 7 | 目录 | 自动生成；「目录」二字二号黑体居中、段前段后 1 行；一级四号宋体、二级小四宋体；页码行末 | [ ] |
| 8 | 图表 | 标题五号、图题在下表题在上、编号「图1.1/表1.1」、图表与题注均居中无缩进 | [ ] |
| 9 | 避免留白 | 页面下方无大块空白 | [ ] |
| 10 | 参考文献 | 格式规范、标识符正确、条目完整 | [ ] |

**其他硬约束**
- [ ] 总篇幅 **≥ 25 页**
- [ ] **正文无长段代码粘贴**；关键代码用**图片**呈现，浅灰底 + 注释
- [ ] 图**全部自绘**（Visio），无网页/PDF 截图
- [ ] 界面截图与照片 **≤ 页面 1/3**，一行尽量放 2 张
- [ ] 报告结构含五部分：案例简介 / 需求分析 / 项目设计 / 实现与测试 / 总结 + 参考文献

### 10.3 交付物验收

- [ ] 开题 PPT（系统框架 + 开发技术 + 开发思路）
- [ ] 系统演示（可运行 App + 服务端，现场可演）
- [ ] 课程报告（≥ 25 页，排版达标）
- [ ] 答辩 PPT

---

## 11. 风险与降级方案

| # | 风险 | 影响 | 对策 / 降级方案 |
|---|---|---|---|
| 1 | **中文路径导致 hvigor 构建失败** | 卡死 P0 | 迁到 ASCII 路径（`D:\dev\qingxin-jiajiao`）或开启 Windows UTF-8 Beta |
| 2 | **模拟器访问不到宿主机** | 全链路不通 | 按 §2.2 四步验证；baseUrl 单点常量；防火墙放行 8080；不用 `127.0.0.1` |
| 3 | **模拟器不支持摄像头/麦克风** | 资质拍照、语音消息做不了 | 降级为**相册选图**（`READ_IMAGEVIDEO`）；语音可用**本地音频文件**走同一上传链路验证；报告中说明限制 |
| 4 | **音视频实时通话做不动** | 拖垮 P11 | 视频做「模拟接通界面」；语音做「录音上传 + 播放」。不要在课设周期内尝试真 RTC |
| 5 | **鸿蒙三方图表库兼容性差** | 看板做不出 | **Canvas 自绘三大图表组件**，零外部依赖 |
| 6 | **「AI」被质疑无 AI** | 答辩扣分 | 统一口径：「数据驱动的智能分析 + 阈值预警 + 明细下钻」；加分项接一次大模型 API 生成经营建议，未接通时本地模板兜底 |
| 7 | **WebSocket 在模拟器不稳定** | 聊天演示翻车 | 保留 `/api/chat/send` HTTP 兜底 + 轮询降级开关；演示前先在模拟器上实测 |
| 8 | **图片上传落盘路径丢失** | 图片 404 | 静态资源映射 `/upload/**`；配置 `upload` 目录为绝对路径；重启后校验旧图仍可访问 |
| 9 | **报告排版不达标** | **每项扣 10%** | P0 就把 Word 模板（样式/页眉页脚/多级列表/题注）建好，之后**边做边填**，不最后突击 |
| 10 | **演示数据不足导致看板空白** | 演示失败 | `data.sql` 必须含 30 天 `daily_stat` + 各状态订单（见 §6.4） |
| 11 | **越权漏洞** | 答辩被问住 | P15 逐条做越权自查表；所有查询强制带 `user_id` 条件，不信任前端传的 ID |
| 12 | **金额精度** | 对账不平 | 全程 `decimal` + `BigDecimal` 运算，禁用 `double` |

---

## 12. 附录：枚举与字典

### 12.1 角色 `Role`

| 值 | 含义 | 说明 |
|---|---|---|
| `PARENT` | 家长 | 可下单、可支付、可评价、可聊天 |
| `STUDENT` | 学生 | 只读为主 |
| `TEACHER` | 教师 | 需资质审核通过 |
| `ADMIN` | 管理员 | 预置账号，不开放注册 |

### 12.2 用户状态 `sys_user.status`

`0` 正常 · `1` 禁用 · `2` 待审核（仅教师）

### 12.3 资质审核状态 `teacher_cert.status`

`待审核` · `已通过` · `已驳回`

### 12.4 订单状态 `order.order_status`

`待支付` → `已支付` → `上课中` → `已完成`
分支：`待支付` → `已取消`；`已支付` → `退款中` → `已退款`

### 12.5 支付状态 `order.pay_status`

`未支付` · `已支付` · `已退款`

### 12.6 支付方式 `order.pay_type`

`WECHAT` 微信 · `ALIPAY` 支付宝 · `BALANCE` 余额

### 12.7 消息类型 `chat_message.msg_type`

`TEXT` 文字 · `VOICE` 语音 · `IMAGE` 图片 · `VIDEO` 视频

### 12.8 提醒类型 `notice.notice_type`

`CLASS_REMIND` 上课提醒 · `HOMEWORK_REMIND` 作业提醒 · `SYSTEM` 系统通知

### 12.9 科目字典（8 项，沿用参考案例）

`语文` `数学` `英语` `物理` `政治` `化学` `生物` `地理`

### 12.10 年级字典（12 项）

`一年级` `二年级` `三年级` `四年级` `五年级` `六年级` `七年级` `八年级` `九年级` `高一` `高二` `高三`

### 12.11 统一响应码 `ResultCode`

**与 [`API.md` §1.6](./API.md#16-错误码表) 完全一致**（共 19 项）。服务端 `common/ResultCode.java`、客户端 `constants/enums.ets` 均以此为准。

| code | 含义 | 客户端处理 |
|---|---|---|
| 200 | 成功 | 取 `data` |
| 400 | 参数错误 | Toast `msg` |
| 401 | 未登录 / Token 失效 | **清 Token 跳登录，不 Toast** |
| 403 | 无权限 | Toast `msg` |
| 404 | 资源不存在 | Toast `msg` |
| 500 | 服务器内部错误 | Toast 统一文案 |
| 1001 | 手机号已注册 | 就近提示到手机号框 |
| 1002 | 账号或密码错误 | 就近提示到密码框 |
| 1003 | 资质未通过，无法接单 | 弹窗引导去资质页 |
| 1004 | 验证码错误或已过期 | 就近提示到验证码框 |
| 1005 | 该身份证已被绑定 | 就近提示到身份证框 |
| 2001 | 订单状态不允许此操作 | Toast + 刷新订单详情 |
| 2002 | 余额不足 | 提示差额 |
| 2003 | 优惠券不可用 | Toast + 重拉可用券 |
| 2004 | 订单已支付，请勿重复支付 | Toast（幂等出口） |
| 2005 | 该课程已过上课时间，不支持退款 | Toast + 刷新 |
| 3001 | 会话不存在 | Toast |
| 4001 | 文件类型不支持 | Toast |
| 4002 | 文件大小超出限制 | Toast |

### 12.12 参考案例功能对照表（写报告「案例简介」章用）

| 参考案例功能 | 本项目对应 | 状态 |
|---|---|---|
| 登录 / 注册 / 记住密码 / 忘记密码 | `pages/auth/Login` | ✅ 保留 |
| 首页轮播 / 科目九宫格 / 公告滚动 / 名师推荐 | `views/home/HomeView` | ✅ 保留 |
| 按科目 / 科目+年级检索教师 | `views/tutor/TutorSearchView` | ✅ 保留 |
| 教师详情 + 预约 | `pages/tutor/TutorDetail` | ✅ 保留 |
| 我预约的教师 | `pages/order/OrderList` | ✅ 保留 |
| 我要提建议 | `pages/mine/FeedbackSubmit` | ✅ 保留 |
| 我的订单 / 我的钱包 | `pages/order/OrderList` / `pages/mine/Wallet` | ✅ 保留 |
| 奖学券 | `pages/mine/Coupon` | ✅ 保留 |
| 学习计划与总结 | `pages/chat/ReportDetail`（升级为学习报告） | ✅ 升级 |
| 消息论坛 / 优惠信息 | —— | ⛔ 不实现（报告中说明取舍理由） |
| 设置（原案例未实现） | `pages/mine/ProfileEdit` | ✅ 补全 |

---

**文档版本**：v1.0　**最后更新**：2026-10-14
**变更规则**：任何范围、接口、表结构变更，先改本文件再改代码。
