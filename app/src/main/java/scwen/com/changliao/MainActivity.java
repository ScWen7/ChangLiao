package scwen.com.changliao;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.litepal.LitePal;

import io.rong.imkit.RongIM;
import io.rong.imkit.manager.IUnReadMessageObserver;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.message.ContactNotificationMessage;

public class MainActivity extends AppCompatActivity implements IUnReadMessageObserver {

    private CustomListFragment mCustomListFragment;

    private TextView tv_new;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tv_connect = findViewById(R.id.tv_connect);
        tv_new = findViewById(R.id.tv_new);

        tv_connect.setText(RongIM.getInstance().getCurrentConnectionStatus().getMessage());

        liaotian(null);

    }

    private void initData() {

        final Conversation.ConversationType[] conversationTypes = {
                Conversation.ConversationType.PRIVATE,
        };

        RongIM.getInstance().addUnReadMessageCountChangedObserver(this, conversationTypes);
        getConversationPush();// 获取 push 的 id 和 target
        getPushMessage();
    }

    /**
     * 得到不落地 push 消息
     */
    private void getPushMessage() {
        Intent intent = getIntent();
        if (intent != null && intent.getData() != null && intent.getData().getScheme().equals("rong")) {
            String path = intent.getData().getPath();
            if (path.contains("push_message")) {
//                SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
//                String cacheToken = sharedPreferences.getString("loginToken", "");
//                if (TextUtils.isEmpty(cacheToken)) {
//                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
//                } else {
                if (!RongIM.getInstance().getCurrentConnectionStatus().equals(RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED)) {
                    //TODO 增加获取 token 逻辑
                    String token = SealUserInfoManager.getInstance().getToken();
                    RongIM.connect(token, new RongIMClient.ConnectCallback() {
                        @Override
                        public void onTokenIncorrect() {

                        }

                        @Override
                        public void onSuccess(String s) {

                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode e) {

                        }
                    });
                }
//                }
            }
        }
    }

    private void getConversationPush() {
        if (getIntent() != null && getIntent().hasExtra("PUSH_CONVERSATIONTYPE") && getIntent().hasExtra("PUSH_TARGETID")) {

            final String conversationType = getIntent().getStringExtra("PUSH_CONVERSATIONTYPE");
            final String targetId = getIntent().getStringExtra("PUSH_TARGETID");


            RongIM.getInstance().getConversation(Conversation.ConversationType.valueOf(conversationType), targetId, new RongIMClient.ResultCallback<Conversation>() {
                @Override
                public void onSuccess(Conversation conversation) {

                    if (conversation != null) {

                        if (conversation.getLatestMessage() instanceof ContactNotificationMessage) { //好友消息的push
                        } else {
                            Uri uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon().appendPath("conversation")
                                    .appendPath(conversationType).appendQueryParameter("targetId", targetId).build();
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(uri);
                            startActivity(intent);
                        }
                    }
                }

                @Override
                public void onError(RongIMClient.ErrorCode e) {

                }
            });
        }
    }

    @Override
    public void onCountChanged(int count) {
        if (count == 0) {
            tv_new.setVisibility(View.INVISIBLE);
        } else {
            tv_new.setVisibility(View.VISIBLE);
        }
    }

    private void initConversation() {
        mCustomListFragment = new CustomListFragment();
    }

    public void connect(View view) {

    }

    public void disconnect(View view) {
        LitePal.deleteAll(UserCacheInfo.class);
    }

    public void liaotian(View view) {
//        RongIM.getInstance().startConversation(this, Conversation.ConversationType.PRIVATE, "006", "13");


        initConversation();

        getSupportFragmentManager().beginTransaction().replace(R.id.conversation_list, mCustomListFragment, CustomListFragment.class.getSimpleName()).commit();


        if (RongIMClient.getInstance().getCurrentConnectionStatus().equals(RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED)) {
            initData();
        }


    }

    @Override
    protected void onDestroy() {
        RongIM.getInstance().removeUnReadMessageCountChangedObserver(this);
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public static final String URL_BASE = "http://mapi.88art.com/v6.6/Api/";

    public void getToken(View view) {
//
        OkGo.<String>post(URL_BASE + "Member/Login")
                .params("username", "17621017355")
                .params("userpwd", "123456")
                .execute(new StringCallback() {

                    @Override
                    public void onSuccess(Response<String> response) {
                        String body = response.body();
                        LoginResponse loginResponse = JSON.parseObject(body, LoginResponse.class);
                        String rongyun_token = loginResponse.getRongyun_token();
                        SealUserInfoManager.getInstance().setAppToken(loginResponse.getAppToken());
                        SealUserInfoManager.getInstance().setToken(rongyun_token);
                        connectRong(rongyun_token);
                    }
                });


    }

    public void connectRong(String token) {
        if (getApplicationInfo().packageName.equals(App.getCurProcessName(getApplicationContext()))) {
            if (!RongIM.getInstance().getCurrentConnectionStatus().equals(RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED)) {
                RongIM.connect(token, new RongIMClient.ConnectCallback() {

                    /**
                     * Token 错误。可以从下面两点检查 1.  Token 是否过期，如果过期您需要向 App Server 重新请求一个新的 Token
                     *                  2.  token 对应的 appKey 和工程里设置的 appKey 是否一致
                     */
                    @Override
                    public void onTokenIncorrect() {
                        Log.e("rong", "onTokenIncorrec");
                    }

                    /**
                     * 连接融云成功
                     * @param userid 当前 token 对应的用户 id
                     */
                    @Override
                    public void onSuccess(String userid) {
                        currentUserId = userid;
                        Uri uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon()
                                .appendPath("conversationlist")
                                .appendQueryParameter(Conversation.ConversationType.PRIVATE.getName(), "false") //设置私聊会话是否聚合显示
                                .build();
                        Conversation.ConversationType[] mConversationsTypes = new Conversation.ConversationType[]{Conversation.ConversationType.PRIVATE,
                        };
                        mCustomListFragment.setUri(uri);
                        initData();
                    }

                    /**
                     * 连接融云失败
                     * @param errorCode 错误码，可到官网 查看错误码对应的注释
                     */
                    @Override
                    public void onError(RongIMClient.ErrorCode errorCode) {
                        Log.e("rong", "connactError");
                    }
                });
            }


        }
    }

    public void getUser(View view) {

    }
}
