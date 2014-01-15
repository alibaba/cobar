Cobar是什么？
Cobar是基于MySQL关系型数据的分布式处理系统，它可以在分布式的环境下看上去像传统数据库一样为您提供海量数据服务。

目录结构
driver
  是一个客户端jar包，是对mysql jdbc driver的封装，实现客户端对cobar server集群LB和HA的支持。
manager
  是一个webApp，是cobar server集群的管理控制台，用于集群的运维与监控。
server
  是一个server，是cobar的核心模块，用于实现cobar的全部核心功能。
driver,manager,server是3个独立的工程，相互之间没有关联且独立运行。