package com.home.honor.home;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.home.honor.home.R;

import java.util.ArrayList;

public class SendViewPagerAdapter extends FragmentPagerAdapter {
    private ArrayList<Fragment> datas;//因为要用将fragment加入到viewpager之中,所以内容为fragment类型的集合
    private Context context;

    private int[] imageviewId = {R.drawable.weather, R.drawable.camera, R.drawable.music, R.drawable.movie};

    public SendViewPagerAdapter(FragmentManager fm, ArrayList<Fragment> datas, Context context) {//设置构造函数,将fragment传入到viewpager中
        super(fm);
        this.datas = datas;
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        return datas.get(position);
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override//tablayout和viewpager结合之后,创建底部title使用的方法
    public CharSequence getPageTitle(int position) {

        return null;
    }

    public View getTabView(int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.maintab, null);
        //TextView textview = (TextView) view.findViewById(R.id.tab_tv1);
        ImageView imageview = (ImageView) view.findViewById(R.id.tab_iv1);
        //textview.setText(titles[position]);
        imageview.setImageResource(imageviewId[position]);
        return view;
    }
}