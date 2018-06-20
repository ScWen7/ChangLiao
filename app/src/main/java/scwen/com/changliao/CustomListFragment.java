package scwen.com.changliao;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.rong.common.RLog;
import io.rong.eventbus.EventBus;
import io.rong.imkit.R.bool;
import io.rong.imkit.R.drawable;
import io.rong.imkit.R.id;
import io.rong.imkit.R.layout;
import io.rong.imkit.R.string;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.IHistoryDataResultCallback;
import io.rong.imkit.fragment.UriFragment;
import io.rong.imkit.manager.InternalModuleManager;
import io.rong.imkit.model.Event.ClearConversationEvent;
import io.rong.imkit.model.Event.ConnectEvent;
import io.rong.imkit.model.Event.ConversationNotificationEvent;
import io.rong.imkit.model.Event.ConversationRemoveEvent;
import io.rong.imkit.model.Event.ConversationTopEvent;
import io.rong.imkit.model.Event.ConversationUnreadEvent;
import io.rong.imkit.model.Event.MessageDeleteEvent;
import io.rong.imkit.model.Event.MessageLeftEvent;
import io.rong.imkit.model.Event.MessageRecallEvent;
import io.rong.imkit.model.Event.MessagesClearEvent;
import io.rong.imkit.model.Event.OnMessageSendErrorEvent;
import io.rong.imkit.model.Event.OnReceiveMessageEvent;
import io.rong.imkit.model.Event.RemoteMessageRecallEvent;
import io.rong.imkit.model.Event.SyncReadStatusEvent;
import io.rong.imkit.model.UIConversation;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.RongIMClient.ConnectionStatusListener.ConnectionStatus;
import io.rong.imlib.RongIMClient.ErrorCode;
import io.rong.imlib.RongIMClient.ResultCallback;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.Message.MessageDirection;
import io.rong.imlib.model.Message.SentStatus;
import io.rong.imlib.model.UserInfo;
import io.rong.push.RongPushClient;

public class CustomListFragment extends UriFragment {
    private String TAG = "CustemListFragment";
    private ConversationConfig mConversationsConfig;
    private CustomListFragment mThis;
    private CustomerListAdapter mAdapter;
    private ListView mList;
    private LinearLayout mNotificationBar;
    private ImageView mNotificationBarImage;
    private TextView mNotificationBarText;
    private boolean isShowWithoutConnected = false;
    private int leftOfflineMsg = 0;


    private int headerSize = 1;

    public CustomListFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mThis = this;
        this.TAG = this.getClass().getSimpleName();

        EventBus.getDefault().register(this);
        InternalModuleManager.getInstance().onLoaded();
    }

    protected void initFragment(Uri uri) {
        RLog.d(this.TAG, "initFragment " + uri);

        mConversationsConfig = new ConversationConfig();
        mConversationsConfig.conversationType = ConversationType.PRIVATE;

        this.mAdapter.clear();
        if (RongIMClient.getInstance().getCurrentConnectionStatus().equals(ConnectionStatus.DISCONNECTED)) {
            RLog.d(this.TAG, "RongCloud haven't been connected yet, so the conversation list display blank !!!");
            this.isShowWithoutConnected = true;
        } else {
            this.getConversationList(ConversationType.PRIVATE);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.rc_fr_conversationlist_custom, container, false);
        this.mNotificationBar = (LinearLayout) this.findViewById(view, R.id.rc_status_bar);
        this.mNotificationBar.setVisibility(View.GONE);
        this.mNotificationBarImage = (ImageView) this.findViewById(view, R.id.rc_status_bar_image);
        this.mNotificationBarText = (TextView) this.findViewById(view, R.id.rc_status_bar_text);
        this.mList = (ListView) this.findViewById(view, R.id.rc_list);

        if (this.mAdapter == null) {
            this.mAdapter = this.onResolveAdapter(this.getActivity());
        }

        this.mList.setAdapter(this.mAdapter);

        TextView textView = new TextView(getContext());
        textView.setText("头部");
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300);
        textView.setLayoutParams(layoutParams);
        mList.addHeaderView(textView);

        mAdapter.setConversationEventListener(new CustomerListAdapter.OnConversationEventListener() {
            @Override
            public void onDelete(int position) {
                UIConversation uiConversation = (UIConversation) mAdapter.getItem(position);
                RongIM.getInstance().removeConversation(uiConversation.getConversationType(), uiConversation.getConversationTargetId(), (ResultCallback) null);
            }

            @Override
            public void onClickToTalk(int position, View view) {
                UIConversation uiConversation = (UIConversation) mAdapter.getItem(position);
                ConversationType conversationType = uiConversation.getConversationType();

                if (RongContext.getInstance().getConversationListBehaviorListener() != null && RongContext.getInstance().getConversationListBehaviorListener().onConversationClick(getActivity(), view, uiConversation)) {
                    return;
                }
                Log.e("TAG", "onclickToTalk");
                uiConversation.setUnReadMessageCount(0);
                RongIM.getInstance().startConversation(getActivity(), conversationType, uiConversation.getConversationTargetId(), uiConversation.getUIConversationTitle());
            }
        });

        return view;
    }

    public void onResume() {
        super.onResume();
        RLog.d(this.TAG, "onResume " + RongIM.getInstance().getCurrentConnectionStatus());
        RongPushClient.clearAllNotifications(this.getActivity());
        this.setNotificationBarVisibility(RongIM.getInstance().getCurrentConnectionStatus());
    }

    private void getConversationList(ConversationType conversationType) {
        this.getConversationList(conversationType, new IHistoryDataResultCallback<List<Conversation>>() {
            public void onResult(List<Conversation> data) {
                if (data != null && data.size() > 0) {
                    CustomListFragment.this.makeUiConversationList(data);
                    RLog.d(CustomListFragment.this.TAG, "getConversationList : listSize = " + data.size());
                    CustomListFragment.this.mAdapter.notifyDataSetChanged();
                    CustomListFragment.this.onUnreadCountChanged();
                } else {
                    RLog.e(CustomListFragment.this.TAG, "getConversationList return null " + RongIMClient.getInstance().getCurrentConnectionStatus());
                    CustomListFragment.this.isShowWithoutConnected = true;
                }

                CustomListFragment.this.onFinishLoadConversationList(CustomListFragment.this.leftOfflineMsg);
            }

            public void onError() {
                RLog.e(CustomListFragment.this.TAG, "getConversationList Error");
                CustomListFragment.this.onFinishLoadConversationList(CustomListFragment.this.leftOfflineMsg);
            }
        });
    }

    public void getConversationList(ConversationType conversationType, final IHistoryDataResultCallback<List<Conversation>> callback) {
        RongIMClient.getInstance().getConversationList(new ResultCallback<List<Conversation>>() {
            public void onSuccess(List<Conversation> conversations) {
                if (callback != null) {
                    List<Conversation> resultConversations = new ArrayList();
                    if (conversations != null) {
                        Iterator var3 = conversations.iterator();

                        while (var3.hasNext()) {
                            Conversation conversation = (Conversation) var3.next();
                            if (!CustomListFragment.this.shouldFilterConversation(conversation.getConversationType(), conversation.getTargetId())) {
                                resultConversations.add(conversation);
                            }
                        }
                    }

                    callback.onResult(resultConversations);
                }

            }

            public void onError(ErrorCode e) {
                if (callback != null) {
                    callback.onError();
                }

            }
        }, conversationType);
    }


    private void setNotificationBarVisibility(ConnectionStatus status) {
        if (!this.getResources().getBoolean(bool.rc_is_show_warning_notification)) {
            RLog.e(this.TAG, "rc_is_show_warning_notification is disabled.");
        } else {
            String content = null;
            if (status.equals(ConnectionStatus.NETWORK_UNAVAILABLE)) {
                content = this.getResources().getString(string.rc_notice_network_unavailable);
            } else if (status.equals(ConnectionStatus.KICKED_OFFLINE_BY_OTHER_CLIENT)) {
                content = this.getResources().getString(string.rc_notice_tick);
            } else if (status.equals(ConnectionStatus.CONNECTED)) {
                this.mNotificationBar.setVisibility(View.GONE);
            } else if (status.equals(ConnectionStatus.DISCONNECTED)) {
                content = this.getResources().getString(string.rc_notice_disconnect);
            } else if (status.equals(ConnectionStatus.CONNECTING)) {
                content = this.getResources().getString(string.rc_notice_connecting);
            }

            if (content != null && this.mNotificationBar != null) {
                if (this.mNotificationBar.getVisibility() == View.GONE) {
                    final String finalContent = content;
                    this.getHandler().postDelayed(new Runnable() {
                        public void run() {
                            if (!RongIMClient.getInstance().getCurrentConnectionStatus().equals(ConnectionStatus.CONNECTED)) {
                                CustomListFragment.this.mNotificationBar.setVisibility(View.VISIBLE);
                                CustomListFragment.this.mNotificationBarText.setText(finalContent);
                                if (RongIMClient.getInstance().getCurrentConnectionStatus().equals(ConnectionStatus.CONNECTING)) {
                                    CustomListFragment.this.mNotificationBarImage.setImageResource(drawable.rc_notification_connecting_animated);
                                } else {
                                    CustomListFragment.this.mNotificationBarImage.setImageResource(drawable.rc_notification_network_available);
                                }
                            }

                        }
                    }, 4000L);
                } else {
                    this.mNotificationBarText.setText(content);
                    if (RongIMClient.getInstance().getCurrentConnectionStatus().equals(ConnectionStatus.CONNECTING)) {
                        this.mNotificationBarImage.setImageResource(drawable.rc_notification_connecting_animated);
                    } else {
                        this.mNotificationBarImage.setImageResource(drawable.rc_notification_network_available);
                    }
                }
            }

        }
    }

    public boolean onBackPressed() {
        return false;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void setAdapter(CustomerListAdapter adapter) {
        this.mAdapter = adapter;
        if (this.mList != null) {
            this.mList.setAdapter(adapter);
        }

    }

    public CustomerListAdapter onResolveAdapter(Context context) {
        this.mAdapter = new CustomerListAdapter(context);
        return this.mAdapter;
    }

    public void onEventMainThread(SyncReadStatusEvent event) {
        Log.e("TAG", "SyncReadStatusEvent");
        ConversationType conversationType = event.getConversationType();
        String targetId = event.getTargetId();
        RLog.d(this.TAG, "SyncReadStatusEvent " + conversationType + " " + targetId);
        int first = this.mList.getFirstVisiblePosition();
        int last = this.mList.getLastVisiblePosition();
        int position;

        position = this.mAdapter.findPosition(conversationType, targetId);


        if (position >= 0) {
            UIConversation uiConversation = (UIConversation) this.mAdapter.getItem(position);
            uiConversation.clearUnRead(conversationType, targetId);
            if (position >= first && position <= last) {
                this.mAdapter.getView(position, this.mList.getChildAt(position - this.mList.getFirstVisiblePosition() + headerSize), this.mList);
            }
        }

        this.onUnreadCountChanged();
    }


    public boolean shouldUpdateConversation(Message message, int left) {
        return true;
    }

    public boolean shouldFilterConversation(ConversationType type, String targetId) {
        return false;
    }

    public void onUnreadCountChanged() {
    }

    public void onFinishLoadConversationList(int leftOfflineMsg) {
    }

    public void onUIConversationCreated(UIConversation uiConversation) {
    }

    public void updateListItem(UIConversation uiConversation) {
        int position = this.mAdapter.findPosition(uiConversation.getConversationType(), uiConversation.getConversationTargetId());
        if (position >= 0) {
            this.mAdapter.getView(position, this.mList.getChildAt(position - this.mList.getFirstVisiblePosition() + headerSize), this.mList);
        }

    }

    public void onEventMainThread(OnReceiveMessageEvent event) {
        Log.e("TAG", "OnReceiveMessageEvent");
        this.leftOfflineMsg = event.getLeft();
        Message message = event.getMessage();
        String targetId = message.getTargetId();
        ConversationType conversationType = message.getConversationType();
        if (!this.shouldFilterConversation(conversationType, targetId)) {
            int first = this.mList.getFirstVisiblePosition();
            int last = this.mList.getLastVisiblePosition();
            if (this.isConfigured(message.getConversationType()) && this.shouldUpdateConversation(event.getMessage(), event.getLeft())) {
                if (message.getMessageId() > 0) {
                    int position = this.mAdapter.findPosition(conversationType, targetId);
                    Log.e("TAG", "position:" + position);
                    UIConversation uiConversation;
                    int index;
                    if (position < 0) {
                        uiConversation = UIConversation.obtain(this.getActivity(), message, false);
                        index = this.getPosition(uiConversation);
                        this.mAdapter.add(uiConversation, index);
                        this.onUIConversationCreated(uiConversation);
                        this.mAdapter.notifyDataSetChanged();
                    } else {
                        uiConversation = (UIConversation) this.mAdapter.getItem(position);
                        if (event.getMessage().getSentTime() > uiConversation.getUIConversationTime()) {
                            uiConversation.updateConversation(message, false);
                            this.mAdapter.remove(position);
                            index = this.getPosition(uiConversation);
                            if (index == position) {
                                this.mAdapter.add(uiConversation, index);
                                if (index >= first && index <= last) {
                                    this.mAdapter.getView(index, this.mList.getChildAt(index - this.mList.getFirstVisiblePosition() + headerSize), this.mList);
                                }
                            } else {
                                this.mAdapter.add(uiConversation, index);
                                if (index >= first && index <= last) {
                                    this.mAdapter.notifyDataSetChanged();
                                }
                            }
                        } else {
                            RLog.i(this.TAG, "ignore update message " + event.getMessage().getObjectName());
                        }
                    }

                    RLog.i(this.TAG, "conversation unread count : " + uiConversation.getUnReadMessageCount() + " " + conversationType + " " + targetId);
                }

                if (event.getLeft() == 0) {
                    this.syncUnreadCount();
                }

                RLog.d(this.TAG, "OnReceiveMessageEvent: " + message.getObjectName() + " " + event.getLeft() + " " + conversationType + " " + targetId);
            }

        }
    }

    public void onEventMainThread(MessageLeftEvent event) {
        Log.e("TAG", "MessageLeftEvent");
        if (event.left == 0) {
            this.syncUnreadCount();
        }

    }

    private void syncUnreadCount() {
        if (this.mAdapter.getCount() > 0) {
            final int first = this.mList.getFirstVisiblePosition();
            final int last = this.mList.getLastVisiblePosition();

            for (int i = 0; i < this.mAdapter.getCount(); ++i) {
                final UIConversation uiConversation = (UIConversation) this.mAdapter.getItem(i);
                ConversationType conversationType = uiConversation.getConversationType();
                String targetId = uiConversation.getConversationTargetId();
                final int position;
                position = this.mAdapter.findPosition(conversationType, targetId);
                RongIMClient.getInstance().getUnreadCount(conversationType, targetId, new ResultCallback<Integer>() {
                    public void onSuccess(Integer integer) {
                        uiConversation.setUnReadMessageCount(integer.intValue());
                        if (position >= first && position <= last) {
                            CustomListFragment.this.mAdapter.getView(position, CustomListFragment.this.mList.getChildAt(position - CustomListFragment.this.mList.getFirstVisiblePosition() + headerSize), CustomListFragment.this.mList);
                        }

                        CustomListFragment.this.onUnreadCountChanged();
                    }

                    public void onError(ErrorCode e) {
                    }
                });
            }
        }

    }

    public void onEventMainThread(MessageRecallEvent event) {
        Log.e("TAG", "MessageRecallEvent");
        int count = this.mAdapter.getCount();

        for (int i = 0; i < count; ++i) {
            UIConversation uiConversation = (UIConversation) this.mAdapter.getItem(i);
            if (event.getMessageId() == uiConversation.getLatestMessageId()) {
                boolean gatherState = ((UIConversation) this.mAdapter.getItem(i)).getConversationGatherState();
                final String targetId = ((UIConversation) this.mAdapter.getItem(i)).getConversationTargetId();
                if (gatherState) {
                    RongIM.getInstance().getConversationList(new ResultCallback<List<Conversation>>() {
                        public void onSuccess(List<Conversation> conversationList) {
                            if (conversationList != null && conversationList.size() > 0) {
                                UIConversation uiConversation = CustomListFragment.this.makeUIConversation(conversationList);
                                int oldPos = CustomListFragment.this.mAdapter.findPosition(uiConversation.getConversationType(), targetId);
                                if (oldPos >= 0) {
                                    CustomListFragment.this.mAdapter.remove(oldPos);
                                }

                                int newIndex = CustomListFragment.this.getPosition(uiConversation);
                                CustomListFragment.this.mAdapter.add(uiConversation, newIndex);
                                CustomListFragment.this.mAdapter.notifyDataSetChanged();
                            }

                        }

                        public void onError(ErrorCode e) {
                        }
                    }, new ConversationType[]{uiConversation.getConversationType()});
                } else {
                    RongIM.getInstance().getConversation(uiConversation.getConversationType(), uiConversation.getConversationTargetId(), new ResultCallback<Conversation>() {
                        public void onSuccess(Conversation conversation) {
                            if (conversation != null) {
                                UIConversation uiConversation = UIConversation.obtain(CustomListFragment.this.getActivity(), conversation, false);
                                int pos = CustomListFragment.this.mAdapter.findPosition(conversation.getConversationType(), conversation.getTargetId());
                                if (pos >= 0) {
                                    CustomListFragment.this.mAdapter.remove(pos);
                                }

                                int newPosition = CustomListFragment.this.getPosition(uiConversation);
                                CustomListFragment.this.mAdapter.add(uiConversation, newPosition);
                                CustomListFragment.this.mAdapter.notifyDataSetChanged();
                            }

                        }

                        public void onError(ErrorCode e) {
                        }
                    });
                }
                break;
            }
        }

    }

    public void onEventMainThread(RemoteMessageRecallEvent event) {
        Log.e("TAG", "RemoteMessageRecallEvent");
        int count = this.mAdapter.getCount();

        for (int i = 0; i < count; ++i) {
            UIConversation uiConversation = (UIConversation) this.mAdapter.getItem(i);
            if (event.getMessageId() == uiConversation.getLatestMessageId() || event.getTargetId().equals(uiConversation.getConversationTargetId()) && event.getRecallNotificationMessage().getRecallTime() > uiConversation.getUIConversationTime()) {
                boolean gatherState = uiConversation.getConversationGatherState();
                final String targetId = ((UIConversation) this.mAdapter.getItem(i)).getConversationTargetId();
                if (gatherState) {
                    RongIM.getInstance().getConversationList(new ResultCallback<List<Conversation>>() {
                        public void onSuccess(List<Conversation> conversationList) {
                            if (conversationList != null && conversationList.size() > 0) {
                                UIConversation uiConversation = CustomListFragment.this.makeUIConversation(conversationList);
                                int oldPos = CustomListFragment.this.mAdapter.findPosition(uiConversation.getConversationType(), targetId);
                                if (oldPos >= 0) {
                                    CustomListFragment.this.mAdapter.remove(oldPos);
                                }

                                int newIndex = CustomListFragment.this.getPosition(uiConversation);
                                CustomListFragment.this.mAdapter.add(uiConversation, newIndex);
                                CustomListFragment.this.mAdapter.notifyDataSetChanged();
                                CustomListFragment.this.onUnreadCountChanged();
                            }

                        }

                        public void onError(ErrorCode e) {
                        }
                    }, new ConversationType[]{((UIConversation) this.mAdapter.getItem(i)).getConversationType()});
                } else {
                    RongIM.getInstance().getConversation(uiConversation.getConversationType(), uiConversation.getConversationTargetId(), new ResultCallback<Conversation>() {
                        public void onSuccess(Conversation conversation) {
                            if (conversation != null) {
                                UIConversation temp = UIConversation.obtain(CustomListFragment.this.getActivity(), conversation, false);
                                int pos = CustomListFragment.this.mAdapter.findPosition(conversation.getConversationType(), conversation.getTargetId());
                                if (pos >= 0) {
                                    CustomListFragment.this.mAdapter.remove(pos);
                                }

                                int newPosition = CustomListFragment.this.getPosition(temp);
                                CustomListFragment.this.mAdapter.add(temp, newPosition);
                                CustomListFragment.this.mAdapter.notifyDataSetChanged();
                                CustomListFragment.this.onUnreadCountChanged();
                            }

                        }

                        public void onError(ErrorCode e) {
                        }
                    });
                }
                break;
            }
        }

    }

    public void onEventMainThread(Message message) {
        Log.e("TAG", "message:" + message);
        ConversationType conversationType = message.getConversationType();
        String targetId = message.getTargetId();
        RLog.d(this.TAG, "Message: " + message.getObjectName() + " " + message.getMessageId() + " " + conversationType + " " + message.getSentStatus());
        if (!this.shouldFilterConversation(conversationType, targetId)) {
            boolean gathered = false;
            if (this.isConfigured(conversationType) && message.getMessageId() > 0) {
                int position = gathered ? this.mAdapter.findGatheredItem(conversationType) : this.mAdapter.findPosition(conversationType, targetId);

                UIConversation uiConversation;
                if (position < 0) {
                    uiConversation = UIConversation.obtain(this.getActivity(), message, gathered);
                    int index = this.getPosition(uiConversation);
                    this.mAdapter.add(uiConversation, index);
                    this.onUIConversationCreated(uiConversation);
                    this.mAdapter.notifyDataSetChanged();
                } else {
                    uiConversation = (UIConversation) this.mAdapter.getItem(position);
                    long covTime = uiConversation.getUIConversationTime();
                    if (uiConversation.getLatestMessageId() == message.getMessageId() && uiConversation.getSentStatus() == SentStatus.SENDING && message.getSentStatus() == SentStatus.SENT && message.getMessageDirection() == MessageDirection.SEND) {
                        covTime -= RongIMClient.getInstance().getDeltaTime();
                    }

                    if (covTime <= message.getSentTime() || uiConversation.getLatestMessageId() < 0) {
                        this.mAdapter.remove(position);
                        uiConversation.updateConversation(message, gathered);
                        int index = this.getPosition(uiConversation);
                        this.mAdapter.add(uiConversation, index);
                        if (position == index) {
                            this.mAdapter.getView(index, this.mList.getChildAt(index - this.mList.getFirstVisiblePosition() + headerSize), this.mList);
                        } else {
                            this.mAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }

        }
    }

    public void onEventMainThread(ConnectionStatus status) {
        Log.e("TAG", "ConnectionStatus");
        this.setNotificationBarVisibility(status);
    }

    public void onEventMainThread(ConnectEvent event) {
        RLog.d(this.TAG, "ConnectEvent :" + RongIMClient.getInstance().getCurrentConnectionStatus());
        if (this.isShowWithoutConnected) {
            this.getConversationList(ConversationType.PRIVATE);
            this.isShowWithoutConnected = false;
        }

    }


    public void onEventMainThread(UserInfo userInfo) {
        RLog.i(this.TAG, "UserInfo " + userInfo.getUserId() + " " + userInfo.getName());
        int count = this.mAdapter.getCount();
        int last = this.mList.getLastVisiblePosition();
        int first = this.mList.getFirstVisiblePosition();

        for (int i = 0; i < count && userInfo.getName() != null; ++i) {
            UIConversation uiConversation = (UIConversation) this.mAdapter.getItem(i);
            if (uiConversation.hasNickname(userInfo.getUserId())) {
                RLog.i(this.TAG, "has nick name");
            } else {
                uiConversation.updateConversation(userInfo);
                if (i >= first && i <= last) {
                    this.mAdapter.getView(i, this.mList.getChildAt(i - first + headerSize), this.mList);
                }
            }
        }

    }


    public void onEventMainThread(ConversationUnreadEvent unreadEvent) {
        RLog.d(this.TAG, "ConversationUnreadEvent");
        ConversationType conversationType = unreadEvent.getType();
        String targetId = unreadEvent.getTargetId();
        int position = this.mAdapter.findPosition(conversationType, targetId);
        if (position >= 0) {
            int first = this.mList.getFirstVisiblePosition();
            int last = this.mList.getLastVisiblePosition();
            UIConversation uiConversation = (UIConversation) this.mAdapter.getItem(position);
            uiConversation.clearUnRead(conversationType, targetId);
            if (position >= first && position <= last) {
                this.mAdapter.getView(position, this.mList.getChildAt(position - this.mList.getFirstVisiblePosition() + headerSize), this.mList);
            }
        }

        this.onUnreadCountChanged();
    }

    public void onEventMainThread(ConversationTopEvent setTopEvent) {
        RLog.d(this.TAG, "ConversationTopEvent");
        ConversationType conversationType = setTopEvent.getConversationType();
        String targetId = setTopEvent.getTargetId();
        int position = this.mAdapter.findPosition(conversationType, targetId);
        if (position >= 0) {
            UIConversation uiConversation = (UIConversation) this.mAdapter.getItem(position);
            if (uiConversation.isTop() != setTopEvent.isTop()) {
                uiConversation.setTop(!uiConversation.isTop());
                this.mAdapter.remove(position);
                int index = this.getPosition(uiConversation);
                this.mAdapter.add(uiConversation, index);
                if (index == position) {
                    this.mAdapter.getView(index, this.mList.getChildAt(index - this.mList.getFirstVisiblePosition() + headerSize), this.mList);
                } else {
                    this.mAdapter.notifyDataSetChanged();
                }
            }
        }

    }

    public void onEventMainThread(ConversationRemoveEvent removeEvent) {
        RLog.d(this.TAG, "ConversationRemoveEvent");
        ConversationType conversationType = removeEvent.getType();
        this.removeConversation(conversationType, removeEvent.getTargetId());
    }

    public void onEventMainThread(ClearConversationEvent clearConversationEvent) {
        RLog.d(this.TAG, "ClearConversationEvent");
        List<ConversationType> typeList = clearConversationEvent.getTypes();

        for (int i = this.mAdapter.getCount() - 1; i >= 0; --i) {
            if (typeList.indexOf(((UIConversation) this.mAdapter.getItem(i)).getConversationType()) >= 0) {
                this.mAdapter.remove(i);
            }
        }

        this.mAdapter.notifyDataSetChanged();
        this.onUnreadCountChanged();
    }

    public void onEventMainThread(MessageDeleteEvent event) {
        Log.e("TAG", "MessageDeleteEvent");
        int count = this.mAdapter.getCount();

        for (int i = 0; i < count; ++i) {
            if (event.getMessageIds().contains(Integer.valueOf(((UIConversation) this.mAdapter.getItem(i)).getLatestMessageId()))) {
                boolean gatherState = ((UIConversation) this.mAdapter.getItem(i)).getConversationGatherState();
                final String targetId = ((UIConversation) this.mAdapter.getItem(i)).getConversationTargetId();
                if (gatherState) {
                    RongIM.getInstance().getConversationList(new ResultCallback<List<Conversation>>() {
                        public void onSuccess(List<Conversation> conversationList) {
                            if (conversationList != null && conversationList.size() != 0) {
                                UIConversation uiConversation = CustomListFragment.this.makeUIConversation(conversationList);
                                int oldPos = CustomListFragment.this.mAdapter.findPosition(uiConversation.getConversationType(), targetId);
                                if (oldPos >= 0) {
                                    CustomListFragment.this.mAdapter.remove(oldPos);
                                }

                                int newIndex = CustomListFragment.this.getPosition(uiConversation);
                                CustomListFragment.this.mAdapter.add(uiConversation, newIndex);
                                CustomListFragment.this.mAdapter.notifyDataSetChanged();
                            }
                        }

                        public void onError(ErrorCode e) {
                        }
                    }, new ConversationType[]{((UIConversation) this.mAdapter.getItem(i)).getConversationType()});
                } else {
                    RongIM.getInstance().getConversation(((UIConversation) this.mAdapter.getItem(i)).getConversationType(), ((UIConversation) this.mAdapter.getItem(i)).getConversationTargetId(), new ResultCallback<Conversation>() {
                        public void onSuccess(Conversation conversation) {
                            if (conversation == null) {
                                RLog.d(CustomListFragment.this.TAG, "onEventMainThread getConversation : onSuccess, conversation = null");
                            } else {
                                UIConversation uiConversation = UIConversation.obtain(CustomListFragment.this.getActivity(), conversation, false);
                                int pos = CustomListFragment.this.mAdapter.findPosition(conversation.getConversationType(), conversation.getTargetId());
                                if (pos >= 0) {
                                    CustomListFragment.this.mAdapter.remove(pos);
                                }

                                int newIndex = CustomListFragment.this.getPosition(uiConversation);
                                CustomListFragment.this.mAdapter.add(uiConversation, newIndex);
                                CustomListFragment.this.mAdapter.notifyDataSetChanged();
                            }
                        }

                        public void onError(ErrorCode e) {
                        }
                    });
                }
                break;
            }
        }

    }

    public void onEventMainThread(ConversationNotificationEvent notificationEvent) {
        Log.e("TAG", "ConversationNotificationEvent");
        int originalIndex = this.mAdapter.findPosition(notificationEvent.getConversationType(), notificationEvent.getTargetId());
        if (originalIndex >= 0) {
            UIConversation uiConversation = (UIConversation) this.mAdapter.getItem(originalIndex);
            if (!uiConversation.getNotificationStatus().equals(notificationEvent.getStatus())) {
                uiConversation.setNotificationStatus(notificationEvent.getStatus());
                this.mAdapter.getView(originalIndex, this.mList.getChildAt(originalIndex - this.mList.getFirstVisiblePosition() + headerSize), this.mList);
            }

            this.onUnreadCountChanged();
        }

    }

    public void onEventMainThread(MessagesClearEvent clearMessagesEvent) {
        RLog.d(this.TAG, "MessagesClearEvent");
        ConversationType conversationType = clearMessagesEvent.getType();
        String targetId = clearMessagesEvent.getTargetId();
        int position = this.mAdapter.findPosition(conversationType, targetId);
        if (position >= 0) {
            UIConversation uiConversation = (UIConversation) this.mAdapter.getItem(position);
            uiConversation.clearLastMessage();
            this.mAdapter.getView(position, this.mList.getChildAt(position - this.mList.getFirstVisiblePosition() + headerSize), this.mList);
        }

    }

    public void onEventMainThread(OnMessageSendErrorEvent sendErrorEvent) {
        Message message = sendErrorEvent.getMessage();
        ConversationType conversationType = message.getConversationType();
        String targetId = message.getTargetId();
        if (this.isConfigured(conversationType)) {
            int first = this.mList.getFirstVisiblePosition();
            int last = this.mList.getLastVisiblePosition();
            boolean gathered = false;
            int index = gathered ? this.mAdapter.findGatheredItem(conversationType) : this.mAdapter.findPosition(conversationType, targetId);
            if (index >= 0) {
                UIConversation uiConversation = (UIConversation) this.mAdapter.getItem(index);
                message.setSentStatus(SentStatus.FAILED);
                uiConversation.updateConversation(message, gathered);
                if (index >= first && index <= last) {
                    this.mAdapter.getView(index, this.mList.getChildAt(index - this.mList.getFirstVisiblePosition() + headerSize), this.mList);
                }
            }
        }

    }


    private void removeConversation(final ConversationType conversationType, String targetId) {

        int index = this.mAdapter.findPosition(conversationType, targetId);
        if (index >= 0) {
            this.mAdapter.remove(index);
            this.mAdapter.notifyDataSetChanged();
            this.onUnreadCountChanged();
        }
    }


    private void makeUiConversationList(List<Conversation> conversationList) {
        Iterator var3 = conversationList.iterator();

        while (var3.hasNext()) {
            Conversation conversation = (Conversation) var3.next();
            ConversationType conversationType = conversation.getConversationType();
            String targetId = conversation.getTargetId();
            boolean gatherState = false;
            UIConversation uiConversation;
            int originalIndex;
            if (gatherState) {
                originalIndex = this.mAdapter.findGatheredItem(conversationType);
                if (originalIndex >= 0) {
                    uiConversation = (UIConversation) this.mAdapter.getItem(originalIndex);
                    uiConversation.updateConversation(conversation, true);
                } else {
                    uiConversation = UIConversation.obtain(this.getActivity(), conversation, true);
                    this.mAdapter.add(uiConversation);
                    this.onUIConversationCreated(uiConversation);
                }
            } else {
                originalIndex = this.mAdapter.findPosition(conversationType, targetId);
                int index;
                if (originalIndex < 0) {
                    uiConversation = UIConversation.obtain(this.getActivity(), conversation, false);
                    index = this.getPosition(uiConversation);
                    this.mAdapter.add(uiConversation, index);
                    this.onUIConversationCreated(uiConversation);
                } else {
                    uiConversation = (UIConversation) this.mAdapter.getItem(originalIndex);
                    if (uiConversation.getUIConversationTime() < conversation.getSentTime()) {
                        this.mAdapter.remove(originalIndex);
                        uiConversation.updateConversation(conversation, false);
                        index = this.getPosition(uiConversation);
                        this.mAdapter.add(uiConversation, index);
                    } else {
                        uiConversation.setUnReadMessageCount(conversation.getUnreadMessageCount());
                    }
                }
            }
        }

    }

    private UIConversation makeUIConversation(List<Conversation> conversations) {
        int unreadCount = 0;
        boolean isMentioned = false;
        Conversation newest = (Conversation) conversations.get(0);

        Conversation conversation;
        for (Iterator var5 = conversations.iterator(); var5.hasNext(); unreadCount += conversation.getUnreadMessageCount()) {
            conversation = (Conversation) var5.next();
            if (newest.isTop()) {
                if (conversation.isTop() && conversation.getSentTime() > newest.getSentTime()) {
                    newest = conversation;
                }
            } else if (conversation.isTop() || conversation.getSentTime() > newest.getSentTime()) {
                newest = conversation;
            }

            if (conversation.getMentionedCount() > 0) {
                isMentioned = true;
            }
        }

        UIConversation uiConversation = UIConversation.obtain(this.getActivity(), newest, false);
        uiConversation.setUnReadMessageCount(unreadCount);
        uiConversation.setTop(false);
        uiConversation.setMentionedFlag(isMentioned);
        return uiConversation;
    }

    private int getPosition(UIConversation uiConversation) {
        int count = this.mAdapter.getCount();
        int position = 0;

        for (int i = 0; i < count; ++i) {
            if (uiConversation.isTop()) {
                if (!((UIConversation) this.mAdapter.getItem(i)).isTop() || ((UIConversation) this.mAdapter.getItem(i)).getUIConversationTime() <= uiConversation.getUIConversationTime()) {
                    break;
                }

                ++position;
            } else {
                if (!((UIConversation) this.mAdapter.getItem(i)).isTop() && ((UIConversation) this.mAdapter.getItem(i)).getUIConversationTime() <= uiConversation.getUIConversationTime()) {
                    break;
                }

                ++position;
            }
        }

        return position;
    }

    private boolean isConfigured(ConversationType conversationType) {
        return conversationType.equals(ConversationType.PRIVATE);
    }


    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this.mThis);
        super.onDestroyView();
    }

    private class ConversationConfig {
        ConversationType conversationType;
        boolean isGathered;

        private ConversationConfig() {
        }
    }
}
