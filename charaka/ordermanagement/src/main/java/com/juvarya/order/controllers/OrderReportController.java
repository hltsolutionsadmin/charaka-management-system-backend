package com.juvarya.order.controllers;

import com.hlt.commonservice.dto.StandardResponse;
import com.juvarya.order.dto.ItemWiseOrderReportDTO;
import com.juvarya.order.service.OrderReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class OrderReportController {

    private final OrderReportService orderReportService;

    @GetMapping("/outlet-itemwise/excel")
    public ResponseEntity<InputStreamResource> downloadOutletItemWiseExcelReport(
            @RequestParam("startDate") String startDateStr,
            @RequestParam("endDate") String endDateStr,
            @RequestParam("businessId") Long businessId,
            @RequestParam(value = "type", required = true) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws IOException {

        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);

        Page<ItemWiseOrderReportDTO> reportPage = orderReportService
                .generatePaginatedOutletItemWiseReport(startDate, endDate, businessId, type,page, size);

        ByteArrayInputStream in = orderReportService.exportOutletItemWiseExcel(reportPage.getContent());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=OutletItemWiseReport.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/outlet-itemwise/paged")
    public ResponseEntity<StandardResponse<Page<ItemWiseOrderReportDTO>>> viewOutletItemWiseReportPaged(
            @RequestParam("startDate") String startDateStr,
            @RequestParam("endDate") String endDateStr,
            @RequestParam("businessId") Long businessId,
            @RequestParam(value = "type", required = true) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);

        Page<ItemWiseOrderReportDTO> reportData = orderReportService.generatePaginatedOutletItemWiseReport(
                startDate, endDate, businessId, type, page, size);

        return ResponseEntity.ok(StandardResponse.page("Paginated outlet item wise report fetched successfully", reportData));
    }

}
