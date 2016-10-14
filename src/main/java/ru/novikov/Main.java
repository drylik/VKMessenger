package ru.novikov;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.AuthResponse;
import com.vk.api.sdk.objects.friends.responses.GetResponse;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import com.vk.api.sdk.queries.users.UserField;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.novikov.model.Friend;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    private static Logger log = LogManager.getLogger(Main.class.getName());

    private String token;
    private Integer userId;

    private Stage primaryStage;
    private BorderPane rootLayout;
    private ObservableList<Friend> friends = FXCollections.observableArrayList();

    public Main() throws FileNotFoundException {
        TransportClient transportClient = HttpTransportClient.getInstance();
        VkApiClient vk = new VkApiClient(transportClient);
        UserActor actor = connectToVK(vk);
        GetResponse response;
        List<UserXtrCounters> friendsList = null;
        try {
            response = vk.friends().get(actor).execute();
            List<String> userIds = new ArrayList<>(response.getCount());
            for (Object o : response.getItems()) {
                userIds.add(String.valueOf(o));
            }
            friendsList = vk.users().get(actor).userIds(userIds).execute();
        } catch (NullPointerException e){
            log.log(Level.ERROR, "Error during connecting to VK", e);
        } catch (ApiException e) {
            log.log(Level.ERROR, "Api error during getting a response", e);
        } catch (ClientException e) {
            log.log(Level.ERROR, "Client error during getting a response", e);
        }
        if (friendsList != null) {
            for (UserXtrCounters friend :
                    friendsList) {
                friends.add(new Friend(friend.getFirstName(), friend.getLastName()));
            }
        }
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("VKMessenger");

        initRootLayout();

        showFriends();
    }

    public ObservableList<Friend> getPersonData() {
        return friends;
    }

    /**
     * Инициализирует корневой макет.
     */
    public void initRootLayout() {
        try {
            // Загружаем корневой макет из fxml файла.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/RootLayout.fxml"));
            rootLayout = loader.load();

            // Отображаем сцену, содержащую корневой макет.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Показывает в корневом макете сведения о друзьях.
     */
    public void showFriends() {
        try {
            // Загружаем сведения об адресатах.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/main.fxml"));
            AnchorPane messenger = loader.load();

            // Помещаем сведения о друзьях в центр корневого макета.
            rootLayout.setCenter(messenger);

            // Даём контроллеру доступ к главному приложению.
            MainController mainController = loader.getController();
            mainController.setMain(this);

        } catch (IOException e) {
            log.log(Level.ERROR, "Error during showing friends list", e);
        }
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }

    private UserActor connectToVK(VkApiClient vk) {
        AuthResponse authResponse;
        String code;
        token = null;
        userId = null;
        readData();
        try {
            if (token == null || userId == null) {
                code = showLoginDialog();
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

    private static void showErrorDialog(String title, String header, String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    public String showLoginDialog() {
        try {
            // Загружаем fxml-файл и создаём новую сцену
            // для всплывающего диалогового окна.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/LoginDialog.fxml"));
            AnchorPane page = loader.load();

            // Создаём диалоговое окно Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Login");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Создаём контроллер контроллер.
            LoginDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
            dialogStage.showAndWait();
            String result = controller.getResult();
            if (result.contains("error")) {
                throw new IOException(result);
            } else {
                String[] res = result.split("code=");
                return res[res.length - 1];
            }

        } catch (IOException e) {
            log.log(Level.ERROR, "Error during showing login dialog", e);
            return null;
        }
    }
}
