package scwen.com.changliao;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.litepal.LitePal;

import java.util.Date;

import io.rong.imkit.RongIM;
import io.rong.imlib.model.UserInfo;

/**
 * Created by xxh on 2018/6/10.
 */

public class SealUserInfoManager {
    private static SealUserInfoManager sInstance;
    private final Context mContext;


    private static SharedPreferences preferences;


    public String getToken() {
        return preferences.getString("token", "");
    }

    public void setToken(String token) {
        preferences.edit().putString("token", token).commit();
    }


    public  void setAppToken(String appToken){
        preferences.edit().putString("apptoken", appToken).commit();
    }

    public String getAppToken(){
        return preferences.getString("apptoken", "");
    }

    public static SealUserInfoManager getInstance() {
        return sInstance;
    }

    public static void init(Context context) {
        Log.e("rong", "SealUserInfoManager init");
        sInstance = new SealUserInfoManager(context);
        preferences = context.getSharedPreferences("changliao", Context.MODE_PRIVATE);
    }

    public SealUserInfoManager(Context context) {
        mContext = context;

    }


    public static void saveUserInfo(String userId, String nickName, String userProtrait) {

        UserCacheInfo user = new UserCacheInfo();

        user.setUid(userId);
        user.setNickname(nickName);
        user.setHeadurl(userProtrait);


        Log.e("rong", "保存数据库");

        user.saveOrUpdate("uid = ?", userId);
    }

    public static final String URL_BASE = "http://mapi.88art.com/v6.6/Api/";

    public static UserInfo get(final String userid) {

        UserInfo userInfo = null;
        if (notExistedOrExpired(userid)) {
            //不存在或者过期
            Log.e("rong", "本地缓存不存在该账号");

            OkGo.<String>post(URL_BASE + "Chat/GetUserByUid")
                    .params("userid", userid)
                    .params("appToken",SealUserInfoManager.getInstance().getAppToken())
                    .execute(new StringCallback() {

                        @Override
                        public void onSuccess(Response<String> response) {
                            String body = response.body();
                            GetUserInfoResponse userInfoResponse = JSON.parseObject(body, GetUserInfoResponse.class);
                            String nickname = userInfoResponse.getData().getNickname();
                            String userimg = userInfoResponse.getData().getUserimg();
                            UserInfo userInfo = new UserInfo(userid, nickname, Uri.parse(userimg));
                            RongIM.getInstance().refreshUserInfoCache(userInfo);
                            saveUserInfo(userid, nickname, userimg);
                        }
                    });
            return null;
        }
        userInfo = getUserFromDb(userid);

        return userInfo;

    }

    /**
     * 用户不存在或已过期
     *
     * @param userid uid
     * @return
     */
    public static boolean notExistedOrExpired(String userid) {

        int count = LitePal.where("uid = ? ", userid).count(UserCacheInfo.class);

        return count <= 0;
    }


    public static UserInfo getUserFromDb(String userid) {
        Log.e("rong", "读取数据库数据");
        UserCacheInfo user = LitePal.where("uid = ?", userid).findFirst(UserCacheInfo.class);
        UserInfo userInfo = null;
        if (user != null) {
            userInfo = new UserInfo(user.getUid(), user.getNickname(), Uri.parse(user.getHeadurl()));
        }
        return userInfo;
    }

}
