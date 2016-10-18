package ru.novikov;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class NewMessageController {
    @FXML
    private TextArea newMessage;

    private VK vk;

    //TODO: refresh messages list after sending
    @FXML
    private void handleSendButton() {
        String message = newMessage.getText();
        vk.sendMessage(message);
        newMessage.clear();
    }

    public void setVK(VK vk) {
        this.vk = vk;
    }
}
