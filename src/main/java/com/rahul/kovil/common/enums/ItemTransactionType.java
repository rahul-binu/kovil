package com.rahul.kovil.common.enums;

public enum ItemTransactionType {
	PURCHASE, // Bought from supplier (IN)
	SALE, // Sold to customer (OUT)
	CONVERSION, // Internal conversion (IN/OUT)
	ADJUSTMENT, // Manual correction
	RETURN_IN, // Customer return
	RETURN_OUT, // Supplier return
	OPENING // Item opening
}
