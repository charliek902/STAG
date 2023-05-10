package edu.uob;


import com.alexmerz.graphviz.ParseException;
import com.alexmerz.graphviz.objects.Edge;
import com.alexmerz.graphviz.objects.Graph;
import org.xml.sax.SAXException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Arrays;


/** This class implements the STAG server. */
public final class GameServer {

    private GameEngine gameEngine;
    private GameMap map;
    private HashMap<String, HashSet<GameAction>> actionsMap;
    List<String> gameEntities;

    private static final char END_OF_TRANSMISSION = 4;

    public static void main(String[] args) throws Exception {
        File entitiesFile = Paths.get("config" + File.separator + "extended-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "extended-actions.xml").toAbsolutePath().toFile();
        GameServer server = new GameServer(entitiesFile, actionsFile);
        server.blockingListenOn(8888);
    }

    /**
    * KEEP this signature (i.e. {@code edu.uob.GameServer(File, File)}) otherwise we won't be able to mark
    * your submission correctly.
    *
    * <p>You MUST use the supplied {@code entitiesFile} and {@code actionsFile}
    *
    * @param entitiesFile The game configuration file containing all game entities to use in your game
    * @param actionsFile The game configuration file containing all game actions to use in your game
    *
    */

    public GameServer(File entitiesFile, File actionsFile) throws Exception {

        try {
            ActionsParserAndLoader ParsedActionFile = new ActionsParserAndLoader(actionsFile);
            EntitiesParserAndLoader parsedEntities = new EntitiesParserAndLoader(entitiesFile);
            List<Graph> locations = parsedEntities.getLocations();
            List<Edge> paths = parsedEntities.getPaths();
            this.actionsMap = ParsedActionFile.getActionsMap();
            this.map = new GameMap(locations, paths);
            this.gameEntities = map.getEntities();
            if(!ValidEntities(gameEntities)){
                throw new Exception("Invalid game entities in entities file");
            }
            this.gameEngine = new GameEngine(map, actionsMap);
        }
        catch(ParseException e){
            throw new ParseException("Problem parsing the files" + e.getMessage());
        }
        catch (FileNotFoundException e){
            throw new FileNotFoundException("File is not found " + e.getMessage());
        }
        catch (IOException e){
            throw new IOException("Input output exception: " + e.getMessage());
        }
        catch (RuntimeException e){
            throw new RuntimeException("Runtime error: " + e.getMessage());
        }
        catch (SAXException e){
            throw new SAXException("Problem parsing xml action file: " + e.getMessage());
        }
        catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    /**
    * KEEP this signature (i.e. {@code edu.uob.GameServer.handleCommand(String)}) otherwise we won't be
    * able to mark your submission correctly.
    *
    * <p>This method handles all incoming game commands and carries out the corresponding actions.
    */
    public String handleCommand(String command) {
        String lowerCaseInputCommand = command.toLowerCase();
        return this.gameEngine.handleCommand(lowerCaseInputCommand, map);
    }

    /* This method checks the names of the entities and returns false if any of them are the inbuilt keywords */
    private boolean ValidEntities(List<String> gameEntities) {
        List<String> disallowedKeywords = Arrays.asList("health", "goto", "look", "inv", "inventory", "drop", "get");
        for (String entity : gameEntities) {
            if (disallowedKeywords.contains(entity)) {
                return false;
            }
        }
        return true;
    }

    //  === Methods below are there to facilitate server related operations. ===

    /**
    * Starts a *blocking* socket server listening for new connections. This method blocks until the
    * current thread is interrupted.
    *
    * <p>This method isn't used for marking. You shouldn't have to modify this method, but you can if
    * you want to.
    *
    * @param portNumber The port to listen on.
    * @throws IOException If any IO related operation fails.
    */
    public void blockingListenOn(int portNumber) throws IOException {
        try (ServerSocket s = new ServerSocket(portNumber)) {
            System.out.println("Server listening on port " + portNumber);
            while (!Thread.interrupted()) {
                try {
                    blockingHandleConnection(s);
                } catch (IOException e) {
                    System.out.println("Connection closed");
                }
            }
        }
    }

    /**
    * Handles an incoming connection from the socket server.
    *
    * <p>This method isn't used for marking. You shouldn't have to modify this method, but you can if
    * * you want to.
    *
    * @param serverSocket The client socket to read/write from.
    * @throws IOException If any IO related operation fails.
    */
    private void blockingHandleConnection(ServerSocket serverSocket) throws IOException {
        try (Socket s = serverSocket.accept();
        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {
            System.out.println("Connection established");
            String incomingCommand = reader.readLine();
            if(incomingCommand != null) {
                System.out.println("Received message from " + incomingCommand);
                String result = handleCommand(incomingCommand);
                writer.write(result);
                writer.write("\n" + END_OF_TRANSMISSION + "\n");
                writer.flush();
            }
        }
    }
}
