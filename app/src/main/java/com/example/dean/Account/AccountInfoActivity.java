package com.example.dean.Account;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.dean.MainMenuActivity;
import com.example.dean.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AccountInfoActivity extends Fragment {

    TextView tvUserNameC;
    Button btBackC;
    Spinner spinner;
    ImageButton Instruction;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_account_info, container, false);

        tvUserNameC = view.findViewById(R.id.tvUsername);
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
        bottomNavigationView.setVisibility(View.GONE);
        btBackC = view.findViewById(R.id.btBack);
        Instruction = view.findViewById(R.id.instruction);
        spinner = view.findViewById(R.id.spinner);
        Bundle arguments = getArguments();
        if (arguments != null) {
            String username = arguments.getString("username");
            tvUserNameC.setText(username);
        }

        registerForContextMenu(tvUserNameC);
        String[] choices = {"Nam", "Nữ", "X"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, choices);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);

        spinner.setSelection(2);
         Instruction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInstructionsDialog();
            }
        });
        btBackC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainMenuActivity mainMenuActivity = new MainMenuActivity();
                BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);

                bottomNavigationView.setVisibility(View.VISIBLE);
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, mainMenuActivity)
                        .addToBackStack(null)
                        .commit();

                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        // Update the userWelcome TextView in the MainMenuFragment
                        TextView userWelcomeTextView = mainMenuActivity.getView().findViewById(R.id.userWelcome);
                        if (userWelcomeTextView != null) {
                            userWelcomeTextView.setText("Xin chào, " + tvUserNameC.getText().toString().trim() + "!");
                        }
                    }
                });
            }
        });

        return view;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit:
                editItem();
                return true;
            case R.id.defaultname:
                defaultName();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void editItem() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit Text");

        final EditText editText = new EditText(requireContext());
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setText(tvUserNameC.getText().toString());
        builder.setView(editText);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newText = editText.getText().toString();
                tvUserNameC.setText(newText);
                Toast.makeText(requireContext(), "Name updated", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void defaultName() {
        tvUserNameC.setText("User");
    }
    private void showInstructionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Instructions");
        builder.setMessage("Bấm giữ tên để đổi tên! \n");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();

    }

}
