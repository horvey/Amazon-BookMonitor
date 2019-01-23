package horvey.tools;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * 抓取亚马逊数据进行分析的工具类
 */
public class Spider {
    private URL url;
    private String rawData;
    private Map<String, Price> destData;

    public Spider() throws MalformedURLException {
        this.destData = new HashMap<>();
        this.url = new URL("https://www.amazon.cn/gp/bestsellers/digital-text");
    }

    /**
     * 返回处理好的数据
     *
     * @return 处理好的数据
     */
    public Map<String, Price> getData() throws IOException {
        this.connect();
        this.getRawData();
        this.parseData();
        return destData;
    }

    /**
     * 建立与网站的连接，用于获取数据
     *
     * @return 连接对象
     */
    private URLConnection connect() throws IOException {
        //打开一个连接
        URLConnection connection = this.url.openConnection();
        //设置请求头，防止被503
        connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        connection.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
        connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9");
        connection.setRequestProperty("Host", "www.amazon.cn");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36");
        //发起连接
        connection.connect();
        return connection;
    }

    /**
     * 获取原始数据数据
     */
    private void getRawData() throws IOException {
        byte[] tmp = new byte[4096];
        int len;
        System.out.println("建立连接");
        URLConnection connection = this.connect();
        System.out.println("建立连接成功");
        //获取数据,因为服务器发过来的数据经过GZIP压缩，要用对应的流进行读取
        BufferedInputStream br = new BufferedInputStream(new GZIPInputStream(connection.getInputStream()));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //将获取的每一行数据存入sb中
        System.out.println("开始读取");
        while ((len = br.read(tmp)) != -1) {
            baos.write(tmp, 0, len);
        }
        System.out.println("读取结束");
        this.rawData = new String(baos.toByteArray(), "utf8");
        br.close();
    }

    /**
     * 用正则表达式解析并处理数据，提取出有用的信息并保存
     */
    private void parseData() {
        //先用正则表达式去取单个li标签
        Pattern p1 = Pattern.compile("<li class=\"zg-item-immersion\"[\\s\\S]+?</li>");
        Matcher m1 = p1.matcher(this.rawData == null ? "" : this.rawData);
        while (m1.find()) {
            //取出单个li标签的名字和价格
            Pattern p2 = Pattern.compile("alt=\"([\\u4E00-\\u9FA5：—，0-9a-zA-Z]+)[\\s\\S]+?￥(\\d{1,2}\\.\\d{2})");
            Matcher m2 = p2.matcher(m1.group());
            while (m2.find()) {
                //先取出名字
                String name = m2.group(1);
                //再取出价格
                double price = Double.parseDouble(m2.group(2));
                //若有相同名字的书籍只记录价格低的
                if (this.destData.containsKey(name)) {
                    double oldPrice = this.destData.get(name).getPrice();
                    price = oldPrice > price ? price : oldPrice;
                }
                //将数据放入Map中
                this.destData.put(name, new Price(price, new Date()));
            }
        }
    }
}
