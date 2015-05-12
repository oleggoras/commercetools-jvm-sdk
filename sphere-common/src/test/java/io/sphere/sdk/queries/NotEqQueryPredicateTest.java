package io.sphere.sdk.queries;

import org.junit.Test;
import static io.sphere.sdk.queries.Fixtures.emptyQueryModel;
import static org.assertj.core.api.Assertions.assertThat;

public class NotEqQueryPredicateTest {
    @Test
    public void render() throws Exception {
        assertThat(NotEqQueryPredicate.of(emptyQueryModel, "xyz").render()).isEqualTo(" <> \"xyz\"");
    }
}