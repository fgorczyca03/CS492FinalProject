import java.util.Scanner;
import javax.crypto.SecretKey;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in); //Read user input
        FileHandler fileHandler = new FileHandler(); //For saving or loading data and salt

        //load the saved salt
        byte[] salt = fileHandler.loadSalt();
        if (salt == null) {
            //if no salt file exists, genereate one and prompt for master pass creation
            salt = Encryption.generateSalt();
            fileHandler.saveSalt(salt);
            System.out.print("Create master password: ");
        } else {
            //If salt.bin exist, prompt user for the maspass
            System.out.print("Enter master password: ");
        }

        String masterPassword = scanner.nextLine(); //User input for Master Password
        SecretKey key = Encryption.deriveKey(masterPassword, salt); //Derive the AES enc key from master password and salt

        //Init enc and password manager with derived key
        Encryption encryption = new Encryption(key);
        PasswordManager pm = new PasswordManager(encryption);

        //main prog loop
        while (true) {
            //Show menu items
            System.out.println("\n1. Add Password\n2. Retrieve Password\n3. Exit");
            System.out.print("Choice: ");
            String choice = scanner.nextLine();

            if (choice.equals("1")) {
                // add password
                System.out.print("Site: ");
                String site = scanner.nextLine();
                System.out.print("Password: ");
                String password = scanner.nextLine();
                pm.addPassword(site, password);
            } else if (choice.equals("2")) {
                //retrieve password
                System.out.print("Site: ");
                String site = scanner.nextLine();
                pm.getPassword(site);
            } else if (choice.equals("3")) {
                //exit the program
                break;
            } else {
                //invalid input handling
                System.out.println("Invalid choice.");
            }
        }
        scanner.close(); //close input loop
    }
}
