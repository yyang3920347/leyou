package com.leyou.item.mapper;

import com.leyou.common.mapper.BaseMapper;
import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface BrandMapper extends BaseMapper<Brand> {

    @Insert("INSERT INTO tb_category_brand (category_id, brand_id) VALUES (#{cid},#{bid})")
    int insertCategoryBrand(@Param("cid")Long cid,@Param("bid")Long bid);

    //如果新增商品分类重复就修改cid，否则之间添加新商品分类
    @Update("INSERT INTO  tb_category_brand(category_id, brand_id) VALUES (#{cid},#{bid}) " +
            "ON DUPLICATE KEY UPDATE `category_id` = #{cid}")
    int updateCategoryBrand(@Param("cid")Long cid,@Param("bid")Long bid);

    @Delete("DELETE FROM `tb_category_brand` WHERE `brand_id` = #{bid}")
    int deleteCategoryBrand(@Param("bid") Long bid);

    @Select("SELECT b.id,b.`name`,b.image,b.letter " +
            "FROM tb_brand b INNER JOIN tb_category_brand cb ON b.id=cb.brand_id " +
            "WHERE cb.category_id=#{cid}")
    List<Brand> queryBrandByCid(@Param("cid")Long cid);
}
