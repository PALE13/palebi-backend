
<p align="center">
</p>

<p align="center">
<a>
    <img src="https://img.shields.io/badge/Spring Boot-2.7.2-brightgreen.svg" alt="Spring Boot">
    <img src="https://img.shields.io/badge/MySQL-8.0.20-orange.svg" alt="MySQL">
    <img src="https://img.shields.io/badge/Java-1.8.0__371-blue.svg" alt="Java">
    <img src="https://img.shields.io/badge/Redis-5.0.14-red.svg" alt="Redis">
    <img src="https://img.shields.io/badge/RabbitMQ-3.9.11-orange.svg" alt="RabbitMQ">
    <img src="https://img.shields.io/badge/MyBatis--Plus-3.5.2-blue.svg" alt="MyBatis-Plus">
    <img src="https://img.shields.io/badge/Redisson-3.21.3-yellow.svg" alt="Redisson">
    <img src="https://img.shields.io/badge/Gson-3.9.1-blue.svg" alt="Gson">
    <img src="https://img.shields.io/badge/Hutool-5.8.8-green.svg" alt="Hutool">
    <img src="https://img.shields.io/badge/MyBatis-2.2.2-yellow.svg" alt="MyBatis">
</a>
</p>

# PALE-BI
基于 React + Spring Boot + MQ + AIGC 的智能数据分析平台。区别于传统 BI，用户只需要导入原始数据集、并输入分析诉求，就能自动生成可视化图表及分析结论，实现数据分析的降本增效。

## 已有功能
用户登录  
智能分析（同步）。调用AI根据用户上传csv文件生成对应的 JSON 数据，并使用 ECharts图表 将分析结果可视化展示  
智能分析（异步）。使用了线程池异步生成图表，最后将线程池改造成使用 RabbitMQ消息队列 保证消息的可靠性，实现消息重试机制  
用户限流。本项目使用到令牌桶限流算法，使用Redisson实现简单且高效分布式限流，限制用户每秒只能调用一次数据分析接口，防止用户恶意占用系统资源调用AI进行数据分析，并控制AI的输出  
由于AIGC的输入 Token 限制，使用 Easy Excel 解析用户上传的 XLSX 表格数据文件并压缩为CSV，实测提高了20%的单次输入数据量、并节约了成本。  
后端自定义 Prompt 预设模板并封装用户输入的数据和分析诉求，通过对接 AIGC 接口生成可视化图表 JSON 配置和分析结论，返回给前端渲染。  


## 项目亮点
自动化分析：通过AI技术，将传统繁琐的数据处理和可视化操作自动化，使得数据分析过程更加高效、快速和准确。  
一键生成：只需要导入原始数据集和输入分析目标，系统即可自动生成符合要求的可视化图表和分析结论，无需手动进行复杂的操作和计算。  
可视化管理：项目提供了图表管理功能，可以对生成的图表进行整理、保存和分享，方便用户进行后续的分析和展示。  
异步生成：项目支持异步生成，即使处理大规模数据集也能保持较低的响应时间，提高用户的使用体验和效率。  
AI对话功能：除了自动生成图表和分析结果，项目还提供了AI对话功能，可以与系统进行交互，进一步解答问题和提供更深入的分析洞察。  
智能数据处理：项目通过AI技术实现了智能化的数据处理功能，能够自动识别和处理各种数据类型、格式和缺失值，提高数据的准确性和一致性。  


## 项目架构
客户端输入分析诉求和原始数据，向业务后端发送请求。业务后端将请求事件放入消息队列，并为客户端生成取餐号，让要生成图表的客户端去排队，消息队列根据AI服务负载情况，定期检查进度，如果AI服务还能处理更多的图表生成请求，就向任务处理模块发送消息。任务处理模块调用AI服务处理客户端数据，AI 服务异步生成结果返回给后端并保存到数据库，当后端的AI服务生成完毕后，可以通过向前端发送通知的方式，或者通过业务后端监控数据库中图表生成服务的状态，来确定生成结果是否可用。若生成结果可用，前端即可获取并处理相应的数据，最终将结果返回给客户端展示，在此期间，用户可以去做自己的事情。 

![image-20240401220005946](https://palepics.oss-cn-guangzhou.aliyuncs.com/img/image-20240401220005946.png)


## 技术栈
**后端**  
Spring Boot 2.7.2  
Spring MVC  
MyBatis + MyBatis Plus 数据访问（开启分页）  
Spring Boot 调试工具和项目处理器  
Spring AOP 切面编程  
Spring Scheduler 定时任务    
Spring 事务注解    
Redis：Redisson限流控制    
MyBatis-Plus 数据库访问结构      
IDEA插件 MyBatisX：根据数据库表自动生成    
RabbitMQ：消息队列    
AI SDK：讯飞星火  
JDK 线程池及异步化    
Swagger + Knife4j 项目文档    
Easy Excel：表格数据处理、Hutool工具库 、Apache Common Utils、Gson 解析库、Lombok 注解    


**前端**    
React 18  
Umi 4 前端框架  
Ant Design Pro 5.x 脚手架  
Ant Design 组件库  
OpenAPI 代码生成：自动生成后端调用代码  
EChart 图表生成  

