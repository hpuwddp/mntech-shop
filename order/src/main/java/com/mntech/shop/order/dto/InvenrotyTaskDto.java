package com.mntech.shop.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvenrotyTaskDto implements Serializable {
    private String product_id;
    private int num;
    private String user_id;

}
