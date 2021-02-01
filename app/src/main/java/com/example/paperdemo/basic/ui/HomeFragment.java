package com.example.paperdemo.basic.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.paperdemo.basic.R;
import com.ikangtai.papersdk.util.ImageUtil;
import com.ikangtai.papersdk.util.ToastUtils;
import com.ikangtai.paperui.PaperCameraActivity;
import com.ikangtai.paperui.PaperClipActivity;
import com.ikangtai.paperui.view.ActionSheetDialog;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {
    public static final String TAG = HomeFragment.class.getSimpleName();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        root.findViewById(R.id.image_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ActionSheetDialog(getContext())
                        .builder()
                        .setCancelable(false)
                        .setCanceledOnTouchOutside(false)
                        .addSheetItem(getString(R.string.photo), ActionSheetDialog.SheetItemColor.Blue,
                                new ActionSheetDialog.OnSheetItemClickListener() {
                                    @Override
                                    public void onClick(int which) {
                                        //进行试纸拍照识别
                                        Intent intent = new Intent(getContext(), PaperCameraActivity.class);
                                        startActivity(intent);
                                    }
                                })
                        .addSheetItem(getString(R.string.camera), ActionSheetDialog.SheetItemColor.Blue,
                                new ActionSheetDialog.OnSheetItemClickListener() {
                                    @Override
                                    public void onClick(int which) {
                                        //相册选择图片
                                        Intent intent = new Intent(Intent.ACTION_PICK);
                                        intent.setType("image/*");
                                        startActivityForResult(intent, 1001);
                                    }
                                }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                    }
                }).show();
            }
        });
        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "页面返回结果 requestCode：" + requestCode + " resultCode:" + resultCode);
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri fileUri = data.getData();
                if (fileUri != null) {
                    // 得到图片的全路径
                    final String uriStr = ImageUtil.getPathFromUri(getContext(), fileUri);
                    if (TextUtils.isEmpty(uriStr)) {
                        ToastUtils.show(getContext(), "图片错误");
                        return;
                    }
                    //相册选择图片裁剪识别
                    Intent intent = new Intent(getContext(), PaperClipActivity.class);
                    intent.putExtra("paperUri", uriStr);
                    startActivity(intent);
                } else {
                    ToastUtils.show(getContext(), "权限不足");
                }
            }
        }

    }
}