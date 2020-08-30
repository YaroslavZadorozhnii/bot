package com.gmail.docfordja.bot;


import com.gmail.docfordja.model.*;
import com.gmail.docfordja.service.ArtService;
import com.gmail.docfordja.service.UserService;
import com.vdurmont.emoji.EmojiParser;
import org.apache.tomcat.jni.Error;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendInvoice;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
import org.telegram.telegrambots.meta.api.objects.payments.SuccessfulPayment;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class BotStateMethods {

    private BotContext context;
    private static BotState botState;
    @Autowired
    private static UserService userService;
    @Autowired
    private static ChatBot chatBot;
    @Autowired
    private static ArtService artService;

    public BotStateMethods() {}

    public BotStateMethods(BotContext context, BotState botState) {
        this.context = context;
        this.botState = botState;
    }

    /*public static int getDuration() {return duration;}

    public static long getNoMus() {return noMus;}

    public static long getIns(){return ins;}

    public static boolean isSendMusicCheck() { return sendMusicCheck;}

    public static void setSendMusicCheck(boolean sendMusicCheck) {BotStateMethods.sendMusicCheck = sendMusicCheck;}
*/
    public static ReplyKeyboardMarkup requestPhone() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup().setOneTimeKeyboard(true);
        List<KeyboardRow> row = new ArrayList<>();
        KeyboardRow bord = new KeyboardRow();
        KeyboardButton kb = new KeyboardButton(EmojiParser.parseToUnicode(":white_check_mark:") + "Начать регистрацию").setRequestContact(true);
        bord.add(kb);
        row.add(bord);
        keyboardMarkup.setKeyboard(row);
        return keyboardMarkup;
    }
    public static InlineKeyboardMarkup inlineMarkupArtist(Map<String, String> artists) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
        for (String string : artists.keySet()) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(string).setCallbackData(artists.get(string));
            keyboardButtonsRow = new ArrayList<>();
            keyboardButtonsRow.add(button);
            rowList.add(keyboardButtonsRow);
        }
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }
    public static InlineKeyboardMarkup inlineMarkup(Map<String, String> buttonText, boolean radio) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
        int num = 0;
        int count = 0;
        for (String string : buttonText.keySet()) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            if(radio) { button.setText(string).setCallbackData(buttonText.get(string));
            }else { button.setText(string).setUrl(buttonText.get(string));}
            count++;
            if (num < 1) {
                keyboardButtonsRow = new ArrayList<>();
            }
            keyboardButtonsRow.add(button);
            num++;
            if (num == 2 || count == buttonText.size()) {
                rowList.add(keyboardButtonsRow);
                num = 0;}
        }
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }
    public static HashMap<String, String> response(String... points){
        HashMap<String, String> map = new HashMap<>();
        for (String point : points){
            map.put(point, point); }
        return map;}

    public static boolean ifCommand(String input){
        boolean check = true;
       String[] command = new String[]{ "тренер", "участник", "\uD83D\uDCDE Информация", "\uD83D\uDCDD Заявки",
                "\uD83D\uDCE1 Коммуникация", "\uD83C\uDF81 Спонсоры","Место проведения","Правила Пилон",
                "Правила Воздух", "Дата мероприятия","\uD83D\uDDD2 Наши контакты","\uD83D\uDCE2 Задать вопрос",
                "Мои заявки","Подать заявку","Соло","Дует","Pole Sport", "Pole Art", "Aerial Hoop",
                "Aerial Silks", "Original Genre","5-6 лет", "7-9 лет", "10-12 лет", "13-14 лет",
                "15-17 лет", "18+ лет", "35+ лет","Начинающие", "Аматоры", "Профессионалы",
                "Профессионалы-Элит","Оплатить", "Начать сначала","Редактировать", "Музыка",
                "Cтраховка", "Видео", "\uD83D\uDD19 Назад", "Заменить трек", "Подать трек", "Проверить трек" };
       for (String s : command){if(input.equals(s) && input != null){check = false;}}
       return check;
    }
    private static int musicRevise(BotContext context){
        User user = context.getUser();
        try {
            Artist artist = ChatBot.findDoubleId(user.getId(), user.getNoMus()).get(0);
            String length = context.getInput().substring(context.getInput().indexOf("howLong?") + "howLong?".length());
            if(Integer.parseInt(length) == 1){
                return -2;
            }
            int duration = Integer.parseInt(length);
            if ("BEGINNER".equals(artist.getLevel().toString())) {
                if (duration <= 151) {return 0;
                } else {return duration - 150;}
            }
            if ("AMATEUR".equals(artist.getLevel().toString())) {
                if (duration <= 181) {return 0;
                } else { return duration - 180;}
            }
            if ("PROFESSIONAL".equals(artist.getLevel().toString()) || "ELITE".equals(artist.getLevel().toString())) {
                if (duration <= 241) {return 0;
                } else return duration - 240;}
        }catch (Exception e){}
      return -1;
    }
    private static String musicPath(BotContext context){
        User user = context.getUser();
        String result = "error";
        Artist artist = ChatBot.findDoubleId(user.getId(), user.getNoMus()).get(0);
        String track = context.getInput();
        if("PoleSport".equals(artist.getView().toString()) && track.indexOf("video") > 0){
            result = "errorSport";
        }else {
        try {
            result = track.substring(track.indexOf("fileId") + 11, track.indexOf("howLong?"));
        }catch (Exception e){
        }}
        return result;
    }

    public static void upArt(BotContext context, String method){
        User user = context.getUser();
        Artist artist = context.getBot().getArt(user);
        if("music".equals(method) && context.getInput().indexOf("MessageId") == 0){
            List<Artist> artists = ChatBot.findDoubleId(user.getId(), user.getNoMus());
                for (Artist a : artists){
                    int duration = musicRevise(context);
                    a.setDuration(duration);
                    String path = musicPath(context);
                    if(!"errorSport".equals(path) && !"error".equals(path) && (duration == 0 || duration == -2)) {
                        a.setMusicPath(context.getInput().substring(context.getInput().indexOf("fileId") + 11,
                                context.getInput().indexOf("howLong?")));
                        a.setMusicMessage(Long.parseLong(context.getInput().substring(
                                context.getInput().indexOf("MessageId") + "MessageId".length(),
                                context.getInput().indexOf("fileId"))));
                        if(duration == -2){a.setSendMusicCheck(true);}
                        a.setDuration(-1);
                    }
                    ChatBot.targetSave(a, context.getUser());
                }
        }
        if("noMusic".equals(method) && ifCommand(context.getInput()) &&
                ChatBot.findDoubleId(context.getUser().getId(), Long.parseLong(context.getInput())).get(0).getOwner() ==
                        context.getUser().getId()){
            user.setNoMus(Long.parseLong(context.getInput()));}
        if("sale".equals(method) && ifCommand(context.getInput()) &&
                ChatBot.getArtist(Long.parseLong(context.getInput())).getOwner() == user.getId()){
            artist.setSale(true);
            artist.setUsername(ChatBot.getArtist(Long.parseLong(context.getInput())).getUsername());
            artist.setFathername(ChatBot.getArtist(Long.parseLong(context.getInput())).getFathername());
            artist.setLastname(ChatBot.getArtist(Long.parseLong(context.getInput())).getLastname());
            artist.setSity(ChatBot.getArtist(Long.parseLong(context.getInput())).getSity());
            artist.setStudio(ChatBot.getArtist(Long.parseLong(context.getInput())).getStudio());
            artist.setAge(ChatBot.getArtist(Long.parseLong(context.getInput())).getAge());
        }
        if("adres".equals(method) && context.getInput().length() > 10 && BotState.byId(context.getUser().getStateId()).
                toString().equals("InsAdress") && ifCommand(context.getInput())){
            artist = ChatBot.getArtist(user.getIns());
            artist.setAdres(context.getInput());
        }
        if("username".equals(method) && ifCommand(context.getInput())) {
            if (artist.getUsername() != null && artist.isDoubles() &&
                    artist.getUsername().indexOf(" / ") < 0) {
                if(!artist.isControl()){
                artist.setUsername(artist.getUsername() + " / " + context.getInput());
                }else {artist.setUsername(context.getInput());
                    artist.setControl(false);}
            } else if(artist.getUsername() != null && artist.isDoubles()  &&
                    artist.getUsername().indexOf(" / ") > 0 && artist.isControl()){
                artist.setUsername(artist.getUsername().substring(0,
                    artist.getUsername().indexOf(" / ")) + " / " + context.getInput());
                    artist.setControl(false);
            }else {artist.setUsername(context.getInput());}
        }
        if("lastname".equals(method) && ifCommand(context.getInput())){
            if (artist.getLastname() != null && artist.isDoubles()  &&
                    artist.getLastname().indexOf(" / ") < 0) {
                if(!artist.isControl()){
                    artist.setLastname(artist.getLastname() + " / " + context.getInput());
                }else {artist.setLastname(context.getInput());
                    artist.setControl(false);}
            } else if(artist.getLastname() != null && artist.isDoubles() &&
                    artist.getLastname().indexOf(" / ") > 0 && artist.isControl()){artist.setLastname(artist.getLastname()
                    .substring(0, artist.getLastname().indexOf(" / ")) + " / " + context.getInput());
                artist.setControl(false);
            }else {artist.setLastname(context.getInput());}
        }
        if ("fathername".equals(method) && ifCommand(context.getInput())){
            if (artist.getFathername() != null && artist.isDoubles() &&
                    artist.getFathername().indexOf(" / ") < 0) {
                if(!artist.isControl()){
                    artist.setFathername(artist.getFathername() + " / " + context.getInput());
                }else {artist.setFathername(context.getInput());
                    artist.setControl(false);}
            } else if(artist.getFathername() != null && artist.isDoubles() &&
                    artist.getFathername().indexOf(" / ") > 0 && artist.isControl()){artist.setFathername(artist.getFathername()
                    .substring(0, artist.getFathername().indexOf(" / ")) + " / " + context.getInput());
                artist.setControl(false);
            }else {artist.setFathername(context.getInput());}
        }
        if("sity".equals(method) && ifCommand(context.getInput())){ artist.setSity(context.getInput());}
        if("studio".equals(method) && ifCommand(context.getInput())){artist.setStudio(context.getInput());}
        if("view".equals(method) && View.stringToView(context.getInput()) != null){
            artist.setView(View.stringToView(context.getInput()));}
        if("age".equals(method) && Age.stringToAge(context.getInput()) != null){
            artist.setAge(Age.stringToAge(context.getInput()));}
        if("level".equals(method) && Level.stringToLevel(context.getInput()) != null){if(user.getRedactor() > 0){
            for(Artist a : ChatBot.findDoubleId(context.getUser().getId(), user.getRedactor())){
                a.setLevel(Level.stringToLevel(context.getInput()));
                ChatBot.updateArt(a);}
        return;}
        artist.setLevel(Level.stringToLevel(context.getInput()));
        }
        ChatBot.targetSave(artist, user);
    }
    private static BotState comeBack(String next, BotContext context) {
        BotState result = BotState.Point;
        if ("ForwardSound".equals(next) || "Redactor".equals(next) || "Insurance".equals(next)) {result = BotState.Part;}
        if ("Ok".equals(next) || "HeartMusic".equals(next) || "MusicRedactor".equals(next) || "SendMusic".equals(next) ||
                "PreSendMusic".equals(next)) {result = BotState.ForwardSound;}
        if ("InsPrePay".equals(next) || "InsAdress".equals(next) || "InsurancePay".equals(next)) {result = BotState.Insurance; }
        if ("Final".equals(next) || "MyLevel".equals(next) || "MyAge".equals(next) ||
                "MyCategory".equals(next) || "Studio".equals(next) || "Sity".equals(next) ||
                "PartisipantFirstName".equals(next) || "PartisipantFathername".equals(next) || "Pay".equals(next) ||
                "PartisipantName".equals(next) || "SendApplicationPerformans".equals(next) || "NextRequest".equals(next)) {
            Artist a = ChatBot.getArt(context.getUser());
            a.setDoubles(false);
            a.setView(null);
            a.setLevel(null);
            a.setUsername(null);
            a.setFathername(null);
            a.setLastname(null);
            a.setSity(null);
            a.setStudio(null);
            a.setAge(null);
            ChatBot.targetSave(a, context.getUser());
            result = BotState.RequestVariant;}
        if("Part".equals(next) || "RequestVariant".equals(next)){ result = BotState.Request;}
        if("Contacts".equals(next) || "Question".equals(next)){result = BotState.Communication;}
        if("Date".equals(next) || "Place".equals(next) || "RulesAir".equals(next) ||
                "RulesPole".equals(next)){result = BotState.Inform;}
        return result;
    }

    public static BotState nextBotState(BotContext context) {
        BotState next = BotState.byId(context.getUser().getStateId());
        BotState result = next;
        Artist artist = ChatBot.getArt(context.getUser());
        User user = context.getUser();
       // if(ChatBot.getArt(context.getUser()) != null){ artist = ChatBot.getArt(context.getUser());}
        if(!next.toString().equals("HeartMusic") && !next.toString().equals("MusicRedactor") && !next.toString().equals("SendMusic") &&
                !next.toString().equals("ErrorVideo") && !next.toString().equals("ErrorLength")&&!next.toString().equals("MusicSuccess")){
            artist.setDuration(-1);}
        if(!next.toString().equals("Insurance") && !next.toString().equals("InsPrePay") && !next.toString().equals("InsAdress")&&
                !next.toString().equals("InsurancePay")){user.setIns( -1);}
        if("\uD83D\uDD19 Назад".equals(context.getInput())){return comeBack(next.toString(), context);}
        if(next.toString().equals(BotState.InsurancePay.toString())){
            if(ChatBot.getArtist(user.getIns()).isInsurance()){
                user.setIns(-1);
                result = BotState.InsFin;}
        }
        if("Проверить трек".equals(context.getInput()) && "ForwardSound".equals(next.toString())){result = BotState.HeartMusic;}
        if("Заменить трек".equals(context.getInput()) && "ForwardSound".equals(next.toString())){result = BotState.MusicRedactor;}
        if(user.getNoMus() > 0 && "SendMusic".equals(next.toString())){
            Artist a = ChatBot.findDoubleId(user.getId(), user.getNoMus()).get(0);
            if(a.getDuration() > 0 ){return BotState.ErrorLength;}
            if(context.getInput().equals("null")){return BotState.MusicError;}
            if(a.getMusicPath() == null){return BotState.ErrorVideo;}
            result = BotState.MusicSuccess;}
        if("MusicRedactor".equals(next.toString()) && ifCommand(context.getInput())){result = BotState.SendMusic;}
        if("PreSendMusic".equals(next.toString()) && user.getNoMus() > 0 && ifCommand(context.getInput())){result = BotState.SendMusic;}
        if("Подать трек".equals(context.getInput()) && next.toString().equals(BotState.ForwardSound.toString())){
            result = BotState.PreSendMusic;}
        if("Застраховать".equals(context.getInput()) && next.toString().equals(BotState.Insurance.toString()) &&
                ifCommand(context.getInput())){
        result = BotState.InsPrePay;}
        if(next.toString().equals(BotState.InsPrePay.toString())){
            result = BotState.Point;
            if(!"\uD83D\uDD19 Назад".equals(context.getInput()) && ChatBot.getArtist(Long.parseLong(context.getInput())).getOwner() == context.getUser().getId() &&
            !ChatBot.getArtist(Long.parseLong(context.getInput())).isInsurance()){user.setIns(Long.parseLong(context.getInput()));
        result = BotState.InsAdress;}
        }
        if(next.toString().equals(BotState.InsAdress.toString()) && ifCommand(context.getInput()) &&
                ChatBot.getArtist(user.getIns()).getAdres() != null){
        result = BotState.InsurancePay;}
        if(next.toString().equals(BotState.Redactor.toString()) && ifCommand(context.getInput()) &&
                ChatBot.getArtist(Long.parseLong(context.getInput())).getOwner() == context.getUser().getId()){
            user.setRedactor(Long.parseLong(context.getInput()));
        result = BotState.MyLevel;}
        if("Музыка".equals(context.getInput()) && next.toString().equals(BotState.Part.toString())){result = BotState.ForwardSound;}
        if("Cтраховка".equals(context.getInput()) && next.toString().equals(BotState.Part.toString())){result = BotState.Insurance;}
        if("Редактировать".equals(context.getInput()) && next.toString().equals(BotState.Part.toString())){ result = BotState.Redactor;}
        if("Оплатить".equals(context.getInput()) && next.toString().equals(BotState.Pay.toString())){result = BotState.Final;}
        if("Начать сначала".equals(context.getInput()) && next.toString().equals(BotState.Pay.toString())){result = BotState.Request;}
        if("Sity".equals(next.toString()) && ifCommand(context.getInput())){result = BotState.Studio;}
        if("Studio".equals(next.toString()) && ifCommand(context.getInput())){result = BotState.MyCategory;}
        if("MyCategory".equals(next.toString()) && ifEquals(context,"Pole Sport", "Pole Art", "Aerial Hoop",
                "Aerial Silks", "Original Genre")){
            if(artist.isSale() && !artist.isDoubles()){result = BotState.MyLevel;
            }else {result = BotState.MyAge;}}
        if("MyAge".equals(next.toString()) && ifEquals(context, "5-6 лет", "7-9 лет", "10-12 лет", "13-14 лет",
                "15-17 лет", "18+ лет", "35+ лет")){result = BotState.MyLevel;}
        if("MyLevel".equals(next.toString()) && ifEquals(context, "Начинающие", "Аматоры", "Профессионалы",
                "Профессионалы-Элит")){if(user.getRedactor() > 0){/*artist = ChatBot.getArtist(redactor);*/
                user.setRedactor( 0);
                return BotState.Part;}else{
                result = BotState.Pay;}}
        if("PartisipantFirstName".equals(next.toString()) && ifCommand(context.getInput())){
            result = BotState.PartisipantName;}
       if("PartisipantName".equals(next.toString()) && ifCommand(context.getInput())){
            result = BotState.PartisipantFathername;}
        if("PartisipantFathername".equals(next.toString()) && ifCommand(context.getInput())){
            if(artist.isChecks()){result = BotState.DoubleIntro;} else {result =   BotState.Sity;}
        }
        if("Request".equals(next.toString()) || "Point".equals(next.toString())) {
            if("Подать заявку".equals(context.getInput())){
            result =  BotState.RequestVariant;
            }if("Мои заявки".equals(context.getInput())){result = BotState.Part;}}
        if("RequestVariant".equals(next.toString()) && "Подать заявку".equals(context.getInput())){
            result = BotState.SendApplicationPerformans;}
        if("RequestVariant".equals(next.toString()) && "Вторая категория".equals(context.getInput())){
            result = BotState.NextRequest;}
        if("NextRequest".equals(next.toString()) && ifCommand(context.getInput()) &&
                ChatBot.getArtist(Long.parseLong(context.getInput())).getOwner() == context.getUser().getId()){
            result = BotState.SendApplicationPerformans;}
        /*if("Request".equals(next.toString()) /*|| "Point".equals(next.toString()) && "Мои заявки".equals(context.getInput())){
            result =  BotState.Part;}*/
        if("SendApplicationPerformans".equals(next.toString())) {
            if ("Дует".equals(context.getInput())) {
                artist.setDoubles(true);
                artist.setChecks(true);
                if(artist.isSale()){result = BotState.DoubleIntro;
                }else { result = BotState.Intro;}
            } else if ("Соло".equals(context.getInput())) {
                if(artist.isSale()){result = BotState.MyCategory;
                }else { result = BotState.Intro;}}
        }
        if("тренер".equals(context.getInput()) || "участник".equals(context.getInput())){
            if("тренер".equals(context.getInput())){
            user.setRole(true);}
            result =  BotState.Point;}
        if("\uD83D\uDCDE Информация".equals(context.getInput())){result =  BotState.Inform;}
        if("Правила Пилон".equals(context.getInput())){result =  BotState.RulesPole;}
        if("Правила Воздух".equals(context.getInput())){result =  BotState.RulesAir;}
        if("Место проведения".equals(context.getInput())){result =  BotState.Place;}
        if("Дата мероприятия".equals(context.getInput())){result =  BotState.Date;}
        if("\uD83D\uDCDD Заявки".equals(context.getInput())){result =  BotState.Request;}
        if("\uD83D\uDCE1 Коммуникация".equals(context.getInput())){result =  BotState.Communication;}
        if("\uD83D\uDCE2 Задать вопрос".equals(context.getInput())){result =  BotState.Question;}
        if("\uD83D\uDDD2 Наши контакты".equals(context.getInput())){result =  BotState.Contacts;}

        context.getBot().targetSave(artist, context.getUser());
        return result;
    }

    public static void back(BotContext context){
        Map <String, String> map = new HashMap<>();
        map.put("\uD83D\uDD19 Назад", "\uD83D\uDD19 Назад");
        sendKeyboard(context, map, "Вернуться в предыдущее меню");
    }
    public static void sendKeyboard(BotContext context, Map map, String menuName) {
        try {
            context.getBot().execute(new SendMessage().setChatId(context.getUser().getChatId())
                    .setText(menuName)
                    .setReplyMarkup(inlineMarkup(map, true)));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
        public static void sendKeyboardArtist(BotContext context, Map map, String menuName){
            try {
                context.getBot().execute(new SendMessage().setChatId(context.getUser().getChatId())
                        .setText(menuName)
                        .setReplyMarkup(inlineMarkupArtist(map)));
            } catch (TelegramApiException e) { e.printStackTrace();}
        }

    public static int priceMaker(Artist artist){
        int prise = 10000000;
        if(!artist.getAge().toString().equals(Age.ADULT.toString())
                && !artist.getAge().toString().equals(Age.OLD.toString())){prise = 85000;
        }else {prise = 95000;}
        if(artist.isDoubles()){
            prise = 100000;
        if(artist.getAge().toString().equals(Age.ADULT.toString())
                || artist.getAge().toString().equals(Age.OLD.toString())){prise = 120000;}
        }
        if(artist.isSale()){prise -= 10000;}
    return prise;}
    public static String titleMaker(BotContext context){
        Artist artist = context.getBot().getArt(context.getUser());
        String title = "Сольное выступление " + artist.getView();

        if(context.getBot().getArt(context.getUser()).isDoubles()){
            title = "Дуетное выступление " + artist.getView();}
        return title;
    }

    public static SendInvoice sendInvoice(BotContext context){

        SendInvoice invoice = new SendInvoice();
        LabeledPrice price = new LabeledPrice();
        price.setLabel("Art Dance & Aerial Championship");
        price.setAmount(priceMaker(context.getBot().getArt(context.getUser())));
        List<LabeledPrice> priceList = new LinkedList<>();
        priceList.add(price);

        invoice.setChatId(Math.toIntExact(context.getUser().getChatId()))
                .setTitle(titleMaker(context))
                .setDescription(context.getBot().getArt(context.getUser()).toString())
                .setPayload("70")
                .setProviderToken(Utils.getValue("field.info.payment-token"))
                .setStartParameter("start_parameter")
                .setCurrency("UAH")
                .setPrices(priceList)
                .setNeedName(false)
                .setNeedPhoneNumber(false)
                .setNeedEmail(false)
                .setNeedShippingAddress(false)
                .setFlexible(false);
        return invoice;
    }
    public static SendInvoice invoiceIns(User user, Artist artist, String title, int amount){

        SendInvoice invoice = new SendInvoice();
        LabeledPrice price = new LabeledPrice();
        price.setLabel("Страховка на чемпионат");
        price.setAmount(amount);
        List<LabeledPrice> priceList = new LinkedList<>();
        priceList.add(price);

        invoice.setChatId(Math.toIntExact(user.getChatId()))
                .setTitle(title)
                .setDescription(artist.getAdres())
                .setPayload("70")
                .setProviderToken(Utils.getValue("field.info.payment-token"))
                .setStartParameter("start_parameter")
                .setCurrency("UAH")
                .setPrices(priceList)
                .setNeedName(false)
                .setNeedPhoneNumber(false)
                .setNeedEmail(false)
                .setNeedShippingAddress(false)
                .setFlexible(false);
        return invoice;
    }
    private static boolean ifEquals(BotContext context, String... parameters){
        for (int i = 0 ; i < parameters.length ; i++){
            if (context.getInput().equals(parameters[i])){return true;}
        }
        return false;}

    public static Map<String, String> artistMap(List<Artist> artistList){
        Map<String, String> map = new HashMap<>();
        for (Artist a : artistList){map.put(a.getLastname() + " " + a.getUsername() + " " + a.getView() + " "
                 + a.getLevel() , Long.toString(a.getDoubleId()));}
        return map;}

    public static HashMap<String, String> artistsList(User user){
        HashMap<Long, String> list = new HashMap<>();
        for (Artist a : ChatBot.pay(user)) {
            if (!a.isDoubles()) {
                list.put(a.getDoubleId(), a.getLastname() + " " + a.getUsername() + " " +
                        a.getFathername() + " " + a.getView() + " " + Age.ageToString(a.getAge()) + " " +
                        Level.levelToString(a.getLevel()));
            }else {
                if(list.get(a.getDoubleId()) != null){
                    list.put(a.getDoubleId(),list.get(a.getDoubleId()) + a.getLastname() + " " + a.getUsername() + " " +
                            a.getFathername() + " " + a.getView() + " " + Age.ageToString(a.getAge()) + " " +
                            Level.levelToString(a.getLevel()));
                }else {
                    list.put(a.getDoubleId(), a.getLastname() + " " + a.getUsername() + " " +
                            a.getFathername() + " / ");}
            }
        }
        HashMap <String, String> result = new HashMap<>();
        for (Long l : list.keySet()){
            result.put(list.get(l), Long.toString(l));
        }
     return result;}

    public static HashMap<String, String> insList(User user){
        HashMap<String, String> art = new HashMap<>();
        for (Artist a : ChatBot.pay(user)){
            String s = a.getLastname() + " " + a.getUsername() + " " + a.getFathername();
            if(art.get(s) == null){art.put(s, a.getId() + "||" + a.isInsurance());
            }else {
                if(a.isInsurance()){art.put(s, a.getId() + "||" + a.isInsurance());}}
        }
        return art;}

    private static List<Artist> mixer(List<Artist> artists){
        List<Artist> art = artists;
        if(art.size()>1) {
            for (int i = 0; i < art.size() - 1; i++) {
                Artist a = art.get(i);
                for (int j = i + 1; j < art.size(); j++) {
                    Artist b = art.get(j);
                    if (a.getDoubleId() == b.getDoubleId()) {
                        art.remove(j);
                        art.get(i).setLastname(a.getLastname() + " " + a.getUsername() + " / ");
                        art.get(i).setUsername(b.getLastname() + " " + b.getUsername());
                    }
                }
            }
        }
        return art;}

    public static String musicString(User user){
        List<Artist> art = mixer(ChatBot.music(user));
        String result = "Список заявок с музыкальным сопровождением:\n";
        String time = "* Вы еще не подали муз.сопровождение.\n--------------------\n";
        if(art.size() > 0) {
            time = "";
            for (Artist a : art) {time += a.getLastname() + " " + a.getUsername() + " " +
                    a.getView() + " " + a.getLevel() + " " + Age.ageToString(a.getAge()) + "\n--------------------\n";}
        }
        result += time + "Список заявок ожидающих подачи музыкального сопровождения: \n";
        time = "* Все ваши заявки имеют музыкальный трек.";
        art = mixer(ChatBot.noMusic(user));
        if(art.size() > 0) {
            time = "";
            for (Artist a : art) {time += a.getLastname() + " " + a.getUsername() + " " +
                    a.getView() + " " + a.getLevel() + " " + Age.ageToString(a.getAge()) + "\n--------------------\n";}
        }
        result += time;
        return result;}

    public  static List<Artist> noMusicList(User user){return mixer(ChatBot.noMusic(user));}

    public  static List<Artist> musicList(User user){
        return mixer(ChatBot.music(user));}

    public static HashMap<String, String> noMusicMap(List<Artist> noMusicList){
        HashMap<String, String> mus = new HashMap<>();
        for (Artist a : noMusicList){mus.put(a.getLastname() + " " + a.getUsername() + " " + a.getView() + " " +
                Age.ageToString(a.getAge()) + " " + a.getLevel(), Long.toString(a.getDoubleId()));}
    return mus;}
}
