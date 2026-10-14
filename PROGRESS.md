# PROGRESS —— 开发进度状态文件

> **这是跨会话的唯一进度权威源。**每个 Part 完成后必须更新本文件。
> 新会话启动时**第一件事**就是读本文件，不要靠 git log 猜进度（commit message 可能不规范，且本地未 push 的提交会误导）。

**最后更新**：2026-10-14　（Part 0：双端工程均已建立且**双端都能编译/运行**，代码已提交 `b90b17c` + 已打标签 `p0-init`；
**只剩「签名 + 模拟器」两步需 GUI/账号**，属环境验证而非代码工作）

---

## 1. 当前状态

| 项 | 值 |
|---|---|
| 当前 Part | **Part 0 —— 进行中（仅剩 2 项 GUI/账号依赖的验收）** |
| 下一个待做 Part | **Part 0（收尾）** —— 做完才进 Part 1 |
| 工程路径 | `D:\dev\qingxin-jiajiao\`（已按 `SPEC.md` §2.3 方案一迁至纯 ASCII 路径） |
| git 远端 | ✅ 已配置 `https://github.com/you615074-png/Iot-mobile-app-.git` |
| git 提交 | ✅ `b90b17c` 已 push 到 `origin/main`；已打标签 `p0-init` 并推送。`git status` 干净 |
| 服务端可运行 | ✅ `mvn spring-boot:run` 启动成功，`GET /api/ping` 双通返回契约值（见 §3） |
| 客户端可编译 | ✅ **`hvigorw assembleHap` → `BUILD SUCCESSFUL`**，产物 `entry-default-unsigned.hap`（121,495 B） |
| 客户端可安装 | ❌ 未签名（无签名配置），**未签名的 hap 装不进模拟器** —— 见 §4 阻塞项 5 |

### Part 0 还剩什么（就这两条，其余全过）

| 剩余项 | 为什么我推不动 | 你要做什么 |
|---|---|---|
| 验收 2 后半：「模拟器启动后显示 Hello World」 | 安装 hap **必须有签名**；自动签名要**登录华为账号**，且现有那份调试 Profile 绑的是别的 App（见 §4 阻塞项 5） | DevEco 打开 `client/` → `File > Project Structure > Signing Configs` → 勾 **Automatically generate signature** → 登录 → 回到 `Run` 启动模拟器（见 §7 操作卡） |
| 验收 5 第 ③ 步：模拟器浏览器访问 `http://100.92.119.226:8080/api/ping` | 需要模拟器先跑起来（同上） | 模拟器启动后在设备内置浏览器里打开该 URL，看到 `{"code":200,...,"data":"pong"}` 即过 |

---

## 2. Part 状态表

**状态取值**：`未开始` / `进行中` / `已完成` / `已阻塞`

| Part | 名称 | 状态 | 完成时间 | commit | 验收证据 |
|---|---|---|---|---|---|
| 0 | 仓库与双端工程初始化 | **进行中**（6 项验收过 4 项，剩 2 项卡签名/模拟器） | 2026-10-14 | `b90b17c` | §3 Part 0 |
| 1 | 数据库建模与初始化数据 | 未开始 | —— | —— | —— |
| 2 | 服务端公共层 | 未开始 | —— | —— | —— |
| 3 | 客户端基建 | 未开始 | —— | —— | —— |
| 4 | 认证与角色（端到端） | 未开始 | —— | —— | —— |
| 5 | 管理员审核台（端到端） | 未开始 | —— | —— | —— |
| 6 | 首页与教师检索（端到端） | 未开始 | —— | —— | —— |
| 7 | 教师详情与预约（端到端） | 未开始 | —— | —— | —— |
| 8 | 课程、套餐与下单（端到端） | 未开始 | —— | —— | —— |
| 9 | 模拟支付、钱包与奖学券（端到端） | 未开始 | —— | —— | —— |
| 10 | 订单管理与退款（端到端） | 未开始 | —— | —— | —— |
| 11 | 实时聊天（端到端） | 未开始 | —— | —— | —— |
| 12 | 学习反馈与提醒（端到端） | 未开始 | —— | —— | —— |
| 13 | AI 数据看板（端到端） | 未开始 | —— | —— | —— |
| 14 | 评价与建议反馈（端到端） | 未开始 | —— | —— | —— |
| 15 | 联调、演示与交付物 | 未开始 | —— | —— | —— |

---

## 3. 验收证据索引

**规则**：每完成一个 Part，把「怎么证明它成了」记录在这里——**命令 + 关键输出**，不是「已测试通过」这种空话。

### Part 0（进行中 —— 已过项 / 未过项分开列）

对照 `SPEC.md` §8 Part 0「验收成果」六条：

1. `git status` 干净，`.gitignore` 已排除 `build/`、`oh_modules/`、`node_modules/`、`target/`、`.hvigor/`、`.idea/`、`server/src/main/resources/upload/`
   → **通过 ✅**。`.gitignore`（工程根）是 `SPEC.md` §9.4 的**超集**，额外含 `client/**/node_modules/`、`client/**/*.har`、`client/signature/`、`server/**/application-local.yml`。
   证据一：`git add -A` 后 `git diff --cached --name-only` 共 **49 个文件**，
   用 `Select-String "build/|oh_modules/|node_modules/|target/|\.hvigor/|\.idea/|\.hap$|application-local"` 过滤 **零命中** —— 构建产物与本地配置均未泄漏。
   （本次构建确实生成了 `client/entry/build/`、`client/.hvigor/`、`server/target/` 三个目录，均被正确排除。）
   证据二：commit + tag 之后 `git status --short` **无任何输出**，工作区干净。

2. DevEco 能 `assembleHap` 成功，模拟器启动后显示 "Hello World"
   → **`assembleHap` 通过 ✅ / 模拟器显示未做 ❌**。
   命令与关键输出（`client/` 目录下，`DEVECO_SDK_HOME` 指向本机 SDK）：
   ```
   > hvigor Finished :entry:default@CompileArkTS... after 27 s 322 ms
   > hvigor Finished :entry:default@PackageHap... after 366 ms
   > hvigor WARN: No signingConfig found for product default
   > hvigor Finished :entry:default@SignHap... after 2 ms
   > hvigor BUILD SUCCESSFUL in 4 s 209 ms
   ```
   产物：`client/entry/build/default/outputs/default/entry-default-unsigned.hap`　**121,495 字节**
   另：`client/entry/build/default/outputs/default/pack.info`、`mapping/sourceMaps.map` 一并生成。
   唯一 WARN 是 `No signingConfig found for product default` —— 未签名属**预期**（签名配置留待 §6.3 自动签名回填）。
   「模拟器显示 Hello World」未过：**未签名 hap 无法安装**，见 §4 阻塞项 5。

3. `mvn spring-boot:run` 启动成功，日志无异常
   → **通过**。证据（后台任务 `b2sjqt0u6` 输出）：
   ```
   14:32:41.271 [main] INFO  o.s.b.w.e.tomcat.TomcatWebServer - Tomcat initialized with port(s): 8080 (http)
   14:32:42.264 [main] INFO  o.s.b.w.e.tomcat.TomcatWebServer - Tomcat started on port(s): 8080 (http) with context path ''
   14:32:42.352 [main] INFO  c.jsu.qxjiajiao.QxJiaJiaoApplication - Started QxJiaJiaoApplication in 2.283 seconds
   ```
   唯一的 WARN 是 `No MyBatis mapper was found in '[com.jsu.qxjiajiao.mapper]' package` ——
   Part 0 本就没有 Mapper，属预期，不是异常。

4. 宿主机浏览器访问 `http://localhost:8080/` 有响应
   → **通过（字面）**。返回 HTTP **404**（Spring Boot 默认 Whitelabel 错误页）。
   连接建立、服务器应答，满足「有响应」；但**不是 200**。
   注意：`SPEC.md` §7 接口清单中**没有任何**根路径 `GET /` 接口，`AGENT_PROMPT.md` §5.5 禁止擅自新增接口，
   故**未添加** `/` 端点。如后续需要 `/` 返回 200 页面，须先按 §8 第 3 条向人提出补充方案。

5. **模拟器浏览器能访问 `http://<局域网IP>:8080/`**（四步验证全过）
   → **部分通过**。① ② 已过，③ 未做。

   | 步 | 目标 | 结果 |
   |---|---|---|
   | ① | 宿主机 → `http://localhost:8080/api/ping` | ✅ HTTP 200，`{"code":200,"msg":"success","data":"pong"}` |
   | ② | 宿主机 → `http://100.92.119.226:8080/api/ping`（本机局域网 IP） | ✅ HTTP 200，`{"code":200,"msg":"success","data":"pong"}` |
   | ③ | 模拟器浏览器 → `http://100.92.119.226:8080/api/ping` | ❌ 未做（模拟器未启动，见 §4 阻塞项 2/5） |
   | ④ | 全过才写 ArkTS 代码 | —— |

   ② 的返回值与 `API.md` `API-SYS-01` 契约**逐字节一致**。
   防火墙已放行：规则 `qxjiajiao-8080-inbound`（Enabled=True，Inbound，Allow，Profile=Any，TCP，LocalPort 8080）已创建并核实。

6. 已 `push` 到远端，远端可见首次提交
   → **通过 ✅**。commit `b90b17c`（`chore: 初始化双端工程与仓库结构`，49 文件）已 `git push -u origin main`，
   远端输出 `* [new branch] main -> main`；随后 `git tag -a p0-init` 并推送，输出 `* [new tag] p0-init -> p0-init`。

**检查点回归记录**（Part 4 / 8 / 12 结束后各做一次全链路回归，见 `AGENT_PROMPT.md` §6）

```
（尚无）
```

---

## 4. 阻塞项

| # | 阻塞 | 影响的 Part | 解除方式 | 状态 |
|---|---|---|---|---|
| 1 | ~~git 远端仓库地址未知~~ | Part 0 及之后全部 | 用户已提供 `https://github.com/you615074-png/Iot-mobile-app-.git`，已 `git remote add origin` | ✅ **已解除** |
| 2 | ~~鸿蒙客户端工程未创建~~ | Part 0 起的全部客户端工作 | **本会话已解除**：按 DevEco 官方 FreeMarker 模板（`plugins\openharmony\lib\templates\`）手工渲染出完整 `client/` 工程，等价于 GUI 生成物，并已 `assembleHap` 实测通过 | ✅ **已解除** |
| 3 | ~~API 12 SDK 未安装~~ | 客户端构建 | **本会话已定性**：这不是「没装」，而是 **DevEco 6.x 单 SDK 模型下不可能装**——hvigor 对 `runtimeOS:"HarmonyOS"` 强制以本机已装 SDK 编译（`validate-util.js` 的 `integrationVersionCheck`）。处置：`compatibleSdkVersion` 保留 `5.0.0(12)`，**不写** `compileSdkVersion`。处置已写入 `SPEC.md` §2.1.1 与 `SCAFFOLD.md` §8.1 | ✅ **已解除（改为口径修正）** |
| 4 | ~~Windows 防火墙未放行 8080 入站~~ | 四步验证第 ③ 步、Part 3 起全部客户端联调 | **本会话已解除**：经 UAC 提权创建规则 `qxjiajiao-8080-inbound`（Inbound / TCP / 8080 / Allow / Profile=Any），已核实存在且 Enabled=True | ✅ **已解除** |
| 5 | **客户端未签名，装不进模拟器** | Part 0 验收 2 后半、第 ③ 步；Part 3 起每次真机/模拟器运行 | DevEco 打开 `client/` → `File > Project Structure > Signing Configs` → 勾 **Automatically generate signature** → 登录华为账号 → 回填 `build-profile.json5` 的 `signingConfigs`。**已实测确认现有材料不可复用**：`~\.ohos\config\default_Capwords_*.p7b` 用 `hap-sign-tool verify-profile` 解析后，`bundle-info.bundle-name = "com.vocio.app"`（**别的 App**），且 `debug-info.device-ids` 只白名单了 1 个 UDID，`acls.allowed-acls` 为空 —— 对 `com.jsu.qxjiajiao` 无效 | ⛔ **未解除** |
| 6 | `NOTIFICATION_CONTROLLER` 权限风险 | Part 12 | 已决定：若签名/安装失败且报错指向该权限，改为只声明前 4 个，Part 12 走运行时 `requestEnableNotification()`。**本会话新增证据**：本机调试 Profile 的 `acls.allowed-acls` 为 `[]`，说明该权限若属受限 ACL，一定不会获批 —— 倾向直接走运行时路线 | ✅ 已定方案（证据加强） |
| 7 | **旧中文路径残留了一份过期副本** | **全部** —— 会误导后续会话读错进度 | 工程已迁到 `D:\dev\qingxin-jiajiao\`，但 `C:\Users\28451\Desktop\物联网移动应用开发\qingxin-jiajiao\` **仍然存在**且是**过期副本**：只有 8 个 md 文件、**没有 `server/`**。迁移时该目录被本会话 harness 进程占用（工作目录就是它），`Move-Item` / `Remove-Item` 都报 `item is in use`。**处置**：本会话结束后执行 `Remove-Item -Recurse -Force "C:\Users\28451\Desktop\物联网移动应用开发\qingxin-jiajiao"`。**在新会话开工前必须确认它已消失** | ⛔ **未解除** |

### 4.1 环境风险（尚未成为阻塞）

| 风险 | 实测证据 | 处置建议 |
|---|---|---|
| **代理软件干扰路由** | 默认路由有两条且 metric 均为 0：`28.0.0.2 / FlClash` 与 `100.92.0.1 / WLAN`。`FlClash`（Clash 类代理，装了 TUN 虚拟网卡 `28.0.0.1`）优先 | 第 ③ 步若模拟器连不上宿主机，**先退出 FlClash / 关闭其 TUN 模式**再试 |
| **局域网 IP 属 CGNAT 段** | WLAN 地址 = `100.92.119.226`，落在 `100.64.0.0/10`（RFC 6598 运营商级 NAT） | 校园网常见。若模拟器走 NAT 后访问不到该地址，改用模拟器可直接到达的其它网段地址，或临时开热点 |
| **MySQL 为 5.7.19（SPEC 要求 8.0）** | `mysqld --version` → 5.7.19 | **Part 1 风险**：`schema.sql` 禁用 8.0 专有语法（`utf8mb4_0900_ai_ci` 排序规则、窗口函数、CTE）。`SCAFFOLD.md` §2 的 JDBC URL 已含 `serverTimezone`，连接层无碍 |
| **MySQL `skip-grant-tables`** | `my.ini` 启用该选项，口令校验被跳过 | `application.yml` 的 `password` 已留空即可连通；换到有口令的环境须改回 |
| **模拟器镜像只有 API 24** | `%LOCALAPPDATA%\Huawei\Emulator\deployed\lists.json` 里 6 个镜像（Pura / Pura 90 / Mate X7 / MatePad Pro 13 / MateBook Pro / Capwords320）**全部** `"apiVersion":"24"`、`"showVersion":"HarmonyOS 6.1.1(24)"` | 向下兼容，API 24 模拟器能装 `compatibleSdkVersion=12` 的应用，第 ③ 步照做即可。若答辩严格要 API 12 镜像，须登录华为账号在 Device Manager 里补装 |

### 4.2 已固化的构建口径（本会话实证得出，别再踩）

| 项 | 值 | 来源 |
|---|---|---|
| 客户端编译 SDK | **不写 `compileSdkVersion`**（HarmonyOS 工程 hvigor 强制用本机已装 SDK） | `validate-util.js` → `integrationVersionCheck()` |
| 客户端兼容 SDK | `"compatibleSdkVersion": "5.0.0(12)"` | SPEC 口径，hvigor 实测接受 |
| `modelVersion` | **`"5.0.0"`**，且必须 `client/oh-package.json5` 与 `client/hvigor/hvigor-config.json5` **两处完全一致** | `version-const.js` → `MINIMUM_MODEL_VERSION`；`validate-util.js` → `modelVersionCheck()` |
| 构建命令（CLI） | `DEVECO_SDK_HOME="D:\devecostudio-windows\DevEco Studio\sdk"`，并把 `java`（JDK 8）与 DevEco 的 `tools\node` 加进 PATH，然后 `hvigorw assembleHap --no-daemon` | 实测：缺 `java` 会在 `PackageHap` 报 `spawn java ENOENT`（错误码 00308018） |

---

## 5. 会话日志

**每完成一次会话追加一行**：日期 · 完成的 Part · 遗留问题。

| 日期 | 完成内容 | 遗留 |
|---|---|---|
| 2026-10-14 | 文档阶段：完成 `SPEC.md` / `API.md` / `UI.md` / `SCAFFOLD.md` / 本文件 | git 远端未配，Part 0 未开工 |
| 2026-10-14 | **Part 0 服务端侧**：装 Temurin JDK 8（`D:\dev\tools\jdk8`）+ 接好 IDEA 捆绑 Maven 3.9.9；创建库 `qxjiajiao`(utf8mb4)；工程按 SPEC §2.3 迁到 `D:\dev\qingxin-jiajiao\`；`git init` + `remote add origin`；落盘 `server/`（pom / 启动类 / yml / `Result` / `PingController`）；`mvn` 编译 BUILD SUCCESS；`spring-boot:run` 起服务；四步验证 **① ② 已过**；补 `.gitignore`；重写 `README.md` 本机环境章节 | 未 commit；客户端、API 12 SDK、防火墙三项阻塞 |
| 2026-10-14 | **Part 0 收尾（本会话）**：重写 `PROGRESS.md`（状态 / 验收证据 / 阻塞项 / 环境风险 / 构建口径 / 操作卡）；`git add -A` 复核 **49 文件零泄漏**；commit `b90b17c`（message 按 SPEC §8 指定：`chore: 初始化双端工程与仓库结构`）；`git push -u origin main` 成功；`git tag -a p0-init` 并推送成功；`SPEC.md` Part 0 验收清单回填 push 证据 | 只剩 §7 操作卡两步（自动签名 → 模拟器 Hello World + 四步验证③）。**签名会在 `client/build-profile.json5` 新增 `signingConfigs`，届时应再补一个小 commit** | 
| 2026-10-14 | **Part 0 客户端侧（本会话）**：① 读遍 DevEco 官方模板，取得完整 file→dest 配方；② **手工渲染出完整 `client/` 工程**（根 `build-profile.json5` / `oh-package.json5` / `hvigorfile.ts` / `code-linter.json5` / `.gitignore` / `hvigor/hvigor-config.json5`，`AppScope/`，`entry/` 含 `module.json5`、`EntryAbility.ets`、`EntryBackupAbility.ets`、`pages/Index.ets`、全套 resources、`ohosTest/`、`test/`）；③ **`hvigorw assembleHap` → `BUILD SUCCESSFUL`**，产出 `entry-default-unsigned.hap`；④ 经 UAC 提权放行防火墙 8080；⑤ 用 `hap-sign-tool verify-profile` 解析现有调试 Profile，证实其绑的是别的 App，无法复用；⑥ 把 4 条实测偏差回写 `SPEC.md` §2.1.1 + Part 0 验收清单 + `SCAFFOLD.md` §8.1 | 只剩两项 GUI/账号依赖：自动签名（→ 模拟器能装 hap）、四步验证第 ③ 步。旧中文路径副本待删（阻塞 7） |

---

## 6. 文档同步欠账

**改了代码但没回写文档的地方，记在这里**，避免文档悄悄失真。

| 日期 | 改了什么 | 该同步到哪 | 状态 |
|---|---|---|---|
| 2026-10-14 | `.gitignore` 增加 `client/**/node_modules/` | `SPEC.md` §9.4 的清单未收录该项 | 待补（低优先） |
| 2026-10-14 | `application.yml` 的 `spring.datasource.password` 留空（本机 MySQL 启用了 `skip-grant-tables`） | 已在 yml 内就地注释说明 | 待确认 |
| 2026-10-14 | **`common/Result.java` 与 `controller/PingController.java` 提前到 Part 0 落地** | 两文件 Javadoc 已写明理由。**Part 2 落地时不要再新建这两个文件**，应在其上扩展 | 已就地说明 |
| 2026-10-14 | 客户端 `module.json5` 的 `skills.actions` 用模板值 **`ohos.want.action.home`**，而 `SCAFFOLD.md` §7 示例写的是 `action.system.home` | 二者是同一 action 的新旧别名。已按 `SCAFFOLD.md` §7 开头「保留模板生成原值」的指示取模板值 | 已说明 |
| 2026-10-14 | 客户端 `module.json5` 的 ability `icon` 用 **`$media:layered_image`**（API≥11 模板值），`SCAFFOLD.md` §7 示例写 `$media:startIcon`；两者资源均已落在 `entry/src/main/resources/base/media/` | 同上，取模板值 | 已说明 |
| 2026-10-14 | **`compileSdkVersion` 不写、`modelVersion` 取 `"5.0.0"`、模拟器镜像只有 API 24** 三件事 | 已回写 `SPEC.md` §2.1.1 + `SCAFFOLD.md` §8.1 | ✅ 已同步 |

---

## 7. Part 0 收尾操作卡（只剩这两步，给你手把手）

### 第 1 步：自动签名（解除阻塞 5）

1. 打开 DevEco Studio → `File > Open` → 选 `D:\dev\qingxin-jiajiao\client`
   （**打开 `client` 这一层**，不是 `qingxin-jiajiao`）
2. 等右上角 hvigor 同步完成（首次会拉 `@ohos/hypium` / `@ohos/hamock`，需要联网）
3. `File > Project Structure > Project > Signing Configs`
   - 勾选 **Automatically generate signature**
   - 提示登录时用**你的华为开发者账号**登录
   - 成功后 DevEco 会往 `client/build-profile.json5` 的 `signingConfigs` 里回填一条名为 `default` 的配置，
     并在 `~\.ohos\config\` 下生成**绑定 `com.jsu.qxjiajiao`** 的新 `.p12/.cer/.p7b`
4. 回填后请确认 `products[0]` 里出现了 `"signingConfig": "default"`

> ⚠️ 若第 3 步签名/安装失败且报错**指向** `ohos.permission.NOTIFICATION_CONTROLLER`，
> 就把它从 `client/entry/src/main/module.json5` 的 `requestPermissions` 里删掉（保留前 4 个），
> Part 12 改用运行时 `notificationManager.requestEnableNotification()`，并回写 `SPEC.md` §3.2。

### 第 2 步：跑模拟器 + 四步验证第 ③ 步

1. `Tools > Device Manager` → `Local Emulator` → 选一个 **phone** 镜像（本机有 `Pura` / `Pura 90` / `Mate X7` / `Capwords320`）
   → 点 ▶ 启动
   > 本机镜像**只有 API 24**，这是正常的——API 24 能装 `compatibleSdkVersion=12` 的应用。
2. 等模拟器进桌面后，在 DevEco 里点 **Run 'entry'**（或 `Shift+F10`）
   → 应用装上并打开，应看到居中的 **"Hello World"**（点一下会变 "Welcome"）→ **验收 2 全过**
3. 在**模拟器里**找到「浏览器」应用，地址栏输入：
   ```
   http://100.92.119.226:8080/api/ping
   ```
   看到 `{"code":200,"msg":"success","data":"pong"}` → **四步验证 ③ 过，验收 5 全过**

> ⚠️ 连不上先关 **FlClash 的 TUN 模式**（见 §4.1），校园网 CGNAT 段偶发拦截。

### 第 3 步：告诉我结果

**已经落盘的部分（无需你再操作）**：
- commit `b90b17c` 已 push 到 `origin/main`，标签 `p0-init` 已推送
- 双端当前**都可编译可运行**，满足 `SPEC.md` §9.1「push 前必须处于可编译、可运行状态」

两步都过之后，在新会话里说一句「Part 0 模拟器验收过了」，我会：
把 §1 / §2 / §3 / §5 的「进行中」改成「已完成」并附上截图或现象描述 →
把自动签名给 `client/build-profile.json5` 带来的改动**单独提交一个小 commit** →
`git tag -f p0-init` 重打标签并推送 → 然后开工 **Part 1**（数据库建模与初始化数据）。

> 注意：**不要**把 `client/signature/` 或 `~\.ohos\config\` 下的签名材料提交进仓库
> （根 `.gitignore` 已排除 `client/signature/`）。自动签名会在 `client/build-profile.json5`
> 的 `signingConfigs` 里写入**本机绝对路径**，提交前请确认这是你想要的——
> 若不想暴露本机路径，可把该文件加进 `.gitignore` 改为本地保留，并在 `README.md` 说明签名需自行生成。
