//
//  XNet.h
//  RTMPiOSDemo
//
//  Created by yanyin on 2019/10/26.
//  Copyright © 2019 tencent. All rights reserved.
//

#ifndef XNet_h
#define XNet_h

#import <Foundation/Foundation.h>

typedef NS_ENUM(NSInteger, LogLevel) {
    NONE,           //关闭日志
    LOG_GENERALLY,  //开启部分日志
    LOG_ALL         //开启全部日志
};

@protocol Logger <NSObject>
@required
- (void)onLogPrint:(NSString*)msg;
@end

__attribute__((visibility("default")))
@interface XNet : NSObject
+ (int)initWith:(NSString*)appId appKey:(NSString*)appKey appSecretKey:(NSString*)appSecretKey;
+ (NSString*)version;
+ (void)enableDebug:(LogLevel)leve;
+ (void)setLogger:(id<Logger>)delegate;
+ (int)resume;
+ (NSString*)host;
+ (NSString*)proxyOf:(NSString*)domain;
@end
#endif /* XNet_h */
