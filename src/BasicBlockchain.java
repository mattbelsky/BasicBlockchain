import com.google.gson.GsonBuilder;

import java.util.ArrayList;

public class BasicBlockchain {

    public static ArrayList<Block> blockchain = new ArrayList<>();
    public static int difficulty = 5;

    public static void main(String[] args) {

        blockchain.add(new Block("0", "This is the first block."));
        System.out.println("Trying to mine block 1...");
        blockchain.get(0).mineBlock(difficulty);

        blockchain.add(new Block(blockchain.get(blockchain.size() - 1).hash, "This is the second block."));
        System.out.println("Trying to mine block 2...");
        blockchain.get(1).mineBlock(difficulty);

        blockchain.add(new Block(blockchain.get(blockchain.size() - 1).hash, "This is the third block."));
        System.out.println("Trying to mine block 3...");
        blockchain.get(2).mineBlock(difficulty);

        System.out.println("Blockchain is valid: " + isChainValid());

        String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
        System.out.println("The blockchain:\n" + blockchainJson);
    }

    // Checks whether any blocks have been modified.
    public static Boolean isChainValid() {

        Block current;
        Block previous;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');

        for (int i = 1; i < blockchain.size(); i++) {
            current = blockchain.get(i);
            previous = blockchain.get(i - 1);

            // Checks current registered hash against calculated hash.
            // Was this or the previous block modified?
            if (!current.hash.equals(current.calculateHash())) {
                System.out.println("Current hashes are not equal.");
                return false;
            }
            // Checks previous registered hash against the current block's value for the previous hash.
            // Was the previous block modified?
            if (!previous.hash.equals(current.previousHash)) {
                System.out.println("Previous hashes are not equal.");
                return false;
            }
            // Checks if hash has been solved.
            // You can add a block to the chain, but until the hash has been solved by everyone storing the chain, the
            // block and thus the entire chain is temporarily invalid.
            if (!current.hash.substring(0, difficulty).equals(hashTarget)) {
                System.out.println("This block hasn't been mined.");
                return false;
            }
        }
        return true;
    }
}
