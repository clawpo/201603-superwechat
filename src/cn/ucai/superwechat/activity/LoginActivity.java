/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.ucai.superwechat.activity;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroupManager;
import com.google.gson.Gson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.ucai.superwechat.Constant;
import cn.ucai.superwechat.DemoHXSDKHelper;
import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.applib.controller.HXSDKHelper;
import cn.ucai.superwechat.bean.Result;
import cn.ucai.superwechat.bean.UserAvatar;
import cn.ucai.superwechat.bean.UserBean;
import cn.ucai.superwechat.data.OkHttpUtils2;
import cn.ucai.superwechat.db.EMUserDao;
import cn.ucai.superwechat.db.UserDao;
import cn.ucai.superwechat.domain.EMUser;
import cn.ucai.superwechat.listener.OnSetAvatarListener;
import cn.ucai.superwechat.task.DownloadAllGroupTask;
import cn.ucai.superwechat.task.DownloadContactListTask;
import cn.ucai.superwechat.task.DownloadPublicGroupTask;
import cn.ucai.superwechat.utils.CommonUtils;
import cn.ucai.superwechat.utils.MD5;
import cn.ucai.superwechat.utils.Utils;

/**
 * 登陆页面
 * 
 */
public class LoginActivity extends BaseActivity {
	private static final String TAG = "LoginActivity";
	Activity mContext;
	public static final int REQUEST_CODE_SETNICK = 1;
	private EditText usernameEditText;
	private EditText passwordEditText;

	private boolean progressShow;
    ProgressDialog pd;
	private boolean autoLogin = false;

	private String currentUsername;
	private String currentPassword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 如果用户名密码都有，直接进入主页面
		if (DemoHXSDKHelper.getInstance().isLogined()) {
			autoLogin = true;
			startActivity(new Intent(LoginActivity.this, MainActivity.class));

			return;
		}
		setContentView(R.layout.activity_login);
		mContext = this;

		usernameEditText = (EditText) findViewById(R.id.username);
		passwordEditText = (EditText) findViewById(R.id.password);

		setListener();

		if (SuperWeChatApplication.getInstance().getUserName() != null) {
			usernameEditText.setText(SuperWeChatApplication.getInstance().getUserName());
		}
	}

    private void setListener() {
        setLoginClickListener();
        setUserNameTextChangedListener();
        setRegisterClickListener();
        setServerUrlClickListener();
	}

    String serverUrl;
    private void setServerUrlClickListener() {
        findViewById(R.id.btnUrl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SharedPreferences sp = getSharedPreferences("server_url",MODE_PRIVATE);
                serverUrl = sp.getString("url","");
                View layout = View.inflate(mContext,R.layout.dialog_serverurl,null);
                final EditText etServerUrl = (EditText) layout.findViewById(R.id.et_server_url);
                final String url = etServerUrl.getText().toString();
                if(serverUrl!=null){
                    etServerUrl.setText(serverUrl);
                }
                Builder builder = new Builder(mContext);
                builder.setTitle("设置服务器IP地址")
                        .setView(layout)
                        .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                serverUrl = etServerUrl.getText().toString();
                                if(serverUrl.isEmpty()){
                                    return;
                                }
                                sp.edit().putString("url",serverUrl).commit();
                                SuperWeChatApplication.SERVER_ROOT = serverUrl;
                                Utils.showToast(mContext,"设置服务器IP地址成功",Toast.LENGTH_SHORT);
                            }
                        })
                        .setNegativeButton("取消",null);
                builder.create().show();
            }
        });
    }

    private void setUserNameTextChangedListener() {
        // 如果用户名改变，清空密码
        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                passwordEditText.setText(null);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setProgressShow() {
        progressShow = true;
        pd = new ProgressDialog(LoginActivity.this);
        pd.setCanceledOnTouchOutside(false);
        pd.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                progressShow = false;
            }
        });
        pd.setMessage(getString(R.string.Is_landing));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pd.show();
            }
        });
    }

    /**
	 * 登录
	 *
	 */
	public void setLoginClickListener() {
        findViewById(R.id.btnLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!CommonUtils.isNetWorkConnected(mContext)) {
                    Toast.makeText(mContext, R.string.network_isnot_available, Toast.LENGTH_SHORT).show();
                    return;
                }
                currentUsername = usernameEditText.getText().toString().trim();
                currentPassword = passwordEditText.getText().toString().trim();

                if (TextUtils.isEmpty(currentUsername)) {
                    Toast.makeText(mContext, R.string.User_name_cannot_be_empty, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(currentPassword)) {
                    Toast.makeText(mContext, R.string.Password_cannot_be_empty, Toast.LENGTH_SHORT).show();
                    return;
                }

                setProgressShow();

                final long start = System.currentTimeMillis();
                // 调用sdk登陆方法登陆聊天服务器
                EMChatManager.getInstance().login(currentUsername, currentPassword, new EMCallBack() {

                    @Override
                    public void onSuccess() {
                        if (!progressShow) {
                            return;
                        }
                        loginAppServer();
                    }

                    @Override
                    public void onProgress(int progress, String status) {
                    }

                    @Override
                    public void onError(final int code, final String message) {
                        if (!progressShow) {
                            return;
                        }
                        runOnUiThread(new Runnable() {
                            public void run() {
                                pd.dismiss();
                                Toast.makeText(getApplicationContext(), getString(R.string.Login_failed) + message,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });

	}

    private void loginAppServer() {
        UserDao dao = new UserDao(mContext);
        UserBean user = dao.findUserByUserName(currentUsername);
        if(user!=null) {
            if(user.getPassword().equals(MD5.getData(currentPassword))){
                saveUser(user);
                loginSuccess();
            } else {
                pd.dismiss();
                Toast.makeText(getApplicationContext(), getString(R.string.Login_failed),
                        Toast.LENGTH_SHORT).show();
            }
        }else{
            //volley login server
            try {
                Log.e(TAG,"okhttp login");
                OkHttpUtils2<Result> utils=new OkHttpUtils2<>();
                utils.url(SuperWeChatApplication.SERVER_ROOT)
                        .addParam(I.KEY_REQUEST,I.REQUEST_LOGIN)
                        .addParam(I.User.USER_NAME,currentUsername)
                        .addParam(I.User.PASSWORD,currentPassword)
                        .targetClass(Result.class)
                        .execute(new OkHttpUtils2.OnCompleteListener<Result>() {
                            @Override
                            public void onSuccess(Result result) {
                                if(result.isRetMsg()){
                                    Log.e(TAG,"resule.getRetData()="+result.getRetData());
                                    Gson mGson = new Gson();
                                    UserAvatar userBean = mGson.fromJson(result.getRetData().toString(),UserAvatar.class);
                                    UserBean u = new UserBean(userBean.getMUserName(),MD5.getData(currentPassword),userBean.getMUserNick());
                                    saveUser(u);
                                    UserDao dao = new UserDao(mContext);
                                    dao.addUser(u);
                                    loginSuccess();
                                }else{
                                    pd.dismiss();
                                    Utils.showToast(mContext,Utils.getResourceString(mContext,result.getRetCode()),Toast.LENGTH_LONG);
                                }
                            }

                            @Override
                            public void onError(String error) {
                                pd.dismiss();
                                Utils.showToast(mContext,getString(R.string.Login_failed) + error,Toast.LENGTH_LONG);
                            }
                        });
//                String path = new ApiParams()
//                        .with(I.User.USER_NAME,currentUsername)
//                        .with(I.User.PASSWORD,currentPassword)
//                        .getRequestUrl(I.REQUEST_LOGIN);
//                Log.e(TAG,"path = "+ path);
//                executeRequest(new GsonRequest<Result>(path, Result.class,
//                        responseListener(), errorListener()));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

//    private Response.Listener<Result> responseListener() {
//        return new Response.Listener<Result>() {
//            @Override
//            public void onResponse(Result result) {
//                Log.e(TAG,"resule="+result);
//				if(result.isRetMsg()){
//                    Log.e(TAG,"resule.getRetData()="+result.getRetData());
//                    Gson mGson = new Gson();
//                    UserAvatar userBean = mGson.fromJson(result.getRetData().toString(),UserAvatar.class);
//                    UserBean u = new UserBean(userBean.getMUserName(),MD5.getData(currentPassword),userBean.getMUserNick());
//					saveUser(u);
//					UserDao dao = new UserDao(mContext);
//					dao.addUser(u);
//					loginSuccess();
//				}else{
//					pd.dismiss();
//					Utils.showToast(mContext,Utils.getResourceString(mContext,result.getRetCode()),Toast.LENGTH_LONG);
//				}
//            }
//        };
//    }

    /**保存当前登录的用户到全局变量*/
    private void saveUser(UserBean user) {
        SuperWeChatApplication instance = SuperWeChatApplication.getInstance();
        instance.setUser(user);
        // 登陆成功，保存用户名密码
        instance.setUserName(currentUsername);
        instance.setPassword(currentPassword);
        SuperWeChatApplication.currentUserNick = user.getNick();
    }

    private void loginSuccess() {
        try {
            // ** 第一次登录或者之前logout后再登录，加载所有本地群和回话
            // ** manually load all local groups and
            EMGroupManager.getInstance().loadAllGroups();
            EMChatManager.getInstance().loadAllConversations();
            //下载用户头像
            final OkHttpUtils2<Message> utils = new OkHttpUtils2<Message>();
            utils.url(SuperWeChatApplication.SERVER_ROOT)//设置服务端根地址
                    .addParam(I.KEY_REQUEST, I.REQUEST_DOWNLOAD_AVATAR)//添加上传的请求参数
                    .addParam(I.NAME_OR_HXID, currentUsername)//添加用户的账号
                    .addParam(I.AVATAR_TYPE, I.AVATAR_TYPE_USER_PATH)//添加用户的头像类型
            .doInBackground(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    Log.e(TAG,e.getMessage());
//                    Toast.makeText(mContext,e.getMessage(),Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResponse(com.squareup.okhttp.Response response) throws IOException {
                    String avatarPath = I.AVATAR_TYPE_USER_PATH + I.BACKSLASH
                            + currentUsername + I.AVATAR_SUFFIX_JPG;
                    File file = OnSetAvatarListener.getAvatarFile(mContext,avatarPath);
                    FileOutputStream out = null;
                    out = new FileOutputStream(file);
                    utils.downloadFile(response,file);
                }
            }).execute(new OkHttpUtils2.OnCompleteListener<Message>() {
                @Override
                public void onSuccess(Message msg) {
                    switch (msg.what) {
                        case OkHttpUtils2.DOWNLOADING_FINISH:
//                            Toast.makeText(mContext,"头像下载成功",Toast.LENGTH_SHORT).show();
                            //从sd卡的file所指向的路径下读取图片，结果是位图类型
//                            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
//                            mivAvatar.setImageBitmap(bitmap);
                            break;
                    }
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(mContext,error,Toast.LENGTH_SHORT).show();
                }
            });
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG,"start download contact,group,public group");
                    //下载联系人集合
                    new DownloadContactListTask(mContext,currentUsername).execute();
                    //下载群组集合
                    new DownloadAllGroupTask(mContext,currentUsername).execute();
                    //下载公开群组集合
                    new DownloadPublicGroupTask(mContext,currentUsername,
                            I.PAGE_ID_DEFAULT,I.PAGE_SIZE_DEFAULT).execute();
                }
            });

            // 处理好友和群组
            initializeContacts();
        } catch (Exception e) {
            e.printStackTrace();
            // 取好友或者群聊失败，不让进入主页面
            runOnUiThread(new Runnable() {
                public void run() {
                    pd.dismiss();
                    DemoHXSDKHelper.getInstance().logout(true,null);
                    Toast.makeText(getApplicationContext(), R.string.login_failure_failed, Toast.LENGTH_LONG).show();
                }
            });
            return;
        }
        // 更新当前用户的nickname 此方法的作用是在ios离线推送时能够显示用户nick
        boolean updatenick = EMChatManager.getInstance().updateCurrentUserNick(
                SuperWeChatApplication.currentUserNick.trim());
        if (!updatenick) {
            Log.e("LoginActivity", "update current user nick fail");
        }
        if (!LoginActivity.this.isFinishing() && pd.isShowing()) {
            pd.dismiss();
        }
        // 进入主页面
        Intent intent = new Intent(LoginActivity.this,
                MainActivity.class);
        startActivity(intent);

        finish();
    }

	private void initializeContacts() {
		Map<String, EMUser> userlist = new HashMap<String, EMUser>();
		// 添加user"申请与通知"
		EMUser newFriends = new EMUser();
		newFriends.setUsername(Constant.NEW_FRIENDS_USERNAME);
		String strChat = getResources().getString(
				R.string.Application_and_notify);
		newFriends.setNick(strChat);

		userlist.put(Constant.NEW_FRIENDS_USERNAME, newFriends);
		// 添加"群聊"
		EMUser groupUser = new EMUser();
		String strGroup = getResources().getString(R.string.group_chat);
		groupUser.setUsername(Constant.GROUP_USERNAME);
		groupUser.setNick(strGroup);
		groupUser.setHeader("");
		userlist.put(Constant.GROUP_USERNAME, groupUser);
		
//		// 添加"Robot"
//		EMUser robotUser = new EMUser();
//		String strRobot = getResources().getString(R.string.robot_chat);
//		robotUser.setUsername(Constant.CHAT_ROBOT);
//		robotUser.setNick(strRobot);
//		robotUser.setHeader("");
//		userlist.put(Constant.CHAT_ROBOT, robotUser);
		
		// 存入内存
		((DemoHXSDKHelper) HXSDKHelper.getInstance()).setContactList(userlist);
		// 存入db
		EMUserDao dao = new EMUserDao(LoginActivity.this);
		List<EMUser> users = new ArrayList<EMUser>(userlist.values());
		dao.saveContactList(users);
	}
	
	/**
	 * 注册
	 *
	 */
	public void setRegisterClickListener() {
        findViewById(R.id.btnRegister).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(mContext, RegisterActivity.class), 0);
            }
        });
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (autoLogin) {
			return;
		}
	}
}
