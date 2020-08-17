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

@protocol Logger <NSObject>
@required
- (void)onLogPrint:(NSString*)msg;
@end

__attribute__((visibility("default")))
@interface XNet : NSObject
+ (int)initWith:(NSString*)appId appKey:(NSString*)appKey appSecretKey:(NSString*)appSecretKey;
+ (NSString*)version;
+ (void)enableDebug;
+ (void)disableDebug;
+ (void)setLogger:(id<Logger>)delegate;
+ (int)resume;
+ (NSString*)host;
+ (NSString*)proxyOf:(NSString*)domain;
+ (void)alias:(NSString*)host of:(NSString*)name;
@end
#endif /* XNet_h */
