package cn.unis.check.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Date;
@Component
public class DelBak {
    private final static Logger logger = LoggerFactory.getLogger(CheckBak.class);
    public void del(JSONArray delListArray) {
        if (delListArray == null) {
            logger.info("删除列表为空");
            return;
        }

        logger.info("开始执行文件删除任务，共 " + delListArray.size() + " 个目录");

        for (int i = 0; i < delListArray.size(); i++) {
            try {
                JSONObject item = delListArray.getJSONObject(i);
                String path = item.getString("path");
                int days = item.getIntValue("day");

                logger.info("处理目录: " + path + ", 删除 " + days + " 天前的文件");

                deleteOldFiles(path, days);
            } catch (Exception e) {
                logger.info("处理删除任务时出错: " + e.getMessage());
                System.err.println("处理删除任务时出错: " + e.getMessage());
            }
        }

        logger.info("文件删除任务执行完成");
    }

    /**
     * 删除指定目录下指定天数前的文件
     * @param directoryPath 目录路径
     * @param days 天数
     */
    private void deleteOldFiles(String directoryPath, int days) {
        File directory = new File(directoryPath);

        // 检查目录是否存在
        if (!directory.exists()) {
            logger.info("目录不存在: " + directoryPath);
            System.out.println("目录不存在: " + directoryPath);
            return;
        }

        if (!directory.isDirectory()) {
            logger.info("指定路径不是目录: " + directoryPath);
            System.out.println("指定路径不是目录: " + directoryPath);
            return;
        }

        // 计算截止时间（当前时间减去指定天数）
        long cutoffTime = System.currentTimeMillis() - (long) days * 24 * 60 * 60 * 1000;
        Date cutoffDate = new Date(cutoffTime);

        logger.info("删除 " + directoryPath + " 目录下早于 " + cutoffDate + " 的文件");
        System.out.println("删除 " + directoryPath + " 目录下早于 " + cutoffDate + " 的文件");

        // 删除旧文件
        int deletedCount = deleteFilesRecursive(directory, cutoffTime);

        logger.info("目录 " + directoryPath + " 处理完成，共删除 " + deletedCount + " 个文件");
        System.out.println("目录 " + directoryPath + " 处理完成，共删除 " + deletedCount + " 个文件");
    }

    /**
     * 递归删除指定时间之前的文件
     * @param directory 目录
     * @param cutoffTime 截止时间戳
     * @return 删除的文件数量
     */
    private int deleteFilesRecursive(File directory, long cutoffTime) {
        int deletedCount = 0;

        File[] files = directory.listFiles();
        if (files == null) {
            return deletedCount;
        }

        for (File file : files) {
            if (file.isFile()) {
                // 检查文件最后修改时间
                if (file.lastModified() < cutoffTime) {
                    // 删除文件
                    String filePath = file.getAbsolutePath();
                    if (file.delete()) {
                        deletedCount++;
                        logger.info("已删除文件: " + filePath + " (修改时间: " + new Date(file.lastModified()) + ")");
                        System.out.println("已删除文件: " + filePath);
                    } else {
                        logger.info("删除文件失败: " + filePath);
                        System.out.println("删除文件失败: " + filePath);
                    }
                }
            } else if (file.isDirectory()) {
                // 递归处理子目录
                deletedCount += deleteFilesRecursive(file, cutoffTime);

                // 检查子目录是否为空，如果为空则删除目录
                if (file.listFiles().length == 0) {
                    String dirPath = file.getAbsolutePath();
                    if (file.delete()) {
                        logger.info("已删除空目录: " + dirPath);
                        System.out.println("已删除空目录: " + dirPath);
                    }
                }
            }
        }

        return deletedCount;
    }
}
