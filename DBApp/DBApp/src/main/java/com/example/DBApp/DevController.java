package com.example.DBApp;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DevController {
    Map<String, String> tableNamesDictionary = new HashMap<>();

    @PostConstruct // Инициализируется при старте приложения
    public void init() {
        // Справочники
        tableNamesDictionary.put("Организации", "company");
        tableNamesDictionary.put("Сотрудники", "staff");
        tableNamesDictionary.put("Материалы", "material");
        tableNamesDictionary.put("Склады", "storage");
        tableNamesDictionary.put("Поставщики", "provaider");
        tableNamesDictionary.put("Отделы", "department");

        // Таблицы данных
        tableNamesDictionary.put("Учет расходов", "expense_accounting_log");
        tableNamesDictionary.put("Поступление материалов", "materials_receipt_log");
        tableNamesDictionary.put("Остатки на складе", "remains_storage");
        tableNamesDictionary.put("История цен", "history_price");
        tableNamesDictionary.put("Статьи затрат", "cost_items");
    }

    @Autowired
    JdbcOperations jdbcTemplate;

    @GetMapping("/dev/edit/tables/reference")
    public String showEditTablesReference(Model model) {
        String sql = "SELECT \n" +
                "  CASE \n" +
                "    WHEN table_name = 'company' THEN 'Организации'\n" +
                "    WHEN table_name = 'staff' THEN 'Сотрудники'\n" +
                "    WHEN table_name = 'material' THEN 'Материалы'\n" +
                "    WHEN table_name = 'provaider' THEN 'Поставщики'\n" +
                "    WHEN table_name = 'storage' THEN 'Склады'\n" +
                "    WHEN table_name = 'department' THEN 'Отделы'\n" +
                "    ELSE table_name\n" +
                "  END\n" +
                "FROM information_schema.tables \n" +
                "WHERE table_name IN ('company', 'staff', 'material', 'provaider', 'storage', 'department'); ";
        List<String> tables = jdbcTemplate.queryForList(sql, String.class);
        model.addAttribute("tables", tables);
        model.addAttribute("menuType", "reference");
        return "developerInterface/tables-list";
    }

    @GetMapping("/dev/edit/tables/data")
    public String showEditTablesData(Model model) {
        System.out.println("model = " + model);
        String sql = "SELECT \n" +
                "  CASE \n" +
                "    WHEN table_name = 'expense_accounting_log' THEN 'Учет расходов'\n" +
                "    WHEN table_name = 'materials_receipt_log' THEN 'Поступление материалов'\n" +
                "    WHEN table_name = 'remains_storage' THEN 'Остатки на складе'\n" +
                "    WHEN table_name = 'history_price' THEN 'История цен'\n" +
                "    WHEN table_name = 'cost_items' THEN 'Статьи затрат'\n" +
                "    ELSE table_name\n" +
                "  END\n" +
                "FROM information_schema.tables \n" +
                "WHERE table_name IN (\n" +
                "  'expense_accounting_log', \n" +
                "  'materials_receipt_log', \n" +
                "  'remains_storage', \n" +
                "  'history_price', \n" +
                "  'cost_items'\n" +
                ");";
        List<String> tables = jdbcTemplate.queryForList(sql, String.class);
        model.addAttribute("tables", tables);
        model.addAttribute("menuType", "data");
        return "developerInterface/tables-list";
    }

    @GetMapping("/dev/edit/tables/reference/{name}")
    public String showTableData(@PathVariable String name, Model model) {
        String realName = tableNamesDictionary.get(name);
        String sql = "SELECT * FROM " + realName;
        List<Map<String, Object>> data = jdbcTemplate.queryForList(sql);
        System.out.println("name = " + data);
        model.addAttribute("tableName", name);
        model.addAttribute("tableData", data);
        return "developerInterface/tables-data";
    }

    @GetMapping("/dev/edit/tables/{type}/{name}/edit")
    public String showEditForm(
            @PathVariable String name,
            @PathVariable String type,
            @RequestParam Map<String, String> params,
            Model model) {
        String realName = tableNamesDictionary.get(name);
        String idColumn = params.keySet().iterator().next();
        Integer idValue = Integer.parseInt(params.get(idColumn));
        String sql = "SELECT * FROM " + realName + " WHERE " + idColumn + " = ?";
        Map<String, Object> record = jdbcTemplate.queryForMap(sql, idValue); // Передаём число

        model.addAttribute("tableName", name);
        model.addAttribute("record", record);
        model.addAttribute("idColumn", idColumn);
        System.out.println("name = " + name + ", params = " + params + ", model = " + model);
        return "developerInterface/edit-form";
    }

    @PostMapping("/dev/edit/tables/{type}/{name}/update")
    public String updateRecord(
            @PathVariable String name,
            @RequestParam Map<String, String> params,
            Model model) {
        String realName = tableNamesDictionary.get(name);
        String idColumn = params.keySet().iterator().next();
        try {
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                    "SELECT column_name, data_type FROM information_schema.columns " +
                            "WHERE table_name = ?",
                    realName
            );
            Map<String, String> columnTypes = new HashMap<>();
            for (Map<String, Object> row : columns) {
                columnTypes.put(
                        row.get("column_name").toString(),
                        row.get("data_type").toString()
                );
            }
            Object idValue = convertValue(params.get(idColumn), columnTypes.get(idColumn));
            StringBuilder setClause = new StringBuilder();
            List<Object> values = new ArrayList<>();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String column = entry.getKey();
                if (!column.equals(idColumn)) {
                    setClause.append(column).append(" = ?, ");
                    String type = columnTypes.get(column);
                    values.add(convertValue(entry.getValue(), type));
                }
            }
            if (setClause.length() > 0) {
                setClause.setLength(setClause.length() - 2);
            }
            values.add(idValue);
            String updateSQL = "UPDATE " + realName + " SET " + setClause +
                    " WHERE " + idColumn + " = ?";
            jdbcTemplate.update(updateSQL, values.toArray());
            String rep=URLEncoder.encode(name, StandardCharsets.UTF_8).replace("+","%20");

            return "redirect:/dev/edit/tables/reference/" + rep;
        } catch (NumberFormatException e) {
            model.addAttribute("error", "Некорректный числовой формат: " + e.getMessage());
            return "error-page";
        } catch (DataAccessException e) {
            model.addAttribute("error", "Ошибка базы данных: " + e.getMostSpecificCause().getMessage());
            return "error-page";
        }
    }

    private Object convertValue(String value, String columnType) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        try {
            return switch (columnType) {
                case "smallint" -> Short.parseShort(value);
                case "integer" -> Integer.parseInt(value);
                case "bigint" -> Long.parseLong(value);
                case "numeric", "decimal" -> new BigDecimal(value);
                case "real", "double precision" -> Double.parseDouble(value);
                default -> value; // Для строковых типов
            };
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Не удалось преобразовать значение '" + value +
                            "' к типу " + columnType
            );
        }
    }
    @PostMapping("/dev/edit/tables/reference/{tableName}/delete")
    public String deleteRecord(@PathVariable String tableName,Model model,@RequestParam Map<String, String> params){
        String realName=tableNamesDictionary.get(tableName);
        params.remove("_method");
        String idColumn = params.keySet().iterator().next();
        Integer idValue = Integer.parseInt(params.get(idColumn));
        System.out.println("tableName = " + idValue);
        String sql="DELETE FROM "+realName+" WHERE " + idColumn+ " = " +idValue;
        jdbcTemplate.update(sql);
        String rep=URLEncoder.encode(tableName, StandardCharsets.UTF_8).replace("+","%20");
        return "redirect:/dev/edit/tables/reference/" + rep;
    }
    @GetMapping("/dev/edit/tables/reference/{tableName}/add")
    public String showAddForm(
            @PathVariable("tableName") String tableName,
            Model model) {
        String realName = tableNamesDictionary.get(tableName);
        List<String> columns = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_name = ?",
                String.class,
                realName
        );
        Map<String, Object> emptyRecord = new HashMap<>();
        for (String column : columns) {
            emptyRecord.put(column, "");
        }
        String idColumn = columns.stream()
                .filter(col -> col.toLowerCase().contains("id"))
                .findFirst()
                .orElse(columns.get(0));
        model.addAttribute("tableName", tableName);
        model.addAttribute("record", emptyRecord);
        model.addAttribute("idColumn", idColumn);
        return "developerInterface/add-form";
    }
//    @PostMapping("/dev/edit/tables/reference/{tableName}/create")
//    public String createRecord()
}
