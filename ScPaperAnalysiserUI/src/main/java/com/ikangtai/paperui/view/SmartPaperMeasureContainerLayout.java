package com.ikangtai.paperui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.ikangtai.paperui.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 试纸拍照View
 *
 * @author xiongyl 2019/11/6 21:22
 */
public class SmartPaperMeasureContainerLayout extends FrameLayout {

    private ManualSmartPaperMeasureLayout manualSmartPaperMeasureLayout;

    public SmartPaperMeasureContainerLayout(@NonNull Context context) {
        super(context);
    }

    public SmartPaperMeasureContainerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SmartPaperMeasureContainerLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec, widthMeasureSpec);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        manualSmartPaperMeasureLayout = findViewById(R.id.paper_manual_smart_paper_measureLayout);
    }

    /**
     * 显示手动拍照试纸
     */
    public void showManualSmartPaperMeasure() {

        if (manualSmartPaperMeasureLayout != null) {
            manualSmartPaperMeasureLayout.setVisibility(VISIBLE);
            manualSmartPaperMeasureLayout.scanPaperCoordinatesData(null);
        }

    }

    /**
     * 手动测量范围 Data
     *
     * @return
     */
    public ManualSmartPaperMeasureLayout.Data getManualSmartPaperMeasuereData() {
        if (manualSmartPaperMeasureLayout != null) {
            return manualSmartPaperMeasureLayout.getData();
        }
        return null;
    }

    /**
     * 显示手动拍照试纸结果
     */
    public void showManualSmartPaperMeasure(Bitmap originSquareBitmap) {

        if (manualSmartPaperMeasureLayout != null) {
            manualSmartPaperMeasureLayout.setVisibility(VISIBLE);
            manualSmartPaperMeasureLayout.scanPaperCoordinatesData(originSquareBitmap);
        }
    }

    public void clearImageData() {

        if (manualSmartPaperMeasureLayout != null) {
            manualSmartPaperMeasureLayout.scanPaperCoordinatesData(null);
        }
    }

    public void setPaperViewClick(ManualSmartPaperMeasureLayout.ViewClick viewClick) {
        if (manualSmartPaperMeasureLayout != null) {
            manualSmartPaperMeasureLayout.setViewClick(viewClick);
        }
    }

}

