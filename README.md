# 股票决策应用 (Stock Decision)

一个基于 Kotlin 和 Jetpack Compose 开发的 Android 股票监控与决策应用。

## 功能特性

### 核心功能
- **股票监控**: 添加股票代码、买入价格、数量、期望收益率和目标价格
- **实时价格监控**: 后台前台服务每分钟检查股票价格
- **智能提醒**: 当股票达到目标收益率或目标价格时，通过通知、弹窗和邮件提醒
- **历史数据分析**: 查看股票过去一年价格走势，统计历史最高/最低价
- **AI 预测**: 模拟 AI 预测和推荐功能（可替换为真实 AI 服务）
- **资讯板块**: 展示相关财经新闻（模拟数据，可接入真实 API）

### 技术特点
- **目标平台**: Android 16 (API 35)
- **UI 框架**: Jetpack Compose + Material3
- **架构模式**: MVVM (ViewModel + Repository + Flow)
- **本地存储**: Room 数据库
- **网络请求**: Retrofit + Gson
- **图表展示**: Vico Compose
- **邮件发送**: JavaMail (SMTP)
- **安全配置**: EncryptedSharedPreferences 加密存储敏感信息

## 快速开始

### 环境要求
- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK 35

### 配置步骤

#### 1. 获取 Alpha Vantage API Key

1. 访问 [Alpha Vantage](https://www.alphavantage.co/support/#api-key) 官网
2. 免费注册获取 API Key
3. 在 `gradle.properties` 文件中替换 API Key：

```properties
ALPHA_VANTAGE_API_KEY=your_actual_api_key_here
```

**注意**: 免费版 API 限制为每分钟 5 次调用，每天 500 次调用。

#### 2. 配置邮件 SMTP

在应用内的"邮件配置"页面设置：

| 字段 | 说明 | 示例 |
|------|------|------|
| SMTP 服务器 | 邮件服务商的 SMTP 地址 | smtp.gmail.com |
| 端口 | SMTP 端口 | 587 (TLS) 或 465 (SSL) |
| 邮箱账号 | 发送方邮箱地址 | your_email@gmail.com |
| 密码/授权码 | SMTP 授权码（非登录密码） | xxxx xxxx xxxx xxxx |
| 接收邮箱 | 接收提醒的邮箱地址 | recipient@example.com |

**常见邮箱 SMTP 设置：**

- **Gmail**: 
  - 服务器: smtp.gmail.com
  - 端口: 587
  - 需要开启两步验证并使用应用专用密码

- **QQ邮箱**:
  - 服务器: smtp.qq.com
  - 端口: 587
  - 需要开启 SMTP 服务并获取授权码

- **163邮箱**:
  - 服务器: smtp.163.com
  - 端口: 25 或 587
  - 需要开启 SMTP 服务

#### 3. 构建项目

```bash
# 克隆项目
git clone <repository-url>
cd StockDecision

# 构建 Debug APK
./gradlew assembleDebug

# 构建 Release APK
./gradlew assembleRelease
```

#### 4. 安装运行

```bash
# 安装到连接的设备
./gradlew installDebug
```

或在 Android Studio 中点击 "Run" 按钮直接运行。

## GitHub Actions 自动构建

本项目已配置 GitHub Actions 工作流，实现自动构建 APK。

### 触发条件
- 每次推送到 `main` 或 `master` 分支
- 创建以 `v` 开头的标签（如 `v1.0.0`）
- 提交 Pull Request 到主分支

### 获取构建产物

1. 推送代码后，访问 GitHub 仓库的 **Actions** 标签页
2. 选择最新的工作流运行
3. 在 **Artifacts** 部分下载 `debug-apk` 文件
4. APK 文件位于压缩包中的 `app/build/outputs/apk/debug/` 目录

### 工作流配置

工作流文件位于 `.github/workflows/build.yml`，包含：
- **Build 任务**: 构建 Debug 和 Release APK
- **Lint 任务**: 代码质量检查
- **缓存优化**: Gradle 依赖缓存加速构建

## 项目结构

```
StockDecision/
├── app/
│   ├── src/main/java/com/example/stockdecision/
│   │   ├── data/
│   │   │   ├── local/          # Room 数据库
│   │   │   ├── remote/         # Retrofit API
│   │   │   ├── repository/     # 数据仓库
│   │   │   └── model/          # 数据模型
│   │   ├── service/            # 前台服务、邮件发送
│   │   ├── ui/
│   │   │   ├── theme/          # Compose 主题
│   │   │   ├── screens/        # 界面屏幕
│   │   │   └── viewmodel/      # ViewModel
│   │   └── utils/              # 工具类
│   └── build.gradle.kts        # 应用级构建配置
├── .github/workflows/           # GitHub Actions
├── build.gradle.kts            # 项目级构建配置
└── gradle.properties           # Gradle 配置（含 API Key）
```

## 适配说明

### 小米14 (6.4英寸) 适配
- 使用响应式布局（`Modifier.fillMaxWidth()`）
- 动态字体大小适配
- 列表项高度优化，确保可点击区域充足
- 图表高度设为屏幕高度的 30-40%

### Android 16 新特性
- 前台服务类型声明 (`FOREGROUND_SERVICE_DATA_SYNC`)
- Android 13+ 通知权限动态申请
- 加密 SharedPreferences 存储敏感信息

## 权限说明

应用需要以下权限：

| 权限 | 用途 |
|------|------|
| `INTERNET` | 访问网络获取股票数据 |
| `ACCESS_NETWORK_STATE` | 检查网络状态 |
| `POST_NOTIFICATIONS` | 发送价格提醒通知（Android 13+） |
| `VIBRATE` | 提醒时震动 |
| `FOREGROUND_SERVICE` | 运行前台监控服务 |
| `FOREGROUND_SERVICE_DATA_SYNC` | 前台服务数据同步类型 |
| `WAKE_LOCK` | 保持 CPU 运行以监控价格 |

## 常见问题

### Q: API 调用频率限制怎么办？
A: Alpha Vantage 免费版限制为每分钟 5 次调用。应用已实现内存缓存机制，同一分钟内不会重复请求相同股票。

### Q: 邮件发送失败？
A: 请检查：
1. SMTP 服务器地址和端口是否正确
2. 是否使用授权码而非登录密码
3. 邮箱是否开启 SMTP 服务
4. 网络连接是否正常

### Q: 后台监控不工作？
A: 请检查：
1. 是否授予通知权限
2. 电池优化设置中是否允许后台运行
3. 是否有活跃监控的股票

### Q: 如何替换为真实 AI 服务？
A: 修改 `StockViewModel.kt` 中的 `generateMockAIPrediction` 和 `generateMockAIRecommendations` 方法，接入真实的 AI API。

## 许可证

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request！

## 更新日志

### v1.0.0
- 初始版本发布
- 股票监控功能
- 后台价格检查
- 邮件/通知提醒
- 历史价格图表
- AI 预测与推荐（模拟）
