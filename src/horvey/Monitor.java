package horvey;

import java.io.*;
import java.util.Properties;
import java.util.Timer;

public class Monitor {
    public static void main(String[] args) {
        try {
            //设置异常流输出到文件，方便查看
            System.setErr(new PrintStream(new FileOutputStream("ErrLog.txt", true)));
            //设置系统输出流输出到文件，方便查看
            System.setOut(new PrintStream(new FileOutputStream("Log.txt", true)));

            //读取配置文件
            Properties p = new Properties();
            p.load(new FileReader("setting.conf"));
            double intervalTime = Double.parseDouble(p.getProperty("IntervalTime"));
            Timer t = new Timer();
            t.schedule(new Task(), 0, (long) (1000 * 60 * 60 * intervalTime));
        } catch (FileNotFoundException e) {
            System.err.println("找不到配置文件");
            System.exit(-1);
        } catch (NumberFormatException e) {
            System.err.println("配置文件格式不正确！");
            System.exit(-1);
        } catch (IOException e) {
            System.err.println("读取配置文件失败");
            System.exit(-1);
        }
    }
}