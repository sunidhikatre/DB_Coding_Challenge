package com.dws.challenge.service;

import com.dws.challenge.constants.AccountConstants;
import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.MoneyTransferRequest;
import com.dws.challenge.exception.DataValidationException;
import com.dws.challenge.exception.NotFoundException;
import com.dws.challenge.repository.AccountsRepository;
import java.math.BigDecimal;
import java.util.Objects;
import javax.transaction.Transactional;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;

  private final NotificationService notificationService;

  @Autowired
  public AccountsService(AccountsRepository accountsRepository,
      NotificationService notificationService) {
    this.accountsRepository = accountsRepository;
    this.notificationService = notificationService;
  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }

  /**
   * Method to transfer money from one account to another.
   * @param moneyTransferRequest {@link MoneyTransferRequest}
   */
  public void moneyTransfer(MoneyTransferRequest moneyTransferRequest) {
    //Getting accounts
    Account senderAccount = getAccount(moneyTransferRequest.getSenderAccountId());
    Account recipientAccount = getAccount(moneyTransferRequest.getRecipientAccountId());

    makeTransaction(senderAccount, recipientAccount, moneyTransferRequest);
  }

  @Transactional
  private synchronized void makeTransaction(Account senderAccount, Account recipientAccount,
      MoneyTransferRequest moneyTransferRequest) {
    //validate MoneyTransferRequest Params
    validateMoneyTransferRequest(senderAccount, recipientAccount, moneyTransferRequest);

    //Debiting amount from Sender's account
    senderAccount.debit(moneyTransferRequest.getAmount());

    //Crediting amount to recipient account
    recipientAccount.credit(moneyTransferRequest.getAmount());

    accountsRepository.updateAccount(senderAccount);
    accountsRepository.updateAccount(recipientAccount);

    //Sending notifications to account holders.
    notifyAccountHolders(senderAccount, recipientAccount, moneyTransferRequest);
  }

  private void validateMoneyTransferRequest(Account senderAccount, Account recipientAccount, MoneyTransferRequest moneyTransferRequest){
    if (Objects.isNull(senderAccount))
      throw new NotFoundException(moneyTransferRequest.getSenderAccountId() + " " + AccountConstants.ACCOUNT_NOT_FOUND);

    if (Objects.isNull(recipientAccount))
      throw new NotFoundException(moneyTransferRequest.getRecipientAccountId() + " " + AccountConstants.ACCOUNT_NOT_FOUND);

    if (Objects.isNull(moneyTransferRequest.getAmount()))
      throw new DataValidationException(AccountConstants.AMOUNT_NOT_SPECIFIED);

    if (moneyTransferRequest.getAmount().compareTo(BigDecimal.valueOf(0)) < 0)
      throw new DataValidationException(AccountConstants.AMOUNT_INVALID);

    if (senderAccount.getBalance().compareTo(moneyTransferRequest.getAmount()) < 0)
      throw new DataValidationException(AccountConstants.INSUFFICIENT_BALANCE);
  }

  private void notifyAccountHolders(Account senderAccount, Account recipientAccount,
      MoneyTransferRequest moneyTransferRequest) {
    notificationService.notifyAboutTransfer(senderAccount,
        moneyTransferRequest.getAmount().toString() + " amount transferred to account ID: " + moneyTransferRequest.getRecipientAccountId());
    notificationService.notifyAboutTransfer(recipientAccount,
        moneyTransferRequest.getAmount().toString() + " amount received from account ID: " + moneyTransferRequest.getSenderAccountId());
  }
}
