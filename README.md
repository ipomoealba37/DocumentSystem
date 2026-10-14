# 文档信息管理系统

基于 Spring Boot + MyBatis + MySQL 的多用户文档管理平台，支持文件上传、回收站恢复和 AI 智能摘要。

## 技术栈

| 分类 | 技术 |
|------|------|
| 后端 | Spring Boot 3.2、MyBatis、MySQL 8.0、Redis |
| 前端 | HTML/CSS/JavaScript、Bootstrap 4.5、jQuery |
| 安全 | BCrypt 密码加密 |
| AI | DeepSeek API（文档摘要生成） |
| 部署 | 阿里云轻量应用服务器、Linux |

## 功能列表

- 用户注册、登录、注销（BCrypt 密码加密）
- 文档增删改查、模糊搜索、批量删除
- 软删除 + 回收站恢复 + 永久删除
- 文件上传、预览、下载
- 多用户数据隔离（每个用户只能操作自己的文档）
- Redis 缓存文档列表，提升查询性能
- AI 智能摘要生成（调用 DeepSeek API）

## 项目结构

src/main/java/com/javaee/documentsystem/
- common/         公共模块
- controller/     控制层
- entity/         实体类
- mapper/         数据访问层
- service/        业务逻辑层

## 如何启动

### 1. 创建数据库

CREATE DATABASE document_system CHARACTER SET utf8mb4;

### 2. 导入 SQL 文件

导入项目根目录下的 document_system.sql。

### 3. 修改配置

修改 application.properties 中的数据库密码和 Redis 地址。

### 4. 启动 Redis

确保本地 Redis 已启动（默认端口 6379）。

### 5. 运行项目

运行 DocumentSystemApplication.java。

### 6. 访问

1.http://localhost:8080/login.html
2. 注册成功后自动跳转登录页
3. 登录后即可使用文档管理、文件上传、回收站、AI 摘要等功能

## 接口示例

- POST /api/user/login              用户登录
- POST /api/user/register           用户注册
- GET  /api/document/list           查询文档列表
- POST /api/document/add            添加文档
