package cn.unis.ad.service.impl;

import cn.unis.ad.service.RelationService;
import cn.unis.ad.task.AddAdTask;
import cn.unis.ad.util.GetToken;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RelationServiceImpl implements RelationService {
    private final static Logger log = LoggerFactory.getLogger(RelationServiceImpl.class);
    @Resource
    JdbcTemplate jdbcTemplate;
    public List<Map<String, Object>> GetRelationList() {
        String sql = "select co,ad,userid from relation order by co,ad;";
        return jdbcTemplate.queryForList(sql);
    }
    public String addRelation(String co, String ad, String userid) {
        String sql = "insert into relation (co,ad,userid)values(?,?,?);";
        int ret = jdbcTemplate.update(sql, co, ad, userid);
        return ret + "";
    }
    public String updateRelation(String co, String ad, String userid) {
        String sql = "update relation set ad='"+ad+"' where co="+co+" and userid='"+userid+"';";
        log.info("updateRelation:"+sql);
        int ret = jdbcTemplate.update(sql);
        return ret + "";
    }
    public String deleteRelation(String ad) {
        log.info("deleteRelation:"+ad);
        String sql = "delete from relation where ad='"+ad+"';";
        int ret = jdbcTemplate.update(sql);
        return ret + "";
    }
}

