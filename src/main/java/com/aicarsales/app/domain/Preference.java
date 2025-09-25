package com.aicarsales.app.domain;

import com.aicarsales.app.domain.converter.StringListJsonConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "preferences")
public class Preference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "budget_min")
    private Integer budgetMin;

    @Column(name = "budget_max")
    private Integer budgetMax;

    private String usage;

    private Short passengers;

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "preferred_body_types")
    private List<String> preferredBodyTypes;

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "preferred_brands")
    private List<String> preferredBrands;

    @Column(name = "year_range_start")
    private Short yearRangeStart;

    @Column(name = "year_range_end")
    private Short yearRangeEnd;

    @Column(name = "mileage_range_start")
    private Integer mileageRangeStart;

    @Column(name = "mileage_range_end")
    private Integer mileageRangeEnd;

    @Column(columnDefinition = "jsonb")
    private String options;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getBudgetMin() {
        return budgetMin;
    }

    public void setBudgetMin(Integer budgetMin) {
        this.budgetMin = budgetMin;
    }

    public Integer getBudgetMax() {
        return budgetMax;
    }

    public void setBudgetMax(Integer budgetMax) {
        this.budgetMax = budgetMax;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public Short getPassengers() {
        return passengers;
    }

    public void setPassengers(Short passengers) {
        this.passengers = passengers;
    }

    public List<String> getPreferredBodyTypes() {
        return preferredBodyTypes;
    }

    public void setPreferredBodyTypes(List<String> preferredBodyTypes) {
        this.preferredBodyTypes = preferredBodyTypes;
    }

    public List<String> getPreferredBrands() {
        return preferredBrands;
    }

    public void setPreferredBrands(List<String> preferredBrands) {
        this.preferredBrands = preferredBrands;
    }

    public Short getYearRangeStart() {
        return yearRangeStart;
    }

    public void setYearRangeStart(Short yearRangeStart) {
        this.yearRangeStart = yearRangeStart;
    }

    public Short getYearRangeEnd() {
        return yearRangeEnd;
    }

    public void setYearRangeEnd(Short yearRangeEnd) {
        this.yearRangeEnd = yearRangeEnd;
    }

    public Integer getMileageRangeStart() {
        return mileageRangeStart;
    }

    public void setMileageRangeStart(Integer mileageRangeStart) {
        this.mileageRangeStart = mileageRangeStart;
    }

    public Integer getMileageRangeEnd() {
        return mileageRangeEnd;
    }

    public void setMileageRangeEnd(Integer mileageRangeEnd) {
        this.mileageRangeEnd = mileageRangeEnd;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
