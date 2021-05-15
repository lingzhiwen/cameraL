用ffmpeg提取H264裸流
裸流提取指令说明：

命令行：
ffmpeg -i 800_600.mp4 -codec copy -bsf: h264_mp4toannexb -f h264 800_600.264
说明：
 -i 800_600.mp4：是输入的MP4文件
-codec copy：从MP4封装中进行拷贝
-bsf: h264_mp4toannexb：从MP4拷贝到annexB封装
-f h264：采用h.264格式
800_600.264：输出的文件名称


报错：
Codec 'hevc' (173) is not supported by the bitstream filter 'h264_mp4toannexb'. Supported codecs are: h264 (27)
Error initializing bitstream filter: h264_mp4toannexb

解决办法：
h264_mp4toannexb => hevc_mp4toannexb





