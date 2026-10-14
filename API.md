# API —— 「倾心家教」接口契约

> **本文档与 `SPEC.md` 的关系**
> `SPEC.md` 是总纲（需求、范围、数据库、任务分解）。
> 本文档是 `SPEC.md` §7「接口清单」的**完整展开**——把 62 条路径补全为**可直接编码的请求/响应契约**。
> **冲突时以 `SPEC.md` 为准**；若本文档的字段与 `SPEC.md` §6.3 的表字段冲突，改本文档。
> **本文件是双端唯一字段权威源**：客户端 `client/entry/src/main/ets/models/*.ets` 与服务端 `server/.../vo/*.java` 的字段名必须逐字对齐本文档。

---

## 0. 目录

- [1. 通用约定](#1-通用约定)
- [2. 数据模型（VO 字典）](#2-数据模型vo-字典)
- [3. 枚举字典](#3-枚举字典)
- [4. 接口详情](#4-接口详情)
- [5. WebSocket 协议](#5-websocket-协议)
- [6. 附录：与 SPEC §7 的对应关系](#6-附录与-spec-7-的对应关系)

---

## 1. 通用约定

### 1.1 基地址

| 项 | 值 |
|---|---|
| 服务端监听 | `0.0.0.0:8080` |
| 客户端 `BASE_URL` | `http://<宿主机局域网IP>:8080`（**禁止 `127.0.0.1`**，见 SPEC §2.2） |
| 接口前缀 | `/api` |
| 文档地址 | `http://<IP>:8080/doc.html`（Knife4j） |
| WebSocket | `ws://<IP>:8080/ws/chat?token=<jwt>` |

### 1.2 请求头

| Header | 值 | 说明 |
|---|---|---|
| `Content-Type` | `application/json;charset=UTF-8` | 除文件上传外 |
| `Authorization` | `Bearer <token>` | 登录后所有受保护接口必带 |
| `Content-Type` | `multipart/form-data` | 仅 `/api/file/upload` |

### 1.3 统一响应体

**除 `/api/order/export`（返回 CSV 文件流）外，所有接口均返回此结构：**

```json
{
  "code": 200,
  "msg": "success",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `code` | int | 200 表示业务成功；非 200 表示失败（见 §1.6） |
| `msg` | string | 失败时为中文提示，可直接 Toast；成功时为 `"success"` |
| `data` | any | 业务数据，失败时为 `null` |

**客户端 `request.ets` 统一处理规则**：
- `code === 200` → 返回 `data`
- `code === 401` → 清 Token、跳登录页、**不弹 Toast**
- 其他 → `Toast(msg)` 并抛错

### 1.4 分页

**请求参数**（所有列表接口通用，GET 放 query，POST 放 body）：

| 参数 | 类型 | 默认 | 说明 |
|---|---|---|---|
| `page` | int | 1 | **从 1 开始** |
| `size` | int | 10 | 每页条数，上限 100 |

**响应结构**（固定放在 `data` 里）：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 37,
    "records": []
  }
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `total` | long | 总条数 |
| `records` | array | 当前页数据数组 |

### 1.5 字段格式约定

| 类型 | 格式 | 示例 |
|---|---|---|
| 日期时间 | `yyyy-MM-dd HH:mm:ss` 字符串 | `"2026-10-14 15:30:00"` |
| 日期 | `yyyy-MM-dd` 字符串 | `"2026-10-14"` |
| 金额 | JSON number，2 位小数 | `199.00`、`0.00` |
| 比率/百分比 | JSON number，2 位小数，**存 0–100 的数** | `85.50`（表示 85.5%） |
| 主键 ID | JSON number（long） | `1001` |
| 手机号 | **列表/详情默认脱敏**：`138****8888`；仅「我的资料」返回原值 | —— |
| 身份证 | **永远脱敏**：`3201**********1234` | —— |
| 空值 | `null`，**不返回空字符串** | —— |
| 数组 | 无数据时返回 `[]`，**不返回 `null`** | —— |

> **金额铁律**：全程 `BigDecimal`，**禁止 `double`**。前端展示统一走 `utils/format.ets` 的 `formatMoney()`。

### 1.6 错误码表

| code | msg 示例 | 触发场景 |
|---|---|---|
| 200 | success | 成功 |
| 400 | 参数校验失败：手机号格式不正确 | `@Valid` 失败 |
| 401 | 登录已失效，请重新登录 | Token 缺失/过期/非法；账号被禁用 |
| 403 | 无权限访问该资源 | 角色不符；越权访问他人数据 |
| 404 | 资源不存在 | 订单/教师/会话不存在 |
| 500 | 服务器开小差了，请稍后重试 | 未捕获异常（**响应体不含堆栈**） |
| 1001 | 该手机号已注册 | 注册重复 |
| 1002 | 账号或密码错误 | 登录失败 |
| 1003 | 您的资质尚未通过审核，暂时无法接单 | 教师未通过审核执行教师操作 |
| 1004 | 验证码错误或已过期 | 验证码校验失败 |
| 1005 | 该身份证已被绑定 | 实名重复 |
| 2001 | 当前订单状态不支持该操作 | 状态机拒绝 |
| 2002 | 余额不足 | 余额支付 |
| 2003 | 该优惠券不可用 | 券已用/已过期/不满足门槛 |
| 2004 | 订单已支付，请勿重复支付 | 幂等拦截 |
| 2005 | 该课程已过上课时间，不支持退款 | 退款规则 |
| 3001 | 会话不存在 | 聊天 |
| 4001 | 文件类型不支持 | 上传 |
| 4002 | 文件大小超出限制 | 上传 |

### 1.7 鉴权与角色控制

| 标记 | 含义 |
|---|---|
| 🔓 | 公开接口，无需 Token |
| 🔐 | 需登录，任意角色 |
| 👤P | 仅 `PARENT` |
| 👤S | 仅 `STUDENT` |
| 👤T | 仅 `TEACHER`（且资质已通过） |
| 👤A | 仅 `ADMIN` |

**越权防线**：所有涉及「我的数据」的查询，服务端**强制以 Token 中的 `userId` 为条件**，绝不信任请求参数里传来的 `userId`。详见 SPEC §10.1 第 11 条的越权自查要求。

---

## 2. 数据模型（VO 字典）

> 下文接口一律**引用本章的 VO**，不重复定义字段。
> 命名规则：`xxxVO` 为出参，`xxxDTO` 为入参。

### 2.1 `UserVO` —— 用户信息（脱敏）

```json
{
  "id": 1001,
  "phone": "13800000001",
  "role": "PARENT",
  "nickname": "小明妈妈",
  "avatar": "http://192.168.1.100:8080/upload/2026/10/a1b2c3.jpg",
  "realName": "王芳",
  "idCard": "3201**********1234",
  "gender": 0,
  "address": "江苏省镇江市京口区学府路301号",
  "status": 0,
  "createTime": "2026-10-01 09:12:33"
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | long | 用户 ID |
| `phone` | string | 手机号（**仅 `/api/auth/me`、`/api/user/profile` 返回原值**，其他场景脱敏） |
| `role` | string | 见 §3.1 |
| `nickname` | string | 昵称 |
| `avatar` | string | 头像完整 URL（可直接给 `Image()` 用）；无头像时为 `null` |
| `realName` | string | 真实姓名，未实名时 `null` |
| `idCard` | string | **永远脱敏** |
| `gender` | int | `0` 未知 · `1` 男 · `2` 女 |
| `address` | string | 地址 |
| `status` | int | `0` 正常 · `1` 禁用 · `2` 待审核 |
| `createTime` | string | 注册时间 |

### 2.2 `LoginVO` —— 登录结果

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEwMDF9.xxxxx",
  "expireAt": "2026-10-21 15:30:00",
  "user": { "参见 UserVO" }
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `token` | string | JWT，有效期 7 天 |
| `expireAt` | string | 过期时间（客户端可据此提前刷新） |
| `user` | `UserVO` | 用户信息 |

> 客户端登录成功后：`token` 写 `Preferences` + `AppStorage`，`user` 写 `UserStore`，然后按 `user.role` 跳首屏。

### 2.3 `TeacherCardVO` —— 教师卡片（列表用，字段少）

```json
{
  "teacherId": 2001,
  "nickname": "李老师",
  "avatar": "http://192.168.1.100:8080/upload/2026/10/t1.jpg",
  "gender": 2,
  "subjects": ["数学", "物理"],
  "grades": ["七年级", "八年级", "九年级"],
  "teachYears": 5,
  "education": "硕士",
  "school": "江苏大学",
  "pricePerHour": 120.00,
  "rating": 4.80,
  "orderCount": 132,
  "favorited": false
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `teacherId` | long | **即 `sys_user.id`**（教师没有独立主键） |
| `nickname` | string | 姓名/昵称 |
| `avatar` | string | 头像 URL |
| `gender` | int | `1` 男 · `2` 女 |
| `subjects` | string[] | 科目数组（服务端从 `subjects` 逗号串拆分，**客户端不要自己 split**） |
| `grades` | string[] | 年级数组（同上） |
| `teachYears` | int | 教龄 |
| `education` | string | 学历 |
| `school` | string | 毕业院校 |
| `pricePerHour` | number | 时薪 |
| `rating` | number | 综合评分 0–5，1 位小数 |
| `orderCount` | int | 累计接单数 |
| `favorited` | boolean | 当前登录用户是否已收藏 |

### 2.4 `TeacherVO` —— 教师详情（继承 `TeacherCardVO` 全部字段，另加）

```json
{
  "...": "TeacherCardVO 的全部字段",
  "phone": "138****0002",
  "address": "江苏省镇江市京口区",
  "intro": "从教五年，擅长初高中数学提分……",
  "acceptRate": 92.30,
  "goodRate": 96.50,
  "refundRate": 1.20,
  "certStatus": "已通过",
  "courses": [ { "参见 CourseVO" } ],
  "evaluations": [ { "参见 EvaluationVO" } ]
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `phone` | string | **脱敏** |
| `address` | string | 授课地址 |
| `intro` | string | 教学经历/简介（可含换行） |
| `acceptRate` | number | 接单率 % |
| `goodRate` | number | 好评率 % |
| `refundRate` | number | 退课率 % |
| `certStatus` | string | 见 §3.3 |
| `courses` | `CourseVO[]` | 该教师开的课 |
| `evaluations` | `EvaluationVO[]` | 最近 10 条评价 |

### 2.5 `CertVO` —— 教师资质

```json
{
  "id": 301,
  "userId": 2001,
  "nickname": "李老师",
  "phone": "138****0002",
  "certType": "教师资格证",
  "certNo": "2023XXXXXXX",
  "certImg": "http://192.168.1.100:8080/upload/2026/10/cert1.jpg",
  "status": "待审核",
  "rejectReason": null,
  "auditBy": null,
  "auditTime": null,
  "createTime": "2026-10-14 10:00:00"
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `status` | string | 见 §3.3 |
| `rejectReason` | string | 仅「已驳回」时有值 |
| `auditBy` / `auditTime` | —— | 管理员 ID / 审核时间 |

### 2.6 `CourseVO`

```json
{
  "id": 501,
  "teacherId": 2001,
  "teacherName": "李老师",
  "teacherAvatar": "http://192.168.1.100:8080/upload/2026/10/t1.jpg",
  "subject": "数学",
  "grade": "八年级",
  "title": "初中数学同步提分",
  "cover": "http://192.168.1.100:8080/upload/2026/10/c1.jpg",
  "pricePerHour": 120.00,
  "durationMin": 60,
  "classType": "线下",
  "intro": "同步教材进度，重点突破函数与几何",
  "status": 1,
  "packages": [ { "参见 PackageVO" } ]
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `durationMin` | int | 单次课时长（分钟） |
| `classType` | string | `线上` / `线下` |
| `status` | int | `1` 上架 · `0` 下架 |
| `packages` | `PackageVO[]` | 仅详情接口返回；列表接口为 `null` |

### 2.7 `PackageVO` —— 套餐

```json
{
  "id": 601,
  "courseId": 501,
  "lessonCount": 10,
  "originPrice": 1200.00,
  "packagePrice": 1020.00,
  "discountRate": 85.00,
  "validDays": 180,
  "label": "10课时套餐 · 85折"
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `lessonCount` | int | 课时数 |
| `originPrice` | number | 原价（= 单价 × 课时数） |
| `packagePrice` | number | 套餐价 |
| `discountRate` | number | 折扣率 %（85.00 表示 85 折） |
| `validDays` | int | 有效期天数 |
| `label` | string | **服务端拼好的展示文案**，客户端直接渲染，不要自己拼 |

### 2.8 `OrderPreviewVO` —— 下单预览

```json
{
  "courseId": 501,
  "courseName": "初中数学同步提分",
  "teacherName": "李老师",
  "subject": "数学",
  "unitPrice": 120.00,
  "lessonCount": 10,
  "originAmount": 1200.00,
  "packageDiscount": 180.00,
  "couponDiscount": 50.00,
  "discountAmount": 230.00,
  "payAmount": 970.00,
  "packageId": 601,
  "packageLabel": "10课时套餐 · 85折",
  "couponId": 701,
  "availableCoupons": [ { "参见 CouponVO" } ]
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `originAmount` | number | 原价合计（`unitPrice × lessonCount`） |
| `packageDiscount` | number | 套餐优惠额（无套餐时 `0.00`） |
| `couponDiscount` | number | 券优惠额（未用券时 `0.00`） |
| `discountAmount` | number | 总优惠（= 上两者之和） |
| `payAmount` | number | **实付（≥ 0）** |
| `availableCoupons` | `CouponVO[]` | 当前订单可用（未过期 + 满足门槛 + 未使用）的券 |

> **计价规则（服务端唯一实现，客户端不得复制）**：
> ```
> originAmount    = unitPrice × lessonCount
> packageDiscount = packageId != null ? (originAmount − packagePrice) : 0
> baseForCoupon   = originAmount − packageDiscount        // 套餐折扣先算
> couponDiscount  = (couponId != null && coupon 可用 && baseForCoupon >= coupon.minAmount)
>                   ? min(coupon.amount, baseForCoupon) : 0
> discountAmount  = packageDiscount + couponDiscount
> payAmount       = max(originAmount − discountAmount, 0)
> ```
> 全部用 `BigDecimal`，`setScale(2, RoundingMode.HALF_UP)`。

### 2.9 `OrderVO`

```json
{
  "id": 901,
  "orderNo": "QX202610140930001",
  "parentId": 1001,
  "parentName": "小明妈妈",
  "studentId": 401,
  "studentName": "小明",
  "teacherId": 2001,
  "teacherName": "李老师",
  "teacherAvatar": "http://192.168.1.100:8080/upload/2026/10/t1.jpg",
  "courseId": 501,
  "courseName": "初中数学同步提分",
  "subject": "数学",
  "packageId": 601,
  "packageLabel": "10课时套餐 · 85折",
  "lessonCount": 10,
  "unitPrice": 120.00,
  "totalAmount": 1200.00,
  "discountAmount": 230.00,
  "payAmount": 970.00,
  "payType": "WECHAT",
  "payStatus": "已支付",
  "orderStatus": "已支付",
  "classTime": "2026-10-20 14:00:00",
  "createTime": "2026-10-14 09:30:00",
  "payTime": "2026-10-14 09:31:12",
  "refundReason": null,
  "refundAmount": null,
  "refundTime": null,
  "evaluated": false,
  "canCancel": false,
  "canRefund": true,
  "canEvaluate": false,
  "statusDesc": "已支付，等待上课"
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `orderNo` | string | 业务单号，格式 `QX + yyyyMMddHHmmss + 3位随机` |
| `totalAmount` | number | 原价（= `originAmount`） |
| `evaluated` | boolean | 是否已评价（`canEvaluate == true` 时才可能为 `false`） |
| `canCancel` / `canRefund` / `canEvaluate` | boolean | **⭐ 服务端算好的操作权限**，客户端**只按这三个布尔值显隐按钮**，不要自己判状态 |
| `statusDesc` | string | 服务端拼好的状态文案，客户端直接显示 |

> **设计意图**：把状态机判断全部收在服务端。客户端一旦自己写 `if (orderStatus === '已支付')`，两边规则就会漂移。**这是本项目最重要的接口设计约定。**

### 2.10 `WalletVO`

```json
{
  "totalAmount": 1500.00,
  "balance": 530.00,
  "points": 1000
}
```

对应参考案例 `me_money.xml` 的「我的总额 / 我的余额 / 我的积分」三格。

### 2.11 `WalletRecordVO`

```json
{
  "id": 801,
  "bizType": "退款",
  "amount": 970.00,
  "balanceAfter": 530.00,
  "orderNo": "QX202610140930001",
  "remark": "订单退款",
  "createTime": "2026-10-14 10:00:00"
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `bizType` | string | `充值` / `支付` / `退款` / `奖励` |
| `amount` | number | **带符号**：收入为正，支出为负 |
| `balanceAfter` | number | 变动后余额 |

### 2.12 `CouponVO`

```json
{
  "id": 701,
  "name": "新用户专享券",
  "amount": 50.00,
  "minAmount": 200.00,
  "expireTime": "2026-12-31",
  "status": "未使用",
  "usable": true
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `minAmount` | number | 使用门槛 |
| `status` | string | `未使用` / `已使用` / `已过期` |
| `usable` | boolean | 当前订单下是否可用（仅在 `OrderPreviewVO.availableCoupons` 中有意义） |

### 2.13 `HomeIndexVO` —— 首页聚合

```json
{
  "banners": [
    { "id": 1, "image": "http://192.168.1.100:8080/upload/banner1.jpg", "title": "名师一对一辅导啦", "link": null }
  ],
  "notices": [
    { "id": 11, "text": "秋季班开始报名，限时85折" }
  ],
  "subjects": [
    { "name": "语文", "icon": "http://192.168.1.100:8080/static/subject/yuwen.png" }
  ],
  "hotTeachers": [ { "参见 TeacherCardVO" } ]
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `banners` | array | 5 张轮播图（取自 `sys_config` 或写死在 `HomeController` 常量） |
| `notices` | array | 动态公告滚动文案（对应参考案例 `ViewFlipper`） |
| `subjects` | array | **固定 8 个科目**，见 SPEC §12.9 |
| `hotTeachers` | `TeacherCardVO[]` | 按 `rating` 倒序取前 6 个，**仅资质已通过者** |

### 2.14 `ChatSessionVO`

```json
{
  "sessionId": 1101,
  "peerId": 2001,
  "peerName": "李老师",
  "peerAvatar": "http://192.168.1.100:8080/upload/2026/10/t1.jpg",
  "peerRole": "TEACHER",
  "lastMsg": "好的，明天下午两点见",
  "lastMsgType": "TEXT",
  "lastTime": "2026-10-14 15:30:00",
  "unread": 2
}
```

### 2.15 `ChatMessageVO`

```json
{
  "id": 1201,
  "sessionId": 1101,
  "fromId": 1001,
  "toId": 2001,
  "fromAvatar": "http://192.168.1.100:8080/upload/2026/10/p1.jpg",
  "msgType": "TEXT",
  "content": "老师，明天几点上课？",
  "mediaUrl": null,
  "duration": null,
  "isRead": true,
  "createTime": "2026-10-14 15:28:00",
  "clientMsgId": "c-1728890880000-1"
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `msgType` | string | 见 §3.6 |
| `content` | string | `TEXT` 时为文本内容；其他类型可为 `null` 或简单描述 |
| `mediaUrl` | string | `VOICE`/`IMAGE`/`VIDEO` 时的媒体完整 URL |
| `duration` | int | `VOICE` 时的秒数 |
| `clientMsgId` | string | **客户端生成的消息 ID，用于 WebSocket ACK 对账**（防重复气泡） |

### 2.16 `StudyReportVO`

```json
{
  "id": 1301,
  "studentId": 401,
  "studentName": "小明",
  "teacherId": 2001,
  "teacherName": "李老师",
  "parentId": 1001,
  "period": "2026-10 第 2 周",
  "summary": "本周完成了二次函数图像与性质的学习",
  "performance": "课堂专注度良好，对开口方向判断准确，顶点式配方仍需加强",
  "score": 88.5,
  "attitude": "积极",
  "homeworkRate": 92.00,
  "suggestion": "建议每天完成 3 道配方法练习题",
  "isRead": false,
  "createTime": "2026-10-12 20:00:00"
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `score` | number | 综合评分 0–100，1 位小数 |
| `attitude` | string | `积极` / `一般` / `需督促` |
| `homeworkRate` | number | 作业完成率 %（客户端渲染为进度条） |

### 2.17 `NoticeVO`

```json
{
  "id": 1401,
  "noticeType": "CLASS_REMIND",
  "title": "上课提醒",
  "content": "您预约的「初中数学同步提分」将于 30 分钟后开始",
  "bizId": 901,
  "remindTime": "2026-10-20 13:30:00",
  "status": "未读",
  "createTime": "2026-10-14 09:30:00"
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `bizId` | long | 关联业务 ID（如 `order.id`），点击可跳详情 |
| `status` | string | `未读` / `已读` |

### 2.18 `OverviewVO` —— 核心数据看板

```json
{
  "newUser": 128,
  "newUserGrowth": 12.50,
  "orderCount": 342,
  "orderGrowth": -3.20,
  "tradeAmount": 41560.00,
  "tradeGrowth": 8.70,
  "repurchaseRate": 34.80,
  "userTrend": [
    { "date": "2026-09-15", "value": 3.00 },
    { "date": "2026-09-16", "value": 5.00 }
  ],
  "orderTrend": [ { "date": "2026-09-15", "value": 8.00 } ],
  "tradeTrend": [ { "date": "2026-09-15", "value": 960.00 } ],
  "aiSuggestion": "近 30 天交易额环比增长 8.7%，数学与英语贡献 62% 的订单；建议在八年级数学方向增加 2 名教师，并将套餐折扣从 85 折微调至 88 折以提升客单价。"
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `newUser` / `orderCount` / `tradeAmount` / `repurchaseRate` | number | 近 30 天汇总值 |
| `*Growth` | number | 环比增长率 %，**可为负** |
| `*Trend` | `{date, value}[]` | 近 30 天日序列，**必须有 30 个点**，供折线/柱状图 |
| `aiSuggestion` | string | 「AI 经营建议」文案。**未接大模型时用本地模板拼接兜底，永不为 null** |

> `value` 统一为 number，图表组件无需再做类型判断。

### 2.19 `TutorAnalysisVO` —— 家教表现分析

```json
{
  "avgGoodRate": 94.20,
  "avgAcceptRate": 88.60,
  "avgRefundRate": 2.10,
  "ranking": [
    {
      "teacherId": 2001,
      "teacherName": "李老师",
      "avatar": "http://192.168.1.100:8080/upload/2026/10/t1.jpg",
      "goodRate": 96.50,
      "acceptRate": 92.30,
      "refundRate": 1.20,
      "orderCount": 132,
      "warning": false
    }
  ]
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `warning` | boolean | **阈值预警**：`refundRate > 20` 或 `goodRate < 60` 时为 `true`，客户端标红 |

**计算口径（写进报告「功能设计」章）**：

```
好评率 goodRate   = AVG(evaluation.score) / 5 × 100        （无评价时取 0，不进排行榜）
接单率 acceptRate = 该教师 已完成/已支付 订单数 ÷ 该教师收到的预约总数 × 100
退课率 refundRate = 该教师 已退款订单数 ÷ 该教师总订单数 × 100
```

### 2.20 `DemandAnalysisVO` —— 家长需求分析

```json
{
  "subjectDistribution": [
    { "name": "数学", "value": 128 },
    { "name": "英语", "value": 96 }
  ],
  "gradeDistribution": [
    { "name": "八年级", "value": 64 },
    { "name": "九年级", "value": 52 }
  ],
  "priceSensitivity": [
    { "priceRange": "0-80", "orderCount": 22, "avgRating": 4.50 },
    { "priceRange": "80-120", "orderCount": 96, "avgRating": 4.72 },
    { "priceRange": "120-160", "orderCount": 118, "avgRating": 4.81 },
    { "priceRange": "160+", "orderCount": 41, "avgRating": 4.66 }
  ],
  "topSubject": "数学",
  "priceInsight": "120-160 元区间订单量最高且评分最高，说明家长接受该价位且该价位教师质量更优。"
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `subjectDistribution` | `{name, value}[]` | **按订单数**聚合（不是按教师数），饼图数据 |
| `gradeDistribution` | `{name, value}[]` | 柱状图数据 |
| `priceSensitivity` | array | 价格带 × 下单量 + 均价评分，双轴图数据 |
| `priceInsight` | string | 一句话结论，**AI 建议兜底文案** |

### 2.21 `EvaluationVO`

```json
{
  "id": 1501,
  "orderId": 901,
  "orderNo": "QX202610140930001",
  "parentId": 1001,
  "parentName": "小明妈妈",
  "parentAvatar": "http://192.168.1.100:8080/upload/2026/10/p1.jpg",
  "teacherId": 2001,
  "score": 5,
  "tags": ["讲解清晰", "耐心负责", "提分明显"],
  "content": "孩子说老师讲得很清楚，函数那块一下就懂了。",
  "createTime": "2026-10-21 20:00:00"
}
```

### 2.22 `FeedbackVO`

```json
{
  "id": 1601,
  "userId": 1001,
  "userName": "小明妈妈",
  "subject": "数学",
  "title": "希望增加晚间接单的教师",
  "content": "白天上班没时间，希望有晚上 7 点以后能上课的老师。",
  "reply": null,
  "replyTime": null,
  "createTime": "2026-10-13 21:10:00"
}
```

### 2.23 `AdminDashboardVO`

```json
{
  "pendingCertCount": 1,
  "totalUserCount": 9,
  "todayOrderCount": 3,
  "todayTradeAmount": 2350.00
}
```

### 2.24 `UploadVO`

```json
{
  "url": "http://192.168.1.100:8080/upload/2026/10/a1b2c3.jpg",
  "path": "/upload/2026/10/a1b2c3.jpg",
  "name": "a1b2c3.jpg",
  "size": 284621
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `url` | string | 完整可访问 URL，**客户端存库/展示一律用这个** |
| `path` | string | 相对路径（冗余存储用） |
| `size` | long | 字节数 |

### 2.25 `StudentVO`

```json
{
  "id": 401,
  "parentId": 1001,
  "name": "小明",
  "gender": 1,
  "grade": "八年级",
  "school": "镇江市第一中学",
  "avatar": null
}
```

---

## 3. 枚举字典

> **双端必须使用同一套字面量**。服务端用 Java 常量类 `constants/RoleConstant.java` 等，客户端用 `constants/enums.ets`。

### 3.1 角色 `role`

| 值 | 中文 | 客户端跳转首屏 |
|---|---|---|
| `PARENT` | 家长 | 主页 Tab |
| `STUDENT` | 学生 | 主页 Tab |
| `TEACHER` | 教师 | 教师工作台 |
| `ADMIN` | 管理员 | 管理台 |

### 3.2 用户状态 `UserVO.status`

| 值 | 含义 |
|---|---|
| `0` | 正常 |
| `1` | 禁用（登录返回 401） |
| `2` | 待审核（仅教师，可登录但 `👤T` 接口返回 1003） |

### 3.3 资质审核状态 `CertVO.status`

| 值 | 含义 |
|---|---|
| `待审核` | 刚提交 |
| `已通过` | 可通过审核后 `sys_user.status` 置 `0` |
| `已驳回` | `rejectReason` 必有值，教师可重新提交 |

### 3.4 订单状态 `orderStatus`

| 值 | 允许的操作 |
|---|---|
| `待支付` | 支付 · 取消 |
| `已支付` | 退款申请 · 评价（未开课时） |
| `上课中` | —— |
| `已完成` | 评价 |
| `已取消` | —— |
| `退款中` | —— |
| `已退款` | —— |

**合法流转（服务端状态机，非法流转一律返回 2001）**：

```
待支付 ──支付──> 已支付 ──上课──> 上课中 ──结课──> 已完成 ──评价──> 已完成
   │                │
   └──取消──> 已取消  └──申请退款──> 退款中 ──审核通过──> 已退款
```

### 3.5 支付状态 / 支付方式

| `payStatus` | 含义 |
|---|---|
| `未支付` | 初始 |
| `已支付` | 支付回调成功 |
| `已退款` | 退款完成 |

| `payType` | 含义 |
|---|---|
| `WECHAT` | 微信 |
| `ALIPAY` | 支付宝 |
| `BALANCE` | 余额 |

### 3.6 消息类型 `msgType`

| 值 | 说明 |
|---|---|
| `TEXT` | 文本，用 `content` |
| `VOICE` | 语音，用 `mediaUrl` + `duration` |
| `IMAGE` | 图片，用 `mediaUrl` |
| `VIDEO` | 视频，用 `mediaUrl` |

### 3.7 提醒类型 `noticeType`

| 值 | 中文 |
|---|---|
| `CLASS_REMIND` | 上课提醒 |
| `HOMEWORK_REMIND` | 作业提醒 |
| `SYSTEM` | 系统通知 |

### 3.8 科目 / 年级

见 SPEC §12.9 与 §12.10。**客户端从 `/api/home/index` 的 `subjects` 取**，不要在客户端硬编码图标 URL。

---

## 4. 接口详情

> 编号规则 `API-<模块>-<序号>`。每个接口标注：鉴权 · 角色 · 归属 Part。

---

### 4.1 探针

#### `API-SYS-01` 🔓 `GET /api/ping` · Part 2

**作用**：连通性探针。客户端 Part 3 用它验证网络层打通。

**响应**
```json
{ "code": 200, "msg": "success", "data": "pong" }
```

---

### 4.2 认证 `/api/auth`

#### `API-AUTH-01` 🔓 `POST /api/auth/sms` · Part 4

**作用**：发送验证码（模拟）。**不接真实短信服务**：验证码固定为 `123456`，同时写入服务端内存 Map（key = phone，TTL 5 分钟）。

**请求体**
| 字段 | 类型 | 必填 | 校验 |
|---|---|---|---|
| `phone` | string | ✅ | `^1[3-9]\d{9}$` |
| `scene` | string | ✅ | `REGISTER` / `LOGIN` |

**响应** `data`
```json
{ "sent": true, "mockCode": "123456", "expireSeconds": 300 }
```

> `mockCode` 仅开发期返回，**报告/答辩中说明这是模拟实现**。

**错误码**：`400`（手机号格式）

---

#### `API-AUTH-02` 🔓 `POST /api/auth/register` · Part 4

**作用**：注册。**按角色分三条写入链路**。

**请求体**
| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `phone` | string | ✅ | 手机号 |
| `password` | string | ✅ | 6–20 位 |
| `smsCode` | string | ✅ | 固定 `123456` |
| `role` | string | ✅ | `PARENT` / `STUDENT` / `TEACHER`（**不接受 `ADMIN`**） |
| `nickname` | string | ✅ | 2–20 字 |
| `realName` | string | 条件 | `PARENT`/`TEACHER` 必填 |
| `idCard` | string | 条件 | `PARENT`/`TEACHER` 必填，18 位含校验位 |
| `certType` | string | 条件 | `TEACHER` 必填：`教师资格证`/`学历证书`/`身份证` |
| `certNo` | string | 条件 | `TEACHER` 必填 |
| `certImg` | string | 条件 | `TEACHER` 必填，`/api/file/upload` 返回的 `url` |
| `subjects` | string[] | 条件 | `TEACHER` 必填，至少 1 个，取值见 SPEC §12.9 |
| `grades` | string[] | 条件 | `TEACHER` 必填，至少 1 个 |
| `teachYears` | int | ❌ | `TEACHER` 默认 0 |
| `education` | string | ❌ | `TEACHER` 学历 |
| `school` | string | ❌ | `TEACHER` 院校 |
| `pricePerHour` | number | ❌ | `TEACHER` 时薪，默认 100.00 |

**服务端写入链路**

| 角色 | `sys_user` | `teacher_cert` | `teacher_profile` | `wallet` |
|---|---|---|---|---|
| `PARENT` | 写（`status=0`，含实名） | —— | —— | 初始化（送 50 元券 + 500 积分） |
| `STUDENT` | 写（`status=0`） | —— | —— | —— |
| `TEACHER` | 写（**`status=2` 待审核**） | 写（`待审核`） | 写 | —— |

**响应** `data`：即 `LoginVO`（注册后自动登录）。

**错误码**：`1001`（手机号已注册） · `1004`（验证码错误） · `1005`（身份证已绑定） · `400`（参数校验）

---

#### `API-AUTH-03` 🔓 `POST /api/auth/login` · Part 4

**请求体**：`{ "phone", "password" }`

**响应** `data`：`LoginVO`

**错误码**：`1002`（账号或密码错误） · `401`（账号被禁用）

---

#### `API-AUTH-04` 🔓 `POST /api/auth/login/sms` · Part 4

**请求体**：`{ "phone", "smsCode" }`

**响应** `data`：`LoginVO`。**未注册手机号返回 `1002`**，不自动注册。

---

#### `API-AUTH-05` 🔓 `POST /api/auth/login/wx` · Part 4

**作用**：微信登录（**mock 实现**）。客户端传一个模拟 `code`，服务端据此生成稳定的 `wx_openid`。

**请求体**
| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `code` | string | ✅ | 客户端固定传 `mock_wx_code_<时间戳后4位>` 或任意字符串 |
| `nickname` | string | ❌ | 首次自动建号时的昵称，默认「微信用户」 |
| `avatar` | string | ❌ | 头像 URL |

**逻辑**：按 `wx_openid` 查 `sys_user`；不存在则以 `role=PARENT` 自动建号并绑定。

**响应** `data`：`LoginVO`

> 报告中须写明：「微信/QQ 登录采用 mock 实现，通过模拟 openid 走通账号绑定与自动建号逻辑，真机接入需申请开放平台资质并集成华为 Account Kit。」

---

#### `API-AUTH-06` 🔓 `POST /api/auth/login/qq` · Part 4

同 `API-AUTH-05`，字段为 `openid`，落到 `sys_user.qq_openid`。

---

#### `API-AUTH-07` 🔐 `GET /api/auth/me` · Part 4

**作用**：客户端冷启动时校验 Token 并取最新用户信息。`phone` **返回原值**。

**响应** `data`：`UserVO`

**错误码**：`401`

---

#### `API-AUTH-08` 🔐 `POST /api/auth/logout` · Part 4

**作用**：登出。服务端无需维护黑名单（单机课设场景），仅返回成功；**客户端负责清 `Preferences` 与 `AppStorage`**。

**响应** `data`：`null`

---

### 4.3 用户 `/api/user`

#### `API-USER-01` 🔐 `GET /api/user/profile` · Part 4

**响应** `data`：`UserVO`（`phone` 原值）；若为 `TEACHER`，额外附加 `teacherProfile`（`TeacherCardVO`）。

---

#### `API-USER-02` 🔐 `PUT /api/user/profile` · Part 4

**请求体**（全部可选，**只更新非 null 字段**）

| 字段 | 类型 | 校验 |
|---|---|---|
| `nickname` | string | 2–20 字 |
| `avatar` | string | 上传接口返回的 url |
| `gender` | int | 0/1/2 |
| `address` | string | ≤ 100 字 |

**响应** `data`：`UserVO`

---

#### `API-USER-03` 🔐 `POST /api/user/realname` · Part 4

**作用**：提交/修改实名。家长下单前必须已实名。

**请求体**：`{ "realName": "王芳", "idCard": "3201021990XXXX1234" }`

**校验**：18 位 + 校验位算法；同一身份证不可被两个账号绑定。

**响应** `data`：`UserVO`

**错误码**：`1005`

---

#### `API-USER-04` 🔐 `GET /api/user/students` · Part 4

**作用**：家长查看自己的孩子（下单时要选学生）。**仅返回自己名下的**。

**响应** `data`：`StudentVO[]`

---

#### `API-USER-05` 👤P `POST /api/user/students` · Part 4

**请求体**：`{ "name", "gender", "grade", "school" }` → **响应** `data`：`StudentVO`

---

### 4.4 文件 `/api/file`

#### `API-FILE-01` 🔓 `POST /api/file/upload` · Part 2

**作用**：通用上传。头像、资质证书、语音消息共用。

**请求**：`multipart/form-data`，字段名 `file`

| 限制 | 值 |
|---|---|
| 允许后缀 | `jpg` `jpeg` `png` `webp` `mp3` `m4a` `wav` |
| 单文件上限 | 10 MB |
| 落盘路径 | `{qxjiajiao.upload.dir}/yyyy/MM/{uuid}.{ext}` |

**响应** `data`：`UploadVO`

**错误码**：`4001`（类型不支持） · `4002`（超出大小）

> 该接口设为 🔓 是为了让客户端在登录前即可上传注册用的资质图。生产环境应改为 🔐，报告中可说明这一取舍。

---

### 4.5 教师资质与资料 `/api/teacher`

#### `API-CERT-01` 👤T `POST /api/teacher/cert` · Part 4

**作用**：提交资质。**已通过后不可再提交**（返回 403）；`已驳回` 可重新提交（覆盖原记录，状态回 `待审核`）。

**请求体**
| 字段 | 类型 | 必填 |
|---|---|---|
| `certType` | string | ✅ |
| `certNo` | string | ✅ |
| `certImg` | string | ✅ |

**响应** `data`：`CertVO`

---

#### `API-CERT-02` 👤T `GET /api/teacher/cert/my` · Part 4

**响应** `data`：`CertVO`（无记录时 `data` 为 `null`）

---

#### `API-CERT-03` 👤T `GET /api/teacher/profile` · Part 4

**响应** `data`：`TeacherCardVO` + `intro`（教师自己的资料，含未上架课程）

---

#### `API-CERT-04` 👤T `PUT /api/teacher/profile` · Part 4

**请求体**（全部可选）

| 字段 | 类型 | 校验 |
|---|---|---|
| `subjects` | string[] | 至少 1 个 |
| `grades` | string[] | 至少 1 个 |
| `teachYears` | int | 0–60 |
| `education` | string | —— |
| `school` | string | —— |
| `intro` | string | ≤ 500 字 |
| `pricePerHour` | number | 0–9999，2 位小数 |

**响应** `data`：`TeacherCardVO`

---

### 4.6 管理员 `/api/admin`

#### `API-ADMIN-01` 👤A `GET /api/admin/cert/list` · Part 5

**查询参数**：`status`（`待审核`/`已通过`/`已驳回`，可空＝全部） · `keyword`（教师昵称/手机号模糊） · `page` · `size`

**响应** `data`：分页 `CertVO[]`

---

#### `API-ADMIN-02` 👤A `POST /api/admin/cert/audit` · Part 5

**请求体**
| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `certId` | long | ✅ | 资质记录 ID |
| `pass` | boolean | ✅ | `true` 通过 / `false` 驳回 |
| `rejectReason` | string | 条件 | `pass=false` 时必填，≤ 200 字 |

**通过时的事务动作**：`teacher_cert.status = 已通过`、`audit_by/audit_time` 落值、`sys_user.status = 0`、写 `admin_log`。

**响应** `data`：`null`

**错误码**：`404`（资质不存在） · `400`（驳回未填原因） · `2001`（该资质已审核）

---

#### `API-ADMIN-03` 👤A `GET /api/admin/users` · Part 5

**查询参数**：`role` · `status` · `keyword` · `page` · `size`

**响应** `data`：分页 `UserVO[]`（`phone` 脱敏）

---

#### `API-ADMIN-04` 👤A `PUT /api/admin/user/{id}/status` · Part 5

**路径参数**：`id`　**请求体**：`{ "status": 1 }`（0 正常 / 1 禁用）

**响应** `data`：`null`。**越权保护**：不可禁用自己、不可禁用其他 `ADMIN`。

---

#### `API-ADMIN-05` 👤A `GET /api/admin/dashboard` · Part 5

**响应** `data`：`AdminDashboardVO`

---

#### `API-ADMIN-06` 👤A `POST /api/admin/feedback/reply` · Part 14

**请求体**：`{ "feedbackId": 1601, "reply": "已安排晚班教师" }` → **响应** `data`：`null`

---

### 4.7 首页 `/api/home`

#### `API-HOME-01` 🔐 `GET /api/home/index` · Part 6

**作用**：首页一次性聚合，**避免客户端连发 4 个请求**。

**响应** `data`：`HomeIndexVO`

> 轮播图与公告数据源：写死在服务端常量 `HomeController.BANNERS` / `NOTICES`，图片放 `server/src/main/resources/static/banner/`。**不建表**——报告里说明「首页运营位为静态配置，未做后台管理，属范围外」。

---

### 4.8 教师检索 `/api/tutor`

#### `API-TUTOR-01` 🔐 `GET /api/tutor/list` · Part 6

**查询参数**

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `subject` | string | ❌ | 中文科目名，如 `数学`；空＝全部 |
| `grade` | string | ❌ | 中文年级名；空＝全部 |
| `keyword` | string | ❌ | 昵称/院校/简介模糊 |
| `sortBy` | string | ❌ | `rating`（默认）/ `price_asc` / `price_desc` / `order_desc` |
| `page` / `size` | int | ❌ | 分页 |

**关键约束**：**只返回 `teacher_cert.status = 已通过` 的教师**（在 SQL 里 JOIN 过滤，不是内存过滤）。

**响应** `data`：分页 `TeacherCardVO[]`

**空结果**：`records: []`，客户端渲染空态文案「抱歉，没有满足你要查找的教师！」（沿用参考案例）。

---

#### `API-TUTOR-02` 🔐 `GET /api/tutor/{id}` · Part 7

**路径参数**：`id` = 教师 `userId`

**响应** `data`：`TeacherVO`

**错误码**：`404`

---

#### `API-TUTOR-03` 👤P `POST /api/tutor/reserve` · Part 7

**作用**：预约教师 → **生成一条 `待支付` 订单**。

**请求体**
| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `teacherId` | long | ✅ | 教师 userId |
| `courseId` | long | ✅ | 课程 ID |
| `packageId` | long | ❌ | 套餐 ID，空＝单次 |
| `studentId` | long | ❌ | 学生 ID，空则取家长第一个学生 |
| `classTime` | string | ✅ | `yyyy-MM-dd HH:mm:ss`，须晚于当前时间 |
| `remark` | string | ❌ | 备注 |

**响应** `data`
```json
{ "orderNo": "QX202610140930001", "orderId": 901, "payAmount": 970.00 }
```

**副作用**：同时创建一条 `CLASS_REMIND` 的 `notice`（`remindTime = classTime − 30min`）。

**错误码**：`1003`（教师资质未通过） · `400`（上课时间早于当前） · `403`（未实名）

---

#### `API-TUTOR-04` 🔐 `GET /api/tutor/favorite` · Part 7

**响应** `data`：分页 `TeacherCardVO[]`（`favorited` 恒为 `true`）

---

#### `API-TUTOR-05` 👤P `POST /api/tutor/favorite` · Part 7

**作用**：**幂等切换**。

**请求体**：`{ "teacherId": 2001, "favorite": true }`

**响应** `data`：`{ "favorited": true }`

---

### 4.9 课程 `/api/course`

#### `API-COURSE-01` 🔐 `GET /api/course/list` · Part 8

**查询参数**：`teacherId` · `subject` · `page` · `size`

**响应** `data`：分页 `CourseVO[]`（`packages` 为 `null`）

---

#### `API-COURSE-02` 🔐 `GET /api/course/{id}` · Part 8

**响应** `data`：`CourseVO`（**含 `packages`**）

---

#### `API-COURSE-03` 🔐 `GET /api/course/{id}/packages` · Part 8

**响应** `data`：`PackageVO[]`

---

### 4.10 订单 `/api/order`

#### `API-ORDER-01` 👤P `POST /api/order/preview` · Part 8

**作用**：下单前算价。**服务端唯一计价入口**。

**请求体**：`{ "courseId", "packageId"(可空), "couponId"(可空) }`

**响应** `data`：`OrderPreviewVO`

**错误码**：`2003`（券不可用）

---

#### `API-ORDER-02` 👤P `POST /api/order/create` · Part 8

**作用**：创建订单。**服务端用与 preview 完全相同的算法重算金额**——请求体里的金额字段一律忽略（前端不传）。

**请求体**：同 `API-ORDER-01` + `{ "studentId", "classTime", "remark" }`

**响应** `data`：同 `API-TUTOR-03`

**副作用**：创建 `CLASS_REMIND` 提醒。

---

#### `API-ORDER-03` 🔐 `GET /api/order/list` · Part 10

**作用**：**按角色自动过滤**。

| 角色 | 过滤条件 |
|---|---|
| `PARENT` | `parent_id = 当前用户` |
| `STUDENT` | `student_id ∈ 自己` |
| `TEACHER` | `teacher_id = 当前用户` |
| `ADMIN` | 无过滤 |

**查询参数**：`orderStatus`（可空）· `keyword`（订单号/课程名）· `page` · `size`

**响应** `data`：分页 `OrderVO[]`

---

#### `API-ORDER-04` 🔐 `GET /api/order/{orderNo}` · Part 10

**响应** `data`：`OrderVO`。**越权保护**：非本人订单返回 `403`。

---

#### `API-ORDER-05` 👤P `POST /api/order/{orderNo}/cancel` · Part 10

**前置**：`orderStatus == 待支付`，否则 `2001`。

**响应** `data`：`null`

---

#### `API-ORDER-06` 👤P `POST /api/order/{orderNo}/refund` · Part 10

**前置**：`orderStatus == 已支付` **且** `classTime > now()`，否则 `2001` / `2005`。

**请求体**：`{ "refundReason": "时间冲突" }`

**副作用**：`orderStatus → 退款中`；**模拟审核即时通过** → `已退款`、`payStatus = 已退款`、回退钱包余额、写 `wallet_record`、发 `notice`。

**响应** `data`：`null`

---

#### `API-ORDER-07` 🔓 `GET /api/order/refund/rule` · Part 10

**作用**：退款规则说明页数据源。**无需登录**（规则页在未登录时也能看）。

**响应** `data`
```json
{
  "title": "退款规则",
  "items": [
    "仅未上课的订单支持退款",
    "订单需处于「已支付」状态",
    "退款将原路退回，预计 7 个工作日到账",
    "已上课或已完成的订单不支持退款"
  ],
  "arrivalDays": 7
}
```

---

#### `API-ORDER-08` 🔐 `GET /api/order/export` · Part 10 ⚠️ **非统一响应体**

**作用**：导出 CSV。

**查询参数**：同 `API-ORDER-03`（**不带分页**，导出当前筛选的全部）

**响应**：`Content-Type: text/csv;charset=UTF-8`，文件流。

| 项 | 要求 |
|---|---|
| 编码 | **UTF-8 带 BOM**（`﻿` 开头），否则 Excel 打开中文乱码 |
| 文件名 | `Content-Disposition: attachment; filename="orders_20261014.csv"`（**用 ASCII 文件名**，中文文件名在部分浏览器会截断） |
| 列 | `订单号,课程名称,课时数,费用,支付状态,上课时间` |
| 金额格式 | 保留 2 位小数，不加货币符号 |

**客户端处理**：用 `http` 请求拿到 `ArrayBuffer`，写入应用沙箱文件，再调用系统分享/打开（`utils/download.ets`）。

---

### 4.11 支付 `/api/pay`

#### `API-PAY-01` 👤P `POST /api/pay/prepay` · Part 9

**作用**：发起支付，返回模拟支付参数。

**请求体**：`{ "orderNo": "QX2026...", "payType": "WECHAT" }`

**响应** `data`
```json
{
  "orderNo": "QX202610140930001",
  "payType": "WECHAT",
  "payAmount": 970.00,
  "mockPayId": "MOCKPAY1728890880000",
  "expireSeconds": 300
}
```

**错误码**：`2004`（订单已支付） · `2002`（余额支付时余额不足） · `2001`（订单状态不允许支付）

> **客户端模拟支付面板**：展示 `payAmount` + 2 秒 loading → 直接调 `API-PAY-02`。

---

#### `API-PAY-02` 👤P `POST /api/pay/callback` · Part 9

**作用**：模拟支付成功回调。**必须幂等**。

**请求体**：`{ "orderNo": "QX2026...", "payType": "WECHAT", "mockPayId": "MOCKPAY..." }`

**单事务内完成**：
1. 校验订单为 `待支付`，否则返回 `2004`（**幂等出口，不产生任何副作用**）
2. `pay_status = 已支付`、`order_status = 已支付`、`pay_time = now()`
3. 核销 `coupon`（如有）→ `status = 已使用`
4. 若 `payType = BALANCE`：扣 `wallet.balance`，余额不足回滚并返回 `2002`
5. 写 `wallet_record`（支付）
6. 赠送积分（`payAmount / 10`，向下取整）
7. 写 `notice`（`SYSTEM`：支付成功）

**响应** `data`：`OrderVO`（更新后的订单，客户端直接用它刷新详情页）

**错误码**：`2004`（重复支付） · `2002`（余额不足）

> **验收要点**：连续调两次，第二次必须返回 `2004` 且**钱包未二次扣款、流水未新增**。

---

### 4.12 钱包与券

#### `API-WALLET-01` 🔐 `GET /api/wallet` · Part 9

**响应** `data`：`WalletVO`

---

#### `API-WALLET-02` 🔐 `GET /api/wallet/records` · Part 9

**查询参数**：`bizType`（可空） · `page` · `size`

**响应** `data`：分页 `WalletRecordVO[]`

---

#### `API-COUPON-01` 🔐 `GET /api/coupon/list` · Part 9

**查询参数**：`status`（`未使用`/`已使用`/`已过期`，可空＝全部）

**响应** `data`：分页 `CouponVO[]`

> **过期券的惰性刷新**：查询时若 `expire_time < today` 且 `status = 未使用`，服务端顺手更新为 `已过期`。

---

### 4.13 聊天 `/api/chat`

#### `API-CHAT-01` 🔐 `GET /api/chat/sessions` · Part 11

**响应** `data`：`ChatSessionVO[]`（**不设分页**，课设场景会话量小；按 `lastTime` 倒序）

---

#### `API-CHAT-02` 🔐 `GET /api/chat/messages` · Part 11

**查询参数**

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `peerId` | long | ✅ | 对端用户 ID。**服务端按「小 ID 在前」归一化后查找或创建会话** |
| `page` | int | ❌ | 默认 1（**第 1 页是最新的 20 条**） |
| `size` | int | ❌ | 默认 20 |

**响应** `data`：分页 `ChatMessageVO[]`（**页内按 `createTime` 升序**，方便直接渲染）

---

#### `API-CHAT-03` 🔐 `POST /api/chat/send` · Part 11

**作用**：HTTP 兜底发消息（WebSocket 不可用时使用）。

**请求体**
| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `toId` | long | ✅ | 接收方 userId |
| `msgType` | string | ✅ | §3.6 |
| `content` | string | 条件 | `TEXT` 必填，≤ 500 字 |
| `mediaUrl` | string | 条件 | 语音/图片/视频必填 |
| `duration` | int | ❌ | 语音秒数 |
| `clientMsgId` | string | ✅ | 客户端生成，用于去重 |

**响应** `data`：`ChatMessageVO`（含服务端生成的 `id` 与 `createTime`）

---

#### `API-CHAT-04` 🔐 `POST /api/chat/read` · Part 11

**请求体**：`{ "peerId": 2001 }`（**不用传 sessionId**，服务端自行归一化查找）

**响应** `data`：`{ "cleared": 2 }`（清除的未读数）

---

### 4.14 学习报告与提醒

#### `API-REPORT-01` 🔐 `GET /api/report/list` · Part 12

**角色过滤**：`PARENT` → `parent_id = 我`；`STUDENT` → `student_id ∈ 我`；`TEACHER` → `teacher_id = 我`；`ADMIN` → 全部。

**查询参数**：`studentId`（可空） · `page` · `size`

**响应** `data`：分页 `StudyReportVO[]`

---

#### `API-REPORT-02` 🔐 `GET /api/report/{id}` · Part 12

**作用**：查详情。**副作用：顺手把 `is_read` 置 1**（家长打开即已读）。

**响应** `data`：`StudyReportVO`。**越权保护**：非关联人返回 `403`。

---

#### `API-REPORT-03` 👤T `POST /api/report` · Part 12

**请求体**
| 字段 | 类型 | 必填 | 校验 |
|---|---|---|---|
| `studentId` | long | ✅ | 必须是该教师有过订单的学生 |
| `period` | string | ✅ | ≤ 30 字 |
| `summary` | string | ✅ | ≤ 500 字 |
| `performance` | string | ✅ | ≤ 500 字 |
| `score` | number | ✅ | 0–100，1 位小数 |
| `attitude` | string | ✅ | `积极`/`一般`/`需督促` |
| `homeworkRate` | number | ✅ | 0–100，2 位小数 |
| `suggestion` | string | ❌ | ≤ 500 字 |

**响应** `data`：`StudyReportVO`

**副作用**：写 `notice`（`SYSTEM`：您有一份新的学习报告）给该学生的家长。

**错误码**：`403`（该学生不属于该教师） · `1003`（资质未通过）

---

#### `API-NOTICE-01` 🔐 `GET /api/notice/list` · Part 12

**查询参数**：`noticeType`（可空） · `status`（可空） · `page` · `size`

**响应** `data`：分页 `NoticeVO[]`

---

#### `API-NOTICE-02` 🔐 `GET /api/notice/unread-count` · Part 12

**作用**：底栏角标数据源。**客户端在进入前台、收到 WebSocket 推送、下拉刷新时各调一次。**

**响应** `data`：`{ "count": 3 }`

---

#### `API-NOTICE-03` 🔐 `POST /api/notice/read` · Part 12

**请求体**：`{ "ids": [1401, 1402] }`（空数组＝全部已读）

**响应** `data`：`{ "updated": 2 }`

---

### 4.15 分析 `/api/analysis`

> **本组为模块④的全部落点。** 三个接口的数据来源：`daily_stat` 预聚合表 + 实时聚合 SQL（写在 `mapper/AnalysisMapper.xml`，**这些 SQL 截图可用于报告「数据库设计」章**）。

#### `API-ANA-01` 👤A `GET /api/analysis/overview` · Part 13

**查询参数**：`days`（默认 30，可选 7/30/90）

**响应** `data`：`OverviewVO`

**错误码**：`403`（非管理员）

---

#### `API-ANA-02` 🔐 `GET /api/analysis/tutor` · Part 13

**角色差异**：
- `ADMIN` → `ranking` 返回 Top 10 全量排行
- `TEACHER` → `ranking` **只含自己一条**，其余汇总值仍返回全局均值

**响应** `data`：`TutorAnalysisVO`

---

#### `API-ANA-03` 👤A `GET /api/analysis/demand` · Part 13

**响应** `data`：`DemandAnalysisVO`

---

### 4.16 评价与建议

#### `API-EVAL-01` 👤P `POST /api/evaluation` · Part 14

**请求体**
| 字段 | 类型 | 必填 | 校验 |
|---|---|---|---|
| `orderNo` | string | ✅ | 必须是本人的、`已完成` 或 `已支付` 的订单 |
| `score` | int | ✅ | 1–5 |
| `tags` | string[] | ❌ | 最多 3 个，白名单：`讲解清晰` `耐心负责` `提分明显` `准时守约` `因材施教` |
| `content` | string | ❌ | ≤ 300 字 |

**响应** `data`：`EvaluationVO`

**副作用**：**回算该教师的 `good_rate` 与 `rating`** —— 这是 Part 13 看板数据能现场变化的依据，**答辩演示的关键点**。

**错误码**：`2001`（订单状态不允许评价） · `403`（非本人订单）

---

#### `API-EVAL-02` 🔐 `GET /api/evaluation/list` · Part 14

**查询参数**：`teacherId`（可空） · `page` · `size`

**响应** `data`：分页 `EvaluationVO[]`

---

#### `API-FEEDBACK-01` 🔐 `POST /api/feedback` · Part 14

**请求体**：`{ "subject": "数学", "title": "…", "content": "…" }`（`subject` 取 SPEC §12.9，`title` ≤ 50 字，`content` ≤ 500 字）

**响应** `data`：`FeedbackVO`

---

#### `API-FEEDBACK-02` 🔐 `GET /api/feedback/list` · Part 14

**角色差异**：普通用户 → 只看自己的；`ADMIN` → 看全部并可按 `replyStatus`（`已回复`/`未回复`）筛选。

**响应** `data`：分页 `FeedbackVO[]`

---

## 5. WebSocket 协议

### 5.1 连接

```
ws://<IP>:8080/ws/chat?token=<JWT>
```

| 项 | 说明 |
|---|---|
| 鉴权 | **必须在 query 里带 token**（WebSocket 握手无法自定义 Header）。握手失败返回 401，客户端应回登录页 |
| 心跳 | 客户端每 **30s** 发 `PING`，服务端回 `PONG`。**连续 2 次无 PONG 则重连** |
| 重连 | 指数退避：2s → 4s → 8s → 16s，上限 30s；重连成功后调 `API-CHAT-01` 与 `API-NOTICE-02` 拉增量 |
| 生命周期 | `ChatDetail` 页 `aboutToAppear` 连接，`aboutToDisappear` 断开 |

### 5.2 帧格式

**所有帧统一包装**：

```json
{ "type": "<帧类型>", "data": { } }
```

#### 客户端 → 服务端

| `type` | `data` | 说明 |
|---|---|---|
| `PING` | `null` | 心跳 |
| `MSG` | 见下表 | 发消息 |
| `READ` | `{ "peerId": 2001 }` | 标记该会话已读 |

`MSG` 的 `data` 与 `API-CHAT-03` 请求体**完全一致**：
```json
{
  "type": "MSG",
  "data": {
    "toId": 2001,
    "msgType": "TEXT",
    "content": "老师，明天几点上课？",
    "mediaUrl": null,
    "duration": null,
    "clientMsgId": "c-1728890880000-1"
  }
}
```

#### 服务端 → 客户端

| `type` | `data` | 说明 |
|---|---|---|
| `PONG` | `null` | 心跳响应 |
| `ACK` | `{ "clientMsgId": "c-...", "messageId": 1201, "createTime": "..." }` | **发送成功回执**。客户端按 `clientMsgId` 把「发送中」气泡替换为真实消息 |
| `MSG` | `ChatMessageVO` | 收到新消息。**若当前不在该会话页，则只更新未读角标** |
| `NOTICE` | `{ "unreadNotice": 3 }` | 有新提醒/报告，客户端刷新角标 |
| `ERROR` | `{ "code": 1003, "msg": "您的资质尚未通过审核" }` | 业务错误 |

### 5.3 客户端消息发送时序（防重复气泡）

```
用户点发送
  → 本地立即插入一条 status=sending 的气泡（用 clientMsgId 标识）
  → 通过 WS 发 MSG
  → 收到 ACK(clientMsgId)  → 把该气泡标记为已送达（灰色转黑色）
  → 3s 未收到 ACK          → 标记失败，显示重试按钮（重试沿用同一个 clientMsgId）
  → 收到 MSG 且 fromId == 自己（多端/回环） → 按 clientMsgId 去重，不新增气泡
```

---

## 6. 附录：与 SPEC §7 的对应关系

| SPEC §7 序号 | 模块 | 本文档章节 |
|---|---|---|
| 1 | 探针 | §4.1 |
| 2–9 | 认证 | §4.2 |
| 10–14 | 用户（含学生档案） | §4.3 |
| 15 | 文件 | §4.4 |
| 16–19 | 教师资质与资料 | §4.5 |
| 20–25 | 管理员（含建议回复） | §4.6 |
| 26 | 首页 | §4.7 |
| 27–31 | 教师检索与收藏 | §4.8 |
| 32–34 | 课程 | §4.9 |
| 35–36 | 下单 | §4.10（API-ORDER-01/02） |
| 37–38 | 支付 | §4.11 |
| 39–41 | 钱包与券 | §4.12 |
| 42–47 | 订单管理 | §4.10（API-ORDER-03~08） |
| 48–52 | 聊天 | §4.13 + §5 |
| 53–58 | 报告与提醒 | §4.14 |
| 59–61 | 分析 | §4.15 |
| 62–65 | 评价与建议 | §4.16 |

> SPEC §7 已与本表对齐（共 65 条）。**新增接口的流程**：先改 SPEC §7 主表，再在本文档 §4 补详情，最后同步双方代码。

### 6.1 与 SPEC §7 的三处口径统一（已同步，勿回退）

| 项 | 旧口径 | 现行口径 |
|---|---|---|
| `/api/chat/messages` 定位参数 | 按 `sessionId` | 按 **`peerId`**，服务端按「小 ID 在前」归一化后查找/建会话 |
| `/api/chat/read` 请求体 | 未明确 | `{ "peerId": 2001 }`，不用传 `sessionId` |
| `/api/notice/read` 请求体 | 未明确 | `{ "ids": [1401, 1402] }`，`ids` 为空数组＝全部已读 |

---

**文档版本**：v1.0　**最后更新**：2026-10-14
**变更规则**：任何接口字段变更，先改本文件，再同步改客户端 `models/*.ets` 与服务端 `vo/*.java`。
