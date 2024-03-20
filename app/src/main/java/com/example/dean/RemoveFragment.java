package com.example.dean;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
public class RemoveFragment extends DialogFragment {

    private music musicToRemove;

    Button confirmButton, cancelButton;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    public RemoveFragment(music musicToRemove ) {
        this.musicToRemove = musicToRemove;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_remove, container, false);

        confirmButton = view.findViewById(R.id.btnRemoveConfirm);
        cancelButton = view.findViewById(R.id.btnRemoveCancel);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeMusic(view);
                dismiss();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        return view;
    }

    private void removeMusic(View view) {
        if (musicToRemove != null) {
            String musicFileName = musicToRemove.getFilePath();
            String albumArtFileName = musicToRemove.getAlbumArtName();

            Context context = requireContext();

            if (context != null) {
                StorageReference fileRef = storageRef.child("audio/" + musicFileName);
                fileRef.delete().addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Xoá nhạc thành công", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(exception -> {
                    Toast.makeText(context, "Có lỗi khi xoá nhạc", Toast.LENGTH_SHORT).show();
                });

                if (albumArtFileName != null && !albumArtFileName.isEmpty()) {
                    StorageReference albumArtRef = storageRef.child("images/" + albumArtFileName);
                    albumArtRef.delete().addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Xoá album cover thành công", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(exception -> {
                        Toast.makeText(context, "Có lỗi trong khi xoá album cover", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }
    }
}