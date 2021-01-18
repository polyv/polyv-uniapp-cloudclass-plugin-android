package uni.dcloud.io.uniplugin_richalert;

import android.app.Application;

import io.dcloud.weex.AppHookProxy;

public class RichAlert_AppProxy implements AppHookProxy {
    @Override
    public void onCreate(Application application) {
        //可写初始化触发逻辑
    }
}
