package cn.ucai.fulicenter.activity;

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
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.bean.AlbumBean;
import cn.ucai.fulicenter.bean.GoodDetailsBean;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_good_details);
        mContext = this;
        initView();
        initData();
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
