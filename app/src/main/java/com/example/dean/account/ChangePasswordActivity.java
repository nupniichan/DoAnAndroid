package com.example.dean.account;

import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.dean.MainMenuActivity;
import com.example.dean.R;
import com.example.dean.account.AccountActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ChangePasswordActivity extends Fragment {

    EditText edNewPassword, edConfirmNewPassword;
    Button btChangePasswordC, btCancelC;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_password_activity, container, false);

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
            if (validateInput()) {
                performPasswordChange();
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
    private boolean validateInput() {
        String newPassword = edNewPassword.getText().toString().trim();
        String confirmPassword = edConfirmNewPassword.getText().toString().trim();

        if (TextUtils.isEmpty(newPassword)) {
            edNewPassword.setError("New password cannot be empty");
            return false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            edConfirmNewPassword.setError("Confirm password cannot be empty");
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            // New password and Confirm password do not match
            edNewPassword.setError("New password and Confirm password do not match");
            edConfirmNewPassword.setError("New password and Confirm password do not match");
            return false;
        }

        return true;
    }

    private void performPasswordChange() {
        if (validateInput()) {
            String newPassword = edNewPassword.getText().toString().trim();

            AccountActivity accountActivity = new AccountActivity();
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, accountActivity  )
                    .addToBackStack(null)
                    .commit();
        }
    }
}
