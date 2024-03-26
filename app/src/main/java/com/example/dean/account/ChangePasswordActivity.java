package com.example.dean.account;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.dean.MainMenuActivity;
import com.example.dean.R;
import com.example.dean.Utils.DBHelper;
import com.example.dean.account.AccountActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ChangePasswordActivity extends Fragment {

    EditText edNewPassword, edConfirmNewPassword, edOldPassword;
    Button btChangePasswordC, btCancelC;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_password_activity, container, false);
        edOldPassword = view.findViewById(R.id.edOldPassword);
        edNewPassword = view.findViewById(R.id.edNewPassword);
        edConfirmNewPassword = view.findViewById(R.id.ConfirmNewPassword);
        TextView LITextView = view.findViewById(R.id.LITextView);
        if(isDarkMode())
        {
            LITextView.setTextColor(getResources().getColor(R.color.switch_text_color_dark));

        }

        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
        bottomNavigationView.setVisibility(View.GONE);

        btChangePasswordC = view.findViewById(R.id.btChangePassword);
        btCancelC = view.findViewById(R.id.btCancel);

        btChangePasswordC.setText("Change Password"); // Change the button text to indicate password change

        btChangePasswordC.setOnClickListener(view1 -> {
            String oldPassword = edOldPassword.getText().toString().trim();
            String newPassword = edNewPassword.getText().toString().trim();
            String confirmNewPassword = edConfirmNewPassword.getText().toString().trim();

            if (TextUtils.isEmpty(oldPassword)) {
                edOldPassword.setError("Please enter your old password");
                return;
            }

            if (TextUtils.isEmpty(newPassword)) {
                edNewPassword.setError("Please enter a new password");
                return;
            }

            if (!newPassword.equals(confirmNewPassword)) {
                edConfirmNewPassword.setError("Passwords do not match");
                return;
            }

            // Check if the old password matches the current password in the database
            DBHelper dbHelper = new DBHelper(requireContext());
            String savedUsername = dbHelper.getUsername(getLoggedInUsername(), oldPassword);
            if (savedUsername != null) {
                // Old password matches, proceed with changing the password
                dbHelper.updatePassword(getLoggedInUsername(), newPassword);
                dbHelper.close();
                Toast.makeText(requireContext(), "Password changed successfully", Toast.LENGTH_SHORT).show();
                AccountActivity accountActivity = new AccountActivity();
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, accountActivity)
                        .addToBackStack(null)
                        .commit();
            } else {
                // Old password doesn't match, show an error message
                Toast.makeText(requireContext(), "Incorrect old password", Toast.LENGTH_SHORT).show();
            }
        });


        btCancelC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainMenuActivity mainMenuActivity = new MainMenuActivity();
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, mainMenuActivity)
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }
    private boolean isDarkMode() {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }
    private String getLoggedInUsername() {
        // Implement this method to retrieve the currently logged-in username from SharedPreferences or any other source
        // For demonstration purposes, I'll assume you have saved the username in SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("Credentials", Context.MODE_PRIVATE);
        return sharedPreferences.getString("username", "");
    }
}
