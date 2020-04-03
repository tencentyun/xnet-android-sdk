## 腾讯云 X-P2P Vod IOS SDK 接入文档。


### 介绍

腾讯云X-P2P解决方案，可帮助用户直接使用经过大规模验证的直播、点播、文件分发服务，通过经商用验证的P2P服务大幅节省带宽成本，提供更优质的用户体验。开发者可通过SDK中简洁的接口快速同自有应用集成，实现IOS设备上的P2P加速功能。

传统CDN加速服务中，客户端向CDN发送HTTP请求获取数据。在腾讯云X-P2P服务中，SDK可以视为下载代理模块，客户端应用将HTTP请求发送至SDK，SDK从CDN或其他P2P节点获取数据，并将数据返回至上层应用。SDK通过互相分享数据降低CDN流量，并通过CDN的参与，确保了下载的可靠性。

SDK支持多实例，即支持同时开启多个点播p2p。

### 接入SDK

#### 腾讯云对接人员会提供iOS项目的Bundle identifier，并索取App ID、App Key、App Secret Key，如以下形式：

        Bundle identifier：com.qcloud.helloworld
        NSString *appID = @"5919174f79883b4648a90bdd";
        NSString *key = @"3qRcwO0Zn1Gm8t2O";
        NSString *secret = @"Ayg29EDt1AbCXJ9t6HoQNbZUf6cPuV5J";

#### ios当前支持的架构

armv7 armv7s arm64

#### 具体步骤

- 解压解压TencentXP2P.zip并得到TencentXP2P.framework，并在项目中引用

- 在App启动时初始化XP2PModule

首先需要初始化p2p sdk，最好在app启动后就作初始化。

```
		// Example: 程序的入口AppDelegate.m
 		#import "AppDelegate.h"
 		#import <TencentXP2P/TencentXP2P.h>
 		@implementation AppDelegate
 		- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
 		{
 			// do something other...

            //下面的接口调用可以开启SDK log的打印，不调用该接口，log打印默认不开启
            [XP2PModule enableDebug:LOG_ALL]

            //拦截SDK输出日志
            [XP2PModule setLogger:self];

 			// 初始化P2P
 			NSString *appID = @"";
 			NSString *key = @"";
 			NSString *secret = @"";
 			[XP2PModule init:appID appKey:key appSecretKey:secret];

 			return YES;
 		}

        - (void)onLogPrint:(NSString*)msg {
            //这里能够收到SDK输出的日志
        }

 		@end

```

- 加载一个频道

```
		- (int)startPlay:(NSString*)url
		{
 			NSString* p2pUrl = [XP2PModule getVodUrl:url];
			// 直接播放此url即可
			[_player startPlay:p2pUrl];
    		return EXIT_SUCCESS;
		}
```

- 卸载一个频道

```
		- (int)stopPlay
		{
			// 播放器链接断开以后 SDK内部会自动释放频道相关的资源, 直接关闭播放器即可
			[_player stopPlay];
			return EXIT_SUCCESS;
		}
```

- 暂停频道

```
		- (int)pause:(NSString*)url
		{
			[XP2PModule pause:url];
			return EXIT_SUCCESS;
		}
```

- 恢复频道

```
		- (int)resume:(NSString*)url
		{
			[XP2PModule resume:url];
			return EXIT_SUCCESS;
		}
```

- 数据统计方法
    提供CDN和P2P的流量消耗获取，因为是累积统计，在获取后需要重置该统计，客户端可在EventDelegate的onEvent中监听STATISTICS事件获取每次刷新的数据

```
		NSString* statStr = [XP2PModule getStat:url];
		if (statStr != nil) {
			// 解析对应流的cdn流量和p2p流量 flow.cdnBytes & flow.p2pBytes
		}
```

#### 以下是一使用实例，请参考

```
#import <TencentXP2P/TencentXP2P.h>

@interface TXP2PLivePlayer()<EventDelegate>
@end

@implementation TXP2PLivePlayer
{
	
}

- (int)startPlay:(NSString*)url {
    
    NSString* p2pUrl = [XP2PModule getLiveUrl:url];
	// 直接播放此url即可
	[_player startPlay:p2pUrl];
    return 0;
}

- (int)stopPlay
{			
	// 播放器链接断开以后 SDK内部会自动释放频道相关的资源, 直接关闭播放器即可
	[_player stopPlay];
    return [super  stopPlay];
}

- (int)pause:(NSString*)url
{
	[XP2PModule pause:url];
	return EXIT_SUCCESS;
}

- (int)resume:(NSString*)url
{
	[XP2PModule resume:url];
	return EXIT_SUCCESS;
}
@end
```

#### 拦截SDK日志输出
```
//在sdk初始化之前设置日志输出回调，之后日志会在XP2P线程中回调onLogPrint:(NSString*)msg
[XP2PModule setLogger:id<Logger>];

```

#### 网络状态变更
```
	//在网络状态变化时通过该接口通知到SDK
	[XP2PModule changeNetworkTo:NetState];

```
