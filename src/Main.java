public class Main {

    public static void main(String[] args) {
        // все суммы в копейках: 1000_00 = 1000 рублей 00 копеек

        System.out.println("===== 1. Balance: add и subtract =====");
        Balance balance = new Balance(1000_00);
        System.out.println("Баланс: " + balance);
        System.out.println("add(250.50): " + balance.add(250_50));
        System.out.println("subtract(99.99): " + balance.subtract(99_99));
        System.out.println("add(5.25 ₽): " + balance.add(new Balance(5_25)));
        System.out.println("Сам баланс не изменился: " + balance);
        System.out.println("equals: " + balance.equals(new Balance(1000_00)));

        System.out.println();
        System.out.println("===== 2. Конвертация =====");
        Balance rubles = new Balance(9600_00);
        Balance dollars = new Balance(100_00, Currency.USD);
        System.out.println(rubles + " -> " + rubles.convertTo(Currency.USD));
        System.out.println(rubles + " -> " + rubles.convertTo(Currency.EUR));
        System.out.println(rubles + " -> " + rubles.convertTo(Currency.CNY));
        System.out.println(dollars + " -> " + dollars.convertTo(Currency.RUB));
        System.out.println(dollars + " -> " + dollars.convertTo(Currency.EUR));

        System.out.println();
        System.out.println("===== 3. Balance: ошибки =====");
        try {
            new Balance(-100);
        } catch (Exception e) {
            System.out.println("new Balance(-100): " + e.getMessage());
        }
        try {
            new Balance(100, null);
        } catch (Exception e) {
            System.out.println("new Balance(100, null): " + e.getMessage());
        }
        try {
            balance.add(-50);
        } catch (Exception e) {
            System.out.println("add(-50): " + e.getMessage());
        }
        try {
            balance.subtract(2000_00);
        } catch (Exception e) {
            System.out.println("subtract(2000.00): " + e.getMessage());
        }
        try {
            balance.add(dollars);
        } catch (Exception e) {
            System.out.println("рубли + доллары: " + e.getMessage());
        }
        try {
            new Balance(Long.MAX_VALUE).add(1);
        } catch (Exception e) {
            System.out.println("переполнение long: " + e.getMessage());
        }

        System.out.println();
        System.out.println("===== 4. Счета: пополнение и снятие =====");
        BankAccount ivan = new BankAccount("Иван Петров", Currency.RUB, 50_000_00);
        BankAccount anna = new BankAccount("Анна Смирнова", Currency.RUB, 30_000_00);
        BankAccount john = new BankAccount("John Smith", Currency.USD, 1_000_00);
        ivan.deposit(100_000_00);
        ivan.withdraw(5_000_00);
        anna.deposit(20_000_00);
        john.deposit(500_00);
        System.out.println(ivan);
        System.out.println(anna);
        System.out.println(john);
        System.out.println("Деньги Ивана в евро: " + ivan.getBalanceIn(Currency.EUR));

        System.out.println();
        System.out.println("===== 5. Переводы =====");
        ivan.transfer(anna, 10_000_00); // рубли -> рубли
        ivan.transfer(john, 9_600_00); // рубли -> доллары, John получит 100 $
        john.transfer(anna, 50_00); // доллары -> рубли, Анна получит 4800 ₽
        System.out.println(ivan);
        System.out.println(anna);
        System.out.println(john);

        System.out.println();
        System.out.println("===== 6. Счета: ошибки =====");
        try {
            ivan.deposit(0);
        } catch (Exception e) {
            System.out.println("пополнение на 0: " + e.getMessage());
        }
        try {
            ivan.withdraw(60_000_00);
        } catch (Exception e) {
            System.out.println("снятие больше лимита: " + e.getMessage());
        }
        try {
            john.withdraw(800_00);
        } catch (Exception e) {
            System.out.println("снятие больше, чем есть: " + e.getMessage());
        }
        try {
            ivan.transfer(ivan, 100_00);
        } catch (Exception e) {
            System.out.println("перевод самому себе: " + e.getMessage());
        }
        try {
            ivan.transfer(null, 100_00);
        } catch (Exception e) {
            System.out.println("перевод на null: " + e.getMessage());
        }
        try {
            ivan.transfer(john, 1);
        } catch (Exception e) {
            System.out.println("перевод 1 копейки в доллары: " + e.getMessage());
        }
        try {
            new BankAccount("   ", Currency.RUB, 100_00);
        } catch (Exception e) {
            System.out.println("пустое имя владельца: " + e.getMessage());
        }
        try {
            anna.setWithdrawLimit(-5);
        } catch (Exception e) {
            System.out.println("отрицательный лимит: " + e.getMessage());
        }
        System.out.println("Балансы после ошибок не изменились:");
        System.out.println(ivan);
        System.out.println(anna);
        System.out.println(john);

        System.out.println();
        System.out.println("===== 7. Блокировка счёта =====");
        anna.block();
        System.out.println(anna);
        try {
            anna.deposit(100_00);
        } catch (Exception e) {
            System.out.println("пополнение заблокированного счёта: " + e.getMessage());
        }
        try {
            ivan.transfer(anna, 100_00);
        } catch (Exception e) {
            System.out.println("перевод на заблокированный счёт: " + e.getMessage());
        }
        anna.unblock();
        anna.withdraw(1_000_00);
        System.out.println("После разблокировки Анна сняла 1000.00 ₽: " + anna);

        System.out.println();
        System.out.println("===== 8. Изменение лимита =====");
        try {
            ivan.withdraw(70_000_00);
        } catch (Exception e) {
            System.out.println("снять 70000.00 ₽ при лимите 50000.00 ₽: " + e.getMessage());
        }
        ivan.setWithdrawLimit(80_000_00);
        ivan.withdraw(70_000_00);
        System.out.println("Лимит подняли до 80000.00 ₽ и сняли 70000.00 ₽: " + ivan);

        System.out.println();
        System.out.println("===== 9. История операций =====");
        printHistory(ivan);
        printHistory(anna);
        printHistory(john);
    }

    private static void printHistory(final BankAccount account) {
        System.out.println("Счёт №" + account.getNumber() + " (" + account.getOwner() + "):");
        System.out.print(account.getHistory());
    }
}
