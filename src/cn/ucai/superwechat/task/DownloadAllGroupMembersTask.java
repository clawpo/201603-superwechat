package cn.ucai.superwechat.task;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.activity.BaseActivity;
import cn.ucai.superwechat.bean.MemberUserAvatar;
import cn.ucai.superwechat.bean.Result;
import cn.ucai.superwechat.data.OkHttpUtils2;
import cn.ucai.superwechat.utils.Utils;

/**
 * Created by sks on 2016/4/5.
 */
public class DownloadAllGroupMembersTask extends BaseActivity {
    public static final String TAG = DownloadAllGroupMembersTask.class.getName();
    Context mContext;
    String groupId;
    String path;

    public DownloadAllGroupMembersTask(Context context, String groupId) {
        this.mContext = context;
        this.groupId = groupId;
//        initPath();

    }

//    private void initPath(){
//        try {
//            path = new ApiParams()
//                    .with(I.Member.GROUP_HX_ID, groupId)
//                    .setRequestUrl(I.REQUEST_DOWNLOAD_GROUP_MEMBERS_BY_HXID);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public void execute(){
//        executeRequest(new GsonRequest<Result>(path,Result.class,
//                responseDownloadGroupMembersListener(), errorListener()));
//        executeRequest(new StringRequest(path,responseDownloadGroupMembersListener(),errorListener()));
        OkHttpUtils2<String> utils = new OkHttpUtils2<>();
        utils.setRequestUrl(I.REQUEST_DOWNLOAD_GROUP_MEMBERS_BY_HXID)
                .addParam(I.Member.GROUP_HX_ID, groupId)
                .targetClass(String.class)
                .execute(new OkHttpUtils2.OnCompleteListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        try {
                            Result result = (Result) Utils.getListResultFromJson(s, MemberUserAvatar.class);
                            Log.e(TAG,"result="+result);
                            if(result!=null && result.isRetMsg()){
                                ArrayList<MemberUserAvatar> list = (ArrayList<MemberUserAvatar>) result.getRetData();
                                Log.e(TAG, "responseDownloadGroupMembersListener,userList=" + list);
                                if (list == null) {
                                    return;
                                }
                                Log.e(TAG, "responseDownloadGroupMembersListener,userList.length=" + list.size());
                                HashMap<String, ArrayList<MemberUserAvatar>> groupMembers =
                                        SuperWeChatApplication.getInstance().getGroupMembers();
                                groupMembers.put(groupId, list);
                                Intent intent = new Intent("update_group_member");
                                mContext.sendStickyBroadcast(intent);
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

//    private Response.Listener<String> responseDownloadGroupMembersListener() {
//        return new Response.Listener<String>() {
//            @Override
//            public void onResponse(String s) {
//                try {
//                    Result result = (Result) Utils.getListResultFromJson(s, MemberUserAvatar.class);
//                    Log.e(TAG,"result="+result);
//                    if(result!=null && result.isRetMsg()){
//                        ArrayList<MemberUserAvatar> list = (ArrayList<MemberUserAvatar>) result.getRetData();
//                        Log.e(TAG, "responseDownloadGroupMembersListener,userList=" + list);
//                        if (list == null) {
//                            return;
//                        }
//                        Log.e(TAG, "responseDownloadGroupMembersListener,userList.length=" + list.size());
//                        HashMap<String, ArrayList<MemberUserAvatar>> groupMembers =
//                                SuperWeChatApplication.getInstance().getGroupMembers();
//                        groupMembers.put(groupId, list);
//                        Intent intent = new Intent("update_group_member");
//                        mContext.sendStickyBroadcast(intent);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        };
//    }

//    private Response.Listener<Result> responseDownloadGroupMembersListener() {
//        return new Response.Listener<Result>(){
//            @Override
//            public void onResponse(Result result) {
//                Log.e(TAG, "responseDownloadGroupMembersListener");
//                if (result.isRetMsg()) {
//                    ArrayList<MemberUserAvatar> list = Utils.array2List(new Gson().fromJson(result.getRetData().toString(),MemberUserAvatar[].class));
//                    Log.e(TAG, "responseDownloadGroupMembersListener,userList=" + list);
//                    if (list == null) {
//                        return;
//                    }
//                    Log.e(TAG, "responseDownloadGroupMembersListener,userList.length=" + list.size());
//                    HashMap<String, ArrayList<MemberUserAvatar>> groupMembers =
//                            SuperWeChatApplication.getInstance().getGroupMembers();
//                    groupMembers.put(groupId, list);
//                    Intent intent = new Intent("update_group_member");
//                    mContext.sendStickyBroadcast(intent);
//                }
//            }
//        };
//    }
}
