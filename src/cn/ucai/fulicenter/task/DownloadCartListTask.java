package cn.ucai.fulicenter.task;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Response;

import java.util.ArrayList;

import cn.ucai.fulicenter.FuLiCenterApplication;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.activity.BaseActivity;
import cn.ucai.fulicenter.bean.CartBean;
import cn.ucai.fulicenter.bean.GoodDetailsBean;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
import cn.ucai.fulicenter.utils.Utils;

public class DownloadCartListTask extends BaseActivity {
    private static final String TAG = DownloadCartListTask.class.getName();
    Context mContext;
    String username;
    String path;

    public DownloadCartListTask(Context mContext) {
        this.mContext = mContext;
        this.username = FuLiCenterApplication.getInstance().getUserName();
        initPath();
    }

    private void initPath(){
        try {
            path = new ApiParams()
                    .with(I.Cart.USER_NAME,username)
                    .with(I.PAGE_ID, I.PAGE_ID_DEFAULT + "")
                    .with(I.PAGE_SIZE, I.PAGE_SIZE_DEFAULT + "")
                    .getRequestUrl(I.REQUEST_FIND_CARTS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute(){
        executeRequest(new GsonRequest<CartBean[]>(path,CartBean[].class,
                responseDownloadCartListListener(),errorListener()));
    }
    ArrayList<CartBean> list;
    int listSize;
    private Response.Listener<CartBean[]> responseDownloadCartListListener() {
        return new Response.Listener<CartBean[]>() {
            @Override
            public void onResponse(CartBean[] cartBeen) {
                Log.e(TAG,"DownloadCartList");
                if(cartBeen!=null){
                    Log.e(TAG,"DownloadCartList,CartBeans size="+cartBeen.length);
                    list = Utils.array2List(cartBeen);
                    ArrayList<CartBean> cartList =
                            FuLiCenterApplication.getInstance().getCartList();
                    try {
                        for (int i = 0; i < list.size(); i++) {
                            CartBean cart = list.get(i);
                            if (!cartList.contains(cart)) {
                                path = new ApiParams().with(I.CategoryGood.GOODS_ID, cart.getGoodsId() + "")
                                        .getRequestUrl(I.REQUEST_FIND_GOOD_DETAILS);
                                Log.e(TAG, "path=" + path);
                                executeRequest(new GsonRequest<GoodDetailsBean>(path, GoodDetailsBean.class,
                                        responseDownloadGoodDetailListener(cart), errorListener()));
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    private Response.Listener<GoodDetailsBean> responseDownloadGoodDetailListener(final CartBean cart) {
        return new Response.Listener<GoodDetailsBean>() {
            @Override
            public void onResponse(GoodDetailsBean goodDetailsBean) {
                listSize++;
                if(goodDetailsBean!=null){
                    cart.setGoods(goodDetailsBean);
                    FuLiCenterApplication.getInstance().getCartList().add(cart);
                }
                if(listSize==list.size()) {
                    Log.e(TAG,"send broadcast,list size="+FuLiCenterApplication.getInstance().getCartList().size());
                    mContext.sendStickyBroadcast(new Intent("update_cart_list"));
                }
            }
        };
    }
}
