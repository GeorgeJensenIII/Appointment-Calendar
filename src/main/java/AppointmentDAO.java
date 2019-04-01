import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Created by gwjense on 3/11/19.
 */
public class AppointmentDAO {

    private DBConnection dbConnection;
    private String currentUser;




    AppointmentDAO(DBConnection dbConnection, String currentUser) {
        this.dbConnection = dbConnection;
        this.currentUser = currentUser;


    }

    public void modifyAppointment(Appointment appointment) throws Exception{


            Connection conn = dbConnection.getDBConnection();
            ZonedDateTime hoursOfOperation = ZonedDateTime.of(appointment.getDateTime().toLocalDate(), LocalTime.of(9, 00), ZoneId.of("America/New_York"));
            if (appointment.getDateTime().isBefore(hoursOfOperation) || appointment.getDateTime().isAfter(hoursOfOperation.plusHours(8))) {

                throw new Exception(AlertMessages.APPOINTMENT_TIME_NOT_WITHIN_BUSINESS_HOURS);
            }

            // Check if customer exists
            if (!customerExists(appointment)) {
                throw new Exception(AlertMessages.CUSTOMER_DOES_NOT_EXIST);
            }

            // Check if there is already an appointment at that time.
            if (appointmentExists(appointment)) {

                throw new Exception(AlertMessages.APPOINTMENT_OVERLAPS_WITH_EXISTING_APPOINTMENT);

            }


            String query = "UPDATE appointment SET customerId = ?, title = ?, description = ?, location = ?, url = ?, start = ?, end = ?, lastUpdate = ?, lastUpdateBy = ? WHERE appointmentId = ?";


            PreparedStatement preparedStatement = conn.prepareStatement(query);


            Timestamp sqlTimeStamp = Timestamp.valueOf(LocalDateTime.now().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime());

            preparedStatement.setInt(1, appointment.getCustmoerId());
            preparedStatement.setString(2, "Title");
            preparedStatement.setString(3, appointment.getDescription());
            preparedStatement.setString(4, "here");
            preparedStatement.setString(5, "http://www.google.com");
            preparedStatement.setTimestamp(6, Timestamp.valueOf(appointment.getDateTime().withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime()));
            preparedStatement.setTimestamp(7, Timestamp.valueOf(appointment.getDateTime().plusMinutes(15).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime()));
            preparedStatement.setTimestamp(8, sqlTimeStamp);
            preparedStatement.setString(9, currentUser);
            preparedStatement.setInt(10, appointment.getAppointmentId());


            preparedStatement.executeUpdate();


    }

    public void newAppointment(Appointment appointment) throws Exception {

            Connection conn = dbConnection.getDBConnection();

            ZonedDateTime hoursOfOperation = ZonedDateTime.of(appointment.getDateTime().toLocalDate(), LocalTime.of(9, 00), ZoneId.of("America/New_York"));
            if (appointment.getDateTime().isBefore(hoursOfOperation) || appointment.getDateTime().isAfter(hoursOfOperation.plusHours(8))) {

                throw new Exception(AlertMessages.APPOINTMENT_TIME_NOT_WITHIN_BUSINESS_HOURS);

            }


            // Check if customer exists
            if (!customerExists(appointment)) {
                throw new Exception(AlertMessages.CUSTOMER_DOES_NOT_EXIST);
            }

            // Check if there is already an appointment at that time.
            if (appointmentExists(appointment)) {

                throw new Exception(AlertMessages.APPOINTMENT_OVERLAPS_WITH_EXISTING_APPOINTMENT);

            }


            String query = "INSERT INTO appointment (customerId, title, description, location, contact, url, start, end, createDate, createdBy, lastUpdate, lastUpdateBy) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            Timestamp sqlTimeStamp = Timestamp.valueOf(LocalDateTime.now().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime());

            PreparedStatement preparedStatement = conn.prepareStatement(query);

            preparedStatement.setInt(1, appointment.getCustmoerId());
            preparedStatement.setString(2, "Title");
            preparedStatement.setString(3, appointment.getDescription());
            preparedStatement.setString(4, "here");
            preparedStatement.setString(5, currentUser);
            preparedStatement.setString(6, "http://www.google.com");
            preparedStatement.setTimestamp(7, Timestamp.valueOf(appointment.getDateTime().withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime()));
            preparedStatement.setTimestamp(8, Timestamp.valueOf(appointment.getDateTime().plusMinutes(15).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime()));
            preparedStatement.setTimestamp(9, sqlTimeStamp);
            preparedStatement.setString(10, currentUser);
            preparedStatement.setTimestamp(11, sqlTimeStamp);
            preparedStatement.setString(12, currentUser);


            preparedStatement.execute();

            query = "SELECT LAST_INSERT_ID()";

            preparedStatement = conn.prepareStatement(query);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                appointment.setAppointmentId(resultSet.getInt(1));
            }


            resultSet.close();

    }

    public boolean customerExists(Appointment appointment) throws Exception {
        boolean exists = false;

        Connection conn = dbConnection.getDBConnection();

        String query = "SELECT customerId FROM customer WHERE customerName = ?";

        PreparedStatement preparedStatement = conn.prepareStatement(query);

        preparedStatement.setString(1, appointment.getCustomerName());

        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            appointment.setCustmoerId(resultSet.getInt("customerId"));
            exists = true;
        }
        return exists;
    }

    public boolean appointmentExists(Appointment appointment) throws Exception {
        boolean exists = false;

        Connection conn = dbConnection.getDBConnection();

        // Check if appointment exists before insert
        String query = "SELECT start description FROM appointment WHERE start BETWEEN ? AND ?";


        PreparedStatement preparedStatement = conn.prepareStatement(query);

        preparedStatement.setTimestamp(1, Timestamp.valueOf(appointment.getDateTime().minusMinutes(14).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime()));
        preparedStatement.setTimestamp(2, Timestamp.valueOf(appointment.getDateTime().plusMinutes(15).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime()));


        ResultSet resultSet = preparedStatement.executeQuery();


        if (resultSet.next()) {
            exists = true;
        }

        return exists;
    }

    public void removeAppointment(Appointment appointment) throws Exception
    {

            PreparedStatement preparedStatement;

            String query = "DELETE FROM appointment WHERE appointmentId = ?";

            preparedStatement = dbConnection.getDBConnection().prepareStatement(query);

            preparedStatement.setInt(1, appointment.getAppointmentId());

            preparedStatement.execute();

    }


}
