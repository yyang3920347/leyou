package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.util.StringUtil;

import java.util.List;

@Service
public class BrandService {
    @Autowired
    private BrandMapper brandMapper;


    public PageResult<Brand> queryBrandByPage(
            Integer page, Integer rows, String sortBy, Boolean desc, String key) {
        //分页
        PageHelper.startPage(page, rows);
        /*
         * WHERE `name` LIKE "%x%" OR letter = "x"
         * ORDER BY id DESC
         */
        //过滤
        Example example = new Example(Brand.class);
        if (StringUtil.isNotEmpty(key)) {
            //过滤条件
            example.createCriteria().orLike("name", "%" + key + "%")
                    .orEqualTo("letter", key.toUpperCase());
        }
        //排序
        if (StringUtil.isNotEmpty(sortBy)) {
            String orderByClause = sortBy + (desc ? " DESC" : " ASC");
            example.setOrderByClause(orderByClause);
        }
        //查询
        List<Brand> list = brandMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOND);
        }
        //解析分页结果
        PageInfo<Brand> info = new PageInfo<>(list);

        return new PageResult<>(info.getTotal(), list);
    }

    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        //新增品牌
        brand.setId(null);
        int count = brandMapper.insert(brand);
        if (count != 1) {
            throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
        }
        //新增中间表
        for (Long cid : cids) {
            count = brandMapper.insertCategoryBrand(cid, brand.getId());
            if (count != 1) {
                throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
            }
        }
    }

    @Transactional
    public void updateBrand(Brand brand, List<Long> cids) {
        //修改品牌
        int count = brandMapper.updateByPrimaryKeySelective(brand);
        if (count != 1) {
            throw new LyException(ExceptionEnum.BRAND_UPDATE_ERROR);
        }
        //修改中间表
        for (Long cid : cids) {
            count = brandMapper.updateCategoryBrand(cid, brand.getId());
            if (count != 1) {
                throw new LyException(ExceptionEnum.BRAND_UPDATE_ERROR);
            }
        }
    }

    @Transactional
    public void deleteBrand(Long bid) {
        //删除品牌
        int count=brandMapper.deleteByPrimaryKey(bid);
        if (count != 1) {
            throw new LyException(ExceptionEnum.BRAND_DELETE_ERROR);
        }
        //删除中间表
        int count2=brandMapper.deleteCategoryBrand(bid);
        if(count!=1){
            throw new LyException(ExceptionEnum.BRAND_UPDATE_ERROR);
        }
    }

    public Brand queryById(Long id){
        Brand brand = brandMapper.selectByPrimaryKey(id);
        if (brand==null){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOND);
        }
        return brand;
    }
    @Transactional
    public List<Brand> queryBrandByCid(Long cid) {
        //根据cid查询品牌
        List<Brand> list = brandMapper.queryBrandByCid(cid);
        if (CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOND);
        }
        return list;
    }

    public List<Brand> queryBrandByIds(List<Long> ids) {
        //根据ids查询品牌
        List<Brand> brands = brandMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(brands)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOND);
        }
        return brands;
    }
}
