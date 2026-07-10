package com.shrimpslip.app.user.controller;

import com.shrimpslip.common.core.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/statistics")
public class AdminController {

    @GetMapping("/agent")
    public Result<Map<String, Object>> agentStats() {
        return Result.ok(Map.of(
                "totalConversations", 1286,
                "todayConversations", 47,
                "avgResponseTime", 2.3,
                "topCategories", List.of("电子产品", "服装鞋帽", "食品饮料", "家居用品"),
                "categoryCounts", List.of(342, 256, 198, 165)
        ));
    }

    @GetMapping("/revenue")
    public Result<Map<String, Object>> revenueStats() {
        return Result.ok(Map.of(
                "todayRevenue", 12860.00,
                "weekRevenue", 89520.50,
                "monthRevenue", 356800.00,
                "totalRevenue", 1258600.00,
                "trend", List.of(
                        Map.of("date", "07/04", "amount", 18600),
                        Map.of("date", "07/05", "amount", 15200),
                        Map.of("date", "07/06", "amount", 21300),
                        Map.of("date", "07/07", "amount", 9800),
                        Map.of("date", "07/08", "amount", 17800),
                        Map.of("date", "07/09", "amount", 12400),
                        Map.of("date", "07/10", "amount", 12860)
                )
        ));
    }
}
