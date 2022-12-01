package com.lox;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    static boolean hadError = false;
    static boolean hadRuntimeError = false;
    private static final Interpreter interpreter = new Interpreter();
    public static void main(String[] args) throws IOException {
        if(args.length > 1){
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        }else if(args.length == 1){
            runFile(args[0]);
        }else {
            runPrompt();
        }
    }
    private static void runFile(String path) throws IOException{
        byte[] bytes = Files.readAllBytes(Paths.get(path)); // get all bytes of source file
        run(new String(bytes , Charset.defaultCharset()));
        if(hadError){
            // If we have a error , we should exit with an exit code
            System.exit(65);
        }
        if(hadRuntimeError){
            System.exit(70);
        }
    }
    private static void runPrompt() throws IOException{
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        while(true){
            System.out.print("> ");
            String line = reader.readLine();
            if(line == null)break;
            run(line);
            // A line error shouldn't terminate the interactive environment
            hadError = false;
        }
    }
    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
//        for(Token token : tokens){
//            System.out.println(token);
//        }
        Parser parser = new Parser(tokens);
        Expr expr = parser.parser();
        if(hadError)return;
//        System.out.println(new AstPrinter().print(expr));
        interpreter.interpret(expr);
    }
    static void error(int line , String message){
        report(line,"" , message);
    }
    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() +
                "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }
    private static void report(int line , String where , String message){
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message
        );
        hadError = true;
    }
    static void error(Token token ,String message){
        if(token.type == TokenType.EOF){
            report(token.line,  " at end" , message);
        }else{
            report(token.line, "at '"+token.lexeme+"'",message);
        }
    }
}