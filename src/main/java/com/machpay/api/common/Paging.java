package com.machpay.api.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Paging {
    private int page;

    private int pageSize;

    private Long totalCount;
}