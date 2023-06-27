 websocket_netty_chat_room

基于Netty+WebSocket的单体Web聊天室，支持给定组的群聊

## 启动方式

先启动WebsocketApplication

然后访问

```http
http://localhost:8111
```

## 注意事项

1 Netty服务默认绑定端口为9999，如果需要修改，关注如下两个地方

- com.lds.websocket.netty.WebSocketServer#**start**
- /resources/js/common-variate.js 修改变量**wsHost**为自己指定的端口



2 如果需要发布到自己服务器上，需要修改如下

- /resources/js/common-variate.js 修改变量**wsHost**
  - localhost改为自己服务公网ip或者域名
  - 端口改成自己Netty服务的端口

> ps:记得在防火墙开放8111和Netty服务的端口



## 后续计划

1 目前是单体应用，且未持久化，刷新页面即丢失。

2 目前ws数据结构基于JSon,计划进行改造；

3不安全，计划引入Token校验。

4 后期计划基于Zookeeper构建集群版本的聊天室。