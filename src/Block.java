import java.util.ArrayList;
import java.util.Date;

public class Block {

    public String hash;
    public String previousHash;
    public String merkleRoot;
    public ArrayList<Transaction> transactions;
    private long timestamp;
    private int nonce;

    public Block(String previousHash) {
        this.previousHash = previousHash;
        this.timestamp = new Date().getTime();
        this.nonce = 0;
        // Only do this after calculating the other values.
        this.hash = calculateHash();
    }

    public String calculateHash() {

        String calculatedHash = StringUtil.applySha256(
                previousHash +
                        Long.toString(timestamp) +
                        Integer.toString(nonce) +
                        merkleRoot);
        return calculatedHash;
    }

    // Increases nonce value until hash target is reached.
    public void mineBlock(int difficulty) {

        merkleRoot = StringUtil.getMerkleRoot(transactions);
        String target = StringUtil.getDifficultyString(difficulty);
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
        System.out.println("Block mined! : " + hash);
    }

    // Adds transactions to this block.
    public boolean addTransaction(Transaction transaction) {

        // Processes transaction and checks if valid, unless block is genesis block then ignore.
        if (transaction == null) return false;
        if (previousHash != "0") {
            if (transaction.processTransaction() != true) {
                System.out.println("Transaction failed to process. Discarded.");
                return false;
            }
        }
        transactions.add(transaction);
        System.out.println("Transaction successfully added to the block!");
        return true;
    }
}
