package com.example.android.camera2basic;

import android.os.AsyncTask;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Core_API extends AsyncTask<String, Void, String> {

    private CameraActivity mainActivity;

    // Constructer
    public Core_API(CameraActivity activity) {
        super();
        this.mainActivity = activity;
    }

    // Method of Background Job
    @Override
    protected String doInBackground(String... value) {

        try {
//            return Google_Cloud_Vision_API( value[0]);
//            return URLGET( AmazonAPI_Req("sennheiser HD800") )
            return URLGET( AmazonAPI_Req(Google_Cloud_Vision_API( value[0])) );
        } catch (IOException e) {
            e.printStackTrace();
            return "IOException Error!!!!";
        } catch (JSONException e) {
            e.printStackTrace();
            return "JSONException Error!!!!";
        }

    }

    // After Background Job
    @Override
    protected void onPostExecute(String result) {
        mainActivity.setAPIResult(result);
    }

    protected static String Google_Cloud_Vision_API(String img_base64) throws IOException, JSONException {

        String USER_AGENT = "Mozilla/5.0";

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // add request header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setRequestProperty("Accept-Language", "ja");
        con.setRequestProperty("Content-Type", "application/json");

        String json = Build_JSON_for_Google(img_base64);

        con.setDoOutput(true);
        DataOutputStream DOS = new DataOutputStream(con.getOutputStream());
        DOS.writeBytes(json);
        DOS.flush();
        DOS.close();

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()) );
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // parse json
        JSONObject googleapi_json = new JSONObject( response.toString() );
        String total_txt = googleapi_json.getJSONArray("responses").getJSONObject(0).getJSONArray("textAnnotations").getJSONObject(0).getString("description");

        return total_txt;
    }

    public static String Build_JSON_for_Google(String base64image){
        return "{\"requests\":[{\"image\":{\"content\":\"" + base64image + "\"},\"features\":[{\"type\":\"TEXT_DETECTION\",\"maxResults\":1}]}]}";
    }

    ////////////////////////////////////////////////////////////////////////

    protected static String URLGET(String urlstr) throws IOException {
        String USER_AGENT = "Mozilla/5.0";
//        String url = "http://google.com/search?q=探したい単語";

        URL obj = new URL(urlstr);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        // optional default is GET
        con.setRequestMethod("GET");
        // add requesy header
        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + urlstr);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()) );
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

    protected static String Amazon_API(String urlstr) throws IOException {
        String USER_AGENT = "Mozilla/5.0";
//        String url = "http://google.com/search?q=探したい単語";

        URL obj = new URL(urlstr);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        // optional default is GET
        con.setRequestMethod("GET");
        // add requesy header
        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + urlstr);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()) );
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

    //==========Make Amazon Request======================================
    public static String AmazonAPI_Req(String Keywords){
        TreeMap<String,String> kwargs = new TreeMap<String,String>();

        //ここにアクセスキーやシークレットキーを入力するコードを追加して利用する。

        //add Timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'Z'");
        kwargs.put("Timestamp", sdf.format(new Date()) );

        //Make Signature
        String Request = "GET\nwebservices.amazon.co.jp\n/onca/xml\n";
        Request += Build_URL(kwargs);
        // - Calc Signature
        String Signature = Calc_HMACSHA256(Request);

        // Make Request
        Request = "http://webservices.amazon.co.jp/onca/xml?";
        Request += Build_URL(kwargs);
        // - Add Signature
        Request += "&Signature=" + Signature.replace("+","%2B").replace("=","%3D").replace("/","%2F");

        return Request;

    }
    //==========Make Amazon Request======================================

    //==========================Sign=====================================
    public static String Calc_HMACSHA256(String Plane_text){
        String Signature = "";

        String algo = "HmacSHA256";
        try {
            SecretKeySpec sk = new SecretKeySpec(secret.getBytes(), algo);
            Mac mac = Mac.getInstance(algo);
            mac.init(sk);

            byte[] mac_bytes = mac.doFinal(Plane_text.getBytes());
            Signature = Base64.encodeToString(mac_bytes, Base64.DEFAULT);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return Signature;
    }
    //==========================Sign=====================================

    //=================Kwargs to URL ====================================
    public static String Build_URL(TreeMap<String, String> kwargs){
        String url_str = "";
        for (Map.Entry<String, String> param: kwargs.entrySet()) {
            url_str += param.getKey();
            url_str += "=";
            try {
                url_str += URLEncoder.encode(param.getValue(), "UTF-8")
                        .replaceAll("\\+", "%20")
                        .replaceAll("\\%21", "!")
                        .replaceAll("\\%27", "'")
                        .replaceAll("\\%28", "(")
                        .replaceAll("\\%29", ")")
                        .replaceAll("\\%7E", "~");
            }catch(UnsupportedEncodingException e){
                System.out.println("URL Encoding Error!\n");
                System.exit(0);
            }
            url_str += "&";
        }
        if(url_str != null && url_str.length() > 0){
            url_str = url_str.substring(0, url_str.length()-1);
        }
        return url_str;
    }
    //=================Kwargs to URL ====================================

}