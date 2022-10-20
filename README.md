# 打卡
> 该软件仅供参考学习🤔(  

> 对因后台服务器网址变化或所需交付的报文变化等原因可能产生的一系列麻烦事概不负责  

由于本人没有购买云服务器所以还是不能实现自动打卡

## 内容

### POST 请求头与报文

报文中的关键信息
> 报文中的全部内容见文件`defaultSetting.json`


| 键      | 意义               | 默认值    |
|--------|------------------|--------|
| fxdq   | 风险地区(低/中/高)      | 低风险地区  |
| mqszd  | 目前所在地            | 无      |
| xxdz   | 详细地址             | 无      |
| xm     | 姓名               | 无      |
| xh     | 学号               | 无      |
| jtzz   | 家庭住址(仅省市)        | 陕西省西安市 |
| lxdh   | 联系电话             |无      |
| sfzs   | 是否在陕西(1-在; 0-不在) | 1      |
| mqsxdz | 目前陕西地址           | 西安欧亚学院 |
| rctw   | 体温               | 37     |
<br>

请求头

| 键      | 值               |
|--------|------------------|
|Authorization| \<token\> |
|Content-Type|application/json;|
|token|\<token\>|


### Python程序
使用python程序直接请求很简单，在这里直接贴上，如果app出现问题可以使用python排查

```python
  ## 请求token
  requests.get(f"https://stu.eurasia.edu/yqsb/login/in?zh={ <学号> }&&mm={ <密码> }")
  
  ## 打卡
  requests.post("https://stu.eurasia.edu/yqsb/jkdj/save",headers= <请求头> ,data=json.dumps( <报文> ))
```

## App
发送请求的Fragment: 
* com/example/clockin/clockin.java  

报文等设置的Fragment: 
* com/example/clockin/setting.java
* com/example/clockin/setting_Adapter.java
* com/example/clockin/setting_item.java

在Setting页面配置好报文后点击SAVE按钮会自动重新请求token  
如果配置乱套了点击RESET重新设置报文

配置完成后点击Clockin页面的大按钮，如果打卡成功或当日已经打过卡则会被记录下来直到明天刷新

> java 和 android 写的不熟，很多都是网上边学边找边写，效率太低了

## SERVER
> 需要sqlite3环境  

> 需要python3.10版本及以上
### test文件
test文件为sqlite数据库文件，内有两张表：

`UserInfo` - 用户登陆信息表
|字段名|备注|
|--|--|
|ID| |
|uid|学号(不能为空)|
|pwd|密码(明文,不能为空)|
|token|用学号和密码请求的token(如未过期则一直不更新)|
|tokenTime|~~存储token获取时的日期~~ 遗弃|
|isClockin|~~是否已打卡~~ 遗弃|
|isClockinTime|~~打卡当日的日期~~ 遗弃|

`UserData` - 用户打卡配置信息表
|字段名|备注|默认值|
|--|--|--|
|ID| |自增|
|uid|学号(不能为空)| 无|
| fxdq   | 风险地区(低/中/高)      | 低风险地区  |
| mqszd  | 目前所在地            | 无      |
| xxdz   | 详细地址             | 无      |
| xm     | 姓名               | 无      |
| xh     | 学号               | 无      |
| jtzz   | 家庭住址(仅省市)        | 陕西省西安市 |
| lxdh   | 联系电话             |无      |
| sfzs   | 是否在陕西(1-在; 0-不在) | 1      |
| mqsxdz | 目前陕西地址           | 西安欧亚学院 |
| rctw   | 体温               | 37     |


### conf.py
本文件需单独运行，用于修改用户学号密码或用户打卡配置项

```python
$ python3 ./conf.py
+----------------
| 1. 添加用户
| 2. 修改用户配置
| 3. 删除用户
| 4. 退出
+----------------
>>> 
```

### run.py
运行后一直在线，每小时检测一次时间，每日6点-8点打卡两次  
提取数据后检测token并打卡，如果token过期则重新请求再打卡

```python
$ python3 ./run.py
```


