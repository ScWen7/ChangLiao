package scwen.com.changliao;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.implments.SwipeItemAdapterMangerImpl;
import com.daimajia.swipe.interfaces.SwipeAdapterInterface;
import com.daimajia.swipe.interfaces.SwipeItemMangerInterface;
import com.daimajia.swipe.util.Attributes;

import java.util.List;

import io.rong.common.RLog;
import io.rong.imkit.RongContext;
import io.rong.imkit.model.ConversationProviderTag;
import io.rong.imkit.model.UIConversation;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imkit.widget.ProviderContainerView;
import io.rong.imkit.widget.adapter.BaseAdapter;
import io.rong.imkit.widget.provider.IContainerItemProvider;
import io.rong.imlib.model.Conversation;

/**
 * Created by xxh on 2018/6/8.
 */

public class CustomerListAdapter extends BaseAdapter<UIConversation> implements SwipeItemMangerInterface, SwipeAdapterInterface {
    private static final String TAG = "ConversationListAdapter";
    LayoutInflater mInflater;
    Context mContext;

    protected SwipeItemAdapterMangerImpl mItemManger = new SwipeItemAdapterMangerImpl(this);


    OnConversationEventListener mConversationEventListener;


    public long getItemId(int position) {
        UIConversation conversation = (UIConversation) this.getItem(position);
        return conversation == null ? 0L : (long) conversation.hashCode();
    }

    public CustomerListAdapter(Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(this.mContext);
    }

    public void setConversationEventListener(OnConversationEventListener conversationEventListener) {
        mConversationEventListener = conversationEventListener;
    }

    public int findGatheredItem(Conversation.ConversationType type) {
        int index = this.getCount();
        int position = -1;

        while (index-- > 0) {
            UIConversation uiConversation = (UIConversation) this.getItem(index);
            if (uiConversation.getConversationType().equals(type)) {
                position = index;
                break;
            }
        }

        return position;
    }

    public int findPosition(Conversation.ConversationType type, String targetId) {
        int index = this.getCount();
        int position = -1;

        while (index-- > 0) {
            if (((UIConversation) this.getItem(index)).getConversationType().equals(type) && ((UIConversation) this.getItem(index)).getConversationTargetId().equals(targetId)) {
                position = index;
                break;
            }
        }

        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView != null) {
            view = convertView;
            mItemManger.updateConvertView(view, position);
        } else {
            view = this.newView(this.mContext, position, parent);
            mItemManger.initialize(view, position);
        }

        this.bindView(view, position, this.getItem(position));
        return view;
    }

    protected View newView(Context context, int position, ViewGroup group) {
        View result = this.mInflater.inflate(R.layout.rc_item_conversation_cust, (ViewGroup) null);
        ViewHolder holder = new CustomerListAdapter.ViewHolder();
        holder.layout = this.findViewById(result, R.id.rc_item_conversation);
        holder.leftImageView = (AsyncImageView) this.findViewById(result, R.id.rc_left);
        holder.contentView = (ProviderContainerView) this.findViewById(result, R.id.rc_content);
        holder.unReadMsgCountIcon = (ImageView) this.findViewById(result, R.id.rc_unread_message_icon);
        holder.swipeLayout = this.findViewById(result, R.id.swipe);
        holder.ic_delete = this.findViewById(result, R.id.ic_delete);
        result.setTag(holder);
        return result;
    }

    protected void bindView(View v, final int position, final UIConversation data) {
        final CustomerListAdapter.ViewHolder holder = (CustomerListAdapter.ViewHolder) v.getTag();
        if (data != null) {
            IContainerItemProvider provider = RongContext.getInstance().getConversationTemplate(data.getConversationType().getName());
            if (provider == null) {
                RLog.e("ConversationListAdapter", "provider is null");
            } else {
                View view = holder.contentView.inflate(provider);
                provider.bindView(view, position, data);
                if (data.isTop()) {
                    holder.layout.setBackgroundDrawable(this.mContext.getResources().getDrawable(io.rong.imkit.R.drawable.rc_item_top_list_selector));
                } else {
                    holder.layout.setBackgroundDrawable(this.mContext.getResources().getDrawable(io.rong.imkit.R.drawable.rc_item_list_selector));
                }

                ConversationProviderTag tag = RongContext.getInstance().getConversationProviderTag(data.getConversationType().getName());
                int defaultId;
                if (data.getConversationType().equals(Conversation.ConversationType.GROUP)) {
                    defaultId = io.rong.imkit.R.drawable.rc_default_group_portrait;
                } else if (data.getConversationType().equals(Conversation.ConversationType.DISCUSSION)) {
                    defaultId = io.rong.imkit.R.drawable.rc_default_discussion_portrait;
                } else {
                    defaultId = io.rong.imkit.R.drawable.rc_default_portrait;
                }

                if (data.getConversationGatherState()) {
                    holder.leftImageView.setAvatar((String) null, defaultId);
                } else if (data.getIconUrl() != null) {
                    holder.leftImageView.setAvatar(data.getIconUrl().toString(), defaultId);
                } else {
                    holder.leftImageView.setAvatar((String) null, defaultId);
                }

                if (data.getUnReadMessageCount() > 0) {
                    holder.unReadMsgCountIcon.setVisibility(View.VISIBLE);
                    holder.unReadMsgCountIcon.setImageResource(R.drawable.unread_message);
                } else {
                    holder.unReadMsgCountIcon.setVisibility(View.GONE);
                }

                holder.layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mConversationEventListener != null) {
                            mConversationEventListener.onClickToTalk(position, holder.layout);
                        }
                    }
                });

                holder.ic_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mConversationEventListener != null) {
                            mConversationEventListener.onDelete(position);
                        }
                    }
                });


            }

        }
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    @Override
    public void openItem(int position) {
        mItemManger.openItem(position);
    }

    @Override
    public void closeItem(int position) {
        mItemManger.closeItem(position);
    }

    @Override
    public void closeAllExcept(SwipeLayout layout) {
        mItemManger.closeAllExcept(layout);
    }

    @Override
    public void closeAllItems() {
        mItemManger.closeAllItems();
    }

    @Override
    public List<Integer> getOpenItems() {
        return mItemManger.getOpenItems();
    }

    @Override
    public List<SwipeLayout> getOpenLayouts() {
        return mItemManger.getOpenLayouts();
    }

    @Override
    public void removeShownLayouts(SwipeLayout layout) {
        mItemManger.removeShownLayouts(layout);
    }

    @Override
    public boolean isOpen(int position) {
        return mItemManger.isOpen(position);
    }

    @Override
    public Attributes.Mode getMode() {
        return mItemManger.getMode();
    }

    @Override
    public void setMode(Attributes.Mode mode) {
        mItemManger.setMode(mode);
    }

    protected class ViewHolder {
        public View layout;
        public AsyncImageView leftImageView;

        public ImageView unReadMsgCountIcon;
        public ProviderContainerView contentView;
        public SwipeLayout swipeLayout;

        public TextView ic_delete;

        protected ViewHolder() {
        }
    }


    public interface OnConversationEventListener {
        void onDelete(int position);

        void onClickToTalk(int position, View view);

    }


}
