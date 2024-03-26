package com.example.dean.account;

import android.content.Context;
import android.content.Intent;
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

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.dean.admin.AdminActivity;
import com.example.dean.Utils.DBHelper;
import com.example.dean.MainMenuActivity;
import com.example.dean.R;
import com.example.dean.Utils.Utils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AccountActivity extends Fragment {

    EditText edUsernameC, edPasswordC;
    Button btnLoginC, btnCancelC;

    TextView LITextView, tvSignUp;    private boolean isLoggedIn = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_account, container, false);
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
        bottomNavigationView.setVisibility(View.GONE);
        // Ánh xạ
        edUsernameC = view.findViewById(R.id.edUserName);
        edPasswordC = view.findViewById(R.id.edPassword);
        btnLoginC = view.findViewById(R.id.btLogin);
        tvSignUp = view.findViewById(R.id.tvSignUp);
        LITextView = view.findViewById(R.id.LITextView);
        if(isDarkMode())
        {
            LITextView.setTextColor(getResources().getColor(R.color.switch_text_color_dark));
            tvSignUp.setTextColor(getResources().getColor(R.color.switch_text_color_dark));
        }
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
        btnCancelC.setOnClickListener(view1 -> {
            BottomNavigationView bottomNavigationView1 = requireActivity().findViewById(R.id.bottom_navigation);
            bottomNavigationView1.setVisibility(View.VISIBLE);

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
        });

        setHasOptionsMenu(true);

        return view;
    }
    private boolean isDarkMode() {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
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
                edPasswordC.setError("Password không được bỏ trống");
            }
            if (!username.equals(edUsernameC) || !password.equals(edPasswordC)) {
                edUsernameC.setError("Tên đăng nhập hoặc mật khẩu sai");
                edPasswordC.setError("Tên đăng nhập hoặc mật khẩu sai");
            }
        }

        return isValid;
    }


    private void performLogin() {
        String username = edUsernameC.getText().toString().trim();
        String password = edPasswordC.getText().toString().trim();

        DBHelper dbHelper = new DBHelper(requireContext());
        dbHelper.setLoggedInUsername(username);
        String savedUsername = dbHelper.getUsername(username, password);
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
        bottomNavigationView.setVisibility(View.VISIBLE);
        dbHelper.close();

        // Đánh dấu là đã đăng nhập
        Utils.setLoggedIn(true);
        Utils.setUsername(username);
        if (savedUsername != null) {
            boolean isAdmin = "admin".equals(savedUsername) && "admin".equals(password);
            if (isAdmin) {
                Intent intent = new Intent(requireContext(), AdminActivity.class);
                startActivity(intent);
            } else {
                // Chuyển hướng sang MainMenuActivity và truyền giá trị isLoggedIn và savedUsername qua bundle
                MainMenuActivity mainMenuFragment = new MainMenuActivity();
                Bundle bundle = new Bundle();
                bundle.putBoolean("isLoggedIn", Utils.isLoggedIn());
                bundle.putString("username", savedUsername);
                mainMenuFragment.setArguments(bundle);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, mainMenuFragment)
                        .addToBackStack(null)
                        .commit();
            }
        } else {
            Toast.makeText(requireContext(), "Tên đăng nhập hoặc mật khẩu sai", Toast.LENGTH_SHORT).show();
        }
    }
}
