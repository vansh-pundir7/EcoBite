package ecobite.service;

import ecobite.model.FoodRequest;
import ecobite.storage.FoodRequestStorage;

import java.util.ArrayList;
import java.util.List;

public class FoodRequestService {
    private final FoodRequestStorage storage;

    public FoodRequestService() {
        this.storage = new FoodRequestStorage();
    }

    public void addRequest(FoodRequest request) {
        List<FoodRequest> requests = storage.loadList(FoodRequestStorage.FILE_NAME);
        for (FoodRequest existingRequest : requests) {
            if (existingRequest.getRequestId() == request.getRequestId()) {
                throw new IllegalArgumentException("Request ID already exists: " + request.getRequestId());
            }
        }
        requests.add(request);
        storage.saveList(FoodRequestStorage.FILE_NAME, requests);
    }

    public void updateRequestStatus(int requestId, String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Request status is required.");
        }

        List<FoodRequest> requests = storage.loadList(FoodRequestStorage.FILE_NAME);
        FoodRequest target = null;
        for (FoodRequest request : requests) {
            if (request.getRequestId() == requestId) {
                target = request;
                break;
            }
        }
        if (target == null) {
            throw new IllegalArgumentException("Request record " + requestId + " was not found.");
        }

        target.setStatus(status);
        storage.saveList(FoodRequestStorage.FILE_NAME, requests);
    }

    public List<FoodRequest> getAllRequests() {
        return storage.loadList(FoodRequestStorage.FILE_NAME);
    }

    public List<FoodRequest> getPendingRequests() {
        List<FoodRequest> pendingRequests = new ArrayList<>();
        for (FoodRequest request : getAllRequests()) {
            if (request.getStatus() != null && request.getStatus().equals("PENDING")) {
                pendingRequests.add(request);
            }
        }
        return pendingRequests;
    }
}
