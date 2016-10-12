package ru.novikov;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ru.novikov.model.Friend;

public class Controller {
    @FXML
    private TableView<Friend> friendsTable;
    @FXML
    private TableColumn<Friend, String> firstNameColumn;
    @FXML
    private TableColumn<Friend, String> lastNameColumn;

    private Main main;

    //вызывается до initialize()
    public Controller() {
    }

    @FXML
    private void initialize() {
        // Инициализация таблицы адресатов с двумя столбцами.
        firstNameColumn.setCellValueFactory(cellData -> cellData.getValue().firstNameProperty());
        lastNameColumn.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());
    }

    public void setMain(Main main) {
        this.main = main;

        // Добавление в таблицу данных из наблюдаемого списка
        friendsTable.setItems(main.getPersonData());
    }
}
