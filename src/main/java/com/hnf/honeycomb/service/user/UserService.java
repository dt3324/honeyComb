package com.hnf.honeycomb.service.user;

import com.hnf.honeycomb.bean.UserLicenseBean;
import com.hnf.honeycomb.bean.User;
import com.hnf.honeycomb.bean.UserBean;
import com.hnf.honeycomb.bean.enumerations.UserTypeEnum;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * @author zhouhong
 * @ClassName UserService
 * @Description: TODO 用户管理业务层接口
 * @date 2018年6月25日 上午10：38：15
 */
public interface UserService {

    /**
     * 用户登录
     *
     * @param type     用户登录类型，1普通用户登录，2管理员登录
     * @param name     账号
     * @param password 密码
     * @param place    登录IP
     * @param time     登录时间
     * @return Map
     */
    Map<String, Object> login(UserTypeEnum type, String name, String password, String place, String time);

    /**
     * 对身份证号进行加密
     *
     * @param idNumber 身份证号
     */
    String encode3Des(String idNumber);

    /**
     * 激活用户
     *
     * @param userId 用户ID
     * @param place  登录IP
     * @return
     */
    UserBean activate(Integer userId, String place);


    /**
     * 通过身份证号查找用户
     *
     * @param idNumber 身份证号
     * @return
     */
    User findByidNumber(String idNumber);

    Map<String, Object> updatePassword(Integer userid, String oldPassword, String password, String confirm);

    /**
     * 删除用户
     *
     * @param userId
     * @return
     */
    boolean delete(Integer userId);

    /**
     * 用户注册
     *
     * @param name           名字
     * @param password       密码
     * @param nick           昵称
     * @param confirm        确认密码
     * @param roleId         角色
     * @param departmentCode 部门编码
     * @param policeNumber   警号
     * @param idNumber       身份证
     * @param remark         标记
     * @param phoneNumber    电话号码
     * @param vPhone         短号
     * @param activate       激活状态
     * @param caseTypeIds    案件标签类型ids
     * @return the user registed
     * @throws
     */
    User regist(String name, String password, String nick, String confirm, Integer roleId,
                String departmentCode, String policeNumber, String idNumber, String remark,
                String phoneNumber, String vPhone, Integer activate, List<Integer> caseTypeIds,String createUser);

    Page<User> findAll(Integer pageNum, Integer pageSize, String policeNumberAndNickName, String departmentCode,
                       String activate, String nickName, String roleId);

    /**
     * 用户信息修改
     *
     * @param userId
     * @param password
     * @param nickname
     * @param roleId
     * @param departmentCode
     * @param remark
     * @param idNumber
     * @param phoneNumber
     * @param vphone
     * @param operatePolice
     * @param operateUnit
     * @param operateIp
     * @return
     */
    User updatePower(Integer userId, String password, String nickname, Integer roleId
            , String departmentCode, String remark, String idNumber
            , String phoneNumber, String vphone, List<Integer> caseTypeIds
            , String operatePolice, String operateUnit, String operateIp);

    User findById(Integer userid);

    List<User> findByName(String username);

    String loginMD5(String policeNumber, String date);

    User findByUserId(Integer userid);


    User findByPoliceNumber(String policeNumber);

    List<User> findByDepartmentId(Long departmentType);

    User logout(Integer userId, String place);

    User activate(Integer userId, Integer activate, String place);

    /**
     * 判断用户是否有某功能操作权限
     *
     * @param userId      用户ID
     * @param operationId 操作功能ID
     * @return
     */
    Map<String, Object> findOperation(Integer userId, Integer operationId);

    List<User> findUserByDepart(String departmentCode);

    /**
     * 修改对应ID用户的警号
     *
     * @param userId
     * @param policeNumber
     */
    Integer updatePoliceNumber(Integer userId, String policeNumber);


    void updateUserDepartType(Long newDepartmentType, Long oldDepartmentType);

    String decode3Des(String iDnumber);

    User fullFilledLoginedUserInfo(String name);

    /**
     * 通过excel 批量添加用户
     *
     * @param file 表格文件
     * @return 返回成功条数
     */
    String batchCreateUsers(MultipartFile file) throws ParseException;

    User findUserByPoliceNumber(String policeNumber);

    /**
     * 添加用户临时权限
     */
    void addUserLicense(Map<String,Object> userLicenses, String policeNumber, String unitCode, String ip);

    /**
     * 修改用户临时权限
     */
    void updateUserLicense(UserLicenseBean userLicenseBean, String policeNumber, String unitCode, String ip);

    /**
     * 删除用户临时权限
     */
    void deleteUserLicense(Integer id, String policeNumber, String unitCode, String ip);

    /**
     * 查询用户临时权限
     *
     * @param userId
     * @return
     */
    List<UserLicenseBean> findUserLicensesByUserId(Long userId);

    interface LoginStatus {
        String SUCCESS = "5";
    }

    /**
     * 修改登录操作时间
     *
     * @param policeNumber 警号
     */
    void updateLastActiveyTime(String policeNumber);

    /**
     * 插入登录日志
     *
     * @param place 登录IP
     * @param bean  用户
     * @param area  访问内容
     * @param state 登录状态
     * @param i     登录类型
     */
    void logLoginEvent(String place, User bean, String area, String state, int i);

    /**
     * 生成验证token
     *
     * @param map userId,policeNumber...
     * @return
     */
    String getToken(Map<String, Object> map);

    /**
     * 返回用户可操作功能列表
     *
     * @param roleId
     * @return
     */
    List<Object> findOperation(Long roleId);

    /**
     * 记录用户管理日志
     *
     * @param operatePolice 用户警号
     * @param operateUnit   用户departmentCode
     * @param operateIp     用户IP
     * @param operateType   操作类型 增删改123
     * @param operateName   操作类型
     * @param operate       操作内用
     */
    void insertModifyLog(String operatePolice, String operateUnit, String operateIp, int operateType, String operateName, Object operate);
}
