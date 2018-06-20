package scwen.com.changliao;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.rong.common.fwlog.LogEntity;
import io.rong.imkit.IExtensionModule;
import io.rong.imkit.RongExtensionManager;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;

/**
 * Created by xxh on 2018/6/10.
 */

public class SealContext implements RongIMClient.ConnectionStatusListener, RongIM.ConversationClickListener, RongIM.UserInfoProvider {
    private static SealContext mRongCloudInstance;

    /**
     * 初始化 RongCloud.
     *
     * @param context 上下文。
     */
    public static void init(Context context) {

        if (mRongCloudInstance == null) {
            synchronized (SealContext.class) {

                if (mRongCloudInstance == null) {
                    mRongCloudInstance = new SealContext(context);
                }
            }
        }

    }

    private Context mContext;

    public SealContext(Context mContext) {
        this.mContext = mContext;
        SealUserInfoManager.init(mContext);

        RongIM.setConnectionStatusListener(this);

        RongIM.setConversationClickListener(this);

        setInputProvider();


        RongIM.setUserInfoProvider(this, true);
    }

    private void setInputProvider() {

        List<IExtensionModule> moduleList = RongExtensionManager.getInstance().getExtensionModules();
        IExtensionModule defaultModule = null;
        List<IExtensionModule> delete = new ArrayList<>();
        if (moduleList != null) {
            delete.addAll(moduleList);
            for (IExtensionModule deleteEx : delete) {
                RongExtensionManager.getInstance().unregisterExtensionModule(deleteEx);
            }

            RongExtensionManager.getInstance().registerExtensionModule(new SealExtensionModule());

        }
    }

    @Override
    public void onChanged(ConnectionStatus connectionStatus) {
        Log.e("rong", "connactChange:" + connectionStatus.getMessage());

        if (connectionStatus.equals(ConnectionStatus.KICKED_OFFLINE_BY_OTHER_CLIENT)) {
            quit();
        }
    }

    /**
     * 执行  清除缓存   退出登录操作
     */
    private void quit() {
        //todo逻辑需要完善
        Toast.makeText(mContext, "账号被踢！", Toast.LENGTH_SHORT).show();
    }

    /**
     * 用户头像点击事件
     *
     * @param context
     * @param conversationType
     * @param userInfo
     * @param s
     * @return
     */
    @Override
    public boolean onUserPortraitClick(Context context, Conversation.ConversationType conversationType, UserInfo userInfo, String s) {
        Toast.makeText(context, "点击了" + userInfo.getUserId() + "的头像", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public boolean onUserPortraitLongClick(Context context, Conversation.ConversationType conversationType, UserInfo userInfo, String s) {
        return false;
    }

    @Override
    public boolean onMessageClick(Context context, View view, Message message) {
        return false;
    }

    @Override
    public boolean onMessageLinkClick(Context context, String s, Message message) {
        return false;
    }

    @Override
    public boolean onMessageLongClick(Context context, View view, Message message) {
        return false;
    }

    @Override
    public UserInfo getUserInfo(String s) {

        Log.e("rong", "getUserInfo");
        return SealUserInfoManager.getInstance().get(s);
    }
}
