package com.example.admin.circlepath;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.circlepath_library.CircleMenu;
import com.example.circlepath_library.CircleMenuLisenter;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RelativeLayout rootView = (RelativeLayout) findViewById(R.id.root_view);

//        CircleMenu menu1 = new CircleMenu(this);
//        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        menu1.setLayoutParams(params);
//        menu1.setCenterBitmap(R.mipmap.light_blue_center);
//        rootView.addView(menu1);

        CircleMenu menu2 = (CircleMenu) findViewById(R.id.circleMenu);
        menu2.setCenterBitmap(R.mipmap.light_blue_center);
        menu2.setCentertitle("");
        menu2.setOnMenuListener(new CircleMenuLisenter() {
            @Override
            public void menuOpen() {
                //菜单展开
            }

            @Override
            public void menuClose() {
                //菜单关闭
            }

            @Override
            public void menuItemClicked(int menuNumber) {
                Toast.makeText(MainActivity.this,"点击了第"+menuNumber+"个小球",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void isMove() {
                //正在移动
            }
        });

    }
}
