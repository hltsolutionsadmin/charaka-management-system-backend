package com.juvarya.order.service.impl;

import com.juvarya.order.client.ProductClient;
import com.juvarya.order.client.RestaurantFeignClient;
import com.juvarya.order.dao.OrderRepository;
import com.juvarya.order.dto.ItemWiseOrderReportDTO;
import com.juvarya.order.dto.ProductDTO;
import com.juvarya.order.entity.OrderModel;
import com.juvarya.order.service.OrderReportService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderReportServiceImpl implements OrderReportService {


    private static final String[] HEADERS = {"Business", "Product ID", "Product Name", "Category", "Quantity", "Price", "Discount", "Tax %", "Tax Amt", "Gross Sales", "Total", "Order Number", "Order Date", "User ID", "Business ID", "Taxable"};

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    @Autowired
    private RestaurantFeignClient restaurantFeignClient;

    @Override
    public Page<ItemWiseOrderReportDTO> generatePaginatedOutletItemWiseReport(LocalDate start, LocalDate end, Long businessId, String type, int page, int size) {
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.plusDays(1).atStartOfDay().minusNanos(1);

        // Cache and grouping
        Map<Long, ProductDTO> productCache = new HashMap<>();
        Map<Long, List<ItemWiseOrderReportDTO>> groupedItems = new HashMap<>();

        List<ItemWiseOrderReportDTO> rawItems = new ArrayList<>();

        if ("Online".equalsIgnoreCase(type)) {
            List<OrderModel> orders = orderRepository.findByCreatedDateBetweenAndBusinessId(startDateTime, endDateTime, businessId);
            for (OrderModel order : orders) {
                for (var item : order.getOrderItems()) {
                    ItemWiseOrderReportDTO dto = new ItemWiseOrderReportDTO();
                    dto.setBusinessId(order.getBusinessId());
                    dto.setBusinessName(order.getBusinessName());
                    dto.setUserId(order.getUserId());
                    dto.setProductId(item.getProductId());
                    dto.setQuantity(item.getQuantity());
                    dto.setPrice(item.getPrice());
                    dto.setTaxAmount(item.getTaxAmount());
                    dto.setTaxPercentage(item.getTaxPercentage());
                    dto.setTotal(item.getTotalAmount());
                    dto.setGrossSales(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                    dto.setDiscount(BigDecimal.ZERO);
                    dto.setTaxable(!Boolean.TRUE.equals(item.getTaxIgnored()));
                    rawItems.add(dto);
                }
            }
        } else {
            Page<ItemWiseOrderReportDTO> dineInPage = restaurantFeignClient.viewBasicRestaurantSalesReport(businessId, start, end, page, size);
            rawItems = dineInPage.getContent();
        }

        for (ItemWiseOrderReportDTO dto : rawItems) {
            groupedItems.computeIfAbsent(dto.getProductId(), k -> new ArrayList<>()).add(dto);
        }

        // Aggregation
        List<ItemWiseOrderReportDTO> aggregatedList = new ArrayList<>();
        for (Map.Entry<Long, List<ItemWiseOrderReportDTO>> entry : groupedItems.entrySet()) {
            Long productId = entry.getKey();
            List<ItemWiseOrderReportDTO> group = entry.getValue();

            ItemWiseOrderReportDTO agg = new ItemWiseOrderReportDTO();
            agg.setProductId(productId);
            agg.setBusinessId(group.get(0).getBusinessId());
            agg.setBusinessName(group.get(0).getBusinessName());
            agg.setTaxable(group.get(0).getTaxable());

            // Enrich product info
            ProductDTO product = productCache.computeIfAbsent(productId, pid -> {
                try {
                    return productClient.getProductById(pid);
                } catch (Exception ex) {
                    return null;
                }
            });

            if (product != null) {
                agg.setProductName(product.getName());
                agg.setCategoryName(product.getCategoryName());
            }

            int totalQty = group.stream().mapToInt(ItemWiseOrderReportDTO::getQuantity).sum();
            BigDecimal grossSales = group.stream().map(ItemWiseOrderReportDTO::getGrossSales).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal total = group.stream().map(ItemWiseOrderReportDTO::getTotal).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal tax = group.stream().map(ItemWiseOrderReportDTO::getTaxAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
            List<BigDecimal> prices = group.stream().map(ItemWiseOrderReportDTO::getPrice).filter(Objects::nonNull).toList();

            agg.setQuantity(totalQty);
            agg.setGrossSales(grossSales);
            agg.setTotal(total);
            agg.setTaxAmount(tax);
            agg.setMin(prices.stream().min(Comparator.naturalOrder()).orElse(null));
            agg.setMax(prices.stream().max(Comparator.naturalOrder()).orElse(null));
            agg.setAvg(prices.isEmpty() ? null : prices.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(prices.size()), 2, RoundingMode.HALF_UP));

            aggregatedList.add(agg);
        }

        // Manual pagination
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, aggregatedList.size());
        List<ItemWiseOrderReportDTO> paginatedList = aggregatedList.subList(startIndex, endIndex);

        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(paginatedList, pageable, aggregatedList.size());
    }


    @Override
    public ByteArrayInputStream exportOutletItemWiseExcel(List<ItemWiseOrderReportDTO> data) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Outlet Item Wise Report");
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);

            int rowIdx = 0;
            rowIdx = writeSection(sheet, data, true, "Taxable Items", headerStyle, rowIdx);
            rowIdx++;
            rowIdx = writeSection(sheet, data, false, "Non-Taxable Items", headerStyle, rowIdx);
            rowIdx++;

            rowIdx = writeTotalRow(sheet, "Taxable Total", sumByTaxable(data, true), headerStyle, rowIdx);
            rowIdx = writeTotalRow(sheet, "Non-Taxable Total", sumByTaxable(data, false), headerStyle, rowIdx);
            rowIdx = writeTotalRow(sheet, "Grand Total", sumByTaxable(data, null), headerStyle, rowIdx);

            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    private BigDecimal sumByTaxable(List<ItemWiseOrderReportDTO> data, Boolean taxable) {
        return data.stream().filter(dto -> taxable == null || Boolean.valueOf(taxable).equals(dto.getTaxable())).map(ItemWiseOrderReportDTO::getTotal).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int writeSection(Sheet sheet, List<ItemWiseOrderReportDTO> data, boolean taxableSection, String title, CellStyle headerStyle, int rowIdx) {

        Row titleRow = sheet.createRow(rowIdx++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(title);
        titleCell.setCellStyle(headerStyle);

        Row headerRow = sheet.createRow(rowIdx++);
        for (int i = 0; i < HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }

        sheet.setAutoFilter(new CellRangeAddress(headerRow.getRowNum(), headerRow.getRowNum(), 0, HEADERS.length - 1));

        BigDecimal subTotal = BigDecimal.ZERO;

        for (ItemWiseOrderReportDTO dto : data) {
            if (Boolean.TRUE.equals(dto.getTaxable()) != taxableSection) continue;

            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(dto.getBusinessName());
            row.createCell(1).setCellValue(dto.getProductId() != null ? dto.getProductId().toString() : "");
            row.createCell(2).setCellValue(Optional.ofNullable(dto.getProductName()).orElse(""));
            row.createCell(3).setCellValue(Optional.ofNullable(dto.getCategoryName()).orElse(""));
            row.createCell(4).setCellValue(Optional.ofNullable(dto.getQuantity()).orElse(0));
            row.createCell(5).setCellValue(Optional.ofNullable(dto.getPrice()).map(BigDecimal::doubleValue).orElse(0.0));
            row.createCell(6).setCellValue(Optional.ofNullable(dto.getDiscount()).map(BigDecimal::doubleValue).orElse(0.0));
            row.createCell(7).setCellValue(Optional.ofNullable(dto.getTaxPercentage()).map(BigDecimal::doubleValue).orElse(0.0));
            row.createCell(8).setCellValue(Optional.ofNullable(dto.getTaxAmount()).map(BigDecimal::doubleValue).orElse(0.0));
            row.createCell(9).setCellValue(Optional.ofNullable(dto.getGrossSales()).map(BigDecimal::doubleValue).orElse(0.0));
            row.createCell(10).setCellValue(Optional.ofNullable(dto.getTotal()).map(BigDecimal::doubleValue).orElse(0.0));
            row.createCell(11).setCellValue(""); // Order number not relevant for aggregated
            row.createCell(12).setCellValue(""); // Order date not relevant
            row.createCell(13).setCellValue(dto.getUserId() != null ? dto.getUserId().toString() : "");
            row.createCell(14).setCellValue(dto.getBusinessId() != null ? dto.getBusinessId().toString() : "");
            row.createCell(15).setCellValue(Boolean.TRUE.equals(dto.getTaxable()) ? "Yes" : "No");

            subTotal = subTotal.add(Optional.ofNullable(dto.getTotal()).orElse(BigDecimal.ZERO));
        }

        Row subtotalRow = sheet.createRow(rowIdx++);
        Cell subtotalLabel = subtotalRow.createCell(0);
        subtotalLabel.setCellValue("Subtotal for " + title);
        subtotalLabel.setCellStyle(headerStyle);

        Cell subtotalValue = subtotalRow.createCell(10);
        subtotalValue.setCellValue(subTotal.doubleValue());
        subtotalValue.setCellStyle(headerStyle);

        return rowIdx;
    }

    private int writeTotalRow(Sheet sheet, String label, BigDecimal amount, CellStyle style, int rowIdx) {
        Row row = sheet.createRow(rowIdx++);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(style);

        Cell amountCell = row.createCell(10);
        amountCell.setCellValue(Optional.ofNullable(amount).map(BigDecimal::doubleValue).orElse(0.0));
        amountCell.setCellStyle(style);

        return rowIdx;
    }
}
