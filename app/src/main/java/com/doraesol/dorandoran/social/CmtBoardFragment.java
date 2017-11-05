package com.doraesol.dorandoran.social;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.doraesol.dorandoran.R;
import com.github.clans.fab.FloatingActionMenu;
import com.github.clans.fab.FloatingActionButton;
import java.util.ArrayList;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * A simple {@link Fragment} subclass.
 */
public class CmtBoardFragment extends Fragment {

    ArrayList<Cmt.Post> cmtPostList;
    CmtViewAdpater cmtViewAdpater;
    /*@BindView(R.id.fam_cmt)         FloatingActionMenu fam_cmt;
    @BindView(R.id.fab_cmt_write)   FloatingActionButton fab_cmt_write;*/


    public CmtBoardFragment() {
        cmtPostList = new ArrayList<>();
        // dummy
        cmtPostList.clear();
        for(int i=0; i<10; i++){
            Cmt.Post post = new Cmt.Post(i, "기돈맘", "제사지냈어요~", 5, false, 3, "0", "", "");
            cmtPostList.add(post);
        }
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_cmt_board, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv_cmt_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        cmtViewAdpater = new CmtViewAdpater();
        recyclerView.setAdapter(cmtViewAdpater);
        ButterKnife.bind(this, view);
       /* fam_cmt.setClosedOnTouchOutside(true);*/

        return view;
    }

    class CmtViewAdpater extends RecyclerView.Adapter<CmtViewHolder>{

        @Override
        public CmtViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.map_cmt_post_item, parent, false);
            CmtViewHolder cmtViewHolder = new CmtViewHolder(view);

            return cmtViewHolder;
        }

        @Override
        public void onBindViewHolder(CmtViewHolder holder, int position) {

            Cmt.Post item = cmtPostList.get(position);
            String url = item.getImg_url();

            // get post image from server url
            /*
            Glide.with(CmtBoardFragment.this)
                    .load(url)
                    .centerCrop()
                    .crossFade()
                    .into(holder.iv_cmt_post);
            */

            holder.tv_cmt_uploader.setText(item.getUploader());
            holder.tv_cmt_content.setText(item.getContents());
            holder.tv_cmt_like_count.setText(""+item.getLikes_count());
            holder.tv_cmt_comment_count.setText(""+item.getComments_count());
        }

        @Override
        public int getItemCount() {
            return cmtPostList.size();
        }
    }

    class CmtViewHolder extends RecyclerView.ViewHolder{

        public TextView tv_cmt_uploader;
        public TextView tv_cmt_content;
        public TextView tv_cmt_like_count;
        public TextView  tv_cmt_comment_count;
        public ImageView iv_cmt_post;


        public CmtViewHolder(View itemView) {
            super(itemView);

            tv_cmt_uploader = (TextView)itemView.findViewById(R.id.tv_cmt_uploader);
            tv_cmt_content = (TextView)itemView.findViewById(R.id.tv_cmt_content);
            tv_cmt_like_count = (TextView)itemView.findViewById(R.id.tv_cmt_like_count);
            tv_cmt_comment_count = (TextView)itemView.findViewById(R.id.tv_cmt_comment_count);
            iv_cmt_post = (ImageView)itemView.findViewById(R.id.iv_cmt_post);
        }
    }

    /*@OnClick(R.id.fab_cmt_write)
    public void onFabButtonClicked(View view){
        switch(view.getId()){
            case R.id.fab_cmt_write:
                startActivity(new Intent(getActivity(), CmtBoardWriteActivity.class));
                break;
        }
    }*/
}
