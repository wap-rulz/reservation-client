package ds.adeesha.cw2;

import ds.adeesha.cw2.grpc.*;
import ds.adeesha.cw2.utility.Constants;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Scanner;

public class ReservationClient {
    private static final Logger logger = LogManager.getLogger(ReservationClient.class);

    private final String host;
    private final int port;
    private ManagedChannel channel;
    private GetItemServiceGrpc.GetItemServiceBlockingStub getItemServiceBlockingStub;
    private AddItemServiceGrpc.AddItemServiceBlockingStub addItemServiceBlockingStub;
    private UpdateItemServiceGrpc.UpdateItemServiceBlockingStub updateItemServiceBlockingStub;
    private RemoveItemServiceGrpc.RemoveItemServiceBlockingStub removeItemServiceBlockingStub;


    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage ReservationClient <host> <port>");
            System.exit(1);
        }
        String host = args[0];
        int port = Integer.parseInt(args[1].trim());
        ReservationClient client = new ReservationClient(host, port);
        client.initializeConnection();
        client.processUserRequests();
        client.closeConnection();
    }

    private ReservationClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private void initializeConnection() {
        logger.info("Initializing Connecting to server at {}:{}", host, port);
        channel = ManagedChannelBuilder
                .forAddress("localhost", port)
                .usePlaintext()
                .build();
        addItemServiceBlockingStub = AddItemServiceGrpc.newBlockingStub(channel);
        getItemServiceBlockingStub = GetItemServiceGrpc.newBlockingStub(channel);
        updateItemServiceBlockingStub = UpdateItemServiceGrpc.newBlockingStub(channel);
        removeItemServiceBlockingStub = RemoveItemServiceGrpc.newBlockingStub(channel);
    }

    private void closeConnection() {
        channel.shutdown();
    }

    private void processUserRequests() {
        logger.info("Processing user requests");
        Scanner scanner = new Scanner(System.in);
        System.out.println("\nWelcome to Reservation Client");
        String loginType = userLogin(scanner);
        switch (loginType) {
            case Constants.USER_TYPE_SELLER:
                processSellerRequests(scanner);
                break;
            case Constants.USER_TYPE_CUSTOMER:
                processCustomerRequests(scanner);
                break;
            case Constants.USER_TYPE_INVENTORY_SYSTEM_CLERK:
                processClerkRequests(scanner);
                break;
            case Constants.USER_TYPE_WORKSHOP_MANAGER:
                processManagerRequests(scanner);
                break;
            default:
                System.out.println("Invalid login type. Please try again.");
                logger.error("Invalid login type: {}", loginType);
                processUserRequests();
                break;
        }
    }

    private String readInput(Scanner scanner) {
        String input = scanner.nextLine().trim();
        if (input.equals(Constants.INPUT_EXIT)) {
            logger.warn("User requested System Exit");
            System.exit(1);
        }
        logger.info("User input: {}", input);
        return input;
    }

    private String userLogin(Scanner scanner) {
        System.out.println("\nPlease select your login type:");
        System.out.println("1 - Seller");
        System.out.println("2 - Customer");
        System.out.println("3 - Inventory System Clerk");
        System.out.println("4 - Workshop Manager");
        System.out.println("--Enter 0 to exit--");
        System.out.println("\nEnter login type:");
        return readInput(scanner);
    }

    private void processSellerRequests(Scanner scanner) {
        logger.info("Processing seller requests");
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
        logger.info("Processing customer requests");
        System.out.println("\nCustomer logged in");
        System.out.println("\nPlease select your action:");
        System.out.println("1 - List Items");
        System.out.println("5 - Reserve Item");
        System.out.println("--Enter 0 to exit--");
        System.out.println("\nEnter input:");
        String action = readInput(scanner);
        processServiceRequests(action, scanner, Constants.USER_TYPE_CUSTOMER);
    }

    private void processClerkRequests(Scanner scanner) {
        logger.info("Processing inventory system clerk requests");
        System.out.println("\nInventory System Clerk logged in");
        System.out.println("\nPlease select your action:");
        System.out.println("1 - List Items");
        System.out.println("7 - Update Stock");
        System.out.println("--Enter 0 to exit--");
        System.out.println("\nEnter input:");
        String action = readInput(scanner);
        processServiceRequests(action, scanner, Constants.USER_TYPE_INVENTORY_SYSTEM_CLERK);
    }

    private void processManagerRequests(Scanner scanner) {
        logger.info("Processing manager requests");
        System.out.println("\nManager logged in");
        System.out.println("\nPlease select your action:");
        System.out.println("1 - List Items");
        System.out.println("7 - Order Items");
        System.out.println("--Enter 0 to exit--");
        System.out.println("\nEnter input:");
        String action = readInput(scanner);
        processServiceRequests(action, scanner, Constants.USER_TYPE_WORKSHOP_MANAGER);
    }

    //todo: validate service based on role
    private void processServiceRequests(String requestType, Scanner scanner, String userType) {
        switch (requestType) {
            case Constants.GET_ITEMS:
                getItems();
                break;
            case Constants.ADD_ITEM:
                addItem(scanner);
                break;
            case Constants.UPDATE_ITEM:
                updateItem(scanner);
                break;
            case Constants.REMOVE_ITEM:
                removeItem(scanner);
                break;
            case Constants.RESERVE_ITEM:
//                reserveItem(scanner); todo
                break;
            case Constants.ORDER_ITEM:
//                orderItem(scanner); todo
                break;
            case Constants.UPDATE_STOCK:
//                updateStock(scanner); todo
            default:
                System.out.println("Invalid service type. Please try again.");
                logger.error("Invalid service type: {}", requestType);
                break;
        }
        processUserRequests();
    }

    private void getItems() {
        logger.info("Processing get items request");
        GetItemRequest request = GetItemRequest.newBuilder().build();
        logger.info("Sending get item request");
        GetItemResponse response = getItemServiceBlockingStub.getItems(request);
        logger.info("Response received with status: {}", response.getStatus());
        printAllItems(response.getItemsList());
    }

    private void printAllItems(List<Item> items) {
        for (Item item : items) {
            System.out.println("\nItem ID: " + item.getId());
            System.out.println("Name: " + item.getName());
            System.out.println("Description: " + item.getDescription());
            System.out.println("Quantity: " + item.getQty());
            System.out.println("Price: " + item.getPrice());
            System.out.println("Availability: " + (item.getIsAvailable() ? "Available" : "Not Available"));
        }
    }

    private void addItem(Scanner scanner) {
        logger.info("Processing add item request");
        logger.info("Inquiring new item details");
        Item item = getItemDetails(scanner);
        AddItemRequest request = AddItemRequest
                .newBuilder()
                .setItem(item)
                .build();
        logger.info("Sending add item request");
        AddItemResponse response = addItemServiceBlockingStub.addItem(request);
        logger.info("Response received with status: {}", response.getStatus());
    }

    private Item getItemDetails(Scanner scanner) {
        System.out.println("\nEnter item id: ");
        String id = readInput(scanner);
        System.out.println("Enter item name: ");
        String name = readInput(scanner);
        System.out.println("Enter item description: ");
        String description = readInput(scanner);
        System.out.println("Enter item quantity: ");
        int qty = Integer.parseInt(readInput(scanner));
        System.out.println("Enter item price: ");
        double price = Double.parseDouble(readInput(scanner));
        return Item.newBuilder()
                .setId(id)
                .setName(name)
                .setDescription(description)
                .setQty(qty)
                .setPrice(price)
                .setIsAvailable(true)
                .build();
    }

    private void updateItem(Scanner scanner) {
        logger.info("Processing update item request");
        logger.info("Inquiring item details to update");
        Item item = getItemDetails(scanner);
        UpdateItemRequest request = UpdateItemRequest
                .newBuilder()
                .setItem(item)
                .build();
        logger.info("Sending update item request");
        UpdateItemResponse response = updateItemServiceBlockingStub.updateItem(request);
        logger.info("Response received with status: {}", response.getStatus());
    }

    private void removeItem(Scanner scanner) {
        logger.info("Processing remove item request");
        System.out.println("Enter item id to remove: ");
        String id = readInput(scanner);
        RemoveItemRequest request = RemoveItemRequest
                .newBuilder()
                .setId(id)
                .build();
        logger.info("Sending remove item request");
        RemoveItemResponse response = removeItemServiceBlockingStub.removeItem(request);
        logger.info("Response received with status: {}", response.getStatus());
    }
}
