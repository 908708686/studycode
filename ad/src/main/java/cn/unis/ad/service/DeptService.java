package cn.unis.ad.service;

import java.util.List;
import java.util.Map;

public interface DeptService {
    public List<Map<String, Object>> GetDeptList();
    public String updateDept(String co, String dept, String ou);
}
