# 短链接系统 (Short URL System)

一个基于Spring Boot和MyBatis-Plus的高性能短链接服务系统，支持URL缩短、重定向、访问统计和链接过期管理。

## 🚀 功能特性

- **URL缩短**: 将长URL转换为短链接
- **智能重定向**: 通过短链接快速跳转到原始URL
- **访问统计**: 实时记录每个短链接的访问次数
- **过期管理**: 支持设置链接过期时间，自动清理过期链接
- **高性能缓存**: 使用Redis缓存提升访问速度
- **安全验证**: 多层URL验证机制，防止XSS攻击和恶意链接
- **国际化支持**: 支持中文域名和国际化URL
- **监控支持**: 集成Actuator和Prometheus监控

## 🛠 技术栈

- **后端框架**: Spring Boot 3.2.0
- **ORM框架**: MyBatis-Plus 3.5.5
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **监控**: Spring Boot Actuator + Prometheus
- **反向代理**: Nginx
- **构建工具**: Maven
- **Java版本**: JDK 21

## 📋 系统要求

- JDK 21+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.6+

## 🔧 快速开始

### 1. 克隆项目
```bash
git clone <repository-url>
cd short-url-system
```

### 2. 数据库初始化
```bash
# 执行SQL初始化脚本
mysql -u root -p < sql/init.sql
```

### 3. 配置文件
修改 `src/main/resources/application.yml` 中的数据库和Redis连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/short_url_db?useSSL=false&serverTimezone=UTC
    username: your_username
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
```

### 4. 启动应用
```bash
mvn clean install
mvn spring-boot:run
```

应用将在 `http://localhost:8086` 启动

## 📖 API文档

### 创建短链接
```http
POST /api/shorten
Content-Type: text/plain

https://www.example.com/very/long/url
```

**响应示例:**
```
http://localhost:8086/abc123
```

### 访问短链接
```http
GET /{shortCode}
```
自动重定向到原始URL

## 🏗 项目结构

```
src/
├── main/
│   ├── java/com/example/shorturl/
│   │   ├── config/          # 配置类
│   │   ├── controller/      # 控制器
│   │   ├── mapper/          # MyBatis-Plus Mapper
│   │   ├── model/           # 实体类
│   │   ├── service/         # 服务层
│   │   └── util/            # 工具类
│   └── resources/
│       ├── mapper/          # MyBatis XML映射文件
│       └── application.yml  # 配置文件
└── sql/
    └── init.sql            # 数据库初始化脚本
```

## ⚙️ 配置说明

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `short-url.domain` | 短链接域名 | `http://localhost:8086/` |
| `short-url.length` | 短链接长度 | `6` |
| `short-url.expiration-days` | 链接过期天数 | `7` |

## 🔍 监控端点

应用集成了Spring Boot Actuator，提供以下监控端点：

- `/actuator/health` - 健康检查
- `/actuator/metrics` - 应用指标
- `/actuator/prometheus` - Prometheus指标

## 🚀 部署

### 使用Nginx反向代理
项目包含了 `nginx.conf` 配置文件，可以直接使用：

```bash
# 复制配置文件到Nginx配置目录
sudo cp nginx.conf /etc/nginx/sites-available/short-url
sudo ln -s /etc/nginx/sites-available/short-url /etc/nginx/sites-enabled/
sudo nginx -s reload
```

### Docker部署
```bash
# 构建镜像
docker build -t short-url-system .

# 运行容器
docker run -d -p 8086:8086 --name short-url short-url-system
```

## 🔒 URL验证机制

系统采用多层URL验证机制，确保链接的安全性和有效性：

### 验证层级
1. **基础检查**: 空值检查、长度限制（最大2048字符）
2. **安全检查**: 防止XSS攻击、过滤危险协议（javascript:、data:、file:等）
3. **格式检查**: 使用正则表达式验证URL格式
4. **Java URL验证**: 使用Java内置URL类进行最终验证

### 支持的URL格式
- ✅ HTTP/HTTPS协议
- ✅ 国际化域名（支持中文域名）
- ✅ IPv4地址
- ✅ 端口号指定
- ✅ 路径参数、查询参数、锚点
- ✅ localhost和本地开发环境

#### 支持的URL示例
```
✅ 基础URL
https://www.example.com
http://example.com
https://github.com/user/repo

✅ 带端口号
http://localhost:8080
https://example.com:443
http://192.168.1.1:3000

✅ 带路径和参数
https://www.example.com/path/to/page
https://example.com/search?q=keyword&page=1
https://example.com/article#section1
https://api.example.com/v1/users?limit=10&offset=20

✅ 国际化域名
https://www.中文域名.com
https://测试.网站.cn
https://example.测试

✅ IPv4地址
http://192.168.1.1
https://10.0.0.1:8443
http://127.0.0.1:3000

✅ 本地开发
http://localhost
https://localhost:8080
http://localhost:3000/api/test
```

#### 不支持的URL示例
```
❌ 危险协议
javascript:alert('xss')
data:text/html,<script>alert('xss')</script>
file:///etc/passwd
ftp://example.com/file.txt

❌ 格式错误
http://
https://
example.com (缺少协议)
http://.com
http://example

❌ 安全风险
http://example.com/<script>alert('xss')</script>
http://example.com/javascript:alert('xss')
http://example.com/onload=alert('xss')

❌ 其他无效格式
超长URL (>2048字符)
http://192.168.1.256 (无效IP)
http://example.com:99999 (无效端口)
```

### 安全特性
- 🛡️ XSS攻击防护
- 🛡️ 危险协议过滤
- 🛡️ 恶意脚本检测
- 🛡️ URL长度限制

## 📊 性能特点

- **高并发**: 支持高并发访问，使用Redis缓存减少数据库压力
- **快速响应**: 平均响应时间 < 50ms
- **自动扩展**: 支持水平扩展，可部署多个实例
- **故障恢复**: 缓存失效时自动从数据库恢复
- **智能验证**: 多层URL验证，确保链接安全有效

## 🤝 贡献

欢迎提交Issue和Pull Request来改进项目。

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 联系方式

如有问题或建议，请通过以下方式联系：

- 提交 [Issue](../../issues)
- 发送邮件至: [your-email@example.com]

---

⭐ 如果这个项目对你有帮助，请给它一个星标！