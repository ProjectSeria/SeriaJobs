package su.nightexpress.excellenteconomy.api.currency;

public interface ExcellentCurrency {
    String getId();
    String format(double amount);
    boolean isDecimal();
}
