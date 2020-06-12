package com.hnf.honeycomb.serviceimpl.user;

import com.alibaba.fastjson.JSONObject;
import com.hnf.honeycomb.bean.*;
import com.hnf.honeycomb.bean.enumerations.Operation;
import com.hnf.honeycomb.bean.enumerations.RoleEnum;
import com.hnf.honeycomb.bean.enumerations.UserTypeEnum;
import com.hnf.honeycomb.bean.exception.IllegalInputException;
import com.hnf.honeycomb.bean.exception.NoSuchUserException;
import com.hnf.honeycomb.config.ProjectLevelConstants;
import com.hnf.honeycomb.config.security.JwtField;
import com.hnf.honeycomb.dao.BaseDao;
import com.hnf.honeycomb.dao.UserMongoDao;
import com.hnf.honeycomb.remote.user.*;
import com.hnf.honeycomb.service.user.JwtService;
import com.hnf.honeycomb.service.user.UserDepartmentService;
import com.hnf.honeycomb.service.user.UserService;
import com.hnf.honeycomb.util.*;
import com.mongodb.BasicDBObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.InputStream;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.hnf.honeycomb.util.ObjectUtil.getInteger;
import static com.hnf.honeycomb.util.ObjectUtil.getLong;

/**
 * @author admin
 */
@Repository("User")
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(ProjectLevelConstants.FREQUENT_USE_DATETIME_FORMAT);
    private static final String XLSX = ".xlsx";
    private static final String XLS = ".xls";
    @Resource
    private CheckLegal checkLegal;
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserAuthMapper userAuthMapper;
    @Resource
    private BaseDao baseDao;

    @Resource
    private RoleMapper roleDao;

    @Resource
    private RoleOperationMapper roleOperationDao;

    @Resource
    private UserMongoDao userMongoDao;

    @Resource
    private JwtField jwtField;

    @Resource
    private JwtService jwtService;

    @Resource
    private DepartmentMapper departmentMapper;
    @Resource
    private UserDepartmentService userDepartmentService;

    private Function<RoleOperationBean, LinkedHashMap<String, Object>> roleOperationToMap = (o -> {
        LinkedHashMap<String, Object> temp = new LinkedHashMap<>();
        temp.put("roleId", o.getRoleId());
        temp.put("operationId", o.getOperationId());
        return temp;
    });

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User regist(String name, String password, String nick, String confirm, Integer roleId,
                       String departmentCode, String policeNumber, String idNumber, String remark,
                       String phoneNumber, String vPhone, Integer activate, List<Integer> caseTypeIds,String createUser) {
        //获取当前用户创建的用户数量
///        int count = userMapper.getUserCreateUserCount(createUser);
///        if(count>=50){
///            throw new RuntimeException("您创建的用户数已达50上限！");
///        }
        ///        userAuthMapper.addUserCaseType(saved.getUserId(), caseTypeIds);
        return doRegister(name, password, nick, confirm, roleId, departmentCode, policeNumber, idNumber, remark,
                phoneNumber, vPhone, activate,createUser);
    }

    @Transactional(rollbackFor = Exception.class)
    User doRegister(String name, String password, String nick, String confirm, Integer roleid, String departmentCode,
                    String policenumber, String IDnumber, String remark, String phonenumber, String vphone,
                    Integer activate,String createUser) {
        /* Valid Necessary Inputs */
        if (StringUtils.isBlank(nick) && StringUtils.isBlank(name)) {
            throw IllegalInputException.of("名不能为空");
        }
        if (StringUtils.isBlank(password) || StringUtils.isBlank(confirm)) {
            throw IllegalInputException.of("密码不能为空");
        }
        password = password.trim();
        if (!password.equals(confirm.trim())) {
            throw IllegalInputException.of("密码不一致");
        }
        /* encodePassword */
        final String encodedPassword = Utils.getPassword(password);
        /* Valid Defaulted Inputs */
        if (StringUtils.isBlank(nick)) {
            nick = name;
        }
        final Long departmentId = Optional.ofNullable(this.userDepartmentService.findByCode(departmentCode))
                .map(DepartmentBean::getDepartmentId).orElse(null);
        if (departmentId == null) {
            logger.warn("没有找到 " + departmentCode + " 对应的department，已置空！");
        }
        /* Valid If Exists */
        Optional.ofNullable(this.findByidNumber(IDnumber)).ifPresent(o -> {
            throw new IllegalStateException("身份证号已存在");
        });
        Optional.ofNullable(userMapper.findUserInfoByPoliceNumber(policenumber)).ifPresent((o) -> {
            throw new IllegalStateException("警号已存在");
        });
        /* Main Business Control */
        User user = new User(
                null,
                name,
                encodedPassword,
                nick,
                roleid,
                null,
                loginMD5(policenumber.trim(), LocalDateTime.now().format(
                        DEFAULT_DATE_TIME_FORMATTER
                )),
                policenumber.trim(),
                IDnumber,
                remark,
                phonenumber,
                vphone,
                activate,
                departmentId,
                createUser
        );
        User saved;
        userMapper.saveUser(user);
        saved = userMapper.findUserInfoByPoliceNumber(policenumber.trim());
        return saved;
    }

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
    @Override
    public Map<String, Object> login(UserTypeEnum type, String name, String password, String place, String time) {
        // 存储登录返回的相关信息
        Map<String, Object> map = new HashMap<>(11);
        map.put("IP", place);
        map.put("type", 4);
        if (com.hnf.honeycomb.util.StringUtils.isEmpty(name)) {
            map.put("message", "用户名不能为空");
            return map;
        }
        if (com.hnf.honeycomb.util.StringUtils.isEmpty(password)) {
            map.put("message", "密码不能为空");
            return map;
        }
        // 获取警号对应人员信息
        User bean = userMapper.findUserInfoByPoliceNumber(name);
        if (bean == null) {
            map.put("message", "没有此账号");
            return map;
        }
        // 加密密码
        String md5 = Utils.getPassword(password);
        Long count = countErrorPassword(bean.getPoliceNumber(), place);
        //获取最后一次操作成功的时间
        // 若错误次数大于5次
        if (count >= (jwtField.getCount())) {
            map.put("message", "账户已被限制，请稍后重试");
            return map;
        }
        if (!bean.getPassword().equals(md5)) {
            map.put("message", "密码错误，还有" + (jwtField.getCount() - count) + "次机会");
            logLoginEvent(place, bean, "访问系统主界面", "登录失败", 2);
        } else {
            map.put("bean", bean);
            map.put("type", LoginStatus.SUCCESS);
            map.put("message", "登录成功");
            List<Object> operates = findOperation((long) bean.getRoleId());
            map.put("operate", operates);
            boolean isManager = operates.stream().anyMatch(Operation.manager.getOperationId()::equals);
            if (isManager) {
                // 插入登录日志
                logLoginEvent(place, bean, "访问管理员页面", "登录成功", 1);
            } else {
                // 插入登录日志
                logLoginEvent(place, bean, "访问系统主界面", "登录成功", 1);
            }
            // 修改登录操作时间
            updateLastActiveyTime(bean.getPoliceNumber());
        }
        return map;
    }

    @Override
    public void logLoginEvent(String place, User bean, String area, String state, int i) {
        HashMap<String, Object> loginMap = new HashMap<>(8);
        loginMap.put("policeNumber", bean.getPoliceNumber());
        loginMap.put("loginTime", new Date());
        loginMap.put("roleName", bean.getRoleName());
        loginMap.put("loginIp", place);
        loginMap.put("loginArea", area);
        loginMap.put("loginState", state);
        loginMap.put("loginUnitType", bean.getDepartmentCode());
        loginMap.put("loginType", i);
        userMongoDao.insertOperationDocument("logData", "userlogin", loginMap);
    }

    @Override
    public String encode3Des(String idNumber) {
        // TODO Auto-generated method stub
        idNumber = TripleDesUtils.encode3Des("hnf", idNumber);
        return idNumber;
    }

    @Override
    public String decode3Des(String idNumber) {
        idNumber = TripleDesUtils.decode3Des("hnf", idNumber);
        return idNumber;
    }

    @Override
    public User fullFilledLoginedUserInfo(String policeNumber) {
        return userMapper.findUserInfoByPoliceNumber(policeNumber);
    }

    @Override
    public String batchCreateUsers(MultipartFile file) throws ParseException {
        try {
            //获取文件名称
            String fileName = file.getOriginalFilename();
            if (null == fileName || "".equals(fileName)) {
                throw new RuntimeException("选择要导入的文件");
            }
        } catch (Exception e) {
            logger.warn("文件写入失败！@{} ");
            e.printStackTrace();
        }
        List<AddUserBean> userBeans = new ArrayList<>();
        //获取WorkBook 对象
        Workbook wb = getWorkBookByInputStream(file);
        /*
         * 读取工作表两种方式
         * 第一种：按sheet页的名称读取
         * 第二种：按sheet页的索引名称（从0开始）
         * wb.getSheetAt(1); //按索引
         * getSheet("Sheet0");//按名称
         */
        Sheet sheet = wb.getSheet("Sheet1");
        int lastRowNum = sheet.getLastRowNum();
        if (lastRowNum < 1) {
            return "文件中没有数据,请重新导入！";
        }
        /*
         * 先检查最后一行d是否存在空白，如果存在空白 则提示检查文件
         */
        String lastRowAndColumnOfZero = Constants.getStringCellValue(sheet.getRow(lastRowNum).getCell(0));
        if (lastRowAndColumnOfZero == null || lastRowAndColumnOfZero.trim().isEmpty()) {
            throw new RuntimeException("请检查文件末尾是否有空白行--");
        }
        //从1开始遍历，0行是标题，因此1代表第一行数据,是从下标0开始的
        for (int i = 1; i <= lastRowNum; i++) {
            int j;
            //第i行数据
            Row row = sheet.getRow(i);
            j = 1;
            //第一行第0列数据
            String policeNum = Constants.getStringCellValue(row.getCell(j - 1));
            //通过数据库查询是否存在该账号，如果存在直接跳过
            User user = userMapper.findUserByPoliceNumber(policeNum);
            if (user != null) {
                //有该用户，直接跳过，读取下一行（即: 下一个用户）
                continue;
            }
            j++;
            String name = Constants.getStringCellValue(row.getCell(j - 1));
            j++;
            String idNum = Constants.getStringCellValue(row.getCell(j - 1));
            //校验身份证是否合法
            checkLegal.checkLegalByIdNum(idNum, i + 1, j);
            j++;
            String phoneNum = Constants.getPhoneNumber(row.getCell(j - 1));
            checkLegal.checkLegalByPhone(phoneNum, i + 1, j);
            j++;
            String userType = Constants.getStringCellValue(row.getCell(j - 1));
            //判断输入的账户类型是否合法
            checkLegal.checkLegalByUserType(userType, i + 1, j);
            //账户类型合法继续执行
            Integer roleId = RoleEnum.getEnumByKey(userType).getValue();
            j++;
            String departmentId = Constants.getStringCellValue(row.getCell(j - 1));
            //检查部门是否存在，如不存在抛出提示先增加部门
            Long departmentIdByResult = checkLegal.checkLegalByDepartmentId(departmentId, i + 1, j);
            j++;
            String dogNum = Constants.getStringCellValue(row.getCell(j - 1));
            AddUserBean userBean = new AddUserBean(policeNum, name, idNum, phoneNum, roleId, departmentIdByResult, dogNum, Utils.getPassword("123456"), 1);
            userBeans.add(userBean);
        }
        System.out.println(userBeans);
        //往数据库批量加入数据
        userMapper.addUsersOfBatch(userBeans);
        return "导入成功共计" + userBeans.size() + "条信息";
    }

    @Override
    public User findUserByPoliceNumber(String policeNumber) {
        return userMapper.findUserByPoliceNumber(policeNumber);
    }

    @Override
    public void addUserLicense(Map<String,Object> userLicenses, String policeNumber, String unitCode, String ip) {
        if (getInteger(userLicenses.get("userId")) == null || getLong(userLicenses.get("expireTime")) < System.currentTimeMillis()) {
            throw new RuntimeException("数据非法");
        }
        List<String> departmentCodes = (List<String>) userLicenses.get("departmentCodes");
        if(departmentCodes==null||departmentCodes.size()<=0){
            throw new RuntimeException("请选择需要添加的权限单位");
        }
        //判断该用户是否拥有添加单位的权限
        User user = userMapper.findById((int)userLicenses.get("userId"));
        List<String> userDepartCodeChildren = departmentMapper.findDepartmentCodeListByCode(com.hnf.honeycomb.util.StringUtils.getOldDepartmentCode(user.getDepartmentCode()));
        //要添加的单位不是自己单位的子单位就报错
        List<String> departmentBeans = departmentMapper.findDepartmentCodeListByCode(com.hnf.honeycomb.util.StringUtils.getOldDepartmentCode(unitCode));
        for (String departmentCode : departmentCodes) {
            if(!departmentBeans.contains(departmentCode)){
                throw new RuntimeException("请选择自己单位下的子单位！");
            }
            if(userDepartCodeChildren.contains(departmentCode)){
                throw new RuntimeException("该用户已经拥有该单位权限！");
            }
        }

        insertModifyLog(policeNumber, unitCode, ip, 4, "新增临时权限", userLicenses);
        userAuthMapper.addUserLicense(userLicenses);
    }

    @Override
    public void updateUserLicense(UserLicenseBean userLicenseBean, String policeNumber, String unitCode, String ip) {
        if (userLicenseBean.getId() == null || userLicenseBean.getUserId() == null || userLicenseBean.getExpireTime() < System.currentTimeMillis()) {
            throw new RuntimeException("数据非法");
        }
        UserLicenseBean old = userAuthMapper.findUserLicensesById(userLicenseBean.getId());
        if (!old.equals(userLicenseBean)) {
            BuilderMap<String, Object> log = BuilderMap.of(String.class, Object.class);
            if (!old.getExpireTime().equals(userLicenseBean.getExpireTime())) {
                log.put("expire", BuilderMap.of(String.class, Object.class).put("From", old.getExpireTime()).put("To", userLicenseBean).get());
            }
            if (!old.getDepartmentCode().equals(userLicenseBean.getDepartmentCode())) {
                log.put("unitCode", BuilderMap.of("From", old.getDepartmentCode()).put("To", userLicenseBean.getDepartmentCode()));
            }
            insertModifyLog(policeNumber, unitCode, ip, 6, "修改临时权限", log);
            userAuthMapper.updateUserLicense(userLicenseBean);
        }
    }

    @Override
    public void deleteUserLicense(Integer id, String policeNumber, String unitCode, String ip) {
        UserLicenseBean old = userAuthMapper.findUserLicensesById(id);
        if (old == null) {
            throw new RuntimeException("查无数据");
        }
        insertModifyLog(policeNumber, unitCode, ip, 5, "删除用户临时权限", old);
        userAuthMapper.deleteUserLicense(id);
    }

    @Override
    public List<UserLicenseBean> findUserLicensesByUserId(Long userId) {
        return userAuthMapper.findUserLicensesByUserId(userId);
    }
    /**
     * 根据文件类型返回不同的WorkBook对象
     *
     * @param file 前端传入的文件
     * @return 返回 WorkBook 对象
     */
    private Workbook getWorkBookByInputStream(MultipartFile file) {
        InputStream is;
        Workbook wb = null;
        try {
            is = file.getInputStream();
            /*
             * 通过文件后缀名称判断excel 属于新版还是老版
             * .xlsx 后缀是高级版本，要用XSSFWorkbook对象解析
             * .xls 后缀是低版本，使用HSSFWorkbook 解析
             */
            if (file.getOriginalFilename() != null && file.getOriginalFilename().contains(XLSX)) {
                wb = new XSSFWorkbook(is);
            }
            if (file.getOriginalFilename() != null && file.getOriginalFilename().contains(XLS)) {
                wb = new HSSFWorkbook(is);
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("文件格式错误--{}----");
        }
        return wb;
    }

    /**
     * 激活用户
     *
     * @param userId 用户ID
     * @param place  登录IP
     */
    @Override
    public UserBean activate(Integer userId, String place) {
        return null;
    }

    /**
     * 修改密码
     *
     * @param userid      用户ID
     * @param oldPassword 旧密码
     * @param confirm     确认新密码
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updatePassword(Integer userid, String oldPassword, String newPassword, String confirm) {
        final LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        if (Stream.of(oldPassword, newPassword, confirm).anyMatch(StringUtils::isBlank)) {
            throw IllegalInputException.of("密码不能为空");
        }
        newPassword = newPassword.trim();
        confirm = confirm.trim();

        if (!newPassword.equals(confirm)) {
            throw IllegalInputException.of("密码不一致", "3");
        }
        Optional.of(Optional.ofNullable(userMapper.findById(userid)).orElseThrow(() ->
                IllegalInputException.of("id为 " + userid + " 的用户不存在")
        )).filter(u -> u.getPassword().equals(Utils.getPassword(oldPassword))).orElseThrow(() ->
                IllegalInputException.of("旧密码错误", "1")
        );

        final String encryptedNewPassword = Utils.getPassword(newPassword);
        LinkedHashMap<String, Object> param = new LinkedHashMap<>();
        param.put("password", encryptedNewPassword);
        param.put("userid", userid);
        userMapper.updatePassword(param);
        result.put("type", "2");
        result.put("message", "修改成功");
        return result;
    }


    /**
     * 通过身份证号查找用户
     *
     * @param idNumber 身份证号
     */
    @Override
    public User findByidNumber(String idNumber) {
        return userMapper.findByidNumber(this.encode3Des(idNumber));
    }


    @Override
    public boolean delete(Integer userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("要删除的账号不存在");
        }
        // 删除用户专项案件标签库
        userAuthMapper.deleteUserCaseType(user.getUserId());
        userAuthMapper.deleteUserLicenseByUserId(user.getUserId());
        userMapper.delete(user.getPoliceNumber());
        return true;
    }

    @Override
    public Page<User> findAll(Integer pageNum, Integer pageSize, String policeNumberAndNickName, String departmentCode,
                              String activate, String nickName, String roleId) {
        pageNum = Optional.ofNullable(pageNum).orElse(1);
        pageSize = Optional.ofNullable(pageSize).orElse(DEFAULT_PAGE_SIZE);
        Map<String, Object> userMap = new HashMap<>(8);
        Map<String, Object> countMap = new HashMap<>(8);
        if (!com.hnf.honeycomb.util.StringUtils.isEmpty(policeNumberAndNickName)) {
            userMap.put("policenumberAndNickName", policeNumberAndNickName);
            countMap.put("policenumberAndNickName", policeNumberAndNickName);
        }
        if (!com.hnf.honeycomb.util.StringUtils.isEmpty(departmentCode)) {
            departmentCode = com.hnf.honeycomb.util.StringUtils.getOldDepartmentCode(departmentCode);
            userMap.put("departCode", departmentCode);
            countMap.put("departCode", departmentCode);
        }
        if (!com.hnf.honeycomb.util.StringUtils.isEmpty(departmentCode) && "51".equals(departmentCode)) {
            userMap.put("departCode", null);
            countMap.put("departCode", null);
        }
        if (!com.hnf.honeycomb.util.StringUtils.isEmpty(activate)) {
            userMap.put("activate", activate);
            countMap.put("activate", activate);
        }
        if (!com.hnf.honeycomb.util.StringUtils.isEmpty(nickName)) {
            userMap.put("activate", nickName);
            countMap.put("activate", nickName);
        }
        if (!com.hnf.honeycomb.util.StringUtils.isEmpty(roleId)) {
            if(Long.valueOf(roleId) == 3){
//                userMap.put("roleId", new int[]{3,2,1});
//                countMap.put("roleId", new int[]{3,2,1});
            }else if(Long.valueOf(roleId) == 2){
                userMap.put("roleId", new int[]{3});
                countMap.put("roleId", new int[]{3});
            }
        }
        userMap.put("start", (pageNum - 1) * pageSize);
        userMap.put("end", pageSize);
        int count = userMapper.findCount(countMap);
        final List<User> list = userMapper.findAll(userMap);
        if (!CollectionUtils.isEmpty(list)) {
            for (User user : list) {
                if (StringUtils.isNotBlank(user.getDepartmentCode())) {
                    String newDepartName = userDepartmentService.findWholeDepartmentNameByDepartCode(user.getDepartmentCode());
                    user.setDepartmentName(newDepartName);
                }
            }
            return new PageImpl<>(list, PageRequest.of(pageNum - 1, pageSize), count);
        } else {
            return new PageImpl<>(new LinkedList<>(), PageRequest.of(pageNum - 1, pageSize), count);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User updatePower(Integer userId, String password, String nickname, Integer roleId
            , String departmentCode, String remark, String idNumber
            , String phoneNumber, String vphone, List<Integer> caseTypeIds
            , String operatePolice, String operateUnit, String operateIp) {
        User elderUser = userMapper.findById(userId);
        ResidentIdentityCardBean encodedIdNumber = ResidentIdentityCardBean.ofRaw(idNumber);
        if (elderUser != null) {
            Map<String,Object> map = new HashMap<>(2);
            map.put("IDNumber", this.encode3Des(idNumber));
            map.put("userId", userId);
            Optional.ofNullable(userMapper.findByidNumberAndId(map)).ifPresent(o -> {
                throw new IllegalStateException("身份证号已存在");
            });
            Map<String, Object> toUpdate = new HashMap<>(11);
            Map<String, Object> operate = new HashMap<>(11);
            toUpdate.put("userid", userId);
            operate.put("userId", userId);
            operate.put("policeNumber", elderUser.getPoliceNumber());
            /* Role */
            toUpdate.put("roleId", Optional.ofNullable(roleId).orElse(elderUser.getRoleId()));
            if (roleId != null && !roleId.equals(elderUser.getRoleId())) {
                operate.put("roleId", BuilderMap.of(String.class, Object.class).put("From", elderUser.getRoleId()).put("To", roleId).get());
            }
            /* Password */
            password = password == null || "".equals(password) ? "" : password;
            String md5 = Utils.getPassword(password.trim());
            if (StringUtils.isBlank(password)) {
                toUpdate.put("password", elderUser.getPassword());
            } else {
                toUpdate.put("password", md5);
                operate.put("password", "update");
            }

            /* Department */
            if (StringUtils.isNotBlank(departmentCode)) {
                DepartmentBean depart = userDepartmentService.findByCode(departmentCode.trim());
                if (depart == null) {
                    throw IllegalInputException.of("指定的新departmentCode不存在该对应的部门");
                } else {
                    toUpdate.put("departmentId", depart.getDepartmentId());
                    if (!departmentCode.equals(elderUser.getDepartmentCode())) {
                        operate.put("unit", BuilderMap
                                .of("From", elderUser.getDepartmentCode() + elderUser.getDepartmentName())
                                .put("To", depart.getDepartmentCode() + depart.getDepartmentName()).get());
                    }
                }
            } else {
                toUpdate.put("departmentId", elderUser.getDepartmentId());
            }
            List<Integer> caseTypes = userAuthMapper.findCaseTypeIdsByUserId(elderUser.getUserId());
            if (caseTypeIds != null && (!caseTypeIds.containsAll(caseTypes) || !caseTypes.containsAll(caseTypeIds))) {
                operate.put("caseTypes", BuilderMap.of(String.class, Object.class).put("From", caseTypes).put("To", caseTypeIds).get());
                userAuthMapper.updateUserCaseType(elderUser.getUserId(), caseTypeIds);
            }
            /*Others*/
            toUpdate.put("nickname", Optional.ofNullable(nickname).orElse(elderUser.getNickname()).trim());
            toUpdate.put("remark", Optional.ofNullable(remark).orElse(elderUser.getRemark()));
            toUpdate.put("IDnumber", Optional.ofNullable(encodedIdNumber).orElse(elderUser.getIDnumber()));
            toUpdate.put("phonenumber", Optional.ofNullable(phoneNumber).orElse(elderUser.getPhone()));
            toUpdate.put("vphone", Optional.ofNullable(vphone).orElse(elderUser.getPhoneNumber()));
//记录用户操作日志
            if (nickname != null && !nickname.equals(elderUser.getNickname())) {
                operate.put("nickname", BuilderMap.of("From", elderUser.getNickname()).put("To", nickname).get());
            }
            if (remark != null && !remark.equals(elderUser.getRemark())) {
                operate.put("remark", BuilderMap.of("From", elderUser.getRemark()).put("To", remark).get());
            }
            if(idNumber != null && elderUser.getIDnumber() == null){
                operate.put("idNumber", BuilderMap.of("From", "null").put("To", idNumber).get());
            }else if (idNumber != null && !idNumber.equals(decode3Des(elderUser.getIDnumber().getValue()))) {
                operate.put("idNumber", BuilderMap.of("From", decode3Des(elderUser.getIDnumber().getValue())).put("To", idNumber).get());
            }
            if (phoneNumber != null && !phoneNumber.equals(elderUser.getPhoneNumber())) {
                operate.put("phoneNumber", BuilderMap.of("From", elderUser.getPhoneNumber()).put("To", phoneNumber).get());
            }
            if (vphone != null && !vphone.equals(elderUser.getPhone())) {
                operate.put("vPhone", BuilderMap.of("From", elderUser.getPhone()).put("To", vphone).get());
            }
            userMapper.updateOne(toUpdate);
            insertModifyLog(operatePolice, operateUnit, operateIp, 3, "修改用户", operate);

            //修改完后的user
            final User saved = userMapper.findById(userId);
            final DepartmentBean userNewDepartment = userDepartmentService.findByDepartmentId(saved.getDepartmentId());
            updateFetchToMongoDB(userNewDepartment.getDepartmentCode(), saved.getPoliceNumber());
            return saved;
        }
        throw new NoSuchUserException("未找到Id=" + userId + "的待修改用户");
    }

    @Override
    public User findById(Integer userid) {
        // TODO Auto-generated method stub
        return userMapper.findById(userid);
    }

    @Override
    public List<User> findByName(String username) {

        return userMapper.findByName(username);
    }

    @Override
    public String loginMD5(String policenumber, String date) {
        return Utils.getMD5(policenumber.trim(), date.trim());
    }

    private String loginMD5(String policenumber, Instant date) {
        return Utils.getMD5(
                policenumber.trim(),
                LocalDateTime.ofInstant(date, ZoneId.systemDefault()).format(DEFAULT_DATE_TIME_FORMATTER)
        );
    }

//    private UserTokenBean updateToken(String policenumber, String place, Long loginTimeMillis) {
//        // TODO Auto-generated method stub
//
//        final String token = loginMD5(policenumber, Instant.ofEpochMilli(loginTimeMillis));
//        Map<String, Object> map = new HashMap<>();
//        map.put("policeNumber", policenumber.trim());
//        map.put("token", token);
//        map.put("ipaddress", place.trim());
//        map.put("logintime", loginTimeMillis);
//        dao.updateToken(map);
//        Optional.ofNullable(userTokenDao.findTokenByName(policenumber)).ifPresent(saved -> {
//            userTokenDao.deletePolice(saved.getPoliceNumber());
//        });
//        userTokenDao.add(map);
//        return userTokenDao.findNameByToken(token.trim());
//    }

    @Override
    public User findByUserId(Integer userid) {
        // TODO Auto-generated method stub
        return userMapper.findById(userid);
    }

//    @Override
//    public void registOne(String policenumber, List<Integer> softDog) {
//        User one = dao.findUserInfoByPoliceNumber(policenumber.trim());
//
//        Optional.ofNullable(dao.findUserInfoByPoliceNumber(StringUtils.trim(policenumber)))
//                .ifPresent(u -> {
//                    Optional.ofNullable(softDog).orElse(new LinkedList<>()).stream()
//                            .filter(Objects::nonNull)
//                            .map(Objects::toString)
//                            .map(softDogDao::findBySoftDogByNumber)
//                            .filter(Objects::nonNull)
//                            .forEachOrdered(
//                                    aSoftDog -> {
//                                        Map<String, Object> newMap = new HashMap<>();
//                                        newMap.put("softDogNumber", aSoftDog.getSoftDogNumber());
//                                        newMap.put("policeNumber", u.getPoliceNumber());
//                                        userAndSoftdogDao.add(newMap);
//                                    }
//                            );
//                });
//    }


    @Override
    public User findByPoliceNumber(String policenumber) {
        return userMapper.findUserInfoByPoliceNumber(policenumber);
    }

    @Override
    public List<User> findByDepartmentId(Long departmentId) {
        return userMapper.findByDepartmentId(Collections.singletonMap("departmentId", departmentId));
    }

    @Override
    public User logout(Integer userId, String place) {
        User bean = userMapper.findById(userId);
        if (bean == null) {
            return null;
        }

        RoleBean roleBean = roleDao.findByRoleId(bean.getRoleId());

        if (roleBean != null) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("policeNumber", bean.getPoliceNumber().trim());
            map.put("logoutTime", new Date());
            map.put("roleName", roleBean.getRoleName());
            map.put("logoutIp", place);
            map.put("logoutUnitType", bean.getDepartmentCode());
            map.put("logoutState", "用户注销成功");
            userMongoDao.insertOperationDocument("logData", "userlogout", map);
        }
        return bean;
    }

    @Override
    public User activate(Integer userId, Integer activate, String place) {

        Map<String, Object> mapInfo = new HashMap<>();

        mapInfo.put("userId", userId);
        mapInfo.put("activate", activate);

        userMapper.updateActivate(mapInfo);

        User bean = userMapper.findById(userId);

        if (bean == null) {
            return null;
        }

        RoleBean roleBean = roleDao.findByRoleId(bean.getRoleId());

        HashMap<String, Object> map = new HashMap<>(7);
        map.put("policeNumber", bean.getPoliceNumber().trim());
        map.put("activateTime", new Date());
        System.out.println("roleBean:" + roleBean);
        map.put("roleName", roleBean.getRoleName().trim());
        map.put("activateIp", place);
        map.put("activateState", "激活成功");
        map.put("activateUnitType", bean.getDepartmentCode());
        map.put("activateType", 1);
        userMongoDao.insertOperationDocument("logData", "activate", map);

        return bean;
    }

    public void findParentDepart(Map<String, DepartmentBean> canCheckAllDepart, String t) {
        DepartmentBean depart = userDepartmentService.findByCode(t);
        String departCode = depart.getDepartmentCode();
        Integer departType = depart.getDepartmentType().toDBValue();
        canCheckAllDepart.put(departType.toString(), depart);
        String newDepartCode;
        //查询上一级
        while (departType > 1) {
            switch (departType) {
                //对于最下一级单位
                case 4:
                    newDepartCode = departCode.substring(0, 6);
                    if (newDepartCode.endsWith("00")) {
                        newDepartCode = departCode.substring(0, 8);
                    }
                    depart = userDepartmentService.findByCode(newDepartCode);
                    departType = depart.getDepartmentType().toDBValue();
                    departCode = depart.getDepartmentCode();
                    canCheckAllDepart.put(departType.toString(), depart);
                    break;
                //对于是县级或者支队级
                case 3:
                    newDepartCode = departCode.substring(0, 4);
                    depart = userDepartmentService.findByCode(newDepartCode);
                    departType = depart.getDepartmentType().toDBValue();
                    departCode = depart.getDepartmentCode();
                    canCheckAllDepart.put(departType.toString(), depart);
                    break;
                //对于市级
                case 2:
                    newDepartCode = departCode.substring(0, 2);
                    depart = userDepartmentService.findByCode(newDepartCode);
                    departType = depart.getDepartmentType().toDBValue();
                    departCode = depart.getDepartmentCode();
                    canCheckAllDepart.put(departType.toString(), depart);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public Map<String, Object> findOperation(Integer userId, Integer operationId) {
        return Optional.ofNullable(
                userMapper.findById(userId)
        ).map(
                User::getRoleId
        ).map(
                r -> roleOperationDao.find(((Supplier<Map<String, Object>>) () -> {
                    final LinkedHashMap<String, Object> temp = new LinkedHashMap<>();
                    temp.put("roleId", r);
                    temp.put("operationId", operationId);
                    return temp;
                }).get()).stream().findFirst().map(roleOperationToMap).orElse(new LinkedHashMap<>())
        ).orElse(new LinkedHashMap<>());
    }

    @Override
    public List<User> findUserByDepart(String departmentCode) {
        return Optional.ofNullable(departmentCode).map(
                userDepartmentService::findByCode
        ).map(
                DepartmentBean::getDepartmentId
        ).map(
                id -> Collections.singletonMap("departmentId", (Object) id)
        ).map(userMapper::findByDepartmentId).orElse(new LinkedList<>());
    }

    //修改对应用户的警号@FunctionalInterface

    @Override
    public Integer updatePoliceNumber(Integer userId, String policenumber) {
        if (userId == null) {
            throw new RuntimeException("对应的用户ID为空");
        }
        if (StringUtils.isEmpty(policenumber)) {
            throw new RuntimeException("对应的新警号为空");
        }
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("对应的用户为空");
        }
        Map<String, Object> para = new HashMap<>();
        User updateUserInfo = userMapper.findUserByPoliceNumber(policenumber);
        //若此警号不存在对应的用户，则直接将原有单位修改为对应的警号
        if (updateUserInfo == null) {
            para.put("userId", userId);
            para.put("policeNumber", policenumber.trim());
            userMapper.updatePoliceNumberByPID(para);
            return 1;
        }
        para.put("departId", user.getDepartmentId());
        para.put("roleId", user.getRoleId());
        para.put("policeNumber", policenumber.trim());
        //修改对应的已存在用户的ID
        userMapper.updateUserDIdAndRIdByPoliceNumber(para);
        //修改完成后删除对应的用户
        userMapper.delete(String.valueOf(userId));
        return 1;
    }

    /**
     * 修改用户表的部门代码
     */
    @Override
    public void updateUserDepartType(Long newDepartmentId, Long oldDepartmentId) {
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("newDepartmentId", newDepartmentId);
        query.put("oldDepartmentId", oldDepartmentId);
        userMapper.updateUserDepartType(query);
    }

    @Valid
    private void updateFetchToMongoDB(@NotNull String departmentCode, @NotNull String policenumber) {
        userMongoDao.updataFetchByKeyId(policenumber.trim(), departmentCode.trim());
    }

    /**
     * 统计用户错误输入密码的次数
     *
     * @return
     * @ policenumber 警号
     */
    private Long countErrorPassword(String policeNumber, String place) {
        Long count = 0L;
        Long times = System.currentTimeMillis() - jwtField.getTimeLength();
        BasicDBObject query = new BasicDBObject("loginTime"
                , new BasicDBObject("$lte", new Date()).append("$gte", new Date(times)));
        query.append("policeNumber", policeNumber);
        query.append("loginIp", place);
        BasicDBObject sort = new BasicDBObject("loginTime", -1);
        List<Document> loginRecord = baseDao.listQuery("logData", "userlogin", query, sort);
        Integer success = 1;
        for (Document document : loginRecord) {
            if (success.equals(document.getInteger("loginType"))) {
                break;
            }
            count++;
        }
        return count;

        /*BasicDBObject queryActivate = new BasicDBObject();
        queryActivate.append("policeNumber", policeNumber);
        queryActivate.append("activateTime", timeQuery);
        BasicDBObject sort = new BasicDBObject("activateTime", -1);
        List<DBObject> activateBeans = userMongoDao.findActiveTime("logData", "activate", queryActivate, sort, 1);
        BasicDBObject queryLogin = new BasicDBObject();
        queryLogin.append("policeNumber", policeNumber);
        if (activateBeans.size() == 0) {
            queryLogin.append("loginTime", timeQuery);
            queryLogin.append("loginIp", place);
            *//*queryLogin.append("loginType", 2);
            List<DBObject> loginList = userMongoDao.find("logData", "userlogin", queryLogin, 1, 1, new BasicDBObject("loginTime", -1));
            DBObject dbObject = loginList.get(0);
            Date loginTime = (Date)dbObject.get("loginTime");
            timeQuery = new BasicDBObject();
            timeQuery.append("$gte",loginTime.getTime() > times?loginTime.getTime():times);
            timeQuery.append("$lte", new Date(System.currentTimeMillis()));
            queryLogin = new BasicDBObject();
            queryLogin.append("policeNumber", policenumber);
            queryLogin.append("loginTime", timeQuery);
            queryLogin.append("loginIp", place);*//*
            count = userMongoDao.count("logData", "userlogin", queryLogin);
            return count;
        }
        String activeTime = activateBeans.get(0).get("activateTime").toString();
        timeQuery.append("$gte", new Date(Util.dateToString(activeTime)));
        queryLogin.append("loginTime", timeQuery);
        queryLogin.append("loginType", 2);
        count = userMongoDao.count("logData", "userlogin", queryLogin);
        return count;*/
    }

    /**
     * 账号冻结
     *
     * @param policeNumber 警号
     */
    private void userFrozen(String policeNumber) {
        Map<String,Object> userMap = new HashMap<>(2);
        userMap.put("activate", 2);
        userMap.put("policeNumber", policeNumber);
        userMapper.updateActivate(userMap);
    }

    public void insertLoginLog(String policenumber, String roleName, String loginIp, String loginArea, String loginState, Long loginUnitType, Integer loginType) {
        HashMap<String, Object> loginMap = new HashMap<>(8);
        loginMap.put("policeNumber", policenumber);
        loginMap.put("loginTime", new Date());
        loginMap.put("roleName", roleName);
        loginMap.put("loginIp", loginIp);
        loginMap.put("loginArea", loginArea);
        loginMap.put("loginState", loginState);
        loginMap.put("loginUnitType", loginUnitType);
        loginMap.put("loginType", loginType);
        userMongoDao.insertOperationDocument("logData", "userlogin", loginMap);
    }

    @Override
    public void updateLastActiveyTime(String policeNumber) {
        Map<String, Object> updateLastActivityPara = new HashMap<>(2);
        updateLastActivityPara.put("policeNumber", policeNumber);
        updateLastActivityPara.put("lastActivityTime", System.currentTimeMillis());
        userMapper.updateLastActiveyTimeByPoliceNum(updateLastActivityPara);
    }

    @Override
    public String getToken(Map<String, Object> map) {
        String token = null;
        try {
            token = jwtService.createJWT(map, jwtField.getTtlMillis());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return token;
    }

    @Override
    public List<Object> findOperation(Long roleId) {
        List<Object> operation = new ArrayList<>();
        List<RoleOperationBean> roleOperationBeanList = roleOperationDao.findOperation(roleId);
        roleOperationBeanList.forEach(t -> operation.add(t.getOperationId()));
        return operation;
    }
    @Override
    public void insertModifyLog(String operatePolice, String operateUnit, String operateIp, int operateType, String operateName, Object operate) {
        Map<String, Object> log
                = BuilderMap.of(String.class, Object.class)
                .put("operateTime", new Date())
                .put("policeNumber", operatePolice)
                .put("operateUnit", operateUnit)
                .put("operateIp", operateIp)
                .put("operateType", operateType)
                .put("operateName", operateName)
                .put("operate", operate instanceof Map ? operate : new Document(JSONObject.parseObject(JSONObject.toJSONString(operate)))).get();
        userMongoDao.insertOperationDocument("logData", "userModify", log);
    }
}
