package telran;
import org.json.JSONObject;
import telran.net.Request;
import telran.net.Response;
import telran.net.ResponseCode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BullsCowsProtocol implements telran.net.Protocol {
    private Map<Long, Game> games;
    private long gameIdCounter;

    public BullsCowsProtocol() {
        games = new HashMap<>();
        gameIdCounter = 1;
    }

    @Override
    public Response getResponse(Request request) {
        String command = request.requestType();
        String[] parameters = request.requestData().split(" ", 2);

        switch (command) {
            case "START":
                long gameId = createNewGame();
                return new Response(ResponseCode.SUCCESS, "Game started. Game ID: " + gameId);

            case "MOVE":
                if (parameters.length < 2) {
                    return new Response(ResponseCode.ERROR, "Invalid MOVE command");
                }
                long moveGameId = Long.parseLong(parameters[0]);
                String move = parameters[1];
                return processMove(moveGameId, move);

            case "RESULTS":
                if (parameters.length < 1) {
                    return new Response(ResponseCode.ERROR, "Invalid RESULTS command");
                }
                long resultsGameId = Long.parseLong(parameters[0]);
                return getResults(resultsGameId);

            default:
                return new Response(ResponseCode.ERROR, "Unknown command");
        }
    }

    public String processRequest(String requestJSON) {
        JSONObject jsonObj = new JSONObject(requestJSON);
        String requestType = jsonObj.getString("requestType");
        String requestData = jsonObj.getString("requestData");
        
        Request request = new Request(requestType, requestData);
        Response response = getResponse(request);
        
        return response.toString();
    }

    private long createNewGame() {
        String serverSequence = generateRandomSequence();
        long id = gameIdCounter++;
        games.put(id, new Game(id, serverSequence));
        return id;
    }

    private Response processMove(long gameId, String clientSequence) {
        Game game = games.get(gameId);
        if (game == null) {
            return new Response(ResponseCode.ERROR, "Game not found");
        }

        Move move = new Move(gameId, clientSequence);
        List<MoveResult> results = game.moveProcess(move);
        MoveResult lastResult = results.get(results.size() - 1);
        return new Response(ResponseCode.SUCCESS, "Bulls: " + lastResult.getBulls() + ", Cows: " + lastResult.getCows());
    }

    private Response getResults(long gameId) {
        Game game = games.get(gameId);
        if (game == null) {
            return new Response(ResponseCode.ERROR, "Game not found");
        }

        StringBuilder result = new StringBuilder();
        for (MoveResult moveResult : game.getResults()) {
            result.append("Move: ").append(moveResult.getClientSequence())
                  .append(", Bulls: ").append(moveResult.getBulls())
                  .append(", Cows: ").append(moveResult.getCows()).append("\n");
        }

        return new Response(ResponseCode.SUCCESS, result.toString());
    }

    private String generateRandomSequence() {
        
        StringBuilder sequence = new StringBuilder();
        while (sequence.length() < 4) {
            int digit = (int) (Math.random() * 10);
            if (sequence.indexOf(String.valueOf(digit)) == -1) {
                sequence.append(digit);
            }
        }
        return sequence.toString();
    }
}