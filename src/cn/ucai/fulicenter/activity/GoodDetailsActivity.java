package cn.ucai.fulicenter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.toolbox.NetworkImageView;

import cn.ucai.fulicenter.D;
import cn.ucai.fulicenter.FuLiCenterApplication;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.bean.AlbumBean;
import cn.ucai.fulicenter.bean.GoodDetailsBean;
import cn.ucai.fulicenter.bean.MessageBean;
import cn.ucai.fulicenter.bean.User;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
import cn.ucai.fulicenter.task.DownloadCollectCountTask;
import cn.ucai.fulicenter.utils.ImageUtils;
import cn.ucai.fulicenter.utils.Utils;
import cn.ucai.fulicenter.view.DisplayUtils;
import cn.ucai.fulicenter.view.FlowIndicator;
import cn.ucai.fulicenter.view.SlideAutoLoopView;

/**
 * Created by clawpo on 16/6/16.
 */
public class GoodDetailsActivity extends BaseActivity {
    public static final String TAG = GoodDetailsActivity.class.getName();
    GoodDetailsActivity mContext;
    GoodDetailsBean mGoodDetails;
    int mGoodsId;

    SlideAutoLoopView mSlideAutoLoopView;
    FlowIndicator mFlowIndicator;
    /** 显示颜色的容器布局*/
    LinearLayout mLayoutColors;
    ImageView mivCollect;
    ImageView mivAddCart;
    ImageView mivShare;
    TextView mtvCartCount;

    TextView tvGoodName;
    TextView tvGoodEngishName;
    TextView tvShopPrice;
    TextView tvCurrencyPrice;
    WebView wvGoodBrief;

    /** 当前的颜色值*/
    int mCurrentColor;
    /**当前商品是否收藏*/
    boolean isCollect;
    private int actionCollect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_good_details);
        mContext = this;
        initView();
        initData();
        setListener();
    }

    private void setListener() {
        setCollectClickListener();
    }
    /**
     * 设置收藏/取消收藏按钮的点击事件监听
     */
    private void setCollectClickListener() {
        mivCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = FuLiCenterApplication.getInstance().getUser();
                if(user==null){
                    startActivity(new Intent(GoodDetailsActivity.this,LoginActivity.class));
                }else {
                    String userName = user.getMUserName();
                    try {
                        String path = "";
                        if(isCollect){
                            path = new ApiParams()
                                    .with(I.Collect.GOODS_ID, mGoodsId+"")
                                    .with(I.Collect.USER_NAME, userName)
                                    .getRequestUrl(I.REQUEST_DELETE_COLLECT);
                            actionCollect = I.ACTION_DELETE_COLLECT;
                        }else{
                            path = new ApiParams()
                                    .with(I.Collect.GOODS_ID, mGoodsId+"")
                                    .with(I.Collect.USER_NAME, userName)
                                    .with(I.Collect.GOODS_NAME, mGoodDetails.getGoodsName())
                                    .with(I.Collect.GOODS_ENGLISH_NAME, mGoodDetails.getGoodsEnglishName())
                                    .with(I.Collect.GOODS_THUMB, mGoodDetails.getGoodsThumb())
                                    .with(I.Collect.GOODS_IMG, mGoodDetails.getGoodsImg())
                                    .with(I.Collect.ADD_TIME, mGoodDetails.getAddTime()+"")
                                    .getRequestUrl(I.REQUEST_ADD_COLLECT);
                            Log.e(TAG,"path="+path);
                            actionCollect = I.ACTION_ADD_COLLECT;
                        }
                        executeRequest(new GsonRequest<MessageBean>(path,MessageBean.class,
                                responseSetCollectListener(actionCollect),errorListener()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private Response.Listener<MessageBean> responseSetCollectListener(final int action) {
        return new Response.Listener<MessageBean>() {
            @Override
            public void onResponse(MessageBean messageBean) {
                if(messageBean.isSuccess()){
                    if(action == I.ACTION_ADD_COLLECT){
                        isCollect = true;
                        mivCollect.setImageResource(R.drawable.bg_collect_out);
                    }
                    if(action == I.ACTION_DELETE_COLLECT){
                        isCollect = false;
                        mivCollect.setImageResource(R.drawable.bg_collect_in);
                    }
                    new DownloadCollectCountTask(mContext).execute();
                }
                Utils.showToast(mContext,messageBean.getMsg(),Toast.LENGTH_SHORT);
            }
        };
    }

    private void initData() {
        mGoodsId=getIntent().getIntExtra(D.GoodDetails.KEY_GOODS_ID, 0);
        try {
            String path = new ApiParams().with(I.CategoryGood.GOODS_ID, mGoodsId+"")
                    .getRequestUrl(I.REQUEST_FIND_GOOD_DETAILS);
            Log.e(TAG,"path="+path);
            executeRequest(new GsonRequest<GoodDetailsBean>(path,GoodDetailsBean.class,
                    responseDownloadGoodDetailsListener(),errorListener()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Response.Listener<GoodDetailsBean> responseDownloadGoodDetailsListener() {
        return new Response.Listener<GoodDetailsBean>() {
            @Override
            public void onResponse(GoodDetailsBean goodDetailsBean) {
                if(goodDetailsBean!=null){
                    mGoodDetails = goodDetailsBean;
                    DisplayUtils.initBackWithTitle(mContext,getResources().getString(R.string.title_good_details));
                    tvCurrencyPrice.setText(mGoodDetails.getCurrencyPrice());
                    tvGoodEngishName.setText(mGoodDetails.getGoodsEnglishName());
                    tvGoodName.setText(mGoodDetails.getGoodsName());
                    wvGoodBrief.loadDataWithBaseURL(null, mGoodDetails.getGoodsBrief().trim(), D.TEXT_HTML, D.UTF_8, null);

                    //初始化颜色面板
                    initColorsBanner();
                }else {
                    Utils.showToast(mContext, "商品详情下载失败", Toast.LENGTH_LONG);
                    finish();
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        initCollectStatus();
    }
    private void initCollectStatus(){
        User user = FuLiCenterApplication.getInstance().getUser();
        Log.e(TAG,"initCollectStatus,user="+user);
        if(user!=null){
            String userName = user.getMUserName();
            try {
                String path = new ApiParams().with(I.Collect.GOODS_ID, mGoodsId+"")
                        .with(I.Collect.USER_NAME, userName)
                        .getRequestUrl(I.REQUEST_IS_COLLECT);
                executeRequest(new GsonRequest<MessageBean>(path,MessageBean.class,
                        responseIsCollectListener(),errorListener()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            isCollect = false;
            mivCollect.setImageResource(R.drawable.bg_collect_in);
        }
    }

    private Response.Listener<MessageBean> responseIsCollectListener() {
        return new Response.Listener<MessageBean>() {
            @Override
            public void onResponse(MessageBean messageBean) {
                if(messageBean.isSuccess()){
                    isCollect = true;
                    mivCollect.setImageResource(R.drawable.bg_collect_out);
                }else{
                    isCollect = false;
                    mivCollect.setImageResource(R.drawable.bg_collect_in);
                }
            }
        };
    }

    private void initColorsBanner() {
        //设置第一个颜色的图片轮播
        updateColor(0);
        for(int i=0;i<mGoodDetails.getProperties().length;i++){
            mCurrentColor=i;
            View layout=View.inflate(mContext, R.layout.layout_property_color, null);
            final NetworkImageView ivColor=(NetworkImageView) layout.findViewById(R.id.ivColorItem);
            Log.i(TAG,"initColorsBanner.goodDetails="+mGoodDetails.getProperties()[i].toString());
            String colorImg = mGoodDetails.getProperties()[i].getColorImg();
            if(colorImg.isEmpty()){
                continue;
            }
            ImageUtils.setGoodDetailThumb(colorImg,ivColor);
            mLayoutColors.addView(layout);
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateColor(mCurrentColor);
                }
            });
        }
    }
    /**
     * 设置指定属性的图片轮播
     * @param i
     */
    private void updateColor(int i) {
        AlbumBean[] albums = mGoodDetails.getProperties()[i].getAlbums();
        String[] albumImgUrl=new String[albums.length];
        for(int j=0;j<albumImgUrl.length;j++){
            albumImgUrl[j]=albums[j].getImgUrl();
        }
        mSlideAutoLoopView.startPlayLoop(mFlowIndicator, albumImgUrl, albumImgUrl.length);
    }

    private void initView() {
        mivCollect= (ImageView) findViewById(R.id.ivCollect);
        mivAddCart= (ImageView) findViewById(R.id.ivAddCart);
        mivShare = (ImageView) findViewById(R.id.ivShare);
        mtvCartCount= (TextView) findViewById(R.id.tvCartCount);

        mSlideAutoLoopView= (SlideAutoLoopView) findViewById(R.id.salv);
        mFlowIndicator= (FlowIndicator) findViewById(R.id.indicator);
        mLayoutColors= (LinearLayout) findViewById(R.id.layoutColorSelector);
        tvCurrencyPrice= (TextView) findViewById(R.id.tvCurrencyPrice);
        tvGoodEngishName= (TextView) findViewById(R.id.tvGoodEnglishName);
        tvGoodName= (TextView) findViewById(R.id.tvGoodName);
        tvShopPrice= (TextView) findViewById(R.id.tvShopPrice);
        wvGoodBrief= (WebView) findViewById(R.id.wvGoodBrief);
        WebSettings settings = wvGoodBrief.getSettings();
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setBuiltInZoomControls(true);
    }
}
