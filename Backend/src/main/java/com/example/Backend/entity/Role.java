package com.example.Backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name; // CLIENT, SUPPORT, ADMIN, SUPER_ADMIN

    // Role name constants
    public static final String CLIENT = "CLIENT";
    public static final String DRIVER = "DRIVER";
    public static final String SUPPORT = "SUPPORT";
    public static final String ADMIN = "ADMIN";
    public static final String SUPER_ADMIN = "SUPER_ADMIN";
}
