import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

public class Transaction {

    public String transactionId;
    public PublicKey sender;
    public PublicKey recipient;
    public float value;
    public byte[] signature;

    public ArrayList<TransactionInput> inputs;
    public ArrayList<TransactionOutput> outputs = new ArrayList<>();

    public static int sequence = 0;

    public Transaction(PublicKey sender, PublicKey recipient, float value, ArrayList<TransactionInput> inputs) {
        this.sender = sender;
        this.recipient = recipient;
        this.value = value;
        this.inputs = inputs;
    }

    // Calculates the transaction hash, which will serve as the transaction's id.
    private String calculateHash() {

        // Increments the sequence to prevent two transactions from having identical keys.
        sequence++;
        return StringUtil.applySha256(
                StringUtil.getStringFromKey(sender) +
                        StringUtil.getStringFromKey(recipient) +
                        Float.toString(value) +
                        sequence
        );
    }

    // Signs all the data we don't wish to be tampered with.
    public void generateSignature(PrivateKey privateKey) {

        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + Float.toString(value);
        signature = StringUtil.applyECDSASig(privateKey, data);
    }

    // Verifies that the data we signed hasn't been tampered with.
    public boolean verifySignature() {

        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + Float.toString(value);
        return StringUtil.verifyECDSASig(sender, data, signature);
    }

    // Returns true if the new transaction could be created.
    public boolean processTransaction() {

        // Checks to see if the signature can be verified.
        if (verifySignature() == false) {
            System.out.println("# Transaction signature could not be verified.");
            return false;
        }

        // Gathers all transaction inputs, making sure they are unspent.
        for (TransactionInput i : inputs) {
            i.UTXO = BasicBlockchain.UTXOs.get(i.transactionOutputId);
        }

        // Checks if transaction amount is valid.
        if (getInputsValue() < BasicBlockchain.minimumTransaction) {
            System.out.println("# Transaction inputs are too small.");
            return false;
        }

        // Generates transaction outputs.
        // Get value of the inputs then the leftover change.
        float leftOver = getInputsValue() - value;
        transactionId = calculateHash();
        // Send value to recipient.
        outputs.add(new TransactionOutput(this.recipient, value, transactionId));
        // Send the leftover "change" back to sender.
        outputs.add(new TransactionOutput(this.sender, leftOver, transactionId));


        // Adds outputs to the unspent list.
        for (TransactionOutput o : outputs) {
            BasicBlockchain.UTXOs.put(o.id, o);
        }

        // Removes the transaction inputs from the blockchain's UTXO list as spent.
        for (TransactionInput i : inputs) {
            if (i.UTXO == null) continue;
            BasicBlockchain.UTXOs.remove(i.UTXO.id);
        }

        return true;
    }

    // Returns the sum of input (UTXO) values.
    public float getInputsValue() {

        float total = 0;
        for (TransactionInput i : inputs) {
            // If the transaction can't be found, skip it.
            if (i.UTXO == null) continue;
            total += i.UTXO.value;
        }
        return total;
    }

    // Returns the sum of outputs.
    public float getOutputsValue() {

        float total = 0;
        for (TransactionOutput o : outputs) {
            total += value;
        }
        return total;
    }
}
