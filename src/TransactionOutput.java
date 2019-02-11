import java.security.PublicKey;

public class TransactionOutput {

    public String id;
    // AKA, the new owner of the coins
    public PublicKey recipient;
    // The amount of coins they own.
    public float value;
    // The id of the Transaction this output was created in
    public String parentTransactionId;

    public TransactionOutput(PublicKey recipient, float value, String parentTransactionId) {
        this.recipient = recipient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = StringUtil.applySha256(
                StringUtil.getStringFromKey(recipient) +
                        Float.toString(value) +
                        parentTransactionId);
    }

    // Check if coin belongs to you.
    public boolean isMine(PublicKey publicKey) {
        return publicKey == recipient;
    }
}
