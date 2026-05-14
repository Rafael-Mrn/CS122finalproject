import java.util.HashMap;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.io.*;
import java.security.*;
import java.util.Base64;


class SecureNotes {
    /*This will run a while loop with options given to create/edit an existing text file (classs fileDatabase, fileCreate and fileEditor method), password protect a file (passwordProtector method),
    encrypt/decrypt a file (fileEncrypt). The loop only begins after successful login (class Credentials).
    */
    public static HashSet<Note> existingNotes = new HashSet<Note>();
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        Credentials credentials = new Credentials();
        
        System.out.println("====================================================================================");
        System.out.println("Welcome to SecureNotes, the secure HIPAA-aware note-taking platform for therapists.");
        System.out.println("====================================================================================\n");
        
        //Registration loop
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

        //Login loop
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
            System.out.println("\n==========================");
            System.out.println("     SecureNotes Menu     ");
            System.out.println("==========================");
            System.out.println("1. Manage notes (Create or Delete)");
            System.out.println("2. Edit an existing note");
            System.out.println("3. Decrypt a note");
            System.out.println("4. Remove password from a note");
            System.out.println("5. Exit");

            System.out.print("\nEnter a choice 1-5: ");

            // Guards against non-integer input on main menu
            try {
                choice = scan.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Choice must be a number 1-5.");
                scan.nextLine(); // clear bad input
                continue;
            }

            scan.nextLine(); //Clear buffer

            if (choice == 1){
                System.out.println("\n=====NOTE MANAGER=====");
                System.out.println("1. Create new Note");
                System.out.println("2. Delete an existing Note");
                System.out.print("\nEnter a choice 1 or 2: ");
                
                int managerChoice = 0;

                try {
                    managerChoice = scan.nextInt();
                } catch (InputMismatchException e){
                    System.out.println("Choice must be a number 1 or 2.");
                }

                scan.nextLine(); //Clear buffer

                //Create new note
                if (managerChoice == 1){
                    System.out.print("Enter directory to store Note: ");
                    String directory = scan.nextLine();
                    System.out.print("Enter new Note's name: ");
                    String name = scan.nextLine();

                    Note newNote = new Note(name, directory);
                    
                    if (newNote.create()){
                        System.out.print("Enter password to protect note: ");
                        String password = scan.nextLine();
                        
                        try {
                        newNote.passwordProtector(password);
                        } catch (NoSuchAlgorithmException e){
                            System.out.println("Error creating password: " + e.getMessage());
                        }

                        existingNotes.add(newNote);
                        System.out.println("Note added to your collection.");
                    } 

                //Delete existing note
                } else if (managerChoice == 2){
                    listNotes();
                    if (existingNotes.isEmpty()) break;

                    System.out.print("Enter Note name to delete: ");
                    String name = scan.nextLine();

                    boolean removed = existingNotes.removeIf(note -> note.getName().equals(name));
                    if (removed) {
                        System.out.println(name + " successfully deleted!");
                    } else {
                        System.out.println("Note not found.");
                    }
                } 
            // ── 2. EDIT NOTE ─────────────────────────────────────────────
            } else if (choice == 2) {
                listNotes();
                if (existingNotes.isEmpty()) continue;
 
                System.out.print("Enter Note name to edit: ");
                String name = scan.nextLine();
 
                Note target = findNote(name);
                if (target == null) {
                    System.out.println("Note not found.");
                    continue;
                }
 
                System.out.print("Enter Note password: ");
                String password = scan.nextLine();
                System.out.print("Enter content to append: ");
                String content = scan.nextLine();
 
                try {
                    target.editor(content, password);
                } catch (Exception e) {
                    System.out.println("Error editing note: " + e.getMessage());
                }
 
            // ── 3. DECRYPT NOTE ──────────────────────────────────────────
            } else if (choice == 3) {
                listNotes();
                if (existingNotes.isEmpty()) continue;
 
                System.out.print("Enter Note name to decrypt: ");
                String name = scan.nextLine();
 
                Note target = findNote(name);
                if (target == null) {
                    System.out.println("Note not found.");
                    continue;
                }
 
                if (!target.getIsEncrypted()) {
                    System.out.println("This note is not encrypted.");
                    continue;
                }
 
                System.out.print("Enter Note password: ");
                String password = scan.nextLine();
 
                try {
                    String decryptedContent = target.read(password);
                    if (decryptedContent != null) {
                        System.out.println("\n--- Decrypted Content ---");
                        System.out.println(decryptedContent);
                        System.out.println("-------------------------");
                    }
                } catch (Exception e) {
                    System.out.println("Error decrypting note: " + e.getMessage());
                }
 
            // ── 4. REMOVE PASSWORD ───────────────────────────────────────
            } else if (choice == 4) {
                listNotes();
                if (existingNotes.isEmpty()) continue;
 
                System.out.print("Enter Note name to remove password from: ");
                String name = scan.nextLine();
 
                Note target = findNote(name);
                if (target == null) {
                    System.out.println("Note not found.");
                    continue;
                }
 
                if (!target.getIsPasswordProtected()) {
                    System.out.println("This note has no password protection.");
                    continue;
                }
 
                System.out.print("Enter current password to confirm: ");
                String password = scan.nextLine();
 
                try {
                    target.removePassword(password);
                } catch (NoSuchAlgorithmException e) {
                    System.out.println("Error removing password: " + e.getMessage());
                }
 
            // ── 5. EXIT ──────────────────────────────────────────────────
            } else if (choice == 5) {
                System.out.println("Goodbye!");
 
            } else {
                System.out.println("Invalid choice. Please enter a number 1-5.");
            }
        }
        scan.close();
    }

    // Lists all notes with their status
    public static void listNotes(){
        int count = 0;
        System.out.println("\n----Current Notes----");
        if (existingNotes.size() == 0){
            System.out.println("\t None.");
        }
        for (Note note : existingNotes){
            count++;
            System.out.println(count + ". " + note.getName() + "\n\tDirectory: " + note.getDirectory() + "\n\tPassword Protection: " + note.getIsPasswordProtected() + "\n\tEncryption Status: " + note.getIsEncrypted());
        }
        
    }

    // Finds a Note by name. Returns null if not found.
    public static Note findNote(String name) {
        for (Note note : existingNotes) {
            if (note.getName().equals(name)) return note;
        }
        return null;
    }

    
}

class Credentials {
    private final HashMap<String, String> credentials;
    private String currentUsername;

    public Credentials() {
        credentials = new HashMap<String, String>();
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
    
    public Note(String name, String directory){
        this.name = name;
        this.directory = directory;
        this.isPasswordProtected = true;
        this.isEncrypted = false;
    }

    public String getName() {return name;}
    public String getDirectory() {return directory;}
    public boolean getIsPasswordProtected() {return isPasswordProtected;}
    public boolean getIsEncrypted() {return isEncrypted;}

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
                System.out.println(file + " already exists!");
                return false;
            }
            System.out.println("Note created at: " + file.getAbsolutePath());
            return true;
        } catch (IOException e){
            System.out.println("Error creating file: " + e.getMessage());
            return false;
        }
    }

    /* NOTE EDITOR  (append content)
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
 
