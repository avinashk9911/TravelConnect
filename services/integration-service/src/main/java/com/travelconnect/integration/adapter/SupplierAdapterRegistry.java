package com.travelconnect.integration.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Registry of all available {@link SupplierAdapter} beans.
 *
 * Spring automatically injects every SupplierAdapter implementation into the
 * constructor list.  The registry builds an index keyed by supplier type so
 * lookups are O(1).
 *
 * To add a new supplier: implement SupplierAdapter, annotate with @Component.
 * No changes needed here.
 */
@Component
@Slf4j
public class SupplierAdapterRegistry {

    private final Map<String, SupplierAdapter> adapters;

    public SupplierAdapterRegistry(List<SupplierAdapter> adapterList) {
        this.adapters = adapterList.stream()
                .collect(Collectors.toMap(
                        a -> a.getSupplierType().toUpperCase(),
                        a -> a));
        log.info("Registered {} supplier adapters: {}", adapters.size(), adapters.keySet());
    }

    /**
     * Returns the adapter for the given supplier type.
     *
     * @param supplierType FLIGHT, HOTEL, or CAR (case-insensitive)
     * @throws IllegalArgumentException if no adapter is registered for the type
     */
    public SupplierAdapter getAdapter(String supplierType) {
        SupplierAdapter adapter = adapters.get(supplierType.toUpperCase());
        if (adapter == null) {
            throw new IllegalArgumentException("No adapter registered for supplier type: " + supplierType);
        }
        return adapter;
    }

    /** Returns all registered adapters. */
    public List<SupplierAdapter> getAllAdapters() {
        return new ArrayList<>(adapters.values());
    }
}
