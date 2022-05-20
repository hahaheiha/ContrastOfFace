/*    */ package com.megvii.facepp.api.image;
/*    */ 
/*    */ import com.megvii.facepp.api.HttpUtils;
/*    */ import com.megvii.facepp.api.IFacePPCallBack;
/*    */ import com.megvii.facepp.api.TransCallBack;
import com.megvii.facepp.api.bean.MergeFaceResponse;
/*    */
/*    */
/*    */
/*    */
/*    */ import java.util.HashMap;
/*    */ import java.util.Map;
/*    */ import okhttp3.Callback;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class ImageApi
/*    */   implements IImageApi
/*    */ {
/*    */   public void mergeFace(Map<String, String> params, IFacePPCallBack<MergeFaceResponse> callBack) {
/* 42 */     HttpUtils.post("https://www.api-cn.faceplusplus.com/facepp/v3/compare", params, (Callback)new TransCallBack(callBack, MergeFaceResponse.class));
/*    */   }
/*    */ 
/*    */   
/*    */   public void mergeFace(Map<String, String> params, Map<String, byte[]> filePath, IFacePPCallBack<MergeFaceResponse> callBack) {
/* 47 */     HttpUtils.post("https://www.api-cn.faceplusplus.com/facepp/v3/compare", params, filePath, (Callback)new TransCallBack(callBack, MergeFaceResponse.class));
/*    */   }
/*    */ 
/*    */   
/*    */   public void mergeFace(Map<String, String> params, byte[] filePath1, byte[] filePath2, IFacePPCallBack<MergeFaceResponse> callBack) {
/* 52 */     Map<String, byte[]> file1 = (Map)new HashMap<>();
/* 53 */     file1.put("image_file1", filePath1);
/*    */     
/* 55 */     Map<String, byte[]> file2 = (Map)new HashMap<>();
/* 56 */     file2.put("image_file2", filePath2);
/*    */     
/* 58 */     HttpUtils.post("https://www.api-cn.faceplusplus.com/facepp/v3/compare", params, file1, file2, (Callback)new TransCallBack(callBack, MergeFaceResponse.class));
/*    */   }
/*    */ 
/*    */   
/*    */   public void mergeFace(Map<String, String> params, Map<String, byte[]> file1, Map<String, byte[]> file2, IFacePPCallBack<MergeFaceResponse> callBack) {
/* 63 */     HttpUtils.post("https://www.api-cn.faceplusplus.com/facepp/v3/compare", params, file1, file2, (Callback)new TransCallBack(callBack, MergeFaceResponse.class));
/*    */   }
/*    */
/*    */ }


/* Location:              D:\Google Chrome\facepp-java-sdk-master\facepp-java-sdk-master\app\libs\lib-facepp_api.jar!\com\megvii\facepp\api\image\ImageApi.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */