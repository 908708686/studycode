package cn.unis.ad.service.impl;


import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


@Service
public class DynamicTaskService {

    @Resource
    private JdbcTemplate jdbcTemplate;
//    public void init(){
//        List<Map<String, Object>> tasks = jdbcTemplate.queryForList("SELECT id, cron_expression, task_name, parameters FROM scheduled_tasks");
//        for (Map<String, Object> task : tasks) {
//            getTaskByName(task.get("task_name").toString(), parseParameters(task.get("parameters").toString()));
//        }
//    }



}
