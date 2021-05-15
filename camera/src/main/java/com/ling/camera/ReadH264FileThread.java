package com.ling.camera;

import android.content.Context;
import android.util.Log;

import com.ling.utils.H264FileUtils;
import com.ling.video.VideoFrame;
import com.ling.video.WorkThread;

import java.io.InputStream;
import java.util.Arrays;

public class ReadH264FileThread extends Thread {
    //文件路径
    private String path;
    //文件读取完成标识
    private boolean isFinish = false;
    //这个值用于找到第一个帧头后，继续寻找第二个帧头，如果解码失败可以尝试缩小这个值
    private int FRAME_MIN_LEN = 1024;
    //一般H264帧大小不超过200k,如果解码失败可以尝试增大这个值
    private static int FRAME_MAX_LEN = 300 * 1024;
    //根据帧率获取的解码每帧需要休眠的时间,根据实际帧率进行操作
    private int PRE_FRAME_TIME = 1000 / 25;
    private Context mContext;
    private VideoViewGl videoViewGl;

    public ReadH264FileThread(Context context, VideoViewGl videoViewGl) {
        this.mContext = context;
        this.videoViewGl = videoViewGl;
    }

    @Override
    public void run() {
        super.run();
        InputStream is;
        try {
            is = mContext.getResources().getAssets().open("test1.h264");
            //保存完整数据帧
            byte[] frame = new byte[FRAME_MAX_LEN];
            //当前帧长度
            int frameLen = 0;
            //每次从文件读取的数据
            byte[] readData = new byte[10 * 1024];
            //开始时间
            long startTime = System.currentTimeMillis();
            short i = 0;
            //循环读取数据
            while (!isFinish) {
                if (is.available() > 0) {
                    int readLen = is.read(readData);
                    //当前长度小于最大值
                    if (frameLen + readLen < FRAME_MAX_LEN) {
                        //将readData拷贝到frame
                        System.arraycopy(readData, 0, frame, frameLen, readLen);
                        //修改frameLen
                        frameLen += readLen;
                        if(frameLen>0){
                            // H264FileUtils.getspsAndpps(frame,frameLen);
                        }
                        // Log.d("TAG", "run111: "+frameLen+ "<<>>" + Arrays.toString(frame) +">>"+frameLen);
                        //寻找第一个帧头
                        int headFirstIndex = H264FileUtils.findHead(frame, 0, frameLen);
                        //Log.d("TAG", "run1112222: "+headFirstIndex+"<< >>"+H264FileUtils.isHead(frame, headFirstIndex));
                        while (headFirstIndex >= 0 && H264FileUtils.isHead(frame, headFirstIndex)) {
                            //Log.d("TAG", "run2222: "+Arrays.toString(frame));
                            //寻找第二个帧头
                            int headSecondIndex = H264FileUtils.findHead(frame, headFirstIndex + FRAME_MIN_LEN, frameLen);
                            //如果第二个帧头存在，则两个帧头之间的就是一帧完整的数据
                            if (headSecondIndex > 0 && H264FileUtils.isHead(frame, headSecondIndex)) {
//                                    Log.e("ReadH264FileThread", "headSecondIndex:" + headSecondIndex);
                                //视频解码

                                byte[] frameData = Arrays.copyOfRange(frame, headFirstIndex, headSecondIndex-headFirstIndex);
                                Log.d("TAG", "run333: "+Arrays.toString(frameData));
                                VideoFrame videoFrame = new VideoFrame(
                                        frameData,
                                        i++,
                                        frameData.length,
                                        1080,
                                        1920,
                                        System.currentTimeMillis(),
                                        H264FileUtils.isIFrame(frameData,headFirstIndex));
                                // Log.d("TAG","isIFrame:"+videoFrame.isIFrame+"headSecondIndex:"+headSecondIndex);
                                this.videoViewGl.drawVideoFrame(videoFrame);
                                //onFrame(frame, headFirstIndex, headSecondIndex - headFirstIndex);
                                //截取headSecondIndex之后到frame的有效数据,并放到frame最前面
                                byte[] temp = Arrays.copyOfRange(frame, headSecondIndex, frameLen);
                                System.arraycopy(temp, 0, frame, 0, temp.length);
                                //修改frameLen的值
                                frameLen = temp.length;

                                //线程休眠
                                sleepThread(startTime, System.currentTimeMillis());
                                //重置开始时间
                                startTime = System.currentTimeMillis();
                                //继续寻找数据帧
                                headFirstIndex = H264FileUtils.findHead(frame, 0, frameLen);
                                Log.d("TAG","isIFrame2222"+videoFrame.isIFrame+"headFirstIndex:"+headFirstIndex);
                            } else {
                                //找不到第二个帧头1664 -93, -58,     1070
                                headFirstIndex = -1;
                            }
                        }
                    } else {
                        //如果长度超过最大值，frameLen置0
                        frameLen = 0;
                    }
                } else {
                    //文件读取结束
                    isFinish = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //修眠
    private void sleepThread(long startTime, long endTime) {
        //根据读文件和解码耗时，计算需要休眠的时间
        long time = PRE_FRAME_TIME - (endTime - startTime);
        if (time > 0) {
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
