package com.example.dean;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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

import com.example.dean.Account.AccountActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SignInActivity extends Fragment {

    EditText edUserNameC, edPasswordC, edConfirmPassword;
    Button btSignInC, btCancelC;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_sign_in, container, false);

        edUserNameC = view.findViewById(R.id.edUsername);
        edPasswordC = view.findViewById(R.id.edPassword);
        edConfirmPassword = view.findViewById(R.id.ConfirmPassword);

        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
        bottomNavigationView.setVisibility(View.GONE);

        btSignInC = view.findViewById(R.id.btSignIn);
        btCancelC = view.findViewById(R.id.btCancel);

        Bundle arguments = getArguments();
        if (arguments != null) {
            String username = arguments.getString("username");
            edUserNameC.setText(username);
        }

        registerForContextMenu(edUserNameC);

        btSignInC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (validateInput()) {

                    performSignIn();
                }
            }
        });

        btCancelC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AccountActivity accountActivity = new AccountActivity();
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, accountActivity)
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }

    private boolean validateInput() {
        String username = edUserNameC.getText().toString().trim();
        String password = edPasswordC.getText().toString().trim();
        String confirmPassword = edConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            edUserNameC.setError("Username cannot be empty");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            edPasswordC.setError("Password cannot be empty");
            return false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            edConfirmPassword.setError("Confirm Password cannot be empty");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            // Password and Confirm Password do not match
            edPasswordC.setError("Passwords do not match");
            edConfirmPassword.setError("Passwords do not match");
            return false;
        }

        return true;
    }

    private void performSignIn() {
        saveCredentialsToSharedPreferences();
        AccountActivity accountActivity = new AccountActivity();
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, accountActivity)
                .addToBackStack(null)
                .commit();
//nếu muốn để sẵn edUsername sau khi SignIn xong
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                TextView edUsernameChange = accountActivity.getView().findViewById(R.id.edUserName);
                edUsernameChange.setText(edUserNameC.getText().toString().trim());

            }
        });
    }
    private void saveCredentialsToSharedPreferences() {
        String username = edUserNameC.getText().toString().trim();
        String password = edPasswordC.getText().toString().trim();

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("Credentials", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.apply();

}}
