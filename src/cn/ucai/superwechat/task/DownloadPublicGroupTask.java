package cn.ucai.superwechat.task;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Response;

import java.util.ArrayList;
import java.util.List;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.activity.BaseActivity;
import cn.ucai.superwechat.bean.GroupAvatar;
import cn.ucai.superwechat.bean.Pager;
import cn.ucai.superwechat.bean.Result;
import cn.ucai.superwechat.data.ApiParams;
import cn.ucai.superwechat.data.GsonRequest;

public class DownloadPublicGroupTask extends BaseActivity {
    private static final String TAG = DownloadPublicGroupTask.class.getName();
    Context mContext;
    String username;
    int pageId;
    int pageSize;
    String path;

    public DownloadPublicGroupTask(Context mContext, String username, int pageId, int pageSize) {
        this.mContext = mContext;
        this.username = username;
        this.pageId = pageId;
        this.pageSize = pageSize;
        initPath();
    }

    private void initPath() {
        try {
            path = new ApiParams()
                    .with(I.User.USER_NAME,username)
                    .with(I.PAGE_ID,pageId+"")
                    .with(I.PAGE_SIZE,pageSize+"")
                    .getRequestUrl(I.REQUEST_FIND_PUBLIC_GROUPS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute(){
        executeRequest(new GsonRequest<Result>(path,Result.class,
                responseDownloadPublicGroupTaskListener(),errorListener()));
    }

    private Response.Listener<Result> responseDownloadPublicGroupTaskListener() {
        return new Response.Listener<Result>() {
            @Override
            public void onResponse(Result result) {
                Log.e(TAG,"DownloadPublicGroup");
                if(result.isRetMsg()) {
                    Pager pager = (Pager) result.getRetData();
                    if(pager!=null) {
                        List<GroupAvatar> list = (List<GroupAvatar>) pager.getPageData();
                        if (list != null) {
                            Log.e(TAG, "DownloadPublicGroup,groups size=" + list.size());
                            ArrayList<GroupAvatar> publicGroupList =
                                    SuperWeChatApplication.getInstance().getPublicGroupList();
                            for (GroupAvatar g : list) {
                                if (!publicGroupList.contains(g)) {
                                    publicGroupList.add(g);
                                }
                            }
                            mContext.sendStickyBroadcast(new Intent("update_public_group"));
                        }
                    }
                }
            }
        };
    }
}
