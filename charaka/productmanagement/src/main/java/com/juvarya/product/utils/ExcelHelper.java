package com.juvarya.product.utils;

import com.hlt.auth.exception.handling.ErrorCode;
import com.hlt.auth.exception.handling.HltCustomerException;
import com.juvarya.product.dto.ProductAttributeDTO;
import com.juvarya.product.dto.ProductDTO;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

public class ExcelHelper {

    public static List<ProductDTO> parseProductsExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new HltCustomerException(ErrorCode.INVALID_FILE);
        }

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            List<ProductDTO> products = new ArrayList<>();

            Row headerRow = sheet.getRow(0);
            int lastColumn = headerRow.getLastCellNum();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // Skip header
                Row row = sheet.getRow(i);
                if (row == null) continue;

                ProductDTO dto = new ProductDTO();
                dto.setName(getCellValue(row.getCell(0)));
                dto.setShortCode(getCellValue(row.getCell(1)));
                dto.setIgnoreTax(parseBooleanSafe(row.getCell(2)));
                dto.setDiscount(parseBooleanSafe(row.getCell(3)));
                dto.setDescription(getCellValue(row.getCell(4)));
                dto.setPrice(parseDoubleSafe(row.getCell(5)));
                dto.setAvailable(parseBooleanSafe(row.getCell(6)));
                dto.setProductType(getCellValue(row.getCell(7)));
                dto.setBusinessId(parseLongSafe(row.getCell(8)));
                dto.setCategoryId(parseLongSafe(row.getCell(9)));

                // Parse attributes dynamically: columns 10 onwards in pairs
                List<ProductAttributeDTO> attributes = new ArrayList<>();
                for (int j = 10; j + 1 < lastColumn; j += 2) {
                    String attrName = getCellValue(row.getCell(j));
                    String attrValue = getCellValue(row.getCell(j + 1));
                    if (!attrName.isBlank() && !attrValue.isBlank()) {
                        attributes.add(new ProductAttributeDTO(null, attrName, attrValue));
                    }
                }
                dto.setAttributes(attributes);

                products.add(dto);
            }

            return products;

        } catch (Exception e) {
            throw new HltCustomerException(ErrorCode.INVALID_FILE);
        }
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private static Double parseDoubleSafe(Cell cell) {
        try {
            return Double.parseDouble(getCellValue(cell));
        } catch (Exception e) {
            return 0.0;
        }
    }

    private static Boolean parseBooleanSafe(Cell cell) {
        try {
            String val = getCellValue(cell).toLowerCase();
            return val.equals("true") || val.equals("1");
        } catch (Exception e) {
            return false;
        }
    }

    private static Long parseLongSafe(Cell cell) {
        try {
            return Long.parseLong(getCellValue(cell).split("\\.")[0]);
        } catch (Exception e) {
            return null;
        }
    }
}
