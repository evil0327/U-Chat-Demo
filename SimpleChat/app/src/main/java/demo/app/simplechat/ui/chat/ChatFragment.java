package demo.app.simplechat.ui.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import demo.app.simplechat.R;
import demo.app.simplechat.db.ChatMessage;
import demo.app.simplechat.di.DaggerComponentHolder;
import demo.app.simplechat.repo.LocalRepository;
import demo.app.simplechat.util.MyEvent;
import demo.app.simplechat.vm.ChatViewModel;

public class ChatFragment extends Fragment {
    private Unbinder mUnbinder;
    private ChatMessageAdapter mAdapter;
    private List<ChatMessage> mList = new ArrayList<>();
    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    private ChatViewModel mViewModel;

    @Inject
    LocalRepository mLocalRepository;

    private EventBus mEventBus;

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.edit)
    EditText mInputEdit;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        mAdapter = new ChatMessageAdapter(getContext());
        mAdapter.setData(mList);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, true);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int pastVisiblesItems = linearLayoutManager.findFirstVisibleItemPosition();
                int visibleItemCount = linearLayoutManager.getChildCount();
                int totalItemCount = linearLayoutManager.getItemCount();

                if (visibleItemCount + pastVisiblesItems >= totalItemCount) {
                    if(mAdapter.getItemCount()>0){
                        ChatMessage lastMessage = mAdapter.getLastMessage();
                        mViewModel.loadMessages(lastMessage.getCreateTime());
                    }
                }
            }
        });
        return view;
    }

    @OnClick(R.id.back)
    public void back(){
        Navigation.findNavController(getView()).navigateUp();
    }

    @OnClick(R.id.send)
    public void sendMessage(){
        if (TextUtils.isEmpty(mInputEdit.getText().toString())) {
            return;
        }
        String content = mInputEdit.getText().toString();
        final ChatMessage chat = new ChatMessage();
        final long ct = System.currentTimeMillis();

        chat.setCreateTime(ct);
        chat.setContent(content);
        chat.setUserId(mLocalRepository.getUID());
        chat.setState(ChatMessage.STATE_PROCESSING);

        mInputEdit.setText("");
        mViewModel.sendMessage(chat);
        scrollToBottom(0);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DaggerComponentHolder.getAppComponent().inject(this);

        mEventBus = EventBus.getDefault();

        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(ChatViewModel.class);
        mViewModel.getMessagesLiveData().observe(this, (Observer<List<ChatMessage>>) chatMessages -> {
            if(mAdapter.getItemCount()==0){
                scrollToBottom(100);
            }
            mAdapter.setData(chatMessages);
        });

        mViewModel.loadMessages(Long.MAX_VALUE);
        scrollToBottom(0);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MyEvent event) {
        if(MyEvent.NOTIFY_USER_CHANGE.equals(event.getEvent())){
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mViewModel.startListen();
        mEventBus.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mViewModel.stopListen();
        mEventBus.unregister(this);
    }

    private void scrollToBottom(long delay){
        mRecyclerView.postDelayed(() -> mRecyclerView.smoothScrollToPosition(0), delay);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }
}