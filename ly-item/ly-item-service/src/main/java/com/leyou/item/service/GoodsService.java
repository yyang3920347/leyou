package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.mapper.StockMapper;
import com.leyou.item.pojo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.util.StringUtil;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GoodsService {
    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    public PageResult<Spu> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key) {
        //分页
        PageHelper.startPage(page, rows);
        //过滤
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        //搜索字段过滤
        if (StringUtil.isNotEmpty(key)) {
            criteria.andLike("title", "%" + key + "%");
        }
        //上下架过滤
        if (saleable != null) {
            criteria.andEqualTo("saleable", saleable);
        }
        //默认商品的更新时间排序
        example.setOrderByClause("last_update_time DESC");
        //查询
        List<Spu> spus = spuMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(spus)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOND);
        }
        //解析分类和品牌的名称
        loadCategoryAndBrandName(spus);

        //解析分页结果
        PageInfo<Spu> info = new PageInfo<>(spus);
        return new PageResult<>(info.getTotal(), spus);


    }

    private void loadCategoryAndBrandName(List<Spu> spus) {
        for (Spu spu : spus) {
            //处理分类名称
            List<String> names = categoryService.queryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()))
                    .stream().map(Category::getName).collect(Collectors.toList());
            spu.setCname(StringUtils.join(names, "/"));
            //处理品牌名称
            spu.setBname(brandService.queryById(spu.getBrandId()).getName());
        }
    }

    @Transactional
    public void saveGoods(Spu spu) {
        //新增spu
        spu.setId(null);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        spu.setSaleable(true);
        spu.setValid(false);

        int count = spuMapper.insert(spu);
        if (count != 1) {
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
        //新增detail
        SpuDetail detail = spu.getSpuDetail();
        detail.setSpuId(spu.getId());
        spuDetailMapper.insert(detail);
        //调用新增sku和stock方法
        saveSkuAndStock(spu);

        //发送mq消息
        amqpTemplate.convertAndSend("item.insert",spu.getId());
    }

    /**
     * 新增sku和stock
     * @param spu
     */
    private void saveSkuAndStock(Spu spu) {
        int count;//定义库存集合
        List<Stock> stockList = new ArrayList<>();
        //新增sku
        List<Sku> skus = spu.getSkus();
        for (Sku sku : skus) {
            sku.setSpuId(spu.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            count = skuMapper.insert(sku);
            if (count != 1) {
                throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
            }
            //新增库存
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            stockList.add(stock);
        }
        //批量新增库存
        count = stockMapper.insertList(stockList);
        if (count != stockList.size()) {
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }

        //发送mq消息
        amqpTemplate.convertAndSend("item.insert",spu.getId());
    }

    public SpuDetail querySpuDetailBySpuId(Long spuId) {
        //根据spuId查询spuDetail
        return spuDetailMapper.selectByPrimaryKey(spuId);
    }

    @Transactional
    public List<Sku> querySkuBySpuId(Long spuId) {
        //查询sku
        Sku record = new Sku();
        record.setSpuId(spuId);
        List<Sku> skus = this.skuMapper.select(record);
        for (Sku sku : skus) {
            //同时查询出库存
            sku.setStock(this.stockMapper.selectByPrimaryKey(sku.getId()).getStock());
        }
        return skus;
    }

    @Transactional
    public void updateGoods(Spu spu) {
        //查询根据spuId查询要删除的sku
        Sku record = new Sku();
        record.setSpuId(spu.getId());
        List<Sku> skus = this.skuMapper.select(record);
        for (Sku sku : skus) {
            //删除stock
            this.stockMapper.deleteByPrimaryKey(sku.getId());
        }
        //删除sku
        Sku sku = new Sku();
        sku.setSpuId(spu.getId());
        this.skuMapper.delete(sku);
        //新增sku和stock
        saveSkuAndStock(spu);
        //更新spu和spuDetail
        spu.setCreateTime(null);
        spu.setLastUpdateTime(new Date());
        spu.setValid(null);
        spu.setSaleable(null);
        this.spuMapper.updateByPrimaryKeySelective(spu);
        this.spuDetailMapper.updateByPrimaryKeySelective(spu.getSpuDetail());

        //发送mq消息
        amqpTemplate.convertAndSend("item.update",spu.getId());
    }

    @Transactional
    public void deleteGoods(Long spuId) {
        //根据spuId查询要删除的sku
        Sku record = new Sku();
        record.setSpuId(spuId);
        List<Sku> skus = this.skuMapper.select(record);
        for (Sku sku : skus) {
            //删除stock
            this.stockMapper.deleteByPrimaryKey(sku.getId());
        }
        //删除sku
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        this.skuMapper.delete(sku);
        //删除spu
        this.spuMapper.deleteByPrimaryKey(spuId);
        //删除spuDetail
        this.spuDetailMapper.deleteByPrimaryKey(spuId);

        //发送mq消息
        amqpTemplate.convertAndSend("item.delete",spuId);
    }

    @Transactional
    public void updateGoodsSaleable(Long id) {
        //根据Id查询上下架状态
        Spu spu = this.spuMapper.selectByPrimaryKey(id);
        if(spu.getSaleable()==true){
            spu.setSaleable(false);
            spuMapper.updateByPrimaryKeySelective(spu);
        }else {
            spu.setSaleable(true);
            spuMapper.updateByPrimaryKeySelective(spu);
        }

        //发送mq消息
        amqpTemplate.convertAndSend("item.update",spu.getId());
    }

    /**
     * 根据spu的id查询spu
     * @param id
     * @return
     */
    public Spu querySpuById(Long id) {
        //查询spu
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if(spu==null){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOND);
        }
        //查询sku
        spu.setSkus(querySkuBySpuId(id));
        //查询detail
        spu.setSpuDetail(querySpuDetailBySpuId(id));
        return spu;
    }

    /**
     * 根据skuId查询sku
     *
     * @param skuId
     * @return
     */
    public Sku querySkuBySkuId(Long skuId) {
        return skuMapper.selectByPrimaryKey(skuId);
    }
}
