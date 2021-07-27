自助式网络直播SDK（Android版）

一、集成方式：

APP直接依赖uilib库即可，界面可在UI库中自定义
具体API调用方式请参考uilib，或官方文档
####注释
uilib里面的aar替换为maven依赖
具体使用 参考demo
项目的 build.gradle里面添加

```
allprojects {
 maven{
       mavenCentral()
       }
}
``` 

 在uilib里面添加 具体使用依赖

```
 //SaaSSDK
    api 'com.github.vhall.android.library:vh-saas-sdk:6.1.2'
    api 'com.github.vhall.android.library:vh-saas-interactive:6.1.2'
    //投屏相关
    api 'com.github.vhall.android.library:vh-saas-sdk-support:2.0.1'
    
```

二、版本更新信息：
版本6.1.2 更新时间 2021.07.27  
1.新增设置RSA设置私钥接口 
2.优化直播关闭交互方式
3.优化已知bug
 
版本6.1.0 更新时间 2021.06.30  
1.增加主播/嘉宾互动  
2.登陆token失效优化  
3.bug修复  
4.版本向下兼容  
5.更多内容参考[官网](https://saas-doc.vhall.com/docs/show/1204)

版本5.0.0 更新时间 2020.10.30  
1.优化用户使用 h5和flash统一使用方法  
2.优化日志上报  
3.添加h5文档显示 具体可以参考demo实现  
4.修改bug  
####sdk更新方法
删除全部vss相关代码（目前升级后vss的会报错 全部删除即可）
以后统一h5和flash


版本：v4.3.1 更新时间 2020.07.02  
1.支持预告活动，进入后可收到开始直播消息；  
2.Demo 问答显示隐藏优化；  

版本：v4.3.0 更新时间 2020.06.15  
1.支持水印显示；  
2.修复角色问题；  
3.修复回复空数据问题；  
4.优化demo；


版本：V4.2.0 更新时间 2020.04.29  
1.升级互动模块，新增互动美颜；  
2.完善房间信息数据返回（新增问答开启状态，投屏权限）；  
3.完善禁言状态判断；  
4.新增聊天回复、@操作数据返回；  
5.升级投屏依赖包，完善投屏暂停、停止、重播、获取进度操作；


版本：V4.1.0 更新时间2020.03.13  
更新内容：  
1.修复播放器弱网卡死问题；  
2.升级直播美颜；  
3.完善点播播放器屏幕自动适配；
4.完善直播回复显示；  
5.新增SDK混淆配置；  
6.优化数据统计；

 
版本：v4.0.0 更新时间： 2019.09.06  
更新内容：  
1.配合PC版，H5类型房间服务对SDK进行升级；  
2.升级聊天服务；  
3.升级文档服务；  
4.升级基础服务包，提升SDK稳定性；
 
版本： v3.4.4 更新时间：2019.08.09  
更新内容： 
1.修复回放拖动进度条引起的崩溃；

版本：v3.4.3 更新时间：2019.06.11  
更新内容:  
1.新增文档开关功能；  
2.修复看直播全屏异常；  
3.优化消息服务；
  
版本：v3.4.2 更新时间：2019.05.17  

更新内容：  
1.点播新增倍速播放支持；

版本：v3.4.1 更新时间：2019.05.09 
 
更新内容：  
1.更新直播底层库，修复偶现直播崩溃问题；  
2.完善demo逻辑；
  
版本：v3.4.0 更新时间：2019.04.19  

更新内容：  
1. 更新互动底层库，支持双流设置;  
2. 新增互动邀请功能；  
3. 完善点播播放器缩放模式支持； 
  
版本：v3.3.1 更新时间：2019.03.16  
  
更新内容：  
1.修复9.0以上机型横竖屏切换bug；

版本：v3.3.0 更新时间：2019.02.26  
  
更新内容：  
1、功能优化   
2、新增被邀请上麦功能
  
版本：v3.2.3 更新时间：2018.12.21

更新内容：  
1、添加踢出、禁言、全员禁言等功能  
2、修复已知Bug

版本：v3.2.2 更新时间：2018.11.16

更新内容：  
1、修复已知BUG



版本：v3.2.1 更新时间：2018.10.25

更新内容：  
1、互动模块拆分  
2、修复已知BUG

版本：v3.2.0 更新时间：2018.08.09

更新内容：  
1、添加互动模块  
2、uiLibs 添加互动模块演示

版本：v3.1.1 更新时间：2018.07.27

更新内容：  
1、添加历史问答接口 （看直播、看回放通用）


版本：v3.1.0.2 更新时间：2018.06.26

更新内容：  
1、添加回放切换分辨率功能

版本：v3.1.0 更新时间：2018.0604

更新内容：  
1、提高SDK编译环境、更换权限申请模式

2、发直播、看直播、看回放添加调度BU

3、添加数据上报

4、bug修复等


版本：v3.0.3 更新时间：2018.01.11

更新内容：  
1、防盗链重构

2、自定义消息BUG修改


版本：v3.0.2 更新时间：2017.11.22

更新内容：
1、发直播发送自定义消息
2、防盗链重构


版本：v3.0.1 更新时间：2017.10.24

更新内容：
1、投屏显示 (新增投屏Support)
2、自定义消息


版本：v3.0 更新时间：2017.08.18

更新内容：
1、丰富SDK数据统计
2、SDK降噪与增益
3、更改SDK设置滤镜方案及API
4、性能优化及bug修复

版本：v2.9.0 更新时间：2017.06.12

更新内容：

1、添加https支持
2、添加推流地址调度
3、添加用户信息数据统计
4、添加自定义推流数据、自定义渲染
5、修复部分bug



版本：v2.8.0 更新时间：2017.04.13

更新内容：

1、支持观看VR直播
2、升级依赖库socket.io 0.8.3 release
3、添加部分异常数据兼容


版本：v2.7.1 更新时间：2017.03.31

更新内容：

1、添加白板功能
2、添加文档画笔功能

版本：v2.7.0 更新时间：2017.03.13

更新内容：

1、业务层添加问卷功能
2、部分回调和log优化

版本：v2.6.0 更新时间：2017.03.01

更新内容：
1、业务层和UI层分离

2、UI层增加弹幕功能、表情

3、业务层增加签到、公告



版本：v2.5.4 更新时间：2016.12.30

更新内容：
1、SDK观看回放的次数、人数、时长计入微吼管理中心的数据统计

2、支持对回放发表评论和获取评论

3、支持美颜

4、支持前置摄像头发起直播

5、支持子账号结束自己的活动

6、正在推流的活动不能重复发起

7、支持MP4格式播放

版本：v2.5.0 更新时间：2016.11.01

更新内容：

1、增加子账号功能

2、增加抽奖功能

3、增加获取聊天记录

版本：v2.4.0 更新时间：2016.9.27

更新内容：

1、增加聊天问答功能

2、增加应用包名签名身份验证

3、增加用户体系

4、增加观看语音直播

5、优化已知BUG

版本：v2.3.2 更新时间：2016.8.9

更新内容：

1、优化播放集成方案


版本：v2.3.1 更新时间：2016.8.1

更新内容：

1、多分辨率切换

2、防盗链

3、多种展示方案

自助式网络直播SDK（Android版）

版本：v2.2.2 更新时间：2016.6.3

更新内容：

1、优化横竖屏切换后的全屏观看

版本：v2.2.1 更新时间：2016.5.13

更新内容：

1、新增帧率配置

版本：v2.2.0 更新时间：2016.5.6

更新内容：

1、新增文档演示

2、优化观看体验
