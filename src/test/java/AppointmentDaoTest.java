import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;


/**
 * Created by gwjense on 3/8/19.
 */
@RunWith(MockitoJUnitRunner.class)
public class AppointmentDaoTest {

    @Mock
    private Connection connection;

    @Mock
    private DBConnection dbConnection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private AppointmentDAO mockAppointmentDAO;

    @Mock
    private ResultSet resultSet;

    private Appointment appointment;
    private String currentUser = "test";


    @Before
    public void setUp() throws Exception {
        AppointmentDAO appointmentDAO = new AppointmentDAO(dbConnection, currentUser);
        mockAppointmentDAO = Mockito.spy(appointmentDAO);

        appointment = new Appointment("Test Appointment", ZonedDateTime.of(2019, 3, 19, 13, 00, 00, 00, ZoneId.of("America/New_York")), 1, 1, "Harry");

        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        Mockito.doReturn(connection).when(dbConnection).getDBConnection();
    }

    @Test
    public void newAppointmentDaoTest() throws Exception {


        Mockito.doReturn(true).when(mockAppointmentDAO).customerExists(appointment);
        Mockito.doReturn(false).when(mockAppointmentDAO).appointmentExists(appointment);
        mockAppointmentDAO.newAppointment(appointment);

        verify(mockAppointmentDAO).newAppointment(Mockito.any(Appointment.class));
        verify(mockAppointmentDAO, times(1)).newAppointment(appointment);

    }

    @Test
    public void modifyAppointmentTest() throws Exception {

        Mockito.doReturn(true).when(mockAppointmentDAO).customerExists(appointment);
        Mockito.doReturn(false).when(mockAppointmentDAO).appointmentExists(appointment);
        mockAppointmentDAO.modifyAppointment(appointment);

        verify(mockAppointmentDAO).modifyAppointment(Mockito.any(Appointment.class));
        verify(mockAppointmentDAO, times(1)).modifyAppointment(appointment);
    }

    @Test
    public void appointmentExistsWhileAddingNewAppointmentTest() throws Exception {


        Mockito.doReturn(true).when(mockAppointmentDAO).customerExists(appointment);
        Mockito.doReturn(true).when(mockAppointmentDAO).appointmentExists(appointment);
        try
        {
            mockAppointmentDAO.newAppointment(appointment);
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), AlertMessages.APPOINTMENT_OVERLAPS_WITH_EXISTING_APPOINTMENT);
        }

        verify(mockAppointmentDAO).newAppointment(Mockito.any(Appointment.class));
        verify(mockAppointmentDAO, times(1)).newAppointment(appointment);

    }

    @Test
    public void appointmentExistsWhileModifyingAppointmentTest() throws Exception {


        Mockito.doReturn(true).when(mockAppointmentDAO).customerExists(appointment);
        Mockito.doReturn(true).when(mockAppointmentDAO).appointmentExists(appointment);

        try
        {
            mockAppointmentDAO.modifyAppointment(appointment);
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), AlertMessages.APPOINTMENT_OVERLAPS_WITH_EXISTING_APPOINTMENT);
        }

        verify(mockAppointmentDAO).modifyAppointment(Mockito.any(Appointment.class));
        verify(mockAppointmentDAO, times(1)).modifyAppointment(appointment);

    }

    @Test
    public void customerDoesNotExistWhileAddingNewAppointmentTest() throws Exception {
        Mockito.doReturn(false).when(mockAppointmentDAO).customerExists(appointment);
        Mockito.doReturn(false).when(mockAppointmentDAO).appointmentExists(appointment);

        try
        {
            mockAppointmentDAO.newAppointment(appointment);
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), AlertMessages.CUSTOMER_DOES_NOT_EXIST);
        }


        verify(mockAppointmentDAO).newAppointment(Mockito.any(Appointment.class));
        verify(mockAppointmentDAO, times(1)).newAppointment(appointment);
    }


    @Test
    public void customerDoesNotExistWhileModifyingAppointmentTest() throws Exception {
        Mockito.doReturn(false).when(mockAppointmentDAO).customerExists(appointment);
        Mockito.doReturn(false).when(mockAppointmentDAO).appointmentExists(appointment);

        try
        {
            mockAppointmentDAO.modifyAppointment(appointment);
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), AlertMessages.CUSTOMER_DOES_NOT_EXIST);
        }


        verify(mockAppointmentDAO).modifyAppointment(Mockito.any(Appointment.class));
        verify(mockAppointmentDAO, times(1)).modifyAppointment(appointment);
    }

    @Test
    public void appointmentOutsideOfBusinessHoursWhileAddingNewAppointmentTest() throws Exception {
        Appointment badAppointment = new Appointment("Test Appointment", ZonedDateTime.of(2019, 3, 19, 4, 00, 00, 00, ZoneId.of("America/New_York")), 1, 1, "Harry");

        try
        {
            mockAppointmentDAO.newAppointment(badAppointment);
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), AlertMessages.APPOINTMENT_TIME_NOT_WITHIN_BUSINESS_HOURS);
        }
    }


    @Test
    public void removeAppointmentTest() throws Exception {
        mockAppointmentDAO.removeAppointment(appointment);
        verify(mockAppointmentDAO).removeAppointment(Mockito.any(Appointment.class));
        verify(mockAppointmentDAO, times(1)).removeAppointment(appointment);
    }

}
