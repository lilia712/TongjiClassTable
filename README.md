# ClassTable - 同济课程表

一款专为同济大学学生设计的课程表应用，通过对接同济一站式服务自动同步课表数据，支持周视图展示、课程详情查看、自定义上课时间等功能。

## 功能特性

- **一键同步** - 登录同济一站式服务，自动获取课表数据
- **周视图展示** - 清晰的七天课表网格，彩色色块区分不同课程
- **课程详情** - 点击课程卡片查看教师、教室、上课周数等信息
- **周数切换** - 左右切换查看不同周次的课程安排
- **日期显示** - 表头显示对应日期，方便对照
- **自定义时间** - 支持设置每节课的起止时间
- **固定时长** - 可设置统一课时长度，自动计算结束时间
- **开学日期** - 设置学期开始日期，自动计算当前周数
- **新手教程** - 内置引导教程帮助快速上手

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Kotlin |
| UI 框架 | Jetpack Compose + Material 3 |
| 架构 | MVVM（ViewModel + StateFlow） |
| 网络 | Retrofit + OkHttp |
| 数据同步 | WebView + JavaScript 注入 |
| 最低版本 | Android 7.0（API 24） |

## 项目结构

```
app/src/main/java/com/example/courseschedule/
├── MainActivity.kt          # 主界面 - 课表展示
├── TutorialDialog.kt        # 新手教程弹窗
├── data/
│   ├── Course.kt            # 课程数据模型
│   ├── Timetable.kt         # 课表数据模型
│   ├── TimetableStorage.kt  # 本地数据存储
│   ├── TongjiApiService.kt  # 同济 API 接口
│   ├── TongjiMapper.kt      # 数据映射转换
│   ├── TongjiResponse.kt    # API 响应模型
│   └── RetrofitClient.kt    # 网络客户端配置
├── sync/
│   ├── SyncActivity.kt      # 同步登录页面
│   ├── TongjiSyncManager.kt # 同步逻辑管理
│   └── TongjiWebViewClient.kt # WebView 客户端
└── viewmodel/
    ├── ViewModel.kt         # 课表视图模型
    └── ViewModelFactory.kt  # ViewModel 工厂
```

## 构建与运行

### 环境要求

- Android Studio Ladybug 或更高版本
- JDK 11+
- Android SDK 37

### 步骤

```bash
git clone https://github.com/lilia712/TongjiClassTable.git
```

使用 Android Studio 打开项目，等待 Gradle 同步完成后运行即可。

## 使用说明

1. 首次打开应用会显示空白课表
2. 点击「同步课表」按钮，跳转到登录页面
3. 输入同济一站式服务账号密码登录
4. 登录成功后点击「同步课表」按钮完成数据同步
5. 在设置中配置开学日期和每节课的时间
6. 课表将自动按周展示当前周的课程安排


