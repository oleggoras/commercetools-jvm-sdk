package io.sphere.sdk.queries;

import java.util.List;
import java.util.Optional;

import static io.sphere.sdk.utils.IterableUtils.toStream;
import static io.sphere.sdk.utils.ListUtils.*;
import static java.util.stream.Collectors.toList;

public class StringQuerySortingModel<T> extends QueryModelImpl<T> implements QuerySortingModel<T>, StringQueryModel<T> {
    public StringQuerySortingModel(Optional<? extends QueryModel<T>> parent, String pathSegment) {
        super(parent, pathSegment);
    }

    @Override
    public QuerySort<T> sort(final QuerySortDirection sortDirection) {
        return new SphereQuerySort<>(this, sortDirection);
    }

    @Override
    public DirectionlessQuerySort<T> sort() {
        return new DirectionlessQuerySort<>(this);
    }

    /**
     * Escapes Strings like that (Scala notation) """query by name " test name"""
     * @param s the unescaped String
     * @return the escaped string
     */
    public static String escape(final String s) {
        return s.replace("\"", "\\\"");
    }

    public static String normalize(final String s) {
        return '"' + escape(s) + '"';
    }

    @Override
    public QueryPredicate<T> is(final String s) {
        return isPredicate(s);
    }

    @Override
    public QueryPredicate<T> isNot(final String s) {
        return isNotPredicate(s);
    }

    @Override
    public QueryPredicate<T> isIn(final String arg0, final String... args) {
        return isIn(listOf(arg0, args));
    }

    @Override
    public QueryPredicate<T> isIn(final Iterable<String> args) {
        return new IsInQueryPredicate<>(this, normalize(args));
    }

    @Override
    public QueryPredicate<T> isGreaterThan(final String s) {
        return ComparisonQueryPredicate.ofIsGreaterThan(this, normalize(s));
    }

    @Override
    public QueryPredicate<T> isLessThan(final String s) {
        return ComparisonQueryPredicate.ofIsLessThan(this, normalize(s));
    }

    @Override
    public QueryPredicate<T> isLessThanOrEqualTo(final String s) {
        return ComparisonQueryPredicate.ofIsLessThanOrEqualTo(this, normalize(s));
    }

    @Override
    public QueryPredicate<T> isGreaterThanOrEqualTo(final String s) {
        return ComparisonQueryPredicate.ofGreaterThanOrEqualTo(this, normalize(s));
    }

    @Override
    public QueryPredicate<T> isNotIn(final Iterable<String> args) {
        return new IsNotInQueryPredicate<>(this, normalize(args));
    }

    @Override
    public QueryPredicate<T> isNotIn(final String arg0, final String ... args) {
        return isNotIn(listOf(arg0, args));
    }

    @Override
    public QueryPredicate<T> isPresent() {
        return isPresentPredicate();
    }

    @Override
    public QueryPredicate<T> isNotPresent() {
        return isNotPresentPredicate();
    }

    private static List<String> normalize(final Iterable<String> args) {
        return toStream(args).map(x -> normalize(x)).collect(toList());
    }

    private static List<String> escape(final Iterable<String> args) {
        return toStream(args).map(x -> escape(x)).collect(toList());
    }
}