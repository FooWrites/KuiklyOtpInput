#import "KRTextFieldView.h"

NS_ASSUME_NONNULL_BEGIN

/// 处理 OtpInput 设置在原生输入框上的 otpAutofill 属性：
/// 设置 textContentType 为 UITextContentTypeOneTimeCode，键盘上方会出现短信验证码建议。
@interface KRTextFieldView (OtpAutofill)

@end

NS_ASSUME_NONNULL_END
