package io.sphere.sdk.discountcodes.queries;

import io.sphere.sdk.discountcodes.DiscountCode;
import io.sphere.sdk.queries.DefaultModelQueryModelImpl;
import io.sphere.sdk.queries.QueryModel;
import io.sphere.sdk.queries.QueryModelImpl;
import io.sphere.sdk.queries.StringQuerySortingModel;

import java.util.Optional;

public class DiscountCodeQueryModel extends DefaultModelQueryModelImpl<DiscountCode> {

    public static DiscountCodeQueryModel of() {
        return new DiscountCodeQueryModel(Optional.<QueryModelImpl<DiscountCode>>empty(), Optional.<String>empty());
    }

    private DiscountCodeQueryModel(Optional<? extends QueryModel<DiscountCode>> parent, Optional<String> pathSegment) {
        super(parent, pathSegment);
    }


    public StringQuerySortingModel<DiscountCode> code() {
        return new StringQuerySortingModel<>(Optional.of(this), "code");
    }
}
