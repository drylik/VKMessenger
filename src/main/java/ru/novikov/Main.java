package ru.novikov;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.AuthResponse;
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

public class Main extends Application {

    private static Logger log = LogManager.getLogger(Main.class.getName());

    private Stage primaryStage;
    private BorderPane rootLayout;
    private ObservableList<Friend> friends = FXCollections.observableArrayList();

    public Main() {
        //TODO: получить от vk список друзей и добавить их в friends
        connectToVK();
        friends.add(new Friend("Анна", "Павлова"));
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

    public void connectToVK() {
        TransportClient transportClient = HttpTransportClient.getInstance();
        VkApiClient vk = new VkApiClient(transportClient);

        String code = readData();
        if (code == null) {
            //TODO: make first time connection
            showLoginDialog();
        }
        AuthResponse authResponse = null;
        try {
            authResponse = vk.oauth()
                    .userAuthorizationCodeFlow(VK.APP_ID, VK.CLIENT_SECRET, VK.REDIRECT_URI, code)
                    .execute();
        } catch (ApiException | ClientException e) {
            log.log(Level.ERROR, "Error during connecting to vk", e);
        }

        UserActor actor = new UserActor(authResponse.getUserId(), authResponse.getAccessToken());
    }

    private String readData() {
        String code = null;
        File file = new File(VK.FILE_NAME);
        if (!file.exists()) {
            return null;
        }
        InputStream fileIS;
        try {
            fileIS = new FileInputStream(file);
            CipherAlg ideaDe = new Idea(IOUtils.toInputStream(VK.CRYPT_KEY), false);
            ByteArrayOutputStream buffer = (ByteArrayOutputStream) ideaDe.encrypt(fileIS);
            code = buffer.toString();
        } catch (IOException e) {
            log.log(Level.ERROR, "Error during decryption", e);
        }
        return code;
    }

    private void writeData(String code) {
        File file = new File(VK.FILE_NAME);
        OutputStream fileOS;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            fileOS = new FileOutputStream(file);
            CipherAlg ideaEn = new Idea(IOUtils.toInputStream(VK.CRYPT_KEY), true);
            ByteArrayOutputStream buffer = (ByteArrayOutputStream) ideaEn.encrypt(IOUtils.toInputStream(code));
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

    public boolean showLoginDialog() {
        try {
            // Загружаем fxml-файл и создаём новую сцену
            // для всплывающего диалогового окна.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/LoginDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

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

            return controller.getResult();
        } catch (IOException e) {
            log.log(Level.ERROR, "Error during showing login dialog", e);
            return false;
        }
    }
}
