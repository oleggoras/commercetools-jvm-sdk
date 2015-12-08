package io.sphere.sdk.types.queries;

import io.sphere.sdk.queries.QueryModel;
import io.sphere.sdk.queries.ResourceQueryModelImpl;

public abstract class CustomResourceQueryModelImpl<T> extends ResourceQueryModelImpl<T> implements WithCustomQueryModel<T> {
    public CustomResourceQueryModelImpl(final QueryModel<T> parent, final String pathSegment) {
        super(parent, pathSegment);
    }

    @Override
    public CustomQueryModel<T> custom() {
        return new CustomQueryModelImpl<>(this, "custom");
    }
}
