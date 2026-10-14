# 倾心家教 · 鸿蒙移动应用开发

物联网移动应用开发 · 大作业一　｜　单人全栈开发

> **所有任务、验收标准、文件路径以 [`SPEC.md`](./SPEC.md) 为准。**
> 开始任何工作前先读 SPEC.md；范围/接口/表结构有变更时，先改 SPEC.md 再改代码。

## 快速导航

| 我要… | 去哪里 |
|---|---|
| **让 agent 开工** | [`AGENT_PROMPT.md`](./AGENT_PROMPT.md) —— 操作规程，粘 §0 那一句即可 |
| **看现在做到哪了** | [`PROGRESS.md`](./PROGRESS.md) —— 跨会话进度状态 |
| 看任务分解与验收标准 | [`SPEC.md` §8](./SPEC.md#8-任务分解) |
| 看数据库表设计 | [`SPEC.md` §6](./SPEC.md#6-数据库设计) |
| 看接口**索引** | [`SPEC.md` §7](./SPEC.md#7-接口清单) |
| 看接口**字段契约**（请求/响应/错误码） | [`API.md`](./API.md) |
| 看界面稿与色值字号 | [`UI.md`](./UI.md) |
| 看脚手架全文（pom/yml/module.json5/签名） | [`SCAFFOLD.md`](./SCAFFOLD.md) |
| 看 git 提交与 push 规范 | [`SPEC.md` §9](./SPEC.md#9-git-工作流) |
| 看交付物与评分自检 | [`SPEC.md` §10](./SPEC.md#10-全局验收清单) |

## 怎么开工

**一次会话做一个 Part。**开一个新会话，粘这一句：

```
读 qingxin-jiajiao/PROGRESS.md，再读 qingxin-jiajiao/AGENT_PROMPT.md，
然后按 AGENT_PROMPT.md 的规程执行「下一个待做 Part」。
```

agent 会自己读 `PROGRESS.md` 找准位置，做完**恰好一个 Part**、逐条自证验收、commit + push、更新 `PROGRESS.md`，然后停下。重复 16 次即完成全部开发。

> **不要让一个会话硬跑 16 个 Part**——上下文会耗尽，后半程质量骤降。理由与替代方案见 `AGENT_PROMPT.md` §9。

## 文档分工（重要）

六份文档是**索引 + 展开 + 状态**的关系，不是并列。改任何一处都要按链条同步：

```
SPEC.md          总纲：范围、19 张表、任务分解、验收标准（索引）
  ├─ API.md        §7 接口清单 的字段级展开（65 条）
  ├─ UI.md         §3.2 客户端目录 的逐页界面展开（40 个页面）
  └─ SCAFFOLD.md   可复制的配置全文（pom / yml / module.json5 / 签名）
AGENT_PROMPT.md  给 agent 的操作规程（怎么把上面四份变成代码）
PROGRESS.md      跨会话进度状态（做到哪了、卡在哪）
```

**同步铁律**：表结构变更改 `SPEC.md` §6 → 接口变更先改 `SPEC.md` §7 **再**改 `API.md` → 界面变更改 `SPEC.md` §3.2.1 **再**改 `UI.md`。**冲突时一律以 `SPEC.md` 为准。**

## 目录

```
qingxin-jiajiao/
├── SPEC.md          任务书（唯一权威源）
├── API.md           接口契约
├── UI.md            界面稿与视觉规范
├── SCAFFOLD.md      脚手架落地手册
├── AGENT_PROMPT.md  开发执行操作规程
├── PROGRESS.md      开发进度状态
├── README.md        本文件
├── client/          鸿蒙客户端（DevEco Studio 工程）
├── server/          Spring Boot 服务端（Maven 工程）
├── docs/            报告 / PPT / 演示 / 图表 / 截图
└── scripts/         辅助脚本
```

## 起步顺序

1. 阅读 `SPEC.md` 第 2 章，确认本地环境与**网络连通四步验证**
2. 执行 **Part 0**：初始化双端工程 + 配置 git 远端
3. 按 Part 0 → Part 15 顺序推进，**每个 Part 完成即 commit + push**

## 工程位置

> **本工程已按 `SPEC.md` §2.3 迁至纯 ASCII 路径**：`D:\dev\qingxin-jiajiao\`
>
> 原路径 `C:\Users\28451\Desktop\物联网移动应用开发\qingxin-jiajiao\` 含中文，hvigor/ohpm 与部分 Maven
> 插件在中文路径下有构建失败记录。**此后一切命令都在新路径下执行**。

## 本地环境（本机实测）

| 用途 | 本机实际 | 位置 / 验证 |
|---|---|---|
| 服务端 JDK | **Temurin JDK 8**（`1.8.0_504`） | `D:\dev\tools\jdk8`（`JAVA_HOME` 已指向此，用户级环境变量） |
| 服务端构建 | **Maven 3.9.9** | `D:\IDEA\IntelliJ IDEA 2025.2.1\plugins\maven\lib\maven3`（随 IDEA 捆绑，已加入用户 PATH） |
| 数据库 | **MySQL 5.7.19**，端口 3306，服务名 `MySQL` | `D:\mysql\mysql-5.7.19` |
| 鸿蒙 IDE | **DevEco Studio 6.1.1.290** | `D:\devecostudio-windows\DevEco Studio` |
| 鸿蒙 SDK | 需在 SDK Manager 中补装 **HarmonyOS 5.0.0(12)** | 见下方「已知偏差」 |
| 版本控制 | Git 2.49.0，远端 `origin` 已配 | `https://github.com/you615074-png/Iot-mobile-app-.git` |

> ⚠️ **`JAVA_HOME` 与 `mvn` 均已配好，但二者是用户级环境变量**：若某个终端在配置之前就已打开，
> 需要重开终端才能读到。临时使用可在命令前显式指定：
> ```powershell
> $env:JAVA_HOME = "D:\dev\tools\jdk8"
> ```

### 已知偏差（与 `SPEC.md` §2.1 的环境要求不一致）

| 项 | SPEC 要求 | 本机实际 | 影响与处置 |
|---|---|---|---|
| JDK | 8 或 17（方案 A 取 8） | 已装 Temurin 8 ✅ | 无偏差，方案 A 口径成立 |
| Maven | 3.8+ | 3.9.9（IDEA 捆绑）✅ | 无偏差 |
| MySQL | **8.0** | **5.7.19** | ⚠️ **Part 1 风险**：`schema.sql` 若用到 8.0 专有语法（`utf8mb4_0900_ai_ci` 排序规则、窗口函数、CTE）在 5.7 上会失败，Part 1 需按 5.7 语法落地 |
| MySQL 认证 | 账号密码 | `my.ini` 启用了 **`skip-grant-tables`**（跳过权限校验） | 故 `application.yml` 的 `password` 留空即可连通；换环境请改回 |
| 鸿蒙 SDK | `5.0.0(12)` | 现装为 API 24（HarmonyOS 6.1.1） | ⚠️ 需在 DevEco `SDK Manager` 补装 API 12，工程按 `compatibleSdkVersion: 5.0.0(12)` 建 |

## 本地启动

```bash
# 1. 初始化数据库（Part 1 交付该脚本）
scripts/init-db.bat

# 2. 启动服务端（端口 8080，监听 0.0.0.0）
cd server && mvn spring-boot:run

# 3. 客户端：DevEco Studio 打开 client/ → 启动 API 12 模拟器 → Run
```

**服务端连通性四步验证**（`SPEC.md` §2.2，Part 0 必过项）：

```
① 宿主机浏览器访问  http://localhost:8080/api/ping
    → 期望：{"code":200,"msg":"success","data":"pong"}
② 宿主机浏览器改用局域网 IP 再访问一次  http://<局域网IP>:8080/api/ping
③ 模拟器里的浏览器访问  http://<局域网IP>:8080/api/ping
④ 以上全过，才回到 DevEco 写 ArkTS 代码
```

> 客户端 `BASE_URL` 需配置为**宿主机局域网 IP**（见 `SPEC.md` §2.2），不能用 `127.0.0.1`。
> Windows 防火墙需放行 8080 入站。
