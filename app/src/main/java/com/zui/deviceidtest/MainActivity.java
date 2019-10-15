package com.zui.deviceidtest;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.bun.miitmdid.core.ErrorCode;
import com.bun.miitmdid.core.IIdentifierListener;
import com.bun.miitmdid.core.MdidSdk;
import com.bun.miitmdid.supplier.IdSupplier;
import com.zui.opendeviceidlibrary.OpenDeviceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mBOAID;
    private Button mIsSupport;
    private Button mUDID;

    private Button mVAID;
    private Button mAAID;

    private OpenDeviceId odid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBOAID = findViewById(R.id.button_oaid);
        mBOAID.setOnClickListener(this);

        mIsSupport = findViewById(R.id.button_issupport);
        mIsSupport.setOnClickListener(this);

        mUDID = findViewById(R.id.button_udid);
        mUDID.setOnClickListener(this);

        mVAID = findViewById(R.id.button_vaid);
        mVAID.setOnClickListener(this);

        mAAID = findViewById(R.id.button_aaid);
        mAAID.setOnClickListener(this);

        odid = new OpenDeviceId();
        int ret1 = odid.init(this.getApplicationContext(), new OpenDeviceId.CallBack<String>() {
            @Override
            public void serviceConnected(String str, OpenDeviceId openDeviceId) {
                Log.d("liufeng", "liufeng, This is client.   Service, Connected. Device id." + str);
                Log.d("liufeng1", "liufeng1, oaid ssss : " + openDeviceId.getOAID());
            }
        });

        MdidSdk sdk = new MdidSdk();
        int nres = sdk.InitSdk(this, new IIdentifierListener() {
            @Override
            public void OnSupport(boolean b, IdSupplier idSupplier) {
                if (idSupplier != null) {
                    String oaid = idSupplier.getOAID();
                    String aaid = idSupplier.getAAID();
                    String udid = idSupplier.getUDID();
                    String vaid = idSupplier.getVAID();
                    Log.d("liufeng", "new SDK liufeng isSupport: " + b + " /oaid:" + oaid + " /aaid:" + aaid);
                    Log.d("liufeng", "new SDK liufeng udid: " + udid + " /vaid:" + vaid);
                }
            }
        });
        if (nres == ErrorCode.INIT_ERROR_DEVICE_NOSUPPORT) {
            Log.d("liufeng", "new SDK iufeng ,not support");
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        // test
        TelephonyManager mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        TelephonyManager tm = mTelephonyManager.createForSubscriptionId(SubscriptionManager.getDefaultDataSubscriptionId());
        PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
            @Override
            public void onDataConnectionStateChanged(int state, int networkType) {
                Log.d("liufeng1", "liufeng1, DATA change: " + state);
            }
        };

        tm.listen(
                mPhoneStateListener, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
    }

    /**
     *
     */
    static class HttpRequestGetRunnable implements Runnable {
        private String mUrl = "";

        public HttpRequestGetRunnable(final String url) {
            this.mUrl = url;
        }

        @Override
        public void run() {
            URL url = null;
            try {
                url = new URL(this.mUrl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return;
            }
            HttpURLConnection connection = null;
            InputStream is = null;
            BufferedReader br = null;
            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(10000);
                connection.setUseCaches(false);
                connection.setRequestProperty("Charset", "UTF-8");
                connection.setRequestProperty("Content-Type", "application/encrypted-json");
                Log.d("liufeng", "liufeng http get, url : " + url.toString());
                connection.connect();
                Log.d("liufeng", "liufeng http get, connection.getResponseCode() : " + connection.getResponseCode());
                if (connection.getResponseCode() == 200) {
                    is = connection.getInputStream();
                    br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                    StringBuffer sbf = new StringBuffer();
                    String temp = null;
                    while ((temp = br.readLine()) != null) {
                        sbf.append(temp);
                        sbf.append("\r\n");
                    }
                    Log.d("liufeng", "liufeng http get, read111 : " + sbf.toString());
                    try {
                        JSONTokener jsonp = new JSONTokener(new String(sbf));
                        JSONObject jobj = (JSONObject) jsonp.nextValue();
                        Log.d("liufeng", "liufeng http code:, " + jobj.getString("code"));
                        Log.d("liufeng", "liufeng http msg:, " + jobj.getString("msg"));
                        Log.d("liufeng", "liufeng http data:, " + jobj.getString("data"));
                        JSONArray jsa = (JSONArray) jobj.getJSONArray("data");
                        JSONObject jobj2 = (JSONObject) jsa.get(0);
                        Log.d("liufeng", "liufeng http developer :, " + jobj2.getString("developerId"));
                        Log.d("liufeng", "liufeng http applicationData:, " + jobj2.getString("applicationDate"));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (null != br) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (null != is) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                connection.disconnect();
            }

        }
    }

    private static final String GET_USERID_URL = "https://adapi.lenovomm.com/gwouter/appbiz/lestoreDeveloperInfo/api/get?pkgname=";

    private Signature[] getRawSignature(Context paramContext, String paramString) {
        if ((paramString == null) || (paramString.length() == 0)) {

            return null;
        }
        PackageManager localPackageManager = paramContext.getPackageManager();
        PackageInfo localPackageInfo;
        try {
            localPackageInfo = localPackageManager.getPackageInfo(paramString, PackageManager.GET_SIGNATURES);
            if (localPackageInfo == null) {

                return null;
            }
        } catch (PackageManager.NameNotFoundException localNameNotFoundException) {

            return null;
        }
        return localPackageInfo.signatures;
    }

    private String getSign(String packageName) {
        Signature[] arrayOfSignature = getRawSignature(this, packageName);
        if ((arrayOfSignature == null) || (arrayOfSignature.length == 0)) {
            return null;
        }

        return getMD5MessageDigest(arrayOfSignature[0].toByteArray());
    }

    public static final String getMD5MessageDigest(byte[] paramArrayOfByte) {
        char[] arrayOfChar1 = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 97, 98, 99, 100, 101, 102};
        try {
            MessageDigest localMessageDigest = MessageDigest.getInstance("MD5");
            localMessageDigest.update(paramArrayOfByte);
            byte[] arrayOfByte = localMessageDigest.digest();
            int i = arrayOfByte.length;
            char[] arrayOfChar2 = new char[i * 2];
            int j = 0;
            int k = 0;
            while (true) {
                if (j >= i)
                    return new String(arrayOfChar2);
                int m = arrayOfByte[j];
                int n = k + 1;
                arrayOfChar2[k] = arrayOfChar1[(0xF & m >>> 4)];
                k = n + 1;
                arrayOfChar2[n] = arrayOfChar1[(m & 0xF)];
                j++;
            }
        } catch (Exception localException) {
        }
        return null;
    }

    private long getRandom(){
        final long seed = System.currentTimeMillis();
        Random r = new Random(seed);
        long rr = r.nextLong();
        return rr;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == mBOAID.getId()) {
            if (odid != null) {
                Log.i("liufeng", "Clinet call getOAID:" + odid.getOAID());
            } else {
                Log.i("liufeng", "Clinet ReStart call getOAID:" + odid.getOAID());
            }
        } else if (v.getId() == mIsSupport.getId()) {
            //HttpThreadPoolUtils.execute(new HttpRequestGetRunnable(GET_USERID_URL + "com.changba"));


//            if (odid != null) {
//                Log.i("liufeng", "Clinet call isSupport:" + odid.isSupported());
//            } else {
//                Log.i("liufeng", "is null, not support");
//            }
            String strp = "com.zui.flowrecharge";
            String strSign = getSign(strp);
            Log.i("liufeng", "liufeng, zuk sign is :" + strSign);

            String rsaSign = "";
            Log.i("liufeng", "rsa sing 256:" + rsaSign(strp));
            //Log.i("liufeng", "Random:" + Long.toHexString(getRandom()) + System.currentTimeMillis());


        } else if (v.getId() == mUDID.getId()) {
            if (odid != null) {
                Log.i("liufeng", "Clinet call gtUDID:" + odid.getUDID());
            } else {
                Log.i("liufeng", "Clinet ReStart call getUDID failed.");
            }
        } else if (v.getId() == mVAID.getId()) {
            if (odid != null) {
                Log.i("liufeng", "Clinet call getVAID:" + odid.getVAID());
            } else {
                Log.i("liufeng", "Clinet ReStart call getVAID failed.");
            }
        } else if (v.getId() == mAAID.getId()) {
            if (odid != null) {
                // Log.i("liufeng", "liufeng, uuid:" + UUID.randomUUID().toString());
                Log.i("liufeng", "Clinet call getAAID:" + odid.getAAID());
            } else {
                Log.i("liufeng", "Clinet ReStart call getAAID failed.");
            }
        }
    }


    private static final String PRI_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC+pUXQDG2Pvuuuisz1TlVouBMQAvUzcM1f3ykwLNQjBLdZ4IgkUlT3Np/bowrykE4lz12ehTeOA6WPA2bzoJJaKX1T8804WZfi6h9XWzFm3IrKwi2yExiM9BVkv3G0ENMNxfK5er6duVO0wcdqeCLN1vPXweIZJoIFrZ99cZUwYyMcGmkc76yIxLaXHV92BU4ZqSB1C3FQ/GKVESuyf4pkdcaKqOe0xyeTGcruFHqU11x3ObKIn5kwmBn3+faaqiV3IGI2N0W7j+u+yLF4PF35GFidOjKhs91ar8H381QqqLb8s+bxPlt+Sq6HL4YnE8dXnF06aHNrOHhVBKZhZ1DnAgMBAAECggEAcMQ3yDkhsU4gAC0v0MXV9Uw0Uls9iIDnqhbJOSq7DD+k6G/Md5ePlG7mHbeSKSo8X1cKLFLmKoVzr2U2x1KqXtR9wmPKdtNf6SPNCBHz5cD8A7TZ7KQo+G8EHIS1D+qHvHTi/t1g1NCjd57LgumuxIypzWxsfa3uaMdrOHsv4N8tpSqWRix8j4HN9aP5y4xIukfaPbYUMCG4K4KjZrHZu0splmq3AeshmHZCnDfPhvJGgeVguk3bkZVIrFtX4K05xy8HGYNlC9GJ6osUnO11eeWDW939ScZt7A1KJOpJ0Y7bhiCHcEP1ghqQOvpNnnGd0HLcyB7tnf3+FwTnAAWi6QKBgQDhZZiga4j+MJSMgYWA5w9bVA1kYKDPlsCBTW1BcTUNabMkmD6J64V6Bxmx5DtebSyw6wL+iePik08Viq9vq/63ZURSjSVNo2H5UCnVjmu8Z+b0dzDR54Nwp9ZgubJD1/w9pG+i9JJKBxNZ+C/EidL4SlNzc9c10Ri+q9JxilvJqwKBgQDYh8hqk2oVc34qoSkPSq665+RVn4nm4B7+GQSfgoyLTWwERm2h6hO2HxVSfuz++VQVXFzUct2Q1KIpXWic5Y/r9rZYLks9srafeuP+gtcHEsLtGP+M2yhc/cucBDkOFCFgKFrktRVSlhbARxsDZAdi34CMqt2tBFLg41LbHrgxtQKBgHxsqy6TblJz2u0daudXpiCSa7onpV4zKB248kEYD2NSIDRpXsygGVTdqo+LIELmHa+kbEi7MfOXwiZwIpyQ49G1s1um0xriwGjymcVsE4k0CkiVq3uUQ/jijfNjT0coafRVW9MnE8KN2V7nJOdn9fBeh2bKYdkxjmljTI6lBDp1AoGBAL0e85yqftiXlFX1hyBVEYIsMlHa0560mD1FarVLWCf/il29idoG0gqa4Yu5UpRs/tTdZDMm1IDAR5argEixdOAbDy672HneEwX+Vw6gBuGlsF1YHTRQ4tM91M3DHnY+fNw4wxLJWwNUFjEAqgZvIshoACZcwttwUFceFetOzICVAoGAVCrTugqBQ3cXChtvmWoYKbE9bb8aWwAhekYiMCtJsMeqJLS+3rX+hspJ3O096/vxFFB8UNmG7tdcoUnPHDNWvc/Wj39j80Zta1muA/+4bpnN8AsvTJBDXcmgv7i1eG5McFNIpKkLasEqXxk4okrg0q7nvWseaaZPS7nrrB0m92I=";
    private static final String SIGN_TYPE = "RSA2";
    private static final String MERCHANT_ID = "20191010181920";

    public static String rsaSign(String strdata) throws Exception {
        System.out.println("请求参数生成的字符串为:" + strdata);
        PrivateKey priKey  = loadPrivateKey(PRI_KEY);
        java.security.Signature signature = java.security.Signature.getInstance("SHA256WithRSA");

        signature.initSign(priKey);
        signature.update(strdata.getBytes());
        byte[] signed = signature.sign();

        return new String(Base64Utils.encode(signed));

    }

    /**
     * load private key
     * @param privateKeyStr
     * @throws Exception
     */
    public static PrivateKey loadPrivateKey(String privateKeyStr) throws Exception {
        try {
            byte[] buffer = Base64Utils.decode(privateKeyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此算法");
        } catch (InvalidKeySpecException e) {
            throw new Exception("私钥非法");
        } catch (NullPointerException e) {
            throw new Exception("私钥数据为空");
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (odid != null) {
//            odid.shutdown();
//        }
    }

    public static String getSHA256(String str) {
        MessageDigest messageDigest;
        String encodestr = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes("UTF-8"));
            encodestr = byte2Hex(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encodestr;
    }

    private static String byte2Hex(byte[] bytes) {
        StringBuffer stringBuffer = new StringBuffer();
        String temp = null;
        for (int i = 0; i < bytes.length; i++) {
            temp = Integer.toHexString(bytes[i] & 0xFF);
            if (temp.length() == 1) {
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }

}
