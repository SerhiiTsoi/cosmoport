package com.space.controller;

import com.space.controller.exception.ShipNotFoundException;
import com.space.controller.exception.ShipValidationException;
import com.space.model.Ship;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rest/ships")
public class ShipController {
    private ShipService shipService;

    @Autowired
    public ShipController(ShipService shipService) {
        this.shipService = shipService;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<Ship> getShip(@PathVariable("id") Long id) {
        if (isValidId(id)) {
            return ResponseEntity.of(shipService.findById(id));
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<Ship> saveShip(@RequestBody Ship ship) {
        try {
            return new ResponseEntity<>(shipService.saveShip(ship), HttpStatus.OK);
        } catch (ShipValidationException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    public ResponseEntity<Ship> updateShip(@PathVariable("id") Long id, @RequestBody Ship ship) {
        ship.setId(id);
        try {
            return new ResponseEntity<>(shipService.updateShip(ship), HttpStatus.OK);
        } catch (ShipNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ShipValidationException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Ship> deleteShip(@PathVariable Long id) {
        if (!isValidId(id)) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        Optional<Ship> ship = shipService.findById(id);
        if (!ship.isPresent())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        else {
            shipService.removeById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<List<Ship>> getAll(ShipFilter shipFilter,
                                             @RequestParam(defaultValue = "3") Integer pageSize,
                                             @RequestParam(defaultValue = "0") Integer pageNumber,
                                             @RequestParam(defaultValue = "ID") ShipOrder order) {
        Page<Ship> page = shipService.findAll(shipFilter, pageSize, pageNumber, order);
        return new ResponseEntity<>(page.get().collect(Collectors.toList()), HttpStatus.OK);
    }

    @RequestMapping(value = "/count", method = RequestMethod.GET)
    public ResponseEntity<Integer> countShip(ShipFilter shipFilter) {
        return new ResponseEntity<>(shipService.count(shipFilter), HttpStatus.OK);
    }

    private boolean isValidId(Long id) {
        return id != null && id > 0;
    }

}
