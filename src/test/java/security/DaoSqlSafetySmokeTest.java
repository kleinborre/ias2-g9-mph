package security;

import daoimpl.EmployeeDAOImpl;
import org.junit.jupiter.api.Test;
import pojo.Employee;

import static org.junit.jupiter.api.Assertions.*;

public class DaoSqlSafetySmokeTest {

    @Test
    void getEmployeeByUserId_withCraftedString_doesNotEscalate() {
        // classic injection-like payload
        String payload = "x' OR '1'='1";
        EmployeeDAOImpl dao = new EmployeeDAOImpl();
        try {
            Employee e = dao.getEmployeeByUserID(payload);
            assertNull(e, "PreparedStatement must prevent injection; expect null/no match");
        } catch (Exception ex) {
            fail("DAO must not throw on crafted input. Exception: " + ex.getMessage());
        }
    }
}