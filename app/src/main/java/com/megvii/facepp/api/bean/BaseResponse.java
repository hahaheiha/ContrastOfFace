/*    */ package com.megvii.facepp.api.bean;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class BaseResponse
/*    */ {


    /**
     *  "time_used": 473,
     *     "confidence": 96.46,
     *     "thresholds": {
     *         "1e-3": 65.3,
     *         "1e-5": 76.5,
     *         "1e-4": 71.8
     *     },
     *     "request_id": "1469761507,07174361-027c-46e1-811f-ba0909760b18"
     */
    /*    */   private int time_used;
            private int confidence;

/*    */   private String request_id;

/*    */   private String error_message;
/*    */   
/*    */   public int getTime_used() {
/* 18 */     return this.time_used;
/*    */   }
/*    */   
/*    */   public void setTime_used(int time_used) {
/* 22 */     this.time_used = time_used;
/*    */   }
/*    */   
/*    */   public String getRequest_id() {
/* 26 */     return this.request_id;
/*    */   }
/*    */   
/*    */   public void setRequest_id(String request_id) {
/* 30 */     this.request_id = request_id;
/*    */   }

            public void setConfidence(int confidence) {
                this.confidence = confidence;
            }

            public int getConfidence() {
                return this.confidence;
            }
/*    */   
/*    */   public String getError_message() {
/* 34 */     return this.error_message;
/*    */   }
/*    */   
/*    */   public void setError_message(String error_message) {
/* 38 */     this.error_message = error_message;
/*    */   }
/*    */ }


/* Location:              D:\Google Chrome\facepp-java-sdk-master\facepp-java-sdk-master\app\libs\lib-facepp_api.jar!\com\megvii\facepp\api\bean\BaseResponse.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */