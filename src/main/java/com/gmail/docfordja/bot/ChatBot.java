package com.gmail.docfordja.bot;

import com.gmail.docfordja.model.*;
import com.gmail.docfordja.service.ArtService;
import com.gmail.docfordja.service.UserService;
import com.google.gson.Gson;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.payments.SuccessfulPayment;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.*;

@Component
@PropertySource("classpath:telegram.properties")
public class ChatBot extends TelegramLongPollingBot {
    private static int num = 0;
    private static final Logger LOGGER = LogManager.getLogger(ChatBot.class);

    private static final String BROADCAST = "broadcast ";
    private static final String LIST_USERS = "users";
    private static final String ANSWER = "answerQuery";
    private static final String OFFER = "startoffer";
    private static long messageId;
    private static ArtService artService;
    @Value("${bot.name}")
    private String botName;

    @Value("${bot.token}")
    private String botToken;

    private static UserService userService;

    public ChatBot(ArtService artService, UserService userService) {
        this.artService = artService;
        this.userService = userService;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    public static long getMessageId() {return messageId;}
    public static void setMessageId(long messageId) {ChatBot.messageId = messageId;}

    @Override
    public void onUpdateReceived(Update update) {
        System.out.println("Update " + update);
        BotContext context;
        BotState state;
        Map<String, Object> response = responseUpdate(update);
        final String text = adapter(response);
        final long chatId = (long) response.get("id");
        System.out.println(";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;   " + chatId);
        User user = userService.findByChatId(chatId);

        if (checkIfAdminCommand(user, text))
            return;


        if (user == null) {
            state = BotState.getInitialState();
            user = new User(chatId, state.ordinal(),(String) response.get("username"));
            //artist = new Artist(user);
            //artist.setNum(num);
            userService.addUser(user);
            //artService.addArtist(artist);
           /* if(user.getArtists().size() == 0){
            user.getArtists().add(artService.findById(user.getId()).get(artService.findById(user.getId()).size() -1));}*/
            context = BotContext.of(this, user, text);
            state.enter(context);

            LOGGER.info("New user registered: " + chatId);
        } else {
            if(response.get("phone") != null){
                user.setPhone((String) response.get("phone"));}
           // Artist artist = new Artist(user);
           /* try { user.getArtists().clear();
                for (Artist a : artService.findById(user.getId())){user.getArtists().add(a);}
            }catch (Exception e){}
           /* //System.out.println(artist.toString());
            context = BotContext.of(this, user, text);
            state = BotState.byId(user.getStateId());
            artist = artService.findById(user.getId()).get(artService.findById(user.getId()).size() -1);
            artist.setNum(num);
            artService.updateArtist(artist);
            user.setArtists(artService.findById(user.getId()));
            LOGGER.info("Update received for user in state: " + state);*/
            String time = text;

            context = BotContext.of(this, user, time);
            state = BotState.byId(user.getStateId());
            // addArtist(context, text);
            LOGGER.info("Update received for user in state: " + state);
        }
        if(text.equals(OFFER)){
            BotStateMethods botStateMethods = new BotStateMethods(context, state);
            sendOffer();
            return;}
        if(text.equals(ANSWER)){
            SendMessage mes = new SendMessage();
            AnswerPreCheckoutQuery answer = new AnswerPreCheckoutQuery();
            try { context.getBot().execute(new AnswerPreCheckoutQuery().setOk(true)
                    .setPreCheckoutQueryId((String) response.get("ShipId")));
                return;
            } catch (TelegramApiException e) { e.printStackTrace();}
        }

        state.handleInput(context);

        do {
            state = state.nextState();
            state.enter(context);
        } while (!state.isInputNeeded());
        user.setStateId(state.ordinal());
        try{System.out.println("artistcontrol: " + getArt(user).toString() + "\n State: " + state);}catch (Exception q){}
        try{userService.updateUser(user);}catch (Exception e){}

        }


    public void sendOffer(){
        List<User> users = userService.findAllUsers();
        String[] numbers = Utils.getValue("field.info.sponsor.count").split(",");
        for(User user : users) {
            for (String number : numbers) {
                Map<String, String> map = new HashMap<>();
                map.put(Utils.getValue("field.info.sponsor" + number + ".name"),
                        Utils.getValue("field.info.sponsor" + number + ".offerpath"));
                try {sendPhotoWithText(user.getChatId(), Utils.getValue("field.info.sponsor" + number + ".path"),
                            Utils.getValue("field.info.sponsor" + number + ".name") + "\n" +
                                    Utils.getValue("field.info.sponsor" + number + ".text"), this);
                             execute(new SendMessage()
                            .setReplyMarkup(BotStateMethods.inlineMarkup(map, false)).setText("Для детального ознакомления" +
                                    " нажмите на кнопку.")
                            .setChatId(user.getChatId()));
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static void sendPhotoWithText(long chatId, String path, String text, ChatBot bot) {
        try (InputStream is = ChatBot.class.getClassLoader().getResourceAsStream(path)){
            String name = path.substring(path.indexOf("/") + 1);
            name = name.substring(0, name.length() - 4);
            System.out.println(name);
            SendPhoto message = new SendPhoto()
                    .setChatId(chatId)
                    .setPhoto(name, is)
                    .setCaption(text);
            bot.execute(message);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private boolean checkIfAdminCommand(User user, String text) {
        if (user == null || !user.getAdmin())
            return false;

        if (text.startsWith(BROADCAST)) {
            LOGGER.info("Admin command received: " + BROADCAST);

            text = text.substring(BROADCAST.length());
            broadcast(text);

            return true;
        } else if (text.equals(LIST_USERS)) {
            LOGGER.info("Admin command received: " + LIST_USERS);

            listUsers(user);
            return true;
        }

        return false;
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage()
                .setChatId(chatId)
                .setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendPhoto(long chatId) {
        InputStream is = getClass().getClassLoader()
                .getResourceAsStream("test.png");

        SendPhoto message = new SendPhoto()
                .setChatId(chatId)
                .setPhoto("test", is);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void listUsers(User admin) {
        StringBuilder sb = new StringBuilder("All users list:\r\n");
        List<User> users = userService.findAllUsers();

        users.forEach(user ->
            sb.append(user.getId())
                    .append(' ')
                    .append(user.getPhone())
                    .append(' ')
                    .append(user.getEmail())
                    .append("\r\n")
        );

        sendPhoto(admin.getChatId());
        sendMessage(admin.getChatId(), sb.toString());
    }
    public static void photoRequest(long chatId, String path, String text, ChatBot bot) {
            SendPhoto message = new SendPhoto()
                    .setChatId(chatId)
                    .setPhoto(path)
                    .setCaption(text);
            try {bot.execute(message);
        } catch (Exception e) {}
    }

    private void broadcast(String text) {
        if (text.startsWith("$$")) {
            try {
                String[] result = text.substring(2, text.length()).split("!!");
                System.out.println(Arrays.toString(result));
                photoRequest(userService.findById(Long.parseLong(result[1])).getChatId(), result[0], result[2], this);
            } catch (Exception e) {
            }
        } else if (text.startsWith("??")) {
            try {
                String[] result = text.substring(2, text.length()).split("!!");
                System.out.println(userService.findById(Long.parseLong(result[0])).getChatId() + " " + result[1]);
                sendPhotoWithText(userService.findById(Long.parseLong(result[0])).getChatId(), Utils.getValue(
                        "field.info.info"), result[1], this);
            } catch (Exception e) {}
        } else if (text.indexOf("########") >= 0) {
            String[] result = text.split("########");
            List<User> users = userService.findAllUsers();
            users.forEach(user -> photoRequest(user.getChatId(), result[1], result[0], this));
        } else {
            List<User> users = userService.findAllUsers();
            users.forEach(user -> sendPhotoWithText(user.getChatId(), Utils.getValue("field.info.info"),
                    text, this));
        }
    }

    private static Map<String, Object> responseUpdate(Update update){
        try {messageId = update.getMessage().getMessageId();}catch (Exception e){}
        Gson gson = new Gson();
        Map<String, Object> list = new HashMap<>();
        String responseText = null;
        long responseID = -1;
        String resnseCommand = null;
        String userDetals = null;
        String userName = null;
        try {
            if (BotState.byId(userService.findByChatId(update.getMessage().getChatId()).getStateId())
                    .toString().equals("Question") && update.getMessage().hasPhoto()) {
                responseID = Long.parseLong(gson.toJson(update.getMessage().getChat().getId()));
                responseText = "!!!!!#####!!!!!"+update.getMessage().getPhoto().get(0).getFileId() +
                         "!!!!!#####!!!!!" + update.getMessage().getCaption();
                list.put("text", responseText);
                list.put("id", responseID);
                return list;
            }
        }catch (Exception e){}
        try {
            if(update.getMessage().isReply()){
                responseID = Long.parseLong(gson.toJson(update.getMessage().getChat().getId()));
                String[] result = new String[]{};
                if(update.getMessage().hasPhoto()){
                    if(update.getMessage().getReplyToMessage().hasPhoto()){
                        result = update.getMessage().getReplyToMessage().getCaption().split("\n");
                    }else {result = update.getMessage().getReplyToMessage().getText().split("\n");}
                    responseText = "broadcast $$"+update.getMessage().getPhoto().get(0).getFileId()+ "!!" +
                            result[0] + "!!" + "Ответ на ваше обращение:\n\n" + result[2] + "\n\n * * *\n"
                            + "\n" + update.getMessage().getCaption();
                }else {
                    if (update.getMessage().getReplyToMessage().hasPhoto()) {
                        result = update.getMessage().getReplyToMessage().getCaption().split("\n");
                    } else {result = update.getMessage().getReplyToMessage().getText().split("\n");}
                        responseText = "broadcast ??" + result[0] + "!!" + "Ответ на ваше обращение:\n\n" + result[2] + "\n\n * * *\n"
                                + "\n" + update.getMessage().getText();}
                list.put("text", responseText);
                list.put("id", responseID);
                return list;
            }
        }catch (Exception e){}
        try {
        if(update.getMessage().hasPhoto() &&  update.getMessage().getCaption().startsWith("broadcast")) {
            list.put("id", Long.parseLong(gson.toJson(update.getMessage().getChat().getId())));
            list.put("text", update.getMessage().getCaption() + "########" + update.getMessage().getPhoto().get(0).getFileId());
            return list;
        }
        }catch (Exception e){}
        try {
            if (BotState.byId(userService.findByChatId(update.getMessage().getChatId()).getStateId())
                    .toString().equals("SendMusic") && update.getMessage().hasDocument()) {
                String fileId = null;
                responseText = String.valueOf(update.getMessage().getMessageId());
                responseID = Long.parseLong(gson.toJson(update.getMessage().getChat().getId()));
                if (update.getMessage().getDocument().getMimeType().indexOf("video") >= 0) {
                    fileId = "video" + update.getMessage().getDocument().getFileId() + "howLong?" + "1";
                    list.put("text", "MessageId" + responseText + "fileId" + fileId);
                    list.put("id", responseID);
                    return list; }
                if (update.getMessage().getDocument().getMimeType().indexOf("audio") >= 0) {
                    fileId = "audio" + update.getMessage().getDocument().getFileId() + "howLong?" + "1";
                    list.put("text", "MessageId" + responseText + "fileId" + fileId);
                    list.put("id", responseID);
                    return list; }
            }
        }catch (Exception e){}
        try {
            if(BotState.byId(userService.findByChatId(update.getMessage().getChatId()).getStateId())
                    .toString().equals("SendMusic") &&
                    update.getMessage().hasAudio() || update.getMessage().hasVideo()){
                System.out.println("here!");
                String fileId = null;
                if(update.getMessage().hasAudio()){ fileId ="audio" + update.getMessage().getAudio().getFileId() + "howLong?" +
                        update.getMessage().getAudio().getDuration();
                    System.out.println("audio!");}
                if(update.getMessage().hasVideo()){
                    fileId = "video" + update.getMessage().getVideo().getFileId() + "howLong?" +
                        update.getMessage().getVideo().getDuration();
                    System.out.println("video! " + fileId);
                }
                responseText = String.valueOf(update.getMessage().getMessageId());
                responseID = Long.parseLong(gson.toJson(update.getMessage().getChat().getId()));
                list.put("text", "MessageId" + responseText + "fileId" + fileId);
                list.put("id", responseID);
                return list;
            }
        }catch (Exception e){}
        if(update.hasPreCheckoutQuery()){
            System.out.println(gson.toJson(update.getPreCheckoutQuery()));
            responseText = "answerQuery";
            userDetals = gson.toJson(update.getPreCheckoutQuery().getId());
            responseID = Long.parseLong(gson.toJson(update.getPreCheckoutQuery().getFrom().getId()));
            list.put("text", responseText);
            list.put("id", responseID);
            list.put("ShipId", userDetals.replace("\"", ""));
        }
        try {
            System.out.println(gson.toJson(update.getPreCheckoutQuery()));
        }catch (Exception e){}
        try {
            userName = gson.toJson(update.getMessage().getChat().getFirstName());
            LOGGER.info("Username is input");
            list.put("username", userName.replace("\"", ""));
        }catch (Exception e){}
        try {
            responseText = gson.toJson(update.getMessage().getText());
            responseID = Long.parseLong(gson.toJson(update.getMessage().getChat().getId()));
            list.put("text", responseText.replace("\"", ""));
            list.put("id", responseID);
            LOGGER.info("Simple text is input: " + list.get("text"));
        }catch (Exception e){}
        try {
            resnseCommand = gson.toJson(update.getCallbackQuery().getData());
            responseID = Long.parseLong(gson.toJson(update.getCallbackQuery().getFrom().getId()));
            list.put("command", resnseCommand.replace("\"", ""));
            list.put("id", responseID);
            LOGGER.info("Command received: " + list.get("command"));
        }catch (Exception e){}
        try {
            userDetals = gson.toJson(update.getMessage().getContact().getPhoneNumber());
            responseID = Long.parseLong(gson.toJson(update.getMessage().getChat().getId()));
            list.put("phone", userDetals.replace("\"", ""));
            list.put("id", responseID);
            LOGGER.info("User phone is input: " + list.get("phone"));
        }catch (Exception e){}
        try {
            if(update.getMessage().getSuccessfulPayment() != null) {
                responseText = "Payment " + update.getMessage().getSuccessfulPayment().getProviderPaymentChargeId();
                responseID = Long.parseLong(gson.toJson(update.getMessage().getChat().getId()));
                list.put("text", responseText);
                list.put("id", responseID);
                User user = userService.findByChatId(responseID);
                Artist artist = getArt(user);
                if (user.getIns() > 0) {
                    artist = getArtist(user.getIns());
                    artist.setInsurance(true);
                    targetSave(artist, user);
                } else {
                    artist.setPay(true);
                    artist.setDoubleId(artist.getId());
                    targetSave(artist, user);
                    if (artist.isDoubles()) {
                        Artist doubleA = new Artist(user);
                        doubleA.setPay(true);
                        doubleA.setLevel(artist.getLevel());
                        doubleA.setView(artist.getView());
                        doubleA.setAge(artist.getAge());
                        doubleA.setSity(artist.getSity());
                        doubleA.setStudio(artist.getStudio());
                        doubleA.setDoubles(true);
                        String[] time = artist.getUsername().split(" / ");
                        doubleA.setUsername(time[1]);
                        artist.setUsername(time[0]);
                        time = artist.getFathername().split(" / ");
                        doubleA.setFathername(time[1]);
                        artist.setFathername(time[0]);
                        time = artist.getLastname().split(" / ");
                        doubleA.setLastname(time[1]);
                        artist.setLastname(time[0]);
                        doubleA.setDoubleId(artist.getId());
                        artist.setDoubleId(doubleA.getDoubleId());
                        targetSave(artist, user);
                        targetSave(doubleA, user);
                    }
                    Artist newArtist = new Artist(user);

                    artService.addArtist(newArtist);
                    user.getArtists().add(artService.getLastArtist(user));
                    userService.updateUser(user);
                    LOGGER.info("Payment: " + list.get("text"));
                    System.out.println("Succes!!!!!");
                }
            }
        }catch (Exception e){}
        try {
            if(BotState.byId(Math.toIntExact(userService.findByChatId(update.getMessage().getChatId()).getId())).toString().equals("ForwardSound") &&
                    update.getMessage().hasAudio() || update.getMessage().hasAudio()){
                responseText = String.valueOf(update.getMessage().getMessageId());
                responseID = Long.parseLong(gson.toJson(update.getMessage().getChat().getId()));
                list.put("text", responseText);
                list.put("id", responseID);
                return list;
            }
        }catch (Exception e){}
        return list;
    }
    public String adapter(Map<String, Object> map){
        if(map.get("command") != null){return (String) map.get("command");
        }else {return (String) map.get("text");}
    }
   /* public void addArtist(BotContext context, String text) {
        List<Artist> artists = artService.findById(context.getUser().getId());
        for (Artist artist : artists) {
            if ("PartisipantName".equals(BotState.thisState().toString())) {
                if (artist.isDoubles()) {
                    artist.setUsername(artist.getUsername() + " \\ " + text);
                } else {
                    artist.setUsername(text);
                }
            }
            if ("PartisipantFathername".equals(BotState.thisState().toString())) {
                if (artist.isDoubles()) {
                    artist.setFathername(artist.getFathername() + " \\ " + text);
                } else {
                    artist.setFathername(text);
                }
            }
            if ("PartisipantFirstName".equals(BotState.thisState().toString())) {
                if (artist.isDoubles()) {
                    artist.setLastname(artist.getLastname() + " \\ " + text);
                } else {
                    artist.setLastname(text);
                }
            }
            if ("Sity".equals(BotState.thisState().toString())) {
                artist.setSity(text);
            }
            if ("Studio".equals(BotState.thisState().toString())) {
                artist.setStudio(text);
            }
            if ("SendApplicationPerformans".equals(BotState.thisState().toString())) {
                if (text.equals("Дует")) {
                    artist.setDoubles(true);
                }
            }
            if ("MyAge".equals(BotState.thisState().toString())) {
                artist.setAge(Age.stringToAge(text));
            }
            if ("MyLevel".equals(BotState.thisState().toString())) {
                artist.setLevel(Level.stringToLevel(text));
            }
            if ("MyCategory".equals(BotState.thisState().toString())) {
                artist.setView(View.stringToView(text));
            }
            if ("Pay".equals(BotState.thisState().toString())) {
                System.out.println("THIS PAY: " + text);
                artist.setPay(true);
            }
            artService.addArtist(artist);
            userService.updateUser(context.getUser());
        }
    }*/
    /*public boolean isArtistOpen(User user, String text){
        System.out.println("Bot state is: " + BotState.thisState().toString() + " " + text);
            if(BotState.thisState().toString().equals("PartisipantName")){
                Artist artist = new Artist(user);
                artist.setUser(user);
                artist.setUsername(text);
                targetSave(artist, user);
                return true;
            }if(BotState.thisState().toString().equals("PartisipantFirstName")){
               Artist artist = user.getArtists().get(user.getArtists().size() -1);
               artist.setFathername(text);
               targetSave(artist, user);
        }

            return false;
    }*/
    public static void targetSave(Artist artist, User user){
        System.out.println("targetSave >>> >>> >>> >>> artist: " + artist.getId() + " user: " + user.getId());
        artService.updateArtist(artist);
        userService.updateUser(user);
        System.out.println("targetSave: " + artService.findById(artist.getId()));
    }
    public static Artist getArt(User user){return artService.working(user);}//getLastArtist(user);}
    public static void updateArt(Artist artist){
        artService.updateArtist(artist);
    }
    public static User findById(long id){return userService.findById(id);}
    public static List<Artist> pay(User user){return artService.findByPay(user.getId());}
    public static Artist getArtist(Long id){ return artService.byId(id);}
    public static List<Artist> findDoubleId(Long id, Long did){return artService.findByDoubleId(id, did);}
    public static void updateLevel(Level level, long did, long id){artService.updateLevel(level, did, id);}
    public static List<Artist> music(User user){return artService.findIsMusic(user.getId());}
    public static List<Artist> noMusic(User user){return artService.findIsNoMusic(user.getId());}
    public static User getAdmin(){
        for (User u : userService.findAllUsers()){if(u.getAdmin()){return u;}}
        return null;
    }
}
