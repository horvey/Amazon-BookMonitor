package horvey.tools;

import com.sun.mail.util.MailSSLSocketFactory;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Properties;

public class MailSender {
    private Properties userSetting = new Properties();
    private Properties systemSetting = new Properties();

    public MailSender() {
        this.loadSetting();
        this.mailInit();
    }

    /**
     * 对外的发送方法
     * @param tittle    邮件标题
     * @param content   邮件内容
     * @return          发送结果
     */
    public void Send(String tittle, String content) {
        //1、创建session
        Session session = Session.getInstance(this.systemSetting);
        //开启Session的debug模式，这样就可以查看到程序发送Email的运行状态，可通过配置文件开启或关闭
        session.setDebug(!(this.userSetting.getProperty("Debug").equals("0")));
        //2、通过session得到transport对象
        Transport ts = null;
        try {
            ts = session.getTransport();
            //3、使用邮箱的用户名和密码连上邮件服务器，发送邮件时，发件人需要提交邮箱的用户名和密码给smtp服务器，用户名和密码都通过验证之后才能够正常发送邮件给收件人。
            ts.connect(this.userSetting.getProperty("SmtpHost"), this.userSetting.getProperty("Sender"), this.userSetting.getProperty("AuthCode"));
            //4、创建邮件
            Message message = this.setMailContent(session, tittle, content);
            //5、发送邮件
            ts.sendMessage(message, message.getAllRecipients());
        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("邮件配置不正确");
        } finally {
            try {
                if (ts != null) {
                    ts.close();
                }
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 读取用户配置信息
     */
    private void loadSetting() {
        try {
            this.userSetting.load(new FileReader("setting.conf"));
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("找不到配置文件");
        }
    }

    /**
     * 初始化邮件设置，为发送做准备
     */
    private void mailInit() {
        this.systemSetting.setProperty("mail.host", this.userSetting.getProperty("SmtpHost"));
        this.systemSetting.setProperty("mail.transport.protocol", "smtp");
        this.systemSetting.setProperty("mail.smtp.auth", "true");
        try {
            // 开启SSL加密，否则会失败
            MailSSLSocketFactory sf;
            sf = new MailSSLSocketFactory();
            sf.setTrustAllHosts(true);
            systemSetting.setProperty("mail.smtp.ssl.enable", "true");
            systemSetting.put("mail.smtp.ssl.socketFactory", sf);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置邮件内容
     * @param session   邮件服务器连接对象
     * @param title     邮件标题
     * @param content   邮件内容
     * @return          邮件对象
     */
    private MimeMessage setMailContent(Session session, String title, String content) {
        //创建邮件对象
        MimeMessage message = new MimeMessage(session);
        //设置发件人别名
        String nick = this.userSetting.getProperty("SenderNickName");
        //指明邮件的发件人
        try {
            if (null == nick) {
                message.setFrom(this.userSetting.getProperty("Sender"));
            } else {
                message.setFrom(nick + "<" + this.userSetting.getProperty("Sender") + ">");
            }
            //指明邮件的收件人，现在发件人和收件人是一样的，那就是自己给自己发
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(this.userSetting.getProperty("Receiver")));
            //邮件的标题
            message.setSubject(title);
            //邮件的文本内容
            message.setContent(content, "text/html;charset=utf-8");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        //返回创建好的邮件对象
        return message;
    }
}