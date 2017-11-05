package com.doraesol.dorandoran.setting;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.doraesol.dorandoran.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingNoticeActivity extends AppCompatActivity
{

    @BindView(R.id.elv_notice_list)
    ExpandableListView elv_notice_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_notice);
        ButterKnife.bind(this);

        ArrayList<NoticeItem> noticeItemList = new ArrayList<>();

        for(int i=0; i<5; i++){
            NoticeItem item = new NoticeItem(""+ i);
            for(int j=0; j<3; j++) {
                item.childList.add("" + j);
            }
            noticeItemList.add(item);
        }

        NoticeExpandableListAdapter adapter = new NoticeExpandableListAdapter(getApplicationContext(),
                noticeItemList, R.layout.setting_notice_parent, R.layout.setting_notice_child);


        Display display = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        elv_notice_list.setIndicatorBounds(width-130, width-40);
        elv_notice_list.setAdapter(adapter);
    }

    class NoticeExpandableListAdapter extends BaseExpandableListAdapter
    {
        Context context;
        int groupLayout = 0;
        int childLayout = 0;
        ArrayList<NoticeItem> noticeItemList;
        LayoutInflater inflater = null;

        public NoticeExpandableListAdapter(Context context, ArrayList<NoticeItem> noticeItemList, int groupLayout, int childLayout) {
            this.context = context;
            this.noticeItemList = noticeItemList;
            this.groupLayout = groupLayout;
            this.childLayout = childLayout;
            this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getGroupCount() {
            return noticeItemList.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return noticeItemList.get(groupPosition).childList.size();
        }

        @Override
        public NoticeItem getGroup(int groupPosition) {
            return noticeItemList.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return noticeItemList.get(groupPosition).childList.get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = inflater.inflate(this.groupLayout, parent, false);
            }

            TextView tv_notice_title = (TextView) convertView.findViewById(R.id.tv_notice_title);
            tv_notice_title.setText(noticeItemList.get(groupPosition).title);

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = inflater.inflate(this.childLayout, parent, false);
            }
            TextView tv_notice_content = (TextView)convertView.findViewById(R.id.tv_notice_content);
            tv_notice_content.setText(noticeItemList.get(groupPosition).childList.get(childPosition));

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}
