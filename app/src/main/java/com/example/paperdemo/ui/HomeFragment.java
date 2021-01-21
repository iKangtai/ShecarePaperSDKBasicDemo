package com.example.paperdemo.ui;

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

import com.example.paperdemo.PaperCameraActivity;
import com.example.paperdemo.PaperClipActivity;
import com.example.paperdemo.PaperDetailActivity;
import com.example.paperdemo.R;
import com.example.paperdemo.view.ActionSheetDialog;
import com.ikangtai.papersdk.util.ImageUtil;
import com.ikangtai.papersdk.util.ToastUtils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {
    public static final String TAG = PaperDetailActivity.class.getSimpleName();

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
                                        Intent intent = new Intent(getContext(), PaperCameraActivity.class);
                                        startActivity(intent);
                                    }
                                })
                        .addSheetItem(getString(R.string.camera), ActionSheetDialog.SheetItemColor.Blue,
                                new ActionSheetDialog.OnSheetItemClickListener() {
                                    @Override
                                    public void onClick(int which) {
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
                // 得到图片的全路径
                Uri fileUri = data.getData();
                if (fileUri != null) {
                    final String uriStr = ImageUtil.getPathFromUri(getContext(), fileUri);
                    if (TextUtils.isEmpty(uriStr)) {
                        ToastUtils.show(getContext(), "图片错误");
                        return;
                    }
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