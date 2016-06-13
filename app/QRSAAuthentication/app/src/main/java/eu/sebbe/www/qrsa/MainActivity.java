package eu.sebbe.www.qrsa;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import javax.crypto.Cipher;
import android.util.Base64;
import android.content.Context;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.security.keystore.KeyInfo;
import android.widget.TextView;
import android.view.View;
import android.os.Process;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialogbox);
        setFinishOnTouchOutside(false);
        String b64encoded;
        String PublicKey;
        String Plaintext;
        String toclipboard;
        String todialog;
        String action;
        boolean actionfound;
        Intent intent = getIntent();
        Uri data = intent.getData();
        b64encoded = data.getEncodedSchemeSpecificPart().replaceAll("[^abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-]","");
        if (b64encoded != null) {
            action = b64encoded.substring(0,1);
            b64encoded = b64encoded.substring(1);
            actionfound = false;
            if (action.equals("c")) {
            actionfound = true;
              Plaintext = DecryptData(Base64.decode(b64encoded,Base64.URL_SAFE));
                if (Plaintext.equals("")) {
                    ShowDialog("Authentication failed. Either you have not done the enrollment step, or this link is not intended for you.");
                }
                else {
                    String[] str_array = Plaintext.split("::");
                    if (str_array.length != 4) {
                        ShowDialog("Malformed data was received from remote host. Please contact the site owner. Data needs to be: PADDING::OTP::MESSAGE::PADDING");
                    }
                    else {
                        toclipboard = str_array[1];
                        todialog = str_array[2];
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("One Time Password", toclipboard);
                        clipboard.setPrimaryClip(clip);
                        ShowDialog("Authentication completed. Please use the Paste command on the OTP field by long-pressing and then selecting the clipboard icon.\n\nText sent by remote host:\n" + todialog);
                    }
                }
            }
            if (action.equals("s")) {
            actionfound = true;
                Plaintext = DecryptData(Base64.decode(b64encoded,Base64.URL_SAFE));
                if (Plaintext.equals("")) {
                    ShowDialog("Authentication failed. Either you have not done the enrollment step, or this QR code is not intended for you.");
                }
                else {
                    String[] str_array = Plaintext.split("::");
                    if (str_array.length != 4) {
                        ShowDialog("Malformed data was received from remote host. Please contact the site owner. Data needs to be: PADDING::OTP::MESSAGE::PADDING");
                    }
                    else {
                        toclipboard = str_array[1];
                        todialog = str_array[2];
                        ShowDialog("Authentication completed.\n\nYour authentication code:\n" + toclipboard + "\n\nText sent by remote host:\n" + todialog);
                    }
                }
            }
            if (action.equals("e")) {
            actionfound = true;
                PublicKey = GenKey();

                if (PublicKey.equals("")) {
                    ShowDialog("Couldn't enroll your device. The causes can be:\n\n- Your device does not support Hardware-backed key storage.\n- Your device's key storage is not initialized.\n- Your device's key storage requires a secure lock screen.\n- Your device's key storage does not support 2048 bit RSA/ECB/OAEP.\n- Your phone is/was rooted and the hardware key storage has disabled itself.\n\nTip: Sometimes its possible to initialize the key storage by setting a PIN lock screen, then run this enroll process again, and then remove PIN lock screen. After this, the key will remain in secure storage.");
                }
                else {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Enrollment key", PublicKey);
                    clipboard.setPrimaryClip(clip);
                    ShowDialog("Enrollment successful. Please use the Paste command on the Enroll field by long-pressing and then selecting the clipboard icon.");
                }

            }
            if (!actionfound) {
                ShowDialog("Unknown action " + action);
            }
        } else {
            ShowDialog("No data was supplied");
        }

    }

  @Override
  public void onUserLeaveHint() {
      Process.killProcess(Process.myPid());
      super.onUserLeaveHint();
  }

  @Override
  public void onPause() {
      Process.killProcess(Process.myPid());
      super.onPause();
  }

    @Override
    public void onDestroy() {
        Process.killProcess(Process.myPid());
        super.onDestroy();
    }

    public String DecryptData(byte[] CipherText) {
        String ClearText;
        boolean IsSecure;
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            KeyStore.Entry entry = ks.getEntry("eu.sebbe.www.qrsa", null);
            KeyFactory factory = KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
            IsSecure = factory.getKeySpec(((PrivateKeyEntry) entry).getPrivateKey(), KeyInfo.class).isInsideSecureHardware();
            if (IsSecure) {
                Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                c.init(Cipher.DECRYPT_MODE, ((PrivateKeyEntry) entry).getPrivateKey());
                ClearText = new String(c.doFinal(CipherText));
            }
            else
            {
                ClearText = "";
            }
        }
        catch (Exception e) {
            ClearText = "";
        }
        return(ClearText);
    }

    public String GenKey() {
        byte[] pubKey;
        boolean IsSecure;
        KeyGenParameterSpec.Builder kpgs;
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            KeyStore.Entry entry = ks.getEntry("eu.sebbe.www.qrsa", null);
            pubKey = ((PrivateKeyEntry) entry).getCertificate().getPublicKey().getEncoded();
            KeyFactory factory = KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
            IsSecure = factory.getKeySpec(((PrivateKeyEntry) entry).getPrivateKey(), KeyInfo.class).isInsideSecureHardware();
        }
        catch (Exception e) {
            pubKey = null;
            IsSecure = false;
        }

        if (pubKey == null) {
            try {
                KeyPairGenerator kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
                kpgs = new KeyGenParameterSpec.Builder("eu.sebbe.www.qrsa", KeyProperties.PURPOSE_DECRYPT);
                kpgs.setKeySize(2048);
                kpgs.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1);
                kpg.initialize(kpgs.build());
                KeyPair kp = kpg.generateKeyPair();
                pubKey = kp.getPublic().getEncoded();
                KeyFactory factory = KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
                IsSecure = factory.getKeySpec(kp.getPrivate(), KeyInfo.class).isInsideSecureHardware();
            } catch (Exception e) {
                pubKey = null;
                IsSecure = false;
            }
        }
        if ((!(pubKey == null)) && (IsSecure)) {
            return (Base64.encodeToString(pubKey,Base64.URL_SAFE));
        }
        else
        {
            return "";
        }
    }

    public void ShowDialog(String message) {
        String formattedText;
        formattedText = message + "\n\n";
        TextView t;
        t=new TextView(this);
        t=(TextView)findViewById(R.id.textView);
        t.setText(formattedText);
    }

    public void KillApp(View someview) {
        Process.killProcess(Process.myPid());
    }
}
