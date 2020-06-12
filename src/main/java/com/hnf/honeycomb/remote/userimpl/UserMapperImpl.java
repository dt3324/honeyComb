package com.hnf.honeycomb.remote.userimpl;

import com.hnf.honeycomb.bean.AddUserBean;
import com.hnf.honeycomb.bean.User;
import com.hnf.honeycomb.remote.user.UserMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import static com.hnf.honeycomb.util.CollectionUtils.ofMap;
import static com.hnf.honeycomb.util.HttpUtil.userCall;

/**
 * @author xhz
 * @version 1.0
 * @date 2019/7/12 11:21
 */
@Repository
@EnableCaching
public class UserMapperImpl implements UserMapper {
    @Override
    @Cacheable(value = "userRemote", keyGenerator = "keyGenerator")
    public User findUserByPoliceNumber(String name) {
        return userCall("user/findUserByPoliceNumber"
                , ofMap("name", name)
                , User.class
        );
    }

    @Override
    @CacheEvict(value = "userRemote", allEntries = true)
    public void updateActivate(Map<String, Object> map) {
        userCall(
                "user/updateActivate"
                , map
                , String.class
        );
    }

    @Override
    @CacheEvict(value = "userRemote", allEntries = true)
    public void updateLastActiveyTimeByPoliceNum(Map<String, Object> map) {
        userCall(
                "user/updateLastActiveyTimeByPoliceNum"
                , map
                , String.class
        );
    }

    @Override
    @CacheEvict(value = "userRemote", allEntries = true)
    public Integer saveUser(User user) {
        return userCall(
                "user/saveUser"
                , user
                , Integer.class
        );
    }

    @Override
    @CacheEvict(value = "userRemote", allEntries = true)
    public void updateUserUnitType(Map<String, Object> map) {
        userCall(
                "user/updateUserUnitType"
                , map
                , String.class
        );
    }

    @Override
    @CacheEvict(value = "userRemote", allEntries = true)
    public void updateUserDepartType(Map<String, Object> map) {
        userCall(
                "user/updateUserDepartType"
                , map
                , String.class
        );
    }

    @Override
    @CacheEvict(value = "userRemote", allEntries = true)
    public void updateOne(Map<String, Object> map) {
        userCall(
                "user/updateOne"
                , map
                , String.class
        );
    }

    @Override
    @Cacheable(value = "userRemote", keyGenerator = "keyGenerator")
    public List<User> findByDepartmentId(Map<String, Object> map) {
        return userCall(
                "user/findByDepartmentId"
                , map
                , new ParameterizedTypeReference<List<User>>() {
                }
        );
    }

    @Override
    @Cacheable(value = "userRemote", keyGenerator = "keyGenerator")
    public int findCount(Map<String, Object> map) {
        return userCall(
                "user/findCountNew"
                , map
                , Integer.class
        );
    }

    @Override
    @Cacheable(value = "userRemote", keyGenerator = "keyGenerator")
    public List<User> findAll(Map<String, Object> map) {
        return userCall(
                "user/findAllNew"
                , map
                , new ParameterizedTypeReference<List<User>>() {
                }
        );
    }

    @Override
    @Cacheable(value = "userRemote", keyGenerator = "keyGenerator")
    public User findUserInfoByPoliceNumber(String policeNumber) {
        return userCall(
                "user/findUserInfoByPoliceNumber"
                , ofMap("policeNumber", policeNumber)
                , User.class
        );
    }

    @Override
    @Cacheable(value = "userRemote", keyGenerator = "keyGenerator")
    public List<User> findByName(String name) {
        return userCall(
                "user/findByName"
                , ofMap("name", name)
                , new ParameterizedTypeReference<List<User>>() {
                }
        );
    }

    @Override
    @Cacheable(value = "userRemote", keyGenerator = "keyGenerator")
    public User findById(Integer userId) {
        return userCall(
                "user/findById"
                , ofMap("userId", userId)
                , User.class
        );
    }

    @Override
    @CacheEvict(value = "userRemote", allEntries = true)
    public void delete(String policenumber) {
        userCall(
                "user/delete"
                , ofMap("policenumber", policenumber)
                , String.class
        );
    }

    @Override
    @CacheEvict(value = "userRemote", allEntries = true)
    public void updateToken(Map<String, Object> map) {
        userCall(
                "user/updateToken"
                , map
                , String.class
        );
    }

    @Override
    @CacheEvict(value = "userRemote", allEntries = true)
    public void updatePoliceNumberByPID(Map<String, Object> map) {
        userCall(
                "user/updatePoliceNumberByPID"
                , map
                , String.class
        );
    }

    @Override
    @CacheEvict(value = "userRemote", allEntries = true)
    public void updateUserDIdAndRIdByPoliceNumber(Map<String, Object> map) {
        userCall(
                "user/updateUserDIdAndRIdByPoliceNumber"
                , map
                , String.class
        );
    }

    @Override
    @Cacheable(value = "userRemote", keyGenerator = "keyGenerator")
    public Map<String, Object> findUserBaseInfoByUserId(Integer userId) {
        return userCall(
                "user/findUserBaseInfoByUserId"
                , ofMap("userId", userId)
                , new ParameterizedTypeReference<Map<String, Object>>() {
                }
        );
    }

    @Override
    @CacheEvict(value = "userRemote", allEntries = true)
    public void updatePassword(Map<String, Object> map) {
        userCall(
                "user/updatePassword"
                , map
                , String.class
        );
    }

    @Override
    @Cacheable(value = "userRemote", keyGenerator = "keyGenerator")
    public User findByidNumber(String idNumber) {
        return userCall(
                "user/findByidNumber"
                , ofMap("idNumber", idNumber)
                , User.class
        );
    }

    @Override
    @Cacheable(value = "userRemote", keyGenerator = "keyGenerator")
    public User findByidNumberAndId(Map map) {
        return userCall(
                "user/findByidNumberAndId"
                , map
                , User.class
        );
    }

    @Override
    @Cacheable(value = "userRemote", keyGenerator = "keyGenerator")
    public List<User> findByRoleId(Long roleId) {
        return userCall(
                "user/findByRoleId"
                , ofMap("roleId", roleId)
                , new ParameterizedTypeReference<List<User>>() {
                }
        );
    }
    @Override
    @Cacheable(value = "userRemote", keyGenerator = "keyGenerator")
    public int getUserCreateUserCount(String createUser) {
        return userCall(
                "user/getUserCreateUserCount"
                , ofMap("createUser", createUser)
                , new ParameterizedTypeReference<Integer>() {
                }
        );
    }

    @Override
    @CacheEvict(value = "userRemote", allEntries = true)
    public void addUsersOfBatch(List<AddUserBean> listUsers) {
        userCall(
                "user/addUsersOfBatch"
                , listUsers
                , String.class
        );
    }
}
