package com.megvii.facepp.api;

public interface IFacePPCallBack<T extends com.megvii.facepp.api.bean.BaseResponse> {
  void onSuccess(T paramT);
  
  void onFailed(String paramString);
}


/* Location:              D:\Google Chrome\facepp-java-sdk-master\facepp-java-sdk-master\app\libs\lib-facepp_api.jar!\com\megvii\facepp\api\IFacePPCallBack.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */