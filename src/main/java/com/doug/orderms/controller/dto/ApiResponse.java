package com.doug.orderms.controller.dto;

import java.util.List;


public record ApiResponse<T>(OrderSummaryResponse summary,
                             List<T> data,
                             PaginationResponse pagination
) {
}
