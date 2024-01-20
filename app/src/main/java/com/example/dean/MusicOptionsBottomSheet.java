package com.example.dean;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
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
    public void setOnDeleteClickListener(View.OnClickListener listener) {
        deleteClickListener = listener;
    }
    public void setPlayClickListener(View.OnClickListener listener){
        playClickListener = listener;
    }
    private void setupCardViews(View view) {
        CardView deleteCardView = view.findViewById(R.id.musicOptionRemove);

        if (deleteCardView != null && deleteClickListener != null) {
            deleteCardView.setOnClickListener(deleteClickListener);
        }

        // Các CardView khác tương tự
        CardView playMusicCardView = view.findViewById(R.id.playMusicCardView);
        if (playMusicCardView != null && playClickListener != null){
            playMusicCardView.setOnClickListener(playClickListener);
        }
    }
}