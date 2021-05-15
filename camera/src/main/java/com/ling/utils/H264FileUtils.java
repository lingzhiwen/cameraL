package com.ling.utils;

import android.util.Log;

import java.util.Arrays;

public class H264FileUtils {

    /**
     * 判断是否是I帧/P帧头:
     * 00 00 00 01 65    (I帧)
     * 00 00 00 01 61 / 41   (P帧)
     *
     * @param data
     * @param offset
     * @return 是否是帧头
     */
    public static boolean isHead(byte[] data, int offset) {
        boolean result = false;
        // 00 00 00 01
        if (data[offset] == 0x00
                && data[offset + 1] == 0x00
                && data[offset + 2] == 0x00
                && data[offset + 3] == 0x01
                && isVideoFrameHeadType(data[offset + 4])
        ) {
            result = true;
        }
        // 00 00 01
        if (data[offset] == 0x00
                && data[offset + 1] == 0x00
                && data[offset + 2] == 0x01
                && isVideoFrameHeadType(data[offset + 3])
        ) {
            result = true;
        }
        return result;
    }

    /**
     * I帧
     * @param data
     * @param offset
     * @return
     */
    public static boolean isIFrame(byte[] data, int offset) {
        boolean result = false;
        // 00 00 00 01
        if (data[offset] == 0x00
                && data[offset + 1] == 0x00
                && data[offset + 2] == 0x00
                && data[offset + 3] == 0x01
                && isIFrameHeadType(data[offset + 4])) {
            result = true;
        }
        // 00 00 01
        if (data[offset] == 0x00
                && data[offset + 1] == 0x00
                && data[offset + 2] == 0x01
                && isIFrameHeadType(data[offset + 3])) {
            result = true;
        }
        return result;
    }

    /**
     * I帧
     * @param head
     * @return
     */
    public static boolean isIFrameHeadType(byte head){
        return head == (byte) 0x65;
    }


    /**
     * I帧或者P帧
     */
    public static boolean isVideoFrameHeadType(byte head) {
        return head == (byte) 0x65 || head == (byte) 0x61 || head == (byte) 0x41;
    }

    /**
     * 寻找指定buffer中h264头的开始位置
     *
     * @param data   数据
     * @param offset 偏移量
     * @param max    需要检测的最大值
     * @return h264头的开始位置 ,-1表示未发现
     */
    public static int findHead(byte[] data, int offset, int max) {
        int i;
        for (i = offset; i <= max; i++) {
            //发现帧头
            if (isHead(data, i))
                break;
        }
        //检测到最大值，未发现帧头
        if (i == max) {
            i = -1;
        }
        return i;
    }

    public static void getspsAndpps(byte[] data, int length) {
        int startPos = 0;
        int pausePos = 0;
        int endPos = 0;
        for (int i = 0; i < length; i++) {
            if (data[i] == 0
                    && data[i + 1] == 0
                    && data[i + 2] == 0
                    && data[i + 3] == 1
                    && (data[i + 4] & 0x1f) == 7) {
                startPos = i;
                break;
            }
        }
        for (int i = startPos; i < length; i++) {
            if (data[i] == 0
                    && data[i + 1] == 0
                    && data[i + 2] == 0
                    && data[i + 3] == 1
                    && (data[i + 4] & 0x1f) == 8) {
                pausePos = i;
                break;
            }
        }

        for (int i = pausePos + 1; i < length; i++) {
            if (data[i] == 0
                    && data[i + 1] == 0
                    && data[i + 2] == 0
                    && data[i + 3] == 1) {
                endPos = i;
                break;
            }
            if (data[i] == 0
                    && data[i + 1] == 0
                    && data[i + 2] == 1) {
                endPos = i;
                break;
            }
        }
        Log.d("TAG", "getspsAndpps: "+startPos+"pause>>"+pausePos+"end>>"+endPos);
        byte[] sps = new byte[pausePos - startPos];
        byte[] pps = new byte[endPos - pausePos];
        System.arraycopy(data, startPos, sps, 0, (pausePos - startPos));
        System.arraycopy(data, pausePos, pps, 0, (endPos - pausePos));

        Log.e("TAG", "sps=" + Arrays.toString(sps));
        Log.e("TAG", "pps=" + Arrays.toString(pps));
    }
}
