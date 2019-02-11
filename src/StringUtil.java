import java.security.*;
import java.util.ArrayList;
import java.util.Base64;

public class StringUtil {

    public static String applySha256(String input) {

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        }
        catch (Exception e) {
            throw new RuntimeException();
        }
    }

    // Applies ECDSA signature and returns the results.
    public static byte[] applyECDSASig(PrivateKey privateKey, String input) {

        Signature dsa;
        byte[] output = new byte[0];

        try {
            dsa = Signature.getInstance("ECDSA", "BC");
            dsa.initSign(privateKey);
            byte[] strByte = input.getBytes();
            dsa.update(strByte);
            byte[] realSig = dsa.sign();
            output = realSig;
        }
        catch (Exception e) {
            throw new RuntimeException();
        }

        return output;
    }

    // Verifies a string signature.
    public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {

        try {
            Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(data.getBytes());
            return ecdsaVerify.verify(signature);
        }
        catch (Exception e) {
            throw new RuntimeException();
        }
    }

    public static String getStringFromKey(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    // Takes in an array of transactions and returns a merkle root.
    public static String getMerkleRoot(ArrayList<Transaction> transactions) {

        int count = transactions.size();
        ArrayList<String> previousTreeLayer = new ArrayList<>();

        for (Transaction transaction : transactions) {
            previousTreeLayer.add(transaction.transactionId);
        }

        ArrayList<String> treeLayer = new ArrayList<>();

        while (count > 1) {
            treeLayer = new ArrayList<>();

            for (int i = 0; i < previousTreeLayer.size(); i++) {
                treeLayer.add(applySha256(previousTreeLayer.get(i - 1) + previousTreeLayer.get(i)));
            }

            count = treeLayer.size();
            previousTreeLayer = treeLayer;
        }

        String merkleRoot = (treeLayer.size() == 1) ? treeLayer.get(0) : "";
        return merkleRoot;
    }

    public static String getDifficultyString(int difficulty) {
        return Integer.toString(difficulty);
    }
}
