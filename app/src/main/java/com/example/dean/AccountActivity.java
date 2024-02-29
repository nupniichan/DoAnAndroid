package com.example.dean;

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
                // Handle the click event to navigate to SignUpActivity or SignUpFragment
                // You can use Intent or FragmentTransaction here
                // Example for FragmentTransaction:
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
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new MainMenuActivity());
                transaction.addToBackStack(null);
                transaction.commit();
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

        // Proceed with login logic
        // For example, navigate to the next fragment with user information
        AccountInfoActivity accountInfoActivity = new AccountInfoActivity();
        Bundle bundle = new Bundle();
        bundle.putString("username", username);
        bundle.putString("password", password);
        accountInfoActivity.setArguments(bundle);

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, accountInfoActivity)
                .addToBackStack(null)
                .commit();
    }

}
