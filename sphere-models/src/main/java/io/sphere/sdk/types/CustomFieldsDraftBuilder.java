package io.sphere.sdk.types;

import com.fasterxml.jackson.databind.JsonNode;
import io.sphere.sdk.json.SphereJsonUtils;
import io.sphere.sdk.models.Base;
import io.sphere.sdk.models.Builder;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class CustomFieldsDraftBuilder extends Base implements Builder<CustomFieldsDraft> {
    @Nullable
    private final String typeId;
    @Nullable
    private final String typeKey;
    private final Map<String, JsonNode> fields = new HashMap<>();

    private CustomFieldsDraftBuilder(@Nullable final String typeId, @Nullable final String typeKey) {
        this.typeId = typeId;
        this.typeKey = typeKey;
    }

    public static CustomFieldsDraftBuilder ofTypeId(final String typeId) {
        requireNonNull(typeId);
        return new CustomFieldsDraftBuilder(typeId, null);
    }

    public static CustomFieldsDraftBuilder ofTypeKey(final String typeKey) {
        requireNonNull(typeKey);
        return new CustomFieldsDraftBuilder(null, typeKey);
    }

    public static CustomFieldsDraftBuilder ofType(final Type type) {
        return ofTypeId(type.getId());
    }

    public CustomFieldsDraftBuilder addObject(final String fieldName, final Object object) {
        final JsonNode jsonNode = SphereJsonUtils.toJsonNode(object);
        return addJsonField(fieldName, jsonNode);
    }

    private CustomFieldsDraftBuilder addJsonField(final String fieldName, final JsonNode jsonNode) {
        fields.put(fieldName, jsonNode);
        return this;
    }

    @Override
    public CustomFieldsDraft build() {
        return new CustomFieldsDraft(typeId, typeKey, fields);
    }
}
