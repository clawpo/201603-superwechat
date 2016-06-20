package cn.ucai.fulicenter.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;
import java.util.HashMap;

import cn.ucai.fulicenter.FuLiCenterApplication;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.activity.CollectActivity;
import cn.ucai.fulicenter.activity.SettingsActivity;
import cn.ucai.fulicenter.bean.User;
import cn.ucai.fulicenter.task.DownloadCollectCountTask;
import cn.ucai.fulicenter.utils.UserUtils;

public class PersonalCenterFragment extends Fragment {
    public static final String TAG = PersonalCenterFragment.class.getName();
    Context mContext;

    NetworkImageView mivUserAvarar;
    TextView mtvUserName;
    TextView mtvCollectCount;
    TextView mtvSettings;
    ImageView mivMessage;
    LinearLayout mLayoutCenterCollet;
    RelativeLayout mLyaoutCenterUserInfo;

    int mCollectCount = 0;
    User mUser;

    MyClickListener listener;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getActivity();
        View layout = View.inflate(mContext, R.layout.fragment_personal_center,null);
        initView(layout);
        initData();
        setListener();
        return layout;
    }

    private void setListener() {
        listener = new MyClickListener();
        registerUpdateUserChangedReceiver();
        registerCollectCountReceiver();
        mtvSettings.setOnClickListener(listener);
        mLyaoutCenterUserInfo.setOnClickListener(listener);
        mLayoutCenterCollet.setOnClickListener(listener);
    }

    class MyClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.tv_center_settings:
                case R.id.center_user_info:
                    startActivity(new Intent(mContext, SettingsActivity.class));
                    break;
                case R.id.layout_center_collect:
                    startActivity(new Intent(mContext, CollectActivity.class));
                    break;
            }
        }
    }

    private void initData() {
        mUser = FuLiCenterApplication.getInstance().getUser();
        Log.e(TAG,"initData,mUser="+mUser);
        mtvCollectCount.setText(""+mCollectCount);
        if(mUser!=null) {
            UserUtils.setCurrentUserAvatar(mivUserAvarar);
            UserUtils.setCurrentUserBeanNick(mtvUserName);
        }
    }
    
    private void initView(View layout) {
        mivUserAvarar = (NetworkImageView) layout.findViewById(R.id.iv_user_avatar);
        mtvUserName = (TextView) layout.findViewById(R.id.tv_user_name);
        mLayoutCenterCollet = (LinearLayout) layout.findViewById(R.id.layout_center_collect);
        mtvCollectCount = (TextView) layout.findViewById(R.id.tv_collect_count);
        mtvSettings = (TextView) layout.findViewById(R.id.tv_center_settings);
        mivMessage = (ImageView) layout.findViewById(R.id.iv_persona_center_msg);
        mLyaoutCenterUserInfo = (RelativeLayout) layout.findViewById(R.id.center_user_info);

        initOrderList(layout);
    }

    private void initOrderList(View layout) {
        // 显示GridView的界面
        GridView mOrderList = (GridView)layout.findViewById(R.id.center_user_order_lis);
        ArrayList<HashMap<String,Object>> imagelist = new ArrayList<HashMap<String,Object>>();

        // 使用HashMap将图片添加到一个数组中，注意一定要是HashMap<String,Object>类型的，因为装到map中的图片要是资源ID，而不是图片本身
        // 如果是用findViewById(R.drawable.image)这样把真正的图片取出来了，放到map中是无法正常显示的
        HashMap<String,Object> map1 = new HashMap<String,Object>();
        map1.put("image", R.drawable.order_list1);
        imagelist.add(map1);
        HashMap<String,Object> map2 = new HashMap<String,Object>();
        map2.put("image", R.drawable.order_list2);
        imagelist.add(map2);
        HashMap<String,Object> map3 = new HashMap<String,Object>();
        map3.put("image", R.drawable.order_list3);
        imagelist.add(map3);
        HashMap<String,Object> map4 = new HashMap<String,Object>();
        map4.put("image", R.drawable.order_list4);
        imagelist.add(map4);
        HashMap<String,Object> map5 = new HashMap<String,Object>();
        map5.put("image", R.drawable.order_list5);
        imagelist.add(map5);

        // 使用simpleAdapter封装数据，将图片显示出来
        // 参数一是当前上下文Context对象
        // 参数二是图片数据列表，要显示数据都在其中
        // 参数三是界面的XML文件，注意，不是整体界面，而是要显示在GridView中的单个Item的界面XML
        // 参数四是动态数组中与map中图片对应的项，也就是map中存储进去的相对应于图片value的key
        // 参数五是单个Item界面XML中的图片ID
        SimpleAdapter simpleAdapter = new SimpleAdapter(mContext, imagelist, R.layout.simple_grid_item, new String[] {"image"}, new int[]{R.id.image});

        // 设置GridView的适配器为新建的simpleAdapter
        mOrderList.setAdapter(simpleAdapter);
    }

    class CollectCountChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            mCollectCount = FuLiCenterApplication.getInstance().getCollectCount();
            Log.e(TAG,"CollectCountChangedReceiver,mCollectCount="+mCollectCount);
            initData();
        }
    }

    CollectCountChangedReceiver mReceiver;
    private void registerCollectCountReceiver(){
        mReceiver = new CollectCountChangedReceiver();
        IntentFilter filter = new IntentFilter("update_collect_count");
        mContext.registerReceiver(mReceiver,filter);
    }
    class UpdateUserChangerReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG,"UpdateUserChangerReceiver,user="+FuLiCenterApplication.getInstance().getUser());
            new DownloadCollectCountTask(mContext).execute();
            initData();
        }
    }
    UpdateUserChangerReceiver mUpdateUserReceiver;
    private void registerUpdateUserChangedReceiver(){
        mUpdateUserReceiver = new UpdateUserChangerReceiver();
        IntentFilter filter = new IntentFilter("update_user");
        mContext.registerReceiver(mUpdateUserReceiver,filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mReceiver!=null){
            mContext.unregisterReceiver(mReceiver);
        }
        if(mUpdateUserReceiver!=null){
            mContext.unregisterReceiver(mUpdateUserReceiver);
        }
    }
}
