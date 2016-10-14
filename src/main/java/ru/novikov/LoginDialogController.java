package ru.novikov;

import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class LoginDialogController {
    @FXML
    private WebView webViewLogin;

    private Stage dialogStage;
    private boolean result;

    public LoginDialogController() {
        result = true;
    }

    /**
     * Инициализирует класс-контроллер. Этот метод вызывается автоматически
     * после того, как fxml-файл будет загружен.
     */
    @FXML
    public void initialize() {
        webViewLogin = new WebView();
        WebEngine engine = webViewLogin.getEngine();
        engine.load("https://oauth.vk.com/authorize?client_id=" + VK.APP_ID
                + "&display=page&redirect_uri=" + VK.REDIRECT_URI
                + "&scope="+ (2 + 4096)
                + "&response_type=token&v=5.58");
    }

    /**
     * Устанавливает сцену для этого окна
     *
     * @param dialogStage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean getResult() {
        return result;
    }
}
