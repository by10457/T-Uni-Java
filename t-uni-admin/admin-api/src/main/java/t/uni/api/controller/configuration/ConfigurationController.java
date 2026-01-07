package t.uni.api.controller.configuration;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import t.uni.common.core.result.Result;
import t.uni.common.core.result.ResultCodeEnum;
import t.uni.configuration.service.ConfigurationService;
import t.uni.core.annotation.PermissionTag;
import t.uni.domain.configuration.dto.WebConfigurationDto;
import t.uni.domain.configuration.entity.WebConfiguration;

@Tag(name = "web配置文件", description = "web配置相关接口")
@PermissionTag(permission = "config:*")
@RequestMapping("/api/config")
@RestController
@RequiredArgsConstructor
public class ConfigurationController {

    private final ConfigurationService configurationService;

    @Operation(summary = "获取web配置文件", description = "读取web配置文件并返回给前端")
    @GetMapping("public/web-config")
    public WebConfiguration webConfig() {
        return configurationService.webConfig();
    }

    @Operation(summary = "更新web配置文件", description = "更新web配置文件，重启应用失效")
    @PermissionTag(permission = "config::update")
    @PutMapping()
    public Result<Object> updateWebConfiguration(@Valid @RequestBody WebConfigurationDto dto) {
        configurationService.updateWebConfiguration(dto);
        return Result.success(ResultCodeEnum.UPDATE_SUCCESS);
    }
}
