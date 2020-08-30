package com.gmail.docfordja.bot;

import com.gmail.docfordja.model.Artist;
import com.gmail.docfordja.model.RequestOrder;
import com.gmail.docfordja.model.User;
import com.liqpay.LiqPay;
import com.sun.crypto.provider.HmacMD5;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendLocation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVenue;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import sun.security.provider.MD5;

import javax.xml.crypto.dsig.spec.HMACParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static org.apache.commons.io.FileUtils.checksum;

public enum BotState {

    Start {
        String hello = "Привет, ";
        private BotState next;

        @Override
        public void enter(BotContext context) {
            String name = context.getUser().getUsername() + "!";
            if (!"Привет, ".equals(hello)) {
                name = ".";
            }
            ReplyKeyboardMarkup markup = methods.requestPhone();
            try {context.getBot().execute(new SendMessage().setText(hello + name).setChatId(context.getUser()
                        .getChatId()).setReplyMarkup(markup));
                if("Hажми кнопку \"Начать регистрацию\" чтоб продолжить".equals(hello)){
                sendMessage(context, "Организатор обязуеться использовать ваш томер телефона только для связи с вами" +
                        " и не передавать его третьим лицам.");}
            } catch (TelegramApiException e) {}
        }

        @Override
        public void handleInput(BotContext context) {
            hello = "Hажми кнопку \"Начать регистрацию\" чтоб продолжить";
            if (context.getUser().getPhone() != null) {
                next = EnterEmail;
            } else {
                next = Start;
            }
        }

        @Override
        public BotState nextState() {
            return next;
        }
    },

    EnterPhone {
        private BotState next;

        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Ваш контактный мобильный номер телефона:");
        }

        @Override
        public void handleInput(BotContext context) {
            boolean send = Utils.sendSMS(Utils.phoneFormat(context.getInput()));
            if (send) {
                context.getUser().setPhone2(Utils.phoneFormat(context.getInput()));
                sendMessage(context, "Ваш номер записан: " + context.getUser().getPhone2());
                next = EnterEmail;
            } else {
                sendMessage(context, "Проверьте правильность ввода и повторите попытку.");
                sendMessage(context, "Вы ввели: " + context.getInput());
                next = EnterPhone;
            }
        }

        @Override
        public BotState nextState() {
            return next;
        }
    }, EnterEmail {
        private BotState next;

        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Важные уведомления о событиях чемпионата вы будете получать по почте.");
            sendMessage(context, "Введите, пожалуйста, ваш email :");
        }

        @Override
        public void handleInput(BotContext context) {
            if (Utils.isValidEmailAddress(context.getInput())) {
                context.getUser().setEmail(context.getInput());
                sendMessage(context, "Ваш email записан: " + context.getUser().getEmail());
                next = ChoiseUser;
            } else {
                sendMessage(context, "Вы ввели неправильный email: " + context.getInput());
                next = EnterEmail;}
        }

        @Override
        public BotState nextState() {
            return next;
        }
    }, ChoiseUser {
        private BotContext myContext;

        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Вы хотите продолжить регистрацию как тренер, или как участник соревнований?");
            Map<String, String> choise = new HashMap<>();
            choise.put("\uD83D\uDCAA Я тренер", "тренер");
            choise.put("\uD83C\uDFC6 Я буду выступать", "участник");
            methods.sendKeyboard(context, choise, "\uD83C\uDFAF Цель вашей регистрации: ");
        }

        @Override
        public void handleInput(BotContext context) {
            sendMessage(context, "Вы: " + context.getInput());
            myContext = context;
        }

        @Override
        public BotState nextState() {
            return methods.nextBotState(myContext);
        }

    }, Point {
        private BotContext myContest;

        @Override
        public void enter(BotContext context) {
            Map<String, String> map = methods.response("\uD83D\uDCDE Информация", "\uD83D\uDCDD Заявки",
                    "\uD83D\uDCE1 Коммуникация");
            methods.sendKeyboard(context, map, "\uD83E\uDDED" + " Навигация");
        }

        @Override
        public void handleInput(BotContext context) {
            myContest = context;
            sendMessage(context, "Вы выбрали: " + context.getInput());
        }

        @Override
        public BotState nextState() {
            return methods.nextBotState(myContest);
        }

    }, Inform {
        private BotContext myContext;

        @Override
        public void enter(BotContext context) {
            Map<String, String> response = methods.response("Место проведения", "Правила Пилон",
                    "Правила Воздух", "Дата мероприятия");
            methods.sendKeyboard(context, response, "\uD83D\uDCDE Информация");
            methods.back(context);
        }

        @Override
        public void handleInput(BotContext context) {
            sendMessage(context, "Вы выбрали: " + context.getInput());
            myContext = context;
        }

        @Override
        public BotState nextState() {
            return methods.nextBotState(myContext);
        }

    }, RulesPole {
        private BotContext myContext;

        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Кликлите по ссылке, чтоб открыть документ \uD83D\uDC47");
            sendMessage(context, Utils.getValue("field.info.rulespole"));
            methods.back(context);
        }

        @Override
        public void handleInput(BotContext context) {
            myContext = context;
        }

        @Override
        public BotState nextState() {
            return methods.nextBotState(myContext);
        }
    }, RulesAir {
        private BotContext myContext;

        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Кликлите по ссылке, чтоб открыть документ \uD83D\uDC47");
            sendMessage(context, Utils.getValue("field.info.rulesaerial"));
            methods.back(context);
        }

        @Override
        public void handleInput(BotContext context) {
            myContext = context;
        }

        @Override
        public BotState nextState() {
            return methods.nextBotState(myContext);
        }

    }, Place {
        private BotContext myContext;

        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Кликлите по ссылке, чтоб открыть карту \uD83E\uDDED");
            sendMessage(context, Utils.getValue("field.info.place"));
            sendMessage(context, new String(Utils.getValue("field.info.address").getBytes(), StandardCharsets.UTF_8));
            methods.back(context);
        }

        @Override
        public void handleInput(BotContext context) {
            myContext = context;
        }

        @Override
        public BotState nextState() {
            return methods.nextBotState(myContext);
        }
        //ЗДЕСЬ ПОЛЬЗОВАТЕЛЮ ОТПРАВЯТ ДАТУ ПРОВЕДЕНИЯ
    }, Date {
        private BotContext myContext;

        @Override
        public void enter(BotContext context) {
            sendMessage(context, Utils.getValue("field.info.date"));
            methods.back(context);
        }

        @Override
        public void handleInput(BotContext context) {
            myContext = context;
        }

        @Override
        public BotState nextState() {
            return methods.nextBotState(myContext);
        }
        //МЕНЮ КОММУНИКАЦИЙ
    }, Communication {
        private BotContext myContext;

        @Override
        public void enter(BotContext context) {
            Map<String, String> response = BotState.methods.response("\uD83D\uDDD2 Наши контакты", "\uD83D\uDCE2 Задать вопрос");
            methods.sendKeyboard(context, response, "\uD83D\uDCE1 Коммуникация");
            methods.back(context);
        }

        @Override
        public void handleInput(BotContext context) {
            sendMessage(context, "Вы выбрали: " + context.getInput());
            myContext = context;
        }

        @Override
        public BotState nextState() {
            return methods.nextBotState(myContext);
        }
        //ЗДЕСЬ ПОЛЬЗОВАТЕЛЮ ОТПРАВЯТ КОНТАКТЫ
    }, Contacts {
        private BotContext myContext;

        @Override
        public void enter(BotContext context) {
            sendMessage(context, Utils.getValue("field.info.contacts.phone"));
            sendMessage(context, Utils.getValue("field.info.contacts.Viber"));
            sendMessage(context, Utils.getValue("field.info.contacts.Instagram"));
            sendMessage(context, Utils.getValue("field.info.contacts.Facebook"));
            methods.back(context);
        }

        @Override
        public void handleInput(BotContext context) {
            myContext = context;
        }

        @Override
        public BotState nextState() {
            return methods.nextBotState(myContext);
        }
        //ЗДЕСЬ ПОЛЬЗОВАТЕЛЬ МОЖЕТ ЗАДАТЬ ВОПРОС
    }, Question {
        private BotContext myContext;

        @Override
        public void enter(BotContext context) {
            ChatBot.sendPhotoWithText(context.getUser().getChatId(), Utils.getValue("field.info.question"),
                    "\uD83D\uDCDD Напишите свой вопрос и мы ответим вам в телеграмм в ближайщее время. \n" +
                            "Если вы хотите прикрепить фото, то нажмите на изображение скрепки внизу эрана, выберите " +
                            "нужное вам фото и пишите свой вопрос в описании к фото, как показано на этой картинке." +
                            "Если хотите вернуться нажмите \"\uD83D\uDD19 Назад\".", context.getBot() );
            /*sendMessage(context,
                    "\uD83D\uDCDD Напишите свой вопрос и мы ответим вам в телеграмм в ближайщее время. \n" +
                            "Или нажмите \"\uD83D\uDD19 Назад\" чтоб отменить действие");*/
            methods.back(context);
        }

        @Override
        public void handleInput(BotContext context) {
            myContext = context;
            if (methods.ifCommand(context.getInput()) && !context.getInput().substring(context.getInput().indexOf(":") +
                    1).equals("null")) {
                sendMessage(context, "Спасибо за ваш вопрос! Мы обработаем его и сразу же свяжемся с вами");
                if (context.getInput().startsWith("!!!!!#####!!!!!")) {
                    String[] result = context.getInput().split("!!!!!#####!!!!!");
                    ChatBot.photoRequest(ChatBot.getAdmin().getChatId(), result[1], context.getUser().getId() + "\n" +
                            context.getUser().getPhone() + ": \n" + result[2], context.getBot());
                } else {
                    try {
                        context.getBot().execute(new SendMessage().setChatId(ChatBot.getAdmin().getChatId())
                                .setText(context.getUser().getId() + "\n" + context.getUser().getPhone() + ": \n" +
                                        context.getInput()));
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }else if(!"\uD83D\uDD19 Назад".equals(context.getInput())) {sendMessage(context, "К сожалению при отправке вашего вопроса произошла ошибка." +
                    "В вопросах можно использовать текст, или фото с текстовым описанием. Попробуйте еще раз.");}
        }

        @Override
        public BotState nextState() {
            return Point;
        }
        //МЕНЮ ЗАЯВОК
    }, Request {
        private BotContext myContext;

        public void enter(BotContext context) {
            Map<String, String> response = methods.response("Мои заявки", "Подать заявку");
            methods.sendKeyboard(context, response, "\uD83D\uDCDD Заявки");
            methods.back(context);
        }

        @Override
        public void handleInput(BotContext context) {
            sendMessage(context, "Вы выбрали: " + context.getInput());
            myContext = context;
        }

        @Override
        public BotState nextState() {
            return methods.nextBotState(myContext);
        }
        },RequestVariant {
        private BotContext myContext;

        public void enter(BotContext context) {
            sendMessage(context, "Если у участника еще нет заявок на участие в чемпионате выберите \"Подать заявку\"" +
                    ". Для повторной подачи заявки участника во вторую категорию со скидкой 100 грн выберите \"Вторая категория\".");
            Map<String, String> response = methods.response("Вторая категория", "Подать заявку");
            methods.sendKeyboard(context, response, "\uD83D\uDCDD Выбор заявки");
            methods.back(context);
        }

        @Override
        public void handleInput(BotContext context) {
            sendMessage(context, "Вы выбрали: " + context.getInput());
            myContext = context;
        }

        @Override
        public BotState nextState() {
            return methods.nextBotState(myContext);
        }
    },NextRequest{
        private BotContext myContext;
        @Override
        public void enter(BotContext context) {
            HashMap<String, String> time = methods.insList(context.getUser());
            HashMap<String, String> map = new HashMap<>();
            for (String s : time.keySet()){
                map.put(s, time.get(s).substring(0, time.get(s).indexOf("||"))); }
            if(ChatBot.pay(context.getUser()).size() < 1){
                sendMessage(context, "Вы еще не подали ни одной заявки. Скидкой 100 грн может воспользоваться участник" +
                        " у которого уже есть оплаченная заявка в какую-либо категорию. Нажмите \"Назад\" -> \"Подать заявку\".");
            }else {methods.sendKeyboardArtist(context, map, "Выберите из списка участника которому необходимо" +
                        " подать заявку в еще одну категорию: ");}
            methods.back(context);}

        @Override
        public void handleInput(BotContext context) {
            methods.upArt(context, "sale");
            myContext = context;}
        @Override
        public BotState nextState() {
            return methods.nextBotState(myContext); }
        //ВЫВОДИТ СООБЩЕНИЕ О НАЧАЛЕ РЕГИСТРАЦИИ  удалить?
    },Intro(false){
        @Override
        public void enter(BotContext context) {
            String isDouble = "";
            if (context.getUser().getArtists().size() > 0 &&
                    context.getUser().getArtists().get(context.getUser().getArtists().size() - 1).isDoubles()) {
                isDouble = "\nИмя второго участника в дуете вам предложат ввести ниже в форме заявки.";
                sendMessage(context, "Заполните данные участника. " + isDouble);}
        }
@Override
        public BotState nextState() {return PartisipantFirstName;
        }
     //ВЫВОДИТ СООБЩЕНИЕ О НАЧАЛЕ РЕГИСТРАЦИИ ВТОРОГО УЧАСТНИКА ДУЭТА
    },DoubleIntro(false){
        private BotContext myContext;
        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Заполните данные второго участника этого дуэта. ");
            Artist artist = context.getBot().getArt(context.getUser());
            artist.setChecks(false);
            context.getBot().targetSave(artist, context.getUser());}

        @Override
        public BotState nextState() {
            return PartisipantFirstName;/*methods.nextBotState(myContext);*/ }
            //ЗДЕСЬ ПОЛЬЗОВАТЕЛЬ ВВОДИТ СВОЕ ИМЯ
    },SendApplicationPerformans{
        private BotContext myContext;
        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Выберите каким будет ваше выступление.");
            Map<String, String> response = methods.response("Соло","Дует");
            methods.sendKeyboard(context, response, "Подать заявку");}
        @Override
        public void handleInput(BotContext context) {
            HashMap<String, String> map = new HashMap<>();
            map.put("Начать сначала", "\uD83D\uDD19 Назад");
            methods.sendKeyboard(context, map, "Проверьте корректность выбора и нажмите" +
                    " \"Начать сначала\" если допустили ошибку.");
            sendMessage(context, "Вы выбрали: " + context.getInput());
            myContext = context; }
        @Override
        public BotState nextState() {
            return methods.nextBotState(myContext);}
        //ВЫБОР КАТЕГОРИИ
    },PartisipantName{
        BotContext myContext;
        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Введите ИМЯ участника : (Например: Екатерина)");}
        @Override
        public void handleInput(BotContext context) {
            HashMap<String, String> map = new HashMap<>();
            map.put("Начать сначала", "\uD83D\uDD19 Назад");
            methods.sendKeyboard(context, map, "Проверьте корректность ввода имени участника и нажмите" +
                    " \"Начать сначала\" если допустили ошибку.");
            myContext = context;
            methods.upArt(context, "username");
            sendMessage(context, "Имя участника: " + context.getInput());}
        @Override
        public BotState nextState() {
            return methods.nextBotState(myContext); }
          //ЗДЕСЬ ПОЛЬЗОВАТЕЛЬ ВВОДИТ ОТЧЕСТВО
    },PartisipantFathername{
        BotContext myContext;
        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Введите ОТЧЕСТВО участника : " + "(Например: Олеговна)");}
        @Override
        public void handleInput(BotContext context) {
            myContext = context;
            methods.upArt(context, "fathername");
            HashMap<String, String> map = new HashMap<>();
            map.put("Начать сначала", "\uD83D\uDD19 Назад");
            methods.sendKeyboard(context, map, "Проверьте корректность ввода отчества участника и нажмите" +
                    " \"Начать сначала\" если допустили ошибку.");
            sendMessage(context, "Отчество участника: " + context.getInput());}
        @Override
        public BotState nextState() {
            return methods.nextBotState(myContext); }
            //ЗДЕСЬ ПОЛЬЗОВАТЕЛЬ ВВОДИТ ФАМИЛИЮ
    },PartisipantFirstName{
        private BotContext myContext;
        @Override
        public void enter(BotContext context) {
            if (ChatBot.getArt(context.getUser()).isDoubles() && ChatBot.getArt(context.getUser()).getUsername() == null){
                sendMessage(context, "Ведите данные первого участника дуэта. Данные второго участника вам предложат" +
                        " ввести позже.");}
            sendMessage(context, "Введите ФАМИЛИЮ участника без имени и отчества: " + "(Например: Затейченко)");}
        @Override
        public void handleInput(BotContext context) {
            methods.upArt(context, "lastname");
            myContext = context;
            HashMap<String, String> map = new HashMap<>();
            map.put("Начать сначала", "\uD83D\uDD19 Назад");
            methods.sendKeyboard(context, map, "Проверьте корректность ввода фамилии участника и нажмите" +
                    " \"Начать сначала\" если допустили ошибку.");
            sendMessage(context, "Фамилия участника: " + context.getInput());}
        @Override
        public BotState nextState() {
            return methods.nextBotState(myContext); }
            //ЗДЕСЬ ПОЛЬЗОВАТЕЛЬ ВВОДИТ ГОРОД
    },Sity{
        private BotContext myContext;
        @Override
        public void enter(BotContext context) {
            String text = "Из какого вы города? ";
            if(context.getUser().isRole()){text = "Из какого города ваш участник? ";}
            sendMessage(context, text + "(Например: Киев)");}
        @Override
        public void handleInput(BotContext context) {
            methods.upArt(context, "sity");
            myContext = context;
            HashMap<String, String> map = new HashMap<>();
            map.put("Начать сначала", "\uD83D\uDD19 Назад");
            methods.sendKeyboard(context, map, "Проверьте корректность ввода названия города и нажмите" +
                    " \"Начать сначала\" если допустили ошибку.");
            sendMessage(context, "Вы указали: " + context.getInput());}
        @Override
        public BotState nextState() {
            return methods.nextBotState(myContext); }
            //ЗДЕСЬ ПОЛЬЗОВАТЕЛЬ ВВОДИТ СТУДИЮ
    },Studio{
        private BotContext myContext;
        @Override
        public void enter(BotContext context) {
            String text = "Какую студию вы будете представлять? ";
            if(context.getUser().isRole()){text = "Какую сдудию представляет ваш участник? ";}
            sendMessage(context, text + "(Например: Super Pole Danse Studio");}
        @Override
        public void handleInput(BotContext context) {
            myContext = context;
            methods.upArt(context, "studio");
            HashMap<String, String> map = new HashMap<>();
            map.put("Начать сначала", "\uD83D\uDD19 Назад");
            methods.sendKeyboard(context, map, "Проверьте корректность названия студии и нажмите" +
                    " \"Начать сначала\" если допустили ошибку.");
            sendMessage(context, "Вы указали: " + context.getInput());}
        @Override
        public BotState nextState() {
            return methods.nextBotState(myContext); }
            //ЗДЕСЬ ПОЛЬЗОВАТЕЛЬ ВЫБИРАЕТ СОЛО ИЛИ ДУЕТ
    },MyCategory{
        private BotContext myContext;
        @Override
        public void enter(BotContext context) {
            Map<String, String> response = methods.response("Pole Sport", "Pole Art", "Aerial Hoop",
                    "Aerial Silks", "Original Genre");
            methods.sendKeyboard(context, response, "Выберите какое направление вас интересует.");}
        @Override
        public void handleInput(BotContext context) {
            myContext = context;
            methods.upArt(context, "view");
            HashMap<String, String> map = new HashMap<>();
            map.put("Начать сначала", "\uD83D\uDD19 Назад");
            methods.sendKeyboard(context, map, "Проверьте корректность выбора направления и нажмите" +
                    " \"Начать сначала\" если допустили ошибку.");
            sendMessage(context, "Вы выбрали: " + context.getInput());}

        @Override
        public BotState nextState() {
            return methods.nextBotState(myContext);}
            //ВЫБОР ВОЗРАСТНОЙ ГРУППЫ
    },MyAge{
        private BotContext myContext;
        @Override
        public void enter(BotContext context) {
            if(ChatBot.getArt(context.getUser()).isDoubles()){
                sendMessage(context, "Если участники дуета находятся в разных возрастных группах нужно" +
                        " указывать возрастную группу старшего участника."); }
            Map<String, String> response = methods.response("5-6 лет", "7-9 лет", "10-12 лет", "13-14 лет",
                    "15-17 лет", "18+ лет", "35+ лет");
            methods.sendKeyboard(context, response, "Выберите вашу возрастную группу.");
            }
        @Override
        public void handleInput(BotContext context) {
            methods.upArt(context, "age");
            HashMap<String, String> map = new HashMap<>();
            map.put("Начать сначала", "\uD83D\uDD19 Назад");
            methods.sendKeyboard(context, map, "Проверьте корректность выбора возрастной группы и нажмите" +
                    " \"Начать сначала\" если допустили ошибку.");
            sendMessage(context, "Вы выбрали: " + context.getInput());
            myContext = context; }
        @Override
        public BotState nextState() {
            return methods.nextBotState(myContext);}
            //ВЫБОР КВАЛИФИКАЦИИ
    },MyLevel{
        private BotContext myContext;
        @Override
        public void enter(BotContext context) {
            Map<String, String> response = methods.response("Начинающие", "Аматоры", "Профессионалы",
                    "Профессионалы-Элит");
            methods.sendKeyboard(context, response, "Выберите вашу соревновательную группу");}
        @Override
        public void handleInput(BotContext context) {
            methods.upArt(context, "level");
            sendMessage(context, "Вы выбрали: " + context.getInput());
            myContext = context; }
        @Override
        public BotState nextState() {
            return methods.nextBotState(myContext);}
            //ОПЛАТА
    },Pay{
        private BotContext myContext;
        @Override
        public void enter(BotContext context) {
            String sale = "";
            if(ChatBot.getArt(context.getUser()).isSale()){sale = "с учетом скидки ";}
            sendMessage(context, context.getBot().getArt(context.getUser()).toString());
            methods.sendKeyboard(context, methods.response("Оплатить", "Начать сначала"),
                    "Если введенная вами информация содержит ошибки нажмите" +
                    " \"Начать сначала\" и " +
                    "начните регистрацию сначала. Если все верно - нажмите на кнопкку оплаты взноса.");
            sendMessage(context, "Стоимость регистрационного взноса в данную категорию " + sale + "составляет " +
                    methods.priceMaker(ChatBot.getArt(context.getUser())) / 100 + " грн.");
            sendMessage(context, "Карта для тестового платежа 4242 4242 4242 4242. \nСрок действия и CVV - любые.");

        }

        @Override
        public void handleInput(BotContext context) {
            myContext = context;
            sendMessage(context, "Вы выбрали: " + context.getInput());}
        @Override
        public BotState nextState() {
            return methods.nextBotState(myContext);}//methods.nextBotState(myContext);}
    },Final{
        @Override
        public void enter(BotContext context) {
            try { context.getBot().execute(methods.sendInvoice(context));
            } catch (TelegramApiException e) {e.printStackTrace();}
        }

        @Override
        public void handleInput(BotContext context) {
            String message = "Вы можете увидеть список ваших подтвержденных заявок, а так же подать ваш трек для" +
                    " музыкального сопровождения в меню " +
                    "\"Заявки\" -> \"Мои заявки\". Желаем вам хорошо подготовиться и успешно выступить." +
                    " До встречи на чемпионате!";
            sendMessage(context, message);}
        @Override
        public BotState nextState() {
            return Point;}
    }, ForwardSound{
        private BotContext myContext;
        @Override
        public void enter(BotContext context) {
            sendMessage(context, methods.musicString(context.getUser()));
            String[] points = new String[]{"Заменить трек", "Подать трек", "Проверить трек"};
            if(methods.musicString(context.getUser()).indexOf("* Вы еще не подали муз.сопровождение.") > 0){
                points = new String[]{"Подать трек"};}
            if(methods.noMusicList(context.getUser()).size() < 1){points=new String[]{"Заменить трек","Проверить трек"};}
            methods.sendKeyboard(context, methods.response(points),
                    "Выберите нужную команду:");
            methods.back(context);}
        @Override
        public void handleInput(BotContext context) {
            myContext = context;
        }
        @Override
        public BotState nextState() {
            return methods.nextBotState(myContext);}
    }, Part{
        private BotContext myContext;
        @Override
        public void enter(BotContext context) {
            String part = "Список вашиx оплаченных заявок:\n------------------------------------------------------------\n";
            for (String s :methods.artistsList(context.getUser()).keySet()){part += s +
                    "\n------------------------------------------------------------\n"; }
            sendMessage(context, "В этом меню вы можете изменить квалификацию участника, добавить его трек для " +
                    "музыкального сопровождения, а так же подать заявку на страховку.\n"+part);
                methods.sendKeyboard(context, methods.response("Редактировать", "Музыка",
                    "Cтраховка"),
                    "Выберите нужную команду:");
            methods.back(context);}

        @Override
        public void handleInput(BotContext context) {
            sendMessage(context, "Вы выбрали: " + context.getInput());
            myContext = context;}
        @Override
        public BotState nextState() {
            return methods.nextBotState(myContext); }
    }, Redactor{
        private BotContext myContext;
        @Override
        public void enter(BotContext context) {
            methods.sendKeyboardArtist(context, methods.artistsList(context.getUser()),
                    "Выберите из списка участника которому необходимо изменить квалификацию.");
            methods.back(context);}

        @Override
        public void handleInput(BotContext context) {
            myContext = context;}
        @Override
        public BotState nextState() {
            return methods.nextBotState(myContext); }
    }, Insurance{
        private BotContext myContext;
        @Override
        public void enter(BotContext context) {
            String result = "Список заявок:\n_____________________________________\n";
            HashMap<String, String> time = methods.insList(context.getUser());
            for (String s: time.keySet()){
                result += s + " -> " + time.get(s).substring(time.get(s).indexOf("||") + 2) +
                "\n_____________________________________\n";}
            sendMessage(context, result.replace("true", "Застрахован").replace("false",
                    "Не застрахован"));
            if(result.indexOf("false") > 0){
            methods.sendKeyboard(context, methods.response("Застраховать"), "Подать заявку на страховку");}


            try {
                sendMessage(context, Utils.HMAC_MD5_encode("test_merchant;www.market.ua;DH783023;1415379863;1547.36;UAH;Процессор Intel Core i5-4670 3.4GHz;Память Kingston DDR3-1600 4096MB PC3-12800;1;1;1000;547.36"));
            } catch (Exception e) {
                e.printStackTrace();
            }


            methods.back(context);}

        @Override
        public void handleInput(BotContext context) {
            myContext = context;}
        @Override
        public BotState nextState() {
            return methods.nextBotState(myContext); }
    },InsPrePay{
        private BotContext myContext;
        @Override
        public void enter(BotContext context) {
            HashMap<String, String> time = methods.insList(context.getUser());
            HashMap<String, String> map = new HashMap<>();
            for (String s : time.keySet()){
                if(time.get(s).indexOf("false") > 0){map.put(s, time.get(s).substring(0, time.get(s).indexOf("||"))); }
            }
            methods.sendKeyboardArtist(context, map, Utils.getValue("field.info.insurancetext"));
            methods.back(context);}

        @Override
        public void handleInput(BotContext context) {
            myContext = context;}
        @Override
        public BotState nextState() {
            return methods.nextBotState(myContext); }

    },InsAdress {
        private BotContext myContext;
        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Введите адрес прописки страхуемого. (Например: Киевская обл, Святошинский р-н," +
                    " с.Ветродуевка, ул.Заречная, 9а)");}

        @Override
        public void handleInput(BotContext context) {
            myContext = context;
            methods.upArt(context, "adres");
            sendMessage(context, "Вы ввели: " + context.getInput()); }

        @Override
        public BotState nextState() {
            return methods.nextBotState(myContext); }

    },InsurancePay{
        private BotContext myContext;
        @Override
        public void enter(BotContext context) {
            Artist a = ChatBot.getArtist(context.getUser().getIns());
            sendMessage(context,"Проверьте адрес страхуемого.Если все верно - нажмите на кнопку оплаты. Вернитесь в главное меню и начните " +
                    "сначала если нашли ошибки.\n" + a.getAdres());
            sendMessage(context, "Карта для тестового платежа 4242 4242 4242 4242. \nСрок действия и CVV - любые.");
            try {context.getBot().execute(methods.invoiceIns(context.getUser(), a,"Страховка " + a.getLastname() + " " +
                        a.getUsername() + " " + a.getFathername(), Integer.parseInt(Utils.getValue("field.info.insuranceprise"))));
            } catch (TelegramApiException e) {e.printStackTrace();}
            methods.back(context); }

        @Override
        public void handleInput(BotContext context) {
            myContext = context;
            methods.back(context); }

        @Override
        public BotState nextState() {
            return methods.nextBotState(myContext);
        }
    },InsFin(false){
        @Override
        public void enter(BotContext context) {
            sendMessage(context,"Ваша страховка оформлена. Вы можете проверить ее наличие в разделе \"Заявки\" -> " +
                    "\"Мои заявки\" -> \"Страховка\"");
        }

        @Override
        public BotState nextState() {
            return Point;
        }
    }, PreSendMusic{
        private BotContext myContext;
        @Override
        public void enter(BotContext context) {
        methods.sendKeyboardArtist(context,methods.noMusicMap(methods.noMusicList(context.getUser())),
                "Выберите участника и категорию для подачи трека.");}

        @Override
        public void handleInput(BotContext context) {
            myContext = context;
            methods.upArt(context, "noMusic");
            try {sendMessage(context, "Вы выбрали: " + ChatBot.findDoubleId(context.getUser().getId(),
                        Long.parseLong(context.getInput())).toString().replace("Данные участника:", ""));
            }catch (Exception e){}
        }

        @Override
        public BotState nextState() {
            return methods.nextBotState(myContext);};
    },SendMusic{
        private BotContext myContext;
        @Override
        public void enter(BotContext context) {
            ChatBot.sendPhotoWithText(context.getUser().getChatId(), Utils.getValue("field.info.down"),
                    "Добавьте ваш трек как это показано на картинке. Вам нужно нажать на изображение скрепки" +
                            " и найти нужный трек в вашей файловой системе.", context.getBot());
        }
        @Override
        public void handleInput(BotContext context) {
            myContext = context;
            methods.upArt(context, "music");
            try {sendMessage(context, "Вы вбрали: " + ChatBot.findDoubleId(context.getUser().getId(),
                    Long.parseLong(context.getInput())).toString());}catch (Exception e){}
        }

        @Override
        public BotState nextState() {
            return methods.nextBotState(myContext);};
    },ErrorVideo(false){
        @Override
        public void enter(BotContext context) {
          sendMessage(context, "Ваш трек не принят! В категории \"Pole Sport\" не разрешено использование видеоряда. Начните процедуру подачи " +
                  "музыки сначала и загрузите ваш файл в формате MP3");
        }

        @Override
        public BotState nextState() {
            return Ok;
        }
    },ErrorLength(false){
        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Ваш трек не принят! Продолжительность загруженного трека превышает " +
                    "максимально допустимую продолжительность для соревнующихся в квалификации " +
                    ChatBot.findDoubleId(context.getUser().getId(), context.getUser().getNoMus()).get(0).getLevel() +
                    " на " +ChatBot.getArtist(context.getUser().getNoMus()).getDuration() + " сек. Обрежьте ваш трек и начните процедуру подачи" +
                    " музыкального сопровождения сначала.");
        }

        @Override
        public BotState nextState() {
            return Ok;
        }
    },MusicSuccess(false){
        @Override
        public void enter(BotContext context) {
            User user = context.getUser();
           for (Artist a : ChatBot.findDoubleId(user.getId(), user.getNoMus())) {
               try { Utils.downloadMusic(a);}catch (Exception e){}
               if (a.isSendMusicCheck()) {
                   a.setSendMusicCheck(false);
                   try {context.getBot().execute(new SendMessage()
                               .setChatId(Long.parseLong(Utils.getValue("field.info.musicaccount")))
                               .setText("SEND MUSIC\n" + a.toString()));
                   } catch (TelegramApiException e) {e.printStackTrace();}
                   ForwardMessage message = new ForwardMessage()
                           .setChatId(Long.parseLong(Utils.getValue("field.info.musicaccount")))
                           .setFromChatId(context.getUser().getChatId())
                           .setMessageId(Math.toIntExact(ChatBot.findDoubleId(context.getUser().getId(),
                                   user.getNoMus()).get(0).getMusicMessage()));
                   ChatBot.targetSave(a, user);
                   try {context.getBot().execute(message);} catch (TelegramApiException e) {}
               }
           }

            sendMessage(context, "Ваш трек принят! Вы можете осуществлять проверку и/или замену ваших треков" +
                    " в меню \"Заявки\" -> \"Мои заявки\" -> \"Музыка\". ");
        }

        @Override
        public BotState nextState() {
            return Ok;
        }
    },MusicError(false){
        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Ваш трек не принят! Вы можете можете подать ваш файл в форматах mp3, mp4 или vaw");
        }

        @Override
        public BotState nextState() {
            return Ok;}

        },MusicRedactor{
        private BotContext myContext;
        @Override
        public void enter(BotContext context) {
            methods.sendKeyboardArtist(context,methods.noMusicMap(methods.musicList(context.getUser())),
                    "Выберите участника и категорию для замены трека.");
            methods.back(context);}

        @Override
        public void handleInput(BotContext context) {
            myContext = context;
            methods.upArt(context, "noMusic");
            try {sendMessage(context, "Вы выбрали: " + ChatBot.findDoubleId(context.getUser().getId(),
                    Long.parseLong(context.getInput())).toString());}catch (Exception e){}
        }
        @Override
        public BotState nextState() {
            return methods.nextBotState(myContext); }
    },HeartMusic{
        private BotContext myContext;
        @Override
        public void enter(BotContext context) {
            methods.sendKeyboardArtist(context,methods.noMusicMap(methods.musicList(context.getUser())),
                    "Выберите участника и категорию для проверки трека.");
            methods.back(context);}

        @Override
        public void handleInput(BotContext context) {
           // methods.counter(context);
            myContext = context;
            methods.upArt(context, "noMusic");
            try {sendMessage(context, "Вы выбрали: " + ChatBot.findDoubleId(context.getUser().getId(),
                    Long.parseLong(context.getInput())).toString());} catch (Exception e) {}
            try {if(!"\uD83D\uDD19 Назад".equals(context.getInput())){
                sendMessage(context, "Ваш актуальный трек: ");
                ForwardMessage message = new ForwardMessage()
                        .setChatId(context.getUser().getChatId())
                        .setFromChatId(context.getUser().getChatId())
                        .setMessageId(Math.toIntExact(ChatBot.findDoubleId(context.getUser().getId(),
                                context.getUser().getNoMus()).get(0).getMusicMessage()));
                try {context.getBot().execute(message);} catch (TelegramApiException e) {e.printStackTrace();}
                }
            }catch (Exception e){}
        }
        @Override
        public BotState nextState() {
            return methods.nextBotState(myContext); }
    }, Ok{
        private BotContext myContext;
        @Override
        public void enter(BotContext context) {
            HashMap<String, String> ok = new HashMap<>();
            ok.put("Продолжить", "Продолжить");
            methods.sendKeyboard(context, ok,"С информацией ознакомлен(а).");}

        @Override
        public void handleInput(BotContext context) {
            myContext = context;}
        @Override
        public BotState nextState() {
            return ForwardSound; }
    }, Ask{
        private BotContext myContext;

        public void enter(BotContext context) {
            Map<String, String> map = new HashMap<>();
            map.put("Да", "ДааД");
            map.put("Нет", "НеттеН");
            String m = "Проверьте правильность ввода. ";
            String menu = m + "Имя учасника " + context.getInput() + "?";
            methods.sendKeyboard(context, map, menu);
        }

        @Override
        public void handleInput(BotContext context) {
            myContext = context;
        }

        @Override
        public BotState nextState() {
            return methods.nextBotState(myContext);
        }
    };



    @Autowired
    private static BotStateMethods methods;
    public static int getNumber() {
        return number;}
    public static void setNumber(int number) {
        BotState.number = number; }
    public static BotState[] getStates() {
        return states;}
    private static int number = -1;
    private static BotState[] states;
    private final boolean inputNeeded;

    BotState() { this.inputNeeded = true; }

    BotState(boolean inputNeeded) { this.inputNeeded = inputNeeded; }

    public static BotState getInitialState() {return byId(0); }

    public static BotState byId(int id) {
        if (states == null) {
            states = BotState.values(); }
        return states[id];
    }
    public boolean isInputNeeded() { return inputNeeded; }

    public void handleInput(BotContext context) {/* do nothing by default*/}

    public abstract void enter(BotContext context);

    public abstract BotState nextState();

    protected void sendMessage(BotContext context, String text) {
        SendMessage message = new SendMessage()
                .setChatId(context.getUser().getChatId())
                .setText(text);
        try { context.getBot().execute(message);
        } catch (TelegramApiException e) { e.printStackTrace(); }
    }

}
