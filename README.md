# 一键打卡
> ！免责声明: 因后台服务器网址变化或所需交付的报文变化而可能产生的一系列麻烦事概不负责  

由于本人没有购买云服务器所以还是不能实现自动打卡

## 代码

### POST 请求头与报文

报文


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