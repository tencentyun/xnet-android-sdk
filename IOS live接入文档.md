## 腾讯云 X-P2P Live IOS SDK 接入文档。


### 介绍

腾讯云X-P2P解决方案，可帮助用户直接使用经过大规模验证的直播、点播、文件分发服务，通过经商用验证的P2P服务大幅节省带宽成本，提供更优质的用户体验。开发者可通过SDK中简洁的接口快速同自有应用集成，实现IOS设备上的P2P加速功能。

传统CDN加速服务中，客户端向CDN发送HTTP请求获取数据。在腾讯云X-P2P服务中，SDK可以视为下载代理模块，客户端应用将HTTP请求发送至SDK，SDK从CDN或其他P2P节点获取数据，并将数据返回至上层应用。SDK通过互相分享数据降低CDN流量，并通过CDN的参与，确保了下载的可靠性。

SDK支持多实例，即支持同时开启多个直播p2p。

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
            // 将resource之前的 域名/路径 内容替换为 [XP2PModule host] 即可
 			NSString* p2pUrl = [[XP2PModule host] stringByAppendingString:@"${yourResoruce.ext}"];
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

#### 拦截SDK日志输出
```
//在sdk初始化之前设置日志输出回调，之后日志会在XP2P线程中回调onLogPrint:(NSString*)msg
[XP2PModule setLogger:id<Logger>];

```

## api
  SDK接口除初始化接口, 其余接口均由http实现, 请求格式为http:://${host}:${port}/${func}?${param}, 其中${host}:${port}为本地代理服务器
  
  以下为详细api
  
-------------------------------

-------------------------------
#### 统计
- 描述: 请求对应频道的统计数据

- 方法: GET

- 路径: /stat?channel=${yourUrl}

- 请求参数:
    |  参数名称   | 必选 | 类型 | 说明 |
    |  ----  | ----   | ---- | ----  |
    | channel  | 是 | string | 默认为url中的resource, 否则为频道的encode url |

- 返回参数: 
    |  返回码   | 说明 |
    |  ----  | ----  |
    | 200  | 查询成功 |
    | 404  | 查询失败, 频道不存在 |
    200, 返回内容为JSON
    |  参数名称  |  类型 | 说明 |
    |  ----     | ---- | ----  |
    | flow.p2pBytes | num | 对应频道p2p流量 |
    | flow.cdnBytes | num | 对应频道cdn流量 |

- 请求样例
    
    http://127.0.0.1:16080/live.p2p.com/stat?channel=xxx
    
    *注:*channel 即 http://127.0.0.1:16080/live.p2p.com/resoruce.ext 中的 resource

- 返回样例
``` json
    "{"flow":{"p2pBytes":0,"cdnBytes":0}}"
```

-------------------------------
#### 设置上下行
- 描述: 请求设置p2p上行与下行, 0为开启, 1为关闭

- 方法: GET

- 路径: /feature?download=${0or1}&upload=${0or1}
    |  参数名称   | 必选 | 类型 | 说明 |
    |  ----  | ----   | ---- | ----  |
    | download  | 是 | num | 0为关闭; 1为默认值, 开启 |
    | upload  | 是 | num | 0为关闭; 1为默认值, 开启 |

 - 请求样例
    
    http://127.0.0.1:16080/live.p2p.com/feature?download=1&upload=0
    
    *注:*一般情况下移动网络需要关闭上传

- 返回参数: JSON, 格式如下
    |  参数名称   | 必选 | 类型 | 说明 |
    |  ----  | ----   | ---- | ----  |
    | ret  | 是 | num | 0表示正常 |
    | msg  | 是 | string | 相关信息, 调试使用|
    | upload  | 是 | bool | true表示开启, false表示关闭 |
    | download  | 是 | bool | true表示开启, false表示关闭 |