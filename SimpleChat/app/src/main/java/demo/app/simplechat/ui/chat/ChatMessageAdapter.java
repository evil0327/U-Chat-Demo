package demo.app.simplechat.ui.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import demo.app.simplechat.R;
import demo.app.simplechat.cache.UserLiveCache;
import demo.app.simplechat.db.ChatMessage;
import demo.app.simplechat.db.User;
import demo.app.simplechat.di.DaggerComponentHolder;
import demo.app.simplechat.repo.LocalRepository;
import demo.app.simplechat.util.GlideApp;
import demo.app.simplechat.util.ImageHelper;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ME = 0;
    private static final int TYPE_FRIEND = 1;

    private List<ChatMessage> mList = new ArrayList<>();
    @Inject
    LocalRepository mLocalRepository;
    @Inject
    UserLiveCache mUserLiveCache;

    private Context mContext;

    public ChatMessageAdapter(Context context) {
        DaggerComponentHolder.getAppComponent().inject(this);

        mContext = context;
    }

    public void setData(List<ChatMessage> list) {
        mList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ME) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_talk_me_layout, parent, false);
            return new TalkMeHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_talk_friend_layout, parent, false);
            return new TalkFriendHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = mList.get(position);
        if (getItemViewType(position) == TYPE_ME) {
            TalkMeHolder vh = (TalkMeHolder) holder;
            vh.messageView.setText(message.getContent());

            if (message.getState() == ChatMessage.STATE_PROCESSING) {
                vh.arrowView.setVisibility(View.VISIBLE);
            } else {
                vh.arrowView.setVisibility(View.INVISIBLE);
            }
        } else {
            TalkFriendHolder vh = (TalkFriendHolder) holder;

            User friend = mUserLiveCache.getUserByUid(message.getUserId());

            int avatar = 0;
            String name = "";

            if(friend!=null){
                avatar = friend.getAvatar();
                name = friend.getName();
            }

            GlideApp.with(mContext)
                    .load(ImageHelper.getResource(avatar))
                    .centerCrop()
                    .apply(RequestOptions.circleCropTransform())
                    .into(vh.avatarView);

            vh.messageView.setText(message.getContent());
            vh.nameView.setText(name);
        }
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage chatMessage = mList.get(position);
        String myUid = mLocalRepository.getUID();
        if (myUid.equals(chatMessage.getUserId())) {
            return TYPE_ME;
        }
        return TYPE_FRIEND;
    }

    public ChatMessage getLastMessage(){
        return mList.get(mList.size()-1);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class TalkMeHolder extends RecyclerView.ViewHolder {
        public TextView messageView;
        public View arrowView;

        public TalkMeHolder(@NonNull View itemView) {
            super(itemView);
            messageView = itemView.findViewById(R.id.message);
            arrowView = itemView.findViewById(R.id.arrow_view);
        }
    }

    class TalkFriendHolder extends RecyclerView.ViewHolder {
        public TextView messageView, nameView;
        public ImageView avatarView;

        public TalkFriendHolder(@NonNull View itemView) {
            super(itemView);
            messageView = itemView.findViewById(R.id.message);
            nameView = itemView.findViewById(R.id.name);
            avatarView = itemView.findViewById(R.id.avatar);
        }
    }


}
