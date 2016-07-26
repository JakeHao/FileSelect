/*
 * 版权信息：嘉赛信息技术有限公司
 * Copyright (C) Justsy Information Technology Co., Ltd. All Rights Reserved
 *
 * FileName: .java
 * Description:
 *   <author> - <version> - <date> - <desc>
 *       jake - v1.1 - 2016.4.27 - 创建类
 *
 */
package com.jake.fileselect.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.KeyEvent;

import com.jake.fileselect.R;

/**
 * Created by jake on 16/4/28.
 */
public class DialogPermission extends AlertDialog.Builder
{
    private Dialog dialog;

    public DialogPermission(final Activity activity, int resMsg)
    {
        super(activity);
        setTitle(R.string.dialog_public_title);
        setMessage(resMsg);
        setPositiveButton(R.string.btn_confirm, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                activity.finish();
            }
        });
        setOnKeyListener(new DialogInterface.OnKeyListener()
        {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
            {
                return true;
            }
        });
        dialog = create();
        dialog.setCanceledOnTouchOutside(false);
    }

    public DialogPermission(final Activity activity, int resMsg, DialogInterface.OnClickListener onClickListener)
    {
        super(activity);
        setTitle(R.string.dialog_public_title);
        setMessage(resMsg);
        setPositiveButton(R.string.btn_confirm, onClickListener);
        setOnKeyListener(new DialogInterface.OnKeyListener()
        {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
            {
                return true;
            }
        });
        dialog = create();
        dialog.setCanceledOnTouchOutside(false);
    }

    public Dialog showDialog()
    {
        dialog.show();
        return dialog;
    }
}
