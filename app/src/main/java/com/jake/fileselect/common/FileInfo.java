/*
 * 版权信息：嘉赛信息技术有限公司
 * Copyright (C) Justsy Information Technology Co., Ltd. All Rights Reserved
 *
 * fileName: .java
 * Description:
 * <author> - <version> - <date> - <desc>
 *    JakeHao -  v1.0 - 2016.1.4 - 创建类
 */
package com.jake.fileselect.common;

import java.io.File;

public class FileInfo
{
    private String fileName;
    private boolean isFile;
    private String filePath;
    private String fileSize;
    private File file;

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public boolean isFile()
    {
        return isFile;
    }

    public void setIsFile(boolean file)
    {
        isFile = file;
    }

    public String getFilePath()
    {
        return filePath;
    }

    public void setFilePath(String filePath)
    {
        this.filePath = filePath;
    }

    public String getFileSize()
    {
        return fileSize;
    }

    public void setFileSize(String fileSize)
    {
        this.fileSize = fileSize;
    }

    public File getFile()
    {
        return file;
    }

    public void setFile(File file)
    {
        this.file = file;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o != null && o instanceof FileInfo)
        {
            return ((FileInfo)o).getFilePath().equals(filePath);
        }
        return super.equals(o);
    }
}
