package com.jake.fileselect.common;

import android.support.annotation.NonNull;

public interface OnPermissionCallback
{
    void onPermissionGranted(@NonNull String[] permissionName);

    void onPermissionDeclined(@NonNull String[] permissionName);

    void onPermissionPreGranted(@NonNull String permissionsName);

    void onPermissionReallyDeclined(@NonNull String permissionName);

    void onNoPermissionNeeded();
}
