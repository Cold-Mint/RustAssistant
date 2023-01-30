### 铁锈助手 - Rust Assistant
安卓端铁锈战争游戏编码辅助工具。

#### 源码运行问题解决：

###### 1.打包时报错，缺少SDK路径

```
SDK location not found.
```

请在项目根目录创建名为 local.properties 的文件。并在其文件内写入

```
sdk.dir=(安卓SDK路径)
```
