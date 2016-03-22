
/*
 * Copyright (c) 1998 - 2005 Versant Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Versant Corporation - initial API and implementation
 */
package petstore.db;


import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * This class represents a particular item in the Catalog Component.
 * Each item belongs to particular type of product
 * and has attributes like id,listprice etc.
 *
 */
public class Item implements Serializable {

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";

    private int itemId;
    private Category category;
    private String code;
    private String name;
    private String description;
    private String status;
    private String attribute1;
    private String attribute2;
    private String attribute3;
    private String attribute4;
    private String attribute5;
    private double listPrice;
    private double unitCost;
    private String imageLocation;
    private NumberFormat  format = NumberFormat.getCurrencyInstance();

    public Item() {
        super();
    }

    public Item(Category category,
                String code,
                String name,
                String description,
                String status,
                String imageLocation,
                String attribute1,
                String attribute2,
                String attribute3,
                String attribute4,
                String attribute5,
                double listPrice,
                double unitCost) {

        this.category = category;
        this.code = code;
        this.name = name;
        this.description = description;
        this.status = status;
        this.imageLocation = imageLocation;
        this.attribute1 = attribute1;
        this.attribute2 = attribute2;
        this.attribute3 = attribute3;
        this.attribute4 = attribute4;
        this.attribute5 = attribute5;
        this.listPrice = listPrice;
        this.unitCost = unitCost;

    }

    public int getItemId() {
       return itemId;
    }

    public void setItemId(int itemId) {
       this.itemId = itemId;
    }

    public Category getCategory() {
       return category;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
       return status;
    }

    public void setStatus(String status) {
       this.status = status;
    }

    public String getImageLocation() {
        return imageLocation;
    }

    public String getAttribute() {
        return attribute1;
    }

    public String getAttribute(int index) {
        switch (index) {
            case 1: return attribute1;
            case 2: return attribute2;
            case 3: return attribute3;
            case 4: return attribute4;
            case 5: return attribute5;
            default: return attribute1;
        }
    }

    public double getUnitCost() {
        return unitCost;
    }

    public double getListPrice() {
        return listPrice;
    }

    public String getListPriceString(){
        return format.format(listPrice);
    }

    public String toString() {
        return "{ code=" + code + "; listPrice=" + listPrice + "; category=" + category + "}";
    }

}
