package com.example.dean.BottomSheet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.example.dean.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class MusicOptionsBottomSheet extends BottomSheetDialogFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.musicbottomsheetlayout, container, false);
        setupCardViews(view);
        return view;
    }
    private View.OnClickListener deleteClickListener;
    private View.OnClickListener playClickListener;
    private View.OnClickListener editClickListener;
    public void setOnDeleteClickListener(View.OnClickListener listener) {
        deleteClickListener = listener;
    }
    public void setPlayClickListener(View.OnClickListener listener){
        playClickListener = listener;
    }
    public void setOnEditClickListener(View.OnClickListener listener) {
        editClickListener = listener;
    }
    private void setupCardViews(View view) {
        CardView playMusicCardView = view.findViewById(R.id.playMusicCardView);
        if (playMusicCardView != null && playClickListener != null){
            playMusicCardView.setOnClickListener(playClickListener);
        }

        CardView editMusicCardView = view.findViewById(R.id.editMusicCardView);
        if (editMusicCardView != null && editClickListener != null){
            editMusicCardView.setOnClickListener(editClickListener);
        }

        CardView deleteCardView = view.findViewById(R.id.removeMusicCardView);
        if (deleteCardView != null && deleteClickListener != null) {
            deleteCardView.setOnClickListener(deleteClickListener);
        }
    }
}