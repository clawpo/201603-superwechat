package cn.ucai.fulicenter.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.bean.CartBean;
import cn.ucai.fulicenter.bean.GoodDetailsBean;
import cn.ucai.fulicenter.utils.ImageUtils;

import static android.support.v7.widget.RecyclerView.ViewHolder;

public class CartAdapter extends RecyclerView.Adapter<ViewHolder> {
    private static final String TAG = CartAdapter.class.getName();
    Context mContext;
    ArrayList<CartBean> mCartList;

    CartItemViewHolder cartViewHolder;

    private boolean isMore;

    public boolean isMore() {
        return isMore;
    }

    public void setMore(boolean more) {
        isMore = more;
    }

    public CartAdapter(Context mContext, ArrayList<CartBean> list) {
        this.mContext = mContext;
        this.mCartList = list;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewHolder holder = new CartItemViewHolder(inflater.inflate(R.layout.item_cart,parent,false));
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
//        if(holder instanceof CartItemViewHolder){
            cartViewHolder = (CartItemViewHolder) holder;
            final CartBean cart = mCartList.get(position);
            GoodDetailsBean goods = cart.getGoods();
            if(goods==null){
                return;
            }
            cartViewHolder.tvGoodsName.setText(goods.getGoodsName());
            cartViewHolder.tvCartCount.setText("("+cart.getCount()+")");
            cartViewHolder.tvGoodsPrice.setText(goods.getCurrencyPrice());
            String path = I.DOWNLOAD_GOODS_THUMB_URL+cart.getGoods().getGoodsThumb();
            ImageUtils.setThumb(path,cartViewHolder.ivGoodsThumb);
//        }

    }

    @Override
    public int getItemCount() {
        return mCartList ==null?0: mCartList.size();
    }

    public void initItems(ArrayList<CartBean> list) {
        if(mCartList !=null && !mCartList.isEmpty()){
            mCartList.clear();
        }
        mCartList.addAll(list);
        notifyDataSetChanged();
    }

    public void addItems(ArrayList<CartBean> list) {
        mCartList.addAll(list);
        notifyDataSetChanged();
    }


    class CartItemViewHolder extends ViewHolder{
        TextView tvGoodsName;
        TextView tvCartCount;
        ImageView ivAddCart;
        ImageView ivReduceCart;
        NetworkImageView ivGoodsThumb;
        TextView tvGoodsPrice;

        CheckBox chkChecked;

        public CartItemViewHolder(View itemView) {
            super(itemView);
            tvCartCount=(TextView) itemView.findViewById(R.id.tvCartCount);
            tvGoodsName=(TextView) itemView.findViewById(R.id.tvGoodsName);
            ivAddCart=(ImageView) itemView.findViewById(R.id.ivAddCart);
            ivReduceCart=(ImageView) itemView.findViewById(R.id.ivReduceCart);
            ivGoodsThumb=(NetworkImageView) itemView.findViewById(R.id.ivGoodsThumb);
            tvGoodsPrice=(TextView) itemView.findViewById(R.id.tvGoodsPrice);
            chkChecked=(CheckBox) itemView.findViewById(R.id.chkSelect);
        }
    }
}
