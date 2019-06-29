package com.dexter.mystyle.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dexter.mystyle.activity.consumer.ConsumerMainActivity;
import com.dexter.mystyle.models.User;
import com.dexter.seller.mystyle.R;
import com.dexter.mystyle.activity.seller.SellerMainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity";
    private static final int GOOGLE_SIGNIN_CODE = 1000;
    private Button consumerSignIn;
    private Button sellerSignIn;
    private GoogleSignInOptions googleSignInOptions;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference userReference;
    private ProgressDialog loginProgress;
    private Boolean isSellerSignIn = false;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
            userReference.document(currentUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(documentSnapshot.exists()){
                        String userName = (String) documentSnapshot.get("userName");
                        String email = (String) documentSnapshot.get("email");
                        Boolean seller = (Boolean) documentSnapshot.get("seller");

                        navigateToMainActivity(userName,email,seller);
                    }
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        captureUIElements();

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        userReference = firebaseFirestore.collection("Users");
        loginProgress = getProgressDialog("MyStyle TM","Please wait, while we sign in...",false,false,LoginActivity.this);

        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        consumerSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSellerSignIn=false;
                startActivityForResult(googleSignInClient.getSignInIntent(),GOOGLE_SIGNIN_CODE);
            }
        });

        sellerSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSellerSignIn = Boolean.TRUE;
                startActivityForResult(googleSignInClient.getSignInIntent(),GOOGLE_SIGNIN_CODE);
            }
        });
    }

    private void captureUIElements(){
        consumerSignIn = findViewById(R.id.consumerSignIn);
        sellerSignIn = findViewById(R.id.sellerSigIn);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

      if(requestCode == GOOGLE_SIGNIN_CODE){
          loginProgress.show();
          Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount signInAccount = task.getResult(ApiException.class);
                connectFireBaseWithGoogle(signInAccount);
            }catch (Exception e){
                loginProgress.dismiss();
                e.printStackTrace();
                Log.w(TAG,"Google Sign In Failed");
                Toast.makeText(getApplicationContext(),"Google Sign In Failed",Toast.LENGTH_LONG).show();
            }
        }
    }

    private void connectFireBaseWithGoogle(GoogleSignInAccount signInAccount) {

        AuthCredential credential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            addUser(user);
                        } else {
                            loginProgress.dismiss();
                            Log.w(TAG,"Google Sign In Failed");
                            Toast.makeText(getApplicationContext(),"Google Sign In Failed",Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void addUser(FirebaseUser user) {

        final User newUser = new User(user.getUid(),user.getDisplayName(),user.getEmail(),isSellerSignIn);

        userReference.document(newUser.getUserId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(!documentSnapshot.exists()){
                    userReference.document(newUser.getUserId()).set(newUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            loginProgress.dismiss();
                            Toast.makeText(getApplicationContext(),"Signed In Successfully !",Toast.LENGTH_LONG).show();
                            navigateToMainActivity(newUser.getUserName(), newUser.getEmail(), isSellerSignIn);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            loginProgress.dismiss();
                            Toast.makeText(getApplicationContext(),"Failed to add user",Toast.LENGTH_LONG).show();
                        }
                    });
                }else{
                    loginProgress.dismiss();
                    Boolean isSeller = (Boolean) documentSnapshot.get("seller");
                    Log.d("isSeller",isSeller.toString());
                    Log.d("isSellerSignIn",isSellerSignIn.toString());
                    if((isSeller && isSellerSignIn) || (!isSeller && !isSellerSignIn)) {
                        Toast.makeText(getApplicationContext(), "Signed In Successfully !", Toast.LENGTH_LONG).show();
                        navigateToMainActivity(newUser.getUserName(), newUser.getEmail(), isSeller);
                    }else if(!isSeller && isSellerSignIn){
                        signOut();
                        Toast.makeText(getApplicationContext(),"You have already registered as consumer !",Toast.LENGTH_LONG).show();
                    }else if(isSeller && !isSellerSignIn){
                        signOut();
                        Toast.makeText(getApplicationContext(),"You have already registered as seller !",Toast.LENGTH_LONG).show();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loginProgress.dismiss();
                Toast.makeText(getApplicationContext(),"Failed to get user",Toast.LENGTH_LONG).show();
            }
        });
    }

    private void signOut() {
        mAuth.signOut();
        googleSignInClient.signOut();
    }

    private void navigateToMainActivity(String userName, String email, Boolean isSeller) {
        Intent mainActivityIntent;

        if(isSeller) {
            mainActivityIntent =  new Intent(LoginActivity.this, SellerMainActivity.class);
        }else{
            mainActivityIntent =  new Intent(LoginActivity.this, ConsumerMainActivity.class);
        }
        mainActivityIntent.putExtra("email", email);
        mainActivityIntent.putExtra("username", userName);
        startActivity(mainActivityIntent);
        finish();
    }
}
