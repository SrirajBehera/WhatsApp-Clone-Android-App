package com.example.whatsappclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.whatsappclone.Adapters.FragmentsAdapter;
import com.example.whatsappclone.Adapters.UsersAdapter;
import com.example.whatsappclone.Fragments.ChatsFragment;
import com.example.whatsappclone.Models.Users;
import com.example.whatsappclone.databinding.ActivityMainBinding;
import com.example.whatsappclone.databinding.FragmentChatsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    FirebaseAuth auth;

    ArrayList<Users> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        binding.viewPager.setAdapter(new FragmentsAdapter(getSupportFragmentManager()));
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        if (auth.getCurrentUser() == null){
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);
            finishAffinity();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentId = FirebaseAuth.getInstance().getUid();
        FirebaseDatabase.getInstance().getReference().child("presence").child(currentId).setValue("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (auth.getCurrentUser() != null) {
            String currentId = FirebaseAuth.getInstance().getUid();
            FirebaseDatabase.getInstance().getReference().child("presence").child(currentId).setValue("Offline");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.search:
                SearchView searchView = (SearchView) item.getActionView();
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
//                        UsersAdapter adapter = new UsersAdapter(list, ChatsFragment.this);
//                        adapter.getFilter().filter(newText);
                        return false;
                    }
                });
                break;

            case R.id.settings:
                Intent intent2 = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent2);
                break;

            case R.id.groupChat:
                Intent intent1 = new Intent(MainActivity.this, GroupChatActivity.class);
                startActivity(intent1);
                break;

            case R.id.logout:
                auth.signOut();
                Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                startActivity(intent);
                finishAffinity();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}