package com.fh.category.service;

import com.fh.category.mapper.CategoryMapper;
import com.fh.category.model.Category;
import com.fh.common.ServerResponse;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Resource
    private CategoryMapper categoryMapper;

    @Override
    public ServerResponse queryCategoryList() {
        List<Map<String, Object>> allList = categoryMapper.findCategoryList();
        List<Map<String, Object>> pList = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> aMap : allList) {
            if(aMap.get("pid").equals(0)){
                pList.add(aMap);
            }
        }
        selectedChildren(pList,allList);

        return ServerResponse.success(pList);
    }
    public void selectedChildren(List<Map<String, Object>> pList,List<Map<String, Object>> allList){
        for (Map<String, Object> pMap : pList) {
            List<Map<String, Object>> childrenList = new ArrayList<Map<String, Object>>();
            for (Map<String, Object> aMap : allList) {
                if(pMap.get("id").equals(aMap.get("pid"))){
                    //String childId = aMap.get("id").toString();
                    //delList.add(Integer.valueOf(childId));
                    childrenList.add(aMap);
                }
            }
            if(childrenList != null && childrenList.size()>0){
                pMap.put("children",childrenList);
                selectedChildren(childrenList,allList);
            }
        }
    }
}
