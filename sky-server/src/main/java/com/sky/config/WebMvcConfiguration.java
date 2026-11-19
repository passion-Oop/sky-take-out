package com.sky.config;

import com.sky.interceptor.JwtTokenAdminInterceptor;
import com.sky.interceptor.JwtTokenUserInterceptor;
import com.sky.json.JacksonObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 配置类，注册web层相关组件
 */
@Configuration
@Slf4j
public class  WebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;

    /**
     * 注册自定义拦截器
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/employee/login");

        // 用户端拦截器：校验令牌后把userId放入BaseContext，购物车、地址簿、订单等接口依赖它
        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/user/**")
                // 登录接口与无需登录即可访问的公开查询接口放行
                .excludePathPatterns(
                        "/user/user/login",
                        "/user/shop/status",
                        "/user/category/list",
                        "/user/dish/list",
                        "/user/setmeal/list",
                        "/user/setmeal/dish/*"
                );
    }

    /**
     * 通过springdoc + knife4j生成接口文档（OpenAPI 3）
     * 替代原先springfox的Docket
     */
    @Bean
    public OpenAPI skyOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("苍穹外卖项目接口文档")
                        .version("2.0")
                        .description("苍穹外卖项目接口文档")
                        .contact(new Contact().name("sky"))
                        .license(new License().name("Apache 2.0")));
    }

    /**
     * 接口文档分组，等价于原先的 RequestHandlerSelectors.basePackage("com.sky.controller")
     */
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("管理端")
                .packagesToScan("com.sky.controller")
                .build();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开始注册静态资源映射...");
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 不要 new 一个转换器 addFirst：自定义 Jackson 转换器排在 ByteArray 转换器之前时，
        // springdoc 的 /v3/api-docs 返回的 byte[] 会被当成普通对象序列化成 Base64 字符串，
        // 导致 Knife4j 前端解析不到 OpenAPI JSON，报“文档请求异常”。
        // 这里改成直接替换 Spring Boot 默认 Jackson 转换器使用的 ObjectMapper，保持原有顺序。
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter jacksonConverter) {
                jacksonConverter.setObjectMapper(new JacksonObjectMapper());
                break;
            }
        }
    }
}
