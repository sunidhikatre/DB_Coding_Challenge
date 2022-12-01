package com.dws.challenge.domain;

import java.math.BigDecimal;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MoneyTransferRequest {

  @NotNull(message = "Amount can't be null")
  @Min(value = 0, message = "Amount must be greater than 0")
  private BigDecimal amount;

  @NotNull(message = "receiverId cannot be null")
  @NotEmpty
  private String recipientAccountId;

  @NotNull(message = "senderId cannot be null")
  @NotEmpty
  private String senderAccountId;
}
