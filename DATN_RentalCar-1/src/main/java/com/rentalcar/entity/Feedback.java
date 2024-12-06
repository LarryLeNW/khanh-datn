package com.rentalcar.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "Feedbacks")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long feedbackId;

    @ManyToOne
    @JoinColumn(name = "rentalId", nullable = false)
    private Rental rental;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 255, columnDefinition = "NVARCHAR(255)")
    private String comment;

    @Column(nullable = false)
    private LocalDate feedbackDate;

    // Getters and Setters
}
