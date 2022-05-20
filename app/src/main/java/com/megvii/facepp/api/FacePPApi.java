/*     */ package com.megvii.facepp.api;
/*     */ 
/*     */

/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */ import com.megvii.facepp.api.bean.MergeFaceResponse;
import com.megvii.facepp.api.image.IImageApi;
/*     */ import com.megvii.facepp.api.image.ImageApi;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class FacePPApi
/*     */   implements IImageApi
/*     */ {
/*     */   private static final String API_KEY = "api_key";
/*     */   private static final String API_SECRET = "api_secret";
/*  51 */   private Map<String, String> AUTH_INFO = new HashMap<>();
/*     */
/*     */
/*     */   
/*  58 */   private IImageApi imageApi = (IImageApi)new ImageApi();
/*     */
/*     */
/*     */   public FacePPApi(String apiKey, String apiSecret) {
/*  63 */     this.AUTH_INFO.put("api_key", apiKey);
/*  64 */     this.AUTH_INFO.put("api_secret", apiSecret);
/*     */   }
    /*     */
/*     */
/*     */
/*     */
/*     */   public void mergeFace(Map<String, String> params, Map<String, byte[]> filePath, IFacePPCallBack<MergeFaceResponse> callBack) {
/* 290 */     this.imageApi.mergeFace(buildParams(params), filePath, callBack);
/*     */   }
/*     */
/*     */
/*     */   public void mergeFace(Map<String, String> params, Map<String, byte[]> file1, Map<String, byte[]> file2, IFacePPCallBack<MergeFaceResponse> callBack) {
/* 295 */     this.imageApi.mergeFace(buildParams(params), file1, file2, callBack);
/*     */   }
/*     */
/*     */
/*     */   public void mergeFace(Map<String, String> params, byte[] template_file, byte[] merge_file, IFacePPCallBack<MergeFaceResponse> callBack) {
/* 300 */     this.imageApi.mergeFace(buildParams(params), template_file, merge_file, callBack);
/*     */   }
/*     */
/*     */
/*     */   private Map<String, String> buildParams(Map<String, String> params) {
/* 314 */     if (null != params) {
/* 315 */       params.putAll(this.AUTH_INFO);
/*     */     } else {
/* 317 */       params = new HashMap<>(this.AUTH_INFO);
/*     */     } 
/* 319 */     return params;
/*     */   }
/*     */ }


/* Location:              D:\Google Chrome\facepp-java-sdk-master\facepp-java-sdk-master\app\libs\lib-facepp_api.jar!\com\megvii\facepp\api\FacePPApi.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */