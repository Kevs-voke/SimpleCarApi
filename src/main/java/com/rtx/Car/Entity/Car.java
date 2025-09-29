package com.rtx.Car.Entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("car")
@Getter
@Setter
public class Car {
    @Id
    private Long id;

    private String vin;   // unique
    private String make;
    private String model;

    @Column("manufacture_year")   // avoid reserved word "year"
    private int year;

    private String color;

    @Column("engine_type")
    private String engineType;

    @Column("registration_number")
    private String registrationNumber;
}
