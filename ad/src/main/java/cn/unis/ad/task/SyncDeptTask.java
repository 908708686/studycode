package cn.unis.ad.task;

import cn.hutool.http.HttpRequest;
import cn.unis.ad.util.GetToken;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
@Component
public class SyncDeptTask extends QuartzJobBean {
    private final static Logger log = LoggerFactory.getLogger(AddAdTask.class);
    private static final ReentrantLock lock = new ReentrantLock();
    @Resource
    GetToken getToken;
    @Resource
    JdbcTemplate jdbcTemplate;
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        // 打印任务运行信息
        try {
            lock.lock();
            String co = context.getMergedJobDataMap().getString("param");
            SyncDept(co);
        } finally {
            lock.unlock();
        }
    }
    public void SyncDept(String co){
        log.info("SyncDept--------------------------------------------------------------------------------------start");
        //  获取部门列表
        String access_token = getToken.getAccessTokenCache(String.valueOf(co));
        Map<String, Object> p = new HashMap<>();
        p.put("access_token", access_token);
        String result = HttpRequest.get("https://qyapi.weixin.qq.com/cgi-bin/department/simplelist").form(p).execute().body();
        JSONObject jsonObject = JSONObject.parseObject(result);
        JSONArray deptArray = jsonObject.getJSONArray("department_id");
        for (int i = 0; i < deptArray.size(); i++) {
            JSONObject deptObj = deptArray.getJSONObject(i);
            String dept = deptObj.getString("id");
            String sql = "INSERT OR IGNORE INTO dept (co,dept,name,ou) values( ?,?,'','');";
            jdbcTemplate.update(sql,co,dept);
        }
        // 遍历部门列表，获取部门详情
        String sql = "select dept from dept where co=?";
        List<Map<String, Object>> deptList = jdbcTemplate.queryForList(sql,co);
            for (Map<String, Object> deptmap : deptList) {
                String deptid = (String) deptmap.get("dept");
                p.put("access_token", access_token);
                p.put("id", deptid);
                result = HttpRequest.get("https://qyapi.weixin.qq.com/cgi-bin/department/get").form(p).execute().body();
                System.out.println("result:"+result);
                jsonObject = JSONObject.parseObject(result);
                String errcode = jsonObject.getString("errcode");
                System.out.println("errcode:"+errcode);
                if (errcode.equals("0")) {
                    log.info("deptid:" + deptid + "存在");
                    JSONObject deptObj = jsonObject.getJSONObject("department");
                    String name = deptObj.getString("name");
                    sql = "update dept set name=? where co=? and dept=?";
                    jdbcTemplate.update(sql, name, co, deptid);
                } else {
                    log.info("deptid:" + deptid + "不存在");
                    sql = "delete from dept where co=? and dept=?";
                    jdbcTemplate.update(sql, co, deptid);
                }
            }
        log.info("SyncDept----------------------------------------------------------------------------------------end");
    }
}
