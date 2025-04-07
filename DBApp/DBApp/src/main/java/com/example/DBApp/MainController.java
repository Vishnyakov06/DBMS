package com.example.DBApp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MainController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/")
    public String mainPage(Model model) {
        return "userInterface/main";
    }
    Map<String, String> tableNamesDictionary = new HashMap<>();
    @GetMapping("/tables/reference")
    public String showTablesReference(Model model) {
        tableNamesDictionary.put("Организации", "company");
        tableNamesDictionary.put("Сотрудники", "staff");
        tableNamesDictionary.put("Материалы", "material");
        tableNamesDictionary.put("Склады", "storage");
        tableNamesDictionary.put("Поставщики", "provaider");
        tableNamesDictionary.put("Отделы", "department");
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
        return "userInterface/tables-list";
    }

    @GetMapping("/tables/data")
    public String showTablesData(Model model){
        tableNamesDictionary.put("Учет расходов", "expense_accounting_log");
        tableNamesDictionary.put("Поступление материалов", "materials_receipt_log");
        tableNamesDictionary.put("Остатки на складе", "remains_storage");
        tableNamesDictionary.put("История цен", "history_price");
        tableNamesDictionary.put("Статьи затрат", "cost_items");
        String sql="SELECT \n" +
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
        List<String> tables=jdbcTemplate.queryForList(sql,String.class);
        model.addAttribute("tables",tables);
        model.addAttribute("menuType","data");
        return "userInterface/tables-list";
    }
    @GetMapping("/data/{tableName}")
    public String showTableData(@PathVariable String tableName, Model model) {
        tableName=tableNamesDictionary.get(tableName);
        String sql = "SELECT * FROM " + tableName;
        List<Map<String, Object>> data = jdbcTemplate.queryForList(sql);

        model.addAttribute("tableName", tableName);
        model.addAttribute("tableData", data);
        return "userInterface/table-data";
    }
   

    @GetMapping("/reference/{tableName}")
    public String showTableReference(@PathVariable String tableName, Model model) {
        tableName=tableNamesDictionary.get(tableName);
        String sql = "SELECT * FROM " + tableName;
        List<Map<String, Object>> data = jdbcTemplate.queryForList(sql);
        model.addAttribute("tableName", tableName);
        model.addAttribute("tableData", data);
        return "userInterface/table-data";
    }
    @GetMapping("/tables/reports")
    public String showReports(Model model) {
        String sumSql = "SELECT sum(Sum_Expense) as SUMMA FROM Expense_Accounting_Log";
        List<Map<String, Object>> sumData = jdbcTemplate.queryForList(sumSql);
        model.addAttribute("sumData", sumData);
        String countSql = "SELECT m.Name_Product AS Material_Name, " +
                "COALESCE(SUM(mrl.Count_material), 0) AS Total_Received, " +
                "COALESCE(SUM(rs.Count), 0) AS Total_Remaining, " +
                "COALESCE(SUM(mrl.Count_material), 0) - COALESCE(SUM(rs.Count), 0) AS Total_Spent " +
                "FROM Material m " +
                "JOIN Materials_Receipt_Log mrl ON m.Name_Product = mrl.Name_Material " +
                "JOIN Remains_Storage rs ON m.ID_Product = rs.ID_Product " +
                "GROUP BY m.Name_Product";
        List<Map<String, Object>> countData = jdbcTemplate.queryForList(countSql);
        model.addAttribute("countData", countData);

        return "userInterface/reports";
    }
    @GetMapping("/about")
    public String about() {
        return "userInterface/about";
    }

}