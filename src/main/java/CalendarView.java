import com.mysql.jdbc.*;
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

import java.sql.*;
import java.sql.PreparedStatement;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;


/**
 * The main CalendarView UI Element
 */

public class CalendarView {


    private String currentUser;
    private HBox calendarViewControlBox;
    private HBox calendarMonthControlBox;
    private VBox calendarControlBox;
    private BorderPane calendar;
    private MonthView currentMonthView;
    private WeekView currentWeekView;
    private int monthIndex;
    private int weekIndex;
    private int startMonthValue;
    private DBConnection dbConnection;

    private ArrayList<MonthOfYear> monthsOfYear;
    private ArrayList<MonthView> monthViews;
    private ArrayList<WeekView> weekViews;
    private AppointmentDAO appointmentDAO;


    CalendarView(String currentUser) {

        calendarViewControlBox = new HBox();
        calendarMonthControlBox = new HBox();
        calendarControlBox = new VBox();
        calendar = new BorderPane();
        monthsOfYear = new ArrayList<>();
        monthViews = new ArrayList<>();
        weekViews = new ArrayList<>();
        monthIndex = 0;
        weekIndex = 0;
        this.currentUser = currentUser;
        startMonthValue = LocalDate.now().getMonthValue();
        dbConnection = new DBConnection();
        try {


            this.appointmentDAO = new AppointmentDAO(dbConnection, currentUser);
        } catch (Exception e)
        {
            alertDialog.accept(e.getMessage());
        }

        monthsOfYear.add(new MonthOfYear(YearMonth.now(), alertDialog));
        monthViews.add(new MonthView(alertDialog, monthsOfYear.get(0)));
        weekViews.add(new WeekView(alertDialog, LocalDate.now(), monthsOfYear.get(0)));
        currentMonthView = monthViews.get(0);
        currentWeekView = weekViews.get(0);

        // calendarViewControlBox

        RadioButton weekViewButton = new RadioButton("Week View");
        RadioButton monthViewButton = new RadioButton("Month View");

        ToggleGroup viewToggle = new ToggleGroup();
        viewToggle.getToggles().addAll(weekViewButton, monthViewButton);
        monthViewButton.setSelected(true);

        calendarViewControlBox.setSpacing(20);
        calendarViewControlBox.setAlignment(Pos.CENTER);
        calendarViewControlBox.setPadding(new Insets(24, 24, 24, 24));
        calendarViewControlBox.getChildren().addAll(weekViewButton, monthViewButton);

        // calendarMonthControlBox
        Button leftButton = new Button(" < ");
        leftButton.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        leftButton.setAlignment(Pos.CENTER_LEFT);
        Label currentMonthLabel = new Label(currentMonthView.getCurrentMonth().getYearMonth().getMonth().toString());
        currentMonthLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        currentMonthLabel.setAlignment(Pos.CENTER);
        Button rightButton = new Button(" > ");
        rightButton.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        rightButton.setAlignment(Pos.CENTER_RIGHT);
        calendarMonthControlBox.setPadding(new Insets(24, 48, 24, 48));
        calendarMonthControlBox.getChildren().addAll(leftButton, currentMonthLabel, rightButton);

        calendarControlBox.getChildren().addAll(calendarViewControlBox, calendarMonthControlBox);

        calendar.setTop(calendarControlBox);
        calendar.setCenter(currentMonthView.getMonthView());

        rightButton.setOnAction((ActionEvent a) -> {
            if (monthViewButton.isSelected()) {

                if (monthIndex + 1 >= monthViews.size()) {

                    monthsOfYear.add(new MonthOfYear(currentMonthView.getCurrentMonth().getYearMonth().plusMonths(1), alertDialog));
                    monthViews.add(new MonthView(alertDialog, monthsOfYear.get(currentMonthView.getCurrentMonth().getYearMonth().getMonth().plus(1).getValue() - startMonthValue)));
                    incrementMonth();
                    currentMonthLabel.setText(currentMonthView.getCurrentMonth().getYearMonth().getMonth().toString());
                } else {
                    incrementMonth();
                    currentMonthLabel.setText(currentMonthView.getCurrentMonth().getYearMonth().getMonth().toString());
                }
            } else {
                if (weekIndex + 1 >= weekViews.size()) {
                    if (currentWeekView.getCurrentMonth().getYearMonth().getMonth() != currentWeekView.getWeekEnding().plusDays(1).getMonth()) {
                        if (currentWeekView.getCurrentMonth().getYearMonth().getMonth().plus(1).getValue() > monthsOfYear.get(monthsOfYear.size() - 1).getYearMonth().getMonth().getValue()) {
                            monthsOfYear.add(new MonthOfYear(currentWeekView.currentMonth.getYearMonth().plusMonths(1), alertDialog));

                        }
                        if (currentWeekView.getWeekEnding().getMonth() != currentWeekView.getWeekEnding().minusDays(6).getMonth()) {
                            weekViews.add(new WeekView(alertDialog, LocalDate.of(currentWeekView.getWeekEnding().getYear(), currentWeekView.getWeekEnding().plusDays(1).getMonth(), 1), monthsOfYear.get(currentWeekView.getCurrentMonth().getYearMonth().getMonth().getValue() - monthsOfYear.get(0).getYearMonth().getMonth().getValue() + 1)));

                        } else {
                            weekViews.add(new WeekView(alertDialog, currentWeekView.getWeekEnding().plusDays(1), monthsOfYear.get((currentWeekView.getCurrentMonth().getYearMonth().getMonth().getValue() - monthsOfYear.get(0).getYearMonth().getMonth().getValue()) + 1)));
                        }
                    } else {
                        weekViews.add(new WeekView(alertDialog, currentWeekView.getWeekEnding().plusDays(1), currentWeekView.getCurrentMonth()));
                    }

                }

                if (currentWeekView.getCurrentMonth().getYearMonth().getMonth() != currentWeekView.getWeekEnding().plusDays(1).getMonth()) {
                    incrementWeek();
                    currentMonthLabel.setText(currentWeekView.getCurrentMonth().getYearMonth().getMonth().toString());
                } else {
                    incrementWeek();
                }
            }

        });

        leftButton.setOnAction((ActionEvent a) -> {
            if (monthViewButton.isSelected()) {
                if (monthIndex > 0) {

                    decrementMonth();
                    currentMonthLabel.setText(currentMonthView.getCurrentMonth().getYearMonth().getMonth().toString());
                } else {
                    alertDialog.accept("Begining of Calendar");
                }
            } else {
                if (weekIndex > 0) {
                    if (currentWeekView.getWeekEnding().getMonth() != currentWeekView.getWeekEnding().minusDays(7).getMonth()) {
                        decrementWeek();
                        currentMonthLabel.setText(currentWeekView.getCurrentMonth().getYearMonth().getMonth().toString());
                    } else {
                        decrementWeek();
                    }

                } else {
                    alertDialog.accept("Begining of calendar");
                }
            }

        });


        weekViewButton.setOnAction((ActionEvent e) -> {
            calendar.setCenter(currentWeekView.getWeekView());
            currentMonthLabel.setText(currentWeekView.getCurrentMonth().getYearMonth().getMonth().toString());
            for (WeekView wv : weekViews) {
                wv.refresh();
            }
        });

        monthViewButton.setOnAction((ActionEvent f) -> {
            calendar.setCenter(currentMonthView.getMonthView());
            currentMonthLabel.setText(currentMonthView.getCurrentMonth().getYearMonth().getMonth().toString());
            for (MonthView mv : monthViews) {
                mv.refresh();
            }
        });


    }


    /**
     * Populates the calendar with DayView elements
     *
     * @param currentMonth     the current month
     * @param dayViews         the ArrayList of DayViews
     * @param calendarDaysPane the UI element that is the actual calendar view
     * @param calendarDate     the current day.
     */
    public void populateCalendar(MonthOfYear currentMonth, ArrayList<DayView> dayViews, GridPane calendarDaysPane, LocalDate calendarDate) {

        // rollback to start of week SUNDAY
        while (!calendarDate.getDayOfWeek().toString().equals("SUNDAY")) {
            calendarDate = calendarDate.minusDays(1);
        }

        // populate calendar days

        int counter = 0;

        for (int row = 0; row < 6; row++) {

            for (int col = 0; col < 7; col++) {
                if (calendarDate.getMonth() == currentMonth.getYearMonth().getMonth()) {


                    dayViews.get(counter).setAppointmentList(currentMonth.getDaysOfMonth().get(calendarDate.getDayOfMonth() - 1).getAppointments());

                    calendarDaysPane.add(dayViews.get(counter).getDayView(), col, row);
                    counter++;
                } else {
                    Pane pane = new Pane();
                    pane.setStyle("-fx-background-color: grey;");
                    calendarDaysPane.add(pane, col, row);

                }
                calendarDate = calendarDate.plusDays(1);
            }
            if (dayViews.size() < 8) {
                row = row + 6;
            }

        }


    }

    public BorderPane getCalendarView() {
        return calendar;
    }

    public void incrementMonth() {
        monthIndex++;

        currentMonthView = monthViews.get(monthIndex);


        calendar.setCenter(null);
        calendar.setCenter(currentMonthView.getMonthView());

    }

    public void decrementMonth() {
        monthIndex--;
        currentMonthView = monthViews.get(monthIndex);
        calendar.setCenter(currentMonthView.getMonthView());

    }

    public void incrementWeek() {
        weekIndex++;
        currentWeekView = weekViews.get(weekIndex);

        calendar.setCenter(currentWeekView.getWeekView());

    }

    public void decrementWeek() {

        weekIndex--;
        currentWeekView = weekViews.get(weekIndex);
        calendar.setCenter(currentWeekView.getWeekView());

    }


    /**
     * DayView UI Element
     * Acts as it's own independent UI Element
     * Can be used as part of either a weekly view, or a monthly view
     */
    private class DayView {
        DayOfMonth dayOfMonth;
        private VBox dayView;
        private TableView<Appointment> dayList;
        private ObservableList<Appointment> appointmentList;
        Consumer<String> alertDialog;

        DayView(DayOfMonth dayOfMonth, Consumer<String> alertLambda) {
            this.dayOfMonth = dayOfMonth;
            appointmentList = dayOfMonth.getAppointments();
            alertDialog = alertLambda;
        }


        private VBox getDayView() {

            dayView = new VBox();
            dayList = new TableView<>();

            dayList.setPrefHeight(600);

            Text calendarDate = new Text(String.valueOf(dayOfMonth.getDate().getDayOfMonth()));


            TableColumn<Appointment, String> description = new TableColumn<>("Description");
            description.setCellValueFactory(new PropertyValueFactory<>("description"));
            dayList.getColumns().add(description);

            TableColumn<Appointment, ZonedDateTime> ldtCol = new TableColumn<>("LDT");

            ldtCol.setCellValueFactory(cellData -> cellData.getValue().dateTimeProperty());
            ldtCol.setCellFactory(col -> new TableCell<Appointment, ZonedDateTime>() {
                @Override
                protected void updateItem(ZonedDateTime item, boolean empty) {

                    super.updateItem(item, empty);
                    if (empty)
                        setText(null);
                    else
                        setText(item.getHour() + ":" + item.getMinute());
                }
            });

            dayList.getColumns().add(ldtCol);

            dayList.setItems(appointmentList);
            dayList.setId("tableView");
            dayList.getStylesheets().add("stylesheet.css");
            dayView.getChildren().addAll(calendarDate, dayList);
            dayView.setAlignment(Pos.CENTER_LEFT);
            dayView.setSpacing(2);


            dayList.setOnMousePressed(event -> {
                if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {

                    if (dayList.getSelectionModel().getSelectedItem() == null) {
                        Appointment appointment = new Appointment("Appointment Description", ZonedDateTime.of(dayOfMonth.getDate(), LocalTime.of(12, 00), ZoneId.systemDefault()), 0, 0);
                        newAppointment(appointment);


                    } else {
                        modifyAppointmentView(dayList.getSelectionModel().getSelectedItem(), appointmentList);

                    }
                }
            });


            return dayView;
        }


        /**
         * Generates the Modify Appointment view
         *
         * @param appointment     the appointment to be modified
         * @param appointmentList the observable list of appointments
         */
        public void modifyAppointmentView(Appointment appointment, ObservableList<Appointment> appointmentList) {

            GridPane mainPain = new GridPane();
            Stage modifyAppointmentStage = new Stage();
            Scene newScene = new Scene(mainPain, 600, 600);

            Label title = new Label("Modify Appointment Screen");
            title.setFont(Font.font("Arial", FontWeight.BOLD, 24));

            Label descriptionLabel = new Label("Appointment Description:");
            TextField descriptionTextField = new TextField();
            descriptionTextField.setText(appointment.getDescription());


            Label businessHours = new Label("Business Hours:");
            Label weekdayHours = new Label("Monday - Friday  9AM = 5PM EST");


            Label appointmentTimeLable = new Label("Appointment Time");

            Label customerName = new Label("Customer Name");
            TextField customerNameField = new TextField();
            customerNameField.setText(appointment.getCustomerName());


            HBox buttons = new HBox();
            Button save = new Button("Save");
            Button delete = new Button("Delete");
            Button cancel = new Button("Cancel");
            buttons.getChildren().addAll(save, delete, cancel);
            buttons.setSpacing(20);
            buttons.setAlignment(Pos.CENTER);
            TimeDial timeDial = new TimeDial(appointment.getDateTime().toLocalTime());

            mainPain.add(title, 0, 0, 2, 1);
            mainPain.add(descriptionLabel, 0, 1);
            mainPain.add(descriptionTextField, 0, 2);
            mainPain.add(businessHours, 0, 3);
            mainPain.add(weekdayHours, 0, 4);
            mainPain.add(appointmentTimeLable, 0, 5);
            mainPain.add(timeDial, 0, 6);
            mainPain.add(customerName, 0, 7);
            mainPain.add(customerNameField, 0, 8);
            mainPain.add(buttons, 0, 9, 2, 2);

            modifyAppointmentStage.setScene(newScene);
            modifyAppointmentStage.setResizable(false);
            modifyAppointmentStage.show();


            save.setOnAction((ActionEvent a) -> {


                appointment.setDateTime(LocalDateTime.of(appointment.getDateTime().toLocalDate(), LocalTime.parse(timeDial.getEditor().getText())).atZone(ZoneId.systemDefault()));
                appointment.setDescription(descriptionTextField.getText());
                appointment.setCustomerName(customerNameField.getText());


                try {
                    appointmentDAO.modifyAppointment(appointment);
                    modifyAppointmentStage.close();
                } catch (Exception e) {
                    alertDialog.accept(e.getMessage());
                }

            });

            delete.setOnAction((ActionEvent f) -> {
                try {
                    appointmentDAO.removeAppointment(appointment);
                    appointmentList.remove(appointment);
                    modifyAppointmentStage.close();
                } catch (Exception e) {
                    alertDialog.accept(e.getMessage());

                }
            });


            cancel.setOnAction((ActionEvent e) -> {
                modifyAppointmentStage.close();
            });

        }

        private ObservableList<Appointment> getAppointmentList() {
            return appointmentList;
        }

        private void setAppointmentList(ObservableList<Appointment> appointmentList) {
            this.appointmentList = appointmentList;
        }

        private TableView<Appointment> getDayList() {
            return dayList;
        }

        public void setDayList(TableView<Appointment> dayList) {
            this.dayList = dayList;
        }

        /**
         * Generates the New Appointment view
         *
         * @param appointment the appointment to be created -- pre-populated by calendar touch events
         */
        public void newAppointment(Appointment appointment) {


            GridPane mainPain = new GridPane();
            Stage newAppointmentStage = new Stage();
            Scene newScene = new Scene(mainPain, 600, 600);

            Label title = new Label("New Appointment Screen");
            title.setFont(Font.font("Arial", FontWeight.BOLD, 24));

            Label descriptionLabel = new Label("Appointment Description:");
            TextField descriptionTextField = new TextField();
            descriptionTextField.setText(appointment.getDescription());

            Label businessHours = new Label("Business Hours:");
            Label weekdayHours = new Label("Monday - Friday  9AM = 5PM EST");


            Label appointmentTimeLable = new Label("Appointment Time");

            TimeDial timeDial = new TimeDial(appointment.getDateTime().toLocalTime());


            Label customerName = new Label("Customer Name");
            TextField customerNameField = new TextField("");


            HBox buttons = new HBox();
            Button save = new Button("Save");
            Button cancel = new Button("Cancel");
            buttons.getChildren().addAll(save, cancel);
            buttons.setSpacing(20);
            buttons.setAlignment(Pos.CENTER);

            mainPain.add(title, 0, 0, 2, 1);
            mainPain.add(descriptionLabel, 0, 1);
            mainPain.add(descriptionTextField, 0, 2);
            mainPain.add(businessHours, 0, 3);
            mainPain.add(weekdayHours, 0, 4);
            mainPain.add(appointmentTimeLable, 0, 5);
            mainPain.add(timeDial, 0, 6);
            mainPain.add(customerName, 0, 7);
            mainPain.add(customerNameField, 0, 8);
            mainPain.add(buttons, 0, 9, 2, 2);

            newAppointmentStage.setScene(newScene);
            newAppointmentStage.setResizable(false);
            newAppointmentStage.show();


            save.setOnAction((ActionEvent a) -> {

                appointment.setDescription(descriptionTextField.getText());
                appointment.setDateTime(LocalDateTime.of(appointment.getDateTime().toLocalDate(), LocalTime.parse(timeDial.getEditor().getText())).atZone(ZoneId.systemDefault()));
                appointment.setCustomerName(customerNameField.getText());


                try {
                    appointmentDAO.newAppointment(appointment);
                    appointmentList.add(appointment);
                    newAppointmentStage.close();
                } catch (Exception e) {
                    alertDialog.accept(e.getMessage());

                }

            });

            cancel.setOnAction((ActionEvent e) -> {
                newAppointmentStage.close();
            });

        }


    }


    /**
     * Generates the Calendar View for the Month and then calls the populateCalendar to populate each day with data from the data set
     */
    private class MonthView {

        ArrayList<DayView> dayViews;
        Consumer<String> alertDialog;
        VBox monthView;
        MonthOfYear currentMonth;

        MonthView(Consumer<String> alertLambda, MonthOfYear currentMonth) {
            dayViews = new ArrayList<>();
            alertDialog = alertLambda;
            monthView = new VBox();
            this.currentMonth = currentMonth;

            GridPane calendarDaysTitlePane = new GridPane();

            Text[] daysOfWeek = new Text[]{new Text("Sunday"), new Text("Monday"), new Text("Tuesday"), new Text("Wednesday"), new Text("Thursday"), new Text("Friday"), new Text("Saturday")};

            int col = 0;

            RowConstraints r = new RowConstraints(30);
            calendarDaysTitlePane.getRowConstraints().add(r);

            for (Text text : daysOfWeek) {
                ColumnConstraints column = new ColumnConstraints(175);
                calendarDaysTitlePane.getColumnConstraints().add(column);
                calendarDaysTitlePane.setGridLinesVisible(true);

                calendarDaysTitlePane.add(text, col, 0);
                col++;
            }


            GridPane monthViewPane = new GridPane();
            monthViewPane.setGridLinesVisible(true);


            for (int i = 0; i < currentMonth.getYearMonth().getMonth().length(currentMonth.getYearMonth().isLeapYear()); i++) {
                dayViews.add(new DayView(currentMonth.getDaysOfMonth().get(i), alertDialog));
            }


            for (int i = 0; i < 6; i++) {
                RowConstraints row = new RowConstraints(100);
                monthViewPane.getRowConstraints().add(row);
            }

            for (int i = 0; i < 7; i++) {
                ColumnConstraints column = new ColumnConstraints(175);
                monthViewPane.getColumnConstraints().add(column);
            }


            populateCalendar(this.getCurrentMonth(), dayViews, monthViewPane, currentMonth.getDaysOfMonth().get(1).getDate());


            monthView.getChildren().addAll(calendarDaysTitlePane, monthViewPane);


        }

        public MonthOfYear getCurrentMonth() {
            return currentMonth;
        }

        public VBox getMonthView() {

            return monthView;
        }


        public void refresh() {
            for (DayView d : dayViews) {
                d.getDayList().setItems(d.getAppointmentList());
            }
        }

    }


    /**
     * Generates the Calendar View for the Week and then calls the populateCalendar to populate each day with data from the data set
     */
    private class WeekView {

        ArrayList<DayView> dayViews;
        Consumer<String> alertDialog;
        LocalDate date;
        LocalDate weekEnding;
        MonthOfYear currentMonth;
        VBox weekView;


        WeekView(Consumer<String> alertLambda, LocalDate date, MonthOfYear currentMonth) {
            dayViews = new ArrayList<>();
            alertDialog = alertLambda;

            this.currentMonth = currentMonth;
            this.date = date;

            weekView = new VBox();


            GridPane calendarDaysTitlePane = new GridPane();

            Text[] daysOfWeek = new Text[]{new Text("Sunday"), new Text("Monday"), new Text("Tuesday"), new Text("Wednesday"), new Text("Thursday"), new Text("Friday"), new Text("Saturday")};

            int col = 0;

            RowConstraints r = new RowConstraints(30);
            calendarDaysTitlePane.getRowConstraints().add(r);

            for (Text text : daysOfWeek) {
                ColumnConstraints column = new ColumnConstraints(175);
                calendarDaysTitlePane.getColumnConstraints().add(column);
                calendarDaysTitlePane.setGridLinesVisible(true);

                calendarDaysTitlePane.add(text, col, 0);
                col++;
            }

            GridPane weekViewPane = new GridPane();


            weekViewPane.setGridLinesVisible(true);

            RowConstraints row = new RowConstraints(600);
            weekViewPane.getRowConstraints().add(row);


            for (int i = 0; i < 7; i++) {
                ColumnConstraints column = new ColumnConstraints(175);
                weekViewPane.getColumnConstraints().add(column);
            }

            while (!date.getDayOfWeek().toString().equals("SUNDAY")) {
                date = date.minusDays(1);
            }

            weekEnding = date.plusDays(6);


            for (; ChronoUnit.DAYS.between(date, weekEnding) >= 0; date = date.plusDays(1)) {
                if (date.getMonth() == currentMonth.getYearMonth().getMonth()) {

                    dayViews.add(new DayView(currentMonth.getDaysOfMonth().get(date.getDayOfMonth() - 1), alertDialog));

                }
            }


            populateCalendar(currentMonth, dayViews, weekViewPane, weekEnding.minusDays(6));

            weekView.getChildren().addAll(calendarDaysTitlePane, weekViewPane);


        }


        public void refresh() {
            for (DayView d : dayViews) {
                d.dayList.setItems(d.getAppointmentList());
            }
        }

        public VBox getWeekView() {

            return weekView;
        }

        private LocalDate getWeekEnding() {
            return weekEnding;
        }

        private MonthOfYear getCurrentMonth() {
            return currentMonth;
        }


    }

    /**
     * Alert Dialog Method
     *
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


}




