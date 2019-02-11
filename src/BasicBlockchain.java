import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

public class BasicBlockchain {

    public static ArrayList<Block> blockchain = new ArrayList<>();
    // List of all unspent transactions
    public static HashMap<String, TransactionOutput> UTXOs = new HashMap<>();

    public static int difficulty = 5;
    public static float minimumTransaction = 0.1f;
    public static Wallet walletA;
    public static Wallet walletB;
    public static Transaction genesisTransaction;

    public static void main(String[] args) {

        // BouncyCastle is the security provider.
        Security.addProvider(new BouncyCastleProvider());

        // Create new wallets.
        walletA = new Wallet();
        walletB = new Wallet();
        Wallet coinbase = new Wallet();

        // Creates genesis transaction, which sends 100 units to walletA.
        genesisTransaction = new Transaction(coinbase.publicKey, walletA.publicKey, 100f, null);
        // Manually signs the genesis transaction.
        genesisTransaction.generateSignature(coinbase.privateKey);
        // Manually sets the transaction ID.
        genesisTransaction.transactionId = "0";
        // Manually adds the transaction output object.
        genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.recipient, genesisTransaction.value,
                genesisTransaction.transactionId));
        // It's important to store the first transaction in the UTXO list.
        UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        System.out.println("Creating and mining genesis block...");
        Block genesis = new Block("0");
        genesis.addTransaction(genesisTransaction);
        addBlock(genesis);

        // Testing
        Block block1 = new Block(genesis.hash);
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("\nWalletA is Attempting to send funds (40) to WalletB...");
        block1.addTransaction(walletA.sendFunds(walletB.publicKey, 40f));
        addBlock(block1);
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        Block block2 = new Block(block1.hash);
        System.out.println("\nWalletA Attempting to send more funds (1000) than it has...");
        block2.addTransaction(walletA.sendFunds(walletB.publicKey, 1000f));
        addBlock(block2);
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        Block block3 = new Block(block2.hash);
        System.out.println("\nWalletB is Attempting to send funds (20) to WalletA...");
        block3.addTransaction(walletB.sendFunds( walletA.publicKey, 20));
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        isChainValid();
    }

    // Checks whether any blocks have been modified.
    public static Boolean isChainValid() {

        Block current;
        Block previous;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');
        HashMap<String, TransactionOutput> tempUTXOs = new HashMap<>();
        tempUTXOs.put(UTXOs.get(0).id, UTXOs.get(0));

        for (int i = 1; i < blockchain.size(); i++) {
            current = blockchain.get(i);
            previous = blockchain.get(i - 1);

            // Checks current registered hash against calculated hash.
            // Was this or the previous block modified?
            if (!current.hash.equals(current.calculateHash())) {
                System.out.println("# Current hashes are not equal.");
                return false;
            }
            // Checks previous registered hash against the current block's value for the previous hash.
            // Was the previous block modified?
            if (!previous.hash.equals(current.previousHash)) {
                System.out.println("# Previous hashes are not equal.");
                return false;
            }
            // Checks if hash has been solved.
            // You can add a block to the chain, but until the hash has been solved by everyone storing the chain, the
            // block and thus the entire chain is temporarily invalid.
            if (!current.hash.substring(0, difficulty).equals(hashTarget)) {
                System.out.println("# This block hasn't been mined.");
                return false;
            }

            TransactionOutput tempOutput;
            for (int t = 0; t < current.transactions.size(); t++) {

                // Each transaction in the current block.
                Transaction currentTransaction = current.transactions.get(t);

                // If the signature is invalid...
                if (!currentTransaction.verifySignature()) {
                    System.out.println("# Signature on transaction " + t + " is invalid.");
                    return false;
                }

                if (currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
                    System.out.println("# Inputs are not equal to outputs on transaction(" + t + ").");
                    return false;
                }

                for (TransactionInput input : currentTransaction.inputs) {
                    tempOutput = tempUTXOs.get(input.transactionOutputId);

                    if (tempOutput == null) {
                        System.out.println("# Referenced output on transaction(" + ") is missing.");
                        return false;
                    }

                    if (input.UTXO.value != tempOutput.value) {
                        System.out.println("# Referenced input transaction(" + t + " +) is invalid.");
                        return false;
                    }

                    tempUTXOs.remove(input.transactionOutputId);
                }

                for(TransactionOutput output: currentTransaction.outputs) {
                    tempUTXOs.put(output.id, output);
                }

                if( currentTransaction.outputs.get(0).recipient != currentTransaction.recipient) {
                    System.out.println("#Transaction(" + t + ") output recipient is not who it should be");
                    return false;
                }
                if( currentTransaction.outputs.get(1).recipient != currentTransaction.sender) {
                    System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
                    return false;
                }
            }
        }
        System.out.println("Blockchain is valid.");
        return true;
    }

    public static void addBlock(Block block) {
        block.mineBlock(difficulty);
        blockchain.add(block);
    }
}
