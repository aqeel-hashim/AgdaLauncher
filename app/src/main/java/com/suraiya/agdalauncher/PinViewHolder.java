package com.suraiya.agdalauncher;

import android.animation.ObjectAnimator;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Handler;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.amirarcane.lockscreen.andrognito.pinlockview.IndicatorDots;
import com.amirarcane.lockscreen.andrognito.pinlockview.PinLockListener;
import com.amirarcane.lockscreen.andrognito.pinlockview.PinLockView;
import com.amirarcane.lockscreen.fingerprint.FingerPrintListener;
import com.amirarcane.lockscreen.fingerprint.FingerprintHandler;
import com.amirarcane.lockscreen.util.Animate;
import com.amirarcane.lockscreen.util.Utils;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import static android.app.Activity.RESULT_FIRST_USER;
import static android.app.Activity.RESULT_OK;
import static android.content.Context.FINGERPRINT_SERVICE;
import static android.content.Context.KEYGUARD_SERVICE;
import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by Suraiya on 1/4/2018.
 */

public class PinViewHolder{

    public static final String TAG = "EnterPinActivity";

    public static final int RESULT_BACK_PRESSED = RESULT_FIRST_USER;
    //    public static final int RESULT_TOO_MANY_TRIES = RESULT_FIRST_USER + 1;
    public static final String EXTRA_SET_PIN = "set_pin";
    public static final String EXTRA_FONT_TEXT = "textFont";
    public static final String EXTRA_FONT_NUM = "numFont";

    private static final int PIN_LENGTH = 4;
    private static final String FINGER_PRINT_KEY = "FingerPrintKey";

    private static final String PREFERENCES = "com.amirarcane.lockscreen";
    private static final String KEY_PIN = "pin";

    private PinLockView mPinLockView;
    private IndicatorDots mIndicatorDots;
    private TextView mTextTitle;
    private TextView mTextAttempts;
    private TextView mTextFingerText;
    private AppCompatImageView mImageViewFingerView;

    private Cipher mCipher;
    private KeyStore mKeyStore;
    private KeyGenerator mKeyGenerator;
    private FingerprintManager.CryptoObject mCryptoObject;
    private FingerprintManager mFingerprintManager;
    private KeyguardManager mKeyguardManager;
    private boolean mSetPin = false;
    private String mFirstPin = "";
    //    private int mTryCount = 0;

    private AnimatedVectorDrawable showFingerprint;
    private AnimatedVectorDrawable fingerprintToTick;
    private AnimatedVectorDrawable fingerprintToCross;

    private Context context;

    private View mView;

    public View getmView(){
        return mView;
    }

    public PinViewHolder(View mainView, boolean setPin, Context ctx){
        mView = mainView;
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.RIGHT | Gravity.TOP;
        WindowManager wm = (WindowManager) ctx.getSystemService(WINDOW_SERVICE);
        wm.addView(mainView, params);
        context = ctx;
        mTextAttempts = (TextView) mainView.findViewById(R.id.attempts);
        mTextTitle = (TextView) mainView.findViewById(R.id.title);
        mIndicatorDots = (IndicatorDots) mainView.findViewById(R.id.indicator_dots);
        mImageViewFingerView = (AppCompatImageView) mainView.findViewById(R.id.fingerView);
        mTextFingerText = (TextView) mainView.findViewById(R.id.fingerText);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            showFingerprint = (AnimatedVectorDrawable) context.getDrawable(R.drawable.show_fingerprint);
            fingerprintToTick = (AnimatedVectorDrawable) context.getDrawable(R.drawable.fingerprint_to_tick);
            fingerprintToCross = (AnimatedVectorDrawable) context.getDrawable(R.drawable.fingerprint_to_cross);

            mSetPin = setPin;
            if (mSetPin) {
                changeLayoutForSetPin();
            } else {
                String pin = getPinFromSharedPreferences();
                if (pin.equals("")) {
                    changeLayoutForSetPin();
                    mSetPin = true;
                } else {
                    checkForFingerPrint();
                }
            }

            final PinLockListener pinLockListener = new PinLockListener() {

                @Override
                public void onComplete(String pin) {
                    if (mSetPin) {
                        setPin(pin);
                    } else {
                        checkPin(pin);
                    }
                }

                @Override
                public void onEmpty() {
                    Log.d(TAG, "Pin empty");
                }

                @Override
                public void onPinChange(int pinLength, String intermediatePin) {
                    Log.d(TAG, "Pin changed, new length " + pinLength + " with intermediate pin " + intermediatePin);
                }

            };

            mPinLockView = (PinLockView) mainView.findViewById(R.id.pinlockView);
            mIndicatorDots = (IndicatorDots) mainView.findViewById(R.id.indicator_dots);

            mPinLockView.attachIndicatorDots(mIndicatorDots);
            mPinLockView.setPinLockListener(pinLockListener);

            mPinLockView.setPinLength(PIN_LENGTH);

            mIndicatorDots.setIndicatorType(IndicatorDots.IndicatorType.FILL_WITH_ANIMATION);
        }


    }

    private void setTextFont(String font) {
        try {
            Typeface typeface = Typeface.createFromAsset(context.getAssets(), font);

            mTextTitle.setTypeface(typeface);
            mTextAttempts.setTypeface(typeface);
            mTextFingerText.setTypeface(typeface);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setNumFont(String font) {
        try {
            Typeface typeface = Typeface.createFromAsset(context.getAssets(), font);

            mPinLockView.setTypeFace(typeface);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Create the generateKey method that we’ll use to gain access to the Android keystore and generate the encryption key//
    private void generateKey() throws FingerprintException {
        try {
            // Obtain a reference to the Keystore using the standard Android keystore container identifier (“AndroidKeystore”)//
            mKeyStore = KeyStore.getInstance("AndroidKeyStore");

            //Generate the key//
            mKeyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            //Initialize an empty KeyStore//
            mKeyStore.load(null);

            //Initialize the KeyGenerator//
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mKeyGenerator.init(new

                        //Specify the operation(s) this key can be used for//
                        KeyGenParameterSpec.Builder(FINGER_PRINT_KEY,
                        KeyProperties.PURPOSE_ENCRYPT |
                                KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)

                        //Configure this key so that the user has to confirm their identity with a fingerprint each time they want to use it//
                        .setUserAuthenticationRequired(true)
                        .setEncryptionPaddings(
                                KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .build());
            }

            //Generate the key//
            mKeyGenerator.generateKey();

        } catch (KeyStoreException
                | NoSuchAlgorithmException
                | NoSuchProviderException
                | InvalidAlgorithmParameterException
                | CertificateException
                | IOException exc) {
            throw new FingerprintException(exc);
        }
    }

    //Create a new method that we’ll use to initialize our mCipher//
    public boolean initCipher() {
        try {
            //Obtain a mCipher instance and configure it with the properties required for fingerprint authentication//
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mCipher = Cipher.getInstance(
                        KeyProperties.KEY_ALGORITHM_AES + "/"
                                + KeyProperties.BLOCK_MODE_CBC + "/"
                                + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            }
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            mKeyStore.load(null);
            SecretKey key = (SecretKey) mKeyStore.getKey(FINGER_PRINT_KEY,
                    null);
            mCipher.init(Cipher.ENCRYPT_MODE, key);
            //Return true if the mCipher has been initialized successfully//
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    private void writePinToSharedPreferences(String pin) {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_PIN, Utils.sha256(pin)).apply();
    }

    private String getPinFromSharedPreferences() {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        return prefs.getString(KEY_PIN, "");
    }

    private void setPin(String pin) {
        if (mFirstPin.equals("")) {
            mFirstPin = pin;
            mTextTitle.setText(context.getString(R.string.pinlock_secondPin));
            mPinLockView.resetPinLockView();
        } else {
            if (pin.equals(mFirstPin)) {
                writePinToSharedPreferences(pin);
            } else {
                shake();
                mTextTitle.setText(context.getString(R.string.pinlock_tryagain));
                mPinLockView.resetPinLockView();
                mFirstPin = "";
            }
        }
    }

    private void checkPin(String pin) {
        if (Utils.sha256(pin).equals(getPinFromSharedPreferences())) {
            Toast.makeText(context, "Test Working ", Toast.LENGTH_SHORT).show();
        } else {
            shake();

//            mTryCount++;

            mTextAttempts.setText(context.getString(R.string.pinlock_wrongpin));
            mPinLockView.resetPinLockView();

//            if (mTryCount == 1) {
//                mTextAttempts.setText(getString(R.string.pinlock_firsttry));
//                mPinLockView.resetPinLockView();
//            } else if (mTryCount == 2) {
//                mTextAttempts.setText(getString(R.string.pinlock_secondtry));
//                mPinLockView.resetPinLockView();
//            } else if (mTryCount > 2) {
//                setResult(RESULT_TOO_MANY_TRIES);
//                finish();
//            }
        }
    }

    private void shake() {
        ObjectAnimator objectAnimator = new ObjectAnimator().ofFloat(mPinLockView, "translationX",
                0, 25, -25, 25, -25, 15, -15, 6, -6, 0).setDuration(1000);
        objectAnimator.start();
    }

    private void changeLayoutForSetPin() {
        mImageViewFingerView.setVisibility(View.GONE);
        mTextFingerText.setVisibility(View.GONE);
        mTextAttempts.setVisibility(View.GONE);
        mTextTitle.setText(context.getString(R.string.pinlock_settitle));
    }

    private void checkForFingerPrint() {

        final FingerPrintListener fingerPrintListener = new FingerPrintListener() {

            @Override
            public void onSuccess() {

                Animate.animate(mImageViewFingerView, fingerprintToTick);
                final Handler handler = new Handler();
                handler.postDelayed(() -> Toast.makeText(context, "test working", Toast.LENGTH_SHORT).show(), 750);
            }

            @Override
            public void onFailed() {
                Animate.animate(mImageViewFingerView, fingerprintToCross);
                final Handler handler = new Handler();
                handler.postDelayed(() -> Animate.animate(mImageViewFingerView, showFingerprint), 750);
            }

            @Override
            public void onError(CharSequence errorString) {
                Toast.makeText(context, errorString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onHelp(CharSequence helpString) {
                Toast.makeText(context, helpString, Toast.LENGTH_SHORT).show();
            }

        };

        // If you’ve set your app’s minSdkVersion to anything lower than 23, then you’ll need to verify that the device is running Marshmallow
        // or higher before executing any fingerprint-related code
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Get an instance of KeyguardManager and FingerprintManager//
            mKeyguardManager = (KeyguardManager) context.getSystemService(KEYGUARD_SERVICE  );
            mFingerprintManager = (FingerprintManager) context.getSystemService(FINGERPRINT_SERVICE);

            //Check whether the device has a fingerprint sensor//
            if (!mFingerprintManager.isHardwareDetected()) {
                // If a fingerprint sensor isn’t available, then inform the user that they’ll be unable to use your app’s fingerprint functionality//
//                textView.setText("Your device doesn't support fingerprint authentication");
                mImageViewFingerView.setVisibility(View.GONE);
                mTextFingerText.setVisibility(View.GONE);
            }
            //Check whether the user has granted your app the USE_FINGERPRINT permission//
            if (ActivityCompat.checkSelfPermission(context, FINGERPRINT_SERVICE) != PackageManager.PERMISSION_GRANTED) {
                // If your app doesn't have this permission, then display the following text//
//                Toast.makeText(EnterPinActivity.this, "Please enable the fingerprint permission", Toast.LENGTH_LONG).show();
                mImageViewFingerView.setVisibility(View.GONE);
                mTextFingerText.setVisibility(View.GONE);
            }

            //Check that the user has registered at least one fingerprint//
            if (!mFingerprintManager.hasEnrolledFingerprints()) {
                // If the user hasn’t configured any fingerprints, then display the following message//
//                Toast.makeText(EnterPinActivity.this,
//                        "No fingerprint configured. Please register at least one fingerprint in your device's Settings",
//                        Toast.LENGTH_LONG).show();
                mImageViewFingerView.setVisibility(View.GONE);
                mTextFingerText.setVisibility(View.GONE);
            }

            //Check that the lockscreen is secured//
            if (!mKeyguardManager.isKeyguardSecure()) {
                // If the user hasn’t secured their lockscreen with a PIN password or pattern, then display the following text//
//                Toast.makeText(EnterPinActivity.this, "Please enable lockscreen security in your device's Settings", Toast.LENGTH_LONG).show();
                mImageViewFingerView.setVisibility(View.GONE);
                mTextFingerText.setVisibility(View.GONE);
            } else {
                try {
                    generateKey();
                } catch (FingerprintException e) {
                    Log.wtf(TAG, "Failed to generate key for fingerprint.", e);
                }

                if (initCipher()) {
                    //If the mCipher is initialized successfully, then create a CryptoObject instance//
                    mCryptoObject = new FingerprintManager.CryptoObject(mCipher);

                    // Here, I’m referencing the FingerprintHandler class that we’ll create in the next section. This class will be responsible
                    // for starting the authentication process (via the startAuth method) and processing the authentication process events//
                    FingerprintHandler helper = new FingerprintHandler(context);
                    helper.startAuth(mFingerprintManager, mCryptoObject);
                    helper.setFingerPrintListener(fingerPrintListener);
                }
            }
        } else {
            mImageViewFingerView.setVisibility(View.GONE);
            mTextFingerText.setVisibility(View.GONE);
        }
    }

    private class FingerprintException extends Exception {
        public FingerprintException(Exception e) {
            super(e);
        }
    }


}
