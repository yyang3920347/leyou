package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum  ExceptionEnum {
    BRAND_NOT_FOND(404,"品牌不存在"),
    CATEGORY_NOT_FOND(404,"商品分类没查到"),
    SPEC_GROUP_NOT_FOND(404,"商品规格组没查到"),
    SPEC_GROUP_SAVE_ERROR(500,"新增品牌分组失败"),
    SPEC_GROUP_UPDATE_ERROR(500,"修改品牌分组失败"),
    SPEC_GROUP_DELETE_ERROR(500,"删除品牌分组失败"),
    SPEC_PARAM_NOT_FOND(404,"商品规格参数没查到"),
    SPEC_PARAM_SAVE_ERROR(500,"新增商品规格参数失败"),
    SPEC_PARAM_UPDATE_ERROR(500,"修改商品规格参数失败"),
    SPEC_PARAM_DELETE_ERROR(500,"删除商品规格参数失败"),
    GOODS_NOT_FOND(404,"商品没查到"),
    GOODS_SAVE_ERROR(500,"新增商品失败"),
    GOODS_SKU_NOT_FOND(500,"商品详情没查到"),
    GOODS_STOCK_NOT_FOND(500,"商品库存没查到"),
    BRAND_SAVE_ERROR(500,"新增品牌失败"),
    BRAND_UPDATE_ERROR(500,"修改品牌失败"),
    BRAND_DELETE_ERROR(500,"删除品牌失败"),
    UPLOAD_FILE_ERROR(500,"文件上传失败"),
    INVALID_FILE_TYPE(500,"文件上传失败,文件类型不匹配"),
    INVALID_USER_DATA_TYPE(400,"用户的数据类型无效"),
    INVALID_VERIFY_CODE(400,"无效的验证码"),
    INVALID_USERNAME_PASSWORD(400,"用户名或密码错误"),
    CREATE_TOKEN_ERROR(500,"用户凭证生成失败"),
    UNAUTHORIZED(403,"未授权"),
    ;
    private int code;
    private String msg;
}
