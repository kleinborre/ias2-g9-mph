
package test;

import org.junit.jupiter.api.*;
import ui.PageEmployeeData;
import util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.sql.Date;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EmployeeProfilePageTest {
//
//    private PageEmployeeData page;
//    
//    private final int expectedEmployeeID = 10012;
//    private final String expectedFirstName = "Josie";
//    private final String expectedLastName = "Lopez";
//    private final String expectedPosition = "Payroll Team Leader";
//    private final String expectedSupervisor = "Salcedo, Anthony";
//    private final String expectedBirthday = "1987-01-14";
//    private final String expectedPhone = "478-355-427";
//    private final String expectedAddress = "<html>49 Apt. 266 Springs<br>Poblacion<br>Taguig<br>Occidental Mindoro 3200</html>";
//    private final String expectedSSS = "44-8563448-3";
//    private final String expectedPagibig = "113071293354";
//    private final String expectedPhilhealth = "431709011012";
//    private final String expectedTIN = "218-489-737-000";
//    private final String expectedStatus = "Regular";
//
//
//    @BeforeAll
//    void setUpSession() {
//        // Set the session before constructing the page
//        SessionManager.setSession("U10012", 10012);
//    }
//
//    @BeforeEach
//    void setUp() {
//        page = Assertions.assertDoesNotThrow(() -> {
//            final PageEmployeeData[] p = new PageEmployeeData[1];
//            try {
//                EventQueue.invokeAndWait(() -> p[0] = new PageEmployeeData());
//            } catch (Exception e) {
//                fail("Failed to create PageEmployeeData", e);
//            }
//            return p[0];
//        });
//    }
//
//    @AfterEach
//    void tearDown() {
//        if (page != null) {
//            page.dispose();
//        }
//    }
//
//    @Test
//    void testEmployeeIDLabel() {
//        JLabel label = page.getEmployeeIDText();
//        assertEquals(String.valueOf(expectedEmployeeID), label.getText());
//    }
//
//    @Test
//    void testFirstNameLabel() {
//        JLabel label = page.getFirstNameText();
//        assertEquals(expectedFirstName, label.getText());
//    }
//
//    @Test
//    void testLastNameLabel() {
//        JLabel label = page.getLastNameText();
//        assertEquals(expectedLastName, label.getText());
//    }
//
//    @Test
//    void testPositionLabel() {
//        JLabel label = page.getPositionText();
//        assertEquals(expectedPosition, label.getText());
//    }
//
//    @Test
//    void testSupervisorLabel() {
//        JLabel label = page.getSupervisorText();
//        assertEquals(expectedSupervisor, label.getText());
//    }
//
//    @Test
//    void testBirthdayLabel() {
//        JLabel label = page.getBirthdayText();
//        assertTrue(label.getText().contains(expectedBirthday.substring(0, 4)));
//    }
//
//    @Test
//    void testPhoneLabel() {
//        JLabel label = page.getPhoneNumberText();
//        assertEquals(expectedPhone, label.getText());
//    }
//
//    @Test
//    void testAddressLabel() {
//        JLabel label = page.getAddressText();
//        assertEquals(expectedAddress, label.getText());
//    }
//
//    @Test
//    void testSSSLabel() {
//        JLabel label = page.getSSSNumberText();
//        assertEquals(expectedSSS, label.getText());
//    }
//
//    @Test
//    void testPagibigLabel() {
//        JLabel label = page.getPagibigNumberText();
//        assertEquals(expectedPagibig, label.getText());
//    }
//
//    @Test
//    void testPhilhealthLabel() {
//        JLabel label = page.getPhilhealthNumberText();
//        assertEquals(expectedPhilhealth, label.getText());
//    }
//
//    @Test
//    void testTINLabel() {
//        JLabel label = page.getTINNumberText();
//        assertEquals(expectedTIN, label.getText());
//    }
//
//    @Test
//    void testStatusLabel() {
//        JLabel label = page.getStatusText();
//        assertEquals(expectedStatus, label.getText());
//    }
//
//    @Test
//    void testEditProfileButtonAction() {
//        JButton button = getButtonByText(page, "Edit Profile");
//        assertNotNull(button);
//        assertTrue(button.isEnabled());
//        // Simulate click
//        Assertions.assertDoesNotThrow(() -> button.doClick());
//    }
//
//    @Test
//    void testModifyCredentialsButtonAction() {
//        JButton button = getButtonByText(page, "Modify Credentials");
//        assertNotNull(button);
//        assertTrue(button.isEnabled());
//        // Simulate click
//        Assertions.assertDoesNotThrow(() -> button.doClick());
//    }
//
//    @Test
//    void testBackButtonAction() {
//        JButton button = getButtonByText(page, "Back");
//        assertNotNull(button);
//        assertTrue(button.isEnabled());
//        // Simulate click
//        Assertions.assertDoesNotThrow(() -> button.doClick());
//    }
//
//    // Helper to get button by displayed text
//    private JButton getButtonByText(Container container, String text) {
//        for (Component c : container.getComponents()) {
//            if (c instanceof JButton) {
//                if (((JButton) c).getText().equals(text)) {
//                    return (JButton) c;
//                }
//            }
//            if (c instanceof Container) {
//                JButton b = getButtonByText((Container) c, text);
//                if (b != null) return b;
//            }
//        }
//        return null;
//    }
}
