package com.jinian.test1.utils;

import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class Base64ParseImg {
    public static byte[] GenerateImage(String imgStr) {// 对字节数组字符串进行Base64解码并生成图片
        if (imgStr == null) // 图像数据为空
            return null;
//        BASE64Decoder decoder = new BASE64Decoder();
        byte[] bytes = Base64.decode(imgStr, Base64.DEFAULT);

        try {
            // Base64解码
//            byte[] bytes = decoder.decodeBuffer(imgStr);
            for (int i = 0; i < bytes.length; ++i) {
                if (bytes[i] < 0) {// 调整异常数据
                    bytes[i] += 256;
                }
            }


            return bytes;
            // 生成jpeg图片
//            OutputStream out = new FileOutputStream(imgFilePath);
//            out.write(bytes);
//            out.flush();
//            out.close();
//            return true;
        } catch (Exception e) {
            return null;
        }
    }
}
