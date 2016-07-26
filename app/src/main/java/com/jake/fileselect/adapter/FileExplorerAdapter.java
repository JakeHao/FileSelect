package com.jake.fileselect.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.jake.fileselect.common.FileInfo;
import com.jake.fileselect.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yinhao on 16/7/21.
 */
public class FileExplorerAdapter extends AbsBaseAdapter<FileInfo>
{
    public static final int TYPE_FILE = 0;
    public static final int TYPE_FOLDER = 1;
    private List<FileInfo> checkedList;
    private boolean isSingleCheck;

    public FileExplorerAdapter(Context context, boolean isSingleCheck)
    {
        super(context);
        checkedList = new ArrayList<>();
        this.isSingleCheck = isSingleCheck;
    }

    public void changeChecked(FileInfo fileInfo)
    {
        if (isSingleCheck)
        {
            if (checkedList.isEmpty())
            {
                checkedList.add(fileInfo);
            }
            else if (checkedList.contains(fileInfo))
            {
                checkedList.remove(fileInfo);
            }
            else
            {
                checkedList.clear();
                checkedList.add(fileInfo);
            }
        }
        else
        {
            if (checkedList.contains(fileInfo))
            {
                checkedList.remove(fileInfo);
            }
            else { checkedList.add(fileInfo); }
        }
    }

    public List<FileInfo> getCheckedList()
    {
        return checkedList;
    }

    @Override
    protected void convert(int itemViewType, ViewHolderHelper helper, FileInfo item, ViewGroup parent)
    {
        helper.setText(R.id.tvFileName, item.getFileName());
        if (itemViewType == TYPE_FILE)
        {
            helper.setText(R.id.tvFileSize, item.getFileSize());
            helper.setChecked(R.id.checkBox, checkedList.contains(item));
        }
    }

    @Override
    protected View genConvertView(int itemViewType, int position, ViewGroup parent)
    {
        if (itemViewType == TYPE_FILE)
        {
            return inflater.inflate(R.layout.layout_file_explorer_file, parent, false);
        }
        else { return inflater.inflate(R.layout.layout_file_explorer_folder, parent, false); }
    }

    @Override
    public int getViewTypeCount()
    {
        return 2;
    }

    @Override
    public int getItemViewType(int position)
    {
        FileInfo fileInfo = getItem(position);
        if (fileInfo.isFile()) { return TYPE_FILE; }
        else { return TYPE_FOLDER; }
    }
}
