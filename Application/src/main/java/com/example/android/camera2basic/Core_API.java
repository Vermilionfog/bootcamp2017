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

    public CameraActivity mainActivity;
    private long start;
    private long end;

    // Constructer
    public Core_API(CameraActivity activity) {
        super();
        start = System.currentTimeMillis();
        this.mainActivity = activity;
    }

    // Method of Background Job
    @Override
    protected String doInBackground(String... value) {
        System.out.println("API Start");

        try {
//            return Google_Cloud_Vision_API( value[0]);
//            return URLGET( AmazonAPI_Req("sennheiser HD800") )
            float start_google_api = System.currentTimeMillis();
            String targetText =Google_Cloud_Vision_API( value[0]);
            float end_google_api = System.currentTimeMillis();
            System.out.println("Google_Cloud_Vision_API(True) " + (end_google_api-start_google_api) + "ms");
            System.out.println("検索対象の文字列 :" + targetText);
//            mainActivity.setRecognitionText(targetText);

            float amazon_api_start = System.currentTimeMillis();
            String amazon_api = AmazonAPI_Req(targetText);
            float amazon_api_end = System.currentTimeMillis();
            System.out.println("Amazon API " + (amazon_api_end - amazon_api_start) + "ms");
            return URLGET(amazon_api);
        } catch (IOException e) {
            e.printStackTrace();
            return "IOException";
        } catch (JSONException e) {
            e.printStackTrace();
            return "JSONException";
        }

    }

    // After Background Job
    @Override
    protected void onPostExecute(String result) {
        System.out.println("Core_API onPostExecute : " + result);
        mainActivity.setAPIResult(result);
        mainActivity.displayReload(true);
        end = System.currentTimeMillis();
        System.out.println("Core_API " + (end-start) + "ms");
    }

    protected static String Google_Cloud_Vision_API(String img_base64) throws IOException, JSONException {

        System.out.println("GoogleCloudVisionApi START");
        try {
            String USER_AGENT = "Mozilla/5.0";

            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            // add request header
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            con.setRequestProperty("Accept-Language", "ja");
            con.setRequestProperty("Content-Type", "application/json");
            System.out.println("A3");
            String json = Build_JSON_for_Google(img_base64);

            con.setDoOutput(true);
            DataOutputStream DOS = new DataOutputStream(con.getOutputStream());
            DOS.writeBytes(json);
            DOS.flush();
            DOS.close();

            System.out.println("A4");

//            System.out.println("GET RESPONSE CODE");
//            int responseCode = con.getResponseCode();
//            System.out.println("GET RESPONSE CODE");
//
//            System.out.println("\nSending 'POST' request to URL : " + url);
//            System.out.println("Response Code : " + responseCode);
            System.out.println("1");
            // 重い
            InputStreamReader inputStreamReader = new InputStreamReader(con.getInputStream());
            System.out.println("2");
            BufferedReader in = new BufferedReader(inputStreamReader);
            System.out.println("3");
            String inputLine;
            StringBuffer response = new StringBuffer();
            System.out.println("4");


            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            System.out.println(response);
            // parse json
            JSONObject googleapi_json = new JSONObject(response.toString());
            System.out.println(googleapi_json);
            String total_txt = googleapi_json.getJSONArray("responses").getJSONObject(0).getJSONArray("textAnnotations").getJSONObject(0).getString("description");
            String test_text = googleapi_json.getJSONArray("responses").getJSONObject(0).getJSONArray("textAnnotations").getJSONObject(0).getString("description");
            total_txt = selectProductTitle(total_txt);
            System.out.println("Total TXT " + total_txt);
            System.out.println("GoogleCloudVisionApi END");
            return total_txt;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return "";
        }

    }

    public static String Build_JSON_for_Google(String base64image){
        return "{\"requests\":[{\"image\":{\"content\":\"" + base64image + "\"},\"features\":[{\"type\":\"TEXT_DETECTION\",\"maxResults\":1}]}]}";
    }

    ////////////////////////////////////////////////////////////////////////

    protected static String URLGET(String urlstr) throws IOException {
        try {
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

            System.out.println(con.getInputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        }
        catch(Exception e)
        {
            System.out.println("エラー");
            e.printStackTrace();
            return "";
        }
    }

    //==========Make Amazon Request======================================
    public static String AmazonAPI_Req(String Keywords){
        //ここにアクセスキーやシークレットキーを入力するコードを追加して利用する。
        TreeMap<String,String> kwargs = new TreeMap<String,String>();


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

    // 画像から読み取ったワードから、重要なワードだけを選択する。
    public static String selectProductTitle(String total_txt)
    {
        //改行文字で分割し、上位２つまでのワードを結合して返却する
        String[] word = total_txt.split("\n");
        Integer i = 0;
        String result = "";
        while(i < word.length && i<2)
        {
            result += (word[i] + " || ");
            i++;
        }

        return result;
    }

}