package com.doraesol.dorandoran;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.doraesol.dorandoran.calendar.CalendarMainFragment;
import com.doraesol.dorandoran.map.MapMainFragment;
import com.doraesol.dorandoran.setting.SettingFragment;
import com.doraesol.dorandoran.social.CmtBoardFragment;
import com.doraesol.dorandoran.familytree.FamilyTreeFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
{
    final String LOG_TAG = MainActivity.class.getSimpleName();
    @BindView(R.id.vp_main_pager)   ViewPager viewPager;
    @BindView(R.id.tl_main_tabs)    TabLayout tabLayout;
    @BindView(R.id.tb_main_bar)     Toolbar tb_main_bar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Intent intent = getIntent();
        int page = intent.getExtras().getInt("page");
        int[] tabIcons = {
                //R.drawable.ic_list_home,
                R.drawable.ic_list_genogram,
                //R.drawable.ic_list_map,
                R.drawable.ic_list_social,
                R.drawable.ic_list_tool4
        };

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        BusProvider.getInstance().register(this);

        Fragment[] arrFragments = new Fragment[3];
        //arrFragments[0] = new HomeFragment();
        arrFragments[0] = new FamilyTreeFragment();
        //arrFragments[1] = new MapMainFragment();
        //arrFragments[3] = new CmtBoardFragment();
        arrFragments[1] = new CalendarMainFragment();
        arrFragments[2] = new SettingFragment();

        MainPagerAdapter adapter = new MainPagerAdapter(getSupportFragmentManager(), arrFragments);

        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(page, true);
        tabLayout.setupWithViewPager(viewPager);


        for(int i=0; i<tabLayout.getTabCount(); i++) {
            tabLayout.getTabAt(i).setIcon(tabIcons[i]);
        }


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                //Toast.makeText(getApplicationContext(), tab.getPosition()+"", Toast.LENGTH_SHORT).show();

                tb_main_bar.getMenu().clear();

                switch (tab.getPosition()){
                    case 0:
                        tb_main_bar.setTitle("가계도");


                        break;

                    case 1:
                        tb_main_bar.setTitle("경조사");
                        break;

                    case 2:
                        tb_main_bar.setTitle("설정");
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        tb_main_bar.setTitle("도란도란");
        tb_main_bar.setTitleTextColor(Color.WHITE);

    }



    @Override
    protected void onDestroy() {
        BusProvider.getInstance().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        BusProvider.getInstance().post(new ActivityResultEvent(requestCode, resultCode, data));
        Log.d(LOG_TAG, data.getStringExtra("name"));
}


    class MainPagerAdapter extends FragmentPagerAdapter {

        Fragment[] arrFragments;

        public MainPagerAdapter(FragmentManager fm, Fragment[] arrFragments) {
            super(fm);
            this.arrFragments = arrFragments;


        }


        /* 메뉴 텍스트
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                /
                case 0:
                    return "FAMILYTREE";
                case 1:
                    return "MAP";
                case 2:
                    return "CALENDAR";
                case 3:
                    return "SETTING";
                default:
                    return "";
            }
        }
        */

        @Override
        public Fragment getItem(int position) {

            return arrFragments[position];
        }

        @Override
        public int getCount() {
            return arrFragments.length;
        }
    }
}
