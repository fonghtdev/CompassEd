package com.compassed.compassed_api.service.impl;

import org.springframework.stereotype.Service;

import com.compassed.compassed_api.service.PricingService;

@Service
public class PricingServiceImpl implements PricingService {

    @Override
    public long calculateTotalAmountVnd(int subjectCount) {
        if (subjectCount <= 0) throw new RuntimeException("Must select at least 1 subject");
        if (subjectCount == 1) return 50_000L;
        if (subjectCount == 2) return 90_000L;
        if (subjectCount == 3) return 120_000L;
        // thêm môn thì sửa rule ở đây
        throw new RuntimeException("Unsupported subject count: " + subjectCount);
    }
}
