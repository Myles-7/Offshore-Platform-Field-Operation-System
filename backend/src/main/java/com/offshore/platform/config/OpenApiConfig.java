package com.offshore.platform.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI offshorePlatformOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("海上平台现场作业管理系统后端接口")
                .version("0.0.1")
                .description("海上平台现场作业管理系统 REST API 文档"));
    }

    @Bean
    public GroupedOpenApi authApi() {
        return group("auth 用户认证与权限",
                "/api/auth/**",
                "/api/admin/users/**",
                "/api/admin/roles/**",
                "/api/admin/permissions/**");
    }

    @Bean
    public GroupedOpenApi adminWorkOrderApi() {
        return group("admin-work-order PC后台工单管理",
                "/api/admin/projects/**",
                "/api/admin/work-orders/**",
                "/api/admin/work-order-templates/**",
                "/api/admin/work-records/**");
    }

    @Bean
    public GroupedOpenApi mobileWorkOrderApi() {
        return group("mobile-work-order 移动端工单作业", "/api/mobile/work-orders/**", "/api/health");
    }

    @Bean
    public GroupedOpenApi fileApi() {
        return group("file 文件附件管理", "/api/files/**");
    }

    @Bean
    public GroupedOpenApi acceptanceApi() {
        return group("acceptance 电子签名与PDF验收", "/api/admin/acceptance/**", "/api/mobile/acceptance/**");
    }

    @Bean
    public GroupedOpenApi materialApi() {
        return group("material 物料追溯", "/api/admin/materials/**", "/api/mobile/materials/**");
    }

    @Bean
    public GroupedOpenApi qualificationApi() {
        return group("qualification 人员资质", "/api/admin/qualifications/**", "/api/mobile/qualifications/**");
    }

    @Bean
    public GroupedOpenApi syncApi() {
        return group("sync 离线同步", "/api/sync/**", "/api/admin/sync/**");
    }

    @Bean
    public GroupedOpenApi aiApi() {
        return group("ai AI辅助验收", "/api/ai/**", "/api/admin/ai/**");
    }

    @Bean
    public GroupedOpenApi dashboardApi() {
        return group("dashboard 经营看板", "/api/admin/dashboard/**", "/api/admin/reports/**");
    }

    @Bean
    public GroupedOpenApi logApi() {
        return group("log 操作日志", "/api/admin/logs/**");
    }

    private GroupedOpenApi group(String groupName, String... paths) {
        return GroupedOpenApi.builder()
                .group(groupName)
                .pathsToMatch(paths)
                .build();
    }
}
