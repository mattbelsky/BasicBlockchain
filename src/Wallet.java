import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Wallet {

    public PublicKey publicKey;
    public PrivateKey privateKey;

    // Only UTXOs owned by this wallet
    public HashMap<String, TransactionOutput> UTXOs = new HashMap<>();

    public Wallet() {
        generateKeyPair();
    }

    private void generateKeyPair() {

        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");

            // Initialize the key generator and generate a key pair.
            keyGen.initialize(ecSpec, random);
            KeyPair keyPair = keyGen.generateKeyPair();

            // Set the public and private keys from the key pair.
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        }
        catch (Exception e) {
            throw new RuntimeException();
        }
    }

    // Gets all the unspent transaction outputs (UTXOs) from the blockchain, determines which belong to this wallet,
    // adds the matching ones to the list of UTXOs in this wallet, and returns the total value of them all.
    public float getBalance() {

        float total = 0;

        for (Map.Entry<String, TransactionOutput> item : BasicBlockchain.UTXOs.entrySet()) {
            TransactionOutput UTXO = item.getValue();

            // If output belongs to me (if coins belong to me)...
            if (UTXO.isMine(publicKey)) {
                // Add it to our list of unspent transactions.
                UTXOs.put(UTXO.id, UTXO);
                total += UTXO.value;
            }
        }
        return total;
    }

    // Generates and returns a new transaction from this wallet.
    public Transaction sendFunds(PublicKey recipient, float value) {

        // Gets the wallet's balance and ensures that there are enough funds to send.
        if (getBalance() < value) {
            System.out.println("# Not enough funds to send transaction. Transaction discarded.");
            return null;
        }

        // Each input will hold an output. The list, which will contain the entire value to be sent plus a little more,
        // will be added to a new transaction object. Finally, each UTXO contained in the list of inputs will be removed
        // from this.UTXO.
        ArrayList<TransactionInput> inputs = new ArrayList<>();

        // The total value of each UTXO to be sent.
        float total = 0;

        // Adds each transaction output in this.UTXOs to a new transaction input and its value to "total" until the value
        // of "total" exceeds the value to be sent, upon which the loop breaks from iteration.
        for (Map.Entry<String, TransactionOutput> item : UTXOs.entrySet()) {
            TransactionOutput UTXO = item.getValue();
            total += UTXO.value;
            inputs.add(new TransactionInput(UTXO.id));
            if (total > value) break;
        }

        Transaction newTransaction = new Transaction(publicKey, recipient, value, inputs);
        newTransaction.generateSignature(privateKey);

        // Since a new transaction has been generated, and an array list of the transaction outputs which have been
        // used to fund it filled, iterates through each of the transaction inputs and removes from this.UTXOs each
        // output with the ID contained in the transaction input, therefore reducing the amount of funds in the wallet.
        for (TransactionInput input : inputs) {
            UTXOs.remove(input.transactionOutputId);
        }

        return newTransaction;
    }
}
