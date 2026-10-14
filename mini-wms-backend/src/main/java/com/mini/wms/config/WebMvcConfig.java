package com.mini.wms.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * Web MVC 配置：处理静态资源映射、SPA 单页应用路由转发、认证权限拦截器注册
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    /**
     * 注册认证与权限拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射静态资源（前端打包产物）
        // 注意顺序：外部 public 目录（Docker 构建时注入的最新前端）优先于 jar 内置的 classpath:/static/，
        // 否则随 jar 打包的旧前端会屏蔽外部新产物，导致前端改动部署后不生效。
        registry.addResourceHandler("/**")
                .addResourceLocations(
                        "file:/app/public/",
                        "file:./public/",
                        "classpath:/static/"
                )
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);
                        // 如果请求的是已有静态文件且存在且可读，则直接返回该文件
                        if (requestedResource.exists() && requestedResource.isReadable()) {
                            return requestedResource;
                        }
                        // 如果请求以 api/ 开头，则交给 Controller 处理，不重定向到 index.html
                        if (resourcePath.startsWith("api/") || resourcePath.startsWith("/api/")) {
                            return null;
                        }
                        // 否则转发到前端 SPA 入口 index.html (支持 Vue 路由 HTML5 History 模式)
                        Resource indexResource = location.createRelative("index.html");
                        if (indexResource.exists() && indexResource.isReadable()) {
                            return indexResource;
                        }
                        return null;
                    }
                });
    }
}
