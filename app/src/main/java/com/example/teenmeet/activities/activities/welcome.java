package com.example.teenmeet.activities.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.teenmeet.R;
import com.google.firebase.auth.FirebaseAuth;

public class welcome extends AppCompatActivity {

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        auth=FirebaseAuth.getInstance();
        if (auth.getCurrentUser()!=null){
           goToNextActivity();
        }



        findViewById(R.id.btn_Getstarted).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                goToNextActivity();
            }
        });
    }
    void goToNextActivity(){
        startActivity(new Intent(welcome.this,loginActivity.class));
   finish();
    }
}