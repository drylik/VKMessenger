package ru.novikov;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class LoginDialogController {
    @FXML
    private WebView webViewLogin;
    private WebEngine webviewLoginEngine;

    @FXML
    private Button buttonOK;

    private Stage dialogStage;
    private String result;

    public LoginDialogController() {
        result = null;
        webViewLogin = new WebView();
    }

    /**
     * Инициализирует класс-контроллер. Этот метод вызывается автоматически
     * после того, как fxml-файл будет загружен.
     */
    @FXML
    public void initialize() {
        webviewLoginEngine = webViewLogin.getEngine();
        webviewLoginEngine.setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_5) AppleWebKit/601.6.17 (KHTML, like Gecko) Version/9.1.1 Safari/601.6.17");
        webviewLoginEngine.load("https://oauth.vk.com/authorize?client_id=" + VK.APP_ID
                + "&display=page&redirect_uri=" + VK.REDIRECT_URI
                + "&scope="+ (2 + 4096)
                + "&response_type=code&v=5.58");
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    private void handleOKButton() {
        result = webviewLoginEngine.getLocation();
        dialogStage.close();
    }

    public String getResult() {
        return result;
    }
}
