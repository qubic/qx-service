package org.qubic.qx.api.controller.service;

import org.qubic.qx.api.adapter.QxApiService;
import org.qubic.qx.api.controller.domain.AssetOrder;
import org.qubic.qx.api.controller.domain.EntityOrder;
import org.qubic.qx.api.controller.domain.Fees;

import java.util.*;
import java.util.stream.Collectors;

public class QxService {

    private final QxApiService integrationApi;

    public QxService(QxApiService integrationApiService) {
        this.integrationApi = integrationApiService;
    }

    public Fees getFees() {
        return integrationApi.getFees();
    }

    public List<AssetOrder> getAssetAskOrders(String issuer, String asset) {
        return integrationApi.getAssetAskOrders(issuer, asset);
    }

    public List<AssetOrder> getAssetBidOrders(String issuer, String asset) {
        return integrationApi.getAssetBidOrders(issuer, asset);
    }

    public List<EntityOrder> getEntityAskOrders(String identity) {
        return integrationApi.getEntityAskOrders(identity);
    }

    public List<EntityOrder> getEntityBidOrders(String identity) {
        return integrationApi.getEntityBidOrders(identity);
    }

    public List<AssetOrder> getAggregatedAssetAskOrders(String issuer, String asset) {
        List<AssetOrder> aggregated = new ArrayList<>();
        Map<Long, List<AssetOrder>> groupedByPrice = groupedByPrice(integrationApi.getAssetAskOrders(issuer, asset));
        for (Long price : sorted(groupedByPrice(integrationApi.getAssetAskOrders(issuer, asset)))) {
            collectEntriesWithSamePrice(price, groupedByPrice, aggregated);
        }
        return aggregated;
    }

    public List<AssetOrder> getAggregatedAssetBidOrders(String issuer, String asset) {
        List<AssetOrder> aggregated = new ArrayList<>();
        Map<Long, List<AssetOrder>> groupedByPrice = groupedByPrice(integrationApi.getAssetBidOrders(issuer, asset));
        for (Long price : sorted(groupedByPrice).reversed()) { // revers sort order for bids
            collectEntriesWithSamePrice(price, groupedByPrice, aggregated);
        }
        return aggregated;
    }

    Map<Long, List<AssetOrder>> groupedByPrice(List<AssetOrder> assetOrders) {
        return assetOrders.stream().collect(Collectors.groupingBy(AssetOrder::price));
    }

    private static NavigableSet<Long> sorted(Map<Long, List<AssetOrder>> groupedByPrice) {
        return new TreeSet<>(groupedByPrice.keySet());
    }

    private static void collectEntriesWithSamePrice(Long price, Map<Long, List<AssetOrder>> groupedByPrice, List<AssetOrder> aggregated) {
        long shares = groupedByPrice.get(price).stream().mapToLong(AssetOrder::numberOfShares).sum();
        aggregated.add(new AssetOrder(null, price, shares));
    }


}
