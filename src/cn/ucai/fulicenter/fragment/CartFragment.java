package cn.ucai.fulicenter.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Response;

import java.util.ArrayList;

import cn.ucai.fulicenter.FuLiCenterApplication;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.activity.FuliCenterMainActivity;
import cn.ucai.fulicenter.adapter.CartAdapter;
import cn.ucai.fulicenter.bean.CartBean;
import cn.ucai.fulicenter.bean.GoodDetailsBean;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
import cn.ucai.fulicenter.utils.Utils;

/**
 * Created by clawpo on 16/6/16.
 */
public class CartFragment extends Fragment {
    public static final String TAG = CartFragment.class.getName();

    FuliCenterMainActivity mContext;
    ArrayList<CartBean> mCartList;
    CartAdapter mAdapter;
    private int action = I.ACTION_DOWNLOAD;
    int pageId=0;
    String path;
    String username;

    /** 下拉刷新控件*/
    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView mRecyclerView;
    TextView mtvHint;
    LinearLayoutManager mLinearLayoutManager;
    TextView mtvNothing;
    TextView mtvSumPrice;
    TextView mtvSavePrice;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = (FuliCenterMainActivity) getActivity();
        View layout = inflater.inflate(R.layout.fragment_cart,container,false);
        mCartList = new ArrayList<CartBean>();
        initView(layout);
        setListener();
        initData();
        return layout;
    }

    private void setListener() {
        setPullDownRefreshListener();
        setPullUpRefreshListener();
        registerCartChangedReceiver();
    }

    /**
     * 上拉刷新事件监听
     */
    private void setPullUpRefreshListener() {
        mRecyclerView.setOnScrollListener(
                new RecyclerView.OnScrollListener() {
                    int lastItemPosition;
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                        if(newState == RecyclerView.SCROLL_STATE_IDLE &&
                                lastItemPosition == mAdapter.getItemCount()-1){
                            if(mAdapter.isMore()){
                                mSwipeRefreshLayout.setRefreshing(true);
                                action = I.ACTION_PULL_UP;
                                pageId+=I.PAGE_SIZE_DEFAULT;
                                getPath();
                                mContext.executeRequest(new GsonRequest<CartBean[]>(path,
                                        CartBean[].class, responseDownloadCartListener(),
                                        mContext.errorListener()));
                            }
                        }
                    }

                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        //获取最后列表项的下标
                        lastItemPosition = mLinearLayoutManager.findLastVisibleItemPosition();
                        //解决RecyclerView和SwipeRefreshLayout共用存在的bug
                        mSwipeRefreshLayout.setEnabled(mLinearLayoutManager
                                .findFirstCompletelyVisibleItemPosition() == 0);
                    }
                }
        );
    }

    /**
     * 下拉刷新事件监听
     */
    private void setPullDownRefreshListener() {
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener(){
                    @Override
                    public void onRefresh() {
                        mtvHint.setVisibility(View.VISIBLE);
                        action = I.ACTION_PULL_DOWN;
                        getPath();
                        mContext.executeRequest(new GsonRequest<CartBean[]>(path,
                                CartBean[].class, responseDownloadCartListener(),
                                mContext.errorListener()));
                    }
                }
        );
    }

    private void initData() {
        ArrayList<CartBean> list = FuLiCenterApplication.getInstance().getCartList();
        mCartList.clear();
        mCartList.addAll(list);
        Log.e(TAG,"refresh,mCartList="+mCartList.size()+",getCartList="+list.size());
        mAdapter.notifyDataSetChanged();
        sumPrice();
        if(mCartList!=null&&mCartList.size()>0) {
            mtvNothing.setVisibility(View.GONE);
        }else{
            mtvNothing.setVisibility(View.VISIBLE);
        }
    }
    private String getPath(){
        try {
            username = FuLiCenterApplication.getInstance().getUserName();
            path = new ApiParams()
                    .with(I.Cart.USER_NAME,username)
                    .with(I.PAGE_ID, pageId + "")
                    .with(I.PAGE_SIZE, I.PAGE_SIZE_DEFAULT + "")
                    .getRequestUrl(I.REQUEST_FIND_CARTS);
            Log.e(TAG,"path="+path);
            return path;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Response.Listener<CartBean[]> responseDownloadCartListener() {
        return new Response.Listener<CartBean[]>() {
            @Override
            public void onResponse(CartBean[] cartBeen) {
                if(cartBeen!=null) {
                    mAdapter.setMore(true);
                    mSwipeRefreshLayout.setRefreshing(false);
                    mtvHint.setVisibility(View.GONE);
                    mtvNothing.setVisibility(View.GONE);
//                    mAdapter.setFooterText(getResources().getString(R.string.load_more));
                    //将数组转换为集合
                    ArrayList<CartBean> list = Utils.array2List(cartBeen);
                    if (action == I.ACTION_DOWNLOAD || action == I.ACTION_PULL_DOWN) {
                        mAdapter.initItems(list);
                    } else if (action == I.ACTION_PULL_UP) {
                        mAdapter.addItems(list);
                    }
                    if(cartBeen.length<I.PAGE_SIZE_DEFAULT){
                        mAdapter.setMore(false);
//                        mAdapter.setFooterText(getResources().getString(R.string.no_more));
                    }
                }else{
                    mtvNothing.setVisibility(View.VISIBLE);
                }
            }
        };
    }

    private void initView(View layout) {
        mSwipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.sfl_cart);
        mSwipeRefreshLayout.setColorSchemeColors(
                R.color.google_blue,
                R.color.google_green,
                R.color.google_red,
                R.color.google_yellow
        );
        mtvHint = (TextView) layout.findViewById(R.id.tv_refresh_hint);
        mtvNothing = (TextView) layout.findViewById(R.id.tv_nothing);
        mtvNothing.setVisibility(View.GONE);
        mtvSumPrice = (TextView) layout.findViewById(R.id.tvSumPrice);
        mtvSavePrice = (TextView) layout.findViewById(R.id.tvSavePrice);
        mLinearLayoutManager = new LinearLayoutManager(mContext);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView = (RecyclerView) layout.findViewById(R.id.rv_cart);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mAdapter = new CartAdapter(mContext, mCartList);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
    }

    /**
     * 统计购物车中所有商品的总价和打折节省的钱
     */
    protected void sumPrice() {
        ArrayList<CartBean> cartList = FuLiCenterApplication.getInstance().getCartList();
        int sumRankPrice=0;//人民币折扣价
        int sumCurrentPrice=0;//人民币价
        //遍历购物车
        for(int i=0;i<cartList.size();i++){
            CartBean cart = cartList.get(i);
            GoodDetailsBean goods = cart.getGoods();
            if(cart.isChecked()){
                //当同一种商品有多件时，需要多次累加该商品的价格
                for(int k=0;k<cart.getCount();k++){
                    if(goods!=null) {
                        int rankPrice = convertPrice(goods.getRankPrice());
                        int currentPrice = convertPrice(goods.getCurrencyPrice());
                        sumRankPrice += rankPrice;
                        sumCurrentPrice += currentPrice;
                    }
                }
            }
        }
        int sumSavePrice=sumCurrentPrice-sumRankPrice;
        mtvSumPrice.setText("合计:￥"+sumRankPrice);
        mtvSavePrice.setText("节省:￥"+sumSavePrice);
    }

    /**
     * 将头部带￥的商品价格转换为int类型
     * @param strPrice
     * @return
     */
    private int convertPrice(String strPrice) {
        strPrice=strPrice.substring(strPrice.indexOf("￥")+1);
        int price=Integer.parseInt(strPrice);
        return price;
    }

    /**
     * 接收来自DownloadCartTask发送的购物车数据改变的广播
     * @author yao
     */
    class CartChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            initData();
        }
    }
    CartChangedReceiver mCartChangedReceiver;
    private void registerCartChangedReceiver() {
        mCartChangedReceiver=new CartChangedReceiver();
        IntentFilter filter=new IntentFilter("update_cart_list");
        getActivity().registerReceiver(mCartChangedReceiver, filter);
    }
}
