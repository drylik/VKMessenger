package ru.novikov;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.AuthResponse;
import com.vk.api.sdk.objects.friends.responses.GetResponse;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.messages.responses.GetHistoryResponse;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.novikov.model.Friend;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class VK {

    private static Logger log = LogManager.getLogger(Main.class.getName());

    //vk API constants
    public static final int APP_ID = 5664399;
    public static final String CLIENT_SECRET = "BNZt6Ed8h3rlSGXxNvP2";
    public static final String REDIRECT_URI = "https://oauth.vk.com/blank.html";

    //TODO: move data.dat somewhere. Now it's in the root of current hd
    private static final String FILE_NAME = "/data.dat";
    private static final String CRYPT_KEY = "VKMessengerCrypt";

    private String token;
    private Integer userId;
    private VkApiClient vk;
    private UserActor actor;

    private Friend currentFriend;

    public VkApiClient getVk() {
        return vk;
    }

    public UserActor getActor() {
        return actor;
    }

    public VK(Main main) {
        TransportClient transportClient = HttpTransportClient.getInstance();
        vk = new VkApiClient(transportClient);
        actor = connectToVK(main);
    }

    private UserActor connectToVK(Main main) {
        AuthResponse authResponse;
        String code;
        token = null;
        userId = null;
        readData();
        try {
            if (token == null || userId == null) {
                code = main.showLoginDialog();
                if (code != null) {
                    authResponse = vk.oauth()
                            .userAuthorizationCodeFlow(VK.APP_ID, VK.CLIENT_SECRET, VK.REDIRECT_URI, code)
                            .execute();
                    token = authResponse.getAccessToken();
                    userId = authResponse.getUserId();
                    writeData();
                }
            }
            return new UserActor(userId, token);
        } catch (ApiException e) {
            log.log(Level.ERROR, "Api error during connecting to vk", e);
        } catch (ClientException e) {
            log.log(Level.ERROR, "Client error during connecting to vk", e);
        }
        return null;
    }

    private void readData() {
        String data;
        File file = new File(VK.FILE_NAME);
        if (!file.exists()) {
            return;
        }
        InputStream fileIS;
        try {
            fileIS = new FileInputStream(file);
            CipherAlg ideaDe = new Idea(IOUtils.toInputStream(VK.CRYPT_KEY), false);
            ByteArrayOutputStream buffer = (ByteArrayOutputStream) ideaDe.encrypt(fileIS);
            data = buffer.toString();
            token = data.split(" ")[0];
            userId = new Integer(data.split(" ")[1]);
        } catch (IOException e) {
            log.log(Level.ERROR, "Error during decryption", e);
        }
    }

    private void writeData() {
        File file = new File(VK.FILE_NAME);
        OutputStream fileOS;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            fileOS = new FileOutputStream(file);
            CipherAlg ideaEn = new Idea(IOUtils.toInputStream(VK.CRYPT_KEY), true);
            ByteArrayOutputStream buffer = (ByteArrayOutputStream) ideaEn.encrypt(IOUtils.toInputStream(token + " " + userId));
            buffer.writeTo(fileOS);
        } catch (IOException e) {
            log.log(Level.ERROR, "Error during writing data to file", e);
        }
    }

    public List<UserXtrCounters> loadFriendsList() {
        List<UserXtrCounters> friendsList = null;
        GetResponse response;
        try {
            response = vk.friends()
                    .get(actor)
                    .execute();
            List<String> userIds = new ArrayList<>(response.getCount());
            for (Object o : response.getItems()) {
                userIds.add(String.valueOf(o));
            }
            friendsList = vk.users()
                    .get(actor)
                    .userIds(userIds)
                    .execute();
        } catch (NullPointerException e){
            log.log(Level.ERROR, "Error during connecting to VK", e);
        } catch (ApiException e) {
            log.log(Level.ERROR, "Api error during getting a response", e);
        } catch (ClientException e) {
            log.log(Level.ERROR, "Client error during getting a response", e);
        }
        return friendsList;
    }

    public ObservableList<String> showFriendsMessages(Friend friend) {
        currentFriend = friend;
        ObservableList<Message> messages = FXCollections.observableArrayList();
        try {
            GetHistoryResponse historyResponse = vk.messages()
                    .getHistory(actor)
                    .userId(String.valueOf(friend.getId()))
                    .execute();
            messages.addAll(historyResponse.getItems());
        } catch (ApiException e) {
            log.log(Level.ERROR, "Api error during getting a response", e);
        } catch (ClientException e) {
            log.log(Level.ERROR, "Client error during getting a response", e);
        }
        ObservableList<String> messagesTexts = FXCollections.observableArrayList();
        for (Message message :
                messages) {
            messagesTexts.add(message.getBody());
        }
        return messagesTexts; //main.messagesListView.setItems(messagesTexts);
    }

    public void sendMessage(String message){
        try {
            vk.messages().send(actor)
                    .userId(currentFriend.getId())
                    .message(message)
                    .execute();
        } catch (ApiException e) {
            log.log(Level.ERROR, "Api error during getting a response", e);
        } catch (ClientException e) {
            log.log(Level.ERROR, "Client error during getting a response", e);
        }
    }
}
