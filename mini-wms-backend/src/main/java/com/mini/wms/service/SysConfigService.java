package com.mini.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mini.wms.dto.Result;
import com.mini.wms.entity.SysConfig;
import com.mini.wms.mapper.SysConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置服务
 */
@Service
@RequiredArgsConstructor
public class SysConfigService {

    private final SysConfigMapper sysConfigMapper;

    /**
     * 获取全部配置
     */
    public Result<?> list() {
        List<SysConfig> list = sysConfigMapper.selectList(null);
        Map<String, String> result = new HashMap<>();
        for (SysConfig config : list) {
            result.put(config.getConfigKey(), config.getConfigValue());
        }
        return Result.success(result);
    }

    /**
     * 更新配置（JSON 键值对）
     */
    public Result<?> updateConfig(String configJson) {
        // 简化处理：将配置整体存为 json
        SysConfig config = sysConfigMapper.selectOne(
                new QueryWrapper<SysConfig>().eq("config_key", "app"));
        if (config == null) {
            config = new SysConfig();
            config.setConfigKey("app");
            config.setConfigValue(configJson);
            config.setUpdatedAt(LocalDateTime.now());
            sysConfigMapper.insert(config);
        } else {
            config.setConfigValue(configJson);
            config.setUpdatedAt(LocalDateTime.now());
            sysConfigMapper.updateById(config);
        }
        return Result.success();
    }

    /**
     * 获取单个配置值
     */
    public String get(String key) {
        SysConfig config = sysConfigMapper.selectOne(
                new QueryWrapper<SysConfig>().eq("config_key", key));
        return config == null ? null : config.getConfigValue();
    }
}