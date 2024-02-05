package com.coldmint.rust.pro.ui;

import android.content.Context;
import android.text.Editable;
import android.text.NoCopySpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.coldmint.rust.pro.R;

public class Edit extends LinearLayout {
    public Edit(@NonNull Context context) {
        super(context);
        initView();
    }

    AttributeSet attrs;
    public Edit(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.attrs=attrs;
        initView();
    }

    public Edit(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.attrs=attrs;
        initView();
    }

    ImageView chahao;
    public EditText editText;

    private void initView() {
        setGravity(Gravity.CENTER_VERTICAL);
//        setBackgroundResource(R.drawable.ui_editview);
        setPadding(30, 10, 30, 10);
        //构建编辑框
        editText = new EditText(getContext());
        editText.setHint("搜索");
        editText.setSingleLine(true);
        editText.setBackground(null);
        editText.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence var1, int var2, int var3, int var4) {

            }

            @Override
            public void onTextChanged(CharSequence var1, int var2, int var3, int var4) {
                iskong();
            }

            @Override
            public void afterTextChanged(Editable var1) {

            }
        });

        ImageView imageView = new ImageView(getContext());
        imageView.setImageResource(R.drawable.ic_search_black_24dp);
        addView(imageView, (int) TypedValue.applyDimension(TypedValue.
                        COMPLEX_UNIT_DIP, 26, getResources().getDisplayMetrics())
                , (int) TypedValue.applyDimension(TypedValue.
                        COMPLEX_UNIT_DIP, 26, getResources().getDisplayMetrics()));

        LayoutParams layoutParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1;
        layoutParams.gravity = Gravity.CENTER;
        addView(editText, layoutParams);

        chahao = new ImageView(getContext());
        chahao.setImageResource(R.drawable.ic_outline_clear_24);
        chahao.setOnClickListener(view -> {
            editText.setText("");
            view.setVisibility(GONE);
        });
        addView(chahao, (int) TypedValue.applyDimension(TypedValue.
                        COMPLEX_UNIT_DIP, 26, getResources().getDisplayMetrics())
                , (int) TypedValue.applyDimension(TypedValue.
                        COMPLEX_UNIT_DIP, 26, getResources().getDisplayMetrics()));
        iskong();
    }

    public void addTextChangedListener(TextWatcher textWatcher) {
        editText.addTextChangedListener(textWatcher);
    }

    public void iskong() {
        if (editText.getText().toString().isEmpty()) {
            chahao.setVisibility(GONE);
        } else {
            chahao.setVisibility(VISIBLE);
        }
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
//        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        super.setLayoutParams(params);
//        setGravity(Gravity.CENTER_VERTICAL);
    }

    public void setOnEditorActionListener(TextView.OnEditorActionListener a) {
        editText.setOnEditorActionListener(a);
    }

    public void setText(String s) {
        editText.setText(s);
    }

    public Editable getText() {
        return editText.getText();
    }

    public interface TextWatcher extends NoCopySpan, android.text.TextWatcher {
        void beforeTextChanged(CharSequence var1, int var2, int var3, int var4);

        void onTextChanged(CharSequence var1, int var2, int var3, int var4);

        void afterTextChanged(Editable var1);
    }
}
