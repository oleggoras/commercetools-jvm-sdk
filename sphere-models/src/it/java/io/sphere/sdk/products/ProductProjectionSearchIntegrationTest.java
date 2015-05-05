package io.sphere.sdk.products;

import io.sphere.sdk.attributes.*;
import io.sphere.sdk.models.LocalizedStrings;
import io.sphere.sdk.products.commands.ProductCreateCommand;
import io.sphere.sdk.products.commands.ProductDeleteCommand;
import io.sphere.sdk.products.queries.ProductProjectionQuery;
import io.sphere.sdk.products.search.ProductProjectionSearch;
import io.sphere.sdk.producttypes.ProductTypeDraft;
import io.sphere.sdk.producttypes.ProductType;
import io.sphere.sdk.producttypes.commands.ProductTypeCreateCommand;
import io.sphere.sdk.producttypes.commands.ProductTypeDeleteCommand;
import io.sphere.sdk.producttypes.queries.ProductTypeQuery;
import io.sphere.sdk.queries.QueryDsl;
import io.sphere.sdk.search.*;
import io.sphere.sdk.test.IntegrationTest;
import io.sphere.sdk.test.RetryIntegrationTest;
import io.sphere.sdk.test.SphereTestUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static io.sphere.sdk.models.DefaultCurrencyUnits.EUR;
import static io.sphere.sdk.products.ProductProjectionType.STAGED;
import static io.sphere.sdk.products.search.VariantSearchSortDirection.*;
import static io.sphere.sdk.test.SphereTestUtils.ENGLISH;
import static io.sphere.sdk.test.SphereTestUtils.GERMAN;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.math.BigDecimal.*;
import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;

public class ProductProjectionSearchIntegrationTest extends IntegrationTest {
    private static final String EVIL_CHARACTER_WORD = "öóßàç";

    private static Product product1;
    private static Product product2;
    private static Product product3;
    private static ProductType productType;

    private static Product evilProduct1;
    private static Product evilProduct2;
    private static ProductType evilProductType;

    public static final String TEST_CLASS_NAME = ProductProjectionSearchIntegrationTest.class.getSimpleName();
    public static final String ATTR_NAME_COLOR = ("Color" + TEST_CLASS_NAME).substring(0, min(20, TEST_CLASS_NAME.length()));
    public static final String ATTR_NAME_SIZE = ("Size" + TEST_CLASS_NAME).substring(0, min(20, TEST_CLASS_NAME.length()));
    public static final String ATTR_NAME_EVIL = ("Evil" + TEST_CLASS_NAME).substring(0, min(20, TEST_CLASS_NAME.length()));

    @Rule
    public RetryIntegrationTest retry = new RetryIntegrationTest(60, 10000, LoggerFactory.getLogger(this.getClass()));

    @BeforeClass
    public static void setupProducts() {
        removeProducts();
        setupTestProducts();
        setupEvilTestProducts();
    }

    @AfterClass
    public static void removeProducts() {
        removeProductTypeAndItsProducts(productType);
        removeProductTypeAndItsProducts(evilProductType);
        product1 = null;
        product2 = null;
        product3 = null;
        evilProduct1 = null;
        evilProduct2 = null;
        productType = null;
        evilProductType = null;
    }

    @Test
    public void searchByTextInACertainLanguage() throws Exception {
        final SearchDsl<ProductProjection> search = ProductProjectionSearch.of(STAGED).withText(ENGLISH, "shoe");
        final PagedSearchResult<ProductProjection> result = executeSearch(search);
        assertThat(SphereTestUtils.toIds(result.getResults())).containsOnly(product1.getId());
    }

    @Test
    public void sortByAttributeAscending() throws Exception {
        final SearchSort<ProductProjection> sort = ProductProjectionSearch.model().allVariants().attribute().ofNumber(ATTR_NAME_SIZE).sort(ASC);
        final PagedSearchResult<ProductProjection> result = executeSearch(ProductProjectionSearch.of(STAGED).withSort(sort));
        assertThat(resultsToIds(result)).containsExactly(product2.getId(), product1.getId(), product3.getId());
    }

    @Test
    public void sortByAttributeDescending() throws Exception {
        final SearchSort<ProductProjection> sort = ProductProjectionSearch.model().allVariants().attribute().ofNumber(ATTR_NAME_SIZE).sort(DESC);
        final PagedSearchResult<ProductProjection> result = executeSearch(ProductProjectionSearch.of(STAGED).withSort(sort));
        assertThat(resultsToIds(result)).containsExactly(product1.getId(), product2.getId(), product3.getId());
    }

    @Test
    public void sortWithAdditionalParameterByAttributeAscending() throws Exception {
        final SearchSort<ProductProjection> sort = ProductProjectionSearch.model().allVariants().attribute().ofNumber(ATTR_NAME_SIZE).sort(ASC_MAX);
        final PagedSearchResult<ProductProjection> result = executeSearch(ProductProjectionSearch.of(STAGED).withSort(sort));
        assertThat(resultsToIds(result)).containsExactly(product3.getId(), product2.getId(), product1.getId());
    }

    @Test
    public void sortWithAdditionalParameterByAttributeDescending() throws Exception {
        final SearchSort<ProductProjection> sort = ProductProjectionSearch.model().allVariants().attribute().ofNumber(ATTR_NAME_SIZE).sort(DESC_MIN);
        final PagedSearchResult<ProductProjection> result = executeSearch(ProductProjectionSearch.of(STAGED).withSort(sort));
        assertThat(resultsToIds(result)).containsExactly(product3.getId(), product1.getId(), product2.getId());
    }

    @Test
    public void sortWithSimpleExpression() {
        final SearchSort<ProductProjection> sort = SearchSort.of("variants.attributes." + ATTR_NAME_SIZE + " asc.max");
        final PagedSearchResult<ProductProjection> result = executeSearch(ProductProjectionSearch.of(STAGED).withSort(sort));
        assertThat(resultsToIds(result)).containsExactly(product3.getId(), product2.getId(), product1.getId());
    }

    @Test
    public void responseContainsRangeFacetsForAttributes() throws Exception {
        final RangeFacetExpression<ProductProjection, BigDecimal> facetExpression = ProductProjectionSearch.model().allVariants().price().amount().facetOf().greaterThanOrEqualTo(ZERO);
        final PagedSearchResult<ProductProjection> result = executeSearch(ProductProjectionSearch.of(STAGED).plusFacet(facetExpression));
        assertThat(result.getRangeFacetResult(facetExpression).get().getRanges().get(0).getCount()).isGreaterThan(0);
    }

    @Test
    public void responseContainsTermFacetsForAttributes() throws Exception {
        final TermFacetExpression<ProductProjection, String> facet = ProductProjectionSearch.model().allVariants().attribute().ofText(ATTR_NAME_COLOR).facetOf().all();
        final PagedSearchResult<ProductProjection> result = executeSearch(ProductProjectionSearch.of(STAGED).plusFacet(facet));
        final Predicate<TermStats<String>> isTermBlue = termStat -> termStat.getTerm().equals("blue") && termStat.getCount() > 0;
        assertThat(result.getTermFacetResult(facet).get().getTerms().stream().anyMatch(isTermBlue)).isTrue();
    }

    @Test
    public void resultsAndFacetsAreFilteredByColor() throws Exception {
        final TermFacetExpression<ProductProjection, String> facet = ProductProjectionSearch.model().allVariants().attribute().ofText(ATTR_NAME_COLOR).facetOf().all();
        final FilterExpression<ProductProjection> filter = ProductProjectionSearch.model().allVariants().attribute().ofNumber(ATTR_NAME_SIZE).filterBy().exactly(valueOf(36));
        final PagedSearchResult<ProductProjection> result = executeSearch(ProductProjectionSearch.of(STAGED)
                .plusFacet(facet)
                .plusFilterResults(filter)
                .plusFilterFacets(filter));
        assertThat(resultsToIds(result)).containsOnly(product2.getId());
        assertThat(result.getTermFacetResult(facet).get().getTerms()).containsExactly(TermStats.of("red", 1));
    }

    @Test
    public void onlyResultsAreFilteredByColor() throws Exception {
        final TermFacetExpression<ProductProjection, String> facet = ProductProjectionSearch.model().allVariants().attribute().ofText(ATTR_NAME_COLOR).facetOf().all();
        final FilterExpression<ProductProjection> filter = ProductProjectionSearch.model().allVariants().attribute().ofNumber(ATTR_NAME_SIZE).filterBy().exactly(valueOf(36));
        final PagedSearchResult<ProductProjection> result = executeSearch(ProductProjectionSearch.of(STAGED)
                .plusFacet(facet)
                .plusFilterResults(filter));
        assertThat(resultsToIds(result)).containsOnly(product2.getId());
        assertThat(result.getTermFacetResult(facet).get().getTerms()).containsExactly(TermStats.of("blue", 2), TermStats.of("red", 1));
    }

    @Test
    public void onlyFacetsAreFilteredByColor() throws Exception {
        final TermFacetExpression<ProductProjection, String> facet = ProductProjectionSearch.model().allVariants().attribute().ofText(ATTR_NAME_COLOR).facetOf().all();
        final FilterExpression<ProductProjection> filter = ProductProjectionSearch.model().allVariants().attribute().ofNumber(ATTR_NAME_SIZE).filterBy().exactly(valueOf(36));
        final PagedSearchResult<ProductProjection> result = executeSearch(ProductProjectionSearch.of(STAGED)
                .plusFacet(facet)
                .plusFilterFacets(filter));
        assertThat(result.getTermFacetResult(facet).get().getTerms()).containsExactly(TermStats.of("red", 1));
        assertThat(resultsToIds(result)).contains(product1.getId(), product2.getId(), product3.getId());
    }

    @Test
    public void filtersByTerm() throws Exception {
        final FilterExpression<ProductProjection> filter = ProductProjectionSearch.model().allVariants().attribute().ofText(ATTR_NAME_COLOR).filterBy().exactly("red");
        final PagedSearchResult<ProductProjection> result = executeSearch(ProductProjectionSearch.of(STAGED).plusFilterQuery(filter));
        assertThat(resultsToIds(result)).containsOnly(product2.getId());
    }

    @Test
    public void filtersByRange() throws Exception {
        final FilterExpression<ProductProjection> filter = ProductProjectionSearch.model().allVariants().attribute().ofNumber(ATTR_NAME_SIZE).filterBy().greaterThanOrEqualTo(valueOf(44));
        final PagedSearchResult<ProductProjection> result = executeSearch(ProductProjectionSearch.of(STAGED).plusFilterQuery(filter));
        assertThat(resultsToIds(result)).containsOnly(product1.getId(), product2.getId());
    }

    @Test
    public void simpleFilterByRange() throws Exception {
        final FilterExpression<ProductProjection> filter = FilterExpression.of("variants.attributes." + ATTR_NAME_SIZE + ":range(44 to *)");
        final PagedSearchResult<ProductProjection> result = executeSearch(ProductProjectionSearch.of(STAGED).plusFilterQuery(filter));
        assertThat(resultsToIds(result)).containsOnly(product1.getId(), product2.getId());
    }

    @Test
    public void resultsArePaginated() throws Exception {
        final FilterExpression<ProductProjection> filter = ProductProjectionSearch.model().allVariants().attribute().ofText(ATTR_NAME_COLOR).filterBy().exactly(asList("blue", "red"));
        final SearchSort<ProductProjection> sort = ProductProjectionSearch.model().name().locale(ENGLISH).sort(SimpleSearchSortDirection.DESC);
        final PagedSearchResult<ProductProjection> result = executeSearch(ProductProjectionSearch.of(STAGED)
                .plusFilterQuery(filter)
                .withSort(sort)
                .withOffset(1)
                .withLimit(1));
        assertThat(resultsToIds(result)).containsOnly(product2.getId());
    }

    @Test
    public void filterQueryFiltersBeforeFacetsAreCalculated() throws Exception {
        final TermFacetExpression<ProductProjection, String> facet = ProductProjectionSearch.model().allVariants().attribute().ofText(ATTR_NAME_COLOR).facetOf().all();
        final FilterExpression<ProductProjection> filter = ProductProjectionSearch.model().allVariants().attribute().ofNumber(ATTR_NAME_SIZE).filterBy().exactly(valueOf(36));
        final PagedSearchResult<ProductProjection> result = executeSearch(ProductProjectionSearch.of(STAGED)
                .plusFacet(facet)
                .plusFilterQuery(filter));
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.getResults().get(0).getId()).isEqualTo(product2.getId());
        assertThat(result.getTermFacetResult(facet).get().getTerms()).containsExactly(TermStats.of("red", 1));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void simpleFacetsAreParsed() throws Exception {
        final String path = "variants.attributes." + ATTR_NAME_COLOR;
        final FacetExpression<ProductProjection> facet = FacetExpression.of(path);
        final PagedSearchResult<ProductProjection> result = executeSearch(ProductProjectionSearch.of(STAGED).plusFacet(facet));
        final TermFacetResult<String> termFacetResult = (TermFacetResult) result.getFacetResult(path).get();
        assertThat(termFacetResult.getMissing()).isGreaterThanOrEqualTo(3);
        assertThat(termFacetResult.getTotal()).isEqualTo(3);
        assertThat(termFacetResult.getOther()).isEqualTo(0);
        assertThat(termFacetResult.getTerms()).containsExactly(TermStats.of("blue", 2), TermStats.of("red", 1));
    }

    @Test
    public void termFacetsAreParsed() throws Exception {
        final TermFacetExpression<ProductProjection, String> facet = ProductProjectionSearch.model().allVariants().attribute().ofText(ATTR_NAME_COLOR).facetOf().all();
        final PagedSearchResult<ProductProjection> result = executeSearch(ProductProjectionSearch.of(STAGED).plusFacet(facet));
        final TermFacetResult<String> termFacetResult = result.getTermFacetResult(facet).get();
        assertThat(termFacetResult.getMissing()).isGreaterThanOrEqualTo(3);
        assertThat(termFacetResult.getTotal()).isEqualTo(3);
        assertThat(termFacetResult.getOther()).isEqualTo(0);
        assertThat(termFacetResult.getTerms()).containsExactly(TermStats.of("blue", 2), TermStats.of("red", 1));
    }

    @Test
    public void rangeFacetsAreParsed() throws Exception {
        final RangeFacetExpression<ProductProjection, BigDecimal> facet = ProductProjectionSearch.model().allVariants().attribute().ofNumber(ATTR_NAME_SIZE).facetOf().greaterThanOrEqualTo(ZERO);
        final PagedSearchResult<ProductProjection> result = executeSearch(ProductProjectionSearch.of(STAGED).plusFacet(facet));
        final RangeStats<Double> expectedRange = RangeStats.of(Optional.of(0D), Optional.empty(), 6, 36D, 46D, 246D, 41D);
        assertThat(result.getRangeFacetResult(facet).get().getRanges()).containsExactly(expectedRange);
    }

    @Test
    public void filteredFacetsAreParsed() throws Exception {
        final FilteredFacetExpression<ProductProjection, String> facet = ProductProjectionSearch.model().allVariants().attribute().ofText(ATTR_NAME_COLOR).facetOf().only("blue");
        final PagedSearchResult<ProductProjection> result = executeSearch(ProductProjectionSearch.of(STAGED).plusFacet(facet));
        assertThat(result.getFilteredFacetResult(facet).get().getCount()).isEqualTo(2);
    }

    @Test
    public void termFacetsSupportsAlias() throws Exception {
        final String alias = "my-facet";
        final TermFacetExpression<ProductProjection, String> facet = ProductProjectionSearch.model().allVariants().attribute().ofText(ATTR_NAME_COLOR).facetOf().withAlias(alias).all();
        final PagedSearchResult<ProductProjection> result = executeSearch(ProductProjectionSearch.of(STAGED).plusFacet(facet));
        final TermFacetResult<String> termFacetResult = result.getTermFacetResult(facet).get();
        assertThat(facet.resultPath()).isEqualTo(alias);
        assertThat(termFacetResult.getTerms()).containsExactly(TermStats.of("blue", 2), TermStats.of("red", 1));
    }

    @Test
    public void rangeFacetsSupportsAlias() throws Exception {
        final String alias = "my-facet";
        final RangeFacetExpression<ProductProjection, BigDecimal> facet = ProductProjectionSearch.model().allVariants().attribute().ofNumber(ATTR_NAME_SIZE).facetOf().withAlias(alias).greaterThanOrEqualTo(ZERO);
        final PagedSearchResult<ProductProjection> result = executeSearch(ProductProjectionSearch.of(STAGED).plusFacet(facet));
        final RangeStats<Double> expectedRange = RangeStats.of(Optional.of(0D), Optional.empty(), 6, 36D, 46D, 246D, 41D);
        assertThat(facet.resultPath()).isEqualTo(alias);
        assertThat(result.getRangeFacetResult(facet).get().getRanges()).containsExactly(expectedRange);
    }

    @Test
    public void filteredFacetsSupportsAlias() throws Exception {
        final String alias = "my-facet";
        final FilteredFacetExpression<ProductProjection, String> facet = ProductProjectionSearch.model().allVariants().attribute().ofText(ATTR_NAME_COLOR).facetOf().withAlias(alias).only("blue");
        final PagedSearchResult<ProductProjection> result = executeSearch(ProductProjectionSearch.of(STAGED).plusFacet(facet));
        assertThat(facet.resultPath()).isEqualTo(alias);
        assertThat(result.getFilteredFacetResult(facet).get().getCount()).isEqualTo(2);
    }

    @Test
    public void paginationExample() {
        final PagedSearchResult<ProductProjection> result = executeSearch(ProductProjectionSearch.of(STAGED).withOffset(50).withLimit(25));
        assertThat(result.getOffset()).isEqualTo(50);
        assertThat(result.getResults().size()).isEqualTo(min(25, max(result.getTotal() - 50, 0)));
    }

    @Test
    public void unicodeExampleForFullTextSearch() throws Exception {
        final SearchDsl<ProductProjection> searchDsl = ProductProjectionSearch.of(STAGED).withText(GERMAN, EVIL_CHARACTER_WORD);
        final PagedSearchResult<ProductProjection> result = executeEvilSearch(searchDsl);
        assertThat(result.getTotal()).isEqualTo(2);
    }

    @Test
    public void unicodeExampleForFilter() throws Exception {
        final FilterExpression<ProductProjection> filter = ProductProjectionSearch.model().allVariants().attribute().ofText(ATTR_NAME_EVIL).filterBy().exactly(EVIL_CHARACTER_WORD);
        final SearchDsl<ProductProjection> searchDsl = ProductProjectionSearch.of(STAGED).plusFilterQuery(filter);
        final PagedSearchResult<ProductProjection> result = executeEvilSearch(searchDsl);
        assertThat(result.getTotal()).isEqualTo(1);
    }

    private static List<String> resultsToIds(final PagedSearchResult<ProductProjection> result) {
        return SphereTestUtils.toIds(result.getResults());
    }

    private static PagedSearchResult<ProductProjection> executeSearch(final SearchDsl<ProductProjection> searchDsl) {
        final FilterExpression<ProductProjection> onlyCreatedProducts = FilterExpression.of(
                String.format("id:\"%s\",\"%s\",\"%s\"", product1.getId(), product2.getId(), product3.getId()));
        return execute(searchDsl.plusFilterQuery(onlyCreatedProducts));
    }

    private static PagedSearchResult<ProductProjection> executeEvilSearch(final SearchDsl<ProductProjection> searchDsl) {
        final FilterExpression<ProductProjection> onlyCreatedProducts = FilterExpression.of(
                String.format("id:\"%s\",\"%s\"", evilProduct1.getId(), evilProduct2.getId()));
        return execute(searchDsl.plusFilterQuery(onlyCreatedProducts));
    }

    private static void setupTestProducts() {
        final AttributeDefinition colorAttrDef = AttributeDefinitionBuilder
                .of(ATTR_NAME_COLOR, LocalizedStrings.ofEnglishLocale(ATTR_NAME_COLOR), TextType.of()).isSearchable(true).build();
        final AttributeDefinition sizeAttrDef = AttributeDefinitionBuilder
                .of(ATTR_NAME_SIZE, LocalizedStrings.ofEnglishLocale(ATTR_NAME_SIZE), NumberType.of()).isSearchable(true).build();
        final ProductTypeDraft productTypeDraft = ProductTypeDraft.of(TEST_CLASS_NAME, "", asList(colorAttrDef, sizeAttrDef));
        final ProductTypeCreateCommand productTypeCreateCommand = ProductTypeCreateCommand.of(productTypeDraft);
        productType = execute(productTypeCreateCommand);
        product1 = createTestProduct(productType, "Schuh", "shoe", "blue", 38, 46);
        product2 = createTestProduct(productType, "Hemd", "shirt", "red", 36, 44);
        product3 = createTestProduct(productType, "Kleider", "dress", "blue", 40, 42);

    }

    private static void setupEvilTestProducts() {
        final AttributeDefinition evilAttrDef = AttributeDefinitionBuilder
                .of(ATTR_NAME_EVIL, LocalizedStrings.ofEnglishLocale(ATTR_NAME_EVIL), TextType.of()).isSearchable(true).build();
        final ProductTypeDraft evilProductTypeDraft = ProductTypeDraft.of("Evil" + TEST_CLASS_NAME, "", asList(evilAttrDef));
        final ProductTypeCreateCommand evilProductTypeCreateCommand = ProductTypeCreateCommand.of(evilProductTypeDraft);
        evilProductType = execute(evilProductTypeCreateCommand);
        evilProduct1 = createEvilTestProduct(evilProductType, EVIL_CHARACTER_WORD, "foo");
        evilProduct2 = createEvilTestProduct(evilProductType, "bar", EVIL_CHARACTER_WORD);

    }

    private static Product createTestProduct(final ProductType productType, final String germanName, final String englishName,
                                             final String color, final int size1, final int size2) {
        final LocalizedStrings name = LocalizedStrings.of(GERMAN, germanName, ENGLISH, englishName);
        final ProductVariantDraft masterVariant = ProductVariantDraftBuilder.of()
                .attributes(Attribute.of(ATTR_NAME_SIZE, size1), Attribute.of(ATTR_NAME_COLOR, color))
                .price(Price.of(new BigDecimal("23.45"), EUR)).build();
        final ProductVariantDraft variant = ProductVariantDraftBuilder.of()
                .attributes(Attribute.of(ATTR_NAME_SIZE, size2))
                .price(Price.of(new BigDecimal("27.45"), EUR)).build();
        final ProductDraft productDraft = ProductDraftBuilder.of(productType, name, name.slugified(), masterVariant)
                .variants(asList(variant)).build();
        return execute(ProductCreateCommand.of(productDraft));
    }

    private static Product createEvilTestProduct(final ProductType productType, final String germanName, final String evilValue) {
        final LocalizedStrings name = LocalizedStrings.of(GERMAN, germanName);
        final ProductVariantDraft masterVariant = ProductVariantDraftBuilder.of()
                .attributes(Attribute.of(ATTR_NAME_EVIL, evilValue)).build();
        final ProductDraft productDraft = ProductDraftBuilder.of(productType, name, name.slugified(), masterVariant).build();
        return execute(ProductCreateCommand.of(productDraft));
    }

    private static void removeProductTypeAndItsProducts(final ProductType productType) {
        if (productType != null) {
            final QueryDsl<ProductType> request = ProductTypeQuery.of().byName(productType.getName());
            List<ProductType> productTypes = execute(request).getResults();
            if (!productTypes.isEmpty()) {
                final List<ProductProjection> products = execute(ProductProjectionQuery.of(STAGED)
                        .withPredicate(ProductProjectionQuery.model().productType().isAnyOf(productTypes))).getResults();
                products.forEach(p -> execute(ProductDeleteCommand.of(p.toProductVersioned())));
                productTypes.forEach(p -> execute(ProductTypeDeleteCommand.of(p)));
            }
        }
    }
}
