//
//  Config.h
//  rn-contact-tracing
//
//  Created by Ran Greenberg on 23/04/2020.
//

#import <Foundation/Foundation.h>


NS_ASSUME_NONNULL_BEGIN

@interface Config : NSObject

+(void)SetConfig:(NSDictionary*) configDict;
+(NSDictionary*)GetConfig;

@end

NS_ASSUME_NONNULL_END
