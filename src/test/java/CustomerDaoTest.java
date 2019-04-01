import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.mockito.runners.MockitoJUnitRunner;

import org.testng.Assert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


/**
 * Created by gwjense on 3/19/19.
 *
 * Tests created to test each method of CustomerDAO and different input parameters for those methods.
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomerDaoTest {
    public final int DOES_NOT_EXIST = -1;

    @Mock
    private Connection connection;

    @Mock
    private DBConnection dbConnection;

    @Mock
    private CustomerDAO mockCustomerDAO;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private Customer customer;

    @Before
    public void setup() {
        try {

            String currentUser = "Test";
            Mockito.doReturn(connection).when(dbConnection).getDBConnection();
            Mockito.doReturn(preparedStatement).when(connection).prepareStatement(Mockito.anyString());
            Mockito.doReturn(resultSet).when(preparedStatement).executeQuery();
            CustomerDAO customerDAO = new CustomerDAO(dbConnection, currentUser);
            mockCustomerDAO = Mockito.spy(customerDAO);

            customer = new Customer(1, "Tom", 1, "1234 Cherry Tree Lane", "", "12345", "555-555-5555", "London", "England");
        } catch (Exception e) {
            System.out.println("Failed Initialization");
        }
    }


    @Test
    public void customerExistsWhileInsertingCustomerRecordTest() {
        try {
            int customerId = 1;
            Mockito.doReturn(customerId).when(mockCustomerDAO).customerExists(customer);
            mockCustomerDAO.insertCustomerRecord(customer);
        } catch (Exception e) {
            Assert.assertEquals(e.getMessage(), AlertMessages.CUSTOMER_ALREADY_EXISTS_IN_DATABASE);
        }
    }


    @Test
    public void removeCustomerRecordTest() throws Exception {
        Mockito.doReturn(1).when(mockCustomerDAO).customerExists(customer);
        Mockito.doReturn(connection).when(dbConnection).getDBConnection();
        Mockito.doReturn(preparedStatement).when(connection).prepareStatement(Mockito.anyString());
        mockCustomerDAO.removeCustomerRecord(customer);
    }


    @Test
    public void removeCustomerRecordThatDoesNotExistTest() {
        try {
            Mockito.doReturn(DOES_NOT_EXIST).when(mockCustomerDAO).customerExists(customer);
            mockCustomerDAO.removeCustomerRecord(customer);

        } catch (Exception e) {
            Assert.assertEquals(e.getMessage(), AlertMessages.CUSTOMER_DOES_NOT_EXIST);
        }
    }

    @Test
    public void updateCustomerStatus() throws Exception {
        int active = 0;
        int addressId = 1;
        Customer updateCustomer = new Customer(1, "Tom", active, "1234 Cherry Tree Lane", "", "12345", "555-555-5555", "Surrey", "England");

        Mockito.doReturn(addressId).when(mockCustomerDAO).addressExists(customer);

        mockCustomerDAO.updateCustomerRecord(customer, updateCustomer);

        Mockito.verify(mockCustomerDAO, Mockito.times(1)).addressExists(customer);
        Mockito.verify(mockCustomerDAO, Mockito.times(1)).updateCustomer(customer, updateCustomer, addressId);

        Assert.assertEquals(customer.getActive(), updateCustomer.getActive());

    }

    @Test
    public void updateCustomerRecord_UpdateAddressWithNewStreetAddressTest() throws Exception {
        int cityId = 1;
        int addressId = 1;
        Customer updateCustomer = new Customer(1, "Tom", 1, "2100 Cherry Tree Lane", "", "12345", "555-555-5555", "London", "England");
        Mockito.doReturn(1).doReturn(DOES_NOT_EXIST).when(mockCustomerDAO).addressExists(Mockito.any());
        Mockito.doReturn(true).when(mockCustomerDAO).addressHasMutipleCustomers(1);
        Mockito.doReturn(1).when(mockCustomerDAO).cityExists(updateCustomer);
        Mockito.doReturn(addressId).when(mockCustomerDAO).insertAddress(updateCustomer, cityId);

        mockCustomerDAO.updateCustomerRecord(customer, updateCustomer);

        Mockito.verify(mockCustomerDAO, Mockito.times(1)).addressHasMutipleCustomers(1);
        Mockito.verify(mockCustomerDAO, Mockito.times(2)).addressExists(Mockito.any());
        Mockito.verify(mockCustomerDAO, Mockito.times(1)).insertAddress(updateCustomer, cityId);

        Assert.assertEquals(customer.getAddress(), updateCustomer.getAddress());
    }

    @Test
    public void updateCustomerRecord_UpdateAddressWithNewOtherAddressTest() throws Exception {
        int cityId = 1;
        int addressId = 1;
        Customer updateCustomer = new Customer(1, "Tom", 1, "1234 Cherry Tree Lane", "Apt. 101", "12345", "555-555-5555", "London", "England");

        // first call to addressExists checks for current addressId
        // second call to addressExists checks to see if new address is in the database
        Mockito.doReturn(1).doReturn(DOES_NOT_EXIST).when(mockCustomerDAO).addressExists(Mockito.any());

        Mockito.doReturn(true).when(mockCustomerDAO).addressHasMutipleCustomers(1);

        Mockito.doReturn(1).when(mockCustomerDAO).cityExists(updateCustomer);

        Mockito.doReturn(addressId).when(mockCustomerDAO).insertAddress(updateCustomer, cityId);

        mockCustomerDAO.updateCustomerRecord(customer, updateCustomer);

        Mockito.verify(mockCustomerDAO, Mockito.times(1)).addressHasMutipleCustomers(1);
        Mockito.verify(mockCustomerDAO, Mockito.times(2)).addressExists(Mockito.any());
        Mockito.verify(mockCustomerDAO, Mockito.times(1)).insertAddress(updateCustomer, cityId);

        Assert.assertEquals(customer.getAddress(), updateCustomer.getAddress());
    }

    @Test
    public void updateCustomerRecord_UpdateAddressWithNewCityTest() throws Exception {
        int cityId = 1;
        int countryId = 1;
        int addressId = 2;
        int oldAddress = 1;

        Customer updateCustomer = new Customer(1, "Tom", 1, "3433 River bend road", "", "12345", "555-555-5555", "Surrey", "England");

        Mockito.doReturn(oldAddress).when(mockCustomerDAO).addressExists(customer);
        Mockito.doReturn(countryId).when(mockCustomerDAO).countryExists(updateCustomer);
        Mockito.doReturn(DOES_NOT_EXIST).doReturn(1).when(mockCustomerDAO).cityExists(updateCustomer);
        Mockito.doReturn(cityId).when(mockCustomerDAO).insertCity(updateCustomer, countryId);
        Mockito.doReturn(addressId).when(mockCustomerDAO).addressExists(updateCustomer);
        Mockito.doReturn(true).when(mockCustomerDAO).addressHasMutipleCustomers(oldAddress);



        mockCustomerDAO.updateCustomerRecord(customer, updateCustomer);

        Mockito.verify(mockCustomerDAO, Mockito.times(1)).insertCity(updateCustomer, countryId);
        Mockito.verify(mockCustomerDAO, Mockito.times(1)).updateCustomer(customer, updateCustomer, addressId);

        Assert.assertEquals(customer.getCity(), updateCustomer.getCity());

    }


    @Test
    public void updateCustomerRecord_UpdateAddressWithExistingStreetAddressTest() throws Exception {
        Customer updateCustomer = new Customer(1, "Tom", 1, "2100 Cherry Tree Lane", "", "12345", "555-555-5555", "London", "England");
        Mockito.doReturn(1).doReturn(3).when(mockCustomerDAO).addressExists(Mockito.any());
        Mockito.doReturn(true).when(mockCustomerDAO).addressHasMutipleCustomers(1);
        Mockito.doReturn(1).when(mockCustomerDAO).cityExists(updateCustomer);

        mockCustomerDAO.updateCustomerRecord(customer, updateCustomer);

        Mockito.verify(mockCustomerDAO, Mockito.times(1)).addressHasMutipleCustomers(1);
        Mockito.verify(mockCustomerDAO, Mockito.times(2)).addressExists(Mockito.any());
        Mockito.verify(mockCustomerDAO, Mockito.times(1)).updateCustomer(customer, updateCustomer, 3);

        Assert.assertEquals(customer.getAddress(), updateCustomer.getAddress());
    }

    @Test
    public void updateCustomerRecord_UpdateAddressWithExistingOtherAddressTest() throws Exception {

        Customer updateCustomer = new Customer(1, "Tom", 1, "1234 Cherry Tree Lane", "Apt. 101", "12345", "555-555-5555", "London", "England");

        // first call to addressExists checks for current addressId
        // second call to addressExists checks to see if new address is in the database
        Mockito.doReturn(1).doReturn(3).when(mockCustomerDAO).addressExists(Mockito.any());

        Mockito.doReturn(true).when(mockCustomerDAO).addressHasMutipleCustomers(1);

        Mockito.doReturn(1).when(mockCustomerDAO).cityExists(updateCustomer);

        mockCustomerDAO.updateCustomerRecord(customer, updateCustomer);

        Mockito.verify(mockCustomerDAO, Mockito.times(1)).addressHasMutipleCustomers(1);
        Mockito.verify(mockCustomerDAO, Mockito.times(2)).addressExists(Mockito.any());
        Mockito.verify(mockCustomerDAO, Mockito.times(1)).updateCustomer(customer, updateCustomer, 3);

        Assert.assertEquals(customer.getAddress(), updateCustomer.getAddress());
    }


    @Test
    public void updateCustomerRecord_UpdateAddressWithExistingCityTest() throws Exception {
        int cityId = 1;
        int countryId = 1;
        int addressId = 1;
        int oldAddress = 2;

        Customer updateCustomer = new Customer(1, "Tom", 1, "1234 Tough Road", "", "12345", "555-555-5555", "Surrey", "England");

        Mockito.doReturn(countryId).when(mockCustomerDAO).countryExists(updateCustomer);
        Mockito.doReturn(oldAddress).when(mockCustomerDAO).addressExists(customer);
        Mockito.doReturn(cityId).when(mockCustomerDAO).cityExists(updateCustomer);
        Mockito.doReturn(false).when(mockCustomerDAO).addressHasMutipleCustomers(oldAddress);


        mockCustomerDAO.updateCustomerRecord(customer, updateCustomer);

        Mockito.verify(mockCustomerDAO, Mockito.times(1)).updateAddress(updateCustomer, oldAddress, cityId);

        Assert.assertEquals(customer.getCity(), updateCustomer.getCity());

    }

    @Test
    public void updateCustomerRecord_UpdateCountryWithExistingCountryTest() throws Exception {
        int countryId = 1;
        int addressId = 1;
        Customer updateCustomer = new Customer(1, "Tom", 1, "1234 Cherry Tree Lane", "", "12345", "555-555-5555", "Surrey", "France");

        Mockito.doReturn(addressId).when(mockCustomerDAO).addressExists(updateCustomer);
        Mockito.doReturn(countryId).when(mockCustomerDAO).countryExists(updateCustomer);

        mockCustomerDAO.updateCustomerRecord(customer, updateCustomer);

        Assert.assertEquals(customer.getCountry(), updateCustomer.getCountry());

    }

    @Test
    public void updateCustomerRecord_UpdateCountryWithNewCountryTest() throws Exception
    {
        int oldAddressId = 1;
        int countryId = 1;
        int cityId = 1;
        int addressId = 2;
        int customerId = 1;


        Customer updateCustomer = new Customer(1, "Tom", 1, "Main Street", "", "12345", "555-555-5555", "Surrey", "France");

        Mockito.doReturn(oldAddressId).when(mockCustomerDAO).addressExists(customer);
        Mockito.doReturn(true).when(mockCustomerDAO).addressHasMutipleCustomers(Mockito.anyInt());
        Mockito.doReturn(DOES_NOT_EXIST).doReturn(countryId).when(mockCustomerDAO).countryExists(updateCustomer);
        Mockito.doReturn(countryId).when(mockCustomerDAO).insertCountry(updateCustomer);
        Mockito.doReturn(DOES_NOT_EXIST).doReturn(cityId).when(mockCustomerDAO).cityExists(updateCustomer);
        Mockito.doReturn(cityId).when(mockCustomerDAO).insertCity(updateCustomer, countryId);
        Mockito.doReturn(DOES_NOT_EXIST).when(mockCustomerDAO).addressExists(updateCustomer);
        Mockito.doReturn(addressId).when(mockCustomerDAO).insertAddress(updateCustomer, cityId);
        Mockito.doReturn(customerId).when(mockCustomerDAO).customerExists(updateCustomer);


        mockCustomerDAO.updateCustomerRecord(customer, updateCustomer);

        Mockito.verify(mockCustomerDAO, Mockito.times(1)).insertCountry(updateCustomer);
        Mockito.verify(mockCustomerDAO, Mockito.times(1)).insertCity(updateCustomer, countryId);
        Mockito.verify(mockCustomerDAO, Mockito.times(1)).insertAddress(updateCustomer, cityId);
        Mockito.verify(mockCustomerDAO, Mockito.times(1)).updateCustomer(customer, updateCustomer, addressId);

        Assert.assertEquals(updateCustomer.getAddress(), updateCustomer.getAddress());
    }


    @Test
    public void updateCustomerRecord_WhileCustomerExistsRecordTest() throws Exception {

        try {


            Customer updateCustomer = new Customer(1, "Harry", 1, "1234 Cherry Tree Lane", "", "12345", "555-555-5555", "London", "England");
            Mockito.doReturn(1).when(mockCustomerDAO).addressExists(customer);
            Mockito.doReturn(1).when(mockCustomerDAO).customerExists(updateCustomer);
            mockCustomerDAO.updateCustomerRecord(customer, updateCustomer);
        } catch (Exception e) {
            Assert.assertEquals(e.getMessage(), AlertMessages.CUSTOMER_NAME_IS_ALREADY_TAKEN);
        }

    }


    @Test
    public void insertCustomerRecord_WithNewStreetAddressTest() throws Exception {
        int countryId = 1;
        int cityId = 1;
        int addressId = 1;
        int customerId = 1;
        Mockito.doReturn(DOES_NOT_EXIST).when(mockCustomerDAO).customerExists(customer);
        Mockito.doReturn(countryId).when(mockCustomerDAO).countryExists(customer);
        Mockito.doReturn(cityId).when(mockCustomerDAO).cityExists(customer);
        Mockito.doReturn(DOES_NOT_EXIST).when(mockCustomerDAO).addressExists(Mockito.any());
        Mockito.doReturn(addressId).when(mockCustomerDAO).insertAddress(customer, cityId);

        mockCustomerDAO.insertCustomerRecord(customer);

        Mockito.verify(mockCustomerDAO, Mockito.times(1)).addressExists(Mockito.any());
        Mockito.verify(mockCustomerDAO, Mockito.times(1)).insertAddress(customer, cityId);

    }


    @Test
    public void insertCustomerRecord_WithNewCityTest() throws Exception {
        int cityId = 1;
        int countryId = 1;
        int addressId = 1;
        int customerId = 1;


        Customer newCustomer = new Customer(1, "Tom", 1, "1234 Cherry Tree Lane", "", "12345", "555-555-5555", "Surrey", "England");

        Mockito.doReturn(DOES_NOT_EXIST).when(mockCustomerDAO).customerExists(newCustomer);
        Mockito.doReturn(countryId).when(mockCustomerDAO).countryExists(newCustomer);
        Mockito.doReturn(DOES_NOT_EXIST).when(mockCustomerDAO).addressExists(newCustomer);
        Mockito.doReturn(DOES_NOT_EXIST).when(mockCustomerDAO).cityExists(newCustomer);
        Mockito.doReturn(cityId).when(mockCustomerDAO).insertCity(newCustomer,countryId);
        Mockito.doReturn(addressId).when(mockCustomerDAO).insertAddress(newCustomer, cityId);
        Mockito.doReturn(customerId).when(mockCustomerDAO).insertCustomer(newCustomer, addressId);

        mockCustomerDAO.insertCustomerRecord(newCustomer);

        Mockito.verify(mockCustomerDAO, Mockito.times(1)).insertCity(newCustomer, countryId);
        Mockito.verify(mockCustomerDAO, Mockito.times(1)).insertAddress(newCustomer, addressId);
        Mockito.verify(mockCustomerDAO, Mockito.times(1)).insertCustomer(newCustomer, addressId);

    }


    @Test
    public void insertCustomerRecord_AddressWithExistingStreetAddressTest() throws Exception {
        int countryId = 1;
        int cityId = 1;
        int addressId = 1;
        int customerId = 1;

        Mockito.doReturn(DOES_NOT_EXIST).when(mockCustomerDAO).customerExists(customer);
        Mockito.doReturn(countryId).when(mockCustomerDAO).countryExists(customer);
        Mockito.doReturn(cityId).when(mockCustomerDAO).cityExists(customer);
        Mockito.doReturn(addressId).when(mockCustomerDAO).addressExists(customer);
        Mockito.doReturn(customerId).when(mockCustomerDAO).insertCustomer(customer, addressId);


        mockCustomerDAO.insertCustomerRecord(customer);

        Mockito.verify(mockCustomerDAO, Mockito.times(1)).insertCustomer(customer, addressId);

    }


    @Test
    public void insertCustomerRecord_AddressWithExistingCityTest() throws Exception {
        int countryId = 1;
        int cityId = 1;
        int addressId = 1;
        int customerId = 1;


        Mockito.doReturn(countryId).when(mockCustomerDAO).countryExists(customer);
        Mockito.doReturn(cityId).when(mockCustomerDAO).cityExists(customer);
        Mockito.doReturn(DOES_NOT_EXIST).when(mockCustomerDAO).addressExists(customer);
        Mockito.doReturn(addressId).when(mockCustomerDAO).insertAddress(customer, cityId);
        Mockito.doReturn(customerId).when(mockCustomerDAO).insertCustomer(customer, addressId);


        mockCustomerDAO.insertCustomerRecord(customer);

        Mockito.verify(mockCustomerDAO, Mockito.times(1)).insertAddress(customer, cityId);
        Mockito.verify(mockCustomerDAO, Mockito.times(1)).insertCustomer(customer, addressId);

    }

    @Test
    public void insertCustomerRecord_AddressWithExistingCountryTest() throws Exception
    {
        int countryId = 1;
        int cityId = 1;
        int addressId = 1;
        int customerId = 1;

        Mockito.doReturn(countryId).when(mockCustomerDAO).countryExists(customer);
        Mockito.doReturn(DOES_NOT_EXIST).when(mockCustomerDAO).cityExists(customer);
        Mockito.doReturn(cityId).when(mockCustomerDAO).insertCity(customer, countryId);
        Mockito.doReturn(DOES_NOT_EXIST).when(mockCustomerDAO).addressExists(customer);
        Mockito.doReturn(addressId).when(mockCustomerDAO).insertAddress(customer, cityId);
        Mockito.doReturn(DOES_NOT_EXIST).when(mockCustomerDAO).customerExists(customer);
        Mockito.doReturn(customerId).when(mockCustomerDAO).insertCustomer(customer, addressId);

        mockCustomerDAO.insertCustomerRecord(customer);

        Mockito.verify(mockCustomerDAO, Mockito.times(1)).insertCity(customer, countryId);
        Mockito.verify(mockCustomerDAO, Mockito.times(1)).insertAddress(customer, cityId);
        Mockito.verify(mockCustomerDAO, Mockito.times(1)).insertCustomer(customer, addressId);
    }

    @Test
    public void insertCustomerRecord_AddressWithNewCountryTest() throws Exception
    {
        int countryId = 1;
        int cityId = 1;
        int addressId = 1;
        int customerId = 1;

        Mockito.doReturn(DOES_NOT_EXIST).when(mockCustomerDAO).countryExists(customer);
        Mockito.doReturn(countryId).when(mockCustomerDAO).insertCountry(customer);
        Mockito.doReturn(DOES_NOT_EXIST).when(mockCustomerDAO).cityExists(customer);
        Mockito.doReturn(cityId).when(mockCustomerDAO).insertCity(customer, countryId);
        Mockito.doReturn(DOES_NOT_EXIST).when(mockCustomerDAO).addressExists(customer);
        Mockito.doReturn(addressId).when(mockCustomerDAO).insertAddress(customer, cityId);
        Mockito.doReturn(DOES_NOT_EXIST).when(mockCustomerDAO).customerExists(customer);
        Mockito.doReturn(customerId).when(mockCustomerDAO).insertCustomer(customer, addressId);

        mockCustomerDAO.insertCustomerRecord(customer);

        Mockito.verify(mockCustomerDAO, Mockito.times(1)).insertCountry(customer);
        Mockito.verify(mockCustomerDAO, Mockito.times(1)).insertCity(customer, countryId);
        Mockito.verify(mockCustomerDAO, Mockito.times(1)).insertAddress(customer, cityId);
        Mockito.verify(mockCustomerDAO, Mockito.times(1)).insertCustomer(customer, addressId);
    }

    @Test
    public void insertCustomerRecord_CustomerExistsTest() throws Exception {

        int customerId = 1;

        try {
            Mockito.doReturn(customerId).when(mockCustomerDAO).customerExists(customer);

            mockCustomerDAO.insertCustomerRecord(customer);


        } catch (Exception e) {
            Assert.assertEquals(e.getMessage(), AlertMessages.CUSTOMER_ALREADY_EXISTS_IN_DATABASE);
        }

    }


}
