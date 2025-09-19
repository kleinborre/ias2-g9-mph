package util;

/**
 * Reflection utility for test code. Use in all JUnit test classes to avoid repetition.
 */
public class FieldAccessTest {

    /**
     * Fetches any private/protected field by name, returns as correct type.
     * Throws on error (fast fail for bad field or type).
     */
    @SuppressWarnings("unchecked")
    public static <T> T getField(Object instance, String fieldName, Class<T> type) {
        try {
            java.lang.reflect.Field field = instance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(instance);
        } catch (Exception e) {
            throw new RuntimeException("Could not access field: " + fieldName, e);
        }
    }
}
