<p align="center">
  <img src="favicon.ico" alt="Nextdoc4j Logo" width="180">
</p>

<h1 align="center">Nextdoc4j</h1>

<p align="center">
  <strong>🚀 现代化的 API 文档后端解决方案</strong><br>
  基于 <strong>SpringBoot 3.5.7</strong> + <strong>SpringDoc</strong> 构建，替代 Swagger UI，<br>
  提供更美观、更强大的开发体验，让 API 文档焕然一新。
</p>

<p align="center">
  🌐 <a href="https://demo.nextdoc4j.top/">在线演示</a> |
  📘 <a href="https://nextdoc4j.top/">官方文档</a> |
  🧩 <a href="https://nextdoc4j.top/more/changelog.html">更新日志</a> |
  ❓ <a href="https://nextdoc4j.top/more/faq.html">常见问题</a>
</p>

## 📖 项目简介

**NextDoc4j Demo** 是一个演示项目，用于展示 Nextdoc4j 在不同架构下的集成方式，包括传统的单体应用和现代化的微服务架构。

该项目的主要目标是：

1. **展示 NextDoc4j 的基本功能**
   提供可运行的示例代码，让开发者快速了解 NextDoc4j 的核心用法。

2. **演示不同架构下的集成方式**
   展示 NextDoc4j 在 Spring Boot 单体应用和 Spring Cloud 微服务架构中的集成方法。

3. **作为学习与参考示例**
   开发者可以参考该项目快速上手，或在自己的项目中测试 NextDoc4j 文档生成功能。

> ⚡ 本项目仅作演示与示例用途，不建议直接用于生产环境。

## 🏗 项目结构

本项目采用 Maven 多模块结构，包含以下模块：

```
nextdoc4j-demo
├── nextdoc4j-demo-common          # 公共模块，包含通用配置、实体类、工具类等
├── nextdoc4j-demo-springboot      # Spring Boot 单体应用示例
└── nextdoc4j-demo-springcloud-gateway  # Spring Cloud 微服务架构示例
    ├── gateway-server             # 网关服务
    └── modules                    # 业务服务模块
        ├── user-service           # 用户服务
        ├── system-service         # 系统服务
        └── file-service           # 文件服务
```

## 🚀 快速开始

### 环境要求

- JDK 17 或更高版本
- Maven 3.6+
- IDE 支持（IntelliJ IDEA, Eclipse, VS Code 等）

### 运行 Spring Boot 单体应用

```bash
cd nextdoc4j-demo-springboot
mvn spring-boot:run
```

访问地址：http://localhost:8080/doc.html

### 运行 Spring Cloud 微服务架构

1. 启动网关服务：
```bash
cd nextdoc4j-demo-springcloud-gateway/gateway-server
mvn spring-boot:run
```

2. 启动各业务服务：
```bash
# 用户服务
cd nextdoc4j-demo-springcloud-gateway/modules/user-service
mvn spring-boot:run

# 系统服务
cd nextdoc4j-demo-springcloud-gateway/modules/system-service
mvn spring-boot:run

# 文件服务
cd nextdoc4j-demo-springcloud-gateway/modules/file-service
mvn spring-boot:run
```

访问地址：http://localhost:9000/<网关配置的业务系统地址>/doc.html

## 📁 模块说明

### Common 模块
包含项目通用的配置、实体类、枚举、工具类等，被其他模块共享。

### Spring Boot 单体应用
展示了在传统的 Spring Boot 单体应用中如何集成 NextDoc4j，适合小型项目或快速原型开发。

### Spring Cloud 微服务架构
展示了在微服务架构中如何使用 NextDoc4j，包括：
- 网关层的统一文档入口
- 各个微服务的独立文档聚合
- 服务发现与负载均衡集成

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支：`git checkout -b feature/new-feature`
3. 提交更改：`git commit -am 'Add new feature'`
4. 推送分支：`git push origin feature/new-feature`
5. 提交 Pull Request

## 📄 许可证

本项目采用 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.html) 许可证。

## 💬 联系我们

- 📧 邮箱：nextdoc4j@163.com
- 🌐 官网：[https://nextdoc4j.top](https://nextdoc4j.top)

---

**NextDoc4j** - 让 API 文档焕然一新！  如果这个项目对你有帮助，请给它一个 ⭐️