package su.nightexpress.excellenteconomy.api;

import org.bukkit.entity.Player;
import su.nightexpress.excellenteconomy.api.currency.ExcellentCurrency;
import java.util.Collection;

public interface ExcellentEconomyAPI {
    Collection<ExcellentCurrency> getCurrencies();
    boolean hasCurrency(String currencyId);
    ExcellentCurrency getCurrency(String currencyId);
    double getBalance(Player player, String currencyId);
    boolean deposit(Player player, String currencyId, double amount);
    boolean withdraw(Player player, String currencyId, double amount);
    boolean setBalance(Player player, String currencyId, double amount);
}
