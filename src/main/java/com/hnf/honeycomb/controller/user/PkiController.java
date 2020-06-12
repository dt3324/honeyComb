package com.hnf.honeycomb.controller.user;

import com.hnf.honeycomb.bean.User;
import com.hnf.honeycomb.bean.UserLicenseBean;
import com.hnf.honeycomb.bean.vm.output.LoginedUserVmAssembler;
import com.hnf.honeycomb.controller.AbstractController;
import com.hnf.honeycomb.service.user.PkiService;
import com.hnf.honeycomb.service.user.RoleService;
import com.hnf.honeycomb.service.user.UserService;
import com.hnf.honeycomb.util.BuilderMap;
import com.hnf.honeycomb.util.JsonResult;
import com.hnf.honeycomb.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.hnf.honeycomb.util.ObjectUtil.getInteger;
import static com.hnf.honeycomb.util.ObjectUtil.getString;

/**
 * PKI认证登录表现层实现
 *
 * @author zhouhong
 */
@RestController
public class PkiController extends AbstractController {

    @Autowired
    private PkiService pkiService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    /**
     * 生成认证原文
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/jitGWRandom", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> jitGWRandom(HttpServletRequest request) {
        // TODO Auto-generated method stub
        // 设置认证原文到session，用于程序向后传递，通讯报文中使用
        String random = pkiService.jitGWRandom();
        request.getSession().setAttribute("original_data", random);
        return new JsonResult<>(random);
    }

    /**
     * PKI读取插件下载
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/jitDownloadVCTK", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> jitDownloadVCTK(HttpServletRequest request, HttpServletResponse response) {
        // TODO Auto-generated method stub
        String filePath = "/wg/PNXClient.exe";
        // 1.获取要下载的文件的相对路径
        String realPath = request.getSession().getServletContext().getRealPath(filePath);
        // 2.获取要下载的文件名
        String fileName = realPath.substring(realPath.lastIndexOf("/") + 1);
        String userAgent = request.getHeader("User-Agent");
        OutputStream os = null;
        InputStream in = null;
        try {
            // 针对IE或者以IE为内核的浏览器：
            if (userAgent.contains("MSIE") || userAgent.contains("Trident")) {
                fileName = URLEncoder.encode(fileName, "UTF-8");
            } else {
                // 非IE浏览器的处理：
                fileName = new String(fileName.getBytes("UTF-8"), "ISO-8859-1");
            }
            // 3.设置content-disposition响应头控制浏览器以下载的方式打开文件
            response.setHeader("content-disposition", "attachment;filename=" + fileName);
            // 4.获取要下载的文件输入流
            in = new FileInputStream(realPath);

            int len = 0;
            // 5.创建书缓冲区
            byte[] buffer = new byte[1024];
            // 6.通过response对象获取OutputStream输出流对象
            os = response.getOutputStream();
            // 7.将FileInputStream流对象写入到buffer缓冲区
            while ((len = in.read(buffer)) > 0) {
                os.write(buffer, 0, len);
            }
        } catch (Exception e) {
            //C:\Users\admin\AppData\Local\Temp\\undertow-docbase.1252789285686335905.12222\wg\JITWEBSKT_Setup.exe (系统找不到指定的路径。)
            System.err.println("下载VCTK-S失败：" + e.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (Exception e1) {
            }
        }
        return new JsonResult<>("ok");
    }

    /**
     * 网关认证登录
     *
     * @param request
     * @param response
     * @return
     * @ type
     * @ authMode
     * @ token
     * @ originalData
     * @ original
     * @ signedData
     * @ remoteAddr
     */
    @RequestMapping(value = "/jitGWAuth", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> jitGWAuth(@RequestBody Map<String, Object> map, HttpServletRequest request, HttpServletResponse response) {
        // TODO Auto-generated method stub
        String place = Utils.getLocalIp(request);
        Map<String,Object> userMap = pkiService.jitGWAuth(
                getInteger(map.get("type"))
                , getString(map.get("authMode"))
                , null
                //original_data cnj0vo
                , getString(map.get("original_data"))
                //original cnj0vo
                , getString(map.get("original"))
                //signed_data MIIDrwYJKoZIhvcNAQcCoIIDoDCCA5wCAQExCzAJBgUrDgMCGgUAMAsGCSqGSIb3DQEHAaCCAoIwggJ+MIIB56ADAgECAgh3idaopel8BDANBgkqhkiG9w0BAQUFADBFMQswCQYDVQQGEwJDTjEMMAoGA1UEChMDSklUMRcwFQYDVQQLEw53d3cuaml0LmNvbS5jbjEPMA0GA1UEAxMGSklUIENBMB4XDTE0MDQyMTAyMjE1MFoXDTI0MDQxODAyMjE1MFowNzELMAkGA1UEBhMCQ04xDDAKBgNVBAoMA0pJVDEaMBgGA1UEAwwR5a6J5YWo5a6h6K6h5ZGYX0wwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAMr/ZhB+uRnF7U20XbbIHQOUpYsayHP/6iBwZa7q1xQuOf2sfJwcm4NRE8TJZaIvzsIA/Y43WomyWbGMoJGVtdYAxAkz2ezwjDmVlOlq5gEZJuKyJDS2YLN39oQa6SHrDqX+9bT8cOTiUbOhAPvBSLO0wFlAWqgPWNfGeoaEvIUPAgMBAAGjgYQwgYEwHwYDVR0jBBgwFoAUz1a1tP/Xq/ZbpGLSLpuLoR8s0AcwMgYDVR0fBCswKTAnoCWgI4YhaHR0cDovL2ppdGNybC5qaXQuY29tLmNuL2NybDUuY3JsMAsGA1UdDwQEAwIE8DAdBgNVHQ4EFgQUP9X5f+bqoY/DwnHF7O4i20oN2oQwDQYJKoZIhvcNAQEFBQADgYEARPr+0bwX0/gKPhQaWV5rnvsKhTaomLfFTK+dqIvr3RPcAgJmQM1u9y3w2FFYXpg/ZXUqsissnVsTYM5BCtUTAMhY4UI6iXS6vB1qWrhObuRi3sUlOBvK5KWFBQCHiYbhByxzkJP4OUXW55YkHspB7mD8niKE1r06lfZq0vj/pnIxgfYwgfMCAQEwUTBFMQswCQYDVQQGEwJDTjEMMAoGA1UEChMDSklUMRcwFQYDVQQLEw53d3cuaml0LmNvbS5jbjEPMA0GA1UEAxMGSklUIENBAgh3idaopel8BDAJBgUrDgMCGgUAMA0GCSqGSIb3DQEBAQUABIGAUqQKNyWCjP8Qj03pDse3sNx+h9MVRCK2TgLoCeQEahX4nFryxV4DdQUnSLJTf20VKyryZvKgqQoKSXootGuAtGLJpDaT4amoUdbiXFA6tNNi5vwEr9eoh6I75UEZcC3LZTxJntuNpHehA1p+FkmuuNAm/7et2GKFWJ+pFAUPZPk=
                , getString(map.get("signed_data"))
                , place,
                request.getRemoteAddr()
        );
        User user = (User) userMap.get("bean");
        userMap.put("bean", LoginedUserVmAssembler.userToLoginUserVm(user));
        // 添加可操作功能权限
        List<Object> canOperation = userService.findOperation(user.getRoleId().longValue());
        userMap.put("canOperation", canOperation);

        // 添加可操作功能权限
        List<UserLicenseBean> userLicenses = user.getUserLicenseBeans();
        List<String> licenseUnit = new ArrayList<>();
        userLicenses.forEach(ul -> licenseUnit.add(ul.getDepartmentCode()));

        // 添加查看数据权限
        String canChectDepartCode = roleService.findCanChectDepartCode(user.getRoleId().longValue());
        if (canChectDepartCode == null || canChectDepartCode.isEmpty()) {
            canChectDepartCode = user.getDepartmentCode();
        }
        userMap.put("canChectDepartCode", canChectDepartCode);
        userMap.put("canCheckDepartmentType", com.hnf.honeycomb.util.StringUtils.getDepartmentType(canChectDepartCode));

        // 添加token
        String tokens;
        Map<String, Object> bm = BuilderMap.of(String.class, Object.class)
                .put("userId", user.getUserId())
                .put("police", user.getPoliceNumber())
                .put("roleId", user.getRoleId())
                .put("operate", userMap.get("operate"))
                .put("license", licenseUnit)
                .put("unit", user.getDepartmentCode())
                .put("unitId", user.getDepartmentId())
                .get();
        try {
            tokens = userService.getToken(bm);
            response.setHeader("Access-Control-Expose-Headers", "token");
            response.setHeader("token", tokens);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JsonResult<>(userMap);
    }
}
