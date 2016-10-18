package ru.novikov;

import com.vk.api.sdk.objects.users.UserXtrCounters;
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
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.novikov.model.Friend;

import java.io.*;
import java.util.List;

public class Main extends Application {

    private static Logger log = LogManager.getLogger(Main.class.getName());

    private VK vk;

    private Stage primaryStage;
    private BorderPane rootLayout;
    private ObservableList<Friend> friends = FXCollections.observableArrayList();

    public Main() {
        vk = new VK(this);
        List<UserXtrCounters> friendsList = vk.loadFriendsList();
        if (friendsList != null) {
            for (UserXtrCounters friend :
                    friendsList) {
                friends.add(new Friend(friend.getFirstName(), friend.getLastName(), friend.getId()));
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
     * Показывает в корневом макете список друзей.
     */
    public void showFriends() {
        try {
            // Загружаем сведения об адресатах.
            FXMLLoader loaderMainTable = new FXMLLoader();
            loaderMainTable.setLocation(Main.class.getResource("/mainStage.fxml"));
            AnchorPane mainTable = loaderMainTable.load();

            FXMLLoader loaderNewMessage = new FXMLLoader();
            loaderNewMessage.setLocation(Main.class.getResource("/NewMessage.fxml"));
            AnchorPane newMessage = loaderNewMessage.load();

            // Помещаем сведения о друзьях в центр корневого макета.
            rootLayout.setCenter(mainTable);
            //помещаем поле для ввода текста и кнопку отправить в нижнюю часть корневого макета.
            rootLayout.setBottom(newMessage);

            // Даём контроллеру доступ к главному приложению.
            MainController mainController = loaderMainTable.getController();
            mainController.setMain(this);
            mainController.setVk(vk);
            NewMessageController newMessageController = loaderNewMessage.getController();
            newMessageController.setVK(vk);

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
