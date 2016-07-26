package com.jake.fileselect;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jake.fileselect.adapter.FileBarAdapter;
import com.jake.fileselect.adapter.FileExplorerAdapter;
import com.jake.fileselect.common.ComparatorFile;
import com.jake.fileselect.common.DialogPermission;
import com.jake.fileselect.common.FileInfo;
import com.jake.fileselect.common.OnPermissionCallback;
import com.jake.fileselect.common.ThreadPoolHelper;
import com.jake.fileselect.common.Utils;
import com.jake.fileselect.common.ViewHolderItemClick;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileSelectActivity extends AppCompatActivity implements View.OnClickListener, OnPermissionCallback
{
    public static final String KEY_SINGLE_CHECK = "key_single_check";
    public static final String KEY_RESULT = "key_result";
    private RecyclerView recyclerView;
    private ListView lvFile;
    private RelativeLayout footer;
    private RelativeLayout rlCurFile;
    private TextView tvCurFile;
    private TextView tvCheckSize;
    private TextView noDataView, btnConfirm;
    public static final int TYPE_ALL_FILE = 0;
    public static final int TYPE_SD_FILE = 1;
    public static final int TYPE_EXT_FILE = 2;
    private FileExplorerAdapter mAdapter;
    private PopupWindow mPopupWindow;
    private ImageView ivAllChecked, ivSDChecked, ivExtChecked;
    private int curType = TYPE_SD_FILE;
    private ComparatorFile mComparatorFile = new ComparatorFile();
    private String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private String externalPath;
    private File currentFile;
    private FileBarAdapter mFileBarAdapter;
    private ActionBar mActionBar;
    private FileFilter mFileFilter = new FileFilter()
    {
        @Override
        public boolean accept(File pathname)
        {
            return !pathname.isHidden() && pathname.exists();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        lvFile = (ListView)findViewById(R.id.lvFile);
        footer = (RelativeLayout)findViewById(R.id.footer);
        rlCurFile = (RelativeLayout)findViewById(R.id.rlCurFile);
        tvCurFile = (TextView)findViewById(R.id.tvCurFile);
        tvCheckSize = (TextView)findViewById(R.id.tvCheckSize);
        noDataView = (TextView)findViewById(R.id.noDataView);
        mActionBar = getSupportActionBar();
        initData();
        registerListener();
    }

    private void initData()
    {
        mFileBarAdapter = new FileBarAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mFileBarAdapter);
        noDataView.setText(R.string.txt_no_file);
        tvCheckSize.setText(String.format(getString(R.string.txt_check_file_size), Utils.formatFileSize(0)));
        boolean isSingleCheck = getIntent().getBooleanExtra(KEY_SINGLE_CHECK, true);
        mAdapter = new FileExplorerAdapter(this, isSingleCheck);
        mAdapter.registerDataSetObserver(new DataSetObserver()
        {
            @Override
            public void onChanged()
            {
                if (mAdapter.isEmpty())
                {
                    noDataView.setVisibility(View.VISIBLE);
                }
                else { noDataView.setVisibility(View.GONE); }
            }
        });
        lvFile.setAdapter(mAdapter);
        changeCurrentFolder();
        externalPath = getAvailableExternalStorage(this);
        checkStoragePermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.public_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_confirm);
        MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
        MenuItemCompat.setActionView(menuItem, R.layout.public_menu_button);
        View view = MenuItemCompat.getActionView(menuItem);
        btnConfirm = (TextView)view.findViewById(R.id.btnConfirm);
        btnConfirm.setText(R.string.btn_confirm);
        btnConfirm.setEnabled(false);
        btnConfirm.setTextColor(getResources().getColor(R.color.c999999));
        btnConfirm.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent();
                ArrayList<String> list = new ArrayList<>();
                for (FileInfo fileInfo : mAdapter.getCheckedList())
                {
                    list.add(fileInfo.getFilePath());
                }
                intent.putStringArrayListExtra(KEY_RESULT, list);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void registerListener()
    {
        rlCurFile.setOnClickListener(this);
        tvCurFile.setOnClickListener(this);
        lvFile.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                FileInfo fileInfo = mAdapter.getItem(position);
                if (fileInfo.isFile())
                {
                    mAdapter.changeChecked(fileInfo);
                    long count = 0;
                    for (FileInfo info : mAdapter.getCheckedList())
                    {
                        count += info.getFile().length();
                    }
                    tvCheckSize.setText(
                            String.format(getString(R.string.txt_check_file_size), Utils.formatFileSize(count)));
                    mAdapter.notifyDataSetChanged();
                    if (mAdapter.getCheckedList().isEmpty())
                    {
                        btnConfirm.setTextColor(getResources().getColor(R.color.c999999));
                        btnConfirm.setEnabled(false);
                        btnConfirm.setText(R.string.btn_confirm);
                    }
                    else
                    {
                        btnConfirm.setTextColor(Color.BLUE);
                        btnConfirm.setEnabled(true);
                        btnConfirm.setText(String.format(getString(R.string.txt_check_file_num),
                                                         mAdapter.getCheckedList().size()));
                    }
                }
                else
                {
                    showFiles(fileInfo.getFile());
                }
            }
        });
        mFileBarAdapter.setViewHolderItemClick(new ViewHolderItemClick<FileBarAdapter.ViewHolder>()
        {
            @Override
            public void onItemClick(FileBarAdapter.ViewHolder viewHolder)
            {
                int position = viewHolder.getAdapterPosition();
                FileInfo fileInfo = mFileBarAdapter.getItem(position);
                if (fileInfo.getFilePath().equals(currentFile.getAbsolutePath())) { return; }
                List<FileInfo> temp = new ArrayList<>();
                temp.addAll(mFileBarAdapter.getAll());
                mFileBarAdapter.clear();
                mFileBarAdapter.addAll(temp.subList(0, position + 1));
                mFileBarAdapter.notifyDataSetChanged();
                showFiles(fileInfo.getFile());
            }
        });
    }

    private void showFiles(final File folder)
    {
        currentFile = folder;
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFile(folder);
        fileInfo.setIsFile(folder.isFile());
        fileInfo.setFilePath(folder.getAbsolutePath());
        if (!mFileBarAdapter.contains(fileInfo))
        {
            if (currentFile.getAbsolutePath().equals("/"))
            {
                fileInfo.setFileName(getString(R.string.txt_all_file));
            }
            else if (currentFile.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath()))
            {
                fileInfo.setFileName(getString(R.string.txt_sd_file));
            }
            else if (currentFile.getAbsolutePath().equals(externalPath))
            {
                fileInfo.setFileName(getString(R.string.txt_ext_file));
            }
            else { fileInfo.setFileName(folder.getName()); }
            mFileBarAdapter.add(fileInfo);
            mFileBarAdapter.notifyDataSetChanged();
        }
        if (mFileBarAdapter.getItemCount() > 0)
        {
            recyclerView.smoothScrollToPosition(mFileBarAdapter.getItemCount() - 1);
        }
        ThreadPoolHelper.getInstance().execInCached(new Runnable()
        {
            @Override
            public void run()
            {
                final List<FileInfo> folderList = new ArrayList<>();
                final List<FileInfo> fileList = new ArrayList<>();
                if (folder != null && folder.isDirectory())
                {
                    File[] files = folder.listFiles(mFileFilter);
                    if (files != null)
                    {
                        FileInfo fileInfo = null;
                        for (File f : files)
                        {
                            fileInfo = new FileInfo();
                            fileInfo.setFile(f);
                            fileInfo.setIsFile(f.isFile());
                            fileInfo.setFileName(f.getName());
                            fileInfo.setFilePath(f.getAbsolutePath());
                            if (f.isFile())
                            {
                                fileInfo.setFileSize(Utils.formatFileSize(f.length()));
                                fileList.add(fileInfo);
                            }
                            else { folderList.add(fileInfo); }
                        }
                    }
                }
                Collections.sort(folderList, mComparatorFile);
                Collections.sort(fileList, mComparatorFile);
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mAdapter.clear();
                        mAdapter.addAll(folderList);
                        mAdapter.addAll(fileList);
                        mAdapter.notifyDataSetChanged();
                        lvFile.setSelection(0);
                    }
                });
            }
        });
    }

    /**
     * 获取外部存储设备
     *
     * @param context
     * @return
     */
    public String getAvailableExternalStorage(Context context)
    {
        StorageManager mStorageManager = (StorageManager)context.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try
        {
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Method mState = storageVolumeClazz.getMethod("getState");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++)
            {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String)getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean)isRemovable.invoke(storageVolumeElement);
                String state = (String)mState.invoke(storageVolumeElement);
                //mRemovable=false：为内置存储  =true：为外置存储
                //mPath 为存储设置路径
                //mState 为存储设置挂载状态
                if (removable && Environment.MEDIA_MOUNTED.equals(state))
                {
                    return path;
                }
            }
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (InvocationTargetException e)
        {
            e.printStackTrace();
        }
        catch (NoSuchMethodException e)
        {
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private void showFolder()
    {
        if (mPopupWindow == null) { mPopupWindow = createListPopupWindow(); }
        if (mPopupWindow.isShowing())
        {
            mPopupWindow.dismiss();
        }
        else
        {
            backgroundAlpha(0.3f);
            DisplayMetrics displayMetrics = Utils.getScreenPix(this);
            int y;
            if (TextUtils.isEmpty(externalPath))
            {
                y = displayMetrics.heightPixels - (int)Utils.dpToPx(this, 160 + 44);
            }
            else
            {
                y = displayMetrics.heightPixels - (int)Utils.dpToPx(this, 240 + 44);
            }
            changeCurrentFolder();
            mPopupWindow.showAtLocation(footer, Gravity.NO_GRAVITY, 0, y);
        }
    }

    private PopupWindow createListPopupWindow()
    {
        View view = null;
        if (TextUtils.isEmpty(externalPath))
        {
            view = LayoutInflater.from(this).inflate(R.layout.layout_popwind_folder_no_extsd, null);
        }
        else
        {
            view = LayoutInflater.from(this).inflate(R.layout.layout_popwind_folder, null);
            view.findViewById(R.id.rlExt).setOnClickListener(this);
            ivExtChecked = (ImageView)view.findViewById(R.id.ivExtChecked);
        }
        view.findViewById(R.id.rlAll).setOnClickListener(this);
        view.findViewById(R.id.rlSD).setOnClickListener(this);
        ivAllChecked = (ImageView)view.findViewById(R.id.ivAllChecked);
        ivSDChecked = (ImageView)view.findViewById(R.id.ivSDChecked);
        DisplayMetrics displayMetrics = Utils.getScreenPix(this);
        if (TextUtils.isEmpty(externalPath))
        {
            mPopupWindow = new PopupWindow(view, displayMetrics.widthPixels, (int)Utils.dpToPx(this, 160));
        }
        else
        {
            mPopupWindow = new PopupWindow(view, displayMetrics.widthPixels, (int)Utils.dpToPx(this, 240));
        }
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mPopupWindow.setAnimationStyle(R.style.dialogAnim);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener()
        {
            @Override
            public void onDismiss()
            {
                backgroundAlpha(1.0f);
            }
        });
        return mPopupWindow;
    }

    /**
     * 设置屏幕透明度  0.0透明  1.0不透明
     */
    public void backgroundAlpha(float alpha)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            lvFile.setAlpha(alpha);
            footer.setAlpha(1.0f);
        }
    }

    private void changeCurrentFolder()
    {
        if (TYPE_ALL_FILE == curType)
        {
            mActionBar.setTitle(R.string.txt_all_file);
            tvCurFile.setText(R.string.txt_all_file);
            if (mPopupWindow != null)
            {
                ivAllChecked.setVisibility(View.VISIBLE);
                ivSDChecked.setVisibility(View.INVISIBLE);
                if (ivExtChecked != null) { ivExtChecked.setVisibility(View.INVISIBLE); }
            }
        }
        else if (TYPE_SD_FILE == curType)
        {
            mActionBar.setTitle(R.string.txt_sd_file);
            tvCurFile.setText(R.string.txt_sd_file);
            if (mPopupWindow != null)
            {
                ivAllChecked.setVisibility(View.INVISIBLE);
                ivSDChecked.setVisibility(View.VISIBLE);
                if (ivExtChecked != null) { ivExtChecked.setVisibility(View.INVISIBLE); }
            }
        }
        else if (TYPE_EXT_FILE == curType)
        {
            mActionBar.setTitle(R.string.txt_ext_file);
            tvCurFile.setText(R.string.txt_ext_file);
            if (mPopupWindow != null)
            {
                ivAllChecked.setVisibility(View.INVISIBLE);
                ivSDChecked.setVisibility(View.INVISIBLE);
                if (ivExtChecked != null) { ivExtChecked.setVisibility(View.VISIBLE); }
            }
        }
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.rlCurFile:
            case R.id.tvCurFile:
                showFolder();
                break;
            case R.id.rlAll:
                mPopupWindow.dismiss();
                if (curType == TYPE_ALL_FILE) { return; }
                curType = TYPE_ALL_FILE;
                changeCurrentFolder();
                rootPath = "/";
                mFileBarAdapter.clear();
                mFileBarAdapter.notifyDataSetChanged();
                showFiles(new File(rootPath));
                break;
            case R.id.rlSD:
                mPopupWindow.dismiss();
                if (curType == TYPE_SD_FILE) { return; }
                curType = TYPE_SD_FILE;
                changeCurrentFolder();
                rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                mFileBarAdapter.clear();
                mFileBarAdapter.notifyDataSetChanged();
                showFiles(new File(rootPath));
                break;
            case R.id.rlExt:
                mPopupWindow.dismiss();
                if (curType == TYPE_EXT_FILE) { return; }
                curType = TYPE_EXT_FILE;
                changeCurrentFolder();
                rootPath = externalPath;
                mFileBarAdapter.clear();
                mFileBarAdapter.notifyDataSetChanged();
                showFiles(new File(rootPath));
                break;
        }
    }

    @Override
    public void onBackPressed()
    {
        if (!TextUtils.isEmpty(rootPath) && currentFile != null && !rootPath.equals(currentFile.getAbsolutePath()))
        {
            mFileBarAdapter.remove(mFileBarAdapter.getItemCount() - 1);
            mFileBarAdapter.notifyDataSetChanged();
            FileInfo fileInfo = mFileBarAdapter.getItem(mFileBarAdapter.getItemCount() - 1);
            showFiles(fileInfo.getFile());
        }
        else { super.onBackPressed(); }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (requestCode == 100)
        {
            boolean isAllGranted = true;
            if (grantResults.length < 1)
            {
                isAllGranted = false;
            }
            else
            {
                for (int flag : grantResults)
                {
                    if (flag != PackageManager.PERMISSION_GRANTED)
                    {
                        isAllGranted = false;
                        break;
                    }
                }
            }
            if (isAllGranted)
            {
                //所有请求权限都被同意
                onPermissionGranted(permissions);
            }
            else
            {
                List<String> declinedPermissions = new ArrayList<>();
                for (String permission : permissions)
                {
                    if (PackageManager.PERMISSION_DENIED == ActivityCompat.checkSelfPermission(this, permission))
                    {
                        declinedPermissions.add(permission);
                    }
                }
                List<Boolean> deniedPermissionsLength = new ArrayList<Boolean>();//needed
                for (String permissionName : declinedPermissions)
                {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissionName))
                    {
                        onPermissionReallyDeclined(permissionName);
                        deniedPermissionsLength.add(false);
                    }
                }
                if (deniedPermissionsLength.isEmpty())
                {
                    onPermissionDeclined(declinedPermissions.toArray(new String[declinedPermissions.size()]));
                }
            }
        }
    }

    private void checkStoragePermission(String permission)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (permissionExists(permission))
            {
                int flag = ActivityCompat.checkSelfPermission(this, permission);
                if (flag == PackageManager.PERMISSION_GRANTED)
                {
                    //preGranted
                    onPermissionPreGranted(permission);
                }
                else
                {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
                    {
                        //ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
                        //为true 之前请求过该权限
                        flag = ActivityCompat.checkSelfPermission(this, permission);
                        if (flag == PackageManager.PERMISSION_GRANTED)
                        {
                            //preGranted
                            onPermissionPreGranted(permission);
                        }
                        else
                        {
                            ActivityCompat.requestPermissions(this, new String[] { permission }, 100);
                        }
                    }
                    else
                    {
                        ActivityCompat.requestPermissions(this, new String[] { permission }, 100);
                    }
                }
            }
            else
            {
                //preGranted
                onPermissionPreGranted(permission);
            }
        }
        else
        {
            //onNoPermissionNeeded
            onNoPermissionNeeded();
        }
    }

    /**
     * @return true if permission exists in the manifest, false otherwise.
     */
    public boolean permissionExists(@NonNull String permissionName)
    {
        try
        {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(),
                                                                         PackageManager.GET_PERMISSIONS);
            if (packageInfo.requestedPermissions != null)
            {
                for (String p : packageInfo.requestedPermissions)
                {
                    if (p.equals(permissionName))
                    {
                        return true;
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onPermissionGranted(@NonNull String[] permissionName)
    {
        showFiles(new File(rootPath));
    }

    @Override
    public void onPermissionDeclined(@NonNull String[] permissionName)
    {
        new DialogPermission(this, R.string.dialog_no_storage_permission_tip, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                Utils.openSettingsScreen(getApplicationContext());
                finish();
            }
        }).showDialog();
    }

    @Override
    public void onPermissionPreGranted(@NonNull String permissionsName)
    {
        showFiles(new File(rootPath));
    }

    @Override
    public void onPermissionReallyDeclined(@NonNull String permissionName)
    {
        new DialogPermission(this, R.string.dialog_no_storage_permission_tip).showDialog();
    }

    @Override
    public void onNoPermissionNeeded()
    {
        showFiles(new File(rootPath));
    }
}
