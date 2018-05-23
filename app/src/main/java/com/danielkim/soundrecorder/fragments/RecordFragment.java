package com.danielkim.soundrecorder.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.Toast;

import com.danielkim.soundrecorder.CustomDialogSetName;
import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.RecordingItem;
import com.danielkim.soundrecorder.RecordingService;
import com.danielkim.soundrecorder.activities.ListRecordActivity;
import com.melnykov.fab.FloatingActionButton;

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link RecordFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecordFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String LOG_TAG = RecordFragment.class.getSimpleName();

    private int position;

    //Recording controls
    private FloatingActionButton mRecordButton = null;
    private Button mPauseButton = null;

    private int mRecordPromptCount = 0;
    private ImageView imgPlay;
    private ImageView imgStop;

    private boolean mStartRecording = true;
    private boolean mPauseRecording = true;
    ImageView imgListRecord;
    private Chronometer mChronometer = null;
    long timeWhenPaused = 0; //stores time when user clicks pause button
    Intent intent;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment Record_Fragment.
     */
    public static RecordFragment newInstance() {
        RecordFragment f = new RecordFragment();
        return f;
    }

    public RecordFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View recordView = inflater.inflate(R.layout.fragment_record, container, false);

        mChronometer = (Chronometer) recordView.findViewById(R.id.chronometer);
        imgStop = (ImageView) recordView.findViewById(R.id.img_stop);
        imgPlay = (ImageView) recordView.findViewById(R.id.img_play);
        //update recording prompt text
        // mRecordingPrompt = (TextView) recordView.findViewById(R.id.recording_status_text);
        imgListRecord = (ImageView) recordView.findViewById(R.id.img_list_record);
        mRecordButton = (FloatingActionButton) recordView.findViewById(R.id.btnRecord);
        mRecordButton.setColorNormal(getResources().getColor(R.color.primary));
        mRecordButton.setColorPressed(getResources().getColor(R.color.primary_dark));
        imgPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mStartRecording) {
                    onRecord();
                    mStartRecording = false;
                    mPauseRecording = false;
                }
                onPauseRecord(mPauseRecording);


            }
        });

        imgListRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ListRecordActivity.start(getActivity());
            }
        });

        imgStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stop();

                imgStop.setVisibility(View.GONE);
                imgListRecord.setVisibility(View.VISIBLE);
            }
        });

        //  mPauseButton = (Button) recordView.findViewById(R.id.btnPause);
//        mPauseButton.setVisibility(View.GONE); //hide pause button before recording starts
//        mPauseButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onPauseRecord(mPauseRecording);
//                mPauseRecording = !mPauseRecording;
//            }
//        });

        return recordView;
    }

    // Recording Start/Stop
    //TODO: recording pause
    private void onRecord() {

        imgStop.setVisibility(View.VISIBLE);
        imgListRecord.setVisibility(View.GONE);
        intent = new Intent(getActivity(), RecordingService.class);
        // start recording
        imgPlay.setImageResource(R.drawable.ic_record_pause);
        //mPauseButton.setVisibility(View.VISIBLE);
        Toast.makeText(getActivity(), R.string.toast_recording_start, Toast.LENGTH_SHORT).show();
        File folder = new File(Environment.getExternalStorageDirectory() + "/SoundRecorder");
        if (!folder.exists()) {
            //folder /SoundRecorder doesn't exist, create the folder
            folder.mkdir();
        }

        //start Chronometer
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();
        mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                if (mRecordPromptCount == 0) {
                } else if (mRecordPromptCount == 1) {
                } else if (mRecordPromptCount == 2) {
                    mRecordPromptCount = -1;
                }

                mRecordPromptCount++;
            }
        });

        //start RecordingService
        getActivity().startService(intent);
        //keep screen on while recording
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mRecordPromptCount++;


    }

    private void stop() {

        mChronometer.stop();
        mChronometer.setBase(SystemClock.elapsedRealtime());
        timeWhenPaused = 0;
        getActivity().stopService(intent);
        imgPlay.setImageResource(R.drawable.ic_record_play);
        //allow the screen to turn off again once recording is finished
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mStartRecording = true;
        RecordingService.setOnSaveListener(new RecordingService.OnSaveListener() {
            @Override
            public void onSaveListener(RecordingItem item) {
                CustomDialogSetName dialog = new CustomDialogSetName();
                dialog.show(getActivity().getFragmentManager(), LOG_TAG);
                dialog.setItem(item);
            }
        });

    }

    //TODO: implement pause recording
    private void onPauseRecord(boolean pause) {
        if (pause) {
            imgPlay.setImageResource(R.drawable.ic_record_start);
            //pause recording
            timeWhenPaused = mChronometer.getBase() - SystemClock.elapsedRealtime();
            mChronometer.stop();
            mPauseRecording = false;
        } else {
            imgPlay.setImageResource(R.drawable.ic_record_pause);
            //resume recording
            mChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenPaused);
            mChronometer.start();
            mPauseRecording = true;
        }
    }


}