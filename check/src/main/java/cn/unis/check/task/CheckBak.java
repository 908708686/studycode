package cn.unis.check.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.util.Date;

@Component
public class CheckBak {
    private final static Logger logger = LoggerFactory.getLogger(CheckBak.class);

    public void check(JSONArray checkListArray, String proxyServer, String webhookUrl) {
        logger.info("开始执行文件检测任务，共 " + checkListArray.size() + " 个目录");
        String message = "";
        for (int i = 0; i < checkListArray.size(); i++) {
            JSONObject jsonObject = checkListArray.getJSONObject(i);
            String sys = jsonObject.getString("sys");
            String path = jsonObject.getString("path");
            int day = jsonObject.getInteger("day");
            Date lastModifiedTime = getLastModifiedTime(path);
            logger.info("时间: " + lastModifiedTime);
            if (lastModifiedTime == null) {
                logger.info("系统: " + sys + "系统从未份系统从未备份过文件，该系统要求的备份频率为" + day + "天一次");
                message = message + "系统: " + sys + "系统从未份系统从未备份过文件，该系统要求的备份频率为" + day + "天一次\n";
            } else if (lastModifiedTime.getTime() < System.currentTimeMillis() - day * 24 * 60 * 60 * 1000) {
                logger.info("系统: " + sys + "系统已经超过"
                        + ((System.currentTimeMillis() - lastModifiedTime.getTime()) / 24 / 60 / 60 / 1000)
                        + "天未备份,该系统要求的备份频率为" + day + "天一次，请及时备份\n");
                message = message + "系统: " + sys + "系统已经超过"
                        + ((System.currentTimeMillis() - lastModifiedTime.getTime()) / 24 / 60 / 60 / 1000)
                        + "天未备份,该系统要求的备份频率为" + day + "天一次，请及时备份\n";
            } else {
                logger.info("系统: " + sys + "系统已经备份，请勿重复备份");
            }
        }
        sendWeChatNotification(message, proxyServer, webhookUrl);
        logger.info("文件检测任务执行完成");
    }

    public Date getLastModifiedTime(String folderPath) {
        File folder = new File(folderPath);

        // 检查文件夹是否存在
        if (!folder.exists()) {
            System.err.println("文件夹不存在: " + folderPath);
            return null;
        }

        if (!folder.isDirectory()) {
            System.err.println("指定路径不是文件夹: " + folderPath);
            return null;
        }

        // 查找最后修改的文件
        long lastModified = findLastModifiedTime(folder);
        return lastModified > 0 ? new Date(lastModified) : null;
    }

    private long findLastModifiedTime(File folder) {
        long latestTime = 0;
        // 获取文件夹中的所有文件和子文件夹
        File[] files = folder.listFiles();
        if (files == null) {
            return latestTime;
        }
        for (File file : files) {
            if (file.isFile()) {
                // 对于文件，检查修改时间
                long fileModified = file.lastModified();
                if (fileModified > latestTime) {
                    latestTime = fileModified;
                }
            } else if (file.isDirectory()) {
                // 对于子文件夹，递归查找
                long subFolderTime = findLastModifiedTime(file);
                if (subFolderTime > latestTime) {
                    latestTime = subFolderTime;
                }
            }
        }
        return latestTime;
    }

    /**
     * 通过代理服务器发送企业微信通知
     *
     * @param message 消息内容
     */
    private void sendWeChatNotification(String message, String proxyServer, String webhookUrl) {
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            logger.info("Webhook URL未配置，无法发送通知");
            return;
        }

        try {
            // 构造企业微信消息格式
            JSONObject json = new JSONObject();
            json.put("msgtype", "markdown");

            JSONObject markdown = new JSONObject();
            markdown.put("content", message);
            json.put("markdown", markdown);

            // 发送消息
            boolean result = sendMessageViaProxy(json.toJSONString(), proxyServer, webhookUrl);
            if (result) {
                logger.info("企业微信通知发送成功");
            } else {
                logger.info("企业微信通知发送失败");
            }
        } catch (Exception e) {
            logger.info("发送企业微信通知时出错: " + e.getMessage());
        }
    }

    /**
     * 通过代理服务器发送HTTP POST请求
     *
     * @param jsonContent JSON内容
     * @return 发送是否成功
     */
    private boolean sendMessageViaProxy(String jsonContent, String proxyServer, String webhookUrl) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(webhookUrl);

            // 创建代理
            Proxy proxy = createProxy(proxyServer);

            // 建立连接
            connection = (HttpURLConnection) url.openConnection(proxy);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000); // 10秒连接超时
            connection.setReadTimeout(10000);    // 10秒读取超时

            // 发送消息内容
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonContent.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // 读取响应
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                }

                // 解析响应结果
                JSONObject result = JSONObject.parseObject(response.toString());
                int errorCode = result.getIntValue("errcode");

                return errorCode == 0;
            } else {
                logger.info("HTTP请求失败，状态码: " + responseCode);
                return false;
            }
        } catch (Exception e) {
            logger.info("发送消息时发生异常: " + e.getMessage());
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 解析代理服务器地址并创建Proxy对象
     *
     * @param proxyServer 代理服务器地址 (如: http://10.37.60.60:8888)
     * @return Proxy对象
     */
    private Proxy createProxy(String proxyServer) {
        if (proxyServer == null || proxyServer.isEmpty()) {
            return Proxy.NO_PROXY;
        }

        try {
            // 解析代理服务器地址
            URI proxyUri = URI.create(proxyServer.trim());
            String host = proxyUri.getHost();
            int port = proxyUri.getPort();

            if (host == null || port == -1) {
                logger.info("代理服务器地址格式错误: " + proxyServer);
                return Proxy.NO_PROXY;
            }

            InetSocketAddress proxyAddress = new InetSocketAddress(host, port);
            return new Proxy(Proxy.Type.HTTP, proxyAddress);
        } catch (Exception e) {
            logger.info("解析代理服务器地址失败: " + e.getMessage());
            return Proxy.NO_PROXY;
        }
    }
}
