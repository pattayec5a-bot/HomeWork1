import java.util.Objects;

// неизменяемый класс: все поля final, операции возвращают новый объект
public final class Balance {

    private final long amount; // в копейках (у долларов и евро в центах)
    private final Currency currency;

    public Balance(final long kopecks) {
        this(kopecks, Currency.RUB);
    }

    public Balance(final long amount, final Currency currency) {
        if (currency == null) {
            throw new IllegalArgumentException("Валюта не может быть null");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("Баланс не может быть отрицательным: " + amount);
        }
        this.amount = amount;
        this.currency = currency;
    }

    public long getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public boolean isZero() {
        return amount == 0;
    }

    public Balance add(final long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Нельзя прибавить отрицательную сумму: " + value);
        }
        // addExact кинет ArithmeticException при переполнении long
        return new Balance(Math.addExact(amount, value), currency);
    }

    public Balance add(final Balance other) {
        checkSameCurrency(other);
        return add(other.amount);
    }

    public Balance subtract(final long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Нельзя вычесть отрицательную сумму: " + value);
        }
        if (value > amount) {
            throw new IllegalArgumentException("Недостаточно средств: есть " + this
                    + ", а нужно " + new Balance(value, currency));
        }
        return new Balance(amount - value, currency);
    }

    public Balance subtract(final Balance other) {
        checkSameCurrency(other);
        return subtract(other.amount);
    }

    public Balance convertTo(final Currency target) {
        if (target == null) {
            throw new IllegalArgumentException("Валюта для конвертации не может быть null");
        }
        if (target == currency) {
            return this;
        }
        // курс в Currency это цена 1 единицы валюты в копейках, а в единице 100 копеек/центов,
        // поэтому сотни сокращаются. остаток меньше копейки отбрасывается
        long converted = Math.multiplyExact(amount, currency.getPriceInRublesKopecks())
                / target.getPriceInRublesKopecks();
        return new Balance(converted, target);
    }

    private void checkSameCurrency(final Balance other) {
        if (other == null) {
            throw new IllegalArgumentException("Баланс не может быть null");
        }
        if (other.currency != currency) {
            throw new IllegalArgumentException("Валюты не совпадают: " + currency.getCode()
                    + " и " + other.currency.getCode() + ", сначала нужен convertTo");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Balance balance = (Balance) o;
        return amount == balance.amount && currency == balance.currency;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return String.format("%d.%02d %s", amount / 100, amount % 100, currency.getSymbol());
    }
}
