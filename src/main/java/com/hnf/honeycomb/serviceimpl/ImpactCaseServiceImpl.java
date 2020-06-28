package com.hnf.honeycomb.serviceimpl;

import com.hnf.honeycomb.daoimpl.ImpactCaseDao;
import com.hnf.honeycomb.service.ImpactCaseService;
import com.hnf.honeycomb.util.StringUtils;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author hnf
 */
@Service
public class ImpactCaseServiceImpl implements ImpactCaseService {

    private static final Logger logger = LoggerFactory.getLogger(ImpactCaseServiceImpl.class);
    @Autowired
    private ImpactCaseDao impactCaseDao;

    @Override
    public List<Document> findCaseByCaseName(String unitCode, String caseName) {

        BasicDBObject query = new BasicDBObject();
        if (StringUtils.isNotEmptyStr(caseName)) {
            Pattern pattern = Pattern.compile("^.*" + caseName.trim() + ".*$", Pattern.CASE_INSENSITIVE);
            query.put("caseName", pattern);
        }
        unitCode = StringUtils.getOldDepartmentCode(unitCode);
        Pattern unitPattern = Pattern.compile("^.*" + unitCode + ".*$", Pattern.CASE_INSENSITIVE);
        query.put("departmentCode", unitPattern);
        Document start = new Document("$skip", 0);
        Document size = new Document("$limit", 20);
        List<Document> caseData = impactCaseDao.findCase(Arrays.asList(new Document("$match", query), start, size));
        return caseData;
    }

    @Override
    public List<Document> findDeviceByCaseName(String query) {
        Document doc = new Document();
        doc.put("caseuniquemark", query);
        return impactCaseDao.findDeviceByCaseName(doc);
    }

    @Override
    public List<Document> findPersonByPersonName(String unitCode, String nameOrIdNumber) {
        // TODO Auto-generated method stub
        BasicDBObject query = new BasicDBObject();
        if (StringUtils.isNotEmptyStr(nameOrIdNumber)) {
            //模糊查询
            String normal = Pattern.quote(nameOrIdNumber.trim());
            Pattern pattern = Pattern.compile("^.*" + normal + ".*$", Pattern.CASE_INSENSITIVE);
            BasicDBList queryList = new BasicDBList();
            queryList.add(new BasicDBObject("personName", pattern));
            queryList.add(new BasicDBObject("personNumber", pattern));
            query.put("$or", queryList);
        }
        unitCode = StringUtils.getOldDepartmentCode(unitCode);
        Pattern unitPattern = Pattern.compile("^" + unitCode + ".*$", Pattern.CASE_INSENSITIVE);
        query.put("departmentCode", unitPattern);
        BasicDBObject start = new BasicDBObject("$skip", 0);
        BasicDBObject size = new BasicDBObject("$limit", 20);
        return impactCaseDao.findPerson(Arrays.asList(new BasicDBObject("$match", query), start, size));
    }

    @Override
    public List<Document> findPersonByPhone(String unitCode, String nameOrIdNumber) {
        BasicDBObject match = null;
        if (!StringUtils.isEmpty(nameOrIdNumber)) {
            match = new BasicDBObject().append("$match", new BasicDBObject("phone",
                    Pattern.compile("^.*" + nameOrIdNumber + ".*$", Pattern.CASE_INSENSITIVE)));
        }
        //按照设备唯一标识展开
        BasicDBObject unwind = new BasicDBObject("$unwind", "$usernumber");
        //通过设备唯一标识关联fetch表
        BasicDBObject lookup = new BasicDBObject("$lookup",
                new BasicDBObject("from", "personAbstract")
                        .append("localField", "usernumber")
                        .append("foreignField", "personNumber")
                        .append("as", "temp"));
        //并把多个结果拆分成多条数据
        BasicDBObject unwind1 = new BasicDBObject("$unwind", "$temp");
        //然后再把查询条件进行筛选
        BasicDBObject query = new BasicDBObject();
        query.append("temp.departmentCode", Pattern.compile("^" + StringUtils.getOldDepartmentCode(unitCode) + ".*$"));
        //只查询指定字段
        BasicDBObject project = new BasicDBObject("$project", new BasicDBObject("_id", 1).append("temp", 1).append("phone", 1));
        //只查询20条
        BasicDBObject start = new BasicDBObject("$skip", 0);
        BasicDBObject size = new BasicDBObject("$limit", 20);

        return impactCaseDao.findPersonParent(Arrays.asList(match, unwind, lookup, unwind1,project,start,size),nameOrIdNumber);
    }

}
