package pack_work;

public class Day {
    private String date;
    private String ticker;
    private double open;
    private double high;
    private double low;
    private double close;
    public Day(String date, String ticker, double open, double high, double low,  double close) {
        this.date = date;
        this.ticker = ticker;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }
    public String getDate() {
        return this.date;
    }
    public void setDate(String number) {
        this.date = number;
    }
    public String getTicker() {
        return ticker;
    }
    public void setTicker(String ticker) {
        this.ticker = ticker;
    }
    public double getOpen() {
        return open;
    }
    public void setOpen(double open) {
        this.open = open;
    }
    public double getHigh() {
        return high;
    }
    public void setHigh(double high) {
        this.high = high;
    }
    public double getLow() {
        return low;
    }
    public void setLow(double low) {
        this.low = low;
    }
    public double getClose() {
        return close;
    }
    public void setClose(double close) {
        this.close = close;
    }
}
