/*     */ package com.megvii.facepp.api;
/*     */ 
/*     */ import androidx.annotation.NonNull;

import java.io.IOException;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.Map;
/*     */ import okhttp3.Call;
import okhttp3.Callback;
/*     */ import okhttp3.MediaType;
/*     */ import okhttp3.MultipartBody;
/*     */ import okhttp3.OkHttpClient;
/*     */ import okhttp3.Request;
/*     */ import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/*     */
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class HttpUtils
/*     */ {
/*  21 */   private static final MediaType MediaType_APPLICATION = MediaType.parse("application/octet-stream");
/*     */   
/*     */   private static final String KEY_IMAGE_FILE = "image_file";
/*     */   
/*     */   public static final String KEY_IMAGE_FILE_1 = "image_file1";
/*     */   
/*     */   public static final String KEY_IMAGE_FILE_2 = "image_file2";
/*     */   
/*     */   public static final String KEY_TEMPLATE_FILE = "template_file";
/*     */   
/*     */   public static final String KEY_MERGE_FILE = "merge_file";
/*     */   
/*     */   private static OkHttpClient okHttpClient;
/*     */   
/*     */   public static void post(String url, Map<String, String> params, Callback callback) {
/*  36 */     post(url, params, new byte[0], callback);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static void post(String url, Map<String, String> params, byte[] file, Callback callback) {
/*  43 */     Map<String, byte[]> fileParams = (Map)new HashMap<>();
/*  44 */     fileParams.put("image_file", file);
/*  45 */     post(url, params, fileParams, null, callback);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static void post(String url, Map<String, String> params, Map<String, byte[]> file, Callback callback) {
/*  52 */     post(url, params, file, null, callback);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static void post(String url, Map<String, String> params, Map<String, byte[]> file1, Map<String, byte[]> file2, Callback callback) {
/*     */     try {
/*  61 */       RequestBody requestBody = buildRequestBody(params, file1, file2);
/*     */ 
/*     */
//        System.out.println(requestBody.);
/*  64 */       doPost((new Request.Builder()).url(url).post(requestBody).build(), callback);
/*  65 */     } catch (Exception e) {
/*  66 */       if (null != callback) {
/*  67 */         callback.onFailure(null, new IOException(e.getMessage()));
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static RequestBody buildRequestBody(Map<String, String> params, Map<String, byte[]> filePath1, Map<String, byte[]> filePath2) throws Exception {
/*  77 */     MultipartBody.Builder bodyBuilder = addNormalParams(params);
/*     */ 
/*     */     
/*  80 */     addFileParamsByKey(filePath1, bodyBuilder);
/*  81 */     addFileParamsByKey(filePath2, bodyBuilder);
/*     */     
/*  83 */     return (RequestBody)bodyBuilder.build();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static MultipartBody.Builder addNormalParams(Map<String, String> params) {
/*  91 */     MultipartBody.Builder bodyBuilder = (new MultipartBody.Builder()).setType(MultipartBody.FORM);
/*     */ 
/*     */     
/*  94 */     Iterator<String> iterator = params.keySet().iterator();
/*  95 */     while (iterator.hasNext()) {
/*  96 */       String key = iterator.next();
/*  97 */       String value = params.get(key);
/*  98 */       bodyBuilder.addFormDataPart(key, value);
/*     */     } 
/* 100 */     return bodyBuilder;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static void doPost(Request request, Callback callback) {
/* 107 */     if (null == okHttpClient) {
/* 108 */       okHttpClient = new OkHttpClient();
/*     */     }
//    try {
//        Response execute = okHttpClient.newCall(request).execute();
//
//        ResponseBody body = execute.body();
//        System.out.println(body.string());
//    } catch (IOException e) {
//        e.printStackTrace();
//    }
    /* 110 */     okHttpClient.newCall(request).enqueue(new Callback() {
        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            System.out.println(e.getMessage());
            System.out.println("失败！！！");
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            String string = response.body().string();
            System.out.println(string);
        }
    });
//    try {
//        System.out.println(okHttpClient.newCall(request).execute().toString());
//    } catch (IOException e) {
//        e.printStackTrace();
//    }
    /*     */   }
/*     */   
/*     */   private static void addFileParams(MultipartBody.Builder bodyBuilder, String key, byte[] file) throws Exception {
/* 114 */     bodyBuilder.addFormDataPart(key, key, RequestBody.create(MediaType_APPLICATION, file));
/*     */   }
/*     */   
/*     */   private static boolean isEmpty(byte[] data) {
/* 118 */     return (data == null || data.length == 0);
/*     */   }
/*     */   
/*     */   private static void addFileParamsByKey(Map<String, byte[]> filePath, MultipartBody.Builder bodyBuilder) throws Exception {
/* 122 */     if (null != filePath) {
/* 123 */       getDataByKey(filePath, "image_file", bodyBuilder);
/* 124 */       getDataByKey(filePath, "image_file1", bodyBuilder);
/* 125 */       getDataByKey(filePath, "image_file2", bodyBuilder);
/* 126 */       getDataByKey(filePath, "template_file", bodyBuilder);
/* 127 */       getDataByKey(filePath, "merge_file", bodyBuilder);
/*     */     } 
/*     */   }
/*     */   
/*     */   private static void getDataByKey(Map<String, byte[]> file, String key, MultipartBody.Builder bodyBuilder) throws Exception {
/* 132 */     if (!isEmpty(file.get(key)))
/* 133 */       addFileParams(bodyBuilder, key, file.get(key)); 
/*     */   }
/*     */ }


/* Location:              D:\Google Chrome\facepp-java-sdk-master\facepp-java-sdk-master\app\libs\lib-facepp_api.jar!\com\megvii\facepp\api\HttpUtils.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */