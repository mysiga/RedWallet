package com.mysiga.wallet.model;

import java.io.Serializable;

/**
 * 模式实体类
 *
 * @author Wilson milin411@163.com
 */
public class WalletModeModel implements Serializable {
    public String name;
    public String mode;

    public WalletModeModel(String name, String mode) {
        this.name = name;
        this.mode = mode;
    }
}
