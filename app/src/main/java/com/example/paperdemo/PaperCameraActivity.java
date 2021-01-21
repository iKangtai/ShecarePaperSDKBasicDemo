package com.example.paperdemo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.paperdemo.view.ManualSmartPaperMeasureLayout;
import com.example.paperdemo.view.PaperHintDialog;
import com.example.paperdemo.view.ProgressDialog;
import com.example.paperdemo.view.SmartPaperMeasureContainerLayout;
import com.example.paperdemo.view.TopBar;
import com.ikangtai.papersdk.Config;
import com.ikangtai.papersdk.PaperAnalysiserClient;
import com.ikangtai.papersdk.PaperResultDialog;
import com.ikangtai.papersdk.event.IBaseAnalysisEvent;
import com.ikangtai.papersdk.model.PaperResult;
import com.ikangtai.papersdk.util.AiCode;
import com.ikangtai.papersdk.util.CameraUtil;
import com.ikangtai.papersdk.util.FileUtil;
import com.ikangtai.papersdk.util.LogUtils;
import com.ikangtai.papersdk.util.ToastUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 试纸拍照识别
 *
 * @author xiongyl 2021/01/21 12:31
 */
public class PaperCameraActivity extends Activity {
    private TopBar topBar;
    private TextureView textureView;
    private SmartPaperMeasureContainerLayout smartPaperMeasureContainerLayout;
    private PaperAnalysiserClient paperAnalysiserClient;
    private CameraUtil cameraUtil;
    private TextView ovulationCameraTips, flashTv;
    private ImageView shutterBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paper_camera);
        initTopBar();
        /**
         * 使用测试网络
         */
        Config.setTestServer(true);
        /**
         * 网络超时时间
         */
        Config.setNetTimeOut(30);

        /**
         * 自定义log文件有两种方式,设置一次即可
         * 1.new Config.Builder().logWriter(logWriter).
         * 2.new Config.Builder().logFilePath(logFilePath).
         */
        String logFilePath = new File(FileUtil.createRootPath(this), "log.txt").getAbsolutePath();
        BufferedWriter logWriter = null;
        try {
            logWriter = new BufferedWriter(new FileWriter(logFilePath, true), 2048);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //试纸识别sdk相关配置
        Config config = new Config.Builder().logWriter(logWriter).build();
        //初始化sdk
        paperAnalysiserClient = new PaperAnalysiserClient(this, AppConstant.appId, AppConstant.appSecret, "xyl1@qq.com", config);
        findViewById(R.id.camera_scrollview).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        initView();
        initData();
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

    private void initView() {
        textureView = findViewById(R.id.camera_textureview);
        textureView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (cameraUtil != null) {
                    cameraUtil.focusOnTouch(event);
                }
                return true;
            }
        });
        smartPaperMeasureContainerLayout = findViewById(R.id.paper_scan_content_view);
        ovulationCameraTips = findViewById(R.id.ovulationCameraTips);
        flashTv = findViewById(R.id.paper_flash_tv);
        shutterBtn = findViewById(R.id.shutterBtn);
    }

    private void initData() {
        ovulationCameraTips.setText(Html.fromHtml(String.format(getString(R.string.ovulation_camera_tips), String.format(getString(R.string.format_font_79FA1E), "10-15"))));
        flashTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraUtil == null) {
                    return;
                }
                if (cameraUtil.isOpenFlashLight()) {
                    flashTv.setText(getText(R.string.paper_open_flashlight));
                    flashTv.setTextColor(getResources().getColor(R.color.white));
                    flashTv.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.icon_lamp_close, 0, 0);
                    cameraUtil.closeFlashLight();
                } else {
                    flashTv.setText(getText(R.string.paper_close_flashlight));
                    flashTv.setTextColor(0xFFF4F400);
                    flashTv.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.icon_lamp_open, 0, 0);
                    cameraUtil.openFlashLight();
                }
            }
        });
        shutterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //手动拍照
                cameraUtil.takePicture(new CameraUtil.ICameraTakeEvent() {
                    @Override
                    public void takeBitmap(Bitmap originSquareBitmap) {
                        clipPaperDialog(originSquareBitmap);
                    }
                });
            }
        });
        smartPaperMeasureContainerLayout.setPaperViewClick(new ManualSmartPaperMeasureLayout.ViewClick() {
            @Override
            public void onClick() {
                new PaperHintDialog(PaperCameraActivity.this)
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

    }

    private void restartScan(boolean restartOpenCamera) {
        if (smartPaperMeasureContainerLayout != null) {
            smartPaperMeasureContainerLayout.showManualSmartPaperMeasure();
        }
        if (restartOpenCamera) {
            handleCamera();
        }
    }


    private void handleCamera() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (cameraUtil == null) {
                    cameraUtil = new CameraUtil();
                }
                cameraUtil.initCamera(PaperCameraActivity.this, textureView, mPreviewCallback);
            }
        }, 200);
    }

    @Override
    public void onResume() {
        super.onResume();
        handleCamera();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (cameraUtil != null) {
            cameraUtil.stopCamera();
            cameraUtil = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d("paper sdk closeSession");
        paperAnalysiserClient.closeSession();
    }

    private void clipPaperDialog(Bitmap fileBitmap) {
        final ManualSmartPaperMeasureLayout.Data data =
                smartPaperMeasureContainerLayout.getManualSmartPaperMeasuereData();
        Point upLeftPoint = new Point(data.innerLeft, data.innerTop);
        Point rightBottomPoint = new Point(data.innerRight, data.innerBottom);
        paperAnalysiserClient.analysisClipBitmapFromCamera(fileBitmap, upLeftPoint, rightBottomPoint, new IBaseAnalysisEvent() {
            @Override
            public void showProgressDialog() {
                LogUtils.d("Show Loading Dialog");
                PaperCameraActivity.this.showProgressDialog(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        paperAnalysiserClient.stopShowProgressDialog();
                    }
                });
            }

            @Override
            public void dismissProgressDialog() {
                LogUtils.d("Show Loading Dialog");
                PaperCameraActivity.this.dismissProgressDialog();
            }

            @Override
            public void cancel() {
                LogUtils.d("取消试纸结果确认");
                ToastUtils.show(PaperCameraActivity.this, AiCode.getMessage(AiCode.CODE_201));
            }

            @Override
            public void save(PaperResult paperResult) {
                LogUtils.d("保存试纸分析结果：\n" + paperResult.toString());
                if (paperResult.getErrNo() != 0) {
                    ToastUtils.show(PaperCameraActivity.this, AiCode.getMessage(paperResult.getErrNo()));
                }
                //显示试纸结果
                FileUtil.saveBitmap(paperResult.getPaperBitmap(), paperResult.getPaperId());
                paperResult.setPaperBitmap(null);
                paperResult.setNoMarginBitmap(null);
                Intent intent = new Intent(PaperCameraActivity.this, PaperDetailActivity.class);
                intent.putExtra("bean", paperResult);
                startActivityForResult(intent, 1002);
            }

            @Override
            public void saasAnalysisError(String errorResult, int code) {
                LogUtils.d("试纸分析出错 code：" + code + " errorResult:" + errorResult);
                ToastUtils.show(PaperCameraActivity.this, AiCode.getMessage(code));
            }

            @Override
            public void paperResultDialogShow(PaperResultDialog paperResultDialog) {

            }
        });


    }

    /**
     * 实时预览回调
     */
    private Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(final byte[] data, final Camera camera) {

        }
    };
    private Dialog progressDialog;

    public void showProgressDialog(View.OnClickListener onClickListener) {
        progressDialog = ProgressDialog.createLoadingDialog(this, onClickListener);
        if (progressDialog != null && !progressDialog.isShowing() && !this.isFinishing()) {
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtils.d("页面返回结果 requestCode：" + requestCode + " resultCode:" + resultCode);
        if (requestCode == 1002) {
            if (resultCode == Activity.RESULT_OK) {
                int paperValue = data.getIntExtra("paperValue", 0);
                //手动修改lhValue
                paperAnalysiserClient.updatePaperValue(paperValue);
            }
            //重新开始扫描
            restartScan(false);
        } else if (requestCode == 1003) {
            //重新开始扫描
            restartScan(false);
        }
    }
}
