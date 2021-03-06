package io.sphere.sdk.shippingmethods;

import io.sphere.sdk.cartdiscounts.CartPredicate;
import io.sphere.sdk.models.Reference;
import io.sphere.sdk.models.ResourceIdentifier;
import io.sphere.sdk.taxcategories.TaxCategory;

import javax.annotation.Nullable;
import java.util.List;

public final class ShippingMethodDraftBuilder extends ShippingMethodDraftBuilderBase<ShippingMethodDraftBuilder> {
    ShippingMethodDraftBuilder(Boolean _default, @Nullable String description, @Nullable String key,
                               String name, @Nullable String predicate, ResourceIdentifier<TaxCategory> taxCategory, List<ZoneRateDraft> zoneRates) {
        super(_default, description, key, name, predicate, taxCategory, zoneRates);
    }

    public ShippingMethodDraftBuilder predicate(final CartPredicate predicate) {
        return super.predicate(predicate.toSphereCartPredicate());
    }
}
