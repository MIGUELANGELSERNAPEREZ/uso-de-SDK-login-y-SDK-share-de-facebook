package com.example.facebook;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.util.MalformedJsonException;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.MessageDialog;
import com.facebook.share.widget.ShareButton;
import com.facebook.share.widget.ShareDialog;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    CallbackManager callbackManager;
    TextView txtEmail, txtUno, txtDos;
    ImageView foto;
    ProgressDialog mDialog;
    ShareLinkContent linkContent;
    ShareButton shareButton;
    ShareDialog shareDialog;

    //@Override
    //protected void onActivityResult(int requestCode, int resultCode, int date){ }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtEmail = findViewById(R.id.txtEmail);
        txtUno = findViewById(R.id.txtUno);
        txtDos = findViewById(R.id.txtDos);
        foto = findViewById(R.id.mifoto);

        callbackManager = CallbackManager.Factory.create();

        shareDialog = new ShareDialog(this);

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile","email","user_birthday","user_friends"));
        //printKeyHash();

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
              mDialog = new ProgressDialog(MainActivity.this);
              mDialog.setMessage("Recuperando datos...");
              mDialog.show();

              String accesoToken = loginResult.getAccessToken().getToken();
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        mDialog.dismiss();
                        Log.d("response",response.toString());
                        getData(object);
                    }
                });

                Bundle parametros = new Bundle();
                parametros.putString("fields","id,email,birthday,friends");
                request.setParameters(parametros);
                request.executeAsync();
            }
            @Override
            public void onCancel() {
                Toast.makeText(MainActivity.this, "Login cancelado", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(MainActivity.this, "A ocurrido un error " + error.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        if (AccessToken.getCurrentAccessToken() != null){
            txtEmail.setText(AccessToken.getCurrentAccessToken().getUserId());
        }

        //compartir en facebook

        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse("https://developers.facebook.com"))
                .build();

        ShareButton shareButton = (ShareButton)findViewById(R.id.fb_share_button);
        shareButton.setShareContent(content);

        ShareDialog.show(this, content);

        if (ShareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentUrl(Uri.parse("http://developers.facebook.com/android"))
                    .build();
            shareDialog.show(linkContent);

            MessageDialog.show(this, content);
        }

    }


    private void getData(JSONObject object) {
        try {
            URL cargar_imagen = new URL("https://graph.facebook.com/" + object.getString("id") + "/picture?width=250&heigth=250");
            Picasso.with(this).load(cargar_imagen.toString()).into(foto);
            txtEmail.setText(object.getString("email"));
            txtUno.setText((object.getString("birthday")));
            txtDos.setText("Friends: " + object.getJSONObject("friends").getJSONObject("summary").getString("total_count"));
        }catch (MalformedURLException | JSONException e) {
            e.printStackTrace();
        }

    }


    private void printKeyHash (){
        try{
            PackageInfo info = getPackageManager().getPackageInfo("com.example.facebook",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures){
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }

        }catch (PackageManager.NameNotFoundException ex){
            ex.printStackTrace();

        }catch (NoSuchAlgorithmException ex){
            ex.printStackTrace();
        }
    }
}
