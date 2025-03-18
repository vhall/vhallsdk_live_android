
names=(
'library/vh-saas-interactive/6.20.15/vh-saas-interactive-6.20.15.aar'
'library/vh-saas-sdk/6.20.15/vh-saas-sdk-6.20.15.aar'
'library/vhallyun-framework/2.7.0/vhallyun-framework-2.7.0.aar'
'base/http-client/1.3.0/http-client-1.3.0.aar'
'base/logmanager/2.8.1/logmanager-2.8.1.aar'
'base/message-client/1.6.2/message-client-1.6.2.aar'
'base/vhall-beautify-lib/1.1.6/vhall-beautify-lib-1.1.6.aar'
'library/vhallyun-ims/2.6.0/vhallyun-ims-2.6.0.aar'
'library/vhallyun-vod/2.7.0/vhallyun-vod-2.7.0.aar'
'library/vhallyun-ops/2.8.0/vhallyun-ops-2.8.0.aar'
'base/vhall-module-document/2.3.1/vhall-module-document-2.3.1.aar'
'library/vhallyun-ilss/3.1.0/vhallyun-ilss-3.1.0.aar'
'base/vhall-module-rtc/2.4.2.3/vhall-module-rtc-2.4.2.3.aar'
'base/vhall-module-rtc-so/3.5.0.2.1/vhall-module-rtc-so-3.5.0.2.1.aar'
'library/vhallyun-lss/2.11.0/vhallyun-lss-2.11.0.aar'
'base/vhall-module-core/2.7.2/vhall-module-core-2.7.2.aar'
'base/exoplayer/2.9.1.1/exoplayer-2.9.1.1.aar'
'base/exoplayer-core/2.9.1/exoplayer-core-2.9.1.aar'
'base/exoplayer-dash/2.9.1.1/exoplayer-dash-2.9.1.1.aar'
'base/exoplayer-hls/2.9.1.1/exoplayer-hls-2.9.1.1.aar'
'base/exoplayer-smoothstreaming/2.9.1.1/exoplayer-smoothstreaming-2.9.1.1.aar'
'base/exoplayer-ui/2.9.1.1/exoplayer-ui-2.9.1.1.aar'
'base/vhall-dispatch-native/1.3.0/vhall-dispatch-native-1.3.0.aar'
'base/vhall-dispatch-native-so/1.1.4/vhall-dispatch-native-so-1.1.4.aar'
'base/vhall-live-native/1.2.3/vhall-live-native-1.2.3.aar'
'base/vhall-live-native-so/1.1.2/vhall-live-native-so-1.1.2.aar')

url="https://repo1.maven.org/maven2/com/github/vhall/android/"
echo 'SDK开始下载'
for name in ${names[*]};do
  echo $name
  curl -O $url$name
done

echo 'SDK下载结束'



support_names=(
'https://repo1.maven.org/maven2/com/github/vhall/android/library/vh-saas-sdk-support/2.0.1/vh-saas-sdk-support-2.0.1.aar'
'https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-server/8.1.8.v20121106/jetty-server-8.1.8.v20121106.jar'
'https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-http/8.1.8.v20121106/jetty-http-8.1.8.v20121106.jar'
'https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-io/8.1.8.v20121106/jetty-io-8.1.8.v20121106.jar'
'https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-util/8.1.8.v20121106/jetty-util-8.1.8.v20121106.jar'
'https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-jmx/8.1.8.v20121106/jetty-jmx-8.1.8.v20121106.jar'
'https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-security/8.1.8.v20121106/jetty-security-8.1.8.v20121106.jar'
'https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-client/8.1.8.v20121106/jetty-client-8.1.8.v20121106.jar'
'https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-servlet/8.1.8.v20121106/jetty-servlet-8.1.8.v20121106.jar'
'https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-continuation/8.1.8.v20121106/jetty-continuation-8.1.8.v20121106.jar'
'http://4thline.org/m2/org/fourthline/cling/cling-core/2.1.1/cling-core-2.1.1.jar'
'http://4thline.org/m2/org/seamless/seamless-util/1.1.1/seamless-util-1.1.1.jar'
'http://4thline.org/m2/org/seamless/seamless-http/1.1.1/seamless-http-1.1.1.jar'
'http://4thline.org/m2/org/seamless/seamless-xml/1.1.1/seamless-xml-1.1.1.jar'
'http://4thline.org/m2/org/fourthline/cling/cling-support/2.1.1/cling-support-2.1.1.jar'
'http://4thline.org/m2/org/seamless/seamless-swing/1.1.1/seamless-swing-1.1.1.jar'
)
echo '投屏开始下载'
for name in ${support_names[*]};do
  echo $name
  curl -O $name
done

echo '投屏下载结束'



beautify_names=(
'https://repo1.maven.org/maven2/com/github/vhall/android/base/vhall-beautify-faceunity/1.1.6/vhall-beautify-faceunity-1.1.6.aar'
)
echo '高级美颜下载'
for name in ${beautify_names[*]};do
  echo $name
  curl -O $name
done

echo '高级美颜结束'

