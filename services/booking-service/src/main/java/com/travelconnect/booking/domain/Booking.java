package com.travelconnect.booking.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "bookings",
    indexes = {
        @Index(name = "idx_booking_traveler_id", columnList = "traveler_id"),
        @Index(name = "idx_booking_status", columnList = "status"),
        @Index(name = "idx_booking_reference", columnList = "booking_reference"),
        @Index(name = "idx_booking_trip_id", columnList = "trip_id")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(name = "traveler_id", nullable = false)
    private UUID travelerId;

    @Column(name = "booking_reference", unique = true, nullable = false, length = 20)
    private String bookingReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "total_amount", precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "GBP";

    @Column(name = "trace_id", length = 64)
    private String traceId;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<BookingItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    private void generateBookingReference() {
        if (bookingReference == null) {
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            StringBuilder sb = new StringBuilder("TC-");
            for (int i = 0; i < 8; i++) {
                sb.append(chars.charAt((int) (Math.random() * chars.length())));
            }
            this.bookingReference = sb.toString();
        }
    }
}
