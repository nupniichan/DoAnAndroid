package com.example.dean;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
                else {
                    Toast.makeText(requireContext(),"Tên đăng nhập hoặc mật khẩu sai",Toast.LENGTH_SHORT);
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
                    Intent intent = new Intent(requireContext(), AdminActivity.class);
                    startActivity(intent);
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

        DBHelper dbHelper = new DBHelper(requireContext());
        boolean isValid = dbHelper.checkUser(username, password);
        dbHelper.close();

        if (!isValid) {
            if (TextUtils.isEmpty(username)) {
                edUsernameC.setError("Tên đăng nhập không được bỏ trống");
            }
            if (TextUtils.isEmpty(password)) {
                edPasswordC.setError("Psssword không được b trống");
            }
        }

        return isValid;
    }

    private void performLogin() {
        String username = edUsernameC.getText().toString().trim();
        String password = edPasswordC.getText().toString().trim();

        DBHelper dbHelper = new DBHelper(requireContext());
        String savedUsername = dbHelper.getUsername(username, password);

        dbHelper.close();

        if (savedUsername != null) {
            boolean isAdmin = "admin".equals(savedUsername) && "admin".equals(password);
            if (isAdmin) {
                Intent intent = new Intent(requireContext(), AdminActivity.class);
                startActivity(intent);
            } else {
                MainMenuActivity mainMenuFragment = new MainMenuActivity();
                Bundle bundle = new Bundle();
                bundle.putString("username", savedUsername);
                mainMenuFragment.setArguments(bundle);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, mainMenuFragment)
                        .addToBackStack(null)
                        .commit();
            }
        } else {
            Toast.makeText(requireContext(),"Tên đăng nhập hoặc mật khẩu sai",Toast.LENGTH_SHORT);
        }
    }
}
