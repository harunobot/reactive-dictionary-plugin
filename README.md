# 词库插件
用于精确匹配关键词进行响应。常用于处理查询量大，回复确定的情况。所有配置均为yaml格式，请提前熟悉。

#### 支持特性
- [x] 限速
- [x] 白名单
- [ ] 限时撤回

# 使用说明
插件同目录下新建config.yml配置文件。文件内容为数组，结构如下：
```
- name: "全局唯一名称"
  prefix: "响应前缀"
  file: "词库文件相对路径"
  resource: "词库用到的资源文件根目录绝对路径(windows路径需要在盘符前添加'/')"
  groups:
  - duration: 限速时间(单位为秒，0为不限制，需要与frequency一起使用)
    frequency: 限速次数(0为不限制，需要与duration一起使用)
    alivetime: 发送内容存活时间(单位为秒。0为不限制，撤回功能暂未实现)
    group: 群号码
    mute-duration: 禁言时间(单位为秒，0为不禁言)
```
"file"指向的词库文件内容为数组，结构如下：
```
'关键词(不含响应前缀)': (下面内容为数组)
- recordType: "类型"(支持的类型见下表)
  xxx: (类型对应数据)
```

-----------------
> 下文提及的相对路径均以插件所在目录为对象。

#### 实现
<details>
<summary>已实现词库类型</summary>

- 字符串
```
'字符串':
- recordType: "TEXT"
  text: "字符串内容1"
- recordType: "TEXT"
  text: "字符串内容2"
'换行':
- recordType: "TEXT"
  text: "\n"
```
- 图片
```
'图片':
- recordType: "IMAGE"
  file: "文件相对路径"
- recordType: "IMAGE"
  file: "文件相对路径"
```
- 随机内容(将从一级数组随机选择一个，二级数组不支持随机内容)
```
'随机内容':
- recordType: "RANDOM"
  entry:
  - - recordType: "TEXT"
      text: "random1"
    - recordType: "TEXT"
      text: "random2"
  - - recordType: "TEXT"
      text: "random3"
    - recordType: "TEXT"
      text: "random4"
```


</details>