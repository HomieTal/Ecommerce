package com.training.ecommerce.repository.inmemory;

import com.training.ecommerce.domain.model.Coupon;
import com.training.ecommerce.repository.CouponRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Thread-safe in-memory {@link CouponRepository}, keyed by uppercase coupon code. */
@Repository
public class InMemoryCouponRepository implements CouponRepository {

    private final Map<String, Coupon> couponsByCode = new ConcurrentHashMap<>();

    @Override
    public Coupon save(Coupon coupon) {
        Objects.requireNonNull(coupon, "Coupon is required");
        couponsByCode.put(coupon.getCode(), coupon);
        return coupon;
    }

    @Override
    public Optional<Coupon> findByCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(couponsByCode.get(code.trim().toUpperCase(Locale.ROOT)));
    }

    @Override
    public List<Coupon> findAll() {
        return new ArrayList<>(couponsByCode.values());
    }
}
