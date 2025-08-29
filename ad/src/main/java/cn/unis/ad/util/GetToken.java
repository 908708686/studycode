package cn.unis.ad.util;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import com.github.benmanes.caffeine.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Component
public class GetToken {
    private final static Logger log = LoggerFactory.getLogger(GetToken.class);
    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private Cache<String, Object> caffeineCache;

    public String generateAccessToken(String co) {
        String accesstoken = null; // 初始化为 null
        String corpsecret = null;
        String corpid = null;
        String sql = "select * from wecom where co=" + co;
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
        for (Map<String, Object> row : results) {
            corpid = row.get("corpid").toString();
            corpsecret = row.get("corpsecret").toString();
            log.info(corpid);
            log.info(corpsecret);
        }
        try {
            String access_token = "";
            JSONObject params = new JSONObject();
            params.put("corpid", corpid);
            params.put("corpsecret", corpsecret);
            String result = HttpRequest.get("https://qyapi.weixin.qq.com/cgi-bin/gettoken?").form(params).execute().body();
            JSONObject jsonObject = JSONObject.parseObject(result);
            accesstoken = jsonObject.getString("access_token");
            caffeineCache.put(String.valueOf(co), accesstoken);
            log.info(access_token);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return accesstoken;
    }

    public String getAccessTokenCache(String co) {
        if (caffeineCache.getIfPresent(co) != null) {
            return caffeineCache.getIfPresent(co).toString();
        }else {
            return generateAccessToken(co);
        }
    }
}
