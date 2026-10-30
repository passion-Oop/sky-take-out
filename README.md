# 苍穹外卖 - Cangqiao Takeout

> 基于 SpringBoot 的 O2O 外卖平台后端实战项目

## ✨ 项目简介
苍穹外卖是一套企业级 Java 后端外卖项目，采用前后端分离架构。项目包含**商家管理后台接口**与**微信小程序用户端接口**，完整覆盖外卖平台核心业务流程，适合课程实训、毕业设计、简历项目展示。

## 🛠️ 技术栈
| 分类     | 技术                  |
|----------|-----------------------|
| 核心框架 | SpringBoot、SpringMVC |
| 持久层   | MyBatis、MyBatis-Plus |
| 数据库   | MySQL 8.0             |
| 缓存     | Redis                 |
| 身份认证 | JWT                   |
| 接口文档 | Knife4j(Swagger)      |
| 反向代理 | Nginx                 |
| 工具     | Lombok、Maven         |

## 📦 功能模块
### 管理端（商家后台）
- 员工登录、账号启用/禁用、退出登录
- 菜品分类管理：新增、编辑、删除、启停
- 菜品管理：新增菜品、图片上传、上下架、套餐关联
- 套餐管理：套餐创建、菜品组合、套餐上下架
- 订单管理：订单查询、接单、拒单、派送、完成、取消订单
- 数据统计：营业额统计、订单报表、销量排名

### 用户端（微信小程序）
- 微信授权登录
- 菜品分类、套餐浏览查询
- 购物车：添加、修改数量、清空购物车
- 用户地址簿：新增、编辑、设置默认收货地址
- 下单、模拟支付、订单查询、订单取消
- 查看历史订单

## 💡 项目亮点
1. 标准化分层架构：`Controller / Service / Mapper / Entity / DTO / VO`，符合企业开发规范
2. 全局异常处理器 + 统一返回结果封装
3. Redis 缓存菜品分类与热门菜品，减少数据库压力
4. JWT 无状态令牌实现登录鉴权，基于拦截器做权限校验
5. 完善接口文档，所有接口携带参数注释，便于调试测试

## 📁 项目结构
```bash
cangqiao-takeout
├── src
│   └── main
│       └── java
│           └── com
│               └── sky
│                   ├── config        # 配置类、拦截器、跨域、Knife4j、Redis配置
│                   ├── controller    # 接口控制器（管理端 + 用户端）
│                   ├── service       # 业务逻辑层
│                   ├── mapper        # MyBatis持久层
│                   ├── entity        # 数据库实体
│                   ├── dto           # 数据传输对象
│                   ├── vo            # 视图返回对象
│                   ├── util          # 工具类：JWT、Redis工具等
│                   ├── exception     # 自定义全局异常处理
│                   └── result        # 统一返回结果封装
│       └── resources
│           ├── application.yml       # 主配置文件
│           └── mybatis               # mapper xml文件
├── sql                               # 项目数据库脚本
├── pom.xml                           # Maven依赖配置
└── README.md                         # 项目说明文档
```

## 📋 环境要求
- JDK 1.8+
- Maven 3.6+
- MySQL 8.0
- Redis 5.0+

## 🚀 快速启动
1. 克隆仓库
```bash
git clone https://github.com/你的用户名/cangqiao-takeout.git
cd cangqiao-takeout
