package com.mntech.shop.inventory.service.impl;

import com.mntech.shop.inventory.dao.StockDeductRecordMapper;
import com.mntech.shop.inventory.model.StockDeductRecord;
import com.mntech.shop.inventory.service.IStockDeductRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockDeductRecordService implements IStockDeductRecordService {
    private final StockDeductRecordMapper recordMapper;
    @Override
    public StockDeductRecord getStockDeductRecord(String bizNo) {
        return recordMapper.selectByBizNo(bizNo);
    }
}
