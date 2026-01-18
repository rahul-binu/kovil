package com.rahul.kovil;

import com.rahul.kovil.common.util.HardwareIdUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class MainFxApp extends Application {

    private ConfigurableApplicationContext applicationContext;

    @Override
    public void start(Stage stage) throws Exception {

        //------------------------------------------------------
        // 1️⃣ MACHINE LOCK CHECK
        //------------------------------------------------------
        Path licensePath = Path.of("license.key");

        if (!Files.exists(licensePath)) {
            showErrorPopup("❌ License file missing.\nContact your developer.");
            return;
        }

        String savedId = Files.readString(licensePath).trim();
        String currentId = HardwareIdUtil.getHardwareId();
        System.err.println(savedId);
        System.out.println(currentId);
        if (!currentId.equals(savedId)) {
            showErrorPopup("❌ Unauthorized Machine!\nApp will now close.");
            return;
        }

        //------------------------------------------------------
        // 2️⃣ START SPRING BOOT BACKEND
        //------------------------------------------------------
//        CompletableFuture.runAsync(() -> {
//            applicationContext = new SpringApplicationBuilder(KovilApplication.class).run();
//        });

//        CompletableFuture.runAsync(() -> {
//            applicationContext =
//                new SpringApplicationBuilder()
//                    .sources(KovilApplication.class)
//                    .main(KovilApplication.class)
//                    .web(org.springframework.boot.WebApplicationType.SERVLET)
//                    .run();
//        });
        CompletableFuture.runAsync(() -> {
        	System.out.println("inside async");
            applicationContext = SpringApplication.run(KovilApplication.class, getParameters().getRaw().toArray(new String[0]));
        });

        System.out.println("application conted created");
        
        // Poll localhost until server is ready
        boolean serverReady = false;
        int tries = 0;
        while (!serverReady && tries < 50) { // try ~10 seconds
            try {
                Thread.sleep(500);
                try (Socket socket = new Socket("localhost", 8090)) {
                    serverReady = true;
                }
            } catch (Exception e) {
                tries++;
            }
        }

        System.out.println("Server redy "+serverReady);
        //------------------------------------------------------
        // 3️⃣ LOAD WEBVIEW UI
        //------------------------------------------------------
        WebView webView = new WebView();
        webView.setContextMenuEnabled(false);
        webView.setZoom(1);

        webView.getEngine().load("http://localhost:8090/web/auth/login");

        Scene scene = new Scene(webView, 1200, 800);
        stage.setScene(scene);
        stage.setTitle("Kovil Desktop App");

        // 🔥 When "X" is clicked, STOP EVERYTHING CLEANLY
        stage.setOnCloseRequest(event -> stopApp());

        stage.show();
    }

    // Show popup for license errors
    private void showErrorPopup(String message) {
        Stage errorStage = new Stage();
        errorStage.setTitle("License Error");

        Label label = new Label(message);
        label.setWrapText(true);

        Button close = new Button("Close");
        close.setOnAction(e -> System.exit(0));

        VBox box = new VBox(15, label, close);
        box.setPadding(new Insets(25));
        box.setAlignment(Pos.CENTER);

        errorStage.setScene(new Scene(box, 350, 200));
        errorStage.show();
    }

    // 🔥 PROPER SHUTDOWN METHOD
    private void stopApp() {
        try {
            System.out.println("Shutting down application...");

            // 1️⃣ Stop Spring Boot backend
            if (applicationContext != null) {
                applicationContext.close();
            }

            // 2️⃣ Stop JavaFX
            Platform.exit();

            // 3️⃣ Kill JVM completely
            System.exit(0);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
