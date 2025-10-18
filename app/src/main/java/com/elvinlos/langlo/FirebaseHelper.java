package com.elvinlos.langlo;

import android.app.Activity;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.ClearCredentialException;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.concurrent.Executors;

public class FirebaseHelper {

    private static final String TAG = "FirebaseHelper";

    private final FirebaseAuth auth;
    private final CredentialManager credentialManager;
    private final Activity activity;

    public interface SignInListener {
        void onSignInSuccess();
    }

    public FirebaseHelper(Activity activity) {
        this.activity = activity;
        this.auth = FirebaseAuth.getInstance();
        this.credentialManager = CredentialManager.create(activity.getBaseContext());
    }

    /**
     * Launch Google Sign-In flow using Credential Manager.
     */
    public void launchSignIn(SignInListener listener) {
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(activity.getString(R.string.default_web_client_id))
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                activity,
                request,
                new CancellationSignal(),
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleCredential(result.getCredential(), listener);
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        Log.e(TAG, "Sign-in failed: " + e.getLocalizedMessage());
                    }
                }
        );
    }

    /**
     * Handles the returned Google credential.
     */
    private void handleCredential(Credential credential, SignInListener listener) {
        if (credential instanceof CustomCredential customCredential
                && credential.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
            Bundle data = customCredential.getData();
            GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(data);
            firebaseAuthWithGoogle(googleIdTokenCredential.getIdToken(), listener);
        } else {
            Log.w(TAG, "Unexpected credential type");
        }
    }

    /**
     * Authenticate Firebase with Google ID token.
     */
    private void firebaseAuthWithGoogle(String idToken, SignInListener listener) {
        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(firebaseCredential)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        Log.d(TAG, "signInWithCredential:success");
                        assert user != null;
                        Toast.makeText(activity, "Welcome, " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                        listener.onSignInSuccess();
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(activity, "Sign-in failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Check current signed-in user.
     */
    public FirebaseUser checkSignIn() {
        return auth.getCurrentUser();
    }

    /**
     * Sign out user and clear credentials.
     */
    public void signOut() {
        auth.signOut();
        ClearCredentialStateRequest clearRequest = new ClearCredentialStateRequest();
        credentialManager.clearCredentialStateAsync(
                clearRequest,
                new CancellationSignal(),
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<>() {
                    @Override
                    public void onResult(@NonNull Void result) {
                        Toast.makeText(activity, "Signed out", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(@NonNull ClearCredentialException e) {
                        Log.e(TAG, "Couldn't clear credentials: " + e.getLocalizedMessage());
                    }
                });
    }
}
