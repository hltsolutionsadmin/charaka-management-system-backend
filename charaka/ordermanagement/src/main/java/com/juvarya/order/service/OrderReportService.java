package com.juvarya.order.service;

import com.juvarya.order.dto.ItemWiseOrderReportDTO;
import org.springframework.data.domain.Page;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface OrderReportService {

    ByteArrayInputStream exportOutletItemWiseExcel(List<ItemWiseOrderReportDTO> data) throws IOException;

     Page<ItemWiseOrderReportDTO> generatePaginatedOutletItemWiseReport(LocalDate start, LocalDate end, Long businessId, String type, int page, int size);

}
