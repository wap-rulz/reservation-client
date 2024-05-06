package ds.adeesha.cw2;

import ds.adeesha.cw2.grpc.*;
import ds.adeesha.cw2.utility.Constants;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ReservationClient {
    private final String host;
    private final int port;
    private ManagedChannel channel;
    private GetItemServiceGrpc.GetItemServiceBlockingStub getItemServiceBlockingStub;
    private AddItemServiceGrpc.AddItemServiceBlockingStub addItemServiceBlockingStub;
    private UpdateItemServiceGrpc.UpdateItemServiceBlockingStub updateItemServiceBlockingStub;
    private RemoveItemServiceGrpc.RemoveItemServiceBlockingStub removeItemServiceBlockingStub;
    private ReserveItemServiceGrpc.ReserveItemServiceBlockingStub reserveItemServiceBlockingStub;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage ReservationClient <host> <port>");
            System.exit(1);
        }
        String host = args[0];
        int port = Integer.parseInt(args[1].trim());
        ReservationClient client = new ReservationClient(host, port);
        client.initializeConnection();
        client.processUserRequests(null);
        client.closeConnection();
    }

    private ReservationClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private void initializeConnection() {
        System.out.println("Initializing Connecting to server at " + host + ":" + port);
        channel = ManagedChannelBuilder
                .forAddress("localhost", port)
                .usePlaintext()
                .build();
        addItemServiceBlockingStub = AddItemServiceGrpc.newBlockingStub(channel);
        getItemServiceBlockingStub = GetItemServiceGrpc.newBlockingStub(channel);
        updateItemServiceBlockingStub = UpdateItemServiceGrpc.newBlockingStub(channel);
        removeItemServiceBlockingStub = RemoveItemServiceGrpc.newBlockingStub(channel);
        reserveItemServiceBlockingStub = ReserveItemServiceGrpc.newBlockingStub(channel);
    }

    private void closeConnection() {
        channel.shutdown();
    }

    private void processUserRequests(Scanner scanner) {
        System.out.println("\nWelcome to Reservation Client");
        if (scanner == null) {
            scanner = new Scanner(System.in);
        }
        String loginType = userLogin(scanner);
        switch (loginType) {
            case Constants.USER_TYPE_SELLER:
                processSellerRequests(scanner);
                break;
            case Constants.USER_TYPE_CUSTOMER:
                processCustomerRequests(scanner);
                break;
            default:
                System.out.println("Invalid login type. Please try again.");
                processUserRequests(scanner);
                break;
        }
    }

    private String readInput(Scanner scanner) {
        String input = scanner.nextLine().trim();
        if (input.equals(Constants.INPUT_EXIT)) {
            System.out.println("User requested System Exit");
            System.exit(1);
        }
        return input;
    }

    private String userLogin(Scanner scanner) {
        System.out.println("\nPlease select your login type:");
        System.out.println("1 - Seller");
        System.out.println("2 - Customer");
        System.out.println("--Enter 0 to exit--");
        System.out.println("\nEnter login type:");
        return readInput(scanner);
    }

    private void processSellerRequests(Scanner scanner) {
        System.out.println("Processing seller requests");
        System.out.println("\nSeller logged in");
        System.out.println("\nPlease select your action:");
        System.out.println("1 - List Items");
        System.out.println("2 - Add Item");
        System.out.println("3 - Update Item");
        System.out.println("4 - Remove Item");
        System.out.println("--Enter 0 to exit--");
        System.out.println("\nEnter input:");
        String action = readInput(scanner);
        processServiceRequests(action, scanner, Constants.USER_TYPE_SELLER);
    }

    private void processCustomerRequests(Scanner scanner) {
        System.out.println("Processing customer requests");
        System.out.println("\nCustomer logged in");
        System.out.println("\nPlease select your action:");
        System.out.println("1 - List Items");
        System.out.println("5 - Reserve Item");
        System.out.println("--Enter 0 to exit--");
        System.out.println("\nEnter input:");
        String action = readInput(scanner);
        processServiceRequests(action, scanner, Constants.USER_TYPE_CUSTOMER);
    }

    private void processServiceRequests(String requestType, Scanner scanner, String userType) {
        switch (requestType) {
            case Constants.GET_ITEMS:
                getItems();
                break;
            case Constants.ADD_ITEM:
                addItem(scanner, userType);
                break;
            case Constants.UPDATE_ITEM:
                updateItem(scanner, userType);
                break;
            case Constants.REMOVE_ITEM:
                removeItem(scanner, userType);
                break;
            case Constants.RESERVE_ITEM:
                reserveItem(scanner, userType);
                break;
            default:
                System.out.println("Invalid service type. Please try again.");
                break;
        }
        processUserRequests(scanner);
    }

    private void getItems() {
        System.out.println("Processing get items request");
        GetItemRequest request = GetItemRequest.newBuilder().build();
        System.out.println("Sending get item request");
        GetItemResponse response = getItemServiceBlockingStub.getItems(request);
        System.out.println("Response received with status: " + response.getStatus());
        printAllItems(response.getItemsList());
    }

    private void printAllItems(List<Item> items) {
        if (items == null || items.isEmpty()) {
            System.out.println("<Items are empty>");
        } else {
            for (Item item : items) {
                System.out.println("\nItem ID: " + item.getId());
                System.out.println("Name: " + item.getName());
                System.out.println("Description: " + item.getDescription());
                System.out.println("Price: " + item.getPrice());
                Map<String, String> reservationDates = item.getReservationsMap();
                if (reservationDates.isEmpty()) {
                    System.out.println("Reservation dates: <None>");
                } else {
                    System.out.println("Reservation dates: ");
                    for (Map.Entry<String, String> entry : reservationDates.entrySet()) {
                        System.out.println("\tCustomer No: " + entry.getKey() + " - " + " Date: " + entry.getValue());
                    }
                }
            }
        }
    }

    private void addItem(Scanner scanner, String userType) {
        if (!userType.equals(Constants.USER_TYPE_SELLER)) {
            System.out.println("User not authorized!");
            return;
        }
        Item item = getItemDetails(scanner);
        AddItemRequest request = AddItemRequest
                .newBuilder()
                .setItem(item)
                .build();
        System.out.println("Sending add item request");
        AddItemResponse response = addItemServiceBlockingStub.addItem(request);
        System.out.println("Response received with status: " + response.getStatus());
    }

    private Item getItemDetails(Scanner scanner) {
        System.out.println("\nEnter item id: ");
        String id = readInput(scanner);
        System.out.println("Enter item name: ");
        String name = readInput(scanner);
        System.out.println("Enter item description: ");
        String description = readInput(scanner);
        System.out.println("Enter item price: ");
        double price = Double.parseDouble(readInput(scanner));
        return Item.newBuilder()
                .setId(id)
                .setName(name)
                .setDescription(description)
                .setPrice(price)
                .build();
    }

    private void updateItem(Scanner scanner, String userType) {
        if (!userType.equals(Constants.USER_TYPE_SELLER)) {
            System.out.println("User not authorized!");
            return;
        }
        Item item = getItemDetails(scanner);
        UpdateItemRequest request = UpdateItemRequest
                .newBuilder()
                .setItem(item)
                .build();
        System.out.println("Sending update item request");
        UpdateItemResponse response = updateItemServiceBlockingStub.updateItem(request);
        System.out.println("Response received with status: " + response.getStatus());
    }

    private void removeItem(Scanner scanner, String userType) {
        if (!userType.equals(Constants.USER_TYPE_SELLER)) {
            System.out.println("User not authorized!");
            return;
        }
        System.out.println("Enter item id to remove: ");
        String id = readInput(scanner);
        RemoveItemRequest request = RemoveItemRequest
                .newBuilder()
                .setId(id)
                .build();
        System.out.println("Sending remove item request");
        RemoveItemResponse response = removeItemServiceBlockingStub.removeItem(request);
        System.out.println("Response received with status: " + response.getStatus());
    }

    private void reserveItem(Scanner scanner, String userType) {
        if (!userType.equals(Constants.USER_TYPE_CUSTOMER)) {
            System.out.println("User not authorized!");
            return;
        }
        System.out.println("Enter your customer no: ");
        String cusNo = readInput(scanner);
        System.out.println("Enter item id to reserve: ");
        String id = readInput(scanner);
        System.out.println("Enter date of reservation: ");
        String date = readInput(scanner);
        ReserveItemRequest request = ReserveItemRequest
                .newBuilder()
                .setId(id)
                .setCustomerNo(cusNo)
                .setReservationDate(date)
                .build();
        System.out.println("Sending reserve item request");
        ReserveItemResponse response = reserveItemServiceBlockingStub.reserveItem(request);
        System.out.println("Response received with status: " + response.getStatus());
    }
}
