package com.pinger.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.pinger.slide.SlideLayout;
import com.pinger.swipeview.SwipeScrollView;

public class MainActivity extends AppCompatActivity {

    private SlideLayout mSlideLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSlideLayout = (SlideLayout) findViewById(R.id.slideLayout);

        SwipeScrollView swipeScrollView = (SwipeScrollView) findViewById(R.id.swipeScrollView);
        View contentView = View.inflate(this, R.layout.layout_menu_content, null);
        initEvent(contentView);
        swipeScrollView.setContentView(contentView);
    }

    private void initEvent(View view) {
        view.findViewById(R.id.message).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "消息中心", Toast.LENGTH_SHORT).show();
                mSlideLayout.close();
            }
        });
        view.findViewById(R.id.skin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "皮肤", Toast.LENGTH_SHORT).show();
                mSlideLayout.close();
            }
        });
        view.findViewById(R.id.vip_center).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "会员中心", Toast.LENGTH_SHORT).show();
                mSlideLayout.close();
            }
        });
    }

    @Override
    public void onBackPressed() {

        if (mSlideLayout.isOpened()) {
            mSlideLayout.close();
        } else {
            super.onBackPressed();
        }
    }
}
