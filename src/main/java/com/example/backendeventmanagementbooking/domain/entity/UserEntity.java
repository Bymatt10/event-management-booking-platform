package com.example.backendeventmanagementbooking.domain.entity;

import com.example.backendeventmanagementbooking.enums.RolesType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.UUID;

@Data
@Entity(name = "user")
@DynamicInsert
@DynamicUpdate
@EqualsAndHashCode
public class UserEntity {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolesType role;

    @JsonIgnore
    public UUID getId() {
        return this.userId;
    }
}
