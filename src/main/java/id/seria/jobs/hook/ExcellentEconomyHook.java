package id.seria.jobs.hook;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Collection;

public class ExcellentEconomyHook {

    private Object api;
    private Method depositMethod;
    private Method withdrawMethod;
    private Method getBalanceMethod;
    private Method hasCurrencyMethod;
    private Method getCurrencyMethod;
    private Method getCurrenciesMethod;

    // Currency methods
    private Method currencyGetIdMethod;
    private Method currencyGetNameMethod;
    private Method currencyGetSymbolMethod;
    private Method currencyFormatMethod;
    private Method currencyIsPrimaryMethod;

    public boolean setup() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("ExcellentEconomy");
        if (plugin == null || !plugin.isEnabled()) {
            plugin = Bukkit.getPluginManager().getPlugin("CoinsEngine");
        }
        if (plugin == null || !plugin.isEnabled()) {
            return false;
        }

        try {
            // First try via getAPI() on the plugin instance
            try {
                Method getApiMethod = plugin.getClass().getMethod("getAPI");
                this.api = getApiMethod.invoke(plugin);
            } catch (Throwable ignored) {}

            // Fallback via Bukkit ServicesManager
            if (this.api == null) {
                String[] apiClassNames = {
                        "su.nightexpress.excellenteconomy.api.ExcellentEconomyAPI",
                        "su.nightexpress.coinsengine.api.CoinsEngineAPI"
                };
                for (String className : apiClassNames) {
                    try {
                        Class<?> apiClass = Class.forName(className);
                        RegisteredServiceProvider<?> rsp = Bukkit.getServicesManager().getRegistration(apiClass);
                        if (rsp != null) {
                            this.api = rsp.getProvider();
                            break;
                        }
                    } catch (ClassNotFoundException ignored) {}
                }
            }

            if (this.api == null) {
                return false;
            }

            Class<?> apiClass = this.api.getClass();

            // Cache API methods
            for (Method m : apiClass.getMethods()) {
                String name = m.getName();
                Class<?>[] params = m.getParameterTypes();
                if (name.equals("deposit") && params.length >= 3 && params[0] == Player.class && params[1] == String.class && params[2] == double.class) {
                    this.depositMethod = m;
                } else if (name.equals("withdraw") && params.length >= 3 && params[0] == Player.class && params[1] == String.class && params[2] == double.class) {
                    this.withdrawMethod = m;
                } else if (name.equals("getBalance") && params.length == 2 && params[0] == Player.class && params[1] == String.class) {
                    this.getBalanceMethod = m;
                } else if (name.equals("hasCurrency") && params.length == 1 && params[0] == String.class) {
                    this.hasCurrencyMethod = m;
                } else if (name.equals("getCurrency") && params.length == 1 && params[0] == String.class) {
                    this.getCurrencyMethod = m;
                } else if (name.equals("getCurrencies") && params.length == 0) {
                    this.getCurrenciesMethod = m;
                }
            }

            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public boolean isLoaded() {
        if (this.api == null) {
            setup();
        }
        return this.api != null;
    }

    public Object getApi() {
        return api;
    }

    public boolean hasCurrency(@NotNull String currencyId) {
        if (!isLoaded() || hasCurrencyMethod == null) return false;
        try {
            return (boolean) hasCurrencyMethod.invoke(api, currencyId);
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Nullable
    public Object getCurrency(@NotNull String currencyId) {
        if (!isLoaded() || getCurrencyMethod == null) return null;
        try {
            return getCurrencyMethod.invoke(api, currencyId);
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Nullable
    public Object getDefaultCurrency() {
        if (!isLoaded() || getCurrenciesMethod == null) return null;
        try {
            Object result = getCurrenciesMethod.invoke(api);
            if (result instanceof Collection<?> col) {
                for (Object curr : col) {
                    if (isCurrencyPrimary(curr)) {
                        return curr;
                    }
                }
                if (!col.isEmpty()) {
                    return col.iterator().next();
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    public String getDefaultCurrencyId() {
        Object def = getDefaultCurrency();
        if (def != null) {
            String id = getCurrencyId(def);
            if (id != null) return id;
        }
        return "coins";
    }

    private boolean isCurrencyPrimary(Object currency) {
        if (currency == null) return false;
        try {
            if (currencyIsPrimaryMethod == null) {
                currencyIsPrimaryMethod = currency.getClass().getMethod("isPrimary");
            }
            return (boolean) currencyIsPrimaryMethod.invoke(currency);
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Nullable
    public String getCurrencyId(Object currency) {
        if (currency == null) return null;
        try {
            if (currencyGetIdMethod == null) {
                currencyGetIdMethod = currency.getClass().getMethod("getId");
            }
            return (String) currencyGetIdMethod.invoke(currency);
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Nullable
    public String getCurrencyName(Object currency) {
        if (currency == null) return null;
        try {
            if (currencyGetNameMethod == null) {
                currencyGetNameMethod = currency.getClass().getMethod("getName");
            }
            return (String) currencyGetNameMethod.invoke(currency);
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Nullable
    public String getCurrencySymbol(Object currency) {
        if (currency == null) return null;
        try {
            if (currencyGetSymbolMethod == null) {
                currencyGetSymbolMethod = currency.getClass().getMethod("getSymbol");
            }
            return (String) currencyGetSymbolMethod.invoke(currency);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public boolean deposit(@NotNull Player player, @Nullable String currencyId, double amount) {
        if (!isLoaded() || amount <= 0) return false;
        String targetId = currencyId != null && !currencyId.isBlank() ? currencyId : getDefaultCurrencyId();
        try {
            if (depositMethod != null) {
                Object res = depositMethod.invoke(api, player, targetId, amount);
                if (res instanceof Boolean b) return b;
            }
        } catch (Throwable ignored) {}
        return false;
    }

    public boolean withdraw(@NotNull Player player, @Nullable String currencyId, double amount) {
        if (!isLoaded() || amount <= 0) return false;
        String targetId = currencyId != null && !currencyId.isBlank() ? currencyId : getDefaultCurrencyId();
        try {
            if (withdrawMethod != null) {
                Object res = withdrawMethod.invoke(api, player, targetId, amount);
                if (res instanceof Boolean b) return b;
            }
        } catch (Throwable ignored) {}
        return false;
    }

    public double getBalance(@NotNull Player player, @Nullable String currencyId) {
        if (!isLoaded()) return 0.0;
        String targetId = currencyId != null && !currencyId.isBlank() ? currencyId : getDefaultCurrencyId();
        try {
            if (getBalanceMethod != null) {
                Object res = getBalanceMethod.invoke(api, player, targetId);
                if (res instanceof Number num) return num.doubleValue();
            }
        } catch (Throwable ignored) {}
        return 0.0;
    }

    public String format(@Nullable String currencyId, double amount) {
        if (!isLoaded()) return String.format("%.2f", amount);
        Object curr = currencyId != null ? getCurrency(currencyId) : getDefaultCurrency();
        if (curr != null) {
            try {
                if (currencyFormatMethod == null) {
                    currencyFormatMethod = curr.getClass().getMethod("format", double.class);
                }
                Object res = currencyFormatMethod.invoke(curr, amount);
                if (res != null) return res.toString();
            } catch (Throwable ignored) {}
        }
        String symbol = curr != null ? getCurrencySymbol(curr) : null;
        if (symbol != null && !symbol.isBlank()) {
            return String.format("%.2f %s", amount, symbol);
        }
        return String.format("%.2f", amount);
    }
}
