package com.hnf.honeycomb.remote.userimpl;

import com.hnf.honeycomb.mapper.FetchMapper;
import com.hnf.honeycomb.remote.user.FetchLogMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static com.hnf.honeycomb.util.HttpUtil.addField;

/**
 * @author xhz
 * @version 1.0
 * @date 2019/7/12 11:13
 */
@Repository
public class FetchLogMapperImpl implements FetchLogMapper {
    @Resource
    private FetchMapper fetchMapper;

    @Override
    public List<Map<String, Object>> findSBeeFetchLogByUnitTypeAndPNumber(Map<String, Object> map) {
        List<Map<String, Object>> list = fetchMapper.findSBeeFetchLogByUnitTypeAndPNumber(map);
        addField(list, "department_code", "departmentName"
                , "department/findDepartmentNameListByCode"
        );
        return list;
    }


    @Override
    public List<Map<String, Object>> countSBeeFetch(Map<String, Object> para) {
        List<Map<String, Object>> list = fetchMapper.countSBeeFetch(para);
        addField(list, "code", "name"
                , "department/findDepartmentNameListByCode"
        );
        return list;
    }

    @Override
    public List<Map<String, Object>> countBBeeFetch(Map<String, Object> para) {
        List<Map<String, Object>> list = fetchMapper.countBBeeFetch(para);
        addField(list, "code", "name"
                , "department/findDepartmentNameListByCode"
        );
        return list;
    }

    @Override
    public List<Map<String, Object>> countBBeeFetchDist(Map<String, Object> para) {
        return fetchMapper.countBBeeFetchDist(para);
    }

    @Override
    public List<Map<String, Object>> countBBeeFetchByCompleteDepartCodeOrPnumber(Map<String, Object> para) {
        List<Map<String, Object>> list = fetchMapper.countBBeeFetchByCompleteDepartCodeOrPnumber(para);
        addField(list
                , "code"
                , "name"
                , "user/findNickNameListByMap"
        );
        return list;
    }

    @Override
    public List<Map<String, Object>> countBBeeFetchByCompleteDepartCodeOrPnumberDist(Map<String, Object> para) {
        return fetchMapper.countBBeeFetchByCompleteDepartCodeOrPnumberDist(para);
    }

    @Override
    public List<Map<String, Object>> countSBeeFetchByCompleteDepartCodeOrPnumber(Map<String, Object> para) {
        List<Map<String, Object>> list = fetchMapper.countSBeeFetchByCompleteDepartCodeOrPnumber(para);
        addField(list, "code", "name"
                , "user/findNickNameListByMap"
        );
        return list;
    }
}
