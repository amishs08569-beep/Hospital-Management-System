package com.hospital.model;

import java.math.BigDecimal;

public class InvoiceItem {
    private Integer itemId;
    private Integer invoiceId;
    private String itemType; // CONSULTATION, ROOM_CHARGE, LAB_TEST, PHARMACY, PROCEDURE, MISC
    private String description;
    private int quantity = 1;
    private BigDecimal unitPrice = BigDecimal.ZERO;
    private BigDecimal totalPrice = BigDecimal.ZERO;

    public InvoiceItem() {}

    public InvoiceItem(Integer itemId, Integer invoiceId, String itemType,
                       String description, int quantity, BigDecimal unitPrice, BigDecimal totalPrice) {
        this.itemId = itemId;
        this.invoiceId = invoiceId;
        this.itemType = itemType;
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }

    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }

    public Integer getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Integer invoiceId) { this.invoiceId = invoiceId; }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    @Override
    public String toString() {
        return description + " (" + quantity + " @ $" + unitPrice + " = $" + totalPrice + ")";
    }
}
