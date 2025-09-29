package com.rtx.Car.Mapper;

import com.rtx.Car.DTo.CarDTO;
import com.rtx.Car.Entity.Car;
import org.springframework.stereotype.Component;

@Component
public class CarDTOMapper {

    public Car mapToEntity(CarDTO carDTO){
        Car car = new Car();
        car.setColor(carDTO.color());
        car.setEngineType(carDTO.engineType());
        car.setYear(carDTO.year());
        car.setMake(carDTO.make());
        car.setVin(carDTO.vin());
        car.setModel(carDTO.model());
        car.setRegistrationNumber(carDTO.registrationNumber());

        return car;
    }
    public CarDTO mapToDTO(Car car){
        return new CarDTO(
                car.getVin(),
                car.getMake(),
                car.getModel(),
                car.getYear(),
                car.getColor(),
                car.getEngineType(),
                car.getRegistrationNumber());

    }
}
