package org.qubic.qx.sync.domain;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class AssetOrderTest {

    @Test
    void maxPrice() {
        AssetOrder order = Stream.of(
                new AssetOrder("x", 1, 42),
                new AssetOrder("y", 2, 42),
                new AssetOrder("z", 3, 42)
        ).max(Comparator.comparing(AssetOrder::price)).orElseThrow();
        assertThat(order.price()).isEqualTo(3);
    }

    @Test
    void sortByPrice() {
        List<AssetOrder> orders = Stream.of(
                        new AssetOrder("x", 1, 42),
                        new AssetOrder("y", 2, 42),
                        new AssetOrder("z", 3, 42)
                ).sorted(Comparator
                        .comparing(AssetOrder::price)
                        .reversed())
                .toList();

        assertThat(orders.getFirst().price()).isEqualTo(3);
        assertThat(orders.getLast().price()).isEqualTo(1);
    }

}