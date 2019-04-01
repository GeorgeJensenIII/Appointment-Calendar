import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.*;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.function.Consumer;


/**
 * Created by gwjense on 7/16/17.
 */
public class Main extends Application {


    /**
     * Alert Dialog Method
     * @alertMessage A string that is to be displayed as a error message.
     */
    Consumer<String> alertDialog = ((String alertMessage) -> {
        Alert alert = new Alert(Alert.AlertType.NONE);

        Stage dialog = (Stage) alert.getDialogPane().getScene().getWindow();
        dialog.setAlwaysOnTop(true);


        BorderPane dialogPane = new BorderPane();

        Text alertMsg = new Text(alertMessage);
        alertMsg.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        Button ok = new Button("Ok");


        dialogPane.setCenter(alertMsg);
        dialogPane.setBottom(ok);


        Scene dialogScene = new Scene(dialogPane);


        dialog.setScene(dialogScene);
        dialog.sizeToScene();
        dialog.setResizable(false);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.show();


        ok.setOnAction((ActionEvent e) -> dialog.close());

    });





@Override
    public void start(Stage primaryStage) throws Exception{
            // Change Locale to France to test localization
            // Locale.setDefault(new Locale("fr","FR"));


            TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));


            GridPane loginMainPane = new GridPane();
            Scene login = new Scene(loginMainPane);

            TextField userNameField = new TextField();
            Text userName = new Text("User Name: ");
            userName.setFont(Font.font("Arial", FontWeight.BOLD, 24));
            Text password = new Text("Password: ");
            password.setFont(Font.font("Arial", FontWeight.BOLD, 24));
            PasswordField passwordField = new PasswordField();

            Button loginButton = new Button("Login");

            CurrentResourceBundle resourceBundle = new CurrentResourceBundle(Locale.getDefault());

            resourceBundle.setRb(ResourceBundle.getBundle("MessagesBundle", Locale.getDefault()));
            ResourceBundle rb = resourceBundle.getRb();

            userName.setText(rb.getString("username"));
            password.setText(rb.getString("password"));
            loginButton.setText(rb.getString("login"));


            loginMainPane.add(userName,0,0);
            loginMainPane.add(userNameField,1,0);
            loginMainPane.add(password,0,1);
            loginMainPane.add(passwordField,1,1);
            loginMainPane.add(loginButton,2,2);




            loginMainPane.setVgap(10);
            loginMainPane.setHgap(10);
            loginMainPane.setPadding(new Insets(20,20,20,20));


            primaryStage.setScene(login);
            primaryStage.sizeToScene();
            primaryStage.setResizable(false);
            primaryStage.show();



            // Check DB for username and password
            // Not used for production


            loginButton.setOnAction((ActionEvent a)->{
                try {

                    Connection conn = DriverManager.getConnection("jdbc:mysql://52.206.157.109/U041WH",
                            "U041WH","53688147352");

                    String query = "SELECT userName, password FROM user WHERE userName = ?";

                    PreparedStatement preparedStatement = conn.prepareStatement(query);

                    preparedStatement.setString(1, userNameField.getText());

                    ResultSet resultSet = preparedStatement.executeQuery();

                    if (resultSet.next()) {
                        if (resultSet.getString("password").compareTo(passwordField.getText()) == 0)
                        {

                            String currentUser = userNameField.getText();



                            LocalDateTime dateTime = LocalDateTime.now();
                            String s = currentUser + " Logged on at: " + dateTime.toString()+"\n";


                            BufferedWriter bw = new BufferedWriter(new FileWriter("./logfile.txt", true));
                            bw.write(s);
                            bw.newLine();
                            bw.flush();
                            bw.close();

                            primaryStage.hide();
                            primaryStage.setResizable(true);
                            primaryStage.setScene(new Scene(new AppointmentCalendar(primaryStage, currentUser).getMainView(), 1500, 750));
                            primaryStage.sizeToScene();
                            primaryStage.setResizable(false);
                            primaryStage.show();

                        }
                        else {
                            // Password is incorrect
                            alertDialog.accept(rb.getString("errorDNE"));

                        }

                    }
                    else
                    {
                        // user does not exist
                        alertDialog.accept(rb.getString("errorDNE"));
                    }



                } catch (Exception e)
                {
                    System.out.println(e.getMessage());
                }

            });


    }

    public static void main (String [] args){
        launch(args);
    }


    private class CurrentResourceBundle
    {
        ResourceBundle rb;

        CurrentResourceBundle(Locale locale)
        {
            rb = ResourceBundle.getBundle("MessagesBundle", locale);
        }

        public ResourceBundle getRb() {
            return rb;
        }

        public void setRb(ResourceBundle rb) {
            this.rb = rb;
        }
    }

}