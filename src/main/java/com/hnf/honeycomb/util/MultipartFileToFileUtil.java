package com.hnf.honeycomb.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;

public class MultipartFileToFileUtil {

    public static File multipartFileToFile(MultipartFile file) {

        File toFile = null;
        try {
            if (file.equals("") || file.getSize() <= 0) {
                file = null;
            } else {
                InputStream ins = null;
                ins = file.getInputStream();
                toFile = new File(file.getOriginalFilename());
                inputStreamToFile(ins, toFile);
                ins.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
//            delteTempFile(toFile);
//            System.out.println("文件已删除\t"+toFile.getAbsolutePath());
        }

        return toFile;
    }

    //获取流文件
    private static void inputStreamToFile(InputStream ins, File file) {
        try {
            OutputStream os = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            ins.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除本地临时文件
     *
     * @param file
     */
    public static void delteTempFile(File file) {

        if (file != null) {
            File del = new File(file.toURI());
            del.delete();
        }

    }
}
