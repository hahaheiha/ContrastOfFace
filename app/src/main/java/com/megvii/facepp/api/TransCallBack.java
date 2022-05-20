/*    */ package com.megvii.facepp.api;
/*    */ 
/*    */ import com.alibaba.fastjson.JSONObject;
/*    */ import com.megvii.facepp.api.bean.BaseResponse;
/*    */ import java.io.IOException;
/*    */ import okhttp3.Call;
/*    */ import okhttp3.Callback;
/*    */ import okhttp3.Response;
/*    */ import okhttp3.ResponseBody;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class TransCallBack<T extends BaseResponse>
/*    */   implements Callback
/*    */ {
/*    */   private Class<T> clazz;
/*    */   private IFacePPCallBack<T> facePPCallBack;
/*    */   
/*    */   public TransCallBack(IFacePPCallBack<T> facePPCallBack, Class<T> clazz) {
/* 24 */     this.facePPCallBack = facePPCallBack;
/* 25 */     this.clazz = clazz;
/*    */   }
/*    */ 
/*    */   
/*    */   public void onFailure(Call call, IOException e) {
/* 30 */     if (null != this.facePPCallBack) {
/* 31 */       this.facePPCallBack.onFailed(e.getMessage());
/*    */     }
/* 33 */     System.out.println(e.getMessage());
/*    */   }
/*    */ 
/*    */   
/*    */   public void onResponse(Call call, Response response) throws IOException {
/* 38 */     if (null == this.facePPCallBack) {
/*    */       return;
/*    */     }
/* 41 */     ResponseBody responseBody = response.body();
/*    */     try {
/* 43 */       BaseResponse baseResponse = (BaseResponse)JSONObject.parseObject(responseBody.string(), this.clazz);
/* 44 */       String error = baseResponse.getError_message();
/* 45 */       if (null != error && !"".equals(error)) {
/* 46 */         this.facePPCallBack.onFailed(error);
/*    */       } else {
/* 48 */         this.facePPCallBack.onSuccess((T)baseResponse);
/*    */       } 
/* 50 */     } catch (Exception e) {
/* 51 */       if (null != this.facePPCallBack)
/* 52 */         this.facePPCallBack.onFailed(e.getMessage()); 
/*    */     } 
/*    */   }
/*    */ }


/* Location:              D:\Google Chrome\facepp-java-sdk-master\facepp-java-sdk-master\app\libs\lib-facepp_api.jar!\com\megvii\facepp\api\TransCallBack.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */