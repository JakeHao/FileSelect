package com.jake.fileselect.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jake.fileselect.common.FileInfo;
import com.jake.fileselect.R;
import com.jake.fileselect.common.ViewHolderItemClick;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yinhao on 16/7/22.
 */
public class FileBarAdapter extends RecyclerView.Adapter<FileBarAdapter.ViewHolder>
{
    private ViewHolderItemClick<ViewHolder> mViewHolderItemClick;
    private List<FileInfo> list;
    private LayoutInflater mInflater;

    public FileBarAdapter(Context context)
    {
        list = new ArrayList<>();
        mInflater = LayoutInflater.from(context);
    }

    public void setViewHolderItemClick(ViewHolderItemClick<ViewHolder> viewHolderItemClick)
    {
        mViewHolderItemClick = viewHolderItemClick;
    }

    public boolean contains(FileInfo fileInfo)
    {
        return list.contains(fileInfo);
    }

    public void add(FileInfo fileInfo)
    {
        list.add(fileInfo);
    }

    public void remove(int position)
    {
        list.remove(position);
    }

    public void remove(FileInfo fileInfo)
    {
        list.remove(fileInfo);
    }

    public void addAll(List<FileInfo> list)
    {
        this.list.addAll(list);
    }

    public List<FileInfo> getAll()
    {
        return list;
    }

    public void clear()
    {
        list.clear();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = mInflater.inflate(R.layout.item_file_bar, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        FileInfo fileInfo = getItem(position);
        holder.txtFileName.setText(fileInfo.getFileName());
    }

    public FileInfo getItem(int position)
    {
        return list.get(position);
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView txtFileName;
        ImageView ivMore;

        public ViewHolder(View itemView)
        {
            super(itemView);
            txtFileName = (TextView)itemView.findViewById(R.id.txtFileName);
            ivMore = (ImageView)itemView.findViewById(R.id.ivMore);
            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (mViewHolderItemClick != null) { mViewHolderItemClick.onItemClick(ViewHolder.this); }
                }
            });
        }
    }
}
