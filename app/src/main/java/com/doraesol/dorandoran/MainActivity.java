package com.doraesol.dorandoran;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.doraesol.dorandoran.calendar.CalendarMainFragment;
import com.doraesol.dorandoran.calendar.IconSpinnerAdapter;
import com.doraesol.dorandoran.familytree.FamilyTreeFragment;
import com.doraesol.dorandoran.setting.SettingFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    final String LOG_TAG = MainActivity.class.getSimpleName();
    @BindView(R.id.vp_main_pager)   ViewPager viewPager;
    @BindView(R.id.tl_main_tabs)    TabLayout tabLayout;
    @BindView(R.id.tb_main_bar)     Toolbar tb_main_bar;
    @BindView(R.id.tv_main_bar_title) TextView tv_main_bar_title;

    // Spinner 리소스 바인딩 에러남
    Spinner sp_schedule_insert;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Intent intent = getIntent();
        int page = intent.getExtras().getInt("page");
        int[] tabIcons = {
                //R.drawable.ic_list_home,
                R.drawable.ic_list_genogram,
                //R.drawable.ic_list_map,
                R.drawable.img_main_calendar,
                R.drawable.ic_list_tool4
        };

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        tv_main_bar_title.setText("가계도");
        //BusProvider.getInstance().register(this);

        //sp_schedule_insert = (Spinner)findViewById(R.id.sp_schedule_insert);

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
                tb_main_bar.getMenu().clear();

                switch (tab.getPosition()){
                    case 0:
                        //tb_main_bar.setTitle("가계도");
                        tv_main_bar_title.setText("가계도");
                        break;

                    case 1:
                        //tb_main_bar.setTitle("경조사");
                        tv_main_bar_title.setText("경조사");
                        tb_main_bar.inflateMenu(R.menu.menu_schedule_insert);
                        tb_main_bar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                if(item.getItemId() == R.id.menu_schedule_insert){
                                    showScheduleDlg();
                                }

                                return false;
                            }
                        });
                        break;

                    case 2:
                        //tb_main_bar.setTitle("설정");
                        tv_main_bar_title.setText("설정");
                        break;
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });


        tb_main_bar.setTitle("도란도란");
        tb_main_bar.setTitleTextColor(Color.WHITE);
        //setSpinnerAdapter();
    }


    private void setSpinnerAdapter(){
        int[] imageResource = new int[]{R.drawable.ic_list_home,
                R.drawable.ic_list_map,
                R.drawable.ic_list_social};
        sp_schedule_insert.setOnItemClickListener(this);
        IconSpinnerAdapter iconSpinnerAdapter = new IconSpinnerAdapter(getApplicationContext(), imageResource);
        sp_schedule_insert.setAdapter(iconSpinnerAdapter);
    }

    private void showScheduleDlg(){
        
    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }


    @Override
    protected void onDestroy() {
        BusProvider.getInstance().unregister(this);
        super.onDestroy();
    }

    @OnClick(R.id.tv_main_bar_title)
    public void OnLoginButtonClicked() {
        Toast.makeText(this, "복구 파일을 불러왔습니다.", Toast.LENGTH_LONG).show();
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
