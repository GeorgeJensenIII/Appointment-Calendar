/**
 * Created by gwjense on 3/8/19.
 */

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Data Set wrapper
 */
public class DayOfMonth {
    private ObservableList<Appointment> appointments;
    private ArrayList<Appointment> appointmentList;
    Consumer<String> alertDialog;
    private LocalDate date;

    DayOfMonth(LocalDate date, Consumer<String> alertLambda) {
        this.date = date;
        appointmentList = new ArrayList<>();
        appointments = FXCollections.observableArrayList(appointmentList);
        alertDialog = alertLambda;

    }

    public ObservableList<Appointment> getAppointments() {
        return appointments;
    }


    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
