package com.space.service;

import com.space.controller.ShipFilter;
import com.space.controller.ShipOrder;
import com.space.controller.exception.ShipNotFoundException;
import com.space.controller.exception.ShipValidationException;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

@Service
public class ShipService {

    private final ShipRepository shipRepository;

    @Autowired
    public ShipService(ShipRepository shipRepository) {
        this.shipRepository = shipRepository;
    }

    public Page<Ship> findAll(ShipFilter shipFilter, Integer pageSize, Integer pageNumber, ShipOrder order) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.ASC, order.getFieldName()));
        Specification<Ship> spec = createSpecification(shipFilter);
        if (spec == null) {
            return shipRepository.findAll(pageable);
        }
        return shipRepository.findAll(Specification.where(spec), pageable);
    }

    public Integer count(ShipFilter shipFilter) {
        Specification<Ship> spec = createSpecification(shipFilter);
        if (spec == null) {
            return Math.toIntExact(shipRepository.count());
        }
        return Math.toIntExact(shipRepository.count(Specification.where(spec)));
    }

    private Specification<Ship> createSpecification(ShipFilter shipFilter) {
        Specification<Ship> spec = null;
        if (shipFilter.getName() != null) {
            spec = byName(shipFilter.getName());
        }
        if (shipFilter.getPlanet() != null) {
            if (spec == null) {
                spec = byPlanet(shipFilter.getPlanet());
            } else {
                spec = Specification.where(spec).and(byPlanet(shipFilter.getPlanet()));
            }
        }
        if (shipFilter.getAfter() != null && shipFilter.getBefore() != null) {
            if (spec == null) {
                spec = byProdDate(shipFilter.getAfter(), shipFilter.getBefore());
            } else {
                spec = Specification.where(spec).and(byProdDate(shipFilter.getAfter(), shipFilter.getBefore()));
            }
        }
        if(shipFilter.getAfter() != null) {
            if (spec == null) {
                spec = byProdDateAfter(shipFilter.getAfter());
            } else {
                spec = Specification.where((spec).and(byProdDateAfter(shipFilter.getAfter())));
            }
        }
        if(shipFilter.getBefore() != null) {
            if (spec == null) {
                spec = byProdDateBefore(shipFilter.getBefore());
            } else {
                spec = Specification.where((spec).and(byProdDateBefore(shipFilter.getBefore())));
            }
        }
        if (shipFilter.getShipType() != null) {
            if (spec == null) {
                spec = byShipType(shipFilter.getShipType());
            } else {
                spec = Specification.where(spec).and(byShipType(shipFilter.getShipType()));
            }
        }
        if (shipFilter.getIsUsed() != null) {
            if (spec == null) {
                spec = byUsed(shipFilter.getIsUsed());
            } else {
                spec = Specification.where(spec).and(byUsed(shipFilter.getIsUsed()));
            }
        }
        if (shipFilter.getMinCrewSize() != null && shipFilter.getMaxCrewSize() != null) {
            if (spec == null) {
                spec = byCrewSize(shipFilter.getMinCrewSize(), shipFilter.getMaxCrewSize());
            } else {
                spec = Specification.where(spec).and(byCrewSize(shipFilter.getMinCrewSize(), shipFilter.getMaxCrewSize()));
            }
        }
        if (shipFilter.getMinCrewSize() != null) {
            if (spec == null) {
                spec = byCrewSizeAfter(shipFilter.getMinCrewSize());
            } else {
                spec = Specification.where((spec).and(byCrewSizeAfter(shipFilter.getMinCrewSize())));
            }
        }
        if (shipFilter.getMaxCrewSize() != null) {
            if (spec == null) {
                spec = byCrewSizeBefore(shipFilter.getMaxCrewSize());
            } else {
                spec = Specification.where((spec).and(byCrewSizeBefore(shipFilter.getMaxCrewSize())));
            }
        }
        if (shipFilter.getMinSpeed() != null && shipFilter.getMaxSpeed() != null) {
            if (spec == null) {
                spec = bySpeed(shipFilter.getMinSpeed(), shipFilter.getMaxSpeed());
            } else {
                spec = Specification.where(spec).and(bySpeed(shipFilter.getMinSpeed(), shipFilter.getMaxSpeed()));
            }
        }
        if (shipFilter.getMinSpeed() != null) {
            if (spec == null) {
                spec = bySpeedAfter(shipFilter.getMinSpeed());
            } else {
                spec = Specification.where(spec).and(bySpeedAfter(shipFilter.getMinSpeed()));
            }
        }
        if (shipFilter.getMaxSpeed() != null) {
            if (spec == null) {
                spec = bySpeedBefore(shipFilter.getMaxSpeed());
            } else {
                spec = Specification.where(spec).and(bySpeedBefore(shipFilter.getMaxSpeed()));
            }
        }
        if (shipFilter.getMinRating() != null && shipFilter.getMaxRating() != null) {
            if (spec == null) {
                spec = byRating(shipFilter.getMinRating(), shipFilter.getMaxRating());
            } else {
                spec = Specification.where(spec).and(byRating(shipFilter.getMinRating(), shipFilter.getMaxRating()));
            }
        }
        if (shipFilter.getMinRating() != null) {
            if (spec == null) {
                spec = byRatingAfter(shipFilter.getMinRating());
            } else {
                spec = Specification.where(spec).and(byRatingAfter(shipFilter.getMinRating()));
            }
        }
        if (shipFilter.getMaxRating() != null) {
            if (spec == null) {
                spec = byRatingBefore(shipFilter.getMaxRating());
            } else {
                spec = Specification.where(spec).and(byRatingBefore(shipFilter.getMaxRating()));
            }
        }
        return spec;
    }

    private Specification<Ship> byName(String name) {
        return new Specification<Ship>() {
            @Override
            public Predicate toPredicate(Root<Ship> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.like(root.get("name"), "%" + name + "%");
            }
        };
    }

    private Specification<Ship> byPlanet(String planet) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get("planet"), "%" + planet + "%");
    }

    private Specification<Ship> byShipType(ShipType shipType) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("shipType"), shipType.name());
    }

    private Specification<Ship> byUsed(Boolean used) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("isUsed"), used);
    }

    private Specification<Ship> byProdDate(Long after, Long before) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.greaterThanOrEqualTo(root.get("prodDate"), new Date(after)),
                        criteriaBuilder.lessThanOrEqualTo(root.get("prodDate"), new Date(before))
                );
    }

    private Specification<Ship> byProdDateAfter(Long after) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.greaterThanOrEqualTo(root.get("prodDate"), new Date(after))
                );
    }

    private Specification<Ship> byProdDateBefore(Long before) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.lessThanOrEqualTo(root.get("prodDate"), new Date(before))
                );
    }

    private Specification<Ship> byCrewSize(Integer after, Integer before) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.ge(root.get("crewSize"), after),
                        criteriaBuilder.le(root.get("crewSize"), before)
                );
    }

    private Specification<Ship> byCrewSizeAfter(Integer after) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.ge(root.get("crewSize"), after)
                );
    }

    private Specification<Ship> byCrewSizeBefore(Integer before) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.le(root.get("crewSize"), before)
                );
    }

    private Specification<Ship> bySpeed(Double after, Double before) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.ge(root.get("speed"), after),
                        criteriaBuilder.le(root.get("speed"), before)
                );
    }

    private Specification<Ship> bySpeedAfter(Double after) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.ge(root.get("speed"), after)
                );
    }

    private Specification<Ship> bySpeedBefore(Double before) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.le(root.get("speed"), before)
                );
    }

    private Specification<Ship> byRating(Double after, Double before) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.ge(root.get("rating"), after),
                        criteriaBuilder.le(root.get("rating"), before)
                );
    }

    private Specification<Ship> byRatingAfter(Double after) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.ge(root.get("rating"), after)
                );
    }

    private Specification<Ship> byRatingBefore(Double before) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.le(root.get("rating"), before)
                );
    }

    public Optional<Ship> findById(Long id) {
        return shipRepository.findById(id);
    }

    public void removeById(Long id) {
        shipRepository.deleteById(id);
    }

    public Ship saveShip(Ship ship) {
        if (ship.getUsed() == null) {
            ship.setUsed(false);
        }
        if (!isValidShip(ship)) {
            throw new ShipValidationException();
        }
        ship.setRating(calculateRating(ship));
        return shipRepository.save(ship);
    }


    public Ship updateShip(Ship ship) {
        if (!isValidId(ship.getId())) {
            throw new ShipValidationException();
        }
        Ship updatedShip = shipRepository.findById(ship.getId())
                .map(dbShip -> updateShipFields(dbShip, ship))
                .orElseThrow(ShipNotFoundException::new);
        if (!isValidShip(updatedShip)) {
            throw new ShipValidationException();
        }
        return shipRepository.save(updatedShip);
    }

    private Double calculateRating(Ship ship) {
        Double k;
        if (!ship.getUsed())
            k = 1.0;
        else
            k = 0.5;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(ship.getProdDate());
        int year = calendar.get(Calendar.YEAR);
        double r = (80 * ship.getSpeed() * k) / (3019 - year + 1);
        BigDecimal bigDecimal = new BigDecimal(r);
        return bigDecimal.setScale(2, RoundingMode.HALF_DOWN).doubleValue();
    }

    public Boolean isValidYear(Ship ship) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(ship.getProdDate());
        int year = calendar.get(Calendar.YEAR);
        return year >= 2800 && year <= 3019;
    }

    private Ship updateShipFields(Ship source, Ship target) {
        target.setName(getValue(source, target, Ship::getName));
        target.setUsed(getValue(source, target, Ship::getUsed));
        target.setCrewSize(getValue(source, target, Ship::getCrewSize));
        target.setPlanet(getValue(source, target, Ship::getPlanet));
        target.setProdDate(getValue(source, target, Ship::getProdDate));
        target.setShipType(getValue(source, target, Ship::getShipType));
        target.setSpeed(getValue(source, target, Ship::getSpeed));
        target.setRating(calculateRating(target));
        return target;
    }

    private <T> T getValue(Ship source, Ship target, Function<Ship, T> func) {
        if (func.apply(target) == null) {
            return func.apply(source);
        } else {
            return func.apply(target);
        }
    }

    private boolean isValidId(Long id) {
        return id != null && id > 0;
    }

    private boolean isValidShip(Ship ship) {
        if (ship == null)
            return false;
        if (ship.getName() == null || ship.getName().length() > 50 || ship.getName().equals(""))
            return false;
        if (ship.getPlanet() == null || ship.getPlanet().length() > 50 || ship.getPlanet().equals(""))
            return false;
        if (ship.getSpeed() == null || ship.getSpeed() < 0.01 || ship.getSpeed() > 0.99)
            return false;
        if (ship.getCrewSize() == null || ship.getCrewSize() < 1 || ship.getCrewSize() > 9999)
            return false;
        if (ship.getProdDate() == null || ship.getProdDate().getTime() < 0)
            return false;
        return isValidYear(ship);
    }
}
