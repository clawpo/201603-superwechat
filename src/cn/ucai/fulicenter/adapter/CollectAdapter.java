package cn.ucai.fulicenter.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

import cn.ucai.fulicenter.D;
import cn.ucai.fulicenter.FuLiCenterApplication;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.activity.CollectActivity;
import cn.ucai.fulicenter.activity.GoodDetailsActivity;
import cn.ucai.fulicenter.bean.CollectBean;
import cn.ucai.fulicenter.bean.MessageBean;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
import cn.ucai.fulicenter.task.DownloadCollectCountTask;
import cn.ucai.fulicenter.utils.ImageUtils;
import cn.ucai.fulicenter.utils.Utils;
import cn.ucai.fulicenter.view.FooterViewHolder;

import static android.support.v7.widget.RecyclerView.ViewHolder;

/**
 * Created by clawpo on 16/6/15.
 */
public class CollectAdapter extends RecyclerView.Adapter<ViewHolder> {
    CollectActivity mContext;
    ArrayList<CollectBean> mCollectList;

    CollectItemViewHolder collectHolder;
    FooterViewHolder footerHolder;

    private String footerText;
    private boolean isMore;

    public void setFooterText(String footerText) {
        this.footerText = footerText;
        notifyDataSetChanged();
    }

    public boolean isMore() {
        return isMore;
    }

    public void setMore(boolean more) {
        isMore = more;
    }

    public CollectAdapter(Context mContext, ArrayList<CollectBean> mGoodList) {
        this.mContext = (CollectActivity)mContext;
        this.mCollectList = mGoodList;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewHolder holder = null;
        switch (viewType){
            case I.TYPE_ITEM:
                holder = new CollectItemViewHolder(inflater.inflate(R.layout.item_collect,parent,false));
                break;
            case I.TYPE_FOOTER:
                holder = new FooterViewHolder(inflater.inflate(R.layout.item_footer,parent,false));
                break;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(holder instanceof FooterViewHolder){
            footerHolder = (FooterViewHolder) holder;
            footerHolder.tvFooter.setText(footerText);
            footerHolder.tvFooter.setVisibility(View.VISIBLE);
        }
        if(holder instanceof CollectItemViewHolder){
            collectHolder = (CollectItemViewHolder) holder;
            final CollectBean collect = mCollectList.get(position);
            collectHolder.tvCollectName.setText(collect.getGoodsName());
            ImageUtils.setNewGoodThumb(collect.getGoodsThumb(), collectHolder.nivThumb);

            collectHolder.layoutItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.startActivity(new Intent(mContext, GoodDetailsActivity.class)
                            .putExtra(D.NewGood.KEY_GOODS_ID,collect.getGoodsId()));
                }
            });

            collectHolder.ivDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String path = new ApiParams()
                                .with(I.Collect.GOODS_ID, collect.getGoodsId()+"")
                                .with(I.Collect.USER_NAME, FuLiCenterApplication.getInstance().getUserName())
                                .getRequestUrl(I.REQUEST_DELETE_COLLECT);
                        Log.e("collectAdapter","path="+path);
                        mContext.executeRequest(new GsonRequest<MessageBean>(path, MessageBean.class,
                                new Response.Listener<MessageBean>() {
                                    @Override
                                    public void onResponse(MessageBean messageBean) {
                                        if(messageBean.isSuccess()){
                                            mCollectList.remove(collect);
                                            notifyDataSetChanged();
                                            new DownloadCollectCountTask(mContext).execute();
                                        }
                                        Utils.showToast(mContext,messageBean.getMsg(), Toast.LENGTH_SHORT);
                                    }
                                }, mContext.errorListener()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return mCollectList ==null?1: mCollectList.size()+1;
    }

    @Override
    public int getItemViewType(int position) {
        if(position==getItemCount()-1){
            return I.TYPE_FOOTER;
        }else{
            return I.TYPE_ITEM;
        }
    }

    public void initItems(ArrayList<CollectBean> list) {
        if(mCollectList !=null && !mCollectList.isEmpty()){
            mCollectList.clear();
        }
        mCollectList.addAll(list);
        notifyDataSetChanged();
    }

    public void addItems(ArrayList<CollectBean> list) {
        mCollectList.addAll(list);
        notifyDataSetChanged();
    }

    class CollectItemViewHolder extends ViewHolder{
        LinearLayout layoutItem;
        NetworkImageView nivThumb;
        TextView tvCollectName;
        ImageView ivDel;

        public CollectItemViewHolder(View itemView) {
            super(itemView);
            layoutItem = (LinearLayout) itemView.findViewById(R.id.layout_collect);
            nivThumb = (NetworkImageView) itemView.findViewById(R.id.niv_collect_thumb);
            tvCollectName = (TextView) itemView.findViewById(R.id.tv_collect_name);
            ivDel = (ImageView) itemView.findViewById(R.id.iv_del_collect);
        }
    }
}
