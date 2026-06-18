package com.guilhermesemog.unimove.model;

import com.guilhermesemog.unimove.model.enums.Role;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String cpf;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true)
    private String phone;

    @Column(nullable = false)
    private Boolean active;

    @Column(nullable = false)
    private Role role;

    public User(String cpf, String password, String firstName, String lastName, String phone, Boolean active, Role role) {
        this.cpf = cpf;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.active = active;
        this.role = role;
    }
}
