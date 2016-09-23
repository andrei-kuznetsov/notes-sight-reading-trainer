package name.kuznetsov.andrei.scoresightreading.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by andrei on 9/23/16.
 */

public class ObjectWithAttachableAttributesImpl implements ObjectWithAttachableAttributes {
    private Map<Class, Object> attributes = new HashMap<>();

    @Override
    public <T> void attachAttribute(Class<T> clazz, T attr) {
        attributes.put(clazz, attr);
    }

    @Override
    public void detachAttribute(Class clazz) {
        attributes.remove(clazz);
    }

    @Override
    public <T> T obtainAttributeOrNull(Class<T> clazz) {
        return (T) attributes.get(clazz);
    }
}
