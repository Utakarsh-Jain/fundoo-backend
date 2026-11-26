
package com.fundoo.notes.entity;

import jakarta.persistence.*;

/*
 * Class Name  : Label
 * Description : Represents a user-specific label for organizing notes.
 * Author      : Utakarsh Jain
 * Date        : 25/11/2026
 */

@Entity
@Table(
        name = "labels",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"name", "user_id"}
                )
        }
)
public class Label {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // Multiple labels can belong to one user.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Label() {
    }

    public Label(String name, User user) {
        this.name = name;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
