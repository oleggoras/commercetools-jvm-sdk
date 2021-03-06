package io.sphere.sdk.carts;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.sphere.sdk.models.Base;

class CartsConfigurationImpl extends Base implements CartsConfiguration {
    
    private Integer deleteDaysAfterLastModification;

    @JsonCreator
    public CartsConfigurationImpl(Integer deleteDaysAfterLastModification) {
        this.deleteDaysAfterLastModification = deleteDaysAfterLastModification;
    }

    @Override
    public Integer getDeleteDaysAfterLastModification() {
        return this.deleteDaysAfterLastModification;
    }
    
    static CartsConfiguration of(final Integer deleteDaysAfterLastModification) {
        return new CartsConfigurationImpl(deleteDaysAfterLastModification);
    }
}
