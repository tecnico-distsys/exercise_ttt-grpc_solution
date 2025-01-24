package pt.tecnico.ttt.server;

import io.grpc.stub.StreamObserver;
import java.util.WeakHashMap;
import pt.tecnico.ttt.*;
import static io.grpc.Status.INVALID_ARGUMENT;

public class TTTServiceImpl extends TTTGrpc.TTTImplBase {

	/** Game implementation. */
	private TTTGame ttt = new TTTGame();

	@Override
	public void currentBoard(CurrentBoardRequest request, StreamObserver<CurrentBoardResponse> responseObserver) {
		// StreamObserver is used to represent the gRPC stream between the server and
		// client in order to send the appropriate responses (or errors, if any occur).

		CurrentBoardResponse response = CurrentBoardResponse.newBuilder().setBoard(ttt.toString()).build();

		// Send a single response through the stream.
		responseObserver.onNext(response);
		// Notify the client that the operation has been completed.
		responseObserver.onCompleted();
	}

	@Override
	public void play(PlayRequest request, StreamObserver<PlayResponse> responseObserver) {
		int row = request.getRow();
		int column = request.getColumn();
		int player = request.getPlayer();

		PlayResult playResult = ttt.play(row, column, player);

		if(playResult == PlayResult.OUT_OF_BOUNDS) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Input has to be a valid position").asRuntimeException());
		} else {
			PlayResponse response = PlayResponse.newBuilder().setPlayResult(playResult).build();
			// Send a single response through the stream.
			responseObserver.onNext(response);
			// Notify the client that the operation has been completed.
			responseObserver.onCompleted();
		}
	}

	@Override
	public void checkWinner(CheckWinnerRequest request, StreamObserver<CheckWinnerResponse> responseObserver) {

		CheckWinnerResponse response = CheckWinnerResponse.newBuilder().setResult(ttt.checkWinner()).build();

		// Send a single response through the stream.
		responseObserver.onNext(response);
		// Notify the client that the operation has been completed.
		responseObserver.onCompleted();
	}

	@Override
	public void waitForWinner(WaitForWinnerRequest request, StreamObserver<WaitForWinnerResponse> responseObserver) {

		WaitForWinnerResponse response = WaitForWinnerResponse.newBuilder().setResult(ttt.waitForWinner()).build();

		// Send a single response through the stream.
		responseObserver.onNext(response);
		// Notify the client that the operation has been completed.
		responseObserver.onCompleted();
	}

	@Override
	public void resetBoard(ResetBoardRequest request, StreamObserver<ResetBoardResponse> responseObserver){
		// Call the resetBoard method from the game
		ttt.resetBoard();
		// Create the response
		ResetBoardResponse response = ResetBoardResponse.getDefaultInstance();
		// Send the response
		responseObserver.onNext(response);
		// Notify the client that the operation has been completed.
		responseObserver.onCompleted();
	}
}
