package org.qubic.qx.sync.domain;

import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.api.domain.AssetOrder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderBookTest {

    @Test
    void equals() {
        OrderBook ob1 = new OrderBook(1, "issuer", "asset", List.of(new AssetOrder("entity", 2, 3)), List.of(new AssetOrder("other", 4, 5)));
        OrderBook ob2 = new OrderBook(1, "issuer", "asset", List.of(new AssetOrder("entity", 2, 3)), List.of(new AssetOrder("other", 4, 5)));
        assertThat(ob1).isEqualTo(ob2);

        OrderBook ob3 = new OrderBook(1, "issuer", "asset", List.of(), null);
        OrderBook ob4 = new OrderBook(1, "issuer", "asset", List.of(), null);
        assertThat(ob3).isEqualTo(ob4);
    }

    @Test
    void notEquals() {
        OrderBook ob1 = new OrderBook(1, "issuer", "asset", List.of(new AssetOrder("entity", 2, 3)), List.of(new AssetOrder("other", 4, 5)));
        OrderBook ob2 = new OrderBook(1, "issuer", "asset", List.of(new AssetOrder("other", 4, 5)), List.of(new AssetOrder("entity", 2, 3)));
        assertThat(ob1).isNotEqualTo(ob2);

        OrderBook ob3 = new OrderBook(1, "issuer", "asset", List.of(new AssetOrder("entity", 3, 3)), List.of(new AssetOrder("other", 4, 5)));
        assertThat(ob1).isNotEqualTo(ob3);
    }

}