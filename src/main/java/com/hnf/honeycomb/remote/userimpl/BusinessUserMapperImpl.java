package com.hnf.honeycomb.remote.userimpl;

import com.hnf.honeycomb.bean.UserBean;
import com.hnf.honeycomb.remote.user.BusinessUserMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Repository;

import java.util.Map;

import static com.hnf.honeycomb.util.CollectionUtils.ofMap;
import static com.hnf.honeycomb.util.HttpUtil.userCall;

/**
 * @author xhz
 * @version 1.0
 * @date 2019/7/12 11:13
 */
@Repository
@EnableCaching
public class BusinessUserMapperImpl implements BusinessUserMapper {
    @Override
    @Cacheable(value = "userRemote", keyGenerator = "keyGenerator")
    public Map findById(Integer userId) {
        return userCall("user/findMapById"
                , ofMap("userId", userId)
                , Map.class
        );
    }

    @Override
    @Cacheable(value = "userRemote", keyGenerator = "keyGenerator")
    public UserBean findByPoliceNumber(String policeNumber) {
        return userCall("user/findUserBeanByPoliceNumber"
                , ofMap("policeNumber", policeNumber)
                , UserBean.class
        );
    }

}
