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
        Document start = new Document("$skip", 0);
        Document size = new Document("$limit", 20);
        List<Document> person = impactCaseDao.findPerson(Arrays.asList(new Document("$match", query), start, size));
        return person;
    }

}
