package com.rahul.kovil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KovilApplication {

	public static void main(String[] args) {
		SpringApplication.run(KovilApplication.class, args);
	}

}

//
//
// package com.rahul.kovil;
//
// import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Path;
//
// import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.boot.builder.SpringApplicationBuilder;
// import org.springframework.context.ConfigurableApplicationContext;
//
// import com.rahul.kovil.common.util.HardwareIdUtil;
//
// import javafx.application.Application;
// import javafx.application.Platform;
// import javafx.geometry.Insets;
// import javafx.geometry.Pos;
// import javafx.scene.Scene;
// import javafx.scene.control.Button;
// import javafx.scene.control.Label;
// import javafx.scene.layout.VBox;
// import javafx.scene.web.WebView;
// import javafx.stage.Stage;
//
// @SpringBootApplication
// public class KovilApplication extends Application {
//
// private ConfigurableApplicationContext context;
//
// public static void main(String[] args) {
// Application.launch(args); // ← JavaFX entry
// }
//
// @Override
// public void init() {
// context = new SpringApplicationBuilder(KovilApplication.class)
// .run(getParameters().getRaw().toArray(new String[0]));
// }
//
// @Override
// public void start(Stage stage) throws IOException {
//
// //------------------------------------------------------
// // 1️⃣ MACHINE LOCK CHECK
// //------------------------------------------------------
// Path licensePath = Path.of("license.key");
//
// if (!Files.exists(licensePath)) {
// showErrorPopup("❌ License file missing.\nContact your developer.");
// return;
// }
//
// String savedId = Files.readString(licensePath).trim();
// String currentId = HardwareIdUtil.getHardwareId();
// System.err.println(savedId);
// System.out.println(currentId);
// if (!currentId.equals(savedId)) {
// showErrorPopup("❌ Unauthorized Machine!\nApp will now close.");
//// return;
// }
//
//
// WebView webView = new WebView();
// webView.getEngine().load("http://localhost:8090/web/auth/login");
//
// stage.setScene(new Scene(webView, 1200, 800));
// stage.setTitle("Kovil Desktop App");
//
// stage.setOnCloseRequest(event -> stopApp());
// stage.show();
// }
//
// @Override
// public void stop() {
// context.close();
// Platform.exit();
// }
//
// // Show popup for license errors
// private void showErrorPopup(String message) {
// Stage errorStage = new Stage();
// errorStage.setTitle("License Error");
//
// Label label = new Label(message);
// label.setWrapText(true);
//
// Button close = new Button("Close");
// close.setOnAction(e -> System.exit(0));
//
// VBox box = new VBox(15, label, close);
// box.setPadding(new Insets(25));
// box.setAlignment(Pos.CENTER);
//
// errorStage.setScene(new Scene(box, 350, 200));
// errorStage.show();
// }
// private void stopApp() {
// try {
// System.out.println("Shutting down application...");
//
// // 1️⃣ Stop Spring Boot backend
// if (context != null) {
// context.close();
// }
//
// // 2️⃣ Stop JavaFX
// Platform.exit();
//
// // 3️⃣ Kill JVM completely
// System.exit(0);
//
// } catch (Exception e) {
// e.printStackTrace();
// System.exit(1);
// }
// }
// }