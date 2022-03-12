package com.example.teenmeet.activities.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.teenmeet.R;
import com.example.teenmeet.activities.models.InterfaceJava;
import com.example.teenmeet.activities.models.User;
import com.example.teenmeet.databinding.ActivityCallactuivityBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

public class Callactuivity extends AppCompatActivity {
    ActivityCallactuivityBinding binding;
    String uniqueId = "";
    FirebaseAuth auth;
    String username = "";
    String friendUsername = "";
    boolean isPeerConnected = false;

    DatabaseReference firebaseRef;
    boolean isAudio = true;
    boolean isVideo = true;
    String createdBy = "";
    boolean pageExit = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallactuivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();
        firebaseRef = FirebaseDatabase.getInstance().getReference("users");
        username = getIntent().getStringExtra("username");
        String incoming = getIntent().getStringExtra("incoming");
        createdBy = getIntent().getStringExtra("createdBy");
        friendUsername = incoming;
        setupWebView();
        binding.micbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAudio = !isAudio;
                callJavaScriptFunction("javascript:toggleAudio(\"" + isAudio + "\")");
                if (isAudio) {
                    binding.micbtn.setImageResource(R.drawable.btn_unmute_normal);

                } else {
                    binding.micbtn.setImageResource(R.drawable.btn_mute_normal);
                }
            }
        });
        binding.btnend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        binding.videobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isVideo = !isVideo;
                callJavaScriptFunction("javascript:toggleVideo(\"" + isVideo + "\")");
                if (isVideo) {
                    binding.videobtn.setImageResource(R.drawable.btn_video_normal);

                } else {
                    binding.videobtn.setImageResource(R.drawable.btn_video_muted);
                }
            }
        });
    }

    void setupWebView() {
        binding.webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    request.grant(request.getResources());
                }
            }
        });
        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.getSettings().setAllowFileAccess(true);
        binding.webView.getSettings().setAllowFileAccessFromFileURLs(true);
        binding.webView.getSettings().setMediaPlaybackRequiresUserGesture(false);

        binding.webView.addJavascriptInterface(new InterfaceJava(this), "Android");
        loadVideoCall();
    }

    public void loadVideoCall() {

        String filePath = "file:android_asset/call.html";
        binding.webView.loadUrl(filePath);
        binding.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                initializePeer();
            }
        });
    }

    void initializePeer() {
        uniqueId = getUniqueId();
        callJavaScriptFunction("javascript:init(\"" + uniqueId + "\")");
        if (createdBy.equalsIgnoreCase(username)) {
            if (pageExit)
                return;
            firebaseRef.child(username).child("connID").setValue(uniqueId);
            firebaseRef.child(username).child("isAvailable").setValue(true);
//            binding.loodingCntrols.setVisibility(View.GONE);
            binding.controls.setVisibility(View.VISIBLE);
            FirebaseDatabase.getInstance().getReference()
                    .child("profiles")
                    .child(friendUsername)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);
                            Glide.with(Callactuivity.this).load(user.getProfile())
                                    .into(binding.profile);
                            binding.profilename.setText(user.getName());
                            binding.profilecity.setText(user.getCity());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
        else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    friendUsername = createdBy;
                    FirebaseDatabase.getInstance().getReference()
                            .child("profiles")
                            .child(friendUsername)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    User user = snapshot.getValue(User.class);
                                    Glide.with(Callactuivity.this).load(user.getProfile())
                                            .into(binding.profile);
                                    binding.profilename.setText(user.getName());
                                    binding.profilecity.setText(user.getCity());
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                    FirebaseDatabase.getInstance().getReference().child("users")
                            .child(friendUsername).child("connId")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.getValue() != null) {
                                        sendCallRequest();
//                                    if (snapshot.getValue() != null) {
//                                        isPeerConnected = true;
//                                        sendCallRequest();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            }, 10000);
        }

    }

    public void onPeerConnected() {
        isPeerConnected  = true;
    }
    void sendCallRequest(){
        if(!isPeerConnected) {
            Toast.makeText(this, "You are not connected. Please check your internet.", Toast.LENGTH_SHORT).show();
            return;
        }

        listenConnId();
    }

//    void sendCallRequest() {
//        if (!isPeerconnected) {
//            Toast.makeText(this, "you are not connect. Please check your internet connection.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        listenConnId();
//    }

    void listenConnId() {
                firebaseRef.child(friendUsername).child("connId").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() == null)
                    return;
//                binding.loodingCntrols.setVisibility(View.GONE);
                binding.controls.setVisibility(View.VISIBLE);
                String connId = snapshot.getValue(String.class);
                callJavaScriptFunction("javascript:startCall(\""+connId+"\")");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    void callJavaScriptFunction(String function){
        binding.webView.post(new Runnable() {
            @Override
            public void run() {
                binding.webView.evaluateJavascript(function, null);
            }
        });

    }

    String getUniqueId(){
        return UUID.randomUUID().toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pageExit = true;
        firebaseRef.child(createdBy).setValue(null);
        finish();
    }
}