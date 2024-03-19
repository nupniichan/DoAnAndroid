package com.example.dean;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

    public class EditFragment extends DialogFragment {

    private EditText editMusicName;
    private EditText editAuthorName;
    private Button saveButton;
    private music musicToEdit;

    private FirebaseStorage storage;
    private StorageReference storageRef;

    public static EditFragment newInstance(music music) {
        EditFragment fragment = new EditFragment();
        Bundle args = new Bundle();
        args.putSerializable("music", music);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            musicToEdit = (music) getArguments().getSerializable("music");
        }
        setEnterTransition(new Slide(Gravity.BOTTOM));
        setExitTransition(new Slide(Gravity.BOTTOM));
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit, container, false);

        editMusicName = view.findViewById(R.id.editTitle);
        editAuthorName = view.findViewById(R.id.editArtist);

        editMusicName.setHint(musicToEdit.getMusicTitle());
        editAuthorName.setHint(musicToEdit.getArtist());

        saveButton = view.findViewById(R.id.btnRemoveConfirm);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSaveButtonClicked(view);
                dismiss();
            }
        });

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        return view;
    }

    private void onSaveButtonClicked(View view) {
        String newTitle = editMusicName.getText().toString();
        String newArtist = editAuthorName.getText().toString();
        String musicFileName = musicToEdit.getFilePath();

        StorageReference fileRef = storageRef.child("audio/" + musicFileName);

        fileRef.getMetadata()
                .addOnSuccessListener(storageMetadata -> {
                    StorageMetadata updatedMetadata = new StorageMetadata.Builder()
                            .setCustomMetadata("title", newTitle.isEmpty() ? storageMetadata.getCustomMetadata("title") : newTitle)
                            .setCustomMetadata("artist", newArtist.isEmpty() ? storageMetadata.getCustomMetadata("artist") : newArtist)
                            .build();

                    fileRef.updateMetadata(updatedMetadata)
                            .addOnSuccessListener(updatedStorageMetadata -> {
                                Toast.makeText(getContext(), "Sửa thông tin nhạc thành công", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(exception -> {
                                Toast.makeText(getContext(), "Có lỗi trong quá trình cập nhật thông tin", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(exception -> {
                    Toast.makeText(getContext(), "Có lỗi trong quá trình cập nhật thông tin", Toast.LENGTH_SHORT).show();
                });
    }
}
