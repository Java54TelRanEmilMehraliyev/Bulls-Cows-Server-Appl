package telran;
import java.util.List;

import telran.net.Request;
import telran.net.Response;
import telran.net.ResponseCode;

public class BullsCowsProtocol implements telran.net.Protocol {
    private BullsCowsService service;

    public BullsCowsProtocol(BullsCowsService service) {
        this.service = service;
    }

    @Override
    public Response getResponse(Request request) {
        String command = request.requestType();
        String[] parameters = request.requestData().split(" ", 2);

        switch (command) {
            case "START":
                long gameId = service.createNewGame();
                return new Response(ResponseCode.SUCCESS, "Game started. Game ID: " + gameId);

            case "MOVE":
                if (parameters.length < 2) {
                    return new Response(ResponseCode.ERROR, "Invalid MOVE command");
                }
                long moveGameId = Long.parseLong(parameters[0]);
                String move = parameters[1];
                List<MoveResult> results = service.getResults(moveGameId, new Move(moveGameId, move));
                MoveResult lastResult = results.get(results.size() - 1);
                return new Response(ResponseCode.SUCCESS, "Bulls: " + lastResult.getBulls() + ", Cows: " + lastResult.getCows());

            case "RESULTS":
                if (parameters.length < 1) {
                    return new Response(ResponseCode.ERROR, "Invalid RESULTS command");
                }
                long resultsGameId = Long.parseLong(parameters[0]);
                StringBuilder result = new StringBuilder();
                for (MoveResult moveResult : service.getResults(resultsGameId, null)) {
                    result.append("Move: ").append(moveResult.getClientSequence())
                          .append(", Bulls: ").append(moveResult.getBulls())
                          .append(", Cows: ").append(moveResult.getCows()).append("\n");
                }
                return new Response(ResponseCode.SUCCESS, result.toString());

            default:
                return new Response(ResponseCode.ERROR, "Unknown command");
        }
    }
}