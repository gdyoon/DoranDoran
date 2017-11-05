package com.doraesol.dorandoran.setting;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.doraesol.dorandoran.LoginActivity;
import com.doraesol.dorandoran.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends Fragment {

    @BindView(R.id.rl_setting_mypage) RelativeLayout rl_setting_mypage;
    @BindView(R.id.rl_setting_notice) RelativeLayout rl_setting_notice;
    @BindView(R.id.rl_setting_alarm)  RelativeLayout rl_setting_alarm;
    @BindView(R.id.rl_setting_logout) RelativeLayout rl_setting_logout;

    public SettingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        ButterKnife.bind(this, view);

        return view;
    }


    @OnClick({R.id.rl_setting_mypage,
              R.id.rl_setting_notice,
              R.id.rl_setting_alarm,
              R.id.rl_setting_logout})
    public void OnClick(View view)
    {
        switch(view.getId())
        {
            case R.id.rl_setting_mypage:
                startActivity(new Intent(getActivity(), SettingMyPageActivity.class));
                break;
            case R.id.rl_setting_notice:
                startActivity(new Intent(getActivity(), SettingNoticeActivity.class));
                break;
            case R.id.rl_setting_alarm:
                break;
            case R.id.rl_setting_logout:
                startActivity(new Intent(getActivity(), LoginActivity.class));
                break;
            default:
                break;
        }
    }

}
