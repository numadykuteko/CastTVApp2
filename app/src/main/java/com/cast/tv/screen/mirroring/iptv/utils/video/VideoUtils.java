package com.cast.tv.screen.mirroring.iptv.utils.video;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;

import androidx.annotation.NonNull;

import com.cast.tv.screen.mirroring.iptv.constants.DataConstants;
import com.cast.tv.screen.mirroring.iptv.utils.file.FileUtils;
import com.otaliastudios.transcoder.Transcoder;
import com.otaliastudios.transcoder.TranscoderListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class VideoUtils {
    @TargetApi(21)
    public static VideoProfile getVideoEncodingProfile(final String videoFilePath) {
        File inputFile = new File(videoFilePath);
        if (!inputFile.canRead()) {
            return null;
        }

        MediaExtractor mediaExtractor = new MediaExtractor();
        // Initialize MediaExtractor and configure/extract video information
        try {
            mediaExtractor.setDataSource(inputFile.toString());
        } catch (IOException e) {
            return null;
        }

        MediaFormat videoMediaFormat = findVideoMediaFormat(mediaExtractor);

        VideoProfile videoProfile = null;

        // MediaCodecInfo.CodecProfileLevel of the video track
        if (videoMediaFormat != null && videoMediaFormat.containsKey(MediaFormat.KEY_PROFILE)) {
            videoProfile = new VideoProfile();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                videoProfile.setLevel(videoMediaFormat.getInteger(MediaFormat.KEY_LEVEL));
            }
            videoProfile.setMimeType(videoMediaFormat.getString(MediaFormat.KEY_MIME));
            videoProfile.setProfileAac(videoMediaFormat.getString(MediaFormat.KEY_AAC_PROFILE));
            videoProfile.setProfileCode(videoMediaFormat.getInteger(MediaFormat.KEY_PROFILE));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    videoProfile.setColorRange(videoMediaFormat.getInteger(MediaFormat.KEY_COLOR_RANGE));
                } catch (Exception ignored) {}
                try {
                    videoProfile.setColorStandard(videoMediaFormat.getInteger(MediaFormat.KEY_COLOR_STANDARD));
                } catch (Exception ignored) {}
                try {
                    videoProfile.setColorStandard(videoMediaFormat.getInteger(MediaFormat.KEY_COLOR_TRANSFER));
                } catch (Exception ignored) {}
            }

        }

        mediaExtractor.release();
        mediaExtractor = null;

        return videoProfile;
    }

    public static boolean isAACVideo(String videoPath) {
        VideoProfile videoCodec = VideoUtils.getVideoEncodingProfile(videoPath);

        if (videoCodec != null) {
            List<Integer> AAC_LIST = Arrays.asList(
                    MediaCodecInfo.CodecProfileLevel.AACObjectELD,
                    MediaCodecInfo.CodecProfileLevel.AACObjectERLC,
                    MediaCodecInfo.CodecProfileLevel.AACObjectELD,
                    MediaCodecInfo.CodecProfileLevel.AACObjectHE,
                    MediaCodecInfo.CodecProfileLevel.AACObjectHE_PS,
                    MediaCodecInfo.CodecProfileLevel.AACObjectLD,
                    MediaCodecInfo.CodecProfileLevel.AACObjectScalable,
                    MediaCodecInfo.CodecProfileLevel.AACObjectLTP);
            return AAC_LIST.contains(videoCodec.getProfileCode());
        }

        return false;
    }

    /**
     * Find video MediaFormat from MediaExtractor.
     *
     * @param mediaExtractor The MediaExtractor which is used to find video track.
     * @return MediaFormat for video track, or {@code null} when video track is not found.
     */
    private static MediaFormat findVideoMediaFormat(final MediaExtractor mediaExtractor) {
        MediaFormat videoTrackMediaFormat = null;
        int totalTracks = mediaExtractor.getTrackCount();
        for (int i = 0; i < totalTracks; i++) {
            MediaFormat trackFormat = mediaExtractor.getTrackFormat(i);
            if ((trackFormat.containsKey(MediaFormat.KEY_MIME)
                    && trackFormat.getString(MediaFormat.KEY_MIME).contains("video"))
                    || (trackFormat.containsKey(MediaFormat.KEY_WIDTH) && trackFormat.containsKey(MediaFormat.KEY_HEIGHT))
            ) {
                videoTrackMediaFormat = trackFormat;
                break;
            }
        }
        return videoTrackMediaFormat;
    }

    public static void encodeVideo(Context context, String filePath, DecodeVideoListener decodeVideoListener) {
        File outputFile;
        try {
            File outputDir = new File(context.getExternalFilesDir(null), DataConstants.TRANSCODE_VIDEO_FOLDER_NAME);
            outputDir.mkdir();
            outputFile = File.createTempFile(DataConstants.TRANSCODE_VIDEO_PREFIX_NAME, ".mp4", outputDir);
        } catch (IOException e) {
            return;
        }

        Transcoder.into(outputFile.getAbsolutePath())
                .addDataSource(filePath)
                .setListener(new TranscoderListener() {
                    public void onTranscodeProgress(double progress) {
                           decodeVideoListener.onProgress(progress);
                    }
                    public void onTranscodeCompleted(int successCode) {
                        decodeVideoListener.onSuccess(outputFile.getAbsolutePath());
                    }
                    public void onTranscodeCanceled() {
                        decodeVideoListener.onError();
                    }
                    public void onTranscodeFailed(@NonNull Throwable exception) {
                        decodeVideoListener.onError();
                    }
                }).transcode();
    }

    public static void deleteVideoTempFolder(Context context) {
        try {
            File outputDir = new File(context.getExternalFilesDir(null), DataConstants.TRANSCODE_VIDEO_FOLDER_NAME);
            if (outputDir.isDirectory()) {
                if (outputDir.listFiles() != null) {
                    for (File ct : outputDir.listFiles()) {
                        FileUtils.deleteFileOnExist(ct.getAbsolutePath());

                    }
                }
            }
        } catch (Exception e) {
        }
    }

    public interface DecodeVideoListener {
        void onProgress(double progress);
        void onError();
        void onSuccess(String outputPath);
    }
}
