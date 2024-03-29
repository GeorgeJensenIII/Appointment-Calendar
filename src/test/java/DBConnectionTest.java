import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Created by gwjense on 3/8/19.
 */
public class DBConnectionTest {


        @InjectMocks
        private DBConnection dbConnection;
        @Mock
        private Connection mockConnection;
        @Mock
        private PreparedStatement mockStatement;


        @Before
        public void setUp() {
            MockitoAnnotations.initMocks(this);
        }

        @Test
        public void testMockDBConnection() throws Exception {
            Mockito.when(mockConnection.createStatement()).thenReturn(mockStatement);
            Mockito.when(mockConnection.createStatement().executeUpdate(Mockito.any())).thenReturn(1);
            int value = dbConnection.executeQuery("");
            Assert.assertEquals(value, 1);
            Mockito.verify(mockConnection.createStatement(), Mockito.times(1));
        }


}
