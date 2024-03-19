package com.example.dean;

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

    public RemoveFragment(music musicToRemove) {
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
            String albumFilePath = musicToRemove.getAlbumArtBitmap();

            StorageReference fileRef = storageRef.child("audio/" + musicFileName);
            fileRef.delete().addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "Xoá nhạc thành công", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(exception -> {
                Toast.makeText(getContext(), "Có lỗi khi xoá nhạc", Toast.LENGTH_SHORT).show();
            });

            if (albumFilePath != null && !albumFilePath.isEmpty()) {
                StorageReference albumArtRef = storageRef.child(albumFilePath);
                albumArtRef.delete().addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Xoá album cover thành công", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(exception -> {
                    Toast.makeText(getContext(), "Có lỗi trong khi xoá album cover", Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    private void showSnackbar(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }
}