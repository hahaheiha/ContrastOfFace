package com.megvii.facepp.api.image;

import com.megvii.facepp.api.IFacePPCallBack;
import com.megvii.facepp.api.bean.MergeFaceResponse;

import java.util.Map;

public interface IImageApi {
  public static final String BASE_URL = "https://api-cn.faceplusplus.com/imagepp";
  
  public static final String API_IMAGE_SCENE_DETECT = "https://api-cn.faceplusplus.com/imagepp/beta/detectsceneandobject";
  
  public static final String API_IMAGE_RECOGNIZE_TEXT = "https://api-cn.faceplusplus.com/imagepp/v1/recognizetext";
  
  public static final String API_IMAGE_MERGE_FACE = "https://api-cn.faceplusplus.com/imagepp/v1/mergeface";
  
  public static final String API_IMAGE_LICENSE_PLATE = "https://api-cn.faceplusplus.com/imagepp/v1/licenseplate";

  
  void mergeFace(Map<String, String> paramMap, Map<String, byte[]> paramMap1, IFacePPCallBack<MergeFaceResponse> paramIFacePPCallBack);
  
  void mergeFace(Map<String, String> paramMap, byte[] paramArrayOfbyte1, byte[] paramArrayOfbyte2, IFacePPCallBack<MergeFaceResponse> paramIFacePPCallBack);
  
  void mergeFace(Map<String, String> paramMap, Map<String, byte[]> paramMap1, Map<String, byte[]> paramMap2, IFacePPCallBack<MergeFaceResponse> paramIFacePPCallBack);

}


/* Location:              D:\Google Chrome\facepp-java-sdk-master\facepp-java-sdk-master\app\libs\lib-facepp_api.jar!\com\megvii\facepp\api\image\IImageApi.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */