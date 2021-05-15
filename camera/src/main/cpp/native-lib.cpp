#include <jni.h>
#include <string>
#include "Log.h"
#include <GLES/gl.h>
#include <GLES/glext.h>
//#include <GLES2/gl2.h>
//#include <EGL/egl.h>
//#include <android/native_window_jni.h>


extern "C"{
    #include <libavcodec/avcodec.h>
    //封装格式处理
    #include <libavformat/avformat.h>
    //像素处理
    #include <libswscale/swscale.h>
}

int _IS_REGISTER_ALL = 0;
bool is_inited = false;
AVCodec *h264_codec = NULL;
AVCodecContext *h264_codec_ctx = NULL;
int width = 0;
int height = 0;

extern "C"
JNIEXPORT void JNICALL
Java_com_decoder_ling_H264Decoder_nativeInit(JNIEnv *env, jclass clazz) {
    // TODO: implement nativeInit()
    LOGE("进来了...");
    AVFormatContext *avFormatContext = avformat_alloc_context();
    LOGE("出去了...");
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_decoder_ling_H264Decoder_init(JNIEnv *env, jclass thiz) {
    // TODO: implement init()
    LOGE("进来了...Java_com_decoder_ling_H264Decoder_init");
    AVFormatContext *avFormatContext = avformat_alloc_context();
    if (is_inited) return 1;

    if (!_IS_REGISTER_ALL) {
        av_register_all();
        _IS_REGISTER_ALL = 1;
    }

    h264_codec = avcodec_find_decoder(AV_CODEC_ID_H264);
    if (!h264_codec) {
        return -1;
    }
    h264_codec_ctx = avcodec_alloc_context3(h264_codec);
    if (!h264_codec_ctx) {
        return -2;
    }

    h264_codec_ctx->time_base.num = 1;
    h264_codec_ctx->frame_number = 1;
    h264_codec_ctx->codec_type = AVMEDIA_TYPE_VIDEO;
    h264_codec_ctx->bit_rate = 0;
     h264_codec_ctx->pix_fmt = AV_PIX_FMT_YUVJ420P;
    // h264_codec_ctx->time_base.den = den; //帧率
//    h264_codec_ctx->pix_fmt = AV_PIX_FMT_YUVJ420P;
//    h264_codec_ctx->color_range = AVCOL_RANGE_MPEG;
    LOGE("进来了2222...Java_com_decoder_ling_H264Decoder_init i = %d",h264_codec_ctx->pix_fmt);
    if (avcodec_open2(h264_codec_ctx, h264_codec, 0) == 0) {
        LOGE("进来了3333...Java_com_decoder_ling_H264Decoder_init");
        is_inited = true;
        return 0;
    }
    return -3;
    LOGE("出去了...Java_com_decoder_ling_H264Decoder_init");
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_decoder_ling_H264Decoder_decode(JNIEnv *env, jobject thiz, jbyteArray data, jint num_bytes,
                                         jlong packet_pts) {
    LOGE("进来了...Java_com_decoder_ling_H264Decoder_decode");
    int ret;
    jbyte *h264buff = env->GetByteArrayElements(data, 0);
    AVPacket packet = {0};
    packet.data = (uint8_t *)h264buff;
    packet.size = env->GetArrayLength(data);
    packet.pts = packet_pts;
    // packet.stream_index = num_bytes;
    ret = avcodec_send_packet(h264_codec_ctx, &packet);
    return ret;

}extern "C"
JNIEXPORT void JNICALL
Java_com_decoder_ling_H264Decoder_release(JNIEnv *env, jobject thiz) {
    if (!is_inited) return;
    avcodec_close(h264_codec_ctx);
    is_inited = false;
}extern "C"
JNIEXPORT jboolean JNICALL
Java_com_decoder_ling_H264Decoder_decodeBuffer(JNIEnv *env, jobject thiz, jobject data,
                                               jint num_bytes, jlong packet_pts) {
    // TODO: implement decodeBuffer()
}extern "C"
JNIEXPORT jint JNICALL
Java_com_decoder_ling_H264Decoder_toTexture(JNIEnv *env, jobject thiz, jint texture_y,
                                            jint texture_u, jint texture_v) {
    AVFrame *yuv_frame = av_frame_alloc();
    LOGE("进来了...Java_com_decoder_ling_H264Decoder_toTexture i = %d", avcodec_receive_frame(h264_codec_ctx, yuv_frame));
    if (avcodec_receive_frame(h264_codec_ctx, yuv_frame) == 0) {
        height = h264_codec_ctx->height;
        width = h264_codec_ctx->width;
        LOGE("进来了2222...Java_com_decoder_ling_H264Decoder_toTexture 0 = %d",yuv_frame->format);
        LOGE("进来了2222...Java_com_decoder_ling_H264Decoder_toTexture 1 = %d",yuv_frame->linesize[1]);
        LOGE("进来了2222...Java_com_decoder_ling_H264Decoder_toTexture 2 = %d",yuv_frame->linesize[2]);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture_y);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, yuv_frame->linesize[0], yuv_frame->height,0, GL_LUMINANCE, GL_UNSIGNED_BYTE, yuv_frame->data[0]);

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, texture_u);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE,  yuv_frame->linesize[1], yuv_frame->height/2,0, GL_LUMINANCE, GL_UNSIGNED_BYTE, yuv_frame->data[1]);

        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, texture_v);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE,  yuv_frame->linesize[2], yuv_frame->height/2,0, GL_LUMINANCE, GL_UNSIGNED_BYTE, yuv_frame->data[2]);


        /***
        * 纹理更新完成后开始绘制
        ***/
        // glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

        // glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

        // eglSwapBuffers(eglDisp, eglWindow);

        av_frame_free(&yuv_frame);

        return 1;
    }

    LOGE("出去了...Java_com_decoder_ling_H264Decoder_toTexture");
    //av_frame_free(&yuv_frame);
    return -1;
}extern "C"
JNIEXPORT jint JNICALL
Java_com_decoder_ling_H264Decoder_toYUV(JNIEnv *env, jobject thiz, jbyteArray data) {
    // TODO: implement toYUV()
}extern "C"
JNIEXPORT jint JNICALL
Java_com_decoder_ling_H264Decoder_toBitmap(JNIEnv *env, jobject thiz, jobject bitmap) {
    // TODO: implement toBitmap()
}extern "C"
JNIEXPORT jint JNICALL
Java_com_decoder_ling_H264Decoder_getHeight(JNIEnv *env, jobject thiz) {
    return height;
}extern "C"
JNIEXPORT jint JNICALL
Java_com_decoder_ling_H264Decoder_getWidth(JNIEnv *env, jobject thiz) {
    return width;
}