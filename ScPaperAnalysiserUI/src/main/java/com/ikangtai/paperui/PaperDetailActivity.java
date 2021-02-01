package com.ikangtai.paperui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ikangtai.papersdk.http.reqmodel.PaperCycleAnalysisReq;
import com.ikangtai.papersdk.model.PaperResult;
import com.ikangtai.papersdk.util.DateUtil;
import com.ikangtai.papersdk.util.FileUtil;
import com.ikangtai.paperui.view.OvulationSeekBar;
import com.ikangtai.paperui.view.TopBar;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import androidx.annotation.Nullable;

/**
 * 试纸结果
 *
 * @author xiongyl 2021/01/21 12:31
 */
public class PaperDetailActivity extends Activity implements View.OnClickListener {
    private TopBar topBar;
    /**
     * 试纸条
     */
    private ImageView paperImg;
    /**
     * 修改试纸条参考值
     */
    private TextView updatePaperResult;
    private OvulationSeekBar ovulationSeekBar;
    /**
     * 试纸结果
     */
    private TextView analysisResultTitle;
    /**
     * 试纸时间
     */
    private TextView paperTime;
    /**
     * 保存
     */
    private Button saveBtn;
    private String paperDate;
    private String paperNameId;
    private int paperResult;
    private int lhPaperAlType;
    private PaperResult paperBean;
    public static final String PIC_JPG = ".jpg";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paper_detail_layout);
        topBar = findViewById(R.id.topBar);
        ovulationSeekBar = findViewById(R.id.ovulationSeekBar);
        paperImg = findViewById(R.id.paperImg);
        analysisResultTitle = findViewById(R.id.analysisResultTitle);
        updatePaperResult = findViewById(R.id.updatePaperResult);
        paperTime = findViewById(R.id.camera_result_time);
        saveBtn = findViewById(R.id.save_btn);
        loadData();
        ArrayList<PaperCycleAnalysisReq.Paper> papers = new ArrayList<>();
        PaperCycleAnalysisReq.Paper paper = new PaperCycleAnalysisReq.Paper();
        paper.setTimestamp(DateUtil.getStringToDate(paperBean.getPaperTime()));
        paper.setValue(paperBean.getPaperValue());
        papers.add(paper);
    }

    private void loadData() {
        Serializable serializable = getIntent().getSerializableExtra("bean");
        if (serializable != null) {
            if (serializable instanceof PaperResult) {
                paperBean = (PaperResult) serializable;
                paperDate = paperBean.getPaperTime();
                paperNameId = paperBean.getPaperId();
                paperResult = (int) paperBean.getPaperValue();
                paperResult = handlePaperResult(paperResult);
                lhPaperAlType = paperBean.getPaperType();
            }
        }

        if (topBar != null) {
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

        if (ovulationSeekBar != null) {
            ovulationSeekBar.setSeekBarTitle(getString(R.string.color_reference_bar_title_1));

            ovulationSeekBar.setCallbackListener(new OvulationSeekBar.CallbackListener() {
                @Override
                public void changeResult(int result) {
                    showAnalysisResult(result);
                    result = result == 0 ? 1 : result;
                    paperResult = result;
                }
            });

        }

        if (updatePaperResult != null) {
            updatePaperResult.getPaint().setUnderlineText(true);
            updatePaperResult.setOnClickListener(this);
        }

        if (paperImg != null) {
            //显示试纸照片
            String paperName = paperNameId + PIC_JPG;
            FileUtil.initPath(PaperDetailActivity.this, "");
            String paperImgPath = FileUtil.getPlayCameraPath() + File.separator + paperName;
            File file = new File(paperImgPath);
            if (file.exists()) {
                Glide.with(PaperDetailActivity.this).load("file://" + paperImgPath).into(paperImg);
            }
        }

        if (paperTime != null) {
            paperTime.setText(paperDate);
        }

        if (saveBtn != null) {
            saveBtn.setOnClickListener(this);
        }
        showAnalysisResult(paperResult);
    }

    /**
     * 显示试纸结果
     *
     * @param paperResult
     */
    private void showAnalysisResult(int paperResult) {
        if (ovulationSeekBar != null) {
            ovulationSeekBar.setSeekBarStatus(paperResult);
        }
        if (analysisResultTitle != null) {
            String analysisResult = String.format(getString(R.string.lh_refer_result), paperResult);
            analysisResultTitle.setText(Html.fromHtml(analysisResult));
        }

    }


    private int handlePaperResult(int paperResult) {
        if (paperResult < 0) {
            paperResult = 0;
        }
        return paperResult;
    }

    @Override
    public void onClick(View v) {
        if (v == updatePaperResult) {
            v.setVisibility(View.INVISIBLE);
            ovulationSeekBar.setSeekbarEnable(true);
            ovulationSeekBar.setSeekBarTitle(getString(R.string.color_reference_bar_title_2));
            ovulationSeekBar.setSeekbarMapEnable(true);
        } else if (v == saveBtn) {
            Intent intent = new Intent();
            intent.putExtra("paperValue", paperResult);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}


