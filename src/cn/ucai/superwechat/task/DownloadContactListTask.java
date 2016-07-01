package cn.ucai.superwechat.task;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.activity.BaseActivity;
import cn.ucai.superwechat.bean.Result;
import cn.ucai.superwechat.bean.UserAvatar;
import cn.ucai.superwechat.data.ApiParams;
import cn.ucai.superwechat.data.OkHttpUtils2;
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
//        executeRequest(new GsonRequest<Result>(path,Result.class,
//                responseDownloadContactListListener(),errorListener()));
//        executeRequest(new StringRequest(path,responseDownloadContactListListener(),errorListener()));
        final OkHttpUtils2<String> utils = new OkHttpUtils2<>();
        utils.url(SuperWeChatApplication.SERVER_ROOT)
                .addParam(I.Contact.USER_NAME,username)
                .execute(new OkHttpUtils2.OnCompleteListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        try {
                            Result result = (Result) Utils.getListResultFromJson(s, UserAvatar.class);
                            Log.e(TAG,"result="+result);
                            if(result!=null && result.isRetMsg()){
                                List<UserAvatar> list = (List<UserAvatar>) result.getRetData();
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
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG,"onError,error="+error);
                    }
                });
    }

    private Response.Listener<String> responseDownloadContactListListener() {
        return new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    Result result = (Result) Utils.getListResultFromJson(s, UserAvatar.class);
                    Log.e(TAG,"result="+result);
                    if(result!=null && result.isRetMsg()){
                        List<UserAvatar> list = (List<UserAvatar>) result.getRetData();
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
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

//    private Response.Listener<Result> responseDownloadContactListListener() {
//        return new Response.Listener<Result>() {
//            @Override
//            public void onResponse(Result result) {
//                Log.e(TAG,"DownloadContactList");
//                if(result.isRetMsg()){
//                    ArrayList<UserAvatar> list = Utils.array2List(new Gson().fromJson(result.getRetData().toString(),UserAvatar[].class));
//                    if(list!=null) {
//                        Log.e(TAG, "DownloadContactList,contacts size=" + list.size());
//                        ArrayList<UserAvatar> contactList =
//                                SuperWeChatApplication.getInstance().getContactList();
//                        contactList.clear();
//                        contactList.addAll(list);
//                        HashMap<String, UserAvatar> userList =
//                                SuperWeChatApplication.getInstance().getUserList();
//                        userList.clear();
//                        for (UserAvatar c : list) {
//                            userList.put(c.getMUserName(), c);
//                        }
//                        mContext.sendStickyBroadcast(new Intent("update_contact_list"));
//                    }
//                }
//            }
//        };
//    }
}