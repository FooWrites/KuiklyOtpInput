#import "KRTextFieldView+OtpAutofill.h"

@implementation KRTextFieldView (OtpAutofill)

// 视图复用时 Kuikly 以 nil 重置已设置过的属性
- (void)setCss_otpAutofill:(NSString *)css_otpAutofill {
    BOOL enabled = css_otpAutofill.length > 0 && ![css_otpAutofill isEqualToString:@"off"];
    self.textContentType = enabled ? UITextContentTypeOneTimeCode : nil;
}

@end
