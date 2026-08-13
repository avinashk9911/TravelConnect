package com.travelconnect.booking.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
    name = "booking_items",
    indexes = {
        @Index(name = "idx_booking_item_booking_id", columnList = "booking_id")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 20)
    private BookingItemType itemType;

    @Column(name = "supplier_code", length = 50)
    private String supplierCode;

    @Column(name = "origin", length = 10)
    private String origin;

    @Column(name = "destination", length = 10)
    private String destination;

    @Column(name = "departure_date")
    private LocalDate departureDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "passengers")
    @Builder.Default
    private Integer passengers = 1;

    @Column(name = "price_per_unit", precision = 19, scale = 4)
    private BigDecimal pricePerUnit;

    @Column(name = "quantity")
    @Builder.Default
    private Integer quantity = 1;

    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "GBP";
}
