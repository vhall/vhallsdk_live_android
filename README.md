自助式网络直播SDK（Android版）

一、集成方式：

APP直接依赖uilib库即可，界面可在UI库中自定义
具体API调用方式请参考uilib，或官方文档
#### 注释
uilib里面的aar替换为maven依赖
具体使用 参考demo
项目的 build.gradle里面添加

```
allprojects {
 maven{
       mavenCentral()
       //since 6.3.0
       //使用高级美颜添加 不用可以不写
       maven {url 'http://maven.faceunity.com/repository/maven-public/'}
       }
}
```

 在uilib里面添加 具体使用依赖

```
 //SaaSSDK
    api 'com.github.vhall.android.library:vh-saas-sdk:6.19.3'
    api 'com.github.vhall.android.library:vh-saas-interactive:6.19.3'
    //投屏相关
    api 'com.github.vhall.android.library:vh-saas-sdk-support:2.0.1'
    
    
    //美颜相关 使用高级美颜添加 不用可以不写
   api 'com.github.vhall.android.base:vhall-beautify-faceunity:1.1.7'

    //美颜demoUI 建议自己按照自己需求实现 使用高级美颜添加 不用可以不写
    api 'com.github.vhall.android.library:vhall-beautify-kit-support:1.1.2'

    
```

#### Demo 运行

1.  微吼控制台获取 Appkey、appSecretKey， 签名设置页可以获得控制台配置具体参数  对接文档： https://saas-doc.vhall.com/opendocs/show/1224
2.  Demo 的 com.vhall.live.vhall.EvConfigProvider 中填写 Appkey、appSecretKey



二、版本更新信息：
版本：v6.21.1  更新时间 2025.03.25

1. 【优化】升级美颜能力，增加多种特效

版本：v6.20.15  更新时间 2025.03.18

1. 【优化】优化底层消息模块，支持大并发活动
2. 【优化】优化其它已知问题


版本：v6.19.3  更新时间 2024.03.15

1. 【新增】支持跑马灯功能


版本：v6.19.2  更新时间 2024.02.20

1. 【新增】Demo 新增弹幕实现示例
2. 【优化】修改依赖加密库版本版本

版本：v6.19.1  更新时间 2024.01.09

1. 【优化】SDK targetSdkVersion 升级为30

版本：v6.19.0  更新时间 2023.09.28

1. 【新增】SDK新增优惠券领用、核销功能，配合商品订单支付使用

版本：v6.18.0  更新时间 2023.08.31

1. 【新增】SDK新增商品订单支付能力，支持微信、支付宝支付；
2. 【优化】SDK新增SHA256、SM3签名加密方式；

版本：v6.17.1  更新时间 2023.07.21

1. 【新增】推屏卡片互动功能，提升直播间营销互动能力，对接说明见 推屏卡片 ；
2. 【优化】解决底层包引用冲突问题
3. 【优化】优化弱网环境下，互动连麦弱网问题

版本：v6.16.0  更新时间 2023.06.16

1. 【新增】文件下载功能；
2. 【新增】删除聊天信息功能；


版本：v6.15.0  更新时间 2023.06.07

1. 【新增】SDK新增播放指定回放或视频能力，支持精彩片段播放场景；
2. 【新增】新增播放器截图功能；
3. 【优化】支持播放1080P视频；
4. 【优化】修复了外接蓝牙设备互动连麦的兼容性问题；

版本：v6.14.0  更新时间 2023.05.05

1. 【优化】优化SDK对弱网、断网及异常中断等异常场景的兼容性


版本：v6.13.0  更新时间 2023.03.29

1. 【新增】SDK新增显示章节、视频打点能力; 新增查看中奖名单等抽奖相关接口
2. 【优化】优化抽奖互动能力，新增支持口令等特殊抽奖条件 ; 观看回放初始化播放位置的功能 ;优化了登录和其他已知问题
3. 【修复】文档显示问题

 版本：v6.12.0  更新时间 2023.03.07

1.【新增】新增支持验证SaaS观看限制（密码、白名单）
2.【优化】对接直播发起端发起倒计时公告
3.【优化】优化直播播放器暂停后的交互效果
4.【注意】从该版本开始，SDK将不再支持K值验证

 版本：v6.11.0  更新时间 2022.12.23


1. 【新增】新增直播中 [快问快答](https://saas-doc.vhall.com/opendocs/show/1417)功能，提升直播的转化效果；
2. 【优化】优化互动连麦、文档水印等已知问题。
3.  优化sdk中一些已知问题|

 版本：v6.10.0  更新时间 2022.11.24

1. 【新增】 [暖场视频功能](https://saas-doc.vhall.com/opendocs/show/1391)
2. 【新增】回放 [获取回放禁言状态](https://saas-doc.vhall.com/opendocs/show/1395#获取活动配置开关)  和  [章节打点](https://saas-doc.vhall.com/opendocs/show/1237#获取章节打点信息) 
3. 优化sdk中一些已知问题




 版本：v6.9.0  更新时间 2022.10.28

1. 【新增】 直播支持新的「连麦演示」布局
2. 【新增】支持文档融屏 [活动详情增加融屏开关字段](/opendocs/show/1265)  和  [互动增加融屏开关 文档流回调](/opendocs/show/1230) 
3. 优化sdk中一些已知问题


  版本：v6.8.0  更新时间 2022.9.23

1. 【新增】 彩排权限 [live_rehearsal](https://saas-doc.vhall.com/docs/show/1630)<br>
2. 【新增】主持人发起互动混流支持pc配置的图片和背景（sdk内部处理，不需要用户处理）<br>
3.  优化sdk中一些已知问题<br>

  版本：v6.7.0  更新时间 2022.9.6

1. 【新增】 [点赞](https://saas-doc.vhall.com/docs/show/1573)
2. 【新增】 [计时器](https://saas-doc.vhall.com/docs/show/1623)
3. 【新增】 [礼物](https://saas-doc.vhall.com/docs/show/1624)
4. 【新增】 [直播间彩排](https://saas-doc.vhall.com/docs/show/1633)
5. 【新增】 [虚拟人数](https://saas-doc.vhall.com/docs/show/1634)
6.  活动详情新增直播主持人信息，直播标题等
7.  demo新增极简观看模式
8.  优化sdk中一些已知问题


 版本：v6.6.0 更新时间 2022.8.9 <br>

1. 【新增】公告列表 公告 <br>
2. 【新增】观看直播增加k_id验证 新增方法 <br>
3. 【新增】聊天记录增加分页消息锚点 ，防止重复数据 新增方法 <br>

 版本：v6.5.1  更新时间 2022.7.8 <br>

1. 增加观众参与视频轮巡 <br>
2. 优化播放器自动播放   [播放器自动播放](https://saas-doc.vhall.com/docs/show/1210 "播放器自动播放")<br>
3. 互动增加清晰度根据互动人数自动变化 <br>
4. demo增加文档横竖屏切换<br>

版本：v6.4.1  更新时间 2022.6.14 <br>

1. 抽奖增加中奖列表，领奖开关    使用步骤 [demo中新功能对应使用文档](https://saas-doc.vhall.com/docs/show/1489 "demo中新功能对应使用文档")
[消息说明](https://saas-doc.vhall.com/docs/show/1214 "消息说明") <br>




版本：v6.4.0  更新时间 2022.5.31 <br>

1. 新增支持云导播活动的发起和推流 [使用指南](https://saas-doc.vhall.com/docs/show/1507 "使用指南")<br>

2. 问卷&问答支持修改显示名称、批量删除等功能 [使用指南](https://saas-doc.vhall.com/docs/show/1507 "使用指南")<br>

3. 支持设置图片或颜色为播放器背景 [使用指南](https://saas-doc.vhall.com/docs/show/1507 "使用指南") <br>

4. 修改嘉宾和观众的设备检验流程

5. SDK不再采集IMEI、IMSI、设备序列号、安卓ID等安全信息

6. 修改已知SDK提示问题


版本：v6.3.4  更新时间 2022.5.13

1. 开放动态过滤私聊 使用步骤 [demo中新功能对应使用文档](https://saas-doc.vhall.com/docs/show/1445 "demo中新功能对应使用文档")<br>

2. 增加新的消息类型 互动流设置混流大画面 EVENT_VRTC_BIG_SCREEN_SET ，开放其他没使用的消息 <br>

3. 优化已知bug <br>


版本：v6.3.3 更新时间 2022.5.6

1. 活动详情增加 List<String> permission 嘉宾权限列表  [房间新增字段信息说明](https://saas-doc.vhall.com/docs/show/1217 "房间新增字段信息说明")
msginfo 增加inviter_account_id 字段[新增消息字段信息说明](https://saas-doc.vhall.com/docs/show/1214 "新增消息字段信息说明")<br>

2. 增加嘉宾作为主讲人上麦 使用步骤[demo中新功能对应使用文档](https://saas-doc.vhall.com/docs/show/1445 "demo中新功能对应使用文档") <br>

3. 优化已知bug <br>


版本：v6.3.2 更新时间 2022.4.29

1. 增加观看协议-回放和看直播前调用<br>
2. 增加观看协议 使用步骤[demo中新功能对应使用文档](https://saas-doc.vhall.com/docs/show/1445 "demo中新功能对应使用文档") <br>
3. 优化已知bug <br>


版本：v6.3.1 更新时间 2022.3.30

1. 支持观看回放，无延迟直播，互动时 相同账号踢出功能<br>
2. 优化已知bug <br>


版本：v6.3.0 更新时间 2022.3.22 <br>

1.支持高级美颜 <br>
   (1): 美颜功能优化。新增红润、大眼、瘦脸、锐化、白牙、亮眼等美颜功能；<br>
   (2):   新增滤镜。支持接入自然款、粉嫩款、白亮款等6种滤镜；<br>
2.修改直播间聊天回复信息格式，统一成和历史聊天记录一致，不再拼接。<br>
3.优化底层服务  <br>
4.修复助理文档翻页主持人不同步问题 <br>

版本：v6.2.3 更新时间 2022.2.21<br>
1.底层包名修改并且需要修改混淆相关，详情见大版本更新 6.2.3  <br>
2.支持新功能 -修改主持人、嘉宾、助理的角色名称 <br>
3.主要更新预告，我们将于半年内升级开发sdk的版本，并转向Androidx <br>


版本：v6.2.2 更新时间 2021.12.14 <br>
1.完善sdk 关键词过滤功能 <br>
2.优化已知bug <br>


版本：v6.2.1 更新时间 2021.11.11 <br>

1.sdk 和 demo 增加无延迟直播 <br>
2.互动sdk 增加 直播sdk方法 <br>
3.活动信息无延迟直播字端 <br>
4.新增提示：观众没有上麦不建议直接进入互动（非无延迟）房间内，否则会占用房间名额，导致嘉宾助理进不去。 <br>
5.优化已知bug <br>


版本：v6.2.0 更新时间 2021.10.13 <br>

1.嘉宾支持带头像进入 <br>
2.调整用户设置互动分辨率 <br>
3.活动信息增加部分字端 <br>
4.demo支持多人互动上麦 <br>
5.优化已知bug <br>


版本：v6.1.4 更新时间 2021.09.06 <br>

1. 优化已知bug <br>

版本：v6.1.3 更新时间 2021.08.26 <br>

1.更改 setDeviceStatus的方法名为switchDevice和之前保持一致  <br>
2.兼容服务器接口升级  <br>
3.修改问答判断逻辑，只能看自己和公开的问答  <br>
4.优化已知bug  <br>

版本6.1.2 更新时间 2021.07.27  <br>

1.新增设置RSA设置私钥接口  <br>
2.优化直播关闭交互方式  <br>
3.优化已知bug <br>

版本6.1.0 更新时间 2021.06.30  <br>

1.增加主播/嘉宾互动  <br>
2.登陆token失效优化  <br>
3.bug修复  <br>
4.版本向下兼容  <br>
5.更多内容参考[官网](https://saas-doc.vhall.com/docs/show/1204) <br>

版本5.0.0 更新时间 2020.10.30  <br>
1.优化用户使用 h5和flash统一使用方法  <br>
2.优化日志上报  <br>
3.添加h5文档显示 具体可以参考demo实现  <br>
4.修改bug  <br> 

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
