Cobar是什么？<br>
  Cobar是基于MySQL关系型数据的分布式处理系统，它可以在分布式的环境下看上去像传统数据库一样为您提供海量数据服务。<br>

目录结构<br>
driver<br>
&nbsp;&nbsp;是一个客户端jar包，是对mysql jdbc driver的封装，实现客户端对cobar server集群LB和HA的支持。<br>
manager<br>
&nbsp;&nbsp;是一个webApp，是cobar server集群的管理控制台，用于集群的运维与监控。<br>
server<br>
&nbsp;&nbsp;是一个server，是cobar的核心模块，用于实现cobar的全部核心功能。<br>
driver,manager,server是3个独立的工程，相互之间没有关联且独立运行。<br>
