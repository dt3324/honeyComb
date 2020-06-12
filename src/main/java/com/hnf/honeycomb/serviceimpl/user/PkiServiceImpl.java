package com.hnf.honeycomb.serviceimpl.user;

import com.hnf.honeycomb.bean.User;
import com.hnf.honeycomb.bean.enumerations.Operation;
import com.hnf.honeycomb.service.user.PkiService;
import com.hnf.honeycomb.service.user.UserService;
import com.hnf.honeycomb.util.JitGatewayUtil;
import com.hnf.honeycomb.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * pki认证登录业务层实现
 *
 * @author zhouhong
 */
@Service("pkiService")
public class PkiServiceImpl implements PkiService {

    private Logger logger = LoggerFactory.getLogger(PkiServiceImpl.class);

    @Autowired
    private UserService userService;

    @Override
    public String jitGWRandom() {
        // TODO Auto-generated method stub
        // 调用网关工具方法请求认证原文
        JitGatewayUtil jitGatewayUtil = new JitGatewayUtil();
        String randNum = jitGatewayUtil.generateRandomNum();

        if (!jitGatewayUtil.isNotNull(randNum)) {
            logger.info("生成原文为空！");
            return null;
        }
        return randNum;
    }

    @Override
    public Map<String, Object> jitGWAuth(Integer type, String authMode, String token, String originalData, String original,
                                         String signedData, String remoteAddr, String place) {
        // 实例化网关工具类
        JitGatewayUtil jitGatewayUtil = new JitGatewayUtil();

        // 设置认证方式、报文token、session中认证原文、客户端认证原文、认证数据包、远程地址
        jitGatewayUtil.jitGatewayUtilBean.setAuthMode(authMode);
        jitGatewayUtil.jitGatewayUtilBean.setToken(token);
        jitGatewayUtil.jitGatewayUtilBean.setOriginalData(originalData);
        jitGatewayUtil.jitGatewayUtilBean.setOriginalJsp(original);
        jitGatewayUtil.jitGatewayUtilBean.setSignedData(signedData);
        jitGatewayUtil.jitGatewayUtilBean.setRemoteAddr(remoteAddr);

        // 调用网关工具类方式进行身份认证

        // 存放网关认证返回前端的信息
        Map<String, Object> hashMap = new HashMap<>(7);
        jitGatewayUtil.auth();
        // 获取认证返回结果中的认证信息中的身份证号
        String idNumber = null;
        Map nodeMapNew = jitGatewayUtil.authResult.getCertAttributeNodeMapNew();
        if (nodeMapNew != null && nodeMapNew.size() > 0) {
//            String string = nodeMapNew.get("X509Certificate.SubjectDN").toString();
            //正式环境获取到的数据 X509Certificate.SubjectDN=CN=李睿 510704198410179251, OU=00, OU=00, O=34, L=00, L=07, ST=51, C=CN,
            //开发环境获取到的数据 X509Certificate.SubjectDN=CN=安全审计管理员_2048, O=JIT, C=CN
//            logger.info("pki 获取的数据 X509Certificate.SubjectDN："+ string);
//            Pattern pattern = Pattern.compile("[0-9]{17}[0-9xX]");
//            Matcher m = pattern.matcher(string);
//            StringBuilder stringBuilder = new StringBuilder();
//            while(m.find()) {
//                stringBuilder.append(m.group(0));
//            }
//            idNumber = stringBuilder.toString();
            idNumber = nodeMapNew.get("X509Certificate.SubjectDN").toString();
            logger.info("获取到的身份证号是："+idNumber);
        }
        //开发软证书测试，如果能获取到数据就算通过认证
        if("CN=安全审计管理员_2048, O=JIT, C=CN".equals(idNumber)){
            idNumber = "110101199003074290";
        }
        if (!StringUtils.legalString(idNumber, 18, 18)) {
            throw new RuntimeException("身份信息有误！");
        }
        User bean = userService.findByidNumber(idNumber);
        if (bean == null) {
            throw new RuntimeException("PKI对应用户不存在！");
        }
        hashMap.put("bean", userService.fullFilledLoginedUserInfo(bean.getPoliceNumber()));
        hashMap.put("IP", place);
        hashMap.put("type", UserService.LoginStatus.SUCCESS);
        hashMap.put("message", "登录成功");
        List<Object> operates = userService.findOperation((long) bean.getRoleId());
        hashMap.put("operate", operates);
        boolean isManager = operates.stream().anyMatch(Operation.manager.getOperationId()::equals);
        if (isManager) {
            // 插入登录日志
            userService.logLoginEvent(place, bean, "访问管理员页面", "PKI登录成功", 1);
        } else {
            // 插入登录日志
            userService.logLoginEvent(place, bean, "访问系统主界面", "PKI登录成功", 1);
        }
        // 修改登录操作时间
        userService.updateLastActiveyTime(bean.getPoliceNumber());
        return hashMap;
    }

}
