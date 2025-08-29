package cn.unis.ad.util;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GetMailUtil {
    private final static Logger log = LoggerFactory.getLogger(GetMailUtil.class);
    @Autowired
    private  GetToken getToken;
    @Resource
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private CmdUtil cmdUtil;

    public String getMailAddress(String adname) {
        int co;
        String userid = null;
        List<String> cmdres = cmdUtil.execCmd("(Get-ADUser -Identity " + adname + " -Properties mail).mail");
        if (cmdres.size() > 0) {
            //如果域账号属性中有mail属性，则直接返回
            return cmdres.get(0);
        } else {
            //如果没有，则从企业微信中获取正确的邮箱。
            //从数据库查找是正确的userid
            String sql = "select userid,co from relation where ad='" + adname + "' limit 1";
            List<Map<String, Object>> sqlres = jdbcTemplate.queryForList(sql);
            if (sqlres.size() > 0) {
                //如果数据库中有userid，则直接返回userid和co
                userid = sqlres.get(0).get("userid").toString();
                co = Integer.parseInt(sqlres.get(0).get("co").toString());
                return getWecomBizMail(userid, co);
            } else {
                //如果没有则查找ad账号的ou，从dept表中获取co
                cmdres = cmdUtil.execCmd("dsquery user -samid " + adname);
                String originalString = cmdres.get(0);
                // 查找第一个逗号的位置
                int commaIndex = originalString.indexOf(',');
                String modifiedString = originalString.substring(commaIndex + 1);
                modifiedString = modifiedString.substring(0, modifiedString.length() - 1);
                //在数据库中根据ou查找企业微信的co
                String sql1 = "select co from dept where ou='" + modifiedString + "' limit 1";
                log.info("ad账号的ou为" + sql1);
                List<Map<String, Object>> sqlres1 = jdbcTemplate.queryForList(sql1);
                if (sqlres1.size() > 0) {
                    co = Integer.parseInt(sqlres1.get(0).get("co").toString());
                    return getWecomBizMail(adname, co);
                } else {
                    log.info("未找到该ou的co");
                    return null;
                }
            }
        }
    }

    private String getWecomBizMail(String userid, int co) {
        Map<String, Object> params = new HashMap<>();
        String access_token = getToken.getAccessTokenCache(String.valueOf(co));
        log.info("access_token:" + access_token);
        params.put("access_token", access_token);
        params.put("userid", userid);
        String reqres = HttpRequest.get("https://qyapi.weixin.qq.com/cgi-bin/user/get")
                .form(params)
                .execute()
                .body();
        JSONObject jsonObject = JSONObject.parseObject(reqres);
        String errcode = jsonObject.getString("errcode");
        if (errcode.equals("60111")) {
            return null;
        } else if (errcode.equals("0")) {
            log.info("userid:" + userid + "存在");
            return jsonObject.getString("biz_mail");
        } else {
            log.info("userid:" + userid + "其他报错，报错码为" + errcode);
            return null;
        }
    }
}
