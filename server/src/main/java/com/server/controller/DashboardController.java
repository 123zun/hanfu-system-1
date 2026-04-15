package com.server.controller;

import com.server.activity.service.ActivityService;
import com.server.article.service.ArticleService;
import com.server.common.R;
import com.server.user.service.UserService;
import com.server.work.service.WorkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private WorkService workService;

    /**
     * 获取社区统计数据（排除已删除的记录）
     */
    @GetMapping("/stats")
    public R<?> getStats() {
        try {
            Map<String, Object> stats = new HashMap<>();

            long userCount = userService.countActiveUsers();
            long activityCount = activityService.countActiveActivities();
            long articleCount = articleService.countActiveArticles();
            long workCount = workService.countActiveWorks();

            stats.put("userCount", userCount);
            stats.put("activityCount", activityCount);
            stats.put("articleCount", articleCount);
            stats.put("workCount", workCount);

            return R.success(stats);
        } catch (Exception e) {
            log.error("获取社区统计数据失败", e);
            return R.error("获取失败");
        }
    }
}
