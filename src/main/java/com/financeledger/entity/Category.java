package com.financeledger.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A predefined category for transaction classification.
 * Used by the AI auto-categorization engine in Week 5.
 */
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CategoryType type;

    protected Category() {
    }

    public Category(String name, CategoryType type) {
        this.name = name;
        this.type = type;
    }

    // ── Getters ──

    public Long getId() { return id; }
    public String getName() { return name; }
    public CategoryType getType() { return type; }
}
