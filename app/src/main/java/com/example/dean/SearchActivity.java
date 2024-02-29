package com.example.dean;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SearchActivity extends Fragment {

    private View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_search, container, false);

        setupViews();

        return rootView;
    }

    private void setupViews() {
        Button buttonSearch = rootView.findViewById(R.id.buttonSearch);
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonSearch.setText("Searching...");

                simulateSearch();
            }
        });

    }

    private void simulateSearch() {
        rootView.postDelayed(new Runnable() {
            @Override
            public void run() {
                Button buttonSearch = rootView.findViewById(R.id.buttonSearch);
                buttonSearch.setText("Search");

            }
        }, 2000);
    }
}
