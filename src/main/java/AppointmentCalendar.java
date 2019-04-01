import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;


import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;


import java.sql.*;

import java.time.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;


/**
 * Created by gwjense on 7/16/17.
 */
public class AppointmentCalendar {
    private BorderPane mainView;

    private String currentUser;

    private DBConnection dbConnection = new DBConnection();

    public Consumer<String> alertDialog;

    private CustomerDAO customerDAO;

    public AppointmentCalendar() {
    }

    public AppointmentCalendar(Stage primaryStage, String currentUser) throws SQLException {

        // initialization
        mainView = new BorderPane();

        this.currentUser = currentUser;



        CalendarView calendarView = new CalendarView(currentUser);
        VBox customerMaintenanceView = getCustomerMaintenanceView();



        // Main UI

        ToggleGroup toggleGroup = new ToggleGroup();
        Label title = new Label("Appointment Scheduling Application");
        title.setFont(Font.font("Arial", 24));
        RadioToggleButton scheduleViewer = new RadioToggleButton();
        scheduleViewer.setText("Schedule Maintenance");
        RadioToggleButton customerMaintenance = new RadioToggleButton();
        customerMaintenance.setText("Customer Maintenance");
        RadioToggleButton reports = new RadioToggleButton();
        reports.setText("Reports");

        toggleGroup.getToggles().addAll(scheduleViewer, customerMaintenance, reports);

        Button exit = new Button("Exit");

        VBox buttons = new VBox();
        buttons.getChildren().addAll(scheduleViewer, customerMaintenance, reports, exit);
        buttons.setSpacing(10);
        mainView.setLeft(buttons);
        mainView.setTop(title);
        mainView.setAlignment(title, Pos.CENTER);

        /**
         * alertDialog generates an alert message to alert users of errors
         * @alertMessage is the message to be displayed to the user.
         */
        alertDialog = ((String alertMessage) -> {
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


            ok.setOnAction((ActionEvent e) -> {

                dialog.close();
            });

        });





        LocalDateTime ldtTimeStamp = LocalDateTime.now().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();


        try {
            // Connect to DB and retrieve any upcoming appointments

            Connection conn = DriverManager.getConnection("jdbc:mysql://52.206.157.109/U041WH",
                    "U041WH", "53688147352");

            String query = "SELECT appointment.description, appointment.start, appointment.end, customer.customerName FROM appointment JOIN customer ON appointment.customerId = customer.customerId WHERE appointment.start BETWEEN ? AND ?";

            PreparedStatement preparedStatement = conn.prepareStatement(query);

            preparedStatement.setTimestamp(1, Timestamp.valueOf(ldtTimeStamp));
            preparedStatement.setTimestamp(2, Timestamp.valueOf(ldtTimeStamp.plusMinutes(15)));


            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
               alertDialog.accept("You have an upcoming appointment at " + resultSet.getTimestamp("start").toString() + "\n With description: " + resultSet.getString("description"));
            }
        }
        catch (SQLException e){
           alertDialog.accept(e.getMessage());

        }


        scheduleViewer.setOnAction((ActionEvent e) -> {

            mainView.setCenter(calendarView.getCalendarView());

        });

        exit.setOnAction((ActionEvent a) -> {
            primaryStage.close();
        });


        customerMaintenance.setOnAction((ActionEvent a) -> {

            mainView.setCenter(customerMaintenanceView);

        });

        reports.setOnAction((ActionEvent a) ->{

            mainView.setCenter(reportView());

        });

    }


    /**
     * Generates the Customer Maintenance View
     * @return the customerMaintenanceView VBox element.
     */
    public VBox getCustomerMaintenanceView() {
        VBox customerMaintenanceView = new VBox();
        customerMaintenanceView.setPadding(new Insets(20, 20, 20, 20));


        // Buttons HBox

        HBox buttons = new HBox();

        Button addCustomer = new Button("Add Customer");

        Button modifyCustomer = new Button("Modify Customer");

        Button removeCustomer = new Button("Remove Customer");

        buttons.getChildren().addAll(addCustomer, modifyCustomer, removeCustomer);
        buttons.setSpacing(20);
        buttons.setAlignment(Pos.CENTER);

        // Customer Table View
        ArrayList<Customer> customers = getCustomerList();

        ObservableList<Customer> customerList = FXCollections.observableArrayList(customers);

        TableView<Customer> tv = new TableView<>(customerList);

        TableColumn<Customer, Integer> customerId = new TableColumn<>("Customer ID");
        customerId.setCellValueFactory(new PropertyValueFactory<>("customerId"));

        TableColumn<Customer, String> customerName = new TableColumn<>("Customer Name");
        customerName.setCellValueFactory(new PropertyValueFactory<>("customerName"));

        TableColumn<Customer, Integer> active = new TableColumn<>("Active");
        active.setCellValueFactory(new PropertyValueFactory<>("active"));

        TableColumn<Customer, String> address = new TableColumn<>("address");
        address.setCellValueFactory(new PropertyValueFactory<>("address"));

        TableColumn<Customer, String> address2 = new TableColumn<>("Address 2");
        address2.setCellValueFactory(new PropertyValueFactory<>("address2"));

        TableColumn<Customer, String> postalCode = new TableColumn<>("Postal Code");
        postalCode.setCellValueFactory(new PropertyValueFactory<>("postalCode"));

        TableColumn<Customer, String> phone = new TableColumn<>("Phone");
        phone.setCellValueFactory(new PropertyValueFactory<>("phone"));

        TableColumn<Customer, String> city = new TableColumn<>("City");
        city.setCellValueFactory(new PropertyValueFactory<>("city"));

        TableColumn<Customer, String> country = new TableColumn<>("Country");
        country.setCellValueFactory(new PropertyValueFactory<>("country"));


        tv.getColumns().addAll(customerId, customerName, active, address, address2, postalCode, phone, city, country);

        TableView.TableViewSelectionModel<Customer> tvSelectionModel = tv.getSelectionModel();

        customerDAO = new CustomerDAO(dbConnection, currentUser);


        // button Actions

        addCustomer.setOnAction((ActionEvent a) -> {
            addCustomerView(customerList);

        });

        modifyCustomer.setOnAction((ActionEvent m) -> {
            if (tvSelectionModel.isEmpty()) {

            } else {
                modifyCustomerView(tvSelectionModel.getSelectedItem());
            }
        });

        removeCustomer.setOnAction((ActionEvent r) -> {
            if (tvSelectionModel.isEmpty()) {

            } else {
                try {
                    customerDAO.removeCustomerRecord(tvSelectionModel.getSelectedItem());
                    customers.remove(tvSelectionModel.getSelectedItem());
                } catch (Exception e)
                {
                    alertDialog.accept(e.getMessage());
                }
            }
        });

        Label subtitle = new Label("Customer Maintenance");
        subtitle.setFont(Font.font("Trebuchet", FontWeight.BOLD, 24));

        customerMaintenanceView.getChildren().addAll(subtitle, buttons, tv);

        return customerMaintenanceView;
    }

    /**
     * Generates the Add Customer View
     * @param customers the array of customers in which to add the new customer to
     */
    public void addCustomerView(ObservableList<Customer> customers) {
        GridPane mainPain = new GridPane();


        mainPain.setPadding(new Insets(50, 50, 50, 50));

        mainPain.setHgap(20);
        mainPain.setVgap(20);


        Label title = new Label("Add customer View");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));

        Label customerID = new Label("Customer ID: ");


        Label customerName = new Label("Customer Name: ");
        TextField customerNameField = new TextField();
        customerNameField.setPromptText("Customer Name");


        Label active = new Label("Active");

        CheckBox activeCheckBox = new CheckBox("Active");
        activeCheckBox.setSelected(true);

        Label address = new Label("Address Line 1: ");
        TextField addressField = new TextField();
        addressField.setPromptText("Address Line 1");

        Label address2 = new Label("Address Line 2: ");
        TextField address2Field = new TextField();
        addressField.setPromptText("Address Line 2");

        Label postalCode = new Label("Postal Code: ");
        TextField postalCodeField = new TextField();
        postalCodeField.setPromptText("Postal Code");

        Label phone = new Label("Phone: ");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number");

        Label city = new Label("City: ");
        TextField cityField = new TextField();
        cityField.setPromptText("City");

        Label country = new Label("Country: ");
        TextField countryField = new TextField();
        countryField.setPromptText("Country");


        Button cancel = new Button("Cancel");

        Button save = new Button("Save");

        mainPain.add(title, 0, 0, 2, 1);
        mainPain.add(customerID, 0, 1);
        mainPain.add(customerName, 0, 2);
        mainPain.add(customerNameField, 1, 2);
        mainPain.add(active, 0, 3);
        mainPain.add(activeCheckBox, 1, 3);
        mainPain.add(address, 0, 4);
        mainPain.add(addressField, 1, 4);
        mainPain.add(address2, 0, 5);
        mainPain.add(address2Field, 1, 5);
        mainPain.add(postalCode, 0, 6);
        mainPain.add(postalCodeField, 1, 6);
        mainPain.add(phone, 0, 7);
        mainPain.add(phoneField, 1, 7);
        mainPain.add(city, 0, 8);
        mainPain.add(cityField, 1, 8);
        mainPain.add(country, 0, 9);
        mainPain.add(countryField, 1, 9);
        mainPain.add(save, 0, 10);
        mainPain.add(cancel, 1, 10);


        Stage newStage = new Stage();

        Scene scene = new Scene(mainPain);

        newStage.setScene(scene);
        newStage.sizeToScene();
        newStage.setResizable(false);
        newStage.show();


        cancel.setOnAction((ActionEvent a) -> {
            newStage.close();
        });

        // Save customer to DB

        save.setOnAction((ActionEvent s) -> {

            Customer newCustomer = new Customer(0, customerNameField.getText(), 1, addressField.getText(), address2Field.getText(), postalCodeField.getText(), phoneField.getText(), cityField.getText(), countryField.getText());

            try {
                customerDAO.insertCustomerRecord(newCustomer);
                newStage.close();
                customers.add(newCustomer);

            } catch (Exception e) {
                alertDialog.accept(e.getMessage());
            }

        });


    }

    /**
     * Generate the Modify Customer View
     * @param customer the customer to be modified
     */
    public void modifyCustomerView(Customer customer) {
        GridPane mainPain = new GridPane();


        mainPain.setPadding(new Insets(50, 50, 50, 50));

        mainPain.setHgap(20);
        mainPain.setVgap(20);


        Label title = new Label("Modify customer View");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));

        Label customerID = new Label("Customer ID: ");


        Label customerName = new Label("Customer Name: ");
        TextField customerNameField = new TextField();
        customerNameField.setText(customer.getCustomerName());

        Label activeLabel = new Label("Active");

        CheckBox activeCheckBox = new CheckBox("Active");
        activeCheckBox.setSelected(true);
        if (customer.getActive() == 0) {
            activeCheckBox.setSelected(false);
        }

        Label address = new Label("Address Line 1: ");
        TextField addressField = new TextField();
        addressField.setText(customer.getAddress());

        Label address2 = new Label("Address Line 2: ");
        TextField address2Field = new TextField();
        address2Field.setText(customer.getAddress2());

        Label postalCode = new Label("Postal Code: ");
        TextField postalCodeField = new TextField();
        postalCodeField.setText(customer.getPostalCode());

        Label phone = new Label("Phone: ");
        TextField phoneField = new TextField();
        phoneField.setText(customer.getPhone());

        Label city = new Label("City: ");
        TextField cityField = new TextField();
        cityField.setText(customer.getCity());

        Label country = new Label("Country: ");
        TextField countryField = new TextField();
        countryField.setText(customer.getCountry());


        Button cancel = new Button("Cancel");

        Button save = new Button("Save");

        mainPain.add(title, 0, 0, 2, 1);
        mainPain.add(customerID, 0, 1);
        mainPain.add(customerName, 0, 2);
        mainPain.add(customerNameField, 1, 2);
        mainPain.add(activeLabel, 0, 3);
        mainPain.add(activeCheckBox, 1, 3);
        mainPain.add(address, 0, 4);
        mainPain.add(addressField, 1, 4);
        mainPain.add(address2, 0, 5);
        mainPain.add(address2Field, 1, 5);
        mainPain.add(postalCode, 0, 6);
        mainPain.add(postalCodeField, 1, 6);
        mainPain.add(phone, 0, 7);
        mainPain.add(phoneField, 1, 7);
        mainPain.add(city, 0, 8);
        mainPain.add(cityField, 1, 8);
        mainPain.add(country, 0, 9);
        mainPain.add(countryField, 1, 9);
        mainPain.add(save, 0, 10);
        mainPain.add(cancel, 1, 10);


        Stage newStage = new Stage();

        Scene scene = new Scene(mainPain);

        newStage.setScene(scene);
        newStage.sizeToScene();
        newStage.setResizable(false);
        newStage.show();


        cancel.setOnAction((ActionEvent a) -> {
            newStage.close();
        });

        // Save customer to DB
        save.setOnAction((ActionEvent s) -> {
            int active = 0;
            if (activeCheckBox.isSelected())
            {
                active = 1;
            }

            Customer updateCustomer = new Customer(0, customerNameField.getText(), active, addressField.getText(), address2Field.getText(), postalCodeField.getText(), phoneField.getText(), cityField.getText(), countryField.getText());

            try {
                customerDAO.updateCustomerRecord(customer, updateCustomer);

                newStage.close();
            } catch (Exception e) {
               alertDialog.accept(e.getMessage());

            }

        });


    }


    /**
     * Instantiates an ArrayList from the customers in the DB
     * @return the ArrayList<Customers> of the current customers in the DB
     */
    public ArrayList<Customer> getCustomerList() {
        Connection conn;
        ArrayList<Customer> customers = new ArrayList<>();
        try {
            conn = dbConnection.getDBConnection();

            String query = "select customerId,customerName, active, address, address2, postalCode, phone, city, country from customer a, address b, city c, country d where a.addressId = b.addressId and b.cityId = c.cityId and c.countryId = d.countryId";
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {

                customers.add(new Customer(
                        rs.getInt("customerId"),
                        rs.getString("customerName"),
                        rs.getInt("active"),
                        rs.getString("address"),
                        rs.getString("address2"),
                        rs.getString("postalCode"),
                        rs.getString("phone"),
                        rs.getString("city"),
                        rs.getString("country")));
            }
            rs.close();
            conn.close();
        } catch (Exception e) {
           alertDialog.accept(e.getMessage());

        }

        return customers;
    }


    /**
     * Gets the pre-generated Main View
     * @return the Mian View
     */
    public BorderPane getMainView() {
        return mainView;
    }



    /**
     * Generates TableView with results form the DB
     * @return a TableView of the query results.
     */
    public BorderPane reportView()
    {


        // Lambda to build and display data for reports
        Function<ResultSet, TableView> getResults = (ResultSet resultSet1) -> {



            TableView tableDataView = new TableView();
            ObservableList<ObservableList> data = FXCollections.observableArrayList();
            try {


                for (int i = 0; i < resultSet1.getMetaData().getColumnCount(); i++) {
                    final int j = i;
                    TableColumn col = new TableColumn(resultSet1.getMetaData().getColumnName(i + 1));
                    col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                        public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                            return new SimpleStringProperty(param.getValue().get(j).toString());
                        }
                    });

                    tableDataView.getColumns().addAll(col);
                }


                // Grab Data

                while (resultSet1.next()) {
                    //Iterate Row
                    ObservableList<String> row = FXCollections.observableArrayList();
                    for (int i = 1; i <= resultSet1.getMetaData().getColumnCount(); i++) {
                        //Iterate Column
                        row.add(resultSet1.getString(i));
                    }

                    data.add(row);

                }
            }
            catch (SQLException e)
            {

                alertDialog.accept(e.getMessage());
            }
            tableDataView.setItems(data);

            return tableDataView;
        };



        ToggleButton appointmentSummaryRollUp = new ToggleButton("Appointment Summary Rollup");
        ToggleButton consultantSchedule = new ToggleButton("Consultant Schedule");
        ToggleButton activeCustomers = new ToggleButton("Active Customers");



        ToggleGroup reportToggles = new ToggleGroup();

        HBox leftButtons = new HBox();

        VBox buttons = new VBox();
        HBox generateButton = new HBox();

        Button generateReport = new Button("Generate Report");

        leftButtons.getChildren().addAll(appointmentSummaryRollUp,consultantSchedule,activeCustomers);

        leftButtons.setSpacing(20);


        generateButton.getChildren().addAll(generateReport);

        leftButtons.setAlignment(Pos.CENTER_LEFT);


        generateButton.setAlignment(Pos.CENTER);

        buttons.setSpacing(100);
        reportToggles.getToggles().addAll(appointmentSummaryRollUp,consultantSchedule,activeCustomers);

        BorderPane mainPane = new BorderPane();

        buttons.getChildren().addAll(leftButtons,generateButton);
        buttons.setSpacing(50);




        generateReport.setOnAction((ActionEvent a)->{


            try {
                Connection conn = dbConnection.getDBConnection();

                Statement stmt = conn.createStatement();

                ResultSet resultSet;

                if (appointmentSummaryRollUp.isSelected()) {

                    String query = "SELECT description, MONTH(start) AS Month, COUNT(*) AS Occurrences FROM appointment GROUP BY description, Month";

                    Statement statement = conn.createStatement();

                    resultSet = statement.executeQuery(query);


                    mainPane.setCenter(getResults.apply(resultSet));
                }


                if (consultantSchedule.isSelected()) {

                    String query = "SELECT * FROM(SELECT contact AS Consultant, start, end FROM appointment GROUP BY createdBy) AS appointments";

                    Statement statement = conn.createStatement();

                    resultSet = statement.executeQuery(query);


                    mainPane.setCenter(getResults.apply(resultSet));                }

                if (activeCustomers.isSelected())
                {
                    String query = "SELECT * FROM customer";

                    Statement statement = conn.createStatement();

                    resultSet = statement.executeQuery(query);


                    mainPane.setCenter(getResults.apply(resultSet));
                }





            }catch(Exception e){
                alertDialog.accept(e.getMessage());
            }





        });

        mainPane.setTop(buttons);



        return mainPane;
    }


    private class RadioToggleButton extends ToggleButton {

        // As in RadioButton.


        @Override
        public void fire() {

            if (getToggleGroup() == null || !isSelected()) {
                super.fire();
            }
        }
    }




    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }




}



