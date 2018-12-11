package sample.lyon.things.pithingsbluetooth.Volume;

import android.app.Dialog;
import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import org.w3c.dom.Text;

import sample.lyon.things.pithingsbluetooth.R;

public class VolumeDialog implements OnSeekBarChangeListener, OnKeyListener {
    private Dialog dialog;
    private View view;
    private SeekBar sb_music;
    private AudioManager mAudioMgr;
    private int MUSIC = AudioManager.STREAM_MUSIC;
    private int mMaxVolume;
    private int mNowVolume;
    private TextView voluemTitle;
    String volumeStr="音量：";
    boolean isShow=false;
    public VolumeDialog(Context context ,int type) {
        MUSIC = type;
        mAudioMgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioMgr.getStreamMaxVolume(MUSIC);
        mNowVolume = mAudioMgr.getStreamVolume(MUSIC);
        view = LayoutInflater.from(context).inflate(R.layout.dialog_volume, null);
        dialog = new Dialog(context);
        sb_music = (SeekBar) view.findViewById(R.id.sb_music);
        sb_music.setOnSeekBarChangeListener(this);
        sb_music.setProgress(sb_music.getMax() * mNowVolume / mMaxVolume);
        voluemTitle = (TextView)view.findViewById(R.id.voluemTitle);
    }

    public void show() {
        if(isShow)
            return;
        dialog.getWindow().setContentView(view);
        dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        isShow=true;
        dialog.show();
        sb_music.setFocusable(true);
        sb_music.setFocusableInTouchMode(true);
        sb_music.setOnKeyListener(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                int i=0;
                while (i<2){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    i++;
                }
                isShow=false;
                dismiss();
            }
        }).start();
    }

    public void setVolume(int volume){
        sb_music.setProgress(volume* sb_music.getMax()  / mMaxVolume);
        voluemTitle.setText(volumeStr+volume+"");
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public boolean isShowing() {
        if (dialog != null) {
            return dialog.isShowing();
        } else {
            return false;
        }
    }

    public void adjustVolume(int direction, boolean fromActivity) {
        if (direction == AudioManager.ADJUST_RAISE) {
            mNowVolume += 1;
        } else {
            mNowVolume -= 1;
        }
        sb_music.setProgress(sb_music.getMax() * mNowVolume / mMaxVolume);
        mAudioMgr.adjustStreamVolume(MUSIC, direction, AudioManager.FLAG_PLAY_SOUND);
        if (mListener != null && fromActivity != true) {
            mListener.onVolumeAdjust(mNowVolume);
        }
        close();
    }

    private void close() {
        mHandler.removeCallbacks(mClose);
        mHandler.postDelayed(mClose, 2000);
    }

    private Handler mHandler = new Handler();
    private Runnable mClose = new Runnable() {
        @Override
        public void run() {
            dismiss();
        }
    };

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mNowVolume = mMaxVolume * seekBar.getProgress() / seekBar.getMax();
        mAudioMgr.setStreamVolume(MUSIC, mNowVolume, AudioManager.FLAG_PLAY_SOUND);
        if (mListener != null) {
            mListener.onVolumeAdjust(mNowVolume);
        }
        close();
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP && event.getAction() == KeyEvent.ACTION_DOWN) {
            adjustVolume(AudioManager.ADJUST_RAISE, false);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && event.getAction() == KeyEvent.ACTION_DOWN) {
            adjustVolume(AudioManager.ADJUST_LOWER, false);
            return true;
        } else {
            return false;
        }
    }

    private VolumeAdjustListener mListener;

    public void setVolumeAdjustListener(VolumeAdjustListener listener) {
        mListener = listener;
    }

    public static interface VolumeAdjustListener {
        public abstract void onVolumeAdjust(int volume);
    }

}

