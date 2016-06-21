package cn.ucai.fulicenter.fragment;

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
    String path;
    String username;

    /** 下拉刷新控件*/
    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView mRecyclerView;
    TextView mtvHint;
    LinearLayoutManager mLinearLayoutManager;
    TextView mtvNothing;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = (FuliCenterMainActivity) getActivity();
        View layout = inflater.inflate(R.layout.fragment_cart,container,false);
        mCartList = FuLiCenterApplication.getInstance().getCartList();
        initView(layout);
        setListener();
        initData();
        return layout;
    }

    private void setListener() {
        setPullDownRefreshListener();
        setPullUpRefreshListener();
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
        try {
            getPath();
            mContext.executeRequest(new GsonRequest<CartBean[]>(path,
                    CartBean[].class, responseDownloadCartListener(),
                    mContext.errorListener()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private String getPath(){
        try {
            username = FuLiCenterApplication.getInstance().getUserName();
            path = new ApiParams()
                    .with(I.Cart.USER_NAME,username)
                    .with(I.PAGE_ID, I.PAGE_ID_DEFAULT + "")
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
            public void onResponse(CartBean[] boutiqueBeen) {
                if(boutiqueBeen!=null) {
                    mAdapter.setMore(true);
                    mSwipeRefreshLayout.setRefreshing(false);
                    mtvHint.setVisibility(View.GONE);
                    mtvNothing.setVisibility(View.GONE);
//                    mAdapter.setFooterText(getResources().getString(R.string.load_more));
                    //将数组转换为集合
                    ArrayList<CartBean> list = Utils.array2List(boutiqueBeen);
                    if (action == I.ACTION_DOWNLOAD || action == I.ACTION_PULL_DOWN) {
                        mAdapter.initItems(list);
                    } else if (action == I.ACTION_PULL_UP) {
                        mAdapter.addItems(list);
                    }
                    if(boutiqueBeen.length<I.PAGE_SIZE_DEFAULT){
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
        mLinearLayoutManager = new LinearLayoutManager(mContext);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView = (RecyclerView) layout.findViewById(R.id.rv_cart);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mAdapter = new CartAdapter(mContext, mCartList);
        mRecyclerView.setAdapter(mAdapter);
    }
}
