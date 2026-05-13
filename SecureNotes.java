import java.util.HashMap;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.Base64;




class SecureNotes {
    /*This will run a while loop with options given to create/edit an existing text file (classs fileDatabase, fileCreate and fileEditor method), password protect a file (passwordProtector method),
    encrypt/decrypt a file (fileEncrypt). The loop only begins after successful login (class Credentials). The loop will automatically exit after 5 mintues of inactivity during login.
    */
    public static void main(String[] args) {
        System.out.println("Welcome to SecureNotes");
        System.out.println("Enter username: ");
        
        /*
        1. Manage notes (Create or Delete)
        2. Edit an existing note
        3. Encrypt a note
        4. Password Protect a note
        5. Exit
         */

    }

    
}

class Credentials{
    //Creates new credentials and stores them in an an existing HashMap (credentials). Checks if a credential exists and approves login attempts
    HashMap<String, String> credentials = new HashMap<String, String>();
    private String username;
    private String password;
//Constructor
    public Credentials(){
        credentials = new HashMap<>();}

    //Creating a new user
    public void user(String username, String password){
if(credentials.containsKey(username)){
    System.out.println("Username already exists.");
    }else{
    credentials.put(username, password);
    System.out.println("User created successfully.");}
}
    // This is the login method
    public boolean login(String username, String password){
       if(credentials.containsKey(username)){ 
           if(credentials.get(username).equals(password)){
               this.username = username;
               this.password = password;

            System.out.println("Login was successful.");
               return true;
           }
           else{ System.out.println("Incorrect password!");
                return false;    
           }
       }
        else{
            System.out.println("Username not found.");
            return false;
        }
    }
    //Checking
        public boolean userExists(String username){
           return credentials.containsKey(username);
        }

    //Removing user
            public void removeUser(String username){
                if(credentials.containsKey(username)){
                credentials.remove(username);
                System.out.println("User removed.");
        }
            else{
                System.out.println("User does not exist.");
            }
        }

    // Getting the current log in username
    public String getCurrentUsername(){
        return username;
    }

    //Getting the current log in password
    public String getCurrentPassword(){
        return password;
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

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int KEY_SIZE = 128;

    
    public Note(String name, String directory, boolean isPasswordProtected, boolean isEncrypted){
        this.name = name;
        this.directory = directory;
        this.isPasswordProtected = isPasswordProtected;
        this.isEncrypted = isEncrypted;
    }

    public boolean create(){
        File file = new File(directory, name + ".txt");
        try{
            if (!file.getParentFile().exists()){
                file.getParentFile().mkdir();
            }
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

    
    // ─────────────────────────────────────────────
    //  FILE EDITOR  (create or overwrite content)
    // ─────────────────────────────────────────────

    /**
     * Writes (or overwrites) the note's content.
     * If the note is password-protected, the correct password must be supplied.
     * If the note is encrypted, content is encrypted before writing.
     */
    public void fileEditor(String content, String password) throws Exception {
        if (!authenticate(password)) {
            System.out.println("Incorrect password. Edit denied.");
            return;
        }

        File file = new File(directory, name + ".txt");
        String dataToWrite = isEncrypted ? fileEncrypt(content, password) : content;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(dataToWrite);
            System.out.println("Note saved successfully.");
        } catch (IOException e) {
            System.err.println("Error writing file: " + e.getMessage());
        }
    }

    /**
     * Reads the note's content.
     * If encrypted, the content is decrypted before being returned.
     */
    public String readNote(String password) throws Exception {
        if (!authenticate(password)) {
            System.out.println("Incorrect password. Read denied.");
            return null;
        }

        File file = new File(directory, name + ".txt");
        StringBuilder content = new StringBuilder();

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
        return isEncrypted ? fileDecrypt(raw, password) : raw;
    }

    // ─────────────────────────────────────────────
    //  PASSWORD PROTECTION
    // ─────────────────────────────────────────────

    /**
     * Enables password protection by hashing and storing the given password.
     */
    public void passwordProtector(String password) throws NoSuchAlgorithmException {
        if (password == null || password.isEmpty()) {
            System.out.println("Password cannot be empty.");
            return;
        }
        this.passwordHash = hashPassword(password);
        this.isPasswordProtected = true;
        System.out.println("Password protection enabled.");
    }

    /** Removes password protection from the note. */
    public void removePassword(String currentPassword) throws NoSuchAlgorithmException {
        if (!authenticate(currentPassword)) {
            System.out.println("Incorrect password. Cannot remove protection.");
            return;
        }
        this.passwordHash = null;
        this.isPasswordProtected = false;
        System.out.println("Password protection removed.");
    }

    /** Returns true when the password matches (or no protection is set). */
    private boolean authenticate(String password) throws NoSuchAlgorithmException {
        if (!isPasswordProtected) return true;
        if (password == null) return false;
        return hashPassword(password).equals(passwordHash);
    }

    /** SHA-256 one-way hash. */
    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }
    
    // ─────────────────────────────────────────────
    //  ENCRYPTION / DECRYPTION  (Caesar Cipher)
    // ─────────────────────────────────────────────

    /**
    * Encrypts plaintext using a Caesar cipher.
    * The shift value is derived from the sum of the password's char values.
    */
    public String fileEncrypt(String plainText, String password) {
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

    /**
    * Decrypts a Caesar-encrypted string by reversing the shift.
    */
    public String fileDecrypt(String encryptedText, String password) {
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

    /**
    * Derives a shift value (1–25) from the password
    * by summing its char values and clamping to the alphabet size.
    */
    private int deriveShift(String password) {
        int sum = 0;
        for (char c : password.toCharArray()) {
            sum += c;
        }
        int shift = sum % 26;
        return shift == 0 ? 1 : shift;   // ensure shift is never 0
    }
}
 
