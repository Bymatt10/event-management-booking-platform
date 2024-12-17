package com.example.backendeventmanagementbooking.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static com.example.backendeventmanagementbooking.utils.GenericResponse.GenerateResponse;

@Data
@Builder
@AllArgsConstructor
public class PaginationUtils {

    public static <T> ResponseEntity<GenericResponse<PaginationDto<T>>> pageableRepository(Page<T> pageTuts) {
        try {
            return pageableGenericRepository(pageTuts).GenerateResponse();
        } catch (Exception e) {
            return GenerateResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static <T> GenericResponse<PaginationDto<T>> pageableGenericRepository(Page<T> pageTuts) {
        try {
            final var paginationDto = new PaginationDto<>(
                    pageTuts.getContent(),
                    pageTuts.getNumber(),
                    pageTuts.getTotalPages(),
                    pageTuts.getTotalElements()
            );
            return new GenericResponse<>(HttpStatus.OK, paginationDto);
        } catch (Exception e) {
            return new GenericResponse<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static <T> PaginationDto<T> pageableRepositoryPaginationDto(Page<T> pageTuts) {
        return new PaginationDto<>(
                pageTuts.getContent(),
                pageTuts.getNumber(),
                pageTuts.getTotalPages(),
                pageTuts.getTotalElements()
        );
    }

    @Data
    @AllArgsConstructor
    @Builder
    public static class PaginationDto<T> {
        private List<T> value;
        private Integer currentPage;
        private Integer totalPages;
        private Long totalItems;
    }
}
