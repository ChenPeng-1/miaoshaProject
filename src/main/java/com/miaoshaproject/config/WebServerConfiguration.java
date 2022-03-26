package com.miaoshaproject.config;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;
//为了提高性能，除了通过application.properties中提供的可以修改的参数（队列长度，最大连接数等等），此处是为了修改keepalive相关参数
@Component
public class WebServerConfiguration implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {
    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        //使用对应工厂类提供的接口定制化我们的Tomcat connector
        ((TomcatServletWebServerFactory)factory).addConnectorCustomizers(new TomcatConnectorCustomizer() {
            @Override
            public void customize(Connector connector) {
                Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
                //定制化keepalivetimeout设置30秒内没有请求则自动断开keepalive连接
                protocol.setKeepAliveTimeout(30000);
                //当客户端发送超过一万个请求则自动断开keepalive连接
                protocol.setMaxKeepAliveRequests(10000);


            }
        });
    }
}
