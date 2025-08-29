package cn.unis.ad.util;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import cn.unis.ad.util.CmdUtil;

@Component
public class CommonUtil {
    @Resource
    private CmdUtil cmdUtil;

    // 判断AD用户是否存在
    public  boolean checkAdExist(String adname) {
        List<String> reslist = cmdUtil.execCmd("net user " + adname);
        if(reslist.size()==4) {
            return false;
        }else {
            return true;
        }
    }
    // 判断AD用户是否被启用
    public  boolean checkAdEnable(String adname){
        List<String> reslist = cmdUtil.execCmd("dsquery user -samid "+adname+" |dsget user  -disabled");
        if(reslist.get(1).contains("no")){
            return true;
        }else {
            return false;
        }
    }
}
