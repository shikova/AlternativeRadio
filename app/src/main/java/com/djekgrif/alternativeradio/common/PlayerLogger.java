package com.djekgrif.alternativeradio.common;

import android.os.SystemClock;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.metadata.MetadataRenderer;
import com.google.android.exoplayer2.metadata.id3.ApicFrame;
import com.google.android.exoplayer2.metadata.id3.GeobFrame;
import com.google.android.exoplayer2.metadata.id3.Id3Frame;
import com.google.android.exoplayer2.metadata.id3.PrivFrame;
import com.google.android.exoplayer2.metadata.id3.TextInformationFrame;
import com.google.android.exoplayer2.metadata.id3.TxxxFrame;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

/**
 * Created by djek-grif on 10/20/16.
 */

public class PlayerLogger implements AudioRendererEventListener,
        ExoPlayer.EventListener, MetadataRenderer.Output<List<Id3Frame>> {

    private static final int MAX_TIMELINE_ITEM_LINES = 3;
    private static final NumberFormat TIME_FORMAT;
    static {
        TIME_FORMAT = NumberFormat.getInstance(Locale.US);
        TIME_FORMAT.setMinimumFractionDigits(2);
        TIME_FORMAT.setMaximumFractionDigits(2);
        TIME_FORMAT.setGroupingUsed(false);
    }

    private final long startTimeMs;
    private final Timeline.Window window;
    private final Timeline.Period period;

    public PlayerLogger() {
        window = new Timeline.Window();
        period = new Timeline.Period();
        startTimeMs = SystemClock.elapsedRealtime();
    }

    @Override
    public void onAudioEnabled(DecoderCounters counters) {
        Timber.d("audioEnabled [" + getSessionTimeString() + "]");
        Timber.d("inputBufferCount: %s", String.valueOf(counters.inputBufferCount));
    }

    @Override
    public void onAudioSessionId(int audioSessionId) {
        Timber.d("audioSessionId [" + audioSessionId + "]");
    }

    @Override
    public void onAudioDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {
        Timber.d("audioDecoderInitialized [" + getSessionTimeString() + ", " + decoderName + "]");
    }

    @Override
    public void onAudioInputFormatChanged(Format format) {
        Timber.d("audioFormatChanged [" + getSessionTimeString() + ", " + getFormatString(format) + "]");
    }

    @Override
    public void onAudioTrackUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {

    }

    @Override
    public void onAudioDisabled(DecoderCounters counters) {
        Timber.d("audioDisabled [" + getSessionTimeString() + "]");
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        Timber.d("loading [" + isLoading + "]");
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        String state;
        switch (playbackState) {
            case ExoPlayer.STATE_BUFFERING:
                state = "B";
                break;
            case ExoPlayer.STATE_ENDED:
                state = "E";
                break;
            case ExoPlayer.STATE_IDLE:
                state = "I";
                break;
            case ExoPlayer.STATE_READY:
                state = "R";
                break;
            default:
                state = "?";
        }
        Timber.d("state [" + getSessionTimeString() + ", " + playWhenReady + ", " + state + "]");
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
        if (timeline == null) {
            return;
        }
        int periodCount = timeline.getPeriodCount();
        int windowCount = timeline.getWindowCount();
        Timber.d("sourceInfo [periodCount=" + periodCount + ", windowCount=" + windowCount);
        for (int i = 0; i < Math.min(periodCount, MAX_TIMELINE_ITEM_LINES); i++) {
            timeline.getPeriod(i, period);
            Timber.d("  " +  "period [" + getTimeString(period.getDurationMs()) + "]");
        }
        if (periodCount > MAX_TIMELINE_ITEM_LINES) {
            Timber.d("  ...");
        }
        for (int i = 0; i < Math.min(windowCount, MAX_TIMELINE_ITEM_LINES); i++) {
            timeline.getWindow(i, window);
            Timber.d("  " +  "window [" + getTimeString(window.getDurationMs()) + ", " + window.isSeekable + ", " + window.isDynamic + "]");
        }
        if (windowCount > MAX_TIMELINE_ITEM_LINES) {
            Timber.d("  ...");
        }
        Timber.d("]");
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        Timber.e(error, "playerFailed [" + getSessionTimeString() + "]");
    }

    @Override
    public void onPositionDiscontinuity() {
        Timber.d("positionDiscontinuity");
    }


    @Override
    public void onMetadata(List<Id3Frame> metadata) {
        for (Id3Frame id3Frame : metadata) {
            if (id3Frame instanceof TxxxFrame) {
                TxxxFrame txxxFrame = (TxxxFrame) id3Frame;
                Timber.i(String.format("ID3 TimedMetadata %s: description=%s, value=%s", txxxFrame.id,
                        txxxFrame.description, txxxFrame.value));
            } else if (id3Frame instanceof PrivFrame) {
                PrivFrame privFrame = (PrivFrame) id3Frame;
                Timber.i(String.format("ID3 TimedMetadata %s: owner=%s", privFrame.id, privFrame.owner));
            } else if (id3Frame instanceof GeobFrame) {
                GeobFrame geobFrame = (GeobFrame) id3Frame;
                Timber.i(String.format("ID3 TimedMetadata %s: mimeType=%s, filename=%s, description=%s",
                        geobFrame.id, geobFrame.mimeType, geobFrame.filename, geobFrame.description));
            } else if (id3Frame instanceof ApicFrame) {
                ApicFrame apicFrame = (ApicFrame) id3Frame;
                Timber.i(String.format("ID3 TimedMetadata %s: mimeType=%s, description=%s",
                        apicFrame.id, apicFrame.mimeType, apicFrame.description));
            } else if (id3Frame instanceof TextInformationFrame) {
                TextInformationFrame textInformationFrame = (TextInformationFrame) id3Frame;
                Timber.i(String.format("ID3 TimedMetadata %s: description=%s", textInformationFrame.id,
                        textInformationFrame.description));
            } else {
                Timber.i(String.format("ID3 TimedMetadata %s", id3Frame.id));
            }
        }
    }

    private String getSessionTimeString() {
        return getTimeString(SystemClock.elapsedRealtime() - startTimeMs);
    }

    private static String getTimeString(long timeMs) {
        return timeMs == C.TIME_UNSET ? "?" : TIME_FORMAT.format((timeMs) / 1000f);
    }

    private static String getFormatString(Format format) {
        if (format == null) {
            return "null";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("id=").append(format.id).append(", mimeType=").append(format.sampleMimeType);
        if (format.bitrate != Format.NO_VALUE) {
            builder.append(", bitrate=").append(format.bitrate);
        }
        if (format.width != Format.NO_VALUE && format.height != Format.NO_VALUE) {
            builder.append(", res=").append(format.width).append("x").append(format.height);
        }
        if (format.frameRate != Format.NO_VALUE) {
            builder.append(", fps=").append(format.frameRate);
        }
        if (format.channelCount != Format.NO_VALUE) {
            builder.append(", channels=").append(format.channelCount);
        }
        if (format.sampleRate != Format.NO_VALUE) {
            builder.append(", sample_rate=").append(format.sampleRate);
        }
        if (format.language != null) {
            builder.append(", language=").append(format.language);
        }
        return builder.toString();
    }
}
