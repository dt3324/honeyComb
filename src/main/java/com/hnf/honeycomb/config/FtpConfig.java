package com.hnf.honeycomb.config;

import com.hnf.honeycomb.util.StringUtils;
import com.hnf.utils.ftp.FTPUtil;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author xhz
 * @version 1.0
 * @date 2019/8/21 18:25
 */
@Configuration
@ConfigurationProperties(prefix = "spring.ftp")
public class FtpConfig {

    private String ip;
    private String user;
    private String pwd;
    private Integer port;

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * 获取对应的FTP下载文件路径
     *
     * @param fileName
     * @return
     */
    public String getFTPDownloadPath(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            return "";
        }
        String substring = fileName.substring(0, fileName.indexOf(".hpk"));
        String replace = fileName.replace(".hpk", ".zip");
        fileName =File.separator  + "ftp"+ File.separator  + "data"+ File.separator  + "hpkunzip" + File.separator + substring + File.separator + replace;
        return getDownloadPath(fileName);
    }

    /**
     * 获取对应的采集质量统计excel文件下载路径
     *
     * @param fileName
     * @return
     */
    private String getDownloadPath(String fileName) {
        FTPUtil ftp = new FTPUtil();
        try {
            //使用:账号,密码,端口号 进行FTP链接
            ftp.connect(ip, port, user, pwd);
            List<Map<String, Object>> result = ftp.list(File.separator + fileName);
            if (!result.isEmpty()) {
                //此时应拼接对应的路径，供前端下载
                return "ftp://" + user + ":" + pwd + "@" + ip + ":" + fileName;
            }
            //断开FTP连接
            ftp.disconnect();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return "";
    }

    /**
     * 获取对应的采集质量统计excel文件下载路径
     *
     * @param fileName
     * @return
     */
    public String getExcelDownloadPath(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            return "";
        }
        return getDownloadPath(fileName);
    }
}
