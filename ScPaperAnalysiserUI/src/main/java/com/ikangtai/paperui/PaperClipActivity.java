package com.ikangtai.paperui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.ikangtai.papersdk.Config;
import com.ikangtai.papersdk.PaperAnalysiserClient;
import com.ikangtai.papersdk.PaperResultDialog;
import com.ikangtai.papersdk.event.IBaseAnalysisEvent;
import com.ikangtai.papersdk.model.PaperResult;
import com.ikangtai.papersdk.util.AiCode;
import com.ikangtai.papersdk.util.FileUtil;
import com.ikangtai.papersdk.util.ImageUtil;
import com.ikangtai.papersdk.util.LogUtils;
import com.ikangtai.papersdk.util.PxDxUtil;
import com.ikangtai.papersdk.util.ToastUtils;
import com.ikangtai.paperui.view.ManualSmartPaperMeasureLayout;
import com.ikangtai.paperui.view.PaperHintDialog;
import com.ikangtai.paperui.view.ProgressDialog;
import com.ikangtai.paperui.view.TopBar;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 试纸裁剪识别
 *
 * @author xiongyl 2021/01/21 12:31
 */
public class PaperClipActivity extends Activity implements View.OnTouchListener {
    private ImageView srcPic;
    private ManualSmartPaperMeasureLayout measureLayout;
    private TopBar topBar;

    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();

    private float oldRotation = 0;

    /**
     * 动作标志：无
     */
    private static final int NONE = 0;
    /**
     * 动作标志：拖动
     */
    private static final int DRAG = 1;
    /**
     * 动作标志：缩放
     */
    private static final int ZOOM = 2;
    /**
     * 初始化动作标志
     */
    private int mode = NONE;

    /**
     * 记录起始坐标
     */
    private PointF start = new PointF();
    /**
     * 记录缩放时两指中间点坐标
     */
    private PointF mid = new PointF();
    private float oldDist = 1f;

    private Bitmap bitmap;

    private ImageView mImageView;
    private String uriStr;
    private PaperAnalysiserClient paperAnalysiserClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * 自定义log文件有两种方式,设置一次即可
         * 默认/data/android/package/documents/log.txt
         * 1.new Config.Builder().logWriter(logWriter).
         * 2.new Config.Builder().logFilePath(logFilePath).
         */
        String logFilePath = new File(FileUtil.createRootPath(this), AppConstant.logFileName).getAbsolutePath();
        BufferedWriter logWriter = null;
        try {
            logWriter = new BufferedWriter(new FileWriter(logFilePath, true), 2048);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //试纸识别sdk相关配置
        Config config = new Config.Builder().logWriter(logWriter).build();
        //初始化sdk
        paperAnalysiserClient = new PaperAnalysiserClient(this, AppConstant.appId, AppConstant.appSecret, AppConstant.unionId,config);
        setContentView(R.layout.activity_paper_clip_picture);
        srcPic = this.findViewById(R.id.src_pic);
        mImageView = srcPic;
        measureLayout = findViewById(R.id.paper_clip_measureLayout);
        srcPic.setOnTouchListener(this);
        ViewTreeObserver observer = srcPic.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                srcPic.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                initClipView();
            }
        });

        initTopBar();
        measureLayout.setViewClick(new ManualSmartPaperMeasureLayout.ViewClick() {
            @Override
            public void onClick() {
                new PaperHintDialog(PaperClipActivity.this)
                        .builder()
                        .title(getString(R.string.warm_prompt))
                        .buttonText(getString(R.string.i_know))
                        .initEvent(new PaperHintDialog.IEvent() {
                            @Override
                            public void clickButton() {

                            }
                        })
                        .show();
            }
        });
        findViewById(R.id.paper_clip_save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mImageView == null) {
                    return;
                }
                Bitmap clipBitmap = getBitmap();
                ManualSmartPaperMeasureLayout.Data data = measureLayout.getData();
                int x = data.innerLeft;
                int y = data.innerTop + measureLayout.getTop() - PxDxUtil.dip2px(PaperClipActivity.this, 50);
                int width = data.innerWidth;
                int height = data.innerHeight;
                Point upLeftPoint = new Point(x, y);
                Point rightBottomPoint = new Point(x + width, y + height);

                paperAnalysiserClient.analysisClipBitmapFromPhoto(clipBitmap, upLeftPoint, rightBottomPoint, new IBaseAnalysisEvent() {
                    @Override
                    public void showProgressDialog() {
                        LogUtils.d("Show Loading Dialog");
                        PaperClipActivity.this.showProgressDialog(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                paperAnalysiserClient.stopShowProgressDialog();
                            }
                        });
                    }

                    @Override
                    public void dismissProgressDialog() {
                        LogUtils.d("Hide Loading Dialog");
                        PaperClipActivity.this.dismissProgressDialog();
                    }

                    @Override
                    public void cancel() {
                        LogUtils.d("取消试纸结果确认");
                    }

                    @Override
                    public void save(PaperResult paperResult) {
                        LogUtils.d("保存试纸分析结果：\n" + paperResult.toString());
                        if (paperResult.getErrNo() != 0) {
                            ToastUtils.show(PaperClipActivity.this, AiCode.getMessage(paperResult.getErrNo()));
                        }
                        FileUtil.saveBitmap(paperResult.getPaperBitmap(), paperResult.getPaperId());
                        paperResult.setNoMarginBitmap(null);
                        paperResult.setPaperBitmap(null);
                        Intent intent = new Intent(PaperClipActivity.this, PaperDetailActivity.class);
                        intent.putExtra("bean", paperResult);
                        startActivityForResult(intent, 2001);
                    }

                    @Override
                    public void saasAnalysisError(String errorResult, int code) {
                        LogUtils.d("试纸分析出错 code：" + code + " errorResult:" + errorResult);
                    }

                    @Override
                    public void paperResultDialogShow(PaperResultDialog paperResultDialog) {

                    }
                });
            }
        });
    }

    private Dialog progressDialog;

    public void showProgressDialog(View.OnClickListener onClickListener) {
        progressDialog = ProgressDialog.createLoadingDialog(this, onClickListener);
        if (progressDialog != null && !progressDialog.isShowing() && !isFinishing()) {
            progressDialog.setCancelable(true);
            progressDialog.show();
        }
    }

    public void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    private void initTopBar() {
        topBar = findViewById(R.id.topBar);
        topBar.setOnTopBarClickListener(new TopBar.OnTopBarClickListener() {
            @Override
            public void leftClick() {
                finish();
            }

            @Override
            public void midLeftClick() {

            }

            @Override
            public void midRightClick() {

            }

            @Override
            public void rightClick() {

            }
        });

    }

    /**
     * 初始化截图区域，并将源图按裁剪框比例缩放
     */
    private void initClipView() {
        Intent intent = getIntent();
        uriStr = intent.getStringExtra("paperUri");
        File file = ImageUtil.getFileFromUril(uriStr);
        bitmap = ImageUtil.getBitmapByFile(file);

        measureLayout.post(new Runnable() {
            @Override
            public void run() {
                int clipHeight = srcPic.getHeight();
                int clipWidth = srcPic.getWidth();

                int imageWidth = bitmap.getWidth();
                int imageHeight = bitmap.getHeight();
                // 按裁剪框求缩放比例
                float scale = (clipWidth * 1.0f) / imageWidth;
                // 起始中心点
                float imageMidX = imageWidth * scale / 2;
                float imageMidY = imageHeight * scale / 2;
                srcPic.setScaleType(ImageView.ScaleType.MATRIX);

                // 缩放
                matrix.postScale(scale, scale);
                // 平移
                matrix.postTranslate(0, clipHeight / 2 - imageMidY);

                srcPic.setImageMatrix(matrix);
                srcPic.setImageBitmap(bitmap);
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mImageView = (ImageView) v;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                // 设置开始点位置
                start.set(event.getX(), event.getY());
                mode = DRAG;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                oldRotation = rotation(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x, event.getY()
                            - start.y);
                } else if (mode == ZOOM) {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float rotation = rotation(event) - oldRotation;
                        float scale = newDist / oldDist;
                        // 缩放
                        matrix.postScale(scale, scale, mid.x, mid.y);
                        // 旋转
                        matrix.postRotate(rotation, mid.x, mid.y);
                    }
                }
                break;
        }
        mImageView.setImageMatrix(matrix);
        return true;
    }

    /**
     * 多点触控时，计算最先放下的两指距离
     *
     * @param event
     * @return
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 多点触控时，计算最先放下的两指中心坐标
     *
     * @param point
     * @param event
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }


    /**
     * 获取裁剪框内截图
     *
     * @return
     */
    private Bitmap getBitmap() {

        mImageView.setDrawingCacheEnabled(true);
        mImageView.buildDrawingCache();
        Bitmap bitmap = mImageView.getDrawingCache();
        return bitmap;
    }

    /**
     * 取旋转角度
     *
     * @param event
     * @return
     */
    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.d("paper sdk closeSession");
        paperAnalysiserClient.closeSession();
        // 释放资源
        mImageView.destroyDrawingCache();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2001) {
            if (resultCode == Activity.RESULT_OK) {
                int paperValue = data.getIntExtra("paperValue", 0);
                //结果页修改result后，需要同步给SDK,有助于识别优化
                paperAnalysiserClient.updatePaperValue(paperValue);
            }
            finish();
        }
    }
}
