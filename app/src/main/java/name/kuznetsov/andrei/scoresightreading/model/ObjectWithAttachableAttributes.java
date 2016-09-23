package name.kuznetsov.andrei.scoresightreading.model;

/**
 * Created by andrei on 9/23/16.
 */

public interface ObjectWithAttachableAttributes {

    <T> void attachAttribute(Class<T> clazz, T attr);

    void detachAttribute(Class clazz);

    <T> T obtainAttributeOrNull(Class<T> clazz);
}
