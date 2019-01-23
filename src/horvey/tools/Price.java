package horvey.tools;

import java.util.Date;

/**
 * 价格与时间的聚合类
 */
public class Price {
    private double price;
    private Date date;

    public Price(double price, Date date) {
        this.price = price;
        this.date = date;
    }

    public double getPrice() {
        return price;
    }

    public Date getDate() {
        return date;
    }
}
