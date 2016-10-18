package ru.novikov;

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
    private VK vk;

    //вызывается до initialize()
    public MainController() {
    }

    @FXML
    private void initialize() {
        // Инициализация таблицы адресатов с двумя столбцами.
        firstNameColumn.setCellValueFactory(cellData -> cellData.getValue().firstNameProperty());
        lastNameColumn.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());

        // Очистка дополнительной информации об адресате.
        messagesListView.getItems().clear();

        // Слушаем изменения выбора, и при изменении отображаем
        // дополнительную информацию об адресате.
        friendsTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showFriendsMessages(newValue));
    }

    private void showFriendsMessages(Friend friend) {
        if (friend != null) {
            messagesListView.setItems(vk.showFriendsMessages(friend));
        } else {
            //clear it, if null
            messagesListView.getItems().clear();
        }
    }

    public void setMain(Main main) {
        this.main = main;

        // Добавление в таблицу данных из наблюдаемого списка
        friendsTable.setItems(main.getPersonData());
    }

    public void setVk(VK vk) {
        this.vk = vk;
    }
}
