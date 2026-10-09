package ecobite.model;

import java.io.Serializable;

public class FoodRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum RequestType {
        NGO,
        SHELTER
    }

    private int requestId;
    private int foodId;
    private String foodName;
    private String requesterName;
    private String organizationName;
    private String phone;
    private RequestType requestType;
    private String requestTime;
    private String status;

    public FoodRequest() {
    }

    public FoodRequest(int requestId, int foodId, String foodName, String requesterName,
                      String organizationName, String phone, RequestType requestType,
                      String requestTime, String status) {
        this.requestId = requestId;
        this.foodId = foodId;
        this.foodName = foodName;
        this.requesterName = requesterName;
        this.organizationName = organizationName;
        this.phone = phone;
        this.requestType = requestType;
        this.requestTime = requestTime;
        this.status = status;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getFoodId() {
        return foodId;
    }

    public void setFoodId(int foodId) {
        this.foodId = foodId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
