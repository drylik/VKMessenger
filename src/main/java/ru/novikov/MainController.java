package ru.novikov;

import com.vk.api.sdk.objects.messages.Message;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ru.novikov.model.Friend;

public class MainController {
    @FXML
    private TableView<Friend> friendsTable;
    @FXML
    private TableColumn<Friend, String> firstNameColumn;
    @FXML
    private TableColumn<Friend, String> lastNameColumn;
    @FXML
    private ListView<String> messagesListView;

    private Main main;

    //вызывается до initialize()
    public MainController() {
    }

    @FXML
    private void initialize() {
        // Инициализация таблицы адресатов с двумя столбцами.
        firstNameColumn.setCellValueFactory(cellData -> cellData.getValue().firstNameProperty());
        lastNameColumn.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());

        // Очистка дополнительной информации об адресате.
        showFriendsMessages(null);

        // Слушаем изменения выбора, и при изменении отображаем
        // дополнительную информацию об адресате.
        friendsTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showFriendsMessages(newValue));
    }

    private void showFriendsMessages(Friend friend) {
        if (friend != null) {
            ObservableList<Message> messages = main.getMessages(friend);
            ObservableList<String> messagesTexts = FXCollections.observableArrayList();
            for (Message message :
                    messages) {
                messagesTexts.add(message.getBody());
            }
            messagesListView.setItems(messagesTexts);
        } else {
            // Если friend = null, то убираем все сообщения.
            messagesListView.setItems(null);
        }
    }

    public void setMain(Main main) {
        this.main = main;

        // Добавление в таблицу данных из наблюдаемого списка
        friendsTable.setItems(main.getPersonData());
    }
}
