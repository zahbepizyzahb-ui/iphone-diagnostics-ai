# إنشاء keystore للتوقيع
# keytool -genkey -v -keystore iphone-diagnostics.keystore -alias iphoneai -keyalg RSA -keysize 2048 -validity 10000

# ثم أضف في app/build.gradle:
# android {
#     signingConfigs {
#         release {
#             storeFile file("iphone-diagnostics.keystore")
#             storePassword "your-password"
#             keyAlias "iphoneai"
#             keyPassword "your-password"
#         }
#     }
#     buildTypes {
#         release {
#             signingConfig signingConfigs.release
#             minifyEnabled true
#             proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
#         }
#     }
# }
