package com.gmail.docfordja.bot;

import com.gmail.docfordja.model.Age;
import com.gmail.docfordja.model.Artist;
import com.gmail.docfordja.model.Level;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.validator.EmailValidator;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

import static com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER;


public class Utils {
    public static final String TOKEN = "1149330908:AAGvUyOopaFD3eaaAlnPtmWsVk8JKmwK4KE";
    public static final String PATH = "https://api.telegram.org/file/bot" + TOKEN;
    public static final String FILE = getValue("field.info.downloadmusic");
    public static final String ACCOUNT_SID = "AC6ba7726a2c8655152b200e5525ca210b";
    public static final String AUTH_TOKEN = "a521b9c24d035fe4ab5ae1477c592577";

    public static boolean isValidEmailAddress(String email) {
        return EmailValidator.getInstance().isValid(email);
    }

    public static boolean sendSMS(String phone) {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        try {
            Message message = Message.creator(
                    new com.twilio.type.PhoneNumber(phoneFormat(phone)),
                    new com.twilio.type.PhoneNumber("+12069845848"),
                    ":)")
                    .create();
        } catch (com.twilio.exception.ApiException apiException) {
            System.out.println("Phone check message: " + apiException.getMessage());
            if (apiException.getMessage().indexOf("messages to unverified numbers") != -1) {
                System.out.println("Valid");
                return true;
            } else {
                System.out.println("Not valid");
                return false;
            }
        }
        return true;
    }
    public static String phoneFormat(String in){
        if (in != null) {
            String result = in.replace("-", "")
                    .replace("(", "")
                    .replace(")", "")
                    .replace("*", "")
                    .replace("#", "")
                    .replace("/", "")
                    .replace("+", "")
                    .replace("!", "")
                    .replace("@", "")
                    .replace("$", "")
                    .replace("^", "")
                    .replace("&", "")
                    .replace(" ", "")
                    .replace("\"", "")
                    .trim();

            if (result.indexOf("0") == 0 && result.length() == 10) {
                result = "+38" + result;
            } else if (result.indexOf("8") == 0 && result.length() == 11) {
                result = "+3" + result;
            } else if (result.indexOf("3") == 0 && result.length() == 12) {
                result = "+" + result;
            }
            System.out.println("result format " + result);
            return result;
        }
        return "02";
    }
    public static String getValue(String key){
        Properties properties = new Properties();
        String result = null;
        File propertyFile = new File("src/main/resources/detals.xml");
        try(FileReader reader = new FileReader(propertyFile)) {
            properties.load(reader);
            result = properties.getProperty(key);
        } catch (FileNotFoundException e) {
            LOGGER.info("ULILS ERROR: PROPERTIES NOT FOUND");
        } catch (IOException e) {
            LOGGER.info("ULILS ERROR: PROPERTIES NOT FOUND");
        }
        return result;
    }
    public static void downloadMusic(Artist a){
        try{
            String isDouble = "\\Solo\\";
            if(a.isDoubles()){isDouble = "\\Doubles\\";}
        String file = FILE + isDouble + a.getView() + "\\" + Age.ageToString(a.getAge())+ "\\" + Level.levelToString(
                a.getLevel()) + "\\" + a.getDoubleId() + "_" + a.getLastname() + " " + a.getUsername() + " " + a.getFathername() ;
        File time = new File(file);
        time.delete();
        time.mkdirs();
       /* time.createNewFile();*/
        InputStream in = new URL(PATH +"/"+ a.getPath()).openStream();
        Files.copy(in, Paths.get(file), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {e.printStackTrace();}
    }
        public static String HMAC_MD5_encode(String message) throws Exception {
            System.out.println("message  " + message);
            SecretKeySpec keySpec = new SecretKeySpec(
                    getValue("merchant.key").getBytes(),
                    "HmacMD5");

            Mac mac = Mac.getInstance("HmacMD5");
            mac.init(keySpec);
            byte[] rawHmac = mac.doFinal(message.getBytes());

            return Hex.encodeHexString(rawHmac);
        }

}