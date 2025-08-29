package cn.unis.ad.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component
public class CmdUtil {
    private final static Logger log = LoggerFactory.getLogger(CmdUtil.class);
    public List<String> execCmd(String command) {
        List<String> endstrs = new ArrayList<>();
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("powershell.exe", "-Command", command);
        processBuilder.redirectErrorStream(true);
        Process process = null;
        try {
            // 启动进程
            process = processBuilder.start();

            // 读取输出
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(),  "GBK"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug(line);
                    endstrs.add(line);
                }
            }
            // 等待进程完成并获取退出码
            int exitCode = process.waitFor();
            log.debug("\n命令执行完毕，退出码: " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            // 可根据需要抛出运行时异常或记录日志
        } finally {
            // 确保进程关闭
            if (process != null) {
                process.destroy();
                if (process.isAlive()) {
                    process.destroyForcibly();
                }
            }
        }
        return endstrs;
    }

}
