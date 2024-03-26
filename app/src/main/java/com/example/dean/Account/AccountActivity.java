package com.example.dean.Account;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.dean.MainMenuActivity;
import com.example.dean.R;
import com.example.dean.SignInActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AccountActivity extends Fragment {

    EditText edUsernameC, edPasswordC;
    Button btnLoginC, btnCancelC;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_account, container, false);
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
        bottomNavigationView.setVisibility(View.GONE);
        // Ánh xạ
        edUsernameC = view.findViewById(R.id.edUserName);
        edPasswordC = view.findViewById(R.id.edPassword);
        btnLoginC = view.findViewById(R.id.btLogin);
        TextView tvSignUp = view.findViewById(R.id.tvSignUp);

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new SignInActivity());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        btnLoginC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateInput()) {
                    performLogin();
                }
            }
        });

        btnCancelC = view.findViewById(R.id.btCancel);
        btnCancelC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
                bottomNavigationView.setVisibility(View.VISIBLE);

                // Kiểm tra quyền truy cập và chuyển hướng tương ứng
                SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("Credentials", Context.MODE_PRIVATE);
                boolean isAdmin = sharedPreferences.getBoolean("isAdmin", false);

                if (isAdmin) {
                    AdminMainMenuFragment managerFragment = new AdminMainMenuFragment();
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, managerFragment)
                            .addToBackStack(null)
                            .commit();
                } else {
                    // Nếu là user thường, chuyển hướng đến MainMenuFragment
                    MainMenuActivity mainMenuFragment = new MainMenuActivity();
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, mainMenuFragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        setHasOptionsMenu(true);

        return view;
    }

    private boolean validateInput() {
        String username = edUsernameC.getText().toString().trim();
        String password = edPasswordC.getText().toString().trim();
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("Credentials", Context.MODE_PRIVATE);
        String savedUsername = sharedPreferences.getString("username", "");
        String savedPassword = sharedPreferences.getString("password", "");

        boolean isValid = true;


        if (TextUtils.isEmpty(username) || !username.equals(savedUsername)) {
            edUsernameC.setError("Invalid username");
            isValid = false;
        }

        if (TextUtils.isEmpty(password) || !password.equals(savedPassword)) {
            edPasswordC.setError("Invalid password");
            isValid = false;
        }

        return isValid;
    }

    private void performLogin() {
        String username = edUsernameC.getText().toString().trim();
        String password = edPasswordC.getText().toString().trim();

        boolean isAdmin = "admin".equals(username) && "admin".equals(password);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("Credentials", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.putBoolean("isAdmin", isAdmin);
        editor.apply();

        if (isAdmin) {
            AdminMainMenuFragment adminMenuFragment = new AdminMainMenuFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, adminMenuFragment)
                    .addToBackStack(null)
                    .commit();
        } else {
            // Nếu là user thường, chuyển hướng đến MainMenuFragment
            MainMenuActivity mainMenuFragment = new MainMenuActivity();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, mainMenuFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}
