import java.io.*;
import java.util.*;
import org.json.simple.*;
import org.json.simple.parser.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class FileHandler {
    //File where encrypted password is stored
    private final String filename = "passwords.json";

    //Loads pass data from File
    //Returns JSON object containing all passes
    public JSONObject loadPasswords() {
        try (FileReader reader = new FileReader(filename)) {
            //Parse and return JSON file
            return (JSONObject) new JSONParser().parse(reader);
        } catch (Exception e) {
            //If file not found or corrupted, return new empty JSON objkect
            return new JSONObject();
        }
    }

    //Saves the pass data to JSON file
    public void savePasswords(JSONObject data) {
        try (FileWriter writer = new FileWriter(filename)) {
            //Write the data as JSON string
            writer.write(data.toJSONString());
        } catch (IOException e) {
            //Print error on message
            System.out.println("Error saving passwords.");
        }
    }

    //Saves generated salt to bin file
    public void saveSalt(byte[] salt) {
        try (FileOutputStream out = new FileOutputStream("salt.bin")) {
            //Write the raw bytes to a file
            out.write(salt);
        } catch (IOException e) {
            //Handle save errors
            System.out.println("Error saving salt.");
        }
    }


    //Loads salt from bin file
    //Returns the byte array used in key derivation
    public byte[] loadSalt() {
        try (FileInputStream in = new FileInputStream("salt.bin")) {
            return in.readAllBytes();
        } catch (IOException e) {
            //null if file doesnt exist
            return null;
        }
    }
}
