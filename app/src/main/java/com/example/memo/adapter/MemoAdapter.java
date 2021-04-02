package com.example.memo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import com.example.memo.R;
import com.example.memo.entity.Memo;

public class MemoAdapter extends ArrayAdapter<Memo> {
    private int resourceId;
    private ImageView alarm;
    List<Memo> memos;

    public MemoAdapter(Context context, int resource, List<Memo> memos) {
        super(context, resource, memos);
        this.memos=memos;
        resourceId = resource;
    }

    /**
     * 每当滚动屏幕时就调用
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Memo memo = getItem(position);//获取当前项的Memo实例
        View view=LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
        TextView memoTitle = view.findViewById(R.id.memoTitle);
        TextView memoContent = view.findViewById(R.id.memoContent);
        TextView memoCreateTime = view.findViewById(R.id.memoCreateTime);
        TextView memoAlarmTime = view.findViewById(R.id.memoAlarmTime);
        TextView memoLocation = view.findViewById(R.id.memoLocation);
        ImageView alarm = view.findViewById(R.id.alarm);

        memoTitle.setText(memo.getTitle());
        memoContent.setText(memo.getContent());
        memoCreateTime.setText("备忘录创建时间："+memo.getCreateTime());
        if(!String.valueOf(memo.getAlarmTime()).equals("null-null-null null:null")) {
            memoAlarmTime.setText("备忘录响铃时间：" + String.valueOf(memo.getAlarmTime()));
            alarm.setVisibility(View.VISIBLE);
        }
        memoLocation.setText(memo.getLocation());
        return view;
    }

    public void setList(List<Memo> memos){
        this.memos=memos;
        notifyDataSetChanged();       //更新布局
    }
}
