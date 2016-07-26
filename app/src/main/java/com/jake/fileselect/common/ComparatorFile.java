package com.jake.fileselect.common;

import java.util.Comparator;

/**
 * Created by yinhao on 16/7/21.
 */
public class ComparatorFile implements Comparator<FileInfo>
{
    @Override
    public int compare(FileInfo lhs, FileInfo rhs)
    {
        return lhs.getFileName().compareTo(rhs.getFileName());
    }
}
