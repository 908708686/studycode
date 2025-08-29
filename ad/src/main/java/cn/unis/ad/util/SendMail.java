package cn.unis.ad.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@Component
public class SendMail {
    private final static Logger log = LoggerFactory.getLogger(SendMail.class);
    //smtp服务器
    @Value("${spring.mail.smtp}")
    private String smtp;
    // 发件人电子邮箱
    @Value("${spring.mail.from}")
    private String from;
    @Value("${spring.mail.password}")
    private String password;

    public boolean SendExpireMai(String to, String mailmsg) {
        // 获取系统属性
        Properties properties = System.getProperties();

        // 设置邮件服务器
        properties.setProperty("mail.smtp.host", smtp);

        // 如果需要SMTP认证，则启用以下两行
        // 注意，对于Gmail，你需要将 "smtp.example.com" 替换为 "smtp.gmail.com"，
        // 并且需要设置 "mail.smtp.port" 属性为 "587"（对于TLS）或 "465"（对于SSL）。
        // 你可能还需要设置 "mail.smtp.auth" 为 "true"。
        properties.put("mail.smtp.auth", "true");

        // 如果需要SSL连接，则启用以下行
        // properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        // properties.put("mail.smtp.socketFactory.port", "465");

        // 如果使用Gmail，你可能需要设置以下属性
        // properties.put("mail.smtp.starttls.enable", "true"); // 启用TLS

        // 获取默认session对象
        Session session = Session.getDefaultInstance(properties,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(from, password);
                    }
                });

        try {
            // 创建默认的 MimeMessage 对象
            MimeMessage message = new MimeMessage(session);

            // Set From: 头部头字段
            message.setFrom(new InternetAddress(from));

            // Set To: 头部头字段
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set Subject: 头部头字段
            message.setSubject("域控邮件通知");

            // 现在设置实际消息
            message.setContent(mailmsg, "text/html; charset=utf-8");

            // 发送消息
            Transport.send(message);
            log.info("邮件发送成功...");

        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
        return true;
    }
}
