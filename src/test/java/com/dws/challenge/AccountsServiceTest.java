package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import com.dws.challenge.domain.MoneyTransferRequest;
import com.dws.challenge.exception.DataValidationException;
import com.dws.challenge.exception.NotFoundException;
import java.math.BigDecimal;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.service.AccountsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AccountsServiceTest {

  @Autowired
  private AccountsService accountsService;

  @Test
  void addAccount() {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  void addAccount_failsOnDuplicateId() {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }
  }

  @Test
  void moneyTransfer() {
    Account account1 = new Account("Id-123");
    account1.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account1);

    Account account2 = new Account("Id-124");
    account2.setBalance(new BigDecimal(20));
    this.accountsService.createAccount(account2);

    MoneyTransferRequest moneyTransferRequest = new MoneyTransferRequest(new BigDecimal(30), "Id-124", "Id-123");
    this.accountsService.moneyTransfer(moneyTransferRequest);

    assertEquals(new BigDecimal(970), this.accountsService.getAccount("Id-123").getBalance());
  }

  @Test
  void moneyTransferNegativeAmountThrowsDataValidationException() {
    Account account1 = new Account("Id-123");
    account1.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account1);

    Account account2 = new Account("Id-124");
    account2.setBalance(new BigDecimal(20));
    this.accountsService.createAccount(account2);

    MoneyTransferRequest moneyTransferRequest = new MoneyTransferRequest(new BigDecimal(-30), "Id-124", "Id-123");

    assertThrows(DataValidationException.class, () -> this.accountsService.moneyTransfer(moneyTransferRequest));
  }

  @Test
  void moneyTransferInsufficientBalanceThrowsDataValidationException() {
    Account account1 = new Account("Id-123");
    account1.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account1);

    Account account2 = new Account("Id-124");
    account2.setBalance(new BigDecimal(20));
    this.accountsService.createAccount(account2);

    MoneyTransferRequest moneyTransferRequest = new MoneyTransferRequest(new BigDecimal(2000000), "Id-124", "Id-123");

    assertThrows(DataValidationException.class, () -> this.accountsService.moneyTransfer(moneyTransferRequest));
  }

  @Test
  void moneyTransferAccountNotFoundThrowsNotFoundException() {
    MoneyTransferRequest moneyTransferRequest = new MoneyTransferRequest(new BigDecimal(2000000), "Id-124", "Id-123");

    assertThrows(NotFoundException.class, () -> this.accountsService.moneyTransfer(moneyTransferRequest));
  }
}
