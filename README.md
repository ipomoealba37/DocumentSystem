# 初始用户及密码
name:admin;secret:123456
name:judy;secret:666666

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
| src/main/java/com/javaee/documentsystem/ |
| ├── common/ # 统一返回格式、密码工具、全局异常处理|
| ├── config/ # 静态资源映射配置 |
| ├── controller/ # UserController、DocumentController |
| ├── entity/ # User、Document |
| ├── mapper/ # UserMapper、DocumentMapper |
| └── service/ # UserService、DocumentService |
| src/main/resources/ |
| ├── mapper/ # MyBatis XML |
| ├── static/ # login.html、register.html、main.html |
| └── application.properties |


## 如何启动

### 1. 创建数据库

```sql
CREATE DATABASE document_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
### 2. 导入 SQL 文件
导入项目根目录下的 document_system.sql。
###3. 修改配置
修改 application.properties 中的数据库密码、Redis 地址、DeepSeek API Key。
###4. 启动 Redis
确保本地 Redis 已启动（默认端口 6379）。
###5. 启动项目
运行 DocumentSystemApplication.java。
###6. 访问
http://localhost:8080/login.html

## 接口示例

- POST /api/user/login          用户登录
- POST /api/user/register       用户注册
- GET  /api/document/list       查询文档列表
- POST /api/document/add        添加文档（含文件）
- GET  /api/document/search     模糊搜索
- DELETE /api/document/deleteBatch   批量删除
- GET  /api/document/recycle/list    回收站列表
- PUT  /api/document/recycle/restore/{id}   恢复文档
- POST /api/ai/summary          AI 生成摘要

## 在线访问

部署地址：http://47.95.122.184:8080/login.html
测试账号：admin / 123456

## 注意

- .gitignore 里要排除 target/、.idea/、*.iml
- 不要把数据库密码、API Key 提交上去
