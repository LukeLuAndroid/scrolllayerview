package layerview.lyd.com.scrolllayerview;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by luyd on 2018/3/20.
 */

public class ShareAdapter extends RecyclerView.Adapter {
    private Context mContext;
    private List<ResolveInfo> mList;
    private PackageManager mPackage;

    public ShareAdapter(Context mContext, List<ResolveInfo> list) {
        this.mContext = mContext;
        mList = list;
        mPackage = mContext.getPackageManager();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyHolder(View.inflate(mContext, R.layout.item_layout, null));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        ResolveInfo info = mList.get(position);
        MyHolder h = (MyHolder) holder;
        Drawable d = info.loadIcon(mPackage);
        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        h.img.setImageDrawable(d);

        h.itemView.setTag(position);
        h.tv.setText(info.loadLabel(mPackage));
        h.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Share","item click");
                int p = (int) v.getTag();
                ResolveInfo info = mList.get(p);
//                shareAppLink(MainActivity.this, "aaa", new ComponentName(info.activityInfo.packageName, info.activityInfo.name));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    private class MyHolder extends RecyclerView.ViewHolder {
        public TextView tv;
        public ImageView img;

        public MyHolder(View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tv);
            img = itemView.findViewById(R.id.img);
        }
    }

}
