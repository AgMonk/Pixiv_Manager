package com.gin.pixiv_manager.sys.swagger;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * @author bx002
 */
@Configuration
@EnableKnife4j
public class Knife4jConfiguration {

    @Bean(value = "userApi")
    public Docket userApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfoBuilder()
                        .title("用户/角色/权限接口")
                        .description("接口文档描述")
                        .termsOfServiceUrl("https//www.baidu.com")
                        .contact(new Contact("黄俊钢","https://www.baidu.com","hjg719@139.com"))
                        .version("1.0")
                        .build())
                //分组名称
                .groupName("用户/角色/权限")
                .select()
                //这里指定Controller扫描包路径
                .apis(RequestHandlerSelectors.basePackage("com.gin"))
                .paths(PathSelectors.regex("/sys/.+"))
                .build();
    }




}
