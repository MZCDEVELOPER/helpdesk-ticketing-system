package com.helpdesk;

import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;

import java.util.List;

import com.helpdesk.database.DatabaseManager;

import com.helpdesk.model.Ticket;

import com.helpdesk.model.TicketComment;

import com.helpdesk.model.User;

import com.helpdesk.repository.TicketCommentRepository;

import com.helpdesk.repository.TicketRepository;

import com.helpdesk.repository.UserRepository;

import javafx.application.Application;

import javafx.beans.property.SimpleStringProperty;

import javafx.collections.FXCollections;

import javafx.collections.ObservableList;

import javafx.collections.transformation.FilteredList;

import javafx.geometry.Insets;

import javafx.geometry.Pos;

import javafx.scene.Scene;

import javafx.scene.control.Alert;

import javafx.scene.control.Button;

import javafx.scene.control.ComboBox;

import javafx.scene.control.Label;

import javafx.scene.control.PasswordField;

import javafx.scene.control.ScrollPane;

import javafx.scene.control.TableColumn;

import javafx.scene.control.TableView;

import javafx.scene.control.TextArea;

import javafx.scene.control.TextField;

import javafx.scene.control.cell.PropertyValueFactory;

import javafx.scene.layout.BorderPane;

import javafx.scene.layout.GridPane;

import javafx.scene.layout.HBox;

import javafx.scene.layout.VBox;

import javafx.stage.Stage;

import javafx.util.StringConverter;

public class Main extends Application {

    private final ObservableList<Ticket> tickets =

            FXCollections.observableArrayList();

    private final TicketRepository ticketRepository =

            new TicketRepository();

    private final UserRepository userRepository =

            new UserRepository();

    private final TicketCommentRepository commentRepository =
            new TicketCommentRepository();

    private Stage primaryStage;

    private User currentUser;


    // Dashboard statistic labels
    private final Label totalTicketsLabel =
            new Label();

    private final Label openTicketsLabel =
            new Label();

    private final Label inProgressTicketsLabel =
            new Label();

    private final Label resolvedTicketsLabel =
            new Label();

    private final Label closedTicketsLabel =
            new Label();


    /* Override */
    public void start(Stage stage) {
        primaryStage = stage;

        DatabaseManager.initializeDatabase();

        userRepository.createDefaultUsers();

        showLoginScreen();

        primaryStage.show();

    }

    /* Displays the login screen */

    private void showLoginScreen() {

        currentUser = null;

        tickets.clear();

        Label applicationTitle =

                new Label("Help Desk Ticketing System");

        applicationTitle.setStyle(

                "-fx-font-size: 26px;"

                + "-fx-font-weight: bold;"

        );

        Label loginTitle =

                new Label("User Login");

        loginTitle.setStyle(

                "-fx-font-size: 18px;"

                + "-fx-font-weight: bold;"

        );

        Label usernameLabel =

                new Label("Username:");

        TextField usernameField =

                new TextField();

        usernameField.setPromptText(

                "Enter username"

        );

        usernameField.setMaxWidth(250);

        Label passwordLabel =

                new Label("Password:");

        PasswordField passwordField =

                new PasswordField();

        passwordField.setPromptText(

                "Enter password"

        );

        passwordField.setMaxWidth(250);

        Button loginButton =

                new Button("Login");

        loginButton.setDefaultButton(true);

        loginButton.setOnAction(event -> {

            String username =

                    usernameField

                            .getText()

                            .trim();

            String password =

                    passwordField

                            .getText();

            if (username.isEmpty()

                    || password.isEmpty()) {

                showAlert(

                        Alert.AlertType.WARNING,

                        "Login Required",

                        "Username and password are required.",

                        "Please enter your username and password."

                );

                return;

            }

            User authenticatedUser =

                    userRepository.authenticate(

                            username,

                            password

                    );

            if (authenticatedUser == null) {

                showAlert(

                        Alert.AlertType.ERROR,

                        "Login Failed",

                        "Invalid username or password.",

                        "Please check your credentials and try again."

                );

                passwordField.clear();

                return;

            }

            currentUser =

                    authenticatedUser;

            System.out.println(

                    "Login successful: "

                    + currentUser.getUsername()

                    + " ("

                    + currentUser.getRole()

                    + ")"

            );

            loadTicketsForCurrentUser();

            showDashboard();

        });

        VBox loginBox =

                new VBox(12);

        loginBox.setAlignment(

                Pos.CENTER

        );

        loginBox.setPadding(

                new Insets(40)

        );

        loginBox.getChildren().addAll(

                applicationTitle,

                loginTitle,

                usernameLabel,

                usernameField,

                passwordLabel,

                passwordField,

                loginButton

        );

        Scene loginScene =

                new Scene(

                        loginBox,

                        500,

                        450

                );

        primaryStage.setScene(

                loginScene

        );

        primaryStage.setTitle(

                "Help Desk Ticketing System - Login"

        );

        usernameField.requestFocus();

    }

    /* Loads tickets based on the current user's role */

    private void loadTicketsForCurrentUser() {

        tickets.clear();

        if (currentUser == null) {

            return;

        }

        if (currentUser

                .getRole()

                .equals("Employee")) {

            tickets.addAll(

                    ticketRepository.findByCreatedBy(

                            currentUser.getId()

                    )

            );

        } else {

            tickets.addAll(

                    ticketRepository.findAll()

            );

        }

    }

    /* Determines whether the current user can manage tickets */

    private boolean canManageTickets() {

        if (currentUser == null) {

            return false;

        }

        return currentUser

                .getRole()

                .equals("Technician")

                ||

                currentUser

                        .getRole()

                        .equals("Administrator");

    }

    /* Displays the main dashboard */

    private void showDashboard() {

        BorderPane root =

                new BorderPane();

        root.setPadding(

                new Insets(20)

        );

        Label titleLabel =

                new Label(

                        "Help Desk Ticketing System"

                );

        titleLabel.setStyle(

                "-fx-font-size: 24px;"

                + "-fx-font-weight: bold;"

        );

        Label userLabel =

                new Label(

                        "Welcome, "

                        + currentUser.getName()

                        + " ("

                        + currentUser.getRole()

                        + ")"

                );

        Label permissionLabel =

                new Label();

        if (currentUser

                .getRole()

                .equals("Employee")) {

            permissionLabel.setText(

                    "Employee access: Create, view, "

                    + "and comment on your tickets."

            );

        } else if (currentUser

                .getRole()

                .equals("Technician")) {

            permissionLabel.setText(

                    "Technician access: View, assign, "

                    + "manage, and comment on all tickets."

            );

        } else {

            permissionLabel.setText(

                    "Administrator access: "

                    + "Full ticket management."

            );

        }

        Button createTicketButton =

                new Button(

                        "Create Ticket"

                );

        Button updateTicketButton =

                new Button(

                        "Update Ticket"

                );

        Button commentsButton =

                new Button(

                        "View Details / Comments"

                );

        Button clearSelectionButton =

                new Button(

                        "Clear Selection"

                );

        Button refreshButton =

                new Button(

                        "Refresh"

                );

        Button logoutButton =

                new Button(

                        "Logout"

                );

        updateTicketButton.setDisable(

                !canManageTickets()

        );

        Label searchLabel =

                new Label("Search:");

        TextField searchField =

                new TextField();

        searchField.setPromptText(

                "Search tickets..."

        );

        searchField.setPrefWidth(350);
        
        Label statusFilterLabel =
                new Label("Status:");

        ComboBox<String> statusFilterBox =
                new ComboBox<>();

        statusFilterBox.getItems().addAll(
                "All Statuses",
                "Open",
                "In Progress",
                "Resolved",
                "Closed"
        );

        statusFilterBox.setValue(
                "All Statuses"
        );
        
        

        TableView<Ticket> ticketTable =

                new TableView<>();

        FilteredList<Ticket> filteredTickets =

                new FilteredList<>(

                        tickets,

                        ticket -> true

                );

        TableColumn<Ticket, Integer> idColumn =

                new TableColumn<>("ID");

        idColumn.setCellValueFactory(

                new PropertyValueFactory<>(

                        "id"

                )

        );
        
        Label priorityFilterLabel =
                new Label("Priority:");

        ComboBox<String> priorityFilterBox =
                new ComboBox<>();

        priorityFilterBox.getItems().addAll(
                "All Priorities",
                "Low",
                "Medium",
                "High",
                "Critical"
        );

        priorityFilterBox.setValue(
                "All Priorities"
        );
        
        Label categoryFilterLabel =
                new Label("Category:");

        ComboBox<String> categoryFilterBox =
                new ComboBox<>();

        categoryFilterBox.getItems().addAll(
                "All Categories",
                "Hardware",
                "Software",
                "Network",
                "Account Access",
                "Security",
                "Other"
        );

        categoryFilterBox.setValue(
                "All Categories"
        );

        TableColumn<Ticket, String> titleColumn =

                new TableColumn<>("Title");

        titleColumn.setCellValueFactory(

                new PropertyValueFactory<>(

                        "title"

                )

        );

        TableColumn<Ticket, String> categoryColumn =

                new TableColumn<>("Category");

        categoryColumn.setCellValueFactory(

                new PropertyValueFactory<>(

                        "category"

                )

        );

        TableColumn<Ticket, String> priorityColumn =

                new TableColumn<>("Priority");

        priorityColumn.setCellValueFactory(

                new PropertyValueFactory<>(

                        "priority"

                )

        );

        TableColumn<Ticket, String> statusColumn =

                new TableColumn<>("Status");

        statusColumn.setCellValueFactory(

                new PropertyValueFactory<>(

                        "status"

                )

        );

        TableColumn<Ticket, String> createdByColumn =

                new TableColumn<>(

                        "Created By"

                );

        createdByColumn.setCellValueFactory(

                cellData -> {

                    int userId =

                            cellData

                                    .getValue()

                                    .getCreatedBy();

                    return new SimpleStringProperty(

                            getUserDisplayName(

                                    userId,

                                    "Unknown"

                            )

                    );

                }

        );

        TableColumn<Ticket, String> assignedColumn =

                new TableColumn<>(

                        "Assigned To"

                );

        assignedColumn.setCellValueFactory(

                cellData -> {

                    int userId =

                            cellData

                                    .getValue()

                                    .getAssignedTo();

                    return new SimpleStringProperty(

                            getUserDisplayName(

                                    userId,

                                    "Unassigned"

                            )

                    );

                }

        );

        ticketTable.getColumns().add(

                idColumn

        );

        ticketTable.getColumns().add(

                titleColumn

        );

        ticketTable.getColumns().add(

                categoryColumn

        );

        ticketTable.getColumns().add(

                priorityColumn

        );

        ticketTable.getColumns().add(

                statusColumn

        );

        ticketTable.getColumns().add(

                createdByColumn

        );

        ticketTable.getColumns().add(

                assignedColumn

        );

        ticketTable.setItems(

                filteredTickets

        );

        searchField.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        applyTicketFilters(
                                filteredTickets,
                                searchField.getText(),
                                statusFilterBox.getValue(),
                                priorityFilterBox.getValue(),
                                categoryFilterBox.getValue()
                        )
        );

        statusFilterBox.setOnAction(
                event ->
                        applyTicketFilters(
                                filteredTickets,
                                searchField.getText(),
                                statusFilterBox.getValue(),
                                priorityFilterBox.getValue(),
                                categoryFilterBox.getValue()
                        )
        );

        priorityFilterBox.setOnAction(
                event ->
                        applyTicketFilters(
                                filteredTickets,
                                searchField.getText(),
                                statusFilterBox.getValue(),
                                priorityFilterBox.getValue(),
                                categoryFilterBox.getValue()
                        )
        );

        categoryFilterBox.setOnAction(
                event ->
                        applyTicketFilters(
                                filteredTickets,
                                searchField.getText(),
                                statusFilterBox.getValue(),
                                priorityFilterBox.getValue(),
                                categoryFilterBox.getValue()
                        )
        );

        ticketTable.setColumnResizePolicy(

                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN

        );

        createTicketButton.setOnAction(

                event ->

                        showCreateTicketWindow()

        );

        updateTicketButton.setOnAction(event -> {

            if (!canManageTickets()) {

                showAlert(

                        Alert.AlertType.WARNING,

                        "Access Denied",

                        "You do not have permission to manage tickets.",

                        "Ticket management is available to "

                        + "Technicians and Administrators."

                );

                return;

            }

            Ticket selectedTicket =

                    ticketTable

                            .getSelectionModel()

                            .getSelectedItem();

            if (selectedTicket == null) {

                showAlert(

                        Alert.AlertType.WARNING,

                        "No Ticket Selected",

                        "Select a ticket",

                        "Please select a ticket before "

                        + "clicking Update Ticket."

                );

                return;

            }

            showUpdateTicketWindow(

                    selectedTicket,

                    ticketTable

            );

        });

        commentsButton.setOnAction(event -> {

            Ticket selectedTicket =

                    ticketTable

                            .getSelectionModel()

                            .getSelectedItem();

            if (selectedTicket == null) {

                showAlert(

                        Alert.AlertType.WARNING,

                        "No Ticket Selected",

                        "Select a ticket",

                        "Please select a ticket before "

                        + "opening ticket details."

                );

                return;

            }

            showTicketDetailsWindow(

                    selectedTicket

            );

        });

        clearSelectionButton.setOnAction(

                event ->

                        ticketTable

                                .getSelectionModel()

                                .clearSelection()

        );

        refreshButton.setOnAction(event -> {

            loadTicketsForCurrentUser();

            ticketTable.refresh();

            ticketTable

                    .getSelectionModel()

                    .clearSelection();

        });

        logoutButton.setOnAction(event -> {

            System.out.println(

                    "User logged out: "

                    + currentUser.getUsername()

            );

            showLoginScreen();

        });

        HBox buttonBar =

                new HBox(10);

        buttonBar.getChildren().addAll(

                createTicketButton,

                updateTicketButton,

                commentsButton,

                clearSelectionButton,

                refreshButton,

                logoutButton

        );

        HBox searchBar =
                new HBox(
                        10,
                        searchLabel,
                        searchField,
                        statusFilterLabel,
                        statusFilterBox,
                        priorityFilterLabel,
                        priorityFilterBox,
                        categoryFilterLabel,
                        categoryFilterBox
                );

        searchBar.setAlignment(
                Pos.CENTER_LEFT
        );
        
        HBox statisticsBar =
                new HBox(
                        25,
                        totalTicketsLabel,
                        openTicketsLabel,
                        inProgressTicketsLabel,
                        resolvedTicketsLabel,
                        closedTicketsLabel
                );

        statisticsBar.setAlignment(
                Pos.CENTER_LEFT
        );
        
        updateDashboardStatistics();
        
     // Automatically update statistics when
     // the ticket list changes.
     tickets.addListener(
             (javafx.collections.ListChangeListener<Ticket>) change -> {

                 updateDashboardStatistics();
             }
     );



        VBox header =

                new VBox(10);

        header.setPadding(

                new Insets(

                        0,

                        0,

                        20,

                        0

                )

        );

        header.getChildren().addAll(

                titleLabel,

                userLabel,

                permissionLabel,

                searchBar,
                
                statisticsBar,

                buttonBar

        );

        root.setTop(

                header

        );

        root.setCenter(

                ticketTable

        );

        Scene dashboardScene =

                new Scene(

                        root,

                        1100,

                        600

                );

        primaryStage.setScene(

                dashboardScene

        );

        primaryStage.setTitle(

                "Help Desk Ticketing System - "

                + currentUser.getRole()

        );

    }

    /* Opens the Create Ticket window */

    private void showCreateTicketWindow() {

        Stage ticketStage =

                new Stage();

        ticketStage.setTitle(

                "Create Ticket"

        );

        Label titleLabel =

                new Label("Title:");

        TextField titleField =

                new TextField();

        Label descriptionLabel =

                new Label("Description:");

        TextArea descriptionArea =

                new TextArea();

        descriptionArea.setPrefRowCount(4);

        descriptionArea.setWrapText(true);

        Label categoryLabel =

                new Label("Category:");

        ComboBox<String> categoryBox =

                new ComboBox<>();

        categoryBox.getItems().addAll(

                "Hardware",

                "Software",

                "Network",

                "Account Access",

                "Security",

                "Other"

        );

        categoryBox.setValue(

                "Hardware"

        );

        Label priorityLabel =

                new Label("Priority:");

        ComboBox<String> priorityBox =

                new ComboBox<>();

        priorityBox.getItems().addAll(

                "Low",

                "Medium",

                "High",

                "Critical"

        );

        priorityBox.setValue(

                "Medium"

        );

        Button submitButton =

                new Button(

                        "Create Ticket"

                );

        Button cancelButton =

                new Button(

                        "Cancel"

                );

        submitButton.setOnAction(event -> {

            String title =

                    titleField

                            .getText()

                            .trim();

            String description =

                    descriptionArea

                            .getText()

                            .trim();

            String category =

                    categoryBox.getValue();

            String priority =

                    priorityBox.getValue();

            if (title.isEmpty()

                    || description.isEmpty()) {

            	

                showAlert(

                        Alert.AlertType.WARNING,

                        "Invalid Ticket",

                        "Required information is missing",

                        "Please enter both a title and description."

                );

                return;

            }

            Ticket ticketToSave =

                    new Ticket(

                            0,

                            title,

                            description,

                            category,

                            priority,

                            "Open",

                            currentUser.getId(),

                            0

                    );

            int generatedId =

                    ticketRepository.save(

                            ticketToSave

                    );

            if (generatedId != -1) {

                Ticket savedTicket =

                        new Ticket(

                                generatedId,

                                title,

                                description,

                                category,

                                priority,

                                "Open",

                                currentUser.getId(),

                                0

                        );

                tickets.add(

                        savedTicket

                );

                ticketStage.close();

            } else {

                showAlert(

                        Alert.AlertType.ERROR,

                        "Ticket Creation Failed",

                        "The ticket could not be saved.",

                        "A database error occurred. Please try again."

                );

            }

        });

        cancelButton.setOnAction(

                event ->

                        ticketStage.close()

        );

        GridPane form =

                new GridPane();

        form.setPadding(

                new Insets(20)

        );

        form.setHgap(10);

        form.setVgap(10);

        form.add(

                titleLabel,

                0,

                0

        );

        form.add(

                titleField,

                1,

                0

        );

        form.add(

                descriptionLabel,

                0,

                1

        );

        form.add(

                descriptionArea,

                1,

                1

        );

        form.add(

                categoryLabel,

                0,

                2

        );

        form.add(

                categoryBox,

                1,

                2

        );

        form.add(

                priorityLabel,

                0,

                3

        );

        form.add(

                priorityBox,

                1,

                3

        );

        form.add(

                cancelButton,

                0,

                4

        );

        form.add(

                submitButton,

                1,

                4

        );

        Scene scene =

                new Scene(

                        form,

                        500,

                        350

                );

        ticketStage.setScene(

                scene

        );

        ticketStage.show();

    }

    /* Opens the Update Ticket window */

    private void showUpdateTicketWindow(

            Ticket ticket,

            TableView<Ticket> ticketTable) {

        if (!canManageTickets()) {

            showAlert(

                    Alert.AlertType.WARNING,

                    "Access Denied",

                    "You do not have permission to update tickets.",

                    "Only Technicians and Administrators "

                    + "can manage tickets."

            );

            return;

        }

        Stage updateStage =

                new Stage();

        updateStage.setTitle(

                "Update Ticket"

        );

        Label idLabel =

                new Label("Ticket ID:");

        Label idValue =

                new Label(

                        String.valueOf(

                                ticket.getId()

                        )

                );

        Label titleLabel =

                new Label("Title:");

        TextField titleField =

                new TextField(

                        ticket.getTitle()

                );

        Label descriptionLabel =

                new Label("Description:");

        TextArea descriptionArea =

                new TextArea(

                        ticket.getDescription()

                );

        descriptionArea.setPrefRowCount(4);

        descriptionArea.setWrapText(true);

        Label categoryLabel =

                new Label("Category:");

        ComboBox<String> categoryBox =

                new ComboBox<>();

        categoryBox.getItems().addAll(

                "Hardware",

                "Software",

                "Network",

                "Account Access",

                "Security",

                "Other"

        );

        categoryBox.setValue(

                ticket.getCategory()

        );

        Label priorityLabel =

                new Label("Priority:");

        ComboBox<String> priorityBox =

                new ComboBox<>();

        priorityBox.getItems().addAll(

                "Low",

                "Medium",

                "High",

                "Critical"

        );

        priorityBox.setValue(

                ticket.getPriority()

        );

        Label statusLabel =

                new Label("Status:");

        ComboBox<String> statusBox =

                new ComboBox<>();

        statusBox.getItems().addAll(

                "Open",

                "In Progress",

                "Resolved",

                "Closed"

        );

        statusBox.setValue(

                ticket.getStatus()

        );

        Label assignedLabel =

                new Label(

                        "Assigned Technician:"

                );

        ComboBox<User> technicianBox =

                new ComboBox<>();

        List<User> technicians =

                userRepository.findTechnicians();

        technicianBox.getItems().addAll(

                technicians

        );

        technicianBox.setConverter(

                new StringConverter<User>() {

                    /* Override */

                    public String toString(

                            User user) {

                        if (user == null) {

                            return "Unassigned";

                        }

                        return user.getName()

                                + " ("

                                + user.getUsername()

                                + ")";

                    }

                    /** Override */

                    public User fromString(

                            String string) {

                        return null;

                    }

                }

        );

        if (ticket.getAssignedTo() != 0) {

            for (User technician : technicians) {

                if (technician.getId()

                        == ticket.getAssignedTo()) {

                    technicianBox.setValue(

                            technician

                    );

                    break;

                }

            }

        }

        technicianBox.setPromptText(

                "Unassigned"

        );

        Button clearAssignmentButton =

                new Button(

                        "Unassign"

                );

        clearAssignmentButton.setOnAction(

                event ->

                        technicianBox.setValue(null)

        );

        Button saveButton =

                new Button(

                        "Save Changes"

                );

        Button cancelButton =

                new Button(

                        "Cancel"

                );

        saveButton.setOnAction(event -> {

            if (!canManageTickets()) {

                showAlert(

                        Alert.AlertType.WARNING,

                        "Access Denied",

                        "You do not have permission to update tickets.",

                        "Only Technicians and Administrators "

                        + "can manage tickets."

                );

                updateStage.close();

                return;

            }

            String title =

                    titleField

                            .getText()

                            .trim();

            String description =

                    descriptionArea

                            .getText()

                            .trim();

            String category =

                    categoryBox.getValue();

            String priority =

                    priorityBox.getValue();

            String status =

                    statusBox.getValue();

            if (title.isEmpty()

                    || description.isEmpty()) {

                showAlert(

                        Alert.AlertType.WARNING,

                        "Invalid Ticket",

                        "Required information is missing",

                        "Please enter both a title and description."

                );

                return;

            }

            if (!isValidStatusTransition(

                    ticket.getStatus(),

                    status)) {

                showAlert(

                        Alert.AlertType.WARNING,

                        "Invalid Status Change",

                        "This status change is not allowed.",

                        "A ticket cannot move directly from "

                        + ticket.getStatus()

                        + " to "

                        + status

                        + "."

                );

                return;

            }

            User selectedTechnician =

                    technicianBox.getValue();

            int assignedTo = 0;

            if (selectedTechnician != null) {

                assignedTo =

                        selectedTechnician.getId();

            }

            Ticket updatedTicket =

                    new Ticket(

                            ticket.getId(),

                            title,

                            description,

                            category,

                            priority,

                            status,

                            ticket.getCreatedBy(),

                            assignedTo

                    );

            boolean updateSuccessful =

                    ticketRepository.update(

                            updatedTicket

                    );

            if (!updateSuccessful) {

                showAlert(

                        Alert.AlertType.ERROR,

                        "Update Failed",

                        "The ticket could not be updated.",

                        "A database error occurred. "

                        + "Your changes were not applied."

                );

                return;

            }

            ticket.setTitle(

                    title

            );

            ticket.setDescription(

                    description

            );

            ticket.setCategory(

                    category

            );

            ticket.setPriority(

                    priority

            );

            ticket.setStatus(

                    status

            );

            ticket.setAssignedTo(
                    assignedTo
            );

            ticketTable.refresh();


            // Recalculate dashboard statistics after
            // an existing ticket is updated.
            updateDashboardStatistics();


            ticketTable
                    .getSelectionModel()
                    .clearSelection();

            updateStage.close();

        });

        cancelButton.setOnAction(

                event ->

                        updateStage.close()

        );

        GridPane form =

                new GridPane();

        form.setPadding(

                new Insets(20)

        );

        form.setHgap(10);

        form.setVgap(10);

        form.add(

                idLabel,

                0,

                0

        );

        form.add(

                idValue,

                1,

                0

        );

        form.add(

                titleLabel,

                0,

                1

        );

        form.add(

                titleField,

                1,

                1

        );

        form.add(

                descriptionLabel,

                0,

                2

        );

        form.add(

                descriptionArea,

                1,

                2

        );

        form.add(

                categoryLabel,

                0,

                3

        );

        form.add(

                categoryBox,

                1,

                3

        );

        form.add(

                priorityLabel,

                0,

                4

        );

        form.add(

                priorityBox,

                1,

                4

        );

        form.add(

                statusLabel,

                0,

                5

        );

        form.add(

                statusBox,

                1,

                5

        );

        form.add(

                assignedLabel,

                0,

                6

        );

        form.add(

                technicianBox,

                1,

                6

        );

        form.add(

                clearAssignmentButton,

                2,

                6

        );

        form.add(

                cancelButton,

                0,

                7

        );

        form.add(

                saveButton,

                1,

                7

        );

        Scene scene =

                new Scene(

                        form,

                        650,

                        475

                );

        updateStage.setScene(

                scene

        );

        updateStage.show();

    }

    /* Opens the ticket details and comments window */

    private void showTicketDetailsWindow(

            Ticket ticket) {

        Stage detailsStage =

                new Stage();

        detailsStage.setTitle(

                "Ticket #"

                + ticket.getId()

                + " - Details and Comments"

        );

        Label headingLabel =

                new Label(

                        "Ticket #"

                        + ticket.getId()

                        + " - "

                        + ticket.getTitle()

                );

        headingLabel.setStyle(

                "-fx-font-size: 20px;"

                + "-fx-font-weight: bold;"

        );

        String createdByText =

                getUserDisplayName(

                        ticket.getCreatedBy(),

                        "Unknown"

                );

        String assignedToText =

                getUserDisplayName(

                        ticket.getAssignedTo(),

                        "Unassigned"

                );

        Label categoryLabel =

                new Label(

                        "Category: "

                        + ticket.getCategory()

                );

        Label priorityLabel =

                new Label(

                        "Priority: "

                        + ticket.getPriority()

                );

        Label statusLabel =

                new Label(

                        "Status: "

                        + ticket.getStatus()

                );

        Label createdByLabel =

                new Label(

                        "Created By: "

                        + createdByText

                );

        Label assignedToLabel =

                new Label(

                        "Assigned To: "

                        + assignedToText

                );

        Label descriptionTitle =

                new Label("Description");

        descriptionTitle.setStyle(

                "-fx-font-weight: bold;"

        );

        TextArea descriptionArea =

                new TextArea(

                        ticket.getDescription()

                );

        descriptionArea.setEditable(false);

        descriptionArea.setWrapText(true);

        descriptionArea.setPrefRowCount(4);

        Label commentsTitle =

                new Label("Comments");

        commentsTitle.setStyle(

                "-fx-font-size: 16px;"

                + "-fx-font-weight: bold;"

        );

        VBox commentsBox =

                new VBox(10);

        commentsBox.setPadding(

                new Insets(10)

        );

        refreshComments(

                ticket.getId(),

                commentsBox

        );

        ScrollPane commentsScrollPane =

                new ScrollPane(

                        commentsBox

                );

        commentsScrollPane.setFitToWidth(true);

        commentsScrollPane.setPrefHeight(250);

        TextArea commentArea =

                new TextArea();

        commentArea.setPromptText(

                "Enter a comment..."

        );

        commentArea.setWrapText(true);

        commentArea.setPrefRowCount(3);

        Button addCommentButton =

                new Button(

                        "Add Comment"

                );

        Button closeButton =

                new Button(

                        "Close"

                );

        addCommentButton.setOnAction(event -> {

            String commentText =

                    commentArea

                            .getText()

                            .trim();

            if (commentText.isEmpty()) {

                showAlert(

                        Alert.AlertType.WARNING,

                        "Comment Required",

                        "The comment is empty.",

                        "Please enter a comment "

                        + "before submitting."

                );

                return;

            }

            if (currentUser == null) {

                showAlert(

                        Alert.AlertType.ERROR,

                        "Session Error",

                        "No authenticated user was found.",

                        "Please log in again."

                );

                detailsStage.close();

                return;

            }

            TicketComment comment =

                    new TicketComment(

                            0,

                            ticket.getId(),

                            currentUser.getId(),

                            commentText,

                            LocalDateTime.now()

                    );

            int generatedId =

                    commentRepository.save(

                            comment

                    );

            if (generatedId == -1) {

                showAlert(

                        Alert.AlertType.ERROR,

                        "Comment Failed",

                        "The comment could not be saved.",

                        "A database error occurred. "

                        + "Please try again."

                );

                return;

            }

            commentArea.clear();

            refreshComments(

                    ticket.getId(),

                    commentsBox

            );

        });

        closeButton.setOnAction(

                event ->

                        detailsStage.close()

        );

        HBox commentButtons =

                new HBox(

                        10,

                        addCommentButton,

                        closeButton

                );

        VBox root =

                new VBox(12);

        root.setPadding(

                new Insets(20)

        );

        root.getChildren().addAll(

                headingLabel,

                categoryLabel,

                priorityLabel,

                statusLabel,

                createdByLabel,

                assignedToLabel,

                descriptionTitle,

                descriptionArea,

                commentsTitle,

                commentsScrollPane,

                commentArea,

                commentButtons

        );

        Scene scene =

                new Scene(

                        root,

                        700,

                        750

                );

        detailsStage.setScene(

                scene

        );

        detailsStage.show();

    }

    /* Reloads the comments for a specific ticket */

    private void refreshComments(

            int ticketId,

            VBox commentsBox) {

        commentsBox

                .getChildren()

                .clear();

        List<TicketComment> comments =

                commentRepository.findByTicketId(

                        ticketId

                );

        if (comments.isEmpty()) {

            Label noCommentsLabel =

                    new Label(

                            "No comments have been added yet."

                    );

            commentsBox

                    .getChildren()

                    .add(

                            noCommentsLabel

                    );

            return;

        }

        DateTimeFormatter formatter =

                DateTimeFormatter.ofPattern(

                        "MM/dd/yyyy h:mm a"

                );

        for (TicketComment comment : comments) {

            String author =

                    getUserDisplayName(

                            comment.getUserId(),

                            "Unknown User"

                    );

            Label authorLabel =

                    new Label(

                            author

                    );

            authorLabel.setStyle(

                    "-fx-font-weight: bold;"

            );

            Label dateLabel =

                    new Label(

                            comment

                                    .getCreatedAt()

                                    .format(formatter)

                    );

            TextArea commentText =

                    new TextArea(

                            comment.getCommentText()

                    );

            commentText.setEditable(false);

            commentText.setWrapText(true);

            commentText.setPrefRowCount(2);

            VBox commentCard =

                    new VBox(

                            4,

                            authorLabel,

                            dateLabel,

                            commentText

                    );

            commentCard.setPadding(

                    new Insets(8)

            );

            commentCard.setStyle(

                    "-fx-border-color: lightgray;"

                    + "-fx-border-radius: 4;"

                    + "-fx-background-radius: 4;"

            );

            commentsBox

                    .getChildren()

                    .add(

                            commentCard

                    );

        }

    }

    /* Converts a database user ID into a readable name and role */

    private String getUserDisplayName(

            int userId,

            String emptyValue) {

        if (userId == 0) {

            return emptyValue;

        }

        User user =

                userRepository.findById(

                        userId

                );

        if (user == null) {

            return "Unknown";

        }

        return user.getName()

                + " ("

                + user.getRole()

                + ")";

    }

    /*  Enforces the ticket state machine */

    private boolean isValidStatusTransition(

            String currentStatus,

            String newStatus) {

        if (currentStatus.equals(

                newStatus)) {

            return true;

        }

        return switch (currentStatus) {

            case "Open" ->

                    newStatus.equals(

                            "In Progress"

                    );

            case "In Progress" ->

                    newStatus.equals(

                            "Resolved"

                    );

            case "Resolved" ->

                    newStatus.equals(

                            "Closed"

                    )

                    ||

                    newStatus.equals(

                            "In Progress"

                    );

            case "Closed" ->

                    false;

            default ->

                    false;

        };

    }
    
    /**
     * Updates the ticket statistics displayed
     * on the dashboard.
     */
    private void updateDashboardStatistics() {

        long total =
                tickets.size();

        long open =
                tickets.stream()
                        .filter(ticket ->
                                "Open".equals(
                                        ticket.getStatus()
                                ))
                        .count();

        long inProgress =
                tickets.stream()
                        .filter(ticket ->
                                "In Progress".equals(
                                        ticket.getStatus()
                                ))
                        .count();

        long resolved =
                tickets.stream()
                        .filter(ticket ->
                                "Resolved".equals(
                                        ticket.getStatus()
                                ))
                        .count();

        long closed =
                tickets.stream()
                        .filter(ticket ->
                                "Closed".equals(
                                        ticket.getStatus()
                                ))
                        .count();

        totalTicketsLabel.setText(
                "Total Tickets: " + total
        );

        openTicketsLabel.setText(
                "Open: " + open
        );

        inProgressTicketsLabel.setText(
                "In Progress: " + inProgress
        );

        resolvedTicketsLabel.setText(
                "Resolved: " + resolved
        );

        closedTicketsLabel.setText(
                "Closed: " + closed
        );
    }
    
    /**
     * Applies the dashboard search, status,
     * priority, and category filters.
     */
    private void applyTicketFilters(
            FilteredList<Ticket> filteredTickets,
            String searchValue,
            String statusValue,
            String priorityValue,
            String categoryValue) {

        String searchText =
                searchValue == null
                        ? ""
                        : searchValue
                                .trim()
                                .toLowerCase();

        String selectedStatus =
                statusValue == null
                        ? "All Statuses"
                        : statusValue;

        String selectedPriority =
                priorityValue == null
                        ? "All Priorities"
                        : priorityValue;

        String selectedCategory =
                categoryValue == null
                        ? "All Categories"
                        : categoryValue;

        filteredTickets.setPredicate(ticket -> {

            boolean matchesStatus =
                    selectedStatus.equals("All Statuses")
                    || ticket.getStatus().equals(
                            selectedStatus
                    );

            boolean matchesPriority =
                    selectedPriority.equals("All Priorities")
                    || ticket.getPriority().equals(
                            selectedPriority
                    );

            boolean matchesCategory =
                    selectedCategory.equals("All Categories")
                    || ticket.getCategory().equals(
                            selectedCategory
                    );

            if (!matchesStatus
                    || !matchesPriority
                    || !matchesCategory) {

                return false;
            }

            if (searchText.isEmpty()) {
                return true;
            }

            if (String.valueOf(ticket.getId())
                    .contains(searchText)) {

                return true;
            }

            if (containsIgnoreCase(
                    ticket.getTitle(),
                    searchText)) {

                return true;
            }

            if (containsIgnoreCase(
                    ticket.getDescription(),
                    searchText)) {

                return true;
            }

            if (containsIgnoreCase(
                    ticket.getCategory(),
                    searchText)) {

                return true;
            }

            if (containsIgnoreCase(
                    ticket.getPriority(),
                    searchText)) {

                return true;
            }

            if (containsIgnoreCase(
                    ticket.getStatus(),
                    searchText)) {

                return true;
            }

            String createdBy =
                    getUserDisplayName(
                            ticket.getCreatedBy(),
                            "Unknown"
                    );

            if (containsIgnoreCase(
                    createdBy,
                    searchText)) {

                return true;
            }

            String assignedTo =
                    getUserDisplayName(
                            ticket.getAssignedTo(),
                            "Unassigned"
                    );

            return containsIgnoreCase(
                    assignedTo,
                    searchText
            );
        });
    }

        /* Performs a case-insensitive text comparison
         * for the dashboard search feature */
    private boolean containsIgnoreCase(
            String value,
            String searchText) {

        if (value == null) {
            return false;
        }

        return value
                .toLowerCase()
                .contains(searchText);
    }

/* Displays a reusable JavaFX alert */

    private void showAlert(

            Alert.AlertType type,

            String title,

            String header,

            String message) {

        Alert alert =

                new Alert(type);

        alert.setTitle(

                title

        );

        alert.setHeaderText(

                header

        );

        alert.setContentText(

                message

        );

        alert.showAndWait();

    }

    public static void main(

            String[] args) {

        launch(args);

    }

}