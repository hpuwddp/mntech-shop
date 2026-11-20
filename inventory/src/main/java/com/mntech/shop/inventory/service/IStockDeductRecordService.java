package com.mntech.shop.inventory.service;

import com.mntech.shop.inventory.model.StockDeductRecord;

public interface IStockDeductRecordService {
    StockDeductRecord getStockDeductRecord(String bizNo);
}
