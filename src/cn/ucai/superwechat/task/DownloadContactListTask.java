package cn.ucai.superwechat.task;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Response;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.activity.BaseActivity;
import cn.ucai.superwechat.bean.Result;
import cn.ucai.superwechat.bean.UserAvatar;
import cn.ucai.superwechat.data.ApiParams;
import cn.ucai.superwechat.data.GsonRequest;
import cn.ucai.superwechat.utils.Utils;

public class DownloadContactListTask extends BaseActivity {
    private static final String TAG = DownloadContactListTask.class.getName();
    Context mContext;
    String username;
    String path;

    public DownloadContactListTask(Context mContext, String username) {
        this.mContext = mContext;
        this.username = username;
        initPath();
    }

    private void initPath(){
        try {
            path = new ApiParams()
                    .with(I.Contact.USER_NAME,username)
                    .getRequestUrl(I.REQUEST_DOWNLOAD_CONTACT_ALL_LIST);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute(){
        executeRequest(new GsonRequest<Result>(path,Result.class,
                responseDownloadContactListListener(),errorListener()));
    }

    private Response.Listener<Result> responseDownloadContactListListener() {
        return new Response.Listener<Result>() {
            @Override
            public void onResponse(Result result) {
                Log.e(TAG,"DownloadContactList");
                if(result.isRetMsg()){
                    ArrayList<UserAvatar> list = Utils.array2List(new Gson().fromJson(result.getRetData().toString(),UserAvatar[].class));
                    if(list!=null) {
                        Log.e(TAG, "DownloadContactList,contacts size=" + list.size());
                        ArrayList<UserAvatar> contactList =
                                SuperWeChatApplication.getInstance().getContactList();
                        contactList.clear();
                        contactList.addAll(list);
                        HashMap<String, UserAvatar> userList =
                                SuperWeChatApplication.getInstance().getUserList();
                        userList.clear();
                        for (UserAvatar c : list) {
                            userList.put(c.getMUserName(), c);
                        }
                        mContext.sendStickyBroadcast(new Intent("update_contact_list"));
                    }
                }
            }
        };
    }
}
