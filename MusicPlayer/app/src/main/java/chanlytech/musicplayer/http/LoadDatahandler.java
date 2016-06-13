package chanlytech.musicplayer.http;

public class LoadDatahandler {
    
/** 
     * 加载数据时调用 
  
   */
  
  public void onStart() {
    };
    
   
 /** 
     * 加载数据调用,得到缓存数据 
   
  * @param data 
     */
   
 public void onLoadCaches(String data) {
    };
  
  
    /** 
     * 当调用服务器接口成功获取数据时,调用这个方法 
     * @param data 
     */
  
  public void onSuccess(String data) {
    };
    
    /** 
     * 当调用服务器接口获取数据失败时,调用这个方法 
     * @param error     出错原因 
     * @param message   出错原因描述 
     */
   
 public void onFailure(String error, String message) {
    };
   
 
    /** 
     * 加载完成时调用 
     */
   
 public void onFinish() {
    };
}
