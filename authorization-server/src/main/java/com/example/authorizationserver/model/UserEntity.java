package com.example.authorizationserver.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Setter
@Getter
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"login"}), //        @UniqueConstraint(columnNames = {"login", ""}),
        @UniqueConstraint(columnNames = {"email"})
})
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    private String login;
    private String password;
    private String email;
    @CreationTimestamp
    @Column(name = "creation_date")
    private Timestamp creationDate;
    @UpdateTimestamp
    @Column(name = "last_change_date")
    private Timestamp lastChangeDate;
    @Column(name = "user_role")
    private String userRole;
}
