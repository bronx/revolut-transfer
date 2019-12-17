package com.revolut.transfer.api.resource

import java.math.BigDecimal

data class AccountDTO(val id: String? = null, val name: String, val balance: BigDecimal?)