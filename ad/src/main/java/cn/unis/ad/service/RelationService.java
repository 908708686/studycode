package cn.unis.ad.service;

import java.util.List;
import java.util.Map;

public interface RelationService {
    public List<Map<String, Object>> GetRelationList();
    public String addRelation(String co, String dept, String ou);
    public String updateRelation(String co, String dept, String ou);
    public String deleteRelation(String ad);
}
