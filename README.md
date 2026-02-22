# VOS-Desktop

A modern JavaFX-based desktop application for recruitment and contract management. This application provides a user-friendly interface for managing recruitment processes and employment contracts with a sleek, contemporary design.

## ?? Table of Contents

- [Features](#features)
- [Technologies](#technologies)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Running the Application](#running-the-application)
- [Configuration](#configuration)
- [Troubleshooting](#troubleshooting)
- [Project Structure Details](#project-structure-details)

## ? Features

- **Recruitment Management**: Create, view, and manage recruitment records
- **Contract Management**: Track and manage employment contracts
- **Modern UI**: Sleek, responsive JavaFX interface with theme toggle support
- **Splash Screen**: Professional splash screen on application startup
- **Theme Support**: Dark/light theme toggle for improved user experience
- **Database Integration**: Persistent data storage with database backend
- **Email Integration**: Send notifications and communications via email
- **Google Calendar Integration**: Sync recruitment events with Google Calendar
- **PDF Generation**: Generate PDF reports and contracts

## ??? Technologies

- **Language**: Java
- **UI Framework**: JavaFX
- **Build Tool**: Maven
- **Database**: MySQL/Database (via custom DB utility)
- **Styling**: CSS (Modern themes)
- **Architecture Pattern**: MVC (Model-View-Controller)
- **External APIs**: Google Calendar API, Email Services

## ?? Project Structure

`
VOS-Desktop/
+-- src/main/java/
¦   +-- application/          # Main application entry points
¦   ¦   +-- MainApp.java
¦   ¦   +-- SplashScreenLauncher.java
¦   ¦   +-- ThemeToggle.java
¦   +-- controllers/          # JavaFX Controllers
¦   ¦   +-- MainController.java
¦   ¦   +-- RecrutementController.java
¦   ¦   +-- ContratController.java
¦   ¦   +-- AdministrationController.java
¦   ¦   +-- SplashScreenController.java
¦   +-- entities/            # Data Models
¦   ¦   +-- Recrutement.java
¦   ¦   +-- Contrat.java
¦   ¦   +-- RecrutementGroup.java
¦   ¦   +-- RecrutementTableRow.java
¦   +-- services/            # Business Logic
¦   ¦   +-- ServiceRecrutement.java
¦   ¦   +-- ServiceContrat.java
¦   ¦   +-- EmailService.java
¦   ¦   +-- GoogleCalendarService.java
¦   ¦   +-- PDFService.java
¦   +-- utils/               # Utility Classes
¦   ¦   +-- MyDB.java
¦   +-- resources/
¦       +-- views/           # FXML files
¦       +-- styles/          # CSS stylesheets
¦       +-- Images/          # Application images
¦       +-- credentials.json # Google API credentials
+-- pom.xml
`

## ?? Prerequisites

- Java Development Kit (JDK) 11 or higher
- Apache Maven 3.6+
- MySQL Database (or compatible database)
- JavaFX SDK (typically included via Maven)

## ?? Installation

1. **Clone the repository**:
   `ash
   git clone <repository-url>
   cd VOS-Desktop
   `

2. **Configure database connection**:
   - Update the database credentials in src/main/java/utils/MyDB.java
   - Ensure your MySQL server is running

3. **Build the project**:
   `ash
   mvn clean install
   `

4. **Configure credentials** (Optional for advanced features):
   - **Google Calendar Integration**: Place your credentials.json in src/main/resources/
   - **Email Service**: Configure SMTP settings in the application

## ?? Running the Application

### Option 1: Run from Maven
`ash
mvn javafx:run
`

### Option 2: Run the JAR file
`ash
java -jar target/VOS-Desktop.jar
`

### Option 3: Run from IDE
- Open the project in your IDE (IntelliJ IDEA, Eclipse, etc.)
- Right-click MainApp.java and select "Run"

## ?? Configuration

### Database Configuration
Update the database credentials in src/main/java/utils/MyDB.java:
`java
// Configure your database connection parameters
String dbURL = "jdbc:mysql://localhost:3306/your_database";
String dbUser = "your_username";
String dbPassword = "your_password";
`

### Google Calendar Integration
1. Set up a Google Cloud Project
2. Create a service account and download credentials.json
3. Place the file in src/main/resources/credentials.json
4. Grant the service account access to your Google Calendar

### Email Configuration
To use the email service, configure:
- SMTP Server (e.g., smtp.gmail.com)
- Port (587 for TLS, 465 for SSL)
- Email credentials
- Sender address

## ?? Troubleshooting

### Common Issues

**Database Connection Failed**
- Ensure MySQL server is running and accessible on localhost:3306
- Verify database credentials in MyDB.java
- Check that the database exists and the required tables are created

**JavaFX Module Not Found**
- Ensure Java 11 or higher is installed: java -version
- Run mvn clean install to download all dependencies

**Google Calendar API Errors**
- Verify credentials.json exists in src/main/resources/
- Check that the service account has "Calendar API" enabled
- Ensure the credentials file contains valid Google API key

**Email Service Fails to Send**
- Verify SMTP server address and port are correct
- Check that email credentials are valid
- Ensure firewall/antivirus isn't blocking SMTP port
- For Gmail, use App Password instead of regular password

**Build Failures**
- Clear Maven cache: mvn clean
- Check Maven version: mvn --version (should be 3.6+)
- Verify JDK version: javac -version (should be 11+)
- Update dependencies: mvn clean install -U

## ??? Project Structure Details

### Application Layer (pplication/)
- **MainApp.java**: Entry point for the application
- **SplashScreenLauncher.java**: Manages splash screen display
- **ThemeToggle.java**: Handles application theme switching

### Controller Layer (controllers/)
- **MainController.java**: Main window controller
- **RecrutementController.java**: Manages recruitment UI and interactions
- **ContratController.java**: Manages contract UI and interactions
- **AdministrationController.java**: Administrative functionality
- **SplashScreenController.java**: Splash screen logic

### Entity Layer (entities/)
- **Recrutement.java**: Recruitment data model
- **Contrat.java**: Contract data model
- **RecrutementGroup.java**: Grouping for recruitment records
- **RecrutementTableRow.java**: Table row representation

### Service Layer (services/)
- **ServiceRecrutement.java**: Business logic for recruitment operations
- **ServiceContrat.java**: Business logic for contract operations
- **EmailService.java**: Email communication and notifications
- **GoogleCalendarService.java**: Integration with Google Calendar for scheduling
- **PDFService.java**: PDF document generation and management

### Utility Layer (utils/)
- **MyDB.java**: Database connection and operations manager

## ?? License

Manai Fares

## ?? Support

For support, please contact the development team or open an issue on the repository.

---

**Last Updated**: February 2026
