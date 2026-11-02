public class BankAccount {

    private static int accountsCount = 0;

    private final int number;
    private final String owner;
    private Balance balance; // Balance неизменяемый, поэтому после операций просто заменяем объект
    private long withdrawLimit; // лимит на одно снятие или перевод
    private boolean blocked;
    private final StringBuilder history = new StringBuilder();

    public BankAccount(final String owner, final Currency currency, final long withdrawLimit) {
        if (owner == null || owner.isBlank()) {
            throw new IllegalArgumentException("Имя владельца не может быть пустым");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Валюта счёта не может быть null");
        }
        checkLimit(withdrawLimit);

        accountsCount++;
        this.number = accountsCount;
        this.owner = owner.trim();
        this.balance = new Balance(0, currency);
        this.withdrawLimit = withdrawLimit;
        this.blocked = false;
        addHistory("Счёт открыт");
    }

    public void deposit(final long amount) {
        checkNotBlocked();
        checkAmount(amount);
        balance = balance.add(amount);
        addHistory("Пополнение: +" + formatMoney(amount));
    }

    public void withdraw(final long amount) {
        checkNotBlocked();
        checkAmount(amount);
        checkWithdrawLimit(amount);
        balance = balance.subtract(amount);
        addHistory("Снятие: -" + formatMoney(amount));
    }

    // amount в валюте этого счёта, если у получателя другая валюта - конвертируем
    public void transfer(final BankAccount target, final long amount) {
        if (target == null) {
            throw new IllegalArgumentException("Счёт получателя не может быть null");
        }
        if (target == this) {
            throw new IllegalArgumentException("Нельзя перевести деньги на тот же счёт");
        }
        checkNotBlocked();
        if (target.isBlocked()) {
            throw new IllegalStateException("Счёт получателя №" + target.getNumber() + " заблокирован");
        }
        checkAmount(amount);
        checkWithdrawLimit(amount);

        Balance sent = new Balance(amount, balance.getCurrency());
        Balance received = sent.convertTo(target.getBalance().getCurrency());
        if (received.isZero()) {
            throw new IllegalArgumentException("Сумма " + sent
                    + " слишком маленькая: после конвертации получается " + received);
        }

        // сначала считаем оба баланса и только потом меняем, чтобы при ошибке ничего не поменялось
        Balance newBalance = balance.subtract(sent);
        Balance newTargetBalance = target.getBalance().add(received);
        balance = newBalance;
        target.balance = newTargetBalance; // private поле, но мы внутри класса BankAccount, поэтому можно

        addHistory("Перевод на счёт №" + target.getNumber() + ": -" + sent);
        target.addHistory("Перевод со счёта №" + number + ": +" + received);
    }

    public void block() {
        if (blocked) {
            throw new IllegalStateException("Счёт №" + number + " уже заблокирован");
        }
        blocked = true;
        addHistory("Счёт заблокирован");
    }

    public void unblock() {
        if (!blocked) {
            throw new IllegalStateException("Счёт №" + number + " не заблокирован");
        }
        blocked = false;
        addHistory("Счёт разблокирован");
    }

    public void setWithdrawLimit(final long newLimit) {
        checkLimit(newLimit);
        withdrawLimit = newLimit;
        addHistory("Новый лимит на списание: " + formatMoney(newLimit));
    }

    public Balance getBalanceIn(final Currency currency) {
        return balance.convertTo(currency);
    }

    public int getNumber() {
        return number;
    }

    public String getOwner() {
        return owner;
    }

    public Balance getBalance() {
        return balance;
    }

    public long getWithdrawLimit() {
        return withdrawLimit;
    }

    public boolean isBlocked() {
        return blocked;
    }

    // String неизменяемый, поэтому историю снаружи поменять нельзя
    public String getHistory() {
        return history.toString();
    }

    private void addHistory(final String text) {
        history.append("- ").append(text).append("\n");
    }

    private void checkNotBlocked() {
        if (blocked) {
            throw new IllegalStateException("Счёт №" + number + " заблокирован, операция невозможна");
        }
    }

    private void checkWithdrawLimit(final long amount) {
        if (amount > withdrawLimit) {
            throw new IllegalArgumentException("Сумма " + formatMoney(amount)
                    + " превышает лимит на списание " + formatMoney(withdrawLimit));
        }
    }

    private static void checkAmount(final long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Сумма должна быть больше нуля, а передано: " + amount);
        }
    }

    private static void checkLimit(final long limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Лимит должен быть больше нуля, а передано: " + limit);
        }
    }

    private String formatMoney(final long amount) {
        return new Balance(amount, balance.getCurrency()).toString();
    }

    @Override
    public String toString() {
        String status = "активен";
        if (blocked) {
            status = "заблокирован";
        }
        return "Счёт №" + number
                + " {владелец: " + owner
                + ", баланс: " + balance
                + ", лимит на списание: " + formatMoney(withdrawLimit)
                + ", " + status
                + "}";
    }
}
