package com.hnf.honeycomb.remote.userimpl;

import com.hnf.honeycomb.mapper.DeviceMapper;
import com.hnf.honeycomb.remote.user.BusinessDeviceMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static com.hnf.honeycomb.util.HttpUtil.addField;

/**
 * @author xhz
 * @version 1.0
 * @date 2019/7/13 14:06
 */
@Repository
public class BusinessDeviceMapperImpl implements BusinessDeviceMapper {
    @Resource
    private DeviceMapper deviceMapper;

    @Override
    public Integer findMaxFetchLogId() {
        return deviceMapper.findMaxFetchLogId();
    }

    @Override
    public List<Map<String, Object>> findFetchLogMore(Long id) {
        List<Map<String, Object>> list = deviceMapper.findFetchLogMore(id);
        addField(list
                , "department_code"
                , "department_name"
                , "department/findDepartmentNameListByCode");
        return list;
    }

    @Override
    public List<Map<String, Object>> findMoreSmallFetchLog(Long id) {
        List<Map<String, Object>> list = deviceMapper.findMoreSmallFetchLog(id);
        addField(list
                , "department_code"
                , "department_name"
                , "department/findDepartmentNameListByCode"
        );
        return list;
    }

    @Override
    public Integer countUpLoadTimeByDUniqueAndDogNum(Map map) {
        return deviceMapper.countUpLoadTimeByDUniqueAndDogNum(map);
    }

    @Override
    public List<Object> findAboutFetchLogByDeviceUnique(String deviceUnique) {
        List<Object> list = deviceMapper.findAboutFetchLogByDeviceUnique(deviceUnique);
        addField((List<Map<String, Object>>) (List<?>) list
                , "department_code"
                , "department_name"
                , "department/findDepartmentNameListByCode"
        );
        return list;
    }

    @Override
    public List<Object> findSBeeFetchDetailByDeviceUnique(String deviceUnique) {
        List<Object> list = deviceMapper.findSBeeFetchDetailByDeviceUnique(deviceUnique);
        addField((List<Map<String, Object>>) (List<?>) list
                , "department_code"
                , "department_name"
                , "department/findDepartmentNameListByCode"
        );
        return list;
    }
}
