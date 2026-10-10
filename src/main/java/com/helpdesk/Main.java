package com.helpdesk;

import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import com.helpdesk.validation.TicketValidator;

import com.helpdesk.validation.TicketStatusValidator;

import com.helpdesk.security.TicketAuthorization;

public class Main extends Application {
	private static final Logger LOGGER =
	        Logger.getLogger(Main.class.getName());

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

        try {

            DatabaseManager.initializeDatabase();

            userRepository.createDefaultUsers();

            showLoginScreen();

            primaryStage.show();

        } catch (IllegalStateException e) {

            LOGGER.log(
                    Level.SEVERE,
                    "Application startup failed.",
                    e
            );

            showAlert(
                    Alert.AlertType.ERROR,
                    "Startup Failed",
                    "The application could not start.",
                    "The database could not be initialized. "
                    + "Please check the database configuration "
                    + "and try again."
            );

        }
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
                + "-fx-text-fill: #17365d;"
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
        String loginInputStyle =
                "-fx-font-size: 13px;"
                + "-fx-background-color: white;"
                + "-fx-background-radius: 6px;"
                + "-fx-border-color: #cbd5e1;"
                + "-fx-border-radius: 6px;"
                + "-fx-padding: 8px 12px;";

        usernameField.setStyle(loginInputStyle);
        passwordField.setStyle(loginInputStyle);

        Button loginButton =

                new Button("Login");
        loginButton.setStyle(
                "-fx-background-color: #17365d;"
                + "-fx-text-fill: white;"
                + "-fx-font-size: 13px;"
                + "-fx-font-weight: bold;"
                + "-fx-padding: 9px 24px;"
                + "-fx-background-radius: 6px;"
                + "-fx-cursor: hand;"
        );

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

            loadTicketsForCurrentUser();

            showDashboard();

        });

        VBox loginBox =

                new VBox(12);
        loginBox.setStyle(
                "-fx-background-color: #f4f7fb;"
        );

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

        if (currentUser == null) {
            return;
        }

        try {

            List<Ticket> loadedTickets;

            String role = currentUser.getRole();

            if ("Employee".equals(role)) {

                loadedTickets = ticketRepository.findByCreatedBy(
                        currentUser.getId()
                );

            } else if ("Technician".equals(role)
                    || "Administrator".equals(role)) {

                loadedTickets = ticketRepository.findAll();

            } else {

                throw new SecurityException(
                        "Unauthorized user role: access denied."
                );
            }

            // Replace displayed tickets only after
            // the database query succeeds.
            tickets.setAll(loadedTickets);

        } catch (IllegalStateException e) {

        	LOGGER.log(
        	        Level.SEVERE,
        	        "Unable to refresh the ticket dashboard.",
        	        e
        	);

            showAlert(
                    Alert.AlertType.ERROR,
                    "Ticket Loading Failed",
                    "Unable to load tickets.",
                    "A database error occurred. "
                    + "The previously loaded tickets "
                    + "will remain displayed."
            );
        }
    }

    /* Determines whether the current user can manage tickets */
    private boolean canManageTickets() {

        return TicketAuthorization.canManageTickets(currentUser);

    }

    /* Displays the main dashboard */

    private void showDashboard() {

        BorderPane root =

                new BorderPane();

        root.setPadding(

                new Insets(20)

        );
        root.setStyle(
        	    "-fx-background-color: #f4f7fb;"
        	);

        Label titleLabel =

                new Label(

                        "Help Desk Ticketing System"

                );
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setAlignment(Pos.CENTER);

        titleLabel.setStyle(

                "-fx-font-size: 24px;"

                + "-fx-font-weight: bold;"

        );
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setAlignment(Pos.CENTER);

        titleLabel.setStyle(
            "-fx-font-size: 26px;"
            + "-fx-font-weight: bold;"
            + "-fx-text-fill: #17365d;"
            + "-fx-padding: 8px 0 12px 0;"
        );

        Label userLabel =

                new Label(

                        "Welcome, "

                        + currentUser.getName()

                        + " ("

                        + currentUser.getRole()

                        + ")"

                );
        userLabel.setStyle(
                "-fx-font-size: 15px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: #334155;"
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
        permissionLabel.setStyle(
                "-fx-font-size: 12px;"
                + "-fx-text-fill: #64748b;"
                + "-fx-padding: 0 0 6px 0;"
        );

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

        searchField.setPrefWidth(300);

        searchField.setStyle(
            "-fx-font-size: 13px;"
            + "-fx-padding: 8px 12px;"
            + "-fx-background-color: white;"
            + "-fx-border-color: #cbd5e1;"
            + "-fx-border-radius: 6px;"
            + "-fx-background-radius: 6px;"
        );
        
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
        statusFilterBox.setPrefWidth(150);

        statusFilterBox.setStyle(
                "-fx-font-size: 13px;"
                + "-fx-background-color: white;"
                + "-fx-border-color: #cbd5e1;"
                + "-fx-border-radius: 6px;"
                + "-fx-background-radius: 6px;"
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
        priorityFilterBox.setPrefWidth(150);

        priorityFilterBox.setStyle(
                "-fx-font-size: 13px;"
                + "-fx-background-color: white;"
                + "-fx-border-color: #cbd5e1;"
                + "-fx-border-radius: 6px;"
                + "-fx-background-radius: 6px;"
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
        categoryFilterBox.setPrefWidth(150);

        categoryFilterBox.setStyle(
                "-fx-font-size: 13px;"
                + "-fx-background-color: white;"
                + "-fx-border-color: #cbd5e1;"
                + "-fx-border-radius: 6px;"
                + "-fx-background-radius: 6px;"
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
     // Improve ticket table readability.
        ticketTable.setFixedCellSize(34);

        ticketTable.setStyle(
            "-fx-font-size: 13px;"
            + "-fx-background-color: white;"
            + "-fx-border-color: #dce3eb;"
            + "-fx-border-radius: 6px;"
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

            LOGGER.log(
                    Level.INFO,
                    "User logged out. ID: {0}",
                    currentUser.getId()
            );

            showLoginScreen();

        });

        HBox buttonBar =
                new HBox(12);
     // Make dashboard action buttons easier to read.
        for (Button button : new Button[] {
                createTicketButton,
                updateTicketButton,
                commentsButton,
                clearSelectionButton,
                refreshButton,
                logoutButton
        }) {
        	button.setStyle(
        	        "-fx-background-color: #17365d;"
        	        + "-fx-text-fill: white;"
        	        + "-fx-font-size: 13px;"
        	        + "-fx-font-weight: bold;"
        	        + "-fx-padding: 9px 16px;"
        	        + "-fx-background-radius: 6px;"
        	        + "-fx-cursor: hand;"
        	);
        }

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

statisticsBar.setSpacing(12);

for (Label statisticLabel : new Label[] {
        totalTicketsLabel,
        openTicketsLabel,
        inProgressTicketsLabel,
        resolvedTicketsLabel,
        closedTicketsLabel
}) {
    statisticLabel.setStyle(
            "-fx-background-color: #f1f5f9;"
            + "-fx-background-radius: 8px;"
            + "-fx-border-color: #dce3eb;"
            + "-fx-border-radius: 8px;"
            + "-fx-padding: 12px 18px;"
            + "-fx-font-size: 13px;"
            + "-fx-font-weight: bold;"
            + "-fx-text-fill: #334155;"
    );
}


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
    	        new VBox(18);

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
                        1280,
                        720
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
        String inputStyle =
                "-fx-font-size: 13px;"
                + "-fx-background-radius: 6px;"
                + "-fx-border-color: #cbd5e1;"
                + "-fx-border-radius: 6px;";

        titleField.setStyle(inputStyle);
        descriptionArea.setStyle(inputStyle);

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
        categoryBox.setStyle(inputStyle);
        categoryBox.setPrefWidth(300);
       

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
        priorityBox.setStyle(inputStyle);
        priorityBox.setPrefWidth(300);
    
        Button submitButton =

                new Button(

                        "Create Ticket"

                );
        submitButton.setStyle(
                "-fx-background-color: #17365d;"
                + "-fx-text-fill: white;"
                + "-fx-font-size: 13px;"
                + "-fx-font-weight: bold;"
                + "-fx-padding: 9px 16px;"
                + "-fx-background-radius: 6px;"
                + "-fx-cursor: hand;"
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
            
         // Validate ticket title length.
            if (!TicketValidator.isValidTitle(title)) { 

                showAlert(
                        Alert.AlertType.WARNING,
                        "Invalid Ticket",
                        "Invalid title length",
                        "The ticket title must be between 3 and 100 characters."
                );

                return;
            }


            // Validate ticket description length.
            if (!TicketValidator.isValidDescription(description)) {

                showAlert(
                        Alert.AlertType.WARNING,
                        "Invalid Ticket",
                        "Invalid description length",
                        "The ticket description must be between 10 and 1000 characters."
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
        form.setStyle(
                "-fx-background-color: #f4f7fb;"
        );
        form.getColumnConstraints().addAll(
                new javafx.scene.layout.ColumnConstraints(110),
                new javafx.scene.layout.ColumnConstraints(300)
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

                        520,

                        380

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

        String inputStyle =
                "-fx-font-size: 13px;"
                + "-fx-background-radius: 6px;"
                + "-fx-border-color: #cbd5e1;"
                + "-fx-border-radius: 6px;";

        titleField.setStyle(inputStyle);
        descriptionArea.setStyle(inputStyle);

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
        categoryBox.setStyle(inputStyle);
        categoryBox.setPrefWidth(300);

        priorityBox.setStyle(inputStyle);
        priorityBox.setPrefWidth(300);

        statusBox.setStyle(inputStyle);
        statusBox.setPrefWidth(300);

        technicianBox.setStyle(inputStyle);
        technicianBox.setPrefWidth(300);

        List<User> technicians;

        try {

            technicians =
                    userRepository.findTechnicians();

        } catch (IllegalStateException e) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to load technicians for ticket assignment.",
                    e
            );

            showAlert(
                    Alert.AlertType.ERROR,
                    "Database Error",
                    "Unable to load technicians.",
                    "The technician list could not be retrieved. "
                    + "Please try opening the ticket again."
            );

            return;
        }

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
        saveButton.setStyle(
                "-fx-background-color: #17365d;"
                + "-fx-text-fill: white;"
                + "-fx-font-size: 13px;"
                + "-fx-font-weight: bold;"
                + "-fx-padding: 9px 16px;"
                + "-fx-background-radius: 6px;"
                + "-fx-cursor: hand;"
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
            
         // Validate ticket title length.
            if (!TicketValidator.isValidTitle(title)) {

                showAlert(
                        Alert.AlertType.WARNING,
                        "Invalid Ticket",
                        "Invalid title length",
                        "The ticket title must be between 3 and 100 characters."
                );

                return;
            }

            // Validate ticket description length.
            if (!TicketValidator.isValidDescription(description)) {

                showAlert(
                        Alert.AlertType.WARNING,
                        "Invalid Ticket",
                        "Invalid description length",
                        "The ticket description must be between 10 and 1000 characters."
                );

                return;
            }

            if (!TicketStatusValidator.isValidStatusTransition(
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
        form.setStyle(
                "-fx-background-color: #f4f7fb;"
        );
        form.getColumnConstraints().addAll(
                new javafx.scene.layout.ColumnConstraints(150),
                new javafx.scene.layout.ColumnConstraints(300)
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

                        520,

                        380

                );

        updateStage.setScene(

                scene

        );

        updateStage.show();

    }

    /* Opens the ticket details and comments window */

    private void showTicketDetailsWindow(

            Ticket ticket) {
    	// Verify that a user is authenticated.
    	if (currentUser == null || ticket == null) {

    	    showAlert(
    	            Alert.AlertType.ERROR,
    	            "Access Denied",
    	            "Unable to open ticket",
    	            "You must be logged in to view ticket details."
    	    );

    	    return;
    	}
    	// Retrieve the current ticket directly from SQLite.
Ticket storedTicket;

try {
    storedTicket = ticketRepository.findById(ticket.getId());
} catch (IllegalStateException e) {

    showAlert(
            Alert.AlertType.ERROR,
            "Database Error",
            "Unable to verify ticket permissions",
            "Please try again."
    );

    return;
}

if (storedTicket == null) {

    showAlert(
            Alert.AlertType.ERROR,
            "Ticket Not Found",
            "The selected ticket no longer exists.",
            "Please refresh the dashboard."
    );

    return;
}

//Verify that the user has permission to view this ticket.
if (!TicketAuthorization.canViewTicket(currentUser, storedTicket)) {

 showAlert(
         Alert.AlertType.ERROR,
         "Access Denied",
         "Insufficient permissions",
         "You do not have permission to view this ticket."
 );

 return;
}

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
                "-fx-font-size: 22px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: #17365d;"
                + "-fx-padding: 0 0 8px 0;"
        );
        headingLabel.setWrapText(true);

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
        String detailStyle =
                "-fx-font-size: 13px;"
                + "-fx-text-fill: #334155;";

        for (Label label : new Label[] {
                categoryLabel,
                priorityLabel,
                statusLabel,
                createdByLabel,
                assignedToLabel
        }) {
            label.setStyle(detailStyle);
        }

        Label descriptionTitle =

                new Label("Description");

        descriptionTitle.setStyle(
                "-fx-font-size: 16px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: #17365d;"
        );

        TextArea descriptionArea =

                new TextArea(

                        ticket.getDescription()

                );

        descriptionArea.setEditable(false);

        descriptionArea.setWrapText(true);

        descriptionArea.setPrefRowCount(4);
        descriptionArea.setStyle(
                "-fx-font-size: 13px;"
                + "-fx-background-radius: 6px;"
                + "-fx-border-color: #cbd5e1;"
                + "-fx-border-radius: 6px;"
        );
        descriptionArea.setFocusTraversable(false);

        Label commentsTitle =

                new Label("Comments");

        commentsTitle.setStyle(
                "-fx-font-size: 16px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: #17365d;"
        );
        VBox commentsBox =

                new VBox(10);

        commentsBox.setPadding(

                new Insets(10)

        );
        commentsBox.setStyle(
                "-fx-background-color: white;"
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
        commentsScrollPane.setStyle(
                "-fx-background-color: white;"
                + "-fx-border-color: #dce3eb;"
                + "-fx-border-radius: 6px;"
                + "-fx-background-radius: 6px;"
        );

        TextArea commentArea =

                new TextArea();

        commentArea.setPromptText(

                "Enter a comment..."

        );

        commentArea.setWrapText(true);

        commentArea.setPrefRowCount(3);
        commentArea.setStyle(
                "-fx-font-size: 13px;"
                + "-fx-background-radius: 6px;"
                + "-fx-border-color: #cbd5e1;"
                + "-fx-border-radius: 6px;"
        );
        Label commentCounter = new Label("0 / 2000 characters");
        commentCounter.setStyle(
                "-fx-font-size: 12px;"
                + "-fx-text-fill: #64748b;"
        );

        commentArea.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    int count = newValue.length();

                    commentCounter.setText(
                            count + " / 2000 characters"
                    );

                    if (count > 2000) {
                        commentCounter.setStyle("-fx-text-fill: red;");
                    } else if (count >= 1800) {
                        commentCounter.setStyle("-fx-text-fill: orange;");
                    } else {
                        commentCounter.setStyle("-fx-text-fill: gray;");
                    }
                }
        );

        Button addCommentButton =

                new Button(

                        "Add Comment"

                );

        Button closeButton =

                new Button(

                        "Close"

                );
        addCommentButton.setStyle(
                "-fx-background-color: #17365d;"
                + "-fx-text-fill: white;"
                + "-fx-font-size: 13px;"
                + "-fx-font-weight: bold;"
                + "-fx-padding: 9px 16px;"
                + "-fx-background-radius: 6px;"
                + "-fx-cursor: hand;"
        );

        closeButton.setStyle(
                "-fx-background-color: #e2e8f0;"
                + "-fx-text-fill: #334155;"
                + "-fx-font-size: 13px;"
                + "-fx-padding: 9px 16px;"
                + "-fx-background-radius: 6px;"
                + "-fx-cursor: hand;"
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
            
            if (commentText.length() > 2000) {
                showAlert(
                        Alert.AlertType.WARNING,
                        "Invalid Comment",
                        "Comment is too long",
                        "Comments cannot exceed 2,000 characters."
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
         // Verify ticket ownership against SQLite before saving.
            Ticket currentTicket;

            try {
                currentTicket = ticketRepository.findById(ticket.getId());
            } catch (IllegalStateException e) {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Database Error",
                        "Unable to verify comment permissions",
                        "Please try again."
                );

                return;
            }

            if (currentTicket == null) {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Ticket Not Found",
                        "This ticket no longer exists.",
                        "Please refresh the dashboard."
                );

                return;
            }

         // Verify comment permissions using the current database record.
            if (!TicketAuthorization.canCommentOnTicket(
                    currentUser,
                    currentTicket)) {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Access Denied",
                        "Comment permission denied",
                        "You do not have permission to comment on this ticket."
                );

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
        root.setStyle(
                "-fx-background-color: #f4f7fb;"
        );

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
                commentCounter,
                commentButtons

        );

        Scene scene =

                new Scene(

                        root,

                        760,

                        780

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

    	List<TicketComment> comments;

    	try {

    	    comments = commentRepository.findByTicketId(ticketId);

    	} catch (IllegalStateException e) {

    		LOGGER.log(
    		        Level.SEVERE,
    		        "Unable to refresh ticket comments.",
    		        e
    		);

    	    showAlert(
    	            Alert.AlertType.ERROR,
    	            "Comment Loading Failed",
    	            "Unable to load comments.",
    	            "A database error occurred. "
    	            + "Previously displayed comments will remain visible."
    	    );

    	    return;
    	}

    	// Clear the previous comments only after
    	// the database query succeeds.
    	commentsBox.getChildren().clear();

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
                    "-fx-font-size: 13px;"
                    + "-fx-font-weight: bold;"
                    + "-fx-text-fill: #17365d;"
            );

            Label dateLabel =

                    new Label(

                            comment

                                    .getCreatedAt()

                                    .format(formatter)

                    );
            dateLabel.setStyle(
                    "-fx-font-size: 11px;"
                    + "-fx-text-fill: #64748b;"
            );

            TextArea commentText =

                    new TextArea(

                            comment.getCommentText()

                    );

            commentText.setEditable(false);

            commentText.setWrapText(true);

            commentText.setPrefRowCount(2);
            commentText.setStyle(
                    "-fx-font-size: 13px;"
                    + "-fx-background-color: white;"
                    + "-fx-border-color: #e2e8f0;"
                    + "-fx-border-radius: 4px;"
            );
            commentText.setFocusTraversable(false);

            VBox commentCard =

                    new VBox(

                            4,

                            authorLabel,

                            dateLabel,

                            commentText

                    );

            commentCard.setPadding(

                    new Insets(12)

            );

            commentCard.setStyle(
                    "-fx-background-color: #f8fafc;"
                    + "-fx-border-color: #dce3eb;"
                    + "-fx-border-radius: 8px;"
                    + "-fx-background-radius: 8px;"
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

        try {

            User user =
                    userRepository.findById(userId);

            if (user == null) {
                return "Unknown";
            }

            return user.getName()
                    + " ("
                    + user.getRole()
                    + ")";

        } catch (IllegalStateException e) {

            LOGGER.log(
                    Level.SEVERE,
                    "Unable to retrieve display name for user ID: "
                            + userId,
                    e
            );

            return "Unavailable";
        }
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