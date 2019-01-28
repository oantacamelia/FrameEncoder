package com.oantacamelia.frameencoder.encoder;

import java.io.IOException;
import java.nio.ByteBuffer;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import androidx.annotation.NonNull;

public class FrameEncoderManager implements FrameEncoder {

    private MediaCodec mediaCodec;

    private MediaMuxer mediaMuxer;

    private String outputPath;

    private int outputIndex;

    public FrameEncoderManager() {
        try {
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            mediaCodec.setCallback(new EncoderCallback());
            mediaCodec.configure(createOutputMediaFormat(), null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startEncoder() {
        mediaCodec.start();
    }

    @Override
    public void stopEncoder() {
        mediaCodec.stop();
        mediaCodec.release();
    }

    /**
     * Creates the {@code MediaFormat} representing the output format for {@code MediaCodec}.
     * @return the format for encoder output.
     */
    private MediaFormat createOutputMediaFormat() {
        //TODO: refactor the magic numbers
        MediaFormat result = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 640, 480);
        result.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
        result.setInteger(MediaFormat.KEY_BIT_RATE, 1300000);
        result.setInteger(MediaFormat.KEY_FRAME_RATE, 25);
        result.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10);
        return result;
    }

    private class EncoderCallback extends MediaCodec.Callback {
        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec mediaCodec, int i) {

        }

        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec mediaCodec, int i, @NonNull MediaCodec.BufferInfo bufferInfo) {
            ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(i);
            if (bufferInfo.size != 0 && outputBuffer != null) {
                if (mediaMuxer == null) {
                    try {
                        mediaMuxer = new MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                        outputIndex = mediaMuxer.addTrack(mediaCodec.getOutputFormat());
                        mediaMuxer.start();
                    } catch (IOException e) {
                        //TODO: Handle exception
                        e.printStackTrace();
                    }
                }
                mediaMuxer.writeSampleData(outputIndex, outputBuffer, bufferInfo);
            }
            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                mediaMuxer.stop();
                mediaMuxer.release();
            }
            mediaCodec.releaseOutputBuffer(i, false);
        }

        @Override
        public void onError(@NonNull MediaCodec mediaCodec, @NonNull MediaCodec.CodecException e) {

        }

        @Override
        public void onOutputFormatChanged(@NonNull MediaCodec mediaCodec, @NonNull MediaFormat mediaFormat) {

        }
    }
}
