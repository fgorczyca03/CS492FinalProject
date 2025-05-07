import org.json.simple.JSONObject;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;



public class PasswordManager {
    //References to other files
    private final Encryption encryption;
    private final FileHandler fileHandler;
    //JSON object that stores all site password entries in memory
    private final JSONObject passwordStore;

    //Inits components and loads saved passes from file
    public PasswordManager(Encryption encryption) {
        this.encryption = encryption;
        this.fileHandler = new FileHandler();
        this.passwordStore = fileHandler.loadPasswords();
    }

    //add password
    public void addPassword(String site, String password) throws Exception {
        //encrypt the password
        String[] result = encryption.encrypt(password);
        //Create a JSON object to store all 3 componenets
        JSONObject entry = new JSONObject();
        entry.put("iv", result[0]);
        entry.put("password", result[1]);
        entry.put("hash", result[2]);
        //Store the encrypted entry under site name
        passwordStore.put(site, entry);

        //Save the updated paswsword to the JSON fikle
        fileHandler.savePasswords(passwordStore);
        System.out.println("Password saved.");
    }

    //Retrieve and decrypt passwords for a given site
    public void getPassword(String site) throws Exception {
        //Get the JSON object corresponding to site
        JSONObject entry = (JSONObject) passwordStore.get(site);
        if (entry == null) {
            System.out.println("No password found.");
            return;
        }

        //decrypt the password using IV, CT and hash
        String decrypted = encryption.decrypt(
            (String) entry.get("iv"),
            (String) entry.get("password"),
            (String) entry.get("hash")
        );
        System.out.println("Password: " + decrypted);
    }
}
