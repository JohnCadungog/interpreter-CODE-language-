import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import Analysis.Interpreter;
// import Analysis.Syntax.Parser;
// import Analysis.Syntax.Lexer;
// import Analysis.Type.TokenType;

public class App {
    public static void main(String[] args) {
        String codeFilePath = "D:\\School\\3rd year\\2nd Sem\\Programming  Language\\interpreter V2\\interpreter(CODE language)\\src\\CODE.txt"; // Update this with the path to your text file
        
        try {
            // Read the code from the text file
            String code = Files.readString(Paths.get(codeFilePath)).replace("\r", "");
            System.out.println(code);

            // Execute the interpreter
          
        
            Interpreter program = new Interpreter(code);
            program.execute(null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

        System.out.println("\nPress any key to continue...");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
