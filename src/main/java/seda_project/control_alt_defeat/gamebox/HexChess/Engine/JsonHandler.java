package seda_project.control_alt_defeat.gamebox.HexChess.Engine;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import seda_project.control_alt_defeat.gamebox.HexChess.Controller.BoardDesigner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Who is Json?
 * Why is he important?
 */
public class JsonHandler {
    public JsonHandler(){}
    private final ObjectMapper jsonMapper = new ObjectMapper();

    private final File externalSaveFile = new File("BoardDesign.json");

    /**
     * Reads all Board States and then visualizes them on the Scene
     * @return
     */
    public List<BoardDesignState> readBoardStates(){
        List<BoardDesignState> listofBoards = new ArrayList<>();
        try {
            String path = "/HexChess/BoardDesign.json";
            JsonNode nodes = null;

            if (externalSaveFile.exists()) {
                try {
                    nodes = jsonMapper.readTree(externalSaveFile);
                } catch (IOException e) {
                    System.err.println("BoardDesign.json is corrupted. Resetting to defaults.");
                    nodes = jsonMapper.createArrayNode();
                }
            }
            else {
                try (InputStream is = BoardDesigner.class.getResourceAsStream(path)) {
                    if (is != null){
                        nodes = jsonMapper.readTree(is);
                    }
                }
            }

            if (nodes == null || nodes.isMissingNode()) {
                nodes = jsonMapper.createArrayNode();
            }
            for (JsonNode x : nodes){
                listofBoards.add(new BoardDesignState(x));
            }
        } catch ( IOException e) {
            throw new RuntimeException(e);
        }
        return listofBoards;
    }

    /**
     * Create a new Bord State on basis of one FenLike notation and the information of the pieceamounts
     * @param notation
     * @param pieceAmounts
     * @param starting
     * @return
     */
    public BoardDesignState createNewState(String notation, Map<String, Integer> pieceAmounts, int starting){
        ObjectNode newBoard = jsonMapper.createObjectNode();
        newBoard.put("FENState", notation);
        newBoard.put("startingPlayer", starting);
        // add 0 for missing keys
        newBoard.put("p1Pawn", pieceAmounts.getOrDefault("p1PawnImg", 0));
        newBoard.put("p1Rook", pieceAmounts.getOrDefault("p1RookImg", 0));
        newBoard.put("p1Knight", pieceAmounts.getOrDefault("p1KnightImg", 0));
        newBoard.put("p1Bishop", pieceAmounts.getOrDefault("p1BishopImg", 0));
        newBoard.put("p1Queen", pieceAmounts.getOrDefault("p1QueenImg", 0));
        newBoard.put("p1King", pieceAmounts.getOrDefault("p1KingImg", 0));
        newBoard.put("p2Pawn", pieceAmounts.getOrDefault("p2PawnImg", 0));
        newBoard.put("p2Rook", pieceAmounts.getOrDefault("p2RookImg", 0));
        newBoard.put("p2Knight", pieceAmounts.getOrDefault("p2KnightImg", 0));
        newBoard.put("p2Bishop", pieceAmounts.getOrDefault("p2BishopImg", 0));
        newBoard.put("p2Queen", pieceAmounts.getOrDefault("p2QueenImg", 0));
        newBoard.put("p2King", pieceAmounts.getOrDefault("p2KingImg", 0));

        return new BoardDesignState(newBoard);
    }

    /**
     * Method to write a Board State to a JSON file.
     * @param listofBoards
     */
    public void writeBoardStates(List<BoardDesignState> listofBoards) {
        ArrayNode arrayNode = jsonMapper.createArrayNode();
        listofBoards.forEach(board -> arrayNode.add(board.getBoard()));

        try {
            // always write to the external file
            jsonMapper.writerWithDefaultPrettyPrinter().writeValue(externalSaveFile, arrayNode);
        } catch (IOException e) {
            System.err.println("Failed to write to BoardDesign.json: " + e.getMessage());
        }
    }
}
