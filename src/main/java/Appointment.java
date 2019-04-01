import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.ZonedDateTime;


/**
 * Created by gwjense on 7/16/17.
 */
public class Appointment {
    private final SimpleStringProperty description;
    private final SimpleObjectProperty<ZonedDateTime> dateTime;

    private int custmoerId;
    private int appointmentId;
    private String customerName;



    public Appointment(String description, ZonedDateTime dateTime, int appointmentId, int custmoerId) {
        this(description,dateTime,appointmentId,custmoerId,"");

    }

    public Appointment(String description, ZonedDateTime dateTime, int appointmentId, int custmoerId, String customerName) {
        this.dateTime= new SimpleObjectProperty<>(dateTime);
        this.description = new SimpleStringProperty(description);
        this.custmoerId = custmoerId;
        this.appointmentId = appointmentId;
        this.customerName = customerName;
    }

    public String getDescription() {
        return description.get();
    }

    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public ZonedDateTime getDateTime() {
        return dateTime.get();
    }

    public SimpleObjectProperty<ZonedDateTime> dateTimeProperty() {
        return dateTime;
    }

    public void setDateTime(ZonedDateTime dateTime) {
        this.dateTime.set(dateTime);
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public int getCustmoerId() {
        return custmoerId;
    }

    public void setCustmoerId(int custmoerId) {
        this.custmoerId = custmoerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
}
