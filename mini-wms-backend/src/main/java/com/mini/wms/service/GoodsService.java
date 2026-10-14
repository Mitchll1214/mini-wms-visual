package com.mini.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mini.wms.dto.Result;
import com.mini.wms.entity.Goods;
import com.mini.wms.entity.GoodsImage;
import com.mini.wms.mapper.GoodsMapper;
import com.mini.wms.mapper.ImageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品服务
 */
@Service
@RequiredArgsConstructor
public class GoodsService {

    private final GoodsMapper goodsMapper;
    private final ImageMapper imageMapper;

    /**
     * 分页查询商品（自动填充主图）
     */
    public Result<?> page(int page, int size, String keyword, String categoryId, String status) {
        Page<Goods> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<Goods>()
                .and(keyword != null && !keyword.isEmpty(), w -> w
                        .like(Goods::getName, keyword)
                        .or()
                        .like(Goods::getCode, keyword))
                .eq(categoryId != null && !categoryId.isEmpty(), Goods::getCategoryId, categoryId)
                .eq(status != null && !status.isEmpty(), Goods::getStatus, status)
                .orderByDesc(Goods::getCreatedAt);
        IPage<Goods> result = goodsMapper.selectPage(pageObj, wrapper);
        fillMainImages(result.getRecords());
        return Result.success(result);
    }

    public Result<?> get(Integer id) {
        Goods goods = goodsMapper.selectById(id);
        if (goods != null) {
            fillMainImages(List.of(goods));
        }
        return Result.success(goods);
    }

    public Result<?> create(Goods goods) {
        // 商品编码后台自动生成（留空时）
        if (goods.getCode() == null || goods.getCode().isBlank()) {
            goods.setCode(generateCode());
        }
        goods.setCreatedAt(LocalDateTime.now());
        goods.setUpdatedAt(LocalDateTime.now());
        if (goods.getStatus() == null) {
            goods.setStatus("ENABLED");
        }
        goodsMapper.insert(goods);
        return Result.success(goods);
    }

    public Result<?> update(Integer id, Goods goods) {
        goods.setId(id);
        goods.setUpdatedAt(java.time.LocalDateTime.now());
        goodsMapper.updateById(goods);
        return Result.success(goods);
    }

    public Result<?> delete(Integer id) {
        goodsMapper.deleteById(id);
        return Result.success();
    }

    public Result<?> status(Integer id, String status) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            return Result.error("商品不存在");
        }
        goods.setStatus(status);
        goodsMapper.updateById(goods);
        return Result.success(goods);
    }

    public Result<?> selectByCode(String code) {
        return Result.success(goodsMapper.selectByCode(code));
    }

    public Result<?> selectByBarcode(String barcode) {
        return Result.success(goodsMapper.selectByBarcode(barcode));
    }

    /**
     * 批量填充商品主图地址（图片 id → /api/images/{id}/thumb）
     */
    private void fillMainImages(List<Goods> goodsList) {
        if (goodsList == null || goodsList.isEmpty()) {
            return;
        }
        List<Integer> ids = goodsList.stream().map(Goods::getId).collect(Collectors.toList());
        List<Map<String, Object>> thumbs = imageMapper.selectMainThumbs(ids);
        Map<Integer, String> urlByGoods = thumbs.stream().collect(Collectors.toMap(
                m -> ((Number) m.get("goods_id")).intValue(),
                m -> "/api/images/" + ((Number) m.get("id")).intValue() + "/thumb",
                (a, b) -> a));
        for (Goods g : goodsList) {
            g.setMainImageUrl(urlByGoods.get(g.getId()));
        }
    }

    /**
     * 生成商品编码：SP + yyyyMMddHHmmss + 3 位随机序号（冲突时重试）
     */
    private String generateCode() {
        for (int i = 0; i < 5; i++) {
            String code = "SP" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                    + String.format("%03d", (int) (Math.random() * 1000));
            if (goodsMapper.selectByCode(code) == null) {
                return code;
            }
        }
        return "SP" + System.currentTimeMillis();
    }
}