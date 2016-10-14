package ru.novikov;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.AuthResponse;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

public class VK {

    private static Logger log = LogManager.getLogger(Main.class.getName());

    //vk API constants
    public static final int APP_ID = 5664399;
    public static final String CLIENT_SECRET = "BNZt6Ed8h3rlSGXxNvP2";
    public static final String REDIRECT_URI = "https://oauth.vk.com/blank.html";

    public static final String FILE_NAME = "/data.dat";
    public static final String CRYPT_KEY = "VKMessengerCrypt";

    public VK() {

    }


}
