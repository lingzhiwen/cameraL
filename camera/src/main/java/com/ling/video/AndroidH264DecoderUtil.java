package com.ling.video;

import android.annotation.TargetApi;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.os.Build.VERSION;
import android.util.Log;

public class AndroidH264DecoderUtil {
    public static final String TAG = "AndroidH264DecoderUtil";
    public static final String AVC_MIME_TYPE = "video/avc";
    public static final int COLOR_QCOM_FORMATYUV420PackedSemiPlanar32m = 2141391876;
    public static final int[] supportedColorList = new int[]{19, 2130706688, 21, 2141391872, 2141391876};
    static String[] blacklisted_decoders = new String[]{"omx.google", "avcdecoder", "avcdecoder_flash", "flvdecoder", "m2vdecoder", "m4vh263decoder", "rvdecoder", "vc1decoder", "vpxdecoder", "omx.nvidia.mp4.decode", "omx.nvidia.h263.decode", "omx.mtk.video.decoder.mpeg4", "omx.mtk.video.decoder.h263"};
    static String[] blacklisted_decoders_Lollipop = new String[]{"omx.google", "avcdecoder", "avcdecoder_flash", "flvdecoder", "m2vdecoder", "m4vh263decoder", "rvdecoder", "vc1decoder", "vpxdecoder"};

    public AndroidH264DecoderUtil() {
    }

    @TargetApi(16)
    public static AndroidH264DecoderUtil.DecoderProperties findAVCDecoder() {
        if (VERSION.SDK_INT < 16) {
            return null;
        } else {
            int supportedColorFormat;
            int codecColorFormat;
            if (VERSION.SDK_INT < 21) {
                for(int i = 0; i < MediaCodecList.getCodecCount(); ++i) {
                    MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
                    if (!info.isEncoder()) {
                        String codecName = info.getName().toLowerCase();
                        if (!isInBlack(codecName)) {
                            try {
                                CodecCapabilities capabilities = info.getCapabilitiesForType("video/avc");
                                int[] var4 = supportedColorList;
                                int var5 = var4.length;

                                for(int var6 = 0; var6 < var5; ++var6) {
                                    supportedColorFormat = var4[var6];
                                    int[] var8 = capabilities.colorFormats;
                                    codecColorFormat = var8.length;

                                    for(int var10 = 0; var10 < codecColorFormat; ++var10) {
                                        codecColorFormat = var8[var10];
                                        if (codecColorFormat == supportedColorFormat) {
                                            Log.d("AndroidH264DecoderUtil", "Found target decoder " + info.getName() + ". Color: 0x" + Integer.toHexString(codecColorFormat));
                                            return new AndroidH264DecoderUtil.DecoderProperties(info.getName(), codecColorFormat);
                                        }
                                    }
                                }
                            } catch (Exception var13) {
                                Log.d("AndroidH264DecoderUtil", "IllegalArgumentException" + var13.toString());
                            }
                        }
                    }
                }
            }

            if (VERSION.SDK_INT >= 21) {
                MediaCodecList list = new MediaCodecList(1);
                MediaCodecInfo[] codecInfos = list.getCodecInfos();

                for(int i = 0; i < codecInfos.length; ++i) {
                    MediaCodecInfo info = codecInfos[i];
                    if (!info.isEncoder()) {
                        String codecName = info.getName().toLowerCase();
                        if (!isInBlackLollipop(codecName)) {
                            try {
                                CodecCapabilities capabilities = info.getCapabilitiesForType("video/avc");
                                int[] var20 = capabilities.colorFormats;
                                supportedColorFormat = var20.length;

                                for(int var21 = 0; var21 < supportedColorFormat; ++var21) {
                                    codecColorFormat = var20[var21];
                                    if (codecColorFormat == 2135033992) {
                                        Log.d("AndroidH264DecoderUtil", "Found target decoder " + info.getName() + ". Color: 0x" + Integer.toHexString(codecColorFormat));
                                        return new AndroidH264DecoderUtil.DecoderProperties(info.getName(), codecColorFormat);
                                    }
                                }
                            } catch (Exception var12) {
                                Log.d("AndroidH264DecoderUtil", "IllegalArgumentException" + var12.toString());
                            }
                        }
                    }
                }
            }

            return null;
        }
    }

    private static boolean isInBlack(String codecName) {
        String[] var1 = blacklisted_decoders;
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            String blackCodec = var1[var3];
            if (codecName.startsWith(blackCodec)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isInBlackLollipop(String codecName) {
        String[] var1 = blacklisted_decoders_Lollipop;
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            String blackCodec = var1[var3];
            if (codecName.startsWith(blackCodec)) {
                return true;
            }
        }

        return false;
    }

    public static class DecoderProperties {
        public final String codecName;
        public final int colorFormat;

        public DecoderProperties(String codecName, int colorFormat) {
            this.codecName = codecName;
            this.colorFormat = colorFormat;
        }
    }
}
