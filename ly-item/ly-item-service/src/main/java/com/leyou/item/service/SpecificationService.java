package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SpecificationService {
    @Autowired
    private SpecGroupMapper specGroupMapper;

    @Autowired
    private SpecParamMapper specParamMapper;


    public List<SpecGroup> queryGroupByCid(Long cid) {
        //查询条件
        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(cid);
        //查询
        List<SpecGroup> list = specGroupMapper.select(specGroup);
        if (CollectionUtils.isEmpty(list)) {
            //没查到
            throw new LyException(ExceptionEnum.SPEC_GROUP_NOT_FOND);
        }
        return list;
    }

    public List<SpecParam> queryParamList(Long gid, Long cid, Boolean searching) {
        //查询条件
        SpecParam specParam = new SpecParam();
        specParam.setGroupId(gid);
        specParam.setCid(cid);
        specParam.setSearching(searching);
        //查询
        List<SpecParam> list = specParamMapper.select(specParam);
        if (CollectionUtils.isEmpty(list)) {
            //没查到
            throw new LyException(ExceptionEnum.SPEC_PARAM_NOT_FOND);
        }
        return list;
    }

    @Transactional
    public void saveSpecGroup(SpecGroup specGroup) {
        //新增分组
        specGroup.setId(null);
        int count = specGroupMapper.insert(specGroup);
        if (count != 1) {
            throw new LyException(ExceptionEnum.SPEC_GROUP_SAVE_ERROR);
        }
    }

    @Transactional
    public void updateSpecGroup(SpecGroup specGroup) {
        //修改分组name
        int count = specGroupMapper.updateByPrimaryKeySelective(specGroup);
        if (count != 1) {
            throw new LyException(ExceptionEnum.SPEC_GROUP_UPDATE_ERROR);
        }
    }

    @Transactional
    public void deleteSpecGroup(Long id) {
        //删除分组
        int count = specGroupMapper.deleteByPrimaryKey(id);
        if (count != 1) {
            throw new LyException(ExceptionEnum.SPEC_GROUP_DELETE_ERROR);
        }
        //同时删除参数信息
        int count2 = specParamMapper.deleteSpecParam(id);
        if (count2 != 1) {
            throw new LyException(ExceptionEnum.SPEC_PARAM_DELETE_ERROR);
        }
    }

    @Transactional
    public void saveSpecParam(SpecParam specParam) {
        //新增参数信息
        specParam.setId(null);
        int count = specParamMapper.insert(specParam);
        if (count != 1) {
            throw new LyException(ExceptionEnum.SPEC_PARAM_SAVE_ERROR);
        }
    }


    @Transactional
    public void updateSpecParam(SpecParam specParam) {
        //修改参数信息
        int count = specParamMapper.updateByPrimaryKeySelective(specParam);
        if (count != 1) {
            throw new LyException(ExceptionEnum.SPEC_PARAM_UPDATE_ERROR);
        }
    }

    @Transactional
    public void deleteSpecParam(Long id) {
        //删除参数信息
        int count = specParamMapper.deleteByPrimaryKey(id);
        if (count != 1) {
            throw new LyException(ExceptionEnum.SPEC_PARAM_DELETE_ERROR);
        }
    }

    /**
     * 根据分类查询规格组及组内参数
     * @param cid
     * @return
     */

    public List<SpecGroup> queryListByCid(Long cid) {
        //查询规格组
        List<SpecGroup> specGroups = queryGroupByCid(cid);
        //查询分类下的参数
        List<SpecParam> specParams = queryParamList(null, cid, null);
        //先把规格参数变成map,map的key是规格组id,map的值是组下的所有参数
        Map<Long, List<SpecParam>> map = new HashMap<>();
        for (SpecParam param : specParams) {
            if(!map.containsKey(param.getGroupId())){
                //这个组id在map中不存在，新增一个list
                map.put(param.getGroupId(),new ArrayList<>());
            }
            map.get(param.getGroupId()).add(param);
        }
        //填充param到group
        for (SpecGroup specGroup : specGroups) {
            specGroup.setParams(map.get(specGroup.getId()));
        }
        return specGroups;
    }

}
