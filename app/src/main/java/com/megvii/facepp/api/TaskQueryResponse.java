/*     */ package com.megvii.facepp.api;
/*     */ 
/*     */ import com.megvii.facepp.api.bean.BaseResponse;
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
/*     */ public class TaskQueryResponse
/*     */   extends BaseResponse
/*     */ {
/*     */   private String task_id;
/*     */   private int status;
/*     */   private String faceset_token;
/*     */   private String outer_id;
/*     */   private int face_added;
/*     */   private int face_removed;
/*     */   private int face_count;
/*     */   private String task_failure_detail;
/*     */   
/*     */   public String getTask_id() {
/*  31 */     return this.task_id;
/*     */   }
/*     */   
/*     */   public void setTask_id(String task_id) {
/*  35 */     this.task_id = task_id;
/*     */   }
/*     */   
/*     */   public int getStatus() {
/*  39 */     return this.status;
/*     */   }
/*     */   
/*     */   public void setStatus(int status) {
/*  43 */     this.status = status;
/*     */   }
/*     */   
/*     */   public String getFaceset_token() {
/*  47 */     return this.faceset_token;
/*     */   }
/*     */   
/*     */   public void setFaceset_token(String faceset_token) {
/*  51 */     this.faceset_token = faceset_token;
/*     */   }
/*     */   
/*     */   public String getOuter_id() {
/*  55 */     return this.outer_id;
/*     */   }
/*     */   
/*     */   public void setOuter_id(String outer_id) {
/*  59 */     this.outer_id = outer_id;
/*     */   }
/*     */   
/*     */   public int getFace_added() {
/*  63 */     return this.face_added;
/*     */   }
/*     */   
/*     */   public void setFace_added(int face_added) {
/*  67 */     this.face_added = face_added;
/*     */   }
/*     */   
/*     */   public int getFace_removed() {
/*  71 */     return this.face_removed;
/*     */   }
/*     */   
/*     */   public void setFace_removed(int face_removed) {
/*  75 */     this.face_removed = face_removed;
/*     */   }
/*     */   
/*     */   public int getFace_count() {
/*  79 */     return this.face_count;
/*     */   }
/*     */   
/*     */   public void setFace_count(int face_count) {
/*  83 */     this.face_count = face_count;
/*     */   }
/*     */   public String getTask_failure_detail() {
/*  95 */     return this.task_failure_detail;
/*     */   }
/*     */   
/*     */   public void setTask_failure_detail(String task_failure_detail) {
/*  99 */     this.task_failure_detail = task_failure_detail;
/*     */   }
/*     */ 
/*     */   
/*     */   public String toString() {
/* 104 */     return "{\"task_id\":'" + this.task_id + "', \"status\":" + this.status + ", \"faceset_token\":'" + this.faceset_token + "', \"outer_id\":'" + this.outer_id + "', \"face_added\":" + this.face_added + ", \"face_removed\":" + this.face_removed + ", \"face_count\":" + this.face_count + ", \"task_failure_detail\":'" + this.task_failure_detail + "'" + '}';
/*     */   }
/*     */ }


/* Location:              D:\Google Chrome\facepp-java-sdk-master\facepp-java-sdk-master\app\libs\lib-facepp_api.jar!\com\megvii\facepp\api\TaskQueryResponse.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */