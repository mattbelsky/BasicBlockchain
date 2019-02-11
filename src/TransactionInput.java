public class TransactionInput {

    // Reference to TransactionOutput -> transactionId
    public String transactionOutputId;
    // Contains the unspent transaction output.
    public TransactionOutput UTXO;

    public TransactionInput(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }
}
