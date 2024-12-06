package com.rentalcar.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "Roles")
public class Role {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleId;

    @Column(nullable = false, unique = true, length = 50, columnDefinition = "NVARCHAR(50)")
    private String roleName;

    @Column(length = 255, columnDefinition = "NVARCHAR(255)")
    private String description;

    @ManyToMany(mappedBy = "roles")  // Đây là ánh xạ ngược lại từ bảng Account
    @JsonIgnore
    private List<Account> accounts;
}
