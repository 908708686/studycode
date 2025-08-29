package cn.unis.ad.service.impl;

import cn.hutool.http.HttpRequest;
import cn.unis.ad.controller.CheckCodeController;
import cn.unis.ad.service.DeptService;
import cn.unis.ad.util.GetToken;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeptServiceImpl implements DeptService {
    @Resource
    JdbcTemplate jdbcTemplate;
    @Resource
    GetToken getToken;
    public List<Map<String, Object>> GetDeptList() {
        String sql = "select co,dept,name,ou from dept order by CAST(co as REAL),CAST(dept as REAL);";
        return jdbcTemplate.queryForList(sql);
    }
    public String updateDept(String co, String dept, String ou) {
        String sql = "update dept set ou=? where co=? and dept=?;";
        int ret = jdbcTemplate.update(sql, ou, co, dept);
        return ret + "";
    }
}

