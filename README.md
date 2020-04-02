# 腾讯云 X-P2P Live Android SDK 接入文档


## 介绍

腾讯云X-P2P解决方案，可帮助用户直接使用经过大规模验证的直播、点播、文件分发服务，通过经商用验证的P2P服务大幅节省带宽成本，提供更优质的用户体验。开发者可通过SDK中简洁的接口快速同自有应用集成，实现Android设备上的P2P加速功能。

传统CDN加速服务中，客户端向CDN发送HTTP请求获取数据。在腾讯云X-P2P服务中，SDK可以视为下载代理模块，客户端应用将HTTP请求发送至SDK，SDK从CDN或其他P2P节点获取数据，并将数据返回至上层应用。SDK通过互相分享数据降低CDN流量，并通过CDN的参与，确保了下载的可靠性。

SDK支持多实例，即支持同时开启多个不同资源的直播p2p

## 接入

#### 添加依赖
1.将libp2pimpl-release.aar拷贝到libs目录下

2.在应用模块的build.gradle中加入
``` gradle
dependencies {
    implementation fileTree(include: ['*.jar'], dir: 'libs')
    implementation (name: 'libp2pimpl-release', ext: 'aar')
}
```
#### 支持的 ABI

'armeabi-v7a'

#### 具体步骤

- 在App启动之初初始化XP2PModule

首先需要初始化p2p sdk，最好在app启动后就作初始化。

``` java
    // 初始化appId等关键客户信息
    final String APP_ID = "$your_app_id";
    final String APP_KEY = "$your_app_key";
    final String APP_SECRET = "$your_app_secret";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 返回值ok可用于统计p2p成功与失败
        // 无论成功与失败，都可以使用XNet.HTTP_Proxy与XNet.HTTPS_PROXY
        // 失败时，XNet.HTTP_PROXY是""
        // 成功时，XNet.HTTP_PROXY="127.0.0.1:16080/live.p2p.com/
        bool ok = XNet.create(this, APP_ID, APP_KEY, APP_SECRET);

        //开启调试日志(发布可关闭)
        XNet.enableDebug();
    }
```

- 播放控制（start/stop）

启动直播p2p：

``` java
    // 例如：http://domain/path/to/resource.flv?params=xxx
    // 变成：http://{XNet.http_proxy}domain/path/to/resource.flv?params=xxx
    // 要改的仅仅是在://后插入XNET.HTTP_PROXY
    String p2pUrl = "http://" + XNet.HTTP_PROXY + "${resource}.${ext}"
    // 通过获取的url, 播放器直接执行请求
    mediaPlayer.setDataSource(context, Uri.parse(p2pUrl), headers);
    mediaPlayer.prepareAsync();

    // 相关资源随着http请求关闭直接释放，无需再做任何处理
    //注意：该http请求可能会返回302
```

由于后台监听限制的问题，resume的情况下需要调用resume接口

``` java
    // 返回值ok代表着是否恢复成功
    bool ok = XNet.resume();
```

## api
  SDK接口除初始化接口, 其余接口均由http实现, 请求格式为http:://${XNet.HTTP_PROXY}${func}?${param}

注意，在初始化返回值为false时，或者XNet.HTTP_PROXY为""时，意味着没走p2p，不应再向其发起HTTP请求，否则会遇到502等错误，也可认为没P2P的标志
  
  以下为详细api

-------------------------------
#### 统计
- 描述: 请求对应频道的统计数据

- 方法: GET

- 路径: /stat?channel=${resource}

- 请求参数:
    |  参数名称   | 必选 | 类型 | 说明 |
    |  ----  | ----   | ---- | ----  |
    | channel  | 是 | string | 默认为url中的resource, 否则为频道请求中的channel值 |

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
    | upload  | 是 | bool | 1表示开启, 0表示关闭 |
    | download  | 是 | bool | 1表示开启, 0表示关闭 |

- 返回样例
``` json
    "{"ret":0, "msg":"ok", "download":0,"upload":0}}"
```