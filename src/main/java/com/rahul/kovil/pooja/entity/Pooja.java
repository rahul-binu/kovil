package com.rahul.kovil.pooja.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.rahul.kovil.common.entity.BaseEntity;
import com.rahul.kovil.common.enums.BaseStatus;
import com.rahul.kovil.common.enums.BookingStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "poojas")
public class Pooja extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Devotee who booked the pooja
    @Column(name = "devotee_id", nullable = false)
    private String devotee;

    // User who created the booking
    @Column(name = "user_id", nullable = false)
    private String user;

    @Column(length = 60)
    private String transId;

    private Boolean booking = false;

    private LocalDate bookingDate = null;

    private LocalDate bookingCloseDate = null;

    private BigDecimal advanceAmount;

    @Enumerated(EnumType.STRING)
    private BookingStatus bookingStatus = BookingStatus.NONE;

    private LocalDateTime date;

    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private BaseStatus status = BaseStatus.ACTIVE;
}
