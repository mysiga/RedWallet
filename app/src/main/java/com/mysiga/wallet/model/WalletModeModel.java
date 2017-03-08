package com.mysiga.wallet.model;

import java.io.Serializable;

/**
 * 模式实体类
 */
public class WalletModeModel implements Serializable {
    public String name;
    public String mode;

    public WalletModeModel(String name, String mode) {
        this.name = name;
        this.mode = mode;
    }
}
