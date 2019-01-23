package horvey;

import horvey.tools.MailSender;
import horvey.tools.Price;
import horvey.tools.Spider;

import java.io.FileReader;
import java.util.*;

public class Task extends TimerTask {
    private Map<String, Price> cache;
    private Properties p;

    public Task(){
        this.cache = new HashMap<>();
        this.p = new Properties();
    }

    @Override
    public void run() {
        //如果缓存中数据过多则清理
        if (this.cache.size() > 200) {
            this.cache.clear();
        }
        System.out.println("开始爬取！当前时间：" + new Date());
        try {
            //读取配置文件
            this.p.load(new FileReader("setting.conf"));
            //获得价格的监控预设值
            double destPrice = Double.parseDouble(p.getProperty("LowerThan"));
            //获得有效时间
            double ValidTime = Double.parseDouble(p.getProperty("ValidTime"));

            //分析并处理数据
            Map<String, Price> data = new Spider().getData();
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Price> entry : data.entrySet()) {
                String name = entry.getKey();
                Price price = entry.getValue();
                //当前价格小于预设值时
                if (price.getPrice() <= destPrice) {
                    //初始化判断条件
                    boolean isOutDate = true;
                    boolean isLower = false;
                    if (this.cache.containsKey(name)) {
                        isOutDate = this.cache.get(name).getDate().before(new Date((long) (System.currentTimeMillis() - 1000 * 60 * 60 * 24 * ValidTime)));
                        isLower = price.getPrice() < cache.get(name).getPrice();
                    }
                    //如果缓存中有该记录、添加时间不超过有效期且价格没有降低则跳过
                    if (this.cache.containsKey(name) && !isOutDate && !isLower) {
                        continue;
                    }
                    //将数据加入缓存，用于判断是否已经通知过
                    cache.put(name, price);
                    String tmp = name + "," + price.getPrice() + "元";
                    sb.append(tmp).append("<br />");
                }
            }
            //如果没有数据则不发送邮件通知
            if (sb.length() > 0) {
                //System.out.println(sb.toString());
                new MailSender().Send("图书优惠提醒", sb.toString());
                System.out.println("邮件发送成功!当前时间：" + new Date());
            }
            System.out.println("爬取结束！");
            System.out.println();
        } catch (NumberFormatException e) {
            System.err.println("配置文件格式不正确！");
            System.exit(-1);
        } catch (Exception e) {
            System.err.println(new Date());
            e.printStackTrace();
            System.err.println();
            new MailSender().Send("异常警告", "爬虫出现异常！");
            System.exit(-1);
        }
    }
}
