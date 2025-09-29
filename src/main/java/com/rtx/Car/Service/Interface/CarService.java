package com.rtx.Car.Service.Interface;

import com.rtx.Car.DTo.CarDTO;

import com.rtx.Car.Entity.Car;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface CarService {
    Mono<CarDTO> saveCar(CarDTO carDTO);


    Mono<Car> findCarByregistrationNumber(String registrationNumber);

    Flux<Car> findCarsByModel(String model);

    Flux<CarDTO> searchCars(String model, String color, String registrationNumber);

}
