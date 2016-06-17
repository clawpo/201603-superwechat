package cn.ucai.fulicenter.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.activity.CategoryChildActivity;
import cn.ucai.fulicenter.bean.CategoryChildBean;
import cn.ucai.fulicenter.bean.CategoryGroupBean;
import cn.ucai.fulicenter.utils.ImageUtils;

/**
 * Created by clawpo on 16/6/17.
 */
public class CategoryAdapter extends BaseExpandableListAdapter {
    Context mContext;
    ArrayList<CategoryGroupBean> mGroupList;
    ArrayList<ArrayList<CategoryChildBean>> mChildredList;

    public CategoryAdapter(Context mContext, ArrayList<CategoryGroupBean> mGroupList,
                           ArrayList<ArrayList<CategoryChildBean>> mChildredList) {
        this.mContext = mContext;
        this.mGroupList = mGroupList;
        this.mChildredList = mChildredList;
    }

    @Override
    public int getGroupCount() {
        return mGroupList==null?0:mGroupList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mChildredList==null||mChildredList.get(groupPosition)==null?0:mChildredList.get(groupPosition).size();
    }

    @Override
    public CategoryGroupBean getGroup(int groupPosition) {
        return mGroupList.get(groupPosition);
    }

    @Override
    public CategoryChildBean getChild(int groupPosition, int childPosition) {
        return mChildredList.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View layout, ViewGroup parent) {
        ViewGroupHolder holder=null;
        if(layout==null){
            layout=View.inflate(mContext, R.layout.item_category_group, null);
            holder=new ViewGroupHolder();
            holder.ivIndicator=(ImageView) layout.findViewById(R.id.ivIndicator);
            holder.ivThumb=(NetworkImageView) layout.findViewById(R.id.ivGroupThumb);
            holder.tvGroupName=(TextView) layout.findViewById(R.id.tvGroupName);
            layout.setTag(holder);
        }else{
            holder=(ViewGroupHolder) layout.getTag();
        }
        CategoryGroupBean group = getGroup(groupPosition);
        holder.tvGroupName.setText(group.getName());
        String imgUrl=group.getImageUrl();
        String url= I.DOWNLOAD_DOWNLOAD_CATEGORY_GROUP_IMAGE_URL+imgUrl;
        ImageUtils.setThumb(url,holder.ivThumb);
        if(isExpanded){
            holder.ivIndicator.setImageResource(R.drawable.expand_off);
        }else{
            holder.ivIndicator.setImageResource(R.drawable.expand_on);
        }
        return layout;
    }

    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View layout, ViewGroup parent) {
        ViewChildHolder holder=null;
        if(layout==null){
            layout=View.inflate(mContext, R.layout.item_cateogry_child, null);
            holder=new ViewChildHolder();
            holder.layoutItem=(RelativeLayout) layout.findViewById(R.id.layout_category_child);
            holder.ivThumb=(NetworkImageView) layout.findViewById(R.id.ivCategoryChildThumb);
            holder.tvChildName=(TextView) layout.findViewById(R.id.tvCategoryChildName);
            layout.setTag(holder);
        }else{
            holder=(ViewChildHolder) layout.getTag();
        }
        final CategoryChildBean child = getChild(groupPosition, childPosition);
        String name=child.getName();
        holder.tvChildName.setText(name);

        String imgUrl=child.getImageUrl();
        String url=I.DOWNLOAD_DOWNLOAD_CATEGORY_CHILD_IMAGE_URL+imgUrl;
        ImageUtils.setThumb(url,holder.ivThumb);
        holder.layoutItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(new Intent(mContext, CategoryChildActivity.class)
                .putExtra(I.CategoryChild.CAT_ID,child.getId()));
            }
        });
        return layout;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
    class ViewGroupHolder{
        ImageView ivIndicator;
        NetworkImageView ivThumb;
        TextView tvGroupName;
    }
    class ViewChildHolder{
        RelativeLayout layoutItem;
        NetworkImageView ivThumb;
        TextView tvChildName;
    }

    public void addItems(ArrayList<CategoryGroupBean> groupList,
                         ArrayList<ArrayList<CategoryChildBean>> childList){
        this.mGroupList.addAll(groupList);
        this.mChildredList.addAll(childList);
        notifyDataSetChanged();
    }
}
