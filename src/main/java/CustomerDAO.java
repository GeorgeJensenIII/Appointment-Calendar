import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.function.Consumer;

/**
 * Created by gwjense on 3/8/19.
 */
public class CustomerDAO {

    private DBConnection dbConnection;
    private String currentUser;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private Timestamp sqlTimeStamp;

    CustomerDAO(DBConnection dbConnection, String currentUser) {
        this.currentUser = currentUser;
        this.dbConnection = dbConnection;
        sqlTimeStamp = Timestamp.valueOf(LocalDateTime.now().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime());

    }

    /**
     * Inserts the specified customer into the DB.
     *
     * @param customer the customer to be inserted.
     * @return customerId if customer is new, 0 if existing customer.
     * @throws SQLException if there are any issues.
     */
    public void insertCustomerRecord(Customer customer) throws Exception {

        int customerId = customerExists(customer);

        if (customerId == -1) {

            int countryId = countryExists(customer);

            if (countryId == -1) {
                countryId = insertCountry(customer);
            }

            int cityId = cityExists(customer);

            if (cityId == -1) {
                cityId = insertCity(customer, countryId);
            }

            int addressId = addressExists(customer);

            if (addressId == -1) {
                addressId = insertAddress(customer, cityId);
            }

            customer.setCustomerId(insertCustomer(customer, addressId));

        } else {
            throw new Exception(AlertMessages.CUSTOMER_ALREADY_EXISTS_IN_DATABASE);
        }

    }


    /**
     * Updates the specified customer in the DB
     *
     * @param updateCustomer the updated customer object
     * @param customer       the customer object to be updated
     * @throws SQLException
     */
    public void updateCustomerRecord(Customer customer, Customer updateCustomer) throws Exception {
        int countryId;
        int cityId;
        int addressId;
        int oldAddressId = addressExists(customer);

        boolean countryChanged = (updateCustomer.getCountry().compareTo(customer.getCountry()) != 0);
        boolean cityChanged = (updateCustomer.getCity().compareTo(customer.getCity()) != 0);
        boolean addressAndOrCustomerDataChanged = (updateCustomer.getAddress().compareTo(customer.getAddress()) != 0 || updateCustomer.getAddress2().compareTo(customer.getAddress2()) != 0 || updateCustomer.getPhone().compareTo(customer.getPhone()) != 0 || updateCustomer.getPostalCode().compareTo(customer.getPostalCode()) != 0);

        // Check if country has changed
        if (countryChanged) {
            // Check if new country exists
            countryId = countryExists(updateCustomer);

            if (countryId == -1) {
                insertCountry(updateCustomer);
            }
        }

        // Check if city has changed
        if (cityChanged || countryChanged) {
            // Check if city exists before insert
            countryId = countryExists(updateCustomer);
            cityId = cityExists(updateCustomer);
            if (cityId == -1) {


                insertCity(updateCustomer, countryId);
            }

        }
        // Check if address/ phone has changed
        if (addressAndOrCustomerDataChanged || cityChanged) {
            cityId = cityExists(updateCustomer);

            // check if old address is in use by other customers
            if (addressHasMutipleCustomers(oldAddressId)) {

                // check if new address exists
                addressId = addressExists(updateCustomer);

                if (addressId == -1) {
                    // if address doesn't exist, then insert new address

                    addressId = insertAddress(updateCustomer, cityId);
                }
                // update customer record to reflect address change
                updateCustomer(customer, updateCustomer, addressId);

            } else {
                // change data of addressId to reflect new address
                addressId = oldAddressId;
                updateAddress(updateCustomer, addressId, cityId);

            }


        } else // address has not changed
        {
            addressId = oldAddressId;
        }


        if (updateCustomer.getCustomerName().compareTo(customer.getCustomerName()) != 0) {
            // update customerId to reflect changes to customer
            if (customerExists(updateCustomer) == -1) {
                updateCustomer(customer, updateCustomer, addressId);
            } else {
                throw new Exception(AlertMessages.CUSTOMER_NAME_IS_ALREADY_TAKEN);
            }
        }

        if (updateCustomer.getActive() != customer.getActive()) {
            updateCustomer(customer, updateCustomer, addressId);
        }

        customer.setCustomerName(updateCustomer.getCustomerName());
        customer.setActive(updateCustomer.getActive());
        customer.setAddress(updateCustomer.getAddress());
        customer.setAddress2(updateCustomer.getAddress2());
        customer.setPhone(updateCustomer.getPhone());
        customer.setPostalCode(updateCustomer.getPostalCode());
        customer.setCountry(updateCustomer.getCountry());
        customer.setCity(updateCustomer.getCity());

    }

    /**
     * Removes the specified customer from the database and the observable list
     *
     * @param customer the specified customer object to be removed
     */
    public void removeCustomerRecord(Customer customer) throws Exception {

        if (customerExists(customer) != -1) {
            Connection conn = dbConnection.getDBConnection();
            String query = "DELETE FROM customer WHERE customerName = ?";

            PreparedStatement preparedStatement = conn.prepareStatement(query);

            preparedStatement.setString(1, customer.getCustomerName());

            preparedStatement.executeUpdate();
        } else {
            throw new Exception(AlertMessages.CUSTOMER_DOES_NOT_EXIST);
        }
    }

    public int countryExists(Customer customer) throws Exception {

        int countryId = 0;

        Connection conn = dbConnection.getDBConnection();

        PreparedStatement preparedStatement;


        String query = "SELECT countryId FROM country WHERE country = ?";
        // Check if country exists before insert

        preparedStatement = conn.prepareStatement(query);

        preparedStatement.setString(1, customer.getCountry());

        resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            // if country exists get countryId
            countryId = resultSet.getInt(1);

        } else {
            countryId = -1;
        }

        return countryId;
    }

    public int customerExists(Customer customer) throws Exception {
        int customerId;

        Connection conn = dbConnection.getDBConnection();

        PreparedStatement preparedStatement;

        // Check if customer exists before insert
        String query = "SELECT customerName FROM customer WHERE customerName = ?";


        query = "SELECT customerId FROM customer WHERE customerName = ?";
        preparedStatement = conn.prepareStatement(query);
        preparedStatement.setString(1, customer.getCustomerName());
        resultSet = preparedStatement.executeQuery();


        if (resultSet.next()) {
            customerId = resultSet.getInt(1); // returns customerId
        } else {
            customerId = -1;
        }
        return customerId;
    }

    public int cityExists(Customer customer) throws Exception {
        int cityId = -1;

        Connection conn = dbConnection.getDBConnection();

        // Check if city exists before insert
        String query = "SELECT cityId FROM city WHERE city = ?";


        preparedStatement = conn.prepareStatement(query);

        preparedStatement.setString(1, customer.getCity());

        resultSet = preparedStatement.executeQuery();


        if (resultSet.next()) {
            // if city exists get cityId
            cityId = resultSet.getInt(1);

        }
        return cityId;
    }

    public int insertCountry(Customer customer) throws Exception {

        int countryId = 0;


        Connection conn = dbConnection.getDBConnection();
        String query = "INSERT INTO country (country, createDate, createdBy, lastUpdate, lastUpdateBy) VALUES (?, ?, ?, ?, ?)";


        preparedStatement = conn.prepareStatement(query);

        preparedStatement.setString(1, customer.getCountry());
        preparedStatement.setTimestamp(2, sqlTimeStamp);
        preparedStatement.setString(3, currentUser);
        preparedStatement.setTimestamp(4, sqlTimeStamp);
        preparedStatement.setString(5, currentUser);

        preparedStatement.executeUpdate();

        query = "SELECT LAST_INSERT_ID()";

        preparedStatement = conn.prepareStatement(query);

        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            countryId = resultSet.getInt(1);
        }
        return countryId;
    }

    public int insertCity(Customer customer, int countryId) throws Exception {

        int cityId = 0;

        Connection conn = dbConnection.getDBConnection();

        String query = "INSERT INTO city (city, countryId, createDate, createdBy, lastUpdate, lastUpdateBy) VALUES (?, ?, ?, ?, ?, ?);";

        preparedStatement = conn.prepareStatement(query);
        preparedStatement.setString(1, customer.getCity());
        preparedStatement.setInt(2, countryId);
        preparedStatement.setTimestamp(3, sqlTimeStamp);
        preparedStatement.setString(4, currentUser);
        preparedStatement.setTimestamp(5, sqlTimeStamp);
        preparedStatement.setString(6, currentUser);
        preparedStatement.executeUpdate();

        query = "SELECT LAST_INSERT_ID()";

        preparedStatement = conn.prepareStatement(query);

        ResultSet resultSet = preparedStatement.executeQuery();


        if (resultSet.next()) {
            cityId = resultSet.getInt(1);
        }


        return cityId;
    }

    public int insertAddress(Customer customer, int cityId) {

        int addressId = 0;
        try {


            Connection conn = dbConnection.getDBConnection();

            String query = "INSERT INTO address (address, address2, cityId, postalCode, phone, createDate, createdBy, lastUpdate, lastUpdateBy) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";

            preparedStatement = conn.prepareStatement(query);

            preparedStatement.setString(1, customer.getAddress());
            preparedStatement.setString(2, customer.getAddress2());
            preparedStatement.setInt(3, cityId);
            preparedStatement.setString(4, customer.getPostalCode());
            preparedStatement.setString(5, customer.getPhone());
            preparedStatement.setTimestamp(6, sqlTimeStamp);
            preparedStatement.setString(7, currentUser);
            preparedStatement.setTimestamp(8, sqlTimeStamp);
            preparedStatement.setString(9, currentUser);
            preparedStatement.executeUpdate();

            query = "SELECT LAST_INSERT_ID()";

            preparedStatement = conn.prepareStatement(query);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                addressId = resultSet.getInt(1);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return addressId;
    }

    public int insertCustomer(Customer customer, int addressId) throws Exception {
        int customerId = -1;


        Connection conn = dbConnection.getDBConnection();

        String query = "INSERT INTO customer (customerName, addressId, active, createDate, createdBy, lastUpdate, lastUpdateBy) VALUES (?, ?, ?, ?, ?, ?, ?);";
        preparedStatement = conn.prepareStatement(query);

        preparedStatement.setString(1, customer.getCustomerName());
        preparedStatement.setInt(2, addressId);
        preparedStatement.setInt(3, customer.getActive());
        preparedStatement.setTimestamp(4, sqlTimeStamp);
        preparedStatement.setString(5, currentUser);
        preparedStatement.setTimestamp(6, sqlTimeStamp);
        preparedStatement.setString(7, currentUser);
        preparedStatement.executeUpdate();

        query = "SELECT LAST_INSERT_ID()";

        preparedStatement = conn.prepareStatement(query);

        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            customerId = resultSet.getInt(1);
        }

        return customerId;
    }

    public boolean updateAddress(Customer updateCustomer, int addressId, int cityId) {
        boolean successful = false;
        try {

            Connection conn = dbConnection.getDBConnection();

            String query = "UPDATE address SET address = ?, address2 = ?, cityId = ?, postalCode = ?, phone = ?, lastUpdate = ?, lastUpdateBy = ? WHERE addressId = ?";
            preparedStatement = conn.prepareStatement(query);

            preparedStatement.setString(1, updateCustomer.getAddress());
            preparedStatement.setString(2, updateCustomer.getAddress2());
            preparedStatement.setInt(3, cityId);
            preparedStatement.setString(4, updateCustomer.getPostalCode());
            preparedStatement.setString(5, updateCustomer.getPhone());
            preparedStatement.setTimestamp(6, sqlTimeStamp);
            preparedStatement.setString(7, currentUser);
            preparedStatement.setInt(8, addressId);
            preparedStatement.executeUpdate();

            successful = true;
        } catch (Exception e) {
            System.out.println(e.getMessage());

        }
        return successful;
    }

    public void updateCustomer(Customer customer, Customer updateCustomer, int addressId) throws Exception {


        Connection conn = dbConnection.getDBConnection();

        String query = "UPDATE customer SET customerName = ?, addressId = ?, active = ?, lastUpdate = ?, lastUpdateBy = ? WHERE customerId = ?";

        preparedStatement = conn.prepareStatement(query);

        preparedStatement.setString(1, updateCustomer.getCustomerName());
        preparedStatement.setInt(2, addressId);
        preparedStatement.setInt(3, updateCustomer.getActive());
        preparedStatement.setTimestamp(4, sqlTimeStamp);
        preparedStatement.setString(5, currentUser);
        preparedStatement.setInt(6, customer.getCustomerId());
        preparedStatement.executeUpdate();

    }

    public int addressExists(Customer customer) throws Exception {
        int addressId = 0;


        Connection conn = dbConnection.getDBConnection();


        // Check if address exists before insert

        String query = "SELECT addressId FROM address WHERE address = ? AND COALESCE(address2, '') = ? AND postalCode = ?";

        preparedStatement = conn.prepareStatement(query);

        preparedStatement.setString(1, customer.getAddress());
        preparedStatement.setString(2, customer.getAddress2());
        preparedStatement.setString(3, customer.getPostalCode());


        resultSet = preparedStatement.executeQuery();


        if (resultSet.next()) {
            // if address exists get addressId and move on to customer record
            addressId = resultSet.getInt(1);

        } else {
            addressId = -1;
        }


        return addressId;
    }

    public boolean addressHasMutipleCustomers(int addressId) throws Exception {
        boolean answer = false;

        Connection conn = dbConnection.getDBConnection();

        // Check if current address is in use by more than one customer
        int numberOfCustomersWithAddress = 0;
        String query = "SELECT * FROM customer WHERE addressId = ?";


        preparedStatement = conn.prepareStatement(query);
        preparedStatement.setInt(1, addressId);

        resultSet = preparedStatement.executeQuery();

        query = "SELECT FOUND_ROWS()";
        preparedStatement = conn.prepareStatement(query);

        resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            numberOfCustomersWithAddress = resultSet.getInt(1);
        }

        if (numberOfCustomersWithAddress > 1) {
            answer = true;
        }
        return answer;
    }
}


