package org.irmantas.booksstore.model;

import java.math.BigDecimal;

public interface BookHelpers {
    String validateBook();
    BigDecimal acquireTotalPrice();
}
