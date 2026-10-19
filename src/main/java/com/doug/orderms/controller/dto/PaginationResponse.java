package com.doug.orderms.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

public record PaginationResponse(
                                 @Schema(description = "Número da página atual, começando em 0.", example = "0")
                                 Integer page,

                                 @Schema(description = "Quantidade de pedidos por página.", example = "10")
                                 Integer pageSize,

                                 @Schema(description = "Quantidade total de pedidos encontrados.", example = "25")
                                 Long totalElements,

                                 @Schema(description = "Quantidade total de páginas.", example = "3")
                                 Integer totalPages) {

    public static PaginationResponse fromPage(Page<?> page){
        return new PaginationResponse(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
