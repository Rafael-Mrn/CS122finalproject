import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.io.*;
import java.security.*;
import java.util.Base64;






class SecureNotes {
    /*This will run a while loop with options given to create/edit an existing text file (classs fileDatabase, fileCreate and fileEditor method), password protect a file (passwordProtector method),
    encrypt/decrypt a file (fileEncrypt). The loop only begins after successful login (class Credentials). The loop will automatically exit after 5 mintues of inactivity during login.
    */

    private HashSet<Note> existingNotes = new HashSet<Note>();
    


    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        Credentials credentials = new Credentials();
        
        System.out.println("====================================================================================");
        System.out.println("Welcome to SecureNotes, the secure HIPAA-aware note-taking platform for therapists.");
        System.out.println("====================================================================================\n");
        
        boolean registration = false;
        while (!registration){
            System.out.print("Enter a username to register: ");
            String username = scan.nextLine();
            System.out.print("Create a password: ");
            String password = scan.nextLine();
            
            if (credentials.register(username, password)){
                registration = true;
            }
        }

        boolean login = false;
        while (!login){
            System.out.print("Enter your username to login: ");
            String username = scan.nextLine();
            System.out.print("Password: ");
            String password = scan.nextLine();

            if (credentials.login(username, password)){
                login = true;
            }
        }

        int choice = 0;

        while (choice != 5){
            System.out.println("1. Manage notes (Create or Delete)");
            System.out.println("2. Edit an existing note");
            System.out.println("3. Decrypt a note");
            System.out.println("4. Remove password from a note");
            System.out.println("5. Exit");

            System.out.print("Enter a choice 1-5: ");
            choice = scan.nextInt();

            if (choice == 1){
                System.out.println("=====NOTE MANAGER=====")
                System.out.println("1. Create new Note");
                System.out.println("2. Delete an existing Note");
                
                System.out.println("Enter a choice 1 or 2: ");
                int managerChoice = scan.nextInt();

                if (managerChoice == 1){
                    System.out.print("Enter new Note's name: ");
                    System.out.println("Enter password to access note: ")
                }



            }

        }
        

    }

    
}

class Credentials {

    // Stores username -> [hash, salt] pairs
    private final HashMap<String, String> credentials;
    private String currentUsername;

    public Credentials() {
        credentials = new HashMap<>();
    }

    public boolean register(String username, String password) {
        if (username == null || username.isBlank()) {
            System.out.println("Username cannot be empty, try again.");
            return false;
        }
        if (credentials.containsKey(username)) {
            System.out.println("Username already exists, try again.");
            return false;
        }
        if (password == null || password.isBlank()) {
            System.out.println("Password cannot be empty, try again.");
            return false;
        }

        try {
            String hash = hashPassword(password);
            credentials.put(username, hash);
            System.out.println("\nUser registered successfully!\n");
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error registering user: " + e.getMessage());
        }
        return true;
    }

    public boolean login(String username, String password) {
        if (!credentials.containsKey(username)) {
            System.out.println("Username not found.");
            return false;
        }

        try {
            String hash = credentials.get(username);
            if (hashPassword(password).equals(hash)) {
                currentUsername = username;
                System.out.println("\nLogin successful. Welcome, " + username + "!\n");
            } else {
                System.out.println("Incorrect password.");
                return false;
            }
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Login error: " + e.getMessage());
            return false;
        }
        return true;
    }

    public boolean userExists(String username) {
        return credentials.containsKey(username);
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    //SHA-256 one-way hash
    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256"); //Instantiates a SHA-256 hash
        byte[] hash = digest.digest(password.getBytes()); //Hashes the binary version of String password stores it in a byte array
        return Base64.getEncoder().encodeToString(hash); //Returns the byte[] hash as a readable String
    }
    

}
            

class Note{
    
    /*create/edit an existing text file (classs Notes, fileEditor method), password protect a file (passwordProtector method),
    encrypt/decrypt a file (fileEncrypt)*/
    private String name;
    private String directory;
    private boolean isPasswordProtected;
    private boolean isEncrypted;
    private String passwordHash;
    private File file;
    
    public Note(String name, String directory, boolean isPasswordProtected, boolean isEncrypted){
        this.name = name;
        this.directory = directory;
        this.isPasswordProtected = isPasswordProtected;
        this.isEncrypted = isEncrypted;
    }

    //Creates the Note's respective file
    public boolean create(){
        file = new File(directory, name + ".txt");
        try{
            //Make the full directory if one wasn't provided or doesn't exist
            if (file.getParentFile() != null && !file.getParentFile().exists()){
                file.getParentFile().mkdirs();
            }
            //Creates the file
            if (!file.createNewFile()){
                System.out.println(file + "already exists!");
                return false;
            }
            System.out.println("Note created at: " + file.getAbsolutePath());
            return true;
        } catch (IOException e){
            System.out.println("Error creating file: " + e.getMessage());
            return false;
        }
    }

    /* NOTE EDITOR  (create or overwrite content)
       ─────────────────────────────────────────────
       Appends content to the note's file.
       If the note is password-protected, the correct password must be supplied.
       If the note is encrypted, content is encrypted before writing. */

    //Appends content to the note's file. If password is incorrect, deny append and return false.
    //Otherwise, append content and return true.
    public boolean editor(String content, String password) throws Exception {    
        if (!authenticate(password)) {
            System.out.println("Incorrect password. Edit denied.");
            return false;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(content);
            System.out.println("Note saved successfully.");
        } catch (IOException e) {
            System.err.println("Error writing file: " + e.getMessage());
        }
        return true;
    }

    //Reads the note's content. If encrypted, the content is decrypted before being returned.
    public String read(String password) throws Exception {
        //If password is incorrect, read will be denied
        if (!authenticate(password)) {
            System.out.println("Incorrect password. Read denied.");
            return null;
        }

        StringBuilder content = new StringBuilder();

        //Builds content line by line and catches the IOException
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return null;
        }

        String raw = content.toString().trim();

        //If isEncrypted == true, return decrypted note, else return raw
        return isEncrypted ? decrypt(raw, password) : raw;
    }

    /*  PASSWORD PROTECTION
        ─────────────────────────────────────────────
        Implements password protection and the option to remove a password from a note. 
        Created passwords are hashed and stored in passwordHash. The authenticate method 
        verifies a password and can only be used within this class. */

    //Enables password protection by hashing and storing the given password.
    public void passwordProtector(String password) throws NoSuchAlgorithmException {
        if (password == null || password.isEmpty()) {
            System.out.println("Password cannot be empty.");
            return;
        }
        this.passwordHash = hashPassword(password); //Stores the hash String
        this.isPasswordProtected = true;
        System.out.println("Password protection enabled.");
    }

    //Removes password from the note
    public void removePassword(String currentPassword) throws NoSuchAlgorithmException {
        if (!authenticate(currentPassword)) {
            System.out.println("Incorrect password. Cannot remove protection.");
            return;
        }
        this.passwordHash = null;
        this.isPasswordProtected = false;
        System.out.println("Password protection removed.");
    }

    //Returns true when the password matches (or no protection is set).
    private boolean authenticate(String password) throws NoSuchAlgorithmException {
        //Notes with no password protection are automatically authenticated
        if (!isPasswordProtected) return true; 
        if (password == null) return false;
        //Hashes the provided password and compares it with the existing password's hash
        return hashPassword(password).equals(passwordHash);
    }

    //SHA-256 one-way hash
    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256"); //Instantiates a SHA-256 hash
        byte[] hash = digest.digest(password.getBytes()); //Hashes the binary version of String password stores it in a byte array
        return Base64.getEncoder().encodeToString(hash); //Returns the byte[] hash as a readable String
    }
    

    /*  ENCRYPTION / DECRYPTION  (Caesar Cipher)
        ─────────────────────────────────────────────
        Encrypts plaintext using a Caesar cipher.
        The shift value is derived from the sum of the password's char values. */

    public String encrypt(String plainText, String password) {
        int shift = deriveShift(password);
        StringBuilder encrypted = new StringBuilder();

        for (char c : plainText.toCharArray()) {
            if (Character.isLetter(c)) {
                char base = Character.isUpperCase(c) ? 'A' : 'a';
                encrypted.append((char) ((c - base + shift) % 26 + base));
            } else {
                encrypted.append(c);  // non-letters are unchanged
            }
        }
        return encrypted.toString();
}

    //Decrypts a Caesar-encrypted string by reversing the shift.
    public String decrypt(String encryptedText, String password) {
        int shift = deriveShift(password);
        StringBuilder decrypted = new StringBuilder();

        for (char c : encryptedText.toCharArray()) {
            if (Character.isLetter(c)) {
                char base = Character.isUpperCase(c) ? 'A' : 'a';
                decrypted.append((char) ((c - base - shift + 26) % 26 + base));
            } else {
                decrypted.append(c);  // non-letters are unchanged
            }
        }
        return decrypted.toString();
    }

    //Derives a shift value (1–25) from the password by summing its char values and clamping to the alphabet size.
    private int deriveShift(String password) {
        int sum = 0;
        for (char c : password.toCharArray()) {
            sum += c;
        }
        int shift = sum % 26;
        return shift == 0 ? 1 : shift;   // ensure shift is never 0
    }
}
 
