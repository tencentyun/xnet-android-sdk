//
//  XNet.m
//  RTMPiOSDemo
//
//  Created by yanyin on 2019/10/26.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "XNet.h"
#import <Foundation/Foundation.h>
#import <sys/signal.h>
#import "TencentXP2P/XP2PService.h"

__weak static id<Logger> _loggger = nil;

@implementation XNet

static NSString* host = @"";

+ (int)initWith:(NSString*)appId appKey:(NSString*)appKey appSecretKey:(NSString*)appSecretKey {
    NSLog(@"[TencentXP2P] [qcloud] start init TencentXP2P SDK");
    signal(SIGPIPE, SIG_IGN);

    NSDictionary* infoDictionary = [[NSBundle mainBundle] infoDictionary];
    NSString* cacheDirectory = [NSSearchPathForDirectoriesInDomains(
        NSCachesDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    NSString* packageName = [infoDictionary objectForKey:@"CFBundleIdentifier"];

    XP2PService::init([appId UTF8String], [appKey UTF8String], [appSecretKey UTF8String],
                      [packageName UTF8String], [cacheDirectory UTF8String]);

    host = [[NSString alloc] initWithCString:(const char*)XP2PService::host().c_str()
                                    encoding:NSASCIIStringEncoding];
    return EXIT_SUCCESS;
}

+ (NSString*)version {
    return [[NSString alloc] initWithCString:(const char*)XP2PService::version().c_str()
                                    encoding:NSASCIIStringEncoding];
}

+ (void)enableDebug:(LogLevel)leve {
    if (leve == LOG_ALL) {
        XP2PService::enableDebug();
    } else if (leve == NONE) {
        XP2PService::disableDebug();
    } else {
        XP2PService::enalbeNormalLog();
    }
}

static void defaultLog(int prio, const char* msg) {
    if ([_loggger conformsToProtocol:@protocol(Logger)] &&
        [_loggger respondsToSelector:@selector(onLogPrint:)]) {
        NSString* message = [[NSString alloc] initWithCString:msg encoding:NSASCIIStringEncoding];
        [_loggger onLogPrint:message];
    }
}

+ (void)setLogger:(id<Logger>)delegate {
    _loggger = delegate;
    if (_loggger != nil) {
        XP2PService::setLogger(defaultLog);
    }
}

+ (int)resume {
    XP2PService::resume();
    host = [[NSString alloc] initWithCString:(const char*)XP2PService::host().c_str()
                                    encoding:NSASCIIStringEncoding];
    return EXIT_SUCCESS;
}

+ (NSString*)host {
    return host;
}

+ (NSString*)proxyOf:(NSString*)domain {
    NSString* proxy = @"";
    if ([host length] > 0) {
        proxy = [[NSString alloc] initWithFormat:@"%@/%@/", host, domain];
    }
    return proxy;
}

@end
