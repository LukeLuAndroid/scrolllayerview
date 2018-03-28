package layerview.lyd.com.scrolllayerview;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    RecyclerView mRecycler;
    List<ResolveInfo> mList;
    RecyclerView.Adapter adapter;
    ShareBottomLayout layout_bottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        layout_bottom = findViewById(R.id.layout_bottom);
        findViewById(R.id.tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup();
            }
        });

        mList = new ArrayList<>();
        mRecycler = findViewById(R.id.recycler);
        adapter = new RecyclerView.Adapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new MyHolder(View.inflate(MainActivity.this, R.layout.item_layout, null));
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
                ResolveInfo info = mList.get(position);
                MyHolder h = (MyHolder) holder;
                h.tv.setTag(position);
                h.tv.setText(info.activityInfo.name);
                h.tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int p = (int) v.getTag();
                        ResolveInfo info = mList.get(p);
                        shareAppLink(MainActivity.this, "aaa", new ComponentName(info.activityInfo.packageName, info.activityInfo.name));
                    }
                });
            }

            @Override
            public int getItemCount() {
                return mList.size();
            }
        };
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.setAdapter(adapter);
        getSystemApps();
    }

    private void showPopup() {
        layout_bottom.show();
    }

    private class MyHolder extends RecyclerView.ViewHolder {
        public TextView tv;

        public MyHolder(View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tv);
        }
    }

    private void getSystemApps() {
        PackageManager pm = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_SEND, null);
//        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setType("text/plain");

        List<ResolveInfo> list = pm.queryIntentActivities(intent, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
        for (int i = 0; i < list.size(); i++) {
            ResolveInfo info = list.get(i);
            Log.d("appInfo", info.activityInfo.packageName + ",activity=" + info.activityInfo.name);
        }
        mList.addAll(list);
        adapter.notifyDataSetChanged();
    }

    /**
     * 分享app给好友
     */
    public static void shareAppLink(Activity context, String extra, ComponentName componentName) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, extra);
        shareIntent.setComponent(componentName);
        context.startActivityForResult(Intent.createChooser(shareIntent, "发送"), 100);
    }
}
