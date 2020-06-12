package com.hnf.honeycomb.serviceimpl.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hnf.honeycomb.bean.DepartmentBean;
import com.hnf.honeycomb.bean.User;
import com.hnf.honeycomb.bean.UserLicenseBean;
import com.hnf.honeycomb.bean.enumerations.DepartmentType;
import com.hnf.honeycomb.bean.exception.IllegalDeletingOperationException;
import com.hnf.honeycomb.bean.exception.IllegalInputException;
import com.hnf.honeycomb.bean.exception.IllegalUpdatingOperationException;
import com.hnf.honeycomb.config.ProjectLevelConstants;
import com.hnf.honeycomb.dao.UserMongoDao;
import com.hnf.honeycomb.remote.user.DepartmentMapper;
import com.hnf.honeycomb.remote.user.UserAuthMapper;
import com.hnf.honeycomb.remote.user.UserMapper;
import com.hnf.honeycomb.service.user.UserDepartmentService;
import com.hnf.honeycomb.util.CollectionUtils;
import com.hnf.honeycomb.util.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * @author zhouhong
 * @ClassName UserDepartmentServiceImpl
 * @date 2018年6月26日 上午10：38：15
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class UserDepartmentServiceImpl implements UserDepartmentService {

    private static final Function<String, Map<String, Object>> CODE_TO_ALL_CHILDREN_QUERY = departmentCode -> {
        Map<String, Object> allChildrenQuery = new LinkedHashMap<>();
        allChildrenQuery.put("departmentCode", departmentCode);
        allChildrenQuery.put("departmentType", Arrays.stream(DepartmentType.values()).map(DepartmentType::toDBValue).collect(Collectors.toList()));
        return allChildrenQuery;
    };

    @Resource
    private DepartmentMapper departmentMapper;
    @Resource
    private UserAuthMapper userAuthMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserMongoDao userMongoDao;

    /**
     * 通过部门ID查找部门
     *
     * @param departmentId 部门ID
     * @return DepartmentBean
     */
    @Override
    public DepartmentBean findByDepartmentId(Long departmentId) {
        return departmentMapper.findByDepartmentId(departmentId);
    }

    @Override
    public Map<String, Object> delete(Long departmentId) {
        Map<String, Object> result = new HashMap<>();
        DepartmentBean bean = departmentMapper.findByDepartmentId(departmentId);
        if (bean == null) {
            result.put("state", "所检索的部门不存在");
            result.put("type", "2");
            return result;
        }
        boolean b = bean.getDepartmentCode().endsWith("00000");
        if (b) {
            String code = com.hnf.honeycomb.util.StringUtils.getOldDepartmentCode(bean.getDepartmentCode());
            List<DepartmentBean> childDepartmentByCodeAndTypes = departmentMapper.findDepartmentCountByCode(code);
            if (childDepartmentByCodeAndTypes.size() > 1) {
                throw new RuntimeException("不能删除有子单位的单位；");
            }
        }
        return delete(bean.getDepartmentCode());
    }

    @Override
    public Map<String, Object> update(Long departmentId, String newName, String newCode) {

        DepartmentBean currentDepartment = departmentMapper.findByDepartmentId(departmentId);

        if (currentDepartment == null) {
            throw IllegalInputException.of("要修改的部门不存在，请该用新增接口", "2");
        }
        newCode = com.hnf.honeycomb.util.StringUtils.getNewDepartmentCode(newCode);
        Optional.ofNullable(departmentMapper.findByDepartmentCode(newCode)).filter(d ->
                !d.getDepartmentCode().equals(currentDepartment.getDepartmentCode())
        ).ifPresent(d -> {
            throw IllegalInputException.of("新code与存在的部门" + d.getDepartmentName() + "冲突", "3");
        });

        if (StringUtils.isBlank(newCode) || newCode.equals(currentDepartment.getDepartmentCode())) {
            newCode = currentDepartment.getDepartmentCode();
        } else {
            Optional.of(currentDepartment).ifPresent(d -> {
                final String departmentCode = d.getDepartmentCode();
                Map<String, Object> allChildrenQuery = CODE_TO_ALL_CHILDREN_QUERY.apply(departmentCode);
                if (departmentMapper.findChildDepartmentByCodeAndTypes(allChildrenQuery).stream()
                        .anyMatch(dbv -> !dbv.getDepartmentId().equals(currentDepartment.getDepartmentId()))) {
                    throw IllegalUpdatingOperationException.of("存在下级单位，不能修改", "4");
                }
            });
        }

        Map<String, Object> toUpdate = new HashMap<>();
        toUpdate.put("departmentName", newName.trim());
        toUpdate.put("departmentCode", newCode);
        toUpdate.put("departmentId", departmentId);

        departmentMapper.update(toUpdate);
        Map<String, Object> result = new HashMap<>();
        userMongoDao.updateFetchByDoc(newCode, currentDepartment.getDepartmentCode());
        result.put("type", "0");
        result.put("state", "修改成功");
        return result;
    }

    @Override
    public DepartmentBean findByName(String departmentName, Long unitType) {
        Map<Object, Object> map = new HashMap<>();
        map.put("departmentName", departmentName.trim());
        return departmentMapper.findByName(map);
    }

    @Override
    public Map<String, Object> add(@NotBlank String departmentName, @NotBlank String departmentCode1, String parentDepartmentCode1) {
        //补全parentDepartmentCode
        String parentDepartmentCode = com.hnf.honeycomb.util.StringUtils.getNewDepartmentCode(parentDepartmentCode1);
        final DepartmentBean parent = Optional.ofNullable(parentDepartmentCode).map(departmentMapper::findByDepartmentCode).orElse(null);
        int a = 2;
        if (parent == null && departmentCode1.length() > a) {
            throw IllegalInputException.of("指定的父单位不存在");
        }
        DepartmentType departmentType;
        final DepartmentType parentType = parent != null ? parent.getDepartmentType() : DepartmentType.NATION;
        switch (parentType) {
            case NATION:
                if (!ProjectLevelConstants.PROVINCE_CODE_PREDICATE.test(departmentCode1)) {
                    throw IllegalInputException.of("单位格式不符合预期规则", "1");
                }
                departmentType = DepartmentType.PROVINCE;
                break;
            case PROVINCE:
                if (!ProjectLevelConstants.CITY_CODE_PREDICATE.test(departmentCode1)) {
                    throw IllegalInputException.of("单位格式不符合预期规则", "1");
                }
                //市
                departmentType = DepartmentType.CITY;
                break;
            case CITY:
                if (!ProjectLevelConstants.COUNTY_AND_TEAM_CODE_PREDICATE.test(departmentCode1)) {
                    throw IllegalInputException.of("单位格式不符合预期规则", "1");
                }
                //区县，支队
                departmentType = DepartmentType.COUNTY;
                break;
            default:
                if (!ProjectLevelConstants.TOWN_DEPARTMENT_CODE_PREDICATE.test(departmentCode1)) {
                    throw IllegalInputException.of("单位格式不符合预期规则", "1");
                }
                //派出所
                departmentType = DepartmentType.TOWN;
                break;
        }
        Map<String, Object> map = new LinkedHashMap<>();
        String departmentCode = com.hnf.honeycomb.util.StringUtils.getNewDepartmentCode(departmentCode1);
        DepartmentBean dBean = departmentMapper.findByDepartmentCode(departmentCode);
        if (dBean != null) {
            throw IllegalInputException.of("单位已存在", "3");
        }
        DepartmentBean departmentBean = new DepartmentBean(departmentName.trim(), departmentCode, departmentType);


        ObjectMapper mapper = new ObjectMapper();
        try {
            final Map toSave = mapper.readValue(mapper.writeValueAsString(departmentBean), Map.class);
            departmentMapper.add(toSave);
            map.put("state", "新增成功");
            map.put("type", "0");
            return map;
        } catch (IOException e) {
            e.printStackTrace();
            map.put("state", "新增失败");
            map.put("type", "1");
            return map;
        }
    }

    @Override
    public Map<String, Object> delete(String departmentCode) {
        DepartmentBean bean = departmentMapper.findByDepartmentCode(departmentCode);
        Map<String, Object> map = new LinkedHashMap<>();
        if (bean != null) {
            List<User> user = userMapper.findByDepartmentId(Collections.singletonMap("departmentId", bean.getDepartmentId()));
            List<UserLicenseBean> license = userAuthMapper.findUserLicensesByUserUnitCode(departmentCode);
            List<DepartmentBean> children = departmentMapper.findChildDepartmentByCodeAndTypes(CODE_TO_ALL_CHILDREN_QUERY.apply(departmentCode));
            if (Stream.of(user, license).anyMatch(l -> !l.isEmpty())) {
                throw IllegalDeletingOperationException.of("有数据不能删除", "1");
            }
            if (children.stream().anyMatch(c -> !c.getDepartmentCode().equals(departmentCode))) {
                throw IllegalDeletingOperationException.of("存在下级部门，不能删除", "2");
            }
            departmentMapper.delete(bean.getDepartmentCode());
            map.put("state", "删除成功");
            map.put("type", "0");
        }
        return map;
    }

    @Override
    public List<DepartmentBean> findParentDepart(String departmentCode) {
        List<DepartmentBean> list = null;
        departmentCode = com.hnf.honeycomb.util.StringUtils.getNewDepartmentCode(departmentCode);
        Map<String, DepartmentBean> map = new HashMap<>();
        DepartmentBean depart = departmentMapper.findByDepartmentCode(departmentCode);
        if (depart == null) {
            return list;
        }
        String departCode = depart.getDepartmentCode();
        Integer departType = depart.getDepartmentType().toDBValue();
        map.put(departType.toString(), depart);
        String newDepartCode;
        //查询上一级
        while (departType > 1) {
            switch (departType) {
                //对于最下一级单位
                case 4:
                    // 51010A 03001
                    newDepartCode = departCode.substring(0, 6);
                    if (newDepartCode.endsWith("00")) {
                        newDepartCode = departCode.substring(0, 8);
                        newDepartCode = newDepartCode + "000";
                    } else {
                        newDepartCode = newDepartCode + "00000";
                    }
                    depart = departmentMapper.findByDepartmentCode(newDepartCode);
                    if (depart == null) {
                        departType --;
                        System.out.println("没有上级部门的单位代码："+departCode);
                    }else {
                        departType = depart.getDepartmentType().toDBValue();
                        map.put(departType.toString(), depart);
                    }
                    break;
                //对于是县级或者支队级
                case 3:
                    newDepartCode = departCode.substring(0, 4);
                    newDepartCode = newDepartCode + "0000000";
                    depart = departmentMapper.findByDepartmentCode(newDepartCode);
                    if (depart == null) {
                        departType --;
                        System.out.println("没有上级部门的单位代码："+departCode);
                    }else {
                        departType = depart.getDepartmentType().toDBValue();
                        map.put(departType.toString(), depart);
                    }
                    break;
                //对于市级
                case 2:
                    newDepartCode = departCode.substring(0, 2);
                    newDepartCode = newDepartCode + "000000000";
                    depart = departmentMapper.findByDepartmentCode(newDepartCode);
                    if (depart == null) {
                        departType --;
                        System.out.println("没有上级部门的单位代码："+departCode);
                    }else {
                        departType = depart.getDepartmentType().toDBValue();
                        map.put(departType.toString(), depart);
                    }
                    break;
                default:
                    break;
            }
        }
        list = map.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
        return list;
    }

    @Override
    public DepartmentBean findByCode(String departmentCode) {
        return departmentMapper.findByDepartmentCode(departmentCode);
    }

    /**
     * find Departments of next level
     *
     * @param departmentCode the code of Department which can be a parent
     * @return daughterDepartment if Department of {@code departmentCode} exists,
     * or Top Department if {@code departmentCode} is null,
     * or EmptyList if Department of {@code departmentCode} not exists.
     */
    @Override
    public List<DepartmentBean> findChildDepartmentByCode(String departmentCode) {
        return Optional.ofNullable(departmentMapper.findByDepartmentCode(departmentCode)).map(d -> {
            if (DepartmentType.END_POINT.equals(d.getDepartmentType())) {
                return new LinkedList<DepartmentBean>();
            }
            final LinkedHashMap<String, Object> params = new LinkedHashMap<>();
            String depart = null;
            String subStr = "000000000";
            String subStr1 = "0000000";
            String subStr2 = "00000";
            if (d.getDepartmentCode().endsWith(subStr)) {
                depart = d.getDepartmentCode().substring(0, 2);
            } else if (d.getDepartmentCode().endsWith(subStr1)) {
                depart = d.getDepartmentCode().substring(0, 4);
            } else if (d.getDepartmentCode().endsWith(subStr2)) {
                depart = d.getDepartmentCode().substring(0, 6);
            }
            params.put("departmentCode", depart);
            params.put("departmentType", d.getDepartmentType().childLevel().toDBValue());
            return departmentMapper.findChildDepartmentByCodeAndTypes(params);
        }).orElse(((Supplier<List<DepartmentBean>>) () -> {
            if (StringUtils.isBlank(departmentCode)) {
                final LinkedHashMap<String, Object> params = new LinkedHashMap<>();
                params.put("departmentCode", "");
                params.put("departmentType", Arrays.asList(DepartmentType.PROVINCE.toDBValue().toString()));
                return departmentMapper.findChildDepartmentByCodeAndTypes(params);
            }
            return new LinkedList<>();
        }).get());
    }

    @Override
    public String findWholeDepartmentNameByDepartCode(String departmentCode) {
        // TODO Auto-generated method stub
        if (StringUtils.isEmpty(departmentCode)) {
            return "";
        }
        List<DepartmentBean> parentDepart = findParentDepart(departmentCode);
        StringBuilder newDepartName = new StringBuilder();
        if (!CollectionUtils.isEmpty(parentDepart)) {
            for (DepartmentBean t1 : parentDepart) {
                newDepartName.append(t1.getDepartmentName());
            }
        }
        return newDepartName.toString();
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public String insertBatch(File file) {
        // TODO Auto-generated method stub
        Workbook wb = null;
        InputStream in;
        int num = 0;
        //存放excel中的单位列表
        List<Object> departList = new ArrayList<>();
        try {
            in = new FileInputStream(file);
            //判断此文件是.xlsx后缀还是.xls后缀
            //.xlsx后缀是高版本excel文件 ，要用XSSFWorkbook对象解析
            //.xls后缀的是低版本的excel文件用HSSFWorkbook解析
            if (file.getName() == null || "".equals(file.getName())) {
                throw new RuntimeException("文件为空，请重新选择要导入的文件！");
            }
            //path中是否包含.xlsx
            String xlsx = ".xlsx";
            String xls = ".xls";
            if (file.getName().contains(xlsx)) {
                wb = new XSSFWorkbook(in);
            } else if (file.getName().contains(xls)) {
                wb = new HSSFWorkbook(in);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("文件错误，请重新导入！");
        }
        Sheet hssfSheet = wb.getSheetAt(0);
        //获取到这个工作表最后一行的行号（就是这一行中有几行就循环几圈）
        int lastRowNum = hssfSheet.getLastRowNum();
        if (lastRowNum < 1) {
            throw new RuntimeException("此文件没有数据，请重新导入！");
        }
        //从第二行开始循环
        for (int rowNum = 1; rowNum < lastRowNum; rowNum++) {
            Map map = new HashMap<>();
            HSSFRow hssfRow = (HSSFRow) hssfSheet.getRow(rowNum);
            String depart = Constants.getStringCellValue(hssfRow.getCell(1));
            DepartmentBean department = departmentMapper.findByDeCode(depart);
            if (department == null) {
                map.put("department_name", Constants.getStringCellValue(hssfRow.getCell(0)));
                map.put("department_code", depart);
                switch (depart.length()) {
                    case 2:
                        map.put("department_type", 1);
                        break;
                    case 4:
                        map.put("department_type", 2);
                        break;
                    case 6:
                        map.put("department_type", 3);
                        break;
                    case 8:
                        map.put("department_type", 3);
                        break;
                    case 12:
                        map.put("department_type", 4);
                        break;
                    default:
                        throw new RuntimeException("第" + (rowNum + 1) + "行，第" + 2 + "列编码格式错误！");
                }
                departList.add(map);
            }
        }
        if (!CollectionUtils.isEmpty(departList)) {
            departmentMapper.insertBatch(departList);
            num = departList.size();
        }
        return "成功导入数据" + num + "条";
    }


    @Override
    public String findByDepartmentCode(String departmentCode) {
        return departmentMapper.findByDepartmentCode(departmentCode).getDepartmentName();
    }
    @Override
    public List<DepartmentBean> findProvince() {
        return departmentMapper.findProvince();
    }

    @Override
    public List<DepartmentBean> findListByCodeAndWord(Map<String, String> map) {
        List<DepartmentBean> listByCodeAndWord = null;
        String departmentCode = map.get("departmentCode");
        if (null != departmentCode && !"".equals(departmentCode)) {
            map.put("departmentCode", com.hnf.honeycomb.util.StringUtils.getOldDepartmentCode(departmentCode));
            listByCodeAndWord = departmentMapper.findListByCodeAndWord(map);
            for (int i = 0; i < listByCodeAndWord.size(); i++) {
                DepartmentBean departmentBean = listByCodeAndWord.get(i);
                if (departmentCode.equals(departmentBean.getDepartmentCode())) {
                    listByCodeAndWord.remove(departmentBean);
                }
            }
        }
        return listByCodeAndWord;
    }
}
