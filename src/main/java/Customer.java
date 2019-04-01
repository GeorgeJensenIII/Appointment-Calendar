import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Created by gwjense on 8/5/17.
 */
public class Customer {
        // Customer Table
        SimpleIntegerProperty customerId = new SimpleIntegerProperty();
        SimpleStringProperty customerName = new SimpleStringProperty();
        SimpleIntegerProperty active = new SimpleIntegerProperty();

        // Address Table
        SimpleStringProperty address = new SimpleStringProperty();
        SimpleStringProperty address2 = new SimpleStringProperty();
        SimpleStringProperty postalCode = new SimpleStringProperty();
        SimpleStringProperty phone = new SimpleStringProperty();


        // City Table
        SimpleStringProperty city = new SimpleStringProperty();

        // Country Table
        SimpleStringProperty country = new SimpleStringProperty();

        Customer (int customerId, String customerName, int active, String address, String address2, String postalCode, String phone, String city, String country)
        {
            this.setCustomerId(customerId);
            this.setCustomerName(customerName);
            this.setActive(active);
            this.setAddress(address);
            this.setAddress2(address2);
            this.setPostalCode(postalCode);
            this.setPhone(phone);
            this.setCity(city);
            this.setCountry(country);
        }

    public String getCustomerName() {
        return customerName.get();
    }

    public SimpleStringProperty customerNameProperty() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName.set(customerName);
    }

    public int getCustomerId() {
        return customerId.get();
    }

    public SimpleIntegerProperty customerIdProperty() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId.set(customerId);
    }

    public String getAddress() {
        return address.get();
    }

    public SimpleStringProperty addressProperty() {
        return address;
    }

    public void setAddress(String address) {
        this.address.set(address);
    }

    public String getAddress2() {
        return address2.get();
    }

    public SimpleStringProperty address2Property() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2.set(address2);
    }

    public String getPostalCode() {
        return postalCode.get();
    }

    public SimpleStringProperty postalCodeProperty() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode.set(postalCode);
    }

    public String getPhone() {
        return phone.get();
    }

    public SimpleStringProperty phoneProperty() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone.set(phone);
    }

    public String getCity() {
        return city.get();
    }

    public SimpleStringProperty cityProperty() {
        return city;
    }

    public void setCity(String city) {
        this.city.set(city);
    }

    public String getCountry() {
        return country.get();
    }

    public SimpleStringProperty countryProperty() {
        return country;
    }

    public void setCountry(String country) {
        this.country.set(country);
    }

    public int getActive() {
        return active.get();
    }

    public SimpleIntegerProperty activeProperty() {
        return active;
    }

    public void setActive(int active) {
        this.active.set(active);
    }
}

