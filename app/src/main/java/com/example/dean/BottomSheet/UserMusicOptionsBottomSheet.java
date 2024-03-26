package com.example.dean.BottomSheet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.dean.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class UserMusicOptionsBottomSheet extends BottomSheetDialogFragment {

    private View.OnClickListener playClickListener;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_user_music_options_bottom_sheet, container, false);
        setupCardViews(view);
        return view;
    }
    public void setPlayClickListener(View.OnClickListener listener){
        playClickListener = listener;
    }
    private void setupCardViews(View view) {
        CardView playMusicCardView = view.findViewById(R.id.playMusicCardView);
        if (playMusicCardView != null && playClickListener != null){
            playMusicCardView.setOnClickListener(playClickListener);
        }
    }
}