package com.gmail.docfordja.model;

import com.gmail.docfordja.bot.BotStateMethods;
import com.gmail.docfordja.bot.Utils;
import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.aspectj.apache.bcel.classfile.annotation.NameValuePair;
import org.json.JSONObject;

import java.io.*;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

public class RequestOrder {

    private Artist artist;
    private String merchantAccount;
    private String merchantDomainName;
    private String orderReference;
    private long orderDate;
    private long amount;
    private String currency;
    private String productName;
    private int productCount;
    private String productPrice;
    private String merchantSignature;

    public RequestOrder() {}

    public RequestOrder(Artist a) throws Exception {
        this.artist = a;
        this.merchantAccount = Utils.getValue("merchant.merchantAccount");
        this.merchantDomainName = Utils.getValue("merchant.merchantDomainName");
        this.orderReference = Utils.HMAC_MD5_encode(Long.toString(new Date().getTime()) );
        this.orderDate = Instant.now().getEpochSecond();;
        this.amount = BotStateMethods.priceMaker(a)/100;
        this.currency = Utils.getValue("merchant.currency");
        this.productName = "Performans";//a.getLastname() + " " + a.getUsername() + " " + a.getFathername() + " " + a.getView();
        this.productCount = 1;
        this.productPrice = String.valueOf(amount);
        this.merchantSignature = Utils.HMAC_MD5_encode(merchantAccount + ";" + merchantDomainName + ";" +
                orderReference + ";" + orderDate + ";" + amount + ";" + currency + ";" + productName + ";" +
                productCount + ";" + productPrice);
    }

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public void order() throws Exception {
        HttpClient httpclient = HttpClients.createDefault();
        Map<String, String> arguments = new HashMap<>();
        arguments.put("merchantAccount", merchantAccount);
        arguments.put("merchantDomainName", merchantDomainName);
        arguments.put("orderReference", orderReference);
        arguments.put("orderDate", String.valueOf(orderDate));
        arguments.put("amount", String.valueOf(amount));
        arguments.put("currency", currency);
        arguments.put("productName[]", productName);
        arguments.put("productCount[]", String.valueOf(productCount));
        arguments.put("productPrice[]", productPrice);
        arguments.put("merchantSignature", merchantSignature);
        StringJoiner sj = new StringJoiner("&");
        for (Map.Entry<String, String> entry : arguments.entrySet()) {
            sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "="
                    + URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        System.out.println(sj.toString());
        HttpPost httppost = new HttpPost("https://secure.wayforpay.com/pay?" + sj.toString());
        //httppost.setEntity(new StringEntity(sj.toString(), "UTF-8"));
        HttpResponse response = httpclient.execute(httppost);
        Scanner sc = new Scanner(response.getEntity().getContent());
        System.out.println(response.getStatusLine());
        while (sc.hasNext()) {
            System.out.println(sc.nextLine());

        }
       /* HttpEntity entity = response.getEntity();
        String content = EntityUtils.toString(entity);
        if (entity != null) {
            InputStream instream = entity.getContent();
            try {
                byte[] targetArray = new byte[instream.available()];
                instream.read(targetArray);
                System.out.println("!!##!#!#!#!#!#!#!#!#!##!#!#!#!#!##!#!#!#!#!\n" + targetArray.toString()
                        +"\n" +content);
            } finally {
                instream.close();
            }


       /* URL url = new URL("https://secure.wayforpay.com/pay");
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection)con;
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        Map<String,Object> arguments = new HashMap<>();
        arguments.put("merchantAccount", merchantAccount);
        arguments.put("merchantDomainName", merchantDomainName);
        arguments.put("orderReference", orderReference);
        arguments.put("orderDate", String.valueOf(orderDate));
        arguments.put("amount", String.valueOf(amount));
        arguments.put("currency", currency);
        arguments.put("productName[]", productName);
        arguments.put("productCount[]", productCount);
        arguments.put("productPrice[]", productPrice);
        arguments.put("merchantSignature", merchantSignature);
        StringJoiner sj = new StringJoiner("&");
        for(Map.Entry<String, Object> entry : arguments.entrySet()){
            sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(entry.getValue()), "UTF-8"));}
        System.out.println(sj.toString());
        byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
        int length = out.length;
        http.setFixedLengthStreamingMode(length);
        System.out.println(out.toString());
        http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        http.connect();
        try(OutputStream os = http.getOutputStream()) {
            os.write(out);
            System.out.println(http.getResponseMessage());*/
    }
        }


        /*Artist artist = getArtist();

        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost("https://secure.wayforpay.com/pay/");
        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        HttpResponse response = httpclient.execute(httppost);

        HttpEntity entity = response.getEntity();
        String content = EntityUtils.toString(entity);
        if (entity != null) {
            InputStream instream = entity.getContent();
            try {
                byte[] targetArray = new byte[instream.available()];
                instream.read(targetArray);
                System.out.println("!!##!#!#!#!#!#!#!#!#!##!#!#!#!#!##!#!#!#!#!\n" + targetArray.toString()
                +"\n" +content);
            } finally {
                instream.close();
            }


    URL url = new URL("https://secure.wayforpay.com/pay");
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection)con;
        http.setRequestMethod("POST"); // PUT is another valid option
        http.setDoOutput(true);
        Map<String,Object> arguments = new HashMap<>();
        arguments.put("merchantAccount", merchantAccount);
        arguments.put("merchantDomainName", merchantDomainName);
        arguments.put("orderReference", orderReference);
        arguments.put("orderDate", String.valueOf(orderDate));
        arguments.put("amount", String.valueOf(amount));
        arguments.put("currency", currency);
        arguments.put("productName", new String[]{ productName});
        arguments.put("productCount", new String[]{productCount});
        arguments.put("productPrice",new String[]{ productPrice});
        arguments.put("merchantSignature", merchantSignature);
        StringJoiner sj = new StringJoiner("&");
        for(Map.Entry<String, Object> entry : arguments.entrySet())
            sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(entry.getValue()), "UTF-8"));
        byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
        int length = out.length;
        http.setFixedLengthStreamingMode(length);
        http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        http.connect();
        try(OutputStream os = http.getOutputStream()) {
            os.write(out);
            System.out.println(http.getResponseMessage());







        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>(10);
        params.add(new BasicNameValuePair("merchantAccount", merchantAccount));
        params.add(new BasicNameValuePair("merchantDomainName", merchantDomainName));
        params.add(new BasicNameValuePair("orderReference", orderReference));
        params.add(new BasicNameValuePair("orderDate", String.valueOf(orderDate)));
        params.add(new BasicNameValuePair("amount", String.valueOf(amount)));
        params.add(new BasicNameValuePair("currency", currency));
        params.add(new BasicNameValuePair("productName", productName));
        params.add(new BasicNameValuePair("productCount", productCount));
        params.add(new BasicNameValuePair("productPrice", productPrice));
        params.add(new BasicNameValuePair("merchantSignature", merchantSignature));
        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        System.out.println(params.toString());
//Execute and get the response.
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();

        // Read the contents of an entity and return it as a String.
        String content = EntityUtils.toString(entity);
        if (entity != null) {
            InputStream instream = entity.getContent();
            try {
                byte[] targetArray = new byte[instream.available()];
                instream.read(targetArray);
                System.out.println("!!##!#!#!#!#!#!#!#!#!##!#!#!#!#!##!#!#!#!#!\n" + targetArray.toString()
                +"\n" +content);
            } finally {
                instream.close();
            }
        }*/




