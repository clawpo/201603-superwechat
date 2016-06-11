package cn.ucai.superwechat.task;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Response;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.activity.BaseActivity;
import cn.ucai.superwechat.bean.GroupAvatar;
import cn.ucai.superwechat.bean.Result;
import cn.ucai.superwechat.data.ApiParams;
import cn.ucai.superwechat.data.GsonRequest;
import cn.ucai.superwechat.utils.Utils;

public class DownloadAllGroupTask extends BaseActivity {
    private static final String TAG = DownloadAllGroupTask.class.getName();
    Context mContext;
    String username;
    String path;

    public DownloadAllGroupTask(Context mContext, String username) {
        this.mContext = mContext;
        this.username = username;
        initPath();
    }

    private void initPath() {
        try {
            path = new ApiParams()
                    .with(I.User.USER_NAME,username)
                    .getRequestUrl(I.REQUEST_FIND_GROUP_BY_USER_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute(){
        executeRequest(new GsonRequest<Result>(path,Result.class,
                responseDownloadAllGroupTaskListener(),errorListener()));
    }

    private Response.Listener<Result> responseDownloadAllGroupTaskListener() {
        return new Response.Listener<Result>() {
            @Override
            public void onResponse(Result result) {
                Log.e(TAG,"DownloadAllGroup");
                if(result.isRetMsg()) {
                    List<GroupAvatar> list = Utils.array2List(new Gson().fromJson(result.getRetData().toString(), GroupAvatar[].class));
                    if (list != null) {
                        Log.e(TAG, "DownloadAllGroup,groups size=" + list.size());
                        ArrayList<GroupAvatar> groupList =
                                SuperWeChatApplication.getInstance().getGroupList();
                        groupList.clear();
                        groupList.addAll(list);
                        mContext.sendStickyBroadcast(new Intent("update_group_list"));
                    }
                }
            }
        };
    }
}
