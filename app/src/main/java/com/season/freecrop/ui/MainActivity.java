package com.season.freecrop.ui;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.season.freecrop.R;
import com.season.freecrop.view.CropView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    View resultContainerView;
    List<Integer> mData;
    CropView cropView;
    RecyclerView mRv;
    ImageCropAdapter mAdapter;
    ImageView preView, proView, backView, completeView, resultView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resultContainerView = findViewById(R.id.result);
        resultView = (ImageView) findViewById(R.id.iv_result);
        resultContainerView.setVisibility(View.GONE);

        preView = (ImageView) findViewById(R.id.iv_pre);
        proView = (ImageView) findViewById(R.id.iv_pro);
        mRv= (RecyclerView) findViewById(R.id.rv);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRv.setLayoutManager(layoutManager);
        mData = getCropListBig();
        mAdapter =new ImageCropAdapter(this, getCropList(), getCropListSel()){
            @Override
            public void onItemClick(int position) {
                super.onItemClick(position);
                addResView(position);
            }
        };
        mRv.setAdapter(mAdapter);

        backView = (ImageView) findViewById(R.id.iv_close);
        backView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cropView.canBack()){
                    finish();
                }else{
                    cropView.clearTool();
                    resetStatus();
                    mAdapter.clearPosition();
                }
            }
        });

        completeView = (ImageView) findViewById(R.id.iv_crop);
        completeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //调用该方法得到剪裁好的图片
                if (!cropView.canComplete()) {
                    return;
                }

                File cropFile =  new File(getCacheDir(), "result.png");
                String path = cropView.getCropImage(cropFile);
                if (TextUtils.isEmpty(path)) {

                } else {
                    resultContainerView.setVisibility(View.VISIBLE);
                    resultView.setImageBitmap(BitmapFactory.decodeFile(cropFile.getAbsolutePath()));
                }

            }
        });
        cropView = (CropView) findViewById(R.id.mask_view);

        boolean res = false;
        try {
            res = cropView.setBitmap(BitmapFactory.decodeStream(getAssets().open("original.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!res){
           // ToastUtil.show("文件找不到了");
            finish();
        }

        preView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropView.undo();
                resetStatus();
            }
        });
        proView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                cropView.redo();
                resetStatus();
            }
        });
        resetStatus();
        cropView.setOnActionListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetStatus();
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (resultContainerView.getVisibility() == View.VISIBLE){
            resultContainerView.setVisibility(View.GONE);
            return;
        }
        super.onBackPressed();
    }

    private void resetStatus(){
        preView.setImageResource(cropView.canPre()? R.mipmap.chexiao1: R.mipmap.chexiao2);
        proView.setImageResource(cropView.canPro()? R.mipmap.chongzuo1: R.mipmap.chongzuo2);
        completeView.setImageResource(cropView.canComplete()? R.drawable.selector_crop_finish: R.mipmap.crop_wancheng2);
        backView.setImageResource(cropView.canBack()? R.drawable.selector_crop_back: R.drawable.selector_crop_cancel);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cropView.release();
    }

    public void addResView(int choosePosition) {
        if (choosePosition==0){
            //多点边框裁剪
            cropView.startPathCrop();
        }else if (choosePosition==1){
            //路径裁剪
            cropView.startPathFreeCrop();
        }else {
            //图片裁剪
            cropView.startImageCrop(mData.get(choosePosition));
        }
        resetStatus();
    }


    /***
     * 裁剪列表图
     * @return
     */
    public static List<Integer> getCropListBig()
    {
        List<Integer> list = new ArrayList<>();
        list.add(R.mipmap.dian);
        list.add(R.mipmap.shoudong);
        list.add(R.mipmap.crop_1);
        list.add(R.mipmap.crop_2);
        list.add(R.mipmap.crop_3);
        list.add(R.mipmap.crop_4);
        list.add(R.mipmap.crop_5);
        list.add(R.mipmap.crop_6);
        list.add(R.mipmap.crop_7);
        list.add(R.mipmap.crop_8);
        list.add(R.mipmap.crop_9);
        list.add(R.mipmap.crop_10);
        list.add(R.mipmap.crop_11);
        list.add(R.mipmap.crop_12);
        list.add(R.mipmap.crop_13);
        list.add(R.mipmap.crop_14);
        list.add(R.mipmap.crop_15);
        list.add(R.mipmap.crop_16);
        list.add(R.mipmap.crop_17);
        list.add(R.mipmap.crop_18);
        list.add(R.mipmap.crop_19);
        list.add(R.mipmap.crop_20);
        list.add(R.mipmap.crop_21);
        list.add(R.mipmap.crop_22);
        list.add(R.mipmap.crop_23);
        list.add(R.mipmap.crop_24);
        list.add(R.mipmap.crop_25);
        return list;
    }


    /***
     * 裁剪列表图
     * @return
     */
    public static List<Integer> getCropList()
    {
        List<Integer> list = new ArrayList<>();
        list.add(R.mipmap.dian);
        list.add(R.mipmap.shoudong);
        list.add(R.mipmap.fangxing);
        list.add(R.mipmap.yuanjiao);
        list.add(R.mipmap.yuan);
        list.add(R.mipmap.sanjiaoxing);
        list.add(R.mipmap.lingxing);
        list.add(R.mipmap.liaan82);
        list.add(R.mipmap.huabian);
        list.add(R.mipmap.zuanshi);
        list.add(R.mipmap.wojiaoxing);
        list.add(R.mipmap.duojiao);
        list.add(R.mipmap.tixing);
        list.add(R.mipmap.qipaokuang);
        list.add(R.mipmap.duihuakuang);
        list.add(R.mipmap.yun);
        list.add(R.mipmap.hua);
        list.add(R.mipmap.lian1);
        list.add(R.mipmap.lian2);
        list.add(R.mipmap.lian3);
        list.add(R.mipmap.lian4);
        list.add(R.mipmap.lian5);
        list.add(R.mipmap.lian6);
        list.add(R.mipmap.lian7);
        list.add(R.mipmap.lian8);
        list.add(R.mipmap.lian9);
        list.add(R.mipmap.lian10);
        return list;
    }

    /***
     * 裁剪列表图
     * @return
     */
    public static List<Integer> getCropListSel()
    {
        List<Integer> list = new ArrayList<>();
        list.add(R.mipmap.dian2);
        list.add(R.mipmap.shoudong2);
        list.add(R.mipmap.fangxin2);
        list.add(R.mipmap.yunajiao2);
        list.add(R.mipmap.yuan2);
        list.add(R.mipmap.sanjiao2);
        list.add(R.mipmap.lingxing2);
        list.add(R.mipmap.lian2222);
        list.add(R.mipmap.huabian2);
        list.add(R.mipmap.zusnhi2);
        list.add(R.mipmap.wujiaoxing2);
        list.add(R.mipmap.duojiao2);
        list.add(R.mipmap.tixing2);
        list.add(R.mipmap.qipaokuang2);
        list.add(R.mipmap.duihuakuang2);
        list.add(R.mipmap.yun2);
        list.add(R.mipmap.hua2);
        list.add(R.mipmap.lian12);
        list.add(R.mipmap.lian22);
        list.add(R.mipmap.lain32);
        list.add(R.mipmap.lain42);
        list.add(R.mipmap.lian52);
        list.add(R.mipmap.lian62);
        list.add(R.mipmap.lian72);
        list.add(R.mipmap.lian82);
        list.add(R.mipmap.lian92);
        list.add(R.mipmap.lian102);
        return list;
    }

}
