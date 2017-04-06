package com.home.honor.home;


import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.support.design.widget.TabLayout;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ArrayList<Fragment> datas;
    public static NoScrollViewPager viewpager;
    private TabLayout tablayout;
    private SendViewPagerAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        datas = new ArrayList<>();
        datas.add(new WeatherFragment());//将几个fragment加入datas中,data为fragment类型的集合
        datas.add(new CameraFragment());
        datas.add(new MusicFragment());
        datas.add(new MovieFragment());
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        viewpager = (NoScrollViewPager) findViewById(R.id.viewPager);//绑定viewpager
        tablayout = (TabLayout) findViewById(R.id.tabLayout);//绑定tablayout
        adapter = new SendViewPagerAdapter(getSupportFragmentManager(), datas,this);//初始化fragment类型adapter,datas为adapter构造函数参数
        viewpager.setAdapter(adapter);//将adapter加入viewpager中
//        tablayout.setTabTextColors(Color.BLUE,Color.GREEN);
        //加入title选择和被选择的颜色,前面为未点击的颜色,后面为点击后的颜色
        tablayout.setupWithViewPager(viewpager);//将tablayout与viewpager建立关系
        viewpager.setOffscreenPageLimit(4);
        for(int i = 0;i < tablayout.getTabCount();i++){//判断tablayout有几个菜单数量
            TabLayout.Tab tab = tablayout.getTabAt(i);
            if (tab != null){
                tab.setCustomView(adapter.getTabView(i));//将adapter设置好的获取图片和文字的方法设置到视图中
            }
        }
        viewpager.setCurrentItem(0);
        StatusBarUtils.setFullScreen(this);
        new PlayControl();
    }
}
