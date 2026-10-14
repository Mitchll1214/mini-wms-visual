package com.mini.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mini.wms.dto.Result;
import com.mini.wms.entity.Bin;
import com.mini.wms.mapper.BinMapper;
import com.mini.wms.service.BinService;
import com.mini.wms.util.QrCodeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BinServiceImpl extends ServiceImpl<BinMapper, Bin> implements BinService {

    private final BinMapper binMapper;
    private final QrCodeUtil qrCodeUtil;

    @Override
    public Result<?> listByShelf(Integer shelfId) {
        return Result.success(binMapper.selectByShelfId(shelfId));
    }

    @Override
    public Result<?> tree() {
        return Result.success(binMapper.selectAllWithLocation());
    }

    @Override
    public Result<?> get(Integer id) {
        return Result.success(binMapper.selectById(id));
    }

    @Override
    public Result<?> create(Bin bin) {
        bin.setCreatedAt(java.time.LocalDateTime.now());
        binMapper.insert(bin);
        return Result.success(bin);
    }

    @Override
    public Result<?> update(Integer id, Bin bin) {
        bin.setId(id);
        binMapper.updateById(bin);
        return Result.success(bin);
    }

    @Override
    public Result<?> delete(Integer id) {
        binMapper.deleteById(id);
        return Result.success();
    }

    @Override
    public Result<?> qrcode(Integer id) {
        Bin bin = binMapper.selectById(id);
        if (bin == null) {
            return Result.error("储位不存在");
        }
        String url = "/api/bin/qrcode/" + id;
        String base64 = qrCodeUtil.generateBase64(url);
        return Result.success(base64);
    }
}