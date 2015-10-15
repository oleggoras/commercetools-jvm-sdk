package io.sphere.sdk.types.commands.updateactions;

import io.sphere.sdk.commands.UpdateActionImpl;
import io.sphere.sdk.models.EnumValue;
import io.sphere.sdk.types.Type;

/**
 Adds an a new {@link EnumValue} value to an {@link io.sphere.sdk.types.EnumType} field.

 {@doc.gen intro}

 {@include.example io.sphere.sdk.types.commands.TypeUpdateCommandTest#addEnumValue()}
 */
public class AddEnumValue extends UpdateActionImpl<Type> {
    private final String fieldName;
    private final EnumValue value;

    public static AddEnumValue of(final String fieldName, final EnumValue value) {
        return new AddEnumValue(fieldName, value);
    }

    private AddEnumValue(final String fieldName, final EnumValue value) {
        super("addEnumValue");
        this.fieldName = fieldName;
        this.value = value;
    }

    public String getFieldName() {
        return fieldName;
    }

    public EnumValue getValue() {
        return value;
    }
}
