package cn.unis.ad.task;

import cn.hutool.http.HttpRequest;
import cn.unis.ad.util.CmdUtil;
import cn.unis.ad.util.CommonUtil;
import cn.unis.ad.util.GetToken;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class AddAdTask extends QuartzJobBean {

    private final static Logger log = LoggerFactory.getLogger(AddAdTask.class);
    private static final ReentrantLock lock = new ReentrantLock();
    @Resource
    JdbcTemplate jdbcTemplate;
    @Resource
    GetToken getToken;
    @Resource
    CommonUtil commonUtil;
    @Resource
    CmdUtil cmdUtil;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        // 打印任务运行信息
        try {
            lock.lock();
            String co = context.getMergedJobDataMap().getString("param");
            addAd(co);
        } finally {
            lock.unlock();
        }

    }

    public void addAd(String co) {
        log.info("AddAdTask --------------------------------------------------------------------------------------start");
        log.info("context.getMergedJobDataMap().getString-------------------------------paramKey:" + co);
        String sql = "select dept,ou from dept where ou is not null and co='" + co + "'";
        log.info("sql:" + sql);
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
        for (Map<String, Object> row : results) {
            String dept = (String) row.get("dept");
            String ou = (String) row.get("ou");

            if (dept != null && ou != null) {
                List<Map<String, String>> userList= getWecomUserList(co, dept);
                for (Map<String, String> user : userList) {
                    String samid = checkAdAndWecomRelation(co, user.get("userid"));
                    log.info("samid:" + samid);
                    if (commonUtil.checkAdExist(samid)) {
                        log.info("用户" + samid + "已存在，不能重新创建");
                    } else {
                        addadUser(co, samid, ou, user.get("name"));
                    }
                }
            } else {
                log.warn("dept 或 ou 为空，跳过处理。dept: " + dept + ", ou: " + ou);
            }
        }
        log.info("AddAdTask --------------------------------------------------------------------------------------end");
    }

    public List<Map<String, String>> getWecomUserList(String co, String dept) {
        Map<String, Object> params = new HashMap<>();
        List<Map<String, String>> userListMap = new ArrayList<>();
        String access_token = getToken.getAccessTokenCache(String.valueOf(co));
        params.put("access_token", access_token);
        params.put("department_id", dept);
        String result = HttpRequest.get("https://qyapi.weixin.qq.com/cgi-bin/user/simplelist").form(params).execute().body();
        JSONObject jsonObject = JSONObject.parseObject(result);
        JSONArray userArray = jsonObject.getJSONArray("userlist");
        for (int i = 0; i < userArray.size(); i++) {
            Map<String, String> userInfo = new HashMap<>();
            JSONObject user = userArray.getJSONObject(i);
            String userid = user.getString("userid");
            String name = user.getString("name");
            userInfo.put("userid",userid);
            userInfo.put("name",name);
            userListMap.add(userInfo);
        }
        log.info(userListMap.toString());
        return userListMap;
    }

    public String checkAdAndWecomRelation(String co, String userid) {
        String sql = "select ad from relation where co='" + co + "' and userid='" + userid + "'";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
        String adname = null;
        if (results.size() > 0) {
            for (Map<String, Object> row : results) {
                adname = row.get("ad").toString();
            }
        } else {
            adname = userid;
        }
        return adname;
    }

    public boolean addadUser(String co, String adname, String ou, String name) {
        String sql = "select domain,defaultpwd from wecom where co=" + co;
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
        String domain = results.get(0).get("domain").toString();
        String defaultpwd = results.get(0).get("defaultpwd").toString();
        String cmd = "dsadd user CN=" + adname + "," + ou + " -upn " + adname + domain + " -pwd " + defaultpwd +" -desc " + name ;
        List<String> relist = cmdUtil.execCmd(cmd);
        if (relist.size() > 0) {
            log.info(relist.get(0));
        } else {
            log.info("域账号" + adname + "新建失败");
        }
        return true;
    }
}
