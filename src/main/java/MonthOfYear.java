/**
 * Created by gwjense on 3/8/19.
 */

import java.sql.*;
import java.time.*;
import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Data Set Wrapper
 */
public class MonthOfYear {
    private ArrayList<DayOfMonth> daysOfMonth;
    private YearMonth yearMonth;
    Consumer<String> alertDialog;

    MonthOfYear(YearMonth currentMonth, Consumer<String> alertLambda) {
        daysOfMonth = new ArrayList<>();
        yearMonth = currentMonth;
        alertDialog = alertLambda;

        for (int i = 1; i <= currentMonth.lengthOfMonth(); i++) {
            DayOfMonth dayOfMonth = new DayOfMonth(LocalDate.of(currentMonth.getYear(), currentMonth.getMonth(), i),alertDialog);
            daysOfMonth.add(dayOfMonth);

        }
        populateAppointmentList(daysOfMonth, currentMonth);

    }

    public ArrayList<DayOfMonth> getDaysOfMonth() {
        return daysOfMonth;
    }

    public YearMonth getYearMonth() {
        return yearMonth;
    }

    /**
     * Grabs appointments from the DB, then addeds them to the DayOfMonth objects.
     * @param daysOfMonth the list of the DayOfMonth objects for the current month.
     * @param currentMonth the current month.
     */
    public void populateAppointmentList(ArrayList<DayOfMonth> daysOfMonth, YearMonth currentMonth) {

        try {


            Connection conn = DriverManager.getConnection("jdbc:mysql://52.206.157.109/U041WH",
                    "U041WH", "53688147352");




            PreparedStatement preparedStatement;

            String query = "SELECT start, description, appointmentId, appointment.customerId, customerName FROM appointment JOIN customer ON appointment.customerId = customer.customerId WHERE start between ? and ?";

            preparedStatement = conn.prepareStatement(query);


            preparedStatement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.of(LocalDate.of(currentMonth.getYear(), currentMonth.getMonth(), 1), LocalTime.of(00, 00, 00)).atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime()));
            preparedStatement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.of(LocalDate.of(currentMonth.getYear(), currentMonth.getMonth(), currentMonth.lengthOfMonth()), LocalTime.of(23, 59, 59)).atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime()));


            ResultSet resultSet = preparedStatement.executeQuery();


            while (resultSet.next()) {

                Timestamp tsStart = resultSet.getTimestamp("start");

                ZoneId newzid = ZoneId.systemDefault();

                ZonedDateTime newzStart = tsStart.toLocalDateTime().atZone(ZoneId.of("UTC"));


                ZonedDateTime zonedDateTime = newzStart.withZoneSameInstant(newzid);



                daysOfMonth.get(zonedDateTime.getDayOfMonth() - 1).getAppointments().add(new Appointment(resultSet.getString("description"), zonedDateTime, resultSet.getInt("appointmentId"), resultSet.getInt("customerId"), resultSet.getString("customerName")));
            }


            resultSet.close();
            conn.close();
        }
        catch (SQLException e)
        {
            alertDialog.accept(e.getMessage());
        }
    }

}