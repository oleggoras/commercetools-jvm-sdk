package io.sphere.sdk.discountcodes.commands.updateactions;

import io.sphere.sdk.commands.UpdateActionImpl;
import io.sphere.sdk.discountcodes.DiscountCode;

import javax.annotation.Nullable;

/**
 * Sets the maximum applicable number of a discount code.
 *
 * {@doc.gen intro}
 *
 * {@include.example io.sphere.sdk.discountcodes.commands.DiscountCodeUpdateCommandTest#setMaxApplications()}
 */
public class SetMaxApplications extends UpdateActionImpl<DiscountCode> {
    @Nullable
    private final Long maxApplications;

    private SetMaxApplications(final Long maxApplications) {
        super("setMaxApplications");
        this.maxApplications = maxApplications;
    }

    public static SetMaxApplications of(@Nullable final Long maxApplications) {
        return new SetMaxApplications(maxApplications);
    }

    @Nullable
    public Long getMaxApplications() {
        return maxApplications;
    }
}
