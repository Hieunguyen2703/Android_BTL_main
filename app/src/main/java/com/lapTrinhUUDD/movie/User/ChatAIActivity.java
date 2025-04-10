package com.lapTrinhUUDD.movie.User;

import static com.lapTrinhUUDD.movie.Common.ChatBotAI.futureModel;
import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.EditText;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.lapTrinhUUDD.movie.Adapter.ChatAdapter;
import com.lapTrinhUUDD.movie.Common.ChatCallback;
import com.lapTrinhUUDD.movie.Models.Message;
import com.lapTrinhUUDD.movie.R;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAIActivity extends AppCompatActivity implements ChatCallback {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<Message> mChats = new ArrayList<>();
    private final ChatAdapter chatAdapter = new ChatAdapter(this);
    private boolean isGenerating = false;
    private RecyclerView rcv;
    private EditText etInput;
    private ConstraintLayout rootView;

    @Override
    @SuppressLint("MissingInflatedId")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        EdgeToEdge.enable(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setContentView(R.layout.activity_chatvie_aiactivity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ll_chat), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setUpView();
        setUpRecyclerView();
        setUpOnClick();
        fixKeyboardOverlap();

    }

    private void fixKeyboardOverlap() {
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect rect = new Rect();
            rootView.getWindowVisibleDisplayFrame(rect);

            int screenHeight = rootView.getRootView().getHeight();
            int keypadHeight = screenHeight - rect.bottom;

            if (keypadHeight > screenHeight * 0.15) {
                etInput.setTranslationY(-keypadHeight);

                rcv.setPadding(
                        rcv.getPaddingLeft(),
                        rcv.getPaddingTop(),
                        rcv.getPaddingRight(),
                        keypadHeight + 50
                );

            } else {
                etInput.setTranslationY(0);
                rcv.setPadding(
                        rcv.getPaddingLeft(),
                        rcv.getPaddingTop(),
                        rcv.getPaddingRight(),
                        50
                );
            }
        });
    }

    private void setUpView() {
        rcv = findViewById(R.id.rcv);
        etInput = findViewById(R.id.editTextText);
        rootView = findViewById(R.id.ll_chat);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUpOnClick() {
        etInput.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP && etInput.getCompoundDrawables()[2] != null) {
                int drawableWidth = etInput.getCompoundDrawables()[2].getBounds().width();
                if (event.getRawX() >= (etInput.getRight() - drawableWidth)) {
                    String content = etInput.getText().toString();
                    if (!content.isEmpty()) {
                        Message message = new Message(content, timeCurrent(), ChatAdapter.RIGHT);
                        mChats.add(message);
                        etInput.getText().clear();
                        chatAdapter.submitList(mChats);
                        rcv.post(() -> rcv.scrollToPosition(mChats.size() - 1));
                    }
                    return true;
                }
            }
            return false;
        });


    }

    private void setUpRecyclerView() {
        rcv.setLayoutManager(new LinearLayoutManager(this));
        rcv.setAdapter(chatAdapter);
    }

    private String timeCurrent() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    private void generateAIResponse(String prompt) {
        if (isGenerating) {
            return;
        }

        isGenerating = true;

        Message loadingMessage = new Message("...", timeCurrent(), ChatAdapter.LEFT);
        mChats.add(loadingMessage);
        chatAdapter.submitList(mChats);
        rcv.scrollToPosition(mChats.size() - 1);

        Content content = new Content.Builder()
                .addText(prompt)
                .build();

        StringBuilder fullText = new StringBuilder();

        Publisher<GenerateContentResponse> response = futureModel.generateContentStream(content);

        response.subscribe(new Subscriber<>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(GenerateContentResponse response) {
                String textChunk = response.getText() != null ? response.getText() : "";

                for (char c : textChunk.toCharArray()) {
                    fullText.append(c);

                    runOnUiThread(() -> updateLastMessage(fullText.toString()));

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onError(Throwable t) {
                Log.e("AI Error", t.getMessage() != null ? t.getMessage() : "Unknown error");
                runOnUiThread(() -> updateLastMessage("Đã xảy ra lỗi khi tạo phản hồi."));
                isGenerating = false;
            }

            @Override
            public void onComplete() {
                isGenerating = false;
            }
        });
    }

    private void updateLastMessage(String content) {
        if (!mChats.isEmpty()) {
            Message lastMessage = mChats.get(mChats.size() - 1);

            if (!lastMessage.getContent().equals(content)) {
                lastMessage.setContent(content);
                chatAdapter.notifyItemChanged(mChats.size() - 1);
                rcv.scrollToPosition(mChats.size() - 1);
            }
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onMessageSent(Message message) {
        if (!isGenerating) {
            generateAIResponse(message.getContent());
        }
    }
}
