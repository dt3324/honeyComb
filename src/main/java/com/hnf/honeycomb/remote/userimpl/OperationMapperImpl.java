package com.hnf.honeycomb.remote.userimpl;

import com.hnf.honeycomb.bean.OperationBean;
import com.hnf.honeycomb.remote.user.OperationMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.hnf.honeycomb.util.CollectionUtils.ofMap;
import static com.hnf.honeycomb.util.HttpUtil.userCall;

/**
 * @author xhz
 * @version 1.0
 * @date 2019/7/12 11:15
 */
@Repository
@EnableCaching
public class OperationMapperImpl implements OperationMapper {


    @Override
    @Cacheable(value = "operationRemote", keyGenerator = "keyGenerator")
    public List<OperationBean> find() {
        return userCall("operation/find"
                , null
                , new ParameterizedTypeReference<List<OperationBean>>() {
                }
        );
    }

    @Override
    @Cacheable(value = "operationRemote", keyGenerator = "keyGenerator")
    public List<OperationBean> findByRoleid(Long roleId) {
        return userCall("operation/findByRoleid"
                , ofMap("roleId", roleId)
                , new ParameterizedTypeReference<List<OperationBean>>() {
                }
        );
    }
}
