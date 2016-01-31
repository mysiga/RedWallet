package wallet.mysiga.com.redwallet.model;

import java.io.Serializable;

/**
 * 模式实体类
 *
 * @author Wilson
 */
public class WalletModeModel implements Serializable {
    public String name;
    public String mode;

    public WalletModeModel(String name, String mode) {
        this.name = name;
        this.mode = mode;
    }
}
